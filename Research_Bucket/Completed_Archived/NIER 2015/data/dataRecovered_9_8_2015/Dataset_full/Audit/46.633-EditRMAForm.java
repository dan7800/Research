/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.inventory;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;

public class EditRMAForm extends java.lang.Object implements Serializable
{
	public Vector vecRecentlyCreatedRMA;
	Integer userId = null;
	Integer previousBranch = new Integer(0);
	RMATicketObject rmaTicket = null;
	

	// MEMBER VARIABLES
	public EditRMAForm(Integer userId, Long rmaPkid) throws Exception
	{
		this.userId = userId;
		this.rmaTicket = RMATicketNut.getObject(rmaPkid);
		
		if (this.rmaTicket == null)
		{
			throw new Exception("Invalid RMA Number!");
		}
	}

	public RMATicketObject getTicket()
	{
		return this.rmaTicket;
	}

	public String getBranchFromId(String buf)
	{
		return this.rmaTicket.branchFrom.toString();
	}

	public String getBranchToId(String buf)
	{
		return this.rmaTicket.branchTo.toString();
	}

	public void setTxnType(String buf)
	{
		this.rmaTicket.txnType = buf;
	}

	public void setResolution(String buf)
	{
		this.rmaTicket.txnResolution = buf;
	}
	
	public void setTechnicianRmks(String technician1Rmks)
	{
		Log.printVerbose(technician1Rmks);
		this.rmaTicket.technician1Rmks = technician1Rmks;
		Log.printVerbose(technician1Rmks);
	}

	public void setState(String buf)
	{
		this.rmaTicket.state = buf;
	}

	public void setOwnerCharges(BigDecimal bdParts, BigDecimal bdLabour, BigDecimal bdDisposal, BigDecimal bdTax,
			BigDecimal bdPaid)
	{
		this.rmaTicket.priceParts = bdParts;
		this.rmaTicket.priceLabour = bdLabour;
		this.rmaTicket.priceDisposal = bdDisposal;
		this.rmaTicket.priceTax = bdTax;
		this.rmaTicket.amtPaid = bdPaid;
	}

	public void setUserEdit(Integer userid)
	{
		this.rmaTicket.useridEdit = userid;
	}
	
