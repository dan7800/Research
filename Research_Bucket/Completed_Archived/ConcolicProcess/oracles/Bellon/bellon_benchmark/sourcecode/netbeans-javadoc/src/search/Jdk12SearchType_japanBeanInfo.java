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
import java.beans.*;
import java.awt.*;
import java.util.ResourceBundle;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
public class Jdk12SearchType_japanBeanInfo extends SimpleBeanInfo {
     /** @retrun BeanDescriptor for this class */
    public BeanDescriptor getBeanDescriptor () {    
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.javadoc.search.Jdk12SearchType_japan.class);
        descr.setName(ResourceUtils.getBundledString("PROP_Jdk12SearchTypeJapan")); // NOI18N
        descr.setValue ("version", "1.1"); // NOI18N            
        return descr; }
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] desc;
        try {
            desc = new PropertyDescriptor[]{
                    new PropertyDescriptor("caseSensitive", Jdk12SearchType_japan.class), //0 // NOI18N
                    new PropertyDescriptor("japanEncoding", Jdk12SearchType_japan.class) //1 // NOI18N
            };
            desc[0].setDisplayName(ResourceUtils.getBundledString("PROP_CaseSensitive"));   //NOI18N
            desc[0].setShortDescription(ResourceUtils.getBundledString("HINT_CaseSensitive"));   //NOI18N
            desc[1].setDisplayName(ResourceUtils.getBundledString("PROP_JaEncoding"));   //NOI18N
            desc[1].setShortDescription(ResourceUtils.getBundledString("HINT_JaEncoding"));   //NOI18N
            desc[1].setPropertyEditorClass(JapanJavadocEncodings.class);
        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
            return null; }
        return desc; }
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/searchDoc.gif"); // NOI18N
        else
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/searchDoc.gif");  } }// NOI18N
