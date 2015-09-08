/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.accounting;

import java.io.Serializable;
import java.util.Vector;
import com.vlee.ejb.accounting.*;
import com.vlee.util.*;

public class MonthEndProcessingSession extends java.lang.Object implements Serializable
{
	static final long serialVersionUID = 0;
	private ProfitCostCenterObject pccObj = null;
	private Integer userId = null;
	private Vector vecFinYear;
	private GLSummaryObject glSumObj;

	public MonthEndProcessingSession(Integer theUserId)
	{
		this.userId = theUserId;
		init();
	}

	private void init()
	{
		this.pccObj = null;
		this.vecFinYear = new Vector();
		this.glSumObj = null;
	}

	public synchronized void setPCCenter(Integer pcc) throws Exception
	{
		this.pccObj = ProfitCostCenterNut.getObject(pcc);
		loadPreviousFinancialYear();
		getSummary();
	}

	public String getPCCenter(String buf)
	{
		if (this.pccObj != null)
		{
			buf = this.pccObj.mPkid.toString();
		}
		return buf;
	}

	public ProfitCostCenterObject getPCCenter()
	{
		return this.pccObj;
	}

	public boolean validPCCenter()
	{
		if (this.pccObj == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void loadPreviousFinancialYear()
	{
		this.vecFinYear.clear();
		if (this.pccObj == null)
		{
			this.vecFinYear.clear();
			return;
		}
		QueryObject query = new QueryObject(new String[] {
				FinancialYearBean.PCCENTER + " = '" + this.pccObj.mPkid.toString() + "' ",
				FinancialYearBean.BATCH + " = '" + BatchBean.PKID_DEFAULT.toString() + "' " });
		query.setOrder(" ORDER BY " + FinancialYearBean.DATE_START + " DESC , " + FinancialYearBean.PKID + " DESC ");
		this.vecFinYear = new Vector(FinancialYearNut.getObjects(query));
	}

	public boolean forwardToCreateFinYear()
	{
		if (this.vecFinYear.size() == 0 && this.pccObj != null)
		{
			return true;
		}
		return false;
	}

	public Vector getFinYearList()
	{
		return this.vecFinYear;
	}

	public void getSummary()
	{
		this.glSumObj = new GLSummaryObject();
		this.glSumObj.pcCenter = this.pccObj.mPkid;
		// this.glSumObj.batch ///////// take default value DEFAULT
		// this.glSumObj.glCode = null;
		// this.glSumObj.jTypeId = /////// take default value POSTED
		// this.glSumObj.dateFrom = //// take default value 0001-01-01
		// this.glSumObj.dateTo = TimeFormat.getTimestamp();
		this.glSumObj.dateTo = TimeFormat.createTimestamp("9999-12-30");
		this.glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		this.glSumObj = GeneralLedgerNut.getSummary(this.glSumObj);
	}

	public GLSummaryObject getGLSumObj()
	{
		return this.glSumObj;
	}

	public int countOutstandingTemp()
	{
		if (this.glSumObj == null)
		{
			return 0;
		}
		int count = 0;
		for (int cnt = 0; cnt < this.glSumObj.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) this.glSumObj.vecRow.get(cnt);
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				count++;
			}
		}
		return count++;
	}

	public synchronized void cleanUpTransferOfPLforRealGL()
	{
		JournalTransactionNut.fnCleanUpTransferOfPLforRealGL();
	}

	public synchronized void transferToPL() throws Exception
	{
		this.glSumObj = doTransfer(this.glSumObj, this.userId, this.pccObj);
	}

