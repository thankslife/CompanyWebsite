package com.cc.company.action;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.cc.company.domain.User;
import com.cc.company.service.UserService;
import com.cc.company.utils.VerifyCode;
import com.opensymphony.xwork2.ActionSupport;

public class UserAction extends ActionSupport {

	private UserService userService;

	
	private String username;
	private String pwd;

	private String vcode;
	
	private String oldPwd;
	private String newPwd;
	private String confirmPwd;
	
	public void setOldPwd(String oldPwd) {
		this.oldPwd = oldPwd;
	}



	public void setNewPwd(String newPwd) {
		this.newPwd = newPwd;
	}



	public void setConfirmPwd(String confirmPwd) {
		this.confirmPwd = confirmPwd;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public void setPwd(String pwd) {
		this.pwd = pwd;
	}






	public void setVcode(String vcode) {
		this.vcode = vcode;
	}



	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	
	
	public String regist(){
		int regist = 0;
		//查看验证码是否正确
		String  code = (String) ServletActionContext.getContext().getSession().get("session_vcode");
		if(code.toUpperCase().equals(vcode.trim().toUpperCase())){
			//创建会员
			User user = new User();
			user.setUsername(username);
			user.setPwd(pwd);
			//查看用户名是否已经存在
			User userByUserName = userService.getUserByUserName(user);
			if(userByUserName!=null){
				regist = -2;//用户名已经存在
			}else{
				//注册用户
				regist = userService.regist(user);
			}
		}else{
			regist = -1;//验证码不正确
			
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		try {
			response.getWriter().print(regist);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
	
	
	public String login(){
		User user = new User();
		user.setUsername(username);
		user.setPwd(pwd);
		int login = 1;
		User userByUserName = userService.getUserByUserName(user);
		if(userByUserName==null){
			login = -1;//用户名不存在
		}else if (!userByUserName.getPwd().equals(user.getPwd())) {
			// 密码不正确
			login = -2;
		}else {
			// 存储入session
			ServletActionContext.getContext().getSession().put("user", userByUserName);
		}
		HttpServletResponse response = ServletActionContext.getResponse();
		try {
			response.getWriter().print(login);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
	
	
	public String getVerifyCode(){
		/*
		 * 1. 创建验证码类
		 */
		VerifyCode vc = new VerifyCode();
		/*
		 * 2. 得到验证码图片
		 */
		BufferedImage image = vc.getImage();
		/*
		 * 3. 把图片上的文本保存到session中
		 */
		ServletActionContext.getContext().getSession().put("session_vcode", vc.getText());
		/*
		 * 4. 把图片响应给客户端
		 */
		try {
			VerifyCode.output(image, ServletActionContext.getResponse().getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	/**
	 * 退出登录
	 */
	public String logout() {
		ServletActionContext.getContext().getSession().remove("user");
		return "logout";
	}
	
	
	/**
	 * 管理员密码修改
	 * 
	 * @return
	 */
	public String userPwd() {
		User user = (User) ServletActionContext.getContext().getSession().get("user");
		int state = -1;// 原密码错误
		// 取出原密码进行比对
		if (user.getPwd().equals(oldPwd)) {
			if (newPwd.equals(confirmPwd)) {
				state = 1;// 修改成功
				user.setPwd(newPwd);
				user = userService.updateUserPwd(user);
				// 重新存入session
				ServletActionContext.getContext().getSession().put("user", user);
			} else {
				state = 0;// 确认密码不一致
			}
		}
		try {
			ServletActionContext.getResponse().getWriter().print(state);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
}
