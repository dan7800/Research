/**
* Copyright (C) 2002 Lars J. Nilsson, webmaster at larsan.net
*
*   This program is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public License
*   as published by the Free Software Foundation; either version 2.1
*   of the License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
*/

package net.larsan.urd.util.fileset;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
import java.security.cert.*;
import net.larsan.urd.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.net.*;

/**
* A jar file set is a watchable file set that works on JAR archives. This file set
* is not recursive by default. The file set checks for modified dates on the jar archives.
*
* <p>This file set needs to be started for the automatic rescans to take effect. This means that
* the file set also needs to be stopped to cancel all pending threads.
*
* <p>This file set supports signed jar files.
* 
* <p>This class can be extended to validate the JAR files before their inclusion using
* the {@link #validateJar(File) validate} method.
* 
* <p>This class pre-caches all files in the JAR file not to hold the file open. 
*
* @author Lars J. Nilsson
* @version ##URD-VERSION##
*/

public class JarFileSet extends FileSet implements WatchableFileSet {
    
    /**
    * A Map of files which are used in this file set mapped to their last
    * modification time as a <i>Long<i>. All of these files
    * should be jar archives (but file objects).
    */
    
    private Map jarFiles;
    
    /**
    * A map of path names -> resources. The <code>JarResources</code>
    * within this map will share file form the <code>jarFile</code> set above.
    */
    
    private Map resources;
    
    /**
    * Current listeners. This is a <code>java.lang.reflect.Proxy</code> object using
    * an <code>EventInvoker</code> as a dispatcher.
    */
    
    private Proxy listeners;
    
    /**
    * Current timer task, used to scan for resources changes
    */
    
    private TimerTask currentTask;
    
    /**
    * Timer that is used to check the file set for changes.
    */
    
    private Timer timer;
    
    
    
    /// --- CONSTRUCTORS --- ///
    
    /**
    * Contruct the file set with a root folder and a timer to use for the watch. This will throw an 
    * <code>IOException</code> if the root does not exist or denotes a file.
    *
    * <p>If the timer is set to null, the filet will not be watchable.
    *
    * @param root File set root folder, must exist
    * @param timer Timer to use for resource change checks, may be null
    * @throws IOException If the root folder does not exist
    */
    
