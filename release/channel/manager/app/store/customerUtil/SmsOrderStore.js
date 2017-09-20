/**
 * 包月订购用户store
 */
Ext.define('CMS.store.customerUtil.SmsOrderStore',{
	extend : 'Ext.data.Store',
	model : 'CMS.model.operation.sms.SmsOrderModel',
	pageSize : pageSize,
    proxy : {
		type : 'ajax',
		url : '../operation/smsOrder_listByPhone.action',
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