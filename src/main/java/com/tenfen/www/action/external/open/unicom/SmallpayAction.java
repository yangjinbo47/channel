package com.tenfen.www.action.external.open.unicom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.open.TOpenSellerApps;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.encrypt.ThreeDes;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SmallpayAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private CacheFactory cacheFactory;
	
	public String input() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		try {
			if (Utils.isEmpty(sellerKey)) {
				setRequestAttribute("msg", "seller_key参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(appName)) {
				setRequestAttribute("msg", "app_name参数不能为空");
				return "smallpayfail";
			} else if (fee == 0) {
				setRequestAttribute("msg", "fee参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(outTradeNo)) {
				setRequestAttribute("msg", "out_trade_no参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(sign)) {
				setRequestAttribute("msg", "sign参数不能为空");
				return "smallpayfail";
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				setRequestAttribute("msg", "没有找到渠道相关信息");
				return "smallpayfail";
			}
			if (tOpenSeller.getStatus() == 0) {
				setRequestAttribute("msg", "该渠道已被关闭，请联系管理员");
				return "smallpayfail";
			}
			//校验sign
			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign) && !"test".equals(sign)) {
				setRequestAttribute("msg", "消息签名不正确");
				return "smallpayfail";
			}
			
			setRequestAttribute("sellerKey", sellerKey);
			setRequestAttribute("appName", appName);
			setRequestAttribute("fee", fee);
			setRequestAttribute("outTradeNo", outTradeNo);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			setRequestAttribute("msg", "未知异常");
			return "smallpayfail";
		}
		return "smallpayinput";
	}
	
	public String generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String imsi = phone;
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		
		//返回响应obj
		try {
			String appNameDecode = new String(Base64.decodeBase64(appName));
			LogUtil.log("smallpay generate params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appNameDecode+" fee:"+fee+" outTradeNo:"+outTradeNo);
			if (Utils.isEmpty(sellerKey)) {
				setRequestAttribute("msg", "seller_key参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(appName)) {
				setRequestAttribute("msg", "app_name参数不能为空");
				return "smallpayfail";
			} else if (fee == 0) {
				setRequestAttribute("msg", "fee参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(imsi)) {
				setRequestAttribute("msg", "imsi参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(outTradeNo)) {
				setRequestAttribute("msg", "out_trade_no参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(phone)) {
				setRequestAttribute("msg", "phone参数不能为空");
				return "smallpayfail";
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				setRequestAttribute("msg", "没有找到渠道相关信息");
				return "smallpayfail";
			}
			if (tOpenSeller.getStatus() == 0) {
				setRequestAttribute("msg", "该渠道已被关闭，请联系管理员");
				return "smallpayfail";
			}
			
			//查询关联app
			boolean limit = false;//检测应用是否全部达到限量值
			TOpenProductInfo pro = null;
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
					List<TOpenProductInfo> proList = tOpenApp.getProductList();
					for (TOpenProductInfo tOpenProductInfo : proList) {
						if (tOpenProductInfo.getPrice() == fee) {
							limit = true;
							pro = tOpenProductInfo;
						}
					}
					if (!Utils.isEmpty(pro)) {
						break;
					}
				}
			} else {
				setRequestAttribute("msg", "没有找到相关app信息");
				return "smallpayfail";
			}
			if (!limit) {
				setRequestAttribute("msg", "未找到符合条件app");
				return "smallpayfail";
			}
			
			////////检测省份是否到量
			Integer appId = tOpenApp.getId();
			String province = null;
			String provinceEncoder = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province, "UTF-8");
			}
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			String day = sdfDay.format(calendar.getTime());
			Integer appdaylimit_conf = null;
			if (!Utils.isEmpty(appId)) {
				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
				appdaylimit_conf = tOpenAppLimit.getDayLimit();
			}
			String SMALLPAY_APP_PROVINCE_DAY_LIMIT = "smallpay_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			//判断app日限是否到达
			Integer appdaylimit = (Integer)mc.getCache(SMALLPAY_APP_PROVINCE_DAY_LIMIT);
			appdaylimit = appdaylimit == null ? 0 : appdaylimit;
			if (appdaylimit_conf == -1) {//无限制
				//不处理
			} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
				//不处理
			} else {
				setRequestAttribute("msg", "该省份已达到当日推送量");
				return "smallpayfail";
			}
			
			String key = tOpenApp.getAppKey();//cpid
			String secret = tOpenApp.getAppSecret();//key
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
//			JSONObject json = generateWoPlusOrder(appNameDecode, outTradeNo, appId, merchantId, sellerId, key, secret, pro.getPrice(), imsi, phone, pro.getInstruction(), pro.getCode(), pro.getType());
			JSONObject json = generateSmallpayOrder(outTradeNo, appId, merchantId, sellerId, key, secret, pro.getPrice(), imsi, phone, pro.getProductId(), pro.getName(), appNameDecode, pro.getCode(), pro.getType());
			int code = json.getInteger("code");
			if (code == 0) {
				String orderId = json.getString("order_id");
				String transSeq = json.getString("trans_seq");
				setRequestAttribute("orderId", orderId);
				setRequestAttribute("outTradeNo", outTradeNo);
				setRequestAttribute("transSeq", transSeq);
				return "smallpaycodeinput";
			} else {
				String msg = json.getString("msg");
				setRequestAttribute("msg", msg);
				return "smallpayfail";
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
	
//	public void generateOrder() {
//		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
//		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
//		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
//		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
//		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
//		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
//		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
//		
//		//返回响应obj
//		JSONObject returnJson = new JSONObject();
//		try {
//			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
//			LogUtil.log("smallpay generate params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appNameDecode+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
//			} else if (Utils.isEmpty(sign)) {
//				returnJson.put("code", "1005");
//				returnJson.put("msg", "sign参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(outTradeNo)) {
//				returnJson.put("code", "1006");
//				returnJson.put("msg", "out_trade_no参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(phone)) {
//				returnJson.put("code", "1007");
//				returnJson.put("msg", "phone参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//通过sellerKey查询渠道信息
//			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
//			if (Utils.isEmpty(tOpenSeller)) {
//				returnJson.put("code", "1008");
//				returnJson.put("msg", "没有找到渠道相关信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			if (tOpenSeller.getStatus() == 0) {
//				returnJson.put("code", "1009");
//				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			//校验sign
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>();
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("phone",phone));
//			queryParamList.add(new TokenParam("app_name",appName));
//			queryParamList.add(new TokenParam("fee",fee+""));
//			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
//			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
//			if (!sign.toLowerCase().equals(geneSign) && !"test".equals(sign)) {
//				returnJson.put("code", "1010");
//				returnJson.put("msg", "消息签名不正确");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			//查询关联app
//			boolean limit = false;//检测应用是否全部达到限量值
//			TOpenProductInfo pro = null;
//			List<TOpenSellerApps> openSellerAppList = tOpenSeller.getSellerApps();
//			TOpenApp tOpenApp = null;
//			if (openSellerAppList.size() > 0) {
//				for (TOpenSellerApps tOpenSellerApps : openSellerAppList) {					
//					Integer appLimit = tOpenSellerApps.getAppLimit();
//					Integer appToday = tOpenSellerApps.getAppToday();
//					if (appLimit != -1) {
//						if (appToday >= appLimit) {
//							continue;
//						}
//					}
//					tOpenApp = tOpenSellerApps.getOpenApp();
//					List<TOpenProductInfo> proList = tOpenApp.getProductList();
//					for (TOpenProductInfo tOpenProductInfo : proList) {
////						if (tOpenProductInfo.getPrice() == fee && tOpenProductInfo.getType() == chargeType) {
//						if (tOpenProductInfo.getPrice() == fee) {
//							limit = true;
//							pro = tOpenProductInfo;
//						}
//					}
//					if (!Utils.isEmpty(pro)) {
//						break;
//					}
//				}
//			} else {
//				returnJson.put("code", "1011");
//				returnJson.put("msg", "没有找到相关app信息");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			if (!limit) {
//				returnJson.put("code", "1012");
//				returnJson.put("msg", "未找到符合条件app");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			////////检测省份是否到量
//			Integer appId = tOpenApp.getId();
//			String province = null;
//			String provinceEncoder = null;
//			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
//			if (mobileArea != null) {
//				province = mobileArea.getProvince();
//				provinceEncoder = URLEncoder.encode(province, "UTF-8");
//			}
//			Calendar calendar = Calendar.getInstance();
//			//格式化时间
//			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
//			String day = sdfDay.format(calendar.getTime());
//			Integer appdaylimit_conf = null;
//			if (!Utils.isEmpty(appId)) {
//				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
//				appdaylimit_conf = tOpenAppLimit.getDayLimit();
//			}
//			String SMALLPAY_APP_PROVINCE_DAY_LIMIT = "smallpay_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
//			ICacheClient mc = cacheFactory.getCommonCacheClient();
//			//判断app日限是否到达
//			Integer appdaylimit = (Integer)mc.getCache(SMALLPAY_APP_PROVINCE_DAY_LIMIT);
//			appdaylimit = appdaylimit == null ? 0 : appdaylimit;
//			if (appdaylimit_conf == -1) {//无限制
//				//不处理
//			} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
//				//不处理
//			} else {
//				returnJson.put("code", "1013");
//				returnJson.put("msg", "该省份已达到当日推送量");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			String key = tOpenApp.getAppKey();//cpid
//			String secret = tOpenApp.getAppSecret();//key
//			Integer merchantId = tOpenApp.getMerchantId();
//			Integer sellerId = tOpenSeller.getId();
//			
////			JSONObject json = generateWoPlusOrder(appNameDecode, outTradeNo, appId, merchantId, sellerId, key, secret, pro.getPrice(), imsi, phone, pro.getInstruction(), pro.getCode(), pro.getType());
//			JSONObject json = generateSmallpayOrder(outTradeNo, appId, merchantId, sellerId, key, secret, pro.getPrice(), imsi, phone, pro.getProductId(), pro.getName(), appNameDecode, pro.getCode(), pro.getType());
//			StringUtil.printJson(response, json.toString());
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	private JSONObject generateSmallpayOrder(String outTradeNo, Integer appId, Integer merchantId, Integer sellerId, String key, String secret, Integer price, String imsi, String phone, String productId, String productName, String appName, String sendNumber, Integer type) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderSeq = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateSmallpayOrder(phone, orderSeq, key, secret, productId, productName, type);
			JSONObject json = JSONObject.parseObject(result);
			if (!Utils.isEmpty(json)) {
				Integer code = json.getInteger("code");
				String msg = json.getString("msg");
				
				if (code == 0) {
					String transSeq = json.getString("transSeq");//小额平台交易流水号
					//创建订单
					TOpenOrder tOpenOrder = new TOpenOrder();
					tOpenOrder.setImsi(imsi);
					tOpenOrder.setOrderId(orderSeq);
					tOpenOrder.setOutTradeNo(outTradeNo);
					tOpenOrder.setAppId(appId);
					tOpenOrder.setMerchantId(merchantId);
					tOpenOrder.setSellerId(sellerId);
					tOpenOrder.setSubject(appName);
					tOpenOrder.setSenderNumber(sendNumber);
					tOpenOrder.setMsgContent(transSeq);
					tOpenOrder.setFee(price);
					tOpenOrder.setPayPhone(phone);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
					if (!Utils.isEmpty(mobileArea)) {
						tOpenOrder.setProvince(mobileArea.getProvince());
					}
					openOrderManager.save(tOpenOrder);
					
					returnJsonMsgObj = new JSONObject();
					returnJsonMsgObj.put("order_id", orderSeq);
					returnJsonMsgObj.put("out_trade_no", outTradeNo);
					returnJsonMsgObj.put("trans_seq", transSeq);
					returnJsonMsgObj.put("code", code);
					returnJsonMsgObj.put("fee", price);
					returnJsonMsgObj.put("msg", msg);
				} else {
					returnJsonMsgObj = new JSONObject();
					returnJsonMsgObj.put("code", code);
					returnJsonMsgObj.put("out_trade_no", outTradeNo);
					returnJsonMsgObj.put("fee", price);
					returnJsonMsgObj.put("msg", msg);
				}
			} else {
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("code", "9999");
				returnJsonMsgObj.put("msg", "联通系统未返回订单");
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	private String generateSmallpayOrder(String phone, String orderSeq, String key, String secret, String productId, String productName, Integer type) {
		String resultData = null;
		try {
			String orderType = "0";
			if (type == 1) {
				orderType = "0";//按次点播
			} else if (type == 2) {
				orderType = "1";//周期性计费
			}
			String clientIp = "127.0.0.1";
			String time = DateUtil.getCurrentTimestamp("yyyyMMddHHmmss");
			String payMode = "2";// 2-验证码支付 3-二次确认支付
			String orderDesc = productName;
			
			String sign = MD5.getMD5(key+orderSeq+phone+clientIp+time+orderType+payMode+productId+productName+orderDesc+secret);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("appKey", key);
			jsonObject.put("orderSeq", orderSeq);
			jsonObject.put("payer", phone);
			jsonObject.put("clientIp", clientIp);
			jsonObject.put("time", time);
			jsonObject.put("type", orderType);
			jsonObject.put("payMode", payMode);
			jsonObject.put("productId", productId);
			jsonObject.put("productName", productName);
			jsonObject.put("orderDesc", orderDesc);
			jsonObject.put("sign", sign);
			
			String threeDesKey = "channel3des_012345678910";
			String encode = Base64.encodeBase64String(ThreeDes.encryptMode(threeDesKey.getBytes(), jsonObject.toString().getBytes()));
			String res = HttpClientUtils.postJson("http://112.96.29.64:9000/unipay/test/order.do", encode);
			resultData = new String(ThreeDes.decryptMode(threeDesKey.getBytes(), Base64.decodeBase64(res)));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return resultData;
	}
	
	
	public void generateSubOrder() {
		LogUtil.log("进入smallpay包月");
	}
	
	public String pay() {
		String orderId = ServletRequestUtils.getStringParameter(request, "order_id", null);
		String transSeq = ServletRequestUtils.getStringParameter(request, "trans_seq", null);
		String code = ServletRequestUtils.getStringParameter(request, "code", null);
		
		try {
			if (Utils.isEmpty(orderId)) {
				setRequestAttribute("msg", "order_id参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(transSeq)) {
				setRequestAttribute("msg", "trans_seq参数不能为空");
				return "smallpayfail";
			} else if (Utils.isEmpty(code)) {
				setRequestAttribute("msg", "code参数不能为空");
				return "smallpayfail";
			}
			
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				Integer appId = tOpenOrder.getAppId();
				TOpenApp tOpenApp = openAppManager.get(appId);
				String appKey = tOpenApp.getAppKey();
				String secret = tOpenApp.getAppSecret();
				String clientIp = "127.0.0.1";
				String sign = MD5.getMD5(appKey+transSeq+code+clientIp+secret);
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("appKey", appKey);
				jsonObject.put("transSeq", transSeq);
				jsonObject.put("code", code);
				jsonObject.put("clientIp", clientIp);
				jsonObject.put("sign", sign);
				
				String threeDesKey = "channel3des_012345678910";
				String encode = Base64.encodeBase64String(ThreeDes.encryptMode(threeDesKey.getBytes(), jsonObject.toString().getBytes()));
				String res = HttpClientUtils.postJson("http://112.96.29.64:9000/unipay/test/pay.do", encode);
				String resultData = new String(ThreeDes.decryptMode(threeDesKey.getBytes(), Base64.decodeBase64(res)));
				JSONObject resultJson = JSONObject.parseObject(resultData);
				String msg = resultJson.getString("msg");
				setRequestAttribute("msg", msg);
				return "smallpayfail";
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			setRequestAttribute("msg", "未知异常");
			return "smallpayfail";
		}
		return null;
	}
//	public void pay() {
//		String orderId = ServletRequestUtils.getStringParameter(request, "order_id", null);
//		String transSeq = ServletRequestUtils.getStringParameter(request, "trans_seq", null);
//		String code = ServletRequestUtils.getStringParameter(request, "code", null);
//		
//		JSONObject returnJson = new JSONObject();
//		try {
//			if (Utils.isEmpty(orderId)) {
//				returnJson.put("code", "1001");
//				returnJson.put("msg", "order_id参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(transSeq)) {
//				returnJson.put("code", "1002");
//				returnJson.put("msg", "trans_seq参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			} else if (Utils.isEmpty(code)) {
//				returnJson.put("code", "1003");
//				returnJson.put("msg", "code参数不能为空");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
//			
//			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//			if (!Utils.isEmpty(tOpenOrder)) {
//				Integer appId = tOpenOrder.getAppId();
//				TOpenApp tOpenApp = openAppManager.get(appId);
//				String appKey = tOpenApp.getAppKey();
//				String secret = tOpenApp.getAppSecret();
//				String clientIp = "127.0.0.1";
//				String sign = MD5.getMD5(appKey+transSeq+code+clientIp+secret);
//				
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("appKey", appKey);
//				jsonObject.put("transSeq", transSeq);
//				jsonObject.put("code", code);
//				jsonObject.put("clientIp", clientIp);
//				jsonObject.put("sign", sign);
//				
//				String threeDesKey = "channel3des_012345678910";
//				String encode = Base64.encodeBase64String(ThreeDes.encryptMode(threeDesKey.getBytes(), jsonObject.toString().getBytes()));
//				String res = HttpClientUtils.postJson("http://112.96.29.64:9000/unipay/test/pay.do", encode);
//				String resultData = new String(ThreeDes.decryptMode(threeDesKey.getBytes(), Base64.decodeBase64(res)));
//				JSONObject resultJson = JSONObject.parseObject(resultData);
//				returnJson.put("code", resultJson.getString("code"));
//				returnJson.put("msg", returnJson.getString("msg"));
//			}
//		} catch (Exception e) {
//			returnJson.put("code", 9999);
//			returnJson.put("msg", "未知异常");
//			LogUtil.error(e.getMessage(), e);
//		}
//		StringUtil.printJson(response, returnJson.toString());
//	}
	
	public String callBack() throws Exception {
		// 读取请求内容
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine())!=null){
			sb.append(line);
		}
		LogUtil.log("smallpay接收到的json："+sb.toString());
		
		String success = "fail";
		try {
			JSONObject json = JSONObject.parseObject(sb.toString());
			String transReq = json.getString("transReq");
			String orderSeq = json.getString("orderSeq");
			String paySeq = json.getString("paySeq");
			String payDate = json.getString("payDate");
			String amount = json.getString("amount");
			String code = json.getString("code");
			String msg = json.getString("msg");
			String sign = json.getString("sign");
			
			int reduce = 0;
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderSeq);
			if (!Utils.isEmpty(tOpenOrder)) {
				Integer appId = tOpenOrder.getAppId();
				TOpenApp tOpenApp = openAppManager.get(appId);
				String secret = tOpenApp.getAppSecret();
				String geneSign = MD5.getMD5(transReq+orderSeq+paySeq+payDate+amount+code+msg+secret);
				if (geneSign.equals(sign)) {
					String orderStatus = "4";
					if ("0000".equals(code)) {
						orderStatus = "3";
					} else {
						orderStatus = "4";
					}
					tOpenOrder.setStatus(orderStatus);
					tOpenOrder.setPayTime(new Date());
					String province = tOpenOrder.getProvince();
					if ("0000".equals(code)) {
						//是否扣量
						TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
						double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
						double rate = new Random().nextDouble();
						if (rate < reduce_conf) {
							reduce = 1;
							tOpenOrder.setReduce(reduce);
						}
					}
					openOrderManager.save(tOpenOrder);
					
					//增加今日量
					if ("3".equals(orderStatus)) {
						openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
					}
					success = "ok";
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {
						String outTradeNo = tOpenOrder.getOutTradeNo();
						if (reduce != 1) {//不扣量
							new Thread(new SendPartner(orderStatus,orderSeq,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						} else {
							new Thread(new SendPartner("4",orderSeq,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						}
					}
					
				}
			}
			
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println(success);
			out.flush();
			out.close();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		return null;
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
			        LogUtil.log("sendSmallpayMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
