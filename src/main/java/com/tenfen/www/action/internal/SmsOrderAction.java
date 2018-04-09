package com.tenfen.www.action.internal;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
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
import com.tenfen.www.service.operation.BlackListManager;
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
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private BlackListManager blackListManager;

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
			//查询该产品类型，点播或者包月
			TSmsProductInfo tSmsProductInfo = smsProductInfoManager.getSmsProductInfoByProperty("productId", productId);
			Integer fee = tSmsProductInfo.getPrice() / 100;
			String proName = tSmsProductInfo.getName();
			if (Utils.isEmpty(tSmsProductInfo)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "该产品未注册");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			String[] msgArray = msg.split("#");
			if (msgArray.length == 2) {//包含#
				if (tSmsProductInfo.getType() == 2) {//包月
					if (msgArray[1].startsWith("seller")) {//MTK直接入库 格式：指令#seller渠道号
						String sellerStr = msgArray[1];
						LogUtil.log("smsmopackage sellerStr:"+sellerStr);
//						String[] sellerArray = sellerStr.split("_");
						
//						Integer sellerId = Integer.parseInt(sellerArray[1]);
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
						tSmsOrder.setImsi(mobile);
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
						tips = MessageFormat.format(tips, fee, proName);
						
						//调用回调渠道方法
						String callbackUrl = tSmsSeller.getCallbackUrl();
						if (!Utils.isEmpty(callbackUrl)) {
							//mtk 直接返回服务端orderid 作为订单id
							new Thread(new SendPartner(mobile, "3", null, tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", msg, callbackUrl)).start();
						}
						
						returnJson.put("code", "1");
						returnJson.put("msg", tips);
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
							tips = MessageFormat.format(tips, fee, proName);
							
							//调用回调渠道方法
							TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
							String callbackUrl = tSmsSeller.getCallbackUrl();
							if (!Utils.isEmpty(callbackUrl)) {
								new Thread(new SendPartner(mobile, "3", tSmsOrder.getOrderId(), tSmsOrder.getOutTradeNo(), tSmsOrder.getFee()+"", msg, callbackUrl)).start();
							}
							
							returnJson.put("code", "1");
							returnJson.put("msg", tips);
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
					if (msgArray[1].startsWith("seller")) {//MTK直接入库 格式：指令#seller渠道号
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
						tSmsOrder.setImsi(mobile);
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
							tips = MessageFormat.format(tips, fee, proName);
							
							returnJson.put("code", "1");
							returnJson.put("msg", tips);
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
								LogUtil.log("smsmo returnJson:"+returnJson.toString());
								StringUtil.printJson(response, returnJson.toString());
								return;
							}
							String tips = tSmsApp.getTips();
							tips = MessageFormat.format(tips, fee, proName);
							
							returnJson.put("code", "1");
							returnJson.put("msg", tips);
							LogUtil.log("smsmo returnJson:"+returnJson.toString());
							StringUtil.printJson(response, returnJson.toString());
						} else {//未找到订单
							returnJson.put("code", "1009");
							returnJson.put("msg", "订单不存在");
							LogUtil.log("smsmo returnJson:"+returnJson.toString());
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
					}
				}
			} else {//不包含#，供渠道直接使用业务指令，只可供一家渠道使用
				if (tSmsProductInfo.getType() == 2) {//包月
					
				} else {
//					int insL = instruction.length();
//					String extraStr = msg.substring(insL);
					int sellerId = 22;
					
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
						//判断省是否到量
						TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
						Integer packagedaylimit_conf = tSmsAppLimit.getDayLimit();
						if (packagedaylimit_conf == -1) {//不屏蔽
							flag = true;
						}
					} else {//未取到号码所在地
						flag = true;
					}
					boolean validateUser = validateUser(mobile, sellerId, appId);
					
					//订单号
					String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
					
					TSmsOrder tSmsOrder = new TSmsOrder();
					tSmsOrder.setOrderId(orderSeq);
					tSmsOrder.setOutTradeNo(null);
					tSmsOrder.setLinkId(linkId);
					tSmsOrder.setImsi(mobile);
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
					} else if(flag && !validateUser) {
						tSmsOrder.setStatus("4");
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
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
						StringUtil.printJson(response, returnJson.toString());
						return;
					} else if(flag && !validateUser){
						returnJson.put("code", "1010");
						returnJson.put("msg", "该用户超过日月限或被黑名单屏蔽");
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
						return;
					} else {
						String tips = tSmsApp.getTips();
						tips = MessageFormat.format(tips, fee, proName);
						
						returnJson.put("code", "1");
						returnJson.put("msg", tips);
						LogUtil.log("smsmo returnJson:"+returnJson.toString());
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
				
				String moMsg = tSmsOrder.getMoMsg();
				Integer sellerId = tSmsOrder.getSellerId();
				Integer appId = tSmsOrder.getAppId();
				Integer fee = tSmsOrder.getFee();
//				TMobileArea mobileArea = mobileAreaManager.getMobileArea(mobile);
//				if (!Utils.isEmpty(mobileArea)) {
//					tSmsOrder.setProvince(mobileArea.getProvince());
//				}
				
				smsOrderManager.save(tSmsOrder);
				
				//增加缓存
				setLimitCache(mobile, sellerId, appId, fee);
				//增加今日量
				if ("3".equals(status)) {
					smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
				}
				
				//调用回调渠道方法
				TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
				String callbackUrl = tSmsSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					new Thread(new SendPartner(mobile, status, tSmsOrder.getOrderId(), tSmsOrder.getOutTradeNo(), tSmsOrder.getFee()+"", moMsg, callbackUrl)).start();
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
		private String moMsg;
		
		public SendPartner(String phone, String status, String orderNo, String outTradeNo, String fee, String moMsg, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.outTradeNo = outTradeNo;
			this.moMsg = moMsg;
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
				jsonObject.put("msg", moMsg);
				
		        LogUtil.log("smsOrder call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
	/**
	 * 校验用户是否可订购 false-屏蔽 true-不屏蔽
	 * @param phone
	 * @param sellerId
	 * @param appId
	 * @return
	 */
	public boolean validateUser(String phone, Integer sellerId, Integer appId) {
		boolean ret = false;//false-屏蔽 true-不屏蔽
		try {
			//判断用户是否在黑名单
			boolean isExist = blackListManager.isBlackList(phone);
			if (isExist) {
				ret = false;
				return ret;
			} else {
				ret = true;
			}
			
			//初始化缓存
			setLimitCache(phone, sellerId, appId, 0);
			
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
			String day = sdfDay.format(calendar.getTime());
			String month = sdfMonth.format(calendar.getTime());
			
			String SMSORDER_USER_DAY_LIMIT = "smsOrder_user_daylimit_"+phone+"_"+day;
			String SMSORDER_USER_MONTH_LIMIT = "smsOrder_user_monthlimit_"+phone+"_"+month;
			//判断是否用户日限当达
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			if (ret) {
				Integer userDaylimit = (Integer)mc.getCache(SMSORDER_USER_DAY_LIMIT);
				if (userDaylimit < 500) {
					ret = true;
				} else {
					ret = false;
				}
			}
			//判断是否用户月限当达
			if (ret) {
				Integer userMonthlimit = (Integer)mc.getCache(SMSORDER_USER_MONTH_LIMIT);
				if (userMonthlimit < 1000) {
					ret = true;
				} else {
					ret = false;
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return ret;
	}
	
	private void setLimitCache(String phone, Integer sellerId, Integer appId, Integer fee) {
		try {
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
			String day = sdfDay.format(calendar.getTime());
			String month = sdfMonth.format(calendar.getTime());
			
			SimpleDateFormat sdfsqlday = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//获取当日开始结束时间
			String startdayString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startdayString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			String enddayString = sdfsqlday.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(enddayString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			//获取当月开始结束时间
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String mstartString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date mstartDate = sdfSql.parse(mstartString);
			java.sql.Date mstart = new java.sql.Date(mstartDate.getTime());
			calendar.add(Calendar.MONTH, 1);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String mendString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date mendDate = sdfSql.parse(mendString);
			java.sql.Date mend = new java.sql.Date(mendDate.getTime());
			
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			String provinceEncoder = null;
			String province = null;
			if (!Utils.isEmpty(mobileArea)) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province,"UTF-8");
			}

			String SMSORDER_USER_DAY_LIMIT = "smsOrder_user_daylimit_"+phone+"_"+day;
			String SMSORDER_USER_MONTH_LIMIT = "smsOrder_user_monthlimit_"+phone+"_"+month;
			String SMSORDER_SELLER_PROVINCE_DAY_LIMIT = "smsOrder_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String SMSORDER_APP_PROVINCE_DAY_LIMIT = "smsOrder_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String SMSORDER_APP_PROVINCE_MONTH_LIMIT = "smsOrder_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			//用户日限
			Integer userDaylimit = (Integer)iCacheClient.getCache(SMSORDER_USER_DAY_LIMIT);
			if (Utils.isEmpty(userDaylimit)) {
				Integer sumfeeByPhone = smsOrderManager.getSumFeeByPhone(sellerId, phone, start, end);
				iCacheClient.setCache(SMSORDER_USER_DAY_LIMIT, sumfeeByPhone+fee, CacheFactory.DAY);
			} else {
				iCacheClient.setCache(SMSORDER_USER_DAY_LIMIT, userDaylimit+fee, CacheFactory.DAY);
			}
			//用户月限
			Integer userMonthlimit = (Integer)iCacheClient.getCache(SMSORDER_USER_MONTH_LIMIT);
			if (Utils.isEmpty(userMonthlimit)) {
				Integer sumfeeByPhone = smsOrderManager.getSumFeeByPhone(sellerId, phone, mstart, mend);
				iCacheClient.setCache(SMSORDER_USER_MONTH_LIMIT, sumfeeByPhone+fee, CacheFactory.UNEXPIRY);
			} else {
				iCacheClient.setCache(SMSORDER_USER_MONTH_LIMIT, userMonthlimit+fee, CacheFactory.UNEXPIRY);
			}
			//渠道省份日限
			Integer sellerdaylimit = (Integer)iCacheClient.getCache(SMSORDER_SELLER_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(sellerdaylimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String sellerProvLimitKey = "smsOrder_seller_prov_daylimit_"+sellerId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(sellerProvLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(SMSORDER_SELLER_PROVINCE_DAY_LIMIT, sellerdaylimit+fee, CacheFactory.DAY);
			}
			//app省份日限
			Integer appdaylimit = (Integer)iCacheClient.getCache(SMSORDER_APP_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(appdaylimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountByAppId(appId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvDayLimitKey = "smsOrder_app_prov_daylimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(appProvDayLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(SMSORDER_APP_PROVINCE_DAY_LIMIT, appdaylimit+fee, CacheFactory.DAY);
			}
			//app省份月限
			Integer appmonthlimit = (Integer)iCacheClient.getCache(SMSORDER_APP_PROVINCE_MONTH_LIMIT);
			if (Utils.isEmpty(appmonthlimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountByAppId(appId, mstart, mend, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvMonthLimitKey = "smsOrder_app_prov_monthlimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+month;
						iCacheClient.setCache(appProvMonthLimitKey, provFee+fee, CacheFactory.UNEXPIRY);
					}
				}
			} else {
				iCacheClient.setCache(SMSORDER_APP_PROVINCE_MONTH_LIMIT, appmonthlimit+fee, CacheFactory.UNEXPIRY);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
