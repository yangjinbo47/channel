package com.tenfen.www.service.account;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.entity.account.Privilege;
import com.tenfen.entity.account.Role;
import com.tenfen.www.dao.account.PrivilegeDao;
import com.tenfen.www.dao.account.RoleDao;

@Component
@Transactional
public class RoleManager {
	
	@Autowired
	private PrivilegeDao privilegeDao;
	@Autowired
	private RoleDao roleDao;
	
	/**
	 * 获取所有的一级权限
	 * @return
	 * @author BOBO
	 */
	public List<Privilege> getAllFirstPrivilege() {
		return privilegeDao.getAllFirstPrivilege();
	}
	
	/**
	 * 获取所有的二级权限
	 * @param nid
	 * @return
	 */
	public List<Privilege> getSecondPrivilege(Integer parentId) {
		return privilegeDao.getSecondPrivilege(parentId);
	}
	
	/**
	 * 根据名字查询角色
	 * @param name
	 * @return
	 */
	public List<Role> getRolesByName(String name) {
		return roleDao.getRolesByName(name);
	}
	
}