	public static synchronized GLSummaryObject doTransfer(GLSummaryObject theGLSUM, Integer theUser,
			ProfitCostCenterObject thePCC) throws Exception
	{
		if (theGLSUM == null)
		{
			throw new Exception("Please set the PC Center first!");
		}
		// / load existing financial periods!
		QueryObject fpQuery = new QueryObject(new String[] {
				FinancialPeriodBean.PCCENTER + " = '" + thePCC.mPkid.toString() + "' ",
				FinancialPeriodBean.BATCH + " = '" + theGLSUM.batch.toString() + "' ",
				FinancialPeriodBean.STATE + " = '" + FinancialPeriodBean.STATE_CREATED + "' " });
		fpQuery.setOrder(" ORDER BY " + FinancialYearBean.DATE_START);
		Vector vecFinPeriod = new Vector(FinancialPeriodNut.getObjects(fpQuery));
		for (int cnt = 0; cnt < theGLSUM.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) theGLSUM.vecRow.get(cnt);
			Log.printVerbose("checkpoint 1:---------------------------------------------------");
			Log.printVerbose("checkpoint 1:---------------------------------------------------");
			Log.printVerbose("checkpoint 1:---------------------------------------------------");
			Log.printVerbose("checkpoint 1:---------------------------------------------------");
			Log.printVerbose("checkpoint 1: cnt         :" + cnt);
			Log.printVerbose("checkpoint 1: realtemp    :" + theRow.realTemp);
			Log.printVerbose("checkpoint 1: genLedgerId :" + theRow.genLedgerId.toString());
			Log.printVerbose("checkpoint 1: glCode      :" + theRow.glCode);
			Log.printVerbose("checkpoint 1: glCodeName  :" + theRow.glCodeName);
			Log.printVerbose("checkpoint 1: amount      :" + theRow.amount.toString());
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS))
//					&& theRow.amount.signum() != 0)
			{

				Log.printVerbose("checkpoint 2: ---------------------------------------");
				Log.printVerbose("checkpoint 2: going thru the various financial periods");

				for (int cnt2 = 0; cnt2 < vecFinPeriod.size(); cnt2++)
				{
					FinancialPeriodObject fpObj = (FinancialPeriodObject) vecFinPeriod.get(cnt2);
				Log.printVerbose("checkpoint 3: ---------------------------------------");
				Log.printVerbose("checkpoint 3: Period From:" + TimeFormat.strDisplayDate(fpObj.dateStart));
				Log.printVerbose("checkpoint 3: Period To:" + TimeFormat.strDisplayDate(fpObj.dateEnd));
					GLSummaryObject glSum2 = new GLSummaryObject();
					glSum2.pcCenter = fpObj.pcCenter;
					// theGLSUM.batch ///////// take default value DEFAULT
					glSum2.glCode = theRow.glCode;
					glSum2.jTypeId = JournalTransactionBean.TYPEID_POSTED;
					glSum2.dateFrom = fpObj.dateStart;
					glSum2.dateTo = fpObj.dateEnd;
					glSum2 = GeneralLedgerNut.getSummary(glSum2);
					// // Creating the transfer as a Journal Transaction
					for (int cnt3 = 0; cnt3 < glSum2.vecRow.size(); cnt3++)
					{
						GeneralLedgerObject glPL = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.PROFIT_LOSS,
								theGLSUM.batch, glSum2.pcCenter, thePCC.mCurrency);
						GLSummaryObject.Row theRow3 = (GLSummaryObject.Row) glSum2.vecRow.get(cnt3);
				Log.printVerbose("checkpoint 4: ---------------------------------------");
				Log.printVerbose("checkpoint 4: AMOUNT: "+CurrencyFormat.strCcy(theRow3.amount));
						if (theRow3.amount.signum() == 0)
						{
				Log.printVerbose("checkpoint 4: CONTINUE!!!!!!!!!!!!!!!!");
							continue;
						}
				Log.printVerbose("checkpoint 5: CREATING JOURNAL TXN!!!!!!!!!!!!!");
						JournalTransactionObject jttObj = new JournalTransactionObject();
						jttObj.txnCode = JournalTransactionBean.TXNCODE_PROFIT;
						jttObj.pcCenterId = glSum2.pcCenter;
						jttObj.batchId = glSum2.batch;
						jttObj.typeId = JournalTransactionBean.TYPEID_POSTED;
						jttObj.status = JournalTransactionBean.ACTIVE;
						jttObj.name = "Transfer To Profit And Loss From " + glSum2.glCode;
						jttObj.description = " Posting Temporary GL " + glSum2.glCode
								+ " during Month End Processing ";
						jttObj.amount = theRow3.amount.abs();
						jttObj.transactionDate = fpObj.dateEnd;
						// jttObj.docRef = SalesReturnBean.TABLENAME;
						// jttObj.docKey = srObj.mPkid;
						jttObj.state = JournalTransactionBean.STATE_CREATED;
						jttObj.userIdCreate = theUser;
						jttObj.userIdEdit = theUser;
						jttObj.userIdCancel = theUser;
						jttObj.timestampCreate = TimeFormat.getTimestamp();
						jttObj.timestampEdit = TimeFormat.getTimestamp();
						jttObj.timestampCancel = TimeFormat.getTimestamp();
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = theRow3.genLedgerId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount.negate();
							jttObj.vecEntry.add(jeObj);
						}
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = glPL.pkId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount;
							jttObj.vecEntry.add(jeObj);
						}
						Log.printVerbose(" Creating Journal Txn:....... ");
						jttObj = JournalTransactionNut.fnCreate(jttObj);
						Log.printVerbose(" Created Journal Txn:" + jttObj.pkId.toString());
					}
				}
			}
		}
		// / reload the glSummary Object
		theGLSUM = GeneralLedgerNut.getSummary(theGLSUM);
		return theGLSUM;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.accounting;

