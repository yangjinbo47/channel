package com.tenfen.www.action.external.open.tyspace;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;

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
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;

/**
 * 天翼空间（欣网接入）
 * @author BOBO
 *
 */
public class TySpaceXWAction extends SimpleActionSupport {
	
	private static final long serialVersionUID = 8112913793580559503L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	/**
	 * 天翼空间欣网三方-回调地址
	 */
//	public void callBack() {
//		String result = ServletRequestUtils.getStringParameter(request, "result", null);
//		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
//		String channel = ServletRequestUtils.getStringParameter(request, "channel", null);
//		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
//		String orderSN = ServletRequestUtils.getStringParameter(request, "orderSN", null);
//		String amount = ServletRequestUtils.getStringParameter(request, "amount", null);
//		String chargeType = ServletRequestUtils.getStringParameter(request, "chargeType", null);
//		String payTime = ServletRequestUtils.getStringParameter(request, "payTime", null);
//		String sig = ServletRequestUtils.getStringParameter(request, "sig", null);
//		try {
//			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", channel);
//			String geneSig = null;
//			if (!Utils.isEmpty(tOpenApp)) {
//				String secret = tOpenApp.getAppSecret();
//				
//				List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//				queryParamList.add(new TokenParam("result",result));
//				queryParamList.add(new TokenParam("imsi",imsi));
//				queryParamList.add(new TokenParam("channel",channel));
//				queryParamList.add(new TokenParam("orderId", orderId));
//				queryParamList.add(new TokenParam("orderSN", orderSN));
//				queryParamList.add(new TokenParam("amount", amount));
//				queryParamList.add(new TokenParam("chargeType", chargeType));
//				queryParamList.add(new TokenParam("payTime", payTime));
//				
//				geneSig = TokenService.buildTySpaceToken(queryParamList, secret);
//			}
//			
//			String success = "fail";
//			if (sig.equals(geneSig)) {
//				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//				if (!Utils.isEmpty(tOpenOrder) && "1".equals(tOpenOrder.getStatus())) {
//					//status转换
//					String statusChange = "9999";
//					if ("0".equals(result)) {
//						statusChange = "3";
//					} else if ("100001002".equals(result)) {
//						statusChange = "4";
//					}
//					
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					tOpenOrder.setStatus(statusChange);
//					tOpenOrder.setPayTime(sdf.parse(payTime));
//					tOpenOrder.setPayPhone(imsi);
//					openOrderManager.save(tOpenOrder);
//					
//					//增加今日量
//					if ("0".equals(result)) {
//						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
//					}
//					
//					success = "success";
//					//回调渠道
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					String callbackUrl = tOpenSeller.getCallbackUrl();
//					if (!Utils.isEmpty(callbackUrl)) {						
//						String orderid = tOpenOrder.getOrderId();
//						String outTradeNo = tOpenOrder.getOutTradeNo();
//						String price = tOpenOrder.getFee()+"";
//						new Thread(new SendPartner(statusChange,orderid,outTradeNo,price,callbackUrl)).start();
//					}
//					
//				} else {
//					success = "fail";
//				}
//			} else {
//				success = "fail";
//			}
//			PrintWriter out = response.getWriter();
//			response.setContentType("text/html");
//			out.println(success);
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	public void callBack() {
		int result = ServletRequestUtils.getIntParameter(request, "result", -1);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		String payTime = ServletRequestUtils.getStringParameter(request, "payTime", null);
		try {
			String success = "fail";
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder) && "1".equals(tOpenOrder.getStatus())) {
				//status转换
				String statusChange = "9999";
				if (result == 0) {
					statusChange = "3";
				} else if (result == -100) {
					statusChange = "5";
				} else {
					statusChange = "4";
				}
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				tOpenOrder.setStatus(statusChange);
				tOpenOrder.setPayTime(sdf.parse(payTime));
				String payPhone = imsiMdnRelationManager.getPhone(imsi);
				if (!Utils.isEmpty(payPhone)) {
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(payPhone);
					if (!Utils.isEmpty(mobileArea)) {
						tOpenOrder.setProvince(mobileArea.getProvince());
					}
					tOpenOrder.setPayPhone(payPhone);
				} else {
					tOpenOrder.setPayPhone(imsi);
				}
				openOrderManager.save(tOpenOrder);
				
				//增加今日量
				if ("0".equals(result)) {
					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				}
				
				success = "success";
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {						
					String orderid = tOpenOrder.getOrderId();
					String outTradeNo = tOpenOrder.getOutTradeNo();
					String price = tOpenOrder.getFee()+"";
					new Thread(new SendPartner(statusChange,orderid,outTradeNo,price,callbackUrl)).start();
				}
				
			} else {
				success = "fail";
			}
			
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println(success);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderId;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String status, String orderId, String outTradeNo, String fee, String callbackUrl) {
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
				
		        LogUtil.log("sendChannelTySpaceXWMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
