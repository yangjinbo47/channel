/*
 * 文 件 名:  HttpUtil.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  h00101670
 * 修改时间:  2009-3-5
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.tenfen.util;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * <一句话功能简述> <功能详细描述>
 * 
 * @author h00101670
 * @version [版本号, 2009-3-5]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpUtil {
	/**
	 * http的header中的content-type属性的名字
	 */
	private static final String CONTENT_TYPE_NAME = "content-type";

	/**
	 * http的header中的content-type属性的内容
	 */
	private static final String CONTENT_TYPE_VALUE_XML_UTF_8 = "text/xml; charset=UTF-8";

	/**
	 * http的header中的content-type属性的传输类型
	 */
	private static final String TEXT_XML = "text/xml";

	private static final String CONTENT_TYPE_VALUE_XML_APP = "application/xml; charset=UTF-8";

	private static final String APP_XML = "application/xml";

	private static final String APPLICATION_JSON = "application/json";

	private static final String CONTENT_TYPE_VALUE_TEXT_JSON = "application/json; charset=utf-8";

	/**
	 * http的header中的content-type属性的字符编码
	 */
	private static final String UTF_8 = "UTF-8";

	/**
	 * HttpUtil类的实例
	 */
	private static HttpUtil instance = new HttpUtil();

	/**
	 * HttpClient实例
	 */
	private HttpClient httpClient = getHttpClient();

	/**
	 * HttpUtil类构造函数
	 */
	private HttpUtil() {

	}

	/**
	 * 单例模式返回唯一的HttpUtil的实例 在创建HttpUtil实例的时候创建HttpClient对象，并且设置HttpClient超时的属性。
	 * 
	 * 创建HttpClient实例，默认是SimpleHttpConnectionManager创建的，不支持多线程。
	 * 
	 * 使用多线程技术就是说，client可以在多个线程中被用来执行多个方法。
	 * 
	 * 每次调用HttpClient.executeMethod() 方法，都会去链接管理器申请一个连接实例，
	 * 申请成功这个链接实例被签出(checkout)，随之在链接使用完后必须归还管理器。
	 * 
	 * 管理器支持两个设置：
	 * 
	 * maxConnectionsPerHost 每个主机的最大并行链接数，默认为2
	 * 
	 * maxTotalConnections 客户端总并行链接最大数，默认为20
	 * 
	 * @return HttpUtil
	 */
	public static HttpUtil getInstance() {
		return instance;
	}

	/**
	 * 发生Get请求
	 * 
	 * @param url
	 *            请求url
	 * @return
	 * @throws PortalException
	 *             [参数说明]
	 * 
	 * @return String [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public String get(String url) {
		GetMethod httpMethod = new GetMethod(url);

		// 设置header信息，传输XML格式的
		httpMethod.setRequestHeader(CONTENT_TYPE_NAME,
				CONTENT_TYPE_VALUE_XML_UTF_8);

		// 获取响应报文
		String response = null;
		try {
			// 处理响应结果码
			int resultCode = httpClient.executeMethod(httpMethod);
			if (HttpStatus.SC_OK == resultCode) {
				byte[] resBody = httpMethod.getResponseBody();
				if (null != resBody && resBody.length > 0) {
					response = new String(resBody, UTF_8);
				} else {
					LogUtil.error("Http resultCode=200, but responseBody is empty.");
				}
			} else {
				LogUtil.error("Http resultCode=" + resultCode);
			}
		} catch (Exception ex) {
			LogUtil.error("send http request error.", ex);
		} finally {
			if (null != httpMethod) {
				httpMethod.releaseConnection();
			}
		}

		return response;
	}

	public String postApp(String url, String xml) {
		EntityEnclosingMethod httpMethod = new PostMethod(url);
		try {
			RequestEntity entity = new StringRequestEntity(xml, APP_XML, UTF_8);
			httpMethod.setRequestEntity(entity);
		} catch (UnsupportedEncodingException e) {
		}

		return sendApp(httpMethod);
	}

	private String sendApp(HttpMethod httpMethod) {

		String response = null;
		int resultCode = HttpStatus.SC_OK;

		try {

			httpMethod.setRequestHeader(CONTENT_TYPE_NAME,
					CONTENT_TYPE_VALUE_XML_APP);

			resultCode = httpClient.executeMethod(httpMethod);
			if (HttpStatus.SC_OK == resultCode) {
				byte[] resBody = httpMethod.getResponseBody();
				if (null != resBody && resBody.length > 0) {
					response = new String(resBody, UTF_8);
				}
			} else {
				response = "ResultCode = " + resultCode;
			}
		} catch (Exception ex) {
			response = ex.toString();
		} finally {
			if (null != httpMethod) {
				httpMethod.releaseConnection();
			}
		}
		return response;

	}

	/**
	 * 发送http请求，并返回响应的xml报文 <功能详细描述>
	 * 
	 * @param url
	 *            http请求url
	 * @param xml
	 *            请求xml报文
	 * @return
	 */
	public String post(String url, String xml) {
		EntityEnclosingMethod httpMethod = new PostMethod(url);

		// 发送含xml消息体的对象
		try {
			RequestEntity entity = new StringRequestEntity(xml, TEXT_XML, UTF_8);
			httpMethod.setRequestEntity(entity);
		} catch (UnsupportedEncodingException e) {
			LogUtil.error(e.getMessage(), e);
		}

		return send(httpMethod);
	}

	/**
	 * 发送http请求，并返回响应的xml报文 <功能详细描述>
	 * 
	 * @param url
	 *            http请求url
	 * @param xml
	 *            请求xml报文
	 * @return
	 */
	private String send(HttpMethod httpMethod) {
		// 获取响应报文
		String response = null;
		int resultCode = HttpStatus.SC_OK;
		try {
			// 设置header信息，传输XML格式的
			httpMethod.setRequestHeader(CONTENT_TYPE_NAME,
					CONTENT_TYPE_VALUE_XML_UTF_8);

			// 处理响应结果码
			resultCode = httpClient.executeMethod(httpMethod);
			if (HttpStatus.SC_OK == resultCode) {
				byte[] resBody = httpMethod.getResponseBody();
				if (null != resBody && resBody.length > 0) {
					response = new String(resBody, UTF_8);
				}
			} else {
				response = resultCode + "";
				LogUtil.error("Http response: "
						+ httpMethod.getResponseBodyAsString());
			}
		} catch (Exception ex) {
			response = ex.toString();
			LogUtil.error("send http request error!", ex);
		} finally {
			if (null != httpMethod) {
				httpMethod.releaseConnection();
			}
		}
		return response;
	}

	/**
	 * 发送http请求，并返回响应的xml报文 <功能详细描述>
	 * 
	 * @param url
	 *            http请求url
	 * @param xml
	 *            请求xml报文
	 * @return
	 */
	public String postJson(String strURL, String json) {
		try {
			URL url = new URL(strURL);// 创建连接
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // 设置请求方式
			connection.setRequestProperty("Accept", APPLICATION_JSON); // 设置接收数据的格式
			connection.setRequestProperty("Content-Type", CONTENT_TYPE_VALUE_TEXT_JSON); // 设置发送数据的格式
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
			out.append(json);
			out.flush();
			out.close();
			// 读取响应
			int length = (int) connection.getContentLength();// 获取长度
			InputStream is = connection.getInputStream();
			if (length != -1) {
				byte[] data = new byte[length];
				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					System.arraycopy(temp, 0, data, destPos, readLen);
					destPos += readLen;
				}
				String result = new String(data, "UTF-8"); // utf-8编码
				return result;
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return "error"; // 自定义错误信息
	}

	/**
	 * 构造Http客户端对象 <功能详细描述>
	 * 
	 * @return [参数说明]
	 * 
	 * @return HttpClient [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public HttpClient getHttpClient() {

		/**
		 * 链接的超时数,默认为5秒,此处要做成可配置
		 */
		final int CONNECTION_TIME_OUT = 5000;
		final int SOCKET_TIME_OUT = 5000;

		/**
		 * 每个主机的最大并行链接数，默认为10
		 */
		final int MAX_CONNECTIONS_PER_HOST = 10;

		/**
		 * 客户端总并行链接最大数，默认为50
		 */
		final int MAX_TOTAL_CONNECTIONS = 50;

		// 此处运用连接池技术。
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

		// 设定参数：与每个主机的最大连接数
		manager.getParams().setDefaultMaxConnectionsPerHost(
				MAX_CONNECTIONS_PER_HOST);

		// 设定参数：客户端的总连接数
		manager.getParams().setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);

		// 使用连接池技术创建HttpClient对象
		HttpClient httpClient = new HttpClient(manager);

		// 设置超时时间
		httpClient.getParams().setConnectionManagerTimeout(CONNECTION_TIME_OUT);// 从连接池中获取连接超时设置
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(CONNECTION_TIME_OUT);// 建立连接超时设置
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(SOCKET_TIME_OUT);// socket上没有数据流动超时设置

		return httpClient;
	}
}
