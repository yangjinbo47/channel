Ext.define('CMS.model.operation.open.OpenAppModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'merchantId', 'appKey', 'appSecret', 'clientId', 'callbackUrl', 'merchantShowName', 'appLimit', 'appToday', 'excludeArea', 'excludeAreaArray', 'companyShow']
});