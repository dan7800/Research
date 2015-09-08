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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/MessageServiceHandlerConnection.java,v 1.27 2004/02/10 03:12:53 bobpykoon Exp $
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

import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessageFactory;
import javax.xml.messaging.JAXMException;
import javax.xml.messaging.ProviderConnection;
import javax.xml.messaging.ProviderMetaData;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Logger;

/**
 * A client's active connection to <code>MessageServiceHandler</code>
 *
 * @author cyng
 * @version $Revision: 1.27 $
 */
public class MessageServiceHandlerConnection implements ProviderConnection {

    static Logger logger = Logger.getLogger
                           (MessageServiceHandlerConnection.class); 

    private static final String CONNECTION_CLOSED_MESSAGE =
        "This MessageServiceHandlerConnection already closed and should not "
        + "be used again!";

    private static MessageFactory messageFactory;

    /** Flag indicating if the class has been configured.  */
    protected static boolean isConfigured = false;

    /**
     * msh config which will be unregistered when send complete.
     */
    private MessageServiceHandlerConfig unregisterMshConfig;

    /** 
     * Configure the class if and only if it has not been configured.
     * 
     * @param prop <code>Property</code> object.
     */
    static synchronized void configure(Property prop) 
        throws InitializationException {

        if (isConfigured) return;

        try {
            messageFactory = MessageFactory.newInstance();
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_MESSAGE_FACTORY, e);
            logger.error(err);
            throw new InitializationException(err);
        }

