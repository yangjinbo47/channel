package com.tenfenTest.tySpace;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.util.LogUtil;

public class CallBack {
	
	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
	
	public static void main(String[] args) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("order_no", "20150924175435672275");
			jsonObject.put("out_trade_no", "20150924868195603c85b9f2d1");
			jsonObject.put("fee", "1200");
			jsonObject.put("status", "3");
			
			System.out.println(jsonObject.toString());
			
	        HttpPost httpPost = new HttpPost("http://115.29.168.140:2588/post/TenPersent.aspx");
	        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
	        
	        StringEntity se = new StringEntity(jsonObject.toString());
	        se.setContentType(CONTENT_TYPE_TEXT_JSON);
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
	        httpPost.setEntity(se);
	        httpClient.execute(httpPost);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

}
