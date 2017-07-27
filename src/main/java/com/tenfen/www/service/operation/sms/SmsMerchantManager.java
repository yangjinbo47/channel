package com.tenfen.www.service.operation.sms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsMerchant;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.sms.SmsMerchantDao;

@Component
@Transactional
public class SmsMerchantManager {
	
	@Autowired
	private SmsMerchantDao smsMerchantDao;
	
	public Page<TSmsMerchant> findMerchantPage(final Page<TSmsMerchant> page) {
		Page<TSmsMerchant> merchantPage = smsMerchantDao.getAll(page);
		return merchantPage;
	}
	
	public Page<TSmsMerchant> findMerchantPage(String merchantName, final Page<TSmsMerchant> page) {
		Page<TSmsMerchant> merchantPage = smsMerchantDao.findMerchantByProperties(merchantName, page);
		return merchantPage;
	}
	
	public TSmsMerchant getSmsMerchantByProperty(String propertyName, Object value) {
		TSmsMerchant tSmsMerchant = null;
		try {
			List<TSmsMerchant> tSmsMerchants = smsMerchantDao.findBy(propertyName, value);
			if (tSmsMerchants.size() > 0) {
				tSmsMerchant = tSmsMerchants.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsMerchant;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsMerchant entity) {
		smsMerchantDao.save(entity);
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TSmsMerchant get(Integer id) {
		return smsMerchantDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsMerchantDao.delete(id);
	}
	
}
