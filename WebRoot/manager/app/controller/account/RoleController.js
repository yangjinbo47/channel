Ext.define('CMS.controller.account.RoleController', {
	extend : 'Ext.app.Controller',
	stores : [ 'CMS.store.account.RoleStore'],
	views : ['CMS.view.account.RoleManager','CMS.view.account.RolesAddWindow','CMS.view.account.RolesModWindow'],
	refs : [{
		ref : 'roleManager',
		selector : 'roleManager'
	},{
		ref : 'rolesAddWindow',
		selector : 'rolesAddWindow'
	},{
		ref : 'rolesModWindow',
		selector : 'rolesModWindow'
	}],
	init : function() {
		this.control({
			'roleManager > toolbar > button[name=add]' : {//新增
				click : this.onAddClick
			},
			'roleManager > toolbar > button[name=mod]' : {//修改
				click : this.onModClick
			},
			'roleManager > toolbar > button[name=delete]' : {//删除
				click : this.onDelClick
			},
			'roleManager > toolbar > button[name=search]' : {//查询
				click : this.onSearchClick
			},
			'roleManager > toolbar > button[name=reset]' : {//重置
				click : this.onResetClick
			},
			'rolesAddWindow > toolbar > button[name=save]' : {//新增窗口保存按钮
				click : this.addWindowSave
			},
			'rolesAddWindow > toolbar > button[name=cancel]' : {//新增窗口取消按钮
				click : this.addWindowCancel
			},
			'rolesModWindow > toolbar > button[name=save]' : {//修改窗口保存按钮
				click : this.modWindowSave
			},
			'rolesModWindow > toolbar > button[name=cancel]' : {//修改窗口取消按钮
				click : this.modWindowCancel
			},
			'rolesChoosenTreePanel' : {
				selectionchange : this.onNavSelectionChange
			}
		});
	},
	
	onAddClick : function() {
		var win = Ext.create('CMS.view.account.RolesAddWindow');
		var form = Ext.create('CMS.view.account.RolesAddFormPanel');
		win.add(form);
		win.show();
	},
	
	onModClick : function() {
		var grid = this.getRoleManager();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要修改的角色!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个角色进行修改!');
			return;
		} else {
			var id = rs[0].data.roleid;
			var panel = Ext.create('CMS.view.account.RolesModFormPanel');
			with(panel.form){
				findField('roleid').setValue(rs[0].data.roleid);
				findField('roleName').setValue(rs[0].data.name);
				findField('remark').setValue(rs[0].data.remark);
			}
			var rstore = Ext.create('Ext.data.TreeStore', {
				root : {
					expanded : true,
					leaf : false,
					expandable : false,
					text : '根节点',
					id : 'root'
				},
				model : 'CMS.model.account.RolePrivilegeModel',
				proxy : {
					type : 'ajax',
					url : '../account/role_privilegeList.action?id=' + id
				}
			});
			var tree = Ext.create('CMS.view.account.RolesModTreePanel', {
				store : rstore
			});
			panel.add(tree);
			var win = Ext.create('CMS.view.account.RolesModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	onDelClick : function() {
		var grid = this.getRoleManager();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择相应的数据行');
			return;
		} else {
			Ext.Msg.confirm("请确认", "是否真的要删除指定的内容", function(button, text) {
				if (button == "yes") {
					checkids = [];
					Ext.Array.each(rs, function(record) {
						checkids.push(record.data.roleid);
					});
					Ext.Ajax.request({
						url : '../account/role_delete.action',
						params : {
							'ids' : checkids
						},
						success : function(response) {
							var json = Ext.decode(response.responseText);
							if(json.success){
								Ext.Msg.alert('提示',json.msg || '删除成功');
								grid.getStore().load();
							}else{
								Ext.Msg.alert('提示',json.msg ? "删除失败：" + json.msg : '删除失败');
							}
						},
						failure : function() {
							Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
						}
					});
				}
			});
		}
	},
	
	addWindowSave : function() {
		var me = this;
		var form = Ext.getCmp("rolesAddForm").getForm();
		if (!form.isValid()) {
			return;
		}
		var grid = Ext.getCmp("rolesChoosenTreePanel");
		var v = grid.getView();
		var b = v.getChecked();
		checkids = [];
		for ( var i = 0; i < b.length; i++) {
			if (b.length == 1) {
				checkids = b[i].data.id;
			} else {
				if (i < b.length - 1) {
					checkids += b[i].data.id + ",";
				}
				if (i == b.length - 1) {
					checkids += b[i].data.id;
				}
			}
		}
		if (checkids.length < 1) {
			Ext.Msg.alert('提示', '请选择权限!');
			return;
		}
		Ext.getCmp('rolesAddForm').submit({
			params : {
				'ids' : checkids
			},
			success : function(form, action) {
				var result = action.result.msg;
				if (result == 0) {
					result = '添加角色信息成功！';
				}
				Ext.Msg.alert('提示', result);
				me.getRoleManager().getStore().reload();
				me.getRolesAddWindow().close();
			},
			failure : function(form, action) {
				Ext.Msg.alert('提示', action.result.msg);
				me.getRolesAddWindow().close();
			}
		});
	},
	
	addWindowCancel : function() {
		this.getRolesAddWindow().close();
	},
	
	modWindowSave : function() {
		var me = this;
		var form = Ext.getCmp("rolesModForm").getForm();
		if (!form.isValid()) {
			return;
		}
		var grid = Ext.getCmp("rolesModTreePanel");
		var v = grid.getView();
		var b = v.getChecked();
		checkids = [];
		for ( var i = 0; i < b.length; i++) {
			if (b.length == 1) {
				checkids = b[i].data.id;
			} else {
				if (i < b.length - 1) {
					checkids += b[i].data.id + ",";
				}
				if (i == b.length - 1) {
					checkids += b[i].data.id;
				}
			}
		}
		if (checkids.length < 1) {
			Ext.Msg.alert('提示', '请选择权限!');
			return;
		}
		Ext.getCmp('rolesModForm').submit({
			params : {
				'ids' : checkids
			},
			success : function(form, action) {
				var result = action.result.msg;
				if (result == 0) {
					result = '修改角色信息成功！';
				}
				Ext.Msg.alert('提示', result);
				me.getRoleManager().getStore().reload();
				me.getRolesModWindow().close();
			},
			failure : function(form, action) {
				Ext.Msg.alert('提示', action.result.msg);
				me.getRolesModWindow().close();
			}
		});
	},
	
	modWindowCancel : function() {
		this.getRolesModWindow().close();
	},
	
	onSearchClick : function() {
		var grid = this.getRoleManager();
		var roleNameField = this.getRoleManager().down('toolbar > textfield[name=r_name]');
		var queryParams = {};
		
		queryParams.name = roleNameField.getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
//		grid.getStore().loadPage(1,{
//			url : '../account/role_list.action',
//			params : {
//				'name' : roleNameField.getValue()
//			}
//		});
		grid.getView().refresh();
	},
	
	onResetClick : function() {
		var grid = this.getRoleManager();
		var roleNameField = this.getRoleManager().down('toolbar > textfield[name=r_name]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		roleNameField.reset();
	},
	
	onNavSelectionChange : function(selModel, records) {
		var record = records[0];
		name = record.get('name');
		id = record.get('id');
		pid = record.parentNode.data.id;
		pname = record.parentNode.data.name;
		description = record.get('description');
	}
});
