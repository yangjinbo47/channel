package com.tenfen.bean.account;

public class RolePrivilegeModel {
	
	private Integer id;
	private String text;
	private boolean checked;
	private boolean leaf;
	private Integer parentid;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public boolean isLeaf() {
		return leaf;
	}
	
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	
	public Integer getParentid() {
		return parentid;
	}
	
	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}

}
