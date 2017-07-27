package com.tenfenTest.tySpace;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.util.LogUtil;

public class TySpaceLtCallbackTest {
	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
	
	public static void main(String[] args) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("out_trade_no", "ad077b64410b40");
			jsonObject.put("fee", "100");
			jsonObject.put("status", "00");
			
	        HttpPost httpPost = new HttpPost("http://sa.91muzhi.com:8090/sdk/Open189Rsq");
	        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
	        
	        LogUtil.log("sendMsg:"+jsonObject.toString());
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
