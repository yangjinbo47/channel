Ext.define('CMS.view.account.UserModFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'userModForm',
	requires : ['CMS.model.account.RoleModel'],
	id : 'userModForm',
	plain : true,
	border : 0,
	bodyPadding : 5,
	fieldDefaults : {
		labelWidth : 70,
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
	initComponent : function() {
		var me = this;
		var rstore = this.rolestore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.account.RoleModel',
			//autoLoad:true,
			proxy : {
				type : 'ajax',
				url : '../account/user_rolesOfUser.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'roles'
				}
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '序号',
			name : 'operid',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '登录账号',
			readOnly : true,
			name : 'loginName',
			allowBlank : false
		}, {
			xtype : 'textfield',
			fieldLabel : '用户名',
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'textfield',
			fieldLabel : '登录密码',
			name : 'password',
			inputType : 'password',
			emptyText:'请输入新密码',
			maxLength : 64,
			regex : /^(?!^\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{8,20}$/,
			regexText : '密码是8-20位的数字、字母、符号组成',
			maxLengthText : '登陆密码不能超过20个字符'
		}, {
			xtype : 'textfield',
			fieldLabel : '验证密码',
			name : 'confirmPassword',
			inputType : 'password',
			emptyText:'请再次输入新密码',
			maxLength : 64,
			regex : /^(?!^\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{8,20}$/,
			regexText : '密码是8-20位的数字、字母、符号组成',
			maxLengthText : '登陆密码不能超过20个字符',
			validator : function(){
				if(this.getValue() == this.ownerCt.down('textfield[name=password]').getValue()) return true;
				return '两次输入的密码不一致';
			}
		}, {
			xtype : 'combobox',
			fieldLabel : '所属公司',
			valueField : 'id',
			displayField : 'value',
			name : 'type',
			allowBlank : false,
			emptyText : '请选择',
			store : Ext.create('Ext.data.Store', {
			    fields: ['id', 'value'],
			    data : [
			        {id:0, value:'所有可见'},
			        {id:1, value:'十分科技'},
			        {id:2, value:'乾坤汇智'},
			        {id:3, value:'安晴科技'}
			    ]
			})
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			selModel : new Ext.selection.CheckboxModel,
			store : rstore,
			columns : {
				items : [{
					text : '序号',
					dataIndex : 'roleid',
					hidden : true
				}, {
					text : '角色名',
					dataIndex : 'name',
					width : 200
				}, {
					text : '角色说明',
					dataIndex : 'remark',
					flex : 1
				}]
			},
			tbar : [ {
				text : '添加角色',
				iconCls : 'icon-add',
				name : 'add'
			}, {
				text : '删除角色',
				iconCls : 'icon-delete',
				name : 'delete'
			} ]
		}]
		
		this.callParent(arguments);
	}
});