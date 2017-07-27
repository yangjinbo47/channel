package com.tenfen.www.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.tenfen.util.LogUtil;

public class ExceptionInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1001905602054461217L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {

		try {
			return invocation.invoke();

		} catch (Exception ex) {

			ActionSupport action = (ActionSupport) invocation.getAction();
			String methodName = invocation.getProxy().getMethod();

			LogUtil.log(action.getClass().getCanonicalName() + "@" + methodName + " 执行发生错误", ex);
//			ex.printStackTrace();

			return null;
			
		}

	}

}
