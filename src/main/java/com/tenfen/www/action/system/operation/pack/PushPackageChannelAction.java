package com.tenfen.www.action.system.operation.pack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.tenfen.entity.operation.pack.PushPackageChannel;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.servlet.ServletRequestUtils;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.common.MSG;
import com.tenfen.www.service.operation.pack.PackageChannelManager;

public class PushPackageChannelAction extends SimpleActionSupport {

	private static final long serialVersionUID = -3983674683762797070L;

	@Autowired
	private PackageChannelManager packageChannelManager;

	//请求参数
	private Integer limit;
	private Integer page;
	private Integer start;
	
	private static SerializeConfig config = new SerializeConfig();
	static {
		config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
	}
	
	public void list() {
		String name = ServletRequestUtils.getStringParameter(request, "name", null);

		Page<PushPackageChannel> packageChannelPage = new Page<PushPackageChannel>();
		//设置默认排序方式
		packageChannelPage.setPageSize(limit);
		packageChannelPage.setPageNo(page);
		if (!packageChannelPage.isOrderBySetted()) {
			packageChannelPage.setOrderBy("id");
			packageChannelPage.setOrder(Page.DESC);
		}
		
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
		if (Utils.isEmpty(name)) {
			packageChannelPage = packageChannelManager.getPackageChannelPage(packageChannelPage, userType);
		} else {
			packageChannelPage = packageChannelManager.getPackageChannelsByName(name, packageChannelPage, userType);
		}
		
		long nums = packageChannelPage.getTotalCount();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("packageChannels:");

		List<PushPackageChannel> packageChannelList = packageChannelPage.getResult();
		jstr.append(JSON.toJSONString(packageChannelList, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}
	
	public void treelist() {
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
		List<PushPackageChannel> packageChannelList = packageChannelManager.getPackageChannelList(userType);
		
		long nums = packageChannelList.size();
		StringBuilder jstr = new StringBuilder("{");
		jstr.append("total:" + String.valueOf(nums) + ",");
		jstr.append("children:");

		jstr.append(JSON.toJSONString(packageChannelList, config));
		jstr.append("}");
		StringUtil.printJson(response, jstr.toString());
	}

	public void save() {
		try {
			Integer id = ServletRequestUtils.getIntParameter(request, "id", -1);
			String channelName = ServletRequestUtils.getStringParameter(request, "channelName");
			String clientVersion = ServletRequestUtils.getStringParameter(request, "clientVersion");
			Integer companyShow = ServletRequestUtils.getIntParameter(request, "companyShow", -1);

			if (id == -1) {
				PushPackageChannel pushPackageChannel = new PushPackageChannel();
				pushPackageChannel.setChannelName(channelName);
				pushPackageChannel.setClientVersion(clientVersion);
				pushPackageChannel.setCompanyShow(companyShow);
				packageChannelManager.save(pushPackageChannel);
			} else {//更新
				PushPackageChannel pushPackageChannel = packageChannelManager.get(id);
				if (pushPackageChannel != null) {
					pushPackageChannel.setChannelName(channelName);
					pushPackageChannel.setClientVersion(clientVersion);
					pushPackageChannel.setCompanyShow(companyShow);
					packageChannelManager.save(pushPackageChannel);
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
			String ids = ServletRequestUtils.getStringParameter(getRequest(), "ids");
			if (!Utils.isEmpty(ids)) {
				String[] idsArr = ids.split(",");
				for (String id : idsArr) {
					PushPackageChannel pushPackageChannel = packageChannelManager.get(Integer.parseInt(id));
					packageChannelManager.delete(pushPackageChannel);
				}
			}
			
			StringUtil.printJson(response, MSG.success(MSG.DELETESUCCESS));
		} catch (Exception e) {
			StringUtil.printJson(response, MSG.failure(MSG.DELETEFAILURE));
			logger.error(e.getMessage(), e);
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
