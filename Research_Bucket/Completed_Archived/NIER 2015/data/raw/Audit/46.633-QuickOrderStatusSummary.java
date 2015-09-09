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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

/*--------------------------------------------------------------
 BASIC LOGIC
 1. CRV is only created when Cashsale is confirmed because
 a) Full payment must be made
 b) No more additional sales order item to be added after
 the invoice is issued, so CRV gain could be determined.
 ----------------------------------------------------------------*/
public class QuickOrderStatusSummary extends java.lang.Object implements Serializable
{

	Timestamp dateToday = null;
	Long orderNo = null;
	Integer userId = null;
	ProcessingTripForm proTripForm = null;


	public Vector vecOrderChecking;
	public Vector vecOrderUpdate;
	public Vector vecFloAssignedBy;

	// // constructor
	public QuickOrderStatusSummary(Integer userId)
		throws Exception
	{
		this.userId = userId;
		this.dateToday = TimeFormat.getTimestamp();
		this.proTripForm = new ProcessingTripForm(this.userId);
	}


	public Integer getUserId()
	{ return this.userId;}


	public void setOrderNo(Long orderNo)
	{ 
		this.orderNo = orderNo;
		this.proTripForm.setSalesOrder(this.orderNo);
		loadObjects();
	}


	public ProcessingTripForm getProcessingTripForm()
	{ return this.proTripForm;}

	public void loadObjects()
	{
		if(this.orderNo!=null)
		{
			QueryObject query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-CHECK' ",
						});
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME);
			this.vecOrderChecking = new Vector(DocumentProcessingItemNut.getObjects(query));

			query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'UPDATE-ORDER' ",
						});
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME);
			this.vecOrderUpdate = new Vector(DocumentProcessingItemNut.getObjects(query));
		}

	}


	public String getOrderNo(String buf)
	{
		if(this.orderNo==null){ return buf;}
		return this.orderNo.toString();
	}

	public Long getOrderNo()
	{
		return this.orderNo;
	}

	public Vector getOrderChecking()
	{ return this.vecOrderChecking; }
	
	public Vector getOrderUpdate()
	{ return this.vecOrderUpdate;}

	public DocumentProcessingItemObject getLastCheck(String category)
	{
      DocumentProcessingItemObject result = null;
      for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
      {
         DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
         if(dpiObj.category.equals(category))
         { result = dpiObj;}
      }
      return result;
	}

	public DocumentProcessingItemObject getLastUpdate(String category)
	{
      DocumentProcessingItemObject result = null;
      for(int cnt1=0;cnt1<vecOrderUpdate.size();cnt1++)
      {
         DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderUpdate.get(cnt1);
         if(dpiObj.category.equals(category))
         { result = dpiObj;}
      }
      return result;
	}


	public boolean hasChecked(String category)
	{
		boolean result = false;
		for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
			if(dpiObj.category.equals(category))
			{ result = true;}
		}
		return result;
	}
	
	public String floAssignedBy(String category)
	{
		if(this.orderNo!=null)
		{
			QueryObject query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'UPDATE-ORDER' ",});
			
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME+" DESC");
			this.vecFloAssignedBy = new Vector(DocumentProcessingItemNut.getObjects(query));
		}
		
		String floAssignedBy = "";
		
		if(vecFloAssignedBy.size() > 0)
		{
			for(int cnt1=0;cnt1<1;cnt1++)
			{
				DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecFloAssignedBy.get(cnt1);
				
				if(dpiObj.category.equals(category))
				{ 
					floAssignedBy = UserNut.getUserName(dpiObj.userid);
				}
			}
		}
		
		return floAssignedBy;
	}


	public boolean hasUpdated(String category)
	{
		boolean result = false;
		for(int cnt1=0;cnt1<vecOrderUpdate.size();cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderUpdate.get(cnt1);
			if(dpiObj.category.equals(category))
			{ result = true;}
		}
		return result;
	}


	public void setOrderChecked(String category)
		throws Exception
	{
		/// check if order is valid
		if(this.orderNo==null){ throw new Exception("Order Is NULL!");}
		// check permission of the user

		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(this.orderNo);
		
		String strTmp = "";
		
		if("CHECK-MSG".equals(category))
		{
			if("f".equals(soObj.messageCardCheck))				
				strTmp = "NOT_CHECKED";
			else
				strTmp = "CHECKED";
		}
		else
		{
			if("f".equals(soObj.checkOrderDetails))				
				strTmp = "NOT_CHECKED";
			else
				strTmp = "CHECKED";
		}
		
		System.out.println("strTmp : "+strTmp);
							
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);
		
		if("CHECK-MSG".equals(category))
		{
			soEJB.checkMessage();
		}
		else
		{
			soEJB.checkOrder();
		}
		
		
		/// create the check item
         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
         dpiObj.processType = "CHECK ";
         dpiObj.category = category;
         dpiObj.auditLevel = new Integer(1);
