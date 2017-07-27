/**
 * @Jquery 相关的综合util类。
 * @author galle.chu@skymobi.com
 * @version 1.0
 * @requires jQuery v1.2 or later
 * Created by Gallen Chu ( http://hi.baidu.com/zhuguoneng )
 */

/*
 * 2012-09-18: <version 1.0> - <galle.chu@skymobi.com> - <创建>
 */

/**
 * 统一ajax请求入口
 * @param url 请求链接
 * @param param 请求参数
 * @param fnSuccess 成功回调 fnSuccess = {"fn":function(){},"param":{}} fn为回调方法，param为回调方法参数;
 * @param fnError 失败回调 fnError = {"fn":function(){},"param":{}};
 */
function ajaxRequest(url,param,fnSuccess,fnError,async){
	$J.ajax({
		type : "post",
		url : url,
		dataType : 'json',
		async : async ? async : true,
		data : param,
		success : function(data) {
			if (data && data.statusCode == 999) {
				location.href = window.baseUrl + "/userweb/index?cpUser=" + data.cpUser;
				return;
			}
			
			if(typeof fnSuccess.fn == "function"){
				fnSuccess.fn(data, fnSuccess.param);
			}
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			if(fnError && typeof fnError.fn == "function"){
				fnError.fn(fnError.param);
			}
		}
	});
}

/**********************
 * Dialog Utils
 * top - topLeft - topCenter - topRight - center - centerLeft - centerRight - bottom - bottomLeft - bottomCenter - bottomRight
 **********************/
var Dialog = {
		defaultLayout : "topRight"
};
Dialog.success = function(text, layout, fn) {
	var ui = Dialog.create(text, "success", layout, 4000, fn);
}
Dialog.error = function(text,layout, fn) {
	var ui = Dialog.create(text, "error", layout, 3000, fn);
}
Dialog.warning = function(text,layout, fn) {
	var ui = Dialog.create(text, "warning", layout, 3000, fn);
}
Dialog.timeout = function(text,layout, fn) {
	var ui = Dialog.create(text, "warning", layout, 3000, fn);
}
Dialog.create = function(text, type, layout, timeout, fn) {
	$J.noty({
		layout : layout,
		text : text,
		type : type,
		timeout : timeout,
		afterClose: function() {
			if(fn && typeof fn == "function"){
				fn();
			}
		}
	});
}