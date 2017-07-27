package com.tenfen.www.action.system.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.tenfen.entity.account.Privilege;
import com.tenfen.entity.account.User;
import com.tenfen.util.StringUtil;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.account.AccountManager;
import com.tenfen.www.service.account.PrivilegeManager;

/**
 * 权限管理Action.
 * 
 * @author BOBO
 */
public class PrivilegeAction extends SimpleActionSupport {

	private static final long serialVersionUID = 5750485198848585137L;

	@Autowired
	private AccountManager accountManager;
	@Autowired
	private PrivilegeManager privilegeManager;

	//-- 页面属性 --//
	private Integer privilegeid;
	private Privilege privilege;
	private List<Privilege> allPrivilegeList;

	public void delete() throws Exception {
		if(privilegeid == null){
			StringUtil.printJson(response, MSG.failure("删除失败：缺少功能菜单id参数"));
			return ;
		}
		
//		if (accountManager.existsUserAuthority(privilegeid)) {
//			StringUtil.printJson(response, MSG.failure("当前有用户关联了该权限，无法被删除"));
//			return;
//		}
		try {
			Privilege privilege = accountManager.getAuthority(privilegeid);
			privilegeManager.delete(privilege);
			
			//更新权限缓存
			Integer operId = (Integer)getSessionAttribute(Constants.OPERATOR_ID);
//			Integer operId = (Integer) getMemcacheAttribute(Constants.OPERATOR_ID);
			User user = accountManager.getUser(operId);
			accountManager.cacheAuthority(user);
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
		}
	}

	public void add() throws Exception {
		String text = ServletRequestUtils.getStringParameter(request, "text", "");
		Integer status =  ServletRequestUtils.getIntParameter(request, "status", 1);
		String xtype = ServletRequestUtils.getStringParameter(request, "xtype", "");
		String iconCls = ServletRequestUtils.getStringParameter(request, "iconCls", "");
		String controller = ServletRequestUtils.getStringParameter(request, "controller", "");
		Integer parentid =  ServletRequestUtils.getIntParameter(request, "parentid", 0);
		Integer menulevel =  ServletRequestUtils.getIntParameter(request, "menulevel", 1);
		
		try {
//			boolean exist = accountManager.isAuthorityUnique(text, "");
//			if (!exist) {
//				StringUtil.printJson(response, MSG.failure("该权限名已存在"));
//				return;
//			}
			
			if (privilegeid == null) {
				privilege = new Privilege();
			} else {
				privilege = accountManager.getAuthority(privilegeid);
			}
			privilege.setText(text);
			privilege.setStatus(status);
			privilege.setXtype(xtype);
			privilege.setIconcls(iconCls);
			privilege.setController(controller);
			privilege.setParentid(parentid);
			privilege.setMenulevel(menulevel);
			privilege.setIsleaf(1);
			privilege.setCreateoperid((Integer)getSessionAttribute(Constants.OPERATOR_ID));
//			privilege.setCreateoperid((Integer)getMemcacheAttribute(Constants.OPERATOR_ID));
			//得到menuorder
			if (parentid == 0) {//一级节点的序列值
				Integer maxOrder = privilegeManager.getFirstLevelMaxOrder();
				privilege.setMenuorder(maxOrder + 1);
			} else {//二级节点的序列值
				Integer maxOrder = privilegeManager.getSecondLevelMaxOrder(parentid);
				privilege.setMenuorder(maxOrder + 1);
			}
			
			Integer serializable = accountManager.saveAuthorityEntity(privilege);
			
			if (parentid == 0) {//添加的是一级节点，更新父亲节点
				privilege.setParentid(serializable);
				accountManager.saveAuthority(privilege);
			} else {//添加的是二级节点，更新父亲节点leaf属性为0
				Privilege parentPrivilege = accountManager.getAuthority(parentid);
				parentPrivilege.setIsleaf(0);
				accountManager.saveAuthority(parentPrivilege);
			}
			
			StringUtil.printJson(response, MSG.success(MSG.ADDSUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.ADDFAILURE));
		}
	}
	
