package com.tenfen.entity.operation.sms;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;
import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_SELLER")
public class TSmsSeller extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String name;
	private String email;
	private String contact;
	private String telephone;
	private String sellerKey;
	private String sellerSecret;
	private String callbackUrl;
	private Integer companyShow;
	private Integer status;
	
	//extra field
	private boolean leaf;
	private boolean select;//是否选中，邮件组设置用
		
//	private List<TOpenApp> appList = Lists.newArrayList();//有序的关联对象集合
	private List<TSmsSellerApps> sellerApps = Lists.newArrayList();

	// Constructors
	public TSmsSeller() {
		leaf = true;
		companyShow = 0;
	}

	/** minimal constructor */
	public TSmsSeller(Integer id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 45)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "EMAIL", length = 45)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "CONTACT", length = 20)
	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Column(name = "SELLER_KEY", length = 45)
	public String getSellerKey() {
		return sellerKey;
	}

	public void setSellerKey(String sellerKey) {
		this.sellerKey = sellerKey;
	}

	@Column(name = "TELEPHONE", length = 45)
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "SELLER_SECRET", length = 45)
	public String getSellerSecret() {
		return sellerSecret;
	}

	public void setSellerSecret(String sellerSecret) {
		this.sellerSecret = sellerSecret;
	}
	
//	//多对多定义
//	@ManyToMany
//	//中间表定义,表名采用默认命名规则
//	@JoinTable(name = "t_open_seller_apps", joinColumns = { @JoinColumn(name = "seller_id") }, inverseJoinColumns = { @JoinColumn(name = "app_id") })
//	//Fecth策略定义
//	@Fetch(FetchMode.SUBSELECT)
//	//集合按id排序.
//	@OrderBy("id")
//	public List<TOpenApp> getAppList() {
//		return appList;
//	}
//
//	public void setAppList(List<TOpenApp> appList) {
//		this.appList = appList;
//	}
	
	@Column(name = "CALLBACK_URL", length = 200)
	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	@Column(name = "COMPANY_SHOW")
	public Integer getCompanyShow() {
		return companyShow;
	}

	public void setCompanyShow(Integer companyShow) {
		this.companyShow = companyShow;
	}
	
	@Column(name = "STATUS")
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@OneToMany(mappedBy = "smsSeller",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	public List<TSmsSellerApps> getSellerApps() {
		return sellerApps;
	}

	public void setSellerApps(List<TSmsSellerApps> sellerApps) {
		this.sellerApps = sellerApps;
	}

	@Transient
	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	@Transient
	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}
	
}