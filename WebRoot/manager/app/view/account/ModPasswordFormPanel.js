Ext.define('CMS.view.account.ModPasswordFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'modPasswordFormPanel',
	id : 'modPasswordFormPanel',
	plain : true,
	url : '../account/user_modPassword.action',
	border : false,
	bodyPadding : 5,
	fieldDefaults : {
		labelWidth : 80,
		anchor : '100%'
	},
//	reader : new Ext.data.reader.Json({
//		model : 'CMS.model.UserModel',
//		record : 'user',
//		successProperty : '@success'
//	}),
	layout : {
		type : 'vbox',
		align : 'stretch'
	},
	items : [ {
		xtype : 'textfield',
		fieldLabel : '序号',
		name : 'id',
		value : __operatorid__,
		hidden : true
	}, {
		xtype : 'displayfield',
		fieldLabel : '登录账号',
		readOnly : true,
		name : 'loginName',
		value : __operatorname__,
		allowBlank : false
	}, {
		xtype : 'textfield',
		fieldLabel : '登录密码',
		name : 'password',
		emptyText:'请输入新密码',
		inputType : 'password',
		allowBlank: false,
		maxLength : 64,
		regex : /^(?!^\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{6,20}$/,
		regexText : '密码是6-20位的数字、字母、符号组成',
		maxLengthText : '登陆密码不能超过20个字符'
	}, {
		xtype : 'textfield',
		fieldLabel : '验证密码',
		name : 'passwordConfirm',
		allowBlank: false,
		inputType : 'password',
		emptyText:'请再次输入新密码',
		maxLength : 64,
		regex : /^(?!^\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{6,20}$/,
		regexText : '密码是6-20位的数字、字母、符号组成',
		maxLengthText : '登陆密码不能超过20个字符',
		validator : function(){
			if(this.value == this.ownerCt.down('textfield[name=password]').getValue()) return true;
			return '两次输入的密码不一致';
		}
	}]
});