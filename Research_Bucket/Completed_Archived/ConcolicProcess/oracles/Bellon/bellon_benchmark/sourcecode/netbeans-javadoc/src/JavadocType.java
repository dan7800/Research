/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JavadocType.java
 *
 * Created on 30. prosinec 2000, 11:47
 */
package org.netbeans.modules.javadoc;
import java.util.*;
import java.io.File;
import org.openide.ServiceType;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.MapFormat;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.javadoc.settings.StdDocletSettingsService;
import org.openide.util.Lookup;
/**
 *
 * @author  Petr Suchomel
 * @version 
 */
public abstract class JavadocType extends ServiceType {
    /** generated Serialized Version UID */
    static final long serialVersionUID =-4590199769798257324L;
    /** doclets */
    public ServiceType.Handle doclets;
    /** hold the inicialized doclet */
    transient StdDocletSettingsService type = null;
    /** Holds value of property useThis. */
    //protected boolean useThis = false;
    /** Creates new JavadocType */
    public JavadocType() { }
    /** Getter for members
    */
    public abstract long getMembers();
    /** Setter for members
    */
    public abstract void setMembers( long l );
    /** Getter for path to overview file.
    */
    public abstract File getOverview ();
    /** Setter for path to overview file.
    */
    public abstract void setOverview (File s);
    /** Getter for bootclasspath
    */
    public abstract String getBootclasspath ();
    /** Setter for bootclasspath
    */
    public abstract void setBootclasspath (String s);
    /** Getter for extension directories
    */
    public abstract String getExtdirs ();
    /** Setter extension directories
    */
    public abstract void setExtdirs (String s);
    /** Getter for JDK 1.1 Style
    */
    public abstract boolean isStyle1_1 ();
    /** Setter for JDK 1.1 Style
    */
    public abstract void setStyle1_1 (boolean b);
    /** Getter for verbose mode
    */
    public abstract boolean isVerbose ();
    /** Setter for verbose mode
    */
    public abstract void setVerbose (boolean b);
    /** Getter for encoding
    */
    public abstract String getEncoding ();
    /** Setter for encoding
    */
    public abstract void setEncoding (String s);
    /** Getter for locale
    */
    public abstract String getLocale ();
    /** Setter locale
    */
    public abstract void setLocale (String s);
    /** Getter for selected doclet
    */
    public ServiceType getDoclets () {        
        if (type != null)
            return type;
        if (doclets != null) {
            type = (StdDocletSettingsService)doclets.getServiceType(); }
        if (type == null) {
            return (ServiceType)Lookup.getDefault().lookup(org.netbeans.modules.javadoc.settings.StdDocletSettingsService.class); }
        return type;         }
    /** Setter selected doclet
    */
    public synchronized void setDoclets (ServiceType s) {        
        type = (StdDocletSettingsService)s;
        doclets = new StdDocletSettingsService.Handle(s);
        firePropertyChange("doclets", null, null); } }
