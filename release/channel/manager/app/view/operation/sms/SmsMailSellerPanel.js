Ext.define('CMS.view.operation.sms.SmsMailSellerPanel', {
	extend : 'Ext.form.Panel',
	xtype : 'smsMailSellerPanel',
	id : 'smsMailSellerPanel',
	requires : ['CMS.model.operation.sms.SmsSellerModel'],
	layout : 'fit',
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
		
		var sellerstore = this.sstore = Ext.create('Ext.data.Store', {
			model : 'CMS.model.operation.sms.SmsSellerModel',
//			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : '../operation/smsMail_sellerList.action',
				reader : {
					type : 'json',
					total : 'total',
					root : 'sellers'
				},
				timeout : 180000
			}
		});
		
		this.items = [ {
			xtype : 'textfield',
			fieldLabel : '邮件组ID号',
			name : 'groupId',
			hidden : true
		}, {
			xtype : 'displayfield',
			fieldLabel : '邮件组名称',
			labelWidth : 80,
			readOnly : true,
			name : 'name',
			allowBlank : false
		}, {
			xtype : 'gridpanel',
			columnLines : true,
			loadMask : true,
			multiSelect : true,
			height: 300,
			store : sellerstore,
			plugins : [{
				ptype : 'rowediting',
				pluginId : 'smsMailSellerPanel-rowediting',
		        clicksToMoveEditor: 1,
		        saveBtnText : '保存',
		        cancelBtnText : '取消'
		    }],
			selModel : new Ext.selection.CheckboxModel({
				mode : 'MULTI',
				showHeaderCheckbox : true
			}),
			columns : [ {
				text : 'ID',
				dataIndex : 'id',
				width : 50
			}, {
				text : '渠道',
				dataIndex : 'name',
				flex : 1,
				editor : {
					xtype : 'textfield',
					allowBlank : false,
					selectOnFocus : true
				}
			}]
		}]
		
		this.callParent(arguments);
	}
});
