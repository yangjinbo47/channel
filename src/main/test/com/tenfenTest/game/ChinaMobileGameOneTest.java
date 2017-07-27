package com.tenfenTest.game;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.tenfen.util.LogUtil;

public class ChinaMobileGameOneTest {
	
	private static final String APPLICATION_XML = "application/xml";
	private static final String CONTENT_TYPE_TEXT_XML = "text/xml";
	
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://115.159.74.129:8000/o/vp/ba04900c56561e04930d");
			post.addHeader(HTTP.CONTENT_TYPE, APPLICATION_XML);
			
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><request><imsi>460028581846908</imsi><imei>358016063126423</imei><price>0.10</price></request>";
			StringEntity se = new StringEntity(xml);
			se.setContentType(CONTENT_TYPE_TEXT_XML);
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_XML));
	        post.setEntity(se);
	        
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
		
		
		
//		try {
//			String a = QueryMDNByIMSI("460028581846908", "358016063126423", "0.10");
//			System.out.println(a);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	
	
	
	
	
	
	
	
	public static String QueryMDNByIMSI(String imsi, String imei, String price) {
		// TODO 自动生成方法存根
		try {
			URL urls = new URL("http://115.159.74.129:8000/o/vp/ba04900c56561e04930d");
			StringBuffer message = new StringBuffer();
			
			message.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
			.append("<request>")
			.append("<imsi>"+imsi+"</imsi>")
			.append("<imei>"+imei+"</imei>")
			.append("<price>"+price+"</price>")
			.append("</request>");
			String msg = message.toString();
			
			byte[] bytes = msg.getBytes("UTF-8");
			HttpURLConnection con = (HttpURLConnection) urls.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			con.setRequestProperty("Content-length", String.valueOf(bytes.length));
			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStream out = con.getOutputStream();
			
			out.write(bytes);
			out.flush();
			out.close();
			
			InputStream incoming = con.getInputStream();
			byte[] b = new byte[incoming.available()];
			incoming.read(b);
			return new String(b);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return null;
		}
	}
}
