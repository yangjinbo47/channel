package com.tenfen.www.dao.operation.open;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public List<TOpenOrderHistory> getOrderList(Integer sellerId, Date startTime, Date endTime) {
		List<TOpenOrderHistory> list = null;
		try {
			String hql = "select t from TOpenOrderHistory t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("sellerId", sellerId);
			
			list = find(hql, map);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return list;
	}
	
	public List<TOpenOrderHistory> getSuccOrderList(Date startTime, Date endTime) {
		List<TOpenOrderHistory> list = null;
		try {
			String hql = "select t from TOpenOrderHistory t where t.createTime > :startTime and t.createTime < :endTime and t.status=3";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			list = find(hql, map);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return list;
	}
	
}