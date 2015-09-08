/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalTransactionEdit implements Action
{
	private String strClassName = "DoJournalTransactionEdit: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			if (fnPopulateGeneralLedger(servlet, req, res))
			{
				return new ActionRouter("acc-add-journaltxn-page");
			} else
			{
				return new ActionRouter("acc-add-pccenter-journaltxn-page");
			}
		} else if (formName.equals("selectPCCenterAndBatch"))
		{
			// fnPopulatePCCenter(servlet, req, res);
			// fnPopulateBatch(servlet, req, res);
			return new ActionRouter("acc-add-pccenter-journaltxn-page");
		} else if (formName.equals("addJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnAddJournalTransaction(servlet, req, res);
		} else if (formName.equals("editJournalTransaction"))
		{
			fnPopulateGeneralLedger(servlet, req, res);
			fnPopulateJournalTransaction(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("printableEditJournalTransaction"))
		{
			fnPopulateJournalTransaction(servlet, req, res);
			req.setAttribute("pcCenter", trim(req.getParameter("pcCenter")));
			req.setAttribute("batch", trim(req.getParameter("batch")));
			return new ActionRouter("acc-edit-journaltxn-printable-page");
		} else if (formName.equals("updateJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnUpdateJournalTransaction(servlet, req, res);
			fnPopulateJournalTransaction2(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("postJournalTransactions"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnPostJournalTransactions(servlet, req, res);
		}
		/*
		 * else if (formName.equals("viewJournalTransaction")) {
		 * fnPopulatePCCenter(servlet, req, res); fnPopulateBatch(servlet, req,
		 * res); fnPopulatePeriod(servlet, req, res); } else if
		 * (formName.equals("printableViewJournalTransaction")) {
		 * fnGetJournalTransactionList(servlet, req, res); return new
		 * ActionRouter("acc-journaltxn-listing-printable-page"); }
		 * 
		 * fnGetJournalTransactionList(servlet, req, res);
		 */
		return new ActionRouter("acc-journaltxn-listing-page");
	}

	protected boolean fnPopulateGeneralLedger(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGeneralLedger()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		boolean flag = false;
		if ((pcCenter != null && !pcCenter.equals("")) && (batch != null && !batch.equals("")))
		{
			// Vector vecGL = GeneralLedgerNut.getValueObjectsGiven(
			// "pccenterid", pcCenter, "batchid", batch);
			// req.setAttribute("vecGL", vecGL);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("batch", batch);
			flag = true;
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
		return flag;
	}

	protected void fnPopulatePCCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePCCenter()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecPCCenter = ProfitCostCenterNut.getAllValueObjects();
		req.setAttribute("vecPCCenter", vecPCCenter);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateBatch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateBatch()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecBatches = BatchNut.getAllValueObjects();
		req.setAttribute("vecBatches", vecBatches);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulatePeriod(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePeriod(()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFrom = new Vector();
		Vector vecTo = new Vector();
		Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		for (int i = 0; i < vecFY.size(); i++)
		{
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
			Integer year = fyo.fiscalYear;
			Calendar cal = Calendar.getInstance();
			cal.setLenient(true);
			for (int j = 0; j < 12; j++)
			{
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, 1);
				Timestamp ts = new Timestamp(cal.getTimeInMillis());
				vecFrom.add(ts);
				int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, maxDay);
				Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
				vecTo.add(ts2);
			}
		}
		req.setAttribute("vecFrom", vecFrom);
		req.setAttribute("vecTo", vecTo);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("referenceNo"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("pkid"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetJournalTransactionList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetJournalTransactionList()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String typeID = trim(req.getParameter("typeID"));
		String batch = trim(req.getParameter("batch"));
		String from = trim(req.getParameter("from"));
		String to = trim(req.getParameter("to"));
		if (!pcCenter.equals("") && !batch.equals(""))
		{
			if (typeID.equals(""))
			{
				typeID = "0";
			}
			Timestamp tsTo = parseToDate(to);
			Timestamp tsFrom = parseFromDate(from, tsTo);
			Timestamp tsToNextDay = TimeFormat.createTimeStampNextDay(tsTo.toString());
			Vector vecJT = JournalTransactionNut.getValueObjectsGivenDate("pccenterid", pcCenter, "typeid", typeID,
					"batchid", batch, tsFrom, tsToNextDay);
			req.setAttribute("vecJT", vecJT);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("typeID", typeID);
			req.setAttribute("batch", batch);
			req.setAttribute("tsFrom", tsFrom);
			req.setAttribute("tsTo", tsTo);
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPostJournalTransactions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPostJournalTransactions()";
		Log.printVerbose("In " + strClassName + funcName);
		String[] pkId = req.getParameterValues("pkid");
		if (pkId != null)
		{
			for (int i = 0; i < pkId.length; i++)
			{
				try
				{
					JournalTransaction jt = JournalTransactionNut.getHandle(new Long(pkId[i]));
					jt.setTypeId(new Integer(1));
					JournalTransactionObject jto = jt.getValueObject();
					Timestamp tsTransactionDate = jto.transactionDate;
					Vector vecJE = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkId[i], null, null);
					for (int j = 0; j < vecJE.size(); j++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(j);
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						if (real_temp.equals("temp"))
						{
							Collection colRE = null;
							Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
							for (int k = 0; k < vecFY.size(); k++)
							{
								FiscalYearObject fyo = (FiscalYearObject) vecFY.get(k);
								Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-"
										+ fyo.beginMonth + "-01");
								Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear
										.intValue() + 1)
										+ "-" + fyo.beginMonth + "-01");
								if (tsTransactionDate.compareTo(beginDate) >= 0
										&& tsTransactionDate.compareTo(endDate) < 0)
								{
									colRE = RetainedEarningsNut.getCollectionByField("pccenterid", jto.pcCenterId
											.toString(), "batchid", jto.batchId.toString(), "fiscalyearid", fyo.pkId
											.toString());
									break;
								}
							}
							Iterator itrRE = colRE.iterator();
							while (itrRE.hasNext())
							{
								RetainedEarnings re = (RetainedEarnings) itrRE.next();
								RetainedEarningsObject reo = re.getValueObject();
								String ledgerSide = glcao.ledgerSide;
								if (ledgerSide.equals("cr"))
								{
									reo.retainedEarnings = reo.retainedEarnings.add(jeo.amount.negate());
								} else
								{
									reo.retainedEarnings = reo.retainedEarnings.subtract(jeo.amount);
								}
								re.setValueObject(reo);
								break;
							}
						}
					}
				} catch (Exception e)
				{
					Log.printDebug("Post Journal Transactions Failed" + e.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		isEditTransaction(req, res, false);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String action = req.getParameter("action");
		String referenceNo = req.getParameter("referenceNo");
		JournalTransaction jt = null;
		JournalTransactionObject jto = null;
		try
		{
			jt = JournalTransactionNut.getHandle(new Long(referenceNo));
			jto = jt.getValueObject();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (action.equals("Save Changes") && jto.typeId.intValue() == 0)
		{
			isEditTransaction(req, res, true);
		} else if (action.equals("Delete Transaction") && jto.typeId.intValue() == 0)
		{
			try
			{
				Timestamp tsTransactionDate = jto.transactionDate;
				Collection col = JournalEntryNut.getObjectsGivenTxnId(new Long(referenceNo));
				Iterator journalEntries = col.iterator();
				while (journalEntries.hasNext())
				{
					JournalEntry je = (JournalEntry) journalEntries.next();
					JournalEntryObject jeo = je.getValueObject();
					BigDecimal debitCreditAmount = jeo.amount;
					GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					String real_temp = glcao.realTemp;
					je.remove();
					/*
					 * if (real_temp.equals("temp")) { Collection colRE = null;
					 * Vector vecFY =
					 * FiscalYearNut.getValueObjectsGiven("status", "active",
					 * null, null); for (int i = 0; i < vecFY.size(); i++) {
					 * FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
					 * Timestamp beginDate =
					 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
					 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
					 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
					 * 1) + "-" + fyo.beginMonth + "-01"); if
					 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
					 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
					 * RetainedEarningsNut.getCollectionByField("pccenterid",
					 * jto.pcCenterId.toString(), "batchid",
					 * jto.batchId.toString(), "fiscalyearid",
					 * fyo.pkId.toString()); break; } } Iterator itrRE =
					 * colRE.iterator(); while (itrRE.hasNext()) {
					 * RetainedEarnings re = (RetainedEarnings) itrRE.next();
					 * RetainedEarningsObject reo = re.getValueObject(); String
					 * ledgerSide = glcao.ledgerSide; if
					 * (ledgerSide.equals("cr")) { reo.retainedEarnings =
					 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
					 * else { reo.retainedEarnings =
					 * reo.retainedEarnings.add(debitCreditAmount); }
					 * re.setValueObject(reo); break; } }
					 */
				}
				jt.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove Journal Transaction/Entries Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private void isEditTransaction(HttpServletRequest req, HttpServletResponse res, boolean edit)
	{
		Vector vecGLCode = new Vector();
		Vector vecDescription = new Vector();
		Vector vecDebit = new Vector();
		Vector vecCredit = new Vector();
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		if (pcCenter.equals("") || batch.equals(""))
		{
			return;
		}
		String transactionDate = trim(req.getParameter("transactionDate"));
		if (!FiscalYearNut.transactionDateIsValid(transactionDate))
		{
			return;
		}
		for (int i = 0; i < 10; i++)
		{
			String str = Integer.toString(i);
			String glCode = trim(req.getParameter("glCode" + str));
			String description = trim(req.getParameter("description" + str));
			String debit = trim(req.getParameter("debit" + str));
			String credit = trim(req.getParameter("credit" + str));
			if (!glCode.equals("") && (!debit.equals("") || !credit.equals("")))
			{
				vecGLCode.add(glCode);
				vecDescription.add(description);
				vecDebit.add(debit);
				vecCredit.add(credit);
			}
		}
		if (!vecGLCode.isEmpty() && doubleEntryIsBalanced(vecDebit, vecCredit))
		{
			HttpSession session = req.getSession();
			User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lusr != null)
			{
				Log.printVerbose("Adding new JournalTransaction");
				java.util.Date ldt = new java.util.Date();
				Timestamp tsCreate = new Timestamp(ldt.getTime());
				Integer usrid = null;
				try
				{
					usrid = lusr.getUserId();
				} catch (Exception ex)
				{
					Log.printAudit("User does not exist: " + ex.getMessage());
				}
				BigDecimal totalAmount = new BigDecimal("0");
				for (int i = 0; i < vecDebit.size(); i++)
				{
					String strDebit = (String) vecDebit.get(i);
					if (strDebit.equals(""))
						strDebit = "0";
					BigDecimal debit = new BigDecimal(strDebit);
					if (debit.signum() == 1)
					{
						totalAmount = totalAmount.add(debit);
					}
				}
				for (int i = 0; i < vecCredit.size(); i++)
				{
					String strCredit = (String) vecCredit.get(i);
					if (strCredit.equals(""))
						strCredit = "0";
					BigDecimal credit = new BigDecimal(strCredit);
					if (credit.signum() == -1)
					{
						totalAmount = totalAmount.add(credit.negate());
					}
				}
				Long jtPkId = null;
				Timestamp tsTransactionDate = TimeFormat.createTimeStamp(transactionDate);
				JournalTransactionObject jto = null;
				try
				{
					if (edit)
					{
						String jtxnDescription = req.getParameter("jtxnDescription");
						if (jtxnDescription == null)
						{
							jtxnDescription = "";
						}
						jtPkId = new Long(req.getParameter("referenceNo"));
						JournalTransaction jt = JournalTransactionNut.getHandle(jtPkId);
						jto = jt.getValueObject();
						Timestamp originalDate = jto.transactionDate;
						jto.description = jtxnDescription;
						jto.amount = totalAmount;
						jto.transactionDate = tsTransactionDate;
						jto.userIdEdit = usrid;
						jto.timestampEdit = tsCreate;
						jt.setValueObject(jto);
						String[] jePkId = req.getParameterValues("pkid");
						for (int i = 0; i < jePkId.length; i++)
						{
							JournalEntry je = JournalEntryNut.getHandle(new Long(jePkId[i]));
							JournalEntryObject jeo = je.getValueObject();
							BigDecimal debitCreditAmount = jeo.amount;
							GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
							GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
							GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
							String real_temp = glcao.realTemp;
							je.remove();
							/*
							 * if (real_temp.equals("temp")) { Collection colRE =
							 * null; Vector vecFY =
							 * FiscalYearNut.getValueObjectsGiven("status",
							 * "active", null, null); for (int j = 0; j <
							 * vecFY.size(); j++) { FiscalYearObject fyo =
							 * (FiscalYearObject) vecFY.get(j); Timestamp
							 * beginDate =
							 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
							 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
							 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
							 * 1) + "-" + fyo.beginMonth + "-01"); if
							 * (originalDate.compareTo(beginDate) >= 0 &&
							 * originalDate.compareTo(endDate) < 0) { colRE =
							 * RetainedEarningsNut.getCollectionByField("pccenterid",
							 * jto.pcCenterId.toString(), "batchid",
							 * jto.batchId.toString(), "fiscalyearid",
							 * fyo.pkId.toString()); break; } } Iterator itrRE =
							 * colRE.iterator(); while (itrRE.hasNext()) {
							 * RetainedEarnings re = (RetainedEarnings)
							 * itrRE.next(); RetainedEarningsObject reo =
							 * re.getValueObject(); String ledgerSide =
							 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
							 * reo.retainedEarnings =
							 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
							 * else { reo.retainedEarnings =
							 * reo.retainedEarnings.add(debitCreditAmount); }
							 * re.setValueObject(reo); break; } }
							 */
						}
					} else
					{
						JournalTransactionHome jtHome = JournalTransactionNut.getHome();
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						JournalTransaction jt = jtHome.create("manual", new Integer(pcCenter), new Integer(batch),
								new Integer(0), null, (String) vecDescription.get(0), totalAmount, tsTransactionDate,
								"", new Long(0), usrid, tsCreate);
						jto = jt.getValueObject();
						jtPkId = jto.pkId;
					}
					for (int i = 0; i < vecGLCode.size(); i++)
					{
						String strDebit = (String) vecDebit.get(i);
						String strCredit = (String) vecCredit.get(i);
						if (strDebit.equals(""))
							strDebit = "0";
						if (strCredit.equals(""))
							strCredit = "0";
						BigDecimal debit = new BigDecimal(strDebit);
						BigDecimal credit = new BigDecimal(strCredit);
						BigDecimal debitCreditAmount = null;
						BigDecimal transactionAmount = null;
						if (debit.signum() == 0)
						{
							debitCreditAmount = new BigDecimal(strCredit);
							debitCreditAmount = debitCreditAmount.negate();
						} else
						{
							debitCreditAmount = new BigDecimal(strDebit);
						}
						debitCreditAmount = debitCreditAmount.setScale(2);
						Integer gl = new Integer((String) vecGLCode.get(i));
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(gl);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						jeHome.create(jtPkId, gl, (String) vecDescription.get(i), "MYR", debitCreditAmount, "USDMYR",
								new BigDecimal("3.8000"));
						/*
						 * if (real_temp.equals("temp")) { Collection colRE =
						 * null; Vector vecFY =
						 * FiscalYearNut.getValueObjectsGiven("status",
						 * "active", null, null); for (int k = 0; k <
						 * vecFY.size(); k++) { FiscalYearObject fyo =
						 * (FiscalYearObject) vecFY.get(k); Timestamp beginDate =
						 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
						 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
						 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
						 * 1) + "-" + fyo.beginMonth + "-01"); if
						 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
						 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
						 * RetainedEarningsNut.getCollectionByField("pccenterid",
						 * jto.pcCenterId.toString(), "batchid",
						 * jto.batchId.toString(), "fiscalyearid",
						 * fyo.pkId.toString()); break; } } Iterator itrRE =
						 * colRE.iterator(); while (itrRE.hasNext()) {
						 * RetainedEarnings re = (RetainedEarnings)
						 * itrRE.next(); RetainedEarningsObject reo =
						 * re.getValueObject(); String ledgerSide =
						 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
						 * reo.retainedEarnings =
						 * reo.retainedEarnings.add(debitCreditAmount.negate()); }
						 * else { reo.retainedEarnings =
						 * reo.retainedEarnings.subtract(debitCreditAmount); }
						 * re.setValueObject(reo); break; } }
						 */
					}
				} catch (Exception ex)
				{
					Log.printDebug("Cannot create JournalTransaction " + ex.getMessage());
				}
			}
		} else
		{
		}
	}

	private boolean doubleEntryIsBalanced(Vector vecDebit, Vector vecCredit)
	{
		BigDecimal totalDebit = new BigDecimal("0");
		BigDecimal totalCredit = new BigDecimal("0");
		for (int i = 0; i < vecDebit.size(); i++)
		{
			try
			{
				String strDebit = (String) vecDebit.get(i);
				String strCredit = (String) vecCredit.get(i);
				if (strDebit.equals(""))
					strDebit = "0";
				if (strCredit.equals(""))
					strCredit = "0";
				BigDecimal debit = new BigDecimal(strDebit);
				BigDecimal credit = new BigDecimal(strCredit);
				if (debit.signum() != 0 && credit.signum() != 0)
					return false;
				if (debit.signum() == 0 && credit.signum() == 0)
					return false;
				totalDebit = totalDebit.add(debit);
				totalCredit = totalCredit.add(credit);
			} catch (Exception e)
			{
				return false;
			}
		}
		totalDebit = totalDebit.setScale(2);
		totalCredit = totalCredit.setScale(2);
		if (totalDebit.compareTo(totalCredit) == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private Timestamp parseToDate(String to)
	{
		Timestamp tsTo = null;
		try
		{
			tsTo = Timestamp.valueOf(to + " 00:00:00.000000000");
		} catch (Exception e)
		{
			Calendar cal = Calendar.getInstance();
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxDay, 0, 0, 0);
			cal.set(cal.MILLISECOND, 0);
			tsTo = new Timestamp(cal.getTimeInMillis());
		}
		return tsTo;
	}

	private Timestamp parseFromDate(String from, Timestamp tsTo)
	{
		Timestamp tsFrom = null;
		try
		{
			tsFrom = Timestamp.valueOf(from + " 00:00:00.000000000");
			if (tsFrom.before(tsTo) == false)
			{
				throw new Exception("fromDate after toDate");
			}
		} catch (Exception e)
		{
			Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(0);
			Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
					+ fyo.beginMonth + "-01");
			if (tsTo.compareTo(beginDate) >= 0 && tsTo.compareTo(endDate) < 0)
			{
				tsFrom = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			} else
			{
				tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
						+ fyo.beginMonth + "-01");
			}
		}
		return tsFrom;
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalTransactionEdit implements Action
{
	private String strClassName = "DoJournalTransactionEdit: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			if (fnPopulateGeneralLedger(servlet, req, res))
			{
				return new ActionRouter("acc-add-journaltxn-page");
			} else
			{
				return new ActionRouter("acc-add-pccenter-journaltxn-page");
			}
		} else if (formName.equals("selectPCCenterAndBatch"))
		{
			// fnPopulatePCCenter(servlet, req, res);
			// fnPopulateBatch(servlet, req, res);
			return new ActionRouter("acc-add-pccenter-journaltxn-page");
		} else if (formName.equals("addJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnAddJournalTransaction(servlet, req, res);
		} else if (formName.equals("editJournalTransaction"))
		{
			fnPopulateGeneralLedger(servlet, req, res);
			fnPopulateJournalTransaction(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("printableEditJournalTransaction"))
		{
			fnPopulateJournalTransaction(servlet, req, res);
			req.setAttribute("pcCenter", trim(req.getParameter("pcCenter")));
			req.setAttribute("batch", trim(req.getParameter("batch")));
			return new ActionRouter("acc-edit-journaltxn-printable-page");
		} else if (formName.equals("updateJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnUpdateJournalTransaction(servlet, req, res);
			fnPopulateJournalTransaction2(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("postJournalTransactions"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnPostJournalTransactions(servlet, req, res);
		}
		/*
		 * else if (formName.equals("viewJournalTransaction")) {
		 * fnPopulatePCCenter(servlet, req, res); fnPopulateBatch(servlet, req,
		 * res); fnPopulatePeriod(servlet, req, res); } else if
		 * (formName.equals("printableViewJournalTransaction")) {
		 * fnGetJournalTransactionList(servlet, req, res); return new
		 * ActionRouter("acc-journaltxn-listing-printable-page"); }
		 * 
		 * fnGetJournalTransactionList(servlet, req, res);
		 */
		return new ActionRouter("acc-journaltxn-listing-page");
	}

	protected boolean fnPopulateGeneralLedger(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGeneralLedger()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		boolean flag = false;
		if ((pcCenter != null && !pcCenter.equals("")) && (batch != null && !batch.equals("")))
		{
			// Vector vecGL = GeneralLedgerNut.getValueObjectsGiven(
			// "pccenterid", pcCenter, "batchid", batch);
			// req.setAttribute("vecGL", vecGL);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("batch", batch);
			flag = true;
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
		return flag;
	}

	protected void fnPopulatePCCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePCCenter()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecPCCenter = ProfitCostCenterNut.getAllValueObjects();
		req.setAttribute("vecPCCenter", vecPCCenter);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateBatch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateBatch()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecBatches = BatchNut.getAllValueObjects();
		req.setAttribute("vecBatches", vecBatches);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulatePeriod(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePeriod(()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFrom = new Vector();
		Vector vecTo = new Vector();
		Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		for (int i = 0; i < vecFY.size(); i++)
		{
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
			Integer year = fyo.fiscalYear;
			Calendar cal = Calendar.getInstance();
			cal.setLenient(true);
			for (int j = 0; j < 12; j++)
			{
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, 1);
				Timestamp ts = new Timestamp(cal.getTimeInMillis());
				vecFrom.add(ts);
				int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, maxDay);
				Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
				vecTo.add(ts2);
			}
		}
		req.setAttribute("vecFrom", vecFrom);
		req.setAttribute("vecTo", vecTo);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("referenceNo"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("pkid"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetJournalTransactionList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetJournalTransactionList()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String typeID = trim(req.getParameter("typeID"));
		String batch = trim(req.getParameter("batch"));
		String from = trim(req.getParameter("from"));
		String to = trim(req.getParameter("to"));
		if (!pcCenter.equals("") && !batch.equals(""))
		{
			if (typeID.equals(""))
			{
				typeID = "0";
			}
			Timestamp tsTo = parseToDate(to);
			Timestamp tsFrom = parseFromDate(from, tsTo);
			Timestamp tsToNextDay = TimeFormat.createTimeStampNextDay(tsTo.toString());
			Vector vecJT = JournalTransactionNut.getValueObjectsGivenDate("pccenterid", pcCenter, "typeid", typeID,
					"batchid", batch, tsFrom, tsToNextDay);
			req.setAttribute("vecJT", vecJT);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("typeID", typeID);
			req.setAttribute("batch", batch);
			req.setAttribute("tsFrom", tsFrom);
			req.setAttribute("tsTo", tsTo);
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPostJournalTransactions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPostJournalTransactions()";
		Log.printVerbose("In " + strClassName + funcName);
		String[] pkId = req.getParameterValues("pkid");
		if (pkId != null)
		{
			for (int i = 0; i < pkId.length; i++)
			{
				try
				{
					JournalTransaction jt = JournalTransactionNut.getHandle(new Long(pkId[i]));
					jt.setTypeId(new Integer(1));
					JournalTransactionObject jto = jt.getValueObject();
					Timestamp tsTransactionDate = jto.transactionDate;
					Vector vecJE = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkId[i], null, null);
					for (int j = 0; j < vecJE.size(); j++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(j);
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						if (real_temp.equals("temp"))
						{
							Collection colRE = null;
							Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
							for (int k = 0; k < vecFY.size(); k++)
							{
								FiscalYearObject fyo = (FiscalYearObject) vecFY.get(k);
								Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-"
										+ fyo.beginMonth + "-01");
								Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear
										.intValue() + 1)
										+ "-" + fyo.beginMonth + "-01");
								if (tsTransactionDate.compareTo(beginDate) >= 0
										&& tsTransactionDate.compareTo(endDate) < 0)
								{
									colRE = RetainedEarningsNut.getCollectionByField("pccenterid", jto.pcCenterId
											.toString(), "batchid", jto.batchId.toString(), "fiscalyearid", fyo.pkId
											.toString());
									break;
								}
							}
							Iterator itrRE = colRE.iterator();
							while (itrRE.hasNext())
							{
								RetainedEarnings re = (RetainedEarnings) itrRE.next();
								RetainedEarningsObject reo = re.getValueObject();
								String ledgerSide = glcao.ledgerSide;
								if (ledgerSide.equals("cr"))
								{
									reo.retainedEarnings = reo.retainedEarnings.add(jeo.amount.negate());
								} else
								{
									reo.retainedEarnings = reo.retainedEarnings.subtract(jeo.amount);
								}
								re.setValueObject(reo);
								break;
							}
						}
					}
				} catch (Exception e)
				{
					Log.printDebug("Post Journal Transactions Failed" + e.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		isEditTransaction(req, res, false);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String action = req.getParameter("action");
		String referenceNo = req.getParameter("referenceNo");
		JournalTransaction jt = null;
		JournalTransactionObject jto = null;
		try
		{
			jt = JournalTransactionNut.getHandle(new Long(referenceNo));
			jto = jt.getValueObject();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (action.equals("Save Changes") && jto.typeId.intValue() == 0)
		{
			isEditTransaction(req, res, true);
		} else if (action.equals("Delete Transaction") && jto.typeId.intValue() == 0)
		{
			try
			{
				Timestamp tsTransactionDate = jto.transactionDate;
				Collection col = JournalEntryNut.getObjectsGivenTxnId(new Long(referenceNo));
				Iterator journalEntries = col.iterator();
				while (journalEntries.hasNext())
				{
					JournalEntry je = (JournalEntry) journalEntries.next();
					JournalEntryObject jeo = je.getValueObject();
					BigDecimal debitCreditAmount = jeo.amount;
					GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					String real_temp = glcao.realTemp;
					je.remove();
					/*
					 * if (real_temp.equals("temp")) { Collection colRE = null;
					 * Vector vecFY =
					 * FiscalYearNut.getValueObjectsGiven("status", "active",
					 * null, null); for (int i = 0; i < vecFY.size(); i++) {
					 * FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
					 * Timestamp beginDate =
					 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
					 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
					 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
					 * 1) + "-" + fyo.beginMonth + "-01"); if
					 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
					 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
					 * RetainedEarningsNut.getCollectionByField("pccenterid",
					 * jto.pcCenterId.toString(), "batchid",
					 * jto.batchId.toString(), "fiscalyearid",
					 * fyo.pkId.toString()); break; } } Iterator itrRE =
					 * colRE.iterator(); while (itrRE.hasNext()) {
					 * RetainedEarnings re = (RetainedEarnings) itrRE.next();
					 * RetainedEarningsObject reo = re.getValueObject(); String
					 * ledgerSide = glcao.ledgerSide; if
					 * (ledgerSide.equals("cr")) { reo.retainedEarnings =
					 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
					 * else { reo.retainedEarnings =
					 * reo.retainedEarnings.add(debitCreditAmount); }
					 * re.setValueObject(reo); break; } }
					 */
				}
				jt.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove Journal Transaction/Entries Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private void isEditTransaction(HttpServletRequest req, HttpServletResponse res, boolean edit)
	{
		Vector vecGLCode = new Vector();
		Vector vecDescription = new Vector();
		Vector vecDebit = new Vector();
		Vector vecCredit = new Vector();
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		if (pcCenter.equals("") || batch.equals(""))
		{
			return;
		}
		String transactionDate = trim(req.getParameter("transactionDate"));
		if (!FiscalYearNut.transactionDateIsValid(transactionDate))
		{
			return;
		}
		for (int i = 0; i < 10; i++)
		{
			String str = Integer.toString(i);
			String glCode = trim(req.getParameter("glCode" + str));
			String description = trim(req.getParameter("description" + str));
			String debit = trim(req.getParameter("debit" + str));
			String credit = trim(req.getParameter("credit" + str));
			if (!glCode.equals("") && (!debit.equals("") || !credit.equals("")))
			{
				vecGLCode.add(glCode);
				vecDescription.add(description);
				vecDebit.add(debit);
				vecCredit.add(credit);
			}
		}
		if (!vecGLCode.isEmpty() && doubleEntryIsBalanced(vecDebit, vecCredit))
		{
			HttpSession session = req.getSession();
			User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lusr != null)
			{
				Log.printVerbose("Adding new JournalTransaction");
				java.util.Date ldt = new java.util.Date();
				Timestamp tsCreate = new Timestamp(ldt.getTime());
				Integer usrid = null;
				try
				{
					usrid = lusr.getUserId();
				} catch (Exception ex)
				{
					Log.printAudit("User does not exist: " + ex.getMessage());
				}
				BigDecimal totalAmount = new BigDecimal("0");
				for (int i = 0; i < vecDebit.size(); i++)
				{
					String strDebit = (String) vecDebit.get(i);
					if (strDebit.equals(""))
						strDebit = "0";
					BigDecimal debit = new BigDecimal(strDebit);
					if (debit.signum() == 1)
					{
						totalAmount = totalAmount.add(debit);
					}
				}
				for (int i = 0; i < vecCredit.size(); i++)
				{
					String strCredit = (String) vecCredit.get(i);
					if (strCredit.equals(""))
						strCredit = "0";
					BigDecimal credit = new BigDecimal(strCredit);
					if (credit.signum() == -1)
					{
						totalAmount = totalAmount.add(credit.negate());
					}
				}
				Long jtPkId = null;
				Timestamp tsTransactionDate = TimeFormat.createTimeStamp(transactionDate);
				JournalTransactionObject jto = null;
				try
				{
					if (edit)
					{
						String jtxnDescription = req.getParameter("jtxnDescription");
						if (jtxnDescription == null)
						{
							jtxnDescription = "";
						}
						jtPkId = new Long(req.getParameter("referenceNo"));
						JournalTransaction jt = JournalTransactionNut.getHandle(jtPkId);
						jto = jt.getValueObject();
						Timestamp originalDate = jto.transactionDate;
						jto.description = jtxnDescription;
						jto.amount = totalAmount;
						jto.transactionDate = tsTransactionDate;
						jto.userIdEdit = usrid;
						jto.timestampEdit = tsCreate;
						jt.setValueObject(jto);
						String[] jePkId = req.getParameterValues("pkid");
						for (int i = 0; i < jePkId.length; i++)
						{
							JournalEntry je = JournalEntryNut.getHandle(new Long(jePkId[i]));
							JournalEntryObject jeo = je.getValueObject();
							BigDecimal debitCreditAmount = jeo.amount;
							GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
							GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
							GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
							String real_temp = glcao.realTemp;
							je.remove();
							/*
							 * if (real_temp.equals("temp")) { Collection colRE =
							 * null; Vector vecFY =
							 * FiscalYearNut.getValueObjectsGiven("status",
							 * "active", null, null); for (int j = 0; j <
							 * vecFY.size(); j++) { FiscalYearObject fyo =
							 * (FiscalYearObject) vecFY.get(j); Timestamp
							 * beginDate =
							 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
							 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
							 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
							 * 1) + "-" + fyo.beginMonth + "-01"); if
							 * (originalDate.compareTo(beginDate) >= 0 &&
							 * originalDate.compareTo(endDate) < 0) { colRE =
							 * RetainedEarningsNut.getCollectionByField("pccenterid",
							 * jto.pcCenterId.toString(), "batchid",
							 * jto.batchId.toString(), "fiscalyearid",
							 * fyo.pkId.toString()); break; } } Iterator itrRE =
							 * colRE.iterator(); while (itrRE.hasNext()) {
							 * RetainedEarnings re = (RetainedEarnings)
							 * itrRE.next(); RetainedEarningsObject reo =
							 * re.getValueObject(); String ledgerSide =
							 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
							 * reo.retainedEarnings =
							 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
							 * else { reo.retainedEarnings =
							 * reo.retainedEarnings.add(debitCreditAmount); }
							 * re.setValueObject(reo); break; } }
							 */
						}
					} else
					{
						JournalTransactionHome jtHome = JournalTransactionNut.getHome();
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						JournalTransaction jt = jtHome.create("manual", new Integer(pcCenter), new Integer(batch),
								new Integer(0), null, (String) vecDescription.get(0), totalAmount, tsTransactionDate,
								"", new Long(0), usrid, tsCreate);
						jto = jt.getValueObject();
						jtPkId = jto.pkId;
					}
					for (int i = 0; i < vecGLCode.size(); i++)
					{
						String strDebit = (String) vecDebit.get(i);
						String strCredit = (String) vecCredit.get(i);
						if (strDebit.equals(""))
							strDebit = "0";
						if (strCredit.equals(""))
							strCredit = "0";
						BigDecimal debit = new BigDecimal(strDebit);
						BigDecimal credit = new BigDecimal(strCredit);
						BigDecimal debitCreditAmount = null;
						BigDecimal transactionAmount = null;
						if (debit.signum() == 0)
						{
							debitCreditAmount = new BigDecimal(strCredit);
							debitCreditAmount = debitCreditAmount.negate();
						} else
						{
							debitCreditAmount = new BigDecimal(strDebit);
						}
						debitCreditAmount = debitCreditAmount.setScale(2);
						Integer gl = new Integer((String) vecGLCode.get(i));
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(gl);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						jeHome.create(jtPkId, gl, (String) vecDescription.get(i), "MYR", debitCreditAmount, "USDMYR",
								new BigDecimal("3.8000"));
						/*
						 * if (real_temp.equals("temp")) { Collection colRE =
						 * null; Vector vecFY =
						 * FiscalYearNut.getValueObjectsGiven("status",
						 * "active", null, null); for (int k = 0; k <
						 * vecFY.size(); k++) { FiscalYearObject fyo =
						 * (FiscalYearObject) vecFY.get(k); Timestamp beginDate =
						 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
						 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
						 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
						 * 1) + "-" + fyo.beginMonth + "-01"); if
						 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
						 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
						 * RetainedEarningsNut.getCollectionByField("pccenterid",
						 * jto.pcCenterId.toString(), "batchid",
						 * jto.batchId.toString(), "fiscalyearid",
						 * fyo.pkId.toString()); break; } } Iterator itrRE =
						 * colRE.iterator(); while (itrRE.hasNext()) {
						 * RetainedEarnings re = (RetainedEarnings)
						 * itrRE.next(); RetainedEarningsObject reo =
						 * re.getValueObject(); String ledgerSide =
						 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
						 * reo.retainedEarnings =
						 * reo.retainedEarnings.add(debitCreditAmount.negate()); }
						 * else { reo.retainedEarnings =
						 * reo.retainedEarnings.subtract(debitCreditAmount); }
						 * re.setValueObject(reo); break; } }
						 */
					}
				} catch (Exception ex)
				{
					Log.printDebug("Cannot create JournalTransaction " + ex.getMessage());
				}
			}
		} else
		{
		}
	}

	private boolean doubleEntryIsBalanced(Vector vecDebit, Vector vecCredit)
	{
		BigDecimal totalDebit = new BigDecimal("0");
		BigDecimal totalCredit = new BigDecimal("0");
		for (int i = 0; i < vecDebit.size(); i++)
		{
			try
			{
				String strDebit = (String) vecDebit.get(i);
				String strCredit = (String) vecCredit.get(i);
				if (strDebit.equals(""))
					strDebit = "0";
				if (strCredit.equals(""))
					strCredit = "0";
				BigDecimal debit = new BigDecimal(strDebit);
				BigDecimal credit = new BigDecimal(strCredit);
				if (debit.signum() != 0 && credit.signum() != 0)
					return false;
				if (debit.signum() == 0 && credit.signum() == 0)
					return false;
				totalDebit = totalDebit.add(debit);
				totalCredit = totalCredit.add(credit);
			} catch (Exception e)
			{
				return false;
			}
		}
		totalDebit = totalDebit.setScale(2);
		totalCredit = totalCredit.setScale(2);
		if (totalDebit.compareTo(totalCredit) == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private Timestamp parseToDate(String to)
	{
		Timestamp tsTo = null;
		try
		{
			tsTo = Timestamp.valueOf(to + " 00:00:00.000000000");
		} catch (Exception e)
		{
			Calendar cal = Calendar.getInstance();
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxDay, 0, 0, 0);
			cal.set(cal.MILLISECOND, 0);
			tsTo = new Timestamp(cal.getTimeInMillis());
		}
		return tsTo;
	}

	private Timestamp parseFromDate(String from, Timestamp tsTo)
	{
		Timestamp tsFrom = null;
		try
		{
			tsFrom = Timestamp.valueOf(from + " 00:00:00.000000000");
			if (tsFrom.before(tsTo) == false)
			{
				throw new Exception("fromDate after toDate");
			}
		} catch (Exception e)
		{
			Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(0);
			Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
					+ fyo.beginMonth + "-01");
			if (tsTo.compareTo(beginDate) >= 0 && tsTo.compareTo(endDate) < 0)
			{
				tsFrom = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			} else
			{
				tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
						+ fyo.beginMonth + "-01");
			}
		}
		return tsFrom;
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalTransactionEdit implements Action
{
	private String strClassName = "DoJournalTransactionEdit: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			if (fnPopulateGeneralLedger(servlet, req, res))
			{
				return new ActionRouter("acc-add-journaltxn-page");
			} else
			{
				return new ActionRouter("acc-add-pccenter-journaltxn-page");
			}
		} else if (formName.equals("selectPCCenterAndBatch"))
		{
			// fnPopulatePCCenter(servlet, req, res);
			// fnPopulateBatch(servlet, req, res);
			return new ActionRouter("acc-add-pccenter-journaltxn-page");
		} else if (formName.equals("addJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnAddJournalTransaction(servlet, req, res);
		} else if (formName.equals("editJournalTransaction"))
		{
			fnPopulateGeneralLedger(servlet, req, res);
			fnPopulateJournalTransaction(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("printableEditJournalTransaction"))
		{
			fnPopulateJournalTransaction(servlet, req, res);
			req.setAttribute("pcCenter", trim(req.getParameter("pcCenter")));
			req.setAttribute("batch", trim(req.getParameter("batch")));
			return new ActionRouter("acc-edit-journaltxn-printable-page");
		} else if (formName.equals("updateJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnUpdateJournalTransaction(servlet, req, res);
			fnPopulateJournalTransaction2(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("postJournalTransactions"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnPostJournalTransactions(servlet, req, res);
		}
		/*
		 * else if (formName.equals("viewJournalTransaction")) {
		 * fnPopulatePCCenter(servlet, req, res); fnPopulateBatch(servlet, req,
		 * res); fnPopulatePeriod(servlet, req, res); } else if
		 * (formName.equals("printableViewJournalTransaction")) {
		 * fnGetJournalTransactionList(servlet, req, res); return new
		 * ActionRouter("acc-journaltxn-listing-printable-page"); }
		 * 
		 * fnGetJournalTransactionList(servlet, req, res);
		 */
		return new ActionRouter("acc-journaltxn-listing-page");
	}

	protected boolean fnPopulateGeneralLedger(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGeneralLedger()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		boolean flag = false;
		if ((pcCenter != null && !pcCenter.equals("")) && (batch != null && !batch.equals("")))
		{
			// Vector vecGL = GeneralLedgerNut.getValueObjectsGiven(
			// "pccenterid", pcCenter, "batchid", batch);
			// req.setAttribute("vecGL", vecGL);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("batch", batch);
			flag = true;
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
		return flag;
	}

	protected void fnPopulatePCCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePCCenter()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecPCCenter = ProfitCostCenterNut.getAllValueObjects();
		req.setAttribute("vecPCCenter", vecPCCenter);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateBatch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateBatch()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecBatches = BatchNut.getAllValueObjects();
		req.setAttribute("vecBatches", vecBatches);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulatePeriod(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePeriod(()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFrom = new Vector();
		Vector vecTo = new Vector();
		Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		for (int i = 0; i < vecFY.size(); i++)
		{
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
			Integer year = fyo.fiscalYear;
			Calendar cal = Calendar.getInstance();
			cal.setLenient(true);
			for (int j = 0; j < 12; j++)
			{
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, 1);
				Timestamp ts = new Timestamp(cal.getTimeInMillis());
				vecFrom.add(ts);
				int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, maxDay);
				Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
				vecTo.add(ts2);
			}
		}
		req.setAttribute("vecFrom", vecFrom);
		req.setAttribute("vecTo", vecTo);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("referenceNo"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("pkid"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetJournalTransactionList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetJournalTransactionList()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String typeID = trim(req.getParameter("typeID"));
		String batch = trim(req.getParameter("batch"));
		String from = trim(req.getParameter("from"));
		String to = trim(req.getParameter("to"));
		if (!pcCenter.equals("") && !batch.equals(""))
		{
			if (typeID.equals(""))
			{
				typeID = "0";
			}
			Timestamp tsTo = parseToDate(to);
			Timestamp tsFrom = parseFromDate(from, tsTo);
			Timestamp tsToNextDay = TimeFormat.createTimeStampNextDay(tsTo.toString());
			Vector vecJT = JournalTransactionNut.getValueObjectsGivenDate("pccenterid", pcCenter, "typeid", typeID,
					"batchid", batch, tsFrom, tsToNextDay);
			req.setAttribute("vecJT", vecJT);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("typeID", typeID);
			req.setAttribute("batch", batch);
			req.setAttribute("tsFrom", tsFrom);
			req.setAttribute("tsTo", tsTo);
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPostJournalTransactions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPostJournalTransactions()";
		Log.printVerbose("In " + strClassName + funcName);
		String[] pkId = req.getParameterValues("pkid");
		if (pkId != null)
		{
			for (int i = 0; i < pkId.length; i++)
			{
				try
				{
					JournalTransaction jt = JournalTransactionNut.getHandle(new Long(pkId[i]));
					jt.setTypeId(new Integer(1));
					JournalTransactionObject jto = jt.getValueObject();
					Timestamp tsTransactionDate = jto.transactionDate;
					Vector vecJE = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkId[i], null, null);
					for (int j = 0; j < vecJE.size(); j++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(j);
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						if (real_temp.equals("temp"))
						{
							Collection colRE = null;
							Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
							for (int k = 0; k < vecFY.size(); k++)
							{
								FiscalYearObject fyo = (FiscalYearObject) vecFY.get(k);
								Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-"
										+ fyo.beginMonth + "-01");
								Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear
										.intValue() + 1)
										+ "-" + fyo.beginMonth + "-01");
								if (tsTransactionDate.compareTo(beginDate) >= 0
										&& tsTransactionDate.compareTo(endDate) < 0)
								{
									colRE = RetainedEarningsNut.getCollectionByField("pccenterid", jto.pcCenterId
											.toString(), "batchid", jto.batchId.toString(), "fiscalyearid", fyo.pkId
											.toString());
									break;
								}
							}
							Iterator itrRE = colRE.iterator();
							while (itrRE.hasNext())
							{
								RetainedEarnings re = (RetainedEarnings) itrRE.next();
								RetainedEarningsObject reo = re.getValueObject();
								String ledgerSide = glcao.ledgerSide;
								if (ledgerSide.equals("cr"))
								{
									reo.retainedEarnings = reo.retainedEarnings.add(jeo.amount.negate());
								} else
								{
									reo.retainedEarnings = reo.retainedEarnings.subtract(jeo.amount);
								}
								re.setValueObject(reo);
								break;
							}
						}
					}
				} catch (Exception e)
				{
					Log.printDebug("Post Journal Transactions Failed" + e.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		isEditTransaction(req, res, false);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String action = req.getParameter("action");
		String referenceNo = req.getParameter("referenceNo");
		JournalTransaction jt = null;
		JournalTransactionObject jto = null;
		try
		{
			jt = JournalTransactionNut.getHandle(new Long(referenceNo));
			jto = jt.getValueObject();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (action.equals("Save Changes") && jto.typeId.intValue() == 0)
		{
			isEditTransaction(req, res, true);
		} else if (action.equals("Delete Transaction") && jto.typeId.intValue() == 0)
		{
			try
			{
				Timestamp tsTransactionDate = jto.transactionDate;
				Collection col = JournalEntryNut.getObjectsGivenTxnId(new Long(referenceNo));
				Iterator journalEntries = col.iterator();
				while (journalEntries.hasNext())
				{
					JournalEntry je = (JournalEntry) journalEntries.next();
					JournalEntryObject jeo = je.getValueObject();
					BigDecimal debitCreditAmount = jeo.amount;
					GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					String real_temp = glcao.realTemp;
					je.remove();
					/*
					 * if (real_temp.equals("temp")) { Collection colRE = null;
					 * Vector vecFY =
					 * FiscalYearNut.getValueObjectsGiven("status", "active",
					 * null, null); for (int i = 0; i < vecFY.size(); i++) {
					 * FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
					 * Timestamp beginDate =
					 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
					 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
					 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
					 * 1) + "-" + fyo.beginMonth + "-01"); if
					 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
					 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
					 * RetainedEarningsNut.getCollectionByField("pccenterid",
					 * jto.pcCenterId.toString(), "batchid",
					 * jto.batchId.toString(), "fiscalyearid",
					 * fyo.pkId.toString()); break; } } Iterator itrRE =
					 * colRE.iterator(); while (itrRE.hasNext()) {
					 * RetainedEarnings re = (RetainedEarnings) itrRE.next();
					 * RetainedEarningsObject reo = re.getValueObject(); String
					 * ledgerSide = glcao.ledgerSide; if
					 * (ledgerSide.equals("cr")) { reo.retainedEarnings =
					 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
					 * else { reo.retainedEarnings =
					 * reo.retainedEarnings.add(debitCreditAmount); }
					 * re.setValueObject(reo); break; } }
					 */
				}
				jt.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove Journal Transaction/Entries Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private void isEditTransaction(HttpServletRequest req, HttpServletResponse res, boolean edit)
	{
		Vector vecGLCode = new Vector();
		Vector vecDescription = new Vector();
		Vector vecDebit = new Vector();
		Vector vecCredit = new Vector();
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		if (pcCenter.equals("") || batch.equals(""))
		{
			return;
		}
		String transactionDate = trim(req.getParameter("transactionDate"));
		if (!FiscalYearNut.transactionDateIsValid(transactionDate))
		{
			return;
		}
		for (int i = 0; i < 10; i++)
		{
			String str = Integer.toString(i);
			String glCode = trim(req.getParameter("glCode" + str));
			String description = trim(req.getParameter("description" + str));
			String debit = trim(req.getParameter("debit" + str));
			String credit = trim(req.getParameter("credit" + str));
			if (!glCode.equals("") && (!debit.equals("") || !credit.equals("")))
			{
				vecGLCode.add(glCode);
				vecDescription.add(description);
				vecDebit.add(debit);
				vecCredit.add(credit);
			}
		}
		if (!vecGLCode.isEmpty() && doubleEntryIsBalanced(vecDebit, vecCredit))
		{
			HttpSession session = req.getSession();
			User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lusr != null)
			{
				Log.printVerbose("Adding new JournalTransaction");
				java.util.Date ldt = new java.util.Date();
				Timestamp tsCreate = new Timestamp(ldt.getTime());
				Integer usrid = null;
				try
				{
					usrid = lusr.getUserId();
				} catch (Exception ex)
				{
					Log.printAudit("User does not exist: " + ex.getMessage());
				}
				BigDecimal totalAmount = new BigDecimal("0");
				for (int i = 0; i < vecDebit.size(); i++)
				{
					String strDebit = (String) vecDebit.get(i);
					if (strDebit.equals(""))
						strDebit = "0";
					BigDecimal debit = new BigDecimal(strDebit);
					if (debit.signum() == 1)
					{
						totalAmount = totalAmount.add(debit);
					}
				}
				for (int i = 0; i < vecCredit.size(); i++)
				{
					String strCredit = (String) vecCredit.get(i);
					if (strCredit.equals(""))
						strCredit = "0";
					BigDecimal credit = new BigDecimal(strCredit);
					if (credit.signum() == -1)
					{
						totalAmount = totalAmount.add(credit.negate());
					}
				}
				Long jtPkId = null;
				Timestamp tsTransactionDate = TimeFormat.createTimeStamp(transactionDate);
				JournalTransactionObject jto = null;
				try
				{
					if (edit)
					{
						String jtxnDescription = req.getParameter("jtxnDescription");
						if (jtxnDescription == null)
						{
							jtxnDescription = "";
						}
						jtPkId = new Long(req.getParameter("referenceNo"));
						JournalTransaction jt = JournalTransactionNut.getHandle(jtPkId);
						jto = jt.getValueObject();
						Timestamp originalDate = jto.transactionDate;
						jto.description = jtxnDescription;
						jto.amount = totalAmount;
						jto.transactionDate = tsTransactionDate;
						jto.userIdEdit = usrid;
						jto.timestampEdit = tsCreate;
						jt.setValueObject(jto);
						String[] jePkId = req.getParameterValues("pkid");
						for (int i = 0; i < jePkId.length; i++)
						{
							JournalEntry je = JournalEntryNut.getHandle(new Long(jePkId[i]));
							JournalEntryObject jeo = je.getValueObject();
							BigDecimal debitCreditAmount = jeo.amount;
							GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
							GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
							GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
							String real_temp = glcao.realTemp;
							je.remove();
							/*
							 * if (real_temp.equals("temp")) { Collection colRE =
							 * null; Vector vecFY =
							 * FiscalYearNut.getValueObjectsGiven("status",
							 * "active", null, null); for (int j = 0; j <
							 * vecFY.size(); j++) { FiscalYearObject fyo =
							 * (FiscalYearObject) vecFY.get(j); Timestamp
							 * beginDate =
							 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
							 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
							 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
							 * 1) + "-" + fyo.beginMonth + "-01"); if
							 * (originalDate.compareTo(beginDate) >= 0 &&
							 * originalDate.compareTo(endDate) < 0) { colRE =
							 * RetainedEarningsNut.getCollectionByField("pccenterid",
							 * jto.pcCenterId.toString(), "batchid",
							 * jto.batchId.toString(), "fiscalyearid",
							 * fyo.pkId.toString()); break; } } Iterator itrRE =
							 * colRE.iterator(); while (itrRE.hasNext()) {
							 * RetainedEarnings re = (RetainedEarnings)
							 * itrRE.next(); RetainedEarningsObject reo =
							 * re.getValueObject(); String ledgerSide =
							 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
							 * reo.retainedEarnings =
							 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
							 * else { reo.retainedEarnings =
							 * reo.retainedEarnings.add(debitCreditAmount); }
							 * re.setValueObject(reo); break; } }
							 */
						}
					} else
					{
						JournalTransactionHome jtHome = JournalTransactionNut.getHome();
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						JournalTransaction jt = jtHome.create("manual", new Integer(pcCenter), new Integer(batch),
								new Integer(0), null, (String) vecDescription.get(0), totalAmount, tsTransactionDate,
								"", new Long(0), usrid, tsCreate);
						jto = jt.getValueObject();
						jtPkId = jto.pkId;
					}
					for (int i = 0; i < vecGLCode.size(); i++)
					{
						String strDebit = (String) vecDebit.get(i);
						String strCredit = (String) vecCredit.get(i);
						if (strDebit.equals(""))
							strDebit = "0";
						if (strCredit.equals(""))
							strCredit = "0";
						BigDecimal debit = new BigDecimal(strDebit);
						BigDecimal credit = new BigDecimal(strCredit);
						BigDecimal debitCreditAmount = null;
						BigDecimal transactionAmount = null;
						if (debit.signum() == 0)
						{
							debitCreditAmount = new BigDecimal(strCredit);
							debitCreditAmount = debitCreditAmount.negate();
						} else
						{
							debitCreditAmount = new BigDecimal(strDebit);
						}
						debitCreditAmount = debitCreditAmount.setScale(2);
						Integer gl = new Integer((String) vecGLCode.get(i));
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(gl);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						jeHome.create(jtPkId, gl, (String) vecDescription.get(i), "MYR", debitCreditAmount, "USDMYR",
								new BigDecimal("3.8000"));
						/*
						 * if (real_temp.equals("temp")) { Collection colRE =
						 * null; Vector vecFY =
						 * FiscalYearNut.getValueObjectsGiven("status",
						 * "active", null, null); for (int k = 0; k <
						 * vecFY.size(); k++) { FiscalYearObject fyo =
						 * (FiscalYearObject) vecFY.get(k); Timestamp beginDate =
						 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
						 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
						 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
						 * 1) + "-" + fyo.beginMonth + "-01"); if
						 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
						 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
						 * RetainedEarningsNut.getCollectionByField("pccenterid",
						 * jto.pcCenterId.toString(), "batchid",
						 * jto.batchId.toString(), "fiscalyearid",
						 * fyo.pkId.toString()); break; } } Iterator itrRE =
						 * colRE.iterator(); while (itrRE.hasNext()) {
						 * RetainedEarnings re = (RetainedEarnings)
						 * itrRE.next(); RetainedEarningsObject reo =
						 * re.getValueObject(); String ledgerSide =
						 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
						 * reo.retainedEarnings =
						 * reo.retainedEarnings.add(debitCreditAmount.negate()); }
						 * else { reo.retainedEarnings =
						 * reo.retainedEarnings.subtract(debitCreditAmount); }
						 * re.setValueObject(reo); break; } }
						 */
					}
				} catch (Exception ex)
				{
					Log.printDebug("Cannot create JournalTransaction " + ex.getMessage());
				}
			}
		} else
		{
		}
	}

	private boolean doubleEntryIsBalanced(Vector vecDebit, Vector vecCredit)
	{
		BigDecimal totalDebit = new BigDecimal("0");
		BigDecimal totalCredit = new BigDecimal("0");
		for (int i = 0; i < vecDebit.size(); i++)
		{
			try
			{
				String strDebit = (String) vecDebit.get(i);
				String strCredit = (String) vecCredit.get(i);
				if (strDebit.equals(""))
					strDebit = "0";
				if (strCredit.equals(""))
					strCredit = "0";
				BigDecimal debit = new BigDecimal(strDebit);
				BigDecimal credit = new BigDecimal(strCredit);
				if (debit.signum() != 0 && credit.signum() != 0)
					return false;
				if (debit.signum() == 0 && credit.signum() == 0)
					return false;
				totalDebit = totalDebit.add(debit);
				totalCredit = totalCredit.add(credit);
			} catch (Exception e)
			{
				return false;
			}
		}
		totalDebit = totalDebit.setScale(2);
		totalCredit = totalCredit.setScale(2);
		if (totalDebit.compareTo(totalCredit) == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private Timestamp parseToDate(String to)
	{
		Timestamp tsTo = null;
		try
		{
			tsTo = Timestamp.valueOf(to + " 00:00:00.000000000");
		} catch (Exception e)
		{
			Calendar cal = Calendar.getInstance();
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxDay, 0, 0, 0);
			cal.set(cal.MILLISECOND, 0);
			tsTo = new Timestamp(cal.getTimeInMillis());
		}
		return tsTo;
	}

	private Timestamp parseFromDate(String from, Timestamp tsTo)
	{
		Timestamp tsFrom = null;
		try
		{
			tsFrom = Timestamp.valueOf(from + " 00:00:00.000000000");
			if (tsFrom.before(tsTo) == false)
			{
				throw new Exception("fromDate after toDate");
			}
		} catch (Exception e)
		{
			Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(0);
			Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
					+ fyo.beginMonth + "-01");
			if (tsTo.compareTo(beginDate) >= 0 && tsTo.compareTo(endDate) < 0)
			{
				tsFrom = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			} else
			{
				tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
						+ fyo.beginMonth + "-01");
			}
		}
		return tsFrom;
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoJournalTransactionEdit implements Action
{
	private String strClassName = "DoJournalTransactionEdit: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			if (fnPopulateGeneralLedger(servlet, req, res))
			{
				return new ActionRouter("acc-add-journaltxn-page");
			} else
			{
				return new ActionRouter("acc-add-pccenter-journaltxn-page");
			}
		} else if (formName.equals("selectPCCenterAndBatch"))
		{
			// fnPopulatePCCenter(servlet, req, res);
			// fnPopulateBatch(servlet, req, res);
			return new ActionRouter("acc-add-pccenter-journaltxn-page");
		} else if (formName.equals("addJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnAddJournalTransaction(servlet, req, res);
		} else if (formName.equals("editJournalTransaction"))
		{
			fnPopulateGeneralLedger(servlet, req, res);
			fnPopulateJournalTransaction(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("printableEditJournalTransaction"))
		{
			fnPopulateJournalTransaction(servlet, req, res);
			req.setAttribute("pcCenter", trim(req.getParameter("pcCenter")));
			req.setAttribute("batch", trim(req.getParameter("batch")));
			return new ActionRouter("acc-edit-journaltxn-printable-page");
		} else if (formName.equals("updateJournalTransaction"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnUpdateJournalTransaction(servlet, req, res);
			fnPopulateJournalTransaction2(servlet, req, res);
			return new ActionRouter("acc-edit-journaltxn-page");
		} else if (formName.equals("postJournalTransactions"))
		{
			fnPopulatePCCenter(servlet, req, res);
			fnPopulateBatch(servlet, req, res);
			fnPopulatePeriod(servlet, req, res);
			fnPostJournalTransactions(servlet, req, res);
		}
		/*
		 * else if (formName.equals("viewJournalTransaction")) {
		 * fnPopulatePCCenter(servlet, req, res); fnPopulateBatch(servlet, req,
		 * res); fnPopulatePeriod(servlet, req, res); } else if
		 * (formName.equals("printableViewJournalTransaction")) {
		 * fnGetJournalTransactionList(servlet, req, res); return new
		 * ActionRouter("acc-journaltxn-listing-printable-page"); }
		 * 
		 * fnGetJournalTransactionList(servlet, req, res);
		 */
		return new ActionRouter("acc-journaltxn-listing-page");
	}

	protected boolean fnPopulateGeneralLedger(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGeneralLedger()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		boolean flag = false;
		if ((pcCenter != null && !pcCenter.equals("")) && (batch != null && !batch.equals("")))
		{
			// Vector vecGL = GeneralLedgerNut.getValueObjectsGiven(
			// "pccenterid", pcCenter, "batchid", batch);
			// req.setAttribute("vecGL", vecGL);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("batch", batch);
			flag = true;
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
		return flag;
	}

	protected void fnPopulatePCCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePCCenter()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecPCCenter = ProfitCostCenterNut.getAllValueObjects();
		req.setAttribute("vecPCCenter", vecPCCenter);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateBatch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateBatch()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecBatches = BatchNut.getAllValueObjects();
		req.setAttribute("vecBatches", vecBatches);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulatePeriod(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulatePeriod(()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFrom = new Vector();
		Vector vecTo = new Vector();
		Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		for (int i = 0; i < vecFY.size(); i++)
		{
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
			Integer year = fyo.fiscalYear;
			Calendar cal = Calendar.getInstance();
			cal.setLenient(true);
			for (int j = 0; j < 12; j++)
			{
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, 1);
				Timestamp ts = new Timestamp(cal.getTimeInMillis());
				vecFrom.add(ts);
				int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.set(year.intValue(), Integer.parseInt(fyo.beginMonth) - 1 + j, maxDay);
				Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
				vecTo.add(ts2);
			}
		}
		req.setAttribute("vecFrom", vecFrom);
		req.setAttribute("vecTo", vecTo);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("referenceNo"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String pkid = trim(req.getParameter("pkid"));
		JournalTransactionObject jto = JournalTransactionNut.getObject(new Long(pkid));
		Vector vecJEO = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkid, null, null);
		req.setAttribute("jto", jto);
		req.setAttribute("vecJEO", vecJEO);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetJournalTransactionList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetJournalTransactionList()";
		Log.printVerbose("In " + strClassName + funcName);
		String pcCenter = trim(req.getParameter("pcCenter"));
		String typeID = trim(req.getParameter("typeID"));
		String batch = trim(req.getParameter("batch"));
		String from = trim(req.getParameter("from"));
		String to = trim(req.getParameter("to"));
		if (!pcCenter.equals("") && !batch.equals(""))
		{
			if (typeID.equals(""))
			{
				typeID = "0";
			}
			Timestamp tsTo = parseToDate(to);
			Timestamp tsFrom = parseFromDate(from, tsTo);
			Timestamp tsToNextDay = TimeFormat.createTimeStampNextDay(tsTo.toString());
			Vector vecJT = JournalTransactionNut.getValueObjectsGivenDate("pccenterid", pcCenter, "typeid", typeID,
					"batchid", batch, tsFrom, tsToNextDay);
			req.setAttribute("vecJT", vecJT);
			req.setAttribute("pcCenter", pcCenter);
			req.setAttribute("typeID", typeID);
			req.setAttribute("batch", batch);
			req.setAttribute("tsFrom", tsFrom);
			req.setAttribute("tsTo", tsTo);
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPostJournalTransactions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPostJournalTransactions()";
		Log.printVerbose("In " + strClassName + funcName);
		String[] pkId = req.getParameterValues("pkid");
		if (pkId != null)
		{
			for (int i = 0; i < pkId.length; i++)
			{
				try
				{
					JournalTransaction jt = JournalTransactionNut.getHandle(new Long(pkId[i]));
					jt.setTypeId(new Integer(1));
					JournalTransactionObject jto = jt.getValueObject();
					Timestamp tsTransactionDate = jto.transactionDate;
					Vector vecJE = JournalEntryNut.getValueObjectsGiven("journaltxnid", pkId[i], null, null);
					for (int j = 0; j < vecJE.size(); j++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(j);
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						if (real_temp.equals("temp"))
						{
							Collection colRE = null;
							Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
							for (int k = 0; k < vecFY.size(); k++)
							{
								FiscalYearObject fyo = (FiscalYearObject) vecFY.get(k);
								Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-"
										+ fyo.beginMonth + "-01");
								Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear
										.intValue() + 1)
										+ "-" + fyo.beginMonth + "-01");
								if (tsTransactionDate.compareTo(beginDate) >= 0
										&& tsTransactionDate.compareTo(endDate) < 0)
								{
									colRE = RetainedEarningsNut.getCollectionByField("pccenterid", jto.pcCenterId
											.toString(), "batchid", jto.batchId.toString(), "fiscalyearid", fyo.pkId
											.toString());
									break;
								}
							}
							Iterator itrRE = colRE.iterator();
							while (itrRE.hasNext())
							{
								RetainedEarnings re = (RetainedEarnings) itrRE.next();
								RetainedEarningsObject reo = re.getValueObject();
								String ledgerSide = glcao.ledgerSide;
								if (ledgerSide.equals("cr"))
								{
									reo.retainedEarnings = reo.retainedEarnings.add(jeo.amount.negate());
								} else
								{
									reo.retainedEarnings = reo.retainedEarnings.subtract(jeo.amount);
								}
								re.setValueObject(reo);
								break;
							}
						}
					}
				} catch (Exception e)
				{
					Log.printDebug("Post Journal Transactions Failed" + e.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		isEditTransaction(req, res, false);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateJournalTransaction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateJournalTransaction()";
		Log.printVerbose("In " + strClassName + funcName);
		String action = req.getParameter("action");
		String referenceNo = req.getParameter("referenceNo");
		JournalTransaction jt = null;
		JournalTransactionObject jto = null;
		try
		{
			jt = JournalTransactionNut.getHandle(new Long(referenceNo));
			jto = jt.getValueObject();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (action.equals("Save Changes") && jto.typeId.intValue() == 0)
		{
			isEditTransaction(req, res, true);
		} else if (action.equals("Delete Transaction") && jto.typeId.intValue() == 0)
		{
			try
			{
				Timestamp tsTransactionDate = jto.transactionDate;
				Collection col = JournalEntryNut.getObjectsGivenTxnId(new Long(referenceNo));
				Iterator journalEntries = col.iterator();
				while (journalEntries.hasNext())
				{
					JournalEntry je = (JournalEntry) journalEntries.next();
					JournalEntryObject jeo = je.getValueObject();
					BigDecimal debitCreditAmount = jeo.amount;
					GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					String real_temp = glcao.realTemp;
					je.remove();
					/*
					 * if (real_temp.equals("temp")) { Collection colRE = null;
					 * Vector vecFY =
					 * FiscalYearNut.getValueObjectsGiven("status", "active",
					 * null, null); for (int i = 0; i < vecFY.size(); i++) {
					 * FiscalYearObject fyo = (FiscalYearObject) vecFY.get(i);
					 * Timestamp beginDate =
					 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
					 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
					 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
					 * 1) + "-" + fyo.beginMonth + "-01"); if
					 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
					 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
					 * RetainedEarningsNut.getCollectionByField("pccenterid",
					 * jto.pcCenterId.toString(), "batchid",
					 * jto.batchId.toString(), "fiscalyearid",
					 * fyo.pkId.toString()); break; } } Iterator itrRE =
					 * colRE.iterator(); while (itrRE.hasNext()) {
					 * RetainedEarnings re = (RetainedEarnings) itrRE.next();
					 * RetainedEarningsObject reo = re.getValueObject(); String
					 * ledgerSide = glcao.ledgerSide; if
					 * (ledgerSide.equals("cr")) { reo.retainedEarnings =
					 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
					 * else { reo.retainedEarnings =
					 * reo.retainedEarnings.add(debitCreditAmount); }
					 * re.setValueObject(reo); break; } }
					 */
				}
				jt.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove Journal Transaction/Entries Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private void isEditTransaction(HttpServletRequest req, HttpServletResponse res, boolean edit)
	{
		Vector vecGLCode = new Vector();
		Vector vecDescription = new Vector();
		Vector vecDebit = new Vector();
		Vector vecCredit = new Vector();
		String pcCenter = trim(req.getParameter("pcCenter"));
		String batch = trim(req.getParameter("batch"));
		if (pcCenter.equals("") || batch.equals(""))
		{
			return;
		}
		String transactionDate = trim(req.getParameter("transactionDate"));
		if (!FiscalYearNut.transactionDateIsValid(transactionDate))
		{
			return;
		}
		for (int i = 0; i < 10; i++)
		{
			String str = Integer.toString(i);
			String glCode = trim(req.getParameter("glCode" + str));
			String description = trim(req.getParameter("description" + str));
			String debit = trim(req.getParameter("debit" + str));
			String credit = trim(req.getParameter("credit" + str));
			if (!glCode.equals("") && (!debit.equals("") || !credit.equals("")))
			{
				vecGLCode.add(glCode);
				vecDescription.add(description);
				vecDebit.add(debit);
				vecCredit.add(credit);
			}
		}
		if (!vecGLCode.isEmpty() && doubleEntryIsBalanced(vecDebit, vecCredit))
		{
			HttpSession session = req.getSession();
			User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lusr != null)
			{
				Log.printVerbose("Adding new JournalTransaction");
				java.util.Date ldt = new java.util.Date();
				Timestamp tsCreate = new Timestamp(ldt.getTime());
				Integer usrid = null;
				try
				{
					usrid = lusr.getUserId();
				} catch (Exception ex)
				{
					Log.printAudit("User does not exist: " + ex.getMessage());
				}
				BigDecimal totalAmount = new BigDecimal("0");
				for (int i = 0; i < vecDebit.size(); i++)
				{
					String strDebit = (String) vecDebit.get(i);
					if (strDebit.equals(""))
						strDebit = "0";
					BigDecimal debit = new BigDecimal(strDebit);
					if (debit.signum() == 1)
					{
						totalAmount = totalAmount.add(debit);
					}
				}
				for (int i = 0; i < vecCredit.size(); i++)
				{
					String strCredit = (String) vecCredit.get(i);
					if (strCredit.equals(""))
						strCredit = "0";
					BigDecimal credit = new BigDecimal(strCredit);
					if (credit.signum() == -1)
					{
						totalAmount = totalAmount.add(credit.negate());
					}
				}
				Long jtPkId = null;
				Timestamp tsTransactionDate = TimeFormat.createTimeStamp(transactionDate);
				JournalTransactionObject jto = null;
				try
				{
					if (edit)
					{
						String jtxnDescription = req.getParameter("jtxnDescription");
						if (jtxnDescription == null)
						{
							jtxnDescription = "";
						}
						jtPkId = new Long(req.getParameter("referenceNo"));
						JournalTransaction jt = JournalTransactionNut.getHandle(jtPkId);
						jto = jt.getValueObject();
						Timestamp originalDate = jto.transactionDate;
						jto.description = jtxnDescription;
						jto.amount = totalAmount;
						jto.transactionDate = tsTransactionDate;
						jto.userIdEdit = usrid;
						jto.timestampEdit = tsCreate;
						jt.setValueObject(jto);
						String[] jePkId = req.getParameterValues("pkid");
						for (int i = 0; i < jePkId.length; i++)
						{
							JournalEntry je = JournalEntryNut.getHandle(new Long(jePkId[i]));
							JournalEntryObject jeo = je.getValueObject();
							BigDecimal debitCreditAmount = jeo.amount;
							GeneralLedgerObject glo = GeneralLedgerNut.getObject(jeo.glId);
							GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
							GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
							String real_temp = glcao.realTemp;
							je.remove();
							/*
							 * if (real_temp.equals("temp")) { Collection colRE =
							 * null; Vector vecFY =
							 * FiscalYearNut.getValueObjectsGiven("status",
							 * "active", null, null); for (int j = 0; j <
							 * vecFY.size(); j++) { FiscalYearObject fyo =
							 * (FiscalYearObject) vecFY.get(j); Timestamp
							 * beginDate =
							 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
							 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
							 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
							 * 1) + "-" + fyo.beginMonth + "-01"); if
							 * (originalDate.compareTo(beginDate) >= 0 &&
							 * originalDate.compareTo(endDate) < 0) { colRE =
							 * RetainedEarningsNut.getCollectionByField("pccenterid",
							 * jto.pcCenterId.toString(), "batchid",
							 * jto.batchId.toString(), "fiscalyearid",
							 * fyo.pkId.toString()); break; } } Iterator itrRE =
							 * colRE.iterator(); while (itrRE.hasNext()) {
							 * RetainedEarnings re = (RetainedEarnings)
							 * itrRE.next(); RetainedEarningsObject reo =
							 * re.getValueObject(); String ledgerSide =
							 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
							 * reo.retainedEarnings =
							 * reo.retainedEarnings.subtract(debitCreditAmount.negate()); }
							 * else { reo.retainedEarnings =
							 * reo.retainedEarnings.add(debitCreditAmount); }
							 * re.setValueObject(reo); break; } }
							 */
						}
					} else
					{
						JournalTransactionHome jtHome = JournalTransactionNut.getHome();
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						JournalTransaction jt = jtHome.create("manual", new Integer(pcCenter), new Integer(batch),
								new Integer(0), null, (String) vecDescription.get(0), totalAmount, tsTransactionDate,
								"", new Long(0), usrid, tsCreate);
						jto = jt.getValueObject();
						jtPkId = jto.pkId;
					}
					for (int i = 0; i < vecGLCode.size(); i++)
					{
						String strDebit = (String) vecDebit.get(i);
						String strCredit = (String) vecCredit.get(i);
						if (strDebit.equals(""))
							strDebit = "0";
						if (strCredit.equals(""))
							strCredit = "0";
						BigDecimal debit = new BigDecimal(strDebit);
						BigDecimal credit = new BigDecimal(strCredit);
						BigDecimal debitCreditAmount = null;
						BigDecimal transactionAmount = null;
						if (debit.signum() == 0)
						{
							debitCreditAmount = new BigDecimal(strCredit);
							debitCreditAmount = debitCreditAmount.negate();
						} else
						{
							debitCreditAmount = new BigDecimal(strDebit);
						}
						debitCreditAmount = debitCreditAmount.setScale(2);
						Integer gl = new Integer((String) vecGLCode.get(i));
						GeneralLedgerObject glo = GeneralLedgerNut.getObject(gl);
						GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
						GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
						String real_temp = glcao.realTemp;
						JournalEntryHome jeHome = JournalEntryNut.getHome();
						jeHome.create(jtPkId, gl, (String) vecDescription.get(i), "MYR", debitCreditAmount, "USDMYR",
								new BigDecimal("3.8000"));
						/*
						 * if (real_temp.equals("temp")) { Collection colRE =
						 * null; Vector vecFY =
						 * FiscalYearNut.getValueObjectsGiven("status",
						 * "active", null, null); for (int k = 0; k <
						 * vecFY.size(); k++) { FiscalYearObject fyo =
						 * (FiscalYearObject) vecFY.get(k); Timestamp beginDate =
						 * TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() +
						 * "-" + fyo.beginMonth + "-01"); Timestamp endDate =
						 * TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() +
						 * 1) + "-" + fyo.beginMonth + "-01"); if
						 * (tsTransactionDate.compareTo(beginDate) >= 0 &&
						 * tsTransactionDate.compareTo(endDate) < 0) { colRE =
						 * RetainedEarningsNut.getCollectionByField("pccenterid",
						 * jto.pcCenterId.toString(), "batchid",
						 * jto.batchId.toString(), "fiscalyearid",
						 * fyo.pkId.toString()); break; } } Iterator itrRE =
						 * colRE.iterator(); while (itrRE.hasNext()) {
						 * RetainedEarnings re = (RetainedEarnings)
						 * itrRE.next(); RetainedEarningsObject reo =
						 * re.getValueObject(); String ledgerSide =
						 * glcao.ledgerSide; if (ledgerSide.equals("cr")) {
						 * reo.retainedEarnings =
						 * reo.retainedEarnings.add(debitCreditAmount.negate()); }
						 * else { reo.retainedEarnings =
						 * reo.retainedEarnings.subtract(debitCreditAmount); }
						 * re.setValueObject(reo); break; } }
						 */
					}
				} catch (Exception ex)
				{
					Log.printDebug("Cannot create JournalTransaction " + ex.getMessage());
				}
			}
		} else
		{
		}
	}

	private boolean doubleEntryIsBalanced(Vector vecDebit, Vector vecCredit)
	{
		BigDecimal totalDebit = new BigDecimal("0");
		BigDecimal totalCredit = new BigDecimal("0");
		for (int i = 0; i < vecDebit.size(); i++)
		{
			try
			{
				String strDebit = (String) vecDebit.get(i);
				String strCredit = (String) vecCredit.get(i);
				if (strDebit.equals(""))
					strDebit = "0";
				if (strCredit.equals(""))
					strCredit = "0";
				BigDecimal debit = new BigDecimal(strDebit);
				BigDecimal credit = new BigDecimal(strCredit);
				if (debit.signum() != 0 && credit.signum() != 0)
					return false;
				if (debit.signum() == 0 && credit.signum() == 0)
					return false;
				totalDebit = totalDebit.add(debit);
				totalCredit = totalCredit.add(credit);
			} catch (Exception e)
			{
				return false;
			}
		}
		totalDebit = totalDebit.setScale(2);
		totalCredit = totalCredit.setScale(2);
		if (totalDebit.compareTo(totalCredit) == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private Timestamp parseToDate(String to)
	{
		Timestamp tsTo = null;
		try
		{
			tsTo = Timestamp.valueOf(to + " 00:00:00.000000000");
		} catch (Exception e)
		{
			Calendar cal = Calendar.getInstance();
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxDay, 0, 0, 0);
			cal.set(cal.MILLISECOND, 0);
			tsTo = new Timestamp(cal.getTimeInMillis());
		}
		return tsTo;
	}

	private Timestamp parseFromDate(String from, Timestamp tsTo)
	{
		Timestamp tsFrom = null;
		try
		{
			tsFrom = Timestamp.valueOf(from + " 00:00:00.000000000");
			if (tsFrom.before(tsTo) == false)
			{
				throw new Exception("fromDate after toDate");
			}
		} catch (Exception e)
		{
			Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
			FiscalYearObject fyo = (FiscalYearObject) vecFY.get(0);
			Timestamp beginDate = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			Timestamp endDate = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
					+ fyo.beginMonth + "-01");
			if (tsTo.compareTo(beginDate) >= 0 && tsTo.compareTo(endDate) < 0)
			{
				tsFrom = TimeFormat.createTimeStamp2(fyo.fiscalYear.toString() + "-" + fyo.beginMonth + "-01");
			} else
			{
				tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1) + "-"
						+ fyo.beginMonth + "-01");
			}
		}
		return tsFrom;
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
