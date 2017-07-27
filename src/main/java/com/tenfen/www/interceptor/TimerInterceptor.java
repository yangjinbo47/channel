package com.tenfen.www.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.tenfen.util.LogUtil;

@SuppressWarnings("serial")
public class TimerInterceptor extends AbstractInterceptor {

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {

		long startTime = System.currentTimeMillis();
		String result = invocation.invoke();
		long executionTime = System.currentTimeMillis() - startTime;

		StringBuffer message = new StringBuffer(100);
		message.append("TimerInterceptor [");
		String namespace = invocation.getProxy().getNamespace();
		if ((namespace != null) && (namespace.trim().length() > 0)) {
			message.append(namespace);
		}
		message.append("/");
		message.append(invocation.getProxy().getActionName());
		message.append("!");
		message.append(invocation.getProxy().getMethod());
		message.append("] Cost ").append(executionTime).append(" ms.");

		LogUtil.log(message.toString());

		return result;
	}

}
