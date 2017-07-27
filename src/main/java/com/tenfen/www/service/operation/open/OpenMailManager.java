package com.tenfen.www.service.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.open.TOpenMailer;
import com.tenfen.entity.operation.open.TOpenMailgroup;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.open.OpenMailerDao;
import com.tenfen.www.dao.operation.open.OpenMailgroupDao;

@Component
@Transactional
public class OpenMailManager {
	
	@Autowired
	private OpenMailgroupDao openMailgroupDao;
	@Autowired
	private OpenMailerDao openMailerDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TOpenMailgroup getOpenMailgroupByProperty(String propertyName, Object value) {
		TOpenMailgroup tOpenMailgroup = null;
		try {
			List<TOpenMailgroup> tOpenMailgroups = openMailgroupDao.findBy(propertyName, value);
			if (tOpenMailgroups.size() > 0) {
				tOpenMailgroup = tOpenMailgroups.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenMailgroup;
	}
	
	public Page<TOpenMailgroup> findMailgroupPage(final Page<TOpenMailgroup> page) {
		Page<TOpenMailgroup> groupPage = openMailgroupDao.getAll(page);
		return groupPage;
	}
	
	public Page<TOpenMailgroup> findMailgroupPage(String name, final Page<TOpenMailgroup> page) {
		Page<TOpenMailgroup> groupPage = openMailgroupDao.findGroupByProperties(name, page);
		return groupPage;
	}
	
	public List<TOpenMailgroup> getGroupAll() {
		return openMailgroupDao.getAll();
	}
	
	public List<TOpenMailer> getMailerAll() {
		return openMailerDao.getAll();
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void saveGroup(TOpenMailgroup entity) {
		openMailgroupDao.save(entity);
	}
	
	public void saveMailer(TOpenMailer entity) {
		openMailerDao.save(entity);
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TOpenMailgroup getGroup(Integer id) {
		return openMailgroupDao.get(id);
	}
	
	public TOpenMailer getMailer(Integer id) {
		return openMailerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void deleteGroup(Integer id) {
		openMailgroupDao.delete(id);
	}
	
	public void deleteMailer(Integer id) {
		openMailerDao.delete(id);
	}
	
	public void deleteGroupMailer(Integer mailerId) {
		openMailerDao.deleteGroupMailer(mailerId);
	}
	
}
