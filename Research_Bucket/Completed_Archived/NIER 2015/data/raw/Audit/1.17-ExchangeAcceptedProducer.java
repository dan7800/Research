package org.apache.servicemix.audit.jcr;

import javax.jms.ObjectMessage;

import org.springframework.jms.core.JmsTemplate;

public class ExchangeAcceptedProducer {

    private JmsTemplate jmsTemplate;

    public ExchangeAcceptedProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(ObjectMessage message) {
      
        jmsTemplate.convertAndSend(message);
    }
}
