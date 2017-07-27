package com.tenfen.www.action.system.operation.thirdpart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.thirdpart.TThirdMerchant;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.thirdpart.ThirdpartMerchantManager;

public class ThirdpartMerchantAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private ThirdpartMerchantManager thirdpartMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String merchantName = ServletRequestUtils.getStringParameter(request, "merchantName", null);

		Page<TThirdMerchant> merchantPage = new Page<TThirdMerchant>();
		//设置默认排序方式
		merchantPage.setPageSize(limit);
		merchantPage.setPageNo(page);
		if (!merchantPage.isOrderBySetted()) {
			merchantPage.setOrderBy("id");
			merchantPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(merchantName)) {
			merchantPage = thirdpartMerchantManager.findMerchantPage(merchantPage);
		} else {
			merchantPage = thirdpartMerchantManager.findMerchantPage(merchantName, merchantPage);
		}
		
		long nums = merchantPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("merchants:");

		List<TThirdMerchant> merchantList = merchantPage.getResult();
		jstr.append(JSON.toJSONString(merchantList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String merchantName = ServletRequestUtils.getStringParameter(request, "merchantName", null);
			String email = ServletRequestUtils.getStringParameter(request, "email", null);
			String contact = ServletRequestUtils.getStringParameter(request, "contact", null);
			String telephone = ServletRequestUtils.getStringParameter(request, "telephone", null);
			Integer joinType = ServletRequestUtils.getIntParameter(request, "joinType", -1);

			if (id == -1) {
				TThirdMerchant tThirdMerchant = new TThirdMerchant();
				tThirdMerchant.setMerchantName(merchantName);
				tThirdMerchant.setEmail(email);
				tThirdMerchant.setContact(contact);
				tThirdMerchant.setTelephone(telephone);
				tThirdMerchant.setJoinType(joinType);
				thirdpartMerchantManager.save(tThirdMerchant);
			} else {//更新
				TThirdMerchant tThirdMerchant = thirdpartMerchantManager.get(id);
				if (tThirdMerchant != null) {
					tThirdMerchant.setMerchantName(merchantName);
					tThirdMerchant.setEmail(email);
					tThirdMerchant.setContact(contact);
					tThirdMerchant.setTelephone(telephone);
					tThirdMerchant.setJoinType(joinType);
					thirdpartMerchantManager.save(tThirdMerchant);
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
					thirdpartMerchantManager.delete(Integer.parseInt(id));
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
