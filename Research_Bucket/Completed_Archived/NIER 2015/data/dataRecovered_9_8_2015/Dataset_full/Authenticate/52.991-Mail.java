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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/transport/Mail.java,v 1.49 2004/04/01 08:10:09 bobpykoon Exp $
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

package hk.hku.cecid.phoenix.message.transport;

import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.handler.MessageServer;
import hk.hku.cecid.phoenix.message.handler.Constants;
import hk.hku.cecid.phoenix.message.handler.ErrorMessages;
import hk.hku.cecid.phoenix.message.handler.InitializationException;
import hk.hku.cecid.phoenix.message.handler.Utility;
import hk.hku.cecid.phoenix.message.packaging.AttachmentDataSource;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.MessageHeader;
import hk.hku.cecid.phoenix.pki.SMIMEDecrypter;
import hk.hku.cecid.phoenix.pki.SMIMEEncrypter;
import hk.hku.cecid.phoenix.pki.SMIMEException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Logger;

/**
 * Transport layer to send an <code>EbxmlMessage</code> via JavaMail.
 *
 * @author cyng
 * @version $Revision: 1.49 $
 */
public final class Mail {

    static Logger logger = Logger.getLogger(Mail.class);

    /** 
     * Path to get the "debug" property from the configuration file,
     * which is a flag indicating whether debugging information from the 
     * JavaMail library should be used.
     */
    public static final String PROPERTY_MAIL_DEBUG = "MSH/Mail/Debug";

    /** 
     * Path to get the ForceChangeSubType property from the configuration file,
     * which is a flag indicating whether MSH should force to change the
     * subtype of all outgoing SOAP message from "text/xml" to
     * "multipart/related"
     */
    public static final String PROPERTY_CHANGE_SUBTYPE = 
        "MSH/Mail/Poll/ForceChangeSubType";

    /** 
     * Path to get the SMTP host name from the configuration file.
     */
    public static final String PROPERTY_MAIL_SMTP_HOST = "MSH/Mail/SMTP/Host";

    /** 
     * Path to get the SMTP authentication username from the configuration file.
     */
    public static final String PROPERTY_MAIL_SMTP_AUTH_USER = 
        "MSH/Mail/SMTP/User";

    /** 
     * Path to get the SMTP authentication password from the configuration file.
     */
    public static final String PROPERTY_MAIL_SMTP_AUTH_PASS = 
        "MSH/Mail/SMTP/Password";

    /** 
     * Path to get the SMTP port from the configuration file.
     */
    public static final String PROPERTY_MAIL_SMTP_HOST_PORT = 
        "MSH/Mail/SMTP/Port";

    /**
     * Path to get SMIME encryption keystore path.
     */
    public static final String PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_PATH =
        "MSH/Mail/SMIME/Encryption/KeyStore/Path";

    /**
     * Path to get SMIME encryption keystore file.
     */
    public static final String PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_FILE =
        "MSH/Mail/SMIME/Encryption/KeyStore/File";

    /**
     * Path to get SMIME encryption keystore password.
     */
    public static final String PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_PASSWORD
        = "MSH/Mail/SMIME/Encryption/KeyStore/Password";

    /**
     * Path to get SMIME decryption keystore path.
     */
    public static final String PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_PATH =
        "MSH/Mail/SMIME/Decryption/KeyStore/Path";

    /**
     * Path to get SMIME decryption keystore file.
     */
    public static final String PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_FILE =
        "MSH/Mail/SMIME/Decryption/KeyStore/File";

    /**
     * Path to get SMIME decryption keystore alias.
     */
    public static final String PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_ALIAS
        = "MSH/Mail/SMIME/Decryption/KeyStore/Alias";

    /**
     * Path to get SMIME decryption keystore password.
     */
    public static final String PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_PASSWORD
        = "MSH/Mail/SMIME/Decryption/KeyStore/Password";

    /** 
     * SMTP host name for the JavaMail library.
     */
    public static final String PROPERTY_JAVA_MAIL_SMTP_HOST = "mail.smtp.host";

    /** 
     * SMTP port number for the JavaMail library.
     */
    public static final String PROPERTY_JAVA_MAIL_SMTP_HOST_PORT =
        "mail.smtp.port";

    /** 
     * Whether to use SMTP authentication for the JavaMail library.
     */
    public static final String PROPERTY_JAVA_MAIL_AUTH = "mail.smtp.auth";

