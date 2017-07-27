package com.tenfen.www.action.external.open.woread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
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
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class WoReadOpenAction extends SimpleActionSupport{

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
	private BlackListManager blackListManager;
	@Autowired
	private CacheFactory cacheFactory;
	
	
	/**
	 * 生成单笔订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String imsi = ServletRequestUtils.getStringParameter(request, "imsi", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		int chargeFee = 0;
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("woread 短代参数: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "sign参数不能为空");
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
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
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
				returnJson.put("code", "1010");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "已达到今日限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String key = tOpenApp.getAppKey();
			Integer appId = tOpenApp.getId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			JSONArray returnJsonArray = new JSONArray();
			//通过app查询关联的计费信息
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			Collections.sort(proList);
			while (fee >= 100) {
				for (int i = 0; i < proList.size(); i++) {
					chargeFee = proList.get(i).getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						JSONObject json = generateWoReadOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, proList.get(i).getCode(), proList.get(i).getInstruction(), key, chargeFee, imsi);
						returnJsonArray.add(json);
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", returnJsonArray);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 生成单笔订单action
	 * app_name需要申报
	 * 计费点需要申报，事先录入产品列表
	 */
	public void generateDyOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		int chargeFee = 0;
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("woread 短验参数: seller_key:"+sellerKey+" phone:"+phone+" appName:"+appName+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			} else if (Utils.isEmpty(phone)) {
				returnJson.put("code", "1004");
				returnJson.put("msg", "phone参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(sign)) {
				returnJson.put("code", "1005");
				returnJson.put("msg", "sign参数不能为空");
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
			queryParamList.add(new TokenParam("phone",phone));
			queryParamList.add(new TokenParam("app_name",appName));
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
			boolean b = false;//检测应用是否全部达到限量值
			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
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
				returnJson.put("code", "1010");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!b) {
				returnJson.put("code", "1011");
				returnJson.put("msg", "已达到今日总限量值");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			Integer appId = tOpenApp.getId();
			String province = null;
			String provinceEncoder = null;
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province, "UTF-8");
			}
//			boolean flag = false;//false-屏蔽 true-不屏蔽
//			if (province != null) {
//				//判断省是否到量
//				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
//				Integer packagedaylimit_conf = tOpenAppLimit.getDayLimit();
//				if (packagedaylimit_conf == -1) {//不屏蔽
//					flag = true;
//				}
//			} else {//未取到号码所在地
//				flag = true;
//			}
//			
//			if (!flag) {
//				returnJson.put("code", "1012");
//				returnJson.put("msg", "号码在屏蔽地市内");
//				StringUtil.printJson(response, returnJson.toString());
//				return;
//			}
			
			////////检测省份是否到量
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			String day = sdfDay.format(calendar.getTime());
			Integer appdaylimit_conf = null;
			if (!Utils.isEmpty(appId)) {
				TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
				appdaylimit_conf = tOpenAppLimit.getDayLimit();
			}
			String WOREADDY_APP_PROVINCE_DAY_LIMIT = "woreaddy_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			//判断app日限是否到达
			Integer appdaylimit = (Integer)mc.getCache(WOREADDY_APP_PROVINCE_DAY_LIMIT);
			appdaylimit = appdaylimit == null ? 0 : appdaylimit;
			if (appdaylimit_conf == -1) {//无限制
				//不处理
			} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
				//不处理
			} else {
				returnJson.put("code", "1012");
				returnJson.put("msg", "该省份已达到当日推送量");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//查询app对应的channelId和key,及创建订单所需参数
			String key = tOpenApp.getAppKey();
			String clientId = tOpenApp.getClientId();
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
			
			JSONArray returnJsonArray = new JSONArray();
			//通过app查询关联的计费信息
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			Collections.sort(proList);
			while (fee >= 100) {
				for (int i = 0; i < proList.size(); i++) {
					chargeFee = proList.get(i).getPrice();
					if (fee - chargeFee >= 0) {
						fee = fee - chargeFee;
						
						JSONObject json = generateWoReadDyOrder(outTradeNo, appNameDecode, appId, merchantId, sellerId, key, clientId, proList.get(i).getProductId(), chargeFee, proList.get(i).getName(), phone);
						returnJsonArray.add(json);
                        break;
					}
				}
			}
			
			returnJson.put("code", "1");
			returnJson.put("msg", returnJsonArray);
			StringUtil.printJson(response, returnJson.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
	}
	
	/**
	 * 短验提交验证并支付
	 */
	public void dyPay() {
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		String code = ServletRequestUtils.getStringParameter(request, "code", null);
		
		JSONObject returnJson = new JSONObject();
		try {
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				String clientId = tOpenOrder.getSenderNumber();
				String paycode = tOpenOrder.getMsgContent();
				String productdesc = tOpenOrder.getSubject();
				String outTradeNo = tOpenOrder.getOutTradeNo();
				Integer appId = tOpenOrder.getAppId();
				String phone = tOpenOrder.getImsi();
				Integer fee = tOpenOrder.getFee();
				TOpenApp tOpenApp = openAppManager.get(appId);
				String key = tOpenApp.getAppKey();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String timestamp = sdf.format(new Date());
				String keyversion = "1";
				String passcode = MD5.getMD5(timestamp+clientId+keyversion+key).toLowerCase();
				String optype = "order";
				String orderType = "2";
				String payType = "1";
				String producttype = "2";
				String username = phone;
				String phonenum = phone;
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("timestamp",timestamp);
				map.put("clientid",clientId);
				map.put("keyversion",keyversion);
				map.put("passcode",passcode);
				map.put("optype", optype);
				map.put("orderid",orderId);
				map.put("ordertype",orderType);
				map.put("paytype",payType);
				map.put("paycode",paycode);
				map.put("producttype",producttype);
				map.put("username",username);
				map.put("productdesc",productdesc);
				map.put("phonenum",phonenum);
				map.put("smscode", code);
				String res = HttpClientUtils.simplePostInvoke("http://42.48.28.10:9080/smscharge/orderBySMScode.action", map);
				LogUtil.log("dyPayRes:"+res);
				Document doc = Jsoup.parse(res);
				Elements elements = doc.getElementsByClass("book_list");
				String ret = elements.get(0).text();
				LogUtil.log("dyPayRet:"+ret);
				if (!Utils.isEmpty(ret) && ret.indexOf("成功购买") != -1) {
					returnJson.put("order_no", orderId);
					returnJson.put("out_trade_no", outTradeNo);
					returnJson.put("fee", fee);
					returnJson.put("code", "1");
					returnJson.put("msg", "提交成功");
					StringUtil.printJson(response, returnJson.toString());
				} else {
					returnJson.put("order_no", orderId);
					returnJson.put("out_trade_no", outTradeNo);
					returnJson.put("fee", fee);
					returnJson.put("code", "1004");
					returnJson.put("msg", ret);
					StringUtil.printJson(response, returnJson.toString());
				}
			} else {
				returnJson.put("code", "1001");
				returnJson.put("msg", "订单号不存在");
				StringUtil.printJson(response, returnJson.toString());
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", e.getMessage());
			StringUtil.printJson(response, returnJson.toString());
		}
	}
	
	public static String appendZero(String str, int length) {
		// String.valueOf()是用来将其他类型的数据转换为string型数据的
		String tmpString = str;
		for (int i = tmpString.length(); i < length; i++) {
			tmpString = "0" + tmpString;
		}
		return tmpString;
	}
	
	public JSONObject generateWoReadOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, String senderNumber, String signId, String key, Integer price, String imsi) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			orderNo = appendZero(orderNo, 32);
			
			String[] signIdArr = signId.split(",");
			String CPID = signIdArr[0];
			String APPID = signIdArr[1];
			String CHANNELID = signIdArr[2];
			String MYID = signIdArr[3];
			String serviceid = signIdArr[4];
			String time = String.valueOf(System.currentTimeMillis());
			time = time.substring(0,6);
			StringBuilder upMsg = new StringBuilder();
			upMsg.append("2");//itfType
	        upMsg.append("1");//command
	        upMsg.append("1");//feetype
	        upMsg.append(CPID);
	        upMsg.append(serviceid);
	        upMsg.append(CHANNELID);
	        upMsg.append(APPID);
	        upMsg.append(MYID);
	        upMsg.append(time);
	        upMsg.append(orderNo);
	        upMsg.append(geneRan());
	        String ystr = MD5.getMD5((new StringBuilder(String.valueOf(CPID))).append(serviceid).append(APPID).append(time).append(orderNo).append(key).toString());
	        if(ystr.length() == 32)
	            ystr = ystr.substring(8, 24);
	        upMsg.append(ystr);
	        upMsg.append("1");
			
			//创建订单
			TOpenOrder tOpenOrder = new TOpenOrder();
			tOpenOrder.setImsi(imsi);
			tOpenOrder.setOrderId(orderNo);
			tOpenOrder.setOutTradeNo(outTradeNo);
			tOpenOrder.setAppId(appId);
			tOpenOrder.setMerchantId(merchantId);
			tOpenOrder.setSellerId(sellerId);
			tOpenOrder.setSubject(appName);
			tOpenOrder.setSenderNumber(senderNumber);
			tOpenOrder.setMsgContent(upMsg.toString());
			tOpenOrder.setFee(price);
			openOrderManager.save(tOpenOrder);
			
			returnJsonMsgObj = new JSONObject();
			returnJsonMsgObj.put("order_id", orderNo);
			returnJsonMsgObj.put("out_trade_no", outTradeNo);
			returnJsonMsgObj.put("fee", price);
			returnJsonMsgObj.put("sender_number", senderNumber);
			returnJsonMsgObj.put("message_content", upMsg.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	public JSONObject generateWoReadDyOrder(String outTradeNo, String appName, Integer appId, Integer merchantId, Integer sellerId, String key, String clientId, String payCode, Integer price, String productName, String phone) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderId = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String timestamp = sdf.format(new Date());
			String clientid = clientId;
			String keyversion = "1";
			String passcode = MD5.getMD5(timestamp+clientid+keyversion+key).toLowerCase();
			String optype = "order";
			String orderType = "2";
			String payType = "1";
			String paycode = payCode;
			String producttype = "2";
			String username = phone;
			String productdesc = productName;
			String phonenum = phone;
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("timestamp",timestamp);
			map.put("clientid",clientid);
			map.put("keyversion",keyversion);
			map.put("passcode",passcode);
			map.put("optype", optype);
			map.put("orderid",orderId);
			map.put("ordertype",orderType);
			map.put("paytype",payType);
			map.put("paycode",paycode);
			map.put("producttype",producttype);
			map.put("username",username);
			map.put("productdesc",appName+productdesc);
			map.put("phonenum",phonenum);
			String res = HttpClientUtils.simplePostInvoke("http://open.iread.wo.com.cn/smscharge/toSMSPage.action", map);
			Document doc = Jsoup.parse(res);
			Elements elements = doc.getElementsByClass("error");
			String ret = elements.get(0).text();
			
			if ("短信发送成功".equals(ret)) {
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(phone);
				tOpenOrder.setOrderId(orderId);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setSubject(appName+productName);
				tOpenOrder.setSenderNumber(clientId);
				tOpenOrder.setMsgContent(payCode);
				tOpenOrder.setFee(price);
				openOrderManager.save(tOpenOrder);
			}
			
			returnJsonMsgObj = new JSONObject();
			returnJsonMsgObj.put("order_id", orderId);
			returnJsonMsgObj.put("out_trade_no", outTradeNo);
			returnJsonMsgObj.put("fee", price);
			returnJsonMsgObj.put("msg", ret);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	/**
	 * 沃阅读- 短代回调地址
	 */
	public void callBack() {
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			LogUtil.log("woread 接收到的xml："+sb.toString());
			int reduce = 0;
			
			// 将资料解码
			String reqBody = sb.toString();
			String orderId = StringUtils.substringBetween(reqBody, "<orderid>", "</orderid>");
			String phoneNum = StringUtils.substringBetween(reqBody, "<phonenum>", "</phonenum>");
			String hRet = StringUtils.substringBetween(reqBody, "<hRet>", "</hRet>");//0-成功 1-失败
//			String status = StringUtils.substringBetween(reqBody, "<status>", "</status>");
			
			if (!Utils.isEmpty(orderId)) {
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
				if (!Utils.isEmpty(tOpenOrder)) {
					String orderStatus = "4";
					if ("0".equals(hRet)) {//成功
						orderStatus = "3";
					} else {
						orderStatus = "4";
					}
					tOpenOrder.setStatus(orderStatus);
					tOpenOrder.setPayTime(new Date());
					tOpenOrder.setPayPhone(phoneNum);
					TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNum);
					String province = null;
					if (!Utils.isEmpty(mobileArea)) {
						province = mobileArea.getProvince();
						tOpenOrder.setProvince(province);
					}
					if ("0".equals(hRet)) {
						//是否扣量
						Integer appId = tOpenOrder.getAppId();
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
					
					//回调渠道
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					String callbackUrl = tOpenSeller.getCallbackUrl();
					if (!Utils.isEmpty(callbackUrl)) {						
						String outTradeNo = tOpenOrder.getOutTradeNo();
//						if ("0".equals(hRet)) {//成功
//							orderStatus = "3";
//						} else {
//							orderStatus = status;
//						}
						if (reduce != 1) {//不扣量
							new Thread(new SendPartner(orderStatus,orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						} else {
							new Thread(new SendPartner("4",orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
						}
					}
				}
			}
			String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><callbackAckRsp>0</callbackAckRsp>";
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/xml; charset=utf-8");
			
			PrintWriter out = response.getWriter();
			out.print(xmlString);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 沃阅读- 短验回调地址
	 */
	public void dyRet() {
		JSONObject returnJson = new JSONObject();
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			LogUtil.log("woread 接收到的短验json："+sb.toString());
			int reduce = 0;
			
			JSONObject json = JSONObject.parseObject(sb.toString());
			String orderId = json.getString("orderid");
			String passcode = json.getString("passcode");
			String timestamp = json.getString("timestamp");
			String clientid = json.getString("clientid");
			String keyversion = json.getString("keyversion");
			String resultcode = json.getString("resultcode");
			String phone = json.getString("username");
			
			if (!Utils.isEmpty(orderId)) {
				TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
				Integer appId = tOpenOrder.getAppId();
				Integer sellerId = tOpenOrder.getSellerId();
				Integer fee = tOpenOrder.getFee();
				TOpenApp tOpenApp = openAppManager.get(appId);
				String key = tOpenApp.getAppKey();
				String genePassCode = MD5.getMD5(timestamp+clientid+keyversion+key).toLowerCase();
				if (genePassCode.equals(passcode)) {//签名通过
					if (!Utils.isEmpty(tOpenOrder)) {
						String orderStatus = "4";
						if ("0000".equals(resultcode)) {//成功
							orderStatus = "3";
						} else {
							orderStatus = "4";
						}
						tOpenOrder.setStatus(orderStatus);
						tOpenOrder.setPayTime(new Date());
						tOpenOrder.setPayPhone(phone);
						TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
						String province = null;
						if (!Utils.isEmpty(mobileArea)) {
							province = mobileArea.getProvince();
							tOpenOrder.setProvince(province);
						}
						if ("0000".equals(resultcode)) {
							//是否扣量
							TOpenAppLimit tOpenAppLimit = openAppManager.findAppLimitByProperty(appId, province);
							double reduce_conf = tOpenAppLimit.getReduce()/(double)100;
							double rate = new Random().nextDouble();
							if (rate < reduce_conf) {
								reduce = 1;
								tOpenOrder.setReduce(reduce);
							}
							//增加缓存记录值
							setLimitCache(phone, sellerId, appId, fee);
						}
						openOrderManager.save(tOpenOrder);
						
						//增加今日量
						if ("3".equals(orderStatus)) {
							openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
						}
						
						//回调渠道
						TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
						String callbackUrl = tOpenSeller.getCallbackUrl();
						if (!Utils.isEmpty(callbackUrl)) {						
							String outTradeNo = tOpenOrder.getOutTradeNo();
							if (reduce != 1) {//不扣量
								new Thread(new SendPartner(orderStatus,orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
							} else {
								new Thread(new SendPartner("4",orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
							}
						}
						
						returnJson.put("code", "0000");
						returnJson.put("innercode", "0000");
						returnJson.put("message", "ok");
					}
				}
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("innercode", "9999");
			returnJson.put("message", "exception");
		}
		StringUtil.printJson(response, returnJson.toString());
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
			        LogUtil.log("sendChannelWoReadMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
	
	private void setLimitCache(String phone, Integer sellerId, Integer appId, Integer fee) {
		try {
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
			String day = sdfDay.format(calendar.getTime());
			String month = sdfMonth.format(calendar.getTime());
			
			SimpleDateFormat sdfsqlday = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//获取当日开始结束时间
			String startdayString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startdayString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			String enddayString = sdfsqlday.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(enddayString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			//获取当月开始结束时间
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String mstartString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date mstartDate = sdfSql.parse(mstartString);
			java.sql.Date mstart = new java.sql.Date(mstartDate.getTime());
			calendar.add(Calendar.MONTH, 1);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String mendString = sdfsqlday.format(calendar.getTime()) + " 00:00:00";
			Date mendDate = sdfSql.parse(mendString);
			java.sql.Date mend = new java.sql.Date(mendDate.getTime());
			
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
			String provinceEncoder = null;
			String province = null;
			if (!Utils.isEmpty(mobileArea)) {
				province = mobileArea.getProvince();
				provinceEncoder = URLEncoder.encode(province,"UTF-8");
			}

			String WOREADDY_USER_DAY_LIMIT = "woreaddy_user_daylimit_"+phone+"_"+day;
			String WOREADDY_USER_MONTH_LIMIT = "woreaddy_user_monthlimit_"+phone+"_"+month;
			String WOREADDY_SELLER_PROVINCE_DAY_LIMIT = "woreaddy_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String WOREADDY_APP_PROVINCE_DAY_LIMIT = "woreaddy_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String WOREADDY_APP_PROVINCE_MONTH_LIMIT = "woreaddy_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			//用户日限
			Integer userDaylimit = (Integer)iCacheClient.getCache(WOREADDY_USER_DAY_LIMIT);
			if (Utils.isEmpty(userDaylimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, start, end);
				iCacheClient.setCache(WOREADDY_USER_DAY_LIMIT, sumfeeByPhone+fee, CacheFactory.DAY);
			} else {
				iCacheClient.setCache(WOREADDY_USER_DAY_LIMIT, userDaylimit+fee, CacheFactory.DAY);
			}
			//用户月限
			Integer userMonthlimit = (Integer)iCacheClient.getCache(WOREADDY_USER_MONTH_LIMIT);
			if (Utils.isEmpty(userMonthlimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, mstart, mend);
				iCacheClient.setCache(WOREADDY_USER_MONTH_LIMIT, sumfeeByPhone+fee, CacheFactory.UNEXPIRY);
			} else {
				iCacheClient.setCache(WOREADDY_USER_MONTH_LIMIT, userMonthlimit+fee, CacheFactory.UNEXPIRY);
			}
			//渠道省份日限
			Integer sellerdaylimit = (Integer)iCacheClient.getCache(WOREADDY_SELLER_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(sellerdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String sellerProvLimitKey = "woreaddy_seller_prov_daylimit_"+sellerId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(sellerProvLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOREADDY_SELLER_PROVINCE_DAY_LIMIT, sellerdaylimit+fee, CacheFactory.DAY);
			}
			//app省份日限
			Integer appdaylimit = (Integer)iCacheClient.getCache(WOREADDY_APP_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(appdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvDayLimitKey = "woreaddy_app_prov_daylimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(appProvDayLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOREADDY_APP_PROVINCE_DAY_LIMIT, appdaylimit+fee, CacheFactory.DAY);
			}
			//app省份月限
			Integer appmonthlimit = (Integer)iCacheClient.getCache(WOREADDY_APP_PROVINCE_MONTH_LIMIT);
			if (Utils.isEmpty(appmonthlimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, mstart, mend, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvMonthLimitKey = "woreaddy_app_prov_monthlimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+month;
						iCacheClient.setCache(appProvMonthLimitKey, provFee+fee, CacheFactory.UNEXPIRY);
					}
				}
			} else {
				iCacheClient.setCache(WOREADDY_APP_PROVINCE_MONTH_LIMIT, appmonthlimit+fee, CacheFactory.UNEXPIRY);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public static String geneRan() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("AA","794693");
		map.put("AB","904602");
		map.put("AC","922905");
		map.put("AD","594672");
		map.put("AE","181254");
		map.put("AF","030039");
		map.put("AG","816239");
		map.put("AH","810210");
		map.put("AI","295015");
		map.put("AJ","720321");
		map.put("AK","936018");
		map.put("AL","695292");
		map.put("AM","073135");
		map.put("AN","620177");
		map.put("AO","462839");
		map.put("AP","296334");
		map.put("AQ","163545");
		map.put("AR","680021");
		map.put("AS","956869");
		map.put("AT","906821");
		map.put("AU","304431");
		map.put("AV","049841");
		map.put("AW","611743");
		map.put("AX","503152");
		map.put("AY","071223");
		map.put("AZ","795498");
		map.put("A0","420807");
		map.put("A1","335776");
		map.put("A2","619725");
		map.put("A3","505779");
		map.put("A4","324978");
		map.put("A5","080777");
		map.put("A6","360579");
		map.put("A7","574389");
		map.put("A8","519055");
		map.put("A9","661515");
		map.put("BA","315978");
		map.put("BB","095862");
		map.put("BC","801085");
		map.put("BD","845448");
		map.put("BE","213538");
		map.put("BF","681055");
		map.put("BG","370518");
		map.put("BH","011956");
		map.put("BI","071982");
		map.put("BJ","268330");
		map.put("BK","253996");
		map.put("BL","170223");
		map.put("BM","148953");
		map.put("BN","608081");
		map.put("BO","560689");
		map.put("BP","504301");
		map.put("BQ","823433");
		map.put("BR","962832");
		map.put("BS","118666");
		map.put("BT","519734");
		map.put("BU","681852");
		map.put("BV","668944");
		map.put("BW","495320");
		map.put("BX","196102");
		map.put("BY","805619");
		map.put("BZ","172254");
		map.put("B0","815966");
		map.put("B1","133459");
		map.put("B2","258661");
		map.put("B3","922438");
		map.put("B4","200778");
		map.put("B5","814506");
		map.put("B6","193796");
		map.put("B7","994780");
		map.put("B8","121233");
		map.put("B9","259522");
		map.put("CA","679155");
		map.put("CB","121618");
		map.put("CC","114991");
		map.put("CD","925749");
		map.put("CE","741124");
		map.put("CF","961872");
		map.put("CG","553129");
		map.put("CH","571824");
		map.put("CI","393722");
		map.put("CJ","471177");
		map.put("CK","331274");
		map.put("CL","434155");
		map.put("CM","921638");
		map.put("CN","227567");
		map.put("CO","558145");
		map.put("CP","855532");
		map.put("CQ","287721");
		map.put("CR","694055");
		map.put("CS","959700");
		map.put("CT","611249");
		map.put("CU","514986");
		map.put("CV","678140");
		map.put("CW","215393");
		map.put("CX","454845");
		map.put("CY","799017");
		map.put("CZ","261952");
		map.put("C0","125185");
		map.put("C1","591353");
		map.put("C2","649098");
		map.put("C3","635251");
		map.put("C4","683790");
		map.put("C5","212298");
		map.put("C6","601710");
		map.put("C7","352754");
		map.put("C8","185851");
		map.put("C9","383686");
		map.put("DA","554357");
		map.put("DB","287512");
		map.put("DC","298678");
		map.put("DD","980904");
		map.put("DE","317008");
		map.put("DF","725978");
		map.put("DG","462424");
		map.put("DH","243264");
		map.put("DI","381081");
		map.put("DJ","859116");
		map.put("DK","023634");
		map.put("DL","076080");
		map.put("DM","290359");
		map.put("DN","578793");
		map.put("DO","620796");
		map.put("DP","978060");
		map.put("DQ","222925");
		map.put("DR","247580");
		map.put("DS","573605");
		map.put("DT","758546");
		map.put("DU","738081");
		map.put("DV","963350");
		map.put("DW","107084");
		map.put("DX","316165");
		map.put("DY","483457");
		map.put("DZ","946858");
		map.put("D0","494427");
		map.put("D1","160980");
		map.put("D2","693839");
		map.put("D3","128423");
		map.put("D4","993651");
		map.put("D5","933621");
		map.put("D6","759627");
		map.put("D7","056943");
		map.put("D8","757741");
		map.put("D9","746499");
		map.put("EA","597883");
		map.put("EB","851883");
		map.put("EC","549759");
		map.put("ED","414218");
		map.put("EE","955693");
		map.put("EF","301456");
		map.put("EG","717937");
		map.put("EH","142201");
		map.put("EI","371159");
		map.put("EJ","509153");
		map.put("EK","212355");
		map.put("EL","736825");
		map.put("EM","753776");
		map.put("EN","223925");
		map.put("EO","673242");
		map.put("EP","269120");
		map.put("EQ","105235");
		map.put("ER","621350");
		map.put("ES","869014");
		map.put("ET","090441");
		map.put("EU","268581");
		map.put("EV","029500");
		map.put("EW","768868");
		map.put("EX","336154");
		map.put("EY","840539");
		map.put("EZ","245357");
		map.put("E0","259372");
		map.put("E1","997393");
		map.put("E2","464482");
		map.put("E3","544147");
		map.put("E4","708752");
		map.put("E5","995650");
		map.put("E6","430089");
		map.put("E7","591707");
		map.put("E8","705956");
		map.put("E9","788676");
		map.put("FA","958603");
		map.put("FB","027393");
		map.put("FC","537076");
		map.put("FD","232843");
		map.put("FE","706107");
		map.put("FF","792666");
		map.put("FG","499938");
		map.put("FH","030479");
		map.put("FI","537307");
		map.put("FJ","290589");
		map.put("FK","895185");
		map.put("FL","417408");
		map.put("FM","209034");
		map.put("FN","525797");
		map.put("FO","146926");
		map.put("FP","350435");
		map.put("FQ","930725");
		map.put("FR","488120");
		map.put("FS","363903");
		map.put("FT","100522");
		map.put("FU","687550");
		map.put("FV","540568");
		map.put("FW","446435");
		map.put("FX","026389");
		map.put("FY","257773");
		map.put("FZ","674856");
		map.put("F0","797675");
		map.put("F1","735857");
		map.put("F2","882491");
		map.put("F3","171549");
		map.put("F4","976559");
		map.put("F5","748505");
		map.put("F6","133771");
		map.put("F7","548668");
		map.put("F8","570735");
		map.put("F9","636478");
		map.put("GA","828599");
		map.put("GB","546742");
		map.put("GC","187062");
		map.put("GD","836262");
		map.put("GE","931251");
		map.put("GF","767508");
		map.put("GG","252712");
		map.put("GH","755462");
		map.put("GI","197443");
		map.put("GJ","377546");
		map.put("GK","936419");
		map.put("GL","303936");
		map.put("GM","187798");
		map.put("GN","820779");
		map.put("GO","943819");
		map.put("GP","629266");
		map.put("GQ","937438");
		map.put("GR","661730");
		map.put("GS","844900");
		map.put("GT","524596");
		map.put("GU","682019");
		map.put("GV","954713");
		map.put("GW","093875");
		map.put("GX","151893");
		map.put("GY","071022");
		map.put("GZ","991047");
		map.put("G0","954319");
		map.put("G1","898796");
		map.put("G2","625335");
		map.put("G3","287149");
		map.put("G4","352712");
		map.put("G5","441595");
		map.put("G6","449795");
		map.put("G7","665292");
		map.put("G8","670804");
		map.put("G9","470669");
		map.put("HA","577068");
		map.put("HB","841166");
		map.put("HC","193350");
		map.put("HD","507375");
		map.put("HE","608394");
		map.put("HF","483383");
		map.put("HG","556452");
		map.put("HH","864833");
		map.put("HI","672088");
		map.put("HJ","658449");
		map.put("HK","395201");
		map.put("HL","555346");
		map.put("HM","801575");
		map.put("HN","878341");
		map.put("HO","147338");
		map.put("HP","636484");
		map.put("HQ","889662");
		map.put("HR","301212");
		map.put("HS","068376");
		map.put("HT","934792");
		map.put("HU","131127");
		map.put("HV","180177");
		map.put("HW","124339");
		map.put("HX","422861");
		map.put("HY","617913");
		map.put("HZ","937865");
		map.put("H0","495833");
		map.put("H1","890146");
		map.put("H2","754969");
		map.put("H3","572088");
		map.put("H4","485742");
		map.put("H5","437923");
		map.put("H6","518660");
		map.put("H7","779087");
		map.put("H8","546112");
		map.put("H9","394172");
		map.put("IA","790586");
		map.put("IB","994250");
		map.put("IC","833474");
		map.put("ID","783164");
		map.put("IE","927280");
		map.put("IF","700391");
		map.put("IG","523543");
		map.put("IH","286291");
		map.put("II","958564");
		map.put("IJ","605319");
		map.put("IK","518412");
		map.put("IL","898667");
		map.put("IM","297379");
		map.put("IN","710947");
		map.put("IO","673100");
		map.put("IP","944339");
		map.put("IQ","192604");
		map.put("IR","732188");
		map.put("IS","723819");
		map.put("IT","763320");
		map.put("IU","965251");
		map.put("IV","303560");
		map.put("IW","542971");
		map.put("IX","901693");
		map.put("IY","570207");
		map.put("IZ","490972");
		map.put("I0","581248");
		map.put("I1","407052");
		map.put("I2","124601");
		map.put("I3","497049");
		map.put("I4","252764");
		map.put("I5","267763");
		map.put("I6","455005");
		map.put("I7","298770");
		map.put("I8","477185");
		map.put("I9","761419");
		map.put("JA","117579");
		map.put("JB","885344");
		map.put("JC","931667");
		map.put("JD","565611");
		map.put("JE","897552");
		map.put("JF","057806");
		map.put("JG","404941");
		map.put("JH","929166");
		map.put("JI","702481");
		map.put("JJ","752985");
		map.put("JK","668915");
		map.put("JL","460345");
		map.put("JM","331312");
		map.put("JN","572251");
		map.put("JO","948541");
		map.put("JP","389778");
		map.put("JQ","286475");
		map.put("JR","318444");
		map.put("JS","131137");
		map.put("JT","937508");
		map.put("JU","986925");
		map.put("JV","745395");
		map.put("JW","329663");
		map.put("JX","815760");
		map.put("JY","851584");
		map.put("JZ","253556");
		map.put("J0","797328");
		map.put("J1","928059");
		map.put("J2","453668");
		map.put("J3","261318");
		map.put("J4","417282");
		map.put("J5","300496");
		map.put("J6","118295");
		map.put("J7","928616");
		map.put("J8","151886");
		map.put("J9","679105");
		map.put("KA","053526");
		map.put("KB","498652");
		map.put("KC","082602");
		map.put("KD","105047");
		map.put("KE","802871");
		map.put("KF","464551");
		map.put("KG","520798");
		map.put("KH","985687");
		map.put("KI","088074");
		map.put("KJ","044160");
		map.put("KK","454164");
		map.put("KL","626588");
		map.put("KM","762588");
		map.put("KN","480118");
		map.put("KO","378321");
		map.put("KP","442284");
		map.put("KQ","365702");
		map.put("KR","273124");
		map.put("KS","152692");
		map.put("KT","047463");
		map.put("KU","459281");
		map.put("KV","138863");
		map.put("KW","695580");
		map.put("KX","673537");
		map.put("KY","744538");
		map.put("KZ","407380");
		map.put("K0","752878");
		map.put("K1","885464");
		map.put("K2","003912");
		map.put("K3","537057");
		map.put("K4","686749");
		map.put("K5","273368");
		map.put("K6","118268");
		map.put("K7","509783");
		map.put("K8","918189");
		map.put("K9","609440");
		map.put("LA","356683");
		map.put("LB","988201");
		map.put("LC","856967");
		map.put("LD","399508");
		map.put("LE","660050");
		map.put("LF","925409");
		map.put("LG","990765");
		map.put("LH","073083");
		map.put("LI","665306");
		map.put("LJ","902506");
		map.put("LK","684345");
		map.put("LL","560079");
		map.put("LM","351005");
		map.put("LN","089276");
		map.put("LO","701596");
		map.put("LP","308020");
		map.put("LQ","929528");
		map.put("LR","944647");
		map.put("LS","896362");
		map.put("LT","178548");
		map.put("LU","378913");
		map.put("LV","307756");
		map.put("LW","408347");
		map.put("LX","384547");
		map.put("LY","420018");
		map.put("LZ","878189");
		map.put("L0","851432");
		map.put("L1","402018");
		map.put("L2","668974");
		map.put("L3","225652");
		map.put("L4","616048");
		map.put("L5","381012");
		map.put("L6","264227");
		map.put("L7","107467");
		map.put("L8","733092");
		map.put("L9","851721");
		map.put("MA","084932");
		map.put("MB","995171");
		map.put("MC","407883");
		map.put("MD","422674");
		map.put("ME","645340");
		map.put("MF","960165");
		map.put("MG","868904");
		map.put("MH","422265");
		map.put("MI","783547");
		map.put("MJ","899096");
		map.put("MK","544027");
		map.put("ML","021058");
		map.put("MM","252377");
		map.put("MN","489394");
		map.put("MO","904206");
		map.put("MP","159878");
		map.put("MQ","255794");
		map.put("MR","990986");
		map.put("MS","475327");
		map.put("MT","131229");
		map.put("MU","884437");
		map.put("MV","951334");
		map.put("MW","370309");
		map.put("MX","823593");
		map.put("MY","979083");
		map.put("MZ","113351");
		map.put("M0","755077");
		map.put("M1","538140");
		map.put("M2","474566");
		map.put("M3","528124");
		map.put("M4","194335");
		map.put("M5","863032");
		map.put("M6","928979");
		map.put("M7","215793");
		map.put("M8","948871");
		map.put("M9","056082");
		map.put("NA","308806");
		map.put("NB","536143");
		map.put("NC","912066");
		map.put("ND","529594");
		map.put("NE","780394");
		map.put("NF","694354");
		map.put("NG","795435");
		map.put("NH","828516");
		map.put("NI","813080");
		map.put("NJ","750481");
		map.put("NK","311690");
		map.put("NL","400602");
		map.put("NM","798885");
		map.put("NN","482687");
		map.put("NO","409545");
		map.put("NP","186897");
		map.put("NQ","900663");
		map.put("NR","480269");
		map.put("NS","766526");
		map.put("NT","938177");
		map.put("NU","849334");
		map.put("NV","527888");
		map.put("NW","529880");
		map.put("NX","346177");
		map.put("NY","330075");
		map.put("NZ","346199");
		map.put("N0","612741");
		map.put("N1","963099");
		map.put("N2","872401");
		map.put("N3","085437");
		map.put("N4","580777");
		map.put("N5","106763");
		map.put("N6","586329");
		map.put("N7","665607");
		map.put("N8","351411");
		map.put("N9","543050");
		map.put("OA","998191");
		map.put("OB","724225");
		map.put("OC","977529");
		map.put("OD","848724");
		map.put("OE","005110");
		map.put("OF","748711");
		map.put("OG","789848");
		map.put("OH","850469");
		map.put("OI","064046");
		map.put("OJ","901689");
		map.put("OK","458169");
		map.put("OL","343529");
		map.put("OM","209388");
		map.put("ON","225273");
		map.put("OO","524020");
		map.put("OP","356209");
		map.put("OQ","896027");
		map.put("OR","802714");
		map.put("OS","368120");
		map.put("OT","981916");
		map.put("OU","765739");
		map.put("OV","178833");
		map.put("OW","584038");
		map.put("OX","930760");
		map.put("OY","963917");
		map.put("OZ","378352");
		map.put("O0","457967");
		map.put("O1","289576");
		map.put("O2","418520");
		map.put("O3","255886");
		map.put("O4","734299");
		map.put("O5","613423");
		map.put("O6","199482");
		map.put("O7","572404");
		map.put("O8","165062");
		map.put("O9","180931");
		map.put("PA","298356");
		map.put("PB","533162");
		map.put("PC","990759");
		map.put("PD","658964");
		map.put("PE","023421");
		map.put("PF","335432");
		map.put("PG","341373");
		map.put("PH","111071");
		map.put("PI","599821");
		map.put("PJ","545288");
		map.put("PK","319679");
		map.put("PL","454518");
		map.put("PM","299674");
		map.put("PN","867382");
		map.put("PO","357974");
		map.put("PP","161553");
		map.put("PQ","852647");
		map.put("PR","333927");
		map.put("PS","829585");
		map.put("PT","641170");
		map.put("PU","013819");
		map.put("PV","502300");
		map.put("PW","876204");
		map.put("PX","765625");
		map.put("PY","208049");
		map.put("PZ","588244");
		map.put("P0","903696");
		map.put("P1","389998");
		map.put("P2","243118");
		map.put("P3","319529");
		map.put("P4","080092");
		map.put("P5","113369");
		map.put("P6","329282");
		map.put("P7","058163");
		map.put("P8","754952");
		map.put("P9","420872");
		map.put("QA","650306");
		map.put("QB","271887");
		map.put("QC","069791");
		map.put("QD","327753");
		map.put("QE","183309");
		map.put("QF","625194");
		map.put("QG","414893");
		map.put("QH","179634");
		map.put("QI","955467");
		map.put("QJ","541151");
		map.put("QK","212755");
		map.put("QL","043808");
		map.put("QM","341767");
		map.put("QN","780386");
		map.put("QO","743439");
		map.put("QP","552818");
		map.put("QQ","564565");
		map.put("QR","483147");
		map.put("QS","075019");
		map.put("QT","802175");
		map.put("QU","765024");
		map.put("QV","033116");
		map.put("QW","126022");
		map.put("QX","367105");
		map.put("QY","633585");
		map.put("QZ","314527");
		map.put("Q0","712628");
		map.put("Q1","410580");
		map.put("Q2","566512");
		map.put("Q3","952331");
		map.put("Q4","579906");
		map.put("Q5","580913");
		map.put("Q6","934052");
		map.put("Q7","263566");
		map.put("Q8","490229");
		map.put("Q9","045944");
		map.put("RA","696584");
		map.put("RB","482668");
		map.put("RC","259182");
		map.put("RD","347090");
		map.put("RE","462901");
		map.put("RF","654987");
		map.put("RG","919438");
		map.put("RH","799081");
		map.put("RI","547067");
		map.put("RJ","134953");
		map.put("RK","113548");
		map.put("RL","486558");
		map.put("RM","515569");
		map.put("RN","599595");
		map.put("RO","156543");
		map.put("RP","947177");
		map.put("RQ","721739");
		map.put("RR","952413");
		map.put("RS","013509");
		map.put("RT","955309");
		map.put("RU","889983");
		map.put("RV","631610");
		map.put("RW","801624");
		map.put("RX","908548");
		map.put("RY","855537");
		map.put("RZ","801710");
		map.put("R0","967027");
		map.put("R1","935734");
		map.put("R2","215503");
		map.put("R3","847603");
		map.put("R4","299839");
		map.put("R5","542298");
		map.put("R6","346883");
		map.put("R7","745672");
		map.put("R8","099232");
		map.put("R9","182337");
		map.put("SA","068764");
		map.put("SB","194821");
		map.put("SC","726899");
		map.put("SD","149395");
		map.put("SE","293566");
		map.put("SF","756412");
		map.put("SG","063117");
		map.put("SH","234330");
		map.put("SI","957989");
		map.put("SJ","950145");
		map.put("SK","711589");
		map.put("SL","227617");
		map.put("SM","681832");
		map.put("SN","813359");
		map.put("SO","208116");
		map.put("SP","044409");
		map.put("SQ","511223");
		map.put("SR","485228");
		map.put("SS","015753");
		map.put("ST","165843");
		map.put("SU","847056");
		map.put("SV","883685");
		map.put("SW","789575");
		map.put("SX","047773");
		map.put("SY","499209");
		map.put("SZ","918087");
		map.put("S0","679376");
		map.put("S1","656907");
		map.put("S2","783715");
		map.put("S3","406202");
		map.put("S4","963197");
		map.put("S5","989456");
		map.put("S6","496330");
		map.put("S7","181046");
		map.put("S8","334546");
		map.put("S9","059750");
		map.put("TA","722569");
		map.put("TB","381537");
		map.put("TC","379954");
		map.put("TD","901461");
		map.put("TE","075873");
		map.put("TF","228401");
		map.put("TG","615926");
		map.put("TH","127202");
		map.put("TI","679575");
		map.put("TJ","186413");
		map.put("TK","919793");
		map.put("TL","022016");
		map.put("TM","849192");
		map.put("TN","197350");
		map.put("TO","592427");
		map.put("TP","635523");
		map.put("TQ","811590");
		map.put("TR","994797");
		map.put("TS","346910");
		map.put("TT","952099");
		map.put("TU","100462");
		map.put("TV","339781");
		map.put("TW","462784");
		map.put("TX","917838");
		map.put("TY","042222");
		map.put("TZ","034141");
		map.put("T0","974017");
		map.put("T1","473192");
		map.put("T2","420097");
		map.put("T3","168441");
		map.put("T4","722027");
		map.put("T5","169163");
		map.put("T6","350234");
		map.put("T7","584416");
		map.put("T8","025233");
		map.put("T9","937235");
		map.put("UA","358601");
		map.put("UB","078415");
		map.put("UC","583008");
		map.put("UD","939246");
		map.put("UE","163358");
		map.put("UF","363354");
		map.put("UG","466142");
		map.put("UH","079913");
		map.put("UI","638639");
		map.put("UJ","806862");
		map.put("UK","493117");
		map.put("UL","078896");
		map.put("UM","609398");
		map.put("UN","710606");
		map.put("UO","109329");
		map.put("UP","153865");
		map.put("UQ","949361");
		map.put("UR","601117");
		map.put("US","790067");
		map.put("UT","369181");
		map.put("UU","600205");
		map.put("UV","867643");
		map.put("UW","316184");
		map.put("UX","271835");
		map.put("UY","820084");
		map.put("UZ","331770");
		map.put("U0","358639");
		map.put("U1","818278");
		map.put("U2","417225");
		map.put("U3","876034");
		map.put("U4","591993");
		map.put("U5","793073");
		map.put("U6","536982");
		map.put("U7","006542");
		map.put("U8","213687");
		map.put("U9","529273");
		map.put("VA","830205");
		map.put("VB","291820");
		map.put("VC","000499");
		map.put("VD","907203");
		map.put("VE","147242");
		map.put("VF","531451");
		map.put("VG","982817");
		map.put("VH","081374");
		map.put("VI","531539");
		map.put("VJ","895941");
		map.put("VK","306201");
		map.put("VL","997293");
		map.put("VM","283641");
		map.put("VN","874732");
		map.put("VO","217160");
		map.put("VP","642177");
		map.put("VQ","504674");
		map.put("VR","949973");
		map.put("VS","271571");
		map.put("VT","871249");
		map.put("VU","890688");
		map.put("VV","014258");
		map.put("VW","164352");
		map.put("VX","144080");
		map.put("VY","694492");
		map.put("VZ","620375");
		map.put("V0","680526");
		map.put("V1","818203");
		map.put("V2","655583");
		map.put("V3","415259");
		map.put("V4","173289");
		map.put("V5","102002");
		map.put("V6","009233");
		map.put("V7","653046");
		map.put("V8","970670");
		map.put("V9","179462");
		map.put("WA","537846");
		map.put("WB","581820");
		map.put("WC","662937");
		map.put("WD","564895");
		map.put("WE","863214");
		map.put("WF","422452");
		map.put("WG","597936");
		map.put("WH","779101");
		map.put("WI","840289");
		map.put("WJ","998095");
		map.put("WK","996938");
		map.put("WL","437523");
		map.put("WM","553807");
		map.put("WN","173903");
		map.put("WO","521747");
		map.put("WP","995393");
		map.put("WQ","495084");
		map.put("WR","876296");
		map.put("WS","354433");
		map.put("WT","485412");
		map.put("WU","006128");
		map.put("WV","801237");
		map.put("WW","436191");
		map.put("WX","667861");
		map.put("WY","540814");
		map.put("WZ","873636");
		map.put("W0","411192");
		map.put("W1","269446");
		map.put("W2","833067");
		map.put("W3","053863");
		map.put("W4","485837");
		map.put("W5","753479");
		map.put("W6","776982");
		map.put("W7","336905");
		map.put("W8","965067");
		map.put("W9","566698");
		map.put("XA","084244");
		map.put("XB","603974");
		map.put("XC","863436");
		map.put("XD","424815");
		map.put("XE","694251");
		map.put("XF","646661");
		map.put("XG","212590");
		map.put("XH","328465");
		map.put("XI","425001");
		map.put("XJ","400879");
		map.put("XK","143789");
		map.put("XL","042168");
		map.put("XM","943498");
		map.put("XN","137860");
		map.put("XO","689981");
		map.put("XP","807825");
		map.put("XQ","130089");
		map.put("XR","661643");
		map.put("XS","859685");
		map.put("XT","001480");
		map.put("XU","889946");
		map.put("XV","692162");
		map.put("XW","073505");
		map.put("XX","541938");
		map.put("XY","467019");
		map.put("XZ","645927");
		map.put("X0","994881");
		map.put("X1","876618");
		map.put("X2","035156");
		map.put("X3","245343");
		map.put("X4","915898");
		map.put("X5","941506");
		map.put("X6","548593");
		map.put("X7","173712");
		map.put("X8","599731");
		map.put("X9","288811");
		map.put("YA","695220");
		map.put("YB","917052");
		map.put("YC","908683");
		map.put("YD","926316");
		map.put("YE","180431");
		map.put("YF","062226");
		map.put("YG","186689");
		map.put("YH","683061");
		map.put("YI","897517");
		map.put("YJ","898160");
		map.put("YK","019070");
		map.put("YL","560704");
		map.put("YM","674316");
		map.put("YN","520303");
		map.put("YO","142926");
		map.put("YP","003307");
		map.put("YQ","993707");
		map.put("YR","066471");
		map.put("YS","691097");
		map.put("YT","939116");
		map.put("YU","426521");
		map.put("YV","447558");
		map.put("YW","761983");
		map.put("YX","362414");
		map.put("YY","318871");
		map.put("YZ","727255");
		map.put("Y0","648715");
		map.put("Y1","151901");
		map.put("Y2","804013");
		map.put("Y3","439620");
		map.put("Y4","623175");
		map.put("Y5","974629");
		map.put("Y6","381425");
		map.put("Y7","786319");
		map.put("Y8","040619");
		map.put("Y9","820578");
		map.put("ZA","522559");
		map.put("ZB","769713");
		map.put("ZC","626487");
		map.put("ZD","472192");
		map.put("ZE","990407");
		map.put("ZF","494982");
		map.put("ZG","950890");
		map.put("ZH","108006");
		map.put("ZI","231380");
		map.put("ZJ","502684");
		map.put("ZK","863132");
		map.put("ZL","674609");
		map.put("ZM","502865");
		map.put("ZN","909167");
		map.put("ZO","442083");
		map.put("ZP","284206");
		map.put("ZQ","228668");
		map.put("ZR","568175");
		map.put("ZS","821658");
		map.put("ZT","170643");
		map.put("ZU","873957");
		map.put("ZV","206597");
		map.put("ZW","483547");
		map.put("ZX","021382");
		map.put("ZY","128188");
		map.put("ZZ","218078");
		map.put("Z0","494491");
		map.put("Z1","806085");
		map.put("Z2","779464");
		map.put("Z3","052110");
		map.put("Z4","050355");
		map.put("Z5","635153");
		map.put("Z6","077487");
		map.put("Z7","112450");
		map.put("Z8","021379");
		map.put("Z9","328510");
		map.put("0A","298087");
		map.put("0B","877007");
		map.put("0C","722090");
		map.put("0D","286696");
		map.put("0E","857536");
		map.put("0F","094172");
		map.put("0G","501265");
		map.put("0H","598485");
		map.put("0I","476089");
		map.put("0J","090008");
		map.put("0K","069615");
		map.put("0L","766027");
		map.put("0M","802239");
		map.put("0N","385525");
		map.put("0O","833252");
		map.put("0P","904637");
		map.put("0Q","717234");
		map.put("0R","660021");
		map.put("0S","054680");
		map.put("0T","562726");
		map.put("0U","940870");
		map.put("0V","992416");
		map.put("0W","089443");
		map.put("0X","968513");
		map.put("0Y","986801");
		map.put("0Z","935592");
		map.put("00","398849");
		map.put("01","938205");
		map.put("02","540095");
		map.put("03","101807");
		map.put("04","710158");
		map.put("05","451712");
		map.put("06","253855");
		map.put("07","252725");
		map.put("08","962318");
		map.put("09","567742");
		map.put("1A","674561");
		map.put("1B","129165");
		map.put("1C","857340");
		map.put("1D","430911");
		map.put("1E","527087");
		map.put("1F","357434");
		map.put("1G","803806");
		map.put("1H","517290");
		map.put("1I","692261");
		map.put("1J","392200");
		map.put("1K","710618");
		map.put("1L","360315");
		map.put("1M","359409");
		map.put("1N","935003");
		map.put("1O","372961");
		map.put("1P","475275");
		map.put("1Q","590543");
		map.put("1R","516254");
		map.put("1S","574750");
		map.put("1T","409158");
		map.put("1U","858987");
		map.put("1V","249408");
		map.put("1W","512242");
		map.put("1X","573449");
		map.put("1Y","888335");
		map.put("1Z","989235");
		map.put("10","510737");
		map.put("11","410199");
		map.put("12","503412");
		map.put("13","993285");
		map.put("14","945305");
		map.put("15","903617");
		map.put("16","669485");
		map.put("17","243217");
		map.put("18","413282");
		map.put("19","665158");
		map.put("2A","897311");
		map.put("2B","112269");
		map.put("2C","241170");
		map.put("2D","257757");
		map.put("2E","407750");
		map.put("2F","757152");
		map.put("2G","112232");
		map.put("2H","560176");
		map.put("2I","060930");
		map.put("2J","672089");
		map.put("2K","699873");
		map.put("2L","796347");
		map.put("2M","718304");
		map.put("2N","485186");
		map.put("2O","736769");
		map.put("2P","593067");
		map.put("2Q","951809");
		map.put("2R","391975");
		map.put("2S","874128");
		map.put("2T","788500");
		map.put("2U","813084");
		map.put("2V","637169");
		map.put("2W","043395");
		map.put("2X","359104");
		map.put("2Y","851210");
		map.put("2Z","851342");
		map.put("20","514278");
		map.put("21","568798");
		map.put("22","313515");
		map.put("23","154726");
		map.put("24","142255");
		map.put("25","249990");
		map.put("26","119130");
		map.put("27","312440");
		map.put("28","710767");
		map.put("29","649666");
		map.put("3A","907089");
		map.put("3B","945145");
		map.put("3C","706763");
		map.put("3D","882653");
		map.put("3E","206666");
		map.put("3F","940570");
		map.put("3G","746625");
		map.put("3H","220634");
		map.put("3I","157307");
		map.put("3J","403087");
		map.put("3K","081018");
		map.put("3L","191216");
		map.put("3M","529288");
		map.put("3N","173237");
		map.put("3O","769546");
		map.put("3P","164956");
		map.put("3Q","280642");
		map.put("3R","773525");
		map.put("3S","746036");
		map.put("3T","753810");
		map.put("3U","317753");
		map.put("3V","841892");
		map.put("3W","818266");
		map.put("3X","225849");
		map.put("3Y","340292");
		map.put("3Z","112799");
		map.put("30","510822");
		map.put("31","127661");
		map.put("32","255107");
		map.put("33","710414");
		map.put("34","836829");
		map.put("35","938216");
		map.put("36","191590");
		map.put("37","723372");
		map.put("38","774118");
		map.put("39","190693");
		map.put("4A","131638");
		map.put("4B","218014");
		map.put("4C","794764");
		map.put("4D","197741");
		map.put("4E","119374");
		map.put("4F","570173");
		map.put("4G","484727");
		map.put("4H","517033");
		map.put("4I","800465");
		map.put("4J","150914");
		map.put("4K","319168");
		map.put("4L","763984");
		map.put("4M","823135");
		map.put("4N","087212");
		map.put("4O","642979");
		map.put("4P","710588");
		map.put("4Q","524440");
		map.put("4R","331392");
		map.put("4S","140651");
		map.put("4T","906500");
		map.put("4U","484856");
		map.put("4V","358724");
		map.put("4W","059869");
		map.put("4X","378300");
		map.put("4Y","244113");
		map.put("4Z","200752");
		map.put("40","633320");
		map.put("41","026903");
		map.put("42","792511");
		map.put("43","153838");
		map.put("44","683759");
		map.put("45","687579");
		map.put("46","270478");
		map.put("47","042574");
		map.put("48","470327");
		map.put("49","364062");
		map.put("5A","977109");
		map.put("5B","520771");
		map.put("5C","821149");
		map.put("5D","225846");
		map.put("5E","257391");
		map.put("5F","920624");
		map.put("5G","011086");
		map.put("5H","006437");
		map.put("5I","913541");
		map.put("5J","898210");
		map.put("5K","850291");
		map.put("5L","482004");
		map.put("5M","728265");
		map.put("5N","966761");
		map.put("5O","144852");
		map.put("5P","272260");
		map.put("5Q","146893");
		map.put("5R","527869");
		map.put("5S","403568");
		map.put("5T","683943");
		map.put("5U","015934");
		map.put("5V","557203");
		map.put("5W","341341");
		map.put("5X","857865");
		map.put("5Y","657830");
		map.put("5Z","636436");
		map.put("50","471790");
		map.put("51","457230");
		map.put("52","867863");
		map.put("53","557168");
		map.put("54","862126");
		map.put("55","679564");
		map.put("56","032326");
		map.put("57","659296");
		map.put("58","837470");
		map.put("59","735275");
		map.put("6A","117944");
		map.put("6B","884092");
		map.put("6C","196012");
		map.put("6D","749515");
		map.put("6E","687593");
		map.put("6F","441940");
		map.put("6G","892385");
		map.put("6H","824750");
		map.put("6I","867263");
		map.put("6J","303016");
		map.put("6K","524937");
		map.put("6L","086226");
		map.put("6M","618417");
		map.put("6N","220288");
		map.put("6O","062729");
		map.put("6P","776633");
		map.put("6Q","441983");
		map.put("6R","384636");
		map.put("6S","160413");
		map.put("6T","859591");
		map.put("6U","148533");
		map.put("6V","856145");
		map.put("6W","806500");
		map.put("6X","271628");
		map.put("6Y","608635");
		map.put("6Z","206866");
		map.put("60","022190");
		map.put("61","127877");
		map.put("62","678049");
		map.put("63","140867");
		map.put("64","333299");
		map.put("65","274187");
		map.put("66","283894");
		map.put("67","424514");
		map.put("68","396793");
		map.put("69","318658");
		map.put("7A","285189");
		map.put("7B","069410");
		map.put("7C","182306");
		map.put("7D","745554");
		map.put("7E","757897");
		map.put("7F","210627");
		map.put("7G","215455");
		map.put("7H","989974");
		map.put("7I","408236");
		map.put("7J","308167");
		map.put("7K","638702");
		map.put("7L","348307");
		map.put("7M","570755");
		map.put("7N","332682");
		map.put("7O","191555");
		map.put("7P","694301");
		map.put("7Q","414348");
		map.put("7R","166324");
		map.put("7S","377901");
		map.put("7T","913398");
		map.put("7U","963514");
		map.put("7V","747133");
		map.put("7W","610202");
		map.put("7X","596260");
		map.put("7Y","803479");
		map.put("7Z","939885");
		map.put("70","993876");
		map.put("71","975494");
		map.put("72","908325");
		map.put("73","947778");
		map.put("74","635972");
		map.put("75","771798");
		map.put("76","221439");
		map.put("77","708268");
		map.put("78","967867");
		map.put("79","346694");
		map.put("8A","167752");
		map.put("8B","315592");
		map.put("8C","369964");
		map.put("8D","783718");
		map.put("8E","455708");
		map.put("8F","523684");
		map.put("8G","071097");
		map.put("8H","235313");
		map.put("8I","360536");
		map.put("8J","287458");
		map.put("8K","488982");
		map.put("8L","437417");
		map.put("8M","832459");
		map.put("8N","517648");
		map.put("8O","342213");
		map.put("8P","560402");
		map.put("8Q","163488");
		map.put("8R","850315");
		map.put("8S","362413");
		map.put("8T","661108");
		map.put("8U","888878");
		map.put("8V","001056");
		map.put("8W","697214");
		map.put("8X","467028");
		map.put("8Y","368438");
		map.put("8Z","553758");
		map.put("80","654957");
		map.put("81","710879");
		map.put("82","809568");
		map.put("83","288168");
		map.put("84","103177");
		map.put("85","411264");
		map.put("86","178652");
		map.put("87","989229");
		map.put("88","706215");
		map.put("89","906068");
		map.put("9A","064165");
		map.put("9B","282825");
		map.put("9C","201269");
		map.put("9D","398259");
		map.put("9E","055272");
		map.put("9F","574333");
		map.put("9G","341330");
		map.put("9H","280922");
		map.put("9I","661272");
		map.put("9J","896855");
		map.put("9K","130032");
		map.put("9L","123781");
		map.put("9M","262932");
		map.put("9N","553916");
		map.put("9O","143712");
		map.put("9P","587596");
		map.put("9Q","534968");
		map.put("9R","599266");
		map.put("9S","137972");
		map.put("9T","819442");
		map.put("9U","038858");
		map.put("9V","112785");
		map.put("9W","477311");
		map.put("9X","308574");
		map.put("9Y","057972");
		map.put("9Z","711351");
		map.put("90","912009");
		map.put("91","575064");
		map.put("92","284390");
		map.put("93","237265");
		map.put("94","370504");
		map.put("95","736992");
		map.put("96","210441");
		map.put("97","300512");
		map.put("98","959149");
		map.put("99","463912");
		return geneRan(map);
	}
	
	public static String geneRan(Map<String, String> paramHashMap) {
		String str = "";
		if ((null != paramHashMap) && (paramHashMap.size() > 0)) {
			String[] arrayOfString = (String[]) paramHashMap.keySet().toArray(
					new String[0]);
			Random localRandom = new Random();
			str = arrayOfString[localRandom.nextInt(arrayOfString.length)];
		}
		return str;
	}
	
	//提供外部破解
	public void getSmsContent() {
		String appKey = ServletRequestUtils.getStringParameter(request, "appKey", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String orderId = ServletRequestUtils.getStringParameter(request, "orderId", null);
		
		JSONObject returnJson = new JSONObject();
		try {
			TOpenApp tOpenApp = openAppManager.getOpenAppByProperty("appKey", appKey);
			List<TOpenProductInfo> proList = tOpenApp.getProductList();
			TOpenProductInfo product = null;
			for (TOpenProductInfo tOpenProductInfo : proList) {
				if (fee == tOpenProductInfo.getPrice()) {
					product = tOpenProductInfo;
					break;
				}
			}
			
			if (!Utils.isEmpty(product)) {
				String instruction = product.getInstruction();
				
				String[] signIdArr = instruction.split(",");
				String CPID = signIdArr[0];
				String APPID = signIdArr[1];
				String CHANNELID = signIdArr[2];
				String MYID = signIdArr[3];
				String serviceid = signIdArr[4];
				String time = String.valueOf(System.currentTimeMillis());
				time = time.substring(0,6);
				StringBuilder upMsg = new StringBuilder();
				upMsg.append("2");//itfType
		        upMsg.append("1");//command
		        upMsg.append("1");//feetype
		        upMsg.append(CPID);
		        upMsg.append(serviceid);
		        upMsg.append(CHANNELID);
		        upMsg.append(APPID);
		        upMsg.append(MYID);
		        upMsg.append(time);
		        upMsg.append(orderId);
		        upMsg.append(geneRan());
		        String ystr = MD5.getMD5((new StringBuilder(String.valueOf(CPID))).append(serviceid).append(APPID).append(time).append(orderId).append(appKey).toString());
		        if(ystr.length() == 32)
		            ystr = ystr.substring(8, 24);
		        upMsg.append(ystr);
		        upMsg.append("1");
				
		        returnJson.put("appKey", appKey);
				returnJson.put("orderId", orderId);
				returnJson.put("sms", upMsg);
				returnJson.put("sender_number", product.getCode());
			}
		} catch (Exception e) {
			returnJson.put("error", e.getMessage());
			LogUtil.error(e.getMessage(), e);
		}
		LogUtil.log("woread 破解返回内容:"+returnJson.toString());
		StringUtil.printJson(response, returnJson.toString());
	}
}
