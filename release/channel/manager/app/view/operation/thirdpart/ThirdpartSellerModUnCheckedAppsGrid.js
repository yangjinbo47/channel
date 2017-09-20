Ext.define('CMS.view.operation.thirdpart.ThirdpartSellerModUnCheckedAppsGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'thirdpartSellerModUnCheckedAppsGrid',
	id : 'thirdpartSellerModUnCheckedAppsGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.thirdpart.ThirdpartAppModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/thirdpartSeller_getUnallocateApps.action',
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
