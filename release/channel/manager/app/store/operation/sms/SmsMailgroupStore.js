/**
 * 开放平台app管理store
 */
Ext.define('CMS.store.operation.sms.SmsMailgroupStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsMailgroupModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsMail_groups.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'groups'
		},
		actionMethods:{
			read:'POST'
		}
	}
});