package com.tenfen.entity.operation.pack;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_PUSH_PACKAGE")
public class PushPackage extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String packageName;
	private String packageUrl;
	private String packageSentence;
//	private String excludeArea;
//	private String recChannel;
	private Integer status;
	private Date createtime;
	private Integer packageLimit;
	private Integer packageToday;
	private Integer price;
	private Integer type;
	private Integer reduce;
	private Integer companyShow;
//	private String[] excludeAreaArray;
	
	//extra Fields
	private String channelName;

	// Constructors

	/** default constructor */
	public PushPackage() {
		this.createtime = new Date();
		this.packageToday = 0;
		this.reduce = 0;
		this.companyShow = 0;
	}

	/** minimal constructor */
	public PushPackage(Integer id) {
		this.id = id;
	}

	/** full constructor */
	public PushPackage(Integer id, String packageName, String packageUrl, String packageSentence) {
		this.id = id;
		this.packageName = packageName;
		this.packageUrl = packageUrl;
		this.packageSentence = packageSentence;
	}

	@Column(name = "PACKAGE_NAME", length = 50)
	public String getPackageName() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Column(name = "PACKAGE_URL", length = 200)
	public String getPackageUrl() {
		return this.packageUrl;
	}

	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	@Column(name = "PACKAGE_SENTENCE", length = 50)
	public String getPackageSentence() {
		return this.packageSentence;
	}

	public void setPackageSentence(String packageSentence) {
		this.packageSentence = packageSentence;
	}

//	@Column(name = "EXCLUDE_AREA", length = 200)
//	public String getExcludeArea() {
//		return excludeArea;
//	}
//
//	public void setExcludeArea(String excludeArea) {
//		this.excludeArea = excludeArea;
//	}

//	@Column(name = "REC_CHANNEL", length = 50)
//	public String getRecChannel() {
//		return recChannel;
//	}
//
//	public void setRecChannel(String recChannel) {
//		this.recChannel = recChannel;
//	}

	@Column(name = "STATUS")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATETIME", nullable = false, length = 19)
	public Date getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	@Column(name = "PACKAGE_LIMIT")
	public Integer getPackageLimit() {
		return packageLimit;
	}

	public void setPackageLimit(Integer packageLimit) {
		this.packageLimit = packageLimit;
	}

	@Column(name = "PACKAGE_TODAY")
	public Integer getPackageToday() {
		return packageToday;
	}

	public void setPackageToday(Integer packageToday) {
		this.packageToday = packageToday;
	}

	@Column(name = "PRICE")
	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Column(name = "TYPE")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "REDUCE")
	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}

	@Column(name = "COMPANY_SHOW")
	public Integer getCompanyShow() {
		return companyShow;
	}

	public void setCompanyShow(Integer companyShow) {
		this.companyShow = companyShow;
	}

//	@Transient
//	public String[] getExcludeAreaArray() {
//		excludeAreaArray = excludeArea.split(",");
//		return excludeAreaArray;
//	}
//
//	public void setExcludeAreaArray(String[] excludeAreaArray) {
//		this.excludeAreaArray = excludeAreaArray;
//	}

	@Transient
	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	
}