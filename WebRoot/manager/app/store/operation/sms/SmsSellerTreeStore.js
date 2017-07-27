Ext.define('CMS.store.operation.sms.SmsSellerTreeStore', {
	extend : 'Ext.data.TreeStore',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.operation.sms.SmsSellerModel',
	autoLoad : true,
	root : {
		name : '渠道',
		expanded : true
	},
	proxy : {
		type : 'ajax',
		url : '../operation/smsSeller_treelist.action'
	}
});
