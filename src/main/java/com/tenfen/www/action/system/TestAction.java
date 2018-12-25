package com.tenfen.www.action.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.tenfen.bean.operation.OpenDailyBean;
import com.tenfen.bean.operation.PackageDailyBean;
import com.tenfen.bean.operation.SmsDailyBean;
import com.tenfen.bean.system.SystemProperty;
import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TBlackList;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.open.TOpenApp;
import com.tenfen.entity.operation.open.TOpenMailer;
import com.tenfen.entity.operation.open.TOpenMailgroup;
import com.tenfen.entity.operation.open.TOpenOrder;
import com.tenfen.entity.operation.open.TOpenOrderHistory;
import com.tenfen.entity.operation.open.TOpenSeller;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.PushPackageChannel;
import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.entity.operation.pack.TPushMailer;
import com.tenfen.entity.operation.pack.TPushMailgroup;
import com.tenfen.entity.operation.pack.TPushSeller;
import com.tenfen.entity.operation.sms.TSmsApp;
import com.tenfen.entity.operation.sms.TSmsMailer;
import com.tenfen.entity.operation.sms.TSmsMailgroup;
import com.tenfen.entity.operation.sms.TSmsOrder;
import com.tenfen.entity.operation.sms.TSmsOrderHistory;
import com.tenfen.entity.operation.sms.TSmsSeller;
import com.tenfen.entity.system.ImsiMdnRelation;
import com.tenfen.mongoEntity.MongoTOpenOrder;
import com.tenfen.mongoEntity.MongoTOrder;
import com.tenfen.mongoEntity.MongoTSmsOrder;
import com.tenfen.util.CTUtil;
import com.tenfen.util.HttpClientUtils;
import com.tenfen.util.LogUtil;
import com.tenfen.util.RegExp;
import com.tenfen.util.SendMailUtil;
import com.tenfen.util.SftpClientUtil;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.BASE64;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.dao.operation.open.OpenOrderDao;
import com.tenfen.www.dao.operation.open.OpenOrderHistoryDao;
import com.tenfen.www.dao.operation.pack.OrderDao;
import com.tenfen.www.dao.operation.sms.SmsOrderDao;
import com.tenfen.www.dao.operation.sms.SmsOrderHistoryDao;
import com.tenfen.www.mongodao.MongoTOpenOrderDao;
import com.tenfen.www.mongodao.MongoTOrderDao;
import com.tenfen.www.mongodao.MongoTSmsOrderDao;
import com.tenfen.www.service.account.AccountManager;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.open.OpenAppManager;
import com.tenfen.www.service.operation.open.OpenMailManager;
import com.tenfen.www.service.operation.open.OpenOrderConversionRateManager;
import com.tenfen.www.service.operation.open.OpenOrderHistoryManager;
import com.tenfen.www.service.operation.open.OpenOrderManager;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageChannelManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushMailManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;
import com.tenfen.www.service.operation.sms.SmsAppManager;
import com.tenfen.www.service.operation.sms.SmsMailManager;
import com.tenfen.www.service.operation.sms.SmsOrderHistoryManager;
import com.tenfen.www.service.operation.sms.SmsOrderManager;
import com.tenfen.www.service.operation.sms.SmsProductInfoManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;
import com.tenfen.www.service.system.ImsiMdnRelationManager;
import com.tenfen.www.service.system.SystemConfigManager;
import com.tenfen.www.service.system.VisitLogManager;
import com.tenfen.www.service.system.VisitLogTmpManager;
import com.tenfen.www.util.TokenService;
import com.tenfen.www.util.TokenService.TokenParam;
import com.tenfen.www.util.sendToBj.SendOpenHisToBJ;
import com.tenfen.www.util.sendToBj.SendOpenToBJ;
import com.tenfen.www.util.sendToBj.SendSmsHisToBJ;
import com.tenfen.www.util.sendToBj.SendSmsToBJ;
import com.tenfen.www.util.tyyd.Base64;
import com.tenfen.www.util.tyydclient.HttpSendClient;
import com.tenfen.www.util.tyydclient.HttpSendRequest;
import com.tenfen.www.util.tyydclient.HttpSendResponse;

public class TestAction extends SimpleActionSupport {

