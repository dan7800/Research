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

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class ConfigWorkingCalendarNut
{
	public static ConfigWorkingCalendarHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			ConfigWorkingCalendarHome lEJBHome = (ConfigWorkingCalendarHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/ConfigWorkingCalendar"), ConfigWorkingCalendarHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static ConfigWorkingCalendar getHandle(Integer pkid)
	{
		return (ConfigWorkingCalendar) getHandle(getHome(), pkid);
	}

	public static ConfigWorkingCalendar getHandle(ConfigWorkingCalendarHome lEJBHome, Integer pkid)
	{
		try
		{
			return (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static ConfigWorkingCalendar fnCreate(ConfigWorkingCalendarObject mObject)
	{
		ConfigWorkingCalendarHome lEJBHome = getHome();
		try
		{
			ConfigWorkingCalendar ejb = lEJBHome.create(mObject);
			mObject = ejb.getObject();
			return ejb;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create ConfigWorkingCalendar -> " + ex.getMessage());
			ex.printStackTrace();
			return (ConfigWorkingCalendar) null;
		}
	}
	
	public static ConfigWorkingCalendarObject getObjectByCalendarYear(String str)
	{
		ConfigWorkingCalendarObject valObj = null;
		try
		{
			ConfigWorkingCalendar ejb;
			ConfigWorkingCalendarHome lEJBHome = getHome();
			Collection coll = lEJBHome.getPkIdByCode(str);
			Integer pkid = (Integer) coll.iterator().next();
			ejb = (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
			valObj = ejb.getObject();
		} catch (Exception ex)
		{
			Log.printDebug("PricingMatrixNut:" + ex.getMessage());
		}
		return valObj;
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

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class ConfigWorkingCalendarNut
{
	public static ConfigWorkingCalendarHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			ConfigWorkingCalendarHome lEJBHome = (ConfigWorkingCalendarHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/ConfigWorkingCalendar"), ConfigWorkingCalendarHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static ConfigWorkingCalendar getHandle(Integer pkid)
	{
		return (ConfigWorkingCalendar) getHandle(getHome(), pkid);
	}

	public static ConfigWorkingCalendar getHandle(ConfigWorkingCalendarHome lEJBHome, Integer pkid)
	{
		try
		{
			return (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static ConfigWorkingCalendar fnCreate(ConfigWorkingCalendarObject mObject)
	{
		ConfigWorkingCalendarHome lEJBHome = getHome();
		try
		{
			ConfigWorkingCalendar ejb = lEJBHome.create(mObject);
			mObject = ejb.getObject();
			return ejb;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create ConfigWorkingCalendar -> " + ex.getMessage());
			ex.printStackTrace();
			return (ConfigWorkingCalendar) null;
		}
	}
	
	public static ConfigWorkingCalendarObject getObjectByCalendarYear(String str)
	{
		ConfigWorkingCalendarObject valObj = null;
		try
		{
			ConfigWorkingCalendar ejb;
			ConfigWorkingCalendarHome lEJBHome = getHome();
			Collection coll = lEJBHome.getPkIdByCode(str);
			Integer pkid = (Integer) coll.iterator().next();
			ejb = (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
			valObj = ejb.getObject();
		} catch (Exception ex)
		{
			Log.printDebug("PricingMatrixNut:" + ex.getMessage());
		}
		return valObj;
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

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class ConfigWorkingCalendarNut
{
	public static ConfigWorkingCalendarHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			ConfigWorkingCalendarHome lEJBHome = (ConfigWorkingCalendarHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/ConfigWorkingCalendar"), ConfigWorkingCalendarHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static ConfigWorkingCalendar getHandle(Integer pkid)
	{
		return (ConfigWorkingCalendar) getHandle(getHome(), pkid);
	}

	public static ConfigWorkingCalendar getHandle(ConfigWorkingCalendarHome lEJBHome, Integer pkid)
	{
		try
		{
			return (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static ConfigWorkingCalendar fnCreate(ConfigWorkingCalendarObject mObject)
	{
		ConfigWorkingCalendarHome lEJBHome = getHome();
		try
		{
			ConfigWorkingCalendar ejb = lEJBHome.create(mObject);
			mObject = ejb.getObject();
			return ejb;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create ConfigWorkingCalendar -> " + ex.getMessage());
			ex.printStackTrace();
			return (ConfigWorkingCalendar) null;
		}
	}
	
	public static ConfigWorkingCalendarObject getObjectByCalendarYear(String str)
	{
		ConfigWorkingCalendarObject valObj = null;
		try
		{
			ConfigWorkingCalendar ejb;
			ConfigWorkingCalendarHome lEJBHome = getHome();
			Collection coll = lEJBHome.getPkIdByCode(str);
			Integer pkid = (Integer) coll.iterator().next();
			ejb = (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
			valObj = ejb.getObject();
		} catch (Exception ex)
		{
			Log.printDebug("PricingMatrixNut:" + ex.getMessage());
		}
		return valObj;
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

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.vlee.util.Log;

public class ConfigWorkingCalendarNut
{
	public static ConfigWorkingCalendarHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			ConfigWorkingCalendarHome lEJBHome = (ConfigWorkingCalendarHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/user/ConfigWorkingCalendar"), ConfigWorkingCalendarHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caugth exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static ConfigWorkingCalendar getHandle(Integer pkid)
	{
		return (ConfigWorkingCalendar) getHandle(getHome(), pkid);
	}

	public static ConfigWorkingCalendar getHandle(ConfigWorkingCalendarHome lEJBHome, Integer pkid)
	{
		try
		{
			return (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static ConfigWorkingCalendar fnCreate(ConfigWorkingCalendarObject mObject)
	{
		ConfigWorkingCalendarHome lEJBHome = getHome();
		try
		{
			ConfigWorkingCalendar ejb = lEJBHome.create(mObject);
			mObject = ejb.getObject();
			return ejb;
		} catch (Exception ex)
		{
			Log.printDebug("AuditTrailNut: Failed to create ConfigWorkingCalendar -> " + ex.getMessage());
			ex.printStackTrace();
			return (ConfigWorkingCalendar) null;
		}
	}
	
	public static ConfigWorkingCalendarObject getObjectByCalendarYear(String str)
	{
		ConfigWorkingCalendarObject valObj = null;
		try
		{
			ConfigWorkingCalendar ejb;
			ConfigWorkingCalendarHome lEJBHome = getHome();
			Collection coll = lEJBHome.getPkIdByCode(str);
			Integer pkid = (Integer) coll.iterator().next();
			ejb = (ConfigWorkingCalendar) lEJBHome.findByPrimaryKey(pkid);
			valObj = ejb.getObject();
		} catch (Exception ex)
		{
			Log.printDebug("PricingMatrixNut:" + ex.getMessage());
		}
		return valObj;
	}
}
