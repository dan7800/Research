/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated
 * documentation (the "Software") whose owner (the "Licensor") has placed the
 * statement "Licensed under the Academic Free License Version 1.0" immediately
 * after the copyright notice that applies to the Software.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of the Software (1) to use, copy, modify, merge, publish, perform,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, and (2) under patent
 * claims owned or controlled by the Licensor that are embodied in the Software
 * as furnished by the Licensor, to make, use, sell and offer for sale the
 * Software and derivative works thereof, subject to the following conditions:
 *
 * - Redistributions of the Software in source code form must retain all
 *   copyright notices in the Software as furnished by the Licensor, this list
 *   of conditions, and the following disclaimers.
 * - Redistributions of the Software in executable form must reproduce all
 *   copyright notices in the Software as furnished by the Licensor, this list
 *   of conditions, and the following disclaimers in the documentation and/or
 *   other materials provided with the distribution.
 * - Neither the names of Licensor, nor the names of any contributors to the
 *   Software, nor any of their trademarks or service marks, may be used to
 *   endorse or promote products derived from this Software without express
 *   prior written permission of the Licensor.
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved.
 * Permission is hereby granted to copy and distribute this license without
 * modification. This license may not be modified without the express written
 * permission of its copyright owner.
 */

/* =====
 *
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/transport/Http.java,v 1.12 2003/12/15 09:51:35 bobpykoon Exp $
 *
 * Code authored by:
 *
 * cyng [2003-06-09]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.message.transport;

import com.sun.net.ssl.HostnameVerifier;
import com.sun.net.ssl.HttpsURLConnection;
import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.handler.Constants;
import hk.hku.cecid.phoenix.message.handler.ErrorMessages;
import hk.hku.cecid.phoenix.message.handler.InitializationException;
import hk.hku.cecid.phoenix.message.handler.MessageServer;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.pki.KeyStoreKeyManager;
import hk.hku.cecid.phoenix.pki.KeyStoreTrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Logger;

/**
 * Transport layer to send and receive <code>SOAPMessage</code> via HTTP
 * synchronously.
 *
 * @author cyng
 * @version $Revision: 1.12 $
 */
public final class Http {

    private static final String HTTP_METHOD = "POST";

    private static final String AUTHORIZATION = "Authorization";

    private static final String PROTOCOL_HANDLER_PKGS =
        "java.protocol.handler.pkgs";

    private static final String SSL_WWW_PROTOCOL =
        "com.sun.net.ssl.internal.www.protocol";

    private static String SSL_SSL_PROVIDER =
        "com.sun.net.ssl.internal.ssl.Provider";

    static Logger logger = Logger.getLogger(Http.class);

    private static String encoding =
        Constants.DEFAULT_CONTENT_TRANSFER_ENCODING;

