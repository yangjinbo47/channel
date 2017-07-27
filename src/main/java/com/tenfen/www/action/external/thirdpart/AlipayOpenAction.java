package com.tenfen.www.action.external.thirdpart;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.tenfen.util.encrypt.RsaEncypt;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.thirdpart.ThirdpartOrderManager;
import com.tenfen.www.service.operation.thirdpart.ThirdpartSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class AlipayOpenAction extends SimpleActionSupport{

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
			LogUtil.log("alipay 参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
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
			String ali_partner = tThirdApp.getThirdAppId();//appid
			String ali_seller = tThirdApp.getThirdAppMch();//seller
			String ali_rsa_private = tThirdApp.getThirdAppSecret();//private_key
			Integer appId = tThirdApp.getId();
			Integer merchantId = tThirdApp.getMerchantId();
			Integer sellerId = tThirdSeller.getId();
			String callBackUrl = tThirdApp.getCallbackUrl();
			JSONObject json = generateOrder(ali_seller, ali_partner, ali_rsa_private, outTradeNo, appNameDecode, appId, merchantId, sellerId, fee, imsi, callBackUrl);
			
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
	
	/**
	 * 生成订单
	 * @param outTradeNo
	 * @param appName - 应用名
	 * @param price - 价格 单位分
	 * @return
	 */
	public JSONObject generateOrder(String ali_seller, String ali_partner, String ali_rsa_private, String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, Integer fee, String imsi, String callBackUrl) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			float f = (float)fee/100;
			DecimalFormat df = new DecimalFormat("0.00");//格式化小数，不足的补0
			String price = df.format(f);
			// 订单
			String orderInfo = getOrderInfo(ali_partner, ali_seller, orderNo, appName, appName, price, callBackUrl);
			
			// 对订单做RSA 签名
			String sign = sign(orderInfo, ali_rsa_private);
//			String sign = AlipaySignature.rsaSign(orderInfo, ali_rsa_private, "UTF-8");
			sign = URLEncoder.encode(sign, "UTF-8");
			String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
			
			//创建alipay订单信息
			//创建订单
			TThirdOrder thirdOrder = new TThirdOrder();
			thirdOrder.setImsi(imsi);
			thirdOrder.setOrderId(orderNo);
			thirdOrder.setOutTradeNo(outTradeNo);
			thirdOrder.setAppId(appId);
			thirdOrder.setMerchantId(merchantId);
			thirdOrder.setSellerId(sellerId);
			thirdOrder.setSubject(appName);
			thirdOrder.setFee(fee);
			thirdOrder.setType(1);
			thirdpartOrderManager.save(thirdOrder);
			
			returnJsonMsgObj = new JSONObject();
			returnJsonMsgObj.put("order_id", orderNo);
			returnJsonMsgObj.put("out_trade_no", outTradeNo);
			returnJsonMsgObj.put("payInfo", payInfo);
			returnJsonMsgObj.put("fee", fee);
			returnJsonMsgObj.put("sign", sign);
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	/**
	 * create the order info. 创建订单信息
	 * @param outTradeNo
	 * @param subject
	 * @param body
	 * @param price - 价格 单位元
	 * @return
	 */
	public String getOrderInfo(String ali_partner, String ali_seller, String outTradeNo, String subject, String body, String price, String callBackUrl) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + ali_partner + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + ali_seller + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + outTradeNo + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + callBackUrl + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
//		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}
	
	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param content
	 *            待签名订单信息
	 */
	public String sign(String content, String ali_rsa_private) {
		return RsaEncypt.sign(content, ali_rsa_private);
	}
	
	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}
	
	/**
	 * 支付宝支付-回调地址
	 */
	public void callBack() {
		try {
			String orderId = ServletRequestUtils.getStringParameter(request, "out_trade_no");
			String tradeStatus = ServletRequestUtils.getStringParameter(request, "trade_status");
			String buyerEmail = ServletRequestUtils.getStringParameter(request, "buyer_email");
//			String sign = ServletRequestUtils.getStringParameter(request, "sign");
//			LogUtil.log("alipay 得到的sign:"+sign);
//			String ali_rsa_private = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANZMR6jGv605bM20yLPVrbCRwveh13ilPSiRNAd3ZTEGWfQ/anbdQWi4G3BYTwgyQPfy+KOZwEN1Zpss/HzxZF1JVOn8HENJfyMV55NkoPhaO/hPHL750LfOOOlU03I2DOe2qffWfWcdMApk1d704iSLi39VW1gILtnIWxz1wP3RAgMBAAECgYAYBUDbCDwf/JnXjPYaQk9PxWbnPvraCRuL2orMc3OiRSX8HMegPzi/tjNNKnjhYFCr+k4oeGl2pkg0CJzcnGo1XgHOwfus+vLj2nLNyDht500u2paD4LWpgF3P4SBKODoW71YtGr4cqABS+/ZedouLqUaA03dUCadiX0TgoENjiQJBAP05SWiOXKBFfZkr1PxHEfRgYcmRgt7kWds+eWS1jhHjvgduranVgCjLtKU2e0BSFXHy4WJQFJJPAsP1rkkmAncCQQDYpb3bU/LWHXXoHWHiCUFc5gmH74OAsrg077H6kMakHXuZcnmbSyfFCHBc8/zehT+mls0iyBeWGEtR7SM4sov3AkBJEaHEIOhLeyHd9A72aQ+eQ72T64AnDx96q1FeJSGEugHYpTBJhIkBvJ442jrAea8kfWBZ/R+ihFgY8ajEBMrLAkEAl9ax1cJkc8R6GpBdRfqOoPlovKkVVWHs0M0dxCsrzWIMemNM75Yg7WsYtU0bcSmajrsqUrJCNaQZRfYZtWcNyQJAG46JAFmnrZ8QLRmGl92KJ/t/DPyuog/i9GQ8NeufeZJXsK7M32irMKWNZ1xkxG6/Hc76Kgv1tMNgQKsUMlNX4A==";
//			String ali_rsa_public = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDWTEeoxr+tOWzNtMiz1a2wkcL3odd4pT0okTQHd2UxBln0P2p23UFouBtwWE8IMkD38vijmcBDdWabLPx88WRdSVTp/BxDSX8jFeeTZKD4Wjv4Txy++dC3zjjpVNNyNgzntqn31n1nHTAKZNXe9OIki4t/VVtYCC7ZyFsc9cD90QIDAQAB";

//			Map<String, String> params = ServletRequestUtils.getRequestParams(request);
//			boolean b = AlipaySignature.rsaCheckV1(params, ali_rsa_public, "UTF-8");
//			LogUtil.log("alipay 校验结果:"+b);
			
			if (!Utils.isEmpty(orderId)) {
				TThirdOrder tThirdOrder = thirdpartOrderManager.getOrderByProperty("orderId", orderId);
				if (!Utils.isEmpty(tThirdOrder)) {
					String orderStatus = "4";
					if ("WAIT_BUYER_PAY".equals(tradeStatus)) {//等待支付
						orderStatus = "2";
					} else if ("TRADE_FINISHED".equals(tradeStatus) || "TRADE_SUCCESS".equals(tradeStatus)) {//成功
						orderStatus = "3";
					} else {
						orderStatus = "4";
					}
					tThirdOrder.setStatus(orderStatus);
					tThirdOrder.setPayTime(new Date());
					tThirdOrder.setPayUser(buyerEmail);
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
			PrintWriter out = response.getWriter();
			out.print("success");
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
