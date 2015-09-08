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

package net.larsan.urd.service.user;

import java.io.*;
import java.util.*;

import net.larsan.norna.*;
import net.larsan.norna.Status;
import net.larsan.norna.StatusCallback;
import net.larsan.norna.UnavailableException;
import net.larsan.norna.InitParameters;
import net.larsan.norna.base.*;
import net.larsan.norna.service.user.*;
import net.larsan.norna.syslog.*;

import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;
import javax.naming.*;

import net.larsan.urd.Constants;
import net.larsan.urd.impl.*;

import net.larsan.urd.util.*;

/**
 * This is the main Urd user service. This service uses JAAS to authenticate its 
 * principals. To determine which JAAS login context to use, use the 'context'
 * init parameter:
 * 
 * <pre>
 *     <init-param name="context" value="MySpecialContext" />
 * </pre>
 * 
 * The login context default to 'SimpleUrdContext'. This service works with name and
 * password callbacks fro ordinary authentications. For indirect authentications the login
 * module must use an <code>IndirectionCallback</code>.
 * 
 * <p>Available indirections can be registered with the service whrough the 'indirections'
 * initiation parameter. This paramtere takes a comma separated value of class names
 * for the indirections to use. For example:
 * 
 * <pre>
 *     <init-param name="indirections" value="net.larsan.norna.service.user.NullIndirection,
 *                                            com.mycompany.MyHashIndirection" />
 * </pre>
 * 
 * <p>This service is dependent on the Urd JNDI context and must have a context permission
 * in order to use it.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public class ServiceImpl implements Service, Initializable {
                                    
                                    
    /// --- PERMISSIONS --- ///
    
    private final static UserPermission permission = new UserPermission("getAuthenticator");
    
    
    /// --- INSTANCE MEMBERS --- ///

	private Handle handle;          
    private StatusCallback callback;     
    private Factory factory;
    private SystemLog log;
    private String context;     
    private Status status;


    public ServiceImpl() { 
        status = Status.CREATED;
    }


    /// --- SERVICE --- ///

    public void setStatusCallback(StatusCallback callback) {
        this.callback = callback;
        setStatus(status);
    }

    public SoftwareInfo getServiceInfo() {
        return new SoftwareInfo() {
            public String getPublicId() {
                return "http://larsan.net/ns/net.larsan.urd.service.user.Service";
            }
            public String getSoftwareName() { return "Urd User Servive"; }
            public String getOriginator() { return "The Norna Team"; }
            public String getRelease() { return "RC1"; }
            public double getVersion() { return 1.0; }
            public String getDescription() { return "This is the default Urd user service."; }
        };
    }

    public ServiceHandle getServiceHandle() {
        return handle;
    }


    /// --- INITIALIZABLE --- ///

    public void init(ServicePlatform platform) throws UnavailableException {
        checkContext();
        handle = new Handle();
        log = platform.getSystemLog();
        InitParameters param = platform.getInitParameters();
        this.context = param.getParameter("context", "SimpleUrdContext");
        String inds = param.getParameter("indirections", "net.larsan.norna.service.user.NullIndirection");
        createFactory(StringUtils.tokenize(inds, ","));
        setStatus(Status.INITIALIZED);
        setStatus(Status.READY);
    }

    public void destroy() {
        handle = null;
        if(factory != null) factory.destroy();
        factory = null;
        setStatus(Status.DESTROYED);
        context = null;
        callback = null;  
        log = null;
    }
    
    
    
    /// --- PRIVATE METHODS --- ///
    
    /// create indirection factory
    private void createFactory(String[] classes) throws UnavailableException {
        Factory fac = new Factory();
        for (int i = 0; i < classes.length; i++) {
			try {
                fac.add(classes[i].trim());
            } catch(Exception e) {
                handleException(e);
                throw new UnavailableException("could not create indirection factory");
            }
		}
        this.factory = fac;
    }
    
    
    /// set status
    private void setStatus(Status status) {
        callback.newStatus(status);
        this.status = status;
    }
    

    // handle login exception
    private void handleException(Exception e) {
        if(log != null) log.record(EntryType.ERROR, "User Service: Login Exception: " + e.getMessage());
    }
    
    
    // fire event to listeners
    private void fireEvent(LoginListener[] list, LoginEvent ev) {
        for (int i = 0; i < list.length; i++) {
			try {
                list[i].receiveLoginEvent(ev);
            } catch(Throwable th) { }   
		}
    }
    
    
    // check that we have access to the context
    private void checkContext() throws UnavailableException {
        try {
            javax.naming.Context con = new InitialContext(Constants.JNDI_ENVIRONMENT);
            File file = (File)ContextUtils.nullReturnLookup(con, "/urd/root"); 
            if(file == null) throw new UnavailableException("could not find root directory in JNDI context");
        } catch(NamingException e) {
            throw new UnavailableException("failed to access JNDI context: " + e);
        }   
    }
    
    
    /// --- INNER CLASSES --- ///
    
    /// service handle
    private class Handle implements UserHandle {
        
        public Status getStatus() { return status; }
        
        public Authenticator getAuthenticator(String user) {
            SecurityManager man = System.getSecurityManager();
            if(man != null) man.checkPermission(permission);
            try {
                return new Checker(user, ServiceImpl.this.context);
            } catch(LoginException e) {
                handleException(e);
                return new Dummy();
            }
        }
        
        public Authenticator getAuthenticator(String user, Indirection ind) {
            SecurityManager man = System.getSecurityManager();
            if(man != null) man.checkPermission(permission);
            try {
                if(!ServiceImpl.this.factory.isInstance(ind)) throw new SecurityException("attempt to use non-local indirection");
                else return new Checker(user, ServiceImpl.this.context, ind);
            } catch(LoginException e) {
                handleException(e);
                return new Dummy();
            }
        }
        
        public IndirectionFactory getIndirectionFactory() {
            return factory;
        }
    }
    
    
    /// non-validating authenticator
    private static class Dummy implements Authenticator {
        
        public LoginSession authenticate(char[] pass) throws AuthenticationFailedException { 
            throw new AuthenticationFailedException("internal error: failed to create login context");
        }
        
        public LoginSession authenticate(char[] pass, LoginListener[] listeners) throws AuthenticationFailedException { 
            throw new AuthenticationFailedException("internal error: failed to create login context");
        }
        
        public Indirection getIndirection() {
            return null;   
        }
    }
    
    
    /// authenticator
    private class Checker implements Authenticator {
        
        private char[] cred;
        private String name;
        private LoginContext con;
        private Indirection ind;
        
        private Checker(String name, String context, Indirection ind) throws LoginException {
            this.con = new LoginContext(context, new CallbackCheck(ind));
            this.name = name;
            this.ind = ind;
        }   
        
        private Checker(String name, String context) throws LoginException {
            this(name, context, null);   
        }
        
        public synchronized LoginSession authenticate(final char[] cred) throws AuthenticationFailedException { 
            this.cred = cred;
            ind.setCredentials(cred);
            try {
                con.login();
            } catch(LoginException e) {
                throw new AuthenticationFailedException(e.getMessage());
            } finally {
                this.cred = null;
            }
            return new Session(con);
        }
        
        public LoginSession authenticate(char[] cred, LoginListener[] listeners) throws AuthenticationFailedException { 
            LoginSession ses = authenticate(cred);
            fireEvent(listeners, new LoginEvent(ses.getSubject()));
            return ses;   
        }
        
        public synchronized Indirection getIndirection() {
            return ind;   
        }
        
        private class CallbackCheck implements CallbackHandler {
            
            private Indirection ind;
            
            private CallbackCheck(Indirection ind) {
                this.ind = (ind == null ? new NullIndirection() : ind);   
            }
            
            public void handle(Callback[] calls) throws UnsupportedCallbackException {
                for (int i = 0; i < calls.length; i++) {
					if(calls[i] instanceof NameCallback) {
                        ((NameCallback)calls[i]).setName(name);
                    } else if(calls[i] instanceof PasswordCallback) {
                        ((PasswordCallback)calls[i]).setPassword(cred);
                    } else if(calls[i] instanceof IndirectionCallback) {
                        ((IndirectionCallback)calls[i]).setIndirection(ind);
                    } else throw new UnsupportedCallbackException(calls[i], "CallbackCheck");
				}
            }
        }
    }
    
    
    /// indirection factory
    private class Factory implements IndirectionFactory {
        
        private Map map = new HashMap();
        
        private synchronized void destroy() {
            map.clear();   
        }
        
        private synchronized void add(String className) throws ClassNotFoundException,
                                                                   InstantiationException,
                                                                   IllegalAccessException {

            Class cl = Class.forName(className);
            try {
                Indirection ind = (Indirection)cl.newInstance();
                map.put(ind.getID(), cl);
            } catch(ClassCastException e) {
                throw new ClassNotFoundException("class \'" + className + "\' no indirection");   
            } 
        }
        
        
        // check if an indirection is instanciated by us
        private synchronized boolean isInstance(Indirection ind) {
            if(!map.containsKey(ind.getID())) return false;
            else return ((Class)map.get(ind.getID())).isInstance(ind);
        }
        
        
        public synchronized Indirection createIndirection(String id) throws IndirectionUnavailableException {
            Class cl = (Class)map.get(id);
            if(cl == null) throw new IndirectionUnavailableException("Indirection \'" + id + "\' not found");
            else {
                try {
                    return (Indirection)cl.newInstance();
                } catch(Exception e) {
                    handleException(e);
                    throw new IndirectionUnavailableException("could not create indirection");
                }
            }
        }
    
        public synchronized String[] availableIndirections() {
            String[] arr = new String[map.size()];
            map.keySet().toArray(arr);
            return arr;
        }
    }
    

    /// context session
    private class Session implements LoginSession {
        
        private LoginContext context;
        
        private Session(LoginContext context) {
            this.context = context;
        }
     
        public Subject getSubject() {
            return context.getSubject();   
        }
    
        public void invalidate() {
            try {
                context.logout();   
            } catch(LoginException e) {
                handleException(e);   
            }
        }   
    }
}