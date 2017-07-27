package com.tenfen.www.dao.system;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.system.TVisitLogTmp;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class VisitLogTmpDao extends CustomHibernateDao<TVisitLogTmp, Long>{
	
	public Page<TVisitLogTmp> getVisitTmpPage(Page<TVisitLogTmp> page, String clientVersion, Date startTime, Date endTime) {
		Page<TVisitLogTmp> visitLogTmpPage = null;
		try {
			String hql = "select t from TVisitLogTmp t where t.visitTime > :startTime and t.visitTime < :endTime and t.clientVersion=:clientVersion";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("clientVersion", clientVersion);
			
			visitLogTmpPage = findPage(page, hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return visitLogTmpPage;
	}
	
	public Page<TVisitLogTmp> getVisitTmpPage(Page<TVisitLogTmp> page, Date startTime, Date endTime) {
		Page<TVisitLogTmp> visitLogTmpPage = null;
		try {
			String hql = "select t from TVisitLogTmp t where t.visitTime > :startTime and t.visitTime < :endTime";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			visitLogTmpPage = findPage(page, hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return visitLogTmpPage;
	}
	
	public List<TVisitLogTmp> getVisitTmpList(String clientVersion,Date startTime,Date endTime) {
		List<TVisitLogTmp> list = null;
		try {
			String hql = "select t from TVisitLogTmp t where t.visitTime > :startTime and t.visitTime < :endTime and t.clientVersion=:clientVersion";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("clientVersion", clientVersion);
			
			list = find(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
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
			sql.append("select count(t) from TVisitLogTmp t where t.visitTime > :startTime and t.visitTime < :endTime");
			return (Long) createQuery(sql.toString(), map).uniqueResult();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			return 0l;
		}
	}
	
	public int delete(Date startTime,Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			
			String hql = "delete from TVisitLogTmp t where t.visitTime > :startTime and t.visitTime < :endTime";
			return batchExecute(hql, map);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
			return 0;
		}
	}
}