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
import java.text.MessageFormat;
import java.net.URL;
import java.lang.reflect.Field;
import sun.tools.util.ModifierFilter;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.MemberDefinition;
import sun.tools.java.Constants;
import sun.tools.java.ClassPath;
import com.sun.javadoc.RootDoc;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.CoronaEnvironment;
import org.netbeans.modules.java.CoronaClassPath;
import org.netbeans.modules.java.CoronaClassFile;
import org.netbeans.modules.java.ErrConsumer;
import org.netbeans.modules.javadoc.settings.JavadocSettingsService;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
/** This class provides internal access to the Javadoc
 *
 * @author Petr Hrebejk
 */
public class JavadocInvoker implements Runnable {
    static final String oneoneDocletClassName = "com.sun.tools.doclets.oneone.OneOne"; // NOI18N
    static final String standardDocletClassName = "com.sun.tools.doclets.standard.Standard"; // NOI18N
    /** Stop javadoc running more than once
    */
    static private boolean isRunning = false;
    /** Ouput Tab and output streams for Javadoc
    */
    static InputOutput   ioTab = null;
    static OutputWriter  out = null;
    static OutputWriter  err = null;
    /** The environment where all sources will be parsed in.
    */
    protected CoronaEnvironment   ce;
    /** source path and classpath determined by current contens of repository
    */
    private CoronaClassPath srcPath = null;
    private CoronaClassPath binPath = null;
    /** Options for Javadoc run
    */
    protected ArrayList options;
    /** The nodes on which will Javadoc run
    */
    protected Node activatedNodes[];
    /** List of packages and classes Javadoc generates docs for. These list contain only
     * unique classes and packages. Any subpackages or classe contained in packages and
     *  subpackages are removed
     */
    private LinkedList pckgList = new LinkedList();
    private LinkedList clssList = new LinkedList();
    /** List and variables needed by RootDocImpl
    */
    private List userClasses;
    private List userPckgs;
    private ModifierFilter showAccess;
    /** Construtor
    */
    public JavadocInvoker(Node[] activatedNodes) {
        srcPath = new CoronaClassPath(false);
        binPath = new CoronaClassPath(true);
        ce = new CoronaEnvironment ( srcPath, binPath, new myConsumer() );
        options = OptionListProducer.getOptionList();
        /*
        ArrayList adding = new ArrayList();
        adding.add("-sourcepath");
        adding.add("");
        options.add( adding );
        */
        userClasses = new ArrayList();
        userPckgs = new ArrayList();
        showAccess = new ModifierFilter(OptionListProducer.getMembers());
        this.activatedNodes = activatedNodes;
         }//setName( "JavaDocThread" ); // NOI18N
    /** Tests if javadoc generation is in progress
    */
    public static boolean isRunning() {
        return isRunning; }
    /** Parses all the files, creates envronment and RootDocImpl object and then
     * invokes the doclet. 
     */
    void invoke() {
        //System.out.println("1. Allocated:: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory( )) );
        isRunning = true;
        // Remember static Hashtables of sun.tools.compiler
        saveHashtables();
        // Parse all elements
        InheritanceChecksSwitch.turnOffInheritanceChecks();
        ListIterator iterator = clssList.listIterator();
        while( iterator.hasNext() )
            parseJdo( (JavaDataObject)iterator.next() );
        for ( Enumeration e1 = ce.getClasses();  e1.hasMoreElements(); ) {
            ClassDeclaration decl = (ClassDeclaration)e1.nextElement();
            userClasses.add(decl); }
        iterator = pckgList.listIterator();
        while( iterator.hasNext() )
            parseFolder( (DataFolder)iterator.next() );
        // System.out.println("2. Allocated " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );
        TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_Constructing" ) );   //NOI18N
        // Create Env and feed it with fields of CoronaEnvironment
        EnvWrapper envWrapper = new EnvWrapper( (ClassPath)srcPath, (ClassPath)binPath, Constants.F_WARNINGS, "" ); // NOI18N
        envWrapper.copyCoronaEnvironment( ce );
        ce = null;
        System.gc(); // Let the compiler stuff go
        // System.out.println("3. Allocated " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );
        RootDocImplWrapper rdiWrapper = new RootDocImplWrapper(
                                            envWrapper.getEnv(),
                                            userClasses,
                                            userPckgs,
                                            showAccess,
                                            options );
        rdiWrapper.setIO( out, err );
        //BhmDebug.BHM_Memory();
        envWrapper = null;
        // System.out.println("4. Allocated " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );
        TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_RunningDoclet" ) );   //NOI18N
        String docletClassName = OptionListProducer.isStyle1_1() ?
                                 oneoneDocletClassName :
                                 standardDocletClassName;
        //System.out.println (docletClassName);
        NbDocletInvoker dclInvkr = new NbDocletInvoker( docletClassName, null, err );
        dclInvkr.validOptions( rdiWrapper.options( options ) );
        dclInvkr.start( rdiWrapper );
        dclInvkr = null;
        // allows to free the memory after running javadoc
        setStaticField( "com.sun.tools.doclets.Configuration", "root", null); // NOI18N
        setStaticField( "com.sun.tools.doclets.HtmlDocWriter", "configuration", null); // NOI18N
        setStaticField( "com.sun.tools.doclets.standard.HtmlStandardWriter", "configuration", null); // NOI18N
        setStaticField( "com.sun.tools.doclets.standard.HtmlStandardWriter", "currentcd", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.MethodDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.FieldDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ConstructorDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ClassDocImpl", "classMap", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.PackageDocImpl", "packageMap", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ThrowsTagImpl", "runtimeException", null); // NOI18N
        // Here we have to let the compiler stuff go
        restoreHashtables();
        // Resets group setting after running javadoc to empty collections
        setStaticField( "com.sun.tools.doclets.standard.Group", "regExpGroupMap", new HashMap() );   //NOI18N
        setStaticField( "com.sun.tools.doclets.standard.Group", "sortedRegExpList", new ArrayList() );   //NOI18N
        setStaticField( "com.sun.tools.doclets.standard.Group", "groupList", new ArrayList() );   //NOI18N
        setStaticField( "com.sun.tools.doclets.standard.Group", "pkgNameGroupMap", new HashMap() );   //NOI18N
        rdiWrapper = null;
        System.gc(); // All the stuff becomes trash - hopefully
        // System.out.println("4. Allocated " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );
        TopManager.getDefault().setStatusText( "" ); // NOI18N
        InheritanceChecksSwitch.turnOnInheritanceChecks();
        isRunning = false;
        //notify user that Javadoc has been generated
        if( DocumentationSettings.getDefault().getAskAfterGenerating() ){
            NotifyJavadocGenerated.showNotifyDialog( true ); }
        System.gc(); }
    /** Parses one JavaDatObject
     */
    private void parseJdo( JavaDataObject jdo ) {
        try {
            CoronaClassFile ccf = new CoronaClassFile( jdo.getPrimaryFile( ) );
            TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_Parsing" ) + " " + ccf.getName());   //NOI18N
            ce.x_parseFile ( ccf ); }
        catch (java.io.FileNotFoundException e) {
            TopManager.getDefault().notifyException( e ); } }
    /** Parses all java files in folder and it's subfolders
    */
    private void parseFolder(DataFolder folder) {
        boolean isPackage = false;
        DataObject dobj[] = folder.getChildren();
        for( int i = 0; i < dobj.length; i++ )
            if ( dobj[i] instanceof JavaDataObject ) {
                try {
                    CoronaClassFile ccf = new CoronaClassFile( dobj[i].getPrimaryFile( ) );
                    TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_Parsing" ) + " " + ccf.getName());   //NOI18N
                    ce.parseFile ( ccf );
                    if (!isPackage) {
                        isPackage = true;
                        userPckgs.add( folder.getPrimaryFile().getPackageName('.') ); } }
                catch (java.io.FileNotFoundException e) {
                    TopManager.getDefault().notifyException( e ); } }
            else if (dobj[i] instanceof DataFolder) {
                parseFolder( (DataFolder)dobj[i] ); } }
    /** Inner class consuming parser's errors
    */
    class myConsumer implements ErrConsumer {
        public void pushError (FileObject errorFile,
                               int line,
                               int column,
                               String message,
                               String referenceText) {
            err.println ( ResourceUtils.getBundledString( "MSG_Error" ) + " " + line + ":" + column + " " + message );    } }//NOI18N
    /** Adds package to list only if is not subpackage of any othetr and remove any
     * present subpackages of this package
     */
    private void addPackage( DataFolder df ) {
        ListIterator iterator = pckgList.listIterator();
        DataFolder cdf;
        while (iterator.hasNext()) {
            cdf = (DataFolder)iterator.next();
            if ( df.getPrimaryFile().getPackageName('.').startsWith(
                        cdf.getPrimaryFile().getPackageName('.')))
                return;
            else if ( cdf.getPrimaryFile().getPackageName('.').startsWith(
                          df.getPrimaryFile().getPackageName('.')))
                iterator.remove(); }
        pckgList.add( df ); }
    /** Tests if the class is in any package
    */
    private boolean classInPackage( JavaDataObject jdo ) {
        ListIterator iterator = pckgList.listIterator(0);
        while (iterator.hasNext())
            if ( jdo.getPrimaryFile().getPackageName('.').startsWith(
                        ((DataObject)iterator.next()).getPrimaryFile().getPackageName('.')))
                return true;
        return false; }
    /** Makes lists of packages and classes and removes not uniques entries
    */
    private void createLists() {
        DataFolder df;
        JavaDataObject jdo;
        TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_GeneratingList" ) );   //NOI18N
        for( int i = 0; i < activatedNodes.length; ++i )
            if ((df = (DataFolder)activatedNodes[i].getCookie( DataFolder.class )) != null ) {
                addPackage( df ); }
            else if ((jdo = (JavaDataObject)activatedNodes[i].getCookie( JavaDataObject.class )) != null ) {
                clssList.add( jdo ); }
        // Remove all classes contained in packages
        ListIterator iterator = clssList.listIterator();
        while (iterator.hasNext())
            if ( classInPackage( (JavaDataObject)iterator.next()))
                iterator.remove(); }
    /** The run method creates lists of packages and classes to be processed and
     * class invoke().
     */
    public void run() {
        createLists();
        invoke(); }
    /** Gets the Input/Output tab */
    static InputOutput getIO() {
        // if ( ioTab != null ) {
        //System.out.println("IOC " + ioTab.isClosed() ); // NOI18N
        // ioTab.closeInputOutput();
        // }
        if ( ioTab == null ) {
            ioTab = TopManager.getDefault().getIO( ResourceUtils.getBundledString( "CTL_Javadoc_IOTab" ) );   //NOI18N
            ioTab.setErrSeparated (true);
            ioTab.setOutputVisible (true);
            ioTab.setErrVisible (true);
            out = ioTab.getOut ();
            ioTab.setFocusTaken (true);
            err = ioTab.getErr ();
            ioTab.select (); }
        else {
            try {
                out.reset();
                err.reset();
                ioTab.select(); }
            catch (java.io.IOException e) {
                TopManager.getDefault().notifyException( e ); } }
        return ioTab; }
    /** Sets field in class on value
    */
    private static void setStaticField( String className, String fieldName, Object value) {
        try {
            Class clazz = Class.forName( className );
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( clazz, value ); }
        catch (ClassNotFoundException e) { }
        catch (IllegalAccessException e) { }
        catch (NoSuchFieldException e) { } }
    private static Hashtable hashType = null;
    private static Hashtable hashIdentifier = null;
    private static void saveHashtables() {
        try {
            Class.forName( "sun.tools.java.Constants" );   //NOI18N
            Class.forName( "sun.tools.java.Type" );   //NOI18N
            Class.forName( "sun.tools.java.Identifier" );   //NOI18N
            Class.forName( "sun.tools.java.Scanner" );    }//NOI18N
        catch ( ClassNotFoundException e ) {
             }// System.out.println( e );            
        if ( hashType == null ) {
            hashType = saveHashtable( "sun.tools.java.Type", "typeHash" );    }//NOI18N
        if ( hashIdentifier == null ) {
            hashIdentifier = saveHashtable( "sun.tools.java.Identifier", "hash" );    } }//NOI18N
    private static Hashtable saveHashtable( String className, String fieldName ) {
        try {
            Class clazz = Class.forName( className );
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            Hashtable h = (Hashtable)field.get( null );
            if ( h != null ) {
                Hashtable res = new Hashtable( h );
                return res; } }
        catch (ClassNotFoundException e) {
             }// System.out.println( e );
        catch (IllegalAccessException e) {
             }// System.out.println( e );
        catch (NoSuchFieldException e) {
             }// System.out.println( e );
        return null; }
    private static void restoreHashtables() {
        restoreHashtable( "sun.tools.java.Type", "typeHash", hashType );   //NOI18N
        restoreHashtable( "sun.tools.java.Identifier", "hash", hashIdentifier );    }//NOI18N
    private static void restoreHashtable( String className, String fieldName, Hashtable old ) {
        if ( old == null ) {
            return; }
        try {
            Class clazz = Class.forName( className );
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            Hashtable h = (Hashtable)field.get( null );
            if ( h != null ) {
                h.clear();
                h.putAll( old ); } }
        catch (ClassNotFoundException e) {
             }// System.out.println( e );
        catch (IllegalAccessException e) {
             }// System.out.println( e );
        catch (NoSuchFieldException e) {
             } } }// System.out.println( e );
/*
 * Log
 *  17   Gandalf   1.16        2/11/00  Petr Hrebejk    Memory leak in javadoc 
 *       generation fixed
 *  16   Gandalf   1.15        1/13/00  Petr Hrebejk    i18n mk3  
 *  15   Gandalf   1.14        1/12/00  Petr Hrebejk    i18n
 *  14   Gandalf   1.13        1/11/00  Petr Hrebejk    Better handling of ioTab
 *  13   Gandalf   1.12        1/10/00  Petr Hrebejk    Bug 4747 - closing of 
 *       output tab fixed
 *  12   Gandalf   1.11        1/3/00   Petr Hrebejk    Bugfix 4747
 *  11   Gandalf   1.10        11/25/99 Petr Hrebejk    Parser change in Java 
 *       loader module reflected
 *  10   Gandalf   1.9         11/10/99 Petr Hrebejk    Displaying packages.html
 *       instad of index.html for 1.1 style documentation
 *  9    Gandalf   1.8         11/9/99  Petr Hrebejk    Javadoc runs in 
 *       ExecEngine and captures all output
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/15/99  Petr Hrebejk    New status texts + 
 *       localization
 *  6    Gandalf   1.5         6/11/99  Petr Hrebejk    
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    Fixed to compile
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/14/99  Petr Hrebejk    
 *  2    Gandalf   1.1         4/23/99  Ian Formanek    better capitalization of
 *       output window tab
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 
