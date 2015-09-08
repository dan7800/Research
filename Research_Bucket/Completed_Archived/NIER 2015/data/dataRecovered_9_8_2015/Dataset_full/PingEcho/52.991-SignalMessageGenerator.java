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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/SignalMessageGenerator.java,v 1.3 2003/12/11 06:41:29 bobpykoon Exp $
 *
 * Code authored by:
 *
 * pykoon [2002-11-21]
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
import hk.hku.cecid.phoenix.message.packaging.Acknowledgment;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.MessageHeader;
import hk.hku.cecid.phoenix.message.packaging.Signature;
import hk.hku.cecid.phoenix.message.packaging.SignatureReference;
import java.util.Date;
import java.util.Iterator;
import javax.xml.soap.SOAPException;
/**
 * <code>SignalMessageGenerator</code> is an utility api for the user to
 * generate some signal message
 * @author pykoon
 * @version $Revision: 1.3 $
 */
public class SignalMessageGenerator {
   /**
     * Generate a simple response message containing required elements in
     * message header. They include:
     * <ul>
     * <li>From/PartyId [ebMSS 3.1.1]</li>
     * <li>To/PartyId [ebMSS 3.1.1]</li>
     * <li>CPAId [ebMSS 3.1.2].</li>
     * <li>ConversationId [ebMSS 3.1.3].</li>
     * <li>Service [ebMSS 3.1.4].</li>
     * <li>Action [ebMSS 3.1.5].</li>
     * <li>MessageId [ebMSS 3.1.6.1].</li>
     * <li>Timestamp [ebMSS 3.1.6.2].</li>
     * </ul>
     *
     * @param requestMessage    Request message for which a response message
     *                          shall be generated.
     * @param action            Name of the action.
     *
     * @return An {@link EbxmlMessage} that contains the fields mentioned
     *         above.
     *
     * @throws SOAPException
     */
    private static EbxmlMessage generateResponseMessage(
            EbxmlMessage requestMessage, String action) throws SOAPException {
        final EbxmlMessage responseMessage;
        responseMessage = new EbxmlMessage();
        final MessageHeader.PartyId fromParty = (MessageHeader.PartyId)
            requestMessage.getToPartyIds().next();
        final String fromPartyId = fromParty.getId();
        final String fromPartyIdType = fromParty.getType();
        final MessageHeader.PartyId toParty = (MessageHeader.PartyId)
            requestMessage.getFromPartyIds().next();
        final String toPartyId = toParty.getId();
        final String toPartyIdType = toParty.getType();
        final Date date = new Date();
        final String timeStamp = Utility.toUTCString(date);
        final String messageId = Utility.generateMessageId(date, toPartyId,
                requestMessage.getCpaId(), Constants.SERVICE, action);
        responseMessage.addMessageHeader(fromPartyId, fromPartyIdType,
            toPartyId, toPartyIdType, requestMessage.getCpaId(),
            requestMessage.getConversationId(), Constants.SERVICE, action, messageId,
            timeStamp);
        return responseMessage;
    }


