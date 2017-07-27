package com.tenfenTest.woRead;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.util.DateUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class WoReadTest {
//	public static void main(String[] args) {
//		HttpClient httpClient = null;
//		try {
//			String sellerKey = "OPEN_PARTNER_ZHANGTUO";
//			String imsi = "460036970087302";
//			String appName = URLEncoder.encode("测试游戏", "UTF-8");
//			String fee = "100";
//			String outTradeNo = String.valueOf(System.currentTimeMillis());
//			
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("app_name", appName));
//			queryParamList.add(new TokenParam("fee", fee));
//			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
//			String sign = TokenService.buildToken(queryParamList, "50b534c257414bb2be45054b608a5383");
//			
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/woReadOpen_generateOrder.action");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("seller_key",sellerKey));
//			nvps.add(new BasicNameValuePair("imsi",imsi));
//			nvps.add(new BasicNameValuePair("app_name",appName));
//			nvps.add(new BasicNameValuePair("fee",fee));
//			nvps.add(new BasicNameValuePair("out_trade_no", outTradeNo));
//			nvps.add(new BasicNameValuePair("sign",sign));
//			post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
//			
//			HttpResponse response = httpClient.execute(post);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response.getEntity());
//				
//				JSONObject json = JSONObject.parseObject(responseString);
//				String code = json.getString("code");
//				System.out.println(code);
//				String msg = json.getString("msg");
//				System.out.println(msg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		
//	}
	
	
	public static String appendZero(String str, int length) {
		// String.valueOf()是用来将其他类型的数据转换为string型数据的
		String tmpString = str;
		for (int i = tmpString.length(); i < length; i++) {
			tmpString = "0" + tmpString;
		}
		return tmpString;
	}
	public static void main(String[] args) {
		String orderNo = DateUtil.getCurrentTimestamp("yyyyMMddHHmmssSSS") + Math.round(Math.random() * 1000);
		orderNo = appendZero(orderNo, 32);
		String appKey = "a0201a73cc7d25818ea1a2a784c3d67c";
		
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("appKey", appKey);
			map.put("fee", "100");
			map.put("orderId", orderNo);
			
			String res = HttpClientUtils.simplePostInvoke("http://localhost:8080/channel/external/woReadOpen_getSmsContent.action", map);
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
