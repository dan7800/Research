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
import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;
import org.netbeans.modules.javadoc.StdDocletType;
import org.netbeans.modules.javadoc.ExternalJavadocExecutor;
import org.openide.util.Utilities;
/**
 *
 * @author  Petr Suchomel
 * @version 1.0
 */
public class ExternalJavadocSettingsServiceBeanInfo extends SimpleBeanInfo {
     /** @retrun BeanDescriptor for this class */
     public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.javadoc.search.Jdk12SearchType.class);
        descr.setName(ResourceUtils.getBundledString("CTL_ExtJavadoc_settings")); // NOI18N
        descr.setValue ("version", "1.1"); // NOI18N            
        return descr; }
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc;
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("members", ExternalJavadocSettingsService.class),         //0 // NOI18N
                       new PropertyDescriptor("overview", ExternalJavadocSettingsService.class),        // 1 // NOI18N
                       new PropertyDescriptor("extdirs", ExternalJavadocSettingsService.class),         // 2 // NOI18N
                       new PropertyDescriptor("style1_1", ExternalJavadocSettingsService.class),        // 3 // NOI18N
                       new PropertyDescriptor("verbose", ExternalJavadocSettingsService.class),         // 4 // NOI18N
                       new PropertyDescriptor("encoding", ExternalJavadocSettingsService.class),        // 5 // NOI18N
                       new PropertyDescriptor("locale", ExternalJavadocSettingsService.class),          // 6 // NOI18N
                       new PropertyDescriptor("maxmemory", ExternalJavadocSettingsService.class),          // 7 // NOI18N 
                       new PropertyDescriptor("doclets", ExternalJavadocSettingsService.class),          // 8 // NOI18N 
                       new PropertyDescriptor("recursive", ExternalJavadocSettingsService.class),          // 9 // NOI18N 
                       new PropertyDescriptor("externalExecutorEngine", ExternalJavadocSettingsService.class)          // 9 // NOI18N 
            };
            desc[0].setDisplayName(ResourceUtils.getBundledString("PROP_Members"));   //NOI18N
            desc[0].setShortDescription(ResourceUtils.getBundledString("HINT_Members"));   //NOI18N
            desc[0].setPropertyEditorClass(MembersPropertyEditor.class);
            desc[1].setDisplayName(ResourceUtils.getBundledString("PROP_Overview"));   //NOI18N
            desc[1].setShortDescription(ResourceUtils.getBundledString("HINT_Overview"));   //NOI18N
            desc[1].setValue("directories", Boolean.FALSE );  //NOI18N
            desc[1].setValue("files", Boolean.TRUE );  //NOI18N
            desc[2].setDisplayName(ResourceUtils.getBundledString("PROP_Extdirs"));   //NOI18N
            desc[2].setShortDescription(ResourceUtils.getBundledString("HINT_Extdirs"));   //NOI18N
            desc[3].setDisplayName(ResourceUtils.getBundledString("PROP_Style1_1"));   //NOI18N
            desc[3].setShortDescription(ResourceUtils.getBundledString("HINT_Style1_1"));   //NOI18N
            desc[4].setDisplayName(ResourceUtils.getBundledString("PROP_Verbose"));   //NOI18N
            desc[4].setShortDescription(ResourceUtils.getBundledString("HINT_Verbose"));   //NOI18N
            desc[5].setDisplayName(ResourceUtils.getBundledString("PROP_Encoding"));   //NOI18N
            desc[5].setShortDescription(ResourceUtils.getBundledString("HINT_Encoding"));   //NOI18N
            desc[6].setDisplayName(ResourceUtils.getBundledString("PROP_Locale"));   //NOI18N
            desc[6].setShortDescription(ResourceUtils.getBundledString("HINT_Locale"));   //NOI18N
            desc[7].setDisplayName(ResourceUtils.getBundledString("PROP_Maxmem"));   //NOI18N
            desc[7].setShortDescription(ResourceUtils.getBundledString("HINT_Maxmem"));   //NOI18N
            desc[7].setExpert(true);
            desc[8].setDisplayName(ResourceUtils.getBundledString("PROP_Doclets"));   //NOI18N
            desc[8].setShortDescription(ResourceUtils.getBundledString("HINT_Doclets"));   //NOI18N
            desc[8].setValue("superClass", StdDocletType.class);   //NOI18N
            desc[9].setDisplayName(ResourceUtils.getBundledString("PROP_Recursive"));   //NOI18N
            desc[9].setShortDescription(ResourceUtils.getBundledString("HINT_Recursive"));   //NOI18N
            desc[10].setDisplayName(ResourceUtils.getBundledString("PROP_ExecutorEngine"));   //NOI18N
            desc[10].setShortDescription(ResourceUtils.getBundledString("HINT_ExecutorEngine"));    //NOI18N    
            desc[10].setValue ("superClass", ExternalJavadocExecutor.class);   //NOI18N
        } catch (IntrospectionException ex) {            
            ex.printStackTrace ();
            return null; }
        return desc; }
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavaDoc.gif"); // NOI18N
        } else {
                return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } } }// NOI18N
