Ext.define('CMS.view.operation.open.OpenAppModUnCheckedProductsGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'openAppModUnCheckedProductsGrid',
	id : 'openAppModUnCheckedProductsGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.open.OpenProductInfoModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/openApp_getUnallocateProducts.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'productInfos'
				}
			}
		});
		Ext.apply(this, {
			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '序号',
				dataIndex : 'id',
				hidden : false,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '产品名',
				dataIndex : 'name',
				flex : 3,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			},
			// {text: '类型',dataIndex: 'type',flex: 1},
			{
				text : '价格',
				dataIndex : 'price',
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				},
				flex : 1
			} ]
		});
		this.callParent(arguments);
	}
});
