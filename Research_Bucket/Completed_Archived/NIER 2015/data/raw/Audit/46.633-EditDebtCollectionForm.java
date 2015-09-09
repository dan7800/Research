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
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class EditDebtCollectionForm extends java.lang.Object implements Serializable
{
	Integer userId;
	DocumentProcessingObject docProcObj;
	DocumentProcessing docProcEJB;

	// / contructor!
	public EditDebtCollectionForm(Integer iUser)
	{
		this.userId = iUser;
	}

	public void setDebtCollectionPool(Long dcpPkid) throws Exception
	{
		this.docProcObj = DocumentProcessingNut.getObject(dcpPkid);
		this.docProcEJB = DocumentProcessingNut.getHandle(dcpPkid);
		if (this.docProcObj == null)
		{
			this.docProcEJB = null;
			throw new Exception(" Invalid PKID: " + dcpPkid.toString());
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
		}
	}

	public DocumentProcessingObject getDebtCollectionPool()
	{
		return this.docProcObj;
	}

	public void setDebtCollectionPoolDetails(Integer iUserPerform, String description1, String description2,
			String remarks)
	{
		if (docProcObj == null || docProcEJB == null)
		{
			return;
		}
		this.docProcObj.userPerform = iUserPerform;
		this.docProcObj.description1 = description1;
		this.docProcObj.description2 = description2;
		this.docProcObj.remarks = remarks;
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setDateScheduled(String dateScheduled)
	{
		this.docProcObj.timeScheduled = TimeFormat.createTimestamp(dateScheduled);
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			dpiObj.time = this.docProcObj.timeScheduled;
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			try
			{
				dpiEJB.setObject(dpiObj);
			} catch (Exception ex)
			{
			}
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void reset()
	{
		this.docProcObj = null;
		this.docProcEJB = null;
	}

	public void fnAddOrder(Long lPkid) throws Exception
	{
		Log.printVerbose(" CHECKPOINT 1-------------------------------------------");
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lPkid);
		if (soObj == null)
		{
			throw new Exception("Invalid Order PKID!");
		}
		Log.printVerbose(" CHECKPOINT 2-------------------------------------------");
		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
		Log.printVerbose(" CHECKPOINT 3-------------------------------------------");
		dpiObj.processType = DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION;
		dpiObj.category = "";
		dpiObj.auditLevel = new Integer(0);
		Log.printVerbose(" CHECKPOINT 4-------------------------------------------");
		dpiObj.processId = this.docProcObj.pkid;
		dpiObj.userid = new Integer(0);
		dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
		Log.printVerbose(" CHECKPOINT 5-------------------------------------------");
		dpiObj.docId = lPkid;
		dpiObj.entityRef = soObj.senderTable1;
		dpiObj.entityId = soObj.senderKey1;
		Log.printVerbose(" CHECKPOINT 6-------------------------------------------");
		dpiObj.description1 = this.docProcObj.description1;
		dpiObj.description2 = this.docProcObj.description2;
		Log.printVerbose(" CHECKPOINT 7-------------------------------------------");
		dpiObj.remarks = this.docProcObj.remarks;
		dpiObj.time = this.docProcObj.timeScheduled;
		Log.printVerbose(" CHECKPOINT 8-------------------------------------------");
		if (soObj.statusPayment.equals(SalesOrderIndexBean.PAY_STATUS_SETTLED))
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
		} else
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		}
		// dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
		Log.printVerbose(" CHECKPOINT 9-------------------------------------------");
		if (dpiEJB == null)
		{
			throw new Exception("Failed to add order!");
		}
		this.docProcObj.vecItem.add(dpiObj);
		Log.printVerbose(" CHECKPOINT 10-------------------------------------------");
		this.docProcObj.updateCollectionStatus();
		Log.printVerbose(" CHECKPOINT 11-------------------------------------------");
		this.docProcEJB.setObject(this.docProcObj);
		Log.printVerbose(" CHECKPOINT 12-------------------------------------------");
	}

	public void setReceiptStatus(Long soPkid, String receiptRemarks, String payStatus)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptRemarks = receiptRemarks;
			soObj.statusPayment = payStatus;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.docRef.equals(SalesOrderIndexBean.TABLENAME) && dpiObj.docId.equals(soPkid))
			{
				try
				{
					if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
							|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_CREATED);
					} else
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_COMPLETE);
					}
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		QueryObject query = new QueryObject(new String[] {
				DocumentProcessingItemBean.DOC_ID + " = '" + soPkid.toString() + "' ",
				DocumentProcessingItemBean.DOC_REF + " = '" + SalesOrderIndexBean.TABLENAME + "' ",
				DocumentProcessingItemBean.PROCESS_TYPE + " = '" + DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION
						+ "' ", DocumentProcessingItemBean.PROCESS_ID + " > '0' " });
		Vector vecDPI = new Vector(DocumentProcessingItemNut.getObjects(query));
		for (int cnt1 = 0; cnt1 < vecDPI.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecDPI.get(cnt1);
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
					|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			} else
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
			}
			try
			{
				dpiEJB.setObject(dpiObj);
				DocumentProcessingObject dpObj = DocumentProcessingNut.getObject(dpiObj.processId);
				DocumentProcessing dpEJB = DocumentProcessingNut.getHandle(dpiObj.processId);
				dpObj.updateCollectionStatus();
				dpEJB.setObject(dpObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		this.docProcObj.updateCollectionStatus();
	}

	public void fnUpdateOrder(Long dpiPkid, String dpiState) throws Exception
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.pkid.equals(dpiPkid))
			{
				dpiObj.state = dpiState;
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
					dpiEJB.setObject(dpiObj);
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
				}
			}
		}
	}

	public void fnRemoveOrder(Long dpiPkid)
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.pkid.equals(dpiPkid))
			{
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiPkid);
					dpiEJB.remove();
					this.docProcObj.vecItem.remove(cnt1);
					cnt1--;
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector getOrderIDs()
	{
		Vector vecOrderID = new Vector();
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.docRef.equals(SalesOrderIndexBean.TABLENAME))
			{
				vecOrderID.add(itemObj.docId);
			}
		}
		return vecOrderID;
	}

	public void setApprovalCode(Long soPkid, String approvalCode)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptApprovalCode = approvalCode;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class EditDebtCollectionForm extends java.lang.Object implements Serializable
{
	Integer userId;
	DocumentProcessingObject docProcObj;
	DocumentProcessing docProcEJB;

	// / contructor!
	public EditDebtCollectionForm(Integer iUser)
	{
		this.userId = iUser;
	}

	public void setDebtCollectionPool(Long dcpPkid) throws Exception
	{
		this.docProcObj = DocumentProcessingNut.getObject(dcpPkid);
		this.docProcEJB = DocumentProcessingNut.getHandle(dcpPkid);
		if (this.docProcObj == null)
		{
			this.docProcEJB = null;
			throw new Exception(" Invalid PKID: " + dcpPkid.toString());
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
		}
	}

	public DocumentProcessingObject getDebtCollectionPool()
	{
		return this.docProcObj;
	}

	public void setDebtCollectionPoolDetails(Integer iUserPerform, String description1, String description2,
			String remarks)
	{
		if (docProcObj == null || docProcEJB == null)
		{
			return;
		}
		this.docProcObj.userPerform = iUserPerform;
		this.docProcObj.description1 = description1;
		this.docProcObj.description2 = description2;
		this.docProcObj.remarks = remarks;
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setDateScheduled(String dateScheduled)
	{
		this.docProcObj.timeScheduled = TimeFormat.createTimestamp(dateScheduled);
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			dpiObj.time = this.docProcObj.timeScheduled;
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			try
			{
				dpiEJB.setObject(dpiObj);
			} catch (Exception ex)
			{
			}
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void reset()
	{
		this.docProcObj = null;
		this.docProcEJB = null;
	}

	public void fnAddOrder(Long lPkid) throws Exception
	{
		Log.printVerbose(" CHECKPOINT 1-------------------------------------------");
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lPkid);
		if (soObj == null)
		{
			throw new Exception("Invalid Order PKID!");
		}
		Log.printVerbose(" CHECKPOINT 2-------------------------------------------");
		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
		Log.printVerbose(" CHECKPOINT 3-------------------------------------------");
		dpiObj.processType = DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION;
		dpiObj.category = "";
		dpiObj.auditLevel = new Integer(0);
		Log.printVerbose(" CHECKPOINT 4-------------------------------------------");
		dpiObj.processId = this.docProcObj.pkid;
		dpiObj.userid = new Integer(0);
		dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
		Log.printVerbose(" CHECKPOINT 5-------------------------------------------");
		dpiObj.docId = lPkid;
		dpiObj.entityRef = soObj.senderTable1;
		dpiObj.entityId = soObj.senderKey1;
		Log.printVerbose(" CHECKPOINT 6-------------------------------------------");
		dpiObj.description1 = this.docProcObj.description1;
		dpiObj.description2 = this.docProcObj.description2;
		Log.printVerbose(" CHECKPOINT 7-------------------------------------------");
		dpiObj.remarks = this.docProcObj.remarks;
		dpiObj.time = this.docProcObj.timeScheduled;
		Log.printVerbose(" CHECKPOINT 8-------------------------------------------");
		if (soObj.statusPayment.equals(SalesOrderIndexBean.PAY_STATUS_SETTLED))
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
		} else
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		}
		// dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
		Log.printVerbose(" CHECKPOINT 9-------------------------------------------");
		if (dpiEJB == null)
		{
			throw new Exception("Failed to add order!");
		}
		this.docProcObj.vecItem.add(dpiObj);
		Log.printVerbose(" CHECKPOINT 10-------------------------------------------");
		this.docProcObj.updateCollectionStatus();
		Log.printVerbose(" CHECKPOINT 11-------------------------------------------");
		this.docProcEJB.setObject(this.docProcObj);
		Log.printVerbose(" CHECKPOINT 12-------------------------------------------");
	}

	public void setReceiptStatus(Long soPkid, String receiptRemarks, String payStatus)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptRemarks = receiptRemarks;
			soObj.statusPayment = payStatus;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.docRef.equals(SalesOrderIndexBean.TABLENAME) && dpiObj.docId.equals(soPkid))
			{
				try
				{
					if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
							|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_CREATED);
					} else
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_COMPLETE);
					}
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		QueryObject query = new QueryObject(new String[] {
				DocumentProcessingItemBean.DOC_ID + " = '" + soPkid.toString() + "' ",
				DocumentProcessingItemBean.DOC_REF + " = '" + SalesOrderIndexBean.TABLENAME + "' ",
				DocumentProcessingItemBean.PROCESS_TYPE + " = '" + DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION
						+ "' ", DocumentProcessingItemBean.PROCESS_ID + " > '0' " });
		Vector vecDPI = new Vector(DocumentProcessingItemNut.getObjects(query));
		for (int cnt1 = 0; cnt1 < vecDPI.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecDPI.get(cnt1);
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
					|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			} else
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
			}
			try
			{
				dpiEJB.setObject(dpiObj);
				DocumentProcessingObject dpObj = DocumentProcessingNut.getObject(dpiObj.processId);
				DocumentProcessing dpEJB = DocumentProcessingNut.getHandle(dpiObj.processId);
				dpObj.updateCollectionStatus();
				dpEJB.setObject(dpObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		this.docProcObj.updateCollectionStatus();
	}

	public void fnUpdateOrder(Long dpiPkid, String dpiState) throws Exception
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.pkid.equals(dpiPkid))
			{
				dpiObj.state = dpiState;
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
					dpiEJB.setObject(dpiObj);
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
				}
			}
		}
	}

	public void fnRemoveOrder(Long dpiPkid)
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.pkid.equals(dpiPkid))
			{
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiPkid);
					dpiEJB.remove();
					this.docProcObj.vecItem.remove(cnt1);
					cnt1--;
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector getOrderIDs()
	{
		Vector vecOrderID = new Vector();
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.docRef.equals(SalesOrderIndexBean.TABLENAME))
			{
				vecOrderID.add(itemObj.docId);
			}
		}
		return vecOrderID;
	}

	public void setApprovalCode(Long soPkid, String approvalCode)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptApprovalCode = approvalCode;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class EditDebtCollectionForm extends java.lang.Object implements Serializable
{
	Integer userId;
	DocumentProcessingObject docProcObj;
	DocumentProcessing docProcEJB;

	// / contructor!
	public EditDebtCollectionForm(Integer iUser)
	{
		this.userId = iUser;
	}

	public void setDebtCollectionPool(Long dcpPkid) throws Exception
	{
		this.docProcObj = DocumentProcessingNut.getObject(dcpPkid);
		this.docProcEJB = DocumentProcessingNut.getHandle(dcpPkid);
		if (this.docProcObj == null)
		{
			this.docProcEJB = null;
			throw new Exception(" Invalid PKID: " + dcpPkid.toString());
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
		}
	}

	public DocumentProcessingObject getDebtCollectionPool()
	{
		return this.docProcObj;
	}

	public void setDebtCollectionPoolDetails(Integer iUserPerform, String description1, String description2,
			String remarks)
	{
		if (docProcObj == null || docProcEJB == null)
		{
			return;
		}
		this.docProcObj.userPerform = iUserPerform;
		this.docProcObj.description1 = description1;
		this.docProcObj.description2 = description2;
		this.docProcObj.remarks = remarks;
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setDateScheduled(String dateScheduled)
	{
		this.docProcObj.timeScheduled = TimeFormat.createTimestamp(dateScheduled);
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			dpiObj.time = this.docProcObj.timeScheduled;
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			try
			{
				dpiEJB.setObject(dpiObj);
			} catch (Exception ex)
			{
			}
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void reset()
	{
		this.docProcObj = null;
		this.docProcEJB = null;
	}

	public void fnAddOrder(Long lPkid) throws Exception
	{
		Log.printVerbose(" CHECKPOINT 1-------------------------------------------");
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lPkid);
		if (soObj == null)
		{
			throw new Exception("Invalid Order PKID!");
		}
		Log.printVerbose(" CHECKPOINT 2-------------------------------------------");
		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
		Log.printVerbose(" CHECKPOINT 3-------------------------------------------");
		dpiObj.processType = DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION;
		dpiObj.category = "";
		dpiObj.auditLevel = new Integer(0);
		Log.printVerbose(" CHECKPOINT 4-------------------------------------------");
		dpiObj.processId = this.docProcObj.pkid;
		dpiObj.userid = new Integer(0);
		dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
		Log.printVerbose(" CHECKPOINT 5-------------------------------------------");
		dpiObj.docId = lPkid;
		dpiObj.entityRef = soObj.senderTable1;
		dpiObj.entityId = soObj.senderKey1;
		Log.printVerbose(" CHECKPOINT 6-------------------------------------------");
		dpiObj.description1 = this.docProcObj.description1;
		dpiObj.description2 = this.docProcObj.description2;
		Log.printVerbose(" CHECKPOINT 7-------------------------------------------");
		dpiObj.remarks = this.docProcObj.remarks;
		dpiObj.time = this.docProcObj.timeScheduled;
		Log.printVerbose(" CHECKPOINT 8-------------------------------------------");
		if (soObj.statusPayment.equals(SalesOrderIndexBean.PAY_STATUS_SETTLED))
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
		} else
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		}
		// dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
		Log.printVerbose(" CHECKPOINT 9-------------------------------------------");
		if (dpiEJB == null)
		{
			throw new Exception("Failed to add order!");
		}
		this.docProcObj.vecItem.add(dpiObj);
		Log.printVerbose(" CHECKPOINT 10-------------------------------------------");
		this.docProcObj.updateCollectionStatus();
		Log.printVerbose(" CHECKPOINT 11-------------------------------------------");
		this.docProcEJB.setObject(this.docProcObj);
		Log.printVerbose(" CHECKPOINT 12-------------------------------------------");
	}

	public void setReceiptStatus(Long soPkid, String receiptRemarks, String payStatus)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptRemarks = receiptRemarks;
			soObj.statusPayment = payStatus;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.docRef.equals(SalesOrderIndexBean.TABLENAME) && dpiObj.docId.equals(soPkid))
			{
				try
				{
					if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
							|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_CREATED);
					} else
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_COMPLETE);
					}
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		QueryObject query = new QueryObject(new String[] {
				DocumentProcessingItemBean.DOC_ID + " = '" + soPkid.toString() + "' ",
				DocumentProcessingItemBean.DOC_REF + " = '" + SalesOrderIndexBean.TABLENAME + "' ",
				DocumentProcessingItemBean.PROCESS_TYPE + " = '" + DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION
						+ "' ", DocumentProcessingItemBean.PROCESS_ID + " > '0' " });
		Vector vecDPI = new Vector(DocumentProcessingItemNut.getObjects(query));
		for (int cnt1 = 0; cnt1 < vecDPI.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecDPI.get(cnt1);
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
					|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			} else
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
			}
			try
			{
				dpiEJB.setObject(dpiObj);
				DocumentProcessingObject dpObj = DocumentProcessingNut.getObject(dpiObj.processId);
				DocumentProcessing dpEJB = DocumentProcessingNut.getHandle(dpiObj.processId);
				dpObj.updateCollectionStatus();
				dpEJB.setObject(dpObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		this.docProcObj.updateCollectionStatus();
	}

	public void fnUpdateOrder(Long dpiPkid, String dpiState) throws Exception
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.pkid.equals(dpiPkid))
			{
				dpiObj.state = dpiState;
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
					dpiEJB.setObject(dpiObj);
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
				}
			}
		}
	}

	public void fnRemoveOrder(Long dpiPkid)
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.pkid.equals(dpiPkid))
			{
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiPkid);
					dpiEJB.remove();
					this.docProcObj.vecItem.remove(cnt1);
					cnt1--;
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector getOrderIDs()
	{
		Vector vecOrderID = new Vector();
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.docRef.equals(SalesOrderIndexBean.TABLENAME))
			{
				vecOrderID.add(itemObj.docId);
			}
		}
		return vecOrderID;
	}

	public void setApprovalCode(Long soPkid, String approvalCode)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptApprovalCode = approvalCode;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class EditDebtCollectionForm extends java.lang.Object implements Serializable
{
	Integer userId;
	DocumentProcessingObject docProcObj;
	DocumentProcessing docProcEJB;

	// / contructor!
	public EditDebtCollectionForm(Integer iUser)
	{
		this.userId = iUser;
	}

	public void setDebtCollectionPool(Long dcpPkid) throws Exception
	{
		this.docProcObj = DocumentProcessingNut.getObject(dcpPkid);
		this.docProcEJB = DocumentProcessingNut.getHandle(dcpPkid);
		if (this.docProcObj == null)
		{
			this.docProcEJB = null;
			throw new Exception(" Invalid PKID: " + dcpPkid.toString());
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
		}
	}

	public DocumentProcessingObject getDebtCollectionPool()
	{
		return this.docProcObj;
	}

	public void setDebtCollectionPoolDetails(Integer iUserPerform, String description1, String description2,
			String remarks)
	{
		if (docProcObj == null || docProcEJB == null)
		{
			return;
		}
		this.docProcObj.userPerform = iUserPerform;
		this.docProcObj.description1 = description1;
		this.docProcObj.description2 = description2;
		this.docProcObj.remarks = remarks;
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setDateScheduled(String dateScheduled)
	{
		this.docProcObj.timeScheduled = TimeFormat.createTimestamp(dateScheduled);
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			dpiObj.time = this.docProcObj.timeScheduled;
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			try
			{
				dpiEJB.setObject(dpiObj);
			} catch (Exception ex)
			{
			}
		}
		this.docProcObj.updateCollectionStatus();
		try
		{
			this.docProcEJB.setObject(this.docProcObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void reset()
	{
		this.docProcObj = null;
		this.docProcEJB = null;
	}

	public void fnAddOrder(Long lPkid) throws Exception
	{
		Log.printVerbose(" CHECKPOINT 1-------------------------------------------");
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lPkid);
		if (soObj == null)
		{
			throw new Exception("Invalid Order PKID!");
		}
		Log.printVerbose(" CHECKPOINT 2-------------------------------------------");
		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
		Log.printVerbose(" CHECKPOINT 3-------------------------------------------");
		dpiObj.processType = DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION;
		dpiObj.category = "";
		dpiObj.auditLevel = new Integer(0);
		Log.printVerbose(" CHECKPOINT 4-------------------------------------------");
		dpiObj.processId = this.docProcObj.pkid;
		dpiObj.userid = new Integer(0);
		dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
		Log.printVerbose(" CHECKPOINT 5-------------------------------------------");
		dpiObj.docId = lPkid;
		dpiObj.entityRef = soObj.senderTable1;
		dpiObj.entityId = soObj.senderKey1;
		Log.printVerbose(" CHECKPOINT 6-------------------------------------------");
		dpiObj.description1 = this.docProcObj.description1;
		dpiObj.description2 = this.docProcObj.description2;
		Log.printVerbose(" CHECKPOINT 7-------------------------------------------");
		dpiObj.remarks = this.docProcObj.remarks;
		dpiObj.time = this.docProcObj.timeScheduled;
		Log.printVerbose(" CHECKPOINT 8-------------------------------------------");
		if (soObj.statusPayment.equals(SalesOrderIndexBean.PAY_STATUS_SETTLED))
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
		} else
		{
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		}
		// dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
		Log.printVerbose(" CHECKPOINT 9-------------------------------------------");
		if (dpiEJB == null)
		{
			throw new Exception("Failed to add order!");
		}
		this.docProcObj.vecItem.add(dpiObj);
		Log.printVerbose(" CHECKPOINT 10-------------------------------------------");
		this.docProcObj.updateCollectionStatus();
		Log.printVerbose(" CHECKPOINT 11-------------------------------------------");
		this.docProcEJB.setObject(this.docProcObj);
		Log.printVerbose(" CHECKPOINT 12-------------------------------------------");
	}

	public void setReceiptStatus(Long soPkid, String receiptRemarks, String payStatus)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptRemarks = receiptRemarks;
			soObj.statusPayment = payStatus;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.docRef.equals(SalesOrderIndexBean.TABLENAME) && dpiObj.docId.equals(soPkid))
			{
				try
				{
					if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
							|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_CREATED);
					} else
					{
						fnUpdateOrder(dpiObj.pkid, DocumentProcessingItemBean.STATE_COMPLETE);
					}
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		QueryObject query = new QueryObject(new String[] {
				DocumentProcessingItemBean.DOC_ID + " = '" + soPkid.toString() + "' ",
				DocumentProcessingItemBean.DOC_REF + " = '" + SalesOrderIndexBean.TABLENAME + "' ",
				DocumentProcessingItemBean.PROCESS_TYPE + " = '" + DocumentProcessingItemBean.PROCESS_DEBT_COLLECTION
						+ "' ", DocumentProcessingItemBean.PROCESS_ID + " > '0' " });
		Vector vecDPI = new Vector(DocumentProcessingItemNut.getObjects(query));
		for (int cnt1 = 0; cnt1 < vecDPI.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecDPI.get(cnt1);
			DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
			if (payStatus.equals(SalesOrderIndexBean.PAY_STATUS_PENDING)
					|| payStatus.equals(SalesOrderIndexBean.PAY_STATUS_INVOICE))
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			} else
			{
				dpiObj.state = DocumentProcessingItemBean.STATE_COMPLETE;
			}
			try
			{
				dpiEJB.setObject(dpiObj);
				DocumentProcessingObject dpObj = DocumentProcessingNut.getObject(dpiObj.processId);
				DocumentProcessing dpEJB = DocumentProcessingNut.getHandle(dpiObj.processId);
				dpObj.updateCollectionStatus();
				dpEJB.setObject(dpObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		this.docProcObj.updateCollectionStatus();
	}

	public void fnUpdateOrder(Long dpiPkid, String dpiState) throws Exception
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (dpiObj.pkid.equals(dpiPkid))
			{
				dpiObj.state = dpiState;
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
					dpiEJB.setObject(dpiObj);
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
				}
			}
		}
	}

	public void fnRemoveOrder(Long dpiPkid)
	{
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.pkid.equals(dpiPkid))
			{
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiPkid);
					dpiEJB.remove();
					this.docProcObj.vecItem.remove(cnt1);
					cnt1--;
					this.docProcObj.updateCollectionStatus();
					this.docProcEJB.setObject(this.docProcObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector getOrderIDs()
	{
		Vector vecOrderID = new Vector();
		for (int cnt1 = 0; cnt1 < this.docProcObj.vecItem.size(); cnt1++)
		{
			DocumentProcessingItemObject itemObj = (DocumentProcessingItemObject) this.docProcObj.vecItem.get(cnt1);
			if (itemObj.docRef.equals(SalesOrderIndexBean.TABLENAME))
			{
				vecOrderID.add(itemObj.docId);
			}
		}
		return vecOrderID;
	}

	public void setApprovalCode(Long soPkid, String approvalCode)
	{
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(soPkid);
		SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soPkid);
		try
		{
			soObj.receiptApprovalCode = approvalCode;
			soEJB.setObject(soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
