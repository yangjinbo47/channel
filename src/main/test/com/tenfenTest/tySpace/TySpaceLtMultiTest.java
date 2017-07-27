package com.tenfenTest.tySpace;

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

public class TySpaceLtMultiTest {
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String sellerKey = "OPEN_PARTNER_MUZHIYOUWAN";
			String imsi = "460030237386954";
			String appName = URLEncoder.encode("羊羊去哪儿", "UTF-8");
//			String appName = "%E6%9C%80%E6%96%B0%E5%8D%95%E6%9C%BASDK";
			String fee = "200";
			String outTradeNo =  String.valueOf(System.currentTimeMillis());
//			String sign = "f6b140eeef0766735a0c3f2772b0a199";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name", appName));
			queryParamList.add(new TokenParam("fee", fee));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, "5438e99521e747e98a67450f3972e992");
			
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://www.gomzone.com/external/tySpaceltOpen_generateMultiOrder.action");
//			HttpPost post = new HttpPost("http://localhost:8080/channel/external/tySpaceltOpen_generateMultiOrder.action");
			
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
