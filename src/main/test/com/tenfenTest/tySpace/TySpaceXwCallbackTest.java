package com.tenfenTest.tySpace;

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

public class TySpaceXwCallbackTest {
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String method = "getSmsDetail";
			String channel = "5004";
			String imsi = "460036970087302";
			String timestamp = String.valueOf(System.currentTimeMillis());
			String ver = "1.0";
			String id_fee = "111_1";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("method",method));
			queryParamList.add(new TokenParam("channel",channel));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("timestamp", timestamp));
			queryParamList.add(new TokenParam("ver", ver));
			queryParamList.add(new TokenParam("id_fee", id_fee));
			String sig = TokenService.buildTySpaceToken(queryParamList, "n2KSZHJyu38K096SvmvQZbO5HZ8");
			
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://m.52yole.com:8085/CTPay/API.ashx");
			
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("method",method));
			nvps.add(new BasicNameValuePair("channel",channel));
			nvps.add(new BasicNameValuePair("imsi",imsi));
			nvps.add(new BasicNameValuePair("timestamp",timestamp));
			nvps.add(new BasicNameValuePair("ver",ver));
			nvps.add(new BasicNameValuePair("id_fee",id_fee));
			nvps.add(new BasicNameValuePair("sig",sig));
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
}
