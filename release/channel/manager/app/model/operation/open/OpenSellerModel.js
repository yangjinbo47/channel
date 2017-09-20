Ext.define('CMS.model.operation.open.OpenSellerModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'email', 'contact', 'telephone', 'sellerKey', 'sellerSecret', 'callbackUrl','status','companyShow',
	'select'//邮件组渠道设置是否选中
	]
});