Ext.define('CMS.store.operation.open.OpenSellerTreeStore', {
	extend : 'Ext.data.TreeStore',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.operation.open.OpenSellerModel',
	autoLoad : true,
	root : {
		name : '渠道',
		expanded : true
	},
	proxy : {
		type : 'ajax',
		url : '../operation/openSeller_treelist.action'
	}
});
