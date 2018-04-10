package com.tenfen.www.action.external.open.cmcc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
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
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class CmccWABPAction extends SimpleActionSupport{

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
	
	// 公钥
	private static final String PUBLIC = "DSAPublicKey";
	// 私钥
	private static final String PRIVATE = "DSAPrivateKey";
	
	// 默认字符编码
	private static final String DEFAULT_CHARSET = "UTF-8";
	// 默认加密算法
	private static final String DEFAULT_SIGN_METHOD = "DSA";
	
	public static final Charset UTF_8 = Charset.forName(DEFAULT_CHARSET);
	
	public String mminput() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		String sign = ServletRequestUtils.getStringParameter(request, "sign", null);
		
		try {
			if (Utils.isEmpty(sellerKey)) {
				setRequestAttribute("msg", "seller_key参数不能为空");
				return "mmfail";
			} else if (Utils.isEmpty(appName)) {
				setRequestAttribute("msg", "app_name参数不能为空");
				return "mmfail";
			} else if (fee == 0) {
				setRequestAttribute("msg", "fee参数不能为空");
				return "mmfail";
			} else if (Utils.isEmpty(outTradeNo)) {
				setRequestAttribute("msg", "out_trade_no参数不能为空");
				return "mmfail";
			} else if (Utils.isEmpty(sign)) {
				setRequestAttribute("msg", "sign参数不能为空");
				return "mmfail";
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				setRequestAttribute("msg", "没有找到渠道相关信息");
				return "mmfail";
			}
			if (tOpenSeller.getStatus() == 0) {
				setRequestAttribute("msg", "该渠道已被关闭，请联系管理员");
				return "mmfail";
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
				return "mmfail";
			}
			
			setRequestAttribute("sellerKey", sellerKey);
			setRequestAttribute("appName", appName);
			setRequestAttribute("fee", fee);
			setRequestAttribute("outTradeNo", outTradeNo);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			setRequestAttribute("msg", "未知异常");
			return "mmfail";
		}
		return "mminput";
	}
	
	public void generateOrder() {
		String sellerKey = ServletRequestUtils.getStringParameter(request, "seller_key", null);
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		String imsi = phone;
		String appName = ServletRequestUtils.getStringParameter(request, "app_name", null);
		int fee = ServletRequestUtils.getIntParameter(request, "fee", 0);
		String outTradeNo = ServletRequestUtils.getStringParameter(request, "out_trade_no", null);
		
		//返回响应obj
		JSONObject returnJson = new JSONObject();
		try {
//			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
			String appNameDecode = new String(Base64.decodeBase64(appName));
			LogUtil.log("cmccwabp generate params: seller_key:"+sellerKey+" imsi:"+imsi+" appName:"+appNameDecode+" fee:"+fee+" outTradeNo:"+outTradeNo);
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
			} else if (Utils.isEmpty(outTradeNo)) {
				returnJson.put("code", "1006");
				returnJson.put("msg", "out_trade_no参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			} else if (Utils.isEmpty(phone)) {
				returnJson.put("code", "1007");
				returnJson.put("msg", "phone参数不能为空");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			//通过sellerKey查询渠道信息
			TOpenSeller tOpenSeller = openSellerManager.getOpenSellerByProperty("sellerKey", sellerKey);
			if (Utils.isEmpty(tOpenSeller)) {
				returnJson.put("code", "1008");
				returnJson.put("msg", "没有找到渠道相关信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (tOpenSeller.getStatus() == 0) {
				returnJson.put("code", "1009");
				returnJson.put("msg", "该渠道已被关闭，请联系管理员");
				StringUtil.printJson(response, returnJson.toString());
				return;
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
				returnJson.put("code", "1011");
				returnJson.put("msg", "没有找到相关app信息");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			if (!limit) {
				returnJson.put("code", "1012");
				returnJson.put("msg", "未找到符合条件app");
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
			String SMALLPAY_APP_PROVINCE_DAY_LIMIT = "mmwabp_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			//判断app日限是否到达
			Integer appdaylimit = (Integer)mc.getCache(SMALLPAY_APP_PROVINCE_DAY_LIMIT);
			appdaylimit = appdaylimit == null ? 0 : appdaylimit;
			if (appdaylimit_conf == -1) {//无限制
				//不处理
			} else if (appdaylimit < appdaylimit_conf){//缓存中限制还没到设定值
				//不处理
			} else {
				returnJson.put("code", "1013");
				returnJson.put("msg", "该省份已达到当日推送量");
				StringUtil.printJson(response, returnJson.toString());
				return;
			}
			
			Integer merchantId = tOpenApp.getMerchantId();
			Integer sellerId = tOpenSeller.getId();
//			String key = tOpenApp.getAppKey();//wabp公钥
			String secret = tOpenApp.getAppSecret();//公钥,私钥
			String bu = tOpenApp.getCallbackUrl();
			String[] keyPair = secret.split(",");
			String publicKeyStr = keyPair[0];
			String privateKeyStr = keyPair[1];
			String clientId = tOpenApp.getClientId();
			String ch = null;
			String ex = null;
			String sin = null;
			if (!Utils.isEmpty(clientId)) {
				JSONObject jsonObject = JSONObject.parseObject(clientId);
				ch = jsonObject.getString("ch");
				ex = jsonObject.getString("ex");
				sin = jsonObject.getString("sin");
			}
			
			Map<String, String> keyMap = getkeys(publicKeyStr, privateKeyStr);
			String apco = "apco";//内容id
			String aptid = "aptid";
			String aptrid = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
			String mid = getMid(phone);
			// 模拟数据
			Map<String, Object> map = getDataMap(apco, aptid, aptrid, ch, ex, sin, mid, bu);
			String signStr = buildSign(keyMap.get(PRIVATE), map);
			Map<String, String> paramMap = getParamMap(apco, aptid, aptrid, ch, ex, sin, mid, bu, signStr);
			generateOrder(outTradeNo, aptrid, appId, merchantId, sellerId, pro.getPrice(), imsi, phone, appNameDecode);
			String url = buildGetUrl("http://210.75.5.244/wabps/wap/purchase.action", paramMap);
			response.sendRedirect(url);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			returnJson.put("code", "9999");
			returnJson.put("msg", "系统未知异常");
			StringUtil.printJson(response, returnJson.toString());
			return;
		}
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
////			String appNameDecode = URLDecoder.decode(appName, "UTF-8");
//			String appNameDecode = new String(Base64.decodeBase64(appName));
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
//			String SMALLPAY_APP_PROVINCE_DAY_LIMIT = "mmwabp_app_prov_daylimit_"+appId+"_"+provinceEncoder+"_"+day;
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
//			Integer merchantId = tOpenApp.getMerchantId();
//			Integer sellerId = tOpenSeller.getId();
////			String key = tOpenApp.getAppKey();//wabp公钥
//			String secret = tOpenApp.getAppSecret();//公钥,私钥
//			String bu = tOpenApp.getCallbackUrl();
//			String[] keyPair = secret.split(",");
//			String publicKeyStr = keyPair[0];
//			String privateKeyStr = keyPair[1];
//			String clientId = tOpenApp.getClientId();
//			String ch = null;
//			String ex = null;
//			String sin = null;
//			if (!Utils.isEmpty(clientId)) {
//				JSONObject jsonObject = JSONObject.parseObject(clientId);
//				ch = jsonObject.getString("ch");
//				ex = jsonObject.getString("ex");
//				sin = jsonObject.getString("sin");
//			}
//			
//			Map<String, String> keyMap = getkeys(publicKeyStr, privateKeyStr);
//			String apco = "apco";//内容id
//			String aptid = "aptid";
//			String aptrid = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
//			String mid = getMid(phone);
//			// 模拟数据
//			Map<String, Object> map = getDataMap(apco, aptid, aptrid, ch, ex, sin, mid, bu);
//			String signStr = buildSign(keyMap.get(PRIVATE), map);
//			Map<String, String> paramMap = getParamMap(apco, aptid, aptrid, ch, ex, sin, mid, bu, signStr);
//			generateOrder(outTradeNo, aptrid, appId, merchantId, sellerId, pro.getPrice(), imsi, phone, appNameDecode);
//			String url = buildGetUrl("http://210.75.5.244/wabps/wap/purchase.action", paramMap);
//			response.sendRedirect(url);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//			returnJson.put("code", "9999");
//			returnJson.put("msg", "系统未知异常");
//			StringUtil.printJson(response, returnJson.toString());
//			return;
//		}
//	}
	
	private Map<String, String> getkeys(String publicKeyStr, String privateKeyStr) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(PUBLIC, publicKeyStr);
		map.put(PRIVATE, privateKeyStr);
		return map;
	}
	
	private String getMid(String phone) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "R");
		map.put("1", "I");
		map.put("2", "Z");
		map.put("3", "B");
		map.put("4", "H");
		map.put("5", "G");
		map.put("6", "E");
		map.put("7", "C");
		map.put("8", "F");
		map.put("9", "O");
		String change = "";
		for (int i = 0; i < phone.length(); i++) {
			String s = String.valueOf(phone.charAt(i));
			String zimu = map.get(s);
			change += zimu;
			if (i==4) {
				change += "KAF";
			}
		}
		return change;
	}
	
	/**
	 * 简单组装map(未做封装)
	 */
	private Map<String, Object> getDataMap(String apco,String aptid, 
			String aptrid, String ch, String ex, String sin, String mid, String bu) {

		Map<String, Object> map = new TreeMap<String, Object>(); // 用treeMap按照key做排序
		map.put("apco", apco);
		map.put("aptid", aptid);
		map.put("aptrid", aptrid);
		map.put("ch", ch);
		map.put("ex", ex);
		map.put("sin", sin);
		map.put("mid", mid);
		map.put("bu", bu);

		return map;
	}
	
	private Map<String, String> getParamMap(String apco,String aptid, String aptrid, 
			String ch, String ex, String sin, String mid, String bu, String sign) {

		Map<String, String> map = new TreeMap<String, String>(); // 用treeMap按照key做排序
		map.put("apco", apco);
		map.put("aptid", aptid);
		map.put("aptrid", aptrid);
		map.put("ch", ch);
		map.put("ex", ex);
		map.put("sin", sin);
		map.put("mid", mid);
		map.put("bu", bu);
		map.put("sign", sign);

		return map;
	}
	
	/**
	 * 
	 * 生成数字签名字符串
	 * 
	 * @Title: buildSign
	 * @param privateKey
	 *            私钥
	 * @param data
	 *            待校验数据
	 * @return
	 * @throws Exception
	 * @author: yanhuajian 2013-9-6上午10:37:30
	 */
	private String buildSign(String privateKey, Map<String, Object> data) throws Exception {

		// 按照标准url参数的形式组装签名源字符串
		String stringToSign = map2String(data);
		// 转换成二进制
		byte[] bytesToSign = stringToSign.getBytes(DEFAULT_CHARSET);

		// 初始化DSA签名工具
		Signature sg = Signature.getInstance("DSA");
		// 初始化DSA私钥
		sg.initSign((PrivateKey) getPrivateKey(privateKey));
		sg.update(bytesToSign);

		// 得到二进制形式的签名
		byte[] signBytes = sg.sign();
		// 进行标准Base64编码
		byte[] sign = Base64.encodeBase64(signBytes);
		// 转换成签名字符串
		String signContent = new String(sign);

		System.out.println("the sign content is: " + signContent);

		return signContent;
	}
	
	/**
	 * 
	 * 将map转换为url格式字符串
	 * 
	 * @Title: map2String
	 * @param map
	 * @return
	 * @author: yanhuajian 2013-7-21下午7:25:08
	 */
	private String map2String(Map<String, Object> map) {
		if (null == map || map.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}

		return sb.toString();
	}
	
	/**
	 * 通过私钥字符串初始化DSA的私钥
	 * 
	 * @return
	 * @throws Exception
	 */
	private PrivateKey getPrivateKey(String privateKeyStr)
			throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_SIGN_METHOD);
		EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyStr.getBytes(DEFAULT_CHARSET)));
		return keyFactory.generatePrivate(keySpec);
	}
	
	private String buildGetUrl(String url, Map<String, String> params) {
		StringBuffer uriStr = new StringBuffer(url);
		if (params != null) {
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				ps.add(new BasicNameValuePair(key, params.get(key)));
			}
			uriStr.append("?");
			uriStr.append(URLEncodedUtils.format(ps, UTF_8));
		}
		return uriStr.toString();
	}
	
	private void generateOrder(String outTradeNo, String orderId, Integer appId, Integer merchantId, Integer sellerId, Integer price, String imsi, String phone, String appName) {
		//创建订单
		TOpenOrder tOpenOrder = new TOpenOrder();
		tOpenOrder.setImsi(imsi);
		tOpenOrder.setOrderId(orderId);
		tOpenOrder.setOutTradeNo(outTradeNo);
		tOpenOrder.setAppId(appId);
		tOpenOrder.setMerchantId(merchantId);
		tOpenOrder.setSellerId(sellerId);
		tOpenOrder.setSubject(appName);
		tOpenOrder.setSenderNumber("");
		tOpenOrder.setMsgContent("");
		tOpenOrder.setFee(price);
		tOpenOrder.setPayPhone(phone);
		TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
		if (!Utils.isEmpty(mobileArea)) {
			tOpenOrder.setProvince(mobileArea.getProvince());
		}
		openOrderManager.save(tOpenOrder);
	}
	
	public void verifyUser() {
		
	}
	
	public void callBack() throws Exception {
		String retResult = "000";
		String retMsg = "成功";
		String retTrId = "";
		try {
			// 读取请求内容
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			String content = sb.toString();
			LogUtil.log("cmccWABP 接收到的xml："+content);
			
			int reduce = 0;
			String orderId = StringUtils.substringBetween(content, "<APTransactionID>", "</APTransactionID>");
			String result = StringUtils.substringBetween(content, "<ServiceAction>", "</ServiceAction>");
			TOpenOrder tOpenOrder = openOrderManager.getOpenOrderByProperty("orderId", orderId);
			if (!Utils.isEmpty(tOpenOrder)) {
				Integer appId = tOpenOrder.getAppId();
				String province = tOpenOrder.getProvince();
				String status = "4";
				if ("0".equals(result)) {
					status = "3";
				} else {
					status = "4";
				}
				retResult = "000";
				retMsg = "成功";
				retTrId = orderId;
				tOpenOrder.setStatus(status);
				if ("3".equals(status)) {
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
				if ("3".equals(status)) {
					openSellerManager.saveOpenSellerApps(tOpenOrder.getSellerId(), tOpenOrder.getAppId(), tOpenOrder.getFee());
				}
				//回调渠道
				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
				String callbackUrl = tOpenSeller.getCallbackUrl();
				if (!Utils.isEmpty(callbackUrl)) {
					String outTradeNo = tOpenOrder.getOutTradeNo();
					if (reduce != 1) {//不扣量
						new Thread(new SendPartner(status,orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					} else {
						new Thread(new SendPartner("4",orderId,outTradeNo,tOpenOrder.getFee()+"",callbackUrl)).start();
					}
				}
			} else {
				retResult = "001";
				retMsg = "失败";
			}
		} catch (Exception e) {
			retResult = "001";
			retMsg = "失败";
			LogUtil.log(e.getMessage(), e);
		}
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceWebTransfer2APRsp><APTransactionID>"+retTrId+"</APTransactionID><ResultCode>"+retResult+"</ResultCode><ResultMSG>"+retMsg+"</ResultMSG><RspTime>"+new Date()+"</RspTime></ServiceWebTransfer2APRsp>";
		StringUtil.printXml(response, xml);
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
			        LogUtil.log("sendToPartnerCmccWABPMsg:"+jsonObject.toString());
		        	HttpClientUtils.postJson(callbackUrl, jsonObject.toString());
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}
}
