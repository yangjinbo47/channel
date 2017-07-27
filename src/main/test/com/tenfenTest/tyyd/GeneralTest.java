package com.tenfenTest.tyyd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GeneralTest {
//	public static void main(String[] args) {
//		try {
//			String sellerKey = "OPEN_PARTNER_JINGSI";
//			String sellerSecret = "35999c81f0c8438cb1518943b4d2a2c4";
//			String imsi = "460036431196818";
//			String appName = URLEncoder.encode("测试游戏", "UTF-8");
//			String fee = "400";
//			String outTradeNo = String.valueOf(System.currentTimeMillis());
//			
//			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
//			queryParamList.add(new TokenParam("seller_key",sellerKey));
//			queryParamList.add(new TokenParam("imsi",imsi));
//			queryParamList.add(new TokenParam("app_name", appName));
//			queryParamList.add(new TokenParam("fee", fee));
//			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
//			String sign = TokenService.buildToken(queryParamList, sellerSecret);
//			
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("seller_key",sellerKey);
//			map.put("imsi",imsi);
//			map.put("app_name",appName);
//			map.put("fee",fee);
//			map.put("out_trade_no", outTradeNo);
//			map.put("sign",sign);
//			String responseString = HttpClientUtils.simplePostInvoke("http://www.gomzone.com:8080/external/generalOpen_generateOrder.action", map);
////			String responseString = HttpClientUtils.simplePostInvoke("http://localhost:8080/channel/external/generalOpen_generateOrder.action", map);
//			JSONObject json = JSONObject.parseObject(responseString);
//			String code = json.getString("code");
//			System.out.println(code);
//			String msg = json.getString("msg");
//			System.out.println(msg);
//			JSONArray jsonMsgArray = JSONArray.parseArray(msg);
//			for (int i = 0; i < jsonMsgArray.size(); i++) {
//				JSONObject jsonMsg = (JSONObject)jsonMsgArray.get(i);
//				System.out.println(jsonMsg.getString("message_content"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		String result = "";
		BufferedReader in = null;
		try {			
			String url = "http://www.gomzone.com:8080/external/createOrder.action?imsi=460030090634987&channel=ANQIN_MUZHI&out_trade_no="+System.currentTimeMillis()+"&price=1000";
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			
			connection.connect();
			
			in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
