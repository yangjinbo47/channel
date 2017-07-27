package com.tenfen.www.service.system;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.system.TVisitLog;
import com.tenfen.www.dao.system.VisitLogDao;

@Component
@Transactional
public class VisitLogManager {
	
	@Autowired
	private VisitLogDao visitLogDao;
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TVisitLog> getVisitLogPageByProperty(final Page<TVisitLog> page, String clientVersion, Date start, Date end) {
		Page<TVisitLog> visitLogPage = visitLogDao.getVisitPage(page, clientVersion, start, end);
		return visitLogPage;
	}
	
	public List<TVisitLog> getUserList(String clientVersion, Date startTime, Date endTime) {
		List<TVisitLog> list = visitLogDao.getVisitList(clientVersion, startTime, endTime);
		return list;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TVisitLog entity) {
		visitLogDao.save(entity);
	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
	public TVisitLog get(Integer id) {
		return visitLogDao.get(id);
	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(TVisitLog entity) {
		visitLogDao.delete(entity);
	}
	
}
