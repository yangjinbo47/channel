package com.tenfen.www.service.operation.pack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.pack.PushPackageChannel;
import com.tenfen.www.dao.operation.pack.PackageChannelDao;

@Component
@Transactional
public class PackageChannelManager {
	
	@Autowired
	private PackageChannelDao packageChannelDao;
	
	public List<PushPackageChannel> getPackageChannelList() {
		List<PushPackageChannel> list = packageChannelDao.findAllList();
		return list;
	}
	
	public List<PushPackageChannel> getPackageChannelList(Integer operatorType) {
		List<PushPackageChannel> list = packageChannelDao.findAllList(operatorType);
		return list;
	}
	
	/**
	 * 查询正常状态的推送列表
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Page<PushPackageChannel> getPackageChannelPage(final Page<PushPackageChannel> page, Integer operatorType) {
//		Page<PushPackageChannel> packageChannelPage = packageChannelDao.getAll(page);
		Page<PushPackageChannel> packageChannelPage = packageChannelDao.getPackageChannelPage(page, operatorType);
		return packageChannelPage;
	}
	
	/**
	 * 根据名字查询渠道列表
	 * @param name
	 * @return
	 */
	public Page<PushPackageChannel> getPackageChannelsByName(String name, final Page<PushPackageChannel> page, Integer operatorType) {
		Page<PushPackageChannel> packageChannelPage = packageChannelDao.getPackageChannelsByName(name, page, operatorType);
		return packageChannelPage;
	}
	
	/**
	 * 保存渠道信息
	 * @param entity
	 */
	public void save(PushPackageChannel entity) {
		packageChannelDao.save(entity);
	}
	
	/**
	 * 获取渠道信息
	 * @param id
	 * @return
	 */
	public PushPackageChannel get(Integer id) {
		return packageChannelDao.get(id);
	}
	
	public PushPackageChannel findByClientVersion(Object clientVersion) {
		return packageChannelDao.findUniqueBy("clientVersion", clientVersion);
	}
	
	/**
	 * 删除渠道信息
	 * @param id
	 */
	public void delete(PushPackageChannel entity) {
		packageChannelDao.delete(entity);
	}
	
}
