package com.tenfen.job.hour;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.entity.operation.open.TOpenOrderConversionrate;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.mongoEntity.MongoTOpenOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.open.OpenOrderConversionRateManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

public class OpenOrderConversionRateJob {

	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderConversionRateManager openOrderConversionRateManager;
	
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
			List<TOpenSeller> sellerList = openSellerManager.findAllOpenSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TOpenSeller tOpenSeller : sellerList) {
//				List<TOpenOrder> orders = openOrderManager.getOrderList(tOpenSeller.getId(), null, start, end);
				List<MongoTOpenOrder> orders = openOrderManager.getOrderListFromMongo(tOpenSeller.getId(), null, start, end);
				
				int orderReq = orders.size();
				int mr = 0;
				int fail = 0;
				for (MongoTOpenOrder tOpenOrder : orders) {
					if ("3".equals(tOpenOrder.getStatus())) {
						mr++;
					} else if ("4".equals(tOpenOrder.getStatus())) {
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
				
				TOpenOrderConversionrate tOpenOrderConversionrate = openOrderConversionRateManager.getEntity(year, month, day, hour, tOpenSeller.getId());
				if (Utils.isEmpty(tOpenOrderConversionrate)) {
					tOpenOrderConversionrate = new TOpenOrderConversionrate();
					tOpenOrderConversionrate.setYear(year);
					tOpenOrderConversionrate.setMonth(month);
					tOpenOrderConversionrate.setDay(day);
					tOpenOrderConversionrate.setHour(hour);
					tOpenOrderConversionrate.setOrderReq(orderReq);
					tOpenOrderConversionrate.setMo(mo);
					tOpenOrderConversionrate.setMr(mr);
					tOpenOrderConversionrate.setRate(f*100);
					tOpenOrderConversionrate.setRateReq(reqf*100);
					tOpenOrderConversionrate.setSellerId(tOpenSeller.getId());
					openOrderConversionRateManager.save(tOpenOrderConversionrate);
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
