Ext.define('CMS.view.operation.open.OpenMailSellerWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openMailSellerWindow',
	id : 'openMailSellerWindow',
	title : '邮件推送渠道设置',
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
		}]
	} ]
});
