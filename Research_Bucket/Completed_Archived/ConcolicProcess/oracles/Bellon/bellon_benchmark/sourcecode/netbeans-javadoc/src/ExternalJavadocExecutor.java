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
import java.io.*;
import java.lang.reflect.*;
import javax.swing.JFileChooser;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.execution.*;
import org.openide.windows.OutputWriter;
import org.openide.windows.InputOutput;
import org.openide.NotifyDescriptor;
import org.openide.compiler.ExternalCompiler;
import org.openide.filesystems.FileSystemCapability;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.javadoc.settings.*;
import org.netbeans.modules.javadoc.NotifyJavadocGenerated;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author  Petr Suchomel
 * @version 1.0
 */
public class ExternalJavadocExecutor extends ProcessExecutor {//implements Runnable{
    /** hold state of javadoc */
    private static boolean running = false;    
/** Desribes format of external javadoc process
 */    
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
    "{" + JavadocFormat.TAG_JDKHOME + "}{" + JavadocFormat.TAG_SEPARATOR + "}bin{" + Format.TAG_SEPARATOR + "}javadoc",    //NOI18N
                " {" + JavadocFormat.TAG_PARAMS + "} -classpath {" + JavadocFormat.TAG_REPOSITORY + "}" +      //NOI18N
                "{" + JavadocFormat.TAG_PATHSEPARATOR + "}" +      //NOI18N
                "{" + JavadocFormat.TAG_CLASSPATH + "}" +      //NOI18N
                "{" + JavadocFormat.TAG_PATHSEPARATOR + "}" + "{" + JavadocFormat.TAG_BOOTCLASSPATH + "}" +    //NOI18N
                "{" + JavadocFormat.TAG_PATHSEPARATOR + "}" + "{" + JavadocFormat.TAG_LIBRARY + "}" +      //NOI18N
                " {" + JavadocFormat.TAG_FILES + "}");       //NOI18N
    /** process genarating javadoc */
    private Process javadocProcess = null;
    /** paraneters for genarating javadoc */
    static private List args;
    /** Desribes files for which javadco will be created
     */    
    static private String files = null;
    /** Packages over wchich will be javadoc generated */
    static java.util.List pckList = null;
    /** Ouput Tab and output streams for Javadoc */
    static OutputWriter  out = null;
    /** Err stream
     */    
    static OutputWriter  err = null;
    /** Reference to output window
     */    
    static InputOutput   ioTab = null;
    /** Constructor for ExtrnalJavadocExecutor
     */    
    public ExternalJavadocExecutor() {
        setExternalExecutor(DEFAULT); }
     /** Starts parser action
         * @param activatedNodes arraz of activated nodes
     */    
    public void execute(Node[] activatedNodes) {
        if( isRunning() )
            return;
        //set it is running        
        setRunning(true);
        //save all files
        TopManager.getDefault().saveAll();
        files = createLists(activatedNodes);
        //no files in list, terminate
        if( files == null ){
            TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_NoFilesInList" ) );    //NOI18N
            setRunning(false);
            return; }
        //create parameters
        args = new ArrayList();        
        args.addAll(ExternalOptionListProducer.getOptionList(activatedNodes));                
        //for debuging only
        if(Boolean.getBoolean("netbeans.debug.javadoc")){      //NOI18N
            JavadocFormat jf = new JavadocFormat(createParametersLine(), files);
            System.out.println(JavadocFormat.format( DEFAULT.getProcessName(), jf.getMap()) + " "+ JavadocFormat.format( DEFAULT.getArguments(), jf.getMap() )); }//NOI18N
        //MapFormat.format( new JavadocFormat(createParametersLine(), files).
        //open I/O window
        ioTab = getIO();
        out = ioTab.getOut ();
        err = ioTab.getErr ();
        boolean success = true;
        ExternalJavadocOutputParser info = null;
        ExternalJavadocOutputParser error = null;
        try {                        
            TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_GeneratingJavadoc" ) );    //NOI18N
            javadocProcess = createProcess(new ExecInfo("ExternalJavadocExecutor"));       //NOI18N
            info = new ExternalJavadocOutputParser( javadocProcess.getInputStream(), out );
            info.start();
            error = new ExternalJavadocOutputParser( javadocProcess.getErrorStream(), err );
            error.start();
            //synchronized( javadocProcess ) {            
                javadocProcess.waitFor();   //waits for the end of external task
             }//}
        catch(Exception ex)  {
            TopManager.getDefault().notifyException(ex);
            success = false; }
        finally {
            while( info.isAlive() || error.isAlive() )
            ;
            setRunning(false); }
        if( DocumentationSettings.getDefault().getAskAfterGenerating() ){
            if( success ){
                //notify user that Javadoc has been generated
                if( !info.isFoundError() )
                    NotifyJavadocGenerated.showNotifyDialog( false ); } }
        if( info.isFoundError() || !success )
            TopManager.getDefault().notify( new NotifyDescriptor.Message( (Object)ResourceUtils.getBundledString("MSG_Error_When_Generating"), org.openide.NotifyDescriptor.ERROR_MESSAGE) );      //NOI18N
        TopManager.getDefault().setStatusText( ResourceUtils.getBundledString("CTL_Javadoc_Finished") );       //NOI18N
        javadocProcess = null; }
