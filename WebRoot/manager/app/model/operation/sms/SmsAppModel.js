Ext.define('CMS.model.operation.sms.SmsAppModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'merchantId', 'appKey', 'appSecret', 'tips', 'merchantShowName', 'appLimit', 'appToday', 'excludeArea', 'excludeAreaArray', 'companyShow']
});