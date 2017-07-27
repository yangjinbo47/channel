package com.tenfen.www.service.account;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tenfen.entity.account.Privilege;
import com.tenfen.www.dao.account.PrivilegeDao;

@Component
@Transactional
public class PrivilegeManager {
	
	@Autowired
	private PrivilegeDao privilegeDao;
	
	/**
	 * 获取一级权限的最大order
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Integer getFirstLevelMaxOrder() {
		return privilegeDao.getFirstLevelMaxOrder();
	}
	
	/**
	 * 获取二级权限的最大order
	 * @param page
	 * @return
	 * @author BOBO
	 */
	public Integer getSecondLevelMaxOrder(Integer parentId) {
		return privilegeDao.getSecondLevelMaxOrder(parentId);
	}
	
	/**
	 * 更新权限顺序
	 * @param jsonArray
	 * @return
	 */
	public int updateOrder(JSONArray jsonArray) {
		int size = jsonArray.size(), counter = 0;
		for(int i=0;i<size;i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			Integer privilegeid = jsonObject.getInteger("privilegeid");
			Integer menuorder = jsonObject.getInteger("menuorder");
			if(privilegeid != null && menuorder != null){
				int result = privilegeDao.updateOrder(privilegeid, menuorder);
				if(result > 0){
					counter++;
				}
			}
		}
		return counter;
	}
	
	/**
	 * 删除权限
	 * @param privilege
	 */
	@Transactional
	public void delete(Privilege privilege) {
		Integer parentId = privilege.getParentid();
		//判断是什么级别的菜单
		if (privilege.getIsleaf() == 1) {//是叶子节点的菜单
			Integer privilegeid = privilege.getPrivilegeid();
			//直接删除权限
			privilegeDao.delete(privilege);
			//删除角色权限关联关系
			privilegeDao.deleteRolePrivilege(privilegeid);
			//查询父节点下是否还有子节点
			Integer childrenNum = privilegeDao.getChildrenNum(parentId) - 1;//-1为了未提交数据前产生的脏数据预减1
			if (childrenNum == 0) {//无
				//更新父节点的leaf状态
				Privilege parentNode = privilegeDao.get(parentId);
				parentNode.setIsleaf(1);
				privilegeDao.saveEntity(parentNode);
			} else {//有
				//父节点不变化
			}
		} else {//非叶子节点菜单
			//查询包括父节点及所有子节点
			List<Privilege> privileges = privilegeDao.findBy("parentid", parentId);
			for (Privilege privilegeNode : privileges) {
				Integer privilegeid = privilegeNode.getPrivilegeid();
				//逐个删除子节点
				privilegeDao.delete(privilegeNode);
				//逐个删除角色权限关联关系
				privilegeDao.deleteRolePrivilege(privilegeid);
			}
		}
	}
	
}
