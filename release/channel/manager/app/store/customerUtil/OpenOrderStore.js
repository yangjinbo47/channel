/**
 * 包月订购用户store
 */
Ext.define('CMS.store.customerUtil.OpenOrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.open.OpenOrderModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/openOrder_listByPhone.action',
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