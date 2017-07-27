package com.tenfen.www.action.external.sms;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SmsIMusicAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	/**
	 * 生成订单
	 * 计费点事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		Integer fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("iMusic参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			
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
			
			TSmsSeller tSmsSeller = smsSellerManager.getSmsSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tSmsSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
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
			String geneSign = TokenService.buildToken(queryParamList, tSmsSeller.getSellerSecret());
			if (!sign.equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
			TSmsApp tSmsApp = null;
			if (smsSellerAppList.size() > 0) {
				for (TSmsSellerApps tSmsSellerApps : smsSellerAppList) {
					Integer appLimit = tSmsSellerApps.getAppLimit();
					Integer appToday = tSmsSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tSmsApp = tSmsSellerApps.getSmsApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			Integer appId = tSmsApp.getId();
			Integer merchantId = tSmsApp.getMerchantId();
			Integer sellerId = tSmsSeller.getId();
			
			int chargeFee = 0;
			JSONArray returnJsonArray = new JSONArray();
			//通过app查询关联的计费信息
			List<TSmsProductInfo> proList = tSmsApp.getProductList();
			Collections.sort(proList);
			while (fee >= 100) {
				for (int i = 0; i < proList.size(); i++) {
					chargeFee = proList.get(i).getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						JSONObject json = generateIMusicOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, proList.get(i).getSendNumber(), proList.get(i).getInstruction(), chargeFee, imsi, Constants.T_SMS_ORDER_PRODUCT_TYPE.DIANBO.getValue());
						returnJsonArray.add(json);
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", returnJsonArray);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 生成包月订单
	 * 计费点事先录入产品列表
	 */
	public void generatePackageOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		Integer fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("iMusic参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			
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
			
			TSmsSeller tSmsSeller = smsSellerManager.getSmsSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tSmsSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
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
			String geneSign = TokenService.buildToken(queryParamList, tSmsSeller.getSellerSecret());
			if (!sign.equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
			TSmsApp tSmsApp = null;
			if (smsSellerAppList.size() > 0) {
				for (TSmsSellerApps tSmsSellerApps : smsSellerAppList) {
					Integer appLimit = tSmsSellerApps.getAppLimit();
					Integer appToday = tSmsSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tSmsApp = tSmsSellerApps.getSmsApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			Integer appId = tSmsApp.getId();
			Integer merchantId = tSmsApp.getMerchantId();
			Integer sellerId = tSmsSeller.getId();
			
			int chargeFee = 0;
			JSONArray returnJsonArray = new JSONArray();
			//通过app查询关联的计费信息
			List<TSmsProductInfo> proList = tSmsApp.getProductList();
			Collections.sort(proList);
			while (fee >= 100) {
				for (int i = 0; i < proList.size(); i++) {
					chargeFee = proList.get(i).getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						JSONObject json = generateIMusicOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, proList.get(i).getSendNumber(), proList.get(i).getInstruction(), chargeFee, imsi, Constants.T_SMS_ORDER_PRODUCT_TYPE.PACKAGE.getValue());
						returnJsonArray.add(json);
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", returnJsonArray);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	public JSONObject generateIMusicOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, String senderNumber, String instruction, Integer price, String imsi, Integer productType) {
		JSONObject returnJsonMsgObj = null;
		try {
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String msgContent = instruction+orderNo;
			//创建订单
			TSmsOrder tSmsOrder = new TSmsOrder();
			tSmsOrder.setImsi(imsi);
			tSmsOrder.setOrderId(orderNo);
			tSmsOrder.setOutTradeNo(outTradeNo);
			tSmsOrder.setAppId(appId);
			tSmsOrder.setMerchantId(merchantId);
			tSmsOrder.setSellerId(sellerId);
			tSmsOrder.setSubject(appName);
			tSmsOrder.setSenderNumber(senderNumber);
			tSmsOrder.setMsgContent(msgContent);
			tSmsOrder.setFee(price);
			tSmsOrder.setProductType(productType);
			smsOrderManager.save(tSmsOrder);
			
			returnJsonMsgObj = new JSONObject();
			returnJsonMsgObj.put("order_id", orderNo);
			returnJsonMsgObj.put("out_trade_no", outTradeNo);
			returnJsonMsgObj.put("fee", price);
			returnJsonMsgObj.put("sender_number", senderNumber);
			returnJsonMsgObj.put("message_content", msgContent);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	/**
	 * 爱音乐-回调地址
	 */
	public void callBack() {
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", null);
		String smsOrder = ServletRequestUtils.getStringParameter(request, "sms_order", null);
		Integer productType = ServletRequestUtils.getIntParameter(request, "product_type", -1);
		String state = ServletRequestUtils.getStringParameter(request, "state", null);
		Integer opType = ServletRequestUtils.getIntParameter(request, "op_type", -1);
		String time = ServletRequestUtils.getStringParameter(request, "time", null);
		
		String deviceid = request.getHeader("deviceid");
		String timestamp = request.getHeader("timestamp");
		String signature = request.getHeader("signature");
		String keyword = null;
		if ("5613".equals(deviceid)) {
			keyword = "CT%^!#5613";
		}
		
		JSONObject jsonObject = new JSONObject();
		try {
			time = URLDecoder.decode(time, "UTF-8");
			LogUtil.log("iMusic callBack param mobile:"+mobile+" sms_order:"+smsOrder+" product_type:"+productType+" state:"+state+" op_type:"+opType+" time:"+time+" deviceid:"+deviceid+" timestamp:"+timestamp+" signature:"+signature+" keyword:"+keyword);
			
			if (Utils.isEmpty(mobile) || Utils.isEmpty(smsOrder) || Utils.isEmpty(keyword) || Utils.isEmpty(signature)) {
				jsonObject.put("code", "0001");
				jsonObject.put("description", "必要参数为空");
				StringUtil.printJson(response, jsonObject.toString());
				return;
			}
			
			String geneSign = MD5.getMD5(keyword+timestamp);
			if (!signature.equals(geneSign)) {
				jsonObject.put("code", "0002");
				jsonObject.put("description", "签名不正确");
				StringUtil.printJson(response, jsonObject.toString());
				return;
			}
			LogUtil.log("iMusic geneSign:"+geneSign);
			
			int index = smsOrder.indexOf(deviceid);
			String orderId = smsOrder.substring(index+4);
			
			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("orderId", orderId);
			if (Utils.isEmpty(tSmsOrder)) {
				jsonObject.put("code", "0003");
				jsonObject.put("description", "订单未找到");
				StringUtil.printJson(response, jsonObject.toString());
				return;
			}
			//status转换
			String statusChange = "9999";
			if ("0".equals(state) && opType == 0) {//成功-订购
				statusChange = "3";
			} else if ("1".equals(state) && opType == 0) {//失败-订购
				statusChange = "4";
			} else if ("0".equals(state) && opType == 1){//成功-退订
				statusChange = "5";
			}
			if ("1".equals(tSmsOrder.getStatus())) {
				tSmsOrder.setStatus(statusChange);
				tSmsOrder.setMoNumber(tSmsOrder.getSenderNumber());
				tSmsOrder.setMoMsg(smsOrder);
				tSmsOrder.setPayPhone(mobile);
				tSmsOrder.setPayTime(new Date());
				
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
				if (!Utils.isEmpty(mobileArea)) {
					tSmsOrder.setProvince(mobileArea.getProvince());
				}
				
				smsOrderManager.save(tSmsOrder);
				
				//通知渠道
				Integer sellerId = tSmsOrder.getSellerId();
				TSmsSeller tSmsSeller = smsSellerManager.get(sellerId);
				String callbackUrl = tSmsSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String outTradeNo = tSmsOrder.getOutTradeNo();
					String orderNo = tSmsOrder.getOrderId();
					new Thread(new SendPartner(statusChange,orderNo,outTradeNo,tSmsOrder.getFee()+"",callbackUrl)).start();
				}
				
				jsonObject.put("code", "0000");
				jsonObject.put("description", "成功");
			} else if ("3".equals(tSmsOrder.getStatus()) && opType == 1) {//包月退订
				tSmsOrder.setStatus(statusChange);
				tSmsOrder.setPayTime(new Date());
				smsOrderManager.save(tSmsOrder);
				
				//通知渠道
				Integer sellerId = tSmsOrder.getSellerId();
				TSmsSeller tSmsSeller = smsSellerManager.get(sellerId);
				String callbackUrl = tSmsSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String outTradeNo = tSmsOrder.getOutTradeNo();
					String orderNo = tSmsOrder.getOrderId();
					new Thread(new SendPartner(statusChange,orderNo,outTradeNo,tSmsOrder.getFee()+"",callbackUrl)).start();
				}
				
				jsonObject.put("code", "0000");
				jsonObject.put("description", "成功");
			} else {
				jsonObject.put("code", "0004");
				jsonObject.put("description", "订单状态已更新");
			}
		} catch (Exception e) {
			jsonObject.put("code", "9999");
			jsonObject.put("description", "未知异常");
			LogUtil.error(e.getMessage(), e);
		}
		StringUtil.printJson(response, jsonObject.toString());
		return;
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
				
				HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
//		        HttpPost httpPost = new HttpPost(callbackUrl);
//		        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
//		        
//		        StringEntity se = new StringEntity(jsonObject.toString());
//		        se.setContentType(CONTENT_TYPE_TEXT_JSON);
//		        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
//		        httpPost.setEntity(se);
//		        httpClient.execute(httpPost);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
