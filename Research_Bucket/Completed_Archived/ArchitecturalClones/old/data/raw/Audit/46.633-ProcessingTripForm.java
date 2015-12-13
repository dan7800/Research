/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.distribution;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Vector;

import com.vlee.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.util.Log;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

/*---------------------------------------------------------
 BASIC RULES
 If Receipt Has Already Been Created
 1) CANNOT Change/edit the memberCard/User/CustAccount
 2) CANNOT remove Sales Order Item
 3) CAN add Sales Order Item
 4) Cannot Change Receipt Details
 5) Recheck the CRV to see if it is still valid
 
 If Receipt Has Not Been Created
 1) Can add/remove Sales Order Items
 2) Can set receipt/payment info
 3) Able to create receipt, and respective CRV if amount 
 matches the SalesOrder bill amount

 SalesOrder Cancellation
 1) Cancel the CRV if it has already been created
 2) Lead user to SalesReturn use-case if invoice has already been created
 3) Reset the state of the sales order items

 -----------------------------------------------------------*/
public class ProcessingTripForm extends java.lang.Object implements Serializable
{
	public SalesOrderIndexObject soObj;
	public String productionStatus;
	public Integer productionWorker;
	public BigDecimal prodPts;
	public BigDecimal creaPts;
	public Integer userId;
	public DeliveryTripObject tripObj;

	public ProcessingTripForm(Integer userId)
	{
		this.userId = userId;
		this.productionStatus = "";
		this.productionWorker = new Integer(0);
		this.prodPts = new BigDecimal(0);
		this.creaPts = new BigDecimal(0);
		this.tripObj = null;
	}

