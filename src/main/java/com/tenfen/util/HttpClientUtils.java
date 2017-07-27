package com.tenfen.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpClientUtils {
	/**
	 * 连接超时时间
	 */
	public static final int CONNECTION_TIMEOUT_MS = 30000;

	/**
	 * 读取数据超时时间
	 */
	public static final int SO_TIMEOUT_MS = 30000;

	public static final String APPLICATION_JSON = "application/json";
	
	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
	
	public static final String CONTENT_TYPE_JSON_CHARSET = "application/json;charset=utf-8";

	public static final String CONTENT_TYPE_XML_CHARSET = "application/xml;charset=utf-8";

	/**
	 * httpclient读取内容时使用的字符集
	 */
	public static final String CONTENT_CHARSET = "UTF-8";

	public static final Charset UTF_8 = Charset.forName(CONTENT_CHARSET);

	/**
	 * 简单get调用
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String simpleGetInvoke(String url, Map<String, String> params)
			throws ClientProtocolException, IOException, URISyntaxException {
		return simpleGetInvoke(url, params, CONTENT_CHARSET);
	}

	/**
	 * 简单get调用
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String simpleGetInvoke(String url,
			Map<String, String> params, String charset)
			throws ClientProtocolException, IOException, URISyntaxException {
		String returnStr = null;

		CloseableHttpClient client = buildHttpClient(false);
		try {
			HttpGet get = buildHttpGet(url, params);
			CloseableHttpResponse response = client.execute(get);
			try {
				assertStatus(response);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					returnStr = EntityUtils.toString(entity, charset);
				}
				EntityUtils.consume(entity);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		} finally {
			client.close();
		}
		return returnStr;
	}

	/**
	 * 简单post调用
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String simplePostInvoke(String url, Map<String, String> params)
			throws URISyntaxException, ClientProtocolException, IOException {
		return simplePostInvoke(url, params, CONTENT_CHARSET);
	}

	/**
	 * 简单post调用
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String simplePostInvoke(String url,
			Map<String, String> params, String charset)
			throws URISyntaxException, ClientProtocolException, IOException {
		String returnStr = null;
		CloseableHttpClient client = buildHttpClient(false);
		try {
			HttpPost postMethod = buildHttpPost(url, params);
			
			CloseableHttpResponse response = client.execute(postMethod);
			try {			
				assertStatus(response);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					returnStr = EntityUtils.toString(entity, charset);
				}
				EntityUtils.consume(entity);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		} finally {
			client.close();
		}
		return returnStr;
	}
	
	public static String postJson(String url, String json) throws Exception{
		CloseableHttpClient client = null;
		String returnStr = null;
		try {
			client = buildHttpClient(false);
			
			HttpPost postMethod = buildHttpJsonPost(url, json);
			
			CloseableHttpResponse response = client.execute(postMethod);
			try {
				assertStatus(response);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					returnStr = EntityUtils.toString(entity, CONTENT_CHARSET);
				}
				EntityUtils.consume(entity);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return returnStr;
	}

	/**
	 * 创建HttpClient
	 * 
	 * @param isMultiThread
	 * @return
	 */
	private static CloseableHttpClient buildHttpClient(boolean isMultiThread) {

		CloseableHttpClient client;

		if (isMultiThread)
			client = HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
		else
			client = HttpClientBuilder.create().build();
		// 设置代理服务器地址和端口
		// client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
		return client;
	}

	/**
	 * 构建httpPost对象
	 * 
	 * @param url
	 * @param headers
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	private static HttpPost buildHttpPost(String url, Map<String, String> params)
			throws UnsupportedEncodingException, URISyntaxException {
		HttpPost post = new HttpPost(url);
		setCommonHttpMethod(post);
		post.setConfig(buildRequestConfig());
		HttpEntity he = null;
		if (params != null) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
			he = new UrlEncodedFormEntity(formparams, UTF_8);
			post.setEntity(he);
		}
		// 在RequestContent.process中会自动写入消息体的长度，自己不用写入，写入反而检测报错
		// setContentLength(post, he);
		return post;
	}
	
	private static HttpPost buildHttpJsonPost(String url, String json) {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			httpPost.setConfig(buildRequestConfig());
			httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
//			httpPost.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON_CHARSET);
			
			StringEntity se = new StringEntity(json,CONTENT_CHARSET);
			se.setContentType(CONTENT_TYPE_TEXT_JSON);
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
			httpPost.setEntity(se);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return httpPost;
	}

	/**
	 * 构建httpGet对象
	 * 
	 * @param url
	 * @param headers
	 * @return
	 * @throws URISyntaxException
	 */
	private static HttpGet buildHttpGet(String url, Map<String, String> params)
			throws URISyntaxException {
		HttpGet get = new HttpGet(buildGetUrl(url, params));
		get.setConfig(buildRequestConfig());
		return get;
	}

	/**
	 * build getUrl str
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private static String buildGetUrl(String url, Map<String, String> params) {
		StringBuffer uriStr = new StringBuffer(url);
		if (params != null) {
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				ps.add(new BasicNameValuePair(key, params.get(key)));
			}
			uriStr.append("?");
			uriStr.append(URLEncodedUtils.format(ps, UTF_8));
		}
		return uriStr.toString();
	}

	/**
	 * 设置HttpMethod通用配置
	 * 
	 * @param httpMethod
	 */
	private static void setCommonHttpMethod(HttpRequestBase httpMethod) {
		httpMethod.setHeader(HTTP.CONTENT_ENCODING, CONTENT_CHARSET);// setting
		// contextCoding
		// httpMethod.setHeader(HTTP.CHARSET_PARAM, CONTENT_CHARSET);
		// httpMethod.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON_CHARSET);
		// httpMethod.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_XML_CHARSET);
	}

	/**
	 * 设置成消息体的长度 setting MessageBody length
	 * 
	 * @param httpMethod
	 * @param he
	 */
	public static void setContentLength(HttpRequestBase httpMethod,
			HttpEntity he) {
		if (he == null) {
			return;
		}
		httpMethod.setHeader(HTTP.CONTENT_LEN,
				String.valueOf(he.getContentLength()));
	}

	/**
	 * 构建公用RequestConfig
	 * 
	 * @return
	 */
	private static RequestConfig buildRequestConfig() {
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(SO_TIMEOUT_MS)
				.setConnectTimeout(CONNECTION_TIMEOUT_MS).build();
		return requestConfig;
	}

	/**
	 * 强验证必须是200状态否则报异常
	 * 
	 * @param res
	 * @throws HttpException
	 */
	static void assertStatus(HttpResponse res) throws IOException {
		switch (res.getStatusLine().getStatusCode()) {
		case HttpStatus.SC_OK:
			// case HttpStatus.SC_CREATED:
			// case HttpStatus.SC_ACCEPTED:
			// case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
			// case HttpStatus.SC_NO_CONTENT:
			// case HttpStatus.SC_RESET_CONTENT:
			// case HttpStatus.SC_PARTIAL_CONTENT:
			// case HttpStatus.SC_MULTI_STATUS:
			break;
		default:
			throw new IOException("服务器响应状态异常,失败.");
		}
	}

	private HttpClientUtils() {
	}

	public static void main(String[] args) throws ClientProtocolException,
			IOException, URISyntaxException {
		System.out.println(simpleGetInvoke("http://www.baidu.com",
				new HashMap<String, String>()));
	}
}