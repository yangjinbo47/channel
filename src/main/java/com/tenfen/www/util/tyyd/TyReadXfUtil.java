/**
*@author BOBO
*@功能：
*@Version:
*/
package com.tenfen.www.util.tyyd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tenfen.bean.operation.Product;

/**
 * @author BOBO
 *
 */
public class TyReadXfUtil {

	public static int successCount = 0;
	public static int failCount = 0; 
	
	public static String remoteIp = "61.130.247.175:80";//不走nginx 商用,正式访问使用
	public static String clientAgent ="TYYD_Android_2_1_240_320_HW_C8500_JAVA_2_5_2/720*1028/HUAWEI_C8500";
	public static String version = "1.0.0";
	public static String contentType = "application/xml;charset=UTF-8";
	public static String userType="1";
	
	/**
	 * @param phoneNum
	 * @return
	 * @author BOBO
	 */
	public static String getUserId(String phoneNum, String imsi, String ua) {
		String action = "register";
		String param= "";
		String xml ="<Request><RegisterReq><clientVersion>TYYD_Android_JAVA_3_1_0</clientVersion><clientHash>clientHash</clientHash></RegisterReq></Request>";
		String url = "http://" + remoteIp + "/portalapi/portalapi" + param;
		
		String str = sendRequest(url, action, xml, phoneNum, imsi, "", ua);
		String userId = StringUtils.substringBetween(str, "<userID>", "</userID>");
		return userId;
	}
	
	public static String createEmp(String phoneNum, String userId, String imsi, String ua){
		String type = "2";
		String costPhone = "15372098311";
		String productId = "60749771";
		
		String action = "empOrderCreate";
		String param= "";
		String xml ="<request><EmpOrderCreateReq><Client-Agent>"+clientAgent+"</Client-Agent><type>"+type+"</type><costPhone>"+costPhone+"</costPhone><productId>"+productId+"</productId><payFee>800</payFee><bookId></bookId><token>1111</token></EmpOrderCreateReq></request>";
		String url = "http://" + remoteIp + "/portalapi/portalapi" + param;
		
		String str = sendRequest(url, action, xml, phoneNum, imsi, userId, ua);
		return str;
	}
	
	public static Product getProduct(String phoneNum, String userId, String monthProductId, String imsi, String ua) {
		String action = "subscribepackproductBySSO";
		String param = "?productId="+monthProductId;
		String xml = "";
		String url = "http://" + remoteIp + "/portalapi/portalapi" + param;
		
		String str = sendRequest(url, action, xml, phoneNum, imsi, userId, ua);
		String ssoUrl = StringUtils.substringBetween(str, "<SSOURL>", "</SSOURL>");
		Product p = null;
		if (ssoUrl != null) {
			String ssoAddr = "http://61.130.247.171:8080/sso";
			String sid = StringUtils.substringBetween(ssoUrl, "SID=", "&");
			String spid = StringUtils.substringBetween(ssoUrl, "SPID=", "&");
			String colId = StringUtils.substringBetween(ssoUrl, "colId=", "&");
			String tmpidp = StringUtils.substringBetween(ssoUrl, "tmpidp=", "&");
			String tmpidsp = StringUtils.substringBetween(ssoUrl, "tmpidsp=", "&");
			String token = StringUtils.substringBetween(ssoUrl, "token=", "&");
			p = new Product();
			p.setSsoAddr(ssoAddr);
			p.setSid(sid);
			p.setSpid(spid);
			p.setMonthProductId(colId);
			p.setTmpidp(tmpidp);
			p.setTmpidsp(tmpidsp);
			p.setToken(token);
		}
		return p;
	}
	
	public static String queryMDNByImsi(String imsi, String ua) {
		String action = "authenticateByIsmi";
		String param= "?imsi="+imsi;
		String xml ="";
		String url = "http://" + remoteIp + "/portalapi/portalapi" + param;
		
		String str = sendRequest(url, action, xml, null, imsi, "", ua);
//		System.out.println(str);
		return null;
	}
	
	public static String sendRequest(String url, String action, String xml, String phoneNum, String imsi, String userId, String ua) {
//		long startTime = System.currentTimeMillis();
		String returnString = null;
		if (xml.equals("")) {// GET
			returnString = sendGetRequest(url, action, phoneNum, imsi, userId, ua);
		} else {// POST
			returnString = sendPostRequest(url, action, xml, phoneNum, imsi, userId, ua);
		}
//		System.out.println("The cost time is:" + (System.currentTimeMillis() - startTime));
		return returnString;
	}
	
