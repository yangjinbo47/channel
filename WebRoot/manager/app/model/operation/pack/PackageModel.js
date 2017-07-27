Ext.define('CMS.model.operation.pack.PackageModel', {
	extend : 'Ext.data.Model',
	fields : ['id', 'packageName', 'packageUrl', 'packageSentence', 'excludeArea', 'recChannel', 'status', 'createtime', 'packageLimit',  'packageToday', 'price', 'excludeAreaArray', 'type', 'channelName','companyShow']
});