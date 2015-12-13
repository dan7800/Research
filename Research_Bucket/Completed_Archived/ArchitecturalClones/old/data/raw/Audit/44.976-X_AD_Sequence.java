/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext()
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys()
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo()
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence()
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID()
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix()
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear()
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo()
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix()
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat()
{
return (String)getValue("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.73 2004/05/20 05:59:10 jjanke Exp $ */
public class X_AD_Sequence extends PO
{
/** Standard Constructor */
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);	// 1000000
setCurrentNextSys (0);	// 100
setIncrementNo (0);	// 1
setIsAutoSequence (false);
setName (null);
setStartNo (0);	// 1000000
}
 */
}
/** Load Constructor */
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=115;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_ValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next.
The next number to be used */
public void setCurrentNext (int CurrentNext)
{
set_Value ("CurrentNext", new Integer(CurrentNext));
}
/** Get Current Next.
The next number to be used */
public int getCurrentNext() 
{
Integer ii = (Integer)get_Value("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next (System).
Next sequence for system use */
public void setCurrentNextSys (int CurrentNextSys)
{
set_Value ("CurrentNextSys", new Integer(CurrentNextSys));
}
/** Get Current Next (System).
Next sequence for system use */
public int getCurrentNextSys() 
{
Integer ii = (Integer)get_Value("CurrentNextSys");
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
/** Set Increment.
The number to increment the last document number by */
public void setIncrementNo (int IncrementNo)
{
set_Value ("IncrementNo", new Integer(IncrementNo));
}
/** Get Increment.
The number to increment the last document number by */
public int getIncrementNo() 
{
Integer ii = (Integer)get_Value("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Auto numbering.
Automatically assign the next number */
public void setIsAutoSequence (boolean IsAutoSequence)
{
set_Value ("IsAutoSequence", new Boolean(IsAutoSequence));
}
/** Get Auto numbering.
Automatically assign the next number */
public boolean isAutoSequence() 
{
Object oo = get_Value("IsAutoSequence");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Used for Record ID.
The document number  will be used as the record key */
public void setIsTableID (boolean IsTableID)
{
set_Value ("IsTableID", new Boolean(IsTableID));
}
/** Get Used for Record ID.
The document number  will be used as the record key */
public boolean isTableID() 
{
Object oo = get_Value("IsTableID");
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
/** Set Prefix.
Prefix before the sequence number */
public void setPrefix (String Prefix)
{
if (Prefix != null && Prefix.length() > 10)
{
log.warn("setPrefix - length > 10 - truncated");
Prefix = Prefix.substring(0,9);
}
set_Value ("Prefix", Prefix);
}
/** Get Prefix.
Prefix before the sequence number */
public String getPrefix() 
{
return (String)get_Value("Prefix");
}
/** Set Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public void setStartNewYear (boolean StartNewYear)
{
set_Value ("StartNewYear", new Boolean(StartNewYear));
}
/** Get Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public boolean isStartNewYear() 
{
Object oo = get_Value("StartNewYear");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Start No.
Starting number/position */
public void setStartNo (int StartNo)
{
set_Value ("StartNo", new Integer(StartNo));
}
/** Get Start No.
Starting number/position */
public int getStartNo() 
{
Integer ii = (Integer)get_Value("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Suffix.
Suffix after the number */
public void setSuffix (String Suffix)
{
if (Suffix != null && Suffix.length() > 10)
{
log.warn("setSuffix - length > 10 - truncated");
Suffix = Suffix.substring(0,9);
}
set_Value ("Suffix", Suffix);
}
/** Get Suffix.
Suffix after the number */
public String getSuffix() 
{
return (String)get_Value("Suffix");
}
/** Set Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public void setVFormat (String VFormat)
{
if (VFormat != null && VFormat.length() > 40)
{
log.warn("setVFormat - length > 40 - truncated");
VFormat = VFormat.substring(0,39);
}
set_Value ("VFormat", VFormat);
}
/** Get Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public String getVFormat() 
{
return (String)get_Value("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.9 */
public class X_AD_Sequence extends PO
{
/** Standard Constructor */
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);	// 1000000
setCurrentNextSys (0);	// 100
setIncrementNo (0);	// 1
setIsAutoSequence (false);
setName (null);
setStartNo (0);	// 1000000
}
 */
}
/** Load Constructor */
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=115 */
public static final int Table_ID=115;

/** TableName=AD_Sequence */
public static final String Table_Name="AD_Sequence";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_ValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next.
The next number to be used */
public void setCurrentNext (int CurrentNext)
{
set_Value ("CurrentNext", new Integer(CurrentNext));
}
/** Get Current Next.
The next number to be used */
public int getCurrentNext() 
{
Integer ii = (Integer)get_Value("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next (System).
Next sequence for system use */
public void setCurrentNextSys (int CurrentNextSys)
{
set_Value ("CurrentNextSys", new Integer(CurrentNextSys));
}
/** Get Current Next (System).
Next sequence for system use */
public int getCurrentNextSys() 
{
Integer ii = (Integer)get_Value("CurrentNextSys");
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
/** Set Increment.
The number to increment the last document number by */
public void setIncrementNo (int IncrementNo)
{
set_Value ("IncrementNo", new Integer(IncrementNo));
}
/** Get Increment.
The number to increment the last document number by */
public int getIncrementNo() 
{
Integer ii = (Integer)get_Value("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Auto numbering.
Automatically assign the next number */
public void setIsAutoSequence (boolean IsAutoSequence)
{
set_Value ("IsAutoSequence", new Boolean(IsAutoSequence));
}
/** Get Auto numbering.
Automatically assign the next number */
public boolean isAutoSequence() 
{
Object oo = get_Value("IsAutoSequence");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Used for Record ID.
The document number  will be used as the record key */
public void setIsTableID (boolean IsTableID)
{
set_Value ("IsTableID", new Boolean(IsTableID));
}
/** Get Used for Record ID.
The document number  will be used as the record key */
public boolean isTableID() 
{
Object oo = get_Value("IsTableID");
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
/** Set Prefix.
Prefix before the sequence number */
public void setPrefix (String Prefix)
{
if (Prefix != null && Prefix.length() > 10)
{
log.warn("setPrefix - length > 10 - truncated");
Prefix = Prefix.substring(0,9);
}
set_Value ("Prefix", Prefix);
}
/** Get Prefix.
Prefix before the sequence number */
public String getPrefix() 
{
return (String)get_Value("Prefix");
}
/** Set Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public void setStartNewYear (boolean StartNewYear)
{
set_Value ("StartNewYear", new Boolean(StartNewYear));
}
/** Get Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public boolean isStartNewYear() 
{
Object oo = get_Value("StartNewYear");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Start No.
Starting number/position */
public void setStartNo (int StartNo)
{
set_Value ("StartNo", new Integer(StartNo));
}
/** Get Start No.
Starting number/position */
public int getStartNo() 
{
Integer ii = (Integer)get_Value("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Suffix.
Suffix after the number */
public void setSuffix (String Suffix)
{
if (Suffix != null && Suffix.length() > 10)
{
log.warn("setSuffix - length > 10 - truncated");
Suffix = Suffix.substring(0,9);
}
set_Value ("Suffix", Suffix);
}
/** Get Suffix.
Suffix after the number */
public String getSuffix() 
{
return (String)get_Value("Suffix");
}
/** Set Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public void setVFormat (String VFormat)
{
if (VFormat != null && VFormat.length() > 40)
{
log.warn("setVFormat - length > 40 - truncated");
VFormat = VFormat.substring(0,39);
}
set_Value ("VFormat", VFormat);
}
/** Get Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public String getVFormat() 
{
return (String)get_Value("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext()
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys()
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo()
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence()
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID()
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix()
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear()
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo()
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix()
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat()
{
return (String)getValue("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext()
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys()
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo()
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence()
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID()
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix()
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear()
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo()
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix()
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat()
{
return (String)getValue("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.73 2004/05/20 05:59:10 jjanke Exp $ */
public class X_AD_Sequence extends PO
{
/** Standard Constructor */
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);	// 1000000
setCurrentNextSys (0);	// 100
setIncrementNo (0);	// 1
setIsAutoSequence (false);
setName (null);
setStartNo (0);	// 1000000
}
 */
}
/** Load Constructor */
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=115;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_ValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next.
The next number to be used */
public void setCurrentNext (int CurrentNext)
{
set_Value ("CurrentNext", new Integer(CurrentNext));
}
/** Get Current Next.
The next number to be used */
public int getCurrentNext() 
{
Integer ii = (Integer)get_Value("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next (System).
Next sequence for system use */
public void setCurrentNextSys (int CurrentNextSys)
{
set_Value ("CurrentNextSys", new Integer(CurrentNextSys));
}
/** Get Current Next (System).
Next sequence for system use */
public int getCurrentNextSys() 
{
Integer ii = (Integer)get_Value("CurrentNextSys");
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
/** Set Increment.
The number to increment the last document number by */
public void setIncrementNo (int IncrementNo)
{
set_Value ("IncrementNo", new Integer(IncrementNo));
}
/** Get Increment.
The number to increment the last document number by */
public int getIncrementNo() 
{
Integer ii = (Integer)get_Value("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Auto numbering.
Automatically assign the next number */
public void setIsAutoSequence (boolean IsAutoSequence)
{
set_Value ("IsAutoSequence", new Boolean(IsAutoSequence));
}
/** Get Auto numbering.
Automatically assign the next number */
public boolean isAutoSequence() 
{
Object oo = get_Value("IsAutoSequence");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Used for Record ID.
The document number  will be used as the record key */
public void setIsTableID (boolean IsTableID)
{
set_Value ("IsTableID", new Boolean(IsTableID));
}
/** Get Used for Record ID.
The document number  will be used as the record key */
public boolean isTableID() 
{
Object oo = get_Value("IsTableID");
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
/** Set Prefix.
Prefix before the sequence number */
public void setPrefix (String Prefix)
{
if (Prefix != null && Prefix.length() > 10)
{
log.warn("setPrefix - length > 10 - truncated");
Prefix = Prefix.substring(0,9);
}
set_Value ("Prefix", Prefix);
}
/** Get Prefix.
Prefix before the sequence number */
public String getPrefix() 
{
return (String)get_Value("Prefix");
}
/** Set Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public void setStartNewYear (boolean StartNewYear)
{
set_Value ("StartNewYear", new Boolean(StartNewYear));
}
/** Get Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public boolean isStartNewYear() 
{
Object oo = get_Value("StartNewYear");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Start No.
Starting number/position */
public void setStartNo (int StartNo)
{
set_Value ("StartNo", new Integer(StartNo));
}
/** Get Start No.
Starting number/position */
public int getStartNo() 
{
Integer ii = (Integer)get_Value("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Suffix.
Suffix after the number */
public void setSuffix (String Suffix)
{
if (Suffix != null && Suffix.length() > 10)
{
log.warn("setSuffix - length > 10 - truncated");
Suffix = Suffix.substring(0,9);
}
set_Value ("Suffix", Suffix);
}
/** Get Suffix.
Suffix after the number */
public String getSuffix() 
{
return (String)get_Value("Suffix");
}
/** Set Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public void setVFormat (String VFormat)
{
if (VFormat != null && VFormat.length() > 40)
{
log.warn("setVFormat - length > 40 - truncated");
VFormat = VFormat.substring(0,39);
}
set_Value ("VFormat", VFormat);
}
/** Get Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public String getVFormat() 
{
return (String)get_Value("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.27 2003/10/31 05:30:52 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
/** Standard Constructor **/
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
/** Load Constructor **/
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext() 
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys() 
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo() 
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence() 
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID() 
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix() 
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear() 
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo() 
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix() 
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat() 
{
return (String)getValue("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.9 */
public class X_AD_Sequence extends PO
{
/** Standard Constructor */
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);	// 1000000
setCurrentNextSys (0);	// 100
setIncrementNo (0);	// 1
setIsAutoSequence (false);
setName (null);
setStartNo (0);	// 1000000
}
 */
}
/** Load Constructor */
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=115 */
public static final int Table_ID=115;

/** TableName=AD_Sequence */
public static final String Table_Name="AD_Sequence";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
/** Set Sequence.
Document Sequence */
public void setAD_Sequence_ID (int AD_Sequence_ID)
{
set_ValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
/** Get Sequence.
Document Sequence */
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)get_Value("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next.
The next number to be used */
public void setCurrentNext (int CurrentNext)
{
set_Value ("CurrentNext", new Integer(CurrentNext));
}
/** Get Current Next.
The next number to be used */
public int getCurrentNext() 
{
Integer ii = (Integer)get_Value("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Current Next (System).
Next sequence for system use */
public void setCurrentNextSys (int CurrentNextSys)
{
set_Value ("CurrentNextSys", new Integer(CurrentNextSys));
}
/** Get Current Next (System).
Next sequence for system use */
public int getCurrentNextSys() 
{
Integer ii = (Integer)get_Value("CurrentNextSys");
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
/** Set Increment.
The number to increment the last document number by */
public void setIncrementNo (int IncrementNo)
{
set_Value ("IncrementNo", new Integer(IncrementNo));
}
/** Get Increment.
The number to increment the last document number by */
public int getIncrementNo() 
{
Integer ii = (Integer)get_Value("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Auto numbering.
Automatically assign the next number */
public void setIsAutoSequence (boolean IsAutoSequence)
{
set_Value ("IsAutoSequence", new Boolean(IsAutoSequence));
}
/** Get Auto numbering.
Automatically assign the next number */
public boolean isAutoSequence() 
{
Object oo = get_Value("IsAutoSequence");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Used for Record ID.
The document number  will be used as the record key */
public void setIsTableID (boolean IsTableID)
{
set_Value ("IsTableID", new Boolean(IsTableID));
}
/** Get Used for Record ID.
The document number  will be used as the record key */
public boolean isTableID() 
{
Object oo = get_Value("IsTableID");
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
/** Set Prefix.
Prefix before the sequence number */
public void setPrefix (String Prefix)
{
if (Prefix != null && Prefix.length() > 10)
{
log.warn("setPrefix - length > 10 - truncated");
Prefix = Prefix.substring(0,9);
}
set_Value ("Prefix", Prefix);
}
/** Get Prefix.
Prefix before the sequence number */
public String getPrefix() 
{
return (String)get_Value("Prefix");
}
/** Set Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public void setStartNewYear (boolean StartNewYear)
{
set_Value ("StartNewYear", new Boolean(StartNewYear));
}
/** Get Restart sequence every Year.
Restart the sequence with Start on every 1/1 */
public boolean isStartNewYear() 
{
Object oo = get_Value("StartNewYear");
if (oo != null) 
{
 if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
 return "Y".equals(oo);
}
return false;
}
/** Set Start No.
Starting number/position */
public void setStartNo (int StartNo)
{
set_Value ("StartNo", new Integer(StartNo));
}
/** Get Start No.
Starting number/position */
public int getStartNo() 
{
Integer ii = (Integer)get_Value("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
/** Set Suffix.
Suffix after the number */
public void setSuffix (String Suffix)
{
if (Suffix != null && Suffix.length() > 10)
{
log.warn("setSuffix - length > 10 - truncated");
Suffix = Suffix.substring(0,9);
}
set_Value ("Suffix", Suffix);
}
/** Get Suffix.
Suffix after the number */
public String getSuffix() 
{
return (String)get_Value("Suffix");
}
/** Set Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public void setVFormat (String VFormat)
{
if (VFormat != null && VFormat.length() > 40)
{
log.warn("setVFormat - length > 40 - truncated");
VFormat = VFormat.substring(0,39);
}
set_Value ("VFormat", VFormat);
}
/** Get Value Format.
Format of the value;
 Can contain fixed format elements, Variables: "_lLoOaAcCa09" */
public String getVFormat() 
{
return (String)get_Value("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID()
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext()
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys()
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription()
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo()
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence()
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID()
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix()
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear()
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo()
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix()
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat()
{
return (String)getValue("VFormat");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence
 ** @version $Id: X_AD_Sequence.java,v 1.27 2003/10/31 05:30:52 jjanke Exp $ **/
public class X_AD_Sequence extends PO
{
/** Standard Constructor **/
public X_AD_Sequence (Properties ctx, int AD_Sequence_ID)
{
super (ctx, AD_Sequence_ID);
/** if (AD_Sequence_ID == 0)
{
setAD_Sequence_ID (0);
setCurrentNext (0);
setCurrentNextSys (0);
setIncrementNo (0);
setIsAutoSequence (false);
setName (null);
setStartNo (0);
}
 **/
}
/** Load Constructor **/
public X_AD_Sequence (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 115;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence[").append(getID()).append("]");
return sb.toString();
}
void setAD_Sequence_ID (int AD_Sequence_ID)
{
setValueNoCheck ("AD_Sequence_ID", new Integer(AD_Sequence_ID));
}
public int getAD_Sequence_ID() 
{
Integer ii = (Integer)getValue("AD_Sequence_ID");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNext (int CurrentNext)
{
setValue ("CurrentNext", new Integer(CurrentNext));
}
public int getCurrentNext() 
{
Integer ii = (Integer)getValue("CurrentNext");
if (ii == null) return 0;
return ii.intValue();
}
public void setCurrentNextSys (int CurrentNextSys)
{
setValue ("CurrentNextSys", new Integer(CurrentNextSys));
}
public int getCurrentNextSys() 
{
Integer ii = (Integer)getValue("CurrentNextSys");
if (ii == null) return 0;
return ii.intValue();
}
public void setDescription (String Description)
{
setValue ("Description", Description);
}
public String getDescription() 
{
return (String)getValue("Description");
}
public void setIncrementNo (int IncrementNo)
{
setValue ("IncrementNo", new Integer(IncrementNo));
}
public int getIncrementNo() 
{
Integer ii = (Integer)getValue("IncrementNo");
if (ii == null) return 0;
return ii.intValue();
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
public void setIsAutoSequence (boolean IsAutoSequence)
{
setValue ("IsAutoSequence", new Boolean(IsAutoSequence));
}
public boolean isAutoSequence() 
{
Boolean bb = (Boolean)getValue("IsAutoSequence");
if (bb != null) return bb.booleanValue();
return false;
}
public void setIsTableID (boolean IsTableID)
{
setValue ("IsTableID", new Boolean(IsTableID));
}
public boolean isTableID() 
{
Boolean bb = (Boolean)getValue("IsTableID");
if (bb != null) return bb.booleanValue();
return false;
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
public void setPrefix (String Prefix)
{
setValue ("Prefix", Prefix);
}
public String getPrefix() 
{
return (String)getValue("Prefix");
}
public void setStartNewYear (boolean StartNewYear)
{
setValue ("StartNewYear", new Boolean(StartNewYear));
}
public boolean isStartNewYear() 
{
Boolean bb = (Boolean)getValue("StartNewYear");
if (bb != null) return bb.booleanValue();
return false;
}
public void setStartNo (int StartNo)
{
setValue ("StartNo", new Integer(StartNo));
}
public int getStartNo() 
{
Integer ii = (Integer)getValue("StartNo");
if (ii == null) return 0;
return ii.intValue();
}
public void setSuffix (String Suffix)
{
setValue ("Suffix", Suffix);
}
public String getSuffix() 
{
return (String)getValue("Suffix");
}
public void setVFormat (String VFormat)
{
setValue ("VFormat", VFormat);
}
public String getVFormat() 
{
return (String)getValue("VFormat");
}
}