/** Used to indicate running javadoc
 * @return true if javadoc is running
 */    
    public static boolean isRunning() {
        return running; }
/** Used to set flag that javadoc is running
 * @param run true indicates that javadoc is running
 */    
    public static void setRunning(boolean run) {
        running = run; }
/** Stop execute task of javadoc
 */    
    public void stopJavadoc() {
        if( running ) {
            javadocProcess.destroy(); } }
    /** Gets the Input/Output tab
     * @return IO for output window
 */
    static InputOutput getIO() {
        if ( ioTab == null ) {
            ioTab = TopManager.getDefault().getIO( ResourceUtils.getBundledString( "CTL_Javadoc_IOTab" ) );    //NOI18N
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
/** Used to create new javadoc process (external)
 * @param info not used
 * @throws IOException if occured problem creating new process
 * @return Process (process of javadoc )
 */    
    protected Process createProcess(ExecInfo info) throws IOException {
        return getExternalExecutor().exec( new JavadocFormat(createParametersLine(), files)); }
/** Creates part of command line (parameters)
 * @return string of parameters line
 */    
    private static String createParametersLine() {
        StringBuffer params = new StringBuffer();
        Iterator iter = args.iterator();
        while( iter.hasNext() ) {
            params.append( iter.next().toString() );
            params.append(" ");  }//NOI18N
        return params.toString(); }
    /** Makes lists of java source
     * @param activatedNodes activated nodes
     * @return String string representing the files to compile or null if it
     * cannot be created
 */
    public static String createLists(org.openide.nodes.Node[] activatedNodes) {
        pckList = new java.util.ArrayList();                
        List list = new ArrayList();    //files for to generate javadoc
        DataFolder df;
        JavaDataObject jdo;
        //load javadoc setting (need for recursive parameter)
        ExternalJavadocSettingsService javadocS = (ExternalJavadocSettingsService)TopManager.getDefault ().getServices ().find (ExternalJavadocSettingsService.class);
        for( int i = 0; i < activatedNodes.length; ++i ) {                
            if ((df = (DataFolder)activatedNodes[i].getCookie( DataFolder.class )) != null ) {
                if( !isAvailableFile(df.getPrimaryFile()) )
                    continue;
                String pck = df.getPrimaryFile().getPackageName('.');                                                
                if( existsJdoFilesInFolder(df) && !pckList.contains(pck)){    //add package only if there exist any java file (HTML !!)
                    pckList.add(pck);
                    list.add(pck); }
                if( javadocS == null || javadocS.getRecursive () )
                    list.addAll(parseFolders(df)); }
            else if ((jdo = (JavaDataObject)activatedNodes[i].getCookie( JavaDataObject.class )) != null ) {
                Set filesSet = jdo.files();
                Iterator iterator = filesSet.iterator();
                while(iterator.hasNext()) {
                    FileObject fo = (FileObject)iterator.next();
                    if( !isAvailableFile( fo ) )
                        continue;
                    String pck = getFileObjectPackage(fo);
                    if(fo.hasExt("java") && !pckList.contains(pck)){//NOI18N
                        try {
                            list.add(FileUtil.toFile (fo).toString ()); }
                        catch(Exception exEx){
                             } } } } }//the exception is raised if it is f.e. jar file system
        if( list.size() != 0 ) {
            return asParameterString((String[])list.toArray(new String[1])); }
        else {
            return null; } }
/** Recusively parse the folders
 * @param df Data folder
 * @return list of files or packages found in the folder
 */    
    private static List parseFolders(DataFolder df){
        List list = new ArrayList();    //files for to generate javadoc
        DataObject[] objects = df.getChildren();
        for( int i = 0; i < objects.length; i++ ){                        
            if( objects[i] instanceof JavaDataObject ){
                JavaDataObject jdo = (JavaDataObject)objects[i];
                Set filesSet = jdo.files();
                Iterator iterator = filesSet.iterator();
                while(iterator.hasNext()) {
                    FileObject fo = (FileObject)iterator.next();                    
                    if( !isAvailableFile( fo ) )
                        continue;
                    String pck = getFileObjectPackage(fo);                    
                    if(fo.hasExt("java") && !pckList.contains(pck)){//NOI18N
                        try {
                            list.add(FileUtil.toFile (fo).toString ()); }
                        catch(Exception exEx){
                             } } } }//the exception is raised if it is f.e. jar file system
            else if ( objects[i] instanceof DataFolder ){
                String pck = ((DataFolder)objects[i]).getPrimaryFile().getPackageName('.');
                if( !isAvailableFile(df.getPrimaryFile()) )
                    continue;
                if( !pckList.contains(pck)){
                    pckList.add(pck);
                    if( existsJdoFilesInFolder(((DataFolder)objects[i])) ){    //add package only if there exist any java file (HTML !!)                        
                        list.add(pck); } }
                list.addAll(parseFolders((DataFolder)objects[i])); } }
        return list;     }
/** Used to test, if one or more java files is in folder
 * @param df Data folder to be tested
 * @return true, if any java files found in DataFolder
 */    
    private static boolean existsJdoFilesInFolder(DataFolder df){
        DataObject dataob[] = df.getChildren();
        for( int i = 0; i < dataob.length; i++ ){
            if( dataob[i] instanceof JavaDataObject ){
                JavaDataObject jdo = (JavaDataObject)dataob[i];
                Set filesSet = jdo.files();
                Iterator iterator = filesSet.iterator();
                while(iterator.hasNext()) {
                    //HTML ????!!!!                    
                    if(((FileObject)iterator.next()).hasExt("java") ){//NOI18N
                        return true; } } } }
        return false; }
    private static boolean isAvailableFile(FileObject fo){
        try{                
            //if I try to get FileUtil.toFile from jar or zip, I get the exception
            FileUtil.toFile( fo ).toString(); }
        catch(Exception exEx){
            return false; }
        return true; }
/** Gets "like" java file package name
 * @param fo FileObject on which package has to be found
 * @return package
 */    
    private static String getFileObjectPackage(FileObject fo){
        int dot;
        String pck = fo.getPackageName('.');
        if( ( dot = pck.lastIndexOf('.')) != -1 ) {
            pck = pck.substring(0, dot); }
        return pck;  }
    /** Helper method to allows conversion of list of files to compile to
    * one string that can be passed as parameter to external process.
    * On non Windows machines the method simply concatenates the strings
    * into one. On Windows, if the file count it greater then ten, it
    * creates temporary file, writes the strings into it and returns
    * "@filename" witch is accepted by common programmers instead of the
    * list of files.
    *
    * @param files array of files to compile
    * @return the string representing the files to compile or null if it
    *   cannot be created (like the temporary file cannot be created)
    */
    private static String asParameterString (String[] files) {
        if (files.length > 10 && Utilities.isWindows()) {
            File f = constructFile(files);
            if (f == null) return null;
            return "@" + f; // NOI18N
        } else {
            return constructString(files); } }
    /** prefix for a tmp file */
    private static final String PREFIX = "javadocparams"; // NOI18N
    /** suffix for a tmp file */
    private static final String SUFFIX = "pms";   //NOI18N
    private static final long serialVersionUID =4377377355485697603L;
 // NOI18N
    /** Create temporary file describing which files will be parsed by javadoc
     * @return File containing all files to compile.
     * @param files array of files to be parsed
 */
    private static File constructFile(String[] files) {
        try {
            File f = File.createTempFile(PREFIX, SUFFIX);
            f.deleteOnExit();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));
            Iterator iter = Arrays.asList (files).iterator();
            while (iter.hasNext()) {
                pw.println((String) iter.next()); }
            pw.close();
            return f;
        } catch (IOException e) {
            return null; } }
    /**
     * Creates String containing all files to compile.
     * @return String containing all files to compile.
     * @param files array of files to be parsed
 */
    private static String constructString(String[] files) {
        StringBuffer sb = new StringBuffer ();
        String add = ""; // NOI18N
        for (int i = 0; i < files.length; i++) {
            sb.append (add);
            if (files[i].indexOf(' ') >= 0) {
                sb.append("\""); // NOI18N
                sb.append(files[i]);
                sb.append("\""); // NOI18N
            } else {
                sb.append (files[i]); }
            add = " ";  }// NOI18N
        return sb.toString (); }
    static InputOutput getIODup() {
        // if ( ioTab != null ) {
        //System.out.println("IOC " + ioTab.isClosed() ); // NOI18N
        // ioTab.closeInputOutput();
        // }
        if ( ioTab == null ) {
            ioTab = TopManager.getDefault().getIO( ResourceUtils.getBundledString( "CTL_Javadoc_IOTab" ) );   //NOI18N
            ioTab.setErrSeparated (false);
            ioTab.setOutputVisible (true);
            ioTab.setErrVisible (true);
            ioTab.setFocusTaken (true);
            ioTab.select (); }
        else {
            try {
                out.reset();
                err.reset();
                ioTab.select(); }
            catch (java.io.IOException e) {
		java.lang.System.out.println("IOException");
                TopManager.getDefault().notifyException( e ); } }
        return ioTab; }
