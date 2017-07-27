/**
 * @全局应用基础JS
 * @author galle.chu@mopote.com
 * @version 1.0
 * Created by Gallen Chu ( http://hi.baidu.com/zhuguoneng )
 */

/*
 * 2012-09-18: <version 1.0> - <galle.chu@mopote.com> - <创建>
 */

var $J = jQuery.noConflict(); 

function $I(s){
	return document.getElementById(arguments[0]);
};

/**
 * 用途:与VC交互时的unicode编码
 */
String.prototype.unicodeEncode = function() {
	var str = this;
	str.replace(/\&/g, "&#38;");
	str = str.replace(/\</g, "&#60;");
	str = str.replace(/\>/g, "&#62;");
	str = str.replace(/\"/g, "&#34;");
	str = str.replace(/\'/g, "&#39;");
	str = str.replace(/ /g, "&#160;");
	return str;
}

/**
 * 用途:与VC交互时的unicode解码
 */
String.prototype.unicodeDecode = function() {
	var str = '';
	str = this.replace(/\x26/g, "&");
	str = str.replace(/\x3C/g, "<");
	str = str.replace(/\x3E/g, ">");
	str = str.replace(/\x22/g, "\"");
	str = str.replace(/\x27/g, "'");
	str = str.replace(/\xA0/g, " ");
	return str;
}

//计算app大小
function countSize(size) {
	return (size / 1024 / 1024).toFixed(2);
}

// 替换全部
String.prototype.replaceAll = stringReplaceAll;
function stringReplaceAll(AFindText, ARepText) {
	var raRegExp = new RegExp(AFindText.replace(
			/([\(\)\[\]\{\}\^\$\+\-\*\?\.\"\'\|\/\\])/g, "\\$1"), "ig");
	return this.replace(raRegExp, ARepText);
}

$J(function() {
	var imgNum=$J('img').length;
	$J('img').load(function(){
	    if(--imgNum == 1){
	        $J('body').trigger('imageloaded');
	    }
	});
});