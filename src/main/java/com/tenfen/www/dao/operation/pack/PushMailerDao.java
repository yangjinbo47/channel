package com.tenfen.www.dao.operation.pack;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.pack.TPushMailer;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class PushMailerDao extends CustomHibernateDao<TPushMailer, Long>{
	
	public void deleteGroupMailer(Integer mailerId) {
		Session session = null;
		try {
			session = getSession();
			String sql = "delete from t_push_mailgroup_mailer where mail_id="+mailerId;
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}