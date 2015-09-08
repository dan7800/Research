/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.26 2003/08/06 06:50:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx),
			fromCurrentOrRequest ? getEMail(ctx, false) : getCurrentUserEMail(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user or request
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		if (AD_User_ID != 0)
			from = getCurrentUserEMail (ctx, true);
		if (from == null || from.length() == 0)
			from = getRequestEMail (ctx);
		//	bogus
		if (from == null || from.length() == 0)
		{
			if (strict)
				return null;
			from = getBogusEMail(ctx);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 * 	@param strict no bogous email address
	 * 	@param ctx optional context
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID, boolean strict, Properties ctx)
	{
		String email = null;
		//	Get ID
		String sql = "SELECT EMail, EMailUser, EMailUserPw, Name "
			+ "FROM AD_User "
			+ "WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString(1);
				if (email != null)
				{
					email = cleanUpEMail(email);
					if (ctx != null)
					{
						Env.setContext (ctx, CTX_EMAIL, email);
						Env.setContext (ctx, CTX_EMAIL_USER, rs.getString (2));
						Env.setContext (ctx, CTX_EMAIL_USERPW, rs.getString (3));
					}
				}
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getEMailOfUser - " + sql, e);
		}
		if (email == null || email.length() == 0)
		{
			s_log.warn("getEMailOfUser - EMail not found - AD_User_ID=" + AD_User_ID);
			if (strict)
				return null;
			email = getBogusEMail(ctx == null ? Env.getCtx() : ctx);
		}
		return email;
	}	//	getEMailOfUser

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		return getEMailOfUser(AD_User_ID, false, null);
	}	//	getEMailOfUser

	/**
	 *  Get Email Address current AD_User
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		from = getEMailOfUser(AD_User_ID, strict, ctx);
		return from;
	}	//	getCurrentUserEMail

	/**
	 * 	Clean up EMail address
	 *	@param email email address
	 *	@return lower case email w/o spaces
	 */
	private static String cleanUpEMail (String email)
	{
		if (email == null || email.length() == 0)
			return "";
		//
		email = email.trim().toLowerCase();
		//	Delete all spaces
		int pos = email.indexOf(" ");
		while (pos != -1)
		{
			email = email.substring(0, pos) + email.substring(pos+1);
			pos = email.indexOf(" ");
		}
		return email;
	}	//	cleanUpEMail

	/**
	 * 	Construct Bogos email
	 *	@param ctx Context
	 *	@return userName.ClientName.com
	 */
	public static String getBogusEMail (Properties ctx)
	{
		String email = System.getProperty("user.name") + "@"
			+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		email = cleanUpEMail(email);
		return email;
	}	//	getBogusEMail

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**
	 * 	Get Client Request EMail
	 *  @param ctx  Context
	 *  @return Request EMail Address
	 */
	public static String getRequestEMail (Properties ctx)
	{
		String email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		if (email.length() != 0)
			return email;

		String sql = "SELECT RequestEMail, RequestUser, RequestUserPw "
			+ "FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(ctx, "#AD_Client_ID"));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString (1);
				email = cleanUpEMail(email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL, email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USER, rs.getString(2));
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USERPW, rs.getString(3));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getRequestEMail", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return email;
	}	//	getRequestEMail


	/**************************************************************************
	 *	Get the current Client EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		String SMTP = Env.getContext(ctx, CTX_SMTP);
		if (SMTP.length() != 0)
			return SMTP;
		//	Get SMTP name
		SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "localhost";
		Env.setContext(ctx, CTX_SMTP, SMTP);
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error ("getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost




	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.smtp.*;
import org.compiere.model.*;

/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.35 2004/04/26 06:15:05 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param userTo user to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, MUser userTo, String subject, String message)
	{
		this (client, userFrom, userTo.getEmail(), subject, message);
	}	//	EMail

	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param to email to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, String to, String subject, String message)
	{
		this (client.getSMTPHost(), 
			userFrom == null ? client.getRequestEMail() : userFrom.getEmail(), 
			to, subject, message);
		if (client.isSmtpAuthorization())
		{
			if (userFrom != null)
				setEMailUser(userFrom.getEmailUser(), userFrom.getEmailUserPW());
			else
				setEMailUser(client.getRequestUser(), client.getRequestUserPW());
		}
		m_valid = isValid (true);
	}	//	EMail

	/**
	 *	Full Constructor
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		if (subject == null || subject.length() == 0)
			setSubject(".");	//	pass validation
		else
			setSubject (subject);
		if (message != null && message.length() > 0)
			setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = null;
		try
		{
			session = Session.getDefaultInstance(props, m_auth);
		}
		catch (Exception e)
		{
			log.error ("send - " + m_auth, e);
			return e.toString();
		}
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			//
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	
	/**************************************************************************
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	
	/**************************************************************************
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 * 	@param recheck if true check main variables
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		if (!recheck)
			return m_valid;
			
		//  From
		if (m_from == null 
			|| m_from.getAddress().length() == 0 
			|| m_from.getAddress().indexOf(' ') != -1)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		//	To
		InternetAddress[] ias = getTos();
		if (ias == null)
		{
			log.warn("isValid - No To");
			return false;
		}
		for (int i = 0; i < ias.length; i++)
		{
			if (ias[i] == null 
				|| ias[i].getAddress().length() == 0
				|| ias[i].getAddress().indexOf(' ') != -1)
			{
				log.warn("isValid - To(" + i + ") is invalid=" + ias[i]);
				return false;
			}
		}

		//	Host
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		
		//	Subject
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**************************************************************************
	 *  Test.
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 * 	--
	 * 	If you get SendFailedException: 550 5.7.1 Unable to relay for ..
	 * 	Check:
	 * 	- Does the SMTP server allow you to relay
	 *    (Exchange: SMTP server - Access)
	 *  - Did you authenticate (setEmailUser)
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.compiere.model.*;

import com.sun.mail.smtp.*;

/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.37 2004/09/09 14:14:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param userTo user to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, MUser userTo, String subject, String message)
	{
		this (client, userFrom, userTo.getEmail(), subject, message);
	}	//	EMail

	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param to email to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, String to, String subject, String message)
	{
		this (client.getSMTPHost(), 
			userFrom == null ? client.getRequestEMail() : userFrom.getEmail(), 
			to, subject, message);
		if (client.isSmtpAuthorization())
		{
			if (userFrom != null)
				setEMailUser(userFrom.getEmailUser(), userFrom.getEmailUserPW());
			else
				setEMailUser(client.getRequestUser(), client.getRequestUserPW());
		}
	}	//	EMail

	/**
	 *	Full Constructor
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		if (subject == null || subject.length() == 0)
			setSubject(".");	//	pass validation
		else
			setSubject (subject);
		if (message != null && message.length() > 0)
			setMessageText (message);
		m_valid = isValid (false);
	}	//	EMail

	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = null;
		try
		{
			session = Session.getDefaultInstance(props, m_auth);
		}
		catch (Exception e)
		{
			log.error ("send - " + m_auth, e);
			return e.toString();
		}
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			//
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	
	/**************************************************************************
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	
	/**************************************************************************
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 * 	@param recheck if true check main variables
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		if (!recheck)
			return m_valid;
			
		//  From
		if (m_from == null 
			|| m_from.getAddress().length() == 0 
			|| m_from.getAddress().indexOf(' ') != -1)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		//	To
		InternetAddress[] ias = getTos();
		if (ias == null)
		{
			log.warn("isValid - No To");
			return false;
		}
		for (int i = 0; i < ias.length; i++)
		{
			if (ias[i] == null 
				|| ias[i].getAddress().length() == 0
				|| ias[i].getAddress().indexOf(' ') != -1)
			{
				log.warn("isValid - To(" + i + ") is invalid=" + ias[i]);
				return false;
			}
		}

		//	Host
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		
		//	Subject
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**************************************************************************
	 *  Test.
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 * 	--
	 * 	If you get SendFailedException: 550 5.7.1 Unable to relay for ..
	 * 	Check:
	 * 	- Does the SMTP server allow you to relay
	 *    (Exchange: SMTP server - Access)
	 *  - Did you authenticate (setEmailUser)
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.16 2003/02/18 06:12:19 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx), getCurrentUserEMail(ctx), to, subject, message);
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
		Log.trace(Log.l3_Util, "EMail (" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		Log.trace(Log.l3_Util, "EMail (" + smtpHost + ") " + from + " -> " + to,
			"Subject=" + subject + ", Message=" + message);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";



	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		Log.trace(Log.l4_Data, "EMail.send (" + m_smtpHost + ")",
			m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.getTraceLevel() > 9);

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
			m_msg.setDescription("myDestription");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
			t.connect();
		//	t.connect(m_smtpHost, user, password);
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			Log.trace(Log.l4_Data, "EMail.send success", "MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("EMail.send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password");
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			Log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			Log.error("EMail.send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.getTraceLevel() > 9)
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
			{
				System.out.println ("- " + e.nextElement ());
			}
		}
		catch (MessagingException ex)
		{
			System.err.println(ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			Log.error("EMail.getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user
	 *  @param ctx  Context
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx)
	{
		String from = getEMailOfUser (Env.getContextAsInt (ctx, "#AD_User_ID"));
		if (from == null)
			from = Env.getContext(ctx, "#AD_User_Name") + "@"
				+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		//	Delete all spaces
		while (from.indexOf(" ") != -1)
		{
			int pos = from.indexOf(" ");
			from = from.substring(0, pos) + from.substring(pos+1);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID System User ID
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		String from = null;
		//	Get ID
		String sql = "SELECT u.EMail,bp.EMail,bpc.EMail "
			+ "FROM AD_User u, C_BPartner bp, C_BPartner_Contact bpc "
			+ "WHERE u.C_BPartner_ID=bp.C_BPartner_ID(+)"
			+ " AND u.C_BPartner_ID=bpc.C_BPartner_ID(+)"
			+ " AND u.AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() && from == null)
			{
				from = rs.getString(1);
				if (from == null)
					from = rs.getString(2);
				if (from == null)
					from = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("EMail.getEMailOfUser", e);
		}
		//  No user found - do some guessing
		if (from == null)
		{
			return System.getProperty("user.name") + "@"
				+ Env.getContext(Env.getCtx(), "#AD_Client_Name") + ".com";
		}

		//	clean up
		from = from.trim().toLowerCase();
		//	Delete all spaces
		while (from.indexOf(" ") != -1)
		{
			int pos = from.indexOf(" ");
			from = from.substring(0, pos) + from.substring(pos+1);
		}
		return from;
	}	//	getEMailOfUser

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("EMail.getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**************************************************************************
	 *	Get the current EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		//  Test environment
		if (Env.getContext(ctx, "#CompiereSys").equals("Y"))
			return "main";
		//
		String SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "main";
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("EMail.getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost


	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		m_auth = new EMailAuthenticator (username, password);
	}	//	setEmailUser

	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress(newFrom);
		}
		catch (Exception e)
		{
			Log.error("EMail.setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newTo);
		}
		catch (Exception e)
		{
			Log.error("EMail.addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newCc);
		}
		catch (Exception e)
		{
			Log.error("EMail.addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newTo);
		}
		catch (Exception e)
		{
			Log.error("EMail.setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						Log.error("EMail.setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					Log.error("EMail.setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
	//	emailTest.setEMailUser("jjanke", "joergok");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.30 2003/10/11 05:20:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (EMailUtil.getSmtpHost(ctx),
			fromCurrentOrRequest ? EMailUtil.getEMail(ctx, false) : EMailUtil.getEMail_User(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.25 2003/07/16 19:08:37 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx),
			fromCurrentOrRequest ? getEMail(ctx, false) : getCurrentUserEMail(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
		log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject + ", Message=" + message);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
			log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
			log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user or request
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		if (AD_User_ID != 0)
			from = getCurrentUserEMail (ctx, true);
		if (from == null || from.length() == 0)
			from = getRequestEMail (ctx);
		//	bogus
		if (from == null || from.length() == 0)
		{
			if (strict)
				return null;
			from = getBogusEMail(ctx);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 * 	@param strict no bogous email address
	 * 	@param ctx optional context
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID, boolean strict, Properties ctx)
	{
		String email = null;
		//	Get ID
		String sql = "SELECT EMail, EMailUser, EMailUserPw, Name "
			+ "FROM AD_User "
			+ "WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString(1);
				if (email != null)
				{
					email = cleanUpEMail(email);
					if (ctx != null)
					{
						Env.setContext (ctx, CTX_EMAIL, email);
						Env.setContext (ctx, CTX_EMAIL_USER, rs.getString (2));
						Env.setContext (ctx, CTX_EMAIL_USERPW, rs.getString (3));
					}
				}
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getEMailOfUser - " + sql, e);
		}
		if (email == null || email.length() == 0)
		{
			s_log.warn("getEMailOfUser - EMail not found - AD_User_ID=" + AD_User_ID);
			if (strict)
				return null;
			email = getBogusEMail(ctx == null ? Env.getCtx() : ctx);
		}
		return email;
	}	//	getEMailOfUser

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		return getEMailOfUser(AD_User_ID, false, null);
	}	//	getEMailOfUser

	/**
	 *  Get Email Address current AD_User
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		from = getEMailOfUser(AD_User_ID, strict, ctx);
		return from;
	}	//	getCurrentUserEMail

	/**
	 * 	Clean up EMail address
	 *	@param email email address
	 *	@return lower case email w/o spaces
	 */
	private static String cleanUpEMail (String email)
	{
		if (email == null || email.length() == 0)
			return "";
		//
		email = email.trim().toLowerCase();
		//	Delete all spaces
		int pos = email.indexOf(" ");
		while (pos != -1)
		{
			email = email.substring(0, pos) + email.substring(pos+1);
			pos = email.indexOf(" ");
		}
		return email;
	}	//	cleanUpEMail

	/**
	 * 	Construct Bogos email
	 *	@param ctx Context
	 *	@return userName.ClientName.com
	 */
	public static String getBogusEMail (Properties ctx)
	{
		String email = System.getProperty("user.name") + "@"
			+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		email = cleanUpEMail(email);
		return email;
	}	//	getBogusEMail

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**
	 * 	Get Client Request EMail
	 *  @param ctx  Context
	 *  @return Request EMail Address
	 */
	public static String getRequestEMail (Properties ctx)
	{
		String email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		if (email.length() != 0)
			return email;

		String sql = "SELECT RequestEMail, RequestUser, RequestUserPw "
			+ "FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(ctx, "#AD_Client_ID"));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString (1);
				email = cleanUpEMail(email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL, email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USER, rs.getString(2));
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USERPW, rs.getString(3));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getRequestEMail", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return email;
	}	//	getRequestEMail


	/**************************************************************************
	 *	Get the current Client EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		String SMTP = Env.getContext(ctx, CTX_SMTP);
		if (SMTP.length() != 0)
			return SMTP;
		//	Get SMTP name
		SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "localhost";
		Env.setContext(ctx, CTX_SMTP, SMTP);
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error ("getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost




	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.26 2003/08/06 06:50:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx),
			fromCurrentOrRequest ? getEMail(ctx, false) : getCurrentUserEMail(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user or request
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		if (AD_User_ID != 0)
			from = getCurrentUserEMail (ctx, true);
		if (from == null || from.length() == 0)
			from = getRequestEMail (ctx);
		//	bogus
		if (from == null || from.length() == 0)
		{
			if (strict)
				return null;
			from = getBogusEMail(ctx);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 * 	@param strict no bogous email address
	 * 	@param ctx optional context
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID, boolean strict, Properties ctx)
	{
		String email = null;
		//	Get ID
		String sql = "SELECT EMail, EMailUser, EMailUserPw, Name "
			+ "FROM AD_User "
			+ "WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString(1);
				if (email != null)
				{
					email = cleanUpEMail(email);
					if (ctx != null)
					{
						Env.setContext (ctx, CTX_EMAIL, email);
						Env.setContext (ctx, CTX_EMAIL_USER, rs.getString (2));
						Env.setContext (ctx, CTX_EMAIL_USERPW, rs.getString (3));
					}
				}
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getEMailOfUser - " + sql, e);
		}
		if (email == null || email.length() == 0)
		{
			s_log.warn("getEMailOfUser - EMail not found - AD_User_ID=" + AD_User_ID);
			if (strict)
				return null;
			email = getBogusEMail(ctx == null ? Env.getCtx() : ctx);
		}
		return email;
	}	//	getEMailOfUser

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		return getEMailOfUser(AD_User_ID, false, null);
	}	//	getEMailOfUser

	/**
	 *  Get Email Address current AD_User
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		from = getEMailOfUser(AD_User_ID, strict, ctx);
		return from;
	}	//	getCurrentUserEMail

	/**
	 * 	Clean up EMail address
	 *	@param email email address
	 *	@return lower case email w/o spaces
	 */
	private static String cleanUpEMail (String email)
	{
		if (email == null || email.length() == 0)
			return "";
		//
		email = email.trim().toLowerCase();
		//	Delete all spaces
		int pos = email.indexOf(" ");
		while (pos != -1)
		{
			email = email.substring(0, pos) + email.substring(pos+1);
			pos = email.indexOf(" ");
		}
		return email;
	}	//	cleanUpEMail

	/**
	 * 	Construct Bogos email
	 *	@param ctx Context
	 *	@return userName.ClientName.com
	 */
	public static String getBogusEMail (Properties ctx)
	{
		String email = System.getProperty("user.name") + "@"
			+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		email = cleanUpEMail(email);
		return email;
	}	//	getBogusEMail

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**
	 * 	Get Client Request EMail
	 *  @param ctx  Context
	 *  @return Request EMail Address
	 */
	public static String getRequestEMail (Properties ctx)
	{
		String email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		if (email.length() != 0)
			return email;

		String sql = "SELECT RequestEMail, RequestUser, RequestUserPw "
			+ "FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(ctx, "#AD_Client_ID"));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString (1);
				email = cleanUpEMail(email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL, email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USER, rs.getString(2));
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USERPW, rs.getString(3));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getRequestEMail", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return email;
	}	//	getRequestEMail


	/**************************************************************************
	 *	Get the current Client EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		String SMTP = Env.getContext(ctx, CTX_SMTP);
		if (SMTP.length() != 0)
			return SMTP;
		//	Get SMTP name
		SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "localhost";
		Env.setContext(ctx, CTX_SMTP, SMTP);
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error ("getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost




	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.smtp.*;
import org.compiere.model.*;

/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.35 2004/04/26 06:15:05 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param userTo user to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, MUser userTo, String subject, String message)
	{
		this (client, userFrom, userTo.getEmail(), subject, message);
	}	//	EMail

	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param to email to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, String to, String subject, String message)
	{
		this (client.getSMTPHost(), 
			userFrom == null ? client.getRequestEMail() : userFrom.getEmail(), 
			to, subject, message);
		if (client.isSmtpAuthorization())
		{
			if (userFrom != null)
				setEMailUser(userFrom.getEmailUser(), userFrom.getEmailUserPW());
			else
				setEMailUser(client.getRequestUser(), client.getRequestUserPW());
		}
		m_valid = isValid (true);
	}	//	EMail

	/**
	 *	Full Constructor
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		if (subject == null || subject.length() == 0)
			setSubject(".");	//	pass validation
		else
			setSubject (subject);
		if (message != null && message.length() > 0)
			setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = null;
		try
		{
			session = Session.getDefaultInstance(props, m_auth);
		}
		catch (Exception e)
		{
			log.error ("send - " + m_auth, e);
			return e.toString();
		}
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			//
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	
	/**************************************************************************
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	
	/**************************************************************************
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 * 	@param recheck if true check main variables
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		if (!recheck)
			return m_valid;
			
		//  From
		if (m_from == null 
			|| m_from.getAddress().length() == 0 
			|| m_from.getAddress().indexOf(' ') != -1)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		//	To
		InternetAddress[] ias = getTos();
		if (ias == null)
		{
			log.warn("isValid - No To");
			return false;
		}
		for (int i = 0; i < ias.length; i++)
		{
			if (ias[i] == null 
				|| ias[i].getAddress().length() == 0
				|| ias[i].getAddress().indexOf(' ') != -1)
			{
				log.warn("isValid - To(" + i + ") is invalid=" + ias[i]);
				return false;
			}
		}

		//	Host
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		
		//	Subject
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**************************************************************************
	 *  Test.
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 * 	--
	 * 	If you get SendFailedException: 550 5.7.1 Unable to relay for ..
	 * 	Check:
	 * 	- Does the SMTP server allow you to relay
	 *    (Exchange: SMTP server - Access)
	 *  - Did you authenticate (setEmailUser)
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.16 2003/02/18 06:12:19 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx), getCurrentUserEMail(ctx), to, subject, message);
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
		Log.trace(Log.l3_Util, "EMail (" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		Log.trace(Log.l3_Util, "EMail (" + smtpHost + ") " + from + " -> " + to,
			"Subject=" + subject + ", Message=" + message);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";



	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		Log.trace(Log.l4_Data, "EMail.send (" + m_smtpHost + ")",
			m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.getTraceLevel() > 9);

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
			m_msg.setDescription("myDestription");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
			t.connect();
		//	t.connect(m_smtpHost, user, password);
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			Log.trace(Log.l4_Data, "EMail.send success", "MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("EMail.send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password");
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			Log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			Log.error("EMail.send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.getTraceLevel() > 9)
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
			{
				System.out.println ("- " + e.nextElement ());
			}
		}
		catch (MessagingException ex)
		{
			System.err.println(ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			Log.error("EMail.getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user
	 *  @param ctx  Context
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx)
	{
		String from = getEMailOfUser (Env.getContextAsInt (ctx, "#AD_User_ID"));
		if (from == null)
			from = Env.getContext(ctx, "#AD_User_Name") + "@"
				+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		//	Delete all spaces
		while (from.indexOf(" ") != -1)
		{
			int pos = from.indexOf(" ");
			from = from.substring(0, pos) + from.substring(pos+1);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID System User ID
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		String from = null;
		//	Get ID
		String sql = "SELECT u.EMail,bp.EMail,bpc.EMail "
			+ "FROM AD_User u, C_BPartner bp, C_BPartner_Contact bpc "
			+ "WHERE u.C_BPartner_ID=bp.C_BPartner_ID(+)"
			+ " AND u.C_BPartner_ID=bpc.C_BPartner_ID(+)"
			+ " AND u.AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() && from == null)
			{
				from = rs.getString(1);
				if (from == null)
					from = rs.getString(2);
				if (from == null)
					from = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("EMail.getEMailOfUser", e);
		}
		//  No user found - do some guessing
		if (from == null)
		{
			return System.getProperty("user.name") + "@"
				+ Env.getContext(Env.getCtx(), "#AD_Client_Name") + ".com";
		}

		//	clean up
		from = from.trim().toLowerCase();
		//	Delete all spaces
		while (from.indexOf(" ") != -1)
		{
			int pos = from.indexOf(" ");
			from = from.substring(0, pos) + from.substring(pos+1);
		}
		return from;
	}	//	getEMailOfUser

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("EMail.getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**************************************************************************
	 *	Get the current EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		//  Test environment
		if (Env.getContext(ctx, "#CompiereSys").equals("Y"))
			return "main";
		//
		String SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "main";
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("EMail.getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost


	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		m_auth = new EMailAuthenticator (username, password);
	}	//	setEmailUser

	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress(newFrom);
		}
		catch (Exception e)
		{
			Log.error("EMail.setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newTo);
		}
		catch (Exception e)
		{
			Log.error("EMail.addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newCc);
		}
		catch (Exception e)
		{
			Log.error("EMail.addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress(newTo);
		}
		catch (Exception e)
		{
			Log.error("EMail.setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						Log.error("EMail.setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					Log.error("EMail.setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			Log.trace(Log.l3_Util, "EMail.isValid", "Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
	//	emailTest.setEMailUser("jjanke", "joergok");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.compiere.model.*;

import com.sun.mail.smtp.*;

/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.37 2004/09/09 14:14:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param userTo user to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, MUser userTo, String subject, String message)
	{
		this (client, userFrom, userTo.getEmail(), subject, message);
	}	//	EMail

	/**
	 * 	Constructor
	 *	@param client client
	 *	@param userFrom optional user from - if null - request mail
	 *	@param to email to
	 *	@param subject optional subject
	 *	@param message optional message
	 */
	public EMail (MClient client, MUser userFrom, String to, String subject, String message)
	{
		this (client.getSMTPHost(), 
			userFrom == null ? client.getRequestEMail() : userFrom.getEmail(), 
			to, subject, message);
		if (client.isSmtpAuthorization())
		{
			if (userFrom != null)
				setEMailUser(userFrom.getEmailUser(), userFrom.getEmailUserPW());
			else
				setEMailUser(client.getRequestUser(), client.getRequestUserPW());
		}
	}	//	EMail

	/**
	 *	Full Constructor
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		if (subject == null || subject.length() == 0)
			setSubject(".");	//	pass validation
		else
			setSubject (subject);
		if (message != null && message.length() > 0)
			setMessageText (message);
		m_valid = isValid (false);
	}	//	EMail

	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = null;
		try
		{
			session = Session.getDefaultInstance(props, m_auth);
		}
		catch (Exception e)
		{
			log.error ("send - " + m_auth, e);
			return e.toString();
		}
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			//
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	
	/**************************************************************************
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	
	/**************************************************************************
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 * 	@param recheck if true check main variables
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		if (!recheck)
			return m_valid;
			
		//  From
		if (m_from == null 
			|| m_from.getAddress().length() == 0 
			|| m_from.getAddress().indexOf(' ') != -1)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		//	To
		InternetAddress[] ias = getTos();
		if (ias == null)
		{
			log.warn("isValid - No To");
			return false;
		}
		for (int i = 0; i < ias.length; i++)
		{
			if (ias[i] == null 
				|| ias[i].getAddress().length() == 0
				|| ias[i].getAddress().indexOf(' ') != -1)
			{
				log.warn("isValid - To(" + i + ") is invalid=" + ias[i]);
				return false;
			}
		}

		//	Host
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		
		//	Subject
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/**************************************************************************
	 *  Test.
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 * 	--
	 * 	If you get SendFailedException: 550 5.7.1 Unable to relay for ..
	 * 	Check:
	 * 	- Does the SMTP server allow you to relay
	 *    (Exchange: SMTP server - Access)
	 *  - Did you authenticate (setEmailUser)
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.30 2003/10/11 05:20:32 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (EMailUtil.getSmtpHost(ctx),
			fromCurrentOrRequest ? EMailUtil.getEMail(ctx, false) : EMailUtil.getEMail_User(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
	//	log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Logger							*/
	protected Logger			log = Logger.getCLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getCLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
		//	log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			Transport.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
		//	log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo - " + e.toString());
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.util;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.*;


