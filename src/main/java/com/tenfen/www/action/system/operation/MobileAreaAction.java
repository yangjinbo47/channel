package com.tenfen.www.action.system.operation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.MobileAreaManager;

public class MobileAreaAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;

	@Autowired
	private MobileAreaManager mobileAreaManager;

	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String firstNum = ServletRequestUtils.getStringParameter(request, "firstNum", null);
		String middleNum = ServletRequestUtils.getStringParameter(request, "middleNum", null);

		Page<TMobileArea> mobileAreadPage = new Page<TMobileArea>();
		//设置默认排序方式
		mobileAreadPage.setPageSize(limit);
		mobileAreadPage.setPageNo(page);
		if (!mobileAreadPage.isOrderBySetted()) {
			mobileAreadPage.setOrderBy("id");
			mobileAreadPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(firstNum) && Utils.isEmpty(middleNum)) {
			mobileAreadPage = mobileAreaManager.getMobileAreaPage(mobileAreadPage);
		} else {
			mobileAreadPage = mobileAreaManager.getMobileAreaByProperties(firstNum, middleNum, mobileAreadPage);
		}
		
		long nums = mobileAreadPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("mobileAreas:");

		List<TMobileArea> mobileAreas = mobileAreadPage.getResult();
		jstr.append(JSON.toJSONString(mobileAreas));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}

	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String firstNum = ServletRequestUtils.getStringParameter(request, "firstNum").trim();
			String middleNum = ServletRequestUtils.getStringParameter(request, "middleNum").trim();
			String address = ServletRequestUtils.getStringParameter(request, "address").trim();
			String province = ServletRequestUtils.getStringParameter(request, "province").trim();
			String city = ServletRequestUtils.getStringParameter(request, "city").trim();
			String brand = ServletRequestUtils.getStringParameter(request, "brand").trim();

			if (id == -1) {
				List<TMobileArea> list = mobileAreaManager.getMobileAreaList(firstNum, middleNum);
				if (list.size() > 0) {
					StringUtil.printJson(response, MSG.failure("该号段已存在"));
					return;
				}
				TMobileArea mobileArea = new TMobileArea();
				mobileArea.setFirstNum(firstNum);
				mobileArea.setMiddleNum(middleNum);
				mobileArea.setAddress(address);
				mobileArea.setProvince(province);
				mobileArea.setCity(city);
				mobileArea.setBrand(brand);
				mobileAreaManager.save(mobileArea);
			} else {//更新
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(id);
				if (mobileArea != null) {
					mobileArea.setFirstNum(firstNum);
					mobileArea.setMiddleNum(middleNum);
					mobileArea.setAddress(address);
					mobileArea.setProvince(province);
					mobileArea.setCity(city);
					mobileArea.setBrand(brand);
					mobileAreaManager.save(mobileArea);
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
					mobileAreaManager.delete(Integer.parseInt(id));
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
