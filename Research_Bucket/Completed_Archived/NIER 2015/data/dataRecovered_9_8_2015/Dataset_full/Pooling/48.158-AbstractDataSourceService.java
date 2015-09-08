/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mortbay.jetty.plus;

import java.io.IOException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jndi.Util;
import org.mortbay.util.LogSupport;




/** AbstractDataSourceService
 * 
 *  Base class for DataSource Service implementations.
 * 
 *  This class provides support for plugging DataSources and
 *  DataSource Connection pooling implementations into JettyPlus.
 *  
 * A DefaultDataSourceService subclass provides a Connection pooling
 * implementation courtesy of XAPool project http://xapool.experlog.com.
 * 
 * If you wish to incorporate a different connection pooling implementation,
 * then subclass this class an implement the addDataSource(name, connectionpoolingdatasource)
 * method.
 * 
 */
public abstract class AbstractDataSourceService extends AbstractService
{
    private static Log log = LogFactory.getLog(AbstractDataSourceService.class);
    
    /**
     * Map of jndiNames to DataSource objects.
     */
    protected Map dsMap;
    
    public interface DataSourceMap
    {
        public DataSource getDataSource (String jndiName);
    }
    
    
    /**
     * DataSourceObjectFactory
     * 
     * ObjectFactory for references to DataSources bound in JNDI.
     *
     */
    public static class DataSourceObjectFactory implements ObjectFactory
    {
        private static DataSourceMap mapper = null;
        
        public static void setDataSourceMap(DataSourceMap dsm)
        {
            mapper = dsm;
        }
        
        public Object getObjectInstance(Object obj, Name name, Context ctx,
                Hashtable env) throws Exception 
        {
           
            if (obj instanceof Reference) 
            {
                Reference ref = (Reference)obj;
                
                
        	    RefAddr addr = ref.get("name");
        	    
        	    if ((addr != null) && (mapper != null)) 
        	    {
          	        return mapper.getDataSource((String)addr.getContent());
                }
            }
            return null;       
        }        
    }
    
    
    
    /**
     * Constructor.
     */
    public AbstractDataSourceService ()
    {
        //keep a static map of DataSource instances to their JNDI names
        dsMap = new HashMap();
       
        //use the static map to lookup References bound in JNDI
        DataSourceObjectFactory.setDataSourceMap(
                new DataSourceMap()
                {
            		public DataSource getDataSource (String name)
            		{
            		    return AbstractDataSourceService.this.getDataSource (name);
            		}
                }
        );
    }
    
    
    
    
    /** Add a JDBC2/3 compliant source of poolable connections.
     * 
     * The pool implementation is pluggable in JettyPlus. To use
     * a particular pool implementation (eg XAPool, DBCP etc),
     * subclass this class and implement the method createPooledDataSource()
     * to instantiate the pool. A DefaultDataSourceService is provided that uses
     * XAPool to plug in a Pool implementation.
     * 
     * The DataSource returned by this method must be capable of 
     * interacting with the pool implemenation, as the DataSource
     * will be bound into JNDI and will be accessed by webapps
     * doing a lookup on java:comp/env/<jndiName>
     * 
     * @param jndiName
     * @param cpds
     * @return
     * @throws Exception
     */
    public DataSource addConnectionPoolDataSource (String jndiName, ConnectionPoolDataSource cpds)
    throws Exception
    {
        DataSource ds = createPooledDataSource (jndiName, cpds);
        addDataSource (jndiName, ds);
        return ds;
    }
     
    
    
    /** Implement this method to create your pool implementation.
     * 
     * The method must return a javax.sql.DataSource that will be bound
     * into JNDI for client lookups on java:comp/env
     * 
     * @param jndiName
     * @param cpds
     * @return
     * @throws Exception
     */
    public abstract DataSource createPooledDataSource (String jndiName, ConnectionPoolDataSource cpds)
    throws Exception;
    
      
         
    /** Add a DataSource implementation to JettyPlus.
     * 
     * Note that the particular javax.sql.DataSource implementation
     * may implement many other features, such as internal connection
     * pooling. If this is the case, and you want to take advantage
     * 
     * 
     * @param jndiName then name the client uses to lookup the DataSource relative to java:comp/env
     * @param ds the javax.sql.DataSource implementation
     * @throws Exception
     */
    public void addDataSource (String jndiName, DataSource ds)
    throws Exception
    {
        dsMap.put(jndiName, ds);
    }
    
    
    
    
    /** Get a DataSource implementation from the list of registered DataSources.
     * @param jndiName the name of the DataSource from addDataSource()
     * @return the DataSource object
     */
    public DataSource getDataSource (String jndiName)
    {
        return (DataSource)dsMap.get(jndiName);
    }
    
    
    
    /**
     * Start the Service. Called by org.mortbay.jetty.plus.Server.start().
     * This will bind all of the registered DataSources into the global java:
     * namespace in JNDI. 
     */
    public void start()
    throws Exception
    {
        if (!isStarted())
        {      
            log.info("Starting Service "+getName());       
            
            Context ictx = null;
            Context ctx = null;
            try 
            {
                ictx = new InitialContext();
                if(log.isDebugEnabled())log.debug("InitialContext instanceof "+ictx.getClass().getName());
                if(log.isDebugEnabled())log.debug("java.naming.factory.initial="+System.getProperty("java.naming.factory.initial"));
            } 
            catch (NamingException e) 
            {
                log.warn(LogSupport.EXCEPTION,e);
                throw new IOException("No initial context: "+e);
            }
                       
            
            // Now take the data sources and register them with JNDI
            DataSource ds;
            Iterator dsItor;
            Map.Entry  entry;
            String dsName;
            
            for (dsItor = dsMap.entrySet().iterator();
            dsItor.hasNext();)
            {
                entry = (Map.Entry)dsItor.next();
                dsName = (String)entry.getKey();
                ds = (DataSource)entry.getValue();
                
                Reference ref = getDataSourceReference(dsName, ds);
                try 
                {
                    Util.bind(ictx, dsName, ref);
                    if(log.isDebugEnabled())log.debug("DataSource ref bound in JNDI with name "+dsName);
                } 
                catch (NamingException e) 
                {
                    if(log.isDebugEnabled())log.debug("DataSource ref rebind failed :" + e.getExplanation());
                    log.warn(LogSupport.EXCEPTION,e);
                    throw e;
                }           
            }
            
            super.start();
            
            log.info("Service "+getName()+" running.");
        }
        else
            log.info("Service "+getName()+" is already running");
    }
    
    
    
    /** Create a Reference for a DataSource. 
     * The Reference will be bound into JNDI. Lookups on the Reference 
     * will return the instance of the DataSource as stored in the dsMap.
     * 
     * @param jndiName
     * @param ds
     * @return
     */
    protected Reference getDataSourceReference (String jndiName, DataSource ds)
    {
        return new Reference (javax.sql.DataSource.class.getName(),
                new StringRefAddr ("name", jndiName),
                DataSourceObjectFactory.class.getName(),
                null
                );
    }
}
