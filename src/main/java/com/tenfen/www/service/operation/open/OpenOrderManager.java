package com.tenfen.www.service.operation.open;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.mongoEntity.MongoTOpenOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.open.OpenOrderDao;
import com.tenfen.www.dao.operation.open.OpenSellerDao;
import com.tenfen.www.mongodao.MongoTOpenOrderDao;
import com.tenfen.www.util.sendToBj.SendOpenToBJ;

@Component
@Transactional
public class OpenOrderManager {
	
	private static final int POOL_SIZE = 20;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
	
	@Autowired
	private OpenOrderDao openOrderDao;
	@Autowired
	private OpenSellerDao openSellerDao;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private MongoTOpenOrderDao mongoTOpenOrderDao;
	
	public TOpenOrder getOpenOrderByProperty(String propertyName, Object value) {
		TOpenOrder tOpenOrder = null;
		try {
			List<TOpenOrder> tOpenOrders = openOrderDao.findBy(propertyName, value);
			Collections.reverse(tOpenOrders);
			if (tOpenOrders.size() > 0) {
				tOpenOrder = tOpenOrders.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tOpenOrder;
	}
	
	/**
	 * 查询订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
//	public Page<TOpenOrder> getOrderPageByProperty(final Page<TOpenOrder> page, Integer sellerId, String payPhone, Date start, Date end) {
//		Page<TOpenOrder> orderPage = null;
//		if (Utils.isEmpty(payPhone)) {
//			orderPage = openOrderDao.getOrderPage(page, sellerId, start, end);
//		} else {
//			orderPage = openOrderDao.getOrderPage(page, payPhone, sellerId, start, end);
//		}
//		return orderPage;
//	}
	
	/**
	 * 根据号码查询订购列表
	 * @param page
	 * @param phone
	 * @return
	 */
//	public Page<TOpenOrder> getOrderPageByPhone(final Page<TOpenOrder> page, String phone) {
//		Page<TOpenOrder> orderPage = null;
//		if (!Utils.isEmpty(phone)) {
//			orderPage = openOrderDao.getOrderPageByPhone(page, phone);
//		}
//		return orderPage;
//	}
	
	/**
	 * 查询订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public List<MongoTOpenOrder> getOrderListFromMongo(int page, int pageSize, Integer sellerId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.getOrderList(page, pageSize, sellerId, startTime, endTime);
	}
	
	/**
	 * 查询订购总数
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Long getOrderListFromMongoCount(Integer sellerId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.getOrderListCount(sellerId, startTime, endTime);
	}
	
	/**
	 * 根据号码查询订购记录
	 * @param page
	 * @param pageSize
	 * @param phone
	 * @return
	 */
	public List<MongoTOpenOrder> getOrderPageByPhoneFromMongo(int page, int pageSize, String phone) {
		return mongoTOpenOrderDao.getOrderListByPhone(page, pageSize, phone);
	}
	
	/**
	 * 根据号码查询订购记录总数
	 * @param page
	 * @param pageSize
	 * @param phone
	 * @return
	 */
	public Long getOrderPageByPhoneFromMongoCount(String phone) {
		return mongoTOpenOrderDao.getOrderListByPhoneCount(phone);
	}
	
//	public List<TOpenOrder> getOrderList(Integer sellerId, String payPhone, Date startTime, Date endTime) {
//		List<TOpenOrder> list = new ArrayList<TOpenOrder>();
//		if (Utils.isEmpty(payPhone)) {
//			list = openOrderDao.getOrderList(sellerId, startTime, endTime);
//		} else {
//			list = openOrderDao.getOrderList(sellerId, payPhone, startTime, endTime);
//		}
//		
//		return list;
//	}
	
	public List<MongoTOpenOrder> getOrderListFromMongo(Integer sellerId, String payPhone, Date startTime, Date endTime) {
		List<MongoTOpenOrder> list = new ArrayList<MongoTOpenOrder>();
		if (Utils.isEmpty(payPhone)) {
			list = mongoTOpenOrderDao.getOrderList(sellerId, startTime, endTime);
		} else {
			list = mongoTOpenOrderDao.getOrderList(sellerId, payPhone, startTime, endTime);
		}
		return list;
	}
	
	/**
	 * 查询需要同步给数据
	 * @param merchantId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<TOpenOrder> getOrderListByMerchantId(Integer merchantId, Date startTime, Date endTime) {
		List<TOpenOrder> list = openOrderDao.getOrderListByMerchantId(merchantId, startTime, endTime);
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
		return openOrderDao.getOrderFee(sellerId, appId, startTime, endTime);
	}
	
	/**
	 * mapReduce sellerId下phone的记录数和信息费
	 * @param sellerId
	 * @param phone
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Integer getSumFeeByPhone(Integer sellerId, String phone, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.getSumFeeByPhone(sellerId, phone, startTime, endTime);
	}
	
	/**
	 * mapReduce sellerId下所有省份的信息费和记录数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<String, String> getProvinceCountBySellerId(Integer sellerId, Date startTime, Date endTime, String status) {
		return mongoTOpenOrderDao.mapReduceProvinceBySellerId(sellerId, startTime, endTime, status);
	}
	
	/**
	 * mapReduce sellerId,appId下所有省份的信息费和记录数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<String, String> mapReduceProvinceBySellerIdAndAppId(Integer sellerId, Integer appId, Date startTime, Date endTime, String status) {
		return mongoTOpenOrderDao.mapReduceProvinceBySellerIdAndAppId(sellerId, appId, startTime, endTime, status);
	}
	
	/**
	 * mapReduce appId下所有省份的信息费和记录数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<String, String> getProvinceCountByAppId(Integer appId, Date startTime, Date endTime, String status) {
		return mongoTOpenOrderDao.mapReduceProvinceByAppId(appId, startTime, endTime, status);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TOpenOrder entity) {
		openOrderDao.save(entity);
		try {
			//同步至mongo
			if (systemProperty.getIsSaveToMongo()) {
				exe.execute(new MongoThread(entity));
			}
			//同步至北京平台
			if (systemProperty.getIsSaveToBeijing()) {
				Integer sellerId = entity.getSellerId();
				TOpenSeller openSeller = openSellerDao.get(sellerId);
				Integer companyShow = openSeller.getCompanyShow();
				if ("3".equals(entity.getStatus()) && companyShow==1) {
					exe.execute(new SendOpenToBJ(entity));
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public class HbaseThread implements Runnable {
//		private TOpenOrder tOpenOrder;
//		
//		public HbaseThread(TOpenOrder tOpenOrder) {
//			this.tOpenOrder = tOpenOrder;
//		}
//	
//		@Override
//		public void run() {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String createDay = sdf.format(tOpenOrder.getCreateTime());
//			String rowKey = createDay+"_"+tOpenOrder.getOrderId();
//			String sellerId = String.valueOf(tOpenOrder.getSellerId());
//			String appId = String.valueOf(tOpenOrder.getAppId());
//			String merchantId = String.valueOf(tOpenOrder.getMerchantId());
//			
//			String orderId = tOpenOrder.getOrderId();
//			String imsi = tOpenOrder.getImsi();
//			String outTradeNo = tOpenOrder.getOutTradeNo();
//			String subject = tOpenOrder.getSubject();
//			String sendNumber = tOpenOrder.getSenderNumber();
//			String msgContent = tOpenOrder.getMsgContent();
//			String createTime = sdf1.format(tOpenOrder.getCreateTime());
//			String fee = String.valueOf(tOpenOrder.getFee());
//			String status = tOpenOrder.getStatus();
//			
//			String payPhone = tOpenOrder.getPayPhone() == null ? "" : tOpenOrder.getPayPhone();
//			String payTime = "";
//			if (tOpenOrder.getPayTime() != null) {
//				payTime = sdf1.format(tOpenOrder.getPayTime());
//			}
//			String province = tOpenOrder.getProvince() == null ? "" : tOpenOrder.getProvince();
//			
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "seller_id", sellerId);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "app_id", appId);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "merchant_id", merchantId);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "imsi", imsi);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "order_id", orderId);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "out_trade_no", outTradeNo);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "subject", subject);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "send_number", sendNumber);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "msg_content", msgContent);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "create_time", createTime);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "fee", fee);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "status", status);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "pay_phone", payPhone);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "pay_time", payTime);
//			hbaseHelper.writeRecord("t_open_order", rowKey, "order_info", "province", province);
//		}
//	}
	
	public class MongoThread implements Runnable {
		
		private MongoTOpenOrder mongoTOpenOrder;
		
		public MongoThread(TOpenOrder tOpenOrder) {
			try {
				mongoTOpenOrder = new MongoTOpenOrder();
				mongoTOpenOrder.setImsi(tOpenOrder.getImsi());
				mongoTOpenOrder.setOrderId(tOpenOrder.getOrderId());
				mongoTOpenOrder.setOutTradeNo(tOpenOrder.getOutTradeNo());
				mongoTOpenOrder.setSellerId(tOpenOrder.getSellerId());
				mongoTOpenOrder.setAppId(tOpenOrder.getAppId());
				mongoTOpenOrder.setMerchantId(tOpenOrder.getMerchantId());
				mongoTOpenOrder.setSubject(tOpenOrder.getSubject());
				mongoTOpenOrder.setSenderNumber(tOpenOrder.getSenderNumber());
				mongoTOpenOrder.setMsgContent(tOpenOrder.getMsgContent());
				mongoTOpenOrder.setCreateTime(tOpenOrder.getCreateTime());
				mongoTOpenOrder.setFee(tOpenOrder.getFee());
				mongoTOpenOrder.setStatus(tOpenOrder.getStatus());
				mongoTOpenOrder.setPayPhone(tOpenOrder.getPayPhone());
				mongoTOpenOrder.setPayTime(tOpenOrder.getPayTime());
				mongoTOpenOrder.setProvince(tOpenOrder.getProvince());
				mongoTOpenOrder.setUnsubscribeTime(tOpenOrder.getUnsubscribeTime());
				mongoTOpenOrder.setReduce(tOpenOrder.getReduce());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTOpenOrderDao.saveAndUpdate(mongoTOpenOrder);
		}
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TOpenOrder get(Integer id) {
		return openOrderDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		openOrderDao.delete(id);
	}
	
//	/**
//	 * 获取总数
//	 * @param startTime
//	 * @param endTime
//	 * @return
//	 */
//	public Long getCount(Date startTime,Date endTime) {
//		return openOrderDao.getCount(startTime, endTime);
//	}
	
	/**
	 * group by app_id 各状态请求订单数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, String> reqStateCount(Integer sellerId, Date startTime, Date endTime, String status) {
		return mongoTOpenOrderDao.reqStateCount(sellerId, startTime, endTime, status);
	}
	
	/**
	 * group by app_id 成功用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> succUserCount(Integer sellerId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.succUserCount(sellerId, startTime, endTime);
	}
	
	/**
	 * group by app_id 请求用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> reqUserCount(Integer sellerId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.reqUserCount(sellerId, startTime, endTime);
	}
	
	/**
	 * mapreduce 根据sellerId查询出该sellerId下存在多少appId,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<Integer, String> mapReduceAppIds(Integer sellerId, Date startTime, Date endTime, String status, Integer reduce) {
		return mongoTOpenOrderDao.mapReduceAppIds(sellerId, startTime, endTime, status, reduce);
	}
	
	/**
	 * mapreduce 根据appId查询出该appId下存在多少sellerId,并统计请求数和金额
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<Integer, String> mapReduceSellerIds(Integer appId, Date startTime, Date endTime, String status, Integer reduce) {
		return mongoTOpenOrderDao.mapReduceSellerIds(appId, startTime, endTime, status, reduce);
	}
	
	/**
	 * mapreduce 根据sellerId，appId 查询出用户数
	 * @param sellerId
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long mapReduceUserCount(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.mapReduceUserCount(sellerId, appId, startTime, endTime);
	}
	
	/**
	 * mapreduce 根据sellerId，appId 查询出成功用户数
	 * @param sellerId
	 * @param appId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long mapReduceSuccUserCount(Integer sellerId, Integer appId, Date startTime, Date endTime) {
		return mongoTOpenOrderDao.mapReduceSuccUserCount(sellerId, appId, startTime, endTime);
	}
	
	/**
	 * 查询时间点之前的订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
//	public Page<TOpenOrder> getOrderPageBeforeDate(final Page<TOpenOrder> page, Date beforeDate) {
//		Page<TOpenOrder> orderPage = openOrderDao.getOrderPageBeforeDate(page, beforeDate);
//		return orderPage;
//	}
	
	/**
	 * 获取时间点之前的订单总数
	 * @param beforeDate
	 * @return
	 */
//	public Long getOrderPageBeforeDateCount(Date beforeDate) {
//		return openOrderDao.getOrderPageBeforeDateCount(beforeDate);
//	}
	
	/**
	 * 将beforeTime时间点之前的订单移入历史表
	 * @param beforeTime
	 */
	public void batchExecuteMoveToHistory(Date beforeTime) {
		openOrderDao.batchExecuteMoveToHistory(beforeTime);
	}
	
	/**
	 * 删除时间点之前的订单
	 * @param beforeDate
	 */
	public void deleteOrderBeforeDate(Date beforeDate) {
		openOrderDao.deleteOrderBeforeDate(beforeDate);
	}
	
}
