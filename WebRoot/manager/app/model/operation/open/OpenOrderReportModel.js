Ext.define('CMS.model.operation.open.OpenOrderReportModel', {
	extend : 'Ext.data.Model',
	fields: ['sellerId','sellerName','appId','appName','req','succ','succReduce','fail','noPay','fee','feeReduce','users_num','users_succ_num','rate','reqRate']
});