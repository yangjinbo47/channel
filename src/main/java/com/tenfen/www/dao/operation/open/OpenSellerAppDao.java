package com.tenfen.www.dao.operation.open;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenSellerAppDao extends CustomHibernateDao<TOpenSellerApps, Long>{
	
	public void saveEntity(Integer sellerId, Integer appId, Integer fee) {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_open_seller_apps set app_today=app_today+"+fee+" where seller_id="+sellerId+" and app_id="+appId;
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public void deleteOpenSellerApps(Integer sellerId, Integer appId) {
//		Session session = null;
//		try {
//			session = getSession();
//			String sql = "delete from t_open_seller_apps where seller_id="+sellerId+" and app_id="+appId;
//			Query query  = session.createSQLQuery(sql);
//			query.executeUpdate();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	public void resetAppToday() {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_open_seller_apps set app_today=0";
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
	public List<Integer> findSellerByAppId(Integer appId) {
		Session session = null;
		List<Integer> list = null;
		try {
			session = getSession();
			String sql = "select seller_id from t_open_seller_apps t where t.app_id=:appId";
			Query query = session.createSQLQuery(sql);
			query.setParameter("appId", appId);
			list = query.list();//sellerIds
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
}