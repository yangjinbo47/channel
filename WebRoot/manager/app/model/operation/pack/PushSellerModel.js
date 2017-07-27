Ext.define('CMS.model.operation.pack.PushSellerModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'sellerKey', 'sellerSecret', 'callbackUrl','status','companyShow',
	'select'//邮件组渠道设置是否选中
	]
});