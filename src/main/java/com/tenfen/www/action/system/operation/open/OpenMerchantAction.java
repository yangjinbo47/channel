package com.tenfen.www.action.system.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.open.OpenMerchantManager;

public class OpenMerchantAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenMerchantManager openMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String merchantName = ServletRequestUtils.getStringParameter(request, "merchantName", null);

		Page<TOpenMerchant> merchantPage = new Page<TOpenMerchant>();
		//设置默认排序方式
		merchantPage.setPageSize(limit);
		merchantPage.setPageNo(page);
		if (!merchantPage.isOrderBySetted()) {
			merchantPage.setOrderBy("id");
			merchantPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(merchantName)) {
			merchantPage = openMerchantManager.findMerchantPage(merchantPage);
		} else {
			merchantPage = openMerchantManager.findMerchantPage(merchantName, merchantPage);
		}
		
		long nums = merchantPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("merchants:");

		List<TOpenMerchant> merchantList = merchantPage.getResult();
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
				TOpenMerchant tOpenMerchant = new TOpenMerchant();
				tOpenMerchant.setMerchantName(merchantName);
				tOpenMerchant.setEmail(email);
				tOpenMerchant.setContact(contact);
				tOpenMerchant.setTelephone(telephone);
				tOpenMerchant.setJoinType(joinType);
				openMerchantManager.save(tOpenMerchant);
			} else {//更新
				TOpenMerchant tOpenMerchant = openMerchantManager.get(id);
				if (tOpenMerchant != null) {
					tOpenMerchant.setMerchantName(merchantName);
					tOpenMerchant.setEmail(email);
					tOpenMerchant.setContact(contact);
					tOpenMerchant.setTelephone(telephone);
					tOpenMerchant.setJoinType(joinType);
					openMerchantManager.save(tOpenMerchant);
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
					openMerchantManager.delete(Integer.parseInt(id));
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			logger.error(e.getMessage(), e);
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
