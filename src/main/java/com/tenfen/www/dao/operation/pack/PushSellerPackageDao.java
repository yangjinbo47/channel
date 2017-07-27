package com.tenfen.www.dao.operation.pack;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.pack.TPushSellerPackages;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class PushSellerPackageDao extends CustomHibernateDao<TPushSellerPackages, Long>{
	
	public void saveEntity(Integer sellerId, Integer packageId) {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_push_seller_packages set package_today=package_today+1 where seller_id="+sellerId+" and package_id="+packageId;
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void resetPackageToday() {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_push_seller_packages set package_today=0";
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 根据appid查询所属sellerId
	 * @param pid
	 * @return
	 */
	public List<Integer> findSellerByPackageId(Integer packageId) {
		Session session = null;
		List<Integer> list = null;
		try {
			session = getSession();
			String sql = "select seller_id from t_push_seller_packages t where t.package_id=:packageId";
			Query query = session.createSQLQuery(sql);
			query.setParameter("packageId", packageId);
			list = query.list();//sellerIds
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
}