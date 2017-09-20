Ext.define('CMS.controller.operation.sms.SmsProductInfoController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.sms.SmsProductInfoManager'],
	stores : ['CMS.store.operation.sms.SmsProductInfoStore','CMS.store.operation.sms.SmsMerchantStore'],
	refs : [{
		ref : 'smsProductInfoManagerView',
		selector : 'smsProductInfoManagerView'
	}],
	init : function() {
		this.control({
			'smsProductInfoManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('smsProductInfoManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'smsProductInfoManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'smsProductInfoManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'smsProductInfoManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'smsProductInfoManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getSmsProductInfoManagerView();
		var roweditplugin = grid.getPlugin('smsProductInfoManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.sms.SmsProductInfoModel', {
            name: '产品名',
            price:'100',
            sendNumber:'接入点',
            instruction:'指令',
            productId:'计费点ID',
            type:'计费点类型',
            merchantId:'所属商户'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getSmsProductInfoManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择要删除的记录');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					var ids = '';
					Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
						ids = ids + row.data.id + ',';
					});
					ids = ids.slice(0, -1);
					Ext.Ajax.request({
						url : '../operation/smsProductInfo_delete.action',
						params : {
							'ids' : ids
						},
						success : function(action) {
							var respText = Ext.JSON.decode(action.responseText);
							Ext.Msg.alert('提示', respText.msg);
							grid.getStore().reload();
						},
						failure : function(action) {
							var respText = Ext.JSON.decode(action.responseText);
							Ext.Msg.alert('提示', respText.msg);
						}
					});
				}
			});
		}
	},
	
	roweditupdate : function(plugin) {
		var me = this;
		if(!plugin.editor.isValid()){
			plugin.editor.body.highlight("fb7a7a", {
			    attr: "backgroundColor",
			    easing: 'easeIn',
			    duration: 1000
			});
			return false;
		}
		
		var grid = this.getSmsProductInfoManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var merchantId;
			if (isNaN(row.data.merchantId)){//非数字
				Ext.Msg.alert('温馨提示','请选择所属商户');
				plugin.startEdit(0,0);
				return;
			} else {//数字
				merchantId = row.data.merchantId;
			}
			var type;
			if (isNaN(row.data.type)){//非数字
				Ext.Msg.alert('温馨提示','请选择计费点类型');
				plugin.startEdit(0,0);
				return;
			} else {//数字
				type = row.data.type;
			}
			
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/smsProductInfo_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'price' : row.data.price,
					'sendNumber' : row.data.sendNumber,
					'instruction' : row.data.instruction,
					'productId' : row.data.productId,
					'type' : row.data.type,
					'merchantId' : row.data.merchantId
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					var success = respText.success;
					Ext.Msg.alert(success == true ? "成功" : "失败", respText.msg);
					if(success) {
						grid.getSelectionModel().clearSelections();
						grid.getStore().reload();
					}
				},
				failure : function(action) {
					Ext.Msg.alert('提示', respText.msg);
				}
			});
		});
		
	},
	
	roweditcancel : function() {
		var grid = this.getSmsProductInfoManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getSmsProductInfoManagerView();
		var queryParams = {};
		queryParams.productName = this.getSmsProductInfoManagerView().down('toolbar > textfield[name=productName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getSmsProductInfoManagerView();
		var productNameField = grid.down('toolbar > textfield[name=productName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		productNameField.reset();
	}
});
