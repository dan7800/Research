package org.apache.servicemix.audit.jcr;

import javax.jbi.messaging.MessageExchange;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

/**
 * 
 * Marshaler for whole message exchange
 * 
 * @author vkrejcirik
 * 
 */
public class EntireMarshaler implements AuditorMarshaler {

    private static final Log LOG = LogFactory.getLog(EntireMarshaler.class);

    public ObjectMessage marschal(MessageExchange exchange, Session session) {

        ObjectMessage message = null;

        try {
            message = session
                    .createObjectMessage((MessageExchangeImpl) exchange);

        } catch (JMSException e) {
            LOG.error("Error while serializing message exchange.");
        }

        return message;
    }

    public MessageExchange unmarshal(ObjectMessage message) {

        MessageExchange exchange = null;

        try {
            exchange = (MessageExchange) message.getObject();
        } catch (JMSException e) {
            LOG.error("Error while deserializing object message.");
        }

        return exchange;
    }

}