    public JarFileSet(File root, Timer timer) throws IOException {
        super(root);
        this.jarFiles = new HashMap();
        this.resources = new HashMap();
        listeners = (Proxy)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { FileSetListener.class }, new EventInvoker());
        super.setRegexpPattern("^.*\\.jar$");    
        super.setIsRecursive(false);
        this.timer = timer;
        this.currentTask = null;
        scan();
    }
    
    
    /// --- PUBLIC ACCESSORS --- ///
    
    /**
    * Get the active archves files in this file set.
    * 
    * @return The current file set.
    */
    
    public synchronized File[] getActiveFiles() {
        File[] arr = new File[jarFiles.size()];   
        jarFiles.keySet().toArray(arr);
        return arr;
    }
    
    
    /**
    * Start watching this file set. The watch interval will be determined by the parameter which must
    * be a long which is bigger than 0 for the watch to start.
    *
    * @param interval Interval in milliseconds between file set checks, must be bigger than 0
    */
    
    public synchronized void start(long interval) { 
        if(interval > 0 && timer != null) {
            if(currentTask != null) stop();
            timer.schedule(new TimerTask() {
                public void run() {
                    checkScan();
                }
            }, interval, interval);
        }
    }
    
    /**
    * Stop watcing this file set.
    */
    
    public synchronized void stop() {
        if(currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }
    
    /**
    * Perform a scan of available files for the set. This method can be called at
    * any time and it is up to subclasses to take steps to synchronize resources. <p>
    *
    * @throws An IOException should the scan fail
    */
    
    public synchronized void scan() throws IOException {
        doScan();
    }
    
    /**
    * Get all available resources ids. This arrays should be empty if
    * the file set is empty.
    *
    * @return A string array of relative paths, an empty array will be returned if the set is empty
    */

    public synchronized String[] getResourcePaths() {
        String[] answer = new String[resources.size()];
        resources.keySet().toArray(answer);
        return answer;
    }
    
    /**
    * Get a resource from the set. This should return a Resource if the resource is found
    * or null if not. The parameter should be a relative path as reported by <code>getResourcePaths</code>. 
    *
    * @param filePath A relative path to the resource to get
    * @return A FileSet.Resource object representing the resource or null if not found
    */

    public synchronized Resource getResource(String path) {
        return (Resource)resources.get(path);
    }    
    
    /**
    * Get the certificates for a resource. This method returns null if the resource is not
    * signed.
    *
    * @param resource The resource to check for signing certificates
    * @return An arraya of signing certificates or null if not signed
    */
    
    public Certificate[] getCertificates(Resource resource) {
        if(resource instanceof Certifiable) return ((Certifiable)resource).getCertificates();
        else return null;
    }
    
    /**
    * Register file set listener.
    */
    
    public void addFileSetListener(FileSetListener listener) {
        ((EventInvoker)listeners.getInvocationHandler(listeners)).addListener(listener);
    }
    
    /**
    * De-register file set listener
    */
    
    public void removeFileSetListener(FileSetListener listener) {
        ((EventInvoker)listeners.getInvocationHandler(listeners)).removeListener(listener);  
    }
    
    
    
    /// --- PROTECTED INTERFACE --- ///
    
    /**
    * Check if a JAR file should be included in the file set.
    *
    * @param file Jar file to check
    * @return true Only if the jar file should be included
    */
    
    protected boolean validateJar(File file) throws IOException { return true; }
    
    
    
    /// --- PRIVATE HELPER METHODS --- ///
    
    /**
    * Do a scan of available files in the file set, this method clears available resources
    * and then calls the recursive <i>scanJars</i> method.
    */
    
    private void doScan() throws IOException {
        resources.clear();
        jarFiles.clear();
        scanJars(root, jarFiles);
        scanResources(jarFiles.keySet());
    }
    
    /**
    * Perform re-scan to check for changes.
    */
    
    private void checkScan() {
        if(((EventInvoker)listeners.getInvocationHandler(listeners)).size() > 0) {  
            List events = new LinkedList();
            Map newJars = new HashMap();
            try {
                resources.clear();
                scanJars(root, newJars);
                checkChanged(newJars.keySet(), jarFiles, events);
                scanResources(jarFiles.keySet());
                fireEvents(events);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
    * Check if to set of files differ and create events accordingly. This will ever only throw 
    * batch change events and only one per check.
    */
    
    private synchronized void checkChanged(Set newJars, Map oldJars, List events) {
        boolean isAdded = false;
        
        // check addition and changes
        for(Iterator i = newJars.iterator(); i.hasNext(); ) {
            File file = (File)i.next();
            if(oldJars.containsKey(file)) {
                long last = ((Long)oldJars.get(file)).longValue();
                if(last != file.lastModified() && !isAdded) {
                    events.add(new FileSetEvent(FileSetEvent.STRUCTURAL_CHANGE, this));
                    isAdded = true;
                }  
                oldJars.remove(file);  
            } else {
                if(!isAdded) {
                    events.add(new FileSetEvent(FileSetEvent.STRUCTURAL_CHANGE, this));
                }
            }
        }
        
        // check removed
        if(oldJars.size() > 0 && !isAdded) events.add(new FileSetEvent(FileSetEvent.STRUCTURAL_CHANGE, this));
        
        // clear old and add new
        oldJars.clear();
        for(Iterator i = newJars.iterator(); i.hasNext(); ) {
            File file = (File)i.next();
            oldJars.put(file, new Long(file.lastModified()));
        }
    }
    
    
    /**
    * Recursive scan method using a folder and a map to garther jar files in.
    */
    
    private void scanJars(File folder, Map jarFiles) throws IOException {
        File[] files = folder.listFiles();   
        for(int i = 0; i < files.length; i++) {
            if(!files[i].isFile() && super.isRecursive()) scanJars(files[i], jarFiles);
            else {
                String path = resolvePath(files[i]);
                if(super.matches(path) && validateJar(files[i])) {
                    jarFiles.put(files[i], new Long(files[i].lastModified()));
                }
            }
        }
    }
    
    /**
    * Scan available jar files and garther resources from them
    */
    
    private void scanResources(Set jarFiles) throws IOException {
        for(Iterator i = jarFiles.iterator(); i.hasNext(); ) {
            File file = (File)i.next();
            
            /*
             * WARNING! Here we try to lock the file while we're reading
             * it. This might fail on some platforms that does not support
             * shared locks.
             */ 
            
            FileInputStream str = new FileInputStream(file);
            FileLock lock = str.getChannel().lock(0L, Long.MAX_VALUE, true); 
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(file);
            try {
                for(Enumeration e = jarFile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry)e.nextElement();
                    String name = entry.getName(); //.replace('/', File.separatorChar);
                    resources.put(name, new JarResource(file, jarFile, name, System.currentTimeMillis(), super.getErrorHandler()));
                }
            } finally {
                try { str.close(); } catch(IOException e) { }
                jarFile.close();
                lock.release();
            }
        }
    }

    /**
    * Fire a list of <code>FileSetEvent</code>s to the listeners. This method synchronizes
    * upon the listener collection and takes a defensive copy of the listeners before commencing.
    */
    
    private void fireEvents(List events) { 
        if(((EventInvoker)listeners.getInvocationHandler(listeners)).size() > 0) { 
            for(Iterator i = events.iterator(); i.hasNext(); ) {
                FileSetEvent ev = (FileSetEvent)i.next();
                ((FileSetListener)listeners).receiveFileSetEvent(ev);
            }
        }
    }
    
    
    /// --- INNER CLASSES --- ///
    
    /**
    * Inner class for a resource within a jar archive. 
    */
    
    public static class JarResource extends ResourceBase implements Certifiable {
        
        /**
        * File that this resource was fatched from.
        */
        
        private File file;
        
        /**
        * Resource byte content.
        */
        
        private byte[] bytes;
        
        /**
        * Jar file path, this file path may differ from the super class file path
        * since all jar archives will store their entries using a "/" separator.
        */
        
        private String jarPath;
        
        /**
        * Signing certificates for this resource, if any. This reference should be
        * checked using the <code>checkCerificates</code> method, and will be null if there
        * are no signing certificates.
        */
        
        private Certificate[] certs;
        
        /**
         * Entry URL.
         */
        
        private URL url;
        
        
        /// --- CONSTRUCTORS --- ///
        
        /**
        * Contruct the resource with a jar file, a path (within the archive) and
        * a time when it was visited.
        *
        * @param file JAR archive file
        * @param jarFile Jar file object
        * @param filePath File path within archive
        * @param visited Time of file scan
        */
        
        public JarResource(File file, java.util.jar.JarFile jarFile, String filePath, long visited, ErrorHandler handler) {
            super(filePath, visited, handler);
            this.jarPath = filePath.replace(File.separatorChar, '/');
            this.file = file;
            checkCertificates(jarFile);
            preCacheBytes(jarFile);   
        } 
        
        /**
        * Contruct the resource with a jar file, and a path (within the archive).
        *
        * @param file JAR archive file
        * @param jarFile Jar file object
        * @param filePath File path within archive
        */
        
        public JarResource(File file, java.util.jar.JarFile jarFile, String filePath) {
            this(file, jarFile, filePath, System.currentTimeMillis(), null);
        }
        
        
        
        /// --- PUBLIC ACCESSORS --- ///
        
        /**
         * Destroy this resource. 
         */
        
        public void destroy() {
            bytes = null;
            certs = null;   
        }
        
        
        /**
        * Get a byte stream from the file. Should the resource have been disabled or
        * removed before this method is called it will return null.
        *
        * @return A file input stream from the resource, or null if disabled
        * @throws IOException On IO errors
        */
        
        public InputStream getStream() {
            return (bytes == null ? null : new ByteArrayInputStream(bytes));
            /*JarEntry entr = getEntry();
            if(entr == null || entr.isDirectory()) return null;
            else {
                try {
                    return jarFile.getInputStream(entr);
                } catch(IOException e) { 
                    return (InputStream)super.report(e);
                }
            }*/
        }
        
        /**
        * Get a byte array from the file. Should the resource have been disabled or
        * removed before this method is called it will return null.
        *
        * @return A byte array from the resource, or null if disabled
        * @throws IOException On IO errors
        */
        
        public byte[] getBytes() {
            return bytes;
            /*JarEntry entr = getEntry();
            if(entr == null || entr.isDirectory()) return null;
            else {
                try {
                    return IOUtils.toByteArray(jarFile.getInputStream(entr));
                } catch(IOException e) { 
                    return (byte[])super.report(e);
                }
            }*/
        }
        
        /**
        * Get a URL to the resource. Should the resource have been disabled or
        * removed before this method is called it will return null.
        *
        * @return The resource URL, or null if disabled
        */
        
        public URL getURL() {
            if(bytes == null) return null;
            else {
                try {
                    StringBuffer buff = new StringBuffer("jar:");
                    buff.append(file.toURL().toString()).append("!/");
                    buff.append(jarPath);
                    return new URL(buff.toString());
                } catch(MalformedURLException e) {
                    throw new RuntimeException("malformed jar URL: " + e.getMessage());
                }
            }   
        }
        
        /**
        * Get the code source.
        */
        
        public URL getCodeSource() {
            try {
                return file.toURL();
            } catch(MalformedURLException e) {
                throw new RuntimeException("malformed jar URL: " + e.getMessage());
            }
        }
        
        
        /**
        * Check if the resource is valid. This should return false if the underlying
        * file have is removed or unusable.
        */
        
        public boolean exists() {
            return (bytes != null);
            /*JarEntry entr = getEntry();
            return (entr != null && !entr.isDirectory());*/
        }
        
        /**
        * Get certificates for this entry. Returns null if the entry is
        * not signed or not found.
        *
        * @return A Certificate array, or null if not signed.
        */
        
        public Certificate[] getCertificates() {
            //checkCertificates();
            return certs;
        }
        
        
        
        /// --- PRIVATE HELPER METHODS --- ///
        
        /**
        * Get the jar file entry, this returns null if the extry is not found.
        */
        
        /*private JarEntry getEntry() {
            if(file.exists()) return jarFile.getJarEntry(jarPath);
            else return null;
        }*/
        
        /**
        * Make sure we've read the certificates fro this entry. The certificates will be
        * referenced in this class after this method is finished.
        */
        
        private void checkCertificates(java.util.jar.JarFile file) {
            JarEntry entr = file.getJarEntry(jarPath);
            if(entr == null) certs = null;
            else {
                if(certs == null) {
                    try {
                        IOUtils.toByteArray(file.getInputStream(entr));   
                        certs = entr.getCertificates();
                    } catch(IOException e) {
                        certs = null;
                    }
                }   
            }
        }
        
        /**
         * Precache the entry bytes.
         */
        
        private void preCacheBytes(java.util.jar.JarFile file) {
            JarEntry entr = file.getJarEntry(jarPath);
            if(entr == null) bytes = null;
            else {
                try {
                    bytes = IOUtils.toByteArray(file.getInputStream(entr));
                } catch(IOException e) { 
                    bytes = (byte[])super.report(e);
                }
            }
        }
    }
}