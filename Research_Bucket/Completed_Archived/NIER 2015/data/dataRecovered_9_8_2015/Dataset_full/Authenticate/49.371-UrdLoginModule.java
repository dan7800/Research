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


package net.larsan.urd.service.user.jaas;

import java.io.*;
import java.util.*;
import java.security.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.callback.*;
import javax.naming.*;

import net.larsan.urd.Constants;
import net.larsan.norna.service.user.*;
import net.larsan.urd.util.FileUtils;


/**
 * This login module acts on the Urd user file. It has two optional 'option'
 * when configured:
 * 
 * <pre>
 *      file    - the Urd user file
 *      debug   - print debug info
 * </pre>
 * 
 * If the file option is not available this module will look for the
 * default file, which is the 'conf/users.xml' in the Urd man directory.
 * A file can be specified absolute or relative, in which case it is resolved
 * from the Urd directory.
 * 
 * <p>If debug is set to 'true' debug information will be printed to the
 * standard out.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public class UrdLoginModule implements LoginModule {
    
    private CallbackHandler callback;
    private UserRegistry registry;
    private Principal principal;
    private Indirection ind;
    private Subject subject;
    private String name;
    private boolean debug;
    private boolean committed;

	public void initialize(Subject subject, CallbackHandler callback, Map state, Map options) {
        if(callback == null) throw new IllegalArgumentException("null callback");
        this.callback = callback;
        this.subject = subject;
        
        String tmp = (String)options.get("debug");
        debug = (tmp != null && tmp.equals("true"));
        
        tmp = (String)options.get("file");
        if(tmp == null) tmp = "conf/users.xml";
        registry = UserRegistry.getInstance(findFile(tmp));
	}

	public boolean login() throws LoginException {
		if(debug) debug("Login");
        if(registry == null) return false;
        else {
            try {
                callback.handle(new Callback[] { new NameCaller(), new IndirectionCaller() });
            } catch(Exception e) {
                throw new LoginException("callback exception caught: " + e.getMessage());
            }
            try {
                principal = registry.getUser(name, ind);  
                return true;
            } catch(NoSuchUserException e) {
                throw new LoginException("no such user: " + e.getUser());
            } catch(AuthenticationFailedException e) {
                throw new LoginException("incorrect password");
            } finally {
                name = null;
                ind = null;
            }
        }   
	}

	public boolean commit() throws LoginException {
		if(debug) debug("Commit");
        if(principal == null) return false;
        else {
            if(!subject.getPrincipals().contains(principal)) {
                subject.getPrincipals().add(principal);   
            }
            committed = true;
            return true;
        }
	}

	public boolean abort() throws LoginException {
        if(debug) debug("Abort");
		if(principal == null) return false;
        else {
            if(!committed) principal = null;
            else logout();
            return true;
        }
	}
    
    public boolean logout() {
        if(debug) debug("Logout");
        subject.getPrincipals().remove(principal);
        principal = null;
        committed = false;
        return true;
    }
    
    
    
    /// --- PRIVATE METHODS --- ///
    
    /// print debug
    private void debug(String str) {
        System.out.println("UrdLoginModule: " + str);
    }
    
    
    /// find a registry file
    private File findFile(final String file) {
        return (File)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                File fil = FileUtils.resolveName(findRoot(), file);
                if(debug) debug("Uses file: " + fil);
                return fil;
            }
        });
        
    }
    
    // find the install root
    private File findRoot() {
        try {
            Context con = new InitialContext(Constants.JNDI_ENVIRONMENT);
            Object o = con.lookup("/urd/root");
            if(o == null || !(o instanceof File)) throw new IllegalStateException("Illegal root object");   
            else return (File)o;
        } catch(Exception e) {
            throw new IllegalStateException("Failed to retrieve root folder from context: " + e.getMessage());
        }   
    }


    /// --- INNER CLASSES --- ///
    
    /// name callback 
    private class NameCaller extends NameCallback {
        
        private NameCaller() {
            super("Please procide a user name");
        }   
        
        public void setName(String name) {
            UrdLoginModule.this.name = name;
            super.setName(name);
        }
    }
    
    
    /// password callback
    private class IndirectionCaller implements IndirectionCallback {
        
        public void setIndirection(Indirection ind) {
            UrdLoginModule.this.ind = ind;
        }
    }
}
