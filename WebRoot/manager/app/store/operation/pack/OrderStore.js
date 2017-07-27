/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.pack.OrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.OrderModel',
	buffered: true,
	leadingBufferZone: 300,
	pageSize: 100,
    proxy : {
		type : 'ajax',
		url : '../operation/order_list.action',
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