/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoCollectionProblemLog extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-collection-problem-log-page");
		}

		if(formName.equals("setAction"))
		{
			fnSetAction(servlet,req,res);
		}

		if(formName.equals("sendEmail"))
		{
			fnSendEmail(servlet,req,res);
		}

		return new ActionRouter("dist-collection-problem-log-page");
	}

	private void fnSetAction(HttpServlet servlet, HttpServletRequest req,
										HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String orderNo = req.getParameter("orderNo");
		String followUpMode = req.getParameter("followUpMode");
		String followUpRemarks = req.getParameter("followUpRemarks");

		try
		{
			Long iOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(iOrder);
			if(soObj==null){ return ;}

               DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
               dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
               dpiObj.processType = "UPDATE-ORDER";
               dpiObj.category = "COLLECTION-FOLLOWUP-"+followUpMode;
               dpiObj.auditLevel = new Integer(0);
               dpiObj.processId = new Long(0);
               dpiObj.userid = (Integer) session.getAttribute("userId");
               dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
               dpiObj.docId = soObj.pkid;
               dpiObj.entityRef = CustAccountBean.TABLENAME;
               dpiObj.entityId = soObj.senderKey1;
               dpiObj.description1 = "FOLLOW UP WITH:"+followUpMode+" REMARKS:"+followUpRemarks;
               dpiObj.description2 = "";
               dpiObj.remarks = "";
               dpiObj.time = TimeFormat.getTimestamp();
               DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

		}
		catch(Exception ex)
		{ ex.printStackTrace();}
	}

	private void fnSendEmail(HttpServlet servlet, HttpServletRequest req,
										HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String orderNo = req.getParameter("orderNo");
		String emailAddressFrom = req.getParameter("emailAddressFrom");
		String emailAddressTo = req.getParameter("emailAddressTo");
		String emailSubject = req.getParameter("emailSubject");
		String emailBody = req.getParameter("emailBody");

		try
		{
			Long iOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(iOrder);
			if(soObj==null){ return ;}

			String userName = (String) session.getAttribute("userName");

			emailBody += "\n\n username:"+userName;
			emailSubject += "ODR"+iOrder.toString()+": ";
			String documentTrail = " FOLLOWED UP WITH EMAIL <br>";
			documentTrail += "<br>From:"+emailAddressFrom;
			documentTrail += "<br>To:"+emailAddressTo;
			documentTrail += "<br>Subject:"+emailSubject;
			documentTrail += "<br>Message:" + emailBody;
               DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
               dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
               dpiObj.processType = "UPDATE-ORDER";
               dpiObj.category = "COLLECTION-FOLLOWUP-EMAIL";
               dpiObj.auditLevel = new Integer(0);
               dpiObj.processId = new Long(0);
               dpiObj.userid = (Integer) session.getAttribute("userId");
               dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
               dpiObj.docId = soObj.pkid;
               dpiObj.entityRef = CustAccountBean.TABLENAME;
               dpiObj.entityId = soObj.senderKey1;
               dpiObj.description1 = documentTrail;
               dpiObj.description2 = "";
               dpiObj.remarks = "";
               dpiObj.time = TimeFormat.getTimestamp();
               DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

			boolean bSentMail = false;
			boolean bSendError = false;
			String strErrorMsg = new String(" ");
			Properties props = new Properties();
			props.put("mail.smtp.host", "localhost");
			props.put("mail.smtp.ehlo", "false");
			props.put("mail.smtp.sendpartial", "true");
			Session sss = Session.getInstance(props, null);
			String mailFrom = emailAddressFrom;
			String mailTo = emailAddressTo;
      	String mailBcc = new String("emp@wavelet.biz");
      	String mailSubject = emailSubject;
			String mailText = emailBody;
			MimeMessage message = new MimeMessage(sss);
			try
      	{
				InternetAddress iaMailFrom = new InternetAddress(emailAddressFrom);
				message.setFrom(iaMailFrom);
            InternetAddress iaMailBcc[] = InternetAddress.parse(emailAddressTo);
				message.addRecipients(Message.RecipientType.BCC, iaMailBcc);
         	message.setSubject(emailSubject);
         	message.setText(emailBody); 
         	Transport.send(message);
			} 
			catch (MessagingException msgEx)
			{
				strErrorMsg += "\n" + strErrorMsg + msgEx.getMessage();
				Log.printDebug("DoSendMail: " + strErrorMsg);
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace();}

	}

}



