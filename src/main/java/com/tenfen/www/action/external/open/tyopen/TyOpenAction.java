package com.tenfen.www.action.external.open.tyopen;

import java.util.Map;

import com.tenfen.util.LogUtil;
import com.tenfen.www.action.SimpleActionSupport;

public class TyOpenAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	
	/**
	 * 天翼能力开放-回调地址
	 */
	public void callBack() {
		LogUtil.log("tyopen callBack visiting");
		Map<String, String[]> map = request.getParameterMap();
		for (String key : map.keySet()) {
			String[] value = map.get(key);
			for (String string : value) {
				LogUtil.log("tyopen callBack param key:"+key+" value:"+string);
			}
		}
	}
}
