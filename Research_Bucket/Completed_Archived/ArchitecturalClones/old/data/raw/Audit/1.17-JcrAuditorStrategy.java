package org.apache.servicemix.audit.jcr;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.xml.transform.TransformerException;

/**
 * 
 * Interface to store an exchange on behalf of the {@link JcrAuditor}
 * 
 * @author vkrejcirik
 * 
 */
public interface JcrAuditorStrategy {

	public abstract void processExchange(MessageExchange messageExchange,
			Session session) throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException, MessagingException, TransformerException;

}
