/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
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
 *	Sequence Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MSequence.java,v 1.9 2004/05/20 05:59:11 jjanke Exp $
 */
public class MSequence extends X_AD_Sequence
{
	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getNextID - TableName missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getNextID - " + TableName + " - CompiereSys=" + compiereSys + " - " + trxName);

		//
		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y'"
			+ " FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, TableName);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getNextID - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				if (compiereSys)
				{
					retValue = rs.getInt(2);
					rs.updateInt(2, retValue + IncrementNo);
				}
				else
				{
					retValue = rs.getInt(1);
					rs.updateInt(1, retValue + IncrementNo);
				}
				rs.updateRow();
			}
			else
				s_log.error ("getNextID - no record found - " + TableName);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getNextID", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getNextID - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(9))
			s_log.debug ("getNextID - " + retValue + " - Table=" + TableName + " - " + trx);
		return retValue;
	}	//	getNextID


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - TableName missing");
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - " + TableName + " - CompiereSys=" + compiereSys  + " - " + trxName);

		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND AD_Client_ID IN (0,?)"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' "
			+ "ORDER BY AD_Client_ID DESC "
			+ "FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		int IncrementNo = 0;
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, PREFIX_DOCSEQ + TableName);
			pstmt.setInt(2, AD_Client_ID);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
			{
				s_log.warn ("getDocumentNo (Table) - no record found - " + TableName);
				MSequence seq = new MSequence (Env.getCtx(), AD_Client_ID, TableName);
				documentNo = seq.getDocumentNo();
				seq.save();
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " (" + IncrementNo + ")" 
				+ " - Table=" + TableName + " - Trx=" + trx);
		return documentNo;
	}	//	getDocumentNo
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param AD_Client_ID client
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int C_DocType_ID, String trxName)
	{
		if (C_DocType_ID == 0)
		{
			s_log.error ("getDocumentNo - C_DocType_ID=0");
			return null;
		}
		MDocType dt = MDocType.get (Env.getCtx(), C_DocType_ID);	//	wrong for SERVER, but r/o
		if (dt != null && !dt.isDocNoControlled())
		{
			if (Log.isTraceLevel(6))
				s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " Not DocNo controlled");
			return null;
		}
		if (dt == null || dt.getDocNoSequence_ID() == 0)
		{
			s_log.warn ("getDocumentNo - No Sequence for DocType - " + dt);
			return null;
		}
			
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " - " + trxName);
		
		String select = "SELECT	CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Client_ID "
			+ "FROM AD_Sequence "
			+ "WHERE AD_Sequence_ID=?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y'"
			+ " FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setInt(1, dt.getDocNoSequence_ID());
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				int IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				int AD_Client_ID = rs.getInt(6);
				if (compiereSys && AD_Client_ID > 11)
					compiereSys = false;
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
				s_log.warn ("getDocumentNo (DocType)- no record found - " + dt);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " - C_DocType_ID=" + C_DocType_ID + " - " + trx);
		return documentNo;
	}	//	getDocumentNo

	
	
	/**
	 *	Check/Initialize Client DocumentNo/Value Sequences 	
	 *	@param ctx context
	 *	@param AD_Client_ID client
	 *	@return true if no error
	 */
	public static boolean checkClientSequences (Properties ctx, int AD_Client_ID)
	{
		String sql = "SELECT TableName "
			+ "FROM AD_Table t "
			+ "WHERE IsActive='Y'"
			//	Get all Tables with DocumentNo or Value
			+ " AND AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column "
				+ "WHERE ColumnName = 'DocumentNo' OR ColumnName = 'Value')"
			//	Ability to run multiple times
			+ " AND 'DocumentNo_' || TableName NOT IN (SELECT Name FROM AD_Sequence s "
				+ "WHERE s.AD_Client_ID = ?)";		//	#1
		int counter = 0;
		boolean success = true;
		//
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				MSequence seq = new MSequence (ctx, AD_Client_ID, tableName);
				if (seq.save())
					counter++;
				else
				{
					s_log.error ("checkClientSequences - Not created "
						+ " - AD_Client_ID=" + AD_Client_ID
						+ " - "  + tableName);
					success = false;
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("checkClientSequences", e);
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
		s_log.info ("checkClientSequences - AD_Client_ID=" + AD_Client_ID 
			+ " - created #" + counter
			+ " - success=" + success);
		return success;
	}	//	checkClientSequences
	

	/**
	 * 	Create Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 * 	@return true if created
	 */
	public static boolean createTableSequence (Properties ctx, String TableName)
	{
		MSequence seq = new MSequence (ctx, 0);
		seq.setName(TableName);
		seq.setDescription("Table " + TableName);
		seq.setIsTableID(true);
		return seq.save();
	}	//	createTableSequence
	
	/**	Sequence for Table Document No's	*/
	private static final String	PREFIX_DOCSEQ = "DocumentNo_";
	/**	Start Number			*/
	public static final int		INIT_NO = 1000000;	//	1 Mio
	/**	Start System Number		*/
	public static final int		INIT_SYS_NO = 100;	
	/** Static Logger			*/
	private static Logger 		s_log = Logger.getCLogger(MSequence.class);
	
	
	/**************************************************************************
	 *	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Sequence_ID id
	 */
	public MSequence (Properties ctx, int AD_Sequence_ID)
	{
		super(ctx, AD_Sequence_ID);
		if (AD_Sequence_ID == 0)
		{
		//	setName (null);
			//
			setIsTableID(false);
			setStartNo (INIT_NO);
			setCurrentNext (INIT_NO);
			setCurrentNextSys (INIT_SYS_NO);
			setIncrementNo (1);
			setIsAutoSequence (true);
			setIsAudited(false);
			setStartNewYear(false);
		}
	}	//	MSequence

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MSequence (Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MSequence

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String tableName)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(PREFIX_DOCSEQ + tableName);
		setDescription("DocumentNo/Value for Table " + tableName);
	}	//	MSequence;
	
	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String sequenceName, int StartNo)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(sequenceName);
		setDescription(sequenceName);
		setStartNo(StartNo);
		setCurrentNext(StartNo);
		setCurrentNextSys(StartNo/10);
	}	//	MSequence;
	
	
	/**************************************************************************
	 * 	Get Next No and increase current next
	 *	@return next no to use
	 */
	public int getNextID()
	{
		int retValue = getCurrentNext();
		setCurrentNext(retValue + getIncrementNo());
		return retValue;
	}	//	getNextNo
	
	/**
	 * 	Get next DocumentNo
	 *	@return document no
	 */
	public String getDocumentNo()
	{
		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(getNextID());
		String suffix = getSuffix();
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		return doc.toString();
	}	//	getDocumentNo
	
	
	/**************************************************************************
	 *	Test
	 *	@param args ignored
	 */
	static public void main (String[] args)
	{
		org.compiere.Compiere.startupClient();
		int AD_Client_ID = 0;
		int C_DocType_ID = 115;	//	GL
		String TableName = "C_Invoice";
		String trxName = "x";
		Trx trx = Trx.get(trxName);		

				
		trx = Trx.get(trxName);		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getNextID (0, "Test", trxName));
		System.out.println ("trx2 " + getNextID (0, "Test", trxName));
		trx.rollback();
		System.out.println ("trx3 " + getNextID (0, "Test", trxName));
		trx.commit();
		System.out.println ("trx4 " + getNextID (0, "Test", trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("==============================================");		
		

		trx = Trx.get(trxName);		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		System.out.println ("trx2 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("==============================================");		


		trx = Trx.get(trxName);		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(C_DocType_ID, trxName));
		System.out.println ("trx2 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(C_DocType_ID, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
	//	System.out.println ("==============================================");		

	}	//	args

}	//	MSequence
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
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
 *	Sequence Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MSequence.java,v 1.13 2004/09/09 14:16:50 jjanke Exp $
 */
public class MSequence extends X_AD_Sequence
{
	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getNextID - TableName missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getNextID - " + TableName + " - CompiereSys=" + compiereSys + " - " + trxName);

		//
		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y'"
			+ " FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, TableName);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getNextID - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				if (compiereSys)
				{
					retValue = rs.getInt(2);
					rs.updateInt(2, retValue + IncrementNo);
				}
				else
				{
					retValue = rs.getInt(1);
					rs.updateInt(1, retValue + IncrementNo);
				}
				rs.updateRow();
			}
			else
				s_log.error ("getNextID - no record found - " + TableName);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getNextID", e);
			retValue = -1;
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getNextID - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(9))
			s_log.debug ("getNextID - " + retValue + " - Table=" + TableName + " - " + trx);
		return retValue;
	}	//	getNextID


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - TableName missing");
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - " + TableName + " - CompiereSys=" + compiereSys  + " - " + trxName);

		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND AD_Client_ID IN (0,?)"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' "
			+ "ORDER BY AD_Client_ID DESC "
			+ "FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		int IncrementNo = 0;
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, PREFIX_DOCSEQ + TableName);
			pstmt.setInt(2, AD_Client_ID);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
			{
				s_log.warn ("getDocumentNo (Table) - no record found - " + TableName);
				MSequence seq = new MSequence (Env.getCtx(), AD_Client_ID, TableName);
				documentNo = seq.getDocumentNo();
				seq.save();
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " (" + IncrementNo + ")" 
				+ " - Table=" + TableName + " - Trx=" + trx);
		return documentNo;
	}	//	getDocumentNo
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param AD_Client_ID client
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int C_DocType_ID, String trxName)
	{
		if (C_DocType_ID == 0)
		{
			s_log.error ("getDocumentNo - C_DocType_ID=0");
			return null;
		}
		MDocType dt = MDocType.get (Env.getCtx(), C_DocType_ID);	//	wrong for SERVER, but r/o
		if (dt != null && !dt.isDocNoControlled())
		{
			if (Log.isTraceLevel(6))
				s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " Not DocNo controlled");
			return null;
		}
		if (dt == null || dt.getDocNoSequence_ID() == 0)
		{
			s_log.warn ("getDocumentNo - No Sequence for DocType - " + dt);
			return null;
		}
			
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " - " + trxName);
		
		String select = "SELECT	CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Client_ID "
			+ "FROM AD_Sequence "
			+ "WHERE AD_Sequence_ID=?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y'"
			+ " FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setInt(1, dt.getDocNoSequence_ID());
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				int IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				int AD_Client_ID = rs.getInt(6);
				if (compiereSys && AD_Client_ID > 11)
					compiereSys = false;
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
				s_log.warn ("getDocumentNo (DocType)- no record found - " + dt);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " - C_DocType_ID=" + C_DocType_ID + " - " + trx);
		return documentNo;
	}	//	getDocumentNo

	
	
	/**
	 *	Check/Initialize Client DocumentNo/Value Sequences 	
	 *	@param ctx context
	 *	@param AD_Client_ID client
	 *	@return true if no error
	 */
	public static boolean checkClientSequences (Properties ctx, int AD_Client_ID)
	{
		String sql = "SELECT TableName "
			+ "FROM AD_Table t "
			+ "WHERE IsActive='Y'"
			//	Get all Tables with DocumentNo or Value
			+ " AND AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column "
				+ "WHERE ColumnName = 'DocumentNo' OR ColumnName = 'Value')"
			//	Ability to run multiple times
			+ " AND 'DocumentNo_' || TableName NOT IN (SELECT Name FROM AD_Sequence s "
				+ "WHERE s.AD_Client_ID = ?)";		//	#1
		int counter = 0;
		boolean success = true;
		//
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				MSequence seq = new MSequence (ctx, AD_Client_ID, tableName);
				if (seq.save())
					counter++;
				else
				{
					s_log.error ("checkClientSequences - Not created "
						+ " - AD_Client_ID=" + AD_Client_ID
						+ " - "  + tableName);
					success = false;
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("checkClientSequences", e);
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
		s_log.info ("checkClientSequences - AD_Client_ID=" + AD_Client_ID 
			+ " - created #" + counter
			+ " - success=" + success);
		return success;
	}	//	checkClientSequences
	

	/**
	 * 	Create Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 * 	@return true if created
	 */
	public static boolean createTableSequence (Properties ctx, String TableName)
	{
		MSequence seq = new MSequence (ctx, 0);
		seq.setName(TableName);
		seq.setDescription("Table " + TableName);
		seq.setIsTableID(true);
		return seq.save();
	}	//	createTableSequence
	
	/**	Sequence for Table Document No's	*/
	private static final String	PREFIX_DOCSEQ = "DocumentNo_";
	/**	Start Number			*/
	public static final int		INIT_NO = 1000000;	//	1 Mio
	/**	Start System Number		*/
	public static final int		INIT_SYS_NO = 100;	
	/** Static Logger			*/
	private static Logger 		s_log = Logger.getCLogger(MSequence.class);
	
	
	/**************************************************************************
	 *	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Sequence_ID id
	 */
	public MSequence (Properties ctx, int AD_Sequence_ID)
	{
		super(ctx, AD_Sequence_ID);
		if (AD_Sequence_ID == 0)
		{
		//	setName (null);
			//
			setIsTableID(false);
			setStartNo (INIT_NO);
			setCurrentNext (INIT_NO);
			setCurrentNextSys (INIT_SYS_NO);
			setIncrementNo (1);
			setIsAutoSequence (true);
			setIsAudited(false);
			setStartNewYear(false);
		}
	}	//	MSequence

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MSequence (Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MSequence

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String tableName)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(PREFIX_DOCSEQ + tableName);
		setDescription("DocumentNo/Value for Table " + tableName);
	}	//	MSequence;
	
	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String sequenceName, int StartNo)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(sequenceName);
		setDescription(sequenceName);
		setStartNo(StartNo);
		setCurrentNext(StartNo);
		setCurrentNextSys(StartNo/10);
	}	//	MSequence;
	
	
	/**************************************************************************
	 * 	Get Next No and increase current next
	 *	@return next no to use
	 */
	public int getNextID()
	{
		int retValue = getCurrentNext();
		setCurrentNext(retValue + getIncrementNo());
		return retValue;
	}	//	getNextNo
	
	/**
	 * 	Get next DocumentNo
	 *	@return document no
	 */
	public String getDocumentNo()
	{
		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(getNextID());
		String suffix = getSuffix();
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		return doc.toString();
	}	//	getDocumentNo
	
	
	/**************************************************************************
	 *	Test
	 *	@param args ignored
	 */
	static public void main (String[] args)
	{
		org.compiere.Compiere.startupClient();
		
		try
		{
			int retValue = -1;
		//	Connection conn = DB.getConnectionRW ();
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			Connection conn = DriverManager.getConnection ("jdbc:oracle:thin:@dev2:1521:dev2", "compiere", "compiere");

		//	conn.setAutoCommit(false);
			String sql = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
				+ "FROM AD_Sequence "
				+ "WHERE Name='AD_Sequence' "
				+ "FOR UPDATE OF CurrentNext, CurrentNextSys";
			

			PreparedStatement pstmt = conn.prepareStatement(sql,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = pstmt.executeQuery();
			System.out.println(" AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
				+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				retValue = rs.getInt(1);
				rs.updateInt(1, retValue + IncrementNo);
				rs.updateRow();
			}
			else
				s_log.error ("no record found");
			rs.close();
			pstmt.close();
			System.out.println("Next=" + retValue);
			
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		
		
		
		
		
System.exit(0);		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		int AD_Client_ID = 0;
		int C_DocType_ID = 115;	//	GL
		String TableName = "C_Invoice";
		String trxName = "x";
		Trx trx = Trx.get(trxName, true);		

				
		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getNextID (0, "Test", trxName));
		System.out.println ("trx2 " + getNextID (0, "Test", trxName));
		trx.rollback();
		System.out.println ("trx3 " + getNextID (0, "Test", trxName));
		trx.commit();
		System.out.println ("trx4 " + getNextID (0, "Test", trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("==============================================");		
		

		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		System.out.println ("trx2 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("==============================================");		


		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(C_DocType_ID, trxName));
		System.out.println ("trx2 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(C_DocType_ID, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
	//	System.out.println ("==============================================");		

	}	//	args

}	//	MSequence
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
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
 *	Sequence Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MSequence.java,v 1.9 2004/05/20 05:59:11 jjanke Exp $
 */
public class MSequence extends X_AD_Sequence
{
	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getNextID - TableName missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getNextID - " + TableName + " - CompiereSys=" + compiereSys + " - " + trxName);

		//
		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y'"
			+ " FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, TableName);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getNextID - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				if (compiereSys)
				{
					retValue = rs.getInt(2);
					rs.updateInt(2, retValue + IncrementNo);
				}
				else
				{
					retValue = rs.getInt(1);
					rs.updateInt(1, retValue + IncrementNo);
				}
				rs.updateRow();
			}
			else
				s_log.error ("getNextID - no record found - " + TableName);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getNextID", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getNextID - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(9))
			s_log.debug ("getNextID - " + retValue + " - Table=" + TableName + " - " + trx);
		return retValue;
	}	//	getNextID


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - TableName missing");
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - " + TableName + " - CompiereSys=" + compiereSys  + " - " + trxName);

		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND AD_Client_ID IN (0,?)"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' "
			+ "ORDER BY AD_Client_ID DESC "
			+ "FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		int IncrementNo = 0;
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, PREFIX_DOCSEQ + TableName);
			pstmt.setInt(2, AD_Client_ID);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
			{
				s_log.warn ("getDocumentNo (Table) - no record found - " + TableName);
				MSequence seq = new MSequence (Env.getCtx(), AD_Client_ID, TableName);
				documentNo = seq.getDocumentNo();
				seq.save();
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " (" + IncrementNo + ")" 
				+ " - Table=" + TableName + " - Trx=" + trx);
		return documentNo;
	}	//	getDocumentNo
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param AD_Client_ID client
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int C_DocType_ID, String trxName)
	{
		if (C_DocType_ID == 0)
		{
			s_log.error ("getDocumentNo - C_DocType_ID=0");
			return null;
		}
		MDocType dt = MDocType.get (Env.getCtx(), C_DocType_ID);	//	wrong for SERVER, but r/o
		if (dt != null && !dt.isDocNoControlled())
		{
			if (Log.isTraceLevel(6))
				s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " Not DocNo controlled");
			return null;
		}
		if (dt == null || dt.getDocNoSequence_ID() == 0)
		{
			s_log.warn ("getDocumentNo - No Sequence for DocType - " + dt);
			return null;
		}
			
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " - " + trxName);
		
		String select = "SELECT	CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Client_ID "
			+ "FROM AD_Sequence "
			+ "WHERE AD_Sequence_ID=?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y'"
			+ " FOR UPDATE OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setInt(1, dt.getDocNoSequence_ID());
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				int IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				int AD_Client_ID = rs.getInt(6);
				if (compiereSys && AD_Client_ID > 11)
					compiereSys = false;
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
				s_log.warn ("getDocumentNo (DocType)- no record found - " + dt);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " - C_DocType_ID=" + C_DocType_ID + " - " + trx);
		return documentNo;
	}	//	getDocumentNo

	
	
	/**
	 *	Check/Initialize Client DocumentNo/Value Sequences 	
	 *	@param ctx context
	 *	@param AD_Client_ID client
	 *	@return true if no error
	 */
	public static boolean checkClientSequences (Properties ctx, int AD_Client_ID)
	{
		String sql = "SELECT TableName "
			+ "FROM AD_Table t "
			+ "WHERE IsActive='Y'"
			//	Get all Tables with DocumentNo or Value
			+ " AND AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column "
				+ "WHERE ColumnName = 'DocumentNo' OR ColumnName = 'Value')"
			//	Ability to run multiple times
			+ " AND 'DocumentNo_' || TableName NOT IN (SELECT Name FROM AD_Sequence s "
				+ "WHERE s.AD_Client_ID = ?)";		//	#1
		int counter = 0;
		boolean success = true;
		//
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				MSequence seq = new MSequence (ctx, AD_Client_ID, tableName);
				if (seq.save())
					counter++;
				else
				{
					s_log.error ("checkClientSequences - Not created "
						+ " - AD_Client_ID=" + AD_Client_ID
						+ " - "  + tableName);
					success = false;
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("checkClientSequences", e);
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
		s_log.info ("checkClientSequences - AD_Client_ID=" + AD_Client_ID 
			+ " - created #" + counter
			+ " - success=" + success);
		return success;
	}	//	checkClientSequences
	

	/**
	 * 	Create Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 * 	@return true if created
	 */
	public static boolean createTableSequence (Properties ctx, String TableName)
	{
		MSequence seq = new MSequence (ctx, 0);
		seq.setName(TableName);
		seq.setDescription("Table " + TableName);
		seq.setIsTableID(true);
		return seq.save();
	}	//	createTableSequence
	
	/**	Sequence for Table Document No's	*/
	private static final String	PREFIX_DOCSEQ = "DocumentNo_";
	/**	Start Number			*/
	public static final int		INIT_NO = 1000000;	//	1 Mio
	/**	Start System Number		*/
	public static final int		INIT_SYS_NO = 100;	
	/** Static Logger			*/
	private static Logger 		s_log = Logger.getCLogger(MSequence.class);
	
	
	/**************************************************************************
	 *	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Sequence_ID id
	 */
	public MSequence (Properties ctx, int AD_Sequence_ID)
	{
		super(ctx, AD_Sequence_ID);
		if (AD_Sequence_ID == 0)
		{
		//	setName (null);
			//
			setIsTableID(false);
			setStartNo (INIT_NO);
			setCurrentNext (INIT_NO);
			setCurrentNextSys (INIT_SYS_NO);
			setIncrementNo (1);
			setIsAutoSequence (true);
			setIsAudited(false);
			setStartNewYear(false);
		}
	}	//	MSequence

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MSequence (Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MSequence

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String tableName)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(PREFIX_DOCSEQ + tableName);
		setDescription("DocumentNo/Value for Table " + tableName);
	}	//	MSequence;
	
	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String sequenceName, int StartNo)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(sequenceName);
		setDescription(sequenceName);
		setStartNo(StartNo);
		setCurrentNext(StartNo);
		setCurrentNextSys(StartNo/10);
	}	//	MSequence;
	
	
	/**************************************************************************
	 * 	Get Next No and increase current next
	 *	@return next no to use
	 */
	public int getNextID()
	{
		int retValue = getCurrentNext();
		setCurrentNext(retValue + getIncrementNo());
		return retValue;
	}	//	getNextNo
	
	/**
	 * 	Get next DocumentNo
	 *	@return document no
	 */
	public String getDocumentNo()
	{
		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(getNextID());
		String suffix = getSuffix();
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		return doc.toString();
	}	//	getDocumentNo
	
	
	/**************************************************************************
	 *	Test
	 *	@param args ignored
	 */
	static public void main (String[] args)
	{
		org.compiere.Compiere.startupClient();
		int AD_Client_ID = 0;
		int C_DocType_ID = 115;	//	GL
		String TableName = "C_Invoice";
		String trxName = "x";
		Trx trx = Trx.get(trxName);		

				
		trx = Trx.get(trxName);		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getNextID (0, "Test", trxName));
		System.out.println ("trx2 " + getNextID (0, "Test", trxName));
		trx.rollback();
		System.out.println ("trx3 " + getNextID (0, "Test", trxName));
		trx.commit();
		System.out.println ("trx4 " + getNextID (0, "Test", trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("==============================================");		
		

		trx = Trx.get(trxName);		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		System.out.println ("trx2 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("==============================================");		


		trx = Trx.get(trxName);		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(C_DocType_ID, trxName));
		System.out.println ("trx2 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(C_DocType_ID, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
	//	System.out.println ("==============================================");		

	}	//	args

}	//	MSequence
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
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
 *	Sequence Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MSequence.java,v 1.13 2004/09/09 14:16:50 jjanke Exp $
 */
public class MSequence extends X_AD_Sequence
{
	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getNextID - TableName missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getNextID - " + TableName + " - CompiereSys=" + compiereSys + " - " + trxName);

		//
		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y'"
			+ " FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, TableName);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getNextID - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				if (compiereSys)
				{
					retValue = rs.getInt(2);
					rs.updateInt(2, retValue + IncrementNo);
				}
				else
				{
					retValue = rs.getInt(1);
					rs.updateInt(1, retValue + IncrementNo);
				}
				rs.updateRow();
			}
			else
				s_log.error ("getNextID - no record found - " + TableName);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getNextID", e);
			retValue = -1;
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getNextID - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(9))
			s_log.debug ("getNextID - " + retValue + " - Table=" + TableName + " - " + trx);
		return retValue;
	}	//	getNextID


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int AD_Client_ID, String TableName, String trxName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - TableName missing");
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - " + TableName + " - CompiereSys=" + compiereSys  + " - " + trxName);

		String select = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			+ " AND AD_Client_ID IN (0,?)"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' "
			+ "ORDER BY AD_Client_ID DESC "
			+ "FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		int IncrementNo = 0;
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, PREFIX_DOCSEQ + TableName);
			pstmt.setInt(2, AD_Client_ID);
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
			{
				s_log.warn ("getDocumentNo (Table) - no record found - " + TableName);
				MSequence seq = new MSequence (Env.getCtx(), AD_Client_ID, TableName);
				documentNo = seq.getDocumentNo();
				seq.save();
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (Table) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " (" + IncrementNo + ")" 
				+ " - Table=" + TableName + " - Trx=" + trx);
		return documentNo;
	}	//	getDocumentNo
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param AD_Client_ID client
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int C_DocType_ID, String trxName)
	{
		if (C_DocType_ID == 0)
		{
			s_log.error ("getDocumentNo - C_DocType_ID=0");
			return null;
		}
		MDocType dt = MDocType.get (Env.getCtx(), C_DocType_ID);	//	wrong for SERVER, but r/o
		if (dt != null && !dt.isDocNoControlled())
		{
			if (Log.isTraceLevel(6))
				s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " Not DocNo controlled");
			return null;
		}
		if (dt == null || dt.getDocNoSequence_ID() == 0)
		{
			s_log.warn ("getDocumentNo - No Sequence for DocType - " + dt);
			return null;
		}
			
		String documentNo = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		
		if (Log.isTraceLevel(10))
			s_log.debug("getDocumentNo - DocType_ID=" + C_DocType_ID + " - " + trxName);
		
		String select = "SELECT	CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Client_ID "
			+ "FROM AD_Sequence "
			+ "WHERE AD_Sequence_ID=?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y'"
			+ " FOR UPDATE";// OF CurrentNext, CurrentNextSys";
		Connection conn = null;
		PreparedStatement pstmt = null;
		Trx trx = trxName == null ? null : Trx.get(trxName, true);
		boolean wasAutocommit = false;
		try
		{
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW();
			wasAutocommit = conn.getAutoCommit();
			if (wasAutocommit)
				conn.setAutoCommit(false);
			//
			pstmt = conn.prepareStatement(select,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setInt(1, dt.getDocNoSequence_ID());
			//
			ResultSet rs = pstmt.executeQuery();
		//	s_log.debug("getDocumentNo - AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
		//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				int next = 0;
				int IncrementNo = rs.getInt(3);
				String prefix = rs.getString(4);
				String suffix = rs.getString(5);
				int AD_Client_ID = rs.getInt(6);
				if (compiereSys && AD_Client_ID > 11)
					compiereSys = false;
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + IncrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + IncrementNo);
				}
				rs.updateRow();
				//	create DocumentNo
				StringBuffer doc = new StringBuffer();
				if (prefix != null && prefix.length() > 0)
					doc.append(prefix);
				doc.append(next);
				if (suffix != null && suffix.length() > 0)
					doc.append(suffix);
				documentNo = doc.toString();
			}
			else
				s_log.warn ("getDocumentNo (DocType)- no record found - " + dt);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType)", e);
		}
		//	Finish
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
			//	Commit
			if (wasAutocommit && trx == null)
			{
				conn.setAutoCommit(true);
				conn.commit();
			}
		}
		catch (Exception e)
		{
			s_log.error("getDocumentNo (DocType) - finish", e);
			pstmt = null;
		}
		if (Log.isTraceLevel(6))
			s_log.debug ("getDocumentNo - " + documentNo + " - C_DocType_ID=" + C_DocType_ID + " - " + trx);
		return documentNo;
	}	//	getDocumentNo

	
	
	/**
	 *	Check/Initialize Client DocumentNo/Value Sequences 	
	 *	@param ctx context
	 *	@param AD_Client_ID client
	 *	@return true if no error
	 */
	public static boolean checkClientSequences (Properties ctx, int AD_Client_ID)
	{
		String sql = "SELECT TableName "
			+ "FROM AD_Table t "
			+ "WHERE IsActive='Y'"
			//	Get all Tables with DocumentNo or Value
			+ " AND AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column "
				+ "WHERE ColumnName = 'DocumentNo' OR ColumnName = 'Value')"
			//	Ability to run multiple times
			+ " AND 'DocumentNo_' || TableName NOT IN (SELECT Name FROM AD_Sequence s "
				+ "WHERE s.AD_Client_ID = ?)";		//	#1
		int counter = 0;
		boolean success = true;
		//
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				MSequence seq = new MSequence (ctx, AD_Client_ID, tableName);
				if (seq.save())
					counter++;
				else
				{
					s_log.error ("checkClientSequences - Not created "
						+ " - AD_Client_ID=" + AD_Client_ID
						+ " - "  + tableName);
					success = false;
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("checkClientSequences", e);
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
		s_log.info ("checkClientSequences - AD_Client_ID=" + AD_Client_ID 
			+ " - created #" + counter
			+ " - success=" + success);
		return success;
	}	//	checkClientSequences
	

	/**
	 * 	Create Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 * 	@return true if created
	 */
	public static boolean createTableSequence (Properties ctx, String TableName)
	{
		MSequence seq = new MSequence (ctx, 0);
		seq.setName(TableName);
		seq.setDescription("Table " + TableName);
		seq.setIsTableID(true);
		return seq.save();
	}	//	createTableSequence
	
	/**	Sequence for Table Document No's	*/
	private static final String	PREFIX_DOCSEQ = "DocumentNo_";
	/**	Start Number			*/
	public static final int		INIT_NO = 1000000;	//	1 Mio
	/**	Start System Number		*/
	public static final int		INIT_SYS_NO = 100;	
	/** Static Logger			*/
	private static Logger 		s_log = Logger.getCLogger(MSequence.class);
	
	
	/**************************************************************************
	 *	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Sequence_ID id
	 */
	public MSequence (Properties ctx, int AD_Sequence_ID)
	{
		super(ctx, AD_Sequence_ID);
		if (AD_Sequence_ID == 0)
		{
		//	setName (null);
			//
			setIsTableID(false);
			setStartNo (INIT_NO);
			setCurrentNext (INIT_NO);
			setCurrentNextSys (INIT_SYS_NO);
			setIncrementNo (1);
			setIsAutoSequence (true);
			setIsAudited(false);
			setStartNewYear(false);
		}
	}	//	MSequence

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MSequence (Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MSequence

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String tableName)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(PREFIX_DOCSEQ + tableName);
		setDescription("DocumentNo/Value for Table " + tableName);
	}	//	MSequence;
	
	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 */
	public MSequence (Properties ctx, int AD_Client_ID, String sequenceName, int StartNo)
	{
		this (ctx, 0);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(sequenceName);
		setDescription(sequenceName);
		setStartNo(StartNo);
		setCurrentNext(StartNo);
		setCurrentNextSys(StartNo/10);
	}	//	MSequence;
	
	
	/**************************************************************************
	 * 	Get Next No and increase current next
	 *	@return next no to use
	 */
	public int getNextID()
	{
		int retValue = getCurrentNext();
		setCurrentNext(retValue + getIncrementNo());
		return retValue;
	}	//	getNextNo
	
	/**
	 * 	Get next DocumentNo
	 *	@return document no
	 */
	public String getDocumentNo()
	{
		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(getNextID());
		String suffix = getSuffix();
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		return doc.toString();
	}	//	getDocumentNo
	
	
	/**************************************************************************
	 *	Test
	 *	@param args ignored
	 */
	static public void main (String[] args)
	{
		org.compiere.Compiere.startupClient();
		
		try
		{
			int retValue = -1;
		//	Connection conn = DB.getConnectionRW ();
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			Connection conn = DriverManager.getConnection ("jdbc:oracle:thin:@dev2:1521:dev2", "compiere", "compiere");

		//	conn.setAutoCommit(false);
			String sql = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
				+ "FROM AD_Sequence "
				+ "WHERE Name='AD_Sequence' "
				+ "FOR UPDATE OF CurrentNext, CurrentNextSys";
			

			PreparedStatement pstmt = conn.prepareStatement(sql,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = pstmt.executeQuery();
			System.out.println(" AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation() 
				+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			
			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				retValue = rs.getInt(1);
				rs.updateInt(1, retValue + IncrementNo);
				rs.updateRow();
			}
			else
				s_log.error ("no record found");
			rs.close();
			pstmt.close();
			System.out.println("Next=" + retValue);
			
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		
		
		
		
		
System.exit(0);		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		int AD_Client_ID = 0;
		int C_DocType_ID = 115;	//	GL
		String TableName = "C_Invoice";
		String trxName = "x";
		Trx trx = Trx.get(trxName, true);		

				
		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getNextID (0, "Test", trxName));
		System.out.println ("trx2 " + getNextID (0, "Test", trxName));
		trx.rollback();
		System.out.println ("trx3 " + getNextID (0, "Test", trxName));
		trx.commit();
		System.out.println ("trx4 " + getNextID (0, "Test", trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getNextID (0, "Test", null));
		System.out.println ("==============================================");		
		

		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		System.out.println ("trx2 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(AD_Client_ID, TableName, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("==============================================");		


		trx = Trx.get(trxName, true);		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(C_DocType_ID, trxName));
		System.out.println ("trx2 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(C_DocType_ID, trxName));
		trx.commit();
		System.out.println ("trx4 " + getDocumentNo(C_DocType_ID, trxName));
		trx.rollback();
		trx.close();
		System.out.println ("----------------------------------------------");		
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
	//	System.out.println ("==============================================");		

	}	//	args

}	//	MSequence
