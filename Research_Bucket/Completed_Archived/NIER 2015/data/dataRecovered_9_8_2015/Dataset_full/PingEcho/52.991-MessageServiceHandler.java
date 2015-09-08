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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/MessageServiceHandler.java,v 1.186 2004/02/10 03:12:52 bobpykoon Exp $
 *
 * Code authored by:
 *
 * cyng [2002-03-21]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.message.handler;

import hk.hku.cecid.phoenix.common.util.AuthenticationManager;
import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.packaging.AckRequested;
import hk.hku.cecid.phoenix.message.packaging.Acknowledgment;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessageFactory;
import hk.hku.cecid.phoenix.message.packaging.ErrorList;
import hk.hku.cecid.phoenix.message.packaging.MessageOrder;
import hk.hku.cecid.phoenix.message.packaging.Signature;
import hk.hku.cecid.phoenix.message.packaging.SignatureException;
import hk.hku.cecid.phoenix.message.packaging.StatusRequest;
import hk.hku.cecid.phoenix.message.packaging.StatusResponse;
import hk.hku.cecid.phoenix.message.packaging.validation.EbxmlMessageValidator;
import hk.hku.cecid.phoenix.message.packaging.validation.EbxmlValidationException;
import hk.hku.cecid.phoenix.message.packaging.validation.SOAPValidationException;
import hk.hku.cecid.phoenix.message.transport.Mail;
import hk.hku.cecid.phoenix.message.transport.TransportException;
import hk.hku.cecid.phoenix.pki.CertResolver;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.activation.DataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.messaging.JAXMException;
import javax.xml.messaging.ProviderMetaData;
import javax.xml.messaging.ReqRespListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Message Service Handler that supports asynchronous communication and
 * reliable messaging.
 *
 * @author cyng
 * @version $Revision: 1.186 $
 */
