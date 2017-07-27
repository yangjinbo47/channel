Ext.define('CMS.view.operation.open.OpenAppReportWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openAppReportWindow',
	id : 'openAppReportWindow',
	title : 'APP订单统计',
	modal : true,
	resizable : false,
	width : 1100,
	height : 500,
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
