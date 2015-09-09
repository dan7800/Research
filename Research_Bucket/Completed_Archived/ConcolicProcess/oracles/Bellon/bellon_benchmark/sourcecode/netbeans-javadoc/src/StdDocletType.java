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
public abstract class StdDocletType extends ServiceType {
    /** generated Serialized Version UID */
    static final long serialVersionUID =-7047173485153997969L;
    /** Creates new test */
    public StdDocletType() { }
    public String displayName() {
        return NbBundle.getBundle(StdDocletType.class).getString("CTL_StdDoclet");    }//NOI18N
    /** Getter for destination directory
    */
    public abstract File getDirectory ();
    /** Setter for destination directory
    */
    public abstract void setDirectory (File s);
    /** Getter for use option
     */
    public abstract boolean isUse ();
    /** Setter for use option
     */
    public abstract void setUse (boolean b);
    /** Getter for version option;
     */
    public abstract boolean isVersion ();
    /** Setter for version option
     */
    public abstract void setVersion (boolean b);
    /** Getter for author option
     */
    public abstract boolean isAuthor ();
    /** Setter for autho option
     */
    public abstract void setAuthor (boolean b);
    /** Getter for splitindex option
     */
    public abstract boolean isSplitindex ();
    /** Setter for splitindex option
     */
    public abstract void setSplitindex (boolean b);
    /** Getter for windowtitle option
     */
    public abstract String getWindowtitle ();
    /** Setter for windowtitle option
     */
    public abstract void setWindowtitle (String s);
    /** Getter for doctitle option
     */
    public abstract String getDoctitle ();
    /** Setter for doctitle option
     */
    public abstract void setDoctitle (String s);
    /** Getter for header option
     */
    public abstract String getHeader ();
    /** Setter for header option
     */
    public abstract void setHeader (String s);
    /** Getter for footer option
     */
    public abstract String getFooter ();
    /** Setter for footer option
     */
    public abstract void setFooter (String s);
    /** Getter for bottom option
     */
    public abstract String getBottom ();
    /** Setter for bottom option
     */
    public abstract void setBottom (String s);
    /** Getter for link option
     */
    public abstract String[] getLink ();
    /** Setter for link option
     */
    public abstract void setLinkoffline (String[] s);
    /** Getter for link option
     */
    public abstract String[] getLinkoffline ();
    /** Setter for link option
     */
    public abstract void setLink (String[] s);
    /** Getter for group option
     */
    public abstract String[] getGroup ();
    /** Setter for group option
     */
    public abstract void setGroup (String[] s);
    /** Getter for nodeprecated option
     */
    public abstract boolean isNodeprecated ();
    /** Setter for nodeprecated option
     */
    public abstract void setNodeprecated (boolean b);
    /** Getter for nodeprecatedlist option
     */
    public abstract boolean isNodeprecatedlist ();
    /** Setter for nodeprecatedlist option
     */
    public abstract void setNodeprecatedlist (boolean b);
    /** Getter for notree option
     */
    public abstract boolean isNotree (); 
    /** Setter for notree option
     */
    public abstract void setNotree (boolean b);
    /** Getter for noindex option
     */
    public abstract boolean isNoindex ();
    /** Setter for noindex option
     */
    public abstract void setNoindex (boolean b);
    /** Getter for nohlep option
     */
    public abstract boolean isNohelp ();
    /** Setter for nohelp option
     */
    public abstract void setNohelp (boolean b);
    /** Getter for nonavbar option
     */
    public abstract boolean isNonavbar ();
    /** Setter for nonavbar option
     */
    public abstract void setNonavbar (boolean b);
    /** Getter for helpfile option
     */
    public abstract File getHelpfile ();
    /** Setter for helpfile option
     */
    public abstract void setHelpfile (File f);
    /** Getter for stylesheetfile option
     */
    public abstract File getStylesheetfile ();
    /** Setter for stylesheetfile option
     */
    public abstract void setStylesheetfile (File f);
    /** Getter for docencoding option
     */
    public abstract String getCharset ();
    /** Setter for docencoding option
     */
    public abstract void setCharset (String s); }
