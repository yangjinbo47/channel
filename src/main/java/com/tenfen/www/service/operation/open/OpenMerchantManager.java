package com.tenfen.www.service.operation.open;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.open.OpenMerchantDao;

@Component
@Transactional
public class OpenMerchantManager {
	
	@Autowired
	private OpenMerchantDao openMerchantDao;
	
	public Page<TOpenMerchant> findMerchantPage(final Page<TOpenMerchant> page) {
		Page<TOpenMerchant> merchantPage = openMerchantDao.getAll(page);
		return merchantPage;
	}
	
	public Page<TOpenMerchant> findMerchantPage(String merchantName, final Page<TOpenMerchant> page) {
		Page<TOpenMerchant> merchantPage = openMerchantDao.findMerchantByProperties(merchantName, page);
		return merchantPage;
	}
	
	public TOpenMerchant getOpenMerchantByProperty(String propertyName, Object value) {
		TOpenMerchant tOpenMerchant = null;
		try {
			List<TOpenMerchant> tOpenMerchants = openMerchantDao.findBy(propertyName, value);
			if (tOpenMerchants.size() > 0) {
				tOpenMerchant = tOpenMerchants.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenMerchant;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenMerchant entity) {
		openMerchantDao.save(entity);
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TOpenMerchant get(Integer id) {
		return openMerchantDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openMerchantDao.delete(id);
	}
	
}
