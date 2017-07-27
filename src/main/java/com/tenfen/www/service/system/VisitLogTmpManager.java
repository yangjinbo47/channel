package com.tenfen.www.service.system;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.system.TVisitLogTmp;
import com.tenfen.www.dao.system.VisitLogTmpDao;

@Component
@Transactional
public class VisitLogTmpManager {
	
	@Autowired
	private VisitLogTmpDao visitLogTmpDao;
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TVisitLogTmp> getVisitLogTmpPageByProperty(final Page<TVisitLogTmp> page, String clientVersion, Date start, Date end) {
		Page<TVisitLogTmp> visitLogTmpPage = visitLogTmpDao.getVisitTmpPage(page, clientVersion, start, end);
		return visitLogTmpPage;
	}
	
	/**
	 * 查询当日所有记录
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TVisitLogTmp> getVisitLogTmpPageByProperty(final Page<TVisitLogTmp> page, Date start, Date end) {
		Page<TVisitLogTmp> visitLogTmpPage = visitLogTmpDao.getVisitTmpPage(page, start, end);
		return visitLogTmpPage;
	}
	
	public List<TVisitLogTmp> getUserList(String clientVersion, Date startTime, Date endTime) {
		List<TVisitLogTmp> list = visitLogTmpDao.getVisitTmpList(clientVersion, startTime, endTime);
		return list;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TVisitLogTmp entity) {
		visitLogTmpDao.save(entity);
	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
	public TVisitLogTmp get(Integer id) {
		return visitLogTmpDao.get(id);
	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(TVisitLogTmp entity) {
		visitLogTmpDao.delete(entity);
	}
	
	/**
	 * 获取总数
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getCount(Date startTime,Date endTime) {
		return visitLogTmpDao.getCount(startTime, endTime);
	}
	
	/**
	 * 删除时间区间的记录
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public int delete(Date startTime,Date endTime) {
		return visitLogTmpDao.delete(startTime, endTime);
	}
	
}
