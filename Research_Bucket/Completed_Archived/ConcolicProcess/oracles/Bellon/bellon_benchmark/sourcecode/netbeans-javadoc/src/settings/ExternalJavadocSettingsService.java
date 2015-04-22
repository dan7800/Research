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
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.javadoc.ExternalJavadocType;
import org.openide.execution.*;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.javadoc.settings.MemberConstants;
import org.openide.TopManager;
import org.openide.ServiceType;
import org.netbeans.modules.javadoc.ExternalJavadocExecutor;
import org.openide.util.Lookup;
/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 */
public class ExternalJavadocSettingsService extends ExternalJavadocType {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4753873543243545L;
    /** path to overview file */
    private File overview = null;
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
    /** max mamory */
    private  int maxmem = 96;
    /** recursive */
    private  boolean recursive = true;
    /** holds executor settings */
    private ExternalJavadocExecutor.Handle executor = null;
    public ExternalJavadocSettingsService(){
        //create doclet
         }//getDoclets();
    /** @return human presentable name */
    public String displayName() {
        return ResourceUtils.getBundledString("CTL_ExtJavadoc_settings");   }// NOI18N
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ExternalJavadocSettingsService.class); }
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
            JavadocSettingsService.invalidArgument();
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
    /** Getter for max memory
    */
    public int getMaxmemory () {
        return maxmem; }
    /** Setter max memory
    */
    public synchronized void setMaxmemory (int s) {
        int old = maxmem;
        maxmem = s;
        firePropertyChange("maxmemory", new Integer(old), new Integer(maxmem)); }
    /** Getter for recursive
    */
    public boolean getRecursive (){
        return recursive; }
    /** Setter max recursive
    */
    public void setRecursive (boolean rec){
        boolean old = recursive;
        recursive = rec;
        firePropertyChange("recursive", new Boolean(old), new Boolean(recursive)); }
    public ServiceType getExternalExecutorEngine() {
        ExternalJavadocExecutor service = null;
        if (executor != null) {
            service = (ExternalJavadocExecutor)executor.getServiceType(); }
        if (service == null) {
            return (ServiceType)Lookup.getDefault().lookup(org.netbeans.modules.javadoc.ExternalJavadocExecutor.class); }
        return service;         }
    public void setExternalExecutorEngine(ServiceType executor){
        this.executor = new ExternalJavadocExecutor.Handle(executor);
        firePropertyChange("externalExecutorEngine", null, null); } }
