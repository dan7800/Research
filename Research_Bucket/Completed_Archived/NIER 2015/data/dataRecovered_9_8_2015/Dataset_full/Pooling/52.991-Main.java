/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,Main,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001-2002 ---

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
/*.IA,	PUBLIC Include File Main.java			*/
package org.openebxml.ebxml.bpss.appl.bpssvalidator;

/************************************************
	Includes
\************************************************/
import java.io.IOException;              /* JME CLDC 1.0 */
import java.io.InputStream;              /* JME CLDC 1.0 */
import java.io.OutputStream;             /* JME CLDC 1.0 */
import java.io.FileInputStream;          /* JME CLDC 1.0 */
import java.io.FileOutputStream;         /* JME CLDC 1.0 */
import java.io.File;
import java.lang.StringBuffer;           /* JME CLDC 1.0 */
import java.util.Enumeration;			 /* JME CLDC 1.0 */

import java.io.FileWriter;


import org.w3c.dom.Document ;   /*.TODO change into BML */
import org.w3c.dom.Element ;    /*.TODO change into BML */
import org.w3c.dom.Node ;       /*.TODO change into BML */
import org.w3c.dom.NodeList ;   /*.TODO change into BML */

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


import org.openebxml.comp.util.*;

import org.openebxml.comp.bml.*;
import org.openebxml.comp.bml.data.*;
import org.openebxml.comp.language.execution.*;

import org.openebxml.comp.log.*;
import org.openebxml.comp.log.system.*;

import org.openebxml.comp.xml.jdk.*;

import org.openebxml.comp.appl.Application;

import org.openebxml.comp.model.*;

import org.openebxml.comp.validator.*;
import org.openebxml.comp.validator.storage.*;
import org.openebxml.comp.validator.presentation.*;

import org.openebxml.ebxml.docspec.logic.*;
import org.openebxml.ebxml.docspec.logic.impl.*;

import org.openebxml.ebxml.bpss.logic.*;
import org.openebxml.ebxml.bpss.logic.impl.*;
import org.openebxml.ebxml.bpss.storage.*;
import org.openebxml.ebxml.bpss.storage.xml.*;

/*.TODO remove import org.openebxml.ebxml.bpss.presentation.test.*; */


/**
 *  BPSSInstance validator application that can handle reading, writing and walidating of BPSSInstances that adhere to different specifications (API's).
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: Main.java,v 1.6 2002/08/25 09:12:06 awtopensource Exp $
 */

public class Main  extends Application {

    /****/
    public static final int PHASE_ARGUMENTS    = 64;

    /****/
    protected static final int FILES_INITIAL    = 8;

    /****/
    public static final String  DEFAULT_TYPE    = "text/xml";
    /****/
    public static final String  DEFAULT_TYPEPARAMETERS    = null;
    /*-------------------------------------------------------------*/
	protected	boolean		fDebug;

	protected	String 	fConfig_URL;

    /****/
    protected   DocSpecManager fDocSpecManager;

    /****/
    protected   BPSSManager fBPSSManager;

	/****/
	protected	ModelCritics		fModelCritics;

    /****/
    protected String    fArgument_Files[];
    /****/
    protected int       fArgument_Files_No;

    /****/
    protected boolean   fValidating;

    /****/
    protected boolean   fWriteToFile;

    /****/
    protected boolean   fWriteToFile_Critics;

	/****/
	protected String 	fWriteToFile_Filename;

    /****/
	protected String 	fPrinting;

    /****/
    protected String fValidatorContextName;

    /****/
    protected boolean fResolver;

