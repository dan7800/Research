/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.ih,======================================================================*/
/*.ic,--- COPYRIGHT (c) --  Open ebXML - 2001-2002 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2002. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
package org.openebxml.ebxml.foundation.logic;


/************************************************
	Includes
\************************************************/
import java.io.IOException;              /* JME CLDC 1.0 */
import java.io.InputStream;              /* JME CLDC 1.0 */
import java.io.OutputStream;             /* JME CLDC 1.0 */
import java.util.Hashtable;             /* JME CLDC 1.0 */

import org.openebxml.comp.log.*;
import org.openebxml.comp.util.*;
import org.openebxml.comp.managers.*;

import org.openebxml.comp.validator.*;
import org.openebxml.comp.validator.storage.*;
import org.openebxml.comp.validator.presentation.*;

import org.openebxml.ebxml.foundation.logic.template.*;
import org.openebxml.ebxml.foundation.storage.*;

/**
 *  Generic superclass to be subclassed by specific managers that handles ebXML related resources such as specifications and documents.
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: ebXMLSpecificationManager.java,v 1.3 2002/06/30 16:53:48 awtopensource Exp $
 */

public abstract class ebXMLSpecificationManager   extends  ResourceManager implements Configurable {

    /*-----------------------------------------------------------------*/
	/****/
    public static LogEvent  LOGEVENT_ValidationContextNotFound = new DefaultLogEvent(null /*parent*/,"ValidationContext identifier not found",LogEvent.GRADE_ERROR);
    /****/
    public static LogEvent  LOGEVENT_ValidationSuccess = new DefaultLogEvent(null /*parent*/,"Validation Success",LogEvent.GRADE_INFO);
    /****/
    public static LogEvent  LOGEVENT_ValidationFailure = new DefaultLogEvent(null /*parent*/,"Validation failed",LogEvent.GRADE_ERROR);
    /****/
    public static LogEvent  LOGEVENT_ResolvmentFailure = new DefaultLogEvent(null /*parent*/,"Resolvment problem",LogEvent.GRADE_WARNING);
    /****/
    public static LogEvent  LOGEVENT_ReadingFailure = new DefaultLogEvent(null /*parent*/,"Reading problem",LogEvent.GRADE_ERROR);
    /****/
    public static LogEvent  LOGEVENT_WritingFailure = new DefaultLogEvent(null /*parent*/,"Writing problem",LogEvent.GRADE_ERROR);

    /*-----------------------------------------------------------------*/
	/**
     * List of InstanceFactories keyed by specification and version.
     */
    protected Hashtable     fInstanceFactories;
	/**
     * List of registered templates
     */
    protected Hashtable     fTemplates;
	/**
     * List of Read handlers keyed by type.
     */
    private Hashtable     fReadHandlers;

	/**
     * List of Write handlers keyed by type.
     */
    private Hashtable     fWriteHandlers;


    /**
     * Primary validation manager.
     */
    protected ModelValidatorManager fValidatorManager;

	/*****/
	protected boolean 				fValidate_Debug;

    /****/
    public ebXMLSpecificationManager(String name,ThreadPoolManager tpMgr, BindingManager bmgr)
    {
        super(name,tpMgr, bmgr);

		fInstanceFactories  = new Hashtable();
        fReadHandlers       = new Hashtable();
        fWriteHandlers      = new Hashtable();

        fTemplates          = new Hashtable();

        fValidatorManager   = new ModelValidatorManager();
		fValidate_Debug		= false;
    }
	/****/
	public ModelValidatorManager getValidatorManager() {
		return fValidatorManager;
	}

	/****/
	public boolean getValidationDebug() {
		return fValidate_Debug;
	}

	/**
	 * Opens this manager after its has been Initiated.
	 */
	public void Open() {
        /* do nothing*/
	}

    /**
     * Primary mainloop when running as separate resourcemanager on its own thread.
     */
    protected void MainLoop()
    {
        super.MainLoop();
    }

	/** 
	 * Called when timer expires. Overwrite this method to do
	 * anything useful. 
	 *@param tv Time Value for when timer expired
	 *@param obj An arbitrary object that was passed to the Timer Queue
	 * (Asynchronous Completion Token)
	 *@return -1
	 */
	public  int handleTimeout (TimeValue tv, Object obj)
    {
        /* check for regular timeout */
        if( obj == this )
            {
            }
        return 0;
    }
	public void Close() {
        fActive = false;
	}

	/** 
	 * Terminate the object. Note that an object can not be explicitly
	 * unloaded. Overwrite this method to do anything useful.
	 *@return -1 (default implementation)
	 */
	public int fini ()
    {
        return super.fini();
    }
	
    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Registers a template for specific EBXMLInstance.
     * @param template templeate to register.
     */
    public void RegisterTemplate(ebXMLTemplateEntry template) {
        if( template == null )
            return;

        fTemplates.put(template,template);
    }

