Ext.define('CMS.view.account.RolesModFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'rolesModForm',
	id : 'rolesModForm',
	plain : true,
	requires : [ 'CMS.view.account.RolesModTreePanel' ],
	autoScroll : true,
	url : '../account/role_mod.action',
	border : 0,
	bodyPadding : 5,
	fieldDefaults : {
		labelWidth : 80,
		anchor : '100%'
	},
//	reader : new Ext.data.reader.Json({
//		model : 'CMS.model.SysRoleModel',
//		record : 'roles',
//		successProperty : '@success'
//	}),
	layout : {
		type : 'vbox',
		align : 'stretch'
	},
	items : [ {
		xtype : 'textfield',
		fieldLabel : '序号',
		name : 'roleid',
		hidden : true
	}, {
		xtype : 'textfield',
		fieldLabel : '角色名称',
		name : 'roleName',
		allowBlank : false,
		maxLength : 20,
		blankText : '请输入角色名称',
		maxLengthText : '角色名称不能超过20个字符'
	}, {
		xtype : 'textfield',
		fieldLabel : '角色说明',
		name : 'remark',
		allowBlank : false,
		maxLength : 200,
		blankText : '请输入角色说明',
		maxLengthText : '角色说明不能超过200个字符'
	} ]
});