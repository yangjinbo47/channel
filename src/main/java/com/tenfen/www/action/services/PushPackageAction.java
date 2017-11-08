package com.tenfen.www.action.services;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.operation.PackageDailyBean;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class PushPackageAction extends SimpleActionSupport {

	private static final long serialVersionUID = 9173885858938550927L;

	@Autowired
	private PackageManager packageManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private PushSellerManager pushSellerManager;
	
	/**
	*@功能：检查是否包月
	*@author BOBO
	*@date Apr 18, 2014
	 */
	public void checkBaoyue() {
		try {
			String phoneNum = ServletRequestUtils.getStringParameter(getRequest(), "phoneNum");
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			String key = "check_baoyue_"+phoneNum;
			if (phoneNum != null && Utils.checkCellPhone(phoneNum)) {
				String result = (String)iCacheClient.getCache(key);
				if (result == null) {
					Calendar calendarStart = Calendar.getInstance();
					calendarStart.add(Calendar.MONTH, -2);
					calendarStart.set(Calendar.DAY_OF_MONTH, 1);
					java.sql.Date start = new java.sql.Date(calendarStart.getTimeInMillis());
					
					Calendar calendarEnd = Calendar.getInstance();
					calendarEnd.set(Calendar.DAY_OF_MONTH,1);
					calendarEnd.add(Calendar.MONTH, 1);
					java.sql.Date end = new java.sql.Date(calendarEnd.getTimeInMillis());
					
					Long baoyueNum = orderManager.getBaoyueCount(phoneNum, start, end);
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("phoneNum", phoneNum);
					jsonObject.put("packageNum", baoyueNum);
					result = jsonObject.toString();
					
					iCacheClient.setCache(key, result, CacheFactory.HOUR * 3);
				}
				
				StringUtil.printJson(getResponse(), result);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	*@功能：用户状态入库
	*@author BOBO
	*@date Jun 2, 2013
	 */
//	public void updateUser() {
//		try {
//			ICacheClient mc = cacheFactory.getCommonCacheClient();
//			String phoneNum = ServletRequestUtils.getStringParameter(getRequest(), "phoneNum");
//			Integer status = ServletRequestUtils.getIntParameter(getRequest(), "status");
//			String name = ServletRequestUtils.getStringParameter(getRequest(), "name");
//			String channel = ServletRequestUtils.getStringParameter(getRequest(), "channel");
//			String province = ServletRequestUtils.getStringParameter(getRequest(), "province");
//			Integer pushPackageId = ServletRequestUtils.getIntParameter(getRequest(), "pushPackageId");
//
//			if (phoneNum != null && Utils.checkCellPhone(phoneNum)) {
//				TOrder order = new TOrder();
//				order.setPhoneNum(phoneNum);
//				order.setStatus(status);
//				order.setCreateTime(new Date());
//				order.setName(name);
//				order.setChannel(channel);
//				order.setProvince(province);
//				order.setPushId(pushPackageId);
//				//保存订单(批量)并增加当日订购数
////				orderManager.batchSave(order);
//				orderManager.save(order);
//				//更新缓存
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//				Integer limit = (Integer) mc.getCache("limit_" + channel + "_" + pushPackageId + "_"+ sdf.format(new Date()));
//				if (Utils.isEmpty(limit)) {
//					PushPackage pushPackage = packageManager.get(pushPackageId);
//					int package_limit = pushPackage.getPackageLimit();
//					int package_today = pushPackage.getPackageToday();
//					limit = package_limit-package_today-1 < 0 ? 0 : package_limit-package_today-1;
//					mc.setCache("limit_" + channel + "_" + pushPackageId + "_" + sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
//				} else {
//					limit = limit - 1 < 0 ? 0 : limit - 1;
//					mc.setCache("limit_" + channel + "_" + pushPackageId + "_" + sdf.format(new Date()), limit, CacheFactory.DAY);// 包月剩余量
//				}
//				//删除用户是否包月检测缓存
//				String key = "check_baoyue_"+phoneNum;
//				mc.deleteCache(key);
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}

	/**
	*@功能：根据号码查地域
	*@author BOBO
	*@date Jun 16, 2013
	 */
	public void searchAreaByPhone() {
		try {
			String phoneNum = ServletRequestUtils.getStringParameter(getRequest(), "phoneNum");
			phoneNum = Utils.mobilePhoneFormat(phoneNum);
			if (!Utils.checkCellPhone(phoneNum)) {
				return;
			}
			
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNum);
			if (mobileArea != null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("phoneNum", phoneNum);
				jsonObject.put("area", mobileArea.getAddress());
				jsonObject.put("province", mobileArea.getProvince());
				
				StringUtil.printJson(getResponse(), jsonObject.toString());
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 获取该客户端版本剩余包月数量
	 */
//	public void getLimit() {
//		try {
//			ICacheClient mc = cacheFactory.getCommonCacheClient();
//			String clientVersion = ServletRequestUtils.getStringParameter(getRequest(), "clientVersion");
//			Integer pushPackageId = ServletRequestUtils.getIntParameter(getRequest(), "pushPackageId", 0);
//			
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			Integer limit = (Integer)mc.getCache("limit_"+clientVersion+"_"+pushPackageId+"_"+sdf.format(new Date()));
//			JSONObject jsonObject = new JSONObject();
//			if (Utils.isEmpty(limit)) {
//				PushPackage pushPackage = packageManager.get(pushPackageId);
//				int package_limit = pushPackage.getPackageLimit();
//				int package_today = pushPackage.getPackageToday();
//				limit = package_limit-package_today < 0 ? 0 : package_limit-package_today;
//				mc.setCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
//			}
//			jsonObject.put("limit", limit);
//			
//			StringUtil.printJson(getResponse(), jsonObject.toString());
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	private final List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
			"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆"});
	
	public void packageProvCount() {
		Integer sellerId = ServletRequestUtils.getIntParameter(getRequest(), "sellerId", -1);
		String startTime = ServletRequestUtils.getStringParameter(getRequest(), "start", null);
		String endTime = ServletRequestUtils.getStringParameter(getRequest(), "end", null);
		
		try {
			TPushSeller pushSeller = pushSellerManager.get(sellerId);
			String sellerName = pushSeller.getName();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
			java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
			
			Map<String, String> map = orderManager.mapReduceProvince(sellerId, start, end);
			Integer moQuanguo = 0;
			Integer moQuanguoQc = 0;
			Integer mrQuanguo = 0;
			Integer feeQuanguo = 0;
			String zhlQuanguo = null;
			
			List<PackageDailyBean> packageDailyBeans = new ArrayList<PackageDailyBean>();
			for (String province : provinceList) {
				Integer mo = 0;
				Integer moQc = 0;
				Integer mr = 0;
				Integer fee = 0;
				String zhl = null;
				String resultStr = map.get(province);
				if (resultStr != null) {
					JSONObject jsonObject = JSONObject.parseObject(resultStr);
					mo = jsonObject.getInteger("count") == null ? 0 : jsonObject.getInteger("count");//请求总数
					moQc = jsonObject.getInteger("user") == null ? 0 : jsonObject.getInteger("user");//mo去重
					mr = jsonObject.getInteger("succ") == null ? 0 : jsonObject.getInteger("succ");//mr
					fee = jsonObject.getInteger("fee") == null ? 0 : jsonObject.getInteger("fee");//成功信息费
					fee = fee / 100;//转化以元为单位
				}
				//转化率
				float f = 0;
				if (mr == 0) {
					f = 0;
				} else {
					f = (float)mr/moQc;
				}
				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
				if (f == 0) {
					zhl = "0%";
				} else {
					zhl = df.format(f*100) + "%";//返回的是String类型的
				}
				
				PackageDailyBean packageDailyBean = new PackageDailyBean();
				packageDailyBean.setProvince(province);
				packageDailyBean.setMo(mo);
				packageDailyBean.setMoQc(moQc);
				packageDailyBean.setMr(mr);
				packageDailyBean.setFee(fee);
				packageDailyBean.setZhlf(f);
				packageDailyBean.setZhl(zhl);
				packageDailyBeans.add(packageDailyBean);
				
				moQuanguo += mo;
				moQuanguoQc += moQc;
				mrQuanguo += mr;
				feeQuanguo += fee;
			}
			//全国转化率
			DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
			float quanguof = 0;
			if (mrQuanguo == 0) {
				quanguof = 0;
			} else {
				quanguof = (float)mrQuanguo/moQuanguoQc;
			}
			if (quanguof == 0) {
				zhlQuanguo = "0%";
			} else {
				zhlQuanguo = df.format(quanguof*100) + "%";//返回的是String类型的
			}
			Collections.sort(packageDailyBeans);//按转化率排序
			
			PackageDailyBean packageDailyBean = new PackageDailyBean();
			packageDailyBean.setProvince("全国");
			packageDailyBean.setMo(moQuanguo);
			packageDailyBean.setMoQc(moQuanguoQc);
			packageDailyBean.setMr(mrQuanguo);
			packageDailyBean.setFee(feeQuanguo);
			packageDailyBean.setZhlf(quanguof);
			packageDailyBean.setZhl(zhlQuanguo);
			packageDailyBeans.add(packageDailyBean);
			
			JSONObject returnJson = new JSONObject();
			returnJson.put("sellerId", sellerId);
			returnJson.put("sellerName", sellerName);
			returnJson.put("data", JSON.toJSONString(packageDailyBeans));
			
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void packageCount() {
		Integer sellerId = ServletRequestUtils.getIntParameter(getRequest(), "sellerId", -1);
		String startTime = ServletRequestUtils.getStringParameter(getRequest(), "start", null);
		String endTime = ServletRequestUtils.getStringParameter(getRequest(), "end", null);
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.sql.Date start = new java.sql.Date(sdfSql.parse(startTime).getTime());
			java.sql.Date end = new java.sql.Date(sdfSql.parse(endTime).getTime());
			
			//获取当月时间区间
			Calendar calendarMonth = Calendar.getInstance();
			calendarMonth.set(Calendar.DAY_OF_MONTH, 1);
			String monthStartString = sdf.format(calendarMonth.getTime()) + " 00:00:00";
			Date monthStartDate = sdfSql.parse(monthStartString);
			java.sql.Date monthStart = new java.sql.Date(monthStartDate.getTime());
			
			calendarMonth.add(Calendar.MONTH, 1);
			calendarMonth.set(Calendar.DAY_OF_MONTH, 1);
			calendarMonth.add(Calendar.DAY_OF_MONTH, -1);
			String monthEndString = sdf.format(calendarMonth.getTime()) + " 23:59:59";
			Date monthEndDate = sdfSql.parse(monthEndString);
			java.sql.Date monthEnd = new java.sql.Date(monthEndDate.getTime());
			
			JSONObject returnJson = new JSONObject();
			returnJson.put("sellerId", sellerId);
			Map<Integer, String> succ_day = orderManager.mapReducePushIds(sellerId, start, end, 3);//当日成功数
			Map<Integer, String> succ_month = orderManager.mapReducePushIds(sellerId, monthStart, monthEnd, 3);//当月成功数
			List<PackageDailyBean> packageDailyBeans = new ArrayList<PackageDailyBean>();
			for (Integer pushId : succ_month.keySet()) {
				PushPackage pushPackage = packageManager.get(pushId);
				String packageName = pushPackage.getPackageName();
				String monthStr = succ_month.get(pushId);
				JSONObject monthJson = JSONObject.parseObject(monthStr);
				Integer monthCount = monthJson.getInteger("succ");
				Integer monthFee = monthJson.getInteger("fee")/100;
				if (monthCount == 0) {
					continue;
				}
				
				Integer dayCount = 0;
				Integer dayFee = 0;
				String dayStr = succ_day.get(pushId);
				if (!Utils.isEmpty(dayStr)) {
					JSONObject dayJson = JSONObject.parseObject(dayStr);
					dayCount = dayJson.getInteger("succ");
					dayFee = dayJson.getInteger("fee")/100;
				}
				//计算每个包多少钱
				Integer fee = monthFee/monthCount;
				
				PackageDailyBean packageDailyBean = new PackageDailyBean();
				packageDailyBean.setFee(fee);
				packageDailyBean.setPackageName(packageName);
				packageDailyBean.setDayCount(dayCount);
				packageDailyBean.setDayFee(dayFee);
				packageDailyBean.setMonthCount(monthCount);
				packageDailyBean.setMonthFee(monthFee);
				packageDailyBeans.add(packageDailyBean);
			}
			
			returnJson.put("data", JSON.toJSONString(packageDailyBeans));
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

}
