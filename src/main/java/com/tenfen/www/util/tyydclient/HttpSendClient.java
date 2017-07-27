package com.tenfen.www.util.tyydclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.util.EncodingUtil;

import com.tenfen.util.StringUtil;

/**
 * @description class
 * @author yejunwei
 * @date 2013年9月22日 下午10:16:40
 */
public class HttpSendClient {
	
	/**
	 * 采用何种方式对parm进行url编码
	 *
	 * */
	public String parmUrlEncode(NameValuePair[] nameValuePair, String charset) {
		return EncodingUtil.formUrlEncode(nameValuePair, charset);
	}
	
	 /**
     * 发送消息，这里要考虑：消息编码，消息签名，post方法
     *
     * @param url     接收URL
     * @param content 消息内容
     * @return 响应的值
     * @throws IOException 服务不可用
     */
    public HttpSendResponse executeHttpPost(HttpSendRequest request)throws Exception{
    	if(request == null)
    		throw new Exception("executeHttpPost param exception, request is null");
    	String queryString = "";
    	if(request.getContent() != null && request.getContent().size() >0){
	    	List<NameValuePair> list = new ArrayList<NameValuePair>();
	    	List<String> keys = new ArrayList<String>(request.getContent().keySet());
			Collections.sort(keys);
	    	for(String key : keys){
	    		String value = request.getContent().get(key);
	    		NameValuePair nv = new NameValuePair(key, value);
	    		list.add(nv);
	    	}
	    	NameValuePair digest = new NameValuePair("sign", request.getSign());
	    	list.add(digest);
	    	NameValuePair[] data = list.toArray(new NameValuePair[list.size()]);
	    	queryString= this.parmUrlEncode(data, request.getCharset());
    	}
    	
    	PostMethod httpMethod = new PostMethod(request.getUrl());
    	if(request.getHeads() != null && request.getHeads().size() > 0){
	    	for(String key: request.getHeads().keySet()){
	    		String value = request.getHeads().get(key);
	    		httpMethod.setRequestHeader(key, value);
	    	}
    	}
    	if(request.getStream() != null && request.getStream().length > 0){
    		ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(request.getStream());
    		httpMethod.setRequestEntity(requestEntity);
    	}else{
	    	//设置Request默认的header
	    	httpMethod.setRequestHeader("Content-type", "text/xml; charset=" + request.getCharset());
	    	httpMethod.setRequestHeader("Accept-Language", "zh-cn");
	    	httpMethod.setRequestHeader("Cache-Control", "no-cache");
	    	queryString = request.getContent().get("content");
	    	httpMethod.setRequestEntity(new StringRequestEntity(queryString,"text/xml" + request.getCharset(),  request.getCharset()));
    	}
    	
    	return this.execute_(httpMethod, request.getTimeout(), request.getCharset());
    }

    public HttpSendResponse executeHttpGet(HttpSendRequest request)throws Exception{
    	if(request == null)
    		throw new Exception("executeHttpGet param exception, request is null");
    	String queryString="";
    	if(request.getContent() != null && request.getContent().size() >0){
	    	List<NameValuePair> list = new ArrayList<NameValuePair>();
	    	List<String> keys = new ArrayList<String>(request.getContent().keySet());
			Collections.sort(keys);
	    	for(String key : keys){
	    		String value = request.getContent().get(key);
	    		if(value == null)
	    			continue;
	    		NameValuePair nv = new NameValuePair(key, value);
	    		list.add(nv);
	    	}
	    	NameValuePair digest = new NameValuePair("sign", request.getSign());
	    	list.add(digest);
	    	NameValuePair[] data = list.toArray(new NameValuePair[list.size()]);
	    	queryString= this.parmUrlEncode(data, request.getCharset());
    	}
    	
    	GetMethod httpMethod = new GetMethod(request.getUrl());
    	httpMethod.setRequestHeader("Content-type", "text/xml; charset=" + request.getCharset());
    	httpMethod.setRequestHeader("Accept-Language", "zh-cn");
    	httpMethod.setRequestHeader("Cache-Control", "no-cache");
    	if(request.getHeads() != null && request.getHeads().size() > 0){
	    	for(String key: request.getHeads().keySet()){
	    		String value = request.getHeads().get(key);
	    		httpMethod.setRequestHeader(key, value);
	    	}
    	}
		httpMethod.setQueryString(queryString);
		
    	return this.execute_(httpMethod, request.getTimeout(), request.getCharset());
    }

    private HttpSendResponse execute_(HttpMethod httpMethod, int timeout, String charset)throws Exception{
    	/**
         * 进行httpclient的详细参数配置，请参考  http://jakarta.apache.org/commons/httpclient/preference-api.html
         */
    	HttpSendResponse response = new HttpSendResponse();
    	HttpClient client = new HttpClient();     //HttpClient创建
        HttpClientParams clientParams = client.getParams();
        clientParams.setParameter("http.socket.timeout", timeout); //5秒socket等待数据
        clientParams.setParameter("http.connection.timeout", timeout); //5秒http connection建立超时
        clientParams.setParameter("http.connection-manager.timeout", new Long(timeout)); //5秒从http connection manager获取可用的Http connection超时
        clientParams.setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler()); //如果Http出错，三次重试
        //设置Response默认的字符集为GBK，防止对方没有返回“Content-type”的header造成解析中文出错
    	clientParams.setParameter("http.protocol.content-charset", charset);
		try {
			int status = client.executeMethod(httpMethod);
			response.setResponseStatus(status);
//			byte[] b = httpMethod.getResponseBody();
//			System.out.println(b.length);
			response.setResponseBody(httpMethod.getResponseBodyAsString());
			response.setResponseHeaders(headsToMap(httpMethod.getResponseHeaders()));
			
		} catch (IOException e) {
			httpMethod.releaseConnection();
			throw e;
		} finally {
			httpMethod.releaseConnection();
        }
		return response;
    }
    
    private Map<String, String> headsToMap(Header[] headers){
		Map<String, String> result = new HashMap<String, String>();
		if (null != headers) {
			for (Header header : headers) {
				result.put(header.getName(), header.getValue());
			}
		}
		return result;
	}
    
    protected void printResponse(HttpSendResponse response){
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
			System.out.println(replaceBlank(response.getResponseBody()));
			//System.out.println(response.getResponseBody());
		}
		System.out.println("");
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("");
	}
    
    public static String replaceBlank(String str) {
		if(StringUtil.isBlank(str))
			return null;
		String dest = "";
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(str);
		dest = m.replaceAll("");
		return dest;

	}
}

