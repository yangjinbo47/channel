package com.tenfen.entity.system;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_IMSI_MDN_RELATION")
public class ImsiMdnRelation extends IdEntity{

	private static final long serialVersionUID = 7946125481867495029L;
	
	private String phoneNum;
	private String imsi;
	
	@Column(name = "PHONE_NUM", length = 11)
	public String getPhoneNum() {
		return phoneNum;
	}
	
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	
	@Column(name = "IMSI", length = 20)
	public String getImsi() {
		return imsi;
	}
	
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	
}
