Ext.define('CMS.view.operation.open.OpenSellerModUnCheckedAppsGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'openSellerModUnCheckedAppsGrid',
	id : 'openSellerModUnCheckedAppsGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.open.OpenAppModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/openSeller_getUnallocateApps.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'apps'
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
				text : '应用名',
				dataIndex : 'name',
				flex : 3
			}]
		});
		this.callParent(arguments);
	}
});
