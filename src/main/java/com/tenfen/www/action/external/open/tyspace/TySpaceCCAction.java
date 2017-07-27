package com.tenfen.www.action.external.open.tyspace;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.util.SendToBJ;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

/**
 * 天翼空间（长城接入）
 * @author BOBO
 *
 */
public class TySpaceCCAction extends SimpleActionSupport {
	
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
	 * EMP方式
	 */
//	public void generateEmpSubscribeOrder() {
//		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
//		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
//		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
//		String subject = ServletRequestUtils.getStringParameter(request, "subject", null);
//		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
//		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
//		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
//		
//		//返回响应obj
//		JSONObject returnJson = new JSONObject();
//		try {
//			LogUtil.log("changcheng 长城天翼空间参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" subject:"+subject+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
//			if (Utils.isEmpty(sellerKey)) {
//				returnJson.put("code", "1001");
//				returnJson.put("msg", "seller_key参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(appName)) {
//				returnJson.put("code", "1002");
//				returnJson.put("msg", "app_name参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (fee == 0) {
//				returnJson.put("code", "1003");
//				returnJson.put("msg", "fee参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(imsi)) {
//				returnJson.put("code", "1004");
//				returnJson.put("msg", "imsi参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(subject)) {
//				returnJson.put("code", "1005");
//				returnJson.put("msg", "subject参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(outTradeNo)) {
//				returnJson.put("code", "1006");
//				returnJson.put("msg", "out_trade_no参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//通过sellerKey查询渠道信息
//			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
//			if (Utils.isEmpty(tOpenSeller)) {
//				returnJson.put("code", "1007");
//				returnJson.put("msg", "没有找到渠道相关信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			if (tOpenSeller.getStatus() == 0) {
//				returnJson.put("code", "1008");
//				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			//校验sign
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("app_name",appName));
//			queryParamList.add(new TokenParam("subject",subject));
//			queryParamList.add(new TokenParam("fee",fee+""));
//			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
//			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
//			if (!sign.toLowerCase().equals(geneSign)) {
//				returnJson.put("code", "1009");
//				returnJson.put("msg", "消息签名不正确");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//查询关联app
//			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
//			TOpenApp tOpenApp = null;
//			String packageName = null;
//			for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {
//				TOpenApp tOpenAppTmp = tOpenSellerApps.getOpenApp();
//				List<TOpenProductInfo> proList = tOpenAppTmp.getProductList();
//				for (TOpenProductInfo tOpenProductInfo : proList) {
//					if(fee == tOpenProductInfo.getPrice()) {
//						packageName = tOpenProductInfo.getName();
//						tOpenApp = tOpenAppTmp;
//						break;
//					}
//				}
//			}
//			if (Utils.isEmpty(tOpenApp)) {
//				returnJson.put("code", "1010");
//				returnJson.put("msg", "没有找到相关包月信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//查询app对应的channelId和key,及创建订单所需参数
//			//通用参数
//			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
//			String method = "createOrder";
//			String name = URLDecoder.decode(subject, "UTF-8");
//			String detail = "1234";
//			String channelId = tOpenApp.getAppKey();
//			String secret = tOpenApp.getAppSecret();
//			String ver = "1.0";
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			//业务参数
//			String amount = String.valueOf(fee/100);
//			String chargeType = "1";
//			String orderId = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
//			
//			Integer appId = tOpenApp.getId();
//			Integer merchantId = tOpenApp.getMerchantId();
//			Integer sellerId = tOpenSeller.getId();
//			
//			
//			//调用长城接口
//			String res = generateEmpSubscribeOrder(orderId, method, channelId, appNameDecode, name, detail, packageName, ver, timestamp, imsi, amount, chargeType, secret);
//			JSONObject jsonObject = JSONObject.parseObject(res);
//			String code = jsonObject.getString("result");
//			String message = jsonObject.getString("message");
//			if ("8101".equals(code)) {
//				String senderNumber = "";
//				String orderSN = jsonObject.getString("orderSN");
//				
//				TOpenOrder tOpenOrder = new TOpenOrder();
//				tOpenOrder.setImsi(imsi);
//				tOpenOrder.setOrderId(orderId);
//				tOpenOrder.setOutTradeNo(outTradeNo);
//				tOpenOrder.setSellerId(sellerId);
//				tOpenOrder.setAppId(appId);
//				tOpenOrder.setMerchantId(merchantId);
//				tOpenOrder.setSubject(appNameDecode+","+name);
//				tOpenOrder.setSenderNumber(senderNumber);
//				tOpenOrder.setMsgContent(orderSN);
//				tOpenOrder.setFee(fee);
//				openOrderManager.save(tOpenOrder);
//				
//				returnJson.put("code", "1");
//				returnJson.put("msg", "成功");
//				returnJson.put("orderId", orderId);
//				returnJson.put("out_trade_no", outTradeNo);
//				returnJson.put("fee", fee);
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else {
//				returnJson.put("code", code);
//				returnJson.put("msg", message);
//				returnJson.put("out_trade_no", outTradeNo);
//				returnJson.put("fee", fee);
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			returnJson.put("code", "9999");
//			returnJson.put("msg", "系统未知异常");
//			StringUtil.printJson(response, returnJson.toString());
//			return;
//		}
//	}
	
	/**
	 * sso支付
	 */
	public void generateSSOSubscribeOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		String subject = ServletRequestUtils.getStringParameter(request, "subject", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("changcheng 长城天翼空间参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" subject:"+subject+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
			if (Utils.isEmpty(sellerKey)) {
				returnJson.put("code", "1001");
				returnJson.put("msg", "seller_key参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(appName)) {
				returnJson.put("code", "1002");
				returnJson.put("msg", "app_name参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (fee == 0) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "fee参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(imsi)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "imsi参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(subject)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "subject参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("subject",subject));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "消息签名不正确");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询关联app
			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
			TOpenApp tOpenApp = null;
			String packageName = null;
			for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {
				TOpenApp tOpenAppTmp = tOpenSellerApps.getOpenApp();
				List<TOpenProductInfo> proList = tOpenAppTmp.getProductList();
				for (TOpenProductInfo tOpenProductInfo : proList) {
					if(fee == tOpenProductInfo.getPrice()) {
						packageName = tOpenProductInfo.getName();
						tOpenApp = tOpenAppTmp;
						break;
					}
				}
			}
			if (Utils.isEmpty(tOpenApp)) {
				returnJson.put("code", "1010");
				returnJson.put("msg", "没有找到相关包月信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			//通用参数
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			String method = "payForApp";
			String name = URLDecoder.decode(subject, "UTF-8");
			String detail = "1234";
			String channelId = tOpenApp.getAppKey();
			String secret = tOpenApp.getAppSecret();
			String ver = "1.0";
			String timestamp = String.valueOf(System.currentTimeMillis());
			//业务参数
//			String amount = String.valueOf(fee/100);
			String chargeType = "1";
			String orderId = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			//调用支付线程
			new Thread(new CallCC(imsi, appNameDecode, method, name, packageName, detail, channelId, secret, ver, timestamp, fee, chargeType, orderId, appId, merchantId, sellerId, outTradeNo, tOpenSeller.getCallbackUrl())).start();

			returnJson.put("code", "1");
			returnJson.put("msg", "提交成功");
			returnJson .put("orderId", orderId);
			StringUtil.printJson(response, returnJson.toString());
			return;
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 调用长虹创建EMP订单接口
	 * @param orderId
	 * @return
	 */
//	private String generateEmpSubscribeOrder(String orderId, String method, String channel, String app, String name, String detail,
//			String packageName, String ver, String timestamp, String imsi, String amount, String chargeType, String secret) {
//		String responseString = null;
//		try {
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("method",method));
//			queryParamList.add(new TokenParam("channel",channel));
//			queryParamList.add(new TokenParam("app", app));
//			queryParamList.add(new TokenParam("name", name));
//			queryParamList.add(new TokenParam("detail", detail));
//			queryParamList.add(new TokenParam("packageName", packageName));
//			queryParamList.add(new TokenParam("ver", ver));
//			queryParamList.add(new TokenParam("timestamp", timestamp));
//			queryParamList.add(new TokenParam("imsi", imsi));
//			queryParamList.add(new TokenParam("amount", amount));
//			queryParamList.add(new TokenParam("chargeType", chargeType));
//			queryParamList.add(new TokenParam("orderId", orderId));
//			String sig = TokenService.buildTySpaceToken(queryParamList, secret);
//			
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("method",method);
//			map.put("channel",channel);
//			map.put("app",app);
//			map.put("name",name);
//			map.put("detail", detail);
//			map.put("packageName",packageName);
//			map.put("ver",ver);
//			map.put("timestamp",timestamp);
//			map.put("imsi",imsi);
//			map.put("amount",amount);
//			map.put("chargeType",chargeType);
//			map.put("orderId",orderId);
//			map.put("sig",sig);
//			responseString = HttpClientUtils.simplePostInvoke("http://m.52yole.com:8085/CTPay/Subscribe.ashx", map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return responseString;
//	}
	
	/**
	 * 支付接口
	 */
//	public void empPay() {
//		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
//		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
//		String smsCode = ServletRequestUtils.getStringParameter(request, "smsCode", null);
//		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
//		
//		//返回响应obj
//		JSONObject returnJson = new JSONObject();
//		try {
//			if (Utils.isEmpty(sellerKey)) {
//				returnJson.put("code", "1001");
//				returnJson.put("msg", "seller_key参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(orderId)) {
//				returnJson.put("code", "1002");
//				returnJson.put("msg", "orderId参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(smsCode)) {
//				returnJson.put("code", "1003");
//				returnJson.put("msg", "smsCode参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//通过sellerKey查询渠道信息
//			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
//			if (Utils.isEmpty(tOpenSeller)) {
//				returnJson.put("code", "1004");
//				returnJson.put("msg", "没有找到渠道相关信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			if (tOpenSeller.getStatus() == 0) {
//				returnJson.put("code", "1005");
//				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			//校验sign
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("orderId",orderId));
//			queryParamList.add(new TokenParam("smsCode",smsCode));
//			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
//			if (!sign.toLowerCase().equals(geneSign)) {
//				returnJson.put("code", "1006");
//				returnJson.put("msg", "消息签名不正确");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//查询订单
//			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//			if (Utils.isEmpty(tOpenOrder)) {
//				returnJson.put("code", "1007");
//				returnJson.put("msg", "订单未找到");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			Integer appId = tOpenOrder.getAppId();
//			int fee = tOpenOrder.getFee();
//			String packageName = null;
//			TOpenApp tOpenApp = openAppManager.getEntity(appId);
//			List<TOpenProductInfo> proList = tOpenApp.getProductList();
//			for (TOpenProductInfo tOpenProductInfo : proList) {
//				if(fee == tOpenProductInfo.getPrice()) {
//					packageName = tOpenProductInfo.getName();
//					break;
//				}
//			}
//			
//			String subjectString = tOpenOrder.getSubject();
//			String[] subjectArray = subjectString.split(",");
//			//查询app对应的channelId和key,及创建订单所需参数
//			//通用参数
//			String appNameDecode = subjectArray[0];
//			String method = "submitOrder";
//			String name = subjectArray[1];
//			String detail = "1234";
//			String channelId = tOpenApp.getAppKey();
//			String secret = tOpenApp.getAppSecret();
//			String ver = "1.0";
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			//业务参数
//			String orderSN = tOpenOrder.getMsgContent();
//			
//			//调用长虹接口
//			String res = empPay(method, channelId, appNameDecode, name, detail, packageName, ver, timestamp, orderSN, smsCode, secret);
//			JSONObject jsonObject = JSONObject.parseObject(res);
//			String code = jsonObject.getString("result");
//			if ("8201".equals(code)) {
//				returnJson.put("code", "1");
//				returnJson.put("msg", "计费操作提交成功");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else {
//				returnJson.put("code", "1008");
//				returnJson.put("msg", "计费操作提交失败");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			
//		} catch (Exception e) {
//			returnJson.put("code", "9999");
//			returnJson.put("msg", e.getMessage());
//			StringUtil.printJson(response, returnJson.toString());
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	/**
	 * 调用长城emp支付接口
	 */
//	private String empPay(String method, String channel, String app, String name, String detail,
//			String packageName, String ver, String timestamp, String orderSN, String smsCode, String secret) {
//		String responseString = null;
//		try {
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("method",method));
//			queryParamList.add(new TokenParam("channel",channel));
//			queryParamList.add(new TokenParam("app", app));
//			queryParamList.add(new TokenParam("name", name));
//			queryParamList.add(new TokenParam("detail", detail));
//			queryParamList.add(new TokenParam("packageName", packageName));
//			queryParamList.add(new TokenParam("ver", ver));
//			queryParamList.add(new TokenParam("timestamp", timestamp));
//			queryParamList.add(new TokenParam("orderSN", orderSN));
//			queryParamList.add(new TokenParam("smsCode", smsCode));
//			String sig = TokenService.buildTySpaceToken(queryParamList, secret);
//			
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("method",method);
//			map.put("channel",channel);
//			map.put("app",app);
//			map.put("name",name);
//			map.put("detail", detail);
//			map.put("packageName",packageName);
//			map.put("ver",ver);
//			map.put("timestamp",timestamp);
//			map.put("sig",sig);
//			map.put("orderSN",orderSN);
//			map.put("smsCode",smsCode);
//			responseString = HttpClientUtils.simplePostInvoke("http://m.52yole.com:8085/CTPay/Subscribe.ashx", map);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return responseString;
//	}
	
	/**
	 * 天翼空间长虹三方-回调地址
	 */
//	public void subscribeCallBack() {
//		int result = ServletRequestUtils.getIntParameter(request, "result", -1);
//		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
//		LogUtil.log("长虹天翼空间回调参数: result:"+result+" orderId:"+orderId);
//		
//		try {
//			boolean suc = true;
//			//查询订单
//			String status = "4";
//			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//			if (Utils.isEmpty(tOpenOrder)) {
//				suc = false;
//			} else {
//				if ("1".equals(tOpenOrder.getStatus())) {
//					if ((0 == result) || (-99 == result)) {
//						status = "3";
//						tOpenOrder.setStatus(status);
//						String imsi = tOpenOrder.getImsi();
//						tOpenOrder.setPayPhone(imsi);
//						
//						//增加今日量
//						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
//					} else if (-100 == result) {
//						status = "5";
//						tOpenOrder.setStatus(status);
//						String imsi = tOpenOrder.getImsi();
//						tOpenOrder.setPayPhone(imsi);
//					} else {
//						status = "4";
//						tOpenOrder.setStatus(status);
//						String imsi = tOpenOrder.getImsi();
//						tOpenOrder.setPayPhone(imsi);
//					}
//					openOrderManager.save(tOpenOrder);
//					suc = true;
//					
//					//回调渠道
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					String callbackUrl = tOpenSeller.getCallbackUrl();
//					if (!Utils.isEmpty(callbackUrl)) {
//						String outTradeNo = tOpenOrder.getOutTradeNo();
//						String price = tOpenOrder.getFee()+"";
//						new Thread(new SendPartner(status,orderId,outTradeNo,price,callbackUrl)).start();
//					}
//				}
//			}
//			
//			PrintWriter out = response.getWriter();
//	        response.setContentType("text/html");
//	        if (suc) {
//	        	out.print("success");
//			} else {
//				out.print("fail");
//			}
//	        out.flush();
//			out.close();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		
//	}
//	
//	private class SendPartner implements Runnable {
//		private String fee;
//		private String status;
//		private String orderId;
//		private String outTradeNo;
//		private String callbackUrl;
//		
//		public SendPartner(String status, String orderId, String outTradeNo, String fee, String callbackUrl) {
//			this.fee = fee;
//			this.status = status;
//			this.orderId = orderId;
//			this.outTradeNo = outTradeNo;
//			this.callbackUrl = callbackUrl;
//		}
//		
//		@Override
//		public void run() {
//			try {
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("order_no", orderId);
//				jsonObject.put("out_trade_no", outTradeNo);
//				jsonObject.put("fee", fee);
//				jsonObject.put("status", status);
//				
//		        LogUtil.log("chsendMsg:"+jsonObject.toString());
//		        HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
//			} catch (Exception e) {
//				LogUtil.error(e.getMessage(), e);
//			}
//			
//		}
//	}
	
	
	private class CallCC implements Runnable {
		private String imsi;
		private String appNameDecode;
		private String method;
		private String name;
		private String packageName;
		private String detail;
		private String channelId;
		private String secret;
		private String ver;
		private String timestamp;
		private int fee;
		private String chargeType;
		private String orderId;
		private Integer appId;
		private Integer merchantId;
		private Integer sellerId;
		private String outTradeNo;
		private String url;
		
		public CallCC(String imsi, String appNameDecode, String method, String name, String packageName, String detail, String channelId, String secret, String ver, String timestamp,
				int fee, String chargeType, String orderId, Integer appId, Integer merchantId, Integer sellerId, String outTradeNo, String url) {
			this.imsi = imsi;
			this.appNameDecode = appNameDecode;
			this.method = method;
			this.name = name;
			this.packageName = packageName;
			this.detail = detail;
			this.channelId = channelId;
			this.secret = secret;
			this.ver = ver;
			this.timestamp = timestamp;
			this.fee = fee;
			this.chargeType = chargeType;
			this.orderId = orderId;
			this.appId = appId;
			this.merchantId = merchantId;
			this.sellerId = sellerId;
			this.outTradeNo = outTradeNo;
			this.url = url;
		}
		
		@Override
		public void run() {
			try {
				String amount = String.valueOf(fee/100);
				
				JSONObject returnJson = new JSONObject();
				returnJson.put("order_no", orderId);
				returnJson.put("out_trade_no", outTradeNo);
				returnJson.put("fee", fee);
				
				//调用长城sso支付接口
				String res = ssoPay(orderId, method, channelId, appNameDecode, name, detail, packageName, ver, timestamp, imsi, amount, chargeType, secret);
				LogUtil.log("CallTySpaceCCPackageResp:"+res);
				JSONObject jsonObject = JSONObject.parseObject(res);
				String code = jsonObject.getString("result");
				String message = jsonObject.getString("message");
				
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderId);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSubject(appNameDecode+","+name);
				tOpenOrder.setFee(fee);
				tOpenOrder.setSenderNumber("");
				tOpenOrder.setMsgContent("");
				if ("0".equals(code)) {
					tOpenOrder.setStatus("3");
					returnJson.put("code", "3");
					returnJson.put("msg", "成功");
					
					//增加今日量
					openSellerManager.saveOpenSellerApps(sellerId, appId, fee);
				} else {
					tOpenOrder.setStatus("4");
					returnJson.put("code", "4");
					returnJson.put("msg", message);
				}
				String phone = imsiMdnRelationManager.getPhone(imsi);
				if (!Utils.isEmpty(phone)) {
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
					if (!Utils.isEmpty(mobileArea)) {
						tOpenOrder.setProvince(mobileArea.getProvince());
					}
					tOpenOrder.setPayPhone(phone);
					tOpenOrder.setPayTime(new Date());
				} else {
					tOpenOrder.setPayPhone(imsi);
					tOpenOrder.setPayTime(new Date());
				}
				
				openOrderManager.save(tOpenOrder);
				
		        LogUtil.log("sendChannelTySpaceCCPackageMsg:"+returnJson.toString());
		        if (!Utils.isEmpty(url)) {
		        	HttpClientUtils.postJson(url, returnJson.toString());
				}
		        
		        //调用北京平台接口
		        TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
		        String merName = tOpenSeller.getName();
		        SendToBJ.sendOrder(String.valueOf(tOpenOrder.getSellerId()), merName, outTradeNo, appNameDecode, tOpenOrder.getCreateTimeString(), String.valueOf(fee), tOpenOrder.getStatus(), tOpenOrder.getPayTimeString(), imsi, "2");
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
		
		/**
		 * 调用长虹创建EMP订单接口
		 * @param orderId
		 * @return
		 */
		private String ssoPay(String orderId, String method, String channel, String app, String name, String detail,
				String packageName, String ver, String timestamp, String imsi, String amount, String chargeType, String secret) {
			String responseString = null;
			try {
				List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
				queryParamList.add(new TokenParam("method",method));
				queryParamList.add(new TokenParam("channel",channel));
				queryParamList.add(new TokenParam("app", app));
				queryParamList.add(new TokenParam("name", name));
				queryParamList.add(new TokenParam("detail", detail));
				queryParamList.add(new TokenParam("packageName", packageName));
				queryParamList.add(new TokenParam("ver", ver));
				queryParamList.add(new TokenParam("timestamp", timestamp));
				queryParamList.add(new TokenParam("imsi", imsi));
				queryParamList.add(new TokenParam("amount", amount));
				queryParamList.add(new TokenParam("chargeType", chargeType));
				queryParamList.add(new TokenParam("orderId", orderId));
				String sig = TokenService.buildTySpaceToken(queryParamList, secret);

				Map<String, String> map = new HashMap<String, String>();
				map.put("method",method);
				map.put("channel",channel);
				map.put("app",app);
				map.put("name",name);
				map.put("detail", detail);
				map.put("packageName",packageName);
				map.put("ver",ver);
				map.put("timestamp",timestamp);
				map.put("imsi", imsi);
				map.put("amount",amount);
				map.put("chargeType",chargeType);
				map.put("orderId",orderId);
				map.put("sig",sig);
				responseString = HttpClientUtils.simplePostInvoke("http://m.52yole.com:8085/CTPay2/Subscribe.ashx", map);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			return responseString;
		}
	}
}
