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
import java.util.ArrayList;
import java.util.Enumeration;
import org.openide.TopManager;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileObject;
/** This class represents one file system in repository which was found
 * to be a directory with documentation in formated by standard 1.2 doclet.
 * The static method {@link #getDocFileSystems} returns all such systems in
 * the repository.
 *
 * @author Petr Hrebejk, Petr Suchomel
 */
public class DocFileSystem extends Object {
    FileObject indexFileObject;
    /**
     * Constructor for new DocFileSystem object
     */
    public DocFileSystem( FileObject indexFileObject ) {
        this.indexFileObject = indexFileObject; }
    /** return FO for index-file
     * @return  index-file FO 
     */    
    FileObject getIndexFile( ) {
        return indexFileObject; }
    /**
     * @param df 
     * @return false
     */    
    static boolean isDocFolder( DataFolder df ) {
        return false; }
    /** return default engine and try to find java doc index files for it on it
     * @param fs on which is search for index-files
     * @return returns FileObject for given FileSystem which contains index-files
     */
    public static FileObject getDocFileObject( FileSystem fs ) {
        return JavaDocFSSettings.getDefaultJavaDocSearchType().getDocFileObject( fs , null ); }
    /** returns array of FO containing index-files
     * @return FileObjects which contains index-files
     */    
    static DocFileSystem[] getFolders() {
        ArrayList result = new ArrayList();
        //Enumeration fileSystems = TopManager.getDefault().getRepository().getFileSystems();
        Enumeration fileSystems = FileSystemCapability.DOC.fileSystems();
        while ( fileSystems.hasMoreElements() ) {
            FileSystem fs = (FileSystem)fileSystems.nextElement();
            JavaDocFSSettings setting = JavaDocFSSettings.getSettingForFS( fs );
            //System.out.println(setting.getSearchTypeEngine().getName());
            FileObject fo = setting.getSearchTypeEngine().getDocFileObject( fs , setting.getSecondRoot() );
            //System.out.println(fo);
            //FileObject fo = getDocFileObject( fs );
            if ( fo != null ) {
                result.add( new DocFileSystem( fo ) ); } }
        DocFileSystem[] dsa = new DocFileSystem[result.size()];
        result.toArray( dsa );
        return dsa; } }
/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Petr Hrebejk    i18n
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  5    Gandalf   1.4         7/30/99  Petr Hrebejk    Search uses 
 *       FileSystemCapabilities
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/27/99  Petr Hrebejk    Crtl+F1 documentation 
 *       search form editor added
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 
