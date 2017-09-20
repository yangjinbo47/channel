<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<base href="<%=request.getContextPath()%>/manager/" />
	<title>管理平台</title>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="extjs/resources/ext-theme-neptune/ext-theme-neptune-all.css">
	<script type="text/javascript" src="extjs/ext-all.js"></script>
	<script type="text/javascript" src="extjs/ext-theme-neptune.js"></script>
	<script type="text/javascript" src="login.js"></script>
	<style type="text/css">
		.loadingres {width : 120px;height : 120px;margin:0px auto;display:none;position:absolute;opacity:1;background-image: url(resources/img/loading1.gif);}
		.inputtiper {font-size:1.6em; padding: 3px 10px;font-weight: bold; font-family: "楷体","楷体_GB2312";}
		.x-form-code {width: 73px; height: 20px; vertical-align: middle;cursor: pointer;  margin-left: 7px;}
	</style>
	<script type="text/javascript">
	Ext.application({
	    name: 'CMS',
	    launch: function() {
	    	//设置cookieId，服务器间共享，session共享用
	    	var cookidReqId = new Date().getTime();
	    	Ext.util.Cookies.set('cookie_req_id',cookidReqId);
	    	Ext.QuickTips.init();
	    	Ext.create('CMS.LoginForm').show();
	    	try{Ext.get('loadingres').hide();}catch(e){}
	    }
	});
	</script>
</head>

<body>
	<div id="loadingres" class="loadingres"></div>
	<script type="text/javascript">try{Ext.get('loadingres').show().center();}catch(e){}</script>
</body>
</html>
