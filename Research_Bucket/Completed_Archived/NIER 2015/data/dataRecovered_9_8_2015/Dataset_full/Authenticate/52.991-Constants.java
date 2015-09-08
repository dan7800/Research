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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/Constants.java,v 1.36 2004/01/16 06:52:42 bobpykoon Exp $
 *
 * Code authored by:
 *
 * frankielam [2003-01-07]
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

/**
 * This class serves as a bag containing all the constants that fall in the
 * following criteria:
 *
 * (1) Related to property settings
 * (2) Public strings
 * (3) Previously public strings that are declared multiple times in various
 *     locations.
 *
 * @author  Frankie Lam
 * @version $Revision: 1.36 $
 */
public class Constants {

    /** Program name */
    public static final String PROGRAM_NAME = "Hermes (ebms2) MSH";

    /*
     * Property file names
     */

    public static final String MSH_SERVER_PROPERTY_FILE =
        "msh.properties.xml";

    public static final String MSH_CLIENT_PROPERTY_FILE =
        "msh_client.properties.xml";

    public static final String DIAGNOSIS_PROPERTY_FILE =
        "diagnosis.properties.xml";

    /*
     * System properties
     */

    /**
     * System property storing the user's home directory
     */
    public static final String PROPERTY_USER_HOME = "user.home";

    /**
     * System property to get the host name of the proxy server
     */
    public static final String PROPERTY_HTTP_PROXY_HOST = "http.proxyHost";

    /**
     * System property to get the port number of the proxy server
     */
    public static final String PROPERTY_HTTP_PROXY_PORT = "http.proxyPort";


    /*
     * Constants for Server Side Properties
     */


    // Settings for MSH

    /**
     * Path to access the URL of this message service handler in
     * configuration file.
     */
    public static final String PROPERTY_MESSAGE_SERVICE_HANDLER_URL =
        "MSH/Config/URL";

    /**
     * Path to access the password file for authentication in
     * configuration file.
     */
    public static final String PROPERTY_AUTHENTICATION_FILE =
        "MSH/Config/AuthenticationFile";

    /**
     * Path to access the implementation class name of
     * <code>hk.hku.cecid.phoenix.message.handler.ToUrlResolver</code>
     * which maps a <ToPartyId> to a physical URL for sending message.
     */
    public static final String PROPERTY_TO_URL_RESOLVER =
        "MSH/Config/ToUrlResolver";

    /**
     * Path to access the implementation class name of
     * <code>hk.hku.cecid.phoenix.pki.CertResolver</code> which returns an
     * array of java.security.cert.Certificate's based on the identity
     * information in the received EbxmlMessage
     */
    public static final String PROPERTY_CERT_RESOLVER =
        "MSH/Config/CertResolver";

    /**
     * Path to access the custom implementation class name with a static
     * configure(hk.hku.cecid.phoenix.common.util.Property) method which is
     * invoked by MSH upon initialization to do local configuration.
     */
    public static final String PROPERTY_CONFIG_LOCAL =
        "MSH/Config/Local";


    // Settings for proxy

    /**
     * Path to access the host name of the proxy server in the
     * configuration file.
     */
    public static final String PROPERTY_PROXY_HOST = "MSH/Proxy/Host";

    /**
     * Path to access the port number of the proxy server in the
     * configuration file.
     */
    public static final String PROPERTY_PROXY_PORT = "MSH/Proxy/Port";


    // Settings for keystore on Digital Signature

    /**
     * Path to get trusted keystore location in the configuration file.
     */
    public static final String PROPERTY_TRUSTED_KEY_STORE_PATH =
        "MSH/DigitalSignature/TrustedAnchor/KeyStore/Path";

    /**
     * Path to get trusted keystore file name in the configuration file.
     */
    public static final String PROPERTY_TRUSTED_KEY_STORE_FILE =
        "MSH/DigitalSignature/TrustedAnchor/KeyStore/File";

    /**
     * Path to get trusted keystore password in configuration file
     */
    public static final String PROPERTY_TRUSTED_KEY_STORE_PASSWORD =
        "MSH/DigitalSignature/TrustedAnchor/KeyStore/Password";

    /**
     * Path to get keystore alias name in configuration file
     */
    public static final String PROPERTY_MSH_KEY_STORE_ALIAS =
        "MSH/DigitalSignature/AckSign/KeyStore/Alias";

