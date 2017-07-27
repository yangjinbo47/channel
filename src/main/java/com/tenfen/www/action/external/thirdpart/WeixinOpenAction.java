package com.tenfen.www.action.external.thirdpart;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.entity.operation.thirdpart.TThirdOrder;
import com.tenfen.entity.operation.thirdpart.TThirdSeller;
import com.tenfen.entity.operation.thirdpart.TThirdSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.thirdpart.ThirdpartOrderManager;
import com.tenfen.www.service.operation.thirdpart.ThirdpartSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class WeixinOpenAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private ThirdpartOrderManager thirdpartOrderManager;
	@Autowired
	private ThirdpartSellerManager thirdpartSellerManager;
	
	/**
	 * 生成单笔订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("weixinpay 参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			if (Utils.isEmpty(sellerKey)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "seller_key参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(appName)) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "app_name参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (fee == 0) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "fee参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(imsi)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "imsi参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "sign参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			TThirdSeller tThirdSeller = thirdpartSellerManager.getThirdSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tThirdSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tThirdSeller.getStatus() == 0) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			//-----secret修改
			String geneSign = TokenService.buildToken(queryParamList, tThirdSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TThirdSellerApps> thirdSellerAppList = tThirdSeller.getSellerApps();
			TThirdApp tThirdApp = null;
			if (thirdSellerAppList.size() > 0) {
				for (TThirdSellerApps tThirdSellerApps : thirdSellerAppList) {
					Integer appLimit = tThirdSellerApps.getAppLimit();
					Integer appToday = tThirdSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tThirdApp = tThirdSellerApps.getThirdApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1010");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1012");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String wx_appid = tThirdApp.getThirdAppId();
			String wx_mchid = tThirdApp.getThirdAppMch();
			String wx_apikey = tThirdApp.getThirdAppSecret();
			Integer appId = tThirdApp.getId();
			Integer merchantId = tThirdApp.getMerchantId();
			Integer sellerId = tThirdSeller.getId();
			String callBackUrl = tThirdApp.getCallbackUrl();
			JSONObject json = generateOrder(wx_mchid, wx_appid, wx_apikey, outTradeNo, appNameDecode, appId, merchantId, sellerId, fee, imsi, callBackUrl);
			
			returnJson.put("code", "1");
			returnJson.put("msg", json);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	public JSONObject generateOrder(String wx_mchid, String wx_appid, String wx_apikey, String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, Integer price, String imsi, String callBackUrl) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			//请求微信接口得到prepay_id
			String url = String
					.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			String entity = genProductArgs(wx_mchid, wx_appid, wx_apikey, orderNo, appName, String.valueOf(price), callBackUrl);
			String res = HttpClientUtils.postJson(url, entity);
			String wxprepay_id = StringUtils.substringBetween(res, "<prepay_id><![CDATA[", "]]></prepay_id>");
			if (!Utils.isEmpty(wxprepay_id)) {
				//创建订单
				TThirdOrder thirdOrder = new TThirdOrder();
				thirdOrder.setImsi(imsi);
				thirdOrder.setOrderId(orderNo);
				thirdOrder.setOutTradeNo(outTradeNo);
				thirdOrder.setAppId(appId);
				thirdOrder.setMerchantId(merchantId);
				thirdOrder.setSellerId(sellerId);
				thirdOrder.setSubject(appName);
				thirdOrder.setFee(price);
				thirdOrder.setType(2);
				thirdpartOrderManager.save(thirdOrder);
				
				String nonceStr = genNonceStr();
				String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
				List<NameValuePair> signParams = new LinkedList<NameValuePair>();
				signParams.add(new BasicNameValuePair("appid", wx_appid));
				signParams.add(new BasicNameValuePair("noncestr", nonceStr));
				signParams.add(new BasicNameValuePair("package", "Sign=WXPay"));
				signParams.add(new BasicNameValuePair("partnerid", wx_mchid));
				signParams.add(new BasicNameValuePair("prepayid", wxprepay_id));
				signParams.add(new BasicNameValuePair("timestamp", timeStamp));
				String sign = genSign(signParams, wx_apikey);
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("wxprepay_id", wxprepay_id);
				returnJsonMsgObj.put("wx_appid", wx_appid);
				returnJsonMsgObj.put("wx_mchid", wx_mchid);
				returnJsonMsgObj.put("nonceStr", nonceStr);
				returnJsonMsgObj.put("timeStamp", timeStamp);
				returnJsonMsgObj.put("packageValue", "Sign=WXPay");
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("sign", sign);
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	private String genProductArgs(String wx_mchid, String wx_appid, String wx_apikey, String tradeId, String subject, String fee, String callBackUrl) {
		try {
			String nonceStr = genNonceStr();

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("appid", wx_appid));
			packageParams.add(new BasicNameValuePair("body", subject));
			packageParams.add(new BasicNameValuePair("mch_id", wx_mchid));
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
			packageParams.add(new BasicNameValuePair("notify_url", callBackUrl));
			packageParams.add(new BasicNameValuePair("out_trade_no", tradeId));
			packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
			packageParams.add(new BasicNameValuePair("total_fee", fee));
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));

			String sign = genSign(packageParams, wx_apikey);
			packageParams.add(new BasicNameValuePair("sign", sign));

			String xmlstring = toXml(packageParams);
			
			return new String(xmlstring.toString().getBytes("UTF-8"), "ISO8859-1");
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return null;
		}
	}
	
	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMD5(String.valueOf(random.nextInt(10000)));
	}
	
	/**
	 * 生成签名
	 */
	private String genSign(List<NameValuePair> params, String wx_apikey) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(wx_apikey);

		String packageSign = MD5.getMD5(sb.toString()).toUpperCase();
		return packageSign;
	}
	
	private String toXml(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<" + params.get(i).getName() + ">");

			sb.append(params.get(i).getValue());
			sb.append("</" + params.get(i).getName() + ">");
		}
		sb.append("</xml>");

		return sb.toString();
	}
	
	/**
	 * 沃阅读-回调地址
	 */
	public void callBack() {
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			LogUtil.log("weixincallback 接收到的xml："+sb.toString());
			
			// 将资料解码
			String reqBody = sb.toString();
			String orderId = StringUtils.substringBetween(reqBody, "<out_trade_no><![CDATA[", "]]></out_trade_no>");
			String payUser = StringUtils.substringBetween(reqBody, "<openid><![CDATA[", "]]></openid>");
			String resultCode = StringUtils.substringBetween(reqBody, "<result_code><![CDATA[", "]]></result_code>");//业务结果
			
			if (!Utils.isEmpty(orderId)) {
				TThirdOrder tThirdOrder = thirdpartOrderManager.getOrderByProperty("orderId", orderId);
				if (!Utils.isEmpty(tThirdOrder)) {
					String orderStatus = "4";
					if ("SUCCESS".equals(resultCode)) {//成功
						orderStatus = "3";
					} else {
						orderStatus = "4";
					}
					tThirdOrder.setStatus(orderStatus);
					tThirdOrder.setPayTime(new Date());
					tThirdOrder.setPayUser(payUser);
					thirdpartOrderManager.save(tThirdOrder);
					
					//增加今日量
					if ("3".equals(orderStatus)) {
						thirdpartSellerManager.saveThirdSellerApps(tThirdOrder.getSellerId(), tThirdOrder.getAppId(), tThirdOrder.getFee());
					}
					
					//回调渠道
					TThirdSeller tThirdSeller = thirdpartSellerManager.get(tThirdOrder.getSellerId());
					String callbackUrl = tThirdSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tThirdOrder.getOutTradeNo();
						new Thread(new SendPartner(orderStatus,orderId,outTradeNo,tThirdOrder.getFee()+"",callbackUrl)).start();
					}
				}
			}
			String xmlString = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/xml; charset=utf-8");
			
			PrintWriter out = response.getWriter();
			out.print(xmlString);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String status,String orderNo,String outTradeNo,String fee,String callbackUrl) {
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.outTradeNo = outTradeNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
				HttpClientUtils.postJson(callbackUrl, jsonObject.toJSONString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
}
