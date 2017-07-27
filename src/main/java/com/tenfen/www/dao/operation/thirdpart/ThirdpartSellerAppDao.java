package com.tenfen.www.dao.operation.thirdpart;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.thirdpart.TThirdSellerApps;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class ThirdpartSellerAppDao extends CustomHibernateDao<TThirdSellerApps, Long>{
	
	public void saveEntity(Integer sellerId, Integer appId, Integer fee) {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_thirdpart_seller_apps set app_today=app_today+"+fee+" where seller_id="+sellerId+" and app_id="+appId;
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void resetAppToday() {
		Session session = null;
		try {
			session = getSession();
			String sql = "update t_thirdpart_seller_apps set app_today=0";
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}