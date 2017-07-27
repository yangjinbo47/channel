package com.tenfen.www.action.system.operation.open;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenOrderConversionrate;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.mongoEntity.MongoTOpenOrder;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenOrderConversionRateManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

public class OpenOrderAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private OpenOrderConversionRateManager openOrderConversionRateManager;
	
	private Integer limit;
	private Integer page;
	private Integer start;

	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
		config.put(java.util.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
	}
	public void list() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		String payPhone = ServletRequestUtils.getStringParameter(request, "payPhone", null);
		
		try {
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
				java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
				
				Page<TOpenOrder> orderPage = new Page<TOpenOrder>();
				//设置默认排序方式
				orderPage.setPageSize(limit);
				orderPage.setPageNo(page);
				
				orderPage = openOrderManager.getOrderPageByProperty(orderPage, sellerId, payPhone, start, end);
				
				long nums = orderPage.getTotalCount();
				StringBuilder jstr = new StringBuilder("{");
				jstr.append("total:" + String.valueOf(nums) + ",");
				jstr.append("orders:");

				List<TOpenOrder> openOrders = orderPage.getResult();
				jstr.append(JSON.toJSONString(openOrders, config));
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
//			Page<TOpenOrder> orderPage = new Page<TOpenOrder>();
//			//设置默认排序方式
//			orderPage.setPageSize(limit);
//			orderPage.setPageNo(page);
//			if (!orderPage.isOrderBySetted()) {
//				orderPage.setOrderBy("id");
//				orderPage.setOrder(Page.DESC);
//			}
//			if (!Utils.isEmpty(phone)) {
//				orderPage = openOrderManager.getOrderPageByPhone(orderPage, phone);
//			}
			
//			long nums = orderPage.getTotalCount() < 0 ? 0 :orderPage.getTotalCount();
			long nums = openOrderManager.getOrderPageByPhoneFromMongoCount(phone);
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + String.valueOf(nums) + ",");
			jstr.append("orders:");
			
