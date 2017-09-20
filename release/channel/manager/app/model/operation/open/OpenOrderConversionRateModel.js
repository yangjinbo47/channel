Ext.define('CMS.model.operation.open.OpenOrderConversionRateModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'year', 'month', 'day', 'hour', 'orderReq', 'mo', 'mr', 'rate', 'rateReq', 'sellerId', 'showHour']
});