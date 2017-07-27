package com.tenfen.www.action.external.open.woplus;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.DateUtil;
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

public class WoPlusMixAction extends SimpleActionSupport{

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
	@Autowired
	private CacheFactory cacheFactory;
	
	
	/**
	 * 生成单笔订单action
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		int chargeType = ServletRequestUtils.getIntParameter(request, "charge_type", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			LogUtil.log("woplusmix 参数: seller_key:"+sellerKey+" imsi:"+imsi+" phone:"+phone+" app_name:"+appNameDecode+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			} else if (Utils.isEmpty(phone)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "phone参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("phone", phone));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("charge_type",chargeType+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean limit = false;//检测应用是否全部达到限量值
			TOpenProductInfo pro = null;
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			if (openSellerAppList.size() > 0) {
				for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {					
					Integer appLimit = tOpenSellerApps.getAppLimit();
					Integer appToday = tOpenSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tOpenApp = tOpenSellerApps.getOpenApp();
					List<TOpenProductInfo> proList = tOpenApp.getProductList();
					for (TOpenProductInfo tOpenProductInfo : proList) {
						if (tOpenProductInfo.getPrice() == fee && tOpenProductInfo.getType() == chargeType) {
							limit = true;
							pro = tOpenProductInfo;
						}
					}
					if (!Utils.isEmpty(pro)) {
						break;
					}
				}
			} else {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!limit) {
				returnJson.put("code", "1012");
				returnJson.put("msg", "未找到符合条件app");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			////////检测省份是否到量
			Integer appId = tOpenApp.getId();
			String province = null;
			String provinceEncoder = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province, "UTF-8");
			}
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			String day = sdfDay.format(calendar.getTime());
			Integer appdaylimit_conf = null;
			if (!Utils.isEmpty(appId)) {
				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
				appdaylimit_conf = tOpenAppLimit.getDayLimit();
			}
			String WOPlUS_APP_PROVINCE_DAY_LIMIT = "woplus_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			//判断app日限是否到达
			Integer appdaylimit = (Integer)mc.getCache(WOPlUS_APP_PROVINCE_DAY_LIMIT);
			appdaylimit = appdaylimit == null ? 0 : appdaylimit;
			if (appdaylimit_conf == -1) {//无限制
				//不处理
			} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
				//不处理
			} else {
				returnJson.put("code", "1013");
				returnJson.put("msg", "该省份已达到当日推送量");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String key = tOpenApp.getAppKey();//cpid
			String secret = tOpenApp.getAppSecret();//key
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			//通过app查询关联的计费信息
//			List<TOpenProductInfo> proList = tOpenApp.getProductList();
//			for (TOpenProductInfo tOpenProductInfo : proList) {
//				if (tOpenProductInfo.getPrice() == fee) {
//				}
//			}
			JSONObject json = generateWoPlusOrder(appNameDecode, outTradeNo, appId, merchantId, sellerId, key, secret, pro.getPrice(), imsi, phone, pro.getInstruction(), pro.getCode(), pro.getType());
			int resultCode = json.getInteger("resultCode");
			if (resultCode != 0) {
				returnJson.put("code", "2000");
				returnJson.put("msg", json);
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else {
				returnJson.put("code", "1");
				returnJson.put("msg", json);
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 支付
	 */
	public void verify() {
		String verifyCode = ServletRequestUtils.getStringParameter(request, "verify_code", null);
		String orderId = ServletRequestUtils.getStringParameter(request, "order_id", null);
		String cpOrderId = ServletRequestUtils.getStringParameter(request, "cp_order_id", null);
		JSONObject returnJson = new JSONObject();
		try {
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", cpOrderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				int fee = tOpenOrder.getFee();
				Integer appId = tOpenOrder.getAppId();
				TOpenApp tOpenApp = openAppManager.getEntity(appId);
				String cpId = tOpenApp.getAppKey();
				String key = tOpenApp.getAppSecret();
				
				String goodsCode = null;
				Integer type = null;//1-点播 2-包月
				List<TOpenProductInfo> proList = tOpenApp.getProductList();
				for (TOpenProductInfo tOpenProductInfo : proList) {
					if (tOpenProductInfo.getPrice() == fee) {
						goodsCode = tOpenProductInfo.getInstruction();
						type = tOpenProductInfo.getType();
					}
				}
				
				String timestamp = String.valueOf(System.currentTimeMillis()/1000);
				
				List<TokenParam> queryParamList = new ArrayList<TokenParam>();
				queryParamList.add(new TokenParam("orderId",orderId));
				queryParamList.add(new TokenParam("cpId",cpId));
				queryParamList.add(new TokenParam("goodsCode", goodsCode));
				queryParamList.add(new TokenParam("verifyCode", verifyCode));
				queryParamList.add(new TokenParam("timestamp",timestamp));
				String sign = TokenService.buildWoPlusMixToken(queryParamList, key);
				
				JSONObject json = new JSONObject();
				json.put("orderId", orderId);
				json.put("cpOrderId", cpOrderId);
				json.put("cpId", cpId);
				json.put("goodsCode", goodsCode);
				json.put("verifyCode", verifyCode);
				json.put("timestamp", timestamp);
				json.put("sign", sign);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("data", json.toJSONString());
				String resultData = null;
				if (type == 1) {
					resultData = HttpClientUtils.simpleGetInvoke("http://220.194.53.168:7020/api/b2b/wopluspayment", map);
				} else if (type == 2) {
					resultData = HttpClientUtils.simpleGetInvoke("http://220.194.53.168:7020/api/b2b/wopluspaymentmonthwo2", map);
				}
				LogUtil.log("woplusmix pay 返回："+resultData);
				JSONObject jsonObj = JSONObject.parseObject(resultData);
				
				returnJson.put("code", "1");
				returnJson.put("msg", jsonObj);
				StringUtil.printJson(response, returnJson.toString());
			} else {
				returnJson.put("code", "1001");
				returnJson.put("msg", "订单号不存在");
				StringUtil.printJson(response, returnJson.toString());
			}
			
		} catch (Exception e) {
			returnJson.put("code", "9999");
			returnJson.put("msg", "未知异常");
			StringUtil.printJson(response, returnJson.toString());
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private JSONObject generateWoPlusOrder(String appName, String outTradeNo, Integer appId, Integer merchantId, Integer sellerId, String cpId, String key, Integer price, String imsi, String phone, String goodsCode, String senderNumber, Integer type) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateWoPlusOrder(phone, orderNo, cpId, key, goodsCode, type);
			JSONObject json = JSONObject.parseObject(result);
			Integer code = json.getInteger("resultCode");
			String description = json.getString("resultMsg");
			
			if (code == 0) {
				String orderId = json.getString("orderId");//wo+订单号
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderNo);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setSubject(appName);
				tOpenOrder.setSenderNumber(senderNumber);
				tOpenOrder.setMsgContent(orderId);
				tOpenOrder.setFee(price);
				tOpenOrder.setPayPhone(phone);
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				if (!Utils.isEmpty(mobileArea)) {
					tOpenOrder.setProvince(mobileArea.getProvince());
				}
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderId);
				returnJsonMsgObj.put("cp_order_id", orderNo);
				returnJsonMsgObj.put("resultCode", code);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("description", description);
			} else {
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("resultCode", code);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("description", description);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	private String generateWoPlusOrder(String phone, String orderNo, String cpId, String key, String goodsCode, Integer type) {
		String resultData = null;
		try {
			String orderType = "0";
			if (type == 1) {
				orderType = "0";//按次点播
			} else if (type == 2) {
				orderType = "1";//周期性计费
			}
			String timestamp = String.valueOf(System.currentTimeMillis()/1000);
			String sourceType = "2";
			String channelCode = "1";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
			queryParamList.add(new TokenParam("cpOrderId",orderNo));
			queryParamList.add(new TokenParam("cpId",cpId));
			queryParamList.add(new TokenParam("goodsCode", goodsCode));
			queryParamList.add(new TokenParam("orderType", orderType));
			queryParamList.add(new TokenParam("mobile", phone));
			queryParamList.add(new TokenParam("timestamp",timestamp));
			queryParamList.add(new TokenParam("sourceType", sourceType));
			queryParamList.add(new TokenParam("channelCode", channelCode));
			String sign = TokenService.buildWoPlusMixToken(queryParamList, key);
			
			JSONObject json = new JSONObject();
			json.put("cpOrderId", orderNo);
			json.put("cpId", cpId);
			json.put("goodsCode", goodsCode);
			json.put("orderType", orderType);
			json.put("mobile", phone);
			json.put("timestamp", timestamp);
			json.put("sourceType", sourceType);
			json.put("channelCode", channelCode);
			json.put("sign", sign);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("data", json.toJSONString());
			if (type == 1) {
				resultData = HttpClientUtils.simpleGetInvoke("http://220.194.53.168:7020/api/b2b/sendVerificationCode", map);
			} else if (type == 2) {
				resultData = HttpClientUtils.simpleGetInvoke("http://220.194.53.168:7020/api/b2b/sendMonthWo2VerificationCode", map);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return resultData;
	}
	
	public void callBack() {
		LogUtil.log("woplusmix callBack visiting");
		Map<String, String[]> map = request.getParameterMap();
		for (String key : map.keySet()) {
			String[] value = map.get(key);
			for (String string : value) {
				LogUtil.log("woplusmix callBack param key:"+key+" value:"+string);
			}
		}
		
		String cpOrderId = ServletRequestUtils.getStringParameter(request, "cpOrderId", null);
		String status = ServletRequestUtils.getStringParameter(request, "status", null);
		int reduce = 0;
		JSONObject returnJson = new JSONObject();
		try {
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", cpOrderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				String tradeStatus = null;
				if ("4".equals(status)) {//订购成功
					tradeStatus = "3";
				} else if ("5".equals(status)) {
					tradeStatus = "4";
				} else if ("8".equals(status)){
					tradeStatus = "5";
				}
				
				if (tradeStatus != null) {
					tOpenOrder.setStatus(tradeStatus);
					if ("3".equals(tradeStatus)) {
						tOpenOrder.setPayTime(new Date());
						String province = tOpenOrder.getProvince();
						//是否扣量
						Integer appId = tOpenOrder.getAppId();
						TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
						double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
						double rate = new Random().nextDouble();
						if (rate < reduce_conf) {
							reduce = 1;
							tOpenOrder.setReduce(reduce);
						}
						
						//增加缓存记录值
						String phone = tOpenOrder.getPayPhone();
						Integer sellerId = tOpenOrder.getSellerId();
						Integer fee = tOpenOrder.getFee();
						setLimitCache(phone, sellerId, appId, fee);
					} else if ("5".equals(tradeStatus)) {//退订
						tOpenOrder.setUnsubscribeTime(new Date());
					}
					openOrderManager.save(tOpenOrder);
					
					//增加今日量
					if ("3".equals(tradeStatus)) {
						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
						
						//回调渠道
						TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
						String callbackUrl = tOpenSeller.getCallbackUrl();
						if (!Utils.isEmpty(callbackUrl)) {
							String outTradeNo = tOpenOrder.getOutTradeNo();
							if (reduce != 1) {//不扣量
								new Thread(new SendPartner(tradeStatus,cpOrderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
							} else {
								new Thread(new SendPartner("4",cpOrderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
							}
						}
					}
				}
				
				
				returnJson.put("resultCode", "0");
				returnJson.put("resultMsg", "SUCCESS");
				System.out.println(returnJson.toString());
			} else {
				returnJson.put("resultCode", "1001");
				returnJson.put("resultMsg", "未查询到订单");
				System.out.println(returnJson.toString());
			}
		} catch (Exception e) {
			returnJson.put("resultCode", "9999");
			returnJson.put("resultMsg", "未知异常");
			System.out.println(returnJson.toString());
			LogUtil.error(e.getMessage(), e);
		}
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
				
				LogUtil.log("sendChannelWoPlusMixMsg:"+jsonObject.toString());
				HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
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

			String WOPLUS_USER_DAY_LIMIT = "woplus_user_daylimit_"+phone+"_"+day;
			String WOPLUS_USER_MONTH_LIMIT = "woplus_user_monthlimit_"+phone+"_"+month;
			String WOPLUS_SELLER_PROVINCE_DAY_LIMIT = "woplus_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String WOPLUS_APP_PROVINCE_DAY_LIMIT = "woplus_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String WOPLUS_APP_PROVINCE_MONTH_LIMIT = "woplus_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			//用户日限
			Integer userDaylimit = (Integer)iCacheClient.getCache(WOPLUS_USER_DAY_LIMIT);
			if (Utils.isEmpty(userDaylimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, start, end);
				iCacheClient.setCache(WOPLUS_USER_DAY_LIMIT, sumfeeByPhone+fee, CacheFactory.DAY);
			} else {
				iCacheClient.setCache(WOPLUS_USER_DAY_LIMIT, userDaylimit+fee, CacheFactory.DAY);
			}
			//用户月限
			Integer userMonthlimit = (Integer)iCacheClient.getCache(WOPLUS_USER_MONTH_LIMIT);
			if (Utils.isEmpty(userMonthlimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, mstart, mend);
				iCacheClient.setCache(WOPLUS_USER_MONTH_LIMIT, sumfeeByPhone+fee, CacheFactory.UNEXPIRY);
			} else {
				iCacheClient.setCache(WOPLUS_USER_MONTH_LIMIT, userMonthlimit+fee, CacheFactory.UNEXPIRY);
			}
			//渠道省份日限
			Integer sellerdaylimit = (Integer)iCacheClient.getCache(WOPLUS_SELLER_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(sellerdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String sellerProvLimitKey = "woplus_seller_prov_daylimit_"+sellerId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(sellerProvLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_SELLER_PROVINCE_DAY_LIMIT, sellerdaylimit+fee, CacheFactory.DAY);
			}
			//app省份日限
			Integer appdaylimit = (Integer)iCacheClient.getCache(WOPLUS_APP_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(appdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvDayLimitKey = "woplus_app_prov_daylimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(appProvDayLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_APP_PROVINCE_DAY_LIMIT, appdaylimit+fee, CacheFactory.DAY);
			}
			//app省份月限
			Integer appmonthlimit = (Integer)iCacheClient.getCache(WOPLUS_APP_PROVINCE_MONTH_LIMIT);
			if (Utils.isEmpty(appmonthlimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, mstart, mend, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvMonthLimitKey = "woplus_app_prov_monthlimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+month;
						iCacheClient.setCache(appProvMonthLimitKey, provFee+fee, CacheFactory.UNEXPIRY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_APP_PROVINCE_MONTH_LIMIT, appmonthlimit+fee, CacheFactory.UNEXPIRY);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
