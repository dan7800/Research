/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.util.*;
import java.sql.*;

import org.compiere.util.*;

/**
 * 	Request Model
 *
 *  @author Jorg Janke
 *  @version $Id: MRequest.java,v 1.5 2004/05/09 04:46:31 jjanke Exp $
 */
public class MRequest extends X_R_Request
{
	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param R_Request_ID request or 0 for new
	 */
	public MRequest(Properties ctx, int R_Request_ID)
	{
		super (ctx, R_Request_ID);
		if (R_Request_ID == 0)
		{
			setDueType (DUETYPE_Due);
		//  setSalesRep_ID (0);
		//	setDocumentNo (null);
			setProcessed (false);
			setRequestAmt (Env.ZERO);
			setPriority (PRIORITY_Medium);
		//  setR_RequestType_ID (0);
		//  setSummary (null);
			setIsEscalated (false);
			setIsSelfService (false);
		}
	}	//	MRequest

	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param SalesRep_ID SalesRep
	 * 	@param R_RequestType_ID request type
	 * 	@param Summary summary
	 * 	@param isSelfService self service
	 */
	public MRequest(Properties ctx, int SalesRep_ID,
		int R_RequestType_ID, String Summary, boolean isSelfService)
	{
		this(ctx, 0);
		setSalesRep_ID (SalesRep_ID);
		setR_RequestType_ID (R_RequestType_ID);
		setSummary (Summary);
		setIsSelfService(isSelfService);
	}	//	MRequest


	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRequest (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
	}	//	MRequest

	/**	Default Request Type		*/
	private Integer			m_R_RequestType_ID;

	/*************************************************************************/

	/**
	 * 	Set Request Type.
	 *	@param R_RequestType_ID request type, if 0 get default
	 */
	public void setR_RequestType_ID (int R_RequestType_ID)
	{
		if (R_RequestType_ID == 0)
		{
			if (m_R_RequestType_ID != null)
			{
				R_RequestType_ID = m_R_RequestType_ID.intValue();
			}
			else
			{
				MRequestType rt = MRequestType.getDefault(getCtx());
				if (rt == null)
					log.error ("setR_RequestType_ID - No default found");
				else
				{
					R_RequestType_ID = rt.getR_RequestType_ID();
					m_R_RequestType_ID = new Integer(R_RequestType_ID);
				}
			}
		}
		super.setR_RequestType_ID(R_RequestType_ID);
	}	//	setR_RequestType_ID

	/**
	 * 	Set Summary with length restrictions
	 *	@param Summary	Summary
	 */
	public void setSummary (String Summary)
	{
		if (Summary != null)
		{
			if (Summary.length() < 2000)
				super.setSummary (Summary);
			else
				super.setSummary (Summary.substring (0, 1999));
		}
	}	//	setSummary
	
	/**
	 * 	Set Result with length restrictions
	 *	@param Result Result
	 */
	public void setResult (String Result)
	{
		if (Result == null)
			super.setResult(null);
		else
		{
			if (Result.length() < 2000)
				super.setResult (Result);
			else
				super.setResult (Result.substring (0, 1999));
		}
	}	//	setResult

	/**
	 * 	Add To Result
	 * 	@param Result
	 */
	public void addToResult (String Result)
	{
		String oldResult = getResult();
		if (Result == null || Result.length() == 0)
			;
		else if (oldResult == null || oldResult.length() == 0)
			setResult (Result);
		else
			setResult (oldResult + "\n-\n" + Result);
	}	//	addToResult

	
	/**************************************************************************
	 * 	Action call
	 * 	@param Result new result
	 *	@return true if processed
	 */
	public boolean action_call (String Result)
	{
		setActionType(ACTIONTYPE_Call);
		addToResult(Result);
		//
		int AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (getSalesRep_ID() != AD_User_ID)
			sendEMailToSalesRep();
		//
		return processIt();
	}	//	action_call
	
	/**
	 * 	Action close
	 *	@return true if processed
	 */
	public boolean action_close()
	{
		setActionType(ACTIONTYPE_Close);
		setProcessed(true);
		return processIt();
	}	//	action_close
	
	/**
	 * 	Action credit
	 *	@return true if processed
	 */
	boolean action_credit()
	{
		setActionType(ACTIONTYPE_Credit);
		return processIt();
	}	//	action_credit

	/**
	 * 	Action email
	 *	@return true if processed
	 */
	public boolean action_email(String MailSubject, String MailText)
	{
		setActionType(ACTIONTYPE_EMail);
		setMailSubject(MailSubject);
		setMailText(MailText);
		//
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get(getCtx(), getSalesRep_ID());
		MUser to = new MUser (getCtx(), getAD_User_ID());
		
		EMail email = new EMail(client, from, to, MailSubject, MailText);
		String msg = email.send();
		addToResult(Msg.getMsg(getCtx(), EMail.SENT_OK.equals(msg) ? 
			"RequestActionEMailOK" : "RequestActionEMailError"));
		return processIt();
	}	//	action_email

	/**
	 * 	Action invoice
	 *	@return true if processed
	 */
	boolean action_Invoice()
	{
		setActionType(ACTIONTYPE_Invoice);
		return processIt();
	}	//	action_invoice
	
	/**
	 * 	Action mail 
	 *	@return true if processed
	 */
	boolean action_mail()
	{
		setActionType(ACTIONTYPE_Mail);
		return processIt();
	}	//	action_mail
	