	private static final long serialVersionUID = 3205177939227033736L;
	private static final int POOL_SIZE = 200;// 线程池的容量
	ExecutorService exe = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private BlackListManager blackListManager;
	@Autowired
	private MobileAreaManager mobileAreaManager;
	@Autowired
	private CacheFactory cacheFactory;
	@Autowired
	private OrderManager orderManager;
	@Autowired
	private PackageChannelManager packageChannelManager;
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private SystemProperty systemProperty;
	@Autowired
	private OpenOrderManager openOrderManager;
	@Autowired
	private OpenAppManager openAppManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private OpenOrderConversionRateManager openOrderConversionRateManager;
	@Autowired
	private VisitLogTmpManager visitLogTmpManager;
	@Autowired
	private VisitLogManager visitLogManager;
	@Autowired
	private OpenOrderHistoryManager openOrderHistoryManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private SmsOrderManager smsOrderManager;
	@Autowired
	private SmsAppManager smsAppManager;
	@Autowired
	private OpenOrderHistoryDao openOrderHistoryDao;
	@Autowired
	private SmsOrderHistoryDao smsOrderHistoryDao;
	@Autowired
	private ImsiMdnRelationManager imsiMdnRelationManager;
	@Autowired
	private SmsOrderDao smsOrderDao;
	@Autowired
	private SmsOrderHistoryManager smsOrderHistoryManager;
	@Autowired
	private MongoTOpenOrderDao mongoTOpenOrderDao;
	@Autowired
	private MongoTSmsOrderDao mongoTSmsOrderDao;
	@Autowired
	private OpenOrderDao openOrderDao;
	@Autowired
	private SystemConfigManager systemConfigManager;
	@Autowired
	private SmsProductInfoManager smsProductInfoManager;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private MongoTOrderDao mongoTOrderDao;
	@Autowired
	private OpenMailManager openMailManager;
	@Autowired
	private SmsMailManager smsMailManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	@Autowired
	private PushMailManager pushMailManager;

	//放置空间数据到ftp
//	public String execute() {
//		FileOutputStream fos = null;
//		OutputStreamWriter osw = null;
//		BufferedWriter bw = null;
//		try {
//			//放置点播数据
////			Integer merchantId = 3;//十分-朗天
//			List<Integer> merchantIdList = Arrays.asList(new Integer[]{3,12,14});//点播商户
//			List<Integer> merchantPackageIdList = Arrays.asList(new Integer[]{13});//包月商户
//			
//			Calendar calendar = Calendar.getInstance();
//			//格式化时间
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
//			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			SimpleDateFormat sdfTxt = new SimpleDateFormat("yyyy-MM-dd_HH");
//			//获取结束时间
//			String endString = sdf.format(calendar.getTime()) + ":00:00";
//			Date endDate = sdfSql.parse(endString);
//			java.sql.Date end = new java.sql.Date(endDate.getTime());
//			
//			calendar.add(Calendar.HOUR_OF_DAY, -1);
//			//获取当日时间区间
//			String startString = sdf.format(calendar.getTime()) + ":00:00";
//			Date startDate = sdfSql.parse(startString);
//			java.sql.Date start = new java.sql.Date(startDate.getTime());
//			
//			String tySpaceDataLocalDir = systemProperty.getTySpaceDataLocalDir();
//			fos = new FileOutputStream(tySpaceDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt", true);
//			osw = new OutputStreamWriter(fos,"UTF-8");
//			bw = new BufferedWriter(osw);
//			
//			for (Integer merchantId : merchantIdList) {
//				List<TOpenOrder> orderList = openOrderManager.getOrderListByMerchantId(merchantId, start, end);
//				for (TOpenOrder tOpenOrder : orderList) {
//					StringBuffer sb = new StringBuffer();
//					sb.append(tOpenOrder.getOutTradeNo()).append(",");
//					sb.append(tOpenOrder.getSubject()).append(",");
//					sb.append(tOpenOrder.getCreateTime()).append(",");
//					sb.append(tOpenOrder.getFee()).append(",");
//					sb.append(tOpenOrder.getStatus()).append(",");
//					sb.append(tOpenOrder.getPayTime()).append(",");
//					sb.append(tOpenOrder.getImsi()).append(",");
//					sb.append(tOpenOrder.getSellerId()).append(",");
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					sb.append(tOpenSeller.getName()).append(System.getProperty("line.separator"));
//					bw.write(sb.toString());
//				}
//			}
//			
//			bw.flush();
//			bw.close();
//			osw.close();
//			fos.close();
//			
//			//放置包月数据
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
//					sb.append(tOpenOrder.getPayPhone()).append(",");
//					sb.append(tOpenOrder.getSellerId()).append(",");
//					TOpenSeller tOpenSeller = openSellerManager.get(tOpenOrder.getSellerId());
//					sb.append(tOpenSeller.getName()).append(System.getProperty("line.separator"));
//					bw.write(sb.toString());
//				}
//			}
//			
//			bw.flush();
//			bw.close();
//			osw.close();
//			fos.close();
//			
//			//放置点播数据到北京ftp
//			SftpClientUtil sftp = new SftpClientUtil(systemProperty.getTySpaceDataIp(), Integer.parseInt(systemProperty.getTySpaceDataPort()), systemProperty.getTySpaceDataUserName(), systemProperty.getTySpaceDataPassword());
//			sftp.connect();
//			sftp.upload(systemProperty.getTySpaceDataRemoteDir(), tySpaceDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt");
//			
//			//放置包月数据到北京ftp
//			sftp.upload(systemProperty.getTySpacePackageDataRemoteDir(), tySpacePackageDataLocalDir+File.separator+sdfTxt.format(new Date())+".txt");
//			sftp.disconnect();
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return null;
//	}
	
