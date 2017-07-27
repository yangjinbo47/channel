package com.tenfen.mongoEntity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "t_order")
public class MongoTOrder implements Serializable{
	
	private static final long serialVersionUID = -4051818671872639854L;
	@Id
	private String id;
	@Field("trade_id")
	private String tradeId;
	@Field("out_trade_no")
	private String outTradeNo;
	@Field("imsi")
	private String imsi;
	@Field("phone_num")
	private String phoneNum;
	@Field("seller_id")
	private Integer sellerId;
	@Field("push_id")
	private Integer pushId;
	@Field("fee")
	private Integer fee;
	@Field("status")
	private Integer status;
	@Field("create_time")
	private Date createTime;
	@Field("name")
	private String name;
//	@Field("channel")
//	private String channel;
	@Field("province")
	private String province;
	@Field("reduce")
	private Integer reduce;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTradeId() {
		return tradeId;
	}
	
	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}
	
	public String getOutTradeNo() {
		return outTradeNo;
	}
	
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
	
	public String getImsi() {
		return imsi;
	}
	
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	
	public String getPhoneNum() {
		return phoneNum;
	}
	
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	
	public Integer getSellerId() {
		return sellerId;
	}

	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}
	
	public Integer getPushId() {
		return pushId;
	}
	
	public void setPushId(Integer pushId) {
		this.pushId = pushId;
	}
	
	public Integer getFee() {
		return fee;
	}
	
	public void setFee(Integer fee) {
		this.fee = fee;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
//	public String getChannel() {
//		return channel;
//	}
//	
//	public void setChannel(String channel) {
//		this.channel = channel;
//	}
	
	public String getProvince() {
		return province;
	}
	
	public void setProvince(String province) {
		this.province = province;
	}

	public Integer getReduce() {
		return reduce;
	}

	public void setReduce(Integer reduce) {
		this.reduce = reduce;
	}
	
}