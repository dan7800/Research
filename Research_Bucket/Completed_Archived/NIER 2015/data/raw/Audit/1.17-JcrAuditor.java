package org.apache.servicemix.audit.jcr;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.audit.AuditorException;

/**
 * 
 * JCR auditor
 * 
 * @author vkrejcirik
 * 
 */
public class JcrAuditor extends AsynchronousAbstractAuditor {

    private static final Log LOG = LogFactory.getLog(JcrAuditor.class);

    private Repository repository;
    private ThreadLocal<Session> session = new ThreadLocal<Session>();
    private JcrAuditorStrategy strategy;
    private ConfigEventListener eventListener = new ConfigEventListener();

    public static final String CONFIG_TYPE = "content/servicemix/config";

    @Override
    public void doStart() throws JBIException {
        if (repository == null) {
            throw new JBIException(
                    "No repository configured, unable to start JCR auditor");
        }
        if (strategy == null) {
            throw new JBIException(
                    "No JcrAuditorStrategy configure, unable to start JCR auditor");
        }

        try {
            marshaler = getMarshaler();
        } catch (LoginException e) {
            throw new JBIException(
                    "Login exception, unable to get Auditor Marshaler", e);
        } catch (RepositoryException e1) {
            throw new JBIException(
                    "Repository exception, unable to get Auditor Marshaler", e1);
        }

        try {
           ObservationManager observationManager = getSession().getWorkspace().getObservationManager();
           
           observationManager.addEventListener(
                    eventListener, Event.PROPERTY_CHANGED,
                    CONFIG_TYPE, false, null, null, false);
            
        } catch (UnsupportedRepositoryOperationException e) {
            throw new JBIException(
                    "Unsupported repository operation exception, unable to add Event Listener", e);
        } catch (RepositoryException e1) {
            throw new JBIException(
                    "Repository exception, unable to add Event Listener", e1);
        }

        super.doStart();
    }

    protected Session getSession() throws LoginException, RepositoryException {
        if (session.get() == null) {
            Session session = repository.login(new SimpleCredentials("admin",
                    "admin".toCharArray()));
            this.session.set(session);
        }
        return session.get();
    }

    public void onExchangeSent(MessageExchange exchange) {

        try {
            strategy.processExchange(exchange, getSession());
            getSession().save();

            LOG.info("Successfully stored information about message exchange "
                    + exchange.getExchangeId() + " in the JCR repository");
        } catch (Exception e) {
            LOG.error("Unable to store information about message exchange "
                    + exchange.getExchangeId(), e);
        }
    }

    @Override
    public void onExchangeAccepted(MessageExchange exchange) {

    }

    @Override
    protected AuditorMarshaler getMarshaler() throws LoginException,
            RepositoryException {

        AuditorMarshaler marshaler = null;
        Node config = getSession().getRootNode().getNode(CONFIG_TYPE);
        String mar = null;

        try {
                       
            mar = config.getProperty("marshaler").getValue().getString();
        } catch (RepositoryException e) {

            config.setProperty("marshaler", "EntireMarshaler");
            mar = "EntireMarshaler";
        }

        if (mar.equals("EntireMarshaler"))
            marshaler = new EntireMarshaler();
        else if (mar.equals("HeaderMetadataMarshaler"))
            marshaler = new HeaderMetadataMarshaler();

        return marshaler;
    }

    public String getDescription() {
        return "ServiceMix JCR Auditor";
    }

    // just some setters and getters
    /**
     * Configure the JCR Repository to connect to
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Configure the {@link JcrAuditorStrategy} to use
     * 
     * @param strategy
     */
    public void setStrategy(JcrAuditorStrategy strategy) {
        this.strategy = strategy;
    }

    // to be implemented
    @Override
    public int deleteExchangesByIds(String[] arg0) throws AuditorException {
        return 0;
    }

    @Override
    public int getExchangeCount() throws AuditorException {
        return 0;
    }

    @Override
    public String[] getExchangeIdsByRange(int arg0, int arg1)
            throws AuditorException {
        return null;
    }

    @Override
    public MessageExchange[] getExchangesByIds(String[] arg0)
            throws AuditorException {
        return null;
    }
}