    /** 
     * Mail store protocol for the JavaMail library.
     */
    public static final String PROPERTY_JAVA_MAIL_STORE_PROTOCOL =
        "mail.store.protocol";

    /** 
     * Transfer encoding name for the JavaMail library.
     */
    public static final String PROPERTY_JAVA_MAIL_TRANSFER_ENCODING =
        "mail.transfer.encoding";

    /** 
     * Content-ID attribute name for MIME messages.
     */
    public static final String CONTENT_ID = "Content-ID";

    /** 
     * Content transfer encoding attribute name for MIME messages.
     */
//    public static final String CONTENT_TRANSFER_ENCODING =
//        "Content-Transfer-Encoding";

    /** 
     * Content type attribute for MIME messages.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Mime header for SOAP action
     */
    public static final String SOAP_ACTION = "SOAPAction";

    /**
     * Recommended SOAP Action value
     */
    public static final String EBXML_SOAP_ACTION = "\"ebXML\"";

    /** 
     * Default MIME multipart subtype for MIME messages.
     */
    public static final String MIME_MULTIPART_SUBTYPE = "related";

    /** 
     * Default message type for MIME messages.
     */
    public static final String MESSAGE_TYPE = "text/xml";

    /** 
     * Default content type for SMIME messages.
     */
    public static final String SMIME_ENCRYPTED = "application/pkcs7-mime";

    /** 
     * Default transfer encoding for MIME messages.
     */
    private static final String MAIL_TRANSFER_ENCODING = "base64";

    /**
     * Prefix of email address.
     */
    private static final String MAIL_PREFIX = "mailto:";

    private static final String WRONG_TYPE = "TEXT/XML; TYPE=\"TEXT/XML\"";

    /** 
     * SOAP Message factory.
     */
    private static MessageFactory messageFactory;

    /** 
     * SMTP host name
     */
    private static String smtpHost;

    /** 
     * SMTP authentication user name
     */
    private static String smtpAuthUser;

    /** 
     * SMTP authentication password
     */
    private static String smtpAuthPass;

    /**
     *  
     */
    private static String smimeEncKeyStoreLocation;

    /** 
     * SMIME encryption keystore password
     */
    private static String smimeEncKeyStorePassword;

    /**
     *  SMIME decryption location
     */
    private static String smimeDecKeyStoreLocation;

    /** 
     * SMIME decryption keystore alias
     */
    private static String smimeDecKeyStoreAlias;

    /** 
     * SMIME decryption keystore password
     */
    private static String smimeDecKeyStorePassword;

    /** 
     * Flag indicating if debug information from JavaMail should be used.
     */
    private static boolean debug;

    /**
     * Flag indicating if MSH will change the subtype of all outgoing SOAP
     * messages.
     */ 
    private static boolean changeSubType;
    
    private static String smtpPort = null;

    protected static boolean isConfigured = false;

    public static synchronized void configure(Property prop) 
        throws InitializationException {

        if (isConfigured) return;

        Utility.configureLogger(prop, "hk.hku.cecid.phoenix.message");

        try {
            messageFactory = MessageFactory.newInstance();
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_MESSAGE_FACTORY, e);
            logger.error(err);
            throw new InitializationException(err);
        }

        smtpHost = prop.get(PROPERTY_MAIL_SMTP_HOST);
        smtpPort = prop.get(PROPERTY_MAIL_SMTP_HOST_PORT);
        smtpAuthUser = prop.get(PROPERTY_MAIL_SMTP_AUTH_USER);
        smtpAuthPass = prop.get(PROPERTY_MAIL_SMTP_AUTH_PASS);
        String smimeEncKeyStoreLocation = prop.get(
            PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_PATH) + File.separator
            + prop.get(PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_FILE);
        smimeEncKeyStorePassword = prop.get
            (PROPERTY_MAIL_SMIME_ENCRYPTION_KEY_STORE_PASSWORD);
        String smimeDecKeyStorePath = prop.get
            (PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_PATH);
        String smimeDecKeyStoreFile = prop.get
            (PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_FILE);
        String decKeyStoreAlias = prop.get
            (PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_ALIAS);
        String decKeyStorePassword = prop.get
            (PROPERTY_MAIL_SMIME_DECRYPTION_KEY_STORE_PASSWORD);
        configSMIMEDecryption(
            smimeDecKeyStorePath + File.separator + smimeDecKeyStoreFile,
            decKeyStoreAlias, decKeyStorePassword);
        debug = Boolean.valueOf(prop.get(PROPERTY_MAIL_DEBUG, "false"))
                .booleanValue();
        changeSubType = Boolean.valueOf(
                prop.get(PROPERTY_CHANGE_SUBTYPE, "false"))
                .booleanValue();

