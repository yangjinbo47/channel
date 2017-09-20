Ext.define('CMS.view.account.UserAddWindow', {
	extend : 'Ext.window.Window',
	xtype : 'userAddWindow',
	id : 'userAddWindow',
	title : '添加用户',
	modal : true,
	resizable : false,
	width : 500,
	height : 400,
	minWidth : 300,
	minHeight : 200,
	layout : 'fit',
	constrainHeader : true,
	dockedItems : [ {
		xtype : 'toolbar',
		dock : 'bottom',
		ui : 'footer',
		layout : {
			pack : 'center'
		},
		items : [ {
			minWidth : 80,
			text : '保存',
			name : 'save'
//			handler : function() {
//				var form = Ext.getCmp("userAddForm").getForm();
//				if (!form.isValid()) {
//					return;
//				}
//				// 此处需添加新增用户时未选择角色的判断提示
//				var grid = Ext.getCmp("userAddRolesGrid").getStore();
//				if (!grid.getCount()) {
//					Ext.Msg.alert('提示', '请添加用户角色！');
//					return;
//				}
//				Ext.getCmp('userAddForm').submit({
//					success : function(form, action) {
//						var result = action.result.msg;
//						Ext.Msg.alert('提示', result);
//						Ext.getCmp("userGrid").getStore().reload();
//						Ext.getCmp("userAddWindow").hide();
//					},
//					failure : function(form, action) {
//						Ext.Msg.alert('提示', '添加用户失败！');
//						Ext.getCmp("userAddWindow").close();
//					}
//				});
//				Ext.getCmp("userGrid").getStore().reload();
//				// Ext.getCmp("userAddWindow").close();
//				Ext.getCmp("userAddWindow").hide();
//			}
		}, {
			minWidth : 80,
			text : '取消',
			name : 'cancel'
//			handler : function() {
//				this.ownerCt.ownerCt.close();
//			}
		}
		]
	} ]
});
