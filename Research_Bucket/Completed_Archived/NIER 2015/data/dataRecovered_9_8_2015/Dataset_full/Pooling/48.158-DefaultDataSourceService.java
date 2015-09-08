/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mortbay.jetty.plus;


import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.enhydra.jdbc.pool.StandardPoolDataSource;
import org.enhydra.jdbc.util.Logger;



/** DefaultDataSourceService
 * 
 * An implementation of a DataSource Service using XAPool as the
 * connection pooling implementation.
 * 
 * 
 * @author janb
 */
public class DefaultDataSourceService extends AbstractDataSourceService
{
    private static final Log log = LogFactory.getLog(DefaultDataSourceService.class);
    
    
    
    public DefaultDataSourceService ()
    {
        super();
    }

    
   
    /** 
     * Configure a DataSource that is capable of pooling Connections.
     * This is accomplished by use of the XAPool connection pooling manager.
     * To set up the pool configuration, call methods on the StandardPoolDataSource
     * return object.
     * @param jndiName name of DataSource that client will lookup in java:comp/env
     * @param cpds connection pool factory implementation
     * @return StandardPoolDataSource instance that will be registered in java:comp/env
     */
    public DataSource createPooledDataSource (String jndiName, ConnectionPoolDataSource cpds) 
    throws Exception
    {
       
        StandardPoolDataSource poolingDS = new StandardPoolDataSource (cpds);
        poolingDS.setDataSourceName ("CPDS"+jndiName); 
        //overcome bug in StandardDataSource not setting up log correctly
        poolingDS.setLogger( (new Logger(LogFactory.getLog("org.enhydra.xapool"))));
        return poolingDS;
    }
}
