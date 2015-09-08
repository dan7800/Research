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

package net.larsan.urd.impl;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.naming.*;

import net.larsan.norna.*;
import net.larsan.norna.Status;
import net.larsan.norna.NoSuchServiceException;
import net.larsan.urd.jndi.*;
import net.larsan.norna.base.*;
import net.larsan.urd.util.*;
import net.larsan.urd.cmd.*;
import net.larsan.urd.*;

/**
* This is the main Urd server class. This class does not do any preparations to its
* execution but asumes that all needed objects will be available in the Urd JNDI 
* context.
* 
* <p>The context must be prepared by this class environment. In particular this server
* will search the context for the following objects:
* 
* <pre>
*      /urd/classloader/shared (shared services class loader)
*      /urd/threads (the framework thread pool)
* </pre>
* 
* All of the above objects must be available before this server can start. And should
* any of them be missing the execution will immediately halt. The server will also look
* for the following objects, but will use default values if they are not found:
* 
* <pre>
*      /urd/handler (current error handler; default: stderr)
*      /urd/classloader/system (system framework class loader; default: current class loader)
*      /urd/classloader/service (service root classloader; default: parent class loader)
* </pre>
* 
* The command line server takes its default values (such as address and port) from the preference
* object from the JNDI context ('/urd/pref').
*
* @author Lars J. Nilsson
* @version ##URD-VERSION##
*/

public class UrdServer implements Runnable {

    private boolean runFlag;
    private Thread hook;
    private Proxy listeners;
    private javax.naming.Context context;
    private Registry registry;
    private ErrorHandler handler;

    /**
    * Create server
    */

    public UrdServer(javax.naming.Context context) {
        hook = new Thread() {
            public void run() {
                disableHook();
                kill();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        listeners = (Proxy)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { UrdServerListener.class }, new EventInvoker());
        this.runFlag = true;
        try {
            this.context = context;
            handler = (ErrorHandler)context.lookup("/urd/handler");
            registry = new Registry(context);
        } catch(NamingException e) {
            throw new IllegalStateException("Failed to get initial context: " + e.getMessage());
        }
        try {
            if(Constants.isVerbose()) System.out.println("Binds service registry at \"/urd/registry\"");
            ContextUtils.creationBind(context, "/urd/registry", registry);
        } catch(NamingException e) {
            //e.printStackTrace();
            throw new IllegalStateException("Failed to bind registry: " + e.getMessage());
        }
    }
    
    
    /**
    * Run server
    */

    public void run() {
        registry.start();
        doAutoStarts();
        fireServerStarted();
        handler.systemMessage("Startup sequence finished");
        while(getRunFlag()) {
            synchronized(this) {
                try {
                    this.wait();
                } catch(InterruptedException e) { }
            }
        }
        handler.systemMessage("Server shutdown started");
        fireServerStopped();
    }
    
    
    /**
     * Get the service registry.
     */
    
    public Registry getRegistry() {
        return registry;   
    }
    
    
    /**
     * Add listener to this server.
     */    
    
    public void addServerListener(UrdServerListener list) {
        ((EventInvoker)listeners.getInvocationHandler(listeners)).addListener(list);
    }
    
    
    /** 
     * Remive listener from this server
     */
    
    public void removeServerListener(UrdServerListener list) {
        ((EventInvoker)listeners.getInvocationHandler(listeners)).removeListener(list);
    }
    
    
    /**
    * Kill the server
    */
    
    public void kill() {
        new StopAllTask(context, handler).run();
        new Destruction().run();
        cleanup();
    }
    
    
    /**
     * Cleanup resources
     */
    
    protected synchronized void cleanup() {
        if(hook != null) Runtime.getRuntime().removeShutdownHook(hook);
        if(registry != null) registry.stop();
        disableHook();
        runFlag = false;
        this.notify();
    }
    
    
    /**
    * Shutdown server. 
    * 
    * @param millis Millisecond timeout for shutdown event
    */
    
    public void shutdown(long millis) {
        try {
            Executor exec = (Executor)context.lookup("/urd/threads");
            exec.execute("Urd Shutdown Visitor", new Shutdown(millis));
        } catch(NamingException e) {
            handler.systemError("Failed to retrive urd thread pool: " + e.getMessage());   
        } 
    }
    

    /**
    * Get internal thread flag
    */

    protected synchronized boolean getRunFlag() {
        return runFlag;
    }
    
    /**
    * Set internal thread flag
    */
    
    protected synchronized void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }
    
    
    
    /**
    * Print all threads
    */
    
    public Thread[] dumpThreads() {
        Thread[] th = new Thread[Thread.activeCount()];
        Thread.enumerate(th);
        return th;
    }
    
    /**
    * Disable the system shutdown hook
    */
    
    private synchronized void disableHook() {
        this.hook = null;
    }
    
    /**
    * List service info
    */
    
