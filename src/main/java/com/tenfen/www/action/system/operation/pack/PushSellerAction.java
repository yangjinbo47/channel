package com.tenfen.www.action.system.operation.pack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.entity.operation.pack.TPushSellerPackages;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class PushSellerAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private PushSellerManager pushSellerManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private CacheFactory cacheFactory;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String sellerName = ServletRequestUtils.getStringParameter(request, "sellerName", null);

		Page<TPushSeller> sellerPage = new Page<TPushSeller>();
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
			sellerPage = pushSellerManager.findSellerPage(sellerPage, userType);
		} else {
			sellerPage = pushSellerManager.findSellerPage(sellerName, sellerPage, userType);
		}
		
		long nums = sellerPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("sellers:");

		List<TPushSeller> sellerList = sellerPage.getResult();
		JSONArray jsonArray = new JSONArray();
		for (TPushSeller tPushSeller : sellerList) {
			JSONObject json = new JSONObject();
			json.put("id", tPushSeller.getId());
			json.put("name", tPushSeller.getName());
			json.put("sellerKey", tPushSeller.getSellerKey());
			json.put("sellerSecret", tPushSeller.getSellerSecret());
			json.put("callbackUrl", tPushSeller.getCallbackUrl());
			json.put("status", tPushSeller.getStatus());
			json.put("companyShow", tPushSeller.getCompanyShow());
			jsonArray.add(json);
		}
		jstr.append(jsonArray.toString());
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void treelist() {
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
//		Integer userType = (Integer) getMemcacheAttribute(Constants.OPERATOR_TYPE);
		List<TPushSeller> sellerTmpList = pushSellerManager.findAllPushSellerList(userType);
		
		List<TPushSeller> sellerList = new ArrayList<TPushSeller>();
		for (TPushSeller tPushSeller : sellerTmpList) {
			TPushSeller pushSeller = new TPushSeller();
			pushSeller.setId(tPushSeller.getId());
			pushSeller.setName(tPushSeller.getName());
			sellerList.add(pushSeller);
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
			String sellerKey = ServletRequestUtils.getStringParameter(request, "sellerKey", null);
			String sellerSecret = ServletRequestUtils.getStringParameter(request, "sellerSecret", null);
			String callbackUrl = ServletRequestUtils.getStringParameter(request, "callbackUrl", null);
			Integer status = ServletRequestUtils.getIntParameter(request, "status", -1);
			Integer companyShow = ServletRequestUtils.getIntParameter(request, "companyShow", -1);

			if (id == -1) {
				TPushSeller tPushSeller = new TPushSeller();
				tPushSeller.setName(name);
				tPushSeller.setSellerKey(sellerKey);
				tPushSeller.setSellerSecret(sellerSecret);
				tPushSeller.setCallbackUrl(callbackUrl);
				tPushSeller.setStatus(status);
				tPushSeller.setCompanyShow(companyShow);
				pushSellerManager.save(tPushSeller);
			} else {//更新
				TPushSeller tPushSeller = pushSellerManager.getEntity(id);
				if (tPushSeller != null) {
					tPushSeller.setName(name);
					tPushSeller.setSellerKey(sellerKey);
					tPushSeller.setSellerSecret(sellerSecret);
					tPushSeller.setCallbackUrl(callbackUrl);
					tPushSeller.setStatus(status);
					tPushSeller.setCompanyShow(companyShow);
					pushSellerManager.save(tPushSeller);
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
					TPushSeller tPushSeller = pushSellerManager.getEntity(Integer.parseInt(id));
					List<TPushSellerPackages> list = tPushSeller.getSellerPackages();
					for (TPushSellerPackages tPushSellerPackages : list) {
						tPushSellerPackages.setPushPackage(null);
					}
					pushSellerManager.delete(tPushSeller);
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
	public void packagesOfSeller() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);

		TPushSeller tPushSeller = pushSellerManager.getEntity(sellerId);
		
		List<PushPackage> pushPackages = new ArrayList<PushPackage>();
		List<TPushSellerPackages> tPushSellerPackageList = tPushSeller.getSellerPackages();
		for (TPushSellerPackages tPushSellerPackages : tPushSellerPackageList) {
			int companyShow = tPushSellerPackages.getPushPackage().getCompanyShow();
			if (companyShow == userType || userType.equals(Constants.USER_TYPE.ALL.getValue())) {
				PushPackage pushPackage = new PushPackage();
				pushPackage.setId(tPushSellerPackages.getPushPackage().getId());
				pushPackage.setPackageName(tPushSellerPackages.getPushPackage().getPackageName());
				pushPackage.setPackageLimit(tPushSellerPackages.getPushPackage().getPackageLimit());
				pushPackage.setPackageToday(tPushSellerPackages.getPushPackage().getPackageToday());
				pushPackages.add(pushPackage);
			}
		}
		int size = pushPackages.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("packages:");
		jstr.append(JSON.toJSONString(pushPackages));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给渠道的app
	 */
	public void getUnallocatePackages() {
		String packageIds = ServletRequestUtils.getStringParameter(request, "packageIds", "");
		
		String[] idsArr = packageIds.split(",");
		List<PushPackage> packages = new ArrayList<PushPackage>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		PushPackage pushPackage = packageManager.get(Integer.parseInt(string));
        		packages.add(pushPackage);
			}
		}
		
        Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		List<PushPackage> packagesAll = packageManager.findAllPackageList(userType);
		packagesAll.removeAll(packages);
		
		int size = packagesAll.size();
		List<PushPackage> packagesAllList = new ArrayList<PushPackage>();
		for (PushPackage pushPackage : packagesAll) {
			PushPackage pushPackageNew = new PushPackage();
			pushPackageNew.setId(pushPackage.getId());
			pushPackageNew.setPackageName(pushPackage.getPackageName());
			pushPackageNew.setPackageLimit(-1);
			pushPackageNew.setPackageToday(0);
			packagesAllList.add(pushPackageNew);
		}
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("packages:");
		jstr.append(JSON.toJSONString(packagesAllList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveSellerPackageRelation() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		String packageIds = ServletRequestUtils.getStringParameter(request, "packageIds", "");
		String packageLimit = ServletRequestUtils.getStringParameter(request, "packageLimit", "");
//		String appToday = ServletRequestUtils.getStringParameter(request, "appToday", "");
		
		try {
			String[] idsArr = packageIds.split(",");
			List<Integer> packageids = new ArrayList<Integer>();
			for (String string : idsArr) {
				if (!Utils.isEmpty(string)) {
					packageids.add(Integer.parseInt(string));
				}
			}
			
			String[] packageLimitArr = packageLimit.split(",");
			List<Integer> packageLimits = new ArrayList<Integer>();
			for (String string : packageLimitArr) {
				if (!Utils.isEmpty(string)) {
					packageLimits.add(Integer.parseInt(string));
				} else {
					packageLimits.add(-1);
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
			
			TPushSeller tPushSeller = pushSellerManager.getEntity(sellerId);
			pushSellerManager.deleteByProperty(tPushSeller);//删除关联
			
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
			
			List<TPushSellerPackages> sellerPackages = Lists.newArrayList();
			for (int i = 0; i < packageids.size(); i++) {
				TPushSellerPackages tPushSellerPackages = new TPushSellerPackages();
				tPushSellerPackages.setPushSeller(tPushSeller);
				PushPackage pushPackage = packageManager.get(packageids.get(i));
				tPushSellerPackages.setPushPackage(pushPackage);
				tPushSellerPackages.setPackageLimit(packageLimits.get(i));
				//查询该渠道该app今日的成功量
				//需完善
//				long today = orderManager.getOrderFee(sellerId, packageids.get(i), start, end);
				tPushSellerPackages.setPackageToday(0);
				sellerPackages.add(tPushSellerPackages);
			}
			tPushSeller.setSellerPackages(sellerPackages);
			
			pushSellerManager.save(tPushSeller);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
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
