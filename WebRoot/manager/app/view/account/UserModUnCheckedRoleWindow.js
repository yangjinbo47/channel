Ext.define('CMS.view.account.UserModUnCheckedRoleWindow', {
	extend : 'Ext.window.Window',
	xtype : 'userModUnCheckedRoleWindow',
	title : '选择角色',
	resizable : false,
	modal : true,
	width : 750,
	height : 500,
	minWidth : 300,
	minHeight : 200,
	draggable : false,
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
			text : '添加',
			name : 'save'
		},{
			minWidth : 80,
			text : '取消',
			name : 'cancel'
		} ]
	} ]
});