	public void setSalesOrder(Long orderPkid)
	{
		this.soObj = SalesOrderIndexNut.getObjectTree(orderPkid);
		if (soObj == null)
		{
			reset();
		} else
		{
			this.prodPts = new BigDecimal(0);
			if (this.soObj.vecItem.size() > 0)
			{
				Vector vecOrderRow = this.soObj.vecItem;
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) vecOrderRow.get(0);
				this.productionStatus = soItmObj.productionStatus;
				this.productionWorker = soItmObj.valueadd1Userid;
				this.creaPts = soItmObj.valueadd2Points;

				for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
				{
					SalesOrderItemObject soItmObj2 = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
					if(!soItmObj2.itemCode.equals(ItemBean.CODE_DELIVERY))
					{
						this.prodPts =this.prodPts.add(soItmObj2.valueadd1Points);
					}
				}

				if(this.prodPts.signum()==0)
				{
					this.prodPts = getInventoryItemAmount();
				}

			}
		}
	}

	public SalesOrderIndexObject getSalesOrder()
	{
		return this.soObj;
	}

	public void reset()
	{
		Log.printVerbose("Reset");
		this.soObj = null;
		this.productionStatus = "";
		this.productionWorker = new Integer(0);
		this.prodPts = new BigDecimal(0);
		this.creaPts = new BigDecimal(0);
	}

	public String getProductionStatus()
	{
		Log.printVerbose("Production Status" + this.productionStatus);
		return this.productionStatus;
	}

	public void setProductionStatus(String buf)
	{
		this.productionStatus = buf;
	}

	public Integer getProductionWorker()
	{
		Log.printVerbose("ProductionWorker" + this.productionWorker);
		return this.productionWorker;
	}

	public void setProductionWorker(Integer buf)
	{
		this.productionWorker = buf;
	}

	public BigDecimal getProductivePoints()
	{
		Log.printVerbose("ProductivePoints" + this.prodPts);
		return this.prodPts;
	}

	public BigDecimal getInventoryItemAmount()
	{
		BigDecimal totalAmt = new BigDecimal(0);
		for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
		{
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			if(!soItmObj.itemCode.equals(ItemBean.CODE_DELIVERY))
			{
				totalAmt = totalAmt.add(soItmObj.price1.multiply(soItmObj.quantity));
			}
		}
		return totalAmt;
	}

	public void setProductivePoints(BigDecimal buf)
	{
		if(buf==null){ return ;}
		this.prodPts = buf;
		for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
		{
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			soItmObj.valueadd1Points = new BigDecimal(0);
			try
			{
				if(!soItmObj.itemCode.equals(ItemBean.CODE_DELIVERY))
				{
					soItmObj.valueadd1Points = this.prodPts.divide(
								getInventoryItemAmount(),12,BigDecimal.ROUND_HALF_EVEN).multiply(
								soItmObj.price1.multiply(soItmObj.quantity));
				}
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}
	}

	public BigDecimal getCreativePoints()
	{
		Log.printVerbose("CreatePoints" + this.creaPts);
		return this.creaPts;
	}

	public void setCreativePoints(BigDecimal buf)
	{
		this.creaPts = buf;
	}

	public void setWorkshopDetails(String productionStatus, Integer iUserProcessing, BigDecimal bdProd,
			BigDecimal bdCrea, Integer userId)
	{
		String docInserted = "false";
		
		this.productionStatus = productionStatus;
		this.productionWorker = iUserProcessing;
		this.prodPts = bdProd;
		setProductivePoints(bdProd);
	
		setCreativePoints(bdCrea);

		Log.printVerbose("Just enter setWorkshopDetails");
		Log.printVerbose(productionStatus);
		Log.printVerbose(productionWorker.toString());
		Log.printVerbose(prodPts.toString());
		Log.printVerbose(creaPts.toString());
		// / update all SalesOrderItem
		for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
		{
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			
			try
			{				
				if(!soItmObj.valueadd1Userid.equals(this.productionWorker) && docInserted.equals("false"))
				{
					Integer oldFlorist = soItmObj.valueadd1Userid;
					UserObject floristOld = UserNut.getObject(oldFlorist);
					UserObject floristNew = UserNut.getObject(this.productionWorker);
					
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
					
					docInserted = "true";
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
			Log.printVerbose("for loop");
			Log.printVerbose(productionStatus);
			Log.printVerbose(productionWorker.toString());
			Log.printVerbose(prodPts.toString());
			Log.printVerbose(creaPts.toString());
			
			soItmObj.productionStatus = this.productionStatus;
			soItmObj.valueadd1Userid = this.productionWorker;
			soItmObj.valueadd2Userid = this.productionWorker;
//			soItmObj.valueadd1Points = soItmObj.price1;
			soItmObj.valueadd2Points = this.creaPts;
						
			SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
			if (soItmEJB != null)
			{
				try
				{
					soItmEJB.setObject(soItmObj);
				} 
				catch (Exception ex)
				{ ex.printStackTrace(); }
			}	
						
		} // end for
				
		Log.printVerbose("End of For");
	} // / end of function

	public void setTrip(Long tripPkid)
	{
		this.tripObj = DeliveryTripNut.getObject(tripPkid);
		if (tripObj != null)
		{
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				Vector vecTripLink = new Vector(DeliveryTripSOLinkNut.getObjectsBySalesOrderItem(soItmObj.pkid));
				for (int cnt2 = 0; cnt2 < vecTripLink.size(); cnt2++)
				{
					DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecTripLink.get(cnt2);
					try
					{
						DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkObj.pkid);
						tripLinkEJB.remove();
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}// / end for
				// // create the linking from SalesOrderItem to Trip
				DeliveryTripSOLinkObject tripLinkObj = new DeliveryTripSOLinkObject();
				// tripLinkObj.pkid = new Long(0);
				tripLinkObj.soItem = soItmObj.pkid;
				tripLinkObj.tripId = this.tripObj.pkid;
				// tripLinkObj.sequence = new Integer(0);
				tripLinkObj.timeExpectedStr = this.soObj.expDeliveryTime;
				// tripLinkObj.timeExpectedCollection =
				// TimeFormat.createTimestamp("0001-01-01");
				// tripLinkObj.timeExpectedJourney =
				// tripLinkObj.timeExpectedCollection;
				// tripLinkObj.timeExpectedArrival =
				tripLinkObj.timePlannedCollection = this.tripObj.timePlannedCollection;
				// tripLinkObj.timePlannedJourney =
				// tripLinkObj.timeExpectedCollection;
				tripLinkObj.timePlannedArrival = tripLinkObj.timePlannedCollection;
				// tripLinkObj.timeActualCollection =
				// tripLinkObj.timeExpectedCollection;;
				// tripLinkObj.timeActualJourney =
				// tripLinkObj.timeExpectedCollection; //10
				// tripLinkObj.timeActualArrival =
				// tripLinkObj.timeExpectedCollection;
				tripLinkObj.userTripDriver = tripObj.userTripDriver;
				tripLinkObj.userTripOrganizer = tripObj.userTripOrganizer;
				tripLinkObj.userTripCoordinator = tripObj.userTripCoordinator;
				// tripLinkObj.deliveryUserid = new Integer(0);
				try
				{
					GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripObj.userTripDriver);
					tripLinkObj.deliveryName = geiObj.name;
					tripLinkObj.deliveryPhone = geiObj.phoneMobile;
					tripLinkObj.deliveryCompany = geiObj.name;
				} catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}
				tripLinkObj.currency = this.soObj.currency;
				// tripLinkObj.deliveryBillReference = ""; //20
				tripLinkObj.deliveryBillAmount = new BigDecimal(0);
				tripLinkObj.deliveryBillOutstanding = tripLinkObj.deliveryBillAmount;
				tripLinkObj.remarks = soItmObj.remarks;
				tripLinkObj.instructions = soObj.deliveryPreferences;
				// tripLinkObj.complaints = "";
				// tripLinkObj.state = DeliveryTripSOLinkBean.STATE_CREATED;
				// tripLinkObj.status = DeliveryTripSOLinkBean.STATUS_ACTIVE;
				tripLinkObj.useridEdit = this.userId;
				// tripLinkObj.displayFormat = "";
				// tripLinkObj.docType = ""; //30
		String description = "";
		description += " TRIP-NO:"+tripLinkObj.tripId.toString();
		description += " DELIVERY-TIME:"+tripLinkObj.timeExpectedStr;
		description += " PLANNED-COLLECTION:"+tripLinkObj.timePlannedCollection.toString();
		description += " TRIP-ORGANIZER:"+UserNut.getUserName(tripObj.userTripOrganizer);
		description += " TRIP-COORDINATOR:"+UserNut.getUserName(tripObj.userTripOrganizer);
		description += " DRIVER:"+tripLinkObj.deliveryName;	
		description += " DRIVER-PHONE:"+tripLinkObj.deliveryPhone;	
				try
				{

					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
            	dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
            	dpiObj.processType = "UPDATE-ORDER";
            	dpiObj.category = "ORDER-ASSIGNED-TRIP";
            	dpiObj.auditLevel = new Integer(0);
            	dpiObj.processId = new Long(0);
            	dpiObj.userid = userId;
            	dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
            	dpiObj.docId = soObj.pkid;
            	dpiObj.entityRef = CustAccountBean.TABLENAME;
            	dpiObj.entityId = soObj.senderKey1;
            	dpiObj.description1 = description;
            	dpiObj.description2 = "";
            	dpiObj.remarks = "";
            	dpiObj.time = TimeFormat.getTimestamp();
            	DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);




					DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.fnCreate(tripLinkObj);
					if (tripLinkEJB == null)
					{
						return;
					}
					SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
					soItmObj.deliveryStatus = SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED;
					soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED);
					// / update the states of the SalesOrderItem ->
					// deliveryStatus
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}// // end for
		}// // end if
	} // // end of set trip
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
package com.vlee.bean.distribution;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Vector;

