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

import java.util.*;
import java.util.regex.*;

import net.larsan.norna.NamespaceID;
import net.larsan.norna.ServiceController;
import net.larsan.norna.ServiceList;
import net.larsan.norna.SearchProperties;
import net.larsan.norna.ServiceProperties;
import net.larsan.norna.SoftwareInfo;
import net.larsan.norna.Status;
import net.larsan.norna.FrameworkEnvironment;
import net.larsan.norna.RegistryListener;
import net.larsan.norna.ServicePermission;
import net.larsan.norna.ServiceRegistry;
import net.larsan.norna.base.ServiceHandle;
import net.larsan.norna.NoSuchServiceException;
import net.larsan.norna.service.log.*;
import net.larsan.norna.util.*;
import net.larsan.urd.util.*;
import net.larsan.urd.jndi.*;
import javax.naming.*;
import java.security.*;

/**
 * Urd {@link net.larsan.norna.ServiceRegistry ServiceRegistry} implementation.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */


/// TO-DO: check returns on getservice, control and search for permission to get service


public class Registry implements ServiceRegistry {
    
    private Context context;
    private ErrorHandler handler;
    private SecurityManager manager;
    private List containers;
    private RegEventQueue queue;
    
    /**
     * ID mapping of IDs -> urls. Since IDs not necessarry unique
     * they're prepended with their container name.
     */
    
    private Map idMap;


    /**
     * Create a registry based on a root context.
     * 
     * @param rootContext Context to use, must not be null
     */

	public Registry(Context rootContext) throws NamingException {
        if(rootContext == null) throw new IllegalArgumentException("null context");
        manager = System.getSecurityManager();
		this.context = rootContext;
        Executor exec = findExecutor();
        queue = new RegEventQueue(exec);
        handler = findHandler();
        containers = new ArrayList(5);
        idMap = new HashMap();
        scan();
	}


    /// --- PUBLIC CONTRACT --- ///

    public void addRegistryListener(RegistryListener listener) { 
        queue.addRegistryListener(listener);
    }
    
    
    public void addRegistryListener(SearchProperties filter, RegistryListener listener) { 
        queue.addRegistryListener(filter, listener);
    }
    
    
    public void removeRegistryListener(RegistryListener listener) { 
        queue.removeRegistryListener(listener);
    }


