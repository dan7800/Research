/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/



package com.vlee.util.mail;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.Authenticator;
import java.util.Date;
import java.util.*;
import java.text.*;
import java.net.PasswordAuthentication;
import java.sql.*;
import java.io.*;

import com.vlee.util.*;

public class SendMail
{

	public static void sendEmail(String emailFrom, String emailTo, String subject, String body)
	{
		java.util.Properties props = null;
		Session session = null;
		MimeMessage msg = null;
		javax.mail.Address aEmailTo[] = null;
		try
		{
			System.out.println("Inside SendMail.sendEmail");
			
			props = System.getProperties();
			props.put("mail.smtp.host", Constants.SMTP_HOST);
			props.put("mail.smtp.from","vincent@wavelet.biz");
			props.put("mail.smtp.localhost","wavelet.biz");


			System.out.println("Set props");
			
			session = Session.getDefaultInstance(props, null);
			msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(emailFrom));
			aEmailTo = InternetAddress.parse(emailTo);
			msg.setRecipients(javax.mail.Message.RecipientType.TO, aEmailTo);
			msg.setSubject(subject);
			msg.setText(body, "ISO-8859-1");
			msg.setSentDate(new Date());
			msg.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
			
			System.out.println("Set msg");
			
			Transport.send(msg);
			
			System.out.println("Sent email. Leaving SendMail.sendEmail");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendEmail(String emailFrom, String emailTo, String subject, String body, 
			String username, String password, String smtpAddress)
	{
		java.util.Properties props = null;
		Session session = null;
		MimeMessage msg = null;
		javax.mail.Address aEmailTo[] = null;
		try
		{
			System.out.println("Inside SendMail.sendEmail");
			
			props = System.getProperties();
			props.put("mail.smtp.host", smtpAddress);
	        props.put("mail.smtp.user", username);
	        props.put("mail.smtp.auth", "true");
	         
			//props.put("mail.smtp.host", Constants.SMTP_HOST);
			//props.put("mail.smtp.from","vincent@wavelet.biz");
			//props.put("mail.smtp.localhost","wavelet.biz");
			
			session = Session.getDefaultInstance(props, null);
			msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(emailFrom));
			aEmailTo = InternetAddress.parse(emailTo);
			msg.setRecipients(javax.mail.Message.RecipientType.TO, aEmailTo);
			msg.setSubject(subject);
			msg.setText(body, "ISO-8859-1");
			msg.setSentDate(new Date());
			msg.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
			
			//Transport.send(msg);
			
			Transport tr = session.getTransport("smtp");
	        tr.connect(smtpAddress, username, password);
	        msg.saveChanges();  
	        tr.sendMessage(msg, msg.getAllRecipients());
	        tr.close();
	         
			System.out.println("Sent email. Leaving SendMail.sendEmail");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void sendEmail(String emailFrom, String emailTo[ ], String subject, String body, 
			String username, String password, String smtpAddress)
	{
		java.util.Properties props = null;
		Session session = null;
		MimeMessage msg = null;
		try
		{
	
			props = System.getProperties();
			props.put("mail.smtp.host", smtpAddress);
	        props.put("mail.smtp.user", username);
	        props.put("mail.smtp.auth", "true");
			
			session = Session.getDefaultInstance(props, null);
			msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(emailFrom));
			
			InternetAddress[] aEmailTo = new InternetAddress[emailTo.length]; 
			for (int i = 0; i < emailTo.length; i++)
			{
				if(emailTo[i] != null && !"".equals(emailTo[i]))
					aEmailTo[i] = new InternetAddress(emailTo[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, aEmailTo);
	
			msg.setSubject(subject);
			msg.setText(body, "ISO-8859-1");
			msg.setSentDate(new Date());
			msg.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
			
			//Transport.send(msg);
			
			Transport tr = session.getTransport("smtp");
	        tr.connect(smtpAddress, username, password);
	        msg.saveChanges();  
	        tr.sendMessage(msg, msg.getAllRecipients());
	        tr.close();
	         
			System.out.println("Sent email. Leaving SendMail.sendEmail");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
