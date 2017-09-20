Ext.define('CMS.view.operation.pack.OrderReportDetailWindow', {
	extend : 'Ext.window.Window',
	xtype : 'orderReportDetailWindow',
	id : 'orderReportDetailWindow',
	title : '渠道订单统计详细',
	modal : true,
	resizable : false,
	width : 1100,
	height : 500,
	minWidth : 600,
	minHeight : 500,
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
