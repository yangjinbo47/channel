<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
	<base href="<%=request.getContextPath()%>/" />
	<title>小额支付-用户信息输入</title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
	<style>
		body{
		  font-size: 20px;
		  color: rgba(0,0,0,.6);
		}
	</style>
</head>

<body>
<div><img src="./jsp/smallpay/unicom.png"></div>
<div style="margin-top:20px;">
<form action="./external/smallpay_generateOrder.action">
<p>请输入付费手机号：<input type="text" name="phone" style="border-width:1px;height:20px"/><p>
<s:hidden name="seller_key" value="%{#request.sellerKey}"/>
<s:hidden name="app_name" value="%{#request.appName}"/>
<s:hidden name="fee" value="%{#request.fee}"/>
<s:hidden name="out_trade_no" value="%{#request.outTradeNo}"/>
<p style="text-align:center;"><input type="submit" text="提交"/></p>
</form>
</div>
</body>
</html>