Ext.define('CMS.view.operation.sms.SmsOrderProvinceCountWindow', {
	extend : 'Ext.window.Window',
	xtype : 'smsOrderProvinceCountWindow',
	title : '省份成功率统计',
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
		}
	} ]
});