    /****/
    protected   String  fType;
    /****/
    protected   String  fTypeParameters;
    /**
     *  Constructor
     */
    public Main()
    {
		super("bpssvalidator");	
        fConfig_URL	    = "bpssvalidator.xml";

        /* For logging */
        fNetworkItem    = new DefaultLogNetworkItem(null,"",
                                                    "Default NetworkItem");
        fActivity       = new DefaultLogActivity(null, fName,
                                                 "BPSSValidator");
        fProcessor      = new DefaultLogProcessor(null,
                                                  LogProcessor.TYPE_APPLICATION,
                                                  "Processor");
        fUser           = new DefaultLogUser("anonymous",
                                             "Default User");
        
        fDocSpecManager = new DefaultDocSpecManager();
        fBPSSManager    = new DefaultBPSSManager((ThreadPoolManager)null /*ThreadPoolManager tpMgr*/,
                                                 (BindingManager)null /*BindingManager bMgr*/,
                                                 fDocSpecManager);

		fDebug		    = false;
		fModelCritics	= new ModelCritics();

        fValidating 	= false;

		fWriteToFile	        = false;
		fWriteToFile_Critics	= false;
		fWriteToFile_Filename = "validated";

		fPrinting	    = "console";

        fArgument_Files         = new String[FILES_INITIAL];
        fArgument_Files_No      = 0;

        fValidatorContextName   = null;
        fResolver               = false;
        fType                   = DEFAULT_TYPE;
        fTypeParameters         = DEFAULT_TYPEPARAMETERS;
    }

    /**
     * Returns configuration filename/URL.
     */
    public  String  getConfigURL()
    {
		return fConfig_URL;
	}


    /**
     * Opportunity to preload classes.
     */
	public  void	    PreloadClasses()
    throws Exception
    {
	}

	/**
	 * Initiation phase, called before main loop.
	 */
    protected  void    DoInit(int phase, String args[])
    throws Exception
    {
		switch(phase) {
		case PHASE_FIRST:
          getProcessor().addLoggingEventListener(new SystemLoggingEventListener());
		  break;

        case PHASE_CONFIG_LOAD: /* Load configuration file */
          break;

        case PHASE_SERVICE_LOAD_UNLOAD:  /* load services */
          break;

        case PHASE_ARGUMENTS:
//          fSHS_Product      = System.getProperty(SHS_PRODUCT);


          Arguments lArguments = new Arguments();
          lArguments.setUsage( new String[] {
          "usage: java org.openebxml.ebxml.bpss.appl.bpssvalidator.Main (options) filename(s)",
          "",
          "options:",
          "  -s             Validating using XML Schema or DTD",
          "  -c <name>      Name  of ModelValidatorContext to use",
		  "  -r             Treat filename as BPPSInstance ID and use BPSS resolver instead of reading from file",
		  "  -w <suffix>    Write validated BPSS to a file with specified suffix",
		  "  -wc <suffix>   Write validated BPSS to a file with specified suffix",
          "                 Interleave Critics in file as ProcessingInstructions.",
		  "  -p <xml|ui|console>   Print critics to screen using XML, UI or console",
		  "  -d             Verbose printing of  debug information",
          "  -h | -?        This help screen."
		  } );
          
          lArguments.Parse(args , new String[] { "c", "w", "wc", "p"} );
          int   lcChar;
          String lsArgument = null; 

          while ( (lsArgument =  lArguments.nextSwitch()) != null )  
              {              
              switch (lsArgument.charAt(0)) {
              case 'c':
                fValidatorContextName = lArguments.nextSwitchParameter();
                break;
                                
              case 'w':
                fWriteToFile = true;
                fWriteToFile_Filename = lArguments.nextSwitchParameter();
                if(lsArgument.equals("wc" ) )
                    fWriteToFile_Critics = true;
                else
                    fWriteToFile_Critics = false;
                break;
                
              case 'p':
                fPrinting  = lArguments.nextSwitchParameter();
                break;
                
                
              default:
                break;
              }/*switch*/
             

              }/*while argument*/

          while ( (lsArgument =  lArguments.nextSingleSwitch()) != null )
              {              
              switch (lsArgument.charAt(0)) {
                
              case 'r':
                fResolver = true;
                break;
                
              case 's':
                fValidating = true;
                break;
                                
              case 'd':
                fDebug	= true;
                break;
                
              case '?':
              case 'h':
                lArguments.printUsage();
                System.exit(1);
                break;
                
              default:
                break;
              }/*switch*/
              }/*while argument*/

          while( (lsArgument = lArguments.nextParameter()) != null )
              {
              /* append to list of files */
              if( fArgument_Files.length == fArgument_Files_No)
                  {
                  String[] lNew = new String[fArgument_Files.length*2];
                  System.arraycopy(fArgument_Files,0, lNew, 0, fArgument_Files_No);
                  fArgument_Files = lNew;
                  }
              fArgument_Files[fArgument_Files_No] = lsArgument;
              fArgument_Files_No++;
              }/*while  file */

          /* Check no arguments */
          if(fArgument_Files_No == 0 )
              {
              lArguments.printUsage();
              System.exit(1);
              }
          break;

		case PHASE_LAST:    /* last phase */ 
        {
        Element  lElem = fConfig_Document.getDocumentElement();
        BPSSManagerDOMConfigurator lCfg = new BPSSManagerDOMConfigurator(lElem);
        fBPSSManager.Init(lCfg);
        fBPSSManager.Open();
        }
        break;
		default:
		  break;
		}/*switch*/
	}

