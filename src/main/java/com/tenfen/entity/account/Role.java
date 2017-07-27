package com.tenfen.entity.account;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springside.modules.utils.ReflectionUtils;

import com.google.common.collect.Lists;


/**
 * The persistent class for the t_frame_role database table.
 * 
 */
@Entity
@Table(name="t_frame_role")
public class Role implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private Integer roleid;

	private Integer createoperid;

	private Date createtime;

	private String name;

	private String remark;
	
	private List<Privilege> authorityList = Lists.newArrayList();
	
	public Role() {
		createtime = new Date();
	}

	@Transient
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.roleid = id;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getRoleid() {
		return this.roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	@Column(name = "CREATEOPERID")
	public Integer getCreateoperid() {
		return this.createoperid;
	}

	public void setCreateoperid(Integer createoperid) {
		this.createoperid = createoperid;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATETIME", nullable = false, length = 19)
	public Date getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	@Column(name = "NAME", length = 45)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "REMARK", length = 255)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@ManyToMany
	@JoinTable(name = "t_frame_role_privilege", joinColumns = { @JoinColumn(name = "roleid") }, inverseJoinColumns = { @JoinColumn(name = "privilegeid") })
	@Fetch(FetchMode.SUBSELECT)
	@OrderBy("privilegeid")
	public List<Privilege> getAuthorityList() {
		return authorityList;
	}

	public void setAuthorityList(List<Privilege> authorityList) {
		this.authorityList = authorityList;
	}
	
	@Transient
	public String getAuthNames() {
		return ReflectionUtils.convertElementPropertyToString(authorityList, "text", ", ");
	}
	
	@Transient
	@SuppressWarnings("unchecked")
	public List<Integer> getAuthIds() {
		return ReflectionUtils.convertElementPropertyToList(authorityList, "privilegeid");
	}

}