        isConfigured = true;
    }

    private final MessageServiceHandler msh;

    private final MessageServiceHandlerConfig mshConfig;

    private final MessageServer messageServer;

    private boolean closed;

    MessageServiceHandlerConnection(MessageServiceHandler msh,
                                    MessageServiceHandlerConfig mshConfig)
        throws MessageServiceHandlerConnectionException {

        logger.debug("=> MessageServiceHandlerConnection."
            + "MessageServiceHandlerConnection");

        this.msh = msh;
        this.mshConfig = mshConfig;
        this.messageServer = msh.getMessageServer();
        closed = false;
        initialize();

        logger.debug("<= MessageServiceHandlerConnection."
            + "MessageServiceHandlerConnection");
    }

    public void close() throws JAXMException {
        logger.debug("=> MessageServiceHandlerConnection.close");

        if (closed) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_STATE_ERROR,
                CONNECTION_CLOSED_MESSAGE);
            logger.error(err);
            throw new JAXMException(err);
        }

        closed = true;
        logger.debug("<= MessageServiceHandlerConnection.close");
    }

    public MessageFactory createMessageFactory(String profile)
        throws JAXMException {

        logger.debug("=> MessageServiceHandlerConnection.createMessageFactory");
        
        if (closed) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_STATE_ERROR,
                CONNECTION_CLOSED_MESSAGE);
            logger.error(err);
            throw new JAXMException(err);
        }

        MessageFactory ret;
        if (profile == null || profile.equals("")) {
            if (messageFactory == null) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_SOAP_MESSAGE_FACTORY,
                    "default MessageFactory cannot be instantiated");
                logger.error(err);
                throw new JAXMException(err);
            }

            ret = messageFactory;
        }
        else {
            ret = 
                MessageServiceHandler.getMetaData().getMessageFactory(profile);
        }

        logger.debug("<= MessageServiceHandlerConnection.createMessageFactory");
        return ret;
    }

    public ProviderMetaData getMetaData() {
        return MessageServiceHandler.getMetaData();
    }

    public void send(SOAPMessage message) throws JAXMException {
        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            send(message, tx);
            tx.commit();
        }
        catch (Throwable t) {
            try {
                tx.rollback();
            }
            catch (Throwable t2) {}
            throw new JAXMException(t.getMessage());
        }
    }

    public void send(SOAPMessage message, Transaction tx) throws JAXMException {
        final EbxmlMessage ebxmlMessage;
        try {
            ebxmlMessage = new EbxmlMessage(message);
        }
        catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_SOAP_CANNOT_CREATE_OBJECT, e);
            logger.error(err);
            throw new JAXMException(err);
        }

        try {
            send(ebxmlMessage, tx);
        }
        catch (MessageServiceHandlerConnectionException mshce) {
            throw new JAXMException(mshce.getMessage());
        }
    }

    public void send(EbxmlMessage ebxmlMessage)
        throws MessageServiceHandlerConnectionException {
        Transaction tx = new Transaction(MessageServer.dbConnectionPool);
        try {
            send(ebxmlMessage, tx);
            tx.commit();
        }
        catch (Throwable t) {
            String message = t.getMessage();
            try {
                tx.rollback();
            }
            catch (TransactionException te) {}
            throw new MessageServiceHandlerConnectionException(message);
        }
    }

    public void send(EbxmlMessage ebxmlMessage, Transaction tx)
        throws MessageServiceHandlerConnectionException {

        logger.debug("=> MessageServiceHandlerConnection.send");

        if (closed) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_STATE_ERROR,
                CONNECTION_CLOSED_MESSAGE);
            logger.error(err);
            throw new MessageServiceHandlerConnectionException(err);
        }

        try {
            /*
            boolean stored = (ebxmlMessage.getFileName() != null ?
                              true : false);
            try {
                messageServer.store(ebxmlMessage, mshConfig.
                    getApplicationContext(), MessageServer.STATE_SENT_STARTED,
                    true, tx);
            }
            catch (MessageServerException e) {
                throw e;
            }
            final MessageProcessor messageProcessor = (stored ?
                new MessageProcessor(ebxmlMessage, mshConfig, msh) :
                new MessageProcessor(MessageServer.getMessageFromFile
                    (new File(ebxmlMessage.getFileName())), mshConfig, msh));
            */
            String persistenceName = ebxmlMessage.getPersistenceName();
            try {
                messageServer.store(ebxmlMessage, mshConfig.
                    getApplicationContext(), MessageServer.STATE_SENT_STARTED,
                    true, tx);
            }
            catch (MessageServerException e) {
                throw e;
            }
            MessageProcessor messageProcessor;
            if (persistenceName != null) {
                messageProcessor = new MessageProcessor(
                        (EbxmlMessage) MessageServer.getMessageFromDataSource(
                                ebxmlMessage.getPersistenceHandler().getObject(
                                        persistenceName) , true),
                                                mshConfig, msh);
            } else {
                messageProcessor = new MessageProcessor(ebxmlMessage,
                        mshConfig, msh);
            }
            messageProcessor.setUnregisterMessageServiceHandlerConfig(
                unregisterMshConfig);
            msh.addSendThread(ebxmlMessage.getMessageId(), messageProcessor);
            //messageProcessor.start();
            tx.addThread(messageProcessor);
        }
        catch (MessageServiceHandlerException e) {
            throw new MessageServiceHandlerConnectionException(e.getMessage());
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServiceHandlerConnectionException(err);
        }

        logger.debug("<= MessageServiceHandlerConnection.send");
    }

    public EbxmlMessage getMessage()
        throws MessageServiceHandlerConnectionException {

        logger.debug("=> MessageServiceHandlerConnection.getMessage");

        try {
            EbxmlMessage ret =
                msh.getMessage(mshConfig.getApplicationContext(), null);

            logger.debug("<= MessageServiceHandlerConnection.getMessage");
            return ret;
        }
        catch (MessageServiceHandlerException mshe) {
            throw new MessageServiceHandlerConnectionException(
                mshe.getMessage());
        }
    }

    public EbxmlMessageFactory createMessageFactory()
        throws MessageServiceHandlerConnectionException {

        logger.debug("=> MessageServiceHandlerConnection.createMessageFactory");

        if (closed) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_STATE_ERROR,
                CONNECTION_CLOSED_MESSAGE);
            logger.error(err);
            throw new MessageServiceHandlerConnectionException(err);
        }

        EbxmlMessageFactory ret = new EbxmlMessageFactory();

        logger.debug("<= MessageServiceHandlerConnection.createMessageFactory");
        return ret;
    }

    MessageServiceHandlerConfig getMessageServiceHandlerConfig() {
        return mshConfig;
    }

    boolean isEnabled() {
        return (mshConfig != null && mshConfig.isEnabled());
    }

    private void initialize() throws MessageServiceHandlerConnectionException {
    }
    
    /**
     * set the unregister MSH Config.
     * such config will be unregister when the send message complete.
     * (Either ok or fail)
     * @param mshConfig
     */
    void setUnregisterMessageServiceHandlerConfig(
        MessageServiceHandlerConfig mshConfig) {
        unregisterMshConfig = mshConfig;
    }
}
