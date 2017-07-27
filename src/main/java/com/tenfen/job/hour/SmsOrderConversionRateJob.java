package com.tenfen.job.hour;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsOrderConversionrate;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.sms.SmsOrderConversionRateManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class SmsOrderConversionRateJob {

	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderConversionRateManager smsOrderConversionRateManager;
	
	public void execute() {
		try {
			Calendar calendar = Calendar.getInstance();
			Integer year = calendar.get(Calendar.YEAR);
			Integer month = calendar.get(Calendar.MONTH)+1;
			Integer day = calendar.get(Calendar.DAY_OF_MONTH);
			Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
			//当前时间，取记录时开始时间从-1个小时算起
			if (hour == 0) {
				calendar.add(Calendar.DATE, -1);
				hour = 23;
			} else {
				hour = hour - 1;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取时间区间
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = sdf.format(calendar.getTime()) + " "+hour+":00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String endString = sdf.format(calendar.getTime()) + " "+hour+":59:59";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			//入库hour将减掉的时间加回来
			hour = hour == 23 ? 0 : hour+1;
			List<TSmsSeller> sellerList = smsSellerManager.findAllSmsSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TSmsSeller tSmsSeller : sellerList) {
				List<TSmsOrder> orders = smsOrderManager.getOrderList(tSmsSeller.getId(), null, start, end);
				
				int orderReq = orders.size();
				int mr = 0;
				int fail = 0;
				for (TSmsOrder tSmsOrder : orders) {
					if ("3".equals(tSmsOrder.getStatus())) {
						mr++;
					} else if ("4".equals(tSmsOrder.getStatus())) {
						fail++;
					}
				}
				int mo = mr+fail;
				
				//转化率
				float f = 0;
				if (mr == 0) {
					f = 0;
				} else {
					f = (float)mr/mo;
				}
				if (f != 0) {
					f = (float)(Math.round(f*1000))/1000;
				}
				//请求转化率
				float reqf = 0;
				if (mr == 0) {
					reqf = 0;
				} else {
					reqf = (float)mr/orderReq;
				}
				if (reqf != 0) {
					reqf = (float)(Math.round(reqf*1000))/1000;
				}
				
				TSmsOrderConversionrate tSmsOrderConversionrate = smsOrderConversionRateManager.getEntity(year, month, day, hour, tSmsSeller.getId());
				if (Utils.isEmpty(tSmsOrderConversionrate)) {
					tSmsOrderConversionrate = new TSmsOrderConversionrate();
					tSmsOrderConversionrate.setYear(year);
					tSmsOrderConversionrate.setMonth(month);
					tSmsOrderConversionrate.setDay(day);
					tSmsOrderConversionrate.setHour(hour);
					tSmsOrderConversionrate.setOrderReq(orderReq);
					tSmsOrderConversionrate.setMo(mo);
					tSmsOrderConversionrate.setMr(mr);
					tSmsOrderConversionrate.setRate(f*100);
					tSmsOrderConversionrate.setRateReq(reqf*100);
					tSmsOrderConversionrate.setSellerId(tSmsSeller.getId());
					smsOrderConversionRateManager.save(tSmsOrderConversionrate);
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