	/**
	 * 	Action Offer
	 *	@return true if processed
	 */
	boolean action_offer()
	{
		setActionType(ACTIONTYPE_OfferQuote);
		return processIt();
	}	//	action_offer
	
	/**
	 * 	Action Order
	 *	@return true if processed
	 */
	boolean action_order()
	{
		setActionType(ACTIONTYPE_Order);
		return processIt();
	}	//	action_order
	
	/**
	 * 	Action re-open
	 *	@return true if processed
	 */
	public boolean action_reopen()
	{
		setProcessed(false);
		return processIt();
	}	//	action_reopen

	/**
	 * 	Action reminder
	 * 	@param DateNextAction next action
	 *	@return true if processed
	 */
	public boolean action_reminder(Timestamp DateNextAction)
	{
		setActionType(ACTIONTYPE_Reminder);
		setDateNextAction(DateNextAction);
		return processIt();
	}	//	action_reminder

	/**
	 * 	Action transfer
	 * 	@param AD_User_ID user who initiated the transfer or 0
	 * 	@param newSalesRep_ID new Sales Rep
	 *	@return true if processed
	 */
	public boolean action_transfer (int AD_User_ID, int newSalesRep_ID)
	{
		//	Sender
		if (AD_User_ID == 0)
			AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (AD_User_ID == 0)
			AD_User_ID = getUpdatedBy();

		//  RequestActionTransfer - Request {0} was transfered by {1} to {2}
		Object[] args = new Object[] {getDocumentNo(), 
			EMailUtil.getNameOfUser(AD_User_ID), 
			EMailUtil.getNameOfUser(newSalesRep_ID)};
		String subject = Msg.getMsg(getCtx(), "RequestActionTransfer", args);
		//  Optional Info mail
		String message = subject + "\n" + getSummary();
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get (getCtx(), AD_User_ID);
		MUser to = MUser.get (getCtx(), newSalesRep_ID);
		EMail email = new EMail(client, from, to, subject, message);
		String msg = email.send();
		//
		setActionType(ACTIONTYPE_Transfer);
		setSalesRep_ID(newSalesRep_ID);
		setUpdatedBy (AD_User_ID);
		return processIt();
	}	//	action_transfer
	
	/**
	 *	Common Action
	 *	@return true if processed
	 */
	private boolean processIt()
	{
		MRequestAction ra = new MRequestAction(this);
		if (ra.save())
		{
			setDateLastAction(getUpdated());
			setLastResult(getResult());
			setDueType(getDateNextAction());
			//
			setResult(null);
			setMailText(null);
			setR_MailText_ID(0);
			setActionType(null);
			if (!save())
			{
				log.error("processIt - NOT saved");
				return false;
			}
		}
		else
		{
			log.error("processIt - Action NOT saved");
			return false;
		}
		return true;
	}	//	process it

	/**
	 * 	Set DueType based on Date
	 *	@param date
	 */
	public void setDueType(Timestamp date)
	{
		String DueType = DUETYPE_Due;
		Timestamp now = new Timestamp (System.currentTimeMillis());
		if (date == null)
			;
		else if (date.after(now))
			DueType = DUETYPE_Scheduled;
		else if (date.before(now))
			DueType = DUETYPE_Overdue;
		super.setDueType(DueType);
	}	//	setDueType
	
