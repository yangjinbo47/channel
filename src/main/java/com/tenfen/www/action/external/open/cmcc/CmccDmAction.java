package com.tenfen.www.action.external.open.cmcc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.BASE64;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class CmccDmAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("cmccDm 参数: seller_key:"+sellerKey+" phone:"+phone+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			if (Utils.isEmpty(sellerKey)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "seller_key参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (fee == 0) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "fee参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(phone)) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "phone参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "sign参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("phone",phone));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			if (openSellerAppList.size() > 0) {
				for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {					
					Integer appLimit = tOpenSellerApps.getAppLimit();
					Integer appToday = tOpenSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tOpenApp = tOpenSellerApps.getOpenApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//选择产品
			List<TOpenProductInfo> productInfos = tOpenApp.getProductList();
			TOpenProductInfo tOpenProductInfo = null;
			for (TOpenProductInfo productInfo : productInfos) {
				int price = productInfo.getPrice();
				if (price == fee) {
					tOpenProductInfo = productInfo;
				}
			}
			if (Utils.isEmpty(tOpenProductInfo)) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相应的产品");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			String AppID = tOpenApp.getAppKey();
			String AppKey = tOpenApp.getAppSecret();
			String ChannelID = tOpenApp.getClientId();
			String PayCode = tOpenProductInfo.getProductId();
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			String TimeStamp = String.valueOf(System.currentTimeMillis()/1000);
			String mySign = orderNo+"&"+AppID+"&"+AppKey+"&"+PayCode+"&"+TimeStamp+"&"+ChannelID;
			String myMd5 = MD5.getMD5(mySign).toUpperCase();
			String Signature = URLEncoder.encode(BASE64.encode(myMd5.getBytes()),"UTF-8");
			String url = "http://wap.dm.10086.cn/apay/orderHandle.jsp?RequestID="+orderNo+"&AppID="+AppID+"&PayCode="+PayCode+"&TimeStamp="+TimeStamp+"&ChannelID="+ChannelID+"&Signature="+Signature;
			
			//创建订单
			TOpenOrder tOpenOrder = new TOpenOrder();
			tOpenOrder.setImsi(phone);
			tOpenOrder.setOrderId(orderNo);
			tOpenOrder.setOutTradeNo(outTradeNo);
			tOpenOrder.setAppId(appId);
			tOpenOrder.setMerchantId(merchantId);
			tOpenOrder.setSellerId(sellerId);
			tOpenOrder.setSubject(tOpenApp.getName());
			tOpenOrder.setSenderNumber(tOpenProductInfo.getCode());
			tOpenOrder.setMsgContent(orderNo);
			tOpenOrder.setFee(fee);
			tOpenOrder.setPayPhone(phone);
			String province = null;
			if (Utils.checkCellPhone(phone)) {
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				if (mobileArea != null) {
					province = mobileArea.getProvince();
				}
				tOpenOrder.setProvince(province);
			}
			openOrderManager.save(tOpenOrder);
			
			returnJson.put("code", "1");
			returnJson.put("msg", url);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	public void generateSubOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String user = ServletRequestUtils.getStringParameter(request, "user", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("cmccDm 包月参数: seller_key:"+sellerKey+" user:"+user+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			if (Utils.isEmpty(sellerKey)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "seller_key参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (fee == 0) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "fee参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(user)) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "user参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "sign参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("user",user));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			boolean b = false;//检测应用是否全部达到限量值
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			if (openSellerAppList.size() > 0) {
				for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {					
					Integer appLimit = tOpenSellerApps.getAppLimit();
					Integer appToday = tOpenSellerApps.getAppToday();
					if (appLimit != -1) {
						if (appToday >= appLimit) {
							continue;
						}
					}
					tOpenApp = tOpenSellerApps.getOpenApp();
					b = true;
				}
			} else {
				returnJson.put("code", "1009");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			TOpenProductInfo tOpenProductInfo = null;
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			for (TOpenProductInfo openProductInfo : proList) {
				if(fee == openProductInfo.getPrice()) {
					tOpenProductInfo = openProductInfo;
				}
			}
			if (Utils.isEmpty(tOpenProductInfo)) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有相应额度的产品");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			String AppID = tOpenProductInfo.getCode();
			String AppKey = tOpenProductInfo.getInstruction();
			String ChannelID = tOpenApp.getClientId();
			String PayCode = tOpenProductInfo.getProductId();
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			String TimeStamp = String.valueOf(System.currentTimeMillis()/1000);
			String mySign = orderNo+"&"+AppID+"&"+AppKey+"&"+PayCode+"&"+TimeStamp+"&"+ChannelID;
			String myMd5 = MD5.getMD5(mySign).toUpperCase();
			String Signature = URLEncoder.encode(BASE64.encode(myMd5.getBytes()),"UTF-8");
			String url = "http://wap.dm.10086.cn/apay/monthlyOrderHandle.jsp?RequestID="+orderNo+"&AppID="+AppID+"&PayCode="+PayCode+"&TimeStamp="+TimeStamp+"&ChannelID="+ChannelID+"&Signature="+Signature;
			
			//创建订单
			TOpenOrder tOpenOrder = new TOpenOrder();
			tOpenOrder.setImsi(user);
			tOpenOrder.setOrderId(orderNo);
			tOpenOrder.setOutTradeNo(outTradeNo);
			tOpenOrder.setAppId(appId);
			tOpenOrder.setMerchantId(merchantId);
			tOpenOrder.setSellerId(sellerId);
			tOpenOrder.setSubject(tOpenApp.getName());
			tOpenOrder.setSenderNumber(tOpenProductInfo.getCode());
			tOpenOrder.setMsgContent(orderNo);
			tOpenOrder.setFee(fee);
			tOpenOrder.setPayPhone(user);
			String province = null;
			if (Utils.checkCellPhone(user)) {
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(user);
				if (mobileArea != null) {
					province = mobileArea.getProvince();
				}
				tOpenOrder.setProvince(province);
			}
			openOrderManager.save(tOpenOrder);
			
			returnJson.put("code", "1");
			returnJson.put("msg", url);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e){
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	
	
	
	public String callBack() throws Exception {
		String success = "1";
		String desc = null;
		
		// 读取请求内容
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine())!=null){
			sb.append(line);
		}
		LogUtil.log("cmcc 接收到的xml："+sb.toString());
		
		String reqBody = sb.toString();
		String msgType = StringUtils.substringBetween(reqBody, "<MsgType>", "</MsgType>");
		if ("ChargedNotifyReq".equals(msgType)) {//点播
			try {
				String orderId = StringUtils.substringBetween(reqBody, "<ExData>", "</ExData>");
				String chargeTime = StringUtils.substringBetween(reqBody, "<ChargeTime>", "</ChargeTime>");
				String fee = StringUtils.substringBetween(reqBody, "<Cost>", "</Cost>");
				
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
				if (Utils.isEmpty(tOpenOrder)) {
					tOpenOrder = new TOpenOrder();
					tOpenOrder.setImsi("4600");
					tOpenOrder.setOrderId(orderId);
					tOpenOrder.setOutTradeNo(orderId);
					tOpenOrder.setMerchantId(32);
					tOpenOrder.setAppId(62);
					tOpenOrder.setSellerId(30);
					tOpenOrder.setSubject("咪咕动漫点播");
					tOpenOrder.setSenderNumber("");
					tOpenOrder.setMsgContent("");
					tOpenOrder.setFee(Integer.parseInt(fee));
					tOpenOrder.setStatus("3");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					tOpenOrder.setPayTime(sdf.parse(chargeTime));
					openOrderManager.save(tOpenOrder);
				} else {
					tOpenOrder.setStatus("3");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					tOpenOrder.setPayTime(sdf.parse(chargeTime));
					openOrderManager.save(tOpenOrder);
				}
				//增加今日量
				openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String outTradeNo = tOpenOrder.getOutTradeNo();
					new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
				}
				
				success = "0";
				desc = "成功";
			} catch (Exception e) {
				success = "1";
				desc = "未知错误";
				LogUtil.error(e.getMessage(), e);
			}
			
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Response><MsgType>"+msgType+"</MsgType><ReturnCode>"+success+"</ReturnCode><ResultDesc>"+desc+"</ResultDesc></Response>";
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			out.println(xml);
			out.flush();
			out.close();
		} else if ("MonthlyOrderNotifyReq".equals(msgType)) {//包月
			try {
				String orderId = StringUtils.substringBetween(reqBody, "<OrderID>", "</OrderID>");
				String chargeTimeStr = StringUtils.substringBetween(reqBody, "<ChargeTime>", "</ChargeTime>");
				String rentSuccess = StringUtils.substringBetween(reqBody, "<RentSuccess>", "</RentSuccess>");
				String updateType = StringUtils.substringBetween(reqBody, "<UpdateType>", "</UpdateType>");
				String fee = StringUtils.substringBetween(reqBody, "<Fee>", "</Fee>");
				
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
				if (Utils.isEmpty(tOpenOrder)) {
					tOpenOrder = new TOpenOrder();
					tOpenOrder.setImsi("4600");
					tOpenOrder.setOrderId(orderId);
					tOpenOrder.setOutTradeNo(orderId);
					tOpenOrder.setMerchantId(32);
					tOpenOrder.setAppId(67);
					tOpenOrder.setSellerId(30);
					tOpenOrder.setSubject("咪咕动漫包月");
					tOpenOrder.setSenderNumber("");
					tOpenOrder.setMsgContent("");
					tOpenOrder.setFee(Integer.parseInt(fee));
				}
				if ("1".equals(updateType) && "0".equals(rentSuccess)) {//增加订购关系&扣费成功
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					Date chargeTime = sdf.parse(chargeTimeStr);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(chargeTime);
					calendar.add(Calendar.HOUR, 8);
					chargeTime = calendar.getTime();
					
					tOpenOrder.setPayTime(chargeTime);
					tOpenOrder.setStatus("3");
					
					//增加今日量
					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
					
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tOpenOrder.getOutTradeNo();
						new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					}
				} else if ("2".equals(updateType)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					Date chargeTime = sdf.parse(chargeTimeStr);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(chargeTime);
					calendar.add(Calendar.HOUR, 8);
					chargeTime = calendar.getTime();
					
					tOpenOrder.setUnsubscribeTime(chargeTime);
					tOpenOrder.setStatus("5");
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tOpenOrder.getOutTradeNo();
						new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					}
				} else {
					tOpenOrder.setStatus("4");
				}
				openOrderManager.save(tOpenOrder);
			} catch (Exception e) {
				success = "1";
				desc = "未知错误";
				LogUtil.error(e.getMessage(), e);
			}
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Response><MsgType>"+msgType+"</MsgType><Result>"+success+"</Result><ErrorMsg>"+desc+"</ErrorMsg></Response>";
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			out.println(xml);
			out.flush();
			out.close();
		}
		
		return null;
	}
	
	/**
	 * 天翼能力开放-回调地址
	 * @throws Exception 
	 */
