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

import com.vlee.servlet.main.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.math.*;
import java.sql.*;
import java.lang.*;

import com.vlee.ejb.supplier.PurchaseOrderBean;
import com.vlee.ejb.supplier.PurchaseOrderItemObject;
import com.vlee.ejb.supplier.SuppAccountNut;
import com.vlee.ejb.supplier.SuppAccountObject;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.bean.procurement.CreatePOForm;
import com.vlee.bean.procurement.EditPOForm;
import com.vlee.bean.procurement.PurchaseOrderStockSession;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoFloristEditPOFromSOOutstation extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession();				
		String formName = req.getParameter("formName");
				
		if(formName == null)
			formName = "";
		
		System.out.println("formName : "+formName);
		
		String subFormName = "";
		try
		{
			subFormName = formName.substring(0, 8);
		}
		catch(Exception ex){}	
		
		if (formName.equals("setSOPkid"))
		{
			return setSOPkid(servlet, req, res);
		}		
		else if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
		}
		else if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);

			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("setOtherDetails"))
		{
			setOtherDetails(servlet, req, res);
		}
		else if (formName.equals("addStockWithESS"))
		{
			try
			{
				fnAddStockWithESS(servlet, req, res);
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
		}
		else if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("rmDocRow"))
		{
			try
			{
				fnRmDocRow(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}	
		else if (formName.equals("setDocRowDescriptions"))
		{
			try
			{
				fnSetDocRowDescriptions(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("print"))
		{
			fnPrint(servlet, req, res);			
			
			return new ActionRouter("dist-florist-process-outstation-print-page");
		}
		else if (subFormName.equals("setImage"))
		{
			try
			{
				fnSetImage(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}	
		
		return new ActionRouter("dist-florist-process-outstation-edit-po-page");			
	}
	
	private ActionRouter setSOPkid(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();		
						
		String soPkid = (String) req.getParameter("soPkid");
		Long lOrder = new Long(0);
		try{ lOrder = new Long(soPkid); } catch(Exception ex) { }
		SalesOrderIndexObject soObj = null;
		try
		{
			soObj = SalesOrderIndexNut.getObject(lOrder);
			
			if(soObj == null)
			{
				throw new Exception("Invalid Order Number!");
			}
		}
		catch(Exception ex){}
		
		if(soObj.interfloraOsPONo.intValue() == 0)
		{
			// Create new PO
			try
			{
				req.setAttribute("newForm", "true");
				session.setAttribute("soPkid", lOrder.toString());
				return new ActionRouter("dist-florist-process-outstation-create-po-page");	
				
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}	
		}
		else
		{
			// Edit PO
			try
			{
				fnLoadPO(servlet, req, res, lOrder);
				return new ActionRouter("dist-florist-process-outstation-edit-po-page");	
				
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}	
		}
		
		return new ActionRouter("dist-florist-process-outstation-edit-po-page");	
	}

	private void fnLoadPO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, Long soPkid) throws Exception
	{		
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		
		SalesOrderIndexObject soObj = null;
		try
		{
			soObj = SalesOrderIndexNut.getObject(soPkid);
			
			if(soObj == null)
			{
				throw new Exception("Invalid Order Number!");
			}
		}
		catch(Exception ex){}
		
		try
		{
			Long poId = soObj.interfloraOsPONo;				
			EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");		
			if (epof == null)
			{			
				epof = new EditPOForm(poId, userid);		
				session.setAttribute("distribution-edit-po-from-so-outstation-form", epof);
			}
			
			session.setAttribute("soPkid", soPkid.toString());
			epof.loadPO(poId);
			epof.setSOPkid(soPkid);
			epof.setLinkFromSO("true");		
			epof.setIntefloraDetails("", "", "", "", soObj.interfloraOtherInstructions, soObj.interfloraSendingFlorist);												
									
			if(PurchaseOrderBean.STATE_APPROVED.equals(epof.poObj.mState))
			{
				req.setAttribute("canEdit", "Cannot Edit "+epof.poObj.mPkid.toString()+" as has been APPROVED");
			}
						
			if(epof.poObj.vecPurchaseOrderItems != null)
			{
				Vector vecItem = new Vector(epof.poObj.vecPurchaseOrderItems);			
				
				for(int cnt1=0; cnt1<vecItem.size(); cnt1++)
				{
					PurchaseOrderItemObject poiObj = (PurchaseOrderItemObject) vecItem.get(cnt1);
					
					if(poiObj.mOutstandingQty.compareTo(new BigDecimal(0)) <= 0)
					{
						req.setAttribute("canEdit", "Cannot Edit "+epof.poObj.mPkid.toString()+" as GRN has been issued");
						return;
					}		
				}			
			}
			
			// Janet
			// create the PurchaseOrderStockSession so that the po items could be edited
			System.out.println("-------- Putting the items into PurchaseOrderStockSession to allow edit");
			
			TreeMap tableRows = epof.getTableRows();
			Vector vecRows = new Vector(tableRows.values());
			Integer userId = (Integer) session.getAttribute("userId");
			
			for(int cnt1=0;cnt1<vecRows.size();cnt1++)
			{
				DocRow docRow = (DocRow) vecRows.get(cnt1);
				System.out.println("ItemCode : "+docRow.getItemCode());
				
				PurchaseOrderStockSession efif = new PurchaseOrderStockSession(docRow,epof.branch.pkid);
				efif.setGuid(docRow.getGuid());
				session.setAttribute("procurement-po-edit" + PurchaseOrderStockSession.OBJNAME, efif);
				session.setAttribute(efif.getKey(),efif);			
				
				System.out.println("docRow.getGuid() : "+docRow.getGuid());
			}	
			// End Janet
								
		} catch (Exception ex)
		{
			throw ex;
		}
	}
		
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");
		epof.setBranch(new Integer(req.getParameter("branchId")));
	}
	
	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");
		
		try
		{			
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!epof.setSupplier(iSuppAcc))
			{
				throw new Exception("Invalid Supplier Account");
			}
			
			epof.setInterfloraSendingFlorist(suppAccObj.nameFirst + " " + suppAccObj.nameLast);
			
		} catch (Exception ex)
		{
			throw new Exception("Invalid Supplier PKID: " + ex.getMessage());
		}
	}
	
	protected void setOtherDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String interfloraOtherInstructions = (String) req.getParameter("interfloraOtherInstructions");
		String interfloraSendingFlorist = (String) req.getParameter("interfloraSendingFlorist");
		
		interfloraOtherInstructions = StringManup.truncate(interfloraOtherInstructions,450);
		
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");
		epof.setIntefloraDetails("", "", "", "", interfloraOtherInstructions, interfloraSendingFlorist);
	}
	
	protected void fnAddStockWithESS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String essGuid = req.getParameter("essGuid");
		Log.printVerbose("essGuid = " + essGuid);
		
		PurchaseOrderStockSession ess = (PurchaseOrderStockSession) session.getAttribute(essGuid);
		
		ItemObject itmObj = ess.getItemObject();
		BranchObject branch = ess.getBranch();
		
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");		
		
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		
		Log.printVerbose("TUPP: DoFloristEditPOFromSO::fnAddStockESS() checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		try
		{
			Log.printVerbose("TUPP: DoFloristEditPOFromSO::fnAddStockESS()checkpoint 5..........................");
			docrow = new DocRow();
			docrow.setGuid(ess.getGuid()); 
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.description1 = itmObj.name;
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
			Vector vecSN = ess.getSerialNumbers();
			if (vecSN != null)
			{
				for (int cnt = 0; cnt < vecSN.size(); cnt++)
				{
					String sn = (String) vecSN.get(cnt);
					docrow.addSerial(sn);
					Log.printVerbose("TUPP: DoFloristEditPOFromSO::fnAddStockESS() checkpoint 6..........................");
				}
			}
			Log.printVerbose("TUPP: DoFloristEditPOFromSO::fnAddStockESS() checkpoint 7..........................");
			epof.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");	
		epof.reset();
	}
	
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");
		try
		{			
			epof.confirmAndSave();
			
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}
	
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");	
		epof.dropDocRow(docRowKey);
	}
	
	protected void fnSetDocRowDescriptions(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		String description1 = req.getParameter("itemDescription1");
		String description2 = req.getParameter("itemDescription2");
		
		description1 = StringManup.truncate(description1,450);
		description2 = StringManup.truncate(description2,450);
		
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");	
		
		epof.setDocRowDescriptions(docRowKey, description1, description2);
		
		System.out.println("aaaaaaaaaaaa");
	}
	
	protected void fnSetImage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		String strImg_thumbnail = req.getParameter("imageThumb");
				
		Long img_thumbnail = new Long(0);
		try
		{
			img_thumbnail = new Long(strImg_thumbnail);
		}
		catch(Exception ex)
		{
			return;
		}
		
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("distribution-edit-po-from-so-outstation-form");	
		
		epof.setImage(docRowKey, img_thumbnail);
		
		System.out.println("bbbbbbbbbbbbb");
	}
	
	protected void fnPrint(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String soPkid = (String) req.getParameter("soPkid");
		
		Vector vecOrder = new Vector();
			
		try
		{
			Long lPkid = new Long(soPkid);
			SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
			vecOrder.add(soObj);			
		}
		catch(Exception ex)
		{ ex.printStackTrace();}
		
		req.setAttribute("vecOrder", vecOrder);		
	}
}
