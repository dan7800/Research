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
package org.openebxml.ebxml.tppa.logic.impl;


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
import org.openebxml.comp.language.execution.*;


import org.openebxml.comp.appl.*;
import org.openebxml.comp.managers.*;


import org.openebxml.comp.model.*;

import org.openebxml.comp.validator.*;
import org.openebxml.comp.validator.storage.*;
import org.openebxml.comp.validator.presentation.*;

import org.openebxml.ebxml.foundation.logic.*;
import org.openebxml.ebxml.foundation.logic.template.*;
import org.openebxml.ebxml.foundation.storage.*;

import org.openebxml.ebxml.bpss.logic.*;
import org.openebxml.ebxml.docspec.logic.*;

import org.openebxml.ebxml.tppa.logic.*;
import org.openebxml.ebxml.tppa.logic.template.*;
import org.openebxml.ebxml.tppa.storage.*;

/**
 *  DefaultTPPAManager
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: DefaultTPPAManager.java,v 1.3 2003/08/20 10:16:34 awtopensource Exp $
 */

public class DefaultTPPAManager extends ebXMLSpecificationManager implements TPPAManager{


	/****/
	public static final String   NAME          = "DefaultTPPAManager";

    /*-----------------------------------------------------------------*/
	/**
	 * Document specification manager to use.
	 */
	protected DocSpecManager fDocSpecManager;

	/**
	 * Document specification manager to use.
	 */
	protected BPSSManager fBPSSManager;

	/**
     * Resolver used in finding TPA resources.
     */
	protected TPAResolver  fTPAResolver;

	/**
     * Resolver used in finding TPD resources.
     */
	protected TPDResolver  fTPDResolver;
	/**
     * Resolver used in finding TPP resources.
     */
	protected TPPResolver  fTPPResolver;


    /*-----------------------------------------------------------------*/
    /**
     *  Default constructor
     */
        public DefaultTPPAManager(){
        this((DocSpecManager)null,(BPSSManager)null);
    }
    /**
     *  Constructor with Document specification manager.to associated.
     */
        public DefaultTPPAManager(DocSpecManager dsMgr,BPSSManager bpssMgr){
        this(null,null, dsMgr,bpssMgr);
    }
    
