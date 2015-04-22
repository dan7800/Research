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
/*
 * JavaDocFSSettings.java
 *
 * Created on 22. ?nor 2001, 15:02
 */
package org.netbeans.modules.javadoc.search;
import java.util.HashMap;
import java.util.Iterator;
import org.openide.filesystems.FileSystem;
import org.openide.util.SharedClassObject;
import org.openide.TopManager;
import org.openide.ServiceType;
import org.netbeans.modules.javadoc.settings.*;
import java.beans.PropertyChangeEvent;
/**
 *
 * @author  Petr Suchomel
 * @version 1.1
 * Helper class
 */
public class JavaDocFSSettings extends Object implements java.io.Serializable, java.beans.PropertyChangeListener {
    /** Serialized Version UID */
    static final long serialVersionUID =-1524542458662425748L;
    private String secondRoot;
    private ServiceType.Handle searchEngine;
    private String fsName;
    private static HashMap fsSetting;
    public static JavaDocFSSettings getSettingForFS(FileSystem fs){
        if( fsSetting == null ){
            fsSetting = DocumentationSettings.getDefault().getFileSystemSettings(); }
        String systemName = fs.getSystemName();
        //System.err.println("|" + systemName + "|");
        if( fsSetting.get(systemName) == null || !(fsSetting.get(systemName) instanceof JavaDocFSSettings) ){
            //System.err.println("creating new fs settings");
            fsSetting.put(systemName, new JavaDocFSSettings(systemName));
            //save fs setting ??
            DocumentationSettings.getDefault().setFileSystemSettings(fsSetting); }
        return (JavaDocFSSettings)fsSetting.get(fs.getSystemName()); }
    /** for deseralization */     
    public JavaDocFSSettings(){ }
    public JavaDocFSSettings(String fsName){
        this.fsName = fsName; }
    /** Getter for property searchEngine.
     * @return Value of property searchEngine.
    */
    public ServiceType getSearchEngine() {
        JavadocSearchType type = null;
        if (searchEngine != null) {
            type = (JavadocSearchType)searchEngine.getServiceType(); }
        if (type == null) {
            type = getDefaultJavaDocSearchType();
            type.removePropertyChangeListener(this);
            fsSetting.put(fsName, this);
            DocumentationSettings.getDefault().setFileSystemSettings(fsSetting); }
        type.addPropertyChangeListener(this);
        return type;         }
    /** Setter for property searchEngine.
     * @param searchEngine New value of property searchEngine.
     */
    public void setSearchEngine(ServiceType searchEngine) {
        this.searchEngine = new JavadocSearchType.Handle(searchEngine);        
        searchEngine.removePropertyChangeListener(this);        
        fsSetting.put(fsName, this);
        DocumentationSettings.getDefault().setFileSystemSettings(fsSetting);
        searchEngine.addPropertyChangeListener(this); }
    public JavadocSearchType getSearchTypeEngine(){
        JavadocSearchType jst = (JavadocSearchType)getSearchEngine();
        return jst; }
    public String getSecondRoot(){
        return secondRoot; }
    public void setSecondRoot(String secondRoot){
        this.secondRoot = secondRoot;
        fsSetting.put(fsName, this);
        DocumentationSettings.getDefault().setFileSystemSettings(fsSetting); }
    public static JavadocSearchType getDefaultJavaDocSearchType(){                
        return (JavadocSearchType)DocumentationSettings.getDefault().getSearchEngine(); }
    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *         and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        fsSetting.put(fsName, this);
        DocumentationSettings.getDefault().setFileSystemSettings(fsSetting); } }
