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

public class DebtCollectionPoolAddOrderForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public String dateType;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String payStatus;

	public DebtCollectionPoolAddOrderForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.payStatus = SalesOrderIndexBean.PAY_STATUS_INVOICE;
		this.dateType = SalesOrderIndexBean.EXP_DELIVERY_TIME_START;
	}

	public String getDateType()
	{
		return this.dateType;
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dateType, String dtFrom, String dtTo)
	{
		this.dateType = dateType;
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public void setPayStatus(String buf)
	{
		this.payStatus = buf;
	}

	public String getPayStatus()
	{
		return this.payStatus;
	}

	public Vector getList(Vector vecExcludePKID)
	{
		Vector vecResult = new Vector();
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
				SalesOrderIndexBean.STATUS_PAYMENT + " = '" + this.payStatus + "' " });
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecTemp = new Vector(SalesOrderIndexNut.getObjects(query));
		// / need to remove SalesOrder that is already exist in the
		// DebtCollectionPool
		for (int cnt1 = 0; cnt1 < vecTemp.size(); cnt1++)
		{
			SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecTemp.get(cnt1);
			boolean selected = false;
			for (int cnt2 = 0; cnt2 < vecExcludePKID.size(); cnt2++)
			{
				Long excludePkid = (Long) vecExcludePKID.get(cnt2);
				if (excludePkid.equals(soObj.pkid))
				{
					selected = true;
				}
			}
			if (selected)
			{
				vecTemp.remove(cnt1);
				cnt1--;
			}
		}

         QueryObject queryRecycleBin = new QueryObject(new String[]{
                           DocumentProcessingItemBean.CATEGORY +" = 'RECYCLE-BIN' ",
                           DocumentProcessingItemBean.PROCESS_TYPE + " = 'ORDER-RECYCLE' ",
                           DocumentProcessingItemBean.DOC_REF+" ='"+SalesOrderIndexBean.TABLENAME+"' "
                     });
         queryRecycleBin.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID ) ;
         Vector vecRecycleBin = new Vector(DocumentProcessingItemNut.getObjects(queryRecycleBin));

         QueryObject queryUnsavedOrder = new QueryObject(new String[]{
                           DocumentProcessingItemBean.CATEGORY +" = 'UNSAVED-ORDER-BIN' ",
                           DocumentProcessingItemBean.PROCESS_TYPE + " = 'ORDER-CREATION' ",
                           DocumentProcessingItemBean.DOC_REF+" ='"+SalesOrderIndexBean.TABLENAME+"' "
                     });
         queryUnsavedOrder.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID ) ;
         Vector vecUnsavedOrder = new Vector(DocumentProcessingItemNut.getObjects(queryUnsavedOrder));

		for(int cnt1=0;cnt1<vecTemp.size();cnt1++)
		{
				SalesOrderIndexObject orow = (SalesOrderIndexObject) vecTemp.get(cnt1);

            boolean insideBin = false;
            for(int cnt2=0;cnt2<vecUnsavedOrder.size();cnt2++)
            {
               DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecUnsavedOrder.get(cnt2);
               if(dpiObj.docId.equals(orow.pkid))
               { insideBin = true;}
            }
            for(int cnt2=0;cnt2<vecRecycleBin.size();cnt2++)
            {
               DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecRecycleBin.get(cnt2);
               if(dpiObj.docId.equals(orow.pkid))
               { insideBin = true;}
            }

            if(insideBin==false)
            {
               vecResult.add(orow);
            }
		}

		return vecResult;
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

public class DebtCollectionPoolAddOrderForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public String dateType;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String payStatus;

	public DebtCollectionPoolAddOrderForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.payStatus = SalesOrderIndexBean.PAY_STATUS_INVOICE;
		this.dateType = SalesOrderIndexBean.EXP_DELIVERY_TIME_START;
	}

	public String getDateType()
	{
		return this.dateType;
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dateType, String dtFrom, String dtTo)
	{
		this.dateType = dateType;
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public void setPayStatus(String buf)
	{
		this.payStatus = buf;
	}

	public String getPayStatus()
	{
		return this.payStatus;
	}

	public Vector getList(Vector vecExcludePKID)
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
				SalesOrderIndexBean.STATUS_PAYMENT + " = '" + this.payStatus + "' " });
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(SalesOrderIndexNut.getObjects(query));
		// / need to remove SalesOrder that is already exist in the
		// DebtCollectionPool
		for (int cnt1 = 0; cnt1 < vecResult.size(); cnt1++)
		{
			SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecResult.get(cnt1);
			boolean selected = false;
			for (int cnt2 = 0; cnt2 < vecExcludePKID.size(); cnt2++)
			{
				Long excludePkid = (Long) vecExcludePKID.get(cnt2);
				if (excludePkid.equals(soObj.pkid))
				{
					selected = true;
				}
			}
			if (selected)
			{
				vecResult.remove(cnt1);
				cnt1--;
			}
		}
		return vecResult;
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

public class DebtCollectionPoolAddOrderForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public String dateType;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String payStatus;

	public DebtCollectionPoolAddOrderForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.payStatus = SalesOrderIndexBean.PAY_STATUS_INVOICE;
		this.dateType = SalesOrderIndexBean.EXP_DELIVERY_TIME_START;
	}

	public String getDateType()
	{
		return this.dateType;
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dateType, String dtFrom, String dtTo)
	{
		this.dateType = dateType;
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public void setPayStatus(String buf)
	{
		this.payStatus = buf;
	}

	public String getPayStatus()
	{
		return this.payStatus;
	}

	public Vector getList(Vector vecExcludePKID)
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
				SalesOrderIndexBean.STATUS_PAYMENT + " = '" + this.payStatus + "' " });
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(SalesOrderIndexNut.getObjects(query));
		// / need to remove SalesOrder that is already exist in the
		// DebtCollectionPool
		for (int cnt1 = 0; cnt1 < vecResult.size(); cnt1++)
		{
			SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecResult.get(cnt1);
			boolean selected = false;
			for (int cnt2 = 0; cnt2 < vecExcludePKID.size(); cnt2++)
			{
				Long excludePkid = (Long) vecExcludePKID.get(cnt2);
				if (excludePkid.equals(soObj.pkid))
				{
					selected = true;
				}
			}
			if (selected)
			{
				vecResult.remove(cnt1);
				cnt1--;
			}
		}
		return vecResult;
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

public class DebtCollectionPoolAddOrderForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public String dateType;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String payStatus;

	public DebtCollectionPoolAddOrderForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.payStatus = SalesOrderIndexBean.PAY_STATUS_INVOICE;
		this.dateType = SalesOrderIndexBean.EXP_DELIVERY_TIME_START;
	}

	public String getDateType()
	{
		return this.dateType;
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dateType, String dtFrom, String dtTo)
	{
		this.dateType = dateType;
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public void setPayStatus(String buf)
	{
		this.payStatus = buf;
	}

	public String getPayStatus()
	{
		return this.payStatus;
	}

	public Vector getList(Vector vecExcludePKID)
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
				SalesOrderIndexBean.STATUS_PAYMENT + " = '" + this.payStatus + "' " });
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(SalesOrderIndexNut.getObjects(query));
		// / need to remove SalesOrder that is already exist in the
		// DebtCollectionPool
		for (int cnt1 = 0; cnt1 < vecResult.size(); cnt1++)
		{
			SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecResult.get(cnt1);
			boolean selected = false;
			for (int cnt2 = 0; cnt2 < vecExcludePKID.size(); cnt2++)
			{
				Long excludePkid = (Long) vecExcludePKID.get(cnt2);
				if (excludePkid.equals(soObj.pkid))
				{
					selected = true;
				}
			}
			if (selected)
			{
				vecResult.remove(cnt1);
				cnt1--;
			}
		}
		return vecResult;
	}
}
