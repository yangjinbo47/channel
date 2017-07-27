package com.tenfen.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

	public static String getCurrentTime(){
		SimpleDateFormat format = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		format.applyPattern("yyyyMMddHHmmss"); 
		String timeStr = format.format(new Date()); 
        return timeStr;
	}
	
	public static String getCurrentTimestamp(){
		SimpleDateFormat format = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		format.applyPattern("yyyy-MM-dd HH:mm:ss"); 
		String timeStr = format.format(new Date()); 
        return timeStr;
	}
	
	/****
	 * 根据格式获取时间
	 * @param formatStr 如：“yyyy-MM-dd HH:mm:ss ”
	 * @return
	 */
	public static String getCurrentTimestamp(String formatStr){
		SimpleDateFormat format = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		format.applyPattern(formatStr); 
		String timeStr = format.format(new Date()); 
        return timeStr;
	}
	
	/**
	 * 将Timestamp转换成指定的日期格式
	 * @param fomatStr 如(yyyy年MM月dd日 HH:mm)
	 * @param dateSource 源日期(Timestamp)
	 * @return
	 */
	public static String getTimeByFomat(String fomatStr,Timestamp dateSource){
		if(dateSource==null){//容错处理
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		format.applyPattern(fomatStr);
		return format.format(dateSource);
	}
	
	/**
	 * 将Date转换成指定的日期格式
	 * @param fomatStr 如(yyyy年MM月dd日 HH:mm)
	 * @param dateSource 源日期(Date)
	 * @return
	 */
	public static String getDateByFomat(String fomatStr,Date date){
		if(date==null){//容错处理
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		format.applyPattern(fomatStr);
		return format.format(date);
	}
	
}
