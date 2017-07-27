package com.tenfen.www.service.operation.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.entity.operation.open.TOpenOrderYiXin;
import com.tenfen.www.dao.operation.open.OpenOrderYiXinDao;

@Component
@Transactional
public class OpenOrderYiXinManager {
	
	@Autowired
	private OpenOrderYiXinDao openOrderYiXinDao;
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenOrderYiXin entity) {
		openOrderYiXinDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TOpenOrderYiXin get(Integer id) {
		return openOrderYiXinDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openOrderYiXinDao.delete(id);
	}
	
}
