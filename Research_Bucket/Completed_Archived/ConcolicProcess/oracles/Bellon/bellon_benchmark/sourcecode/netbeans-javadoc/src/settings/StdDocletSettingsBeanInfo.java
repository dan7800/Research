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
public class StdDocletSettingsBeanInfo extends SimpleBeanInfo {
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc;
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("directory", StdDocletSettings.class),       // 0 // NOI18N
                       new PropertyDescriptor("use", StdDocletSettings.class),             // 1 // NOI18N
                       new PropertyDescriptor("version", StdDocletSettings.class),         // 2 // NOI18N
                       new PropertyDescriptor("author", StdDocletSettings.class),          // 3 // NOI18N
                       new PropertyDescriptor("splitindex", StdDocletSettings.class),      // 4 // NOI18N
                       new PropertyDescriptor("windowtitle", StdDocletSettings.class),     // 5 // NOI18N
                       new PropertyDescriptor("doctitle", StdDocletSettings.class),        // 6 // NOI18N
                       new PropertyDescriptor("header", StdDocletSettings.class),          // 7 // NOI18N
                       new PropertyDescriptor("footer", StdDocletSettings.class),          // 8 // NOI18N
                       new PropertyDescriptor("bottom", StdDocletSettings.class),          // 9 // NOI18N
                       new PropertyDescriptor("link", StdDocletSettings.class),            // 10 // NOI18N
                       new PropertyDescriptor("group", StdDocletSettings.class),           // 11 // NOI18N
                       new PropertyDescriptor("nodeprecated", StdDocletSettings.class),    // 12 // NOI18N
                       new PropertyDescriptor("nodeprecatedlist", StdDocletSettings.class),// 13 // NOI18N
                       new PropertyDescriptor("notree", StdDocletSettings.class),          // 14 // NOI18N
                       new PropertyDescriptor("noindex", StdDocletSettings.class),         // 15 // NOI18N
                       new PropertyDescriptor("nohelp", StdDocletSettings.class),          // 16 // NOI18N
                       new PropertyDescriptor("nonavbar", StdDocletSettings.class),        // 17 // NOI18N
                       new PropertyDescriptor("helpfile", StdDocletSettings.class),        // 18 // NOI18N
                       new PropertyDescriptor("stylesheetfile", StdDocletSettings.class),  // 19 // NOI18N
                       new PropertyDescriptor("charset", StdDocletSettings.class),         // 20 // NOI18N
                   };
            desc[0].setDisplayName(ResourceUtils.getBundledString("PROP_Directory"));   //NOI18N
            desc[0].setShortDescription(ResourceUtils.getBundledString("HINT_Directory"));   //NOI18N
            desc[0].setValue("directories", Boolean.TRUE ); //NOI18N
            desc[0].setValue("files", Boolean.FALSE );  //NOI18N
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
            desc[11].setDisplayName(ResourceUtils.getBundledString("PROP_Group"));   //NOI18N
            desc[11].setShortDescription(ResourceUtils.getBundledString("HINT_Group"));   //NOI18N
            //desc[11].setPropertyEditorClass(StdDocletSettings.GroupEditor.class);
            desc[12].setDisplayName(ResourceUtils.getBundledString("PROP_Nodeprecated"));   //NOI18N
            desc[12].setShortDescription(ResourceUtils.getBundledString("HINT_Nodeprecated"));   //NOI18N
            desc[13].setDisplayName(ResourceUtils.getBundledString("PROP_Nodeprecatedlist"));   //NOI18N
            desc[13].setShortDescription(ResourceUtils.getBundledString("HINT_Nodeprecatedlist"));   //NOI18N
            desc[14].setDisplayName(ResourceUtils.getBundledString("PROP_Notree"));   //NOI18N
            desc[14].setShortDescription(ResourceUtils.getBundledString("HINT_Notree"));   //NOI18N
            desc[15].setDisplayName(ResourceUtils.getBundledString("PROP_Noindex"));   //NOI18N
            desc[15].setShortDescription(ResourceUtils.getBundledString("HINT_Noindex"));   //NOI18N
            desc[16].setDisplayName(ResourceUtils.getBundledString("PROP_Nohelp"));   //NOI18N
            desc[16].setShortDescription(ResourceUtils.getBundledString("HINT_Nohelp"));   //NOI18N
            desc[17].setDisplayName(ResourceUtils.getBundledString("PROP_Nonavbar"));   //NOI18N
            desc[17].setShortDescription(ResourceUtils.getBundledString("HINT_Nonavbar"));   //NOI18N
            desc[18].setDisplayName(ResourceUtils.getBundledString("PROP_Helpfile"));   //NOI18N
            desc[18].setShortDescription(ResourceUtils.getBundledString("HINT_Helpfile"));   //NOI18N
            desc[18].setValue("directories", Boolean.FALSE );   //NOI18N
            desc[18].setValue("files", Boolean.TRUE );  //NOI18N
            desc[19].setDisplayName(ResourceUtils.getBundledString("PROP_Stylesheetfile"));   //NOI18N
            desc[19].setShortDescription(ResourceUtils.getBundledString("HINT_Stylesheetfile"));   //NOI18N
            desc[19].setValue("directories", Boolean.FALSE );   //NOI18N
            desc[19].setValue("files", Boolean.TRUE );  //NOI18N
            desc[20].setDisplayName(ResourceUtils.getBundledString("PROP_Charset"));   //NOI18N
            desc[20].setShortDescription(ResourceUtils.getBundledString("HINT_Charset"));   //NOI18N
        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
            return null; }
        return desc; }
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavadocSettings.gif"); // NOI18N
        } else {
            return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } } }// NOI18N