    private static HostnameVerifier hostnameVerifier = null;
    private static TrustManager[] trustManagers = null;
    private static Map keyManagerMap = null;
    private static KeyManager defaultKeyStoreKeyManager = null;
    //private static SSLSocketFactory sslSocketFactory;
    public static void configure(Property prop) throws InitializationException {
        String s = prop.get(Constants.PROPERTY_CONTENT_TRANSFER_ENCODING);
        if (s != null && !s.equals("")) {
            encoding = s;
        }
        /*
         HTTPS Connection settings
         */
        /*
         Set the custom HostnameVerifier if it is set on the properties.
         The HostnameVerifier is used for the case that the URL's hostname
         and the server's identification hostname mismatch
         */
        logger.debug("Configure HTTPS");
        String hostnameVerifierClassname = prop.get(
                Constants.PROPERTY_SSL_HOSTNAME_VERIFIER);
        if (hostnameVerifierClassname != null
                && !hostnameVerifierClassname.equals("")) {
            logger.debug("Use custom Hostname Verifier on SSL : "
                    + hostnameVerifierClassname);
            //HostnameVerifier hostnameVerifier = null;
            try {
                 hostnameVerifier = (HostnameVerifier)
                        Class.forName(hostnameVerifierClassname).newInstance();
            } catch (Exception e) {
                String err = ErrorMessages.getMessage
                    (ErrorMessages.ERR_HERMES_INIT_ERROR, e.getMessage());
                logger.error(err, e);
                throw new InitializationException(err);
            }
            //HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        }
        /*
         Get the trust certificates on SSL if it is set on the properties.
         */
        String trustedStorePath = prop.get
            (Constants.PROPERTY_SSL_TRUST_KEY_STORE_PATH, "");
        String trustedStoreFile = prop.get
            (Constants.PROPERTY_SSL_TRUST_KEY_STORE_FILE);
        String trustedStorePassword = prop.get
            (Constants.PROPERTY_SSL_TRUST_KEY_STORE_PASSWORD, "");
        if (trustedStorePath.equals("")) {
            trustedStorePath = System.getProperty(
                    Constants.PROPERTY_USER_HOME);
        }
        File realTrustStoreFile = new File(trustedStorePath + File.separator
            + trustedStoreFile);
        TrustManager[] trustManagers = null;
        KeyManager[] keyManagers = null;
        if (realTrustStoreFile.exists() && realTrustStoreFile.isFile()) {
            logger.debug("Use SSL trusted keystore : " + realTrustStoreFile);
            KeyStore keystore = null;
            InputStream istream = null;
            /*
             load the trust certificate keystore.
             */
            try {
                keystore = KeyStore.getInstance("JKS");
                istream = new FileInputStream(realTrustStoreFile);
                keystore.load(istream, trustedStorePassword.toCharArray());
                /*
                 init the trust manager using trust certificate keystore.
                 */
                trustManagers = new TrustManager[]{
                        new KeyStoreTrustManager(keystore)};
            } catch (Exception e) {
                logger.warn("Cannot load SSL Trust Keystore : "
                        + e.getMessage());
                logger.warn("Use Default SSL Trust Keystore settings");
            } finally {
                try {
                    if (istream != null) {
                        istream.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        keyManagerMap = makeKeyManagerMap(prop);
    }

    private static Map makeKeyManagerMap(Property prop)
            throws InitializationException {
        Map result = new HashMap();
        String[] values = prop.getMultiple(Constants.PROPERTY_SSL_CLIENT_AUTH);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                String prefix = Constants.PROPERTY_SSL_CLIENT_AUTH + "[" + i + "]";
                String urlString = prop.get(prefix + "/" + Constants.PROPERTY_URL);
                String keystorePath = prop.get(
                        prefix + "/" + Constants.PROPERTY_KEY_STORE_PATH, "");
                String keystoreFile = prop.get(
                        prefix + "/" + Constants.PROPERTY_KEY_STORE_FILE, "");
                String keystoreAlias = prop.get(
                        prefix + "/" + Constants.PROPERTY_KEY_STORE_ALIAS, "");
                String keystorePassword = prop.get(
                        prefix + "/" + Constants.PROPERTY_KEY_STORE_PASSWORD,
                                "");
                File realKeyStoreFile = new File(keystorePath + File.separator
                        + keystoreFile);
                URL url = null;
                if (!(realKeyStoreFile.exists()
                        && realKeyStoreFile.isFile())) {
                    logger.warn("KeyStoreFile not exist or is not a file : "
                            + realKeyStoreFile.toString());
                    realKeyStoreFile = null;
                }
                if (urlString != null ) {
                    try {
                        url = new URL(urlString);
                    } catch (java.net.MalformedURLException e) {
                        logger.warn("Malformed url for SSL Client auth '"
                                + urlString + "' : " + e.getMessage());
                    }
                }
                if (realKeyStoreFile != null
                        && (urlString == null || url != null)) {
                    KeyManager keyManager = null;
                    try {
                        keyManager = new KeyStoreKeyManager(
                                realKeyStoreFile, keystoreAlias,
                                        keystorePassword.toCharArray());
                    } catch (KeyStoreException e) {
                        String err = "Cannot load the keystore on SSL "
                                + "client authentication : " + e.getMessage();
                        logger.error(err);
                        throw new InitializationException(err);
                    }
                    if (keyManager != null) {
                        if (url != null) {
                            logger.info("Add SSL Client Authentication entry : "
                                    + url + " " + realKeyStoreFile);
                            result.put(url, keyManager);
                        } else {
                            logger.info("Add default SSL Client "
                                    + "Authentication entry : "
                                    + realKeyStoreFile);
                            defaultKeyStoreKeyManager = keyManager;
                        }
                    }
                } else {
                    logger.warn("Ignore this SSL Client Authenication setting");
                }
            }
        }
        return result;
    }

    private static SSLSocketFactory makeSSLSocketFactory(
            KeyManager[] keyManagers, TrustManager[] trustManagers)
                    throws InitializationException {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(keyManagers, trustManagers, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            String err = ErrorMessages.getMessage
                (ErrorMessages.ERR_HERMES_INIT_ERROR, e.getMessage());
            logger.error(err, e);
            throw new InitializationException(err);
        }
    }

    /**
     * Send an <code>EbxmlMessage</code> synchronously to the given URL and
     * block the thread until a response is received.
     *
     * @param message       <code>EbxmlMessage</code> to be sent.
     * @param toUrl         Destination URL for the message to be sent to.
     *
     * @return the <code>SOAPMessage</code> which is the response of the
     *         message that was sent.
     * @throws TransportException
     */
    public static EbxmlMessage send(EbxmlMessage message, String toUrl)
        throws TransportException {

        logger.debug("=> Http.send");
        logger.info("Sending message to " + toUrl);
        /*
        boolean hasAttachments = message.getPayloadContainers().hasNext();

        try {
            // set content-transfer-encoding
            if (hasAttachments) {
                Utility.addContentTransferEncoding(message,
                    Constants.DEFAULT_CONTENT_TRANSFER_ENCODING, encoding);
                message.saveChanges();
            }
        }
        catch (SOAPException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT));
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new TransportException(err);
        }

        // modify content type to a single line
        String type = message.getMimeHeaders().getHeader
            (Constants.CONTENT_TYPE)[0];
        int index = type.indexOf("\n");
        if (index != -1) {
            type = type.substring(0, index).trim() + " " +
                type.substring(index+1, type.length()).trim();
        }
        index = type.indexOf("\r");
        if (index != -1) {
            type = type.substring(0, index).trim() + " " +
                type.substring(index+1, type.length()).trim();
        }
        if (type.toLowerCase().indexOf(Constants.CHARACTER_SET) == -1) {
            type += "; " + Constants.CHARACTER_SET + "=\"" +
                Constants.CHARACTER_ENCODING + "\"";
        }
        if (type.toLowerCase().indexOf(Constants.MULTIPART_RELATED) != -1
            && type.toLowerCase().indexOf(Constants.START) == -1) {
            type += "; " + Constants.START + "=\"<" +
                EbxmlMessage.SOAP_PART_CONTENT_ID + ">\"";
        }

        message.getMimeHeaders().setHeader(Constants.CONTENT_TYPE, type);
        */
        EbxmlMessage responseMessage = null;
        try {
            URL url = new URL(toUrl);
            if (url.getProtocol().equalsIgnoreCase
                (Constants.TRANSPORT_TYPE_HTTPS)) {
                String pkgs = System.getProperty(PROTOCOL_HANDLER_PKGS);
                if (pkgs == null || pkgs.indexOf(SSL_WWW_PROTOCOL) < 0 ) {
                    pkgs = (pkgs == null ? SSL_WWW_PROTOCOL :
                            SSL_WWW_PROTOCOL + "|" + pkgs);
                    System.setProperty(PROTOCOL_HANDLER_PKGS, pkgs);
                }
            }
            HttpURLConnection connection = (HttpURLConnection)
                url.openConnection();
            logger.debug("Connection class : " + connection.getClass());
            logger.debug("Instance of HttpsURLConnection : "
                    + (connection instanceof HttpsURLConnection));
            if (connection instanceof HttpsURLConnection) {
                logger.info("Configuration to a HTTPS connection");
                HttpsURLConnection httpsConnection
                        = (HttpsURLConnection) connection;
                if (hostnameVerifier != null) {
                    httpsConnection.setHostnameVerifier(hostnameVerifier);
                }
                KeyManager[] keyManagers = null;
                if (keyManagerMap != null) {
                    KeyManager keyManager = (KeyManager) keyManagerMap.get(url);
                    if (keyManager != null) {
                        logger.debug("use key manager for url : " + url);
                        keyManagers = new KeyManager[]{keyManager};
                    } else if (defaultKeyStoreKeyManager != null) {
                        logger.debug("use default key manager");
                        keyManagers
                                = new KeyManager[]{defaultKeyStoreKeyManager};
                    }
                }
                if (trustManagers != null || keyManagers != null) {
                    SSLSocketFactory sslSocketFactory = makeSSLSocketFactory(
                            keyManagers, trustManagers);
                    httpsConnection.setSSLSocketFactory(sslSocketFactory);
                }
            }
            connection.setRequestMethod(HTTP_METHOD);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            HttpURLConnection.setFollowRedirects(true);

            boolean hasAuthorization = false;
            //MimeHeaders headers = message.getSOAPMessage().getMimeHeaders();
            Map headers = message.getMimeHeaders(
                Constants.DEFAULT_CONTENT_TRANSFER_ENCODING, encoding);
            //for (Iterator i=headers.getAllHeaders() ; i.hasNext() ; ) {
            for (Iterator i=headers.entrySet().iterator() ; i.hasNext() ; ) {
                /*
                MimeHeader header = (MimeHeader) i.next();
                String[] values = headers.getHeader(header.getName());
                String value = header.getValue();
                if (values.length > 1) {
                    for (int j=1 ; j<values.length ; j++) {
                        value += "," + values[j];
                    }
                }
                connection.setRequestProperty(header.getName(), value);
                hasAuthorization = header.getName().equals(AUTHORIZATION);
                */
                Map.Entry entry = (Map.Entry) i.next();
                connection.setRequestProperty((String) entry.getKey(),
                                              (String) entry.getValue());
                hasAuthorization = entry.getKey().equals(AUTHORIZATION);
            }

            if (hasAuthorization) {
                throw new Error("HTTP Authorization not yet implemented!");
            }

            OutputStream os = connection.getOutputStream();
            message.writeTo(os, Constants.DEFAULT_CONTENT_TRANSFER_ENCODING,
                            encoding);
            os.flush();
            os.close();
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                MimeHeaders mimeHeaders = new MimeHeaders();
                String key = connection.getHeaderFieldKey(1);
                String value = connection.getHeaderField(1);
                for (int i=2 ; key != null ; i++) {
                    StringTokenizer values = new StringTokenizer(value, ",");
                    while (values.hasMoreTokens()) {
                        mimeHeaders.addHeader(key, values.nextToken().trim());
                    }
                    key = connection.getHeaderFieldKey(i);
                    value = connection.getHeaderField(i);
                }
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[65536];
                for (int c=is.read(buffer) ; c != -1 ; c=is.read(buffer)) {
                    baos.write(buffer, 0, c);
                }
                byte[] bytes = baos.toByteArray();
                baos = null;
                is.close();
                int contentLength = connection.getContentLength();
                if (contentLength != -1 && contentLength != bytes.length) {
                    String err = ErrorMessages.getMessage(ErrorMessages.
                        ERR_HERMES_HTTP_POST_FAILED, Constants.CONTENT_LENGTH +
                        " = " + contentLength + " but only " + bytes.length +
                        " bytes are successfully read from response stream");
                    logger.error(err);
                    throw new TransportException(err);
                }

                if (bytes.length > 0) {
                    SOAPMessage responseSoapMessage
                            = MessageFactory.newInstance().createMessage(
                                    mimeHeaders, new ByteArrayInputStream(
                                            bytes));
                    byte[] soapEnvelope
                            = MessageServer.getSoapEnvelopeBytesFromStream(
                                    new ByteArrayInputStream(bytes));
                    responseMessage = new EbxmlMessage(responseSoapMessage);
                    responseMessage.setSoapEnvelopeBytes(soapEnvelope);
                }
            }
            else if ((responseCode/100) != (HttpURLConnection.HTTP_OK/100)) {
                String err = ErrorMessages.getMessage(ErrorMessages.
                    ERR_HERMES_HTTP_POST_FAILED, "Bad response: code = " +
                    responseCode + " and message = " + connection.
                    getResponseMessage());
                logger.error(err);
                throw new TransportException(err);
            }
            connection.disconnect();
        }
        catch (TransportException e) {
            throw e;
        }
        catch (Exception e) {
            String err =
                ((e instanceof SOAPException || e instanceof IOException) ?
                 ErrorMessages.getMessage
                 (ErrorMessages.ERR_SOAP_CANNOT_SEND_MESSAGE, e) :
                 ErrorMessages.getMessage
                 (ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e));
            logger.error(err);
            throw new TransportException(err);
        }
        /*
        try {
            // unset content-transfer-encoding
            if (hasAttachments) {
                Utility.removeContentTransferEncoding(message);
                message.saveChanges();
            }
        }
        catch (SOAPException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT));
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new TransportException(err);
        }
        */
        logger.debug("<= Http.send");
        return responseMessage;
    }
}
