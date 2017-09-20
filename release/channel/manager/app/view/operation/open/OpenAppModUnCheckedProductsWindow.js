Ext.define('CMS.view.operation.open.OpenAppModUnCheckedProductsWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openAppModUnCheckedProductWindow',
	title : '选择产品',
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
