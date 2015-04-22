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
package org.netbeans.modules.javadoc.search.environment;
import java.beans.*;
import org.openide.util.Utilities;
/**
 *
 * @author  sdedic
 * @version 
 */
public class JavadocArchiveBeanInfo extends SimpleBeanInfo {
    public java.awt.Image getIcon (int kind) {
        try {
            return Introspector.getBeanInfo (org.openide.filesystems.JarFileSystem.class).getIcon (kind);
        } catch (IntrospectionException ie) {
            return null; } }
    public java.beans.BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                Introspector.getBeanInfo(org.openide.filesystems.JarFileSystem.class)
            };
        } catch (IntrospectionException ie) {
            return null; } }
    public java.beans.BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(JavadocArchive.class);
        bd.setValue("transient", Boolean.TRUE);    //NOI18N
       bd.setValue("global", Boolean.TRUE);   //NOI18N
        return bd; } }