//	public String callBack() throws Exception {
//		String success = "1";
//		String desc = null;
//		
//		// 读取请求内容
//		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
//		String line = null;
//		StringBuilder sb = new StringBuilder();
//		while((line = br.readLine())!=null){
//			sb.append(line);
//		}
//		LogUtil.log("cmcc 接收到的xml："+sb.toString());
//		
//		String reqBody = sb.toString();
//		String msgType = StringUtils.substringBetween(reqBody, "<MsgType>", "</MsgType>");
//		if ("ChargedNotifyReq".equals(msgType)) {//点播
//			try {
//				String orderId = StringUtils.substringBetween(reqBody, "<ExData>", "</ExData>");
//				String chargeTime = StringUtils.substringBetween(reqBody, "<ChargeTime>", "</ChargeTime>");
//				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//				if (!Utils.isEmpty(tOpenOrder)) {
//					tOpenOrder.setStatus("3");
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					tOpenOrder.setPayTime(sdf.parse(chargeTime));
//					openOrderManager.save(tOpenOrder);
//					//增加今日量
//					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
//					
//					//回调渠道
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					String callbackUrl = tOpenSeller.getCallbackUrl();
//					if (!Utils.isEmpty(callbackUrl)) {
//						String outTradeNo = tOpenOrder.getOutTradeNo();
//						new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
//					}
//					
//					success = "0";
//					desc = "成功";
//				} else {
//					success = "1";
//					desc = "订单未找到";
//				}
//			} catch (Exception e) {
//				success = "1";
//				desc = "未知错误";
//				LogUtil.error(e.getMessage(), e);
//			}
//			
//			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Response><MsgType>"+msgType+"</MsgType><ReturnCode>"+success+"</ReturnCode><ResultDesc>"+desc+"</ResultDesc></Response>";
//			PrintWriter out = response.getWriter();
//			response.setContentType("text/xml");
//			out.println(xml);
//			out.flush();
//			out.close();
//		} else if ("MonthlyOrderNotifyReq".equals(msgType)) {//包月
//			try {
//				String orderId = StringUtils.substringBetween(reqBody, "<OrderID>", "</OrderID>");
//				String chargeTimeStr = StringUtils.substringBetween(reqBody, "<ChargeTime>", "</ChargeTime>");
//				String rentSuccess = StringUtils.substringBetween(reqBody, "<RentSuccess>", "</RentSuccess>");
//				String updateType = StringUtils.substringBetween(reqBody, "<UpdateType>", "</UpdateType>");
//				
//				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//				if (!Utils.isEmpty(tOpenOrder)) {
//					if ("1".equals(updateType) && "0".equals(rentSuccess)) {//增加订购关系&扣费成功
//						tOpenOrder.setStatus("3");
//						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//						Date chargeTime = sdf.parse(chargeTimeStr);
//						tOpenOrder.setPayTime(chargeTime);
//						openOrderManager.save(tOpenOrder);
//						//增加今日量
//						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
//						
//						//回调渠道
//						TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//						String callbackUrl = tOpenSeller.getCallbackUrl();
//						if (!Utils.isEmpty(callbackUrl)) {
//							String outTradeNo = tOpenOrder.getOutTradeNo();
//							new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
//						}
//					} 
////					if ("2".equals(updateType)) {//删除订购关系
////						tOpenOrder.setStatus("5");
////						openOrderManager.save(tOpenOrder);
////						//回调渠道
////						TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
////						String callbackUrl = tOpenSeller.getCallbackUrl();
////						if (!Utils.isEmpty(callbackUrl)) {
////							String outTradeNo = tOpenOrder.getOutTradeNo();
////							new Thread(new SendPartner(tOpenOrder.getStatus(),orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
////						}
////					} 
//					else {
//						tOpenOrder.setStatus("4");
//						openOrderManager.save(tOpenOrder);
//					}
//					success = "0";
//					desc = "成功";
//				} else {
//					success = "1";
//					desc = "订单未找到";
//				}
//			} catch (Exception e) {
//				success = "1";
//				desc = "未知错误";
//				LogUtil.error(e.getMessage(), e);
//			}
//			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><Response><MsgType>"+msgType+"</MsgType><Result>"+success+"</Result><ErrorMsg>"+desc+"</ErrorMsg></Response>";
//			PrintWriter out = response.getWriter();
//			response.setContentType("text/xml");
//			out.println(xml);
//			out.flush();
//			out.close();
//		}
//		
//		return null;
//	}
	
	public static void main(String[] args) {
		try {
			String time = "20170209094125";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = sdf.parse(time);
			System.out.println(date);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	private class SendPartner implements Runnable {
		private String fee;
		private String status;
		private String orderNo;
		private String outTradeNo;
		private String callbackUrl;
		
		public SendPartner(String status,String orderNo,String outTradeNo,String fee,String callbackUrl) {
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
				jsonObject.put("fee", fee);
				jsonObject.put("status", status);
				
				if (!Utils.isEmpty(callbackUrl)) {
			        LogUtil.log("sendCmccDmMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
