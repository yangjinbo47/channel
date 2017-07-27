Ext.define('CMS.controller.operation.open.OpenMerchantController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenMerchantManager'],
	stores : ['CMS.store.operation.open.OpenMerchantStore'],
	refs : [{
		ref : 'openMerchantManagerView',
		selector : 'openMerchantManagerView'
	}],
	init : function() {
		this.control({
			'openMerchantManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('openMerchantManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'openMerchantManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'openMerchantManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'openMerchantManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'openMerchantManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getOpenMerchantManagerView();
		var roweditplugin = grid.getPlugin('openMerchantManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.open.OpenMerchantModel', {
            merchantName: '商户名',
            email:'邮箱',
            contact:'联系人',
            telephone:'联系电话',
            joinType:'接入类型'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getOpenMerchantManagerView();
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
						url : '../operation/openMerchant_delete.action',
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
		
		var grid = this.getOpenMerchantManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var joinType;
			if (isNaN(row.data.joinType)){//非数字
				Ext.Msg.alert('温馨提示','请选择接入类型');
				plugin.startEdit(0,0);
				return;
			} else {//数字
				joinType = row.data.joinType;
			}
			
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/openMerchant_save.action',
				params : {
					'id' : id,
					'merchantName' : row.data.merchantName,
					'email' : row.data.email,
					'contact' : row.data.contact,
					'telephone' : row.data.telephone,
					'joinType' : row.data.joinType
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
		var grid = this.getOpenMerchantManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getOpenMerchantManagerView();
		var queryParams = {};
		queryParams.merchantName = this.getOpenMerchantManagerView().down('toolbar > textfield[name=merchantName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getOpenMerchantManagerView();
		var merchantNameField = grid.down('toolbar > textfield[name=merchantName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		merchantNameField.reset();
	}
});
