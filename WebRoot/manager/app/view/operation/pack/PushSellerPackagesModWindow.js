Ext.define('CMS.view.operation.pack.PushSellerPackagesModWindow', {
	extend : 'Ext.window.Window',
	xtype : 'pushSellerPackagesModWindow',
	id : 'pushSellerPackagesModWindow',
	title : '关联包月产品',
	modal : true,
	resizable : false,
	width : 500,
	height : 400,
	minWidth : 300,
	minHeight : 200,
	layout : 'fit',
	constrainHeader : true,
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
