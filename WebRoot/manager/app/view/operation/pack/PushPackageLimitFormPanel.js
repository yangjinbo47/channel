Ext.define('CMS.view.operation.pack.PushPackageLimitFormPanel', {
	extend : 'Ext.grid.Panel',
	xtype : 'pushPackageLimitFormPanel',
	id : 'pushPackageLimitFormPanel',
	requires : ['CMS.model.operation.pack.PackageLimitModel'],
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	plugins : [{
		ptype : 'cellediting',
		pluginId : 'pushPackageLimitsManager-cellediting',
        clicksToEdit: 1
    }],
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.pack.PackageLimitModel',
			proxy : {
				type : 'ajax',
				url : '../operation/pushPackage_limitlist.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'packageLimits'
				}
			}
		});
		Ext.apply(this, {
			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '省份',
				dataIndex : 'province',
				flex : 1
			}, {
				text : '日限',
				dataIndex : 'dayLimit',
				flex : 1,
				editor : {
					xtype : 'numberfield',
					selectOnFocus : true
				}
			}
			,{
				text : '月限',
				dataIndex : 'monthLimit',
				flex : 1,
				editor : {
					xtype : 'numberfield',
					selectOnFocus : true
				}
			}]
		});
		this.callParent(arguments);
	}
});