    /**
     * Path to get key algorithm in the configuration file.
     */
    public static final String PROPERTY_MSH_KEY_ALGORITHM =
        "MSH/DigitalSignature/AckSign/KeyStore/Algorithm";

    /**
     * Path to get keystore directory in configuration file
     */
    public static final String PROPERTY_MSH_KEY_STORE_PATH =
        "MSH/DigitalSignature/AckSign/KeyStore/Path";

    /**
     * Path to get keystore file name in configuration file
     */
    public static final String PROPERTY_MSH_KEY_STORE_FILE =
        "MSH/DigitalSignature/AckSign/KeyStore/File";

    /**
     * Path to get keystore password in configuration file
     */
    public static final String PROPERTY_MSH_KEY_STORE_PASSWORD =
        "MSH/DigitalSignature/AckSign/KeyStore/Password";


    // Settings for logger

    /**
     * Path to access the logger used in configuration file.
     */
    public static final String PROPERTY_EXTERNAL_LOGGER_CONFIG =
        "MSH/Log/ExternalProperties";

    /**
     * Path to access the log path in configuration file.
     */
    public static final String PROPERTY_LOG_PATH =
        "MSH/Log/LogPath";

    /**
     * Path to access the log file in configuration file.
     */
    public static final String PROPERTY_LOG_FILE =
        "MSH/Log/LogFile";

    /**
     * Path to access the log level in configuration file.
     */
    public static final String PROPERTY_LOG_LEVEL =
        "MSH/Log/LogLevel";

    /**
     * Path to access the maximum log size in configuration file.
     */
    public static final String PROPERTY_MAX_LOG_SIZE = "MSH/Log/MaxFileSize";


    // Settings for Database

    public static final String PROPERTY_DATABASE_DRIVER =
        "MSH/Persistent/Database/Driver";

    public static final String PROPERTY_DATABASE_USER =
        "MSH/Persistent/Database/User";

    public static final String PROPERTY_DATABASE_PASSWORD =
        "MSH/Persistent/Database/Password";

    public static final String PROPERTY_DATABASE_URL =
        "MSH/Persistent/Database/URL";

    public static final String PROPERTY_DATABASE_INITIAL_CONNECTIONS =
        "MSH/Persistent/Database/InitialConnections";

    public static final String PROPERTY_DATABASE_MAXIMUM_CONNECTIONS =
        "MSH/Persistent/Database/MaximumConnections";

    public static final String PROPERTY_DATABASE_MAXIMUM_WAIT =
        "MSH/Persistent/Database/MaximumWait";

    public static final String PROPERTY_DATABASE_MAXIMUM_IDLE =
        "MSH/Persistent/Database/MaximumIdle";

    /**
     * Service name reserved for services described in ebXML Message Service
     * Specification [ebMSS 3.1.4].
     */
    public static final String SERVICE =
        "urn:oasis:names:tc:ebxml-msg:service";

    /**
     * Acknowledgement action [ebMSS 6.3.2.7]
     */
    public static final String ACTION_ACKNOWLEDGMENT = "Acknowledgment";

    /**
     * Action for an ErrorList element to be included in an independent
     * message [ebMSS 4.2.4.3]
     */
    public static final String ACTION_MESSAGE_ERROR = "MessageError";

    /**
     * Action for ping message [ebMSS 8.1]
     */
    public static final String ACTION_PING = "Ping";

    /**
     * Action for pong message [ebMSS 8.2]
     */
    public static final String ACTION_PONG = "Pong";

    /**
     * Action for status request message [ebMSS 7.1.1]
     */
    public static final String ACTION_STATUS_REQUEST = "StatusRequest";

    /**
     * Action for status response message [ebMSS 7.1.2]
     */
    public static final String ACTION_STATUS_RESPONSE = "StatusResponse";
    
    // Settings for SSL
    /**
     Path to access the classname of customized Hostname verifier for
     the SSL connection
     */
    public static final String PROPERTY_SSL_HOSTNAME_VERIFIER =
        "MSH/SSL/HostnameVerifier";

    /**
     Path to access the path of the keystore for the trust certificates on
     SSL Server Authentication
     */
    public static final String PROPERTY_SSL_TRUST_KEY_STORE_PATH =
        "MSH/SSL/TrustedAnchor/KeyStore/Path";

