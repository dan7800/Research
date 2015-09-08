/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class AuditTrailObject extends Object implements Serializable
{
	public Long pkid = new Long("0"); // primary key !!!
	public Integer userId = new Integer("0");
	public String namespace = " ";
	public Integer auditType = new Integer("0");
	public Integer auditLevel = new Integer("0");
	public String foreignTable1 = " ";
	public Long foreignKey1 = new Long("0");
	public String foreignTable2 = " ";
	public Integer foreignKey2 = new Integer("0");
	public String remarks = " ";
	public Timestamp time = TimeFormat.getTimestamp();
	public String state = AuditTrailBean.STATE_NONE;
	public String status = " ";
	public String tc_entity_table = "";
	public Integer tc_entity_id = new Integer(0);
	public String tc_action = "";

	public AuditTrailObject()
	{
		time = TimeFormat.getTimestamp();
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
package com.vlee.ejb.user;

import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class AuditTrailObject extends Object implements Serializable
{
	public Long pkid = new Long("0"); // primary key !!!
	public Integer userId = new Integer("0");
	public String namespace = " ";
	public Integer auditType = new Integer("0");
	public Integer auditLevel = new Integer("0");
	public String foreignTable1 = " ";
	public Long foreignKey1 = new Long("0");
	public String foreignTable2 = " ";
	public Integer foreignKey2 = new Integer("0");
	public String remarks = " ";
	public Timestamp time = TimeFormat.getTimestamp();
	public String state = AuditTrailBean.STATE_NONE;
	public String status = " ";

	public AuditTrailObject()
	{
		time = TimeFormat.getTimestamp();
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
package com.vlee.ejb.user;

import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class AuditTrailObject extends Object implements Serializable
{
	public Long pkid = new Long("0"); // primary key !!!
	public Integer userId = new Integer("0");
	public String namespace = " ";
	public Integer auditType = new Integer("0");
	public Integer auditLevel = new Integer("0");
	public String foreignTable1 = " ";
	public Long foreignKey1 = new Long("0");
	public String foreignTable2 = " ";
	public Integer foreignKey2 = new Integer("0");
	public String remarks = " ";
	public Timestamp time = TimeFormat.getTimestamp();
	public String state = AuditTrailBean.STATE_NONE;
	public String status = " ";

	public AuditTrailObject()
	{
		time = TimeFormat.getTimestamp();
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
package com.vlee.ejb.user;

import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;

public class AuditTrailObject extends Object implements Serializable
{
	public Long pkid = new Long("0"); // primary key !!!
	public Integer userId = new Integer("0");
	public String namespace = " ";
	public Integer auditType = new Integer("0");
	public Integer auditLevel = new Integer("0");
	public String foreignTable1 = " ";
	public Long foreignKey1 = new Long("0");
	public String foreignTable2 = " ";
	public Integer foreignKey2 = new Integer("0");
	public String remarks = " ";
	public Timestamp time = TimeFormat.getTimestamp();
	public String state = AuditTrailBean.STATE_NONE;
	public String status = " ";

	public AuditTrailObject()
	{
		time = TimeFormat.getTimestamp();
	}
}
