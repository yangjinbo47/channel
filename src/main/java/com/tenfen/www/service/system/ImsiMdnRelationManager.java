package com.tenfen.www.service.system;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tenfen.entity.system.ImsiMdnRelation;
import com.tenfen.util.CTUtil;
import com.tenfen.util.LogUtil;
import com.tenfen.util.Utils;
import com.tenfen.www.dao.system.ImsiMdnRelationDao;

@Component
@Transactional
public class ImsiMdnRelationManager {
	
	@Autowired
	private ImsiMdnRelationDao imsiMdnRelationDao;
	
	/**
	 * 保存信息
	 * @param entity
	 */
	public void save(ImsiMdnRelation entity) {
		imsiMdnRelationDao.save(entity);
	}
	
	/**
	 * 获取包月信息
	 * @param id
	 * @return
	 */
//	public ImsiMdnRelation get(Integer id) {
//		return imsiMdnRelationDao.get(id);
//	}
	
	/**
	 * 删除包月信息
	 * @param id
	 */
	public void delete(Integer id) {
		imsiMdnRelationDao.delete(id);
	}
	
	public ImsiMdnRelation getEntityByProperty(String propertyName, Object value) {
		ImsiMdnRelation imsiMdnRelation = null;
		try {
			List<ImsiMdnRelation> imsiMdnRelations = imsiMdnRelationDao.findBy(propertyName, value);
			if (imsiMdnRelations.size() > 0) {
				imsiMdnRelation = imsiMdnRelations.get(0);
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return imsiMdnRelation;
	}
	
	/**
	 * 从库里获取手机号,若没有从接口查找并保存
	 * @param imsi
	 * @return
	 */
	public String getPhone(String imsi) {
		String phone = null;
		ImsiMdnRelation imsiMdnRelation = getEntityByProperty("imsi", imsi);
		if (imsiMdnRelation != null) {
			phone = imsiMdnRelation.getPhoneNum();
		} else {
			phone = CTUtil.queryPhoneByIMSI(imsi);
			if (!Utils.isEmpty(phone)) {
				//保存入本地库
				imsiMdnRelation = new ImsiMdnRelation();
				imsiMdnRelation.setImsi(imsi);
				imsiMdnRelation.setPhoneNum(phone);
				imsiMdnRelationDao.save(imsiMdnRelation);
			}
		}
		return phone;
	}
}