    /**
     Path to access the file of the keystore for the trust certificates on
     SSL Server Authentication.
     */
    public static final String PROPERTY_SSL_TRUST_KEY_STORE_FILE =
        "MSH/SSL/TrustedAnchor/KeyStore/File";

    /**
     Path to access the password of the keystore for the trust certificates on
     SSL Server Authentication.
     */
    public static final String PROPERTY_SSL_TRUST_KEY_STORE_PASSWORD =
        "MSH/SSL/TrustedAnchor/KeyStore/Password";

    /**
     Path to access the ssl client authentication info.
     */
    public static final String PROPERTY_SSL_CLIENT_AUTH = "MSH/SSL/ClientAuth";

    /**
     Path to access the URL inside ssl client authentication info.
     */
    public static final String PROPERTY_URL = "URL";

    /**
     Path to access the Keystore path inside ssl client authentication info.
     */
    public static final String PROPERTY_KEY_STORE_PATH = "KeyStore/Path";

    /**
     Path to access the Keystore file inside ssl client authentication info.
     */
    public static final String PROPERTY_KEY_STORE_FILE = "KeyStore/File";

    /**
     Path to access the Keystore alias inside ssl client authentication info.
     */
    public static final String PROPERTY_KEY_STORE_ALIAS = "KeyStore/Alias";

    /**
     Path to access the Keystore password inside ssl client authentication info.
     */
    public static final String PROPERTY_KEY_STORE_PASSWORD
        = "KeyStore/Password";

    // Settings for Repositories

    /**
     * Path to access the location of the trusted message listener repository
     * in configuration file.
     */
    public static final String PROPERTY_TRUSTED_LISTENER_REPOSITORY =
        "MSH/MessageListener/TrustedRepository";

    /** Parent path to save ebXML messages */
    public static final String PROPERTY_MESSAGE_REPOSITORY =
        "MSH/Persistent/MessageRepository";
    
    /**
     * The PersistenceHandler to store the message in message repository
     */
    public static final String PROPERTY_MESSAGE_REPOSITORY_PERSIST_HANDLER
            = "MSH/Persistent/MessageRepositoryPersistHandler";

    /** Maximum number of files allowed in a directory for saving ebXML
        message */
    public static final String PROPERTY_MAX_FILE_IN_SUBDIRECTORY =
        "MSH/Persistent/MaxFiles";

    /** Location of backup file */
    public static final String PROPERTY_MSH_BACKUP_FILE =
        "MSH/Persistent/BackupFile";

    /** Location of archive */
    public static final String PROPERTY_MSH_ARCHIVE_DIRECTORY =
        "MSH/Persistent/ArchiveDirectory";

    /**
     * Prefix to be prepended to the file name storing the message received.
     */
    public static final String TEMP_FILE_PREFIX = "received.";

    /**
     * Suffix to be appended to the file name storing the message received.
     */
    public static final String TEMP_FILE_SUFFIX = ".message";


    // Settings for mail

    /**
     * Path to get mail protocol in configuration file
     */
    public static final String PROPERTY_MAIL_PROTOCOL =
        "MSH/Mail/Poll/Protocol";

    /**
     * Path to get mail server in configuration file
     */
    public static final String PROPERTY_MAIL_HOST = "MSH/Mail/Poll/Host";

    /**
     * Path to get mail server port in configuration file
     */
    public static final String PROPERTY_MAIL_PORT = "MSH/Mail/Poll/Port";

    /**
     * Path to get mail folder in configuration file
     */
    public static final String PROPERTY_MAIL_FOLDER =
        "MSH/Mail/Poll/Folder";

    /**
     * Path to get user name for the mail account in configuration file
     */
    public static final String PROPERTY_MAIL_USER = "MSH/Mail/Poll/User";

    /**
     * Path to get password for the mail account in configuration file
     */
    public static final String PROPERTY_MAIL_PASSWORD =
        "MSH/Mail/Poll/Password";

    /**
     * Path to get the time interval for polling the mail account
     * in configuration file.
     */
    public static final String PROPERTY_MAIL_MONITOR_INTERVAL =
        "MSH/Mail/Poll/MonitorInterval";


    // Settings for special behaviours of MSH

    public static final String PROPERTY_POSITIVE_ACKNOWLEDGMENT =
        "MSH/Config/PositiveAcknowledgment";

