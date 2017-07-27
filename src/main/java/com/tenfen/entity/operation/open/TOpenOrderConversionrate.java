package com.tenfen.entity.operation.open;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tenfen.entity.IdEntity;

@Entity
@Table(name = "T_OPEN_ORDER_CONVERSIONRATE")
public class TOpenOrderConversionrate extends IdEntity implements Comparable<TOpenOrderConversionrate>{

	private static final long serialVersionUID = 6932210734466832689L;
	// Fields    
	private Integer year;
	private Integer month;
	private Integer day;
	private Integer hour;
	private Integer orderReq;
	private Integer mo;
	private Integer mr;
	private float rate;
	private float rateReq;
	private Integer sellerId;
	
	// extra field
	private String showHour;
	
	@Column(name = "YEAR")
	public Integer getYear() {
		return year;
	}
	
	public void setYear(Integer year) {
		this.year = year;
	}
	
	@Column(name = "MONTH")
	public Integer getMonth() {
		return month;
	}
	
	public void setMonth(Integer month) {
		this.month = month;
	}
	
	@Column(name = "DAY")
	public Integer getDay() {
		return day;
	}
	
	public void setDay(Integer day) {
		this.day = day;
	}
	
	@Column(name = "HOUR")
	public Integer getHour() {
		return hour;
	}
	
	public void setHour(Integer hour) {
		this.hour = hour;
	}
	
	@Column(name = "RATE")
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	@Column(name = "SELLER_ID")
	public Integer getSellerId() {
		return sellerId;
	}
	
	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}

	@Column(name = "ORDER_REQ")
	public Integer getOrderReq() {
		return orderReq;
	}

	public void setOrderReq(Integer orderReq) {
		this.orderReq = orderReq;
	}

	@Column(name = "MO")
	public Integer getMo() {
		return mo;
	}

	public void setMo(Integer mo) {
		this.mo = mo;
	}

	@Column(name = "MR")
	public Integer getMr() {
		return mr;
	}

	public void setMr(Integer mr) {
		this.mr = mr;
	}

	@Column(name = "RATE_REQ")
	public float getRateReq() {
		return rateReq;
	}

	public void setRateReq(float rateReq) {
		this.rateReq = rateReq;
	}

	@Override
	public int compareTo(TOpenOrderConversionrate entity) {
		if (this.getYear() < entity.getYear()) {
			if (this.getMonth() < entity.getMonth()) {
				if (this.getDay() < entity.getDay()) {
					if (this.getHour() < entity.getHour()) {
						return -1;
					} else {
						return 1;
					}
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	@Transient
	public String getShowHour() {
		showHour = day + "日" + hour;
		return showHour;
	}

	public void setShowHour(String showHour) {
		this.showHour = showHour;
	}
	
}