	/****/
	public DefaultTPPAManager(ThreadPoolManager tpMgr, BindingManager bmgr) {
		this(tpMgr,bmgr, null, null);
	}
	/****/
	public DefaultTPPAManager(ThreadPoolManager tpMgr, BindingManager bmgr, DocSpecManager dsMgr,BPSSManager bpssMgr) {
        
        super(NAME,tpMgr, bmgr);

		fTPAResolver	        = new DefaultTPAResolver();
		fDocSpecManager		= dsMgr;
		fBPSSManager		= bpssMgr;

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
                                                 "DefaultTPPAManager",
                                                 "Default DefaultTPPAManager");
        fLogSource= new DefaultLogSource("DefaultTPPAManager",
                                         fLogNetworkItem ,
                                         fLogProcessor,
                                         fLogActivity,
                                         fLogUser);
        
	}

    /****/
    public int getType() {return TYPE;}


   /*----------------------------------------------------------------------*/
	/**
     * Configure this instance using a Configurator.
     */
	public synchronized void Init(Configurator cfg) throws ConfigurationException {
		try 
			{
			TPPAManagerConfigurator lCfg = (TPPAManagerConfigurator)cfg;
			
			lCfg.Configure(this);
			}
		catch( Exception ex)
			{
			throw new ConfigurationException(true /* do logging*/,
                                             fLogSource, 
                                             LOGEVENT_ConfigurationFailure,
                                             "Failed to configure DefaultTPPAManager due to:"+ex.toString());
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
        if( fTPAResolver != null )
            {
            fTPAResolver.Close();
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


    /*=====================================================================*/
    /*=====================================================================*/
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
	 * Get the value of BPSSManager.
	 * @return value of BPSSManager.
	 */
	public BPSSManager getBPSSManager() {
		return fBPSSManager;
	}
	
	/**
	 * Set the value of BPSSManager.
	 * @param v  Value to assign to BPSSManager.
	 */
	public void setBPSSManager(BPSSManager  v) {
		this.fBPSSManager = v;
	}
	
	/**
	 * Get the value of TPAResolver.
	 * @return value of TPAResolver.
	 */
	public TPAResolver getTPAResolver() {
		return fTPAResolver;
	}
	
	/**
	 * Set the value of TPAResolver.  MUST not be <code>null</code>.
	 * @param v  Value to assign to TPAResolver.
	 */
	public void setTPAResolver(TPAResolver  v) {
        if( v == null )
            throw new IllegalArgumentException("TPAResolver cannot be null.");

        if( this.fTPAResolver != null )
            {
            this.fTPAResolver.Close();
            }
		this.fTPAResolver = v;
	}
	
	/**
	 * Get the value of TPDResolver.
	 * @return value of TPDResolver.
	 */
	public TPDResolver getTPDResolver() {
		return fTPDResolver;
	}
	
	/**
	 * Set the value of TPDResolver.  MUST not be <code>null</code>.
	 * @param v  Value to assign to TPDResolver.
	 */
	public void setTPDResolver(TPDResolver  v) {
        if( v == null )
            throw new IllegalArgumentException("TPDResolver cannot be null.");

        if( this.fTPDResolver != null )
            {
            this.fTPDResolver.Close();
            }
		this.fTPDResolver = v;
	}
	
	/**
	 * Get the value of TPPResolver.
	 * @return value of TPPResolver.
	 */
	public TPPResolver getTPPResolver() {
		return fTPPResolver;
	}
	
	/**
	 * Set the value of TPPResolver.  MUST not be <code>null</code>.
	 * @param v  Value to assign to TPPResolver.
	 */
	public void setTPPResolver(TPPResolver  v) {
        if( v == null )
            throw new IllegalArgumentException("TPPResolver cannot be null.");

        if( this.fTPPResolver != null )
            {
            this.fTPPResolver.Close();
            }
		this.fTPPResolver = v;
	}
	
	
    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Retrieves TPA InstanceFactory by specificatin and version.
     * @param specification TPAInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of TPAInstanceFactory specification
     
     */
    public TPAInstanceFactory findTPAInstanceFactory(String specification,
                                                   String version) {

        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");
        QName   lKey = new QName(specification,version);

        Object lFound = fInstanceFactories.get(lKey);

        return (TPAInstanceFactory)lFound;
    }
		
    /*----------------------------------------------------------------------*/
  

    /*----------------------------------------------------------------------*/

    /**
     * Finds a template of specified TPA  type and version that operates on a specific TPAInstances.
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instance  instance to use a base for template, i.e. where changes are stored.
     * @return Found template or <code>null</code> otherwise.
     */
    public TPATemplate findTPATemplate(String templateSpecification, 
                                     String templateVersion, 
                                     TPAInstance instance) {
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
            return ((TPATemplateEntry)lFound).getTemplate();
            }
        return null;
    }

    /*----------------------------------------------------------------------*/

	/**
	 * Performs a validation of an TPAInstance using a predefined  ValidationContext.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPAInstance TPA, String context)
	throws OEXException{
        try 
			{
			String lsTarget 	  = TPA.getClass().getName();
			ModelCritics lCritics = fValidatorManager.ValidateDeep(
                 inv,
			     context,		/* id of context to use */
				 lsTarget,     	/* target objects class */
				 null /*this*/,			/* context */
				 null, 			/* anonymous role */
				 TPA);			/* target object */
			
            LogManager.Log(fLogSource,
                           LOGEVENT_ValidationSuccess,
                           new StringLogData("Validate TPA success")); 
			return lCritics;
			}   
        catch (Exception ex) 
            {
			try 
				{                
                throw new OEXException(true /* do logging*/,
                                       fLogSource, 
                                       LOGEVENT_ValidationFailure,
                                       "Failed to validate TPAInstance due to:"+ex.toString());
				} 
			catch (Exception ex1) {}
			}

		return null;
	}

	/**
	 * Performs a validation of an TPAInstance using the the TPAInstance version for lookup of the correct validation context.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPAInstance TPA) throws OEXException{

		TPAValidationContext lCtx = (TPAValidationContext)fValidatorManager.Context_Find(TPA.getSpecificationVersion());
		if( lCtx == null )
			{
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ValidationContextNotFound,
                                   "TPAValidationContext not found for version:"+TPA.getSpecificationVersion());
			}

		return Validate(inv,TPA, lCtx.getID());
	}


	/**
	 * Read TPAInstance direct from supplied inputstream using supplied handler.
     *
     * @param inv Invocation with shared contexts.
	 * @param handler handler to use.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to read from.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPAInstance or <code>null</code> if no instance was read.
	 */
	protected TPAInstance ReadTPAFromStream(Invocation inv,
                                          TPAReadHandler handler, 
                                          String typeParameters,
                                          InputStream str,
                                          boolean validation)
	throws IOException , OEXException, ValidationException{

        try 
            {
            TPAInstance lFound = handler.Read(inv, typeParameters,str);
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
                                                      "Failed to validate TPAInstance",
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
	 * Read TPAInstance direct from supplied inputstream using reader matching TPA version.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPAInstance or <code>null</code> if no input stream or default reader
	 */
	public TPAInstance ReadTPAFromStream(Invocation inv,
                                       InputStream str, 
                                       String type,
                                       String typeParameters,
                                       boolean validation)
	throws IOException , OEXException ,ValidationException{

        /* check if handler exists */
        ebXMLReadHandler lFound = findReadHandler(TPAReadHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
            throw new OEXException(true /* do logging*/,
                                   fLogSource, 
                                   LOGEVENT_ReadingFailure,
                                   "Failed to read TPAInstance due to: Missing registered reader for type:'"+type + "' typeParameters:'"+typeParameters+"'");
            }

        TPAReadHandler  lReader = (TPAReadHandler)lFound;
        return ReadTPAFromStream(inv,lReader, typeParameters,str, validation);
	}

	/**
	 * Read TPAInstance using reference information to find the actual instance. The resolver mechanism is often used.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
	 * @param version TPA version
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPAInstance or <code>null</code> if no input stream or default reader
	 */
	public TPAInstance ReadTPA(Invocation inv,TPAReference reference,boolean validation)
	throws IOException , OEXException ,ValidationException{


        TPAResolverStream  lResult = fTPAResolver.Resolve(reference);
        if( lResult == null )
            {
            return null;
            }

        /* read result  */
        TPAInstance lI = ReadTPAFromStream( inv,
                                          lResult.getStream(), 
                                          lResult.getType(),
                                          lResult.getTypeParameters(),
                                          validation);
        return lI;
	}

	/**
	 * Write TPAInstance direct to supplied outputstream using writer matching TPA version.
     *
     * @param inv Invocation with shared context.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 */
	public void WriteTPAToStream(Invocation inv,
                              TPAInstance instance,
                              OutputStream str, 
                              String type,
                              String typeParameters)
	throws IOException , OEXException{

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPAWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPAWriteHandler  lWriter = (TPAWriteHandler)lFound;
        WriteTPAToStream(inv,lWriter, instance,typeParameters, str);
	}


	/**
	 * Write TPAInstance direct to supplied outputstream using writer matching TPA version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
	 * @return Read TPAInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPAToStream(Invocation inv,
                                 TPAWriteHandler handler, 
                                 TPAInstance instance,
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
	 * Write TPAInstance direct to supplied outputstream using writer matching TPA version. ModelCritics is written together TPAInstance information.
     *
     * @param inv Invocation with shared contexts.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param critics interleave written information with critics.
	 */
	public void WriteTPAToStream(Invocation inv,
                              TPAInstance instance,OutputStream str, 
                              String type, String typeParameters,
                              ModelCritics critics)
    throws IOException , OEXException {

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPAWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPAWriteHandler  lWriter = (TPAWriteHandler)lFound;
        WriteTPAToStream(inv,lWriter, instance,typeParameters, str, critics);
    }

	/**
	 * Write TPAInstance direct to supplied outputstream using writer matching TPA version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
     * @param critics interleave written information with critics.
	 * @return Read TPAInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPAToStream(Invocation inv,
                                 TPAWriteHandler handler, 
                                 TPAInstance instance,
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

    /*=====================================================================*/
    /*=====================================================================*/

    /**
     * Retrieves TPD InstanceFactory by specificatin and version.
     * @param specification TPDInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of TPDInstanceFactory specification
     
     */
    public TPDInstanceFactory findTPDInstanceFactory(String specification,
                                                   String version) {

        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");
        QName   lKey = new QName(specification,version);

        Object lFound = fInstanceFactories.get(lKey);

        return (TPDInstanceFactory)lFound;
    }
		
    /*----------------------------------------------------------------------*/

    /**
     * Finds a template of specified TPD  type and version that operates on a specific TPDInstances.
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instance  instance to use a base for template, i.e. where changes are stored.
     * @return Found template or <code>null</code> otherwise.
     */
    public TPDTemplate findTPDTemplate(String templateSpecification, 
                                     String templateVersion, 
                                     TPDInstance instance) {
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
            return ((TPDTemplateEntry)lFound).getTemplate();
            }
        return null;
    }

    /*----------------------------------------------------------------------*/

	/**
	 * Performs a validation of an TPDInstance using a predefined  ValidationContext.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPDInstance TPD, String context)
	throws OEXException{
        try 
			{
			String lsTarget 	  = TPD.getClass().getName();
			ModelCritics lCritics = fValidatorManager.ValidateDeep(
                 inv,
			     context,		/* id of context to use */
				 lsTarget,     	/* target objects class */
				 null /*this*/,			/* context */
				 null, 			/* anonymous role */
				 TPD);			/* target object */
			
            LogManager.Log(fLogSource,
                           LOGEVENT_ValidationSuccess,
                           new StringLogData("Validate TPD success")); 
			return lCritics;
			}   
        catch (Exception ex) 
            {
			try 
				{                
                throw new OEXException(true /* do logging*/,
                                       fLogSource, 
                                       LOGEVENT_ValidationFailure,
                                       "Failed to validate TPDInstance due to:"+ex.toString());
				} 
			catch (Exception ex1) {}
			}

		return null;
	}

	/**
	 * Performs a validation of an TPDInstance using the the TPDInstance version for lookup of the correct validation context.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPDInstance TPD) throws OEXException{

		TPDValidationContext lCtx = (TPDValidationContext)fValidatorManager.Context_Find(TPD.getSpecificationVersion());
		if( lCtx == null )
			{
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ValidationContextNotFound,
                                   "TPDValidationContext not found for version:"+TPD.getSpecificationVersion());
			}

		return Validate(inv,TPD, lCtx.getID());
	}


	/**
	 * Read TPDInstance direct from supplied inputstream using supplied handler.
     *
     * @param inv Invocation with shared contexts.
	 * @param handler handler to use.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to read from.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPDInstance or <code>null</code> if no instance was read.
	 */
	protected TPDInstance ReadTPDFromStream(Invocation inv,
                                          TPDReadHandler handler, 
                                          String typeParameters,
                                          InputStream str,
                                          boolean validation)
	throws IOException , OEXException, ValidationException{

        try 
            {
            TPDInstance lFound = handler.Read(inv, typeParameters,str);
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
                                                      "Failed to validate TPDInstance",
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
	 * Read TPDInstance direct from supplied inputstream using reader matching TPD version.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPDInstance or <code>null</code> if no input stream or default reader
	 */
	public TPDInstance ReadTPDFromStream(Invocation inv,
                                       InputStream str, 
                                       String type,
                                       String typeParameters,
                                       boolean validation)
	throws IOException , OEXException ,ValidationException{

        /* check if handler exists */
        ebXMLReadHandler lFound = findReadHandler(TPDReadHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
            throw new OEXException(true /* do logging*/,
                                   fLogSource, 
                                   LOGEVENT_ReadingFailure,
                                   "Failed to read TPDInstance due to: Missing registered reader for type:'"+type + "' typeParameters:'"+typeParameters+"'");
            }

        TPDReadHandler  lReader = (TPDReadHandler)lFound;
        return ReadTPDFromStream(inv,lReader, typeParameters,str, validation);
	}

	/**
	 * Read TPDInstance using reference information to find the actual instance. The resolver mechanism is often used.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
	 * @param version TPD version
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPDInstance or <code>null</code> if no input stream or default reader
	 */
	public TPDInstance ReadTPD(Invocation inv,TPDReference reference,boolean validation)
	throws IOException , OEXException ,ValidationException{


        TPDResolverStream  lResult = fTPDResolver.Resolve(reference);
        if( lResult == null )
            {
            return null;
            }

        /* read result  */
        TPDInstance lI = ReadTPDFromStream( inv,
                                          lResult.getStream(), 
                                          lResult.getType(),
                                          lResult.getTypeParameters(),
                                          validation);
        return lI;
	}

	/**
	 * Write TPDInstance direct to supplied outputstream using writer matching TPD version.
     *
     * @param inv Invocation with shared context.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 */
	public void WriteTPDToStream(Invocation inv,
                              TPDInstance instance,
                              OutputStream str, 
                              String type,
                              String typeParameters)
	throws IOException , OEXException{

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPDWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPDWriteHandler  lWriter = (TPDWriteHandler)lFound;
        WriteTPDToStream(inv,lWriter, instance,typeParameters, str);
	}


	/**
	 * Write TPDInstance direct to supplied outputstream using writer matching TPD version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
	 * @return Read TPDInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPDToStream(Invocation inv,
                                 TPDWriteHandler handler, 
                                 TPDInstance instance,
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
	 * Write TPDInstance direct to supplied outputstream using writer matching TPD version. ModelCritics is written together TPDInstance information.
     *
     * @param inv Invocation with shared contexts.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param critics interleave written information with critics.
	 */
	public void WriteTPDToStream(Invocation inv,
                              TPDInstance instance,OutputStream str, 
                              String type, String typeParameters,
                              ModelCritics critics)
    throws IOException , OEXException {

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPDWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPDWriteHandler  lWriter = (TPDWriteHandler)lFound;
        WriteTPDToStream(inv,lWriter, instance,typeParameters, str, critics);
    }

	/**
	 * Write TPDInstance direct to supplied outputstream using writer matching TPD version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
     * @param critics interleave written information with critics.
	 * @return Read TPDInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPDToStream(Invocation inv,
                                 TPDWriteHandler handler, 
                                 TPDInstance instance,
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
    /*=====================================================================*/
    /*=====================================================================*/
    /**
     * Retrieves TPP InstanceFactory by specificatin and version.
     * @param specification TPPInstanceFactory specification. MUST not be <code>null</code>.
     * @param version version of TPPInstanceFactory specification
     
     */
    public TPPInstanceFactory findTPPInstanceFactory(String specification,
                                                   String version) {

        if( specification == null )
            throw new IllegalArgumentException("Specification identifier cannot be null.");
        QName   lKey = new QName(specification,version);

        Object lFound = fInstanceFactories.get(lKey);

        return (TPPInstanceFactory)lFound;
    }
		
    /*----------------------------------------------------------------------*/
	

    /**
     * Finds a template of specified TPP  type and version that operates on a specific TPPInstances.
     * @param templateSpecification template identifier
     * @param templateVersion  template version
     * @param instance  instance to use a base for template, i.e. where changes are stored.
     * @return Found template or <code>null</code> otherwise.
     */
    public TPPTemplate findTPPTemplate(String templateSpecification, 
                                     String templateVersion, 
                                     TPPInstance instance) {
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
            return ((TPPTemplateEntry)lFound).getTemplate();
            }
        return null;
    }

    /*----------------------------------------------------------------------*/

	/**
	 * Performs a validation of an TPPInstance using a predefined  ValidationContext.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPPInstance TPP, String context)
	throws OEXException{
        try 
			{
			String lsTarget 	  = TPP.getClass().getName();
			ModelCritics lCritics = fValidatorManager.ValidateDeep(
                 inv,
			     context,		/* id of context to use */
				 lsTarget,     	/* target objects class */
				 null /*this*/,			/* context */
				 null, 			/* anonymous role */
				 TPP);			/* target object */
			
            LogManager.Log(fLogSource,
                           LOGEVENT_ValidationSuccess,
                           new StringLogData("Validate TPP success")); 
			return lCritics;
			}   
        catch (Exception ex) 
            {
			try 
				{                
                throw new OEXException(true /* do logging*/,
                                       fLogSource, 
                                       LOGEVENT_ValidationFailure,
                                       "Failed to validate TPPInstance due to:"+ex.toString());
				} 
			catch (Exception ex1) {}
			}

		return null;
	}

	/**
	 * Performs a validation of an TPPInstance using the the TPPInstance version for lookup of the correct validation context.
	 *
     * @param inv Invocation with shared contexts.
	 */
	public ModelCritics  Validate(Invocation inv,TPPInstance TPP) throws OEXException{

		TPPValidationContext lCtx = (TPPValidationContext)fValidatorManager.Context_Find(TPP.getSpecificationVersion());
		if( lCtx == null )
			{
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_ValidationContextNotFound,
                                   "TPPValidationContext not found for version:"+TPP.getSpecificationVersion());
			}

		return Validate(inv,TPP, lCtx.getID());
	}


	/**
	 * Read TPPInstance direct from supplied inputstream using supplied handler.
     *
     * @param inv Invocation with shared contexts.
	 * @param handler handler to use.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to read from.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPPInstance or <code>null</code> if no instance was read.
	 */
	protected TPPInstance ReadTPPFromStream(Invocation inv,
                                          TPPReadHandler handler, 
                                          String typeParameters,
                                          InputStream str,
                                          boolean validation)
	throws IOException , OEXException, ValidationException{

        try 
            {
            TPPInstance lFound = handler.Read(inv, typeParameters,str);
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
                                                      "Failed to validate TPPInstance",
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
	 * Read TPPInstance direct from supplied inputstream using reader matching TPP version.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPPInstance or <code>null</code> if no input stream or default reader
	 */
	public TPPInstance ReadTPPFromStream(Invocation inv,
                                       InputStream str, 
                                       String type,
                                       String typeParameters,
                                       boolean validation)
	throws IOException , OEXException ,ValidationException{

        /* check if handler exists */
        ebXMLReadHandler lFound = findReadHandler(TPPReadHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
            throw new OEXException(true /* do logging*/,
                                   fLogSource, 
                                   LOGEVENT_ReadingFailure,
                                   "Failed to read TPPInstance due to: Missing registered reader for type:'"+type + "' typeParameters:'"+typeParameters+"'");
            }

        TPPReadHandler  lReader = (TPPReadHandler)lFound;
        return ReadTPPFromStream(inv,lReader, typeParameters,str, validation);
	}

	/**
	 * Read TPPInstance using reference information to find the actual instance. The resolver mechanism is often used.
     *
     * @param inv Invocation with shared contexts.
	 * @param str stream to read from.
	 * @param version TPP version
     * @param validation <code>true</code> if validation should be performed.
	 * @return Read TPPInstance or <code>null</code> if no input stream or default reader
	 */
	public TPPInstance ReadTPP(Invocation inv,TPPReference reference,boolean validation)
	throws IOException , OEXException ,ValidationException{


        TPPResolverStream  lResult = fTPPResolver.Resolve(reference);
        if( lResult == null )
            {
            return null;
            }

        /* read result  */
        TPPInstance lI = ReadTPPFromStream( inv,
                                          lResult.getStream(), 
                                          lResult.getType(),
                                          lResult.getTypeParameters(),
                                          validation);
        return lI;
	}

	/**
	 * Write TPPInstance direct to supplied outputstream using writer matching TPP version.
     *
     * @param inv Invocation with shared context.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 */
	public void WriteTPPToStream(Invocation inv,
                              TPPInstance instance,
                              OutputStream str, 
                              String type,
                              String typeParameters)
	throws IOException , OEXException{

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPPWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPPWriteHandler  lWriter = (TPPWriteHandler)lFound;
        WriteTPPToStream(inv,lWriter, instance,typeParameters, str);
	}


	/**
	 * Write TPPInstance direct to supplied outputstream using writer matching TPP version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
	 * @return Read TPPInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPPToStream(Invocation inv,
                                 TPPWriteHandler handler, 
                                 TPPInstance instance,
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
	 * Write TPPInstance direct to supplied outputstream using writer matching TPP version. ModelCritics is written together TPPInstance information.
     *
     * @param inv Invocation with shared contexts.
     * @param instance instance to write.
	 * @param str stream to write into.
     * @param type Type of data in InputStream
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
     * @param critics interleave written information with critics.
	 */
	public void WriteTPPToStream(Invocation inv,
                              TPPInstance instance,OutputStream str, 
                              String type, String typeParameters,
                              ModelCritics critics)
    throws IOException , OEXException {

        /* check if handler exists */
        ebXMLWriteHandler lFound = findWriteHandler(TPPWriteHandler.INSTANCECLASS,type);
        if( lFound == null)
            {/*no handler found */
			throw new OEXException(true,
                                   fLogSource,
                                   LOGEVENT_WritingFailure,
                                   "Reading failed due missing WriteHandler for type: "+type);
            }

        TPPWriteHandler  lWriter = (TPPWriteHandler)lFound;
        WriteTPPToStream(inv,lWriter, instance,typeParameters, str, critics);
    }

	/**
	 * Write TPPInstance direct to supplied outputstream using writer matching TPP version.
     *
     * @param inv Invocation with shared context.
     * @param handler handler to use.
     * @param instance instance to write.
     * @param typeParameters additional type parameters. MAY be <code>null</code>.
	 * @param str stream to write into.
     * @param critics interleave written information with critics.
	 * @return Read TPPInstance or <code>null</code> if no instance was read.
	 */
	protected void WriteTPPToStream(Invocation inv,
                                 TPPWriteHandler handler, 
                                 TPPInstance instance,
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


/*.iend,DefaultTPPAManager,==================================*/
