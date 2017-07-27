package com.tenfen.www.action.system.operation.pack;

import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.util.LogUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;

public class OrderAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	
	private Integer limit;
	private Integer page;
	private Integer start;

	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
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
				Date start = new Date(sdf.parse(startTime).getTime());
				Date end = new Date(sdf.parse(endTime).getTime());
				
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
				Date start = new Date(sdf.parse(startTime).getTime());
				Date end = new Date(sdf.parse(endTime).getTime());
				
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
