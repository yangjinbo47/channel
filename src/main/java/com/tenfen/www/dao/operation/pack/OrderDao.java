package com.tenfen.www.dao.operation.pack;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class OrderDao extends CustomHibernateDao<TOrder, Long>{
	
	public List<TOrder> findByPushId(Integer pushId) {
		List<TOrder> list = null;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pushId", pushId);
			
			String hql = "select t from TOrder t where t.pushId=:pushId";
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
//	public Long getCountByProperty(Integer pushId, String province, String channel, Date startTime, Date endTime) {
//		try {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("startTime", startTime);
//			map.put("endTime", endTime);
//			map.put("pushId", pushId);
//			map.put("channel", channel);
//			map.put("province", province);
//			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
//			
//			StringBuffer sql = new StringBuffer(60);
//			sql.append("select count(t) from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.channel=:channel and t.pushId=:pushId and t.province=:province and t.status=:status");
//			return (Long) createQuery(sql.toString(), map).uniqueResult();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			return 0l;
//		}
//	}
	
//	public Long getCountByProperty(Integer sellerId, Integer pushId, String province, Date startTime, Date endTime) {
//		try {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("startTime", startTime);
//			map.put("endTime", endTime);
//			map.put("sellerId", sellerId);
//			map.put("pushId", pushId);
//			map.put("province", province);
//			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
//			
//			StringBuffer sql = new StringBuffer(60);
//			sql.append("select count(t) from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId and t.pushId=:pushId and t.province=:province and t.status=:status");
//			return (Long) createQuery(sql.toString(), map).uniqueResult();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			return 0l;
//		}
//	}
	
//	public Long getCountByNameAndChannel(Integer pushId, String channel, Date startTime, Date endTime) {
//		try {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("startTime", startTime);
//			map.put("endTime", endTime);
//			map.put("channel", channel);
//			map.put("pushId", pushId);
//			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
//			
//			StringBuffer sql = new StringBuffer(60);
//			sql.append("select count(t) from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.channel=:channel and t.pushId=:pushId and t.status=:status");
//			return (Long) createQuery(sql.toString(), map).uniqueResult();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			return 0l;
//		}
//	}
	
	public Long getCountBySellerIdandPushId(Integer sellerId, Integer pushId, Date startTime, Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("sellerId", sellerId);
			map.put("pushId", pushId);
			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
			
			StringBuffer sql = new StringBuffer(60);
			sql.append("select count(t) from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId and t.pushId=:pushId and t.status=:status");
			return (Long) createQuery(sql.toString(), map).uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return 0l;
		}
	}
	
//	public Page<TOrder> getUserPage(Page<TOrder> page, String channel,Date startTime,Date endTime) {
//		Page<TOrder> packageUserPage = null;
//		try {
//			String hql = "select t from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.channel=:channel and t.status=:status";
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("startTime", startTime);
//			map.put("endTime", endTime);
//			map.put("channel", channel);
//			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
//			
//			packageUserPage = findPage(page, hql, map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return packageUserPage;
//	}
	
	public Page<TOrder> getOrderPage(Page<TOrder> page, Integer sellerId, Date startTime, Date endTime) {
		Page<TOrder> packageUserPage = null;
		try {
			String hql = "select t from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId and t.status=:status";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("sellerId", sellerId);
			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
			
			packageUserPage = findPage(page, hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return packageUserPage;
	}
	
//	public List<TOrder> getOrderList(String channel,Date startTime,Date endTime) {
//		List<TOrder> list = null;
//		try {
//			String hql = "select t from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.channel=:channel";
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("startTime", startTime);
//			map.put("endTime", endTime);
//			map.put("channel", channel);
////			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
//			
//			list = find(hql, map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return list;
//	}
	
	public List<TOrder> getOrderList(Date startTime, Date endTime) {
		List<TOrder> list = null;
		try {
			String hql = "select t from TOrder t where t.createTime > :startTime and t.createTime < :endTime";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
	public List<TOrder> getOrderList(Integer sellerId, Date startTime, Date endTime) {
		List<TOrder> list = null;
		try {
			String hql = "select t from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.sellerId=:sellerId and t.status=:status";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("sellerId", sellerId);
			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
			
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
	public Long getBaoyueCount(String phoneNum,Date startTime,Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("phoneNum", phoneNum);
			map.put("status", Constants.T_ORDER_STATUS.SUCCESS.getValue());
			
			StringBuffer sql = new StringBuffer(60);
			sql.append("select count(t) from TOrder t where t.createTime > :startTime and t.createTime < :endTime and t.phoneNum=:phoneNum and t.status=:status");
			return (Long) createQuery(sql.toString(), map).uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return 0l;
		}
	}
	
	//extra
	public List<TOrder> getOrderListByPushId(Integer pushId) {
		List<TOrder> list = null;
		try {
			String hql = "select t from TOrder t where t.pushId=:pushId";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pushId", pushId);
			
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
	
//	public List<TOrder> getOrderListByChannel(String channel) {
//		List<TOrder> list = null;
//		try {
//			String hql = "select t from TOrder t where t.channel=:channel";
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("channel", channel);
//			
//			list = find(hql, map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return list;
//	}
	
	public List<TOrder> findByPhoneAndPushId(String phone, Integer pushId) {
		List<TOrder> list = null;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pushId", pushId);
			map.put("phone", phone);
			
			String hql = "select t from TOrder t where t.phoneNum=:phone and t.pushId=:pushId order by t.id desc";
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return list;
	}
}
