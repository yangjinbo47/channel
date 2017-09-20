
Ext.define('Ext.ux.InputTiper',{
	alias: 'plugin.inputtiper',
    mixins: {
        observable: 'Ext.util.Observable'
    },
    anchor : 'bottom',
    thousandsFormat : true,
    init : function(field){
    	field.enableKeyEvents = true;
    	this.field = field;
    	this.tipbox = Ext.create('Ext.tip.QuickTip',{
    		title : null,
    		anchor : this.anchor,
    		autoHide : false,
    		target : field.inputId
    	});

    	field.on({
    		scope : this,
    		focus : this.onFieldFocus,
    		keyup : this.onFieldKeyup,
    		blur : this.onFieldBlur
    	});
    },

    onFieldKeyup : function(){
    	var me = this, v = me.field.getValue();
		this.onFieldFocus();
    },

    onFieldFocus : function(){
    	var me = this, v = me.field.getValue();
    	if(!Ext.isEmpty(v)){
    		if(me.tipbox.rendered){
	    		Ext.apply(me.tipbox,{anchorOffset : this.getAnchorOffset()});
    		}
    		this.tipbox.update(Ext.String.format('<span class="inputtiper">{0}</span>',me.format(v)));
    		me.tipbox.setTarget(me.field.inputEl);
    		me.tipbox.show();
    	}else{
    		me.onFieldBlur();
    	}
    },

    onFieldBlur : function(){
    	this.tipbox.setTarget('');
    	this.tipbox.update('');
    	this.tipbox.hide();
    },

    getAnchorOffset : function(){
    	var tipboxWidth = this.tipbox.getWidth(), fieldWidth = this.field.getWidth();
    	var result = 10;
    	if(tipboxWidth <= fieldWidth){
    		result = tipboxWidth / 2;
    	}else {
    		result = fieldWidth / 2;
    	}
    	result = result - 20;
    	return result < 0 ? 0 : result;
    },

    format : function(s){
    	// if(/[^0-9\.]/.test(s)) return "invalid value";  
        // s=s.replace(/^(\d*)$/,"$1.");  
        // s=(s+"00").replace(/(\d*\.\d\d)\d*/,"$1");  
        // s=s.replace(".",",");  
        // var re=/(\d)(\d{3},)/;  
        // while(re.test(s))  
        //         s=s.replace(re,"$1,$2");  
        // s=s.replace(/,(\d\d)$/,".$1");  
        // return s.replace(/^\./,"0.")  
        if(this.thousandsFormat){
        	s = s.replace(/^(\d*)$/,"$1.");  
        	s = s.replace(/(\d*\.\d\d)\d*/,"$1");  
        	s = s.replace(".",",");  
        	var re=/(\d)(\d{3},)/;  
        	while(re.test(s))  
                s=s.replace(re,"$1,$2");  
        	s = s.replace(/,(\d\d)$/,".$1");  
        	return s.replace(/^\./,"0.").replace(/,$/,'');
        }
    }


});