	public void setItemCode(String itemCode)
	{
		ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);
		if (itmObj != null)
		{
			this.rmaTicket.itemCode = itemCode;
			this.rmaTicket.itemName = itmObj.name;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = this.userId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Edited SRV Note Item: " +this.rmaTicket.pkid;
			AuditTrailNut.fnCreate(atObj);
		}
	}
	
	public void setItemDetails(String itemCode, BigDecimal quantity, String ownerItemCode, String supplierItemCode,
			String itemName, String itemSerial, String remarks, String problemStmt, Integer technician1Id,
			Integer technician2Id, Integer technician3Id,
			String ownerReference, Timestamp tsPurchased, Timestamp tsMalfunctionDate, Timestamp tsWarrantyExpiry,
			Timestamp tsOwnerReceive, Timestamp tsOwnerReturn, String faultyGoodsPickup)
	{
		// / validate item code
		Log.printVerbose("itemCode: " + itemCode);
		if (!itemCode.equals(""))
		{
			ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);
		
			if (itmObj != null)
			{
				this.rmaTicket.itemCode = itemCode;
				this.rmaTicket.itemName = itmObj.name;
			} else {
				this.rmaTicket.itemCode = "";
				this.rmaTicket.itemName = itemName;
			}
		}
		else
		{
			this.rmaTicket.itemCode = "";
			this.rmaTicket.itemName = itemName;
		}
		this.rmaTicket.quantity = quantity;
		this.rmaTicket.ownerItemCode = ownerItemCode;
		this.rmaTicket.supplierItemCode = supplierItemCode;
		this.rmaTicket.itemSerial = itemSerial;
		this.rmaTicket.remarks = remarks;
		Log.printVerbose(" the remarks inside the EditRMAFORM = "+this.rmaTicket.remarks);
		this.rmaTicket.problemStmt = problemStmt;
		this.rmaTicket.technician1Id = technician1Id;
		this.rmaTicket.technician2Id = technician2Id;
		this.rmaTicket.technician3Id = technician3Id;
		this.rmaTicket.ownerReference = ownerReference;
		this.rmaTicket.timePurchased = tsPurchased;
		this.rmaTicket.timeMalfunctionDate = tsMalfunctionDate;
		this.rmaTicket.timeWarrantyExpiry = tsWarrantyExpiry;
		this.rmaTicket.timeOwnerReceive = tsOwnerReceive;
		this.rmaTicket.timeOwnerReturn = tsOwnerReturn;
		this.rmaTicket.faultyGoodsPickup = faultyGoodsPickup;

	}

	public void setBranch(Integer iBranch)
	{
		if (iBranch != null)
		{
			BranchObject branchObj = BranchNut.getObject(iBranch);
			if (branchObj != null)
			{
				this.rmaTicket.branchFrom = branchObj.pkid;
				this.rmaTicket.locationFrom = branchObj.invLocationId;
				if (this.rmaTicket.txnType.equals(RMATicketBean.RMA_TYPE_INTERNAL))
				{
					setOwnerDetails(branchObj.description, branchObj.emailAdmin, branchObj.phoneNo, "",
							branchObj.faxNo, branchObj.addr1, branchObj.addr2, branchObj.addr3, branchObj.state,
							branchObj.countryCode);
				}
				this.previousBranch = iBranch;
			}
		}
	}

	public boolean hasValidBranch()
	{
		if (this.rmaTicket.branchFrom.intValue() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public void setOwnerDetails(String ownerEntityName, String ownerEntityEmail, String ownerEntityTelephone,
			String ownerEntityMobilePhone, String ownerEntityFax, String ownerEntityAdd1, String ownerEntityAdd2,
			String ownerEntityAdd3, String ownerEntityState, String ownerEntityCountry)
	{
		this.rmaTicket.ownerEntityName = ownerEntityName;
		this.rmaTicket.ownerEntityEmail = ownerEntityEmail;
		this.rmaTicket.ownerEntityTelephone = ownerEntityTelephone;
		this.rmaTicket.ownerEntityMobilePhone = ownerEntityMobilePhone;
		this.rmaTicket.ownerEntityFax = ownerEntityFax;
		this.rmaTicket.ownerEntityAdd1 = ownerEntityAdd1;
		this.rmaTicket.ownerEntityAdd2 = ownerEntityAdd2;
		this.rmaTicket.ownerEntityAdd3 = ownerEntityAdd3;
		this.rmaTicket.ownerEntityState = ownerEntityState;
		this.rmaTicket.ownerEntityCountry = ownerEntityCountry;
	}

   public void setCustomer(CustAccountObject custObj)
   {
      this.rmaTicket.ownerEntityTable = CustAccountBean.TABLENAME;
      this.rmaTicket.ownerEntityId = custObj.pkid;

      this.rmaTicket.ownerEntityName = custObj.name;
      this.rmaTicket.ownerEntityEmail = custObj.email1;
      this.rmaTicket.ownerEntityTelephone = custObj.telephone1;
      this.rmaTicket.ownerEntityMobilePhone = custObj.mobilePhone;
      this.rmaTicket.ownerEntityFax = custObj.faxNo;
      this.rmaTicket.ownerEntityAdd1 = custObj.mainAddress1;
      this.rmaTicket.ownerEntityAdd2 = custObj.mainAddress2;
      this.rmaTicket.ownerEntityAdd3 = custObj.mainAddress3;
      this.rmaTicket.ownerEntityState = custObj.mainState;
      this.rmaTicket.ownerEntityCountry = custObj.mainCountry;
		AuditTrailObject atObj = new AuditTrailObject();
		atObj.userId = this.userId;
		atObj.auditType = AuditTrailBean.TYPE_TXN;
		atObj.time = TimeFormat.getTimestamp();
		atObj.remarks = "Edited SRV Note Customer: " +this.rmaTicket.pkid;
		AuditTrailNut.fnCreate(atObj);
   }

   public void setCustomer(CustUserObject contactObj)
   {
      this.rmaTicket.ownerEntityTable = CustUserBean.TABLENAME;
      this.rmaTicket.ownerEntityId = contactObj.pkid;

      this.rmaTicket.ownerEntityName = contactObj.getName();
      this.rmaTicket.ownerEntityEmail = contactObj.email1;
      this.rmaTicket.ownerEntityTelephone = contactObj.telephone1;
      this.rmaTicket.ownerEntityMobilePhone = contactObj.mobilePhone;
      this.rmaTicket.ownerEntityFax = contactObj.faxNo;
      this.rmaTicket.ownerEntityAdd1 = contactObj.mainAddress1;
      this.rmaTicket.ownerEntityAdd2 = contactObj.mainAddress2;
      this.rmaTicket.ownerEntityAdd3 = contactObj.mainAddress3;
      this.rmaTicket.ownerEntityState = contactObj.mainState;
      this.rmaTicket.ownerEntityCountry = contactObj.mainCountry;
		AuditTrailObject atObj = new AuditTrailObject();
		atObj.userId = this.userId;
		atObj.auditType = AuditTrailBean.TYPE_TXN;
		atObj.time = TimeFormat.getTimestamp();
		atObj.remarks = "Edited SRV Note Customer: " +this.rmaTicket.pkid;
		AuditTrailNut.fnCreate(atObj);
   }

   public void updateOwnerInfo()
   {
      if(this.rmaTicket.ownerEntityTable.equals(CustAccountBean.TABLENAME) && this.rmaTicket.ownerEntityId.intValue()>0)
      {
         CustAccountObject account = CustAccountNut.getObject(this.rmaTicket.ownerEntityId);
         setCustomer(account);
      }

      if(this.rmaTicket.ownerEntityTable.equals(CustUserBean.TABLENAME)  && this.rmaTicket.ownerEntityId.intValue()>0)
      {
         CustUserObject contact = CustUserNut.getObject(this.rmaTicket.ownerEntityId);
         setCustomer(contact);
      }
   }

	public void setSupplierDetails(Integer supplierEntityId, String supplierEntityName, String supplierEntityEmail,
			String supplierEntityTelephone, String supplierEntityMobilePhone, String supplierEntityFax,
			String supplierEntityAdd1, String supplierEntityAdd2, String supplierEntityAdd3,
			String supplierEntityState, String supplierEntityCountry)
	{
		this.rmaTicket.supplierEntityId = supplierEntityId;
		this.rmaTicket.supplierEntityName = supplierEntityName;
		this.rmaTicket.supplierEntityEmail = supplierEntityEmail;
		this.rmaTicket.supplierEntityTelephone = supplierEntityTelephone;
		this.rmaTicket.supplierEntityMobilePhone = supplierEntityMobilePhone;
		this.rmaTicket.supplierEntityFax = supplierEntityFax;
		this.rmaTicket.supplierEntityAdd1 = supplierEntityAdd1;
		this.rmaTicket.supplierEntityAdd2 = supplierEntityAdd2;
		this.rmaTicket.supplierEntityAdd3 = supplierEntityAdd3;
		this.rmaTicket.supplierEntityState = supplierEntityState;
		this.rmaTicket.supplierEntityCountry = supplierEntityCountry;

		
	}
	
	public void setTimeSupplier(Timestamp tsSupplierReturnTo, Timestamp tsSupplierReceiveFrom)
	{
		this.rmaTicket.timeSupplierReturnTo = tsSupplierReturnTo;
		this.rmaTicket.timeSupplierReceiveFrom = tsSupplierReceiveFrom;
	
	}

	public void setReplacementItemDetails(String rplItemCode, BigDecimal rplQuantity, String rplItemName,
			String rplItemSerial, String rplItemDescription, Timestamp tsSupplierReturn, Timestamp tsSupplierReceive)
	{
		
		ItemObject itmObj = ItemNut.getValueObjectByCode(rplItemCode);
		if (itmObj != null)
		{
			this.rmaTicket.rplItemCode = rplItemCode;
			this.rmaTicket.rplItemName = itmObj.name;
		} else
		{
			this.rmaTicket.rplItemCode = "";
			this.rmaTicket.rplItemName = rplItemName;
		}
		
		//this.rmaTicket.rplItemCode = rplItemCode;
		this.rmaTicket.rplQuantity = rplQuantity;
		//this.rmaTicket.rplItemName = rplItemName;
		this.rmaTicket.rplItemSerial = rplItemSerial;
		this.rmaTicket.rplItemDescription = rplItemDescription;
		this.rmaTicket.timeSupplierReturn = tsSupplierReturn;
		this.rmaTicket.timeSupplierReceive = tsSupplierReceive;
	}

	public boolean validOwnerDetails()
	{
		boolean valid = true;
		if (this.rmaTicket.ownerEntityName.length() < 4)
		{
			return false;
		}
		if (this.rmaTicket.ownerEntityTelephone.length() < 4 && this.rmaTicket.ownerEntityMobilePhone.length() < 4)
		{
			return false;
		}
		return valid;
	}

	public boolean validRMAItemDetails()
	{
		boolean valid = true;
		// this.rmaTicket.itemCode = "";
		if (this.rmaTicket.itemName.length() < 5)
		{
			return false;
		}
		if (this.rmaTicket.quantity.signum() <= 0)
		{
			return false;
		}
		if (this.rmaTicket.problemStmt.length() < 5)
		{
			return false;
		}
		if (this.rmaTicket.technician1Id.intValue() == 0)
		{
			return false;
		}
		return valid;
	}

	public synchronized void saveRMA()
	{
		RMATicket rmaTicketEJB = RMATicketNut.getHandle(this.rmaTicket.pkid);
		if (rmaTicketEJB != null)
		{
			try
			{
				rmaTicketEJB.setObject(this.rmaTicket);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
