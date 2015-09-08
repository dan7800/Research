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
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import net.larsan.norna.NoSuchServiceException;
import net.larsan.urd.*;
import net.larsan.urd.util.*;
import net.larsan.urd.util.threadpool.*;
import net.larsan.urd.archive.*;
import net.larsan.urd.jndi.*;
import net.larsan.urd.cmd.*;
import net.larsan.urd.conf.*;
import net.larsan.urd.syslog.*;
import org.w3c.dom.*;
import java.util.*;

/**
 * The standalone is a Urd server which prepares the environment
 * before starting. It asumes that class loading is taken care of and if it have
 * not been explicitly told otherwise it will use it's own class loader as the
 * parent class loader for subservices.
 * 
 * <p>Immediate startup information needed by this standalone can be garthered
 * in a {@link CmdOptions CmdOptions} object.
 * 
 * <p>This server prepares the JNDI context for later use. It reservers the subcontext
 * 'urd' which is one of the prerequisites inherited from the {@link UrdServer}. In preparation
 * it binds the following objects in the context:
 * 
 * <pre>
 *      /urd/root (installation root, file object)
 *      /urd/config (configuration file object)
 *      /urd/handler (current error handler)
 *      /urd/classloader/system (system framework class loader)
 *      /urd/classloader/service (service root classloader)
 *      /urd/classloader/shared (shared services class loader)
 *      /urd/threads (the framework thread pool)
 * </pre>
 * 
 * When the above objects have sucessfully started the server will be startable. The proper
 * invocation chain for this standalone is:
 * 
 * <pre>
 *      prepare -> start (-> destroy)
 * </pre>
 * 
 * Server shutdown is handled by the base server. Destruction will be called when the
 * {@link UrdServer} shuts down.
 * 
 * <p>This class is not thread safe.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public class Standalone implements UrdServerListener {
    
    private UrdLog sysLog;
    private boolean isPrepared;
    private CmdOptions options;
    private ClassLoader serviceLoader;
    private Controller[] controllers;
    private UrdServer server;
    private NSEventQueue nsEvents;
    //private String[] autostarts;

	public Standalone(CmdOptions options, ClassLoader serviceLoader) throws IOException {
        this.serviceLoader = serviceLoader;
        this.options = options;
        isPrepared = false;
    }
    
    public Standalone(CmdOptions options) throws IOException {
        this(options, Standalone.class.getClassLoader());   
    }

    public Standalone() throws IOException {
        this(CmdOptions.parse(new String[0]), Standalone.class.getClassLoader());   
    }
    
    
    
    /// --- LIFECYCLE CONTRACT --- ///
    
    /**
     * Prepare the server before a startup. This method will intialize all shared
     * objects for the framework. In particular it will:
     * 
     * <ul>
     *  <li>Attempt to find and parse configuration files</li>
     *  <li>Start intermediate class loaders</li>
     *  <li>Create and initialize server preferences</li>
     *  <li>Create and start framework thread pool</li>
     * </ul>
     * 
     * Since all objects mentioned above will live in memory it is vital that
     * a call to {@link #destroy destroy} is made after server execution to clean up 
     * resources.
     *
     * @throws IOException On read I/O errors
     * @throws ConfigurationException On configuration errors
     * @throws NamingException On fatal JNDI errors
     */
    
    public void prepare() throws IOException, ConfigurationException, NamingException {
        
        Context con = new InitialContext(Constants.JNDI_ENVIRONMENT);
        
        prepareSyslog(con);
        prepareThreads(con);
        prepareNSEvents(con);
        prepareLoaders(con);
        prepareConfig(con);
        prepareRootDirectory(con);

        isPrepared = true;
    }
    
    
    /**
     * Start the execution of this server. Execution logic is handled by the 
     * {@link UrdServer} and this method will return immediately.
     * 
     * <p>Should {@link #prepare prepared} not have been called before this
     * method is invoked an <code>IllegalStateException</code> will be thrown.
     * 
     * @throws IllegalStateException If the server is not ready
     */
    
    public void start() throws ConfigurationException, IOException, NamingException {
        if(!isPrepared) throw new IllegalStateException("The server must be prepared before starting");
        
        server = new UrdServer(RootContext.CONTEXT);
        doConfig(RootContext.CONTEXT);
        
        // attach controllers
        for(int i = 0; i < controllers.length; i++) {
            controllers[i].setContext(RootContext.CONTEXT);
			controllers[i].attachServer(server);
        }
        
        // listen and start
        server.addServerListener(this);
        if(Constants.isVerbose()) System.out.println("Starts Urd Server");
        Thread th = new Thread(server);
        th.setName("Urd Server main thread");
        th.start();
        
        // try autostarts
        /*Registry reg = server.getRegistry();
        for (int i = 0; i < autostarts.length; i++) {
			try {
                reg.start(autostarts[i]);
            } catch(NoSuchServiceException e) {
                throw new InternalError("failed to auto-start service: " + e.getMessage());
            }
		}*/
    }

    
    
    /**
     * Clean up all available resources. This include closing the JNDI root context, releasing
     * the thread pool and commiting the preferences.
     * 
     * <p>Should {@link #prepare prepared} not have been called before this
     * method is invoked an <code>IllegalStateException</code> will be thrown.
     */
    
    public void destroy() {
        if(!isPrepared) throw new IllegalStateException("Server not prepared or already destroyed");

        clearSyslog();
        clearThreadPool();
        clearRootContext();
        
        server.removeServerListener(this);
        for(int i = 0; i < controllers.length; i++) {
            if(controllers[i] instanceof UrdServerListener) {
                server.removeServerListener((UrdServerListener)controllers[i]);
            }
        }
              
        isPrepared = false;
        
        /*try {
            Thread.currentThread().sleep(2000);
        } catch(Exception e) { }
        
        Thread[] th = new Thread[Thread.activeCount()];
        Thread.enumerate(th);
        
        System.out.println("XXX " + Thread.currentThread().getName());
        for (int i = 0; i < th.length; i++) {
			if(th[i] != null) System.out.println(th[i].getName());
		}*/
        
        System.out.println("*** Urd Standalone Stopped");
        System.exit(0);
    }
    
    
    /**
     * Urd server has started, this does not interest us.
     */

    public void serverStarted() { }


    /**
     * Urd server has stopped, close and destroy
     */
    
    public void serverStopped() {
        destroy();
    }
    
    
    
    /// --- PRIVATE HELPER METHODS --- ///
    
    
    /**
     * Find the configuration file and parse it
     */
    
    private void doConfig(Context con) throws IOException, ConfigurationException {
        File config = new File(options.getConfDir(), "services.xml");
        if(!config.exists()) throw new ConfigurationException("Config file \'" + config.toString() + "\' not found");
        if(Constants.isVerbose()) System.out.println("Parses configuration file: " + config.toString());
        int count = 0;
        try {
            ArrayList contr = new ArrayList(3);
            String rawDoc = IOUtils.toString(config);
            rawDoc = StringUtils.substituteSystemProperties(rawDoc);
            Document doc = XMLUtils.parseString(rawDoc);
            Node rootNode = doc.getFirstChild();
            if(rootNode == null || !rootNode.getNodeName().equals("urd")) throw new XMLException("Missing root node \'urd\'");
            NodeList list = rootNode.getChildNodes();
            Map factories = new HashMap();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
				NamedNodeMap attr = node.getAttributes();
                if(attr != null) {
    			    Node fact = attr.getNamedItem("factory-class");
                    if(fact == null) fact = attr.getNamedItem("factoryClass");
                    if(fact != null) {
                        count++;
                        String cl = fact.getNodeValue();
                        ComponentFactory factory = null;
                        if(!factories.containsKey(cl)) {
                            Class clazz = Class.forName(cl);
                            factory = (ComponentFactory)clazz.newInstance();
                            factories.put(cl, factory);
                        } else factory = (ComponentFactory)factories.get(cl);
                        Object o = factory.createComponent(XMLUtils.copyAttributeProperties(node), node.getChildNodes());
                        if(o instanceof Controller) contr.add(o);
                    }
                }
            }
            controllers = new Controller[contr.size()];
            contr.toArray(controllers);
            
            // find auto starts
            //autostarts = findAutoStarts(doc);
        } catch(InstantiationException e) {
            throw new ConfigurationException("Could not create factory instance", e);
        } catch(ClassNotFoundException e) {
            throw new ConfigurationException("Factory class not found", e);
        } catch(IllegalAccessException e) {
            throw new ConfigurationException("Illegal factory class modifiers", e);
        } catch(IllegalStateException e) {
            throw new ConfigurationException("Internal state error", e);
        } catch(IllegalArgumentException e) {
            throw new ConfigurationException("Internal argument error", e);
        } catch(XMLException e) {
            throw new ConfigurationException("Could not parse configuration", e);
        }
        if(Constants.isVerbose()) {
            System.out.println("Standalone configuration found:");
            System.out.println("   " + (count - controllers.length) + " factory components");
            System.out.println("   " + controllers.length + " controller");
        }
    }
    
    
    
    /**
     * Start shared clas loader and put all three classloaders into the
     * context.
     */
    
    private void prepareLoaders(Context con) throws NamingException {
        if(Constants.isVerbose()) System.out.println("Binds class loaders");
        ContextUtils.creationBind(con, "/urd/classloader/system", getClass().getClassLoader());
        ContextUtils.creationBind(con, "/urd/classloader/shared", new SharedSpaceLoader(this.serviceLoader));
        ContextUtils.creationBind(con, "/urd/classloader/service", this.serviceLoader);
    }
    

    /**
     * Setup the default error handler
     */
    
    private void prepareSyslog(Context con) throws NamingException, IOException {
        if(Constants.isVerbose()) System.out.println("Binds error handler");
        UrdLog handler = new UrdLog(new File(options.getRoot(), "logs" + File.separator + "system"));
        this.sysLog = handler;
        sysLog.systemMessage("System log started");
        ContextUtils.creationBind(con, "/urd/handler", handler);
    }
    
    
    
    /**
     * Set config file in context root
     */
    
    private void prepareConfig(Context con) throws NamingException, IOException {
        if(Constants.isVerbose()) System.out.println("Binds configuration at \"/urd/config\"");
        ContextUtils.creationBind(con, "/urd/config", new File(options.getConfDir(), "services.xml"));
    }
    
    
    /**
     * Set NS event queue in motion
     */
    
    private void prepareNSEvents(Context con) throws NamingException {
        if(Constants.isVerbose()) System.out.println("Starts JNDI context event queue");
         Executor exec = (Executor)ContextUtils.nullReturnLookup(con, "/urd/threads");
         if(exec != null) {
            nsEvents = new NSEventQueue(exec);
            nsEvents.start();
            RootContext.CONTEXT.setHandler(new EventHandler(nsEvents));
         } else throw new IllegalStateException("could not find thread pool");
    }
    
    
    /**
     * Set the root file in the JNDI context
     */
    
    private void prepareRootDirectory(Context con) throws NamingException, IOException {
        if(Constants.isVerbose()) System.out.println("Binds root dir: " + options.getRoot().toString());
        ContextUtils.creationBind(con, "/urd/root", options.getRoot());
    }
    
    
    /**
     * Start the framework thread pool and post it to the context
     */
    
    private void prepareThreads(Context con) throws NamingException {
        if(Constants.isVerbose()) System.out.println("Creating thread pool");
        ContextUtils.creationBind(con, "/urd/threads", new Threads(getClass().getClassLoader()));   
    }
    
    
    /**
     * Take down and close the thread pool
     */
    
    private void clearThreadPool() {
        if(Constants.isVerbose()) System.out.println("Clearing thread pool");
        try {
            Context con = new InitialContext(Constants.JNDI_ENVIRONMENT);
            Threads th = (Threads)con.lookup("/urd/threads");
            con.unbind("/urd/threads");
            th.destroy();
        } catch(NamingException e) {
            e.printStackTrace();   
        }   
    }
    
    /**
     * Clear the root context
     */
    
    private void clearRootContext() {
        if(Constants.isVerbose()) System.out.println("Clearing root context");
        try {
            RootContext.CONTEXT.clear();
        } catch(NamingException e) {
            e.printStackTrace();   
        }   
        if(nsEvents != null) nsEvents.stop();
    }
    
    
    /**
     * Clear the system log
     */
    
    private void clearSyslog() {
        if(Constants.isVerbose()) System.out.println("Clearing system log");
        sysLog.systemMessage("System log closed");
        sysLog.destroy();
    }

}