<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
	<base href="<%=request.getContextPath()%>/" />
	<title>小额支付-错误页面</title>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
</head>

<body>
<div><img src="./jsp/smallpay/unicom.png"></div>
<div><s:property value="#request.msg"/></div>
</body>
</html>