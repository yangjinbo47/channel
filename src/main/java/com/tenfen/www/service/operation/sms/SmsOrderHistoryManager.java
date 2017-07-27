package com.tenfen.www.service.operation.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.entity.operation.sms.TSmsOrderHistory;
import com.tenfen.www.dao.operation.sms.SmsOrderHistoryDao;

@Component
@Transactional
public class SmsOrderHistoryManager {
	
	@Autowired
	private SmsOrderHistoryDao smsOrderHistoryDao;

	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsOrderHistory entity) {
		smsOrderHistoryDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TSmsOrderHistory get(Integer id) {
		return smsOrderHistoryDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsOrderHistoryDao.delete(id);
	}
	
}
