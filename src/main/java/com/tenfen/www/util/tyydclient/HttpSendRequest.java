package com.tenfen.www.util.tyydclient;

import java.io.Serializable;
import java.util.Map;

/**
 * @description class
 * @author yejunwei
 * @date 2013年9月27日 下午1:51:58
 */
public class HttpSendRequest implements Serializable{

	private static final long serialVersionUID = 2525092903576788036L;

	private String url;
	
	private Map<String, String> heads;
	
	private Map<String, String> content;
	
	private byte[] stream;
	
	private String sign;
	
	private int timeout = 5000;
	
	private String charset;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getHeads() {
		return heads;
	}

	public void setHeads(Map<String, String> heads) {
		this.heads = heads;
	}

	public Map<String, String> getContent() {
		return content;
	}

	public void setContent(Map<String, String> content) {
		this.content = content;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public byte[] getStream() {
		return stream;
	}

	public void setStream(byte[] stream) {
		this.stream = stream;
	}
	
}

