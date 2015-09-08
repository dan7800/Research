/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.supplier;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import javax.naming.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.JobsheetIndex;
import com.vlee.ejb.customer.JobsheetIndexObject;
import com.vlee.ejb.customer.JobsheetItem;
import com.vlee.ejb.customer.JobsheetItemBean;
import com.vlee.ejb.customer.JobsheetItemNut;
import com.vlee.ejb.customer.JobsheetItemObject;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class PurchaseOrderNut
{
	private static String strClassName = "PurchaseOrderNut";

	public static PurchaseOrderHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			PurchaseOrderHome lEJBHome = (PurchaseOrderHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/supplier/PurchaseOrder"), PurchaseOrderHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static PurchaseOrder getHandle(Long pkid)
	{
		return (PurchaseOrder) getHandle(getHome(), pkid);
	}

	public static PurchaseOrder getHandle(PurchaseOrderHome lEJBHome, Long pkid)
	{
		try
		{
			return (PurchaseOrder) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	// ///////////////////////////////////////////////////
	public static PurchaseOrder fnCreate(PurchaseOrderObject poObj)
	{
		PurchaseOrder poEJB = null;
		PurchaseOrderHome poHome = getHome();
		try
		{
			poObj.mAmount = poObj.getQuotedAmount();
			poEJB = poHome.create(poObj);
			poObj.mPkid = poEJB.getPkid();
			poObj.mStmtNumber = poEJB.getStmtNumber();
			// 2.1) create the PurchaseOrder Items Object selectively
			for (int countA = 0; countA < poObj.vecPurchaseOrderItems.size(); countA++)
			{
				PurchaseOrderItemObject poItemObj = (PurchaseOrderItemObject) poObj.vecPurchaseOrderItems.get(countA);
				poItemObj.mPurchaseOrderId = poObj.mPkid;
				PurchaseOrderItem poItem = PurchaseOrderItemNut.fnCreate(poItemObj);
			}
			return poEJB;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("PurchaseOrderNut: " + "Cannot create this PurchaseOrder");
			return (PurchaseOrder) null;
		}
	}

	public static PurchaseOrderObject getObject(Long iPkid)
	{
		PurchaseOrderObject purchaseOrderObj = new PurchaseOrderObject();
		PurchaseOrder purchaseOrderEJB = getHandle(iPkid);
		if (purchaseOrderEJB == null)
			return null;
		try
		{
			purchaseOrderObj = purchaseOrderEJB.getObject();
			// If we're successful up to this point, populate the
			// vecPurchaseOrderItems
			purchaseOrderObj.vecPurchaseOrderItems = new Vector(PurchaseOrderItemNut
					.getObjectsForPurchaseOrder(purchaseOrderObj.mPkid));
			
			System.out.println("checkpoint zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
			System.out.println("OLD poObj.mPkid : "+purchaseOrderObj.mPkid.toString());
			
			String strVecPurchaseOrderItems = (new Integer(purchaseOrderObj.vecPurchaseOrderItems.size())).toString(); 
			System.out.println("OLD poItem size : "+strVecPurchaseOrderItems);
			
		} catch (Exception ex)
		{
			Log.printDebug("Unable to create return PurchaseOrder value Object" + ex.getMessage());
		}
		return purchaseOrderObj;
	}

	public static Collection getObjectsForPurchaseTxn(Long purchaseTxnId)
	{
		String funcName = "getObjectsForPurchaseTxn()";
		Log.printVerbose("In PurchaseOrderNut::getObjectsForPurchaseTxn()");
		Collection colPurchaseOrderObj = new Vector();
		Collection colPurchaseOrder = null;
		colPurchaseOrder = getCollectionByField(PurchaseOrderBean.PURCHASE_TXN_ID, purchaseTxnId.toString());
		Log.printVerbose("getObjectsForPurchaseTxn() - " + "Size of colInvoice = " + colPurchaseOrder.size());
		// for each invoice
		// 1. Populate its corresponding colInvoiceObj
		// 2. Call POSItemNut.getChildObject()
		int count = 1;
		for (Iterator objItr = colPurchaseOrder.iterator(); objItr.hasNext();)
		{
			PurchaseOrder lPurchaseOrder = (PurchaseOrder) objItr.next();
			// Get the corresponding colInvoiceObj
			try
			{
				PurchaseOrderObject lJsObj = getObject(lPurchaseOrder.getPkid());
				if (lJsObj == null)
					throw new Exception("Null PurchaseOrderObject");
				colPurchaseOrderObj.add(lJsObj);
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + ":" + funcName + " - " + "Error while processing colPurchaseOrderObj["
						+ count + "]." + "Reason: " + ex.getMessage());
				ex.printStackTrace();
			}
			count++;
		}
		return colPurchaseOrderObj;
	}

	public static Collection getCollectionByField(String fieldName, String value)
	{
		Collection colObjects = null;
		PurchaseOrderHome lEJBHome = getHome();
		try
		{
			colObjects = (Collection) lEJBHome.findObjectsGiven(fieldName, value);
		} catch (Exception ex)
		{
			Log.printDebug("PurchaseOrderNut:" + ex.getMessage());
		}
		return colObjects;
	}

	public static void cancelPurchaseOrders(Long purchaseTxnId)
	{
		String funcName = "cancelPurchaseOrders()";
		Log.printVerbose("In PurchaseOrderNut::" + funcName);
		Collection colPurchaseOrder = null;
		// PurchaseOrderItemHome lEJBHome = getHome();
		colPurchaseOrder = getCollectionByField(PurchaseOrderBean.PURCHASE_TXN_ID, purchaseTxnId.toString());
		Log.printVerbose(funcName + " - Size of colPurchaseOrder = " + colPurchaseOrder.size());
		// for each purchaseOrder
		// 1. Set the STATUS field to CANCELLED
		int count = 1;
		for (Iterator objItr = colPurchaseOrder.iterator(); objItr.hasNext();)
		{
			PurchaseOrder lPurchaseOrder = (PurchaseOrder) objItr.next();
			try
			{
				// Set its own to CANCELLED and the subtending items to
				// CANCELLED as well
				lPurchaseOrder.setStatus(PurchaseOrderBean.STATUS_CANCELLED);
				PurchaseOrderItemNut.cancelItems(lPurchaseOrder.getPkid());
			} catch (Exception ex)
			{
				Log.printDebug(strClassName + ":" + funcName + " - "
						+ "Error while processing colPurchaseOrderItemObj[" + count + "]." + "Reason: "
						+ ex.getMessage());
			} // end try-catch
			count++;
		} // end for
	} // end cancelPurchaseOrders

	/*
	 * public static PurchaseOrder getObjectByCode(String code) {
	 * Log.printVerbose("In PurchaseOrderNut::getObjectByCode(String code)");
	 * 
	 * String fieldName = new String(PurchaseOrderBean.CUSTCODE);
	 * 
	 * Collection colPurchaseOrder = getCollectionByField( fieldName, code);
	 * Iterator itrPurchaseOrder = colPurchaseOrder.iterator(); PurchaseOrder
	 * rtnPurchaseOrder = null; if(itrPurchaseOrder.hasNext()) rtnPurchaseOrder =
	 * (PurchaseOrder) itrPurchaseOrder.next();
	 * 
	 * 
	 * return rtnPurchaseOrder; }
	 * 
	 * public static PurchaseOrder getObjectByName(String name) {
	 * Log.printVerbose("In PurchaseOrderNut::getObjectByName(String name)");
	 * 
	 * String fieldName = new String(PurchaseOrderBean.NAME);
	 * 
	 * Collection colPurchaseOrder = getCollectionByField( fieldName, name);
	 * Iterator itrPurchaseOrder = colPurchaseOrder.iterator(); PurchaseOrder
	 * rtnPurchaseOrder = null; if(itrPurchaseOrder.hasNext()) rtnPurchaseOrder =
	 * (PurchaseOrder) itrPurchaseOrder.next();
	 * 
	 * return rtnPurchaseOrder; }
	 */
	public static Collection getAllObjects()
	{
		Collection colObjects = null;
		PurchaseOrderHome lEJBHome = getHome();
		try
		{
			colObjects = (Collection) lEJBHome.findAllObjects();
		} catch (Exception ex)
		{
			Log.printDebug("PurchaseOrderNut: " + ex.getMessage());
		}
		return colObjects;
	}

/*
	public static Vector getValObjGiven(Integer suppAccId, Integer procCtrId, Timestamp dateFrom, Timestamp dateTo,
			String poState, String poStatus)
	{
		Vector vecObj = null;
		PurchaseOrderHome lEJBHome = getHome();
		try
		{
			vecObj = lEJBHome.getValObjGiven(suppAccId, procCtrId, dateFrom, dateTo, poState, poStatus);
		} catch (Exception ex)
		{
			Log.printDebug("PurchaseOrderNut: " + ex.getMessage());
		}
		return vecObj;
	}
*/

	public static boolean setGRNOK(Long poPkid)
	{
		PurchaseOrder ejb = getHandle(poPkid);
		if (ejb == null)
		{
			Log.printDebug("PurchaseOrderNut: No such PurchaseOrder instance with pkid = " + poPkid.toString());
			return false;
		}
		try
		{
			ejb.setState(PurchaseOrderBean.STATE_GRN_OK);
		} catch (Exception ex)
		{
			Log.printDebug("PurchaseOrderNut: " + ex.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Forces the recalculation (re-derivation) of the state of the PO with the
	 * specified po_id, and sets the PurchaseOrder::state accordingly Valid
	 * states are: <br>
	 * PurchaseOrder.STATE_PRECREAT (not really used) <br>
	 * PurchaseOrder.STATE_CREATED (upon creation) <br>
	 * PurchaseOrder.STATE_APPROVED (when approved)<br>
	 * PurchaseOrder.STATE_GRN_OK (when all goods are received)<br>
	 * PurchaseOrder.STATE_CLOSED (not really used) <br>
	 * <p>
	 * (TO_DO: Once an exception model is designed and implemented, instead of
	 * returning nulls for exception, should throw the appropriate exceptions
	 * instead)
	 * 
	 * @param Long:
	 *            the purchaseOrder Pkid
	 * @return String: the derived state of the PO or null in the case of any
	 *         form of exceptions
	 */
	public static String recalcState(Long poPkid)
	{
		PurchaseOrder poEJB = getHandle(poPkid);
		if (poEJB == null)
		{
			Log.printDebug("PurchaseOrderNut: No such PurchaseOrder instance with pkid = " + poPkid.toString());
			return null;
		}
		String strOrigState = null;
		try
		{
			strOrigState = poEJB.getState();
			Log.printVerbose("*** Orig State = " + strOrigState + " ***");
			// Get the GRNs attached to this PO
			Collection colGRNObj = GoodsReceivedNoteNut.getObjectsForPurchaseTxn(poEJB.getPurchaseTxnId());
			if (colGRNObj == null)
			{
				return strOrigState; // return whichever state it was
										// originally in
			}
			// Get the collection of POItems
			Collection colPOItemsObj = PurchaseOrderItemNut.getObjectsForPurchaseOrder(poPkid);
			// Put each items in a hash map
			HashMap mapPOItems = new HashMap();
			for (Iterator poiItr = colPOItemsObj.iterator(); poiItr.hasNext();)
			{
				PurchaseOrderItemObject thisPOItemObj = (PurchaseOrderItemObject) poiItr.next();
				mapPOItems.put(thisPOItemObj.mPkid, thisPOItemObj.mTotalQty);
				Log.printVerbose("*** Put " + thisPOItemObj.mPkid + "," + thisPOItemObj.mTotalQty + " ***");
			}
			for (Iterator grnItr = colGRNObj.iterator(); grnItr.hasNext();)
			{
				GoodsReceivedNoteObject thisGRNObj = (GoodsReceivedNoteObject) grnItr.next();
				for (int grniCount = 0; grniCount < thisGRNObj.vecGRNItems.size(); grniCount++)
				{
					GoodsReceivedNoteItemObject thisGRNItemObj = (GoodsReceivedNoteItemObject) thisGRNObj.vecGRNItems
							.get(grniCount);
					BigDecimal currVal = null;
					if ((currVal = (BigDecimal) mapPOItems.get(thisGRNItemObj.mPurchaseOrderItemId)) != null)
					{
						mapPOItems.put(thisGRNItemObj.mPurchaseOrderItemId, currVal.subtract(thisGRNItemObj.mTotalQty));
					}
				}
				Log.printVerbose("*** mapPOItems = " + mapPOItems.toString() + " ***");
			}
			// At the end of this routine, check to see if mapPOItems contains
			// all ZERO values!
			Collection mapPOItemsVal = mapPOItems.values();
			boolean bGRNOK = true;
			for (Iterator mapPOItemsValItr = mapPOItemsVal.iterator(); mapPOItemsValItr.hasNext();)
			{
				BigDecimal thisVal = (BigDecimal) mapPOItemsValItr.next();
				if (thisVal.intValue() != 0)
				{
					Log.printVerbose("Detected NON-ZERO GRNItem count");
					bGRNOK = false;
					break;
				}
			}
			if (bGRNOK)
			{
				Log.printVerbose("GRN OK !!");
				poEJB.setState(PurchaseOrderBean.STATE_GRN_OK);
				return PurchaseOrderBean.STATE_GRN_OK;
			} else
			{
				Log.printVerbose("GRN NOT OK !!");
				poEJB.setState(PurchaseOrderBean.STATE_APPROVED);
				return PurchaseOrderBean.STATE_APPROVED;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Exception in PurchaseOrderNut::recalcState(): " + ex.getMessage());
			return strOrigState;
		}
	}
	
	public static synchronized void update(PurchaseOrderObject purchaseOrder) 
	throws Exception 
	{ 		
		PurchaseOrderObject poNew = purchaseOrder;
		Vector vecNewItem = poNew.vecPurchaseOrderItems;					
			
		String strVecNewItem2 = (new Integer(poNew.vecPurchaseOrderItems.size())).toString(); 
		System.out.println("2. vecNewItem : "+strVecNewItem2);				
		
		PurchaseOrderObject poOld = getObjectTree(purchaseOrder.mPkid);
		Vector vecOldItem = poOld.vecPurchaseOrderItems;
		
		poNew.vecPurchaseOrderItems = vecNewItem;
		
		String strVecNewItem3 = (new Integer(poNew.vecPurchaseOrderItems.size())).toString(); 
		System.out.println("3. vecNewItem : "+strVecNewItem3);				
		
		String strVecOldItem = (new Integer(vecOldItem.size())).toString(); 
		System.out.println("4. vecOldItem : "+strVecOldItem);		

		
		poNew.calculateAmount();
		PurchaseOrder poEJB = getHandle(poNew.mPkid);
		try
		{
			poEJB.setObject(purchaseOrder);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}				
		
		// then update eixsting purchase order items
		for(int cnt1=0;cnt1<poNew.vecPurchaseOrderItems.size();cnt1++)
		{
			System.out.println("Checking existing purchaserOrder");	
			
			PurchaseOrderItemObject poiObj = (PurchaseOrderItemObject) poNew.vecPurchaseOrderItems.get(cnt1);
			
			System.out.println("poiObj.mPkid"+poiObj.mPkid.toString());
			
			if(poiObj.mPkid.intValue()>0)
			{
				System.out.println("Updating existing po Item");
				
				PurchaseOrderItem poItemEJB = PurchaseOrderItemNut.getHandle(poiObj.mPkid);
				try
				{					
					PurchaseOrderItemObject oldPOObj = poItemEJB.getObject();
					poItemEJB.setObject(poiObj);
					
					Log.printVerbose("No. of serial numbers to  be deleted: " + oldPOObj.colSerialObj.size());
					
					for(int i = 0; i<oldPOObj.colSerialObj.size();i++)
					{
						PurchaseOrderItemSerialObject snObj = (PurchaseOrderItemSerialObject) oldPOObj.colSerialObj.get(i);
						Log.printVerbose(snObj.guid);
						PurchaseOrderItemSerial ejbPOIS = PurchaseOrderItemSerialNut.getHandle(snObj.guid);
						if(ejbPOIS!=null)
						{
							Log.printVerbose("Deleting: " + snObj.serial);
							ejbPOIS.remove();
						}
						
						Log.printVerbose("Serial deleted: " + snObj.serial);
					}
					if(poiObj.colSerialObj.size()>0)
					{
						Log.printVerbose("No. of serial numbers to  be created: " + poiObj.colSerialObj.size());
						for(int i = 0;i<poiObj.colSerialObj.size();i++)
						{
							PurchaseOrderItemSerialObject snObj = (PurchaseOrderItemSerialObject) poiObj.colSerialObj.get(i);
							PurchaseOrderItemSerialNut.fnCreate(snObj);
						}
						
					}
					
				}
				catch(Exception ex)
				{ ex.printStackTrace();}
			}
			else if(poiObj.mPkid.intValue()==0)
			{
				System.out.println("Insert new po Item");
				
				try	
				{			
					poiObj.mPurchaseOrderId = poNew.mPkid;
					PurchaseOrderItemNut.fnCreate(poiObj);		
					if(poiObj.colSerialObj.size()>0)
					{
						Log.printVerbose("No. of serial numbers to  be created: " + poiObj.colSerialObj.size());
						for(int i = 0;i<poiObj.colSerialObj.size();i++)
						{
							PurchaseOrderItemSerialObject snObj = (PurchaseOrderItemSerialObject) poiObj.colSerialObj.get(i);
							PurchaseOrderItemSerialNut.fnCreate(snObj);
						}
						
					}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}
			}
		}				
		
		System.out.println("Checkpoint AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		String strVecNewItem = (new Integer(poNew.vecPurchaseOrderItems.size())).toString(); 
		System.out.println("1. vecNewItem : "+strVecNewItem);								
				
		// remove the purchase items that have been deleted
		for(int cnt1=0;cnt1<vecOldItem.size();cnt1++)
		{						
			PurchaseOrderItemObject poOldItemObj = (PurchaseOrderItemObject) vecOldItem.get(cnt1);			
						
			String strVecNewItem4 = (new Integer(poNew.vecPurchaseOrderItems.size())).toString(); 			
			System.out.println("1111. vecNewItem : "+strVecNewItem4);	
			
			if(!poNew.hasPOItem(poOldItemObj.mPkid))
			{
				try
				{
					PurchaseOrderItem poOldItemEJB = PurchaseOrderItemNut.getHandle(poOldItemObj.mPkid);
					poOldItemEJB.remove();
				}
				catch(Exception ex)
				{ ex.printStackTrace(); }
			}
		}			
	}
	
	public static PurchaseOrderObject getObjectTree(Long lPkid)
	{		
		PurchaseOrderObject purchaseOrderOld = new PurchaseOrderObject();
		PurchaseOrder purchaseOrderEJB = getHandle(lPkid);
		if (purchaseOrderEJB == null)
			return null;
		
		try
		{
			purchaseOrderOld = purchaseOrderEJB.getObject();
		}
		catch(Exception ex)
		{ ex.printStackTrace(); }
								
		QueryObject query = new QueryObject(new String[]{
			PurchaseOrderItemBean.PURCHASE_ORDER_ID + " = '"+lPkid.toString()+"' "});
		
		query.setOrder(" ORDER BY "+PurchaseOrderItemBean.PKID);
		
		purchaseOrderOld.vecPurchaseOrderItems = new Vector(PurchaseOrderItemNut.getObjects(query));
		
		return purchaseOrderOld;	
	}
}
