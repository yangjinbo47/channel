package com.tenfen.www.action.external.sms.unicombroadband;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.entity.operation.sms.TSmsSellerLimit;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class UnicomBroadBandAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private BlackListManager blackListManager;
	
	public void callBack() {
		try {
			Map<String, String[]> map = request.getParameterMap();
			for (String key : map.keySet()) {
				String[] value = map.get(key);
				for (String string : value) {
					LogUtil.log("unicombroadband callBack param key:"+key+" value:"+string);
				}
			}
			
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			LogUtil.log("unicombroadband callback："+sb.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void statusRet() {
		try {
			Map<String, String[]> map = request.getParameterMap();
			for (String key : map.keySet()) {
				String[] value = map.get(key);
				for (String string : value) {
					LogUtil.log("unicombroadband statusRet param key:"+key+" value:"+string);
				}
			}
			
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			LogUtil.log("unicombroadband statusRet："+sb.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String callbackUrl;
		private String phone;
		private String content;
		private String province;
		private String channelNo;
		
		public SendPartner(String phone, String status, String orderNo, String fee, String channelNo, String content, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.callbackUrl = callbackUrl;
			this.channelNo = channelNo;
			this.content = content;
			this.province = province;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("phone", phone);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				jsonObject.put("channel_no", channelNo);
				jsonObject.put("content", content);
				jsonObject.put("province", province);
				
		        LogUtil.log("unicomzx call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
	}
	
	public void limit() throws Exception {
//		String mo = ServletRequestUtils.getStringParameter(request, "mo", null);
		boolean ret = false;
		String retStr = "0";
		
		try {
			Map<String, String[]> map = request.getParameterMap();
			String mo = null;
			for (String key : map.keySet()) {
				mo = key;
				if (!Utils.isEmpty(key)) {
					break;
				}
			}
			
			JSONObject json = JSONObject.parseObject(mo);
			String phone = json.getString("userNumber");
			String messageContent = json.getString("messageContent");
			
			if (!Utils.isEmpty(phone)) {
				phone = phone.substring(phone.indexOf("86")+2);
			}
			
			//判断用户是否在黑名单
			boolean isExist = blackListManager.isBlackList(phone);
			if (isExist) {
				ret = false;
				retStr = "1000";
			} else {
				ret = true;
			}
			
			String province = null;
			String provinceEncoder = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (!Utils.isEmpty(mobileArea)) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province, "UTF-8");
			}
			
			Integer sellerdaylimit_conf = null;
			Integer appdaylimit_conf = null;
			Integer appmonthlimit_conf = null;
			Integer sellerId = null;
			TSmsApp tSmsApp = null;
			Integer appId = null;
			if (!Utils.isEmpty(messageContent)) {
				String[] message = messageContent.split("#");
				sellerId = Integer.parseInt(message[1]);
				TSmsSellerLimit tSmsSellerLimit = smsSellerManager.findSellerLimitByProperty(sellerId, province);
				sellerdaylimit_conf = tSmsSellerLimit.getDayLimit();
				TSmsSeller tSmsSeller = smsSellerManager.get(sellerId);
				List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
				if (smsSellerAppList.size() > 0) {
					tSmsApp = smsSellerAppList.get(0).getSmsApp();
				}
				appId = tSmsApp.getId();
				if (!Utils.isEmpty(appId)) {
					TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
					appdaylimit_conf = tSmsAppLimit.getDayLimit();
					appmonthlimit_conf = tSmsAppLimit.getMonthLimit();
				}
			}
			
			//初始化缓存
			setCache(phone, sellerId, appId, 0);
			
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
			String day = sdfDay.format(calendar.getTime());
			String month = sdfMonth.format(calendar.getTime());
			String SMS_USER_DAY_LIMIT = "unicomzx_user_daylimit_"+phone+"_"+day;
			String SMS_USER_MONTH_LIMIT = "unicomzx_user_monthlimit_"+phone+"_"+month;
			String SMS_SELLER_PROVINCE_DAY_LIMIT = "unicomxz_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String SMS_APP_PROVINCE_DAY_LIMIT = "unicomxz_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String SMS_APP_PROVINCE_MONTH_LIMIT = "unicomxz_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			//判断是否用户日限当达
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			if (ret) {				
				Integer userDaylimit = (Integer)mc.getCache(SMS_USER_DAY_LIMIT);
				if (userDaylimit <= 1000) {
					ret = true;
				} else {
					ret = false;
					retStr = "1001";
				}
			}
			//判断是否用户月限当达
			if (ret) {
				Integer userMonthlimit = (Integer)mc.getCache(SMS_USER_MONTH_LIMIT);
				if (userMonthlimit <= 2000) {
					ret = true;
				} else {
					ret = false;
					retStr = "1002";
				}
			}
			//判断渠道日限是否到达
			if (ret) {
				Integer sellerdaylimit = (Integer)mc.getCache(SMS_SELLER_PROVINCE_DAY_LIMIT);
				sellerdaylimit = sellerdaylimit == null ? 0 : sellerdaylimit;
				if (sellerdaylimit_conf == -1) {//无限制
					ret = true;
				} else if (sellerdaylimit < sellerdaylimit_conf){//缓存中限制还没到设定值
					ret = true;
				} else {
					ret = false;
					retStr = "1003";
				}
			}
			//判断app日限是否到达
			if (ret) {
				Integer appdaylimit = (Integer)mc.getCache(SMS_APP_PROVINCE_DAY_LIMIT);
				appdaylimit = appdaylimit == null ? 0 : appdaylimit;
				if (appdaylimit_conf == -1) {//无限制
					ret = true;
				} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
					ret = true;
				} else {
					ret = false;
					retStr = "1004";
				}
			}
			//判断app月限是否到达
			if (ret) {
				Integer appmonthlimit = (Integer)mc.getCache(SMS_APP_PROVINCE_MONTH_LIMIT);
				appmonthlimit = appmonthlimit == null ? 0 : appmonthlimit;
				if (appmonthlimit_conf == -1) {//无限制
					ret = true;
				} else if (appmonthlimit < appmonthlimit_conf){//缓存中限制还没到设定值
					ret = true;
				} else {
					ret = false;
					retStr = "1005";
				}
			}
			
			LogUtil.log("unicomzx limit phone:"+phone+" result:"+retStr);
		} catch (Exception e) {
			retStr = "0";
			LogUtil.error(e.getMessage(), e);
		}
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println(retStr);
		out.flush();
		out.close();
	}
	
	private void setCache(String phone, Integer sellerId, Integer appId, Integer fee) {
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

			String SMS_USER_DAY_LIMIT = "unicomzx_user_daylimit_"+phone+"_"+day;
			String SMS_USER_MONTH_LIMIT = "unicomzx_user_monthlimit_"+phone+"_"+month;
			String SMS_SELLER_PROVINCE_DAY_LIMIT = "unicomxz_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String SMS_APP_PROVINCE_DAY_LIMIT = "unicomxz_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String SMS_APP_PROVINCE_MONTH_LIMIT = "unicomxz_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			//用户日限
			Integer userDaylimit = (Integer)iCacheClient.getCache(SMS_USER_DAY_LIMIT);
			if (Utils.isEmpty(userDaylimit)) {
				Integer sumfeeByPhone = smsOrderManager.getSumFeeByPhone(sellerId, phone, start, end);
				iCacheClient.setCache(SMS_USER_DAY_LIMIT, sumfeeByPhone+fee, CacheFactory.DAY);
			} else {
				iCacheClient.setCache(SMS_USER_DAY_LIMIT, userDaylimit+fee, CacheFactory.DAY);
			}
			//用户月限
			Integer userMonthlimit = (Integer)iCacheClient.getCache(SMS_USER_MONTH_LIMIT);
			if (Utils.isEmpty(userMonthlimit)) {
				Integer sumfeeByPhone = smsOrderManager.getSumFeeByPhone(sellerId, phone, mstart, mend);
				iCacheClient.setCache(SMS_USER_MONTH_LIMIT, sumfeeByPhone+fee, CacheFactory.UNEXPIRY);
			} else {
				iCacheClient.setCache(SMS_USER_MONTH_LIMIT, userMonthlimit+fee, CacheFactory.UNEXPIRY);
			}
			//渠道省份日限
			Integer sellerdaylimit = (Integer)iCacheClient.getCache(SMS_SELLER_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(sellerdaylimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String sellerProvLimitKey = "unicomxz_seller_prov_daylimit_"+sellerId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(sellerProvLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(SMS_SELLER_PROVINCE_DAY_LIMIT, sellerdaylimit+fee, CacheFactory.DAY);
			}
			//app省份日限
			Integer appdaylimit = (Integer)iCacheClient.getCache(SMS_APP_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(appdaylimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountByAppId(appId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvDayLimitKey = "unicomxz_app_prov_daylimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(appProvDayLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(SMS_APP_PROVINCE_DAY_LIMIT, appdaylimit+fee, CacheFactory.DAY);
			}
			//app省份月限
			Integer appmonthlimit = (Integer)iCacheClient.getCache(SMS_APP_PROVINCE_MONTH_LIMIT);
			if (Utils.isEmpty(appmonthlimit)) {
				Map<String, String> map = smsOrderManager.getProvinceCountByAppId(appId, mstart, mend, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvMonthLimitKey = "unicomxz_app_prov_monthlimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+month;
						iCacheClient.setCache(appProvMonthLimitKey, provFee+fee, CacheFactory.UNEXPIRY);
					}
				}
			} else {
				iCacheClient.setCache(SMS_APP_PROVINCE_MONTH_LIMIT, appmonthlimit+fee, CacheFactory.UNEXPIRY);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
