Ext.define('CMS.controller.operation.pack.PushMailController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.pack.PushMailManager'],
	stores : ['CMS.store.operation.pack.PushMailgroupStore'],
	refs : [{
		ref : 'pushMailManagerView',
		selector : 'pushMailManagerView'
	},{
		ref : 'pushMailerWindow',
		selector : 'pushMailerWindow'
	},{
		ref : 'pushMailSellerWindow',
		selector : 'pushMailSellerWindow'
	}],
	init : function() {
		this.control({
			'pushMailManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('pushMailgroupManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'pushMailerWindow > pushMailerPanel > gridpanel' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('pushMailerPanel-rowediting');
					roweditplugin.on({
						canceledit : this.mailerroweditcancel,
					    edit: this.mailerroweditupdate,
					    scope: this
					});
				}
			},
			'pushMailManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'pushMailManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'pushMailManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'pushMailManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			'pushMailManagerView > toolbar > button[name=mailer]' : {//组联系人按钮
				click : this.mailer
			},
			'pushMailerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupMailerSave
			},
			'pushMailerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupMailerCancel
			},
			'pushMailerWindow > pushMailerPanel > gridpanel > toolbar > button[name=add]' : {//联系人添加
				click : this.mailerAdd
			},
			'pushMailerWindow > pushMailerPanel > gridpanel > toolbar > button[name=delete]' : {//联系人删除
				click : this.mailerDel
			},
			'pushMailManagerView > toolbar > button[name=seller]' : {//渠道设置按钮
				click : this.seller
			},
			'pushMailSellerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupSellerSave
			},
			'pushMailSellerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupSellerCancel
			}
			
		});
	},
	
	onAddClick : function() {
		var grid = this.getPushMailManagerView();
		var roweditplugin = grid.getPlugin('pushMailgroupManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.pack.PushMailgroupModel', {
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
		
		var grid = this.getPushMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/pushMail_saveGroup.action',
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
		var grid = this.getPushMailManagerView();
		grid.store.reload();
	},
	
	onDelClick : function() {
		var grid = this.getPushMailManagerView();
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
						url : '../operation/pushMail_deleteGroup.action',
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
		var grid = this.getPushMailManagerView();
		var queryParams = {};
		queryParams.groupName = this.getPushMailManagerView().down('toolbar > textfield[name=groupName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getPushMailManagerView();
		var groupNameField = grid.down('toolbar > textfield[name=groupName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		groupNameField.reset();
	},
	
	mailer : function() {
		var grid = this.getPushMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.pack.PushMailerWindow');
			var panel = Ext.create('CMS.view.operation.pack.PushMailerPanel');
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
		var groupId = this.getPushMailerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getPushMailerWindow().down('pushMailerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/pushMail_saveGroupMailer.action',
				params : {
					'groupId' : groupId,
					'mailerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getPushMailerWindow().close();
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
		this.getPushMailerWindow().close();
	},
	
	mailerAdd : function() {
		var grid = this.getPushMailerWindow().down('pushMailerPanel').down('gridpanel');
		var roweditplugin = grid.getPlugin('pushMailerPanel-rowediting');
		roweditplugin.cancelEdit();
		console.dir(roweditplugin);
		var r = Ext.create('CMS.model.operation.pack.PushMailerModel', {
            name: '姓名',
            email:'邮箱'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	mailerDel : function() {
		var groupId = this.getPushMailerWindow().down('textfield[name=groupId]').getValue();
		var grid = this.getPushMailerWindow().down('pushMailerPanel').down('gridpanel');
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
						url : '../operation/pushMail_deleteMailer.action',
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
		
		var grid = this.getPushMailerWindow().down('pushMailerPanel > gridpanel');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/pushMail_saveMailer.action',
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
		var grid = this.getPushMailerWindow().down('pushMailerPanel > gridpanel');
		grid.store.reload();
	},
	
	seller : function() {
		var grid = this.getPushMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.pack.PushMailSellerWindow');
			var panel = Ext.create('CMS.view.operation.pack.PushMailSellerPanel');
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
		var groupId = this.getPushMailSellerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getPushMailSellerWindow().down('pushMailSellerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/pushMail_saveGroupSeller.action',
				params : {
					'groupId' : groupId,
					'sellerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getPushMailSellerWindow().close();
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
		this.getPushMailSellerWindow().close();
	}
	
});
