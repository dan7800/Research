/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wf;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.io.*;

import org.compiere.model.*;
import org.compiere.print.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 *	Workflow Activity Model.
 *	Controlled by WF Process: 
 *		set Node - startWork 
 *	
 *  @author Jorg Janke
 *  @version $Id: MWFActivity.java,v 1.21 2004/05/19 05:51:05 jjanke Exp $
 */
public class MWFActivity extends X_AD_WF_Activity implements Runnable
{
	/**
	 * 	Get Activities for table/tecord 
	 *	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@param activeOnly if true only not processed records are returned
	 *	@return activity
	 */
	public static MWFActivity[] get (Properties ctx, int AD_Table_ID, int Record_ID, boolean activeOnly)
	{
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_WF_Activity WHERE AD_Table_ID=? AND Record_ID=?";
		if (activeOnly)
			sql += " AND Processed<>'Y'";
		sql += " ORDER BY AD_WF_Activity_ID";
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, AD_Table_ID);
			pstmt.setInt (2, Record_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MWFActivity (ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("get", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MWFActivity[] retValue = new MWFActivity[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get Active Info
	 * 	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@return activity summary
	 */
	public static String getActiveInfo (Properties ctx, int AD_Table_ID, int Record_ID)
	{
		MWFActivity[] acts = get (ctx, AD_Table_ID, Record_ID, true);
		if (acts == null || acts.length == 0)
			return null;
		//
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < acts.length; i++)
		{
			if (i > 0)
				sb.append("\n");
			MWFActivity activity = acts[i];
			sb.append(activity.toStringX());
		}
		return sb.toString();
	}	//	getActivityInfo

	/**	Static Logger	*/
	private static Logger	s_log	= Logger.getCLogger (MWFActivity.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_WF_Activity_ID id
	 */
	public MWFActivity (Properties ctx, int AD_WF_Activity_ID)
	{
		super (ctx, AD_WF_Activity_ID);
		if (AD_WF_Activity_ID == 0)
			throw new IllegalArgumentException ("Cannot create new WF Activity directly");
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MWFActivity (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Parent Contructor
	 *	@param process process
	 *	@param AD_WF_Node_ID start node
	 */
	public MWFActivity (MWFProcess process, int AD_WF_Node_ID)
	{
		super (process.getCtx(), 0);
		setAD_WF_Process_ID (process.getAD_WF_Process_ID());
		//	Document Link
		setAD_Table_ID(process.getAD_Table_ID());
		setRecord_ID(process.getRecord_ID());
		//	Status
		super.setWFState(WFSTATE_NotStarted);
		m_state = new StateEngine (getWFState());
		setProcessed (false);
		//	Set Workflow Node
		setAD_Workflow_ID (process.getAD_Workflow_ID());
		setAD_WF_Node_ID (AD_WF_Node_ID);
		//	Responsible
		setResponsible(process);
		save();
		//
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		//
		m_process = process;
	}	//	MWFActivity

	/**	State Machine				*/
	private StateEngine			m_state = null;
	/**	Workflow Node				*/
	private MWFNode				m_node = null;
	/**	Audit						*/
	private MWFEventAudit		m_audit = null;
	/** Persistent Object			*/
	private PO					m_po = null;
	/**	New Value to save in audit	*/
	private String				m_newValue = null;
	/** Process						*/
	private MWFProcess 			m_process = null;
	
	
	/**************************************************************************
	 * 	Get State
	 *	@return state
	 */
	public StateEngine getState()
	{
		return m_state;
	}	//	getState

	/**
	 * 	Set Activity State
	 *	@param WFState
	 */
	public void setWFState (String WFState)
	{
		if (m_state == null)
			m_state = new StateEngine (getWFState());
		if (m_state.isClosed())
			return;
		if (getWFState().equals(WFState))
			return;
		//
		if (m_state.isValidNewState(WFState))
		{
			String oldState = getWFState();
			log.debug("setWFState - " + oldState + "->"+ WFState + ", Msg=" + getTextMsg()); 
			super.setWFState (WFState);
			m_state = new StateEngine (getWFState());
			save();			//	closed in MWFProcess.checkActivities()
			updateEventAudit();			
			
			//	Inform Process
			if (m_process == null)
				m_process = new MWFProcess (getCtx(), getAD_WF_Process_ID());
			m_process.checkActivities();
		}
		else
		{
			String msg = "Set WFState - Ignored Invalid Transformation - New=" 
				+ WFState + ", Current=" + getWFState(); 
			log.error(msg);
			Trace.printStack();
			setTextMsg(msg);
			save();
		}
	}	//	setWFState
	
	/**
	 * 	Is Activity closed
	 */
	public boolean isClosed()
	{
		return m_state.isClosed();
	}	//	isClosed
	
	
	/**************************************************************************
	 * 	Update Event Audit
	 */
	private void updateEventAudit()
	{
	//	log.debug("updateEventAudit");
		getEventAudit();
		m_audit.setTextMsg(getTextMsg());
		m_audit.setWFState(getWFState());
		if (m_newValue != null)
			m_audit.setNewValue(m_newValue);
		if (m_state.isClosed())
		{
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_ProcessCompleted);
			long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
			m_audit.setElapsedTimeMS(new BigDecimal(ms));
		}
		else
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		m_audit.save();
	}	//	updateEventAudit

	/**
	 * 	Get/Create Event Audit
	 * 	@return event
	 */
	public MWFEventAudit getEventAudit()
	{
		if (m_audit != null)
			return m_audit;
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID(), getAD_WF_Node_ID());
		if (events == null || events.length == 0)
			m_audit = new MWFEventAudit(this);
		else
			m_audit = events[events.length-1];		//	last event
		return m_audit;
	}	//	getEventAudit
	
	
	/**************************************************************************
	 * 	Get Persistent Object
	 *	@return po
	 */
	public PO getPO()
	{
		if (m_po != null)
			return m_po;
		
		M_Table table = M_Table.get (getCtx(), getAD_Table_ID());
		m_po = table.getPO(getRecord_ID());
		return m_po;
	}	//	getPO
	
	/**
	 * 	Get Attribute Value (based on Node) of PO
	 *	@return Attribute Value or null
	 */
	public Object getAttributeValue()
	{
		MWFNode node = getNode();
		if (node == null)
			return null;
		int AD_Column_ID = node.getAD_Column_ID();
		if (AD_Column_ID == 0)
			return null;
		PO po = getPO();
		if (po.getID() == 0)
			return null;
		return po.get_ValueOfColumn(AD_Column_ID);
	}	//	getAttributeValue
	
	/**
	 * 	Is SO Trx
	 *	@return SO Trx or of not found true
	 */
	public boolean isSOTrx()
	{
		PO po = getPO();
		if (po.getID() == 0)
			return true;
		//	Is there a Column?
		int index = po.get_ColumnIndex("IsSOTrx");
		if (index < 0)
		{
			if (po.get_TableName().startsWith("M_"))
				return false;
			return true;
		}
		//	we have a column
		try
		{
			Boolean IsSOTrx = (Boolean)po.get_Value(index);
			return IsSOTrx.booleanValue();
		}
		catch (Exception e)
		{
			log.error("isSOTrx", e);
		}
		return true;
	}	//	isSOTrx
	
	
	/**************************************************************************
	 * 	Set AD_WF_Node_ID.
	 * 	(Re)Set to Not Started
	 *	@param AD_WF_Node_ID now node
	 */
	public void setAD_WF_Node_ID (int AD_WF_Node_ID)
	{
		if (AD_WF_Node_ID == 0)
			throw new IllegalArgumentException("Workflow Node is not defined");
		super.setAD_WF_Node_ID (AD_WF_Node_ID);
		//
		if (!WFSTATE_NotStarted.equals(getWFState()))
		{
			super.setWFState(WFSTATE_NotStarted);
			m_state = new StateEngine (getWFState());
		}
		if (isProcessed())
			setProcessed (false);
		save();
	}	//	setAD_WF_Node_ID
	
	/**
	 * 	Get WF Node
	 *	@return node
	 */
	public MWFNode getNode()
	{
		if (m_node == null)
			m_node = MWFNode.get (getCtx(), getAD_WF_Node_ID());
		return m_node;
	}	//	getNode
	
	/**
	 * 	Get WF Node Name
	 *	@return translated node name
	 */
	public String getNodeName()
	{
		return getNode().getName(true);
	}	//	getNodeName

	/**
	 * 	Get Node Description
	 *	@return translated node description
	 */
	public String getNodeDescription()
	{
		return getNode().getDescription(true);
	}	//	getNodeDescription
	
	/**
	 * 	Get Node Help
	 *	@return translated node help
	 */
	public String getNodeHelp()
	{
		return getNode().getHelp(true);
	}	//	getNodeHelp
	
	
	/**
	 * 	Is this an user Approval step?
	 *	@return true if User Approval
	 */
	public boolean isUserApproval()
	{
		return getNode().isUserApproval();
	}	//	isNodeApproval

	/**
	 * 	Is this a Manual user step?
	 *	@return true if Window/Form/..
	 */
	public boolean isUserManual()
	{
		return getNode().isUserManual();
	}	//	isUserManual

	/**
	 * 	Is this a user choice step?
	 *	@return true if User Choice
	 */
	public boolean isUserChoice()
	{
		return getNode().isUserChoice();
	}	//	isUserChoice

	
	/**
	 * 	Set Text Msg (add to existing)
	 *	@param TextMsg
	 */
	public void setTextMsg (String TextMsg)
	{
		if (TextMsg == null || TextMsg.length() == 0)
			return;
		String oldText = getTextMsg();
		if (oldText == null || oldText.length() == 0)
			super.setTextMsg (TextMsg);
		else if (TextMsg != null && TextMsg.length() > 0)
			super.setTextMsg (oldText + "\n - " + TextMsg);
	}	//	setTextMsg	
	
	
	/**
	 * 	Get WF State text
	 *	@return state text
	 */
	public String getWFStateText ()
	{
		return MRefList.getListName(getCtx(), WFSTATE_AD_Reference_ID, getWFState());
	}	//	getWFStateText
	
	/**
	 * 	Set Responsible and User from Process / Node
	 *	@param process process
	 */
	private void setResponsible (MWFProcess process)
	{
		//	Responsible
		int AD_WF_Responsible_ID = getNode().getAD_WF_Responsible_ID();
		if (AD_WF_Responsible_ID == 0)	//	not defined on Node Level
			AD_WF_Responsible_ID = process.getAD_WF_Responsible_ID();
		setAD_WF_Responsible_ID (AD_WF_Responsible_ID);
		MWFResponsible resp = MWFResponsible.get(getCtx(), AD_WF_Responsible_ID);
		
		//	User - Directly responsible
		int AD_User_ID = resp.getAD_User_ID();
		//	Invoker - get Sales Rep or last updater of document
		if (AD_User_ID == 0 && resp.isInvoker())
			AD_User_ID = process.getAD_User_ID();
		//
		setAD_User_ID(AD_User_ID);
	}	//	setResponsible
	
	
	/**************************************************************************
	 * 	Is Invoker (no user & no role)
	 *	@return true if invoker
	 */
	public boolean isInvoker()
	{
		MWFResponsible resp = MWFResponsible.get(getCtx(), getAD_WF_Responsible_ID());
		return resp.isInvoker();
	}	//	isInvoker
	
	/**
	 * 	Get Approval User.
	 * 	If the returned user is the same, the document is approved.
	 *	@param AD_User_ID starting User
	 *	@param C_Currency_ID currency
	 *	@param amount amount
	 *	@param AD_Org_ID document organization
	 *	@param ownDocument the document is owned by AD_User_ID
	 *	@return AD_User_ID - if -1 no Approver
	 */
	public int getApprovalUser (int AD_User_ID, 
			int C_Currency_ID, BigDecimal amount, 
			int AD_Org_ID, boolean ownDocument)
	{
		//	Nothing to approve
		if (amount == null || amount.compareTo(Env.ZERO) == 0)
			return AD_User_ID;
		
		//	Starting user
		MUser user = MUser.get(getCtx(), AD_User_ID);
		log.info("getApprovalUser for " + user.getName() + ", Amt=" + amount + ", Own=" + ownDocument);

		MUser oldUser = null;
		while (user != null)
		{
			if (user.equals(oldUser))
			{
				log.info("getApprovalUser - Loop - = " + user.getName());
				return -1;
			}
			oldUser = user;
			log.debug("getApprovalUser = " + user.getName());
			//	Get Roles of User
			MRole[] roles = user.getRoles(AD_Org_ID);
			for (int i = 0; i < roles.length; i++)
			{
				MRole role = roles[i];
				if (ownDocument && !role.isCanApproveOwnDoc())
					continue;
				BigDecimal roleAmt = role.getAmtApproval();
				if (roleAmt == null || roleAmt.compareTo(Env.ZERO) == 0)
					continue;
				if (C_Currency_ID != role.getC_Currency_ID())	//	today & default rate
					roleAmt =  MConversionRate.convert(getCtx(), 
						roleAmt, role.getC_Currency_ID(), 
						C_Currency_ID, getAD_Client_ID(), AD_Org_ID);
				boolean approved = amount.compareTo(roleAmt) <= 0;
				log.debug("getApprovalUser - " + approved 
					+ " - User=" + user.getName() + ", Role=" + role.getName()
					+ ", ApprovalAmt=" + roleAmt);
				if (approved)
					return user.getAD_User_ID();
			}
			
			//	**** Find next User 
			//	Get Supervisor
			if (user.getSupervisor_ID() != 0)
			{
				user = MUser.get(getCtx(), user.getSupervisor_ID());
				log.debug("getApprovalUser - Supervisor: " + user.getName()); 
			}
			else
			{
				log.debug("getApprovalUser - No Supervisor"); 
				MOrg org = MOrg.get (getCtx(), AD_Org_ID);
				MOrgInfo orgInfo = org.getInfo();
				//	Get Org Supervisor
				if (orgInfo.getSupervisor_ID() != 0)
				{
					user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
					log.debug("getApprovalUser - Org=" + org.getName() + ",Supervisor: " + user.getName()); 
				}
				else
				{
					log.debug("getApprovalUser - No Org Supervisor"); 
					//	Get Parent Org Supervisor
					if (orgInfo.getParent_Org_ID() != 0)
					{
						org = MOrg.get (getCtx(), orgInfo.getParent_Org_ID());
						orgInfo = org.getInfo();
						if (orgInfo.getSupervisor_ID() != 0)
						{
							user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
							log.debug("getApprovalUser - Parent Org Supervisor: " + user.getName()); 
						}
					}
				}
			}	//	No Supervisor
			
		}	//	while there is a user to approve
		
		log.debug("getApprovalUser - No user found"); 
		return -1;
	}	//	getApproval

	
	/**************************************************************************
	 * 	Execute Work.
	 * 	Feedback to Process via setWFState -> checkActivities
	 */
	public void run()
	{
		log.info ("run - " + getNode());
		m_newValue = null;
		if (!m_state.isValidAction(StateEngine.ACTION_Start))
		{
			setTextMsg("State=" + getWFState() + " - cannot start");
			setWFState(StateEngine.STATE_Terminated);
			return;
		}
		//
		setWFState(StateEngine.STATE_Running);
		//
		try
		{
			if (getNode().getID() == 0)
			{
				setTextMsg("Node not found - AD_WF_Node_ID=" + getAD_WF_Node_ID());
				setWFState(StateEngine.STATE_Aborted);
				return;
			}
			//
			boolean done = performWork();
			setWFState (done ? StateEngine.STATE_Completed : StateEngine.STATE_Suspended);
		}
		catch (Exception e)
		{
			log.error("run", e);
			if (e.getCause() != null)
				log.error("run - cause", e.getCause());
			String msg = e.getLocalizedMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			setTextMsg(msg);
			setWFState (StateEngine.STATE_Terminated);
		}
	}	//	run
	
	
	/**
	 * 	Perform Work.
	 * 	Set Text Msg.
	 *	@return true if completed, false otherwise
	 *	@throws Exception if error
	 */
	private boolean performWork() throws Exception
	{
		log.debug ("performWork - " + m_node);
		String action = m_node.getAction();
		
		/******	Document Action				******/
		if (MWFNode.ACTION_DocumentAction.equals(action))
		{
			log.debug ("performWork - DocumentAction=" + m_node.getDocAction());
			getPO();
			if (m_po == null)
				throw new Exception("Persistent Object not found - AD_Table_ID=" 
					+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			boolean success = false;
			String processMsg = null;
			if (m_po instanceof DocAction)
			{
				DocAction doc = (DocAction)m_po;
				try
				{
					success = doc.processIt (m_node.getDocAction());	//	** Do the work
					setTextMsg(doc.getSummary());
					processMsg = doc.getProcessMsg();
				}
				catch (Exception e)
				{
					log.error ("performWork", e);
					processMsg = e.getLocalizedMessage();
					if (processMsg == null || processMsg.length() == 0)
						processMsg = e.toString(); 
					success = false;
					setTextMsg(processMsg);
					//	TODO rollback
				}
				if (m_process != null)
					m_process.setProcessMsg(processMsg);
			}
			else
				throw new IllegalStateException("Persistent Object not DocAction - "
					+ m_po.getClass().getName()
					+ " - AD_Table_ID=" + getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			//
			if (!m_po.save())
			{
				success = false;
				processMsg = "SaveError";
			}
			if (!success)
			{
				if (processMsg == null || processMsg.length() == 0)
					processMsg = "PerformWork Error - " + m_node.toStringX();
				throw new Exception(processMsg);
			}
			return success;
		}	//	DocumentAction
		
		/******	Report						******/
		else if (MWFNode.ACTION_AppsReport.equals(action))
		{
			log.debug ("performWork - Report:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			if (!process.isReport() || process.getAD_ReportView_ID() == 0)
				throw new IllegalStateException("Not a Report AD_Process_ID=" + m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			//	Report
			ReportEngine re = ReportEngine.get(getCtx(), pi);
			if (re == null)
				throw new IllegalStateException("Cannot create Report AD_Process_ID=" + m_node.getAD_Process_ID());
			File report = re.getPDF();
			//	Notice
			int AD_Message_ID = 753;		//	HARDCODED WorkflowResult
			MNote note = new MNote(getCtx(), AD_Message_ID, getAD_User_ID());
			note.setTextMsg(m_node.getName(true));
			note.setDescription(m_node.getDescription(true));
			note.setRecord(getAD_Table_ID(), getRecord_ID());
			note.save();
			//	Attachment
			MAttachment attachment = new MAttachment (getCtx(), MNote.Table_ID, note.getAD_Note_ID());
			attachment.addEntry(report);
			attachment.setTextMsg(m_node.getName(true));
			attachment.save();
			return true;
		}
		
		/******	Process						******/
		else if (MWFNode.ACTION_AppsProcess.equals(action))
		{
			log.debug ("performWork - Process:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			return process.processIt(pi);
		}
		
		else if (MWFNode.ACTION_AppsTask.equals(action))
		{
			log.debug ("performWork - Task:AD_Task_ID=" + m_node.getAD_Task_ID());
//	TODO start task
		}
		
		else if (MWFNode.ACTION_SetVariable.equals(action))
		{
			String value = m_node.getAttributeValue();
			log.debug ("performWork - SetVariable:AD_Column_ID=" + m_node.getAD_Column_ID()
				+ " to " +  value);
			M_Column column = m_node.getColumn();
			int dt = column.getAD_Reference_ID();
			return setVariable (value, dt, null);
		}	//	SetVariable
		
		else if (MWFNode.ACTION_SubWorkflow.equals(action))
		{
			log.debug ("performWork - Workflow:AD_Workflow_ID=" + m_node.getAD_Workflow_ID());
//	TODO start WF
		}
		
		else if (MWFNode.ACTION_UserChoice.equals(action))
		{
			log.debug ("performWork - UserChoice:AD_Column_ID=" + m_node.getAD_Column_ID());
			return false;
		}
		else if (MWFNode.ACTION_UserWorkbench.equals(action))
		{
			log.debug ("performWork - Workbench:?");
			return false;
		}
		
		else if (MWFNode.ACTION_UserForm.equals(action))
		{
			log.debug ("performWork - Form:AD_Form_ID=" + m_node.getAD_Form_ID());
			return false;
		}
		
		else if (MWFNode.ACTION_UserWindow.equals(action))
		{
			log.debug ("performWork - Window:AD_Window_ID=" + m_node.getAD_Window_ID());
			return false;
		}
		
		/**	Sleep (Start/End)			*/
		else if (MWFNode.ACTION_WaitSleep.equals(action))
		{
			log.debug ("performWork - Sleep:WaitTime=" + m_node.getWaitTime());
			if (m_node.getWaitingTime() == 0)
				return true;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, m_node.getWaitTime());
			setEndWaitTime(new Timestamp(cal.getTimeInMillis()));
			return false;
		}
		//
		throw new IllegalArgumentException("Invalid Action (Not Implemented) =" + action);
	}	//	performWork
	
	/**
	 * 	Set Variable
	 *	@param value new Value
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	private boolean setVariable(String value, int displayType, String textMsg) throws Exception
	{
		m_newValue = null;
		getPO();
		if (m_po == null)
			throw new Exception("Persistent Object not found - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
		//	Set Value
		Object dbValue = null;
		if (value == null)
			;
		else if (displayType == DisplayType.YesNo)
			dbValue = new Boolean("Y".equals(value));
		else if (DisplayType.isNumeric(displayType))
			dbValue = new BigDecimal (value);
		else
			dbValue = value;
		m_po.set_ValueOfColumn(getNode().getAD_Column_ID(), dbValue);
		m_po.save();
		if (!dbValue.equals(m_po.get_ValueOfColumn(getNode().getAD_Column_ID())))
			throw new Exception("Persistent Object not updated - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID() 
				+ " - Should=" + value + ", Is=" + m_po.get_ValueOfColumn(m_node.getAD_Column_ID()));
		//	Info
		String msg = getNode().getAttributeName() + "=" + value;
		if (textMsg != null && textMsg.length() > 0)
			msg += " - " + textMsg;
		setTextMsg (msg);
		m_newValue = value;
		return true;
	}	//	setVariable
	
	/**
	 * 	Set User Choice
	 *	@param value new Value
	 *	@param displayType display type
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	public boolean setUserChoice (int AD_User_ID, String value, int displayType, 
		String textMsg) throws Exception
	{
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		boolean ok = setVariable (value, displayType, textMsg);
		if (!ok)
			return false;

		String newState = StateEngine.STATE_Completed;
		//	Approval
		if (getNode().isUserApproval() && getPO() instanceof DocAction)
		{
			DocAction doc = (DocAction)m_po;
			try
			{
				//	Not pproved
				if (!"Y".equals(value))
				{
					newState = StateEngine.STATE_Aborted;
					if (!(doc.processIt (DocAction.ACTION_Reject)))
						setTextMsg ("Cannot Reject - Document Status: " + doc.getDocStatus());
				}
				else
				{
					if (isInvoker())
					{
						int startAD_User_ID = getAD_User_ID();
						if (startAD_User_ID == 0)
							startAD_User_ID = doc.getDoc_User_ID();
						int nextAD_User_ID = getApprovalUser(startAD_User_ID, 
							doc.getC_Currency_ID(), doc.getApprovalAmt(),
							doc.getAD_Org_ID(), 
							startAD_User_ID == doc.getDoc_User_ID());
						//	No Approver
						if (nextAD_User_ID <= 0)
						{
							newState = StateEngine.STATE_Aborted;
							setTextMsg ("Cannot Approve - No Approver");
							doc.processIt (DocAction.ACTION_Reject);
						}
						else if (startAD_User_ID != nextAD_User_ID)
						{
							forwardTo(nextAD_User_ID, "Next Approver");
							newState = StateEngine.STATE_Suspended;
						}
						else	//	Approve
						{
							if (!(doc.processIt (DocAction.ACTION_Approve)))
							{
								newState = StateEngine.STATE_Aborted;
								setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
							}		
						}
					}
					//	No Invoker - Approve
					else if (!(doc.processIt (DocAction.ACTION_Approve)))
					{
						newState = StateEngine.STATE_Aborted;
						setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
					}
				}
				doc.save();
			}
			catch (Exception e)
			{
				newState = StateEngine.STATE_Terminated;
				setTextMsg ("User Choice: " + e.toString());
				log.error("setUserChoice", e);
			}
		}	
		setWFState (newState);
		return ok;
	}	//	setUserChoice
	
	/**
	 * 	Forward To
	 *	@param AD_User_ID user
	 *	@param textMsg text message
	 *	@return true if forwarded
	 */
	public boolean forwardTo (int AD_User_ID, String textMsg)
	{
		if (AD_User_ID == getAD_User_ID())
		{
			log.error("forwardTo - Same User - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//
		MUser oldUser = MUser.get(getCtx(), getAD_User_ID());
		MUser user = MUser.get(getCtx(), AD_User_ID);
		if (user == null || user.getID() == 0)
		{
			log.error("forwardTo - Does not exist - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//	Update 
		setAD_User_ID (user.getAD_User_ID());
		setTextMsg(textMsg);
		save();
		//	Close up Old Event
		getEventAudit();
		m_audit.setAD_User_ID(oldUser.getAD_User_ID());
		m_audit.setTextMsg(getTextMsg());
		m_audit.setAttributeName("AD_User_ID");
		m_audit.setOldValue(oldUser.getName()+ "("+oldUser.getAD_User_ID()+")");
		m_audit.setNewValue(user.getName()+ "("+user.getAD_User_ID()+")");
		//
		m_audit.setWFState(getWFState());
		m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
		m_audit.setElapsedTimeMS(new BigDecimal(ms));
		m_audit.save();
		//	Create new one
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		return true;
	}	//	forwardTo

	/**
	 * 	Set User Confirmation
	 *	@param textMsg optional message
	 */
	public void setUserConfirmation (int AD_User_ID, String textMsg)
	{
		log.debug("setUserConfirmation - " + textMsg);
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		if (textMsg != null)
			setTextMsg (textMsg);
		setWFState (StateEngine.STATE_Completed);
	}	//	setUserConfirmation
	

	/**
	 * 	Fill Parameter
	 *	@param pInstance process instance
	 */
	private void fillParameter(MPInstance pInstance)
	{
		getPO();
		//
		MWFNodePara[] nParams = m_node.getParameters();
		MPInstance_Para[] iParams = pInstance.getParameters();
		for (int pi = 0; pi < iParams.length; pi++)
		{
			MPInstance_Para iPara = iParams[pi];
			for (int np = 0; np < nParams.length; np++)
			{
				MWFNodePara nPara = nParams[np];
				if (iPara.getParameterName().equals(nPara.getAttributeName()))
				{
					String variableName = nPara.getAttributeValue();
					log.debug("fillParameter - " + nPara.getAttributeName()
						+ " = " + variableName);
					//	Value - Constant/Variable
					Object value = variableName;
					if (variableName == null && variableName.length() == 0)
						value = null;
					else if (variableName.indexOf("@") != -1 && m_po != null)	//	we have a variable
					{
						//	Strip
						int index = variableName.indexOf("@");
						String columnName = variableName.substring(index);
						index = columnName.indexOf("@");
						if (index == -1)
						{
							log.warn("fillParameter - " + nPara.getAttributeName()
								+ " - cannot evaluate=" + variableName);
							break;
						}
						columnName = columnName.substring(0, index);
						index = m_po.get_ColumnIndex(columnName);
						if (index != -1)
						{
							value = m_po.get_Value(index);
						}
						else	//	not a column
						{
							//	try Env
							String env = Env.getContext(getCtx(), columnName);
							if (env.length() == 0)
							{
								log.warn("fillParameter - " + nPara.getAttributeName()
									+ " - not column nor environment =" + columnName 
									+ "(" + variableName + ")");
								break;
							}
							else
								value = env;
						}
					}	//	@variable@
					
					//	No Value
					if (value == null)
					{
						if (nPara.isMandatory())
							log.warn("fillParameter - " + nPara.getAttributeName() 
								+ " - empty - mandatory!");
						else
							log.debug("fillParameter - " + nPara.getAttributeName() 
								+ " - empty");
						break;
					}
					
					//	Convert to Type
					try
					{
						if (DisplayType.isNumeric(nPara.getDisplayType()) 
							|| DisplayType.isID(nPara.getDisplayType()))
						{
							BigDecimal bd = null;
							if (value instanceof BigDecimal)
								bd = (BigDecimal)value;
							else if (value instanceof Integer)
								bd = new BigDecimal (((Integer)value).intValue());
							else
								bd = new BigDecimal (value.toString());
							iPara.setP_Number(bd);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + bd + "=)");
						}
						else if (DisplayType.isDate(nPara.getDisplayType()))
						{
							Timestamp ts = null;
							if (value instanceof Timestamp)
								ts = (Timestamp)value;
							else
								ts = Timestamp.valueOf(value.toString());
							iPara.setP_Date(ts);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + ts + "=)");
						}
						else
						{
							iPara.setP_String(value.toString());
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName
								+ " (=" + value + "=) " + value.getClass().getName());
						}
					}
					catch (Exception e)
					{
						log.warn("fillParameter - " + nPara.getAttributeName()
							+ " = " + variableName + " (" + value
							+ ") " + value.getClass().getName()
							+ " - " + e.getLocalizedMessage());
					}
					break;
				}
			}	//	node parameter loop
		}	//	instance parameter loop
	}	//	fillParameter

	
	/**************************************************************************
	 * 	Get Process Activity (Event) History
	 *	@return history
	 */
	public String getHistoryHTML()
	{
		SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.DateTime);
		StringBuffer sb = new StringBuffer();
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID());
		for (int i = 0; i < events.length; i++)
		{
			MWFEventAudit audit = events[i];
		//	sb.append("<p style=\"width:400\">");
			sb.append("<p>");
			sb.append(format.format(audit.getCreated()))
				.append(" ")
				.append(getHTMLpart("b", audit.getNodeName()))
				.append(": ")
				.append(getHTMLpart(null, audit.getDescription()))
				.append(getHTMLpart("i", audit.getTextMsg()));
			sb.append("</p>");
		}
		return sb.toString();
	}	//	getHistory
	
	/**
	 * 	Get HTML part
	 *	@param tag HTML tag
	 *	@param content content
	 *	@return <tag>content</tag>
	 */
	private StringBuffer getHTMLpart (String tag, String content)
	{
		StringBuffer sb = new StringBuffer();
		if (content == null || content.length() == 0)
			return sb;
		if (tag != null && tag.length() > 0)
			sb.append("<").append(tag).append(">");
		sb.append(content);
		if (tag != null && tag.length() > 0)
			sb.append("</").append(tag).append(">");
		return sb;
	}	//	getHTMLpart
	
	
	/**************************************************************************
	 * 	Does the underlying PO (!) object have a PDF Attachment
	 * 	@return true if there is a pdf attachment
	 */
	public boolean isPdfAttachment()
	{
		if (getPO() == null)
			return false;
		return m_po.isPdfAttachment();
	}	//	isPDFAttachment

	/**
	 * 	Get PDF Attachment of underlying PO (!) object
	 *	@return pdf data or null
	 */
	public byte[] getPdfAttachment()
	{
		if (getPO() == null)
			return null;
		return m_po.getPdfAttachment();
	}	//	getPdfAttachment
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MWFActivity[");
		sb.append(getID()).append(",Node=");
		if (m_node == null)
			sb.append(getAD_WF_Node_ID());
		else
			sb.append(m_node.getName());
		sb.append(",State=").append(getWFState())
			.append(",AD_User_ID=").append(getAD_User_ID())
			.append(",").append(getCreated())
			.append ("]");
		return sb.toString ();
	} 	//	toString
	
	/**
	 * 	User String Representation.
	 * 	Suspended: Approve it (Joe)
	 *	@return info
	 */
	public String toStringX ()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getWFStateText())
			.append(": ").append(getNode().getName());
		if (getAD_User_ID() > 0)
		{
			MUser user = MUser.get(getCtx(), getAD_User_ID());
			sb.append(" (").append(user.getName()).append(")");
		}
		return sb.toString();
	}	//	toStringX
	
}	//	MWFActivity
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wf;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.io.*;

import org.compiere.model.*;
import org.compiere.print.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 *	Workflow Activity Model.
 *	Controlled by WF Process: 
 *		set Node - startWork 
 *	
 *  @author Jorg Janke
 *  @version $Id: MWFActivity.java,v 1.23 2004/09/04 06:16:09 jjanke Exp $
 */
public class MWFActivity extends X_AD_WF_Activity implements Runnable
{
	/**
	 * 	Get Activities for table/tecord 
	 *	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@param activeOnly if true only not processed records are returned
	 *	@return activity
	 */
	public static MWFActivity[] get (Properties ctx, int AD_Table_ID, int Record_ID, boolean activeOnly)
	{
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_WF_Activity WHERE AD_Table_ID=? AND Record_ID=?";
		if (activeOnly)
			sql += " AND Processed<>'Y'";
		sql += " ORDER BY AD_WF_Activity_ID";
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, AD_Table_ID);
			pstmt.setInt (2, Record_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MWFActivity (ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("get", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MWFActivity[] retValue = new MWFActivity[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get Active Info
	 * 	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@return activity summary
	 */
	public static String getActiveInfo (Properties ctx, int AD_Table_ID, int Record_ID)
	{
		MWFActivity[] acts = get (ctx, AD_Table_ID, Record_ID, true);
		if (acts == null || acts.length == 0)
			return null;
		//
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < acts.length; i++)
		{
			if (i > 0)
				sb.append("\n");
			MWFActivity activity = acts[i];
			sb.append(activity.toStringX());
		}
		return sb.toString();
	}	//	getActivityInfo

	/**	Static Logger	*/
	private static Logger	s_log	= Logger.getCLogger (MWFActivity.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_WF_Activity_ID id
	 */
	public MWFActivity (Properties ctx, int AD_WF_Activity_ID)
	{
		super (ctx, AD_WF_Activity_ID);
		if (AD_WF_Activity_ID == 0)
			throw new IllegalArgumentException ("Cannot create new WF Activity directly");
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MWFActivity (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Parent Contructor
	 *	@param process process
	 *	@param AD_WF_Node_ID start node
	 */
	public MWFActivity (MWFProcess process, int AD_WF_Node_ID)
	{
		super (process.getCtx(), 0);
		setAD_WF_Process_ID (process.getAD_WF_Process_ID());
		setPriority(process.getPriority());
		//	Document Link
		setAD_Table_ID(process.getAD_Table_ID());
		setRecord_ID(process.getRecord_ID());
		//	Status
		super.setWFState(WFSTATE_NotStarted);
		m_state = new StateEngine (getWFState());
		setProcessed (false);
		//	Set Workflow Node
		setAD_Workflow_ID (process.getAD_Workflow_ID());
		setAD_WF_Node_ID (AD_WF_Node_ID);
		//	Node Priority & End Duration
		MWFNode node = MWFNode.get(getCtx(), AD_WF_Node_ID);
		int priority = node.getPriority();
		if (priority != 0 && priority != getPriority())
			setPriority (priority);
		long limitMS = node.getLimitMS();
		if (limitMS != 0)
			setEndWaitTime(new Timestamp(limitMS + System.currentTimeMillis()));
		//	Responsible
		setResponsible(process);
		save();
		//
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		//
		m_process = process;
	}	//	MWFActivity

	/**	State Machine				*/
	private StateEngine			m_state = null;
	/**	Workflow Node				*/
	private MWFNode				m_node = null;
	/**	Audit						*/
	private MWFEventAudit		m_audit = null;
	/** Persistent Object			*/
	private PO					m_po = null;
	/**	New Value to save in audit	*/
	private String				m_newValue = null;
	/** Process						*/
	private MWFProcess 			m_process = null;
	
	
	/**************************************************************************
	 * 	Get State
	 *	@return state
	 */
	public StateEngine getState()
	{
		return m_state;
	}	//	getState

	/**
	 * 	Set Activity State
	 *	@param WFState
	 */
	public void setWFState (String WFState)
	{
		if (m_state == null)
			m_state = new StateEngine (getWFState());
		if (m_state.isClosed())
			return;
		if (getWFState().equals(WFState))
			return;
		//
		if (m_state.isValidNewState(WFState))
		{
			String oldState = getWFState();
			log.debug("setWFState - " + oldState + "->"+ WFState + ", Msg=" + getTextMsg()); 
			super.setWFState (WFState);
			m_state = new StateEngine (getWFState());
			save();			//	closed in MWFProcess.checkActivities()
			updateEventAudit();			
			
			//	Inform Process
			if (m_process == null)
				m_process = new MWFProcess (getCtx(), getAD_WF_Process_ID());
			m_process.checkActivities();
		}
		else
		{
			String msg = "Set WFState - Ignored Invalid Transformation - New=" 
				+ WFState + ", Current=" + getWFState(); 
			log.error(msg);
			Trace.printStack();
			setTextMsg(msg);
			save();
		}
	}	//	setWFState
	
	/**
	 * 	Is Activity closed
	 */
	public boolean isClosed()
	{
		return m_state.isClosed();
	}	//	isClosed
	
	
	/**************************************************************************
	 * 	Update Event Audit
	 */
	private void updateEventAudit()
	{
	//	log.debug("updateEventAudit");
		getEventAudit();
		m_audit.setTextMsg(getTextMsg());
		m_audit.setWFState(getWFState());
		if (m_newValue != null)
			m_audit.setNewValue(m_newValue);
		if (m_state.isClosed())
		{
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_ProcessCompleted);
			long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
			m_audit.setElapsedTimeMS(new BigDecimal(ms));
		}
		else
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		m_audit.save();
	}	//	updateEventAudit

	/**
	 * 	Get/Create Event Audit
	 * 	@return event
	 */
	public MWFEventAudit getEventAudit()
	{
		if (m_audit != null)
			return m_audit;
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID(), getAD_WF_Node_ID());
		if (events == null || events.length == 0)
			m_audit = new MWFEventAudit(this);
		else
			m_audit = events[events.length-1];		//	last event
		return m_audit;
	}	//	getEventAudit
	
	
	/**************************************************************************
	 * 	Get Persistent Object
	 *	@return po
	 */
	public PO getPO()
	{
		if (m_po != null)
			return m_po;
		
		M_Table table = M_Table.get (getCtx(), getAD_Table_ID());
		m_po = table.getPO(getRecord_ID());
		return m_po;
	}	//	getPO
	
	/**
	 * 	Get Attribute Value (based on Node) of PO
	 *	@return Attribute Value or null
	 */
	public Object getAttributeValue()
	{
		MWFNode node = getNode();
		if (node == null)
			return null;
		int AD_Column_ID = node.getAD_Column_ID();
		if (AD_Column_ID == 0)
			return null;
		PO po = getPO();
		if (po.getID() == 0)
			return null;
		return po.get_ValueOfColumn(AD_Column_ID);
	}	//	getAttributeValue
	
	/**
	 * 	Is SO Trx
	 *	@return SO Trx or of not found true
	 */
	public boolean isSOTrx()
	{
		PO po = getPO();
		if (po.getID() == 0)
			return true;
		//	Is there a Column?
		int index = po.get_ColumnIndex("IsSOTrx");
		if (index < 0)
		{
			if (po.get_TableName().startsWith("M_"))
				return false;
			return true;
		}
		//	we have a column
		try
		{
			Boolean IsSOTrx = (Boolean)po.get_Value(index);
			return IsSOTrx.booleanValue();
		}
		catch (Exception e)
		{
			log.error("isSOTrx", e);
		}
		return true;
	}	//	isSOTrx
	
	
	/**************************************************************************
	 * 	Set AD_WF_Node_ID.
	 * 	(Re)Set to Not Started
	 *	@param AD_WF_Node_ID now node
	 */
	public void setAD_WF_Node_ID (int AD_WF_Node_ID)
	{
		if (AD_WF_Node_ID == 0)
			throw new IllegalArgumentException("Workflow Node is not defined");
		super.setAD_WF_Node_ID (AD_WF_Node_ID);
		//
		if (!WFSTATE_NotStarted.equals(getWFState()))
		{
			super.setWFState(WFSTATE_NotStarted);
			m_state = new StateEngine (getWFState());
		}
		if (isProcessed())
			setProcessed (false);
	}	//	setAD_WF_Node_ID
	
	/**
	 * 	Get WF Node
	 *	@return node
	 */
	public MWFNode getNode()
	{
		if (m_node == null)
			m_node = MWFNode.get (getCtx(), getAD_WF_Node_ID());
		return m_node;
	}	//	getNode
	
	/**
	 * 	Get WF Node Name
	 *	@return translated node name
	 */
	public String getNodeName()
	{
		return getNode().getName(true);
	}	//	getNodeName

	/**
	 * 	Get Node Description
	 *	@return translated node description
	 */
	public String getNodeDescription()
	{
		return getNode().getDescription(true);
	}	//	getNodeDescription
	
	/**
	 * 	Get Node Help
	 *	@return translated node help
	 */
	public String getNodeHelp()
	{
		return getNode().getHelp(true);
	}	//	getNodeHelp
	
	
	/**
	 * 	Is this an user Approval step?
	 *	@return true if User Approval
	 */
	public boolean isUserApproval()
	{
		return getNode().isUserApproval();
	}	//	isNodeApproval

	/**
	 * 	Is this a Manual user step?
	 *	@return true if Window/Form/..
	 */
	public boolean isUserManual()
	{
		return getNode().isUserManual();
	}	//	isUserManual

	/**
	 * 	Is this a user choice step?
	 *	@return true if User Choice
	 */
	public boolean isUserChoice()
	{
		return getNode().isUserChoice();
	}	//	isUserChoice

	
	/**
	 * 	Set Text Msg (add to existing)
	 *	@param TextMsg
	 */
	public void setTextMsg (String TextMsg)
	{
		if (TextMsg == null || TextMsg.length() == 0)
			return;
		String oldText = getTextMsg();
		if (oldText == null || oldText.length() == 0)
			super.setTextMsg (TextMsg);
		else if (TextMsg != null && TextMsg.length() > 0)
			super.setTextMsg (oldText + "\n - " + TextMsg);
	}	//	setTextMsg	
	
	
	/**
	 * 	Get WF State text
	 *	@return state text
	 */
	public String getWFStateText ()
	{
		return MRefList.getListName(getCtx(), WFSTATE_AD_Reference_ID, getWFState());
	}	//	getWFStateText
	
	/**
	 * 	Set Responsible and User from Process / Node
	 *	@param process process
	 */
	private void setResponsible (MWFProcess process)
	{
		//	Responsible
		int AD_WF_Responsible_ID = getNode().getAD_WF_Responsible_ID();
		if (AD_WF_Responsible_ID == 0)	//	not defined on Node Level
			AD_WF_Responsible_ID = process.getAD_WF_Responsible_ID();
		setAD_WF_Responsible_ID (AD_WF_Responsible_ID);
		MWFResponsible resp = MWFResponsible.get(getCtx(), AD_WF_Responsible_ID);
		
		//	User - Directly responsible
		int AD_User_ID = resp.getAD_User_ID();
		//	Invoker - get Sales Rep or last updater of document
		if (AD_User_ID == 0 && resp.isInvoker())
			AD_User_ID = process.getAD_User_ID();
		//
		setAD_User_ID(AD_User_ID);
	}	//	setResponsible
	
	
	/**************************************************************************
	 * 	Is Invoker (no user & no role)
	 *	@return true if invoker
	 */
	public boolean isInvoker()
	{
		MWFResponsible resp = MWFResponsible.get(getCtx(), getAD_WF_Responsible_ID());
		return resp.isInvoker();
	}	//	isInvoker
	
	/**
	 * 	Get Approval User.
	 * 	If the returned user is the same, the document is approved.
	 *	@param AD_User_ID starting User
	 *	@param C_Currency_ID currency
	 *	@param amount amount
	 *	@param AD_Org_ID document organization
	 *	@param ownDocument the document is owned by AD_User_ID
	 *	@return AD_User_ID - if -1 no Approver
	 */
	public int getApprovalUser (int AD_User_ID, 
			int C_Currency_ID, BigDecimal amount, 
			int AD_Org_ID, boolean ownDocument)
	{
		//	Nothing to approve
		if (amount == null || amount.compareTo(Env.ZERO) == 0)
			return AD_User_ID;
		
		//	Starting user
		MUser user = MUser.get(getCtx(), AD_User_ID);
		log.info("getApprovalUser for " + user.getName() + ", Amt=" + amount + ", Own=" + ownDocument);

		MUser oldUser = null;
		while (user != null)
		{
			if (user.equals(oldUser))
			{
				log.info("getApprovalUser - Loop - = " + user.getName());
				return -1;
			}
			oldUser = user;
			log.debug("getApprovalUser = " + user.getName());
			//	Get Roles of User
			MRole[] roles = user.getRoles(AD_Org_ID);
			for (int i = 0; i < roles.length; i++)
			{
				MRole role = roles[i];
				if (ownDocument && !role.isCanApproveOwnDoc())
					continue;
				BigDecimal roleAmt = role.getAmtApproval();
				if (roleAmt == null || roleAmt.compareTo(Env.ZERO) == 0)
					continue;
				if (C_Currency_ID != role.getC_Currency_ID())	//	today & default rate
					roleAmt =  MConversionRate.convert(getCtx(), 
						roleAmt, role.getC_Currency_ID(), 
						C_Currency_ID, getAD_Client_ID(), AD_Org_ID);
				boolean approved = amount.compareTo(roleAmt) <= 0;
				log.debug("getApprovalUser - " + approved 
					+ " - User=" + user.getName() + ", Role=" + role.getName()
					+ ", ApprovalAmt=" + roleAmt);
				if (approved)
					return user.getAD_User_ID();
			}
			
			//	**** Find next User 
			//	Get Supervisor
			if (user.getSupervisor_ID() != 0)
			{
				user = MUser.get(getCtx(), user.getSupervisor_ID());
				log.debug("getApprovalUser - Supervisor: " + user.getName()); 
			}
			else
			{
				log.debug("getApprovalUser - No Supervisor"); 
				MOrg org = MOrg.get (getCtx(), AD_Org_ID);
				MOrgInfo orgInfo = org.getInfo();
				//	Get Org Supervisor
				if (orgInfo.getSupervisor_ID() != 0)
				{
					user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
					log.debug("getApprovalUser - Org=" + org.getName() + ",Supervisor: " + user.getName()); 
				}
				else
				{
					log.debug("getApprovalUser - No Org Supervisor"); 
					//	Get Parent Org Supervisor
					if (orgInfo.getParent_Org_ID() != 0)
					{
						org = MOrg.get (getCtx(), orgInfo.getParent_Org_ID());
						orgInfo = org.getInfo();
						if (orgInfo.getSupervisor_ID() != 0)
						{
							user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
							log.debug("getApprovalUser - Parent Org Supervisor: " + user.getName()); 
						}
					}
				}
			}	//	No Supervisor
			
		}	//	while there is a user to approve
		
		log.debug("getApprovalUser - No user found"); 
		return -1;
	}	//	getApproval

	
	/**************************************************************************
	 * 	Execute Work.
	 * 	Called from MWFProcess.startNext
	 * 	Feedback to Process via setWFState -> checkActivities
	 */
	public void run()
	{
		log.info ("run - " + getNode());
		m_newValue = null;
		if (!m_state.isValidAction(StateEngine.ACTION_Start))
		{
			setTextMsg("State=" + getWFState() + " - cannot start");
			setWFState(StateEngine.STATE_Terminated);
			return;
		}
		//
		setWFState(StateEngine.STATE_Running);
		//
		try
		{
			if (getNode().getID() == 0)
			{
				setTextMsg("Node not found - AD_WF_Node_ID=" + getAD_WF_Node_ID());
				setWFState(StateEngine.STATE_Aborted);
				return;
			}
			//	Do Work
			boolean done = performWork();
			setWFState (done ? StateEngine.STATE_Completed : StateEngine.STATE_Suspended);
		}
		catch (Exception e)
		{
			log.error("run", e);
			if (e.getCause() != null)
				log.error("run - cause", e.getCause());
			String msg = e.getLocalizedMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			setTextMsg(msg);
			setWFState (StateEngine.STATE_Terminated);
		}
	}	//	run
	
	
	/**
	 * 	Perform Work.
	 * 	Set Text Msg.
	 *	@return true if completed, false otherwise
	 *	@throws Exception if error
	 */
	private boolean performWork() throws Exception
	{
		log.debug ("performWork - " + m_node);
		if (m_node.getPriority() != 0)		//	overwrite priority if defined
			setPriority(m_node.getPriority());
		String action = m_node.getAction();
		
		/******	Document Action				******/
		if (MWFNode.ACTION_DocumentAction.equals(action))
		{
			log.debug ("performWork - DocumentAction=" + m_node.getDocAction());
			getPO();
			if (m_po == null)
				throw new Exception("Persistent Object not found - AD_Table_ID=" 
					+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			boolean success = false;
			String processMsg = null;
			if (m_po instanceof DocAction)
			{
				DocAction doc = (DocAction)m_po;
				try
				{
					success = doc.processIt (m_node.getDocAction());	//	** Do the work
					setTextMsg(doc.getSummary());
					processMsg = doc.getProcessMsg();
				}
				catch (Exception e)
				{
					log.error ("performWork", e);
					processMsg = e.getLocalizedMessage();
					if (processMsg == null || processMsg.length() == 0)
						processMsg = e.toString(); 
					success = false;
					setTextMsg(processMsg);
					//	TODO rollback
				}
				if (m_process != null)
					m_process.setProcessMsg(processMsg);
			}
			else
				throw new IllegalStateException("Persistent Object not DocAction - "
					+ m_po.getClass().getName()
					+ " - AD_Table_ID=" + getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			//
			if (!m_po.save())
			{
				success = false;
				processMsg = "SaveError";
			}
			if (!success)
			{
				if (processMsg == null || processMsg.length() == 0)
					processMsg = "PerformWork Error - " + m_node.toStringX();
				throw new Exception(processMsg);
			}
			return success;
		}	//	DocumentAction
		
		/******	Report						******/
		else if (MWFNode.ACTION_AppsReport.equals(action))
		{
			log.debug ("performWork - Report:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			if (!process.isReport() || process.getAD_ReportView_ID() == 0)
				throw new IllegalStateException("Not a Report AD_Process_ID=" + m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			//	Report
			ReportEngine re = ReportEngine.get(getCtx(), pi);
			if (re == null)
				throw new IllegalStateException("Cannot create Report AD_Process_ID=" + m_node.getAD_Process_ID());
			File report = re.getPDF();
			//	Notice
			int AD_Message_ID = 753;		//	HARDCODED WorkflowResult
			MNote note = new MNote(getCtx(), AD_Message_ID, getAD_User_ID());
			note.setTextMsg(m_node.getName(true));
			note.setDescription(m_node.getDescription(true));
			note.setRecord(getAD_Table_ID(), getRecord_ID());
			note.save();
			//	Attachment
			MAttachment attachment = new MAttachment (getCtx(), MNote.Table_ID, note.getAD_Note_ID());
			attachment.addEntry(report);
			attachment.setTextMsg(m_node.getName(true));
			attachment.save();
			return true;
		}
		
		/******	Process						******/
		else if (MWFNode.ACTION_AppsProcess.equals(action))
		{
			log.debug ("performWork - Process:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			return process.processIt(pi);
		}
		
		else if (MWFNode.ACTION_AppsTask.equals(action))
		{
			log.debug ("performWork - Task:AD_Task_ID=" + m_node.getAD_Task_ID());
//	TODO start task
		}
		
		else if (MWFNode.ACTION_SetVariable.equals(action))
		{
			String value = m_node.getAttributeValue();
			log.debug ("performWork - SetVariable:AD_Column_ID=" + m_node.getAD_Column_ID()
				+ " to " +  value);
			M_Column column = m_node.getColumn();
			int dt = column.getAD_Reference_ID();
			return setVariable (value, dt, null);
		}	//	SetVariable
		
		else if (MWFNode.ACTION_SubWorkflow.equals(action))
		{
			log.debug ("performWork - Workflow:AD_Workflow_ID=" + m_node.getAD_Workflow_ID());
//	TODO start WF
		}
		
		else if (MWFNode.ACTION_UserChoice.equals(action))
		{
			log.debug ("performWork - UserChoice:AD_Column_ID=" + m_node.getAD_Column_ID());
			return false;
		}
		else if (MWFNode.ACTION_UserWorkbench.equals(action))
		{
			log.debug ("performWork - Workbench:?");
			return false;
		}
		
		else if (MWFNode.ACTION_UserForm.equals(action))
		{
			log.debug ("performWork - Form:AD_Form_ID=" + m_node.getAD_Form_ID());
			return false;
		}
		
		else if (MWFNode.ACTION_UserWindow.equals(action))
		{
			log.debug ("performWork - Window:AD_Window_ID=" + m_node.getAD_Window_ID());
			return false;
		}
		
		/**	Sleep (Start/End)			*/
		else if (MWFNode.ACTION_WaitSleep.equals(action))
		{
			log.debug ("performWork - Sleep:WaitTime=" + m_node.getWaitTime());
			if (m_node.getWaitingTime() == 0)
				return true;
			Calendar cal = Calendar.getInstance();
			cal.add(m_node.getDurationCalendarField(), m_node.getWaitTime());
			setEndWaitTime(new Timestamp(cal.getTimeInMillis()));
			return false;	//	not done
		}
		//
		throw new IllegalArgumentException("Invalid Action (Not Implemented) =" + action);
	}	//	performWork
	
	/**
	 * 	Set Variable
	 *	@param value new Value
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	private boolean setVariable(String value, int displayType, String textMsg) throws Exception
	{
		m_newValue = null;
		getPO();
		if (m_po == null)
			throw new Exception("Persistent Object not found - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
		//	Set Value
		Object dbValue = null;
		if (value == null)
			;
		else if (displayType == DisplayType.YesNo)
			dbValue = new Boolean("Y".equals(value));
		else if (DisplayType.isNumeric(displayType))
			dbValue = new BigDecimal (value);
		else
			dbValue = value;
		m_po.set_ValueOfColumn(getNode().getAD_Column_ID(), dbValue);
		m_po.save();
		if (!dbValue.equals(m_po.get_ValueOfColumn(getNode().getAD_Column_ID())))
			throw new Exception("Persistent Object not updated - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID() 
				+ " - Should=" + value + ", Is=" + m_po.get_ValueOfColumn(m_node.getAD_Column_ID()));
		//	Info
		String msg = getNode().getAttributeName() + "=" + value;
		if (textMsg != null && textMsg.length() > 0)
			msg += " - " + textMsg;
		setTextMsg (msg);
		m_newValue = value;
		return true;
	}	//	setVariable
	
	/**
	 * 	Set User Choice
	 *	@param value new Value
	 *	@param displayType display type
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	public boolean setUserChoice (int AD_User_ID, String value, int displayType, 
		String textMsg) throws Exception
	{
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		boolean ok = setVariable (value, displayType, textMsg);
		if (!ok)
			return false;

		String newState = StateEngine.STATE_Completed;
		//	Approval
		if (getNode().isUserApproval() && getPO() instanceof DocAction)
		{
			DocAction doc = (DocAction)m_po;
			try
			{
				//	Not pproved
				if (!"Y".equals(value))
				{
					newState = StateEngine.STATE_Aborted;
					if (!(doc.processIt (DocAction.ACTION_Reject)))
						setTextMsg ("Cannot Reject - Document Status: " + doc.getDocStatus());
				}
				else
				{
					if (isInvoker())
					{
						int startAD_User_ID = getAD_User_ID();
						if (startAD_User_ID == 0)
							startAD_User_ID = doc.getDoc_User_ID();
						int nextAD_User_ID = getApprovalUser(startAD_User_ID, 
							doc.getC_Currency_ID(), doc.getApprovalAmt(),
							doc.getAD_Org_ID(), 
							startAD_User_ID == doc.getDoc_User_ID());
						//	No Approver
						if (nextAD_User_ID <= 0)
						{
							newState = StateEngine.STATE_Aborted;
							setTextMsg ("Cannot Approve - No Approver");
							doc.processIt (DocAction.ACTION_Reject);
						}
						else if (startAD_User_ID != nextAD_User_ID)
						{
							forwardTo(nextAD_User_ID, "Next Approver");
							newState = StateEngine.STATE_Suspended;
						}
						else	//	Approve
						{
							if (!(doc.processIt (DocAction.ACTION_Approve)))
							{
								newState = StateEngine.STATE_Aborted;
								setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
							}		
						}
					}
					//	No Invoker - Approve
					else if (!(doc.processIt (DocAction.ACTION_Approve)))
					{
						newState = StateEngine.STATE_Aborted;
						setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
					}
				}
				doc.save();
			}
			catch (Exception e)
			{
				newState = StateEngine.STATE_Terminated;
				setTextMsg ("User Choice: " + e.toString());
				log.error("setUserChoice", e);
			}
		}	
		setWFState (newState);
		return ok;
	}	//	setUserChoice
	
	/**
	 * 	Forward To
	 *	@param AD_User_ID user
	 *	@param textMsg text message
	 *	@return true if forwarded
	 */
	public boolean forwardTo (int AD_User_ID, String textMsg)
	{
		if (AD_User_ID == getAD_User_ID())
		{
			log.error("forwardTo - Same User - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//
		MUser oldUser = MUser.get(getCtx(), getAD_User_ID());
		MUser user = MUser.get(getCtx(), AD_User_ID);
		if (user == null || user.getID() == 0)
		{
			log.error("forwardTo - Does not exist - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//	Update 
		setAD_User_ID (user.getAD_User_ID());
		setTextMsg(textMsg);
		save();
		//	Close up Old Event
		getEventAudit();
		m_audit.setAD_User_ID(oldUser.getAD_User_ID());
		m_audit.setTextMsg(getTextMsg());
		m_audit.setAttributeName("AD_User_ID");
		m_audit.setOldValue(oldUser.getName()+ "("+oldUser.getAD_User_ID()+")");
		m_audit.setNewValue(user.getName()+ "("+user.getAD_User_ID()+")");
		//
		m_audit.setWFState(getWFState());
		m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
		m_audit.setElapsedTimeMS(new BigDecimal(ms));
		m_audit.save();
		//	Create new one
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		return true;
	}	//	forwardTo

	/**
	 * 	Set User Confirmation
	 *	@param textMsg optional message
	 */
	public void setUserConfirmation (int AD_User_ID, String textMsg)
	{
		log.debug("setUserConfirmation - " + textMsg);
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		if (textMsg != null)
			setTextMsg (textMsg);
		setWFState (StateEngine.STATE_Completed);
	}	//	setUserConfirmation
	

	/**
	 * 	Fill Parameter
	 *	@param pInstance process instance
	 */
	private void fillParameter(MPInstance pInstance)
	{
		getPO();
		//
		MWFNodePara[] nParams = m_node.getParameters();
		MPInstance_Para[] iParams = pInstance.getParameters();
		for (int pi = 0; pi < iParams.length; pi++)
		{
			MPInstance_Para iPara = iParams[pi];
			for (int np = 0; np < nParams.length; np++)
			{
				MWFNodePara nPara = nParams[np];
				if (iPara.getParameterName().equals(nPara.getAttributeName()))
				{
					String variableName = nPara.getAttributeValue();
					log.debug("fillParameter - " + nPara.getAttributeName()
						+ " = " + variableName);
					//	Value - Constant/Variable
					Object value = variableName;
					if (variableName == null && variableName.length() == 0)
						value = null;
					else if (variableName.indexOf("@") != -1 && m_po != null)	//	we have a variable
					{
						//	Strip
						int index = variableName.indexOf("@");
						String columnName = variableName.substring(index);
						index = columnName.indexOf("@");
						if (index == -1)
						{
							log.warn("fillParameter - " + nPara.getAttributeName()
								+ " - cannot evaluate=" + variableName);
							break;
						}
						columnName = columnName.substring(0, index);
						index = m_po.get_ColumnIndex(columnName);
						if (index != -1)
						{
							value = m_po.get_Value(index);
						}
						else	//	not a column
						{
							//	try Env
							String env = Env.getContext(getCtx(), columnName);
							if (env.length() == 0)
							{
								log.warn("fillParameter - " + nPara.getAttributeName()
									+ " - not column nor environment =" + columnName 
									+ "(" + variableName + ")");
								break;
							}
							else
								value = env;
						}
					}	//	@variable@
					
					//	No Value
					if (value == null)
					{
						if (nPara.isMandatory())
							log.warn("fillParameter - " + nPara.getAttributeName() 
								+ " - empty - mandatory!");
						else
							log.debug("fillParameter - " + nPara.getAttributeName() 
								+ " - empty");
						break;
					}
					
					//	Convert to Type
					try
					{
						if (DisplayType.isNumeric(nPara.getDisplayType()) 
							|| DisplayType.isID(nPara.getDisplayType()))
						{
							BigDecimal bd = null;
							if (value instanceof BigDecimal)
								bd = (BigDecimal)value;
							else if (value instanceof Integer)
								bd = new BigDecimal (((Integer)value).intValue());
							else
								bd = new BigDecimal (value.toString());
							iPara.setP_Number(bd);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + bd + "=)");
						}
						else if (DisplayType.isDate(nPara.getDisplayType()))
						{
							Timestamp ts = null;
							if (value instanceof Timestamp)
								ts = (Timestamp)value;
							else
								ts = Timestamp.valueOf(value.toString());
							iPara.setP_Date(ts);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + ts + "=)");
						}
						else
						{
							iPara.setP_String(value.toString());
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName
								+ " (=" + value + "=) " + value.getClass().getName());
						}
					}
					catch (Exception e)
					{
						log.warn("fillParameter - " + nPara.getAttributeName()
							+ " = " + variableName + " (" + value
							+ ") " + value.getClass().getName()
							+ " - " + e.getLocalizedMessage());
					}
					break;
				}
			}	//	node parameter loop
		}	//	instance parameter loop
	}	//	fillParameter

	
	/**************************************************************************
	 * 	Get Process Activity (Event) History
	 *	@return history
	 */
	public String getHistoryHTML()
	{
		SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.DateTime);
		StringBuffer sb = new StringBuffer();
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID());
		for (int i = 0; i < events.length; i++)
		{
			MWFEventAudit audit = events[i];
		//	sb.append("<p style=\"width:400\">");
			sb.append("<p>");
			sb.append(format.format(audit.getCreated()))
				.append(" ")
				.append(getHTMLpart("b", audit.getNodeName()))
				.append(": ")
				.append(getHTMLpart(null, audit.getDescription()))
				.append(getHTMLpart("i", audit.getTextMsg()));
			sb.append("</p>");
		}
		return sb.toString();
	}	//	getHistory
	
	/**
	 * 	Get HTML part
	 *	@param tag HTML tag
	 *	@param content content
	 *	@return <tag>content</tag>
	 */
	private StringBuffer getHTMLpart (String tag, String content)
	{
		StringBuffer sb = new StringBuffer();
		if (content == null || content.length() == 0)
			return sb;
		if (tag != null && tag.length() > 0)
			sb.append("<").append(tag).append(">");
		sb.append(content);
		if (tag != null && tag.length() > 0)
			sb.append("</").append(tag).append(">");
		return sb;
	}	//	getHTMLpart
	
	
	/**************************************************************************
	 * 	Does the underlying PO (!) object have a PDF Attachment
	 * 	@return true if there is a pdf attachment
	 */
	public boolean isPdfAttachment()
	{
		if (getPO() == null)
			return false;
		return m_po.isPdfAttachment();
	}	//	isPDFAttachment

	/**
	 * 	Get PDF Attachment of underlying PO (!) object
	 *	@return pdf data or null
	 */
	public byte[] getPdfAttachment()
	{
		if (getPO() == null)
			return null;
		return m_po.getPdfAttachment();
	}	//	getPdfAttachment
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MWFActivity[");
		sb.append(getID()).append(",Node=");
		if (m_node == null)
			sb.append(getAD_WF_Node_ID());
		else
			sb.append(m_node.getName());
		sb.append(",State=").append(getWFState())
			.append(",AD_User_ID=").append(getAD_User_ID())
			.append(",").append(getCreated())
			.append ("]");
		return sb.toString ();
	} 	//	toString
	
	/**
	 * 	User String Representation.
	 * 	Suspended: Approve it (Joe)
	 *	@return info
	 */
	public String toStringX ()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getWFStateText())
			.append(": ").append(getNode().getName());
		if (getAD_User_ID() > 0)
		{
			MUser user = MUser.get(getCtx(), getAD_User_ID());
			sb.append(" (").append(user.getName()).append(")");
		}
		return sb.toString();
	}	//	toStringX
	
}	//	MWFActivity
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wf;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.io.*;

import org.compiere.model.*;
import org.compiere.print.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 *	Workflow Activity Model.
 *	Controlled by WF Process: 
 *		set Node - startWork 
 *	
 *  @author Jorg Janke
 *  @version $Id: MWFActivity.java,v 1.21 2004/05/19 05:51:05 jjanke Exp $
 */
public class MWFActivity extends X_AD_WF_Activity implements Runnable
{
	/**
	 * 	Get Activities for table/tecord 
	 *	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@param activeOnly if true only not processed records are returned
	 *	@return activity
	 */
	public static MWFActivity[] get (Properties ctx, int AD_Table_ID, int Record_ID, boolean activeOnly)
	{
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_WF_Activity WHERE AD_Table_ID=? AND Record_ID=?";
		if (activeOnly)
			sql += " AND Processed<>'Y'";
		sql += " ORDER BY AD_WF_Activity_ID";
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, AD_Table_ID);
			pstmt.setInt (2, Record_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MWFActivity (ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("get", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MWFActivity[] retValue = new MWFActivity[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get Active Info
	 * 	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@return activity summary
	 */
	public static String getActiveInfo (Properties ctx, int AD_Table_ID, int Record_ID)
	{
		MWFActivity[] acts = get (ctx, AD_Table_ID, Record_ID, true);
		if (acts == null || acts.length == 0)
			return null;
		//
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < acts.length; i++)
		{
			if (i > 0)
				sb.append("\n");
			MWFActivity activity = acts[i];
			sb.append(activity.toStringX());
		}
		return sb.toString();
	}	//	getActivityInfo

	/**	Static Logger	*/
	private static Logger	s_log	= Logger.getCLogger (MWFActivity.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_WF_Activity_ID id
	 */
	public MWFActivity (Properties ctx, int AD_WF_Activity_ID)
	{
		super (ctx, AD_WF_Activity_ID);
		if (AD_WF_Activity_ID == 0)
			throw new IllegalArgumentException ("Cannot create new WF Activity directly");
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MWFActivity (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Parent Contructor
	 *	@param process process
	 *	@param AD_WF_Node_ID start node
	 */
	public MWFActivity (MWFProcess process, int AD_WF_Node_ID)
	{
		super (process.getCtx(), 0);
		setAD_WF_Process_ID (process.getAD_WF_Process_ID());
		//	Document Link
		setAD_Table_ID(process.getAD_Table_ID());
		setRecord_ID(process.getRecord_ID());
		//	Status
		super.setWFState(WFSTATE_NotStarted);
		m_state = new StateEngine (getWFState());
		setProcessed (false);
		//	Set Workflow Node
		setAD_Workflow_ID (process.getAD_Workflow_ID());
		setAD_WF_Node_ID (AD_WF_Node_ID);
		//	Responsible
		setResponsible(process);
		save();
		//
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		//
		m_process = process;
	}	//	MWFActivity

	/**	State Machine				*/
	private StateEngine			m_state = null;
	/**	Workflow Node				*/
	private MWFNode				m_node = null;
	/**	Audit						*/
	private MWFEventAudit		m_audit = null;
	/** Persistent Object			*/
	private PO					m_po = null;
	/**	New Value to save in audit	*/
	private String				m_newValue = null;
	/** Process						*/
	private MWFProcess 			m_process = null;
	
	
	/**************************************************************************
	 * 	Get State
	 *	@return state
	 */
	public StateEngine getState()
	{
		return m_state;
	}	//	getState

	/**
	 * 	Set Activity State
	 *	@param WFState
	 */
	public void setWFState (String WFState)
	{
		if (m_state == null)
			m_state = new StateEngine (getWFState());
		if (m_state.isClosed())
			return;
		if (getWFState().equals(WFState))
			return;
		//
		if (m_state.isValidNewState(WFState))
		{
			String oldState = getWFState();
			log.debug("setWFState - " + oldState + "->"+ WFState + ", Msg=" + getTextMsg()); 
			super.setWFState (WFState);
			m_state = new StateEngine (getWFState());
			save();			//	closed in MWFProcess.checkActivities()
			updateEventAudit();			
			
			//	Inform Process
			if (m_process == null)
				m_process = new MWFProcess (getCtx(), getAD_WF_Process_ID());
			m_process.checkActivities();
		}
		else
		{
			String msg = "Set WFState - Ignored Invalid Transformation - New=" 
				+ WFState + ", Current=" + getWFState(); 
			log.error(msg);
			Trace.printStack();
			setTextMsg(msg);
			save();
		}
	}	//	setWFState
	
	/**
	 * 	Is Activity closed
	 */
	public boolean isClosed()
	{
		return m_state.isClosed();
	}	//	isClosed
	
	
	/**************************************************************************
	 * 	Update Event Audit
	 */
	private void updateEventAudit()
	{
	//	log.debug("updateEventAudit");
		getEventAudit();
		m_audit.setTextMsg(getTextMsg());
		m_audit.setWFState(getWFState());
		if (m_newValue != null)
			m_audit.setNewValue(m_newValue);
		if (m_state.isClosed())
		{
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_ProcessCompleted);
			long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
			m_audit.setElapsedTimeMS(new BigDecimal(ms));
		}
		else
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		m_audit.save();
	}	//	updateEventAudit

	/**
	 * 	Get/Create Event Audit
	 * 	@return event
	 */
	public MWFEventAudit getEventAudit()
	{
		if (m_audit != null)
			return m_audit;
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID(), getAD_WF_Node_ID());
		if (events == null || events.length == 0)
			m_audit = new MWFEventAudit(this);
		else
			m_audit = events[events.length-1];		//	last event
		return m_audit;
	}	//	getEventAudit
	
	
	/**************************************************************************
	 * 	Get Persistent Object
	 *	@return po
	 */
	public PO getPO()
	{
		if (m_po != null)
			return m_po;
		
		M_Table table = M_Table.get (getCtx(), getAD_Table_ID());
		m_po = table.getPO(getRecord_ID());
		return m_po;
	}	//	getPO
	
	/**
	 * 	Get Attribute Value (based on Node) of PO
	 *	@return Attribute Value or null
	 */
	public Object getAttributeValue()
	{
		MWFNode node = getNode();
		if (node == null)
			return null;
		int AD_Column_ID = node.getAD_Column_ID();
		if (AD_Column_ID == 0)
			return null;
		PO po = getPO();
		if (po.getID() == 0)
			return null;
		return po.get_ValueOfColumn(AD_Column_ID);
	}	//	getAttributeValue
	
	/**
	 * 	Is SO Trx
	 *	@return SO Trx or of not found true
	 */
	public boolean isSOTrx()
	{
		PO po = getPO();
		if (po.getID() == 0)
			return true;
		//	Is there a Column?
		int index = po.get_ColumnIndex("IsSOTrx");
		if (index < 0)
		{
			if (po.get_TableName().startsWith("M_"))
				return false;
			return true;
		}
		//	we have a column
		try
		{
			Boolean IsSOTrx = (Boolean)po.get_Value(index);
			return IsSOTrx.booleanValue();
		}
		catch (Exception e)
		{
			log.error("isSOTrx", e);
		}
		return true;
	}	//	isSOTrx
	
	
	/**************************************************************************
	 * 	Set AD_WF_Node_ID.
	 * 	(Re)Set to Not Started
	 *	@param AD_WF_Node_ID now node
	 */
	public void setAD_WF_Node_ID (int AD_WF_Node_ID)
	{
		if (AD_WF_Node_ID == 0)
			throw new IllegalArgumentException("Workflow Node is not defined");
		super.setAD_WF_Node_ID (AD_WF_Node_ID);
		//
		if (!WFSTATE_NotStarted.equals(getWFState()))
		{
			super.setWFState(WFSTATE_NotStarted);
			m_state = new StateEngine (getWFState());
		}
		if (isProcessed())
			setProcessed (false);
		save();
	}	//	setAD_WF_Node_ID
	
	/**
	 * 	Get WF Node
	 *	@return node
	 */
	public MWFNode getNode()
	{
		if (m_node == null)
			m_node = MWFNode.get (getCtx(), getAD_WF_Node_ID());
		return m_node;
	}	//	getNode
	
	/**
	 * 	Get WF Node Name
	 *	@return translated node name
	 */
	public String getNodeName()
	{
		return getNode().getName(true);
	}	//	getNodeName

	/**
	 * 	Get Node Description
	 *	@return translated node description
	 */
	public String getNodeDescription()
	{
		return getNode().getDescription(true);
	}	//	getNodeDescription
	
	/**
	 * 	Get Node Help
	 *	@return translated node help
	 */
	public String getNodeHelp()
	{
		return getNode().getHelp(true);
	}	//	getNodeHelp
	
	
	/**
	 * 	Is this an user Approval step?
	 *	@return true if User Approval
	 */
	public boolean isUserApproval()
	{
		return getNode().isUserApproval();
	}	//	isNodeApproval

	/**
	 * 	Is this a Manual user step?
	 *	@return true if Window/Form/..
	 */
	public boolean isUserManual()
	{
		return getNode().isUserManual();
	}	//	isUserManual

	/**
	 * 	Is this a user choice step?
	 *	@return true if User Choice
	 */
	public boolean isUserChoice()
	{
		return getNode().isUserChoice();
	}	//	isUserChoice

	
	/**
	 * 	Set Text Msg (add to existing)
	 *	@param TextMsg
	 */
	public void setTextMsg (String TextMsg)
	{
		if (TextMsg == null || TextMsg.length() == 0)
			return;
		String oldText = getTextMsg();
		if (oldText == null || oldText.length() == 0)
			super.setTextMsg (TextMsg);
		else if (TextMsg != null && TextMsg.length() > 0)
			super.setTextMsg (oldText + "\n - " + TextMsg);
	}	//	setTextMsg	
	
	
	/**
	 * 	Get WF State text
	 *	@return state text
	 */
	public String getWFStateText ()
	{
		return MRefList.getListName(getCtx(), WFSTATE_AD_Reference_ID, getWFState());
	}	//	getWFStateText
	
	/**
	 * 	Set Responsible and User from Process / Node
	 *	@param process process
	 */
	private void setResponsible (MWFProcess process)
	{
		//	Responsible
		int AD_WF_Responsible_ID = getNode().getAD_WF_Responsible_ID();
		if (AD_WF_Responsible_ID == 0)	//	not defined on Node Level
			AD_WF_Responsible_ID = process.getAD_WF_Responsible_ID();
		setAD_WF_Responsible_ID (AD_WF_Responsible_ID);
		MWFResponsible resp = MWFResponsible.get(getCtx(), AD_WF_Responsible_ID);
		
		//	User - Directly responsible
		int AD_User_ID = resp.getAD_User_ID();
		//	Invoker - get Sales Rep or last updater of document
		if (AD_User_ID == 0 && resp.isInvoker())
			AD_User_ID = process.getAD_User_ID();
		//
		setAD_User_ID(AD_User_ID);
	}	//	setResponsible
	
	
	/**************************************************************************
	 * 	Is Invoker (no user & no role)
	 *	@return true if invoker
	 */
	public boolean isInvoker()
	{
		MWFResponsible resp = MWFResponsible.get(getCtx(), getAD_WF_Responsible_ID());
		return resp.isInvoker();
	}	//	isInvoker
	
	/**
	 * 	Get Approval User.
	 * 	If the returned user is the same, the document is approved.
	 *	@param AD_User_ID starting User
	 *	@param C_Currency_ID currency
	 *	@param amount amount
	 *	@param AD_Org_ID document organization
	 *	@param ownDocument the document is owned by AD_User_ID
	 *	@return AD_User_ID - if -1 no Approver
	 */
	public int getApprovalUser (int AD_User_ID, 
			int C_Currency_ID, BigDecimal amount, 
			int AD_Org_ID, boolean ownDocument)
	{
		//	Nothing to approve
		if (amount == null || amount.compareTo(Env.ZERO) == 0)
			return AD_User_ID;
		
		//	Starting user
		MUser user = MUser.get(getCtx(), AD_User_ID);
		log.info("getApprovalUser for " + user.getName() + ", Amt=" + amount + ", Own=" + ownDocument);

		MUser oldUser = null;
		while (user != null)
		{
			if (user.equals(oldUser))
			{
				log.info("getApprovalUser - Loop - = " + user.getName());
				return -1;
			}
			oldUser = user;
			log.debug("getApprovalUser = " + user.getName());
			//	Get Roles of User
			MRole[] roles = user.getRoles(AD_Org_ID);
			for (int i = 0; i < roles.length; i++)
			{
				MRole role = roles[i];
				if (ownDocument && !role.isCanApproveOwnDoc())
					continue;
				BigDecimal roleAmt = role.getAmtApproval();
				if (roleAmt == null || roleAmt.compareTo(Env.ZERO) == 0)
					continue;
				if (C_Currency_ID != role.getC_Currency_ID())	//	today & default rate
					roleAmt =  MConversionRate.convert(getCtx(), 
						roleAmt, role.getC_Currency_ID(), 
						C_Currency_ID, getAD_Client_ID(), AD_Org_ID);
				boolean approved = amount.compareTo(roleAmt) <= 0;
				log.debug("getApprovalUser - " + approved 
					+ " - User=" + user.getName() + ", Role=" + role.getName()
					+ ", ApprovalAmt=" + roleAmt);
				if (approved)
					return user.getAD_User_ID();
			}
			
			//	**** Find next User 
			//	Get Supervisor
			if (user.getSupervisor_ID() != 0)
			{
				user = MUser.get(getCtx(), user.getSupervisor_ID());
				log.debug("getApprovalUser - Supervisor: " + user.getName()); 
			}
			else
			{
				log.debug("getApprovalUser - No Supervisor"); 
				MOrg org = MOrg.get (getCtx(), AD_Org_ID);
				MOrgInfo orgInfo = org.getInfo();
				//	Get Org Supervisor
				if (orgInfo.getSupervisor_ID() != 0)
				{
					user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
					log.debug("getApprovalUser - Org=" + org.getName() + ",Supervisor: " + user.getName()); 
				}
				else
				{
					log.debug("getApprovalUser - No Org Supervisor"); 
					//	Get Parent Org Supervisor
					if (orgInfo.getParent_Org_ID() != 0)
					{
						org = MOrg.get (getCtx(), orgInfo.getParent_Org_ID());
						orgInfo = org.getInfo();
						if (orgInfo.getSupervisor_ID() != 0)
						{
							user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
							log.debug("getApprovalUser - Parent Org Supervisor: " + user.getName()); 
						}
					}
				}
			}	//	No Supervisor
			
		}	//	while there is a user to approve
		
		log.debug("getApprovalUser - No user found"); 
		return -1;
	}	//	getApproval

	
	/**************************************************************************
	 * 	Execute Work.
	 * 	Feedback to Process via setWFState -> checkActivities
	 */
	public void run()
	{
		log.info ("run - " + getNode());
		m_newValue = null;
		if (!m_state.isValidAction(StateEngine.ACTION_Start))
		{
			setTextMsg("State=" + getWFState() + " - cannot start");
			setWFState(StateEngine.STATE_Terminated);
			return;
		}
		//
		setWFState(StateEngine.STATE_Running);
		//
		try
		{
			if (getNode().getID() == 0)
			{
				setTextMsg("Node not found - AD_WF_Node_ID=" + getAD_WF_Node_ID());
				setWFState(StateEngine.STATE_Aborted);
				return;
			}
			//
			boolean done = performWork();
			setWFState (done ? StateEngine.STATE_Completed : StateEngine.STATE_Suspended);
		}
		catch (Exception e)
		{
			log.error("run", e);
			if (e.getCause() != null)
				log.error("run - cause", e.getCause());
			String msg = e.getLocalizedMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			setTextMsg(msg);
			setWFState (StateEngine.STATE_Terminated);
		}
	}	//	run
	
	
	/**
	 * 	Perform Work.
	 * 	Set Text Msg.
	 *	@return true if completed, false otherwise
	 *	@throws Exception if error
	 */
	private boolean performWork() throws Exception
	{
		log.debug ("performWork - " + m_node);
		String action = m_node.getAction();
		
		/******	Document Action				******/
		if (MWFNode.ACTION_DocumentAction.equals(action))
		{
			log.debug ("performWork - DocumentAction=" + m_node.getDocAction());
			getPO();
			if (m_po == null)
				throw new Exception("Persistent Object not found - AD_Table_ID=" 
					+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			boolean success = false;
			String processMsg = null;
			if (m_po instanceof DocAction)
			{
				DocAction doc = (DocAction)m_po;
				try
				{
					success = doc.processIt (m_node.getDocAction());	//	** Do the work
					setTextMsg(doc.getSummary());
					processMsg = doc.getProcessMsg();
				}
				catch (Exception e)
				{
					log.error ("performWork", e);
					processMsg = e.getLocalizedMessage();
					if (processMsg == null || processMsg.length() == 0)
						processMsg = e.toString(); 
					success = false;
					setTextMsg(processMsg);
					//	TODO rollback
				}
				if (m_process != null)
					m_process.setProcessMsg(processMsg);
			}
			else
				throw new IllegalStateException("Persistent Object not DocAction - "
					+ m_po.getClass().getName()
					+ " - AD_Table_ID=" + getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			//
			if (!m_po.save())
			{
				success = false;
				processMsg = "SaveError";
			}
			if (!success)
			{
				if (processMsg == null || processMsg.length() == 0)
					processMsg = "PerformWork Error - " + m_node.toStringX();
				throw new Exception(processMsg);
			}
			return success;
		}	//	DocumentAction
		
		/******	Report						******/
		else if (MWFNode.ACTION_AppsReport.equals(action))
		{
			log.debug ("performWork - Report:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			if (!process.isReport() || process.getAD_ReportView_ID() == 0)
				throw new IllegalStateException("Not a Report AD_Process_ID=" + m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			//	Report
			ReportEngine re = ReportEngine.get(getCtx(), pi);
			if (re == null)
				throw new IllegalStateException("Cannot create Report AD_Process_ID=" + m_node.getAD_Process_ID());
			File report = re.getPDF();
			//	Notice
			int AD_Message_ID = 753;		//	HARDCODED WorkflowResult
			MNote note = new MNote(getCtx(), AD_Message_ID, getAD_User_ID());
			note.setTextMsg(m_node.getName(true));
			note.setDescription(m_node.getDescription(true));
			note.setRecord(getAD_Table_ID(), getRecord_ID());
			note.save();
			//	Attachment
			MAttachment attachment = new MAttachment (getCtx(), MNote.Table_ID, note.getAD_Note_ID());
			attachment.addEntry(report);
			attachment.setTextMsg(m_node.getName(true));
			attachment.save();
			return true;
		}
		
		/******	Process						******/
		else if (MWFNode.ACTION_AppsProcess.equals(action))
		{
			log.debug ("performWork - Process:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			return process.processIt(pi);
		}
		
		else if (MWFNode.ACTION_AppsTask.equals(action))
		{
			log.debug ("performWork - Task:AD_Task_ID=" + m_node.getAD_Task_ID());
//	TODO start task
		}
		
		else if (MWFNode.ACTION_SetVariable.equals(action))
		{
			String value = m_node.getAttributeValue();
			log.debug ("performWork - SetVariable:AD_Column_ID=" + m_node.getAD_Column_ID()
				+ " to " +  value);
			M_Column column = m_node.getColumn();
			int dt = column.getAD_Reference_ID();
			return setVariable (value, dt, null);
		}	//	SetVariable
		
		else if (MWFNode.ACTION_SubWorkflow.equals(action))
		{
			log.debug ("performWork - Workflow:AD_Workflow_ID=" + m_node.getAD_Workflow_ID());
//	TODO start WF
		}
		
		else if (MWFNode.ACTION_UserChoice.equals(action))
		{
			log.debug ("performWork - UserChoice:AD_Column_ID=" + m_node.getAD_Column_ID());
			return false;
		}
		else if (MWFNode.ACTION_UserWorkbench.equals(action))
		{
			log.debug ("performWork - Workbench:?");
			return false;
		}
		
		else if (MWFNode.ACTION_UserForm.equals(action))
		{
			log.debug ("performWork - Form:AD_Form_ID=" + m_node.getAD_Form_ID());
			return false;
		}
		
		else if (MWFNode.ACTION_UserWindow.equals(action))
		{
			log.debug ("performWork - Window:AD_Window_ID=" + m_node.getAD_Window_ID());
			return false;
		}
		
		/**	Sleep (Start/End)			*/
		else if (MWFNode.ACTION_WaitSleep.equals(action))
		{
			log.debug ("performWork - Sleep:WaitTime=" + m_node.getWaitTime());
			if (m_node.getWaitingTime() == 0)
				return true;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, m_node.getWaitTime());
			setEndWaitTime(new Timestamp(cal.getTimeInMillis()));
			return false;
		}
		//
		throw new IllegalArgumentException("Invalid Action (Not Implemented) =" + action);
	}	//	performWork
	
	/**
	 * 	Set Variable
	 *	@param value new Value
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	private boolean setVariable(String value, int displayType, String textMsg) throws Exception
	{
		m_newValue = null;
		getPO();
		if (m_po == null)
			throw new Exception("Persistent Object not found - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
		//	Set Value
		Object dbValue = null;
		if (value == null)
			;
		else if (displayType == DisplayType.YesNo)
			dbValue = new Boolean("Y".equals(value));
		else if (DisplayType.isNumeric(displayType))
			dbValue = new BigDecimal (value);
		else
			dbValue = value;
		m_po.set_ValueOfColumn(getNode().getAD_Column_ID(), dbValue);
		m_po.save();
		if (!dbValue.equals(m_po.get_ValueOfColumn(getNode().getAD_Column_ID())))
			throw new Exception("Persistent Object not updated - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID() 
				+ " - Should=" + value + ", Is=" + m_po.get_ValueOfColumn(m_node.getAD_Column_ID()));
		//	Info
		String msg = getNode().getAttributeName() + "=" + value;
		if (textMsg != null && textMsg.length() > 0)
			msg += " - " + textMsg;
		setTextMsg (msg);
		m_newValue = value;
		return true;
	}	//	setVariable
	
	/**
	 * 	Set User Choice
	 *	@param value new Value
	 *	@param displayType display type
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	public boolean setUserChoice (int AD_User_ID, String value, int displayType, 
		String textMsg) throws Exception
	{
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		boolean ok = setVariable (value, displayType, textMsg);
		if (!ok)
			return false;

		String newState = StateEngine.STATE_Completed;
		//	Approval
		if (getNode().isUserApproval() && getPO() instanceof DocAction)
		{
			DocAction doc = (DocAction)m_po;
			try
			{
				//	Not pproved
				if (!"Y".equals(value))
				{
					newState = StateEngine.STATE_Aborted;
					if (!(doc.processIt (DocAction.ACTION_Reject)))
						setTextMsg ("Cannot Reject - Document Status: " + doc.getDocStatus());
				}
				else
				{
					if (isInvoker())
					{
						int startAD_User_ID = getAD_User_ID();
						if (startAD_User_ID == 0)
							startAD_User_ID = doc.getDoc_User_ID();
						int nextAD_User_ID = getApprovalUser(startAD_User_ID, 
							doc.getC_Currency_ID(), doc.getApprovalAmt(),
							doc.getAD_Org_ID(), 
							startAD_User_ID == doc.getDoc_User_ID());
						//	No Approver
						if (nextAD_User_ID <= 0)
						{
							newState = StateEngine.STATE_Aborted;
							setTextMsg ("Cannot Approve - No Approver");
							doc.processIt (DocAction.ACTION_Reject);
						}
						else if (startAD_User_ID != nextAD_User_ID)
						{
							forwardTo(nextAD_User_ID, "Next Approver");
							newState = StateEngine.STATE_Suspended;
						}
						else	//	Approve
						{
							if (!(doc.processIt (DocAction.ACTION_Approve)))
							{
								newState = StateEngine.STATE_Aborted;
								setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
							}		
						}
					}
					//	No Invoker - Approve
					else if (!(doc.processIt (DocAction.ACTION_Approve)))
					{
						newState = StateEngine.STATE_Aborted;
						setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
					}
				}
				doc.save();
			}
			catch (Exception e)
			{
				newState = StateEngine.STATE_Terminated;
				setTextMsg ("User Choice: " + e.toString());
				log.error("setUserChoice", e);
			}
		}	
		setWFState (newState);
		return ok;
	}	//	setUserChoice
	
	/**
	 * 	Forward To
	 *	@param AD_User_ID user
	 *	@param textMsg text message
	 *	@return true if forwarded
	 */
	public boolean forwardTo (int AD_User_ID, String textMsg)
	{
		if (AD_User_ID == getAD_User_ID())
		{
			log.error("forwardTo - Same User - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//
		MUser oldUser = MUser.get(getCtx(), getAD_User_ID());
		MUser user = MUser.get(getCtx(), AD_User_ID);
		if (user == null || user.getID() == 0)
		{
			log.error("forwardTo - Does not exist - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//	Update 
		setAD_User_ID (user.getAD_User_ID());
		setTextMsg(textMsg);
		save();
		//	Close up Old Event
		getEventAudit();
		m_audit.setAD_User_ID(oldUser.getAD_User_ID());
		m_audit.setTextMsg(getTextMsg());
		m_audit.setAttributeName("AD_User_ID");
		m_audit.setOldValue(oldUser.getName()+ "("+oldUser.getAD_User_ID()+")");
		m_audit.setNewValue(user.getName()+ "("+user.getAD_User_ID()+")");
		//
		m_audit.setWFState(getWFState());
		m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
		m_audit.setElapsedTimeMS(new BigDecimal(ms));
		m_audit.save();
		//	Create new one
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		return true;
	}	//	forwardTo

	/**
	 * 	Set User Confirmation
	 *	@param textMsg optional message
	 */
	public void setUserConfirmation (int AD_User_ID, String textMsg)
	{
		log.debug("setUserConfirmation - " + textMsg);
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		if (textMsg != null)
			setTextMsg (textMsg);
		setWFState (StateEngine.STATE_Completed);
	}	//	setUserConfirmation
	

	/**
	 * 	Fill Parameter
	 *	@param pInstance process instance
	 */
	private void fillParameter(MPInstance pInstance)
	{
		getPO();
		//
		MWFNodePara[] nParams = m_node.getParameters();
		MPInstance_Para[] iParams = pInstance.getParameters();
		for (int pi = 0; pi < iParams.length; pi++)
		{
			MPInstance_Para iPara = iParams[pi];
			for (int np = 0; np < nParams.length; np++)
			{
				MWFNodePara nPara = nParams[np];
				if (iPara.getParameterName().equals(nPara.getAttributeName()))
				{
					String variableName = nPara.getAttributeValue();
					log.debug("fillParameter - " + nPara.getAttributeName()
						+ " = " + variableName);
					//	Value - Constant/Variable
					Object value = variableName;
					if (variableName == null && variableName.length() == 0)
						value = null;
					else if (variableName.indexOf("@") != -1 && m_po != null)	//	we have a variable
					{
						//	Strip
						int index = variableName.indexOf("@");
						String columnName = variableName.substring(index);
						index = columnName.indexOf("@");
						if (index == -1)
						{
							log.warn("fillParameter - " + nPara.getAttributeName()
								+ " - cannot evaluate=" + variableName);
							break;
						}
						columnName = columnName.substring(0, index);
						index = m_po.get_ColumnIndex(columnName);
						if (index != -1)
						{
							value = m_po.get_Value(index);
						}
						else	//	not a column
						{
							//	try Env
							String env = Env.getContext(getCtx(), columnName);
							if (env.length() == 0)
							{
								log.warn("fillParameter - " + nPara.getAttributeName()
									+ " - not column nor environment =" + columnName 
									+ "(" + variableName + ")");
								break;
							}
							else
								value = env;
						}
					}	//	@variable@
					
					//	No Value
					if (value == null)
					{
						if (nPara.isMandatory())
							log.warn("fillParameter - " + nPara.getAttributeName() 
								+ " - empty - mandatory!");
						else
							log.debug("fillParameter - " + nPara.getAttributeName() 
								+ " - empty");
						break;
					}
					
					//	Convert to Type
					try
					{
						if (DisplayType.isNumeric(nPara.getDisplayType()) 
							|| DisplayType.isID(nPara.getDisplayType()))
						{
							BigDecimal bd = null;
							if (value instanceof BigDecimal)
								bd = (BigDecimal)value;
							else if (value instanceof Integer)
								bd = new BigDecimal (((Integer)value).intValue());
							else
								bd = new BigDecimal (value.toString());
							iPara.setP_Number(bd);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + bd + "=)");
						}
						else if (DisplayType.isDate(nPara.getDisplayType()))
						{
							Timestamp ts = null;
							if (value instanceof Timestamp)
								ts = (Timestamp)value;
							else
								ts = Timestamp.valueOf(value.toString());
							iPara.setP_Date(ts);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + ts + "=)");
						}
						else
						{
							iPara.setP_String(value.toString());
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName
								+ " (=" + value + "=) " + value.getClass().getName());
						}
					}
					catch (Exception e)
					{
						log.warn("fillParameter - " + nPara.getAttributeName()
							+ " = " + variableName + " (" + value
							+ ") " + value.getClass().getName()
							+ " - " + e.getLocalizedMessage());
					}
					break;
				}
			}	//	node parameter loop
		}	//	instance parameter loop
	}	//	fillParameter

	
	/**************************************************************************
	 * 	Get Process Activity (Event) History
	 *	@return history
	 */
	public String getHistoryHTML()
	{
		SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.DateTime);
		StringBuffer sb = new StringBuffer();
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID());
		for (int i = 0; i < events.length; i++)
		{
			MWFEventAudit audit = events[i];
		//	sb.append("<p style=\"width:400\">");
			sb.append("<p>");
			sb.append(format.format(audit.getCreated()))
				.append(" ")
				.append(getHTMLpart("b", audit.getNodeName()))
				.append(": ")
				.append(getHTMLpart(null, audit.getDescription()))
				.append(getHTMLpart("i", audit.getTextMsg()));
			sb.append("</p>");
		}
		return sb.toString();
	}	//	getHistory
	
	/**
	 * 	Get HTML part
	 *	@param tag HTML tag
	 *	@param content content
	 *	@return <tag>content</tag>
	 */
	private StringBuffer getHTMLpart (String tag, String content)
	{
		StringBuffer sb = new StringBuffer();
		if (content == null || content.length() == 0)
			return sb;
		if (tag != null && tag.length() > 0)
			sb.append("<").append(tag).append(">");
		sb.append(content);
		if (tag != null && tag.length() > 0)
			sb.append("</").append(tag).append(">");
		return sb;
	}	//	getHTMLpart
	
	
	/**************************************************************************
	 * 	Does the underlying PO (!) object have a PDF Attachment
	 * 	@return true if there is a pdf attachment
	 */
	public boolean isPdfAttachment()
	{
		if (getPO() == null)
			return false;
		return m_po.isPdfAttachment();
	}	//	isPDFAttachment

	/**
	 * 	Get PDF Attachment of underlying PO (!) object
	 *	@return pdf data or null
	 */
	public byte[] getPdfAttachment()
	{
		if (getPO() == null)
			return null;
		return m_po.getPdfAttachment();
	}	//	getPdfAttachment
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MWFActivity[");
		sb.append(getID()).append(",Node=");
		if (m_node == null)
			sb.append(getAD_WF_Node_ID());
		else
			sb.append(m_node.getName());
		sb.append(",State=").append(getWFState())
			.append(",AD_User_ID=").append(getAD_User_ID())
			.append(",").append(getCreated())
			.append ("]");
		return sb.toString ();
	} 	//	toString
	
	/**
	 * 	User String Representation.
	 * 	Suspended: Approve it (Joe)
	 *	@return info
	 */
	public String toStringX ()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getWFStateText())
			.append(": ").append(getNode().getName());
		if (getAD_User_ID() > 0)
		{
			MUser user = MUser.get(getCtx(), getAD_User_ID());
			sb.append(" (").append(user.getName()).append(")");
		}
		return sb.toString();
	}	//	toStringX
	
}	//	MWFActivity
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wf;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.io.*;

import org.compiere.model.*;
import org.compiere.print.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 *	Workflow Activity Model.
 *	Controlled by WF Process: 
 *		set Node - startWork 
 *	
 *  @author Jorg Janke
 *  @version $Id: MWFActivity.java,v 1.23 2004/09/04 06:16:09 jjanke Exp $
 */
public class MWFActivity extends X_AD_WF_Activity implements Runnable
{
	/**
	 * 	Get Activities for table/tecord 
	 *	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@param activeOnly if true only not processed records are returned
	 *	@return activity
	 */
	public static MWFActivity[] get (Properties ctx, int AD_Table_ID, int Record_ID, boolean activeOnly)
	{
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_WF_Activity WHERE AD_Table_ID=? AND Record_ID=?";
		if (activeOnly)
			sql += " AND Processed<>'Y'";
		sql += " ORDER BY AD_WF_Activity_ID";
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, AD_Table_ID);
			pstmt.setInt (2, Record_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MWFActivity (ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("get", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MWFActivity[] retValue = new MWFActivity[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	get

	/**
	 * 	Get Active Info
	 * 	@param ctx context
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@return activity summary
	 */
	public static String getActiveInfo (Properties ctx, int AD_Table_ID, int Record_ID)
	{
		MWFActivity[] acts = get (ctx, AD_Table_ID, Record_ID, true);
		if (acts == null || acts.length == 0)
			return null;
		//
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < acts.length; i++)
		{
			if (i > 0)
				sb.append("\n");
			MWFActivity activity = acts[i];
			sb.append(activity.toStringX());
		}
		return sb.toString();
	}	//	getActivityInfo

	/**	Static Logger	*/
	private static Logger	s_log	= Logger.getCLogger (MWFActivity.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_WF_Activity_ID id
	 */
	public MWFActivity (Properties ctx, int AD_WF_Activity_ID)
	{
		super (ctx, AD_WF_Activity_ID);
		if (AD_WF_Activity_ID == 0)
			throw new IllegalArgumentException ("Cannot create new WF Activity directly");
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MWFActivity (Properties ctx, ResultSet rs)
	{
		super (ctx, rs);
		m_state = new StateEngine (getWFState());
	}	//	MWFActivity

	/**
	 * 	Parent Contructor
	 *	@param process process
	 *	@param AD_WF_Node_ID start node
	 */
	public MWFActivity (MWFProcess process, int AD_WF_Node_ID)
	{
		super (process.getCtx(), 0);
		setAD_WF_Process_ID (process.getAD_WF_Process_ID());
		setPriority(process.getPriority());
		//	Document Link
		setAD_Table_ID(process.getAD_Table_ID());
		setRecord_ID(process.getRecord_ID());
		//	Status
		super.setWFState(WFSTATE_NotStarted);
		m_state = new StateEngine (getWFState());
		setProcessed (false);
		//	Set Workflow Node
		setAD_Workflow_ID (process.getAD_Workflow_ID());
		setAD_WF_Node_ID (AD_WF_Node_ID);
		//	Node Priority & End Duration
		MWFNode node = MWFNode.get(getCtx(), AD_WF_Node_ID);
		int priority = node.getPriority();
		if (priority != 0 && priority != getPriority())
			setPriority (priority);
		long limitMS = node.getLimitMS();
		if (limitMS != 0)
			setEndWaitTime(new Timestamp(limitMS + System.currentTimeMillis()));
		//	Responsible
		setResponsible(process);
		save();
		//
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		//
		m_process = process;
	}	//	MWFActivity

	/**	State Machine				*/
	private StateEngine			m_state = null;
	/**	Workflow Node				*/
	private MWFNode				m_node = null;
	/**	Audit						*/
	private MWFEventAudit		m_audit = null;
	/** Persistent Object			*/
	private PO					m_po = null;
	/**	New Value to save in audit	*/
	private String				m_newValue = null;
	/** Process						*/
	private MWFProcess 			m_process = null;
	
	
	/**************************************************************************
	 * 	Get State
	 *	@return state
	 */
	public StateEngine getState()
	{
		return m_state;
	}	//	getState

	/**
	 * 	Set Activity State
	 *	@param WFState
	 */
	public void setWFState (String WFState)
	{
		if (m_state == null)
			m_state = new StateEngine (getWFState());
		if (m_state.isClosed())
			return;
		if (getWFState().equals(WFState))
			return;
		//
		if (m_state.isValidNewState(WFState))
		{
			String oldState = getWFState();
			log.debug("setWFState - " + oldState + "->"+ WFState + ", Msg=" + getTextMsg()); 
			super.setWFState (WFState);
			m_state = new StateEngine (getWFState());
			save();			//	closed in MWFProcess.checkActivities()
			updateEventAudit();			
			
			//	Inform Process
			if (m_process == null)
				m_process = new MWFProcess (getCtx(), getAD_WF_Process_ID());
			m_process.checkActivities();
		}
		else
		{
			String msg = "Set WFState - Ignored Invalid Transformation - New=" 
				+ WFState + ", Current=" + getWFState(); 
			log.error(msg);
			Trace.printStack();
			setTextMsg(msg);
			save();
		}
	}	//	setWFState
	
	/**
	 * 	Is Activity closed
	 */
	public boolean isClosed()
	{
		return m_state.isClosed();
	}	//	isClosed
	
	
	/**************************************************************************
	 * 	Update Event Audit
	 */
	private void updateEventAudit()
	{
	//	log.debug("updateEventAudit");
		getEventAudit();
		m_audit.setTextMsg(getTextMsg());
		m_audit.setWFState(getWFState());
		if (m_newValue != null)
			m_audit.setNewValue(m_newValue);
		if (m_state.isClosed())
		{
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_ProcessCompleted);
			long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
			m_audit.setElapsedTimeMS(new BigDecimal(ms));
		}
		else
			m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		m_audit.save();
	}	//	updateEventAudit

	/**
	 * 	Get/Create Event Audit
	 * 	@return event
	 */
	public MWFEventAudit getEventAudit()
	{
		if (m_audit != null)
			return m_audit;
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID(), getAD_WF_Node_ID());
		if (events == null || events.length == 0)
			m_audit = new MWFEventAudit(this);
		else
			m_audit = events[events.length-1];		//	last event
		return m_audit;
	}	//	getEventAudit
	
	
	/**************************************************************************
	 * 	Get Persistent Object
	 *	@return po
	 */
	public PO getPO()
	{
		if (m_po != null)
			return m_po;
		
		M_Table table = M_Table.get (getCtx(), getAD_Table_ID());
		m_po = table.getPO(getRecord_ID());
		return m_po;
	}	//	getPO
	
	/**
	 * 	Get Attribute Value (based on Node) of PO
	 *	@return Attribute Value or null
	 */
	public Object getAttributeValue()
	{
		MWFNode node = getNode();
		if (node == null)
			return null;
		int AD_Column_ID = node.getAD_Column_ID();
		if (AD_Column_ID == 0)
			return null;
		PO po = getPO();
		if (po.getID() == 0)
			return null;
		return po.get_ValueOfColumn(AD_Column_ID);
	}	//	getAttributeValue
	
	/**
	 * 	Is SO Trx
	 *	@return SO Trx or of not found true
	 */
	public boolean isSOTrx()
	{
		PO po = getPO();
		if (po.getID() == 0)
			return true;
		//	Is there a Column?
		int index = po.get_ColumnIndex("IsSOTrx");
		if (index < 0)
		{
			if (po.get_TableName().startsWith("M_"))
				return false;
			return true;
		}
		//	we have a column
		try
		{
			Boolean IsSOTrx = (Boolean)po.get_Value(index);
			return IsSOTrx.booleanValue();
		}
		catch (Exception e)
		{
			log.error("isSOTrx", e);
		}
		return true;
	}	//	isSOTrx
	
	
	/**************************************************************************
	 * 	Set AD_WF_Node_ID.
	 * 	(Re)Set to Not Started
	 *	@param AD_WF_Node_ID now node
	 */
	public void setAD_WF_Node_ID (int AD_WF_Node_ID)
	{
		if (AD_WF_Node_ID == 0)
			throw new IllegalArgumentException("Workflow Node is not defined");
		super.setAD_WF_Node_ID (AD_WF_Node_ID);
		//
		if (!WFSTATE_NotStarted.equals(getWFState()))
		{
			super.setWFState(WFSTATE_NotStarted);
			m_state = new StateEngine (getWFState());
		}
		if (isProcessed())
			setProcessed (false);
	}	//	setAD_WF_Node_ID
	
	/**
	 * 	Get WF Node
	 *	@return node
	 */
	public MWFNode getNode()
	{
		if (m_node == null)
			m_node = MWFNode.get (getCtx(), getAD_WF_Node_ID());
		return m_node;
	}	//	getNode
	
	/**
	 * 	Get WF Node Name
	 *	@return translated node name
	 */
	public String getNodeName()
	{
		return getNode().getName(true);
	}	//	getNodeName

	/**
	 * 	Get Node Description
	 *	@return translated node description
	 */
	public String getNodeDescription()
	{
		return getNode().getDescription(true);
	}	//	getNodeDescription
	
	/**
	 * 	Get Node Help
	 *	@return translated node help
	 */
	public String getNodeHelp()
	{
		return getNode().getHelp(true);
	}	//	getNodeHelp
	
	
	/**
	 * 	Is this an user Approval step?
	 *	@return true if User Approval
	 */
	public boolean isUserApproval()
	{
		return getNode().isUserApproval();
	}	//	isNodeApproval

	/**
	 * 	Is this a Manual user step?
	 *	@return true if Window/Form/..
	 */
	public boolean isUserManual()
	{
		return getNode().isUserManual();
	}	//	isUserManual

	/**
	 * 	Is this a user choice step?
	 *	@return true if User Choice
	 */
	public boolean isUserChoice()
	{
		return getNode().isUserChoice();
	}	//	isUserChoice

	
	/**
	 * 	Set Text Msg (add to existing)
	 *	@param TextMsg
	 */
	public void setTextMsg (String TextMsg)
	{
		if (TextMsg == null || TextMsg.length() == 0)
			return;
		String oldText = getTextMsg();
		if (oldText == null || oldText.length() == 0)
			super.setTextMsg (TextMsg);
		else if (TextMsg != null && TextMsg.length() > 0)
			super.setTextMsg (oldText + "\n - " + TextMsg);
	}	//	setTextMsg	
	
	
	/**
	 * 	Get WF State text
	 *	@return state text
	 */
	public String getWFStateText ()
	{
		return MRefList.getListName(getCtx(), WFSTATE_AD_Reference_ID, getWFState());
	}	//	getWFStateText
	
	/**
	 * 	Set Responsible and User from Process / Node
	 *	@param process process
	 */
	private void setResponsible (MWFProcess process)
	{
		//	Responsible
		int AD_WF_Responsible_ID = getNode().getAD_WF_Responsible_ID();
		if (AD_WF_Responsible_ID == 0)	//	not defined on Node Level
			AD_WF_Responsible_ID = process.getAD_WF_Responsible_ID();
		setAD_WF_Responsible_ID (AD_WF_Responsible_ID);
		MWFResponsible resp = MWFResponsible.get(getCtx(), AD_WF_Responsible_ID);
		
		//	User - Directly responsible
		int AD_User_ID = resp.getAD_User_ID();
		//	Invoker - get Sales Rep or last updater of document
		if (AD_User_ID == 0 && resp.isInvoker())
			AD_User_ID = process.getAD_User_ID();
		//
		setAD_User_ID(AD_User_ID);
	}	//	setResponsible
	
	
	/**************************************************************************
	 * 	Is Invoker (no user & no role)
	 *	@return true if invoker
	 */
	public boolean isInvoker()
	{
		MWFResponsible resp = MWFResponsible.get(getCtx(), getAD_WF_Responsible_ID());
		return resp.isInvoker();
	}	//	isInvoker
	
	/**
	 * 	Get Approval User.
	 * 	If the returned user is the same, the document is approved.
	 *	@param AD_User_ID starting User
	 *	@param C_Currency_ID currency
	 *	@param amount amount
	 *	@param AD_Org_ID document organization
	 *	@param ownDocument the document is owned by AD_User_ID
	 *	@return AD_User_ID - if -1 no Approver
	 */
	public int getApprovalUser (int AD_User_ID, 
			int C_Currency_ID, BigDecimal amount, 
			int AD_Org_ID, boolean ownDocument)
	{
		//	Nothing to approve
		if (amount == null || amount.compareTo(Env.ZERO) == 0)
			return AD_User_ID;
		
		//	Starting user
		MUser user = MUser.get(getCtx(), AD_User_ID);
		log.info("getApprovalUser for " + user.getName() + ", Amt=" + amount + ", Own=" + ownDocument);

		MUser oldUser = null;
		while (user != null)
		{
			if (user.equals(oldUser))
			{
				log.info("getApprovalUser - Loop - = " + user.getName());
				return -1;
			}
			oldUser = user;
			log.debug("getApprovalUser = " + user.getName());
			//	Get Roles of User
			MRole[] roles = user.getRoles(AD_Org_ID);
			for (int i = 0; i < roles.length; i++)
			{
				MRole role = roles[i];
				if (ownDocument && !role.isCanApproveOwnDoc())
					continue;
				BigDecimal roleAmt = role.getAmtApproval();
				if (roleAmt == null || roleAmt.compareTo(Env.ZERO) == 0)
					continue;
				if (C_Currency_ID != role.getC_Currency_ID())	//	today & default rate
					roleAmt =  MConversionRate.convert(getCtx(), 
						roleAmt, role.getC_Currency_ID(), 
						C_Currency_ID, getAD_Client_ID(), AD_Org_ID);
				boolean approved = amount.compareTo(roleAmt) <= 0;
				log.debug("getApprovalUser - " + approved 
					+ " - User=" + user.getName() + ", Role=" + role.getName()
					+ ", ApprovalAmt=" + roleAmt);
				if (approved)
					return user.getAD_User_ID();
			}
			
			//	**** Find next User 
			//	Get Supervisor
			if (user.getSupervisor_ID() != 0)
			{
				user = MUser.get(getCtx(), user.getSupervisor_ID());
				log.debug("getApprovalUser - Supervisor: " + user.getName()); 
			}
			else
			{
				log.debug("getApprovalUser - No Supervisor"); 
				MOrg org = MOrg.get (getCtx(), AD_Org_ID);
				MOrgInfo orgInfo = org.getInfo();
				//	Get Org Supervisor
				if (orgInfo.getSupervisor_ID() != 0)
				{
					user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
					log.debug("getApprovalUser - Org=" + org.getName() + ",Supervisor: " + user.getName()); 
				}
				else
				{
					log.debug("getApprovalUser - No Org Supervisor"); 
					//	Get Parent Org Supervisor
					if (orgInfo.getParent_Org_ID() != 0)
					{
						org = MOrg.get (getCtx(), orgInfo.getParent_Org_ID());
						orgInfo = org.getInfo();
						if (orgInfo.getSupervisor_ID() != 0)
						{
							user = MUser.get(getCtx(), orgInfo.getSupervisor_ID());
							log.debug("getApprovalUser - Parent Org Supervisor: " + user.getName()); 
						}
					}
				}
			}	//	No Supervisor
			
		}	//	while there is a user to approve
		
		log.debug("getApprovalUser - No user found"); 
		return -1;
	}	//	getApproval

	
	/**************************************************************************
	 * 	Execute Work.
	 * 	Called from MWFProcess.startNext
	 * 	Feedback to Process via setWFState -> checkActivities
	 */
	public void run()
	{
		log.info ("run - " + getNode());
		m_newValue = null;
		if (!m_state.isValidAction(StateEngine.ACTION_Start))
		{
			setTextMsg("State=" + getWFState() + " - cannot start");
			setWFState(StateEngine.STATE_Terminated);
			return;
		}
		//
		setWFState(StateEngine.STATE_Running);
		//
		try
		{
			if (getNode().getID() == 0)
			{
				setTextMsg("Node not found - AD_WF_Node_ID=" + getAD_WF_Node_ID());
				setWFState(StateEngine.STATE_Aborted);
				return;
			}
			//	Do Work
			boolean done = performWork();
			setWFState (done ? StateEngine.STATE_Completed : StateEngine.STATE_Suspended);
		}
		catch (Exception e)
		{
			log.error("run", e);
			if (e.getCause() != null)
				log.error("run - cause", e.getCause());
			String msg = e.getLocalizedMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			setTextMsg(msg);
			setWFState (StateEngine.STATE_Terminated);
		}
	}	//	run
	
	
	/**
	 * 	Perform Work.
	 * 	Set Text Msg.
	 *	@return true if completed, false otherwise
	 *	@throws Exception if error
	 */
	private boolean performWork() throws Exception
	{
		log.debug ("performWork - " + m_node);
		if (m_node.getPriority() != 0)		//	overwrite priority if defined
			setPriority(m_node.getPriority());
		String action = m_node.getAction();
		
		/******	Document Action				******/
		if (MWFNode.ACTION_DocumentAction.equals(action))
		{
			log.debug ("performWork - DocumentAction=" + m_node.getDocAction());
			getPO();
			if (m_po == null)
				throw new Exception("Persistent Object not found - AD_Table_ID=" 
					+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			boolean success = false;
			String processMsg = null;
			if (m_po instanceof DocAction)
			{
				DocAction doc = (DocAction)m_po;
				try
				{
					success = doc.processIt (m_node.getDocAction());	//	** Do the work
					setTextMsg(doc.getSummary());
					processMsg = doc.getProcessMsg();
				}
				catch (Exception e)
				{
					log.error ("performWork", e);
					processMsg = e.getLocalizedMessage();
					if (processMsg == null || processMsg.length() == 0)
						processMsg = e.toString(); 
					success = false;
					setTextMsg(processMsg);
					//	TODO rollback
				}
				if (m_process != null)
					m_process.setProcessMsg(processMsg);
			}
			else
				throw new IllegalStateException("Persistent Object not DocAction - "
					+ m_po.getClass().getName()
					+ " - AD_Table_ID=" + getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
			//
			if (!m_po.save())
			{
				success = false;
				processMsg = "SaveError";
			}
			if (!success)
			{
				if (processMsg == null || processMsg.length() == 0)
					processMsg = "PerformWork Error - " + m_node.toStringX();
				throw new Exception(processMsg);
			}
			return success;
		}	//	DocumentAction
		
		/******	Report						******/
		else if (MWFNode.ACTION_AppsReport.equals(action))
		{
			log.debug ("performWork - Report:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			if (!process.isReport() || process.getAD_ReportView_ID() == 0)
				throw new IllegalStateException("Not a Report AD_Process_ID=" + m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			//	Report
			ReportEngine re = ReportEngine.get(getCtx(), pi);
			if (re == null)
				throw new IllegalStateException("Cannot create Report AD_Process_ID=" + m_node.getAD_Process_ID());
			File report = re.getPDF();
			//	Notice
			int AD_Message_ID = 753;		//	HARDCODED WorkflowResult
			MNote note = new MNote(getCtx(), AD_Message_ID, getAD_User_ID());
			note.setTextMsg(m_node.getName(true));
			note.setDescription(m_node.getDescription(true));
			note.setRecord(getAD_Table_ID(), getRecord_ID());
			note.save();
			//	Attachment
			MAttachment attachment = new MAttachment (getCtx(), MNote.Table_ID, note.getAD_Note_ID());
			attachment.addEntry(report);
			attachment.setTextMsg(m_node.getName(true));
			attachment.save();
			return true;
		}
		
		/******	Process						******/
		else if (MWFNode.ACTION_AppsProcess.equals(action))
		{
			log.debug ("performWork - Process:AD_Process_ID=" + m_node.getAD_Process_ID());
			//	Process
			MProcess process = MProcess.get(getCtx(), m_node.getAD_Process_ID());
			ProcessInfo pi = new ProcessInfo (m_node.getName(true), m_node.getAD_Process_ID(),
				getAD_Table_ID(), getRecord_ID());
			pi.setAD_User_ID(getAD_User_ID());
			pi.setAD_Client_ID(getAD_Client_ID());
			MPInstance pInstance = new MPInstance(process, getRecord_ID());
			fillParameter(pInstance);
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			return process.processIt(pi);
		}
		
		else if (MWFNode.ACTION_AppsTask.equals(action))
		{
			log.debug ("performWork - Task:AD_Task_ID=" + m_node.getAD_Task_ID());
//	TODO start task
		}
		
		else if (MWFNode.ACTION_SetVariable.equals(action))
		{
			String value = m_node.getAttributeValue();
			log.debug ("performWork - SetVariable:AD_Column_ID=" + m_node.getAD_Column_ID()
				+ " to " +  value);
			M_Column column = m_node.getColumn();
			int dt = column.getAD_Reference_ID();
			return setVariable (value, dt, null);
		}	//	SetVariable
		
		else if (MWFNode.ACTION_SubWorkflow.equals(action))
		{
			log.debug ("performWork - Workflow:AD_Workflow_ID=" + m_node.getAD_Workflow_ID());
//	TODO start WF
		}
		
		else if (MWFNode.ACTION_UserChoice.equals(action))
		{
			log.debug ("performWork - UserChoice:AD_Column_ID=" + m_node.getAD_Column_ID());
			return false;
		}
		else if (MWFNode.ACTION_UserWorkbench.equals(action))
		{
			log.debug ("performWork - Workbench:?");
			return false;
		}
		
		else if (MWFNode.ACTION_UserForm.equals(action))
		{
			log.debug ("performWork - Form:AD_Form_ID=" + m_node.getAD_Form_ID());
			return false;
		}
		
		else if (MWFNode.ACTION_UserWindow.equals(action))
		{
			log.debug ("performWork - Window:AD_Window_ID=" + m_node.getAD_Window_ID());
			return false;
		}
		
		/**	Sleep (Start/End)			*/
		else if (MWFNode.ACTION_WaitSleep.equals(action))
		{
			log.debug ("performWork - Sleep:WaitTime=" + m_node.getWaitTime());
			if (m_node.getWaitingTime() == 0)
				return true;
			Calendar cal = Calendar.getInstance();
			cal.add(m_node.getDurationCalendarField(), m_node.getWaitTime());
			setEndWaitTime(new Timestamp(cal.getTimeInMillis()));
			return false;	//	not done
		}
		//
		throw new IllegalArgumentException("Invalid Action (Not Implemented) =" + action);
	}	//	performWork
	
	/**
	 * 	Set Variable
	 *	@param value new Value
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	private boolean setVariable(String value, int displayType, String textMsg) throws Exception
	{
		m_newValue = null;
		getPO();
		if (m_po == null)
			throw new Exception("Persistent Object not found - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID());
		//	Set Value
		Object dbValue = null;
		if (value == null)
			;
		else if (displayType == DisplayType.YesNo)
			dbValue = new Boolean("Y".equals(value));
		else if (DisplayType.isNumeric(displayType))
			dbValue = new BigDecimal (value);
		else
			dbValue = value;
		m_po.set_ValueOfColumn(getNode().getAD_Column_ID(), dbValue);
		m_po.save();
		if (!dbValue.equals(m_po.get_ValueOfColumn(getNode().getAD_Column_ID())))
			throw new Exception("Persistent Object not updated - AD_Table_ID=" 
				+ getAD_Table_ID() + ", Record_ID=" + getRecord_ID() 
				+ " - Should=" + value + ", Is=" + m_po.get_ValueOfColumn(m_node.getAD_Column_ID()));
		//	Info
		String msg = getNode().getAttributeName() + "=" + value;
		if (textMsg != null && textMsg.length() > 0)
			msg += " - " + textMsg;
		setTextMsg (msg);
		m_newValue = value;
		return true;
	}	//	setVariable
	
	/**
	 * 	Set User Choice
	 *	@param value new Value
	 *	@param displayType display type
	 *	@param textMsg optional Message
	 *	@param return true if set
	 *	@throws Exception if error
	 */
	public boolean setUserChoice (int AD_User_ID, String value, int displayType, 
		String textMsg) throws Exception
	{
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		boolean ok = setVariable (value, displayType, textMsg);
		if (!ok)
			return false;

		String newState = StateEngine.STATE_Completed;
		//	Approval
		if (getNode().isUserApproval() && getPO() instanceof DocAction)
		{
			DocAction doc = (DocAction)m_po;
			try
			{
				//	Not pproved
				if (!"Y".equals(value))
				{
					newState = StateEngine.STATE_Aborted;
					if (!(doc.processIt (DocAction.ACTION_Reject)))
						setTextMsg ("Cannot Reject - Document Status: " + doc.getDocStatus());
				}
				else
				{
					if (isInvoker())
					{
						int startAD_User_ID = getAD_User_ID();
						if (startAD_User_ID == 0)
							startAD_User_ID = doc.getDoc_User_ID();
						int nextAD_User_ID = getApprovalUser(startAD_User_ID, 
							doc.getC_Currency_ID(), doc.getApprovalAmt(),
							doc.getAD_Org_ID(), 
							startAD_User_ID == doc.getDoc_User_ID());
						//	No Approver
						if (nextAD_User_ID <= 0)
						{
							newState = StateEngine.STATE_Aborted;
							setTextMsg ("Cannot Approve - No Approver");
							doc.processIt (DocAction.ACTION_Reject);
						}
						else if (startAD_User_ID != nextAD_User_ID)
						{
							forwardTo(nextAD_User_ID, "Next Approver");
							newState = StateEngine.STATE_Suspended;
						}
						else	//	Approve
						{
							if (!(doc.processIt (DocAction.ACTION_Approve)))
							{
								newState = StateEngine.STATE_Aborted;
								setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
							}		
						}
					}
					//	No Invoker - Approve
					else if (!(doc.processIt (DocAction.ACTION_Approve)))
					{
						newState = StateEngine.STATE_Aborted;
						setTextMsg ("Cannot Approve - Document Status: " + doc.getDocStatus());
					}
				}
				doc.save();
			}
			catch (Exception e)
			{
				newState = StateEngine.STATE_Terminated;
				setTextMsg ("User Choice: " + e.toString());
				log.error("setUserChoice", e);
			}
		}	
		setWFState (newState);
		return ok;
	}	//	setUserChoice
	
	/**
	 * 	Forward To
	 *	@param AD_User_ID user
	 *	@param textMsg text message
	 *	@return true if forwarded
	 */
	public boolean forwardTo (int AD_User_ID, String textMsg)
	{
		if (AD_User_ID == getAD_User_ID())
		{
			log.error("forwardTo - Same User - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//
		MUser oldUser = MUser.get(getCtx(), getAD_User_ID());
		MUser user = MUser.get(getCtx(), AD_User_ID);
		if (user == null || user.getID() == 0)
		{
			log.error("forwardTo - Does not exist - AD_User_ID=" + AD_User_ID);
			return false;
		}
		//	Update 
		setAD_User_ID (user.getAD_User_ID());
		setTextMsg(textMsg);
		save();
		//	Close up Old Event
		getEventAudit();
		m_audit.setAD_User_ID(oldUser.getAD_User_ID());
		m_audit.setTextMsg(getTextMsg());
		m_audit.setAttributeName("AD_User_ID");
		m_audit.setOldValue(oldUser.getName()+ "("+oldUser.getAD_User_ID()+")");
		m_audit.setNewValue(user.getName()+ "("+user.getAD_User_ID()+")");
		//
		m_audit.setWFState(getWFState());
		m_audit.setEventType(MWFEventAudit.EVENTTYPE_StateChanged);
		long ms = System.currentTimeMillis() - m_audit.getCreated().getTime();
		m_audit.setElapsedTimeMS(new BigDecimal(ms));
		m_audit.save();
		//	Create new one
		m_audit = new MWFEventAudit(this);
		m_audit.save();
		return true;
	}	//	forwardTo

	/**
	 * 	Set User Confirmation
	 *	@param textMsg optional message
	 */
	public void setUserConfirmation (int AD_User_ID, String textMsg)
	{
		log.debug("setUserConfirmation - " + textMsg);
		setWFState (StateEngine.STATE_Running);
		setAD_User_ID(AD_User_ID);
		if (textMsg != null)
			setTextMsg (textMsg);
		setWFState (StateEngine.STATE_Completed);
	}	//	setUserConfirmation
	

	/**
	 * 	Fill Parameter
	 *	@param pInstance process instance
	 */
	private void fillParameter(MPInstance pInstance)
	{
		getPO();
		//
		MWFNodePara[] nParams = m_node.getParameters();
		MPInstance_Para[] iParams = pInstance.getParameters();
		for (int pi = 0; pi < iParams.length; pi++)
		{
			MPInstance_Para iPara = iParams[pi];
			for (int np = 0; np < nParams.length; np++)
			{
				MWFNodePara nPara = nParams[np];
				if (iPara.getParameterName().equals(nPara.getAttributeName()))
				{
					String variableName = nPara.getAttributeValue();
					log.debug("fillParameter - " + nPara.getAttributeName()
						+ " = " + variableName);
					//	Value - Constant/Variable
					Object value = variableName;
					if (variableName == null && variableName.length() == 0)
						value = null;
					else if (variableName.indexOf("@") != -1 && m_po != null)	//	we have a variable
					{
						//	Strip
						int index = variableName.indexOf("@");
						String columnName = variableName.substring(index);
						index = columnName.indexOf("@");
						if (index == -1)
						{
							log.warn("fillParameter - " + nPara.getAttributeName()
								+ " - cannot evaluate=" + variableName);
							break;
						}
						columnName = columnName.substring(0, index);
						index = m_po.get_ColumnIndex(columnName);
						if (index != -1)
						{
							value = m_po.get_Value(index);
						}
						else	//	not a column
						{
							//	try Env
							String env = Env.getContext(getCtx(), columnName);
							if (env.length() == 0)
							{
								log.warn("fillParameter - " + nPara.getAttributeName()
									+ " - not column nor environment =" + columnName 
									+ "(" + variableName + ")");
								break;
							}
							else
								value = env;
						}
					}	//	@variable@
					
					//	No Value
					if (value == null)
					{
						if (nPara.isMandatory())
							log.warn("fillParameter - " + nPara.getAttributeName() 
								+ " - empty - mandatory!");
						else
							log.debug("fillParameter - " + nPara.getAttributeName() 
								+ " - empty");
						break;
					}
					
					//	Convert to Type
					try
					{
						if (DisplayType.isNumeric(nPara.getDisplayType()) 
							|| DisplayType.isID(nPara.getDisplayType()))
						{
							BigDecimal bd = null;
							if (value instanceof BigDecimal)
								bd = (BigDecimal)value;
							else if (value instanceof Integer)
								bd = new BigDecimal (((Integer)value).intValue());
							else
								bd = new BigDecimal (value.toString());
							iPara.setP_Number(bd);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + bd + "=)");
						}
						else if (DisplayType.isDate(nPara.getDisplayType()))
						{
							Timestamp ts = null;
							if (value instanceof Timestamp)
								ts = (Timestamp)value;
							else
								ts = Timestamp.valueOf(value.toString());
							iPara.setP_Date(ts);
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName + " (=" + ts + "=)");
						}
						else
						{
							iPara.setP_String(value.toString());
							log.debug("fillParameter - " + nPara.getAttributeName()
								+ " = " + variableName
								+ " (=" + value + "=) " + value.getClass().getName());
						}
					}
					catch (Exception e)
					{
						log.warn("fillParameter - " + nPara.getAttributeName()
							+ " = " + variableName + " (" + value
							+ ") " + value.getClass().getName()
							+ " - " + e.getLocalizedMessage());
					}
					break;
				}
			}	//	node parameter loop
		}	//	instance parameter loop
	}	//	fillParameter

	
	/**************************************************************************
	 * 	Get Process Activity (Event) History
	 *	@return history
	 */
	public String getHistoryHTML()
	{
		SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.DateTime);
		StringBuffer sb = new StringBuffer();
		MWFEventAudit[] events = MWFEventAudit.get(getCtx(), getAD_WF_Process_ID());
		for (int i = 0; i < events.length; i++)
		{
			MWFEventAudit audit = events[i];
		//	sb.append("<p style=\"width:400\">");
			sb.append("<p>");
			sb.append(format.format(audit.getCreated()))
				.append(" ")
				.append(getHTMLpart("b", audit.getNodeName()))
				.append(": ")
				.append(getHTMLpart(null, audit.getDescription()))
				.append(getHTMLpart("i", audit.getTextMsg()));
			sb.append("</p>");
		}
		return sb.toString();
	}	//	getHistory
	
	/**
	 * 	Get HTML part
	 *	@param tag HTML tag
	 *	@param content content
	 *	@return <tag>content</tag>
	 */
	private StringBuffer getHTMLpart (String tag, String content)
	{
		StringBuffer sb = new StringBuffer();
		if (content == null || content.length() == 0)
			return sb;
		if (tag != null && tag.length() > 0)
			sb.append("<").append(tag).append(">");
		sb.append(content);
		if (tag != null && tag.length() > 0)
			sb.append("</").append(tag).append(">");
		return sb;
	}	//	getHTMLpart
	
	
	/**************************************************************************
	 * 	Does the underlying PO (!) object have a PDF Attachment
	 * 	@return true if there is a pdf attachment
	 */
	public boolean isPdfAttachment()
	{
		if (getPO() == null)
			return false;
		return m_po.isPdfAttachment();
	}	//	isPDFAttachment

	/**
	 * 	Get PDF Attachment of underlying PO (!) object
	 *	@return pdf data or null
	 */
	public byte[] getPdfAttachment()
	{
		if (getPO() == null)
			return null;
		return m_po.getPdfAttachment();
	}	//	getPdfAttachment
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MWFActivity[");
		sb.append(getID()).append(",Node=");
		if (m_node == null)
			sb.append(getAD_WF_Node_ID());
		else
			sb.append(m_node.getName());
		sb.append(",State=").append(getWFState())
			.append(",AD_User_ID=").append(getAD_User_ID())
			.append(",").append(getCreated())
			.append ("]");
		return sb.toString ();
	} 	//	toString
	
	/**
	 * 	User String Representation.
	 * 	Suspended: Approve it (Joe)
	 *	@return info
	 */
	public String toStringX ()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getWFStateText())
			.append(": ").append(getNode().getName());
		if (getAD_User_ID() > 0)
		{
			MUser user = MUser.get(getCtx(), getAD_User_ID());
			sb.append(" (").append(user.getName()).append(")");
		}
		return sb.toString();
	}	//	toStringX
	
}	//	MWFActivity
