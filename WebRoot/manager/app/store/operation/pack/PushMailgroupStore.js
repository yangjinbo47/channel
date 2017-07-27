/**
 * 开放平台app管理store
 */
Ext.define('CMS.store.operation.pack.PushMailgroupStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.PushMailgroupModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/pushMail_groups.action',
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