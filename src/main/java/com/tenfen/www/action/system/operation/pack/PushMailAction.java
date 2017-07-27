package com.tenfen.www.action.system.operation.pack;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.pack.TPushMailer;
import com.tenfen.entity.operation.pack.TPushMailgroup;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.pack.PushMailManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class PushMailAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private PushMailManager pushMailManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void groups() {
		String groupName = ServletRequestUtils.getStringParameter(request, "groupName", null);

		Page<TPushMailgroup> groupPage = new Page<TPushMailgroup>();
		//设置默认排序方式
		groupPage.setPageSize(limit);
		groupPage.setPageNo(page);
		if (!groupPage.isOrderBySetted()) {
			groupPage.setOrderBy("id");
			groupPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(groupName)) {
			groupPage = pushMailManager.findMailgroupPage(groupPage);
		} else {
			groupPage = pushMailManager.findMailgroupPage(groupName, groupPage);
		}
		
		long nums = groupPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("groups:");

		List<TPushMailgroup> groupList = groupPage.getResult();
		jstr.append(JSON.toJSONString(groupList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveGroup() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);

			if (id == -1) {
				TPushMailgroup tPushMailgroup = new TPushMailgroup();
				tPushMailgroup.setName(name);
				pushMailManager.saveGroup(tPushMailgroup);
			} else {//更新
				TPushMailgroup tPushMailgroup = pushMailManager.getGroup(id);
				if (tPushMailgroup != null) {
					tPushMailgroup.setName(name);
					pushMailManager.saveGroup(tPushMailgroup);
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
					pushMailManager.deleteGroup(Integer.parseInt(id));
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
			TPushMailgroup pushMailgroup = pushMailManager.getGroup(groupId);
			List<TPushMailer> haveSet = pushMailgroup.getMailerList();
			List<TPushMailer> mailers = pushMailManager.getMailerAll();//查询所有
			for (TPushMailer tPushMailer : mailers) {
				if (haveSet.contains(tPushMailer)) {
					tPushMailer.setSelect(true);
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
			
			TPushMailgroup group = pushMailManager.getGroup(groupId);
			
			String[] idsArr = mailerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getMailerList(), idsList, TPushMailer.class);
			
			pushMailManager.saveGroup(group);
			
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
				TPushMailer tPushMailer = new TPushMailer();
				tPushMailer.setName(name);
				tPushMailer.setEmail(email);
				pushMailManager.saveMailer(tPushMailer);
			} else {//更新
				TPushMailer tPushMailer = pushMailManager.getMailer(id);
				if (tPushMailer != null) {
					tPushMailer.setName(name);
					tPushMailer.setEmail(email);
					pushMailManager.saveMailer(tPushMailer);
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
					pushMailManager.deleteMailer(Integer.parseInt(id));
					//删除所有关联的组
					pushMailManager.deleteGroupMailer(Integer.parseInt(id));
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
			TPushMailgroup tPushMailgroup = pushMailManager.getGroup(groupId);
			List<TPushSeller> haveSet = tPushMailgroup.getSellerList();
			List<TPushSeller> sellers = pushSellerManager.findAllPushSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TPushSeller tPushSeller : sellers) {
				if (haveSet.contains(tPushSeller)) {
					tPushSeller.setSelect(true);
				}
			}
			
			long nums = sellers.size();
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("sellers:");

			JSONArray jsonArray = new JSONArray();
			for (TPushSeller tPushSeller : sellers) {
				JSONObject json = new JSONObject();
				json.put("id", tPushSeller.getId());
				json.put("name", tPushSeller.getName());
				json.put("sellerKey", tPushSeller.getSellerKey());
				json.put("sellerSecret", tPushSeller.getSellerSecret());
				json.put("callbackUrl", tPushSeller.getCallbackUrl());
				json.put("status", tPushSeller.getStatus());
				json.put("companyShow", tPushSeller.getCompanyShow());
				json.put("select", tPushSeller.isSelect());
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
			
			TPushMailgroup group = pushMailManager.getGroup(groupId);
			
			String[] idsArr = sellerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getSellerList(), idsList, TPushSeller.class);
			
			pushMailManager.saveGroup(group);
			
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