	public void update() throws Exception {
		String text = ServletRequestUtils.getStringParameter(request, "text", "");
		Integer status =  ServletRequestUtils.getIntParameter(request, "status", 1);
		String xtype = ServletRequestUtils.getStringParameter(request, "xtype", "");
		String iconCls = ServletRequestUtils.getStringParameter(request, "iconCls", "");
		String controller = ServletRequestUtils.getStringParameter(request, "controller", "");
		
		try {
			privilege = accountManager.getAuthority(privilegeid);
			
//			boolean exist = accountManager.isAuthorityUnique(text, privilege.getText());
//			if (!exist) {
//				StringUtil.printJson(response, MSG.failure("该权限名已存在"));
//				return;
//			}
			
			privilege.setText(text);
			privilege.setStatus(status);
			privilege.setXtype(xtype);
			privilege.setIconcls(iconCls);
			privilege.setController(controller);
			
			accountManager.saveAuthority(privilege);
			StringUtil.printJson(response, MSG.success(MSG.UPDATESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.UPDATEFAILURE));
		}
		
	}
	
	/**
	 * 更新权限菜单顺序
	 * @throws Exception
	 */
	public void changeOrder() throws Exception {
		String data = ServletRequestUtils.getStringParameter(request, "orderdata", "");
		
		if(StringUtils.isEmpty(data)){
			StringUtil.printJson(response, MSG.failure("修改失败：缺少排序参数"));
			return;
		}
		
		if(!data.startsWith("[") || !data.endsWith("]")){
			StringUtil.printJson(response, MSG.failure("修改失败：排序参数非法"));
			return;
		}
		
		JSONArray jsonArray = JSON.parseArray(data);
		int result = privilegeManager.updateOrder(jsonArray);
		if (result > 0) {
			Integer operId = (Integer)getSessionAttribute(Constants.OPERATOR_ID);
//			Integer operId = (Integer)getMemcacheAttribute(Constants.OPERATOR_ID);
			User user = accountManager.getUser(operId);
			accountManager.cacheAuthority(user);
		}
		StringUtil.printJson(response, result > 0 ? MSG.success(String.format("共 <b>%s</b> 条记录需要调整，调整成功 <b>%s</b>", jsonArray.size(),result)) : MSG.failure(MSG.UPDATEFAILURE));
	}
	
	@Override
	public String execute() {
		return null;
	}

	public void list() {
		allPrivilegeList = accountManager.getAllAuthority();
		if(allPrivilegeList != null && allPrivilegeList.size() > 0){
			StringUtil.printJson(response, JSON.toJSONString(this.getFunctionMenuTreeData(allPrivilegeList)));
		}else{
			StringUtil.printJson(response, "[]");
		}
	}
	
	public String input() {
		if (privilegeid == null) {
			privilege = new Privilege();
		} else {
			privilege = accountManager.getAuthority(privilegeid);
		}
		return "authorityinput";
	}

	/**
	 * 组装集合为树形结构
	 * @param listKnowledgePoint 待组装的集合
	 * @return
	 */
	private List<Privilege> getFunctionMenuTreeData(List<Privilege> privilegeList){
		Map<Integer,Privilege> mapvo = new HashMap<Integer,Privilege>();
		List<Privilege> listRootVO = new ArrayList<Privilege>();
		for(Privilege privilege : privilegeList){
			//menuModel.setExpanded(true);
			mapvo.put(privilege.getPrivilegeid(), privilege);
			if(privilege.getParentid().equals(privilege.getPrivilegeid())){
				listRootVO.add(privilege);
			}
		}
		
		for(Privilege privilege : privilegeList){
			Integer parentid = privilege.getParentid();
			Privilege modelParent = (Privilege) mapvo.get(parentid);
			if(modelParent != null && !modelParent.getPrivilegeid().equals(privilege.getPrivilegeid())){
				modelParent.getChildren().add(privilege);
			}
		}
		
		return listRootVO;
	}
	
	public Privilege getModel() {
		return privilege;
	}

	public List<Privilege> getAllAuthorityList() {
		return allPrivilegeList;
	}

	public void setAllAuthorityList(List<Privilege> allPrivilegeList) {
		this.allPrivilegeList = allPrivilegeList;
	}

	public Integer getPrivilegeid() {
		return privilegeid;
	}

	public void setPrivilegeid(Integer privilegeid) {
		this.privilegeid = privilegeid;
	}

	public Privilege getAuthority() {
		return privilege;
	}

	public void setAuthority(Privilege privilege) {
		this.privilege = privilege;
	}

}
