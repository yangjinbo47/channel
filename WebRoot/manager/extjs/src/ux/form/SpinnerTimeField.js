Ext.define('Ext.ux.form.SpinnerTimeField', {
    extend: 'Ext.form.field.Spinner',
    alias: 'widget.spinnertimefield',

    step : 1,
    timer : false,//总是显示当前时间，true-时间走动,false-显示初始化的时间
    interval : 1000,
    focusStop : true,
    repeatTriggerClick : false,
    enableKeyEvents : true,
    cursorPosition : {start : 0,end : 0},
    editable : false,
    timeFormat : 'H:i:s',
    initComponent : function(){
    	this.callParent(arguments);

    	if(this.timer === true){
    		this.timeTask = {
    			scope : this,
    			run : this.updateTime,
    			interval : this.interval
    		};

    		this.timeRunner = new Ext.util.TaskRunner();
    		this.timeRunner.start(this.timeTask);
    	}
    	
    },

    updateTime : function(time){
    	if(!Ext.isString(time) || Ext.isEmpty(time)){
    		time = Ext.Date.format(new Date(),this.timeFormat);
    	}
    	this.setValue(time);
    },

    afterRender : function(){
    	this.callParent(arguments);

    	this.inputText = this.inputEl.dom;
    	this.inputEl.on({
    		scope : this,
    		'mouseup' : this.onSelectInputText,
    		'focus' : this.onTimeRunnerStop,
    		'blur' : this.onTimeRunnerStart,
    		'keyup' : this.onInputTextKeyup
    	});
    },

    onTimeRunnerStart : function(){
    	if(this.timer === true && this.focusStop){
    		Ext.defer(function(){
    			this.timeRunner.start(this.timeTask);
    		},300,this);
    	}
    },

    onTimeRunnerStop : function(){
    	if(this.timer === true && this.focusStop){
    		this.timeRunner.stop(this.timeTask);
    	}
    },

    onInputTextKeyup : function(e,t,o){
    	e.preventDefault();
    	if(e.keyCode == 37){ // left
    		this.onSelectInputText(e,-3);
    	}else if(e.keyCode == 39){ // right
    		this.onSelectInputText(e,3);
    	}else{
    		var pos = this.cursorPosition;
    		this.selectStartToEnd(pos.start,pos.end);
    	}
    },

    onSelectInputText : function(e,adjNum){
    	e.preventDefault();
    	//如果是鼠标事件，则位置需要用this.getCursorPosition()，如果keyup事件，则需要用this.cursorPosition
    	//判断可以使用e.type或e.browserEvent.type == 'mouseup'或e.button
    	var pos = (e.button == 0 || e.button == 1 || e.button == 2)?this.getCursorPosition():this.cursorPosition , v = this.getValue();
    	if(pos.start == pos.end){
    		var loc = pos.start;
    		if(loc < 3){
    			this.selectStartToEnd(0,2);
    		}else if(loc >= 3 && loc < 6){
    			this.selectStartToEnd(3,5);
    		}else if(loc >= 6 && loc < 9){
    			this.selectStartToEnd(6,8);
    		}
    	}else{
    		var vLength = this.getValue().length;
    		var newStart = pos.start + adjNum,newEnd = pos.end + adjNum;
    		if(newStart > vLength - 2){
    			newStart = 0;
    			newEnd = 2;
    		}else if(newStart < 0){
    			newStart = vLength - 2;
    			newEnd = vLength;
    		}
    		this.selectStartToEnd(Math.abs(newStart),Math.abs(newEnd));
    	}
    	return false;
    },

    onSpinUp : function(){
    	var beforeText = this.lastSelectedText || this.getSelectedText(), baseNum = 60;
    	this.beforeSpin();

    	if(isNaN(this.cursorPosition.start) || this.cursorPosition.start == 0 || Ext.isEmpty(beforeText) || beforeText.length > 2){
    		baseNum = 24;
    		this.selectStartToEnd(0,2);
    	}

    	var afterText = (Number) (beforeText);
    	this.setText(this.formatNumber(Math.abs(afterText + this.step) % baseNum),this.cursorPosition.start,this.cursorPosition.end);
    },

    onSpinDown : function(){
    	var beforeText = this.lastSelectedText || this.getSelectedText(), baseNum = 60;
    	this.beforeSpin();

    	if(isNaN(this.cursorPosition.start) || this.cursorPosition.start == 0 || Ext.isEmpty(beforeText) || beforeText.length > 2){
    		baseNum = 24;
    		this.selectStartToEnd(0,2);
    	}
    	var afterText = (Number) (beforeText);
    	if(afterText <= 0){
    		afterText = baseNum;
    	}

    	this.setText(this.formatNumber(Math.abs(afterText - this.step) % baseNum),this.cursorPosition.start,this.cursorPosition.end);
    },

    beforeSpin : function(){
    	var value = this.value || this.getValue();
    	if(Ext.isEmpty(value)){
    		this.updateTime();
    	}
    },

    getRange : function(){
    	return document.selection ? document.selection.createRange() : (this.inputText || this.inputEl.dom);
    },

    formatNumber : function(num){
    	if(num < 10){
    		return '0' + num;
    	}else if(num >= 10){
    		return '' + num;
    	}
    	return '00';
    },

    getSelectedText : function(){
    	var text = '', obj = this.getRange();
    	if (document.selection) {
			var pos = this.getCursorPosition();
			text = this.getValue().slice(pos.start, pos.end);
		} else {
			var p1 = obj.selectionStart, p2 = obj.selectionEnd;
			if (p1 || p1 == '0') {
				if (p1 != p2){
					text = obj.value.substring(p1, p2);
				}
			}
		}
		return text;
    },

    setSelectedText : function(text){
    	var range = this.getRange(),pos = this.getCursorPosition();
    	if(document.selection){
    		var o = this.getCursorPosition(), i = this.inputText, s = this.getValue();
            this.setValue(s.slice(0, o.start) + text + s.slice(o.end));

            var start = o.start += text.length, end = o.start;
            end -= start + this.getValue().slice(start + 1, end).split("\n").length - 1;
            start -= this.getValue().slice(0, start).split("\n").length - 1;
    	}else{
    		range.setRangeText(text);
    	}
    	this.selectStartToEnd(pos.start,pos.end);
    },

    setText : function(text,start,end){
    	var v = this.getValue();
    	this.setValue(v.slice(0, start) + text + v.slice(end));
    	this.selectStartToEnd(start,end);
    },

    selectStartToEnd : function(start,end){
    	if(document.selection){
		    var range = this.inputText.createTextRange();
		    range.duplicate();
	        range.move("character", start);
	        range.moveEnd("character", end - start);
	        range.select(); 
    	}else{
    		this.getRange().setSelectionRange(start,end);
    	}
    	this.lastSelectedText = this.getValue().slice(start,end);
    	this.cursorPosition = {start : start,end : end};
    },

    // private 取得光标在文本框中的位置
    getCursorPosition : function(){
    	var range = this.getRange();
    	if(document.selection){
    		var s = (this.inputText.focus(), document.selection.createRange()), r;
            if(s.parentElement() != this.inputText) return {start: 0, end: 0};
            r = this.inputText.createTextRange();
            return r.setEndPoint("EndToStart", s), {start: r.text.length, end: r.text.length + s.text.length};
    	}else{
    		return {start : range.selectionStart , end : range.selectionEnd};
    	}
    },

    beforeDestroy : function() {
        var me = this;

        if (me.rendered) {
        	if(me.timeRunner){
        		me.timeRunner.stop();
        	}
        	
            Ext.destroy(
                me.timeRunner,
                me.timeTask
            );
        }
        me.callParent();
    }
    
});