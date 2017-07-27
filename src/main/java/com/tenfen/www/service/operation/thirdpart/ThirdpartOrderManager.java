package com.tenfen.www.service.operation.thirdpart;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.thirdpart.TThirdOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.www.dao.operation.thirdpart.ThirdpartOrderDao;

@Component
@Transactional
public class ThirdpartOrderManager {
	
	@Autowired
	private ThirdpartOrderDao thirdpartOrderDao;
	
	public TThirdOrder getOrderByProperty(String propertyName, Object value) {
		TThirdOrder tThirdOrder = null;
		try {
			List<TThirdOrder> tThirdOrders = thirdpartOrderDao.findBy(propertyName, value);
			if (tThirdOrders.size() > 0) {
				tThirdOrder = tThirdOrders.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tThirdOrder;
	}
	
	/**
	 * 查询订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TThirdOrder> getOrderPageByProperty(final Page<TThirdOrder> page, Integer sellerId, String payPhone, Date start, Date end) {
		Page<TThirdOrder> orderPage = thirdpartOrderDao.getOrderPage(page, sellerId, start, end);
		return orderPage;
	}
	
	public List<TThirdOrder> getOrderList(Integer sellerId, Date startTime, Date endTime) {
		List<TThirdOrder> list = thirdpartOrderDao.getOrderList(sellerId, startTime, endTime);
		return list;
	}
	
	/**
	 * 获取订单的费用
	 * @param sellerId
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getOrderFee(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		return thirdpartOrderDao.getOrderFee(sellerId, appId, startTime, endTime);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TThirdOrder entity) {
		thirdpartOrderDao.save(entity);
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TThirdOrder get(Integer id) {
		return thirdpartOrderDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		thirdpartOrderDao.delete(id);
	}
	
	/**
	 * 获取总数
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getCount(Date startTime,Date endTime) {
		return thirdpartOrderDao.getCount(startTime, endTime);
	}
	
	/**
	 * 查询时间点之前的订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TThirdOrder> getOrderPageBeforeDate(final Page<TThirdOrder> page, Date beforeDate) {
		Page<TThirdOrder> orderPage = thirdpartOrderDao.getOrderPageBeforeDate(page, beforeDate);
		return orderPage;
	}
	
	/**
	 * 获取时间点之前的订单总数
	 * @param beforeDate
	 * @return
	 */
	public Long getOrderPageBeforeDateCount(Date beforeDate) {
		return thirdpartOrderDao.getOrderPageBeforeDateCount(beforeDate);
	}
	
	/**
	 * 删除时间点之前的订单
	 * @param beforeDate
	 */
	public void deleteOrderBeforeDate(Date beforeDate) {
		thirdpartOrderDao.deleteOrderBeforeDate(beforeDate);
	}
	
}
