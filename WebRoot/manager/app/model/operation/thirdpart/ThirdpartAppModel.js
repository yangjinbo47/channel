Ext.define('CMS.model.operation.thirdpart.ThirdpartAppModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'merchantId', 'thirdAppId', 'thirdAppMch', 'thirdAppSecret', 'merchantShowName', 'appLimit', 'appToday', 'callbackUrl']
});