Ext.define('CMS.controller.operation.thirdpart.ThirdpartAppController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.thirdpart.ThirdpartAppManager'],
	stores : ['CMS.store.operation.thirdpart.ThirdpartAppStore','CMS.store.operation.thirdpart.ThirdpartMerchantStore'],
	refs : [{
		ref : 'thirdpartAppManagerView',
		selector : 'thirdpartAppManagerView'
	}],
	init : function() {
		this.control({
			'thirdpartAppManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('thirdpartAppManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'thirdpartAppManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'thirdpartAppManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'thirdpartAppManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'thirdpartAppManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			'thirdpartAppManagerView > toolbar > button[name=relation]' : {
				click : this.onRelationClick
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getThirdpartAppManagerView();
		var roweditplugin = grid.getPlugin('thirdpartAppManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.thirdpart.ThirdpartAppModel', {
            name: '应用名',
            merchantId:'所属商户',
            thirdAppId:'三方app_id',
            thirdAppMch:'三方app商户',
            thirdAppSecret:'三方app_secret',
            callbackUrl:'回调地址'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getThirdpartAppManagerView();
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
						url : '../operation/thirdpartApp_delete.action',
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
		
		var grid = this.getThirdpartAppManagerView();
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
			
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/thirdpartApp_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'merchantId' : row.data.merchantId,
					'thirdAppId' : row.data.thirdAppId,
					'thirdAppMch' : row.data.thirdAppMch,
					'thirdAppSecret' : row.data.thirdAppSecret,
					'callbackUrl' : row.data.callbackUrl
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
		var grid = this.getThirdpartAppManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getThirdpartAppManagerView();
		var queryParams = {};
		queryParams.appName = this.getThirdpartAppManagerView().down('toolbar > textfield[name=appName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getThirdpartAppManagerView();
		var appNameField = grid.down('toolbar > textfield[name=appName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		appNameField.reset();
	}
});
