/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;
import com.vlee.util.*;

public class MemberCardAuditTrailObject implements Serializable
{
	public Long pkid;
	public Integer auditLevel;
	public String auditType;
	public Long cardPkid;
	public String cardNo;
	public String cardName;
	public Integer branch;
	public Integer pcCenter;
	public Integer userTxn;
	public Timestamp dateTxn;
	public Timestamp dateCreate;
	public String docRef1;
	public Long docKey1;
	public String docRef2;
	public Long docKey2;
	public BigDecimal amountTxn;
	public BigDecimal amountDelta;
	public String info1;
	public String info2;
	public String warning;
	public String state;
	public String status;

	public MemberCardAuditTrailObject()
	{
		this.pkid = new Long(0);
		this.auditLevel = new Integer(0);
		this.auditType = new String("");
		this.cardPkid = new Long(0);
		this.cardNo = "";
		this.cardName = "";
		this.branch = new Integer(0);
		this.pcCenter = new Integer(0);
		this.userTxn = new Integer(0);
		this.dateTxn = TimeFormat.getTimestamp();
		this.dateCreate = TimeFormat.getTimestamp();
		this.docRef1 = "";
		this.docKey1 = new Long(0);
		this.docRef2 = "";
		this.docKey2 = new Long(0);
		this.amountTxn = new BigDecimal(0);
		this.amountDelta = new BigDecimal(0);
		this.info1 = "";
		this.info2 = "";
		this.warning = "";
		this.state = MemberCardAuditTrailBean.STATE_CREATED;
		this.status = MemberCardAuditTrailBean.STATUS_ACTIVE;
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
package com.vlee.ejb.customer;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;
import com.vlee.util.*;

public class MemberCardAuditTrailObject implements Serializable
{
	public Long pkid;
	public Integer auditLevel;
	public String auditType;
	public Long cardPkid;
	public String cardNo;
	public String cardName;
	public Integer branch;
	public Integer pcCenter;
	public Integer userTxn;
	public Timestamp dateTxn;
	public Timestamp dateCreate;
	public String docRef1;
	public Long docKey1;
	public String docRef2;
	public Long docKey2;
	public BigDecimal amountTxn;
	public BigDecimal amountDelta;
	public String info1;
	public String info2;
	public String warning;
	public String state;
	public String status;

	public MemberCardAuditTrailObject()
	{
		this.pkid = new Long(0);
		this.auditLevel = new Integer(0);
		this.auditType = new String("");
		this.cardPkid = new Long(0);
		this.cardNo = "";
		this.cardName = "";
		this.branch = new Integer(0);
		this.pcCenter = new Integer(0);
		this.userTxn = new Integer(0);
		this.dateTxn = TimeFormat.getTimestamp();
		this.dateCreate = TimeFormat.getTimestamp();
		this.docRef1 = "";
		this.docKey1 = new Long(0);
		this.docRef2 = "";
		this.docKey2 = new Long(0);
		this.amountTxn = new BigDecimal(0);
		this.amountDelta = new BigDecimal(0);
		this.info1 = "";
		this.info2 = "";
		this.warning = "";
		this.state = MemberCardAuditTrailBean.STATE_CREATED;
		this.status = MemberCardAuditTrailBean.STATUS_ACTIVE;
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
package com.vlee.ejb.customer;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;
import com.vlee.util.*;

public class MemberCardAuditTrailObject implements Serializable
{
	public Long pkid;
	public Integer auditLevel;
	public String auditType;
	public Long cardPkid;
	public String cardNo;
	public String cardName;
	public Integer branch;
	public Integer pcCenter;
	public Integer userTxn;
	public Timestamp dateTxn;
	public Timestamp dateCreate;
	public String docRef1;
	public Long docKey1;
	public String docRef2;
	public Long docKey2;
	public BigDecimal amountTxn;
	public BigDecimal amountDelta;
	public String info1;
	public String info2;
	public String warning;
	public String state;
	public String status;

	public MemberCardAuditTrailObject()
	{
		this.pkid = new Long(0);
		this.auditLevel = new Integer(0);
		this.auditType = new String("");
		this.cardPkid = new Long(0);
		this.cardNo = "";
		this.cardName = "";
		this.branch = new Integer(0);
		this.pcCenter = new Integer(0);
		this.userTxn = new Integer(0);
		this.dateTxn = TimeFormat.getTimestamp();
		this.dateCreate = TimeFormat.getTimestamp();
		this.docRef1 = "";
		this.docKey1 = new Long(0);
		this.docRef2 = "";
		this.docKey2 = new Long(0);
		this.amountTxn = new BigDecimal(0);
		this.amountDelta = new BigDecimal(0);
		this.info1 = "";
		this.info2 = "";
		this.warning = "";
		this.state = MemberCardAuditTrailBean.STATE_CREATED;
		this.status = MemberCardAuditTrailBean.STATUS_ACTIVE;
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
package com.vlee.ejb.customer;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;
import com.vlee.util.*;

public class MemberCardAuditTrailObject implements Serializable
{
	public Long pkid;
	public Integer auditLevel;
	public String auditType;
	public Long cardPkid;
	public String cardNo;
	public String cardName;
	public Integer branch;
	public Integer pcCenter;
	public Integer userTxn;
	public Timestamp dateTxn;
	public Timestamp dateCreate;
	public String docRef1;
	public Long docKey1;
	public String docRef2;
	public Long docKey2;
	public BigDecimal amountTxn;
	public BigDecimal amountDelta;
	public String info1;
	public String info2;
	public String warning;
	public String state;
	public String status;

	public MemberCardAuditTrailObject()
	{
		this.pkid = new Long(0);
		this.auditLevel = new Integer(0);
		this.auditType = new String("");
		this.cardPkid = new Long(0);
		this.cardNo = "";
		this.cardName = "";
		this.branch = new Integer(0);
		this.pcCenter = new Integer(0);
		this.userTxn = new Integer(0);
		this.dateTxn = TimeFormat.getTimestamp();
		this.dateCreate = TimeFormat.getTimestamp();
		this.docRef1 = "";
		this.docKey1 = new Long(0);
		this.docRef2 = "";
		this.docKey2 = new Long(0);
		this.amountTxn = new BigDecimal(0);
		this.amountDelta = new BigDecimal(0);
		this.info1 = "";
		this.info2 = "";
		this.warning = "";
		this.state = MemberCardAuditTrailBean.STATE_CREATED;
		this.status = MemberCardAuditTrailBean.STATUS_ACTIVE;
	}
}