    public static final String PROPERTY_AUGMENTED_ERROR_MESSAGE =
        "MSH/Config/AugmentedErrorMessage";

    public static final String PROPERTY_CONTENT_TRANSFER_ENCODING =
        "MSH/Config/ContentTransferEncoding";

    /*
     * Constants for Client-Side Properties
     */

    public static final String PROPERTY_REQUEST_EXTERNAL_LOGGER_CONFIG =
        "Request/Log/ExternalProperties";

    public static final String PROPERTY_REQUEST_LOG_PATH = 
        "Request/Log/LogPath";

    public static final String PROPERTY_REQUEST_LOG_FILE = 
        "Request/Log/LogFile";

    public static final String PROPERTY_REQUEST_LOG_LEVEL =
        "Request/Log/LogLevel";

    public static final String PROPERTY_REQUEST_MAX_LOG_SIZE =
        "Request/Log/MaxFileSize";

    /**
     * Path to save sending thread context
     */
    public static final String PROPERTY_MESSAGE_LISTENER_OBJECT_STORE =
        "MSH/MessageListener/ObjectStore";
    /**
     * The Persistence Handler class to handler the persistence of objectStore
     */
    public static final String
            PROPERTY_MESSAGE_LISTENER_OBJECT_STORE_PERSIST_HANDLER
                    = "MSH/MessageListener/ObjectStorePersistHandler";

    /**
     * Path to access the location of message respository used by listener
     * in the configuration file.
     */
    public static final String PROPERTY_REQUEST_LISTENER_REPOSITORY =
        "Request/MessageListener/MessageRepository";

    /**
     * Path to access the location of message respository used by listener
     * in the configuration file.
     */
    public static final String PROPERTY_REQUEST_MESSAGE_SERVICE_HANDLER_URL =
        "Request/Config/URL";

    /**
     * Path to access the user name for authentication in the configuration
     * file.
     */
    public static final String PROPERTY_AUTHENTICATION_USERNAME =
        "Request/Config/UserName";

    /**
     * Path to access the password for authentication in the configuration
     * file.
     */
    public static final String PROPERTY_AUTHENTICATION_PASSWORD =
        "Request/Config/Password";

    /**
     * Path to access the maximum number of payload in the configuration file.
     */
    public static final String PROPERTY_MAX_NUM_PAYLOAD =
        "Request/Config/MaxNumPayload";

    /**
     * Path to access the maximum payload size in the configuration file.
     */
    public static final String PROPERTY_MAX_PAYLOAD_SIZE =
        "Request/Config/MaxPayloadSize";

    /**
     * Path to get monitor interval in configuration file.
     */
    public static final String PROPERTY_REQUEST_MONITOR_INTERVAL =
        "Request/Config/MonitorInterval";


    /*
     * Constants for Diagnosis tool
     */

    /** Destination directory where the report should be output */
    public static final String PROPERTY_DESTINATION_DIR =
        "Diagnosis/Destination";

    /** Path of the MSH configuration file */
    public static final String PROPERTY_MSH_CONFIG_FILE =
        "Diagnosis/MshConfigFile";

    /** Optional criteria specifying the starting time period */
    public static final String PROPERTY_CRITERIA_START_TIME =
        "Diagnosis/Criteria/TimePeriod/StartTime";

    /** Optional criteria specifying the ending time period */
    public static final String PROPERTY_CRITERIA_END_TIME =
        "Diagnosis/Criteria/TimePeriod/EndTime";

    /**
     * Optional criteria to restrict the set of application contexts to be
     * dumped
     */
    public static final String PROPERTY_CRITERIA_APPCONTEXT =
        "Diagnosis/Criteria/AppContexts/AppContext";

    /** CPA ID of the application context criteria */
    public static final String PROPERTY_CPA_ID = "CpaId";

    /** Conversation ID of the application context criteria */
    public static final String PROPERTY_CONVERSATION_ID = "ConversationId";

    /** Service name of the application context criteria */
    public static final String PROPERTY_SERVICE = "Service";

    /** Action name of the application context criteria */
    public static final String PROPERTY_ACTION = "Action";

    /** The set of files that should be copied */
    public static final String PROPERTY_COPY = "Diagnosis/Copy";

    /** Destination directory where the files should be copied */
    public static final String PROPERTY_TO_DIR = "ToDir";

