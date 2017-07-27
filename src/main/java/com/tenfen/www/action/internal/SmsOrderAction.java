package com.tenfen.www.action.internal;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

/**
 * 接受永擎浩之的mo mr回调
 * @author BOBO
 *
 */
public class SmsOrderAction extends SimpleActionSupport{

	private static final long serialVersionUID = 802481743436649610L;
	
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;

	public void mo() {
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", null);
		String spnum = ServletRequestUtils.getStringParameter(request, "spnum", null);//特服号
		String linkId = ServletRequestUtils.getStringParameter(request, "link_id", null);
		String productId = ServletRequestUtils.getStringParameter(request, "product_id", null);
		String msg = ServletRequestUtils.getStringParameter(request, "msg", null);
		
		LogUtil.log("smsmo param:mobile:"+mobile+" spnum:"+spnum+" linkId:"+linkId+" productId:"+productId+" msg:"+msg);
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		if (Utils.isEmpty(mobile)) {
			returnJson.put("code", "1001");
			returnJson.put("msg", "mobile参数不能为空");
			StringUtil.printJson(response, returnJson.toString());
			return;
		} else if (Utils.isEmpty(spnum)) {
			returnJson.put("code", "1002");
			returnJson.put("msg", "spnum参数不能为空");
			StringUtil.printJson(response, returnJson.toString());
			return;
		} else if (Utils.isEmpty(productId)) {
			returnJson.put("code", "1003");
			returnJson.put("msg", "product_id参数不能为空");
			StringUtil.printJson(response, returnJson.toString());
			return;
		} else if (Utils.isEmpty(msg)) {
			returnJson.put("code", "1004");
			returnJson.put("msg", "msg参数不能为空");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
		
		try {
			String[] msgArray = msg.split("#");
			//查询该产品类型，点播或者包月
			TSmsProductInfo tSmsProductInfo = smsProductInfoManager.getSmsProductInfoByProperty("productId", productId);
			Integer fee = tSmsProductInfo.getPrice() / 100;
			if (Utils.isEmpty(tSmsProductInfo)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "该产品未注册");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tSmsProductInfo.getType() == 2) {//包月
				if (msgArray[1].startsWith("seller")) {//MTK直接入库
					String sellerStr = msgArray[1];
					LogUtil.log("smsmopackage sellerStr:"+sellerStr);
//					String[] sellerArray = sellerStr.split("_");
					
//					Integer sellerId = Integer.parseInt(sellerArray[1]);
					Integer sellerId = Integer.parseInt(sellerStr.substring(6));
					TSmsSeller tSmsSeller = smsSellerManager.getEntity(sellerId);
					List<TSmsSellerApps> tSmsSellerAppsList = tSmsSeller.getSellerApps();
					TSmsApp tSmsApp = null;
					for (TSmsSellerApps tsmsSellerApps : tSmsSellerAppsList) {
						TSmsApp tSmsAppTmp = tsmsSellerApps.getSmsApp();
						List<TSmsProductInfo> proList = tSmsAppTmp.getProductList();
						for (TSmsProductInfo productInfo : proList) {
							if (productId.equals(productInfo.getProductId())) {
								tSmsApp = tSmsAppTmp;
								break;
							}
						}
					}
					if (Utils.isEmpty(tSmsApp)) {
						returnJson.put("code", "1008");
						returnJson.put("msg", "未找到相应的APP");
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
					Integer appId = tSmsApp.getId();
					Integer merchantId = tSmsApp.getMerchantId();
					String subject = tSmsApp.getName();
					
					//订单号
					String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
					
					TSmsOrder tSmsOrder = new TSmsOrder();
					tSmsOrder.setOrderId(orderSeq);
					tSmsOrder.setOutTradeNo(null);
					tSmsOrder.setImsi(null);
					tSmsOrder.setAppId(appId);
					tSmsOrder.setMerchantId(merchantId);
					tSmsOrder.setSellerId(sellerId);
					tSmsOrder.setSubject(subject);
					tSmsOrder.setSenderNumber(tSmsProductInfo.getSendNumber());
					tSmsOrder.setMsgContent(tSmsProductInfo.getInstruction());
					tSmsOrder.setMoNumber(spnum);
					tSmsOrder.setMoMsg(msg);
					tSmsOrder.setFee(tSmsProductInfo.getPrice());
					tSmsOrder.setProductType(Constants.T_SMS_ORDER_PRODUCT_TYPE.PACKAGE.getValue());
					tSmsOrder.setPayPhone(mobile);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
					if (!Utils.isEmpty(mobileArea)) {
						tSmsOrder.setProvince(mobileArea.getProvince());
					}
					tSmsOrder.setPayTime(new Date());
					tSmsOrder.setStatus("3");
					smsOrderManager.save(tSmsOrder);
					
					//增加今日量
					smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
					
					String tips = tSmsApp.getTips();
					String sendMsg = tips.replace("${fee}", fee+"");
					
					//调用回调渠道方法
					String callbackUrl = tSmsSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						//mtk 直接返回服务端orderid 作为订单id
						new Thread(new SendPartner(mobile, "3", null, tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", callbackUrl)).start();
					}
					
					returnJson.put("code", "1");
					returnJson.put("msg", sendMsg);
					LogUtil.log("smsmo returnJson:"+returnJson.toString());
					StringUtil.printJson(response, returnJson.toString());
					return;
				} else {
					//查找该号码创建的订单
					TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("orderId", msgArray[1]);
					if (!Utils.isEmpty(tSmsOrder)) {
						tSmsOrder.setMoNumber(spnum);
						tSmsOrder.setMoMsg(msg);
						tSmsOrder.setStatus("3");//包月接到请求就成功
						tSmsOrder.setPayPhone(mobile);
						tSmsOrder.setPayTime(new Date());
						TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
						if (!Utils.isEmpty(mobileArea)) {
							tSmsOrder.setProvince(mobileArea.getProvince());
						}
						smsOrderManager.save(tSmsOrder);
						
						//增加今日量
						smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
						
						Integer appId = tSmsOrder.getAppId();
						TSmsApp tSmsApp = smsAppManager.get(appId);
						String tips = tSmsApp.getTips();
						String sendMsg = tips.replace("${fee}", fee+"");
						
						//调用回调渠道方法
						TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
						String callbackUrl = tSmsSeller.getCallbackUrl();
						if (!Utils.isEmpty(callbackUrl)) {
							new Thread(new SendPartner(mobile, "3", tSmsOrder.getOrderId(), tSmsOrder.getOutTradeNo(), tSmsOrder.getFee()+"", callbackUrl)).start();
						}
						
						returnJson.put("code", "1");
						returnJson.put("msg", sendMsg);
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
						StringUtil.printJson(response, returnJson.toString());
						return;
					} else {
						returnJson.put("code", "1006");
						returnJson.put("msg", "用户订单未找到");
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
				}
			} else {//点播
				if (msgArray[1].startsWith("seller")) {//MTK直接入库 指令#seller14
					String sellerStr = msgArray[1];					
					LogUtil.log("smsmodianbo sellerStr:"+sellerStr);
					Integer sellerId = Integer.parseInt(sellerStr.substring(6));

					TSmsSeller tSmsSeller = smsSellerManager.getEntity(sellerId);
					List<TSmsSellerApps> tSmsSellerAppsList = tSmsSeller.getSellerApps();
					TSmsApp tSmsApp = null;
					for (TSmsSellerApps tsmsSellerApps : tSmsSellerAppsList) {
						TSmsApp tSmsAppTmp = tsmsSellerApps.getSmsApp();
						List<TSmsProductInfo> proList = tSmsAppTmp.getProductList();
						for (TSmsProductInfo productInfo : proList) {
							if (productId.equals(productInfo.getProductId())) {
								tSmsApp = tSmsAppTmp;
								break;
							}
						}
					}
					if (Utils.isEmpty(tSmsApp)) {
						returnJson.put("code", "1008");
						returnJson.put("msg", "未找到相应的APP");
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
					Integer appId = tSmsApp.getId();
					Integer merchantId = tSmsApp.getMerchantId();
					String subject = tSmsApp.getName();
					
					String province = null;
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
					if (mobileArea != null) {
						province = mobileArea.getProvince();
					}
					boolean flag = false;//false-排除 true-不排除
					if (province != null) {
						// 是否在屏蔽区域内
//						if (tSmsApp.getExcludeArea() == null || tSmsApp.getExcludeArea().length() == 0) {// 没有排除对象
//							flag = true;
//						} else {
//							if (!"all".equals(tSmsApp.getExcludeArea())) {// 是否屏蔽全国
//								if (tSmsApp.getExcludeArea().indexOf(province) == -1) {
//									flag = true;
//								}
//							}
//						}
						//判断省是否到量
						TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
						Integer packagedaylimit_conf = tSmsAppLimit.getDayLimit();
						if (packagedaylimit_conf == -1) {//不屏蔽
							flag = true;
						}
					} else {//未取到号码所在地
						flag = true;
					}
					
					//订单号
					String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
					
					TSmsOrder tSmsOrder = new TSmsOrder();
					tSmsOrder.setOrderId(orderSeq);
					tSmsOrder.setOutTradeNo(null);
					tSmsOrder.setLinkId(linkId);
					tSmsOrder.setImsi(null);
					tSmsOrder.setAppId(appId);
					tSmsOrder.setMerchantId(merchantId);
					tSmsOrder.setSellerId(sellerId);
					tSmsOrder.setSubject(subject);
					tSmsOrder.setSenderNumber(tSmsProductInfo.getSendNumber());
					tSmsOrder.setMsgContent(tSmsProductInfo.getInstruction());
					tSmsOrder.setMoNumber(spnum);
					tSmsOrder.setMoMsg(msg);
					tSmsOrder.setFee(tSmsProductInfo.getPrice());
					tSmsOrder.setProductType(Constants.T_SMS_ORDER_PRODUCT_TYPE.PACKAGE.getValue());
					if (!flag) {
						tSmsOrder.setStatus("1001");//1001表示在屏蔽省份之内
					} else {
						tSmsOrder.setStatus("1");
					}
					tSmsOrder.setPayPhone(mobile);
					tSmsOrder.setPayTime(new Date());
					tSmsOrder.setProvince(mobileArea.getProvince());
					
					smsOrderManager.save(tSmsOrder);
					
					if (!flag) {
						returnJson.put("code", "1007");
						returnJson.put("msg", "该用户所在省被屏蔽");
						StringUtil.printJson(response, returnJson.toString());
						return;
					} else {
						String tips = tSmsApp.getTips();
						String sendMsg = tips.replace("${fee}", fee+"");
						
						returnJson.put("code", "1");
						returnJson.put("msg", sendMsg);
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
					
				} else {
					//查找该号码创建的订单
					TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("orderId", msgArray[1]);
					if (!Utils.isEmpty(tSmsOrder)) {
						//检查是否在屏蔽区域
						Integer appId = tSmsOrder.getAppId();
						TSmsApp tSmsApp = smsAppManager.get(appId);
						String province = null;
						TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
						if (mobileArea != null) {
							province = mobileArea.getProvince();
						}
						boolean flag = false;//false-排除 true-不排除
						if (province != null) {
							// 是否在屏蔽区域内
//							if (tSmsApp.getExcludeArea() == null || tSmsApp.getExcludeArea().length() == 0) {// 没有排除对象
//								flag = true;
//							} else {
//								if (!"all".equals(tSmsApp.getExcludeArea())) {// 是否屏蔽全国
//									if (tSmsApp.getExcludeArea().indexOf(province) == -1) {
//										flag = true;
//									}
//								}
//							}
							//判断省是否到量
							TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
							Integer packagedaylimit_conf = tSmsAppLimit.getDayLimit();
							if (packagedaylimit_conf == -1) {//不屏蔽
								flag = true;
							}
						} else {//未取到号码所在地
							flag = true;
						}
						
						tSmsOrder.setLinkId(linkId);
						tSmsOrder.setMoNumber(spnum);
						tSmsOrder.setMoMsg(msg);
						if (!flag) {
							tSmsOrder.setStatus("1001");//1001表示在屏蔽省份之内
						}
						if (!Utils.isEmpty(province)) {
							tSmsOrder.setProvince(province);
						}
						
						smsOrderManager.save(tSmsOrder);
						
						if (!flag) {
							returnJson.put("code", "1007");
							returnJson.put("msg", "该用户所在省被屏蔽");
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
						String tips = tSmsApp.getTips();
						String sendMsg = tips.replace("${fee}", fee+"");
						
						returnJson.put("code", "1");
						returnJson.put("msg", sendMsg);
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
						StringUtil.printJson(response, returnJson.toString());
					} else {
						returnJson.put("code", "1006");
						returnJson.put("msg", "用户订单未找到");
						StringUtil.printJson(response, returnJson.toString());
						return;
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	public void mr() {
		String linkId = ServletRequestUtils.getStringParameter(request, "link_id", null);
		String status = ServletRequestUtils.getStringParameter(request, "status", null);
		String mobile = ServletRequestUtils.getStringParameter(request, "mobile", null);
		
		TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("linkId", linkId);
		if (!Utils.isEmpty(tSmsOrder)) {
			if ("1".equals(tSmsOrder.getStatus())) {
				tSmsOrder.setStatus(status);
				tSmsOrder.setPayPhone(mobile);
				tSmsOrder.setPayTime(new Date());
				
//				TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
//				if (!Utils.isEmpty(mobileArea)) {
//					tSmsOrder.setProvince(mobileArea.getProvince());
//				}
				
				smsOrderManager.save(tSmsOrder);
				
				//增加今日量
				if ("3".equals(status)) {
					smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
				}
				
				//调用回调渠道方法
				TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
				String callbackUrl = tSmsSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					new Thread(new SendPartner(mobile, status, tSmsOrder.getOrderId(), tSmsOrder.getOutTradeNo(), tSmsOrder.getFee()+"", callbackUrl)).start();
				}
			}
			
		}
	}
	
	public void unSubscribe() {
		
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String outTradeNo;
		private String callbackUrl;
		private String phone;
		
		public SendPartner(String phone, String status, String orderNo, String outTradeNo, String fee, String callbackUrl) {
			this.phone = phone;
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
				jsonObject.put("phone", phone);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
//		        HttpPost httpPost = new HttpPost(callbackUrl);
//		        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
		        
		        LogUtil.log("smsOrder call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
//		        StringEntity se = new StringEntity(jsonObject.toString());
//		        se.setContentType(CONTENT_TYPE_TEXT_JSON);
//		        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
//		        httpPost.setEntity(se);
//		        httpClient.execute(httpPost);
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
}