import java.io.Serializable;
import java.util.Vector;
import com.vlee.ejb.accounting.BatchBean;
import com.vlee.ejb.accounting.FinancialPeriodBean;
import com.vlee.ejb.accounting.FinancialPeriodNut;
import com.vlee.ejb.accounting.FinancialPeriodObject;
import com.vlee.ejb.accounting.FinancialYearBean;
import com.vlee.ejb.accounting.FinancialYearNut;
import com.vlee.ejb.accounting.GLCategoryBean;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GLSummaryObject;
import com.vlee.ejb.accounting.GeneralLedgerNut;
import com.vlee.ejb.accounting.GeneralLedgerObject;
import com.vlee.ejb.accounting.JournalEntryObject;
import com.vlee.ejb.accounting.JournalTransactionBean;
import com.vlee.ejb.accounting.JournalTransactionNut;
import com.vlee.ejb.accounting.JournalTransactionObject;
import com.vlee.ejb.accounting.ProfitCostCenterNut;
import com.vlee.ejb.accounting.ProfitCostCenterObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class MonthEndProcessingSession extends java.lang.Object implements Serializable
{
	static final long serialVersionUID = 0;
	private ProfitCostCenterObject pccObj = null;
	private Integer userId = null;
	private Vector vecFinYear;
	private GLSummaryObject glSumObj;

	public MonthEndProcessingSession(Integer theUserId)
	{
		this.userId = theUserId;
		init();
	}

	private void init()
	{
		this.pccObj = null;
		this.vecFinYear = new Vector();
		this.glSumObj = null;
	}

	public synchronized void setPCCenter(Integer pcc) throws Exception
	{
		this.pccObj = ProfitCostCenterNut.getObject(pcc);
		loadPreviousFinancialYear();
		getSummary();
	}

	public String getPCCenter(String buf)
	{
		if (this.pccObj != null)
		{
			buf = this.pccObj.mPkid.toString();
		}
		return buf;
	}

	public ProfitCostCenterObject getPCCenter()
	{
		return this.pccObj;
	}

	public boolean validPCCenter()
	{
		if (this.pccObj == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void loadPreviousFinancialYear()
	{
		this.vecFinYear.clear();
		if (this.pccObj == null)
		{
			this.vecFinYear.clear();
			return;
		}
		QueryObject query = new QueryObject(new String[] {
				FinancialYearBean.PCCENTER + " = '" + this.pccObj.mPkid.toString() + "' ",
				FinancialYearBean.BATCH + " = '" + BatchBean.PKID_DEFAULT.toString() + "' " });
		query.setOrder(" ORDER BY " + FinancialYearBean.DATE_START + " DESC , " + FinancialYearBean.PKID + " DESC ");
		this.vecFinYear = new Vector(FinancialYearNut.getObjects(query));
	}

	public boolean forwardToCreateFinYear()
	{
		if (this.vecFinYear.size() == 0 && this.pccObj != null)
		{
			return true;
		}
		return false;
	}

	public Vector getFinYearList()
	{
		return this.vecFinYear;
	}

	public void getSummary()
	{
		this.glSumObj = new GLSummaryObject();
		this.glSumObj.pcCenter = this.pccObj.mPkid;
		// this.glSumObj.batch ///////// take default value DEFAULT
		// this.glSumObj.glCode = null;
		// this.glSumObj.jTypeId = /////// take default value POSTED
		// this.glSumObj.dateFrom = //// take default value 0001-01-01
		// this.glSumObj.dateTo = TimeFormat.getTimestamp();
		this.glSumObj.dateTo = TimeFormat.createTimestamp("9999-12-30");
		this.glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		this.glSumObj = GeneralLedgerNut.getSummary(this.glSumObj);
	}

	public GLSummaryObject getGLSumObj()
	{
		return this.glSumObj;
	}

	public int countOutstandingTemp()
	{
		if (this.glSumObj == null)
		{
			return 0;
		}
		int count = 0;
		for (int cnt = 0; cnt < this.glSumObj.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) this.glSumObj.vecRow.get(cnt);
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				count++;
			}
		}
		return count++;
	}

	public synchronized void transferToPL() throws Exception
	{
		this.glSumObj = doTransfer(this.glSumObj, this.userId, this.pccObj);
	}

	public static synchronized GLSummaryObject doTransfer(GLSummaryObject theGLSUM, Integer theUser,
			ProfitCostCenterObject thePCC) throws Exception
	{
		if (theGLSUM == null)
		{
			throw new Exception("Please set the PC Center first!");
		}
		// / load existing financial periods!
		QueryObject fpQuery = new QueryObject(new String[] {
				FinancialPeriodBean.PCCENTER + " = '" + thePCC.mPkid.toString() + "' ",
				FinancialPeriodBean.BATCH + " = '" + theGLSUM.batch.toString() + "' ",
				FinancialPeriodBean.STATE + " = '" + FinancialPeriodBean.STATE_CREATED + "' " });
		fpQuery.setOrder(" ORDER BY " + FinancialYearBean.DATE_START);
		Vector vecFinPeriod = new Vector(FinancialPeriodNut.getObjects(fpQuery));
		for (int cnt = 0; cnt < theGLSUM.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) theGLSUM.vecRow.get(cnt);
			Log.printVerbose("checkpoint 1: cnt :" + cnt);
			Log.printVerbose("checkpoint 1: realtemp:" + theRow.realTemp);
			Log.printVerbose("checkpoint 1: genLedgerId :" + theRow.genLedgerId.toString());
			Log.printVerbose("checkpoint 1: amount:" + theRow.amount.toString());
			Log.printVerbose("checkpoint 1: glcode :" + theRow.glCode.toString());
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				Log.printVerbose("checkpoint 2: glcode :" + theRow.glCode.toString());
				for (int cnt2 = 0; cnt2 < vecFinPeriod.size(); cnt2++)
				{
					FinancialPeriodObject fpObj = (FinancialPeriodObject) vecFinPeriod.get(cnt2);
					Log.printVerbose(fpObj.toString());
					GLSummaryObject glSum2 = new GLSummaryObject();
					glSum2.pcCenter = fpObj.pcCenter;
					// theGLSUM.batch ///////// take default value DEFAULT
					glSum2.glCode = theRow.glCode;
					glSum2.jTypeId = JournalTransactionBean.TYPEID_POSTED;
					glSum2.dateFrom = fpObj.dateStart;
					glSum2.dateTo = fpObj.dateEnd;
					glSum2 = GeneralLedgerNut.getSummary(glSum2);
					// // Creating the transfer as a Journal Transaction
					for (int cnt3 = 0; cnt3 < glSum2.vecRow.size(); cnt3++)
					{
						GeneralLedgerObject glPL = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.PROFIT_LOSS,
								theGLSUM.batch, glSum2.pcCenter, thePCC.mCurrency);
						GLSummaryObject.Row theRow3 = (GLSummaryObject.Row) glSum2.vecRow.get(cnt3);
						if (theRow3.amount.signum() == 0)
						{
							continue;
						}
						JournalTransactionObject jttObj = new JournalTransactionObject();
						jttObj.txnCode = JournalTransactionBean.TXNCODE_PROFIT;
						jttObj.pcCenterId = glSum2.pcCenter;
						jttObj.batchId = glSum2.batch;
						jttObj.typeId = JournalTransactionBean.TYPEID_POSTED;
						jttObj.status = JournalTransactionBean.ACTIVE;
						jttObj.name = "Transfer To Profit And Loss From " + glSum2.glCode;
						jttObj.description = " Posting Temporary GL (" + glSum2.glCode
								+ ") To Profit And Loss On Month End Processing ";
						jttObj.amount = theRow3.amount.abs();
						jttObj.transactionDate = fpObj.dateEnd;
						// jttObj.docRef = SalesReturnBean.TABLENAME;
						// jttObj.docKey = srObj.mPkid;
						jttObj.state = JournalTransactionBean.STATE_CREATED;
						jttObj.userIdCreate = theUser;
						jttObj.userIdEdit = theUser;
						jttObj.userIdCancel = theUser;
						jttObj.timestampCreate = TimeFormat.getTimestamp();
						jttObj.timestampEdit = TimeFormat.getTimestamp();
						jttObj.timestampCancel = TimeFormat.getTimestamp();
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = theRow3.genLedgerId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount.negate();
							jttObj.vecEntry.add(jeObj);
						}
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = glPL.pkId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount;
							jttObj.vecEntry.add(jeObj);
						}
						jttObj = JournalTransactionNut.fnCreate(jttObj);
					}
				}
			}
		}
		// / reload the glSummary Object
		theGLSUM = GeneralLedgerNut.getSummary(theGLSUM);
		return theGLSUM;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.accounting;

