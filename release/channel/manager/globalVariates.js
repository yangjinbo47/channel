// grid分页：页面大小
var pageSize = 30;
// 活动图片尺寸提示语
var activityImgTip = "建议：活动图片的最佳尺寸为480*130，分辨率72";
// 资讯图片尺寸提示语
var newsImgTip = "建议：标题图片的最佳尺寸为133*100，分辨率72";
// 客户端启用页图片尺寸提示语
var clientLogoTip = "建议：图片的最佳尺寸为640*1013，分辨率72";
//课件类型
var courseType = {'1':'课件','2':'电子书','3':'手机报','4':'视频'};
var chapterStatus = {'0':'审核中','1':'待提交', '11':'内容制作中', '12':'待上线', '13':'上线状态', '17':'抢先上线',
					 '14':'屏蔽状态', '15':'待内容修改提交状态', '16':'已下线', '89':'驳回状态', '77':'否决状态',
					 '99':'待删除状态(软)', '98':'待删除状态(硬)',  '24':'同步失败'};
var sexType = {'0':'保密','1':'男','2':'女'};

					 
//视频窗口div z-index有效
Ext.useShims=true;
//var cms = {};
//cms.contentStatus = {'auditing':'1','commit-auditing':'-1','wait-audit'};