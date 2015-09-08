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

package net.larsan.norna.service.user;

import javax.security.auth.Subject;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;
import net.larsan.norna.base.*;

/**
* The user service is intended as a thin layer on top of the JAAS specification. In particular
* the User service allows participating services to specify dynamic priviliges for authenticated
* <code>Subjects</code>.
*
* <p>Services whishing to take part of this service implements a {@link LoginListener} interface
* that will allow them to be informed when a <code>Subject</code> is authenticated. 
*
* <p>All methods in this interface is subject to a security check using the {@link UserPermission}
* permission when they are called.
* 
* <p>For service that can only verify indirect passwords (for example, hashed SASL services) the user
* service offers verification through {@link Indirection indirection}. The indirection object will then
* be used to verify the password through possible transformations. The indirections are available through
* an indirection factory.
* 
* <p>The norna user service has a fixed Namespace ID, "/norna/users", which is also a 
* static identifier in this class.
*
* @author Lars J. Nilsson
* @version ##NORNA-VERSION##
* @see Authenticator
* @see LoginEvent
*/

public interface UserHandle extends ServiceHandle {
    
    
    /// --- NAMESPACE ID --- ///
    
    /**
     * The fixed namespace ID, "/norna/users"
     */
    
    public static final String NAMESPACE_ID = "/norna/users";
    
    
    
    /// --- ACCESSORS --- ///

    /**
     * Get an authenicator interface for a user. The authenticator will
     * be used to validate the user and optionally to fire login
     * events to a coosen set of listeners.
     * 
     * @param user User to authenicate
     * @return An {@link Authenticator} for the user
     * @throws NoSuchUserException If the user name does not exist
     * @throws SecurityException If the caller does not have permission to access this method
     */
    
    public Authenticator getAuthenticator(String user) throws NoSuchUserException, SecurityException;
    
    
    
    /**
     * Get an authentication that works by indirection. This might fail immediately if the
     * user service knows that indirection is not supported.
     * 
     * @param user User to authenticate
     * @param ind Indirection to use
     * @return An {@link Authenticator} for the user
     * @throws NoSuchUserException If the user name does not exist
     * @throws IndirectionUnavailableException If indirection is not available
     * @throws SecurityException If the caller does not have permission to access this method
     */ 
    
    public Authenticator getAuthenticator(String user, Indirection ind) throws NoSuchUserException, 
                                                                                 IndirectionUnavailableException, 
                                                                                 SecurityException;
    
    
    /**
     * Get the indirection factory for the service. This factory can create new idnirection
     * object to use for indirect authentications.
     * 
     * @return The user service indirection factory
     */
    
    public IndirectionFactory getIndirectionFactory();
    
}