//			List<TOpenOrder> openOrders = orderPage.getResult();
//			for (TOpenOrder tOpenOrder : openOrders) {
//				TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//				tOpenOrder.setSellerName(tOpenSeller.getName());
//			}
			List<MongoTOpenOrder> openOrders = openOrderManager.getOrderPageByPhoneFromMongo(page, limit, phone);
			for (MongoTOpenOrder mongoTOpenOrder : openOrders) {
				TOpenSeller tOpenSeller = openSellerManager.get(mongoTOpenOrder.getSellerId());
				mongoTOpenOrder.setSellerName(tOpenSeller.getName());
			}
			jstr.append(JSON.toJSONString(openOrders, config));
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public String export() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", -1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		String payPhone = ServletRequestUtils.getStringParameter(request, "payPhone", null);
		try {
			TOpenSeller tOpenSeller = openSellerManager.getEntity(sellerId);
			String sellerName = null;
			if (!Utils.isEmpty(tOpenSeller)) {
				sellerName = tOpenSeller.getName();
			}
			
			if (startTime != null && endTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
				java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
				
				
				response.setHeader("Content-disposition", "attachment;filename=qdtj.xlsx");
				response.setContentType("application/msexcel");
				OutputStream outputStream = response.getOutputStream();
				
				// 第一步，创建一个webbook，对应一个Excel文件
//				HSSFWorkbook wb = new HSSFWorkbook();
				XSSFWorkbook wb = new XSSFWorkbook();
				// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
//				HSSFSheet sheet = wb.createSheet(sellerName == null ? "所有订单" : sellerName);
				XSSFSheet sheet = wb.createSheet(sellerName == null ? "所有订单" : sellerName);
				// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
//				HSSFRow row = sheet.createRow((int) 0);
				XSSFRow row = sheet.createRow((int) 0);
				// 第四步，创建单元格，并设置值表头 设置表头居中
//				HSSFCellStyle style = wb.createCellStyle();
				XSSFCellStyle style = wb.createCellStyle();
				style.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
				
//				HSSFCell cell = row.createCell((short) 0);
				XSSFCell cell = row.createCell((short) 0);
				cell.setCellValue(new XSSFRichTextString("平台订单号"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 1);
				cell.setCellValue(new XSSFRichTextString("渠道订单号"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 2);
				cell.setCellValue(new XSSFRichTextString("IMSI"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 3);
				cell.setCellValue(new XSSFRichTextString("App名称"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 4);
				cell.setCellValue(new XSSFRichTextString("资费"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 5);
				cell.setCellValue(new XSSFRichTextString("创建时间"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 6);
				cell.setCellValue(new XSSFRichTextString("状态"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 7);
				cell.setCellValue(new XSSFRichTextString("支付时间"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 8);
				cell.setCellValue(new XSSFRichTextString("支付号码"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 9);
				cell.setCellValue(new XSSFRichTextString("省份"));
				cell.setCellStyle(style);
				
				cell = row.createCell((short) 10);
				cell.setCellValue(new XSSFRichTextString("是否扣量"));
				cell.setCellStyle(style);
				
				List<TOpenOrder> list = openOrderManager.getOrderList(sellerId, payPhone, start, end);
				// 第五步，写入实体数据 实际应用中这些数据从数据库得到，
				for (int j = 0; j < list.size(); j++) {
					row = sheet.createRow((int)j + 1);
					TOpenOrder openOrder = list.get(j);
					row.createCell((short) 0).setCellValue(new XSSFRichTextString(openOrder.getOrderId()));
					row.createCell((short) 1).setCellValue(new XSSFRichTextString(openOrder.getOutTradeNo()));
					row.createCell((short) 2).setCellValue(new XSSFRichTextString(openOrder.getImsi()));
					row.createCell((short) 3).setCellValue(new XSSFRichTextString(openOrder.getSubject()));
					row.createCell((short) 4).setCellValue(openOrder.getFee());
					row.createCell((short) 5).setCellValue(new XSSFRichTextString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(openOrder.getCreateTime())));
					String status = null;
					if ("1".equals(openOrder.getStatus())) {
						status = "未支付";
					} else if ("3".equals(openOrder.getStatus())) {
						status = "成功";
					} else if ("4".equals(openOrder.getStatus())) {
						status = "失败";
					}
					row.createCell((short) 6).setCellValue(new XSSFRichTextString(status));
					row.createCell((short) 7).setCellValue(new XSSFRichTextString(Utils.isEmpty(openOrder.getPayTime()) ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(openOrder.getCreateTime())));
					row.createCell((short) 8).setCellValue(new XSSFRichTextString(openOrder.getPayPhone()));
					row.createCell((short) 9).setCellValue(new XSSFRichTextString(openOrder.getProvince()));
					String reduce = null;
					if (0 == openOrder.getReduce()) {
						reduce = "未扣量";
					} else if (1 == openOrder.getReduce()) {
						reduce = "已扣量";
					}
					row.createCell((short) 10).setCellValue(new XSSFRichTextString(reduce));
				}
				
				wb.write(outputStream);
				outputStream.close();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
	
	public void conversionRateList() {
//		Integer sellerId = 1;
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
		Page<TOpenOrderConversionrate> page = openOrderConversionRateManager.findListByProperties(sellerId);
		
		long nums = page.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("rates:");

		List<TOpenOrderConversionrate> list = page.getResult();
		Collections.sort(list);
		jstr.append(JSON.toJSONString(list));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
//	public void provinceCount() {
//		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
//		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
//		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
//		
//		try {
//			Map<String, Integer> mapSucc = new HashMap<String, Integer>();
//			Map<String, Integer> mapFail = new HashMap<String, Integer>();
//			if (startTime != null && endTime != null) {
//				startTime = startTime.replace("T", " ");
//				endTime = endTime.replace("T", " ");
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				Date start = new Date(sdf.parse(startTime).getTime());
//				Date end = new Date(sdf.parse(endTime).getTime());
//				
//				List<TOpenOrder> list = openOrderManager.getOrderList(sellerId, null, start, end);
//				for (TOpenOrder tOpenOrder : list) {
//					String province = tOpenOrder.getProvince();
//					String status = tOpenOrder.getStatus();
//					if ("3".equals(status)) {
//						if (Utils.isEmpty(province)) {//无省份成功
//							Integer succ = mapSucc.get("其他");
//							if (Utils.isEmpty(succ)) {//无初始化数据								
//								mapSucc.put("其他", 1);
//							} else {
//								mapSucc.put("其他", succ + 1);
//							}
//						} else {//有省份成功
//							Integer succ = mapSucc.get(province);
//							if (Utils.isEmpty(succ)) {//无初始化数据								
//								mapSucc.put(province, 1);
//							} else {
//								mapSucc.put(province, succ + 1);
//							}
//						}
//					} else if ("4".equals(status)) {
//						if (Utils.isEmpty(province)) {//无省份失败
//							Integer fail = mapFail.get("其他");
//							if (Utils.isEmpty(fail)) {//无初始化数据								
//								mapFail.put("其他", 1);
//							} else {
//								mapFail.put("其他", fail + 1);
//							}
//						} else {//有省份成功
//							Integer fail = mapFail.get(province);
//							if (Utils.isEmpty(fail)) {//无初始化数据								
//								mapFail.put(province, 1);
//							} else {
//								mapFail.put(province, fail + 1);
//							}
//						}
//					}
//				}
//				List<String> provinceList = Arrays.asList(new String[]{"河北","山西","辽宁","吉林","黑龙江","江苏","浙江","安徽","福建","江西","山东","河南",
//						"湖北","湖南","广东","海南","四川","贵州","云南","陕西","甘肃","青海","内蒙古","广西","西藏","宁夏","新疆","北京","天津","上海","重庆","其他"});
//				
//				StringBuilder jstr = new StringBuilder("{");
//				jstr.append("total:" + String.valueOf(provinceList.size()) + ",");
//				jstr.append("provinces:");
//
//				JSONArray array = new JSONArray();
//				for (String province : provinceList) {
//					JSONObject jsonObject = new JSONObject();
//					Integer succ = mapSucc.get(province) == null ? 0 : mapSucc.get(province);
//					Integer fail = mapFail.get(province) == null ? 0 : mapFail.get(province);
//					jsonObject.put("province", province);
//					jsonObject.put("succ", succ);
//					jsonObject.put("fail", fail);
//					jsonObject.put("count", succ+fail);
//					
//					DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
//					//mr/mo转化率
//					float f = 0;
//					if (succ == 0) {
//						f = 0;
//					} else {
//						f = (float)succ/(succ+fail);
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
//					jsonObject.put("rate", fString);
//					array.add(jsonObject);
//				}
//				jstr.append(array.toString());
//				jstr.append("}");
//				StringUtil.printJson(response, jstr.toString());
//				
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
	
	public void provinceCount() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		try {
			if (startTime != null && endTime != null) {
				startTime = startTime.replace("T", " ");
				endTime = endTime.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
				java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
				
				Map<String, String> mapSucc = openOrderManager.getProvinceCountBySellerId(sellerId, start, end, "3");
				Map<String, String> mapFail = openOrderManager.getProvinceCountBySellerId(sellerId, start, end, "4");
				
				List<String> provinceList = Arrays.asList(new String[]{"河北","山西","辽宁","吉林","黑龙江","江苏","浙江","安徽","福建","江西","山东","河南",
						"湖北","湖南","广东","海南","四川","贵州","云南","陕西","甘肃","青海","内蒙古","广西","西藏","宁夏","新疆","北京","天津","上海","重庆","其他"});
				
				StringBuilder jstr = new StringBuilder("{");
				jstr.append("total:" + String.valueOf(provinceList.size()) + ",");
				jstr.append("provinces:");

				JSONArray array = new JSONArray();
				for (String province : provinceList) {
					Integer succ = 0;
					Integer fail = 0;
					Integer fee = 0;
					
					String succ_string = mapSucc.get(province);
					if (!Utils.isEmpty(succ_string)) {
						JSONObject json_succ = JSONObject.parseObject(succ_string);
						succ = json_succ.getInteger("count");//成功
						fee = json_succ.getInteger("fee") / 100;//信息费
					}
					String fail_string = mapFail.get(province);
					if (!Utils.isEmpty(fail_string)) {
						JSONObject json_fail = JSONObject.parseObject(fail_string);
						fail = json_fail.getInteger("count");//mr
					}
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("province", province);
					jsonObject.put("succ", succ);
					jsonObject.put("fail", fail);
					jsonObject.put("count", succ+fail);
					jsonObject.put("fee", fee);
					
					DecimalFormat df = new DecimalFormat("0.0");//格式化小数，不足的补0
					//mr/mo转化率
					float f = 0;
					if (succ == 0) {
						f = 0;
					} else {
						f = (float)succ/(succ+fail);
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
					jsonObject.put("rate", fString);
					array.add(jsonObject);
				}
				jstr.append(array.toString());
				jstr.append("}");
				StringUtil.printJson(response, jstr.toString());
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
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
			Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
			List<TOpenSeller> openSellerList = openSellerManager.findAllOpenSellerList(userType);
			for (TOpenSeller tOpenSeller : openSellerList) {
				int sellerId = tOpenSeller.getId();
				String sellerName = tOpenSeller.getName();
				
				Map<Integer, String> noPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "1", null);
				Map<Integer, String> succPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", null);
				Map<Integer, String> failPayMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "4", null);
				Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
				allStatusMap.putAll(noPayMap);
				allStatusMap.putAll(succPayMap);
				allStatusMap.putAll(failPayMap);
				Map<Integer, String> succPayReduceMap = openOrderManager.mapReduceAppIds(sellerId, start, end, "3", 0);//扣量后的成功数据
				
				for (Integer appId : allStatusMap.keySet()) {
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
					
					JSONObject report = new JSONObject();
					report.put("sellerId", sellerId);
					report.put("sellerName", sellerName);
					report.put("appId", appId);
					report.put("appName", appName);
					report.put("req", orderReqInt);
					report.put("succ", succInt);
					report.put("succReduce", succReduceInt);
					report.put("fail", failInt);
					report.put("noPay", noPayInt);
					report.put("fee", feeInt);
					report.put("feeReduce", feeReduceInt);
					report.put("users_num", users_num);
					report.put("users_succ_num", users_succ_num);
					report.put("rate", fString);
					report.put("reqRate", reqfString);
					jsonArray.add(report);
				}
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
	
	public void provinceDetail() {
		Integer sellerId = ServletRequestUtils.getIntParameter(request, "sellerId", 1);
		Integer appId = ServletRequestUtils.getIntParameter(request, "appId", -1);
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
			
			Map<String, String> noPayMap = openOrderManager.mapReduceProvinceBySellerIdAndAppId(sellerId, appId, start, end, "1");
			Map<String, String> succPayMap = openOrderManager.mapReduceProvinceBySellerIdAndAppId(sellerId, appId, start, end, "3");
			Map<String, String> failPayMap = openOrderManager.mapReduceProvinceBySellerIdAndAppId(sellerId, appId, start, end, "4");
			
			List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
			"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆",null});
			
			for (String province : provinceList) {
				Integer noPayInt = null;
				Integer noPayUser = null;
				if (noPayMap.size() == 0) {
					noPayInt = 0;
					noPayUser = 0;
				} else {
					JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(province));
					noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
					noPayUser = noPayJson == null ? 0 : noPayJson.getInteger("user");//未支付用户数
					if (noPayInt == null) {
						noPayInt = 0;
					}
					if (noPayUser == null) {
						noPayUser = 0;
					}
				}
				Integer failInt = null;
				Integer failUser = null;
				if (failPayMap.size() == 0) {
					failInt = 0;
					failUser = 0;
				} else {
					JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(province));
					failInt = failPayJson == null ? 0 : failPayJson.getInteger("count");//失败支付请求数
					failUser = failPayJson == null ? 0 : failPayJson.getInteger("user");//失败支付请求数
					if (failInt == null) {
						failInt = 0;
					}
					if (failUser == null) {
						failUser = 0;
					}
				}
				Integer succInt = null;
				Integer feeInt = null;
				Integer succUser = null;
				if (succPayMap.size() == 0) {
					succInt = 0;
					feeInt = 0;
					succUser = 0;
				} else {
					JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(province));
					succInt = succPayJson == null ? 0 : succPayJson.getInteger("count");//成功支付请求数
					if (succInt == null) {
						succInt = 0;
					}
					feeInt = succPayJson == null ? 0 : succPayJson.getInteger("fee");//成功计费金额
					if (feeInt == null) {
						feeInt = 0;
					}
					feeInt = feeInt/100;//fee转化成单位元
					succUser = succPayJson == null ? 0 : succPayJson.getInteger("user");//成功用户数
				}
				
				Integer orderReqInt = noPayInt+failInt+succInt;
				Integer users_num = noPayUser+failUser+succUser;
				
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
				
				JSONObject report = new JSONObject();
				if (province == null) {
					province = "其他";
				}
				report.put("province", province);
				report.put("req", orderReqInt);
				report.put("succ", succInt);
				report.put("fail", failInt);
				report.put("noPay", noPayInt);
				report.put("fee", feeInt);
				report.put("users_num", users_num);
				report.put("users_succ_num", succUser);
				report.put("rate", fString);
				report.put("reqRate", reqfString);
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
