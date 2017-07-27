package com.tenfen.www.action.system.operation.sms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.sms.TSmsMailer;
import com.tenfen.entity.operation.sms.TSmsMailgroup;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.sms.SmsMailManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class SmsMailAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsMailManager smsMailManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void groups() {
		String groupName = ServletRequestUtils.getStringParameter(request, "groupName", null);

		Page<TSmsMailgroup> groupPage = new Page<TSmsMailgroup>();
		//设置默认排序方式
		groupPage.setPageSize(limit);
		groupPage.setPageNo(page);
		if (!groupPage.isOrderBySetted()) {
			groupPage.setOrderBy("id");
			groupPage.setOrder(Page.DESC);
		}
		if (Utils.isEmpty(groupName)) {
			groupPage = smsMailManager.findMailgroupPage(groupPage);
		} else {
			groupPage = smsMailManager.findMailgroupPage(groupName, groupPage);
		}
		
		long nums = groupPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("groups:");

		List<TSmsMailgroup> groupList = groupPage.getResult();
		jstr.append(JSON.toJSONString(groupList));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveGroup() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String name = ServletRequestUtils.getStringParameter(request, "name", null);

			if (id == -1) {
				TSmsMailgroup TSmsMailgroup = new TSmsMailgroup();
				TSmsMailgroup.setName(name);
				smsMailManager.saveGroup(TSmsMailgroup);
			} else {//更新
				TSmsMailgroup TSmsMailgroup = smsMailManager.getGroup(id);
				if (TSmsMailgroup != null) {
					TSmsMailgroup.setName(name);
					smsMailManager.saveGroup(TSmsMailgroup);
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
					smsMailManager.deleteGroup(Integer.parseInt(id));
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
			TSmsMailgroup tSmsMailgroup = smsMailManager.getGroup(groupId);
			List<TSmsMailer> haveSet = tSmsMailgroup.getMailerList();
			List<TSmsMailer> mailers = smsMailManager.getMailerAll();//查询所有
			for (TSmsMailer TSmsMailer : mailers) {
				if (haveSet.contains(TSmsMailer)) {
					TSmsMailer.setSelect(true);
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
			
			TSmsMailgroup group = smsMailManager.getGroup(groupId);
			
			String[] idsArr = mailerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getMailerList(), idsList, TSmsMailer.class);
			
			smsMailManager.saveGroup(group);
			
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
				TSmsMailer tSmsMailer = new TSmsMailer();
				tSmsMailer.setName(name);
				tSmsMailer.setEmail(email);
				smsMailManager.saveMailer(tSmsMailer);
			} else {//更新
				TSmsMailer TSmsMailer = smsMailManager.getMailer(id);
				if (TSmsMailer != null) {
					TSmsMailer.setName(name);
					TSmsMailer.setEmail(email);
					smsMailManager.saveMailer(TSmsMailer);
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
					smsMailManager.deleteMailer(Integer.parseInt(id));
					//删除所有关联的组
					smsMailManager.deleteGroupMailer(Integer.parseInt(id));
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
			TSmsMailgroup tSmsMailgroup = smsMailManager.getGroup(groupId);
			List<TSmsSeller> haveSet = tSmsMailgroup.getSellerList();
			List<TSmsSeller> sellers = smsSellerManager.findAllSmsSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TSmsSeller tSmsSeller : sellers) {
				if (haveSet.contains(tSmsSeller)) {
					tSmsSeller.setSelect(true);
				}
			}
			
			long nums = sellers.size();
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("sellers:");

			JSONArray jsonArray = new JSONArray();
			for (TSmsSeller TSmsSeller : sellers) {
				JSONObject json = new JSONObject();
				json.put("id", TSmsSeller.getId());
				json.put("name", TSmsSeller.getName());
				json.put("email", TSmsSeller.getEmail());
				json.put("contact", TSmsSeller.getContact());
				json.put("telephone", TSmsSeller.getTelephone());
				json.put("sellerKey", TSmsSeller.getSellerKey());
				json.put("sellerSecret", TSmsSeller.getSellerSecret());
				json.put("callbackUrl", TSmsSeller.getCallbackUrl());
				json.put("status", TSmsSeller.getStatus());
				json.put("companyShow", TSmsSeller.getCompanyShow());
				json.put("select", TSmsSeller.isSelect());
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
			
			TSmsMailgroup group = smsMailManager.getGroup(groupId);
			
			String[] idsArr = sellerIds.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(group.getSellerList(), idsList, TSmsSeller.class);
			
			smsMailManager.saveGroup(group);
			
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
