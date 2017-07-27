Ext.define('CMS.controller.MainController', {
	extend : 'Ext.app.Controller',
	refs : [{
		ref : 'navigation',
		selector : 'cmsviewport > panel[name=west]'
	},{
		ref : 'contentPanel',
		selector : 'cmsviewport > tabpanel[name=center]'
	}],
	init : function() {
		this.control({
			'cmsviewport > panel[name=west] panel:first' : {
				beforerender : this.firstTreeInit
			},
			'cmsviewport > panel[name=west] panel' : {
				expand : this.showTreePanel
			}
		});
	},
	firstTreeInit : function(panel) {
		this.showTreePanel(panel);
	},
	showTreePanel : function(panel) {
		// 只会请求一次数据
		if (Ext.isEmpty(panel.child('treepanel'))) {
			var treepanel = Ext.create('Ext.tree.Panel', {
				useArrows : true,
				rootVisible : false,
				viewConfig : {
					loadMask : true
				},
				store : Ext.create('Ext.data.TreeStore', {
					defaultRootId : panel.id,
					fields : [{ name : 'privilegeid', type : 'string' }, {
						name : 'text', type : 'string' }, {
						name : 'parentid', type : 'string' }, {
						name : 'leaf', type : 'boolean' }, {
						name : 'xtype', type : 'string' },{
						name : 'controller', type : 'string' },{
						name : 'iconCls', type : 'string' }],
					root : {
						expanded : true,
						leaf : false,
						expandable : false,
						text : '根节点'
					},
					proxy : {
						type : 'ajax',
						url : '../account/user_getMenu.action'
					}
				})
			});

			// 绑定树节点选择改变事件
			treepanel.on({
				// 这里的scope指定方法所在的作用域范围(鉴定事件默认的作用域为事件本身)
				itemclick : {
					fn : this.onTreeSelectionChange,
					scope : this
				}
			});
			panel.add(treepanel);
		}
	},
	afterViewportLayout : function() {
		if (!this.navigationSelected) {
			var id = location.hash.substring(1), navigation = this.getNavigation(), store = navigation.getStore();
			var node = id ? store.getNodeById(id) : store.getRootNode().firstChild.firstChild;
			navigation.getSelectionModel().select(node);
			navigation.getView().focusNode(node);
			this.navigationSelected = true;
		}
	},
	onTreeSelectionChange : function(view, record, item, index, e) {
		var text = record.get('text'), xtype = record.get('xtype'), iconcls = record.get('iconCls'), alias = 'widget.' + xtype, 
			controller = record.get('controller'),contentPanel = this.getContentPanel(), cmp;
		var comp = Ext.getCmp(xtype + '_id');
		if (xtype) {
			if (comp) {
				comp.show();
				return;
			}
			
			//在数据库里保存controller即可实现controller动态加载
			var outerPanel = Ext.create('Ext.panel.Panel',{
				id : xtype + '_id',
				iconCls : Ext.isEmpty(iconcls) ? 'x-tree-icon-leaf' : iconcls.replace('tree-',''),
				border : false,
				layout : 'fit',
				closable : true,
				title : text
			});
			outerPanel.update(Ext.String.format('<div style="width: 100%; margin-left: 46%;"><span style="top: 35%; position: absolute;" class="loading16"></span><span style="margin-left: 22px; top: 35%; position: absolute;">{0}加载中...</span></div>',text));
			contentPanel.add(outerPanel);
			contentPanel.setActiveTab(outerPanel);//将添加的页面设为激活的
			
			try{
				//初始化controller
				var viewControllers = controller.split(',');
				for(var k = 0;k < viewControllers.length;k++){
					if(!Ext.ClassManager.isCreated(viewControllers[k])){
					    Ext.create(viewControllers[k]).init();
					}
				}
			
				var className = Ext.ClassManager.getNameByAlias(alias);
				if (!Ext.isEmpty(className)) {
					var ViewClass = Ext.ClassManager.get(className);
					var clsProto = ViewClass.prototype;
					if (clsProto.themes) {
						clsProto.themeInfo = clsProto.themes[Ext.themeName] || clsProto.themes.classic;
					}
					cmp = Ext.create(ViewClass);
					outerPanel.update('');
					outerPanel.add(cmp);
					
					if(!this.documentTitle){
						this.documentTitle = document.title;
					}
					document.title = Ext.String.format('{0} - {1}',this.documentTitle,text);
					location.hash = xtype;
				}else{
					outerPanel.update(Ext.String.format('<div style="margin: 0px; top: 35%; color: red; font-weight: bold; position: absolute; display: block; width: 100%; text-align: center;">{0} 加载异常<br>Class name is null.</div>',text));
					throw 'Error:Class name is null.';
				}
			}catch(e){
				outerPanel.update(Ext.String.format('<div style="margin: 0px; top: 35%; color: red; font-weight: bold; position: absolute; display: block; width: 100%; text-align: center;">{0} 加载异常<br>{1}</div>',text,e));
				throw e;
			}
		}else{
			throw 'missing xtype for the ' + text;
		}
	}
});
