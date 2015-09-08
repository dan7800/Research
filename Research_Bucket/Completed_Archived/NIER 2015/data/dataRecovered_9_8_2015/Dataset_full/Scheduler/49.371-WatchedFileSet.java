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
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
import net.larsan.urd.util.*;

/**
* This file set is a <code>RegexpFileSet</code> that implements the <code>WatchableFileSet</code> interface.
* this class uses a <code>java.util.Timer</code> to regulary scan the set for changes, this timer can be provided
* by the controlling class in order to minimize the use of thread when having multiple file sets.
*
* <p>In order to control the timer threads this class should be started and stopped.
*
* <p>This files set recurses through subfolders by default and is synchronized.
*
* <p>This class does not support signed resources.
*
* @author Lars J. Nilsson
* @version ##URD-VERSION##
*/

public class WatchedFileSet extends RegexpFileSet implements WatchableFileSet {

    /**
    * Current timer task.
    */
    
    private TimerTask currentTask;
    
    /**
    * Timer that is used to check the file set for changes.
    */
    
    private Timer timer;

    /**
    * Current listeners. This is a <i>java.lang.reflect.Proxy</i> object using
    * an <i>EventInvocationHandler</i> as a dispatcher.
    */
    
    private Proxy listeners;
    

    /// --- CONTRUCTORS --- ///

    /**
    * Contruct the file set using a regular expression and an external timer. An <code>IOException</code>
    * will be thrown if the root folder does not exists (or is a file) and a <code>PatternSyntaxException</code> if the
    * regular expression is invalid.
    *
    * @param root File set root folder, must exist
    * @param Perl 5 regular expression ti filter resources by
    * @param timer Timer to use for re-scans
    * @throws IOException If the file set root does not exist, or is a file
    * @throws PatternSyntaxException If the regular expression is not valid
    */
    
    public WatchedFileSet(File root, String regexp, Timer timer) throws IOException, PatternSyntaxException {
        super(root, regexp);
        listeners = (Proxy)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { FileSetListener.class }, new EventInvoker()); 
        this.timer = timer;
        this.currentTask = null;
    }

    /**
    * Contruct the file set using a regular expression but without an external timer. An <code>IOException</code>
    * will be thrown if the root folder does not exists (or is a file) and a <code>PatternSyntaxException</code> if the
    * regular expression is invalid.
    *
    * @param root File set root folder, must exist
    * @param Perl 5 regular expression ti filter resources by
    * @throws IOException If the file set root does not exist, or is a file
    * @throws PatternSyntaxException If the regular expression is not valid
    */
    
    public WatchedFileSet(File root, String regexp) throws IOException, PatternSyntaxException {
        this(root, regexp, null);
    }
    
    /**
    * Contruct the file without a regular expression or without an external timer. An <code>IOException</code>
    * will be thrown if the root folder does not exists (or is a file).
    *
    * @param root File set root folder, must exist
    * @throws IOException If the file set root does not exist, or is a file
    */
    
    public WatchedFileSet(File root) throws IOException {
        super(root);
    }
    
    
    
    /// --- PUBLIC ACCESSORS --- ///
    
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
    
    
    
    /// --- PRIVATE HELPER METHODS --- ///
    
    /**
    * Perform re-scan to check for changes.
    */
    
    private void checkScan() {
        // don't waste time if there is no listeners
        if(((EventInvoker)listeners.getInvocationHandler(listeners)).size() > 0) {   
            List events = new LinkedList();
            int count = 0;
            try {
                scanImpl(root, events, count);
                checkRemoved(count, events);
                fireEvents(events);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
    * Do a complete scan for changes and place eventual change events
    * in the provided list
    */
    
    private void scanImpl(File folder, List eventList, int count) throws IOException {
        File[] files = folder.listFiles();   
        for(int i = 0; i < files.length; i++) {
            if(!files[i].isFile() && super.isRecursive()) scanImpl(files[i], eventList, count);
            else {
                String path = resolvePath(files[i]);
                if(super.matches(path)) {
                    ResourceBase rec = (ResourceBase)super.getResource(path);
                    if(rec == null) eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_ADDED, this));
                    else if(rec.getVisitedTime() < files[i].lastModified()) eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_CHANGED, this, rec));
                    count++;
                }
            }
        }
    }
    
    /**
    * One or more resources may have been removed. Check all available resources if they still ares
    * valid and note those that are not.
    */
    
    private synchronized void checkRemoved(int count, List eventList) {
        if(count < resources.size()) {
            for(Iterator i = resources.values().iterator(); i.hasNext(); ) {
                ResourceBase rec = (ResourceBase)i.next();
                if(!rec.exists()) eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_REMOVED, this, rec));
            }
        }
    }
    
    /**
    * Fire a list of <i>FileSetEvent</i>s to the listeners. This method synchronizes
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
}