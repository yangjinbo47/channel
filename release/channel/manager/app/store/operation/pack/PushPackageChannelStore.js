/**
 * 包月推送渠道store
 */
Ext.define('CMS.store.operation.pack.PushPackageChannelStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.PackageChannelModel',
	autoLoad : true,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/pushPackageChannel_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'packageChannels'
		},
		actionMethods:{
			read:'POST'
		}
	}
});