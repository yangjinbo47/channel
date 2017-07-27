package com.tenfen.www.action.system;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.system.SystemProperty;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.SendMailUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageChannelManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.service.system.VisitLogTmpManager;

public class SystemAction extends SimpleActionSupport {

	private static final long serialVersionUID = 6728866051491427081L;
	
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private PackageChannelManager packageChannelManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private VisitLogTmpManager visitLogTmpManager;
	
	/**
	*@功能：清理缓存
	*@author BOBO
	*@date Apr 24, 2012
	*@return
	 */
	public String flushAll() {
		boolean b = false;
		
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			b = mc.flushAll();
			
			setRequestAttribute("isSucc", b);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "success";
	}
	
	/**
	*@功能：清理单个缓存
	*@author BOBO
	*@date Apr 24, 2012
	*@return
	 */
	public String flushSingle() {
		String key = ServletRequestUtils.getStringParameter(request, "key", "");
		try {
			if (!Utils.isEmpty(key)) {
				boolean b = false;
				ICacheClient mc = cacheFactory.getCommonCacheClient();
				b = mc.deleteCache(key);
				
				if (b) {
					addActionMessage("缓存"+key+"清理成功");
				} else {
					addActionMessage("缓存"+key+"清理成功");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "success";
	}
	
	public String bufaDailyjob() {
		int t = ServletRequestUtils.getIntParameter(request, "t", -1);//上推几天
		//opendaily
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, t);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取当日时间区间
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
			sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"20%\">渠道名</td><td width=\"14%\">应用名</td><td width=\"6%\">下单数</td><td width=\"6%\">成功数</td><td width=\"6%\">成功数(扣后)</td><td width=\"6%\">失败数</td><td width=\"6%\">未支付</td><td width=\"6%\">金额</td><td width=\"6%\">金额(扣后)</td><td width=\"6%\">用户数</td><td width=\"6%\">成功用户数</td><td width=\"6%\">MR/MO转化率</td><td width=\"6%\">MR/REQ转化率</td></tr>");
			
			List<TOpenSeller> openSellerList = openSellerManager.findAllOpenSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TOpenSeller tOpenSeller : openSellerList) {
				int sellerId = tOpenSeller.getId();
				if (sellerId == 11) {
					continue;
				}
				String sellerName = tOpenSeller.getName();
				
				Map<Integer, String> noPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
				Map<Integer, String> succPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
				Map<Integer, String> failPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
				Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
				allStatusMap.putAll(noPayMap);
				allStatusMap.putAll(succPayMap);
				allStatusMap.putAll(failPayMap);
				Map<Integer, String> succPayReduceMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
				
				Integer sellerAppSize = allStatusMap.keySet().size();
				int i = 0;
				for (Integer appId : allStatusMap.keySet()) {
					i++;
					Integer noPayInt = null;
					if (noPayMap.size() == 0) {
						noPayInt = 0;
					} else {
						JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
						noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
						if (noPayInt == null) {
							noPayInt = 0;
						}
					}
					Integer failInt = null;
					if (failPayMap.size() == 0) {
						failInt = 0;
					} else {
						JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
						failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
						if (failInt == null) {
							failInt = 0;
						}
					}
					Integer succInt = null;
					Integer feeInt = null;
					if (succPayMap.size() == 0) {
						succInt = 0;
						feeInt = 0;
					} else {						
						JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
						succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
						if (succInt == null) {
							succInt = 0;
						}
						feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
						if (feeInt == null) {
							feeInt = 0;
						}
						feeInt = feeInt/100;//fee转化成单位元
					}
					Integer succReduceInt = null;
					Integer feeReduceInt = null;
					if (succPayReduceMap.size() == 0) {
						succReduceInt = 0;
						feeReduceInt = 0;
					} else {						
						JSONObject succPayJson = JSONObject.parseObject(succPayReduceMap.get(appId));
						succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数（扣量后）
						if (succReduceInt == null) {
							succReduceInt = 0;
						}
						feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额（扣量后）
						if (feeReduceInt == null) {
							feeReduceInt = 0;
						}
						feeReduceInt = feeReduceInt/100;//fee转化成单位元
					}
					
					Integer orderReqInt = noPayInt+failInt+succInt;
					Long users_num = openOrderManager.mapReduceUserCount(sellerId, appId, start, end);
					Long users_succ_num = openOrderManager.mapReduceSuccUserCount(sellerId, appId, start, end);
					
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
					
					TOpenApp tOpenApp = openAppManager.get(appId);
					String appName = tOpenApp.getName();
					if (i == 1) {
						sb.append("<tr><td rowspan=\""+sellerAppSize+"\">"+sellerName+"</td><td>"+appName+"</td><td>"+orderReqInt+"</td><td>"+succInt+"</td><td>"+succReduceInt+"</td><td>"+failInt+"</td><td>"+noPayInt+"</td><td>"+feeInt+"</td><td>"+feeReduceInt+"</td><td>"+users_num+"</td><td>"+users_succ_num+"</td><td>"+fString+"</td><td>"+reqfString+"</td></tr>");
					} else {
						sb.append("<tr><td>"+appName+"</td><td>"+orderReqInt+"</td><td>"+succInt+"</td><td>"+succReduceInt+"</td><td>"+failInt+"</td><td>"+noPayInt+"</td><td>"+feeInt+"</td><td>"+feeReduceInt+"</td><td>"+users_num+"</td><td>"+users_succ_num+"</td><td>"+fString+"</td><td>"+reqfString+"</td></tr>");
					}
				}//end for map
			}//end sellerList
			
			sb.append("</table><br/><br/></body>");
			//发送邮件
			String mailLoginName = "yang.jinbo@tenfen.com";
			String mailLoginPwd = "yang19860202";
			String mailSmtp = "smtp.exmail.qq.com";
			String[] mailToList = new String[] {"yang.jinbo@tenfen.com", "yuan.chenglong@tenfen.com", "xu.zhenglie@tenfen.com", "he.dan@tenfen.com"};
			String mailTitle = sdf.format(calendar.getTime())+"能力开放日报";
			SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		//smsdaily
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, t);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			//获取当日时间区间
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
			Date startDate = sdfSql.parse(startString);
			java.sql.Date start = new java.sql.Date(startDate.getTime());
			
			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
			Date endDate = sdfSql.parse(endString);
			java.sql.Date end = new java.sql.Date(endDate.getTime());
			
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
			sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"20%\">渠道名</td><td width=\"14%\">应用名</td><td width=\"6%\">下单数</td><td width=\"6%\">成功数</td><td width=\"6%\">成功数(扣后)</td><td width=\"6%\">失败数</td><td width=\"6%\">未支付</td><td width=\"6%\">金额</td><td width=\"6%\">金额(扣后)</td><td width=\"6%\">用户数</td><td width=\"6%\">成功用户数</td><td width=\"6%\">MR/MO转化率</td><td width=\"6%\">MR/REQ转化率</td></tr>");
			
			List<TSmsSeller> smsSellerList = smsSellerManager.findAllSmsSellerList(Constants.USER_TYPE.ALL.getValue());
			for (TSmsSeller tSmsSeller : smsSellerList) {
				Integer sellerId = tSmsSeller.getId();
				String sellerName = tSmsSeller.getName();
				
				Map<Integer, String> noPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
				Map<Integer, String> succPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
				Map<Integer, String> failPayMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
				Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
				allStatusMap.putAll(noPayMap);
				allStatusMap.putAll(succPayMap);
				allStatusMap.putAll(failPayMap);
				Map<Integer, String> succPayReduceMap = smsOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
				
				Integer sellerAppSize = allStatusMap.keySet().size();
				int i = 0;
				for (Integer appId : allStatusMap.keySet()) {
					i++;
					Integer noPayInt = null;
					if (noPayMap.size() == 0) {
						noPayInt = 0;
					} else {
						JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(appId));
						noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
						if (noPayInt == null) {
							noPayInt = 0;
						}
					}
					Integer failInt = null;
					if (failPayMap.size() == 0) {
						failInt = 0;
					} else {
						JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(appId));
						failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
						if (failInt == null) {
							failInt = 0;
						}
					}
					Integer succInt = null;
					Integer feeInt = null;
					if (succPayMap.size() == 0) {
						succInt = 0;
						feeInt = 0;
					} else {						
						JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(appId));
						succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
						if (succInt == null) {
							succInt = 0;
						}
						feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
						if (feeInt == null) {
							feeInt = 0;
						}
						feeInt = feeInt/100;//fee转化成单位元
					}
					Integer succReduceInt = null;
					Integer feeReduceInt = null;
					if (succPayReduceMap.size() == 0) {
						succReduceInt = 0;
						feeReduceInt = 0;
					} else {						
						JSONObject succPayJson = JSONObject.parseObject(succPayReduceMap.get(appId));
						succReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数（扣量后）
						if (succReduceInt == null) {
							succReduceInt = 0;
						}
						feeReduceInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额（扣量后）
						if (feeReduceInt == null) {
							feeReduceInt = 0;
						}
						feeReduceInt = feeReduceInt/100;//fee转化成单位元
					}
					
					Integer orderReqInt = noPayInt+failInt+succInt;
					Long users_num = smsOrderManager.mapReduceUserCount(sellerId, appId, start, end);
					Long users_succ_num = smsOrderManager.mapReduceSuccUserCount(sellerId, appId, start, end);
					
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
					
					TSmsApp tSmsApp = smsAppManager.get(appId);
					String appName = tSmsApp.getName();
					if (i == 1) {
						sb.append("<tr><td rowspan=\""+sellerAppSize+"\">"+sellerName+"</td><td>"+appName+"</td><td>"+orderReqInt+"</td><td>"+succInt+"</td><td>"+succReduceInt+"</td><td>"+failInt+"</td><td>"+noPayInt+"</td><td>"+feeInt+"</td><td>"+feeReduceInt+"</td><td>"+users_num+"</td><td>"+users_succ_num+"</td><td>"+fString+"</td><td>"+reqfString+"</td></tr>");
					} else {
						sb.append("<tr><td>"+appName+"</td><td>"+orderReqInt+"</td><td>"+succInt+"</td><td>"+succReduceInt+"</td><td>"+failInt+"</td><td>"+noPayInt+"</td><td>"+feeInt+"</td><td>"+feeReduceInt+"</td><td>"+users_num+"</td><td>"+users_succ_num+"</td><td>"+fString+"</td><td>"+reqfString+"</td></tr>");
					}
				}//end for map
			}//end sellerList
			
			sb.append("</table><br/><br/></body>");
			//发送邮件
			String mailLoginName = "yang.jinbo@tenfen.com";
			String mailLoginPwd = "yang19860202";
			String mailSmtp = "smtp.exmail.qq.com";
			String[] mailToList = new String[] {"yang.jinbo@tenfen.com", "yuan.chenglong@tenfen.com", "xu.zhenglie@tenfen.com", "he.dan@tenfen.com"};
			String mailTitle = sdf.format(calendar.getTime())+"短代日报";
			SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
	
}
