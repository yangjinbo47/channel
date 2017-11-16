package com.tenfen.www.action.system.operation.pack;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.bean.operation.PackageDailyBean;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.mongoEntity.MongoTOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class OrderAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	@Autowired
	private PackageManager packageManager;
	
	private Integer limit;
	private Integer page;
	private Integer start;

	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
		config.put(java.util.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
	}
	public void list() {
//		String channel = ServletRequestUtils.getStringParameter(request, "channel", null);
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 0);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		
		try {
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
				java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
				
				Page<TOrder> orderPage = new Page<TOrder>();
				//设置默认排序方式
				orderPage.setPageSize(limit);
				orderPage.setPageNo(page);
				
//				List<PushPackageUser> list = packageUserDao.getOrderList(channel, start, end);
//				Page<TOrder> pushPackageUser = orderManager.getPackageUserPageByProperty(packageUserPage, channel, start, end);
				Page<TOrder> resultPage = orderManager.getOrderPage(orderPage, sellerId, start, end);
				
				long nums = resultPage.getTotalCount();
				StringBuilder jstr = new StringBuilder("{");
				jstr.append("total:" + String.valueOf(nums) + ",");
				jstr.append("orders:");

				List<TOrder> packageUsers = resultPage.getResult();
				jstr.append(JSON.toJSONString(packageUsers, config));
				jstr.append("}");
				StringUtil.printJson(response, jstr.toString());
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public void listByPhone() {
		String phone = ServletRequestUtils.getStringParameter(request, "phone", null);
		
		try {
			long nums = orderManager.getOrderCountByPhoneFromMongo(phone);
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("orders:");
			
			List<MongoTOrder> orders = orderManager.getOrderPageByPhoneFromMongo(page, limit, phone);
			for (MongoTOrder mongoTOrder : orders) {
				TPushSeller tPushSeller = pushSellerManager.get(mongoTOrder.getSellerId());
				mongoTOrder.setSellerName(tPushSeller.getName());
				PushPackage pushPackage = packageManager.get(mongoTOrder.getPushId());
				mongoTOrder.setPackageName(pushPackage.getPackageName());
			}
			jstr.append(JSON.toJSONString(orders, config));
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
//	public String ajaxSearch() {
//		String channel = ServletRequestUtils.getStringParameter(request, "channel", null);
//		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
//		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
//		try {
//			if (startTime != null && endTime != null) {
//				startTime += " 00:00:00";
//				endTime += " 23:59:59";
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				Date start = new Date(sdf.parse(startTime).getTime());
//				Date end = new Date(sdf.parse(endTime).getTime());
//				
//				Long count = packageUserDao.getCountByProperty(channel, start, end);
//				Long count = 0l;
//				StringUtil.printJson(response, count+"");
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return null;
//	}
	
	public String export() {
//		String channel = ServletRequestUtils.getStringParameter(request, "channel", null);
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 0);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		try {
			TPushSeller tPushSeller = pushSellerManager.getEntity(sellerId);
			if (startTime != null && endTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
				java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
				
//				List<TOrder> list = orderManager.getOrderList(channel, start, end);
				List<TOrder> list = orderManager.getOrderList(sellerId, start, end);
				
				response.setHeader("Content-disposition", "attachment;filename=qdtj.xlsx");
				response.setContentType("application/msexcel");
				OutputStream outputStream = response.getOutputStream();
				
				// 第一步，创建一个webbook，对应一个Excel文件
//				HSSFWorkbook wb = new HSSFWorkbook();
				XSSFWorkbook wb = new XSSFWorkbook();
				// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
//				HSSFSheet sheet = wb.createSheet(channel);
				XSSFSheet sheet = wb.createSheet(tPushSeller.getName());
				// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
//				HSSFRow row = sheet.createRow((int) 0);
				XSSFRow row = sheet.createRow((int) 0);
				// 第四步，创建单元格，并设置值表头 设置表头居中
//				HSSFCellStyle style = wb.createCellStyle();
				XSSFCellStyle style = wb.createCellStyle();
				style.setAlignment(XSSFCellStyle.ALIGN_CENTER);// 创建一个居中格式
				
//				HSSFCell cell = row.createCell((short) 0);
				XSSFCell cell = row.createCell((short) 0);
				cell.setCellValue(new XSSFRichTextString("号码"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 1);
				cell.setCellValue(new XSSFRichTextString("所属省份"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 2);
				cell.setCellValue(new XSSFRichTextString("包月名称"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 3);
				cell.setCellValue(new XSSFRichTextString("渠道名称"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 4);
				cell.setCellValue(new XSSFRichTextString("订购时间"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 5);
				cell.setCellValue(new XSSFRichTextString("状态"));
				cell.setCellStyle(style);
				
				// 第五步，写入实体数据 实际应用中这些数据从数据库得到，
				for (int i = 0; i < list.size(); i++) {
					row = sheet.createRow((int) i + 1);
					TOrder pushPackageUser = list.get(i);
					row.createCell((short) 0).setCellValue(new XSSFRichTextString(pushPackageUser.getPhoneNum()));
					row.createCell((short) 1).setCellValue(new XSSFRichTextString(pushPackageUser.getProvince()));
					row.createCell((short) 2).setCellValue(new XSSFRichTextString(pushPackageUser.getName()));
					row.createCell((short) 3).setCellValue(new XSSFRichTextString(tPushSeller.getName()));
					row.createCell((short) 4).setCellValue(new XSSFRichTextString(Utils.isEmpty(pushPackageUser.getCreateTime()) ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pushPackageUser.getCreateTime())));
					String status = null;
					if (1 == pushPackageUser.getStatus()) {
						status = "未支付";
					} else if (3 == pushPackageUser.getStatus()) {
						status = "成功";
					} else if (4 == pushPackageUser.getStatus()) {
						status = "失败";
					}
					row.createCell((short) 5).setCellValue(new XSSFRichTextString(status));
				}
				
				wb.write(outputStream);
				outputStream.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
	
	public void reportAll() {
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		
		try {
			java.sql.Date start = null;
			java.sql.Date end = null;
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				start = new java.sql.Date(sdf.parse(startTime).getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(endTime));
				calendar.add(Calendar.DATE, 1);
				end = new java.sql.Date(calendar.getTimeInMillis());
			} else {
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//获取当日时间区间
				SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
				String startString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date startDate = sdfSql.parse(startString);
				start = new java.sql.Date(startDate.getTime());
				
				calendar.add(Calendar.DATE, 1);
				String endString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date endDate = sdfSql.parse(endString);
				end = new java.sql.Date(endDate.getTime());
			}
			
			JSONArray jsonArray = new JSONArray();
			Map<Integer, Map<Integer, String>> reduceMap = orderManager.mapReduceSeller(start, end);
			for (Integer sellerId : reduceMap.keySet()) {
				Map<Integer, String> statusMap = reduceMap.get(sellerId);
				String noPayStr = statusMap == null ? null : statusMap.get(1);
				String succStr = statusMap == null ? null : statusMap.get(3);
				String failStr = statusMap == null ? null : statusMap.get(4);
				JSONObject noPayJson = JSONObject.parseObject(noPayStr);
				Integer noPayMo = noPayJson == null ? 0 : noPayJson.getInteger("count");
				Integer noPayMoQc = noPayJson == null ? 0 : noPayJson.getInteger("user");
				JSONObject succJson = JSONObject.parseObject(succStr);
				Integer succMo = succJson == null ? 0 : succJson.getInteger("count");
				Integer succMoQc = succJson == null ? 0 : succJson.getInteger("user");
				JSONObject failJson = JSONObject.parseObject(failStr);
				Integer failMo = failJson == null ? 0 : failJson.getInteger("count");
				Integer failMoQc = failJson == null ? 0 : failJson.getInteger("user");
				
				Integer mo = noPayMo + succMo + failMo;//请求总数
				Integer moQc = noPayMoQc + succMoQc + failMoQc;//mo去重
				Integer mr = succMoQc;//mr
				Integer fee = succJson == null ? 0 : succJson.getInteger("fee");//成功信息费
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
				
				TPushSeller tPushSeller = pushSellerManager.get(sellerId);
				String sellerName = tPushSeller.getName();
				
				JSONObject report = new JSONObject();
				report.put("sellerId", sellerId);
				report.put("sellerName", sellerName);
				report.put("mo", mo);
				report.put("moQc", moQc);
				report.put("mr", mr);
				report.put("fee", fee);
				report.put("zhl", zhl);
				jsonArray.add(report);
			}
			
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + jsonArray.size() + ",");
			jstr.append("reports:");
			jstr.append(jsonArray.toJSONString());
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
	"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆",null});
			
	//具体包的省份详情
//	public void provinceDetail() {
//		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
//		Integer pushId = ServletRequestUtils.getIntParameter(request, "pushId", -1);
//		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
//		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
//		
//		try {
//			java.sql.Date start = null;
//			java.sql.Date end = null;
//			if (startTime != null && endTime != null) {
//				startTime = startTime.replace("T", " ");
//				endTime = endTime.replace("T", " ");
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				start = new java.sql.Date(sdf.parse(startTime).getTime());
//				
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTime(sdf.parse(endTime));
//				calendar.add(Calendar.DATE, 1);
//				end = new java.sql.Date(calendar.getTimeInMillis());
//			} else {
//				Calendar calendar = Calendar.getInstance();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//				//获取当日时间区间
//				SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
//				String startString = sdf.format(calendar.getTime()) + " 00:00:00";
//				Date startDate = sdfSql.parse(startString);
//				start = new java.sql.Date(startDate.getTime());
//				
//				calendar.add(Calendar.DATE, 1);
//				String endString = sdf.format(calendar.getTime()) + " 00:00:00";
//				Date endDate = sdfSql.parse(endString);
//				end = new java.sql.Date(endDate.getTime());
//			}
//			
//			JSONArray jsonArray = new JSONArray();
//			
//			Map<String, String> map = orderManager.mapReduceProvince(sellerId, pushId, start, end);
//			Integer moQuanguo = 0;
//			Integer moQuanguoQc = 0;
//			Integer mrQuanguo = 0;
//			Integer feeQuanguo = 0;
//			String zhlQuanguo = null;
//			
//			List<PackageDailyBean> packageDailyBeans = new ArrayList<PackageDailyBean>();
//			for (String province : provinceList) {
//				Integer mo = 0;
//				Integer moQc = 0;
//				Integer mr = 0;
//				Integer fee = 0;
//				String zhl = null;
//				String resultStr = map.get(province);
//				if (resultStr != null) {
//					JSONObject jsonObject = JSONObject.parseObject(resultStr);
//					mo = jsonObject.getInteger("count") == null ? 0 : jsonObject.getInteger("count");//请求总数
//					moQc = jsonObject.getInteger("user") == null ? 0 : jsonObject.getInteger("user");//mo去重
//					mr = jsonObject.getInteger("succ") == null ? 0 : jsonObject.getInteger("succ");//mr
//					fee = jsonObject.getInteger("fee") == null ? 0 : jsonObject.getInteger("fee");//成功信息费
//					fee = fee / 100;//转化以元为单位
//				}
//				//转化率
//				float f = 0;
//				if (mr == 0) {
//					f = 0;
//				} else {
//					f = (float)mr/moQc;
//				}
//				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//				if (f == 0) {
//					zhl = "0%";
//				} else {
//					zhl = df.format(f*100) + "%";//返回的是String类型的
//				}
//				
//				JSONObject report = new JSONObject();
//				if (province == null) {
//					province = "其他";
//				}
//				report.put("province", province);
//				report.put("mo", mo);
//				report.put("moQc", moQc);
//				report.put("mr", mr);
//				report.put("fee", fee);
//				report.put("zhl", zhl);
//				jsonArray.add(report);
//				
//				moQuanguo += mo;
//				moQuanguoQc += moQc;
//				mrQuanguo += mr;
//				feeQuanguo += fee;
//			}
//			//全国转化率
//			DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//			float quanguof = 0;
//			if (mrQuanguo == 0) {
//				quanguof = 0;
//			} else {
//				quanguof = (float)mrQuanguo/moQuanguoQc;
//			}
//			if (quanguof == 0) {
//				zhlQuanguo = "0%";
//			} else {
//				zhlQuanguo = df.format(quanguof*100) + "%";//返回的是String类型的
//			}
//			Collections.sort(packageDailyBeans);//按转化率排序
//			
//			JSONObject report = new JSONObject();
//			report.put("province", "全国");
//			report.put("mo", moQuanguo);
//			report.put("moQc", moQuanguoQc);
//			report.put("mr", mrQuanguo);
//			report.put("fee", feeQuanguo);
//			report.put("zhl", zhlQuanguo);
//			jsonArray.add(report);
//			
//			StringBuilder jstr = new StringBuilder("{");
//			jstr.append("total:" + jsonArray.size() + ",");
//			jstr.append("reports:");
//			jstr.append(jsonArray.toJSONString());
//			jstr.append("}");
//			StringUtil.printJson(response, jstr.toString());
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	//整个渠道的省份详情
	public void provinceDetail() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		
		try {
			java.sql.Date start = null;
			java.sql.Date end = null;
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				start = new java.sql.Date(sdf.parse(startTime).getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(endTime));
				calendar.add(Calendar.DATE, 1);
				end = new java.sql.Date(calendar.getTimeInMillis());
			} else {
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//获取当日时间区间
				SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
				String startString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date startDate = sdfSql.parse(startString);
				start = new java.sql.Date(startDate.getTime());
				
				calendar.add(Calendar.DATE, 1);
				String endString = sdf.format(calendar.getTime()) + " 00:00:00";
				Date endDate = sdfSql.parse(endString);
				end = new java.sql.Date(endDate.getTime());
			}
			
			JSONArray jsonArray = new JSONArray();
			
			Map<String, Map<Integer, String>> reduceMap = orderManager.mapReduceProvince(sellerId, start, end);
			Integer moQuanguo = 0;
			Integer moQuanguoQc = 0;
			Integer mrQuanguo = 0;
			Integer feeQuanguo = 0;
			String zhlQuanguo = null;
			
			List<PackageDailyBean> packageDailyBeans = new ArrayList<PackageDailyBean>();
			for (String province : provinceList) {
				Integer mo = 0;
				Integer moQc = 0;
				Integer mr = 0;
				Integer fee = 0;
				String zhl = null;
//				String resultStr = map.get(province);
				Map<Integer, String> statusMap = reduceMap.get(province);
				if (statusMap != null) {
					String noPayStr = statusMap == null ? null : statusMap.get(1);
					String succStr = statusMap == null ? null : statusMap.get(3);
					String failStr = statusMap == null ? null : statusMap.get(4);
					JSONObject noPayJson = JSONObject.parseObject(noPayStr);
					Integer noPayMo = noPayJson == null ? 0 : noPayJson.getInteger("count");
					Integer noPayMoQc = noPayJson == null ? 0 : noPayJson.getInteger("user");
					JSONObject succJson = JSONObject.parseObject(succStr);
					Integer succMo = succJson == null ? 0 : succJson.getInteger("count");
					Integer succMoQc = succJson == null ? 0 : succJson.getInteger("user");
					JSONObject failJson = JSONObject.parseObject(failStr);
					Integer failMo = failJson == null ? 0 : failJson.getInteger("count");
					Integer failMoQc = failJson == null ? 0 : failJson.getInteger("user");
					
					mo = noPayMo + succMo + failMo;//请求总数
					moQc = noPayMoQc + succMoQc + failMoQc;//mo去重
					mr = succMoQc;//mr
					fee = succJson == null ? 0 : succJson.getInteger("fee");//成功信息费
					fee = fee / 100;//转化以元为单位
//					JSONObject jsonObject = JSONObject.parseObject(resultStr);
//					mo = jsonObject.getInteger("count") == null ? 0 : jsonObject.getInteger("count");//请求总数
//					moQc = jsonObject.getInteger("user") == null ? 0 : jsonObject.getInteger("user");//mo去重
//					mr = jsonObject.getInteger("succ") == null ? 0 : jsonObject.getInteger("succ");//mr
//					fee = jsonObject.getInteger("fee") == null ? 0 : jsonObject.getInteger("fee");//成功信息费
//					fee = fee / 100;//转化以元为单位
				}
				//转化率
				float f = 0;
				if (mr == 0) {
					f = 0;
				} else {
					f = (float)mr/moQc;
				}
				DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
				if (f == 0) {
					zhl = "0%";
				} else {
					zhl = df.format(f*100) + "%";//返回的是String类型的
				}
				
				JSONObject report = new JSONObject();
				if (province == null) {
					province = "其他";
				}
				report.put("province", province);
				report.put("mo", mo);
				report.put("moQc", moQc);
				report.put("mr", mr);
				report.put("fee", fee);
				report.put("zhl", zhl);
				jsonArray.add(report);
				
				moQuanguo += mo;
				moQuanguoQc += moQc;
				mrQuanguo += mr;
				feeQuanguo += fee;
			}
			//全国转化率
			DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
			float quanguof = 0;
			if (mrQuanguo == 0) {
				quanguof = 0;
			} else {
				quanguof = (float)mrQuanguo/moQuanguoQc;
			}
			if (quanguof == 0) {
				zhlQuanguo = "0%";
			} else {
				zhlQuanguo = df.format(quanguof*100) + "%";//返回的是String类型的
			}
			Collections.sort(packageDailyBeans);//按转化率排序
			
			JSONObject report = new JSONObject();
			report.put("province", "全国");
			report.put("mo", moQuanguo);
			report.put("moQc", moQuanguoQc);
			report.put("mr", mrQuanguo);
			report.put("fee", feeQuanguo);
			report.put("zhl", zhlQuanguo);
			jsonArray.add(report);
			
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + jsonArray.size() + ",");
			jstr.append("reports:");
			jstr.append(jsonArray.toJSONString());
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}
}
