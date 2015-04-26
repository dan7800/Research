/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.73 2004/05/20 05:59:12 jjanke Exp $ */
public class X_C_BP_EDI extends PO
{
/** Standard Constructor */
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 */
}
/** Load Constructor */
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=366;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_SEQUENCE_ID_AD_Reference_ID=128;
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_Value ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set EDI Definition.
Electronic Data Interchange */
public void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
set_ValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
/** Get EDI Definition.
Electronic Data Interchange */
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)get_Value("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
set_Value ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
/** Get Business Partner .
Identifies a Business Partner */
public int getC_BPartner_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Customer No.
EDI Identification Number  */
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
if (CustomerNo.length() > 20)
{
log.warn("setCustomerNo - length > 20 - truncated");
CustomerNo = CustomerNo.substring(0,19);
}
set_Value ("CustomerNo", CustomerNo);
}
/** Get Customer No.
EDI Identification Number  */
public String getCustomerNo() 
{
return (String)get_Value("CustomerNo");
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
public static final int EDITYPE_AD_Reference_ID=201;
/** ASC X12  = X */
public static final String EDITYPE_ASCX12 = "X";
/** EDIFACT = E */
public static final String EDITYPE_EDIFACT = "E";
/** Email EDI = M */
public static final String EDITYPE_EmailEDI = "M";
/** Set EDI Type */
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
if (EDIType.length() > 1)
{
log.warn("setEDIType - length > 1 - truncated");
EDIType = EDIType.substring(0,0);
}
set_Value ("EDIType", EDIType);
}
/** Get EDI Type */
public String getEDIType() 
{
return (String)get_Value("EDIType");
}
/** Set Error Email.
Email address to send error messages to */
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
if (Email_Error_To.length() > 60)
{
log.warn("setEmail_Error_To - length > 60 - truncated");
Email_Error_To = Email_Error_To.substring(0,59);
}
set_Value ("Email_Error_To", Email_Error_To);
}
/** Get Error Email.
Email address to send error messages to */
public String getEmail_Error_To() 
{
return (String)get_Value("Email_Error_To");
}
/** Set From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public void setEmail_From (String Email_From)
{
if (Email_From != null && Email_From.length() > 60)
{
log.warn("setEmail_From - length > 60 - truncated");
Email_From = Email_From.substring(0,59);
}
set_Value ("Email_From", Email_From);
}
/** Get From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public String getEmail_From() 
{
return (String)get_Value("Email_From");
}
/** Set From Email Password.
Password of the sending Email address */
public void setEmail_From_Pwd (String Email_From_Pwd)
{
if (Email_From_Pwd != null && Email_From_Pwd.length() > 20)
{
log.warn("setEmail_From_Pwd - length > 20 - truncated");
Email_From_Pwd = Email_From_Pwd.substring(0,19);
}
set_Value ("Email_From_Pwd", Email_From_Pwd);
}
/** Get From Email Password.
Password of the sending Email address */
public String getEmail_From_Pwd() 
{
return (String)get_Value("Email_From_Pwd");
}
/** Set From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public void setEmail_From_Uid (String Email_From_Uid)
{
if (Email_From_Uid != null && Email_From_Uid.length() > 20)
{
log.warn("setEmail_From_Uid - length > 20 - truncated");
Email_From_Uid = Email_From_Uid.substring(0,19);
}
set_Value ("Email_From_Uid", Email_From_Uid);
}
/** Get From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public String getEmail_From_Uid() 
{
return (String)get_Value("Email_From_Uid");
}
/** Set Info Email.
Email address to send informational messages and copies */
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
if (Email_Info_To.length() > 60)
{
log.warn("setEmail_Info_To - length > 60 - truncated");
Email_Info_To = Email_Info_To.substring(0,59);
}
set_Value ("Email_Info_To", Email_Info_To);
}
/** Get Info Email.
Email address to send informational messages and copies */
public String getEmail_Info_To() 
{
return (String)get_Value("Email_Info_To");
}
/** Set To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public void setEmail_To (String Email_To)
{
if (Email_To != null && Email_To.length() > 60)
{
log.warn("setEmail_To - length > 60 - truncated");
Email_To = Email_To.substring(0,59);
}
set_Value ("Email_To", Email_To);
}
/** Get To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public String getEmail_To() 
{
return (String)get_Value("Email_To");
}
/** Set Activate Audit.
Activate Audit Trail of what numbers are generated */
public void setIsAudited (boolean IsAudited)
{
set_Value ("IsAudited", new Boolean(IsAudited));
}
/** Get Activate Audit.
Activate Audit Trail of what numbers are generated */
public boolean isAudited() 
{
Object oo = get_Value("IsAudited");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Info.
Send informational messages and copies */
public void setIsInfoSent (boolean IsInfoSent)
{
set_Value ("IsInfoSent", new Boolean(IsInfoSent));
}
/** Get Send Info.
Send informational messages and copies */
public boolean isInfoSent() 
{
Object oo = get_Value("IsInfoSent");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_Value ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Received Inquiry Reply */
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
set_Value ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
/** Get Received Inquiry Reply */
public boolean isReceiveInquiryReply() 
{
Object oo = get_Value("ReceiveInquiryReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Receive Order Reply */
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
set_Value ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
/** Get Receive Order Reply */
public boolean isReceiveOrderReply() 
{
Object oo = get_Value("ReceiveOrderReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Inquiry.
Quantity Availability Inquiry */
public void setSendInquiry (boolean SendInquiry)
{
set_Value ("SendInquiry", new Boolean(SendInquiry));
}
/** Get Send Inquiry.
Quantity Availability Inquiry */
public boolean isSendInquiry() 
{
Object oo = get_Value("SendInquiry");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Order */
public void setSendOrder (boolean SendOrder)
{
set_Value ("SendOrder", new Boolean(SendOrder));
}
/** Get Send Order */
public boolean isSendOrder() 
{
Object oo = get_Value("SendOrder");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID()
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo()
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType()
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To()
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From()
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd()
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid()
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To()
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To()
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited()
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent()
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID()
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName()
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply()
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply()
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry()
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder()
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:07.337 */
public class X_C_BP_EDI extends PO
{
/** Standard Constructor */
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 */
}
/** Load Constructor */
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=366 */
public static final int Table_ID=366;

/** TableName=C_BP_EDI */
public static final String Table_Name="C_BP_EDI";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_SEQUENCE_ID_AD_Reference_ID=128;
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_Value ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set EDI Definition.
Electronic Data Interchange */
public void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
set_ValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
/** Get EDI Definition.
Electronic Data Interchange */
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)get_Value("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
set_Value ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
/** Get Business Partner .
Identifies a Business Partner */
public int getC_BPartner_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Customer No.
EDI Identification Number  */
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
if (CustomerNo.length() > 20)
{
log.warn("setCustomerNo - length > 20 - truncated");
CustomerNo = CustomerNo.substring(0,19);
}
set_Value ("CustomerNo", CustomerNo);
}
/** Get Customer No.
EDI Identification Number  */
public String getCustomerNo() 
{
return (String)get_Value("CustomerNo");
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
public static final int EDITYPE_AD_Reference_ID=201;
/** ASC X12  = X */
public static final String EDITYPE_ASCX12 = "X";
/** EDIFACT = E */
public static final String EDITYPE_EDIFACT = "E";
/** Email EDI = M */
public static final String EDITYPE_EmailEDI = "M";
/** Set EDI Type */
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
if (EDIType.length() > 1)
{
log.warn("setEDIType - length > 1 - truncated");
EDIType = EDIType.substring(0,0);
}
set_Value ("EDIType", EDIType);
}
/** Get EDI Type */
public String getEDIType() 
{
return (String)get_Value("EDIType");
}
/** Set Error Email.
Email address to send error messages to */
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
if (Email_Error_To.length() > 60)
{
log.warn("setEmail_Error_To - length > 60 - truncated");
Email_Error_To = Email_Error_To.substring(0,59);
}
set_Value ("Email_Error_To", Email_Error_To);
}
/** Get Error Email.
Email address to send error messages to */
public String getEmail_Error_To() 
{
return (String)get_Value("Email_Error_To");
}
/** Set From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public void setEmail_From (String Email_From)
{
if (Email_From != null && Email_From.length() > 60)
{
log.warn("setEmail_From - length > 60 - truncated");
Email_From = Email_From.substring(0,59);
}
set_Value ("Email_From", Email_From);
}
/** Get From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public String getEmail_From() 
{
return (String)get_Value("Email_From");
}
/** Set From Email Password.
Password of the sending Email address */
public void setEmail_From_Pwd (String Email_From_Pwd)
{
if (Email_From_Pwd != null && Email_From_Pwd.length() > 20)
{
log.warn("setEmail_From_Pwd - length > 20 - truncated");
Email_From_Pwd = Email_From_Pwd.substring(0,19);
}
set_Value ("Email_From_Pwd", Email_From_Pwd);
}
/** Get From Email Password.
Password of the sending Email address */
public String getEmail_From_Pwd() 
{
return (String)get_Value("Email_From_Pwd");
}
/** Set From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public void setEmail_From_Uid (String Email_From_Uid)
{
if (Email_From_Uid != null && Email_From_Uid.length() > 20)
{
log.warn("setEmail_From_Uid - length > 20 - truncated");
Email_From_Uid = Email_From_Uid.substring(0,19);
}
set_Value ("Email_From_Uid", Email_From_Uid);
}
/** Get From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public String getEmail_From_Uid() 
{
return (String)get_Value("Email_From_Uid");
}
/** Set Info Email.
Email address to send informational messages and copies */
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
if (Email_Info_To.length() > 60)
{
log.warn("setEmail_Info_To - length > 60 - truncated");
Email_Info_To = Email_Info_To.substring(0,59);
}
set_Value ("Email_Info_To", Email_Info_To);
}
/** Get Info Email.
Email address to send informational messages and copies */
public String getEmail_Info_To() 
{
return (String)get_Value("Email_Info_To");
}
/** Set To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public void setEmail_To (String Email_To)
{
if (Email_To != null && Email_To.length() > 60)
{
log.warn("setEmail_To - length > 60 - truncated");
Email_To = Email_To.substring(0,59);
}
set_Value ("Email_To", Email_To);
}
/** Get To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public String getEmail_To() 
{
return (String)get_Value("Email_To");
}
/** Set Activate Audit.
Activate Audit Trail of what numbers are generated */
public void setIsAudited (boolean IsAudited)
{
set_Value ("IsAudited", new Boolean(IsAudited));
}
/** Get Activate Audit.
Activate Audit Trail of what numbers are generated */
public boolean isAudited() 
{
Object oo = get_Value("IsAudited");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Info.
Send informational messages and copies */
public void setIsInfoSent (boolean IsInfoSent)
{
set_Value ("IsInfoSent", new Boolean(IsInfoSent));
}
/** Get Send Info.
Send informational messages and copies */
public boolean isInfoSent() 
{
Object oo = get_Value("IsInfoSent");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_Value ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Received Inquiry Reply */
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
set_Value ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
/** Get Received Inquiry Reply */
public boolean isReceiveInquiryReply() 
{
Object oo = get_Value("ReceiveInquiryReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Receive Order Reply */
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
set_Value ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
/** Get Receive Order Reply */
public boolean isReceiveOrderReply() 
{
Object oo = get_Value("ReceiveOrderReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Inquiry.
Quantity Availability Inquiry */
public void setSendInquiry (boolean SendInquiry)
{
set_Value ("SendInquiry", new Boolean(SendInquiry));
}
/** Get Send Inquiry.
Quantity Availability Inquiry */
public boolean isSendInquiry() 
{
Object oo = get_Value("SendInquiry");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Order */
public void setSendOrder (boolean SendOrder)
{
set_Value ("SendOrder", new Boolean(SendOrder));
}
/** Get Send Order */
public boolean isSendOrder() 
{
Object oo = get_Value("SendOrder");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.27 2003/10/31 05:30:54 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
/** Standard Constructor **/
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
/** Load Constructor **/
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo() 
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType() 
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To() 
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From() 
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd() 
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid() 
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To() 
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To() 
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited() 
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent() 
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName() 
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply() 
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply() 
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry() 
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder() 
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID()
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo()
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType()
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To()
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From()
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd()
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid()
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To()
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To()
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited()
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent()
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID()
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName()
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply()
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply()
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry()
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder()
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID()
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo()
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType()
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To()
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From()
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd()
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid()
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To()
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To()
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited()
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent()
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID()
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName()
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply()
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply()
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry()
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder()
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.73 2004/05/20 05:59:12 jjanke Exp $ */
public class X_C_BP_EDI extends PO
{
/** Standard Constructor */
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 */
}
/** Load Constructor */
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=366;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_SEQUENCE_ID_AD_Reference_ID=128;
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_Value ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set EDI Definition.
Electronic Data Interchange */
public void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
set_ValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
/** Get EDI Definition.
Electronic Data Interchange */
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)get_Value("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
set_Value ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
/** Get Business Partner .
Identifies a Business Partner */
public int getC_BPartner_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Customer No.
EDI Identification Number  */
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
if (CustomerNo.length() > 20)
{
log.warn("setCustomerNo - length > 20 - truncated");
CustomerNo = CustomerNo.substring(0,19);
}
set_Value ("CustomerNo", CustomerNo);
}
/** Get Customer No.
EDI Identification Number  */
public String getCustomerNo() 
{
return (String)get_Value("CustomerNo");
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
public static final int EDITYPE_AD_Reference_ID=201;
/** ASC X12  = X */
public static final String EDITYPE_ASCX12 = "X";
/** EDIFACT = E */
public static final String EDITYPE_EDIFACT = "E";
/** Email EDI = M */
public static final String EDITYPE_EmailEDI = "M";
/** Set EDI Type */
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
if (EDIType.length() > 1)
{
log.warn("setEDIType - length > 1 - truncated");
EDIType = EDIType.substring(0,0);
}
set_Value ("EDIType", EDIType);
}
/** Get EDI Type */
public String getEDIType() 
{
return (String)get_Value("EDIType");
}
/** Set Error Email.
Email address to send error messages to */
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
if (Email_Error_To.length() > 60)
{
log.warn("setEmail_Error_To - length > 60 - truncated");
Email_Error_To = Email_Error_To.substring(0,59);
}
set_Value ("Email_Error_To", Email_Error_To);
}
/** Get Error Email.
Email address to send error messages to */
public String getEmail_Error_To() 
{
return (String)get_Value("Email_Error_To");
}
/** Set From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public void setEmail_From (String Email_From)
{
if (Email_From != null && Email_From.length() > 60)
{
log.warn("setEmail_From - length > 60 - truncated");
Email_From = Email_From.substring(0,59);
}
set_Value ("Email_From", Email_From);
}
/** Get From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public String getEmail_From() 
{
return (String)get_Value("Email_From");
}
/** Set From Email Password.
Password of the sending Email address */
public void setEmail_From_Pwd (String Email_From_Pwd)
{
if (Email_From_Pwd != null && Email_From_Pwd.length() > 20)
{
log.warn("setEmail_From_Pwd - length > 20 - truncated");
Email_From_Pwd = Email_From_Pwd.substring(0,19);
}
set_Value ("Email_From_Pwd", Email_From_Pwd);
}
/** Get From Email Password.
Password of the sending Email address */
public String getEmail_From_Pwd() 
{
return (String)get_Value("Email_From_Pwd");
}
/** Set From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public void setEmail_From_Uid (String Email_From_Uid)
{
if (Email_From_Uid != null && Email_From_Uid.length() > 20)
{
log.warn("setEmail_From_Uid - length > 20 - truncated");
Email_From_Uid = Email_From_Uid.substring(0,19);
}
set_Value ("Email_From_Uid", Email_From_Uid);
}
/** Get From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public String getEmail_From_Uid() 
{
return (String)get_Value("Email_From_Uid");
}
/** Set Info Email.
Email address to send informational messages and copies */
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
if (Email_Info_To.length() > 60)
{
log.warn("setEmail_Info_To - length > 60 - truncated");
Email_Info_To = Email_Info_To.substring(0,59);
}
set_Value ("Email_Info_To", Email_Info_To);
}
/** Get Info Email.
Email address to send informational messages and copies */
public String getEmail_Info_To() 
{
return (String)get_Value("Email_Info_To");
}
/** Set To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public void setEmail_To (String Email_To)
{
if (Email_To != null && Email_To.length() > 60)
{
log.warn("setEmail_To - length > 60 - truncated");
Email_To = Email_To.substring(0,59);
}
set_Value ("Email_To", Email_To);
}
/** Get To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public String getEmail_To() 
{
return (String)get_Value("Email_To");
}
/** Set Activate Audit.
Activate Audit Trail of what numbers are generated */
public void setIsAudited (boolean IsAudited)
{
set_Value ("IsAudited", new Boolean(IsAudited));
}
/** Get Activate Audit.
Activate Audit Trail of what numbers are generated */
public boolean isAudited() 
{
Object oo = get_Value("IsAudited");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Info.
Send informational messages and copies */
public void setIsInfoSent (boolean IsInfoSent)
{
set_Value ("IsInfoSent", new Boolean(IsInfoSent));
}
/** Get Send Info.
Send informational messages and copies */
public boolean isInfoSent() 
{
Object oo = get_Value("IsInfoSent");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_Value ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Received Inquiry Reply */
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
set_Value ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
/** Get Received Inquiry Reply */
public boolean isReceiveInquiryReply() 
{
Object oo = get_Value("ReceiveInquiryReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Receive Order Reply */
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
set_Value ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
/** Get Receive Order Reply */
public boolean isReceiveOrderReply() 
{
Object oo = get_Value("ReceiveOrderReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Inquiry.
Quantity Availability Inquiry */
public void setSendInquiry (boolean SendInquiry)
{
set_Value ("SendInquiry", new Boolean(SendInquiry));
}
/** Get Send Inquiry.
Quantity Availability Inquiry */
public boolean isSendInquiry() 
{
Object oo = get_Value("SendInquiry");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Order */
public void setSendOrder (boolean SendOrder)
{
set_Value ("SendOrder", new Boolean(SendOrder));
}
/** Get Send Order */
public boolean isSendOrder() 
{
Object oo = get_Value("SendOrder");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:07.337 */
public class X_C_BP_EDI extends PO
{
/** Standard Constructor */
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 */
}
/** Load Constructor */
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=366 */
public static final int Table_ID=366;

/** TableName=C_BP_EDI */
public static final String Table_Name="C_BP_EDI";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_SEQUENCE_ID_AD_Reference_ID=128;
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_Value ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set EDI Definition.
Electronic Data Interchange */
public void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
set_ValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
/** Get EDI Definition.
Electronic Data Interchange */
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)get_Value("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
set_Value ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
/** Get Business Partner .
Identifies a Business Partner */
public int getC_BPartner_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Customer No.
EDI Identification Number  */
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
if (CustomerNo.length() > 20)
{
log.warn("setCustomerNo - length > 20 - truncated");
CustomerNo = CustomerNo.substring(0,19);
}
set_Value ("CustomerNo", CustomerNo);
}
/** Get Customer No.
EDI Identification Number  */
public String getCustomerNo() 
{
return (String)get_Value("CustomerNo");
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
public static final int EDITYPE_AD_Reference_ID=201;
/** ASC X12  = X */
public static final String EDITYPE_ASCX12 = "X";
/** EDIFACT = E */
public static final String EDITYPE_EDIFACT = "E";
/** Email EDI = M */
public static final String EDITYPE_EmailEDI = "M";
/** Set EDI Type */
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
if (EDIType.length() > 1)
{
log.warn("setEDIType - length > 1 - truncated");
EDIType = EDIType.substring(0,0);
}
set_Value ("EDIType", EDIType);
}
/** Get EDI Type */
public String getEDIType() 
{
return (String)get_Value("EDIType");
}
/** Set Error Email.
Email address to send error messages to */
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
if (Email_Error_To.length() > 60)
{
log.warn("setEmail_Error_To - length > 60 - truncated");
Email_Error_To = Email_Error_To.substring(0,59);
}
set_Value ("Email_Error_To", Email_Error_To);
}
/** Get Error Email.
Email address to send error messages to */
public String getEmail_Error_To() 
{
return (String)get_Value("Email_Error_To");
}
/** Set From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public void setEmail_From (String Email_From)
{
if (Email_From != null && Email_From.length() > 60)
{
log.warn("setEmail_From - length > 60 - truncated");
Email_From = Email_From.substring(0,59);
}
set_Value ("Email_From", Email_From);
}
/** Get From Email.
Full Email address used to send requests - e.g. edi@organization.com */
public String getEmail_From() 
{
return (String)get_Value("Email_From");
}
/** Set From Email Password.
Password of the sending Email address */
public void setEmail_From_Pwd (String Email_From_Pwd)
{
if (Email_From_Pwd != null && Email_From_Pwd.length() > 20)
{
log.warn("setEmail_From_Pwd - length > 20 - truncated");
Email_From_Pwd = Email_From_Pwd.substring(0,19);
}
set_Value ("Email_From_Pwd", Email_From_Pwd);
}
/** Get From Email Password.
Password of the sending Email address */
public String getEmail_From_Pwd() 
{
return (String)get_Value("Email_From_Pwd");
}
/** Set From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public void setEmail_From_Uid (String Email_From_Uid)
{
if (Email_From_Uid != null && Email_From_Uid.length() > 20)
{
log.warn("setEmail_From_Uid - length > 20 - truncated");
Email_From_Uid = Email_From_Uid.substring(0,19);
}
set_Value ("Email_From_Uid", Email_From_Uid);
}
/** Get From Email User ID.
User ID of the sending Email address (on default SMTP Host) - e.g. edi */
public String getEmail_From_Uid() 
{
return (String)get_Value("Email_From_Uid");
}
/** Set Info Email.
Email address to send informational messages and copies */
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
if (Email_Info_To.length() > 60)
{
log.warn("setEmail_Info_To - length > 60 - truncated");
Email_Info_To = Email_Info_To.substring(0,59);
}
set_Value ("Email_Info_To", Email_Info_To);
}
/** Get Info Email.
Email address to send informational messages and copies */
public String getEmail_Info_To() 
{
return (String)get_Value("Email_Info_To");
}
/** Set To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public void setEmail_To (String Email_To)
{
if (Email_To != null && Email_To.length() > 60)
{
log.warn("setEmail_To - length > 60 - truncated");
Email_To = Email_To.substring(0,59);
}
set_Value ("Email_To", Email_To);
}
/** Get To Email.
Email address to send requests to - e.g. edi@manufacturer.com  */
public String getEmail_To() 
{
return (String)get_Value("Email_To");
}
/** Set Activate Audit.
Activate Audit Trail of what numbers are generated */
public void setIsAudited (boolean IsAudited)
{
set_Value ("IsAudited", new Boolean(IsAudited));
}
/** Get Activate Audit.
Activate Audit Trail of what numbers are generated */
public boolean isAudited() 
{
Object oo = get_Value("IsAudited");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Info.
Send informational messages and copies */
public void setIsInfoSent (boolean IsInfoSent)
{
set_Value ("IsInfoSent", new Boolean(IsInfoSent));
}
/** Get Send Info.
Send informational messages and copies */
public boolean isInfoSent() 
{
Object oo = get_Value("IsInfoSent");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_Value ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Received Inquiry Reply */
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
set_Value ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
/** Get Received Inquiry Reply */
public boolean isReceiveInquiryReply() 
{
Object oo = get_Value("ReceiveInquiryReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Receive Order Reply */
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
set_Value ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
/** Get Receive Order Reply */
public boolean isReceiveOrderReply() 
{
Object oo = get_Value("ReceiveOrderReply");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Inquiry.
Quantity Availability Inquiry */
public void setSendInquiry (boolean SendInquiry)
{
set_Value ("SendInquiry", new Boolean(SendInquiry));
}
/** Get Send Inquiry.
Quantity Availability Inquiry */
public boolean isSendInquiry() 
{
Object oo = get_Value("SendInquiry");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Send Order */
public void setSendOrder (boolean SendOrder)
{
set_Value ("SendOrder", new Boolean(SendOrder));
}
/** Get Send Order */
public boolean isSendOrder() 
{
Object oo = get_Value("SendOrder");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID()
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo()
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType()
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To()
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From()
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd()
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid()
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To()
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To()
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited()
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent()
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID()
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName()
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply()
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply()
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry()
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder()
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_BP_EDI
 ** @version $Id: X_C_BP_EDI.java,v 1.27 2003/10/31 05:30:54 jjanke Exp $ **/
public class X_C_BP_EDI extends PO
{
/** Standard Constructor **/
public X_C_BP_EDI (Properties ctx, int C_BP_EDI_ID)
{
super (ctx, C_BP_EDI_ID);
/** if (C_BP_EDI_ID == 0)
{
setAD_Sequence_ID (0);
setC_BP_EDI_ID (0);
setC_BPartner_ID (0);
setCustomerNo (null);
setEDIType (null);
setEmail_Error_To (null);
setEmail_Info_To (null);
setIsAudited (false);
setIsInfoSent (false);
setM_Warehouse_ID (0);
setName (null);
setReceiveInquiryReply (false);
setReceiveOrderReply (false);
setSendInquiry (false);
setSendOrder (false);
}
 **/
}
/** Load Constructor **/
public X_C_BP_EDI (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 366;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_BP_EDI[").append(getID()).append("]");
return sb.toString();
}
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValue ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_BP_EDI_ID (int C_BP_EDI_ID)
{
setValueNoCheck ("C_BP_EDI_ID", new Integer(C_BP_EDI_ID));
}
public int getC_BP_EDI_ID() 
{
Integer ii = (Integer)getValue("C_BP_EDI_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCustomerNo (String CustomerNo)
{
if (CustomerNo == null) throw new IllegalArgumentException ("CustomerNo is mandatory");
setValue ("CustomerNo", CustomerNo);
}
public String getCustomerNo() 
{
return (String)getValue("CustomerNo");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public static final String EDITYPE_ASCX12 = "X";
public static final String EDITYPE_EDIFACT = "E";
public static final String EDITYPE_EmailEDI = "M";
public void setEDIType (String EDIType)
{
if (EDIType.equals("X") || EDIType.equals("E") || EDIType.equals("M"));
 else throw new IllegalArgumentException ("EDIType Invalid value - Reference_ID=201 - X - E - M");
if (EDIType == null) throw new IllegalArgumentException ("EDIType is mandatory");
setValue ("EDIType", EDIType);
}
public String getEDIType() 
{
return (String)getValue("EDIType");
}
public void setEmail_Error_To (String Email_Error_To)
{
if (Email_Error_To == null) throw new IllegalArgumentException ("Email_Error_To is mandatory");
setValue ("Email_Error_To", Email_Error_To);
}
public String getEmail_Error_To() 
{
return (String)getValue("Email_Error_To");
}
public void setEmail_From (String Email_From)
{
setValue ("Email_From", Email_From);
}
public String getEmail_From() 
{
return (String)getValue("Email_From");
}
public void setEmail_From_Pwd (String Email_From_Pwd)
{
setValue ("Email_From_Pwd", Email_From_Pwd);
}
public String getEmail_From_Pwd() 
{
return (String)getValue("Email_From_Pwd");
}
public void setEmail_From_Uid (String Email_From_Uid)
{
setValue ("Email_From_Uid", Email_From_Uid);
}
public String getEmail_From_Uid() 
{
return (String)getValue("Email_From_Uid");
}
public void setEmail_Info_To (String Email_Info_To)
{
if (Email_Info_To == null) throw new IllegalArgumentException ("Email_Info_To is mandatory");
setValue ("Email_Info_To", Email_Info_To);
}
public String getEmail_Info_To() 
{
return (String)getValue("Email_Info_To");
}
public void setEmail_To (String Email_To)
{
setValue ("Email_To", Email_To);
}
public String getEmail_To() 
{
return (String)getValue("Email_To");
}
public void setIsAudited (boolean IsAudited)
{
setValue ("IsAudited", new Boolean(IsAudited));
}
public boolean isAudited() 
{
Boolean bb = (Boolean)getValue("IsAudited");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsInfoSent (boolean IsInfoSent)
{
setValue ("IsInfoSent", new Boolean(IsInfoSent));
}
public boolean isInfoSent() 
{
Boolean bb = (Boolean)getValue("IsInfoSent");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
setValue ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)getValue("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setName (String Name)
{
if (Name == null) throw new IllegalArgumentException ("Name is mandatory");
setValue ("Name", Name);
}
public String getName() 
{
return (String)getValue("Name");
}
public void setReceiveInquiryReply (boolean ReceiveInquiryReply)
{
setValue ("ReceiveInquiryReply", new Boolean(ReceiveInquiryReply));
}
public boolean isReceiveInquiryReply() 
{
Boolean bb = (Boolean)getValue("ReceiveInquiryReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setReceiveOrderReply (boolean ReceiveOrderReply)
{
setValue ("ReceiveOrderReply", new Boolean(ReceiveOrderReply));
}
public boolean isReceiveOrderReply() 
{
Boolean bb = (Boolean)getValue("ReceiveOrderReply");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendInquiry (boolean SendInquiry)
{
setValue ("SendInquiry", new Boolean(SendInquiry));
}
public boolean isSendInquiry() 
{
Boolean bb = (Boolean)getValue("SendInquiry");
if (bb != null) return bb.booleanValue();
return false;
}
public void setSendOrder (boolean SendOrder)
{
setValue ("SendOrder", new Boolean(SendOrder));
}
public boolean isSendOrder() 
{
Boolean bb = (Boolean)getValue("SendOrder");
if (bb != null) return bb.booleanValue();
return false;
}
}
