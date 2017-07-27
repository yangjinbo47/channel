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
 * The persistent class for the t_frame_operator database table.
 * 
 */
@Entity
@Table(name="t_frame_operator")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer operid;

	private Date createtime;

	private String loginName;
	
	private String name;

	private String password;

	private Integer type;
	
	private List<Role> roleList = Lists.newArrayList();//有序的关联对象集合

	public User() {
		createtime = new Date();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getOperid() {
		return this.operid;
	}

	public void setOperid(Integer operid) {
		this.operid = operid;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATETIME", nullable = false, length = 19)
	public Date getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	@Column(name = "login_name", length = 45)
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@Column(name = "name", length = 45)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "PASSWORD", length = 64)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "TYPE", precision = 2, scale = 0)
	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	//多对多定义
	@ManyToMany
	//中间表定义,表名采用默认命名规则
	@JoinTable(name = "t_frame_user_role", joinColumns = { @JoinColumn(name = "operid") }, inverseJoinColumns = { @JoinColumn(name = "roleid") })
	//Fecth策略定义
	@Fetch(FetchMode.SUBSELECT)
	//集合按id排序.
	@OrderBy("roleid")
	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}
	
	/**
	 * 用户拥有的角色名称字符串, 多个角色名称用','分隔.
	 */
	//非持久化属性.
	@Transient
	public String getRoleNames() {
		return ReflectionUtils.convertElementPropertyToString(roleList, "name", ", ");
	}
	
	/**
	 * 用户拥有的角色id字符串, 多个角色id用','分隔.
	 */
	//非持久化属性.
	@Transient
	@SuppressWarnings("unchecked")
	public List<Integer> getRoleIds() {
		return ReflectionUtils.convertElementPropertyToList(roleList, "roleid");
	}

}