	/** 发送GET请求 */
	public static String sendGetRequest(String url, String action, String phoneNum, String imsi, String userId, String ua) {
		HttpClient httpclient = new DefaultHttpClient();
		String returnString = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Client-Agent", clientAgent);
			httpGet.setHeader("x-up-calling-line-id", phoneNum);
			httpGet.setHeader("phone-number", phoneNum);
			httpGet.setHeader("client-imsi", imsi);
			httpGet.setHeader("user-id", userId);
			httpGet.setHeader("user-agent",ua);
			httpGet.setHeader("APIVersion", version);
			httpGet.setHeader("Content-Type", contentType);
			httpGet.setHeader("Accept-Encoding", "UTF-8");
			httpGet.setHeader("Cookie", "");
			httpGet.setHeader("Action", action);
			httpGet.setHeader("userType", userType);
			httpGet.setHeader("guest-id", null);
			HttpResponse response = null;
//			Long start = System.currentTimeMillis();
			response = httpclient.execute(httpGet);
//			System.out.println("response time:" + (System.currentTimeMillis() - start));
			Header h[] = response.getAllHeaders();
			for (int i = 0; i < h.length; i++) {
				int index = h[i].toString().indexOf(':');
				String head = h[i].toString().substring(0, index).trim();
				if (head.equals("result-code")) {
					String value = h[i].toString().substring(index + 1).trim();
					if (!"3004".equals(value) && !"3999".equals(value)) {
						successCount++;
					} else {
						failCount++;
					}
				}
			}
			InputStream inputS = response.getEntity().getContent();
			byte b[] = new byte[512000];
			byte a[] = new byte[1024];
			int readLen = -1;
			int total = 0;
			while ((readLen = inputS.read(a)) != -1) {
				System.arraycopy(a, 0, b, total, readLen);
				total += readLen;
			}
//			System.out.println("sendGetRequest finish");
			returnString = new String(b, 0, total, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {			
			httpclient.getConnectionManager().shutdown();
		}
		return returnString;
	}
	
	public static String sendPostRequest(String url, String action, String xml, String phoneNum, String imsi, String userId, String ua) {
		HttpClient httpclient = new DefaultHttpClient();
		String returnString = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Client-Agent", clientAgent);
			httpPost.setHeader("x-up-calling-line-id", phoneNum);
			httpPost.setHeader("phone-number", phoneNum);
			httpPost.setHeader("user-id", userId);
			httpPost.setHeader("user-agent",ua);
			httpPost.setHeader("APIVersion", version);
			httpPost.setHeader("Content-Type", contentType);
			httpPost.setHeader("Accept-Encoding", "UTF-8");
			httpPost.setHeader("Cookie", "");
			httpPost.setHeader("Action", action);
			httpPost.setHeader("userType", userType);
//			httpPost.setHeader("guest-id", null);
			httpPost.setHeader("client-imsi", imsi);
			
			InputStreamEntity reqEntity;
			reqEntity = new InputStreamEntity(new ByteArrayInputStream(xml.getBytes("UTF-8")), xml.getBytes("UTF-8").length);
			httpPost.setEntity(reqEntity);
			HttpResponse response = null;
			response = httpclient.execute(httpPost);
			
			Header h[] = response.getAllHeaders();
			int totalLen = 0;
			for (int i = 0; i < h.length; i++) {
				int index = h[i].toString().indexOf(':');
				String head = h[i].toString().substring(0, index).trim();
				if (head.equals("result-code")) {
					String value = h[i].toString().substring(index + 1).trim();
					if (!"3004".equals(value) && !"3999".equals(value)) {
						successCount++;
					} else {
						failCount++;
					}
				} else if (head.equals("Content-Length")) {
					String value = h[i].toString().substring(index + 1).trim();
					totalLen = Integer.valueOf(value);
				}
			}

			if (totalLen != 0) {
				InputStream inputS = response.getEntity().getContent();
				byte b[] = new byte[totalLen];
				byte a[] = new byte[1024];
				int readLen = -1;
				int total = 0;
				while ((readLen = inputS.read(a)) != -1) {
					System.arraycopy(a, 0, b, total, readLen);
					total += readLen;
				}
				if (total == totalLen) {
					returnString = new String(b, 0,totalLen, "UTF-8");
				} else {
//					System.out.println("The response is fail");
				}
			}
//			System.out.println("sendPostReq finish");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {			
			httpclient.getConnectionManager().shutdown();
		}
		return returnString;
	}
	
}
