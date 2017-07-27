package com.tenfen.www.action.external.open.woplus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
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
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class WoPlusOpenAction extends SimpleActionSupport{

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
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		int chargeFee = 0;
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
			LogUtil.log("woplus 参数: seller_key:"+sellerKey+" imsi:"+imsi+" fee:"+fee+" outTradeNo:"+outTradeNo+" sign:"+sign);
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
			} else if (Utils.isEmpty(imsi)) {
				returnJson.put("code", "1003");
				returnJson.put("msg", "imsi参数不能为空");
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
			} else if (Utils.isEmpty(phone)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "phone参数不能为空");
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
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			queryParamList.add(new TokenParam("phone", phone));
			String geneSign = TokenService.buildToken(queryParamList, tOpenSeller.getSellerSecret());
			if (!sign.toLowerCase().equals(geneSign)) {
				returnJson.put("code", "1009");
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
			String WOPlUS_APP_PROVINCE_DAY_LIMIT = "woplus_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			//判断app日限是否到达
			Integer appdaylimit = (Integer)mc.getCache(WOPlUS_APP_PROVINCE_DAY_LIMIT);
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
			String secret = tOpenApp.getAppSecret();
			String token = (String)mc.getCache(Constants.PRE_WOPLUS_TOKEN+appId);
			if (Utils.isEmpty(token)) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("appKey", key);
				map.put("appSecret", secret);
				
				String res = HttpClientUtils.simpleGetInvoke("https://open.wo.com.cn/openapi/authenticate/v1.0", map);
				JSONObject json = JSONObject.parseObject(res);
				token = json.getString("token");
				mc.setCache(Constants.PRE_WOPLUS_TOKEN+appId, token, CacheFactory.DAY * 7);
			}
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
						
						JSONObject json = generateWoPlusOrder(proList.get(i).getName(), outTradeNo, appId, merchantId, sellerId, key, token, proList.get(i).getPrice(), imsi, phone, proList.get(i).getCode(), proList.get(i).getInstruction());
						if (Utils.isEmpty(json)) {
							returnJson.put("code", "1011");
							returnJson.put("msg", "已达到今日限量值");
							StringUtil.printJson(response, returnJson.toString());
							return;
						}
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
	 * 支付
	 */
	public void pay() {
		Integer verifyCode = ServletRequestUtils.getIntParameter(request, "verify_code", 0);
		String orderId = ServletRequestUtils.getStringParameter(request, "order_id", null);
		JSONObject returnJson = new JSONObject();
//		if (verifyCode == 878) {
//			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
//			tOpenOrder.setPayTime(new Date());
//			tOpenOrder.setStatus("3");
//			openOrderManager.save(tOpenOrder);
//			
//			returnJson.put("order_no", orderId);
//			returnJson.put("out_trade_no", tOpenOrder.getOutTradeNo());
//			returnJson.put("fee", tOpenOrder.getFee());
//			returnJson.put("status", "3");
//			StringUtil.printJson(response, returnJson.toString());
//			return;
//		}
		try {
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				String phone = tOpenOrder.getPayPhone();
				Integer appId = tOpenOrder.getAppId();
				Integer sellerId = tOpenOrder.getSellerId();
				TOpenApp tOpenApp = openAppManager.get(appId);
				//callback
				String appKey = tOpenApp.getAppKey();
				String appSecret = tOpenApp.getAppSecret();
				ICacheClient mc = cacheFactory.getCommonCacheClient();
				String token = (String)mc.getCache(Constants.PRE_WOPLUS_TOKEN+appId);
				if (Utils.isEmpty(token)) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("appKey", appKey);
					map.put("appSecret", appSecret);
					
					String res = HttpClientUtils.simpleGetInvoke("https://open.wo.com.cn/openapi/authenticate/v1.0", map);
					JSONObject json = JSONObject.parseObject(res);
					token = json.getString("token");
					mc.setCache(Constants.PRE_WOPLUS_TOKEN+appId, token, CacheFactory.MINUTE * 7);
				}
				
				// Post请求的url，与get不同的是不需要带参数
				URL postUrl = new URL("https://open.wo.com.cn/openapi/rpc/apppayment/v2.0");
				HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
				// 打开读写属性，默认均为false
				connection.setDoOutput(true);
				connection.setDoInput(true);
				// 设置请求方式，默认为GET
				connection.setRequestMethod("POST");
				// Post 请求不能使用缓存
				connection.setUseCaches(false);
				connection.setInstanceFollowRedirects(true);
				// 时间建议长点
				connection.setConnectTimeout(60000);
				connection.setReadTimeout(60000);
				// 配置连接的Content-type，配置为application/json;charset=UTF-8
				connection.setRequestProperty("Content-Type",
						"application/json;charset=UTF-8");
				connection.setRequestProperty("Accept", "application/json");
				String head = "appKey=" + "\"" + appKey + "\"" + ", token=" + "\""
						+ token + "\"" + "";
				connection.setRequestProperty("Authorization", head);

				DataOutputStream out = new DataOutputStream(
						connection.getOutputStream());
				// 正文内容其实跟get的URL中'?'后的参数字符串一致
				HashMap<String, Object> cnts = new HashMap<String, Object>();
				// //联通手机号
				cnts.put("paymentUser", phone);
				cnts.put("paymentAcount", "001");
				// 外部订单号
				cnts.put("outTradeNo", orderId);
				// 商品名称
				cnts.put("subject", tOpenOrder.getSubject());
				Integer fee = tOpenOrder.getFee();
				Integer price = fee/100;
				// 总费用
				cnts.put("totalFee", price);
				// 时间戳
				cnts.put("timeStamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
				cnts.put("paymentcodesms", verifyCode);
				
				// 后台通知支付状态的URL
				String secretKey = appKey + "&" + appSecret;
				String signature = Encrypt.encryptHmacSha1(cnts, secretKey);

				// signType必须是HMAC-SHA1
				cnts.put("signType", "HMAC-SHA1");
				cnts.put("signature", signature);
				String content = JSON.toJSONString(cnts);

				// DataOutputStream.writeBytes将字符串中的16位的 unicode字符以8位的字符形式写道流里面
				out.write(content.getBytes("UTF-8"));
				out.flush();
				out.close(); // flush and close
				int responseCode = connection.getResponseCode();
				String resultData = null;
				if (200 == responseCode || 201 == responseCode) {
					InputStream in = connection.getInputStream();
					resultData = getResponseResult(new InputStreamReader(in, "UTF-8"));
					connection.disconnect();
				} else {
					BufferedInputStream err = new BufferedInputStream(
							connection.getErrorStream());
					resultData = getResponseResult(new InputStreamReader(err));
					connection.disconnect();
				}
				
				LogUtil.log("woplus pay 返回："+resultData);
				
				JSONObject json = JSONObject.parseObject(resultData);
				String code = json.getString("resultCode");
				String transactionId = json.getString("transactionId");
				if ("0".equals(code)) {
					tOpenOrder.setPayTime(new Date());
					tOpenOrder.setStatus("3");
					tOpenOrder.setMsgContent(transactionId);
					
					//增加缓存记录值
					setLimitCache(phone, sellerId, appId, fee);
				} else {
					tOpenOrder.setPayTime(new Date());
					tOpenOrder.setStatus("4");
					tOpenOrder.setMsgContent(transactionId);
				}
				openOrderManager.save(tOpenOrder);
				
				if ("0".equals(code)) {
					//增加今日量
					openSellerManager.saveOpenSellerApps(sellerId, appId, fee);
				}
				
				String outTradeNo = tOpenOrder.getOutTradeNo();
				returnJson.put("order_no", orderId);
				returnJson.put("out_trade_no", outTradeNo);
				returnJson.put("fee", fee);
				returnJson.put("status", tOpenOrder.getStatus());
				StringUtil.printJson(response, returnJson.toString());
			} else {
				returnJson.put("code", "1001");
				returnJson.put("msg", "订单号不存在");
				StringUtil.printJson(response, returnJson.toString());
			}
			
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private JSONObject generateWoPlusOrder(String productName, String outTradeNo, Integer appId, Integer merchantId, Integer sellerId, String appKey, String token, Integer price, String imsi, String phone, String senderNumber, String instruction) {
		JSONObject returnJsonMsgObj = null;
		try {
			//订单号
			String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String result = generateWoPlusOrder(productName, imsi, phone, orderNo, price, appKey, token);
			JSONObject json = JSONObject.parseObject(result);
			Integer code = json.getInteger("resultCode");
			String description = json.getString("resultDescription");
			
			if (code == 0) {
				//创建订单
				TOpenOrder tOpenOrder = new TOpenOrder();
				tOpenOrder.setImsi(imsi);
				tOpenOrder.setOrderId(orderNo);
				tOpenOrder.setOutTradeNo(outTradeNo);
				tOpenOrder.setAppId(appId);
				tOpenOrder.setMerchantId(merchantId);
				tOpenOrder.setSellerId(sellerId);
				tOpenOrder.setSubject(productName);
				tOpenOrder.setSenderNumber(senderNumber);
				tOpenOrder.setMsgContent(instruction);
				tOpenOrder.setFee(price);
				tOpenOrder.setPayPhone(phone);
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				if (!Utils.isEmpty(mobileArea)) {
					tOpenOrder.setProvince(mobileArea.getProvince());
				}
				openOrderManager.save(tOpenOrder);
				
				returnJsonMsgObj = new JSONObject();
				returnJsonMsgObj.put("order_id", orderNo);
				returnJsonMsgObj.put("out_trade_no", outTradeNo);
				returnJsonMsgObj.put("fee", price);
				returnJsonMsgObj.put("description", description);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnJsonMsgObj;
	}
	
	private String generateWoPlusOrder(String productName, String imsi, String phone, String orderId, Integer price, String appKey, String token) {
		String resultData = null;
		try {
			Integer fee = price/100;
			
			// Post请求的url，与get不同的是不需要带参数
			URL postUrl = new URL("https://open.wo.com.cn/openapi/rpc/paymentcodesms/v2.0");
			HttpURLConnection connection = (HttpURLConnection)postUrl.openConnection();
			// 打开读写属性，默认均为false
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// 设置请求方式，默认为GET
			connection.setRequestMethod("POST");
			// Post 请求不能使用缓存
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			// 时间建议长点
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);
			// 配置连接的Content-type，配置为application/json;charset=UTF-8
			connection.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");

			connection.setRequestProperty("Accept", "application/json");

			String head = "appKey=" + "\"" + appKey + "\"" + ", token=" + "\""
					+ token + "\"" + "";

			connection.setRequestProperty("Authorization", head);

			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			// 正文内容其实跟get的URL中'?'后的参数字符串一致
			HashMap<String, Object> cnts = new HashMap<String, Object>();

			// 联通手机号
			cnts.put("paymentUser", phone);
			// 操作类型：0 按次扣费 1 周期性订购
			cnts.put("paymentType", 0);
			// 外部订单号
			cnts.put("outTradeNo", orderId);
			// 支付账户类型
			cnts.put("paymentAcount", "001");
			// 商品名称
			cnts.put("subject", productName);
			// 商品描述
			cnts.put("description", productName);
			// 商品单价
			cnts.put("price", fee);
			// 购买数量
			cnts.put("quantity", 1);
			// 交易金额
			cnts.put("totalFee", fee);
			
			String content = JSON.toJSONString(cnts);
			// DataOutputStream.writeBytes将字符串中的16位的 unicode字符以8位的字符形式写道流里面
			out.write(content.getBytes("UTF-8"));
			out.flush();
			out.close();
			int responseCode = connection.getResponseCode();
			if (200 == responseCode || 201 == responseCode) {
				InputStream in = connection.getInputStream();
				resultData = getResponseResult(new InputStreamReader(in, "UTF-8"));
				connection.disconnect();
			} else {
				BufferedInputStream err = new BufferedInputStream(
						connection.getErrorStream());
				resultData = getResponseResult(new InputStreamReader(err));
				connection.disconnect();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return resultData;
	}
	
	public static String getResponseResult(InputStreamReader inputReader) {
		String dataTemp = "";
		BufferedReader buffer = null;
		try {
			buffer = new BufferedReader(inputReader);
			String inputLine = null;
			dataTemp = "";
			while ((inputLine = buffer.readLine()) != null) {
				dataTemp += inputLine + "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != buffer) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return dataTemp;
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

			String WOPLUS_USER_DAY_LIMIT = "woplus_user_daylimit_"+phone+"_"+day;
			String WOPLUS_USER_MONTH_LIMIT = "woplus_user_monthlimit_"+phone+"_"+month;
			String WOPLUS_SELLER_PROVINCE_DAY_LIMIT = "woplus_seller_prov_daylimit_"+sellerId+"_"+provinceEncoder+"_"+day;
			String WOPLUS_APP_PROVINCE_DAY_LIMIT = "woplus_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			String WOPLUS_APP_PROVINCE_MONTH_LIMIT = "woplus_app_prov_monthlimit_"+appId+"_"+provinceEncoder+"_"+month;
			
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			//用户日限
			Integer userDaylimit = (Integer)iCacheClient.getCache(WOPLUS_USER_DAY_LIMIT);
			if (Utils.isEmpty(userDaylimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, start, end);
				iCacheClient.setCache(WOPLUS_USER_DAY_LIMIT, sumfeeByPhone+fee, CacheFactory.DAY);
			} else {
				iCacheClient.setCache(WOPLUS_USER_DAY_LIMIT, userDaylimit+fee, CacheFactory.DAY);
			}
			//用户月限
			Integer userMonthlimit = (Integer)iCacheClient.getCache(WOPLUS_USER_MONTH_LIMIT);
			if (Utils.isEmpty(userMonthlimit)) {
				Integer sumfeeByPhone = openOrderManager.getSumFeeByPhone(sellerId, phone, mstart, mend);
				iCacheClient.setCache(WOPLUS_USER_MONTH_LIMIT, sumfeeByPhone+fee, CacheFactory.UNEXPIRY);
			} else {
				iCacheClient.setCache(WOPLUS_USER_MONTH_LIMIT, userMonthlimit+fee, CacheFactory.UNEXPIRY);
			}
			//渠道省份日限
			Integer sellerdaylimit = (Integer)iCacheClient.getCache(WOPLUS_SELLER_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(sellerdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String sellerProvLimitKey = "woplus_seller_prov_daylimit_"+sellerId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(sellerProvLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_SELLER_PROVINCE_DAY_LIMIT, sellerdaylimit+fee, CacheFactory.DAY);
			}
			//app省份日限
			Integer appdaylimit = (Integer)iCacheClient.getCache(WOPLUS_APP_PROVINCE_DAY_LIMIT);
			if (Utils.isEmpty(appdaylimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, start, end, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvDayLimitKey = "woplus_app_prov_daylimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+day;
						iCacheClient.setCache(appProvDayLimitKey, provFee+fee, CacheFactory.DAY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_APP_PROVINCE_DAY_LIMIT, appdaylimit+fee, CacheFactory.DAY);
			}
			//app省份月限
			Integer appmonthlimit = (Integer)iCacheClient.getCache(WOPLUS_APP_PROVINCE_MONTH_LIMIT);
			if (Utils.isEmpty(appmonthlimit)) {
				Map<String, String> map = openOrderManager.getProvinceCountByAppId(appId, mstart, mend, "3");
				for (String prov : map.keySet()) {
					if (!Utils.isEmpty(prov)) {						
						JSONObject json = JSONObject.parseObject(map.get(prov));
						Integer provFee = json.getInteger("fee");
						String appProvMonthLimitKey = "woplus_app_prov_monthlimit_"+appId+"_"+URLEncoder.encode(prov, "UTF-8")+"_"+month;
						iCacheClient.setCache(appProvMonthLimitKey, provFee+fee, CacheFactory.UNEXPIRY);
					}
				}
			} else {
				iCacheClient.setCache(WOPLUS_APP_PROVINCE_MONTH_LIMIT, appmonthlimit+fee, CacheFactory.UNEXPIRY);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
