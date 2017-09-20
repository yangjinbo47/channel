Ext.define('CMS.controller.operation.open.OpenOrderConversionRateController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.open.OpenOrderConversionRateManager'],
	stores : ['CMS.store.operation.open.OpenOrderConversionRateStore', 'CMS.store.operation.open.OpenSellerStore'],
	refs : [{
		ref : 'openOrderConversionRateView',
		selector : 'openOrderConversionRateView'
	}],
	init : function() {
		this.control({
			'openOrderConversionRateView > toolbar > combobox[name=sellerId]' : {
				change : this.comboboxChange
			}
		});
	},
	
	comboboxChange : function(combobox, newValue, oldValue, eOpts) {
		var panel = this.getOpenOrderConversionRateView();
		var chart = panel.down('chart[name=chart]');
		chart.store.proxy.extraParams.sellerId = newValue;
		chart.store.loadPage(1);
	}
});