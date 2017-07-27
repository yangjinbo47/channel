package com.tenfen.www.dao.account;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.hibernate.HibernateDao;

import com.tenfen.entity.account.Role;
import com.tenfen.entity.account.User;

/**
 * 角色对象的泛型DAO.
 * 
 * @author BOBO
 */
@Component
public class RoleDao extends HibernateDao<Role, Integer> {
	
	private static final String QUERY_USER_BY_ROLEID = "select u from User u left join u.roleList r where r.roleid=?";
	
	/**
	 * 重载函数,因为Role中没有建立与User的关联,因此需要以较低效率的方式进行删除User与Role的多对多中间表.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void delete(Integer id) {
		Role role = get(id);
		//查询出拥有该角色的用户,并删除该用户的角色.
		List<User> users = createQuery(QUERY_USER_BY_ROLEID, role.getRoleid()).list();
		for (User u : users) {
			u.getRoleList().remove(role);
		}
		super.delete(role);
	}
	
	@SuppressWarnings("unchecked")
	public List<Role> getRolesByName(String name) {
		List<Role> roleList = new ArrayList<Role>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.ilike("name", "%"+name+"%"));
			criteria.addOrder(Order.asc("roleid"));
			
			roleList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return roleList;
	}
}