import java.io.Serializable;
import java.util.Vector;
import com.vlee.ejb.accounting.BatchBean;
import com.vlee.ejb.accounting.FinancialPeriodBean;
import com.vlee.ejb.accounting.FinancialPeriodNut;
import com.vlee.ejb.accounting.FinancialPeriodObject;
import com.vlee.ejb.accounting.FinancialYearBean;
import com.vlee.ejb.accounting.FinancialYearNut;
import com.vlee.ejb.accounting.GLCategoryBean;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GLSummaryObject;
import com.vlee.ejb.accounting.GeneralLedgerNut;
import com.vlee.ejb.accounting.GeneralLedgerObject;
import com.vlee.ejb.accounting.JournalEntryObject;
import com.vlee.ejb.accounting.JournalTransactionBean;
import com.vlee.ejb.accounting.JournalTransactionNut;
import com.vlee.ejb.accounting.JournalTransactionObject;
import com.vlee.ejb.accounting.ProfitCostCenterNut;
import com.vlee.ejb.accounting.ProfitCostCenterObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class MonthEndProcessingSession extends java.lang.Object implements Serializable
{
	static final long serialVersionUID = 0;
	private ProfitCostCenterObject pccObj = null;
	private Integer userId = null;
	private Vector vecFinYear;
	private GLSummaryObject glSumObj;

	public MonthEndProcessingSession(Integer theUserId)
	{
		this.userId = theUserId;
		init();
	}

	private void init()
	{
		this.pccObj = null;
		this.vecFinYear = new Vector();
		this.glSumObj = null;
	}

	public synchronized void setPCCenter(Integer pcc) throws Exception
	{
		this.pccObj = ProfitCostCenterNut.getObject(pcc);
		loadPreviousFinancialYear();
		getSummary();
	}

	public String getPCCenter(String buf)
	{
		if (this.pccObj != null)
		{
			buf = this.pccObj.mPkid.toString();
		}
		return buf;
	}

	public ProfitCostCenterObject getPCCenter()
	{
		return this.pccObj;
	}

	public boolean validPCCenter()
	{
		if (this.pccObj == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void loadPreviousFinancialYear()
	{
		this.vecFinYear.clear();
		if (this.pccObj == null)
		{
			this.vecFinYear.clear();
			return;
		}
		QueryObject query = new QueryObject(new String[] {
				FinancialYearBean.PCCENTER + " = '" + this.pccObj.mPkid.toString() + "' ",
				FinancialYearBean.BATCH + " = '" + BatchBean.PKID_DEFAULT.toString() + "' " });
		query.setOrder(" ORDER BY " + FinancialYearBean.DATE_START + " DESC , " + FinancialYearBean.PKID + " DESC ");
		this.vecFinYear = new Vector(FinancialYearNut.getObjects(query));
	}

	public boolean forwardToCreateFinYear()
	{
		if (this.vecFinYear.size() == 0 && this.pccObj != null)
		{
			return true;
		}
		return false;
	}

	public Vector getFinYearList()
	{
		return this.vecFinYear;
	}

	public void getSummary()
	{
		this.glSumObj = new GLSummaryObject();
		this.glSumObj.pcCenter = this.pccObj.mPkid;
		// this.glSumObj.batch ///////// take default value DEFAULT
		// this.glSumObj.glCode = null;
		// this.glSumObj.jTypeId = /////// take default value POSTED
		// this.glSumObj.dateFrom = //// take default value 0001-01-01
		// this.glSumObj.dateTo = TimeFormat.getTimestamp();
		this.glSumObj.dateTo = TimeFormat.createTimestamp("9999-12-30");
		this.glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		this.glSumObj = GeneralLedgerNut.getSummary(this.glSumObj);
	}

	public GLSummaryObject getGLSumObj()
	{
		return this.glSumObj;
	}

	public int countOutstandingTemp()
	{
		if (this.glSumObj == null)
		{
			return 0;
		}
		int count = 0;
		for (int cnt = 0; cnt < this.glSumObj.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) this.glSumObj.vecRow.get(cnt);
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				count++;
			}
		}
		return count++;
	}

	public synchronized void transferToPL() throws Exception
	{
		this.glSumObj = doTransfer(this.glSumObj, this.userId, this.pccObj);
	}

	public static synchronized GLSummaryObject doTransfer(GLSummaryObject theGLSUM, Integer theUser,
			ProfitCostCenterObject thePCC) throws Exception
	{
		if (theGLSUM == null)
		{
			throw new Exception("Please set the PC Center first!");
		}
		// / load existing financial periods!
		QueryObject fpQuery = new QueryObject(new String[] {
				FinancialPeriodBean.PCCENTER + " = '" + thePCC.mPkid.toString() + "' ",
				FinancialPeriodBean.BATCH + " = '" + theGLSUM.batch.toString() + "' ",
				FinancialPeriodBean.STATE + " = '" + FinancialPeriodBean.STATE_CREATED + "' " });
		fpQuery.setOrder(" ORDER BY " + FinancialYearBean.DATE_START);
		Vector vecFinPeriod = new Vector(FinancialPeriodNut.getObjects(fpQuery));
		for (int cnt = 0; cnt < theGLSUM.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) theGLSUM.vecRow.get(cnt);
			Log.printVerbose("checkpoint 1: cnt :" + cnt);
			Log.printVerbose("checkpoint 1: realtemp:" + theRow.realTemp);
			Log.printVerbose("checkpoint 1: genLedgerId :" + theRow.genLedgerId.toString());
			Log.printVerbose("checkpoint 1: amount:" + theRow.amount.toString());
			Log.printVerbose("checkpoint 1: glcode :" + theRow.glCode.toString());
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				Log.printVerbose("checkpoint 2: glcode :" + theRow.glCode.toString());
				for (int cnt2 = 0; cnt2 < vecFinPeriod.size(); cnt2++)
				{
					FinancialPeriodObject fpObj = (FinancialPeriodObject) vecFinPeriod.get(cnt2);
					Log.printVerbose(fpObj.toString());
					GLSummaryObject glSum2 = new GLSummaryObject();
					glSum2.pcCenter = fpObj.pcCenter;
					// theGLSUM.batch ///////// take default value DEFAULT
					glSum2.glCode = theRow.glCode;
					glSum2.jTypeId = JournalTransactionBean.TYPEID_POSTED;
					glSum2.dateFrom = fpObj.dateStart;
					glSum2.dateTo = fpObj.dateEnd;
					glSum2 = GeneralLedgerNut.getSummary(glSum2);
					// // Creating the transfer as a Journal Transaction
					for (int cnt3 = 0; cnt3 < glSum2.vecRow.size(); cnt3++)
					{
						GeneralLedgerObject glPL = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.PROFIT_LOSS,
								theGLSUM.batch, glSum2.pcCenter, thePCC.mCurrency);
						GLSummaryObject.Row theRow3 = (GLSummaryObject.Row) glSum2.vecRow.get(cnt3);
						if (theRow3.amount.signum() == 0)
						{
							continue;
						}
						JournalTransactionObject jttObj = new JournalTransactionObject();
						jttObj.txnCode = JournalTransactionBean.TXNCODE_PROFIT;
						jttObj.pcCenterId = glSum2.pcCenter;
						jttObj.batchId = glSum2.batch;
						jttObj.typeId = JournalTransactionBean.TYPEID_POSTED;
						jttObj.status = JournalTransactionBean.ACTIVE;
						jttObj.name = "Transfer To Profit And Loss From " + glSum2.glCode;
						jttObj.description = " Posting Temporary GL (" + glSum2.glCode
								+ ") To Profit And Loss On Month End Processing ";
						jttObj.amount = theRow3.amount.abs();
						jttObj.transactionDate = fpObj.dateEnd;
						// jttObj.docRef = SalesReturnBean.TABLENAME;
						// jttObj.docKey = srObj.mPkid;
						jttObj.state = JournalTransactionBean.STATE_CREATED;
						jttObj.userIdCreate = theUser;
						jttObj.userIdEdit = theUser;
						jttObj.userIdCancel = theUser;
						jttObj.timestampCreate = TimeFormat.getTimestamp();
						jttObj.timestampEdit = TimeFormat.getTimestamp();
						jttObj.timestampCancel = TimeFormat.getTimestamp();
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = theRow3.genLedgerId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount.negate();
							jttObj.vecEntry.add(jeObj);
						}
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = glPL.pkId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount;
							jttObj.vecEntry.add(jeObj);
						}
						jttObj = JournalTransactionNut.fnCreate(jttObj);
					}
				}
			}
		}
		// / reload the glSummary Object
		theGLSUM = GeneralLedgerNut.getSummary(theGLSUM);
		return theGLSUM;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.accounting;

