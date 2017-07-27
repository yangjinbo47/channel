Ext.define('CMS.view.operation.pack.PushSellerPackagesModFormPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'pushSellerPackagesModForm',
	id : 'pushSellerPackagesModForm',
	requires : ['CMS.model.operation.pack.PackageModel'],
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
			model : 'CMS.model.operation.pack.PackageModel',
			//autoLoad:true,
			proxy : {
				type : 'ajax',
				url : '../operation/pushSeller_packagesOfSeller.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'packages'
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
			height: 290,
			store : astore,
//			plugins : [{
//				ptype : 'rowediting',
//				pluginId : 'openSellerAppsManager-rowediting',
//		        clicksToMoveEditor: 1,
//		        saveBtnText : '保存',
//		        cancelBtnText : '取消'
//		    }],
			plugins : [{
				ptype : 'cellediting',
				pluginId : 'pushSellerPackagesManager-cellediting',
		        clicksToEdit: 1
		    }],
			columns : [{
				text : '序号',
				dataIndex : 'id',
				width : 50
			}, {
				text : '包月产品名',
				dataIndex : 'packageName',
				flex : 2
			}, {
				text : '限量(个)',
				dataIndex : 'packageLimit',
				flex : 2,
				editor : {
					xtype : 'numberfield'
				}
			}, {
				text : '当日(个)',
				dataIndex : 'packageToday',
				flex : 2
			}],
			tbar : [ {
				text : '添加产品',
				iconCls : 'icon-add',
				name : 'add'
			}, {
				text : '删除产品',
				iconCls : 'icon-delete',
				name : 'delete'
			} ]
		}]
		
		this.callParent(arguments);
	}
});