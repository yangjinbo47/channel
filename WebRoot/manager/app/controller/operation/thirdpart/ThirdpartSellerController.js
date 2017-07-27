Ext.define('CMS.controller.operation.thirdpart.ThirdpartSellerController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.thirdpart.ThirdpartSellerManager'],
	stores : ['CMS.store.operation.thirdpart.ThirdpartSellerStore'],
	refs : [{
		ref : 'thirdpartSellerManagerView',
		selector : 'thirdpartSellerManagerView'
	},{
		ref : 'thirdpartSellerAppsModWindow',
		selector : 'thirdpartSellerAppsModWindow'
	},{
		ref : 'thirdpartSellerModUnCheckedAppsWindow',
		selector : 'thirdpartSellerModUnCheckedAppsWindow'
	}],
	init : function() {
		this.control({
			'thirdpartSellerManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('thirdpartSellerManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'thirdpartSellerManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'thirdpartSellerManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'thirdpartSellerManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'thirdpartSellerManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			
			'thirdpartSellerManagerView > toolbar > button[name=relation]' : {
				click : this.onRelationClick
			},
			'thirdpartSellerAppsModWindow > toolbar >button[name=save]' : {//关联窗口保存按钮
				click : this.modWindowSave
			},
			'thirdpartSellerAppsModWindow > toolbar >button[name=cancel]' : {//关联窗口取消按钮
				click : this.modWindowCancel
			},
			'thirdpartSellerAppsModWindow > thirdpartSellerAppsModForm > gridpanel > toolbar > button[name=add]' : {//关联窗口新增APP按钮
				click : this.modWindowAddClick
			},
			'thirdpartSellerAppsModWindow > thirdpartSellerAppsModForm > gridpanel > toolbar > button[name=delete]' : {//关联窗口删除APP按钮
				click : this.modWindowDelClick
			},
			'thirdpartSellerModUnCheckedAppsWindow > toolbar > button[name=save]' : {//修改窗口角色选择窗口保存按钮
				click : this.modAppCheckWindowSaveClick
			},
			'thirdpartSellerModUnCheckedAppsWindow > toolbar > button[name=cancel]' : {//修改窗口角色选择窗口取消按钮
				click : this.modAppCheckWindowCancelClick
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getThirdpartSellerManagerView();
		var roweditplugin = grid.getPlugin('thirdpartSellerManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.thirdpart.ThirdpartSellerModel', {
            name: '渠道名',
            email:'邮箱',
            contact:'联系人',
            telephone:'联系电话',
            sellerKey:'渠道KEY',
            sellerSecret:'渠道secret',
            callbackUrl:'通知渠道方地址',
            status:'状态'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getThirdpartSellerManagerView();
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
						url : '../operation/thirdpartSeller_delete.action',
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
		
		var grid = this.getThirdpartSellerManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var status;
			if (isNaN(row.data.status)){//非数字
				Ext.Msg.alert('温馨提示','请选择状态');
				plugin.startEdit(0,0);
				return;
			} else {//数字
				status = row.data.status;
			}
			
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/thirdpartSeller_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'email' : row.data.email,
					'contact' : row.data.contact,
					'telephone' : row.data.telephone,
					'sellerKey' : row.data.sellerKey,
					'sellerSecret' : row.data.sellerSecret,
					'callbackUrl' : row.data.callbackUrl,
					'status' : row.data.status
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
		var grid = this.getThirdpartSellerManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getThirdpartSellerManagerView();
		var queryParams = {};
		queryParams.sellerName = this.getThirdpartSellerManagerView().down('toolbar > textfield[name=sellerName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getThirdpartSellerManagerView();
		var sellerNameField = grid.down('toolbar > textfield[name=sellerName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		sellerNameField.reset();
	},
	
	onRelationClick : function() {
		var grid = this.getThirdpartSellerManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要关联的渠道!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个渠道进行关联!');
			return;
		} else {
			var sellerid = rs[0].data.id;
			var panel = Ext.create('CMS.view.operation.thirdpart.ThirdpartSellerAppsModFormPanel');
			panel.appstore.proxy.extraParams.sellerId = sellerid;
			panel.appstore.loadPage(1);
			
			with(panel.form){
				findField('sellerId').setValue(rs[0].data.id);
				findField('name').setValue(rs[0].data.name);
			}
			var win = Ext.create('CMS.view.operation.thirdpart.ThirdpartSellerAppsModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	modWindowSave : function() {
		var me=this;
		var form = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加APP！');
			return;
		}
		var sellerId = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm > textfield[name=sellerId]').getValue();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		appIds = [];
		appLimit = [];
		Ext.Array.each(rs, function(record) {
			appIds.push(record.data.id);
			appLimit.push(record.data.appLimit);
		});
		Ext.Ajax.request({
			url : '../operation/thirdpartSeller_saveSellerAppRelation.action',
			params : {
				'sellerId' : sellerId,
				'appIds' : appIds.join(","),
				'appLimit' : appLimit.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getThirdpartSellerAppsModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	modWindowCancel : function() {
		this.getThirdpartSellerAppsModWindow().close();
	},
	
	//关联窗口新增APP按钮
	modWindowAddClick : function() {
		var grid = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		appids = [];
		Ext.Array.each(rs, function(record) {
			appids.push(record.data.id);
		});
		
		var form = Ext.create('CMS.view.operation.thirdpart.ThirdpartSellerModUnCheckedAppsGrid');
		form.store.proxy.extraParams.appIds = appids.join(",");
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.operation.thirdpart.ThirdpartSellerModUnCheckedAppsWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//关联窗口删除产品按钮
	modWindowDelClick : function() {
		var grid = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm > gridpanel');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择相应的数据行');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					Ext.Array.each(rs, function(record) {
						grid.getStore().remove(record);
					});
				}
			});
		}
	},
	
	modAppCheckWindowSaveClick : function() {
		var grid = this.getThirdpartSellerModUnCheckedAppsWindow().down('thirdpartSellerModUnCheckedAppsGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的应用!');
			return;
		}
		
		var modWindowGrid = this.getThirdpartSellerAppsModWindow().down('thirdpartSellerAppsModForm > gridpanel');
		var modWindowStore = modWindowGrid.getStore();
		Ext.Array.each(rs, function(rec) {
			modWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getThirdpartSellerModUnCheckedAppsWindow().close();
	},
	
	modAppCheckWindowCancelClick : function() {
		this.getThirdpartSellerModUnCheckedAppsWindow().close();
	}
});
