package com.tenfen.job.daily;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.operation.SmsDailyBean;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsMailer;
import com.tenfen.entity.operation.sms.TSmsMailgroup;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.SendMailUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsMailManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class SmsDailyJob {
	
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private SmsMailManager smsMailManager;
	
	//发送邮件
	private final String mailLoginName = "yang.jinbo@tenfen.com";
	private final String mailLoginPwd = "yang19860202";
	private final String mailSmtp = "smtp.exmail.qq.com";
	
	//from mongodb mapreduce 高效模式，防止内存溢出
//	public void execute() {
//		try {
//			boolean haveData = false;//是否有数据，有数据则发送邮件
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.DATE, -1);
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			//获取当日时间区间
//			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
//			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
//			Date startDate = sdfSql.parse(startString);
//			java.sql.Date start = new java.sql.Date(startDate.getTime());
//			
//			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
//			Date endDate = sdfSql.parse(endString);
//			java.sql.Date end = new java.sql.Date(endDate.getTime());
//			
//			Map<Integer, List<SmsDailyBean>> map = new HashMap<Integer, List<SmsDailyBean>>();
//			List<TSmsSeller> smsSellerList = smsSellerManager.findAllSmsSellerList(Constants.USER_TYPE.ALL.getValue());
//			for (TSmsSeller tSmsSeller : smsSellerList) {
//				List<SmsDailyBean> smsDailyBeans = new ArrayList<SmsDailyBean>();//app统计数据
//				SmsDailyBean smsDailyBean = null;
//				int sellerId = tSmsSeller.getId();
//				String sellerName = tSmsSeller.getName();
//				
//				Map<Integer, String> noPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
//				Map<Integer, String> succPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
//				Map<Integer, String> failPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
//				Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
//				allStatusMap.putAll(noPayMap);
//				allStatusMap.putAll(succPayMap);
//				allStatusMap.putAll(failPayMap);
//				Map<Integer, String> succPayReduceMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
//				
//				for (Integer appId : allStatusMap.keySet()) {
//					haveData = true;
//					Integer noPayInt = null;
//					if (noPayMap.size() == 0) {
//						noPayInt = 0;
//					} else {
//						JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
//						noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
//						if (noPayInt == null) {
//							noPayInt = 0;
//						}
//					}
//					Integer failInt = null;
//					if (failPayMap.size() == 0) {
//						failInt = 0;
//					} else {
//						JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
//						failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
//						if (failInt == null) {
//							failInt = 0;
//						}
//					}
//					Integer succInt = null;
//					Integer feeInt = null;
//					if (succPayMap.size() == 0) {
//						succInt = 0;
//						feeInt = 0;
//					} else {
//						JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
//						succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
//						if (succInt == null) {
//							succInt = 0;
//						}
//						feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
//						if (feeInt == null) {
//							feeInt = 0;
//						}
//						feeInt = feeInt/100;//fee转化成单位元
//					}
//					Integer succReduceInt = null;
//					Integer feeReduceInt = null;
//					if (succPayReduceMap.size() == 0) {
//						succReduceInt = 0;
//						feeReduceInt = 0;
//					} else {
//						JSONObject succPayJson = JSONObject.parseObject(succPayReduceMap.get(appId));
//						succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数（扣量后）
//						if (succReduceInt == null) {
//							succReduceInt = 0;
//						}
//						feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额（扣量后）
//						if (feeReduceInt == null) {
//							feeReduceInt = 0;
//						}
//						feeReduceInt = feeReduceInt/100;//fee转化成单位元
//					}
//					
//					Integer orderReqInt = noPayInt+failInt+succInt;
//					Long users_num = smsOrderManager.mapReduceUserCount(sellerId, appId, start, end);
//					Long users_succ_num = smsOrderManager.mapReduceSuccUserCount(sellerId, appId, start, end);
//					
//					DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//					//mr/mo转化率
//					float f = 0;
//					if (succInt == 0) {
//						f = 0;
//					} else {
//						f = (float)succInt/(succInt+failInt);
//					}
//					if (f != 0) {
//						f = (float)(Math.round(f*1000))/1000;
//					}
//					String fString = "0%";
//					if (f == 0) {
//						fString = "0%";
//					} else {
//						fString = df.format(f*100) + "%";//返回的是String类型的
//					}
//					//mr/req请求转化率
//					float reqf = 0;
//					if (succInt == 0) {
//						reqf = 0;
//					} else {
//						reqf = (float)succInt/orderReqInt;
//					}
//					if (reqf != 0) {
//						reqf = (float)(Math.round(reqf*1000))/1000;
//					}
//					String reqfString = "0%";
//					if (reqf == 0) {
//						reqfString = "0%";
//					} else {
//						reqfString = df.format(reqf*100) + "%";//返回的是String类型的
//					}
//					
//					TSmsApp tSmsApp = smsAppManager.get(appId);
//					String appName = tSmsApp.getName();
//					
//					smsDailyBean = new SmsDailyBean();
//					smsDailyBean.setSellerId(sellerId);
//					smsDailyBean.setAppId(appId);
//					smsDailyBean.setSellerName(sellerName);
//					smsDailyBean.setAppName(appName);
//					smsDailyBean.setOrderReq(orderReqInt);
//					smsDailyBean.setSucc(succInt);
//					smsDailyBean.setSuccReduce(succReduceInt);
//					smsDailyBean.setFail(failInt);
//					smsDailyBean.setNoPay(noPayInt);
//					smsDailyBean.setFee(feeInt);
//					smsDailyBean.setFeeReduce(feeReduceInt);
//					smsDailyBean.setUserNum(users_num);
//					smsDailyBean.setUserSuccNum(users_succ_num);
//					smsDailyBean.setRate(fString);
//					smsDailyBean.setReqRate(reqfString);
//					smsDailyBeans.add(smsDailyBean);
//				}//end for app map
//				map.put(sellerId, smsDailyBeans);
//			}//end sellerList
//			
//			if (haveData) {
//				//查询邮件组
//				List<TSmsMailgroup> groupList = smsMailManager.getGroupAll();
//				for (TSmsMailgroup tSmsMailgroup : groupList) {
//					StringBuffer sb = new StringBuffer();
//					sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
//					sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"20%\">渠道名</td><td width=\"14%\">应用名</td><td width=\"6%\">下单数</td><td width=\"6%\">成功数</td><td width=\"6%\">成功数(扣后)</td><td width=\"6%\">失败数</td><td width=\"6%\">未支付</td><td width=\"6%\">金额</td><td width=\"6%\">金额(扣后)</td><td width=\"6%\">用户数</td><td width=\"6%\">成功用户数</td><td width=\"6%\">MR/MO转化率</td><td width=\"6%\">MR/REQ转化率</td></tr>");
//					
//					//查询邮件组内关联渠道
//					List<TSmsSeller> sellerList = tSmsMailgroup.getSellerList();
//					for (TSmsSeller tSmsSeller : sellerList) {
//						List<SmsDailyBean> dataList = map.get(tSmsSeller.getId());//渠道下的数据组
//						for (int i = 0; i < dataList.size(); i++) {
//							SmsDailyBean data = dataList.get(i);
//							if (i == 0) {
//								sb.append("<tr><td rowspan=\""+dataList.size()+"\">"+data.getSellerName()+"</td><td>"+data.getAppName()+"</td><td>"+data.getOrderReq()+"</td><td>"+data.getSucc()+"</td><td>"+data.getSuccReduce()+"</td><td>"+data.getFail()+"</td><td>"+data.getNoPay()+"</td><td>"+data.getFee()+"</td><td>"+data.getFeeReduce()+"</td><td>"+data.getUserNum()+"</td><td>"+data.getUserSuccNum()+"</td><td>"+data.getRate()+"</td><td>"+data.getReqRate()+"</td></tr>");
//							} else {
//								sb.append("<tr><td>"+data.getAppName()+"</td><td>"+data.getOrderReq()+"</td><td>"+data.getSucc()+"</td><td>"+data.getSuccReduce()+"</td><td>"+data.getFail()+"</td><td>"+data.getNoPay()+"</td><td>"+data.getFee()+"</td><td>"+data.getFeeReduce()+"</td><td>"+data.getUserNum()+"</td><td>"+data.getUserSuccNum()+"</td><td>"+data.getRate()+"</td><td>"+data.getReqRate()+"</td></tr>");
//							}
//						}
//					}
//					sb.append("</table><br/><br/></body>");
//					//查询邮件组关联人
//					List<TSmsMailer> mailers = tSmsMailgroup.getMailerList();
//					if (mailers.size() > 0) {					
//						String[] mailToList = new String[mailers.size()];
//						for (int i = 0; i < mailers.size(); i++) {
//							mailToList[i] = mailers.get(i).getEmail();
//						}
//						//发送邮件
//						String mailTitle = sdf.format(calendar.getTime())+"短代日报("+tSmsMailgroup.getName()+")";
//						SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
//					}
//				}
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	public void execute() {
		try {
			boolean haveData = false;//是否有数据，有数据则发送邮件
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
			
			Map<Integer, List<SmsDailyBean>> map = new HashMap<Integer, List<SmsDailyBean>>();
			List<TSmsSeller> smsSellerList = smsSellerManager.findAllSmsSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TSmsSeller tSmsSeller : smsSellerList) {
				List<SmsDailyBean> smsDailyBeans = new ArrayList<SmsDailyBean>();//app统计数据
				SmsDailyBean smsDailyBean = null;
				int sellerId = tSmsSeller.getId();
				String sellerName = tSmsSeller.getName();
				
				Map<Integer, String> noPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
				Map<Integer, String> succPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
				Map<Integer, String> failPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
				Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
				allStatusMap.putAll(noPayMap);
				allStatusMap.putAll(succPayMap);
				allStatusMap.putAll(failPayMap);
//				Map<Integer, String> succPayReduceMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
				
				for (Integer appId : allStatusMap.keySet()) {
					haveData = true;
					Integer noPayInt = null;
					Integer noPayUserInt = null;
					if (noPayMap.size() == 0) {
						noPayInt = 0;
						noPayUserInt = 0;
					} else {
						JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
						noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
						if (noPayInt == null) {
							noPayInt = 0;
						}
						noPayUserInt = noPayJson == null ? 0 : noPayJson.getInteger("user");//未支付用户数
						if (noPayUserInt == null) {
							noPayUserInt = 0;
						}
					}
					Integer failInt = null;
					Integer failUserInt = null;
					if (failPayMap.size() == 0) {
						failInt = 0;
						failUserInt = 0;
					} else {
						JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
						failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
						if (failInt == null) {
							failInt = 0;
						}
						failUserInt = failPayJson == null ? 0 : failPayJson.getInteger("user");//失败用户数
						if (failUserInt == null) {
							failUserInt = 0;
						}
					}
					Integer succInt = null;
					Integer succUserInt = null;
					Integer feeInt = null;
					Integer succReduceInt = null;
					Integer feeReduceInt = null;
					if (succPayMap.size() == 0) {
						succInt = 0;
						succUserInt = 0;
						feeInt = 0;
						succReduceInt = 0;
						feeReduceInt = 0;
					} else {
						JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
						succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
						if (succInt == null) {
							succInt = 0;
						}
						succUserInt = succPayJson == null ? 0 : succPayJson.getInteger("user");//成功用户数
						if (succUserInt == null) {
							succUserInt = 0;
						}
						feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
						if (feeInt == null) {
							feeInt = 0;
						}
						succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("countReduce");//成功支付请求数(扣)
						if (succReduceInt == null) {
							succReduceInt = 0;
						}
						feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("feeReduce");//成功计费金额
						if (feeReduceInt == null) {
							feeReduceInt = 0;
						}
						feeInt = feeInt/100;//fee转化成单位元
						feeReduceInt = feeReduceInt/100;
					}
					
					Integer orderReqInt = noPayInt+failInt+succInt;
//					Long users_num = openOrderManager.mapReduceUserCount(sellerId, appId, start, end);
//					Long users_succ_num = openOrderManager.mapReduceSuccUserCount(sellerId, appId, start, end);
					Integer users_num = noPayUserInt + failUserInt + succUserInt;
					Integer users_succ_num = succUserInt;
					
					DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
					//mr/mo转化率
					float f = 0;
					if (succInt == 0) {
						f = 0;
					} else {
						f = (float)succInt/(succInt+failInt);
					}
					if (f != 0) {
						f = (float)(Math.round(f*1000))/1000;
					}
					String fString = "0%";
					if (f == 0) {
						fString = "0%";
					} else {
						fString = df.format(f*100) + "%";//返回的是String类型的
					}
					//mr/req请求转化率
					float reqf = 0;
					if (succInt == 0) {
						reqf = 0;
					} else {
						reqf = (float)succInt/orderReqInt;
					}
					if (reqf != 0) {
						reqf = (float)(Math.round(reqf*1000))/1000;
					}
					String reqfString = "0%";
					if (reqf == 0) {
						reqfString = "0%";
					} else {
						reqfString = df.format(reqf*100) + "%";//返回的是String类型的
					}
					
					TSmsApp tSmsApp = smsAppManager.getSmsAppByProperty("id", appId);
					if (!Utils.isEmpty(tSmsApp)) {
						String appName = tSmsApp.getName();
						
						smsDailyBean = new SmsDailyBean();
						smsDailyBean.setSellerId(sellerId);
						smsDailyBean.setAppId(appId);
						smsDailyBean.setSellerName(sellerName);
						smsDailyBean.setAppName(appName);
						smsDailyBean.setOrderReq(orderReqInt);
						smsDailyBean.setSucc(succInt);
						smsDailyBean.setSuccReduce(succReduceInt);
						smsDailyBean.setFail(failInt);
						smsDailyBean.setNoPay(noPayInt);
						smsDailyBean.setFee(feeInt);
						smsDailyBean.setFeeReduce(feeReduceInt);
						smsDailyBean.setUserNum(users_num);
						smsDailyBean.setUserSuccNum(users_succ_num);
						smsDailyBean.setRate(fString);
						smsDailyBean.setReqRate(reqfString);
						smsDailyBeans.add(smsDailyBean);
					}
				}//end for app map
				map.put(sellerId, smsDailyBeans);
			}//end sellerList
			
			if (haveData) {
				//查询邮件组
				List<TSmsMailgroup> groupList = smsMailManager.getGroupAll();
				for (TSmsMailgroup tSmsMailgroup : groupList) {
					StringBuffer sb = new StringBuffer();
					sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
					sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"20%\">渠道名</td><td width=\"14%\">应用名</td><td width=\"6%\">下单数</td><td width=\"6%\">成功数</td><td width=\"6%\">成功数(扣后)</td><td width=\"6%\">失败数</td><td width=\"6%\">未支付</td><td width=\"6%\">金额</td><td width=\"6%\">金额(扣后)</td><td width=\"6%\">用户数</td><td width=\"6%\">成功用户数</td><td width=\"6%\">MR/MO转化率</td><td width=\"6%\">MR/REQ转化率</td></tr>");
					
					//查询邮件组内关联渠道
					List<TSmsSeller> sellerList = tSmsMailgroup.getSellerList();
					for (TSmsSeller tSmsSeller : sellerList) {
						List<SmsDailyBean> dataList = map.get(tSmsSeller.getId());//渠道下的数据组
						for (int i = 0; i < dataList.size(); i++) {
							SmsDailyBean data = dataList.get(i);
							if (i == 0) {
								sb.append("<tr><td rowspan=\""+dataList.size()+"\">"+data.getSellerName()+"</td><td>"+data.getAppName()+"</td><td>"+data.getOrderReq()+"</td><td>"+data.getSucc()+"</td><td>"+data.getSuccReduce()+"</td><td>"+data.getFail()+"</td><td>"+data.getNoPay()+"</td><td>"+data.getFee()+"</td><td>"+data.getFeeReduce()+"</td><td>"+data.getUserNum()+"</td><td>"+data.getUserSuccNum()+"</td><td>"+data.getRate()+"</td><td>"+data.getReqRate()+"</td></tr>");
							} else {
								sb.append("<tr><td>"+data.getAppName()+"</td><td>"+data.getOrderReq()+"</td><td>"+data.getSucc()+"</td><td>"+data.getSuccReduce()+"</td><td>"+data.getFail()+"</td><td>"+data.getNoPay()+"</td><td>"+data.getFee()+"</td><td>"+data.getFeeReduce()+"</td><td>"+data.getUserNum()+"</td><td>"+data.getUserSuccNum()+"</td><td>"+data.getRate()+"</td><td>"+data.getReqRate()+"</td></tr>");
							}
						}
					}
					sb.append("</table><br/><br/></body>");
					//查询邮件组关联人
					List<TSmsMailer> mailers = tSmsMailgroup.getMailerList();
					if (mailers.size() > 0) {					
						String[] mailToList = new String[mailers.size()];
						for (int i = 0; i < mailers.size(); i++) {
							mailToList[i] = mailers.get(i).getEmail();
						}
						//发送邮件
						String mailTitle = sdf.format(calendar.getTime())+"短代日报("+tSmsMailgroup.getName()+")";
						SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
