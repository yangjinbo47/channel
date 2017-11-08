package com.tenfen.www.action.system;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.tenfen.util.LogUtil;
import com.tenfen.util.RegExp;

public class PackTest {
	
	public static void main(String[] args) {
		String monthProductId = "105032454";
		String phone = "15365731080";
		String ua = "Mozilla/5.0 (Linux; U; Android 4.1.1; zh-cn; HW-HUAWEI_C8813/C8813V100R001C92B169; 480*854; CTC/2.0) AppleWebKit/534.30 (KHTML, like Gecko) Mobile Safari/534.30";
		try {
			HttpClientContext context = HttpClientContext.create();
			CookieStore cookieStore = new BasicCookieStore();
			CloseableHttpClient client = buildHttpClient(cookieStore, buildRequestConfig());
			
			String orderUrl = getOrderUrlByUrl(monthProductId, "http://wap.tyread.com/baoyueInfoListAction.action", phone, ua, client, context, cookieStore);
			
			orderUrl = "http://wap.tyread.com" + orderUrl;
			String tradeId = goPreBuySubmit(orderUrl, phone, ua, client, context, cookieStore);
//			System.out.println(tradeId);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @功能：根据url获取包月地址
	 * @author BOBO
	 * @date 2014-3-31
	 * @param url
	 * @param phone
	 * @return
	 */
	public static String getOrderUrlByUrl(String monthProductId, String url, String phone, String ua, CloseableHttpClient client, HttpClientContext context, CookieStore cookieStore) throws Exception{
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
	
	public static String getYZM(String url, String phone, String ua, CloseableHttpClient client, HttpClientContext context, CookieStore cookieStore) throws Exception{
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
				System.out.println(respStr);
//				EntityUtils.consume(entity);
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
	
	private static CloseableHttpClient buildHttpClient(CookieStore cookieStore, RequestConfig requestConfig) {

		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.build();

//		if (isMultiThread)
//			client = HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
//		else
//			client = HttpClientBuilder.create().build();
		// 设置代理服务器地址和端口
		// client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
		return client;
	}
	
	private static HttpGet buildHttpGet(String url, Map<String, String> params)
			throws URISyntaxException {
		HttpGet get = new HttpGet(buildGetUrl(url, params));
//		get.setConfig(buildRequestConfig());
		return get;
	}
	
	private static String buildGetUrl(String url, Map<String, String> params) {
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
	
	private static RequestConfig buildRequestConfig() {
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(30000)
				.setConnectTimeout(30000).build();
		return requestConfig;
	}
	
//	public static String getSessionId(HttpClient httpClient){
//		CookieStore store = ((CloseableHttpClient) httpClient).getCookieStore();
//		List<Cookie> list = store.getCookies();
//		String sessionId = "";
//		for (int i = 0; i < list.size(); i++) {
//			Cookie cookie = (Cookie) list.get(i);
//			String cookieName = cookie.getName();
//			if (cookieName.equals("JSESSIONID")) {
//				sessionId = cookie.getValue();
//				break;
//			}
//		}
//		return sessionId;
//	}

}
