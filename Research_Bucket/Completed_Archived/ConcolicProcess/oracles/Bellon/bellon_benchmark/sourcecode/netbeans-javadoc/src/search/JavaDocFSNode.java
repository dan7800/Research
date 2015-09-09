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
package org.netbeans.modules.javadoc.search;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.HashMap;
import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.loaders.DataFolder;
import org.openide.execution.NbClassPath;
import org.openide.ServiceType;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
/** The basic node for representing features included in BanInfo. It recognizes
* the type of the BiFeature and creates properties according to it.
* @author Petr Hrebejk, Petr Suchomel
*/
class JavaDocFSNode extends DataFolder.FolderNode implements Node.Cookie {
    // private variables ..........................................................................
    private transient FileSystem fileSystem;
    /** FSNode - for customize sheet */
    private JavaDocFSNode.FSNode fsnode;
    /** our data folder */ //is serialized
    private DataFolder df;
    /** message that gives name to the root */
    private static MessageFormat formatRoot;
    /**
    * 
    */    
    public JavaDocFSNode ( DataFolder df, Children children ) throws java.beans.IntrospectionException {
        df.super(children);
        this.df = df;         
        getCookieSet().add( df );  //(this)
        getCookieSet().add( this );
        init(); }
    /** initiates node */
    void init() {
        try {
            this.fileSystem = df.getPrimaryFile ().getFileSystem ();
        } catch (org.openide.filesystems.FileStateInvalidException ex) {
            // hopefully should not happen
            throw new InternalError (); }
        ResourceBundle bundle = org.openide.util.NbBundle.getBundle (JavaDocFSNode.class);
        formatRoot = new MessageFormat (bundle.getString ("dataFolderJavaDocRootName"));           //NOI18N
        initDisplayName ();
         }//createProperties();
    /** Initializes display name.
    */
    void initDisplayName () {
        String s = formatRoot.format (
                       new Object[] {fileSystem.getDisplayName (), fileSystem.getSystemName ()}
                   );
        setDisplayName (s);
        if (fileSystem instanceof JarFileSystem) {
           java.io.File root = ((JarFileSystem) fileSystem).getJarFile ();
           try {
             if (root != null)
                 setShortDescription (root.getCanonicalPath ());
            } catch (java.io.IOException e) {  } } }// ignore error, no hint in such case
    /** Finds an icon for this node. The filesystem's icon is returned.
    * @see java.bean.BeanInfo
    * @see org.openide.filesystems.FileSystem#getIcon
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean
    */
    public Image getIcon (int type) {
        Class klass = fileSystem.getClass();        
        BeanInfo bi = null;
        try {
            bi = org.openide.util.Utilities.getBeanInfo(fileSystem.getClass());
        } catch (IntrospectionException e) {
            return super.getIcon(type); }
        Image icon =  bi.getIcon(type);
        return icon==null ? super.getIcon(type) : icon; }
    /** The DataFolderRoot's opened icon is the same as the closed one.
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
        return getIcon(type); }
    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavaDocNode.class); }
    /** Setter for parent node. Is protected for subclasses. Fires info about
    * change of the parent.
    *
    * @param n new parent node
    */
    /*
    protected void setParentNode (Node n) {
      super.setParentNode (n); }
    */
    /** To have only one FSNode there
    * @return an <code>JavaDocFSNode.FSNode</code> node for accessing customize sheet
    */   
    private JavaDocFSNode.FSNode getFSNode() {
        if(fsnode == null)            
            try {
                fsnode = new JavaDocFSNode.FSNode (fileSystem);
            } catch (java.beans.IntrospectionException ex) { }
        return fsnode;         }
    /** Adds properties from customize sheet and for sorting.
    * @return the property sheet
    */        
    protected Sheet createSheet () {
        Sheet s = super.createSheet ();
        s.remove(Sheet.PROPERTIES);
        Sheet.Set sortSet = s.remove(DataFolder.SET_SORTING);
        Node.PropertySet[] np = getFSNode().getPropertySets();                        
        for(int i=0;i<np.length;i++)
            s.put((Sheet.Set)np[i]);            
        s.put(sortSet);
        createProperties(s);
        return s; }
    public Node.Cookie getCookie( Class type ) {
        return getCookieSet().getCookie( type ); }
    // implementation of Node ..........................................................
    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    SystemAction[] staticActions;
    public SystemAction[] getActions () {
        if (staticActions == null) {
            staticActions = new SystemAction[] {                                
                   SystemAction.get (org.openide.actions.OpenLocalExplorerAction.class),
                   SystemAction.get (org.openide.actions.FindAction.class),
                   null,
                   SystemAction.get (org.openide.actions.FileSystemAction.class),
                   null,
                   //      SystemAction.get (org.openide.actions.CutAction.class),
                   //      SystemAction.get (org.openide.actions.CopyAction.class),
                   SystemAction.get (org.openide.actions.PasteAction.class),
                   null,
                   SystemAction.get (UnmountJavaDocFSAction.class),
                   null,
                   SystemAction.get (org.openide.actions.PropertiesAction.class)
                                  ,
                   null,                                  
                   SystemAction.get (ToolsAction.class)               
                                //null
                                //SystemAction.get (DeleteAction.class),
                                //null,
                                //,
                                //SystemAction.get (PropertiesAction.class),
                            }; }
        return staticActions; }
    /**
    * the feature cannot be removed it can only be disabled from BeanInfo
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return false; }
    /**
    * Deletes breakpoint and removes the node too.
    * Ovverrides destroy() from abstract node.
    */
    public void destroy () throws IOException {
        // remove node
         }// super.destroy ();
    /** It has default action - it is the toggle of value for include to bean info
    * @return <CODE>true</CODE>
    */
    public boolean hasDefaultAction () {
        return true; }
    /* No default action on FS node.
     * @return null
     */
    public org.openide.util.actions.SystemAction getDefaultAction() {
        return null; }
    public JavaDocFSSettings getJavaDocFSSettings(){
        return JavaDocFSSettings.getSettingForFS(fileSystem); }
    /** Returns the fileSystem which this node represents */
    FileSystem getFileSystem() {
        return fileSystem; }
/*    
    public Node.Handle getHandle () {
        return new JavaDocFSHandle(JavaDocFSNode.this); }
    static final class JavaDocFSHandle implements Node.Handle {
        private final static long serialVersionUID = 24234097765186L;
        private DataFolder.FolderNode folder;
        public JavaDocFSHandle (DataFolder.FolderNode folder) {
            this.folder = folder; }
        public Node getNode () {
            return ((DataFolder.FolderNode)folder); } }
*/     
    /** Creates the sheet.
        */
    protected void createProperties(Sheet sheet){//Object bean, BeanInfo info) {
        Sheet.Set ps = Sheet.createExpertSet();//sheet.get(Sheet.EXPERT);
        ps.put( new FSOffsetEditorBase (
                   "secondRoot", //NOI18N
                   String.class,//java.io.File.class,
                   ResourceUtils.getBundledString ("CTL_SEARCH_inner_root" ),   //NOI18N
                   ResourceUtils.getBundledString ("HINT_SEARCH_inner_root" ),   //NOI18N
                   this
               ) {
                   public Object getValue () {                                                      
                       if( getJavaDocFSSettings().getSecondRoot() != null )
                           return getJavaDocFSSettings().getSecondRoot(); 
                       else
                           return "";       }//NOI18N
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {                           
                       try {                            
                           if( val != null && val instanceof DataFolder ){
                                 getJavaDocFSSettings().setSecondRoot( (((DataFolder)val).getPrimaryFile()).toString()); }
                           else if( val != null && val instanceof java.lang.String ){
                                 getJavaDocFSSettings().setSecondRoot( (String)val); }
                           else{
                                getJavaDocFSSettings().setSecondRoot(null); }
                           //fsSetting.setSecondRoot( (( val != null && ((String)val).length() != 0 )?(String)val:null) );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException (); } }
               });
        sheet.put(ps);
        try{
            PropertySupport.ReadWrite p = new PropertySupport.ReadWrite (
                       "searchEngine", //NOI18N
                       ServiceType.class,
                       ResourceUtils.getBundledString ("CTL_SEARCH_search_engine" ),   //NOI18N
                       ResourceUtils.getBundledString ("HINT_SEARCH_search_engine" )   //NOI18N
                   ) {
                        public Object getValue () {
                            return getJavaDocFSSettings().getSearchEngine();
                            //return (JavadocSearchType)((DocumentationSettings)SharedClassObject.findObject(DocumentationSettings.class, true)).getSearchEngine();
                            }//return ( fsSetting.getSearchEngine() != null ) ? fsSetting.getSearchEngine() : "";
                        public void setValue (Object val) throws
                           IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                           try {
                               getJavaDocFSSettings().setSearchEngine( (JavadocSearchType)val );//(( val != null )?(String)val:null) );
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException (); } }
                   };
             p.setValue("superClass", JavadocSearchType.class);         //NOI18N
             ps.put(p);       
             sheet.put(ps);             }
        catch( Exception ex){
            ex.printStackTrace(); }
        setSheet(sheet);            
        /*
         Uncomment for later usage --> shows File System capabilities
        FileSystemCapability cap = ((FileSystem)bean).getCapability ();
        try {
            if (cap != null) {
                BeanInfo bi = Introspector.getBeanInfo (cap.getClass (), FileSystemCapability.class);
                Descriptor d = computeProperties (cap, bi);
                Sheet.Set ss = new Sheet.Set ();
                ss.setName ("Capabilities"); // NOI18N
                ss.setDisplayName ("Capabilities");//Main.getString ("PROP_Capabilities"));
                ss.setShortDescription ("Capabilities");//Main.getString ("HINT_Capabilities"));
                ss.put (d.property);
                ss.put (d.expert);
                getSheet ().put (ss); }
        } catch (IntrospectionException e) {
        }*/ }
    abstract class FSOffsetEditorBase extends PropertySupport.ReadWrite {
        //FileSystem currentFs;
        Node currentFs;
        //firePropertyChange (ExPropertyEditor.PROP_VALUE_VALID, null, Boolean.TRUE);
        FSOffsetEditorBase(String name, Class type,
                              String displayName, String shortDescription, Node currentFs) {
            super(name, type, displayName, shortDescription);
            this.currentFs = currentFs;             }
        public PropertyEditor getPropertyEditor() {
            return new DocFSOffsetEditor(currentFs); } }
    /** A Node for filesystems. Redefines remove() to implement removing of
    * filesystems
    */
    static class FSNode extends BeanNode implements PropertyChangeListener {
        //private static String SUB_ROOT         = "subRoot";
        //private static String SEARCH_ENGINE    = "searchEngine";
        /** The filesystem represented by this node */
        private FileSystem fs;
        /** setting */
        private HashMap setting;
        /** Constructs a new FSNode for specified filesystem.
        * @param system the filesystem for which we are constructint the node
        */
        public FSNode(final FileSystem system) throws IntrospectionException {
            super(system);
            fs = system;
            fs.addPropertyChangeListener (WeakListener.propertyChange (this, fs));
            propertyChange (null); }
        public HelpCtx getHelpCtx () {
            return new HelpCtx (FSNode.class); }
        public void propertyChange (PropertyChangeEvent ev) {
            super.setName (fs.getDisplayName ()); } } }
/*
 * Log
 *
 */
