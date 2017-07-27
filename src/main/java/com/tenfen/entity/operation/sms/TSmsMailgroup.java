package com.tenfen.entity.operation.sms;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Lists;
import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_SMS_MAILGROUP")
public class TSmsMailgroup extends IdEntity {

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private String name;

	private List<TSmsMailer> tSmsMailers = Lists.newArrayList();//有序的关联对象集合
	private List<TSmsSeller> tSmsSellers = Lists.newArrayList();//有序的关联对象集合
	
	// Constructors
	public TSmsMailgroup() {
	}
	
	/** minimal constructor */
	public TSmsMailgroup(Integer id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 45)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//多对多定义
	@ManyToMany(fetch=FetchType.EAGER)
	//中间表定义,表名采用默认命名规则
	@JoinTable(name = "t_sms_mailgroup_mailer", joinColumns = { @JoinColumn(name = "group_id") }, inverseJoinColumns = { @JoinColumn(name = "mail_id") })
	//Fecth策略定义
	@Fetch(FetchMode.SUBSELECT)
	//集合按id排序.
	@OrderBy("id")
	public List<TSmsMailer> getMailerList() {
		return tSmsMailers;
	}

	public void setMailerList(List<TSmsMailer> tSmsMailers) {
		this.tSmsMailers = tSmsMailers;
	}
	
	//多对多定义
	@ManyToMany(fetch=FetchType.EAGER)
	//中间表定义,表名采用默认命名规则
	@JoinTable(name = "t_sms_mailgroup_seller", joinColumns = { @JoinColumn(name = "group_id") }, inverseJoinColumns = { @JoinColumn(name = "seller_id") })
	//Fecth策略定义
	@Fetch(FetchMode.SUBSELECT)
	//集合按id排序.
	@OrderBy("id")
	public List<TSmsSeller> getSellerList() {
		return tSmsSellers;
	}

	public void setSellerList(List<TSmsSeller> tSmsSellers) {
		this.tSmsSellers = tSmsSellers;
	}

}