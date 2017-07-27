package com.tenfen.www.service.operation.sms;

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
import org.springside.modules.orm.Page;

import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsOrderXwPackage;
import com.tenfen.mongoEntity.MongoTSmsOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.operation.sms.SmsOrderDao;
import com.tenfen.www.dao.operation.sms.SmsOrderXwPackageDao;
import com.tenfen.www.mongodao.MongoTSmsOrderDao;

@Component
@Transactional
public class SmsOrderManager {
	
	private static final int POOL_SIZE = 20;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
	
	@Autowired
	private SmsOrderDao smsOrderDao;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private MongoTSmsOrderDao mongoTSmsOrderDao;
	@Autowired
	private SmsOrderXwPackageDao smsOrderXwPackageDao;
	
	public TSmsOrder getSmsOrderByProperty(String propertyName, Object value) {
		TSmsOrder tSmsOrder = null;
		try {
			List<TSmsOrder> tSmsOrders = smsOrderDao.findBy(propertyName, value);
			Collections.reverse(tSmsOrders);
			if (tSmsOrders.size() > 0) {
				tSmsOrder = tSmsOrders.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return tSmsOrder;
	}
	
	/**
	 * 查询用户列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<TSmsOrder> getOrderPageByProperty(final Page<TSmsOrder> page, Integer sellerId, String payPhone, Date start, Date end) {
		Page<TSmsOrder> packageUserPage = null;
		if (Utils.isEmpty(payPhone)) {
			packageUserPage = smsOrderDao.getOrderPage(page, sellerId, start, end);
		} else {
			packageUserPage = smsOrderDao.getOrderPage(page, payPhone, sellerId, start, end);
		}
		return packageUserPage;
	}
	
	/**
	 * 根据号码查询订购记录
	 * @param page
	 * @param pageSize
	 * @param phone
	 * @return
	 */
	public List<MongoTSmsOrder> getOrderPageByPhoneFromMongo(int page, int pageSize, String phone) {
		return mongoTSmsOrderDao.getOrderListByPhone(page, pageSize, phone);
	}
	
	/**
	 * 根据号码查询订购记录总数
	 * @param page
	 * @param pageSize
	 * @param phone
	 * @return
	 */
	public Long getOrderPageByPhoneFromMongoCount(String phone) {
		return mongoTSmsOrderDao.getOrderListByPhoneCount(phone);
	}
	
	public TSmsOrder getOrder(Integer sellerId, String payPhone) {
		TSmsOrder tSmsOrder = null;
		List<TSmsOrder> list = smsOrderDao.getOrderList(sellerId, payPhone);
		if (list.size() > 0) {
			tSmsOrder = list.get(0);
		}
		return tSmsOrder;
	}
	
	public List<TSmsOrder> getOrderList(Integer sellerId, String payPhone, Date startTime, Date endTime) {
		List<TSmsOrder> list = new ArrayList<TSmsOrder>();
		if (Utils.isEmpty(payPhone)) {
			list = smsOrderDao.getOrderList(sellerId, startTime, endTime);
		} else {
			list = smsOrderDao.getOrderList(sellerId, payPhone, startTime, endTime);
		}
		
		return list;
	}
	
	public List<MongoTSmsOrder> getOrderListFromMongo(Integer sellerId, String payPhone, Date startTime, Date endTime) {
		List<MongoTSmsOrder> list = new ArrayList<MongoTSmsOrder>();
		if (Utils.isEmpty(payPhone)) {
			list = mongoTSmsOrderDao.getOrderList(sellerId, startTime, endTime);
		} else {
			list = mongoTSmsOrderDao.getOrderList(sellerId, payPhone, startTime, endTime);
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
	public List<TSmsOrder> getOrderListByMerchantId(Integer merchantId ,Date startTime, Date endTime) {
		List<TSmsOrder> list = smsOrderDao.getOrderListByMerchantId(merchantId, startTime, endTime);
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
		return smsOrderDao.getOrderFee(sellerId, appId, startTime, endTime);
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
		return mongoTSmsOrderDao.getSumFeeByPhone(sellerId, phone, startTime, endTime);
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
		return mongoTSmsOrderDao.mapReduceProvinceBySellerId(sellerId, startTime, endTime, status);
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
		return mongoTSmsOrderDao.mapReduceProvinceBySellerIdAndAppId(sellerId, appId, startTime, endTime, status);
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
		return mongoTSmsOrderDao.mapReduceProvinceByAppId(appId, startTime, endTime, status);
	}
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsOrder entity) {
		smsOrderDao.save(entity);
		//同步至mongo
		try {
			if (systemProperty.getIsSaveToMongo()) {
				exe.execute(new MongoThread(entity));
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public class HbaseThread implements Runnable {
//		private TSmsOrder tSmsOrder;
//		
//		public HbaseThread(TSmsOrder tSmsOrder) {
//			this.tSmsOrder = tSmsOrder;
//		}
//	
//		@Override
//		public void run() {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String createDay = sdf.format(tSmsOrder.getCreateTime());
//			String rowKey = createDay+"_"+tSmsOrder.getOrderId();
//			String sellerId = String.valueOf(tSmsOrder.getSellerId());
//			String appId = String.valueOf(tSmsOrder.getAppId());
//			String merchantId = String.valueOf(tSmsOrder.getMerchantId());
//			
//			String imsi = tSmsOrder.getImsi() == null ? "" : tSmsOrder.getImsi();
//			String orderId = tSmsOrder.getOrderId();
//			String outTradeNo = tSmsOrder.getOutTradeNo() == null ? "" : tSmsOrder.getOutTradeNo();
//			String linkId = tSmsOrder.getLinkId() == null ? "" : tSmsOrder.getLinkId();
//			String subject = tSmsOrder.getSubject();
//			String sendNumber = tSmsOrder.getSenderNumber() == null ? "" : tSmsOrder.getSenderNumber();
//			String msgContent = tSmsOrder.getMsgContent() == null ? "" : tSmsOrder.getMsgContent();
//			String mo_number = tSmsOrder.getMoNumber() == null ? "" : tSmsOrder.getMoNumber();
//			String mo_msg = tSmsOrder.getMoMsg() == null ? "" : tSmsOrder.getMoMsg();
//			String createTime = sdf1.format(tSmsOrder.getCreateTime());
//			String fee = String.valueOf(tSmsOrder.getFee());
//			String productType = String.valueOf(tSmsOrder.getProductType());
//			String status = tSmsOrder.getStatus();
//			
//			String payPhone = tSmsOrder.getPayPhone() == null ? "" : tSmsOrder.getPayPhone();
//			String payTime = "";
//			if (tSmsOrder.getPayTime() != null) {
//				payTime = sdf1.format(tSmsOrder.getPayTime());
//			}
//			String province = tSmsOrder.getProvince() == null ? "" : tSmsOrder.getProvince();
//			
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "seller_id", sellerId);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "app_id", appId);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "merchant_id", merchantId);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "imsi", imsi);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "order_id", orderId);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "out_trade_no", outTradeNo);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "link_id", linkId);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "subject", subject);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "send_number", sendNumber);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "msg_content", msgContent);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "mo_number", mo_number);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "mo_msg", mo_msg);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "create_time", createTime);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "fee", fee);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "product_type", productType);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "status", status);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "pay_phone", payPhone);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "pay_time", payTime);
//			hbaseHelper.writeRecord("t_sms_order", rowKey, "order_info", "province", province);
//		}
//	}
	
	public class MongoThread implements Runnable {
		
		private MongoTSmsOrder mongoTSmsOrder;
		
		public MongoThread(TSmsOrder tSmsOrder) {
			try {
				mongoTSmsOrder = new MongoTSmsOrder();
				mongoTSmsOrder.setImsi(tSmsOrder.getImsi());
				mongoTSmsOrder.setOrderId(tSmsOrder.getOrderId());
				mongoTSmsOrder.setOutTradeNo(tSmsOrder.getOutTradeNo());
				mongoTSmsOrder.setLinkId(tSmsOrder.getLinkId());
				mongoTSmsOrder.setSellerId(tSmsOrder.getSellerId());
				mongoTSmsOrder.setAppId(tSmsOrder.getAppId());
				mongoTSmsOrder.setMerchantId(tSmsOrder.getMerchantId());
				mongoTSmsOrder.setSubject(tSmsOrder.getSubject());
				mongoTSmsOrder.setSenderNumber(tSmsOrder.getSenderNumber());
				mongoTSmsOrder.setMsgContent(tSmsOrder.getMsgContent());
				mongoTSmsOrder.setMoNumber(tSmsOrder.getMoNumber());
				mongoTSmsOrder.setMoMsg(tSmsOrder.getMoMsg());
				mongoTSmsOrder.setCreateTime(tSmsOrder.getCreateTime());
				mongoTSmsOrder.setFee(tSmsOrder.getFee());
				mongoTSmsOrder.setProductType(tSmsOrder.getProductType());
				mongoTSmsOrder.setStatus(tSmsOrder.getStatus());
				mongoTSmsOrder.setPayPhone(tSmsOrder.getPayPhone());
				mongoTSmsOrder.setPayTime(tSmsOrder.getPayTime());
				mongoTSmsOrder.setProvince(tSmsOrder.getProvince());
				mongoTSmsOrder.setUnsubscribeTime(tSmsOrder.getUnsubscribeTime());
				mongoTSmsOrder.setReduce(tSmsOrder.getReduce());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTSmsOrderDao.saveAndUpdate(mongoTSmsOrder);
		}
	}
	
	/**
	 * 获取订单信息
	 * @param id
	 * @return
	 */
	public TSmsOrder get(Integer id) {
		return smsOrderDao.get(id);
	}
	
	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Integer id) {
		smsOrderDao.delete(id);
	}
	
	/**
	 * group by app_id 各状态请求订单数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, String> reqStateCount(Integer sellerId, Date startTime, Date endTime, String status) {
		return mongoTSmsOrderDao.reqStateCount(sellerId, startTime, endTime, status);
	}
	
	/**
	 * group by app_id 成功用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> succUserCount(Integer sellerId, Date startTime, Date endTime) {
		return mongoTSmsOrderDao.succUserCount(sellerId, startTime, endTime);
	}
	
	/**
	 * group by app_id 请求用户数
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 */
	public Map<Integer, Integer> reqUserCount(Integer sellerId, Date startTime, Date endTime) {
		return mongoTSmsOrderDao.reqUserCount(sellerId, startTime, endTime);
	}
	
