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
package org.netbeans.modules.javadoc.settings;
import java.io.File;
import java.util.ResourceBundle;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.javadoc.JavadocType;
import org.openide.util.Lookup;
import org.openide.ErrorManager;
/** Options for invoking internal Javadoc
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class JavadocSettingsService extends JavadocType {
    /** generated Serialized Version UID */
    static final long serialVersionUID =5671560473265010369L;
    /** path to overview file */
    private File overview;
    /** members to show */
    private long members = MemberConstants.PROTECTED;
    /** override standard bootclasspath */
    private String bootclasspath;
    /** extension directories */
    private String extdirs = ""; // NOI18N
    /** generate JDK 1.1 style documentation*/
    private boolean style1_1;
    /** detail messages */
    private boolean verbose;
    /** encoding */
    private String encoding = ""; // NOI18N
    /** locale */
    private String locale = ""; // NOI18N
    /** @return human presentable name */
    public String displayName() {
        return ResourceUtils.getBundledString("CTL_IntJavadoc_settings");  }//NOI18N
    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavadocSettingsService.class); }
    /** Getter for members
    */
    public long getMembers() {
        return members; }
    /** Setter for members
    */
    public void setMembers( long l ) {
        long old = members;
        members = l;
        firePropertyChange("members", new Long(old), new Long(members)); }
    /** Getter for path to overview file.
    */
    public File getOverview () {
        return overview; }
    /** Setter for path to overview file.
    */
    public void setOverview (File s) {
        if( s != null && (!s.exists() || !s.isFile()))
            invalidArgument();
        File old = overview;
        overview = s;
        firePropertyChange("overview", old, overview); }
    /** Getter for bootclasspath
    */
    public String getBootclasspath () {
        return bootclasspath; }
    /** Setter for bootclasspath
    */
    public void setBootclasspath (String s) {
        String old = bootclasspath;
        bootclasspath = s;
        firePropertyChange("bootclasspath", old, bootclasspath); }
    /** Getter for extension directories
    */
    public String getExtdirs () {
        return extdirs; }
    /** Setter extension directories
    */
    public void setExtdirs (String s) {
        String old = extdirs;
        extdirs = s;
        firePropertyChange("extdirs", old, extdirs); }
    /** Getter for JDK 1.1 Style
    */
    public boolean isStyle1_1 () {
        return style1_1; }
    /** Setter for JDK 1.1 Style
    */
    public void setStyle1_1 (boolean b) {
        boolean old = style1_1;
        style1_1 = b;
        firePropertyChange("style1_1", new Boolean(old), new Boolean(style1_1)); }
    /** Getter for verbose mode
    */
    public boolean isVerbose () {
        return verbose; }
    /** Setter for verbose mode
    */
    public void setVerbose (boolean b) {
        boolean old = verbose;
        verbose = b;
        firePropertyChange("verbose", new Boolean(old), new Boolean(verbose)); }
    /** Getter for encoding
    */
    public String getEncoding () {
        return encoding; }
    /** Setter for encoding
    */
    public void setEncoding (String s) {
        String old = encoding;
        encoding = s;
        firePropertyChange("encoding", old, encoding); }
    /** Getter for locale
    */
    public String getLocale () {
        return locale; }
    /** Setter locale
    */
    public void setLocale (String s) {
        String old = locale;
        locale = s;
        firePropertyChange("locale", old, locale); }
    /** helper method for annotating exceptions */
    static void invalidArgument() {
        IllegalArgumentException iae=new IllegalArgumentException("Invalid path or file"); //NOI18N
        ErrorManager errMan=(ErrorManager)Lookup.getDefault().lookup(ErrorManager.class);
        if (errMan!=null) {
            String msg=ResourceUtils.getBundledString("MSG_InvalidPathFile"); //NOI18N
            errMan.annotate(iae, ErrorManager.USER, iae.getMessage(), msg, null, null); }
        throw iae; } }
