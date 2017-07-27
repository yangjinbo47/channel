package com.tenfenTest.game;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class ChinaMobileGameTwoTest {
	
	private static final String APPLICATION_XML = "application/xml";
	private static final String CONTENT_TYPE_TEXT_XML = "text/xml";
	
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://115.159.74.129:8000/o/svp/ba04900c56561e04930d");
			post.addHeader(HTTP.CONTENT_TYPE, APPLICATION_XML);
			
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><request><content_sid>内容识别码</content_sid></request >";
			StringEntity se = new StringEntity(xml);
			se.setContentType(CONTENT_TYPE_TEXT_XML);
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_XML));
	        
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
