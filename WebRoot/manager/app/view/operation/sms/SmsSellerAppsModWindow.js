Ext.define('CMS.view.operation.sms.SmsSellerAppsModWindow', {
	extend : 'Ext.window.Window',
	xtype : 'smsSellerAppsModWindow',
	id : 'smsSellerAppsModWindow',
	title : '关联APP信息',
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
