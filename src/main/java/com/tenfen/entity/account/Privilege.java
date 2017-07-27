package com.tenfen.entity.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * The persistent class for the t_frame_menu database table.
 * 
 */
@Entity
@Table(name="t_frame_privilege")
public class Privilege implements Serializable,Comparable<Privilege> {
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private Integer privilegeid;

	private String controller;

	private Integer createoperid;

	private Date createtime;

	private String iconcls;

	private Integer isleaf;

	private Integer menulevel;

	private Integer menuorder;

	private String text;

	private Integer parentid;

	private Integer status;
	
	private String xtype;
	
	private List<Privilege> children = new ArrayList<Privilege>();
	
	public Privilege() {
		createtime = new Date();
	}

	@Transient
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.privilegeid = id;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Integer getPrivilegeid() {
		return privilegeid;
	}

	public void setPrivilegeid(Integer privilegeid) {
		this.privilegeid = privilegeid;
	}

	@Column(name = "CONTROLLER", length = 255)
	public String getController() {
		return this.controller;
	}

	public void setController(String controller) {
		this.controller = controller;
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

	@Column(name = "ICONCLS", length = 100)
	public String getIconcls() {
		return this.iconcls;
	}

	public void setIconcls(String iconcls) {
		this.iconcls = iconcls;
	}

	@Column(name = "ISLEAF")
	public Integer getIsleaf() {
		return isleaf;
	}

	public void setIsleaf(Integer isleaf) {
		this.isleaf = isleaf;
	}
	
	@Transient
	public Boolean getLeaf() {
		if (this.isleaf == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Column(name = "MENULEVEL")
	public Integer getMenulevel() {
		return this.menulevel;
	}

	public void setMenulevel(Integer menulevel) {
		this.menulevel = menulevel;
	}

	@Column(name = "MENUORDER")
	public Integer getMenuorder() {
		return this.menuorder;
	}

	public void setMenuorder(Integer menuorder) {
		this.menuorder = menuorder;
	}

	@Column(name = "NAME", length = 100)
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Column(name = "PARENTID")
	public Integer getParentid() {
		return this.parentid;
	}

	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}

	@Column(name = "STATUS")
	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "XTYPE", length = 45)
	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	@Transient
	public List<Privilege> getChildren() {
		return children;
	}

	public void setChildren(List<Privilege> children) {
		this.children = children;
	}

	@Override
	public int compareTo(Privilege o) {
		if (this.menuorder < o.menuorder) {
			return -1;
		} else if(this.menuorder > o.menuorder) {
			return 1;
		} else {			
			return 0;
		}
	}

}