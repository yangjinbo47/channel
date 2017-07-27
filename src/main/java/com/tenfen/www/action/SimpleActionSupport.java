package com.tenfen.www.action;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.util.LogUtil;
import com.tenfen.util.RequestUtils;
import com.tenfen.util.Utils;

public abstract class SimpleActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	private static final long serialVersionUID = 6120462616918718977L;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	@Autowired
	private CacheFactory cacheFactory;

	public SimpleActionSupport() {
	}
	
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	protected void setRequestAttribute(String key, Object object) {
		if (request != null && key != null) {
			request.setAttribute(key, object);
		}
	}

	protected Object getRequestAttribute(String key, Object object) {
		return request == null ? null : request.getAttribute(key);
	}

	protected void setSessionAttribute(String key, Object object) {
//		if (request != null && key != null) {
//			HttpSession session = request.getSession();
//			session.setAttribute(key, object);
//		}
		String cookieReqId = getParamFromCookie("cookie_req_id");
		key = cookieReqId+"_"+key;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		if (mc != null && key != null) {
			mc.setCache(key, object, CacheFactory.DAY);
		}
	}

	protected Object getSessionAttribute(String key) {
//		HttpSession session = request.getSession();
//		return request == null ? null : session.getAttribute(key);
		String cookieReqId = getParamFromCookie("cookie_req_id");
		key = cookieReqId+"_"+key;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		return mc == null ? null : mc.getCache(key);
	}
	
	protected void removeSessionAttribute(String key) {
//		request.getSession().removeAttribute(key);
		String cookieReqId = getParamFromCookie("cookie_req_id");
		key = cookieReqId+"_"+key;
		ICacheClient mc = cacheFactory.getCommonCacheClient();
		mc.deleteCache(key);
	}
	
//	protected void setMemcacheAttribute(String key, Object object) {
//		HttpSession session = request.getSession();
//		String sessionId = session.getId();
//		ICacheClient mc = cacheFactory.getCommonCacheClient();
//		if (mc != null && key != null) {
//			mc.setCache(sessionId+key, object, CacheFactory.DAY);
//		}
//	}
//	
//	protected Object getMemcacheAttribute(String key) {
//		HttpSession session = request.getSession();
//		String sessionId = session.getId();
//		ICacheClient mc = cacheFactory.getCommonCacheClient();
//		return mc == null ? null : mc.getCache(sessionId+key);
//	}

	protected int getIntParam(String name) {
		return RequestUtils.getIntParameter(getRequest(), name, -1);
	}

	protected int getIntParam(String name, int defaultVal) {
		return RequestUtils.getIntParameter(getRequest(), name, defaultVal);
	}

	protected String getStringParam(String name) {
		return RequestUtils.getStringParameter(getRequest(), name, null);
	}

	protected String getStringParam(String name, String defaultVal) {
		return RequestUtils.getStringParameter(getRequest(), name, defaultVal);
	}

	public String getParamFromCookie(String key) {
		if (!Utils.isEmpty(request)) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : request.getCookies()) {
					if (cookie.getName().equals(key)) {
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}
	
	public String removeCookie(String key){
		try {
			Cookie cookie = new Cookie(key, null); 
			cookie.setMaxAge(-1);
			response.addCookie(cookie); 
		} catch (Exception e) {
			LogUtil.error(e.getMessage(),e);
		}
		return null;
	}
}
