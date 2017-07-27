package com.tenfen.www.action.system.operation.open;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.open.TOpenMailer;
import com.tenfen.entity.operation.open.TOpenMailgroup;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.open.OpenMailManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

public class OpenMailAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenMailManager openMailManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void groups() {
		String groupName = ServletRequestUtils.getStringParameter(request, "groupName", null);

		Page<TOpenMailgroup> groupPage = new Page<TOpenMailgroup>();
		//设置默认排序方式
		groupPage.setPageSize(limit);
		groupPage.setPageNo(page);
		if (!groupPage.isOrderBySetted()) {
			groupPage.setOrderBy("id");
			groupPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(groupName)) {
			groupPage = openMailManager.findMailgroupPage(groupPage);
		} else {
			groupPage = openMailManager.findMailgroupPage(groupName, groupPage);
		}
		
		long nums = groupPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("groups:");

		List<TOpenMailgroup> groupList = groupPage.getResult();
		jstr.append(JSON.toJSONString(groupList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveGroup() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);

			if (id == -1) {
				TOpenMailgroup tOpenMailgroup = new TOpenMailgroup();
				tOpenMailgroup.setName(name);
				openMailManager.saveGroup(tOpenMailgroup);
			} else {//更新
				TOpenMailgroup tOpenMailgroup = openMailManager.getGroup(id);
				if (tOpenMailgroup != null) {
					tOpenMailgroup.setName(name);
					openMailManager.saveGroup(tOpenMailgroup);
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
	public void deleteGroup() {
		try {
			String ids = ServletRequestUtils.getStringParameter(getRequest(), "ids");
			if (!Utils.isEmpty(ids)) {
				String[] idsArr = ids.split(",");
				for (String id : idsArr) {
					openMailManager.deleteGroup(Integer.parseInt(id));
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	public void mailerList(){
		Integer groupId = ServletRequestUtils.getIntParameter(request, "groupId", -1);
		
		try {
			TOpenMailgroup tOpenMailgroup = openMailManager.getGroup(groupId);
			List<TOpenMailer> haveSet = tOpenMailgroup.getMailerList();
			List<TOpenMailer> mailers = openMailManager.getMailerAll();//查询所有
			for (TOpenMailer tOpenMailer : mailers) {
				if (haveSet.contains(tOpenMailer)) {
					tOpenMailer.setSelect(true);
				}
			}
			
			long nums = mailers.size();
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("mailers:");

			jstr.append(JSON.toJSONString(mailers));
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	public void saveGroupMailer() {
		try {
			Integer groupId = ServletRequestUtils.getIntParameter(request, "groupId", -1);
			String mailerIds = ServletRequestUtils.getStringParameter(getRequest(), "mailerIds");
			
			TOpenMailgroup group = openMailManager.getGroup(groupId);
			
			String[] idsArr = mailerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getMailerList(), idsList, TOpenMailer.class);
			
			openMailManager.saveGroup(group);
			
			StringUtil.printJson(response, MSG.success(MSG.ADDSUCCESS));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
		}
	}
	
	public void saveMailer() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);
			String email = ServletRequestUtils.getStringParameter(request, "email", null);

			if (id == -1) {
				TOpenMailer tOpenMailer = new TOpenMailer();
				tOpenMailer.setName(name);
				tOpenMailer.setEmail(email);
				openMailManager.saveMailer(tOpenMailer);
			} else {//更新
				TOpenMailer tOpenMailer = openMailManager.getMailer(id);
				if (tOpenMailer != null) {
					tOpenMailer.setName(name);
					tOpenMailer.setEmail(email);
					openMailManager.saveMailer(tOpenMailer);
				}
			}

			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	public void deleteMailer() {
		try {
			String ids = ServletRequestUtils.getStringParameter(getRequest(), "ids");
			if (!Utils.isEmpty(ids)) {
				String[] idsArr = ids.split(",");
				for (String id : idsArr) {
					openMailManager.deleteMailer(Integer.parseInt(id));
					//删除所有关联的组
					openMailManager.deleteGroupMailer(Integer.parseInt(id));
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 邮件组渠道设置
	 */
	public void sellerList(){
		Integer groupId = ServletRequestUtils.getIntParameter(request, "groupId", -1);
		
		try {
			TOpenMailgroup tOpenMailgroup = openMailManager.getGroup(groupId);
			List<TOpenSeller> haveSet = tOpenMailgroup.getSellerList();
			List<TOpenSeller> sellers = openSellerManager.findAllOpenSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TOpenSeller tOpenSeller : sellers) {
				if (haveSet.contains(tOpenSeller)) {
					tOpenSeller.setSelect(true);
				}
			}
			
			long nums = sellers.size();
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("sellers:");

			JSONArray jsonArray = new JSONArray();
			for (TOpenSeller tOpenSeller : sellers) {
				JSONObject json = new JSONObject();
				json.put("id", tOpenSeller.getId());
				json.put("name", tOpenSeller.getName());
				json.put("email", tOpenSeller.getEmail());
				json.put("contact", tOpenSeller.getContact());
				json.put("telephone", tOpenSeller.getTelephone());
				json.put("sellerKey", tOpenSeller.getSellerKey());
				json.put("sellerSecret", tOpenSeller.getSellerSecret());
				json.put("callbackUrl", tOpenSeller.getCallbackUrl());
				json.put("status", tOpenSeller.getStatus());
				json.put("companyShow", tOpenSeller.getCompanyShow());
				json.put("select", tOpenSeller.isSelect());
				jsonArray.add(json);
			}
			
			jstr.append(jsonArray.toString());
//			jstr.append(JSON.toJSONString(sellers));
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	public void saveGroupSeller() {
		try {
			Integer groupId = ServletRequestUtils.getIntParameter(request, "groupId", -1);
			String sellerIds = ServletRequestUtils.getStringParameter(getRequest(), "sellerIds");
			
			TOpenMailgroup group = openMailManager.getGroup(groupId);
			
			String[] idsArr = sellerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getSellerList(), idsList, TOpenSeller.class);
			
			openMailManager.saveGroup(group);
			
			StringUtil.printJson(response, MSG.success(MSG.ADDSUCCESS));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
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
