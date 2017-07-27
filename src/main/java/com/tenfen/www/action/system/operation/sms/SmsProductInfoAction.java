package com.tenfen.www.action.system.operation.sms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.sms.TSmsMerchant;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.sms.SmsMerchantManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;

public class SmsProductInfoAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private SmsMerchantManager smsMerchantManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String productName = ServletRequestUtils.getStringParameter(request, "productName", null);

		Page<TSmsProductInfo> productInfoPage = new Page<TSmsProductInfo>();
		//设置默认排序方式
		productInfoPage.setPageSize(limit);
		productInfoPage.setPageNo(page);
		if (!productInfoPage.isOrderBySetted()) {
			productInfoPage.setOrderBy("id");
			productInfoPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(productName)) {
			productInfoPage = smsProductInfoManager.findProductPage(productInfoPage);
		} else {
			productInfoPage = smsProductInfoManager.findProductPage(productName, productInfoPage);
		}
		
		long nums = productInfoPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("productInfos:");

		List<TSmsProductInfo> productInfoList = productInfoPage.getResult();
		for (TSmsProductInfo tSmsProductInfo : productInfoList) {
			TSmsMerchant tSmsMerchant = smsMerchantManager.get(tSmsProductInfo.getMerchantId());
			tSmsProductInfo.setMerchantShowName(tSmsMerchant.getMerchantShowName());
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
			String sendNumber = ServletRequestUtils.getStringParameter(request, "sendNumber", null);
			String instruction = ServletRequestUtils.getStringParameter(request, "instruction", null);
			String productId = ServletRequestUtils.getStringParameter(request, "productId", null);
			Integer type = ServletRequestUtils.getIntParameter(request, "type", -1);
			Integer merchantId = ServletRequestUtils.getIntParameter(request, "merchantId", -1);

			if (id == -1) {
				TSmsProductInfo tSmsProductInfo = new TSmsProductInfo();
				tSmsProductInfo.setName(name);
				tSmsProductInfo.setPrice(price);
				tSmsProductInfo.setSendNumber(sendNumber);
				tSmsProductInfo.setInstruction(instruction);
				tSmsProductInfo.setProductId(productId);
				tSmsProductInfo.setType(type);
				tSmsProductInfo.setMerchantId(merchantId);
				smsProductInfoManager.save(tSmsProductInfo);
			} else {//更新
				TSmsProductInfo tSmsProductInfo = smsProductInfoManager.get(id);
				if (tSmsProductInfo != null) {
					tSmsProductInfo.setName(name);
					tSmsProductInfo.setPrice(price);
					tSmsProductInfo.setSendNumber(sendNumber);
					tSmsProductInfo.setInstruction(instruction);
					tSmsProductInfo.setProductId(productId);
					tSmsProductInfo.setType(type);
					tSmsProductInfo.setMerchantId(merchantId);
					smsProductInfoManager.save(tSmsProductInfo);
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
					smsProductInfoManager.delete(Integer.parseInt(id));
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
