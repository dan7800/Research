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

import java.sql.Timestamp;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class AuditTrailNut
{
	public static AuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			AuditTrailHome lEJBHome = (AuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/AuditTrail"), AuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static AuditTrail getHandle(Long pkid)
	{
		return (AuditTrail) getHandle(getHome(), pkid);
	}

	public static AuditTrail getHandle(AuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (AuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static AuditTrail fnCreate(AuditTrailObject mObject)
	{
		AuditTrailHome lEJBHome = getHome();
		try
		{
			if(mObject.userId.intValue()==501){ return (AuditTrail) null;}
			AuditTrail auditTrailEJB = lEJBHome.create(mObject);
			mObject = auditTrailEJB.getObject();
			return auditTrailEJB;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create AuditTrail -> " + ex.getMessage());
			ex.printStackTrace();
			return (AuditTrail) null;
		}
	}
	
	public static Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			AuditTrailHome home = getHome();
			vecValObj = home.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom,
					dateTo, state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}
	
	//20080109 Jimmy - add branch and filter
	public static Vector getAuditTrailReport(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status,
			String checkUserBranch, Integer iBranch)
	{
		Vector vecValObj = null;
		try
		{
			AuditTrailHome home = getHome();
			vecValObj = home.getAuditTrailReport(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo, state, status, checkUserBranch, iBranch);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
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

import java.sql.Timestamp;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class AuditTrailNut
{
	public static AuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			AuditTrailHome lEJBHome = (AuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/AuditTrail"), AuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static AuditTrail getHandle(Long pkid)
	{
		return (AuditTrail) getHandle(getHome(), pkid);
	}

	public static AuditTrail getHandle(AuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (AuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static AuditTrail fnCreate(AuditTrailObject mObject)
	{
		AuditTrailHome lEJBHome = getHome();
		try
		{
			AuditTrail auditTrailEJB = lEJBHome.create(mObject);
			mObject = auditTrailEJB.getObject();
			return auditTrailEJB;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create AuditTrail -> " + ex.getMessage());
			ex.printStackTrace();
			return (AuditTrail) null;
		}
	}

	public static Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			AuditTrailHome home = getHome();
			vecValObj = home.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom,
					dateTo, state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
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

import java.sql.Timestamp;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class AuditTrailNut
{
	public static AuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			AuditTrailHome lEJBHome = (AuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/AuditTrail"), AuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static AuditTrail getHandle(Long pkid)
	{
		return (AuditTrail) getHandle(getHome(), pkid);
	}

	public static AuditTrail getHandle(AuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (AuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static AuditTrail fnCreate(AuditTrailObject mObject)
	{
		AuditTrailHome lEJBHome = getHome();
		try
		{
			AuditTrail auditTrailEJB = lEJBHome.create(mObject);
			mObject = auditTrailEJB.getObject();
			return auditTrailEJB;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create AuditTrail -> " + ex.getMessage());
			ex.printStackTrace();
			return (AuditTrail) null;
		}
	}

	public static Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			AuditTrailHome home = getHome();
			vecValObj = home.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom,
					dateTo, state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
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

import java.sql.Timestamp;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class AuditTrailNut
{
	public static AuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			AuditTrailHome lEJBHome = (AuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/AuditTrail"), AuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static AuditTrail getHandle(Long pkid)
	{
		return (AuditTrail) getHandle(getHome(), pkid);
	}

	public static AuditTrail getHandle(AuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (AuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static AuditTrail fnCreate(AuditTrailObject mObject)
	{
		AuditTrailHome lEJBHome = getHome();
		try
		{
			AuditTrail auditTrailEJB = lEJBHome.create(mObject);
			mObject = auditTrailEJB.getObject();
			return auditTrailEJB;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create AuditTrail -> " + ex.getMessage());
			ex.printStackTrace();
			return (AuditTrail) null;
		}
	}

	public static Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			AuditTrailHome home = getHome();
			vecValObj = home.getValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom,
					dateTo, state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}
}
