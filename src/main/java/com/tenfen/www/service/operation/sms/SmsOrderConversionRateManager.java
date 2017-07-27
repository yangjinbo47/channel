package com.tenfen.www.service.operation.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.sms.TSmsOrderConversionrate;
import com.tenfen.www.dao.operation.sms.SmsOrderConversionRateDao;

@Component
@Transactional
public class SmsOrderConversionRateManager {
	
	@Autowired
	private SmsOrderConversionRateDao smsOrderConversionRateDao;
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TSmsOrderConversionrate> findListByProperties(Integer sellerId) {
		Page<TSmsOrderConversionrate> page = smsOrderConversionRateDao.findListByProperties(sellerId);
		return page;
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsOrderConversionrate entity) {
		smsOrderConversionRateDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TSmsOrderConversionrate get(Integer id) {
		return smsOrderConversionRateDao.get(id);
	}
	
	public TSmsOrderConversionrate getEntity(Integer year, Integer month, Integer day, Integer hour, Integer sellerId) {
		return smsOrderConversionRateDao.getEntity(year, month, day, hour, sellerId);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsOrderConversionRateDao.delete(id);
	}
	
}
