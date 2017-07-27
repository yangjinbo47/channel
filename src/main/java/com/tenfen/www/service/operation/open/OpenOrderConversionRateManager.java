package com.tenfen.www.service.operation.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.open.TOpenOrderConversionrate;
import com.tenfen.www.dao.operation.open.OpenOrderConversionRateDao;

@Component
@Transactional
public class OpenOrderConversionRateManager {
	
	@Autowired
	private OpenOrderConversionRateDao openOrderConversionRateDao;
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TOpenOrderConversionrate> findListByProperties(Integer sellerId) {
		Page<TOpenOrderConversionrate> page = openOrderConversionRateDao.findListByProperties(sellerId);
		return page;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenOrderConversionrate entity) {
		openOrderConversionRateDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TOpenOrderConversionrate get(Integer id) {
		return openOrderConversionRateDao.get(id);
	}
	
	public TOpenOrderConversionrate getEntity(Integer year, Integer month, Integer day, Integer hour, Integer sellerId) {
		return openOrderConversionRateDao.getEntity(year, month, day, hour, sellerId);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openOrderConversionRateDao.delete(id);
	}
	
}
