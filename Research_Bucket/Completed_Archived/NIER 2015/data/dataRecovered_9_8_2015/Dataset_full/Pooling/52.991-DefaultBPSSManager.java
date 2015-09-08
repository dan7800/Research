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
package org.openebxml.ebxml.bpss.logic.impl;


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

import org.openebxml.ebxml.bpss.logic.*;
import org.openebxml.ebxml.bpss.logic.template.*;
import org.openebxml.ebxml.bpss.storage.*;

/**
 *  DefaultBPSSManager
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: DefaultBPSSManager.java,v 1.10 2003/08/20 10:16:33 awtopensource Exp $
 */

public class DefaultBPSSManager extends ebXMLSpecificationManager implements  BPSSManager{


	/****/
	public static final String   NAME          = "DefaultBPSSManager";

    /*-----------------------------------------------------------------*/
	/**
     * Resolver used in finding resources.
     */
	protected BPSSResolver  fResolver;

	/**
	 * Document specification manager to use.
	 */
	protected DocSpecManager fDocSpecManager;

    /*-----------------------------------------------------------------*/
    /**
     *  Default constructor
     */
    public DefaultBPSSManager(){
        this(null,null);
    }
    /**
     *  Constructor with Document specification manager.to associated.
     */
    public DefaultBPSSManager(DocSpecManager dsMgr){
        this(null,null, dsMgr);
    }
    
