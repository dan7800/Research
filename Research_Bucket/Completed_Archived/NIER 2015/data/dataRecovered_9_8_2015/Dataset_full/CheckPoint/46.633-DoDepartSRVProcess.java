/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.trading;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.util.*;
import com.vlee.bean.inventory.EditDepartRMAForm;
import com.vlee.bean.inventory.EditRMAForm;
import com.vlee.ejb.inventory.RMATicketBean;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoDepartSRVProcess implements Action
{
	String strClassName = "DoRMACreate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String remark = req.getParameter("remark");
		req.setAttribute("remark", remark);
		if (formName == null)
		{
			return new ActionRouter("trading-pos-depart-srv-note-process-page");
		}
		if (formName.equals("saveDetails"))
		{
			try
			{
				fnSaveDetails(servlet, req, res);
				if((remark!=null)&&(remark.equals("editDepartRMA")))
				{
					fnRefresh(servlet, req, res);
					return new ActionRouter("trading-pos-depart-rma-edit-page");
		
				}
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("getSRV"))
		{
			try
			{
				fnGetSRV(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", "Invalid SRV Number!");
			}
		}
		// 20080307 Jimmy
		if (formName.equals("selectItemCode"))
		{
			try
			{
				fnSetItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", "Invalid Item Code !");
			}
		}
		return new ActionRouter("trading-pos-depart-srv-note-process-page");
	}

	private void fnSaveDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Log.printVerbose(" checkpoint 1...........");

		HttpSession session = req.getSession();
		EditRMAForm rmaForm = (EditRMAForm) session.getAttribute("trading-pos-depart-srv-edit-form");
		String txnType = req.getParameter("txnType");
		if (txnType != null)
		{
			rmaForm.setTxnType(txnType);
		}
		String txnResolution = req.getParameter("txnResolution");
		if (txnResolution != null)
		{
			rmaForm.setResolution(txnResolution);
		}
		
		String technician1Rmks = req.getParameter("technician1Rmks");
		if (technician1Rmks != null)
		{
			rmaForm.setTechnicianRmks(technician1Rmks);
		}
		
		String state = req.getParameter("state");
		if (state != null)
		{
			rmaForm.setState(state);
		}
		String branchFrom = req.getParameter("branchFrom");
		try
		{
			Integer iBranch = new Integer(branchFrom);
			rmaForm.setBranch(iBranch);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		String ownerEntityName = req.getParameter("ownerEntityName");
		String ownerEntityEmail = req.getParameter("ownerEntityEmail");
		String ownerEntityTelephone = req.getParameter("ownerEntityTelephone");
		String ownerEntityMobilePhone = req.getParameter("ownerEntityMobilePhone");
		String ownerEntityFax = req.getParameter("ownerEntityFax");
		String ownerEntityAdd1 = req.getParameter("ownerEntityAdd1");
		String ownerEntityAdd2 = req.getParameter("ownerEntityAdd2");
		String ownerEntityAdd3 = req.getParameter("ownerEntityAdd3");
		String ownerEntityState = req.getParameter("ownerEntityState");
		String ownerEntityCountry = req.getParameter("ownerEntityCountry");
		Log.printVerbose(" checkpoint 2...........");

		System.out.println("txnType : "+txnType);
		System.out.println("ownerEntityName : "+ownerEntityName);
		System.out.println("ownerEntityTelephone : "+ownerEntityTelephone);
		
		if (txnType != null && txnType.equals(RMATicketBean.RMA_TYPE_EXTERNAL) && ownerEntityName != null
				&& ownerEntityTelephone != null)
		{
			System.out.println("checkpoint a.............");
			
			rmaForm.setOwnerDetails(ownerEntityName, ownerEntityEmail, ownerEntityTelephone, ownerEntityMobilePhone,
					ownerEntityFax, ownerEntityAdd1, ownerEntityAdd2, ownerEntityAdd3, ownerEntityState,
					ownerEntityCountry);
			String priceParts = req.getParameter("priceParts");
			String priceLabour = req.getParameter("priceLabour");
			String priceDisposal = req.getParameter("priceDisposal");
			String priceTax = req.getParameter("priceTax");
			String amtPaid = req.getParameter("amtPaid");
			BigDecimal bdParts = new BigDecimal(0);
			BigDecimal bdLabour = new BigDecimal(0);
			BigDecimal bdDisposal = new BigDecimal(0);
			BigDecimal bdTax = new BigDecimal(0);
			BigDecimal bdPaid = new BigDecimal(0);
			
			System.out.println("checkpoint b.............");
			
			try
			{
				bdParts = new BigDecimal(priceParts);
				bdLabour = new BigDecimal(priceLabour);
				bdDisposal = new BigDecimal(priceDisposal);
				bdTax = new BigDecimal(priceTax);
				bdPaid = new BigDecimal(amtPaid);
			} catch (Exception ex)
			{
			}
			
			System.out.println("checkpoint c.............");
			
			rmaForm.setOwnerCharges(bdParts, bdLabour, bdDisposal, bdTax, bdPaid);
			
			System.out.println("checkpoint d.............");
		}
		Log.printVerbose(" checkpoint 3...........");
		String faultyGoodsPickup = req.getParameter("faultyGoodsPickup");	
		String itemCode = req.getParameter("itemCode");
		String quantity = req.getParameter("quantity");
		String ownerItemCode = req.getParameter("ownerItemCode");
		String supplierItemCode = req.getParameter("supplierItemCode");
		String itemName = req.getParameter("itemName");
		String itemSerial = req.getParameter("itemSerial");
		String remarks = req.getParameter("remarks");
		Log.printVerbose("remarks = "+remarks);
		String problemStmt = req.getParameter("problemStmt");
		String technician1Id = req.getParameter("technician1Id");
		String technician2Id = req.getParameter("technician2Id");
		String technician3Id = req.getParameter("technician3Id");
		String ownerReference = req.getParameter("ownerReference");
		String timePurchased = req.getParameter("timePurchased");
		String timeMalfunctionDate = req.getParameter("timeMalfunctionDate");
		String timeWarrantyExpiry = req.getParameter("timeWarrantyExpiry");
		String timeOwnerReceive = req.getParameter("timeOwnerReceive");
		String timeOwnerReturn = req.getParameter("timeOwnerReturn");
		
		Log.printVerbose(" checkpoint 4...........");
		
		Integer userId = (Integer) session.getAttribute("userId");	
		
		String timeSupplierReturnTo = req.getParameter("timeSupplierReturnTo");
		String timeSupplierReceiveFrom = req.getParameter("timeSupplierReceiveFrom");
		Timestamp tsSupplierReturnTo = TimeFormat.createTimestamp(timeSupplierReturnTo);
		Timestamp tsSupplierReceiveFrom = TimeFormat.createTimestamp(timeSupplierReceiveFrom);
		
		rmaForm.setTimeSupplier( tsSupplierReturnTo, tsSupplierReceiveFrom);

		
		try
		{
			Timestamp tsPurchased = TimeFormat.createTimestamp(timePurchased);
			Timestamp tsMalfunctionDate = TimeFormat.createTimestamp(timeMalfunctionDate);
			Timestamp tsWarrantyExpiry = TimeFormat.createTimestamp(timeWarrantyExpiry);
			Timestamp tsOwnerReceive = TimeFormat.createTimestamp(timeOwnerReceive);
			Timestamp tsOwnerReturn = TimeFormat.createTimestamp(timeOwnerReturn);

				
			rmaForm.setItemDetails(itemCode, new BigDecimal(quantity), ownerItemCode, supplierItemCode, itemName,
					itemSerial, remarks, problemStmt, new Integer(technician1Id), new Integer(technician2Id),
					new Integer(technician3Id), ownerReference, tsPurchased,
					tsMalfunctionDate, tsWarrantyExpiry, tsOwnerReceive, tsOwnerReturn,faultyGoodsPickup);
			rmaForm.setUserEdit(userId);
			
		Log.printVerbose(" checkpoint 5...........");
		} catch (Exception ex)
		{
		Log.printVerbose(" checkpoint 6...........");
			ex.printStackTrace();
		}
		Integer supplierEntityId = new Integer(req.getParameter("supplierEntityId"));
		String supplierEntityName = req.getParameter("supplierEntityName");
		String supplierEntityEmail = req.getParameter("supplierEntityEmail");
		Log.printVerbose(" checkpoint 7...........");
		String supplierEntityTelephone = req.getParameter("supplierEntityTelephone");
		String supplierEntityMobilePhone = req.getParameter("supplierEntityMobilePhone");
		String supplierEntityFax = req.getParameter("supplierEntityFax");
		String supplierEntityAdd1 = req.getParameter("supplierEntityAdd1");
		String supplierEntityAdd2 = req.getParameter("supplierEntityAdd2");
		String supplierEntityAdd3 = req.getParameter("supplierEntityAdd3");
		String supplierEntityState = req.getParameter("supplierEntityState");
		String supplierEntityCountry = req.getParameter("supplierEntityCountry");
		
		if (supplierEntityName != null && supplierEntityTelephone != null)
		{
			rmaForm.setSupplierDetails(supplierEntityId, supplierEntityName, supplierEntityEmail,
					supplierEntityTelephone, supplierEntityMobilePhone, supplierEntityFax, supplierEntityAdd1,
					supplierEntityAdd2, supplierEntityAdd3, supplierEntityState, supplierEntityCountry);
		}
		String rplItemCode = req.getParameter("rplItemCode");
		String rplQuantity = req.getParameter("rplQuantity");
		String rplItemName = req.getParameter("rplItemName");
		String rplItemSerial = req.getParameter("rplItemSerial");
		String rplItemDescription = req.getParameter("rplItemDescription");
		String timeSupplierReturn = req.getParameter("timeSupplierReturn");
		String timeSupplierReceive = req.getParameter("timeSupplierReceive");
		try
		{
			if (rplItemCode != null && rplItemName != null)
			{
				Timestamp tsSupplierReturn = TimeFormat.createTimestamp(timeSupplierReturn);
				Timestamp tsSupplierReceive = TimeFormat.createTimestamp(timeSupplierReceive);
				rmaForm.setReplacementItemDetails(rplItemCode, new BigDecimal(rplQuantity), rplItemName, rplItemSerial,
						rplItemDescription, tsSupplierReturn, tsSupplierReceive);
			} else
			{
				Timestamp tsSupplierReturn = TimeFormat.createTimestamp("0001-01-01");
				Timestamp tsSupplierReceive = TimeFormat.createTimestamp("0001-01-01");
				rmaForm.setReplacementItemDetails("", new BigDecimal(0), "", "", "", tsSupplierReturn,
						tsSupplierReceive);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		rmaForm.saveRMA();
		
	}

	private synchronized void fnGetSRV(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String srvPkid = req.getParameter("srvPkid");
		EditRMAForm rmaForm = new EditRMAForm(userId, new Long(srvPkid));
		session.setAttribute("trading-pos-depart-srv-edit-form", rmaForm);		
	}
	
	private synchronized void fnRefresh(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws Exception
	{
		HttpSession session = req.getSession();
		EditDepartRMAForm drmaEditForm = (EditDepartRMAForm) session.getAttribute("trading-pos-edit-depart-rma-form");
		drmaEditForm.getRMA();
	}
	
	// 20080307 Jimmy
	private synchronized void fnSetItemCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		HttpSession session = req.getSession();
		EditRMAForm rmaForm = (EditRMAForm) session.getAttribute("trading-pos-depart-srv-edit-form");
		
		rmaForm.setItemCode(req.getParameter("itemCode"));
		session.setAttribute("trading-pos-depart-srv-edit-form", rmaForm);
	}
}
