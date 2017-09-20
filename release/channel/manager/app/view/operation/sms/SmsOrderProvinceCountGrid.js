Ext.define('CMS.view.operation.sms.SmsOrderProvinceCountGrid', {
	extend : 'Ext.grid.Panel',
	xtype : 'smsOrderProvinceCountGrid',
	id : 'smsOrderProvinceCountGrid',
	columnLines : true,
	layout : 'fit',
	border : false,
	loadMask : true,
	multiSelect : true,
	initComponent : function() {
		this.store = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.sms.SmsOrderProvinceCountModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/smsOrder_provinceCount.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'provinces'
				}
			}
		});
		Ext.apply(this, {
//			selModel : new Ext.selection.CheckboxModel,
			columns : [ {
				text : '省份',
				dataIndex : 'province',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '成功',
				dataIndex : 'succ',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '失败',
				dataIndex : 'fail',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '总计',
				dataIndex : 'count',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '金额',
				dataIndex : 'fee',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}, {
				text : '转化率',
				dataIndex : 'rate',
				flex : 1,
				editor : {
					xtype : 'textfield',
					readOnly : true,
					selectOnFocus : true
				}
			}]
		});
		this.callParent(arguments);
	}
});
