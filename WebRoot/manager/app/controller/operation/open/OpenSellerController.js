Ext.define('CMS.controller.operation.open.OpenSellerController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenSellerManager'],
	stores : ['CMS.store.operation.open.OpenSellerStore'],
	refs : [{
		ref : 'openSellerManagerView',
		selector : 'openSellerManagerView'
	},{
		ref : 'openSellerAppsModWindow',
		selector : 'openSellerAppsModWindow'
	},{
		ref : 'openSellerModUnCheckedAppsWindow',
		selector : 'openSellerModUnCheckedAppsWindow'
	},{
		ref : 'openSellerReportWindow',
		selector : 'openSellerReportWindow'
	}],
	init : function() {
		this.control({
			'openSellerManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('openSellerManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				},
				afterrender : this.hidColumn
			},
			'openSellerManagerView actioncolumn' : {//统计按钮
				render : function(actioncolumn){
					actioncolumn.items[0].handler = Ext.bind(this.report,this);
				}
			},
			'openSellerReportWindow > openSellerReportPanel > gridpanel > toolbar > button[name=report]' : {//统计窗口开始统计按钮
				click : this.startReport
			},
			'openSellerManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'openSellerManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'openSellerManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'openSellerManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			//关联窗口相关
			'openSellerManagerView > toolbar > button[name=relation]' : {
				click : this.onRelationClick
			},
			'openSellerAppsModWindow > toolbar >button[name=save]' : {//关联窗口保存按钮
				click : this.modWindowSave
			},
			'openSellerAppsModWindow > toolbar >button[name=cancel]' : {//关联窗口取消按钮
				click : this.modWindowCancel
			},
			'openSellerAppsModWindow > openSellerAppsModForm > gridpanel > toolbar > button[name=add]' : {//关联窗口新增APP按钮
				click : this.modWindowAddClick
			},
			'openSellerAppsModWindow > openSellerAppsModForm > gridpanel > toolbar > button[name=delete]' : {//关联窗口删除APP按钮
				click : this.modWindowDelClick
			},
			'openSellerModUnCheckedAppsWindow > toolbar > button[name=save]' : {//修改窗口角色选择窗口保存按钮
				click : this.modAppCheckWindowSaveClick
			},
			'openSellerModUnCheckedAppsWindow > toolbar > button[name=cancel]' : {//修改窗口角色选择窗口取消按钮
				click : this.modAppCheckWindowCancelClick
			}
		});
	},
	
	hidColumn : function() {
		var grid = this.getOpenSellerManagerView();
		Ext.Ajax.request({
			url : '../www/getStatus.action',
			disableCaching : true, // 禁止缓存
			timeout : 60000, // 最大等待时间,超出则会触发超时
			method : "GET",
			success : function(response, opts){
				var ret = Ext.JSON.decode(response.responseText); // JSON对象化
                if (ret.success){
					if(ret.operatorType != 0) {//非超级管理员隐藏
						grid.columns[6].hide();
					}
                }
			},
			failure : function(response, opts){
				Ext.TaskManager.stop(checkActiveTask); // 停止该定时任务
				Ext.Msg.alert('提 示','还没有登陆或者操作已超时，请重新登陆',function(){
					window.location.href='index.jsp';
				});
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getOpenSellerManagerView();
		var roweditplugin = grid.getPlugin('openSellerManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.open.OpenSellerModel', {
            name: '渠道名',
            email:'邮箱',
            contact:'联系人',
            telephone:'联系电话',
            sellerKey:'渠道KEY',
            sellerSecret:'渠道secret',
            callbackUrl:'通知渠道方地址',
            status:'状态',
            companyShow:0
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getOpenSellerManagerView();
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
						url : '../operation/openSeller_delete.action',
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
		
		var grid = this.getOpenSellerManagerView();
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
				url : '../operation/openSeller_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'email' : row.data.email,
					'contact' : row.data.contact,
					'telephone' : row.data.telephone,
					'sellerKey' : row.data.sellerKey,
					'sellerSecret' : row.data.sellerSecret,
					'callbackUrl' : row.data.callbackUrl,
					'status' : row.data.status,
					'companyShow' : row.data.companyShow
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
		var grid = this.getOpenSellerManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getOpenSellerManagerView();
		var queryParams = {};
		queryParams.sellerName = this.getOpenSellerManagerView().down('toolbar > textfield[name=sellerName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getOpenSellerManagerView();
		var sellerNameField = grid.down('toolbar > textfield[name=sellerName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		sellerNameField.reset();
	},
	
	report : function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		var sellerId = rec.get('id');
		var name = rec.get('name');
		
		var win = Ext.create('CMS.view.operation.open.OpenSellerReportWindow');
		var panel = Ext.create('CMS.view.operation.open.OpenSellerReportPanel');
		with(panel.form){
			panel.rstore.proxy.extraParams.sellerId = sellerId;
			panel.rstore.loadPage(1);
			
			findField('sellerId').setValue(sellerId);
			findField('name').setValue(name);
		}
		win.add(panel);
		win.show();
	},
	
	startReport : function() {
		var me = this,sellerReportPanel = me.getOpenSellerReportWindow().down('form'),grid = sellerReportPanel.down('gridpanel');
		var startTimeField = sellerReportPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = sellerReportPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
		
		if(startTimeField.getValue() == null) {
			Ext.tipbox.msg('提示', '请选择查询开始时间');
			return;
		}
		if(endTimeField.getValue() == null) {
			Ext.tipbox.msg('提示', '请选择查询结束时间');
			return;
		}
		if(startTimeField.lastActiveError.length != 0){
			return;
		}
		if(endTimeField.lastActiveError.length != 0){
			return;
		}
		
		var sellerIdField = sellerReportPanel.down('textfield[name=sellerId]');
		var sellerId = sellerIdField.getValue();
		var queryParams = {};
		queryParams.sellerId = sellerId;
		queryParams.startTime = startTimeField.getValue();
		queryParams.endTime = endTimeField.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	onRelationClick : function() {
		var grid = this.getOpenSellerManagerView();
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
			var panel = Ext.create('CMS.view.operation.open.OpenSellerAppsModFormPanel');
			panel.appstore.proxy.extraParams.sellerId = sellerid;
			panel.appstore.loadPage(1);
			
			with(panel.form){
				findField('sellerId').setValue(rs[0].data.id);
				findField('name').setValue(rs[0].data.name);
			}
			var win = Ext.create('CMS.view.operation.open.OpenSellerAppsModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	modWindowSave : function() {
		var me=this;
		var form = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加APP！');
			return;
		}
		var sellerId = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm > textfield[name=sellerId]').getValue();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		appIds = [];
		appLimit = [];
		Ext.Array.each(rs, function(record) {
			appIds.push(record.data.id);
			appLimit.push(record.data.appLimit);
		});
		Ext.Ajax.request({
			url : '../operation/openSeller_saveSellerAppRelation.action',
			params : {
				'sellerId' : sellerId,
				'appIds' : appIds.join(","),
				'appLimit' : appLimit.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getOpenSellerAppsModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	modWindowCancel : function() {
		this.getOpenSellerAppsModWindow().close();
	},
	
	//关联窗口新增APP按钮
	modWindowAddClick : function() {
		var grid = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		appids = [];
		Ext.Array.each(rs, function(record) {
			appids.push(record.data.id);
		});
		
		var form = Ext.create('CMS.view.operation.open.OpenSellerModUnCheckedAppsGrid');
		form.store.proxy.extraParams.appIds = appids.join(",");
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.operation.open.OpenSellerModUnCheckedAppsWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//关联窗口删除产品按钮
	modWindowDelClick : function() {
		var grid = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm > gridpanel');
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
		var grid = this.getOpenSellerModUnCheckedAppsWindow().down('openSellerModUnCheckedAppsGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的应用!');
			return;
		}
		
		var modWindowGrid = this.getOpenSellerAppsModWindow().down('openSellerAppsModForm > gridpanel');
		var modWindowStore = modWindowGrid.getStore();
		Ext.Array.each(rs, function(rec) {
			modWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getOpenSellerModUnCheckedAppsWindow().close();
	},
	
	modAppCheckWindowCancelClick : function() {
		this.getOpenSellerModUnCheckedAppsWindow().close();
	}
	
	
});