	public ServiceList search(final SearchProperties attributes) throws PatternSyntaxException {
        final Set answer = new HashSet();
        class Visitor extends ContextVisitor {
            
            Searcher searcher;
            
            Visitor() {
                super(Registry.this.context, Registry.this.handler);
                searcher = new Searcher(attributes);
            }
            
            public void visit(String name, Object o) {
                Servicebox box = (Servicebox)o;
                if(searcher.matches(name, box.getService().getServiceInfo())) {
                    try {
                        answer.add(box.getNamespaceId());   
                    } catch(Exception e) {
                        super.sysLog.systemError("Failed to get service name: " + e.getMessage());   
                    }
                }
            }   
        } 
        return (ServiceList)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Visitor v = new Visitor(); 
                v.start(new Class[] { Servicebox.class });
                final List list = new ArrayList(answer.size());
                for(Iterator i = answer.iterator(); i.hasNext(); ) {
                    NamespaceID url = (NamespaceID)i.next();
                    try {
                        list.add(getService(url.toString()));
                    } catch(NoSuchServiceException e) { }
                }
                return new ServiceList() {
                    public int size() {
                        return list.size();   
                    }
                    public ServiceProperties get(int i) {
                        return (ServiceProperties)list.get(i);
                    }
                };
            }   
        });
	}


    public ServiceList listAll() {
		return search(new SearchPropertiesImpl());
	}
    

	public ServiceProperties getService(final String url) throws NoSuchServiceException {
		//if(manager != null) {
        //    try {
        //        manager.checkPermission(new ServicePermission(url, "get"));   
        //    } catch(SecurityException e) {
        //        return null; // EARLY RETURN
        //    }
        //}
        ServiceProperties props = (ServiceProperties)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    final Object o = ContextUtils.nullReturnLookup(context, url);
                    if(o instanceof Servicebox) {
                        final SoftwareInfo info = ((Servicebox)o).getService().getServiceInfo();
                        return new ServiceProperties() {
                            
                            public String getPublicId() { return info.getPublicId(); }
    
                            public String getSoftwareName() { return info.getSoftwareName(); }
            
                            public String getOriginator() { return info.getOriginator(); }
        
                            public String getRelease() { return info.getRelease(); }
    
                            public double getVersion() { return info.getVersion(); }
        
                            public String getDescription() { return info.getDescription(); }
                            
                            public Class[] getBaseContract() {
                                return ((Servicebox)o).getMetaData().baseClasses();
                            }

                            public String getNamespaceId() {
                                return ((Servicebox)o).getNamespaceId().getNamespaceId();
                            }

                            public Status getStatus() {
                                return ((Servicebox)o).getStatus();   
                            }
                            
                        };
                    } else return null;
                } catch(NamingException e) {
                    logError("Failed to get service", e);
                    return null;   
                }
            }   
        });
        if(props == null) throw new NoSuchServiceException("copuld not find service", url);
        else return props;
	}
    

    public ServiceController getControl(final String url) throws NoSuchServiceException {
        Servicebox box = null;
        try {
            box = (Servicebox)AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    try {
                        Servicebox box = (Servicebox)ContextUtils.nullReturnLookup(context, url);   
                        if(box == null) throw new NoSuchServiceException("Service \'" + url + "\' not found", url);
                        else return box;
                    } catch(NamingException e) {
                        logError("Failed to find service", e);
                        throw new NoSuchServiceException("Service \'" + url + "\' not found", url);
                    }
                }
            });
        } catch(PrivilegedActionException e) {
            throw (NoSuchServiceException)e.getCause();   
        }
        
        final Servicebox fBox = box;

        return new ServiceController() {
            
            public void start() { 
                if(manager != null) manager.checkPermission(new ServicePermission(url, "start"));
                if(permitStart(fBox.getStatus())) fBox.start();
            }

            public void stop() { 
                if(manager != null) manager.checkPermission(new ServicePermission(url, "stop"));
                if(permitStop(fBox.getStatus())) fBox.stop();
            }
            
            private boolean permitStop(Status status) {
                return (status == Status.READY || status == Status.STARTING);
            }

            private boolean permitStart(Status status) {
                return (status == Status.CREATED || status == Status.INITIALIZED || status == Status.LOADED || status == Status.STOPPED);
            }
        };
    }

	
    
    
    
    /// --- PACKAGE METHODS --- ///
    
    /**
     * Get a reference to the environment.
     */

    FrameworkEnvironment getEnvironment() {
        return UrdEnvironment.getInstance();   
    }
    
    /**
     * Start registry
     */
    
    void start() {
        queue.start("Urd Registry Event Queue");    
    }
    
    /** 
     * Stop registry
     */
    
    void stop() {
        queue.stop();
    }
    
    /**
     * Get registry event queue
     */
    
    RegEventQueue getQueue() {
        return queue;   
    }
    
    
    
    /// --- PRIVATE HELPER METHODS --- ///
    
    
    /**
     * Get the error handler from the context. Return a new default handler
     * if we cannot find it.
     */
    
    private ErrorHandler findHandler() throws NamingException {
        return (ErrorHandler)context.lookup("/urd/handler"); 
    }
    
    
    /**
     * Find executor in the context. Should it fail we'll throw an
     * illeagl state exception.
     */
    
    private Executor findExecutor() {
        try {
            return (Executor)context.lookup("/urd/threads");   
        } catch(NamingException e) {
            throw new IllegalStateException("could not find thread pool");
        }   
    }
    
    
    /** 
     * Scan the JNDI context for available services. Do this by getting
     * hold of every container and ask it nicely for a listing. Then
     * put the result in the id map.
     */
    
    private synchronized void scan() {
        AccessController.doPrivileged(new PrivilegedAction() { 
            public Object run() {
                containers.clear();
                idMap.clear();
                class Visitor extends ContextVisitor {
                    Visitor() {
                        super(Registry.this.context, Registry.this.handler);   
                    }   
                    
                    public void visit(String name, Object o) {
                        containers.add(name);
                        Container cont = (Container)o;   
                        ListInfo[] info = cont.list();
                        for (int i = 0; i < info.length; i++) {
                            SoftwareInfo tmp = info[i].getServiceInfo();
        					idMap.put(name + "/" + tmp.getPublicId(), info[i].getServiceURL());
        				}
                    }
                }
                new Visitor().start(new Class[] { Container.class });
                return null;
            }
        });
    }
    
    
    // logga ett fel-medelande
    private void logError(String msg, Throwable th) {
        handler.systemError("Registry error: " + msg + "; " + th.getMessage());
    }
    
    
    /// --- INNER CLASSES --- ///
    
    /**
     * A simple class that precompiles search criteria for
     * the search methdod.
     */
    
    static class Searcher { 
        
        private Pattern url;
        private HashMap patterns;
        
        public Searcher(SearchProperties props) throws PatternSyntaxException {
            patterns = new HashMap();
            String tmp = null;
            url = ((tmp = props.getNamespaceId()) != null ? Pattern.compile(tmp) : null);
            if((tmp = props.getName()) != null) patterns.put("name", Pattern.compile(tmp));
            if((tmp = props.getPublicId()) != null) patterns.put("puid", Pattern.compile(tmp));
            if((tmp = props.getOriginator()) != null) patterns.put("originator", Pattern.compile(tmp));
            if((tmp = props.getRelease()) != null) patterns.put("release", Pattern.compile(tmp));
            if((tmp = props.getVersion()) != null) patterns.put("version", Pattern.compile(tmp));
        }
        
        public boolean matches(String name, SoftwareInfo info) {
            if(url != null && !url.matcher(name).matches()) return false;
            for(Iterator i = patterns.keySet().iterator(); i.hasNext(); ) {
                String nam = (String)i.next();   
                String value = getValue(nam, info);
                Pattern pattern = (Pattern)patterns.get(nam);
                if(value != null && !pattern.matcher(value).matches()) {
                    return false;
                }
            }
            return true;
        }
        
        private String getValue(String name, SoftwareInfo info) {
            if(name.equals("puid")) return info.getPublicId();
            else if(name.equals("name")) return info.getSoftwareName();
            else if(name.equals("originator")) return info.getOriginator();
            else if(name.equals("release")) return info.getRelease();
            else if(name.equals("version")) return String.valueOf(info.getVersion());
            else return null;
        }
    }
}
