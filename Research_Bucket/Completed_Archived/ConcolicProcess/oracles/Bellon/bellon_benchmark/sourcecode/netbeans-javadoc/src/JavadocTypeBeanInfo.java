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
import java.beans.*;
import java.awt.*;
import java.util.ResourceBundle;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/** BeanInfo for javadoc type
 */
public class JavadocTypeBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (org.netbeans.modules.javadoc.JavadocType.class);
        descr.setDisplayName (ResourceUtils.getBundledString("CTL_JavadocExec"));   //NOI18N
        descr.setShortDescription (ResourceUtils.getBundledString("HINT_JavadocExec"));   //NOI18N
        descr.setValue ("version", "1.1"); // NOI18N
        return descr; }
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null; } }
    /* Provides the JarFileSystem's icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavaDoc.gif"); // NOI18N
        else
            return Utilities.loadImage("/org/netbeans/modules/javadoc/resources/JavadocSettings32.gif");  } }// NOI18N
