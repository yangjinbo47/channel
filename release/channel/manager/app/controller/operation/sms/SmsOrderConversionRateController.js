Ext.define('CMS.controller.operation.sms.SmsOrderConversionRateController', {
	extend : 'Ext.app.Controller',
	views : ['CMS.view.operation.sms.SmsOrderConversionRateManager'],
	stores : ['CMS.store.operation.sms.SmsOrderConversionRateStore', 'CMS.store.operation.sms.SmsSellerStore'],
	refs : [{
		ref : 'smsOrderConversionRateView',
		selector : 'smsOrderConversionRateView'
	}],
	init : function() {
		this.control({
			'smsOrderConversionRateView > toolbar > combobox[name=sellerId]' : {
				change : this.comboboxChange
			}
		});
	},
	
	comboboxChange : function(combobox, newValue, oldValue, eOpts) {
		var panel = this.getSmsOrderConversionRateView();
		var chart = panel.down('chart[name=chart]');
		chart.store.proxy.extraParams.sellerId = newValue;
		chart.store.loadPage(1);
	}
});