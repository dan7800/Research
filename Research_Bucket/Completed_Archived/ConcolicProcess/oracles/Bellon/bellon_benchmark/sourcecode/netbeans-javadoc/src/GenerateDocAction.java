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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;
import org.openide.util.RequestProcessor;
import org.openide.util.SharedClassObject;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.TopManager;
import org.openide.ServiceType;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.javadoc.settings.JavadocSettingsService;
import org.netbeans.modules.javadoc.settings.StdDocletSettingsService;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
import org.netbeans.modules.javadoc.ExternalJavadocExecutor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.io.*;
import java.lang.reflect.*;
import javax.swing.JFileChooser;
import com.sun.javadoc.*;
import sun.tools.util.ModifierFilter;
import sun.tools.java.ClassDeclaration;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;
import org.openide.NotifyDescriptor;
/** Generate Javadoc - Tools action
 *
 * @author Petr Hrebejk
 */
public class GenerateDocAction extends CookieAction {
    /** We have to hold refwerence on ExecutorTask in order not to los ioTab */
    private static ExecutorTask et = null;
    static final long serialVersionUID =-7617405431087800775L;
    /** Creates and starts a thread for generating documentation
     */
    protected void performAction(Node[] activatedNodes) {        
        DocumentationSettings dss = DocumentationSettings.getDefault();
        JavadocType service = (JavadocType)dss.getExecutor();
        //it is nessesary to block internal javadoc if runnning under jdk1.4
        if(JavadocModule.isDisabledForJDK14().booleanValue()){
            if( !( service instanceof ExternalJavadocType) ){
                NotifyDescriptor nd  = new NotifyDescriptor(ResourceUtils.getBundledString("MSG_NoInternalInJDK14"), 
                                                    ResourceUtils.getBundledString("MSG_NoInternalInJDK14Title"), 
                                                    NotifyDescriptor.YES_NO_OPTION, 
                                                    NotifyDescriptor.WARNING_MESSAGE, null, null);
                TopManager.getDefault().notify(nd);
                if( nd.getValue() == NotifyDescriptor.NO_OPTION )
                    return;
                service = (JavadocType)Lookup.getDefault().lookup(org.netbeans.modules.javadoc.settings.ExternalJavadocSettingsService.class); } }
        StdDocletSettingsService options = (StdDocletSettingsService)service.getDoclets();
        File dir = options.getDirectory();        
        if( dir == null || !dir.isDirectory() ) {
            File directory;
            String fileSep = System.getProperty ("file.separator");   //NOI18N
            try {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile();    }//NOI18N
            catch ( java.io.IOException e ) {
                directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile();    }//NOI18N
            dir = DestinationPanel.showDialog( directory ); }
        else if( dss.getAskBeforeGenerating() ){
            dir = DestinationPanel.showDialog( dir );
            if ( dir != null )  {
                options.setDirectory( dir );
                //save changes
                service.setDoclets(options);
                dss.setExecutor(service); } }
        if ( dir != null )  {
            TopManager.getDefault().setStatusText( ResourceUtils.getBundledString( "MSG_StartingJavadoc" ) );   //NOI18N
            if( service instanceof ExternalJavadocType ){
                //external
                ExternalJavadocExecutor eje = (ExternalJavadocExecutor)((ExternalJavadocType)service).getExternalExecutorEngine();// new ExternalJavadocExecutor();  //this invoke javadoc (now)
                eje.execute(activatedNodes); }
            else {
                //internal
                ExecutionEngine ee = TopManager.getDefault().getExecutionEngine();
                JavadocInvoker ji = new JavadocInvoker( activatedNodes );
                et = ee.execute( "Javadoc generation", ji, JavadocInvoker.getIO() ); //NOI18N
                 } }//RequestProcessor.postRequest( ji );
        else {
            return; } }
    /** Cookie classes contains one class returned by cookie () method.
     */
    protected final Class[] cookieClasses () {
        return new Class[] { DataFolder.class, JavaDataObject.class }; }
    /** If javadoc already running disable the actions.
     * Otherways let decide overriden method
     */
    protected boolean enable( Node[] activatedNodes ) {
        if (JavadocInvoker.isRunning() || ExternalJavadocExecutor.isRunning() )
            return false;
        else
            if(testForSFSFolder(activatedNodes))
                return false;
        return super.enable (activatedNodes); }
    private boolean testForSFSFolder( Node[] activatedNodes ){
        for( int i = 0; i < activatedNodes.length; i++ ){
            DataFolder df = (DataFolder)activatedNodes[i].getCookie(DataFolder.class);
            if( df == null )
                continue;
            try {
                if(df.getPrimaryFile().getFileSystem().isDefault())
                    return true; }
            catch(FileStateInvalidException fsie) {
                continue; } }
        return false; }
    /** Get the requested cookie class.
     * @return the class, e.g. {@link CompilerCookie.Compile}
 */
    protected Class cookie () {
        return DataFolder.class; }
    /** All must be DataFolders or JavaDataObjects
     */
    protected int mode () {
        return MODE_ALL; }
    /* Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GenerateDocAction.class); }
    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return ResourceUtils.getBundledString("CTL_ActionGenerate");    }//NOI18N
    protected String iconResource(){
        return "/org/netbeans/modules/javadoc/resources/generatejavadoc.gif";  } }//NOI18N
/*
 * Log
 */
