/*
 *  Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 *  University of Hong Kong (HKU). All Rights Reserved.
 *
 *  This software is licensed under the Academic Free License Version 1.0
 *
 *  Academic Free License
 *  Version 1.0
 *
 *  This Academic Free License applies to any software and associated
 *  documentation (the "Software") whose owner (the "Licensor") has placed the
 *  statement "Licensed under the Academic Free License Version 1.0" immediately
 *  after the copyright notice that applies to the Software.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of the Software (1) to use, copy, modify, merge, publish, perform,
 *  distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, and (2) under patent
 *  claims owned or controlled by the Licensor that are embodied in the Software
 *  as furnished by the Licensor, to make, use, sell and offer for sale the
 *  Software and derivative works thereof, subject to the following conditions:
 *
 *  - Redistributions of the Software in source code form must retain all
 *  copyright notices in the Software as furnished by the Licensor, this list
 *  of conditions, and the following disclaimers.
 *  - Redistributions of the Software in executable form must reproduce all
 *  copyright notices in the Software as furnished by the Licensor, this list
 *  of conditions, and the following disclaimers in the documentation and/or
 *  other materials provided with the distribution.
 *  - Neither the names of Licensor, nor the names of any contributors to the
 *  Software, nor any of their trademarks or service marks, may be used to
 *  endorse or promote products derived from this Software without express
 *  prior written permission of the Licensor.
 *
 *  DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS
 *  OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER
 *  A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY
 *  PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS
 *  AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 *  LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES
 *  OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 *
 *  This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved.
 *  Permission is hereby granted to copy and distribute this license without
 *  modification. This license may not be modified without the express written
 *  permission of its copyright owner.
 */
/*
 *  =====
 *
 *  $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/Request.java,v 1.78 2004/04/02 06:02:42 bobpykoon Exp $
 *
 *  Code authored by:
 *
 *  cyng [2002-03-21]
 *
 *  Code reviewed by:
 *
 *  username [YYYY-MM-DD]
 *
 *  Remarks:
 *
 *  =====
 */
package hk.hku.cecid.phoenix.message.handler;

import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.packaging.AttachmentDataSource;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.MessageOrder;
import hk.hku.cecid.phoenix.message.packaging.PayloadContainer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 * <code>Request</code> is an API of which an Application makes use to
 * request service from the <code>MessageServiceHandler</code>.
 *
 * @author cyng
 * @version $Revision: 1.78 $
 */
public class Request {

    static Logger logger = Logger.getLogger(Request.class);

    /**
     * Polls the Message Service Handler and checks for any new message.
     *
     * @author Bob Koon
     */
    private final static class Monitor extends Thread {

        /**
         * Default time interval between successive polls configured by
         * <code>Property</code>.
         */
        private static long MONITOR_INTERVAL = 0;

        /**
         * Flag indicating if the class has been configured.
         */
        protected static boolean isConfigured = false;

        /**
         * Configure the class if and only if it has not been configured.
         *
         *
         * @param prop <code>Property</code> object.
         * @exception InitializationException Description of the Exception
         */
        static void configure(Property prop) throws InitializationException {

            if (isConfigured) {
                return;
            }

            /*
             *  Load monitor interval property from configuration file.
             */
            try {
                final String monitorInterval =
                        prop.get(Constants.PROPERTY_REQUEST_MONITOR_INTERVAL);
                if (monitorInterval == null) {
                    MONITOR_INTERVAL = Constants.REQUEST_MAX_WAIT_INTERVAL;
                } else {
                    MONITOR_INTERVAL = Long.valueOf(monitorInterval).
                            longValue();
                }
            } catch (NumberFormatException e) {
                throw new InitializationException
                        (Constants.PROPERTY_REQUEST_MONITOR_INTERVAL +
                        " is not an integer.");
            }

            HttpSender.configure(prop);
            MailSender.configure(prop);

            isConfigured = true;
        }

        /**
         * Time interval between successive polls.
         */
        private long waitInterval = MONITOR_INTERVAL;

        /**
         * Custom specified time interval between successive polls.
         */
        private long customInterval = MONITOR_INTERVAL;

        /**
         * Application context of this application.
         */
        private ApplicationContext appContext = null;

        /**
         * Message listener of this application.
         */
        private final MessageListener messageListener;

        private boolean shutDown = false;

        private boolean isEnabled = true;

        private String exceptionMessage = null;

        /**
         * Initializes <code>Monitor</code> object.
         *
         *
         * @param messageListener Set message listener so that the application
         *                          can be notified if a message is received.
         */
        Monitor(MessageListener messageListener) {
            this.messageListener = messageListener;
        }

        /**
         * Set application context of the application.
         *
         *
         * @param appContext Application context of the application.
         * @throws RequestException
         */
        void setApplicationContext(ApplicationContext appContext)
                 throws RequestException {
            if (this.appContext != null) {
                throw new RequestException("ApplicationContext in "
                         + Monitor.class.getName() + " has been already set!");
            }
            this.appContext = appContext;
        }

        /**
         * Polls the message service handler and checks if there is any new
         * incoming message by sending commands to MSH at intervals specified.
         * If an incoming message is received, it notifies client application
         * by calling its {@link MessageListener#onMessage} handler.
         */
        public void run() {
            if (appContext == null) {
                exceptionMessage = Monitor.class.getName() + " must not be "
                         + "started as ApplicationContext is not set yet!";
                return;
            }

            while (!shutDown) {
                while (!isEnabled) {
                    try {
                        idle(0);
                    } catch (InterruptedException e) {}
                }

                final Command command =
                        new Command(CommandConstants.GET_MESSAGE, appContext);
                long startTime = System.currentTimeMillis();
                try {
                    final HttpURLConnection connection = sendCommand(command);
                    final int responseCode = connection.getResponseCode();
                    final String responseMessage =
                            connection.getResponseMessage();
                    if (responseCode != HttpURLConnection.HTTP_OK
                             && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                        throw new RequestException("Fail to get EbxmlMessage "
                                 + "from MessageServiceHandler:\n  HTTP response "
                                 + "code = " + String.valueOf(responseCode)
                                 + "\n  HTTP response message = " + responseMessage);
                    }
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        waitInterval = customInterval;
                        /*
                         *  if (waitInterval < MAX_WAIT_INTERVAL) {
                         *  waitInterval *= 2;
                         *  }
                         */
                    } else if (responseCode == HttpURLConnection.HTTP_OK) {
                        final MimeHeaders headers = new MimeHeaders();
                        int i = 1;
                        String key = null;
                        while ((key = connection.getHeaderFieldKey(i)) != null) {
                            headers.addHeader(key,
                                    connection.getHeaderField(i++));
                        }
                        final SOAPMessage soapMessage =
                                messageFactory.createMessage(headers,
                                connection.getInputStream());
                        final EbxmlMessage ebxmlMessage =
                                new EbxmlMessage(soapMessage);
                        messageListener.onMessage(ebxmlMessage);
                        waitInterval = Constants.REQUEST_MIN_WAIT_INTERVAL;
                    }
                    connection.disconnect();

                    startTime = System.currentTimeMillis();
                    idle(waitInterval);
                } catch (Throwable t) {
                    exceptionMessage = t.getMessage();
                    if (!(t instanceof InterruptedException)) {
                        startTime = System.currentTimeMillis();
                    }
                    long endTime = System.currentTimeMillis();
                    while ((endTime - startTime) < waitInterval) {
                        try {
                            idle(waitInterval - (endTime - startTime));
                            break;
                        } catch (InterruptedException ie) {
                        }
                        endTime = System.currentTimeMillis();
                    }

                    waitInterval = customInterval;
                }
            }
        }

        /**
         * Description of the Method
         */
        synchronized void haltPoller() {
            isEnabled = false;
            this.notify();
        }

        /**
         * Description of the Method
         */
        synchronized void resumePoller() {
            isEnabled = true;
            this.notify();
        }

