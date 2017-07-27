package com.tenfenTest.sms;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;

public class SmsCreatOrderTest {
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String sellerKey = "SMS_PARTNER_TEST";
			String secret = "877eb16a61d1430884ddcae163fcc064";
			String imsi = "460034122148723";
			String appName = URLEncoder.encode("测试游戏", "UTF-8");
			String fee = "500";
			String outTradeNo = String.valueOf(System.currentTimeMillis());
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name", appName));
			queryParamList.add(new TokenParam("fee", fee));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, secret);
			
			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/iMusicOpen_generatePackageOrder.action");
			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/smsPay_generateOrder.action");
//			HttpPost post = new HttpPost("http://localhost:8080/channel/external/smsPay_generateOrder.action");
			
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("seller_key",sellerKey));
			nvps.add(new BasicNameValuePair("imsi",imsi));
			nvps.add(new BasicNameValuePair("app_name",appName));
			nvps.add(new BasicNameValuePair("fee",fee));
			nvps.add(new BasicNameValuePair("out_trade_no", outTradeNo));
			nvps.add(new BasicNameValuePair("sign",sign));
			post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
			
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				String responseString = EntityUtils.toString(response.getEntity());
				
				System.out.println(responseString);
//				JSONObject json = JSONObject.parseObject(responseString);
//				String code = json.getString("code");
//				System.out.println(code);
//				String msg = json.getString("msg");
//				System.out.println(msg);
//				JSONArray jsonMsgArray = JSONArray.parseArray(msg);
//				for (int i = 0; i < jsonMsgArray.size(); i++) {
//					JSONObject jsonMsg = (JSONObject)jsonMsgArray.get(i);
//					System.out.println(jsonMsg.getString("message_content"));
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		
	}
}
