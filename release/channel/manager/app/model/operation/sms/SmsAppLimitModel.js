Ext.define('CMS.model.operation.sms.SmsAppLimitModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'appId', 'province', 'dayLimit', 'monthLimit', 'userDayLimit', 'userMonthLimit', 'reduce']
});