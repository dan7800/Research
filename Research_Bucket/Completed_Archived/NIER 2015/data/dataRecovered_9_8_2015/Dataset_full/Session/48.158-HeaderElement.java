/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/HeaderElement.java,v 1.18.2.1 2004/02/22 18:21:13 olegk Exp $
 * $Revision: 1.18.2.1 $
 * $Date: 2004/02/22 18:21:13 $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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

package org.apache.commons.httpclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * <p>One element of an HTTP header's value.</p>
 * <p>
 * Some HTTP headers (such as the set-cookie header) have values that
 * can be decomposed into multiple elements.  Such headers must be in the
 * following form:
 * </p>
 * <pre>
 * header  = [ element ] *( "," [ element ] )
 * element = name [ "=" [ value ] ] *( ";" [ param ] )
 * param   = name [ "=" [ value ] ]
 *
 * name    = token
 * value   = ( token | quoted-string )
 *
 * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
 *                       white space&gt;
 * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
 * text          = any char except &lt;"&gt;
 * quoted-char   = "\" char
 * </pre>
 * <p>
 * Any amount of white space is allowed between any part of the
 * header, element or param and is ignored. A missing value in any
 * element or param will be stored as the empty {@link String};
 * if the "=" is also missing <var>null</var> will be stored instead.
 * </p>
 * <p>
 * This class represents an individual header element, containing
 * both a name/value pair (value may be <tt>null</tt>) and optionally
 * a set of additional parameters.
 * </p>
 * <p>
 * This class also exposes a {@link #parse} method for parsing a
 * {@link Header} value into an array of elements.
 * </p>
 *
 * @see Header
 *
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @since 1.0
 * @version $Revision: 1.18.2.1 $ $Date: 2004/02/22 18:21:13 $
 */
public class HeaderElement extends NameValuePair {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public HeaderElement() {
        this(null, null, null);
    }

    /**
      * Constructor.
      * @param name my name
      * @param value my (possibly <tt>null</tt>) value
      */
    public HeaderElement(String name, String value) {
        this(name, value, null);
    }

    /**
     * Constructor with name, value and parameters.
     *
     * @param name my name
     * @param value my (possibly <tt>null</tt>) value
     * @param parameters my (possibly <tt>null</tt>) parameters
     */
    public HeaderElement(String name, String value,
            NameValuePair[] parameters) {
        super(name, value);
        setParameters(parameters);
    }

    // -------------------------------------------------------- Constants

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HeaderElement.class);

    /**
     * Map of numeric values to whether or not the
     * corresponding character is a "separator
     * character" (tspecial).
     */
    private static final BitSet SEPARATORS = new BitSet(128);

    /**
     * Map of numeric values to whether or not the
     * corresponding character is a "token
     * character".
     */
    private static final BitSet TOKEN_CHAR = new BitSet(128);

    /**
     * Map of numeric values to whether or not the
     * corresponding character is an "unsafe
     * character".
     */
    private static final BitSet UNSAFE_CHAR = new BitSet(128);

    /**
     * Static initializer for {@link #SEPARATORS},
     * {@link #TOKEN_CHAR}, and {@link #UNSAFE_CHAR}.
     */
    static {
        // rfc-2068 tspecial
        SEPARATORS.set('(');
        SEPARATORS.set(')');
        SEPARATORS.set('<');
        SEPARATORS.set('>');
        SEPARATORS.set('@');
        SEPARATORS.set(',');
        SEPARATORS.set(';');
        SEPARATORS.set(':');
        SEPARATORS.set('\\');
        SEPARATORS.set('"');
        SEPARATORS.set('/');
        SEPARATORS.set('[');
        SEPARATORS.set(']');
        SEPARATORS.set('?');
        SEPARATORS.set('=');
        SEPARATORS.set('{');
        SEPARATORS.set('}');
        SEPARATORS.set(' ');
        SEPARATORS.set('\t');

        // rfc-2068 token
        for (int ch = 32; ch < 127; ch++) {
            TOKEN_CHAR.set(ch);
        }
        TOKEN_CHAR.xor(SEPARATORS);

        // rfc-1738 unsafe characters, including CTL and SP, and excluding
        // "#" and "%"
        for (int ch = 0; ch < 32; ch++) {
            UNSAFE_CHAR.set(ch);
        }
        UNSAFE_CHAR.set(' ');
        UNSAFE_CHAR.set('<');
        UNSAFE_CHAR.set('>');
        UNSAFE_CHAR.set('"');
        UNSAFE_CHAR.set('{');
        UNSAFE_CHAR.set('}');
        UNSAFE_CHAR.set('|');
        UNSAFE_CHAR.set('\\');
        UNSAFE_CHAR.set('^');
        UNSAFE_CHAR.set('~');
        UNSAFE_CHAR.set('[');
        UNSAFE_CHAR.set(']');
        UNSAFE_CHAR.set('`');
        UNSAFE_CHAR.set(127);
    }

    // ----------------------------------------------------- Instance Variables

    /** My parameters, if any. */
    private NameValuePair[] parameters = null;

    // ------------------------------------------------------------- Properties

    /**
     * Get parameters, if any.
     *
     * @since 2.0
     * @return parameters as an array of {@link NameValuePair}s
     */
    public NameValuePair[] getParameters() {
        return this.parameters;
    }

    /**
     * 
     * @param pairs The new parameters.  May be null.
     */
    protected void setParameters(final NameValuePair[] pairs) {
        parameters = pairs;
    }
    // --------------------------------------------------------- Public Methods

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue  the string representation of the header value
     *                     (as received from the web server).
     * @return the header elements containing <code>Header</code> elements.
     * @throws HttpException if the above syntax rules are violated.
     */
    public static final HeaderElement[] parse(String headerValue)
        throws HttpException {
            
        LOG.trace("enter HeaderElement.parse(String)");

        if (headerValue == null) {
            return null;
        }
        
        Vector elements = new Vector();
        StringTokenizer tokenizer =
            new StringTokenizer(headerValue.trim(), ",");

        while (tokenizer.countTokens() > 0) {
            String nextToken = tokenizer.nextToken();

            // FIXME: refactor into private method named ?
            // careful... there may have been a comma in a quoted string
            try {
                while (HeaderElement.hasOddNumberOfQuotationMarks(nextToken)) {
                    nextToken += "," + tokenizer.nextToken();
                }
            } catch (NoSuchElementException exception) {
                throw new HttpException(
                    "Bad header format: wrong number of quotation marks");
            }

            // FIXME: Refactor out into a private method named ?
            try {
                /*
                 * Following to RFC 2109 and 2965, in order not to conflict
                 * with the next header element, make it sure to parse tokens.
                 * the expires date format is "Wdy, DD-Mon-YY HH:MM:SS GMT".
                 * Notice that there is always comma(',') sign.
                 * For the general cases, rfc1123-date, rfc850-date.
                 */
                if (tokenizer.hasMoreTokens()) {
                    String s = nextToken.toLowerCase();
                    if (s.endsWith("mon") 
                        || s.endsWith("tue")
                        || s.endsWith("wed") 
                        || s.endsWith("thu")
                        || s.endsWith("fri")
                        || s.endsWith("sat")
                        || s.endsWith("sun")
                        || s.endsWith("monday") 
                        || s.endsWith("tuesday") 
                        || s.endsWith("wednesday") 
                        || s.endsWith("thursday") 
                        || s.endsWith("friday") 
                        || s.endsWith("saturday") 
                        || s.endsWith("sunday")) {

                        nextToken += "," + tokenizer.nextToken();
                    }
                }
            } catch (NoSuchElementException exception) {
                throw new HttpException
                    ("Bad header format: parsing with wrong header elements");
            }

            String tmp = nextToken.trim();
            if (!tmp.endsWith(";")) {
                tmp += ";";
            }
            char[] header = tmp.toCharArray();

            // FIXME: refactor into a private method named? parseElement?
            boolean inAString = false;
            int startPos = 0;
            HeaderElement element = new HeaderElement();
            Vector paramlist = new Vector();
            for (int i = 0 ; i < header.length ; i++) {
                if (header[i] == ';' && !inAString) {
                    NameValuePair pair = parsePair(header, startPos, i);
                    if (pair == null) {
                        throw new HttpException(
                            "Bad header format: empty name/value pair in" 
                            + nextToken);

                    // the first name/value pair are handled differently
                    } else if (startPos == 0) {
                        element.setName(pair.getName());
                        element.setValue(pair.getValue());
                    } else {
                        paramlist.addElement(pair);
                    }
                    startPos = i + 1;
                } else if (header[i] == '"' 
                    && !(inAString && i > 0 && header[i - 1] == '\\')) {
                    inAString = !inAString;
                }
            }

            // now let's add all the parameters into the header element
            if (paramlist.size() > 0) {
                NameValuePair[] tmp2 = new NameValuePair[paramlist.size()];
                paramlist.copyInto((NameValuePair[]) tmp2);
                element.setParameters (tmp2);
                paramlist.removeAllElements();
            }

            // and save the header element into the list of header elements
            elements.addElement(element);
        }

        HeaderElement[] headerElements = new HeaderElement[elements.size()];
        elements.copyInto((HeaderElement[]) headerElements);
        return headerElements;
    }

    /**
     * Return <tt>true</tt> if <i>string</i> has
     * an odd number of <tt>"</tt> characters.
     *
     * @param string the string to test
     * @return true if there are an odd number of quotation marks, false 
     *      otherwise
     */
    private static final boolean hasOddNumberOfQuotationMarks(String string) {
        boolean odd = false;
        int start = -1;
        while ((start = string.indexOf('"', start + 1)) != -1) {
            odd = !odd;
        }
        return odd;
    }

    /**
     * Parse a header character array into a {@link NameValuePair}
     *
     * @param header the character array to parse
     * @param start the starting position of the text within the array
     * @param end the end position of the text within the array
     * @return a {@link NameValuePair} representing the header
     */
    private static final NameValuePair parsePair(char[] header, 
        int start, int end) {
            
        LOG.trace("enter HeaderElement.parsePair(char[], int, int)");

        NameValuePair pair = null;
        String name = new String(header, start, end - start).trim();
        String value = null;

        //TODO: This would certainly benefit from a StringBuffer
        int index = name.indexOf("=");
        if (index >= 0) {
            if ((index + 1) < name.length()) {
                value = name.substring(index + 1).trim();
                // strip quotation marks
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
            }
            name = name.substring(0, index).trim();
        }

        pair = new NameValuePair(name, value);

        return pair;
    }


    /**
     * Returns parameter with the given name, if found. Otherwise null 
     * is returned
     *
     * @param name The name to search by.
     * @return NameValuePair parameter with the given name
     */

    public NameValuePair getParameterByName(String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        } 
        NameValuePair found = null;
        NameValuePair parameters[] = getParameters();
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                NameValuePair current = parameters[ i ];
                if (current.getName().equalsIgnoreCase(name)) {
                    found = current;
                    break;
                }
            }
        }
        return found;
    }


}

