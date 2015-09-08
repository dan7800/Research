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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/MessageProcessor.java,v 1.56 2004/04/02 06:02:41 bobpykoon Exp $
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
import hk.hku.cecid.phoenix.message.handler.MessageServiceHandler.Delivery;
import hk.hku.cecid.phoenix.message.packaging.AckRequested;
import hk.hku.cecid.phoenix.message.packaging.AttachmentDataSource;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.ErrorList;
import hk.hku.cecid.phoenix.message.packaging.MessageHeader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Logger;

/**
 * <code>MessageProcessor</code> sends an <code>EbxmlMessage</code> to the
 * receiving <code>MessageServiceHandler</code> in a non-blocking manner.
 * If the message cannot be sent due to transport layer error,
 * <code>MessageProcessor</code> waits for at least <code>retryInterval</code>
 * and resends the message for at most <code>retries</code> times.
 *
 * @author cyng
 * @version $Revision: 1.56 $
 */
class MessageProcessor extends Thread {

    static Logger logger = Logger.getLogger(MessageProcessor.class);

    private static ToUrlResolver toUrlResolver;

    private static boolean positiveAck;

    private static boolean augmented;

    private final EbxmlMessage ebxmlMessage;

    private final String transportType;

    private final URL toMshUrl;

    private final int retries;

    private final long retryInterval;

    private final MessageServiceHandler msh;

    private final MessageServer messageServer;
    
    private final MessageServiceHandlerConfig mshConfig;

    private int currentTry;

    private long nextRetryTime;

    private long latency;

    private boolean ackReceived = false;

    private int shutDown = MessageServiceHandler.NORMAL_LEVEL;

    private MessageSender messageSender;

    /**
     * msh config which will be unregistered when send complete.
     */
    private MessageServiceHandlerConfig unregisterMshConfig;

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

        String s = prop.get(Constants.PROPERTY_TO_URL_RESOLVER);
        try {
            toUrlResolver = (s == null ? null :
                             (ToUrlResolver) Class.forName(s).newInstance());
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage
                (ErrorMessages.ERR_HERMES_INIT_ERROR, e.getMessage());
            logger.error(err);
            throw new InitializationException(err);
        }
        s = prop.get(Constants.PROPERTY_POSITIVE_ACKNOWLEDGMENT);
        positiveAck = ((s != null && s.toLowerCase().equals("true")) ?
                       true : false);
        s = prop.get(Constants.PROPERTY_AUGMENTED_ERROR_MESSAGE);
        augmented = ((s != null && s.toLowerCase().equals("true")) ?
                     true : false);

