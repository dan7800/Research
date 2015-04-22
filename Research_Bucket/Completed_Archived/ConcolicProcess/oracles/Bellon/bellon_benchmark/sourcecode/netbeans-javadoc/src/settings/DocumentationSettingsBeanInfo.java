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
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.StringArrayEditor;
import org.openide.explorer.propertysheet.editors.StringArrayCustomEditor;
import org.netbeans.modules.javadoc.JavadocType;
import org.netbeans.modules.javadoc.search.JavadocSearchType;
import org.openide.util.Utilities;
/** BeanInfo for general documentation settings
*
* @author Petr Hrebejk
*/
public class DocumentationSettingsBeanInfo extends SimpleBeanInfo {
    /** descriptor of bean */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.javadoc.search.Jdk12SearchType_japan.class);
        descr.setDisplayName(NbBundle.getBundle(DocumentationSettings.class).getString("CTL_Documentation_settings"));   //NOI18N
        descr.setValue ("version", "1.1"); // NOI18N            
        descr.setValue ("global", Boolean.FALSE); //NOI18N
        return descr; }
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc;
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor("autocommentModifierMask", DocumentationSettings.class), // 0 // NOI18N
                       new PropertyDescriptor("autocommentPackage", DocumentationSettings.class),      // 1 // NOI18N
                       new PropertyDescriptor("autocommentErrorMask", DocumentationSettings.class),    // 2 // NOI18N
                       new PropertyDescriptor("idxSearchSort", DocumentationSettings.class),           // 3 // NOI18N
                       new PropertyDescriptor("idxSearchNoHtml", DocumentationSettings.class),         // 4 // NOI18N
                       new PropertyDescriptor("idxSearchSplit", DocumentationSettings.class),          // 5 // NOI18N
                       new PropertyDescriptor("autocommentSplit", DocumentationSettings.class),        // 6 // NOI18N
                       new PropertyDescriptor("executor", DocumentationSettings.class),                // 7 // NOI18N
                       new PropertyDescriptor("searchEngine", DocumentationSettings.class),            // 8 // NOI18N 
                       new PropertyDescriptor("fileSystemSettings", DocumentationSettings.class),      // 9 // NOI18N 
                       new PropertyDescriptor("askBeforeGenerating", DocumentationSettings.class),      // 10 // NOI18N 
                       new PropertyDescriptor("askAfterGenerating", DocumentationSettings.class),      // 11 // NOI18N 
            };
            desc[0].setDisplayName("autocommentModifierMask"); //NOI18N
            desc[0].setHidden( true );
            desc[1].setDisplayName("autocommentPackage"); //NOI18N
            desc[1].setHidden( true );
            desc[2].setDisplayName("autocommentErrorMask"); //NOI18N
            desc[2].setHidden( true );
            desc[3].setDisplayName("idxSearchSort"); //NOI18N
            desc[3].setHidden( true );
            desc[4].setDisplayName("idxSearchNoHtml"); //NOI18N
            desc[4].setHidden( true );
            desc[5].setDisplayName("idxSearchSplit"); //NOI18N
            desc[5].setHidden( true );
            desc[6].setDisplayName("autocommentSplit"); //NOI18N
            desc[6].setHidden( true );
            desc[7].setDisplayName(ResourceUtils.getBundledString("PROP_SetExecutor"));   //NOI18N
            desc[7].setShortDescription(ResourceUtils.getBundledString("HINT_SetExecutor"));   //NOI18N
            desc[7].setValue("superClass", JavadocType.class);   //NOI18N
            desc[8].setDisplayName(ResourceUtils.getBundledString("PROP_SetDefaultSearchEngine"));   //NOI18N
            desc[8].setShortDescription(ResourceUtils.getBundledString("HINT_SetDefaultSearchEngine"));   //NOI18N
            desc[8].setValue("superClass", JavadocSearchType.class);   //NOI18N
            desc[9].setDisplayName("fileSystemSettings"); //NOI18N
            desc[9].setHidden(true);    //file system setting
            desc[10].setDisplayName(ResourceUtils.getBundledString("PROP_AskBeforeGenerating"));   //NOI18N
            desc[10].setShortDescription(ResourceUtils.getBundledString("HINT_AskBeforeGenerating"));   //NOI18N
            desc[11].setDisplayName(ResourceUtils.getBundledString("PROP_AskAfterGenerating"));   //NOI18N
            desc[11].setShortDescription(ResourceUtils.getBundledString("HINT_AskAfterGenerating"));   //NOI18N
        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
            return null; }
        return desc; }
    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavadocSettings.gif"); // NOI18N
        } else {
            return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } } }// NOI18N
