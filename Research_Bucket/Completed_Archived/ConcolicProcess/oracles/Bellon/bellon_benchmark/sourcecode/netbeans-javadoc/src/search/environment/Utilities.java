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
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.filesystems.FileSystem;
import org.openide.execution.NbClassPath;
/**
 * Various utilities useful when solving execution/compilation environment
 * issues.
 *
 * @author  sdedic
 * @version 
 */
public class Utilities {
    private static final char PATH_SEPARATOR = java.io.File.pathSeparatorChar;
    /**
     * Holds the file representing user's installation root.
     */
    private static java.io.File userHome;
    /**
     * Holds the file that represents system installation root.
     */
    private static java.io.File systemHome;
    private static java.util.ResourceBundle bundle;
    static {
        userHome = findDirectory(System.getProperty("netbeans.user")); // NOI18N
        systemHome = findDirectory(System.getProperty("netbeans.home"));  }// NOI18N
    static String getString(String key) {
        if (bundle == null)
            bundle = org.openide.util.NbBundle.getBundle(Utilities.class);
        return bundle.getString(key); }
    /**
     * Attempts to find a named file in NetBeans installation. The function
     * searches user directory first, then it look in the shared one.
     * @param identifying filename, relative to the root of installation dir.
     * @return File instance, if it finds the file and that is accessible,
     * null otherwise.
     * @throws IOException if some file-handling function reports that exception.
     */
    public static java.io.File findInstalledFile(String relativeName) {
        java.io.File f;
        if (userHome != null) {
            f = new java.io.File(userHome, relativeName);
            //System.err.println("trying " + f);
            if (f.exists())
                return f; }
        if (systemHome != null) {
            f = new java.io.File(systemHome, relativeName);
            //System.err.println("Trying " + f);
            if (f.exists())
                return f; }
        return null; }
    static final java.io.File findDirectory(String s) {
        if (s == null || s.length() == 0)
            return null;
        java.io.File f = new java.io.File(s);
        if (f.exists() && f.isDirectory())
            return f;
        else
            return null; }
    /**
     * Returns a java.io.File instance representing the user installation
     * root directory. Can return null, if the user's directory was not
     * specified or does not exist.
     */
    public static File getUserHome() {
        return userHome; }
    /**
     * Returns a java.io.File instance that represents the system installation
     * directory. Can return null, if that directory is null or is inaccessible.
     */
    public static File getSystemHome() {
        return systemHome; } }