	//根据号码查省份导出
//	private static Log visitLog = LogFactory.getLog("visitLog");
//	public String execute() {
//		try {
//			File file = new File("D:\\phone.txt");
//			
//			List<String> list = FileUtils.readLines(file);
//			for (String phoneNum : list) {
//				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNum);
//				if (mobileArea == null) {					
//					visitLog.info("");
//				} else {
////					String province = mobileArea.getProvince();
//					String city = mobileArea.getCity();
//					visitLog.info(city);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
//	public class DaoThread implements Runnable {
//		private MobileAreaDao mobileAreaDao;
//		private TMobileArea tMobileArea;
//
//		public DaoThread(MobileAreaDao mobileAreaDao,TMobileArea tMobileArea) {
//			this.mobileAreaDao = mobileAreaDao;
//			this.tMobileArea = tMobileArea;
//		}
//		
//		@Override
//		public void run() {
//			mobileAreaDao.save(tMobileArea);
//		}
//	}
	
	//黑名单导入
	public String execute() {
		try {
			File file = new File("/home/yangjinbo/black.txt");
			
			List<String> list = FileUtils.readLines(file);
			for (String phoneNum : list) {
				String p = phoneNum.trim();
				boolean b = Utils.checkCellPhone(p);
				if (b) {
					exe.execute(new DaoThread(blackListManager, p));
				} else {
					System.out.println(phoneNum+"非手机号码");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public class DaoThread implements Runnable {
		private BlackListManager blackListManager;
		private String phoneNum;

		public DaoThread(BlackListManager blackListManager,String phoneNum) {
			this.blackListManager = blackListManager;
			this.phoneNum = phoneNum;
		}
		
		@Override
		public void run() {
			if (Utils.checkCellPhone(phoneNum)) {
				boolean b = blackListManager.isBlackList(phoneNum);
				System.out.println(phoneNum+"是否存在："+b);
				if (!b) {
					TBlackList tBlackList = new TBlackList();
					tBlackList.setPhoneNum(phoneNum);
					blackListManager.save(tBlackList);
				}
			}
		}
	}
	
	//导入访问日志
//	public String execute() {
//		try {
//			Calendar calendar = Calendar.getInstance();
////			calendar.add(Calendar.DATE, -1);
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			String visitPath = systemProperty.getClientVisitLogDir() + File.separator + "visit_" +sdf.format(calendar.getTime()) + ".log";
//			File file = new File(visitPath);
//			List<String> list = FileUtils.readLines(file, "UTF-8");
//			for (String string : list) {
//				if (!Utils.isEmpty(string)) {
//					String log[] = string.split("\\|\\|");
//					String imsi = log[0];
//					String phoneNum = log[1];
//					String clientVersion = log[2];
//					String province = log[3];
//					String ua = log[4];
//					String date = log[5];
//					
//					TVisitLogTmp tVisitLog = new TVisitLogTmp();
//					tVisitLog.setImsi(imsi);
//					tVisitLog.setPhoneNum(phoneNum);
//					tVisitLog.setClientVersion(clientVersion);
//					tVisitLog.setProvince(province);
//					tVisitLog.setUserAgent(ua);
//					tVisitLog.setVisitTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
//					exe.execute(new DaoThread(visitLogTmpManager, tVisitLog));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public class DaoThread implements Runnable {
//		private VisitLogTmpManager visitLogTmpManager;
//		private TVisitLogTmp tVisitLogTmp;
//
//		public DaoThread(VisitLogTmpManager visitLogTmpManager, TVisitLogTmp tVisitLogTmp) {
//			this.visitLogTmpManager = visitLogTmpManager;
//			this.tVisitLogTmp = tVisitLogTmp;
//		}
//
//		@Override
//		public void run() {
//			visitLogTmpManager.save(tVisitLogTmp);
//		}
//	}
	
	//导入H码表
//	public String execute() {
//		try {
//			File file = new File("/home/channel/province.txt");
//			
//			List<String> list = FileUtils.readLines(file,"UTF-8");
//			for (String string : list) {
//				String[] array = string.split(",");
//				String first = array[0];
//				String mid = array[1];
//				String address = array[2];
//				String province = array[3];
//				String city = array[4];
//				String brand = null;
//				if ("133".equals(first) || "153".equals(first) || "180".equals(first) || "181".equals(first) || "189".equals(first) || "177".equals(first) || "173".equals(first)) {
//					brand = "中国电信";
//				} else if ("130".equals(first) || "131".equals(first) || "132".equals(first) || "155".equals(first) || "156".equals(first) || "145".equals(first) || "185".equals(first) || "186".equals(first) || "175".equals(first) || "176".equals(first)) {
//					brand = "中国联通";
//				} else if ("134".equals(first) || "135".equals(first) || "136".equals(first) || "137".equals(first) || "138".equals(first) || "139".equals(first) || "150".equals(first) || "151".equals(first) || "152".equals(first) || "158".equals(first) || "159".equals(first) || "182".equals(first) || "183".equals(first) || "184".equals(first) || "157".equals(first) || "187".equals(first) || "188".equals(first) || "147".equals(first) || "178".equals(first)) {
//					brand = "中国移动";
//				} else if ("170".equals(first)) {
//					brand = "虚拟运营商";
//				}
//				
//				exe.execute(new DaoThread(mobileAreaManager, first, mid, address, province, city, brand));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public class DaoThread implements Runnable {
//		private MobileAreaManager mobileAreaManager;
//		private String first;
//		private String mid;
//		private String address;
//		private String province;
//		private String city;
//		private String brand;
//	
//		public DaoThread(MobileAreaManager mobileAreaManager, String first, String mid, String address, String province, String city, String brand) {
//			this.mobileAreaManager = mobileAreaManager;
//			this.first = first;
//			this.mid = mid;
//			this.address = address;
//			this.province = province;
//			this.city = city;
//			this.brand = brand;
//		}
//	
//		@Override
//		public void run() {
//			List<TMobileArea> list = mobileAreaManager.getMobileAreaList(first, mid);
//			if (list.size() == 0) {
//				System.out.println("号段不存在：first:"+first+" middle:"+mid);
//				TMobileArea tMobileArea = new TMobileArea();
//				tMobileArea.setFirstNum(first);
//				tMobileArea.setMiddleNum(mid);
//				tMobileArea.setAddress(address);
//				tMobileArea.setProvince(province);
//				tMobileArea.setCity(city);
//				tMobileArea.setBrand(brand);
//				mobileAreaManager.save(tMobileArea);
//			}
//		}
//	}
	
	
	
	
	//导入模拟数据
//	public String execute() {
//		try {
//			int count = 130000;//信息费
//			int year = 2015;
//			int month = 12;//当前月
//			int day = 31;//当月天数
//			int sellerId = 10;//8-MM 9-游戏 10-移动动漫
//			int appId = 1;
//			int merchantId = 1;
//			
//			int avg = count/day;//平均一天推广费
//			int floats = avg/10;//浮动范围
//			
//			int sum = 0;
//			for (int i = 0; i < day; i++) {
//				int random_float = new Random().nextInt(floats);//随机浮动数
//				boolean b = new Random().nextBoolean();//随机正负，表示平均值的上偏还是下偏
//				int result = 0;
//				if (b) {
//					result = avg - random_float;
//				} else {
//					result = avg + random_float;
//				}
//				if (i < day -1) {
//					sum += result;
//				}
//				
//				int dayFee = 0;//当日应该插入的金额
//				if (i<day-1) {
//					dayFee = result;
//				} else {
//					dayFee = count - sum;
//				}
//				
//				//按dayFee逐级扣减，扣完为止
//				dayFee = dayFee * 100;
//				while (dayFee >= 100) {
//					int fee = 0;
//					if (dayFee == 100) {
//						fee = dayFee;
//					} else {
//						fee = createFee();
//					}
//					if (dayFee - fee >= 0) {
//						//按转化率30%进行插入操作
//						double rate = new Random().nextDouble();
//						TSmsOrder tSmsOrder = new TSmsOrder();
//						tSmsOrder.setSellerId(sellerId);
//						tSmsOrder.setAppId(appId);
//						tSmsOrder.setMerchantId(merchantId);
//						tSmsOrder.setCreateImsi(createImsi());
//						String outTradeNo = UUID.randomUUID().toString().trim().substring(0,8);
//						tSmsOrder.setOutTradeNo(outTradeNo);
//						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//格式化时间
//						SimpleDateFormat sdfOrderId = new SimpleDateFormat("yyyyMMddHHmmssSSS");//格式化时间
//						String time = createTime(year, month, i+1);
//						Date createTime = sdf.parse(time);
//						tSmsOrder.setCreateTime(createTime);
//						long l = createTime.getTime() + new Random().nextInt(30000);
//						Date payTime = new Date(l);
//						tSmsOrder.setPayTime(payTime);
//						String orderSeq = sdfOrderId.format(createTime) + Math.round(Math.random() * 1000);
//						tSmsOrder.setOrderId(orderSeq);
//						if (sellerId==8) {
//							tSmsOrder.setSubject("移动MM");
//						} else if (sellerId==9) {
//							tSmsOrder.setSubject("移动游戏");
//						} else if (sellerId==10) {
//							tSmsOrder.setSubject("移动动漫");
//						} else if (sellerId==11) {
//							tSmsOrder.setSubject("移动PC");
//						} else if (sellerId==12) {
//							tSmsOrder.setSubject("联通沃商店");
//						} else if (sellerId==13) {
//							tSmsOrder.setSubject("电信天翼空间");
//						}
//						
//						if (rate < 0.4) {//成功
//							dayFee = dayFee - fee;
//							tSmsOrder.setStatus("3");
//						} else if ((0.4 < rate) && (rate < 0.8)) {//失败
//							tSmsOrder.setStatus("4");
//						} else {//未支付
//							tSmsOrder.setStatus("1");
//						}
//						tSmsOrder.setProductType(1);
//						tSmsOrder.setFee(fee);
//						tSmsOrder.setSenderNumber("");
//						tSmsOrder.setMsgContent("");
//						
//						exe.execute(new DaoThread(smsOrderManager, tSmsOrder));
//					}
//					
//				}
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return null;
//	}
//	
//	private static String createImsi() {
//		String imsi = "";
//		imsi = imsi + "4600";
//		for (int i = 0; i < 11; i++) {
//			int s = new Random().nextInt(10);
//			imsi = imsi+s;
//		}
//		return imsi;
//	}
//	
//	private static String createTime(int year, int month, int day){
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.YEAR, year);
//		calendar.set(Calendar.MONTH, month-1);
//		calendar.set(Calendar.DAY_OF_MONTH, day);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//格式化时间
//		String ymd = sdf.format(calendar.getTime());
//		int ms = new Random().nextInt(86400000);
//		Date date = new Date(ms);
//		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss.SSS");
//		String time = sdfTime.format(date);
//		return ymd + " "+ time;
//	}
//	
//	private static Integer createFee() {
//		int result = 0;
//		int r = new Random().nextInt(5);
//		switch (r) {
//		case 0:
//			result = 100;
//			break;
//		case 1:
//			result = 200;
//			break;
//		case 2:
//			result = 400;
//			break;
//		case 3:
//			result = 600;
//			break;
//		case 4:
//			result = 800;
//			break;
//		default:
//			result = 100;
//			break;
//		}
//		return result;
//	}
//	
//	public class DaoThread implements Runnable {
//		
//		private SmsOrderManager smsOrderManager;
//		private TSmsOrder tSmsOrder;
//		
//		public DaoThread(SmsOrderManager smsOrderManager, TSmsOrder tSmsOrder) {
//			this.smsOrderManager = smsOrderManager;
//			this.tSmsOrder = tSmsOrder;
//		}
//
//		@Override
//		public void run() {
//			smsOrderManager.save(tSmsOrder);
//		}
//		
//	}
	
	public String execute1() {
		//mongodb 数据查询
//		try {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
//			String startString = "2016-04-26 00:00:00";
//			String endString = "2016-04-27 00:00:00";
//			Date start = sdf.parse(startString);
//			java.sql.Date startTime = new java.sql.Date(start.getTime());
//			Date end = sdf.parse(endString);
//			java.sql.Date endTime = new java.sql.Date(end.getTime());
//			Criteria criteria = Criteria.where("seller_id").is(8).and("create_time").gte(startTime).lt(endTime);
//			Query query = new Query(criteria);
//			List<MongoTOpenOrder> list = mongoTOpenOrderDao.findList(query);
//			System.out.println("总数："+list.size());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		//导入某天的数据至mongo
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
			String startString = "2018-03-29 00:00:00";
			String endString = "2018-04-03 00:00:00";
			Date start = sdf.parse(startString);
			java.sql.Date startTime = new java.sql.Date(start.getTime());
			Date end = sdf.parse(endString);
			java.sql.Date endTime = new java.sql.Date(end.getTime());
//			List<TSmsOrder> smslist = smsOrderDao.getOrderList(startTime, endTime);
//			for (TSmsOrder tSmsOrder : smslist) {
//				System.out.println("插入id："+tSmsOrder.getId());
//				exe.execute(new MongoSmsThread(tSmsOrder));
//			}
			List<TOpenOrder> openlist = openOrderDao.getOrderList(startTime, endTime);
			for (TOpenOrder tOpenOrder : openlist) {
				System.out.println("插入id："+tOpenOrder.getId());
				exe.execute(new MongoOpenThread(tOpenOrder));
			}
//			List<TOpenOrderHistory> openlist = openOrderHistoryDao.getOrderList(1,startTime, endTime);
//			for (TOpenOrderHistory tOpenOrder : openlist) {
//				System.out.println("插入id："+tOpenOrder.getId());
//				exe.execute(new MongoOpenHisThread(tOpenOrder));
//			}
//			List<TOrder> list = orderDao.getOrderList(startTime, endTime);
//			for (TOrder tOrder : list) {
//				System.out.println("插入id："+tOrder.getId());
//				exe.execute(new MongoPackageThread(tOrder));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public class MongoOpenThread implements Runnable {
		
		private MongoTOpenOrder mongoTOpenOrder;
		
		public MongoOpenThread(TOpenOrder entity) {
			try {
				mongoTOpenOrder = new MongoTOpenOrder();
				mongoTOpenOrder.setImsi(entity.getImsi());
				mongoTOpenOrder.setOrderId(entity.getOrderId());
				mongoTOpenOrder.setOutTradeNo(entity.getOutTradeNo());
				mongoTOpenOrder.setSellerId(entity.getSellerId());
				mongoTOpenOrder.setAppId(entity.getAppId());
				mongoTOpenOrder.setMerchantId(entity.getMerchantId());
				mongoTOpenOrder.setSubject(entity.getSubject());
				mongoTOpenOrder.setSenderNumber(entity.getSenderNumber());
				mongoTOpenOrder.setMsgContent(entity.getMsgContent());
				mongoTOpenOrder.setCreateTime(entity.getCreateTime());
				mongoTOpenOrder.setFee(entity.getFee());
				mongoTOpenOrder.setStatus(entity.getStatus());
				mongoTOpenOrder.setPayPhone(entity.getPayPhone());
				mongoTOpenOrder.setPayTime(entity.getPayTime());
				mongoTOpenOrder.setProvince(entity.getProvince());
				mongoTOpenOrder.setUnsubscribeTime(entity.getUnsubscribeTime());
				mongoTOpenOrder.setReduce(entity.getReduce());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTOpenOrderDao.saveAndUpdate(mongoTOpenOrder);
		}
	}
	
	public class MongoOpenHisThread implements Runnable {
		
		private MongoTOpenOrder mongoTOpenOrder;
		
		public MongoOpenHisThread(TOpenOrderHistory entity) {
			try {
				mongoTOpenOrder = new MongoTOpenOrder();
				mongoTOpenOrder.setImsi(entity.getImsi());
				mongoTOpenOrder.setOrderId(entity.getOrderId());
				mongoTOpenOrder.setOutTradeNo(entity.getOutTradeNo());
				mongoTOpenOrder.setSellerId(entity.getSellerId());
				mongoTOpenOrder.setAppId(entity.getAppId());
				mongoTOpenOrder.setMerchantId(entity.getMerchantId());
				mongoTOpenOrder.setSubject(entity.getSubject());
				mongoTOpenOrder.setSenderNumber(entity.getSenderNumber());
				mongoTOpenOrder.setMsgContent(entity.getMsgContent());
				mongoTOpenOrder.setCreateTime(entity.getCreateTime());
				mongoTOpenOrder.setFee(entity.getFee());
				mongoTOpenOrder.setStatus(entity.getStatus());
				mongoTOpenOrder.setPayPhone(entity.getPayPhone());
				mongoTOpenOrder.setPayTime(entity.getPayTime());
				mongoTOpenOrder.setProvince(entity.getProvince());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTOpenOrderDao.saveAndUpdate(mongoTOpenOrder);
		}
	}
	
	public class MongoSmsThread implements Runnable {
		
		private MongoTSmsOrder mongoTSmsOrder;
		
		public MongoSmsThread(TSmsOrder tSmsOrder) {
			try {
				mongoTSmsOrder = new MongoTSmsOrder();
				mongoTSmsOrder.setImsi(tSmsOrder.getImsi());
				mongoTSmsOrder.setOrderId(tSmsOrder.getOrderId());
				mongoTSmsOrder.setOutTradeNo(tSmsOrder.getOutTradeNo());
				mongoTSmsOrder.setLinkId(tSmsOrder.getLinkId());
				mongoTSmsOrder.setSellerId(tSmsOrder.getSellerId());
				mongoTSmsOrder.setAppId(tSmsOrder.getAppId());
				mongoTSmsOrder.setMerchantId(tSmsOrder.getMerchantId());
				mongoTSmsOrder.setSubject(tSmsOrder.getSubject());
				mongoTSmsOrder.setSenderNumber(tSmsOrder.getSenderNumber());
				mongoTSmsOrder.setMsgContent(tSmsOrder.getMsgContent());
				mongoTSmsOrder.setMoNumber(tSmsOrder.getMoNumber());
				mongoTSmsOrder.setMoMsg(tSmsOrder.getMoMsg());
				mongoTSmsOrder.setCreateTime(tSmsOrder.getCreateTime());
				mongoTSmsOrder.setFee(tSmsOrder.getFee());
				mongoTSmsOrder.setProductType(tSmsOrder.getProductType());
				mongoTSmsOrder.setStatus(tSmsOrder.getStatus());
				mongoTSmsOrder.setPayPhone(tSmsOrder.getPayPhone());
				mongoTSmsOrder.setPayTime(tSmsOrder.getPayTime());
				mongoTSmsOrder.setProvince(tSmsOrder.getProvince());
				mongoTSmsOrder.setReduce(tSmsOrder.getReduce());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTSmsOrderDao.saveAndUpdate(mongoTSmsOrder);
		}
	}
	
	public class MongoPackageThread implements Runnable {
		
		private MongoTOrder mongoTOrder;
		
		public MongoPackageThread(TOrder tOrder) {
			try {
				mongoTOrder = new MongoTOrder();
				mongoTOrder.setTradeId(tOrder.getTradeId());
				mongoTOrder.setOutTradeNo(tOrder.getOutTradeNo());
				mongoTOrder.setImsi(tOrder.getImsi());
				mongoTOrder.setPhoneNum(tOrder.getPhoneNum());
				mongoTOrder.setSellerId(tOrder.getSellerId());
				mongoTOrder.setPushId(tOrder.getPushId());
				mongoTOrder.setFee(tOrder.getFee());
				mongoTOrder.setStatus(tOrder.getStatus());
				mongoTOrder.setCreateTime(tOrder.getCreateTime());
				mongoTOrder.setName(tOrder.getName());
//				mongoTOrder.setChannel(tOrder.getChannel());
				mongoTOrder.setProvince(tOrder.getProvince());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}

		@Override
		public void run() {
			mongoTOrderDao.saveAndUpdate(mongoTOrder);
		}
	}
	
//	public static void main(String[] args) {
//		try {
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("order_no", "555");
//			jsonObject.put("out_trade_no", "555");
//			jsonObject.put("phone", "15372098311");
//			jsonObject.put("fee", "500");
//			jsonObject.put("status", "3");
//			jsonObject.put("msg", "m91111");
//			
//			LogUtil.log("sendNotify:"+jsonObject.toString());
////			String res = HttpClientUtils.postJson("http://58.215.139.208:9090/hj/pg/sh_079.jsp", jsonObject.toString());
////			System.out.println(res);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	//检查在信投诉
	public String execute3() {
		try {
			File file = new File("/home/channel/phone.txt");
			
			List<String> list = FileUtils.readLines(file);
			for (String phoneNum : list) {
				List<MongoTSmsOrder> orderList = mongoTSmsOrderDao.getOrderListByPhone(1, 10, phoneNum);
				if (orderList.size() > 0) {
					Integer sellerId = orderList.get(0).getSellerId();
					TSmsSeller seller = smsSellerManager.get(sellerId);
					LogUtil.log(phoneNum+","+seller.getName());
				} else {
					LogUtil.log(phoneNum+",无");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//查看朗天是否
	public String executeLangtian() {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			File file = new File("/home/channel/phone.txt");
			fos = new FileOutputStream("/home/channel/l.txt", true);
			osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);
			
			List<String> list = FileUtils.readLines(file);
			for (String imsi : list) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
				String startString = "2016-08-01 00:00:00";
				String endString = "2016-09-01 00:00:00";
				Date start = sdf.parse(startString);
				java.sql.Date startTime = new java.sql.Date(start.getTime());
				Date end = sdf.parse(endString);
				java.sql.Date endTime = new java.sql.Date(end.getTime());
				
				Query query = new Query();
				Criteria criteria = Criteria.where("imsi").is(imsi).and("seller_id").is(5);
				query.addCriteria(criteria);
				List<MongoTOpenOrder> orderList = mongoTOpenOrderDao.findList(query);
				if (orderList.size() > 0) {
					String phone = orderList.get(0).getPayPhone();
					bw.write(imsi+","+phone+System.getProperty("line.separator"));
				} else {
					bw.write(imsi+",无"+System.getProperty("line.separator"));
				}
				bw.flush();
			}
			
			bw.close();
			osw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//更新空白省份
	public String executeProvince() {
		String hql = "from TOpenOrder t where t.payPhone is not null and t.province is null";
		Session session = openOrderDao.getSession();
		org.hibernate.Query query = session.createQuery(hql);
		List<TOpenOrder> list = query.list();
		System.out.println(list.size());
		for (TOpenOrder tOpenOrder : list) {
			String phone = tOpenOrder.getPayPhone();
			if (!phone.startsWith("460")) {				
				TMobileArea mobileArea = mobileAreaManager.getMobileArea(phone);
				if (mobileArea == null) {
					System.out.println(phone+"省份未找到");
				} else {
					String province = mobileArea.getProvince();
					tOpenOrder.setProvince(province);
					openOrderManager.save(tOpenOrder);
				}
			}
		}
		return null;
	}
	
//	public String executecmcctd() {
//		try {
//			String msgType = "UnsubscribeReq";
//			String payCode = "300008371001";
//			String appId = "600000008371";
//			String appKey = "2FR0MTX42E6VTRWL";
//
//			File file = new File("/home/channel/phone.txt");
//			List<String> list = FileUtils.readLines(file, "UTF-8");
//			for (String phone : list) {
//				String userMobile = BASE64.encode(phone.getBytes());
//				String sign = MD5.getMD5(msgType+appId+appKey+userMobile);
//				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Request><MsgType>"+msgType+"</MsgType><AppID>"+appId+"</AppID><PayCode>"+payCode+"</PayCode><UserMobile>"+userMobile+"</UserMobile><Signature>"+sign.toUpperCase()+"</Signature></Request>";
//				
//				String res = HttpClientUtils.postJson("http://wap.dm.10086.cn/capability/capacc", xml);
//				System.out.println(phone + ":" +res);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
//	public String execute() {
//		try {
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.DATE, -1);
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			//获取当日时间区间
//			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化时间
////			String startString = sdf.format(calendar.getTime()) + " 00:00:00";
//			String startString = "2018-01-01 00:00:00";
//			Date startDate = sdfSql.parse(startString);
//			java.sql.Date start = new java.sql.Date(startDate.getTime());
//			
////			String endString = sdf.format(calendar.getTime()) + " 23:59:59";
//			String endString = "2018-02-01 00:00:00";
//			Date endDate = sdfSql.parse(endString);
//			java.sql.Date end = new java.sql.Date(endDate.getTime());
//			
////			List<TOpenOrder> openlist = openOrderDao.getSuccOrderList(start, end);
////			for (TOpenOrder entity : openlist) {
////				Integer sellerId = entity.getSellerId();
////				TOpenSeller openSeller = openSellerManager.get(sellerId);
////				Integer companyShow = openSeller.getCompanyShow();
////				if ("3".equals(entity.getStatus()) && companyShow==1) {
////					exe.execute(new SendOpenToBJ(entity));
////				}
////			}
//			
////			List<TSmsOrder> smslist = smsOrderDao.getSuccOrderList(start, end);
////			for (TSmsOrder entity : smslist) {
////				Integer sellerId = entity.getSellerId();
////				TSmsSeller smsSeller = smsSellerManager.get(sellerId);
////				Integer companyShow = smsSeller.getCompanyShow();
////				if ("3".equals(entity.getStatus()) && companyShow==1) {
////					exe.execute(new SendSmsToBJ(entity));
////				}
////			}
//			
//			List<TOpenOrderHistory> openlist = openOrderHistoryDao.getSuccOrderList(start, end);
//			for (TOpenOrderHistory entity : openlist) {
//				Integer sellerId = entity.getSellerId();
//				TOpenSeller openSeller = openSellerManager.get(sellerId);
//				Integer companyShow = openSeller.getCompanyShow();
//				if ("3".equals(entity.getStatus()) && companyShow==1) {
//					exe.execute(new SendOpenHisToBJ(entity));
//				}
//			}
////			List<TSmsOrderHistory> smslist = smsOrderHistoryDao.getSuccOrderList(start, end);
////			for (TSmsOrderHistory entity : smslist) {
////				Integer sellerId = entity.getSellerId();
////				TSmsSeller smsSeller = smsSellerManager.get(sellerId);
////				Integer companyShow = smsSeller.getCompanyShow();
////				if ("3".equals(entity.getStatus()) && companyShow==1) {
////					exe.execute(new SendSmsHisToBJ(entity));
////				}
////			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	
}

