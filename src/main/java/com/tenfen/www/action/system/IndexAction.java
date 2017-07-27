package com.tenfen.www.action.system;

import org.springframework.beans.factory.annotation.Autowired;

import sun.misc.BASE64Decoder;

import com.tenfen.entity.account.User;
import com.tenfen.util.StringUtil;
import com.tenfen.util.Utils;
import com.tenfen.util.encrypt.MD5;
import com.tenfen.www.action.SimpleActionSupport;
import com.tenfen.www.common.Constants;
import com.tenfen.www.service.account.AccountManager;

public class IndexAction extends SimpleActionSupport {

	private static final long serialVersionUID = 3205177939227033736L;

	@Autowired
	private AccountManager accountManager;
	
	public void login() {
		String checkCode = request.getParameter("checkCode");
//        String vc = (String)getSessionAttribute("ADMIN_VERIRY_CODE");
		String vc = getParamFromCookie("ADMIN_VERIRY_CODE");
        
        try {
        	if (checkCode.equals(vc))
            {
            	String userName = request.getParameter("userName");
                String password = request.getParameter("passWord");
                
                //将提交的密码进行base64解码，并去除末尾添加的3位随机数
                byte[] bytes = new BASE64Decoder().decodeBuffer(password);
                String base64edPwd = new String(bytes);
                base64edPwd = base64edPwd.substring(0, base64edPwd.length() - 3);
                String userPwd = MD5.getMD5(base64edPwd);
                
                User user = accountManager.findUserByLoginName(userName);
                if (Utils.isEmpty(user)) {
                	String rspStr = "{failure:true,msg:'用户名不存在!'}";
                	StringUtil.printJson(response, rspStr);
                	return;
    			}
                
                if (!userPwd.equals(user.getPassword())) {
                	String rspStr = "{failure:true,msg:'密码输入有误!'}";
                	StringUtil.printJson(response, rspStr);
                	return;
    			}
                
                //保存用户信息
                setSessionAttribute(Constants.OPERATOR_ID, user.getOperid());
                setSessionAttribute(Constants.OPERATOR_NAME, user.getLoginName());
                setSessionAttribute(Constants.OPERATOR_TYPE, user.getType());
//                setMemcacheAttribute(Constants.OPERATOR_ID, user.getOperid());
//                setMemcacheAttribute(Constants.OPERATOR_NAME, user.getLoginName());
//                setMemcacheAttribute(Constants.OPERATOR_TYPE, user.getType());
                //memcache保存用户权限信息
                accountManager.cacheAuthority(user);
                
                String rspStr = "{success:true,msg:'ok',operatorname:'"+user.getLoginName()+"',operatorid:"+user.getOperid()+"}";
                StringUtil.printJson(response, rspStr);
                return;
            } else {
            	String rspStr = "{failure:true,msg:'验证码输入错误,请重新输入!'}";
            	StringUtil.printJson(response, rspStr);
            	return;
            }
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
        
	}

	/**
	 * 获取当前登录状态
	 */
	public void getStatus() {
		String rspStr = "";
		String name = (String) getSessionAttribute(Constants.OPERATOR_NAME);
		Integer userType = (Integer)getSessionAttribute(Constants.OPERATOR_TYPE);
		
//		String name = (String) getMemcacheAttribute(Constants.OPERATOR_NAME);
		if (!StringUtil.isEmpty(name)) {
			rspStr = "{success:true,msg:'ok',name:'" + name + "',operatorType:"+userType+"}";
		} else {
			rspStr = "{success:false,msg:'您还没有登陆或用户会话超时,请重新登陆'}";
		}
		StringUtil.printJson(response, rspStr);
	}

	public void logout() {
//		HttpSession session = request.getSession();
//        session.invalidate();
		removeSessionAttribute(Constants.OPERATOR_ID);
		removeSessionAttribute(Constants.OPERATOR_NAME);
		removeSessionAttribute(Constants.OPERATOR_TYPE);
        String rspStr = "{success:true,msg:'ok'}";
        StringUtil.printJson(response, rspStr);
	}

}
