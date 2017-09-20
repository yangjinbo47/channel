Ext.define('CMS.model.operation.sms.SmsSellerModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'name', 'email', 'contact', 'telephone', 'sellerKey', 'sellerSecret', 'callbackUrl','status','companyShow','select']
});