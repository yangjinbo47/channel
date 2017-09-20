Ext.define('CMS.controller.operation.pack.PushPackageController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.pack.PushPackageManager'],
	stores : ['CMS.store.operation.pack.PushPackageStore','CMS.store.operation.pack.PushPackageChannelStore'],
	refs : [{
		ref : 'pushPackageManagerView',
		selector : 'pushPackageManagerView'
	},{
		ref : 'pushPackageLimitWindow',
		selector : 'pushPackageLimitWindow'
	}],
	init : function() {
		this.control({
			'pushPackageManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('pushPackageManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				},
				afterrender : this.hidColumn
			},
			'pushPackageManagerView > toolbar > button[name=add]' : {
				click : this.addPackage
			},
			'pushPackageManagerView > toolbar > button[name=delete]' : {
				click : this.deletePackage
			},
			'pushPackageManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'pushPackageManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			},
			'pushPackageManagerView > toolbar > button[name=limit]' : {//限量按钮
				click : this.onLimitClick
			},
			'pushPackageLimitWindow > toolbar >button[name=save]' : {//省份限量窗口保存按钮
				click : this.limitWindowSave
			},
			'pushPackageLimitWindow > toolbar >button[name=cancel]' : {//省份限量窗口取消按钮
				click : this.limitWindowCancel
			}
		});
	},
	
	addPackage : function() {
		var grid = this.getPushPackageManagerView();
		var roweditplugin = grid.getPlugin('pushPackageManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.pack.PackageModel', {
            packageName: '包月名称',
            packageUrl: '入口地址',
            packageSentence: '推荐名',
            excludeArea: '排除区域',
            recChannel: '渠道号',
            status: '状态',
            price:'200',
            type:'通道类型',
            packageLimit: '200',
            excludeAreaArray:'排除区域',
            companyShow:0
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	deletePackage : function() {
		var grid = this.getPushPackageManagerView();
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
						url : '../operation/pushPackage_delete.action',
						params : {
							'packageIds' : ids
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
		
		var grid = this.getPushPackageManagerView();
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
				url : '../operation/pushPackage_save.action',
				params : {
					'packageId' : id,
					'packageName' : row.data.packageName,
					'packageUrl' : row.data.packageUrl,
					'packageSentence' : row.data.packageSentence,
					'recChannel' : row.data.recChannel,
					'status' : row.data.status,
					'price' : row.data.price,
					'type' : row.data.type,
					'packageLimit' : row.data.packageLimit,
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
		var grid = this.getPushPackageManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getPushPackageManagerView();
		var queryParams = {};
		queryParams.packageName = this.getPushPackageManagerView().down('toolbar > textfield[name=packageName]').getValue();
		queryParams.channelName = this.getPushPackageManagerView().down('toolbar > textfield[name=channelName]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getPushPackageManagerView();
		var packageNameField = grid.down('toolbar > textfield[name=packageName]');
		var channelField = grid.down('toolbar > textfield[name=channelName]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		packageNameField.reset();
		channelField.reset();
	},
	
	onLimitClick : function() {
		var grid = this.getPushPackageManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		if (!rs.length) {
			Ext.Msg.alert('提示', '请选择一个需要限量的包!');
			return;
		} else if (rs.length > 1) {
			Ext.Msg.alert('提示', '只能选择一个包进行限量!');
			return;
		} else {
			var packageid = rs[0].data.id;

			var form = Ext.create('CMS.view.operation.pack.PushPackageLimitFormPanel');
			form.store.proxy.extraParams.packageId = packageid;
			form.store.loadPage(1);
			
			var win = Ext.create('CMS.view.operation.pack.PushPackageLimitWindow');
			win.add(form);
			win.show();
		}
	},
	
	limitWindowSave : function() {
		var me=this;
		var grid = this.getPushPackageLimitWindow().down('gridpanel');
		var store = grid.getStore();
		var count = store.getCount();
		var rs = store.getRange(0,count-1);
		var json = {},limits = [];
		Ext.Array.each(rs, function(record) {
			limits.push({
				packageId : record.data.packageId,
				province : record.data.province,
				dayLimit : record.data.dayLimit,
				monthLimit : record.data.monthLimit
			})
		});
		json.limits = limits;
		Ext.Ajax.request({
			url : '../operation/pushPackage_savePackageLimit.action',
			params : {
				'limits' : JSON.stringify(json.limits)
			},
			success : function(action) {
				var respText = Ext.JSON.decode(action.responseText);
				Ext.Msg.alert('提示', respText.msg);
				//关闭窗口
				me.getPushPackageLimitWindow().close();
			},
			failure : function(action) {
				Ext.Msg.alert('提示', '系统故障，删除失败，请和系统管理员联系');
			}
		});
	},
	
	limitWindowCancel : function() {
		this.getPushPackageLimitWindow().close();
	},
	
	hidColumn : function() {
		var grid = this.getPushPackageManagerView();
		Ext.Ajax.request({
			url : '../www/getStatus.action',
			disableCaching : true, // 禁止缓存
			timeout : 60000, // 最大等待时间,超出则会触发超时
			method : "GET",
			success : function(response, opts){
				var ret = Ext.JSON.decode(response.responseText); // JSON对象化
                if (ret.success){
					if(ret.operatorType != 0) {//非超级管理员隐藏
						grid.columns[9].hide();
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
	}
});