import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.util.Log;
import com.vlee.ejb.inventory.*;

/*---------------------------------------------------------
 BASIC RULES
 If Receipt Has Already Been Created
 1) CANNOT Change/edit the memberCard/User/CustAccount
 2) CANNOT remove Sales Order Item
 3) CAN add Sales Order Item
 4) Cannot Change Receipt Details
 5) Recheck the CRV to see if it is still valid
 
 If Receipt Has Not Been Created
 1) Can add/remove Sales Order Items
 2) Can set receipt/payment info
 3) Able to create receipt, and respective CRV if amount 
 matches the SalesOrder bill amount

 SalesOrder Cancellation
 1) Cancel the CRV if it has already been created
 2) Lead user to SalesReturn use-case if invoice has already been created
 3) Reset the state of the sales order items

 -----------------------------------------------------------*/
public class ProcessingTripForm extends java.lang.Object implements Serializable
{
	public SalesOrderIndexObject soObj;
	public String productionStatus;
	public Integer productionWorker;
	public BigDecimal prodPts;
	public BigDecimal creaPts;
	public Integer userId;
	public DeliveryTripObject tripObj;

	public ProcessingTripForm(Integer userId)
	{
		this.userId = userId;
		this.productionStatus = "";
		this.productionWorker = new Integer(0);
		this.prodPts = new BigDecimal(0);
		this.creaPts = new BigDecimal(0);
		this.tripObj = null;
	}

