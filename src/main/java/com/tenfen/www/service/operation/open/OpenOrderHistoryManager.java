package com.tenfen.www.service.operation.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.entity.operation.open.TOpenOrderHistory;
import com.tenfen.www.dao.operation.open.OpenOrderHistoryDao;

@Component
@Transactional
public class OpenOrderHistoryManager {
	
	@Autowired
	private OpenOrderHistoryDao openOrderHistoryDao;

	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenOrderHistory entity) {
		openOrderHistoryDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TOpenOrderHistory get(Integer id) {
		return openOrderHistoryDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openOrderHistoryDao.delete(id);
	}
	
}
