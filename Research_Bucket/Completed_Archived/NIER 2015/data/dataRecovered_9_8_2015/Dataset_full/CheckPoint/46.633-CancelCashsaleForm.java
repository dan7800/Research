/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.pos;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.bean.customer.*;

public class CancelCashsaleForm extends java.lang.Object implements Serializable
{
	public InvoiceObject invoiceObj;
	public OfficialReceiptObject receiptObj;
	public Vector vecDocLink;
	public Integer userId;
	public String remarksReverse;

	public CancelCashsaleForm(Integer userId)
	{
		this.invoiceObj = null;
		this.receiptObj = null;
		this.vecDocLink = new Vector();
		this.userId = userId;
		this.remarksReverse = "";
	}

	public void reset()
	{
		this.invoiceObj = null;
		this.receiptObj = null;
		this.vecDocLink = new Vector();
		this.remarksReverse = "";
	}

	public Integer getUserId()
	{ return this.userId;}

	public void cancelCashsale(InvoiceObject invObj,OfficialReceiptObject orObj, Integer userId, Timestamp tsSalesReturn) throws Exception
	{
		if(invObj==null){ return;}
		/// first of all , reverse the receipt, if exist
		if(orObj!=null) { OfficialReceiptNut.fnReverseReceipt(orObj.pkid,userId); }

		/// create a sales return with Credit Memo
		CreateSalesReturnSession csrSession = new CreateSalesReturnSession(invObj.mPkid,userId);
		Log.printVerbose("~~~~~~~~~~~~~~~");
		Log.printVerbose("~~~~~~~this.remarksReverse~~~~~~~~: " + this.remarksReverse);
		csrSession.setRemarksReverse(this.remarksReverse);
		csrSession.setRefund(false);/// because we are not using payment voucher
		csrSession.setDate(tsSalesReturn);

		TreeMap formRow = csrSession.getFormRow();
		try
		{
			Set keySet = formRow.keySet();
			Iterator keyItr = keySet.iterator();
			int count=0;
			while(keyItr.hasNext())
			{
				count++;
				Long theKey = (Long) keyItr.next();
				CreateSalesReturnSession.ReturnItem rtnRow = (CreateSalesReturnSession.ReturnItem) formRow.get(theKey);

				InvoiceItemObject iiObj = InvoiceItemNut.getObject(rtnRow.invoiceItemId);
				if(iiObj!=null)
				{
					if(iiObj.mStrName3.equals(SalesOrderItemBean.TABLENAME+SalesOrderItemBean.UUID) && iiObj.mStrValue3.length()>10)
					{
						QueryObject query2 = new QueryObject(new String[]{ SalesOrderItemBean.UUID+" ='"+iiObj.mStrValue3+"' "});
						Vector vecSOI = new Vector(SalesOrderItemNut.getObjects(query2));
						for(int cnt2=0;cnt2<vecSOI.size();cnt2++)
						{ SalesOrderItemObject soiObj = (SalesOrderItemObject) vecSOI.get(cnt2); }
					}

					for(int cnt1=0;cnt1<rtnRow.vecInvoiceItemSerial.size();cnt1++)
					{ 
						String serial = (String) rtnRow.vecInvoiceItemSerial.get(cnt1); 
					}


					Vector vecNotReturned = rtnRow.getSerialForReturn();
					if(rtnRow.serialized)
					{
						for(int cnt1=0;cnt1<vecNotReturned.size();cnt1++)
						{ 
							String serial = (String) vecNotReturned.get(cnt1);
							csrSession.addReturn(rtnRow.invoiceItemId, rtnRow.returningPrice, "CANCEL CASHSALE", serial);
         			}
					}
					else
					{ 
						csrSession.addReturn(rtnRow.invoiceItemId, rtnRow.returningPrice, "CANCEL CASHSALE", rtnRow.invoiceItemQty);
					}
				}
	
			} /// end while	
			csrSession.confirmAndSave();

		/// use the credit memo to CONTRA the Invoice
			CreditMemoIndexObject cmObj = csrSession.getCreditMemo();
			ContraCustCreditMemoSession contraCreditMemoForm = new ContraCustCreditMemoSession(cmObj.pkid,userId);
			TreeMap docTree = contraCreditMemoForm.getDocumentTree();
			Vector vecDocTree = new Vector(docTree.values());
			for(int cnt1=0;cnt1<vecDocTree.size();cnt1++)
			{
				ContraCustCreditMemoSession.Document oneDoc = (ContraCustCreditMemoSession.Document) vecDocTree.get(cnt1);
				if(oneDoc.docTable.equals(InvoiceBean.TABLENAME)&& oneDoc.docKey.equals(invObj.mPkid))
				{
					contraCreditMemoForm.setContra(oneDoc.rowKey,cmObj.balance);
					continue;
				}
			}
			contraCreditMemoForm.confirmAndSave();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
	}

	public void loadCashsale(Long cashsaleNo) throws Exception
	{
		this.invoiceObj = InvoiceNut.getObject(cashsaleNo);
Log.printVerbose(" CancelCashsale checkpoint 1");
		if(this.invoiceObj==null)
		{ throw new Exception("Invalid Cashsale No "+cashsaleNo.toString()+"!");}
Log.printVerbose(" CancelCashsale checkpoint 2");
		/// check if there's previous sales return
		Vector vecSalesReturnItem = SalesReturnItemNut.getObjectsByInvoice(cashsaleNo);
		if(vecSalesReturnItem!=null && vecSalesReturnItem.size()>0)
		{
Log.printVerbose(" CancelCashsale checkpoint 3");
			String srNumberList = " ";
			for(int cnt1=0;cnt1<vecSalesReturnItem.size();cnt1++)
			{
Log.printVerbose(" CancelCashsale checkpoint 4");
				SalesReturnItemObject sriObj = (SalesReturnItemObject) vecSalesReturnItem.get(cnt1);
				srNumberList += " SR"+sriObj.mSalesReturnId.toString();
			}
			reset();
			throw new Exception("There are Sales Return for this cashsale, you cannot cancel it! "+ srNumberList);
		}

		/// check if multiple documents are linked to this invoice
		this.vecDocLink = new Vector(DocLinkNut.getByTargetDoc(InvoiceBean.TABLENAME, this.invoiceObj.mPkid));
Log.printVerbose(" CancelCashsale checkpoint 5");

		int countReceipt = 0;
		int countCM = 0;
		int countSO = 0;
		int countOther = 0;
		String isSameAmt = "";

		for(int cnt1=0;cnt1<this.vecDocLink.size();cnt1++)
		{
Log.printVerbose(" CancelCashsale checkpoint 6");
			DocLinkObject dlObj = (DocLinkObject) this.vecDocLink.get(0);
			if(dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
			{
				Log.printVerbose(" CancelCashsale:Found one receipt"+dlObj.srcDocId.toString());
				countReceipt++;
				OfficialReceiptObject rctObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
				if(rctObj!=null)
				{
					if(rctObj.amount.compareTo(this.invoiceObj.mTotalAmt)==0)
					{
						// It invoice and receipt amount matches. this is good.
					}
					else
					{
						isSameAmt = "yes";
					}
				}
				Log.printVerbose(" countReceipt = "+countReceipt);
			}
			else if(dlObj.srcDocRef.equals(CreditMemoIndexBean.TABLENAME))
			{
				Log.printVerbose(" CancelCashsale:Found one cm "+dlObj.srcDocId.toString());
				countCM ++;
				Log.printVerbose(" countCM = "+countCM);
			}
			else if(dlObj.srcDocRef.equals(SalesOrderIndexBean.TABLENAME))
			{
				Log.printVerbose(" CancelCashsale:Found one SO "+dlObj.srcDocId.toString());
				countSO ++;
				Log.printVerbose(" countSO = "+countSO);
			}
			else 
			{
				Log.printVerbose(" CancelCashsale:Found OTHER "+dlObj.srcDocRef+" No:"+dlObj.srcDocId.toString());
				countOther ++;
				Log.printVerbose(" countOther = "+countOther);
			}
		}

Log.printVerbose(" CancelCashsale checkpoint 7");
		if(countCM>0)
		{
			reset();
			throw new Exception("There's a credit memo link to this cashsale, cannot cancel the cashsale directly.."); 
		}
Log.printVerbose(" CancelCashsale checkpoint 8");
		if(countReceipt>1)
		{
			reset();
			throw new Exception("More than one receipt is detected ("+countReceipt+"), cannot cancel the cashsale... ");
		}

		
		if(isSameAmt.equals("yes"))
		{
			reset();
			throw new Exception("Amount values of invoice and receipt does not match... ");
		}
Log.printVerbose(" CancelCashsale checkpoint 9");
		/// find the receipt objects
		if(this.vecDocLink.size()>0 && countReceipt==1)
		{
			DocLinkObject dlObj = (DocLinkObject)this.vecDocLink.get(0);
			if(dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
			{
				this.receiptObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
			}
		}

Log.printVerbose(" CancelCashsale checkpoint 10");
		//// check if the receipt has been reversed
		if(this.receiptObj!=null)
		{
Log.printVerbose(" CancelCashsale checkpoint 11");
			if(this.receiptObj.state.equals(OfficialReceiptBean.ST_REVERSED))
			{
Log.printVerbose(" CancelCashsale checkpoint 12");
				reset();
				throw new Exception("The receipt "+this.receiptObj.pkid.toString()+" has been reversed!");
			}	
		}
Log.printVerbose(" CancelCashsale checkpoint 13");
	}

	public InvoiceObject getInvoice()
	{ return this.invoiceObj; } 

	public OfficialReceiptObject getReceipt()
	{ return this.receiptObj; }


}


