Ext.define('CMS.controller.account.OperatorController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.account.OperatorManager'],
	stores : ['CMS.store.account.OperatorStore'],
	refs : [ {
		ref : 'operatorManager',
		selector : 'operatorManager'
	},{
		ref : 'userAddWindow',
		selector : 'userAddWindow'
	},{
		ref : 'userAddUnCheckedRoleWindow',
		selector : 'userAddUnCheckedRoleWindow'
	},{
		ref : 'userModWindow',
		selector : 'userModWindow'
	},{
		ref : 'userModUnCheckedRoleWindow',
		selector : 'userModUnCheckedRoleWindow'
	}],
	
	init : function() {
		this.control({
			'operatorManager > toolbar > button[name=add]' : {//新增
				click : this.onAddClick
			},
			'operatorManager > toolbar > button[name=mod]' : {//修改
				click : this.onModClick
			},
			'operatorManager > toolbar > button[name=delete]' : {//删除
				click : this.onDelClick
			},
			'operatorManager > toolbar > button[name=search]' : {//查询
				click : this.onSearchClick
			},
			'operatorManager > toolbar > button[name=reset]' : {//重置
				click : this.onResetClick
			},
			//新增相关
			'userAddWindow > toolbar > button[name=save]' : {//修改窗口新增按钮
				click : this.addWindowSave
			},
			'userAddWindow > toolbar > button[name=cancel]' : {//修改窗口取消按钮
				click : this.addWindowCancel
			},
			'userAddWindow > userAddForm > gridpanel > toolbar > button[name=add]' : {//新增窗口新增角色按钮
				click : this.addWindowAddClick
			},
			'userAddWindow > userAddForm > gridpanel > toolbar > button[name=delete]' : {//新增窗口删除角色按钮
				click : this.addWindowDelClick
			},
			'userAddUnCheckedRoleWindow > toolbar > button[name=save]' : {//新增窗口角色选择窗口保存按钮
				click : this.addRoleCheckWindowSaveClick
			},
			'userAddUnCheckedRoleWindow > toolbar > button[name=cancel]' : {//新增窗口角色选择窗口取消按钮
				click : this.addRoleCheckWindowCancelClick
			},
			//修改相关
			'userModWindow > toolbar > button[name=save]' : {//修改窗口新增按钮
				click : this.modWindowSave
			},
			'userModWindow > toolbar > button[name=cancel]' : {//修改窗口取消按钮
				click : this.modWindowCancel
			},
			'userModWindow > userModForm > gridpanel > toolbar > button[name=add]' : {//修改窗口新增角色按钮
				click : this.modWindowAddClick
			},
			'userModWindow > userModForm > gridpanel > toolbar > button[name=delete]' : {//修改窗口删除角色按钮
				click : this.modWindowDelClick
			},
			'userModUnCheckedRoleWindow > toolbar > button[name=save]' : {//修改窗口角色选择窗口保存按钮
				click : this.modRoleCheckWindowSaveClick
			},
			'userModUnCheckedRoleWindow > toolbar > button[name=cancel]' : {//修改窗口角色选择窗口取消按钮
				click : this.modRoleCheckWindowCancelClick
			}
		});
	},
	
	//新增
	onAddClick : function() {
		var win = this.getUserAddWindow();
		if (win == null) {
			var form = Ext.create('CMS.view.account.UserAddFormPanel');
			var win = Ext.create('CMS.view.account.UserAddWindow');
			win.add(form);
		}
		win.down('userAddForm').getForm().reset();
		win.show();
	},
	
	//修改
	onModClick : function() {
		var grid = this.getOperatorManager();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要修改的用户!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个用户进行修改!');
			return;
		} else {
			var operid = rs[0].data.operid;
			var panel = Ext.create('CMS.view.account.UserModFormPanel');
			panel.rolestore.proxy.extraParams.operid = operid;
			panel.rolestore.loadPage(1);
			
			with(panel.form){
				findField('operid').setValue(rs[0].data.operid);
				findField('loginName').setValue(rs[0].data.loginName);
				findField('name').setValue(rs[0].data.name);
				findField('type').setValue(rs[0].data.type);
			}
			var win = Ext.create('CMS.view.account.UserModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	//删除
	onDelClick : function() {
		var grid = this.getOperatorManager();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择要删除的用户');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个用户进行删除');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					var ids = '';
					Ext.each(rs, function(row, index, value) {
						ids = ids + row.data.operid + ',';
					});
					ids = ids.slice(0, -1);
					if (ids == 1) {
						Ext.Msg.alert('提示', '不能删除超级管理员用户！');
						return;
					}
					Ext.Ajax.request({
						url : '../account/user_delete.action',
						params : {
							'operid' : ids
						},
						success : function(action) {
							var respText = Ext.JSON.decode(action.responseText);
							Ext.Msg.alert('提示', respText.msg);
							grid.getStore().load();
						},
						failure : function(action) {
							Ext.Msg.alert('提示','系统故障，删除失败，请和系统管理员联系！');
						}
					});
				}
			});
		}
	},
	
	//查询
	onSearchClick : function() {
		var grid = this.getOperatorManager();
//		grid.getStore().loadPage(1,{
//			url : '../account/user_list.action',
//			params : {
//				'name' : this.getOperatorManager().down('toolbar > textfield[name=u_name]').getValue()
//			}
//		});
		var queryParams = {};
		
		queryParams.name = this.getOperatorManager().down('toolbar > textfield[name=u_name]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		
		grid.getView().refresh();
	},
	
	//重置
	onResetClick : function() {
		var grid = this.getOperatorManager();
		var userNameField = this.getOperatorManager().down('toolbar > textfield[name=u_name]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		userNameField.reset();
	},
	
	//新增窗口保存按钮
	addWindowSave : function() {
		var me=this;
		var form = this.getUserAddWindow().down('userAddForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getUserAddWindow().down('userAddForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加用户角色！');
			return;
		}
		var name = this.getUserAddWindow().down('userAddForm > textfield[name=name]').getValue();
		var loginName = this.getUserAddWindow().down('userAddForm > textfield[name=loginName]').getValue();
		var password = this.getUserAddWindow().down('userAddForm > textfield[name=password]').getValue();
		var type = this.getUserAddWindow().down('userAddForm > textfield[name=type]').getValue();
		if (isNaN(type)){//非数字
			Ext.Msg.alert('温馨提示','请选择可见类型');
			return;
		}
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		roleids = [];
		Ext.Array.each(rs, function(record) {
			roleids.push(record.data.roleid);
		});
		Ext.Ajax.request({
			url : '../account/user_save.action',
			params : {
				'name' : name,
				'loginName' : loginName,
				'password' : password,
				'type' : type,
				'roleIds' : roleids.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getUserAddWindow().close();
				//刷新数据
				me.getOperatorManager().getStore().reload();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	//新增窗口取消按钮
	addWindowCancel : function() {
		this.getUserAddWindow().close();
	},
	
	//新增窗口新增角色按钮
	addWindowAddClick : function() {
		var grid = this.getUserAddWindow().down('userAddForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		roleids = [];
		Ext.Array.each(rs, function(record) {
			roleids.push(record.data.roleid);
		});
		
		var form = Ext.create('CMS.view.account.UserAddUnCheckedRolesGrid');
		form.store.proxy.extraParams.roleIds = roleids.join(",");
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.account.UserAddUnCheckedRoleWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//新增窗口删除角色按钮
	addWindowDelClick : function() {
		var grid = this.getUserAddWindow().down('userAddForm > gridpanel');
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
	
	//新增窗口角色选择窗口保存按钮
	addRoleCheckWindowSaveClick : function() {
		var grid = this.getUserAddUnCheckedRoleWindow().down('userAddUnCheckedRolesGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的角色!');
			return;
		}
		
		var addWindowGrid = this.getUserAddWindow().down('userAddForm > gridpanel');
		var addWindowStore = addWindowGrid.getStore();
		checkids = [];
		Ext.Array.each(rs, function(rec) {
			checkids.push(rec.data.roleid);
			
			addWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getUserAddUnCheckedRoleWindow().close();
	},
	
	//新增窗口角色选择窗口取消按钮
	addRoleCheckWindowCancelClick : function() {
		this.getUserAddUnCheckedRoleWindow().close();
	},
	
	
	
	//修改窗口新增按钮
	modWindowSave : function() {
		var me=this;
		var form = this.getUserModWindow().down('userModForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getUserModWindow().down('userModForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加用户角色！');
			return;
		}
		var operid = this.getUserModWindow().down('userModForm > textfield[name=operid]').getValue();
		var name = this.getUserModWindow().down('userModForm > textfield[name=name]').getValue();
		var password = this.getUserModWindow().down('userModForm > textfield[name=password]').getValue();
		var type = this.getUserModWindow().down('userModForm > textfield[name=type]').getValue();
		if (isNaN(type)){//非数字
			Ext.Msg.alert('温馨提示','请选择可见类型');
			return;
		}
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		roleids = [];
		Ext.Array.each(rs, function(record) {
			roleids.push(record.data.roleid);
		});
		Ext.Ajax.request({
			url : '../account/user_save.action',
			params : {
				'operid' : operid,
				'name' : name,
				'password' : password,
				'type' : type,
				'roleIds' : roleids.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getUserModWindow().close();
				//刷新数据
				me.getOperatorManager().getStore().reload();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	//修改窗口取消按钮
	modWindowCancel : function() {
		this.getUserModWindow().close();
	},
	
	//修改窗口新增角色按钮
	modWindowAddClick : function() {
		var grid = this.getUserModWindow().down('userModForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		roleids = [];
		Ext.Array.each(rs, function(record) {
			roleids.push(record.data.roleid);
		});
		
		var form = Ext.create('CMS.view.account.UserModUnCheckedRolesGrid');
		form.store.proxy.extraParams.roleIds = roleids.join(",");
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.account.UserModUnCheckedRoleWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//修改窗口删除角色按钮
	modWindowDelClick : function() {
		var grid = this.getUserModWindow().down('userModForm > gridpanel');
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
	
	//修改窗口角色选择窗口保存按钮
	modRoleCheckWindowSaveClick : function() {
		var grid = this.getUserModUnCheckedRoleWindow().down('userModUnCheckedRolesGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的角色!');
			return;
		}
		
		var modWindowGrid = this.getUserModWindow().down('userModForm > gridpanel');
		var modWindowStore = modWindowGrid.getStore();
		checkids = [];
		Ext.Array.each(rs, function(rec) {
			checkids.push(rec.data.roleid);
			
			modWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getUserModUnCheckedRoleWindow().close();
	},
	
	//修改窗口角色选择窗口取消按钮
	modRoleCheckWindowCancelClick : function() {
		this.getUserModUnCheckedRoleWindow().close();
	}
});