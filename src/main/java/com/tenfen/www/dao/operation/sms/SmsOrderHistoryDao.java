package com.tenfen.www.dao.operation.sms;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.sms.TSmsOrderHistory;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SmsOrderHistoryDao extends CustomHibernateDao<TSmsOrderHistory, Long>{
	
	public List<TSmsOrderHistory> getOrderPage(int i) {
		List<TSmsOrderHistory> list = null;
		try {
			String sql = "from TSmsOrderHistory";
			Query query = createQuery(sql);
			query.setFirstResult(i);
			query.setMaxResults(100);
			
			list = query.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return list;
	}
	
	public TSmsOrderHistory getEntity(Integer id) {
		TSmsOrderHistory tSmsOrderHistory = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("id", id));
			
			List<TSmsOrderHistory> tSmsOrderHistories = criteria.list();
			if (tSmsOrderHistories.size() > 0) {
				tSmsOrderHistory = tSmsOrderHistories.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return tSmsOrderHistory;
	}
	
	
}