        isConfigured = true;
    }

    MessageProcessor(EbxmlMessage ebxmlMessage,
                     MessageServiceHandlerConfig mshConfig,
                     MessageServiceHandler msh) 
        throws MessageServiceHandlerException {
        this(ebxmlMessage, mshConfig, msh, 0, 0);
    }

    MessageProcessor(EbxmlMessage ebxmlMessage,
                     MessageServiceHandlerConfig mshConfig,
                     MessageServiceHandler msh, int startTry, long latency) 
        throws MessageServiceHandlerException {

        logger.debug("=> MessageProcessor.MessageProcessor");

        this.ebxmlMessage = ebxmlMessage;
        this.msh = msh;
        this.messageServer = msh.getMessageServer();
        this.mshConfig = mshConfig;
        this.currentTry = startTry;
        this.nextRetryTime = 0;
        this.latency = latency;
        URL url = (toUrlResolver == null ? mshConfig.getToMshUrl() :
                   toUrlResolver.resolve(ebxmlMessage.getMessageHeader()));
        if (url == null) {
            toMshUrl = mshConfig.getToMshUrl();
            transportType = mshConfig.getTransportType();
        }
        else if (url.getProtocol().toLowerCase().startsWith
                 (Constants.TRANSPORT_TYPE_MAIL.toLowerCase())) {
            toMshUrl = url;
            transportType = Constants.TRANSPORT_TYPE_MAIL;
        }
        else if (url.getProtocol().equalsIgnoreCase
                 (Constants.TRANSPORT_TYPE_HTTP)) {
            toMshUrl = url;
            transportType = Constants.TRANSPORT_TYPE_HTTP;
        }
        else if (url.getProtocol().equalsIgnoreCase
                 (Constants.TRANSPORT_TYPE_HTTPS)) {
            toMshUrl = url;
            transportType = Constants.TRANSPORT_TYPE_HTTPS;
        }
        else {
            toMshUrl = mshConfig.getToMshUrl();
            transportType = mshConfig.getTransportType();
        }
        retries = mshConfig.getRetries();
        retryInterval = Long.parseLong(mshConfig.getRetryInterval());

        if (toMshUrl == null) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_DESTINATION,
                "Cannot resolve outgoing URL to send");
            logger.warn(err);
            throw new MessageServiceHandlerException(err);
        }

        logger.debug("<= MessageProcessor.MessageProcessor");
    }

    public void run() {

        logger.debug("=> MessageProcessor.run");
 
        final String messageId = ebxmlMessage.getMessageId();
        final AckRequested ackRequested = ebxmlMessage.getAckRequested();
        final boolean isAppMessage = ebxmlMessage.getAcknowledgment() == null &&
            ebxmlMessage.getErrorList() == null &&
            ebxmlMessage.getStatusResponse() == null &&
            !(ebxmlMessage.getService().equals(Constants.SERVICE) &&
            ebxmlMessage.getAction().equals(Constants.ACTION_PONG));
        messageSender = null;
        boolean commError = false;

        if (latency > 0) {
            final long startTime = System.currentTimeMillis();
            long endTime = startTime;
            while ((endTime - startTime) < latency) {
                try {
                    idle(latency - (endTime - startTime));
                    break;
                }
                catch (InterruptedException ie2) {
                }
                endTime = System.currentTimeMillis();
            }
        }

        for ( ; shutDown == MessageServiceHandler.NORMAL_LEVEL &&
                  !ackReceived && currentTry<retries ; currentTry++) {
            logger.debug("Send... try #" + (currentTry + 1));
            commError = false;
            nextRetryTime = 0;

            if (transportType.equals(Constants.TRANSPORT_TYPE_MAIL)) {
                messageSender = new MailSender(ebxmlMessage, toMshUrl, this);
            }
            else if (transportType.equals(Constants.TRANSPORT_TYPE_HTTP) ||
                     transportType.equals(Constants.TRANSPORT_TYPE_HTTPS)) {
                messageSender = new HttpSender(ebxmlMessage, toMshUrl, this);
            }
            messageSender.setDaemon(true);

            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                tx.lock(messageId);
                messageServer.retry(messageId, currentTry+1, retryInterval, tx);
                tx.commit();
            }
            catch (MessageServerException e) {
                try {
                    tx.rollback();
                }
                catch (Throwable e2) {}
                commError = true;
            }
            catch (Throwable e) {
                try {
                    tx.rollback();
                }
                catch (Throwable e2) {}
                commError = true;
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
            }
            finally {
                tx = null;
            }

            if (ackReceived) {
                break;
            }

            final long startTime = System.currentTimeMillis();
            if (!commError) {
                messageSender.start();
            }
            else {
                nextRetryTime = startTime + retryInterval;
            }

            try {
                idle(retryInterval);
                if (!commError) {
                    tx = new Transaction(MessageServer.dbConnectionPool);
                    try {
                        messageServer.logSentMessage(ebxmlMessage, 
                            messageSender.getExceptionMessage(), tx);
                        tx.commit();
                    }
                    catch (MessageServerException e) {
                        try {
                            tx.rollback();
                        }
                        catch (Throwable e2) {}
                    }
                    catch (Throwable e) {
                        try {
                            tx.rollback();
                        }
                        catch (Throwable e2) {}
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                        logger.error(err);
                    }
                    finally {
                        tx = null;
                    }
                }

                if (messageSender.sendSuccessful() && messageSender.
                    getExceptionMessage() == null) {
                    if (ackRequested ==  null || ackReceived) {
                        tx = new Transaction(MessageServer.dbConnectionPool);
                        try {
                            tx.lock(messageId);
                            if (ackRequested == null) {
                                messageServer.retry(messageId, MessageServer.
                                                    STATE_SENT, -1, tx);
                            }
                            else {
                                messageServer.retry(messageId, MessageServer.
                                                    STATE_ACKNOWLEDGED, -1, tx);
                            }
                            tx.commit();
                        }
                        catch (Throwable e) {
                            commError = true;
                            String msg = "Cannot send message successfully "
                                + "for " + String.valueOf(currentTry+1) 
                                + " times: " + e.getMessage();
                            try {
                                tx.rollback();
                            }
                            catch (Throwable e2) {}
                            finally {
                                tx = null;
                            }
                            throw new InterruptedException(msg);
                        }
                        finally {
                            tx = null;
                        }
                    }

                    final EbxmlMessage responseMessage = messageSender.
                        getResponseMessage();
                    if (responseMessage != null) {
                        final HashMap requestProperty = new HashMap();
                        requestProperty.put(DbTableManager.
                            ATTRIBUTE_REMOTE_ADDRESS, "Sync Response");
                        requestProperty.put(DbTableManager.
                            ATTRIBUTE_REMOTE_HOST, toMshUrl.getHost());
                        try {
                            final EbxmlMessage responseMessage2 = msh.
                                onMessage(responseMessage, requestProperty);
                            if (responseMessage2 != null) {
                                msh.sendMessage(mshConfig.
                                    getApplicationContext(), responseMessage2);
                            }
                        }
                        catch (MessageServiceHandlerException mshe) {
                            logger.error(mshe.getMessage());
                        }
                    }
                    else if (ackRequested != null && mshConfig.getSyncReply()
                             == Constants.SYNC_REPLY_MODE_MSH_SIGNALS_ONLY) {
                        String msg = "Cannot send message successfully for " +
                            (currentTry+1) + " times: Acknowledgment not " +
                            "received synchronously but syncReplyMode=" +
                            "\"mshSignalsOnly\"";
                        throw new InterruptedException(msg);
                    }

                    if (ackRequested ==  null || ackReceived) {
                        if (positiveAck) {
                            if (isAppMessage) {
                                generatePositiveAcknowledgment();
                            }
                        }
                        nextRetryTime = -1;
                        msh.removeSendThread(messageId);
                        if (unregisterMshConfig != null) {
                            unregisterMshConfig();
                        }
                        logger.debug("<= MessageProcessor.run");
                        return;
                    }
                    else if (mshConfig.getSyncReply() ==
                             Constants.SYNC_REPLY_MODE_MSH_SIGNALS_ONLY) {
                        String msg = "Cannot send message successfully for " +
                            (currentTry+1) + " times: Non-Acknowledgment " +
                            "received synchronously but syncReplyMode=" +
                            "\"mshSignalsOnly\"";
                        throw new InterruptedException(msg);
                    }

                    long endTime = System.currentTimeMillis();
                    while ((endTime - startTime) < retryInterval) {
                        try {
                            nextRetryTime = -(startTime + retryInterval);
                            idle(retryInterval - (endTime - startTime));
                            break;
                        }
                        catch (InterruptedException ie) {}
                        endTime = System.currentTimeMillis();
                    }

                    if (shutDown == MessageServiceHandler.NORMAL_LEVEL &&
                        !ackReceived) {
                        String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_STATE_ERROR,
                            "Acknowledgment not received");
                        logger.warn(err);
                    }
                }
                else {
                    if (ackReceived) {
                        logger.debug("Acknowledgment is received before the"
                            + "current thread is waken up by MessageSender!");
                        long endTime = System.currentTimeMillis();
                        if ((endTime - startTime) < retryInterval) {
                            try {
                                idle(retryInterval - (endTime - startTime));
                            }
                            catch (InterruptedException ie) {}
                        }

                        if (messageSender.sendSuccessful() &&
                            messageSender.getExceptionMessage() == null) {
                            if (positiveAck) {
                                generatePositiveAcknowledgment();
                            }
                            nextRetryTime = -1;
                            msh.removeSendThread(messageId);
                            logger.debug("<= MessageProcessor.run");
                            if (unregisterMshConfig != null) {
                                unregisterMshConfig();
                            }
                            return;
                        }
                    }
                    commError = true;

                    String msg = messageSender.getExceptionMessage();
                    if (msg == null) {
                        msg = "Too long time is taken to send a message.";
                    }
                    throw new InterruptedException(messageSender.getClass().
                        getName() + " cannot send message successfully for "
                        + String.valueOf(currentTry+1) + " times: " + msg);
                }
            }
            catch (InterruptedException ie) {
                logger.debug(ie.getMessage());

                if (!isAppMessage) {
                    currentTry = retries - 1;
                }

                if (currentTry == (retries - 1) || isShutDown()) {
                    break;
                }
                long endTime = System.currentTimeMillis();
                while ((endTime - startTime) < retryInterval) {
                    try {
                        nextRetryTime = startTime + retryInterval;
                        idle(retryInterval - (endTime - startTime));
                        break;
                    }
                    catch (InterruptedException ie2) {
                    }
                    endTime = System.currentTimeMillis();
                }
            }
        }

        if (shutDown == MessageServiceHandler.HALT_SUSPEND_LEVEL) {
            nextRetryTime = -1;
            msh.removeSendThread(messageId);
            logger.debug("<= MessageProcessor.run");
            return;
        }

        // test if the message has been acknowledged or not
        if (!ackReceived) {
            if (commError) {
                if (isAppMessage) {
                    generateError(ErrorList.CODE_DELIVERY_FAILURE,
                        ErrorList.SEVERITY_ERROR, "Communication error");
                }
            }
            else {
                // acknowledgment not yet received
                generateError(ErrorList.CODE_DELIVERY_FAILURE,
                              ErrorList.SEVERITY_WARNING,
                              "Cannot receive acknowledgment");
            }

            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                tx.lock(messageId);
                if (shutDown == MessageServiceHandler.HALT_DELETE_LEVEL) {
                    messageServer.retry(messageId, 
                        MessageServer.STATE_DELETED, -1, tx);
                }
                else {
                    messageServer.retry(messageId, 
                        MessageServer.STATE_SENT_FAILED, -1, tx);
                }
                tx.commit();
            }
            catch (Throwable t) {
                logger.error(t.getMessage());
                try {
                    tx.rollback();
                }
                catch (Throwable t2) {}
            }
            finally {
                tx = null;
            }
        }
        else if (commError || currentTry == 0) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                "Acknowledgment is received for an unsuccessfully "
                + "sent message");
            logger.warn(err);
        }
        else if (positiveAck) {
            generatePositiveAcknowledgment();
        }
        msh.removeSendThread(messageId);
        if (unregisterMshConfig != null) {
            unregisterMshConfig();
        }
        nextRetryTime = -1;

        logger.debug("<= MessageProcessor.run");
    }
    
    private void unregisterMshConfig() {
        try {
            msh.unregister(unregisterMshConfig, true);
        } catch (MessageServiceHandlerException e) {
            logger.error("Cannot unregister the msh config.");
        }
    }

    synchronized void wakeUp(boolean ackReceived) {
        logger.debug("=> MessageProcessor.wakeUp");

        this.ackReceived = ackReceived;
        this.notify();

        logger.debug("<= MessageProcessor.wakeUp");
    }

    synchronized void shutDown(int level) {
        logger.debug("=> MessageProcessor.shutDown");
    
        shutDown = level;
        this.notifyAll();

        logger.debug("<= MessageProcessor.shutDown");
    }

    long nextRetryTime() {
        return nextRetryTime;
    }

    boolean isWaitingRetry() {
        if (messageSender == null) {
            return true;
        }
        return !(messageSender.sendSuccessful() || ackReceived);
    }

    private synchronized void idle(long interval)
        throws InterruptedException {
        this.wait(interval);
    }

    /**
     * Generate the positive acknowledgment and delivery it to the corresponding
     * client. The corresponding client to receive the postive acknowledgment
     * should be the one registered the message's cpaid, conversation id,
     * Constants.SERVICE and Constants.STATUS_RESPONSE. If such application
     * context is not registered, error message will be logged, and the message
     * will be supprssed.
     */
    private void generatePositiveAcknowledgment() {

        logger.debug("=> MessageProcessor.generatePositiveAcknowledgment");

        try {
            final EbxmlMessage positiveAckMessage = new EbxmlMessage();
            final String fromPartyId = ((MessageHeader.PartyId)
                ebxmlMessage.getToPartyIds().next()).getId();
            final String toPartyId = ((MessageHeader.PartyId)
                ebxmlMessage.getFromPartyIds().next()).getId();
            final Date date = new Date();
            final String timeStamp = Utility.toUTCString(date);
            final String messageId = Utility.generateMessageId
                (date, toPartyId, ebxmlMessage.getCpaId(),
                 Constants.SERVICE, Constants.ACTION_STATUS_RESPONSE);
            logger.debug("new message id: <" + messageId + ">");
            positiveAckMessage.addMessageHeader(fromPartyId, toPartyId,
                ebxmlMessage.getCpaId(), ebxmlMessage.getConversationId(),
                Constants.SERVICE, Constants.ACTION_STATUS_RESPONSE,
                messageId, timeStamp);
            positiveAckMessage.getMessageHeader().
                setRefToMessageId(ebxmlMessage.getMessageId());
            positiveAckMessage.addStatusResponse(ebxmlMessage.getMessageId(),
                Constants.STATUS_PROCESSED);
            /*
            // Commented due to the roll back on Send without registration.
            // may be uncommented for reason to support back it again.
            //
            ApplicationContext appContext = new ApplicationContext(
                ebxmlMessage.getCpaId(), ebxmlMessage.getConversationId(),
                Constants.SERVICE, Constants.ACTION_STATUS_RESPONSE);
            MessageServiceHandlerConfig receiverMSHConfig
                = msh.getMessageServiceHandlerConfig(appContext);
            if (receiverMSHConfig == null) {
                logger.warn("Application Context is not registered "
                    + "for receiving positive ack message :"
                    + appContext.toString());
                logger.warn("Positive ack message suppressed");
            } else {
                Transaction tx = new Transaction(
                    MessageServer.dbConnectionPool);
                try {
                    messageServer.store(positiveAckMessage, appContext,
                        MessageServer.STATE_RECEIVED, false, tx);
                    MessageServiceHandler.Delivery delivery
                        = new MessageServiceHandler.Delivery(msh, appContext,
                            receiverMSHConfig.getMessageListener(),
                            positiveAckMessage);
                    delivery.start();
                    tx.commit();
                }
                catch (MessageServerException e) {
                    try {
                        tx.rollback();
                    }
                    catch (Exception e2) {}
                    throw e;
                }
                catch (Throwable e) {
                    try {
                        tx.rollback();
                    }
                    catch (Exception e2) {}
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                    logger.error(err);
                    throw new Exception(err);
                }
            }
            */
            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                messageServer.store(positiveAckMessage, mshConfig.getApplicationContext(), MessageServer.STATE_RECEIVED, false, tx);
                MessageServiceHandler.Delivery delivery = new MessageServiceHandler.Delivery(msh, mshConfig.getApplicationContext(), mshConfig.getMessageListener(), positiveAckMessage);
                delivery.start();
                tx.commit();
            }
            catch (MessageServerException e) {
                try {
                    tx.rollback();
                }
                catch (Exception e2) {}
                throw e;
            }
            catch (Throwable e) {
                try {
                    tx.rollback();
                }
                catch (Exception e2) {}
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new Exception(err);
            }
        }
        catch (Exception e) {
            logger.debug("cannot generate positive acknowledgment message");
        }

        logger.debug("<= MessageProcessor.generatePositiveAcknowledgment");
    }

    /**
     * Generate the error message and delivery it to the corresponding
     * client. The corresponding client to receive the error message
     * should be the one registered the message's cpaid, conversation id,
     * Constants.SERVICE and Constants.ACTION_MESSAGE_ERROR. If such application
     * context is not registered, error message will be logged, and the message
     * will be supprssed.
     */
    private void generateError(String errorCode, String severity,
                               String description) {

        logger.debug("=> MessageProcessor.generateError");

        try {
            // generate new EbxmlMessage containing the ErrorList
            EbxmlMessage errorMessage = new EbxmlMessage();

            String fromPartyId = ((MessageHeader.PartyId)
                ebxmlMessage.getFromPartyIds().next()).getId();
            String fromPartyType = ((MessageHeader.PartyId)
                ebxmlMessage.getFromPartyIds().next()).getType();
            String toPartyId = ((MessageHeader.PartyId)
                ebxmlMessage.getToPartyIds().next()).getId();
            String toPartyType = ((MessageHeader.PartyId)
                ebxmlMessage.getToPartyIds().next()).getType();
            Date date = new Date();
            String timeStamp = Utility.toUTCString(date);
            String messageId = Utility.generateMessageId(date, ebxmlMessage);
            errorMessage.addMessageHeader(toPartyId, toPartyType, 
                fromPartyId, fromPartyType, ebxmlMessage.getCpaId(), 
                ebxmlMessage.getConversationId(), Constants.SERVICE,
                Constants.ACTION_MESSAGE_ERROR, messageId, 
                timeStamp).setRefToMessageId(ebxmlMessage.getMessageId());
            errorMessage.addErrorList(errorCode, severity, description);
            errorMessage.saveChanges();

            if (augmented) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                ebxmlMessage.writeTo(out);
                final byte[] bytes = out.toByteArray();
                final InputStreamReader inputStreamReader =
                    new InputStreamReader(new ByteArrayInputStream(bytes),
                        Constants.CHARACTER_ENCODING);
                final LineNumberReader lineReader =
                    new LineNumberReader(inputStreamReader);
                String s = lineReader.readLine();
                while (s != null && !(s.startsWith(Constants.
                                                   MIME_BOUNDARY_PREFIX))) {
                    s = lineReader.readLine();
                }
                lineReader.close();
                inputStreamReader.close();

                final String contentType;
                if (s == null) {
                    contentType = Constants.TEXT_XML_TYPE;
                }
                else {
                    contentType = Constants.MULTIPART_RELATED_TYPE
                        + "\"" + s.substring(Constants.
                        MIME_BOUNDARY_PREFIX.length()) + "\"";
                }

                final SOAPMessage soapMessage = errorMessage.getSOAPMessage();
                final DataHandler originalMessage = new DataHandler
                    (new AttachmentDataSource(bytes, contentType));
                soapMessage.addAttachmentPart
                    (soapMessage.createAttachmentPart(originalMessage));
            }
            /*
            // Commented due to the roll back on Send without registration.
            // may be uncommented for reason to support back it again.
            //
            ApplicationContext appContext = new ApplicationContext(
                ebxmlMessage.getCpaId(), ebxmlMessage.getConversationId(),
                Constants.SERVICE, Constants.ACTION_MESSAGE_ERROR);
            MessageServiceHandlerConfig receiverMSHConfig
                = msh.getMessageServiceHandlerConfig(appContext);
            if (receiverMSHConfig == null) {
                logger.warn("Application Context is not registered "
                    + "for receiving error message :" + appContext.toString());
                logger.warn("Error message suppressed");
            } else {
                Transaction tx = new Transaction(
                    MessageServer.dbConnectionPool);
                try {
                    messageServer.store(errorMessage, appContext,
                        MessageServer.STATE_RECEIVED, false, tx);
                    MessageServiceHandler.Delivery delivery
                        = new MessageServiceHandler.Delivery(msh, appContext,
                            receiverMSHConfig.getMessageListener(),
                            errorMessage);
                    tx.addThread(delivery);
                    tx.commit();
                } catch (MessageServerException e) {
                    try {
                        tx.rollback();
                    } catch (Exception e2) {}
                    throw e;
                } catch (Throwable e) {
                    try {
                        tx.rollback();
                    } catch (Exception e2) {}
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                    logger.error(err);
                    throw new Exception(err);
                }
            }
            */
            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                messageServer.store(errorMessage, getApplicationContext(),
                    MessageServer.STATE_RECEIVED, false, tx);
                MessageServiceHandler.Delivery delivery
                    = new MessageServiceHandler.Delivery(msh,
                        getApplicationContext(),
                        mshConfig.getMessageListener(),
                        errorMessage);
                tx.addThread(delivery);
                tx.commit();
            } catch (MessageServerException e) {
                try {
                    tx.rollback();
                } catch (Exception e2) {}
                throw e;
            } catch (Throwable e) {
                try {
                    tx.rollback();
                } catch (Exception e2) {}
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new Exception(err);
            }
        }
        catch (Exception e) {
            logger.debug("cannot generate error message");
        }

        logger.debug("<= MessageProcessor.generateError");
    }

    ApplicationContext getApplicationContext() {
        return mshConfig.getApplicationContext();
    }

    boolean isShutDown() {
        return shutDown != MessageServiceHandler.NORMAL_LEVEL;
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
