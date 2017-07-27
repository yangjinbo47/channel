package com.tenfen.job.hour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.SftpClientUtil;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class FtpDataDailyJob {
	
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	
	public void execute() {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			//放置能力开放点播数据
			List<Integer> merchantIdList = Arrays.asList(new Integer[]{1,3,10,12,21,26,27,28,31,32,33,34});//点播商户
			
			Calendar calendar = Calendar.getInstance();
			//格式化时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfTxt = new SimpleDateFormat("yyyy-MM-dd_HH");
			//获取结束时间
			String endString = sdf.format(calendar.getTime()) + ":00:00";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			//获取当日时间区间
			String startString = sdf.format(calendar.getTime()) + ":00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String tySpaceDataLocalDir = systemProperty.getTySpaceDataLocalDir();
			fos = new FileOutputStream(tySpaceDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt", true);
			osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);
			
			for (Integer merchantId : merchantIdList) {
				List<TOpenOrder> orderList = openOrderManager.getOrderListByMerchantId(merchantId, start, end);
				for (TOpenOrder tOpenOrder : orderList) {
					StringBuffer sb = new StringBuffer();
					sb.append(tOpenOrder.getOutTradeNo()).append(",");
					sb.append(tOpenOrder.getSubject()).append(",");
					sb.append(tOpenOrder.getCreateTime()).append(",");
					sb.append(tOpenOrder.getFee()).append(",");
					sb.append(tOpenOrder.getStatus()).append(",");
					sb.append(tOpenOrder.getPayTime()).append(",");
					sb.append(tOpenOrder.getImsi()).append(",");
					sb.append(tOpenOrder.getSellerId()).append(",");
					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
					sb.append(tOpenSeller.getName()).append(",");
					sb.append(tOpenOrder.getProvince()).append(",");
					sb.append(tOpenOrder.getReduce()).append(System.getProperty("line.separator"));
					bw.write(sb.toString());
				}
			}
			
			bw.flush();
			bw.close();
			osw.close();
			fos.close();
			
			//放置空间包月数据
//			List<Integer> merchantPackageIdList = Arrays.asList(new Integer[]{13});//包月商户
//
//			String tySpacePackageDataLocalDir = systemProperty.getTySpacePackageDataLocalDir();
//			fos = new FileOutputStream(tySpacePackageDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt", true);
//			osw = new OutputStreamWriter(fos,"UTF-8");
//			bw = new BufferedWriter(osw);
//			
//			for (Integer merchantId : merchantPackageIdList) {
//				List<TOpenOrder> orderList = openOrderManager.getOrderListByMerchantId(merchantId, start, end);
//				for (TOpenOrder tOpenOrder : orderList) {
//					StringBuffer sb = new StringBuffer();
//					sb.append(tOpenOrder.getImsi()).append(",");
//					sb.append(tOpenOrder.getOutTradeNo()).append(",");
//					String subject = tOpenOrder.getSubject();
//					sb.append(subject.substring(0,subject.indexOf(","))).append(",");
//					sb.append(tOpenOrder.getCreateTime()).append(",");
//					sb.append(tOpenOrder.getFee()).append(",");
//					sb.append(tOpenOrder.getStatus()).append(",");
//					sb.append(tOpenOrder.getPayTime()).append(",");
//					sb.append(tOpenOrder.getImsi()).append(",");
//					sb.append(tOpenOrder.getSellerId()).append(",");
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					sb.append(tOpenSeller.getName()).append(",");
//					sb.append(tOpenOrder.getProvince()).append(",");
//					sb.append(tOpenOrder.getReduce()).append(System.getProperty("line.separator"));
//					bw.write(sb.toString());
//				}
//			}
//			
//			bw.flush();
//			bw.close();
//			osw.close();
//			fos.close();
			
			//放置联通在信数据数据
			List<Integer> unicomzxMerchantIdList = Arrays.asList(new Integer[]{7});//联通在信商户id

			String unicomDataZxLocalDir = systemProperty.getUnicomDataZxLocalDir();
			fos = new FileOutputStream(unicomDataZxLocalDir+File.separator+sdfTxt.format(new Date())+".txt", true);
			osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);
			
			for (Integer merchantId : unicomzxMerchantIdList) {
				List<TSmsOrder> orderList = smsOrderManager.getOrderListByMerchantId(merchantId, start, end);
				for (TSmsOrder tSmsOrder : orderList) {
					StringBuffer sb = new StringBuffer();
					sb.append(tSmsOrder.getOutTradeNo()).append(",");
					String subject = tSmsOrder.getSubject();
					sb.append(subject).append(",");
					sb.append(tSmsOrder.getCreateTime()).append(",");
					sb.append(tSmsOrder.getFee()).append(",");
					sb.append(tSmsOrder.getStatus()).append(",");
					sb.append(tSmsOrder.getPayTime()).append(",");
					sb.append(tSmsOrder.getPayPhone()).append(",");
					sb.append(tSmsOrder.getSellerId()).append(",");
					TSmsSeller tSmsSeller = smsSellerManager.get(tSmsOrder.getSellerId());
					sb.append(tSmsSeller.getName()).append(",");
					sb.append(tSmsOrder.getProvince()).append(",");
					sb.append(tSmsOrder.getReduce()).append(System.getProperty("line.separator"));
					bw.write(sb.toString());
				}
			}
			
			bw.flush();
			bw.close();
			osw.close();
			fos.close();
			
			SftpClientUtil sftp = new SftpClientUtil(systemProperty.getTySpaceDataIp(), Integer.parseInt(systemProperty.getTySpaceDataPort()), systemProperty.getTySpaceDataUserName(), systemProperty.getTySpaceDataPassword());
			sftp.connect();
			//放置空间点播数据到北京ftp
			sftp.upload(systemProperty.getTySpaceDataRemoteDir(), tySpaceDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt");
			//放置空间包月数据到北京ftp
//			sftp.upload(systemProperty.getTySpacePackageDataRemoteDir(), tySpacePackageDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt");
			//放置联通在信数据到北京ftp
			sftp.upload(systemProperty.getUnicomDataZxRemoteDir(), unicomDataZxLocalDir+File.separator+sdfTxt.format(new Date())+".txt");
			sftp.disconnect();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	
}