//	public Integer succUserFee(Integer sellerId, Date startTime, Date endTime){
//		
//	}
	
	/**
	 * mapreduce 根据sellerId查询出该sellerId下存在多少appId,并统计请求数和金额
	 * @param sellerId
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	public Map<Integer, String> mapReduceAppIds(Integer sellerId, Date startTime, Date endTime, String status, Integer reduce) {
		return mongoTSmsOrderDao.mapReduceAppIds(sellerId, startTime, endTime, status, reduce);
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
		return mongoTSmsOrderDao.mapReduceSellerIds(appId, startTime, endTime, status, reduce);
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
		return mongoTSmsOrderDao.mapReduceUserCount(sellerId, appId, startTime, endTime);
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
		return mongoTSmsOrderDao.mapReduceSuccUserCount(sellerId, appId, startTime, endTime);
	}
	
	/**
	 * 查询时间点之前的订购列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
//	public Page<TSmsOrder> getOrderPageBeforeDate(final Page<TSmsOrder> page, Date beforeDate) {
//		Page<TSmsOrder> orderPage = smsOrderDao.getOrderPageBeforeDate(page, beforeDate);
//		return orderPage;
//	}
	
	/**
	 * 获取时间点之前的订单总数
	 * @param beforeDate
	 * @return
	 */
//	public Long getOrderPageBeforeDateCount(Date beforeDate) {
//		return smsOrderDao.getOrderPageBeforeDateCount(beforeDate);
//	}
	
	/**
	 * 将beforeTime时间点之前的订单移入历史表
	 * @param beforeTime
	 */
	public void batchExecuteMoveToHistory(Date beforeTime) {
		smsOrderDao.batchExecuteMoveToHistory(beforeTime);
	}
	
	/**
	 * 删除时间点之前的订单
	 * @param beforeDate
	 */
	public void deleteOrderBeforeDate(Date beforeDate) {
		smsOrderDao.deleteOrderBeforeDate(beforeDate);
	}
	
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(TSmsOrderXwPackage entity) {
		smsOrderXwPackageDao.save(entity);
	}
	
	public List<TSmsOrderXwPackage> getOrderListByPhoneAndContent(String mobile, String content) {
		return smsOrderXwPackageDao.getOrderListByPhoneAndContent(mobile, content);
	}
	
}
