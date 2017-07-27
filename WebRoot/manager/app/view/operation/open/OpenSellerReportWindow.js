Ext.define('CMS.view.operation.open.OpenSellerReportWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openSellerReportWindow',
	id : 'openSellerReportWindow',
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
