Ext.define('CMS.view.account.UserAddFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'userAddForm',
	requires : ['CMS.model.account.RoleModel'],
	id : 'userAddForm',
	plain : true,
	border : 0,
	bodyPadding : 5,
	fieldDefaults : {
		labelWidth : 70,
		anchor : '100%'
	},
	layout : {
		type : 'vbox',
		align : 'stretch' // Child items are stretched to full width
	},
	initComponent : function() {
		var me = this;
		var rstore = this.rolestore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.account.RoleModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
//				url : '../account/role_list.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'roles'
				}
			}
		});
		
		this.items = [{
			xtype : 'textfield',
			fieldLabel : '登录账号',
			name : 'loginName',
			allowBlank : false,
			maxLength : 16,
			regex : /^[a-zA-Z]{1}[\w]{5,15}$/,
			regexText : '首位必须为字母,可由数字、字母组成,长度6-16位',
			validateOnBlur : true,
			validateOnChange : false,
			validator : function (){
				if(this.value.length>16) return '账号不能超过16个字符';
				var exist;
				Ext.Ajax.request({
			        url:'../account/user_checkLoginName.action',
			        method : 'post',
			        async : false,
			        params:{loginName:this.value},
			        success:function(result,request){
			        	if(result.responseText.trim()=='false'){
			        		exist = '用户名重复';
			        	}else{
			        		exist = true;
			        	}
			        }
				});
				return exist;
			}
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
			regex : /^(?!^\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{8,20}$/,
			regexText : '密码是8-20位的数字、字母、符号组成',
			allowBlank : false,
			maxLength : 64,
			blankText : '请输入密码',
			maxLengthText : '密码不能超过20个字符'
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
		} ]
		
		this.callParent(arguments);
	}
});
