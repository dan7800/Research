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

public class EditDeliveryTripForm extends java.lang.Object implements Serializable
{
	DeliveryTripObject tripObj = null;
	Integer userId = null;
	TreeMap treeOrder = null;

	public EditDeliveryTripForm(Long tripId, Integer userId) throws Exception
	{
		// / Load the trip object
		this.tripObj = DeliveryTripNut.getObject(tripId);
		if (tripObj == null)
		{
			throw new Exception("Invalid Trip PKID!");
		}
		this.userId = userId;
		this.treeOrder = new TreeMap();
		// // load the TripSOLInk and populate the treeOrder Object
		loadDeliveryItem();
	}

	public DeliveryTripObject getTrip()
	{
		return this.tripObj;
	}

	public void setTripDetails(Timestamp tsPlannedCollection, Integer tripOrganizer, Integer tripCoordinator,
			Integer tripDriver,Integer tripHelper, String remarks) throws Exception
	{
		DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
		{
			this.tripObj = tripEJB.getObject();
		}
		Log.printVerbose(" formobj... time = " + tsPlannedCollection.toString());
		this.tripObj.timePlannedCollection = tsPlannedCollection;
		this.tripObj.userTripOrganizer = tripOrganizer;
		this.tripObj.userTripCoordinator = tripCoordinator;
		this.tripObj.userTripDriver = tripDriver;
		this.tripObj.userTripHelper = tripHelper;
		GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripDriver);
		this.tripObj.deliveryName = geiObj.name;
		this.tripObj.deliveryPhone = geiObj.phoneMobile;
		this.tripObj.remarks = remarks;
		
		
		tripEJB.setObject(this.tripObj);
		// // todo: update the remaining trip-so-link objects too
	}

	public void setDispatchDetails(Integer dispatchController, String dispatchLocation, String dispatchCTL, String dispatchVehicleLoc,
														String dispatchDescription, Timestamp dispatchTime, String tripCheckinTime,
														String tripCheckoutTime, String statusQc)
		throws Exception
	{
      DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
      {
         this.tripObj = tripEJB.getObject();
      }
      this.tripObj.dispatchController = dispatchController;
      this.tripObj.statusQc = statusQc;
      this.tripObj.dispatchLocation = dispatchLocation;
      this.tripObj.locationCompletedOrder = dispatchCTL;
      this.tripObj.locationVehicle = dispatchVehicleLoc;
      this.tripObj.dispatchDescription = dispatchDescription;
		this.tripObj.dispatchTime = dispatchTime;
		this.tripObj.tripCheckinTime = tripCheckinTime;
		this.tripObj.tripCheckoutTime = tripCheckoutTime;
      tripEJB.setObject(this.tripObj);
	}


	public synchronized void addLink(Long orderItemPkid, Timestamp tsPlannedArrival, BigDecimal deliveryCharges)
			throws Exception
	{
		String description = "";
		// / *) check if item has been delivered
		// / *) check if item requires delivery
		// / *) check if TripSoLink exists
		SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(orderItemPkid);
		if (soItemObj == null)
		{
			throw new Exception("The Sales Order Item does not exist!");
		}
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soItemObj.indexId);
		DeliveryTripSOLinkObject tripLinkObj = new DeliveryTripSOLinkObject();
		// tripLinkObj.pkid = new Long(0);
		tripLinkObj.soItem = soItemObj.pkid;
		tripLinkObj.tripId = this.tripObj.pkid;
		// tripLinkObj.sequence = new Integer(0);
		tripLinkObj.timeExpectedStr = soObj.expDeliveryTime;
		// tripLinkObj.timeExpectedCollection =
		// TimeFormat.createTimestamp("0001-01-01");
		// tripLinkObj.timeExpectedJourney = tripLinkObj.timeExpectedCollection;
		// tripLinkObj.timeExpectedArrival =
		tripLinkObj.timePlannedCollection = this.tripObj.timePlannedCollection;
		// tripLinkObj.timePlannedJourney = tripLinkObj.timeExpectedCollection;
		tripLinkObj.timePlannedArrival = tsPlannedArrival;
		// tripLinkObj.timeActualCollection =
		// tripLinkObj.timeExpectedCollection;;
		// tripLinkObj.timeActualJourney = tripLinkObj.timeExpectedCollection;
		// //10
		// tripLinkObj.timeActualArrival = tripLinkObj.timeExpectedCollection;
		tripLinkObj.userTripDriver = tripObj.userTripDriver;
		tripLinkObj.userTripOrganizer = tripObj.userTripOrganizer;


		tripLinkObj.userTripCoordinator = tripObj.userTripCoordinator;
		// tripLinkObj.deliveryUserid = new Integer(0);
		description += " TRIP-NO:"+tripLinkObj.tripId.toString() + "<br>";
		description += " DELIVERY-TIME:"+tripLinkObj.timeExpectedStr + "<br>";
		description += " PLANNED-COLLECTION:"+tripLinkObj.timePlannedCollection.toString() + "<br>";
		description += " TRIP-ORGANIZER:"+UserNut.getUserName(tripObj.userTripOrganizer) + "<br>";
		description += " TRIP-COORDINATOR:"+UserNut.getUserName(tripObj.userTripOrganizer);
		try
		{
			GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripObj.userTripDriver);
			tripLinkObj.deliveryName = geiObj.name;
			tripLinkObj.deliveryPhone = geiObj.phoneMobile;
			tripLinkObj.deliveryCompany = geiObj.name;

			description += " <br> DELIVERY-MAN:"+tripLinkObj.deliveryName;	
			description += " DELIVERY-MAN-PHONE:"+tripLinkObj.deliveryPhone;	
		} catch (Exception ex)
		{
			throw new Exception("Invalid Driver!!");
		}
		tripLinkObj.currency = soObj.currency;
		// tripLinkObj.deliveryBillReference = ""; //20
		tripLinkObj.deliveryBillAmount = deliveryCharges;
		tripLinkObj.deliveryBillOutstanding = tripLinkObj.deliveryBillAmount;
		tripLinkObj.remarks = soItemObj.remarks;
		tripLinkObj.instructions = soObj.deliveryPreferences;
		// tripLinkObj.complaints = "";
		// tripLinkObj.state = DeliveryTripSOLinkBean.STATE_CREATED;
		// tripLinkObj.status = DeliveryTripSOLinkBean.STATUS_ACTIVE;
		tripLinkObj.useridEdit = this.userId;
		// tripLinkObj.displayFormat = "";
		// tripLinkObj.docType = ""; //30
		try
		{
			DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.fnCreate(tripLinkObj);
			if (tripLinkEJB == null)
			{
				throw new Exception("An internal error has occurred, unable to "
						+ " include the sales order item in this trip!");
			}
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(orderItemPkid);
			soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED);


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

			addToOrderTree(tripLinkObj);
			// / update the states of the SalesOrderItem -> deliveryStatus
