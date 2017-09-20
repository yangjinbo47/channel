/**
 * 转化率分析store
 */
Ext.define('CMS.store.operation.open.OpenOrderConversionRateStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenOrderConversionRateModel',
	autoLoad : true,
    proxy : {
		type : 'ajax',
		url : '../operation/openOrder_conversionRateList.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'rates'
		}
	}
});