	/**
	 * Exiting methid, called after MainLoop has finished.
	 */
    protected  void    DoDone(int phase)
    throws Exception
    {
		switch(phase) {
		case PHASE_LAST:
          fBPSSManager.Close();
		  break;
        case PHASE_SERVICE_LOAD_UNLOAD:
           break;
        case PHASE_CONFIG_LOAD:
          break;
		case PHASE_FIRST:
		  break;
		default:
		  break;
		}/*switch*/
	}


    /****/
    protected void HandleResult(String identifier, BPSSInstance instance) {
        try
            {
			if(fModelCritics.size() > 0 )
				{
                StringBuffer lSB = new StringBuffer();
				ModelCriticsPrinter lPrinter; 
				if( "xml".equals(fPrinting) )
					{
					lPrinter = new XMLCriticsPrinter();
                    lPrinter.Print(fModelCritics, lSB);
                    System.err.println(lSB.toString());
					}
				else if( "ui".equals(fPrinting) )
					{
					lPrinter = new UICriticsPrinter();                    
                    lPrinter.Print(fModelCritics,lSB);
					}
				else
					{
					lPrinter = new ConsoleCriticsPrinter();
                    lPrinter.Print(fModelCritics, lSB);
                    System.err.println(lSB.toString());
					}
				}
            }
        catch (Exception ex)
            {/*.TODO log*/
            System.err.println("EXCEPTION "+ex.toString());
            ex.printStackTrace();
            return;
            }

        /*------------ Resultfile interleaved with critics -------------*/
        try
            {
			if( fWriteToFile )
				{
                /*.TODO implement writing */
                String lsFileName = identifier+"."+fWriteToFile_Filename;
				FileOutputStream lOUT = new FileOutputStream(new File(lsFileName));
                
                try 
                    {
                    DefaultInvocation lINV = new DefaultInvocation(fBPSSManager);
                    if( fWriteToFile_Critics )
                        {
                        fBPSSManager.WriteToStream(lINV,instance, lOUT, 
                                                   fType, fTypeParameters,
                                                   fModelCritics);
                        }
                    else
                        {
                        fBPSSManager.WriteToStream(lINV,instance, lOUT, 
                                                   fType, fTypeParameters);
                        }

                    lOUT.flush();
                    lOUT.close();
                    }
                catch(IOException ex)
                    {
                    System.err.println("IO-EXCEPTION "+ex.toString());
                    try {
                    lOUT.close();
                    } catch(Exception ext) {}
                    }
                catch(OEXException ex)
                    {
                    System.err.println("OEXEXCEPTION "+ex.toString());
                    ex.printStackTrace();
                    try {
                    lOUT.close();
                    } catch(Exception ext) {}
                    }
				}
            }
        catch (IOException ex)
            {/*.TODO log*/
            System.err.println("IOEXCEPTION "+ex.toString());
            ex.printStackTrace();
            }
        catch (Exception ex)
            {/*.TODO log*/
            System.err.println("EXCEPTION "+ex.toString());
            ex.printStackTrace();
            }

    }

