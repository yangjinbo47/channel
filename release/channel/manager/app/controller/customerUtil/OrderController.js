Ext.define('CMS.controller.customerUtil.OrderController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.customerUtil.OrderManager'],
	stores : ['CMS.store.customerUtil.OrderStore'],
	refs : [{
		ref : 'customerUtilOrderManagerView',
		selector : 'customerUtilOrderManagerView'
	}],
	
	init : function() {
		this.control({
			'customerUtilOrderManagerView > toolbar > button[name=search]' : {
				click : this.search
			},
			'customerUtilOrderManagerView > toolbar > button[name=reset]' : {
				click : this.reset
			}
		});
	},
	
	search : function() {
		var me = this,grid = me.getCustomerUtilOrderManagerView();
		var phoneFidld = grid.down('toolbar > textfield[name=phone]');
		
		var queryParams = {};
		queryParams.phone = phoneFidld.getValue();
		
		grid.getStore().proxy.extraParams = queryParams;
		grid.getStore().loadPage(1);
	},
	
	reset : function() {
		var me = this,grid = me.getCustomerUtilOrderManagerView();
		var phoneFidld = grid.down('toolbar > textfield[name=phone]');
		
		grid.getStore().proxy.extraParams = {};
		grid.getStore().load();
		grid.getView().refresh();
		
		phoneFidld.reset();
	}
	
});