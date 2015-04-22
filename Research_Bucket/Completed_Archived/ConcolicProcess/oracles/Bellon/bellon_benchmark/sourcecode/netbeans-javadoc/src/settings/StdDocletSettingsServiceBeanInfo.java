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
import org.openide.util.Utilities;
/** BeanInfo for standard doclet settings
*
* @author Petr Hrebejk
*/
public class StdDocletSettingsServiceBeanInfo extends SimpleBeanInfo {
     /** @retrun BeanDescriptor for this class */
    public BeanDescriptor getBeanDescriptor () {        
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.javadoc.search.Jdk12SearchType.class);
        descr.setName(ResourceUtils.getBundledString("PROP_DocletService")); // NOI18N
        descr.setValue ("version", "1.1"); // NOI18N            
        return descr; }
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc;
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("directory", StdDocletSettingsService.class),       // 0 // NOI18N
                       new PropertyDescriptor("use", StdDocletSettingsService.class),             // 1 // NOI18N
                       new PropertyDescriptor("version", StdDocletSettingsService.class),         // 2 // NOI18N
                       new PropertyDescriptor("author", StdDocletSettingsService.class),          // 3 // NOI18N
                       new PropertyDescriptor("splitindex", StdDocletSettingsService.class),      // 4 // NOI18N
                       new PropertyDescriptor("windowtitle", StdDocletSettingsService.class),     // 5 // NOI18N
                       new PropertyDescriptor("doctitle", StdDocletSettingsService.class),        // 6 // NOI18N
                       new PropertyDescriptor("header", StdDocletSettingsService.class),          // 7 // NOI18N
                       new PropertyDescriptor("footer", StdDocletSettingsService.class),          // 8 // NOI18N
                       new PropertyDescriptor("bottom", StdDocletSettingsService.class),          // 9 // NOI18N
                       new PropertyDescriptor("link", StdDocletSettingsService.class),            // 10 // NOI18N
                       new PropertyDescriptor("linkoffline", StdDocletSettingsService.class),     // 11 // NOI18N
                       new PropertyDescriptor("group", StdDocletSettingsService.class),           // 12 // NOI18N
                       new PropertyDescriptor("nodeprecated", StdDocletSettingsService.class),    // 13 // NOI18N
                       new PropertyDescriptor("nodeprecatedlist", StdDocletSettingsService.class),// 14 // NOI18N
                       new PropertyDescriptor("notree", StdDocletSettingsService.class),          // 15 // NOI18N
                       new PropertyDescriptor("noindex", StdDocletSettingsService.class),         // 16 // NOI18N
                       new PropertyDescriptor("nohelp", StdDocletSettingsService.class),          // 17 // NOI18N
                       new PropertyDescriptor("nonavbar", StdDocletSettingsService.class),        // 18 // NOI18N
                       new PropertyDescriptor("helpfile", StdDocletSettingsService.class),        // 19 // NOI18N
                       new PropertyDescriptor("stylesheetfile", StdDocletSettingsService.class),  // 20 // NOI18N
                       new PropertyDescriptor("charset", StdDocletSettingsService.class)         // 21 // NOI18N
                   };
            desc[0].setDisplayName(ResourceUtils.getBundledString("PROP_Directory"));   //NOI18N
            desc[0].setShortDescription(ResourceUtils.getBundledString("HINT_Directory"));   //NOI18N
            desc[0].setValue("directories", Boolean.TRUE );
            desc[0].setValue("files", Boolean.FALSE );
            desc[1].setDisplayName(ResourceUtils.getBundledString("PROP_Use"));   //NOI18N
            desc[1].setShortDescription(ResourceUtils.getBundledString("HINT_Use"));   //NOI18N
            desc[2].setDisplayName(ResourceUtils.getBundledString("PROP_Version"));   //NOI18N
            desc[2].setShortDescription(ResourceUtils.getBundledString("HINT_Version"));   //NOI18N
            desc[3].setDisplayName(ResourceUtils.getBundledString("PROP_Author"));   //NOI18N
            desc[3].setShortDescription(ResourceUtils.getBundledString("HINT_Author"));   //NOI18N
            desc[4].setDisplayName(ResourceUtils.getBundledString("PROP_Splitindex"));   //NOI18N
            desc[4].setShortDescription(ResourceUtils.getBundledString("HINT_Splitindex"));   //NOI18N
            desc[5].setDisplayName(ResourceUtils.getBundledString("PROP_Windowtitle"));   //NOI18N
            desc[5].setShortDescription(ResourceUtils.getBundledString("HINT_Windowtitle"));   //NOI18N
            desc[6].setDisplayName(ResourceUtils.getBundledString("PROP_Doctitle"));   //NOI18N
            desc[6].setShortDescription(ResourceUtils.getBundledString("HINT_Doctitle"));   //NOI18N
            desc[7].setDisplayName(ResourceUtils.getBundledString("PROP_Header"));   //NOI18N
            desc[7].setShortDescription(ResourceUtils.getBundledString("HINT_Header"));   //NOI18N
            desc[8].setDisplayName(ResourceUtils.getBundledString("PROP_Footer"));   //NOI18N
            desc[8].setShortDescription(ResourceUtils.getBundledString("HINT_Footer"));   //NOI18N
            desc[9].setDisplayName(ResourceUtils.getBundledString("PROP_Bottom"));   //NOI18N
            desc[9].setShortDescription(ResourceUtils.getBundledString("HINT_Bottom"));   //NOI18N
            desc[10].setDisplayName(ResourceUtils.getBundledString("PROP_Link"));   //NOI18N
            desc[10].setShortDescription(ResourceUtils.getBundledString("HINT_Link"));   //NOI18N
            desc[10].setPropertyEditorClass(StdDocletSettingsService.LinkEditor.class);
            desc[11].setDisplayName(ResourceUtils.getBundledString("PROP_Linkoffline"));   //NOI18N
            desc[11].setShortDescription(ResourceUtils.getBundledString("HINT_Linkoffline"));   //NOI18N            
            desc[11].setPropertyEditorClass(StdDocletSettingsService.LinkEditor.class);
            desc[12].setDisplayName(ResourceUtils.getBundledString("PROP_Group"));   //NOI18N
            desc[12].setShortDescription(ResourceUtils.getBundledString("HINT_Group"));   //NOI18N
            desc[12].setPropertyEditorClass(StdDocletSettingsService.GroupEditor.class);
            desc[13].setDisplayName(ResourceUtils.getBundledString("PROP_Nodeprecated"));   //NOI18N
            desc[13].setShortDescription(ResourceUtils.getBundledString("HINT_Nodeprecated"));   //NOI18N
            desc[14].setDisplayName(ResourceUtils.getBundledString("PROP_Nodeprecatedlist"));   //NOI18N
            desc[14].setShortDescription(ResourceUtils.getBundledString("HINT_Nodeprecatedlist"));   //NOI18N
            desc[15].setDisplayName(ResourceUtils.getBundledString("PROP_Notree"));   //NOI18N
            desc[15].setShortDescription(ResourceUtils.getBundledString("HINT_Notree"));   //NOI18N
            desc[16].setDisplayName(ResourceUtils.getBundledString("PROP_Noindex"));   //NOI18N
            desc[16].setShortDescription(ResourceUtils.getBundledString("HINT_Noindex"));   //NOI18N
            desc[17].setDisplayName(ResourceUtils.getBundledString("PROP_Nohelp"));   //NOI18N
            desc[17].setShortDescription(ResourceUtils.getBundledString("HINT_Nohelp"));   //NOI18N
            desc[18].setDisplayName(ResourceUtils.getBundledString("PROP_Nonavbar"));   //NOI18N
            desc[18].setShortDescription(ResourceUtils.getBundledString("HINT_Nonavbar"));   //NOI18N
            desc[19].setDisplayName(ResourceUtils.getBundledString("PROP_Helpfile"));   //NOI18N
            desc[19].setShortDescription(ResourceUtils.getBundledString("HINT_Helpfile"));   //NOI18N
            desc[19].setValue("directories", Boolean.FALSE );  //NOI18N
            desc[19].setValue("files", Boolean.TRUE );  //NOI18N
            desc[20].setDisplayName(ResourceUtils.getBundledString("PROP_Stylesheetfile"));   //NOI18N
            desc[20].setShortDescription(ResourceUtils.getBundledString("HINT_Stylesheetfile"));   //NOI18N
            desc[20].setValue("directories", Boolean.FALSE );  //NOI18N
            desc[20].setValue("files", Boolean.TRUE );  //NOI18N
            desc[21].setDisplayName(ResourceUtils.getBundledString("PROP_Charset"));   //NOI18N
            desc[21].setShortDescription(ResourceUtils.getBundledString("HINT_Charset"));   //NOI18N
        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
            return null; }
        return desc; }
     /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavaDoclet.gif"); // NOI18N
        } else {
            return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } } }// NOI18N
