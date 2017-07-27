Ext.define('CMS.model.operation.open.OpenAppLimitModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'appId', 'province', 'dayLimit', 'monthLimit', 'userDayLimit', 'userMonthLimit', 'reduce']
});