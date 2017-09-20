Ext.define('CMS.model.operation.sms.SmsOrderConversionRateModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'year', 'month', 'day', 'hour', 'orderReq', 'mo', 'mr', 'rate', 'rateReq', 'sellerId', 'showHour']
});