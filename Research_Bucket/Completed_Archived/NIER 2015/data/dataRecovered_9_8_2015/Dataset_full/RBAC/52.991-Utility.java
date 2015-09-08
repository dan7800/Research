/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/util/Utility.java,v 1.25 2003/08/29 02:25:25 farrukh_najmi Exp $
 */
package com.sun.ebxml.registry.util;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.X509Certificate;

import javax.naming.*;
import javax.swing.*;

// java mail
import javax.mail.*;
import javax.mail.internet.*;

// jaxp
import org.xml.sax.InputSource;

// jaxm
import javax.xml.messaging.*;
import javax.xml.soap.*;
import javax.xml.soap.MessageFactory;

import com.sun.ebxml.registry.*;
import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.query.types.*;
import org.oasis.ebxml.registry.bindings.rs.*;
import org.oasis.ebxml.registry.bindings.rs.types.*;
import org.oasis.ebxml.registry.bindings.rim.*;


import java.nio.charset.Charset;


/**
 * Class Declaration.
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 * @version   1.2, 05/02/00
 */
public class Utility {
    
	/**
     * Class Constructor.
     *
     *
     * @see
     */
    protected Utility() {}
	
	/**
    @deprecated Do not use it. It may have problem in handling encoding.
    */
    public StringBuffer getStringBufferFromInputStream(InputStream is) throws IOException {
		int buflen = 1024;
		char[] chars = new char[buflen+1];
		StringBuffer strBuf = new StringBuffer();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));		
		
		int charsRead = 0;
		while((charsRead = br.read(chars, 0, buflen)) != -1) {
			//System.err.println("chars[0] = '" + chars[0] + "' charsRead = " + charsRead);	
			strBuf.append(chars, 0, charsRead);
		}
		
		return strBuf;
	}
    
    /**
    Get the <?xml version="1.0"?> decalaration from XML bytes
    */
    public String getXMLDeclaration(byte [] bytes) {
        String str = new String(bytes);
        return removeXMLDeclaration(new StringBuffer(str));
    }
    
   /**
   * Removes the <?xml version="1.0"?> decalaration from XML bytes
   @return the bytes of the XML document
   */
    public byte[] removeXMLDeclaration(byte [] bytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int declEndIndex = 0;
        
        // Get the end index of declaration 
        if (bytes[0] == '<') {
            
            boolean hasDecl = false;
			for (int i=1; i < bytes.length; i++) {
                if (bytes[1]==' ') {
                    //Skip spaces after '<'
                    declEndIndex++;
                }
                else if (bytes[i]=='?') {
                    // It has declaration
                    hasDecl = true;
                }
                if (hasDecl) {
                    if (bytes[i] != '>') {
                        declEndIndex++;
                    }
                    else {
                        declEndIndex++;
                        break;
                    }
                }
			}
        } 
        
        // skip all spaces after the xml declaration
        for (int i=declEndIndex; i < bytes.length; i++) {
            declEndIndex++;
            if (bytes[i] != ' ') {
                break;                
            }
        }
        
        // System.err.println(declEndIndex + "!!!!!");
        bos.write(bytes, declEndIndex, bytes.length - declEndIndex);
        bos.flush();
        bos.close();
        //System.err.println(new String(bos.toByteArray()));
        return bos.toByteArray();
        
    }

    /**
	 * Removes the <?xml version="1.0"?> decalaration from XML string
     @return the XML declaration
	 */	
    public String removeXMLDeclaration(StringBuffer str) {

        String decl = "";
		int len = str.length();
		int startIndex = -1;	//start of what we are removing
		int endIndex = -1;		//end of what we are removing		
		for (int i=0; i<len; i++) {
			if (str.charAt(i) == '<') {
				decl += "<";
                startIndex = i;	
				
				//Skip spaces after '<'
				i++;
				while (str.charAt(i) == ' ') {
                    decl += " ";
					i++;
				}
				
				if (str.charAt(i) == '?') {
                    //We have an XML declaration to remove
					//Skip forward and find matching '>'
					while (str.charAt(i) != '>') {
                        decl += str.charAt(i);
						i++;
					}
                    decl += ">";
					endIndex = i;
					break;
				}
				else {
					//Not an XML declaration
					startIndex = -1;
					break;
				}				
			
			}
		}

		if (startIndex != -1) {
			str.delete(startIndex, endIndex+1);
		}

        return decl;
	}
	
	/**
	 * Mimics javamail boundary id generator
	 */
	public static String getMimeBoundary()
	{
		Session session = null;//Session.getDefaultInstance(new Properties());
		
	    String s = null;
	    InternetAddress internetaddress = InternetAddress.getLocalAddress(session);
	    if(internetaddress != null)
	        s = internetaddress.getAddress();
	    else
	        s = "javamailuser@localhost";
	    StringBuffer stringbuffer = new StringBuffer();
	    stringbuffer.append(stringbuffer.hashCode()).append('.').append(System.currentTimeMillis()).append('.').append("ebxmlrr.").append(s);
	    return stringbuffer.toString();
	}

    /*
    public InputStream createSOAPStreamFromRequestStream(InputStream req) throws SOAPException, IOException, ParseException {	
	
		String soapNS = "http://schemas.xmlsoap.org/soap/envelope/";
		String soapEnv = "soap-env";
		String soapEnvPrefix = soapEnv + ":";

		StringBuffer reqStr = Utility.getInstance().getStringBufferFromInputStream(req);
		String decl = removeXMLDeclaration(reqStr);

		String boundary = getMimeBoundary();
        
        System.err.println(decl);
        
        String msgStr = //"--" + boundary +
            decl +
			"\n<" + soapEnvPrefix + "Envelope" +
			"\n\txmlns:soap-env=\"" + soapNS + "\"" +
			"\n\txmlns:SOAP-SEC=\"http://schemas.xmlsoap.org/soap/security/2000-12\" soap-env:actor=\"some-uri\"" +  	
			"\n\tsoap-env:mustUnderstand=\"1\">" +

			//The SOAP Header			
			"\n\n\t<" + soapEnvPrefix + "Header>" +
			
			//DSIG goes here

			"\n\t</" + soapEnvPrefix + "Header>" +
			
			//The SOAP Body
			"\n\n\t<" + soapEnvPrefix + "Body SOAP-SEC:id=\"Body\">" + "\n" + 

			//The ebXML Registry request goes here
			reqStr +
			
			"\n\t</" + soapEnvPrefix + "Body>" +

			"\n\n</" + soapEnvPrefix + "Envelope>" 
			//+ "\n--" + boundary + "--"
			;
			
			//System.err.println(msgStr);
			
			InputStream is = (InputStream) new StringBufferInputStream(msgStr); 
			
			
			MimeHeaders headers = new MimeHeaders();
			headers.addHeader("Content-Type", "text/xml");
			SOAPMessage message = MessageFactory.newInstance().createMessage(headers, is);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			message.writeTo(out);
			out.flush();
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

			return in;
	}
    */

    /**
    Get bytes array from InputStream
    */
    public byte [] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while(true) {
            byte [] buffer = new byte[100];
            int noOfBytes = is.read(buffer);
            if (noOfBytes == -1) {
                break;
            }
            else {
                bos.write(buffer, 0, noOfBytes);
            }
        }
        bos.flush();
        bos.close();
        return bos.toByteArray();
    }
    
    /**
    Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
    @param req the InputStream to the registry request
    @return the InputStream to the the created SOAPMessage 
    */
    public InputStream createSOAPStreamFromRequestStream(InputStream req) throws SOAPException, IOException, ParseException {	
	
		String soapNS = "http://schemas.xmlsoap.org/soap/envelope/";
		String soapEnv = "soap-env";
		String soapEnvPrefix = soapEnv + ":";

        byte [] requestBytesWithDecl = getBytesFromInputStream(req);
        String decl = getXMLDeclaration(requestBytesWithDecl);
        byte [] requestBytes = removeXMLDeclaration(requestBytesWithDecl);
        requestBytesWithDecl = null;
        
        // System.err.println(decl);
        
        String partBeforeRequestStr = //"--" + boundary +
            decl +
			"\n<" + soapEnvPrefix + "Envelope" +
			"\n\txmlns:soap-env=\"" + soapNS + "\"" +
			"\n\txmlns:SOAP-SEC=\"http://schemas.xmlsoap.org/soap/security/2000-12\" soap-env:actor=\"some-uri\"" +  	
			"\n\tsoap-env:mustUnderstand=\"1\">" +

			//The SOAP Header			
			"\n\n\t<" + soapEnvPrefix + "Header>" +
			
			//DSIG goes here

			"\n\t</" + soapEnvPrefix + "Header>" +
			
			//The SOAP Body
			"\n\n\t<" + soapEnvPrefix + "Body SOAP-SEC:id=\"Body\">" + "\n"; 

			//The ebXML Registry request goes here
			
            
        String partAfterRequestStr =  
			"\n\t</" + soapEnvPrefix + "Body>" +

			"\n\n</" + soapEnvPrefix + "Envelope>" 
			//+ "\n--" + boundary + "--"
			;
        
        // We concatenate the three bytes array to a single big array
        byte [] partBeforeRequestBytes = partBeforeRequestStr.getBytes("ISO-8859-1"); // The soap env does not have non ascii chars
        byte [] partAfterRequestBytes = partAfterRequestStr.getBytes("ISO-8859-1"); // The soap env does not have non ascii chars
        byte [] soapBytes = new byte[partBeforeRequestBytes.length + requestBytes.length + partAfterRequestBytes.length];
        
        int soapBytesIndex=0;
        for(int a=0; a<partBeforeRequestBytes.length; a++) {
            soapBytes[soapBytesIndex] = partBeforeRequestBytes[a];
            soapBytesIndex++;
        }
        for(int b=0; b<requestBytes.length; b++) {
            soapBytes[soapBytesIndex] = requestBytes[b];
            soapBytesIndex++;
        }
        for(int c=0; c<partAfterRequestBytes.length; c++) {
            soapBytes[soapBytesIndex] = partAfterRequestBytes[c];
            soapBytesIndex++;
        }
        // System.err.println("L343:" + new String(soapBytes));
        return new ByteArrayInputStream(soapBytes);
	}
    
	/**
    Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
    @param req the InputStream to the registry request
    @return the created SOAPMessage 
    */
    public SOAPMessage createSOAPMessageFromRequestStream(InputStream reqStream) throws SOAPException, IOException, ParseException {	
	
			InputStream is = createSOAPStreamFromRequestStream(reqStream); 
			
			MimeHeaders mimeHeaders = new MimeHeaders();
			
			ContentType contentType = new ContentType("text/xml"); //"multipart/related");
			//contentType.setParameter("boundary", boundary);			
			//contentType.setParameter("type", "text/xml");
			String contentTypeStr = contentType.toString();
			//System.err.println("contentTypeStr = '" + contentTypeStr + "'");
			
			mimeHeaders.addHeader("Content-Type", contentTypeStr);
			mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");   

			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage msg = factory.createMessage(mimeHeaders, is);
			
			msg.saveChanges();
			
			return msg;
	}
	
	/**
    Create a SOAPMessage object from a InputStream to a SOAPMessage
    @param soapStream the InputStream to the SOAPMessage
    @return the created SOAPMessage 
    */
    public SOAPMessage createSOAPMessageFromSOAPStream(InputStream soapStream) throws SOAPException, IOException, ParseException {	
	
			MimeHeaders mimeHeaders = new MimeHeaders();
			
			ContentType contentType = new ContentType("text/xml"); //"multipart/related");
			//contentType.setParameter("boundary", boundary);			
			//contentType.setParameter("type", "text/xml");
			String contentTypeStr = contentType.toString();
			//System.err.println("contentTypeStr = '" + contentTypeStr + "'");
			
			mimeHeaders.addHeader("Content-Type", contentTypeStr);
			mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");   

			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage msg = factory.createMessage(mimeHeaders, soapStream);
			
			msg.saveChanges();
			
			return msg;
	}

	public RegistryResponse createRegistryResponseFromThrowable(Throwable t, String codeContext, String errCode) {		
		RegistryResponse resp = new RegistryResponse();
		resp.setStatus(StatusType.FAILURE);
		RegistryErrorList el = new RegistryErrorList();
		RegistryError re = new RegistryError();
		el.addRegistryError(re);
		resp.setRegistryErrorList(el);

		String stackTrace = getStackTraceFromThrowable(t);
		System.err.println(stackTrace);
		
		String nestedStackTrace = null;
		if (t instanceof com.sun.ebxml.registry.RegistryException) {
			Exception nestedException = ((RegistryException)t).getException();
			nestedStackTrace = getStackTraceFromThrowable(nestedException);
		}
		
		stackTrace += "\n\nNested exception was:\n" + nestedStackTrace;
					
		re.setContent(stackTrace);
		re.setCodeContext(codeContext);
		re.setErrorCode(errCode);
		
		return resp;	
	}
	
	public String getStackTraceFromThrowable(Throwable t) {
		String trace = null;
		if (t != null) {
			StringWriter sw = new StringWriter();		
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			trace = sw.toString();
		}
		
		return trace;
	}
	
	public String createTimestamp() {
		// Get the time zone offset in +HH:mm format
        
        TimeZone tz = TimeZone.getDefault();

        float rawOffset = tz.getRawOffset() / 1000 / 60 / 60.0f;
        int offsetHr = (int) Math.abs(rawOffset);
        int offsetMin = (int)(rawOffset - offsetHr) * 60;
        
        String offset = rawOffset > 0 ? "+" : "-";
        offset += offsetHr < 10 ? "0" : "";
        offset += offsetHr;
        offset += ":";
        offset += offsetMin < 10 ? "0" : "";
        offset += offsetMin;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(new Date()) + offset;
	}
	
	/**
	Return ArrayList of ExternalLink that points to unresolvable Http URLs. Any
	non-Http URLs will not be checked. Any non-Http URLs and other types 
	of URIs will not be checked. If the http response code is smaller than 200
	or bigger than 299, the http URL is considered invalid.
	*/

	public ArrayList validateURIs(ArrayList sourceRegistryObjects) throws 
	RegistryException {
		ArrayList invalidURLROs = new ArrayList();
		Iterator iter = sourceRegistryObjects.iterator();
		while (iter.hasNext()) {
			Object ro = iter.next();
			String uRI = null;
			
			if (ro instanceof ExternalLink) {
				uRI = ((ExternalLink)ro).getExternalURI();
			}
			else if (ro instanceof ServiceBinding) {
				uRI = ((ServiceBinding)ro).getAccessURI();
			}
			else {
				throw new RegistryException("Internal Error happens, unknown " 
				+ "RegistryObjectType");
			}
			
			if (!isValidURI(uRI)) {
				invalidURLROs.add(ro);
			}
		}
		return invalidURLROs;
	}
	
	/**
	Any non-Http URLs and other types of URIs will not be checked. If the http 
	response code is smaller than 200 or bigger than 299, the http URL is 
	considered invalid.
	*/
	public boolean isValidURI(String uRI) {
		URL uRL = null;
		try {
			uRL = new URL(uRI);
		}
		catch(MalformedURLException e) {
			// Not an URL, will not try to resolve
			return true;
		}
		// Try to resolve the URLs here
		if (uRL.getProtocol().equalsIgnoreCase("http")) {
			try {
				HttpURLConnection httpUrlConn = (HttpURLConnection)uRL
				.openConnection();
				int responseCode = httpUrlConn.getResponseCode();
				if (responseCode < 200 || responseCode > 299) {
					return false;
				}
				else {
					return true;
				}
			}
			catch (IOException e) {
				return false;
			}
		} 
		else {
			// Will not resolve non-http URL
			return true;
		}
	}
	
	
	//Based on code contributed by Bobby Bissett
	public org.w3c.dom.Element getSignatureElement(SOAPMessage msg) 
		throws SOAPException, javax.xml.transform.TransformerException {	    
		org.w3c.dom.Element sigElement = null;

	    javax.xml.transform.Transformer xFormer = 
			javax.xml.transform.TransformerFactory.newInstance().newTransformer();
	    
	    // grab info out of msg
	    SOAPPart msgPart = msg.getSOAPPart();
	    javax.xml.transform.Source msgSource = msgPart.getContent();
	    
	    // transform
	    javax.xml.transform.dom.DOMResult domResult = 
			new javax.xml.transform.dom.DOMResult();
	    xFormer.transform(msgSource, domResult);
		
		//root node is the soap:Envelope
	    org.w3c.dom.Node envelopeNode = domResult.getNode();

		//now you have the node. the following code strips off the envelope of 
		//the soap message to get to the actual content
            
        // Advance to envelope node in case of text nodes preceding it
        while ((envelopeNode.getLocalName() == null) ||           
			(!envelopeNode.getLocalName().equalsIgnoreCase("envelope"))) {
            envelopeNode = envelopeNode.getFirstChild();
        }
            
        // Advance to header within envelope node
        org.w3c.dom.Node headerNode = envelopeNode.getFirstChild();
        while ((headerNode.getLocalName() == null) ||
        (!headerNode.getLocalName().equalsIgnoreCase("header"))) {
            headerNode = headerNode.getNextSibling();
        }
		
		//System.err.println("headerNode name is: " + headerNode.getLocalName());
        
        // Advance to signature node within header
        org.w3c.dom.Node sigNode = headerNode.getFirstChild();
		
		if (sigNode == null) {
			return null;
		}
		
        //System.err.println("sigNode: " + sigNode);
        while ((sigNode.getLocalName() == null) ||
        (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
            sigNode = sigNode.getNextSibling();
			if (sigNode == null) {
				return null;
			}
        }
		
		//Desired Signature element may be inside a SOAP-SEC signature element
        if (!sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
	        sigNode = sigNode.getFirstChild();
	        while ((sigNode.getLocalName() == null) ||
	        (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
	            sigNode = sigNode.getNextSibling();
	        }
        }
            
        if (sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
        	if (sigNode instanceof org.w3c.dom.Element) {
				sigElement = (org.w3c.dom.Element)sigNode;
        	}
		}
		
		return sigElement;
	}


        /**
        * Strip urn:uuid: part from start of registry 
        * object id.  If id is null or doesn't start with
        * urn:uuid: then the id is returned without modification.
        */
        public String stripId(String id)
        {
            if (id != null && 
                id.startsWith("urn:uuid:")) 
            {
                id = id.substring(9).trim();
            }

            return id;
        }

	
    /**
    Convert the apostrophes in a String field in a SQL statement to 2
    consecutive apostrohes
    */
    public static String escapeSQLChars(String stringField) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < stringField.length(); i++) {
            char c = stringField.charAt(i);
            result.append(c);
            if (c == '\'') {
                result.append(c);            
            }
        }
        return result.toString();
    }
	
	
	
    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public static Utility getInstance() {
        if (instance == null) {
            synchronized (Utility.class) {
                if (instance == null) {
                    instance = new Utility();
                }
            }
        }
        return instance;
    }    	
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    
    /* # private Utility _utility; */
    private static Utility  instance = null;
    private String ebxmlrrHome=null;

}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/common/Utility.java,v 1.8 2004/01/26 18:17:52 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.soap.SOAPMessage;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBinding;


