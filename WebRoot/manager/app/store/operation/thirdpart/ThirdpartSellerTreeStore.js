Ext.define('CMS.store.operation.thirdpart.ThirdpartSellerTreeStore', {
	extend : 'Ext.data.TreeStore',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.operation.thirdpart.ThirdpartSellerModel',
	autoLoad : true,
	root : {
		name : '渠道',
		expanded : true
	},
	proxy : {
		type : 'ajax',
		url : '../operation/thirdpartSeller_treelist.action'
	}
});