    /**
     * Generates acknowledgement message from the given acknowledgement
     * request message and the refToMessageId.
     * Note that the acknowledgment message is not signed.
     *
     * @param ackRequestedMessage   Acknowledgement request message.
     * @param refToMessageId        MessageId of the message to which the
     *                              acknowledgement response should be referred.
     *
     * @return Acknowledgement message.
     *
     * @throws SOAPException
     */
    public static EbxmlMessage generateAcknowledgment(
            EbxmlMessage ackRequestedMessage, String refToMessageId)
                    throws SOAPException {
        final EbxmlMessage ackMessage = generateResponseMessage(
                ackRequestedMessage, Constants.ACTION_ACKNOWLEDGMENT);

        final MessageHeader messageHeader = ackMessage.getMessageHeader();
        messageHeader.setRefToMessageId(refToMessageId);
        if (ackRequestedMessage.getDuplicateElimination()) {
            messageHeader.setDuplicateElimination();
        }
        Iterator toParties = ackRequestedMessage.getToPartyIds();
        if (toParties.hasNext()) {
            MessageHeader.PartyId party = 
                (MessageHeader.PartyId) toParties.next();
            ackMessage.addAcknowledgment(messageHeader.getTimestamp(),
                ackRequestedMessage, party.getId(), party.getType());
        } else {
            /*
            ackMessage.addAcknowledgment(messageHeader.getTimestamp(),
                ackRequestedMessage, mshUrl);
             */
            throw new SOAPException(
                    "Missing To party Id on ack request message");
        }

        Iterator signatures = ackRequestedMessage.getSignatures();
        if (signatures.hasNext()) {
            Acknowledgment ack = ackMessage.getAcknowledgment();
            for (Iterator i=((Signature) signatures.next()).
                     getReferences() ; i.hasNext() ; ) {
                ack.addSignatureReference((SignatureReference) i.next());
            }
            ackMessage.getSOAPMessage().getSOAPPart().getEnvelope().
                addNamespaceDeclaration(Signature.NAMESPACE_PREFIX_DS,
                                        Signature.NAMESPACE_URI_DS);
            ackMessage.saveChanges();
        }
        return ackMessage;
    }

    /**
     * Generates acknowledgement message from the given acknowledgement
     * request message and the refToMessageId.
     * Note that the acknowledgment message is not signed.
     *
     * @param ackRequestedMessage   Acknowledgement request message.
     *
     * @return Acknowledgement message.
     *
     * @throws SOAPException
     */
    public static EbxmlMessage generateAcknowledgment(
            EbxmlMessage ackRequestedMessage) throws SOAPException {
        return generateAcknowledgment(ackRequestedMessage,
                ackRequestedMessage.getMessageId());
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
     * @throws SOAPException
     */
    public static EbxmlMessage generateStatusResponseMessage(
            EbxmlMessage statusRequestMessage, String status, String timestamp)
                    throws SOAPException {
        final EbxmlMessage statusResponseMessage = generateResponseMessage(
                statusRequestMessage, Constants.ACTION_STATUS_RESPONSE);
        statusResponseMessage.getMessageHeader().setRefToMessageId(
                statusRequestMessage.getMessageId());
        final String refToMessageId
                = statusRequestMessage.getStatusRequest().getRefToMessageId();
        if (status.equals(Constants.STATUS_UN_AUTHORIZED) ||
            status.equals(Constants.STATUS_NOT_RECOGNIZED)) {
            statusResponseMessage.addStatusResponse(refToMessageId, status);
        }
        else {
            long time = Long.parseLong(timestamp);
            Date date = new Date(time);
            String utcTime = Utility.toUTCString(date);
            statusResponseMessage.addStatusResponse(
                    refToMessageId, status, utcTime);
        }
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
     * @param location      Source of the error.
     *
     * @return ebXML message containing error code.
     *
     * @throws SOAPException
     */
    public static EbxmlMessage generateErrorMessage(EbxmlMessage ebxmlMessage,
            String errorCode, String severity, String description,
                    String location) throws SOAPException {
        final EbxmlMessage errorMessage = generateResponseMessage(
                ebxmlMessage, Constants.ACTION_MESSAGE_ERROR);
        errorMessage.getMessageHeader().
            setRefToMessageId(ebxmlMessage.getMessageId());
        errorMessage.addErrorList(errorCode, severity, description,
                                  location);
        errorMessage.saveChanges();
        return errorMessage;
    }

    /**
     * Generates pong message from the given ping message [ebMSS 8.2].
     *
     * @param pingMessage   Incoming ping message.
     *
     * @return Pong message in response of the incoming ping message.
     *
     * @throws SOAPException
     */
    public static EbxmlMessage generatePongMessage(EbxmlMessage pingMessage)
            throws SOAPException {
        final EbxmlMessage pongMessage = generateResponseMessage(
                pingMessage, Constants.ACTION_PONG);
        pongMessage.getMessageHeader().setRefToMessageId(
                pingMessage.getMessageId());
        pongMessage.saveChanges();
        return pongMessage;
    }
}