/**
 * Class Declaration.
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 * @version   1.2, 05/02/00
 */
public class Utility {
    private static HashMap tableNameMap = new HashMap();

    static {
        tableNameMap.put("user", "user_");
        tableNameMap.put("name", "name_");
        tableNameMap.put("classificationscheme", "ClassScheme");
    }

    private static HashMap columnNameMap = new HashMap();

    static {
        columnNameMap.put("number", "number_");
        columnNameMap.put("name", "name_");
        columnNameMap.put("user", "user_");
        columnNameMap.put("timestamp", "timestamp_");
    }

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private Utility _utility; */
    private static Utility instance = null;
    org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();
    private org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());
    private org.freebxml.omar.common.UUIDFactory uf = org.freebxml.omar.common.UUIDFactory.getInstance();
    private String ebxmlrrHome = null;

    /**
     * Class Constructor.
     *
     *
     * @see
     */
    protected Utility() {
    }

    /**
     *     Get the <?xml version="1.0"?> decalaration from XML bytes
     */
    public String getXMLDeclaration(byte[] bytes) {
        String str = new String(bytes);

        return removeXMLDeclaration(new StringBuffer(str));
    }

    /**
     * Removes the <?xml version="1.0"?> decalaration from XML bytes
     *    @return the bytes of the XML document
     */
    public byte[] removeXMLDeclaration(byte[] bytes) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int declEndIndex = 0;

        // Get the end index of declaration
        if (bytes[0] == '<') {
            boolean hasDecl = false;

            for (int i = 1; i < bytes.length; i++) {
                if (bytes[1] == ' ') {
                    //Skip spaces after '<'
                    declEndIndex++;
                } else if (bytes[i] == '?') {
                    // It has declaration
                    hasDecl = true;
                }

                if (hasDecl) {
                    if (bytes[i] != '>') {
                        declEndIndex++;
                    } else {
                        declEndIndex++;

                        break;
                    }
                }
            }
        }

        // skip all spaces after the xml declaration
        for (int i = declEndIndex; i < bytes.length; i++) {
            declEndIndex++;

            if (bytes[i] != ' ') {
                break;
            }
        }

        // System.err.println(declEndIndex + "!!!!!");
        bos.write(bytes, declEndIndex, bytes.length - declEndIndex);
        bos.flush();
        bos.close();

        //System.err.println(new String(bos.toByteArray()));
        return bos.toByteArray();
    }

    /**
     * Removes the <?xml version="1.0"?> decalaration from XML string
     *      @return the XML declaration
     */
    public String removeXMLDeclaration(StringBuffer str) {
        String decl = "";
        int len = str.length();
        int startIndex = -1; //start of what we are removing
        int endIndex = -1; //end of what we are removing

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == '<') {
                decl += "<";
                startIndex = i;

                //Skip spaces after '<'
                i++;

                while (str.charAt(i) == ' ') {
                    decl += " ";
                    i++;
                }

                if (str.charAt(i) == '?') {
                    //We have an XML declaration to remove
                    //Skip forward and find matching '>'
                    while (str.charAt(i) != '>') {
                        decl += str.charAt(i);
                        i++;
                    }

                    decl += ">";
                    endIndex = i;

                    break;
                } else {
                    //Not an XML declaration
                    startIndex = -1;

                    break;
                }
            }
        }

        if (startIndex != -1) {
            str.delete(startIndex, endIndex + 1);
        }

        return decl;
    }

    /**
     * Mimics javamail boundary id generator
     */
    public static String getMimeBoundary() {
        javax.mail.Session session = null; //Session.getDefaultInstance(new Properties());

        String s = null;
        javax.mail.internet.InternetAddress internetaddress = javax.mail.internet.InternetAddress.getLocalAddress(session);

        if (internetaddress != null) {
            s = internetaddress.getAddress();
        } else {
            s = "javamailuser@localhost";
        }

        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append(stringbuffer.hashCode()).append('.')
                    .append(System.currentTimeMillis()).append('.')
                    .append("omar.").append(s);

        return stringbuffer.toString();
    }

    /**
     *     Get bytes array from InputStream
     */
    public byte[] getBytesFromInputStream(InputStream is)
        throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

        while (true) {
            byte[] buffer = new byte[100];
            int noOfBytes = is.read(buffer);

            if (noOfBytes == -1) {
                break;
            } else {
                bos.write(buffer, 0, noOfBytes);
            }
        }

        bos.flush();
        bos.close();

        return bos.toByteArray();
    }

    /**
     *     Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
     *     @param req the InputStream to the registry request
     *     @return the InputStream to the the created SOAPMessage
     */
    public InputStream createSOAPStreamFromRequestStream(InputStream req)
        throws javax.xml.soap.SOAPException, IOException, 
            javax.mail.internet.ParseException {
        String soapNS = "http://schemas.xmlsoap.org/soap/envelope/";
        String soapEnv = "soap-env";
        String soapEnvPrefix = soapEnv + ":";

        byte[] requestBytesWithDecl = getBytesFromInputStream(req);
        String decl = getXMLDeclaration(requestBytesWithDecl);
        byte[] requestBytes = removeXMLDeclaration(requestBytesWithDecl);
        requestBytesWithDecl = null;

        // System.err.println(decl);
        String partBeforeRequestStr = decl + "\n<" + soapEnvPrefix +
            "Envelope" + "\n\txmlns:soap-env=\"" + soapNS + "\"" +
            "\n\txmlns:SOAP-SEC=\"http://schemas.xmlsoap.org/soap/security/2000-12\" soap-env:actor=\"some-uri\"" +
            "\n\tsoap-env:mustUnderstand=\"1\">" + 
            //The SOAP Header
            "\n\n\t<" + soapEnvPrefix + "Header>" + 
            //DSIG goes here
            "\n\t</" + soapEnvPrefix + "Header>" + 
            //The SOAP Body
            "\n\n\t<" + soapEnvPrefix + "Body SOAP-SEC:id=\"Body\">" + "\n";

        //The ebXML Registry request goes here
        String partAfterRequestStr = "\n\t</" + soapEnvPrefix + "Body>" +
            "\n\n</" + soapEnvPrefix + "Envelope>";

        // We concatenate the three bytes array to a single big array
        byte[] partBeforeRequestBytes = partBeforeRequestStr.getBytes(
                "ISO-8859-1"); // The soap env does not have non ascii chars
        byte[] partAfterRequestBytes = partAfterRequestStr.getBytes(
                "ISO-8859-1"); // The soap env does not have non ascii chars
        byte[] soapBytes = new byte[partBeforeRequestBytes.length +
            requestBytes.length + partAfterRequestBytes.length];

        int soapBytesIndex = 0;

        for (int a = 0; a < partBeforeRequestBytes.length; a++) {
            soapBytes[soapBytesIndex] = partBeforeRequestBytes[a];
            soapBytesIndex++;
        }

        for (int b = 0; b < requestBytes.length; b++) {
            soapBytes[soapBytesIndex] = requestBytes[b];
            soapBytesIndex++;
        }

        for (int c = 0; c < partAfterRequestBytes.length; c++) {
            soapBytes[soapBytesIndex] = partAfterRequestBytes[c];
            soapBytesIndex++;
        }

        // System.err.println("L343:" + new String(soapBytes));
        return new java.io.ByteArrayInputStream(soapBytes);
    }

    /**
     *     Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
     *     @param req the InputStream to the registry request
     *     @return the created SOAPMessage
     */
    public SOAPMessage createSOAPMessageFromRequestStream(InputStream reqStream)
        throws javax.xml.soap.SOAPException, IOException, 
            javax.mail.internet.ParseException {
        InputStream is = createSOAPStreamFromRequestStream(reqStream);

        javax.xml.soap.MimeHeaders mimeHeaders = new javax.xml.soap.MimeHeaders();

        javax.mail.internet.ContentType contentType = new javax.mail.internet.ContentType(
                "text/xml"); //"multipart/related");

        String contentTypeStr = contentType.toString();

        //System.err.println("contentTypeStr = '" + contentTypeStr + "'");
        mimeHeaders.addHeader("Content-Type", contentTypeStr);
        mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");

        javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage(mimeHeaders, is);

        msg.saveChanges();

        return msg;
    }

    /**
     *     Create a SOAPMessage object from a InputStream to a SOAPMessage
     *     @param soapStream the InputStream to the SOAPMessage
     *     @return the created SOAPMessage
     */
    public SOAPMessage createSOAPMessageFromSOAPStream(InputStream soapStream)
        throws javax.xml.soap.SOAPException, IOException, 
            javax.mail.internet.ParseException {
        javax.xml.soap.MimeHeaders mimeHeaders = new javax.xml.soap.MimeHeaders();

        javax.mail.internet.ContentType contentType = new javax.mail.internet.ContentType(
                "text/xml"); //"multipart/related");

        String contentTypeStr = contentType.toString();

        //System.err.println("contentTypeStr = '" + contentTypeStr + "'");
        mimeHeaders.addHeader("Content-Type", contentTypeStr);
        mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");

        javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage(mimeHeaders, soapStream);

        msg.saveChanges();

        return msg;
    }

    public org.oasis.ebxml.registry.bindings.rs.RegistryResponse createRegistryResponseFromThrowable(
        Throwable t, String codeContext, String errCode) {
        org.oasis.ebxml.registry.bindings.rs.RegistryResponse resp = null;

        try {
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);

            org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el = bu.rsFac.createRegistryErrorList();
            org.oasis.ebxml.registry.bindings.rs.RegistryError re = bu.rsFac.createRegistryError();
            el.getRegistryError().add(re);
            resp.setRegistryErrorList(el);

            String stackTrace = getStackTraceFromThrowable(t);
            System.err.println(stackTrace);

            String nestedStackTrace = null;

            if (t instanceof OMARException) {
                Exception nestedException = ((OMARException) t).getException();
                nestedStackTrace = getStackTraceFromThrowable(nestedException);
            }

            stackTrace += ("\n\nNested exception was:\n" + nestedStackTrace);

            re.setValue(stackTrace);
            re.setCodeContext(codeContext);
            re.setErrorCode(errCode);
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }

        return resp;
    }

    public void updateRegistryResponseFromThrowable(
        org.oasis.ebxml.registry.bindings.rs.RegistryResponseType resp,
        Throwable t, String codeContext, String errCode) {
        try {
            resp.setStatus(org.oasis.ebxml.registry.bindings.rs.Status.FAILURE);

            org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el = bu.rsFac.createRegistryErrorList();
            org.oasis.ebxml.registry.bindings.rs.RegistryError re = bu.rsFac.createRegistryError();
            el.getRegistryError().add(re);
            resp.setRegistryErrorList(el);

            String stackTrace = getStackTraceFromThrowable(t);
            System.err.println(stackTrace);

            String nestedStackTrace = null;

            if (t instanceof RegistryException) {
                Exception nestedException = ((RegistryException) t).getException();
                nestedStackTrace = getStackTraceFromThrowable(nestedException);
            }

            stackTrace += ("\n\nNested exception was:\n" + nestedStackTrace);

            re.setValue(stackTrace);
            re.setCodeContext(codeContext);
            re.setErrorCode(errCode);
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }
    }

    public String getStackTraceFromThrowable(Throwable t) {
        String trace = null;

        if (t != null) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            trace = sw.toString();
        }

        return trace;
    }

    public String createTimestamp() {
        // Get the time zone offset in +HH:mm format
        java.util.TimeZone tz = java.util.TimeZone.getDefault();

        float rawOffset = tz.getRawOffset() / 1000 / 60 / 60.0f;
        int offsetHr = (int) Math.abs(rawOffset);
        int offsetMin = (int) (rawOffset - offsetHr) * 60;

        String offset = (rawOffset > 0) ? "+" : "-";
        offset += ((offsetHr < 10) ? "0" : "");
        offset += offsetHr;
        offset += ":";
        offset += ((offsetMin < 10) ? "0" : "");
        offset += offsetMin;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");

        return sdf.format(new java.util.Date()) + offset;
    }

    /**
     *         Return ArrayList of ExternalLink that points to unresolvable Http URLs. Any
     *         non-Http URLs will not be checked. Any non-Http URLs and other types
     *         of URIs will not be checked. If the http response code is smaller than 200
     *         or bigger than 299, the http URL is considered invalid.
     */
    public ArrayList validateURIs(ArrayList sourceRegistryObjects)
        throws RegistryException {
        ArrayList invalidURLROs = new ArrayList();
        Iterator iter = sourceRegistryObjects.iterator();

        while (iter.hasNext()) {
            Object ro = iter.next();
            String uRI = null;

            if (ro instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLink) {
                uRI = ((org.oasis.ebxml.registry.bindings.rim.ExternalLink) ro).getExternalURI();
            } else if (ro instanceof ServiceBinding) {
                uRI = ((ServiceBinding) ro).getAccessURI();
            } else {
                throw new RegistryException("Internal Error happens, unknown " +
                    "RegistryObjectType");
            }

            if (!isValidURI(uRI)) {
                invalidURLROs.add(ro);
            }
        }

        return invalidURLROs;
    }

    /**
     *         Any non-Http URLs and other types of URIs will not be checked. If the http
     *         response code is smaller than 200 or bigger than 299, the http URL is
     *         considered invalid.
     */
    public boolean isValidURI(String uRI) {
        java.net.URL uRL = null;

        try {
            uRL = new java.net.URL(uRI);
        } catch (java.net.MalformedURLException e) {
            // Not an URL, will not try to resolve
            return true;
        }

        // Try to resolve the URLs here
        if (uRL.getProtocol().equalsIgnoreCase("http")) {
            try {
                java.net.HttpURLConnection httpUrlConn = (java.net.HttpURLConnection) uRL.openConnection();
                int responseCode = httpUrlConn.getResponseCode();

                if ((responseCode < 200) || (responseCode > 299)) {
                    return false;
                } else {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        } else {
            // Will not resolve non-http URL
            return true;
        }
    }

    //Based on code contributed by Bobby Bissett
    public org.w3c.dom.Element getSignatureElement(SOAPMessage msg)
        throws javax.xml.soap.SOAPException, 
            javax.xml.transform.TransformerException {
        org.w3c.dom.Element sigElement = null;

        javax.xml.transform.Transformer xFormer = javax.xml.transform.TransformerFactory.newInstance()
                                                                                        .newTransformer();

        // grab info out of msg
        javax.xml.soap.SOAPPart msgPart = msg.getSOAPPart();
        javax.xml.transform.Source msgSource = msgPart.getContent();

        // transform
        javax.xml.transform.dom.DOMResult domResult = new javax.xml.transform.dom.DOMResult();
        xFormer.transform(msgSource, domResult);

        //root node is the soap:Envelope
        org.w3c.dom.Node envelopeNode = domResult.getNode();

        //now you have the node. the following code strips off the envelope of
        //the soap message to get to the actual content
        // Advance to envelope node in case of text nodes preceding it
        while ((envelopeNode.getLocalName() == null) ||
                (!envelopeNode.getLocalName().equalsIgnoreCase("envelope"))) {
            envelopeNode = envelopeNode.getFirstChild();
        }

        // Advance to header within envelope node
        org.w3c.dom.Node headerNode = envelopeNode.getFirstChild();

        while ((headerNode.getLocalName() == null) ||
                (!headerNode.getLocalName().equalsIgnoreCase("header"))) {
            headerNode = headerNode.getNextSibling();
        }

        //System.err.println("headerNode name is: " + headerNode.getLocalName());
        // Advance to signature node within header
        org.w3c.dom.Node sigNode = headerNode.getFirstChild();

        if (sigNode == null) {
            return null;
        }

        //System.err.println("sigNode: " + sigNode);
        while ((sigNode.getLocalName() == null) ||
                (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
            sigNode = sigNode.getNextSibling();

            if (sigNode == null) {
                return null;
            }
        }

        //Desired Signature element may be inside a SOAP-SEC signature element
        if (!sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
            sigNode = sigNode.getFirstChild();

            while ((sigNode.getLocalName() == null) ||
                    (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
                sigNode = sigNode.getNextSibling();
            }
        }

        if (sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
            if (sigNode instanceof org.w3c.dom.Element) {
                sigElement = (org.w3c.dom.Element) sigNode;
            }
        }

        return sigElement;
    }

    /**
     *     Convert the apostrophes in a String field in a SQL statement to 2
     *     consecutive apostrohes
     */
    public static String escapeSQLChars(String stringField) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < stringField.length(); i++) {
            char c = stringField.charAt(i);
            result.append(c);

            if (c == '\'') {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public static Utility getInstance() {
        if (instance == null) {
            synchronized (Utility.class) {
                if (instance == null) {
                    instance = new Utility();
                }
            }
        }

        return instance;
    }

    /*********************************************************/

    /*  Methods refactored from LifeCicleManagerImpl - START */

    /*********************************************************/
    /**
     * Check if id is a proper UUID. If not make a proper UUID based URN and add
     * a mapping in idMap between old and new Id. The parent attribute of ClassificationNode
     * will be also fixed here according to their hierarchy if the parent attributes are
     * not provided explicitely by th clients.
     *
     * @param ids The List holding all the UUIDs in the SubmitObjectsRequest
     * document
     * @throws UUIDNotUniqueException if any UUID is not unique within a
     * SubmitObjectsRequest
     */
    private void checkId(List ids, java.util.HashMap idMap,
        RegistryObjectType ro) throws RegistryException {
        try {
            String id = ro.getId();

            // Check for uniqueness
            if ((ids != null) && ids.contains(id)) {
                throw new org.freebxml.omar.server.common.UUIDNotUniqueException(id);
            }

            if (id != null) {
                ids.add(id);
            }

            if ((id == null) || !id.startsWith("urn:uuid:")) {
                // Generate UUID if the request does not provide ID for a RO
                // or it does not start with urn:uuid:
                org.freebxml.omar.common.UUID uuid = uf.newUUID();
                String newId = "urn:uuid:" + uuid;
                ro.setId(newId);
                idMap.put(id, newId);
            } else {
                // id starts with "urn:uuid:"
                String uuidStr = id.substring(9);

                if (!uf.isValidUUID(uuidStr)) {
                    // but invalid
                    org.freebxml.omar.common.UUID uuid = uf.newUUID();
                    String newId = "urn:uuid:" + uuid;
                    idMap.put(id, newId);
                }
            }
            
            //Now checkId for all composed objects
            Set composedObjects = BindingUtility.getInstance().getComposedRegistryObjects(ro, -1);
            Iterator iter = composedObjects.iterator();
            while (iter.hasNext()) {
                RegistryObjectType obj = (RegistryObjectType)iter.next();
                checkId(ids, idMap, ro);
            }
            

        } catch (org.freebxml.omar.common.OMARException e) {
            throw new RegistryException(e);
        }
    }


    /*********************************************************/

    /*   Methods refactored from LifeCicleManagerImpl - END  */

    /*********************************************************/
    public String mapTableName(String name) {
        String newName = (String) tableNameMap.get(name.toLowerCase().trim());

        if (newName == null) {
            newName = name;
        }

        return newName;
    }

    public String mapColumnName(String name) {
        String newName = (String) columnNameMap.get(name.toLowerCase().trim());

        if (newName == null) {
            newName = name;
        }

        return newName;
    }
}
