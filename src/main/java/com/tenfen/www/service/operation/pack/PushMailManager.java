package com.tenfen.www.service.operation.pack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.pack.TPushMailer;
import com.tenfen.entity.operation.pack.TPushMailgroup;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.pack.PushMailerDao;
import com.tenfen.www.dao.operation.pack.PushMailgroupDao;

@Component
@Transactional
public class PushMailManager {
	
	@Autowired
	private PushMailgroupDao pushMailgroupDao;
	@Autowired
	private PushMailerDao pushMailerDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TPushMailgroup getPushMailgroupByProperty(String propertyName, Object value) {
		TPushMailgroup tPushMailgroup = null;
		try {
			List<TPushMailgroup> tPushMailgroups = pushMailgroupDao.findBy(propertyName, value);
			if (tPushMailgroups.size() > 0) {
				tPushMailgroup = tPushMailgroups.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tPushMailgroup;
	}
	
	public Page<TPushMailgroup> findMailgroupPage(final Page<TPushMailgroup> page) {
		Page<TPushMailgroup> groupPage = pushMailgroupDao.getAll(page);
		return groupPage;
	}
	
	public Page<TPushMailgroup> findMailgroupPage(String name, final Page<TPushMailgroup> page) {
		Page<TPushMailgroup> groupPage = pushMailgroupDao.findGroupByProperties(name, page);
		return groupPage;
	}
	
	public List<TPushMailgroup> getGroupAll() {
		return pushMailgroupDao.getAll();
	}
	
	public List<TPushMailer> getMailerAll() {
		return pushMailerDao.getAll();
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void saveGroup(TPushMailgroup entity) {
		pushMailgroupDao.save(entity);
	}
	
	public void saveMailer(TPushMailer entity) {
		pushMailerDao.save(entity);
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TPushMailgroup getGroup(Integer id) {
		return pushMailgroupDao.get(id);
	}
	
	public TPushMailer getMailer(Integer id) {
		return pushMailerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void deleteGroup(Integer id) {
		pushMailgroupDao.delete(id);
	}
	
	public void deleteMailer(Integer id) {
		pushMailerDao.delete(id);
	}
	
	public void deleteGroupMailer(Integer mailerId) {
		pushMailerDao.deleteGroupMailer(mailerId);
	}
	
}
