/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Role
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.791 */
public class X_AD_Role extends PO
{
/** Standard Constructor */
public X_AD_Role (Properties ctx, int AD_Role_ID)
{
super (ctx, AD_Role_ID);
/** if (AD_Role_ID == 0)
{
setAD_Role_ID (0);
setIsAccessAllOrgs (false);	// N
setIsCanApproveOwnDoc (false);
setIsCanExport (true);	// Y
setIsCanReport (true);	// Y
setIsManual (false);
setIsPersonalAccess (false);	// N
setIsPersonalLock (false);	// N
setIsShowAcct (false);	// N
setName (null);
setUserLevel (null);
}
 */
}
/** Load Constructor */
public X_AD_Role (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=156 */
public static final int Table_ID=156;

/** TableName=AD_Role */
public static final String Table_Name="AD_Role";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Role[").append(getID()).append("]");
return sb.toString();
}
/** Set Role.
Responsibility Role */
public void setAD_Role_ID (int AD_Role_ID)
{
set_ValueNoCheck ("AD_Role_ID", new Integer(AD_Role_ID));
}
/** Get Role.
Responsibility Role */
public int getAD_Role_ID() 
{
Integer ii = (Integer)get_Value("AD_Role_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int AD_TREE_MENU_ID_AD_Reference_ID=184;
/** Set Primary Tree Menu */
public void setAD_Tree_Menu_ID (int AD_Tree_Menu_ID)
{
if (AD_Tree_Menu_ID == 0) set_Value ("AD_Tree_Menu_ID", null);
 else 
set_Value ("AD_Tree_Menu_ID", new Integer(AD_Tree_Menu_ID));
}
/** Get Primary Tree Menu */
public int getAD_Tree_Menu_ID() 
{
Integer ii = (Integer)get_Value("AD_Tree_Menu_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Approval Amount.
The approval amount limit for this role */
public void setAmtApproval (BigDecimal AmtApproval)
{
set_Value ("AmtApproval", AmtApproval);
}
/** Get Approval Amount.
The approval amount limit for this role */
public BigDecimal getAmtApproval() 
{
BigDecimal bd = (BigDecimal)get_Value("AmtApproval");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
if (C_Currency_ID == 0) set_Value ("C_Currency_ID", null);
 else 
set_Value ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Description.
Optional short description of the record */
public void setDescription (String Description)
{
if (Description != null && Description.length() > 255)
{
log.warn("setDescription - length > 255 - truncated");
Description = Description.substring(0,254);
}
set_Value ("Description", Description);
}
/** Get Description.
Optional short description of the record */
public String getDescription() 
{
return (String)get_Value("Description");
}
/** Set Access all Orgs.
Access all Organizations (no org access control) of the client */
public void setIsAccessAllOrgs (boolean IsAccessAllOrgs)
{
set_Value ("IsAccessAllOrgs", new Boolean(IsAccessAllOrgs));
}
/** Get Access all Orgs.
Access all Organizations (no org access control) of the client */
public boolean isAccessAllOrgs() 
{
Object oo = get_Value("IsAccessAllOrgs");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Approve own Documents.
Users with this role can approve their own documents */
public void setIsCanApproveOwnDoc (boolean IsCanApproveOwnDoc)
{
set_Value ("IsCanApproveOwnDoc", new Boolean(IsCanApproveOwnDoc));
}
/** Get Approve own Documents.
Users with this role can approve their own documents */
public boolean isCanApproveOwnDoc() 
{
Object oo = get_Value("IsCanApproveOwnDoc");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Can Export.
Users with this role can export data */
public void setIsCanExport (boolean IsCanExport)
{
set_Value ("IsCanExport", new Boolean(IsCanExport));
}
/** Get Can Export.
Users with this role can export data */
public boolean isCanExport() 
{
Object oo = get_Value("IsCanExport");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Can Report.
Users with this role can create reports */
public void setIsCanReport (boolean IsCanReport)
{
set_Value ("IsCanReport", new Boolean(IsCanReport));
}
/** Get Can Report.
Users with this role can create reports */
public boolean isCanReport() 
{
Object oo = get_Value("IsCanReport");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Manual.
This is a manual process */
public void setIsManual (boolean IsManual)
{
set_Value ("IsManual", new Boolean(IsManual));
}
/** Get Manual.
This is a manual process */
public boolean isManual() 
{
Object oo = get_Value("IsManual");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Personal Access.
Allow access to all personal records */
public void setIsPersonalAccess (boolean IsPersonalAccess)
{
set_Value ("IsPersonalAccess", new Boolean(IsPersonalAccess));
}
/** Get Personal Access.
Allow access to all personal records */
public boolean isPersonalAccess() 
{
Object oo = get_Value("IsPersonalAccess");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Personal Lock.
Allow users with role to lock access to personal records */
public void setIsPersonalLock (boolean IsPersonalLock)
{
set_Value ("IsPersonalLock", new Boolean(IsPersonalLock));
}
/** Get Personal Lock.
Allow users with role to lock access to personal records */
public boolean isPersonalLock() 
{
Object oo = get_Value("IsPersonalLock");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Show Accounting.
Users with this role can see accounting information */
public void setIsShowAcct (boolean IsShowAcct)
{
set_Value ("IsShowAcct", new Boolean(IsShowAcct));
}
/** Get Show Accounting.
Users with this role can see accounting information */
public boolean isShowAcct() 
{
Object oo = get_Value("IsShowAcct");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Name.
Alphanumeric identifier of the entity */
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
if (Name.length() > 60)
{
log.warn("setName - length > 60 - truncated");
Name = Name.substring(0,59);
}
set_Value ("Name", Name);
}
/** Get Name.
Alphanumeric identifier of the entity */
public String getName() 
{
return (String)get_Value("Name");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getName());
}
public static final int SUPERVISOR_ID_AD_Reference_ID=286;
/** Set Supervisor.
Supervisor for this user/organization - used for escalation and approval */
public void setSupervisor_ID (int Supervisor_ID)
{
if (Supervisor_ID == 0) set_Value ("Supervisor_ID", null);
 else 
set_Value ("Supervisor_ID", new Integer(Supervisor_ID));
}
/** Get Supervisor.
Supervisor for this user/organization - used for escalation and approval */
public int getSupervisor_ID() 
{
Integer ii = (Integer)get_Value("Supervisor_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USERLEVEL_AD_Reference_ID=226;
/** System = S   */
public static final String USERLEVEL_System = "S  ";
/** Client =  C  */
public static final String USERLEVEL_Client = " C ";
/** Organization =   O */
public static final String USERLEVEL_Organization = "  O";
/** Client+Organization =  CO */
public static final String USERLEVEL_ClientPlusOrganization = " CO";
/** Set User Level.
System Client Organization */
public void setUserLevel (String UserLevel)
{
if (UserLevel.equals("S  ") || UserLevel.equals(" C ") || UserLevel.equals("  O") || UserLevel.equals(" CO"));
 else throw new IllegalArgumentException ("UserLevel Invalid value - Reference_ID=226 - S   -  C  -   O -  CO");
if (UserLevel == null) throw new IllegalArgumentException ("UserLevel is mandatory");
if (UserLevel.length() > 3)
{
log.warn("setUserLevel - length > 3 - truncated");
UserLevel = UserLevel.substring(0,2);
}
set_Value ("UserLevel", UserLevel);
}
/** Get User Level.
System Client Organization */
public String getUserLevel() 
{
return (String)get_Value("UserLevel");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Role
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.791 */
public class X_AD_Role extends PO
{
/** Standard Constructor */
public X_AD_Role (Properties ctx, int AD_Role_ID)
{
super (ctx, AD_Role_ID);
/** if (AD_Role_ID == 0)
{
setAD_Role_ID (0);
setIsAccessAllOrgs (false);	// N
setIsCanApproveOwnDoc (false);
setIsCanExport (true);	// Y
setIsCanReport (true);	// Y
setIsManual (false);
setIsPersonalAccess (false);	// N
setIsPersonalLock (false);	// N
setIsShowAcct (false);	// N
setName (null);
setUserLevel (null);
}
 */
}
/** Load Constructor */
public X_AD_Role (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=156 */
public static final int Table_ID=156;

/** TableName=AD_Role */
public static final String Table_Name="AD_Role";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Role[").append(getID()).append("]");
return sb.toString();
}
/** Set Role.
Responsibility Role */
public void setAD_Role_ID (int AD_Role_ID)
{
set_ValueNoCheck ("AD_Role_ID", new Integer(AD_Role_ID));
}
/** Get Role.
Responsibility Role */
public int getAD_Role_ID() 
{
Integer ii = (Integer)get_Value("AD_Role_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int AD_TREE_MENU_ID_AD_Reference_ID=184;
/** Set Primary Tree Menu */
public void setAD_Tree_Menu_ID (int AD_Tree_Menu_ID)
{
if (AD_Tree_Menu_ID == 0) set_Value ("AD_Tree_Menu_ID", null);
 else 
set_Value ("AD_Tree_Menu_ID", new Integer(AD_Tree_Menu_ID));
}
/** Get Primary Tree Menu */
public int getAD_Tree_Menu_ID() 
{
Integer ii = (Integer)get_Value("AD_Tree_Menu_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Approval Amount.
The approval amount limit for this role */
public void setAmtApproval (BigDecimal AmtApproval)
{
set_Value ("AmtApproval", AmtApproval);
}
/** Get Approval Amount.
The approval amount limit for this role */
public BigDecimal getAmtApproval() 
{
BigDecimal bd = (BigDecimal)get_Value("AmtApproval");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
if (C_Currency_ID == 0) set_Value ("C_Currency_ID", null);
 else 
set_Value ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Description.
Optional short description of the record */
public void setDescription (String Description)
{
if (Description != null && Description.length() > 255)
{
log.warn("setDescription - length > 255 - truncated");
Description = Description.substring(0,254);
}
set_Value ("Description", Description);
}
/** Get Description.
Optional short description of the record */
public String getDescription() 
{
return (String)get_Value("Description");
}
/** Set Access all Orgs.
Access all Organizations (no org access control) of the client */
public void setIsAccessAllOrgs (boolean IsAccessAllOrgs)
{
set_Value ("IsAccessAllOrgs", new Boolean(IsAccessAllOrgs));
}
/** Get Access all Orgs.
Access all Organizations (no org access control) of the client */
public boolean isAccessAllOrgs() 
{
Object oo = get_Value("IsAccessAllOrgs");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Approve own Documents.
Users with this role can approve their own documents */
public void setIsCanApproveOwnDoc (boolean IsCanApproveOwnDoc)
{
set_Value ("IsCanApproveOwnDoc", new Boolean(IsCanApproveOwnDoc));
}
/** Get Approve own Documents.
Users with this role can approve their own documents */
public boolean isCanApproveOwnDoc() 
{
Object oo = get_Value("IsCanApproveOwnDoc");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Can Export.
Users with this role can export data */
public void setIsCanExport (boolean IsCanExport)
{
set_Value ("IsCanExport", new Boolean(IsCanExport));
}
/** Get Can Export.
Users with this role can export data */
public boolean isCanExport() 
{
Object oo = get_Value("IsCanExport");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Can Report.
Users with this role can create reports */
public void setIsCanReport (boolean IsCanReport)
{
set_Value ("IsCanReport", new Boolean(IsCanReport));
}
/** Get Can Report.
Users with this role can create reports */
public boolean isCanReport() 
{
Object oo = get_Value("IsCanReport");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Manual.
This is a manual process */
public void setIsManual (boolean IsManual)
{
set_Value ("IsManual", new Boolean(IsManual));
}
/** Get Manual.
This is a manual process */
public boolean isManual() 
{
Object oo = get_Value("IsManual");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Personal Access.
Allow access to all personal records */
public void setIsPersonalAccess (boolean IsPersonalAccess)
{
set_Value ("IsPersonalAccess", new Boolean(IsPersonalAccess));
}
/** Get Personal Access.
Allow access to all personal records */
public boolean isPersonalAccess() 
{
Object oo = get_Value("IsPersonalAccess");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Personal Lock.
Allow users with role to lock access to personal records */
public void setIsPersonalLock (boolean IsPersonalLock)
{
set_Value ("IsPersonalLock", new Boolean(IsPersonalLock));
}
/** Get Personal Lock.
Allow users with role to lock access to personal records */
public boolean isPersonalLock() 
{
Object oo = get_Value("IsPersonalLock");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Show Accounting.
Users with this role can see accounting information */
public void setIsShowAcct (boolean IsShowAcct)
{
set_Value ("IsShowAcct", new Boolean(IsShowAcct));
}
/** Get Show Accounting.
Users with this role can see accounting information */
public boolean isShowAcct() 
{
Object oo = get_Value("IsShowAcct");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Name.
Alphanumeric identifier of the entity */
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
if (Name.length() > 60)
{
log.warn("setName - length > 60 - truncated");
Name = Name.substring(0,59);
}
set_Value ("Name", Name);
}
/** Get Name.
Alphanumeric identifier of the entity */
public String getName() 
{
return (String)get_Value("Name");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getName());
}
public static final int SUPERVISOR_ID_AD_Reference_ID=286;
/** Set Supervisor.
Supervisor for this user/organization - used for escalation and approval */
public void setSupervisor_ID (int Supervisor_ID)
{
if (Supervisor_ID == 0) set_Value ("Supervisor_ID", null);
 else 
set_Value ("Supervisor_ID", new Integer(Supervisor_ID));
}
/** Get Supervisor.
Supervisor for this user/organization - used for escalation and approval */
public int getSupervisor_ID() 
{
Integer ii = (Integer)get_Value("Supervisor_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USERLEVEL_AD_Reference_ID=226;
/** System = S   */
public static final String USERLEVEL_System = "S  ";
/** Client =  C  */
public static final String USERLEVEL_Client = " C ";
/** Organization =   O */
public static final String USERLEVEL_Organization = "  O";
/** Client+Organization =  CO */
public static final String USERLEVEL_ClientPlusOrganization = " CO";
/** Set User Level.
System Client Organization */
public void setUserLevel (String UserLevel)
{
if (UserLevel.equals("S  ") || UserLevel.equals(" C ") || UserLevel.equals("  O") || UserLevel.equals(" CO"));
 else throw new IllegalArgumentException ("UserLevel Invalid value - Reference_ID=226 - S   -  C  -   O -  CO");
if (UserLevel == null) throw new IllegalArgumentException ("UserLevel is mandatory");
if (UserLevel.length() > 3)
{
log.warn("setUserLevel - length > 3 - truncated");
UserLevel = UserLevel.substring(0,2);
}
set_Value ("UserLevel", UserLevel);
}
/** Get User Level.
System Client Organization */
public String getUserLevel() 
{
return (String)get_Value("UserLevel");
}
}
