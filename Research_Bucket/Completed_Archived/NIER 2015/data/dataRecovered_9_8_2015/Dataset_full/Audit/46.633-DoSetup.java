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
import java.math.BigDecimal;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoSetup implements Action
{
	private String strClassName = "DoSetup: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			// do something
		} else if (formName.equals("viewActiveFiscalYears"))
		{
			fnPopulateActiveFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-active-fiscalyears-page");
		} else if (formName.equals("viewClosedFiscalYears"))
		{
			fnPopulateClosedFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-closed-fiscalyears-page");
		} else if (formName.equals("closeFiscalYear"))
		{
			fnCloseFiscalYear(servlet, req, res);
			fnPopulateActiveFiscalYears(servlet, req, res);
		}
		return new ActionRouter("acc-setup-active-fiscalyears-page");
	}

	protected void fnPopulateActiveFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateActiveFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYActive = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		req.setAttribute("vecFYActive", vecFYActive);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateClosedFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateClosedFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYClosed = FiscalYearNut.getValueObjectsGiven("status", "closed", null, null);
		req.setAttribute("vecFYClosed", vecFYClosed);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnCloseFiscalYear(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCloseFiscalYear()";
		Log.printVerbose("In " + strClassName + funcName);
		Collection colFY = FiscalYearNut.getCollectionByField("status", "active");
		Iterator itrFY = colFY.iterator();
		FiscalYear fy = null;
		FiscalYearObject fyo = null;
		while (itrFY.hasNext())
		{
			try
			{
				fy = (FiscalYear) itrFY.next();
				fyo = fy.getValueObject();
				break;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		try
		{
			usrid = lusr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		if (fyo != null && FiscalYearNut.fiscalYearIsValid(fyo) && usrid != null)
		{
			// Close fiscal year and open a new fiscal year
			FiscalYear newFiscalYear = null;
			Integer newPkId = null;
			try
			{
				fyo.status = "closed";
				fy.setValueObject(fyo);
				newFiscalYear = FiscalYearNut.getHome().create(new Integer(fyo.fiscalYear.intValue() + 2),
						fyo.beginMonth, fyo.endMonth, "active");
				newPkId = newFiscalYear.getPkId();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			// Calculate net profit for the fiscal year
			Vector vecDistinct = GeneralLedgerNut.getDistinctPCCenterAndBatch();
			for (int i = 0; i < vecDistinct.size(); i++)
			{
				GeneralLedgerObject glod = (GeneralLedgerObject) vecDistinct.get(i);
				Integer pcCenterId = glod.pcCenterId;
				Integer batchId = glod.batchId;
				Timestamp tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue()) + "-"
						+ fyo.beginMonth + "-01");
				Timestamp tsToNextDay = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1)
						+ "-" + fyo.beginMonth + "-01");
				Integer newREPkId = null;
				BigDecimal retainedEarnings = new BigDecimal("0.00");
				Vector vecGL = GeneralLedgerNut.getValueObjectsGiven("pccenterid", pcCenterId.toString(), "batchid",
						batchId.toString());
				for (int j = 0; j < vecGL.size(); j++)
				{
					GeneralLedgerObject glo = (GeneralLedgerObject) vecGL.get(j);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					BigDecimal amount = new BigDecimal("0.00");
					Vector vecJE = JournalEntryNut.getValueObjectsGivenDate("glaccid", glo.pkId.toString(), "typeid",
							"1", tsFrom, tsToNextDay);
					for (int k = 0; k < vecJE.size(); k++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(k);
						amount = amount.add(jeo.amount);
					}
					try
					{
						Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
						FiscalYearObject fyo2 = (FiscalYearObject) vecFY.get(0);
						if (glcao.realTemp.equals("temp"))
						{
							String ledgerSide = glcao.ledgerSide;
							if (ledgerSide.equals("cr"))
							{
								retainedEarnings = retainedEarnings.add(amount.negate());
							} else
							{
								retainedEarnings = retainedEarnings.subtract(amount);
							}
							// Close "temp" acc and create new "temp" acc with
							// zero opening balance
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									new BigDecimal("0.00"), tsCreate, "active", usrid, tsCreate);
						} else
						{
							// Close "real" acc and create new "real" acc with
							// opening balance brought forward
							Vector vecGLO = GeneralLedgerOpeningNut.getValueObjectsGiven("glid", glo.pkId.toString(),
									"fiscalyearid", fyo.pkId.toString());
							GeneralLedgerOpeningObject gloo = (GeneralLedgerOpeningObject) vecGLO.get(0);
							amount = amount.add(gloo.balance);
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									amount, tsCreate, "active", usrid, tsCreate);
							if (glcao.postToSection.equals("RetainedEarnings"))
							{
								newREPkId = newGLO.getPkId();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				Collection colRE = RetainedEarningsNut.getCollectionByField("pccenterid", pcCenterId.toString(),
						"batchid", batchId.toString(), "fiscalyearid", fyo.pkId.toString());
				Iterator itrRE = colRE.iterator();
				while (itrRE.hasNext())
				{
					RetainedEarnings re = (RetainedEarnings) itrRE.next();
					try
					{
						retainedEarnings.setScale(2);
						RetainedEarningsObject reo = re.getValueObject();
						reo.retainedEarnings = retainedEarnings;
						reo.status = "closed";
						re.setValueObject(reo);
						RetainedEarnings newRetainedEarnings = RetainedEarningsNut.getHome().create(pcCenterId,
								batchId, newPkId, new BigDecimal("0.00"), "active");
						GeneralLedgerOpening newRE = GeneralLedgerOpeningNut.getHandle(newREPkId);
						GeneralLedgerOpeningObject newREO = newRE.getValueObject();
						newREO.balance = (newREO.balance).add(retainedEarnings.negate());
						newRE.setValueObject(newREO);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}
}/*==========================================================
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
import java.math.BigDecimal;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoSetup implements Action
{
	private String strClassName = "DoSetup: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			// do something
		} else if (formName.equals("viewActiveFiscalYears"))
		{
			fnPopulateActiveFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-active-fiscalyears-page");
		} else if (formName.equals("viewClosedFiscalYears"))
		{
			fnPopulateClosedFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-closed-fiscalyears-page");
		} else if (formName.equals("closeFiscalYear"))
		{
			fnCloseFiscalYear(servlet, req, res);
			fnPopulateActiveFiscalYears(servlet, req, res);
		}
		return new ActionRouter("acc-setup-active-fiscalyears-page");
	}

	protected void fnPopulateActiveFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateActiveFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYActive = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		req.setAttribute("vecFYActive", vecFYActive);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateClosedFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateClosedFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYClosed = FiscalYearNut.getValueObjectsGiven("status", "closed", null, null);
		req.setAttribute("vecFYClosed", vecFYClosed);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnCloseFiscalYear(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCloseFiscalYear()";
		Log.printVerbose("In " + strClassName + funcName);
		Collection colFY = FiscalYearNut.getCollectionByField("status", "active");
		Iterator itrFY = colFY.iterator();
		FiscalYear fy = null;
		FiscalYearObject fyo = null;
		while (itrFY.hasNext())
		{
			try
			{
				fy = (FiscalYear) itrFY.next();
				fyo = fy.getValueObject();
				break;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		try
		{
			usrid = lusr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		if (fyo != null && FiscalYearNut.fiscalYearIsValid(fyo) && usrid != null)
		{
			// Close fiscal year and open a new fiscal year
			FiscalYear newFiscalYear = null;
			Integer newPkId = null;
			try
			{
				fyo.status = "closed";
				fy.setValueObject(fyo);
				newFiscalYear = FiscalYearNut.getHome().create(new Integer(fyo.fiscalYear.intValue() + 2),
						fyo.beginMonth, fyo.endMonth, "active");
				newPkId = newFiscalYear.getPkId();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			// Calculate net profit for the fiscal year
			Vector vecDistinct = GeneralLedgerNut.getDistinctPCCenterAndBatch();
			for (int i = 0; i < vecDistinct.size(); i++)
			{
				GeneralLedgerObject glod = (GeneralLedgerObject) vecDistinct.get(i);
				Integer pcCenterId = glod.pcCenterId;
				Integer batchId = glod.batchId;
				Timestamp tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue()) + "-"
						+ fyo.beginMonth + "-01");
				Timestamp tsToNextDay = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1)
						+ "-" + fyo.beginMonth + "-01");
				Integer newREPkId = null;
				BigDecimal retainedEarnings = new BigDecimal("0.00");
				Vector vecGL = GeneralLedgerNut.getValueObjectsGiven("pccenterid", pcCenterId.toString(), "batchid",
						batchId.toString());
				for (int j = 0; j < vecGL.size(); j++)
				{
					GeneralLedgerObject glo = (GeneralLedgerObject) vecGL.get(j);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					BigDecimal amount = new BigDecimal("0.00");
					Vector vecJE = JournalEntryNut.getValueObjectsGivenDate("glaccid", glo.pkId.toString(), "typeid",
							"1", tsFrom, tsToNextDay);
					for (int k = 0; k < vecJE.size(); k++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(k);
						amount = amount.add(jeo.amount);
					}
					try
					{
						Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
						FiscalYearObject fyo2 = (FiscalYearObject) vecFY.get(0);
						if (glcao.realTemp.equals("temp"))
						{
							String ledgerSide = glcao.ledgerSide;
							if (ledgerSide.equals("cr"))
							{
								retainedEarnings = retainedEarnings.add(amount.negate());
							} else
							{
								retainedEarnings = retainedEarnings.subtract(amount);
							}
							// Close "temp" acc and create new "temp" acc with
							// zero opening balance
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									new BigDecimal("0.00"), tsCreate, "active", usrid, tsCreate);
						} else
						{
							// Close "real" acc and create new "real" acc with
							// opening balance brought forward
							Vector vecGLO = GeneralLedgerOpeningNut.getValueObjectsGiven("glid", glo.pkId.toString(),
									"fiscalyearid", fyo.pkId.toString());
							GeneralLedgerOpeningObject gloo = (GeneralLedgerOpeningObject) vecGLO.get(0);
							amount = amount.add(gloo.balance);
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									amount, tsCreate, "active", usrid, tsCreate);
							if (glcao.postToSection.equals("RetainedEarnings"))
							{
								newREPkId = newGLO.getPkId();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				Collection colRE = RetainedEarningsNut.getCollectionByField("pccenterid", pcCenterId.toString(),
						"batchid", batchId.toString(), "fiscalyearid", fyo.pkId.toString());
				Iterator itrRE = colRE.iterator();
				while (itrRE.hasNext())
				{
					RetainedEarnings re = (RetainedEarnings) itrRE.next();
					try
					{
						retainedEarnings.setScale(2);
						RetainedEarningsObject reo = re.getValueObject();
						reo.retainedEarnings = retainedEarnings;
						reo.status = "closed";
						re.setValueObject(reo);
						RetainedEarnings newRetainedEarnings = RetainedEarningsNut.getHome().create(pcCenterId,
								batchId, newPkId, new BigDecimal("0.00"), "active");
						GeneralLedgerOpening newRE = GeneralLedgerOpeningNut.getHandle(newREPkId);
						GeneralLedgerOpeningObject newREO = newRE.getValueObject();
						newREO.balance = (newREO.balance).add(retainedEarnings.negate());
						newRE.setValueObject(newREO);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}
}/*==========================================================
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
import java.math.BigDecimal;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoSetup implements Action
{
	private String strClassName = "DoSetup: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			// do something
		} else if (formName.equals("viewActiveFiscalYears"))
		{
			fnPopulateActiveFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-active-fiscalyears-page");
		} else if (formName.equals("viewClosedFiscalYears"))
		{
			fnPopulateClosedFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-closed-fiscalyears-page");
		} else if (formName.equals("closeFiscalYear"))
		{
			fnCloseFiscalYear(servlet, req, res);
			fnPopulateActiveFiscalYears(servlet, req, res);
		}
		return new ActionRouter("acc-setup-active-fiscalyears-page");
	}

	protected void fnPopulateActiveFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateActiveFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYActive = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		req.setAttribute("vecFYActive", vecFYActive);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateClosedFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateClosedFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYClosed = FiscalYearNut.getValueObjectsGiven("status", "closed", null, null);
		req.setAttribute("vecFYClosed", vecFYClosed);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnCloseFiscalYear(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCloseFiscalYear()";
		Log.printVerbose("In " + strClassName + funcName);
		Collection colFY = FiscalYearNut.getCollectionByField("status", "active");
		Iterator itrFY = colFY.iterator();
		FiscalYear fy = null;
		FiscalYearObject fyo = null;
		while (itrFY.hasNext())
		{
			try
			{
				fy = (FiscalYear) itrFY.next();
				fyo = fy.getValueObject();
				break;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		try
		{
			usrid = lusr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		if (fyo != null && FiscalYearNut.fiscalYearIsValid(fyo) && usrid != null)
		{
			// Close fiscal year and open a new fiscal year
			FiscalYear newFiscalYear = null;
			Integer newPkId = null;
			try
			{
				fyo.status = "closed";
				fy.setValueObject(fyo);
				newFiscalYear = FiscalYearNut.getHome().create(new Integer(fyo.fiscalYear.intValue() + 2),
						fyo.beginMonth, fyo.endMonth, "active");
				newPkId = newFiscalYear.getPkId();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			// Calculate net profit for the fiscal year
			Vector vecDistinct = GeneralLedgerNut.getDistinctPCCenterAndBatch();
			for (int i = 0; i < vecDistinct.size(); i++)
			{
				GeneralLedgerObject glod = (GeneralLedgerObject) vecDistinct.get(i);
				Integer pcCenterId = glod.pcCenterId;
				Integer batchId = glod.batchId;
				Timestamp tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue()) + "-"
						+ fyo.beginMonth + "-01");
				Timestamp tsToNextDay = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1)
						+ "-" + fyo.beginMonth + "-01");
				Integer newREPkId = null;
				BigDecimal retainedEarnings = new BigDecimal("0.00");
				Vector vecGL = GeneralLedgerNut.getValueObjectsGiven("pccenterid", pcCenterId.toString(), "batchid",
						batchId.toString());
				for (int j = 0; j < vecGL.size(); j++)
				{
					GeneralLedgerObject glo = (GeneralLedgerObject) vecGL.get(j);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					BigDecimal amount = new BigDecimal("0.00");
					Vector vecJE = JournalEntryNut.getValueObjectsGivenDate("glaccid", glo.pkId.toString(), "typeid",
							"1", tsFrom, tsToNextDay);
					for (int k = 0; k < vecJE.size(); k++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(k);
						amount = amount.add(jeo.amount);
					}
					try
					{
						Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
						FiscalYearObject fyo2 = (FiscalYearObject) vecFY.get(0);
						if (glcao.realTemp.equals("temp"))
						{
							String ledgerSide = glcao.ledgerSide;
							if (ledgerSide.equals("cr"))
							{
								retainedEarnings = retainedEarnings.add(amount.negate());
							} else
							{
								retainedEarnings = retainedEarnings.subtract(amount);
							}
							// Close "temp" acc and create new "temp" acc with
							// zero opening balance
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									new BigDecimal("0.00"), tsCreate, "active", usrid, tsCreate);
						} else
						{
							// Close "real" acc and create new "real" acc with
							// opening balance brought forward
							Vector vecGLO = GeneralLedgerOpeningNut.getValueObjectsGiven("glid", glo.pkId.toString(),
									"fiscalyearid", fyo.pkId.toString());
							GeneralLedgerOpeningObject gloo = (GeneralLedgerOpeningObject) vecGLO.get(0);
							amount = amount.add(gloo.balance);
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									amount, tsCreate, "active", usrid, tsCreate);
							if (glcao.postToSection.equals("RetainedEarnings"))
							{
								newREPkId = newGLO.getPkId();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				Collection colRE = RetainedEarningsNut.getCollectionByField("pccenterid", pcCenterId.toString(),
						"batchid", batchId.toString(), "fiscalyearid", fyo.pkId.toString());
				Iterator itrRE = colRE.iterator();
				while (itrRE.hasNext())
				{
					RetainedEarnings re = (RetainedEarnings) itrRE.next();
					try
					{
						retainedEarnings.setScale(2);
						RetainedEarningsObject reo = re.getValueObject();
						reo.retainedEarnings = retainedEarnings;
						reo.status = "closed";
						re.setValueObject(reo);
						RetainedEarnings newRetainedEarnings = RetainedEarningsNut.getHome().create(pcCenterId,
								batchId, newPkId, new BigDecimal("0.00"), "active");
						GeneralLedgerOpening newRE = GeneralLedgerOpeningNut.getHandle(newREPkId);
						GeneralLedgerOpeningObject newREO = newRE.getValueObject();
						newREO.balance = (newREO.balance).add(retainedEarnings.negate());
						newRE.setValueObject(newREO);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}
}/*==========================================================
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
import java.math.BigDecimal;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoSetup implements Action
{
	private String strClassName = "DoSetup: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			// do something
		} else if (formName.equals("viewActiveFiscalYears"))
		{
			fnPopulateActiveFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-active-fiscalyears-page");
		} else if (formName.equals("viewClosedFiscalYears"))
		{
			fnPopulateClosedFiscalYears(servlet, req, res);
			return new ActionRouter("acc-setup-closed-fiscalyears-page");
		} else if (formName.equals("closeFiscalYear"))
		{
			fnCloseFiscalYear(servlet, req, res);
			fnPopulateActiveFiscalYears(servlet, req, res);
		}
		return new ActionRouter("acc-setup-active-fiscalyears-page");
	}

	protected void fnPopulateActiveFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateActiveFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYActive = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
		req.setAttribute("vecFYActive", vecFYActive);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateClosedFiscalYears(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateClosedFiscalYears()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecFYClosed = FiscalYearNut.getValueObjectsGiven("status", "closed", null, null);
		req.setAttribute("vecFYClosed", vecFYClosed);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnCloseFiscalYear(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCloseFiscalYear()";
		Log.printVerbose("In " + strClassName + funcName);
		Collection colFY = FiscalYearNut.getCollectionByField("status", "active");
		Iterator itrFY = colFY.iterator();
		FiscalYear fy = null;
		FiscalYearObject fyo = null;
		while (itrFY.hasNext())
		{
			try
			{
				fy = (FiscalYear) itrFY.next();
				fyo = fy.getValueObject();
				break;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		try
		{
			usrid = lusr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		if (fyo != null && FiscalYearNut.fiscalYearIsValid(fyo) && usrid != null)
		{
			// Close fiscal year and open a new fiscal year
			FiscalYear newFiscalYear = null;
			Integer newPkId = null;
			try
			{
				fyo.status = "closed";
				fy.setValueObject(fyo);
				newFiscalYear = FiscalYearNut.getHome().create(new Integer(fyo.fiscalYear.intValue() + 2),
						fyo.beginMonth, fyo.endMonth, "active");
				newPkId = newFiscalYear.getPkId();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			// Calculate net profit for the fiscal year
			Vector vecDistinct = GeneralLedgerNut.getDistinctPCCenterAndBatch();
			for (int i = 0; i < vecDistinct.size(); i++)
			{
				GeneralLedgerObject glod = (GeneralLedgerObject) vecDistinct.get(i);
				Integer pcCenterId = glod.pcCenterId;
				Integer batchId = glod.batchId;
				Timestamp tsFrom = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue()) + "-"
						+ fyo.beginMonth + "-01");
				Timestamp tsToNextDay = TimeFormat.createTimeStamp2(Integer.toString(fyo.fiscalYear.intValue() + 1)
						+ "-" + fyo.beginMonth + "-01");
				Integer newREPkId = null;
				BigDecimal retainedEarnings = new BigDecimal("0.00");
				Vector vecGL = GeneralLedgerNut.getValueObjectsGiven("pccenterid", pcCenterId.toString(), "batchid",
						batchId.toString());
				for (int j = 0; j < vecGL.size(); j++)
				{
					GeneralLedgerObject glo = (GeneralLedgerObject) vecGL.get(j);
					GLCodeObject glco = GLCodeNut.getObject(glo.glCodeId);
					GLCategoryObject glcao = GLCategoryNut.getObject(glco.glCategoryId);
					BigDecimal amount = new BigDecimal("0.00");
					Vector vecJE = JournalEntryNut.getValueObjectsGivenDate("glaccid", glo.pkId.toString(), "typeid",
							"1", tsFrom, tsToNextDay);
					for (int k = 0; k < vecJE.size(); k++)
					{
						JournalEntryObject jeo = (JournalEntryObject) vecJE.get(k);
						amount = amount.add(jeo.amount);
					}
					try
					{
						Vector vecFY = FiscalYearNut.getValueObjectsGiven("status", "active", null, null);
						FiscalYearObject fyo2 = (FiscalYearObject) vecFY.get(0);
						if (glcao.realTemp.equals("temp"))
						{
							String ledgerSide = glcao.ledgerSide;
							if (ledgerSide.equals("cr"))
							{
								retainedEarnings = retainedEarnings.add(amount.negate());
							} else
							{
								retainedEarnings = retainedEarnings.subtract(amount);
							}
							// Close "temp" acc and create new "temp" acc with
							// zero opening balance
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									new BigDecimal("0.00"), tsCreate, "active", usrid, tsCreate);
						} else
						{
							// Close "real" acc and create new "real" acc with
							// opening balance brought forward
							Vector vecGLO = GeneralLedgerOpeningNut.getValueObjectsGiven("glid", glo.pkId.toString(),
									"fiscalyearid", fyo.pkId.toString());
							GeneralLedgerOpeningObject gloo = (GeneralLedgerOpeningObject) vecGLO.get(0);
							amount = amount.add(gloo.balance);
							GeneralLedgerOpening newGLO = GeneralLedgerOpeningNut.getHome().create(glo.pkId, fyo2.pkId,
									amount, tsCreate, "active", usrid, tsCreate);
							if (glcao.postToSection.equals("RetainedEarnings"))
							{
								newREPkId = newGLO.getPkId();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				Collection colRE = RetainedEarningsNut.getCollectionByField("pccenterid", pcCenterId.toString(),
						"batchid", batchId.toString(), "fiscalyearid", fyo.pkId.toString());
				Iterator itrRE = colRE.iterator();
				while (itrRE.hasNext())
				{
					RetainedEarnings re = (RetainedEarnings) itrRE.next();
					try
					{
						retainedEarnings.setScale(2);
						RetainedEarningsObject reo = re.getValueObject();
						reo.retainedEarnings = retainedEarnings;
						reo.status = "closed";
						re.setValueObject(reo);
						RetainedEarnings newRetainedEarnings = RetainedEarningsNut.getHome().create(pcCenterId,
								batchId, newPkId, new BigDecimal("0.00"), "active");
						GeneralLedgerOpening newRE = GeneralLedgerOpeningNut.getHandle(newREPkId);
						GeneralLedgerOpeningObject newREO = newRE.getValueObject();
						newREO.balance = (newREO.balance).add(retainedEarnings.negate());
						newRE.setValueObject(newREO);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}
}