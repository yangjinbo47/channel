package com.tenfen.util;

import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class LogUtil {

	private static final Logger logger = Logger.getLogger(LogUtil.class);

	static {
//		logger.setLevel(org.apache.log4j.Level.INFO);
	}

	public static void log(String info) {
		logger.info(info);
	}

	public static void log(String info, Throwable ex) {
		logger.info(info, ex);
	}

	public static void log(String info, Level level, Throwable ex) {
		log(info, ex);
	}

	public static void log(String userName, String info, Level level, Throwable ex) {
		if (!StringUtils.isEmpty(userName)) {
			log("[" + userName + "]" + info, ex);
		} else {
			log(info, ex);
		}
	}

	public static void log(String userName, String info) {
		if (!StringUtils.isEmpty(userName)) {
			log("[" + userName + "]" + info);
		} else {
			log(info);
		}
	}
	
	public static void error(String userName, String info, Throwable ex) {
		if (!StringUtils.isEmpty(userName)) {
			logger.error("[" + userName + "]" + info, ex);
		} else {
			logger.error(info, ex);
		}
	}
	
	public static void error(String info) {
		logger.error(info);
	}
	
	public static void error(String info, Throwable ex) {
		logger.error(info, ex);
	}
	
}
