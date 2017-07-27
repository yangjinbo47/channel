package com.tenfen.www.dao.account;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.hibernate.HibernateDao;

import com.tenfen.entity.account.Privilege;
import com.tenfen.www.common.Constants;

/**
 * 授权对象的泛型DAO.
 * 
 * @author BOBO
 */
@Component
@SuppressWarnings("unchecked")
public class PrivilegeDao extends HibernateDao<Privilege, Integer> {
	
	public Integer saveEntity(Privilege entity) {
		Session session = getSession();
		Integer result = (Integer) session.save(entity);
		return result;
	}
	
	/**
	 * 获取一级权限的最大order
	 * @return
	 * @author BOBO
	 */
	public Integer getFirstLevelMaxOrder() {
		Integer maxOrder = 0;
		try {
			String sql = "select max(menuorder) from Privilege where menulevel=1";
			Query query = getSession().createQuery(sql);
			Object obj = query.uniqueResult();
			if (obj != null) {
				maxOrder = (Integer)obj;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return maxOrder;
	}
	
	/**
	 * 获取二级权限的最大order
	 * @return
	 * @author BOBO
	 */
	public Integer getSecondLevelMaxOrder(Integer parentId) {
		Integer maxOrder = 0;
		try {
			String sql = "select max(menuorder) from Privilege where parentid=:parentid and menulevel=2";
			Query query = getSession().createQuery(sql);
			query.setParameter("parentid", parentId);
			Object obj = query.uniqueResult();
			if (obj != null) {
				maxOrder = (Integer)obj;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return maxOrder;
	}
	
	/**
	 * 更新权限顺序
	 * @param privilegeid
	 * @param menuorder
	 * @return
	 */
	public Integer updateOrder(Integer privilegeid, Integer menuorder) {
		String hql = "update Privilege t set t.menuorder=:menuorder where t.privilegeid=:privilegeid";
		Query query = getSession().createQuery(hql);
		query.setParameter("menuorder", menuorder);
		query.setParameter("privilegeid", privilegeid);
        return query.executeUpdate();
	}
	
	/**
	 * 删除角色权限关联表
	 * @param privilegeid
	 * @return
	 */
	public Integer deleteRolePrivilege(Integer privilegeid) {
		String sql = "delete from t_frame_role_privilege where privilegeid=:privilegeid";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("privilegeid", privilegeid);
        return query.executeUpdate();
	}
	
	/**
	 * 查询该父节点下的孩子节点个数
	 * @param parentId
	 * @return
	 */
	public Integer getChildrenNum(Integer parentId) {
		Integer count = 0;
		try {
			String sql = "select count(*) from t_frame_privilege where parentid=:parentid and menulevel=2";
			Query query = getSession().createSQLQuery(sql);
			query.setParameter("parentid", parentId);
			count = ((Number)query.uniqueResult()).intValue();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return count;
	}
	
	/**
	 * 获取所有一级权限列表
	 * @return
	 */
	public List<Privilege> getAllFirstPrivilege() {
		List<Privilege> privilegeList = new ArrayList<Privilege>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("status", Constants.PRIVILEGE_STATUS.NORMAL.getValue()));
			criteria.add(Restrictions.eq("menulevel", 1));
			criteria.addOrder(Order.asc("menuorder"));
			
			privilegeList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return privilegeList;
	}
	
	/**
	 * 获取二级权限列表
	 * @return
	 */
	public List<Privilege> getSecondPrivilege(Integer parentId) {
		List<Privilege> privilegeList = new ArrayList<Privilege>();
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("status", Constants.PRIVILEGE_STATUS.NORMAL.getValue()));
			criteria.add(Restrictions.eq("menulevel", 2));
			criteria.add(Restrictions.eq("parentid", parentId));
			criteria.addOrder(Order.asc("menuorder"));
			
			privilegeList = criteria.list();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return privilegeList;
	}
}
