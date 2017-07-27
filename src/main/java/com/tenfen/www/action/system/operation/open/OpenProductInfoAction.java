package com.tenfen.www.action.system.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.open.OpenMerchantManager;
import com.tenfen.www.service.operation.open.OpenProductInfoManager;

public class OpenProductInfoAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenProductInfoManager openProductInfoManager;
	@Autowired
	private OpenMerchantManager openMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String productName = ServletRequestUtils.getStringParameter(request, "productName", null);

		Page<TOpenProductInfo> productInfoPage = new Page<TOpenProductInfo>();
		//设置默认排序方式
		productInfoPage.setPageSize(limit);
		productInfoPage.setPageNo(page);
		if (!productInfoPage.isOrderBySetted()) {
			productInfoPage.setOrderBy("id");
			productInfoPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(productName)) {
			productInfoPage = openProductInfoManager.findProductPage(productInfoPage);
		} else {
			productInfoPage = openProductInfoManager.findProductPage(productName, productInfoPage);
		}
		
		long nums = productInfoPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("productInfos:");

		List<TOpenProductInfo> productInfoList = productInfoPage.getResult();
		for (TOpenProductInfo tOpenProductInfo : productInfoList) {
			TOpenMerchant tOpenMerchant = openMerchantManager.get(tOpenProductInfo.getMerchantId());
			tOpenProductInfo.setMerchantShowName(tOpenMerchant.getMerchantShowName());
		}
		jstr.append(JSON.toJSONString(productInfoList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);
			Integer price = ServletRequestUtils.getIntParameter(request, "price", -1);
			String code = ServletRequestUtils.getStringParameter(request, "code", null);
			String instruction = ServletRequestUtils.getStringParameter(request, "instruction", null);
			String productId = ServletRequestUtils.getStringParameter(request, "productId", null);
			Integer type = ServletRequestUtils.getIntParameter(request, "type", -1);
			Integer merchantId = ServletRequestUtils.getIntParameter(request, "merchantId", -1);

			if (id == -1) {
				TOpenProductInfo tOpenProductInfo = new TOpenProductInfo();
				tOpenProductInfo.setName(name);
				tOpenProductInfo.setPrice(price);
				tOpenProductInfo.setCode(code);
				tOpenProductInfo.setInstruction(instruction);
				tOpenProductInfo.setProductId(productId);
				tOpenProductInfo.setType(type);
				tOpenProductInfo.setMerchantId(merchantId);
				openProductInfoManager.save(tOpenProductInfo);
			} else {//更新
				TOpenProductInfo tOpenProductInfo = openProductInfoManager.get(id);
				if (tOpenProductInfo != null) {
					tOpenProductInfo.setName(name);
					tOpenProductInfo.setPrice(price);
					tOpenProductInfo.setCode(code);
					tOpenProductInfo.setInstruction(instruction);
					tOpenProductInfo.setProductId(productId);
					tOpenProductInfo.setType(type);
					tOpenProductInfo.setMerchantId(merchantId);
					openProductInfoManager.save(tOpenProductInfo);
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
					openProductInfoManager.delete(Integer.parseInt(id));
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
