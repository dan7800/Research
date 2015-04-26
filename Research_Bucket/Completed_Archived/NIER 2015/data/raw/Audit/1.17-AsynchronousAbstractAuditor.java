package org.apache.servicemix.audit.jcr;

import java.net.URISyntaxException;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * 
 * Abstract class for take care of all the serialization and multi-threading
 * stuff
 * 
 * @author vkrejcirik
 * 
 * 
 */
public abstract class AsynchronousAbstractAuditor extends AbstractAuditor {

    private static final Log LOG = LogFactory
            .getLog(AsynchronousAbstractAuditor.class);

    private Session acceptedSession;
    private Session sentSession;

    private ActiveMQConnection connection;

    private Destination acceptedDestination;
    private Destination sentDestination;

    private ExchangeAcceptedProducer acceptedProducer;
    private ExchangeSentProducer sentProducer;

    private AcceptedListener acceptedListener;
    private SentListener sentListener;
    private ExceptionListener exceptionListener;

    private JmsTemplate acceptedJmsTemplate;
    private JmsTemplate sentJmsTemplate;

    private DefaultMessageListenerContainer sentListenerContainer;
    private DefaultMessageListenerContainer acceptedListenerContainer;

    protected AuditorMarshaler marshaler;

    public void doStart() throws JBIException {

        try {
            connection = ActiveMQConnection
                    .makeConnection("tcp://localhost:61616");

            acceptedSession = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            sentSession = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            acceptedDestination = acceptedSession
                    .createQueue("messages.accepted");

            sentDestination = sentSession.createQueue("messages.sent");

        } catch (URISyntaxException e) {
            throw new JBIException("URI syntax is wrong", e);
        } catch (JMSException e1) {
            throw new JBIException("Error while creating queue for exchange",
                    e1);
        }

        sentListenerContainer = new DefaultMessageListenerContainer();
        acceptedListenerContainer = new DefaultMessageListenerContainer();

        ActiveMQConnectionFactory sentConnectionFactory = new ActiveMQConnectionFactory();
        sentConnectionFactory.setBrokerURL("tcp://localhost:61616");

        ActiveMQConnectionFactory acceptedConnectionFactory = new ActiveMQConnectionFactory();
        acceptedConnectionFactory.setBrokerURL("tcp://localhost:61616");

        sentListenerContainer.setConnectionFactory(sentConnectionFactory);
        acceptedListenerContainer
                .setConnectionFactory(acceptedConnectionFactory);

        sentListenerContainer.setConcurrentConsumers(1);
        acceptedListenerContainer.setConcurrentConsumers(1);

        sentListenerContainer.setDestination(sentDestination);
        acceptedListenerContainer.setDestination(acceptedDestination);

        sentListener = new SentListener();
        acceptedListener = new AcceptedListener();

        acceptedJmsTemplate = new JmsTemplate();
        sentJmsTemplate = new JmsTemplate();

        acceptedJmsTemplate.setConnectionFactory(acceptedConnectionFactory);
        acceptedJmsTemplate.setDefaultDestination(acceptedDestination);

        sentJmsTemplate.setConnectionFactory(sentConnectionFactory);
        sentJmsTemplate.setDefaultDestination(sentDestination);

        sentProducer = new ExchangeSentProducer(sentJmsTemplate);
        acceptedProducer = new ExchangeAcceptedProducer(acceptedJmsTemplate);

        sentListenerContainer.setMessageListener(sentListener);
        acceptedListenerContainer.setMessageListener(acceptedListener);

        sentListenerContainer.setExceptionListener(exceptionListener);

        sentListenerContainer.setAutoStartup(true);
        sentListenerContainer.afterPropertiesSet();

        acceptedListenerContainer.setAutoStartup(true);
        acceptedListenerContainer.afterPropertiesSet();

        try {
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        super.doStart();
    }

    @Override
    protected void doStop() throws JBIException {

        try {

            sentListenerContainer.stop();
            sentListenerContainer.shutdown();
            acceptedListenerContainer.stop();
            acceptedListenerContainer.shutdown();

            acceptedSession.close();
            sentSession.close();
            connection.close();

        } catch (JMSException e) {
            throw new JBIException("Close session or connection failed", e);
        }

        super.doStop();
    }

    public void exchangeSent(ExchangeEvent event) {

        MessageExchange messageExchange = event.getExchange();
        ObjectMessage objectExchange = null;

        objectExchange = marshaler.marschal(messageExchange, sentSession);
        sentProducer.sendMessage(objectExchange);

    }

    public void exchangeAccepted(ExchangeEvent event) {

        MessageExchange messageExchange = event.getExchange();
        ObjectMessage objectExchange = null;

        objectExchange = marshaler.marschal(messageExchange, acceptedSession);
        acceptedProducer.sendMessage(objectExchange);

        super.exchangeAccepted(event);
    }

    public abstract void onExchangeSent(MessageExchange exchange);

    public abstract void onExchangeAccepted(MessageExchange exchange);

    protected AuditorMarshaler getMarshaler() throws PathNotFoundException,
            LoginException, RepositoryException {
        return null;

    }

    public class SentListener implements MessageListener {

        public void onMessage(Message message) {

            if (message instanceof ObjectMessage) {

                LOG.debug("receive message");
                ObjectMessage m = (ObjectMessage) message;

                MessageExchange exchange = marshaler.unmarshal(m);
                onExchangeSent(exchange);

            } else {
                throw new IllegalArgumentException(
                        "Message must be of type ObjectMessage");
            }
        }
    }

    public class AcceptedListener implements MessageListener {

        public void onMessage(Message message) {

            if (message instanceof ObjectMessage) {

                LOG.debug("receive message");
                ObjectMessage m = (ObjectMessage) message;

                MessageExchange exchange = marshaler.unmarshal(m);
                onExchangeAccepted(exchange);

            } else {
                throw new IllegalArgumentException(
                        "Message must be of type ObjectMessage");
            }
        }
    }

    public class ConfigEventListener implements EventListener {

        public void onEvent(EventIterator event) {

            LOG.debug("event!");

            try {
                marshaler = getMarshaler();

            } catch (PathNotFoundException e) {
                e.printStackTrace();
            } catch (LoginException e) {
                e.printStackTrace();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }
}
