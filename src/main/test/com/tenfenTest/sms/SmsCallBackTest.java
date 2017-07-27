package com.tenfenTest.sms;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SmsCallBackTest {
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String mobile = "15372098311";
			String sms_order = "kg256132015081114545228864";
			String product_type = "2";
			String state = "0";
			String op_type = "0";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = URLEncoder.encode(sdf.format(new Date()), "UTF-8");
			
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://localhost:8080/channel/external/iMusicOpen_callBack.action");
			
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("mobile",mobile));
			nvps.add(new BasicNameValuePair("sms_order",sms_order));
			nvps.add(new BasicNameValuePair("product_type",product_type));
			nvps.add(new BasicNameValuePair("state",state));
			nvps.add(new BasicNameValuePair("op_type", op_type));
			nvps.add(new BasicNameValuePair("time",time));
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
