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
package org.openebxml.ebxml.docspec.logic.impl;


/************************************************
	Includes
\************************************************/
import java.io.IOException;              /* JME CLDC 1.0 */
import java.io.InputStream;              /* JME CLDC 1.0 */
import java.io.OutputStream;             /* JME CLDC 1.0 */
import java.util.Hashtable;             /* JME CLDC 1.0 */

import org.openebxml.comp.log.*;

import org.openebxml.comp.util.*;

import org.openebxml.comp.bml.*;
import org.openebxml.comp.bml.data.*;


import org.openebxml.comp.appl.*;
import org.openebxml.comp.managers.*;

import org.openebxml.comp.language.execution.*;

import org.openebxml.comp.model.*;

import org.openebxml.comp.validator.*;
import org.openebxml.comp.validator.storage.*;
import org.openebxml.comp.validator.presentation.*;

import org.openebxml.ebxml.foundation.logic.*;
import org.openebxml.ebxml.foundation.logic.template.*;
import org.openebxml.ebxml.foundation.storage.*;

import org.openebxml.ebxml.docspec.logic.*;

import org.openebxml.ebxml.docspec.logic.*;
import org.openebxml.ebxml.docspec.logic.template.*;
import org.openebxml.ebxml.docspec.storage.*;

/**
 *  DefaultDocSpecManager
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: DefaultDocSpecManager.java,v 1.4 2003/08/20 10:16:33 awtopensource Exp $
 */

public class DefaultDocSpecManager extends ebXMLSpecificationManager implements DocSpecManager{


	/****/
	public static final String   NAME          = "DefaultDocSpecManager";

    /*-----------------------------------------------------------------*/
	/**
     * Resolver used in finding resources.
     */
	protected DocSpecResolver  fResolver;

	/**
	 * Document specification manager to use.
	 */
	protected DocSpecManager fDocSpecManager;

    /*-----------------------------------------------------------------*/
    /**
     *  Default constructor
     */
        public DefaultDocSpecManager(){
        this(null,null);
    }
    /**
     *  Constructor with Document specification manager.to associated.
     */
        public DefaultDocSpecManager(DocSpecManager dsMgr){
        this(null,null, dsMgr);
    }
    
	/****/
	public DefaultDocSpecManager(ThreadPoolManager tpMgr, BindingManager bmgr) {
		this(tpMgr,bmgr, null);
	}
	/****/
	public DefaultDocSpecManager(ThreadPoolManager tpMgr, BindingManager bmgr, DocSpecManager dsMgr) {
        
        super(NAME,tpMgr, bmgr);

		fResolver	        = new DefaultDocSpecResolver();
		fDocSpecManager		= dsMgr;

        /* LOGGING */
        if( Application.getInstance() != null )
            {
            fLogNetworkItem    = Application.getInstance().getNetworkItem();
            fLogProcessor      = new DefaultLogProcessor(Application.getInstance().getProcessor(),
                                                         LogProcessor.TYPE_THREAD,
                                                         "Processor");
            }
        fLogUser           = new DefaultLogUser("anonymous", "Default User");
        fLogActivity    = new DefaultLogActivity(LOGACTIVITY_ResourceManager,
                                                 "DefaultDocSpecManager",
                                                 "Default DefaultDocSpecManager");
        fLogSource= new DefaultLogSource("DefaultDocSpecManager",
                                         fLogNetworkItem ,
                                         fLogProcessor,
                                         fLogActivity,
                                         fLogUser);
        
	}

    /****/
    public int getType() {return TYPE;}

	/**
	 * Get the value of DocSpecResolver.
	 * @return value of DocSpecResolver.
	 */
	public DocSpecResolver getResolver() {
		return fResolver;
	}
	
	/**
	 * Set the value of DocSpecResolver.  MUST not be <code>null</code>.
	 * @param v  Value to assign to DocSpecResolver.
	 */
	public void setResolver(DocSpecResolver  v) {
        if( v == null )
            throw new IllegalArgumentException("DocSpecResolver cannot be null.");

        if( this.fResolver != null )
            {
            this.fResolver.Close();
            }
		this.fResolver = v;
	}
	
	
	/**
	 * Get the value of DocSpecManager.
	 * @return value of DocSpecManager.
	 */
	public DocSpecManager getDocSpecManager() {
		return fDocSpecManager;
	}
	
	/**
	 * Set the value of DocSpecManager.
	 * @param v  Value to assign to DocSpecManager.
	 */
	public void setDocSpecManager(DocSpecManager  v) {
		this.fDocSpecManager = v;
	}
	

    /**
     * Retrieves InstanceFactory by specificatin and version.
     * @param specification DocSpecInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of DocSpecInstanceFactory specification
     
     */
    public DocSpecInstanceFactory findInstanceFactory(String specification,
                                                   String version) {

        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");
        QName   lKey = new QName(specification,version);

        Object lFound = fInstanceFactories.get(lKey);

        return (DocSpecInstanceFactory)lFound;
    }
	

