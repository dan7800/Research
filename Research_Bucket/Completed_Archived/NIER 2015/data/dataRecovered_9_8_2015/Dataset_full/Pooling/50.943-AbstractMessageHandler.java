/*
 * Copyright 2004-5 Enigmatec Corporation Ltd
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
 * Mar 23, 2005 : Initial version created by gary
 */
package org.pi4soa.service.container;

import java.util.logging.Logger;

import org.pi4soa.service.EndpointReference;
import org.pi4soa.service.ServiceException;
import org.pi4soa.service.util.OperationMapper;
import org.pi4soa.service.util.OperationMapperFactory;

/**
 * This class provides an abstract implementation for a
 * message provider.
 */
public abstract class AbstractMessageHandler
						implements MessageHandler {

	/**
	 * This is the default constructor for the abstract message
	 * handler.
	 *
	 */
	public AbstractMessageHandler() {	
	}
	
	/**
	 * This is the constructor for the abstract message handler
	 * that can be used to initialize the name.
	 * 
	 * @param name The name
	 */
	public AbstractMessageHandler(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the name of the message handler.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name of the message handler.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method causes the message handler to be initialized.
	 * Each message handler should only be initialized once by the
	 * service container.
	 * 
	 * @throws ServiceException The initialization failed
	 */
	public void initialize() throws ServiceException {
		if (m_initialized) {
			throw new ServiceException("Message handler '"+getName()+
					"' has already been initialized");
		}
		
		m_initialized = true;
	}
	
	/**
	 * This method can be used to determine whether the message
	 * handler has been initialized.
	 * 
	 * @return Whether the message handler has been initialized
	 */
	protected boolean isInitialized() {
		return(m_initialized);
	}
	
	/**
	 * This method initializes the message dispatcher to be used
	 * as a callback for handling asynchronously received
	 * requests, from the server side, or responses/faults from the
	 * client side.
	 * 
	 * @param dispatcher The message dispatcher for receiving
	 * 					asynchronous messages
	 */
	public void setMessageDispatcher(MessageDispatcher dispatcher) {
		m_dispatcher = dispatcher;
	}
	
	/**
	 * This method returns the message dispatcher.
	 * 
	 * @return The message dispatcher
	 */
	protected MessageDispatcher getMessageDispatcher() {
		return(m_dispatcher);
	}
	
	/**
	 * This method schedules a message provider task to be
	 * performed in a separate thread.
	 * 
	 * @param task The task
	 */
	protected void schedule(Runnable task) {
		// TODO: Use thread pooling
		(new Thread(task)).start();
	}
	
	/**
	 * This method determines whether the message handler is
	 * appropriate for the supplied endpoint reference.
	 * 
	 * @param ref The endpoint reference
	 * @return Whether the message provider is suitable
	 */
	public boolean supportsEndpointReference(EndpointReference ref) {
		boolean ret=true;
		
		return(ret);
	}
	
	/**
	 * This method causes the message handler to be closed.
	 * Each message handler should only be closed once by the
	 * service container.
	 * 
	 * @throws ServiceException The close task failed
	 */
	public void close() throws ServiceException {
	}
	
	/**
	 * This method returns the message handler that can be assigned
	 * to the specified class. If this message handler is appropriate
	 * then it will return itself. If the message handler represents
	 * a composite handler, then the composed handlers will recurively
	 * be checked.
	 * 
	 * @param cls The class
	 * @return The message handler, or null if not found
	 */
	public MessageHandler getMessageHandlerForType(Class cls) {
		MessageHandler ret=null;
		
		if (cls != null &&
				cls.isAssignableFrom(cls)) {
			ret = this;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the operation mapper associated with
	 * the message handler.
	 * 
	 * @return The operation mapper
	 */
	protected OperationMapper getOperationMapper() {
		if (m_operationMapper == null) {
			try {
				m_operationMapper = OperationMapperFactory.getOperationMapper();
			} catch(Exception e) {
				logger.severe("Failed to initialize operation mapper: "+e);
			}
		}
		
		return(m_operationMapper);
	}

	/**
	 * This method configures an outbound message transformer.
	 * 
	 * @param transformer The transformer
	 */
	public void setOutboundMessageTransformer(MessageTransformer transformer) {
		m_outboundMessageTransformer = transformer;
	}
	
	/**
	 * This method returns the outbound message transformer.
	 * 
	 * @return The outbound transformer
	 */
	protected MessageTransformer getOutboundMessageTransformer() {
		return(m_outboundMessageTransformer);
	}
	
	/**
	 * This method configures an inbound message transformer.
	 * 
	 * @param transformer The transformer
	 */
	public void setInboundMessageTransformer(MessageTransformer transformer) {
		m_inboundMessageTransformer = transformer;
	}
	
	/**
	 * This method returns the inbound message transformer.
	 * 
	 * @return The inbound transformer
	 */
	protected MessageTransformer getInboundMessageTransformer() {
		return(m_inboundMessageTransformer);
	}
	
	private static Logger logger = Logger.getLogger("org.pi4soa.service.container");
		
	private String m_name=null;
	private MessageDispatcher m_dispatcher=null;
	private boolean m_initialized=false;
	private OperationMapper m_operationMapper=null;
	private MessageTransformer m_inboundMessageTransformer=null;
	private MessageTransformer m_outboundMessageTransformer=null;
}
