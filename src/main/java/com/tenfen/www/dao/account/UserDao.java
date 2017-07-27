package com.tenfen.www.dao.account;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateDao;

import com.tenfen.entity.account.User;

/**
 * 用户对象的泛型DAO类.
 * 
 * @author BOBO
 */
@Component
public class UserDao extends HibernateDao<User, Integer> {
	
	public Page<User> getUsersByName(String name,final Page<User> page) {
		Page<User> userPage = null;
		try {
//			Criteria criteria = createCriteria();
//			criteria.add(Restrictions.ilike("loginName", "%"+name+"%"));
//			criteria.addOrder(Order.asc("operid"));
			userPage = findPage(page, Restrictions.ilike("loginName", "%"+name+"%"));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return userPage;
	}
}
