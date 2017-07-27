Ext.define('CMS.controller.operation.sms.SmsMailController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.sms.SmsMailManager'],
	stores : ['CMS.store.operation.sms.SmsMailgroupStore'],
	refs : [{
		ref : 'smsMailManagerView',
		selector : 'smsMailManagerView'
	},{
		ref : 'smsMailerWindow',
		selector : 'smsMailerWindow'
	},{
		ref : 'smsMailSellerWindow',
		selector : 'smsMailSellerWindow'
	}],
	init : function() {
		this.control({
			'smsMailManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('smsMailgroupManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'smsMailerWindow > smsMailerPanel > gridpanel' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('smsMailerPanel-rowediting');
					roweditplugin.on({
						canceledit : this.mailerroweditcancel,
					    edit: this.mailerroweditupdate,
					    scope: this
					});
				}
			},
			'smsMailManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'smsMailManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'smsMailManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'smsMailManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			'smsMailManagerView > toolbar > button[name=mailer]' : {//组联系人按钮
				click : this.mailer
			},
			'smsMailerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupMailerSave
			},
			'smsMailerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupMailerCancel
			},
			'smsMailerWindow > smsMailerPanel > gridpanel > toolbar > button[name=add]' : {//联系人添加
				click : this.mailerAdd
			},
			'smsMailerWindow > smsMailerPanel > gridpanel > toolbar > button[name=delete]' : {//联系人删除
				click : this.mailerDel
			},
			'smsMailManagerView > toolbar > button[name=seller]' : {//渠道设置按钮
				click : this.seller
			},
			'smsMailSellerWindow > toolbar > button[name=save]' : {//组联系人保存
				click : this.groupSellerSave
			},
			'smsMailSellerWindow > toolbar > button[name=cancel]' : {//组联系人取消
				click : this.groupSellerCancel
			}
			
		});
	},
	
	onAddClick : function() {
		var grid = this.getSmsMailManagerView();
		var roweditplugin = grid.getPlugin('smsMailgroupManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.sms.SmsMailgroupModel', {
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
		
		var grid = this.getSmsMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/smsMail_saveGroup.action',
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
		var grid = this.getSmsMailManagerView();
		grid.store.reload();
	},
	
	onDelClick : function() {
		var grid = this.getSmsMailManagerView();
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
						url : '../operation/smsMail_deleteGroup.action',
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
		var grid = this.getSmsMailManagerView();
		var queryParams = {};
		queryParams.groupName = this.getSmsMailManagerView().down('toolbar > textfield[name=groupName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getSmsMailManagerView();
		var groupNameField = grid.down('toolbar > textfield[name=groupName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		groupNameField.reset();
	},
	
	mailer : function() {
		var grid = this.getSmsMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.sms.SmsMailerWindow');
			var panel = Ext.create('CMS.view.operation.sms.SmsMailerPanel');
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
		var groupId = this.getSmsMailerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getSmsMailerWindow().down('smsMailerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/smsMail_saveGroupMailer.action',
				params : {
					'groupId' : groupId,
					'mailerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getSmsMailerWindow().close();
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
		this.getSmsMailerWindow().close();
	},
	
	mailerAdd : function() {
		var grid = this.getSmsMailerWindow().down('smsMailerPanel').down('gridpanel');
		var roweditplugin = grid.getPlugin('smsMailerPanel-rowediting');
		roweditplugin.cancelEdit();
		console.dir(roweditplugin);
		var r = Ext.create('CMS.model.operation.sms.SmsMailerModel', {
            name: '姓名',
            email:'邮箱'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	mailerDel : function() {
		var groupId = this.getSmsMailerWindow().down('textfield[name=groupId]').getValue();
		var grid = this.getSmsMailerWindow().down('smsMailerPanel').down('gridpanel');
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
						url : '../operation/smsMail_deleteMailer.action',
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
		
		var grid = this.getSmsMailerWindow().down('smsMailerPanel > gridpanel');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/smsMail_saveMailer.action',
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
		var grid = this.getSmsMailerWindow().down('smsMailerPanel > gridpanel');
		grid.store.reload();
	},
	
	seller : function() {
		var grid = this.getSmsMailManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择查看记录');
			return;
		} else {
			var groupid = rs[0].data.id;
			var name = rs[0].data.name;
			
			var win = Ext.create('CMS.view.operation.sms.SmsMailSellerWindow');
			var panel = Ext.create('CMS.view.operation.sms.SmsMailSellerPanel');
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
		var groupId = this.getSmsMailSellerWindow().down('textfield[name=groupId]').getValue();
		
		var grid = this.getSmsMailSellerWindow().down('smsMailSellerPanel').down('gridpanel');
		var store = grid.getStore();
		var sm = grid.getSelectionModel();
		var records = sm.getSelection();
		var arr = [];
		Ext.Array.each(records, function(record) {
			arr.push(record.data.id);
		});
		if(arr.length > 0){
			Ext.Ajax.request({
				url : '../operation/smsMail_saveGroupSeller.action',
				params : {
					'groupId' : groupId,
					'sellerIds' : arr.join(",")
				},
				success : function(action) {
					var respText = Ext.JSON.decode(action.responseText);
					Ext.Msg.alert('提示', respText.msg);
					//关闭窗口
					me.getSmsMailSellerWindow().close();
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
		this.getSmsMailSellerWindow().close();
	}
	
});