import java.io.Serializable;
import java.util.Vector;
import com.vlee.ejb.accounting.BatchBean;
import com.vlee.ejb.accounting.FinancialPeriodBean;
import com.vlee.ejb.accounting.FinancialPeriodNut;
import com.vlee.ejb.accounting.FinancialPeriodObject;
import com.vlee.ejb.accounting.FinancialYearBean;
import com.vlee.ejb.accounting.FinancialYearNut;
import com.vlee.ejb.accounting.GLCategoryBean;
import com.vlee.ejb.accounting.GLCodeBean;
import com.vlee.ejb.accounting.GLSummaryObject;
import com.vlee.ejb.accounting.GeneralLedgerNut;
import com.vlee.ejb.accounting.GeneralLedgerObject;
import com.vlee.ejb.accounting.JournalEntryObject;
import com.vlee.ejb.accounting.JournalTransactionBean;
import com.vlee.ejb.accounting.JournalTransactionNut;
import com.vlee.ejb.accounting.JournalTransactionObject;
import com.vlee.ejb.accounting.ProfitCostCenterNut;
import com.vlee.ejb.accounting.ProfitCostCenterObject;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class MonthEndProcessingSession extends java.lang.Object implements Serializable
{
	static final long serialVersionUID = 0;
	private ProfitCostCenterObject pccObj = null;
	private Integer userId = null;
	private Vector vecFinYear;
	private GLSummaryObject glSumObj;

	public MonthEndProcessingSession(Integer theUserId)
	{
		this.userId = theUserId;
		init();
	}

	private void init()
	{
		this.pccObj = null;
		this.vecFinYear = new Vector();
		this.glSumObj = null;
	}

	public synchronized void setPCCenter(Integer pcc) throws Exception
	{
		this.pccObj = ProfitCostCenterNut.getObject(pcc);
		loadPreviousFinancialYear();
		getSummary();
	}

	public String getPCCenter(String buf)
	{
		if (this.pccObj != null)
		{
			buf = this.pccObj.mPkid.toString();
		}
		return buf;
	}

	public ProfitCostCenterObject getPCCenter()
	{
		return this.pccObj;
	}

	public boolean validPCCenter()
	{
		if (this.pccObj == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void loadPreviousFinancialYear()
	{
		this.vecFinYear.clear();
		if (this.pccObj == null)
		{
			this.vecFinYear.clear();
			return;
		}
		QueryObject query = new QueryObject(new String[] {
				FinancialYearBean.PCCENTER + " = '" + this.pccObj.mPkid.toString() + "' ",
				FinancialYearBean.BATCH + " = '" + BatchBean.PKID_DEFAULT.toString() + "' " });
		query.setOrder(" ORDER BY " + FinancialYearBean.DATE_START + " DESC , " + FinancialYearBean.PKID + " DESC ");
		this.vecFinYear = new Vector(FinancialYearNut.getObjects(query));
	}

	public boolean forwardToCreateFinYear()
	{
		if (this.vecFinYear.size() == 0 && this.pccObj != null)
		{
			return true;
		}
		return false;
	}

	public Vector getFinYearList()
	{
		return this.vecFinYear;
	}

	public void getSummary()
	{
		this.glSumObj = new GLSummaryObject();
		this.glSumObj.pcCenter = this.pccObj.mPkid;
		// this.glSumObj.batch ///////// take default value DEFAULT
		// this.glSumObj.glCode = null;
		// this.glSumObj.jTypeId = /////// take default value POSTED
		// this.glSumObj.dateFrom = //// take default value 0001-01-01
		// this.glSumObj.dateTo = TimeFormat.getTimestamp();
		this.glSumObj.dateTo = TimeFormat.createTimestamp("9999-12-30");
		this.glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		this.glSumObj = GeneralLedgerNut.getSummary(this.glSumObj);
	}

	public GLSummaryObject getGLSumObj()
	{
		return this.glSumObj;
	}

	public int countOutstandingTemp()
	{
		if (this.glSumObj == null)
		{
			return 0;
		}
		int count = 0;
		for (int cnt = 0; cnt < this.glSumObj.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) this.glSumObj.vecRow.get(cnt);
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				count++;
			}
		}
		return count++;
	}

	public synchronized void transferToPL() throws Exception
	{
		this.glSumObj = doTransfer(this.glSumObj, this.userId, this.pccObj);
	}

	public static synchronized GLSummaryObject doTransfer(GLSummaryObject theGLSUM, Integer theUser,
			ProfitCostCenterObject thePCC) throws Exception
	{
		if (theGLSUM == null)
		{
			throw new Exception("Please set the PC Center first!");
		}
		// / load existing financial periods!
		QueryObject fpQuery = new QueryObject(new String[] {
				FinancialPeriodBean.PCCENTER + " = '" + thePCC.mPkid.toString() + "' ",
				FinancialPeriodBean.BATCH + " = '" + theGLSUM.batch.toString() + "' ",
				FinancialPeriodBean.STATE + " = '" + FinancialPeriodBean.STATE_CREATED + "' " });
		fpQuery.setOrder(" ORDER BY " + FinancialYearBean.DATE_START);
		Vector vecFinPeriod = new Vector(FinancialPeriodNut.getObjects(fpQuery));
		for (int cnt = 0; cnt < theGLSUM.vecRow.size(); cnt++)
		{
			GLSummaryObject.Row theRow = (GLSummaryObject.Row) theGLSUM.vecRow.get(cnt);
			Log.printVerbose("checkpoint 1: cnt :" + cnt);
			Log.printVerbose("checkpoint 1: realtemp:" + theRow.realTemp);
			Log.printVerbose("checkpoint 1: genLedgerId :" + theRow.genLedgerId.toString());
			Log.printVerbose("checkpoint 1: amount:" + theRow.amount.toString());
			Log.printVerbose("checkpoint 1: glcode :" + theRow.glCode.toString());
			if (theRow.realTemp.equals(GLCategoryBean.RT_TEMP) && !theRow.glCode.equals(GLCodeBean.PROFIT_LOSS)
					&& theRow.amount.signum() != 0)
			{
				Log.printVerbose("checkpoint 2: glcode :" + theRow.glCode.toString());
				for (int cnt2 = 0; cnt2 < vecFinPeriod.size(); cnt2++)
				{
					FinancialPeriodObject fpObj = (FinancialPeriodObject) vecFinPeriod.get(cnt2);
					Log.printVerbose(fpObj.toString());
					GLSummaryObject glSum2 = new GLSummaryObject();
					glSum2.pcCenter = fpObj.pcCenter;
					// theGLSUM.batch ///////// take default value DEFAULT
					glSum2.glCode = theRow.glCode;
					glSum2.jTypeId = JournalTransactionBean.TYPEID_POSTED;
					glSum2.dateFrom = fpObj.dateStart;
					glSum2.dateTo = fpObj.dateEnd;
					glSum2 = GeneralLedgerNut.getSummary(glSum2);
					// // Creating the transfer as a Journal Transaction
					for (int cnt3 = 0; cnt3 < glSum2.vecRow.size(); cnt3++)
					{
						GeneralLedgerObject glPL = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.PROFIT_LOSS,
								theGLSUM.batch, glSum2.pcCenter, thePCC.mCurrency);
						GLSummaryObject.Row theRow3 = (GLSummaryObject.Row) glSum2.vecRow.get(cnt3);
						if (theRow3.amount.signum() == 0)
						{
							continue;
						}
						JournalTransactionObject jttObj = new JournalTransactionObject();
						jttObj.txnCode = JournalTransactionBean.TXNCODE_PROFIT;
						jttObj.pcCenterId = glSum2.pcCenter;
						jttObj.batchId = glSum2.batch;
						jttObj.typeId = JournalTransactionBean.TYPEID_POSTED;
						jttObj.status = JournalTransactionBean.ACTIVE;
						jttObj.name = "Transfer To Profit And Loss From " + glSum2.glCode;
						jttObj.description = " Posting Temporary GL (" + glSum2.glCode
								+ ") To Profit And Loss On Month End Processing ";
						jttObj.amount = theRow3.amount.abs();
						jttObj.transactionDate = fpObj.dateEnd;
						// jttObj.docRef = SalesReturnBean.TABLENAME;
						// jttObj.docKey = srObj.mPkid;
						jttObj.state = JournalTransactionBean.STATE_CREATED;
						jttObj.userIdCreate = theUser;
						jttObj.userIdEdit = theUser;
						jttObj.userIdCancel = theUser;
						jttObj.timestampCreate = TimeFormat.getTimestamp();
						jttObj.timestampEdit = TimeFormat.getTimestamp();
						jttObj.timestampCancel = TimeFormat.getTimestamp();
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = theRow3.genLedgerId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount.negate();
							jttObj.vecEntry.add(jeObj);
						}
						{
							JournalEntryObject jeObj = new JournalEntryObject();
							jeObj.glId = glPL.pkId;
							jeObj.description = jttObj.description;
							jeObj.currency = thePCC.mCurrency;
							jeObj.amount = theRow3.amount;
							jttObj.vecEntry.add(jeObj);
						}
						jttObj = JournalTransactionNut.fnCreate(jttObj);
					}
				}
			}
		}
		// / reload the glSummary Object
		theGLSUM = GeneralLedgerNut.getSummary(theGLSUM);
		return theGLSUM;
	}
}
