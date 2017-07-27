package com.tenfen.www.action.system.operation.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.entity.operation.pack.PushPackage;
import com.tenfen.entity.operation.pack.PushPackageLimit;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.pack.PackageManager;

public class PushPackageAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;

	@Autowired
	private PackageManager packageManager;
//	@Autowired
//	private PackageChannelManager packageChannelManager;

	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer(
				"yyyy-MM-dd HH:mm:ss"));
	}
	
//	private String key = "push_package_list_";

	public void list() {
		String packageName = ServletRequestUtils.getStringParameter(request, "packageName", null);
		String channelName = ServletRequestUtils.getStringParameter(request, "channelName", null);

		Page<PushPackage> packagePage = new Page<PushPackage>();
		//设置默认排序方式
		packagePage.setPageSize(limit);
		packagePage.setPageNo(page);
		if (!packagePage.isOrderBySetted()) {
			packagePage.setOrderBy("id");
			packagePage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
//		Integer userType = (Integer)getMemcacheAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(packageName) && Utils.isEmpty(channelName)) {
			packagePage = packageManager.getPackageList(packagePage, userType);
		} else {
			packagePage = packageManager.getPackagesByName(packageName, channelName, userType, packagePage);
		}
		
		List<PushPackage> packageList = packagePage.getResult();
//		for (PushPackage pushPackage : packageList) {
//			PushPackageChannel pushPackageChannel = packageChannelManager.findByClientVersion(pushPackage.getRecChannel());
//			pushPackage.setChannelName(pushPackageChannel.getChannelName());
//		}
		long nums = packageList.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("packages:");
		jstr.append(JSON.toJSONString(packageList, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}

//	public String edit() {
//		try {
//			Integer packageId = ServletRequestUtils.getIntParameter(getRequest(), "id");
//			PushPackage pushPackage = packageDao.get(packageId);
//			setRequestAttribute("pushPackage", pushPackage);
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//		return "input";
//	}

//	public void submit() {
//
//		try {
//			Integer packageId = ServletRequestUtils.getIntParameter(request, "packageId", -1);
//			String packageName = ServletRequestUtils.getStringParameter(request, "packageName");
//			String packageUrl = ServletRequestUtils.getStringParameter(request, "packageUrl");
//			String packageSentence = ServletRequestUtils.getStringParameter(request, "packageSentence");
//			String excludeArea = ServletRequestUtils.getStringParameter(request, "excludeArea");
//			String recChannel = ServletRequestUtils.getStringParameter(request, "recChannel");
//
//			if (packageId == -1) {
//				PushPackage pushPackage = new PushPackage();
//				pushPackage.setPackageName(packageName);
//				pushPackage.setPackageUrl(packageUrl);
//				pushPackage.setPackageSentence(packageSentence);
//				pushPackage.setExcludeArea(excludeArea);
//				pushPackage.setRecChannel(recChannel);
//				packageDao.save(pushPackage);
//				
//				//删除原缓存
//				ICacheClient mc = cacheFactory.getCommonCacheClient();
//				System.out.println("删除原缓存key:"+key+pushPackage.getRecChannel()+"结果："+mc.deleteCache(key+pushPackage.getRecChannel()));
//			} else {//更新
//				PushPackage pushPackage = packageDao.get(packageId);
//				if (pushPackage != null) {
//					//删除原缓存
//					ICacheClient mc = cacheFactory.getCommonCacheClient();
//					System.out.println("删除原缓存key:"+key+pushPackage.getRecChannel()+"结果："+mc.deleteCache(key+pushPackage.getRecChannel()));
//					System.out.println("删除新缓存key:"+key+recChannel+"结果："+mc.deleteCache(key+recChannel));
//					
//					pushPackage.setPackageName(packageName);
//					pushPackage.setPackageUrl(packageUrl);
//					pushPackage.setPackageSentence(packageSentence);
//					pushPackage.setExcludeArea(excludeArea);
//					pushPackage.setRecChannel(recChannel);
//					packageDao.save(pushPackage);
//				}
//			}
//
//			StringUtil.printTxt(getResponse(), "1");
//		} catch (Exception e) {
//			LogUtil.error(e.getMessage(), e);
//		}
//
//	}

	public void save() {
		try {
			Integer packageId = ServletRequestUtils.getIntParameter(request, "packageId", -1);
			String packageName = ServletRequestUtils.getStringParameter(request, "packageName");
			String packageUrl = ServletRequestUtils.getStringParameter(request, "packageUrl");
			String packageSentence = ServletRequestUtils.getStringParameter(request, "packageSentence");
//			String[] excludeAreas = ServletRequestUtils.getStringParameters(request, "excludeAreaArray");
//			String recChannel = ServletRequestUtils.getStringParameter(request, "recChannel");
			Integer status = ServletRequestUtils.getIntParameter(request, "status", 1);
			Integer price = ServletRequestUtils.getIntParameter(request, "price", 500);
			Integer type = ServletRequestUtils.getIntParameter(request, "type", 1);
			Integer packageLimit = ServletRequestUtils.getIntParameter(request, "packageLimit", -1);
			Integer companyShow = ServletRequestUtils.getIntParameter(request, "companyShow", -1);

//			StringBuilder sb = new StringBuilder();
//			for (String string : excludeAreas) {
//				sb.append(string).append(",");
//			}
//			String excludeArea = sb.deleteCharAt(sb.length() - 1).toString();
			
			if (packageId == -1) {
				PushPackage pushPackage = new PushPackage();
				pushPackage.setPackageName(packageName);
				pushPackage.setPackageUrl(packageUrl);
				pushPackage.setPackageSentence(Utils.isEmpty(packageSentence) ? packageName : packageSentence);
//				pushPackage.setExcludeArea(excludeArea);
//				pushPackage.setRecChannel(recChannel);
				pushPackage.setStatus(status);
				pushPackage.setPrice(price);
				pushPackage.setType(type);
				pushPackage.setPackageLimit(packageLimit);
				pushPackage.setCompanyShow(companyShow);
				packageManager.save(pushPackage);
				
//				//删除原缓存
//				ICacheClient mc = cacheFactory.getCommonCacheClient();
//				//传price版本缓存
//				System.out.println("删除原缓存key:"+key+pushPackage.getRecChannel()+price+"结果："+mc.deleteCache(key+pushPackage.getRecChannel()+price));
			} else {//更新
				PushPackage pushPackage = packageManager.get(packageId);
				if (pushPackage != null) {
//					//删除原缓存
//					ICacheClient mc = cacheFactory.getCommonCacheClient();
//					//传price版本缓存
//					System.out.println("删除原缓存key:"+key+pushPackage.getRecChannel()+price+"结果："+mc.deleteCache(key+pushPackage.getRecChannel()+price));
//					System.out.println("删除新缓存key:"+key+recChannel+price+"结果："+mc.deleteCache(key+recChannel+price));
					pushPackage.setPackageName(packageName);
					pushPackage.setPackageUrl(packageUrl);
					pushPackage.setPackageSentence(Utils.isEmpty(packageSentence) ? packageName : packageSentence);
//					pushPackage.setExcludeArea(excludeArea);
//					pushPackage.setRecChannel(recChannel);
					pushPackage.setStatus(status);
					pushPackage.setPrice(price);
					pushPackage.setType(type);
					pushPackage.setPackageLimit(packageLimit);
					pushPackage.setCompanyShow(companyShow);
					packageManager.save(pushPackage);
				}
			}

			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.SAVEFAILURE));
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 删除
	 */
	public void delete() {
		try {
			String packageIds = ServletRequestUtils.getStringParameter(getRequest(), "packageIds");
			if (!Utils.isEmpty(packageIds)) {
				String[] idsArr = packageIds.split(",");
				for (String id : idsArr) {
					PushPackage pushPackage = packageManager.get(Integer.parseInt(id));
					packageManager.delete(pushPackage);
					//删除缓存
//					ICacheClient mc = cacheFactory.getCommonCacheClient();
//					logger.info("删除缓存key:"+key+pushPackage.getRecChannel()+pushPackage.getPrice()+"结果:"+mc.deleteCache(key+pushPackage.getRecChannel()+pushPackage.getPrice()));
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
		Integer packageId = ServletRequestUtils.getIntParameter(request, "packageId", -1);

		List<PushPackageLimit> limits = packageManager.findPackageLimits(packageId);
		if (limits.size() == 0) {
			limits = new ArrayList<PushPackageLimit>();
			for (String prov : provinceList) {
				PushPackageLimit pushPackageLimit = new PushPackageLimit();
				pushPackageLimit.setPackageId(packageId);
				pushPackageLimit.setProvince(prov);
				pushPackageLimit.setDayLimit(-1);
				pushPackageLimit.setMonthLimit(-1);
				limits.add(pushPackageLimit);
			}
		}
		long nums = limits.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("packageLimits:");

		jstr.append(JSON.toJSONString(limits));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void savePackageLimit() {
		String limitStr = ServletRequestUtils.getStringParameter(request, "limits", "");
		try {
			List<PushPackageLimit> limits = JSON.parseArray(limitStr, PushPackageLimit.class);
			for (PushPackageLimit pushPackageLimit : limits) {
				//查找存在对象
				PushPackageLimit data = packageManager.findPackageLimitByProperty(pushPackageLimit.getPackageId(), pushPackageLimit.getProvince());
				if (!Utils.isEmpty(data)) {
					data.setPackageId(pushPackageLimit.getPackageId());
					data.setProvince(pushPackageLimit.getProvince());
					data.setDayLimit(pushPackageLimit.getDayLimit());
					data.setMonthLimit(pushPackageLimit.getMonthLimit());
					packageManager.savePackageLimit(data);
				} else {
					packageManager.savePackageLimit(pushPackageLimit);
				}
			}
			StringUtil.printJson(response, MSG.success(MSG.SAVESUCCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.success(MSG.SAVEFAILURE));
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
