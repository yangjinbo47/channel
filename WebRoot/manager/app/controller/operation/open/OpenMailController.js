Ext.define('CMS.controller.operation.open.OpenMailController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenMailManager'],
	stores : ['CMS.store.operation.open.OpenMailgroupStore'],
	refs : [{
		ref : 'openMailManagerView',
		selector : 'openMailManagerView'
	},{
		ref : 'openMailerWindow',
		selector : 'openMailerWindow'
	},{
		ref : 'openMailSellerWindow',
		selector : 'openMailSellerWindow'
	}],
	init : function() {
		this.control({
			'openMailManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('openMailgroupManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'openMailerWindow > openMailerPanel > gridpanel' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('openMailerPanel-rowediting');
					roweditplugin.on({
						canceledit : this.mailerroweditcancel,
					    edit: this.mailerroweditupdate,
					    scope: this
					});
				}
			},
			'openMailManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'openMailManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'openMailManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'openMailManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			'openMailManagerView > toolbar > button[name=mailer]' : {//组联系人按钮
				click : this.mailer
			},
			'openMailerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupMailerSave
			},
			'openMailerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupMailerCancel
			},
			'openMailerWindow > openMailerPanel > gridpanel > toolbar > button[name=add]' : {//联系人添加
				click : this.mailerAdd
			},
			'openMailerWindow > openMailerPanel > gridpanel > toolbar > button[name=delete]' : {//联系人删除
				click : this.mailerDel
			},
			'openMailManagerView > toolbar > button[name=seller]' : {//渠道设置按钮
				click : this.seller
			},
			'openMailSellerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupSellerSave
			},
			'openMailSellerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupSellerCancel
			}
			
		});
	},
	
	onAddClick : function() {
		var grid = this.getOpenMailManagerView();
		var roweditplugin = grid.getPlugin('openMailgroupManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.open.OpenMailgroupModel', {
            name: '邮件组名称'
        });
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
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
		
		var grid = this.getOpenMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/openMail_saveGroup.action',
				params : {
					'id' : id,
					'name' : row.data.name
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
		var grid = this.getOpenMailManagerView();
		grid.store.reload();
	},
	
	onDelClick : function() {
		var grid = this.getOpenMailManagerView();
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
						url : '../operation/openMail_deleteGroup.action',
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
	
	search : function() {
		var grid = this.getOpenMailManagerView();
		var queryParams = {};
		queryParams.groupName = this.getOpenMailManagerView().down('toolbar > textfield[name=groupName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getOpenMailManagerView();
		var groupNameField = grid.down('toolbar > textfield[name=groupName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		groupNameField.reset();
	},
	
	mailer : function() {
		var grid = this.getOpenMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.open.OpenMailerWindow');
			var panel = Ext.create('CMS.view.operation.open.OpenMailerPanel');
			with(panel.form){
				panel.mstore.proxy.extraParams.groupId = groupid;
				var mailerGrid = panel.down('gridpanel');
				var arr = [];
				panel.mstore.load({
					callback : function(r, options, success) {
						Ext.Array.each(r, function(record) {
							if(record.data.select == true){
								arr.push(record);
							}
						});
						var mailerSm = mailerGrid.getSelectionModel();
						mailerSm.select(arr);
					}
				});
				
				findField('groupId').setValue(groupid);
				findField('name').setValue(name);
			}
			win.add(panel);
			win.show();
		}
	},
	
	groupMailerSave : function() {
		var me=this;
		var groupId = this.getOpenMailerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getOpenMailerWindow().down('openMailerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/openMail_saveGroupMailer.action',
				params : {
					'groupId' : groupId,
					'mailerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getOpenMailerWindow().close();
				},
				failure : function(action) {
					Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
				}
			});
		} else {
			Ext.Msg.alert('提示', '您没有选中行');
		}
	},
	
	groupMailerCancel : function() {
		this.getOpenMailerWindow().close();
	},
	
	mailerAdd : function() {
		var grid = this.getOpenMailerWindow().down('openMailerPanel').down('gridpanel');
		var roweditplugin = grid.getPlugin('openMailerPanel-rowediting');
		roweditplugin.cancelEdit();
		console.dir(roweditplugin);
		var r = Ext.create('CMS.model.operation.open.OpenMailerModel', {
            name: '姓名',
            email:'邮箱'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	mailerDel : function() {
		var groupId = this.getOpenMailerWindow().down('textfield[name=groupId]').getValue();
		var grid = this.getOpenMailerWindow().down('openMailerPanel').down('gridpanel');
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
						url : '../operation/openMail_deleteMailer.action',
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
	
	mailerroweditupdate : function(plugin) {
		var me = this;
		if(!plugin.editor.isValid()){
			plugin.editor.body.highlight("fb7a7a", {
			    attr: "backgroundColor",
			    easing: 'easeIn',
			    duration: 1000
			});
			return false;
		}
		
		var grid = this.getOpenMailerWindow().down('openMailerPanel > gridpanel');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/openMail_saveMailer.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'email' : row.data.email
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
	
	mailerroweditcancel : function() {
		var grid = this.getOpenMailerWindow().down('openMailerPanel > gridpanel');
		grid.store.reload();
	},
	
	seller : function() {
		var grid = this.getOpenMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.open.OpenMailSellerWindow');
			var panel = Ext.create('CMS.view.operation.open.OpenMailSellerPanel');
			with(panel.form){
				panel.sstore.proxy.extraParams.groupId = groupid;
				var sellerGrid = panel.down('gridpanel');
				var arr = [];
				panel.sstore.load({
					callback : function(r, options, success) {
						Ext.Array.each(r, function(record) {
							if(record.data.select == true){
								arr.push(record);
							}
						});
						var sellerSm = sellerGrid.getSelectionModel();
						sellerSm.select(arr);
					}
				});
				
				findField('groupId').setValue(groupid);
				findField('name').setValue(name);
			}
			win.add(panel);
			win.show();
		}
	},
	
	groupSellerSave : function() {
		var me=this;
		var groupId = this.getOpenMailSellerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getOpenMailSellerWindow().down('openMailSellerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/openMail_saveGroupSeller.action',
				params : {
					'groupId' : groupId,
					'sellerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getOpenMailSellerWindow().close();
				},
				failure : function(action) {
					Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
				}
			});
		} else {
			Ext.Msg.alert('提示', '您没有选中行');
		}
	},
	
	groupSellerCancel : function() {
		this.getOpenMailSellerWindow().close();
	}
	
});