    /**
     * Validate using the resolver framework.
     */
    protected boolean ValidateResolver(String identifier)
    {
        ModelCritics    lCritics;
		ModelCritics    lList;
        BPSSInstance    lInstance   = null;

		/** INIT **/
		fModelCritics.clear();

        try 
            {
            BPSSReference   lRef = new BPSSReference(identifier);
            
            DefaultInvocation lINV = new DefaultInvocation(fBPSSManager);
            lInstance = fBPSSManager.Read(lINV,lRef, false);
            if( lInstance == null)
                {
                System.err.println("Failed to Resolve:"+identifier);
                return false;
                }
            lCritics = fBPSSManager.Validate(lINV,lInstance, fValidatorContextName);
            fModelCritics.append(lCritics);
            }
        catch (ValidationException  ex)
            {
            System.err.println("Validation EXCEPTION "+ex.toString());
            return false;
            }        
        catch (OEXException  ex)
            {
            System.err.println("OEXEXCEPTION "+ex.toString());
            return false;
            }        
        catch (IOException ex)
            {/*.TODO log*/
            System.err.println("IOEXCEPTION "+ex.toString());
            return false;
            }
        catch (Exception ex)
            {/*.TODO log*/
            System.err.println("EXCEPTION "+ex.toString());
            ex.printStackTrace();
            return false;
            }

        HandleResult(identifier,lInstance);

        return true;
    }

    /**
     * Validate a specific file.
     */
    protected boolean ValidateFile(String filename)
    {
        FileInputStream lIS;
        ModelCritics    lCritics;
		ModelCritics    lList;
        BPSSInstance    lInstance = null;

		/** INIT **/
		fModelCritics.clear();
        
        /*----------- read into MEMORY MODEL ----------*/
        try 
            {
            lIS = new FileInputStream(filename);

            DefaultInvocation lINV = new DefaultInvocation(fBPSSManager);
            lInstance = fBPSSManager.ReadFromStream(lINV,lIS, 
                                                    fType, fTypeParameters,
                                                    false);

            /*.TODO test remove 
            new TestFrame((ModelManagement)lInstance).show();
            try {
            Thread.sleep(1000);
            } catch (Throwable ex){}
            */
        /*.TODO XML Validation */
//        if( fValidating )
//            {
//            try 
//                {
//                lIS = new FileInputStream(filename);
//                lCritics = fCMDValidation.Validate(this ,lIS);
//                fModelCritics.append(lCritics);
//                }
//            catch (IOException ex)
//                {/*.TODO log*/
//                System.err.println("EXCEPTION "+ex.toString());
//                return false;
//                }
//
//            }/*if validation */



            lCritics = fBPSSManager.Validate(lINV,lInstance, fValidatorContextName);
            fModelCritics.append(lCritics);
            }
        catch (ValidationException  ex)
            {
            System.err.println("Validation EXCEPTION "+ex.toString());
            return false;
            }        
        catch (OEXException  ex)
            {
            System.err.println("OEXEXCEPTION "+ex.toString());
            return false;
            }        
        catch (IOException ex)
            {/*.TODO log*/
            System.err.println("IOEXCEPTION "+ex.toString());
            return false;
            }
        catch (Exception ex)
            {/*.TODO log*/
            System.err.println("IOEXCEPTION "+ex.toString());
            ex.printStackTrace();
            return false;
            }

        HandleResult(filename,lInstance);

        return true;
    }

	/*---------------------------------------------------------*/
	/**
	 * Main loop. called after init and before done.
	 */
	protected void	MainLoop()
    throws Exception
    {
        for(int i = 0; i < fArgument_Files_No; i++)
            {
            String lsFilename = fArgument_Files[i];
            boolean lbResult;
            if( ! fResolver ) 
                {            
                lbResult = ValidateFile(lsFilename);
                }/*if*/
            else
                {
                lbResult = ValidateResolver(lsFilename);
                }
            
            if( lbResult )
                {
                System.err.println("OK '" +lsFilename+"'" );                
                }
            else
                {
                System.err.println("FAILED '" +lsFilename+"'" );
                }
            }/*for*/
    }

	/*---------------------------------------------------------*/
    /**
     * Main entrypoint for application.
     */
	public static void main(String args[])
    throws Exception
    {
        if(args.length < 1)
            {
            System.err.println("Must supply at least one argument");
            System.exit(1);
            }
            
		Main	lAppl  = new Main();

        try
            {
            lAppl.Run(args);
            }
        catch(Exception ex)
            {   
            System.err.println("Exception caught");
            ex.printStackTrace();
            System.exit(1);
            }
        System.exit(0);
	}


}


/*.IEnd,Main,====================================*/
