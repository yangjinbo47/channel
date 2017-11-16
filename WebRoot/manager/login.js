/**
 * 系统登陆页面登陆窗体
 */

Ext.define("Ext.locale.zh_CN.form.field.Base",{override: "Ext.form.field.Base",invalidText: "输入值非法"});
Ext.define("Ext.locale.zh_CN.form.field.Text",{override: "Ext.form.field.Text",minLengthText: "该输入项的最小长度是 {0} 个字符",maxLengthText: "该输入项的最大长度是 {0} 个字符",blankText: "该输入项为必输项",regexText: "",emptyText: null});
Ext.define("Ext.locale.zh_CN.window.MessageBox",{override: "Ext.window.MessageBox",buttonText:{ok: "确定",cancel: "取消",yes: "是",no: "否"}});

/**
 * 登陆验证码组件
 */
Ext.define('CMS.CheckCode', {
	extend : 'Ext.form.field.Text',
	alias : 'widget.checkcode',
	inputTyle : 'codefield',
	codeUrl : Ext.BLANK_IMAGE_URL,
	isLoader : true,
	onRender : function(ct, position) {
		this.callParent(arguments);
		this.codeEl = this.inputRow.createChild({
			tag : 'img',
			style:'height:23px;margin-top:1px;',
			src : Ext.BLANK_IMAGE_URL
		});
		this.codeEl.addCls('x-form-code');
		this.codeEl.on('click', this.loadCodeImg, this);
		if (this.isLoader) {
			this.loadCodeImg();
		}
	},
	alignErrorIcon : function() {
		this.errorIcon.alignTo(this.codeEl, 'tl-tr', [5, 0 ]);
	},
	loadCodeImg : function() {
		this.codeEl.set({
			src : this.codeUrl + '?id=' + Math.random()
		});
	},
	reset : function(){
		this.callParent(arguments);
		this.loadCodeImg();
	}
});
	
/** 
*  Base64 encode / decode 
* 
*/  
function Base64() {  
    // private property  
    var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";  
   
    // public method for encoding  
    this.encode = function (input) {  
    	if(Ext.isEmpty(input)){
    		return '';
    	}
        var output = "";  
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;  
        var i = 0;  
        input = _utf8_encode(input);  
        while (i < input.length) {  
            chr1 = input.charCodeAt(i++);  
            chr2 = input.charCodeAt(i++);  
            chr3 = input.charCodeAt(i++);  
            enc1 = chr1 >> 2;  
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);  
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);  
            enc4 = chr3 & 63;  
            if (isNaN(chr2)) {  
                enc3 = enc4 = 64;  
            } else if (isNaN(chr3)) {  
                enc4 = 64;  
            }  
            output = output +  
            _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +  
            _keyStr.charAt(enc3) + _keyStr.charAt(enc4);  
        }  
        return output;  
    }  
   
    // public method for decoding  
    this.decode = function (input) {
    	if(Ext.isEmpty(input)){
    		return '';
    	}
        var output = "";  
        var chr1, chr2, chr3;  
        var enc1, enc2, enc3, enc4;  
        var i = 0;  
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");  
        while (i < input.length) {  
            enc1 = _keyStr.indexOf(input.charAt(i++));  
            enc2 = _keyStr.indexOf(input.charAt(i++));  
            enc3 = _keyStr.indexOf(input.charAt(i++));  
            enc4 = _keyStr.indexOf(input.charAt(i++));  
            chr1 = (enc1 << 2) | (enc2 >> 4);  
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);  
            chr3 = ((enc3 & 3) << 6) | enc4;  
            output = output + String.fromCharCode(chr1);  
            if (enc3 != 64) {  
                output = output + String.fromCharCode(chr2);  
            }  
            if (enc4 != 64) {  
                output = output + String.fromCharCode(chr3);  
            }  
        }  
        output = _utf8_decode(output);  
        return output;  
    }  
   
    // private method for UTF-8 encoding  
    var _utf8_encode = function (str) {  
        str = str.replace(/\r\n/g,"\n");
        var utftext = "";  
        for (var n = 0; n < str.length; n++) {  
            var c = str.charCodeAt(n);  
            if (c < 128) {  
                utftext += String.fromCharCode(c);  
            } else if((c > 127) && (c < 2048)) {  
                utftext += String.fromCharCode((c >> 6) | 192);  
                utftext += String.fromCharCode((c & 63) | 128);  
            } else {  
                utftext += String.fromCharCode((c >> 12) | 224);  
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);  
                utftext += String.fromCharCode((c & 63) | 128);  
            }  
   
        }  
        return utftext;  
    }  
   
    // private method for UTF-8 decoding  
    var _utf8_decode = function (utftext) {  
        var str = "";  
        var i = 0, c = 0,c1 = 0,c2 = 0 ,c3;  
        var c = c1 = c2 = 0;  
        while ( i < utftext.length ) {  
            c = utftext.charCodeAt(i);  
            if (c < 128) {  
                str += String.fromCharCode(c);  
                i++;  
            } else if((c > 191) && (c < 224)) {  
                c2 = utftext.charCodeAt(i+1);  
                str += String.fromCharCode(((c & 31) << 6) | (c2 & 63));  
                i += 2;  
            } else {  
                c2 = utftext.charCodeAt(i+1);  
                c3 = utftext.charCodeAt(i+2);  
                str += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));  
                i += 3;  
            }  
        }  
        return str;  
    }  
}