//       dpiObj.processId = new Long(0);
         dpiObj.userid = this.userId;
         dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
         dpiObj.docId = this.orderNo;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
         dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("CHECK",strTmp,"CHECKED", "");
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
         dpiObj.time = TimeFormat.getTimestamp();
         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
         DocumentProcessingItemNut.fnCreate(dpiObj);

		/// reload the objects..
		loadObjects();
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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

/*--------------------------------------------------------------
 BASIC LOGIC
 1. CRV is only created when Cashsale is confirmed because
 a) Full payment must be made
 b) No more additional sales order item to be added after
 the invoice is issued, so CRV gain could be determined.
 ----------------------------------------------------------------*/
public class QuickOrderStatusSummary extends java.lang.Object implements Serializable
{

	Timestamp dateToday = null;
	Long orderNo = null;
	Integer userId = null;
	ProcessingTripForm proTripForm = null;


	public Vector vecOrderChecking;

	// // constructor
	public QuickOrderStatusSummary(Integer userId)
		throws Exception
	{
		this.userId = userId;
		this.dateToday = TimeFormat.getTimestamp();
		this.proTripForm = new ProcessingTripForm(this.userId);
	}


	public Integer getUserId()
	{ return this.userId;}


	public void setOrderNo(Long orderNo)
	{ 
		this.orderNo = orderNo;
		this.proTripForm.setSalesOrder(this.orderNo);
		loadObjects();
	}


	public ProcessingTripForm getProcessingTripForm()
	{ return this.proTripForm;}