	/**
     * Configure this instance using a Configurator.
     */
	public synchronized void Init(Configurator cfg) throws ConfigurationException {
		try 
			{
			DocSpecManagerConfigurator lCfg = (DocSpecManagerConfigurator)cfg;
			
			lCfg.Configure(this);
			}
		catch( Exception ex)
			{
			throw new ConfigurationException(true /* do logging*/,
                                             fLogSource, 
                                             LOGEVENT_ConfigurationFailure,
                                             "Failed to configure DefaultDocSpecManager due to:"+ex.toString());
			}
	}
	
	/**
	 * Opens this manager after its has been Initiated.
	 */
	public void Open() {
        super.Open();
        /* do nothing*/
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
        super.handleTimeout(tv,obj);

        /* check for regular timeout */
        if( obj == this )
            {
            }
        return 0;
    }

    /**
     * Primary mainloop when running as separate resourcemanager on its own thread.
     */
    protected void MainLoop()
    {
        super.MainLoop();
    }

	/**
	 * Closes the manager and frees used resources.
	 */
	public void Close() {
        super.Close();
        if( fResolver != null )
            {
            fResolver.Close();
            }
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

    /*----------------------------------------------------------------------*/

    /**
     * Finds a template of specified type and version that operates on a specific DocSpecInstances.
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instance  instance to use a base for template, i.e. where changes are stored.
     * @return Found template or <code>null</code> otherwise.
     */
    public DocSpecTemplate findTemplate(String templateSpecification, 
                                     String templateVersion, 
                                     DocSpecInstance instance) {
        if( instance == null )
            return null;

        String lsInstanceSpec       = instance.getSpecification();
        String lsInstanceVersion    = instance.getSpecificationVersion();

        ebXMLTemplateEntry lKey = new ebXMLTemplateEntry(templateSpecification,
                                                         templateVersion,
                                                         lsInstanceSpec,
                                                         lsInstanceVersion);
        Object lFound = fTemplates.get(lKey);
        if( lFound != null )
            {
            return ((DocSpecTemplateEntry)lFound).getTemplate();
            }
        return null;
    }

    /*----------------------------------------------------------------------*/

	/**
	 * Performs a validation of an DocSpecInstance using a predefined  ValidationContext.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,DocSpecInstance DocSpec, String context)
	throws OEXException{
        try 
			{
			String lsTarget 	  = DocSpec.getClass().getName();
			ModelCritics lCritics = fValidatorManager.ValidateDeep(
                 inv,
			     context,		/* id of context to use */
				 lsTarget,     	/* target objects class */
				 null /*this*/,			/* context */
				 null, 			/* anonymous role */
				 DocSpec);			/* target object */
			
