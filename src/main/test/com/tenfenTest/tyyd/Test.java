package com.tenfenTest.tyyd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenfen.util.encrypt.MD5;
import com.tenfen.www.util.tyydclient.HttpSendClient;
import com.tenfen.www.util.tyydclient.HttpSendRequest;
import com.tenfen.www.util.tyydclient.HttpSendResponse;

public class Test {
//	public static void main(String[] args) {
//		String phone = "15372098311";
//		String imsi = "";
//		String userId="10010626410721";
//		String monthProductId = "100000168855095";
//		String ua = null;
//		Product p = TyReadXfUtil.getProduct(phone, userId, monthProductId, imsi, ua);
//	}
	
	private static String phone = "15372098311";
//	private static String url = "http://115.239.135.2:8110/portalapi/portalapi";
	private static String url = "http://61.130.247.175:80/portalapi/portalapi";
	private static String orderId = "11071971";
	private static String code = "7046";
	private static String monthProductId = "24283263";
//	private static String monthProductId = "135000000000000229233";
//	private static String monthProductId = "135000000000000232249";
//	private static String monthProductId = "135000000000000218997";
	
	//empOrderCreate
//	public static void main(String[] args) {
//		String key = "7513fa363b00107a";
////		String userId= "10010626410721";
//		String userAccount = phone;
//		
//		try {
//			Map<String, String> content = new HashMap<String ,String>();
//			Map<String, String> header = new HashMap<String ,String>();
//
//			HttpSendRequest request = new HttpSendRequest();
//			request.setContent(content);
//			request.setHeads(header);
//
//			request.setUrl(url);
//			request.setCharset("utf-8");
//			request.setTimeout(50000000);
//
//			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
//			header.put("userAccount",userAccount);
////			header.put("user-id", userId);
//			header.put("action", "empOrderCreate");
//			
//			String baoyue = "<Request>\n" +
//					"<EmpOrderCreateReq>\n" +
//					"<type>2</type>\n" +
//					"<payFee>800</payFee>\n" +
////					"<token>"+sort(new String[]{"2","800",monthProductId,userAccount,"38"},key)+"</token>\n" +
//					"<token>"+sort(new String[]{"2","800",monthProductId,userAccount},key)+"</token>\n" +
//					"<costPhone>"+userAccount+"</costPhone>\n" +
//					"<productId>"+monthProductId+"</productId>\n" +
////					"<rechargeChannel>38</rechargeChannel>\n" +
//					"</EmpOrderCreateReq>\n" +
//					"</Request>\n";
//			
//			request.getContent().put("content", baoyue);
//			HttpSendClient httpSendClient = new HttpSendClient();
//			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
//			printResponse(culverinResponse);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		String key = "7513fa363b00107a";
//		String userId= "10010626410721";
		String userAccount = phone;
//		String imsi = "460036970087302";
		
