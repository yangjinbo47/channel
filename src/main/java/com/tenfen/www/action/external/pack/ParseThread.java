package com.tenfen.www.action.external.pack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.RegExp;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.util.tyydclient.HttpSendClient;
import com.tenfen.www.util.tyydclient.HttpSendRequest;
import com.tenfen.www.util.tyydclient.HttpSendResponse;

public class ParseThread implements Runnable {
	
	private String ua = "Mozilla/5.0 (Linux; U; Android 4.1.1; zh-cn; HW-HUAWEI_C8813/C8813V100R001C92B169; 480*854; CTC/2.0) AppleWebKit/534.30 (KHTML, like Gecko) Mobile Safari/534.30";
	
	private String imsi;
	private String phone;
	private String monthProductId;
	private String packageName;
	private Integer fee;
	private String outTradeNo;
	private Integer sellerId;
	private Integer pushId;
	private String province;
	
	private OrderManager orderManager;
	
	public ParseThread(String imsi, String phone, String monthProductId, String packageName, Integer fee, String outTradeNo, Integer sellerId, Integer pushId, String province, OrderManager orderManager) {
		this.imsi = imsi;
		this.phone = phone;
		this.monthProductId = monthProductId;
		this.packageName = packageName;
		this.fee = fee;
		this.outTradeNo = outTradeNo;
		this.sellerId = sellerId;
		this.pushId = pushId;
		this.province = province;
		
		this.orderManager = orderManager;
	}

	@Override
	public void run() {
		try {
			HttpClientContext context = HttpClientContext.create();
			CookieStore cookieStore = new BasicCookieStore();
			CloseableHttpClient client = buildHttpClient(cookieStore, buildRequestConfig());
			
			String firstUrl = "http://wap.tyread.com/baoyueInfoListAction.action";
			String orderUrl = getOrderUrlByUrl(monthProductId, firstUrl, phone, ua, client, context, cookieStore);
			
			orderUrl = "http://wap.tyread.com" + orderUrl;
			String tradeId = goPreBuySubmit(orderUrl, phone, ua, client, context, cookieStore);
			LogUtil.log("tyread wapparse:phone="+phone+",tradeId="+tradeId);
			//发送验证码
			boolean isSucc = empSecurityCodeFetch(tradeId, phone);
			//保存入库
			if (isSucc) {
				TOrder order = new TOrder();
				order.setTradeId(tradeId);
				order.setOutTradeNo(outTradeNo);
				order.setImsi(imsi);
				order.setPhoneNum(phone);
				order.setName(packageName);
				order.setSellerId(sellerId);
				order.setPushId(pushId);
				order.setProvince(province);
				order.setFee(fee);
				// 保存订单(批量)并增加当日订购数
				orderManager.save(order);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	private CloseableHttpClient buildHttpClient(CookieStore cookieStore, RequestConfig requestConfig) {
		
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.build();
		
		return client;
	}
	
	private RequestConfig buildRequestConfig() {
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(30000)
				.setConnectTimeout(30000).build();
		return requestConfig;
	}
	
	/**
	 * @功能：根据url获取包月地址
	 * @author BOBO
	 * @date 2014-3-31
	 * @param url
	 * @param phone
	 * @return
	 */
	public String getOrderUrlByUrl(String monthProductId, String url, String phone, String ua, CloseableHttpClient client, HttpClientContext context, CookieStore cookieStore) throws Exception{
		String orderUrl = null;
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("monthProductId", monthProductId);
			params.put("xtype", "1");
			
			HttpGet get = buildHttpGet(url, params);
			get.addHeader("X-Up-Calling-Line-ID", phone);
			get.setHeader("User-Agent", ua);
			get.setHeader("X-Forwarded-For","10.8.70.176");
			get.setHeader("X-Source-ID","10.234.86.39");
			get.setHeader("X-Real-Ip","61.130.246.71");
			
			CloseableHttpResponse response = client.execute(get, context);
			cookieStore = context.getCookieStore();
			try {
				String respStr = null;
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					respStr = EntityUtils.toString(entity, "UTF-8");
				}
				EntityUtils.consume(entity);
				
				orderUrl = RegExp.getString(respStr, "(?<=href=\")(.*?)(?=\".*>订购包月)");
				
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}

		return orderUrl;
	}
	
	public static String goPreBuySubmit(String url, String phone, String ua, CloseableHttpClient client, HttpClientContext context, CookieStore cookieStore) throws Exception{
		String returnStr = null;
		try {
			HttpGet get = new HttpGet(url);
			get.addHeader("X-Up-Calling-Line-ID", phone);
			get.setHeader("User-Agent", ua);
			get.setHeader("X-Forwarded-For","10.8.70.176");
			get.setHeader("X-Source-ID","10.234.86.39");
			get.setHeader("X-Real-Ip","61.130.246.71");
			
			CloseableHttpResponse response = client.execute(get, context);
			cookieStore = context.getCookieStore();
			try {
				String respStr = null;
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					respStr = EntityUtils.toString(entity, "UTF-8");
				}
				EntityUtils.consume(entity);
				
				returnStr = RegExp.getString(respStr, "(?<=\"tradeId\".value=\")(.*?)(?=\".*>)");
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return returnStr;
	}
	
	private HttpGet buildHttpGet(String url, Map<String, String> params) throws URISyntaxException {
		HttpGet get = new HttpGet(buildGetUrl(url, params));
		return get;
	}
	
	private String buildGetUrl(String url, Map<String, String> params) {
		StringBuffer uriStr = new StringBuffer(url);
		if (params != null) {
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				ps.add(new BasicNameValuePair(key, params.get(key)));
			}
			uriStr.append("?");
			uriStr.append(URLEncodedUtils.format(ps, "UTF-8"));
		}
		return uriStr.toString();
	}
	
	private final String key = "7513fa363b00107a";
	private static String url = "http://61.130.247.175:80/portalapi/portalapi";
	//发送验证码
	private Boolean empSecurityCodeFetch(String tradeId, String phone) {
		boolean isSucc = false;
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("utf-8");
			request.setTimeout(50000000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",phone);
			header.put("action", "empSecurityCodeFetch");
			
			String baoyue = "<Request>\n" +
					"<EmpSecurityCodeFetchReq>\n" +
					"<userIdentity>"+phone+"</userIdentity>\n" +
					"<userType>mdn</userType>\n" +
					"<orderId>"+tradeId+"</orderId>\n" +
					"<token>"+sort(new String[]{phone,"mdn",tradeId},key)+"</token>\n" +
					"</EmpSecurityCodeFetchReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			if (!Utils.isEmpty(culverinResponse)) {
				LogUtil.log("culverinResponse:"+culverinResponse.getResponseBody());
				isSucc = Boolean.parseBoolean(StringUtils.substringBetween(culverinResponse.getResponseBody(), "<isSuccess>", "</isSuccess>"));
			}
		} catch (Exception e) {
			LogUtil.log(e.getMessage(), e);
		}
		return isSucc;
	}
	
	public static String sort(String[] args, String key){
		Arrays.sort(args);
		String mySign = "";
		for (int i = 0; i < args.length; i++) {
			mySign += args[i];
		}
		mySign += key;
		return MD5.getMD5(mySign);
	}
}
