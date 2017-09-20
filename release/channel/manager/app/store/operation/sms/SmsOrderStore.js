/**
 * 包月订购用户store
 */
Ext.define('CMS.store.operation.sms.SmsOrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsOrderModel',
//	buffered: true,
//	leadingBufferZone: 300,
//	pageSize: 100,
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsOrder_list.action',
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