		try {
			Map<String, String> content = new HashMap<String ,String>();
			Map<String, String> header = new HashMap<String ,String>();

			HttpSendRequest request = new HttpSendRequest();
			request.setContent(content);
			request.setHeads(header);

			request.setUrl(url);
			request.setCharset("utf-8");
			request.setTimeout(50000000);

			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
			header.put("userAccount",userAccount);
//			header.put("user-id", userId);
			header.put("action", "empSecurityCodeFetch");
			
			String baoyue = "<Request>\n" +
					"<EmpSecurityCodeFetchReq>\n" +
					"<userIdentity>"+userAccount+"</userIdentity>\n" +
					"<userType>mdn</userType>\n" +
					"<orderId>"+orderId+"</orderId>\n" +
					"<token>"+sort(new String[]{userAccount,"mdn",orderId},key)+"</token>\n" +
					"</EmpSecurityCodeFetchReq>\n" +
					"</Request>\n";
			
			request.getContent().put("content", baoyue);
			HttpSendClient httpSendClient = new HttpSendClient();
			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
			printResponse(culverinResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		String key = "7513fa363b00107a";
////		String userId= "10010626410721";
//		String userAccount = phone;
//		
//		try {
//			Map<String, String> content = new HashMap<String ,String>();
//			Map<String, String> header = new HashMap<String ,String>();
//
//			HttpSendRequest request = new HttpSendRequest();
//			request.setContent(content);
//			request.setHeads(header);
//
//			request.setUrl(url);
//			request.setCharset("utf-8");
//			request.setTimeout(50000000);
//
//			header.put("Client-Agent", "TYYD_Android_4_0_1024_800_HW_C8812_JAVA_2_9_8/480*640/public");
//			header.put("userAccount",userAccount);
////			header.put("user-id", userId);
//			header.put("action", "empSecurityCodeValidate");
//			
//			String baoyue = "<Request>\n" +
//					"<EmpSecurityCodeValidateReq>\n" +
//					"<orderId>"+orderId+"</orderId>\n" +
//					"<code>"+code+"</code>\n" +
//					"<token>"+sort(new String[]{orderId,code},key)+"</token>\n" +
//					"</EmpSecurityCodeValidateReq>\n" +
//					"</Request>\n";
//			
//			request.getContent().put("content", baoyue);
//			HttpSendClient httpSendClient = new HttpSendClient();
//			HttpSendResponse culverinResponse = httpSendClient.executeHttpPost(request);
//			printResponse(culverinResponse);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static String sort(String[] args, String key){
		Arrays.sort(args);
		String mySign = "";
		for (int i = 0; i < args.length; i++) {
			mySign += args[i];
		}
		mySign += key;
		return MD5.getMD5(mySign);
	}
	
	public static void printResponse(HttpSendResponse response){
		if(response == null){
			System.out.println("===============================未存响应内容===============================");
			return ;
		}
		System.out.println("===============================响应输出===============================");
		System.out.println("response status=" + response.getResponseStatus());
		
		if(response.getResponseHeaders() != null){
			System.out.println("===============================headers===============================");
			for(String key : response.getResponseHeaders().keySet()){
				System.out.println(key + "=" + response.getResponseHeaders().get(key));
			}
		}
		
		
		if(response.getResponseBody() != null){
			System.out.println("===============================body content===============================");
			//System.out.println(replaceBlank(response.getResponseBody()));
			System.out.println(response.getResponseBody());
		}
		System.out.println("");
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("");
	}
	
	
//	public static void main(String[] args) {
//		String dy="1";
//		String pb="";
//		String randomCode = "";
//		
//		HttpClient httpClient = null;
//		try {
//			httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://wap.tyread.com/gossourl.action?monthProductId="+monthProductId+"&chargeMode=4");
//			
//			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//			nvps.add(new BasicNameValuePair("dy",dy));
//			nvps.add(new BasicNameValuePair("pb",pb));
//			nvps.add(new BasicNameValuePair("randomCode",randomCode));
//			nvps.add(new BasicNameValuePair("indentifyCode",code));
//			nvps.add(new BasicNameValuePair("tradeId", orderId));
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
//	}
	
	
	
	
	
	
	
	
	
	
	
//	private static String ua = "Mozilla/5.0(iPhone;CPU iPhone OS 8_0_2 like Mac OS X) AppleWebKit/600.1.4(KHTML,like Gecko) Version/8.0 Mobile/12A405 Safari/600.1.4";
//	private static String phone = "15372098311";
//	
//	public static void main(String[] args) {
//		String[] string = Test.test1();
////		System.out.println(string[0]+string[1]);
//		
//		String sessionId = string[1];
//		String[] param = test2(sessionId,string[0]);
////		System.out.println(a);
//		
//		test3(sessionId, param[0], param[1]);
//	}
//	
//	public static String[] test1(){
//		String[] returnString = new String[2];
//		HttpClient httpClient = null;
//		try {
//			httpClient = createHttpClient();
//			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//
//			HttpGet httpGet = new HttpGet("http://wap.tyread.com/baoyueInfoListAction.action?monthProductId=100000168855095&xtype=1");
//			httpGet.setHeader("x-up-calling-line-id", phone);
//			httpGet.setHeader("User-Agent", ua);
//
//			HttpResponse response = httpClient.execute(httpGet);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response
//						.getEntity());
//				
//				String url = RegExp.getString(responseString,
//						"(?<=href=\")(.*?)(?=\".*>订购包月)");
//				returnString[0] = "http://wap.tyread.com"+url;
//			}
//			
//			String sessionId = getSessionId(httpClient);
//			returnString[1] = sessionId;
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		} finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
//		}
//		return returnString;
//	}
//	
//	public static String[] test2(String sessionId, String url){
//		HttpClient httpClient = null;
//		String returnString[] = new String[2];
//		try {
//			httpClient = createHttpClient();
//			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//			HttpGet httpGet = new HttpGet(url);
//			httpGet.setHeader("x-up-calling-line-id", phone);
//			httpGet.setHeader("User-Agent", ua);
//			if(null != sessionId){
//				httpGet.setHeader("Cookie", "JSESSIONID=" + sessionId);
//	        }
//			
//			HttpResponse response = httpClient.execute(httpGet);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				String responseString = EntityUtils.toString(response.getEntity());
//				
//				String urlCode = RegExp.getString(responseString,
//						"(?<=name=\"urlCode\" value=\")(.*?)(?=\">)");
//				String tradeId = RegExp.getString(responseString,
//						"(?<=name=\"tradeId\" value=\")(.*?)(?=\">)");
//				returnString[0] = urlCode;
//				returnString[1] = tradeId;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return returnString;
//	}
//	
//	//获取验证码
//	public static String test3(String sessionId, String urlCode, String tradeId){
//		HttpClient httpClient = null;
//		String returnString = null;
//		try {
//			httpClient = createHttpClient();
//			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);//设置连接超时时间为10秒
//			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);//设置无响应超时时间为10秒
//			HttpGet httpGet = new HttpGet("http://wap.tyread.com/user/getIdentifyCode.action?urlCode="+urlCode+"&tradeId="+tradeId);
//			
//			if(null != sessionId){
//				httpGet.setHeader("Cookie", "JSESSIONID=" + sessionId);
//	        }
//			
//			HttpResponse response = httpClient.execute(httpGet);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				returnString = EntityUtils.toString(response.getEntity());
//				
//				System.out.println(returnString);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return returnString;
//	}
//	
//	public static String getSessionId(HttpClient httpClient){
//		CookieStore store = ((AbstractHttpClient) httpClient).getCookieStore();
//		List<Cookie> list = store.getCookies();
//		String sessionId = "";
//		for (int i = 0; i < list.size(); i++) {
//			Cookie cookie = (Cookie) list.get(i);
//			String cookieName = cookie.getName();
//			if (cookieName.equals("JSESSIONID")) {
//				sessionId = cookie.getValue();
//				break;
//			}
//		}
//		return sessionId;
//	}
//	
//	public static HttpClient createHttpClient() {
//		HttpClient httpClient = new DefaultHttpClient();
//		return httpClient;
//	};
}
