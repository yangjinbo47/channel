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

public class TySpaceLtTest {
	public static void main(String[] args) {
		HttpClient httpClient = null;
		try {
			String sellerKey = "OPEN_PARTNER_MUZHIYOUWAN";
			String imsi = "460030302997049";
			String appName = URLEncoder.encode("天降萌宠", "UTF-8");
//			String appName = "%e6%b6%88%e8%90%8c%e8%90%8c";
			String fee = "400";
			String outTradeNo = String.valueOf(System.currentTimeMillis());
//			String outTradeNo = "OD143409018798400000";
			
			List<TokenParam> queryParamList = new ArrayList<TokenParam>(4);
			queryParamList.add(new TokenParam("seller_key",sellerKey));
			queryParamList.add(new TokenParam("imsi",imsi));
			queryParamList.add(new TokenParam("app_name", appName));
			queryParamList.add(new TokenParam("fee", fee));
			queryParamList.add(new TokenParam("out_trade_no", outTradeNo));
			String sign = TokenService.buildToken(queryParamList, "5438e99521e747e98a67450f3972e992");
			
			httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://www.gomzone.com:8080/external/tySpaceltOpen_generateOrder.action");
			
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
//			System.out.println(MD5.getMD5("app_name=%E5%A5%87%E8%91%A9%E6%96%97%E5%9C%B0%E4%B8%BB&fee=200&imsi=460036840008651&out_trade_no=14320173817962335_01C6QU&seller_key=OPEN_PARTNER_QIPAedfd58678d9e49359a2443cfbc5c7236"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		
	}
}
