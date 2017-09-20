Ext.define('CMS.view.operation.open.OpenSellerModUnCheckedAppsWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openSellerModUnCheckedAppsWindow',
	title : '选择应用',
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
