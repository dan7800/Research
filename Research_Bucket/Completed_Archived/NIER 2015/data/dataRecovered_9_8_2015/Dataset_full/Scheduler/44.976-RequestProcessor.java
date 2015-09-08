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
package org.compiere.request;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Msg;


/**
 *  Request Processor.
 *
 *  @author     Jorg Janke
 *  @version    $Id: RequestProcessor.java,v 1.4 2002/11/18 06:11:58 jjanke Exp $
 */
public class RequestProcessor
{
	/**
	 *	Constructor
	 *  @param vo value object
	 */
	public RequestProcessor (RequestProcessorVO vo)
	{
		long startTime = System.currentTimeMillis();
		runProcess (vo);
		//  read new web requests
		runWeb (vo);
		//  read new emails
		runEMail (vo);
		//
		long msec = System.currentTimeMillis() - startTime;
		double sec = (double)msec / 1000;
		log.info("RequestProcessor " + vo.Name + " - " + sec + " sec");
	}	//	RequestProcessor

	/**	Logger					*/
	private static Logger	log = Logger.getLogger(RequestProcessor.class);

	/*************************************************************************/

	/**
	 *  Process requests.
	 *  - update requests
	 *  - send alerts
	 *  - escalate alerts
	 *  - Set time next run
	 *
	 *  @param vo Value Object
	 */
	private void runProcess (RequestProcessorVO vo)
	{
		//  Run only when scheduled
		if (vo.DateNextRun != null && vo.DateNextRun.getTime() > System.currentTimeMillis())
			return;

		/**
		 *  update DueType of all requests
		 */
		String sql = "UPDATE R_Request "
			+ "SET DueType=R_Request_DueType(DateNextAction) "
			+ "WHERE Processed='N' AND IsActive='Y'";
		int no = DB.executeUpdate(sql);
		log.debug("runProcess - Active Requests updated=" + no);

		/**
		 *  send alerts
		 */
		if (vo.OverdueAlertDays != 0)
		{
			sql = "SELECT DocumentNo, Summary, AD_User_ID "
				+ "FROM R_Request "
				+ "WHERE AD_Client_ID=?"
				+ " AND TRUNC(DateNextAction)+" + vo.OverdueAlertDays + " > TRUNC(SysDate)";
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, vo.AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
				{
					String DocumentNo = rs.getString(1);
					String Summary = rs.getString(2);
					int AD_User_ID = rs.getInt(3);
					sendAlert (vo, DocumentNo, Summary, AD_User_ID);
				}
				rs.close();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error("runProcess (Alert): " + vo.Name, e);
			}
		}