/**
 * 登陆窗体
 */
Ext.define('CMS.LoginForm', {
    extend : 'Ext.window.Window',
    animDuration : 50,//定义登陆失败后窗口抖动的频率
    width : 380,
    height : 260,
    title : '用户登录',
    closeAction : 'close',
    closable : false, 
    layout : 'fit',
    modal : true, 
    plain : true,
    draggable : false,
    resizable : false,
    requires : ['Ext.ux.InputTiper'],
    initComponent : function() {
    	
    	var base64 = this.base64 = new Base64();
    	
    	this.checkcode = Ext.create('CMS.CheckCode', {
            fieldLabel : '验证码',
            name : 'checkCode',
            allowBlank : false,
            plugins : ['inputtiper'],
            isLoader : true,
            blankText : '验证码不能为空',
            codeUrl: 'vc.jsp',
            width : 220
        });
        
    	this.image = Ext.create('Ext.Img', {
    	    src: 'extjs/resources/themes/images/logo.jpg',
    	    margin : '0 0 15 0'
    	});
    	
        this.form = Ext.widget('form',{
            border : false,
            bodyPadding : 0,
            fieldDefaults : {
                labelAlign : 'right',
                labelWidth : 80,
                labelStyle : 'font-weight:bold'
            },
            items : [this.image,{
                xtype : 'textfield',
                fieldLabel : '用户名',
                blankText : '用户名不能为空',
                name :'userName',
                value : base64.decode(Ext.util.Cookies.get('elearn-helper0')),
                plugins : ['inputtiper'],
                allowBlank : false,
                width : 300
            }, {
                xtype : 'textfield',
                fieldLabel : '密　码',
                allowBlank : false,
                blankText : '密码不能为空',
                width : 300,
                name : 'pwdhelper',
                value : (function(){
                	var helpervalue = base64.decode(Ext.util.Cookies.get('elearn-helper1'));
                	if(!Ext.isEmpty(helpervalue)){
                		if(helpervalue.length > 6){
                			return helpervalue.substring(0,6);
                		}
                		return helpervalue;
                	}
                	return '';
                })(),
                submitValue : false,
                inputType : 'password',
                enableKeyEvents : true,
                listeners : {
                	scope : this,
                	'keyup' : this.onPwdHelperKeyUp
                }
            }, this.checkcode,{
            	xtype : 'hidden',
            	value : base64.decode(Ext.util.Cookies.get('elearn-helper1')),
            	name : 'passWord'
            },{
                xtype : 'checkbox',
                fieldLabel : '&nbsp;',
                submitValue : false,
                name : 'remenberme',
                labelSeparator : '',
                checked : base64.decode(Ext.util.Cookies.get('elearn-helper2')) || false,
                boxLabel : '记住密码'
            } ],
			listeners : {
				scope : this,
				afterRender : this.doAfterRender
	        }
        });
        
        Ext.apply(this,{
        	items : [this.form],
        	buttons : [{
            	xtype : 'container',
            	name : 'tipmsg'
            },'->',{
                text : '登录',
                width : 60,
                minWidth : 60,
                scope : this,
                handler : this.doLogin
            }, {
				text : '重置',
				width : 60,
				minWidth : 60,
				scope : this,
				handler : this.doReset
			}]
        });
        this.callParent(arguments);
    },
    
    //登陆表单渲染完成后添加key事件
    doAfterRender : function(form,options){
    	this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {
        	enter : this.doLogin,
            scope : this
        });
    },
    
    onPwdHelperKeyUp : function(field){
    	this.form.down('hidden[name=passWord]').setValue('');
    },
    
    //重置登表单
    doReset : function(){
    	this.form.getForm().reset();
    },
    
    //登陆
	doLogin : function(){
		var form = this.form.getForm();
		var loadMask = new Ext.LoadMask(this.id,{msg:'登录中'});
		try{
		    if(form.isValid()) {
		    	var hiddenPwdField = this.form.down('hidden[name=passWord]');
		    	if(Ext.isEmpty(hiddenPwdField.getValue())){
		    		var helperValue = this.form.down('textfield[name=pwdhelper]').getValue();
		    		hiddenPwdField.setValue(this.base64Encode(helperValue + Math.ceil(Math.random()*1000)));
		    	}
		    	loadMask.show();
		        form.submit({
		            clientValidation : true,
		            url : '../www/login.action',
		            scope : this,
		            success : function(form, action) {
		            	loadMask.hide();
		            	Ext.util.Cookies.set('cookie_operator_name',action.result.operatorname);
		            	Ext.util.Cookies.set('cookie_operator_id',action.result.operatorid);
		            	this.onSuccessLogin();
		                window.location.href = "main.jsp";
		            },
		            failure : function(form, action) {
		            	loadMask.hide();
		                this.markMsg('<span style="color:red;font-weight:bold;">'+ (action.result.msg || '登录失败') +'</span>',true);
		            	this.shakeWindow();
		            	this.checkcode.loadCodeImg();
		            }
		        });
		    }else{
		    	this.markMsg('<span style="color:red;font-weight:bold;">请确认表单合法性</span>',true);
		    	this.shakeWindow();
		    }
	    }catch(e){
	    	throw e;
	    	loadMask.hide();
	    }
	},
	
	onSuccessLogin : function(){
		var username = this.form.down('textfield[name=userName]');
		var pwdhelper = this.form.down('hidden[name=passWord]');
		var remenber = this.form.down('checkbox[name=remenberme]');
		var expiretime = new Date(new Date().getTime()+(1000*60*60*24*30));
		try{
			if(remenber.getValue()){//勾选了记住密码
				Ext.util.Cookies.set('elearn-helper0',this.base64Encode(username.getValue()),expiretime);
				Ext.util.Cookies.set('elearn-helper1',this.base64Encode(pwdhelper.getValue()),expiretime);
				Ext.util.Cookies.set('elearn-helper2',this.base64Encode('true'),expiretime);
			}else{
				Ext.util.Cookies.clear('elearn-helper0');
				Ext.util.Cookies.clear('elearn-helper1');
				Ext.util.Cookies.clear('elearn-helper2');
			}
		}catch(e){
			throw e;
		}
		this.markMsg('<span style="color:green;font-weight:bold;">登录成功，正在跳转</span>',false);
		// 登录成功后, 隐藏登录窗口，并重新加载菜单
        this.close();
        try{
        	Ext.get('loadingres').show().center(document.body);
        }catch(e){}
	},
	
	//登陆失败后提示信息
	markMsg : function(msg,remove,delay){
		var me = this;
		delay = delay || 5000;
		me.down('toolbar container[name=tipmsg]').update(msg);
		if(remove){
			setTimeout(function(){
	    		me.down('toolbar container[name=tipmsg]').update('');
	    	},3000);
		}
	},
	
	//private,窗口科动函数
	shake : function(times,posX,adj){
		this.el.setX(posX + adj,{
			duration: this.animDuration,
			scope : this,
			callback : function(){
				times--;
				if(times > 0){
					this.shake(times,posX,(-adj));
				}else{
					this.el.setX(posX,{duration: this.animDuration});
				}
			}
		});
	},
	
	// 抖动窗口
	shakeWindow : function(){
		var posX = this.getPosition()[0];
		this.el.setX(posX - 3,{
			duration: this.animDuration,
			scope : this,
			callback : function(){
				this.shake(9,posX,6);
			}
		});
	},
	
	// baxe64编码
	base64Encode : function(value){
		if(!this.base64){
			this.base64 = new Base64();
		}
		return this.base64.encode(value);
	},
	
	base64Decode : function(value){
		if(Ext.isEmpty(value)){
			return '';
		}
		if(!this.base64){
			this.base64 = new Base64();
		}
		return this.base64.decode(value);
	}
});