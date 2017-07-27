/**
 * 转化率分析store
 */
Ext.define('CMS.store.operation.sms.SmsOrderConversionRateStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsOrderConversionRateModel',
	autoLoad : true,
    proxy : {
		type : 'ajax',
		url : '../operation/smsOrder_conversionRateList.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'rates'
		}
	}
});