	public void setSalesOrder(Long orderPkid)
	{
		this.soObj = SalesOrderIndexNut.getObjectTree(orderPkid);
		if (soObj == null)
		{
			reset();
		} else
		{
			this.prodPts = new BigDecimal(0);
			if (this.soObj.vecItem.size() > 0)
			{
				Vector vecOrderRow = this.soObj.vecItem;
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) vecOrderRow.get(0);
				this.productionStatus = soItmObj.productionStatus;
				this.productionWorker = soItmObj.valueadd1Userid;
				this.creaPts = soItmObj.valueadd2Points;

				for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
				{
					SalesOrderItemObject soItmObj2 = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
					if(!soItmObj2.itemCode.equals(ItemBean.CODE_DELIVERY))
					{
						this.prodPts =this.prodPts.add(soItmObj2.valueadd1Points);
					}
				}

				if(this.prodPts.signum()==0)
				{
					this.prodPts = getInventoryItemAmount();
				}

			}
		}
	}

	public SalesOrderIndexObject getSalesOrder()
	{
		return this.soObj;
	}

	public void reset()
	{
		Log.printVerbose("Reset");
		this.soObj = null;
		this.productionStatus = "";
		this.productionWorker = new Integer(0);
		this.prodPts = new BigDecimal(0);
		this.creaPts = new BigDecimal(0);
	}

	public String getProductionStatus()
	{
		Log.printVerbose("Production Status" + this.productionStatus);
		return this.productionStatus;
	}

	public void setProductionStatus(String buf)
	{
		this.productionStatus = buf;
	}

	public Integer getProductionWorker()
	{
		Log.printVerbose("ProductionWorker" + this.productionWorker);
		return this.productionWorker;
	}

	public void setProductionWorker(Integer buf)
	{
		this.productionWorker = buf;
	}

	public BigDecimal getProductivePoints()
	{
		Log.printVerbose("ProductivePoints" + this.prodPts);
		return this.prodPts;
	}

	public BigDecimal getInventoryItemAmount()
	{
		BigDecimal totalAmt = new BigDecimal(0);
		for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
		{
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			if(!soItmObj.itemCode.equals(ItemBean.CODE_DELIVERY))
			{
				totalAmt = totalAmt.add(soItmObj.price1.multiply(soItmObj.quantity));
			}
		}
		return totalAmt;
	}

	public void setProductivePoints(BigDecimal buf)
	{
		if(buf==null){ return ;}
		this.prodPts = buf;
		for(int cnt1=0;cnt1<this.soObj.vecItem.size();cnt1++)
		{
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			soItmObj.valueadd1Points = new BigDecimal(0);
			try
			{
				if(!soItmObj.itemCode.equals(ItemBean.CODE_DELIVERY))
				{
					soItmObj.valueadd1Points = this.prodPts.divide(
								getInventoryItemAmount(),12,BigDecimal.ROUND_HALF_EVEN).multiply(
								soItmObj.price1.multiply(soItmObj.quantity));
				}
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}
	}

	public BigDecimal getCreativePoints()
	{
		Log.printVerbose("CreatePoints" + this.creaPts);
		return this.creaPts;
	}

	public void setCreativePoints(BigDecimal buf)
	{
		this.creaPts = buf;
	}

	public void setWorkshopDetails(String productionStatus, Integer iUserProcessing, BigDecimal bdProd,
			BigDecimal bdCrea)
	{
		this.productionStatus = productionStatus;
		this.productionWorker = iUserProcessing;
		this.prodPts = bdProd;
		setProductivePoints(bdProd);
	
		setCreativePoints(bdCrea);

		Log.printVerbose("Just enter setWorkshopDetails");
		Log.printVerbose(productionStatus);
		Log.printVerbose(productionWorker.toString());
		Log.printVerbose(prodPts.toString());
		Log.printVerbose(creaPts.toString());
		// / update all SalesOrderItem
		for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
		{
			Log.printVerbose("for loop");
			Log.printVerbose(productionStatus);
			Log.printVerbose(productionWorker.toString());
			Log.printVerbose(prodPts.toString());
			Log.printVerbose(creaPts.toString());
			SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
			soItmObj.productionStatus = this.productionStatus;
			soItmObj.valueadd1Userid = this.productionWorker;
			soItmObj.valueadd2Userid = this.productionWorker;
//			soItmObj.valueadd1Points = soItmObj.price1;
			soItmObj.valueadd2Points = this.creaPts;
						
			SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
			if (soItmEJB != null)
			{
				try
				{
					soItmEJB.setObject(soItmObj);
				} 
				catch (Exception ex)
				{ ex.printStackTrace(); }
			}
		} // end for
		Log.printVerbose("End of For");
	} // / end of function

	public void setTrip(Long tripPkid)
	{
		this.tripObj = DeliveryTripNut.getObject(tripPkid);
		if (tripObj != null)
		{
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				Vector vecTripLink = new Vector(DeliveryTripSOLinkNut.getObjectsBySalesOrderItem(soItmObj.pkid));
				for (int cnt2 = 0; cnt2 < vecTripLink.size(); cnt2++)
				{
					DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecTripLink.get(cnt2);
					try
					{
						DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkObj.pkid);
						tripLinkEJB.remove();
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}// / end for
				// // create the linking from SalesOrderItem to Trip
				DeliveryTripSOLinkObject tripLinkObj = new DeliveryTripSOLinkObject();
				// tripLinkObj.pkid = new Long(0);
				tripLinkObj.soItem = soItmObj.pkid;
				tripLinkObj.tripId = this.tripObj.pkid;
				// tripLinkObj.sequence = new Integer(0);
				tripLinkObj.timeExpectedStr = this.soObj.expDeliveryTime;
				// tripLinkObj.timeExpectedCollection =
				// TimeFormat.createTimestamp("0001-01-01");
				// tripLinkObj.timeExpectedJourney =
				// tripLinkObj.timeExpectedCollection;
				// tripLinkObj.timeExpectedArrival =
				tripLinkObj.timePlannedCollection = this.tripObj.timePlannedCollection;
				// tripLinkObj.timePlannedJourney =
				// tripLinkObj.timeExpectedCollection;
				tripLinkObj.timePlannedArrival = tripLinkObj.timePlannedCollection;
				// tripLinkObj.timeActualCollection =
				// tripLinkObj.timeExpectedCollection;;
				// tripLinkObj.timeActualJourney =
				// tripLinkObj.timeExpectedCollection; //10
				// tripLinkObj.timeActualArrival =
				// tripLinkObj.timeExpectedCollection;
				tripLinkObj.userTripDriver = tripObj.userTripDriver;
				tripLinkObj.userTripOrganizer = tripObj.userTripOrganizer;
				tripLinkObj.userTripCoordinator = tripObj.userTripCoordinator;
				// tripLinkObj.deliveryUserid = new Integer(0);
				try
				{
					GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripObj.userTripDriver);
					tripLinkObj.deliveryName = geiObj.name;
					tripLinkObj.deliveryPhone = geiObj.phoneMobile;
					tripLinkObj.deliveryCompany = geiObj.name;
				} catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}
				tripLinkObj.currency = this.soObj.currency;
				// tripLinkObj.deliveryBillReference = ""; //20
				tripLinkObj.deliveryBillAmount = new BigDecimal(0);
				tripLinkObj.deliveryBillOutstanding = tripLinkObj.deliveryBillAmount;
				tripLinkObj.remarks = soItmObj.remarks;
				tripLinkObj.instructions = soObj.deliveryPreferences;
				// tripLinkObj.complaints = "";
				// tripLinkObj.state = DeliveryTripSOLinkBean.STATE_CREATED;
				// tripLinkObj.status = DeliveryTripSOLinkBean.STATUS_ACTIVE;
				tripLinkObj.useridEdit = this.userId;
				// tripLinkObj.displayFormat = "";
				// tripLinkObj.docType = ""; //30
		String description = "";
		description += " TRIP-NO:"+tripLinkObj.tripId.toString();
		description += " DELIVERY-TIME:"+tripLinkObj.timeExpectedStr;
		description += " PLANNED-COLLECTION:"+tripLinkObj.timePlannedCollection.toString();
		description += " TRIP-ORGANIZER:"+UserNut.getUserName(tripObj.userTripOrganizer);
		description += " TRIP-COORDINATOR:"+UserNut.getUserName(tripObj.userTripOrganizer);
		description += " DRIVER:"+tripLinkObj.deliveryName;	
		description += " DRIVER-PHONE:"+tripLinkObj.deliveryPhone;	
				try
				{

					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
            	dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
            	dpiObj.processType = "UPDATE-ORDER";
            	dpiObj.category = "ORDER-ASSIGNED-TRIP";
            	dpiObj.auditLevel = new Integer(0);
            	dpiObj.processId = new Long(0);
            	dpiObj.userid = userId;
            	dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
            	dpiObj.docId = soObj.pkid;
            	dpiObj.entityRef = CustAccountBean.TABLENAME;
            	dpiObj.entityId = soObj.senderKey1;
            	dpiObj.description1 = description;
            	dpiObj.description2 = "";
            	dpiObj.remarks = "";
            	dpiObj.time = TimeFormat.getTimestamp();
            	DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);




					DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.fnCreate(tripLinkObj);
					if (tripLinkEJB == null)
					{
						return;
					}
					SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
					soItmObj.deliveryStatus = SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED;
					soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED);
					// soItmObj = SalesOrderItemNut.getObject(orderItemPkid);
					// / update the states of the SalesOrderItem ->
					// deliveryStatus
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}// // end for
		}// // end if
	} // // end of set trip
}


