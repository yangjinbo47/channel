package com.tenfen.www.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springside.modules.orm.hibernate.HibernateDao;

public class CustomHibernateDao<T, PK extends Serializable> extends
		HibernateDao<T, Serializable> {

	private Class<T> entityClass;

	public String getClassName() {
		return entityClass == null ? null : entityClass.getSimpleName();
	}

	@SuppressWarnings("unchecked")
	public CustomHibernateDao() {
		Type type = this.getClass().getGenericSuperclass();
		if (type != null && type instanceof ParameterizedType) {
			Type[] args = ((ParameterizedType) type).getActualTypeArguments();
			if (args != null && args.length > 0) {
				entityClass = (Class<T>) args[0];
			}
		}
	}

	/**
	 * @author BOBO
	 * @remark 根据属性统计个数
	 * @param propertyName
	 * @param arg
	 * @return
	 */
	public Integer getCountByProperty(String propertyName, Object arg) {
		try {
			StringBuffer sql = new StringBuffer(60);
			sql.append("select count(t) from ").append(getClassName())
					.append(" t ");
			sql.append("where t.").append(propertyName).append(" = ?");
			return (Integer) createQuery(sql.toString(), arg).uniqueResult();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * @author BOBO
	 * @remark 根据属性删除对象
	 * @param propertyName
	 * @param arg
	 * @return
	 */
	public int deleteByProperty(String propertyName, Object arg) {
		try {
			StringBuffer sql = new StringBuffer(60);
			sql.append("delete from ").append(getClassName()).append(" t ");
			sql.append("where t.").append(propertyName).append(" = ?");
			return batchExecute(sql.toString(), arg);
		} catch (Exception e) {
			return 0;
		}
	}
	
}
