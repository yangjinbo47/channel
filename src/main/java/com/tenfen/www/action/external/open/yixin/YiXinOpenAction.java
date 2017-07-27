package com.tenfen.www.action.external.open.yixin;

import java.io.PrintWriter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

/**
 * 翼信能力开放
 * @author BOBO
 */
public class YiXinOpenAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	
	/**
	 * 翼信-回调地址
	 */
	public void callBack() {
		String phoneNo = ServletRequestUtils.getStringParameter(request, "phoneNo", null);
		String custom = ServletRequestUtils.getStringParameter(request, "custom", null);
		
		String succ = "fail";
		try {
			LogUtil.log("yixin回调参数: phoneNo:"+phoneNo+" custom:"+custom);
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", custom);
			if (!Utils.isEmpty(tOpenOrder)) {
				tOpenOrder.setStatus("3");
				tOpenOrder.setPayTime(new Date());
				tOpenOrder.setPayPhone(phoneNo);
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNo);
				if (!Utils.isEmpty(mobileArea)) {
					tOpenOrder.setProvince(mobileArea.getProvince());
				}
				openOrderManager.save(tOpenOrder);
				
				//增加今日量
				openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String orderid = tOpenOrder.getOrderId();
					String outTradeNo = tOpenOrder.getOutTradeNo();
					String price = tOpenOrder.getFee()+"";
					String status = tOpenOrder.getStatus()+"";
					new Thread(new SendPartner(phoneNo, status,orderid,outTradeNo,price,callbackUrl)).start();
				}
				succ = "ok";
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println(succ);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void unsubCallBack() {
		String phoneNo = ServletRequestUtils.getStringParameter(request, "phoneNo", null);
		String price = ServletRequestUtils.getStringParameter(request, "price", null);
		String cancelMonthly = ServletRequestUtils.getStringParameter(request, "cancelMonthly", null);
		
		System.out.println("yixin unsub param:phoneNo="+phoneNo+",price="+price+",cancelMonthly="+cancelMonthly);
		TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("payPhone", phoneNo);
		if (Utils.isEmpty(tOpenOrder)) {
			tOpenOrder.setStatus("5");
			tOpenOrder.setUnsubscribeTime(new Date());
			openOrderManager.save(tOpenOrder);
		}
	}
	
	
	
//	public void callBack() {
//		Integer seqid = ServletRequestUtils.getIntParameter(request, "seqid", 0);
//		Integer logId = ServletRequestUtils.getIntParameter(request, "log_id", 0);
//		String msisdn = ServletRequestUtils.getStringParameter(request, "msisdn", null);
//		Integer userType = ServletRequestUtils.getIntParameter(request, "user_type", 0);
//		String prodCode = ServletRequestUtils.getStringParameter(request, "prod_code", null);
//		String logTime = ServletRequestUtils.getStringParameter(request, "log_time", null);
//		String operation = ServletRequestUtils.getStringParameter(request, "operation", null);
//		
//		LogUtil.log("seqid:"+seqid+" msisdn:"+msisdn+" userType:"+userType+" prodCode:"+prodCode+" logId:"+logId+" logTime:"+logTime+" operation:"+operation);
//		String success = "fail";
//		try {
//			if (seqid == 0 || logId == 0 || !Utils.isEmpty(msisdn) || userType==0 || !Utils.isEmpty(prodCode)) {
//				TOpenOrderYiXin openOrderYiXin = new TOpenOrderYiXin();
//				openOrderYiXin.setLogId(logId);
//				openOrderYiXin.setSeqId(seqid);
//				openOrderYiXin.setMsisdn(msisdn);
//				openOrderYiXin.setUserType(userType);
//				openOrderYiXin.setProdCode(prodCode);
//				
//				if ("60000009".equals(prodCode)) {//10元包月
//					openOrderYiXin.setChargeType(2);
//					openOrderYiXin.setPrice(1000);
//				} else if ("60000023".equals(prodCode)) {//3元点播
//					openOrderYiXin.setChargeType(1);
//					openOrderYiXin.setPrice(300);
//				} else if ("60000024".equals(prodCode)) {//5元点播
//					openOrderYiXin.setChargeType(1);
//					openOrderYiXin.setPrice(500);
//				} else {
//					openOrderYiXin.setChargeType(0);
//					openOrderYiXin.setPrice(0);
//				}
//				
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				openOrderYiXin.setLogTime(sdf.parse(logTime));
//				openOrderYiXin.setOperation(operation);
//				openOrderYiXinManager.save(openOrderYiXin);
//				
//				success = "success";
//			}
//			
//			PrintWriter out = response.getWriter();
//			response.setContentType("text/html");
//			out.println(success);
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	private class SendPartner implements Runnable {
		private String phone;
		private String fee;
		private String status;
		private String orderId;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String phone, String status, String orderId, String outTradeNo, String fee, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderId = orderId;
			this.outTradeNo = outTradeNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderId);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				jsonObject.put("phone", phone);
				
				if (!Utils.isEmpty(callbackUrl)) {
			        LogUtil.log("sendChannelYiXinMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
