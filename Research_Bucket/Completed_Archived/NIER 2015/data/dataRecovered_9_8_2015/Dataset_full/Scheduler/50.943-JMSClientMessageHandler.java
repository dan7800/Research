/*
 * Copyright 2005-6 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * Mar 14, 2006 : Initial version created by gary
 * Nov 21, 2006 : Updated to be more generic JMS message handler
 */
package org.pi4soa.j2ee.container;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Destination;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.pi4soa.service.Channel;
import org.pi4soa.service.EndpointReference;
import org.pi4soa.service.Message;
import org.pi4soa.service.OutOfSequenceMessageException;
import org.pi4soa.service.ServiceException;
import org.pi4soa.service.behavior.OperationDefinition;
import org.pi4soa.service.container.AbstractMessageHandler;
import org.pi4soa.service.container.MessageHandlerInvocation;

/**
 * This class provides the JMS client message handler
 * implementation.
 *
 */
public class JMSClientMessageHandler extends AbstractMessageHandler {

	/**
	 * This is the default constructor.
	 *
	 */
	public JMSClientMessageHandler() {
		super("JMS-Client");
	}
	
	/**
	 * This method initializes the response connection
	 * and session.
	 * 
	 * @throws JMSException Failed to setup session
	 * @throws NamingException Failed to locate
	 * 						connection factory
	 */
	private void setupPTP() throws JMSException, NamingException {
		
		if (m_session == null) {
			
			// NOTE: This method cannot be performed at message
			// handler initialization time, as the session needs to
			// be created within the scope of the transaction
			
			// Only create JNDI context if factory has been
			// specified
			if (m_jndiContext == null &&
					m_jndiInitialContextFactory != null) {
				
				java.util.Properties props=new java.util.Properties();
				
				if (m_jndiInitialContextFactory != null) {
					props.put("java.naming.factory.initial",
							m_jndiInitialContextFactory);
				}
				
				if (m_jndiProviderURL != null) {
					props.put("java.naming.provider.url",
							m_jndiProviderURL);
				}
				
				if (m_jndiFactoryURLPackages != null) {
					props.put("java.naming.factory.url.pkgs",
							m_jndiFactoryURLPackages);
				}
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Initial context with props="+props);
				}
				
				m_jndiContext = new InitialContext(props);
			}
	
			// Obtain the connection
			if (m_connectionFactory != null) {

				m_connection = m_connectionFactory.createConnection();
				
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Non-JNDI connection: "+m_connection);
				}
				
			} else if (m_jmsConnectionFactory != null) {
				try {
					m_connectionFactory = (ConnectionFactory)
							m_jndiContext.lookup(m_jmsConnectionFactory);
					m_connection = m_connectionFactory.createConnection();
					
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Connection: "+m_connection);
					}
					
				} catch(java.lang.RuntimeException e) {
					
					if (m_jmsConnectionFactoryAlternate != null) {
						m_connectionFactory = (ConnectionFactory)
							m_jndiContext.lookup(m_jmsConnectionFactoryAlternate);
						m_connection = m_connectionFactory.createConnection();
		
						if (logger.isLoggable(Level.FINER)) {
							logger.finer("Alternate Connection: "+m_connection);
						}
						
					} else {
						throw e;
					}
				}
		
			} else {
				logger.severe("JMS client message handler " +
						"property 'jmsConnectionFactory' " +
						"or object 'connectionFactory' have " +
						"not been configured");
			}
			
			if (m_connection != null) {
				
				m_session = m_connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				
				// Check if client reply queue has been configured
				if (m_replyDestinationName != null) {
					m_replyDestination = (Destination)
							getDestination(m_replyDestinationName);
				}
				
				// Check if a reply queue should be created
				if (m_mdbReplyDestination != null) {
					logger.info("JMS client message handler will use " +
							"MDB reply destination: "+m_mdbReplyDestination);
				} else {
					
					if (m_replyDestination == null) {
						// Create temporary destination
						m_replyDestination = m_session.createTemporaryQueue();
					}
					
					logger.info("JMS client message handler will use " +
							"client provided reply destination: "+m_replyDestination);
					
					m_replyReceiver=
						m_session.createConsumer(m_replyDestination);
								
					m_replyReceiver.setMessageListener(new javax.jms.MessageListener() {
						
						public void onMessage(javax.jms.Message mesg) {
							handleReply(mesg, null);
						}
					});
				}
				
				m_connection.start();
			}
		}
	}
	
	/**
	 * This method returns the JMS destination associated with
	 * the supplied name.
	 * 
	 * @param name The name
	 * @return The destination
	 * @throws JMSException Failed to get JMS destination
	 * @throws NamingException Failed to get name from JNDI
	 */
	protected Destination getDestination(String name)
					throws JMSException, NamingException {
		Destination ret=null;
		
		if (m_jndiContext != null) {
			ret = (Destination)
					m_jndiContext.lookup(name);
		} else {
			ret = m_session.createQueue(name);
		}
		
		return(ret);
	}

	/**
	 * This method is used to process a message. If the provider
	 * is a client side message provider, then this will send
	 * the message to the service component. If the provider
	 * is assuming the server side role, then this will be
	 * returning a response or fault to the client side.
	 * The implementations of this provider interface will
	 * implement different protocols for transferring XML based
	 * messages between a client and server component.
	 * All the information required to identify the
	 * specific service endpoint is contained within the
	 * message.<p>
	 * <p>
	 * Where a response message is expected, the message provider
	 * may establish a listener for the subsequent message,
	 * which is then correlated to the current message being
	 * processed. The optional operation definition can be used
	 * to infer whether a response is expected.
	 * 
	 * @param message The message
	 * @param channel The channel associated with the message
	 * @param opdef The optional operation definition
	 * @exception ServiceException Failed to process the message
	 */
	public synchronized void process(Message message, Channel channel,
			OperationDefinition opdef) throws ServiceException {
		
		// Check if session needs to be initialized
		// NOTE: Synchronization now done at method level as
		// we need to ensure that only a single message is processed
		// at once, in case we wish to create and close the session
		// for each message (i.e. in a J2EE container).
		if (m_session == null) {
			try {
				setupPTP();
			} catch(Exception e) {
				throw new ServiceException(
						"Failed to setup session: "+e, e);
			}
		}
		
		// Check if transformation is defined
		if (getOutboundMessageTransformer() != null &&
				message instanceof org.pi4soa.service.MutableMessage) {
			
			((org.pi4soa.service.MutableMessage)message).setValue(
					getOutboundMessageTransformer().transform(message.getValue()));
		}
		
		// Only schedule a task if we need to await a response.
		// This means that a fixed reply queue is not available,
		// otherwise the fixed queue can be used to receive the
		// response later.
		boolean responseExpected=(opdef != null &&
							opdef.isResponseExpected());
			
		/*
		if (message.isRequest() && responseExpected
						&& m_mdbReplyDestination == null &&
						m_replyDestination == null) {

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Perform synchronous invoke: "+message+
							" responseExpected="+responseExpected);
			}
			
			schedule(new MessageHandlerInvocation(this, message,
					true) {
				public Message invoke(Message req) {
					Message ret=null;
					
					try {
						Destination respDest=sendRequest(req, true);
						
						if (respDest != null) {
							MessageConsumer receiver=
								m_session.createConsumer(respDest);
							
							// TODO: Need to decide how long to
							// wait for? Cannot be indefinite
							javax.jms.Message resp=
										receiver.receive();
							java.io.Serializable value=null;
							
							if (resp instanceof TextMessage) {
								value = ((TextMessage)resp).getText();
							} else if (resp instanceof ObjectMessage) {
								value = ((ObjectMessage)resp).getObject();
							} else {
								logger.severe("Unable to handle response message type: "+resp);
							}
							
							String faultName=resp.getStringProperty(
										MDBUtil.MDB_FAULT);
							
							ret = createResponse(value, faultName, null);
							
							// Check if transformation is defined
							if (getInboundMessageTransformer() != null &&
									ret instanceof org.pi4soa.service.MutableMessage) {
								
								((org.pi4soa.service.MutableMessage)ret).setValue(
										getInboundMessageTransformer().transform(
												ret.getValue()));
							}
							
							receiver.close();
						}
					} catch(Exception e) {
						logger.severe("Failed to invoke request: "+e);
					}
					
					return(ret);
				}
			});
		} else {
		*/
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Perform asynchronous invoke: "+message+
						" responseExpected="+responseExpected);
			}

			sendRequest(message, responseExpected);

			// NOTE: This may be slightly inefficient, to close
			// the session/connection after sending each message
			// (when inside a J2EE server). Howeverm the alternative
			// would be to manage multiple sessions/connections on
			// a per thread basis, and cause the MDB to single
			// the handlers to close connections before returning.
			// However, generally it is likely that only a single
			// message will be sent within the scope of a
			// message being processed by an MDB, so the overhead
			// will only be incurred on the fewer times when multiple
			// requests are sent out.
			if (m_mdbReplyDestination != null) {
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Closing XA session");
				}
				
				// Close the JMS connection, when inside the
				// J2EE server, as otherwise the MDB will
				// complain about the connection not being
				// closed and cause it to be implicitly closed.
				if (m_jndiContext != null) {
					close();
				}
			}
		//}
	}
	
	/**
	 * This method is invoked to send a request message to a queue.
	 * 
	 * @param message The message
	 * @param responseExpected Whether a response is expected
	 * @return The response queue, if requester should listen
	 * @throws ServiceException Failed to send request
	 */
	protected void sendRequest(Message message, boolean responseExpected)
							throws ServiceException {
		//Destination ret=null;
		EndpointReference endpoint=message.getServiceEndpoint();

		try {
			Destination dest=getDestination(endpoint.getEndpointURL());
			
			MessageProducer sender = m_session.createProducer(dest);
			javax.jms.Message request=null;
			
			if (message.getValue() instanceof String) {
				request = m_session.createTextMessage((String)
								message.getValue());
			} else {
				request = m_session.createObjectMessage(message.getValue());
			}
			
			// Configure operation name in the header
			MDBUtil.setOperation(request, message.getOperationName());
						
			// Check if reply expected
			Destination replyDest=m_mdbReplyDestination;
			
			if (replyDest == null) {
				replyDest = m_replyDestination;
				
				/*
				if (replyDest == null && responseExpected) {
					replyDest = m_session.createTemporaryQueue();
					
					// Return queue to requester, to monitor
					// for response
					ret = replyDest;
				}
				*/
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Setting reply destination to: "+replyDest);
			}
			
			if (replyDest != null) {
				request.setJMSReplyTo(replyDest);
			}
			
			sender.send(request);
			
		} catch(Exception e) {
			throw new ServiceException("Failed to send request: "+e, e);
		}
		
		//return(ret);
	}
	
	/**
	 * This method acknowledges a request message previously sent
	 * to the message dispatcher registered with this
	 * message provider. Only request messages are acknowledged,
	 * to enable the message provider to establish correlation
	 * information associated with any subsequent response/fault
	 * message.
	 * 
	 * @param message The message being acknowledged
	 * @param channel The channel associated with the message
	 * @throws ServiceException Failed to acknowledge the message
	 */
	public void acknowledge(Message message, Channel channel)
						throws ServiceException {
		
	}
	
	/**
	 * This method sets the JNDI context.
	 * 
	 * @param context The context
	 */
	protected void setContext(Context context) {
		m_jndiContext = context;
	}
	
	/**
	 * This method sets the MDB reply destination. If an
	 * explicit MDB reply destination name is not set,
	 * then the client message handler will create a
	 * temporary queue per request.
	 * 
	 * @param dest The MDB reply destination
	 */
	protected void setMDBReplyDestination(Destination dest) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Setting MDB reply destination to: "+dest);
		}

		m_mdbReplyDestination = dest;
	}
	
	/**
	 * This method sets the client reply destination to use when
	 * a client based service is performing a request/response
	 * interaction with another service. If this destination is not
	 * specified, then a temporary queue will be created on
	 * a per request basis. This queue will need to be set
	 * in an environment that needs to recover from failure, as
	 * temporary queues will not be recoverable.
	 * 
	 * @param queue The reply destination
	 */
	public void setClientReplyDestination(Destination dest) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Setting reply destination to: "+dest);
		}

		m_replyDestination = dest;
	}
	
	/**
	 * This method handles a reply message.
	 * 
	 * @param resp The response
	 * @param serviceType The service type
	 */
	public void handleReply(javax.jms.Message resp,
					String serviceType) {
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(Thread.currentThread()+
					": Received reply message: "+resp);
		}
		
		try {
			java.io.Serializable value=null;
			
			if (resp instanceof TextMessage) {
				value = ((TextMessage)resp).getText();
			} else if (resp instanceof ObjectMessage) {
				value = ((ObjectMessage)resp).getObject();
			} else {
				logger.severe("Unable to handle response message type: "+resp);
			}
			
			Message response=null;
			
			if (value instanceof org.pi4soa.service.Message) {
				response = (org.pi4soa.service.Message)value;
				
			} else {
				String operationName=MDBUtil.getOperation(resp, serviceType);
				
				String messageType=MDBUtil.getMessageType(resp, serviceType);
				
				String faultName=MDBUtil.getFault(resp, serviceType);
					
				response = getMessageDispatcher().
					createResponse(operationName,
						faultName,
						messageType,
						null, //m_request.getServiceType(),
						null, //m_request.getServiceEndpoint(),
						value, null, null);
				
				if (m_retryCount > 0) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine(Thread.currentThread()+
								": Setting retry count: "+m_retryCount);
					}
					response.setRetryCount(m_retryCount);
				}
			}
			
			// Check if transformation is defined
			if (getInboundMessageTransformer() != null &&
					response instanceof org.pi4soa.service.MutableMessage) {
				
				((org.pi4soa.service.MutableMessage)response).setValue(
						getInboundMessageTransformer().transform(
									response.getValue()));
			}
			
			try {
				getMessageDispatcher().dispatch(response);
			} catch(OutOfSequenceMessageException oosmex) {
				
				// Check if should retry reply
				if (response.shouldRetry() &&
						(m_mdbReplyDestination != null ||
						m_replyDestination != null)) {
					sendReplyRetry(response);
					
				} else {
					throw oosmex;
				}
			}
			
		} catch(Exception e) {
			logger.severe("Failed to process response '"+resp+"': "+e);
		}
	}
	
	/**
	 * This method is used to send a reply retry.
	 *
	 * @param mesg The message to be retried
	 * @throws ServiceException Failed to send retry
	 */
	private synchronized void sendReplyRetry(org.pi4soa.service.Message mesg)
							throws ServiceException {

		if (logger.isLoggable(java.util.logging.Level.FINE)) {
			logger.fine("Sending reply retry: mesg="+mesg);
		}
		
		// Check if session needs to be initialized
		// NOTE: Synchronization now done at method level as
		// we need to ensure that only a single message is processed
		// at once, in case we wish to create and close the session
		// for each message (i.e. in a J2EE container).
		if (m_session == null) {
			try {
				setupPTP();
			} catch(Exception e) {
				throw new ServiceException(
						"Failed to setup Queue session: "+e, e);
			}
		}

		try {
			MessageProducer sender=null;
			
			if (m_mdbReplyDestination != null) {
				sender = m_session.createProducer(m_mdbReplyDestination);
			} else if (m_replyDestination != null) {
				sender = m_session.createProducer(m_replyDestination);
			}
			
			javax.jms.Message retry=m_session.createObjectMessage(
							(java.io.Serializable)mesg);
			
			sender.send(retry);
			sender.close();
			
			// Not sure if need to also check if clientReplyQueue
			// set - but at the moment only assuming XA queue session
			// if using MDB (see process method for similar approach
			// when handling the reply)
			//if (m_mdbReplyDestination != null) {
			if (m_jndiContext != null) {
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Closing XA session");
				}
				
				// Close the JMS connection, when inside the
				// J2EE server, as otherwise the MDB will
				// complain about the connection not being
				// closed and cause it to be implicitly closed.
				close();
			}
		} catch(Exception e) {
			throw new ServiceException("Failed to send retry request: "+e);
		}
	}
	
	/**
	 * This method causes the message handler to be closed.
	 * Each message handler should only be closed once by the
	 * service container.
	 * 
	 * @throws ServiceException The close task failed
	 */
	public void close() throws ServiceException {
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Closing J2EE MDB Client Message Handler");
		}
		
		try {
			if (m_session != null) {
				m_session.close();
			}
			
			if (m_connection != null) {
				m_connection.close();
			}
			
		} catch(Exception e) {
			logger.severe("Failed to close response queue " +
					"session and connection: "+e);
			e.printStackTrace();
		}
		
		m_connection = null;
		m_session = null;
	}
		
	/**
	 * The name of the connection factory to use when looking
	 * up the resource.
	 * 
	 * @param name The connection factory name
	 */
	public void setJMSConnectionFactory(String name) {
		m_jmsConnectionFactory = name;

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Connection Factory: "+m_jmsConnectionFactory);
		}
	}
	
	/**
	 * This method returns the JMS connection factory.
	 * 
	 * @return The JMS connection factory
	 */
	public String getJMSConnectionFactory() {
		return(m_jmsConnectionFactory);
	}
		
	/**
	 * The name of the connection factory to use when looking
	 * up the resource.
	 * 
	 * @param name The connection factory name
	 */
	public void setJMSConnectionFactoryAlternate(String name) {
		m_jmsConnectionFactoryAlternate = name;

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Alternate Connection Factory: "+
						m_jmsConnectionFactoryAlternate);
		}
	}
	
	/**
	 * This method returns the JMS connection factory.
	 * 
	 * @return The JMS connection factory
	 */
	public String getJMSConnectionFactoryAlternate() {
		return(m_jmsConnectionFactoryAlternate);
	}

	/**
	 * This method sets the reply queue name.
	 * 
	 * @param name The reply queue name
	 */
	public void setReplyQueue(String name) {
		m_replyDestinationName = name;
	}
	
	/**
	 * This method returns the reply queue name.
	 * 
	 * @return The reply queue name
	 */
	public String getReplyQueue() {
		return(m_replyDestinationName);
	}
	
	/**
	 * This method sets the retry count.
	 * 
	 * @param count The count
	 */
	public void setRetryCount(String count) {
		
		try {
			m_retryCount = Integer.parseInt(count);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Initialized retry count to: "+m_retryCount);
			}
		} catch(Exception e) {
			logger.severe("Failed to parse retry count '"+
					count+"': "+e);
		}
	}
	
	/**
	 * This method sets the JNDI initial context factory class name.
	 * 
	 * @param factory The factory class name
	 */
	public void setJNDIInitialContextFactory(String factory) {
		m_jndiInitialContextFactory = factory;
	}
	
	/**
	 * This method sets the provider URL that is used when publishing
	 * the tracker events.
	 * 
	 * @param url The provider url
	 */
	public void setJNDIProviderURL(String url) {
		m_jndiProviderURL = url;
	}
	
	/**
	 * This method sets the JNDI factory URL packages.
	 * 
	 * @param pkgs The packages
	 */
	public void setJNDIFactoryURLPackages(String pkgs) {
		m_jndiFactoryURLPackages = pkgs;
	}
	
	/**
	 * This method returns the JMS connection factory.
	 * 
	 * @return The connection factory
	 */
	public ConnectionFactory getConnectionFactory() {
		return(m_connectionFactory);
	}
	
	/**
	 * This method sets the JMS connection factory.
	 * 
	 * @param cf The connection factory
	 */
	public void setConnectionFactory(ConnectionFactory cf) {
		m_connectionFactory = cf;
	}
	
	private static Logger logger = Logger.getLogger("org.pi4soa.j2ee.container");

	private Context m_jndiContext=null;
	private String m_jndiInitialContextFactory=null;
	private String m_jndiProviderURL=null;
	private String m_jndiFactoryURLPackages=null;
	private String m_jmsConnectionFactory=null;
	private String m_jmsConnectionFactoryAlternate=null;
	private ConnectionFactory m_connectionFactory=null;
	private Connection m_connection=null;
	private Session m_session=null;	
	private Destination m_mdbReplyDestination=null;
	private Destination m_replyDestination=null;
	private String m_replyDestinationName=null;
	private int m_retryCount=0;
	private MessageConsumer m_replyReceiver=null;
}
