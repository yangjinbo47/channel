/**
 * 包月订购用户store
 */
Ext.define('CMS.store.customerUtil.OrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.pack.OrderModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/order_listByPhone.action',
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