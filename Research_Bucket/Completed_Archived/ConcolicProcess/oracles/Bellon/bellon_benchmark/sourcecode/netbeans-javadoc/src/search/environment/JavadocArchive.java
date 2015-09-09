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
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.FileSystemCapability;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.io.File;
/**
 *
 * @author  sdedic
 * @version 
 */
class JavadocArchive extends JarFileSystem {
    static final long serialVersionUID = -874387334577L;
    final private String systemName; // #13340
    public JavadocArchive(String name) {
        systemName = name; }
    public void setJarFile(File jar) throws IOException, PropertyVetoException {
        super.setJarFile(jar);
        // Prevent the system name from being changed by this.
        setSystemName(systemName); }
    private Object writeReplace() {
        return null; } }
