Ext.define('CMS.model.operation.pack.OrderModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'imsi', 'sellerName', 'packageName', 'fee', 'phoneNum', 'status', 'name', 'channel', 'province', 'createTime']
});