/**
 *	EMail Object.
 *	Resources:
 *	http://java.sun.com/products/javamail/index.html
 * 	http://java.sun.com/products/javamail/FAQ.html
 *
 *  <p>
 *  When I try to send a message, I get javax.mail.SendFailedException:
 * 		550 Unable to relay for my-address
 *  <br>
 *  This is an error reply from your SMTP mail server. It indicates that
 *  your mail server is not configured to allow you to send mail through it.
 *
 *  @author Jorg Janke
 *  @version  $Id: EMail.java,v 1.25 2003/07/16 19:08:37 jjanke Exp $
 */
public final class EMail implements Serializable
{
	/**
	 *	Minimum conveniance Constructor for mail from current SMTPHost and User.
	 *
	 *  @param ctx  Context
	 * 	@param fromCurrentOrRequest if true get user or request - otherwise user only
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (Properties ctx, boolean fromCurrentOrRequest,
		String to, String subject, String message)
	{
		this (getCurrentSmtpHost(ctx),
			fromCurrentOrRequest ? getEMail(ctx, false) : getCurrentUserEMail(ctx, false),
			to, subject, message);
		m_ctx = ctx;
	}	//	EMail

	/**
	 *	Minumum Constructor.
	 *  Need to set subject and message
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 */
	public EMail (String smtpHost, String from, String to)
	{
		log.info("(" + smtpHost + ") " + from + " -> " + to);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
	}	//	EMail

