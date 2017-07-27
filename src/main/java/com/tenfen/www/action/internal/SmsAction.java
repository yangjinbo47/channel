package com.tenfen.www.action.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.axis.types.URI;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsAppLimit;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsProductInfo;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.ChargingInformation;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.EndReason;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.RequestSOAPHeader;
import com.tenfen.www.webservice.chinatelecom.schema.ctcc.common.SimpleReference;
import com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service.SMSSendSmsLocator;
import com.tenfen.www.webservice.chinatelecom.wsdl.ctcc.sms.send.service.SMSSendSmsStub;

/**
 * 电信全网短信 - 接受信元mo mr回调
 * @author BOBO
 *
 */
public class SmsAction extends SimpleActionSupport{

	private static final long serialVersionUID = 802481743436649610L;
	
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	public void subscribe() {
		LogUtil.log("nmsc /sms/sms_subscribe.action visiting");
		Map<String, String[]> map = request.getParameterMap();
		for (String key : map.keySet()) {
			String[] value = map.get(key);
			for (String string : value) {
				LogUtil.log("nmsc subscribe param key:"+key+" value:"+string);
			}
		}
		
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			// 将资料解码
			String reqBody = sb.toString();
			LogUtil.log("nmsc subscribe param:"+reqBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mo() {
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			// 将资料解码
			String reqBody = sb.toString();
			LogUtil.log("nmsc mo param:"+reqBody);
			String senderAddress = StringUtils.substringBetween(reqBody, "<senderAddress>", "</senderAddress>");
			String linkId = StringUtils.substringBetween(reqBody, "<linkId>", "</linkId>");
			String correlator = linkId;
			String message = StringUtils.substringBetween(reqBody, "<message>", "</message>");
			String SAN = StringUtils.substringBetween(reqBody, "<SAN>", "</SAN>");
			
			sendSms(senderAddress, linkId, correlator, message, SAN);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void mr() {
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			// 将资料解码
			String reqBody = sb.toString();
			LogUtil.log("nmsc mr param:"+reqBody);
			String linkId = StringUtils.substringBetween(reqBody, "<linkId>", "</linkId>");
			if (Utils.isEmpty(linkId)) {
				linkId = StringUtils.substringBetween(reqBody, "<sms7:correlator>", "</sms7:correlator>");
			}
			String deliveryStatus = StringUtils.substringBetween(reqBody, "<deliveryStatus>", "</deliveryStatus>");
			String phoneNum = StringUtils.substringBetween(reqBody, "<address>", "</address>");
			phoneNum = phoneNum.substring(4);
			String status = "4";
			if ("DeliveredToTerminal".equals(deliveryStatus)) {
				status = "3";
			} else {
				status = "4";
			}
			
			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("linkId", linkId);
			if (!Utils.isEmpty(tSmsOrder)) {
				if ("1".equals(tSmsOrder.getStatus())) {
					tSmsOrder.setStatus(status);
					tSmsOrder.setPayPhone(phoneNum);
					tSmsOrder.setPayTime(new Date());
					
					smsOrderManager.save(tSmsOrder);
					
					//增加今日量
					if ("3".equals(status)) {
						smsSellerManager.saveSmsSellerApps(tSmsOrder.getSellerId(), tSmsOrder.getAppId(), tSmsOrder.getFee());
					}
					
					//调用回调渠道方法
					TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
					String callbackUrl = tSmsSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						new Thread(new SendPartner(phoneNum, status, tSmsOrder.getOrderId(), tSmsOrder.getOutTradeNo(), tSmsOrder.getFee()+"", callbackUrl)).start();
					}
				}
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void sync() {
		LogUtil.log("/sms/sms_sync.action visiting");
		Map<String, String[]> map = request.getParameterMap();
		for (String key : map.keySet()) {
			String[] value = map.get(key);
			for (String string : value) {
				LogUtil.log("nmsc mr param key:"+key+" value:"+string);
			}
		}
		
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			
			// 将资料解码
			String reqBody = sb.toString();
			LogUtil.log("nmsc sync param:"+reqBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private String sendSms(String spId, String spPassword, String productId, String linkId, String phoneNum, String sendNumber) {
//		
//		try {
//			StringBuffer message = new StringBuffer();
//			message.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
//			.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
//			.append("<soapenv:Header>")
//			.append("<RequestSOAPHeader soapenv:actor=\"http://schemas.xmlsoap.org/soap/actor/next\" soapenv:mustUnderstand=\"0\" xmlns=\"http://www.chinatelecom.com.cn/schema/ctcc/common/v2_1\">")
//			.append("<spId>"+spId+"</spId>")
//			.append("<spPassword>"+spPassword+"</spPassword>")
//			.append("<timeStamp>"+System.currentTimeMillis()+"</timeStamp>")
//			.append("<productId>"+productId+"</productId>")
//			.append("<SAN></SAN>")
//			.append("<transactionId></transactionId>")
//			.append("<transEnd>0</transEnd>")
//			.append("<linkId>"+linkId+"</linkId>")
//			.append("<OA>"+phoneNum+"</OA>")
//			.append("<multicastMessaging>false</multicastMessaging>")
//			.append("</RequestSOAPHeader>")
//			.append("</soapenv:Header>")
//			.append("<soapenv:Body>")
//			.append("<sendSms xmlns=\"http://www.chinatelecom.com.cn/schema/ctcc/sms/send/v2_1/local\">")
//			.append("<addresses>"+phoneNum+"</addresses>")
//			.append("<senderName>"+sendNumber+"</senderName>")
//			.append("<charging>")
//			.append("<description xmlns=\"\">Default</description>")
//			.append("<currency xmlns=\"\">0</currency>")
//			.append("<amount xmlns=\"\">5</amount>")
//			.append("<code xmlns=\"\">"+productId+"</code>")
//			.append("</charging>")
//			.append("<message>小虫点播5元</message>")
//			.append("<receiptRequest>")
//			.append("<endpoint xmlns=\"\">http://118.85.200.55:9081/SendSmsService</endpoint>")
//			.append("<interfaceName xmlns=\"\">notifySmsDeliveryReceiption</interfaceName>")
//			.append("<correlator xmlns=\"\">"+linkId+"</correlator>")
//			.append("</receiptRequest>")
//			.append("</sendSms>")
//			.append("</soapenv:Body>")
//			.append("</soapenv:Envelope>");
//			
//			URL urls = new URL("http://118.85.200.55:9081/SendSmsService");
//			String msg = message.toString();
//			LogUtil.log("nmsc sendtoserver:"+msg);
//			
//	        
//			byte[] bytes = msg.getBytes("UTF-8");
//			HttpURLConnection con = (HttpURLConnection) urls.openConnection();
//			con.setConnectTimeout(10000);
//			con.setReadTimeout(10000);
//			con.setRequestProperty("Content-length", String.valueOf(bytes.length));
//			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
//			con.setRequestProperty("SOAPAction", "http://118.85.200.55:9081/SendSmsService");
//			con.setRequestMethod("POST");
//			con.setDoOutput(true);
//			con.setDoInput(true);
//			OutputStream out = con.getOutputStream();
//			
//			out.write(bytes);
//			out.flush();
//			out.close();
//			
//			InputStream incoming = con.getInputStream();
//			byte[] b = new byte[incoming.available()];
//			incoming.read(b);
//			return new String(b);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			return null;
//		}
//		
//	}
	
	/**
	 * 发送短信
	 * @return 0000-成功，1001-没有找到订单，1002-没有找到计费点信息，1003-屏蔽省份
	 */
	private String sendSms(String senderAddress, String linkId, String correlator, String message, String SAN) {
		String returnCode = "0000";
		try {
			String phoneNum = senderAddress.substring(4);
			senderAddress = senderAddress.substring(0, 4) + "+86" + senderAddress.substring(4);
			String[] addresses = new String[] {senderAddress};
			SAN = SAN.substring(4);
			
			//根据解码的资料获取通道信息
			String orderId = message.substring(message.indexOf("#")+1);
			TSmsOrder tSmsOrder = smsOrderManager.getSmsOrderByProperty("orderId", orderId);
			TSmsApp tSmsApp = null;
			String spId = null;
			String spPassword = null;
			Integer appId = null;
			String productId = null;
			String tips = null;
			if (!Utils.isEmpty(tSmsOrder)) {
				appId = tSmsOrder.getAppId();
				int fee = tSmsOrder.getFee();
				tSmsApp = smsAppManager.get(appId);
				spId = tSmsApp.getAppKey();
				spPassword = tSmsApp.getAppSecret();
				tips = tSmsApp.getTips();
				tips = tips.replace("${fee}", (fee / 100)+"");

				List<TSmsProductInfo> tSmsProductInfoList = tSmsApp.getProductList();
				for (TSmsProductInfo tSmsProductInfo : tSmsProductInfoList) {
					if (fee == tSmsProductInfo.getPrice()) {//查询关联计费点价格
						productId = tSmsProductInfo.getProductId();
						break;
					}
				}
			} else {
				return "1001";//没有找到订单
			}
			if (Utils.isEmpty(productId)) {
				return "1002";//没有找到计费点信息
			}
			//检查屏蔽省份
			String province = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNum);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
			}
			boolean flag = false;//false-排除 true-不排除
			if (province != null) {
				// 是否在屏蔽区域内
//				if (tSmsApp.getExcludeArea() == null || tSmsApp.getExcludeArea().length() == 0) {// 没有排除对象
//					flag = true;
//				} else {
//					if (!"all".equals(tSmsApp.getExcludeArea())) {// 是否屏蔽全国
//						if (tSmsApp.getExcludeArea().indexOf(province) == -1) {
//							flag = true;
//						}
//					}
//				}
				//判断省是否到量
				TSmsAppLimit tSmsAppLimit = smsAppManager.findAppLimitByProperty(appId, province);
				Integer packagedaylimit_conf = tSmsAppLimit.getDayLimit();
				if (packagedaylimit_conf == -1) {//不屏蔽
					flag = true;
				}
			} else {//未取到号码所在地
				flag = true;
			}
			if (!flag) {
				returnCode = "1003";//在排除省份列表中
			}
			
			//调用webservice发送短信
			if ("0000".equals(returnCode)) {
				String smsURL = "http://118.85.200.55:9081/SendSmsService";
				// 目的地址
				org.apache.axis.types.URI[] addressesuri = new org.apache.axis.types.URI[addresses.length];
				for (int i = 0; i < addresses.length; i++) {
					String sss = addresses[i];
					addressesuri[i] = new URI(sss);
				}
				SimpleReference receiptRequest = new SimpleReference(new URI(smsURL), "notifySmsDeliveryReceiption",correlator);
				// 定义消息头对象
				RequestSOAPHeader soapHeader = new RequestSOAPHeader();
				String TimeStamp = getSysTime();
				String temp = spId + spPassword + TimeStamp;
				soapHeader.setSpId(spId);
				soapHeader.setSpPassword(MD5Crypt(temp));
				soapHeader.setTimeStamp(TimeStamp);
				soapHeader.setProductId(productId);
				soapHeader.setTransactionId("");
				String transEnd = "0";
				if (!transEnd.equals("") && (transEnd.length() > 0)) {
					soapHeader.setTransEnd(EndReason.fromString(transEnd));
				}
				// 事务关联id,用于点播，可选
				soapHeader.setLinkId(linkId);
				// 业务订购地址，群发不填
				soapHeader.setOA(new URI(addresses[0])); 
				// 付费地址，可选
				// soapHeader.setFA(new URI(""));
				// 是否群发
				soapHeader.setMulticastMessaging(false);
				// 业务接入码，可选
				soapHeader.setSAN("");
				// 计费方案
				ChargingInformation charging1 = new ChargingInformation("Default", "0", new java.math.BigDecimal(1), productId);
				
				// 发送地址
				URL url = new URL(smsURL);
				
				SMSSendSmsLocator locator = new SMSSendSmsLocator();
				SMSSendSmsStub service = locator.getSendSms(url);
				// 发送
				String result = service.sendSms(addressesuri, SAN, charging1, tips, receiptRequest, soapHeader);
				LogUtil.log("nmsc mo param:"+result);
			}
			//更新订单
			tSmsOrder.setLinkId(linkId);
			tSmsOrder.setMoNumber(phoneNum);
			tSmsOrder.setMoMsg(message);
			if (!Utils.isEmpty(province)) {
				tSmsOrder.setProvince(province);
			}
			
			smsOrderManager.save(tSmsOrder);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnCode;
	}
	
	/**
	 * 得到系统时间
	 * 
	 * @return
	 */
	private String getSysTime() {
		Date myDate = new Date(System.currentTimeMillis());
		SimpleDateFormat sDateFormat = new SimpleDateFormat("MMddHHmmss");
		return sDateFormat.format(myDate);
	}
	
	/**
	 * 二进制转字符串
	 * 
	 * @param b
	 * @return
	 */
	private String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	/**
	 * md5加密
	 * 
	 * @param s
	 * @return
	 */
	private String MD5Crypt(String s) {
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			return byte2hex(md);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String outTradeNo;
		private String callbackUrl;
		private String phone;
		
		public SendPartner(String phone, String status, String orderNo, String outTradeNo, String fee, String callbackUrl) {
			this.phone = phone;
			this.fee = fee;
			this.status = status;
			this.orderNo = orderNo;
			this.outTradeNo = outTradeNo;
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		public void run() {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("order_no", orderNo);
				jsonObject.put("out_trade_no", outTradeNo);
				jsonObject.put("phone", phone);
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
		        LogUtil.log("smsOrder call:"+callbackUrl+" sendMsg:"+jsonObject.toString());
		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
}