    /** Files that should be copied */
    public static final String PROPERTY_PATH = "Path";

    /** Database driver */
    public static final String MSH_PROPERTY_DATABASE_DRIVER =
        "MSH/Persistent/Database/Driver";

    /** Database user name */
    public static final String MSH_PROPERTY_DATABASE_USER =
        "MSH/Persistent/Database/User";

    /** Database password */
    public static final String MSH_PROPERTY_DATABASE_PASSWORD =
        "MSH/Persistent/Database/Password";

    /** JDBC URL of the database */
    public static final String MSH_PROPERTY_DATABASE_URL =
        "MSH/Persistent/Database/URL";

    /** Transaction isolation level. */
    public static final String MSH_PROPERTY_DATABASE_TRANSACTION_ISOLATION_LEVEL =
        "MSH/Persistent/Database/TransactionIsolationLevel";

    /** Message repository of MSH */
    public static final String MSH_PROPERTY_MESSAGE_REPOSITORY =
        "MSH/Persistent/MessageRepository";

    /** Log path of MSH */
    public static final String MSH_PROPERTY_LOG_PATH = "MSH/Log/LogPath";

    /** Log file name of MSH */
    public static final String MSH_PROPERTY_LOG_FILE = "MSH/Log/LogFile";

    /** Name of the repository directory where messages are copied to */
    public static final String DIRECTORY_REPOSITORY = "repository";

    /** Name of the database directory where database reports are saved */
    public static final String DIRECTORY_DATABASE = "database";

    /** Name of the log directory where logs are saved */
    public static final String DIRECTORY_LOG = "logs";


    /*
     * Transport protocols
     */

    /** Transport messages by electronic mail */
    public static final String TRANSPORT_TYPE_MAIL = "mail";

    /** Transport messages by http */
    public static final String TRANSPORT_TYPE_HTTP = "http";

    /** Transport messages by https */
    public static final String TRANSPORT_TYPE_HTTPS = "https";

    /**
     * Different levels of syncReplyMode
     */
    public static final int SYNC_REPLY_MODE_NONE = 0;

    public static final int SYNC_REPLY_MODE_MSH_SIGNALS_ONLY = 1;

    public static final int SYNC_REPLY_MODE_SIGNALS_ONLY = 2;

    public static final int SYNC_REPLY_MODE_RESPONSE_ONLY = 3;

    public static final int SYNC_REPLY_MODE_SIGNALS_AND_RESPONSE = 4;

    /**
     * Different levels of ackRequested
     */
    public static final int ACK_REQUESTED_PERMESSAGE = 0; 
    public static final int ACK_REQUESTED_ALWAYS = 1; 
    public static final int ACK_REQUESTED_NEVER = 2; 

    /*
     * XML declarations and tag names
     */

    /**
     * XML declaration
     */
    public static final String XML_DECLARATION = "<?xml version=\"1.0\"?>";

    /**
     * HTTP Header attribute specifying content type
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * HTTP Header attribute specifying content length.
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * HTTP Header attribute specifying content id.
     */
    public static final String CONTENT_ID = "Content-Id";

    /**
     * HTTP Header attribute specifying content transfer encoding.
     */
    public static final String CONTENT_TRANSFER_ENCODING =
        "Content-Transfer-Encoding";

    public static final String DEFAULT_CONTENT_TRANSFER_ENCODING = "binary";

    /**
     * MIME boundary.
     */
    public static final String MIME_BOUNDARY = "boundary";

    /**
     * Prefix to be applied to separate different parts of MIME data.
     */
    public static final String MIME_BOUNDARY_PREFIX = "--";

    /**
     * HTTP content type specifying xml data.
     */
    public static final String TEXT_XML_TYPE = "text/xml";

    /**
     * multipart/related content type of MIME message.
     */
    public static final String MULTIPART_RELATED = "multipart/related";

    /**
     * HTTP content type for multi-part data, which is used when the ebXML
     * message contains payload data.
     */
    public static final String MULTIPART_RELATED_TYPE =
        MULTIPART_RELATED + "; type=\"" + TEXT_XML_TYPE + "\"; " +
        MIME_BOUNDARY + "=";

    /**
     * Content type character set attribute.
     */
    public static final String CHARACTER_SET = "charset";


