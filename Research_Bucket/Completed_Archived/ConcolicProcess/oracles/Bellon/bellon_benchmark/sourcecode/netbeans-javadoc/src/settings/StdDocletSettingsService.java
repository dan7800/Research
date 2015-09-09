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
import org.netbeans.modules.javadoc.StdDocletType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.StringArrayEditor;
/** Options for invoking internal Javadoc
*
* @author Petr Hrebejk
*/
public class StdDocletSettingsService extends StdDocletType  {//implements ViewerConstants
    /** generated Serialized Version UID */
    static final long serialVersionUID =8476913303755577009L;
    /** destination directory*/
    private File directory;
    public StdDocletSettingsService() { }
    /** use option */
    private boolean use;
    /** version option */
    private boolean version;
    /** author option */
    private boolean author;
    /** splitindex option */
    private boolean splitindex;
    /** window title option */
    private String windowtitle = ""; // NOI18N
    /** doctitle title option */
    private String doctitle = ""; // NOI18N
    /** header title option */
    private String header = ""; // NOI18N
    /** footer title option */
    private String footer = ""; // NOI18N
    /** bottom title option */
    private String bottom = ""; // NOI18N
    /** link option */
    /* this one only for compability */
    private String link = ""; // NOI18N
    private String[] links = { "" }; // NOI18N
    /** linkoffline option */
    private String[] linkoffline = { "" }; // NOI18N
    /** group option */
    private String[] group = { "" }; // NOI18N
    /** nodeprecated option */
    private boolean nodeprecated;
    /** nodeprecatedlist option */
    private boolean nodeprecatedlist;
    /** notree option */
    private boolean notree;
    /** noindex option */
    private boolean noindex;
    /** nohelp option */
    private boolean nohelp;
    /** nonavbar option */
    private boolean nonavbar;
    /** helpfile option */
    private File helpfile = null;
    /** stylesheetfile option */
    private File stylesheetfile = null;
    /** document charset option */
    private String charset = ""; // NOI18N
    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle( StdDocletSettingsService.class ).getString("CTL_StdDoclet_settings"); }
    public HelpCtx getHelpCtx () {
        return new HelpCtx (StdDocletSettingsService.class); }
    /** Getter for destination directory
    */
    public File getDirectory () {
        if( directory == null ){
            String fileSep = System.getProperty ("file.separator");
            try {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile(); }
            catch ( java.io.IOException e ) {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile(); } }
        return directory; }
    /** Setter for destination directory
    */
    public void setDirectory (File s) {
        if( s != null && (!s.exists() || !s.isDirectory()))
            JavadocSettingsService.invalidArgument();
        if( directory == null ){
            String fileSep = System.getProperty ("file.separator");
            try {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile(); }
            catch ( java.io.IOException e ) {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile(); } }
        File old = directory;
        directory = s;
        firePropertyChange("directory", old, directory); }
    /** Getter for use option
     */
    public boolean isUse () {
        return use; }
    /** Setter for use option
     */
    public void setUse (boolean b) {
        boolean old = use;
        use = b;
        firePropertyChange("use", new Boolean(old), new Boolean(use)); }
    /** Getter for version option
     */
    public boolean isVersion () {
        return version;         }
    /** Setter for version option
     */
    public void setVersion (boolean b) {
        boolean old = version;
        version = b;
        firePropertyChange("version", new Boolean(old), new Boolean(version)); }
    /** Getter for author option
     */
    public boolean isAuthor () {
        return author; }
    /** Setter for autho option
     */
    public void setAuthor (boolean b) {
        boolean old = author;
        author = b;
        firePropertyChange("author", new Boolean(old), new Boolean(author)); }
    /** Getter for splitindex option
     */
    public boolean isSplitindex () {
        return splitindex; }
    /** Setter for splitindex option
     */
    public void setSplitindex (boolean b) {
        boolean old = splitindex;
        splitindex = b;
        firePropertyChange("splitindex", new Boolean(old), new Boolean(splitindex)); }
    /** Getter for windowtitle option
     */
    public String getWindowtitle () {
        return windowtitle; }
    /** Setter for windowtitle option
     */
    public void setWindowtitle (String s) {
        String old = windowtitle;
        windowtitle = s;
        firePropertyChange("windowtitle", old, windowtitle); }
    /** Getter for doctitle option
     */
    public String getDoctitle () {
        return doctitle; }
    /** Setter for doctitle option
     */
    public void setDoctitle (String s) {
        String old = doctitle;
        doctitle = s;
        firePropertyChange("doctitle", old, doctitle); }
    /** Getter for header option
     */
    public String getHeader () {
        return header; }
    /** Setter for header option
     */
    public void setHeader (String s) {
        String old = header;
        header = s;
        firePropertyChange("header", old, header); }
    /** Getter for footer option
     */
    public String getFooter () {
        return footer; }
    /** Setter for footer option
     */
    public void setFooter (String s) {
        String old = footer;
        footer = s;
        firePropertyChange("footer", old, footer); }
    /** Getter for bottom option
     */
    public String getBottom () {
        return bottom; }
    /** Setter for bottom option
     */
    public void setBottom (String s) {
        String old = bottom;
        bottom = s;
        firePropertyChange("bottom", old, bottom); }
    /** Getter for link option
     */
    public String[] getLink () {
        return links; }
    /** Setter for link option
     */
    public void setLink (String[] s) {
        String[] old = links;
        links = s;
        firePropertyChange("link", old, link); }
    /** Getter for linkoffline option
     */
    public String[] getLinkoffline () {
        return linkoffline; }
    /** Setter for linkoffline option
     */
    public void setLinkoffline (String[] s) {
        String[] old = linkoffline;
        linkoffline = s;
        firePropertyChange("linkoffline", old, linkoffline); }
    /** Getter for group option
     */
    public String[] getGroup () {
        return group; }
    /** Setter for group option
     */
    public void setGroup (String[] s) {
        String[] old = group;
        group = s;
        firePropertyChange("group", old, group); }
    /** Getter for nodeprecated option
     */
    public boolean isNodeprecated () {
        return nodeprecated; }
    /** Setter for nodeprecated option
     */
    public void setNodeprecated (boolean b) {
        boolean old = nodeprecated;
        nodeprecated = b;
        firePropertyChange("nodeprecated", new Boolean(old), new Boolean(nodeprecated)); }
    /** Getter for nodeprecatedlist option
     */
    public boolean isNodeprecatedlist () {
        return nodeprecatedlist; }
    /** Setter for nodeprecatedlist option
     */
    public void setNodeprecatedlist (boolean b) {
        boolean old = nodeprecatedlist;
        nodeprecatedlist = b;
        firePropertyChange("nodeprecatedlist", new Boolean(old), new Boolean(nodeprecatedlist)); }
    /** Getter for notree option
     */
    public boolean isNotree () {
        return notree; }
    /** Setter for notree option
     */
    public void setNotree (boolean b) {
        boolean old = notree;
        notree = b;
        firePropertyChange("notree", new Boolean(old), new Boolean(notree)); }
    /** Getter for noindex option
     */
    public boolean isNoindex () {
        return noindex; }
    /** Setter for noindex option
     */
    public void setNoindex (boolean b) {
        boolean old = noindex;
        noindex = b;
        firePropertyChange("noindex", new Boolean(old), new Boolean(noindex)); }
    /** Getter for nohlep option
     */
    public boolean isNohelp () {
        return nohelp; }
    /** Setter for nohelp option
     */
    public void setNohelp (boolean b) {
        boolean old = nohelp;
        nohelp = b;
        firePropertyChange("nohelp", new Boolean(old), new Boolean(nohelp)); }
    /** Getter for nonavbar option
     */
    public boolean isNonavbar () {
        return nonavbar; }
    /** Setter for nonavbar option
     */
    public void setNonavbar (boolean b) {
        boolean old = nonavbar;
        nonavbar = b;
        firePropertyChange("nonavbar", new Boolean(old), new Boolean(nonavbar)); }
    /** Getter for helpfile option
     */
    public File getHelpfile () {
        return helpfile; }
    /** Setter for helpfile option
     */
    public void setHelpfile (File f) {
        if( f != null && (!f.exists() || !f.isFile()))
            JavadocSettingsService.invalidArgument();
        File old = helpfile;
        helpfile = f;
        firePropertyChange("helpfile", old, helpfile); }
    /** Getter for stylesheetfile option
     */
    public File getStylesheetfile () {
        return stylesheetfile; }
    /** Setter for stylesheetfile option
     */
    public void setStylesheetfile (File f) {
        if( f != null && (!f.exists() || !f.isFile()))
            JavadocSettingsService.invalidArgument();
        File old = stylesheetfile;
        stylesheetfile = f;
        firePropertyChange("stylesheetfile", old, stylesheetfile); }
    /** Getter for docencoding option
     */
    public String getCharset () {
        return charset; }
    /** Setter for docencoding option
     */
    public void setCharset (String s) {
        String old = charset;
        charset = s;
        firePropertyChange("charset", old, charset); }
    static class GroupEditor extends StringArrayEditor {
        public String getAsText() {
            return null; } }
    static class LinkEditor extends StringArrayEditor {
        public String getAsText() {
            return null; } } }