		/**
		 *  Escalate
		 */
		if (vo.OverdueAssignDays != 0)
		{
			sql = "SELECT DocumentNo, Summary, AD_User_ID, R_Request_ID "
				+ "FROM R_Request "
				+ "WHERE AD_Client_ID=?"
				+ " AND IsEscalated='N'"    //  just once
				+ " AND TRUNC(DateNextAction)+" + vo.OverdueAssignDays + " > TRUNC(SysDate)";
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, vo.AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
				{

					String DocumentNo = rs.getString(1);
					String Summary = rs.getString(2);
					int AD_User_ID = rs.getInt(3);
					int R_Request_ID = rs.getInt(4);
					escalate (vo, DocumentNo, Summary, AD_User_ID, R_Request_ID);
				}
				rs.close();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error("runProcess (Escalate): " + vo.Name, e);
			}
		}


		/**
		 *  New NextRun
		 */
		String nextRun = "SysDate+" + vo.Frequency;				//  default (D)ays
		if (vo.FrequencyType.equals(vo.FREQUENCY_HOUR))			//  (H)our
			nextRun += "/24";
		else if (vo.FrequencyType.equals(vo.FREQUENCY_MINUTE))	//  (M)inute
			nextRun += "/1440";		//	60/24
		//
		String update = "UPDATE R_RequestProcessor "
			+ "SET DateLastRun=SysDate, DateNextRun=" + nextRun
			+ " WHERE R_RequestProcessor_ID=" + vo.R_RequestProcessor_ID;
		DB.executeUpdate(update);
		//	Read
		sql = "SELECT DateNextRun "
			+ "FROM R_RequestProcessor "
			+ "WHERE R_RequestProcessor_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.R_RequestProcessor_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				vo.DateNextRun = rs.getTimestamp(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("runProcess (NextRun): " + vo.Name, e);
		}
		log.debug("runProcess complete:" + vo.Name);
	}	//  runProcess

	/**
	 *  Send Alert EMail
	 *
	 *  @param vo Value Object
	 *  @param DocumentNo document no
	 *  @param Summary document summary
	 *  @param AD_User_ID user
	 */
	private void sendAlert (RequestProcessorVO vo,
		String DocumentNo, String Summary, int AD_User_ID)
	{
		String to = EMail.getEMailOfUser(AD_User_ID);
		//  Alert: Request {0} overdue
		String subject = Msg.getMsg(vo.AD_Language, "RequestAlert", new Object[] {DocumentNo});
		EMail email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
	}   //  sendAlert

	/**
	 *  Escalate
	 *
	 *  @param vo Value Object
	 *  @param DocumentNo document no
	 *  @param Summary document summary
	 *  @param AD_User_ID user
	 *  @param R_Request_ID request
	 */
	private void escalate (RequestProcessorVO vo,
		String DocumentNo, String Summary, int AD_User_ID, int R_Request_ID)
	{
		//  Get Supervisor
		int Supervisor_ID = -1;
		String sql = "SELECT Supervisor_ID from AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Supervisor_ID = rs.getInt(1);
				if (rs.wasNull())
					Supervisor_ID = -1;
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("escalate", e);
		}
		//  No one to escalate to
		if (Supervisor_ID == -1 || Supervisor_ID == vo.Supervisor_ID)
		{
			sql = "UPDATE R_Request SET IsEscalated='Y' WHERE R_Request_ID=" + R_Request_ID;
			DB.executeUpdate(sql);
			return;
		}

		//  ----------------

		//  Escalate: Request {0}
		String subject = Msg.getMsg(vo.AD_Language, "RequestEscalate", new Object[] {DocumentNo});
		sql = "UPDATE R_Request "
			+ "SET IsEscalated='Y', ActionType='T', AD_User_ID=" + Supervisor_ID
			+ ", Result='" + subject + "', Updated=SysDate, UpdatedBy=0 "
			+ "WHERE R_Request_ID=" + R_Request_ID;
		int no = DB.executeUpdate(sql);
		//  We updated Request, now process it.
		if (no == 1)
		{
			try
			{
				CallableStatement cstmt = DB.prepareCall("{CALL R_Request_Process(NULL,?)}");
				cstmt.setInt(1, R_Request_ID);
				cstmt.execute();
				cstmt.close();
			}
			catch (SQLException e)
			{
				log.error("escalate (process)", e);
			}
		}
		else
			log.error("escalate - Request not updated #=" + no);

		//  Send Email - current user
		String to = EMail.getEMailOfUser(AD_User_ID);
		EMail email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
		//  Send Email - new user
		to = EMail.getEMailOfUser(Supervisor_ID);
		email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
	}   //  escalate


	/*************************************************************************/

	/**
	 *  Create Request from Web Request
	 *  @param vo Value Object
	 */
	private void runWeb (RequestProcessorVO vo)
	{
		String sql = "SELECT * FROM W_Request "
			+ "WHERE AD_Client_ID=? AND Processed <> 'Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int AD_Client_ID = rs.getInt("AD_Client_ID");
				if (AD_Client_ID == 0 || AD_Client_ID == vo.AD_Client_ID)
					processWebRequest (vo, rs);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("runWeb", e);
		}
	}   //  runWeb

	/**
	 *  Process Web Request Record.
	 *  - create record
	 *  - assign based on rule
	 *
	 *  @param vo value object
	 *  @param rs W_Request
	 *  @throws SQLException
	 */
	private void processWebRequest (RequestProcessorVO vo, ResultSet rs)
		throws SQLException
	{
		int W_Request_ID = rs.getInt("W_Request_ID");

		StringBuffer sql = new StringBuffer ("INSERT INTO R_REQUEST "
			+ "(R_Request_ID,AD_Client_ID,AD_Org_ID, "
			+ "IsActive,Created,CreatedBy,Updated,UpdatedBy, "
			+ "DocumentNo,R_RequestType_ID,RequestAmt, "
			+ "Priority,DueType,Summary, "
			+ "IsEscalated,AD_User_ID,W_Request_ID, "
			+ "C_BPartner_ID,C_BPartner_Contact_ID, "
			+ "Result,Processed) VALUES (");
		//  (R_Request_ID, AD_Client_ID, AD_Org_ID,
		int R_Request_ID = DB.getKeyNextNo(vo.AD_Client_ID, "N", "R_Request");
		sql.append(R_Request_ID).append(",").append(vo.AD_Client_ID).append(",0, ");
		//  IsActive, Created, CreatedBy, Updated, UpdatedBy,
		sql.append("'Y',SysDate,0,SysDate,0, ");
		//  DocumentNo, R_RequestType_ID, RequestAmt,
		String docNo = DB.getDocumentNo(vo.AD_Client_ID, "N", "R_Request");
		sql.append(DB.TO_STRING(docNo)).append(",");
		Integer R_RequestType_ID = getRequestTypeID(vo, rs.getInt("R_RequestType_ID"));
		if (R_RequestType_ID == null)
			sql.append("null");
		else
			sql.append(R_RequestType_ID);
		sql.append(",0, ");
		//  Priority, DueType, Summary,
		String Question = rs.getString("Question");
		sql.append("'5','5',").append(DB.TO_STRING(Question)).append(", ");
		//  IsEscalated, AD_User_ID, W_Request_ID,
		int AD_User_ID = findUser (vo, Question);
		sql.append("'N',").append(AD_User_ID).append(",").append(W_Request_ID).append(", ");
		//  C_BPartner_ID, C_BPartner_Contact_ID,
		int C_BPartner_ID = rs.getInt("C_BPartner_ID");
		if (C_BPartner_ID == 0)
			sql.append("NULL,");
		else
			sql.append(C_BPartner_ID).append(",");
		int C_BPartner_Contact_ID = rs.getInt("C_BPartner_Contact_ID");
		if (C_BPartner_Contact_ID == 0)
			sql.append("NULL, ");
		else
			sql.append(C_BPartner_Contact_ID).append(", ");
		//  Result, Processed)
		sql.append("'Web','N')");
		//  Save
		int no = DB.executeUpdate(sql.toString());
		if (no == 1)
		{
			//  Set to processed and change Client for visibility
			sql = new StringBuffer ("UPDATE W_Request SET Processed='Y',AD_Client_ID=");
			sql.append(vo.AD_Client_ID).append(" WHERE W_Request_ID=").append(W_Request_ID);
			no = DB.executeUpdate(sql.toString());
			if (no != 1)
				log.error("processWebRequests - Web NOT updated #=" + no);
		}
		else
			log.error("processWebRequests - Request NOT created #=" + no);
	}   //  processWebRequesr

	/**
	 *  Find User
	 *
	 *  @param vo value object
	 *  @param Info info
	 *  @return AD_User_ID user
	 */
	private int findUser (RequestProcessorVO vo, String Info)
	{
		return vo.Supervisor_ID;
	}   //  findUser

	/**
	 *  Get RequestType.
	 *
	 *  @param vo value object
	 *  @param R_RequestType_ID initial value
	 *  @return R_RequestType_ID or default
	 */
	private Integer getRequestTypeID (RequestProcessorVO vo, int R_RequestType_ID)
	{
		if (R_RequestType_ID > 0)
			return new Integer (R_RequestType_ID);

		//	Get Default
		String sql = "SELECT R_RequestType_ID "
			+ "FROM R_RequestType "
			+ "WHERE AD_Client_ID=? AND IsDefault='Y'";
		Integer retValue = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = new Integer (rs.getInt(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("getRequestTypeID", e);
		}
		return retValue;
	}   //  getRequestTypeID

	/*************************************************************************/

	/**
	 *
	 *  @param vo Value Object
	 */
	private void runEMail (RequestProcessorVO vo)
	{
	}   //  runWeb

	/*************************************************************************/

	/**
	 *  Test Run
	 *  @param args ignored
	 */
	public static void main(String[] args)
	{
		org.compiere.Compiere.startupClient ();
		RequestProcessorVO[] vo = RequestProcessorVO.get();
		if (vo != null && vo.length > 0)
			new RequestProcessor (vo[0]);
	}   //  main

}   //  RequestProcessor
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
package org.compiere.request;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Msg;


