package com.tenfen.www.util.tyyd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.TMobileArea;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.json.JSONObject;
import com.tenfen.www.service.operation.BlackListManager;
import com.tenfen.www.service.operation.MobileAreaManager;
import com.tenfen.www.service.operation.pack.OrderManager;
import com.tenfen.www.service.operation.pack.PackageManager;

public class TyreadUtil {
	
	private BlackListManager blackListManager;
	private OrderManager orderManager;
	private CacheFactory cacheFactory;
	private PackageManager packageManager;
	private MobileAreaManager mobileAreaManager;
//	private VisitLogTmpManager visitLogTmpManager;
//	private static Log visitLog = LogFactory.getLog("visitLog");
	
	public TyreadUtil(CacheFactory cacheFactory, BlackListManager blackListManager, OrderManager orderManager, PackageManager packageManager, MobileAreaManager mobileAreaManager) {
		this.cacheFactory = cacheFactory;
		this.blackListManager = blackListManager;
		this.orderManager = orderManager;
		this.packageManager = packageManager;
		this.mobileAreaManager = mobileAreaManager;
	}
	
	/**
	 * 检测是否是黑名单用户
	 * @param phoneNum
	 * @return
	 */
	public boolean checkPhoneIsBlack(String phoneNum) {
		boolean isExist = blackListManager.isBlackList(phoneNum);
		return isExist;
	}
	
	/**
	*@功能：检查是否包月
	*@author BOBO
	*@date Apr 18, 2014
	 */
	public boolean checkBaoyue(String phoneNum) {
		boolean b = false;
		try {
			ICacheClient iCacheClient = cacheFactory.getCommonCacheClient();
			String key = "check_baoyue_"+phoneNum;
			if (phoneNum != null && Utils.checkCellPhone(phoneNum)) {
				String result = (String)iCacheClient.getCache(key);
				if (result == null) {
					Calendar calendarStart = Calendar.getInstance();
					calendarStart.add(Calendar.MONTH, -2);
					calendarStart.set(Calendar.DAY_OF_MONTH, 1);
					java.sql.Date start = new java.sql.Date(calendarStart.getTimeInMillis());
					
					Calendar calendarEnd = Calendar.getInstance();
					calendarEnd.set(Calendar.DAY_OF_MONTH,1);
					calendarEnd.add(Calendar.MONTH, 1);
					java.sql.Date end = new java.sql.Date(calendarEnd.getTimeInMillis());
					
					Long baoyueNum = orderManager.getBaoyueCount(phoneNum, start, end);
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("phoneNum", phoneNum);
					jsonObject.put("packageNum", baoyueNum);
					result = jsonObject.toString();
					
					if (baoyueNum > 0) {
						b = true;
					}
					iCacheClient.setCache(key, result, CacheFactory.HOUR * 3);
				} else {
					JSONObject json = new JSONObject(result);
					if (json.isNull("packageNum") == false) {
						Integer baoyueNum = (Integer)json.getInt("packageNum");
						if (baoyueNum > 0) {
							b = true;
						}
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return b;
	}
	
//	public List<PushPackage> getPackageList(String channel, Integer price) {
//		List<PushPackage> packageList = new ArrayList<PushPackage>();
//		try {
//			String arrayString = packageManager.findPackageList(channel, price);
//			
//			JSONArray jsonArray = new JSONArray(arrayString);
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject json = (JSONObject) jsonArray.get(i);
//				PushPackage pushPackage = new PushPackage();
//				if (json.isNull("id") == false) {
//					pushPackage.setId(json.getInt("id"));
//				}
//				if (json.isNull("packageName") == false) {
//					pushPackage.setPackageName(json.getString("packageName"));
//				}
//				if (json.isNull("packageUrl") == false) {						
//					pushPackage.setPackageUrl(json.getString("packageUrl"));
//				}
//				if (json.isNull("packageSentence") == false) {						
//					pushPackage.setPackageSentence(json.getString("packageSentence"));
//				}
//				if (json.isNull("excludeArea") == false) {						
//					pushPackage.setExcludeArea(json.getString("excludeArea"));
//				}
//				if (json.isNull("recChannel") == false) {
//					pushPackage.setRecChannel(json.getString("recChannel"));
//				}
//				if (json.isNull("status") == false) {
//					pushPackage.setStatus(json.getInt("status"));
//				}
//				if (json.isNull("packageLimit") == false) {
//					pushPackage.setPackageLimit(json.getInt("packageLimit"));
//				}
//				if (json.isNull("packageToday") == false) {
//					pushPackage.setPackageToday(json.getInt("packageToday"));
//				}
//				if (json.isNull("price") == false) {
//					pushPackage.setPrice(json.getInt("price"));
//				}
//
//				packageList.add(pushPackage);
//			}
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return packageList;
//	}
//	public List<PushPackage> getPackageList(String channel, Integer price) {
//		List<PushPackage> packageList = null;
//		try {
//			packageList = packageManager.findPackageList(channel, price);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		
//		return packageList;
//	}
	
	/**
	 * 获取产品剩余量
	 */
	public Integer getProductLimit(Integer pushPackageId) {
		Integer limit = 0;
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			limit = (Integer)mc.getCache("limit_"+pushPackageId+"_"+sdf.format(new Date()));
			if (limit == null) {
				PushPackage pushPackage = packageManager.get(pushPackageId);
				int package_limit = pushPackage.getPackageLimit();
				int package_today = pushPackage.getPackageToday();
				limit = package_limit-package_today < 0 ? 0 : package_limit-package_today;
				mc.setCache("limit_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return limit;
	}
	
	public void clearProductLimit(Integer pushPackageId) {
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String key = "limit_"+pushPackageId+"_"+sdf.format(new Date());
			boolean b = mc.deleteCache(key);
			LogUtil.log("#### MemcacheClient.deleteCache(" + key + "),result="+ b +";");
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	*@功能：根据号码查地域
	*@author BOBO
	*@date Jun 16, 2013
	 */
	public String searchAreaByPhone(String phoneNum) {
		String province = null;
		try {
			phoneNum = Utils.mobilePhoneFormat(phoneNum);
			if (!Utils.checkCellPhone(phoneNum)) {
				return null;
			}
			
			TMobileArea mobileArea = mobileAreaManager.getMobileArea(phoneNum);
			if (mobileArea != null) {
				province = mobileArea.getProvince();
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return province;
	}
	
//	public void visitlog(String imsi, String phoneNum, String channel, String province, String ua) {
//		try {
//			TVisitLogTmp tVisitLogTmp = new TVisitLogTmp();
//			tVisitLogTmp.setImsi(imsi);
//			tVisitLogTmp.setPhoneNum(phoneNum);
//			tVisitLogTmp.setClientVersion(channel);
//			tVisitLogTmp.setProvince(province);
//			tVisitLogTmp.setUserAgent(ua);
//			tVisitLogTmp.setVisitTime(new Date());
//			visitLogTmpManager.save(tVisitLogTmp);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//	}
}
