package com.tenfen.www.service.operation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.tenfen.entity.operation.TBlackList;
import com.tenfen.www.dao.operation.BlackListDao;

@Component
@Transactional
public class BlackListManager {
	
	@Autowired
	private BlackListDao blackListDao;
	
	public Page<TBlackList> findBlackListPage(final Page<TBlackList> page) {
		Page<TBlackList> blackListPage = blackListDao.getAll(page);
		return blackListPage;
	}
	
	public Page<TBlackList> findBlackListPageByPhone(String phoneNum, final Page<TBlackList> page) {
		Page<TBlackList> blackListPage = blackListDao.findBlackListByPhone(phoneNum, page);
		return blackListPage;
	}
	
	public void save(TBlackList entity) {
		blackListDao.save(entity);
	}
	
	public TBlackList get(Integer id) {
		return blackListDao.get(id);
	}
	
	public void delete(Integer id) {
		blackListDao.delete(id);
	}
	
	/**
	 * 检查号码是否在黑名单内 true - 存在 ，false - 不存在
	 * @param phone
	 * @return
	 */
	public boolean isBlackList(String phone){
		List<TBlackList> list = blackListDao.findBy("phoneNum", phone);
		Integer count = list.size();
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
}
