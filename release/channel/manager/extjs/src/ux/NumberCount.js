
/**
 * 统计实时显示输入框里还剩下可以输入字符的个数
 * @class Ext.ux.NumberCount
 * @extends Ext.Component
 */
 Ext.define('Ext.ux.NumberCount',{
 	alias: 'plugin.numbercount',
    mixins: {
        observable: 'Ext.util.Observable'
    },
    maxLength : 256,
	height : 20,
	clear : false,
	init : function(field) {
		var me = this;
		this.maxLength = field.maxLength || this.maxLength;
		this.target = field;
		field.pluginInitValue = Ext.bind(this.onKeyUp,this);
		field.mon(field, 'render', function(){
			this.initExtendEl();

			Ext.override(field,{
				setValue : function(v){
			        this.callParent(arguments);
			        field.pluginInitValue.call();
			        return this;
			    }
			});
		}, this);
		
		field.reset = function(){
	        field.beforeReset();
	        field.setValue(me.originalValue);
	        field.clearInvalid();
	        delete field.wasValid;
	        me.onKeyUp();
		};
	},
	
	initExtendEl : function() {
		this.target.inputEl.setStyle('overflow-y','auto');//只有在需要时才出现纵向滚动条
		this.txtEl = null;
		
		if(Ext.isChrome){
			var node = document.createElement('div');
			node.setAttribute('class','x-form-item-label');
			node.setAttribute('style','display:none;color:#C0C0C0;width:45px;border-radius:3px;cursor:pointer;position:absolute;text-align:center;font-size:11px;border:none;z-index:10;');
			node.innerHTML = this.maxLength;
			document.getElementById(this.target.inputId).parentNode.appendChild(node);
			this.txtEl = Ext.get(node);
		}else{
			this.texthandlerEl = new Ext.Template(
				'<div class="x-form-item-label" style="display:none;color:#C0C0C0;border-radius:3px;width:45px;cursor:pointer;position:absolute;text-align:center;font-size:11px;border:none;z-index:10;">',
					this.maxLength, 
				'</div>');
			this.txtEl = this.texthandlerEl.insertAfter(this.target.inputEl, null, true);
		}
		
		
		this.txtEl.on('click', this.doClearValue, this);
		this.target.mon(this.target.inputEl, 'keyup', this.onKeyUp, this);
		this.target.mon(this.target.inputEl, 'change', this.onKeyUp, this);
		
		this.target.on({
			scope : this,
			resize : this.restrictPosition,
			blur : this.onTipboxBlur,
			focus : this.onTipboxFocus
		});
		
		this.restrictPosition();
	},
	
	onTipboxBlur : function(){
		if(this.txtEl){
			this.txtEl.setStyle({
				'color' : '#C0C0C0',
				'display' : 'none',
				'background-color' : 'transparent'
			});
		}
	},
	
	onTipboxFocus : function(){
		if(this.txtEl){
			this.txtEl.setStyle({
				'color' : '#000000',
				'display' : 'inherit',
				'background-color' : '#E0E0E0'
			});
			this.restrictPosition();
		}
	},
	
	restrictPosition : function(){
		if(this.txtEl){
			var txtElWidth = this.txtEl.getComputedWidth();
			this.txtEl.alignTo(this.target.inputEl,'br-br?',[-20,-5]);
		}
	},

	doClearValue : function() {
		if (!this.clear || this.target.disabled || this.target.readOnly) {
			return;
		}
		this.target.setValue('');
		this.target.setRawValue('');
		this.target.clearInvalid();
	},

	doUpdateTxt : function(txt) {
		if(this.txtEl){
			this.txtEl.update(txt);
		}
	},
	
	reset : function(){
		this.callParent(arguments);
		this.onKeyUp();
	},

	onKeyUp : function() {
		this.target = this.target || this;
		var v = this.target.getValue() || this.target.value;
		if (!Ext.isEmpty(v)) {
			var length = this.maxLength - v.length;
			this.doUpdateTxt(length < 0?('溢出' + Math.abs(length)):length);
		} else {
			this.doUpdateTxt(this.maxLength);
		}
		this.restrictPosition();
	}
    
 });