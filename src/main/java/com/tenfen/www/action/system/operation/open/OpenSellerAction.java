package com.tenfen.www.action.system.operation.open;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

public class OpenSellerAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private CacheFactory cacheFactory;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String sellerName = ServletRequestUtils.getStringParameter(request, "sellerName", null);

		Page<TOpenSeller> sellerPage = new Page<TOpenSeller>();
		//设置默认排序方式
		sellerPage.setPageSize(limit);
		sellerPage.setPageNo(page);
		if (!sellerPage.isOrderBySetted()) {
			sellerPage.setOrderBy("id");
			sellerPage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
//		Integer userType = (Integer)getMemcacheAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(sellerName)) {
			sellerPage = openSellerManager.findSellerPage(sellerPage, userType);
		} else {
			sellerPage = openSellerManager.findSellerPage(sellerName, sellerPage, userType);
		}
		
		long nums = sellerPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("sellers:");

		List<TOpenSeller> sellerList = sellerPage.getResult();
		JSONArray jsonArray = new JSONArray();
		for (TOpenSeller tOpenSeller : sellerList) {
			JSONObject json = new JSONObject();
			json.put("id", tOpenSeller.getId());
			json.put("name", tOpenSeller.getName());
			json.put("email", tOpenSeller.getEmail());
			json.put("contact", tOpenSeller.getContact());
			json.put("telephone", tOpenSeller.getTelephone());
			json.put("sellerKey", tOpenSeller.getSellerKey());
			json.put("sellerSecret", tOpenSeller.getSellerSecret());
			json.put("callbackUrl", tOpenSeller.getCallbackUrl());
			json.put("status", tOpenSeller.getStatus());
			json.put("companyShow", tOpenSeller.getCompanyShow());
			jsonArray.add(json);
		}
		jstr.append(jsonArray.toString());
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void treelist() {
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
//		Integer userType = (Integer) getMemcacheAttribute(Constants.OPERATOR_TYPE);
		List<TOpenSeller> sellerTmpList = openSellerManager.findAllOpenSellerList(userType);
		
		List<TOpenSeller> sellerList = new ArrayList<TOpenSeller>();
		for (TOpenSeller tOpenSeller : sellerTmpList) {
			TOpenSeller openSeller = new TOpenSeller();
			openSeller.setId(tOpenSeller.getId());
			openSeller.setName(tOpenSeller.getName());
			sellerList.add(openSeller);
		}
		
		long nums = sellerList.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("children:");

		jstr.append(JSON.toJSONString(sellerList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);
			String email = ServletRequestUtils.getStringParameter(request, "email", null);
			String contact = ServletRequestUtils.getStringParameter(request, "contact", null);
			String telephone = ServletRequestUtils.getStringParameter(request, "telephone", null);
			String sellerKey = ServletRequestUtils.getStringParameter(request, "sellerKey", null);
			String sellerSecret = ServletRequestUtils.getStringParameter(request, "sellerSecret", null);
			String callbackUrl = ServletRequestUtils.getStringParameter(request, "callbackUrl", null);
			Integer status = ServletRequestUtils.getIntParameter(request, "status", -1);
			Integer companyShow = ServletRequestUtils.getIntParameter(request, "companyShow", -1);

			if (id == -1) {
				TOpenSeller tOpenSeller = new TOpenSeller();
				tOpenSeller.setName(name);
				tOpenSeller.setEmail(email);
				tOpenSeller.setContact(contact);
				tOpenSeller.setTelephone(telephone);
				tOpenSeller.setSellerKey(sellerKey);
				tOpenSeller.setSellerSecret(sellerSecret);
				tOpenSeller.setCallbackUrl(callbackUrl);
				tOpenSeller.setStatus(status);
				tOpenSeller.setCompanyShow(companyShow);
				openSellerManager.save(tOpenSeller);
			} else {//更新
				TOpenSeller tOpenSeller = openSellerManager.getEntity(id);
				if (tOpenSeller != null) {
					tOpenSeller.setName(name);
					tOpenSeller.setEmail(email);
					tOpenSeller.setContact(contact);
					tOpenSeller.setTelephone(telephone);
					tOpenSeller.setSellerKey(sellerKey);
					tOpenSeller.setSellerSecret(sellerSecret);
					tOpenSeller.setCallbackUrl(callbackUrl);
					tOpenSeller.setStatus(status);
					tOpenSeller.setCompanyShow(companyShow);
					openSellerManager.save(tOpenSeller);
				}
			}

			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 删除
	 */
	public void delete() {
		try {
			String ids = ServletRequestUtils.getStringParameter(getRequest(), "ids");
			if (!Utils.isEmpty(ids)) {
				String[] idsArr = ids.split(",");
				for (String id : idsArr) {
//					openSellerManager.delete(Integer.parseInt(id));
					TOpenSeller tOpenSeller = openSellerManager.getEntity(Integer.parseInt(id));
					List<TOpenSellerApps> list = tOpenSeller.getSellerApps();
					for (TOpenSellerApps tOpenSellerApps : list) {
						tOpenSellerApps.setOpenApp(null);
					}
					openSellerManager.delete(tOpenSeller);
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 查询app关联的产品
	 */
	public void appsOfSeller() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);

		TOpenSeller tOpenSeller = openSellerManager.getEntity(sellerId);
		
		List<TOpenApp> tOpenApps = new ArrayList<TOpenApp>();
		List<TOpenSellerApps> tOpenSellerAppList = tOpenSeller.getSellerApps();
		for (TOpenSellerApps tOpenSellerApps : tOpenSellerAppList) {
			TOpenApp tOpenApp = new TOpenApp();
			tOpenApp.setId(tOpenSellerApps.getOpenApp().getId());
			tOpenApp.setName(tOpenSellerApps.getOpenApp().getName());
			tOpenApp.setAppLimit(tOpenSellerApps.getAppLimit());
			tOpenApp.setAppToday(tOpenSellerApps.getAppToday());
			tOpenApps.add(tOpenApp);
		}
		int size = tOpenApps.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("apps:");
		jstr.append(JSON.toJSONString(tOpenApps));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给渠道的app
	 */
	public void getUnallocateApps() {
		String appIds = ServletRequestUtils.getStringParameter(request, "appIds", "");
		
		String[] idsArr = appIds.split(",");
		List<TOpenApp> apps = new ArrayList<TOpenApp>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		TOpenApp tOpenApp = openAppManager.getEntity(Integer.parseInt(string));
        		apps.add(tOpenApp);
			}
		}
		
		List<TOpenApp> appsAll = openAppManager.getAll();
		appsAll.removeAll(apps);
		
		int size = appsAll.size();
		List<TOpenApp> appsAllList = new ArrayList<TOpenApp>();
		for (TOpenApp tOpenApp : appsAll) {
			TOpenApp tOpenAppNew = new TOpenApp();
			tOpenAppNew.setId(tOpenApp.getId());
			tOpenAppNew.setName(tOpenApp.getName());
			tOpenAppNew.setAppLimit(-1);
			tOpenAppNew.setAppToday(0);
			appsAllList.add(tOpenAppNew);
		}
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("apps:");
		jstr.append(JSON.toJSONString(appsAllList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveSellerAppRelation() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		String appIds = ServletRequestUtils.getStringParameter(request, "appIds", "");
		String appLimit = ServletRequestUtils.getStringParameter(request, "appLimit", "");
//		String appToday = ServletRequestUtils.getStringParameter(request, "appToday", "");
		
		try {
			String[] idsArr = appIds.split(",");
			List<Integer> appids = new ArrayList<Integer>();
			for (String string : idsArr) {
				if (!Utils.isEmpty(string)) {
					appids.add(Integer.parseInt(string));
				}
			}
			
			String[] appLimitArr = appLimit.split(",");
			List<Integer> appLimits = new ArrayList<Integer>();
			for (String string : appLimitArr) {
				if (!Utils.isEmpty(string)) {
					appLimits.add(Integer.parseInt(string));
				} else {
					appLimits.add(-1);
				}
			}
//			String[] appTodayArr = appToday.split(",");
//			List<Integer> appTodays = new ArrayList<Integer>();
//			for (String string : appTodayArr) {
//				if (!Utils.isEmpty(string)) {
//					appTodays.add(Integer.parseInt(string));
//				} else {
//					appTodays.add(-1);
//				}
//			}
			
			TOpenSeller tOpenSeller = openSellerManager.getEntity(sellerId);
			openSellerManager.deleteByProperty(tOpenSeller);//删除关联
			
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取当日时间区间
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			List<TOpenSellerApps> sellerApps = Lists.newArrayList();
			for (int i = 0; i < appids.size(); i++) {
				TOpenSellerApps tOpenSellerApp = new TOpenSellerApps();
				tOpenSellerApp.setOpenSeller(tOpenSeller);
				TOpenApp tOpenApp = openAppManager.getEntity(appids.get(i));
				tOpenSellerApp.setOpenApp(tOpenApp);
				tOpenSellerApp.setAppLimit(appLimits.get(i));
//				tOpenSellerApp.setAppToday(appTodays.get(i));
				//查询该渠道该app今日的成功量
				long today = openOrderManager.getOrderFee(sellerId, appids.get(i), start, end);
				tOpenSellerApp.setAppToday((int)today);
				sellerApps.add(tOpenSellerApp);
			}
			tOpenSeller.setSellerApps(sellerApps);
			
			openSellerManager.save(tOpenSeller);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
		}
	}
	
	public void report() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		
		try {
			java.sql.Date start = null;
			java.sql.Date end = null;
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				start = new java.sql.Date(sdf.parse(startTime).getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(endTime));
				calendar.add(Calendar.DATE, 1);
				end = new java.sql.Date(calendar.getTimeInMillis());
			} else {
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//获取当日时间区间
				SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
				String startString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date startDate = sdfSql.parse(startString);
				start = new java.sql.Date(startDate.getTime());
				
				calendar.add(Calendar.DATE, 1);
				String endString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date endDate = sdfSql.parse(endString);
				end = new java.sql.Date(endDate.getTime());
			}
			
			//mapreduce
			Map<Integer, String> noPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
			Map<Integer, String> succPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
			Map<Integer, String> failPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
			Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
			allStatusMap.putAll(noPayMap);
			allStatusMap.putAll(succPayMap);
			allStatusMap.putAll(failPayMap);
			Map<Integer, String> succPayReduceMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
			
			JSONArray jsonArray = new JSONArray();
			for (Integer appId : allStatusMap.keySet()) {
				Integer noPayInt = null;
				if (noPayMap.size() == 0) {
					noPayInt = 0;
				} else {
					JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
					noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
					if (noPayInt == null) {
						noPayInt = 0;
					}
				}
				Integer failInt = null;
				if (failPayMap.size() == 0) {
					failInt = 0;
				} else {
					JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
					failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
					if (failInt == null) {
						failInt = 0;
					}
				}
				Integer succInt = null;
				Integer feeInt = null;
				if (succPayMap.size() == 0) {
					succInt = 0;
					feeInt = 0;
				} else {						
					JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
					succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
					if (succInt == null) {
						succInt = 0;
					}
					feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
					if (feeInt == null) {
						feeInt = 0;
					}
					feeInt = feeInt/100;//fee转化成单位元
				}
				Integer succReduceInt = null;
				Integer feeReduceInt = null;
				if (succPayReduceMap.size() == 0) {
					succReduceInt = 0;
					feeReduceInt = 0;
				} else {						
					JSONObject succPayJson = JSONObject.parseObject(succPayReduceMap.get(appId));
					succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数（扣量后）
					if (succReduceInt == null) {
						succReduceInt = 0;
					}
					feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额（扣量后）
					if (feeReduceInt == null) {
						feeReduceInt = 0;
					}
					feeReduceInt = feeReduceInt/100;//fee转化成单位元
				}
				
				Integer orderReqInt = noPayInt+failInt+succInt;
				Long users_num = openOrderManager.mapReduceUserCount(sellerId, appId, start, end);
				Long users_succ_num = openOrderManager.mapReduceSuccUserCount(sellerId, appId, start, end);
				
				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
				//mr/mo转化率
				float f = 0;
				if (succInt == 0) {
					f = 0;
				} else {
					f = (float)succInt/(succInt+failInt);
				}
				if (f != 0) {
					f = (float)(Math.round(f*1000))/1000;
				}
				String fString = "0%";
				if (f == 0) {
					fString = "0%";
				} else {
					fString = df.format(f*100) + "%";//返回的是String类型的
				}
				//mr/req请求转化率
				float reqf = 0;
				if (succInt == 0) {
					reqf = 0;
				} else {
					reqf = (float)succInt/orderReqInt;
				}
				if (reqf != 0) {
					reqf = (float)(Math.round(reqf*1000))/1000;
				}
				String reqfString = "0%";
				if (reqf == 0) {
					reqfString = "0%";
				} else {
					reqfString = df.format(reqf*100) + "%";//返回的是String类型的
				}
				
				TOpenApp tOpenApp = openAppManager.get(appId);
				String appName = tOpenApp.getName();
				
				JSONObject report = new JSONObject();
				report.put("appName", appName);
				report.put("req", orderReqInt);
				report.put("succ", succInt);
				report.put("succReduce", succReduceInt);
				report.put("fail", failInt);
				report.put("noPay", noPayInt);
				report.put("fee", feeInt);
				report.put("feeReduce", feeReduceInt);
				report.put("users_num", users_num);
				report.put("users_succ_num", users_succ_num);
				report.put("rate", fString);
				report.put("reqRate", reqfString);
				jsonArray.add(report);
			}//end for map
			
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + jsonArray.size() + ",");
			jstr.append("report:");
			jstr.append(jsonArray.toJSONString());
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}
	
}
