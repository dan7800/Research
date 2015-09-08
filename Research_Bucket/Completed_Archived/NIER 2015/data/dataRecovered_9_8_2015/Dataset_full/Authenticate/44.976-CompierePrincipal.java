/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.1 $ $Date: 2001/10/20 21:13:47 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param realm The Realm that owns this principal
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * Return a String representation of this object, which exposes only
	 * information that should be public.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * Does the user represented by this Principal possess the specified role?
	 *
	 * @param role Role to be tested
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.1 $ $Date: 2001/10/20 21:13:47 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param realm The Realm that owns this principal
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * Return a String representation of this object, which exposes only
	 * information that should be public.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * Does the user represented by this Principal possess the specified role?
	 *
	 * @param role Role to be tested
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <strong>java.security.Principal</strong>
 *
 * @author Jorg Janke
 * @version $Revision: 1.2 $ $Date: 2003/07/06 18:39:44 $
 */
public class CompierePrincipal implements Principal
{
	/**
	 * Construct a new Principal, associated with the specified Realm, for the
	 * specified username and password, with the specified role names
	 * (as Strings).
	 *
	 * @param name The username of the user represented by this Principal
	 * @param password Credentials used to authenticate this user
	 * @param roles List of roles (must be Strings) possessed by this user
	 */
	public CompierePrincipal (String name, String password, List roles)
	{
		super();
		m_name = name;
		m_password = password;
		if (roles != null)
		{
			m_roles = new String[roles.size()];
			m_roles = (String[]) roles.toArray(m_roles);
			if (m_roles.length > 0)
				Arrays.sort(m_roles);
		}
	}   //  CompierePrincipal


	/** Username of the user represented by this Principal  */
	private String      m_name = null;
	/** Authentication credentials for the user represented by this Principal   */
	private String      m_password = null;
	/** Array of roles associated with this user            */
	private String      m_roles[] = new String[0];


	/**
	 *  Get Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_name;
	}   //  getName

	/**
	 *  Get Password
	 * 	@return password
	 */
	String getPassword()
	{
		return m_password;
	}   //  getPassword

	/**
	 *  Get Roles
	 * 	@return roles array
	 */
	String[] getRoles()
	{
		return m_roles;
	}   //  getRoles

	/**
	 * 	Return a String representation of this object, which exposes only
	 * 	information that should be public.
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("CompierePrincipal[");
		sb.append(m_name).append(": ");
		for (int i = 0; i < m_roles.length; i++)
			sb.append(m_roles[i]).append(" ");
		sb.append("]");
		return (sb.toString());
	}   //  toString

	/**
	 * 	Does the user represented by this Principal possess the specified role?
	 *
	 * 	@param role Role to be tested
	 * 	@return true if has role
	 */
	public boolean hasRole (String role)
	{
		if (role == null)
			return false;
		return (Arrays.binarySearch(m_roles, role) >= 0);
	}   //  hasRole

}   //  CompierePrincipal
