package com.tenfen.www.service.operation.thirdpart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdMerchant;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.thirdpart.ThirdpartMerchantDao;

@Component
@Transactional
public class ThirdpartMerchantManager {
	
	@Autowired
	private ThirdpartMerchantDao thirdpartMerchantDao;
	
	public Page<TThirdMerchant> findMerchantPage(final Page<TThirdMerchant> page) {
		Page<TThirdMerchant> merchantPage = thirdpartMerchantDao.getAll(page);
		return merchantPage;
	}
	
	public Page<TThirdMerchant> findMerchantPage(String merchantName, final Page<TThirdMerchant> page) {
		Page<TThirdMerchant> merchantPage = thirdpartMerchantDao.findMerchantByProperties(merchantName, page);
		return merchantPage;
	}
	
	public TThirdMerchant getThirdpartMerchantByProperty(String propertyName, Object value) {
		TThirdMerchant tThirdMerchant = null;
		try {
			List<TThirdMerchant> tSmsMerchants = thirdpartMerchantDao.findBy(propertyName, value);
			if (tSmsMerchants.size() > 0) {
				tThirdMerchant = tSmsMerchants.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tThirdMerchant;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TThirdMerchant entity) {
		thirdpartMerchantDao.save(entity);
	}
	
	/**
	 * 获取信息
	 * @param id
	 * @return
	 */
	public TThirdMerchant get(Integer id) {
		return thirdpartMerchantDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		thirdpartMerchantDao.delete(id);
	}
	
}
