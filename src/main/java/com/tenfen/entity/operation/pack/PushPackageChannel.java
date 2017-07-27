package com.tenfen.entity.operation.pack;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

/**
 * TWapCfgPackageChannel entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "T_PUSH_PACKAGE_CHANNEL")
public class PushPackageChannel extends IdEntity{

	// Fields    

	/**
	 * 
	 */
	private static final long serialVersionUID = 5970730059599061798L;
	private String channelName;
	private String clientVersion;
	private Date createTime;
	private Integer companyShow;
	private boolean leaf;

	// Constructors

	/** default constructor */
	public PushPackageChannel() {
		createTime = new Date();
		leaf = true;
		companyShow = 0;
	}

	/** full constructor */
	public PushPackageChannel(String channelName, String clientVersion, Date createTime) {
		this.channelName = channelName;
		this.clientVersion = clientVersion;
		this.createTime = createTime;
	}

	// Property accessors
	@Column(name = "channel_name", nullable = false, length = 45)
	public String getChannelName() {
		return this.channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	@Column(name = "client_version", nullable = false, length = 45)
	public String getClientVersion() {
		return this.clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", nullable = false, length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "COMPANY_SHOW")
	public Integer getCompanyShow() {
		return companyShow;
	}

	public void setCompanyShow(Integer companyShow) {
		this.companyShow = companyShow;
	}
	
	@Transient
	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	
}