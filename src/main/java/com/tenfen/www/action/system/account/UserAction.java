package com.tenfen.www.action.system.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.account.Privilege;
import com.tenfen.entity.account.Role;
import com.tenfen.entity.account.User;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.account.AccountManager;
import com.tenfen.www.service.account.OperatorManager;

/**
 * 用户管理Action.
 * 
 * @author BOBO
 */
@SuppressWarnings("unchecked")
public class UserAction extends SimpleActionSupport {

	private static final long serialVersionUID = 3205177939227033736L;
	
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private OperatorManager operatorManager;
	@Autowired
	private CacheFactory cacheFactory;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	//页面属性
	private Integer id;
	private User user;
	private List<Integer> checkedRoleIds; //页面中钩选的角色id列表
	
	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer(
				"yyyy-MM-dd HH:mm:ss"));
	}
	
	@Override
	public String execute() {
		return null;
	}
	
	public void list() {
		String name = ServletRequestUtils.getStringParameter(request, "name", null);
		Page<User> userPage = new Page<User>();
		//设置默认排序方式
		userPage.setPageSize(limit);
		userPage.setPageNo(page);
		if (!userPage.isOrderBySetted()) {
			userPage.setOrderBy("operid");
			userPage.setOrder(Page.ASC);
		}
		if (Utils.isEmpty(name)) {
			userPage = operatorManager.getUserList(userPage);
		} else {
			userPage = operatorManager.getUsersByName(name, userPage);
		}
		
		long nums = userPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("user:");

		List<User> userList = userPage.getResult();
		jstr.append(JSON.toJSONString(userList, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
		
//		return "userlist";
	}
	
	public void modPassword() {
		String result = null;
		boolean success = false;
		try {
			Integer id = getIntParam("id");
			User user = accountManager.getUser(id);
			String loginName = user.getLoginName();//登录名
			String password = request.getParameter("password");
			String passwordConfirm = request.getParameter("passwordConfirm");
			
			if (Utils.isEmpty(password) || Utils.isEmpty(passwordConfirm)) {
				addActionMessage("密码不能为空！");
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/www/account/user_input.action");
				requestDispatcher.forward(request, response);
			}
			if (!password.equals(passwordConfirm)) {
				addActionMessage("密码不一致！");
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/www/account/user_input.action");
				requestDispatcher.forward(request, response);
			}
			
			user.setLoginName(loginName);
			user.setPassword(MD5.getMD5(password));
			
			accountManager.saveUser(user);
			
			success = true;
			result = "密码修改成功";
		} catch (Exception e) {
			success = false;
			result = "密码修改失败";
			logger.error(e.getMessage(),e);
		}
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("success: "+success+",");
		jstr.append("msg:");
		jstr.append(JSON.toJSONString(result));
		jstr.append("}");
		logger.info(jstr.toString());
		StringUtil.printJson(response, jstr.toString());
		return;
	}
	
	/**
	 * 保存用户
	 */
	public void save() {
		Integer operid = ServletRequestUtils.getIntParameter(request, "operid", -1);
		String loginName = ServletRequestUtils.getStringParameter(request, "loginName", "");
		String name = ServletRequestUtils.getStringParameter(request, "name", "");
		String password = ServletRequestUtils.getStringParameter(request, "password", "");
		String roleIds = ServletRequestUtils.getStringParameter(request, "roleIds", "");
		Integer type = ServletRequestUtils.getIntParameter(request, "type", Constants.USER_TYPE.ALL.getValue());
		
		try {
			String[] idsArr = roleIds.split(",");
			List<Integer> roleids = new ArrayList<Integer>();
			for (String string : idsArr) {
				if (!Utils.isEmpty(string)) {
					roleids.add(Integer.parseInt(string));
				}
			}
			
			if (operid == -1) {
				user = new User();
				user.setLoginName(loginName);
			} else {
				user = accountManager.getUser(operid);
			}
			
			if (!Utils.isEmpty(password)) {
				user.setPassword(MD5.getMD5(password));
			}
			user.setName(name);
			user.setType(type);
			
			HibernateUtils.mergeByCheckedIds(user.getRoleList(), roleids, Role.class);
			accountManager.saveUser(user);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
		}
	}
	
	public void delete() {
		try {
//			String operid = request.getParameter("operid");
			Integer operid = ServletRequestUtils.getIntParameter(request, "operid", -1);
			if (operid == -1) {
				StringUtil.printJson(response, MSG.failure("该用户不存在"));
			}
			
			accountManager.deleteUser(operid);
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.EXCEPTION));
			logger.error(e.getMessage(),e);
		}
	}
	
	public void getCurrentUser() {
		String userName = (String)getSessionAttribute(Constants.OPERATOR_NAME);
//		String userName = (String)getMemcacheAttribute(Constants.OPERATOR_NAME);
		
		User user = accountManager.findUserByLoginName(userName);
		StringBuilder rspStr = new StringBuilder("{");
		rspStr.append("total:1,");
		rspStr.append("user:");
		rspStr.append(JSON.toJSONString(user));
		rspStr.append("}");
		StringUtil.printJson(response, rspStr.toString());
		return;
	}
	
	/**
	 * 获取用户所属角色list
	 */
	public void rolesOfUser() {
		Integer operid = ServletRequestUtils.getIntParameter(request, "operid", -1);
		
		User user = accountManager.getUser(operid);
		
		List<Role> roleList = new ArrayList<Role>();
		if (!Utils.isEmpty(user)) {
			roleList = user.getRoleList();
		}
		int size = roleList.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("roles:");
		jstr.append(JSON.toJSONString(roleList, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给用户的角色
	 */
	public void getUnallocateRole() {
		String roleIds = ServletRequestUtils.getStringParameter(request, "roleIds", "");
		
		String[] idsArr = roleIds.split(",");
		List<Role> checkRoles = new ArrayList<Role>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		Role role = accountManager.getRole(Integer.parseInt(string));
        		checkRoles.add(role);
			}
		}
		
		List<Role> allRoles = accountManager.getAllRole();
		allRoles.removeAll(checkRoles);
		
		int size = allRoles.size();
		
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("roles:");
		jstr.append(JSON.toJSONString(allRoles, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 获取用户菜单
	 */
	public void getMenu() {
		String nid = request.getParameter("node");
		
		Integer operId = (Integer)getSessionAttribute(Constants.OPERATOR_ID);
//		Integer operId = (Integer) getMemcacheAttribute(Constants.OPERATOR_ID);
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		
		List<Privilege> menuList = null;
		if (Utils.isEmpty(nid)) {
			String firstkey = Constants.PRE_FIRST_AUTHORITY+operId;
			menuList = (List<Privilege>)mc.getCache(firstkey);
		} else {
			String secondkey = Constants.PRE_SECOND_AUTHORITY+operId;
			Map<Integer, List<Privilege>> twoMap = (Map<Integer, List<Privilege>>)mc.getCache(secondkey);
			menuList = twoMap.get(Integer.parseInt(nid));
		}
		
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("success: true,");
		jstr.append("children:");
		jstr.append(JSON.toJSONString(menuList));
		jstr.append("}");
		logger.info(jstr.toString());
		StringUtil.printJson(response, jstr.toString());
	}
	//-- 其他Action函数 --//
	/**
	 * 支持使用Jquery.validate Ajax检验用户名是否重复.
	 */
	public String checkLoginName() {
		String loginName = ServletRequestUtils.getStringParameter(request, "loginName", "");

		if (accountManager.isLoginNameUnique(loginName, "")) {
//			Struts2Utils.renderText("true");
			StringUtil.printJson(response, "true");
		} else {
//			Struts2Utils.renderText("false");
			StringUtil.printJson(response, "false");
		}
		return null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * input页面显示所有角色列表.
	 */
	public List<Role> getAllRoleList() {
		return accountManager.getAllRole();
	}
	
	/**
	 * input页面显示用户拥有的角色.
	 */
	public List<Integer> getCheckedRoleIds() {
		return checkedRoleIds;
	}

	/**
	 * input页面提交用户拥有的角色.
	 */
	public void setCheckedRoleIds(List<Integer> checkedRoleIds) {
		this.checkedRoleIds = checkedRoleIds;
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
