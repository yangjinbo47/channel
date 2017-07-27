package com.tenfen.www.dao.system;

import org.springframework.stereotype.Component;

import com.tenfen.entity.system.SystemConfig;
import com.tenfen.www.dao.CustomHibernateDao;

@Component
public class SystemConfigDao extends CustomHibernateDao<SystemConfig, Long> {
}
