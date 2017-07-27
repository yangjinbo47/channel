Ext.define('CMS.view.account.RolesModWindow', {
	extend : 'Ext.window.Window',
	xtype : 'rolesModWindow',
	id : 'rolesModWindow',
	title : '修改角色信息',
	modal : true,
	resizable : false,
	width : 500,
	height : 400,
	minWidth : 300,
	minHeight : 200,
	constrainHeader : true,
	layout : 'fit',
	dockedItems : [ {
		xtype : 'toolbar',
		dock : 'bottom',
		ui : 'footer',
		layout : {
			pack : 'center'
		},
		items : [ {
			minWidth : 80,
			text : '保存',
			name : 'save'
		}, {
			minWidth : 80,
			text : '取消',
			name : 'cancel'
		} ]
	} ]
});
