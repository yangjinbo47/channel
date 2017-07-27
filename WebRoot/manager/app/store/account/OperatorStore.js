Ext.define('CMS.store.account.OperatorStore', {
	extend : 'Ext.data.Store',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.account.OperatorModel',
	autoLoad : true,
	pageSize : pageSize,
	proxy : {
		type : 'ajax',
		url : '../account/user_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'user'
		},
		actionMethods : {
			create : 'POST',
			read : 'POST',
			update : 'POST',
			destroy : 'POST'
		}
	}
});
