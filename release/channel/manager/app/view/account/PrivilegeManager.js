/**
 * 权限菜单管理页面
 */
Ext.define('CMS.view.account.PrivilegeManager',{
	extend : 'Ext.panel.Panel',
	xtype : 'privilegeManager',
	layout : 'border',
	requires : ['Ext.ux.NumberCount','Ext.ux.Spotlight','Ext.ux.form.ISearchField'],
	initComponent : function(){
		this.callParent(arguments);
		
		this.spot = Ext.create('Ext.ux.Spotlight', {
	        easing: 'easeOut',
	        duration: 300
	    });
		
		this.contextMenu = Ext.create('Ext.menu.Menu',{
			items : [{
				text : '添加',
				iconCls : 'icon-add',
				name : 'add'
			},{
				text : '修改',
				iconCls : 'icon-edit',
				name : 'edit'
			},{
				text : '删除',
				iconCls : 'icon-delete',
				name : 'delete'
			},'-',{
				text : '展开',
				iconCls : 'icon-expand',
				name : 'expand'
			},{
				text : '展开全部',
				iconCls : 'icon-expandAll',
				name : 'expandall'
			},{
				text : '折叠',
				iconCls : 'icon-collapse',
				name : 'collapse'
			},{
				text : '折叠全部',
				iconCls : 'icon-collapseAll',
				name : 'collapseall'
			},'-',{
				text : '上移',
				iconCls : 'icon-up',
				name : 'up'
			},{
				text : '下移',
				iconCls : 'icon-down',
				name : 'down'
			},'-',{
				text : '刷新',
				iconCls : 'icon-refresh',
				name : 'refresh'
			}]
		});
		
		this.treePanel = Ext.create('Ext.tree.Panel',{
			bodyStyle : 'border-bottom:none;',
			region : 'center',
			split : true,
			rootVisible:false,
			hideHeaders: false,
			useArrows : true,
			animate : false,
			allowDeselect : true,
			store : 'CMS.store.account.PrivilegeStore',
			columns : [{
                xtype: 'treecolumn',
                header : '名称',
                hideable : false,
                dataIndex: 'text',
                flex : 1
            },{
            	header : '状态',
            	dataIndex : 'status',
            	width : 100,
            	renderer : this.renderStatus
            }],
            viewConfig: {
            	loadMask : true,
            	emptyText : '~没有数据~',
                plugins: [{
                    ptype: 'treeviewdragdrop',
                    pluginId : 'functionMenuDragDropPlugin',
                    ddGroup: 'functionMenuDDGroup',
                    enableDrop: true
                }]
            },
            bbar : [{
            	xtype : 'isearchfield',
            	flex : 1,
//            	hideTrigger1 : false,
//            	hideTrigger2 : false,
            	enableKeyEvents : true,
            	emptyText : '输入权限菜单名称过滤',
            	name : 'privilegetext'
            }]
		});
		
		this.editPanel = Ext.create('Ext.form.Panel',{
			name : 'editpanel',
			bodyPadding : 5,
			defaults : {
				anchor : '92%',
				labelWidth : 65,
				labelAlign : 'right'
			},
			items : [{
				xtype : 'hidden',
				name : 'privilegeid'
			},{
				xtype : 'textfield',
				fieldLabel : '<span style="color:red;">* </span>名称',
				name : 'text',
				allowBlank : false,
				maxLength : 500
			},{
				xtype : 'radiogroup',
				fieldLabel : '<span style="color:red;">* </span>状态',
				allowBlank : false,
				columns : 3,
				items : [{
					boxLabel : '启用',
					name : 'status',
					checked : true,
					inputValue : '1'
				},{
					boxLabel : '禁用',
					name : 'status',
					inputValue : '0'
				}]
			},{
				xtype : 'textfield',
				fieldLabel : '<span style="color:red;">* </span> xtype',
				name : 'xtype',
				allowBlank : false,
				maxLength : 64
			},{
				xtype : 'textfield',
				fieldLabel : 'iconCls',
				name : 'iconCls',
				maxLength : 300
			},{
				xtype : 'textarea',
				fieldLabel : 'controller',
				name : 'controller',
				emptyText : '多个controller以英文逗号分隔',
				maxLength : 1024
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '保存',
				name : 'save'
			},{
				text : '取消',
				name : 'cancel'
			}]
			
		});
		
		var tips = '<div style="text-align:center;margin-top:60px;color:#C0C0C0;">'+
					'点击左侧节点或将节点拖至此处来查看</br>' +
					'在左侧面板上可使用鼠标右键快捷操作</br>' +
				'</div>';
		
		this.viewPanel = Ext.create('Ext.panel.Panel',{
			name : 'viewpanel',
			html : tips,
			tplInfo : new Ext.Template(tips),
			tpl : new Ext.XTemplate('<div style="border:1px solid #d9d9d9;border-top:none;border-left:none;font-weight: bold;padding: 4px 3px;">{menutext}&nbsp;信息</div><table border="0" width="360" class="tablecls" style="border-top:none;table-layout: fixed;">',
			  '<tpl if="menulevel != 1">',
				  '<tr><td width="25%">父节点ID</td><td width="75%" style="word-wrap: break-word;">{parentid}</td></tr>',
			  '</tpl>',
			  '<tpl if="!Ext.isEmpty(parentname)">',
				  '<tr><td width="25%">父节点名称</td><td width="75%" style="word-wrap: break-word;">{parentname}</td></tr>',
			  '</tpl>',
			  '<tr><td width="25%">节点ID</td><td width="75%" style="word-wrap: break-word;">{privilegeid}</td></tr>',
			  '<tr><td width="25%">节点级别</td><td width="75%" style="word-wrap: break-word;">{menulevel}&nbsp;级</td></tr>',
			  '<tr><td width="25%">名称</td><td width="75%" style="word-wrap: break-word;">{menutext}</td></tr>',
			  '<tpl if="!Ext.isEmpty(status)">',
				  '<tr><td width="25%">状态</td><td width="75%" style="word-wrap: break-word;">{status:this.getStatus}</td></tr>',
			  '</tpl>',
			  '<tpl if="!Ext.isEmpty(iconCls)">',
				  '<tr><td width="25%">iconCls</td><td width="75%" style="word-wrap: break-word;">{iconCls}</td></tr>',
			  '</tpl>',
			  '<tpl if="menulevel != 1">',
			      '<tr><td width="25%">xtype</td><td width="75%" style="word-wrap: break-word;">{xtype}</td></tr>',
			  '</tpl>',
			  '<tpl if="!Ext.isEmpty(controller)">',
			      '<tr><td width="25%">controller</td><td width="75%" style="word-wrap: break-word;">{controller}</td></tr>',
			  '</tpl>',
			'</table>',{
				getStatus : function(v){
					return {'0':'<span style="color:red;">禁用</span>','1':'<span style="color:green;">启用</span>'}[v];
				}
			})
		});
		
		this.eastPanel = Ext.create('Ext.panel.Panel',{
			name : 'eastpanel',
			region : 'east',
			width : 360,
			layout : 'card',
			activeItem : 0,
			defaults : {
				style: 'border:1px solid #C0C0C0;border-top:none;border-right:none;border-bottom:none;'
			},
			items : [this.viewPanel,this.editPanel]
		});
		
		this.add(this.treePanel,this.eastPanel);
		this.addDocked({
			xtype : 'toolbar',
			dock : 'top',
			style : 'border:1px solid #C0C0C0 !important;border-left:none;border-top:none;border-right:none;',
			items : [{
				text : '添加',
				iconCls : 'icon-add',
				name : 'add'
			},'-',{
				text : '修改',
				iconCls : 'icon-edit',
				name : 'edit'
			},'-',{
				text : '删除',
				iconCls : 'icon-delete',
				name : 'delete'
			}]
		});
	},
	
	renderStatus : function(v){
		return {'0':'<span style="color:red;">禁用</span>','1':'<span style="color:green;">启用</span>'}[v];
	}
});