/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.math.BigDecimal;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class MemberCardAuditTrailNut
{
	private static String strClassName = "MemberCardAuditTrailNut";

	public static MemberCardAuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			MemberCardAuditTrailHome lEJBHome = (MemberCardAuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/customer/MemberCardAuditTrail"), MemberCardAuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MemberCardAuditTrail getHandle(Long pkid)
	{
		return (MemberCardAuditTrail) getHandle(getHome(), pkid);
	}

	public static MemberCardAuditTrail getHandle(MemberCardAuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (MemberCardAuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static MemberCardAuditTrail fnCreate(MemberCardAuditTrailObject valObj)
	{
		MemberCardAuditTrail ejb = null;
		MemberCardAuditTrailHome home = getHome();
		try
		{
			ejb = home.create(valObj);
			valObj.pkid = ejb.getPkid();
			return ejb;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("MemberCardAuditTrailNut: " + "Cannot create this MemberCardAuditTrail");
			return (MemberCardAuditTrail) null;
		}
	}

	// ///////////////////////////////////////////////////
	public static MemberCardAuditTrailObject getObject(Long lPkid)
	{
		MemberCardAuditTrailObject valueObj = new MemberCardAuditTrailObject();
		MemberCardAuditTrail objEJB = getHandle(lPkid);
		try
		{
			valueObj = objEJB.getObject();
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
		}
		return (MemberCardAuditTrailObject) valueObj;
	}

	// /////////////////////////////////////////////////////////
	public static Collection getObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			MemberCardAuditTrailHome home = getHome();
			result = home.getObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.math.BigDecimal;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class MemberCardAuditTrailNut
{
	private static String strClassName = "MemberCardAuditTrailNut";

	public static MemberCardAuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			MemberCardAuditTrailHome lEJBHome = (MemberCardAuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/customer/MemberCardAuditTrail"), MemberCardAuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MemberCardAuditTrail getHandle(Long pkid)
	{
		return (MemberCardAuditTrail) getHandle(getHome(), pkid);
	}

	public static MemberCardAuditTrail getHandle(MemberCardAuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (MemberCardAuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static MemberCardAuditTrail fnCreate(MemberCardAuditTrailObject valObj)
	{
		MemberCardAuditTrail ejb = null;
		MemberCardAuditTrailHome home = getHome();
		try
		{
			ejb = home.create(valObj);
			valObj.pkid = ejb.getPkid();
			return ejb;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("MemberCardAuditTrailNut: " + "Cannot create this MemberCardAuditTrail");
			return (MemberCardAuditTrail) null;
		}
	}

	// ///////////////////////////////////////////////////
	public static MemberCardAuditTrailObject getObject(Long lPkid)
	{
		MemberCardAuditTrailObject valueObj = new MemberCardAuditTrailObject();
		MemberCardAuditTrail objEJB = getHandle(lPkid);
		try
		{
			valueObj = objEJB.getObject();
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
		}
		return (MemberCardAuditTrailObject) valueObj;
	}

	// /////////////////////////////////////////////////////////
	public static Collection getObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			MemberCardAuditTrailHome home = getHome();
			result = home.getObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.math.BigDecimal;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class MemberCardAuditTrailNut
{
	private static String strClassName = "MemberCardAuditTrailNut";

	public static MemberCardAuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			MemberCardAuditTrailHome lEJBHome = (MemberCardAuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/customer/MemberCardAuditTrail"), MemberCardAuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MemberCardAuditTrail getHandle(Long pkid)
	{
		return (MemberCardAuditTrail) getHandle(getHome(), pkid);
	}

	public static MemberCardAuditTrail getHandle(MemberCardAuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (MemberCardAuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static MemberCardAuditTrail fnCreate(MemberCardAuditTrailObject valObj)
	{
		MemberCardAuditTrail ejb = null;
		MemberCardAuditTrailHome home = getHome();
		try
		{
			ejb = home.create(valObj);
			valObj.pkid = ejb.getPkid();
			return ejb;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("MemberCardAuditTrailNut: " + "Cannot create this MemberCardAuditTrail");
			return (MemberCardAuditTrail) null;
		}
	}

	// ///////////////////////////////////////////////////
	public static MemberCardAuditTrailObject getObject(Long lPkid)
	{
		MemberCardAuditTrailObject valueObj = new MemberCardAuditTrailObject();
		MemberCardAuditTrail objEJB = getHandle(lPkid);
		try
		{
			valueObj = objEJB.getObject();
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
		}
		return (MemberCardAuditTrailObject) valueObj;
	}

	// /////////////////////////////////////////////////////////
	public static Collection getObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			MemberCardAuditTrailHome home = getHome();
			result = home.getObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.math.BigDecimal;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class MemberCardAuditTrailNut
{
	private static String strClassName = "MemberCardAuditTrailNut";

	public static MemberCardAuditTrailHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			MemberCardAuditTrailHome lEJBHome = (MemberCardAuditTrailHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/customer/MemberCardAuditTrail"), MemberCardAuditTrailHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MemberCardAuditTrail getHandle(Long pkid)
	{
		return (MemberCardAuditTrail) getHandle(getHome(), pkid);
	}

	public static MemberCardAuditTrail getHandle(MemberCardAuditTrailHome lEJBHome, Long pkid)
	{
		try
		{
			return (MemberCardAuditTrail) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static MemberCardAuditTrail fnCreate(MemberCardAuditTrailObject valObj)
	{
		MemberCardAuditTrail ejb = null;
		MemberCardAuditTrailHome home = getHome();
		try
		{
			ejb = home.create(valObj);
			valObj.pkid = ejb.getPkid();
			return ejb;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("MemberCardAuditTrailNut: " + "Cannot create this MemberCardAuditTrail");
			return (MemberCardAuditTrail) null;
		}
	}

	// ///////////////////////////////////////////////////
	public static MemberCardAuditTrailObject getObject(Long lPkid)
	{
		MemberCardAuditTrailObject valueObj = new MemberCardAuditTrailObject();
		MemberCardAuditTrail objEJB = getHandle(lPkid);
		try
		{
			valueObj = objEJB.getObject();
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
		}
		return (MemberCardAuditTrailObject) valueObj;
	}

	// /////////////////////////////////////////////////////////
	public static Collection getObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			MemberCardAuditTrailHome home = getHome();
			result = home.getObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
}
