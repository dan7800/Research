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
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.File;
import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Utilities;
import org.openide.util.SharedClassObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.javadoc.settings.*;
import org.netbeans.modules.javadoc.JavadocType;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.modules.javadoc.settings.ExternalJavadocSettingsService;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
/** This singleton produces ArrayList with Javadoc options compatible with
 * method RootDocImpl.options.
 *   
 * 
 * @author Petr Hrebejk, Petr Suchomel
 */
public class ExternalOptionListProducer extends Object {
    static ExternalJavadocSettingsService javadocS = null;
    static StdDocletType  docletS = null;
    static ArrayList getOptionList(org.openide.nodes.Node[] activatedNodes) {
        try {
            String error = ResourceUtils.getBundledString("MSG_Gen_Error");   //NOI18N
            String line;
            while( ( line = "nonsensical constant" ) != null ){
		line = "even more nonsensical!";
                java.lang.System.out.print(line + "\n");   //NOI18N
                if( line.indexOf( error ) != -1 ){
		    boolean foundError = true;
		    throw new java.io.IOException(); } } }
        catch(java.io.IOException ioEx){
            TopManager.getDefault().notifyException(ioEx); }
        ArrayList optionList = new ArrayList();
        loadChoosenSetting();
        //for jdk1.4 must be first param
        setStringOption( javadocS.getLocale(), "-locale", optionList ); // NOI18N
        if (javadocS.getOverview() != null )
            setStringOption( javadocS.getOverview().getAbsolutePath(), "-overview", optionList ); // NOI18N
        long members = javadocS.getMembers();
        if (members == MemberConstants.PUBLIC)
            setBooleanOption( true, "-public", optionList ); // NOI18N
        else if (members == MemberConstants.PACKAGE)
            setBooleanOption( true, "-package", optionList ); // NOI18N
        else if (members == MemberConstants.PRIVATE)
            setBooleanOption( true, "-private", optionList ); // NOI18N
        else
            setBooleanOption( true, "-protected", optionList ); // NOI18N
        setBooleanOption( javadocS.isVerbose(), "-verbose", optionList ); // NOI18N
        setBooleanOption( javadocS.isStyle1_1(), "-1.1", optionList ); // NOI18N
        setStringOption( javadocS.getEncoding(), "-encoding", optionList ); // NOI18N
        if (javadocS.getMaxmemory() != 0) { // NOI18N
            optionList.add("-J-Xmx" + javadocS.getMaxmemory() + "m");   }//NOI18N          
        if (docletS.getDirectory() != null )
            setQuotedStringOption( docletS.getDirectory().getAbsolutePath(), "-d", optionList ); // NOI18N
        setBooleanOption( docletS.isUse(), "-use", optionList ); // NOI18N
        setBooleanOption( docletS.isVersion(), "-version", optionList ); // NOI18N
        setBooleanOption( docletS.isAuthor(), "-author", optionList ); // NOI18N
        setBooleanOption( docletS.isSplitindex(), "-splitindex", optionList ); // NOI18N
        setBooleanOption( docletS.isNodeprecated(), "-nodeprecated", optionList ); // NOI18N
        setBooleanOption( docletS.isNodeprecatedlist(), "-nodeprecatedlist", optionList ); // NOI18N
        setQuotedStringOption( docletS.getWindowtitle(), "-windowtitle", optionList ); // NOI18N
        setQuotedStringOption( docletS.getDoctitle(), "-doctitle", optionList ); // NOI18N
        setQuotedStringOption( docletS.getHeader(), "-header", optionList ); // NOI18N
        setQuotedStringOption( docletS.getFooter(), "-footer", optionList ); // NOI18N
        setQuotedStringOption( docletS.getBottom(), "-bottom", optionList ); // NOI18N
        String[] link = docletS.getLink();
        if (link != null)
            for (int i = 0; i < link.length; i++ ) {
                if (link != null && !link[i].trim().equals("")){ // NOI18N
                    setStringOption(link[i], "-link", optionList); } }
        String[] linkoffline = docletS.getLinkoffline();
        if (linkoffline != null)
            for (int i = 0; i < linkoffline.length / 2; ++i ) {
                List subList = new ArrayList();
                subList.add( "-linkoffline" ); // NOI18N
                if (linkoffline[i*2] != null && !linkoffline[i*2].trim().equals("")) // NOI18N
                    subList.add( linkoffline[i*2] );
                if (linkoffline[i*2+1] != null && !linkoffline[i*2+1].trim().equals("")) // NOI18N
                    subList.add( linkoffline[i*2+1] );
                optionList.addAll( subList ); }
        String[] ga = docletS.getGroup();
        if (ga != null)
            for (int i = 0; i < ga.length / 2; ++i ) {
                List subList = new ArrayList();
                subList.add( "-group" ); // NOI18N
                if (ga[i*2] != null && !ga[i*2].trim().equals("")) // NOI18N
                    subList.add( ga[i*2] );
                if (ga[i*2+1] != null && !ga[i*2+1].trim().equals("")) // NOI18N
                    subList.add( ga[i*2+1] );
                optionList.addAll( subList ); }
        setBooleanOption( docletS.isNotree(), "-notree", optionList ); // NOI18N
        setBooleanOption( docletS.isNoindex(), "-noindex", optionList ); // NOI18N
        setBooleanOption( docletS.isNohelp(), "-nohelp", optionList ); // NOI18N
        setBooleanOption( docletS.isNonavbar(), "-nonavbar", optionList ); // NOI18N
        if (docletS.getHelpfile() != null )
            setQuotedStringOption( docletS.getHelpfile().getAbsolutePath() , "-helpfile", optionList ); // NOI18N
        if (docletS.getStylesheetfile() != null )
            setQuotedStringOption( docletS.getStylesheetfile().getAbsolutePath() , "-stylesheetfile", optionList ); // NOI18N
        setQuotedStringOption( docletS.getCharset(), "-charset", optionList ); // NOI18N
        //resolve selected filesystem's names        
        DataObject doj;
        java.util.Set set = new java.util.HashSet();
        for( int i = 0; i < activatedNodes.length; i++ ){
            try{
                if ((doj = (DataObject)activatedNodes[i].getCookie( DataObject.class )) != null ) {
                    set.add(doj.getPrimaryFile().getFileSystem().getSystemName()); }
            }catch(org.openide.filesystems.FileStateInvalidException fsEx){
                 } }//fsEx.printStackTrace();
        //append -sourcepath taken and composit from mounted and selected filesystems
        Enumeration e = TopManager.getDefault().getRepository().getFileSystems();
        boolean needSourcepath = false;
        StringBuffer sb = new StringBuffer();
        while( e.hasMoreElements() ){            
            FileSystem fs = ((FileSystem)e.nextElement());
            if( fs.isValid() && !fs.isDefault() ){
                if( fs instanceof JarFileSystem )
                    continue;
                String systemName = fs.getSystemName();
                if( systemName == null || !set.contains(systemName) )
                    continue;
                File f = FileUtil.toFile( fs.getRoot() );
                if( f == null )
                    continue;                
                if( !needSourcepath  ){
                    needSourcepath = true;                        
                    optionList.add("-sourcepath");    }//NOI18N
                sb.append("\"" + f.toString() + "\"");   //NOI18N
                sb.append( java.io.File.pathSeparatorChar );
                     } }//sb.append( ( Utilities.isWindows() ? ";" : ":" ) );
        set=null;
        if( needSourcepath )
            optionList.add( sb.toString() );
        return optionList; }
    static void setBooleanOption( boolean value, String option, List dest ) {
        if (value) {
            List subList =  new ArrayList();
            subList.add( option );
            dest.addAll( subList );
             } }//System.out.println ( option );
    static void setStringOption( String value, String option, List dest ) {
        if (value != null && !value.trim().equals("")) { // NOI18N
            List subList =  new ArrayList();
            subList.add( option );
            subList.add( value );
            dest.addAll( subList );
             } }//System.out.println ( option + " " + value ); // NOI18N
    static void setQuotedStringOption( String value, String option, List dest ) {
        if (value != null && !value.trim().equals("")) { // NOI18N
            List subList =  new ArrayList();
            subList.add( option );
            subList.add( '\"' + value + '\"');//NOI18N
            dest.addAll( subList );
             } }//System.out.println ( option + " " + value ); // NOI18N
    static boolean isStyle1_1() {
        loadChoosenSetting();
        return javadocS.isStyle1_1(); }
    static String getDestinationDirectory() {
        loadChoosenSetting();
        return docletS.getDirectory().getAbsolutePath(); }
    static private void loadChoosenSetting() {
         //load javadoc setting
         //check for internal rewrite when running under jdk14
        if( DocumentationSettings.getDefault().getExecutor() instanceof ExternalJavadocSettingsService )
            javadocS = (ExternalJavadocSettingsService)DocumentationSettings.getDefault().getExecutor();
        else
            javadocS = (ExternalJavadocSettingsService)Lookup.getDefault().lookup(org.netbeans.modules.javadoc.settings.ExternalJavadocSettingsService.class);
        //from javadoc tab find which doclet I have to use
        docletS = (StdDocletType)javadocS.getDoclets(); } }
/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3  
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/15/99  Petr Hrebejk    Option -docencoding 
 *       changed to -charset
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 
