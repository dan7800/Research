/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlySalesReportType01 implements Action
{
	String strClassName = "DoMgtMonthlySalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		String strInternal = req.getParameter("strInternal");
		String strExternal = req.getParameter("strExternal");
		req.setAttribute("strInternal", strInternal);
		req.setAttribute("strExternal", strExternal);
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-sales-report-type01-page");
		// return new ActionRouter("mgt-daily-sales-report-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strTheDate = req.getParameter("theDate");
			req.setAttribute("theDate", strTheDate);
			if (strPCCenter == null || currency == null || strTheDate == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			// ////////////////////////////////////////////////////////////
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
			if (strTheDate == null)
			{
				strTheDate = TimeFormat.strDisplayDate();
			}
			Timestamp tsEnd = TimeFormat.createTimestamp(strTheDate);
			int numOfDays = TimeFormat.get(tsEnd, Calendar.DATE);
			Timestamp tsStart = TimeFormat.set(tsEnd, Calendar.DATE, 1);
			tsEnd = TimeFormat.add(tsEnd, 0, 0, 1);
			Log.printVerbose("numOfDays = " + numOfDays);
			String strOption = "active";
			Log.printVerbose(" before calling na nut to retrieve na txns !!");
			Vector vecPerDayInvRecExternalDate = new Vector();
			Vector vecPerDayInvRecExternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecExternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecExternalCash = new Vector();
			Vector vecPerDayInvRecExternalCheque = new Vector();
			Vector vecPerDayInvRecExternalOther = new Vector();
			Vector vecPerDayInvRecExternalCreditCard = new Vector();
			Vector vecPerDayInvRecExternalTerms = new Vector();
			Vector vecPerDayRecOnlyExternalDate = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyExternalCash = new Vector();
			Vector vecPerDayRecOnlyExternalCheque = new Vector();
			Vector vecPerDayRecOnlyExternalOther = new Vector();
			Vector vecPerDayRecOnlyExternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyExternalTerms = new Vector();
			Vector vecPerDayInvRecInternalDate = new Vector();
			Vector vecPerDayInvRecInternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecInternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecInternalCash = new Vector();
			Vector vecPerDayInvRecInternalCheque = new Vector();
			Vector vecPerDayInvRecInternalOther = new Vector();
			Vector vecPerDayInvRecInternalCreditCard = new Vector();
			Vector vecPerDayInvRecInternalTerms = new Vector();
			Vector vecPerDayRecOnlyInternalDate = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyInternalCash = new Vector();
			Vector vecPerDayRecOnlyInternalCheque = new Vector();
			Vector vecPerDayRecOnlyInternalOther = new Vector();
			Vector vecPerDayRecOnlyInternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyInternalTerms = new Vector();
			for (int countDay = 1; countDay <= numOfDays; countDay++)
			{
				Timestamp tsFrom = TimeFormat.set(tsStart, Calendar.DATE, countDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount
				// / in the list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						}// end if
						else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}// end for count2
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
					} else
					{
						vecInvRec.add(naObj);
					} // end if bdTotalInvoice
				}// end for count1
				Vector vecInvRecExternal = new Vector();
				Vector vecInvRecInternal = new Vector();
				for (int count1 = 0; count1 < vecInvRec.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRec.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecInvRecInternal.add(naObj);
							} else
							{
								vecInvRecExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
				Vector vecRecOnlyExternal = new Vector();
				Vector vecRecOnlyInternal = new Vector();
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					} // end if naObj.foreignTable
				} // end if count1
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecExternalDate.add(tsFrom);
				vecPerDayInvRecExternalInvoiceNum.add(new Integer(vecInvRecExternal.size()));
				BigDecimal bdInvoiceIRE = new BigDecimal("0.00");
				BigDecimal bdCashIRE = new BigDecimal("0.00");
				BigDecimal bdChequeIRE = new BigDecimal("0.00");
				BigDecimal bdOtherIRE = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRE = new BigDecimal("0.00");
				BigDecimal bdTermsIRE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRE.add(thisInvoiceAmount.abs());
							// bdInvoiceIRE =
							// bdInvoiceIRE.add(natObj.amount.abs());
							BigDecimal bdInvoiceIREAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRE = bdInvoiceIRE.add(bdInvoiceIREAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIREAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRE = bdCashIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRE = bdChequeIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRE = bdOtherIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRE = bdCreditCardIRE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecExternalInvoiceSum.add(bdInvoiceIRE);
				vecPerDayInvRecExternalCash.add(bdCashIRE);
				vecPerDayInvRecExternalCheque.add(bdChequeIRE);
				vecPerDayInvRecExternalOther.add(bdOtherIRE);
				vecPerDayInvRecExternalCreditCard.add(bdCreditCardIRE);
				bdTermsIRE = bdInvoiceIRE.subtract(bdCashIRE.add(bdChequeIRE.add(bdOtherIRE.add(bdCreditCardIRE))));
				vecPerDayInvRecExternalTerms.add(bdTermsIRE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyExternalDate.add(tsFrom);
				vecPerDayRecOnlyExternalInvoiceNum.add(new Integer(vecRecOnlyExternal.size()));
				BigDecimal bdInvoiceROE = new BigDecimal("0.00");
				BigDecimal bdCashROE = new BigDecimal("0.00");
				BigDecimal bdChequeROE = new BigDecimal("0.00");
				BigDecimal bdOtherROE = new BigDecimal("0.00");
				BigDecimal bdCreditCardROE = new BigDecimal("0.00");
				BigDecimal bdTermsROE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROE.add(thisInvoiceAmount.abs());
							// bdInvoiceROE =
							// bdInvoiceROE.add(natObj.amount.abs());
							BigDecimal bdInvoiceROEAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROE = bdInvoiceROE.add(bdInvoiceROEAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROEAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROE = bdCashROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROE = bdChequeROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROE = bdOtherROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROE = bdCreditCardROE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyExternalInvoiceSum.add(bdInvoiceROE);
				vecPerDayRecOnlyExternalCash.add(bdCashROE);
				vecPerDayRecOnlyExternalCheque.add(bdChequeROE);
				vecPerDayRecOnlyExternalOther.add(bdOtherROE);
				vecPerDayRecOnlyExternalCreditCard.add(bdCreditCardROE);
				bdTermsROE = new BigDecimal("0.00");
				// bdInvoiceROE.subtract( bdCashROE.add( bdChequeROE.add(
				// bdOtherROE.add(bdCreditCardROE))));
				vecPerDayRecOnlyExternalTerms.add(bdTermsROE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ///////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecInternalDate.add(tsFrom);
				vecPerDayInvRecInternalInvoiceNum.add(new Integer(vecInvRecInternal.size()));
				BigDecimal bdInvoiceIRI = new BigDecimal("0.00");
				BigDecimal bdCashIRI = new BigDecimal("0.00");
				BigDecimal bdChequeIRI = new BigDecimal("0.00");
				BigDecimal bdOtherIRI = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRI = new BigDecimal("0.00");
				BigDecimal bdTermsIRI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRI.add(thisInvoiceAmount.abs());
							// bdInvoiceIRI =
							// bdInvoiceIRI.add(natObj.amount.abs());
							BigDecimal bdInvoiceIRIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRI = bdInvoiceIRI.add(bdInvoiceIRIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIRIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRI = bdCashIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRI = bdChequeIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRI = bdOtherIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRI = bdCreditCardIRI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecInternalInvoiceSum.add(bdInvoiceIRI);
				vecPerDayInvRecInternalCash.add(bdCashIRI);
				vecPerDayInvRecInternalCheque.add(bdChequeIRI);
				vecPerDayInvRecInternalOther.add(bdOtherIRI);
				vecPerDayInvRecInternalCreditCard.add(bdCreditCardIRI);
				bdTermsIRI = bdInvoiceIRI.subtract(bdCashIRI.add(bdChequeIRI.add(bdOtherIRI.add(bdCreditCardIRI))));
				vecPerDayInvRecInternalTerms.add(bdTermsIRI);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// //////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyInternalDate.add(tsFrom);
				vecPerDayRecOnlyInternalInvoiceNum.add(new Integer(vecRecOnlyInternal.size()));
				BigDecimal bdInvoiceROI = new BigDecimal("0.00");
				BigDecimal bdCashROI = new BigDecimal("0.00");
				BigDecimal bdChequeROI = new BigDecimal("0.00");
				BigDecimal bdOtherROI = new BigDecimal("0.00");
				BigDecimal bdCreditCardROI = new BigDecimal("0.00");
				BigDecimal bdTermsROI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROI.add(thisInvoiceAmount.abs());
							// bdInvoiceROI =
							// bdInvoiceROI.add(natObj.amount.abs());
							BigDecimal bdInvoiceROIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROI = bdInvoiceROI.add(bdInvoiceROIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROI = bdCashROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROI = bdChequeROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROI = bdOtherROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROI = bdCreditCardROI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyInternalInvoiceSum.add(bdInvoiceROI);
				vecPerDayRecOnlyInternalCash.add(bdCashROI);
				vecPerDayRecOnlyInternalCheque.add(bdChequeROI);
				vecPerDayRecOnlyInternalOther.add(bdOtherROI);
				vecPerDayRecOnlyInternalCreditCard.add(bdCreditCardROI);
				bdTermsROI = new BigDecimal("0.00");
				// bdInvoiceROI.subtract( bdCashROI.add( bdChequeROI.add(
				// bdOtherROI.add(bdCreditCardROI))));
				vecPerDayRecOnlyInternalTerms.add(bdTermsROI);
				// /////////////////////////////////////////////////////////////////
			} // end for countDay
			req.setAttribute("vecPerDayRecOnlyExternalDate", vecPerDayRecOnlyExternalDate);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceNum", vecPerDayRecOnlyExternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceSum", vecPerDayRecOnlyExternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyExternalCash", vecPerDayRecOnlyExternalCash);
			req.setAttribute("vecPerDayRecOnlyExternalCheque", vecPerDayRecOnlyExternalCheque);
			req.setAttribute("vecPerDayRecOnlyExternalOther", vecPerDayRecOnlyExternalOther);
			req.setAttribute("vecPerDayRecOnlyExternalCreditCard", vecPerDayRecOnlyExternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyExternalTerms", vecPerDayRecOnlyExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecExternalDate", vecPerDayInvRecExternalDate);
			req.setAttribute("vecPerDayInvRecExternalInvoiceNum", vecPerDayInvRecExternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecExternalInvoiceSum", vecPerDayInvRecExternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecExternalCash", vecPerDayInvRecExternalCash);
			req.setAttribute("vecPerDayInvRecExternalCheque", vecPerDayInvRecExternalCheque);
			req.setAttribute("vecPerDayInvRecExternalOther", vecPerDayInvRecExternalOther);
			req.setAttribute("vecPerDayInvRecExternalCreditCard", vecPerDayInvRecExternalCreditCard);
			req.setAttribute("vecPerDayInvRecExternalTerms", vecPerDayInvRecExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecInternalDate", vecPerDayInvRecInternalDate);
			req.setAttribute("vecPerDayInvRecInternalInvoiceNum", vecPerDayInvRecInternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecInternalInvoiceSum", vecPerDayInvRecInternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecInternalCash", vecPerDayInvRecInternalCash);
			req.setAttribute("vecPerDayInvRecInternalCheque", vecPerDayInvRecInternalCheque);
			req.setAttribute("vecPerDayInvRecInternalOther", vecPerDayInvRecInternalOther);
			req.setAttribute("vecPerDayInvRecInternalCreditCard", vecPerDayInvRecInternalCreditCard);
			req.setAttribute("vecPerDayInvRecInternalTerms", vecPerDayInvRecInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayRecOnlyInternalDate", vecPerDayRecOnlyInternalDate);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceNum", vecPerDayRecOnlyInternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceSum", vecPerDayRecOnlyInternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyInternalCash", vecPerDayRecOnlyInternalCash);
			req.setAttribute("vecPerDayRecOnlyInternalCheque", vecPerDayRecOnlyInternalCheque);
			req.setAttribute("vecPerDayRecOnlyInternalOther", vecPerDayRecOnlyInternalOther);
			req.setAttribute("vecPerDayRecOnlyInternalCreditCard", vecPerDayRecOnlyInternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyInternalTerms", vecPerDayRecOnlyInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			// ////////////////////////////////////////////////////////////////////
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlySalesReportType01
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlySalesReportType01 implements Action
{
	String strClassName = "DoMgtMonthlySalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		String strInternal = req.getParameter("strInternal");
		String strExternal = req.getParameter("strExternal");
		req.setAttribute("strInternal", strInternal);
		req.setAttribute("strExternal", strExternal);
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-sales-report-type01-page");
		// return new ActionRouter("mgt-daily-sales-report-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strTheDate = req.getParameter("theDate");
			req.setAttribute("theDate", strTheDate);
			if (strPCCenter == null || currency == null || strTheDate == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			// ////////////////////////////////////////////////////////////
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
			if (strTheDate == null)
			{
				strTheDate = TimeFormat.strDisplayDate();
			}
			Timestamp tsEnd = TimeFormat.createTimestamp(strTheDate);
			int numOfDays = TimeFormat.get(tsEnd, Calendar.DATE);
			Timestamp tsStart = TimeFormat.set(tsEnd, Calendar.DATE, 1);
			tsEnd = TimeFormat.add(tsEnd, 0, 0, 1);
			Log.printVerbose("numOfDays = " + numOfDays);
			String strOption = "active";
			Log.printVerbose(" before calling na nut to retrieve na txns !!");
			Vector vecPerDayInvRecExternalDate = new Vector();
			Vector vecPerDayInvRecExternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecExternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecExternalCash = new Vector();
			Vector vecPerDayInvRecExternalCheque = new Vector();
			Vector vecPerDayInvRecExternalOther = new Vector();
			Vector vecPerDayInvRecExternalCreditCard = new Vector();
			Vector vecPerDayInvRecExternalTerms = new Vector();
			Vector vecPerDayRecOnlyExternalDate = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyExternalCash = new Vector();
			Vector vecPerDayRecOnlyExternalCheque = new Vector();
			Vector vecPerDayRecOnlyExternalOther = new Vector();
			Vector vecPerDayRecOnlyExternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyExternalTerms = new Vector();
			Vector vecPerDayInvRecInternalDate = new Vector();
			Vector vecPerDayInvRecInternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecInternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecInternalCash = new Vector();
			Vector vecPerDayInvRecInternalCheque = new Vector();
			Vector vecPerDayInvRecInternalOther = new Vector();
			Vector vecPerDayInvRecInternalCreditCard = new Vector();
			Vector vecPerDayInvRecInternalTerms = new Vector();
			Vector vecPerDayRecOnlyInternalDate = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyInternalCash = new Vector();
			Vector vecPerDayRecOnlyInternalCheque = new Vector();
			Vector vecPerDayRecOnlyInternalOther = new Vector();
			Vector vecPerDayRecOnlyInternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyInternalTerms = new Vector();
			for (int countDay = 1; countDay <= numOfDays; countDay++)
			{
				Timestamp tsFrom = TimeFormat.set(tsStart, Calendar.DATE, countDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount
				// / in the list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						}// end if
						else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}// end for count2
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
					} else
					{
						vecInvRec.add(naObj);
					} // end if bdTotalInvoice
				}// end for count1
				Vector vecInvRecExternal = new Vector();
				Vector vecInvRecInternal = new Vector();
				for (int count1 = 0; count1 < vecInvRec.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRec.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecInvRecInternal.add(naObj);
							} else
							{
								vecInvRecExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
				Vector vecRecOnlyExternal = new Vector();
				Vector vecRecOnlyInternal = new Vector();
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					} // end if naObj.foreignTable
				} // end if count1
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecExternalDate.add(tsFrom);
				vecPerDayInvRecExternalInvoiceNum.add(new Integer(vecInvRecExternal.size()));
				BigDecimal bdInvoiceIRE = new BigDecimal("0.00");
				BigDecimal bdCashIRE = new BigDecimal("0.00");
				BigDecimal bdChequeIRE = new BigDecimal("0.00");
				BigDecimal bdOtherIRE = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRE = new BigDecimal("0.00");
				BigDecimal bdTermsIRE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRE.add(thisInvoiceAmount.abs());
							// bdInvoiceIRE =
							// bdInvoiceIRE.add(natObj.amount.abs());
							BigDecimal bdInvoiceIREAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRE = bdInvoiceIRE.add(bdInvoiceIREAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIREAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRE = bdCashIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRE = bdChequeIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRE = bdOtherIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRE = bdCreditCardIRE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecExternalInvoiceSum.add(bdInvoiceIRE);
				vecPerDayInvRecExternalCash.add(bdCashIRE);
				vecPerDayInvRecExternalCheque.add(bdChequeIRE);
				vecPerDayInvRecExternalOther.add(bdOtherIRE);
				vecPerDayInvRecExternalCreditCard.add(bdCreditCardIRE);
				bdTermsIRE = bdInvoiceIRE.subtract(bdCashIRE.add(bdChequeIRE.add(bdOtherIRE.add(bdCreditCardIRE))));
				vecPerDayInvRecExternalTerms.add(bdTermsIRE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyExternalDate.add(tsFrom);
				vecPerDayRecOnlyExternalInvoiceNum.add(new Integer(vecRecOnlyExternal.size()));
				BigDecimal bdInvoiceROE = new BigDecimal("0.00");
				BigDecimal bdCashROE = new BigDecimal("0.00");
				BigDecimal bdChequeROE = new BigDecimal("0.00");
				BigDecimal bdOtherROE = new BigDecimal("0.00");
				BigDecimal bdCreditCardROE = new BigDecimal("0.00");
				BigDecimal bdTermsROE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROE.add(thisInvoiceAmount.abs());
							// bdInvoiceROE =
							// bdInvoiceROE.add(natObj.amount.abs());
							BigDecimal bdInvoiceROEAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROE = bdInvoiceROE.add(bdInvoiceROEAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROEAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROE = bdCashROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROE = bdChequeROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROE = bdOtherROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROE = bdCreditCardROE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyExternalInvoiceSum.add(bdInvoiceROE);
				vecPerDayRecOnlyExternalCash.add(bdCashROE);
				vecPerDayRecOnlyExternalCheque.add(bdChequeROE);
				vecPerDayRecOnlyExternalOther.add(bdOtherROE);
				vecPerDayRecOnlyExternalCreditCard.add(bdCreditCardROE);
				bdTermsROE = new BigDecimal("0.00");
				// bdInvoiceROE.subtract( bdCashROE.add( bdChequeROE.add(
				// bdOtherROE.add(bdCreditCardROE))));
				vecPerDayRecOnlyExternalTerms.add(bdTermsROE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ///////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecInternalDate.add(tsFrom);
				vecPerDayInvRecInternalInvoiceNum.add(new Integer(vecInvRecInternal.size()));
				BigDecimal bdInvoiceIRI = new BigDecimal("0.00");
				BigDecimal bdCashIRI = new BigDecimal("0.00");
				BigDecimal bdChequeIRI = new BigDecimal("0.00");
				BigDecimal bdOtherIRI = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRI = new BigDecimal("0.00");
				BigDecimal bdTermsIRI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRI.add(thisInvoiceAmount.abs());
							// bdInvoiceIRI =
							// bdInvoiceIRI.add(natObj.amount.abs());
							BigDecimal bdInvoiceIRIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRI = bdInvoiceIRI.add(bdInvoiceIRIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIRIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRI = bdCashIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRI = bdChequeIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRI = bdOtherIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRI = bdCreditCardIRI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecInternalInvoiceSum.add(bdInvoiceIRI);
				vecPerDayInvRecInternalCash.add(bdCashIRI);
				vecPerDayInvRecInternalCheque.add(bdChequeIRI);
				vecPerDayInvRecInternalOther.add(bdOtherIRI);
				vecPerDayInvRecInternalCreditCard.add(bdCreditCardIRI);
				bdTermsIRI = bdInvoiceIRI.subtract(bdCashIRI.add(bdChequeIRI.add(bdOtherIRI.add(bdCreditCardIRI))));
				vecPerDayInvRecInternalTerms.add(bdTermsIRI);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// //////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyInternalDate.add(tsFrom);
				vecPerDayRecOnlyInternalInvoiceNum.add(new Integer(vecRecOnlyInternal.size()));
				BigDecimal bdInvoiceROI = new BigDecimal("0.00");
				BigDecimal bdCashROI = new BigDecimal("0.00");
				BigDecimal bdChequeROI = new BigDecimal("0.00");
				BigDecimal bdOtherROI = new BigDecimal("0.00");
				BigDecimal bdCreditCardROI = new BigDecimal("0.00");
				BigDecimal bdTermsROI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROI.add(thisInvoiceAmount.abs());
							// bdInvoiceROI =
							// bdInvoiceROI.add(natObj.amount.abs());
							BigDecimal bdInvoiceROIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROI = bdInvoiceROI.add(bdInvoiceROIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROI = bdCashROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROI = bdChequeROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROI = bdOtherROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROI = bdCreditCardROI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyInternalInvoiceSum.add(bdInvoiceROI);
				vecPerDayRecOnlyInternalCash.add(bdCashROI);
				vecPerDayRecOnlyInternalCheque.add(bdChequeROI);
				vecPerDayRecOnlyInternalOther.add(bdOtherROI);
				vecPerDayRecOnlyInternalCreditCard.add(bdCreditCardROI);
				bdTermsROI = new BigDecimal("0.00");
				// bdInvoiceROI.subtract( bdCashROI.add( bdChequeROI.add(
				// bdOtherROI.add(bdCreditCardROI))));
				vecPerDayRecOnlyInternalTerms.add(bdTermsROI);
				// /////////////////////////////////////////////////////////////////
			} // end for countDay
			req.setAttribute("vecPerDayRecOnlyExternalDate", vecPerDayRecOnlyExternalDate);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceNum", vecPerDayRecOnlyExternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceSum", vecPerDayRecOnlyExternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyExternalCash", vecPerDayRecOnlyExternalCash);
			req.setAttribute("vecPerDayRecOnlyExternalCheque", vecPerDayRecOnlyExternalCheque);
			req.setAttribute("vecPerDayRecOnlyExternalOther", vecPerDayRecOnlyExternalOther);
			req.setAttribute("vecPerDayRecOnlyExternalCreditCard", vecPerDayRecOnlyExternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyExternalTerms", vecPerDayRecOnlyExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecExternalDate", vecPerDayInvRecExternalDate);
			req.setAttribute("vecPerDayInvRecExternalInvoiceNum", vecPerDayInvRecExternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecExternalInvoiceSum", vecPerDayInvRecExternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecExternalCash", vecPerDayInvRecExternalCash);
			req.setAttribute("vecPerDayInvRecExternalCheque", vecPerDayInvRecExternalCheque);
			req.setAttribute("vecPerDayInvRecExternalOther", vecPerDayInvRecExternalOther);
			req.setAttribute("vecPerDayInvRecExternalCreditCard", vecPerDayInvRecExternalCreditCard);
			req.setAttribute("vecPerDayInvRecExternalTerms", vecPerDayInvRecExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecInternalDate", vecPerDayInvRecInternalDate);
			req.setAttribute("vecPerDayInvRecInternalInvoiceNum", vecPerDayInvRecInternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecInternalInvoiceSum", vecPerDayInvRecInternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecInternalCash", vecPerDayInvRecInternalCash);
			req.setAttribute("vecPerDayInvRecInternalCheque", vecPerDayInvRecInternalCheque);
			req.setAttribute("vecPerDayInvRecInternalOther", vecPerDayInvRecInternalOther);
			req.setAttribute("vecPerDayInvRecInternalCreditCard", vecPerDayInvRecInternalCreditCard);
			req.setAttribute("vecPerDayInvRecInternalTerms", vecPerDayInvRecInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayRecOnlyInternalDate", vecPerDayRecOnlyInternalDate);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceNum", vecPerDayRecOnlyInternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceSum", vecPerDayRecOnlyInternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyInternalCash", vecPerDayRecOnlyInternalCash);
			req.setAttribute("vecPerDayRecOnlyInternalCheque", vecPerDayRecOnlyInternalCheque);
			req.setAttribute("vecPerDayRecOnlyInternalOther", vecPerDayRecOnlyInternalOther);
			req.setAttribute("vecPerDayRecOnlyInternalCreditCard", vecPerDayRecOnlyInternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyInternalTerms", vecPerDayRecOnlyInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			// ////////////////////////////////////////////////////////////////////
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlySalesReportType01
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.management;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoMgtMonthlySalesReportType01 implements Action
{
	String strClassName = "DoMgtMonthlySalesReportType01";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String fwdPage = (String) req.getParameter("fwdPage");
		String strInternal = req.getParameter("strInternal");
		String strExternal = req.getParameter("strExternal");
		req.setAttribute("strInternal", strInternal);
		req.setAttribute("strExternal", strExternal);
		Vector vecPCCenter = ProfitCostCenterNut.getValueObjectsGiven(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.STATUS_ACTIVE, (String) null, (String) null);
		req.setAttribute("vecPCCenter", vecPCCenter);
		fnGetReportType01Values(servlet, req, res);
		if (fwdPage != null)
		{
			return new ActionRouter(fwdPage);
		}
		return new ActionRouter("mgt-monthly-sales-report-type01-page");
		// return new ActionRouter("mgt-daily-sales-report-page");
	}

	public void fnGetReportType01Values(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
			String strPCCenter = req.getParameter("pcCenter");
			req.setAttribute("strPCCenter", strPCCenter);
			String currency = req.getParameter("currency");
			req.setAttribute("currency", currency);
			String strTheDate = req.getParameter("theDate");
			req.setAttribute("theDate", strTheDate);
			if (strPCCenter == null || currency == null || strTheDate == null)
			{
				return;
			}
			Integer pcCenter = null;
			try
			{
				pcCenter = new Integer(strPCCenter);
			} catch (Exception ex)
			{ // do nothing, when "all" is selected
			}
			// // NominalAccount ForeignTable
			// /// NOT NominalAccountTxn ForeignTable
			// ////////////////////////////////////////////////////////////
			String naForeignTable = NominalAccountBean.FT_CUSTOMER;
			Integer naForeignKey = null;// new Integer("0");
			Long natForeignKey = null;
			String natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
			if (strTheDate == null)
			{
				strTheDate = TimeFormat.strDisplayDate();
			}
			Timestamp tsEnd = TimeFormat.createTimestamp(strTheDate);
			int numOfDays = TimeFormat.get(tsEnd, Calendar.DATE);
			Timestamp tsStart = TimeFormat.set(tsEnd, Calendar.DATE, 1);
			tsEnd = TimeFormat.add(tsEnd, 0, 0, 1);
			Log.printVerbose("numOfDays = " + numOfDays);
			String strOption = "active";
			Log.printVerbose(" before calling na nut to retrieve na txns !!");
			Vector vecPerDayInvRecExternalDate = new Vector();
			Vector vecPerDayInvRecExternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecExternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecExternalCash = new Vector();
			Vector vecPerDayInvRecExternalCheque = new Vector();
			Vector vecPerDayInvRecExternalOther = new Vector();
			Vector vecPerDayInvRecExternalCreditCard = new Vector();
			Vector vecPerDayInvRecExternalTerms = new Vector();
			Vector vecPerDayRecOnlyExternalDate = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyExternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyExternalCash = new Vector();
			Vector vecPerDayRecOnlyExternalCheque = new Vector();
			Vector vecPerDayRecOnlyExternalOther = new Vector();
			Vector vecPerDayRecOnlyExternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyExternalTerms = new Vector();
			Vector vecPerDayInvRecInternalDate = new Vector();
			Vector vecPerDayInvRecInternalInvoiceNum = new Vector();
			Vector vecPerDayInvRecInternalInvoiceSum = new Vector();
			Vector vecPerDayInvRecInternalCash = new Vector();
			Vector vecPerDayInvRecInternalCheque = new Vector();
			Vector vecPerDayInvRecInternalOther = new Vector();
			Vector vecPerDayInvRecInternalCreditCard = new Vector();
			Vector vecPerDayInvRecInternalTerms = new Vector();
			Vector vecPerDayRecOnlyInternalDate = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceNum = new Vector();
			Vector vecPerDayRecOnlyInternalInvoiceSum = new Vector();
			Vector vecPerDayRecOnlyInternalCash = new Vector();
			Vector vecPerDayRecOnlyInternalCheque = new Vector();
			Vector vecPerDayRecOnlyInternalOther = new Vector();
			Vector vecPerDayRecOnlyInternalCreditCard = new Vector();
			Vector vecPerDayRecOnlyInternalTerms = new Vector();
			for (int countDay = 1; countDay <= numOfDays; countDay++)
			{
				Timestamp tsFrom = TimeFormat.set(tsStart, Calendar.DATE, countDay);
				Timestamp tsTo = TimeFormat.add(tsFrom, 0, 0, 1);
				// / first, retrieve all invoices posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_INVOICE;
				Vector vecNaInv = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				// / next, retrieve all receipts posted to nominal account
				// / on a particular day
				natForeignTable = NominalAccountTxnBean.FT_CUST_RECEIPT;
				Vector vecNaRec = NominalAccountNut.getValueObjectsGiven(pcCenter, naForeignTable, naForeignKey,
						currency, natForeignTable, natForeignKey, tsFrom, tsTo, strOption);
				vecNaInv.addAll(vecNaRec);
				// / next find out if there's duplicate of NominalAccount
				// / in the list
				// / above, if there are, merge them
				Vector vecMerged = NominalAccountNut.fnMergeNominalAccountWithSameID(vecNaInv);
				Vector vecInvRec = new Vector();
				Vector vecRecOnly = new Vector();
				Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 3");
				for (int count1 = 0; count1 < vecMerged.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecMerged.get(count1);
					BigDecimal bdTotalInvoice = new BigDecimal("0.00");
					BigDecimal bdTotalReceipt = new BigDecimal("0.00");
					Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 4");
					for (int count2 = 0; count2 < naObj.vecNominalAccountTxn.size(); count2++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count2);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalInvoice = bdTotalInvoice.add(natObj.amount);
						}// end if
						else if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Log.printVerbose("xxxxxxxxxxxxxxxxxxxxxxxxxx 1");
							bdTotalReceipt = bdTotalReceipt.add(natObj.amount);
						}
					}// end for count2
					if (bdTotalInvoice.signum() == 0)
					{
						// receipts only vector
						vecRecOnly.add(naObj);
					} else
					{
						vecInvRec.add(naObj);
					} // end if bdTotalInvoice
				}// end for count1
				Vector vecInvRecExternal = new Vector();
				Vector vecInvRecInternal = new Vector();
				for (int count1 = 0; count1 < vecInvRec.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRec.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecInvRecInternal.add(naObj);
							} else
							{
								vecInvRecExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
				Vector vecRecOnlyExternal = new Vector();
				Vector vecRecOnlyInternal = new Vector();
				for (int count1 = 0; count1 < vecRecOnly.size(); count1++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnly.get(count1);
					if (naObj.foreignTable.equals(NominalAccountBean.FT_CUSTOMER))
					{
						try
						{
							CustAccount caEJB = CustAccountNut.getHandle(naObj.foreignKey);
							Integer iType = caEJB.getAccType();
							if (iType.intValue() == CustAccountBean.ACCTYPE_CORPORATE.intValue())
							{
								vecRecOnlyInternal.add(naObj);
							} else
							{
								vecRecOnlyExternal.add(naObj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					} // end if naObj.foreignTable
				} // end if count1
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecExternalDate.add(tsFrom);
				vecPerDayInvRecExternalInvoiceNum.add(new Integer(vecInvRecExternal.size()));
				BigDecimal bdInvoiceIRE = new BigDecimal("0.00");
				BigDecimal bdCashIRE = new BigDecimal("0.00");
				BigDecimal bdChequeIRE = new BigDecimal("0.00");
				BigDecimal bdOtherIRE = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRE = new BigDecimal("0.00");
				BigDecimal bdTermsIRE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRE.add(thisInvoiceAmount.abs());
							// bdInvoiceIRE =
							// bdInvoiceIRE.add(natObj.amount.abs());
							BigDecimal bdInvoiceIREAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRE = bdInvoiceIRE.add(bdInvoiceIREAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIREAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRE = bdCashIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRE = bdChequeIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRE = bdOtherIRE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRE = bdCreditCardIRE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecExternalInvoiceSum.add(bdInvoiceIRE);
				vecPerDayInvRecExternalCash.add(bdCashIRE);
				vecPerDayInvRecExternalCheque.add(bdChequeIRE);
				vecPerDayInvRecExternalOther.add(bdOtherIRE);
				vecPerDayInvRecExternalCreditCard.add(bdCreditCardIRE);
				bdTermsIRE = bdInvoiceIRE.subtract(bdCashIRE.add(bdChequeIRE.add(bdOtherIRE.add(bdCreditCardIRE))));
				vecPerDayInvRecExternalTerms.add(bdTermsIRE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyExternalDate.add(tsFrom);
				vecPerDayRecOnlyExternalInvoiceNum.add(new Integer(vecRecOnlyExternal.size()));
				BigDecimal bdInvoiceROE = new BigDecimal("0.00");
				BigDecimal bdCashROE = new BigDecimal("0.00");
				BigDecimal bdChequeROE = new BigDecimal("0.00");
				BigDecimal bdOtherROE = new BigDecimal("0.00");
				BigDecimal bdCreditCardROE = new BigDecimal("0.00");
				BigDecimal bdTermsROE = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyExternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyExternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROE.add(thisInvoiceAmount.abs());
							// bdInvoiceROE =
							// bdInvoiceROE.add(natObj.amount.abs());
							BigDecimal bdInvoiceROEAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROE = bdInvoiceROE.add(bdInvoiceROEAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROEAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROE = bdCashROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROE = bdChequeROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROE = bdOtherROE.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROE = bdCreditCardROE.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyExternalInvoiceSum.add(bdInvoiceROE);
				vecPerDayRecOnlyExternalCash.add(bdCashROE);
				vecPerDayRecOnlyExternalCheque.add(bdChequeROE);
				vecPerDayRecOnlyExternalOther.add(bdOtherROE);
				vecPerDayRecOnlyExternalCreditCard.add(bdCreditCardROE);
				bdTermsROE = new BigDecimal("0.00");
				// bdInvoiceROE.subtract( bdCashROE.add( bdChequeROE.add(
				// bdOtherROE.add(bdCreditCardROE))));
				vecPerDayRecOnlyExternalTerms.add(bdTermsROE);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// ///////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayInvRecInternalDate.add(tsFrom);
				vecPerDayInvRecInternalInvoiceNum.add(new Integer(vecInvRecInternal.size()));
				BigDecimal bdInvoiceIRI = new BigDecimal("0.00");
				BigDecimal bdCashIRI = new BigDecimal("0.00");
				BigDecimal bdChequeIRI = new BigDecimal("0.00");
				BigDecimal bdOtherIRI = new BigDecimal("0.00");
				BigDecimal bdCreditCardIRI = new BigDecimal("0.00");
				BigDecimal bdTermsIRI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecInvRecInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecInvRecInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceIRI.add(thisInvoiceAmount.abs());
							// bdInvoiceIRI =
							// bdInvoiceIRI.add(natObj.amount.abs());
							BigDecimal bdInvoiceIRIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceIRI = bdInvoiceIRI.add(bdInvoiceIRIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceIRIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashIRI = bdCashIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeIRI = bdChequeIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherIRI = bdOtherIRI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardIRI = bdCreditCardIRI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayInvRecInternalInvoiceSum.add(bdInvoiceIRI);
				vecPerDayInvRecInternalCash.add(bdCashIRI);
				vecPerDayInvRecInternalCheque.add(bdChequeIRI);
				vecPerDayInvRecInternalOther.add(bdOtherIRI);
				vecPerDayInvRecInternalCreditCard.add(bdCreditCardIRI);
				bdTermsIRI = bdInvoiceIRI.subtract(bdCashIRI.add(bdChequeIRI.add(bdOtherIRI.add(bdCreditCardIRI))));
				vecPerDayInvRecInternalTerms.add(bdTermsIRI);
				Log.printVerbose(" after calling na nut to retrieve na txns !!");
				// //////////////////////////////////////////////////////////////////////
				// populating rows in the monthly report table
				vecPerDayRecOnlyInternalDate.add(tsFrom);
				vecPerDayRecOnlyInternalInvoiceNum.add(new Integer(vecRecOnlyInternal.size()));
				BigDecimal bdInvoiceROI = new BigDecimal("0.00");
				BigDecimal bdCashROI = new BigDecimal("0.00");
				BigDecimal bdChequeROI = new BigDecimal("0.00");
				BigDecimal bdOtherROI = new BigDecimal("0.00");
				BigDecimal bdCreditCardROI = new BigDecimal("0.00");
				BigDecimal bdTermsROI = new BigDecimal("0.00");
				for (int count5 = 0; count5 < vecRecOnlyInternal.size(); count5++)
				{
					NominalAccountObject naObj = (NominalAccountObject) vecRecOnlyInternal.get(count5);
					for (int count6 = 0; count6 < naObj.vecNominalAccountTxn.size(); count6++)
					{
						NominalAccountTxnObject natObj = (NominalAccountTxnObject) naObj.vecNominalAccountTxn
								.get(count6);
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_INVOICE))
						{
							// BigDecimal thisInvoiceAmount =
							// InvoiceNut.getInvoiceAmount(
							// natObj.foreignKey);
							// bdInvoiceROI.add(thisInvoiceAmount.abs());
							// bdInvoiceROI =
							// bdInvoiceROI.add(natObj.amount.abs());
							BigDecimal bdInvoiceROIAmt = InvoiceNut.getInvoiceAmount(natObj.foreignKey);
							bdInvoiceROI = bdInvoiceROI.add(bdInvoiceROIAmt);
							Log.printDebug("INVOICE (" + natObj.foreignKey.toString() + "AMT = "
									+ bdInvoiceROIAmt.toString());
							Log.printDebug("NAT AMT = " + natObj.amount.toString());
							// Log.printDebug(" INVOICE (" +
							// natObj.pkid.toString() +
							// ") = " + natObj.amount.toString() );
						}
						if (natObj.foreignTable.equals(NominalAccountTxnBean.FT_CUST_RECEIPT))
						{
							Receipt rctEJB = ReceiptNut.getHandle(natObj.foreignKey);
							String paymentMethod = rctEJB.getPaymentMethod();
							// Log.printDebug(" RECEIPT (" +
							// rctObj.pkid.toString() +
							// ")-"+ rctObj.paymentMethod+
							// " = " + natObj.amount.toString()
							// + rctObj.paymentAmount.abs().toString() );
							if (paymentMethod.equals("cash"))
							{
								bdCashROI = bdCashROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("cheque"))
							{
								bdChequeROI = bdChequeROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("other"))
							{
								bdOtherROI = bdOtherROI.add(natObj.amount.abs());
							}
							if (paymentMethod.equals("creditcard"))
							{
								bdCreditCardROI = bdCreditCardROI.add(natObj.amount.abs());
							}
						}
					} // end for count6
				} // end for count5
				vecPerDayRecOnlyInternalInvoiceSum.add(bdInvoiceROI);
				vecPerDayRecOnlyInternalCash.add(bdCashROI);
				vecPerDayRecOnlyInternalCheque.add(bdChequeROI);
				vecPerDayRecOnlyInternalOther.add(bdOtherROI);
				vecPerDayRecOnlyInternalCreditCard.add(bdCreditCardROI);
				bdTermsROI = new BigDecimal("0.00");
				// bdInvoiceROI.subtract( bdCashROI.add( bdChequeROI.add(
				// bdOtherROI.add(bdCreditCardROI))));
				vecPerDayRecOnlyInternalTerms.add(bdTermsROI);
				// /////////////////////////////////////////////////////////////////
			} // end for countDay
			req.setAttribute("vecPerDayRecOnlyExternalDate", vecPerDayRecOnlyExternalDate);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceNum", vecPerDayRecOnlyExternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyExternalInvoiceSum", vecPerDayRecOnlyExternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyExternalCash", vecPerDayRecOnlyExternalCash);
			req.setAttribute("vecPerDayRecOnlyExternalCheque", vecPerDayRecOnlyExternalCheque);
			req.setAttribute("vecPerDayRecOnlyExternalOther", vecPerDayRecOnlyExternalOther);
			req.setAttribute("vecPerDayRecOnlyExternalCreditCard", vecPerDayRecOnlyExternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyExternalTerms", vecPerDayRecOnlyExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecExternalDate", vecPerDayInvRecExternalDate);
			req.setAttribute("vecPerDayInvRecExternalInvoiceNum", vecPerDayInvRecExternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecExternalInvoiceSum", vecPerDayInvRecExternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecExternalCash", vecPerDayInvRecExternalCash);
			req.setAttribute("vecPerDayInvRecExternalCheque", vecPerDayInvRecExternalCheque);
			req.setAttribute("vecPerDayInvRecExternalOther", vecPerDayInvRecExternalOther);
			req.setAttribute("vecPerDayInvRecExternalCreditCard", vecPerDayInvRecExternalCreditCard);
			req.setAttribute("vecPerDayInvRecExternalTerms", vecPerDayInvRecExternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayInvRecInternalDate", vecPerDayInvRecInternalDate);
			req.setAttribute("vecPerDayInvRecInternalInvoiceNum", vecPerDayInvRecInternalInvoiceNum);
			req.setAttribute("vecPerDayInvRecInternalInvoiceSum", vecPerDayInvRecInternalInvoiceSum);
			req.setAttribute("vecPerDayInvRecInternalCash", vecPerDayInvRecInternalCash);
			req.setAttribute("vecPerDayInvRecInternalCheque", vecPerDayInvRecInternalCheque);
			req.setAttribute("vecPerDayInvRecInternalOther", vecPerDayInvRecInternalOther);
			req.setAttribute("vecPerDayInvRecInternalCreditCard", vecPerDayInvRecInternalCreditCard);
			req.setAttribute("vecPerDayInvRecInternalTerms", vecPerDayInvRecInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			req.setAttribute("vecPerDayRecOnlyInternalDate", vecPerDayRecOnlyInternalDate);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceNum", vecPerDayRecOnlyInternalInvoiceNum);
			req.setAttribute("vecPerDayRecOnlyInternalInvoiceSum", vecPerDayRecOnlyInternalInvoiceSum);
			req.setAttribute("vecPerDayRecOnlyInternalCash", vecPerDayRecOnlyInternalCash);
			req.setAttribute("vecPerDayRecOnlyInternalCheque", vecPerDayRecOnlyInternalCheque);
			req.setAttribute("vecPerDayRecOnlyInternalOther", vecPerDayRecOnlyInternalOther);
			req.setAttribute("vecPerDayRecOnlyInternalCreditCard", vecPerDayRecOnlyInternalCreditCard);
			req.setAttribute("vecPerDayRecOnlyInternalTerms", vecPerDayRecOnlyInternalTerms);
			// / next, split this vector into internal/external vector
			// / base on customer type
			Log.printVerbose(" after calling na nut to retrieve na txns !!");
			// ////////////////////////////////////////////////////////////////////
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "mgt_report: monthly-sales-report";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}// end of DoMgtMonthlySalesReportType01
