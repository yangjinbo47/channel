Ext.define('CMS.view.operation.open.OpenOrderProvinceCountWindow', {
	extend : 'Ext.window.Window',
	xtype : 'openOrderProvinceCountWindow',
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
