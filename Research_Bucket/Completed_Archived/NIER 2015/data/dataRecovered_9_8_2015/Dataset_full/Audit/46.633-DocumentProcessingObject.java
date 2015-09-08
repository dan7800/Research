/*
 *
 * Copyright 2002-2003 VLEE. All Rights Reserved.
 *
 * This software is the proprietary information of VLEE.
 * Use is subject to license terms.
 *
 */
package com.vlee.ejb.accounting;

import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.vlee.util.*;

public class DocumentProcessingObject implements Serializable
{
	public Long pkid; // Primary Key
	public String module;
	public String processType;
	public String category;
	public Integer auditLevel;
	public Integer userCreate;
	public Integer userPerform;
	public Integer userConfirm;
	public String description1;
	public String description2;
	public String remarks;
	public Timestamp timeCreated;
	public Timestamp timeScheduled;
	public Timestamp timeCompleted;
	public String state;
	public String status;
	public Vector vecItem;

	public DocumentProcessingObject()
	{
		this.pkid = new Long(0); // Primary Key
		this.module = "";
		this.processType = "";
		this.category = "";
		this.auditLevel = new Integer(0);
		this.userCreate = new Integer(0);
		this.userPerform = new Integer(0);
		this.userConfirm = new Integer(0);
		this.description1 = "";
		this.description2 = "";
		this.remarks = "";
		this.timeCreated = TimeFormat.getTimestamp();
		this.timeScheduled = this.timeCreated;
		this.timeCompleted = TimeFormat.getTimestamp();
		this.state = DocumentProcessingBean.STATE_CREATED;
		this.status = DocumentProcessingBean.STATUS_ACTIVE;
		this.vecItem = new Vector();
	}

	public void updateCollectionStatus()
	{
		boolean allComplete = true;
		for (int cnt = 0; cnt < this.vecItem.size(); cnt++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.vecItem.get(cnt);
			if (!dpiObj.state.equals(DocumentProcessingItemBean.STATE_COMPLETE))
			{
				allComplete = false;
			}
		}
		if (allComplete)
		{
			this.state = DocumentProcessingBean.STATE_CLOSED;
			this.timeCompleted = TimeFormat.getTimestamp();
		} else
		{
			this.state = DocumentProcessingBean.STATE_PENDING;
		}
	}
}


/*
 *
 * Copyright 2002-2003 VLEE. All Rights Reserved.
 *
 * This software is the proprietary information of VLEE.
 * Use is subject to license terms.
 *
 */
package com.vlee.ejb.accounting;

import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.vlee.util.*;

public class DocumentProcessingObject implements Serializable
{
	public Long pkid; // Primary Key
	public String module;
	public String processType;
	public String category;
	public Integer auditLevel;
	public Integer userCreate;
	public Integer userPerform;
	public Integer userConfirm;
	public String description1;
	public String description2;
	public String remarks;
	public Timestamp timeCreated;
	public Timestamp timeScheduled;
	public Timestamp timeCompleted;
	public String state;
	public String status;
	public Vector vecItem;

	public DocumentProcessingObject()
	{
		this.pkid = new Long(0); // Primary Key
		this.module = "";
		this.processType = "";
		this.category = "";
		this.auditLevel = new Integer(0);
		this.userCreate = new Integer(0);
		this.userPerform = new Integer(0);
		this.userConfirm = new Integer(0);
		this.description1 = "";
		this.description2 = "";
		this.remarks = "";
		this.timeCreated = TimeFormat.getTimestamp();
		this.timeScheduled = this.timeCreated;
		this.timeCompleted = TimeFormat.getTimestamp();
		this.state = DocumentProcessingBean.STATE_CREATED;
		this.status = DocumentProcessingBean.STATUS_ACTIVE;
		this.vecItem = new Vector();
	}

	public void updateCollectionStatus()
	{
		boolean allComplete = true;
		for (int cnt = 0; cnt < this.vecItem.size(); cnt++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.vecItem.get(cnt);
			if (!dpiObj.state.equals(DocumentProcessingItemBean.STATE_COMPLETE))
			{
				allComplete = false;
			}
		}
		if (allComplete)
		{
			this.state = DocumentProcessingBean.STATE_CLOSED;
		} else
		{
			this.state = DocumentProcessingBean.STATE_PENDING;
		}
	}
}
/*
 *
 * Copyright 2002-2003 VLEE. All Rights Reserved.
 *
 * This software is the proprietary information of VLEE.
 * Use is subject to license terms.
 *
 */
