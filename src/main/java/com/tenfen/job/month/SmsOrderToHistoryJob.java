package com.tenfen.job.month;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.util.LogUtil;
import com.tenfen.www.service.operation.sms.SmsOrderHistoryManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;

public class SmsOrderToHistoryJob {
	
	@Autowired
	private SmsOrderHistoryManager smsOrderHistoryManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	
	private static final int POOL_SIZE = 100;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
	
	public void execute() {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -1);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取前一个月第一天
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String dateString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date date = sdfSql.parse(dateString);
			java.sql.Date beforeTime = new java.sql.Date(date.getTime());
			
			//将beforeTime时间点之前的订单移入历史表
			smsOrderManager.batchExecuteMoveToHistory(beforeTime);
			//删除t_open_order数据
			smsOrderManager.deleteOrderBeforeDate(beforeTime);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
}
