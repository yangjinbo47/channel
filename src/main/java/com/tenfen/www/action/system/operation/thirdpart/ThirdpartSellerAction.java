package com.tenfen.www.action.system.operation.thirdpart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.google.common.collect.Lists;
import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.entity.operation.thirdpart.TThirdSeller;
import com.tenfen.entity.operation.thirdpart.TThirdSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.thirdpart.ThirdpartAppManager;
import com.tenfen.www.service.operation.thirdpart.ThirdpartOrderManager;
import com.tenfen.www.service.operation.thirdpart.ThirdpartSellerManager;

public class ThirdpartSellerAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private ThirdpartSellerManager thirdpartSellerManager;
	@Autowired
	private ThirdpartAppManager thirdpartAppManager;
	@Autowired
	private ThirdpartOrderManager thirdpartOrderManager;
	
	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
	}
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String sellerName = ServletRequestUtils.getStringParameter(request, "sellerName", null);

		Page<TThirdSeller> sellerPage = new Page<TThirdSeller>();
		//设置默认排序方式
		sellerPage.setPageSize(limit);
		sellerPage.setPageNo(page);
		if (!sellerPage.isOrderBySetted()) {
			sellerPage.setOrderBy("id");
			sellerPage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(sellerName)) {
			sellerPage = thirdpartSellerManager.findSellerPage(sellerPage, userType);
		} else {
			sellerPage = thirdpartSellerManager.findSellerPage(sellerName, sellerPage, userType);
		}
		
		long nums = sellerPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("sellers:");

		List<TThirdSeller> sellerList = sellerPage.getResult();
//		JSONArray jsonArray = new JSONArray();
//		for (TThirdSeller tThirdSeller : sellerList) {
//			JSONObject json = new JSONObject();
//			json.put("id", tThirdSeller.getId());
//			json.put("name", tThirdSeller.getName());
//			json.put("email", tThirdSeller.getEmail());
//			json.put("contact", tThirdSeller.getContact());
//			json.put("telephone", tThirdSeller.getTelephone());
//			json.put("sellerKey", tThirdSeller.getSellerKey());
//			json.put("sellerSecret", tThirdSeller.getSellerSecret());
//			json.put("callbackUrl", tThirdSeller.getCallbackUrl());
//			json.put("status", tThirdSeller.getStatus());
//			jsonArray.add(json);
//		}
		String jsonArrayString = JSON.toJSONString(sellerList, config);
		jstr.append(jsonArrayString);
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void treelist() {
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		List<TThirdSeller> sellerTmpList = thirdpartSellerManager.findAllThirdSellerList(userType);
		
		List<TThirdSeller> sellerList = new ArrayList<TThirdSeller>();
		for (TThirdSeller tThirdSeller : sellerTmpList) {
			TThirdSeller thirdSeller = new TThirdSeller();
			thirdSeller.setId(tThirdSeller.getId());
			thirdSeller.setName(tThirdSeller.getName());
			sellerList.add(thirdSeller);
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

			if (id == -1) {
				TThirdSeller tThirdSeller = new TThirdSeller();
				tThirdSeller.setName(name);
				tThirdSeller.setEmail(email);
				tThirdSeller.setContact(contact);
				tThirdSeller.setTelephone(telephone);
				tThirdSeller.setSellerKey(sellerKey);
				tThirdSeller.setSellerSecret(sellerSecret);
				tThirdSeller.setCallbackUrl(callbackUrl);
				tThirdSeller.setStatus(status);
				thirdpartSellerManager.save(tThirdSeller);
			} else {//更新
				TThirdSeller tThirdSeller = thirdpartSellerManager.getEntity(id);
				if (tThirdSeller != null) {
					tThirdSeller.setName(name);
					tThirdSeller.setEmail(email);
					tThirdSeller.setContact(contact);
					tThirdSeller.setTelephone(telephone);
					tThirdSeller.setSellerKey(sellerKey);
					tThirdSeller.setSellerSecret(sellerSecret);
					tThirdSeller.setCallbackUrl(callbackUrl);
					tThirdSeller.setStatus(status);
					thirdpartSellerManager.save(tThirdSeller);
				}
			}

			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
			LogUtil.error(e.getMessage(),e);
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
					TThirdSeller tThirdSeller = thirdpartSellerManager.getEntity(Integer.parseInt(id));
					List<TThirdSellerApps> list = tThirdSeller.getSellerApps();
					for (TThirdSellerApps tThirdSellerApps : list) {
						tThirdSellerApps.setThirdApp(null);
					}
					thirdpartSellerManager.delete(tThirdSeller);
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

		TThirdSeller tThirdSeller = thirdpartSellerManager.getEntity(sellerId);
		
		List<TThirdApp> tThirdApps = new ArrayList<TThirdApp>();
		List<TThirdSellerApps> tThirdSellerAppList = tThirdSeller.getSellerApps();
		for (TThirdSellerApps tThirdSellerApps : tThirdSellerAppList) {
			TThirdApp tThirdApp = new TThirdApp();
			tThirdApp.setId(tThirdSellerApps.getThirdApp().getId());
			tThirdApp.setName(tThirdSellerApps.getThirdApp().getName());
			tThirdApp.setAppLimit(tThirdSellerApps.getAppLimit());
			tThirdApp.setAppToday(tThirdSellerApps.getAppToday());
			tThirdApps.add(tThirdApp);
		}
		int size = tThirdApps.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("apps:");
		jstr.append(JSON.toJSONString(tThirdApps));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给渠道的app
	 */
	public void getUnallocateApps() {
		String appIds = ServletRequestUtils.getStringParameter(request, "appIds", "");
		
		String[] idsArr = appIds.split(",");
		List<TThirdApp> apps = new ArrayList<TThirdApp>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		TThirdApp tThirdApp = thirdpartAppManager.getEntity(Integer.parseInt(string));
        		apps.add(tThirdApp);
			}
		}
		
		List<TThirdApp> appsAll = thirdpartAppManager.getAll();
		appsAll.removeAll(apps);
		
		int size = appsAll.size();
		List<TThirdApp> appsAllList = new ArrayList<TThirdApp>();
		for (TThirdApp tThirdApp : appsAll) {
			TThirdApp tThirdAppNew = new TThirdApp();
			tThirdAppNew.setId(tThirdApp.getId());
			tThirdAppNew.setName(tThirdApp.getName());
			tThirdAppNew.setAppLimit(-1);
			tThirdAppNew.setAppToday(0);
			appsAllList.add(tThirdAppNew);
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
			
			TThirdSeller tThirdSeller = thirdpartSellerManager.getEntity(sellerId);
			thirdpartSellerManager.deleteByProperty(tThirdSeller);//删除关联
			
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
			
			List<TThirdSellerApps> sellerApps = Lists.newArrayList();
			for (int i = 0; i < appids.size(); i++) {
				TThirdSellerApps tThirdSellerApp = new TThirdSellerApps();
				tThirdSellerApp.setThirdSeller(tThirdSeller);
				TThirdApp tThirdApp = thirdpartAppManager.getEntity(appids.get(i));
				tThirdSellerApp.setThirdApp(tThirdApp);
				tThirdSellerApp.setAppLimit(appLimits.get(i));
				//查询该渠道该app今日的成功量
				long today = thirdpartOrderManager.getOrderFee(sellerId, appids.get(i), start, end);
				tThirdSellerApp.setAppToday((int)today);
				sellerApps.add(tThirdSellerApp);
			}
			tThirdSeller.setSellerApps(sellerApps);
			
			thirdpartSellerManager.save(tThirdSeller);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
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
