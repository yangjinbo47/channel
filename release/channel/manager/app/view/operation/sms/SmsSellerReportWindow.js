Ext.define('CMS.view.operation.sms.SmsSellerReportWindow', {
	extend : 'Ext.window.Window',
	xtype : 'smsSellerReportWindow',
	id : 'smsSellerReportWindow',
	title : '渠道订单统计',
	modal : true,
	resizable : false,
	width : 1100,
	height : 300,
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
		}
	} ]
});
