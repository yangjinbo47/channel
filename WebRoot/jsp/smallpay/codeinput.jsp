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
<form action="./external/smallpay_pay.action">
<p>请输入运营商下发给您的验证码，并确认本次支付<s:property value="%{#request.fee/100}"/>元：<input type="text" name="code" style="border-width:1px;height:20px"/><p>
<s:hidden name="trans_seq" value="%{#request.transSeq}"/>
<s:hidden name="order_id" value="%{#request.orderId}"/>
<p style="text-align:center;"><input type="submit" value="确认支付"/>&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" onclick="javascript:history.back(-1);" value="取消"/></p>
</form>
</div>
</body>
</html>