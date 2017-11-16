package com.tenfen.bean.operation;

public class OpenDailyBean{

	private Integer sellerId;
	private Integer appId;
	private String sellerName;
	private String appName;
	private Integer orderReq;//下单数
	private Integer succ;//成功数
	private Integer succReduce;//成功（扣量后）
	private Integer fail;//失败
	private Integer noPay;//未支付
	private Integer fee;//金额
	private Integer feeReduce;//金额（扣量后）
//	private Long userNum;//用户数
//	private Long userSuccNum;//成功用户数
	private Integer userNum;//用户数
	private Integer userSuccNum;//成功用户数
	private String rate;//转化率
	private String reqRate;//请求转化率
	
	public Integer getSellerId() {
		return sellerId;
	}
	
	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}
	
	public Integer getAppId() {
		return appId;
	}
	
	public void setAppId(Integer appId) {
		this.appId = appId;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	
	public String getAppName() {
		return appName;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public Integer getOrderReq() {
		return orderReq;
	}
	
	public void setOrderReq(Integer orderReq) {
		this.orderReq = orderReq;
	}
	
	public Integer getSucc() {
		return succ;
	}
	
	public void setSucc(Integer succ) {
		this.succ = succ;
	}
	
	public Integer getSuccReduce() {
		return succReduce;
	}
	
	public void setSuccReduce(Integer succReduce) {
		this.succReduce = succReduce;
	}
	
	public Integer getFail() {
		return fail;
	}
	
	public void setFail(Integer fail) {
		this.fail = fail;
	}
	
	public Integer getNoPay() {
		return noPay;
	}
	
	public void setNoPay(Integer noPay) {
		this.noPay = noPay;
	}
	
	public Integer getFee() {
		return fee;
	}
	
	public void setFee(Integer fee) {
		this.fee = fee;
	}
	
	public Integer getFeeReduce() {
		return feeReduce;
	}
	
	public void setFeeReduce(Integer feeReduce) {
		this.feeReduce = feeReduce;
	}

//	public Long getUserNum() {
//		return userNum;
//	}
//
//	public void setUserNum(Long userNum) {
//		this.userNum = userNum;
//	}
//
//	public Long getUserSuccNum() {
//		return userSuccNum;
//	}
//
//	public void setUserSuccNum(Long userSuccNum) {
//		this.userSuccNum = userSuccNum;
//	}
	public Integer getUserNum() {
		return userNum;
	}

	public void setUserNum(Integer userNum) {
		this.userNum = userNum;
	}

	public Integer getUserSuccNum() {
		return userSuccNum;
	}

	public void setUserSuccNum(Integer userSuccNum) {
		this.userSuccNum = userSuccNum;
	}

	public String getRate() {
		return rate;
	}
	
	public void setRate(String rate) {
		this.rate = rate;
	}
	
	public String getReqRate() {
		return reqRate;
	}
	
	public void setReqRate(String reqRate) {
		this.reqRate = reqRate;
	}

}