    /**
     * UnRegisters and remove template.
     *
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instanceSpecification instance identifier.
     * @param instanceVersion version of instance.
     */
    public void UnRegisterTemplate(String templateSpecification, 
                                   String templateVersion, 
                                   String instanceSpecification,
                                   String instanceVersion) {

        ebXMLTemplateEntry lKey = new ebXMLTemplateEntry(templateSpecification,
                                                         templateVersion,
                                                         instanceSpecification,
                                                         instanceVersion);
        Object lRemoved = fTemplates.remove(lKey);
    }
    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Register a ebXML InstanceFactory,  If a factory was previously registered then it is removed and the new factory is put in its place.
     * @param factory Factory to register.  MUST not be <code>null</code>.
     * @return Previous registered factory or <code>null</code>.
     */
    public  ebXMLInstanceFactory RegisterInstanceFactory(ebXMLInstanceFactory factory) {
        if( factory == null )
            throw new IllegalArgumentException("ebXMLInstanceFactory cannot be null.");
        
        QName   lKey = new QName(factory.getSpecification(),
                                 factory.getSpecificationVersion());

        /* check if it already exists */
        Object lFound = fInstanceFactories.get(lKey);
        if( lFound != null)
            {
            fInstanceFactories.remove(lKey);
            }
        fInstanceFactories.put(lKey, factory);
        
        return (ebXMLInstanceFactory)lFound;
    }

  /**
     * UnRegister a ebXML InstanceFactory.
     * @param specification ebXMLInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of ebXMLInstanceFactory specification
     * @return Unregistered factory or <code>null</code> if factory was not registered.
     */
    public ebXMLInstanceFactory UnRegisterInstanceFactory(String specification,
                                                         String version) {
        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");

        QName   lKey = new QName(specification,version);

        Object lRemoved = fInstanceFactories.remove(lKey);

        return (ebXMLInstanceFactory)lRemoved;
    }


    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Register a read handler, to be used when reading from InputStreams or resolvers. If a handler was previously registered then it is removed and the new handler is put in its place.
     * @param handler Handler to register.
     * @return Previous reader or <code>null</code>.
     */
    public  ebXMLReadHandler RegisterReadHandler(ebXMLReadHandler handler) {
        if( handler == null )
            return null;

        /* check if it already exists */
        QName  lKey = new QName(handler.getInstanceClass(),handler.getType());

        synchronized (fReadHandlers) {
        Object lFound = fReadHandlers.get(lKey);
        if( lFound != null)
            {
            fReadHandlers.remove(lKey);
            }
        fReadHandlers.put(lKey, handler);
        
        return (ebXMLReadHandler)lFound;
        }
    }

  /**
     * UnRegister a ReadHandler.
     * @param instanceClass class or group of instances.
     * @param type type of handler to unregister
     * @return Unregistred Handler or <code>null</code> if Handler was not registered.
     */
    public  ebXMLReadHandler UnRegisterReadHandler(String instanceClass,
                                                   String handler) {
        if( handler == null)
            return null;

        QName  lKey = new QName(instanceClass, handler);

        synchronized (fReadHandlers) {
        return (ebXMLReadHandler)fReadHandlers.remove(lKey);
        }
    }

    /**
     * Find a ReadHandler.
     * @param instanceClass class or group of instances.
     * @param type type of handler to unregister
     * @return Found handler or <code>null</code> if Handler was not registered.
     */
    public ebXMLReadHandler findReadHandler(String instanceClass,
                                            String handler) {
        QName  lKey = new QName(instanceClass, handler);

        return (ebXMLReadHandler)fReadHandlers.get(lKey);
    }

    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Register a write handler, to be used when writing to InputStreams . If a handler was previously registered then it is removed and the new handler is put in its place.
     * @param handler Handler to register.
     * @return Previous writeer or <code>null</code>.
     */
    public  ebXMLWriteHandler RegisterWriteHandler(ebXMLWriteHandler handler) {
        if( handler == null )
            return null;
        
        /* check if it already  exists */
        QName  lKey = new QName(handler.getInstanceClass(),handler.getType());
        
        synchronized (fWriteHandlers) {
        
        Object lFound = fWriteHandlers.get(lKey);
        if( lFound != null)
            {
            fWriteHandlers.remove(lKey);
            }
        fWriteHandlers.put(lKey, handler);
        
        return (ebXMLWriteHandler)lFound;
        }
    }
    
    /**
     * UnRegister a WriteHandler.
     * @param instanceClass class or group of instances.
     * @param type type of handler to unregister
     * @return Unregistred Handler or <code>null</code> if Handler was not registered.
     */
    public ebXMLWriteHandler UnRegisterWriteHandler(String instanceClass,
                                                    String handler) {
        if( handler == null)
            return null;
        
        QName  lKey = new QName(instanceClass, handler);
        
        synchronized(fWriteHandlers) {
        return (ebXMLWriteHandler)fWriteHandlers.remove(lKey);
        }
    }   
    
    /**
     * Find a WriteHandler.
     * @param instanceClass class or group of instances.
     * @param type type of handler to unregister
     * @return Found handler or <code>null</code> if Handler was not registered.
     */
    public ebXMLWriteHandler findWriteHandler(String instanceClass,
                                              String handler) {
        QName  lKey = new QName(instanceClass, handler);

        return (ebXMLWriteHandler)fWriteHandlers.get(lKey);
    }

}


/*.iend,ebXMLSpecificationManager,==================================*/
