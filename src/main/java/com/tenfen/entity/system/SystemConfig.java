package com.tenfen.entity.system;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_CFG_SYSCONFIG")
public class SystemConfig extends IdEntity{

	private static final long serialVersionUID = 7946125481867495029L;
	
	private String name;
	private String jsonConfig;
	
	@Column(name = "name", length = 50)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "json_config", length = 4000)
	public String getJsonConfig() {
		return jsonConfig;
	}
	
	public void setJsonConfig(String jsonConfig) {
		this.jsonConfig = jsonConfig;
	}
	
	
}