public class MessageServiceHandler extends HttpServlet
    implements ReqRespListener {

    static Logger logger = Logger.getLogger(MessageServiceHandler.class);

    /**
     * Service name reserved for services described in ebXML Message Service
     * Specification [ebMSS 3.1.4].
     @deprecated please use Constants.SERVICE
     */
    public static final String SERVICE =
        "urn:oasis:names:tc:ebxml-msg:service";

    /**
     * Acknowledgement action [ebMSS 6.3.2.7]
     * @deprecated please use Constants.ACTION_ACKNOWLEDGMENT
     */
    public static final String ACTION_ACKNOWLEDGMENT = "Acknowledgment";

    /**
     * Action for an ErrorList element to be included in an independent
     * message [ebMSS 4.2.4.3]
     * @deprecated please use Constants.ACTION_MESSAGE_ERROR
     */
    public static final String ACTION_MESSAGE_ERROR = "MessageError";

    /**
     * Action for ping message [ebMSS 8.1]
     * @deprecated please use Constants.ACTION_PING
     */
    public static final String ACTION_PING = "Ping";

    /**
     * Action for pong message [ebMSS 8.2]
     * @deprecated please use Constants.ACTION_PONG
     */
    public static final String ACTION_PONG = "Pong";

    /**
     * Action for status request message [ebMSS 7.1.1]
     * @deprecated please use Constants.ACTION_STATUS_REQUEST
     */
    public static final String ACTION_STATUS_REQUEST = "StatusRequest";

    /**
     * Action for status response message [ebMSS 7.1.2]
     * @deprecated please use Constants.ACTION_STATUS_RESPONSE
     */
    public static final String ACTION_STATUS_RESPONSE = "StatusResponse";

    /**
     * HTTP Header attribute specifying content type
     */
    public static final String CONTENT_TYPE = Constants.CONTENT_TYPE;

    /**
     * HTTP Header attribute specifying content length.
     */
    public static final String CONTENT_LENGTH = Constants.CONTENT_LENGTH;

    /**
     * HTTP content type specifying xml data.
     */
    public static final String TEXT_XML_TYPE = Constants.TEXT_XML_TYPE;

    /**
     * HTTP content type for multi-part data, which is used when the ebXML
     * message contains payload data.
     */
    public static final String MULTIPART_RELATED_TYPE =
        Constants.MULTIPART_RELATED_TYPE;

    /**
     * Prefix to be applied to separate different parts of MIME data.
     */
    public static final String MIME_BOUNDARY_PREFIX =
        Constants.MIME_BOUNDARY_PREFIX;

    /**
     * Default XML character encoding.
     */
    public static final String CHARACTER_ENCODING =
        Constants.CHARACTER_ENCODING;

    /** Implementation of <code>javax.xml.messaging.ProviderMetaData</code>.
        Information about this <code>MessageServiceHandler</code>.
    */
    public static final class MetaData implements ProviderMetaData {

//        static Logger logger = Logger.getLogger(MetaData.class);
        /**
         * Messaging service provider major version
         */
        private static final int MAJOR_VERSION = 2;

        /**
         * Messaging service provider minor version
         */
        private static final int MINOR_VERSION = 0;

        /**
         * Name of the message service provider
         */
        private static final String NAME =
            MessageServiceHandler.class.getName();

        /**
         * Profiles supported by this provider. Currently only "ebxml"
         * is supported.
         */
        private static final String[] SUPPORTED_PROFILES = {"ebxml"};

        /**
         * Empty constructor
         */
        MetaData() {}

        /**
         * Get major version number of the message service provider
         */
        public int getMajorVersion() {
            return MAJOR_VERSION;
        }

        /**
         * Get minor version number of the message service provider
         */
        public int getMinorVersion() {
            return MINOR_VERSION;
        }

        /**
         * Get the name of the message service provider.
         */
        public String getName() {
            return NAME;
        }

        /**
         * Get the list of supported profiles.
         */
        public String[] getSupportedProfiles() {
            return SUPPORTED_PROFILES;
        }

        /**
         * Get the message factory for the specified profile.
         */
        public MessageFactory getMessageFactory(String profile)
            throws JAXMException {
            if (profile.equalsIgnoreCase("ebxml")) {
                return new EbxmlMessageFactory();
            }
            else {
                throw new JAXMException("Unsupported MessageFactory profile: "
                                        + profile);
            }
        }

        /**
         * Get the release number of this <code>MessageServiceHandler</code>
         * implementation.
         */
        public String getRelease() {
            return Utility.getRelease();
        }
    }

    /**
     * Thread class used to monitor a specified e-mail account.
    */
    private static final class MailMonitor extends Thread {

        private static String protocol;

        private static String host;

        private static String port;

        private static String folder;

        private static String user;

        private static String password;

        private static long monitorInterval;

        /** Flag indicating if the class has been configured.  */
        protected static boolean isConfigured = false;

        /**
         * Configure the class if and only if it has not been configured.
         *
         * @param prop <code>Property</code> object.
         */
        static void configure(Property prop) throws InitializationException {

            if (isConfigured) return;

            protocol = prop.get(Constants.PROPERTY_MAIL_PROTOCOL);
            host = prop.get(Constants.PROPERTY_MAIL_HOST);
            port = prop.get(Constants.PROPERTY_MAIL_PORT);
            folder = prop.get(Constants.PROPERTY_MAIL_FOLDER);
            user = prop.get(Constants.PROPERTY_MAIL_USER);
            password = prop.get(Constants.PROPERTY_MAIL_PASSWORD);

            long interval = 0;
            try {
                final String monitorInterval = prop.
                    get(Constants.PROPERTY_MAIL_MONITOR_INTERVAL);
                if (monitorInterval == null) {
                    interval = Constants.MAIL_MIN_WAIT_INTERVAL;
                }
                else {
                    interval = Long.valueOf(monitorInterval).longValue();
                }
            }
            catch (NumberFormatException e) {
                interval = Constants.MAIL_MIN_WAIT_INTERVAL;
            }
            monitorInterval = interval;

            if (protocol == null || host == null || port == null ||
                folder == null || user == null || password == null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_PROPERTY_NOT_SET,
                    "MSH/Mail/*: mail polling disabled");
                logger.warn(err);
            }
            else {
                logger.info("Incoming mail configuration:");
                logger.info("  Protocol: " + protocol);
                logger.info("  POP/IMAP host: " + host);
                logger.info("  POP/IMAP port: " + port);
                logger.info("  POP/IMAP folder: " + folder);
                logger.info("  POP/IMAP user: " + user);
                logger.info("  POP/IMAP password: <not shown>");
                logger.info("  Polling interval: " + interval);
            }

            isConfigured = true;
        }

        private final MessageServiceHandler msh;

        private long waitInterval = monitorInterval;

        private boolean shutDown = false;

        private boolean isSuspended = false;

        private String exceptionMessage = null;

        MailMonitor(MessageServiceHandler msh) {
            this.msh = msh;
        }

        /**
         * Poll the mail account and check if an ebXML message has been received
         * or not. It also verifies the digital signature and invoke onMessage
         * event of the associated message listener.
         */
        public void run() {

            if (protocol == null || host == null || port == null ||
                folder == null || user == null || password == null) {
                return;
            }

            while (!shutDown) {
                long startTime = System.currentTimeMillis();
                Exception exception = null;
                try {
                    while (isSuspended) {
                        synchronized (this) {
                            this.wait();
                        }
                    }
                    logger.debug("polling mail server for messages");
                    final Mail.ReceivedMessage[] receivedMessages =
                        Mail.receive(protocol, host, port, folder, user,
                                     password);
                    logger.debug("got " + receivedMessages.length + " messages");
                    for (int i=0 ; i < receivedMessages.length ; i++) {
                        try {
                            final HashMap requestProperty = new HashMap();
//                            requestProperty.put(DbTableManager.
//                                 ATTRIBUTE_REMOTE_ADDRESS, folder + ", "
//                                 + user);
                            String remoteAddress = user;
                            if (user.length() > 16) {
                                remoteAddress = user.substring(0, 16);
                            }
                            requestProperty.put(DbTableManager.
                                 ATTRIBUTE_REMOTE_ADDRESS, remoteAddress);
                            requestProperty.put(DbTableManager.
                                 ATTRIBUTE_REMOTE_HOST, host + ":" + port);

                            logger.info("Received a message from SMTP");
                            msh.onMessage(receivedMessages[i].getMessage(),
                                requestProperty);
                        }
                        catch (MessageServiceHandlerException mshe) {
                            // error already logged
                        }
                    }
                    startTime = System.currentTimeMillis();
                    idle(waitInterval);
                }
                catch (TransportException e) {
                    exception = e;
                    exceptionMessage = e.getMessage();
                }
                catch (InterruptedException e) {
                    exception = e;
                }
                catch (Exception e) {
                    exception = e;
                    exceptionMessage = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                    logger.error(exceptionMessage);
                }
                if (exception != null) {
                    if (!(exception instanceof InterruptedException)) {
                        startTime = System.currentTimeMillis();
                    }
                    long endTime = System.currentTimeMillis();
                    while ((endTime - startTime) < waitInterval) {
                        try {
                            idle(waitInterval - (endTime - startTime));
                            break;
                        }
                        catch (InterruptedException ie) {}
                        endTime = System.currentTimeMillis();
                    }
                }
            }
        }

        /**
         * Shutdown the polling thread.
         */
        synchronized void shutDown() {
            shutDown = true;
            this.notify();
        }

        /**
         * Suspend the polling thread
         */
        synchronized void haltPolling() {
            isSuspended = true;
            this.notify();
        }

        /**
         * Resume the polling thread
         */
        synchronized void resumePolling() {
            isSuspended = false;
            this.notify();
        }

        /**
         * Get the last exception message, if any.
         */
        String getExceptionMessage() {
            return exceptionMessage;
        }

        private synchronized void idle(long interval)
            throws InterruptedException {
            this.wait(interval);
        }
    }

    private static final class Monitor {
        private boolean halted = false;

        private int request = 0;

        synchronized boolean startRequest() {
            if (!halted) {
                request++;
            }
            return !halted;
        }

        synchronized void endRequest() {
            request--;
            this.notify();
        }

        synchronized void halt() {
            halted = true;
            while (request > 0) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {}
            }
        }

        synchronized void resume() {
            halted = false;
        }

        synchronized boolean isHalted() {
            return halted;
        }
    }

    static final class Delivery extends Thread {

        private final MessageServiceHandler msh;

        private final MessageServer messageServer;

        private final ApplicationContext appContext;

        private final MessageListener messageListener;

        private final EbxmlMessage ebxmlMessage;

        private Exception exception;

        Delivery(MessageServiceHandler msh, ApplicationContext appContext,
                 MessageListener messageListener, EbxmlMessage ebxmlMessage) {
            this.msh = msh;
            this.messageServer = msh.getMessageServer();
            this.appContext = appContext;
            this.messageListener = messageListener;
            this.ebxmlMessage = ebxmlMessage;
            this.exception = null;
        }

        public void run() {
            try {
                URL clientUrl = messageListener.getClientUrl();
                if (clientUrl == null) {
                    return;
                }

                String protocol = clientUrl.getProtocol();
                boolean isFileProtocol = protocol.equalsIgnoreCase
                    (MessageListener.PROTOCOL_FILE);
                if (messageListener instanceof ClientMessageListenerImpl &&
                    isFileProtocol) {
                    return;
                }

                logger.debug("delivering message to Application");
                Transaction tx = null;
                Exception ex = null;
                try {
                    messageListener.onMessage(ebxmlMessage);
                    tx = new Transaction(MessageServer.dbConnectionPool);
                    messageServer.setDeliveryStatus(ebxmlMessage.
                        getMessageId(), true, tx);
                    tx.commit();
                }
                catch (TransactionException e) {
                    ex = e;
                }
                catch (MessageServerException e) {
                    ex = e;
                }
                catch (Exception e) {
                    ex = e;
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                    logger.error(err);
                }
                finally {
                    if (ex != null) {
                        try {
                            if (tx != null) {
                                tx.rollback();
                            }
                        }
                        catch (Throwable e2) {}
                        ex = null;
                    }
                }

                ArrayList results = msh.getUndeliveredMessages(
                    appContext, false);
                PersistenceHandler handler
                        = PersistenceManager.getRepositoryPersistenceHandler();
                for (int i=0 ; i<results.size() ; i++) {
                    tx = null;
                    try {
                        /*
                        String fileName = (String) results.get(i);
                        EbxmlMessage ebxmlMessage = MessageServer.
                            getMessageFromFile(new File(fileName));
                         */
                        String fileName = (String) results.get(i);
                        DataSource source = handler.getObject(fileName);
                        EbxmlMessage ebxmlMessage = (EbxmlMessage)
                                MessageServer.getMessageFromDataSource(
                                        source, true); 
                        messageListener.onMessage(ebxmlMessage);
                        tx = new Transaction(MessageServer.dbConnectionPool);
                        messageServer.setFileDeliveryStatus(fileName, true, tx);
                        tx.commit();
                    }
                    catch (MessageServerException e) {
                        ex = e;
                    }
                    catch (Exception e) {
                        ex = e;
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                        logger.error(err);
                    }
                    finally {
                        if (ex != null) {
                            try {
                                if (tx != null) {
                                    tx.rollback();
                                }
                            }
                            catch (Throwable e2) {}
                            ex = null;
                        }
                    }
                }
                logger.debug("message delivered to Application");
            }
            catch (MessageServiceHandlerException e) {
                exception = e;
            }
            catch (Exception e) {
                exception = e;
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
            }
        }

        Exception getException() {
            return exception;
        }
    }

    static final String WILD_CARD = "*";

    /**
     * File name delimiter, act as a separator of different parts of file name.
     */
    private static final String DELIMITER = ";";

    /**
     * The number of files to be tested in the persistence layer test.
     */
    private static final int TEST_PERSISTENCE_FILE_NUMBER = 10;

    /**
     * The length of the file which is to be written to the message repository
     * in the persistence layer test.
     */
    private static final long TEST_PERSISTENCE_FILE_LENGTH = 1048576;

    /**
     * Size of memory buffer for read/write operations.
     */
    private static final int BLOCK_SIZE = 4096;

    /**
     * Maximum sequence number beyond which the sequence number would be wrapped
     * around. It is defined to be 99999999 in ebMSS 9.1.1.
     */
    public static final int MAX_SEQUENCE_NUMBER = 99999999;

    /**
     * The range of sequence number.
     */
    public static final int RANGE_SEQUENCE_NUMBER = MAX_SEQUENCE_NUMBER + 1;

    static final int NORMAL_LEVEL = -1;

    static final int HALT_SUSPEND_LEVEL = 0;

    static final int HALT_TERMINATE_LEVEL = 1;

    static final int HALT_DELETE_LEVEL = 2;

    /**
     * XML messaging service provider meta data.
     */
    private static final MetaData metaData = new MetaData();

    private static final HashSet trustedListenerRepository = new HashSet();

    private static String messageRepository = null;

    private static String mshUrl = null;

    /**
     * Keystore settings
     */
    private static String trustedStorePath = null;

    private static String trustedStoreFile = null;

    private static String trustedStorePassword = null;

    /**
     * Digital signature settings
     */
    private static String keystoreAlias = null;

    private static String keyAlg = null;

    private static String keystorePath = null;

    private static String keystoreFile = null;

    private static String keystorePassword = null;

    private static CertResolver certResolver = null;

    /**
     * Command authentication password file.
     */
    private static String authPasswordFile = null;

    /* Initialize the message factory by getting an instance through default
       factory finder. */
    private static MessageFactory messageFactory;

    /* Initialze transformer factory by getting an instance through default
     * factory finder. */
    public static TransformerFactory transformerFactory;

    /** Flag indicating if the class has been configured.  */
    protected static boolean isConfigured = false;

    /**
     * Configure the class if and only if it has not been configured.
     *
     * @param prop <code>Property</code> object.
     */
    static synchronized void configure(Property prop)
        throws InitializationException {

        if (isConfigured) return;

        // Load logger settings
        Utility.configureLogger(prop, "hk.hku.cecid.phoenix.message");
        Utility.configureLogger(prop, "hk.hku.cecid.phoenix.pki");

        // Load proxy settings
        String proxyHost = prop.get(Constants.PROPERTY_PROXY_HOST, "");
        String proxyPort = prop.get(Constants.PROPERTY_PROXY_PORT, "");
        if (!proxyHost.equals("") && !proxyPort.equals("")) {
            Properties props = System.getProperties();
            props.put(Constants.PROPERTY_HTTP_PROXY_HOST, proxyHost);
            props.put(Constants.PROPERTY_HTTP_PROXY_PORT, proxyPort);
        }

        // Trusted repository and message repository
        String trustedRepo = prop.get
                             (Constants.PROPERTY_TRUSTED_LISTENER_REPOSITORY);
        if (trustedRepo != null) {
            final StringTokenizer listenerDirs = new StringTokenizer
                (trustedRepo, DELIMITER);
            while (listenerDirs.hasMoreTokens()) {
                try {
                    File f = new File(listenerDirs.nextToken());
                    trustedListenerRepository.add(f.getCanonicalPath());
                }
                catch (IOException e) {}
            }
        }

        messageRepository = prop.get(Constants.PROPERTY_MESSAGE_REPOSITORY);
        if (messageRepository == null) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR,
                "Missing " + Constants.PROPERTY_MESSAGE_REPOSITORY
                    + " property");
            logger.error(err);
            throw new InitializationException(err);
        }

        // Get the MSH URL
        mshUrl = prop.get(Constants.PROPERTY_MESSAGE_SERVICE_HANDLER_URL);

        // Message factory
        try {
            messageFactory = MessageFactory.newInstance();
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_MESSAGE_FACTORY, e);
            logger.error(err);
            throw new InitializationException(err);
        }

        // Transformer factory
        transformerFactory = TransformerFactory.newInstance();

        // Configure trusted key store
        trustedStorePath = prop.get
            (Constants.PROPERTY_TRUSTED_KEY_STORE_PATH, "");
        trustedStoreFile = prop.get
            (Constants.PROPERTY_TRUSTED_KEY_STORE_FILE);
        trustedStorePassword = prop.get
            (Constants.PROPERTY_TRUSTED_KEY_STORE_PASSWORD, "");
        if (trustedStorePath.equals("")) {
            trustedStorePath = System.getProperty
                               (Constants.PROPERTY_USER_HOME);
        }

        // Configure digital signature settings
        keystoreAlias = prop.get(Constants.PROPERTY_MSH_KEY_STORE_ALIAS, "");
        keyAlg = prop.get(Constants.PROPERTY_MSH_KEY_ALGORITHM);
        keystorePath = prop.get(Constants.PROPERTY_MSH_KEY_STORE_PATH, "");
        keystoreFile = prop.get(Constants.PROPERTY_MSH_KEY_STORE_FILE);
        keystorePassword = prop.get
                           (Constants.PROPERTY_MSH_KEY_STORE_PASSWORD, "");

        if (keystorePath.equals("")) {
            keystorePath = System.getProperty
                           (Constants.PROPERTY_USER_HOME);
        }

        if (keyAlg != null && keyAlg.equals("")) {
            keyAlg = null;
        }

        if (keystoreAlias.equals("") || keystorePassword.equals("")) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR,
                "Missing " + Constants.PROPERTY_MSH_KEY_STORE_ALIAS + " or "
                    + Constants.PROPERTY_MSH_KEY_STORE_PASSWORD + " property");
            logger.error(err);
            throw new InitializationException(err);
        }

        String certResolverName = prop.get(Constants.PROPERTY_CERT_RESOLVER);
        if (certResolverName != null && !certResolverName.equals("")) {
            try {
                certResolver = (CertResolver) Class.forName(certResolverName).
                    newInstance();
            }
            catch (Exception e) {
                String err = ErrorMessages.getMessage
                    (ErrorMessages.ERR_HERMES_INIT_ERROR, e);
                logger.error(err, e);
                throw new InitializationException(err);
            }
        }

        // Command authentication password file
        authPasswordFile = prop.get(Constants.PROPERTY_AUTHENTICATION_FILE);
        if (authPasswordFile == null) {
            logger.info("Authentication file not specified: "
                + "authentication disabled");
        }
        else {
            File authFile = new File(authPasswordFile);
            if (!authFile.exists() || !authFile.isFile() 
                || authFile.length() == 0) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "authentication file setup error "
                        + "(not exist/not a file/zero size)");
                logger.error(err);
                throw new InitializationException(err);
            }
        }

        isConfigured = true;
    }

    /**
     * Hash table storing the association of application contexts
     * and connections of registered applications.
     */
    private final Hashtable mshConnectionTable = new Hashtable();

    /**
     * Hash map storing the association of a message id, which is waiting
     * for the acknowledgment, and its sending thread, i.e. MessageProcessor.
     */
    private final Map sendThreadMap;

    /**
     * Hash map storing Conversation ID - (sequence number, delivery status)
     * mapping.
     */
    private final Map deliveryMap;

    /**
     * Has map for storing ConversationID - sequence number assignment map.
     */
    private final Map sentSequenceMap;

    /**
     * Reference of message server for storing messages.
     */
    private final MessageServer messageServer;

    /**
     * Reference to mail monitor object.
     */
    private final MailMonitor mailMonitor;

    /**
     * Monitor indicating if this <code>MessageServiceHandler</code> is halted
     * in order to do backup or archiving operation.
     */
    private final Monitor monitor;

    /**
     * Flag indicating if acknowledgement sending should be suppressed.
     */
    protected boolean suppressedAck;

    /**
     * Flag indicating if acknowledgement received should be ignored.
     */
    protected boolean ignoreAck;

    /**
     * Constructor. It initializes:
     * <ul>
     * <li>Message server for handling persistence of messages.</li>
     * <li>Establish connections with client application.</li>
     * <li>Restart the message sending threads before last shutdown.</li>
     * <li>Set up proxy server.</li>
     * <li>Start the mail monitor.</li>
     * </ul>
     *
     * @throws MessageServiceHandlerException
     */
    public MessageServiceHandler() throws MessageServiceHandlerException {
        super();

        try {
            Property prop = null;
            try {
                prop = Property.load(Constants.MSH_SERVER_PROPERTY_FILE);
            }
            catch (IOException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_NOT_FOUND_ERROR, e,
                    "cannot load " + Constants.MSH_SERVER_PROPERTY_FILE);
                System.err.println(err);
                throw new InitializationException(err);
            }
            configure(prop);
            logger.debug("=> MessageServiceHandler.MessageServiceHandler");
            PersistenceManager.configure(prop);
            DirectoryManager.configure(prop);
            DirectoryManager.initObjects();
            MessageProcessor.configure(prop);
            MessageServer.configure(prop);
            MessageServiceHandlerConnection.configure(prop);
            MailMonitor.configure(prop);
            HttpSender.configure(prop);
            MailSender.configure(prop);

            String className = prop.get(Constants.PROPERTY_CONFIG_LOCAL);
            if (className != null) {
                logger.debug("Calling " + className + ".configure(Property)");
                try {
                    Class localConfig = Class.forName(className);
                    Method method = localConfig.getMethod("configure",
                        new Class[] { Property.class });
                    method.invoke(null, new Object[] { prop });
                }
                catch(Throwable t) {
                    logger.error("Cannot call " + className +
                                 ".configure(Property)", t);
                    throw new InitializationException(t.getMessage());
                }
            }
        }
        catch (InitializationException e) {
            throw new MessageServiceHandlerException(e.getMessage());
        }

        try {
            messageServer = MessageServer.getInstance();
        }
        catch (MessageServerException e) {
            throw new MessageServiceHandlerException(e.getMessage());
        }

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            loadConnectionTable(mshConnectionTable, tx);
            sendThreadMap = messageServer.
                restartSendThread(this, mshConnectionTable, tx);
            deliveryMap = messageServer.getDeliveryMap(tx);
            sentSequenceMap = messageServer.getSentSequenceMap(tx);
            tx.commit();
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            throw e;
        }
        catch (Throwable e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            throw new MessageServiceHandlerException(err);
        }
        monitor = new Monitor();
        suppressedAck = false;
        ignoreAck = false;

        mailMonitor = new MailMonitor(this);
        mailMonitor.start();

        logger.debug("<= MessageServiceHandler.MessageServiceHandler");
    }

    /**
     * Initializes message handler servlet.
     *
     * @param servletConfig     Servlet configuration passed by the servlet
     *                          engine.
     *
     * @throws ServletException
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        logger.debug("=> MessageServiceHandler.init");

        super.init(servletConfig);

        if (messageFactory == null) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_MESSAGE_FACTORY);
            throw new ServletException(err);
        }
        final ServletContext context = servletConfig.getServletContext();
        final Object server = context.getAttribute(getMetaData().getName());
        if (server == null || !(server instanceof MessageServer)
            || messageServer != server) {
            context.setAttribute(getMetaData().getName(), messageServer);
        }

        logger.debug("<= MessageServiceHandler.init");
    }

    /**
     * Final cleanup of used resources and shutdown mail monitor and message
     * server.
     */
    public void destroy() {

        logger.debug("=> MessageServiceHandler.destroy");

        super.destroy();
        mailMonitor.shutDown();
        try {
            messageServer.shutDown();
        }
        catch (MessageServerException mse) {}

        logger.debug("<= MessageServiceHandler.destroy");
    }

    public void halt(int level) {
        logger.debug("=> MessageServiceHandler.halt");

        monitor.halt();
        synchronized (sendThreadMap) {
            for (Iterator i=sendThreadMap.values().iterator(); i.hasNext(); ) {
                ((MessageProcessor) i.next()).shutDown(level);
            }
            sendThreadMap.clear();
        }

        mailMonitor.haltPolling();

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            messageServer.logMSHLifeCycle("halt", tx);
            tx.commit();
        }
        catch (MessageServerException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
        }
        catch (Throwable e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
        }
        logger.debug("<= MessageServiceHandler.halt");
    }

    public boolean resume() {
        logger.debug("=> MessageServiceHandler.resume");

        synchronized (sendThreadMap) {
            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            Map resultMap = null;
            try {
                resultMap = messageServer.
                    restartSendThread(this, mshConnectionTable, tx);
                tx.commit();
            }
            catch (MessageServerException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, 
                    "cannot restart send threads during resume");
                logger.error(err);
                try {
                    tx.rollback();
                }
                catch (Throwable e2) {}
                logger.debug("<= MessageServiceHandler.resume");
                return false;
            }
            catch (Throwable e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                try {
                    tx.rollback();
                }
                catch (Throwable e2) {}
                logger.debug("<= MessageServiceHandler.resume");
                return false;
            }

            for (Iterator i = resultMap.keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = resultMap.get(key);
                sendThreadMap.put(key, value);
            }
        }

        mailMonitor.resumePolling();

        monitor.endRequest();
        monitor.resume();

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            messageServer.logMSHLifeCycle("resume", tx);
            tx.commit();
        }
        catch (MessageServerException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
        }
        catch (Throwable e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
        }
        logger.debug("<= MessageServiceHandler.resume");
        return true;
    }

    public Document backup() throws MessageServiceHandlerException {
        logger.debug("=> MessageServiceHandler.backup");

        try {
            messageServer.backup();
        }
        catch (MessageServerException e) {
            logger.debug("<= MessageServiceHandler.backup");
            return Utility.getFailureResponse(e.getMessage());
        }

        logger.debug("<= MessageServiceHandler.backup");
        return Utility.getSuccessResponse();
    }

    public void restore() throws MessageServiceHandlerException {
        logger.debug("<= MessageServiceHandler.restore");

        messageServer.restore();
        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            loadConnectionTable(mshConnectionTable, tx);
            sendThreadMap.putAll(messageServer.restartSendThread
                                 (this, mshConnectionTable, tx));
            deliveryMap.putAll(messageServer.getDeliveryMap(tx));
            sentSequenceMap.putAll(messageServer.getSentSequenceMap(tx));
            tx.commit();
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (TransactionException e2) {}
            throw e;
        }
        catch (Throwable t) {
            String errorMessage = ErrorMessages.getMessage
                (ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, t);
            logger.error(errorMessage);
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            throw new MessageServiceHandlerException(errorMessage);
        }

        logger.debug("<= MessageServiceHandler.restore");
    }

    /**
     * Handles HTTP get requests. Returns a simple message stating the servlet
     * is alive.
     *
     * @param request       HTTP Servlet request object from which the content
     *                      of the request is obtained.
     * @param response      HTTP servlet response object to which the response
     *                      of this servlet is written.
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter pw = response.getWriter();
        response.setContentType("text/html");
        pw.print("<html><head/><body>Hermes is alive. However, Hermes does not "
            + "respond to HTTP GET method.</body></html>");
    }

    /**
     * Handles HTTP Post requests. Requests including:
     * <ul>
     * <li>Message Service Handler commands.</li>
     * <li>ebXML messages.</li>
     * </ul>
     * It also verifies digital signature of the message and calls
     * {@link #onMessage} for proper handling.
     *
     * @param request       HTTP Servlet request object from which the content
     *                      of the request is obtained.
     * @param response      HTTP servlet response object to which the response
     *                      of this servlet is written.
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
        throws ServletException, IOException {

        boolean isHalted = !monitor.startRequest();
        final HashMap requestProperty;
        try {
            requestProperty = new HashMap();
            requestProperty.put(DbTableManager.ATTRIBUTE_REMOTE_ADDRESS,
                                request.getRemoteAddr());
            requestProperty.put(DbTableManager.ATTRIBUTE_REMOTE_HOST,
                                request.getRemoteHost());
        }
        catch (Throwable e) {
            monitor.endRequest();
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_HTTP_POST_FAILED, e);
            logger.error(err);
            response.sendError(HttpServletResponse.SC_CONFLICT, err);
            return;
        }

        Transaction tx = null;
        String status = null;
        try {
            final MimeHeaders headers = getHeaders(request);
            String contentType = getContentType(headers);
            if (contentType.equals(Constants.SERIALIZABLE_OBJECT)) {

                final ObjectInputStream in =
                    new ObjectInputStream(request.getInputStream());
                final Command command = (Command) in.readObject();

                // Authenticate and process the command object
                authenticateCommand(command);

                logger.info("Process command: " +
                    CommandConstants.getCommandString(command.getType()) +
                    " (" + command.getType() + ")");
                processCommand(response, command, isHalted);
            }
            else if (isHalted) {
                // Reject incoming message if the MSH is halted
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "MSH maintenance in progress. Request not accepted.");
            }
            else {
                // Retrieve data from HTTP servlet input stream
                tx = new Transaction(MessageServer.dbConnectionPool);
                String fileName = receiveData(request, headers, tx);

                // Process the incoming message
                logger.info("Process incoming message");
                status = processMessage(response, headers, fileName,
                                        requestProperty, tx);
                tx.commit();
                monitor.endRequest();
            }
        }
        catch (Throwable e) {
            if (tx != null) {
                try {
                    tx.rollback();
                }
                catch (Throwable e2) {}
            }

            if (!(e instanceof MessageServiceHandlerException)) {
                // not yet logged
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                if (status != null) {
                    logger.warn(status);
                }
                else {
                    status = err;
                }
                logger.warn(err);
            }

            try {
                response.sendError(HttpServletResponse.SC_CONFLICT,
                    ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_HTTP_POST_FAILED, status));
            }
            catch (Throwable t2) {}

            monitor.endRequest();
        }
    }

    /**
     * Handles incoming message by carrying out proper actions as specified in
     * ebMSS. They include:
     * <ul>
     * <li>Status request and responses.</li>
     * <li>Acknowledge request and responses.</li>
     * <li>Ping and Pong message.
     * <li>TimeToLive checking.</li>
     * <li>Duplicate elimination.</li>
     * </ul>
     * This method delivers the incoming message to a registered client
     * application if appropriate.
     *
     * @param message       Incoming SOAP message.
     *
     * @return SOAP response of the incoming message. It is null if not needed.
     */
    public SOAPMessage onMessage(SOAPMessage message) {
        try {
            final EbxmlMessage ebxmlMessage = new EbxmlMessage(message);
            final HashMap requestProperty = new HashMap();
            requestProperty.put
                (DbTableManager.ATTRIBUTE_REMOTE_ADDRESS, "Unknown");
            requestProperty.put
                (DbTableManager.ATTRIBUTE_REMOTE_HOST, "Unknown");
            final EbxmlMessage responseMessage =
                onMessage(ebxmlMessage, requestProperty);
            return (responseMessage == null ? null :
                    responseMessage.getSOAPMessage());
        }
        catch (MessageServiceHandlerException e) {
            // error already logged
            throw new RuntimeException(e.getMessage());
        }
        catch (Throwable e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.warn(err);
            throw new RuntimeException(err);
        }
    }

    /**
     * Handles incoming message by carrying out proper actions as specified in
     * ebMSS. They include:
     * <ul>
     * <li>Status request and responses.</li>
     * <li>Acknowledge request and responses.</li>
     * <li>Ping and Pong message.
     * <li>TimeToLive checking.</li>
     * <li>Duplicate elimination.</li>
     * </ul>
     * This method delivers the incoming message to a registered client
     * application if appropriate.
     *
     * @param message       Incoming ebXML message.
     * @param requestProperty Properties accompanied with the message
     *
     * @return SOAP response of the incoming message. It is null if not needed.
     */
    EbxmlMessage onMessage(EbxmlMessage ebxmlMessage, Map requestProperty)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.onMessage");

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        EbxmlMessage response = null;
        try {
            response = dispatchMessage(ebxmlMessage, requestProperty, tx);
            tx.commit();
        }
        catch (Throwable e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            try {
                logger.debug("retry message dispatching");
                response = dispatchMessage(ebxmlMessage, requestProperty, tx);
                tx.commit();
                logger.debug("retry message dispatching succeeded");
            }
            catch (MessageServiceHandlerException e2) {
                logger.debug("retry message dispatching failed");
                try {
                    tx.rollback();
                }
                catch (Throwable e3) {}
                throw e2;
            }
            catch (Throwable e2) {
                logger.debug("retry message dispatching failed");
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e2);
                logger.error(err);
                try {
                    tx.rollback();
                }
                catch (Throwable e3) {}
                logger.debug("<= MessageServiceHandler.onMessage");
                throw new MessageServiceHandlerException(err);
            }
        }

        logger.debug("<= MessageServiceHandler.onMessage");
        return response;
    }

    /**
     * Send message with automatic registration. It will register a
     * Application context which will not crash with others and then
     * send the message. If send complete (either success or failure),
     * it will try to unregister.
     * @param parameter the send command parameter
     * @param message the message
     * @throws MessageServiceHandlerException throw if exception occur.
     */
    void sendMessageWithAutomaticRegistration(SendCommandParameter parameter,
        EbxmlMessage message) throws MessageServiceHandlerException {
        sendMessageWithAutomaticRegistration(message, parameter.getToMshUrl(),
            parameter.getRetries(), parameter.getRetryInterval());
    }

    /**
     * Send message with automatic registration. It will register a
     * Application context which will not crash with others and then
     * send the message. If send complete (either success or failure),
     * it will try to unregister.
     * @param message the message
     * @param toMshUrl the to msh url
     * @param retries the number of retris
     * @param retryInterval the retry interval
     * @throws MessageServiceHandlerException throw if exception occur.
     */
    void sendMessageWithAutomaticRegistration(EbxmlMessage message,
        URL toMshUrl, int retries, String retryInterval) 
        throws MessageServiceHandlerException {
        MessageServiceHandlerConnection mshConnection = automaticSendRegister(
            toMshUrl, retries, retryInterval);
        MessageServiceHandlerConfig config
            = mshConnection.getMessageServiceHandlerConfig();
        MessageServiceHandlerConfig unregisterConfig
            = new MessageServiceHandlerConfig(config.getApplicationContext(),
                config.getToMshUrl(), config.getMessageListener(),
                config.getTransportType(), config.getRetries(),
                config.getRetryInterval(), config.getSyncReply(),
                config.isMessageOrdered(), config.getPersistDuration(),
                config.getAckRequested(), false);
        mshConnection.setUnregisterMessageServiceHandlerConfig(
            unregisterConfig);
        sendMessage(config.getApplicationContext(), message);
    }

    /**
     * automatic register on the Message Service Handler,
     * which is used for sending.
     * @param toMshUrl the to msh url
     * @param retries the number of retris
     * @param retryInterval the retry interval
     * @return the connection registered.
     * @throws MessageServiceHandlerException throw if exception occur.
     */
    private synchronized MessageServiceHandlerConnection automaticSendRegister(
        URL toMshUrl, int retries, String retryInterval)
        throws MessageServiceHandlerException {
        try {
            ApplicationContext appContext = generateSendApplicationContext();
            MessageListener listener = new ClientMessageListenerImpl(
                new File(".").toURL());
            String transportType = Constants.TRANSPORT_TYPE_HTTP;
            MessageServiceHandlerConfig mshConfig
                = new MessageServiceHandlerConfig(appContext, toMshUrl,
                listener, transportType, retries, retryInterval);
            return register(mshConfig);
        } catch (MalformedURLException e) {
            String err = "Error occur on automaticSendRegister : "
                + e.toString();
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * generatea send application context for send with automatic registration
     * purpose.
     * @return the application context generated.
     */
    private ApplicationContext generateSendApplicationContext() {
        boolean crashWithCurrentAppContext = true;
        String cpaId ="cecid:send_generate_cpaid";
        Random random = new Random(System.currentTimeMillis());
        ApplicationContext madeAppContext = null;
        while (crashWithCurrentAppContext) {
            String conversationId = Double.toString(random.nextDouble());
            String service = Double.toString(random.nextDouble());
            String action = Double.toString(random.nextDouble());
            madeAppContext = new ApplicationContext(cpaId, conversationId,
                service, action);
            if (mshConnectionTable.get(madeAppContext) == null) {
                crashWithCurrentAppContext = false;
            }
        }
        return madeAppContext;
    }

    /**
     * Register a client application to the message service handler.
     *
     * @param mshConfig     Message service handler configuration parameters.
     *
     * @return Connection to the message service handler.
     *
     * @throws MessageServiceHandlerException
     */
    public MessageServiceHandlerConnection
        register(MessageServiceHandlerConfig mshConfig)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.register");

        if (!mshConfig.isEnabled()) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_REGISTRATION_FAILED,
                "inconsistent: try to register a disabled config");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }

        MessageListener listener = mshConfig.getMessageListener();
        URL clientUrl = listener.getClientUrl();
        final MessageServiceHandlerConfig config;
        // Not client message listener and it's not using trusted repository
        if (clientUrl.getProtocol().equals(MessageListener.PROTOCOL_FILE)) {
            File clientUrlFile = new File(clientUrl.getPath());
            boolean isServerRepository = false;
            try {
                isServerRepository = trustedListenerRepository.contains(
                    clientUrlFile.getCanonicalPath());                
            }
            catch (IOException e) {}
            boolean isClientRepository =
                listener instanceof ClientMessageListenerImpl;

            if (!isServerRepository && !isClientRepository) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_REGISTRATION_FAILED,
                    "client URL is file protocol, but message listener is "
                    + "not implementing ClientMessageListener nor not "
                    + "pointing to a trusted repository");
                logger.warn(err);
                throw new MessageServiceHandlerException(err);
            }
            else if (isClientRepository) {
                final ApplicationContext appContext = mshConfig.
                    getApplicationContext();

                try {
                    /*
                    clientUrl = new URL(MessageListener.PROTOCOL_FILE +
                        MessageListener.PROTOCOL_SEPARATOR + messageRepository);
                        */
                    clientUrl = (new File(".")).toURL();
                }
                catch (MalformedURLException e) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_REGISTRATION_FAILED, e,
                        "malformed URL: .");
                        /* + MessageListener.PROTOCOL_FILE
                        + MessageListener.PROTOCOL_SEPARATOR
                        + messageRepository); */
                    logger.warn(err);
                    throw new MessageServiceHandlerException(err);
                }
                final MessageListener messageListener =
                    new ClientMessageListenerImpl(clientUrl);

                config = new MessageServiceHandlerConfig
                    (mshConfig.getApplicationContext(), mshConfig.getToMshUrl(),
                     messageListener, mshConfig.getTransportType(),
                     mshConfig.getRetries(), mshConfig.getRetryInterval(),
                     mshConfig.getSyncReply(), mshConfig.isMessageOrdered(),
                     mshConfig.getPersistDuration(), 
                     mshConfig.getAckRequested(), mshConfig.isEnabled());
            }
            else {
                config = mshConfig;
            }
        }
        else {
            config = mshConfig;
        }

        final MessageServiceHandlerConnectionFactory mshConnectionFactory =
            new MessageServiceHandlerConnectionFactory(this, config);

        final MessageServiceHandlerConnection mshConnection;
        try {
            mshConnection = (MessageServiceHandlerConnection)
                mshConnectionFactory.createConnection();
        }
        catch (JAXMException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "cannot create MSH connection");
            logger.error(err);
            throw new MessageServiceHandlerConnectionException(err);
        }

        final Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            messageServer.store(config, tx);
            mshConnectionTable.put(config.getApplicationContext(),
                                   mshConnection);
            tx.commit();
        }
        catch (Throwable e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            if (e instanceof MessageServiceHandlerException) {
                throw (MessageServiceHandlerException) e;
            }
            else {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new MessageServiceHandlerException(e.getMessage());
            }
        }

        logger.debug("<= MessageServiceHandler.register");
        return mshConnection;
    }
    
    /**
     * Unregister a client application to the message service handler.
     *
     * @param mshConfig     Message service handler configuration parameters.
     *
     * @return true if unregistration succeeded, false otherwise
     *
     * @throws MessageServiceHandlerException
     */
    public boolean unregister(MessageServiceHandlerConfig mshConfig)
        throws MessageServiceHandlerException {
        return unregister(mshConfig, false);
    }

    /**
     * Unregister a client application to the message service handler.
     *
     * @param mshConfig     Message service handler configuration parameters.
     * @param remove whether it should also remove the MSHconfig from database.
     *
     * @return true if unregistration succeeded, false otherwise
     *
     * @throws MessageServiceHandlerException
     */
    boolean unregister(MessageServiceHandlerConfig mshConfig, boolean remove)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.unregister");

        if (mshConfig.isEnabled()) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_REGISTRATION_FAILED,
                "inconsistent: try to unregister a enabled config");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }

        ApplicationContext ac = mshConfig.getApplicationContext();
        if (ac == null) {
            return true;
        }

        boolean result = true;
        final Transaction tx = new Transaction
                               (MessageServer.dbConnectionPool);
        try {
            if (!messageServer.getUndeliveredMessages(ac, tx).isEmpty()) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_REGISTRATION_FAILED,
                    "unregistration failed: pending messages to be delivered");
                logger.warn(err);
                result = false;
            }
            else {
                Iterator iter = sendThreadMap.values().iterator();
                while (iter.hasNext()) {
                    MessageProcessor mp = (MessageProcessor) iter.next();
                    if (mp.getApplicationContext().equals(ac)) {
                        result = false;
                        break;
                    }
                }
                if (result == false) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_REGISTRATION_FAILED,
                        "unregistration failed: pending messages to be sent");
                    logger.warn(err);
                }
            }

            if (result == true) {
                mshConnectionTable.remove(ac);
                if (remove) {
                    messageServer.remove(mshConfig, tx);
                } else {
                    messageServer.store(mshConfig, tx);
                }
            }
            tx.commit();
        }
        catch (Throwable e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            if (e instanceof MessageServiceHandlerException) {
                throw (MessageServiceHandlerException) e;
            }
            else {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new MessageServiceHandlerException(e.getMessage());
            }
        }

        logger.debug("<= MessageServiceHandler.unregister");
        return result;
    }


    /**
     * Get ebXML message service provider metadata.
     */
    public static MetaData getMetaData() {
        return metaData;
    }

    /**
     * Get message server object.
     *
     * @return Reference to the message server object.
     */
    MessageServer getMessageServer() {
        return messageServer;
    }

    HashSet getTrustedListenerRepository() {
        return trustedListenerRepository;
    }

    void sendMessage(ApplicationContext appContext, SOAPMessage soapMessage)
        throws MessageServiceHandlerException {
        try {
            sendMessage(appContext, new EbxmlMessage(soapMessage));
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_CREATE_OBJECT, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    void sendMessage(ApplicationContext appContext, EbxmlMessage ebxmlMessage)
        throws MessageServiceHandlerException {
        MessageServiceHandlerConnection mshConnection = null;
        try {
            logger.debug("=> MessageServiceHandler.sendMessage");
            mshConnection = (MessageServiceHandlerConnection)
                mshConnectionTable.get(appContext);
            if (mshConnection == null || !mshConnection.isEnabled()) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_APP_CONTEXT,
                    appContext.toString());
                logger.warn(err);
                throw new MessageServiceHandlerException(err);
            }
        }
        catch (Exception e) {
            /*
            if (ebxmlMessage.getFileName() != null) {
                try {
                    File file = new File(ebxmlMessage.getFileName());
                    if (file.delete() == false) {
                        file.deleteOnExit();
                    }
                }
                catch (Exception e2) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e2);
                    logger.error(err);
                }
            }*/
            if (ebxmlMessage.getPersistenceName() != null) {
                try {
                    ebxmlMessage.getPersistenceHandler().removeObject(
                            ebxmlMessage.getPersistenceName());
                } catch (Exception e2) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e2);
                    logger.error(err);
                }
            }
            if (e instanceof MessageServiceHandlerException) {
                throw (MessageServiceHandlerException) e;
            }
            else {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new MessageServiceHandlerException(err);
            }
        }

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        /*
        if (ebxmlMessage.getFileName() != null) {
            tx.storeFileName(ebxmlMessage.getFileName());
        }
        */
        if (ebxmlMessage.getPersistenceName() != null) {
            tx.storePersistenceObject(ebxmlMessage.getPersistenceName(),
                    ebxmlMessage.getPersistenceHandler());
        }
        try {
            mshConnection.send(ebxmlMessage, tx);
            tx.commit();
        }
        catch (Throwable e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            if (e instanceof MessageServiceHandlerException) {
                throw (MessageServiceHandlerException) e;
            }
            else {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new MessageServiceHandlerException(err);
            }
        }
        logger.debug("<= MessageServiceHandler.sendMessage");
    }

    EbxmlMessage getMessage(ApplicationContext appContext,
        HttpServletResponse response) throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.getMessage");

        final MessageServiceHandlerConnection mshConnection =
            (MessageServiceHandlerConnection) mshConnectionTable.
            get(appContext);

        if (mshConnection == null || !mshConnection.isEnabled()) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_APP_CONTEXT,
                appContext.toString());
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        final MessageListener messageListener = mshConnection.
            getMessageServiceHandlerConfig().getMessageListener();

        if (!(messageListener instanceof MessageListenerImpl)) {
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            logger.debug("<= MessageServiceHandler.getMessage");
            return null;
        }
        final URL clientUrl = messageListener.getClientUrl();
        if (clientUrl == null || clientUrl.getProtocol().equals
            (MessageListener.PROTOCOL_FILE) == false) {
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            logger.debug("<= MessageServiceHandler.getMessage");
            return null;
        }
        final File name = new File(clientUrl.getFile());
        if (name.exists() == false ||
            (!(name.isDirectory()) && !(name.isFile()))) {
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            logger.debug("<= MessageServiceHandler.getMessage");
            return null;
        }

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        EbxmlMessage ret = null;
        try {
            ret = getNextUndeliveredMessage(appContext, tx);
            tx.commit();
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
            // error already logged
            throw e;
        }
        catch (Exception e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        if (ret == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        else {
            try {
                String contentType;
                if (ret.getPayloadCount() > 0) {
                    contentType = Constants.MULTIPART_RELATED_TYPE
                        + "\"" + EbxmlMessage.mimeBoundary + "\"; "
                        + Constants.CHARACTER_SET + "=\""
                        + Constants.CHARACTER_ENCODING + "\"; "
                        + Constants.START + "=\"<"
                        + EbxmlMessage.SOAP_PART_CONTENT_ID + ">\"";
                }
                else {
                    contentType = ret.getSOAPMessage().
                        getMimeHeaders().getHeader(Constants.CONTENT_TYPE)[0];
                }
                response.setContentType(contentType);
                response.setStatus(HttpServletResponse.SC_OK);

                final ServletOutputStream os = response.getOutputStream();
                ret.writeTo(os);
                os.flush();
            }
            catch (IOException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_SERVLET_IO_ERROR, e,
                    "cannot write message to servlet output stream");
                logger.error(err);
                throw new MessageServiceHandlerException(err);
            }
            catch (SOAPException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_SOAP_GENERAL_ERROR, e,
                    "cannot write message to servlet output stream");
                logger.error(err);
                throw new MessageServiceHandlerException(err);
            }
        }

        logger.debug("<= MessageServiceHandler.getMessage");
        return ret;
    }

    void getMessageById(String messageId, HttpServletResponse response)
        throws MessageServiceHandlerException {
        logger.debug("=> MessageServiceHandler.getMessageById");
        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        String fileName = null;
        try {
            tx.lock(messageId);
            fileName = messageServer.getMessageById(messageId, tx);
            if (fileName == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                tx.commit();
                logger.debug("<= MessageServiceHandler.getMessageById");
                return;
            }
            /*
            EbxmlMessage ebxmlMessage = MessageServer.getMessageFromFile
                (new File(messageRepository, fileName));
            */
            PersistenceHandler handler
                    = PersistenceManager.getRepositoryPersistenceHandler();
            EbxmlMessage ebxmlMessage = (EbxmlMessage)
                    MessageServer.getMessageFromDataSource(
                            handler.getObject(fileName), true);
            boolean shouldBeDelivered = true;
            if (ebxmlMessage.getMessageOrder() != null) {
                String conversationId = ebxmlMessage.getConversationId();
                int seqNo = ebxmlMessage.getMessageOrder().getSequenceNumber();
                DeliveryRecord record = null;
                synchronized (deliveryMap) {
                    record = (DeliveryRecord)
                        deliveryMap.get(conversationId);
                }
                if (record.isNext(seqNo)) {
                    record.incLastDelivered();
                    tx.addDeliveryRecord(record, -1);
                }
                else {
                    shouldBeDelivered = false;
                }
            }

            if (shouldBeDelivered) {
                messageServer.setFileDeliveryStatus(fileName, true, tx);
//                String contentType = ebxmlMessage.getSOAPMessage().
//                    getMimeHeaders().getHeader(Constants.CONTENT_TYPE)[0];
                String contentType = (String) ebxmlMessage.getMimeHeaders(
                        Constants.DEFAULT_CONTENT_TRANSFER_ENCODING, 
                        Constants.DEFAULT_CONTENT_TRANSFER_ENCODING)
                    .get(Constants.CONTENT_TYPE);
                response.setContentType(contentType);
                response.setStatus(HttpServletResponse.SC_OK);
                ServletOutputStream os = response.getOutputStream();
                ebxmlMessage.writeTo(os);
                os.flush();
            }
            else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            tx.commit();
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
            // error already logged
            throw e;
        }
        catch (Exception e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        logger.debug("<= MessageServiceHandler.getMessageById");
    }

    void getUndeliveredMessageIds(ApplicationContext appContext,
        HttpServletResponse response) throws MessageServiceHandlerException {
        logger.debug("=> MessageServiceHandler.getUndeliveredMessageIds");
        ArrayList results = getUndeliveredMessages(appContext, true);

        try {
            if (results.size() == 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            else {
                response.setContentType(Constants.SERIALIZABLE_OBJECT);
                response.setStatus(HttpServletResponse.SC_OK);
                ObjectOutputStream os =
                    new ObjectOutputStream(response.getOutputStream());
                os.writeObject(results);
                os.flush();
            }
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageServiceHandler.getUndeliveredMessageIds");
    }

    private ArrayList getUndeliveredMessages(ApplicationContext appContext,
        boolean getMessageId)  throws MessageServiceHandlerException {

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        Map map = null;
        try {
            map = messageServer.getUndeliveredMessages(appContext, tx);
            tx.commit();
        }
        catch (MessageServerException e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
        }
        catch (Exception e) {
            try {
                tx.rollback();
            }
            catch (Exception e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        try {
            HashMap messageOrders = new HashMap();
            ArrayList results = new ArrayList();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String messageId = (String) entry.getKey();
                ArrayList list = (ArrayList) entry.getValue();
                String conversationId = (String) list.get(0);
                Integer seqNoObj = (Integer) list.get(1);
                int seqNo = seqNoObj.intValue();
                String fileName = (String) list.get(2);
                if (seqNo >= MessageServer.FIRST_MESSAGE_UNDELIVERED &&
                    seqNo < MessageServer.FIRST_MESSAGE_ORDER_UNDELIVERED) {
                    if (getMessageId) {
                        results.add(messageId);
                    }
                    else {
                        results.add(fileName);
                    }
                }
                else {
                    ArrayList seqNoList = (ArrayList) messageOrders.
                        get(conversationId);
                    if (seqNoList == null) {
                        seqNoList = new ArrayList();
                        messageOrders.put(conversationId, seqNoList);
                    }

                    int i = 0;
                    for ( ; i < seqNoList.size() ; i++) {
                        int value = ((Integer) ((ArrayList) seqNoList.get(i)).
                                     get(1)).intValue();
                        if (seqNo <= value) {
                            ArrayList messageInfo = new ArrayList(3);
                            messageInfo.add(messageId);
                            messageInfo.add(seqNoObj);
                            messageInfo.add(fileName);
                            seqNoList.add(i, messageInfo);
                            break;
                        }
                    }

                    if (i == seqNoList.size()) {
                        ArrayList messageInfo = new ArrayList(3);
                        messageInfo.add(messageId);
                        messageInfo.add(seqNoObj);
                        messageInfo.add(fileName);
                        seqNoList.add(messageInfo);
                    }
                }
            }

            iterator = messageOrders.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String conversationId = (String) entry.getKey();
                ArrayList seqNoList = (ArrayList) entry.getValue();
                ArrayList list = (ArrayList) seqNoList.get(0);
                String messageId = (String) list.get(0);
                int seqNo = ((Integer) list.get(1)).intValue();
                String fileName = (String) list.get(2);

                DeliveryRecord record = null;
                synchronized (deliveryMap) {
                    record = (DeliveryRecord) deliveryMap.get(conversationId);
                }
                if (record.isNext(seqNo)) {
                    if (getMessageId) {
                        results.add(messageId);
                    }
                    else {
                        results.add(fileName);
                    }
                    for (int i=1 ; i<seqNoList.size() ; i++) {
                        list = (ArrayList) seqNoList.get(i);
                        messageId = (String) list.get(0);
                        if (((Integer) list.get(1)).intValue() == (seqNo+1)) {
                            if (getMessageId) {
                                results.add(messageId);
                            }
                            else {
                                results.add(fileName);
                            }
                            seqNo++;
                        }
                        else {
                            break;
                        }
                    }
                }
            }

            return results;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    void addSendThread(String messageId, MessageProcessor messageProcessor) {
        synchronized(sendThreadMap) {
            sendThreadMap.put(messageId, messageProcessor);
        }
    }

    void removeSendThread(String messageId) {
        synchronized(sendThreadMap) {
            final MessageProcessor sendThread = (MessageProcessor)
                sendThreadMap.get(messageId);
            if (sendThread != null) {
                if (sendThread.isAlive()) {
                    sendThread.wakeUp(true);
                }
                sendThreadMap.remove(messageId);
            }
        }
    }

    private void loadConnectionTable(Hashtable mshConnectionTable,
                                     Transaction tx)
        throws MessageServiceHandlerException {
        for (Iterator i=messageServer.getMessageServiceHandlerConfig(tx) ;
             i.hasNext() ; ) {
            MessageServiceHandlerConfig mshConfig =
                (MessageServiceHandlerConfig) i.next();
            if (!mshConfig.isEnabled()) {
                continue;
            }
            MessageServiceHandlerConnectionFactory mshConnectionFactory =
                new MessageServiceHandlerConnectionFactory(this, mshConfig);

            MessageServiceHandlerConnection mshConnection = null;
            try {
                mshConnection = (MessageServiceHandlerConnection)
                    mshConnectionFactory.createConnection();
            }
            catch (JAXMException e) {
                String errMessage = ErrorMessages.getMessage
                    (ErrorMessages.ERR_SOAP_INIT_CONNECTION, e);
                logger.error(errMessage);
                throw new MessageServiceHandlerConnectionException(errMessage);
            }
            mshConnectionTable.put(mshConfig.getApplicationContext(),
                                   mshConnection);
        }
    }

    private ApplicationContext getApplicationContext(String cpaId,
        String conversationId, String service, String action) {
        ApplicationContext appContext = null;
        if (mshConnectionTable.containsKey
            ((appContext = new ApplicationContext
              (cpaId, conversationId, service, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, conversationId, service, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, conversationId, WILD_CARD, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, conversationId, WILD_CARD, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, WILD_CARD, service, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, WILD_CARD, service, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, WILD_CARD, WILD_CARD, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (cpaId, WILD_CARD, WILD_CARD, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, conversationId, service, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, conversationId, service, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, conversationId, WILD_CARD, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, conversationId, WILD_CARD, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, WILD_CARD, service, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, WILD_CARD, service, WILD_CARD)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, WILD_CARD, WILD_CARD, action)))) {
            return appContext;
        }
        else if (mshConnectionTable.containsKey
                 ((appContext = new ApplicationContext
                   (WILD_CARD, WILD_CARD, WILD_CARD, WILD_CARD)))) {
            return appContext;
        }

        return null;
    }

    private boolean hasSignature(EbxmlMessage ebxmlMessage)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.hasSignature");

        try {
            final SOAPEnvelope soapEnvelope = ebxmlMessage.getSOAPMessage().
                getSOAPPart().getEnvelope();
            final Name signatureName = soapEnvelope.createName
                (Signature.ELEMENT_SIGNATURE, Signature.NAMESPACE_PREFIX_DS,
                 Signature.NAMESPACE_URI_DS);

            boolean ret = soapEnvelope.getHeader()
                .getChildElements(signatureName).hasNext();
            logger.debug("<= MessageServiceHandler.hasSignature");
            return ret;
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Verify digital signature attached in the given ebXML message using local
     * trusted keystore.
     *
     * @param ebxmlMessage      ebXML message which contains digital signature.
     *
     * @return true if the digital signature is verified successfully; false
     *         otherwise.
     *
     * @throws MessageServiceHandlerException
     */
    private boolean verify(EbxmlMessage ebxmlMessage)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.verify");

        try {
            logger.debug("verify the XML signature");

            char[] password = trustedStorePassword.toCharArray();
            if (trustedStorePassword.equals("")) {
                password = null;
            }
            String trustedStore = trustedStorePath + File.separator
                + trustedStoreFile;

            if (!ebxmlMessage.verify(password, trustedStore, certResolver)) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_PKI_VERIFY_SIGNATURE_FAILED,
                    "SOAPMessage cannot be verified");
                logger.warn("Received SOAPMessage signature "
                    + "verification failed!");
                logger.debug("<= MessageServiceHandler.verify");
                return false;
            }
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (SignatureException e) {
            throw new MessageServiceHandlerException(e.getMessage());
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        logger.debug("received SOAPMessage verified");
        logger.debug("<= MessageServiceHandler.verify");
        return true;
    }


    /**
     * Get the next undelivered message having the specified application
     * context in the delivery record, or a random one if the message is
     * unordered.
     *
     * @param appContext <code>ApplicationContext</code> of the message.
     * @return A previously undelivered <code>EbxmlMessage</code> object; it is
     * marked as "delivered" before the return of this function.
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage getNextUndeliveredMessage
        (ApplicationContext appContext, Transaction tx)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.getNextUndeliveredMessage");

        try {
            tx.lock(appContext);

            String messageId = null;
            //File messageFile = null;
            DataSource messageDataSource = null;
            final Map map = messageServer.getUndeliveredMessages
                            (appContext, tx);
            final Set keys = map.keySet();
            Iterator iter = keys.iterator();
            PersistenceHandler handler
                    = PersistenceManager.getRepositoryPersistenceHandler();

            while (iter.hasNext()) {
                try {
                    messageId = (String) iter.next();
                    ArrayList list = (ArrayList) map.get(messageId);
                    String conversationId = (String) list.get(0);
                    int seqNo = ((Integer) list.get(1)).intValue();
                    String fileName = (String) list.get(2);

                    if (seqNo >= MessageServer.FIRST_MESSAGE_UNDELIVERED &&
                        seqNo < MessageServer.FIRST_MESSAGE_ORDER_UNDELIVERED) {
                        //messageFile = new File(messageRepository, fileName);
                        messageDataSource = handler.getObject(fileName); 
                        seqNo = messageServer.setFileDeliveryStatus
                            (fileName, true, tx);
                        break;
                    }
                    else {
                        DeliveryRecord record;
                        synchronized (deliveryMap) {
                            record = (DeliveryRecord)
                                deliveryMap.get(conversationId);
                        }
                        if (record.isNext(seqNo)) {
                            //messageFile = new File(messageRepository, fileName);
                            messageDataSource = handler.getObject(fileName);
                            messageServer.setFileDeliveryStatus
                                (fileName, true, tx);
                            record.incLastDelivered();
                            tx.addDeliveryRecord(record, -1);
                            break;
                        }
                        else {
                            record.addUndelivered(seqNo);
                            tx.addDeliveryRecord(record, seqNo);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e,
                        "number format exception");
                    logger.warn(err);
                }
            }
            /*
            if (messageFile == null) {
                logger.debug("<= MessageServiceHandler."
                    + "getNextUndeliveredMessage with no message");
                return null;
            }*/
            if (messageDataSource == null ) {
                logger.debug("<= MessageServiceHandler."
                    + "getNextUndeliveredMessage with no message");
                return null;
            }

            // Message found. Internalize the message and return
            //EbxmlMessage msg = MessageServer.getMessageFromFile(messageFile);
            EbxmlMessage msg = (EbxmlMessage)
                    MessageServer.getMessageFromDataSource(messageDataSource,
                            true);
            if (msg == null) {
                logger.debug("<= MessageServiceHandler."
                    + "getNextUndeliveredMessage with no message");
            }
            else {
                logger.debug("<= MessageServiceHandler."
                    + "getNextUndeliveredMessage with messageId: " + messageId);
            }
            return msg;
        }
        catch (MessageServiceHandlerException e) {
            throw e;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Generates acknowledgement message from the given acknowledgement
     * request message and the refToMessageId.
     *
     * @param ackRequestedMessage   Acknowledgement request message.
     * @param refToMessageId        MessageId of the message to which the
     *                              acknowledgement response should be referred.
     *
     * @return Acknowledgement message.
     *
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage generateAcknowledgment(EbxmlMessage
        ackRequestedMessage, String refToMessageId)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.generateAcknowledgment");

        try {
            EbxmlMessage ackMessage
                    = SignalMessageGenerator.generateAcknowledgment(
                            ackRequestedMessage, refToMessageId);
            if (ackRequestedMessage.getAckRequested().getSigned()) {
                logger.debug("sign the Ack");

                String keystore = keystorePath + File.separator + keystoreFile;
                if (keyAlg == null) {
                    ackMessage.sign(keystoreAlias, 
                                    keystorePassword.toCharArray(),
                                    keystore);
                }
                else {
                    ackMessage.sign(keystoreAlias, 
                                    keystorePassword.toCharArray(),
                                    keystore, keyAlg);
                }
            }

            logger.debug("<= MessageServiceHandler.generateAcknowledgment");
            return ackMessage;
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (SignatureException e) {
            throw new MessageServiceHandlerException(e.getMessage());
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Generates response message from the given status request message and
     * the status string [ebMSS 7.1.2].
     *
     * @param statusRequestMessage  Status request message.
     * @param status                Current status of the message service
     *                              handler.
     *
     * @return Status response message.
     *
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage generateStatusResponseMessage
        (EbxmlMessage statusRequestMessage, String status, String timestamp)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.generateStatusResponseMessage");
        EbxmlMessage statusResponseMessage = null;
        try {
            statusResponseMessage
                    = SignalMessageGenerator.generateStatusResponseMessage(
                            statusRequestMessage, status, timestamp);
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (NumberFormatException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_DATA_ERROR, e,
                "cannot parse timestamp of original message");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageServiceHandler.generateStatusResponseMessage");
        return statusResponseMessage;
    }

    /**
     * Generates an error message containing the specfied error code
     * [ebMSS 4.2.3.4.1].
     *
     * @param ebxmlMessage  ebXML message to which error list should be
     *                      attached.
     * @param errorCode     Error code of the message.
     * @param severity      Error severity, either ERROR or WARNING.
     * @param description   Human-readable description of the error message.
     *
     * @return ebXML message containing error code.
     *
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage generateErrorMessage(EbxmlMessage ebxmlMessage,
        String errorCode, String severity, String description)
        throws MessageServiceHandlerException {
        return generateErrorMessage(ebxmlMessage, errorCode, severity,
            description, null);
    }

    /**
     * Generates an error message containing the specfied error code
     * [ebMSS 4.2.3.4.1].
     *
     * @param ebxmlMessage  ebXML message to which error list should be
     *                      attached.
     * @param errorCode     Error code of the message.
     * @param severity      Error severity, either ERROR or WARNING.
     * @param description   Human-readable description of the error message.
     * @param location      Source of the error.
     *
     * @return ebXML message containing error code.
     *
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage generateErrorMessage(EbxmlMessage ebxmlMessage,
        String errorCode, String severity, String description, String location)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.generateErrorMessage");
        EbxmlMessage errorMessage = null;
        try {
            errorMessage = SignalMessageGenerator.generateErrorMessage(
                    ebxmlMessage, errorCode, severity, description, location);
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageServiceHandler.generateErrorMessage");
        return errorMessage;
    }

    /**
     * Generates pong message from the given ping message [ebMSS 8.2].
     *
     * @param pingMessage   Incoming ping message.
     *
     * @return Pong message in response of the incoming ping message.
     *
     * @throws MessageServiceHandlerException
     */
    private EbxmlMessage generatePongMessage(EbxmlMessage pingMessage)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.generatePongMessage");

        EbxmlMessage pongMessage = null;
        try {
            pongMessage = SignalMessageGenerator.generatePongMessage(
                    pingMessage);
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_GENERAL_ERROR, e);
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageServiceHandler.generatePongMessage");
        return pongMessage;
    }

    /**
     * Deliver an ebXML message to a registered client application.
     *
     * @param appContext    Application context of the registered application.
     * @param messageListener   Listener of a particular application context
     * @param ebxmlMessage  Message to be delivered to client.
     * @param shouldStore   True if the message should be stored to persistence
     *                      store; false otherwise.
     *
     * @throws MessageServiceHandlerException
     * @throws MessageServerException
     */
    /*
    void deliverToApplication(ApplicationContext appContext, MessageListener
        messageListener, EbxmlMessage ebxmlMessage, Transaction tx)
        throws MessageServiceHandlerException {

        logger.debug("=> deliverToApplication");

        // Handles the message in a protocol-specific manner
        final URL clientUrl = messageListener.getClientUrl();

        if (clientUrl != null) {
            final String protocol = clientUrl.getProtocol();
            final boolean isFileProtocol =
                protocol.equals(MessageListener.PROTOCOL_FILE);
            final boolean isClientPoller =
                (messageListener instanceof ClientMessageListenerImpl);
            final boolean isServerRepository =
                isFileProtocol && !isClientPoller;

            if (isFileProtocol && !isClientPoller) {
                while ((ebxmlMessage =
                        getNextUndeliveredMessage(appContext, tx))
                    != null) {
                    // Server side trusted repository
                    try {
                        final File name = new File(clientUrl.getFile());
                        if (!name.exists() && !name.mkdirs() && !name.exists()){
                            throw new MessageServiceHandlerException
                                ("Cannot create directory for non-existent "
                                 + "server-side trusted repository: " + name);
                        }

                        final FileOutputStream file;
                        if (name.isDirectory()) {
                            String tempFileName = null;
                            if (ebxmlMessage.getFileName() != null) {
                                tempFileName = new File(ebxmlMessage.
                                    getFileName()).getName();
                            }
                            if (tempFileName != null) {
                                final File tmpFile =
                                    new File(name, tempFileName);
                                file = new FileOutputStream(tmpFile);
                            }
                            else {
                                final File tmpFile = File.createTempFile
                                    (MessageListenerImpl.TEMP_FILE_PREFIX,
                                     MessageListenerImpl.TEMP_FILE_SUFFIX,
                                     name);
                                file = new FileOutputStream(tmpFile);
                            }
                        }
                        else if (name.isFile()) {
                            file = new FileOutputStream(name);
                        }
                        else {
                            throw new MessageServiceHandlerException
                                ("Cannot deliver message: \"" + name
                                 + "\" is neither the name of a file "
                                 + "or a directory");
                        }
                        ebxmlMessage.writeTo(file);
                        file.flush();
                        file.close();
                    }
                    catch (Exception e) {
                        String msg = "Cannot deliver message to server-side "
                                     + "trusted repository: " + e.getMessage();
                        throw new MessageServiceHandlerException(msg);
                    }
                }
            }
            else if (isFileProtocol) {
                // Do nothing; wait for client polling
            }
            else if (protocol.equals(MessageListener.PROTOCOL_HTTP)) {
                while ((ebxmlMessage =
                        getNextUndeliveredMessage(appContext, tx))
                    != null) {
                    final MessageSender messageSender = new HttpSender
                        (ebxmlMessage, clientUrl);
                    messageSender.setDaemon(true);
                    messageSender.start();
                }
            }
            else if (protocol.equals(MessageListener.PROTOCOL_MAIL)) {
                while ((ebxmlMessage =
                        getNextUndeliveredMessage(appContext, tx))
                    != null) {
                    final MessageSender messageSender = new MailSender
                        (ebxmlMessage, clientUrl);
                    messageSender.setDaemon(true);
                    messageSender.start();
                }
            }
            else {
                logger.error("Unsupported transport type for " +
                    "MessageListener: " + protocol);
            }
        }

        logger.debug("<= deliverToApplication");
        return;
    }
    */
    /**
     * List all the system properties
     *
     * @return XML <code>Document</code> listing system properties.
     * @throws MessageServiceHandlerException
     */
    private Document getEnvironment() throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.getEnvironment");

        try {
            String key = null;
            String value = null;
            final Properties props = System.getProperties();
            final Enumeration en = props.propertyNames();
            final Document document = Utility.getSuccessResponse();
            final Element elem_env = document.createElement
                (Constants.ELEMENT_ENVIRONMENT);
            Element elem_key = null;
            Element elem_value = null;
            Element elem_property = null;

            while (en.hasMoreElements()) {
                key = (String) en.nextElement();
                value = props.getProperty(key);

                elem_property = document.createElement(
                    Constants.ELEMENT_ENVIRONMENT_PROPERTY);
                elem_key = document.createElement(
                    Constants.ELEMENT_ENVIRONMENT_KEY);
                elem_value = document.createElement(
                    Constants.ELEMENT_ENVIRONMENT_VALUE);

                elem_key.appendChild(document.createTextNode(key));
                elem_value.appendChild(document.createTextNode(value));
                elem_property.appendChild(elem_key);
                elem_property.appendChild(elem_value);
                elem_env.appendChild(elem_property);
            }

            document.getFirstChild().appendChild(elem_env);
            logger.debug("<= MessageServiceHandler.getEnvironment");
            return document;
        }
        catch (MessageServiceHandlerException e) {
            throw e;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Test the persistence layer of the Message Service Handler.
     *
     * @return null if the test is successful; otherwise a string stating the
     *         reason of failure is returned.
     * @throws MessageServiceHandlerException
     */
    private String checkPersistence() throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.checkPersistence");

        try {
            // Do checking on its permissions
            File dir = new File(messageRepository);
            String retMsg = null;
            if (dir.exists() == false) {
                retMsg = "Message repository does not exist.";
            }
            else if (dir.isDirectory() == false) {
                retMsg = "Message repository must be a directory.";
            }
            else if (dir.canRead() == false) {
                retMsg = "Read permission of the message repository " +
                    "must be set.";
            }
            else if (dir.canWrite() == false) {
                retMsg = "Write permission of the message repository " +
                    "must be set";
            }

            if (retMsg != null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, retMsg);
                logger.warn(err);
                logger.debug("<= MessageServiceHandler.checkPersistence");
                return retMsg;
            }

            // Get the list of files
            String [] filenames = null;
            ArrayList filelist = new ArrayList();
            Stack currentList = new Stack();
            currentList.push(dir);

            while (currentList.empty() == false) {
                File currentDir = (File)currentList.pop();
                String currentDirName = currentDir.getCanonicalPath();

                filenames = currentDir.list();
                for (int i = 0; i < filenames.length; i++) {
                    File tmpFile = new File(currentDirName, filenames[i]);
                    if (tmpFile.isDirectory()) {
                        currentList.push(tmpFile);
                    }
                    else {
                        filelist.add(tmpFile.getCanonicalPath());
                    }
                }
            }

            // Randomly read TEST_PERSISTENCE_FILE_NUMBER number of files
            int numFile;
            if (TEST_PERSISTENCE_FILE_NUMBER > filelist.size()) {
                numFile = filelist.size();
            }
            else {
                numFile = TEST_PERSISTENCE_FILE_NUMBER;
            }

            for (int i = 0; i < numFile; i++) {
                int j = (int)(Math.random() * filelist.size());
                File tmpFile = new File((String)filelist.remove(j));
                FileInputStream fis = new FileInputStream(tmpFile);

                int c;
                byte [] b = new byte[BLOCK_SIZE];
                while ( (c = fis.read(b)) != -1) {};
            }

            // Write a file of length TEST_PERSISTENCE_FILE_LENGTH to
            // message repository.
            byte [] b = new byte[BLOCK_SIZE];
            for (int i = 0; i < BLOCK_SIZE; i++) {
                b[i] = (byte)0xAA;
            }

            File tmpFile = File.createTempFile("msh", null, dir);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            for (long i = TEST_PERSISTENCE_FILE_LENGTH; i >= 0;
                 i -= BLOCK_SIZE) {
                if (i >= BLOCK_SIZE) {
                    fos.write(b);
                }
                else {
                    fos.write(b, 0, (int)i);
                }
            }

            fos.flush();
            fos.close();
            fos = null;

            if (tmpFile.delete() == false) {
                tmpFile.deleteOnExit();
            }
        }
        catch (IOException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e);
            logger.error(err);
            logger.debug("<= MessageServiceHandler.checkPersistence");
            return err;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            return err;
        }

        logger.debug("<= MessageServiceHandler.checkPersistence");
        return null;
    }

    private Document checkInternalStates()
        throws MessageServiceHandlerException {
        return Utility.getFailureResponse("NOT IMPLEMENTED");
    }

    private Document testLoopback() throws MessageServiceHandlerException {
        return Utility.getFailureResponse("NOT IMPLEMENTED");
    }

    private Document deletePendingMessages(Map messageIdMap)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.deletePendingMessages");

        HashMap resultMap = new HashMap();

        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root =
                document.createElement(Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);
            final Element elem_successful =
                document.createElement(Constants.ELEMENT_SUCCEEDED);
            final Element elem_failed = document.createElement
                                        (Constants.ELEMENT_FAILED);

            Iterator it = messageIdMap.keySet().iterator();
            while (it.hasNext()) {
                String messageId = (String)it.next();
                String resultMsgId = Constants.QUERY_RESULT_PREFIX + messageId;

                final Element elem_message =
                    document.createElement(Constants.ELEMENT_MESSAGE);
                elem_message.setAttribute
                    (Constants.ATTRIBUTE_MESSAGE_ID, messageId);

                synchronized (sendThreadMap) {
                    MessageProcessor processor =
                        (MessageProcessor)sendThreadMap.get(messageId);
                    if (processor != null) {
                        if (processor.isWaitingRetry()) {
                            processor.shutDown(HALT_DELETE_LEVEL);
                            elem_successful.appendChild(elem_message);
                        }
                        else {
                            elem_failed.appendChild(elem_message);
                        }
                    }
                    else {
                        elem_failed.appendChild(elem_message);
                    }
                }
            }

            elem_result.appendChild(elem_successful);
            elem_result.appendChild(elem_failed);
            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            logger.debug("<= MessageServiceHandler.deletePendingMessages");
            return document;
        }
        catch (ParserConfigurationException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    private Document getMessageStatus(Map messageIdMap)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.getMessageStatus");

        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            if (messageIdMap == null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_DATA_ERROR,
                    "messageIdMap cannot be null");
                logger.warn(err);
                throw new MessageServiceHandlerException(err);
            }
            final Map resultMap = messageServer.getMessageStatus
                                  (messageIdMap, tx);
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root =
                document.createElement(Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);

            Iterator iter = resultMap.keySet().iterator();
            while (iter.hasNext()) {
                String msgId = (String)iter.next();
                Element elem_message = document.createElement
                                       (Constants.ELEMENT_MESSAGE);
                elem_message.setAttribute
                    (Constants.ATTRIBUTE_MESSAGE_ID, msgId);

                HashMap propMap = (HashMap)resultMap.get(msgId);
                if (propMap != null) {
                    Iterator propIter = propMap.keySet().iterator();
                    while (propIter.hasNext()) {
                        ArrayList nameList = (ArrayList)propIter.next();
                        ArrayList valueList = (ArrayList)propMap.get(nameList);

                        for (int i = 0; i < nameList.size(); i++) {
                            String propName = (String)nameList.get(i);
                            Object propValue = valueList.get(i);
                            Element elem_prop =
                                document.createElement(propName);
                            if (propValue instanceof String) {
                                String value = (String)propValue;
                                elem_prop.appendChild
                                    (document.createTextNode(value));
                            }
                            else if (propValue instanceof Map) {
                                Map contentMap = (Map)propValue;
                                Iterator valueIter =
                                    contentMap.keySet().iterator();
                                while (valueIter.hasNext()) {
                                    String name = (String)valueIter.next();
                                    String value = (String)contentMap.get(name);
                                    Element elem_name =
                                        document.createElement(name);
                                    elem_name.appendChild(
                                        document.createTextNode(value));
                                    elem_prop.appendChild(elem_name);
                                }
                            }
                            elem_message.appendChild(elem_prop);
                        }
                    }
                }
                elem_result.appendChild(elem_message);
            }
            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            tx.commit();

            logger.debug("<= MessageServiceHandler.getMessageStatus");
            return document;
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            throw e;
        }
        catch (ParserConfigurationException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    private Document getPendingMessages()
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.getPendingMessages");

        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root =
                document.createElement(Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);

            synchronized (sendThreadMap) {
                for (Iterator i=sendThreadMap.keySet().iterator() ;
                     i.hasNext() ; ) {
                    String messageId = (String) i.next();
                    MessageProcessor processor =
                        (MessageProcessor)sendThreadMap.get(messageId);
                    if (processor != null && !processor.isShutDown()) {
                        Element elem_message = document.
                            createElement(Constants.ELEMENT_MESSAGE);
                        elem_message.setAttribute(
                            Constants.ATTRIBUTE_MESSAGE_ID, messageId);
                        elem_result.appendChild(elem_message);
                    }
                }
            }
            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            logger.debug("<= MessageServiceHandler.getPendingMessages");
            return document;
        }
        catch (ParserConfigurationException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    private Map getTrustedRepository() throws MessageServiceHandlerException {
        boolean isFirst = true;
        StringBuffer buf = new StringBuffer();
        Iterator it = trustedListenerRepository.iterator();
        while (it.hasNext()) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                buf.append(";");
            }
            buf.append((String)it.next());
        }

        Map resultMap = new HashMap();
        resultMap.put(Constants.QUERY_RESULT_REPOSITORY, buf.toString());

        return resultMap;
    }

    private Document archive(Date startDate, Date endDate,
                             ApplicationContext [] appContexts)
        throws MessageServiceHandlerException {
        try {
            Date invocationTime = new Date(System.currentTimeMillis());
            Map [] resultMap = null;
            if (appContexts == null) {
                logger.debug("archive by Date");
                resultMap = messageServer.archive(startDate, endDate);
            }
            else if (startDate == null && endDate == null) {
                logger.debug("archive by Application Context");
                resultMap = messageServer.archive
                    (null, invocationTime, appContexts);
            }
            else {
                logger.debug("archive by Date and Application Context");
                resultMap = messageServer.archive
                    (startDate, endDate, appContexts);
            }
            Map acceptedMap = resultMap[0];
            Map rejectedMap = resultMap[1];
            Map reasonMap = resultMap[2];

            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root = document.createElement
                                      (Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);
            final Element elem_successful = document.createElement
                                            (Constants.ELEMENT_SUCCEEDED);
            final Element elem_failed = document.createElement
                                        (Constants.ELEMENT_FAILED);
            final Element elem_timestamp = document.createElement
                                        (Constants.ELEMENT_TIMESTAMP);
            elem_timestamp.appendChild
                (document.createTextNode(Utility.toUTCString(invocationTime)));

            Iterator it = acceptedMap.keySet().iterator();
            while (it.hasNext()) {
                ApplicationContext appContext = (ApplicationContext)it.next();

                // Application Context element
                Element elem_appcontext = document.createElement
                                     (Constants.ELEMENT_APPLICATION_CONTEXT);
                Element elem_value = document.createElement
                                     (Constants.ELEMENT_VALUE);
                elem_value.appendChild
                    (document.createTextNode(appContext.toString()));
                elem_appcontext.appendChild(elem_value);

                // Message element
                Set msgSet = (Set)(acceptedMap.get(appContext));
                Iterator msgIt = msgSet.iterator();
                while (msgIt.hasNext()) {
                    String msgId = (String)msgIt.next();
                    Element elem_message = document.createElement
                                           (Constants.ELEMENT_MESSAGE);
                    elem_message.setAttribute
                        (Constants.ATTRIBUTE_MESSAGE_ID, msgId);
                    elem_appcontext.appendChild(elem_message);
                }

                elem_successful.appendChild(elem_appcontext);
            }

            it = rejectedMap.keySet().iterator();
            while (it.hasNext()) {
                ApplicationContext appContext = (ApplicationContext)it.next();
                String reason = (String)reasonMap.get(appContext);

                // Application Context element
                Element elem_appcontext = document.createElement(
                    Constants.ELEMENT_APPLICATION_CONTEXT);
                Element elem_value = document.createElement
                                     (Constants.ELEMENT_VALUE);
                elem_value.appendChild
                    (document.createTextNode(appContext.toString()));
                elem_appcontext.appendChild(elem_value);

                // Check if the archival of messages in an application context
                // was successful or not
                if (reason != null) {
                    Element elem_reason = document.createElement
                        (Constants.ELEMENT_REASON);
                    elem_reason.appendChild(document.createTextNode(reason));
                    elem_appcontext.appendChild(elem_reason);
                }

                // Message element
                Set msgSet = (Set)(rejectedMap.get(appContext));
                Iterator msgIt = msgSet.iterator();
                while (msgIt.hasNext()) {
                    String msgId = (String)msgIt.next();
                    Element elem_message = document.createElement
                                           (Constants.ELEMENT_MESSAGE);
                    elem_message.setAttribute
                        (Constants.ATTRIBUTE_MESSAGE_ID, msgId);

                    reason = (String) reasonMap.get(msgId);
                    if (reason != null) {
                        Element elem_reason = document.
                            createElement(Constants.ELEMENT_REASON);
                        elem_reason.appendChild
                            (document.createTextNode(reason));
                        elem_message.appendChild(elem_reason);
                    }

                    elem_appcontext.appendChild(elem_message);
                }

                elem_failed.appendChild(elem_appcontext);
            }

            elem_result.appendChild(elem_successful);
            elem_result.appendChild(elem_failed);
            elem_result.appendChild(elem_timestamp);
            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            return document;
        }
        catch (MessageServiceHandlerException e) {
            return Utility.getFailureResponse(e.getMessage());
        }
        catch (ParserConfigurationException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
    }

    private Document getDBConnectionPoolInfo()
        throws MessageServiceHandlerException {
        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root = document.createElement
                                      (Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);
            final Element elem_db = document.createElement
                                    (Constants.ELEMENT_DATABASE);
            elem_result.appendChild(elem_db);
            final Element elem_user = document.createElement
                                      (Constants.ELEMENT_DB_USER);
            elem_user.appendChild(document.createTextNode(
                    MessageServer.dbConnectionPool.databaseUser));
            elem_db.appendChild(elem_user);
            final Element elem_url = document.createElement
                                     (Constants.ELEMENT_DB_URL);
            elem_url.appendChild(document.createTextNode(
                    MessageServer.dbConnectionPool.databaseURL));
            elem_db.appendChild(elem_url);
            final Element elem_init = document.createElement
                                      (Constants.ELEMENT_INIT_CONN);
            elem_init.appendChild(document.createTextNode(String.valueOf(
                    MessageServer.dbConnectionPool.initialConnections)));
            elem_db.appendChild(elem_init);
            final Element elem_max = document.createElement
                                     (Constants.ELEMENT_MAX_CONN);
            elem_max.appendChild(document.createTextNode(String.valueOf(
                    MessageServer.dbConnectionPool.maximumConnections)));
            elem_db.appendChild(elem_max);
            final Element elem_created =
                document.createElement(Constants.ELEMENT_CREATED_CONN);
            elem_created.appendChild(document.createTextNode(String.valueOf(
                    MessageServer.dbConnectionPool.createdCount)));
            elem_db.appendChild(elem_created);
            final Element elem_inuse =
                document.createElement(Constants.ELEMENT_CONN_IN_USE);
            elem_inuse.appendChild(document.createTextNode(String.valueOf(
                    MessageServer.dbConnectionPool.usedCount)));
            elem_db.appendChild(elem_inuse);

            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            return document;
        }
        catch (ParserConfigurationException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
    }

    private Document getNumRecordsInDB() throws MessageServiceHandlerException {
        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Element elem_root = document.createElement
                                      (Constants.ELEMENT_RESPONSE_ROOT);
            final Element elem_result = document.createElement
                                        (Constants.ELEMENT_RESULT);

            for (int i=0 ; i<DbTableManager.DBTABLE_LIST.length ; i++) {
                final Element elem_table =
                    document.createElement(Constants.ELEMENT_TABLE);
                elem_table.setAttribute(Constants.ATTRIBUTE_NAME,
                    DbTableManager.DBTABLE_LIST[i].getName());
                final Element elem_records =
                    document.createElement(Constants.ELEMENT_NUM_RECORDS);
                int num = messageServer.getNumRecordsInDB
                    (DbTableManager.DBTABLE_LIST[i], tx);
                elem_records.appendChild
                    (document.createTextNode(String.valueOf(num)));
                elem_table.appendChild(elem_records);
                elem_result.appendChild(elem_table);
            }

            elem_root.appendChild(elem_result);
            document.appendChild(elem_root);

            tx.commit();
            return document;
        }
        catch (MessageServiceHandlerException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            return Utility.getFailureResponse(e.getMessage());
        }
        catch (ParserConfigurationException e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e,
                "parser configuration problem");
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
        catch (Exception e) {
            try {
                tx.rollback();
            }
            catch (Throwable e2) {}
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            return Utility.getFailureResponse(err);
        }
    }

    void sendDocumentResponse(Document doc, HttpServletResponse response)
        throws MessageServiceHandlerException {
        logger.debug("=> MessageServiceHandler.sendDocumentResponse");

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = response.getOutputStream();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(os);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
            os.flush();
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e,
                "cannot send document response");
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageServiceHandler.sendDocumentResponse");
    }

    /**
     * Extract MIME headers from <code>HttpServletRequest</code> object.
     *
     * @param request <code>HttpServletRequest</code> object.
     * @return MIME headers in servlet request.
     */
    private MimeHeaders getHeaders(HttpServletRequest request) {
        final MimeHeaders headers = new MimeHeaders();
        for (Enumeration i=request.getHeaderNames() ;
             i.hasMoreElements() ; ) {
            final String name = (String) i.nextElement();
            headers.addHeader(name, request.getHeader(name));
        }
        return headers;
    }

    /**
     * Extract the content type from the given MIME headers.
     *
     * @param headers   MIME headers to be parsed.
     * @return the content type of the MIME.
     * @throws MessageServiceHandlerException
     */
    private String getContentType(MimeHeaders headers)
        throws MessageServiceHandlerException {
        final String[] contentTypes = headers.getHeader
                                      (Constants.CONTENT_TYPE);
        if (contentTypes.length == 0) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_DATA_ERROR,
                "missing " + Constants.CONTENT_TYPE + " in HTTP header");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }
        else if (contentTypes.length > 1) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_DATA_ERROR,
                "more than one " + Constants.CONTENT_TYPE + " in HTTP header");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }

        return contentTypes[0];
    }

    /**
     * Check if the command object has been password-protected and authenticate
     * the command object if so.
     *
     * @param command   <code>Command</code> object to be authenticated.
     * @throws MessageServiceHandlerException
     */
    private void authenticateCommand(Command command)
        throws MessageServiceHandlerException {

        if (authPasswordFile != null) {
            String userName = command.getUserName();
            char[] password = command.getPassword();
            AuthenticationManager auth = new AuthenticationManager(
                new File(authPasswordFile));
            if (userName == null || password == null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_AUTHENTICATION_FAILED,
                    "user name/password not specified: please check MSH "
                        + "properties file in client side");
                logger.warn(err);
                throw new MessageServiceHandlerException(err);
            }
            if (!auth.authenticate(userName, new String(password))) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_AUTHENTICATION_FAILED,
                    "no such user or wrong password");
                logger.warn(err);
                throw new MessageServiceHandlerException(err);
            }
        }
    }

    /**
     * Process commands sent by the client-side request objects. It throws
     * <code>MessageServiceHandlerException</code> in case any exception
     * occurred.
     *
     * @param command   <code>Command</code> object received.
     * @param response  <code>HttpServletResponse</code> object.
     * @param isHalted  Flag indicating if the MSH is halted.
     * @throws MessageServiceHandlerException
     */
    private void processCommand(HttpServletResponse response, Command command,
                                boolean isHalted)
        throws MessageServiceHandlerException {

        logger.debug("=> MessageServiceHandler.processCommand");
        try {
            ApplicationContext appContext = null;
            MessageServiceHandlerConnection mshConnection = null;
            Document resultDoc = null;
            Map resultMap = null;
            String tmp = null;

            // Commands that should work in all modes (halted / normal)
            boolean isCommandProcessed = false;
            switch (command.getType()) {
                case CommandConstants.QUERY_MSH_STATUS:
                    logger.info("Received request to query MSH status.");
                    resultMap = new HashMap();
                    resultMap.put(Constants.QUERY_RESULT_MSH_STATUS,
                                  new Boolean(isHalted).toString());
                    isCommandProcessed = true;
                    break;

                case CommandConstants.QUERY_DB_CONN_POOL:
                    logger.info("Received request to query database "
                        + "connection pool information.");
                    resultDoc = getDBConnectionPoolInfo();
                    isCommandProcessed = true;
                    break;

                case CommandConstants.QUERY_NUM_RECORDS_IN_DB:
                    logger.info("Received request to query num records "
                        + "in database.");
                    resultDoc = getNumRecordsInDB();
                    isCommandProcessed = true;
                    break;

                case CommandConstants.MSH_ARCHIVE_BY_APPCONTEXT: {
                        logger.info("Received request to archive MSH data by "
                            + "application context.");
                        ApplicationContext [] appContexts =
                            (ApplicationContext [])command.getContent();
                        resultDoc = archive(null, null, appContexts);
                        isCommandProcessed = true;
                    }
                    break;
                case CommandConstants.MSH_ARCHIVE_BY_DATE: {
                        logger.info("Received request to archive MSH data by "
                            + "date.");
                        Date [] dates = (Date [])command.getContent();
                        resultDoc = archive(dates[0], dates[1], null);
                        isCommandProcessed = true;
                    }
                    break;
                case CommandConstants.MSH_ARCHIVE_BY_DATE_AND_APPCONTEXT:
                    logger.info("Received request to archive MSH data by "
                        + "date and application context.");
                    try {
                        byte [] b = (byte [])command.getContent();
                        ObjectInputStream ois = new ObjectInputStream
                                                (new ByteArrayInputStream(b));
                        Date [] dates = (Date [])ois.readObject();
                        ApplicationContext [] appContexts =
                            (ApplicationContext [])ois.readObject();
                        ois.close();
                        ois = null;
                        resultDoc = archive(dates[0], dates[1], appContexts);
                    }
                    catch (Exception e) {
                        throw new MessageServiceHandlerException
                                  (e.getMessage());
                    }
                    finally {
                        isCommandProcessed = true;
                    }
                    break;
            }
            if (isCommandProcessed) {
                if (resultMap != null) {
                    Iterator iter = resultMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String)iter.next();
                        response.setHeader(key, (String)resultMap.get(key));
                    }
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                else if (resultDoc != null) {
                    sendDocumentResponse(resultDoc, response);
                }
                else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                if (isHalted == false) {
                    monitor.endRequest();
                }
                logger.debug("<= MessageServiceHandler.processCommand");
                return;
            }

            // Commands that should work only when the MSH is halted
            if (isHalted) {
                switch (command.getType()) {
                    case CommandConstants.RESUME:
                        logger.info("Received request to resume MSH "
                            + "in halted state.");
                        if (resume()) {
                            resultDoc = Utility.getSuccessResponse();
                        }
                        else {
                            resultDoc = Utility.getFailureResponse(
                                "Cannot restart send threads");
                        }
                        break;
                    case CommandConstants.MSH_BACKUP:
                        logger.info("Received request to backup MSH "
                            + "in halted state.");
                        resultDoc = backup();
                        break;
                    case CommandConstants.MSH_RESTORE:
                        logger.info("Received request to restore MSH "
                            + "in halted state.");
                        try {
                            restore();
                            resultDoc = Utility.getSuccessResponse();
                        }
                        catch (MessageServiceHandlerException e) {
                            resultDoc = Utility.getFailureResponse
                                (e.getMessage());
                        }
                        break;
                    default:
                        try {
                            response.sendError(
                                HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                "MSH maintenance in progress. "
                                + "Request not accepted.");
                        }
                        catch (IOException e) {};
                        logger.debug("<= MessageServiceHandler.processCommand");
                        return;
                }
                if (resultDoc != null) {
                    sendDocumentResponse(resultDoc, response);
                }
                else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                logger.debug("<= MessageServiceHandler.processCommand");
                return;
            }

            // Commands that should work only if the MSH is not halted
            switch (command.getType()) {

            // System commands
            case CommandConstants.REGISTER_MSH_CONFIG:
                final MessageServiceHandlerConfig mshConfig =
                    (MessageServiceHandlerConfig)
                    command.getContent();
                resultMap = new HashMap();
                if (mshConfig.isEnabled()) {
                    logger.info("Received request for registering msh config");
                    register(mshConfig);
                    resultMap.put(Constants.QUERY_RESULT_REGISTRATION, "1");
                }
                else {
                    logger.info(
                        "Received request for unregistering msh config");
                    if (unregister(mshConfig)) {
                        resultMap.put(Constants.QUERY_RESULT_REGISTRATION,
                                      "1");
                    }
                    else {
                        resultMap.put(Constants.QUERY_RESULT_REGISTRATION,
                                      "0");
                    }
                }
                break;
            case CommandConstants.ENABLE_ACKNOWLEDGMENT:
                logger.info("Received request of enable ack.");
                suppressedAck = false;
                break;
            case CommandConstants.DISABLE_ACKNOWLEDGMENT:
                logger.info("Received request of disable ack.");
                suppressedAck = true;
                break;
            case CommandConstants.IGNORE_ACKNOWLEDGMENT:
                logger.info("Received request of ignore ack.");
                ignoreAck = true;
                break;
            case CommandConstants.ACCEPT_ACKNOWLEDGMENT:
                logger.info("Received request of accept ack.");
                ignoreAck = false;
                break;
            case CommandConstants.TEST_LOOPBACK:
                logger.info("Received request of testing loopback.");
                resultDoc = testLoopback();
                break;
            case CommandConstants.RESET_DB_CONNECTION_POOL:
                logger.info("Received request to reset database "
                    + "connection pool.");
                messageServer.resetConnectionPool();
                resultDoc = Utility.getSuccessResponse();
                break;
            case CommandConstants.HALT_SUSPEND:
                logger.info("Received request to halt MSH.");
                monitor.endRequest();
                halt(HALT_SUSPEND_LEVEL);
                break;
            case CommandConstants.HALT_TERMINATE:
                logger.info("Received request to clean halt MSH.");
                monitor.endRequest();
                halt(HALT_TERMINATE_LEVEL);
                break;
            case CommandConstants.RESUME:
                logger.info("Received request to resume MSH");
                tmp = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_STATE_ERROR, "cannot resume MSH "
                    + "in non-halted state.");
                logger.warn(tmp);
                resultDoc = Utility.getFailureResponse(tmp);
                break;
            case CommandConstants.MSH_BACKUP:
                logger.info("Received request to backup MSH");
                tmp = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_STATE_ERROR, "cannot backup MSH "
                    + "in non-halted state.");
                logger.warn(tmp);
                resultDoc = Utility.getFailureResponse(tmp);
                break;
            case CommandConstants.MSH_RESTORE:
                logger.warn("Received request to restore MSH");
                tmp = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_STATE_ERROR, "cannot restore MSH "
                    + "in non-halted state.");
                logger.warn(tmp);
                resultDoc = Utility.getFailureResponse(tmp);
                break;

            // System queries
            case CommandConstants.CHECK_DATABASE:
                logger.info("Received request to check database.");
                tmp = messageServer.checkDatabase();
                if (tmp == null) {
                    resultDoc = Utility.getSuccessResponse();
                }
                else {
                    resultDoc = Utility.getFailureResponse(tmp);
                }
                break;
            case CommandConstants.CHECK_PERSISTENCE:
                logger.info("Received request to check persistence "
                    + "storage.");
                tmp = checkPersistence();
                if (tmp == null) {
                    resultDoc = Utility.getSuccessResponse();
                }
                else {
                    resultDoc = Utility.getFailureResponse(tmp);
                }
                break;
            case CommandConstants.CHECK_INTERNAL_STATES:
                logger.info("Received request to check internal states.");
                resultDoc = checkInternalStates();
                break;
            case CommandConstants.REPORT_ENVIRONMENT:
                logger.info("Received request to check Java environment.");
                resultDoc = getEnvironment();
                break;

            // Commands
            case CommandConstants.SEND_MESSAGE:
                logger.info("Received request to send message");
                sendMessage((ApplicationContext) command.getContent(),
                            command.getEbxmlMessage());
                break;
            case CommandConstants.SEND_MESSAGE_AUTOMATIC_REGISTER:
                logger.info("Receive request to send message"
                    + " with automatic registration");
                
                sendMessageWithAutomaticRegistration(
                    (SendCommandParameter) command.getContent(),
                    command.getEbxmlMessage());
                break;
            case CommandConstants.GET_MESSAGE:
                appContext = (ApplicationContext) command.getContent();
                getMessage(appContext, response);
                monitor.endRequest();
                logger.debug("<= MessageServiceHandler.processCommand");
                return;
            case CommandConstants.GET_MESSAGE_ID:
                appContext = (ApplicationContext) command.getContent();
                logger.info("Received request to receive MessageId's");
                getUndeliveredMessageIds(appContext, response);
                monitor.endRequest();
                logger.debug("<= MessageServiceHandler.processCommand");
                return;
            case CommandConstants.GET_MESSAGE_BY_ID:
                String messageId = (String) command.getContent();
                logger.info("Received request to receive message: messageId=" +
                            messageId);
                getMessageById(messageId, response);
                monitor.endRequest();
                logger.debug("<= MessageServiceHandler.processCommand");
                return;
            case CommandConstants.QUERY_RESET_SEQUENCE_NUMBER:
                logger.info("Received request to reset sequence "
                    + "number for conversation ID: "
                    + (String)command.getContent());
                sentSequenceMap.put(command.getContent(), new Integer(0));
                resultMap = new HashMap();
                resultMap.put(Constants.QUERY_RESULT_SEQUENCE_NUMBER, "0");
                break;
            case CommandConstants.QUERY_SEQUENCE_NUMBER:
                tmp = (String)command.getContent();
                logger.info("Received request to get sequence number for "
                    + "conversation ID: " + tmp);
                Integer seqObj = (Integer)sentSequenceMap.get(tmp);
                resultMap = new HashMap();
                if (seqObj == null) {
                    sentSequenceMap.put(tmp, new Integer(0));
                    resultMap.put(Constants.QUERY_RESULT_SEQUENCE_NUMBER,
                                  "0");
                }
                else {
                    int seqNo = seqObj.intValue();
                    resultMap.put(Constants.QUERY_RESULT_SEQUENCE_NUMBER,
                        (String.valueOf(++seqNo % RANGE_SEQUENCE_NUMBER)));
                    sentSequenceMap.put(tmp, new Integer(seqNo));
                }
                break;
            case CommandConstants.DELETE_PENDING_MESSAGE:
                logger.info("Received requeset for deleting pending "
                    + "messages");
                HashMap messageIdMap = (HashMap)command.getContent();
                resultDoc = deletePendingMessages(messageIdMap);
                break;

            // Queries
            case CommandConstants.QUERY_MESSAGE_STATUS:
                logger.info("Received request for getting message status");
                messageIdMap = (HashMap)command.getContent();
                resultDoc = getMessageStatus(messageIdMap);
                break;

            case CommandConstants.QUERY_TRUSTED_REPOSITORY:
                logger.info("Received request for getting "
                    + "trusted repository path");
                resultMap = getTrustedRepository();
                break;

            case CommandConstants.QUERY_PENDING_MESSAGE:
                logger.info("Received request for getting "
                    + "pending messages");
                resultDoc = getPendingMessages();
                break;

            default:
                tmp = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                    "unknown command type");
                logger.warn(tmp);
                throw new MessageServiceHandlerException(tmp);
            }

            // Return the result
            if (resultDoc != null) {
                sendDocumentResponse(resultDoc, response);
            }
            else if (resultMap != null) {
                Iterator iter = resultMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    response.setHeader(key, (String)resultMap.get(key));
                }
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            monitor.endRequest();
            logger.debug("<= MessageServiceHandler.processCommand");
            return;
        }
        catch (MessageServiceHandlerException e) {
            throw e;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Retrieve data from <code>HttpServletRequest</code> input stream.
     *
     * @param request   <code>HttpServletRequest</code> object.
     * @param headers   <code>MimeHeaders</code>
     * @return The file name of the persisted message.
     */
    public String receiveData(HttpServletRequest request, MimeHeaders headers,
        Transaction tx) throws MessageServiceHandlerException {

        try {
            logger.debug("receive incoming message");
            final String[] contentLengths = headers.getHeader
                                            (Constants.CONTENT_LENGTH);
            final String contentLength;
            if (contentLengths == null || contentLengths.length == 0) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_DATA_ERROR,
                    "missing " + Constants.CONTENT_LENGTH + ": integrity of "
                    + "incoming message is not ensured");
                logger.warn(err);
                contentLength = null;
            }
            else {
                contentLength = contentLengths[0];
            }

            /* Save the servlet request inputstream to file */
            File tempFile = File.createTempFile(Constants.TEMP_FILE_PREFIX,
                Constants.TEMP_FILE_SUFFIX, new File(messageRepository));
            String fileName = tempFile.getCanonicalPath();
            tx.storeFileName(fileName);
            FileOutputStream fos = new FileOutputStream(tempFile);
            InputStream requestStream = request.getInputStream();
            byte[] buffer = new byte[65536];
            int length = 0;
            for (int c = requestStream.read(buffer) ; c != -1 ;
                 c=requestStream.read(buffer)) {
                fos.write(buffer, 0, c);
                length += c;
            }
            fos.close();

            if (contentLength != null) {
                if (!contentLength.equals(String.valueOf(length))){
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR,
                        "content length mismatched");
                    logger.warn(err);
                    throw new MessageServiceHandlerException(err);
                }
            }

            /* Find the MIME boundary in the received message */
            /*
            final InputStreamReader inputStreamReader = new InputStreamReader
                (new ByteArrayInputStream(requestBytes),
                Constants.CHARACTER_ENCODING);
            final LineNumberReader lineReader =
                new LineNumberReader(inputStreamReader);
            String s = lineReader.readLine();
            int offset = 0;
            int length = 0;
            while (s != null &&
                !(s.startsWith(Constants.MIME_BOUNDARY_PREFIX))) {
                offset += s.getBytes().length;
                for (; offset<requestBytes.length &&
                    (requestBytes[offset]==0xA || requestBytes[offset]==0xD) ;
                    offset++);
                s = lineReader.readLine();
            }
            if (s == null) {
            */
                /* Missing MIME boundary in received message */
            /*
                logger.info("Missing MIME boundary in received message");
                offset = 0;
                length = requestBytes.length;
            }
            else {
                offset += s.getBytes().length;
                for (; offset<requestBytes.length &&
                    (requestBytes[offset]==0xA || requestBytes[offset]==0xD) ;
                    offset++);
            */
                /* Find the empty line delimiter separating the MIME header and
                the SOAPPart content
                */
            /*
                s = lineReader.readLine();
                while (s != null && s.length() != 0) {
                    offset += s.getBytes().length;
                    for (; offset<requestBytes.length &&
                            (requestBytes[offset]==0xA ||
                            requestBytes[offset]==0xD) ; offset++);
                    s = lineReader.readLine();
                }
    
                if (s == null) {
                    final String err =
                        "Missing empty line delimiter of MIME header!";
                    logger.error(err);
                    throw new MessageServiceHandlerException(err);
                }
                offset += s.getBytes().length;
                for (; offset<requestBytes.length &&
                        (requestBytes[offset]==0xA ||
                        requestBytes[offset]==0xD) ; offset++);
            */
                /* Find the location and the length of the SOAPPart content
                with offset being the beginning position in requestBytes
                */
            /*
                s = lineReader.readLine();
                while (s != null &&
                    !(s.startsWith(Constants.MIME_BOUNDARY_PREFIX))) {
                    length += s.getBytes().length;
                    for (; (offset+length)<requestBytes.length &&
                            (requestBytes[offset+length]==0xA ||
                            requestBytes[offset+length]==0xD) ; length++);
                    s = lineReader.readLine();
                }
                lineReader.close();
                inputStreamReader.close();
            }
        */
            logger.debug
                ("finished retrieving raw message from HTTP connection");
            return fileName;
        }
        catch (MessageServiceHandlerException e) {
            throw e;
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    /**
     * Internalize and process messages, and send the response back to the
     * client.
     *
     * @param response <code>HttpServletResponse</code> object.
     * @param headers  MIME headers.
     * @param data     raw data in byte array.
     * @param requestProperty
     * @return status of the message received.
     * @throws MessageServiceHandlerException
     */
    private String processMessage(HttpServletResponse response,
        MimeHeaders headers, String fileName, HashMap requestProperty,
        Transaction tx) throws MessageServiceHandlerException {

        try {
            /* Parse the SOAPPart and verify the XML signature */
            SOAPMessage soapMessage = null;
            SOAPMessage responseMessage = null;
            try {
                soapMessage = (SOAPMessage) MessageServer.getMessageFromFile
                    (new File(fileName), false);
            }
            catch (Exception e) {
                SOAPValidationException se = new SOAPValidationException
                    (SOAPValidationException.SOAP_FAULT_CLIENT, e.getMessage());
                responseMessage = se.getSOAPMessage();
            }

            if (responseMessage == null) {
                try {
                    SOAPPart part = soapMessage.getSOAPPart();
                    SOAPBody body = part.getEnvelope().getBody();
                    if (body.hasFault()) {
                        SOAPFault fault = body.getFault();
                        logger.info("SOAP Fault message has been received:"
                            + "\n  - fault code: " + fault.getFaultCode()
                            + "\n  - fault string: " + fault.getFaultString()
                            + "\n  - fault actor: " + fault.getFaultActor());

                        Detail detail = fault.getDetail();
                        if (detail != null) {
                            Iterator it = detail.getDetailEntries();
                            while (it.hasNext()) {
                                logger.error("\n  - fault detail: " +
                                    (String)it.next());
                            }
                        }
                    }
                }
                catch (SOAPException e) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_SOAP_CANNOT_INTERNALIZE, e);
                    logger.warn(err);
                    SOAPValidationException se = new SOAPValidationException(
                        SOAPValidationException.SOAP_FAULT_CLIENT, err);
                    responseMessage = se.getSOAPMessage();
                }
            }

            EbxmlMessage ebxmlMessage = new EbxmlMessage(soapMessage);
            /*
            File newFile = new File(DirectoryManager.getRepositoryFileName
                                    (ebxmlMessage.getMessageId()));
            boolean renamed = true;
            if ((new File(fileName)).renameTo(newFile)) {
                tx.storeFileName(newFile.getCanonicalPath());
                tx.removeFileName(fileName);
            }
            else {
                newFile = new File(fileName);
                renamed = false;
            }
            */
            boolean renamed = true;
            OutputStream ostream = null;
            InputStream istream = null;
            DataSource dataSource = null;
            PersistenceHandler handler
                    = PersistenceManager.getRepositoryPersistenceHandler(); 
            try {
                dataSource = handler.createNewObject();
                ostream = dataSource.getOutputStream();
                istream = new FileInputStream(fileName);
                byte[] buffer = new byte[2048];
                int read = istream.read(buffer);
                while (read != -1) {
                    ostream.write(buffer, 0, read);
                    read = istream.read(buffer);
                }
                logger.debug("Persist message to " + dataSource.getName()
                        + " on persistence handler");
                renamed = true;
            } catch (Exception e) {
                if (dataSource != null) {
                    handler.removeObject(dataSource.getName());
                }
                throw e;
            } finally {
                if (ostream != null) {
                    ostream.close();
                }
                if (istream != null) {
                    istream.close();
                }
            }
            soapMessage = null;
            ebxmlMessage = null;
            try {
                if (!renamed) {
                    ebxmlMessage = MessageServer.getMessageFromFile(
                            new File(fileName));
                    ebxmlMessage.setFileName(null);
                } else {
                    ebxmlMessage = (EbxmlMessage)
                            MessageServer.getMessageFromDataSource(dataSource,
                                    true);
                    ebxmlMessage.setPersistenceInfo(dataSource.getName(),
                            handler);
                }
            }
            catch (Exception e) {
                SOAPValidationException se = new SOAPValidationException
                    (SOAPValidationException.SOAP_FAULT_CLIENT, e.getMessage());
                responseMessage = se.getSOAPMessage();
            }

            String status = null;
            /* Verify number of payloads is consistent with header */
            if (responseMessage == null && ebxmlMessage != null) {
                String payloadInError = ebxmlMessage.getPayloadInError();

                if (payloadInError != null) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR,
                        "manifest entry refers to non-existent payload");
                    logger.warn(err);
                    status = "Payload / Manifest inconsistent";
                    responseMessage = generateErrorMessage(ebxmlMessage,
                        ErrorList.CODE_MIME_PROBLEM, ErrorList.SEVERITY_ERROR,
                        err, payloadInError).getSOAPMessage();
                }
            }

            if (responseMessage == null && ebxmlMessage != null) {
                EbxmlMessage message = onMessage(ebxmlMessage, requestProperty);
                responseMessage = (message == null ? null :
                                   message.getSOAPMessage());
            }

            if (responseMessage != null) {
                Iterator i;
                for (i = responseMessage.getMimeHeaders().getAllHeaders() ;
                     i.hasNext() ; ) {
                    final MimeHeader header = (MimeHeader) i.next();
                    response.setHeader(header.getName(), header.getValue());
                }

                response.setStatus(HttpServletResponse.SC_OK);
                final ServletOutputStream outputStream = response.
                    getOutputStream();
                responseMessage.writeTo(outputStream);
                outputStream.flush();
                logger.info("Response message is sent back");
            }
            else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("No content is sent back");
            }

            /*
            if (ebxmlMessage != null) {
                Transaction tx = new Transaction
                                 (MessageServer.dbConnectionPool);
                try {
                    requestProperty.put(DbTableManager.ATTRIBUTE_STATUS,status);
                    messageServer.logReceivedMessage
                        (ebxmlMessage, requestProperty, tx);
                    tx.commit();
                }
                catch (Throwable t3) {
                    logger.error("Cannot log received message: "
                                 + t3.getMessage());
                    try {
                        tx.rollback();
                    }
                    catch (Throwable t4) {};
                }
            }
            */
            return status;
        }
        catch (IOException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_SERVLET_IO_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SERIALIZE, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerException(err);
        }
    }

    protected EbxmlMessage dispatchMessage(EbxmlMessage ebxmlMessage,
        Map requestProperty, Transaction tx)
        throws MessageServiceHandlerException {

        Throwable throwable = null;
        EbxmlMessage response = null;
        try {
            logger.debug("=> MessageServiceHandler.dispatchMessage");
            final String messageId = ebxmlMessage.getMessageId();
            boolean isVerifyFail = false;
            try {
                isVerifyFail = hasSignature(ebxmlMessage)
                        && !verify(ebxmlMessage);
            } catch (Exception e) {
                String err = "Error on verifying signature : " + e;
                logger.error(err);
                isVerifyFail = true;
            }
            if (isVerifyFail) {
                final String status = "Signature verification failed";
                requestProperty.put(DbTableManager.ATTRIBUTE_STATUS, status);
                try {
                    messageServer.logReceivedMessage
                        (ebxmlMessage, requestProperty, tx);
                }
                catch (MessageServerException mse) {}

                response = generateErrorMessage(ebxmlMessage, ErrorList.
                    CODE_SECURITY_FAILURE, ErrorList.SEVERITY_ERROR,
                    "Security Checks Failed");
                return response;
            }

            final Acknowledgment acknowledgment =
                ebxmlMessage.getAcknowledgment();
            final AckRequested ackRequested = ebxmlMessage.getAckRequested();
            final StatusRequest statusRequest = ebxmlMessage.getStatusRequest();
            final StatusResponse statusResponse =
                ebxmlMessage.getStatusResponse();
            final MessageOrder messageOrder = ebxmlMessage.getMessageOrder();
            final String conversationId = ebxmlMessage.getConversationId();
            final String service = ebxmlMessage.getService();
            final String action = ebxmlMessage.getAction();
            final boolean isPing = service.equals(Constants.SERVICE) &&
                action.equals(Constants.ACTION_PING);
            final boolean isPong = service.equals(Constants.SERVICE) &&
                action.equals(Constants.ACTION_PONG);
            final boolean isError = (ebxmlMessage.getErrorList() != null);

            ApplicationContext appContext;
            MessageServiceHandlerConnection mshConnection = null;
            if (statusRequest == null && statusResponse == null &&
                acknowledgment == null && !isPing && !isPong && !isError) {
                appContext = getApplicationContext(ebxmlMessage.getCpaId(),
                    ebxmlMessage.getConversationId(), service, action);
                mshConnection = (appContext == null ? null :
                                 (MessageServiceHandlerConnection)
                                 mshConnectionTable.get(appContext));
                if (mshConnection == null || !mshConnection.isEnabled()) {
                    try {
                        requestProperty.put(DbTableManager.ATTRIBUTE_STATUS,
                                            "Unknown ApplicationContext");
                        messageServer.logReceivedMessage
                            (ebxmlMessage, requestProperty, tx);
                    }
                    catch (MessageServerException mse) {}

                    ApplicationContext appContextInFault = new
                        ApplicationContext(ebxmlMessage.getCpaId(),
                            ebxmlMessage.getConversationId(), service, action);
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_APP_CONTEXT, 
                        appContextInFault.toString());
                    logger.warn(err);

                    response = generateErrorMessage(ebxmlMessage, ErrorList.
                        CODE_VALUE_NOT_RECOGNIZED, ErrorList.SEVERITY_ERROR,
                        "Application context " + appContextInFault.toString()
                        + " cannot be recognized.");
                    return response;
                }
            }
            else {
                appContext = new ApplicationContext(ebxmlMessage.getCpaId(),
                    ebxmlMessage.getConversationId(), service, action);
                mshConnection = null;
            }

            String status = "Received";
            // Check if it is an incoming error message
            if (isError) {
                logger.debug("Error message received");
                final String refToMessageId = ebxmlMessage.getMessageHeader().
                    getRefToMessageId();
                logger.debug("RefToMessageId: " + refToMessageId);
                if (refToMessageId != null) {
                    final ApplicationContext refToAppContext =
                        messageServer.getApplicationContext(refToMessageId, tx);
                    if (refToAppContext != null) {
                        appContext = refToAppContext;
                        mshConnection = (appContext == null ? null :
                                         (MessageServiceHandlerConnection)
                                         mshConnectionTable.get(appContext));
                    }
                    else {
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_DATA_ERROR,
                            "RefToAppContext is null - error is ignored");
                        logger.warn(err);
                        response = generateErrorMessage
                            (ebxmlMessage, ErrorList.CODE_VALUE_NOT_RECOGNIZED,
                            ErrorList.SEVERITY_ERROR,
                            "Unknown RefToMessageId");
                        return response;
                    }
                }
                else {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR,
                        "missing RefToMessageId - error is ignored");
                    logger.warn(err);
                    response = generateErrorMessage
                        (ebxmlMessage, ErrorList.CODE_VALUE_NOT_RECOGNIZED,
                         ErrorList.SEVERITY_ERROR,
                         "Missing RefToMessageId");
                    return response;
                }
            }
            else {
                // Message is validated only if it is not an error message in
                // order to avoid infinite loop
                try {
                    logger.debug("validating incoming ebXML message");
                    EbxmlMessageValidator validator =
                        new EbxmlMessageValidator();
                    validator.validate(ebxmlMessage);
                    logger.debug("validation of incoming message ok");
                }
                catch (EbxmlValidationException e) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR, e,
                        "invalid message received");
                    logger.warn(err);

                    if (mshConnection != null) {
                        mshConnection.send(e.getEbxmlMessage(), tx);
                        return response;
                    }
                    else {
                        response = e.getEbxmlMessage();
                        return response;
                    }
                }
            }

            // Check TimeToLive value against internal clock in UTC
            // If the message has expired, send an error message back
            final String ttlString = ebxmlMessage.getTimeToLive();
            if (ttlString != null) {
                final Date timestamp = Utility.fromUTCString(ttlString);
                EbxmlMessage errorMessage = null;

                if (timestamp == null) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR,
                        "cannot recognize TTL value in message");
                    logger.warn(err);
                    errorMessage = generateErrorMessage
                        (ebxmlMessage, ErrorList.CODE_VALUE_NOT_RECOGNIZED,
                         ErrorList.SEVERITY_ERROR,
                         "Value cannot be parsed / recognized");
                }
                else {
                    final boolean isExpired = timestamp.before(new Date());
                    if (isExpired) {
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_DATA_ERROR,
                            "message is expired");
                        logger.warn(err);
                        logger.debug("prepare error message");
                        errorMessage = generateErrorMessage
                            (ebxmlMessage, ErrorList.CODE_TIME_TO_LIVE_EXPIRED,
                             ErrorList.SEVERITY_ERROR,
                             "TimeToLive value expired");
                    }
                }

                if (errorMessage != null) {
                    // Check if we can send the message asynchronously. If not,
                    // then deliver it using the same channel
                    if (mshConnection != null) {
                        logger.debug("error message sent asynchronously");
                        mshConnection.send(errorMessage, tx);
                        return response;
                    }
                    else {
                        logger.debug("error message sent synchronously");
                        response = errorMessage;
                        return response;
                    }
                }
            }

            // check the reliable nature of the message matches with
            // the registration or not
            if (statusRequest == null && statusResponse == null &&
                acknowledgment == null && !isPing && !isPong && !isError) {
                if (mshConnection != null) {
                    int ackRequestedInReg = mshConnection
                        .getMessageServiceHandlerConfig()
                        .getAckRequested();
                    EbxmlMessage errorMessage = null;

                    if (ackRequestedInReg == Request.ACK_REQUESTED_ALWAYS 
                        && ackRequested == null) {
                        errorMessage = generateErrorMessage
                            (ebxmlMessage, ErrorList.CODE_INCONSISTENT,
                             ErrorList.SEVERITY_ERROR,
                             "Reliable messaging expected");
                    }
                    else if (ackRequestedInReg == Request.ACK_REQUESTED_NEVER 
                             && ackRequested != null) {
                        errorMessage = generateErrorMessage
                            (ebxmlMessage, ErrorList.CODE_INCONSISTENT,
                             ErrorList.SEVERITY_ERROR,
                             "Reliable messaging not expected");
                    }

                    if (errorMessage != null) {
                        // should send asynchronously
                        logger.debug("error message sent asynchronously");
                        mshConnection.send(errorMessage, tx);
                        return response;
                    }
                }
                else {
                    // no registration, so skip the checking
                }
            }

            if (messageOrder != null) {
                int seqNo = messageOrder.getSequenceNumber();
                int orderStatus = messageOrder.getStatus();

                // Check the range of sequence number
                if (seqNo < MessageServer.MIN_MESSAGE_ORDER ||
                    seqNo > MessageServer.MAX_MESSAGE_ORDER) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_DATA_ERROR,
                        "MessageOrder sequence number out of "
                        + "[" + MessageServer.MIN_MESSAGE_ORDER + ","
                        + MessageServer.MAX_MESSAGE_ORDER + "] range");
                    logger.warn(err);
                    response = generateErrorMessage(ebxmlMessage, ErrorList.
                        CODE_VALUE_NOT_RECOGNIZED, ErrorList.SEVERITY_ERROR,
                        "Message order sequence number must fall within "
                        + MessageServer.MIN_MESSAGE_ORDER + " and "
                        + MessageServer.MAX_MESSAGE_ORDER + ".");
                    return response;
                }

                // Validate and check for duplicate sequence number
                DeliveryRecord record;
                synchronized(deliveryMap) {
                    record = (DeliveryRecord) deliveryMap.get(conversationId);
                    if (record == null) {
                        record = new DeliveryRecord();
                        deliveryMap.put(conversationId, record);
                    }
                }

                if (orderStatus == MessageOrder.STATUS_RESET) {
                    if (record.hasUndelivered() &&
                        record.getLastDelivered() != -1) {
                        String msg = "a status reset message has been "
                            + "received before current messages are delivered";
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_DATA_ERROR, msg);
                        logger.warn(err);
                        response = generateErrorMessage(ebxmlMessage,
                            ErrorList.CODE_INCONSISTENT, ErrorList.
                            SEVERITY_ERROR, msg);
                        return response;
                    }
                    else {
                        record.addUndelivered(seqNo);
                        tx.addDeliveryRecord(record, seqNo);
                        deliveryMap.put(conversationId, record);
                    }
                }
                else {
                    if (record.contains(seqNo)) {

                        tx.lock(messageId);
                        boolean isMessageReceived = messageServer.
                            hasReceived(ebxmlMessage, appContext, tx);

                        if (!isMessageReceived) {
                            String msg = "a message having the same message "
                                + "order already exists, and this is not a "
                                + "STATUS_RESET message.";
                            String err = ErrorMessages.getMessage(
                                ErrorMessages.ERR_HERMES_DATA_ERROR, msg);
                            logger.warn(err);
                            response = generateErrorMessage(ebxmlMessage,
                                ErrorList.CODE_INCONSISTENT, ErrorList.
                                SEVERITY_ERROR, msg);
                            return response;
                        }
                    }
                    else {
                        record.addUndelivered(seqNo);
                        tx.addDeliveryRecord(record, seqNo);
                    }
                }
            }

            if (acknowledgment != null) {
                final String refToMessageId =
                    acknowledgment.getRefToMessageId();
                logger.debug("message is an Acknowledgement, referring to <"
                    + refToMessageId + ">");
                if (ignoreAck) {
                    logger.debug("Ack ignored");
                    status = "Ignored";
                }
                else {
                    if (messageServer.ackReceived
                        (refToMessageId, ebxmlMessage, appContext, tx)) {
                        synchronized(sendThreadMap) {
                            final MessageProcessor sendThread =
                                (MessageProcessor) sendThreadMap.
                                get(refToMessageId);
                            if (sendThread != null) {
                                sendThread.wakeUp(true);
                            }
                        }
                    }
                }
            }
            else if (ackRequested != null) {
                logger.debug("message has an AckReq");
                tx.lock(messageId);
                if (messageServer.hasReceived(ebxmlMessage, appContext, tx)) {
                    logger.debug("message has been received previously");
                    status = "Received before - ";
                    if (!ebxmlMessage.getDuplicateElimination()) {
                        messageServer.setDeliveryStatus
                            (ebxmlMessage.getMessageId(), false, tx);
                        Delivery delivery = new Delivery(this, appContext,
                            mshConnection.getMessageServiceHandlerConfig().
                            getMessageListener(), ebxmlMessage);
                        tx.addThread(delivery);
                        status = status + "Deliver to Application - ";
                    }

                    if (suppressedAck) {
                        logger.debug("Ack sending suppressed");
                        status = status + "Acknowledgment suppressed";
                    }
                    else {
                        EbxmlMessage refToMessage = messageServer.
                            getRefToMessage(messageId, tx);
                        boolean shouldSend = true;
                        int syncReplyMode = mshConnection.
                            getMessageServiceHandlerConfig().getSyncReply();
                        if (refToMessage == null) {
                            logger.debug("old Ack not found");
                            refToMessage = generateAcknowledgment(ebxmlMessage,
                                                                  messageId);
                            if (syncReplyMode == Constants.
                                SYNC_REPLY_MODE_NONE) {
                                messageServer.store(refToMessage, appContext,
                                    MessageServer.STATE_SENT_STARTED, true, tx);
                            }
                            else {
                                messageServer.store(refToMessage, appContext,
                                    MessageServer.STATE_SENT, true, tx);
                            }
                            logger.debug("old Acknowledgment missing");
                            status = status + "Old Acknowledgment missing";
                        }
                        else {
                            String oldAckMsgId = refToMessage.getMessageId();
                            if (sendThreadMap.get(oldAckMsgId) == null) {
                                if (syncReplyMode == Constants.
                                    SYNC_REPLY_MODE_NONE) {
                                    messageServer.resend(oldAckMsgId, tx);
                                    logger.debug("old Acknowledgment resent");
                                    status += "Old Acknowledgment resent";
                                }
                                else {
                                    logger.debug("Old Acknowledgment replied " 
                                        + "synchronously");
                                    status += "Old Acknowledgment replied " 
                                        + "synchronously";
                                }
                            }
                            else {
                                logger.debug("Old Acknowledgment is being " 
                                    + "resent; skipping.");
                                status = status + "Old Acknowledgement is "
                                    + "being resent; skipping.";
                                shouldSend = false;
                            }
                        }

                        if (shouldSend) {
                            if (syncReplyMode == Constants.
                                SYNC_REPLY_MODE_NONE) {
                                final MessageProcessor messageProcessor = new
                                    MessageProcessor(refToMessage,
                                    mshConnection.
                                    getMessageServiceHandlerConfig(), this);
                                addSendThread(refToMessage.getMessageId(),
                                              messageProcessor);
                                messageProcessor.start();
                            }
                            else {
                                response = refToMessage;
                            }
                        }
                    }
                }
                else {
                    logger.debug("message has not been received previously");
                    status = "Received firstly - ";
                    messageServer.store(ebxmlMessage, appContext,
                        MessageServer.STATE_RECEIVED, false, tx);
                    Delivery delivery = new Delivery(this, appContext,
                        mshConnection.getMessageServiceHandlerConfig().
                        getMessageListener(), ebxmlMessage);
                    tx.addThread(delivery);

                    final EbxmlMessage ackMessage =
                        generateAcknowledgment(ebxmlMessage, messageId);

                    if (suppressedAck) {
                        logger.debug("Ack sending suppressed");
                        status = status + "Acknowledgment suppressed";
                    }
                    else if (mshConnection.getMessageServiceHandlerConfig().
                        getSyncReply() == Constants.SYNC_REPLY_MODE_NONE) {
                        mshConnection.send(ackMessage, tx);
                        logger.debug("Ack sent");
                        status = status + "Acknowledgment sent";
                    }
                    else {
                        messageServer.store(ackMessage, appContext,
                            MessageServer.STATE_SENT, true, tx);
                        logger.debug("Ack replied synchronously");
                        status += "Acknowledgment replied synchronously";
                        response = ackMessage;
                    }
                }
            }
            else if (statusRequest != null) {
                logger.debug("Status Request message is received");
                final String refToMessageId = statusRequest.getRefToMessageId();
                final String [] result = messageServer.
                    getMessageStatus(refToMessageId, tx);
                final String messageStatus = result[0];
                final String timestamp = result[1];
                if (!messageStatus.equals(Constants.STATUS_NOT_RECOGNIZED) &&
                    !messageStatus.equals(Constants.STATUS_NOT_RECOGNIZED)) {
                    appContext = new ApplicationContext
                        (result[2], result[3], result[4], result[5]);
                    messageServer.store(ebxmlMessage, appContext,
                        MessageServer.STATE_RECEIVED, true, tx);
                }

                response = generateStatusResponseMessage(ebxmlMessage,
                    messageStatus, timestamp);
                if (!messageStatus.equals(Constants.STATUS_NOT_RECOGNIZED) &&
                    !messageStatus.equals(Constants.STATUS_NOT_RECOGNIZED)) {
                    messageServer.store(response, appContext,
                        MessageServer.STATE_SENT, true, tx);
                }
                logger.debug("Status Response message is sent back");
            }
            else if (statusResponse != null) {
                String statusRequestMessageId = ebxmlMessage.
                    getMessageHeader().getRefToMessageId();
                appContext = messageServer.
                    getApplicationContext(statusRequestMessageId, tx);
                mshConnection = (MessageServiceHandlerConnection)
                    mshConnectionTable.get(appContext);
                messageServer.store(ebxmlMessage, appContext,
                    MessageServer.STATE_RECEIVED, false, tx);
                Delivery delivery = new Delivery(this, appContext,
                    mshConnection.getMessageServiceHandlerConfig().
                    getMessageListener(), ebxmlMessage);
                tx.addThread(delivery);
                logger.debug("Status Response message is received");
            }
            else if (isPing) {
                logger.debug("Ping message is received");
                response = generatePongMessage(ebxmlMessage);
                messageServer.store(response, appContext,
                    MessageServer.STATE_SENT, true, tx);
                logger.debug("Pong message is sent back");
            }
            else if (isPong) {
                String pingMessageId = ebxmlMessage.getMessageHeader().
                    getRefToMessageId();
                appContext = messageServer.getApplicationContext
                    (pingMessageId, tx);
                mshConnection = (MessageServiceHandlerConnection)
                    mshConnectionTable.get(appContext);
                messageServer.store(ebxmlMessage, appContext,
                    MessageServer.STATE_RECEIVED, false, tx);
                Delivery delivery = new Delivery(this, appContext,
                    mshConnection.getMessageServiceHandlerConfig().
                    getMessageListener(), ebxmlMessage);
                tx.addThread(delivery);
                logger.info("Pong message is received");
            }
            else {
                tx.lock(messageId);
                final boolean hasReceived = messageServer.
                    hasReceived(ebxmlMessage, appContext, tx);
                if (!ebxmlMessage.getDuplicateElimination() || !hasReceived) {
                    logger.debug("message doesn't have AckReq");
                    if (!hasReceived) {
                        logger.debug(
                            "message has not been received previously");
                        messageServer.store(ebxmlMessage, appContext,
                            MessageServer.STATE_RECEIVED, false, tx);
                        status = "Received firstly - Deliver to Application";
                    }
                    else {
                        status = "Received before - Deliver to Application";
                        messageServer.setDeliveryStatus
                            (ebxmlMessage.getMessageId(), false, tx);
                    }
                    Delivery delivery = new Delivery(this, appContext,
                        mshConnection.getMessageServiceHandlerConfig().
                        getMessageListener(), ebxmlMessage);
                    tx.addThread(delivery);
                }
                else {
                    logger.debug("will not process message");
                    status = "Received before - Not deliver to Application";
                }
            }

            try {
                requestProperty.put(DbTableManager.ATTRIBUTE_STATUS, status);
                messageServer.logReceivedMessage
                    (ebxmlMessage, requestProperty, tx);
            }
            catch (MessageServerException mse) {}
        }
        catch (MessageServiceHandlerException e) {
            throwable = e;
        }
        catch (Throwable e) {
            throwable = e;
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        }
        finally {
            if (throwable != null) {
                throw new MessageServiceHandlerException
                    (throwable.getMessage());
            }
            logger.debug("<= MessageServiceHandler.dispatchMessage");
        }
        return response;
    }
    
    /**
     * Get the register MessageServiceHandlerConfig for the specify application
     * context. If the specify MessageServiceHandlerConfig is not registered,
     * null will be returned.
     * @param appContext the application context.
     * @return The registered MessageServiceHandlerConfig for the specify
     * application context.
     */
    MessageServiceHandlerConfig getMessageServiceHandlerConfig(
        ApplicationContext appContext) {
        ApplicationContext targetAppContext = getApplicationContext(
            appContext.getCpaId(), appContext.getConversationId(),
            appContext.getService(), appContext.getAction());
        if (targetAppContext == null) {
            return null;
        } else {
            MessageServiceHandlerConnection connection
                = (MessageServiceHandlerConnection)
                mshConnectionTable.get(targetAppContext);
            return connection.getMessageServiceHandlerConfig();
        }
    }
}
