package com.tenfen.job.daily;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenfen.cache.CacheFactory;
import com.tenfen.cache.services.ICacheClient;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.util.LogUtil;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.operation.open.OpenSellerManager;
import com.tenfen.www.service.operation.pack.PackageManager;
import com.tenfen.www.service.operation.pack.PushSellerManager;
import com.tenfen.www.service.operation.sms.SmsSellerManager;

public class SetZeroJob {
	
	@Autowired
	private PackageManager packageManager;
	@Autowired
	private PushSellerManager pushSellerManager;
	@Autowired
	private OpenSellerManager openSellerManager;
	@Autowired
	private SmsSellerManager smsSellerManager;
	@Autowired
	private CacheFactory cacheFactory;
	
//	private static Log visitLog = LogFactory.getLog("visitLog");

	public void execute() {
		try {
			ICacheClient mc = cacheFactory.getCommonCacheClient();
			
			LogUtil.log("开始任务:包月包每日推广量置0");
			List<PushPackage> list = packageManager.findAllPackageList(Constants.USER_TYPE.ALL.getValue());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			for (PushPackage pushPackage : list) {
				pushPackage.setPackageToday(0);
				packageManager.save(pushPackage);
				
				//重置产品当日剩余量缓存
				int limit = pushPackage.getPackageLimit();
//				mc.setCache("limit_"+pushPackage.getRecChannel()+"_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//包月剩余量
				mc.setCache("limit_"+pushPackage.getId()+"_"+sdf.format(new Date()), limit, CacheFactory.DAY);//重置包月剩余量
			}
			
			//包月渠道每日推广量置0
			pushSellerManager.resetPackageToday();
			//能力开放渠道每日推广量置0
			openSellerManager.resetAppToday();
			//短代渠道每日推广量置0
			smsSellerManager.resetAppToday();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
