Ext.define('CMS.model.operation.open.OpenOrderModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'imsi', 'orderId', 'outTradeNo', 'sellerId', 'appId', 'merchantId', 'subject', 
		'sendNumber', 'msgContent', 'createTime', 'fee', 'status', 'payTime', 'payPhone', 'province', 
		'sellerName', 'reduce']
});