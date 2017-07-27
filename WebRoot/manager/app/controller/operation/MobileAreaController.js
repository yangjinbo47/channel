Ext.define('CMS.controller.operation.MobileAreaController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.MobileAreaManager'],
	stores : ['CMS.store.operation.MobileAreaStore'],
	refs : [{
		ref : 'mobileAreaManagerView',
		selector : 'mobileAreaManagerView'
	}],
	init : function() {
		this.control({
			'mobileAreaManagerView' : {
				render : function(grid) {
					var roweditplugin = grid.getPlugin('mobileAreaManager-rowediting');
					roweditplugin.on({
						canceledit : this.roweditcancel,
					    edit: this.roweditupdate,
					    scope: this
					});
				}
			},
			'mobileAreaManagerView > toolbar > button[name=add]' : {
				click : this.onAddClick
			},
			'mobileAreaManagerView > toolbar > button[name=delete]' : {
				click : this.onDelClick
			},
			'mobileAreaManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'mobileAreaManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	onAddClick : function() {
		var grid = this.getMobileAreaManagerView();
		var roweditplugin = grid.getPlugin('mobileAreaManager-rowediting');
		roweditplugin.cancelEdit();
		var r = Ext.create('CMS.model.operation.MobileAreaModel', {
            firstNum: '133',
            middleNum: '0571',
            address: '省市',
            province: '省份',
            city: '城市',
            brand: '运营商'
        });
        
        grid.store.insert(0, r);
        roweditplugin.startEdit(0,0);
	},
	
	onDelClick : function() {
		var grid = this.getMobileAreaManagerView();
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
						url : '../operation/mobileArea_delete.action',
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
		
		var grid = this.getMobileAreaManagerView();
		var sm = grid.getSelectionModel();
		var rs = sm.getSelection();
		
		Ext.each(grid.getSelectionModel().getSelection(), function(row, index, value) {
			var id;
			if(row.data.id){
				id = row.data.id;
			}
			
			Ext.Ajax.request({
				url : '../operation/mobileArea_save.action',
				params : {
					'id' : id,
					'firstNum' : row.data.firstNum,
					'middleNum' : row.data.middleNum,
					'address': row.data.address,
					'province': row.data.province,
					'city': row.data.city,
					'brand': row.data.brand
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
		var grid = this.getMobileAreaManagerView();
		grid.store.reload();
	},
	
	search : function() {
		var grid = this.getMobileAreaManagerView();
		var queryParams = {};
		queryParams.firstNum = this.getMobileAreaManagerView().down('toolbar > textfield[name=firstNum]').getValue();
		queryParams.middleNum = this.getMobileAreaManagerView().down('toolbar > textfield[name=middleNum]').getValue();
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().reload();
		grid.getView().refresh();
	},
	
	reset : function() {
		var grid = this.getMobileAreaManagerView();
		var firstNumField = grid.down('toolbar > textfield[name=firstNum]');
		var middleNumField = grid.down('toolbar > textfield[name=middleNum]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		firstNumField.reset();
		middleNumField.reset();
	}
});
