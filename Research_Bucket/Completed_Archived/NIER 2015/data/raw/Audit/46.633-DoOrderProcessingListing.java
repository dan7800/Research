/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import java.math.*;
import java.util.Vector;

import javax.servlet.http.*;

import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoOrderProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			HttpSession session = req.getSession();
         Integer userId = (Integer) session.getAttribute("userId");
         OrderProcessingForm opf = new OrderProcessingForm(userId);
         session.setAttribute("dist-order-processing-form", opf);
			return new ActionRouter("dist-order-processing-listing-page");
		}

		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(formName.equals("getList2"))
		{
			try
			{
				fnGetListing2(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setItemProductionStatus"))
		{
			try
			{
				fnSetProductionStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setProcessingWorker"))
		{
			try
			{
				fnSetProcessingWorker(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setSort"))
		{
			try
			{ fnSetSort(servlet,req,res); }
			catch(Exception ex)
			{ req.setAttribute("errMsg", ex.getMessage());}
		}

		if(formName.equals("setFlorists"))
		{
			try
			{ fnSetFlorists(servlet,req,res);}
			catch(Exception ex)
			{ ex.printStackTrace(); req.setAttribute("errMsg", ex.getMessage());}
		}
		
		if (formName.equals("setByCustTree"))
		{
			try
			{
				System.out.println("Inside setByCustTree");
				
				fnSetCustomer(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		return new ActionRouter("dist-order-processing-listing-page");
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	private void fnSetCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String option = req.getParameter("option");
		if (option == null)
		{
			return;
		}
		if (option.equals("setAcc"))
		{
			String accPkid = req.getParameter("accPkid");
			try
			{
				Integer pkid = new Integer(accPkid);
				CustAccountObject custObj = CustAccountNut.getObject(pkid);
				if (custObj != null)
				{
					req.setAttribute("custObj", custObj);
					req.setAttribute("custPkid", custObj.pkid.toString());
				} else
				{
					throw new Exception("Invalid Account");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Account Number!");
			}
		}
	}
	
	private void fnSetFlorists(HttpServlet servlet,
										HttpServletRequest req,
										HttpServletResponse res)
		throws Exception
	{

		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");

		String[] itemPkid = req.getParameterValues("itemPkid");
		String[] floristId = req.getParameterValues("floristId");
		for(int cnt1=0;cnt1<itemPkid.length;cnt1++)
		{
			try
			{
				Integer userId = (Integer) session.getAttribute("userId");
				Long iPkid = new Long(itemPkid[cnt1]);
				Integer iFlorist = new Integer(floristId[cnt1]);
				SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(iPkid);
				SalesOrderItemObject soItmObj = soItmEJB.getObject();
				if(!soItmObj.valueadd1Userid.equals(iFlorist))
				{
					Integer oldFlorist = soItmObj.valueadd1Userid;
					UserObject floristOld = UserNut.getObject(oldFlorist);
					UserObject floristNew = UserNut.getObject(iFlorist);
					
					soItmObj.valueadd1Userid = iFlorist;
					soItmObj.valueadd2Userid = iFlorist;
					soItmEJB.setObject(soItmObj);

					/// record in the audit trail!
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "UPDATE-ORDER";
					dpiObj.category = "ASSIGN-FLORIST";
					dpiObj.auditLevel = new Integer(1);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soItmObj.indexId;
					dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("FLORIST",floristOld.userName,
													floristNew.userName, "");
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		opf.getList();
	}


	private void fnSetSort(HttpServlet servlet,
									HttpServletRequest req,
									HttpServletResponse res)
				throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		String sortBy = req.getParameter("sortBy");
		opf.setSort(sortBy);
	}



	private void fnSetProcessingWorker(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soItemPkid = req.getParameter("soItemPkid");
		String userProcessing = req.getParameter("userProcessing");
		String prodPts = req.getParameter("prodPts");
		String creaPts = req.getParameter("creaPts");
		try
		{
			Long itemPkid = new Long(soItemPkid);
			Integer iUser = new Integer(userProcessing);
			BigDecimal prodPoints = new BigDecimal(prodPts);
			BigDecimal creaPoints = new BigDecimal(creaPts);
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(itemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.valueadd1Userid = iUser;
			soItemObj.valueadd2Userid = iUser;
			soItemObj.valueadd1Points = prodPoints;
			soItemObj.valueadd2Points = creaPoints;
			soItemEJB.setObject(soItemObj);
			HttpSession session = req.getSession();
			OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
			opf.getList();
		} 
		catch(Exception ex)
		{
			throw new Exception("Unable to set production worker details!");
		}


	}

	private void fnSetProductionStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strItemPkid = req.getParameter("itemPkid");
		String productionStatus = req.getParameter("productionStatus");
		try
		{
			Long itemPkid = new Long(strItemPkid);
			SalesOrderItem soEJB = SalesOrderItemNut.getHandle(itemPkid);
			if (soEJB != null)
			{
				soEJB.setProductionStatus(productionStatus);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		opf.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		boolean floristFilter = false;
		String orderTakerId = (String) req.getParameter("orderTakerId");
		String floristId = (String) req.getParameter("floristId");
		String senderAcc = (String) req.getParameter("accID");
		String orderNumber = (String) req.getParameter("orderNumber");
		String sender = (String) req.getParameter("sender");
		String recipient = (String) req.getParameter("recipient");
		String deliveryFromName = (String) req.getParameter("deliveryFromName");
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String productionStatus = (String) req.getParameter("productionStatus");
		Integer iReceiptMode = new Integer(req.getParameter("receiptMode"));
		String receiptRemarks = (String) req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		if (iReceiptMode.intValue() != 0)
		{
			CardPaymentConfigObject theObj = CardPaymentConfigNut.getObject(iReceiptMode);
			String receiptMode = theObj.paymentMode;
			opf.setReceiptMode(receiptMode);
		} else
		{
			opf.setReceiptMode("all");
		}
		
		Log.printVerbose(sender);
		
		if(floristId!=null && !floristId.equals("all"))
			floristFilter = true;
		
		opf.setOrderTakerId(orderTakerId);
		opf.setFloristFilter(floristFilter, floristId);
		opf.setAccID(senderAcc);
		opf.setOrderNumber(orderNumber);
		opf.setSender(sender);
		opf.setRecipient(recipient);
		opf.setDeliveryFromName(deliveryFromName);
		opf.setBranchId(branch);
		opf.setDateType(dateType);
		opf.setDateRange(dateFrom, dateTo);
		opf.setProductionStatus(productionStatus);
		opf.setReceiptRemarks(receiptRemarks);
		
		opf.getList();
	}

	private void fnGetListing2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		opf.getList();
	}
}
/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import java.math.*;
import javax.servlet.http.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoOrderProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-order-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("getList2"))
		{
			try
			{
				fnGetListing2(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setItemProductionStatus"))
		{
			try
			{
				fnSetProductionStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setProcessingWorker"))
		{
			try
			{
				fnSetProcessingWorker(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setSort"))
		{
			try
			{ fnSetSort(servlet,req,res); }
			catch(Exception ex)
			{ req.setAttribute("errMsg", ex.getMessage());}
		}

		if(formName.equals("setFlorists"))
		{
			try
			{ fnSetFlorists(servlet,req,res);}
			catch(Exception ex)
			{ ex.printStackTrace(); req.setAttribute("errMsg", ex.getMessage());}
		}

		return new ActionRouter("dist-order-processing-listing-page");
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void fnSetFlorists(HttpServlet servlet,
										HttpServletRequest req,
										HttpServletResponse res)
		throws Exception
	{

		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");

		String[] itemPkid = req.getParameterValues("itemPkid");
		String[] floristId = req.getParameterValues("floristId");
		for(int cnt1=0;cnt1<itemPkid.length;cnt1++)
		{
			try
			{
				Integer userId = (Integer) session.getAttribute("userId");
				Long iPkid = new Long(itemPkid[cnt1]);
				Integer iFlorist = new Integer(floristId[cnt1]);
				SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(iPkid);
				SalesOrderItemObject soItmObj = soItmEJB.getObject();
				if(!soItmObj.valueadd1Userid.equals(iFlorist))
				{
					Integer oldFlorist = soItmObj.valueadd1Userid;
					UserObject floristOld = UserNut.getObject(oldFlorist);
					UserObject floristNew = UserNut.getObject(iFlorist);
					
					soItmObj.valueadd1Userid = iFlorist;
					soItmObj.valueadd2Userid = iFlorist;
					soItmEJB.setObject(soItmObj);

					/// record in the audit trail!
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "ORDER-UPDATE";
					dpiObj.category = "ASSIGN-FLORIST";
					dpiObj.auditLevel = new Integer(1);
//       dpiObj.processId = new Long(0);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soItmObj.indexId;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
					dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("FLORIST",floristOld.userName,
													floristNew.userName, "");
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);

				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		opf.getList();
	}


	private void fnSetSort(HttpServlet servlet,
									HttpServletRequest req,
									HttpServletResponse res)
				throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		String sortBy = req.getParameter("sortBy");
		opf.setSort(sortBy);
	}



	private void fnSetProcessingWorker(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soItemPkid = req.getParameter("soItemPkid");
		String userProcessing = req.getParameter("userProcessing");
		String prodPts = req.getParameter("prodPts");
		String creaPts = req.getParameter("creaPts");
		try
		{
			Long itemPkid = new Long(soItemPkid);
			Integer iUser = new Integer(userProcessing);
			BigDecimal prodPoints = new BigDecimal(prodPts);
			BigDecimal creaPoints = new BigDecimal(creaPts);
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(itemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.valueadd1Userid = iUser;
			soItemObj.valueadd2Userid = iUser;
			soItemObj.valueadd1Points = prodPoints;
			soItemObj.valueadd2Points = creaPoints;
			soItemEJB.setObject(soItemObj);
			HttpSession session = req.getSession();
			OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
			opf.getList();
		} 
		catch(Exception ex)
		{
			throw new Exception("Unable to set production worker details!");
		}


	}

	private void fnSetProductionStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strItemPkid = req.getParameter("itemPkid");
		String productionStatus = req.getParameter("productionStatus");
		try
		{
			Long itemPkid = new Long(strItemPkid);
			SalesOrderItem soEJB = SalesOrderItemNut.getHandle(itemPkid);
			if (soEJB != null)
			{
				soEJB.setProductionStatus(productionStatus);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		opf.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String sender = (String) req.getParameter("sender");
		String recipient = (String) req.getParameter("recipient");
		String deliveryFromName = (String) req.getParameter("deliveryFromName");
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String productionStatus = (String) req.getParameter("productionStatus");
		Integer iReceiptMode = new Integer(req.getParameter("receiptMode"));
		String receiptRemarks = (String) req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		if (iReceiptMode.intValue() != 0)
		{
			CardPaymentConfigObject theObj = CardPaymentConfigNut.getObject(iReceiptMode);
			String receiptMode = theObj.paymentMode;
			opf.setReceiptMode(receiptMode);
		} else
		{
			opf.setReceiptMode("all");
		}
		
		Log.printVerbose(sender);
		
		opf.setSender(sender);
		opf.setRecipient(recipient);
		opf.setDeliveryFromName(deliveryFromName);
		opf.setBranchId(branch);
		opf.setDateType(dateType);
		opf.setDateRange(dateFrom, dateTo);
		opf.setProductionStatus(productionStatus);
		opf.setReceiptRemarks(receiptRemarks);
		
		opf.getList();
	}

	private void fnGetListing2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		opf.getList();
	}
}
/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import java.math.*;
import javax.servlet.http.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoOrderProcessingListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		if (formName == null)
		{
			return new ActionRouter("dist-order-processing-listing-page");
		}
		if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("getList2"))
		{
			try
			{
				fnGetListing2(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (formName.equals("setItemProductionStatus"))
		{
			try
			{
				fnSetProductionStatus(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setProcessingWorker"))
		{
			try
			{
				fnSetProcessingWorker(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setSort"))
		{
			try
			{ fnSetSort(servlet,req,res); }
			catch(Exception ex)
			{ req.setAttribute("errMsg", ex.getMessage());}
		}

		if(formName.equals("setFlorists"))
		{
			try
			{ fnSetFlorists(servlet,req,res);}
			catch(Exception ex)
			{ ex.printStackTrace(); req.setAttribute("errMsg", ex.getMessage());}
		}

		return new ActionRouter("dist-order-processing-listing-page");
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void fnSetFlorists(HttpServlet servlet,
										HttpServletRequest req,
										HttpServletResponse res)
		throws Exception
	{

		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");

		String[] itemPkid = req.getParameterValues("itemPkid");
		String[] floristId = req.getParameterValues("floristId");
		for(int cnt1=0;cnt1<itemPkid.length;cnt1++)
		{
			try
			{
				Integer userId = (Integer) session.getAttribute("userId");
				Long iPkid = new Long(itemPkid[cnt1]);
				Integer iFlorist = new Integer(floristId[cnt1]);
				SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(iPkid);
				SalesOrderItemObject soItmObj = soItmEJB.getObject();
				if(!soItmObj.valueadd1Userid.equals(iFlorist))
				{
					Integer oldFlorist = soItmObj.valueadd1Userid;
					UserObject floristOld = UserNut.getObject(oldFlorist);
					UserObject floristNew = UserNut.getObject(iFlorist);
					
					soItmObj.valueadd1Userid = iFlorist;
					soItmObj.valueadd2Userid = iFlorist;
					soItmEJB.setObject(soItmObj);

					/// record in the audit trail!
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "UPDATE-ORDER";
					dpiObj.category = "ASSIGN-FLORIST";
					dpiObj.auditLevel = new Integer(1);
//       dpiObj.processId = new Long(0);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soItmObj.indexId;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
					dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("FLORIST",floristOld.userName,
													floristNew.userName, "");
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);

				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		opf.getList();
	}


	private void fnSetSort(HttpServlet servlet,
									HttpServletRequest req,
									HttpServletResponse res)
				throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		String sortBy = req.getParameter("sortBy");
		opf.setSort(sortBy);
	}



	private void fnSetProcessingWorker(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String soItemPkid = req.getParameter("soItemPkid");
		String userProcessing = req.getParameter("userProcessing");
		String prodPts = req.getParameter("prodPts");
		String creaPts = req.getParameter("creaPts");
		try
		{
			Long itemPkid = new Long(soItemPkid);
			Integer iUser = new Integer(userProcessing);
			BigDecimal prodPoints = new BigDecimal(prodPts);
			BigDecimal creaPoints = new BigDecimal(creaPts);
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(itemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.valueadd1Userid = iUser;
			soItemObj.valueadd2Userid = iUser;
			soItemObj.valueadd1Points = prodPoints;
			soItemObj.valueadd2Points = creaPoints;
			soItemEJB.setObject(soItemObj);
			HttpSession session = req.getSession();
			OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
			opf.getList();
		} 
		catch(Exception ex)
		{
			throw new Exception("Unable to set production worker details!");
		}


	}

	private void fnSetProductionStatus(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strItemPkid = req.getParameter("itemPkid");
		String productionStatus = req.getParameter("productionStatus");
		try
		{
			Long itemPkid = new Long(strItemPkid);
			SalesOrderItem soEJB = SalesOrderItemNut.getHandle(itemPkid);
			if (soEJB != null)
			{
				soEJB.setProductionStatus(productionStatus);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		opf.getList();
	}

	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String sender = (String) req.getParameter("sender");
		String recipient = (String) req.getParameter("recipient");
		String deliveryFromName = (String) req.getParameter("deliveryFromName");
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String productionStatus = (String) req.getParameter("productionStatus");
		Integer iReceiptMode = new Integer(req.getParameter("receiptMode"));
		String receiptRemarks = (String) req.getParameter("receiptRemarks");
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		if (iReceiptMode.intValue() != 0)
		{
			CardPaymentConfigObject theObj = CardPaymentConfigNut.getObject(iReceiptMode);
			String receiptMode = theObj.paymentMode;
			opf.setReceiptMode(receiptMode);
		} else
		{
			opf.setReceiptMode("all");
		}
		
		Log.printVerbose(sender);
		
		opf.setSender(sender);
		opf.setRecipient(recipient);
		opf.setDeliveryFromName(deliveryFromName);
		opf.setBranchId(branch);
		opf.setDateType(dateType);
		opf.setDateRange(dateFrom, dateTo);
		opf.setProductionStatus(productionStatus);
		opf.setReceiptRemarks(receiptRemarks);
		
		opf.getList();
	}

	private void fnGetListing2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		OrderProcessingForm opf = (OrderProcessingForm) session.getAttribute("dist-order-processing-form");
		if (opf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			opf = new OrderProcessingForm(userId);
			session.setAttribute("dist-order-processing-form", opf);
		}
		opf.getList();
	}
}