    /**
     * Default XML character encoding.
     */
    public static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * Content type start attribute.
     */
    public static final String START = "start";

    /** 
     * CRLF
     */
    public static final String CRLF = "\r\n";

    /*
     * XML tags for Exports
     */

    /** Root element of database reports */
    public static final String ELEMENT_TABLE = "Table";

    /** Name of the element representing each database row */
    public static final String ELEMENT_ROW = "Row";

    /** Name of the attribute of the database name */
    public static final String ATTRIBUTE_NAME = "name";


    /*
     * XML tags for MSH Responses to Client Requests
     */

    /**
     * Root element name for the diagnostic command responses.
     */
    public static final String ELEMENT_RESPONSE_ROOT = "Response";

    /**
     * Element name for storing the result of diagnostic command.
     */
    public static final String ELEMENT_RESULT = "Result";

    /**
     * Optional element for storing the reason of failure, if any.
     */
    public static final String ELEMENT_REASON = "Reason";

    /**
     * List of successful entries
     */
    public static final String ELEMENT_SUCCEEDED = "Succeeded";

    /**
     * List of failed entries
     */
    public static final String ELEMENT_FAILED = "Failed";

    /**
     * Application Context.
     */
    public static final String ELEMENT_APPLICATION_CONTEXT
        = "ApplicationContext";

    /**
     * Timestamp at which archive operation is invoked.
     */
    public static final String ELEMENT_TIMESTAMP = "Timestamp";

    /**
     * Generic Value element.
     */
    public static final String ELEMENT_VALUE = "Value";

    /**
     * Element name for the XML document returned by the
     * REPORT_ENVIRONMENT command.
     */
    public static final String ELEMENT_ENVIRONMENT = "Environment";

    /**
     * Name for the Property element which contains a key-value entry in the
     * XML document returned by the REPORT_ENVIRONMENT command.
     */
    public static final String ELEMENT_ENVIRONMENT_PROPERTY = "Property";

    /**
     * Name for the Key element in the XML document returned by the
     * REPORT_ENVIRONMENT command.
     */
    public static final String ELEMENT_ENVIRONMENT_KEY = "Key";

    /**
     * Name for the Value element in the XML document returned by the
     * REPORT_ENVIRONMENT command.
     */
    public static final String ELEMENT_ENVIRONMENT_VALUE = "Value";

    /**
     * Name of the Message element in the XML document returned by the
     * QUERY_MESSAGE_STATUS command.
     */
    public static final String ELEMENT_MESSAGE = "Message";

    /**
     * Name of the NumRecords element in the XML document returned by the
     * QUERY_NUM_RECORDS_IN_DB command.
     */
    public static final String ELEMENT_NUM_RECORDS = "NumRecords";

    /**
     * Name of the Database element in the XML document returned by
     * QUERY_DB_CONNECTION_POOL command. This allows the returned XML
     * to be very similar to that of MSH configuration file.
     */
    public static final String ELEMENT_DATABASE = "Database";

    /**
     * Name of the DB_URL element in the XML document returned by the
     * QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_DB_URL = "URL";

    /**
     * Name of the DB_User element in the XML document returned by the
     * QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_DB_USER = "User";

    /**
     * Name of the InitialConnections element in the XML document returned by
     * the QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_INIT_CONN = "InitialConnections";

    /**
     * Name of the MaximumConnections element in the XML document returned by
     * the QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_MAX_CONN = "MaximumConnections";

    /**
     * Name of the ConnectionsCreated element in the XML document returned by
     * the QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_CREATED_CONN = "ConnectionsCreated";

    /**
     * Name of the ConnectionsInUse element in the XML document returned by
     * the QUERY_DB_CONNECTION_POOL command.
     */
    public static final String ELEMENT_CONN_IN_USE = "ConnectionsInUse";

    /**
     * Name of the ID attribute of the Message element.
     */
    public static final String ATTRIBUTE_MESSAGE_ID = "id";


    /**
     * Reference time zone.
     */
    public static final String TIME_ZONE = "GMT";


    /*
     * Default values for Directory Manager
     */

    /** Default parent path to save ebXML messages */
    public static final String DEF_MESSAGE_REPOSITORY =
        "/tmp/ebxmlms";

    /** Default path to save sending thread context */
    public static final String DEF_MESSAGE_LISTENER_OBJECT_STORE =
        "/tmp/ebxmlms/objectStore";

