/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.14 2003/08/12 17:59:05 jjanke Exp $ **/
public class X_R_Request extends PO
{
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) setValue ("AD_User_ID", null);
 else 
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID()
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType()
{
return (String)getValue("ActionType");
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) setValue ("C_BPartner_ID", null);
 else 
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) setValue ("C_Campaign_ID", null);
 else 
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID()
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) setValue ("C_Invoice_ID", null);
 else 
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID()
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) setValue ("C_Order_ID", null);
 else 
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID()
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) setValue ("C_Payment_ID", null);
 else 
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID()
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) setValue ("C_Project_ID", null);
 else 
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID()
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction()
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction()
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo()
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType()
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated()
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService()
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult()
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) setValue ("M_Product_ID", null);
 else 
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID()
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject()
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText()
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_Followup = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction()
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority()
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed()
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing()
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) setValue ("R_MailText_ID", null);
 else 
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID()
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID()
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID()
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt()
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult()
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID()
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary()
{
return (String)getValue("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.72 2004/05/20 05:59:11 jjanke Exp $ */
public class X_R_Request extends PO
{
/** Standard Constructor */
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);	// 5
setIsEscalated (false);
setIsSelfService (false);	// N
setPriority (null);	// 5
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);	// @AD_User_ID@
setSummary (null);
}
 */
}
/** Load Constructor */
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=417;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
/** Set User/Contact.
User within the system - Internal or Business Partner Contact */
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) set_Value ("AD_User_ID", null);
 else 
