package com.tenfen.www.dao.operation.open;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.open.TOpenMailer;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenMailerDao extends CustomHibernateDao<TOpenMailer, Long>{
	
	public void deleteGroupMailer(Integer mailerId) {
		Session session = null;
		try {
			session = getSession();
			String sql = "delete from t_open_mailgroup_mailer where mail_id="+mailerId;
			Query query  = session.createSQLQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}