Ext.define('CMS.controller.operation.open.OpenAppController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenAppManager'],
	stores : ['CMS.store.operation.open.OpenAppStore','CMS.store.operation.open.OpenMerchantStore'],
	refs : [{
		ref : 'openAppManagerView',
		selector : 'openAppManagerView'
	},{
		ref : 'openAppProductModWindow',
		selector : 'openAppProductModWindow'
	},{
		ref : 'openAppModUnCheckedProductWindow',
		selector : 'openAppModUnCheckedProductWindow'
	},{
		ref : 'openAppLimitModWindow',
		selector : 'openAppLimitModWindow'
	},{
		ref : 'openAppReportWindow',
		selector : 'openAppReportWindow'
	}],
	init : function() {
		this.control({
			'openAppManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('openAppManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				},
				afterrender : this.hidColumn
			},
			'openAppManagerView actioncolumn' : {//统计按钮
				render : function(actioncolumn){
					actioncolumn.items[0].handler = Ext.bind(this.report,this);
				}
			},
			'openAppReportWindow > openAppReportPanel > gridpanel > toolbar > button[name=report]' : {//统计窗口开始统计按钮
				click : this.startReport
			},
			'openAppReportWindow > openAppReportPanel > gridpanel > toolbar > button[name=export]' : {//统计窗口开始导出按钮
				click : this.exportExcel
			},
			'openAppManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'openAppManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'openAppManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'openAppManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			
			'openAppManagerView > toolbar > button[name=relation]' : {
				click : this.onRelationClick
			},
			'openAppProductModWindow > toolbar >button[name=save]' : {//关联窗口保存按钮
				click : this.modWindowSave
			},
			'openAppProductModWindow > toolbar >button[name=cancel]' : {//关联窗口取消按钮
				click : this.modWindowCancel
			},
			'openAppProductModWindow > openAppProductModForm > gridpanel > toolbar > button[name=add]' : {//关联窗口新增产品按钮
				click : this.modWindowAddClick
			},
			'openAppProductModWindow > openAppProductModForm > gridpanel > toolbar > button[name=delete]' : {//关联窗口删除产品按钮
				click : this.modWindowDelClick
			},
			'openAppModUnCheckedProductWindow > toolbar > button[name=save]' : {//修改窗口产品选择窗口保存按钮
				click : this.modProductCheckWindowSaveClick
			},
			'openAppModUnCheckedProductWindow > toolbar > button[name=cancel]' : {//修改窗口产品选择窗口取消按钮
				click : this.modProductCheckWindowCancelClick
			},
			'openAppManagerView > toolbar > button[name=limit]' : {//限量按钮
				click : this.onLimitClick
			},
			'openAppLimitModWindow > toolbar >button[name=save]' : {//省份限量窗口保存按钮
				click : this.limitWindowSave
			},
			'openAppLimitModWindow > toolbar >button[name=cancel]' : {//省份限量窗口取消按钮
				click : this.limitWindowCancel
			}
		});
	},
	
	hidColumn : function() {
		var grid = this.getOpenAppManagerView();
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
		var grid = this.getOpenAppManagerView();
		var roweditplugin = grid.getPlugin('openAppManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.open.OpenAppModel', {
            name: '应用名',
            merchantId:'所属商户',
            appKey:'app_key',
            appSecret:'app_secret',
            callbackUrl:'通知地址',
            excludeAreaArray:'排除区域',
            clientId:'client_id',
            companyShow:0
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getOpenAppManagerView();
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
						url : '../operation/openApp_delete.action',
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
	
	onRelationClick : function() {
		var grid = this.getOpenAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要关联的APP!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个APP进行关联!');
			return;
		} else {
			var appid = rs[0].data.id;
			var panel = Ext.create('CMS.view.operation.open.OpenAppProductModFormPanel');
			panel.productstore.proxy.extraParams.appId = appid;
			panel.productstore.loadPage(1);
			
			with(panel.form){
				findField('appId').setValue(rs[0].data.id);
				findField('name').setValue(rs[0].data.name);
			}
			var win = Ext.create('CMS.view.operation.open.OpenAppProductModWindow');
			win.add(panel);
			win.show();
		}
	},
	
	modWindowSave : function() {
		var me=this;
		var form = this.getOpenAppProductModWindow().down('openAppProductModForm').getForm();
		if (!form.isValid()) {
			return;
		}
		// 此处需添加新增用户时未选择角色的判断提示
		var grid = this.getOpenAppProductModWindow().down('openAppProductModForm > gridpanel');
		var store = grid.getStore();
		if (!store.getCount()) {
			Ext.Msg.alert('提示', '请添加产品！');
			return;
		}
		var appId = this.getOpenAppProductModWindow().down('openAppProductModForm > textfield[name=appId]').getValue();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		productIds = [];
		Ext.Array.each(rs, function(record) {
			productIds.push(record.data.id);
		});
		Ext.Ajax.request({
			url : '../operation/openApp_saveAppProductRelation.action',
			params : {
				'appId' : appId,
				'productIds' : productIds.join(",")
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getOpenAppProductModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	modWindowCancel : function() {
		this.getOpenAppProductModWindow().close();
	},
	
	//关联窗口新增产品按钮
	modWindowAddClick : function() {
		var appGrid = this.getOpenAppManagerView();
		var appsm = appGrid.getSelectionModel();
		var apprs = appsm.getSelection();
		var merchantId = apprs[0].data.merchantId;
		
		var grid = this.getOpenAppProductModWindow().down('openAppProductModForm > gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		productids = [];
		Ext.Array.each(rs, function(record) {
			productids.push(record.data.id);
		});
		
		var form = Ext.create('CMS.view.operation.open.OpenAppModUnCheckedProductsGrid');
		form.store.proxy.extraParams.productIds = productids.join(",");
		form.store.proxy.extraParams.merchantId = merchantId;
		form.store.loadPage(1);
		
		var win = Ext.create('CMS.view.operation.open.OpenAppModUnCheckedProductsWindow');
		win.add(form);
		win.show();
		win.center();
	},
	
	//关联窗口删除产品按钮
	modWindowDelClick : function() {
		var grid = this.getOpenAppProductModWindow().down('openAppProductModForm > gridpanel');
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
	
	modProductCheckWindowSaveClick : function() {
		var grid = this.getOpenAppModUnCheckedProductWindow().down('openAppModUnCheckedProductsGrid');
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要添加的产品!');
			return;
		}
		
		var modWindowGrid = this.getOpenAppProductModWindow().down('openAppProductModForm > gridpanel');
		var modWindowStore = modWindowGrid.getStore();
//		checkids = [];
		Ext.Array.each(rs, function(rec) {
//			checkids.push(rec.data.id);
			
			modWindowStore.insert(0, rec);//修改窗口添加记录
		});
		this.getOpenAppModUnCheckedProductWindow().close();
	},
	
	modProductCheckWindowCancelClick : function() {
		this.getOpenAppModUnCheckedProductWindow().close();
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
		
		var grid = this.getOpenAppManagerView();
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
				url : '../operation/openApp_save.action',
				params : {
					'id' : id,
					'name' : row.data.name,
					'merchantId' : row.data.merchantId,
					'appKey' : row.data.appKey,
					'appSecret' : row.data.appSecret,
					'callbackUrl' : row.data.callbackUrl,
					'clientId' : row.data.clientId,
					'companyShow' : row.data.companyShow
//					'excludeArea' : row.data.excludeArea,
//					'excludeAreaArray' : row.data.excludeAreaArray
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
		var grid = this.getOpenAppManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getOpenAppManagerView();
		var queryParams = {};
		queryParams.appName = this.getOpenAppManagerView().down('toolbar > textfield[name=appName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getOpenAppManagerView();
		var appNameField = grid.down('toolbar > textfield[name=appName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		appNameField.reset();
	},
	
	report : function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		var appId = rec.get('id');
		var name = rec.get('name');
		
		var win = Ext.create('CMS.view.operation.open.OpenAppReportWindow');
		var panel = Ext.create('CMS.view.operation.open.OpenAppReportPanel');
		with(panel.form){
			panel.rstore.proxy.extraParams.appId = appId;
			panel.rstore.loadPage(1);
			
			findField('appId').setValue(appId);
			findField('name').setValue(name);
		}
		win.add(panel);
		win.show();
	},
	
	startReport : function() {
		var me = this,appReportPanel = me.getOpenAppReportWindow().down('form'),grid = appReportPanel.down('gridpanel');
		var startTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
		
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
		
		var appIdField = appReportPanel.down('textfield[name=appId]');
		var appId = appIdField.getValue();
		var queryParams = {};
		queryParams.appId = appId;
		queryParams.startTime = startTimeField.getValue();
		queryParams.endTime = endTimeField.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	exportExcel : function() {
		var me = this,appReportPanel = me.getOpenAppReportWindow().down('form'),grid = appReportPanel.down('gridpanel');
		var startTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=startTime]');
		var endTimeField = appReportPanel.down('gridpanel > toolbar > datetimefield[name=endTime]');
		
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
		
		var appIdField = appReportPanel.down('textfield[name=appId]');
		var appId = appIdField.getValue();
		var startTime = me.formatDate("yyyy-MM-dd HH:mm:ss",startTimeField.getValue());
		var endTime = me.formatDate("yyyy-MM-dd HH:mm:ss",endTimeField.getValue());
		window.open('../operation/openApp_export.action?appId='+appId+'&startTime='+startTime+'&endTime='+endTime);
	},
	
	formatDate : function (fmt,date) {
	    var o = {
	        "M+": date.getMonth() + 1, //月份 
	        "d+": date.getDate(), //日 
	        "H+": date.getHours(), //小时 
	        "m+": date.getMinutes(), //分 
	        "s+": date.getSeconds(), //秒 
	        "q+": Math.floor((date.getMonth() + 3) / 3), //季度 
	        "S": date.getMilliseconds() //毫秒 
	    };
	    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
	    for (var k in o)
	    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	    return fmt;
	},
	
	onLimitClick : function() {
		var grid = this.getOpenAppManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要关联的渠道!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个渠道进行关联!');
			return;
		} else {
			var appid = rs[0].data.id;

			var form = Ext.create('CMS.view.operation.open.OpenAppLimitModFormPanel');
			form.store.proxy.extraParams.appId = appid;
			form.store.loadPage(1);
			
			var win = Ext.create('CMS.view.operation.open.OpenAppLimitModWindow');
			win.add(form);
			win.show();
		}
	},
	
	limitWindowSave : function() {
		var me=this;
		var grid = this.getOpenAppLimitModWindow().down('gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		var json = {},limits = [];
		Ext.Array.each(rs, function(record) {
			limits.push({
				appId : record.data.appId,
				province : record.data.province,
				dayLimit : record.data.dayLimit,
				monthLimit : record.data.monthLimit,
				userDayLimit : record.data.userDayLimit,
				userMonthLimit : record.data.userMonthLimit,
				reduce : record.data.reduce
			})
		});
		json.limits = limits;
		Ext.Ajax.request({
			url : '../operation/openApp_saveAppLimit.action',
			params : {
				'limits' : JSON.stringify(json.limits)
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getOpenAppLimitModWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	limitWindowCancel : function() {
		this.getOpenAppLimitModWindow().close();
	}
});
