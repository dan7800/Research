package org.apache.servicemix.audit.jcr;

import javax.jbi.messaging.MessageExchange;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * 
 * 
 * 
 * @author vkrejcirik
 *
 */
public interface AuditorMarshaler {

    public ObjectMessage marschal(MessageExchange exchange, Session session);
    public MessageExchange unmarshal(ObjectMessage message);
    
}
