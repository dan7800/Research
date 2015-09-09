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
/**
 *
 * @author  Petr Suchomel
 * @version 1.0
 */
public abstract class ExternalJavadocType extends JavadocType {
    private static final long serialVersionUID =-1980235073291097158L;
    /** Getter for max memory
    */
    public abstract int getMaxmemory ();
    /** Setter max memory
    */
    public abstract void setMaxmemory (int s);
    /** Getter for recursive
    */
    public abstract boolean getRecursive ();
    /** Setter for recursive
    */
    public abstract void setRecursive (boolean rec);
    /** Getter for external executor for Javadoc
    */
    public abstract ServiceType getExternalExecutorEngine();
    /** Setter for external executor for Javadoc
    */
    public abstract void setExternalExecutorEngine(ServiceType executor); }
