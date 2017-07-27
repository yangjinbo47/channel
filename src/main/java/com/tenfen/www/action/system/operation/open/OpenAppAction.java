package com.tenfen.www.action.system.operation.open;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.springside.modules.orm.hibernate.HibernateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenAppLimit;
import com.tenfen.entity.operation.open.TOpenMerchant;
import com.tenfen.entity.operation.open.TOpenProductInfo;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenMerchantManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenProductInfoManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;

public class OpenAppAction extends SimpleActionSupport{

	private static final long serialVersionUID = -7838742375073569223L;
	
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private OpenMerchantManager openMerchantManager;
	@Autowired
	private OpenProductInfoManager openProductInfoManager;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	
	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	public void list() {
		String appName = ServletRequestUtils.getStringParameter(request, "appName", null);

		Page<TOpenApp> appPage = new Page<TOpenApp>();
		//设置默认排序方式
		appPage.setPageSize(limit);
		appPage.setPageNo(page);
		if (!appPage.isOrderBySetted()) {
			appPage.setOrderBy("id");
			appPage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(appName)) {
			appPage = openAppManager.findAppPage(appPage, userType);
		} else {
			appPage = openAppManager.findAppPage(appName, appPage, userType);
		}
		
		long nums = appPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("apps:");
		List<TOpenApp> appList = appPage.getResult();
		
//		for (TOpenApp tOpenApp : appList) {
//			TOpenMerchant tOpenMerchant = openMerchantManager.get(tOpenApp.getMerchantId());
//			tOpenApp.setMerchantShowName(tOpenMerchant.getMerchantShowName());
//		}
//		jstr.append(JSON.toJSONString(appList));
		
		JSONArray jsonArray = new JSONArray();
		for (TOpenApp tOpenApp : appList) {
			JSONObject json = new JSONObject();
			json.put("id", tOpenApp.getId());
			json.put("name", tOpenApp.getName());
			json.put("merchantId", tOpenApp.getMerchantId());
			json.put("appKey", tOpenApp.getAppKey());
			json.put("appSecret", tOpenApp.getAppSecret());
			json.put("clientId", tOpenApp.getClientId());
			json.put("callbackUrl", tOpenApp.getCallbackUrl());
//			json.put("excludeArea", tOpenApp.getExcludeArea());
//			json.put("excludeAreaArray", tOpenApp.getExcludeAreaArray());
			json.put("companyShow", tOpenApp.getCompanyShow());
			TOpenMerchant tOpenMerchant = openMerchantManager.get(tOpenApp.getMerchantId());
			json.put("merchantShowName", tOpenMerchant.getMerchantShowName());
			jsonArray.add(json);
		}
		jstr.append(jsonArray.toString());
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 查询app关联的产品
	 */
	public void productsOfApp() {
		Integer appId = ServletRequestUtils.getIntParameter(request, "appId", -1);

		TOpenApp tOpenApp = openAppManager.getEntity(appId);
		
		List<TOpenProductInfo> productInfos = new ArrayList<TOpenProductInfo>();
		if (!Utils.isEmpty(tOpenApp)) {
			productInfos = tOpenApp.getProductList();
		}
		int size = productInfos.size();

		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("productInfos:");
		jstr.append(JSON.toJSONString(productInfos));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	/**
	 * 未赋值给app的产品
	 */
	public void getUnallocateProducts() {
		Integer merchantId = ServletRequestUtils.getIntParameter(request, "merchantId", -1);
		String productIds = ServletRequestUtils.getStringParameter(request, "productIds", "");
		
		String[] idsArr = productIds.split(",");
		List<TOpenProductInfo> productInfos = new ArrayList<TOpenProductInfo>();
        for (String string : idsArr) {
        	if (!Utils.isEmpty(string)) {
        		TOpenProductInfo tOpenProductInfo = openProductInfoManager.get(Integer.parseInt(string));
        		productInfos.add(tOpenProductInfo);
			}
		}
		
		List<TOpenProductInfo> merchantProducts = openProductInfoManager.findOpenProductInfoByMerchantId(merchantId);
		merchantProducts.removeAll(productInfos);
		
		int size = merchantProducts.size();
		
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(size) + ",");
		jstr.append("productInfos:");
		jstr.append(JSON.toJSONString(merchantProducts));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String appName = ServletRequestUtils.getStringParameter(request, "name", null);
			Integer merchantId = ServletRequestUtils.getIntParameter(request, "merchantId");
			String appKey = ServletRequestUtils.getStringParameter(request, "appKey", null);
			String appSecret = ServletRequestUtils.getStringParameter(request, "appSecret", null);
			String callbackUrl = ServletRequestUtils.getStringParameter(request, "callbackUrl", null);
			String clientId = ServletRequestUtils.getStringParameter(request, "clientId", null);
//			String[] excludeAreas = ServletRequestUtils.getStringParameters(request, "excludeAreaArray");
			Integer companyShow = ServletRequestUtils.getIntParameter(request, "companyShow", -1);
			
//			StringBuilder sb = new StringBuilder();
//			for (String string : excludeAreas) {
//				sb.append(string).append(",");
//			}
//			String excludeArea = sb.deleteCharAt(sb.length() - 1).toString();

			if (id == -1) {
				TOpenApp tOpenApp = new TOpenApp();
				tOpenApp.setName(appName);
				tOpenApp.setMerchantId(merchantId);
				tOpenApp.setAppKey(appKey);
				tOpenApp.setAppSecret(appSecret);
				tOpenApp.setClientId(clientId);
				tOpenApp.setCallbackUrl(callbackUrl);
				tOpenApp.setCompanyShow(companyShow);
//				tOpenApp.setExcludeArea(excludeArea);
				openAppManager.save(tOpenApp);
			} else {//更新
				TOpenApp tOpenApp = openAppManager.getEntity(id);
				if (tOpenApp != null) {
					tOpenApp.setName(appName);
					tOpenApp.setMerchantId(merchantId);
					tOpenApp.setAppKey(appKey);
					tOpenApp.setAppSecret(appSecret);
					tOpenApp.setClientId(clientId);
					tOpenApp.setCallbackUrl(callbackUrl);
					tOpenApp.setCompanyShow(companyShow);
//					tOpenApp.setExcludeArea(excludeArea);
					openAppManager.save(tOpenApp);
				}
			}

			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 保存app-product关联关系
	 */
	public void saveAppProductRelation() {
		Integer appId = ServletRequestUtils.getIntParameter(request, "appId", -1);
		String productIds = ServletRequestUtils.getStringParameter(request, "productIds", "");
		
		try {
			String[] idsArr = productIds.split(",");
			List<Integer> productids = new ArrayList<Integer>();
			for (String string : idsArr) {
				if (!Utils.isEmpty(string)) {
					productids.add(Integer.parseInt(string));
				}
			}
			
			TOpenApp tOpenApp = openAppManager.getEntity(appId);
			
			HibernateUtils.mergeByCheckedIds(tOpenApp.getProductList(), productids, TOpenProductInfo.class);
			openAppManager.save(tOpenApp);
			
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
		}
	}
	
	/**
	 * 删除
	 */
	public void delete() {
		try {
			String ids = ServletRequestUtils.getStringParameter(getRequest(), "ids");
			if (!Utils.isEmpty(ids)) {
				String[] idsArr = ids.split(",");
				for (String id : idsArr) {
					openAppManager.delete(Integer.parseInt(id));
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			logger.error(e.getMessage(), e);
		}
	}
	
	private final List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
			"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆"});
	/**
	 * 获取渠道省份日月限
	 */
	public void limitlist() {
		Integer appId = ServletRequestUtils.getIntParameter(request, "appId", -1);

		List<TOpenAppLimit> limits = openAppManager.findAppLimits(appId);
		if (limits.size() == 0) {
			limits = new ArrayList<TOpenAppLimit>();
			for (String prov : provinceList) {
				TOpenAppLimit tOpenSellerLimit = new TOpenAppLimit();
				tOpenSellerLimit.setAppId(appId);
				tOpenSellerLimit.setProvince(prov);
				tOpenSellerLimit.setDayLimit(-1);
				tOpenSellerLimit.setMonthLimit(-1);
				tOpenSellerLimit.setUserDayLimit(-1);
				tOpenSellerLimit.setUserMonthLimit(-1);
				tOpenSellerLimit.setReduce(0);
				limits.add(tOpenSellerLimit);
			}
		}
		long nums = limits.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("appLimits:");

		jstr.append(JSON.toJSONString(limits));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void saveAppLimit() {
		String limitStr = ServletRequestUtils.getStringParameter(request, "limits", "");
		try {
			List<TOpenAppLimit> limits = JSON.parseArray(limitStr, TOpenAppLimit.class);
			for (TOpenAppLimit tOpenAppLimit : limits) {
				//查找存在对象
				TOpenAppLimit data = openAppManager.findAppLimitByProperty(tOpenAppLimit.getAppId(), tOpenAppLimit.getProvince());
				if (!Utils.isEmpty(data)) {
					data.setAppId(tOpenAppLimit.getAppId());
					data.setProvince(tOpenAppLimit.getProvince());
					data.setDayLimit(tOpenAppLimit.getDayLimit());
					data.setMonthLimit(tOpenAppLimit.getMonthLimit());
					data.setUserDayLimit(tOpenAppLimit.getUserDayLimit());
					data.setUserMonthLimit(tOpenAppLimit.getUserMonthLimit());
					data.setReduce(tOpenAppLimit.getReduce());
					openAppManager.saveAppLimit(data);
				} else {
					openAppManager.saveAppLimit(tOpenAppLimit);
				}
			}
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.success(MSG.SAVEFAILURE));
		}
	}
	
	public void report() {
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
			
			//mapreduce
			Map<Integer, String> noPayMap = openOrderManager.mapReduceSellerIds(appId, start, end, "1", null);
			Map<Integer, String> succPayMap = openOrderManager.mapReduceSellerIds(appId, start, end, "3", null);
			Map<Integer, String> failPayMap = openOrderManager.mapReduceSellerIds(appId, start, end, "4", null);
			Map<Integer, String> allStatusMap = new HashMap<Integer, String>();
			allStatusMap.putAll(noPayMap);
			allStatusMap.putAll(succPayMap);
			allStatusMap.putAll(failPayMap);
			Map<Integer, String> succPayReduceMap = openOrderManager.mapReduceSellerIds(appId, start, end, "3", 0);//扣量后的成功数据
			
			JSONArray jsonArray = new JSONArray();
			for (Integer sellerId : allStatusMap.keySet()) {
				Integer noPayInt = null;
				if (noPayMap.size() == 0) {
					noPayInt = 0;
				} else {
					JSONObject noPayJson = JSONObject.parseObject(noPayMap.get(sellerId));
					noPayInt = noPayJson == null ? 0 : noPayJson.getInteger("count");//未支付请求数
					if (noPayInt == null) {
						noPayInt = 0;
					}
				}
				Integer failInt = null;
				if (failPayMap.size() == 0) {
					failInt = 0;
				} else {
					JSONObject failPayJson = JSONObject.parseObject(failPayMap.get(sellerId));
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
					JSONObject succPayJson = JSONObject.parseObject(succPayMap.get(sellerId));
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
					JSONObject succPayJson = JSONObject.parseObject(succPayReduceMap.get(sellerId));
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
				
				TOpenSeller tOpenSeller = openSellerManager.get(sellerId);
				String sellerName = tOpenSeller.getName();
				
				JSONObject report = new JSONObject();
				report.put("sellerName", sellerName);
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
			}//end for map
			
			StringBuilder jstr = new StringBuilder("{");
			jstr.append("total:" + jsonArray.size() + ",");
			jstr.append("report:");
			jstr.append(jsonArray.toJSONString());
			jstr.append("}");
			StringUtil.printJson(response, jstr.toString());
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 导出excel
	 */
	public void export() {
		Integer appId = ServletRequestUtils.getIntParameter(request, "appId", -1);
		String startTime = ServletRequestUtils.getStringParameter(request, "startTime", null);
		String endTime = ServletRequestUtils.getStringParameter(request, "endTime", null);
		
		try {
			TOpenApp tOpenApp = openAppManager.get(appId);
			if (!Utils.isEmpty(tOpenApp)) {
				String appName = tOpenApp.getName();
				
				if (startTime != null && endTime != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					java.sql.Date start = new java.sql.Date(sdf.parse(startTime).getTime());
					java.sql.Date end = new java.sql.Date(sdf.parse(endTime).getTime());
					
					response.setHeader("Content-disposition", "attachment;filename=app_export.xlsx");
					response.setContentType("application/msexcel");
					OutputStream outputStream = response.getOutputStream();
					
					// 第一步，创建一个webbook，对应一个Excel文件
					XSSFWorkbook wb = new XSSFWorkbook();
					// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
					XSSFSheet sheet = wb.createSheet(appName == null ? "订单统计" : appName);
					// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
					XSSFRow row = sheet.createRow((int) 0);
					// 第四步，创建单元格，并设置值表头 设置表头居中
					XSSFCellStyle style = wb.createCellStyle();
					style.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
					
					XSSFCell cell = row.createCell((short) 0);
					cell.setCellValue(new XSSFRichTextString("省份"));
					cell.setCellStyle(style);
					
					Map<Integer, Map<String, String>> map = new HashMap<Integer, Map<String,String>>();
					Map<String, String> sellerMap = null;
					//根据appId查询渠道
					//title
					int i = 1;
					List<TOpenSeller> openSellers = openSellerManager.findSellerByAppId(appId);
					for (TOpenSeller tOpenSeller : openSellers) {
						String sellerName = tOpenSeller.getName();
						if (sellerName.indexOf("（") != -1) {
							sellerName = sellerName.substring(0, sellerName.indexOf("（"));
						}
						cell = row.createCell((short) i);
						cell.setCellValue(new XSSFRichTextString(sellerName));
						cell.setCellStyle(style);
						i++;
						
//						sellerMap = openOrderManager.getProvinceCountBySellerId(tOpenSeller.getId(), start, end, "3");//bug
						sellerMap = openOrderManager.mapReduceProvinceBySellerIdAndAppId(tOpenSeller.getId(), appId, start, end, "3");
						map.put(tOpenSeller.getId(), sellerMap);
					}
					cell = row.createCell((short) i);
					cell.setCellValue(new XSSFRichTextString("总计"));
					
					//body
					List<String> provinceList = Arrays.asList(new String[] { "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东", 
							"河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆", "北京", "天津", "上海", "重庆"});
					
					int j = 1;//省份
					for (String prov : provinceList) {
						row = sheet.createRow((int) j);
						row.createCell((short) 0).setCellValue(new XSSFRichTextString(prov));
						int k = 1;
						int provSum = 0;
						for (TOpenSeller tOpenSeller : openSellers) {
							Map<String, String> provMap = map.get(tOpenSeller.getId());
							String jsonStr = provMap.get(prov);//取当前省份的汇总数据
							JSONObject jsonObject = JSONObject.parseObject(jsonStr);
							if (!Utils.isEmpty(jsonObject)) {						
								Integer fee = jsonObject.getInteger("fee");
								fee = fee / 100;
								provSum += fee;
								row.createCell((short) k).setCellValue(fee);
							} else {
								row.createCell((short) k).setCellValue(0);
							}
							k++;
						}
						row.createCell((short) k).setCellValue(provSum);
						j++;
					}
					
					//foot
					row = sheet.createRow((int) j);
					row.createCell((short) 0).setCellValue(new XSSFRichTextString("总计"));
					int l = 1;
					for (TOpenSeller tOpenSeller : openSellers) {
						Map<String, String> provMap = map.get(tOpenSeller.getId());
						int sellerSum = 0;
						for(String prov : provMap.keySet()) {
							String jsonStr = provMap.get(prov);
							JSONObject jsonObject = JSONObject.parseObject(jsonStr);
							if (!Utils.isEmpty(jsonObject)) {
								Integer fee = jsonObject.getInteger("fee");
								fee = fee / 100;
								sellerSum += fee;
							}
						}
						row.createCell((short) l).setCellValue(sellerSum);
						l++;
					}
					
					wb.write(outputStream);
					outputStream.close();
				}
			}
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
