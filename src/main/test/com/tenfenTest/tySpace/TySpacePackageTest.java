package com.tenfenTest.tySpace;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class TySpacePackageTest {
	
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String sellerKey = "802aef4e453a4494b91d12d82fae54f6";
			String secret = "7baa2db16ff74d3d8eccdb15501cebf0";
			String imsi = "460022036974955";
			String appName = URLEncoder.encode("猛士突袭", "UTF-8");
			String subject = URLEncoder.encode("猛士突袭", "UTF-8");
			String fee = "1000";
//			String outTradeNo = String.valueOf(System.currentTimeMillis());
			String outTradeNo = "18eacf280";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name", appName));
			queryParamList.add(new TokenParam("subject", subject));
			queryParamList.add(new TokenParam("fee", fee));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, secret);
			
			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://localhost:8080/channel/external/tySpacechOpen_generateSubscribeOrder.action");
			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/tySpacechOpen_generateSubscribeOrder.action");
			
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("seller_key",sellerKey));
			nvps.add(new BasicNameValuePair("imsi",imsi));
			nvps.add(new BasicNameValuePair("app_name",appName));
			nvps.add(new BasicNameValuePair("subject",subject));
			nvps.add(new BasicNameValuePair("fee",fee));
			nvps.add(new BasicNameValuePair("out_trade_no", outTradeNo));
			nvps.add(new BasicNameValuePair("sign",sign));
			post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
			
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				String responseString = EntityUtils.toString(response.getEntity());
				System.out.println(responseString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
	}
	
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String sellerKey = "OPEN_PARTNER_ZHANGTUO";
//			String secret = "50b534c257414bb2be45054b608a5383";
//			String orderId = "20150914151414564585";
//			String smsCode = "0818";
//			
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("orderId",orderId));
//			queryParamList.add(new TokenParam("smsCode",smsCode));
//			String sign = TokenService.buildToken(queryParamList, secret);
//			
//			httpClient = new DefaultHttpClient();
////			HttpPost post = new HttpPost("http://localhost:8080/channel/external/tySpacechOpen_pay.action");
//			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/tySpacechOpen_pay.action");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("seller_key",sellerKey));
//			nvps.add(new BasicNameValuePair("orderId",orderId));
//			nvps.add(new BasicNameValuePair("smsCode",smsCode));
//			nvps.add(new BasicNameValuePair("sign",sign));
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
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String method = "createOrder";
//			String channel = "1301";
//			String app = "消灭星星";
//			String name = "钻石礼包";
//			String detail = "http://www.gomzone.com";
//			String packageName = "十分包月5元";
//			String ver = "1.0";
//			String sig = null;
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			
//			String imsi = "460030090634987";
//			String amount = "5";
//			String chargeType = "1";
//			String orderId = String.valueOf(System.currentTimeMillis());
//			
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
//			sig = TokenService.buildTySpaceToken(queryParamList, "uSqziOAhoui8P8Nk4odt7xmrhQI");
//			
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://m.52yole.com:8085/CTPay/Subscribe.ashx");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("method",method));
//			nvps.add(new BasicNameValuePair("channel",channel));
//			nvps.add(new BasicNameValuePair("app",app));
//			nvps.add(new BasicNameValuePair("name",name));
//			nvps.add(new BasicNameValuePair("detail", detail));
//			nvps.add(new BasicNameValuePair("packageName",packageName));
//			nvps.add(new BasicNameValuePair("ver",ver));
//			nvps.add(new BasicNameValuePair("timestamp",timestamp));
//			nvps.add(new BasicNameValuePair("imsi",imsi));
//			nvps.add(new BasicNameValuePair("amount",amount));
//			nvps.add(new BasicNameValuePair("chargeType",chargeType));
//			nvps.add(new BasicNameValuePair("orderId",orderId));
//			nvps.add(new BasicNameValuePair("sig",sig));
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
//	}
	
	
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String method = "submitOrder";
//			String channel = "1301";
//			String app = "消灭星星";
//			String name = "钻石礼包";
//			String detail = "http://www.gomzone.com";
//			String packageName = "十分包月5元";
//			String ver = "1.0";
//			String sig = null;
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			
//			String orderSN = "20150909161232324162";
//			String smsCode = "5583";
//			
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
//			sig = TokenService.buildTySpaceToken(queryParamList, "uSqziOAhoui8P8Nk4odt7xmrhQI");
//			
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://m.52yole.com:8085/CTPay/Subscribe.ashx");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("method",method));
//			nvps.add(new BasicNameValuePair("channel",channel));
//			nvps.add(new BasicNameValuePair("app",app));
//			nvps.add(new BasicNameValuePair("name",name));
//			nvps.add(new BasicNameValuePair("detail", detail));
//			nvps.add(new BasicNameValuePair("packageName",packageName));
//			nvps.add(new BasicNameValuePair("ver",ver));
//			nvps.add(new BasicNameValuePair("timestamp",timestamp));
//			nvps.add(new BasicNameValuePair("sig",sig));
//			nvps.add(new BasicNameValuePair("orderSN",orderSN));
//			nvps.add(new BasicNameValuePair("smsCode",smsCode));
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
}
