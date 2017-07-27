package com.tenfen.www.action.system.account;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.bean.account.RolePrivilegeModel;
import com.tenfen.entity.account.Privilege;
import com.tenfen.entity.account.Role;
import com.tenfen.util.StringUtil;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.account.AccountManager;
import com.tenfen.www.service.account.RoleManager;

/**
 * 角色管理Action.
 * 
 * 角色管理界面.
 * 
 * @author BOBO
 */
public class RoleAction extends SimpleActionSupport {

	private static final long serialVersionUID = 1L;

	@Autowired
	private AccountManager accountManager;
	@Autowired
	private RoleManager roleManager;
	
	private static SerializeConfig config = new SerializeConfig();
    static
    {
        config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
    }

	//-- 页面属性 --//
	private Integer id;
	private Role role;
	private List<Role> roleList;//角色列表
//	private List<Integer> checkedAuthIds;//页面中钩选的权限id列表

	public String execute() {
		return null;
	}
	
//	public String save() {
//		try {
//			String name = ServletRequestUtils.getStringParameter(request, "name", "");
//			
//			if (id == null) {
//				role = new Role();
//			} else {
//				role = accountManager.getRole(id);
//			}
//			role.setName(name);
//			//根据页面上的checkbox 整合Role的Authorities Set.
//			HibernateUtils.mergeByCheckedIds(role.getAuthorityList(), checkedAuthIds, Privilege.class);
//			//保存用户并放入成功信息.
//			accountManager.saveRole(role);
//			addActionMessage("保存成功！<br/><br/>如果您更改了设置，建议您重新登陆后台管理系统");
//			logger.debug("角色保存成功");
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//		}
//		return "rolereload";
//	}
	
