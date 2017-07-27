package com.tenfen.job.daily;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.operation.PackageDailyBean;
import com.tenfen.bean.system.SystemProperty;
import com.tenfen.entity.operation.pack.TPushMailer;
import com.tenfen.entity.operation.pack.TPushMailgroup;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.SendMailUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageChannelManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushMailManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class PackageDailyJob {
	
	@Autowired
	private PackageChannelManager packageChannelManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	@Autowired
	private PushMailManager pushMailManager;
	
	//发送邮件
	private final String mailLoginName = "yang.jinbo@tenfen.com";
	private final String mailLoginPwd = "yang19860202";
	private final String mailSmtp = "smtp.exmail.qq.com";
	
	public void execute() {
		try {
			boolean haveData = false;
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
			
			Map<Integer, PackageDailyBean> map = new HashMap<Integer, PackageDailyBean>();
			Map<Integer, String> reduceMap = orderManager.mapReduceSeller(start, end);
			for (Integer sellerId : reduceMap.keySet()) {
				haveData = true;
				String resultJson = reduceMap.get(sellerId);
				JSONObject jsonObject = JSONObject.parseObject(resultJson);
				Integer mo = jsonObject.getInteger("count") == null ? 0 : jsonObject.getInteger("count");//请求总数
				Integer moQc = jsonObject.getInteger("user") == null ? 0 : jsonObject.getInteger("user");//mo去重
				Integer mr = jsonObject.getInteger("succ") == null ? 0 : jsonObject.getInteger("succ");//mr
				Integer fee = jsonObject.getInteger("fee") == null ? 0 : jsonObject.getInteger("fee");//成功信息费
				fee = fee / 100;//转化以元为单位
				
				//转化率
				float f = 0;
				if (mr == 0) {
					f = 0;
				} else {
					f = (float)mr/moQc;
				}
				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
				String zhl = null;
				if (f == 0) {
					zhl = "0%";
				} else {
					zhl = df.format(f*100) + "%";//返回的是String类型的
				}
				
				PackageDailyBean packageDailyBean = new PackageDailyBean();
				packageDailyBean.setMo(mo);
				packageDailyBean.setMoQc(moQc);
				packageDailyBean.setMr(mr);
				packageDailyBean.setFee(fee);
				packageDailyBean.setZhl(zhl);
				
				map.put(sellerId, packageDailyBean);
			}
			
			if (haveData) {
				//查询邮件组
				List<TPushMailgroup> groupList = pushMailManager.getGroupAll();
				for (TPushMailgroup tPushMailgroup : groupList) {
					StringBuffer sb = new StringBuffer();
					sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
					sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"200\">渠道名称</td><td width=\"150\">请求MO</td><td width=\"150\">MO去重</td><td width=\"150\">成功MR</td><td width=\"150\">信息费（元）</td><td width=\"150\">转化率MR/MO</td><td width=\"100\">查看</td></tr>");
					
					//查询邮件组内关联渠道
					List<TPushSeller> sellerList = tPushMailgroup.getSellerList();
					for (TPushSeller tPushSeller : sellerList) {
						Integer sellerId = tPushSeller.getId();
						TPushSeller pushSeller = pushSellerManager.get(sellerId);
						String sellerName = pushSeller.getName();
						
						PackageDailyBean packageDailyBean = map.get(sellerId);
						if (!Utils.isEmpty(packageDailyBean)) {
							sb.append("<tr><td>"+sellerName+"</td><td>"+packageDailyBean.getMo()+"</td><td>"+packageDailyBean.getMoQc()+"</td><td>"+packageDailyBean.getMr()+"</td><td>"+packageDailyBean.getFee()+"</td><td>"+packageDailyBean.getZhl()+"</td><td><a href=\"http://www.gomzone.com:8080/html/packageDetail.html?sellerId="+sellerId+"&start="+startString+"&end="+endString+"\">分省详细</a></td></tr>");
						}
					}
					sb.append("</table><br/><br/>");
					
					//查询邮件组关联人
					List<TPushMailer> mailers = tPushMailgroup.getMailerList();
					if (mailers.size() > 0) {
						String[] mailToList = new String[mailers.size()];
						for (int i = 0; i < mailers.size(); i++) {
							mailToList[i] = mailers.get(i).getEmail();
						}
						//发送邮件
						String mailTitle = sdf.format(calendar.getTime())+"包月日报("+tPushMailgroup.getName()+")";
						SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	private final List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
//			"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆"});
	
//	public void execute() {
//		try {
//			//查询渠道数量
//			List<PushPackageChannel> channelList = packageChannelManager.getPackageChannelList();
//			for (PushPackageChannel pushPackageChannel : channelList) {
//				String channel = pushPackageChannel.getClientVersion();
//				String channelName = pushPackageChannel.getChannelName();
//				//第一份报表
//				//查询日志不去重
//				Calendar calendar = Calendar.getInstance();
//				calendar.add(Calendar.DATE, -1);
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//				//获取当日时间区间
//				SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
//				String startString = sdf.format(calendar.getTime()) + " 00:00:00";
//				Date startDate = sdfSql.parse(startString);
//				java.sql.Date start = new java.sql.Date(startDate.getTime());
//				
//				String endString = sdf.format(calendar.getTime()) + " 23:59:59";
//				Date endDate = sdfSql.parse(endString);
//				java.sql.Date end = new java.sql.Date(endDate.getTime());
//				
//				//获取当月时间区间
//				Calendar calendarMonth = Calendar.getInstance();
//				calendarMonth.set(Calendar.DAY_OF_MONTH, 1);
//				String monthStartString = sdf.format(calendarMonth.getTime()) + " 00:00:00";
//				Date monthStartDate = sdfSql.parse(monthStartString);
//				java.sql.Date monthStart = new java.sql.Date(monthStartDate.getTime());
//				
//				calendarMonth.add(Calendar.MONTH, 1);
//				calendarMonth.set(Calendar.DAY_OF_MONTH, 1);
//				calendarMonth.add(Calendar.DAY_OF_MONTH, -1);
//				String monthEndString = sdf.format(calendarMonth.getTime()) + " 23:59:59";
//				Date monthEndDate = sdfSql.parse(monthEndString);
//				java.sql.Date monthEnd = new java.sql.Date(monthEndDate.getTime());
//				
//				StringBuffer sb = new StringBuffer();
//				sb.append("<html><body><table border=\"1\" cellspacing=\"0\">");
//				sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"200\">省份</td><td width=\"150\">请求MO</td><td width=\"150\">MO去重</td><td width=\"150\">成功MR</td><td width=\"150\">信息费（元）</td><td width=\"150\">转化率MR/MO</td></tr>");
//				
//				Map<String, String> map_mo = orderManager.mapReduceProvince(channel, start, end, null);//mo
//				Map<String, String> map_mr = orderManager.mapReduceProvince(channel, start, end, 3);//mr
//				
//				Integer moQuanguo = 0;
//				Integer moQuanguoQc = 0;
//				Integer mrQuanguo = 0;
//				Integer feeQuanguo = 0;
//				String zhlQuanguo = "0%";
//				
//				List<PackageDailyBean> packageDailyBeans = new ArrayList<PackageDailyBean>();
//				for (String province : provinceList) {
//					Integer mo = 0;
//					Integer moQc = 0;
//					Integer mr = 0;
//					Integer fee = 0;
//					
//					String mo_string = map_mo.get(province);
//					if (!Utils.isEmpty(mo_string)) {
//						JSONObject json_mo = JSONObject.parseObject(mo_string);
//						mo = json_mo.getInteger("count");//mo
//					}
//					if (mo!=0) {
//						Map<String, String> map_moQc = orderManager.mapReduceUserByProvince(channel, province, start, end, null);
//						moQc = map_moQc.size();
//					}
//					
//					String mr_string = map_mr.get(province);
//					if (!Utils.isEmpty(mr_string)) {
//						JSONObject json_mr = JSONObject.parseObject(mr_string);
//						mr = json_mr.getInteger("count");//mr
//						fee = json_mr.getInteger("fee") / 100;//信息费
//					}
//					
//					//转化率
//					float f = 0;
//					if (mr == 0) {
//						f = 0;
//					} else {
//						f = (float)mr/moQc;
//					}
//					DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//					String zhl = null;
//					if (f == 0) {
//						zhl = "0%";
//					} else {
//						zhl = df.format(f*100) + "%";//返回的是String类型的
//					}
//					
//					PackageDailyBean packageDailyBean = new PackageDailyBean();
//					packageDailyBean.setProvince(province);
//					packageDailyBean.setMo(mo);
//					packageDailyBean.setMoQc(moQc);
//					packageDailyBean.setMr(mr);
//					packageDailyBean.setFee(fee);
//					packageDailyBean.setZhlf(f);
//					packageDailyBean.setZhl(zhl);
//					packageDailyBeans.add(packageDailyBean);
//					
//					moQuanguo += mo;
//					moQuanguoQc += moQc;
//					mrQuanguo += mr;
//					feeQuanguo += fee;
//				}
//				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//				//全国转化率
//				float quanguof = 0;
//				if (mrQuanguo == 0) {
//					quanguof = 0;
//				} else {
//					quanguof = (float)mrQuanguo/moQuanguoQc;
//				}
//				if (quanguof == 0) {
//					zhlQuanguo = "0%";
//				} else {
//					zhlQuanguo = df.format(quanguof*100) + "%";//返回的是String类型的
//				}
//				
//				Collections.sort(packageDailyBeans);
//				for (PackageDailyBean packageDailyBean : packageDailyBeans) {
//					sb.append("<tr><td>"+packageDailyBean.getProvince()+"</td><td>"+packageDailyBean.getMo()+"</td><td>"+packageDailyBean.getMoQc()+"</td><td>"+packageDailyBean.getMr()+"</td><td>"+packageDailyBean.getFee()+"</td><td>"+packageDailyBean.getZhl()+"</td></tr>");
//				}
//				sb.append("<tr><td>全国</td><td>"+moQuanguo+"</td><td>"+moQuanguoQc+"</td><td>"+mrQuanguo+"</td><td>"+feeQuanguo+"</td><td>"+zhlQuanguo+"</td></tr>");
//				sb.append("</table><br/><br/>");
//				
//				//第二份报表
//				sb.append("<table border=\"1\" cellspacing=\"0\">");
//				sb.append("<tr style=\"background-color:#a0c6e5\"><td width=\"300\">包月包名称</td><td width=\"100\">价格（元）</td><td width=\"100\">当日新增</td><td width=\"150\">日信息费（元）</td><td width=\"100\">当月新增</td><td width=\"150\">月信息费（元）</td></tr>");
//				
//				Map<Integer, String> succ_day = orderManager.mapReducePushIds(channel, start, end, 3);//当日成功数
//				Map<Integer, String> succ_month = orderManager.mapReducePushIds(channel, monthStart, monthEnd, 3);//当日成功数
//				
//				List<PushPackage> packageList = packageManager.findPackageListByChannelAll(channel);
//				for (PushPackage pushPackage : packageList) {
//					String packageName = pushPackage.getPackageName();
//					Integer pushId = pushPackage.getId();
//					Integer price = pushPackage.getPrice()/100;
//					
//					String succ_day_string = succ_day.get(pushId);
//					Integer dangri = 0;
//					Integer feeDay = 0;
//					Integer dangyue = 0;
//					Integer feeMonth = 0;
//					if (!Utils.isEmpty(succ_day_string)) {
//						JSONObject json_day = JSONObject.parseObject(succ_day_string);
//						dangri = json_day.getInteger("count");
//						feeDay = json_day.getInteger("fee")/100;
//					}
//					String succ_month_string = succ_month.get(pushId);
//					if (!Utils.isEmpty(succ_month_string)) {						
//						JSONObject json_month = JSONObject.parseObject(succ_month_string);
//						dangyue = json_month.getInteger("count");
//						feeMonth = json_month.getInteger("fee")/100;
//					}
//					
//					sb.append("<tr><td>"+packageName+"</td><td>"+price+"</td><td>"+dangri+"</td><td>"+feeDay+"</td><td>"+dangyue+"</td><td>"+feeMonth+"</td></tr>");
//				}
//				sb.append("</table></body></html>");
//				
//				if ("QIANKUN_PARTNER_ZHEXIN_1.0.0".equals(channel) || "QIANKUN_ZYXD".equals(channel)) {
//					//发送邮件
//					String mailLoginName = "yang.jinbo@tenfen.com";
//					String mailLoginPwd = "yang19860202";
//					String mailSmtp = "smtp.exmail.qq.com";
//					String[] mailToList = new String[] {"yang.jinbo@tenfen.com", "yuan.chenglong@tenfen.com", "xu.zhenglie@tenfen.com", "he.dan@tenfen.com"};
//					String mailTitle = channelName+sdf.format(calendar.getTime())+"包月日报";
//					SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
//				} else if ("TENFEN_PARTNER_ZHEXIN_1.0.0".equals(channel)) {
//					//发送邮件
//					String mailLoginName = "yang.jinbo@tenfen.com";
//					String mailLoginPwd = "yang19860202";
//					String mailSmtp = "smtp.exmail.qq.com";
//					String[] mailToList = new String[] {"yang.jinbo@tenfen.com", "yuan.chenglong@tenfen.com", "xu.zhenglie@tenfen.com", "he.dan@tenfen.com", "yan.qun@tenfen.com", "gao.feng@tenfen.com"};
//					String mailTitle = channelName+sdf.format(calendar.getTime())+"包月日报";
//					SendMailUtil.sendHtmlMail(mailLoginName, mailLoginPwd, mailSmtp, mailToList, mailTitle, sb.toString());
//				}
//				
//			}//end channelList
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	
}
