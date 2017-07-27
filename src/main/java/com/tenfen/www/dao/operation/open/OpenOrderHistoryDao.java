package com.tenfen.www.dao.operation.open;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.tenfen.entity.operation.open.TOpenOrderHistory;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OpenOrderHistoryDao extends CustomHibernateDao<TOpenOrderHistory, Long>{
	
	public List<TOpenOrderHistory> getOrderPage(int i) {
		List<TOpenOrderHistory> list = null;
		try {
			String sql = "from TOpenOrderHistory";
			Query query = createQuery(sql);
			query.setFirstResult(i);
			query.setMaxResults(100);
			
			list = query.list();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return list;
	}
	
	public TOpenOrderHistory getEntity(Integer id) {
		TOpenOrderHistory tOpenOrderHistory = null;
		try {
			Criteria criteria = createCriteria();
			criteria.add(Restrictions.eq("id", id));
			
			List<TOpenOrderHistory> tOpenOrderHistories = criteria.list();
			if (tOpenOrderHistories.size() > 0) {
				tOpenOrderHistory = tOpenOrderHistories.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return tOpenOrderHistory;
	}
	
	
}