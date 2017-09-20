/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.open.OpenOrderReportStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenOrderReportModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openOrder_reportAll.action',
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