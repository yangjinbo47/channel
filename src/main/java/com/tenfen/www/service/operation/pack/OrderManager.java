package com.tenfen.www.service.operation.pack;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.entity.operation.pack.TOrderSelf;
import com.tenfen.mongoEntity.MongoTOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.pack.OrderDao;
import com.tenfen.www.dao.operation.pack.OrderSelfDao;
import com.tenfen.www.mongodao.MongoTOrderDao;

@Component
@Transactional
public class OrderManager {
	
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private MongoTOrderDao mongoTOrderDao;
	@Autowired
	private OrderSelfDao orderSelfDao;
	
	private static final int POOL_SIZE = 20;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
	
//	private BatchUpdatePushPackage<TOrder> batch = null;
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
//	public Page<TOrder> getPackageUserPageByProperty(final Page<TOrder> page, String channel, Date start, Date end) {
//		Page<TOrder> packageUserPage = orderDao.getUserPage(page, channel, start, end);
//		return packageUserPage;
//	}
	
//	public List<TOrder> getOrderList(String channel,Date startTime,Date endTime) {
//		List<TOrder> list = orderDao.getOrderList(channel, startTime, endTime);
//		return list;
//	}
	
	/**
	 * 查询订购列表
	 * @param page
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Page<TOrder> getOrderPage(Page<TOrder> page, Integer sellerId, Date startTime, Date endTime) {
		Page<TOrder> packageUserPage = orderDao.getOrderPage(page, sellerId, startTime, endTime);
		return packageUserPage;
	}
	
	public List<TOrder> getOrderList(Integer sellerId,Date startTime,Date endTime) {
		List<TOrder> list = orderDao.getOrderList(sellerId, startTime, endTime);
		return list;
	}
	
	/**
	 * 根据号码查询订购记录总数
	 * @param page
	 * @param pageSize
	 * @param phone
	 * @return
	 */
	public Long getOrderCountByPhoneFromMongo(String phone) {
		return mongoTOrderDao.getOrderListByPhoneCount(phone);
	}
	
	public List<MongoTOrder> getOrderPageByPhoneFromMongo(int page, int pageSize, String phone) {
		return mongoTOrderDao.getOrderListByPhone(page, pageSize, phone);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOrder entity) {
		orderDao.save(entity);
		//同步至mongo
		try {
			if (systemProperty.getIsSaveToMongo()) {
				exe.execute(new MongoThread(entity));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void saveToSelf(TOrder entity) {
		try {
			TOrderSelf tOrderSelf = new TOrderSelf();
			BeanUtils.copyProperties(tOrderSelf, entity);
			tOrderSelf.setStatus(3);
			orderSelfDao.save(tOrderSelf);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public class MongoThread implements Runnable {
		
		private MongoTOrder mongoTOrder;
		
		public MongoThread(TOrder tOrder) {
			try {
				mongoTOrder = new MongoTOrder();
				mongoTOrder.setTradeId(tOrder.getTradeId());
				mongoTOrder.setOutTradeNo(tOrder.getOutTradeNo());
				mongoTOrder.setImsi(tOrder.getImsi());
				mongoTOrder.setPhoneNum(tOrder.getPhoneNum());
				mongoTOrder.setSellerId(tOrder.getSellerId());
				mongoTOrder.setPushId(tOrder.getPushId());
				mongoTOrder.setFee(tOrder.getFee());
				mongoTOrder.setStatus(tOrder.getStatus());
				mongoTOrder.setCreateTime(tOrder.getCreateTime());
				mongoTOrder.setName(tOrder.getName());
//				mongoTOrder.setChannel(tOrder.getChannel());
				mongoTOrder.setProvince(tOrder.getProvince());
				mongoTOrder.setReduce(tOrder.getReduce());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTOrderDao.saveAndUpdate(mongoTOrder);
		}
	}
	/**
	 * 批量保存
	 */
//	public void batchSave(TOrder entity) {
//		if (batch == null) {
//			batch = new BatchUpdatePushPackage<TOrder>(orderDao, Constants.TORDER_BATCHSAVEENTITY);
//		}
//		batch.batchSaveEntity(entity);
//	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
	public TOrder get(Integer id) {
		return orderDao.get(id);
	}
	
	/**
	 * 根据tradeId获取order
	 * @param tradeId
	 * @return
	 */
	public TOrder getByTradeId(String tradeId){
		List<TOrder> list = orderDao.findBy("tradeId", tradeId);
		TOrder entity = null;
		if (list.size() > 0) {
			entity = list.get(0);
		}
		return entity;
	}
	
	public TOrder findByPhoneAndPushId(String phone, Integer pushId){
		List<TOrder> list = orderDao.findByPhoneAndPushId(phone, pushId);
		TOrder entity = null;
		if (list.size() > 0) {
			entity = list.get(list.size() - 1);
		}
		return entity;
	}
	
	public TOrder findByImsiAndPushId(String imsi, Integer pushId){
		List<TOrder> list = orderDao.findByImsiAndPushId(imsi, pushId);
		TOrder entity = null;
		if (list.size() > 0) {
			entity = list.get(list.size() - 1);
		}
		return entity;
	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(TOrder entity) {
		orderDao.delete(entity);
	}
	
	/**
	 * 获取用户包月数量
	 * @param phoneNum
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getBaoyueCount(String phoneNum,Date startTime,Date endTime) {
		return orderDao.getBaoyueCount(phoneNum, startTime, endTime);
	}
	
	/**
	 * 根据渠道省份来获取包月数量
	 * @param province
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @return
	 */
//	public int getBaoyueCount(Integer pushId, String province, String channel, Date startTime, Date endTime) {
//		long l = (long)orderDao.getCountByProperty(pushId, province, channel, startTime, endTime);
//		return (int)l;
//	}
	public int getPackageCount(Integer pushId, String province, Date startTime, Date endTime, Integer status) {
		Integer count = 0;
		Map<Integer, String> map = mongoTOrderDao.packageCount(startTime, endTime, status);
		String jsonStr = map.get(pushId);
		if (!Utils.isEmpty(jsonStr)) {
			JSONObject json = JSONObject.parseObject(jsonStr);
			count = json.getInteger("count") == null ? 0 : json.getInteger("count");
		}
		return count;
	}
	
	/**
	 * 根据包月名和渠道来获取包月数量
	 * @param name
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long getCountBySellerIdandPushId(Integer sellerId, Integer pushId, Date startTime, Date endTime) {
		return orderDao.getCountBySellerIdandPushId(sellerId, pushId, startTime, endTime);
	}
	
	/**
	 * 每个包月包推广量
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<Integer, String> mapReducePushIds(Integer sellerId, Date startTime, Date endTime, Integer status) {
		return mongoTOrderDao.mapReducePushIds(sellerId, startTime, endTime, status);
	}
	
	/**
	 * 渠道分省推广量
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<String, Map<Integer, String>> mapReduceProvince(Integer sellerId, Date startTime, Date endTime) {
		return mongoTOrderDao.mapReduceProvince(sellerId, startTime, endTime);
	}
	/**
	 * 渠道各包分省推广量
	 * @param channel
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<String, Map<Integer, String>> mapReduceProvince(Integer sellerId, Integer pushId, Date startTime, Date endTime) {
		return mongoTOrderDao.mapReduceProvince(sellerId, pushId, startTime, endTime);
	}
	
	/**
	 * mapreduce出渠道数据（mo、mo去重量、mr、信息费）
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<Integer, Map<Integer, String>> mapReduceSeller(Date startTime, Date endTime) {
		return mongoTOrderDao.mapReduceSeller(startTime, endTime);
	}
}
