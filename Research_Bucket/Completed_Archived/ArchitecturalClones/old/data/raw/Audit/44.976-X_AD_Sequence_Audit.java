/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (null);
}
 **/
}
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID()
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (String Record_ID)
{
if (Record_ID == null) throw new IllegalArgumentException ("Record_ID is mandatory");
setValueNoCheck ("Record_ID", Record_ID);
}
public String getRecord_ID()
{
return (String)getValue("Record_ID");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.73 2004/05/20 05:59:12 jjanke Exp $ */
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor */
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 */
}
/** Load Constructor */
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=121;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
/** Set Table.
Table for the Fields */
public void setAD_Table_ID (int AD_Table_ID)
{
set_ValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
/** Get Table.
Table for the Fields */
public int getAD_Table_ID() 
{
Integer ii = (Integer)get_Value("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Record ID.
Direct internal record ID */
public void setRecord_ID (int Record_ID)
{
set_ValueNoCheck ("Record_ID", new Integer(Record_ID));
}
/** Get Record ID.
Direct internal record ID */
public int getRecord_ID() 
{
Integer ii = (Integer)get_Value("Record_ID");
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
/** Generated Model for AD_Sequence_Audit
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.916 */
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor */
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 */
}
/** Load Constructor */
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=121 */
public static final int Table_ID=121;

/** TableName=AD_Sequence_Audit */
public static final String Table_Name="AD_Sequence_Audit";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
/** Set Table.
Table for the Fields */
public void setAD_Table_ID (int AD_Table_ID)
{
set_ValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
/** Get Table.
Table for the Fields */
public int getAD_Table_ID() 
{
Integer ii = (Integer)get_Value("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Record ID.
Direct internal record ID */
public void setRecord_ID (int Record_ID)
{
set_ValueNoCheck ("Record_ID", new Integer(Record_ID));
}
/** Get Record ID.
Direct internal record ID */
public int getRecord_ID() 
{
Integer ii = (Integer)get_Value("Record_ID");
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
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (null);
}
 **/
}
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID()
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (String Record_ID)
{
if (Record_ID == null) throw new IllegalArgumentException ("Record_ID is mandatory");
setValueNoCheck ("Record_ID", Record_ID);
}
public String getRecord_ID()
{
return (String)getValue("Record_ID");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.14 2003/08/12 17:59:04 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (null);
}
 **/
}
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID()
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (String Record_ID)
{
if (Record_ID == null) throw new IllegalArgumentException ("Record_ID is mandatory");
setValueNoCheck ("Record_ID", Record_ID);
}
public String getRecord_ID()
{
return (String)getValue("Record_ID");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2004 Jorg Janke */
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import org.compiere.util.*;
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.73 2004/05/20 05:59:12 jjanke Exp $ */
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor */
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 */
}
/** Load Constructor */
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
public static final int Table_ID=121;

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
/** Set Table.
Table for the Fields */
public void setAD_Table_ID (int AD_Table_ID)
{
set_ValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
/** Get Table.
Table for the Fields */
public int getAD_Table_ID() 
{
Integer ii = (Integer)get_Value("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Record ID.
Direct internal record ID */
public void setRecord_ID (int Record_ID)
{
set_ValueNoCheck ("Record_ID", new Integer(Record_ID));
}
/** Get Record ID.
Direct internal record ID */
public int getRecord_ID() 
{
Integer ii = (Integer)get_Value("Record_ID");
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
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.27 2003/10/31 05:30:54 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor **/
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 **/
}
/** Load Constructor **/
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID() 
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (int Record_ID)
{
setValueNoCheck ("Record_ID", new Integer(Record_ID));
}
public int getRecord_ID() 
{
Integer ii = (Integer)getValue("Record_ID");
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
/** Generated Model for AD_Sequence_Audit
 *  @author Jorg Janke (generated) 
 *  @version Release 2.5.1f - 2004-09-04 13:47:04.916 */
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor */
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 */
}
/** Load Constructor */
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** AD_Table_ID=121 */
public static final int Table_ID=121;

/** TableName=AD_Sequence_Audit */
public static final String Table_Name="AD_Sequence_Audit";

/** Load Meta Data */
protected POInfo initPO (Properties ctx)
{
POInfo poi = POInfo.getPOInfo (ctx, Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
/** Set Table.
Table for the Fields */
public void setAD_Table_ID (int AD_Table_ID)
{
set_ValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
/** Get Table.
Table for the Fields */
public int getAD_Table_ID() 
{
Integer ii = (Integer)get_Value("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
/** Set Record ID.
Direct internal record ID */
public void setRecord_ID (int Record_ID)
{
set_ValueNoCheck ("Record_ID", new Integer(Record_ID));
}
/** Get Record ID.
Direct internal record ID */
public int getRecord_ID() 
{
Integer ii = (Integer)get_Value("Record_ID");
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
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.7 2003/07/22 05:41:47 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (null);
}
 **/
}
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID()
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (String Record_ID)
{
if (Record_ID == null) throw new IllegalArgumentException ("Record_ID is mandatory");
setValueNoCheck ("Record_ID", Record_ID);
}
public String getRecord_ID()
{
return (String)getValue("Record_ID");
}
}
/** Generated Model - DO NOT CHANGE - Copyright (C) 1999-2003 Jorg Janke **/
package org.compiere.model;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.Serializable;
import org.compiere.util.*;
/** Generated Model for AD_Sequence_Audit
 ** @version $Id: X_AD_Sequence_Audit.java,v 1.27 2003/10/31 05:30:54 jjanke Exp $ **/
public class X_AD_Sequence_Audit extends PO
{
/** Standard Constructor **/
public X_AD_Sequence_Audit (Properties ctx, int AD_Sequence_Audit_ID)
{
super (ctx, AD_Sequence_Audit_ID);
/** if (AD_Sequence_Audit_ID == 0)
{
setAD_Sequence_ID (0);
setAD_Table_ID (0);
setDocumentNo (null);
setRecord_ID (0);
}
 **/
}
/** Load Constructor **/
public X_AD_Sequence_Audit (Properties ctx, ResultSet rs)
{
super (ctx, rs);
}
/** Load Meta Data **/
protected POInfo initPO (Properties ctx)
{
int AD_Table_ID = 121;
POInfo poi = POInfo.getPOInfo (ctx, AD_Table_ID);
return poi;
}
public String toString()
{
StringBuffer sb = new StringBuffer ("X_AD_Sequence_Audit[").append(getID()).append("]");
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
void setAD_Table_ID (int AD_Table_ID)
{
setValueNoCheck ("AD_Table_ID", new Integer(AD_Table_ID));
}
public int getAD_Table_ID() 
{
Integer ii = (Integer)getValue("AD_Table_ID");
if (ii == null) return 0;
return ii.intValue();
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
void setRecord_ID (int Record_ID)
{
setValueNoCheck ("Record_ID", new Integer(Record_ID));
}
public int getRecord_ID() 
{
Integer ii = (Integer)getValue("Record_ID");
if (ii == null) return 0;
return ii.intValue();
}
}
