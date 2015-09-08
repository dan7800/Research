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

/**
 * The authenticator is a simple interface for authenticating a user.
 * 
 * @author Lars J. Nilsson
 * @version ##NORNA-VERSION##
 * @see UserHandle
 */

public interface Authenticator {
    
    
    /**
     * Authenticate a user using credentials. This method does not fire
     * any login events to the login listeners.
     * 
     * @param cred User credentials
     * @return A user subject
     * @throws AuthenticationFailedException If authentication failed
     */
    
    public LoginSession authenticate(char[] cred) throws AuthenticationFailedException;
    
    
    
    /**
     * Authenticate a user and notify listeners. If the listener parameter is
     * null all available listeners will be notified.
     * 
     * @param cred User credentials
     * @param listeners Listeners to notify, null to notify all
     * @return A user subject or null if authenication failed
     * @throws AuthenticationFailedException If authentication failed
     */
    
    public LoginSession authenticate(char[] cred, LoginListener[] listeners) throws AuthenticationFailedException;
    
    
    
    /**
     * Get backing indirection if available. The indirection is used to transform the credentials
     * used by the authentication.
     * 
     * @return An indirection if available, or null if not used
     */
    
    public Indirection getIndirection();

}