            LogManager.Log(fLogSource,
                           LOGEVENT_ValidationSuccess,
                           new StringLogData("Validate DocSpec success")); 
			return lCritics;
			}   
        catch (Exception ex) 
            {
			try 
				{                
                throw new OEXException(true /* do logging*/,
                                       fLogSource, 
                                       LOGEVENT_ValidationFailure,
                                       "Failed to validate DocSpecInstance due to:"+ex.toString());
				} 
			catch (Exception ex1) {}
			}

		return null;
	}

	/**
	 * Performs a validation of an DocSpecInstance using the the DocSpecInstance version for lookup of the correct validation context.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,DocSpecInstance DocSpec) throws OEXException{

		DocSpecValidationContext lCtx = (DocSpecValidationContext)fValidatorManager.Context_Find(DocSpec.getSpecificationVersion());
		if( lCtx == null )
			{
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ValidationContextNotFound,
                                   "DocSpecValidationContext not found for version:"+DocSpec.getSpecificationVersion());
			}

		return Validate(inv,DocSpec, lCtx.getID());
	}


	/**
	 * Read DocSpecInstance direct from supplied inputstream using supplied handler.
     *
     * @param inv Invocation with shared contexts.
	 * @param handler handler to use.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to read from.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read DocSpecInstance or <code>null</code> if no instance was read.
	 */
	protected DocSpecInstance ReadFromStream(Invocation inv,
                                          DocSpecReadHandler handler, 
                                          String typeParameters,
                                          InputStream str,
                                          boolean validation)
	throws IOException , OEXException, ValidationException{

        try 
            {
            DocSpecInstance lFound = handler.Read(inv, typeParameters,str);
            if( lFound == null )
                {
                return lFound;
                }

            /*  Validate */
            if( validation )
                {
                ModelCritics  lCritics = Validate(inv,lFound);
                if( lCritics != null )
                    {
                    if(lCritics.getSeverityMaximum() >= ModelCritic.SEVERITY_ERROR)
                        {
                        throw new ValidationException(true /* do logging*/,
                                                      fLogSource, 
                                                      LOGEVENT_ValidationFailure,
                                                      "Failed to validate DocSpecInstance",
                                                      lCritics);
                        }
                    }
                }
            
            return lFound;
            }
        catch (IOException ex)
            {
            throw ex;
            }
        catch (ValidationException ex)
            {
            throw ex;
            }
        catch (OEXException ex)
            {
            throw ex;
            }
        catch (Exception ex)
            {
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ReadingFailure,
                                   "Reading failed due to: "+ex.toString());
            }
    }


	/**
	 * Read DocSpecInstance direct from supplied inputstream using reader matching DocSpec version.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read DocSpecInstance or <code>null</code> if no input stream or default reader
	 */
	public DocSpecInstance ReadFromStream(Invocation inv,
                                       InputStream str, 
                                       String type,
                                       String typeParameters,
                                       boolean validation)
	throws IOException , OEXException ,ValidationException{

        /* check if handler exists */
        ebXMLReadHandler lFound = findReadHandler(DocSpecReadHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
            throw new OEXException(true /* do logging*/,
                                   fLogSource, 
                                   LOGEVENT_ReadingFailure,
                                   "Failed to read DocSpecInstance due to: Missing registered reader for type:'"+type + "' typeParameters:'"+typeParameters+"'");
            }

        DocSpecReadHandler  lReader = (DocSpecReadHandler)lFound;
        return ReadFromStream(inv,lReader, typeParameters,str, validation);
	}

	/**
	 * Read DocSpecInstance using reference information to find the actual instance. The resolver mechanism is often used.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
	 * @param version DocSpec version
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read DocSpecInstance or <code>null</code> if no input stream or default reader
	 */
	public DocSpecInstance Read(Invocation inv,DocSpecReference reference,boolean validation)
	throws IOException , OEXException ,ValidationException{


        DocSpecResolverStream  lResult = fResolver.Resolve(reference);
        if( lResult == null )
            {
            return null;
            }

        /* read result  */
        DocSpecInstance lI = ReadFromStream( inv,
                                          lResult.getStream(), 
                                          lResult.getType(),
                                          lResult.getTypeParameters(),
                                          validation);
        return lI;
	}

	/**
	 * Write DocSpecInstance direct to supplied outputstream using writer matching DocSpec version.
     *
     * @param inv Invocation with shared context.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 */
	public void WriteToStream(Invocation inv,
                              DocSpecInstance instance,
                              OutputStream str, 
                              String type,
                              String typeParameters)
	throws IOException , OEXException{

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(DocSpecWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        DocSpecWriteHandler  lWriter = (DocSpecWriteHandler)lFound;
        WriteToStream(inv,lWriter, instance,typeParameters, str);
	}


	/**
	 * Write DocSpecInstance direct to supplied outputstream using writer matching DocSpec version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
	 * @return Read DocSpecInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteToStream(Invocation inv,
                                 DocSpecWriteHandler handler, 
                                 DocSpecInstance instance,
                                 String typeParameters,
                                 OutputStream str)
	throws IOException , OEXException{

        try 
            {
            handler.Write(inv, instance, typeParameters,str);
            }
        catch (IOException ex)
            {
            throw ex;
            }
        catch (OEXException ex)
            {
            throw ex;
            }
        catch (Exception ex)
            {
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Writing failed due to: "+ex.toString());
            }
    }

	/**
	 * Write DocSpecInstance direct to supplied outputstream using writer matching DocSpec version. ModelCritics is written together DocSpecInstance information.
     *
     * @param inv Invocation with shared contexts.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param critics interleave written information with critics.
	 */
	public void WriteToStream(Invocation inv,
                              DocSpecInstance instance,OutputStream str, 
                              String type, String typeParameters,
                              ModelCritics critics)
    throws IOException , OEXException {

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(DocSpecWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        DocSpecWriteHandler  lWriter = (DocSpecWriteHandler)lFound;
        WriteToStream(inv,lWriter, instance,typeParameters, str, critics);
    }

	/**
	 * Write DocSpecInstance direct to supplied outputstream using writer matching DocSpec version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
     * @param critics interleave written information with critics.
	 * @return Read DocSpecInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteToStream(Invocation inv,
                                 DocSpecWriteHandler handler, 
                                 DocSpecInstance instance,
                                 String typeParameters,
                                 OutputStream str,
                                 ModelCritics critics)
	throws IOException , OEXException{

        try 
            {
            handler.Write(inv, instance, typeParameters,str, critics);
            }
        catch (IOException ex)
            {
            throw ex;
            }
        catch (OEXException ex)
            {
            throw ex;
            }
        catch (Exception ex)
            {
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Writing failed due to: "+ex.toString());
            }
    }
}


/*.iend,DefaultDocSpecManager,==================================*/
