package com.tenfen.www.action.system.operation.sms;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.entity.operation.sms.TSmsSellerLimit;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class SmsSellerAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String sellerName = ServletRequestUtils.getStringParameter(request, "sellerName", null);

		Page<TSmsSeller> sellerPage = new Page<TSmsSeller>();
		//设置默认排序方式
		sellerPage.setPageSize(limit);
		sellerPage.setPageNo(page);
		if (!sellerPage.isOrderBySetted()) {
			sellerPage.setOrderBy("id");
			sellerPage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(sellerName)) {
			sellerPage = smsSellerManager.findSellerPage(sellerPage, userType);
		} else {
			sellerPage = smsSellerManager.findSellerPage(sellerName, sellerPage, userType);
		}
		
		long nums = sellerPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("sellers:");

		List<TSmsSeller> sellerList = sellerPage.getResult();
		JSONArray jsonArray = new JSONArray();
		for (TSmsSeller tSmsSeller : sellerList) {
			JSONObject json = new JSONObject();
			json.put("id", tSmsSeller.getId());
			json.put("name", tSmsSeller.getName());
			json.put("email", tSmsSeller.getEmail());
			json.put("contact", tSmsSeller.getContact());
			json.put("telephone", tSmsSeller.getTelephone());
			json.put("sellerKey", tSmsSeller.getSellerKey());
			json.put("sellerSecret", tSmsSeller.getSellerSecret());
			json.put("callbackUrl", tSmsSeller.getCallbackUrl());
			json.put("status", tSmsSeller.getStatus());
			json.put("companyShow", tSmsSeller.getCompanyShow());
			jsonArray.add(json);
		}
		jstr.append(jsonArray.toString());
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void treelist() {
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		List<TSmsSeller> sellerTmpList = smsSellerManager.findAllSmsSellerList(userType);
		
		List<TSmsSeller> sellerList = new ArrayList<TSmsSeller>();
		for (TSmsSeller tSmsSeller : sellerTmpList) {
			TSmsSeller smsSeller = new TSmsSeller();
			smsSeller.setId(tSmsSeller.getId());
			smsSeller.setName(tSmsSeller.getName());
			sellerList.add(smsSeller);
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
				TSmsSeller tSmsSeller = new TSmsSeller();
				tSmsSeller.setName(name);
				tSmsSeller.setEmail(email);
				tSmsSeller.setContact(contact);
				tSmsSeller.setTelephone(telephone);
				tSmsSeller.setSellerKey(sellerKey);
				tSmsSeller.setSellerSecret(sellerSecret);
				tSmsSeller.setCallbackUrl(callbackUrl);
				tSmsSeller.setStatus(status);
				tSmsSeller.setCompanyShow(companyShow);
				smsSellerManager.save(tSmsSeller);
			} else {//更新
				TSmsSeller tSmsSeller = smsSellerManager.getEntity(id);
				if (tSmsSeller != null) {
					tSmsSeller.setName(name);
					tSmsSeller.setEmail(email);
					tSmsSeller.setContact(contact);
					tSmsSeller.setTelephone(telephone);
					tSmsSeller.setSellerKey(sellerKey);
					tSmsSeller.setSellerSecret(sellerSecret);
					tSmsSeller.setCallbackUrl(callbackUrl);
					tSmsSeller.setStatus(status);
					tSmsSeller.setCompanyShow(companyShow);
					smsSellerManager.save(tSmsSeller);
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
					TSmsSeller tSmsSeller = smsSellerManager.getEntity(Integer.parseInt(id));
					List<TSmsSellerApps> list = tSmsSeller.getSellerApps();
					for (TSmsSellerApps tSmsSellerApps : list) {
						tSmsSellerApps.setSmsApp(null);
					}
					smsSellerManager.delete(tSmsSeller);
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

		TSmsSeller tSmsSeller = smsSellerManager.getEntity(sellerId);
		
		List<TSmsApp> tSmsApps = new ArrayList<TSmsApp>();
		List<TSmsSellerApps> tSmsSellerAppList = tSmsSeller.getSellerApps();
		for (TSmsSellerApps tSmsSellerApps : tSmsSellerAppList) {
			TSmsApp tSmsApp = new TSmsApp();
			tSmsApp.setId(tSmsSellerApps.getSmsApp().getId());
			tSmsApp.setName(tSmsSellerApps.getSmsApp().getName());
			tSmsApp.setAppLimit(tSmsSellerApps.getAppLimit());
			tSmsApp.setAppToday(tSmsSellerApps.getAppToday());
			tSmsApps.add(tSmsApp);
		}
		int size = tSmsApps.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("apps:");
		jstr.append(JSON.toJSONString(tSmsApps));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给渠道的app
	 */
	public void getUnallocateApps() {
		String appIds = ServletRequestUtils.getStringParameter(request, "appIds", "");
		
		String[] idsArr = appIds.split(",");
		List<TSmsApp> apps = new ArrayList<TSmsApp>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		TSmsApp tSmsApp = smsAppManager.getEntity(Integer.parseInt(string));
        		apps.add(tSmsApp);
			}
		}
		
		List<TSmsApp> appsAll = smsAppManager.getAll();
		appsAll.removeAll(apps);
		
		int size = appsAll.size();
		List<TSmsApp> appsAllList = new ArrayList<TSmsApp>();
		for (TSmsApp tSmsApp : appsAll) {
			TSmsApp tSmsAppNew = new TSmsApp();
			tSmsAppNew.setId(tSmsApp.getId());
			tSmsAppNew.setName(tSmsApp.getName());
			tSmsAppNew.setAppLimit(-1);
			tSmsAppNew.setAppToday(0);
			appsAllList.add(tSmsAppNew);
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
			
			TSmsSeller tSmsSeller = smsSellerManager.getEntity(sellerId);
			smsSellerManager.deleteByProperty(tSmsSeller);//删除关联
			
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
			
			List<TSmsSellerApps> sellerApps = Lists.newArrayList();
			for (int i = 0; i < appids.size(); i++) {
				TSmsSellerApps tSmsSellerApp = new TSmsSellerApps();
				tSmsSellerApp.setSmsSeller(tSmsSeller);
				TSmsApp tSmsApp = smsAppManager.getEntity(appids.get(i));
				tSmsSellerApp.setSmsApp(tSmsApp);
				tSmsSellerApp.setAppLimit(appLimits.get(i));
				//查询该渠道该app今日的成功量
				long today = smsOrderManager.getOrderFee(sellerId, appids.get(i), start, end);
				tSmsSellerApp.setAppToday((int)today);
				sellerApps.add(tSmsSellerApp);
			}
			tSmsSeller.setSellerApps(sellerApps);
			
			smsSellerManager.save(tSmsSeller);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
		}
	}
	
	private final List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
			"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆"});
	/**
	 * 获取渠道省份日月限
	 */
	public void limitlist() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);

		List<TSmsSellerLimit> limits = smsSellerManager.findSellerLimits(sellerId);
		if (limits.size() == 0) {
			limits = new ArrayList<TSmsSellerLimit>();
			for (String prov : provinceList) {
				TSmsSellerLimit tSmsSellerLimit = new TSmsSellerLimit();
				tSmsSellerLimit.setSellerId(sellerId);
				tSmsSellerLimit.setProvince(prov);
				tSmsSellerLimit.setDayLimit(-1);
				tSmsSellerLimit.setMonthLimit(-1);
				limits.add(tSmsSellerLimit);
			}
		}
		long nums = limits.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("sellerLimits:");

		jstr.append(JSON.toJSONString(limits));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveSellerLimit() {
		String limitStr = ServletRequestUtils.getStringParameter(request, "limits", "");
		try {
			List<TSmsSellerLimit> limits = JSON.parseArray(limitStr, TSmsSellerLimit.class);
			for (TSmsSellerLimit tSmsSellerLimit : limits) {
				//查找存在对象
				TSmsSellerLimit data = smsSellerManager.findSellerLimitByProperty(tSmsSellerLimit.getSellerId(), tSmsSellerLimit.getProvince());
				if (!Utils.isEmpty(data)) {
					data.setSellerId(tSmsSellerLimit.getSellerId());
					data.setProvince(tSmsSellerLimit.getProvince());
					data.setDayLimit(tSmsSellerLimit.getDayLimit());
					data.setMonthLimit(tSmsSellerLimit.getMonthLimit());
					smsSellerManager.saveSellerLimit(data);
				} else {					
					smsSellerManager.saveSellerLimit(tSmsSellerLimit);
				}
			}
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.success(MSG.SAVEFAILURE));
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
			Map<Integer, String> noPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
			Map<Integer, String> succPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
			Map<Integer, String> failPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
			Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
			allStatusMap.putAll(noPayMap);
			allStatusMap.putAll(succPayMap);
			allStatusMap.putAll(failPayMap);
//			Map<Integer, String> succPayReduceMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
			
			JSONArray jsonArray = new JSONArray();
			for (Integer appId : allStatusMap.keySet()) {
				Integer noPayInt = null;
				Integer noPayUserInt = null;
				if (noPayMap.size() == 0) {
					noPayInt = 0;
					noPayUserInt = 0;
				} else {
					JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
					noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
					if (noPayInt == null) {
						noPayInt = 0;
					}
					noPayUserInt = noPayJson == null ? 0 : noPayJson.getInteger("user");//未支付用户数
					if (noPayUserInt == null) {
						noPayUserInt = 0;
					}
				}
				Integer failInt = null;
				Integer failUserInt = null;
				if (failPayMap.size() == 0) {
					failInt = 0;
					failUserInt = 0;
				} else {
					JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
					failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
					if (failInt == null) {
						failInt = 0;
					}
					failUserInt = failPayJson == null ? 0 : failPayJson.getInteger("user");//失败用户数
					if (failUserInt == null) {
						failUserInt = 0;
					}
				}
				Integer succInt = null;
				Integer succUserInt = null;
				Integer feeInt = null;
				Integer succReduceInt = null;
				Integer feeReduceInt = null;
				if (succPayMap.size() == 0) {
					succInt = 0;
					succUserInt = 0;
					feeInt = 0;
					succReduceInt = 0;
					feeReduceInt = 0;
				} else {
					JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
					succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
					if (succInt == null) {
						succInt = 0;
					}
					succUserInt = succPayJson == null ? 0 : succPayJson.getInteger("user");//成功用户数
					if (succUserInt == null) {
						succUserInt = 0;
					}
					feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
					if (feeInt == null) {
						feeInt = 0;
					}
					succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("countReduce");//成功支付请求数(扣)
					if (succReduceInt == null) {
						succReduceInt = 0;
					}
					feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("feeReduce");//成功计费金额
					if (feeReduceInt == null) {
						feeReduceInt = 0;
					}
					feeInt = feeInt/100;//fee转化成单位元
					feeReduceInt = feeReduceInt/100;
				}
				
				Integer orderReqInt = noPayInt+failInt+succInt;
				Integer users_num = noPayUserInt + failUserInt + succUserInt;
				Integer users_succ_num = succUserInt;
				
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
				
				TSmsApp tSmsApp = smsAppManager.get(appId);
				String appName = tSmsApp.getName();
				
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