    /** Default maximum number of files allowed in a directory for saving
        ebXML message */
    public static final int DEF_MAX_NUM_IN_SUBDIR = 1000;


    /*
     * Default values for Export
     */

    /** Prefix of the database reports */
    public static final String PREFIX_DATABASE_REPORT = "DB_";

    /** File name extension of all reports */
    public static final String SUFFIX_ALL_REPORT = ".xml";


    /*
     * Default values for Mail Poller
     */

    /**
     * Largest time interval allowed to poll the mail account
     */
    public static final long MAIL_MAX_WAIT_INTERVAL = 320000;

    /**
     * Smallest time interval allowed to poll the mail account
     */
    public static final long MAIL_MIN_WAIT_INTERVAL = 5000;


    /*
     * Default values for Request
     */

    /**
     * Maximum time interval between successive polls (in milliseconds).
     */
    public static final long REQUEST_MAX_WAIT_INTERVAL = 320000;

    /**
     * Minimum time interval between successive polls (in milliseconds).
     */
    public static final long REQUEST_MIN_WAIT_INTERVAL = 1;

    /*
     * Status values appeared in all sorts of messages.
     */

    // Message statuses in Request.getMessageStatus()

    public static final String MESSAGE_STATUS_STATE = "State";

    public static final String MESSAGE_STATUS_STATUS = "Status";

    public static final String MESSAGE_STATUS_DESCRIPTION = "Description";

    public static final String MESSAGE_STATUS_TIMESTAMP = "Timestamp";

    public static final String MESSAGE_STATUS_REMOTE_ADDRESS = "RemoteAddress";

    public static final String MESSAGE_STATUS_REMOTE_HOST = "RemoteHost";

    public static final String MESSAGE_STATUS_RETRYING = "Retrying ";

    public static final String MESSAGE_STATUS_SENT_STARTED = "Started Sending";

    public static final String MESSAGE_STATUS_ACKNOWLEDGED =
        "Sent and Acknowledgment Received";

    public static final String MESSAGE_STATUS_SENT_FAILED = "Sent Failed";

    public static final String MESSAGE_STATUS_SENT = "Sent";

    public static final String MESSAGE_STATUS_SENT_RECEIVED =
        "Sent and Received (loopback message)";

    public static final String MESSAGE_STATUS_RECEIVED_ACKNOWLEDGED =
        "Received and Acknowledgment Sent";

    public static final String MESSAGE_STATUS_RECEIVED =
        "Received";

    public static final String MESSAGE_STATUS_DELETED = "Deleted";

    public static final String MESSAGE_STATUS_UNKNOWN = "Unknown";


    // Message statuses in ebxml Status Response

    public static final String STATUS_UN_AUTHORIZED = "UnAuthorized";

    public static final String STATUS_NOT_RECOGNIZED = "NotRecognized";

    public static final String STATUS_RECEIVED = "Received";

    public static final String STATUS_PROCESSED = "Processed";

    public static final String STATUS_FORWARDED = "Forwarded";


    // Message deletion statuses

    /**
     * Pending message deletion result.
     */
    public static final String DELETE_SUCCESSFUL =
        "Message deleted successfully";

    public static final String DELETE_FAILED =
        "Message cannot be deleted";

    // Package-level shared constants

    /**
     * Prefixes that are appended to all the key names that are embeded in the
     * HTTP header.
     */
    static final String QUERY_RESULT_PREFIX = "MSHRESULT_";

    static final String QUERY_RESULT_SEQUENCE_NUMBER =
        QUERY_RESULT_PREFIX + "SequenceNumber";

    static final String QUERY_RESULT_REPOSITORY =
        QUERY_RESULT_PREFIX + "Repository";

    static final String QUERY_RESULT_MSH_STATUS =
        QUERY_RESULT_PREFIX + "MshStatus";

    static final String QUERY_RESULT_REGISTRATION =
        QUERY_RESULT_PREFIX + "RegistrationResult";

    /**
     * HTTP content type specifying binary data, which is a serialized command
     * object in <code>MessageServiceHandler</code>.
     */
    public static final String SERIALIZABLE_OBJECT =
        "application/octet-stream";

    /**
     * Default ToMshUrl
     */
    static final String DEFAULT_TO_MSH_URL = "<null>";
}
