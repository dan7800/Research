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
import java.util.ResourceBundle;
import com.sun.javadoc.*;
import org.openide.windows.OutputWriter;
import org.openide.util.NbBundle;
/**
 * This class implements error, warning and notice printing.
 *
 * @author Petr Hrebejk
 */
class DocErrorReporterImpl implements DocErrorReporter {
    private OutputWriter errWriter;
/**
 * @param errWriter  */    
    DocErrorReporterImpl ( OutputWriter errWriter ) {
        this.errWriter = errWriter; }
    /**
     * Print error message, increment error count.
     *
     * @param msg message to print
     */
    public void printError(String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Error") + " " + msg );    }//NOI18N
    /**
     * Print error message, increment error count.
     *
     * @param pos source position
     * @param msg message to print
     */
    public void printError(SourcePosition pos, String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Error") + " " + msg );    }//NOI18N
    /**
     * Print warning message, increment warning count.
     *
     * @param msg message to print
     */
    public void printWarning(String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Warning") + " " + msg );    }//NOI18N
    /**
     * Print warning message, increment warning count.
     *
     * @param pos source position
     * @param msg message to print
     */
    public void printWarning(SourcePosition pos, String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Warning") + " " + msg );    }//NOI18N
    /**
     * Print a message.
     *
     * @param msg message to print
     */
    public void printNotice(String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Notice") + " " + msg );    }//NOI18N
    /**
     * Print a message.
     *
     * @param pos source position
     * @param msg message to print
     */
    public void printNotice(SourcePosition pos, String msg) {
        errWriter.println ( ResourceUtils.getBundledString("MSG_Notice") + " " + msg );    } }//NOI18N
