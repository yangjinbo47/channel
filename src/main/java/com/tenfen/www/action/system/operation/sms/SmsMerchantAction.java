package com.tenfen.www.action.system.operation.sms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.sms.TSmsMerchant;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.sms.SmsMerchantManager;

public class SmsMerchantAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsMerchantManager smsMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String merchantName = ServletRequestUtils.getStringParameter(request, "merchantName", null);

		Page<TSmsMerchant> merchantPage = new Page<TSmsMerchant>();
		//设置默认排序方式
		merchantPage.setPageSize(limit);
		merchantPage.setPageNo(page);
		if (!merchantPage.isOrderBySetted()) {
			merchantPage.setOrderBy("id");
			merchantPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(merchantName)) {
			merchantPage = smsMerchantManager.findMerchantPage(merchantPage);
		} else {
			merchantPage = smsMerchantManager.findMerchantPage(merchantName, merchantPage);
		}
		
		long nums = merchantPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("merchants:");

		List<TSmsMerchant> merchantList = merchantPage.getResult();
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
				TSmsMerchant tSmsMerchant = new TSmsMerchant();
				tSmsMerchant.setMerchantName(merchantName);
				tSmsMerchant.setEmail(email);
				tSmsMerchant.setContact(contact);
				tSmsMerchant.setTelephone(telephone);
				tSmsMerchant.setJoinType(joinType);
				smsMerchantManager.save(tSmsMerchant);
			} else {//更新
				TSmsMerchant tSmsMerchant = smsMerchantManager.get(id);
				if (tSmsMerchant != null) {
					tSmsMerchant.setMerchantName(merchantName);
					tSmsMerchant.setEmail(email);
					tSmsMerchant.setContact(contact);
					tSmsMerchant.setTelephone(telephone);
					tSmsMerchant.setJoinType(joinType);
					smsMerchantManager.save(tSmsMerchant);
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
					smsMerchantManager.delete(Integer.parseInt(id));
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
