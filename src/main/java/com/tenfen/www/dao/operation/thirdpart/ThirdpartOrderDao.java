package com.tenfen.www.dao.operation.thirdpart;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class ThirdpartOrderDao extends CustomHibernateDao<TThirdOrder, Long>{
	
	public Page<TThirdOrder> getOrderPage(Page<TThirdOrder> page, Integer sellerId, Date startTime, Date endTime) {
		Page<TThirdOrder> orderPage = null;
		try {
			String hql = "select t from TThirdOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("sellerId", sellerId);
			
			orderPage = findPage(page, hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return orderPage;
	}
	
	public List<TThirdOrder> getOrderList(Integer sellerId, Date startTime, Date endTime) {
		List<TThirdOrder> list = null;
		try {
			String hql = "select t from TThirdOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId";
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
	
	/**
	 * 获取总数
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getCount(Date startTime,Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			StringBuffer sql = new StringBuffer(60);
			sql.append("select count(t) from TThirdOrder t where t.createTime > :startTime and t.createTime < :endTime");
			return (Long) createQuery(sql.toString(), map).uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			return 0l;
		}
	}
	
	/**
	 * 获取订单的费用
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getOrderFee(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("sellerId", sellerId);
			map.put("appId", appId);
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			StringBuffer sql = new StringBuffer(60);
			sql.append("select sum(fee) from TThirdOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId and t.appId=:appId and t.status=3");
			
			Object sum = createQuery(sql.toString(), map).uniqueResult();
			if (Utils.isEmpty(sum)) {
				return 0l;
			} else {
				return (Long) sum;
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			return 0l;
		}
	}
	
	/**
	 * 查询时间点之前的订单
	 * @param page
	 * @param beforeTime
	 * @return
	 */
	public Page<TThirdOrder> getOrderPageBeforeDate(Page<TThirdOrder> page, Date beforeTime) {
		Page<TThirdOrder> orderPage = null;
		try {
			String hql = "select t from TThirdOrder t where t.createTime < :beforeTime";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("beforeTime", beforeTime);
			
			orderPage = findPage(page, hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return orderPage;
	}
	
	/**
	 * 获取时间点前订单总数
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getOrderPageBeforeDateCount(Date beforeTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("beforeTime", beforeTime);
			
			StringBuffer sql = new StringBuffer(60);
			sql.append("select count(t) from TThirdOrder t where t.createTime < :beforeTime");
			return (Long) createQuery(sql.toString(), map).uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			return 0l;
		}
	}
	
	/**
	 * 删除时间点之前的订单
	 * @param beforeDate
	 */
	public void deleteOrderBeforeDate(Date beforeTime) {
		Session session = null;
		try {
			session = getSession();
			String sql = "delete from TThirdOrder t where t.createTime < :beforeTime";
			Query query = session.createQuery(sql);
			query.setParameter("beforeTime", beforeTime);
			query.executeUpdate();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}