	/****/
	public DefaultBPSSManager(ThreadPoolManager tpMgr, BindingManager bmgr) {
		this(tpMgr,bmgr, null);
	}
	/****/
	public DefaultBPSSManager(ThreadPoolManager tpMgr, BindingManager bmgr,
							  DocSpecManager dsMgr) {
        
        super(NAME,tpMgr, bmgr);

		fResolver	        = new DefaultBPSSResolver();
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
                                                 "DefaultBPSSManager",
                                                 "Default DefaultBPSSManager");
        fLogSource= new DefaultLogSource("DefaultBPSSManager",
                                         fLogNetworkItem ,
                                         fLogProcessor,
                                         fLogActivity,
                                         fLogUser);
        
	}

    /****/
    public int getType() {return TYPE;}

	/**
	 * Get the value of BPSSResolver.
	 * @return value of BPSSResolver.
	 */
	public BPSSResolver getResolver() {
		return fResolver;
	}
	
	/**
	 * Set the value of BPSSResolver.  MUST not be <code>null</code>.
	 * @param v  Value to assign to BPSSResolver.
	 */
	public void setResolver(BPSSResolver  v) {
        if( v == null )
            throw new IllegalArgumentException("BPSSResolver cannot be null.");

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
     * @param specification BPSSInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of BPSSInstanceFactory specification
     
     */
    public BPSSInstanceFactory findInstanceFactory(String specification,
                                                   String version) {

        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");
        QName   lKey = new QName(specification,version);

        Object lFound = fInstanceFactories.get(lKey);

        return (BPSSInstanceFactory)lFound;
    }
		

	/**
     * Configure this instance using a Configurator.
     */
	public synchronized void Init(Configurator cfg) throws ConfigurationException {
		try 
			{
			BPSSManagerConfigurator lCfg = (BPSSManagerConfigurator)cfg;
			
			lCfg.Configure(this);
			}
		catch( Exception ex)
			{
			throw new ConfigurationException(true /* do logging*/,
                                             fLogSource, 
                                             LOGEVENT_ConfigurationFailure,
                                             "Failed to configure DefaultBPSSManager due to:"+ex.toString());
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
     * Finds a template of specified type and version that operates on a specific BPSSInstances.
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instance  instance to use a base for template, i.e. where changes are stored.
     * @return Found template or <code>null</code> otherwise.
     */
    public BPSSTemplate findTemplate(String templateSpecification, 
                                     String templateVersion, 
                                     BPSSInstance instance) {
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
            return ((BPSSTemplateEntry)lFound).getTemplate();
            }
        return null;
    }

    /*----------------------------------------------------------------------*/

	/**
	 * Performs a validation of an BPSSInstance using a predefined  ValidationContext.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,BPSSInstance bpss, String context)
	throws OEXException{
        try 
			{
			String lsTarget 	  = bpss.getClass().getName();
			ModelCritics lCritics = fValidatorManager.ValidateDeep(
                 inv,
			     context,		/* id of context to use */
				 lsTarget,     	/* target objects class */
				 null /*this*/,			/* context */
				 null, 			/* anonymous role */
				 bpss);			/* target object */
			
            LogManager.Log(fLogSource,
                           LOGEVENT_ValidationSuccess,
                           new StringLogData("Validate BPSS success")); 
			return lCritics;
			}   
        catch (Exception ex) 
            {
            ex.printStackTrace();
			try 
				{                
                throw new OEXException(true /* do logging*/,
                                       fLogSource, 
                                       LOGEVENT_ValidationFailure,
                                       "Failed to validate BPSSInstance due to:"+ex.toString());
				} 
			catch (Exception ex1) {}
			}

		return null;
	}

	/**
	 * Performs a validation of an BPSSInstance using the the BPSSInstance version for lookup of the correct validation context.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,BPSSInstance bpss) throws OEXException{

		BPSSValidationContext lCtx = (BPSSValidationContext)fValidatorManager.Context_Find(bpss.getSpecificationVersion());
		if( lCtx == null )
			{
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ValidationContextNotFound,
                                   "BPSSValidationContext not found for version:"+bpss.getSpecificationVersion());
			}

		return Validate(inv,bpss, lCtx.getID());
	}


	/**
	 * Read BPSSInstance direct from supplied inputstream using supplied handler.
     *
     * @param inv Invocation with shared contexts.
	 * @param handler handler to use.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to read from.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read BPSSInstance or <code>null</code> if no instance was read.
	 */
	protected BPSSInstance ReadFromStream(Invocation inv,
                                          BPSSReadHandler handler, 
                                          String typeParameters,
                                          InputStream str,
                                          boolean validation)
	throws IOException , OEXException, ValidationException{

        try 
            {
            BPSSInstance lFound = handler.Read(inv, typeParameters,str);
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
                                                      "Failed to validate BPSSInstance",
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
	 * Read BPSSInstance direct from supplied inputstream using reader matching BPSS version.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read BPSSInstance or <code>null</code> if no input stream or default reader
	 */
	public BPSSInstance ReadFromStream(Invocation inv,
                                       InputStream str, 
                                       String type,
                                       String typeParameters,
                                       boolean validation)
	throws IOException , OEXException ,ValidationException{

        /* check if handler exists */
        ebXMLReadHandler lFound = findReadHandler(BPSSReadHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
            throw new OEXException(true /* do logging*/,
                                   fLogSource, 
                                   LOGEVENT_ReadingFailure,
                                   "Failed to read BPSSInstance due to: Missing registered reader for type:'"+type + "' typeParameters:'"+typeParameters+"'");
            }

        BPSSReadHandler  lReader = (BPSSReadHandler)lFound;
        return ReadFromStream(inv,lReader, typeParameters,str, validation);
	}

	/**
	 * Read BPSSInstance using reference information to find the actual instance. The resolver mechanism is often used.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
	 * @param version BPSS version
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read BPSSInstance or <code>null</code> if no input stream or default reader
	 */
	public BPSSInstance Read(Invocation inv,BPSSReference reference,boolean validation)
	throws IOException , OEXException ,ValidationException{


        BPSSResolverStream  lResult = fResolver.Resolve(reference);
        if( lResult == null )
            {
            return null;
            }

        /* read result  */
        BPSSInstance lI = ReadFromStream( inv,
                                          lResult.getStream(), 
                                          lResult.getType(),
                                          lResult.getTypeParameters(),
                                          validation);
        return lI;
	}

	/**
	 * Write BPSSInstance direct to supplied outputstream using writer matching BPSS version.
     *
     * @param inv Invocation with shared context.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 */
	public void WriteToStream(Invocation inv,
                              BPSSInstance instance,
                              OutputStream str, 
                              String type,
                              String typeParameters)
	throws IOException , OEXException{

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(BPSSWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        BPSSWriteHandler  lWriter = (BPSSWriteHandler)lFound;
        WriteToStream(inv,lWriter, instance,typeParameters, str);
	}


	/**
	 * Write BPSSInstance direct to supplied outputstream using writer matching BPSS version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
	 * @return Read BPSSInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteToStream(Invocation inv,
                                 BPSSWriteHandler handler, 
                                 BPSSInstance instance,
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
	 * Write BPSSInstance direct to supplied outputstream using writer matching BPSS version. ModelCritics is written together BPSSInstance information.
     *
     * @param inv Invocation with shared contexts.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param critics interleave written information with critics.
	 */
	public void WriteToStream(Invocation inv,
                              BPSSInstance instance,OutputStream str, 
                              String type, String typeParameters,
                              ModelCritics critics)
    throws IOException , OEXException {

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(BPSSWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        BPSSWriteHandler  lWriter = (BPSSWriteHandler)lFound;
        WriteToStream(inv,lWriter, instance,typeParameters, str, critics);
    }

	/**
	 * Write BPSSInstance direct to supplied outputstream using writer matching BPSS version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
     * @param critics interleave written information with critics.
	 * @return Read BPSSInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteToStream(Invocation inv,
                                 BPSSWriteHandler handler, 
                                 BPSSInstance instance,
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


/*.iend,DefaultBPSSManager,==================================*/