	/**
	 *	Full Constructor
	 *
	 *  @param smtpHost The mail server
	 *  @param from Sender's EMail address
	 *  @param to   Recipient EMail address
	 *  @param subject  Subject of message
	 *  @param message  The message
	 */
	public EMail (String smtpHost, String from, String to, String subject, String message)
	{
		log.info("(" + smtpHost + ") " + from + " -> " + to + ", Subject=" + subject + ", Message=" + message);
		setSmtpHost(smtpHost);
		setFrom(from);
		addTo(to);
		setSubject (subject);
		setMessageText (message);
		m_valid = isValid (true);
	}	//	EMail

	/**	From Address				*/
	private InternetAddress     m_from;
	/** To Address					*/
	private ArrayList			m_to;
	/** CC Addresses				*/
	private ArrayList			m_cc;
	/** BCC Addresses				*/
	private ArrayList			m_bcc;
	/**	Reply To Address			*/
	private InternetAddress		m_replyTo;
	/**	Mail Subject				*/
	private String  			m_subject;
	/** Mail Plain Message			*/
	private String  			m_messageText;
	/** Mail HTML Message			*/
	private String  			m_messageHTML;
	/**	Mail SMTP Server			*/
	private String  			m_smtpHost;
	/**	Attachments					*/
	private ArrayList			m_attachments;
	/**	UserName and Password		*/
	private EMailAuthenticator	m_auth = null;
	/**	Message						*/
	private SMTPMessage 		m_msg = null;
	/** Comtext - may be null		*/
	private Properties			m_ctx;

