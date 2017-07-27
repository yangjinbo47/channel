package com.tenfen.www.action.external.sms.unicomzx;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.cache.CacheFactory;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.operation.sms.TSmsSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class UnicomYyZxAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private BlackListManager blackListManager;
	
	public void subscribe() {
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String msg = ServletRequestUtils.getStringParameter(request, "msg", null);
		String dest = ServletRequestUtils.getStringParameter(request, "dest", null);
		String srvid = ServletRequestUtils.getStringParameter(request, "srvid", null);
		LogUtil.log("unicomyyzx subscribe param phone:"+phone+" msg:"+msg+" dest:"+dest+" srvid:"+srvid);
		try {
			Integer reduce = 0;
			if (!Utils.isEmpty(msg)) {
				msg = msg.toUpperCase();
			}
			
			String province = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (!Utils.isEmpty(mobileArea)) {
				province = mobileArea.getProvince();
			}
			
			Integer sellerId = 4;
			TSmsSeller tSmsSeller = smsSellerManager.get(sellerId);
			if (!Utils.isEmpty(tSmsSeller)) {
				List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
				TSmsApp tSmsApp = null;
				if (smsSellerAppList.size() > 0) {
					for (TSmsSellerApps tSmsSellerApps : smsSellerAppList) {
						tSmsApp = tSmsSellerApps.getSmsApp();
					}
				}
				
				if (!Utils.isEmpty(tSmsApp)) {
					Integer merchantId = tSmsApp.getMerchantId();//merchantId
					Integer appId = tSmsApp.getId();
					//订单号
					String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
					//根据指令查询计费点
					TSmsProductInfo tSmsProductInfo = smsProductInfoManager.getSmsProductInfoByProperty(merchantId, msg);
					
					TSmsOrder tSmsOrder = new TSmsOrder();
					tSmsOrder.setOrderId(orderNo);
					tSmsOrder.setOutTradeNo(null);
					tSmsOrder.setImsi(phone);
					tSmsOrder.setAppId(appId);
					tSmsOrder.setMerchantId(merchantId);
					tSmsOrder.setSellerId(tSmsSeller.getId());
					tSmsOrder.setSubject("联通在信");
					tSmsOrder.setSenderNumber(tSmsProductInfo.getSendNumber());
					tSmsOrder.setMsgContent(tSmsProductInfo.getInstruction());
					tSmsOrder.setMoNumber(dest);
					tSmsOrder.setMoMsg(msg);
					tSmsOrder.setFee(tSmsProductInfo.getPrice());
					tSmsOrder.setProductType(tSmsProductInfo.getType());
					tSmsOrder.setPayPhone(phone);
					tSmsOrder.setProvince(province);
					tSmsOrder.setPayTime(new Date());
					tSmsOrder.setStatus("3");
					//扣量
					double reduce_conf = 0.2;
					if (!Utils.isEmpty(mobileArea)) {
						TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
						reduce_conf = tSmsAppLimit.getReduce()/(double)100;
					}
					double rate = new Random().nextDouble();
					if (rate < reduce_conf) {
						reduce = 1;
					}
					tSmsOrder.setReduce(reduce);
					smsOrderManager.save(tSmsOrder);
					
					//增加今日量
					smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
					
					//调用回调渠道方法
					if (reduce != 1) {//不扣量
						String callbackUrl = tSmsSeller.getCallbackUrl();
						if (!Utils.isEmpty(callbackUrl)) {
							//mtk 直接返回服务端orderid 作为订单id
							new Thread(new SendPartner(phone, tSmsOrder.getStatus(), tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", dest, msg, province, callbackUrl)).start();
						}
					} else {//扣量
						String callbackUrl = tSmsSeller.getCallbackUrl();
						String callbackStatus = "4";
						if (!Utils.isEmpty(callbackUrl)) {
							//mtk 直接返回服务端orderid 作为订单id
							new Thread(new SendPartner(phone, callbackStatus, tSmsOrder.getOrderId(), tSmsOrder.getFee()+"", dest, msg, province, callbackUrl)).start();
						}
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void unsub() {
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String msg = ServletRequestUtils.getStringParameter(request, "msg", null);
		String dest = ServletRequestUtils.getStringParameter(request, "dest", null);
		String srvid = ServletRequestUtils.getStringParameter(request, "srvid", null);
		LogUtil.log("unicomyyzx unsub param phone:"+phone+" msg:"+msg+" dest:"+dest+" srvid:"+srvid);
		try {
			String province = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (!Utils.isEmpty(mobileArea)) {
				province = mobileArea.getProvince();
			}
			
			Integer sellerId = 4;
			TSmsSeller tSmsSeller = smsSellerManager.get(sellerId);
			if (!Utils.isEmpty(tSmsSeller)) {
				List<TSmsSellerApps> smsSellerAppList = tSmsSeller.getSellerApps();
				TSmsApp tSmsApp = null;
				if (smsSellerAppList.size() > 0) {
					for (TSmsSellerApps tSmsSellerApps : smsSellerAppList) {
						tSmsApp = tSmsSellerApps.getSmsApp();
					}
				}
				
				if (!Utils.isEmpty(tSmsApp)) {
					Integer merchantId = tSmsApp.getMerchantId();//merchantId
					Integer appId = tSmsApp.getId();
					//订单号
					String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
					//根据指令查询计费点
					TSmsProductInfo tSmsProductInfo = smsProductInfoManager.getSmsProductInfoByProperty(merchantId, msg);
					
					TSmsOrder tSmsOrder = new TSmsOrder();
					tSmsOrder.setOrderId(orderNo);
					tSmsOrder.setOutTradeNo(null);
					tSmsOrder.setImsi(phone);
					tSmsOrder.setAppId(appId);
					tSmsOrder.setMerchantId(merchantId);
					tSmsOrder.setSellerId(tSmsSeller.getId());
					tSmsOrder.setSubject("联通在信");
					tSmsOrder.setSenderNumber(tSmsProductInfo.getSendNumber());
					tSmsOrder.setMsgContent(tSmsProductInfo.getInstruction());
					tSmsOrder.setMoNumber(dest);
					tSmsOrder.setMoMsg(msg);
					tSmsOrder.setFee(tSmsProductInfo.getPrice());
					tSmsOrder.setProductType(tSmsProductInfo.getType());
					tSmsOrder.setPayPhone(phone);
					tSmsOrder.setProvince(province);
					tSmsOrder.setUnsubscribeTime(new Date());
					tSmsOrder.setStatus("5");
					smsOrderManager.save(tSmsOrder);
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String callbackUrl;
		private String phone;
		private String content;
		private String province;
		private String channelNo;
		
		public SendPartner(String phone, String status, String orderNo, String fee, String channelNo, String content, String province, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.callbackUrl = callbackUrl;
			this.channelNo = channelNo;
			this.content = content;
			this.province = province;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("phone", phone);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				jsonObject.put("channel_no", channelNo);
				jsonObject.put("content", content);
				jsonObject.put("province", province);
				
		        LogUtil.log("unicomzx call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
	}
	
	
}
