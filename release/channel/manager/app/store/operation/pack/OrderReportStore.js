/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.pack.OrderReportStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.OrderReportModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/order_reportAll.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'reports'
		},
		timeout : 600000,
		actionMethods:{
			read:'POST'
		}
	}
});