/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoCollectionProblemLog extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-collection-problem-log-page");
		}

		if(formName.equals("setAction"))
		{
			fnSetAction(servlet,req,res);
		}

		if(formName.equals("sendEmail"))
		{
			fnSendEmail(servlet,req,res);
		}

		return new ActionRouter("dist-collection-problem-log-page");
	}

	private void fnSetAction(HttpServlet servlet, HttpServletRequest req,
										HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String orderNo = req.getParameter("orderNo");
		String followUpMode = req.getParameter("followUpMode");
		String followUpRemarks = req.getParameter("followUpRemarks");

		try
		{
			Long iOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(iOrder);
			if(soObj==null){ return ;}

               DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
               dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
               dpiObj.processType = "UPDATE-ORDER";
               dpiObj.category = "COLLECTION-FOLLOWUP-"+followUpMode;
               dpiObj.auditLevel = new Integer(0);
               dpiObj.processId = new Long(0);
               dpiObj.userid = (Integer) session.getAttribute("userId");
               dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
               dpiObj.docId = soObj.pkid;
               dpiObj.entityRef = CustAccountBean.TABLENAME;
               dpiObj.entityId = soObj.senderKey1;
               dpiObj.description1 = "FOLLOW UP WITH:"+followUpMode+" REMARKS:"+followUpRemarks;
               dpiObj.description2 = "";
               dpiObj.remarks = "";
               dpiObj.time = TimeFormat.getTimestamp();
               DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

		}
		catch(Exception ex)
		{ ex.printStackTrace();}
	}

	private void fnSendEmail(HttpServlet servlet, HttpServletRequest req,
										HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String orderNo = req.getParameter("orderNo");
		String emailAddressFrom = req.getParameter("emailAddressFrom");
		String emailAddressTo = req.getParameter("emailAddressTo");
		String emailSubject = req.getParameter("emailSubject");
		String emailBody = req.getParameter("emailBody");

		try
		{
			Long iOrder = new Long(orderNo);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(iOrder);
			if(soObj==null){ return ;}

			String userName = (String) session.getAttribute("userName");

			emailBody += "\n\n username:"+userName;
			emailSubject += "ODR"+iOrder.toString()+": ";
			String documentTrail = " FOLLOWED UP WITH EMAIL <br>";
			documentTrail += "<br>From:"+emailAddressFrom;
			documentTrail += "<br>To:"+emailAddressTo;
			documentTrail += "<br>Subject:"+emailSubject;
			documentTrail += "<br>Message:" + emailBody;
               DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
               dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
               dpiObj.processType = "UPDATE-ORDER";
               dpiObj.category = "COLLECTION-FOLLOWUP-EMAIL";
               dpiObj.auditLevel = new Integer(0);
               dpiObj.processId = new Long(0);
               dpiObj.userid = (Integer) session.getAttribute("userId");
               dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
               dpiObj.docId = soObj.pkid;
               dpiObj.entityRef = CustAccountBean.TABLENAME;
               dpiObj.entityId = soObj.senderKey1;
               dpiObj.description1 = documentTrail;
               dpiObj.description2 = "";
               dpiObj.remarks = "";
               dpiObj.time = TimeFormat.getTimestamp();
               DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

			boolean bSentMail = false;
			boolean bSendError = false;
			String strErrorMsg = new String(" ");
			Properties props = new Properties();
			props.put("mail.smtp.host", "localhost");
			props.put("mail.smtp.ehlo", "false");
			props.put("mail.smtp.sendpartial", "true");
			Session sss = Session.getInstance(props, null);
			String mailFrom = emailAddressFrom;
			String mailTo = emailAddressTo;
      	String mailBcc = new String("emp@wavelet.biz");
      	String mailSubject = emailSubject;
			String mailText = emailBody;
			MimeMessage message = new MimeMessage(sss);
			try
      	{
				InternetAddress iaMailFrom = new InternetAddress(emailAddressFrom);
				message.setFrom(iaMailFrom);
            InternetAddress iaMailBcc[] = InternetAddress.parse(emailAddressTo);
				message.addRecipients(Message.RecipientType.BCC, iaMailBcc);
         	message.setSubject(emailSubject);
         	message.setText(emailBody); 
         	Transport.send(message);
			} 
			catch (MessagingException msgEx)
			{
				strErrorMsg += "\n" + strErrorMsg + msgEx.getMessage();
				Log.printDebug("DoSendMail: " + strErrorMsg);
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace();}

	}

}