set_Value ("AD_User_ID", new Integer(AD_User_ID));
}
/** Get User/Contact.
User within the system - Internal or Business Partner Contact */
public int getAD_User_ID() 
{
Integer ii = (Integer)get_Value("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Asset.
Asset used internally or by customers */
public void setA_Asset_ID (int A_Asset_ID)
{
if (A_Asset_ID == 0) set_Value ("A_Asset_ID", null);
 else 
set_Value ("A_Asset_ID", new Integer(A_Asset_ID));
}
/** Get Asset.
Asset used internally or by customers */
public int getA_Asset_ID() 
{
Integer ii = (Integer)get_Value("A_Asset_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int ACTIONTYPE_AD_Reference_ID=220;
/** Call = C */
public static final String ACTIONTYPE_Call = "C";
/** EMail = E */
public static final String ACTIONTYPE_EMail = "E";
/** Close = X */
public static final String ACTIONTYPE_Close = "X";
/** Mail = M */
public static final String ACTIONTYPE_Mail = "M";
/** Offer/Quote = Q */
public static final String ACTIONTYPE_OfferQuote = "Q";
/** Invoice = I */
public static final String ACTIONTYPE_Invoice = "I";
/** Order = O */
public static final String ACTIONTYPE_Order = "O";
/** Credit = R */
public static final String ACTIONTYPE_Credit = "R";
/** Reminder = A */
public static final String ACTIONTYPE_Reminder = "A";
/** Transfer = T */
public static final String ACTIONTYPE_Transfer = "T";
/** Set Action type.
Method of action taken on this request */
public void setActionType (String ActionType)
{
if (ActionType == null || ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
if (ActionType != null && ActionType.length() > 1)
{
log.warn("setActionType - length > 1 - truncated");
ActionType = ActionType.substring(0,0);
}
set_Value ("ActionType", ActionType);
}
/** Get Action type.
Method of action taken on this request */
public String getActionType() 
{
return (String)get_Value("ActionType");
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) set_Value ("C_BPartner_ID", null);
 else 
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
/** Set Campaign.
Marketing Campaign */
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) set_Value ("C_Campaign_ID", null);
 else 
set_Value ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
/** Get Campaign.
Marketing Campaign */
public int getC_Campaign_ID() 
{
Integer ii = (Integer)get_Value("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Invoice.
Invoice Identifier */
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) set_Value ("C_Invoice_ID", null);
 else 
set_Value ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
/** Get Invoice.
Invoice Identifier */
public int getC_Invoice_ID() 
{
Integer ii = (Integer)get_Value("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) set_Value ("C_Order_ID", null);
 else 
set_Value ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment.
Payment identifier */
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) set_Value ("C_Payment_ID", null);
 else 
set_Value ("C_Payment_ID", new Integer(C_Payment_ID));
}
/** Get Payment.
Payment identifier */
public int getC_Payment_ID() 
{
Integer ii = (Integer)get_Value("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Project.
Financial Project */
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) set_Value ("C_Project_ID", null);
 else 
set_Value ("C_Project_ID", new Integer(C_Project_ID));
}
/** Get Project.
Financial Project */
public int getC_Project_ID() 
{
Integer ii = (Integer)get_Value("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Date last action.
Date this request was last acted on */
public void setDateLastAction (Timestamp DateLastAction)
{
set_ValueNoCheck ("DateLastAction", DateLastAction);
}
/** Get Date last action.
Date this request was last acted on */
public Timestamp getDateLastAction() 
{
return (Timestamp)get_Value("DateLastAction");
}
/** Set Date next action.
Date that this request should be acted on */
public void setDateNextAction (Timestamp DateNextAction)
{
set_Value ("DateNextAction", DateNextAction);
}
/** Get Date next action.
Date that this request should be acted on */
public Timestamp getDateNextAction() 
{
return (Timestamp)get_Value("DateNextAction");
}
/** Set Document No.
Document sequence number of the document */
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
if (DocumentNo.length() > 30)
{
log.warn("setDocumentNo - length > 30 - truncated");
DocumentNo = DocumentNo.substring(0,29);
}
set_Value ("DocumentNo", DocumentNo);
}
/** Get Document No.
Document sequence number of the document */
public String getDocumentNo() 
{
return (String)get_Value("DocumentNo");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getDocumentNo());
}
public static final int DUETYPE_AD_Reference_ID=222;
/** Overdue = 3 */
public static final String DUETYPE_Overdue = "3";
/** Due = 5 */
public static final String DUETYPE_Due = "5";
/** Scheduled = 7 */
public static final String DUETYPE_Scheduled = "7";
/** Set Due type.
Status of the next action for this Request */
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
if (DueType.length() > 1)
{
log.warn("setDueType - length > 1 - truncated");
DueType = DueType.substring(0,0);
}
set_Value ("DueType", DueType);
}
/** Get Due type.
Status of the next action for this Request */
public String getDueType() 
{
return (String)get_Value("DueType");
}
/** Set Escalated.
This request has been escalated */
public void setIsEscalated (boolean IsEscalated)
{
set_Value ("IsEscalated", new Boolean(IsEscalated));
}
/** Get Escalated.
This request has been escalated */
public boolean isEscalated() 
{
Object oo = get_Value("IsEscalated");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public void setIsSelfService (boolean IsSelfService)
{
set_ValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
/** Get Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public boolean isSelfService() 
{
Object oo = get_Value("IsSelfService");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Last Result.
Result of last contact */
public void setLastResult (String LastResult)
{
if (LastResult != null && LastResult.length() > 2000)
{
log.warn("setLastResult - length > 2000 - truncated");
LastResult = LastResult.substring(0,1999);
}
set_Value ("LastResult", LastResult);
}
/** Get Last Result.
Result of last contact */
public String getLastResult() 
{
return (String)get_Value("LastResult");
}
/** Set Product.
Product, Service, Item */
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) set_Value ("M_Product_ID", null);
 else 
set_Value ("M_Product_ID", new Integer(M_Product_ID));
}
/** Get Product.
Product, Service, Item */
public int getM_Product_ID() 
{
Integer ii = (Integer)get_Value("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Mail Subject.
Subject of the mail message */
public void setMailSubject (String MailSubject)
{
if (MailSubject != null && MailSubject.length() > 60)
{
log.warn("setMailSubject - length > 60 - truncated");
MailSubject = MailSubject.substring(0,59);
}
set_Value ("MailSubject", MailSubject);
}
/** Get Mail Subject.
Subject of the mail message */
public String getMailSubject() 
{
return (String)get_Value("MailSubject");
}
/** Set Mail text.
Text used for Mail message */
public void setMailText (String MailText)
{
if (MailText != null && MailText.length() > 2000)
{
log.warn("setMailText - length > 2000 - truncated");
MailText = MailText.substring(0,1999);
}
set_Value ("MailText", MailText);
}
/** Get Mail text.
Text used for Mail message */
public String getMailText() 
{
return (String)get_Value("MailText");
}
public static final int NEXTACTION_AD_Reference_ID=219;
/** None = N */
public static final String NEXTACTION_None = "N";
/** Follow up = F */
public static final String NEXTACTION_FollowUp = "F";
/** Set Next action.
Next Action to be taken */
public void setNextAction (String NextAction)
{
if (NextAction == null || NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
if (NextAction != null && NextAction.length() > 1)
{
log.warn("setNextAction - length > 1 - truncated");
NextAction = NextAction.substring(0,0);
}
set_Value ("NextAction", NextAction);
}
/** Get Next action.
Next Action to be taken */
public String getNextAction() 
{
return (String)get_Value("NextAction");
}
public static final int PRIORITY_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITY_High = "3";
/** Medium = 5 */
public static final String PRIORITY_Medium = "5";
/** Low = 7 */
public static final String PRIORITY_Low = "7";
/** Set Priority.
Indicates if this request is of a high, medium or low priority. */
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
if (Priority.length() > 1)
{
log.warn("setPriority - length > 1 - truncated");
Priority = Priority.substring(0,0);
}
set_Value ("Priority", Priority);
}
/** Get Priority.
Indicates if this request is of a high, medium or low priority. */
public String getPriority() 
{
return (String)get_Value("Priority");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_Value ("Processed", new Boolean(Processed));
}
/** Get Processed.
The document has been processed */
public boolean isProcessed() 
{
Object oo = get_Value("Processed");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Process Now */
public void setProcessing (boolean Processing)
{
set_Value ("Processing", new Boolean(Processing));
}
/** Get Process Now */
public boolean isProcessing() 
{
Object oo = get_Value("Processing");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Mail Template.
Text templates for mailings */
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) set_Value ("R_MailText_ID", null);
 else 
set_Value ("R_MailText_ID", new Integer(R_MailText_ID));
}
/** Get Mail Template.
Text templates for mailings */
public int getR_MailText_ID() 
{
Integer ii = (Integer)get_Value("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public void setR_RequestType_ID (int R_RequestType_ID)
{
set_Value ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
/** Get Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public int getR_RequestType_ID() 
{
Integer ii = (Integer)get_Value("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request.
Request from a Business Partner or Prospect */
public void setR_Request_ID (int R_Request_ID)
{
set_ValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
/** Get Request.
Request from a Business Partner or Prospect */
public int getR_Request_ID() 
{
Integer ii = (Integer)get_Value("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Amount.
Amount associated with this request */
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
set_Value ("RequestAmt", RequestAmt);
}
/** Get Request Amount.
Amount associated with this request */
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Result.
Result of the action taken */
public void setResult (String Result)
{
if (Result != null && Result.length() > 2000)
{
log.warn("setResult - length > 2000 - truncated");
Result = Result.substring(0,1999);
}
set_Value ("Result", Result);
}
/** Get Result.
Result of the action taken */
public String getResult() 
{
return (String)get_Value("Result");
}
public static final int SALESREP_ID_AD_Reference_ID=286;
/** Set Sales Representative.
Sales Representative or Company Agent */
public void setSalesRep_ID (int SalesRep_ID)
{
set_Value ("SalesRep_ID", new Integer(SalesRep_ID));
}
/** Get Sales Representative.
Sales Representative or Company Agent */
public int getSalesRep_ID() 
{
Integer ii = (Integer)get_Value("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Summary.
Textual summary of this request */
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
if (Summary.length() > 2000)
{
log.warn("setSummary - length > 2000 - truncated");
Summary = Summary.substring(0,1999);
}
set_Value ("Summary", Summary);
}
/** Get Summary.
Textual summary of this request */
public String getSummary() 
{
return (String)get_Value("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for R_Request
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:19.681 */
public class X_R_Request extends PO
{
/** Standard Constructor */
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);	// 5
setIsEscalated (false);
setIsSelfService (false);	// N
setPriority (null);	// 5
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);	// @AD_User_ID@
setSummary (null);
}
 */
}
/** Load Constructor */
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=417 */
public static final int Table_ID=417;

/** TableName=R_Request */
public static final String Table_Name="R_Request";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
/** Set User/Contact.
User within the system - Internal or Business Partner Contact */
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) set_Value ("AD_User_ID", null);
 else 
set_Value ("AD_User_ID", new Integer(AD_User_ID));
}
/** Get User/Contact.
User within the system - Internal or Business Partner Contact */
public int getAD_User_ID() 
{
Integer ii = (Integer)get_Value("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Asset.
Asset used internally or by customers */
public void setA_Asset_ID (int A_Asset_ID)
{
if (A_Asset_ID == 0) set_Value ("A_Asset_ID", null);
 else 
set_Value ("A_Asset_ID", new Integer(A_Asset_ID));
}
/** Get Asset.
Asset used internally or by customers */
public int getA_Asset_ID() 
{
Integer ii = (Integer)get_Value("A_Asset_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int ACTIONTYPE_AD_Reference_ID=220;
/** Re-Open = x */
public static final String ACTIONTYPE_Re_Open = "x";
/** Call = C */
public static final String ACTIONTYPE_Call = "C";
/** EMail = E */
public static final String ACTIONTYPE_EMail = "E";
/** Close = X */
public static final String ACTIONTYPE_Close = "X";
/** Mail = M */
public static final String ACTIONTYPE_Mail = "M";
/** Offer/Quote = Q */
public static final String ACTIONTYPE_OfferQuote = "Q";
/** Invoice = I */
public static final String ACTIONTYPE_Invoice = "I";
/** Order = O */
public static final String ACTIONTYPE_Order = "O";
/** Credit = R */
public static final String ACTIONTYPE_Credit = "R";
/** Reminder = A */
public static final String ACTIONTYPE_Reminder = "A";
/** Transfer = T */
public static final String ACTIONTYPE_Transfer = "T";
/** Set Action type.
Method of action taken on this request */
public void setActionType (String ActionType)
{
if (ActionType == null || ActionType.equals("x") || ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - x - C - E - X - M - Q - I - O - R - A - T");
if (ActionType != null && ActionType.length() > 1)
{
log.warn("setActionType - length > 1 - truncated");
ActionType = ActionType.substring(0,0);
}
set_Value ("ActionType", ActionType);
}
/** Get Action type.
Method of action taken on this request */
public String getActionType() 
{
return (String)get_Value("ActionType");
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) set_Value ("C_BPartner_ID", null);
 else 
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
/** Set Campaign.
Marketing Campaign */
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) set_Value ("C_Campaign_ID", null);
 else 
set_Value ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
/** Get Campaign.
Marketing Campaign */
public int getC_Campaign_ID() 
{
Integer ii = (Integer)get_Value("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Invoice.
Invoice Identifier */
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) set_Value ("C_Invoice_ID", null);
 else 
set_Value ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
/** Get Invoice.
Invoice Identifier */
public int getC_Invoice_ID() 
{
Integer ii = (Integer)get_Value("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) set_Value ("C_Order_ID", null);
 else 
set_Value ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment.
Payment identifier */
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) set_Value ("C_Payment_ID", null);
 else 
set_Value ("C_Payment_ID", new Integer(C_Payment_ID));
}
/** Get Payment.
Payment identifier */
public int getC_Payment_ID() 
{
Integer ii = (Integer)get_Value("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Project.
Financial Project */
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) set_Value ("C_Project_ID", null);
 else 
set_Value ("C_Project_ID", new Integer(C_Project_ID));
}
/** Get Project.
Financial Project */
public int getC_Project_ID() 
{
Integer ii = (Integer)get_Value("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Date last action.
Date this request was last acted on */
public void setDateLastAction (Timestamp DateLastAction)
{
set_ValueNoCheck ("DateLastAction", DateLastAction);
}
/** Get Date last action.
Date this request was last acted on */
public Timestamp getDateLastAction() 
{
return (Timestamp)get_Value("DateLastAction");
}
/** Set Last Alert.
Date when last alert were sent */
public void setDateLastAlert (Timestamp DateLastAlert)
{
set_Value ("DateLastAlert", DateLastAlert);
}
/** Get Last Alert.
Date when last alert were sent */
public Timestamp getDateLastAlert() 
{
return (Timestamp)get_Value("DateLastAlert");
}
/** Set Date next action.
Date that this request should be acted on */
public void setDateNextAction (Timestamp DateNextAction)
{
set_Value ("DateNextAction", DateNextAction);
}
/** Get Date next action.
Date that this request should be acted on */
public Timestamp getDateNextAction() 
{
return (Timestamp)get_Value("DateNextAction");
}
/** Set Document No.
Document sequence number of the document */
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
if (DocumentNo.length() > 30)
{
log.warn("setDocumentNo - length > 30 - truncated");
DocumentNo = DocumentNo.substring(0,29);
}
set_Value ("DocumentNo", DocumentNo);
}
/** Get Document No.
Document sequence number of the document */
public String getDocumentNo() 
{
return (String)get_Value("DocumentNo");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getDocumentNo());
}
public static final int DUETYPE_AD_Reference_ID=222;
/** Overdue = 3 */
public static final String DUETYPE_Overdue = "3";
/** Due = 5 */
public static final String DUETYPE_Due = "5";
/** Scheduled = 7 */
public static final String DUETYPE_Scheduled = "7";
/** Set Due type.
Status of the next action for this Request */
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
if (DueType.length() > 1)
{
log.warn("setDueType - length > 1 - truncated");
DueType = DueType.substring(0,0);
}
set_Value ("DueType", DueType);
}
/** Get Due type.
Status of the next action for this Request */
public String getDueType() 
{
return (String)get_Value("DueType");
}
/** Set Escalated.
This request has been escalated */
public void setIsEscalated (boolean IsEscalated)
{
set_Value ("IsEscalated", new Boolean(IsEscalated));
}
/** Get Escalated.
This request has been escalated */
public boolean isEscalated() 
{
Object oo = get_Value("IsEscalated");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public void setIsSelfService (boolean IsSelfService)
{
set_ValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
/** Get Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public boolean isSelfService() 
{
Object oo = get_Value("IsSelfService");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Last Result.
Result of last contact */
public void setLastResult (String LastResult)
{
if (LastResult != null && LastResult.length() > 2000)
{
log.warn("setLastResult - length > 2000 - truncated");
LastResult = LastResult.substring(0,1999);
}
set_Value ("LastResult", LastResult);
}
/** Get Last Result.
Result of last contact */
public String getLastResult() 
{
return (String)get_Value("LastResult");
}
/** Set Product.
Product, Service, Item */
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) set_Value ("M_Product_ID", null);
 else 
set_Value ("M_Product_ID", new Integer(M_Product_ID));
}
/** Get Product.
Product, Service, Item */
public int getM_Product_ID() 
{
Integer ii = (Integer)get_Value("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Mail Subject.
Subject of the mail message */
public void setMailSubject (String MailSubject)
{
if (MailSubject != null && MailSubject.length() > 60)
{
log.warn("setMailSubject - length > 60 - truncated");
MailSubject = MailSubject.substring(0,59);
}
set_Value ("MailSubject", MailSubject);
}
/** Get Mail Subject.
Subject of the mail message */
public String getMailSubject() 
{
return (String)get_Value("MailSubject");
}
/** Set Mail text.
Text used for Mail message */
public void setMailText (String MailText)
{
if (MailText != null && MailText.length() > 2000)
{
log.warn("setMailText - length > 2000 - truncated");
MailText = MailText.substring(0,1999);
}
set_Value ("MailText", MailText);
}
/** Get Mail text.
Text used for Mail message */
public String getMailText() 
{
return (String)get_Value("MailText");
}
public static final int NEXTACTION_AD_Reference_ID=219;
/** None = N */
public static final String NEXTACTION_None = "N";
/** Follow up = F */
public static final String NEXTACTION_FollowUp = "F";
/** Set Next action.
Next Action to be taken */
public void setNextAction (String NextAction)
{
if (NextAction == null || NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
if (NextAction != null && NextAction.length() > 1)
{
log.warn("setNextAction - length > 1 - truncated");
NextAction = NextAction.substring(0,0);
}
set_Value ("NextAction", NextAction);
}
/** Get Next action.
Next Action to be taken */
public String getNextAction() 
{
return (String)get_Value("NextAction");
}
public static final int PRIORITY_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITY_High = "3";
/** Medium = 5 */
public static final String PRIORITY_Medium = "5";
/** Low = 7 */
public static final String PRIORITY_Low = "7";
/** Set Priority.
Indicates if this request is of a high, medium or low priority. */
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
if (Priority.length() > 1)
{
log.warn("setPriority - length > 1 - truncated");
Priority = Priority.substring(0,0);
}
set_Value ("Priority", Priority);
}
/** Get Priority.
Indicates if this request is of a high, medium or low priority. */
public String getPriority() 
{
return (String)get_Value("Priority");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_Value ("Processed", new Boolean(Processed));
}
/** Get Processed.
The document has been processed */
public boolean isProcessed() 
{
Object oo = get_Value("Processed");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Process Now */
public void setProcessing (boolean Processing)
{
set_Value ("Processing", new Boolean(Processing));
}
/** Get Process Now */
public boolean isProcessing() 
{
Object oo = get_Value("Processing");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Mail Template.
Text templates for mailings */
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) set_Value ("R_MailText_ID", null);
 else 
set_Value ("R_MailText_ID", new Integer(R_MailText_ID));
}
/** Get Mail Template.
Text templates for mailings */
public int getR_MailText_ID() 
{
Integer ii = (Integer)get_Value("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public void setR_RequestType_ID (int R_RequestType_ID)
{
set_Value ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
/** Get Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public int getR_RequestType_ID() 
{
Integer ii = (Integer)get_Value("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request.
Request from a Business Partner or Prospect */
public void setR_Request_ID (int R_Request_ID)
{
set_ValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
/** Get Request.
Request from a Business Partner or Prospect */
public int getR_Request_ID() 
{
Integer ii = (Integer)get_Value("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Amount.
Amount associated with this request */
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
set_Value ("RequestAmt", RequestAmt);
}
/** Get Request Amount.
Amount associated with this request */
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Result.
Result of the action taken */
public void setResult (String Result)
{
if (Result != null && Result.length() > 2000)
{
log.warn("setResult - length > 2000 - truncated");
Result = Result.substring(0,1999);
}
set_Value ("Result", Result);
}
/** Get Result.
Result of the action taken */
public String getResult() 
{
return (String)get_Value("Result");
}
public static final int SALESREP_ID_AD_Reference_ID=286;
/** Set Sales Representative.
Sales Representative or Company Agent */
public void setSalesRep_ID (int SalesRep_ID)
{
set_Value ("SalesRep_ID", new Integer(SalesRep_ID));
}
/** Get Sales Representative.
Sales Representative or Company Agent */
public int getSalesRep_ID() 
{
Integer ii = (Integer)get_Value("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Summary.
Textual summary of this request */
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
if (Summary.length() > 2000)
{
log.warn("setSummary - length > 2000 - truncated");
Summary = Summary.substring(0,1999);
}
set_Value ("Summary", Summary);
}
/** Get Summary.
Textual summary of this request */
public String getSummary() 
{
return (String)get_Value("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.26 2003/10/31 05:30:53 jjanke Exp $ **/
public class X_R_Request extends PO
{
/** Standard Constructor **/
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
/** Load Constructor **/
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) setValue ("AD_User_ID", null);
 else 
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID() 
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType() 
{
return (String)getValue("ActionType");
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) setValue ("C_BPartner_ID", null);
 else 
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) setValue ("C_Campaign_ID", null);
 else 
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID() 
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) setValue ("C_Invoice_ID", null);
 else 
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID() 
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) setValue ("C_Order_ID", null);
 else 
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID() 
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) setValue ("C_Payment_ID", null);
 else 
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID() 
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) setValue ("C_Project_ID", null);
 else 
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID() 
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction() 
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction() 
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo() 
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType() 
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated() 
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService() 
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult() 
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) setValue ("M_Product_ID", null);
 else 
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID() 
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject() 
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText() 
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_FollowUp = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction() 
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority() 
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed() 
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing() 
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) setValue ("R_MailText_ID", null);
 else 
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID() 
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID() 
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID() 
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult() 
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID() 
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary() 
{
return (String)getValue("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_R_Request extends PO
{
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID()
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType()
{
return (String)getValue("ActionType");
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
public void setC_Campaign_ID (int C_Campaign_ID)
{
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID()
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID()
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID()
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID()
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID()
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction()
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction()
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo()
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType()
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated()
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService()
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult()
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID()
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject()
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText()
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_Followup = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction()
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority()
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed()
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing()
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID()
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID()
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID()
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt()
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult()
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID()
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary()
{
return (String)getValue("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.14 2003/08/12 17:59:05 jjanke Exp $ **/
public class X_R_Request extends PO
{
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) setValue ("AD_User_ID", null);
 else 
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID()
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType()
{
return (String)getValue("ActionType");
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) setValue ("C_BPartner_ID", null);
 else 
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID()
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) setValue ("C_Campaign_ID", null);
 else 
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID()
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) setValue ("C_Invoice_ID", null);
 else 
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID()
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) setValue ("C_Order_ID", null);
 else 
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID()
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) setValue ("C_Payment_ID", null);
 else 
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID()
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) setValue ("C_Project_ID", null);
 else 
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID()
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction()
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction()
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo()
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType()
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated()
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService()
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult()
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) setValue ("M_Product_ID", null);
 else 
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID()
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject()
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText()
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_Followup = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction()
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority()
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed()
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing()
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) setValue ("R_MailText_ID", null);
 else 
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID()
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID()
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID()
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt()
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult()
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID()
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary()
{
return (String)getValue("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.72 2004/05/20 05:59:11 jjanke Exp $ */
public class X_R_Request extends PO
{
/** Standard Constructor */
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);	// 5
setIsEscalated (false);
setIsSelfService (false);	// N
setPriority (null);	// 5
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);	// @AD_User_ID@
setSummary (null);
}
 */
}
/** Load Constructor */
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=417;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
/** Set User/Contact.
User within the system - Internal or Business Partner Contact */
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) set_Value ("AD_User_ID", null);
 else 
set_Value ("AD_User_ID", new Integer(AD_User_ID));
}
/** Get User/Contact.
User within the system - Internal or Business Partner Contact */
public int getAD_User_ID() 
{
Integer ii = (Integer)get_Value("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Asset.
Asset used internally or by customers */
public void setA_Asset_ID (int A_Asset_ID)
{
if (A_Asset_ID == 0) set_Value ("A_Asset_ID", null);
 else 
set_Value ("A_Asset_ID", new Integer(A_Asset_ID));
}
/** Get Asset.
Asset used internally or by customers */
public int getA_Asset_ID() 
{
Integer ii = (Integer)get_Value("A_Asset_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int ACTIONTYPE_AD_Reference_ID=220;
/** Call = C */
public static final String ACTIONTYPE_Call = "C";
/** EMail = E */
public static final String ACTIONTYPE_EMail = "E";
/** Close = X */
public static final String ACTIONTYPE_Close = "X";
/** Mail = M */
public static final String ACTIONTYPE_Mail = "M";
/** Offer/Quote = Q */
public static final String ACTIONTYPE_OfferQuote = "Q";
/** Invoice = I */
public static final String ACTIONTYPE_Invoice = "I";
/** Order = O */
public static final String ACTIONTYPE_Order = "O";
/** Credit = R */
public static final String ACTIONTYPE_Credit = "R";
/** Reminder = A */
public static final String ACTIONTYPE_Reminder = "A";
/** Transfer = T */
public static final String ACTIONTYPE_Transfer = "T";
/** Set Action type.
Method of action taken on this request */
public void setActionType (String ActionType)
{
if (ActionType == null || ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
if (ActionType != null && ActionType.length() > 1)
{
log.warn("setActionType - length > 1 - truncated");
ActionType = ActionType.substring(0,0);
}
set_Value ("ActionType", ActionType);
}
/** Get Action type.
Method of action taken on this request */
public String getActionType() 
{
return (String)get_Value("ActionType");
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) set_Value ("C_BPartner_ID", null);
 else 
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
/** Set Campaign.
Marketing Campaign */
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) set_Value ("C_Campaign_ID", null);
 else 
set_Value ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
/** Get Campaign.
Marketing Campaign */
public int getC_Campaign_ID() 
{
Integer ii = (Integer)get_Value("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Invoice.
Invoice Identifier */
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) set_Value ("C_Invoice_ID", null);
 else 
set_Value ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
/** Get Invoice.
Invoice Identifier */
public int getC_Invoice_ID() 
{
Integer ii = (Integer)get_Value("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) set_Value ("C_Order_ID", null);
 else 
set_Value ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment.
Payment identifier */
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) set_Value ("C_Payment_ID", null);
 else 
set_Value ("C_Payment_ID", new Integer(C_Payment_ID));
}
/** Get Payment.
Payment identifier */
public int getC_Payment_ID() 
{
Integer ii = (Integer)get_Value("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Project.
Financial Project */
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) set_Value ("C_Project_ID", null);
 else 
set_Value ("C_Project_ID", new Integer(C_Project_ID));
}
/** Get Project.
Financial Project */
public int getC_Project_ID() 
{
Integer ii = (Integer)get_Value("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Date last action.
Date this request was last acted on */
public void setDateLastAction (Timestamp DateLastAction)
{
set_ValueNoCheck ("DateLastAction", DateLastAction);
}
/** Get Date last action.
Date this request was last acted on */
public Timestamp getDateLastAction() 
{
return (Timestamp)get_Value("DateLastAction");
}
/** Set Date next action.
Date that this request should be acted on */
public void setDateNextAction (Timestamp DateNextAction)
{
set_Value ("DateNextAction", DateNextAction);
}
/** Get Date next action.
Date that this request should be acted on */
public Timestamp getDateNextAction() 
{
return (Timestamp)get_Value("DateNextAction");
}
/** Set Document No.
Document sequence number of the document */
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
if (DocumentNo.length() > 30)
{
log.warn("setDocumentNo - length > 30 - truncated");
DocumentNo = DocumentNo.substring(0,29);
}
set_Value ("DocumentNo", DocumentNo);
}
/** Get Document No.
Document sequence number of the document */
public String getDocumentNo() 
{
return (String)get_Value("DocumentNo");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getDocumentNo());
}
public static final int DUETYPE_AD_Reference_ID=222;
/** Overdue = 3 */
public static final String DUETYPE_Overdue = "3";
/** Due = 5 */
public static final String DUETYPE_Due = "5";
/** Scheduled = 7 */
public static final String DUETYPE_Scheduled = "7";
/** Set Due type.
Status of the next action for this Request */
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
if (DueType.length() > 1)
{
log.warn("setDueType - length > 1 - truncated");
DueType = DueType.substring(0,0);
}
set_Value ("DueType", DueType);
}
/** Get Due type.
Status of the next action for this Request */
public String getDueType() 
{
return (String)get_Value("DueType");
}
/** Set Escalated.
This request has been escalated */
public void setIsEscalated (boolean IsEscalated)
{
set_Value ("IsEscalated", new Boolean(IsEscalated));
}
/** Get Escalated.
This request has been escalated */
public boolean isEscalated() 
{
Object oo = get_Value("IsEscalated");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public void setIsSelfService (boolean IsSelfService)
{
set_ValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
/** Get Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public boolean isSelfService() 
{
Object oo = get_Value("IsSelfService");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Last Result.
Result of last contact */
public void setLastResult (String LastResult)
{
if (LastResult != null && LastResult.length() > 2000)
{
log.warn("setLastResult - length > 2000 - truncated");
LastResult = LastResult.substring(0,1999);
}
set_Value ("LastResult", LastResult);
}
/** Get Last Result.
Result of last contact */
public String getLastResult() 
{
return (String)get_Value("LastResult");
}
/** Set Product.
Product, Service, Item */
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) set_Value ("M_Product_ID", null);
 else 
set_Value ("M_Product_ID", new Integer(M_Product_ID));
}
/** Get Product.
Product, Service, Item */
public int getM_Product_ID() 
{
Integer ii = (Integer)get_Value("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Mail Subject.
Subject of the mail message */
public void setMailSubject (String MailSubject)
{
if (MailSubject != null && MailSubject.length() > 60)
{
log.warn("setMailSubject - length > 60 - truncated");
MailSubject = MailSubject.substring(0,59);
}
set_Value ("MailSubject", MailSubject);
}
/** Get Mail Subject.
Subject of the mail message */
public String getMailSubject() 
{
return (String)get_Value("MailSubject");
}
/** Set Mail text.
Text used for Mail message */
public void setMailText (String MailText)
{
if (MailText != null && MailText.length() > 2000)
{
log.warn("setMailText - length > 2000 - truncated");
MailText = MailText.substring(0,1999);
}
set_Value ("MailText", MailText);
}
/** Get Mail text.
Text used for Mail message */
public String getMailText() 
{
return (String)get_Value("MailText");
}
public static final int NEXTACTION_AD_Reference_ID=219;
/** None = N */
public static final String NEXTACTION_None = "N";
/** Follow up = F */
public static final String NEXTACTION_FollowUp = "F";
/** Set Next action.
Next Action to be taken */
public void setNextAction (String NextAction)
{
if (NextAction == null || NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
if (NextAction != null && NextAction.length() > 1)
{
log.warn("setNextAction - length > 1 - truncated");
NextAction = NextAction.substring(0,0);
}
set_Value ("NextAction", NextAction);
}
/** Get Next action.
Next Action to be taken */
public String getNextAction() 
{
return (String)get_Value("NextAction");
}
public static final int PRIORITY_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITY_High = "3";
/** Medium = 5 */
public static final String PRIORITY_Medium = "5";
/** Low = 7 */
public static final String PRIORITY_Low = "7";
/** Set Priority.
Indicates if this request is of a high, medium or low priority. */
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
if (Priority.length() > 1)
{
log.warn("setPriority - length > 1 - truncated");
Priority = Priority.substring(0,0);
}
set_Value ("Priority", Priority);
}
/** Get Priority.
Indicates if this request is of a high, medium or low priority. */
public String getPriority() 
{
return (String)get_Value("Priority");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_Value ("Processed", new Boolean(Processed));
}
/** Get Processed.
The document has been processed */
public boolean isProcessed() 
{
Object oo = get_Value("Processed");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Process Now */
public void setProcessing (boolean Processing)
{
set_Value ("Processing", new Boolean(Processing));
}
/** Get Process Now */
public boolean isProcessing() 
{
Object oo = get_Value("Processing");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Mail Template.
Text templates for mailings */
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) set_Value ("R_MailText_ID", null);
 else 
set_Value ("R_MailText_ID", new Integer(R_MailText_ID));
}
/** Get Mail Template.
Text templates for mailings */
public int getR_MailText_ID() 
{
Integer ii = (Integer)get_Value("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public void setR_RequestType_ID (int R_RequestType_ID)
{
set_Value ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
/** Get Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public int getR_RequestType_ID() 
{
Integer ii = (Integer)get_Value("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request.
Request from a Business Partner or Prospect */
public void setR_Request_ID (int R_Request_ID)
{
set_ValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
/** Get Request.
Request from a Business Partner or Prospect */
public int getR_Request_ID() 
{
Integer ii = (Integer)get_Value("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Amount.
Amount associated with this request */
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
set_Value ("RequestAmt", RequestAmt);
}
/** Get Request Amount.
Amount associated with this request */
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Result.
Result of the action taken */
public void setResult (String Result)
{
if (Result != null && Result.length() > 2000)
{
log.warn("setResult - length > 2000 - truncated");
Result = Result.substring(0,1999);
}
set_Value ("Result", Result);
}
/** Get Result.
Result of the action taken */
public String getResult() 
{
return (String)get_Value("Result");
}
public static final int SALESREP_ID_AD_Reference_ID=286;
/** Set Sales Representative.
Sales Representative or Company Agent */
public void setSalesRep_ID (int SalesRep_ID)
{
set_Value ("SalesRep_ID", new Integer(SalesRep_ID));
}
/** Get Sales Representative.
Sales Representative or Company Agent */
public int getSalesRep_ID() 
{
Integer ii = (Integer)get_Value("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Summary.
Textual summary of this request */
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
if (Summary.length() > 2000)
{
log.warn("setSummary - length > 2000 - truncated");
Summary = Summary.substring(0,1999);
}
set_Value ("Summary", Summary);
}
/** Get Summary.
Textual summary of this request */
public String getSummary() 
{
return (String)get_Value("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for R_Request
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:19.681 */
public class X_R_Request extends PO
{
/** Standard Constructor */
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);	// 5
setIsEscalated (false);
setIsSelfService (false);	// N
setPriority (null);	// 5
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);	// @AD_User_ID@
setSummary (null);
}
 */
}
/** Load Constructor */
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=417 */
public static final int Table_ID=417;

/** TableName=R_Request */
public static final String Table_Name="R_Request";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
/** Set User/Contact.
User within the system - Internal or Business Partner Contact */
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) set_Value ("AD_User_ID", null);
 else 
set_Value ("AD_User_ID", new Integer(AD_User_ID));
}
/** Get User/Contact.
User within the system - Internal or Business Partner Contact */
public int getAD_User_ID() 
{
Integer ii = (Integer)get_Value("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Asset.
Asset used internally or by customers */
public void setA_Asset_ID (int A_Asset_ID)
{
if (A_Asset_ID == 0) set_Value ("A_Asset_ID", null);
 else 
set_Value ("A_Asset_ID", new Integer(A_Asset_ID));
}
/** Get Asset.
Asset used internally or by customers */
public int getA_Asset_ID() 
{
Integer ii = (Integer)get_Value("A_Asset_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int ACTIONTYPE_AD_Reference_ID=220;
/** Re-Open = x */
public static final String ACTIONTYPE_Re_Open = "x";
/** Call = C */
public static final String ACTIONTYPE_Call = "C";
/** EMail = E */
public static final String ACTIONTYPE_EMail = "E";
/** Close = X */
public static final String ACTIONTYPE_Close = "X";
/** Mail = M */
public static final String ACTIONTYPE_Mail = "M";
/** Offer/Quote = Q */
public static final String ACTIONTYPE_OfferQuote = "Q";
/** Invoice = I */
public static final String ACTIONTYPE_Invoice = "I";
/** Order = O */
public static final String ACTIONTYPE_Order = "O";
/** Credit = R */
public static final String ACTIONTYPE_Credit = "R";
/** Reminder = A */
public static final String ACTIONTYPE_Reminder = "A";
/** Transfer = T */
public static final String ACTIONTYPE_Transfer = "T";
/** Set Action type.
Method of action taken on this request */
public void setActionType (String ActionType)
{
if (ActionType == null || ActionType.equals("x") || ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - x - C - E - X - M - Q - I - O - R - A - T");
if (ActionType != null && ActionType.length() > 1)
{
log.warn("setActionType - length > 1 - truncated");
ActionType = ActionType.substring(0,0);
}
set_Value ("ActionType", ActionType);
}
/** Get Action type.
Method of action taken on this request */
public String getActionType() 
{
return (String)get_Value("ActionType");
}
/** Set Business Partner .
Identifies a Business Partner */
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) set_Value ("C_BPartner_ID", null);
 else 
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
/** Set Campaign.
Marketing Campaign */
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) set_Value ("C_Campaign_ID", null);
 else 
set_Value ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
/** Get Campaign.
Marketing Campaign */
public int getC_Campaign_ID() 
{
Integer ii = (Integer)get_Value("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Invoice.
Invoice Identifier */
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) set_Value ("C_Invoice_ID", null);
 else 
set_Value ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
/** Get Invoice.
Invoice Identifier */
public int getC_Invoice_ID() 
{
Integer ii = (Integer)get_Value("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) set_Value ("C_Order_ID", null);
 else 
set_Value ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment.
Payment identifier */
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) set_Value ("C_Payment_ID", null);
 else 
set_Value ("C_Payment_ID", new Integer(C_Payment_ID));
}
/** Get Payment.
Payment identifier */
public int getC_Payment_ID() 
{
Integer ii = (Integer)get_Value("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Project.
Financial Project */
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) set_Value ("C_Project_ID", null);
 else 
set_Value ("C_Project_ID", new Integer(C_Project_ID));
}
/** Get Project.
Financial Project */
public int getC_Project_ID() 
{
Integer ii = (Integer)get_Value("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Date last action.
Date this request was last acted on */
public void setDateLastAction (Timestamp DateLastAction)
{
set_ValueNoCheck ("DateLastAction", DateLastAction);
}
/** Get Date last action.
Date this request was last acted on */
public Timestamp getDateLastAction() 
{
return (Timestamp)get_Value("DateLastAction");
}
/** Set Last Alert.
Date when last alert were sent */
public void setDateLastAlert (Timestamp DateLastAlert)
{
set_Value ("DateLastAlert", DateLastAlert);
}
/** Get Last Alert.
Date when last alert were sent */
public Timestamp getDateLastAlert() 
{
return (Timestamp)get_Value("DateLastAlert");
}
/** Set Date next action.
Date that this request should be acted on */
public void setDateNextAction (Timestamp DateNextAction)
{
set_Value ("DateNextAction", DateNextAction);
}
/** Get Date next action.
Date that this request should be acted on */
public Timestamp getDateNextAction() 
{
return (Timestamp)get_Value("DateNextAction");
}
/** Set Document No.
Document sequence number of the document */
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
if (DocumentNo.length() > 30)
{
log.warn("setDocumentNo - length > 30 - truncated");
DocumentNo = DocumentNo.substring(0,29);
}
set_Value ("DocumentNo", DocumentNo);
}
/** Get Document No.
Document sequence number of the document */
public String getDocumentNo() 
{
return (String)get_Value("DocumentNo");
}
public KeyNamePair getKeyNamePair() 
{
return new KeyNamePair(getID(), getDocumentNo());
}
public static final int DUETYPE_AD_Reference_ID=222;
/** Overdue = 3 */
public static final String DUETYPE_Overdue = "3";
/** Due = 5 */
public static final String DUETYPE_Due = "5";
/** Scheduled = 7 */
public static final String DUETYPE_Scheduled = "7";
/** Set Due type.
Status of the next action for this Request */
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
if (DueType.length() > 1)
{
log.warn("setDueType - length > 1 - truncated");
DueType = DueType.substring(0,0);
}
set_Value ("DueType", DueType);
}
/** Get Due type.
Status of the next action for this Request */
public String getDueType() 
{
return (String)get_Value("DueType");
}
/** Set Escalated.
This request has been escalated */
public void setIsEscalated (boolean IsEscalated)
{
set_Value ("IsEscalated", new Boolean(IsEscalated));
}
/** Get Escalated.
This request has been escalated */
public boolean isEscalated() 
{
Object oo = get_Value("IsEscalated");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public void setIsSelfService (boolean IsSelfService)
{
set_ValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
/** Get Self-Service.
This is a Self-Service entry or this entry can be changed via Self-Service */
public boolean isSelfService() 
{
Object oo = get_Value("IsSelfService");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Last Result.
Result of last contact */
public void setLastResult (String LastResult)
{
if (LastResult != null && LastResult.length() > 2000)
{
log.warn("setLastResult - length > 2000 - truncated");
LastResult = LastResult.substring(0,1999);
}
set_Value ("LastResult", LastResult);
}
/** Get Last Result.
Result of last contact */
public String getLastResult() 
{
return (String)get_Value("LastResult");
}
/** Set Product.
Product, Service, Item */
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) set_Value ("M_Product_ID", null);
 else 
set_Value ("M_Product_ID", new Integer(M_Product_ID));
}
/** Get Product.
Product, Service, Item */
public int getM_Product_ID() 
{
Integer ii = (Integer)get_Value("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Mail Subject.
Subject of the mail message */
public void setMailSubject (String MailSubject)
{
if (MailSubject != null && MailSubject.length() > 60)
{
log.warn("setMailSubject - length > 60 - truncated");
MailSubject = MailSubject.substring(0,59);
}
set_Value ("MailSubject", MailSubject);
}
/** Get Mail Subject.
Subject of the mail message */
public String getMailSubject() 
{
return (String)get_Value("MailSubject");
}
/** Set Mail text.
Text used for Mail message */
public void setMailText (String MailText)
{
if (MailText != null && MailText.length() > 2000)
{
log.warn("setMailText - length > 2000 - truncated");
MailText = MailText.substring(0,1999);
}
set_Value ("MailText", MailText);
}
/** Get Mail text.
Text used for Mail message */
public String getMailText() 
{
return (String)get_Value("MailText");
}
public static final int NEXTACTION_AD_Reference_ID=219;
/** None = N */
public static final String NEXTACTION_None = "N";
/** Follow up = F */
public static final String NEXTACTION_FollowUp = "F";
/** Set Next action.
Next Action to be taken */
public void setNextAction (String NextAction)
{
if (NextAction == null || NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
if (NextAction != null && NextAction.length() > 1)
{
log.warn("setNextAction - length > 1 - truncated");
NextAction = NextAction.substring(0,0);
}
set_Value ("NextAction", NextAction);
}
/** Get Next action.
Next Action to be taken */
public String getNextAction() 
{
return (String)get_Value("NextAction");
}
public static final int PRIORITY_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITY_High = "3";
/** Medium = 5 */
public static final String PRIORITY_Medium = "5";
/** Low = 7 */
public static final String PRIORITY_Low = "7";
/** Set Priority.
Indicates if this request is of a high, medium or low priority. */
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
if (Priority.length() > 1)
{
log.warn("setPriority - length > 1 - truncated");
Priority = Priority.substring(0,0);
}
set_Value ("Priority", Priority);
}
/** Get Priority.
Indicates if this request is of a high, medium or low priority. */
public String getPriority() 
{
return (String)get_Value("Priority");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_Value ("Processed", new Boolean(Processed));
}
/** Get Processed.
The document has been processed */
public boolean isProcessed() 
{
Object oo = get_Value("Processed");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Process Now */
public void setProcessing (boolean Processing)
{
set_Value ("Processing", new Boolean(Processing));
}
/** Get Process Now */
public boolean isProcessing() 
{
Object oo = get_Value("Processing");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Mail Template.
Text templates for mailings */
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) set_Value ("R_MailText_ID", null);
 else 
set_Value ("R_MailText_ID", new Integer(R_MailText_ID));
}
/** Get Mail Template.
Text templates for mailings */
public int getR_MailText_ID() 
{
Integer ii = (Integer)get_Value("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public void setR_RequestType_ID (int R_RequestType_ID)
{
set_Value ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
/** Get Request Type.
Type of request (e.g. Inquiry, Complaint, ..) */
public int getR_RequestType_ID() 
{
Integer ii = (Integer)get_Value("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request.
Request from a Business Partner or Prospect */
public void setR_Request_ID (int R_Request_ID)
{
set_ValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
/** Get Request.
Request from a Business Partner or Prospect */
public int getR_Request_ID() 
{
Integer ii = (Integer)get_Value("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Request Amount.
Amount associated with this request */
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
set_Value ("RequestAmt", RequestAmt);
}
/** Get Request Amount.
Amount associated with this request */
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Result.
Result of the action taken */
public void setResult (String Result)
{
if (Result != null && Result.length() > 2000)
{
log.warn("setResult - length > 2000 - truncated");
Result = Result.substring(0,1999);
}
set_Value ("Result", Result);
}
/** Get Result.
Result of the action taken */
public String getResult() 
{
return (String)get_Value("Result");
}
public static final int SALESREP_ID_AD_Reference_ID=286;
/** Set Sales Representative.
Sales Representative or Company Agent */
public void setSalesRep_ID (int SalesRep_ID)
{
set_Value ("SalesRep_ID", new Integer(SalesRep_ID));
}
/** Get Sales Representative.
Sales Representative or Company Agent */
public int getSalesRep_ID() 
{
Integer ii = (Integer)get_Value("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Summary.
Textual summary of this request */
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
if (Summary.length() > 2000)
{
log.warn("setSummary - length > 2000 - truncated");
Summary = Summary.substring(0,1999);
}
set_Value ("Summary", Summary);
}
/** Get Summary.
Textual summary of this request */
public String getSummary() 
{
return (String)get_Value("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.26 2003/10/31 05:30:53 jjanke Exp $ **/
public class X_R_Request extends PO
{
/** Standard Constructor **/
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
/** Load Constructor **/
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
if (AD_User_ID == 0) setValue ("AD_User_ID", null);
 else 
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID() 
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType() 
{
return (String)getValue("ActionType");
}
public void setC_BPartner_ID (int C_BPartner_ID)
{
if (C_BPartner_ID == 0) setValue ("C_BPartner_ID", null);
 else 
setValue ("C_BPartner_ID", new Integer(C_BPartner_ID));
}
public int getC_BPartner_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Campaign_ID (int C_Campaign_ID)
{
if (C_Campaign_ID == 0) setValue ("C_Campaign_ID", null);
 else 
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID() 
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
if (C_Invoice_ID == 0) setValue ("C_Invoice_ID", null);
 else 
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID() 
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
if (C_Order_ID == 0) setValue ("C_Order_ID", null);
 else 
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID() 
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
if (C_Payment_ID == 0) setValue ("C_Payment_ID", null);
 else 
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID() 
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
if (C_Project_ID == 0) setValue ("C_Project_ID", null);
 else 
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID() 
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction() 
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction() 
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo() 
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType() 
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated() 
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService() 
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult() 
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
if (M_Product_ID == 0) setValue ("M_Product_ID", null);
 else 
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID() 
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject() 
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText() 
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_FollowUp = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction() 
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority() 
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed() 
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing() 
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
if (R_MailText_ID == 0) setValue ("R_MailText_ID", null);
 else 
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID() 
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID() 
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID() 
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt() 
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult() 
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID() 
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary() 
{
return (String)getValue("Summary");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for R_Request
 ** @version $Id: X_R_Request.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_R_Request extends PO
{
public X_R_Request (Properties ctx, int R_Request_ID)
{
super (ctx, R_Request_ID);
/** if (R_Request_ID == 0)
{
setDocumentNo (null);
setDueType (null);
setIsEscalated (false);
setIsSelfService (false);
setPriority (null);
setProcessed (false);
setR_RequestType_ID (0);
setR_Request_ID (0);
setRequestAmt (Env.ZERO);
setSalesRep_ID (0);
setSummary (null);
}
 **/
}
public X_R_Request (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 417;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_R_Request[").append(getID()).append("]");
return sb.toString();
}
public void setAD_User_ID (int AD_User_ID)
{
setValue ("AD_User_ID", new Integer(AD_User_ID));
}
public int getAD_User_ID()
{
Integer ii = (Integer)getValue("AD_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final String ACTIONTYPE_Call = "C";
public static final String ACTIONTYPE_EMail = "E";
public static final String ACTIONTYPE_Close = "X";
public static final String ACTIONTYPE_Mail = "M";
public static final String ACTIONTYPE_OfferQuote = "Q";
public static final String ACTIONTYPE_Invoice = "I";
public static final String ACTIONTYPE_Order = "O";
public static final String ACTIONTYPE_Credit = "R";
public static final String ACTIONTYPE_Reminder = "A";
public static final String ACTIONTYPE_Transfer = "T";
public void setActionType (String ActionType)
{
if (ActionType.equals("C") || ActionType.equals("E") || ActionType.equals("X") || ActionType.equals("M") || ActionType.equals("Q") || ActionType.equals("I") || ActionType.equals("O") || ActionType.equals("R") || ActionType.equals("A") || ActionType.equals("T"));
 else throw new IllegalArgumentException ("ActionType Invalid value - Reference_ID=220 - C - E - X - M - Q - I - O - R - A - T");
setValue ("ActionType", ActionType);
}
public String getActionType()
{
return (String)getValue("ActionType");
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
public void setC_Campaign_ID (int C_Campaign_ID)
{
setValue ("C_Campaign_ID", new Integer(C_Campaign_ID));
}
public int getC_Campaign_ID()
{
Integer ii = (Integer)getValue("C_Campaign_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Invoice_ID (int C_Invoice_ID)
{
setValue ("C_Invoice_ID", new Integer(C_Invoice_ID));
}
public int getC_Invoice_ID()
{
Integer ii = (Integer)getValue("C_Invoice_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Order_ID (int C_Order_ID)
{
setValue ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID()
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Payment_ID (int C_Payment_ID)
{
setValue ("C_Payment_ID", new Integer(C_Payment_ID));
}
public int getC_Payment_ID()
{
Integer ii = (Integer)getValue("C_Payment_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Project_ID (int C_Project_ID)
{
setValue ("C_Project_ID", new Integer(C_Project_ID));
}
public int getC_Project_ID()
{
Integer ii = (Integer)getValue("C_Project_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setDateLastAction (Timestamp DateLastAction)
{
setValueNoCheck ("DateLastAction", DateLastAction);
}
public Timestamp getDateLastAction()
{
return (Timestamp)getValue("DateLastAction");
}
public void setDateNextAction (Timestamp DateNextAction)
{
setValue ("DateNextAction", DateNextAction);
}
public Timestamp getDateNextAction()
{
return (Timestamp)getValue("DateNextAction");
}
public void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValue ("DocumentNo", DocumentNo);
}
public String getDocumentNo()
{
return (String)getValue("DocumentNo");
}
public static final String DUETYPE_Overdue = "3";
public static final String DUETYPE_Due = "5";
public static final String DUETYPE_Scheduled = "7";
public void setDueType (String DueType)
{
if (DueType.equals("3") || DueType.equals("5") || DueType.equals("7"));
 else throw new IllegalArgumentException ("DueType Invalid value - Reference_ID=222 - 3 - 5 - 7");
if (DueType == null) throw new IllegalArgumentException ("DueType is mandatory");
setValue ("DueType", DueType);
}
public String getDueType()
{
return (String)getValue("DueType");
}
public void setIsEscalated (boolean IsEscalated)
{
setValue ("IsEscalated", new Boolean(IsEscalated));
}
public boolean isEscalated()
{
Boolean bb = (Boolean)getValue("IsEscalated");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsSelfService (boolean IsSelfService)
{
setValueNoCheck ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService()
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setLastResult (String LastResult)
{
setValue ("LastResult", LastResult);
}
public String getLastResult()
{
return (String)getValue("LastResult");
}
public void setM_Product_ID (int M_Product_ID)
{
setValue ("M_Product_ID", new Integer(M_Product_ID));
}
public int getM_Product_ID()
{
Integer ii = (Integer)getValue("M_Product_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setMailSubject (String MailSubject)
{
setValue ("MailSubject", MailSubject);
}
public String getMailSubject()
{
return (String)getValue("MailSubject");
}
public void setMailText (String MailText)
{
setValue ("MailText", MailText);
}
public String getMailText()
{
return (String)getValue("MailText");
}
public static final String NEXTACTION_None = "N";
public static final String NEXTACTION_Followup = "F";
public void setNextAction (String NextAction)
{
if (NextAction.equals("N") || NextAction.equals("F"));
 else throw new IllegalArgumentException ("NextAction Invalid value - Reference_ID=219 - N - F");
setValue ("NextAction", NextAction);
}
public String getNextAction()
{
return (String)getValue("NextAction");
}
public static final String PRIORITY_High = "3";
public static final String PRIORITY_Medium = "5";
public static final String PRIORITY_Low = "7";
public void setPriority (String Priority)
{
if (Priority.equals("3") || Priority.equals("5") || Priority.equals("7"));
 else throw new IllegalArgumentException ("Priority Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (Priority == null) throw new IllegalArgumentException ("Priority is mandatory");
setValue ("Priority", Priority);
}
public String getPriority()
{
return (String)getValue("Priority");
}
public void setProcessed (boolean Processed)
{
setValue ("Processed", new Boolean(Processed));
}
public boolean isProcessed()
{
Boolean bb = (Boolean)getValue("Processed");
if (bb != null) return bb.booleanValue();
return false;
}
public void setProcessing (String Processing)
{
setValue ("Processing", Processing);
}
public String getProcessing()
{
return (String)getValue("Processing");
}
public void setR_MailText_ID (int R_MailText_ID)
{
setValue ("R_MailText_ID", new Integer(R_MailText_ID));
}
public int getR_MailText_ID()
{
Integer ii = (Integer)getValue("R_MailText_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setR_RequestType_ID (int R_RequestType_ID)
{
setValue ("R_RequestType_ID", new Integer(R_RequestType_ID));
}
public int getR_RequestType_ID()
{
Integer ii = (Integer)getValue("R_RequestType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setR_Request_ID (int R_Request_ID)
{
setValueNoCheck ("R_Request_ID", new Integer(R_Request_ID));
}
public int getR_Request_ID()
{
Integer ii = (Integer)getValue("R_Request_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setRequestAmt (BigDecimal RequestAmt)
{
if (RequestAmt == null) throw new IllegalArgumentException ("RequestAmt is mandatory");
setValue ("RequestAmt", RequestAmt);
}
public BigDecimal getRequestAmt()
{
BigDecimal bd = (BigDecimal)getValue("RequestAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setResult (String Result)
{
setValue ("Result", Result);
}
public String getResult()
{
return (String)getValue("Result");
}
public void setSalesRep_ID (int SalesRep_ID)
{
setValue ("SalesRep_ID", new Integer(SalesRep_ID));
}
public int getSalesRep_ID()
{
Integer ii = (Integer)getValue("SalesRep_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setSummary (String Summary)
{
if (Summary == null) throw new IllegalArgumentException ("Summary is mandatory");
setValue ("Summary", Summary);
}
public String getSummary()
{
return (String)getValue("Summary");
}
}