	public void loadObjects()
	{
		if(this.orderNo!=null)
		{
			QueryObject query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-CHECK' ",
						});
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME);
			this.vecOrderChecking = new Vector(DocumentProcessingItemNut.getObjects(query));

		}

	}


	public String getOrderNo(String buf)
	{
		if(this.orderNo==null){ return buf;}
		return this.orderNo.toString();
	}

	public Long getOrderNo()
	{
		return this.orderNo;
	}

	public Vector getOrderChecking()
	{ return this.vecOrderChecking; }


	public DocumentProcessingItemObject getLastCheck(String category)
	{
      DocumentProcessingItemObject result = null;
      for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
      {
         DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
         if(dpiObj.category.equals(category))
         { result = dpiObj;}
      }
      return result;
	}

	public boolean hasChecked(String category)
	{
		boolean result = false;
		for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
			if(dpiObj.category.equals(category))
			{ result = true;}
		}
		return result;
	}

	public void setOrderChecked(String category, String description)
		throws Exception
	{
		/// check if order is valid
		if(this.orderNo==null){ throw new Exception("Order Is NULL!");}
		// check permission of the user

		/// create the check item
         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
         dpiObj.processType = "ORDER-CHECK";
         dpiObj.category = category;
         dpiObj.auditLevel = new Integer(1);
//       dpiObj.processId = new Long(0);
         dpiObj.userid = this.userId;
         dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
         dpiObj.docId = this.orderNo;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
         dpiObj.description1 = description;
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
         dpiObj.time = TimeFormat.getTimestamp();
         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
         DocumentProcessingItemNut.fnCreate(dpiObj);

		/// reload the objects..
		loadObjects();
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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

/*--------------------------------------------------------------
 BASIC LOGIC
 1. CRV is only created when Cashsale is confirmed because
 a) Full payment must be made
 b) No more additional sales order item to be added after
 the invoice is issued, so CRV gain could be determined.
 ----------------------------------------------------------------*/
public class QuickOrderStatusSummary extends java.lang.Object implements Serializable
{

	Timestamp dateToday = null;
	Long orderNo = null;
	Integer userId = null;
	ProcessingTripForm proTripForm = null;


	public Vector vecOrderChecking;
	public Vector vecOrderUpdate;

	// // constructor
	public QuickOrderStatusSummary(Integer userId)
		throws Exception
	{
		this.userId = userId;
		this.dateToday = TimeFormat.getTimestamp();
		this.proTripForm = new ProcessingTripForm(this.userId);
	}


	public Integer getUserId()
	{ return this.userId;}


	public void setOrderNo(Long orderNo)
	{ 
		this.orderNo = orderNo;
		this.proTripForm.setSalesOrder(this.orderNo);
		loadObjects();
	}


	public ProcessingTripForm getProcessingTripForm()
	{ return this.proTripForm;}

	public void loadObjects()
	{
		if(this.orderNo!=null)
		{
			QueryObject query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-CHECK' ",
						});
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME);
			this.vecOrderChecking = new Vector(DocumentProcessingItemNut.getObjects(query));

			query = new QueryObject(new String[]{
				DocumentProcessingItemBean.DOC_ID+" ='"+this.orderNo.toString()+"' ",
				DocumentProcessingItemBean.DOC_REF +" ='"+SalesOrderIndexBean.TABLENAME+"' ",
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'UPDATE-ORDER' ",
						});
			query.setOrder("ORDER BY "+DocumentProcessingItemBean.TIME);
			this.vecOrderUpdate = new Vector(DocumentProcessingItemNut.getObjects(query));
		}

	}


	public String getOrderNo(String buf)
	{
		if(this.orderNo==null){ return buf;}
		return this.orderNo.toString();
	}

	public Long getOrderNo()
	{
		return this.orderNo;
	}

	public Vector getOrderChecking()
	{ return this.vecOrderChecking; }
	
	public Vector getOrderUpdate()
	{ return this.vecOrderUpdate;}

	public DocumentProcessingItemObject getLastCheck(String category)
	{
      DocumentProcessingItemObject result = null;
      for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
      {
         DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
         if(dpiObj.category.equals(category))
         { result = dpiObj;}
      }
      return result;
	}

	public DocumentProcessingItemObject getLastUpdate(String category)
	{
      DocumentProcessingItemObject result = null;
      for(int cnt1=0;cnt1<vecOrderUpdate.size();cnt1++)
      {
         DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderUpdate.get(cnt1);
         if(dpiObj.category.equals(category))
         { result = dpiObj;}
      }
      return result;
	}


	public boolean hasChecked(String category)
	{
		boolean result = false;
		for(int cnt1=0;cnt1<vecOrderChecking.size();cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderChecking.get(cnt1);
			if(dpiObj.category.equals(category))
			{ result = true;}
		}
		return result;
	}


	public boolean hasUpdated(String category)
	{
		boolean result = false;
		for(int cnt1=0;cnt1<vecOrderUpdate.size();cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecOrderUpdate.get(cnt1);
			if(dpiObj.category.equals(category))
			{ result = true;}
		}
		return result;
	}


	public void setOrderChecked(String category, String description)
		throws Exception
	{
		/// check if order is valid
		if(this.orderNo==null){ throw new Exception("Order Is NULL!");}
		// check permission of the user

		/// create the check item
         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
         dpiObj.processType = "ORDER-CHECK";
         dpiObj.category = category;
         dpiObj.auditLevel = new Integer(1);
//       dpiObj.processId = new Long(0);
         dpiObj.userid = this.userId;
         dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
         dpiObj.docId = this.orderNo;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
         dpiObj.description1 = description;
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
         dpiObj.time = TimeFormat.getTimestamp();
         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
         DocumentProcessingItemNut.fnCreate(dpiObj);

		/// reload the objects..
		loadObjects();
	}

}




