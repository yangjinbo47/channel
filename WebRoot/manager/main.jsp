<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import = "com.tenfen.bean.system.SystemProperty" %>
<!DOCTYPE html>
<html>
<head>
	<base href="<%=request.getContextPath()%>/manager/" />
	<title>管理平台</title>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="resources/common.css">
	<link rel="stylesheet" type="text/css" href="extjs/resources/ext-theme-neptune/ext-theme-neptune-all.css">
	<script type="text/javascript" src="extjs/ext-all.js"></script>
	<script type="text/javascript" src="extjs/ext-overwrite.js"></script>
	<script type="text/javascript" src="jquery/jquery1.11.1.js"></script>
	<script type="text/javascript" src="extjs/tipbox.js"></script>
	<script type="text/javascript" src="extjs/src/form/field/SwfUpload.js"></script>
	
	<script type="text/javascript" src="extjs/ext-theme-neptune.js"></script>
	<script type="text/javascript" src="extjs/ext-lang-zh_CN.js"></script>
	<script type="text/javascript" src="globalVariates.js"></script>
	<script type="text/javascript">
		var __operatorname__ = Ext.util.Cookies.get('cookie_operator_name');
		var __operatorid__ = Ext.util.Cookies.get('cookie_operator_id');
		Ext.application({
			name : 'CMS',
			controllers : [ 'MainController'],
			autoCreateViewport : true,
			launch : function() {
				Ext.QuickTips.init();
				//初始化图片查看器
				CMS.ImageViewer = Ext.create('CMS.extensions.ImageViewer');//全局实例化一个图像查看器
				$("body").delegate("img[data-imgpreview=1]", "click", function(e){
				    CMS.ImageViewer.setSrc(e.target.src).show();
				});
			}
		});
	</script>
</head>

<body>
	<div id="loadingres" class="loadingres"></div>
	<script type="text/javascript">try{Ext.get('loadingres').show().center();}catch(e){}</script>
</body>
</html>