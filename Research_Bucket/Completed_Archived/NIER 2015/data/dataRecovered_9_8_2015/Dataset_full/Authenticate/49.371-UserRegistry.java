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
import org.w3c.dom.*;

import net.larsan.urd.util.*;
import net.larsan.norna.service.user.*;
import java.security.*;

/**
 * A simple user registry for the flat-file Urd user login. This class
 * parses an XML file and keeps a record of all users.
 * 
 * <p>This class checks changes in the underlying file and caches users
 * as long as the file is not changed.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */



/// TODO: Handle all exceptions gracefully



public class UserRegistry {
    
    /// --- STATIC MEMBERS --- ///
    
    private static Map instances = new HashMap();
    
    
    
    /// --- STATIC INTERFACE --- ///
    
    /**
     * Get a registry instance.
     * 
     * @return A registry instance, or null if the registry cannot be created 
     */
    
    public synchronized static UserRegistry getInstance(File file) {
        if(instances.containsKey(file)) return (UserRegistry)instances.get(file);
        else {
            try {
                UserRegistry reg = new UserRegistry(file);
                instances.put(file, reg);
                return reg;
            } catch(IOException e) {
                
                /// *** HANDLE EXCEPTION
                e.printStackTrace();
                return null;
            }
        }
    }
    
    
    
    /// --- INSTANCE MEMBERS --- ///
   
    private RegFile reg;
    private Map users;


    /**
     * Create a new registry.
     */

	private UserRegistry(File file) throws IOException {
        reg = new RegFile(file);
		users = new HashMap();
	}


    /**
     * Create a new trivial registry. This registry instance will
     * always fail to authenticate a user.
     */
    
    private UserRegistry() {
        reg = null;
    }


    /**
     * Get a user principal.
     * 
     * @param name User name
     * @param pass User password
     * @return A principal for the user
     * @throws AuthenticationFailedException If the pass is wrong
     * @throws NoSuchUserException If the user does not exist
     */

    public synchronized Principal getUser(String name, Indirection ind) throws NoSuchUserException, AuthenticationFailedException {
        
        boolean available = ((Boolean)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new Boolean(scan());
            }
        })).booleanValue();
        
        if(available) {
            User user = (User)users.get(name);
            if(user == null) throw new NoSuchUserException(name);
            Principal prin = user.getPrincipal(ind);
            ind.clearCredentials();
            if(prin == null) throw new AuthenticationFailedException("invalid password");
            else return prin;
        } else throw new AuthenticationFailedException("service unavailable");
    }
    
    
    
    /// --- PRIVATE METHODS --- ///
    
    /// scan a new user map if the registry file has changed
    private synchronized boolean scan() {
        if(reg == null) return false;
        else if(reg.isModified()) {
            try {
                Map tmp = new HashMap();
                reg.populate(tmp);
                users.clear();
                users.putAll(tmp);
                return true;
            } catch(IOException e) { 
                
                /// *** HANDLE EXCEPTION 
                e.printStackTrace();
                return false;
            }
        } else return true;
    }


    /// --- INNER CLASSES --- ///
    
    /// file object
    private static class RegFile {
        
        private File file;
        private long lastCheck;
        
        private RegFile(File file) throws IOException {
            if(file == null) throw new IllegalArgumentException("null file");
            if(!file.exists()) throw new FileNotFoundException("file \'" + file + "\' not found");
            this.file = file;
            lastCheck = -1;
        }
        
        private boolean isModified() {
            return (lastCheck == -1 || lastCheck < file.lastModified());   
        }
        
        /// pupolate with user names -> users
        private void populate(Map users) throws IOException {
            try {
                Document doc = XMLUtils.parse(file);
                NodeList list = doc.getElementsByTagName("principal");
                for (int i = 0; i < list.getLength(); i++) {
					Node n = list.item(i);
                    NamedNodeMap map = n.getAttributes();
                    Node name = map.getNamedItem("name");
                    Node pass = map.getNamedItem("password");
                    if(name == null || pass == null) throw new IOException("invalid file format");
                    users.put(name.getNodeValue(), new User(name.getNodeValue(), pass.getNodeValue().toCharArray()));
				}
                lastCheck = System.currentTimeMillis();
            } catch(XMLException e) {
                throw new IOException("error while reading user file: " + e.getMessage());
            }
        }
    }
    
    /// private principal object
    private static class User {
        
        private UrdPrincipal principal;
        private char[] password;
        
        private User(String name, char[] pass) {
            principal = new UrdPrincipal(name);
            password = pass;   
        }
        
        private UrdPrincipal getPrincipal(Indirection ind) {
            if(ind.matches(password)) {
                return this.principal;
            } else return null;
        }

        /*private UrdPrincipal getPrincipal(char[] pass) {
            if(pass != null && Arrays.equals(password, pass)) {
                return this.principal; 
            } else return null;  
        }*/
    }
}