        logger.info("Outgoing mail configuration:");
        logger.info("  SMTP host: " + smtpHost);
        if (smtpAuthUser != null && smtpAuthPass != null) {
            logger.info("  SMTP authentication enabled");
            logger.info("  SMTP user: " + smtpAuthUser);
            logger.info("  SMTP password: <not shown>");
        }
        else {
            logger.info("  SMTP authentication disabled");
        }
        logger.info("  S/MIME encryption keystore: " + smimeEncKeyStoreLocation);
        logger.info("  S/MIME encryption keystore password: <not shown>");
        logger.info("  S/MIME decryption keystore: "
            + smimeDecKeyStoreLocation);
        logger.info("  S/MIME decryption keystore alias: " 
            + smimeDecKeyStoreAlias);
        logger.info("  S/MIME decryption keystore password: <not shown>");
        logger.info("  Mail API debug: " + debug);
        logger.info("  Content-type augmentation: " + changeSubType);

        isConfigured = true;
    }
    
    /**
     * config the Mail setting about SMIME decryption.
     * @param location The keystore location of the SMIME decryption key.
     * @param alias The alias to locate the SMIME decryption key
     * @param password The password to get the SMIME decryption key.
     */
    public static void configSMIMEDecryption(String location, String alias,
        String password) {
        smimeDecKeyStoreLocation = location;
        smimeDecKeyStoreAlias = alias;
        smimeDecKeyStorePassword = password;
        
    }

    /** Send an ebXML message to the SMTP mail server

        @param ebxmlMessage an ebXML message
        @param toMshUrl email address of the receiving MessageServiceHandler
    */
    public static void send(EbxmlMessage ebxmlMessage, String toMshUrl)
        throws TransportException {
        send(ebxmlMessage, toMshUrl, smtpHost, smtpAuthUser, smtpAuthPass, 
            false);
    }

    /** Send an ebXML message to the SMTP mail server

        @param ebxmlMessage an ebXML message
        @param toMshUrl email address of the receiving MessageServiceHandler
        @param smtpHost SMTP host name
    */
    public static void send(EbxmlMessage ebxmlMessage, String toMshUrl,
                            String smtpHost, String smtpUser, String smtpPass)
        throws TransportException {
        logger.debug("=> Mail.send");
        logger.info("Sending message to " 
            + toMshUrl + " through " + smtpHost);
        MimeMessage mimeMessage = createSendMailMessage(ebxmlMessage, toMshUrl,
            smtpHost, smtpUser, smtpPass).getMimeMessage();
        try {
            Transport.send(mimeMessage);
        }
        catch (MessagingException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE));
        }
        logger.debug("<= Mail.send");
    }

    /** Send an ebXML message to the SMTP mail server

        @param ebxmlMessage an ebXML message
        @param toMshUrl email address of the receiving MessageServiceHandler
        @param smtpHost SMTP host name
        @param smtpUser SMTP user name
        @param smtpPass SMTP password
        @param smime use smime for encryption or not
        @throws TransportException throw when exception on transport.
    */
    public static void send(EbxmlMessage ebxmlMessage, String toMshUrl,
        String smtpHost, String smtpUser, String smtpPass, boolean smime)
        throws TransportException {
        if (smime) {
            send(ebxmlMessage, toMshUrl, smtpHost, smtpUser, smtpPass,
                smimeEncKeyStoreLocation, toMshUrl, smimeEncKeyStorePassword);
        } else {
            send(ebxmlMessage, toMshUrl, smtpHost, smtpUser, smtpPass);
        }
    }
    
    /**
     * 
     * @param ebxmlMessage an ebXML message
     * @param toMshUrl email address of the receiving MessageServiceHandler
     * @param smtpHost SMTP host name
     * @param smtpUser SMTP user name
     * @param smtpPass SMTP password
     * @param smimeEncryptionLocation
     * @param smimeEncryptionAlias
     * @param smimeEncryptionPassword
     * @throws TransportException throw when exception on transport.
     */
    public static void send(EbxmlMessage ebxmlMessage, String toMshUrl,
        String smtpHost, String smtpUser, String smtpPass,
        X509Certificate certificate) throws TransportException {
        logger.debug("=> Mail.send");
        logger.info("Sending encrypted message to " 
            + toMshUrl + " through " + smtpHost);

        MimeMessage mimeMessage = createSmimeMessage(ebxmlMessage, toMshUrl,
            smtpHost, smtpUser, smtpPass, certificate);
        try {
            Transport.send(mimeMessage);
        }
        catch (MessagingException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE));
        }
        logger.debug("<= Mail.send");
    }

    /**
     * 
     * @param ebxmlMessage an ebXML message
     * @param toMshUrl email address of the receiving MessageServiceHandler
     * @param smtpHost SMTP host name
     * @param smtpUser SMTP user name
     * @param smtpPass SMTP password
     * @param smimeEncryptionLocation
     * @param smimeEncryptionAlias
     * @param smimeEncryptionPassword
     * @throws TransportException throw when exception on transport.
     */
    public static void send(EbxmlMessage ebxmlMessage, String toMshUrl,
        String smtpHost, String smtpUser, String smtpPass,
        String smimeEncryptionLocation, String smimeEncryptionAlias,
        String smimeEncryptionPassword) throws TransportException {
        logger.debug("=> Mail.send");
        logger.info("Sending encrypted message to " 
            + toMshUrl + " through " + smtpHost);

        MimeMessage mimeMessage = createSmimeMessage(ebxmlMessage, toMshUrl,
            smtpHost, smtpUser, smtpPass, smimeEncryptionLocation,
            smimeEncryptionAlias, smimeEncryptionPassword);
        try {
            Transport.send(mimeMessage);
        }
        catch (MessagingException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_SEND_MESSAGE));
        }
        logger.debug("<= Mail.send");
    }

    /** Receive mails from a remote mail box in <code>smtpHost</code> using
        the specified <code>protocol</code>, <code>username</code> and
        <code>password</code>

        @param protocol protocols such as <code>imap</code> or
        <code>pop3</code> to connect to remote mail box
        @param smtpHost SMTP mail server name
        @param port SMTP mail server port
        @param folderName remote mail box name such as <code>INBOX</code>
        @param username username for mail box authentication
        @param password password for mail box authentication
        @return an array of <code>EbxmlMessage</code>s'
    */
    public static ReceivedMessage[] receive(String protocol, String smtpHost,
                                         String port, String folderName,
                                         String username, String password)
        throws TransportException {

        logger.debug("=> Mail.receive");
        logger.info("Receiving " + protocol + " messages from " + smtpHost 
            + "<" + folderName + ">"); 

        try {
            Properties prop = new Properties();
            prop.put("mail.pop3.connectiontimeout", "30000");
            prop.put("mail.pop3.timeout", "3000");
            final Session session = Session.getDefaultInstance(prop);
            session.setDebug(debug);

            final Store store = session.getStore(protocol);
            if (port == null)
                store.connect(smtpHost, username, password);
            else
                store.connect(smtpHost, Integer.parseInt(port), username,
                              password);
            final Folder folder = store.getFolder(folderName);
            if (!folder.exists() ||
                (folder.getType() & Folder.HOLDS_MESSAGES) == 0) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_POP_INVALID_FOLDER, folderName);
                logger.error(err);
                throw new TransportException(err);
            }

            folder.open(Folder.READ_WRITE);
            final Message[] messages = folder.getMessages();
            //final ArrayList ebxmlMessages = new ArrayList();
            final ArrayList receivedMessages = new ArrayList();

            /* For each of the mails received, compose the corresponding
               <code>EbxmlMessage</code>
            */
            for (int i=0 ; i<messages.length ; i++) {
//                final String mailDate = DateFormat.
//                    getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).
//                    format(messages[i].getSentDate());
                final String messageNumber =
                    Integer.toString(messages[i].getMessageNumber());

                if ((messages[i] instanceof MimeMessage) == false) {
                    logger.debug("Skipping message number " + messageNumber 
                        + ": not a MimeMessage!");
                    /* uncomment the following line if unexpected messages
                       should be deleted from the mail box */
                    // messages[i].setFlag(Flags.Flag.DELETED, true);
                    continue;
                }

                /* Get the ebXML message header and MIME headers from the mail
                   and construct a SOAP message to represent the message header
                */
                final MimeHeaders mimeHeaders = new MimeHeaders();
                final SOAPMessage soapMessage;
                try {
                    String type = messages[i].getContentType();
System.out.println(type);
                    int index = type.toUpperCase().indexOf(WRONG_TYPE);
                    final String[] soapAction =
                        messages[i].getHeader(SOAP_ACTION);
                    
                    if (soapAction != null && soapAction.length != 0) {
                        mimeHeaders.addHeader(SOAP_ACTION, soapAction[0]);
                    }
                    else {
                        mimeHeaders.addHeader(SOAP_ACTION, EBXML_SOAP_ACTION);
                    }
                    boolean isSmimeEncrypted = false;
                    Message currentMessage = messages[i];
                    if (type.toLowerCase().startsWith(SMIME_ENCRYPTED)) {
                        logger.debug("Message S/MIME encrypted, now decrypt");
                        messages[i] = SMIMEDecrypter.decryptMimeMessage(
                            smimeDecKeyStoreLocation, smimeDecKeyStorePassword,
                            smimeDecKeyStoreAlias, smimeDecKeyStorePassword,
                            (MimeMessage) messages[i], session);
                        mimeHeaders.addHeader(CONTENT_TYPE, messages[i].
                                              getContentType());
                        soapMessage = messageFactory.createMessage
                            (mimeHeaders, messages[i].getInputStream());
                        isSmimeEncrypted = true;
                    }
                    else if (changeSubType && index >= 0) {
                        logger.debug(
                            "Repair content sub type of the mime header");
                        byte[] buffer = new byte[4096];
                        InputStream in = messages[i].getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for (int c=in.read(buffer); c!=-1; c=in.read(buffer)) {
                            out.write(buffer, 0, c);
                        }
                        byte[] inBytes = out.toByteArray();
                        InputStreamReader isr = new InputStreamReader(
                            new ByteArrayInputStream(inBytes), "utf-8");

                        BufferedReader br = new BufferedReader(isr);
                        String s = br.readLine();
                        while (s != null && !(s.startsWith("--"))) {
                            s = br.readLine();
                        }

                        if (s != null) {
                            type = "multipart/related; type=\"text/xml\";"
                                + " boundary=\"";
                            type = type + s.substring(2).trim() + "\"";
                        }
                        mimeHeaders.setHeader(CONTENT_TYPE, type);
                        soapMessage = messageFactory.createMessage(
                            mimeHeaders, new ByteArrayInputStream(inBytes));
                    }
                    else {
                        mimeHeaders.setHeader(CONTENT_TYPE, type);
                        soapMessage = messageFactory.createMessage(
                            mimeHeaders, messages[i].getInputStream());
                    }
                    byte[] soapEnvelopeBytes
                            = MessageServer.getSoapEnvelopeBytesFromStream(
                                    messages[i].getInputStream());
                    EbxmlMessage ebxmlMessage = new EbxmlMessage(soapMessage);
                    ebxmlMessage.setSoapEnvelopeBytes(soapEnvelopeBytes);
                    receivedMessages.add(new ReceivedMessage(ebxmlMessage,
                        isSmimeEncrypted));
                    //ebxmlMessages.add(ebxmlMessage);
                    currentMessage.setFlag(Flags.Flag.DELETED, true);
                }
                catch (KeyStoreException e) {
                    logger.error(ErrorMessages.getMessage(
                        ErrorMessages.ERR_PKI_INVALID_KEYSTORE, e));
                }
                catch (NoSuchAlgorithmException e) {
                    logger.error(ErrorMessages.getMessage(
                        ErrorMessages.ERR_PKI_CANNOT_DECRYPT, e));
                }
                catch (UnrecoverableKeyException e) {
                    logger.error(ErrorMessages.getMessage(
                        ErrorMessages.ERR_PKI_CANNOT_DECRYPT, e));
                }
                catch (SMIMEException e) {
                    logger.error(ErrorMessages.getMessage(
                        ErrorMessages.ERR_PKI_CANNOT_DECRYPT, e));
                }
                catch (IOException e) {
                    logger.debug("Cannot process message number " 
                        + messageNumber + ": " + e.getMessage());
                    /* uncomment the following line if unexpected messages
                       should be deleted from the mail box */
                    // messages[i].setFlag(Flags.Flag.DELETED, true);
                }
                catch (SOAPException e) {
                    logger.debug("Cannot process message number " 
                        + messageNumber + ": " + e.getMessage());
                    /* uncomment the following line if unexpected messages
                       should be deleted from the mail box */
                    // messages[i].setFlag(Flags.Flag.DELETED, true);
                }
                catch (Exception e) {
                    logger.error(ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e));
                }
            }
            folder.close(true);
            store.close();

            //final int length = ebxmlMessages.size();
            final int length = receivedMessages.size();
            /*
            final EbxmlMessage[] results = new EbxmlMessage[length];
            final Object[] objects = ebxmlMessages.toArray();
            for (int i=0 ; i<length ; i++) {
                results[i] = (EbxmlMessage) objects[i];
            }
            */
            final ReceivedMessage[] results = new ReceivedMessage[length];
            final Object[] objects = receivedMessages.toArray();
            for (int i=0 ; i<length ; i++) {
                results[i] = (ReceivedMessage) objects[i];
            }

            logger.debug("<= Mail.receive");
            return results;
        }
        catch (NoSuchProviderException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_POP_INVALID_SERVER, e, 
                "Protocol: " + protocol));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_POP_INVALID_SERVER));
        }
        catch (MessagingException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_POP_INVALID_SERVER, e, 
                "Server: " + smtpHost + ", Port: " + port + ", Folder: " 
                + folderName + ", User: " + username));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_POP_INVALID_SERVER));
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new TransportException(err);
        }
    }

    private static MimeMessage createSmimeMessage(EbxmlMessage ebxmlMessage,
        String toMshUrl, String smtpHost, String smtpUser, String smtpPass,
        X509Certificate certificate) throws TransportException {
        SendMailMessage sendMailMessage = createSendMailMessage(ebxmlMessage,
            toMshUrl, smtpHost, smtpUser, smtpPass);
        try {
            return SMIMEEncrypter.createEncryptedMimeMessage(
                certificate, sendMailMessage.getMimeMessage(),
                sendMailMessage.getSession());
        } catch (SMIMEException e) {
            throw new TransportException("Cannot encrypt message due to : "
                + e.toString());
        }
    }

    private static MimeMessage createSmimeMessage(EbxmlMessage ebxmlMessage,
        String toMshUrl, String smtpHost, String smtpUser, String smtpPass,
        String smimeEncryptionLocation, String smimeEncryptionAlias,
        String smimeEncryptionPassword) throws TransportException {
        SendMailMessage sendMailMessage = createSendMailMessage(ebxmlMessage,
            toMshUrl, smtpHost, smtpUser, smtpPass);
        try {
            return SMIMEEncrypter.createEncryptedMimeMessage(
                smimeEncryptionLocation, smimeEncryptionPassword,
                smimeEncryptionAlias, sendMailMessage.getMimeMessage(),
                sendMailMessage.getSession());
        } catch (SMIMEException e) {
            throw new TransportException("Cannot encrypt message due to : "
                + e.toString());
        }
    }

    private static SendMailMessage createSendMailMessage(
        EbxmlMessage ebxmlMessage, String toMshUrl, String smtpHost,
        String smtpUser, String smtpPass) throws TransportException {
        try {
            if (smtpHost == null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_SMTP_INVALID_SERVER, smtpHost);
                logger.error(err);
                throw new TransportException(err);
            }
    
            final Properties properties = new Properties();
            properties.put(PROPERTY_JAVA_MAIL_SMTP_HOST, smtpHost);
            if (smtpPort != null) {
                properties.put(PROPERTY_JAVA_MAIL_SMTP_HOST_PORT, smtpPort);
            }
    
            Session session;
            if (smtpUser != null && smtpPass != null) {
                properties.put(PROPERTY_JAVA_MAIL_AUTH, "true");
                AuthObject auth = new AuthObject(smtpUser, smtpPass);
                session = Session.getInstance(properties, auth);
            }
            else {
                session = Session.getInstance(properties);
            }
            session.setDebug(debug);
    
            final boolean hasAttachments = ebxmlMessage.getPayloadCount() > 0;
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            String contentType = null;
            if (hasAttachments) {
                contentType = Constants.MULTIPART_RELATED_TYPE + "\"" +
                    EbxmlMessage.mimeBoundary + "\"; " +
                    Constants.CHARACTER_SET + "=\"" +
                    Constants.CHARACTER_ENCODING + "\"; " + Constants.START +
                    "=\"<" + EbxmlMessage.SOAP_PART_CONTENT_ID + ">\"";
                ebxmlMessage.writeTo(out, MAIL_TRANSFER_ENCODING,
                                     MAIL_TRANSFER_ENCODING);
            }
            else {
                contentType = Constants.TEXT_XML_TYPE + "; " +
                    Constants.CHARACTER_SET + "=\"" +
                    Constants.CHARACTER_ENCODING + "\"";
                ebxmlMessage.writeTo(out);
            }
            final AttachmentDataSource content =
                new AttachmentDataSource(out.toByteArray(), contentType);
            MimeMessage mimeMessage = new MimeMessage(session);
            MimeMultipart multipart = null;
            if (hasAttachments) {
                multipart = new MimeMultipart(content);
            }
            else {
                mimeMessage.setDataHandler(new DataHandler(content));
                mimeMessage.setHeader(Constants.CONTENT_TRANSFER_ENCODING,
                                      MAIL_TRANSFER_ENCODING);
            }
            String fromPartyId = ((MessageHeader.PartyId) ebxmlMessage.
                                  getFromPartyIds().next()).getId();
            if (fromPartyId.startsWith(MAIL_PREFIX)) {
                fromPartyId = fromPartyId.substring
                    (MAIL_PREFIX.length(), fromPartyId.length());
            }
    
            mimeMessage.setFrom(new InternetAddress(fromPartyId));
            mimeMessage.setRecipient(Message.RecipientType.TO,
                                     new InternetAddress(toMshUrl));
            mimeMessage.setSubject(ebxmlMessage.getConversationId());
            /* some mail servers cannot receive messages correctly
               if the sent date is set */
            // mimeMessage.setSentDate(new Date());
    
            mimeMessage.setHeader(SOAP_ACTION, EBXML_SOAP_ACTION);
            if (multipart != null) {
                mimeMessage.setContent(multipart);
            }
    
            mimeMessage.saveChanges();
            return new SendMailMessage(mimeMessage, session);
        } catch (IOException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SERIALIZE, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SERIALIZE));
        } catch (SOAPException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_SAVE_OBJECT));
        } catch (MessagingException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_COMPOSE_MESSAGE, e));
            throw new TransportException(ErrorMessages.getMessage(
                ErrorMessages.ERR_SMTP_CANNOT_COMPOSE_MESSAGE));
        }
    } 

    static class AuthObject extends Authenticator {
        String user;
        String pass;
    
        AuthObject(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, pass);
        }
    }
    /**
     * A class represented a received message. It include the message and
     * the flag on whether it is encrypted.
     * There is a discussion on whether we should add isEncrypted flag to
     * EbxmlMessage, rather than wrapping the class like that. However, we
     * choose to wrap it because those information will be lost after the
     * serialization/deserialization of the message. In order to make it
     * more consistence for using Mail.receive() and receive behaviour through
     * Client, a wrapper class is used.
     * @author pykoon
     */
    public static class ReceivedMessage {
        /**
         * the ebxml message.
         */
        private EbxmlMessage message;
        /**
         * whether it is encrypted.
         */
        private boolean encrypted;
        /**
         * Construct the Received Message.
         * @param message The ebxmlMessage received. (Decrypted)
         * @param encrypted whether it is encrypted.
         */
        public ReceivedMessage(EbxmlMessage message, boolean encrypted) {
            this.message = message;
            this.encrypted = encrypted;
        }

        /**
         * get the message.
         * @return the message.
         */
        public EbxmlMessage getMessage() {
            return message;
        }

        /**
         * get whether it is encrypted.
         * @return whether it is encrypted.
         */
        public boolean isSMIMEEncrypted() {
            return encrypted;
        }
    }
    
    private static class SendMailMessage {
        private MimeMessage message;
        private Session session;
        public SendMailMessage(MimeMessage message, Session session) {
            this.message = message;
            this.session = session;
        }
        
        public MimeMessage getMimeMessage() {
            return message;
        }
        
        public Session getSession() {
            return session;
        }
    }
}

