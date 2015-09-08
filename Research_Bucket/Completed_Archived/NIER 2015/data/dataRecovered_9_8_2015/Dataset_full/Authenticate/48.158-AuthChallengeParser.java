/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/auth/AuthChallengeParser.java,v 1.4.2.2 2004/02/22 18:21:14 olegk Exp $
 * $Revision: 1.4.2.2 $
 * $Date: 2004/02/22 18:21:14 $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient.auth;

import java.util.Map;
import java.util.HashMap;

/**
 * This class provides utility methods for parsing HTTP www and proxy authentication 
 * challenges.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0beta1
 */
public final class AuthChallengeParser {
    /** 
     * Extracts authentication scheme from the given authentication 
     * challenge.
     *
     * @param challengeStr the authentication challenge string
     * @return authentication scheme
     * 
     * @throws MalformedChallengeException when the authentication challenge string
     *  is malformed
     * 
     * @since 2.0beta1
     */
    public static String extractScheme(final String challengeStr) 
      throws MalformedChallengeException {
        if (challengeStr == null) {
            throw new IllegalArgumentException("Challenge may not be null"); 
        }
        int i = challengeStr.indexOf(' ');
        String s = null; 
        if (i == -1) {
            s = challengeStr;
        } else {
            s = challengeStr.substring(0, i);
        }
        if (s.equals("")) {
            throw new MalformedChallengeException("Invalid challenge: " + challengeStr);
        }
        return s.toLowerCase();
    }

    /** 
     * Extracts a map of challenge parameters from an authentication challenge.
     * Keys in the map are lower-cased
     *
     * @param challengeStr the authentication challenge string
     * @return a map of authentication challenge parameters
     * @throws MalformedChallengeException when the authentication challenge string
     *  is malformed
     * 
     * @since 2.0beta1
     */
    public static Map extractParams(final String challengeStr)
      throws MalformedChallengeException {
        if (challengeStr == null) {
            throw new IllegalArgumentException("Challenge may not be null"); 
        }
        int i = challengeStr.indexOf(' ');
        if (i == -1) {
            throw new MalformedChallengeException("Invalid challenge: " + challengeStr);
        }

        Map elements = new HashMap();

        i++;
        int len = challengeStr.length();

        String name = null;
        String value = null;

        StringBuffer buffer = new StringBuffer();

        boolean parsingName = true;
        boolean inQuote = false;
        boolean gotIt = false;

        while (i < len) {
            // Parse one char at a time 
            char ch = challengeStr.charAt(i);
            i++;
            // Process the char
            if (parsingName) {
                // parsing name
                if (ch == '=') {
                    name = buffer.toString().trim();
                    parsingName = false;
                    buffer.setLength(0);
                } else if (ch == ',') {
                    name = buffer.toString().trim();
                    value = null;
                    gotIt = true;
                    buffer.setLength(0);
                } else {
                    buffer.append(ch);
                }
                // Have I reached the end of the challenge string?
                if (i == len) {
                    name = buffer.toString().trim();
                    value = null;
                    gotIt = true;
                }
            } else {
                //parsing value
                if (!inQuote) {
                    // Value is not quoted or not found yet
                    if (ch == ',') {
                        value = buffer.toString().trim();
                        gotIt = true;
                        buffer.setLength(0);
                    } else {
                        // no value yet
                        if (buffer.length() == 0) {
                            if (ch == ' ') {
                                //discard
                            } else if (ch == '\t') {
                                //discard
                            } else if (ch == '\n') {
                                //discard
                            } else if (ch == '\r') {
                                //discard
                            } else {
                                // otherwise add to the buffer
                                buffer.append(ch);
                                if (ch == '"') {
                                    inQuote = true;
                                }
                            }
                        } else {
                            // already got something
                            // just keep on adding to the buffer
                            buffer.append(ch);
                        }
                    }
                } else {
                    // Value is quoted
                    // Keep on adding until closing quote is encountered
                    buffer.append(ch);
                    if (ch == '"') {
                        inQuote = false;
                    }
                }
                // Have I reached the end of the challenge string?
                if (i == len) {
                    value = buffer.toString().trim();
                    gotIt = true;
                }
            }
            if (gotIt) {
                // Got something
                if ((name == null) || (name.equals(""))) {
                    throw new MalformedChallengeException("Invalid challenge: " + challengeStr);
                }
                // Strip quotes when present
                if ((value != null) && (value.length() > 1)) {
                    if ((value.charAt(0) == '"') 
                     && (value.charAt(value.length() - 1) == '"')) {
                        value = value.substring(1, value.length() - 1);  
                     }
                }
                
                elements.put(name.toLowerCase(), value);
                parsingName = true;
                gotIt = false;
            }
        }
        return elements;
    }
}
