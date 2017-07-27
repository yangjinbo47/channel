package com.tenfen.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMailUtil {

	/**
	*@author BOBO
	*@功能：发送文本邮件
	*@param sendAddress
	*@param password
	*@param smtpServer
	*@param receiveAddress
	*@param message
	*@return
	 */
	public static boolean sendTextMail(String sendAddress, String password, String smtpServer, String[] receiveAddress, String subject, String message) {

		Authenticator auth = new PopupAuthenticator(sendAddress, password);

		Properties mailProps = new Properties();
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("username", sendAddress);
		mailProps.put("password", password);
		mailProps.put("mail.smtp.host", smtpServer);

		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
//		Session sendMailSession = Session.getDefaultInstance(mailProps, auth);
		Session sendMailSession = Session.getInstance(mailProps, auth);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(sendAddress);
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
//			Address to = new InternetAddress(receiveAddress);
//			mailMessage.setRecipient(Message.RecipientType.TO, to);
			String toList = getMailList(receiveAddress);  
            InternetAddress[] iaToList = new InternetAddress().parse(toList);
            mailMessage.setRecipients(Message.RecipientType.TO,iaToList); //收件人 
			// 设置邮件消息的主题
			mailMessage.setSubject(subject);
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// 设置邮件消息的主要内容
			mailMessage.setText(message);
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	*@author BOBO
	*@功能：发送HTML邮件
	*@param sendAddress
	*@param password
	*@param smtpServer
	*@param receiveAddress
	*@param message
	*@return
	 */
	public static boolean sendHtmlMail(String sendAddress, String password, String smtpServer, String[] receiveAddress, String subject, String message) {

		Authenticator auth = new PopupAuthenticator(sendAddress, password);

		Properties mailProps = new Properties();
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("username", sendAddress);
		mailProps.put("password", password);
		mailProps.put("mail.smtp.host", smtpServer);

		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
//		Session sendMailSession = Session.getDefaultInstance(mailProps, auth);
		Session sendMailSession = Session.getInstance(mailProps, auth);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(sendAddress);
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
//			Address to = new InternetAddress(receiveAddress);
//			mailMessage.setRecipient(Message.RecipientType.TO, to);
			String toList = getMailList(receiveAddress);  
            InternetAddress[] iaToList = new InternetAddress().parse(toList);
            mailMessage.setRecipients(Message.RecipientType.TO,iaToList); //收件人 
			// 设置邮件消息的主题
			mailMessage.setSubject(subject);
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
			Multipart mainPart = new MimeMultipart();
			// 创建一个包含HTML内容的MimeBodyPart
			BodyPart html = new MimeBodyPart();
			// 设置HTML内容
			html.setContent(message, "text/html; charset=utf-8");
			mainPart.addBodyPart(html);
			// 将MiniMultipart对象设置为邮件内容
			mailMessage.setContent(mainPart);
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	private static String getMailList(String[] mailArray) {
		StringBuffer toList = new StringBuffer();
		int length = mailArray.length;
		if (mailArray != null && length < 2) {
			toList.append(mailArray[0]);
		} else {
			for (int i = 0; i < length; i++) {
				toList.append(mailArray[i]);
				if (i != (length - 1)) {
					toList.append(",");
				}
			}
		}
		return toList.toString();
	}
	
}

class PopupAuthenticator extends Authenticator {
	private String username;
	private String password;

	public PopupAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username, this.password);
	}
}
