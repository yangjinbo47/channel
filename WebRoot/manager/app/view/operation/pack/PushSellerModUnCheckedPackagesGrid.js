Ext.define('CMS.view.operation.pack.PushSellerModUnCheckedPackagesGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'pushSellerModUnCheckedPackagesGrid',
	id : 'pushSellerModUnCheckedPackagesGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.pack.PackageModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/pushSeller_getUnallocatePackages.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'packages'
				}
			}
		});
		Ext.apply(this, {
			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '序号',
				dataIndex : 'id',
				width : 50
			}, {
				text : '包月产品名',
				dataIndex : 'packageName',
				flex : 3
			}]
		});
		this.callParent(arguments);
	}
});
