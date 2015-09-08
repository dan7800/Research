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
import java.sql.Timestamp;
import java.util.Vector;
import com.vlee.ejb.accounting.DocumentProcessingBean;
import com.vlee.ejb.accounting.DocumentProcessingNut;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class DebtCollectionPoolSelectForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String dateType;
	public String keyword;
	public String state;

	public DebtCollectionPoolSelectForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		// this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.dateFrom = TimeFormat.add(this.dateTo, 0, 0, -1);
		this.keyword = "";
		this.state = "";
		this.dateType = DocumentProcessingBean.TIME_CREATED;
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

	public void setState(String buf)
	{
		this.state = buf;
	}

	public String getState()
	{
		return this.state;
	}

	public Vector getList()
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' " });
		if (this.state.length() > 0)
		{
			query = new QueryObject(new String[] {
					this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
					DocumentProcessingBean.STATE + " = '" + this.state + "' " });
		}
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(DocumentProcessingNut.getObjects(query));
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Vector;
import com.vlee.ejb.accounting.DocumentProcessingBean;
import com.vlee.ejb.accounting.DocumentProcessingNut;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class DebtCollectionPoolSelectForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String dateType;
	public String keyword;
	public String state;

	public DebtCollectionPoolSelectForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		// this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.dateFrom = TimeFormat.add(this.dateTo, 0, 0, -1);
		this.keyword = "";
		this.state = "";
		this.dateType = DocumentProcessingBean.TIME_CREATED;
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

	public void setState(String buf)
	{
		this.state = buf;
	}

	public String getState()
	{
		return this.state;
	}

	public Vector getList()
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' " });
		if (this.state.length() > 0)
		{
			query = new QueryObject(new String[] {
					this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
					DocumentProcessingBean.STATE + " = '" + this.state + "' " });
		}
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(DocumentProcessingNut.getObjects(query));
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Vector;
import com.vlee.ejb.accounting.DocumentProcessingBean;
import com.vlee.ejb.accounting.DocumentProcessingNut;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class DebtCollectionPoolSelectForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String dateType;
	public String keyword;
	public String state;

	public DebtCollectionPoolSelectForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		// this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.dateFrom = TimeFormat.add(this.dateTo, 0, 0, -1);
		this.keyword = "";
		this.state = "";
		this.dateType = DocumentProcessingBean.TIME_CREATED;
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

	public void setState(String buf)
	{
		this.state = buf;
	}

	public String getState()
	{
		return this.state;
	}

	public Vector getList()
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' " });
		if (this.state.length() > 0)
		{
			query = new QueryObject(new String[] {
					this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
					DocumentProcessingBean.STATE + " = '" + this.state + "' " });
		}
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(DocumentProcessingNut.getObjects(query));
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Vector;
import com.vlee.ejb.accounting.DocumentProcessingBean;
import com.vlee.ejb.accounting.DocumentProcessingNut;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class DebtCollectionPoolSelectForm extends java.lang.Object implements Serializable
{
	Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String dateType;
	public String keyword;
	public String state;

	public DebtCollectionPoolSelectForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		// this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.dateFrom = TimeFormat.add(this.dateTo, 0, 0, -1);
		this.keyword = "";
		this.state = "";
		this.dateType = DocumentProcessingBean.TIME_CREATED;
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

	public void setState(String buf)
	{
		this.state = buf;
	}

	public String getState()
	{
		return this.state;
	}

	public Vector getList()
	{
		// Vector vecResult =
		// DocumentProcessingNut.getDebtCollectionPool(this.dateFrom,
		// this.dateTo, this.dateType, this.keyword, this.state);
		Timestamp tsNexTo = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query = new QueryObject(new String[] {
				this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
				this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' " });
		if (this.state.length() > 0)
		{
			query = new QueryObject(new String[] {
					this.dateType + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					this.dateType + " < '" + TimeFormat.strDisplayDate(tsNexTo) + "' ",
					DocumentProcessingBean.STATE + " = '" + this.state + "' " });
		}
		query.setOrder(" ORDER BY " + this.dateType);
		Vector vecResult = new Vector(DocumentProcessingNut.getObjects(query));
		return vecResult;
	}
}
