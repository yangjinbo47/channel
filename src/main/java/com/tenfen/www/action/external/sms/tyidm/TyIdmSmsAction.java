package com.tenfen.www.action.external.sms.tyidm;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.ffcs.cryto.Cryto;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class TyIdmSmsAction extends SimpleActionSupport{
	
	private static final long serialVersionUID = 8850486384317240505L;

	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	/**
	 * 风控
	 */
	public void auth() {
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		String success = "success";
		
		try {
			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tSmsOrder)) {
				success = "success";
			} else {
				success = "fail";
			}
			
			try {
				PrintWriter out = response.getWriter();
				response.setContentType("text/html");
				out.println(success);
				out.flush();
				out.close();
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void callBack() {
		String requestData = ServletRequestUtils.getStringParameter(request, "requestData", null);
		LogUtil.log("tyidmsms requestData="+requestData);
		String sign = StringUtils.substringBetween(requestData, "<Sign>", "</Sign>");
		String behavior = StringUtils.substringBetween(requestData, "<Behavior>", "</Behavior>");
		String trade_status = StringUtils.substringBetween(requestData, "<Trade_status>", "</Trade_status>");
		String trade_no = StringUtils.substringBetween(requestData, "<Trade_no>", "</Trade_no>");
		String buyer_id = StringUtils.substringBetween(requestData, "<Buyer_id>", "</Buyer_id>");
//		String product_id = StringUtils.substringBetween(requestData, "<Product_id>", "</Product_id>");
//		String product_name = StringUtils.substringBetween(requestData, "<Product_name>", "</Product_name>");
		String priceStr = StringUtils.substringBetween(requestData, "<Price>", "</Price>");
		String app_id = StringUtils.substringBetween(requestData, "<App_id>", "</App_id>");
		String extension = StringUtils.substringBetween(requestData, "<Extension>", "</Extension>");
		String source = behavior+trade_status+trade_no+buyer_id+extension;
		
		String retStatus = "0";
		int reduce = 0;
		try {
			int price = Integer.parseInt(priceStr);
			TSmsApp tSmsApp = smsAppManager.getSmsAppByProperty("appKey", app_id);
			Integer merchantId = tSmsApp.getMerchantId();
			String subject = tSmsApp.getName();
			Integer appId = tSmsApp.getId();
			String key = tSmsApp.getAppSecret();
			TSmsProductInfo tSmsProductInfo = null;
			List<TSmsProductInfo> smsProductInfos = tSmsApp.getProductList();
			for (TSmsProductInfo smsProductInfo : smsProductInfos) {
				Integer fee = smsProductInfo.getPrice();
				if (fee == price) {
					tSmsProductInfo = smsProductInfo;
				}
			}
			String geneSign = Cryto.encryptBase643DES(source, key);
			LogUtil.log("tyidmsms geneSign="+geneSign);
			List<TSmsSeller> tSmsSellers = smsSellerManager.findSellerByAppId(appId);
			TSmsSeller tSmsSeller = tSmsSellers.get(0);
			Integer sellerId = tSmsSeller.getId();
			if (geneSign.equals(sign)) {
				String status = "4";
				TSmsOrder tSmsOrder = new TSmsOrder();
				if ("0".equals(trade_status)) {
					status = "3";
					String sendNumber = tSmsProductInfo.getSendNumber();
					Date payTime = new Date();
					tSmsOrder.setOrderId(trade_no);
					tSmsOrder.setImsi(buyer_id);
					tSmsOrder.setMerchantId(merchantId);
					tSmsOrder.setAppId(appId);
					tSmsOrder.setSellerId(sellerId);
					tSmsOrder.setSubject(subject);
					tSmsOrder.setSenderNumber(sendNumber);
					tSmsOrder.setMsgContent(extension);
					tSmsOrder.setFee(price);
					tSmsOrder.setProductType(Constants.PRODUCT_CHARGETYPE.DIANBO.getValue());
					tSmsOrder.setStatus(status);
					tSmsOrder.setPayPhone(buyer_id);
					tSmsOrder.setPayTime(payTime);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(buyer_id);
					String province = null;
					if (!Utils.isEmpty(mobileArea)) {
						province = mobileArea.getProvince();
						tSmsOrder.setProvince(province);
					}
					
					//是否扣量
					TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
					double reduce_conf = tSmsAppLimit.getReduce()/(double)100;
					double rate = new Random().nextDouble();
					if (rate < reduce_conf) {
						reduce = 1;
						tSmsOrder.setReduce(reduce);
					}
					smsOrderManager.save(tSmsOrder);
					
					//增加今日量
					smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
					//回调渠道
					String callbackUrl = tSmsSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						if (reduce != 1) {//不扣量
							new Thread(new SendPartner(status,trade_no,tSmsOrder.getFee()+"",callbackUrl)).start();
						} else {
							new Thread(new SendPartner("4",trade_no,tSmsOrder.getFee()+"",callbackUrl)).start();
						}
					}
				} else {
					status = "4";
					tSmsOrder.setStatus(status);
					smsOrderManager.save(tSmsOrder);
				}
				retStatus = "0";
			}
			
		} catch (Exception e) {
			retStatus = "9999";
			LogUtil.error(e.getMessage(), e);
		}
		
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println(retStatus);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String callbackUrl;
		
		public SendPartner(String status,String orderNo,String fee,String callbackUrl) {
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
				LogUtil.log("sendChannelTyIdmSmsMsg:"+jsonObject.toString());
				HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}

}