	public void add() {
		try {
			Integer operId = (Integer)getSessionAttribute(Constants.OPERATOR_ID);
//			Integer operId = (Integer) getMemcacheAttribute(Constants.OPERATOR_ID);
			String roleName = ServletRequestUtils.getStringParameter(request, "roleName", "");
			String remark = ServletRequestUtils.getStringParameter(request, "remark", "");
            String ids = ServletRequestUtils.getStringParameter(request, "ids", "");
            
            boolean roleExist = accountManager.isRoleUnique(roleName, "");
            if (!roleExist) {
    			StringUtil.printJson(response, MSG.failure("角色名称已存在"));
    			return;
			}
            
            role = new Role();
            role.setName(roleName);
            role.setRemark(remark);
            role.setCreateoperid(operId);
            
            String[] idsArr = ids.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(role.getAuthorityList(), idsList, Privilege.class);
            
			accountManager.saveRole(role);
			
			StringUtil.printJson(response, MSG.success(MSG.ADDSUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
			logger.error(e.getMessage(),e);
		}
	}
	
	public void mod() {
		try {
			Integer roleId = ServletRequestUtils.getIntParameter(request, "roleid", -1);
            String roleName = ServletRequestUtils.getStringParameter(request, "roleName", "");
            String remark = ServletRequestUtils.getStringParameter(request, "remark", "");
            String ids = request.getParameter("ids");
            
            if (roleId == -1) {
            	StringUtil.printJson(response, MSG.failure("该角色不存在"));
    			return;
			}
            
            role = accountManager.getRole(roleId);
            boolean roleExist = accountManager.isRoleUnique(roleName, role.getName());
            if (!roleExist) {
    			StringUtil.printJson(response, MSG.failure("角色名称已存在"));
    			return;
			}
            
            role.setName(roleName);
            role.setRemark(remark);
            
            String[] idsArr = ids.split(",");
            List<Integer> idsList = new ArrayList<Integer>();
            for (String string : idsArr) {
            	idsList.add(Integer.parseInt(string));
			}
            //根据页面上的checkbox 整合Role的Authorities Set.
			HibernateUtils.mergeByCheckedIds(role.getAuthorityList(), idsList, Privilege.class);
            
			accountManager.saveRole(role);
			
			StringUtil.printJson(response, MSG.success(MSG.UPDATESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
			logger.error(e.getMessage(),e);
		}
	}

	public void delete() {
		try {
			String ids = request.getParameter("ids");
			String[] idsArr = ids.split(",");
            for (String id : idsArr) {
            	accountManager.deleteRole(Integer.parseInt(id));
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
			logger.error(e.getMessage(),e);
		}
	}
	
	public void list() {
		String name = ServletRequestUtils.getStringParameter(request, "name", null);
		
		if (StringUtil.isEmpty(name)) {
			roleList = accountManager.getAllRole();
		} else {
			roleList = roleManager.getRolesByName(name);
		}
		
		int nums = roleList.size();
		StringBuilder jstr = new StringBuilder("{");
        jstr.append("total:" + String.valueOf(nums) + ",");
        jstr.append("roles:");
        jstr.append(JSON.toJSONString(roleList, config));
        jstr.append("}");
        
        StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 获取角色权限
	 */
	public void privilegeList() {
		String nid = request.getParameter("node");
		String id = request.getParameter("id");
		
		List<Privilege> privilegeList = null;
		if (StringUtil.isEmpty(nid) || "root".equals(nid)) {
			privilegeList = roleManager.getAllFirstPrivilege();
		} else {
			privilegeList = roleManager.getSecondPrivilege(Integer.parseInt(nid));
		}
		
		List<RolePrivilegeModel> rolePrivilegeModels = new ArrayList<RolePrivilegeModel>();
		for (Privilege privilege : privilegeList) {
			RolePrivilegeModel rolePrivilegeModel = new RolePrivilegeModel();
			rolePrivilegeModel.setId(privilege.getPrivilegeid());
			rolePrivilegeModel.setText(privilege.getText());
			rolePrivilegeModel.setParentid(privilege.getParentid());
			rolePrivilegeModel.setLeaf(privilege.getLeaf());
			
			rolePrivilegeModels.add(rolePrivilegeModel);
		}
		
		if (StringUtil.isNotEmpty(id)) {
//			Integer operId = (Integer)getSessionAttribute(Constants.OPERATOR_ID);
//			User user = accountManager.getUser(operId);
//			List<Role> roleList = user.getRoleList();
//			List<Integer> authIds = new ArrayList<Integer>();
//			for (Role role : roleList) {
//				authIds.addAll(role.getAuthIds());
//			}
			Role role = accountManager.getRole(Integer.parseInt(id));
			List<Integer> authIds = role.getAuthIds();
			boolean checked = false;
            for (RolePrivilegeModel rolePrivilegeModel : rolePrivilegeModels)
            {
                checked = authIds.contains(rolePrivilegeModel.getId());
                rolePrivilegeModel.setChecked(checked);
            }
		}
		
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("success: true,");
		jstr.append("children:");
		jstr.append(JSON.toJSONString(rolePrivilegeModels));
		jstr.append("}");
		logger.info(jstr.toString());
		StringUtil.printJson(response, jstr.toString());
	}

	/**
	 * input页面显示所有授权列表.
	 */
	public List<Privilege> getAllAuthorityList() {
		return accountManager.getAllAuthority();
	}

	/**
	 * input页面显示角色拥有的授权.
	 */
//	public List<Integer> getCheckedAuthIds() {
//		return checkedAuthIds;
//	}
	
	//-- CRUD Action 函数 --//
//	@Override
//	public String input() throws Exception {
//		if (id == null) {
//			role = new Role();
//		} else {			
//			role = accountManager.getRole(id);
//			checkedAuthIds = role.getAuthIds();
//		}
//		return "roleinput";
//	}

	/**
	 * input页面提交角色拥有的授权.
	 */
//	public void setCheckedAuthIds(List<Integer> checkedAuthIds) {
//		this.checkedAuthIds = checkedAuthIds;
//	}

	//-- 页面属性访问函数 --//
	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}