package com.vlee.ejb.accounting;

import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.vlee.util.*;

public class DocumentProcessingObject implements Serializable
{
	public Long pkid; // Primary Key
	public String module;
	public String processType;
	public String category;
	public Integer auditLevel;
	public Integer userCreate;
	public Integer userPerform;
	public Integer userConfirm;
	public String description1;
	public String description2;
	public String remarks;
	public Timestamp timeCreated;
	public Timestamp timeScheduled;
	public Timestamp timeCompleted;
	public String state;
	public String status;
	public Vector vecItem;

	public DocumentProcessingObject()
	{
		this.pkid = new Long(0); // Primary Key
		this.module = "";
		this.processType = "";
		this.category = "";
		this.auditLevel = new Integer(0);
		this.userCreate = new Integer(0);
		this.userPerform = new Integer(0);
		this.userConfirm = new Integer(0);
		this.description1 = "";
		this.description2 = "";
		this.remarks = "";
		this.timeCreated = TimeFormat.getTimestamp();
		this.timeScheduled = this.timeCreated;
		this.timeCompleted = TimeFormat.getTimestamp();
		this.state = DocumentProcessingBean.STATE_CREATED;
		this.status = DocumentProcessingBean.STATUS_ACTIVE;
		this.vecItem = new Vector();
	}

	public void updateCollectionStatus()
	{
		boolean allComplete = true;
		for (int cnt = 0; cnt < this.vecItem.size(); cnt++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.vecItem.get(cnt);
			if (!dpiObj.state.equals(DocumentProcessingItemBean.STATE_COMPLETE))
			{
				allComplete = false;
			}
		}
		if (allComplete)
		{
			this.state = DocumentProcessingBean.STATE_CLOSED;
		} else
		{
			this.state = DocumentProcessingBean.STATE_PENDING;
		}
	}
}


/*
 *
 * Copyright 2002-2003 VLEE. All Rights Reserved.
 *
 * This software is the proprietary information of VLEE.
 * Use is subject to license terms.
 *
 */
package com.vlee.ejb.accounting;

import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.vlee.util.*;

public class DocumentProcessingObject implements Serializable
{
	public Long pkid; // Primary Key
	public String module;
	public String processType;
	public String category;
	public Integer auditLevel;
	public Integer userCreate;
	public Integer userPerform;
	public Integer userConfirm;
	public String description1;
	public String description2;
	public String remarks;
	public Timestamp timeCreated;
	public Timestamp timeScheduled;
	public Timestamp timeCompleted;
	public String state;
	public String status;
	public Vector vecItem;

	public DocumentProcessingObject()
	{
		this.pkid = new Long(0); // Primary Key
		this.module = "";
		this.processType = "";
		this.category = "";
		this.auditLevel = new Integer(0);
		this.userCreate = new Integer(0);
		this.userPerform = new Integer(0);
		this.userConfirm = new Integer(0);
		this.description1 = "";
		this.description2 = "";
		this.remarks = "";
		this.timeCreated = TimeFormat.getTimestamp();
		this.timeScheduled = this.timeCreated;
		this.timeCompleted = TimeFormat.getTimestamp();
		this.state = DocumentProcessingBean.STATE_CREATED;
		this.status = DocumentProcessingBean.STATUS_ACTIVE;
		this.vecItem = new Vector();
	}

	public void updateCollectionStatus()
	{
		boolean allComplete = true;
		for (int cnt = 0; cnt < this.vecItem.size(); cnt++)
		{
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) this.vecItem.get(cnt);
			if (!dpiObj.state.equals(DocumentProcessingItemBean.STATE_COMPLETE))
			{
				allComplete = false;
			}
		}
		if (allComplete)
		{
			this.state = DocumentProcessingBean.STATE_CLOSED;
		} else
		{
			this.state = DocumentProcessingBean.STATE_PENDING;
		}
	}
}