        /**
         * Gets the enabled attribute of the Monitor object
         *
         * @return The enabled value
         */
        boolean isEnabled() {
            return isEnabled;
        }

        /**
         * Shutdown the message monitor.
         */
        synchronized void shutDown() {
            shutDown = true;
            this.notify();
        }

        /**
         * Sets the monitorInterval attribute of the Monitor object
         *
         * @param interval The new monitorInterval value
         */
        synchronized void setMonitorInterval(long interval) {
            customInterval = (interval >= 0 ? interval : MONITOR_INTERVAL);
            this.notify();
        }

        /**
         * Get the last exception string
         *
         *
         * @return Message string of the last exception occurred.
         */
        String getExceptionMessage() {
            return exceptionMessage;
        }

        /**
         * Wait for a specified amount of time
         *
         *
         * @param interval Length of time to wait for in milliseconds.
         * @exception InterruptedException
         */
        private synchronized void idle(long interval)
                 throws InterruptedException {
            this.wait(interval);
        }
    }

    /**
     * Element <Retries> in CPP/A
     */
    public final static String RETRIES = "Retries";

    /**
     * Element <RetryInterval> in CPP/A
     */
    public final static String RETRY_INTERVAL = "RetryInterval";

    /**
     * Element <MessageOrderSemantics> in CPP/A
     */
    public final static String MESSAGE_ORDER_SEMANTICS =
            "MessageOrderSemantics";

    /**
     * Element <PersistDuration> in CPP/A
     */
    public final static String PERSIST_DURATION = "PersistDuration";

    /**
     * Attribute "syncReplyMode" in CPP/A
     */
    public final static String SYNC_REPLY_MODE = "syncReplyMode";

    /**
     * Attribute "ackRequested" in CPP/A
     */
    public final static String ACK_REQUESTED = "ackRequested";

    /**
     * Default number of retries if it has not been explicitly specified.
     */
    public final static int DEFAULT_RETRIES = 2;

    /**
     * Default retry interval if it has not been explicitly specified.
     */
    public final static String DEFAULT_RETRY_INTERVAL = "150000";

    /**
     * Default not to use message order semantics.
     */
    public final static boolean DEFAULT_MESSAGE_ORDER_SEMANTICS = false;

    /**
     * Default persist duration (-1 means persist forever)
     */
    public final static String DEFAULT_PERSIST_DURATION = "-1";

    /**
     * syncReply constants used in <code>Request</code> constructor.
     */
    public final static int SYNC_REPLY_MODE_NONE =
            Constants.SYNC_REPLY_MODE_NONE;

    /**
     * Description of the Field
     */
    public final static int SYNC_REPLY_MODE_MSH_SIGNALS_ONLY =
            Constants.SYNC_REPLY_MODE_MSH_SIGNALS_ONLY;

    /**
     * Description of the Field
     */
    public final static int SYNC_REPLY_MODE_SIGNALS_ONLY =
            Constants.SYNC_REPLY_MODE_SIGNALS_ONLY;

    /**
     * Description of the Field
     */
    public final static int SYNC_REPLY_MODE_RESPONSE_ONLY =
            Constants.SYNC_REPLY_MODE_RESPONSE_ONLY;

    /**
     * Description of the Field
     */
    public final static int SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE =
            Constants.SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE;

    /*
     *  ackRequested constants used in <code>Request</code> constructor.
     */
    /**
     * Description of the Field
     */
    public final static int ACK_REQUESTED_PERMESSAGE =
            Constants.ACK_REQUESTED_PERMESSAGE;
    /**
     * Description of the Field
     */
    public final static int ACK_REQUESTED_ALWAYS =
            Constants.ACK_REQUESTED_ALWAYS;
    /**
     * Description of the Field
     */
    public final static int ACK_REQUESTED_NEVER =
            Constants.ACK_REQUESTED_NEVER;

    /**
     * Enable the Message Service Handler to send acknowledgement
     * response when requested.
     */
    public final static int ENABLE_ACKNOWLEDGMENT =
            CommandConstants.ENABLE_ACKNOWLEDGMENT;

    /**
     * Specify that the Message Service Handler should not send
     * acknowledgement response even if requested.
     */
    public final static int DISABLE_ACKNOWLEDGMENT =
            CommandConstants.DISABLE_ACKNOWLEDGMENT;

    /**
     * Specify that the Message Service Handler should ignore any
     * incoming acknowledgement responses.
     */
    public final static int IGNORE_ACKNOWLEDGMENT =
            CommandConstants.IGNORE_ACKNOWLEDGMENT;

    /**
     * Specify that the Message Service Handler should accept any
     * incoming acknowledgement responses.
     */
    public final static int ACCEPT_ACKNOWLEDGMENT =
            CommandConstants.ACCEPT_ACKNOWLEDGMENT;

    /**
     * Specify that the Message Service Handler should report environment
     * settings.
     */
    public final static int REPORT_ENVIRONMENT =
            CommandConstants.REPORT_ENVIRONMENT;

    /**
     * Check database connections.
     */
    public final static int CHECK_DATABASE = CommandConstants.CHECK_DATABASE;

    /**
     * Check message persistence.
     */
    public final static int CHECK_PERSISTENCE =
            CommandConstants.CHECK_PERSISTENCE;

    /**
     * Check internal state consistency.
     */
    public final static int CHECK_INTERNAL_STATES =
            CommandConstants.CHECK_INTERNAL_STATES;

    /**
     * Get current status of MSH.
     */
    public final static int QUERY_MSH_STATUS =
            CommandConstants.QUERY_MSH_STATUS;

    /**
     * Get database connection pool info.
     */
    public final static int QUERY_DB_CONN_POOL =
            CommandConstants.QUERY_DB_CONN_POOL;

    /**
     * Get number of records in DB.
     */
    public final static int QUERY_NUM_RECORDS_IN_DB =
            CommandConstants.QUERY_NUM_RECORDS_IN_DB;

    /**
     * Carry out message loopback test.
     */
    public final static int TEST_LOOPBACK = CommandConstants.TEST_LOOPBACK;

    /**
     * Reset database connection pool.
     */
    public final static int RESET_DB_CONNECTION_POOL =
            CommandConstants.RESET_DB_CONNECTION_POOL;

    /**
     * Suspend the MSH. Operation can be resumed using <code>resume()</code>
     * function.
     */
    public final static int HALT_SUSPEND = CommandConstants.HALT_SUSPEND;

    /**
     * Terminate the MSH. MSH must be restarted in the servlet container.
     */
    public final static int HALT_TERMINATE = CommandConstants.HALT_TERMINATE;

    /**
     * HTTP method to send Commands to Message Service Handler.
     */
    private final static String HTTP_METHOD = "POST";

    /**
     * File delimiter.
     */
    private final static String DELIMITER = ";";

    /**
     * Reference to ebXML message factory.
     */
    private static MessageFactory messageFactory;

    /**
     * Reference to XML transformer factory.
     */
    private static TransformerFactory transformerFactory;

    /**
     * URL of the current message handler.
     */
    private static URL mshUrl;

    /**
     * User name for authentication
     */
    private static String authUserName;

    private static int maxNumPayload;
    private static long maxPayloadSize;

    /**
     * Password for authentication
     */
    private static char[] authPassword;

    private static String messageListenerRepository = null;

    /**
     * Flag indicating if the class has been configured.
     */
    protected static boolean isConfigured = false;

    /**
     * Configure the class if and only if it has not been configured.
     *
     *
     * @param prop <code>Property</code> object.
     * @exception InitializationException Description of the Exception
     */
    static void configure(Property prop) throws InitializationException {

        if (isConfigured) {
            return;
        }

        try {
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new InitializationException
                    ("Default message factory cannot be instantiated.");
        }

        // Get MSH URL
        String url = prop.get
                (Constants.PROPERTY_REQUEST_MESSAGE_SERVICE_HANDLER_URL);

        Utility.configureClientLogger(prop, "hk.hku.cecid.phoenix.message");
        Utility.configureClientLogger(prop, "hk.hku.cecid.phoenix.pki");

        // for authentication
        authUserName = prop.get(Constants.PROPERTY_AUTHENTICATION_USERNAME);
        String password = prop.get(Constants.PROPERTY_AUTHENTICATION_PASSWORD);
        authPassword = (password != null ? password.toCharArray() : null);

        // payload limitation
        String strMaxNumPayload = prop.get(Constants.PROPERTY_MAX_NUM_PAYLOAD);
        try {
            if (strMaxNumPayload != null) {
                maxNumPayload = Integer.parseInt(strMaxNumPayload);
            } else {
                maxNumPayload = -1;
            }
        } catch (NumberFormatException e) {
            maxNumPayload = -1;
        }
        String strMaxPayloadSize =
                prop.get(Constants.PROPERTY_MAX_PAYLOAD_SIZE);
        try {
            if (strMaxPayloadSize != null) {
                maxPayloadSize = Long.parseLong(strMaxPayloadSize);
            } else {
                maxPayloadSize = -1;
            }
        } catch (NumberFormatException e) {
            maxPayloadSize = -1;
        }

        if (url == null) {
            throw new InitializationException
                    ("Cannot get MSH URL in "
                     + Constants.PROPERTY_REQUEST_MESSAGE_SERVICE_HANDLER_URL);
        } else {
            try {
                mshUrl = new URL(url);
            } catch (MalformedURLException e) {
                throw new InitializationException
                        ("Invalid MSH URL in "
                         + Constants.PROPERTY_REQUEST_MESSAGE_SERVICE_HANDLER_URL
                         + ": " + e.getMessage());
            }
        }

        // Message repository
        messageListenerRepository =
                prop.get(Constants.PROPERTY_REQUEST_LISTENER_REPOSITORY);
        if (messageListenerRepository == null) {
            messageListenerRepository = "";
        }

        // Transformer factory
        try {
            transformerFactory = TransformerFactory.newInstance();
        } catch (TransformerFactoryConfigurationError e) {
            throw new InitializationException
                    ("Cannot create default transformer factory: "
                     + e.getMessage());
        }

        Monitor.configure(prop);
        DirectoryManager.initObjects();

        isConfigured = true;
    }

    /**
     * Message monitor.
     */
    private Monitor monitor = null;

    /**
     * Application context representating this application.
     */
    private ApplicationContext appContext = null;

    /**
     * Message listener of this application.
     */
    private MessageListener messageListener = null;

    /**
     * URL of the target Message Service Handler
     */
    private URL toMshUrl = null;

    /**
     * Message transport method.
     */
    private final String transportType;

    /**
     * Number of retries in sending message.
     */
    private final int retries;

    /**
     * Retry interval between successive sending of message.
     */
    private final String retryInterval;

    /**
     * Whether sync reply mode is used.
     */
    private final int syncReply;

    /**
     * Whether message order semantics is used.
     */
    private final boolean messageOrder;

    /**
     * Persist duration
     */
    private final String persistDuration;

    /**
     * ackRequested
     */
    private final int ackRequested;

    /**
     * Keystore alias.
     */
    private String alias = null;

    /**
     * Keystore password.
     */
    private char[] password = null;

    /**
     * Keystore file name.
     */
    private String keyStoreFile = null;

    /**
     * Signing algorithm/
     */
    private String keyAlg = null;

    /**
     * Initializes request object.
     *
     *
     * @param mshConfig Message Service Handler configuration object.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(MessageServiceHandlerConfig mshConfig)
             throws RequestException {
        this(mshConfig.getApplicationContext(), mshConfig.getToMshUrl(),
                mshConfig.getMessageListener(), mshConfig.getTransportType(),
                mshConfig.getRetries(), mshConfig.getRetryInterval(),
                mshConfig.getSyncReply(), mshConfig.isMessageOrdered(),
                mshConfig.getPersistDuration());
    }

    /**
     * Initializes request object.
     *
     *
     * @param appContext Application context of the application.
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(ApplicationContext appContext, URL toMshUrl,
            MessageListener messageListener, String transportType)
             throws RequestException {
        this(appContext, toMshUrl, messageListener, transportType,
                DEFAULT_RETRIES, DEFAULT_RETRY_INTERVAL, SYNC_REPLY_MODE_NONE,
                DEFAULT_MESSAGE_ORDER_SEMANTICS, DEFAULT_PERSIST_DURATION);
    }

    /**
     * Initializes request object.
     *
     *
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(URL toMshUrl, MessageListener messageListener,
            String transportType)
             throws RequestException {
        this(null, toMshUrl, messageListener, transportType, DEFAULT_RETRIES,
                DEFAULT_RETRY_INTERVAL, SYNC_REPLY_MODE_NONE,
                DEFAULT_MESSAGE_ORDER_SEMANTICS, DEFAULT_PERSIST_DURATION);
    }

    /**
     * Initializes request object.
     *
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @param retries Number of retries when sending messages
     * @param retryInterval Time interval between successive retries.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(URL toMshUrl, MessageListener messageListener,
            String transportType, int retries, String retryInterval)
             throws RequestException {
        this(null, toMshUrl, messageListener, transportType, retries,
                retryInterval, SYNC_REPLY_MODE_NONE,
                DEFAULT_MESSAGE_ORDER_SEMANTICS, DEFAULT_PERSIST_DURATION);
    }

    /**
     * Initializes request object.
     *
     * @param appContext Application context of the application.
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @param retries Number of retries when sending messages
     * @param retryInterval Time interval between successive retries.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(ApplicationContext appContext, URL toMshUrl,
            MessageListener messageListener, String transportType,
            int retries, String retryInterval)
             throws RequestException {
        this(appContext, toMshUrl, messageListener, transportType, retries,
                retryInterval, SYNC_REPLY_MODE_NONE,
                DEFAULT_MESSAGE_ORDER_SEMANTICS, DEFAULT_PERSIST_DURATION);
    }

    /**
     * Initializes request object.
     *
     * @param appContext Application context of the application.
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @param retries Number of retries when sending messages
     * @param retryInterval Time interval between successive retries.
     * @param syncReply Whether sync reply is used.
     * @param messageOrder Whether message order is guaranteed.
     * @param persistDuration Persist duration.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(ApplicationContext appContext, URL toMshUrl,
            MessageListener messageListener, String transportType,
            int retries, String retryInterval, int syncReply,
            boolean messageOrder, String persistDuration)
             throws RequestException {
        if (!isConfigured) {
            try {
                Property prop = Property.load
                        (Constants.MSH_CLIENT_PROPERTY_FILE);
                configure(prop);
            } catch (IOException e) {
                throw new RequestException("Cannot open property file " +
                        Constants.MSH_CLIENT_PROPERTY_FILE);
            } catch (InitializationException e) {
                throw new RequestException
                        ("Cannot initialize request object: " + e.getMessage());
            }
        }

        this.appContext = appContext;
        this.toMshUrl = toMshUrl;
        this.transportType = transportType;
        this.retries = retries;
        try {
            Long.parseLong(retryInterval);
        } catch (NumberFormatException e) {
            throw new RequestException(RETRY_INTERVAL + " = " + retryInterval +
                    " is not valid: " + e.getMessage());
        }
        this.retryInterval = retryInterval;
        if (syncReply != SYNC_REPLY_MODE_NONE &&
                syncReply != SYNC_REPLY_MODE_SIGNALS_ONLY &&
                syncReply != SYNC_REPLY_MODE_MSH_SIGNALS_ONLY &&
                syncReply != SYNC_REPLY_MODE_RESPONSE_ONLY &&
                syncReply != SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE) {
            throw new RequestException(SYNC_REPLY_MODE + " should be" +
                    " set to Request.SYNC_REPLY_MODE_*");
        }
        this.syncReply = syncReply;
        this.messageOrder = messageOrder;
        this.ackRequested = ACK_REQUESTED_PERMESSAGE;
        try {
            Long.parseLong(persistDuration);
        } catch (NumberFormatException e) {
            throw new RequestException(PERSIST_DURATION + " = " +
                    persistDuration + " is not valid: " + e.getMessage());
        }
        this.persistDuration = persistDuration;

        if (messageListener != null) {
            final URL clientUrl = messageListener.getClientUrl();
            String protocol = (clientUrl != null ? clientUrl.getProtocol() :
                    null);

            if (clientUrl == null) {
                if (!(messageListener instanceof ClientMessageListenerImpl) ||
                        ((ClientMessageListenerImpl) messageListener).
                        startMonitor()) {
                    monitor = new Monitor(messageListener);
                    monitor.setDaemon(true);
                } else {
                    try {
                        this.messageListener = new ClientMessageListenerImpl
                                (new URL(MessageListener.PROTOCOL_FILE +
                                MessageListener.PROTOCOL_SEPARATOR +
                                messageListenerRepository));
                    } catch (MalformedURLException e) {
                        throw new RequestException(e.getMessage());
                    }
                }
            } else if (protocol.equals(MessageListener.PROTOCOL_FILE) &&
                    messageListener instanceof ClientMessageListenerImpl) {
                monitor = new Monitor(messageListener);
                monitor.setDaemon(true);
                this.messageListener = messageListener;
            } else {
                this.messageListener = messageListener;
            }
        }

        if (appContext != null) {
            register();
        }
    }

    /**
     * Initializes request object.
     *
     * @param appContext Application context of the application.
     * @param toMshUrl URL of the recipient MSH.
     * @param messageListener Message listener of the application.
     * @param transportType Message transport method.
     * @param parameters Other initialization parameters.
     * @exception RequestException Description of the Exception
     * @throws RequestException
     */
    public Request(ApplicationContext appContext, URL toMshUrl,
            MessageListener messageListener, String transportType,
            HashMap parameters) throws RequestException {
        if (!isConfigured) {
            try {
                Property prop = Property.load
                        (Constants.MSH_CLIENT_PROPERTY_FILE);
                configure(prop);
            } catch (IOException e) {
                throw new RequestException("Cannot open property file " +
                        Constants.MSH_CLIENT_PROPERTY_FILE);
            } catch (InitializationException e) {
                throw new RequestException
                        ("Cannot initialize request object: " + e.getMessage());
            }
        }

        this.appContext = appContext;
        this.toMshUrl = toMshUrl;
        this.transportType = transportType;

        String buffer = (String) parameters.get(RETRIES);
        if (buffer != null) {
            try {
                this.retries = Integer.parseInt(buffer);
            } catch (NumberFormatException e) {
                throw new RequestException(RETRIES + " = " + buffer +
                        " is not valid: " + e.getMessage());
            }
        } else {
            this.retries = DEFAULT_RETRIES;
        }
        buffer = (String) parameters.get(RETRY_INTERVAL);
        if (buffer != null) {
            try {
                Long.parseLong(buffer);
                this.retryInterval = buffer;
            } catch (NumberFormatException e) {
                throw new RequestException(RETRY_INTERVAL + " = " + buffer +
                        " is not valid: " + e.getMessage());
            }
        } else {
            this.retryInterval = DEFAULT_RETRY_INTERVAL;
        }
        buffer = (String) parameters.get(SYNC_REPLY_MODE);
        if (buffer != null) {
            try {
                int s = Integer.parseInt(buffer);
                if (s != SYNC_REPLY_MODE_NONE &&
                        s != SYNC_REPLY_MODE_SIGNALS_ONLY &&
                        s != SYNC_REPLY_MODE_MSH_SIGNALS_ONLY &&
                        s != SYNC_REPLY_MODE_RESPONSE_ONLY &&
                        s != SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE) {
                    throw new RequestException(SYNC_REPLY_MODE + " should be" +
                            " set to Request.SYNC_REPLY_MODE_*");
                }
                this.syncReply = s;
            } catch (Exception e) {
                throw new RequestException(SYNC_REPLY_MODE + " = " + buffer +
                        " is not valid: " + e.getMessage());
            }
        } else {
            this.syncReply = SYNC_REPLY_MODE_NONE;
        }
        buffer = (String) parameters.get(MESSAGE_ORDER_SEMANTICS);
        if (buffer != null) {
            this.messageOrder = Boolean.valueOf(buffer).booleanValue();
        } else {
            this.messageOrder = DEFAULT_MESSAGE_ORDER_SEMANTICS;
        }
        buffer = (String) parameters.get(PERSIST_DURATION);
        if (buffer != null) {
            try {
                Long.parseLong(buffer);
                this.persistDuration = buffer;
            } catch (NumberFormatException e) {
                throw new RequestException(PERSIST_DURATION + " = " + buffer +
                        " is not valid: " + e.getMessage());
            }
        } else {
            this.persistDuration = DEFAULT_PERSIST_DURATION;
        }
        buffer = (String) parameters.get(ACK_REQUESTED);
        if (buffer != null) {
            try {
                int s = Integer.parseInt(buffer);
                if (s != ACK_REQUESTED_PERMESSAGE &&
                        s != ACK_REQUESTED_ALWAYS &&
                        s != ACK_REQUESTED_NEVER) {
                    throw new RequestException(ACK_REQUESTED + " should be" +
                            " set to Request.ACK_REQUESTED_*");
                }
                this.ackRequested = s;
            } catch (Exception e) {
                throw new RequestException(SYNC_REPLY_MODE + " = " + buffer +
                        " is not valid: " + e.getMessage());
            }
        } else {
            this.ackRequested = ACK_REQUESTED_PERMESSAGE;
        }

        if (messageListener != null) {
            final URL clientUrl = messageListener.getClientUrl();
            String protocol = (clientUrl != null ? clientUrl.getProtocol() :
                    null);

            if (clientUrl == null) {
                if (!(messageListener instanceof ClientMessageListenerImpl) ||
                        ((ClientMessageListenerImpl) messageListener).
                        startMonitor()) {
                    monitor = new Monitor(messageListener);
                    monitor.setDaemon(true);
                } else {
                    try {
                        this.messageListener = new ClientMessageListenerImpl
                                (new URL(MessageListener.PROTOCOL_FILE +
                                MessageListener.PROTOCOL_SEPARATOR +
                                messageListenerRepository));
                    } catch (MalformedURLException e) {
                        throw new RequestException(e.getMessage());
                    }
                }
            } else if (protocol.equals(MessageListener.PROTOCOL_FILE) &&
                    messageListener instanceof ClientMessageListenerImpl) {
                monitor = new Monitor(messageListener);
                monitor.setDaemon(true);
                this.messageListener = messageListener;
            } else {
                this.messageListener = messageListener;
            }
        }

        if (appContext != null) {
            register();
        }
    }

    /**
     * Send message to the recipient Message Service Handler.
     *
     *
     * @param ebxmlMessage Message to be sent.
     * @throws RequestException
     */
    public void send(EbxmlMessage ebxmlMessage) throws RequestException {
        /*
         *  if (toMshUrl == null) {
         *  try {
         *  final String url = ((MessageHeader.PartyId) ebxmlMessage.
         *  getToPartyIds().next()).getId();
         *  toMshUrl = new URL(url);
         *  }
         *  catch (MalformedURLException e) {
         *  throw new RequestException(e.getMessage());
         *  }
         *  }
         */
        checkPayload(ebxmlMessage);

        if (syncReply == SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE &&
                !ebxmlMessage.getSyncReply()) {
            try {
                ebxmlMessage.addSyncReply();
            } catch (SOAPException e) {
                throw new RequestException(e.getMessage());
            }
        }

        if (ebxmlMessage.getMessageOrder() != null) {
            throw new RequestException("EbXML messages containing MessageOrder"
                     + " element must be sent using sendReliably() method.");
        }

        if (appContext == null) {
            appContext = new ApplicationContext(ebxmlMessage.getCpaId(),
                    ebxmlMessage.getConversationId(), ebxmlMessage.getService(),
                    ebxmlMessage.getAction());
            register();
        }

        if (alias != null && password != null && keyStoreFile != null) {
            try {
                ebxmlMessage.sign(alias, password, keyStoreFile, keyAlg);
            } catch (Exception e) {
                throw new RequestException(e.getMessage());
            }
        }
        /**
         * rollback to not use send without registration.
         */
        //sendMessage(toMshUrl, retries, retryInterval, ebxmlMessage);
        sendMessage(appContext, ebxmlMessage);
    }

    /**
     * Gets the receivedMessageIds attribute of the Request object
     *
     * @return The receivedMessageIds value
     * @exception RequestException Description of the Exception
     */
    public String[] getReceivedMessageIds() throws RequestException {
        Command command = new Command
                (CommandConstants.GET_MESSAGE_ID, appContext);
        HttpURLConnection connection = sendCommand(command);
        int responseCode;
        String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                ObjectInputStream ois = new ObjectInputStream
                        (connection.getInputStream());
                ArrayList results = (ArrayList) ois.readObject();
                String[] messageIds = new String[results.size()];
                for (int i = 0; i < results.size(); i++) {
                    messageIds[i] = (String) results.get(i);
                }
                return messageIds;
            } catch (Exception e) {
                throw new RequestException(e.getMessage());
            }
        } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        } else {
            throw new RequestException("\nFail to receive MessageId's from " +
                    "MessageServiceHandler:\n  HTTP response code = " +
                    String.valueOf(responseCode) + "\n  HTTP response message = " +
                    responseMessage);
        }
    }

    /**
     * Description of the Method
     *
     * @param messageId Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public EbxmlMessage receive(String messageId) throws RequestException {
        Command command = new Command
                (CommandConstants.GET_MESSAGE_BY_ID, messageId);
        HttpURLConnection connection = sendCommand(command);
        int responseCode;
        String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                MimeHeaders headers = new MimeHeaders();
                int i = 1;
                String key = null;
                while ((key = connection.getHeaderFieldKey(i)) != null) {
                    headers.addHeader(key, connection.getHeaderField(i++));
                }
                SOAPMessage soapMessage = messageFactory.createMessage
                        (headers, connection.getInputStream());
                return new EbxmlMessage(soapMessage);
            } catch (Exception e) {
                throw new RequestException(e.getMessage());
            }
        } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        } else {
            throw new RequestException("\nFail to receive EbxmlMessage from " +
                    "MessageServiceHandler:\n  HTTP response code = " +
                    String.valueOf(responseCode) + "\n  HTTP response message = " +
                    responseMessage);
        }
    }

    /**
     * Send diagnostic command to the Message Service Handler and return
     * result.
     *
     * @param message Message Service Handler diagnostic commands.
     * @return Description of the Return Value
     * @throws RequestException
     */
    public String sendDiagnosticCommand(int message) throws RequestException {
        Command command = null;
        if (message == CommandConstants.TEST_LOOPBACK) {
            final MessageServiceHandlerConfig mshConfig;
            try {
                mshConfig = new MessageServiceHandlerConfig(null,
                        mshUrl, messageListener, transportType, retries,
                        retryInterval, syncReply, messageOrder, persistDuration,
                        ackRequested, true);
            } catch (MessageServiceHandlerException mshe) {
                throw new RequestException(mshe.getMessage());
            }
            command = new Command(message, mshConfig);
        } else {
            command = new Command(message, null);
        }
        final HttpURLConnection connection = sendCommand(command);
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }

        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            // do nothing
            return null;
        } else if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                final InputStream inputStream = connection.getInputStream();
                final byte[] buffer = new byte[4096];
                String result = new String();

                for (int c = inputStream.read(buffer); c != -1;
                        c = inputStream.read(buffer)) {
                    result += new String(buffer, 0, c);
                }

                return result;
            } catch (IOException ioe) {
                throw new RequestException(ioe.getMessage());
            }
        } else {
            throw new RequestException("\nFail to send "
                     + "diagnostic command to MessageServiceHandler: invalid "
                     + "response code. \n"
                     + "  HTTP response code = " + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }

    }

    /**
     * Set the properties of the Message Service Handler.
     *
     *
     * @param message Message Service Handler commands.
     * @throws RequestException
     * @deprecated Use sendDiagnosticCommand() instead.
     */
    public void setSpecialFunction(int message) throws RequestException {
        sendDiagnosticCommand(message);
    }

    /**
     * Send ebXML message through reliable messaging facility.
     *
     *
     * @param ebxmlMessage Message to be sent.
     * @param signed true if the acknowledgment response
     *                              should be signed; false otherwise.
     * @throws RequestException
     */
    public void sendReliably(EbxmlMessage ebxmlMessage, boolean signed)
             throws RequestException {
        /*
         *  if (toMshUrl == null) {
         *  try {
         *  final String url = ((MessageHeader.PartyId) ebxmlMessage.
         *  getToPartyIds().next()).getId();
         *  toMshUrl = new URL(url);
         *  }
         *  catch (MalformedURLException e) {
         *  throw new RequestException(e.getMessage());
         *  }
         *  }
         */
        if (appContext == null) {
            appContext = new ApplicationContext(ebxmlMessage.getCpaId(),
                    ebxmlMessage.getConversationId(), ebxmlMessage.getService(),
                    ebxmlMessage.getAction());
            register();
        }

        checkPayload(ebxmlMessage);

        if (ebxmlMessage.getAckRequested() == null) {
            try {
                ebxmlMessage.addAckRequested(signed);
            } catch (SOAPException e) {
                throw new RequestException(e.getMessage());
            }
        }

        if (messageOrder && (ebxmlMessage.getMessageOrder() == null)) {
            int seqNo = getSequenceNumber(
                    ebxmlMessage.getConversationId(), MessageOrder.STATUS_CONTINUE);
            try {
                ebxmlMessage.addMessageOrder(
                        MessageOrder.STATUS_CONTINUE, seqNo);
            } catch (SOAPException e) {
                throw new RequestException(e.getMessage());
            }
        }

        if (syncReply == SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE &&
                !ebxmlMessage.getSyncReply()) {
            try {
                ebxmlMessage.addSyncReply();
            } catch (SOAPException e) {
                throw new RequestException(e.getMessage());
            }
        }

        if (ebxmlMessage.getMessageOrder() != null) {
            if (ebxmlMessage.getDuplicateElimination() == false) {
                throw new RequestException("Messages containing MessageOrder "
                         + "element must be sent with duplicate elimination "
                         + "enabled.");
            }
            if (ebxmlMessage.getSyncReply()) {
                throw new RequestException("SyncReply element must not "
                         + "coexist with MessageOrder element.");
            }
        }

        if (alias != null && password != null && keyStoreFile != null) {
            try {
                ebxmlMessage.sign(alias, password, keyStoreFile, keyAlg);
            } catch (Exception e) {
                throw new RequestException(e.getMessage());
            }
        }
        //sendMessage(toMshUrl, retries, retryInterval, ebxmlMessage);
        sendMessage(appContext, ebxmlMessage);
    }

    /**
     * Set keystore properties.
     *
     *
     * @param alias Alias of the keystore.
     * @param password Keystore password.
     * @param keyStoreFile Keystore file.
     */
    public void setSign(String alias, char[] password, String keyStoreFile) {
        setSign(alias, password, keyStoreFile, null);
    }

    /**
     * Set keystore properties.
     *
     *
     * @param alias Alias of the keystore.
     * @param password Keystore password.
     * @param keyStoreFile Keystore file.
     * @param keyAlg Key algorithm.
     */
    public void setSign(String alias, char[] password, String keyStoreFile,
            String keyAlg) {
        this.alias = alias;
        this.password = password;
        this.keyStoreFile = keyStoreFile;
        this.keyAlg = keyAlg;
    }

    /**
     * Register an application to the message service handler and starts
     * the message monitor.
     *
     *
     * @throws RequestException
     */
    private void register() throws RequestException {
        if (messageListener == null) {
            messageListener = getDefaultMessageListener(appContext);
            if (monitor == null) {
                monitor = new Monitor(messageListener);
                monitor.setDaemon(true);
            }
        }

        final MessageServiceHandlerConfig mshConfig;
        try {
            mshConfig = new MessageServiceHandlerConfig(appContext,
                    toMshUrl, messageListener, transportType, retries,
                    retryInterval, syncReply, messageOrder, persistDuration,
                    ackRequested, true);
        } catch (MessageServiceHandlerException mshe) {
            throw new RequestException(mshe.getMessage());
        }
        sendMessageServiceHandlerConfig(mshConfig);

        if (monitor != null) {
            monitor.setApplicationContext(appContext);
            monitor.start();
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public boolean unregister() throws RequestException {
        final MessageServiceHandlerConfig mshConfig;
        if (appContext == null) {
            if (monitor != null) {
                monitor.shutDown();
            }
            return true;
        }
        try {
            mshConfig = new MessageServiceHandlerConfig(appContext,
                    toMshUrl, messageListener, transportType, retries,
                    retryInterval, syncReply, messageOrder, persistDuration,
                    ackRequested, false);
        } catch (MessageServiceHandlerException mshe) {
            throw new RequestException(mshe.getMessage());
        }
        boolean result = sendMessageServiceHandlerConfig(mshConfig);
        if (result == true && monitor != null) {
            monitor.shutDown();
        }
        return result;
    }

    /**
     * Description of the Method
     *
     * @param enable Description of the Parameter
     * @exception RequestException Description of the Exception
     */
    public void enablePoller(boolean enable) throws RequestException {
        if (monitor != null && enable != monitor.isEnabled()) {
            if (enable) {
                monitor.resumePoller();
            } else {
                monitor.haltPoller();
            }
        }
    }

    /**
     * Gets the pollerEnabled attribute of the Request object
     *
     * @return The pollerEnabled value
     * @exception RequestException Description of the Exception
     */
    public boolean isPollerEnabled() throws RequestException {
        return monitor.isEnabled();
    }

    /**
     * Set custom monitor interval which overrides the default setting
     * configured via property file.
     *
     * @param interval time interval in milliseconds. Any negative
     *                    number restores to the default setting.
     * @return boolean    true means the polling monitor exists and the
     *                    interval is successfully set. False otherwise.
     */
    public boolean setMonitorInterval(long interval) {
        if (monitor == null) {
            return false;
        }
        monitor.setMonitorInterval(interval);
        return true;
    }

    /**
     * Creates a default message listener if the application has not supplied
     * one.
     *
     *
     * @param appContext
     * @return Default <code>MessageListener</code> object.
     * @throws RequestException
     */
    private static MessageListener getDefaultMessageListener
            (ApplicationContext appContext) throws RequestException {

        String fileName = appContext.getCpaId() + "." +
                appContext.getConversationId() + "." + appContext.getService() + "."
                 + appContext.getAction();
        if (fileName.length() > 255 || fileName.indexOf(':') != -1 ||
                fileName.indexOf('\\') != -1 || fileName.indexOf('/') != -1 ||
                fileName.indexOf('*') != -1) {
            fileName = DirectoryManager.findUniqueFileName
                    (messageListenerRepository, appContext.getCpaId()
                     + appContext.getConversationId() + appContext.getService()
                     + appContext.getAction());
        }
        final String dirName = messageListenerRepository + File.separator
                 + fileName;
        final URL clientUrl;
        try {
            clientUrl = new URL(MessageListener.PROTOCOL_FILE
                     + MessageListener.PROTOCOL_SEPARATOR + dirName);
        } catch (MalformedURLException e) {
            throw new RequestException(e.getMessage());
        }
        return new ClientMessageListenerImpl(clientUrl);
    }


    /**
     * Send configuration information to the message service handler.
     *
     *
     * @param mshConfig
     * @return Description of the Return Value
     * @throws RequestException
     */
    private static boolean
            sendMessageServiceHandlerConfig(MessageServiceHandlerConfig mshConfig)
             throws RequestException {
        final Command command =
                new Command(CommandConstants.REGISTER_MSH_CONFIG, mshConfig);
        final HttpURLConnection connection = sendCommand(command);
        Map result = expectMapResponse(connection,
                "Cannot register application to MSH");
        String regResult = (String) result.get
                (Constants.QUERY_RESULT_REGISTRATION);

        return regResult.equals("1");
    }

    /**
     * Description of the Method
     *
     * @param command Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    private static Map sendQuery(Command command) throws RequestException {
        final HttpURLConnection connection = sendCommand(command);
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            throw new RequestException(e.getMessage());
        }
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            Map result = new TreeMap();
            String headerName;
            String headerValue;
            int i = 1;
            while ((headerName = connection.getHeaderFieldKey(i)) != null) {
                if (headerName.startsWith(Constants.QUERY_RESULT_PREFIX)) {
                    headerValue = connection.getHeaderField(i);
                    result.put(headerName, headerValue);
                }
                i++;
            }
            return result;
        } else {
            throw new RequestException("\n Failed to send query to MSH.\n"
                     + "  HTTP response code = " + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }

    /**
     * Send a serialized command object to the Message Service Handler
     * through an HTTP connection.
     *
     *
     * @param command Command object to be sent.
     * @return <code>java.sql.Connection</code> if it has been
     *         made successfully.
     * @throws RequestException
     */
    static HttpURLConnection sendCommand(Command command)
             throws RequestException {

        if (authUserName != null && authPassword != null) {
            command.setCredentials(authUserName, authPassword);
        }
        try {
            final HttpURLConnection connection = (HttpURLConnection)
                    mshUrl.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            HttpURLConnection.setFollowRedirects(true);
            connection.setRequestProperty(Constants.CONTENT_TYPE,
                    Constants.SERIALIZABLE_OBJECT);
            connection.setRequestMethod(HTTP_METHOD);
            final ObjectOutputStream out =
                    new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(command);
            out.flush();
            out.close();
            connection.connect();
            return connection;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RequestException(ioe.getMessage());
        }
    }
    /**
     * 
     */
    private static void sendMessage(ApplicationContext appContext,
        EbxmlMessage ebxmlMessage) throws RequestException {
        final Command command = new Command(appContext, ebxmlMessage);
        final HttpURLConnection connection = sendCommand(command);
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }
        if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RequestException("\nFail to send EbxmlMessage to "
                     + "MessageServiceHandler:\n  HTTP response code = "
                     + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }
    /**
     * Send message without registration.
     * Note that send message without registration will be disabled now.
     * Therefore this method should not be called. 
     *
     * @param appContext Application context of client application.
     * @param ebxmlMessage Description of the Parameter
     * @throws RequestException
     */
    private static void sendMessage(URL toMshUrl, int retries,
        String retryInterval, EbxmlMessage ebxmlMessage)
        throws RequestException {
        //final Command command = new Command(appContext, ebxmlMessage);
        final Command command = new Command(
            new SendCommandParameter(toMshUrl, retries, retryInterval),
            ebxmlMessage, CommandConstants.SEND_MESSAGE_AUTOMATIC_REGISTER);
        final HttpURLConnection connection = sendCommand(command);
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }
        if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RequestException("\nFail to send EbxmlMessage to "
                     + "MessageServiceHandler:\n  HTTP response code = "
                     + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }

    /**
     * Description of the Method
     *
     * @param ebxmlMessage Description of the Parameter
     * @exception RequestException Description of the Exception
     */
    private void checkPayload(EbxmlMessage ebxmlMessage)
             throws RequestException {

        Iterator i = ebxmlMessage.getPayloadContainers();
        int count = 0;
        while (i.hasNext()) {
            PayloadContainer pc = (PayloadContainer) i.next();
            if (maxPayloadSize >= 0) {
                if (pc.getDataHandler().getDataSource() instanceof
                        AttachmentDataSource) {
                    AttachmentDataSource source = (AttachmentDataSource)
                            pc.getDataHandler().getDataSource();
                    if (source.getLength() > maxPayloadSize) {
                        throw new RequestException("Message containing " +
                                "larger payloads than allowed.");
                    }
                }
            }
            count++;
        }
        if (maxNumPayload >= 0 && count > maxNumPayload) {
            throw new RequestException("Message containing more "
                     + "payloads than allowed.");
        }
    }

    // --------------------------------------------------------------------- //
    // System commands
    // --------------------------------------------------------------------- //

    /**
     * Sets the sendAcknowledgment attribute of the Request object
     *
     * @param sendAck The new sendAcknowledgment value
     * @exception RequestException Description of the Exception
     */
    public void setSendAcknowledgment(boolean sendAck)
             throws RequestException {
        final Command command;
        final HttpURLConnection connection;
        if (sendAck) {
            command = new Command(CommandConstants.ENABLE_ACKNOWLEDGMENT, null);
        } else {
            command =
                    new Command(CommandConstants.DISABLE_ACKNOWLEDGMENT, null);
        }
        connection = sendCommand(command);
        expectNoResponse(connection, "Cannot enable sending acknowledgment");
    }

    /**
     * Sets the receiveAcknowledgment attribute of the Request object
     *
     * @param receiveAck The new receiveAcknowledgment value
     * @exception RequestException Description of the Exception
     */
    public void setReceiveAcknowledgment(boolean receiveAck)
             throws RequestException {
        final Command command;
        final HttpURLConnection connection;
        if (receiveAck) {
            command = new Command(CommandConstants.ACCEPT_ACKNOWLEDGMENT, null);
        } else {
            command = new Command(CommandConstants.IGNORE_ACKNOWLEDGMENT, null);
        }
        connection = sendCommand(command);
        expectNoResponse(connection, "Cannot enable receiving acknowledgment");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String resetConnectionPool() throws RequestException {
        final Command command =
                new Command(CommandConstants.RESET_DB_CONNECTION_POOL, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot reset connection pool");
    }

    /**
     * Description of the Method
     *
     * @param level Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String haltMSH(int level) throws RequestException {
        final Command command;
        Document response = null;
        if (level == HALT_SUSPEND) {
            command = new Command(CommandConstants.HALT_SUSPEND, null);
        } else if (level == HALT_TERMINATE) {
            command = new Command(CommandConstants.HALT_TERMINATE, null);
        } else {
            try {
                response = Utility.getFailureResponse("Invalid halt level. "
                         + "Only Request.HALT_SUSPEND and Request.HALT_TERMINATE "
                         + "are accepted.");
            } catch (MessageServiceHandlerException e) {
                throw new RequestException(e.getMessage());
            }
            return domToString(response);
        }
        final HttpURLConnection connection = sendCommand(command);
        expectNoResponse(connection, "Cannot halt MSH");
        try {
            response = Utility.getSuccessResponse();
        } catch (MessageServiceHandlerException e) {
            throw new RequestException(e.getMessage());
        }
        return domToString(response);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String resumeMSH() throws RequestException {
        final Command command = new Command(CommandConstants.RESUME, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot resume MSH");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String backupMSH() throws RequestException {
        final Command command = new Command(CommandConstants.MSH_BACKUP, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot backup MSH data");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String restoreMSH() throws RequestException {
        final Command command = new Command(CommandConstants.MSH_RESTORE, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot restore MSH data");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String reportEnvironment() throws RequestException {
        final Command command =
                new Command(CommandConstants.REPORT_ENVIRONMENT, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot get environment settings");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String checkDatabase() throws RequestException {
        final Command command =
                new Command(CommandConstants.CHECK_DATABASE, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Check database failed");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String checkPersistence() throws RequestException {
        final Command command =
                new Command(CommandConstants.CHECK_PERSISTENCE, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Check persistence failed");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String checkInternalStates() throws RequestException {
        final Command command =
                new Command(CommandConstants.CHECK_INTERNAL_STATES, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Check internal states failed");
    }

    /**
     * Gets the isHalted attribute of the Request object
     *
     * @return The isHalted value
     * @exception RequestException Description of the Exception
     */
    public boolean getIsHalted() throws RequestException {
        final Command command =
                new Command(CommandConstants.QUERY_MSH_STATUS, null);
        final HttpURLConnection connection = sendCommand(command);
        Map result = expectMapResponse(connection, "Cannot get MSH Status");
        String s = (String) result.get(Constants.QUERY_RESULT_MSH_STATUS);
        if (s == null) {
            throw new RequestException(
                    "Cannot get MSH status; Invalid response from MSH");
        } else {
            boolean b = Boolean.valueOf(s).booleanValue();
            return b;
        }
    }

    /**
     * Gets the dBConnectionPoolInfo attribute of the Request object
     *
     * @return The dBConnectionPoolInfo value
     * @exception RequestException Description of the Exception
     */
    public String getDBConnectionPoolInfo() throws RequestException {
        final Command command =
                new Command(QUERY_DB_CONN_POOL, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(
                connection, "Get DB connection pool info failed");
    }

    /**
     * Gets the numRecordsInDB attribute of the Request object
     *
     * @return The numRecordsInDB value
     * @exception RequestException Description of the Exception
     */
    public String getNumRecordsInDB() throws RequestException {
        final Command command =
                new Command(QUERY_NUM_RECORDS_IN_DB, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Get num records failed");
    }

    /**
     * A unit test for JUnit
     *
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String testLoopback() throws RequestException {
        final Command command =
                new Command(CommandConstants.TEST_LOOPBACK, null);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Loopback test failed");
    }

    /**
     * Gets the sequenceNumber attribute of the Request object
     *
     * @param conversationId Description of the Parameter
     * @param status Description of the Parameter
     * @return The sequenceNumber value
     * @exception RequestException Description of the Exception
     */
    public int getSequenceNumber(String conversationId, int status)
             throws RequestException {
        final Command command;
        if (status == MessageOrder.STATUS_RESET) {
            command = new Command(
                    CommandConstants.QUERY_RESET_SEQUENCE_NUMBER, conversationId);
        } else {
            command = new Command
                    (CommandConstants.QUERY_SEQUENCE_NUMBER, conversationId);
        }
        final HttpURLConnection connection = sendCommand(command);
        Map result = expectMapResponse(connection,
                "Cannot get sequence number from MSH");
        String seq = (String) result.get(Constants.QUERY_RESULT_SEQUENCE_NUMBER);
        try {
            int seqInt = Integer.parseInt(seq);
            return seqInt;
        } catch (NumberFormatException e) {
            throw new RequestException("Cannot get sequence number: "
                     + e.getMessage());
        }
    }

    /**
     * Gets the messageStatus attribute of the Request object
     *
     * @param messageIdList Description of the Parameter
     * @return The messageStatus value
     * @exception RequestException Description of the Exception
     */
    public String getMessageStatus(ArrayList messageIdList)
             throws RequestException {
        HashMap map = new HashMap();
        for (int i = 0; i < messageIdList.size(); i++) {
            map.put(messageIdList.get(i), null);
        }
        final Command command =
                new Command(CommandConstants.QUERY_MESSAGE_STATUS, map);
        final HttpURLConnection connection = sendCommand(command);
        return expectXMLResponse(connection, "Cannot get message status");
    }

    /**
     * Gets the trustedRepository attribute of the Request object
     *
     * @return The trustedRepository value
     * @exception RequestException Description of the Exception
     */
    public String[] getTrustedRepository() throws RequestException {
        final Command command =
                new Command(CommandConstants.QUERY_TRUSTED_REPOSITORY, null);
        final HttpURLConnection connection = sendCommand(command);
        final Map result = expectMapResponse(connection,
                "Cannot get trusted repository location");

        String repository = (String) result.get(Constants.QUERY_RESULT_REPOSITORY);
        StringTokenizer tk = new StringTokenizer(repository, ";");
        String[] resultList = new String[tk.countTokens()];

        int i = 0;
        while (tk.hasMoreTokens()) {
            resultList[i++] = tk.nextToken();
        }

        return resultList;
    }

    /**
     * Gets the pendingMessages attribute of the Request object
     *
     * @return The pendingMessages value
     * @exception RequestException Description of the Exception
     */
    public String[] getPendingMessages() throws RequestException {
        final Command command =
                new Command(CommandConstants.QUERY_PENDING_MESSAGE, null);
        final HttpURLConnection connection = sendCommand(command);
        final String xml = expectXMLResponse
                (connection, "Cannot get pending message");

        try {
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                    factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(
                    new InputSource(new StringReader(xml)));
            final Element elem_root = document.getDocumentElement();
            final NodeList nodeList = elem_root.getElementsByTagName
                    (Constants.ELEMENT_MESSAGE);

            int length = nodeList.getLength();
            String[] messages = new String[length];
            for (int i = 0; i < length; i++) {
                NamedNodeMap map = nodeList.item(i).getAttributes();
                String msgId = map.getNamedItem
                        (Constants.ATTRIBUTE_MESSAGE_ID).getNodeValue();
                messages[i] = msgId;
            }
            return messages;
        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }
    }

    /**
     * Description of the Method
     *
     * @param messageIdList Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public Map deletePendingMessages(ArrayList messageIdList)
             throws RequestException {
        HashMap map = new HashMap();
        for (int i = 0; i < messageIdList.size(); i++) {
            map.put(messageIdList.get(i), null);
        }
        final Command command =
                new Command(CommandConstants.DELETE_PENDING_MESSAGE, map);
        final HttpURLConnection connection = sendCommand(command);

        final String xml = expectXMLResponse
                (connection, "Cannot get message deletion response");
        final Map resultMap = new HashMap();
        try {
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder =
                    factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(
                    new InputSource(new StringReader(xml)));
            final Element elem_root = document.getDocumentElement();
            final Element elem_success = (Element) elem_root.getElementsByTagName
                    (Constants.ELEMENT_SUCCEEDED).item(0);
            final Element elem_failed = (Element) elem_root.getElementsByTagName
                    (Constants.ELEMENT_FAILED).item(0);

            NodeList success_child = elem_success.getChildNodes();
            NodeList failed_child = elem_failed.getChildNodes();

            for (int i = 0; i < success_child.getLength(); i++) {
                NamedNodeMap nodeMap = success_child.item(i).getAttributes();
                String msgId = nodeMap.getNamedItem
                        (Constants.ATTRIBUTE_MESSAGE_ID).getNodeValue();
                resultMap.put(msgId, Constants.DELETE_SUCCESSFUL);
            }
            for (int i = 0; i < failed_child.getLength(); i++) {
                NamedNodeMap nodeMap = success_child.item(i).getAttributes();
                String msgId = nodeMap.getNamedItem
                        (Constants.ATTRIBUTE_MESSAGE_ID).getNodeValue();
                resultMap.put(msgId, Constants.DELETE_FAILED);
            }
        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }

        return resultMap;
    }

    /**
     * Description of the Method
     *
     * @param startDate Description of the Parameter
     * @param endDate Description of the Parameter
     * @param appContexts Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String archive(Date startDate, Date endDate,
            ApplicationContext[] appContexts)
             throws RequestException {

        final Date[] dates = new Date[2];
        dates[0] = startDate;
        dates[1] = endDate;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(dates);
            os.writeObject(appContexts);
            os.close();
            byte[] b = bos.toByteArray();
            bos.close();
            os = null;
            bos = null;

            final Command command = new Command
                    (CommandConstants.MSH_ARCHIVE_BY_DATE_AND_APPCONTEXT, b);
            final HttpURLConnection connection = sendCommand(command);
            return expectXMLResponse(connection,
                    "Incorrect archival response.");
        } catch (IOException e) {
            throw new RequestException
                    ("Cannot send archive command: " + e.getMessage());
        }
    }

    /**
     * Description of the Method
     *
     * @param startDate Description of the Parameter
     * @param endDate Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String archive(Date startDate, Date endDate)
             throws RequestException {
        final Date[] dates = new Date[2];
        dates[0] = startDate;
        dates[1] = endDate;

        final Command command =
                new Command(CommandConstants.MSH_ARCHIVE_BY_DATE, dates);
        final HttpURLConnection connection = sendCommand(command);

        return expectXMLResponse(connection, "Incorrect archival response.");
    }

    /**
     * Description of the Method
     *
     * @param appContexts Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String archive(ApplicationContext[] appContexts)
             throws RequestException {
        final Command command =
                new Command(CommandConstants.MSH_ARCHIVE_BY_APPCONTEXT,
                appContexts);
        final HttpURLConnection connection = sendCommand(command);

        return expectXMLResponse(connection, "Incorrect archival response.");
    }

    /**
     * Description of the Method
     *
     * @param startDate Description of the Parameter
     * @param endDate Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String archiveByDate(Date startDate, Date endDate)
             throws RequestException {
        return archive(startDate, endDate);
    }

    /**
     * Description of the Method
     *
     * @param appContexts Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    public String archiveByAppContext(ApplicationContext[] appContexts)
             throws RequestException {
        return archive(appContexts);
    }

    // MSH Interaction
    /**
     * Description of the Method
     *
     * @param connection Description of the Parameter
     * @param errorMessage Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    private static String expectXMLResponse(HttpURLConnection connection,
            String errorMessage) throws RequestException {
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(errorMessage + ": " + ioe.getMessage());
        }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                final InputStream inputStream = connection.getInputStream();
                final byte[] buffer = new byte[4096];
                String result = new String();

                for (int c = inputStream.read(buffer); c != -1;
                        c = inputStream.read(buffer)) {
                    result += new String(buffer, 0, c);
                }

                return result;
            } catch (IOException ioe) {
                throw new RequestException(errorMessage
                         + ": " + ioe.getMessage());
            }
        } else {
            throw new RequestException("\n" + errorMessage
                     + "\n  HTTP response code = " + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }

    /**
     * Description of the Method
     *
     * @param connection Description of the Parameter
     * @param errorMessage Description of the Parameter
     * @exception RequestException Description of the Exception
     */
    private static void expectNoResponse(HttpURLConnection connection,
            String errorMessage) throws RequestException {
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException ioe) {
            throw new RequestException(ioe.getMessage());
        }
        if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RequestException("\n" + errorMessage
                     + "\n  HTTP response code = " + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }

    /**
     * Description of the Method
     *
     * @param connection Description of the Parameter
     * @param errorMessage Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    private static Map expectMapResponse(HttpURLConnection connection,
            String errorMessage) throws RequestException {
        final int responseCode;
        final String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            throw new RequestException(e.getMessage());
        }
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            Map result = new TreeMap();
            String headerName;
            String headerValue;
            int i = 1;
            while ((headerName = connection.getHeaderFieldKey(i)) != null) {
                if (headerName.startsWith(Constants.QUERY_RESULT_PREFIX)) {
                    headerValue = connection.getHeaderField(i);
                    result.put(headerName, headerValue);
                }
                i++;
            }
            return result;
        } else {
            throw new RequestException("\n Failed to send query to MSH.\n"
                     + "  HTTP response code = " + String.valueOf(responseCode)
                     + "\n  HTTP response message = " + responseMessage);
        }
    }

    /**
     * Description of the Method
     *
     * @param doc Description of the Parameter
     * @return Description of the Return Value
     * @exception RequestException Description of the Exception
     */
    private static String domToString(Document doc) throws RequestException {
        try {
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }
    }
}

