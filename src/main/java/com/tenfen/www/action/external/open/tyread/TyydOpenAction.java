package com.tenfen.www.action.external.open.tyread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;
import com.tenfen.www.util.tyyd.PBECoder;
import com.tenfen.www.util.tyyd.SMSOrderIDGenerator;

public class TyydOpenAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private BlackListManager blackListManager;
	
	public void lxGetProductInfo() {
		String token = ServletRequestUtils.getStringParameter(request, "token", null);
		String reqData = ServletRequestUtils.getStringParameter(request, "req_data", null);
		
		JSONObject reqDataJson = JSONObject.parseObject(reqData);
		String clientAppKey = reqDataJson.getString("client_app_key");
		String phoneNum = reqDataJson.getString("user_mobile");
		String price = reqDataJson.getString("price");
		String orderNo = reqDataJson.getString("order_no");
		String smsExtInfo = reqDataJson.getString("sms_ext_info");//格式：7wCXmu7FZO3zAPN,1|天天爱酷跑
		
		LogUtil.log("tyyd lxGetProductInfo params: token:"+token+" reqData:"+reqData);
		try {
			String code = "";
			String msg = "";
			
			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", clientAppKey);
			String subject = null;
			if (!Utils.isEmpty(tOpenApp)) {
				String secret = tOpenApp.getAppSecret();
				String appName = tOpenApp.getName();
				Integer appId = tOpenApp.getId();
				Integer merchantId = tOpenApp.getMerchantId();
				List<TokenParam> paramList = new ArrayList<TokenParam>();
				paramList.add(new TokenParam("req_data",reqData));
				String geneToken = TokenService.buildToken(paramList, secret);
				if (geneToken.equals(token)) {
					//查找订单
					TOpenOrder tOpenOrder = null;
					if (!Utils.isEmpty(smsExtInfo)) {						
						tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", smsExtInfo);
					}
					if (!Utils.isEmpty(tOpenOrder)) {//事先创建过订单，走正常流程
						subject = tOpenOrder.getSubject();
						code = "0";
						msg = "成功";
					} else {//事先未创建订单，走mtk流程
//						code = "1001";
//						msg = "订单未找到";
						TOpenOrder order = new TOpenOrder();
						order.setImsi(phoneNum);
						order.setOrderId(orderNo);
						order.setOutTradeNo(System.currentTimeMillis()+"");
						List<TOpenSeller> tOpenSellerList = openSellerManager.findSellerByAppId(appId);
						TOpenSeller tOpenSeller = tOpenSellerList.get(0);
						Integer sellerId = tOpenSeller.getId();
						order.setSellerId(sellerId);
						order.setAppId(appId);
						order.setMerchantId(merchantId);
						subject = appName;
						order.setSubject(appName);
						
						TOpenProductInfo pro = null;
						int fee = Integer.parseInt(price);
						List<TOpenProductInfo> proList = tOpenApp.getProductList();
						for (TOpenProductInfo tOpenProductInfo : proList) {
							if (tOpenProductInfo.getPrice() == fee) {
								pro = tOpenProductInfo;
							}
						}
						String senderNumber = pro.getCode();
						String instruction = pro.getInstruction();
						order.setSenderNumber(senderNumber);
						order.setMsgContent(instruction);
						order.setFee(fee);
						openOrderManager.save(order);
						
						code = "0";
						msg = "成功";
					}
					
				} else {
					code = "1002";
					msg = "签名不正确";
				}
			} else {
				code = "1003";
				msg = "App不存在";
			}
			
			JSONObject json = new JSONObject();
			JSONObject resp = new JSONObject();
			resp.put("code", code);
			resp.put("msg", msg);
			resp.put("product_name", subject+"+"+price+"分道具");
			resp.put("ip", "127.0.0.1");
			resp.put("imei", phoneNum);
			resp.put("user_account", phoneNum);
			resp.put("order_no", orderNo);
			resp.put("client_app_key", clientAppKey);
			json.put("response", resp);
			
			StringUtil.printJson(response, json.toString());
		} catch (Exception e) {
			LogUtil.log(e.getMessage(), e);
		}
	}
	/**
	 * 第三方鉴权
	 */
	public void auth() {
		String orderNo = ServletRequestUtils.getStringParameter(request, "order_no", null);
//		String smsExtInfo = ServletRequestUtils.getStringParameter(request, "sms_ext_info", null);
		String userMobile = ServletRequestUtils.getStringParameter(request, "user_mobile", null);
		
		JSONObject returnJson = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		Integer code = null;
		String msg = null;
		
		try {
			//查询是否在屏蔽省份
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderNo);
			Integer appId = tOpenOrder.getAppId();
//			TOpenApp tOpenApp = openAppManager.get(appId);
			
			String province = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(userMobile);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
			}
			boolean flag = false;//false-排除 true-不排除
			if (province != null) {
//				// 是否在屏蔽区域内
//				if (tOpenApp.getExcludeArea() == null
//						|| tOpenApp.getExcludeArea().length() == 0) {// 没有排除对象
//					flag = true;
//				} else {
//					if (!"all".equals(tOpenApp.getExcludeArea())) {// 是否屏蔽全国
//						if (tOpenApp.getExcludeArea().indexOf(
//								province) == -1) {
//							flag = true;
//						}
//					}
//				}
				//判断省是否到量
				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
				Integer packagedaylimit_conf = tOpenAppLimit.getDayLimit();
				if (packagedaylimit_conf == -1) {//不屏蔽
					flag = true;
				}
			} else {//未取到号码所在地
				flag = true;
			}
			
			if (!flag) {
				code = 1001;
				msg = "号码在排除地市内";
			}
			//查询是否在黑名单
			if (Utils.isEmpty(code)) {
				boolean success = blackListManager.isBlackList(userMobile);
				if (success) {
					code = 1002;
					msg = "改号码属于黑名单用户";
				}
			}
			
			if (Utils.isEmpty(code)) {
				code = 0;
				msg = "验证通过";
			}
		} catch (Exception e) {
			code = 0;
			msg = "验证通过";
			LogUtil.error(e.getMessage(), e);
		}
		
		jsonObject.put("code", code);
		jsonObject.put("msg", msg);
		
		returnJson.put("response", jsonObject);
		StringUtil.printJson(response, returnJson.toString());
	}
	
	public void lxAuth() {
		String reqData = ServletRequestUtils.getStringParameter(request, "req_data", null);
		JSONObject reqDataJson = JSONObject.parseObject(reqData);
		String userMobile = reqDataJson.getString("user_mobile");
//		String orderNo = reqDataJson.getString("order_no");
		String orderNo = reqDataJson.getString("sms_ext_info");
		
		JSONObject returnJson = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		Integer code = null;
		String msg = null;
		
		try {
			//查询是否在屏蔽省份
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderNo);
			Integer appId = tOpenOrder.getAppId();
//			TOpenApp tOpenApp = openAppManager.get(appId);
			
			String province = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(userMobile);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
			}
			boolean flag = false;//false-排除 true-不排除
			if (province != null) {
//				// 是否在屏蔽区域内
//				if (tOpenApp.getExcludeArea() == null
//						|| tOpenApp.getExcludeArea().length() == 0) {// 没有排除对象
//					flag = true;
//				} else {
//					if (!"all".equals(tOpenApp.getExcludeArea())) {// 是否屏蔽全国
//						if (tOpenApp.getExcludeArea().indexOf(
//								province) == -1) {
//							flag = true;
//						}
//					}
//				}
				//判断省是否到量
				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
				Integer packagedaylimit_conf = tOpenAppLimit.getDayLimit();
				if (packagedaylimit_conf == -1) {//不屏蔽
					flag = true;
				}
			} else {//未取到号码所在地
				flag = true;
			}
			
			if (!flag) {
				code = 1001;
				msg = "号码在排除地市内";
			}
			//查询是否在黑名单
			if (Utils.isEmpty(code)) {
				boolean success = blackListManager.isBlackList(userMobile);
				if (success) {
					code = 1002;
					msg = "改号码属于黑名单用户";
				}
			}
			
			if (Utils.isEmpty(code)) {
				code = 0;
				msg = "验证通过";
			}
		} catch (Exception e) {
			code = 0;
			msg = "验证通过";
			LogUtil.error(e.getMessage(), e);
		}
		
		jsonObject.put("code", code);
		jsonObject.put("msg", msg);
		
		returnJson.put("response", jsonObject);
		StringUtil.printJson(response, returnJson.toString());
	}
	
	/**
	 * 天翼-回调地址
	 */
	public void callBack() {
		String orderNo = ServletRequestUtils.getStringParameter(request, "order_no", null);
		String orderStatus = ServletRequestUtils.getStringParameter(request, "order_status", "1");
		String clientAppKey = ServletRequestUtils.getStringParameter(request, "client_app_key", null);
		String gmtPayment = ServletRequestUtils.getStringParameter(request, "gmt_payment", null);
		String userMobile = ServletRequestUtils.getStringParameter(request, "user_mobile", null);
		String token = ServletRequestUtils.getStringParameter(request, "token", null);
		int reduce = 0;
		
		JSONObject returnJson = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", clientAppKey);
			String geneToken = null;
			if (!Utils.isEmpty(tOpenApp)) {
				String secret = tOpenApp.getAppSecret();
				
				List<TokenParam> paramList = new ArrayList<TokenParam>(4);
				paramList.add(new TokenParam("order_no",orderNo));
				paramList.add(new TokenParam("order_status",orderStatus));
				paramList.add(new TokenParam("client_app_key",clientAppKey));
				paramList.add(new TokenParam("gmt_payment",gmtPayment));
				paramList.add(new TokenParam("user_mobile",userMobile));
				
				geneToken = TokenService.buildToken(paramList, secret);
			}
			
			if (token.equals(geneToken)) {
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderNo);
				if (!Utils.isEmpty(tOpenOrder)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date payTime = sdf.parse(gmtPayment);
					tOpenOrder.setStatus(orderStatus);
					tOpenOrder.setPayTime(payTime);
					tOpenOrder.setPayPhone(userMobile);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(userMobile);
					String province = null;
					if (!Utils.isEmpty(mobileArea)) {
						province = mobileArea.getProvince();
						tOpenOrder.setProvince(province);
					}
					
					//是否扣量
					Integer appId = tOpenOrder.getAppId();
					TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
					double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
					double rate = new Random().nextDouble();
					if (rate < reduce_conf) {
						reduce = 1;
						tOpenOrder.setReduce(reduce);
					}
					openOrderManager.save(tOpenOrder);
					
					//增加今日量
					if ("3".equals(orderStatus)) {
						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
					}
					
					jsonObject.put("code", 0);
					jsonObject.put("msg", "回调成功");
					
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tOpenOrder.getOutTradeNo();
						if (reduce != 1) {//不扣量
							new Thread(new SendPartner(orderStatus,orderNo,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						} else {
							new Thread(new SendPartner("4",orderNo,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						}
					}
				}
			}
		} catch (Exception e) {
			jsonObject.put("code", 1);
			jsonObject.put("msg", "回调失败");
			LogUtil.error(e.getMessage(), e);
		}
		
		returnJson.put("response", jsonObject);
		StringUtil.printJson(response, returnJson.toString());
	}
	
	public void lxCallBack() {
		String token = ServletRequestUtils.getStringParameter(request, "token", null);
		String reqData = ServletRequestUtils.getStringParameter(request, "req_data", null);
		JSONObject reqDataJson = JSONObject.parseObject(reqData);
		String orderStatus = reqDataJson.getString("order_status");
//		String orderNo = reqDataJson.getString("order_no");
		String orderNo = reqDataJson.getString("sms_ext_info");
		String gmtPayment = reqDataJson.getString("gmt_payment");
		String clientAppKey = reqDataJson.getString("client_app_key");
		String userMobile = reqDataJson.getString("user_mobile");
		int reduce = 0;
		
		JSONObject returnJson = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", clientAppKey);
			String geneToken = null;
			if (!Utils.isEmpty(tOpenApp)) {
				String secret = tOpenApp.getAppSecret();
				
				List<TokenParam> paramList = new ArrayList<TokenParam>(4);
				paramList.add(new TokenParam("req_data",reqData));
				
				geneToken = TokenService.buildToken(paramList, secret);
			}
			
			if (token.equals(geneToken)) {
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderNo);
				if (!Utils.isEmpty(tOpenOrder)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date payTime = sdf.parse(gmtPayment);
					tOpenOrder.setStatus(orderStatus);
					tOpenOrder.setPayTime(payTime);
					tOpenOrder.setPayPhone(userMobile);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(userMobile);
					String province = null;
					if (!Utils.isEmpty(mobileArea)) {
						province = mobileArea.getProvince();
						tOpenOrder.setProvince(province);
					}
					
					//是否扣量
					Integer appId = tOpenOrder.getAppId();
					TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
					double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
					double rate = new Random().nextDouble();
					if (rate < reduce_conf) {
						reduce = 1;
						tOpenOrder.setReduce(reduce);
					}
					openOrderManager.save(tOpenOrder);
					
					//增加今日量
					if ("3".equals(orderStatus)) {
						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
					}
					
					jsonObject.put("code", 0);
					jsonObject.put("msg", "回调成功");
					
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tOpenOrder.getOutTradeNo();
						if (reduce != 1) {//不扣量							
							new Thread(new SendPartner(orderStatus,orderNo,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						} else {
							new Thread(new SendPartner("4",orderNo,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						}
					}
				}
			}
		} catch (Exception e) {
			jsonObject.put("code", 1);
			jsonObject.put("msg", "回调失败");
			LogUtil.error(e.getMessage(), e);
		}
		
		returnJson.put("response", jsonObject);
		StringUtil.printJson(response, returnJson.toString());
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
				
				LogUtil.log("sendChannelTyydMsg:"+jsonObject.toString());
				HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
	//提供外部破解
	public void getSmsContent() {
		String appKey = ServletRequestUtils.getStringParameter(request, "appKey", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		
		JSONObject returnJson = new JSONObject();
		try {
			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", appKey);
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			TOpenProductInfo product = null;
			for (TOpenProductInfo tOpenProductInfo : proList) {
				if (fee == tOpenProductInfo.getPrice()) {
					product = tOpenProductInfo;
					break;
				}
			}
			
			if (!Utils.isEmpty(product)) {
				String instruction = product.getInstruction();
				String parm =  SMSOrderIDGenerator.getOrderID(15)+","+orderId;
				String message = instruction+"#"+parm;
				
				int index = message.indexOf("#");
				String message1 = message.substring(0, index+1);
				String message2 = message.substring(index+1, message.length());
				byte[] b = PBECoder.encrypt(message2.getBytes("UTF-8"));
				message2 = PBECoder.encryptBASE64(b);
				message = message1+message2;
				
				returnJson.put("appKey", appKey);
				returnJson.put("sms", message);
				returnJson.put("sender_number", product.getCode());
			}
		} catch (Exception e) {
			returnJson.put("error", e.getMessage());
			LogUtil.error(e.getMessage(), e);
		}
		LogUtil.log("tyread 破解返回内容:"+returnJson.toString());
		StringUtil.printJson(response, returnJson.toString());
	}
}