/**
 *  Request Processor.
 *
 *  @author     Jorg Janke
 *  @version    $Id: RequestProcessor.java,v 1.4 2002/11/18 06:11:58 jjanke Exp $
 */
public class RequestProcessor
{
	/**
	 *	Constructor
	 *  @param vo value object
	 */
	public RequestProcessor (RequestProcessorVO vo)
	{
		long startTime = System.currentTimeMillis();
		runProcess (vo);
		//  read new web requests
		runWeb (vo);
		//  read new emails
		runEMail (vo);
		//
		long msec = System.currentTimeMillis() - startTime;
		double sec = (double)msec / 1000;
		log.info("RequestProcessor " + vo.Name + " - " + sec + " sec");
	}	//	RequestProcessor

	/**	Logger					*/
	private static Logger	log = Logger.getLogger(RequestProcessor.class);

	/*************************************************************************/

	/**
	 *  Process requests.
	 *  - update requests
	 *  - send alerts
	 *  - escalate alerts
	 *  - Set time next run
	 *
	 *  @param vo Value Object
	 */
	private void runProcess (RequestProcessorVO vo)
	{
		//  Run only when scheduled
		if (vo.DateNextRun != null && vo.DateNextRun.getTime() > System.currentTimeMillis())
			return;

		/**
		 *  update DueType of all requests
		 */
		String sql = "UPDATE R_Request "
			+ "SET DueType=R_Request_DueType(DateNextAction) "
			+ "WHERE Processed='N' AND IsActive='Y'";
		int no = DB.executeUpdate(sql);
		log.debug("runProcess - Active Requests updated=" + no);

		/**
		 *  send alerts
		 */
		if (vo.OverdueAlertDays != 0)
		{
			sql = "SELECT DocumentNo, Summary, AD_User_ID "
				+ "FROM R_Request "
				+ "WHERE AD_Client_ID=?"
				+ " AND TRUNC(DateNextAction)+" + vo.OverdueAlertDays + " > TRUNC(SysDate)";
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, vo.AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
				{
					String DocumentNo = rs.getString(1);
					String Summary = rs.getString(2);
					int AD_User_ID = rs.getInt(3);
					sendAlert (vo, DocumentNo, Summary, AD_User_ID);
				}
				rs.close();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error("runProcess (Alert): " + vo.Name, e);
			}
		}

