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
import java.io.*;
import java.util.*;
import java.text.MessageFormat;
import org.openide.filesystems.*;
import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;
/** Catch and parse javadoc stdio and stderr streams
 *
 * @author Petr Suchomel, part of this taken from org.openide.compilers
 * @version 1.0
 */
class ExternalJavadocOutputParser extends Thread{
    /** buffered reader of input data */
    BufferedReader parsedReader = null;
    /** writer from IO tab */
    private OutputWriter output;
    /** list of errors */
    //private List errorList = new ArrayList();
    /** set if error has found */
    private boolean foundError = false;
    /**
     * inpstream input data from task
     * output where to display result
     * setStop, after finished, shows message that task is finished
     * @param inpstream OutputStream from external process
     * @param output PrintWriter of window to which write parsed data
    */
    ExternalJavadocOutputParser( InputStream inpstream, 
                                 OutputWriter output ) {
        parsedReader = new BufferedReader(new InputStreamReader(inpstream));
        this.output = output;         }
    /** Override Thread.run() method
     */    
    public void run() {
        try {
            String error = ResourceUtils.getBundledString("MSG_Gen_Error");   //NOI18N
            String line;
            while( ( line = parsedReader.readLine() ) != null ){
                output.print(line + "\n");   //NOI18N
                if( line.indexOf( error ) != -1 ){
                    foundError = true; } } }
        catch(IOException ioEx){
            TopManager.getDefault().notifyException(ioEx); } }
    /** Used to get info if error(s) occured while javadoc has been generated
     * @return true if error occure
     */
    public boolean isFoundError(){
        return foundError; } }
