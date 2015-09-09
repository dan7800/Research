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
import org.openide.util.Utilities;
/** BeanInfo for JavadocSettings - defines property editor
*
* @author Petr Hrebejk
*/
public class JavadocSettingsBeanInfo extends SimpleBeanInfo {
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc = null;
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("members", JavadocSettings.class),         //0 // NOI18N
                       new PropertyDescriptor("overview", JavadocSettings.class),        // 1 // NOI18N
                       new PropertyDescriptor("extdirs", JavadocSettings.class),         // 2 // NOI18N
                       new PropertyDescriptor("style1_1", JavadocSettings.class),        // 3 // NOI18N
                       new PropertyDescriptor("verbose", JavadocSettings.class),         // 4 // NOI18N
                       new PropertyDescriptor("encoding", JavadocSettings.class),        // 5 // NOI18N
                       new PropertyDescriptor("locale", JavadocSettings.class),          // 6 // NOI18N
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
        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
            return null; }
        return desc; }
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavadocSettings.gif"); // NOI18N
        } else {
                return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } } }// NOI18N
