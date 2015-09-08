/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_Order
 ** @version $Id: X_C_Order.java,v 1.73 2004/05/20 05:59:13 jjanke Exp $ */
public class X_C_Order extends PO
{
/** Standard Constructor */
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);	// @C_Currency_ID@
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);	// 0
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDateOrdered (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDatePromised (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDeliveryRule (null);	// F
setDeliveryViaRule (null);	// P
setDocAction (null);	// CO
setDocStatus (null);	// DR
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);	// I
setGrandTotal (Env.ZERO);
setInvoiceRule (null);	// I
setIsApproved (false);	// @IsApproved@
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsDropShip (false);	// N
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);	// @IsSOTrx@
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);	// B
setPosted (false);	// N
setPriorityRule (null);	// 5
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 */
}
/** Load Constructor */
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=259;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
/** Set Trx Organization.
Performing or initiating organization */
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) set_Value ("AD_OrgTrx_ID", null);
 else 
set_Value ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
/** Get Trx Organization.
Performing or initiating organization */
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)get_Value("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public static final int BILL_BPARTNER_ID_AD_Reference_ID=138;
/** Set Invoice Partner.
Business Partner to be invoiced */
public void setBill_BPartner_ID (int Bill_BPartner_ID)
{
if (Bill_BPartner_ID == 0) set_Value ("Bill_BPartner_ID", null);
 else 
set_Value ("Bill_BPartner_ID", new Integer(Bill_BPartner_ID));
}
/** Get Invoice Partner.
Business Partner to be invoiced */
public int getBill_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Bill_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_LOCATION_ID_AD_Reference_ID=159;
/** Set Invoice Location.
Business Partner Location for invoicing */
public void setBill_Location_ID (int Bill_Location_ID)
{
if (Bill_Location_ID == 0) set_Value ("Bill_Location_ID", null);
 else 
set_Value ("Bill_Location_ID", new Integer(Bill_Location_ID));
}
/** Get Invoice Location.
Business Partner Location for invoicing */
public int getBill_Location_ID() 
{
Integer ii = (Integer)get_Value("Bill_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_USER_ID_AD_Reference_ID=110;
/** Set Invoice Contact.
Business Partner Contact for invoicing */
public void setBill_User_ID (int Bill_User_ID)
{
if (Bill_User_ID == 0) set_Value ("Bill_User_ID", null);
 else 
set_Value ("Bill_User_ID", new Integer(Bill_User_ID));
}
/** Get Invoice Contact.
Business Partner Contact for invoicing */
public int getBill_User_ID() 
{
Integer ii = (Integer)get_Value("Bill_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Activity.
Business Activity */
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) set_Value ("C_Activity_ID", null);
 else 
set_Value ("C_Activity_ID", new Integer(C_Activity_ID));
}
/** Get Activity.
Business Activity */
public int getC_Activity_ID() 
{
Integer ii = (Integer)get_Value("C_Activity_ID");
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
/** Set Partner Location.
Identifies the (ship to) address for this Business Partner */
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
set_Value ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
/** Get Partner Location.
Identifies the (ship to) address for this Business Partner */
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_Location_ID");
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
/** Set Cash Journal Line.
Cash Journal Line */
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) set_Value ("C_CashLine_ID", null);
 else 
set_Value ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
/** Get Cash Journal Line.
Cash Journal Line */
public int getC_CashLine_ID() 
{
Integer ii = (Integer)get_Value("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_CHARGE_ID_AD_Reference_ID=200;
/** Set Charge.
Additional document charges */
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) set_Value ("C_Charge_ID", null);
 else 
set_Value ("C_Charge_ID", new Integer(C_Charge_ID));
}
/** Get Charge.
Additional document charges */
public int getC_Charge_ID() 
{
Integer ii = (Integer)get_Value("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency Type.
Currency Conversion Rate Type */
public void setC_ConversionType_ID (int C_ConversionType_ID)
{
if (C_ConversionType_ID == 0) set_Value ("C_ConversionType_ID", null);
 else 
set_Value ("C_ConversionType_ID", new Integer(C_ConversionType_ID));
}
/** Get Currency Type.
Currency Conversion Rate Type */
public int getC_ConversionType_ID() 
{
Integer ii = (Integer)get_Value("C_ConversionType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
set_ValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPETARGET_ID_AD_Reference_ID=170;
/** Set Target Document Type.
Target document type for conversing documents */
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
set_Value ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
/** Get Target Document Type.
Target document type for conversing documents */
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)get_Value("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPE_ID_AD_Reference_ID=170;
/** Set Document Type.
Document type or rules */
public void setC_DocType_ID (int C_DocType_ID)
{
set_ValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
/** Get Document Type.
Document type or rules */
public int getC_DocType_ID() 
{
Integer ii = (Integer)get_Value("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
set_ValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Term.
The terms for Payment of this transaction */
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
set_Value ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
/** Get Payment Term.
The terms for Payment of this transaction */
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)get_Value("C_PaymentTerm_ID");
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
/** Set Charge amount.
Charge Amount */
public void setChargeAmt (BigDecimal ChargeAmt)
{
set_Value ("ChargeAmt", ChargeAmt);
}
/** Get Charge amount.
Charge Amount */
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Copy From.
Copy From Record */
public void setCopyFrom (String CopyFrom)
{
if (CopyFrom != null && CopyFrom.length() > 1)
{
log.warn("setCopyFrom - length > 1 - truncated");
CopyFrom = CopyFrom.substring(0,0);
}
set_Value ("CopyFrom", CopyFrom);
}
/** Get Copy From.
Copy From Record */
public String getCopyFrom() 
{
return (String)get_Value("CopyFrom");
}
/** Set Account Date.
Accounting Date */
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
set_Value ("DateAcct", DateAcct);
}
/** Get Account Date.
Accounting Date */
public Timestamp getDateAcct() 
{
return (Timestamp)get_Value("DateAcct");
}
/** Set Date Ordered.
Date of Order */
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
set_Value ("DateOrdered", DateOrdered);
}
/** Get Date Ordered.
Date of Order */
public Timestamp getDateOrdered() 
{
return (Timestamp)get_Value("DateOrdered");
}
/** Set Date printed.
Date the document was printed. */
public void setDatePrinted (Timestamp DatePrinted)
{
set_Value ("DatePrinted", DatePrinted);
}
/** Get Date printed.
Date the document was printed. */
public Timestamp getDatePrinted() 
{
return (Timestamp)get_Value("DatePrinted");
}
/** Set Date Promised.
Date Order was promised */
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
set_Value ("DatePromised", DatePromised);
}
/** Get Date Promised.
Date Order was promised */
public Timestamp getDatePromised() 
{
return (Timestamp)get_Value("DatePromised");
}
public static final int DELIVERYRULE_AD_Reference_ID=151;
/** After Receipt = R */
public static final String DELIVERYRULE_AfterReceipt = "R";
/** Availability = A */
public static final String DELIVERYRULE_Availability = "A";
/** Complete Line = L */
public static final String DELIVERYRULE_CompleteLine = "L";
/** Complete Order = O */
public static final String DELIVERYRULE_CompleteOrder = "O";
/** Force = F */
public static final String DELIVERYRULE_Force = "F";
/** Set Delivery Rule.
Defines the timing of Delivery */
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O") || DeliveryRule.equals("F"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O - F");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
if (DeliveryRule.length() > 1)
{
log.warn("setDeliveryRule - length > 1 - truncated");
DeliveryRule = DeliveryRule.substring(0,0);
}
set_Value ("DeliveryRule", DeliveryRule);
}
/** Get Delivery Rule.
Defines the timing of Delivery */
public String getDeliveryRule() 
{
return (String)get_Value("DeliveryRule");
}
public static final int DELIVERYVIARULE_AD_Reference_ID=152;
/** Pickup = P */
public static final String DELIVERYVIARULE_Pickup = "P";
/** Delivery = D */
public static final String DELIVERYVIARULE_Delivery = "D";
/** Shipper = S */
public static final String DELIVERYVIARULE_Shipper = "S";
/** Set Delivery Via.
How the order will be delivered */
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
if (DeliveryViaRule.length() > 1)
{
log.warn("setDeliveryViaRule - length > 1 - truncated");
DeliveryViaRule = DeliveryViaRule.substring(0,0);
}
set_Value ("DeliveryViaRule", DeliveryViaRule);
}
/** Get Delivery Via.
How the order will be delivered */
public String getDeliveryViaRule() 
{
return (String)get_Value("DeliveryViaRule");
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
public static final int DOCACTION_AD_Reference_ID=135;
/** Complete = CO */
public static final String DOCACTION_Complete = "CO";
/** Approve = AP */
public static final String DOCACTION_Approve = "AP";
/** Reject = RJ */
public static final String DOCACTION_Reject = "RJ";
/** Post = PO */
public static final String DOCACTION_Post = "PO";
/** Void = VO */
public static final String DOCACTION_Void = "VO";
/** Close = CL */
public static final String DOCACTION_Close = "CL";
/** Reverse - Correct = RC */
public static final String DOCACTION_Reverse_Correct = "RC";
/** Reverse - Accrual = RA */
public static final String DOCACTION_Reverse_Accrual = "RA";
/** Invalidate = IN */
public static final String DOCACTION_Invalidate = "IN";
/** Re-activate = RE */
public static final String DOCACTION_Re_Activate = "RE";
/** <None> = -- */
public static final String DOCACTION_None = "--";
/** Prepare = PR */
public static final String DOCACTION_Prepare = "PR";
/** Unlock = XL */
public static final String DOCACTION_Unlock = "XL";
/** Set Document Action.
The targeted status of the document */
public void setDocAction (String DocAction)
{
if (DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("IN") || DocAction.equals("RE") || DocAction.equals("--") || DocAction.equals("PR") || DocAction.equals("XL"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - CO - AP - RJ - PO - VO - CL - RC - RA - IN - RE - -- - PR - XL");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
if (DocAction.length() > 2)
{
log.warn("setDocAction - length > 2 - truncated");
DocAction = DocAction.substring(0,1);
}
set_Value ("DocAction", DocAction);
}
/** Get Document Action.
The targeted status of the document */
public String getDocAction() 
{
return (String)get_Value("DocAction");
}
public static final int DOCSTATUS_AD_Reference_ID=131;
/** Drafted = DR */
public static final String DOCSTATUS_Drafted = "DR";
/** Completed = CO */
public static final String DOCSTATUS_Completed = "CO";
/** Approved = AP */
public static final String DOCSTATUS_Approved = "AP";
/** Not Approved = NA */
public static final String DOCSTATUS_NotApproved = "NA";
/** Voided = VO */
public static final String DOCSTATUS_Voided = "VO";
/** Invalid = IN */
public static final String DOCSTATUS_Invalid = "IN";
/** Reversed = RE */
public static final String DOCSTATUS_Reversed = "RE";
/** Closed = CL */
public static final String DOCSTATUS_Closed = "CL";
/** Unknown = ?? */
public static final String DOCSTATUS_Unknown = "??";
/** Waiting Confirmation = WC */
public static final String DOCSTATUS_WaitingConfirmation = "WC";
/** In Progress = IP */
public static final String DOCSTATUS_InProgress = "IP";
/** Waiting Payment = WP */
public static final String DOCSTATUS_WaitingPayment = "WP";
/** Set Document Status.
The current status of the document */
public void setDocStatus (String DocStatus)
{
if (DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("NA") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("WC") || DocStatus.equals("IP") || DocStatus.equals("WP"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - DR - CO - AP - NA - VO - IN - RE - CL - ?? - WC - IP - WP");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
if (DocStatus.length() > 2)
{
log.warn("setDocStatus - length > 2 - truncated");
DocStatus = DocStatus.substring(0,1);
}
set_Value ("DocStatus", DocStatus);
}
/** Get Document Status.
The current status of the document */
public String getDocStatus() 
{
return (String)get_Value("DocStatus");
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
set_ValueNoCheck ("DocumentNo", DocumentNo);
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
/** Set Freight Amount.
Freight Amount  */
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
set_Value ("FreightAmt", FreightAmt);
}
/** Get Freight Amount.
Freight Amount  */
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int FREIGHTCOSTRULE_AD_Reference_ID=153;
/** Freight included = I */
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
/** Fix price = F */
public static final String FREIGHTCOSTRULE_FixPrice = "F";
/** Calculated = C */
public static final String FREIGHTCOSTRULE_Calculated = "C";
/** Line = L */
public static final String FREIGHTCOSTRULE_Line = "L";
/** Set Freight Cost Rule.
Method for charging Freight */
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
if (FreightCostRule.length() > 1)
{
log.warn("setFreightCostRule - length > 1 - truncated");
FreightCostRule = FreightCostRule.substring(0,0);
}
set_Value ("FreightCostRule", FreightCostRule);
}
/** Get Freight Cost Rule.
Method for charging Freight */
public String getFreightCostRule() 
{
return (String)get_Value("FreightCostRule");
}
/** Set Grand Total.
Total amount of document */
public void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
set_ValueNoCheck ("GrandTotal", GrandTotal);
}
/** Get Grand Total.
Total amount of document */
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)get_Value("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int INVOICERULE_AD_Reference_ID=150;
/** After Order delivered = O */
public static final String INVOICERULE_AfterOrderDelivered = "O";
/** After Delivery = D */
public static final String INVOICERULE_AfterDelivery = "D";
/** Customer Schedule after Delivery = S */
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
/** Immediate = I */
public static final String INVOICERULE_Immediate = "I";
/** Set Invoice Rule.
Frequency and method of invoicing  */
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
if (InvoiceRule.length() > 1)
{
log.warn("setInvoiceRule - length > 1 - truncated");
InvoiceRule = InvoiceRule.substring(0,0);
}
set_Value ("InvoiceRule", InvoiceRule);
}
/** Get Invoice Rule.
Frequency and method of invoicing  */
public String getInvoiceRule() 
{
return (String)get_Value("InvoiceRule");
}
/** Set Approved.
Indicates if this document requires approval */
public void setIsApproved (boolean IsApproved)
{
set_ValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
/** Get Approved.
Indicates if this document requires approval */
public boolean isApproved() 
{
Object oo = get_Value("IsApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Credit Approved.
Credit  has been approved */
public void setIsCreditApproved (boolean IsCreditApproved)
{
set_ValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
/** Get Credit Approved.
Credit  has been approved */
public boolean isCreditApproved() 
{
Object oo = get_Value("IsCreditApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Delivered */
public void setIsDelivered (boolean IsDelivered)
{
set_ValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
/** Get Delivered */
public boolean isDelivered() 
{
Object oo = get_Value("IsDelivered");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Discount Printed.
Print Discount on Invoice and Order */
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
set_Value ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
/** Get Discount Printed.
Print Discount on Invoice and Order */
public boolean isDiscountPrinted() 
{
Object oo = get_Value("IsDiscountPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public void setIsDropShip (boolean IsDropShip)
{
set_ValueNoCheck ("IsDropShip", new Boolean(IsDropShip));
}
/** Get Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public boolean isDropShip() 
{
Object oo = get_Value("IsDropShip");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Invoiced.
It is invoiced */
public void setIsInvoiced (boolean IsInvoiced)
{
set_ValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
/** Get Invoiced.
It is invoiced */
public boolean isInvoiced() 
{
Object oo = get_Value("IsInvoiced");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Printed.
Indicates if this document / line is printed */
public void setIsPrinted (boolean IsPrinted)
{
set_ValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
/** Get Printed.
Indicates if this document / line is printed */
public boolean isPrinted() 
{
Object oo = get_Value("IsPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Sales Transaction.
This is a Sales Transaction */
public void setIsSOTrx (boolean IsSOTrx)
{
set_Value ("IsSOTrx", new Boolean(IsSOTrx));
}
/** Get Sales Transaction.
This is a Sales Transaction */
public boolean isSOTrx() 
{
Object oo = get_Value("IsSOTrx");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Selected */
public void setIsSelected (boolean IsSelected)
{
set_Value ("IsSelected", new Boolean(IsSelected));
}
/** Get Selected */
public boolean isSelected() 
{
Object oo = get_Value("IsSelected");
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
set_Value ("IsSelfService", new Boolean(IsSelfService));
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
/** Set Price includes Tax.
Tax is included in the price  */
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
set_Value ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
/** Get Price includes Tax.
Tax is included in the price  */
public boolean isTaxIncluded() 
{
Object oo = get_Value("IsTaxIncluded");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Transferred.
Transferred to General Ledger (i.e. accounted) */
public void setIsTransferred (boolean IsTransferred)
{
set_ValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
/** Get Transferred.
Transferred to General Ledger (i.e. accounted) */
public boolean isTransferred() 
{
Object oo = get_Value("IsTransferred");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Price List.
Unique identifier of a Price List */
public void setM_PriceList_ID (int M_PriceList_ID)
{
set_Value ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
/** Get Price List.
Unique identifier of a Price List */
public int getM_PriceList_ID() 
{
Integer ii = (Integer)get_Value("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Shipper.
Method or manner of product delivery */
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) set_Value ("M_Shipper_ID", null);
 else 
set_Value ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
/** Get Shipper.
Method or manner of product delivery */
public int getM_Shipper_ID() 
{
Integer ii = (Integer)get_Value("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int M_WAREHOUSE_ID_AD_Reference_ID=197;
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_ValueNoCheck ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public void setPOReference (String POReference)
{
if (POReference != null && POReference.length() > 20)
{
log.warn("setPOReference - length > 20 - truncated");
POReference = POReference.substring(0,19);
}
set_Value ("POReference", POReference);
}
/** Get Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public String getPOReference() 
{
return (String)get_Value("POReference");
}
/** Set Payment BPartner.
Business Partner responsible for the payment */
public void setPay_BPartner_ID (int Pay_BPartner_ID)
{
if (Pay_BPartner_ID == 0) set_Value ("Pay_BPartner_ID", null);
 else 
set_Value ("Pay_BPartner_ID", new Integer(Pay_BPartner_ID));
}
/** Get Payment BPartner.
Business Partner responsible for the payment */
public int getPay_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Pay_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Location.
Location of the Business Partner responsible for the payment */
public void setPay_Location_ID (int Pay_Location_ID)
{
if (Pay_Location_ID == 0) set_Value ("Pay_Location_ID", null);
 else 
set_Value ("Pay_Location_ID", new Integer(Pay_Location_ID));
}
/** Get Payment Location.
Location of the Business Partner responsible for the payment */
public int getPay_Location_ID() 
{
Integer ii = (Integer)get_Value("Pay_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int PAYMENTRULE_AD_Reference_ID=195;
/** Direct Debit = D */
public static final String PAYMENTRULE_DirectDebit = "D";
/** Cash = B */
public static final String PAYMENTRULE_Cash = "B";
/** Credit Card = K */
public static final String PAYMENTRULE_CreditCard = "K";
/** Direct Deposit = T */
public static final String PAYMENTRULE_DirectDeposit = "T";
/** Check = S */
public static final String PAYMENTRULE_Check = "S";
/** On Credit = P */
public static final String PAYMENTRULE_OnCredit = "P";
/** Set Payment Rule.
How you pay the invoice */
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("D") || PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - D - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
if (PaymentRule.length() > 1)
{
log.warn("setPaymentRule - length > 1 - truncated");
PaymentRule = PaymentRule.substring(0,0);
}
set_Value ("PaymentRule", PaymentRule);
}
/** Get Payment Rule.
How you pay the invoice */
public String getPaymentRule() 
{
return (String)get_Value("PaymentRule");
}
/** Set Posted.
Posting status */
public void setPosted (boolean Posted)
{
set_Value ("Posted", new Boolean(Posted));
}
/** Get Posted.
Posting status */
public boolean isPosted() 
{
Object oo = get_Value("Posted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
public static final int PRIORITYRULE_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITYRULE_High = "3";
/** Medium = 5 */
public static final String PRIORITYRULE_Medium = "5";
/** Low = 7 */
public static final String PRIORITYRULE_Low = "7";
/** Set Priority.
Priority of a document */
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
if (PriorityRule.length() > 1)
{
log.warn("setPriorityRule - length > 1 - truncated");
PriorityRule = PriorityRule.substring(0,0);
}
set_Value ("PriorityRule", PriorityRule);
}
/** Get Priority.
Priority of a document */
public String getPriorityRule() 
{
return (String)get_Value("PriorityRule");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_ValueNoCheck ("Processed", new Boolean(Processed));
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
/** Set Referenced Order.
Reference to corresponding Sales/Purchase Order */
public void setRef_Order_ID (int Ref_Order_ID)
{
if (Ref_Order_ID == 0) set_Value ("Ref_Order_ID", null);
 else 
set_Value ("Ref_Order_ID", new Integer(Ref_Order_ID));
}
/** Get Referenced Order.
Reference to corresponding Sales/Purchase Order */
public int getRef_Order_ID() 
{
Integer ii = (Integer)get_Value("Ref_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int SALESREP_ID_AD_Reference_ID=190;
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
/** Set Send EMail.
Enable sending Document EMail */
public void setSendEMail (boolean SendEMail)
{
set_Value ("SendEMail", new Boolean(SendEMail));
}
/** Get Send EMail.
Enable sending Document EMail */
public boolean isSendEMail() 
{
Object oo = get_Value("SendEMail");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Total Lines.
Total of all document lines */
public void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
set_ValueNoCheck ("TotalLines", TotalLines);
}
/** Get Total Lines.
Total of all document lines */
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)get_Value("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int USER1_ID_AD_Reference_ID=134;
/** Set User1.
User defined element #1 */
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) set_Value ("User1_ID", null);
 else 
set_Value ("User1_ID", new Integer(User1_ID));
}
/** Get User1.
User defined element #1 */
public int getUser1_ID() 
{
Integer ii = (Integer)get_Value("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USER2_ID_AD_Reference_ID=137;
/** Set User2.
User defined element #2 */
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) set_Value ("User2_ID", null);
 else 
set_Value ("User2_ID", new Integer(User2_ID));
}
/** Get User2.
User defined element #2 */
public int getUser2_ID() 
{
Integer ii = (Integer)get_Value("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_Order
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:09.822 */
public class X_C_Order extends PO
{
/** Standard Constructor */
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);	// @C_Currency_ID@
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);	// 0
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDateOrdered (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDatePromised (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDeliveryRule (null);	// F
setDeliveryViaRule (null);	// P
setDocAction (null);	// CO
setDocStatus (null);	// DR
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);	// I
setGrandTotal (Env.ZERO);
setInvoiceRule (null);	// I
setIsApproved (false);	// @IsApproved@
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsDropShip (false);	// N
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);	// @IsSOTrx@
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);	// B
setPosted (false);	// N
setPriorityRule (null);	// 5
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 */
}
/** Load Constructor */
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=259 */
public static final int Table_ID=259;

/** TableName=C_Order */
public static final String Table_Name="C_Order";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
/** Set Trx Organization.
Performing or initiating organization */
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) set_Value ("AD_OrgTrx_ID", null);
 else 
set_Value ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
/** Get Trx Organization.
Performing or initiating organization */
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)get_Value("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public static final int BILL_BPARTNER_ID_AD_Reference_ID=138;
/** Set Invoice Partner.
Business Partner to be invoiced */
public void setBill_BPartner_ID (int Bill_BPartner_ID)
{
if (Bill_BPartner_ID == 0) set_Value ("Bill_BPartner_ID", null);
 else 
set_Value ("Bill_BPartner_ID", new Integer(Bill_BPartner_ID));
}
/** Get Invoice Partner.
Business Partner to be invoiced */
public int getBill_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Bill_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_LOCATION_ID_AD_Reference_ID=159;
/** Set Invoice Location.
Business Partner Location for invoicing */
public void setBill_Location_ID (int Bill_Location_ID)
{
if (Bill_Location_ID == 0) set_Value ("Bill_Location_ID", null);
 else 
set_Value ("Bill_Location_ID", new Integer(Bill_Location_ID));
}
/** Get Invoice Location.
Business Partner Location for invoicing */
public int getBill_Location_ID() 
{
Integer ii = (Integer)get_Value("Bill_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_USER_ID_AD_Reference_ID=110;
/** Set Invoice Contact.
Business Partner Contact for invoicing */
public void setBill_User_ID (int Bill_User_ID)
{
if (Bill_User_ID == 0) set_Value ("Bill_User_ID", null);
 else 
set_Value ("Bill_User_ID", new Integer(Bill_User_ID));
}
/** Get Invoice Contact.
Business Partner Contact for invoicing */
public int getBill_User_ID() 
{
Integer ii = (Integer)get_Value("Bill_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Activity.
Business Activity */
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) set_Value ("C_Activity_ID", null);
 else 
set_Value ("C_Activity_ID", new Integer(C_Activity_ID));
}
/** Get Activity.
Business Activity */
public int getC_Activity_ID() 
{
Integer ii = (Integer)get_Value("C_Activity_ID");
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
/** Set Partner Location.
Identifies the (ship to) address for this Business Partner */
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
set_Value ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
/** Get Partner Location.
Identifies the (ship to) address for this Business Partner */
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_Location_ID");
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
/** Set Cash Journal Line.
Cash Journal Line */
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) set_Value ("C_CashLine_ID", null);
 else 
set_Value ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
/** Get Cash Journal Line.
Cash Journal Line */
public int getC_CashLine_ID() 
{
Integer ii = (Integer)get_Value("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_CHARGE_ID_AD_Reference_ID=200;
/** Set Charge.
Additional document charges */
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) set_Value ("C_Charge_ID", null);
 else 
set_Value ("C_Charge_ID", new Integer(C_Charge_ID));
}
/** Get Charge.
Additional document charges */
public int getC_Charge_ID() 
{
Integer ii = (Integer)get_Value("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency Type.
Currency Conversion Rate Type */
public void setC_ConversionType_ID (int C_ConversionType_ID)
{
if (C_ConversionType_ID == 0) set_Value ("C_ConversionType_ID", null);
 else 
set_Value ("C_ConversionType_ID", new Integer(C_ConversionType_ID));
}
/** Get Currency Type.
Currency Conversion Rate Type */
public int getC_ConversionType_ID() 
{
Integer ii = (Integer)get_Value("C_ConversionType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
set_ValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPETARGET_ID_AD_Reference_ID=170;
/** Set Target Document Type.
Target document type for conversing documents */
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
set_Value ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
/** Get Target Document Type.
Target document type for conversing documents */
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)get_Value("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPE_ID_AD_Reference_ID=170;
/** Set Document Type.
Document type or rules */
public void setC_DocType_ID (int C_DocType_ID)
{
set_ValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
/** Get Document Type.
Document type or rules */
public int getC_DocType_ID() 
{
Integer ii = (Integer)get_Value("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
set_ValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Term.
The terms for Payment of this transaction */
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
set_Value ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
/** Get Payment Term.
The terms for Payment of this transaction */
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)get_Value("C_PaymentTerm_ID");
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
/** Set Charge amount.
Charge Amount */
public void setChargeAmt (BigDecimal ChargeAmt)
{
set_Value ("ChargeAmt", ChargeAmt);
}
/** Get Charge amount.
Charge Amount */
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Copy From.
Copy From Record */
public void setCopyFrom (String CopyFrom)
{
if (CopyFrom != null && CopyFrom.length() > 1)
{
log.warn("setCopyFrom - length > 1 - truncated");
CopyFrom = CopyFrom.substring(0,0);
}
set_Value ("CopyFrom", CopyFrom);
}
/** Get Copy From.
Copy From Record */
public String getCopyFrom() 
{
return (String)get_Value("CopyFrom");
}
/** Set Account Date.
Accounting Date */
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
set_Value ("DateAcct", DateAcct);
}
/** Get Account Date.
Accounting Date */
public Timestamp getDateAcct() 
{
return (Timestamp)get_Value("DateAcct");
}
/** Set Date Ordered.
Date of Order */
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
set_Value ("DateOrdered", DateOrdered);
}
/** Get Date Ordered.
Date of Order */
public Timestamp getDateOrdered() 
{
return (Timestamp)get_Value("DateOrdered");
}
/** Set Date printed.
Date the document was printed. */
public void setDatePrinted (Timestamp DatePrinted)
{
set_Value ("DatePrinted", DatePrinted);
}
/** Get Date printed.
Date the document was printed. */
public Timestamp getDatePrinted() 
{
return (Timestamp)get_Value("DatePrinted");
}
/** Set Date Promised.
Date Order was promised */
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
set_Value ("DatePromised", DatePromised);
}
/** Get Date Promised.
Date Order was promised */
public Timestamp getDatePromised() 
{
return (Timestamp)get_Value("DatePromised");
}
public static final int DELIVERYRULE_AD_Reference_ID=151;
/** After Receipt = R */
public static final String DELIVERYRULE_AfterReceipt = "R";
/** Availability = A */
public static final String DELIVERYRULE_Availability = "A";
/** Complete Line = L */
public static final String DELIVERYRULE_CompleteLine = "L";
/** Complete Order = O */
public static final String DELIVERYRULE_CompleteOrder = "O";
/** Force = F */
public static final String DELIVERYRULE_Force = "F";
/** Set Delivery Rule.
Defines the timing of Delivery */
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O") || DeliveryRule.equals("F"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O - F");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
if (DeliveryRule.length() > 1)
{
log.warn("setDeliveryRule - length > 1 - truncated");
DeliveryRule = DeliveryRule.substring(0,0);
}
set_Value ("DeliveryRule", DeliveryRule);
}
/** Get Delivery Rule.
Defines the timing of Delivery */
public String getDeliveryRule() 
{
return (String)get_Value("DeliveryRule");
}
public static final int DELIVERYVIARULE_AD_Reference_ID=152;
/** Pickup = P */
public static final String DELIVERYVIARULE_Pickup = "P";
/** Delivery = D */
public static final String DELIVERYVIARULE_Delivery = "D";
/** Shipper = S */
public static final String DELIVERYVIARULE_Shipper = "S";
/** Set Delivery Via.
How the order will be delivered */
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
if (DeliveryViaRule.length() > 1)
{
log.warn("setDeliveryViaRule - length > 1 - truncated");
DeliveryViaRule = DeliveryViaRule.substring(0,0);
}
set_Value ("DeliveryViaRule", DeliveryViaRule);
}
/** Get Delivery Via.
How the order will be delivered */
public String getDeliveryViaRule() 
{
return (String)get_Value("DeliveryViaRule");
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
public static final int DOCACTION_AD_Reference_ID=135;
/** Complete = CO */
public static final String DOCACTION_Complete = "CO";
/** Approve = AP */
public static final String DOCACTION_Approve = "AP";
/** Reject = RJ */
public static final String DOCACTION_Reject = "RJ";
/** Post = PO */
public static final String DOCACTION_Post = "PO";
/** Void = VO */
public static final String DOCACTION_Void = "VO";
/** Close = CL */
public static final String DOCACTION_Close = "CL";
/** Reverse - Correct = RC */
public static final String DOCACTION_Reverse_Correct = "RC";
/** Reverse - Accrual = RA */
public static final String DOCACTION_Reverse_Accrual = "RA";
/** Invalidate = IN */
public static final String DOCACTION_Invalidate = "IN";
/** Re-activate = RE */
public static final String DOCACTION_Re_Activate = "RE";
/** <None> = -- */
public static final String DOCACTION_None = "--";
/** Wait Complete = WC */
public static final String DOCACTION_WaitComplete = "WC";
/** Prepare = PR */
public static final String DOCACTION_Prepare = "PR";
/** Unlock = XL */
public static final String DOCACTION_Unlock = "XL";
/** Set Document Action.
The targeted status of the document */
public void setDocAction (String DocAction)
{
if (DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("IN") || DocAction.equals("RE") || DocAction.equals("--") || DocAction.equals("WC") || DocAction.equals("PR") || DocAction.equals("XL"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - CO - AP - RJ - PO - VO - CL - RC - RA - IN - RE - -- - WC - PR - XL");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
if (DocAction.length() > 2)
{
log.warn("setDocAction - length > 2 - truncated");
DocAction = DocAction.substring(0,1);
}
set_Value ("DocAction", DocAction);
}
/** Get Document Action.
The targeted status of the document */
public String getDocAction() 
{
return (String)get_Value("DocAction");
}
public static final int DOCSTATUS_AD_Reference_ID=131;
/** Drafted = DR */
public static final String DOCSTATUS_Drafted = "DR";
/** Completed = CO */
public static final String DOCSTATUS_Completed = "CO";
/** Approved = AP */
public static final String DOCSTATUS_Approved = "AP";
/** Not Approved = NA */
public static final String DOCSTATUS_NotApproved = "NA";
/** Voided = VO */
public static final String DOCSTATUS_Voided = "VO";
/** Invalid = IN */
public static final String DOCSTATUS_Invalid = "IN";
/** Reversed = RE */
public static final String DOCSTATUS_Reversed = "RE";
/** Closed = CL */
public static final String DOCSTATUS_Closed = "CL";
/** Unknown = ?? */
public static final String DOCSTATUS_Unknown = "??";
/** Waiting Confirmation = WC */
public static final String DOCSTATUS_WaitingConfirmation = "WC";
/** In Progress = IP */
public static final String DOCSTATUS_InProgress = "IP";
/** Waiting Payment = WP */
public static final String DOCSTATUS_WaitingPayment = "WP";
/** Set Document Status.
The current status of the document */
public void setDocStatus (String DocStatus)
{
if (DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("NA") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("WC") || DocStatus.equals("IP") || DocStatus.equals("WP"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - DR - CO - AP - NA - VO - IN - RE - CL - ?? - WC - IP - WP");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
if (DocStatus.length() > 2)
{
log.warn("setDocStatus - length > 2 - truncated");
DocStatus = DocStatus.substring(0,1);
}
set_Value ("DocStatus", DocStatus);
}
/** Get Document Status.
The current status of the document */
public String getDocStatus() 
{
return (String)get_Value("DocStatus");
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
set_ValueNoCheck ("DocumentNo", DocumentNo);
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
/** Set Freight Amount.
Freight Amount  */
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
set_Value ("FreightAmt", FreightAmt);
}
/** Get Freight Amount.
Freight Amount  */
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int FREIGHTCOSTRULE_AD_Reference_ID=153;
/** Freight included = I */
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
/** Fix price = F */
public static final String FREIGHTCOSTRULE_FixPrice = "F";
/** Calculated = C */
public static final String FREIGHTCOSTRULE_Calculated = "C";
/** Line = L */
public static final String FREIGHTCOSTRULE_Line = "L";
/** Set Freight Cost Rule.
Method for charging Freight */
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
if (FreightCostRule.length() > 1)
{
log.warn("setFreightCostRule - length > 1 - truncated");
FreightCostRule = FreightCostRule.substring(0,0);
}
set_Value ("FreightCostRule", FreightCostRule);
}
/** Get Freight Cost Rule.
Method for charging Freight */
public String getFreightCostRule() 
{
return (String)get_Value("FreightCostRule");
}
/** Set Grand Total.
Total amount of document */
public void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
set_ValueNoCheck ("GrandTotal", GrandTotal);
}
/** Get Grand Total.
Total amount of document */
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)get_Value("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int INVOICERULE_AD_Reference_ID=150;
/** After Order delivered = O */
public static final String INVOICERULE_AfterOrderDelivered = "O";
/** After Delivery = D */
public static final String INVOICERULE_AfterDelivery = "D";
/** Customer Schedule after Delivery = S */
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
/** Immediate = I */
public static final String INVOICERULE_Immediate = "I";
/** Set Invoice Rule.
Frequency and method of invoicing  */
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
if (InvoiceRule.length() > 1)
{
log.warn("setInvoiceRule - length > 1 - truncated");
InvoiceRule = InvoiceRule.substring(0,0);
}
set_Value ("InvoiceRule", InvoiceRule);
}
/** Get Invoice Rule.
Frequency and method of invoicing  */
public String getInvoiceRule() 
{
return (String)get_Value("InvoiceRule");
}
/** Set Approved.
Indicates if this document requires approval */
public void setIsApproved (boolean IsApproved)
{
set_ValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
/** Get Approved.
Indicates if this document requires approval */
public boolean isApproved() 
{
Object oo = get_Value("IsApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Credit Approved.
Credit  has been approved */
public void setIsCreditApproved (boolean IsCreditApproved)
{
set_ValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
/** Get Credit Approved.
Credit  has been approved */
public boolean isCreditApproved() 
{
Object oo = get_Value("IsCreditApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Delivered */
public void setIsDelivered (boolean IsDelivered)
{
set_ValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
/** Get Delivered */
public boolean isDelivered() 
{
Object oo = get_Value("IsDelivered");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Discount Printed.
Print Discount on Invoice and Order */
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
set_Value ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
/** Get Discount Printed.
Print Discount on Invoice and Order */
public boolean isDiscountPrinted() 
{
Object oo = get_Value("IsDiscountPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public void setIsDropShip (boolean IsDropShip)
{
set_ValueNoCheck ("IsDropShip", new Boolean(IsDropShip));
}
/** Get Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public boolean isDropShip() 
{
Object oo = get_Value("IsDropShip");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Invoiced.
It is invoiced */
public void setIsInvoiced (boolean IsInvoiced)
{
set_ValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
/** Get Invoiced.
It is invoiced */
public boolean isInvoiced() 
{
Object oo = get_Value("IsInvoiced");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Printed.
Indicates if this document / line is printed */
public void setIsPrinted (boolean IsPrinted)
{
set_ValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
/** Get Printed.
Indicates if this document / line is printed */
public boolean isPrinted() 
{
Object oo = get_Value("IsPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Sales Transaction.
This is a Sales Transaction */
public void setIsSOTrx (boolean IsSOTrx)
{
set_Value ("IsSOTrx", new Boolean(IsSOTrx));
}
/** Get Sales Transaction.
This is a Sales Transaction */
public boolean isSOTrx() 
{
Object oo = get_Value("IsSOTrx");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Selected */
public void setIsSelected (boolean IsSelected)
{
set_Value ("IsSelected", new Boolean(IsSelected));
}
/** Get Selected */
public boolean isSelected() 
{
Object oo = get_Value("IsSelected");
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
set_Value ("IsSelfService", new Boolean(IsSelfService));
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
/** Set Price includes Tax.
Tax is included in the price  */
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
set_Value ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
/** Get Price includes Tax.
Tax is included in the price  */
public boolean isTaxIncluded() 
{
Object oo = get_Value("IsTaxIncluded");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Transferred.
Transferred to General Ledger (i.e. accounted) */
public void setIsTransferred (boolean IsTransferred)
{
set_ValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
/** Get Transferred.
Transferred to General Ledger (i.e. accounted) */
public boolean isTransferred() 
{
Object oo = get_Value("IsTransferred");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Price List.
Unique identifier of a Price List */
public void setM_PriceList_ID (int M_PriceList_ID)
{
set_Value ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
/** Get Price List.
Unique identifier of a Price List */
public int getM_PriceList_ID() 
{
Integer ii = (Integer)get_Value("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Shipper.
Method or manner of product delivery */
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) set_Value ("M_Shipper_ID", null);
 else 
set_Value ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
/** Get Shipper.
Method or manner of product delivery */
public int getM_Shipper_ID() 
{
Integer ii = (Integer)get_Value("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int M_WAREHOUSE_ID_AD_Reference_ID=197;
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_ValueNoCheck ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public void setPOReference (String POReference)
{
if (POReference != null && POReference.length() > 20)
{
log.warn("setPOReference - length > 20 - truncated");
POReference = POReference.substring(0,19);
}
set_Value ("POReference", POReference);
}
/** Get Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public String getPOReference() 
{
return (String)get_Value("POReference");
}
/** Set Payment BPartner.
Business Partner responsible for the payment */
public void setPay_BPartner_ID (int Pay_BPartner_ID)
{
if (Pay_BPartner_ID == 0) set_Value ("Pay_BPartner_ID", null);
 else 
set_Value ("Pay_BPartner_ID", new Integer(Pay_BPartner_ID));
}
/** Get Payment BPartner.
Business Partner responsible for the payment */
public int getPay_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Pay_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Location.
Location of the Business Partner responsible for the payment */
public void setPay_Location_ID (int Pay_Location_ID)
{
if (Pay_Location_ID == 0) set_Value ("Pay_Location_ID", null);
 else 
set_Value ("Pay_Location_ID", new Integer(Pay_Location_ID));
}
/** Get Payment Location.
Location of the Business Partner responsible for the payment */
public int getPay_Location_ID() 
{
Integer ii = (Integer)get_Value("Pay_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int PAYMENTRULE_AD_Reference_ID=195;
/** Direct Debit = D */
public static final String PAYMENTRULE_DirectDebit = "D";
/** Cash = B */
public static final String PAYMENTRULE_Cash = "B";
/** Credit Card = K */
public static final String PAYMENTRULE_CreditCard = "K";
/** Direct Deposit = T */
public static final String PAYMENTRULE_DirectDeposit = "T";
/** Check = S */
public static final String PAYMENTRULE_Check = "S";
/** On Credit = P */
public static final String PAYMENTRULE_OnCredit = "P";
/** Set Payment Rule.
How you pay the invoice */
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("D") || PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - D - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
if (PaymentRule.length() > 1)
{
log.warn("setPaymentRule - length > 1 - truncated");
PaymentRule = PaymentRule.substring(0,0);
}
set_Value ("PaymentRule", PaymentRule);
}
/** Get Payment Rule.
How you pay the invoice */
public String getPaymentRule() 
{
return (String)get_Value("PaymentRule");
}
/** Set Posted.
Posting status */
public void setPosted (boolean Posted)
{
set_Value ("Posted", new Boolean(Posted));
}
/** Get Posted.
Posting status */
public boolean isPosted() 
{
Object oo = get_Value("Posted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
public static final int PRIORITYRULE_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITYRULE_High = "3";
/** Medium = 5 */
public static final String PRIORITYRULE_Medium = "5";
/** Low = 7 */
public static final String PRIORITYRULE_Low = "7";
/** Set Priority.
Priority of a document */
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
if (PriorityRule.length() > 1)
{
log.warn("setPriorityRule - length > 1 - truncated");
PriorityRule = PriorityRule.substring(0,0);
}
set_Value ("PriorityRule", PriorityRule);
}
/** Get Priority.
Priority of a document */
public String getPriorityRule() 
{
return (String)get_Value("PriorityRule");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_ValueNoCheck ("Processed", new Boolean(Processed));
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
/** Set Referenced Order.
Reference to corresponding Sales/Purchase Order */
public void setRef_Order_ID (int Ref_Order_ID)
{
if (Ref_Order_ID == 0) set_Value ("Ref_Order_ID", null);
 else 
set_Value ("Ref_Order_ID", new Integer(Ref_Order_ID));
}
/** Get Referenced Order.
Reference to corresponding Sales/Purchase Order */
public int getRef_Order_ID() 
{
Integer ii = (Integer)get_Value("Ref_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int SALESREP_ID_AD_Reference_ID=190;
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
/** Set Send EMail.
Enable sending Document EMail */
public void setSendEMail (boolean SendEMail)
{
set_Value ("SendEMail", new Boolean(SendEMail));
}
/** Get Send EMail.
Enable sending Document EMail */
public boolean isSendEMail() 
{
Object oo = get_Value("SendEMail");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Total Lines.
Total of all document lines */
public void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
set_ValueNoCheck ("TotalLines", TotalLines);
}
/** Get Total Lines.
Total of all document lines */
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)get_Value("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int USER1_ID_AD_Reference_ID=134;
/** Set User1.
User defined element #1 */
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) set_Value ("User1_ID", null);
 else 
set_Value ("User1_ID", new Integer(User1_ID));
}
/** Get User1.
User defined element #1 */
public int getUser1_ID() 
{
Integer ii = (Integer)get_Value("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USER2_ID_AD_Reference_ID=137;
/** Set User2.
User defined element #2 */
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) set_Value ("User2_ID", null);
 else 
set_Value ("User2_ID", new Integer(User2_ID));
}
/** Get User2.
User defined element #2 */
public int getUser2_ID() 
{
Integer ii = (Integer)get_Value("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_Order
 ** @version $Id: X_C_Order.java,v 1.27 2003/10/31 05:30:53 jjanke Exp $ **/
public class X_C_Order extends PO
{
/** Standard Constructor **/
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setBillTo_ID (0);
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));
setDateOrdered (new Timestamp(System.currentTimeMillis()));
setDatePromised (new Timestamp(System.currentTimeMillis()));
setDeliveryRule (null);
setDeliveryViaRule (null);
setDocAction (null);
setDocStatus (null);
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);
setGrandTotal (Env.ZERO);
setInvoiceRule (null);
setIsApproved (false);
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);
setPosted (false);
setPriorityRule (null);
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 **/
}
/** Load Constructor **/
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 259;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) setValue ("AD_OrgTrx_ID", null);
 else 
setValue ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)getValue("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public void setBillTo_ID (int BillTo_ID)
{
setValue ("BillTo_ID", new Integer(BillTo_ID));
}
public int getBillTo_ID() 
{
Integer ii = (Integer)getValue("BillTo_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) setValue ("C_Activity_ID", null);
 else 
setValue ("C_Activity_ID", new Integer(C_Activity_ID));
}
public int getC_Activity_ID() 
{
Integer ii = (Integer)getValue("C_Activity_ID");
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
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
setValue ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_Location_ID");
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
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) setValue ("C_CashLine_ID", null);
 else 
setValue ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
public int getC_CashLine_ID() 
{
Integer ii = (Integer)getValue("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) setValue ("C_Charge_ID", null);
 else 
setValue ("C_Charge_ID", new Integer(C_Charge_ID));
}
public int getC_Charge_ID() 
{
Integer ii = (Integer)getValue("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_Currency_ID (int C_Currency_ID)
{
setValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
public int getC_Currency_ID() 
{
Integer ii = (Integer)getValue("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
setValue ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)getValue("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_DocType_ID (int C_DocType_ID)
{
setValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
public int getC_DocType_ID() 
{
Integer ii = (Integer)getValue("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_Order_ID (int C_Order_ID)
{
setValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID() 
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
setValue ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)getValue("C_PaymentTerm_ID");
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
public void setChargeAmt (BigDecimal ChargeAmt)
{
setValue ("ChargeAmt", ChargeAmt);
}
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)getValue("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setCopyFrom (String CopyFrom)
{
setValue ("CopyFrom", CopyFrom);
}
public String getCopyFrom() 
{
return (String)getValue("CopyFrom");
}
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
setValue ("DateAcct", DateAcct);
}
public Timestamp getDateAcct() 
{
return (Timestamp)getValue("DateAcct");
}
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
setValue ("DateOrdered", DateOrdered);
}
public Timestamp getDateOrdered() 
{
return (Timestamp)getValue("DateOrdered");
}
public void setDatePrinted (Timestamp DatePrinted)
{
setValue ("DatePrinted", DatePrinted);
}
public Timestamp getDatePrinted() 
{
return (Timestamp)getValue("DatePrinted");
}
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
setValue ("DatePromised", DatePromised);
}
public Timestamp getDatePromised() 
{
return (Timestamp)getValue("DatePromised");
}
public static final String DELIVERYRULE_AfterReceipt = "R";
public static final String DELIVERYRULE_Availability = "A";
public static final String DELIVERYRULE_CompleteLine = "L";
public static final String DELIVERYRULE_CompleteOrder = "O";
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
setValue ("DeliveryRule", DeliveryRule);
}
public String getDeliveryRule() 
{
return (String)getValue("DeliveryRule");
}
public static final String DELIVERYVIARULE_Pickup = "P";
public static final String DELIVERYVIARULE_Delivery = "D";
public static final String DELIVERYVIARULE_Shipper = "S";
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
setValue ("DeliveryViaRule", DeliveryViaRule);
}
public String getDeliveryViaRule() 
{
return (String)getValue("DeliveryViaRule");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public static final String DOCACTION_Process = "PR";
public static final String DOCACTION_Unlock = "XL";
public static final String DOCACTION_Complete = "CO";
public static final String DOCACTION_Approve = "AP";
public static final String DOCACTION_Reject = "RJ";
public static final String DOCACTION_Post = "PO";
public static final String DOCACTION_Void = "VO";
public static final String DOCACTION_Close = "CL";
public static final String DOCACTION_ReverseMinusCorrection = "RC";
public static final String DOCACTION_ReverseMinusAccrual = "RA";
public static final String DOCACTION_Transfer = "TR";
public static final String DOCACTION_ReMinusActivate = "RE";
public static final String DOCACTION_LeNoneGt = "--";
public void setDocAction (String DocAction)
{
if (DocAction.equals("PR") || DocAction.equals("XL") || DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("TR") || DocAction.equals("RE") || DocAction.equals("--"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - PR - XL - CO - AP - RJ - PO - VO - CL - RC - RA - TR - RE - --");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
setValue ("DocAction", DocAction);
}
public String getDocAction() 
{
return (String)getValue("DocAction");
}
public static final String DOCSTATUS_InProgress = "IP";
public static final String DOCSTATUS_WaitingPayment = "WP";
public static final String DOCSTATUS_Drafted = "DR";
public static final String DOCSTATUS_Completed = "CO";
public static final String DOCSTATUS_Approved = "AP";
public static final String DOCSTATUS_Changed = "CH";
public static final String DOCSTATUS_NotApproved = "NA";
public static final String DOCSTATUS_TransferError = "TE";
public static final String DOCSTATUS_Printed = "PR";
public static final String DOCSTATUS_Transferred = "TR";
public static final String DOCSTATUS_Voided = "VO";
public static final String DOCSTATUS_Inactive = "IN";
public static final String DOCSTATUS_PostingError = "PE";
public static final String DOCSTATUS_Posted = "PO";
public static final String DOCSTATUS_Reversed = "RE";
public static final String DOCSTATUS_Closed = "CL";
public static final String DOCSTATUS_Unknown = "??";
public static final String DOCSTATUS_BeingProcessed = "XX";
void setDocStatus (String DocStatus)
{
if (DocStatus.equals("IP") || DocStatus.equals("WP") || DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("CH") || DocStatus.equals("NA") || DocStatus.equals("TE") || DocStatus.equals("PR") || DocStatus.equals("TR") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("PE") || DocStatus.equals("PO") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("XX"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - IP - WP - DR - CO - AP - CH - NA - TE - PR - TR - VO - IN - PE - PO - RE - CL - ?? - XX");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
setValueNoCheck ("DocStatus", DocStatus);
}
public String getDocStatus() 
{
return (String)getValue("DocStatus");
}
void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValueNoCheck ("DocumentNo", DocumentNo);
}
public String getDocumentNo() 
{
return (String)getValue("DocumentNo");
}
public void setDropShip_BPartner_ID (int DropShip_BPartner_ID)
{
if (DropShip_BPartner_ID == 0) setValue ("DropShip_BPartner_ID", null);
 else 
setValue ("DropShip_BPartner_ID", new Integer(DropShip_BPartner_ID));
}
public int getDropShip_BPartner_ID() 
{
Integer ii = (Integer)getValue("DropShip_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setDropShip_Location_ID (int DropShip_Location_ID)
{
if (DropShip_Location_ID == 0) setValue ("DropShip_Location_ID", null);
 else 
setValue ("DropShip_Location_ID", new Integer(DropShip_Location_ID));
}
public int getDropShip_Location_ID() 
{
Integer ii = (Integer)getValue("DropShip_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setDropShip_User_ID (int DropShip_User_ID)
{
if (DropShip_User_ID == 0) setValue ("DropShip_User_ID", null);
 else 
setValue ("DropShip_User_ID", new Integer(DropShip_User_ID));
}
public int getDropShip_User_ID() 
{
Integer ii = (Integer)getValue("DropShip_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
setValue ("FreightAmt", FreightAmt);
}
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)getValue("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
public static final String FREIGHTCOSTRULE_FixPrice = "F";
public static final String FREIGHTCOSTRULE_Calculated = "C";
public static final String FREIGHTCOSTRULE_Line = "L";
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
setValue ("FreightCostRule", FreightCostRule);
}
public String getFreightCostRule() 
{
return (String)getValue("FreightCostRule");
}
void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
setValueNoCheck ("GrandTotal", GrandTotal);
}
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)getValue("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final String INVOICERULE_AfterOrderDelivered = "O";
public static final String INVOICERULE_AfterDelivery = "D";
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
public static final String INVOICERULE_Immediate = "I";
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
setValue ("InvoiceRule", InvoiceRule);
}
public String getInvoiceRule() 
{
return (String)getValue("InvoiceRule");
}
void setIsApproved (boolean IsApproved)
{
setValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
public boolean isApproved() 
{
Boolean bb = (Boolean)getValue("IsApproved");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsCreditApproved (boolean IsCreditApproved)
{
setValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
public boolean isCreditApproved() 
{
Boolean bb = (Boolean)getValue("IsCreditApproved");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsDelivered (boolean IsDelivered)
{
setValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
public boolean isDelivered() 
{
Boolean bb = (Boolean)getValue("IsDelivered");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
setValue ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
public boolean isDiscountPrinted() 
{
Boolean bb = (Boolean)getValue("IsDiscountPrinted");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsInvoiced (boolean IsInvoiced)
{
setValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
public boolean isInvoiced() 
{
Boolean bb = (Boolean)getValue("IsInvoiced");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsPrinted (boolean IsPrinted)
{
setValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
public boolean isPrinted() 
{
Boolean bb = (Boolean)getValue("IsPrinted");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSOTrx (boolean IsSOTrx)
{
setValue ("IsSOTrx", new Boolean(IsSOTrx));
}
public boolean isSOTrx() 
{
Boolean bb = (Boolean)getValue("IsSOTrx");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSelected (boolean IsSelected)
{
setValue ("IsSelected", new Boolean(IsSelected));
}
public boolean isSelected() 
{
Boolean bb = (Boolean)getValue("IsSelected");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSelfService (boolean IsSelfService)
{
setValue ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService() 
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
setValue ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
public boolean isTaxIncluded() 
{
Boolean bb = (Boolean)getValue("IsTaxIncluded");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsTransferred (boolean IsTransferred)
{
setValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
public boolean isTransferred() 
{
Boolean bb = (Boolean)getValue("IsTransferred");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_PriceList_ID (int M_PriceList_ID)
{
setValue ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
public int getM_PriceList_ID() 
{
Integer ii = (Integer)getValue("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) setValue ("M_Shipper_ID", null);
 else 
setValue ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
public int getM_Shipper_ID() 
{
Integer ii = (Integer)getValue("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
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
public void setPOReference (String POReference)
{
setValue ("POReference", POReference);
}
public String getPOReference() 
{
return (String)getValue("POReference");
}
public static final String PAYMENTRULE_Cash = "B";
public static final String PAYMENTRULE_CreditCard = "K";
public static final String PAYMENTRULE_TransferACH = "T";
public static final String PAYMENTRULE_Check = "S";
public static final String PAYMENTRULE_OnCredit = "P";
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
setValue ("PaymentRule", PaymentRule);
}
public String getPaymentRule() 
{
return (String)getValue("PaymentRule");
}
public void setPosted (boolean Posted)
{
setValue ("Posted", new Boolean(Posted));
}
public boolean isPosted() 
{
Boolean bb = (Boolean)getValue("Posted");
if (bb != null) return bb.booleanValue();
return false;
}
public static final String PRIORITYRULE_High = "3";
public static final String PRIORITYRULE_Medium = "5";
public static final String PRIORITYRULE_Low = "7";
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
setValue ("PriorityRule", PriorityRule);
}
public String getPriorityRule() 
{
return (String)getValue("PriorityRule");
}
void setProcessed (boolean Processed)
{
setValueNoCheck ("Processed", new Boolean(Processed));
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
public void setSendEMail (boolean SendEMail)
{
setValue ("SendEMail", new Boolean(SendEMail));
}
public boolean isSendEMail() 
{
Boolean bb = (Boolean)getValue("SendEMail");
if (bb != null) return bb.booleanValue();
return false;
}
void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
setValueNoCheck ("TotalLines", TotalLines);
}
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)getValue("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) setValue ("User1_ID", null);
 else 
setValue ("User1_ID", new Integer(User1_ID));
}
public int getUser1_ID() 
{
Integer ii = (Integer)getValue("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) setValue ("User2_ID", null);
 else 
setValue ("User2_ID", new Integer(User2_ID));
}
public int getUser2_ID() 
{
Integer ii = (Integer)getValue("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_Order
 ** @version $Id: X_C_Order.java,v 1.73 2004/05/20 05:59:13 jjanke Exp $ */
public class X_C_Order extends PO
{
/** Standard Constructor */
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);	// @C_Currency_ID@
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);	// 0
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDateOrdered (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDatePromised (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDeliveryRule (null);	// F
setDeliveryViaRule (null);	// P
setDocAction (null);	// CO
setDocStatus (null);	// DR
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);	// I
setGrandTotal (Env.ZERO);
setInvoiceRule (null);	// I
setIsApproved (false);	// @IsApproved@
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsDropShip (false);	// N
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);	// @IsSOTrx@
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);	// B
setPosted (false);	// N
setPriorityRule (null);	// 5
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 */
}
/** Load Constructor */
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=259;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
/** Set Trx Organization.
Performing or initiating organization */
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) set_Value ("AD_OrgTrx_ID", null);
 else 
set_Value ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
/** Get Trx Organization.
Performing or initiating organization */
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)get_Value("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public static final int BILL_BPARTNER_ID_AD_Reference_ID=138;
/** Set Invoice Partner.
Business Partner to be invoiced */
public void setBill_BPartner_ID (int Bill_BPartner_ID)
{
if (Bill_BPartner_ID == 0) set_Value ("Bill_BPartner_ID", null);
 else 
set_Value ("Bill_BPartner_ID", new Integer(Bill_BPartner_ID));
}
/** Get Invoice Partner.
Business Partner to be invoiced */
public int getBill_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Bill_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_LOCATION_ID_AD_Reference_ID=159;
/** Set Invoice Location.
Business Partner Location for invoicing */
public void setBill_Location_ID (int Bill_Location_ID)
{
if (Bill_Location_ID == 0) set_Value ("Bill_Location_ID", null);
 else 
set_Value ("Bill_Location_ID", new Integer(Bill_Location_ID));
}
/** Get Invoice Location.
Business Partner Location for invoicing */
public int getBill_Location_ID() 
{
Integer ii = (Integer)get_Value("Bill_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_USER_ID_AD_Reference_ID=110;
/** Set Invoice Contact.
Business Partner Contact for invoicing */
public void setBill_User_ID (int Bill_User_ID)
{
if (Bill_User_ID == 0) set_Value ("Bill_User_ID", null);
 else 
set_Value ("Bill_User_ID", new Integer(Bill_User_ID));
}
/** Get Invoice Contact.
Business Partner Contact for invoicing */
public int getBill_User_ID() 
{
Integer ii = (Integer)get_Value("Bill_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Activity.
Business Activity */
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) set_Value ("C_Activity_ID", null);
 else 
set_Value ("C_Activity_ID", new Integer(C_Activity_ID));
}
/** Get Activity.
Business Activity */
public int getC_Activity_ID() 
{
Integer ii = (Integer)get_Value("C_Activity_ID");
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
/** Set Partner Location.
Identifies the (ship to) address for this Business Partner */
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
set_Value ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
/** Get Partner Location.
Identifies the (ship to) address for this Business Partner */
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_Location_ID");
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
/** Set Cash Journal Line.
Cash Journal Line */
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) set_Value ("C_CashLine_ID", null);
 else 
set_Value ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
/** Get Cash Journal Line.
Cash Journal Line */
public int getC_CashLine_ID() 
{
Integer ii = (Integer)get_Value("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_CHARGE_ID_AD_Reference_ID=200;
/** Set Charge.
Additional document charges */
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) set_Value ("C_Charge_ID", null);
 else 
set_Value ("C_Charge_ID", new Integer(C_Charge_ID));
}
/** Get Charge.
Additional document charges */
public int getC_Charge_ID() 
{
Integer ii = (Integer)get_Value("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency Type.
Currency Conversion Rate Type */
public void setC_ConversionType_ID (int C_ConversionType_ID)
{
if (C_ConversionType_ID == 0) set_Value ("C_ConversionType_ID", null);
 else 
set_Value ("C_ConversionType_ID", new Integer(C_ConversionType_ID));
}
/** Get Currency Type.
Currency Conversion Rate Type */
public int getC_ConversionType_ID() 
{
Integer ii = (Integer)get_Value("C_ConversionType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
set_ValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPETARGET_ID_AD_Reference_ID=170;
/** Set Target Document Type.
Target document type for conversing documents */
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
set_Value ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
/** Get Target Document Type.
Target document type for conversing documents */
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)get_Value("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPE_ID_AD_Reference_ID=170;
/** Set Document Type.
Document type or rules */
public void setC_DocType_ID (int C_DocType_ID)
{
set_ValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
/** Get Document Type.
Document type or rules */
public int getC_DocType_ID() 
{
Integer ii = (Integer)get_Value("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
set_ValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Term.
The terms for Payment of this transaction */
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
set_Value ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
/** Get Payment Term.
The terms for Payment of this transaction */
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)get_Value("C_PaymentTerm_ID");
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
/** Set Charge amount.
Charge Amount */
public void setChargeAmt (BigDecimal ChargeAmt)
{
set_Value ("ChargeAmt", ChargeAmt);
}
/** Get Charge amount.
Charge Amount */
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Copy From.
Copy From Record */
public void setCopyFrom (String CopyFrom)
{
if (CopyFrom != null && CopyFrom.length() > 1)
{
log.warn("setCopyFrom - length > 1 - truncated");
CopyFrom = CopyFrom.substring(0,0);
}
set_Value ("CopyFrom", CopyFrom);
}
/** Get Copy From.
Copy From Record */
public String getCopyFrom() 
{
return (String)get_Value("CopyFrom");
}
/** Set Account Date.
Accounting Date */
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
set_Value ("DateAcct", DateAcct);
}
/** Get Account Date.
Accounting Date */
public Timestamp getDateAcct() 
{
return (Timestamp)get_Value("DateAcct");
}
/** Set Date Ordered.
Date of Order */
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
set_Value ("DateOrdered", DateOrdered);
}
/** Get Date Ordered.
Date of Order */
public Timestamp getDateOrdered() 
{
return (Timestamp)get_Value("DateOrdered");
}
/** Set Date printed.
Date the document was printed. */
public void setDatePrinted (Timestamp DatePrinted)
{
set_Value ("DatePrinted", DatePrinted);
}
/** Get Date printed.
Date the document was printed. */
public Timestamp getDatePrinted() 
{
return (Timestamp)get_Value("DatePrinted");
}
/** Set Date Promised.
Date Order was promised */
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
set_Value ("DatePromised", DatePromised);
}
/** Get Date Promised.
Date Order was promised */
public Timestamp getDatePromised() 
{
return (Timestamp)get_Value("DatePromised");
}
public static final int DELIVERYRULE_AD_Reference_ID=151;
/** After Receipt = R */
public static final String DELIVERYRULE_AfterReceipt = "R";
/** Availability = A */
public static final String DELIVERYRULE_Availability = "A";
/** Complete Line = L */
public static final String DELIVERYRULE_CompleteLine = "L";
/** Complete Order = O */
public static final String DELIVERYRULE_CompleteOrder = "O";
/** Force = F */
public static final String DELIVERYRULE_Force = "F";
/** Set Delivery Rule.
Defines the timing of Delivery */
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O") || DeliveryRule.equals("F"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O - F");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
if (DeliveryRule.length() > 1)
{
log.warn("setDeliveryRule - length > 1 - truncated");
DeliveryRule = DeliveryRule.substring(0,0);
}
set_Value ("DeliveryRule", DeliveryRule);
}
/** Get Delivery Rule.
Defines the timing of Delivery */
public String getDeliveryRule() 
{
return (String)get_Value("DeliveryRule");
}
public static final int DELIVERYVIARULE_AD_Reference_ID=152;
/** Pickup = P */
public static final String DELIVERYVIARULE_Pickup = "P";
/** Delivery = D */
public static final String DELIVERYVIARULE_Delivery = "D";
/** Shipper = S */
public static final String DELIVERYVIARULE_Shipper = "S";
/** Set Delivery Via.
How the order will be delivered */
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
if (DeliveryViaRule.length() > 1)
{
log.warn("setDeliveryViaRule - length > 1 - truncated");
DeliveryViaRule = DeliveryViaRule.substring(0,0);
}
set_Value ("DeliveryViaRule", DeliveryViaRule);
}
/** Get Delivery Via.
How the order will be delivered */
public String getDeliveryViaRule() 
{
return (String)get_Value("DeliveryViaRule");
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
public static final int DOCACTION_AD_Reference_ID=135;
/** Complete = CO */
public static final String DOCACTION_Complete = "CO";
/** Approve = AP */
public static final String DOCACTION_Approve = "AP";
/** Reject = RJ */
public static final String DOCACTION_Reject = "RJ";
/** Post = PO */
public static final String DOCACTION_Post = "PO";
/** Void = VO */
public static final String DOCACTION_Void = "VO";
/** Close = CL */
public static final String DOCACTION_Close = "CL";
/** Reverse - Correct = RC */
public static final String DOCACTION_Reverse_Correct = "RC";
/** Reverse - Accrual = RA */
public static final String DOCACTION_Reverse_Accrual = "RA";
/** Invalidate = IN */
public static final String DOCACTION_Invalidate = "IN";
/** Re-activate = RE */
public static final String DOCACTION_Re_Activate = "RE";
/** <None> = -- */
public static final String DOCACTION_None = "--";
/** Prepare = PR */
public static final String DOCACTION_Prepare = "PR";
/** Unlock = XL */
public static final String DOCACTION_Unlock = "XL";
/** Set Document Action.
The targeted status of the document */
public void setDocAction (String DocAction)
{
if (DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("IN") || DocAction.equals("RE") || DocAction.equals("--") || DocAction.equals("PR") || DocAction.equals("XL"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - CO - AP - RJ - PO - VO - CL - RC - RA - IN - RE - -- - PR - XL");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
if (DocAction.length() > 2)
{
log.warn("setDocAction - length > 2 - truncated");
DocAction = DocAction.substring(0,1);
}
set_Value ("DocAction", DocAction);
}
/** Get Document Action.
The targeted status of the document */
public String getDocAction() 
{
return (String)get_Value("DocAction");
}
public static final int DOCSTATUS_AD_Reference_ID=131;
/** Drafted = DR */
public static final String DOCSTATUS_Drafted = "DR";
/** Completed = CO */
public static final String DOCSTATUS_Completed = "CO";
/** Approved = AP */
public static final String DOCSTATUS_Approved = "AP";
/** Not Approved = NA */
public static final String DOCSTATUS_NotApproved = "NA";
/** Voided = VO */
public static final String DOCSTATUS_Voided = "VO";
/** Invalid = IN */
public static final String DOCSTATUS_Invalid = "IN";
/** Reversed = RE */
public static final String DOCSTATUS_Reversed = "RE";
/** Closed = CL */
public static final String DOCSTATUS_Closed = "CL";
/** Unknown = ?? */
public static final String DOCSTATUS_Unknown = "??";
/** Waiting Confirmation = WC */
public static final String DOCSTATUS_WaitingConfirmation = "WC";
/** In Progress = IP */
public static final String DOCSTATUS_InProgress = "IP";
/** Waiting Payment = WP */
public static final String DOCSTATUS_WaitingPayment = "WP";
/** Set Document Status.
The current status of the document */
public void setDocStatus (String DocStatus)
{
if (DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("NA") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("WC") || DocStatus.equals("IP") || DocStatus.equals("WP"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - DR - CO - AP - NA - VO - IN - RE - CL - ?? - WC - IP - WP");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
if (DocStatus.length() > 2)
{
log.warn("setDocStatus - length > 2 - truncated");
DocStatus = DocStatus.substring(0,1);
}
set_Value ("DocStatus", DocStatus);
}
/** Get Document Status.
The current status of the document */
public String getDocStatus() 
{
return (String)get_Value("DocStatus");
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
set_ValueNoCheck ("DocumentNo", DocumentNo);
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
/** Set Freight Amount.
Freight Amount  */
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
set_Value ("FreightAmt", FreightAmt);
}
/** Get Freight Amount.
Freight Amount  */
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int FREIGHTCOSTRULE_AD_Reference_ID=153;
/** Freight included = I */
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
/** Fix price = F */
public static final String FREIGHTCOSTRULE_FixPrice = "F";
/** Calculated = C */
public static final String FREIGHTCOSTRULE_Calculated = "C";
/** Line = L */
public static final String FREIGHTCOSTRULE_Line = "L";
/** Set Freight Cost Rule.
Method for charging Freight */
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
if (FreightCostRule.length() > 1)
{
log.warn("setFreightCostRule - length > 1 - truncated");
FreightCostRule = FreightCostRule.substring(0,0);
}
set_Value ("FreightCostRule", FreightCostRule);
}
/** Get Freight Cost Rule.
Method for charging Freight */
public String getFreightCostRule() 
{
return (String)get_Value("FreightCostRule");
}
/** Set Grand Total.
Total amount of document */
public void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
set_ValueNoCheck ("GrandTotal", GrandTotal);
}
/** Get Grand Total.
Total amount of document */
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)get_Value("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int INVOICERULE_AD_Reference_ID=150;
/** After Order delivered = O */
public static final String INVOICERULE_AfterOrderDelivered = "O";
/** After Delivery = D */
public static final String INVOICERULE_AfterDelivery = "D";
/** Customer Schedule after Delivery = S */
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
/** Immediate = I */
public static final String INVOICERULE_Immediate = "I";
/** Set Invoice Rule.
Frequency and method of invoicing  */
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
if (InvoiceRule.length() > 1)
{
log.warn("setInvoiceRule - length > 1 - truncated");
InvoiceRule = InvoiceRule.substring(0,0);
}
set_Value ("InvoiceRule", InvoiceRule);
}
/** Get Invoice Rule.
Frequency and method of invoicing  */
public String getInvoiceRule() 
{
return (String)get_Value("InvoiceRule");
}
/** Set Approved.
Indicates if this document requires approval */
public void setIsApproved (boolean IsApproved)
{
set_ValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
/** Get Approved.
Indicates if this document requires approval */
public boolean isApproved() 
{
Object oo = get_Value("IsApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Credit Approved.
Credit  has been approved */
public void setIsCreditApproved (boolean IsCreditApproved)
{
set_ValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
/** Get Credit Approved.
Credit  has been approved */
public boolean isCreditApproved() 
{
Object oo = get_Value("IsCreditApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Delivered */
public void setIsDelivered (boolean IsDelivered)
{
set_ValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
/** Get Delivered */
public boolean isDelivered() 
{
Object oo = get_Value("IsDelivered");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Discount Printed.
Print Discount on Invoice and Order */
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
set_Value ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
/** Get Discount Printed.
Print Discount on Invoice and Order */
public boolean isDiscountPrinted() 
{
Object oo = get_Value("IsDiscountPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public void setIsDropShip (boolean IsDropShip)
{
set_ValueNoCheck ("IsDropShip", new Boolean(IsDropShip));
}
/** Get Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public boolean isDropShip() 
{
Object oo = get_Value("IsDropShip");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Invoiced.
It is invoiced */
public void setIsInvoiced (boolean IsInvoiced)
{
set_ValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
/** Get Invoiced.
It is invoiced */
public boolean isInvoiced() 
{
Object oo = get_Value("IsInvoiced");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Printed.
Indicates if this document / line is printed */
public void setIsPrinted (boolean IsPrinted)
{
set_ValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
/** Get Printed.
Indicates if this document / line is printed */
public boolean isPrinted() 
{
Object oo = get_Value("IsPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Sales Transaction.
This is a Sales Transaction */
public void setIsSOTrx (boolean IsSOTrx)
{
set_Value ("IsSOTrx", new Boolean(IsSOTrx));
}
/** Get Sales Transaction.
This is a Sales Transaction */
public boolean isSOTrx() 
{
Object oo = get_Value("IsSOTrx");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Selected */
public void setIsSelected (boolean IsSelected)
{
set_Value ("IsSelected", new Boolean(IsSelected));
}
/** Get Selected */
public boolean isSelected() 
{
Object oo = get_Value("IsSelected");
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
set_Value ("IsSelfService", new Boolean(IsSelfService));
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
/** Set Price includes Tax.
Tax is included in the price  */
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
set_Value ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
/** Get Price includes Tax.
Tax is included in the price  */
public boolean isTaxIncluded() 
{
Object oo = get_Value("IsTaxIncluded");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Transferred.
Transferred to General Ledger (i.e. accounted) */
public void setIsTransferred (boolean IsTransferred)
{
set_ValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
/** Get Transferred.
Transferred to General Ledger (i.e. accounted) */
public boolean isTransferred() 
{
Object oo = get_Value("IsTransferred");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Price List.
Unique identifier of a Price List */
public void setM_PriceList_ID (int M_PriceList_ID)
{
set_Value ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
/** Get Price List.
Unique identifier of a Price List */
public int getM_PriceList_ID() 
{
Integer ii = (Integer)get_Value("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Shipper.
Method or manner of product delivery */
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) set_Value ("M_Shipper_ID", null);
 else 
set_Value ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
/** Get Shipper.
Method or manner of product delivery */
public int getM_Shipper_ID() 
{
Integer ii = (Integer)get_Value("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int M_WAREHOUSE_ID_AD_Reference_ID=197;
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_ValueNoCheck ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public void setPOReference (String POReference)
{
if (POReference != null && POReference.length() > 20)
{
log.warn("setPOReference - length > 20 - truncated");
POReference = POReference.substring(0,19);
}
set_Value ("POReference", POReference);
}
/** Get Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public String getPOReference() 
{
return (String)get_Value("POReference");
}
/** Set Payment BPartner.
Business Partner responsible for the payment */
public void setPay_BPartner_ID (int Pay_BPartner_ID)
{
if (Pay_BPartner_ID == 0) set_Value ("Pay_BPartner_ID", null);
 else 
set_Value ("Pay_BPartner_ID", new Integer(Pay_BPartner_ID));
}
/** Get Payment BPartner.
Business Partner responsible for the payment */
public int getPay_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Pay_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Location.
Location of the Business Partner responsible for the payment */
public void setPay_Location_ID (int Pay_Location_ID)
{
if (Pay_Location_ID == 0) set_Value ("Pay_Location_ID", null);
 else 
set_Value ("Pay_Location_ID", new Integer(Pay_Location_ID));
}
/** Get Payment Location.
Location of the Business Partner responsible for the payment */
public int getPay_Location_ID() 
{
Integer ii = (Integer)get_Value("Pay_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int PAYMENTRULE_AD_Reference_ID=195;
/** Direct Debit = D */
public static final String PAYMENTRULE_DirectDebit = "D";
/** Cash = B */
public static final String PAYMENTRULE_Cash = "B";
/** Credit Card = K */
public static final String PAYMENTRULE_CreditCard = "K";
/** Direct Deposit = T */
public static final String PAYMENTRULE_DirectDeposit = "T";
/** Check = S */
public static final String PAYMENTRULE_Check = "S";
/** On Credit = P */
public static final String PAYMENTRULE_OnCredit = "P";
/** Set Payment Rule.
How you pay the invoice */
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("D") || PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - D - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
if (PaymentRule.length() > 1)
{
log.warn("setPaymentRule - length > 1 - truncated");
PaymentRule = PaymentRule.substring(0,0);
}
set_Value ("PaymentRule", PaymentRule);
}
/** Get Payment Rule.
How you pay the invoice */
public String getPaymentRule() 
{
return (String)get_Value("PaymentRule");
}
/** Set Posted.
Posting status */
public void setPosted (boolean Posted)
{
set_Value ("Posted", new Boolean(Posted));
}
/** Get Posted.
Posting status */
public boolean isPosted() 
{
Object oo = get_Value("Posted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
public static final int PRIORITYRULE_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITYRULE_High = "3";
/** Medium = 5 */
public static final String PRIORITYRULE_Medium = "5";
/** Low = 7 */
public static final String PRIORITYRULE_Low = "7";
/** Set Priority.
Priority of a document */
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
if (PriorityRule.length() > 1)
{
log.warn("setPriorityRule - length > 1 - truncated");
PriorityRule = PriorityRule.substring(0,0);
}
set_Value ("PriorityRule", PriorityRule);
}
/** Get Priority.
Priority of a document */
public String getPriorityRule() 
{
return (String)get_Value("PriorityRule");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_ValueNoCheck ("Processed", new Boolean(Processed));
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
/** Set Referenced Order.
Reference to corresponding Sales/Purchase Order */
public void setRef_Order_ID (int Ref_Order_ID)
{
if (Ref_Order_ID == 0) set_Value ("Ref_Order_ID", null);
 else 
set_Value ("Ref_Order_ID", new Integer(Ref_Order_ID));
}
/** Get Referenced Order.
Reference to corresponding Sales/Purchase Order */
public int getRef_Order_ID() 
{
Integer ii = (Integer)get_Value("Ref_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int SALESREP_ID_AD_Reference_ID=190;
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
/** Set Send EMail.
Enable sending Document EMail */
public void setSendEMail (boolean SendEMail)
{
set_Value ("SendEMail", new Boolean(SendEMail));
}
/** Get Send EMail.
Enable sending Document EMail */
public boolean isSendEMail() 
{
Object oo = get_Value("SendEMail");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Total Lines.
Total of all document lines */
public void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
set_ValueNoCheck ("TotalLines", TotalLines);
}
/** Get Total Lines.
Total of all document lines */
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)get_Value("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int USER1_ID_AD_Reference_ID=134;
/** Set User1.
User defined element #1 */
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) set_Value ("User1_ID", null);
 else 
set_Value ("User1_ID", new Integer(User1_ID));
}
/** Get User1.
User defined element #1 */
public int getUser1_ID() 
{
Integer ii = (Integer)get_Value("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USER2_ID_AD_Reference_ID=137;
/** Set User2.
User defined element #2 */
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) set_Value ("User2_ID", null);
 else 
set_Value ("User2_ID", new Integer(User2_ID));
}
/** Get User2.
User defined element #2 */
public int getUser2_ID() 
{
Integer ii = (Integer)get_Value("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for C_Order
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:09.822 */
public class X_C_Order extends PO
{
/** Standard Constructor */
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);	// @C_Currency_ID@
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);	// 0
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDateOrdered (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDatePromised (new Timestamp(System.currentTimeMillis()));	// @#Date@
setDeliveryRule (null);	// F
setDeliveryViaRule (null);	// P
setDocAction (null);	// CO
setDocStatus (null);	// DR
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);	// I
setGrandTotal (Env.ZERO);
setInvoiceRule (null);	// I
setIsApproved (false);	// @IsApproved@
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsDropShip (false);	// N
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);	// @IsSOTrx@
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);	// B
setPosted (false);	// N
setPriorityRule (null);	// 5
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 */
}
/** Load Constructor */
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=259 */
public static final int Table_ID=259;

/** TableName=C_Order */
public static final String Table_Name="C_Order";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public static final int AD_ORGTRX_ID_AD_Reference_ID=130;
/** Set Trx Organization.
Performing or initiating organization */
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) set_Value ("AD_OrgTrx_ID", null);
 else 
set_Value ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
/** Get Trx Organization.
Performing or initiating organization */
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)get_Value("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public static final int BILL_BPARTNER_ID_AD_Reference_ID=138;
/** Set Invoice Partner.
Business Partner to be invoiced */
public void setBill_BPartner_ID (int Bill_BPartner_ID)
{
if (Bill_BPartner_ID == 0) set_Value ("Bill_BPartner_ID", null);
 else 
set_Value ("Bill_BPartner_ID", new Integer(Bill_BPartner_ID));
}
/** Get Invoice Partner.
Business Partner to be invoiced */
public int getBill_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Bill_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_LOCATION_ID_AD_Reference_ID=159;
/** Set Invoice Location.
Business Partner Location for invoicing */
public void setBill_Location_ID (int Bill_Location_ID)
{
if (Bill_Location_ID == 0) set_Value ("Bill_Location_ID", null);
 else 
set_Value ("Bill_Location_ID", new Integer(Bill_Location_ID));
}
/** Get Invoice Location.
Business Partner Location for invoicing */
public int getBill_Location_ID() 
{
Integer ii = (Integer)get_Value("Bill_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int BILL_USER_ID_AD_Reference_ID=110;
/** Set Invoice Contact.
Business Partner Contact for invoicing */
public void setBill_User_ID (int Bill_User_ID)
{
if (Bill_User_ID == 0) set_Value ("Bill_User_ID", null);
 else 
set_Value ("Bill_User_ID", new Integer(Bill_User_ID));
}
/** Get Invoice Contact.
Business Partner Contact for invoicing */
public int getBill_User_ID() 
{
Integer ii = (Integer)get_Value("Bill_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Activity.
Business Activity */
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) set_Value ("C_Activity_ID", null);
 else 
set_Value ("C_Activity_ID", new Integer(C_Activity_ID));
}
/** Get Activity.
Business Activity */
public int getC_Activity_ID() 
{
Integer ii = (Integer)get_Value("C_Activity_ID");
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
/** Set Partner Location.
Identifies the (ship to) address for this Business Partner */
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
set_Value ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
/** Get Partner Location.
Identifies the (ship to) address for this Business Partner */
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)get_Value("C_BPartner_Location_ID");
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
/** Set Cash Journal Line.
Cash Journal Line */
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) set_Value ("C_CashLine_ID", null);
 else 
set_Value ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
/** Get Cash Journal Line.
Cash Journal Line */
public int getC_CashLine_ID() 
{
Integer ii = (Integer)get_Value("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_CHARGE_ID_AD_Reference_ID=200;
/** Set Charge.
Additional document charges */
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) set_Value ("C_Charge_ID", null);
 else 
set_Value ("C_Charge_ID", new Integer(C_Charge_ID));
}
/** Get Charge.
Additional document charges */
public int getC_Charge_ID() 
{
Integer ii = (Integer)get_Value("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency Type.
Currency Conversion Rate Type */
public void setC_ConversionType_ID (int C_ConversionType_ID)
{
if (C_ConversionType_ID == 0) set_Value ("C_ConversionType_ID", null);
 else 
set_Value ("C_ConversionType_ID", new Integer(C_ConversionType_ID));
}
/** Get Currency Type.
Currency Conversion Rate Type */
public int getC_ConversionType_ID() 
{
Integer ii = (Integer)get_Value("C_ConversionType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Currency.
The Currency for this record */
public void setC_Currency_ID (int C_Currency_ID)
{
set_ValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
/** Get Currency.
The Currency for this record */
public int getC_Currency_ID() 
{
Integer ii = (Integer)get_Value("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPETARGET_ID_AD_Reference_ID=170;
/** Set Target Document Type.
Target document type for conversing documents */
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
set_Value ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
/** Get Target Document Type.
Target document type for conversing documents */
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)get_Value("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int C_DOCTYPE_ID_AD_Reference_ID=170;
/** Set Document Type.
Document type or rules */
public void setC_DocType_ID (int C_DocType_ID)
{
set_ValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
/** Get Document Type.
Document type or rules */
public int getC_DocType_ID() 
{
Integer ii = (Integer)get_Value("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Sales Order.
Sales Order */
public void setC_Order_ID (int C_Order_ID)
{
set_ValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
/** Get Sales Order.
Sales Order */
public int getC_Order_ID() 
{
Integer ii = (Integer)get_Value("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Term.
The terms for Payment of this transaction */
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
set_Value ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
/** Get Payment Term.
The terms for Payment of this transaction */
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)get_Value("C_PaymentTerm_ID");
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
/** Set Charge amount.
Charge Amount */
public void setChargeAmt (BigDecimal ChargeAmt)
{
set_Value ("ChargeAmt", ChargeAmt);
}
/** Get Charge amount.
Charge Amount */
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
/** Set Copy From.
Copy From Record */
public void setCopyFrom (String CopyFrom)
{
if (CopyFrom != null && CopyFrom.length() > 1)
{
log.warn("setCopyFrom - length > 1 - truncated");
CopyFrom = CopyFrom.substring(0,0);
}
set_Value ("CopyFrom", CopyFrom);
}
/** Get Copy From.
Copy From Record */
public String getCopyFrom() 
{
return (String)get_Value("CopyFrom");
}
/** Set Account Date.
Accounting Date */
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
set_Value ("DateAcct", DateAcct);
}
/** Get Account Date.
Accounting Date */
public Timestamp getDateAcct() 
{
return (Timestamp)get_Value("DateAcct");
}
/** Set Date Ordered.
Date of Order */
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
set_Value ("DateOrdered", DateOrdered);
}
/** Get Date Ordered.
Date of Order */
public Timestamp getDateOrdered() 
{
return (Timestamp)get_Value("DateOrdered");
}
/** Set Date printed.
Date the document was printed. */
public void setDatePrinted (Timestamp DatePrinted)
{
set_Value ("DatePrinted", DatePrinted);
}
/** Get Date printed.
Date the document was printed. */
public Timestamp getDatePrinted() 
{
return (Timestamp)get_Value("DatePrinted");
}
/** Set Date Promised.
Date Order was promised */
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
set_Value ("DatePromised", DatePromised);
}
/** Get Date Promised.
Date Order was promised */
public Timestamp getDatePromised() 
{
return (Timestamp)get_Value("DatePromised");
}
public static final int DELIVERYRULE_AD_Reference_ID=151;
/** After Receipt = R */
public static final String DELIVERYRULE_AfterReceipt = "R";
/** Availability = A */
public static final String DELIVERYRULE_Availability = "A";
/** Complete Line = L */
public static final String DELIVERYRULE_CompleteLine = "L";
/** Complete Order = O */
public static final String DELIVERYRULE_CompleteOrder = "O";
/** Force = F */
public static final String DELIVERYRULE_Force = "F";
/** Set Delivery Rule.
Defines the timing of Delivery */
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O") || DeliveryRule.equals("F"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O - F");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
if (DeliveryRule.length() > 1)
{
log.warn("setDeliveryRule - length > 1 - truncated");
DeliveryRule = DeliveryRule.substring(0,0);
}
set_Value ("DeliveryRule", DeliveryRule);
}
/** Get Delivery Rule.
Defines the timing of Delivery */
public String getDeliveryRule() 
{
return (String)get_Value("DeliveryRule");
}
public static final int DELIVERYVIARULE_AD_Reference_ID=152;
/** Pickup = P */
public static final String DELIVERYVIARULE_Pickup = "P";
/** Delivery = D */
public static final String DELIVERYVIARULE_Delivery = "D";
/** Shipper = S */
public static final String DELIVERYVIARULE_Shipper = "S";
/** Set Delivery Via.
How the order will be delivered */
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
if (DeliveryViaRule.length() > 1)
{
log.warn("setDeliveryViaRule - length > 1 - truncated");
DeliveryViaRule = DeliveryViaRule.substring(0,0);
}
set_Value ("DeliveryViaRule", DeliveryViaRule);
}
/** Get Delivery Via.
How the order will be delivered */
public String getDeliveryViaRule() 
{
return (String)get_Value("DeliveryViaRule");
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
public static final int DOCACTION_AD_Reference_ID=135;
/** Complete = CO */
public static final String DOCACTION_Complete = "CO";
/** Approve = AP */
public static final String DOCACTION_Approve = "AP";
/** Reject = RJ */
public static final String DOCACTION_Reject = "RJ";
/** Post = PO */
public static final String DOCACTION_Post = "PO";
/** Void = VO */
public static final String DOCACTION_Void = "VO";
/** Close = CL */
public static final String DOCACTION_Close = "CL";
/** Reverse - Correct = RC */
public static final String DOCACTION_Reverse_Correct = "RC";
/** Reverse - Accrual = RA */
public static final String DOCACTION_Reverse_Accrual = "RA";
/** Invalidate = IN */
public static final String DOCACTION_Invalidate = "IN";
/** Re-activate = RE */
public static final String DOCACTION_Re_Activate = "RE";
/** <None> = -- */
public static final String DOCACTION_None = "--";
/** Wait Complete = WC */
public static final String DOCACTION_WaitComplete = "WC";
/** Prepare = PR */
public static final String DOCACTION_Prepare = "PR";
/** Unlock = XL */
public static final String DOCACTION_Unlock = "XL";
/** Set Document Action.
The targeted status of the document */
public void setDocAction (String DocAction)
{
if (DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("IN") || DocAction.equals("RE") || DocAction.equals("--") || DocAction.equals("WC") || DocAction.equals("PR") || DocAction.equals("XL"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - CO - AP - RJ - PO - VO - CL - RC - RA - IN - RE - -- - WC - PR - XL");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
if (DocAction.length() > 2)
{
log.warn("setDocAction - length > 2 - truncated");
DocAction = DocAction.substring(0,1);
}
set_Value ("DocAction", DocAction);
}
/** Get Document Action.
The targeted status of the document */
public String getDocAction() 
{
return (String)get_Value("DocAction");
}
public static final int DOCSTATUS_AD_Reference_ID=131;
/** Drafted = DR */
public static final String DOCSTATUS_Drafted = "DR";
/** Completed = CO */
public static final String DOCSTATUS_Completed = "CO";
/** Approved = AP */
public static final String DOCSTATUS_Approved = "AP";
/** Not Approved = NA */
public static final String DOCSTATUS_NotApproved = "NA";
/** Voided = VO */
public static final String DOCSTATUS_Voided = "VO";
/** Invalid = IN */
public static final String DOCSTATUS_Invalid = "IN";
/** Reversed = RE */
public static final String DOCSTATUS_Reversed = "RE";
/** Closed = CL */
public static final String DOCSTATUS_Closed = "CL";
/** Unknown = ?? */
public static final String DOCSTATUS_Unknown = "??";
/** Waiting Confirmation = WC */
public static final String DOCSTATUS_WaitingConfirmation = "WC";
/** In Progress = IP */
public static final String DOCSTATUS_InProgress = "IP";
/** Waiting Payment = WP */
public static final String DOCSTATUS_WaitingPayment = "WP";
/** Set Document Status.
The current status of the document */
public void setDocStatus (String DocStatus)
{
if (DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("NA") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("WC") || DocStatus.equals("IP") || DocStatus.equals("WP"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - DR - CO - AP - NA - VO - IN - RE - CL - ?? - WC - IP - WP");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
if (DocStatus.length() > 2)
{
log.warn("setDocStatus - length > 2 - truncated");
DocStatus = DocStatus.substring(0,1);
}
set_Value ("DocStatus", DocStatus);
}
/** Get Document Status.
The current status of the document */
public String getDocStatus() 
{
return (String)get_Value("DocStatus");
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
set_ValueNoCheck ("DocumentNo", DocumentNo);
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
/** Set Freight Amount.
Freight Amount  */
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
set_Value ("FreightAmt", FreightAmt);
}
/** Get Freight Amount.
Freight Amount  */
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)get_Value("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int FREIGHTCOSTRULE_AD_Reference_ID=153;
/** Freight included = I */
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
/** Fix price = F */
public static final String FREIGHTCOSTRULE_FixPrice = "F";
/** Calculated = C */
public static final String FREIGHTCOSTRULE_Calculated = "C";
/** Line = L */
public static final String FREIGHTCOSTRULE_Line = "L";
/** Set Freight Cost Rule.
Method for charging Freight */
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
if (FreightCostRule.length() > 1)
{
log.warn("setFreightCostRule - length > 1 - truncated");
FreightCostRule = FreightCostRule.substring(0,0);
}
set_Value ("FreightCostRule", FreightCostRule);
}
/** Get Freight Cost Rule.
Method for charging Freight */
public String getFreightCostRule() 
{
return (String)get_Value("FreightCostRule");
}
/** Set Grand Total.
Total amount of document */
public void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
set_ValueNoCheck ("GrandTotal", GrandTotal);
}
/** Get Grand Total.
Total amount of document */
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)get_Value("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int INVOICERULE_AD_Reference_ID=150;
/** After Order delivered = O */
public static final String INVOICERULE_AfterOrderDelivered = "O";
/** After Delivery = D */
public static final String INVOICERULE_AfterDelivery = "D";
/** Customer Schedule after Delivery = S */
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
/** Immediate = I */
public static final String INVOICERULE_Immediate = "I";
/** Set Invoice Rule.
Frequency and method of invoicing  */
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
if (InvoiceRule.length() > 1)
{
log.warn("setInvoiceRule - length > 1 - truncated");
InvoiceRule = InvoiceRule.substring(0,0);
}
set_Value ("InvoiceRule", InvoiceRule);
}
/** Get Invoice Rule.
Frequency and method of invoicing  */
public String getInvoiceRule() 
{
return (String)get_Value("InvoiceRule");
}
/** Set Approved.
Indicates if this document requires approval */
public void setIsApproved (boolean IsApproved)
{
set_ValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
/** Get Approved.
Indicates if this document requires approval */
public boolean isApproved() 
{
Object oo = get_Value("IsApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Credit Approved.
Credit  has been approved */
public void setIsCreditApproved (boolean IsCreditApproved)
{
set_ValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
/** Get Credit Approved.
Credit  has been approved */
public boolean isCreditApproved() 
{
Object oo = get_Value("IsCreditApproved");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Delivered */
public void setIsDelivered (boolean IsDelivered)
{
set_ValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
/** Get Delivered */
public boolean isDelivered() 
{
Object oo = get_Value("IsDelivered");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Discount Printed.
Print Discount on Invoice and Order */
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
set_Value ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
/** Get Discount Printed.
Print Discount on Invoice and Order */
public boolean isDiscountPrinted() 
{
Object oo = get_Value("IsDiscountPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public void setIsDropShip (boolean IsDropShip)
{
set_ValueNoCheck ("IsDropShip", new Boolean(IsDropShip));
}
/** Get Drop Shipment.
Drop Shipments are sent from the Vendor directly to the Customer */
public boolean isDropShip() 
{
Object oo = get_Value("IsDropShip");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Invoiced.
It is invoiced */
public void setIsInvoiced (boolean IsInvoiced)
{
set_ValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
/** Get Invoiced.
It is invoiced */
public boolean isInvoiced() 
{
Object oo = get_Value("IsInvoiced");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Printed.
Indicates if this document / line is printed */
public void setIsPrinted (boolean IsPrinted)
{
set_ValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
/** Get Printed.
Indicates if this document / line is printed */
public boolean isPrinted() 
{
Object oo = get_Value("IsPrinted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Sales Transaction.
This is a Sales Transaction */
public void setIsSOTrx (boolean IsSOTrx)
{
set_Value ("IsSOTrx", new Boolean(IsSOTrx));
}
/** Get Sales Transaction.
This is a Sales Transaction */
public boolean isSOTrx() 
{
Object oo = get_Value("IsSOTrx");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Selected */
public void setIsSelected (boolean IsSelected)
{
set_Value ("IsSelected", new Boolean(IsSelected));
}
/** Get Selected */
public boolean isSelected() 
{
Object oo = get_Value("IsSelected");
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
set_Value ("IsSelfService", new Boolean(IsSelfService));
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
/** Set Price includes Tax.
Tax is included in the price  */
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
set_Value ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
/** Get Price includes Tax.
Tax is included in the price  */
public boolean isTaxIncluded() 
{
Object oo = get_Value("IsTaxIncluded");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Transferred.
Transferred to General Ledger (i.e. accounted) */
public void setIsTransferred (boolean IsTransferred)
{
set_ValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
/** Get Transferred.
Transferred to General Ledger (i.e. accounted) */
public boolean isTransferred() 
{
Object oo = get_Value("IsTransferred");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Price List.
Unique identifier of a Price List */
public void setM_PriceList_ID (int M_PriceList_ID)
{
set_Value ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
/** Get Price List.
Unique identifier of a Price List */
public int getM_PriceList_ID() 
{
Integer ii = (Integer)get_Value("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Shipper.
Method or manner of product delivery */
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) set_Value ("M_Shipper_ID", null);
 else 
set_Value ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
/** Get Shipper.
Method or manner of product delivery */
public int getM_Shipper_ID() 
{
Integer ii = (Integer)get_Value("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int M_WAREHOUSE_ID_AD_Reference_ID=197;
/** Set Warehouse.
Storage Warehouse and Service Point */
public void setM_Warehouse_ID (int M_Warehouse_ID)
{
set_ValueNoCheck ("M_Warehouse_ID", new Integer(M_Warehouse_ID));
}
/** Get Warehouse.
Storage Warehouse and Service Point */
public int getM_Warehouse_ID() 
{
Integer ii = (Integer)get_Value("M_Warehouse_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public void setPOReference (String POReference)
{
if (POReference != null && POReference.length() > 20)
{
log.warn("setPOReference - length > 20 - truncated");
POReference = POReference.substring(0,19);
}
set_Value ("POReference", POReference);
}
/** Get Order Reference.
Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner */
public String getPOReference() 
{
return (String)get_Value("POReference");
}
/** Set Payment BPartner.
Business Partner responsible for the payment */
public void setPay_BPartner_ID (int Pay_BPartner_ID)
{
if (Pay_BPartner_ID == 0) set_Value ("Pay_BPartner_ID", null);
 else 
set_Value ("Pay_BPartner_ID", new Integer(Pay_BPartner_ID));
}
/** Get Payment BPartner.
Business Partner responsible for the payment */
public int getPay_BPartner_ID() 
{
Integer ii = (Integer)get_Value("Pay_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Payment Location.
Location of the Business Partner responsible for the payment */
public void setPay_Location_ID (int Pay_Location_ID)
{
if (Pay_Location_ID == 0) set_Value ("Pay_Location_ID", null);
 else 
set_Value ("Pay_Location_ID", new Integer(Pay_Location_ID));
}
/** Get Payment Location.
Location of the Business Partner responsible for the payment */
public int getPay_Location_ID() 
{
Integer ii = (Integer)get_Value("Pay_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int PAYMENTRULE_AD_Reference_ID=195;
/** Direct Debit = D */
public static final String PAYMENTRULE_DirectDebit = "D";
/** Cash = B */
public static final String PAYMENTRULE_Cash = "B";
/** Credit Card = K */
public static final String PAYMENTRULE_CreditCard = "K";
/** Direct Deposit = T */
public static final String PAYMENTRULE_DirectDeposit = "T";
/** Check = S */
public static final String PAYMENTRULE_Check = "S";
/** On Credit = P */
public static final String PAYMENTRULE_OnCredit = "P";
/** Set Payment Rule.
How you pay the invoice */
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("D") || PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - D - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
if (PaymentRule.length() > 1)
{
log.warn("setPaymentRule - length > 1 - truncated");
PaymentRule = PaymentRule.substring(0,0);
}
set_Value ("PaymentRule", PaymentRule);
}
/** Get Payment Rule.
How you pay the invoice */
public String getPaymentRule() 
{
return (String)get_Value("PaymentRule");
}
/** Set Posted.
Posting status */
public void setPosted (boolean Posted)
{
set_Value ("Posted", new Boolean(Posted));
}
/** Get Posted.
Posting status */
public boolean isPosted() 
{
Object oo = get_Value("Posted");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
public static final int PRIORITYRULE_AD_Reference_ID=154;
/** High = 3 */
public static final String PRIORITYRULE_High = "3";
/** Medium = 5 */
public static final String PRIORITYRULE_Medium = "5";
/** Low = 7 */
public static final String PRIORITYRULE_Low = "7";
/** Set Priority.
Priority of a document */
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
if (PriorityRule.length() > 1)
{
log.warn("setPriorityRule - length > 1 - truncated");
PriorityRule = PriorityRule.substring(0,0);
}
set_Value ("PriorityRule", PriorityRule);
}
/** Get Priority.
Priority of a document */
public String getPriorityRule() 
{
return (String)get_Value("PriorityRule");
}
/** Set Processed.
The document has been processed */
public void setProcessed (boolean Processed)
{
set_ValueNoCheck ("Processed", new Boolean(Processed));
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
/** Set Referenced Order.
Reference to corresponding Sales/Purchase Order */
public void setRef_Order_ID (int Ref_Order_ID)
{
if (Ref_Order_ID == 0) set_Value ("Ref_Order_ID", null);
 else 
set_Value ("Ref_Order_ID", new Integer(Ref_Order_ID));
}
/** Get Referenced Order.
Reference to corresponding Sales/Purchase Order */
public int getRef_Order_ID() 
{
Integer ii = (Integer)get_Value("Ref_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int SALESREP_ID_AD_Reference_ID=190;
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
/** Set Send EMail.
Enable sending Document EMail */
public void setSendEMail (boolean SendEMail)
{
set_Value ("SendEMail", new Boolean(SendEMail));
}
/** Get Send EMail.
Enable sending Document EMail */
public boolean isSendEMail() 
{
Object oo = get_Value("SendEMail");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Total Lines.
Total of all document lines */
public void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
set_ValueNoCheck ("TotalLines", TotalLines);
}
/** Get Total Lines.
Total of all document lines */
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)get_Value("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public static final int USER1_ID_AD_Reference_ID=134;
/** Set User1.
User defined element #1 */
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) set_Value ("User1_ID", null);
 else 
set_Value ("User1_ID", new Integer(User1_ID));
}
/** Get User1.
User defined element #1 */
public int getUser1_ID() 
{
Integer ii = (Integer)get_Value("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public static final int USER2_ID_AD_Reference_ID=137;
/** Set User2.
User defined element #2 */
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) set_Value ("User2_ID", null);
 else 
set_Value ("User2_ID", new Integer(User2_ID));
}
/** Get User2.
User defined element #2 */
public int getUser2_ID() 
{
Integer ii = (Integer)get_Value("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for C_Order
 ** @version $Id: X_C_Order.java,v 1.27 2003/10/31 05:30:53 jjanke Exp $ **/
public class X_C_Order extends PO
{
/** Standard Constructor **/
public X_C_Order (Properties ctx, int C_Order_ID)
{
super (ctx, C_Order_ID);
/** if (C_Order_ID == 0)
{
setBillTo_ID (0);
setC_BPartner_ID (0);
setC_BPartner_Location_ID (0);
setC_Currency_ID (0);
setC_DocTypeTarget_ID (0);
setC_DocType_ID (0);
setC_Order_ID (0);
setC_PaymentTerm_ID (0);
setDateAcct (new Timestamp(System.currentTimeMillis()));
setDateOrdered (new Timestamp(System.currentTimeMillis()));
setDatePromised (new Timestamp(System.currentTimeMillis()));
setDeliveryRule (null);
setDeliveryViaRule (null);
setDocAction (null);
setDocStatus (null);
setDocumentNo (null);
setFreightAmt (Env.ZERO);
setFreightCostRule (null);
setGrandTotal (Env.ZERO);
setInvoiceRule (null);
setIsApproved (false);
setIsCreditApproved (false);
setIsDelivered (false);
setIsDiscountPrinted (false);
setIsInvoiced (false);
setIsPrinted (false);
setIsSOTrx (false);
setIsSelected (false);
setIsSelfService (false);
setIsTaxIncluded (false);
setIsTransferred (false);
setM_PriceList_ID (0);
setM_Warehouse_ID (0);
setPaymentRule (null);
setPosted (false);
setPriorityRule (null);
setProcessed (false);
setSalesRep_ID (0);
setSendEMail (false);
setTotalLines (Env.ZERO);
}
 **/
}
/** Load Constructor **/
public X_C_Order (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 259;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_C_Order[").append(getID()).append("]");
return sb.toString();
}
public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
{
if (AD_OrgTrx_ID == 0) setValue ("AD_OrgTrx_ID", null);
 else 
setValue ("AD_OrgTrx_ID", new Integer(AD_OrgTrx_ID));
}
public int getAD_OrgTrx_ID() 
{
Integer ii = (Integer)getValue("AD_OrgTrx_ID");
if (ii == null) return 0;
return ii.intValue();
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
public void setBillTo_ID (int BillTo_ID)
{
setValue ("BillTo_ID", new Integer(BillTo_ID));
}
public int getBillTo_ID() 
{
Integer ii = (Integer)getValue("BillTo_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Activity_ID (int C_Activity_ID)
{
if (C_Activity_ID == 0) setValue ("C_Activity_ID", null);
 else 
setValue ("C_Activity_ID", new Integer(C_Activity_ID));
}
public int getC_Activity_ID() 
{
Integer ii = (Integer)getValue("C_Activity_ID");
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
public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
{
setValue ("C_BPartner_Location_ID", new Integer(C_BPartner_Location_ID));
}
public int getC_BPartner_Location_ID() 
{
Integer ii = (Integer)getValue("C_BPartner_Location_ID");
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
public void setC_CashLine_ID (int C_CashLine_ID)
{
if (C_CashLine_ID == 0) setValue ("C_CashLine_ID", null);
 else 
setValue ("C_CashLine_ID", new Integer(C_CashLine_ID));
}
public int getC_CashLine_ID() 
{
Integer ii = (Integer)getValue("C_CashLine_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_Charge_ID (int C_Charge_ID)
{
if (C_Charge_ID == 0) setValue ("C_Charge_ID", null);
 else 
setValue ("C_Charge_ID", new Integer(C_Charge_ID));
}
public int getC_Charge_ID() 
{
Integer ii = (Integer)getValue("C_Charge_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_Currency_ID (int C_Currency_ID)
{
setValueNoCheck ("C_Currency_ID", new Integer(C_Currency_ID));
}
public int getC_Currency_ID() 
{
Integer ii = (Integer)getValue("C_Currency_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
{
setValue ("C_DocTypeTarget_ID", new Integer(C_DocTypeTarget_ID));
}
public int getC_DocTypeTarget_ID() 
{
Integer ii = (Integer)getValue("C_DocTypeTarget_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_DocType_ID (int C_DocType_ID)
{
setValueNoCheck ("C_DocType_ID", new Integer(C_DocType_ID));
}
public int getC_DocType_ID() 
{
Integer ii = (Integer)getValue("C_DocType_ID");
if (ii == null) return 0;
return ii.intValue();
}
void setC_Order_ID (int C_Order_ID)
{
setValueNoCheck ("C_Order_ID", new Integer(C_Order_ID));
}
public int getC_Order_ID() 
{
Integer ii = (Integer)getValue("C_Order_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setC_PaymentTerm_ID (int C_PaymentTerm_ID)
{
setValue ("C_PaymentTerm_ID", new Integer(C_PaymentTerm_ID));
}
public int getC_PaymentTerm_ID() 
{
Integer ii = (Integer)getValue("C_PaymentTerm_ID");
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
public void setChargeAmt (BigDecimal ChargeAmt)
{
setValue ("ChargeAmt", ChargeAmt);
}
public BigDecimal getChargeAmt() 
{
BigDecimal bd = (BigDecimal)getValue("ChargeAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public void setCopyFrom (String CopyFrom)
{
setValue ("CopyFrom", CopyFrom);
}
public String getCopyFrom() 
{
return (String)getValue("CopyFrom");
}
public void setDateAcct (Timestamp DateAcct)
{
if (DateAcct == null) throw new IllegalArgumentException ("DateAcct is mandatory");
setValue ("DateAcct", DateAcct);
}
public Timestamp getDateAcct() 
{
return (Timestamp)getValue("DateAcct");
}
public void setDateOrdered (Timestamp DateOrdered)
{
if (DateOrdered == null) throw new IllegalArgumentException ("DateOrdered is mandatory");
setValue ("DateOrdered", DateOrdered);
}
public Timestamp getDateOrdered() 
{
return (Timestamp)getValue("DateOrdered");
}
public void setDatePrinted (Timestamp DatePrinted)
{
setValue ("DatePrinted", DatePrinted);
}
public Timestamp getDatePrinted() 
{
return (Timestamp)getValue("DatePrinted");
}
public void setDatePromised (Timestamp DatePromised)
{
if (DatePromised == null) throw new IllegalArgumentException ("DatePromised is mandatory");
setValue ("DatePromised", DatePromised);
}
public Timestamp getDatePromised() 
{
return (Timestamp)getValue("DatePromised");
}
public static final String DELIVERYRULE_AfterReceipt = "R";
public static final String DELIVERYRULE_Availability = "A";
public static final String DELIVERYRULE_CompleteLine = "L";
public static final String DELIVERYRULE_CompleteOrder = "O";
public void setDeliveryRule (String DeliveryRule)
{
if (DeliveryRule.equals("R") || DeliveryRule.equals("A") || DeliveryRule.equals("L") || DeliveryRule.equals("O"));
 else throw new IllegalArgumentException ("DeliveryRule Invalid value - Reference_ID=151 - R - A - L - O");
if (DeliveryRule == null) throw new IllegalArgumentException ("DeliveryRule is mandatory");
setValue ("DeliveryRule", DeliveryRule);
}
public String getDeliveryRule() 
{
return (String)getValue("DeliveryRule");
}
public static final String DELIVERYVIARULE_Pickup = "P";
public static final String DELIVERYVIARULE_Delivery = "D";
public static final String DELIVERYVIARULE_Shipper = "S";
public void setDeliveryViaRule (String DeliveryViaRule)
{
if (DeliveryViaRule.equals("P") || DeliveryViaRule.equals("D") || DeliveryViaRule.equals("S"));
 else throw new IllegalArgumentException ("DeliveryViaRule Invalid value - Reference_ID=152 - P - D - S");
if (DeliveryViaRule == null) throw new IllegalArgumentException ("DeliveryViaRule is mandatory");
setValue ("DeliveryViaRule", DeliveryViaRule);
}
public String getDeliveryViaRule() 
{
return (String)getValue("DeliveryViaRule");
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public static final String DOCACTION_Process = "PR";
public static final String DOCACTION_Unlock = "XL";
public static final String DOCACTION_Complete = "CO";
public static final String DOCACTION_Approve = "AP";
public static final String DOCACTION_Reject = "RJ";
public static final String DOCACTION_Post = "PO";
public static final String DOCACTION_Void = "VO";
public static final String DOCACTION_Close = "CL";
public static final String DOCACTION_ReverseMinusCorrection = "RC";
public static final String DOCACTION_ReverseMinusAccrual = "RA";
public static final String DOCACTION_Transfer = "TR";
public static final String DOCACTION_ReMinusActivate = "RE";
public static final String DOCACTION_LeNoneGt = "--";
public void setDocAction (String DocAction)
{
if (DocAction.equals("PR") || DocAction.equals("XL") || DocAction.equals("CO") || DocAction.equals("AP") || DocAction.equals("RJ") || DocAction.equals("PO") || DocAction.equals("VO") || DocAction.equals("CL") || DocAction.equals("RC") || DocAction.equals("RA") || DocAction.equals("TR") || DocAction.equals("RE") || DocAction.equals("--"));
 else throw new IllegalArgumentException ("DocAction Invalid value - Reference_ID=135 - PR - XL - CO - AP - RJ - PO - VO - CL - RC - RA - TR - RE - --");
if (DocAction == null) throw new IllegalArgumentException ("DocAction is mandatory");
setValue ("DocAction", DocAction);
}
public String getDocAction() 
{
return (String)getValue("DocAction");
}
public static final String DOCSTATUS_InProgress = "IP";
public static final String DOCSTATUS_WaitingPayment = "WP";
public static final String DOCSTATUS_Drafted = "DR";
public static final String DOCSTATUS_Completed = "CO";
public static final String DOCSTATUS_Approved = "AP";
public static final String DOCSTATUS_Changed = "CH";
public static final String DOCSTATUS_NotApproved = "NA";
public static final String DOCSTATUS_TransferError = "TE";
public static final String DOCSTATUS_Printed = "PR";
public static final String DOCSTATUS_Transferred = "TR";
public static final String DOCSTATUS_Voided = "VO";
public static final String DOCSTATUS_Inactive = "IN";
public static final String DOCSTATUS_PostingError = "PE";
public static final String DOCSTATUS_Posted = "PO";
public static final String DOCSTATUS_Reversed = "RE";
public static final String DOCSTATUS_Closed = "CL";
public static final String DOCSTATUS_Unknown = "??";
public static final String DOCSTATUS_BeingProcessed = "XX";
void setDocStatus (String DocStatus)
{
if (DocStatus.equals("IP") || DocStatus.equals("WP") || DocStatus.equals("DR") || DocStatus.equals("CO") || DocStatus.equals("AP") || DocStatus.equals("CH") || DocStatus.equals("NA") || DocStatus.equals("TE") || DocStatus.equals("PR") || DocStatus.equals("TR") || DocStatus.equals("VO") || DocStatus.equals("IN") || DocStatus.equals("PE") || DocStatus.equals("PO") || DocStatus.equals("RE") || DocStatus.equals("CL") || DocStatus.equals("??") || DocStatus.equals("XX"));
 else throw new IllegalArgumentException ("DocStatus Invalid value - Reference_ID=131 - IP - WP - DR - CO - AP - CH - NA - TE - PR - TR - VO - IN - PE - PO - RE - CL - ?? - XX");
if (DocStatus == null) throw new IllegalArgumentException ("DocStatus is mandatory");
setValueNoCheck ("DocStatus", DocStatus);
}
public String getDocStatus() 
{
return (String)getValue("DocStatus");
}
void setDocumentNo (String DocumentNo)
{
if (DocumentNo == null) throw new IllegalArgumentException ("DocumentNo is mandatory");
setValueNoCheck ("DocumentNo", DocumentNo);
}
public String getDocumentNo() 
{
return (String)getValue("DocumentNo");
}
public void setDropShip_BPartner_ID (int DropShip_BPartner_ID)
{
if (DropShip_BPartner_ID == 0) setValue ("DropShip_BPartner_ID", null);
 else 
setValue ("DropShip_BPartner_ID", new Integer(DropShip_BPartner_ID));
}
public int getDropShip_BPartner_ID() 
{
Integer ii = (Integer)getValue("DropShip_BPartner_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setDropShip_Location_ID (int DropShip_Location_ID)
{
if (DropShip_Location_ID == 0) setValue ("DropShip_Location_ID", null);
 else 
setValue ("DropShip_Location_ID", new Integer(DropShip_Location_ID));
}
public int getDropShip_Location_ID() 
{
Integer ii = (Integer)getValue("DropShip_Location_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setDropShip_User_ID (int DropShip_User_ID)
{
if (DropShip_User_ID == 0) setValue ("DropShip_User_ID", null);
 else 
setValue ("DropShip_User_ID", new Integer(DropShip_User_ID));
}
public int getDropShip_User_ID() 
{
Integer ii = (Integer)getValue("DropShip_User_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setFreightAmt (BigDecimal FreightAmt)
{
if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory");
setValue ("FreightAmt", FreightAmt);
}
public BigDecimal getFreightAmt() 
{
BigDecimal bd = (BigDecimal)getValue("FreightAmt");
if (bd == null) return Env.ZERO;
return bd;
}
public static final String FREIGHTCOSTRULE_FreightIncluded = "I";
public static final String FREIGHTCOSTRULE_FixPrice = "F";
public static final String FREIGHTCOSTRULE_Calculated = "C";
public static final String FREIGHTCOSTRULE_Line = "L";
public void setFreightCostRule (String FreightCostRule)
{
if (FreightCostRule.equals("I") || FreightCostRule.equals("F") || FreightCostRule.equals("C") || FreightCostRule.equals("L"));
 else throw new IllegalArgumentException ("FreightCostRule Invalid value - Reference_ID=153 - I - F - C - L");
if (FreightCostRule == null) throw new IllegalArgumentException ("FreightCostRule is mandatory");
setValue ("FreightCostRule", FreightCostRule);
}
public String getFreightCostRule() 
{
return (String)getValue("FreightCostRule");
}
void setGrandTotal (BigDecimal GrandTotal)
{
if (GrandTotal == null) throw new IllegalArgumentException ("GrandTotal is mandatory");
setValueNoCheck ("GrandTotal", GrandTotal);
}
public BigDecimal getGrandTotal() 
{
BigDecimal bd = (BigDecimal)getValue("GrandTotal");
if (bd == null) return Env.ZERO;
return bd;
}
public static final String INVOICERULE_AfterOrderDelivered = "O";
public static final String INVOICERULE_AfterDelivery = "D";
public static final String INVOICERULE_CustomerScheduleAfterDelivery = "S";
public static final String INVOICERULE_Immediate = "I";
public void setInvoiceRule (String InvoiceRule)
{
if (InvoiceRule.equals("O") || InvoiceRule.equals("D") || InvoiceRule.equals("S") || InvoiceRule.equals("I"));
 else throw new IllegalArgumentException ("InvoiceRule Invalid value - Reference_ID=150 - O - D - S - I");
if (InvoiceRule == null) throw new IllegalArgumentException ("InvoiceRule is mandatory");
setValue ("InvoiceRule", InvoiceRule);
}
public String getInvoiceRule() 
{
return (String)getValue("InvoiceRule");
}
void setIsApproved (boolean IsApproved)
{
setValueNoCheck ("IsApproved", new Boolean(IsApproved));
}
public boolean isApproved() 
{
Boolean bb = (Boolean)getValue("IsApproved");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsCreditApproved (boolean IsCreditApproved)
{
setValueNoCheck ("IsCreditApproved", new Boolean(IsCreditApproved));
}
public boolean isCreditApproved() 
{
Boolean bb = (Boolean)getValue("IsCreditApproved");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsDelivered (boolean IsDelivered)
{
setValueNoCheck ("IsDelivered", new Boolean(IsDelivered));
}
public boolean isDelivered() 
{
Boolean bb = (Boolean)getValue("IsDelivered");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsDiscountPrinted (boolean IsDiscountPrinted)
{
setValue ("IsDiscountPrinted", new Boolean(IsDiscountPrinted));
}
public boolean isDiscountPrinted() 
{
Boolean bb = (Boolean)getValue("IsDiscountPrinted");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsInvoiced (boolean IsInvoiced)
{
setValueNoCheck ("IsInvoiced", new Boolean(IsInvoiced));
}
public boolean isInvoiced() 
{
Boolean bb = (Boolean)getValue("IsInvoiced");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsPrinted (boolean IsPrinted)
{
setValueNoCheck ("IsPrinted", new Boolean(IsPrinted));
}
public boolean isPrinted() 
{
Boolean bb = (Boolean)getValue("IsPrinted");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSOTrx (boolean IsSOTrx)
{
setValue ("IsSOTrx", new Boolean(IsSOTrx));
}
public boolean isSOTrx() 
{
Boolean bb = (Boolean)getValue("IsSOTrx");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSelected (boolean IsSelected)
{
setValue ("IsSelected", new Boolean(IsSelected));
}
public boolean isSelected() 
{
Boolean bb = (Boolean)getValue("IsSelected");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsSelfService (boolean IsSelfService)
{
setValue ("IsSelfService", new Boolean(IsSelfService));
}
public boolean isSelfService() 
{
Boolean bb = (Boolean)getValue("IsSelfService");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTaxIncluded (boolean IsTaxIncluded)
{
setValue ("IsTaxIncluded", new Boolean(IsTaxIncluded));
}
public boolean isTaxIncluded() 
{
Boolean bb = (Boolean)getValue("IsTaxIncluded");
if (bb != null) return bb.booleanValue();
return false;
}
void setIsTransferred (boolean IsTransferred)
{
setValueNoCheck ("IsTransferred", new Boolean(IsTransferred));
}
public boolean isTransferred() 
{
Boolean bb = (Boolean)getValue("IsTransferred");
if (bb != null) return bb.booleanValue();
return false;
}
public void setM_PriceList_ID (int M_PriceList_ID)
{
setValue ("M_PriceList_ID", new Integer(M_PriceList_ID));
}
public int getM_PriceList_ID() 
{
Integer ii = (Integer)getValue("M_PriceList_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setM_Shipper_ID (int M_Shipper_ID)
{
if (M_Shipper_ID == 0) setValue ("M_Shipper_ID", null);
 else 
setValue ("M_Shipper_ID", new Integer(M_Shipper_ID));
}
public int getM_Shipper_ID() 
{
Integer ii = (Integer)getValue("M_Shipper_ID");
if (ii == null) return 0;
return ii.intValue();
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
public void setPOReference (String POReference)
{
setValue ("POReference", POReference);
}
public String getPOReference() 
{
return (String)getValue("POReference");
}
public static final String PAYMENTRULE_Cash = "B";
public static final String PAYMENTRULE_CreditCard = "K";
public static final String PAYMENTRULE_TransferACH = "T";
public static final String PAYMENTRULE_Check = "S";
public static final String PAYMENTRULE_OnCredit = "P";
public void setPaymentRule (String PaymentRule)
{
if (PaymentRule.equals("B") || PaymentRule.equals("K") || PaymentRule.equals("T") || PaymentRule.equals("S") || PaymentRule.equals("P"));
 else throw new IllegalArgumentException ("PaymentRule Invalid value - Reference_ID=195 - B - K - T - S - P");
if (PaymentRule == null) throw new IllegalArgumentException ("PaymentRule is mandatory");
setValue ("PaymentRule", PaymentRule);
}
public String getPaymentRule() 
{
return (String)getValue("PaymentRule");
}
public void setPosted (boolean Posted)
{
setValue ("Posted", new Boolean(Posted));
}
public boolean isPosted() 
{
Boolean bb = (Boolean)getValue("Posted");
if (bb != null) return bb.booleanValue();
return false;
}
public static final String PRIORITYRULE_High = "3";
public static final String PRIORITYRULE_Medium = "5";
public static final String PRIORITYRULE_Low = "7";
public void setPriorityRule (String PriorityRule)
{
if (PriorityRule.equals("3") || PriorityRule.equals("5") || PriorityRule.equals("7"));
 else throw new IllegalArgumentException ("PriorityRule Invalid value - Reference_ID=154 - 3 - 5 - 7");
if (PriorityRule == null) throw new IllegalArgumentException ("PriorityRule is mandatory");
setValue ("PriorityRule", PriorityRule);
}
public String getPriorityRule() 
{
return (String)getValue("PriorityRule");
}
void setProcessed (boolean Processed)
{
setValueNoCheck ("Processed", new Boolean(Processed));
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
public void setSendEMail (boolean SendEMail)
{
setValue ("SendEMail", new Boolean(SendEMail));
}
public boolean isSendEMail() 
{
Boolean bb = (Boolean)getValue("SendEMail");
if (bb != null) return bb.booleanValue();
return false;
}
void setTotalLines (BigDecimal TotalLines)
{
if (TotalLines == null) throw new IllegalArgumentException ("TotalLines is mandatory");
setValueNoCheck ("TotalLines", TotalLines);
}
public BigDecimal getTotalLines() 
{
BigDecimal bd = (BigDecimal)getValue("TotalLines");
if (bd == null) return Env.ZERO;
return bd;
}
public void setUser1_ID (int User1_ID)
{
if (User1_ID == 0) setValue ("User1_ID", null);
 else 
setValue ("User1_ID", new Integer(User1_ID));
}
public int getUser1_ID() 
{
Integer ii = (Integer)getValue("User1_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setUser2_ID (int User2_ID)
{
if (User2_ID == 0) setValue ("User2_ID", null);
 else 
setValue ("User2_ID", new Integer(User2_ID));
}
public int getUser2_ID() 
{
Integer ii = (Integer)getValue("User2_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