	/**
	 * 	Send Update EMail To SalesRep
	 */
	public boolean sendEMailToSalesRep()
	{
		String subject = "Request Updated " + getDocumentNo();
		MClient client = MClient.get(getCtx());
		MUser to = MUser.get (getCtx(), getSalesRep_ID());

		EMail email = new EMail(client, null, to, 
			subject, subject + "\n\n" + getResult());
		String msg = email.send();
		return EMail.SENT_OK.equals(msg);
	}	//	sendEMailToSalesRep

	
	/**************************************************************************
	 * 	Get Actions
	 *	@return array of actions
	 */
	public MRequestAction[] getActions()
	{
		String sql = "SELECT * FROM R_RequestAction "
			+ "WHERE R_Request_ID=? "
			+ "ORDER BY Created DESC";
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getR_Request_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRequestAction(getCtx(), rs));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("getActions", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		MRequestAction[] retValue = new MRequestAction[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getActions

	/**
	 *	Get Request Type String
	 *	@return Request Type String 	
	 */
	public String getRequestType()
	{
		int R_RequestType_ID = getR_RequestType_ID();
		MRequestType rt = MRequestType.get (getCtx(), R_RequestType_ID); 
		return rt.getName();
	}	//	getRequestType

	/**
	 * 	Get ActionType Text
	 *	@return text
	 */
	public String getActionTypeText()
	{
		return MRefList.getListName(getCtx(), ACTIONTYPE_AD_Reference_ID, getActionType());
	}	//	getActionTypeText

	/**
	 * 	Get DueType Text
	 *	@return text
	 */
	public String getDueTypeText()
	{
		return MRefList.getListName(getCtx(), DUETYPE_AD_Reference_ID, getDueType());
	}	//	getDueTypeText

}	//	MRequest
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 * 	Request Model
 *
 *  @author Jorg Janke
 *  @version $Id: MRequest.java,v 1.8 2004/09/01 20:05:11 jjanke Exp $
 */
public class MRequest extends X_R_Request
{
	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param R_Request_ID request or 0 for new
	 */
	public MRequest(Properties ctx, int R_Request_ID)
	{
		super (ctx, R_Request_ID);
		if (R_Request_ID == 0)
		{
			setDueType (DUETYPE_Due);
		//  setSalesRep_ID (0);
		//	setDocumentNo (null);
			setProcessed (false);
			setRequestAmt (Env.ZERO);
			setPriority (PRIORITY_Medium);
		//  setR_RequestType_ID (0);
		//  setSummary (null);
			setIsEscalated (false);
			setIsSelfService (false);
		}
	}	//	MRequest

	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param SalesRep_ID SalesRep
	 * 	@param R_RequestType_ID request type
	 * 	@param Summary summary
	 * 	@param isSelfService self service
	 */
	public MRequest(Properties ctx, int SalesRep_ID,
		int R_RequestType_ID, String Summary, boolean isSelfService)
	{
		this(ctx, 0);
		setSalesRep_ID (SalesRep_ID);
		setR_RequestType_ID (R_RequestType_ID);
		setSummary (Summary);
		setIsSelfService(isSelfService);
	}	//	MRequest


	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRequest (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
	}	//	MRequest

	/**	Default Request Type		*/
	private int				m_Default_RequestType_ID;
	/** Request Type				*/
	private MRequestType	m_requestType = null;
	
	/**************************************************************************
	 * 	Set Request Type.
	 */
	public void setR_RequestType_ID ()
	{
		if (m_Default_RequestType_ID != 0)
		{
			MRequestType rt = MRequestType.getDefault(getCtx());
			if (rt == null)
				log.error ("setR_RequestType_ID - No default found");
			else
				m_Default_RequestType_ID = rt.getR_RequestType_ID();
		}
		super.setR_RequestType_ID(m_Default_RequestType_ID);
	}	//	setR_RequestType_ID

	
	/**
	 * 	Add To Result
	 * 	@param Result
	 */
	public void addToResult (String Result)
	{
		String oldResult = getResult();
		if (Result == null || Result.length() == 0)
			;
		else if (oldResult == null || oldResult.length() == 0)
			setResult (Result);
		else
			setResult (oldResult + "\n-\n" + Result);
	}	//	addToResult

	
	/**************************************************************************
	 * 	Action call
	 * 	@param Result new result
	 *	@return true if processed
	 */
	public boolean action_call (String Result)
	{
		setActionType(ACTIONTYPE_Call);
		addToResult(Result);
		//
		int AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (getSalesRep_ID() != AD_User_ID)
			sendEMailToSalesRep();
		//
		return processIt();
	}	//	action_call
	
	/**
	 * 	Action close
	 *	@return true if processed
	 */
	public boolean action_close()
	{
		setActionType(ACTIONTYPE_Close);
		setProcessed(true);
		return processIt();
	}	//	action_close
	
	/**
	 * 	Action credit
	 *	@return true if processed
	 */
	boolean action_credit()
	{
		setActionType(ACTIONTYPE_Credit);
		return processIt();
	}	//	action_credit

	/**
	 * 	Action email
	 *	@return true if processed
	 */
	public boolean action_email(String MailSubject, String MailText)
	{
		setActionType(ACTIONTYPE_EMail);
		setMailSubject(MailSubject);
		setMailText(MailText);
		//
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get(getCtx(), getSalesRep_ID());
		MUser to = new MUser (getCtx(), getAD_User_ID());
		
		EMail email = new EMail(client, from, to, MailSubject, MailText);
		String msg = email.send();
		addToResult(Msg.getMsg(getCtx(), EMail.SENT_OK.equals(msg) ? 
			"RequestActionEMailOK" : "RequestActionEMailError"));
		return processIt();
	}	//	action_email

	/**
	 * 	Action invoice
	 *	@return true if processed
	 */
	boolean action_Invoice()
	{
		setActionType(ACTIONTYPE_Invoice);
		return processIt();
	}	//	action_invoice
	
	/**
	 * 	Action mail 
	 *	@return true if processed
	 */
	boolean action_mail()
	{
		setActionType(ACTIONTYPE_Mail);
		return processIt();
	}	//	action_mail
	
	/**
	 * 	Action Offer
	 *	@return true if processed
	 */
	boolean action_offer()
	{
		setActionType(ACTIONTYPE_OfferQuote);
		return processIt();
	}	//	action_offer
	
	/**
	 * 	Action Order
	 *	@return true if processed
	 */
	boolean action_order()
	{
		setActionType(ACTIONTYPE_Order);
		return processIt();
	}	//	action_order
	
	/**
	 * 	Action re-open
	 *	@return true if processed
	 */
	public boolean action_reopen()
	{
		setActionType(ACTIONTYPE_Re_Open);
		setProcessed(false);
		return processIt();
	}	//	action_reopen

	/**
	 * 	Action reminder
	 * 	@param DateNextAction next action
	 *	@return true if processed
	 */
	public boolean action_reminder(Timestamp DateNextAction)
	{
		setActionType(ACTIONTYPE_Reminder);
		setDateNextAction(DateNextAction);
		return processIt();
	}	//	action_reminder

	/**
	 * 	Action transfer
	 * 	@param AD_User_ID user who initiated the transfer or 0
	 * 	@param newSalesRep_ID new Sales Rep
	 *	@return true if processed
	 */
	public boolean action_transfer (int AD_User_ID, int newSalesRep_ID)
	{
		//	Sender
		if (AD_User_ID == 0)
			AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (AD_User_ID == 0)
			AD_User_ID = getUpdatedBy();

		//  RequestActionTransfer - Request {0} was transfered by {1} to {2}
		Object[] args = new Object[] {getDocumentNo(), 
			EMailUtil.getNameOfUser(AD_User_ID), 
			EMailUtil.getNameOfUser(newSalesRep_ID)};
		String subject = Msg.getMsg(getCtx(), "RequestActionTransfer", args);
		//  Optional Info mail
		String message = subject + "\n" + getSummary();
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get (getCtx(), AD_User_ID);
		MUser to = MUser.get (getCtx(), newSalesRep_ID);
		EMail email = new EMail(client, from, to, subject, message);
		String msg = email.send();
		//
		setActionType(ACTIONTYPE_Transfer);
		setSalesRep_ID(newSalesRep_ID);
		setUpdatedBy (AD_User_ID);
		return processIt();
	}	//	action_transfer
	
	/**
	 *	Common Action
	 *	@return true if processed
	 */
	public boolean processIt()
	{
		MRequestAction ra = new MRequestAction(this);
		if (ra.save())
		{
			setDateLastAction(getUpdated());
			setLastResult(getResult());
			setDueType();
			//
			setResult(null);
			setMailText(null);
			setR_MailText_ID(0);
			setActionType(null);
			if (!save())
			{
				log.error("processIt - NOT saved");
				return false;
			}
		}
		else
		{
			log.error("processIt - Action NOT saved");
			return false;
		}
		return true;
	}	//	process it

	/**
	 * 	Set DueType based on Date Next Action
	 */
	public void setDueType()
	{
		Timestamp due = getDateNextAction();
		if (due == null)
			return;
		//
		Timestamp overdue = TimeUtil.addDays(due, getRequestType().getDueDateTolerance());
		Timestamp now = new Timestamp (System.currentTimeMillis());
		//
		String DueType = DUETYPE_Due;
		if (now.before(due))
			DueType = DUETYPE_Scheduled;
		else if (now.after(overdue))
			DueType = DUETYPE_Overdue;
		super.setDueType(DueType);
	}	//	setDueType

	
	/**
	 * 	Send Update EMail To SalesRep
	 */
	public boolean sendEMailToSalesRep()
	{
		String subject = "Request Updated " + getDocumentNo();
		MClient client = MClient.get(getCtx());
		MUser to = MUser.get (getCtx(), getSalesRep_ID());

		EMail email = new EMail(client, null, to, 
			subject, subject + "\n\n" + getResult());
		String msg = email.send();
		return EMail.SENT_OK.equals(msg);
	}	//	sendEMailToSalesRep

	
	/**************************************************************************
	 * 	Get Actions
	 *	@return array of actions
	 */
	public MRequestAction[] getActions()
	{
		String sql = "SELECT * FROM R_RequestAction "
			+ "WHERE R_Request_ID=? "
			+ "ORDER BY Created DESC";
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getR_Request_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRequestAction(getCtx(), rs));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("getActions", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		MRequestAction[] retValue = new MRequestAction[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getActions

	/**
	 *	Get Request Type String
	 *	@return Request Type String 	
	 */
	public MRequestType getRequestType()
	{
		if (m_requestType == null)
		{
			int R_RequestType_ID = getR_RequestType_ID();
			if (R_RequestType_ID == 0)
			{
				setR_RequestType_ID();
				R_RequestType_ID = getR_RequestType_ID();
			}
			m_requestType = MRequestType.get (getCtx(), R_RequestType_ID);
		}
		return m_requestType;
	}	//	getRequestType

	/**
	 * 	Get ActionType Text
	 *	@return text
	 */
	public String getActionTypeText()
	{
		return MRefList.getListName(getCtx(), ACTIONTYPE_AD_Reference_ID, getActionType());
	}	//	getActionTypeText

	/**
	 * 	Get DueType Text
	 *	@return text
	 */
	public String getDueTypeText()
	{
		return MRefList.getListName(getCtx(), DUETYPE_AD_Reference_ID, getDueType());
	}	//	getDueTypeText
	
	/**
	 * 	Set Date Last Alert to today
	 */
	public void setDateLastAlert ()
	{
		super.setDateLastAlert (new Timestamp(System.currentTimeMillis()));
	}	//	setDateLastAlert
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (getR_RequestType_ID() == 0)
			setR_RequestType_ID();		//	sets default
		
		//	Verify Action Type info
		if (getActionType() == null)
			;
		else if (ACTIONTYPE_Reminder.equals(getActionType()))
		{
			if (getDateNextAction() == null)
				setDateNextAction(TimeUtil.addDays(new Timestamp(System.currentTimeMillis()), 1));
		}
		
		//	Validate/Update Due Type
		setDueType();
		
		return true;
	}	//	beforeSave
	
	/**
	 * 	Get Sales Rep
	 *	@return Sales Rep User
	 */
	public MUser getSalesRep()
	{
		return MUser.get(getCtx(), getSalesRep_ID());
	}	//	getSalesRep
	
}	//	MRequest
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.util.*;
import java.sql.*;

import org.compiere.util.*;

/**
 * 	Request Model
 *
 *  @author Jorg Janke
 *  @version $Id: MRequest.java,v 1.5 2004/05/09 04:46:31 jjanke Exp $
 */
public class MRequest extends X_R_Request
{
	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param R_Request_ID request or 0 for new
	 */
	public MRequest(Properties ctx, int R_Request_ID)
	{
		super (ctx, R_Request_ID);
		if (R_Request_ID == 0)
		{
			setDueType (DUETYPE_Due);
		//  setSalesRep_ID (0);
		//	setDocumentNo (null);
			setProcessed (false);
			setRequestAmt (Env.ZERO);
			setPriority (PRIORITY_Medium);
		//  setR_RequestType_ID (0);
		//  setSummary (null);
			setIsEscalated (false);
			setIsSelfService (false);
		}
	}	//	MRequest

	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param SalesRep_ID SalesRep
	 * 	@param R_RequestType_ID request type
	 * 	@param Summary summary
	 * 	@param isSelfService self service
	 */
	public MRequest(Properties ctx, int SalesRep_ID,
		int R_RequestType_ID, String Summary, boolean isSelfService)
	{
		this(ctx, 0);
		setSalesRep_ID (SalesRep_ID);
		setR_RequestType_ID (R_RequestType_ID);
		setSummary (Summary);
		setIsSelfService(isSelfService);
	}	//	MRequest


	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRequest (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
	}	//	MRequest

	/**	Default Request Type		*/
	private Integer			m_R_RequestType_ID;

	/*************************************************************************/

	/**
	 * 	Set Request Type.
	 *	@param R_RequestType_ID request type, if 0 get default
	 */
	public void setR_RequestType_ID (int R_RequestType_ID)
	{
		if (R_RequestType_ID == 0)
		{
			if (m_R_RequestType_ID != null)
			{
				R_RequestType_ID = m_R_RequestType_ID.intValue();
			}
			else
			{
				MRequestType rt = MRequestType.getDefault(getCtx());
				if (rt == null)
					log.error ("setR_RequestType_ID - No default found");
				else
				{
					R_RequestType_ID = rt.getR_RequestType_ID();
					m_R_RequestType_ID = new Integer(R_RequestType_ID);
				}
			}
		}
		super.setR_RequestType_ID(R_RequestType_ID);
	}	//	setR_RequestType_ID

	/**
	 * 	Set Summary with length restrictions
	 *	@param Summary	Summary
	 */
	public void setSummary (String Summary)
	{
		if (Summary != null)
		{
			if (Summary.length() < 2000)
				super.setSummary (Summary);
			else
				super.setSummary (Summary.substring (0, 1999));
		}
	}	//	setSummary
	
	/**
	 * 	Set Result with length restrictions
	 *	@param Result Result
	 */
	public void setResult (String Result)
	{
		if (Result == null)
			super.setResult(null);
		else
		{
			if (Result.length() < 2000)
				super.setResult (Result);
			else
				super.setResult (Result.substring (0, 1999));
		}
	}	//	setResult

	/**
	 * 	Add To Result
	 * 	@param Result
	 */
	public void addToResult (String Result)
	{
		String oldResult = getResult();
		if (Result == null || Result.length() == 0)
			;
		else if (oldResult == null || oldResult.length() == 0)
			setResult (Result);
		else
			setResult (oldResult + "\n-\n" + Result);
	}	//	addToResult

	
	/**************************************************************************
	 * 	Action call
	 * 	@param Result new result
	 *	@return true if processed
	 */
	public boolean action_call (String Result)
	{
		setActionType(ACTIONTYPE_Call);
		addToResult(Result);
		//
		int AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (getSalesRep_ID() != AD_User_ID)
			sendEMailToSalesRep();
		//
		return processIt();
	}	//	action_call
	
	/**
	 * 	Action close
	 *	@return true if processed
	 */
	public boolean action_close()
	{
		setActionType(ACTIONTYPE_Close);
		setProcessed(true);
		return processIt();
	}	//	action_close
	
	/**
	 * 	Action credit
	 *	@return true if processed
	 */
	boolean action_credit()
	{
		setActionType(ACTIONTYPE_Credit);
		return processIt();
	}	//	action_credit

	/**
	 * 	Action email
	 *	@return true if processed
	 */
	public boolean action_email(String MailSubject, String MailText)
	{
		setActionType(ACTIONTYPE_EMail);
		setMailSubject(MailSubject);
		setMailText(MailText);
		//
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get(getCtx(), getSalesRep_ID());
		MUser to = new MUser (getCtx(), getAD_User_ID());
		
		EMail email = new EMail(client, from, to, MailSubject, MailText);
		String msg = email.send();
		addToResult(Msg.getMsg(getCtx(), EMail.SENT_OK.equals(msg) ? 
			"RequestActionEMailOK" : "RequestActionEMailError"));
		return processIt();
	}	//	action_email

	/**
	 * 	Action invoice
	 *	@return true if processed
	 */
	boolean action_Invoice()
	{
		setActionType(ACTIONTYPE_Invoice);
		return processIt();
	}	//	action_invoice
	
	/**
	 * 	Action mail 
	 *	@return true if processed
	 */
	boolean action_mail()
	{
		setActionType(ACTIONTYPE_Mail);
		return processIt();
	}	//	action_mail
	
	/**
	 * 	Action Offer
	 *	@return true if processed
	 */
	boolean action_offer()
	{
		setActionType(ACTIONTYPE_OfferQuote);
		return processIt();
	}	//	action_offer
	
	/**
	 * 	Action Order
	 *	@return true if processed
	 */
	boolean action_order()
	{
		setActionType(ACTIONTYPE_Order);
		return processIt();
	}	//	action_order
	
	/**
	 * 	Action re-open
	 *	@return true if processed
	 */
	public boolean action_reopen()
	{
		setProcessed(false);
		return processIt();
	}	//	action_reopen

	/**
	 * 	Action reminder
	 * 	@param DateNextAction next action
	 *	@return true if processed
	 */
	public boolean action_reminder(Timestamp DateNextAction)
	{
		setActionType(ACTIONTYPE_Reminder);
		setDateNextAction(DateNextAction);
		return processIt();
	}	//	action_reminder

	/**
	 * 	Action transfer
	 * 	@param AD_User_ID user who initiated the transfer or 0
	 * 	@param newSalesRep_ID new Sales Rep
	 *	@return true if processed
	 */
	public boolean action_transfer (int AD_User_ID, int newSalesRep_ID)
	{
		//	Sender
		if (AD_User_ID == 0)
			AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (AD_User_ID == 0)
			AD_User_ID = getUpdatedBy();

		//  RequestActionTransfer - Request {0} was transfered by {1} to {2}
		Object[] args = new Object[] {getDocumentNo(), 
			EMailUtil.getNameOfUser(AD_User_ID), 
			EMailUtil.getNameOfUser(newSalesRep_ID)};
		String subject = Msg.getMsg(getCtx(), "RequestActionTransfer", args);
		//  Optional Info mail
		String message = subject + "\n" + getSummary();
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get (getCtx(), AD_User_ID);
		MUser to = MUser.get (getCtx(), newSalesRep_ID);
		EMail email = new EMail(client, from, to, subject, message);
		String msg = email.send();
		//
		setActionType(ACTIONTYPE_Transfer);
		setSalesRep_ID(newSalesRep_ID);
		setUpdatedBy (AD_User_ID);
		return processIt();
	}	//	action_transfer
	
	/**
	 *	Common Action
	 *	@return true if processed
	 */
	private boolean processIt()
	{
		MRequestAction ra = new MRequestAction(this);
		if (ra.save())
		{
			setDateLastAction(getUpdated());
			setLastResult(getResult());
			setDueType(getDateNextAction());
			//
			setResult(null);
			setMailText(null);
			setR_MailText_ID(0);
			setActionType(null);
			if (!save())
			{
				log.error("processIt - NOT saved");
				return false;
			}
		}
		else
		{
			log.error("processIt - Action NOT saved");
			return false;
		}
		return true;
	}	//	process it

	/**
	 * 	Set DueType based on Date
	 *	@param date
	 */
	public void setDueType(Timestamp date)
	{
		String DueType = DUETYPE_Due;
		Timestamp now = new Timestamp (System.currentTimeMillis());
		if (date == null)
			;
		else if (date.after(now))
			DueType = DUETYPE_Scheduled;
		else if (date.before(now))
			DueType = DUETYPE_Overdue;
		super.setDueType(DueType);
	}	//	setDueType
	
	/**
	 * 	Send Update EMail To SalesRep
	 */
	public boolean sendEMailToSalesRep()
	{
		String subject = "Request Updated " + getDocumentNo();
		MClient client = MClient.get(getCtx());
		MUser to = MUser.get (getCtx(), getSalesRep_ID());

		EMail email = new EMail(client, null, to, 
			subject, subject + "\n\n" + getResult());
		String msg = email.send();
		return EMail.SENT_OK.equals(msg);
	}	//	sendEMailToSalesRep

	
	/**************************************************************************
	 * 	Get Actions
	 *	@return array of actions
	 */
	public MRequestAction[] getActions()
	{
		String sql = "SELECT * FROM R_RequestAction "
			+ "WHERE R_Request_ID=? "
			+ "ORDER BY Created DESC";
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getR_Request_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRequestAction(getCtx(), rs));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("getActions", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		MRequestAction[] retValue = new MRequestAction[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getActions

	/**
	 *	Get Request Type String
	 *	@return Request Type String 	
	 */
	public String getRequestType()
	{
		int R_RequestType_ID = getR_RequestType_ID();
		MRequestType rt = MRequestType.get (getCtx(), R_RequestType_ID); 
		return rt.getName();
	}	//	getRequestType

	/**
	 * 	Get ActionType Text
	 *	@return text
	 */
	public String getActionTypeText()
	{
		return MRefList.getListName(getCtx(), ACTIONTYPE_AD_Reference_ID, getActionType());
	}	//	getActionTypeText

	/**
	 * 	Get DueType Text
	 *	@return text
	 */
	public String getDueTypeText()
	{
		return MRefList.getListName(getCtx(), DUETYPE_AD_Reference_ID, getDueType());
	}	//	getDueTypeText

}	//	MRequest
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 * 	Request Model
 *
 *  @author Jorg Janke
 *  @version $Id: MRequest.java,v 1.8 2004/09/01 20:05:11 jjanke Exp $
 */
public class MRequest extends X_R_Request
{
	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param R_Request_ID request or 0 for new
	 */
	public MRequest(Properties ctx, int R_Request_ID)
	{
		super (ctx, R_Request_ID);
		if (R_Request_ID == 0)
		{
			setDueType (DUETYPE_Due);
		//  setSalesRep_ID (0);
		//	setDocumentNo (null);
			setProcessed (false);
			setRequestAmt (Env.ZERO);
			setPriority (PRIORITY_Medium);
		//  setR_RequestType_ID (0);
		//  setSummary (null);
			setIsEscalated (false);
			setIsSelfService (false);
		}
	}	//	MRequest

	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param SalesRep_ID SalesRep
	 * 	@param R_RequestType_ID request type
	 * 	@param Summary summary
	 * 	@param isSelfService self service
	 */
	public MRequest(Properties ctx, int SalesRep_ID,
		int R_RequestType_ID, String Summary, boolean isSelfService)
	{
		this(ctx, 0);
		setSalesRep_ID (SalesRep_ID);
		setR_RequestType_ID (R_RequestType_ID);
		setSummary (Summary);
		setIsSelfService(isSelfService);
	}	//	MRequest


	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRequest (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
	}	//	MRequest

	/**	Default Request Type		*/
	private int				m_Default_RequestType_ID;
	/** Request Type				*/
	private MRequestType	m_requestType = null;
	
	/**************************************************************************
	 * 	Set Request Type.
	 */
	public void setR_RequestType_ID ()
	{
		if (m_Default_RequestType_ID != 0)
		{
			MRequestType rt = MRequestType.getDefault(getCtx());
			if (rt == null)
				log.error ("setR_RequestType_ID - No default found");
			else
				m_Default_RequestType_ID = rt.getR_RequestType_ID();
		}
		super.setR_RequestType_ID(m_Default_RequestType_ID);
	}	//	setR_RequestType_ID

	
	/**
	 * 	Add To Result
	 * 	@param Result
	 */
	public void addToResult (String Result)
	{
		String oldResult = getResult();
		if (Result == null || Result.length() == 0)
			;
		else if (oldResult == null || oldResult.length() == 0)
			setResult (Result);
		else
			setResult (oldResult + "\n-\n" + Result);
	}	//	addToResult

	
	/**************************************************************************
	 * 	Action call
	 * 	@param Result new result
	 *	@return true if processed
	 */
	public boolean action_call (String Result)
	{
		setActionType(ACTIONTYPE_Call);
		addToResult(Result);
		//
		int AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (getSalesRep_ID() != AD_User_ID)
			sendEMailToSalesRep();
		//
		return processIt();
	}	//	action_call
	
	/**
	 * 	Action close
	 *	@return true if processed
	 */
	public boolean action_close()
	{
		setActionType(ACTIONTYPE_Close);
		setProcessed(true);
		return processIt();
	}	//	action_close
	
	/**
	 * 	Action credit
	 *	@return true if processed
	 */
	boolean action_credit()
	{
		setActionType(ACTIONTYPE_Credit);
		return processIt();
	}	//	action_credit

	/**
	 * 	Action email
	 *	@return true if processed
	 */
	public boolean action_email(String MailSubject, String MailText)
	{
		setActionType(ACTIONTYPE_EMail);
		setMailSubject(MailSubject);
		setMailText(MailText);
		//
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get(getCtx(), getSalesRep_ID());
		MUser to = new MUser (getCtx(), getAD_User_ID());
		
		EMail email = new EMail(client, from, to, MailSubject, MailText);
		String msg = email.send();
		addToResult(Msg.getMsg(getCtx(), EMail.SENT_OK.equals(msg) ? 
			"RequestActionEMailOK" : "RequestActionEMailError"));
		return processIt();
	}	//	action_email

	/**
	 * 	Action invoice
	 *	@return true if processed
	 */
	boolean action_Invoice()
	{
		setActionType(ACTIONTYPE_Invoice);
		return processIt();
	}	//	action_invoice
	
	/**
	 * 	Action mail 
	 *	@return true if processed
	 */
	boolean action_mail()
	{
		setActionType(ACTIONTYPE_Mail);
		return processIt();
	}	//	action_mail
	
	/**
	 * 	Action Offer
	 *	@return true if processed
	 */
	boolean action_offer()
	{
		setActionType(ACTIONTYPE_OfferQuote);
		return processIt();
	}	//	action_offer
	
	/**
	 * 	Action Order
	 *	@return true if processed
	 */
	boolean action_order()
	{
		setActionType(ACTIONTYPE_Order);
		return processIt();
	}	//	action_order
	
	/**
	 * 	Action re-open
	 *	@return true if processed
	 */
	public boolean action_reopen()
	{
		setActionType(ACTIONTYPE_Re_Open);
		setProcessed(false);
		return processIt();
	}	//	action_reopen

	/**
	 * 	Action reminder
	 * 	@param DateNextAction next action
	 *	@return true if processed
	 */
	public boolean action_reminder(Timestamp DateNextAction)
	{
		setActionType(ACTIONTYPE_Reminder);
		setDateNextAction(DateNextAction);
		return processIt();
	}	//	action_reminder

	/**
	 * 	Action transfer
	 * 	@param AD_User_ID user who initiated the transfer or 0
	 * 	@param newSalesRep_ID new Sales Rep
	 *	@return true if processed
	 */
	public boolean action_transfer (int AD_User_ID, int newSalesRep_ID)
	{
		//	Sender
		if (AD_User_ID == 0)
			AD_User_ID = Env.getContextAsInt(p_ctx, "#AD_User_ID");
		if (AD_User_ID == 0)
			AD_User_ID = getUpdatedBy();

		//  RequestActionTransfer - Request {0} was transfered by {1} to {2}
		Object[] args = new Object[] {getDocumentNo(), 
			EMailUtil.getNameOfUser(AD_User_ID), 
			EMailUtil.getNameOfUser(newSalesRep_ID)};
		String subject = Msg.getMsg(getCtx(), "RequestActionTransfer", args);
		//  Optional Info mail
		String message = subject + "\n" + getSummary();
		MClient client = MClient.get(getCtx());
		MUser from = MUser.get (getCtx(), AD_User_ID);
		MUser to = MUser.get (getCtx(), newSalesRep_ID);
		EMail email = new EMail(client, from, to, subject, message);
		String msg = email.send();
		//
		setActionType(ACTIONTYPE_Transfer);
		setSalesRep_ID(newSalesRep_ID);
		setUpdatedBy (AD_User_ID);
		return processIt();
	}	//	action_transfer
	
	/**
	 *	Common Action
	 *	@return true if processed
	 */
	public boolean processIt()
	{
		MRequestAction ra = new MRequestAction(this);
		if (ra.save())
		{
			setDateLastAction(getUpdated());
			setLastResult(getResult());
			setDueType();
			//
			setResult(null);
			setMailText(null);
			setR_MailText_ID(0);
			setActionType(null);
			if (!save())
			{
				log.error("processIt - NOT saved");
				return false;
			}
		}
		else
		{
			log.error("processIt - Action NOT saved");
			return false;
		}
		return true;
	}	//	process it

	/**
	 * 	Set DueType based on Date Next Action
	 */
	public void setDueType()
	{
		Timestamp due = getDateNextAction();
		if (due == null)
			return;
		//
		Timestamp overdue = TimeUtil.addDays(due, getRequestType().getDueDateTolerance());
		Timestamp now = new Timestamp (System.currentTimeMillis());
		//
		String DueType = DUETYPE_Due;
		if (now.before(due))
			DueType = DUETYPE_Scheduled;
		else if (now.after(overdue))
			DueType = DUETYPE_Overdue;
		super.setDueType(DueType);
	}	//	setDueType

	
	/**
	 * 	Send Update EMail To SalesRep
	 */
	public boolean sendEMailToSalesRep()
	{
		String subject = "Request Updated " + getDocumentNo();
		MClient client = MClient.get(getCtx());
		MUser to = MUser.get (getCtx(), getSalesRep_ID());

		EMail email = new EMail(client, null, to, 
			subject, subject + "\n\n" + getResult());
		String msg = email.send();
		return EMail.SENT_OK.equals(msg);
	}	//	sendEMailToSalesRep

	
	/**************************************************************************
	 * 	Get Actions
	 *	@return array of actions
	 */
	public MRequestAction[] getActions()
	{
		String sql = "SELECT * FROM R_RequestAction "
			+ "WHERE R_Request_ID=? "
			+ "ORDER BY Created DESC";
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getR_Request_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRequestAction(getCtx(), rs));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("getActions", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		MRequestAction[] retValue = new MRequestAction[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getActions

	/**
	 *	Get Request Type String
	 *	@return Request Type String 	
	 */
	public MRequestType getRequestType()
	{
		if (m_requestType == null)
		{
			int R_RequestType_ID = getR_RequestType_ID();
			if (R_RequestType_ID == 0)
			{
				setR_RequestType_ID();
				R_RequestType_ID = getR_RequestType_ID();
			}
			m_requestType = MRequestType.get (getCtx(), R_RequestType_ID);
		}
		return m_requestType;
	}	//	getRequestType

	/**
	 * 	Get ActionType Text
	 *	@return text
	 */
	public String getActionTypeText()
	{
		return MRefList.getListName(getCtx(), ACTIONTYPE_AD_Reference_ID, getActionType());
	}	//	getActionTypeText

	/**
	 * 	Get DueType Text
	 *	@return text
	 */
	public String getDueTypeText()
	{
		return MRefList.getListName(getCtx(), DUETYPE_AD_Reference_ID, getDueType());
	}	//	getDueTypeText
	
	/**
	 * 	Set Date Last Alert to today
	 */
	public void setDateLastAlert ()
	{
		super.setDateLastAlert (new Timestamp(System.currentTimeMillis()));
	}	//	setDateLastAlert
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (getR_RequestType_ID() == 0)
			setR_RequestType_ID();		//	sets default
		
		//	Verify Action Type info
		if (getActionType() == null)
			;
		else if (ACTIONTYPE_Reminder.equals(getActionType()))
		{
			if (getDateNextAction() == null)
				setDateNextAction(TimeUtil.addDays(new Timestamp(System.currentTimeMillis()), 1));
		}
		
		//	Validate/Update Due Type
		setDueType();
		
		return true;
	}	//	beforeSave
	
	/**
	 * 	Get Sales Rep
	 *	@return Sales Rep User
	 */
	public MUser getSalesRep()
	{
		return MUser.get(getCtx(), getSalesRep_ID());
	}	//	getSalesRep
	
}	//	MRequest