    public ListInfo info(final String url) throws NoSuchServiceException {
        try {
            Servicebox box = (Servicebox)ContextUtils.nullReturnLookup(context, url);
            if(box != null) {
                final SoftwareInfo info = box.getService().getServiceInfo();
                final Status status = box.getStatus();
                final long start = box.getStartTime();
                return new ListInfo() {
                    public Status getStatus() { return status; };

                    public String getServiceURL() { return url; }
    
                    public SoftwareInfo getServiceInfo() { return info; }
    
                    public long startedAt() { return start; }
                };
            } else throw new NoSuchServiceException("Service not found", url);
        } catch(NamingException e) {
            handler.systemError("Failed to list service: " + url + "; " + e.getMessage());
            throw new NoSuchServiceException("Service not found", url);
        }
    }
    
    /**
    * List all services
    */
    
    public ListInfo[] list() {
        ArrayList list = new ArrayList(20);
        ServiceList sList = registry.listAll();
        for(int i = 0; i < sList.size(); i++) {
            final ServiceProperties prop = sList.get(i);   
            try {
                Servicebox box = (Servicebox)ContextUtils.nullReturnLookup(context, prop.getNamespaceId());
                if(box != null) {
                    final SoftwareInfo info = box.getService().getServiceInfo();
                    final Status status = box.getStatus();
                    final long start = box.getStartTime();
                    list.add(new ListInfo() {
                        public Status getStatus() { return status; };

                        public String getServiceURL() { return prop.getNamespaceId(); }
    
                        public SoftwareInfo getServiceInfo() { return info; }
    
                        public long startedAt() { return start; }
                    });
                }
            } catch(NamingException e) {
                handler.systemError("Failed to get listing for url: " + prop.getNamespaceId() + "; " + e.getMessage());
            }
        }
        ListInfo[] answer = new ListInfo[list.size()];
        list.toArray(answer);
        return answer;
    }
    
    /**
    * Start service command
    */
    
    public void startService(String service) throws NoSuchServiceException { 
        registry.getControl(service).start();
    }
    
    /**
    * Stop service command
    */
    
    public void stopService(String service) throws NoSuchServiceException { 
        registry.getControl(service).stop();
    }



    /// --- PRIVATE HEMPER METHODS --- ///
    
    // fire stop event to listeners
    private void fireServerStopped() {
        //System.out.println("FIRING STOP");
        ((UrdServerListener)listeners).serverStopped();
    }
    
    
    // fire start event to listeners
    private void fireServerStarted() {
        //System.out.println("FIRING START");
        ((UrdServerListener)listeners).serverStarted();
    }
    
    
    // do auto-starts
    private void doAutoStarts() {
        AutoStartTask task = new AutoStartTask(context, handler);
        task.run();
    }
    
    
    /// --- INNER CLASSES --- ///
    
    /**
     * Runnable for shutdown handling. When this target is run it first noties
     * all available shutdown {@link net.larsan.norna.base.ShutdownListener listeners} and
     * then calls {@link #kill kill} on the server instance.
     */
    
    private class Shutdown extends ContextVisitor implements Runnable {
        
        long timeout;
        
        /**
         * Create a shutdown object with a timeout in milliseconds
         * 
         * @param timeout Shutdown timeout in millis
         */
        
        public Shutdown(long timeout) {
            super(UrdServer.this.context, UrdServer.this.handler); 
            this.timeout = timeout;  
        }
        
        /**
         * Execute shutdown
         */
        
        public void run() { 
            super.start(new Class[] { ShutdownListener.class });
            try {
                Thread.currentThread().sleep(timeout);
            } catch(InterruptedException e) { }
            UrdServer.this.kill();
        }   
        
        /**
         * Inherited from the context {@link net.larsan.urd.jndi.ContextVisitor visitor}.
         */
        
        public void visit(String name, Object o) { 
            try {
                ((ShutdownListener)o).shutdownWarning(timeout);
            } catch(Throwable th) {
                super.sysLog.systemError("Exception while delivering shutdown warning: " + name + "; " + th.getMessage());
            }
        }
    }
    
    
    /**
     * Runnable for contextual object destruction. This will hit all 
     * {@link Destroyable destroyable} object in the current context.
     */
    
    private class Destruction extends ContextVisitor implements Runnable {
        
        public Destruction() {
            super(UrdServer.this.context, UrdServer.this.handler); 
        }
        
        /**
         * Execute destruction
         */
        
        public void run() { 
            super.start(new Class[] { Destroyable.class });
        }   
        
        /**
         * Inherited from the context {@link net.larsan.urd.jndi.ContextVisitor visitor}.
         */
        
        public void visit(String name, Object o) { 
            try {
                ((Destroyable)o).destroy();
            } catch(Throwable th) {
                super.sysLog.systemError("Exception during destruction: " + name + "; " + th.getMessage());
            }
        }
    }
}