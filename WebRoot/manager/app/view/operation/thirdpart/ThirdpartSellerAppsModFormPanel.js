Ext.define('CMS.view.operation.thirdpart.ThirdpartSellerAppsModFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'thirdpartSellerAppsModForm',
	requires : ['CMS.model.operation.thirdpart.ThirdpartAppModel'],
	id : 'thirdpartSellerAppsModForm',
	plain : true,
	border : 0,
	bodyPadding : 5,
	fieldDefaults : {
		labelWidth : 50,
		anchor : '100%'
	},
	layout : {
		type : 'vbox',
		align : 'stretch'
	},
	initComponent : function() {
		var me = this;
		var astore = this.appstore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.thirdpart.ThirdpartAppModel',
			//autoLoad:true,
			proxy : {
				type : 'ajax',
				url : '../operation/thirdpartSeller_appsOfSeller.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'apps'
				}
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '序号',
			name : 'sellerId',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '渠道名',
			readOnly : true,
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			selModel : new Ext.selection.CheckboxModel,
			store : astore,
			plugins : [{
				ptype : 'cellediting',
				pluginId : 'thirdpartSellerAppsManager-cellediting',
		        clicksToEdit: 1
		    }],
			columns : {
				items : [{
					text : '序号',
					dataIndex : 'id',
					width : 50
				}, {
					text : '应用名',
					dataIndex : 'name',
					flex : 2
				}, {
					text : '限量(分)',
					dataIndex : 'appLimit',
					flex : 2,
					editor : {
						xtype : 'numberfield'
					}
				}, {
					text : '当日',
					dataIndex : 'appToday',
					flex : 2
				}]
			},
			tbar : [ {
				text : '添加应用',
				iconCls : 'icon-add',
				name : 'add'
			}, {
				text : '删除应用',
				iconCls : 'icon-delete',
				name : 'delete'
			} ]
		}]
		
		this.callParent(arguments);
	}
});