/** Format of javadoc for NbProcessDesriptor
 */    
    private static class JavadocFormat extends MapFormat {
        /** Tag replaced with ProcessExecutors.getClassPath () */
        public static final String TAG_CLASSPATH = ProcessExecutor.Format.TAG_CLASSPATH;
        /** Tag replaced with ProcessExecutors.getBootClassPath () */
        public static final String TAG_BOOTCLASSPATH = ProcessExecutor.Format.TAG_BOOTCLASSPATH;
        /** Tag replaced with ProcessExecutors.getRepositoryPath () */
        public static final String TAG_REPOSITORY = ProcessExecutor.Format.TAG_REPOSITORY;
        /** Tag replaced with ProcessExecutors.getLibraryPath () */
        public static final String TAG_LIBRARY = ProcessExecutor.Format.TAG_LIBRARY;
        /** Tag replaced with file list */
        public static final String TAG_FILES = "files"; // NOI18N
        /** Tag replaced with arguments of the program */
        public static final String TAG_PARAMS = "params"; // NOI18N
        /** Tag replaced with install directory of JDK */
        public static final String TAG_JDKHOME = ProcessExecutor.Format.TAG_JDKHOME;
        /** Tag replaced with separator between filename components */
        public static final String TAG_SEPARATOR = ProcessExecutor.Format.TAG_SEPARATOR;
        /** Tag replaced with separator between path components */
        public static final String TAG_PATHSEPARATOR = ProcessExecutor.Format.TAG_PATHSEPARATOR;
        private java.util.Map map;
        private static final long serialVersionUID =7560001740739774352L;
/** Format for javadoc command line
 * @param params list of javadoc parameters
 * @param files list of files on which javadoc will be generated
 */        
        public JavadocFormat(String params, String files) {
            super( new java.util.HashMap(9));
            map = getMap ();
            map.put (TAG_CLASSPATH, NbClassPath.createClassPath().getClassPath());
            map.put (TAG_BOOTCLASSPATH, NbClassPath.createBootClassPath().getClassPath());
            map.put (TAG_REPOSITORY, NbClassPath.createRepositoryPath ().getClassPath());
            map.put (TAG_LIBRARY, NbClassPath.createLibraryPath ().getClassPath());
            map.put (TAG_JDKHOME, System.getProperty ("jdk.home"));   //NOI18N
            map.put (TAG_SEPARATOR, File.separator);
            map.put (TAG_PATHSEPARATOR, File.pathSeparator);
            map.put (TAG_PARAMS, params);
            map.put (TAG_FILES, files); } } }
