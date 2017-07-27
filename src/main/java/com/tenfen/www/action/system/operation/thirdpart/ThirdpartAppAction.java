package com.tenfen.www.action.system.operation.thirdpart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.thirdpart.TThirdApp;
import com.tenfen.entity.operation.thirdpart.TThirdMerchant;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.thirdpart.ThirdpartAppManager;
import com.tenfen.www.service.operation.thirdpart.ThirdpartMerchantManager;

public class ThirdpartAppAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private ThirdpartAppManager thirdpartAppManager;
	@Autowired
	private ThirdpartMerchantManager thirdpartMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String appName = ServletRequestUtils.getStringParameter(request, "appName", null);

		Page<TThirdApp> appPage = new Page<TThirdApp>();
		//设置默认排序方式
		appPage.setPageSize(limit);
		appPage.setPageNo(page);
		if (!appPage.isOrderBySetted()) {
			appPage.setOrderBy("id");
			appPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(appName)) {
			appPage = thirdpartAppManager.findAppPage(appPage);
		} else {
			appPage = thirdpartAppManager.findAppPage(appName, appPage);
		}
		
		long nums = appPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("apps:");

		List<TThirdApp> appList = appPage.getResult();
		JSONArray jsonArray = new JSONArray();
		for (TThirdApp tThirdApp : appList) {
			JSONObject json = new JSONObject();
			json.put("id", tThirdApp.getId());
			json.put("name", tThirdApp.getName());
			json.put("merchantId", tThirdApp.getMerchantId());
			json.put("thirdAppId", tThirdApp.getThirdAppId());
			json.put("thirdAppMch", tThirdApp.getThirdAppMch());
			json.put("thirdAppSecret", tThirdApp.getThirdAppSecret());
			json.put("callbackUrl", tThirdApp.getCallbackUrl());
			TThirdMerchant tThirdMerchant = thirdpartMerchantManager.get(tThirdApp.getMerchantId());
			json.put("merchantShowName", tThirdMerchant.getMerchantShowName());
			jsonArray.add(json);
		}
		jstr.append(jsonArray.toString());
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String appName = ServletRequestUtils.getStringParameter(request, "name", null);
			Integer merchantId = ServletRequestUtils.getIntParameter(request, "merchantId");
			String thirdAppId = ServletRequestUtils.getStringParameter(request, "thirdAppId", null);
			String thirdAppMch = ServletRequestUtils.getStringParameter(request, "thirdAppMch", null);
			String thirdAppSecret = ServletRequestUtils.getStringParameter(request, "thirdAppSecret", null);
			String callbackUrl = ServletRequestUtils.getStringParameter(request, "callbackUrl", null);
			
			if (id == -1) {
				TThirdApp tThirdApp = new TThirdApp();
				tThirdApp.setName(appName);
				tThirdApp.setMerchantId(merchantId);
				tThirdApp.setThirdAppId(thirdAppId);
				tThirdApp.setThirdAppMch(thirdAppMch);
				tThirdApp.setThirdAppSecret(thirdAppSecret);
				tThirdApp.setCallbackUrl(callbackUrl);
				thirdpartAppManager.save(tThirdApp);
			} else {//更新
				TThirdApp tThirdApp = thirdpartAppManager.getEntity(id);
				if (tThirdApp != null) {
					tThirdApp.setName(appName);
					tThirdApp.setMerchantId(merchantId);
					tThirdApp.setThirdAppId(thirdAppId);
					tThirdApp.setThirdAppMch(thirdAppMch);
					tThirdApp.setThirdAppSecret(thirdAppSecret);
					tThirdApp.setCallbackUrl(callbackUrl);
					thirdpartAppManager.save(tThirdApp);
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
					thirdpartAppManager.delete(Integer.parseInt(id));
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
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
