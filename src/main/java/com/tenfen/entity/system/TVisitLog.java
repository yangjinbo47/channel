package com.tenfen.entity.system;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_VISIT_LOG")
public class TVisitLog extends IdEntity {

	private static final long serialVersionUID = 4015910258615211042L;
	// Fields
	private String imsi;
	private String phoneNum;
	private String clientVersion;
	private String province;
	private String userAgent;
	private Date visitTime;

	// Constructors

	/** default constructor */
	public TVisitLog() {
	}

	/** minimal constructor */
	public TVisitLog(Integer id) {
		this.id = id;
	}

	@Column(name = "IMSI", length = 30)
	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	@Column(name = "PHONE_NUM", length = 11)
	public String getPhoneNum() {
		return this.phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	@Column(name = "CLIENT_VERSION", length = 30)
	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	@Column(name = "PROVINCE", length = 20)
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	@Column(name = "USER_AGENT", length = 255)
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "VISIT_TIME", length = 11)
	public Date getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

}