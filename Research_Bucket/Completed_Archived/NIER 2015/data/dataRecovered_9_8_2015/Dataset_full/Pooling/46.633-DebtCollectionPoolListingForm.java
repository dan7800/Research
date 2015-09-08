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

public class DebtCollectionPoolListingForm extends java.lang.Object implements Serializable
{
	public Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String state;
	public Vector vecDPO;

	public DebtCollectionPoolListingForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.vecDPO = new Vector();
		this.state = "";
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dtFrom, String dtTo)
	{
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public String getState()
	{ return this.state;}

	public void setState(String buf)
	{ this.state = buf;}

	public Vector searchRecords()
	{
		Timestamp dateAfterTo = TimeFormat.add(this.dateTo,0,0,1);
		
		if(!state.equals("All"))
		{
			QueryObject query = new QueryObject( new String[]{
					DocumentProcessingBean.TIME_CREATED+" >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					DocumentProcessingBean.TIME_CREATED+" < '"+TimeFormat.strDisplayDate(dateAfterTo)+"' ",
					DocumentProcessingBean.STATE +" = '"+this.state+"' "
										});
			query.setOrder(" ORDER BY "+DocumentProcessingBean.PKID);
			this.vecDPO = new Vector(DocumentProcessingNut.getObjects(query));
			return this.vecDPO;
		}
		else
		{
			QueryObject query = new QueryObject( new String[]{
					DocumentProcessingBean.TIME_CREATED+" >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					DocumentProcessingBean.TIME_CREATED+" < '"+TimeFormat.strDisplayDate(dateAfterTo)+"' "});
			
			query.setOrder(" ORDER BY "+DocumentProcessingBean.PKID);
			this.vecDPO = new Vector(DocumentProcessingNut.getObjects(query));
			return this.vecDPO;
		}
		
	}

	public Vector getRecords()
	{ return this.vecDPO;}

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

public class DebtCollectionPoolListingForm extends java.lang.Object implements Serializable
{
	public Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String state;
	public Vector vecDPO;


	public DebtCollectionPoolListingForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.vecDPO = new Vector();
		this.state = "";
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dtFrom, String dtTo)
	{
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public String getState()
	{ return this.state;}

	public void setState(String buf)
	{ this.state = buf;}

	public Vector searchRecords()
	{
		Timestamp dateAfterTo = TimeFormat.add(this.dateTo,0,0,1);
		QueryObject query = new QueryObject( new String[]{
					DocumentProcessingBean.TIME_CREATED+" >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					DocumentProcessingBean.TIME_CREATED+" < '"+TimeFormat.strDisplayDate(dateAfterTo)+"' ",
					DocumentProcessingBean.STATE +" = '"+this.state+"' "
										});
		query.setOrder(" ORDER BY "+DocumentProcessingBean.PKID);
		this.vecDPO = new Vector(DocumentProcessingNut.getObjects(query));
		return this.vecDPO;
	}

	public Vector getRecords()
	{ return this.vecDPO;}

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

public class DebtCollectionPoolListingForm extends java.lang.Object implements Serializable
{
	public Integer userId;
	public Timestamp dateFrom;
	public Timestamp dateTo;
	public String state;
	public Vector vecDPO;


	public DebtCollectionPoolListingForm(Integer userId)
	{
		this.userId = userId;
		this.dateTo = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateTo, 0, -1, 0);
		this.vecDPO = new Vector();
		this.state = "";
	}

	public String getDateFrom(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public String getDateTo(String buf)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public void setDateRange(String dtFrom, String dtTo)
	{
		this.dateFrom = TimeFormat.createTimestamp(dtFrom);
		this.dateTo = TimeFormat.createTimestamp(dtTo);
	}

	public String getState()
	{ return this.state;}

	public void setState(String buf)
	{ this.state = buf;}

	public Vector searchRecords()
	{
		Timestamp dateAfterTo = TimeFormat.add(this.dateTo,0,0,1);
		QueryObject query = new QueryObject( new String[]{
					DocumentProcessingBean.TIME_CREATED+" >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					DocumentProcessingBean.TIME_CREATED+" < '"+TimeFormat.strDisplayDate(dateAfterTo)+"' ",
					DocumentProcessingBean.STATE +" = '"+this.state+"' "
										});
		query.setOrder(" ORDER BY "+DocumentProcessingBean.PKID);
		this.vecDPO = new Vector(DocumentProcessingNut.getObjects(query));
		return this.vecDPO;
	}

	public Vector getRecords()
	{ return this.vecDPO;}

}


