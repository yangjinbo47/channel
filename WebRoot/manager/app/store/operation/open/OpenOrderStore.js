/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.open.OpenOrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenOrderModel',
//	buffered: true,
//	leadingBufferZone: 300,
//	pageSize: 100,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openOrder_list.action',
		reader : {
			type : 'json',
			total : 'total',
			root : 'orders'
		},
		actionMethods:{
			read:'POST'
		}
	}
});