//			soItemObj = SalesOrderItemNut.getObject(orderItemPkid);
//			PerOrder perOrder = new PerOrder(soObj);
//			perOrder.addTripLink(tripLinkObj, soItemObj);
//			this.treeOrder.put(perOrder.getKey(), perOrder);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		fnResetTripStatus();
	}

	public TreeMap getTreeOrder()
	{
		return this.treeOrder;
	}

	public void removeTripLink(Long tripLinkPkid)
	{
		try
		{
			DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkPkid);
			DeliveryTripSOLinkObject tripLinkObj = tripLinkEJB.getObject();
			if (tripLinkEJB != null)
			{
				tripLinkEJB.remove();
			} else
			{
				return;
			}
			SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(tripLinkObj.soItem);
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(tripLinkObj.soItem);
			if(soItemObj!=null){ soItemObj.deliveryStatus = SalesOrderItemBean.DELIVERY_STATUS_NONE;}
			if(soItemEJB!=null){soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_NONE);}
			Vector vecOrder = new Vector(this.treeOrder.values());
			for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
			{
				PerOrder perOrder = (PerOrder) vecOrder.get(cnt1);
				perOrder.removeTripLink(tripLinkPkid);
				if (perOrder.vecDeliveryItem.size() == 0)
				{
					this.treeOrder.remove(perOrder.getKey());
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void removeOrderFromTrip(Long orderNo)
	{

		Vector vecOrder = new Vector(this.treeOrder.values());
      for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
      {
      	PerOrder perOrder = (PerOrder) vecOrder.get(cnt1);
			if(perOrder.salesOrder.pkid.equals(orderNo))
			{
				for(int cnt2=0;cnt2<perOrder.vecDeliveryItem.size();cnt2++)
				{
					try
					{
						PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
						removeTripLink(perItem.tripItemLink.pkid);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}

				}
         }
      }
		return;
	}

	public void setAllOrderDeliveryStatus(String deliveryStatus)
	{
      Vector vecOrder = new Vector(this.treeOrder.values());
      for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
      {
         PerOrder perOrder = (PerOrder) vecOrder.get(cnt1);
//         if(perOrder.salesOrder.pkid.equals(orderNo))
         {
            for(int cnt2=0;cnt2<perOrder.vecDeliveryItem.size();cnt2++)
            {
               try
               {
            	  System.out.println("Check point 1");
            	  
                  PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
                  setItemDeliveryStatus(perItem.tripItemLink.pkid, perItem.orderItem.pkid,deliveryStatus);
               }
               catch(Exception ex)
               {
                  ex.printStackTrace();
               }

            }
         }
      }
      return;
	}

	public void setItemDeliveryStatus(Long tripLinkPkid, Long soItemPkid, String deliveryStatus) throws Exception
	{
		DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkPkid);
		SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
		
		if (tripLinkEJB == null || soItemEJB == null)
		{
			throw new Exception("Invalid Object!");
		}
		try
		{
			System.out.println("Check point 2");
			
			tripLinkEJB.setDeliveryStatus(deliveryStatus);
			soItemEJB.setDeliveryStatus(deliveryStatus);
			Vector vecTreeOrder = new Vector(this.treeOrder.values());
			if (!deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_NONE))
			{
				for (int cnt1 = 0; cnt1 < vecTreeOrder.size(); cnt1++)
				{
					PerOrder perOrder = (PerOrder) vecTreeOrder.get(cnt1);
					/// update remarks1 so that e-commerce shoppers will see latest delivery status.
					{
						try
						{
							if(deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_CONFIRMED_DELIVERED))
							{
								SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(perOrder.salesOrder.pkid);
								SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(perOrder.salesOrder.pkid);	
								soObj.remarks1 = "Delivered";
								soEJB.setObject(soObj);	
							}		
							else
							{
								SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(perOrder.salesOrder.pkid);
								SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(perOrder.salesOrder.pkid);	
								soObj.remarks1 = "Being processed";
								soEJB.setObject(soObj);	
							}
						}
						catch(Exception ex)
						{
							ex.printStackTrace();		
						}
					}

					for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
					{
						PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
						if (tripLinkPkid.longValue() == perItem.tripItemLink.pkid.longValue())
						{
							perItem.tripItemLink.state = deliveryStatus;
						}

						if (soItemPkid.longValue() == perItem.orderItem.pkid.longValue())
						{
							perItem.orderItem.deliveryStatus = deliveryStatus;
						}
					}
				}
			} else
			{
				try
				{
					tripLinkEJB.remove();
				} catch (Exception ex)
				{
					ex.printStackTrace();
					throw ex;
				}
				for (int cnt1 = 0; cnt1 < vecTreeOrder.size(); cnt1++)
				{
					PerOrder perOrder = (PerOrder) vecTreeOrder.get(cnt1);
					for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
					{
						PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
						if (soItemPkid.longValue() == perItem.orderItem.pkid.longValue())
						{
							perItem.orderItem.deliveryStatus = deliveryStatus;
						}
						if (perItem.tripItemLink.pkid.longValue() == tripLinkPkid.longValue())
						{
							perItem.tripItemLink.state = deliveryStatus;
							perOrder.vecDeliveryItem.remove(cnt1);
							cnt1--;
						}
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		fnResetTripStatus();
	}

	public synchronized void editLink() throws Exception
	{
		// /
	}

	public synchronized void loadDeliveryItem()
	{
		this.treeOrder.clear();
		QueryObject query = new QueryObject(new String[] { DeliveryTripSOLinkBean.TRIP_ID + " ='"
				+ this.tripObj.pkid.toString() + "' " });
		query.setOrder(" ORDER BY " + DeliveryTripSOLinkBean.TIME_PLANNED_ARRIVAL + ", "
				+ DeliveryTripSOLinkBean.SEQUENCE + ", " + DeliveryTripSOLinkBean.SO_ITEM);
		Vector vecDeliveryLink = new Vector(DeliveryTripSOLinkNut.getObjects(query));
		Log.printVerbose(" N DELIVERY LINK = " + vecDeliveryLink.size());
		for (int cnt1 = 0; cnt1 < vecDeliveryLink.size(); cnt1++)
		{
			DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecDeliveryLink.get(cnt1);
			Log.printVerbose(" TRIP LINK PKID:" + tripLinkObj.pkid.toString());
			addToOrderTree(tripLinkObj);
		}
		fnResetTripStatus();
	}

	private void addToOrderTree(DeliveryTripSOLinkObject tripLinkObj)
	{
		// // Load all relevant objects
		SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(tripLinkObj.soItem);
		if(soItemObj==null)
		{
			DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkObj.pkid);			 
			try
			{ tripLinkEJB.remove(); }
			catch(Exception ex)
			{ ex.printStackTrace();}	

			fnResetTripStatus();
			return ;
		}
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soItemObj.indexId);
		Vector vecOrderTree = new Vector(this.treeOrder.values());
		boolean addedToTree = false;
		Log.printVerbose(" CHECK POINT 1...");
		Log.printVerbose(" TRIP LINK PKID ..." + tripLinkObj.pkid.toString());
		for (int cnt1 = 0; cnt1 < vecOrderTree.size(); cnt1++)
		{
			Log.printVerbose(" CHECK POINT 2...");
			PerOrder perOrder = (PerOrder) vecOrderTree.get(cnt1);
			if (soItemObj.indexId.longValue() == perOrder.salesOrder.pkid.longValue())
			{
				perOrder.addTripLink(tripLinkObj, soItemObj);
				addedToTree = true;
				break;
			}
		}
		// / does not exist in tree order, create a new one
		if (!addedToTree)
		{
			try
			{
				PerOrder perOrder = new PerOrder(soObj);
				perOrder.addTripLink(tripLinkObj, soItemObj);
				this.treeOrder.put(perOrder.getKey(), perOrder);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		fnResetTripStatus();
	}

	public synchronized void removeLink(Long linkPkid)
	{
		// / check if item has been delivered
		// / check other potential conflict
	}

	private void fnResetTripStatus()
	{
		int completedItem = 0;
		int totalItem = 0;
		Vector vecOrders = new Vector(this.treeOrder.values());
		
		System.out.println("Check point 3");
		
		for (int cnt1 = 0; cnt1 < vecOrders.size(); cnt1++)
		{
			PerOrder perOrder = (PerOrder) vecOrders.get(cnt1);
			for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
			{
				PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
				totalItem++;
				if (perItem.orderItem.deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_CONFIRMED_DELIVERED))
				{
					completedItem++;
				}
			}
		}
		
		System.out.println("Check point 4");

		
		try
		{
			DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
			if (completedItem == 0)
			{
				this.tripObj.state = DeliveryTripBean.STATE_CREATED;				
				tripEJB.setState(DeliveryTripBean.STATE_CREATED);		

			} else if (completedItem < totalItem)
			{
				this.tripObj.state = DeliveryTripBean.STATE_DELIVERY_PARTIAL;
				tripEJB.setState(DeliveryTripBean.STATE_DELIVERY_PARTIAL);
	
				
			} else if (completedItem == totalItem)
			{
				this.tripObj.state = DeliveryTripBean.STATE_DELIVERY_COMPLETED;
				tripEJB.setState(DeliveryTripBean.STATE_DELIVERY_COMPLETED);

			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	// ////////////////////////////////////////////////////////
	// // INNER CLASS
	public static class PerOrder
	{
		public SalesOrderIndexObject salesOrder;
		public Vector vecDeliveryItem;
		public String uuid = "";

		public PerOrder(SalesOrderIndexObject soObj) throws Exception
		{
			this.salesOrder = soObj;
			this.vecDeliveryItem = new Vector();
			GUIDGenerator guidGen = new GUIDGenerator();
			this.uuid = guidGen.getUUID();
		}

		public String getKey()
		{
			return this.uuid;
		}

		public void addTripLink(DeliveryTripSOLinkObject tripItemLink, SalesOrderItemObject orderItem)
		{
			PerItem perItem = new PerItem(tripItemLink, orderItem);
			this.vecDeliveryItem.add(perItem);
		}

		public void removeTripLink(Long tripLinkPkid)
		{
			for (int cnt1 = 0; cnt1 < this.vecDeliveryItem.size(); cnt1++)
			{
				PerItem perItem = (PerItem) this.vecDeliveryItem.get(cnt1);
				if (perItem.tripItemLink.pkid.equals(tripLinkPkid))
				{
					this.vecDeliveryItem.remove(cnt1);
					cnt1--;
				}
			}
		}
	
		public void removeTripLinkByOrder(Long tripPkid)
		{
         for (int cnt1 = 0; cnt1 < this.vecDeliveryItem.size(); cnt1++)
         {
            PerItem perItem = (PerItem) this.vecDeliveryItem.get(cnt1);
            if (perItem.tripItemLink.tripId.equals(tripPkid))
            {
               this.vecDeliveryItem.remove(cnt1);
               cnt1--;
            }
         }
		}	

		public static class PerItem
		{
			public DeliveryTripSOLinkObject tripItemLink;
			public SalesOrderItemObject orderItem;

			public PerItem(DeliveryTripSOLinkObject tripItemLink, SalesOrderItemObject orderItem)
			{
				this.tripItemLink = tripItemLink;
				this.orderItem = orderItem;
			}
		}
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
package com.vlee.bean.distribution;

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

public class EditDeliveryTripForm extends java.lang.Object implements Serializable
{
	DeliveryTripObject tripObj = null;
	Integer userId = null;
	TreeMap treeOrder = null;

	public EditDeliveryTripForm(Long tripId, Integer userId) throws Exception
	{
		// / Load the trip object
		this.tripObj = DeliveryTripNut.getObject(tripId);
		if (tripObj == null)
		{
			throw new Exception("Invalid Trip PKID!");
		}
		this.userId = userId;
		this.treeOrder = new TreeMap();
		// // load the TripSOLInk and populate the treeOrder Object
		loadDeliveryItem();
	}

	public DeliveryTripObject getTrip()
	{
		return this.tripObj;
	}

	public void setTripDetails(Timestamp tsPlannedCollection, Integer tripOrganizer, Integer tripCoordinator,
			Integer tripDriver, String remarks) throws Exception
	{
		DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
		{
			this.tripObj = tripEJB.getObject();
		}
		Log.printVerbose(" formobj... time = " + tsPlannedCollection.toString());
		this.tripObj.timePlannedCollection = tsPlannedCollection;
		this.tripObj.userTripOrganizer = tripOrganizer;
		this.tripObj.userTripCoordinator = tripCoordinator;
		this.tripObj.userTripDriver = tripDriver;
		GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripDriver);
		this.tripObj.deliveryName = geiObj.name;
		this.tripObj.deliveryPhone = geiObj.phoneMobile;
		this.tripObj.remarks = remarks;
		tripEJB.setObject(this.tripObj);
		// // todo: update the remaining trip-so-link objects too
	}

	public void setDispatchDetails(Integer dispatchController, String dispatchLocation, String dispatchRemarks,
														String dispatchDescription, Timestamp dispatchTime)
		throws Exception
	{
      DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
      {
         this.tripObj = tripEJB.getObject();
      }
      this.tripObj.dispatchController = dispatchController;
      this.tripObj.dispatchLocation = dispatchLocation;
      this.tripObj.dispatchRemarks = dispatchRemarks;
      this.tripObj.dispatchDescription = dispatchDescription;
		this.tripObj.dispatchTime = dispatchTime;
      tripEJB.setObject(this.tripObj);
	}


	public synchronized void addLink(Long orderItemPkid, Timestamp tsPlannedArrival, BigDecimal deliveryCharges)
			throws Exception
	{
		String description = "";
		// / *) check if item has been delivered
		// / *) check if item requires delivery
		// / *) check if TripSoLink exists
		SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(orderItemPkid);
		if (soItemObj == null)
		{
			throw new Exception("The Sales Order Item does not exist!");
		}
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soItemObj.indexId);
		DeliveryTripSOLinkObject tripLinkObj = new DeliveryTripSOLinkObject();
		// tripLinkObj.pkid = new Long(0);
		tripLinkObj.soItem = soItemObj.pkid;
		tripLinkObj.tripId = this.tripObj.pkid;
		// tripLinkObj.sequence = new Integer(0);
		tripLinkObj.timeExpectedStr = soObj.expDeliveryTime;
		// tripLinkObj.timeExpectedCollection =
		// TimeFormat.createTimestamp("0001-01-01");
		// tripLinkObj.timeExpectedJourney = tripLinkObj.timeExpectedCollection;
		// tripLinkObj.timeExpectedArrival =
		tripLinkObj.timePlannedCollection = this.tripObj.timePlannedCollection;
		// tripLinkObj.timePlannedJourney = tripLinkObj.timeExpectedCollection;
		tripLinkObj.timePlannedArrival = tsPlannedArrival;
		// tripLinkObj.timeActualCollection =
		// tripLinkObj.timeExpectedCollection;;
		// tripLinkObj.timeActualJourney = tripLinkObj.timeExpectedCollection;
		// //10
		// tripLinkObj.timeActualArrival = tripLinkObj.timeExpectedCollection;
		tripLinkObj.userTripDriver = tripObj.userTripDriver;
		tripLinkObj.userTripOrganizer = tripObj.userTripOrganizer;


		tripLinkObj.userTripCoordinator = tripObj.userTripCoordinator;
		// tripLinkObj.deliveryUserid = new Integer(0);
		description += " TRIP-NO:"+tripLinkObj.tripId.toString();
		description += " DELIVERY-TIME:"+tripLinkObj.timeExpectedStr;
		description += " PLANNED-COLLECTION:"+tripLinkObj.timePlannedCollection.toString();
		description += " TRIP-ORGANIZER:"+UserNut.getUserName(tripObj.userTripOrganizer);
		description += " TRIP-COORDINATOR:"+UserNut.getUserName(tripObj.userTripOrganizer);
		try
		{
			GeneralEntityIndexObject geiObj = GeneralEntityIndexNut.getObject(tripObj.userTripDriver);
			tripLinkObj.deliveryName = geiObj.name;
			tripLinkObj.deliveryPhone = geiObj.phoneMobile;
			tripLinkObj.deliveryCompany = geiObj.name;

			description += " DRIVER:"+tripLinkObj.deliveryName;	
			description += " DRIVER-PHONE:"+tripLinkObj.deliveryPhone;	
		} catch (Exception ex)
		{
			throw new Exception("Invalid Driver!!");
		}
		tripLinkObj.currency = soObj.currency;
		// tripLinkObj.deliveryBillReference = ""; //20
		tripLinkObj.deliveryBillAmount = deliveryCharges;
		tripLinkObj.deliveryBillOutstanding = tripLinkObj.deliveryBillAmount;
		tripLinkObj.remarks = soItemObj.remarks;
		tripLinkObj.instructions = soObj.deliveryPreferences;
		// tripLinkObj.complaints = "";
		// tripLinkObj.state = DeliveryTripSOLinkBean.STATE_CREATED;
		// tripLinkObj.status = DeliveryTripSOLinkBean.STATUS_ACTIVE;
		tripLinkObj.useridEdit = this.userId;
		// tripLinkObj.displayFormat = "";
		// tripLinkObj.docType = ""; //30
		try
		{
			DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.fnCreate(tripLinkObj);
			if (tripLinkEJB == null)
			{
				throw new Exception("An internal error has occurred, unable to "
						+ " include the sales order item in this trip!");
			}
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(orderItemPkid);
			soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_SCHEDULED);


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


			addToOrderTree(tripLinkObj);
			// / update the states of the SalesOrderItem -> deliveryStatus
//			soItemObj = SalesOrderItemNut.getObject(orderItemPkid);
//			PerOrder perOrder = new PerOrder(soObj);
//			perOrder.addTripLink(tripLinkObj, soItemObj);
//			this.treeOrder.put(perOrder.getKey(), perOrder);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		fnResetTripStatus();
	}

	public TreeMap getTreeOrder()
	{
		return this.treeOrder;
	}

	public void removeTripLink(Long tripLinkPkid)
	{
		try
		{
			DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkPkid);
			DeliveryTripSOLinkObject tripLinkObj = tripLinkEJB.getObject();
			if (tripLinkEJB != null)
			{
				tripLinkEJB.remove();
			} else
			{
				return;
			}
			SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(tripLinkObj.soItem);
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(tripLinkObj.soItem);
			soItemObj.deliveryStatus = SalesOrderItemBean.DELIVERY_STATUS_NONE;
			soItemEJB.setDeliveryStatus(SalesOrderItemBean.DELIVERY_STATUS_NONE);
			Vector vecOrder = new Vector(this.treeOrder.values());
			for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
			{
				PerOrder perOrder = (PerOrder) vecOrder.get(cnt1);
				perOrder.removeTripLink(tripLinkPkid);
				if (perOrder.vecDeliveryItem.size() == 0)
				{
					this.treeOrder.remove(perOrder.getKey());
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setItemDeliveryStatus(Long tripLinkPkid, Long soItemPkid, String deliveryStatus) throws Exception
	{
		DeliveryTripSOLink tripLinkEJB = DeliveryTripSOLinkNut.getHandle(tripLinkPkid);
		SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
		if (tripLinkEJB == null || soItemEJB == null)
		{
			throw new Exception("Invalid Object!");
		}
		try
		{
			tripLinkEJB.setDeliveryStatus(deliveryStatus);
			soItemEJB.setDeliveryStatus(deliveryStatus);
			Vector vecTreeOrder = new Vector(this.treeOrder.values());
			if (!deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_NONE))
			{
				for (int cnt1 = 0; cnt1 < vecTreeOrder.size(); cnt1++)
				{
					PerOrder perOrder = (PerOrder) vecTreeOrder.get(cnt1);
					for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
					{
						PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
						if (tripLinkPkid.longValue() == perItem.tripItemLink.pkid.longValue())
						{
							perItem.tripItemLink.state = deliveryStatus;
						}
						if (soItemPkid.longValue() == perItem.orderItem.pkid.longValue())
						{
							perItem.orderItem.deliveryStatus = deliveryStatus;
						}
					}
				}
			} else
			{
				try
				{
					tripLinkEJB.remove();
				} catch (Exception ex)
				{
					ex.printStackTrace();
					throw ex;
				}
				for (int cnt1 = 0; cnt1 < vecTreeOrder.size(); cnt1++)
				{
					PerOrder perOrder = (PerOrder) vecTreeOrder.get(cnt1);
					for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
					{
						PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
						if (soItemPkid.longValue() == perItem.orderItem.pkid.longValue())
						{
							perItem.orderItem.deliveryStatus = deliveryStatus;
						}
						if (perItem.tripItemLink.pkid.longValue() == tripLinkPkid.longValue())
						{
							perItem.tripItemLink.state = deliveryStatus;
							perOrder.vecDeliveryItem.remove(cnt1);
							cnt1--;
						}
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		fnResetTripStatus();
	}

	public synchronized void editLink() throws Exception
	{
		// /
	}

	public synchronized void loadDeliveryItem()
	{
		this.treeOrder.clear();
		QueryObject query = new QueryObject(new String[] { DeliveryTripSOLinkBean.TRIP_ID + " ='"
				+ this.tripObj.pkid.toString() + "' " });
		query.setOrder(" ORDER BY " + DeliveryTripSOLinkBean.TIME_PLANNED_ARRIVAL + ", "
				+ DeliveryTripSOLinkBean.SEQUENCE + ", " + DeliveryTripSOLinkBean.SO_ITEM);
		Vector vecDeliveryLink = new Vector(DeliveryTripSOLinkNut.getObjects(query));
		Log.printVerbose(" N DELIVERY LINK = " + vecDeliveryLink.size());
		for (int cnt1 = 0; cnt1 < vecDeliveryLink.size(); cnt1++)
		{
			DeliveryTripSOLinkObject tripLinkObj = (DeliveryTripSOLinkObject) vecDeliveryLink.get(cnt1);
			Log.printVerbose(" TRIP LINK PKID:" + tripLinkObj.pkid.toString());
			addToOrderTree(tripLinkObj);
		}
		fnResetTripStatus();
	}

	private void addToOrderTree(DeliveryTripSOLinkObject tripLinkObj)
	{
		// // Load all relevant objects
		SalesOrderItemObject soItemObj = SalesOrderItemNut.getObject(tripLinkObj.soItem);
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soItemObj.indexId);
		Vector vecOrderTree = new Vector(this.treeOrder.values());
		boolean addedToTree = false;
		Log.printVerbose(" CHECK POINT 1...");
		Log.printVerbose(" TRIP PKID ..." + tripLinkObj.pkid.toString());
		for (int cnt1 = 0; cnt1 < vecOrderTree.size(); cnt1++)
		{
			Log.printVerbose(" CHECK POINT 2...");
			PerOrder perOrder = (PerOrder) vecOrderTree.get(cnt1);
			if (soItemObj.indexId.longValue() == perOrder.salesOrder.pkid.longValue())
			{
				perOrder.addTripLink(tripLinkObj, soItemObj);
				addedToTree = true;
				break;
			}
		}
		// / does not exist in tree order, create a new one
		if (!addedToTree)
		{
			try
			{
				PerOrder perOrder = new PerOrder(soObj);
				perOrder.addTripLink(tripLinkObj, soItemObj);
				this.treeOrder.put(perOrder.getKey(), perOrder);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		fnResetTripStatus();
	}

	public synchronized void removeLink(Long linkPkid)
	{
		// / check if item has been delivered
		// / check other potential conflict
	}

	private void fnResetTripStatus()
	{
		int completedItem = 0;
		int totalItem = 0;
		Vector vecOrders = new Vector(this.treeOrder.values());
		for (int cnt1 = 0; cnt1 < vecOrders.size(); cnt1++)
		{
			PerOrder perOrder = (PerOrder) vecOrders.get(cnt1);
			for (int cnt2 = 0; cnt2 < perOrder.vecDeliveryItem.size(); cnt2++)
			{
				PerOrder.PerItem perItem = (PerOrder.PerItem) perOrder.vecDeliveryItem.get(cnt2);
				totalItem++;
				if (perItem.orderItem.deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_DELIVERED))
				{
					completedItem++;
				}
			}
		}
		try
		{
			DeliveryTrip tripEJB = DeliveryTripNut.getHandle(this.tripObj.pkid);
			if (completedItem == 0)
			{
				this.tripObj.state = DeliveryTripBean.STATE_CREATED;
				tripEJB.setState(DeliveryTripBean.STATE_CREATED);
			} else if (completedItem < totalItem)
			{
				this.tripObj.state = DeliveryTripBean.STATE_DELIVERY_PARTIAL;
				tripEJB.setState(DeliveryTripBean.STATE_DELIVERY_PARTIAL);
			} else if (completedItem == totalItem)
			{
				this.tripObj.state = DeliveryTripBean.STATE_DELIVERY_COMPLETED;
				tripEJB.setState(DeliveryTripBean.STATE_DELIVERY_COMPLETED);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	// ////////////////////////////////////////////////////////
	// // INNER CLASS
	public static class PerOrder
	{
		public SalesOrderIndexObject salesOrder;
		public Vector vecDeliveryItem;
		public String uuid = "";

		public PerOrder(SalesOrderIndexObject soObj) throws Exception
		{
			this.salesOrder = soObj;
			this.vecDeliveryItem = new Vector();
			GUIDGenerator guidGen = new GUIDGenerator();
			this.uuid = guidGen.getUUID();
		}

		public String getKey()
		{
			return this.uuid;
		}

		public void addTripLink(DeliveryTripSOLinkObject tripItemLink, SalesOrderItemObject orderItem)
		{
			PerItem perItem = new PerItem(tripItemLink, orderItem);
			this.vecDeliveryItem.add(perItem);
		}

		public void removeTripLink(Long tripLinkPkid)
		{
			for (int cnt1 = 0; cnt1 < this.vecDeliveryItem.size(); cnt1++)
			{
				PerItem perItem = (PerItem) this.vecDeliveryItem.get(cnt1);
				if (perItem.tripItemLink.pkid.equals(tripLinkPkid))
				{
					this.vecDeliveryItem.remove(cnt1);
					cnt1--;
				}
			}
		}
		public static class PerItem
		{
			public DeliveryTripSOLinkObject tripItemLink;
			public SalesOrderItemObject orderItem;

			public PerItem(DeliveryTripSOLinkObject tripItemLink, SalesOrderItemObject orderItem)
			{
				this.tripItemLink = tripItemLink;
				this.orderItem = orderItem;
			}
		}
	}
}