	/**	Info Valid					*/
	private boolean m_valid = false;

	/**	Mail Sent OK Status				*/
	public static final String      SENT_OK = "OK";

	/**	Client SMTP CTX key				*/
	public static final String		CTX_SMTP = "#Client_SMTP";
	/**	User EMail CTX key				*/
	public static final String		CTX_EMAIL = "#User_EMail";
	public static final String		CTX_EMAIL_USER = "#User_EMailUser";
	public static final String		CTX_EMAIL_USERPW = "#User_EMailUserPw";
	/**	Request EMail CTX key			*/
	public static final String		CTX_REQUEST_EMAIL = "#Request_EMail";
	public static final String		CTX_REQUEST_EMAIL_USER = "#Request_EMailUser";
	public static final String		CTX_REQUEST_EMAIL_USERPW = "#Request_EMailUserPw";

	/**	Logger							*/
	protected Logger			log = Logger.getLogger (getClass());
	/**	Logger							*/
	protected static Logger		s_log = Logger.getLogger (EMail.class);

	/**
	 *	Send Mail direct
	 *	@return OK or error message
	 */
	public String send ()
	{
		log.info("send (" + m_smtpHost + ") " + m_from + " -> " + m_to);
		//
		if (!isValid(true))
			return "Invalid Data";
		//
		Properties props = System.getProperties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_smtpHost);
		//
		setEMailUser();
		if (m_auth != null)
			props.put("mail.smtp.auth","true");
		Session session = Session.getDefaultInstance(props, m_auth);
		session.setDebug(Log.isTraceLevel(10));