		/**
		 *  Escalate
		 */
		if (vo.OverdueAssignDays != 0)
		{
			sql = "SELECT DocumentNo, Summary, AD_User_ID, R_Request_ID "
				+ "FROM R_Request "
				+ "WHERE AD_Client_ID=?"
				+ " AND IsEscalated='N'"    //  just once
				+ " AND TRUNC(DateNextAction)+" + vo.OverdueAssignDays + " > TRUNC(SysDate)";
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, vo.AD_Client_ID);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
				{

					String DocumentNo = rs.getString(1);
					String Summary = rs.getString(2);
					int AD_User_ID = rs.getInt(3);
					int R_Request_ID = rs.getInt(4);
					escalate (vo, DocumentNo, Summary, AD_User_ID, R_Request_ID);
				}
				rs.close();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error("runProcess (Escalate): " + vo.Name, e);
			}
		}


		/**
		 *  New NextRun
		 */
		String nextRun = "SysDate+" + vo.Frequency;				//  default (D)ays
		if (vo.FrequencyType.equals(vo.FREQUENCY_HOUR))			//  (H)our
			nextRun += "/24";
		else if (vo.FrequencyType.equals(vo.FREQUENCY_MINUTE))	//  (M)inute
			nextRun += "/1440";		//	60/24
		//
		String update = "UPDATE R_RequestProcessor "
			+ "SET DateLastRun=SysDate, DateNextRun=" + nextRun
			+ " WHERE R_RequestProcessor_ID=" + vo.R_RequestProcessor_ID;
		DB.executeUpdate(update);
		//	Read
		sql = "SELECT DateNextRun "
			+ "FROM R_RequestProcessor "
			+ "WHERE R_RequestProcessor_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.R_RequestProcessor_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				vo.DateNextRun = rs.getTimestamp(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("runProcess (NextRun): " + vo.Name, e);
		}
		log.debug("runProcess complete:" + vo.Name);
	}	//  runProcess

	/**
	 *  Send Alert EMail
	 *
	 *  @param vo Value Object
	 *  @param DocumentNo document no
	 *  @param Summary document summary
	 *  @param AD_User_ID user
	 */
	private void sendAlert (RequestProcessorVO vo,
		String DocumentNo, String Summary, int AD_User_ID)
	{
		String to = EMail.getEMailOfUser(AD_User_ID);
		//  Alert: Request {0} overdue
		String subject = Msg.getMsg(vo.AD_Language, "RequestAlert", new Object[] {DocumentNo});
		EMail email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
	}   //  sendAlert

	/**
	 *  Escalate
	 *
	 *  @param vo Value Object
	 *  @param DocumentNo document no
	 *  @param Summary document summary
	 *  @param AD_User_ID user
	 *  @param R_Request_ID request
	 */
	private void escalate (RequestProcessorVO vo,
		String DocumentNo, String Summary, int AD_User_ID, int R_Request_ID)
	{
		//  Get Supervisor
		int Supervisor_ID = -1;
		String sql = "SELECT Supervisor_ID from AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Supervisor_ID = rs.getInt(1);
				if (rs.wasNull())
					Supervisor_ID = -1;
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("escalate", e);
		}
		//  No one to escalate to
		if (Supervisor_ID == -1 || Supervisor_ID == vo.Supervisor_ID)
		{
			sql = "UPDATE R_Request SET IsEscalated='Y' WHERE R_Request_ID=" + R_Request_ID;
			DB.executeUpdate(sql);
			return;
		}

		//  ----------------

		//  Escalate: Request {0}
		String subject = Msg.getMsg(vo.AD_Language, "RequestEscalate", new Object[] {DocumentNo});
		sql = "UPDATE R_Request "
			+ "SET IsEscalated='Y', ActionType='T', AD_User_ID=" + Supervisor_ID
			+ ", Result='" + subject + "', Updated=SysDate, UpdatedBy=0 "
			+ "WHERE R_Request_ID=" + R_Request_ID;
		int no = DB.executeUpdate(sql);
		//  We updated Request, now process it.
		if (no == 1)
		{
			try
			{
				CallableStatement cstmt = DB.prepareCall("{CALL R_Request_Process(NULL,?)}");
				cstmt.setInt(1, R_Request_ID);
				cstmt.execute();
				cstmt.close();
			}
			catch (SQLException e)
			{
				log.error("escalate (process)", e);
			}
		}
		else
			log.error("escalate - Request not updated #=" + no);

		//  Send Email - current user
		String to = EMail.getEMailOfUser(AD_User_ID);
		EMail email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
		//  Send Email - new user
		to = EMail.getEMailOfUser(Supervisor_ID);
		email = new EMail (vo.SMTPHost, vo.RequestEMail, to, subject, Summary);
		email.send();
	}   //  escalate


	/*************************************************************************/

	/**
	 *  Create Request from Web Request
	 *  @param vo Value Object
	 */
	private void runWeb (RequestProcessorVO vo)
	{
		String sql = "SELECT * FROM W_Request "
			+ "WHERE AD_Client_ID=? AND Processed <> 'Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int AD_Client_ID = rs.getInt("AD_Client_ID");
				if (AD_Client_ID == 0 || AD_Client_ID == vo.AD_Client_ID)
					processWebRequest (vo, rs);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("runWeb", e);
		}
	}   //  runWeb

	/**
	 *  Process Web Request Record.
	 *  - create record
	 *  - assign based on rule
	 *
	 *  @param vo value object
	 *  @param rs W_Request
	 *  @throws SQLException
	 */
	private void processWebRequest (RequestProcessorVO vo, ResultSet rs)
		throws SQLException
	{
		int W_Request_ID = rs.getInt("W_Request_ID");

		StringBuffer sql = new StringBuffer ("INSERT INTO R_REQUEST "
			+ "(R_Request_ID,AD_Client_ID,AD_Org_ID, "
			+ "IsActive,Created,CreatedBy,Updated,UpdatedBy, "
			+ "DocumentNo,R_RequestType_ID,RequestAmt, "
			+ "Priority,DueType,Summary, "
			+ "IsEscalated,AD_User_ID,W_Request_ID, "
			+ "C_BPartner_ID,C_BPartner_Contact_ID, "
			+ "Result,Processed) VALUES (");
		//  (R_Request_ID, AD_Client_ID, AD_Org_ID,
		int R_Request_ID = DB.getKeyNextNo(vo.AD_Client_ID, "N", "R_Request");
		sql.append(R_Request_ID).append(",").append(vo.AD_Client_ID).append(",0, ");
		//  IsActive, Created, CreatedBy, Updated, UpdatedBy,
		sql.append("'Y',SysDate,0,SysDate,0, ");
		//  DocumentNo, R_RequestType_ID, RequestAmt,
		String docNo = DB.getDocumentNo(vo.AD_Client_ID, "N", "R_Request");
		sql.append(DB.TO_STRING(docNo)).append(",");
		Integer R_RequestType_ID = getRequestTypeID(vo, rs.getInt("R_RequestType_ID"));
		if (R_RequestType_ID == null)
			sql.append("null");
		else
			sql.append(R_RequestType_ID);
		sql.append(",0, ");
		//  Priority, DueType, Summary,
		String Question = rs.getString("Question");
		sql.append("'5','5',").append(DB.TO_STRING(Question)).append(", ");
		//  IsEscalated, AD_User_ID, W_Request_ID,
		int AD_User_ID = findUser (vo, Question);
		sql.append("'N',").append(AD_User_ID).append(",").append(W_Request_ID).append(", ");
		//  C_BPartner_ID, C_BPartner_Contact_ID,
		int C_BPartner_ID = rs.getInt("C_BPartner_ID");
		if (C_BPartner_ID == 0)
			sql.append("NULL,");
		else
			sql.append(C_BPartner_ID).append(",");
		int C_BPartner_Contact_ID = rs.getInt("C_BPartner_Contact_ID");
		if (C_BPartner_Contact_ID == 0)
			sql.append("NULL, ");
		else
			sql.append(C_BPartner_Contact_ID).append(", ");
		//  Result, Processed)
		sql.append("'Web','N')");
		//  Save
		int no = DB.executeUpdate(sql.toString());
		if (no == 1)
		{
			//  Set to processed and change Client for visibility
			sql = new StringBuffer ("UPDATE W_Request SET Processed='Y',AD_Client_ID=");
			sql.append(vo.AD_Client_ID).append(" WHERE W_Request_ID=").append(W_Request_ID);
			no = DB.executeUpdate(sql.toString());
			if (no != 1)
				log.error("processWebRequests - Web NOT updated #=" + no);
		}
		else
			log.error("processWebRequests - Request NOT created #=" + no);
	}   //  processWebRequesr

	/**
	 *  Find User
	 *
	 *  @param vo value object
	 *  @param Info info
	 *  @return AD_User_ID user
	 */
	private int findUser (RequestProcessorVO vo, String Info)
	{
		return vo.Supervisor_ID;
	}   //  findUser

	/**
	 *  Get RequestType.
	 *
	 *  @param vo value object
	 *  @param R_RequestType_ID initial value
	 *  @return R_RequestType_ID or default
	 */
	private Integer getRequestTypeID (RequestProcessorVO vo, int R_RequestType_ID)
	{
		if (R_RequestType_ID > 0)
			return new Integer (R_RequestType_ID);

		//	Get Default
		String sql = "SELECT R_RequestType_ID "
			+ "FROM R_RequestType "
			+ "WHERE AD_Client_ID=? AND IsDefault='Y'";
		Integer retValue = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, vo.AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = new Integer (rs.getInt(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error("getRequestTypeID", e);
		}
		return retValue;
	}   //  getRequestTypeID

	/*************************************************************************/

	/**
	 *
	 *  @param vo Value Object
	 */
	private void runEMail (RequestProcessorVO vo)
	{
	}   //  runWeb

	/*************************************************************************/

	/**
	 *  Test Run
	 *  @param args ignored
	 */
	public static void main(String[] args)
	{
		org.compiere.Compiere.startupClient ();
		RequestProcessorVO[] vo = RequestProcessorVO.get();
		if (vo != null && vo.length > 0)
			new RequestProcessor (vo[0]);
	}   //  main

}   //  RequestProcessor
