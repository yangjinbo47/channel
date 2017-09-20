Ext.define('CMS.view.account.RolesAddFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'rolesAddForm',
	id : 'rolesAddForm',
	plain : true,
	requires : [ 'CMS.view.account.RolesChoosenTreePanel' ],
	autoScroll : true,
	border : 0,
	bodyPadding : 5,
	url : '../account/role_add.action',
	fieldDefaults : {
		labelWidth : 80,
		anchor : '100%'
	},
	layout : {
		type : 'vbox',
		align : 'stretch' // Child items are stretched to full width
	},
	items : [ {
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
	}, {
		fieldLabel : '权限选择',
		xtype : 'rolesChoosenTreePanel'
	}]
});