		try
		{
		//	m_msg = new MimeMessage(session);
			m_msg = new SMTPMessage(session);
			//	Addresses
			m_msg.setFrom(m_from);
			InternetAddress[] rec = getTos();
			if (rec.length == 1)
				m_msg.setRecipient (Message.RecipientType.TO, rec[0]);
			else
				m_msg.setRecipients (Message.RecipientType.TO, rec);
			rec = getCcs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.CC, rec);
			rec = getBccs();
			if (rec != null && rec.length > 0)
				m_msg.setRecipients (Message.RecipientType.BCC, rec);
			if (m_replyTo != null)
				m_msg.setReplyTo(new Address[] {m_replyTo});
			//
			m_msg.setSentDate(new java.util.Date());
			m_msg.setHeader("Comments", "CompiereMail");
		//	m_msg.setDescription("Description");
			//	SMTP specifics
			m_msg.setAllow8bitMIME(true);
			//	Send notification on Failure & Success - no way to set envid in Java yet
			m_msg.setNotifyOptions (SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_SUCCESS);
			//	Bounce only header
			m_msg.setReturnOption (SMTPMessage.RETURN_HDRS);
		//	m_msg.setHeader("X-Mailer", "msgsend");
			//
			setContent();
			m_msg.saveChanges();
			log.debug("send - message =" + m_msg);
			//
		//	Transport.send(msg);
			Transport t = session.getTransport("smtp");
		//	log.debug("send - transport=" + t);
			t.connect();
		//	t.connect(m_smtpHost, user, password);
		//	log.debug("send - transport connected");
			t.send(m_msg);
		//	t.sendMessage(msg, msg.getAllRecipients());
			log.debug("send - success - MessageID=" + m_msg.getMessageID());
		}
		catch (MessagingException me)
		{
			Exception ex = me;
			StringBuffer sb = new StringBuffer("send(ME)");
			boolean printed = false;
			do
			{
				if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException)ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (!printed)
					{
						if (invalid != null && invalid.length > 0)
						{
							sb.append (" - Invalid:");
							for (int i = 0; i < invalid.length; i++)
								sb.append (" ").append (invalid[i]);

						}
						Address[] validUnsent = sfex.getValidUnsentAddresses ();
						if (validUnsent != null && validUnsent.length > 0)
						{
							sb.append (" - ValidUnsent:");
							for (int i = 0; i < validUnsent.length; i++)
								sb.append (" ").append (validUnsent[i]);
						}
						Address[] validSent = sfex.getValidSentAddresses ();
						if (validSent != null && validSent.length > 0)
						{
							sb.append (" - ValidSent:");
							for (int i = 0; i < validSent.length; i++)
								sb.append (" ").append (validSent[i]);
						}
						printed = true;
					}
					if (sfex.getNextException() == null)
						sb.append(" ").append(sfex.getLocalizedMessage());
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					sb.append(" - Invalid Username/Password - " + m_auth);
				}
				else
				{
					String msg = ex.getLocalizedMessage();
					if (msg == null)
						msg = ex.toString();
					sb.append(" ").append(msg);
				}
				if (ex instanceof MessagingException)
					ex = ((MessagingException)ex).getNextException();
				else
					ex = null;
			} while (ex != null);
			log.error(sb.toString(), me);
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("send", e);
			return "EMail.send: " + e.getLocalizedMessage();
		}
		//
		if (Log.isTraceLevel(9))
			dumpMessage();
		return SENT_OK;
	}	//	send

	/**
	 * 	Dump Message Info
	 */
	private void dumpMessage()
	{
		if (m_msg == null)
			return;
		try
		{
			Enumeration e = m_msg.getAllHeaderLines ();
			while (e.hasMoreElements ())
				log.debug("- " + e.nextElement ());
		}
		catch (MessagingException ex)
		{
			log.error("dumpMessage", ex);
		}
	}	//	dumpMessage

	/**
	 * 	Get the message directly
	 * 	@return mail message
	 */
	protected MimeMessage getMimeMessage()
	{
		return m_msg;
	}	//	getMessage

	/**
	 * 	Get Message ID or null
	 * 	@return Message ID e.g. <20030130004739.15377.qmail@web13506.mail.yahoo.com>
	 *  <25699763.1043887247538.JavaMail.jjanke@main>
	 */
	public String getMessageID()
	{
		try
		{
			if (m_msg != null)
				return m_msg.getMessageID ();
		}
		catch (MessagingException ex)
		{
			log.error("getMessageID", ex);
		}
		return null;
	}	//	getMessageID

	/**	Getter/Setter ********************************************************/

	/**
	 * 	Create Authentificator for User
	 * 	@param username user name
	 * 	@param password user password
	 */
	public void setEMailUser (String username, String password)
	{
		if (username == null || password == null)
			log.warn("setEMailUser ignored - " +  username + "/" + password);
		else
		{
			log.debug ("setEMailUser: " + username + "/" + password);
			m_auth = new EMailAuthenticator (username, password);
		}
	}	//	setEmailUser

	/**
	 *	Try to set Authentication
	 */
	private void setEMailUser ()
	{
		//	already set
		if (m_auth != null)
			return;
		//
		String from = m_from.getAddress();
		Properties ctx = m_ctx == null ? Env.getCtx() : m_ctx;
		//
		String email = Env.getContext(ctx, CTX_EMAIL);
		String usr = Env.getContext(ctx, CTX_EMAIL_USER);
		String pwd = Env.getContext(ctx, CTX_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
		{
			setEMailUser (usr, pwd);
			return;
		}
		email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		usr = Env.getContext(ctx, CTX_REQUEST_EMAIL_USER);
		pwd = Env.getContext(ctx, CTX_REQUEST_EMAIL_USERPW);
		if (from.equals(email) && usr.length() > 0 && pwd.length() > 0)
			setEMailUser (usr, pwd);
	}	//	setEMailUser


	/**
	 *  Get Sender
	 *  @return Sender's internet address
	 */
	public InternetAddress getFrom()
	{
		return m_from;
	}   //  getFrom

	/**
	 *  Set Sender
	 *  @param newFrom Sender's email address
	 */
	public void setFrom(String newFrom)
	{
		if (newFrom == null)
		{
			m_valid = false;
			return;
		}
		try
		{
			m_from = new InternetAddress (newFrom, true);
		}
		catch (Exception e)
		{
			log.error("setFrom", e);
			m_valid = false;
		}
	}   //  setFrom

	/**
	 *  Add To Recipient
	 *  @param newTo Recipient's email address
	 * 	@returns true if valid
	 */
	public boolean addTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
		{
			m_valid = false;
			return false;
		}
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("addTo", e);
			m_valid = false;
			return false;
		}
		if (m_to == null)
			m_to = new ArrayList();
		m_to.add(ia);
		return true;
	}   //  addTo

	/**
	 *  Get Recipient
	 *  @return Recipient's internet address
	 */
	public InternetAddress getTo()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress ia = (InternetAddress)m_to.get(0);
		return ia;
	}   //  getTo

	/**
	 *  Get TO Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getTos()
	{
		if (m_to == null || m_to.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_to.size()];
		m_to.toArray(ias);
		return ias;
	}   //  getTos

	/**
	 * 	Add CC Recipient
	 * 	@param newCc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addCc (String newCc)
	{
		if (newCc == null || newCc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newCc, true);
		}
		catch (Exception e)
		{
			log.error("addCc", e);
			return false;
		}
		if (m_cc == null)
			m_cc = new ArrayList();
		m_cc.add (ia);
		return true;
	}	//	addCc

	/**
	 *  Get CC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getCcs()
	{
		if (m_cc == null || m_cc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_cc.size()];
		m_cc.toArray(ias);
		return ias;
	}   //  getCcs

	/**
	 * 	Add BCC Recipient
	 * 	@param newBcc EMail cc Recipient
	 * 	@returns true if valid
	 */
	public boolean addBcc (String newBcc)
	{
		if (newBcc == null || newBcc.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newBcc, true);
		}
		catch (Exception e)
		{
			log.error("addBcc", e);
			return false;
		}
		if (m_bcc == null)
			m_bcc = new ArrayList();
		m_bcc.add (ia);
		return true;
	}	//	addBcc

	/**
	 *  Get BCC Recipients
	 *  @return Recipient's internet address
	 */
	public InternetAddress[] getBccs()
	{
		if (m_bcc == null || m_bcc.size() == 0)
			return null;
		InternetAddress[] ias = new InternetAddress[m_bcc.size()];
		m_bcc.toArray(ias);
		return ias;
	}   //  getBccs

	/**
	 *  Set Reply to Address
	 *  @param newTo email address
	 * 	@returns true if valid
	 */
	public boolean setReplyTo (String newTo)
	{
		if (newTo == null || newTo.length() == 0)
			return false;
		InternetAddress ia = null;
		try
		{
			ia = new InternetAddress (newTo, true);
		}
		catch (Exception e)
		{
			log.error("setReplyTo", e);
			return false;
		}
		m_replyTo = ia;
		return true;
	}   //  setReplyTo

	/**
	 *  Get Reply To
	 *  @return Reoly To internet address
	 */
	public InternetAddress getReplyTo()
	{
		return m_replyTo;
	}   //  getReplyTo

	/*************************************************************************/

	/**
	 *  Set Subject
	 *  @param newSubject Subject
	 */
	public void setSubject(String newSubject)
	{
		if (newSubject == null || newSubject.length() == 0)
			m_valid = false;
		else
			m_subject = newSubject;
	}   //  setSubject

	/**
	 *  Get Subject
	 *  @return subject
	 */
	public String getSubject()
	{
		return m_subject;
	}   //  getSubject

	/**
	 *  Set Message
	 *  @param newMessage message
	 */
	public void setMessageText (String newMessage)
	{
		if (newMessage == null || newMessage.length() == 0)
			m_valid = false;
		else
		{
			m_messageText = newMessage;
			if (!m_messageText.endsWith("\n"))
				m_messageText += "\n";
		}
	}   //  setMessage

	/**
	 *  Get MIME String Message - line ending with CRLF.
	 *  @return message
	 */
	public String getMessageCRLF()
	{
		if (m_messageText == null)
			return "";
		char[] chars = m_messageText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\n')
			{
				int previous = i-1;
				if (previous >= 0 && chars[previous] == '\r')
					sb.append(c);
				else
					sb.append("\r\n");
			}
			else
				sb.append(c);
		}
	//	log.debug("IN  " + m_messageText);
	//	log.debug("OUT " + sb);
		return sb.toString();
	}   //  getMessageCRLF

	/**
	 *  Set HTML Message
	 *  @param html message
	 */
	public void setMessageHTML (String html)
	{
		if (html == null || html.length() == 0)
			m_valid = false;
		else
		{
			m_messageHTML = html;
			if (!m_messageHTML.endsWith("\n"))
				m_messageHTML += "\n";
		}
	}   //  setMessageHTML

	/**
	 *  Set HTML Message
	 *  @param subject subject repeated in message as H2
	 * 	@param message message
	 */
	public void setMessageHTML (String subject, String message)
	{
		m_subject = subject;
		StringBuffer sb = new StringBuffer("<HTML>\n")
			.append("<HEAD>\n")
			.append("<TITLE>\n")
			.append(subject + "\n")
			.append("</TITLE>\n")
			.append("</HEAD>\n");
		sb.append("<BODY>\n")
			.append("<H2>" + subject + "</H2>" + "\n")
			.append(message)
			.append("\n")
			.append("</BODY>\n");
		sb.append("</HTML>\n");
		m_messageHTML = sb.toString();
	}   //  setMessageHTML

	/**
	 *  Get HTML Message
	 *  @return message
	 */
	public String getMessageHTML()
	{
		return m_messageHTML;
	}   //  getMessageHTML

	/**
	 *	Add file Attachment
	 * 	@param file file to attach
	 */
	public void addAttachment (File file)
	{
		if (file == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(file);
	}	//	addAttachment

	/**
	 *	Add url based file Attachment
	 * 	@param url url content to attach
	 */
	public void addAttachment (URL url)
	{
		if (url == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(url);
	}	//	addAttachment

	/**
	 *	Add attachment.
	 *  (converted to ByteArrayDataSource)
	 * 	@param data data
	 * 	@param type MIME type
	 * 	@param name name of attachment
	 */
	public void addAttachment (byte[] data, String type, String name)
	{
		ByteArrayDataSource byteArray = new ByteArrayDataSource (data, type).setName(name);
		addAttachment (byteArray);
	}	//	addAttachment

	/**
	 *	Add arbitary Attachment
	 * 	@param dataSource content to attach
	 */
	public void addAttachment (DataSource dataSource)
	{
		if (dataSource == null)
			return;
		if (m_attachments == null)
			m_attachments = new ArrayList();
		m_attachments.add(dataSource);
	}	//	addAttachment

	/**
	 *	Set the message content
	 * 	@throws MessagingException
	 * 	@throws IOException
	 */
	private void setContent ()
		throws MessagingException, IOException
	{
		m_msg.setSubject (getSubject ());

		//	Simple Message
		if (m_attachments == null || m_attachments.size() == 0)
		{
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				m_msg.setContent (getMessageCRLF(), "text/plain");
			else
				m_msg.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));
			//
			log.debug("setContent (simple) " + getSubject());
		}
		else	//	Multi part message	***************************************
		{
			//	First Part - Message
			MimeBodyPart mbp_1 = new MimeBodyPart();
			mbp_1.setText("");
			if (m_messageHTML == null || m_messageHTML.length () == 0)
				mbp_1.setContent (getMessageCRLF(), "text/plain");
			else
				mbp_1.setDataHandler (new DataHandler
					(new ByteArrayDataSource (m_messageHTML, "text/html")));

			// Create Multipart and its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp_1);
			log.debug("setContent (multi) " + getSubject() + " - " + mbp_1);

			//	for all attachments
			for (int i = 0; i < m_attachments.size(); i++)
			{
				Object attachment = m_attachments.get(i);
				DataSource ds = null;
				if (attachment instanceof File)
				{
					File file = (File)attachment;
					if (file.exists())
						ds = new FileDataSource (file);
					else
					{
						log.error("setContent - File does not exist: " + file);
						continue;
					}
				}
				else if (attachment instanceof URL)
				{
					URL url = (URL)attachment;
					ds = new URLDataSource (url);
				}
				else if (attachment instanceof DataSource)
					ds = (DataSource)attachment;
				else
				{
					log.error("setContent - Attachement type unknown: " + attachment);
					continue;
				}
				//	Attachment Part
				MimeBodyPart mbp_2 = new MimeBodyPart();
				mbp_2.setDataHandler(new DataHandler(ds));
				mbp_2.setFileName(ds.getName());
				log.debug("setContent - Added Attachment " + ds.getName() + " - " + mbp_2);
				mp.addBodyPart(mbp_2);
			}

			//	Add to Message
			m_msg.setContent(mp);
		}	//	multi=part
	}	//	setContent

	/*************************************************************************/

	/**
	 *  Set SMTP Host or address
	 *  @param newSmtpHost Mail server
	 */
	public void setSmtpHost(String newSmtpHost)
	{
		if (newSmtpHost == null || newSmtpHost.length() == 0)
			m_valid = false;
		else
			m_smtpHost = newSmtpHost;
	}   //  setSMTPHost

	/**
	 *  Get Mail Server name or address
	 *  @return mail server
	 */
	public String getSmtpHost()
	{
		return m_smtpHost;
	}   //  getSmtpHosr

	/**
	 *  Is Info valid to send EMail
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid()
	{
		return m_valid;
	}   //  isValid

	/**
	 *  Re-Check Info if valid to send EMail
	 *  @param recheck if true, re-evaluate email
	 *  @return true if email is valid and can be sent
	 */
	public boolean isValid (boolean recheck)
	{
		//  mandatory info
		if (m_from == null || m_from.getAddress().length() == 0)
		{
			log.warn("isValid - From is invalid=" + m_from);
			return false;
		}
		InternetAddress ia = getTo();
		if (ia == null || ia.getAddress().length() == 0)
		{
			log.warn("isValid - To is invalid=" + m_to);
			return false;
		}
		if (m_smtpHost == null || m_smtpHost.length() == 0)
		{
			log.warn("isValid - SMTP Host is invalid" + m_smtpHost);
			return false;
		}
		if (m_subject == null || m_subject.length() == 0)
		{
			log.warn("isValid - Subject is invalid=" + m_subject);
			return false;
		}
		return true;
	}   //  isValid


	/*************************************************************************/

	/**
	 *	Get the EMail Address of current user or request
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		if (AD_User_ID != 0)
			from = getCurrentUserEMail (ctx, true);
		if (from == null || from.length() == 0)
			from = getRequestEMail (ctx);
		//	bogus
		if (from == null || from.length() == 0)
		{
			if (strict)
				return null;
			from = getBogusEMail(ctx);
		}
		return from;
	}   //  getCurrentUserEMail

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 * 	@param strict no bogous email address
	 * 	@param ctx optional context
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID, boolean strict, Properties ctx)
	{
		String email = null;
		//	Get ID
		String sql = "SELECT EMail, EMailUser, EMailUserPw, Name "
			+ "FROM AD_User "
			+ "WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString(1);
				if (email != null)
				{
					email = cleanUpEMail(email);
					if (ctx != null)
					{
						Env.setContext (ctx, CTX_EMAIL, email);
						Env.setContext (ctx, CTX_EMAIL_USER, rs.getString (2));
						Env.setContext (ctx, CTX_EMAIL_USERPW, rs.getString (3));
					}
				}
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getEMailOfUser - " + sql, e);
		}
		if (email == null || email.length() == 0)
		{
			s_log.warn("getEMailOfUser - EMail not found - AD_User_ID=" + AD_User_ID);
			if (strict)
				return null;
			email = getBogusEMail(ctx == null ? Env.getCtx() : ctx);
		}
		return email;
	}	//	getEMailOfUser

	/**
	 *  Get Email Address of AD_User
	 *  @param AD_User_ID user
	 *  @return EMail Address
	 */
	public static String getEMailOfUser (int AD_User_ID)
	{
		return getEMailOfUser(AD_User_ID, false, null);
	}	//	getEMailOfUser

	/**
	 *  Get Email Address current AD_User
	 *  @param ctx  Context
	 * 	@param strict no bogous email address
	 *  @return EMail Address
	 */
	public static String getCurrentUserEMail (Properties ctx, boolean strict)
	{
		String from = Env.getContext(ctx, CTX_EMAIL);
		if (from.length() != 0)
			return from;

		int AD_User_ID = Env.getContextAsInt (ctx, "#AD_User_ID");
		from = getEMailOfUser(AD_User_ID, strict, ctx);
		return from;
	}	//	getCurrentUserEMail

	/**
	 * 	Clean up EMail address
	 *	@param email email address
	 *	@return lower case email w/o spaces
	 */
	private static String cleanUpEMail (String email)
	{
		if (email == null || email.length() == 0)
			return "";
		//
		email = email.trim().toLowerCase();
		//	Delete all spaces
		int pos = email.indexOf(" ");
		while (pos != -1)
		{
			email = email.substring(0, pos) + email.substring(pos+1);
			pos = email.indexOf(" ");
		}
		return email;
	}	//	cleanUpEMail

	/**
	 * 	Construct Bogos email
	 *	@param ctx Context
	 *	@return userName.ClientName.com
	 */
	public static String getBogusEMail (Properties ctx)
	{
		String email = System.getProperty("user.name") + "@"
			+ Env.getContext(ctx, "#AD_Client_Name") + ".com";
		email = cleanUpEMail(email);
		return email;
	}	//	getBogusEMail

	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = null;
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("getNameOfUser", e);
		}
		return name;
	}	//	getNameOfUser

	/**
	 * 	Get Client Request EMail
	 *  @param ctx  Context
	 *  @return Request EMail Address
	 */
	public static String getRequestEMail (Properties ctx)
	{
		String email = Env.getContext(ctx, CTX_REQUEST_EMAIL);
		if (email.length() != 0)
			return email;

		String sql = "SELECT RequestEMail, RequestUser, RequestUserPw "
			+ "FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(ctx, "#AD_Client_ID"));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				email = rs.getString (1);
				email = cleanUpEMail(email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL, email);
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USER, rs.getString(2));
				Env.setContext(ctx, CTX_REQUEST_EMAIL_USERPW, rs.getString(3));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getRequestEMail", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return email;
	}	//	getRequestEMail


	/**************************************************************************
	 *	Get the current Client EMail SMTP Host
	 *  @param ctx  Context
	 *  @return Mail Host
	 */
	public static String getCurrentSmtpHost (Properties ctx)
	{
		String SMTP = Env.getContext(ctx, CTX_SMTP);
		if (SMTP.length() != 0)
			return SMTP;
		//	Get SMTP name
		SMTP = getSmtpHost (Env.getContextAsInt(ctx, "#AD_Client_ID"));
		if (SMTP == null)
			SMTP = "localhost";
		Env.setContext(ctx, CTX_SMTP, SMTP);
		return SMTP;
	}   //  getCurrentSmtpHost

	/**
	 *  Get SMTP Host of Client
	 *  @param AD_Client_ID  Client
	 *  @return Mail Host
	 */
	public static String getSmtpHost (int AD_Client_ID)
	{
		String SMTP = null;
		String sql = "SELECT SMTPHost FROM AD_Client "
			+ "WHERE AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				SMTP = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error ("getSmtpHost", e);
		}
		//
		return SMTP;
	}	//	getCurrentSMTPHost




	/**
	 *  Test
	 *  java -cp CTools.jar;CClient.jar org.compiere.util.EMail main info@compiere.org jjanke@compiere.org "My Subject"  "My Message"
	 *  @param args Array of arguments
	 */
	public static void main (String[] args)
	{
		org.compiere.Compiere.startupClient ();
		Log.setTraceLevel(9);
		/**	Test **/
		EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@compiere.org", "TestSubject", "TestMessage");
	//	EMail emailTest = new EMail("main", "jjanke@compiere.org", "jjanke@yahoo.com");
	//	emailTest.addTo("jjanke@acm.org");
	//	emailTest.addCc("jjanke@yahoo.com");
	//	emailTest.setMessageHTML("My Subject1", "My Message1");
	//	emailTest.addAttachment(new File("C:\\Compiere2\\RUN_Compiere2.sh"));
		emailTest.setEMailUser("info", "test");
		emailTest.send();
		System.exit(0);
		/**	Test	*/

		if (args.length != 5)
		{
			System.out.println("Parameters: smtpHost from to subject message");
			System.out.println("Example: java org.compiere.util.EMail mail.acme.com joe@acme.com sue@acme.com HiThere CheersJoe");
			System.exit(1);
		}
		EMail email = new EMail(args[0], args[1], args[2], args[3], args[4]);
		email.send();
	}   //  main

}	//	EMail
