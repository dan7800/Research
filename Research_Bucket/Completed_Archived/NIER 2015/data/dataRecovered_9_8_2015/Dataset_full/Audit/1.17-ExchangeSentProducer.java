package org.apache.servicemix.audit.jcr;

import javax.jms.ObjectMessage;

import org.springframework.jms.core.JmsTemplate;

public class ExchangeSentProducer {

    private JmsTemplate jmsTemplate;

    public ExchangeSentProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(ObjectMessage message) {
      
        jmsTemplate.convertAndSend(message);
    }

}
