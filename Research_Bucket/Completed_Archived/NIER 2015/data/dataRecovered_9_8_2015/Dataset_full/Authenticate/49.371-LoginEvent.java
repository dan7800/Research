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

import java.security.CodeSource;
import javax.security.auth.Subject;

/**
* This event details what subject has been authenticated by the user service. It contains
* the <code>Subject</code> in question plus the code source which gives the listener an opportunity
* to add or remove permissions as it see fit.
*
* @author Lars J. Nilsson
* @version ##NORNA-VERSION##
* @see LoginListener 
* @see UserHandle
*/

public class LoginEvent {

    /**
    * Subject that has been authenticated.
    */

    private Subject subject;
    
    /**
    * Subject code source
    */
    
    private CodeSource codeSource;


    /**
    * Create a login event without a code source.
    *
    * @param subject Subject for the event, must not be null
    */
    
    public LoginEvent(Subject subject) {
        this(subject, null);     
    }
    
    /**
    * Create a login event with a subject and a code source.
    *
    * @param subject Subject for the event, must not be null
    * @param source Code source for the subject, may be null
    */
    
    public LoginEvent(Subject subject, CodeSource source) {
        if(subject == null) throw new IllegalArgumentException("null subject");
        this.codeSource = source;
        this.subject = subject;
    }


    /**
    * Get the subject for this event. This method wil never return null.
    *
    * @return The event subject, never null
    */
    
    public Subject getSubject() {
        return (this.subject); 
    }

    /**
    * Get the code source for this event. May return null.
    *
    * @return CodeSource for the event, may return null.
    */

    public CodeSource getCodeSource() {
        return (this.codeSource); 
    }
}