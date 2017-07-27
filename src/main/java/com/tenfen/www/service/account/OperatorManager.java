package com.tenfen.www.service.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.account.User;
import com.tenfen.www.dao.account.UserDao;

@Component
@Transactional
public class OperatorManager {
	
	@Autowired
	private UserDao userDao;
	
	/**
	 * 查询正常状态的用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<User> getUserList(final Page<User> page) {
//		Page<User> userPage = userDao.findPage(page, Restrictions.eq("status", Constants.OPERATOR_STATUS.NORMAL.getValue()));
		Page<User> userPage = userDao.getAll(page);
		return userPage;
	}
	
	/**
	 * 根据名字查询用户
	 * @param name
	 * @return
	 */
	public Page<User> getUsersByName(String name, final Page<User> page) {
		Page<User> userPage = userDao.getUsersByName(name, page);
		return userPage;
	}
	
}
