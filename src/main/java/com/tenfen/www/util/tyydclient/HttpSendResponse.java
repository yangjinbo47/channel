package com.tenfen.www.util.tyydclient;

import java.io.Serializable;
import java.util.Map;

/**
 * @description class
 * @author yejunwei
 * @date 2013年9月27日 下午2:54:53
 */
public class HttpSendResponse implements Serializable{

	private static final long serialVersionUID = -8606115722936996719L;
	
	private Integer responseStatus;

	private Map<String, String> responseHeaders;
	
	private String responseBody;
	
	public Integer getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}

	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(Map<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	
	
}

