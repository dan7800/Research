/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.finance;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class PVSettleSCMSession extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	private SuppAccountObject supplier = null;
	private PaymentVoucherItemObject pvItemObj = null;
	protected TreeMap tableRows = null;
	protected TreeMap openCreditMemo = null;
	private Integer userId = null;
	private CreatePaymentVoucherSession cpvSes = null;
	private Vector vecDebitNote = new Vector();

	// / contructor!
	public PVSettleSCMSession(CreatePaymentVoucherSession cpvSes)
	{
		this.cpvSes = cpvSes;
		this.state = STATE_DRAFT;
		this.tableRows = new TreeMap();
		this.userId = cpvSes.getUserId();
		this.branch = cpvSes.getBranch();
		this.vecDebitNote = new Vector();
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		this.tableRows.clear();
		this.pvItemObj = null;
		this.openCreditMemo = null;
		this.supplier = null;
		this.vecDebitNote = new Vector();
	}

	public void confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("Incomplete data!!!");
		savePaymentVoucherItem();
		reset();
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}
		// check valid supplier
		if (this.supplier == null)
		{
			result = false;
		}
		// check valid amount
		if (!this.getValidAmt())
		{
			result = false;
		}
		// check procurement center
		if (!this.getValidBranch())
		{
			result = false;
		}
		// check cashbook
		return result;
	}

	// ///////////////////////////////////////////////////
	// // FOREX UTILITIES
	public String getForeignCurrency()
	{
		return this.cpvSes.getForeignCurrency();
	}

	public boolean usingForeignCurrency()
	{
		return this.cpvSes.usingForeignCurrency();
	}

	public String getXrate(String buf)
	{
		return this.cpvSes.getXrate(buf);
	}

	public BigDecimal getXrate()
	{
		BigDecimal tmp = this.cpvSes.getXrate().divide(new BigDecimal(1), 4, BigDecimal.ROUND_HALF_EVEN);
		
		return tmp;
	}

	public String getRXrate(String buf)
	{
		return this.cpvSes.getRXrate(buf);
	}

	// ///////////////////////////////////////////////////
	public void addDocRow(String pkid, String thisAmt) throws Exception
	{
		Long lPkid = null;
		BigDecimal theAmt = null;
		try
		{
			lPkid = new Long(pkid);
		} catch (Exception ex)
		{
			return;
		}
		// / if the pkid is ok
		// / remove previous entry
		this.tableRows.remove(pkid);
		// / then check the amount
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			return;
		}
		// / only add a row if the amt >0
		Log.printVerbose("in addDocRow, thisAmt is: " + theAmt.toString());
      CreditMemoIndexObject cmObj = (CreditMemoIndexObject) this.openCreditMemo.get(pkid.toString());
      if (theAmt.signum() > 0 && cmObj!=null )
      {
/*         if(cmObj.balance.negate().compareTo(theAmt)>=0)
         { this.tableRows.put(pkid, theAmt); 
         	Log.printVerbose("inserting theAmt: " + theAmt.toString());
         }
         else
         { this.tableRows.put(pkid, cmObj.balance.negate());
         Log.printVerbose("inserting cmObj.balance.negate(): " + cmObj.balance.negate().toString());
         }*/
         
		if(usingForeignCurrency())
		{
			if(cmObj.balance2.negate().compareTo(theAmt)>=0)
			{ this.tableRows.put(pkid, theAmt); }
			else
			{ this.tableRows.put(pkid, cmObj.balance2.negate()); }
		}
		else
		{
			if(cmObj.balance.negate().compareTo(theAmt)>=0)
			{ this.tableRows.put(pkid, theAmt); }
			else
			{ this.tableRows.put(pkid, cmObj.balance.negate()); }
		}	
      }




	}

	public void settleEarliest(String thisAmt) throws Exception
	{
		BigDecimal theAmt = null;
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Amount!!!");
		}
		if (theAmt.signum() <= 0)
		{
			throw new Exception("Amount must be more than zero!");
		}
		this.tableRows.clear();
		Vector vecInv = new Vector(this.openCreditMemo.values());
		for (int cnt1 = 0; cnt1 < vecInv.size() && theAmt.signum() > 0; cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecInv.get(cnt1);
			BigDecimal thisOutstanding = cmObj.balance;
			if (usingForeignCurrency())
			{
				thisOutstanding = cmObj.balance2;
			}
			if (thisOutstanding.negate().compareTo(theAmt) > 0)
			{
				this.tableRows.put(cmObj.pkid.toString(), theAmt);
				theAmt = new BigDecimal(0);
			} else
			{
				this.tableRows.put(cmObj.pkid.toString(), thisOutstanding.negate());
				theAmt = theAmt.subtract(thisOutstanding.negate());
			}
		}
	}

	public BigDecimal dropDocRow(String key)
	{
		return (BigDecimal) this.tableRows.remove(key);
	}

	public BigDecimal getRowAmt(String key)
	{
		return (BigDecimal) this.tableRows.get(key);
	}

	public String getState()
	{
		return this.state;
	}

	public boolean getValidBranch()
	{
		if (this.branch == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public boolean getValidSupplier()
	{
		if (this.supplier == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer iProcCtr) throws Exception
	{
		// / if there's a change in procurement center,
		// / need to invalidate the cashbook currently selected
		this.branch = BranchNut.getObject(iProcCtr);
		if (this.branch == null)
		{
			throw new Exception("Error setting Procurement Center!");
		}
		retrieveOpenCreditMemos();
	}

	public void setBranch(BranchObject branchObj)
	{
		this.branch = branchObj;
	}

	public void setSupplier(Integer iSupplier) throws Exception
	{
		this.supplier = SuppAccountNut.getObject(iSupplier);
		if (this.supplier == null)
		{
			throw new Exception("Unable to set supplier!");
		}
		retrieveOpenCreditMemos();
		retrieveDebitNote();
	}



	public SuppAccountObject getSupplier()
	{
		return this.supplier;
	}

	private void savePaymentVoucherItem()
	{
		BigDecimal exchangeRateGainLoss = new BigDecimal(0);
		BigDecimal localSettleAmt = new BigDecimal(0);
		Log.printVerbose(" Saving Payment Voucher Item 1111111111");
		// first, build the Payment Voucher Item value object
		this.pvItemObj = new PaymentVoucherItemObject();
		// this.indexId =
		// this.position =
		// this.uuid =
		// this.txnType
		// this.txnCode
		this.pvItemObj.pcCenter = this.cpvSes.getCashbook().pkId;
		// this.chequeNo =
		this.pvItemObj.branch = this.cpvSes.getBranch().pkid;
		// this.category
		// this.project
		this.pvItemObj.glCodeDebit = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.glCodeCredit = this.cpvSes.getCashbook().accountType;
		this.pvItemObj.currency = this.cpvSes.getCashbook().currency;
		this.pvItemObj.amount = this.getTotalAmt();
		this.pvItemObj.referenceNo = "";
		this.pvItemObj.chequeNo = "";
		this.pvItemObj.description = "";
		this.pvItemObj.remarks = "";
		if (usingForeignCurrency())
		{
			this.pvItemObj.description = " Foreign Currency Settlement ";
			this.pvItemObj.remarks = " Foreign Currency Settlement ";
		}
		this.pvItemObj.info1 = "";
		this.pvItemObj.info2 = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.entityTable = SuppAccountBean.TABLENAME;
		this.pvItemObj.entityKey = this.supplier.pkid;
		this.pvItemObj.entityName = this.supplier.name;
		this.pvItemObj.dateStmt = TimeFormat.getTimestamp();
		this.pvItemObj.dateItem = this.pvItemObj.dateStmt;
		this.pvItemObj.userIdPIC = this.userId;
		try
		{
			Set keySet = this.tableRows.keySet();
			Iterator keyItr = keySet.iterator();
			while (keyItr.hasNext())
			{
				String strPkid = (String) keyItr.next();
				Long thisInvId = new Long(strPkid);
				BigDecimal thisSettleAmt = (BigDecimal) this.tableRows.get(strPkid);
				this.pvItemObj.remarks += " CM:" + strPkid + " ";
				CreditMemoIndex thisCMEJB = CreditMemoIndexNut.getHandle(thisInvId);
				CreditMemoIndexObject cmObj = thisCMEJB.getObject();
				// BigDecimal thisSettleAmt = new
				// BigDecimal(settleAmtArr[invCnt2]);
				// Substract from outstandingAmt
				// thisInvEJB.adjustOutstanding(thisSettleAmt.negate());
				DocLinkObject docLinkObj = new DocLinkObject();
				docLinkObj.namespace = DocLinkBean.NS_SUPPLIER;
				docLinkObj.reference = "";
				docLinkObj.relType = DocLinkBean.RELTYPE_PYMT_SINV;
				docLinkObj.srcDocRef = PaymentVoucherItemBean.TABLENAME;
				// docLinkObj.srcDocId = ?????
				docLinkObj.tgtDocRef = CreditMemoIndexBean.TABLENAME;
				docLinkObj.tgtDocId = thisInvId;
				Log.printVerbose(" ............... CHECKPOINT 1...................");
				Log.printVerbose(" THIS SETTLE AMT = " + thisSettleAmt.toString());
				if (!usingForeignCurrency())
				{
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = thisSettleAmt;
					docLinkObj.currency2 = "";
					docLinkObj.amount2 = new BigDecimal(0);
					localSettleAmt = localSettleAmt.add(thisSettleAmt);
				} else
				{
					docLinkObj.currency2 = getForeignCurrency();
					docLinkObj.amount2 = thisSettleAmt;
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = docLinkObj.amount2.divide(cmObj.xrate, 12, BigDecimal.ROUND_HALF_EVEN);
					BigDecimal newBaseAmt = thisSettleAmt.divide(getXrate(), 12, BigDecimal.ROUND_HALF_EVEN);
					// exchangeRateGainLoss = exchangeRateGainLoss.add(
					// docLinkObj.amount.subtract(newBaseAmt));
					exchangeRateGainLoss = exchangeRateGainLoss.subtract(newBaseAmt.negate().add(docLinkObj.amount));
					localSettleAmt = localSettleAmt.add(docLinkObj.amount);
					
					System.out.println("docLinkObj.currency2 : "+docLinkObj.currency2);
					System.out.println("docLinkObj.amount2 : "+docLinkObj.amount2.toString());
					System.out.println("docLinkObj.currency : "+docLinkObj.currency);
					System.out.println("cmObj.xrate : "+cmObj.xrate.toString());
					System.out.println("getXrate() : "+getXrate().toString());
					System.out.println("docLinkObj.amount : "+docLinkObj.amount.toString());
					System.out.println("newBaseAmt : "+newBaseAmt.toString());
					System.out.println("exchangeRateGainLoss : "+exchangeRateGainLoss.toString());
					System.out.println("localSettleAmt : "+localSettleAmt.toString());
				}
				Log.printVerbose(" LOCAL SETTLE AMT = " + localSettleAmt.toString());
				Log.printVerbose(" EXCHANGE GAIN (LOSS) = " + exchangeRateGainLoss.toString());
				docLinkObj.lastUpdate = TimeFormat.getTimestamp();
				docLinkObj.userIdUpdate = this.userId;
				this.pvItemObj.vecDocLink.add(docLinkObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (!usingForeignCurrency())
		{
			this.pvItemObj.amount = localSettleAmt;
		} else
		{
			this.pvItemObj.amount = localSettleAmt;
			this.pvItemObj.xrate = this.getXrate();
			this.pvItemObj.currency2 = this.getForeignCurrency();
			this.pvItemObj.amount2 = this.getTotalAmt();
			Log.printVerbose("PV ITEM .... " + pvItemObj.toString());
		}
		this.cpvSes.addPVItem(this.pvItemObj);
		if (exchangeRateGainLoss.signum() != 0)
		{
			// / add another PaymentVoucherItem for exchange rate
			if (exchangeRateGainLoss.signum() > 0)
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Gain ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Gain (Auto Created)", exchangeRateGainLoss.negate());
			} else
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Loss ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Loss (Auto Created)", exchangeRateGainLoss.negate());
			}
		}
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public BigDecimal getTotalAmt()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			BigDecimal thisAmt = (BigDecimal) vecDocRow.get(cnt1);
			Log.printVerbose("in getTotalAmt, thisAmt: " + thisAmt.toString());
			totalAmt = totalAmt.add(thisAmt);
		}
		return totalAmt;
	}

	public boolean getValidAmt()
	{
		if (getTotalAmt() != null && getTotalAmt().signum() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private void retrieveOpenCreditMemos()
	{
		// check valid supplier account
		if (!getValidSupplier())
		{
			return;
		}
		// / check valid procurement center
		if (!getValidBranch())
		{
			return;
		}
		QueryObject query = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '"
				+ this.supplier.pkid.toString() + "' " + " AND " + CreditMemoIndexBean.BALANCE + " < '0' " + " AND "
				+ CreditMemoIndexBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' " + " AND "
				+ CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " + " AND "
				+ CreditMemoIndexBean.ENTITY_TABLE + " = '" + SuppAccountBean.TABLENAME + "' " + " AND "
				+ CreditMemoIndexBean.CURRENCY2 + " = '" + getForeignCurrency() + "' " });
		query.setOrder(" ORDER BY " + CreditMemoIndexBean.TIME_CREATE + ", " + CreditMemoIndexBean.PKID);
		Vector vecCreditMemo = new Vector(CreditMemoIndexNut.getObjects(query));
		if (this.openCreditMemo == null)
		{
			this.openCreditMemo = new TreeMap();
		} else
		{
			this.openCreditMemo.clear();
		}
		for (int cnt1 = 0; cnt1 < vecCreditMemo.size(); cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecCreditMemo.get(cnt1);
			// this.openCreditMemo.put(gen.getUUID(), cmObj);
			this.openCreditMemo.put(cmObj.pkid.toString(), cmObj);
		}
		this.tableRows.clear();
	}

   private void retrieveDebitNote()
   {
      // check valid supplier account
      if (!getValidSupplier())
      {
         return;
      }
      // / check valid procurement center
      if (!getValidBranch())
      {
         return;
      }

      QueryObject queryDebitNote = new QueryObject(new String[]{
            CreditMemoIndexBean.ENTITY_KEY +" = '"+ this.supplier.pkid.toString() + "' ",
            CreditMemoIndexBean.BALANCE + " > '0' ",
            CreditMemoIndexBean.ENTITY_TABLE + " = '"+SuppAccountBean.TABLENAME + "' ",
            CreditMemoIndexBean.PC_CENTER + " = '"+this.branch.accPCCenterId.toString()  + "' "
                           });
      queryDebitNote.setOrder(" ORDER BY "+CreditMemoIndexBean.TIME_CREATE + " ");
      this.vecDebitNote = new Vector(CreditMemoIndexNut.getObjects(queryDebitNote));

   }


   public Vector getDebitNote()
   {
      return this.vecDebitNote;
   }



	public TreeMap getOpenCreditMemos()
	{
		return this.openCreditMemo;
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
package com.vlee.bean.finance;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class PVSettleSCMSession extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	private SuppAccountObject supplier = null;
	private PaymentVoucherItemObject pvItemObj = null;
	protected TreeMap tableRows = null;
	protected TreeMap openCreditMemo = null;
	private Integer userId = null;
	private CreatePaymentVoucherSession cpvSes = null;

	// / contructor!
	public PVSettleSCMSession(CreatePaymentVoucherSession cpvSes)
	{
		this.cpvSes = cpvSes;
		this.state = STATE_DRAFT;
		this.tableRows = new TreeMap();
		this.userId = cpvSes.getUserId();
		this.branch = cpvSes.getBranch();
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		this.tableRows.clear();
		this.pvItemObj = null;
		this.openCreditMemo = null;
		this.supplier = null;
	}

	public void confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("Incomplete data!!!");
		savePaymentVoucherItem();
		reset();
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}
		// check valid supplier
		if (this.supplier == null)
		{
			result = false;
		}
		// check valid amount
		if (!this.getValidAmt())
		{
			result = false;
		}
		// check procurement center
		if (!this.getValidBranch())
		{
			result = false;
		}
		// check cashbook
		return result;
	}

	// ///////////////////////////////////////////////////
	// // FOREX UTILITIES
	public String getForeignCurrency()
	{
		return this.cpvSes.getForeignCurrency();
	}

	public boolean usingForeignCurrency()
	{
		return this.cpvSes.usingForeignCurrency();
	}

	public String getXrate(String buf)
	{
		return this.cpvSes.getXrate(buf);
	}

	public BigDecimal getXrate()
	{
		return this.cpvSes.getXrate();
	}

	public String getRXrate(String buf)
	{
		return this.cpvSes.getRXrate(buf);
	}

	// ///////////////////////////////////////////////////
	public void addDocRow(String pkid, String thisAmt) throws Exception
	{
		Long lPkid = null;
		BigDecimal theAmt = null;
		try
		{
			lPkid = new Long(pkid);
		} catch (Exception ex)
		{
			return;
		}
		// / if the pkid is ok
		// / remove previous entry
		this.tableRows.remove(pkid);
		// / then check the amount
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			return;
		}
		// / only add a row if the amt >0
		if (theAmt.signum() > 0)
		{
			this.tableRows.put(pkid, theAmt);
		}
	}

	public void settleEarliest(String thisAmt) throws Exception
	{
		BigDecimal theAmt = null;
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Amount!!!");
		}
		if (theAmt.signum() <= 0)
		{
			throw new Exception("Amount must be more than zero!");
		}
		this.tableRows.clear();
		Vector vecInv = new Vector(this.openCreditMemo.values());
		for (int cnt1 = 0; cnt1 < vecInv.size() && theAmt.signum() > 0; cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecInv.get(cnt1);
			BigDecimal thisOutstanding = cmObj.balance;
			if (usingForeignCurrency())
			{
				thisOutstanding = cmObj.balance2;
			}
			if (thisOutstanding.negate().compareTo(theAmt) > 0)
			{
				this.tableRows.put(cmObj.pkid.toString(), theAmt);
				theAmt = new BigDecimal(0);
			} else
			{
				this.tableRows.put(cmObj.pkid.toString(), thisOutstanding.negate());
				theAmt = theAmt.subtract(thisOutstanding.negate());
			}
		}
	}

	public BigDecimal dropDocRow(String key)
	{
		return (BigDecimal) this.tableRows.remove(key);
	}

	public BigDecimal getRowAmt(String key)
	{
		return (BigDecimal) this.tableRows.get(key);
	}

	public String getState()
	{
		return this.state;
	}

	public boolean getValidBranch()
	{
		if (this.branch == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public boolean getValidSupplier()
	{
		if (this.supplier == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer iProcCtr) throws Exception
	{
		// / if there's a change in procurement center,
		// / need to invalidate the cashbook currently selected
		this.branch = BranchNut.getObject(iProcCtr);
		if (this.branch == null)
		{
			throw new Exception("Error setting Procurement Center!");
		}
		retrieveOpenCreditMemos();
	}

	public void setBranch(BranchObject branchObj)
	{
		this.branch = branchObj;
	}

	public void setSupplier(Integer iSupplier) throws Exception
	{
		this.supplier = SuppAccountNut.getObject(iSupplier);
		if (this.supplier == null)
		{
			throw new Exception("Unable to set supplier!");
		}
		retrieveOpenCreditMemos();
	}

	public SuppAccountObject getSupplier()
	{
		return this.supplier;
	}

	private void savePaymentVoucherItem()
	{
		BigDecimal exchangeRateGainLoss = new BigDecimal(0);
		BigDecimal localSettleAmt = new BigDecimal(0);
		Log.printVerbose(" Saving Payment Voucher Item 1111111111");
		// first, build the Payment Voucher Item value object
		this.pvItemObj = new PaymentVoucherItemObject();
		// this.indexId =
		// this.position =
		// this.uuid =
		// this.txnType
		// this.txnCode
		this.pvItemObj.pcCenter = this.cpvSes.getCashbook().pkId;
		// this.chequeNo =
		this.pvItemObj.branch = this.cpvSes.getBranch().pkid;
		// this.category
		// this.project
		this.pvItemObj.glCodeDebit = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.glCodeCredit = this.cpvSes.getCashbook().accountType;
		this.pvItemObj.currency = this.cpvSes.getCashbook().currency;
		this.pvItemObj.amount = this.getTotalAmt();
		this.pvItemObj.referenceNo = "";
		this.pvItemObj.chequeNo = "";
		this.pvItemObj.description = "";
		this.pvItemObj.remarks = "";
		if (usingForeignCurrency())
		{
			this.pvItemObj.description = " Foreign Currency Settlement ";
			this.pvItemObj.remarks = " Foreign Currency Settlement ";
		}
		this.pvItemObj.info1 = "";
		this.pvItemObj.info2 = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.entityTable = SuppAccountBean.TABLENAME;
		this.pvItemObj.entityKey = this.supplier.pkid;
		this.pvItemObj.entityName = this.supplier.name;
		this.pvItemObj.dateStmt = TimeFormat.getTimestamp();
		this.pvItemObj.dateItem = this.pvItemObj.dateStmt;
		this.pvItemObj.userIdPIC = this.userId;
		try
		{
			Set keySet = this.tableRows.keySet();
			Iterator keyItr = keySet.iterator();
			while (keyItr.hasNext())
			{
				String strPkid = (String) keyItr.next();
				Long thisInvId = new Long(strPkid);
				BigDecimal thisSettleAmt = (BigDecimal) this.tableRows.get(strPkid);
				this.pvItemObj.remarks += " CM:" + strPkid + " ";
				CreditMemoIndex thisCMEJB = CreditMemoIndexNut.getHandle(thisInvId);
				CreditMemoIndexObject cmObj = thisCMEJB.getObject();
				// BigDecimal thisSettleAmt = new
				// BigDecimal(settleAmtArr[invCnt2]);
				// Substract from outstandingAmt
				// thisInvEJB.adjustOutstanding(thisSettleAmt.negate());
				DocLinkObject docLinkObj = new DocLinkObject();
				docLinkObj.namespace = DocLinkBean.NS_SUPPLIER;
				docLinkObj.reference = "";
				docLinkObj.relType = DocLinkBean.RELTYPE_PYMT_SINV;
				docLinkObj.srcDocRef = PaymentVoucherItemBean.TABLENAME;
				// docLinkObj.srcDocId = ?????
				docLinkObj.tgtDocRef = CreditMemoIndexBean.TABLENAME;
				docLinkObj.tgtDocId = thisInvId;
				Log.printVerbose(" ............... CHECKPOINT 1...................");
				Log.printVerbose(" THIS SETTLE AMT = " + thisSettleAmt.toString());
				if (!usingForeignCurrency())
				{
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = thisSettleAmt;
					docLinkObj.currency2 = "";
					docLinkObj.amount2 = new BigDecimal(0);
					localSettleAmt = localSettleAmt.add(thisSettleAmt);
				} else
				{
					docLinkObj.currency2 = getForeignCurrency();
					docLinkObj.amount2 = thisSettleAmt;
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = docLinkObj.amount2.divide(cmObj.xrate, 12, BigDecimal.ROUND_HALF_EVEN);
					BigDecimal newBaseAmt = thisSettleAmt.divide(getXrate(), 12, BigDecimal.ROUND_HALF_EVEN);
					// exchangeRateGainLoss = exchangeRateGainLoss.add(
					// docLinkObj.amount.subtract(newBaseAmt));
					exchangeRateGainLoss = exchangeRateGainLoss.subtract(newBaseAmt.add(docLinkObj.amount));
					localSettleAmt = localSettleAmt.add(docLinkObj.amount);
				}
				Log.printVerbose(" LOCAL SETTLE AMT = " + localSettleAmt.toString());
				Log.printVerbose(" EXCHANGE GAIN (LOSS) = " + exchangeRateGainLoss.toString());
				docLinkObj.lastUpdate = TimeFormat.getTimestamp();
				docLinkObj.userIdUpdate = this.userId;
				this.pvItemObj.vecDocLink.add(docLinkObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (!usingForeignCurrency())
		{
			this.pvItemObj.amount = localSettleAmt;
		} else
		{
			this.pvItemObj.amount = localSettleAmt;
			this.pvItemObj.xrate = this.getXrate();
			this.pvItemObj.currency2 = this.getForeignCurrency();
			this.pvItemObj.amount2 = this.getTotalAmt();
			Log.printVerbose("PV ITEM .... " + pvItemObj.toString());
		}
		this.cpvSes.addPVItem(this.pvItemObj);
		if (exchangeRateGainLoss.signum() != 0)
		{
			// / add another PaymentVoucherItem for exchange rate
			if (exchangeRateGainLoss.signum() > 0)
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Gain ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Gain (Auto Created)", exchangeRateGainLoss.negate());
			} else
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Loss ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Loss (Auto Created)", exchangeRateGainLoss.negate());
			}
		}
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public BigDecimal getTotalAmt()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			BigDecimal thisAmt = (BigDecimal) vecDocRow.get(cnt1);
			totalAmt = totalAmt.add(thisAmt);
		}
		return totalAmt;
	}

	public boolean getValidAmt()
	{
		if (getTotalAmt() != null && getTotalAmt().signum() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private void retrieveOpenCreditMemos()
	{
		// check valid supplier account
		if (!getValidSupplier())
		{
			return;
		}
		// / check valid procurement center
		if (!getValidBranch())
		{
			return;
		}
		QueryObject query = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '"
				+ this.supplier.pkid.toString() + "' " + " AND " + CreditMemoIndexBean.BALANCE + " < '0' " + " AND "
				+ CreditMemoIndexBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' " + " AND "
				+ CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " + " AND "
				+ CreditMemoIndexBean.ENTITY_TABLE + " = '" + SuppAccountBean.TABLENAME + "' " + " AND "
				+ CreditMemoIndexBean.CURRENCY2 + " = '" + getForeignCurrency() + "' " });
		query.setOrder(" ORDER BY " + CreditMemoIndexBean.TIME_CREATE + ", " + CreditMemoIndexBean.PKID);
		Vector vecCreditMemo = new Vector(CreditMemoIndexNut.getObjects(query));
		if (this.openCreditMemo == null)
		{
			this.openCreditMemo = new TreeMap();
		} else
		{
			this.openCreditMemo.clear();
		}
		for (int cnt1 = 0; cnt1 < vecCreditMemo.size(); cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecCreditMemo.get(cnt1);
			// this.openCreditMemo.put(gen.getUUID(), cmObj);
			this.openCreditMemo.put(cmObj.pkid.toString(), cmObj);
		}
		this.tableRows.clear();
	}

	public TreeMap getOpenCreditMemos()
	{
		return this.openCreditMemo;
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
package com.vlee.bean.finance;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class PVSettleSCMSession extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	private SuppAccountObject supplier = null;
	private PaymentVoucherItemObject pvItemObj = null;
	protected TreeMap tableRows = null;
	protected TreeMap openCreditMemo = null;
	private Integer userId = null;
	private CreatePaymentVoucherSession cpvSes = null;

	// / contructor!
	public PVSettleSCMSession(CreatePaymentVoucherSession cpvSes)
	{
		this.cpvSes = cpvSes;
		this.state = STATE_DRAFT;
		this.tableRows = new TreeMap();
		this.userId = cpvSes.getUserId();
		this.branch = cpvSes.getBranch();
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		this.tableRows.clear();
		this.pvItemObj = null;
		this.openCreditMemo = null;
		this.supplier = null;
	}

	public void confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("Incomplete data!!!");
		savePaymentVoucherItem();
		reset();
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}
		// check valid supplier
		if (this.supplier == null)
		{
			result = false;
		}
		// check valid amount
		if (!this.getValidAmt())
		{
			result = false;
		}
		// check procurement center
		if (!this.getValidBranch())
		{
			result = false;
		}
		// check cashbook
		return result;
	}

	// ///////////////////////////////////////////////////
	// // FOREX UTILITIES
	public String getForeignCurrency()
	{
		return this.cpvSes.getForeignCurrency();
	}

	public boolean usingForeignCurrency()
	{
		return this.cpvSes.usingForeignCurrency();
	}

	public String getXrate(String buf)
	{
		return this.cpvSes.getXrate(buf);
	}

	public BigDecimal getXrate()
	{
		return this.cpvSes.getXrate();
	}

	public String getRXrate(String buf)
	{
		return this.cpvSes.getRXrate(buf);
	}

	// ///////////////////////////////////////////////////
	public void addDocRow(String pkid, String thisAmt) throws Exception
	{
		Long lPkid = null;
		BigDecimal theAmt = null;
		try
		{
			lPkid = new Long(pkid);
		} catch (Exception ex)
		{
			return;
		}
		// / if the pkid is ok
		// / remove previous entry
		this.tableRows.remove(pkid);
		// / then check the amount
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			return;
		}
		// / only add a row if the amt >0
		if (theAmt.signum() > 0)
		{
			this.tableRows.put(pkid, theAmt);
		}
	}

	public void settleEarliest(String thisAmt) throws Exception
	{
		BigDecimal theAmt = null;
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Amount!!!");
		}
		if (theAmt.signum() <= 0)
		{
			throw new Exception("Amount must be more than zero!");
		}
		this.tableRows.clear();
		Vector vecInv = new Vector(this.openCreditMemo.values());
		for (int cnt1 = 0; cnt1 < vecInv.size() && theAmt.signum() > 0; cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecInv.get(cnt1);
			BigDecimal thisOutstanding = cmObj.balance;
			if (usingForeignCurrency())
			{
				thisOutstanding = cmObj.balance2;
			}
			if (thisOutstanding.negate().compareTo(theAmt) > 0)
			{
				this.tableRows.put(cmObj.pkid.toString(), theAmt);
				theAmt = new BigDecimal(0);
			} else
			{
				this.tableRows.put(cmObj.pkid.toString(), thisOutstanding.negate());
				theAmt = theAmt.subtract(thisOutstanding.negate());
			}
		}
	}

	public BigDecimal dropDocRow(String key)
	{
		return (BigDecimal) this.tableRows.remove(key);
	}

	public BigDecimal getRowAmt(String key)
	{
		return (BigDecimal) this.tableRows.get(key);
	}

	public String getState()
	{
		return this.state;
	}

	public boolean getValidBranch()
	{
		if (this.branch == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public boolean getValidSupplier()
	{
		if (this.supplier == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer iProcCtr) throws Exception
	{
		// / if there's a change in procurement center,
		// / need to invalidate the cashbook currently selected
		this.branch = BranchNut.getObject(iProcCtr);
		if (this.branch == null)
		{
			throw new Exception("Error setting Procurement Center!");
		}
		retrieveOpenCreditMemos();
	}

	public void setBranch(BranchObject branchObj)
	{
		this.branch = branchObj;
	}

	public void setSupplier(Integer iSupplier) throws Exception
	{
		this.supplier = SuppAccountNut.getObject(iSupplier);
		if (this.supplier == null)
		{
			throw new Exception("Unable to set supplier!");
		}
		retrieveOpenCreditMemos();
	}

	public SuppAccountObject getSupplier()
	{
		return this.supplier;
	}

	private void savePaymentVoucherItem()
	{
		BigDecimal exchangeRateGainLoss = new BigDecimal(0);
		BigDecimal localSettleAmt = new BigDecimal(0);
		Log.printVerbose(" Saving Payment Voucher Item 1111111111");
		// first, build the Payment Voucher Item value object
		this.pvItemObj = new PaymentVoucherItemObject();
		// this.indexId =
		// this.position =
		// this.uuid =
		// this.txnType
		// this.txnCode
		this.pvItemObj.pcCenter = this.cpvSes.getCashbook().pkId;
		// this.chequeNo =
		this.pvItemObj.branch = this.cpvSes.getBranch().pkid;
		// this.category
		// this.project
		this.pvItemObj.glCodeDebit = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.glCodeCredit = this.cpvSes.getCashbook().accountType;
		this.pvItemObj.currency = this.cpvSes.getCashbook().currency;
		this.pvItemObj.amount = this.getTotalAmt();
		this.pvItemObj.referenceNo = "";
		this.pvItemObj.chequeNo = "";
		this.pvItemObj.description = "";
		this.pvItemObj.remarks = "";
		if (usingForeignCurrency())
		{
			this.pvItemObj.description = " Foreign Currency Settlement ";
			this.pvItemObj.remarks = " Foreign Currency Settlement ";
		}
		this.pvItemObj.info1 = "";
		this.pvItemObj.info2 = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.entityTable = SuppAccountBean.TABLENAME;
		this.pvItemObj.entityKey = this.supplier.pkid;
		this.pvItemObj.entityName = this.supplier.name;
		this.pvItemObj.dateStmt = TimeFormat.getTimestamp();
		this.pvItemObj.dateItem = this.pvItemObj.dateStmt;
		this.pvItemObj.userIdPIC = this.userId;
		try
		{
			Set keySet = this.tableRows.keySet();
			Iterator keyItr = keySet.iterator();
			while (keyItr.hasNext())
			{
				String strPkid = (String) keyItr.next();
				Long thisInvId = new Long(strPkid);
				BigDecimal thisSettleAmt = (BigDecimal) this.tableRows.get(strPkid);
				this.pvItemObj.remarks += " CM:" + strPkid + " ";
				CreditMemoIndex thisCMEJB = CreditMemoIndexNut.getHandle(thisInvId);
				CreditMemoIndexObject cmObj = thisCMEJB.getObject();
				// BigDecimal thisSettleAmt = new
				// BigDecimal(settleAmtArr[invCnt2]);
				// Substract from outstandingAmt
				// thisInvEJB.adjustOutstanding(thisSettleAmt.negate());
				DocLinkObject docLinkObj = new DocLinkObject();
				docLinkObj.namespace = DocLinkBean.NS_SUPPLIER;
				docLinkObj.reference = "";
				docLinkObj.relType = DocLinkBean.RELTYPE_PYMT_SINV;
				docLinkObj.srcDocRef = PaymentVoucherItemBean.TABLENAME;
				// docLinkObj.srcDocId = ?????
				docLinkObj.tgtDocRef = CreditMemoIndexBean.TABLENAME;
				docLinkObj.tgtDocId = thisInvId;
				Log.printVerbose(" ............... CHECKPOINT 1...................");
				Log.printVerbose(" THIS SETTLE AMT = " + thisSettleAmt.toString());
				if (!usingForeignCurrency())
				{
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = thisSettleAmt;
					docLinkObj.currency2 = "";
					docLinkObj.amount2 = new BigDecimal(0);
					localSettleAmt = localSettleAmt.add(thisSettleAmt);
				} else
				{
					docLinkObj.currency2 = getForeignCurrency();
					docLinkObj.amount2 = thisSettleAmt;
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = docLinkObj.amount2.divide(cmObj.xrate, 12, BigDecimal.ROUND_HALF_EVEN);
					BigDecimal newBaseAmt = thisSettleAmt.divide(getXrate(), 12, BigDecimal.ROUND_HALF_EVEN);
					// exchangeRateGainLoss = exchangeRateGainLoss.add(
					// docLinkObj.amount.subtract(newBaseAmt));
					exchangeRateGainLoss = exchangeRateGainLoss.subtract(newBaseAmt.add(docLinkObj.amount));
					localSettleAmt = localSettleAmt.add(docLinkObj.amount);
				}
				Log.printVerbose(" LOCAL SETTLE AMT = " + localSettleAmt.toString());
				Log.printVerbose(" EXCHANGE GAIN (LOSS) = " + exchangeRateGainLoss.toString());
				docLinkObj.lastUpdate = TimeFormat.getTimestamp();
				docLinkObj.userIdUpdate = this.userId;
				this.pvItemObj.vecDocLink.add(docLinkObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (!usingForeignCurrency())
		{
			this.pvItemObj.amount = localSettleAmt;
		} else
		{
			this.pvItemObj.amount = localSettleAmt;
			this.pvItemObj.xrate = this.getXrate();
			this.pvItemObj.currency2 = this.getForeignCurrency();
			this.pvItemObj.amount2 = this.getTotalAmt();
			Log.printVerbose("PV ITEM .... " + pvItemObj.toString());
		}
		this.cpvSes.addPVItem(this.pvItemObj);
		if (exchangeRateGainLoss.signum() != 0)
		{
			// / add another PaymentVoucherItem for exchange rate
			if (exchangeRateGainLoss.signum() > 0)
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Gain ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Gain (Auto Created)", exchangeRateGainLoss.negate());
			} else
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Loss ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Loss (Auto Created)", exchangeRateGainLoss.negate());
			}
		}
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public BigDecimal getTotalAmt()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			BigDecimal thisAmt = (BigDecimal) vecDocRow.get(cnt1);
			totalAmt = totalAmt.add(thisAmt);
		}
		return totalAmt;
	}

	public boolean getValidAmt()
	{
		if (getTotalAmt() != null && getTotalAmt().signum() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private void retrieveOpenCreditMemos()
	{
		// check valid supplier account
		if (!getValidSupplier())
		{
			return;
		}
		// / check valid procurement center
		if (!getValidBranch())
		{
			return;
		}
		QueryObject query = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '"
				+ this.supplier.pkid.toString() + "' " + " AND " + CreditMemoIndexBean.BALANCE + " < '0' " + " AND "
				+ CreditMemoIndexBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' " + " AND "
				+ CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " + " AND "
				+ CreditMemoIndexBean.ENTITY_TABLE + " = '" + SuppAccountBean.TABLENAME + "' " + " AND "
				+ CreditMemoIndexBean.CURRENCY2 + " = '" + getForeignCurrency() + "' " });
		query.setOrder(" ORDER BY " + CreditMemoIndexBean.TIME_CREATE + ", " + CreditMemoIndexBean.PKID);
		Vector vecCreditMemo = new Vector(CreditMemoIndexNut.getObjects(query));
		if (this.openCreditMemo == null)
		{
			this.openCreditMemo = new TreeMap();
		} else
		{
			this.openCreditMemo.clear();
		}
		for (int cnt1 = 0; cnt1 < vecCreditMemo.size(); cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecCreditMemo.get(cnt1);
			// this.openCreditMemo.put(gen.getUUID(), cmObj);
			this.openCreditMemo.put(cmObj.pkid.toString(), cmObj);
		}
		this.tableRows.clear();
	}

	public TreeMap getOpenCreditMemos()
	{
		return this.openCreditMemo;
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
package com.vlee.bean.finance;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class PVSettleSCMSession extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	private SuppAccountObject supplier = null;
	private PaymentVoucherItemObject pvItemObj = null;
	protected TreeMap tableRows = null;
	protected TreeMap openCreditMemo = null;
	private Integer userId = null;
	private CreatePaymentVoucherSession cpvSes = null;

	// / contructor!
	public PVSettleSCMSession(CreatePaymentVoucherSession cpvSes)
	{
		this.cpvSes = cpvSes;
		this.state = STATE_DRAFT;
		this.tableRows = new TreeMap();
		this.userId = cpvSes.getUserId();
		this.branch = cpvSes.getBranch();
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		this.tableRows.clear();
		this.pvItemObj = null;
		this.openCreditMemo = null;
		this.supplier = null;
	}

	public void confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("Incomplete data!!!");
		savePaymentVoucherItem();
		reset();
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}
		// check valid supplier
		if (this.supplier == null)
		{
			result = false;
		}
		// check valid amount
		if (!this.getValidAmt())
		{
			result = false;
		}
		// check procurement center
		if (!this.getValidBranch())
		{
			result = false;
		}
		// check cashbook
		return result;
	}

	// ///////////////////////////////////////////////////
	// // FOREX UTILITIES
	public String getForeignCurrency()
	{
		return this.cpvSes.getForeignCurrency();
	}

	public boolean usingForeignCurrency()
	{
		return this.cpvSes.usingForeignCurrency();
	}

	public String getXrate(String buf)
	{
		return this.cpvSes.getXrate(buf);
	}

	public BigDecimal getXrate()
	{
		return this.cpvSes.getXrate();
	}

	public String getRXrate(String buf)
	{
		return this.cpvSes.getRXrate(buf);
	}

	// ///////////////////////////////////////////////////
	public void addDocRow(String pkid, String thisAmt) throws Exception
	{
		Long lPkid = null;
		BigDecimal theAmt = null;
		try
		{
			lPkid = new Long(pkid);
		} catch (Exception ex)
		{
			return;
		}
		// / if the pkid is ok
		// / remove previous entry
		this.tableRows.remove(pkid);
		// / then check the amount
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			return;
		}
		// / only add a row if the amt >0
		if (theAmt.signum() > 0)
		{
			this.tableRows.put(pkid, theAmt);
		}
	}

	public void settleEarliest(String thisAmt) throws Exception
	{
		BigDecimal theAmt = null;
		try
		{
			theAmt = new BigDecimal(thisAmt);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Amount!!!");
		}
		if (theAmt.signum() <= 0)
		{
			throw new Exception("Amount must be more than zero!");
		}
		this.tableRows.clear();
		Vector vecInv = new Vector(this.openCreditMemo.values());
		for (int cnt1 = 0; cnt1 < vecInv.size() && theAmt.signum() > 0; cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecInv.get(cnt1);
			BigDecimal thisOutstanding = cmObj.balance;
			if (usingForeignCurrency())
			{
				thisOutstanding = cmObj.balance2;
			}
			if (thisOutstanding.negate().compareTo(theAmt) > 0)
			{
				this.tableRows.put(cmObj.pkid.toString(), theAmt);
				theAmt = new BigDecimal(0);
			} else
			{
				this.tableRows.put(cmObj.pkid.toString(), thisOutstanding.negate());
				theAmt = theAmt.subtract(thisOutstanding.negate());
			}
		}
	}

	public BigDecimal dropDocRow(String key)
	{
		return (BigDecimal) this.tableRows.remove(key);
	}

	public BigDecimal getRowAmt(String key)
	{
		return (BigDecimal) this.tableRows.get(key);
	}

	public String getState()
	{
		return this.state;
	}

	public boolean getValidBranch()
	{
		if (this.branch == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public boolean getValidSupplier()
	{
		if (this.supplier == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer iProcCtr) throws Exception
	{
		// / if there's a change in procurement center,
		// / need to invalidate the cashbook currently selected
		this.branch = BranchNut.getObject(iProcCtr);
		if (this.branch == null)
		{
			throw new Exception("Error setting Procurement Center!");
		}
		retrieveOpenCreditMemos();
	}

	public void setBranch(BranchObject branchObj)
	{
		this.branch = branchObj;
	}

	public void setSupplier(Integer iSupplier) throws Exception
	{
		this.supplier = SuppAccountNut.getObject(iSupplier);
		if (this.supplier == null)
		{
			throw new Exception("Unable to set supplier!");
		}
		retrieveOpenCreditMemos();
	}

	public SuppAccountObject getSupplier()
	{
		return this.supplier;
	}

	private void savePaymentVoucherItem()
	{
		BigDecimal exchangeRateGainLoss = new BigDecimal(0);
		BigDecimal localSettleAmt = new BigDecimal(0);
		Log.printVerbose(" Saving Payment Voucher Item 1111111111");
		// first, build the Payment Voucher Item value object
		this.pvItemObj = new PaymentVoucherItemObject();
		// this.indexId =
		// this.position =
		// this.uuid =
		// this.txnType
		// this.txnCode
		this.pvItemObj.pcCenter = this.cpvSes.getCashbook().pkId;
		// this.chequeNo =
		this.pvItemObj.branch = this.cpvSes.getBranch().pkid;
		// this.category
		// this.project
		this.pvItemObj.glCodeDebit = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.glCodeCredit = this.cpvSes.getCashbook().accountType;
		this.pvItemObj.currency = this.cpvSes.getCashbook().currency;
		this.pvItemObj.amount = this.getTotalAmt();
		this.pvItemObj.referenceNo = "";
		this.pvItemObj.chequeNo = "";
		this.pvItemObj.description = "";
		this.pvItemObj.remarks = "";
		if (usingForeignCurrency())
		{
			this.pvItemObj.description = " Foreign Currency Settlement ";
			this.pvItemObj.remarks = " Foreign Currency Settlement ";
		}
		this.pvItemObj.info1 = "";
		this.pvItemObj.info2 = GLCodeBean.ACC_PAYABLE;
		this.pvItemObj.entityTable = SuppAccountBean.TABLENAME;
		this.pvItemObj.entityKey = this.supplier.pkid;
		this.pvItemObj.entityName = this.supplier.name;
		this.pvItemObj.dateStmt = TimeFormat.getTimestamp();
		this.pvItemObj.dateItem = this.pvItemObj.dateStmt;
		this.pvItemObj.userIdPIC = this.userId;
		try
		{
			Set keySet = this.tableRows.keySet();
			Iterator keyItr = keySet.iterator();
			while (keyItr.hasNext())
			{
				String strPkid = (String) keyItr.next();
				Long thisInvId = new Long(strPkid);
				BigDecimal thisSettleAmt = (BigDecimal) this.tableRows.get(strPkid);
				this.pvItemObj.remarks += " CM:" + strPkid + " ";
				CreditMemoIndex thisCMEJB = CreditMemoIndexNut.getHandle(thisInvId);
				CreditMemoIndexObject cmObj = thisCMEJB.getObject();
				// BigDecimal thisSettleAmt = new
				// BigDecimal(settleAmtArr[invCnt2]);
				// Substract from outstandingAmt
				// thisInvEJB.adjustOutstanding(thisSettleAmt.negate());
				DocLinkObject docLinkObj = new DocLinkObject();
				docLinkObj.namespace = DocLinkBean.NS_SUPPLIER;
				docLinkObj.reference = "";
				docLinkObj.relType = DocLinkBean.RELTYPE_PYMT_SINV;
				docLinkObj.srcDocRef = PaymentVoucherItemBean.TABLENAME;
				// docLinkObj.srcDocId = ?????
				docLinkObj.tgtDocRef = CreditMemoIndexBean.TABLENAME;
				docLinkObj.tgtDocId = thisInvId;
				Log.printVerbose(" ............... CHECKPOINT 1...................");
				Log.printVerbose(" THIS SETTLE AMT = " + thisSettleAmt.toString());
				if (!usingForeignCurrency())
				{
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = thisSettleAmt;
					docLinkObj.currency2 = "";
					docLinkObj.amount2 = new BigDecimal(0);
					localSettleAmt = localSettleAmt.add(thisSettleAmt);
				} else
				{
					docLinkObj.currency2 = getForeignCurrency();
					docLinkObj.amount2 = thisSettleAmt;
					docLinkObj.currency = this.pvItemObj.currency;
					docLinkObj.amount = docLinkObj.amount2.divide(cmObj.xrate, 12, BigDecimal.ROUND_HALF_EVEN);
					BigDecimal newBaseAmt = thisSettleAmt.divide(getXrate(), 12, BigDecimal.ROUND_HALF_EVEN);
					// exchangeRateGainLoss = exchangeRateGainLoss.add(
					// docLinkObj.amount.subtract(newBaseAmt));
					exchangeRateGainLoss = exchangeRateGainLoss.subtract(newBaseAmt.add(docLinkObj.amount));
					localSettleAmt = localSettleAmt.add(docLinkObj.amount);
				}
				Log.printVerbose(" LOCAL SETTLE AMT = " + localSettleAmt.toString());
				Log.printVerbose(" EXCHANGE GAIN (LOSS) = " + exchangeRateGainLoss.toString());
				docLinkObj.lastUpdate = TimeFormat.getTimestamp();
				docLinkObj.userIdUpdate = this.userId;
				this.pvItemObj.vecDocLink.add(docLinkObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (!usingForeignCurrency())
		{
			this.pvItemObj.amount = localSettleAmt;
		} else
		{
			this.pvItemObj.amount = localSettleAmt;
			this.pvItemObj.xrate = this.getXrate();
			this.pvItemObj.currency2 = this.getForeignCurrency();
			this.pvItemObj.amount2 = this.getTotalAmt();
			Log.printVerbose("PV ITEM .... " + pvItemObj.toString());
		}
		this.cpvSes.addPVItem(this.pvItemObj);
		if (exchangeRateGainLoss.signum() != 0)
		{
			// / add another PaymentVoucherItem for exchange rate
			if (exchangeRateGainLoss.signum() > 0)
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Gain ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Gain (Auto Created)", exchangeRateGainLoss.negate());
			} else
			{
				this.cpvSes.addPVItem(GLCodeBean.FOREX_GAINLOSS, "Exchange Rate Loss ", " N / A", this.cpvSes
						.getStmtDate(), "Forex Loss (Auto Created)", exchangeRateGainLoss.negate());
			}
		}
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public BigDecimal getTotalAmt()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			BigDecimal thisAmt = (BigDecimal) vecDocRow.get(cnt1);
			totalAmt = totalAmt.add(thisAmt);
		}
		return totalAmt;
	}

	public boolean getValidAmt()
	{
		if (getTotalAmt() != null && getTotalAmt().signum() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private void retrieveOpenCreditMemos()
	{
		// check valid supplier account
		if (!getValidSupplier())
		{
			return;
		}
		// / check valid procurement center
		if (!getValidBranch())
		{
			return;
		}
		QueryObject query = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '"
				+ this.supplier.pkid.toString() + "' " + " AND " + CreditMemoIndexBean.BALANCE + " < '0' " + " AND "
				+ CreditMemoIndexBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' " + " AND "
				+ CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " + " AND "
				+ CreditMemoIndexBean.ENTITY_TABLE + " = '" + SuppAccountBean.TABLENAME + "' " + " AND "
				+ CreditMemoIndexBean.CURRENCY2 + " = '" + getForeignCurrency() + "' " });
		query.setOrder(" ORDER BY " + CreditMemoIndexBean.TIME_CREATE + ", " + CreditMemoIndexBean.PKID);
		Vector vecCreditMemo = new Vector(CreditMemoIndexNut.getObjects(query));
		if (this.openCreditMemo == null)
		{
			this.openCreditMemo = new TreeMap();
		} else
		{
			this.openCreditMemo.clear();
		}
		for (int cnt1 = 0; cnt1 < vecCreditMemo.size(); cnt1++)
		{
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecCreditMemo.get(cnt1);
			// this.openCreditMemo.put(gen.getUUID(), cmObj);
			this.openCreditMemo.put(cmObj.pkid.toString(), cmObj);
		}
		this.tableRows.clear();
	}

	public TreeMap getOpenCreditMemos()
	{
		return this.openCreditMemo;
	}
}
