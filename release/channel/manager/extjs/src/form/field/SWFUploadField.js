
//flash文件上传组件
Ext.define('Ext.form.field.SWFUploadField',{
	extend: 'Ext.form.field.Trigger',
	alias: 'widget.swfuploadfield',
	
	uploadUrl : 'uploadFiles.action',//文件上传到服务器上的URL
	postParams : {},//随文件上传提交的参数
	flashUrl : 'extjs/src/form/field/swfupload.swf',//flash文件地址
	flash9Url : 'extjs/src/form/field/swfupload_fp9.swf',
	filePostName : 'uploadFile.upload',
	fileSize : '20 MB',//允许上传文件的最大的大小
	fileTypes : '*.*',//允许上传文件的类型
	fileTypesDescription : '所有文件',//允许上传文件类型说明
	enableView : true,//是否允许预览（当上传非图片文件时，请设为false）
	enableClearBtn : true,//是否允许带有清除按钮,当选择了文件后
	success : Ext.emptyFn,//上传成功回调函数
	failure : Ext.emptyFn,//上传失败回调函数
	exception : Ext.emptyFn,//上传文件出现异常
	clear : Ext.emptyFn,//点击清除按钮
	progressElemId : '',//进度显示ID，比如：<div id="test" style="display:none;"></div>
	fileSrc : '',//文件路径
	
	//private
	trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
    trigger2Cls: Ext.baseCSSPrefix + 'form-selectfile-trigger',
	editable : false,
	flashHandlerId : Ext.id(),
	onTrigger2Click : Ext.emptyFn,
	isFormField : true,
	checkValid : true,
	initComponent : function(){
		this.callParent(arguments);
        
	},
	
	//重写afterRender方法
	afterRender : function(){
		this.callParent(arguments);
		this.triggerCell.item(0).setDisplayed(false);
		this.triggerCell.item(1).child('div.' + this.trigger2Cls).createChild({
            tag: 'div',
            id: this.flashHandlerId
        });
        this.initFlashConfig();//初始化flash设置
	},
	
//	onRender : function(){
//		this.callParent(arguments);
//		this.inputHidden = this.inputCell.createChild({
//            tag: 'input', 
//            type: 'hidden',
//            name : this.name,
//            value: ''
//        });
//        this.el.dom.removeAttribute('name');
//	},
	
	onDisable : function(){
		this.callParent(arguments);
		
		Ext.get(this.swfupload.movieName).setStyle({
            top: '-100000px',
            left:'-1000000px'
        });
    },
    
    onEnable : function(){

    },
    
    validateBlur : Ext.emptyFn,
    
	initFlashConfig : function(){
		this.swfupload = new SWFUpload({
			upload_url : this.uploadUrl,
			post_params : Ext.isEmpty(this.postParams) ? {}:this.postParams,
			flash_url : this.flashUrl, 
			flash9_url : this.flash9Url,
			file_post_name : this.filePostName,
			file_size_limit : this.fileSize,
			file_types : this.fileTypes,
			file_types_description : this.fileTypesDescription,
			
			use_query_string:true,
			debug : false,
			button_width : '22',
			button_height : '22',
			button_placeholder_id : this.flashHandlerId,
			button_window_mode : SWFUpload.WINDOW_MODE.TRANSPARENT,
			button_cursor : SWFUpload.CURSOR.HAND,
			button_disabled : false,//按钮是否禁用，默认值为false
			button_action : SWFUpload.BUTTON_ACTION.SELECT_FILE, //设置Flash Button点击以后的动作，默认值为SWFUpload.BUTTON_ACTION.SELECT_FILES（多文件上传）
			//requeue_on_error, //是否将上传失败的的文件重新添加到上传队列的顶端，默认值为true。当文件上传失败或者停止上传触发uploadError事件，是否将上传失败的的文件重新添加到上传队列的顶端，当然调用cancelUpload方法触发uploadError事件，不会将文件重新添加到上传队列中，而是会丢弃
			custom_settings : {
				scope_handler : this
			},
			swfupload_loaded_handler : this.onFlashLoad,
			file_dialog_start_handler : this.onFileDialogStart,
			
			file_queued_handler : this.onAddFileToQueue,
			file_queue_error_handler : this.onAddFileToQueueError,
			file_dialog_complete_handler : this.onAddFileToQueueComplete,
			
			upload_start_handler : this.doUpload,
			upload_progress_handler : this.doUploadProgress,
			upload_error_handler : this.doUploadError,
			upload_success_handler : this.doUploadSuccess,
			upload_complete_handler : this.doUploadComplete
		});
		this.swfupload.uploadStopped = false;
	},
	
	//当flash组件加载后，调用此方法
	onFlashLoad : function(){ },
	
	//打开文件对话框时，调用此方法
	onFileDialogStart : function(){ },
	
	onAddFileToQueue : function(file){
		var thiz = this.customSettings.scope_handler;
		thiz.setValue(file.name);
		thiz.fireEvent('quene',thiz);
		thiz.clearInvalid();
	},
	
	//添加一个文件到队列中出现错误
	onAddFileToQueueError : function(file, errorCode, message){
		var thiz = this.customSettings.scope_handler;
		var errorMsg = '';
		switch(errorCode){
			case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE :
				errorMsg = String.concat('<b>',file.name,'</b>是0字节文件,不能上传0字节的文件');break;
			case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE :
				errorMsg = String.concat('<b>',file.name,'</b>是无效文件类型');break;
			case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT : 
				errorMsg = String.concat('<b>',file.name,'</b>的大小超过<b>',thiz.fileSize,'</b>');break;
			default : 
				errorMsg = '选取文件时出现未知错误';
		}
		thiz.markInvalid(errorMsg);
		thiz.fireEvent('queneerror',thiz);
	},
	
	//添加文件到数据表动作完成
	onAddFileToQueueComplete : function(selectedFilesCount, queuedFilesCount){
//		alert("selectedFilesCount:" + selectedFilesCount + "  queuedFilesCount:" + queuedFilesCount );
		var thiz = this.customSettings.scope_handler;
		if(selectedFilesCount == 0){
			thiz.fireEvent('cancelupload',thiz);return;
		}
		
		if(thiz.fireEvent('beforeupload',thiz) === false){
			thiz.fireEvent('cancelupload',thiz);
			return;
		}

		thiz.triggerCell.item(0).setDisplayed(this.enableClearBtn);
		thiz.doUpload();
	},
	
	//开始上传文件
	doUpload : function(){
    	if (this.swfupload) {
    		this.swfupload.uploadStopped = false;
			var post_params = this.swfupload.settings.post_params;
			this.swfupload.setPostParams(post_params);
			this.swfupload.startUpload();
    	}
    },
    
    //文件上传进度
    doUploadProgress : function(file, completeBytes, bytesTotal){
    	var thiz = this.customSettings.scope_handler;
    	var percent = Math.ceil((completeBytes / bytesTotal) * 100);
    	
    	var progressEl = Ext.getDom(thiz.progressElemId);
    	if(!Ext.isEmpty(progressEl)){
    		progressEl.style.display = 'block';
    		Ext.getDom(thiz.progressElemId).innerHTML = percent + " %";
    	}
    	
    	thiz.fireEvent('progress',thiz,file, completeBytes, bytesTotal);
    },
    
    //当一个文件上传失败后调用此方法,cancelUpload方法调用后也会触发此事件
	doUploadError : function(file, errorCode, message){
		var thiz = this.customSettings.scope_handler;
		var tipMsg = null;
		try{
			switch(errorCode){
				case SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED :
					tipMsg = '上传失败,上传被终止';break;
				case SWFUpload.UPLOAD_ERROR.MISSING_UPLOAD_URL :
					tipMsg = '上传失败,丢失URL';break;
				case SWFUpload.UPLOAD_ERROR.IO_ERROR :
					tipMsg = '上传失败,IO异常,请检查网络或上传URL是否正确';break;
				case SWFUpload.UPLOAD_ERROR.SECURITY_ERROR :
					tipMsg = '上传失败,安全错误';break;
				case SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED :
					tipMsg = '上传失败,大小限制异常';break;
				case SWFUpload.UPLOAD_ERROR.UPLOAD_FAILED :
					tipMsg = '上传失败,上传异常';break;
				case SWFUpload.UPLOAD_ERROR.SPECIFIED_FILE_ID_NOT_FOUND :
					tipMsg = '上传失败,文件编号丢失';break;
				case SWFUpload.UPLOAD_ERROR.FILE_VALIDATION_FAILED :
					tipMsg = '上传失败,验证失败';break;
				case SWFUpload.UPLOAD_ERROR.FILE_CANCELLED:
					tipMsg = '上传失败,文件取消';break;
			}
		}catch(e){
			throw "SWFUploadField.doUploadError :" + e;
		}
		
		if(!Ext.isEmpty(tipMsg)){
			thiz.markInvalid(tipMsg);
		}
//		alert('onUploadError,errorCode:' + errorCode + ",message:" + message  + ",file.filestatus:" + file.filestatus);
	},
    
    /**当一个文件上传成功后调用些方法
	 * @param {} file 文件对象
	 * @param {} responseText 服务端响应的信息
	 * @param {} isResponsed 是否有服务端响应信息
	 */
	doUploadSuccess : function(file, responseText,isResponsed){
		var thiz = this.customSettings.scope_handler;
		try{
//			thiz.markInvalid(Ext.encode(file) + '\n' + responseText);
			var json = Ext.decode(responseText);
			if(json.success){
				//处理上传完成后的动作
				try{
					thiz.success.call(thiz,thiz,file,json);
				}catch(e){
					throw "SWFUploadField.doUploadSuccess call success method error:" + e;
				}
				thiz.checkValid = true;
			}else{
				thiz.checkValid = false;
				thiz.markInvalid(json.msg || '上传文件失败');
				try{
					thiz.failure.call(thiz,thiz,file,json);
				}catch(e){
					throw "SWFUploadField.doUploadSuccess call failure method error:" + e;
				}
			}
		}catch(e){
			thiz.markInvalid('上传文件出现异常');
			try{
				thiz.exception.call(thiz,thiz);
			}catch(e){
				throw "SWFUploadField.doUploadSuccess call exception method error:" + e;
			}
		}
	},
    
    //文件上传动作完成
	doUploadComplete : function(file){
		var thiz = this.customSettings.scope_handler;
		try{
			thiz.triggerCell.item(0).setDisplayed(thiz.enableClearBtn);
			
//			thiz.onFlashObjectPosition();			
	        var progressEl = Ext.getDom(thiz.progressElemId);
	    	if(!Ext.isEmpty(progressEl)){
	    		progressEl.style.display = 'none';
	    	}
		}catch(e){
			throw "SWFUploadField.doUploadComplete error:" + e;
		}finally{
			thiz.fireEvent('uploadcomplete',thiz,file);
		}
	},
	
	isValid : function(){
		return this.checkValid && this.callParent(arguments);
	},
	
	//清除选择的文件
	onTrigger1Click : function(){
		if(this.disabled){
			return;
		}
		
		this.setValue(null);
		this.value = '';
		this.triggerCell.item(0).setDisplayed(false);
		
		try{
			this.clear.call(this,this);
		}catch(e){
			throw "SWFUploadField.onTrigger1Click call clear method error:" + e;
		}
		this.checkValid = true;
	},
	
	getValue : function(){
		this.callParent(arguments);
		return this.value;
	},
	
	setValue : function(v){
		this.callParent(arguments);
		if(!Ext.isEmpty(v)){
			this.triggerCell.item(0).setDisplayed(true);
		}
	}
});