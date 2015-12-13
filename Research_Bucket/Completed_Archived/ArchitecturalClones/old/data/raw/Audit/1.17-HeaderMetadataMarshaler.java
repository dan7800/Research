package org.apache.servicemix.audit.jcr;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.MessageExchange;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

/**
 * 
 * Marshaler for header and metadata from message exchange
 * 
 * @author vkrejcirik
 * 
 */
public class HeaderMetadataMarshaler implements AuditorMarshaler {

    private static final Log LOG = LogFactory
            .getLog(HeaderMetadataMarshaler.class);

    public ObjectMessage marschal(MessageExchange exchange, Session session) {

        ObjectMessage message = null;
        OwnMessageExchangeImpl exchangeImpl = new OwnMessageExchangeImpl();

        // TODO: naplnit exchangeImpl

        try {
            message = session.createObjectMessage(exchangeImpl);

        } catch (JMSException e) {
            LOG.error("Error while serializing message exchange.");
        }

        return message;
    }

    public MessageExchange unmarshal(ObjectMessage message) {

        MessageExchange exchange = new MessageExchangeImpl(){
        
            @Override
            public void readExternal(ObjectInput arg0) throws IOException,
                    ClassNotFoundException {
                
            }
        };
        
        
        OwnMessageExchangeImpl exchangeImpl = null;

        try {
            exchangeImpl = (OwnMessageExchangeImpl) message.getObject();
        } catch (JMSException e) {
            LOG.error("Error while deserializing object message.");
        }

        // TODO: naplnit exchange

        return exchange;
    }

}
