/**
 * 包月推送渠道store
 */
Ext.define('CMS.store.operation.BlackListStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.BlackListModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/blackList_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'blackLists'
		},
		actionMethods:{
			read:'POST'
		}
	}
});