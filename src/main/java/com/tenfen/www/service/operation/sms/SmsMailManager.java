package com.tenfen.www.service.operation.sms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.sms.TSmsMailer;
import com.tenfen.entity.operation.sms.TSmsMailgroup;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.sms.SmsMailerDao;
import com.tenfen.www.dao.operation.sms.SmsMailgroupDao;

@Component
@Transactional
public class SmsMailManager {
	
	@Autowired
	private SmsMailgroupDao smsMailgroupDao;
	@Autowired
	private SmsMailerDao smsMailerDao;
	@Autowired
	private CacheFactory cacheFactory;
	
	public TSmsMailgroup getSmsMailgroupByProperty(String propertyName, Object value) {
		TSmsMailgroup tSmsMailgroup = null;
		try {
			List<TSmsMailgroup> tSmsMailgroups = smsMailgroupDao.findBy(propertyName, value);
			if (tSmsMailgroups.size() > 0) {
				tSmsMailgroup = tSmsMailgroups.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsMailgroup;
	}
	
	public Page<TSmsMailgroup> findMailgroupPage(final Page<TSmsMailgroup> page) {
		Page<TSmsMailgroup> groupPage = smsMailgroupDao.getAll(page);
		return groupPage;
	}
	
	public Page<TSmsMailgroup> findMailgroupPage(String name, final Page<TSmsMailgroup> page) {
		Page<TSmsMailgroup> groupPage = smsMailgroupDao.findGroupByProperties(name, page);
		return groupPage;
	}
	
	public List<TSmsMailgroup> getGroupAll() {
		return smsMailgroupDao.getAll();
	}
	
	public List<TSmsMailer> getMailerAll() {
		return smsMailerDao.getAll();
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void saveGroup(TSmsMailgroup entity) {
		smsMailgroupDao.save(entity);
	}
	
	public void saveMailer(TSmsMailer entity) {
		smsMailerDao.save(entity);
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public TSmsMailgroup getGroup(Integer id) {
		return smsMailgroupDao.get(id);
	}
	
	public TSmsMailer getMailer(Integer id) {
		return smsMailerDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void deleteGroup(Integer id) {
		smsMailgroupDao.delete(id);
	}
	
	public void deleteMailer(Integer id) {
		smsMailerDao.delete(id);
	}
	
	public void deleteGroupMailer(Integer mailerId) {
		smsMailerDao.deleteGroupMailer(mailerId);
	}
	
}
