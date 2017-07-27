package com.tenfen.bean.operation;

public class PackageDailyBean implements Comparable<PackageDailyBean>{

	//省份报表相关
	private String province;
	private Integer mo;
	private Integer moQc;
	private Integer mr;
	private Integer fee;
	private float zhlf;
	private String zhl;
	
	//包统计相关
	private String packageName;
	private Integer dayCount;
	private Integer dayFee;
	private Integer monthCount;
	private Integer monthFee;
	
	public String getProvince() {
		return province;
	}
	
	public void setProvince(String province) {
		this.province = province;
	}
	
	public Integer getMo() {
		return mo;
	}
	
	public void setMo(Integer mo) {
		this.mo = mo;
	}
	
	public Integer getMoQc() {
		return moQc;
	}

	public void setMoQc(Integer moQc) {
		this.moQc = moQc;
	}

	public Integer getMr() {
		return mr;
	}

	public void setMr(Integer mr) {
		this.mr = mr;
	}

	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	public float getZhlf() {
		return zhlf;
	}

	public void setZhlf(float zhlf) {
		this.zhlf = zhlf;
	}

	public String getZhl() {
		return zhl;
	}
	
	public void setZhl(String zhl) {
		this.zhl = zhl;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Integer getDayCount() {
		return dayCount;
	}

	public void setDayCount(Integer dayCount) {
		this.dayCount = dayCount;
	}

	public Integer getDayFee() {
		return dayFee;
	}

	public void setDayFee(Integer dayFee) {
		this.dayFee = dayFee;
	}

	public Integer getMonthCount() {
		return monthCount;
	}

	public void setMonthCount(Integer monthCount) {
		this.monthCount = monthCount;
	}

	public Integer getMonthFee() {
		return monthFee;
	}

	public void setMonthFee(Integer monthFee) {
		this.monthFee = monthFee;
	}

	@Override
	public int compareTo(PackageDailyBean entity) {
		if (this.getZhlf() < entity.getZhlf()) {
			return 1;
		} else if (this.getZhlf() > entity.getZhlf()) {
			return -1;
		} else {
			return 0;
		}
	}
}
