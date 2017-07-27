package com.tenfen.www.action.system.operation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.tenfen.entity.operation.TBlackList;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.BlackListManager;

public class BlackListAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;

	@Autowired
	private BlackListManager blackListManager;

	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String phoneNum = ServletRequestUtils.getStringParameter(request, "phoneNum", null);

		Page<TBlackList> blackListPage = new Page<TBlackList>();
		//设置默认排序方式
		blackListPage.setPageSize(limit);
		blackListPage.setPageNo(page);
		if (!blackListPage.isOrderBySetted()) {
			blackListPage.setOrderBy("id");
			blackListPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(phoneNum)) {
			blackListPage = blackListManager.findBlackListPage(blackListPage);
		} else {
			blackListPage = blackListManager.findBlackListPageByPhone(phoneNum, blackListPage);
		}
		
		long nums = blackListPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("blackLists:");

		List<TBlackList> blackLists = blackListPage.getResult();
		jstr.append(JSON.toJSONString(blackLists));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}

	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String phoneNum = ServletRequestUtils.getStringParameter(request, "phoneNum");

			if (id == -1) {
				boolean b = blackListManager.isBlackList(phoneNum);
				if (b) {
					StringUtil.printJson(response, MSG.failure("该号码已存在"));
					return;
				}
				TBlackList tBlackList = new TBlackList();
				tBlackList.setPhoneNum(phoneNum);
				blackListManager.save(tBlackList);
			} else {//更新
				TBlackList tBlackList = blackListManager.get(id);
				if (tBlackList != null) {
					tBlackList.setPhoneNum(phoneNum);
					blackListManager.save(tBlackList);
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
					blackListManager.delete(Integer.parseInt(id));
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
