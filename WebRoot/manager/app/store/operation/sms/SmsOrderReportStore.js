/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.sms.SmsOrderReportStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsOrderReportModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsOrder_reportAll.action',
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