Ext.define('CMS.store.operation.pack.PushPackageChannelTreeStore', {
	extend : 'Ext.data.TreeStore',
	requires : [ 'Ext.data.reader.Json' ],
	model : 'CMS.model.operation.pack.PushSellerModel',
	autoLoad : true,
	root : {
		name : '渠道名称',
		expanded : true
	},
//	proxy : {
//		type : 'ajax',
//		url : '../operation/pushPackageChannel_treelist.action'
//	}
	proxy : {
		type : 'ajax',
		url : '../operation/pushSeller_treelist.action'
	}
});
