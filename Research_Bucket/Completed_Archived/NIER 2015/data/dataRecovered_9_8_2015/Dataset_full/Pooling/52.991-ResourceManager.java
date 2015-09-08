/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,ResourceManager,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001 ---

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
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File ResourceManager.java			*/
package org.openebxml.comp.managers;

/************************************************
	Includes
\************************************************/
import java.io.*;
import java.net.*;
import java.util.*;

import org.openebxml.comp.log.*;
import org.openebxml.comp.util.*;
import org.openebxml.comp.language.execution.*;

import org.openebxml.comp.appl.*;


/**
 *  Class ResourceManager
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: ResourceManager.java,v 1.4 2003/08/19 14:51:35 awtopensource Exp $
 */

public abstract class ResourceManager  implements Runnable, Service , TimeoutHandler,Context, Configurable{

    /****/
    public static final String DEFAULT_SEPARATOR = "/";

	/*-----------------------------------------------------------------*/

    /* LOGGING */
    /****/
    public static LogEvent   LOGEVENT_ConfigurationFailure = new DefaultLogEvent(null /*parent*/, "Failed to Configure the Manager",LogEvent.GRADE_ERROR);
    /****/
	public   LogActivity     LOGACTIVITY_ResourceManager = new DefaultLogActivity(null /*parent*/, 
                                                 "ResourceManager",
                                                 "ResourceManager");;
    /*-----------------------------------------------------------------*/
    /****/
	protected   LogNetworkItem	fLogNetworkItem;
    /****/
	protected   LogActivity	    fLogActivity;
    /****/
	protected   LogProcessor	fLogProcessor;
    /****/
	protected   LogUser		    fLogUser;
    /****/
    protected   LogSource       fLogSource;

   /*-----------------------------------------------------------------*/

	/**
	 * Name of this ResourceManager instance.
	 */
	protected String fName;
	
	/**
	 * Status of whether this ServiceObject is suspended or not.
	 * (Initially false)
	 */
	protected boolean fSuspended;

    /****/
	protected boolean fActive;
	

    /**
     *  Timerqueue. Using its own internal thread.
     */
    protected   TimerQueue      fTimerQueue = null;

    /****/
    protected   TimeValue       fTimeout_Timeout;
    /****/
    protected   TimeValue       fTimeout_Interval;

    /*****/
    protected ThreadPoolManager   fThreadPoolManager;

    /****/
    protected BindingManager  fBindingManager;


    /**
     * Container BasePath
     *
     */
    protected   String      fBasePath;

    /****/
    protected String        fSeparator;

    /****/
    protected HashtableUnsync fParameters;

	/**
	 *	Constructor
	 */
    public ResourceManager(String name)
    {
        this(name, null, null);
    }
	/**
	 *	Constructor
	 */
    public ResourceManager(String name,
                           ThreadPoolManager tpMgr, 
                           BindingManager bMgr)
    {
		fName       = name;
        fActive     = true;
        fSuspended  = false;

        fTimerQueue         = new TimerQueue(true);
        fTimeout_Timeout    = new TimeValue(1);
        fTimeout_Interval   = new TimeValue(1);

        if(tpMgr == null )
            {
            tpMgr = new ThreadPoolManager();
            }
        fThreadPoolManager  = tpMgr;
        
        if(bMgr == null )
            {
            bMgr    = new BindingManager();
            }
        fBindingManager     = bMgr;

        fBasePath   = "";
        fSeparator  = System.getProperty("file.separator");

        fParameters = null;

        /* LOGGING */
        fLogNetworkItem = null;;
        fLogActivity    = null;
        fLogProcessor   = new DefaultLogProcessor(null /*parent*/,
                                                  LogProcessor.TYPE_THREAD,
                                                  "ResourceManager thread with timers.");
        fLogUser        = null;
        fLogSource      = null;
    }

	/**
	 * Get the value of NetworkItem.
	 * @return Value of NetworkItem.
	 */
	public LogNetworkItem getLogNetworkItem()
    {
		return fLogNetworkItem;
    }

	/**
	 * Get the value of Processor.
	 * @return Value of Processor.
	 */
	public LogProcessor getLogProcessor()
    {
		return fLogProcessor;
    }


	/**
	 * Get the value of Activity.
	 * @return Value of Activity.
	 */
	public LogActivity getLogActivity()
    {
		return fLogActivity;
    }


	/**
	 * Get the value of User.
	 * @return Value of User.
	 */
	public LogUser getLogUser()
    {
		return fLogUser;
    }

	/**
	 * Is this service suspended? 
	 */
	public boolean suspended ()
    {
		return fSuspended;
	}
	
	/**
	 * Return the name of the Service.  Implementation provided.
	 */
	public String getName ()
    {
		return fName;
	}
	
	/**
	 * Set the name of the Service.  Should be called when a Service is
	 * created -- this is done automatically by ServiceConfig when loading
	 * from a file.  Implementation provided.
	 */
	public void setName (String name)
    {
		fName = name;
	}
	
	/**
	 * Get the value of ThreadPoolManager.
	 * @return value of ThreadPoolManager.
	 */
	public ThreadPoolManager getThreadPoolManager() {
		return fThreadPoolManager;
	}
	
	/**
	 * Set the value of ThreadPoolManager.
	 * @param v  Value to assign to ThreadPoolManager.
	 */
	public void setThreadPoolManager(ThreadPoolManager  v) {
		this.fThreadPoolManager = v;
	}

