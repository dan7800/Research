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
import java.awt.Image;
import java.beans.*;
import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/** Description of the executor.
 *
 * @author jlahoda
 */
public class ExternalJavadocExecutorBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (ExternalJavadocExecutorBeanInfo.class);
        // Display name also serves as default for instances:
        desc.setDisplayName (NbBundle.getMessage (ExternalJavadocExecutorBeanInfo.class, "CTL_Javadoc_executor"));   //NOI18N
        desc.setShortDescription (NbBundle.getMessage (ExternalJavadocExecutorBeanInfo.class, "HINT_Javadoc_executor"));   //NOI18N
        return desc; }
    // Inherit properties and so on from ProcessExecutor.
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (ProcessExecutor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))   //NOI18N
                ie.printStackTrace ();
            return null; } }
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/generatejavadoc.gif"); //NOI18N
        } else {
            return Utilities.loadImage ("/org/netbeans/modules/javadoc/resources/generatejavadoc.gif");  } } }//NOI18N
