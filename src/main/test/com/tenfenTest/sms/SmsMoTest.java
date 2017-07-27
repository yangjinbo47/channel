package com.tenfenTest.sms;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.util.LogUtil;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SmsMoTest {
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String mobile = "13311255712";
//			String spnum = "10661128";
//			String linkId = "06041036390000077302";
//			String productId = "135000000000000233773";
//			String msg = "a9#32143125";
//			
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://www.gomzone.com:8080/sms/smsOrder_mo.action");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("mobile",mobile));
//			nvps.add(new BasicNameValuePair("spnum",spnum));
//			nvps.add(new BasicNameValuePair("link_id",linkId));
//			nvps.add(new BasicNameValuePair("product_id",productId));
//			nvps.add(new BasicNameValuePair("msg", msg));
//			post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
//			
//			HttpResponse response = httpClient.execute(post);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response.getEntity());
//				System.out.println(responseString);
//			}
////			System.out.println(MD5.getMD5("app_name=%E5%A5%87%E8%91%A9%E6%96%97%E5%9C%B0%E4%B8%BB&fee=200&imsi=460036840008651&out_trade_no=14320173817962335_01C6QU&seller_key=OPEN_PARTNER_QIPAedfd58678d9e49359a2443cfbc5c7236"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//		}
//		
//		
//	}
	
	public static void main(String[] args) {
		try {
			String sellerKey = "SMS_PARTNER_TEST";
			String appName = URLEncoder.encode("有缘网", "UTF-8");
			String fee = "100";
			String outTradeNo = System.currentTimeMillis()+"";
			String imsi = "460036970087302";
			String secret = "877eb16a61d1430884ddcae163fcc064";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name",appName));
			queryParamList.add(new TokenParam("fee",fee+""));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, secret);
			
			System.out.println("outTradeNo:"+outTradeNo);
			System.out.println("sign:"+sign);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String mobile = "15372098311";
//			String spnum = "10660631";
//			String product_id = "135000000000000225897";
//			String msg = "k4#20150708152106267778";
//			
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://www.gomzone.com:8080/sms/smsOrder_mo.action");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("mobile",mobile));
//			nvps.add(new BasicNameValuePair("spnum",spnum));
//			nvps.add(new BasicNameValuePair("product_id",product_id));
//			nvps.add(new BasicNameValuePair("msg",msg));
//			post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
//			
//			HttpResponse response = httpClient.execute(post);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response.getEntity());
//				System.out.println(responseString);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//		}
//		
//		
//	}
	
//	private static final String APPLICATION_JSON = "application/json";
//	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
//	
//	public static void main(String[] args) {
//		DefaultHttpClient httpClient = new DefaultHttpClient();
//		try {
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("out_trade_no", System.currentTimeMillis());
//			jsonObject.put("phone", "15372098311");
//			jsonObject.put("fee", "100");
//			jsonObject.put("status", "3");
//			
//	        HttpPost httpPost = new HttpPost("http://182.92.129.54/IFA/sfkj/callback.php");
//	        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
//	        
//	        StringEntity se = new StringEntity(jsonObject.toString());
//	        se.setContentType(CONTENT_TYPE_TEXT_JSON);
//	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
//	        httpPost.setEntity(se);
//	        HttpResponse response = httpClient.execute(httpPost);
//	        if (response.getStatusLine().getStatusCode() == 200) {
//	        	String responseString = EntityUtils.toString(response.getEntity());
//	        	System.out.println(responseString);
//	        }
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//		}
//	}
}