	/**
	 * Get the value of BindingManager.
	 * @return value of BindingManager.
	 */
	public BindingManager getBindingManager() {
		return fBindingManager;
	}
	
	/**
	 * Set the value of BindingManager.
	 * @param v  Value to assign to BindingManager.
	 */
	public void setBindingManager(BindingManager  v) {
		this.fBindingManager = v;
	}

    /****/
    public String getFileSeparator()
    {
        if( fSeparator != null )
            return fSeparator;
        else
            return DEFAULT_SEPARATOR;
    }

 	/**
	 * Set the value of Name.
	 * @param v  Value to assign to Name.
	 */
	public void setBasePath(String  v) {
        if( v == null )
            return;
		this.fBasePath = v;
	}
	/**
	 * Get the Path relative to container.
	 * @return value of Property
	 */
	public String getRealPath(String name)
    {
        return fBasePath + getFileSeparator() + name;
    }
	
	/**
	 * Adds a Named Parameter . Old Parameter with same type is removed.
	 * @param name  Pame of Parameter to add.
	 * @param value  Parameter value to add.
	 */
	public void addParameter(String name, Object value)
    {
        if(name==null )
            return;

        if( fParameters == null)
            {
            fParameters = new HashtableUnsync(4);
            }
        fParameters.put(name, value, false);
    }
	
    /**
     * Retreives a Parameter based on Parameter name.
     *
	 * @param name  Pame of Parameter.
     * @return Found parameter or NULL.
     */
    public Object getParameter(String name)
    {
        if( fParameters == null)
            {
            return null;
            }
        return fParameters.get(name);
    }

    /**
     * Removes a Parameter based on Parameter name.
     *
	 * @param name  Pame of Parameter.
     * @return Found parameter or NULL.
     */
    public Object removeParameter(String name)
    {
        if( fParameters == null)
            {
            return null;
            }
        return fParameters.remove(name);
    }

	/**
	 * Get information on an active object. Overwrite this method to do
	 * anything useful. 
	 *@return null (default implementation)
	 */
	public String info ()
    {
		if(suspended())
			return "suspended";
		else
			return "xx";
	}
	
    /****/
    public Class loadClass(String className,String archiveName,String codebase) 
    throws ClassNotFoundException, IllegalAccessException,ClassCastException,InstantiationException
    {
		Class svcClass = ServiceConfig.LoadClass(className, archiveName, codebase);
        return svcClass;		
	}

    /****/
    public Object loadObject(String className,String archiveName,String codebase) 
    throws ClassNotFoundException, IllegalAccessException,ClassCastException,InstantiationException
    {
		Class svcClass = ServiceConfig.LoadClass(className, archiveName, codebase);
        Object lNew    = svcClass.newInstance();
        return lNew;		
	}


	/**
	 * Initialize object when dynamic loading occurs. Overwrite this method to do anything useful. 
     * @param params List of parameter as name-value pairs.
	 * @return -1 (default implementation)
	 */
	public int init (String [] params, Object extra)
    throws OEXException
    {
        /* Generic Arguments */
        if( params != null )
            {
            for(int i = 0; i < params.length ;i+=2)
                {
                if((i+1) > params.length)
                    break;

                if (params[i] != null 
                    && params[i+1] != null)
                    {
                    addParameter(params[i],params[i+1]);
                    }
                }/*for*/
            }


        fTimerQueue.scheduleTimer(this, this /*object*/, 
                                   fTimeout_Timeout,
                                   fTimeout_Interval);
        return 0;
	}
	
	/** 
	 * Terminate the object. Note that an object can not be explicitly
	 * unloaded. Overwrite this method to do anything useful.
	 *@return -1 (default implementation)
	 */
	public int fini ()
    {
        /* Cancel all timers */
        if( fTimerQueue != null )
            {
            fTimerQueue.cancelTimer(this);
            fTimerQueue.close();
            fTimerQueue = null;
            } 

        if ( fActive ) 
            {
            try 
                {
                fActive = false;
                } 
            catch (Exception ex) 
                {
                LogManager.Log(null, ex, true);
                return -1;
                }
            }      
        
        return 0;
	}
    
	
	/** 
	 * Called when timer expires. Overwrite this method to do anything useful. 
     * <p> Alll ResourceManagers have a default timer running where the object is set to <code>this</code>.
     * </p>
     *
	 *@param tv Time Value for when timer expired
	 *@param obj An arbitrary object that was passed to the Timer Queue
	 * (Asynchronous Completion Token)
	 *@return -1
	 */
	public abstract int handleTimeout (TimeValue tv, Object obj);
	
	/**
	 * Request that this service suspend activity.  Overwrite this
	 * method to do anything useful.  Currently, this sets an internal
	 * state variable to true.
	 */
	public int suspend () 
    {
		fSuspended = true;
		
		return 0;
	}
	
	/**
	 * Request that this service resume activity.  Currently, this sets
	 * an internal state variable to false.
	 */
	public int resume ()
    {
		fSuspended = false;
		
		return 0;
	}
	
	/**
	 *	Finilize means calling fini.
	 */
	protected void finalize() throws Throwable
    {
		fini();
	}

    /****/
    protected void MainLoop()
    {
        try {
        Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Fom Runnable interface.
     */
    public void run ()
    {
        try 
            {
            while(fActive)
                {
                MainLoop();
                }/*while*/
            } 
        catch (Exception ex) 
            {
            LogManager.Log(null, ex, true);
            } 
        finally 
            {
            fini ();
            }
    }
    
        
}


/*.IEnd,ResourceManager,====================================*/
