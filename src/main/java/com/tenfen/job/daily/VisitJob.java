package com.tenfen.job.daily;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.system.TVisitLog;
import com.tenfen.entity.system.TVisitLogTmp;
import com.tenfen.util.LogUtil;
import com.tenfen.www.service.system.VisitLogManager;
import com.tenfen.www.service.system.VisitLogTmpManager;

public class VisitJob {

	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private VisitLogManager visitLogManager;
	@Autowired
	private VisitLogTmpManager visitLogTmpManager;
	
	private static final int POOL_SIZE = 100;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池

	private Map<String, TVisitLog> map = new HashMap<String, TVisitLog>();
	
	public void execute() {
		try {
			if (!map.isEmpty()) {
				map.clear();
			}
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			//获取当日时间区间
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			int pageSize = 1000;
			long count = visitLogTmpManager.getCount(start, end);
			int pageCount = Integer.valueOf(String.valueOf(count)) / pageSize + 1;
			
			for (int i = 1; i <= pageCount; i++) {
				Page<TVisitLogTmp> logTmpPage = new Page<TVisitLogTmp>();
				//设置默认排序方式
				logTmpPage.setPageSize(pageSize);
				logTmpPage.setPageNo(i);
				if (!logTmpPage.isOrderBySetted()) {
					logTmpPage.setOrderBy("id");
					logTmpPage.setOrder(Page.ASC);
				}
				logTmpPage = visitLogTmpManager.getVisitLogTmpPageByProperty(logTmpPage, start, end);
				
				List<TVisitLogTmp> list = logTmpPage.getResult();
				for (TVisitLogTmp tVisitLogTmp : list) {
					String imsi = tVisitLogTmp.getImsi();
					String phoneNum = tVisitLogTmp.getPhoneNum();
					String clientVersion = tVisitLogTmp.getClientVersion();
					String province = tVisitLogTmp.getProvince();
					String ua = tVisitLogTmp.getUserAgent();
					Date visitTime = tVisitLogTmp.getVisitTime();
					
					TVisitLog tVisitLog = new TVisitLog();
					tVisitLog.setImsi(imsi);
					tVisitLog.setPhoneNum(phoneNum);
					tVisitLog.setClientVersion(clientVersion);
					tVisitLog.setProvince(province);
					tVisitLog.setUserAgent(ua);
					tVisitLog.setVisitTime(visitTime);
					
					map.put(imsi+"_"+clientVersion, tVisitLog);
				}
			}
			
			for (String key : map.keySet()) {
				TVisitLog tVisitLog = map.get(key);
				VisitLogThread thread = new VisitLogThread(visitLogManager, tVisitLog);
				exe.execute(thread);
			}
			
			
			//删除三天前的临时表日志
			calendar.add(Calendar.DATE, -3);
			//获取当日时间区间
			SimpleDateFormat sdfSqlBefore = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startBeforeString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date startBeforeDate = sdfSqlBefore.parse(startBeforeString);
			java.sql.Date startBefore = new java.sql.Date(startBeforeDate.getTime());
			
			String endBeforeString = sdf.format(calendar.getTime()) + " 23:59:59";
			Date endBeforeDate = sdfSqlBefore.parse(endBeforeString);
			java.sql.Date endBefore = new java.sql.Date(endBeforeDate.getTime());
			visitLogTmpManager.delete(startBefore, endBefore);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public class VisitLogThread implements Runnable {

		private VisitLogManager visitLogManager;
		private TVisitLog tVisitLog;
		
		public VisitLogThread(VisitLogManager visitLogManager, TVisitLog tVisitLog) {
			this.visitLogManager = visitLogManager;
			this.tVisitLog = tVisitLog;
		}
		
		@Override
		public void run() {
			visitLogManager.save(tVisitLog);
		}
	}
	
}
