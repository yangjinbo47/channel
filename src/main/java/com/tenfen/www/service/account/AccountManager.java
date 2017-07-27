package com.tenfen.www.service.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.PropertyFilter;
import org.springside.modules.security.springsecurity.SpringSecurityUtils;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.account.Privilege;
import com.tenfen.entity.account.Role;
import com.tenfen.entity.account.User;
import com.tenfen.util.Utils;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.account.PrivilegeDao;
import com.tenfen.www.dao.account.RoleDao;
import com.tenfen.www.dao.account.UserDao;

@Component
@Transactional
public class AccountManager {
	private static Logger logger = LoggerFactory.getLogger(AccountManager.class);
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private PrivilegeDao privilegeDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	//-- User Manager --//
	@Transactional(readOnly = true)
	public User getUser(Integer id) {
		return userDao.get(id);
	}

	public void saveUser(User entity) {
		userDao.save(entity);
	}
	
	/**
	 * 删除用户,如果尝试删除超级管理员将抛出异常.
	 */
	public void deleteUser(Integer id) {
		if (isSupervisor(id)) {
			logger.warn("操作员{}尝试删除超级管理员用户", SpringSecurityUtils.getCurrentUserName());
			throw new RuntimeException("不能删除超级管理员用户");
		}
		userDao.delete(id);
	}
	
	/**
	 * 判断是否超级管理员.
	 */
	private boolean isSupervisor(Integer id) {
		return id == 1;
	}
	
	/**
	 * 使用属性过滤条件查询用户.
	 */
	@Transactional(readOnly = true)
	public Page<User> searchUser(final Page<User> page, final List<PropertyFilter> filters) {
		return userDao.findPage(page, filters);
	}
	
	@Transactional(readOnly = true)
	public User findUserByLoginName(String loginName) {
		return userDao.findUniqueBy("loginName", loginName);
	}
	
	/**
	 * 检查用户名是否唯一.
	 *
	 * @return loginName在数据库中唯一或等于oldLoginName时返回true.
	 */
	@Transactional(readOnly = true)
	public boolean isLoginNameUnique(String newLoginName, String oldLoginName) {
		return userDao.isPropertyUnique("loginName", newLoginName, oldLoginName);
	}
	
	//-- Role Manager --//
	@Transactional(readOnly = true)
	public Role getRole(Integer id) {
		return roleDao.get(id);
	}

	@Transactional(readOnly = true)
	public List<Role> getAllRole() {
		return roleDao.getAll("roleid", true);
	}
	
	@Transactional(readOnly = true)
	public boolean isRoleUnique(String newName, String oldName) {
		return roleDao.isPropertyUnique("name", newName, oldName);
	}

	public void saveRole(Role entity) {
		roleDao.save(entity);
	}

	public void deleteRole(Integer id) {
		roleDao.delete(id);
	}

	//-- Authority Manager --//
	@Transactional(readOnly = true)
	public List<Privilege> getAllAuthority() {
		return privilegeDao.getAll("menuorder", true);
	}

	@Transactional(readOnly = true)
	public Boolean existsUserAuthority(Integer id) {
		Role role = (Role) roleDao.findUnique("select r from Role r left join r.authorityList a where a.privilegeid = ?", id);
		return role != null;
	}

	@Transactional(readOnly = true)
	public boolean isAuthorityUnique(String newName, String oldName) {
		return privilegeDao.isPropertyUnique("text", newName, oldName);
	}

	@Transactional(readOnly = true)
	public Privilege getAuthority(Integer id) {
		return privilegeDao.get(id);
	}

	@Transactional
	public void saveAuthority(Privilege entity){
		privilegeDao.save(entity);
	}
	
	@Transactional
	public Integer saveAuthorityEntity(Privilege entity) {
		return privilegeDao.saveEntity(entity);
	}

	@Transactional
	public void deleteAuthority(Integer id) {
		privilegeDao.delete(id);
	}
	
	/**
	 * 缓存用户权限
	 * @param user
	 */
	public void cacheAuthority(User user) {
		List<Role> tFrameRoles = user.getRoleList();
		List<Privilege> oneMenu = new ArrayList<Privilege>();
		Map<Integer, List<Privilege>> twoMap = new HashMap<Integer, List<Privilege>>();
		for (Role tFrameRole : tFrameRoles) {
			List<Privilege> privileges = tFrameRole.getAuthorityList();
			Collections.sort(privileges);
			for (Privilege privilege : privileges) {
				if (privilege.getMenulevel() == 1) {
					oneMenu.add(privilege);
				}
				if (privilege.getMenulevel() == 2) {
					List<Privilege> twoList = twoMap.get(privilege.getParentid());
					if (Utils.isEmpty(twoList)) {
						twoList = new ArrayList<Privilege>();
					}
					twoList.add(privilege);
					twoMap.put(privilege.getParentid(), twoList);
				}
			}
		}
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		mc.setCache(Constants.PRE_FIRST_AUTHORITY+user.getOperid(), oneMenu, CacheFactory.DAY);//保存一级权限
		mc.setCache(Constants.PRE_SECOND_AUTHORITY+user.getOperid(), twoMap, CacheFactory.DAY);//保存二级权限集合
	}
	
}
