package com.tenfen.www.dao.system;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springside.modules.orm.Page;

import com.tenfen.entity.system.TVisitLog;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class VisitLogDao extends CustomHibernateDao<TVisitLog, Long>{
	
	public Page<TVisitLog> getVisitPage(Page<TVisitLog> page, String clientVersion, Date startTime, Date endTime) {
		Page<TVisitLog> visitLogPage = null;
		try {
			String hql = "select t from TVisitLog t where t.visitTime > :startTime and t.visitTime < :endTime and t.clientVersion=:clientVersion";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("clientVersion", clientVersion);
			
			visitLogPage = findPage(page, hql, map);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return visitLogPage;
	}
	
	public List<TVisitLog> getVisitList(String clientVersion,Date startTime,Date endTime) {
		List<TVisitLog> list = null;
		try {
			String hql = "select t from TVisitLog t where t.visitTime > :startTime and t.visitTime < :endTime and t.clientVersion=:clientVersion";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			map.put("clientVersion", clientVersion);
			
			list = find(hql, map);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return list;
	}
}