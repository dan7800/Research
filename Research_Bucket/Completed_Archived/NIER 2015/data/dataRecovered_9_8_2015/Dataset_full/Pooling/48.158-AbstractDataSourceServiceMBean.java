/*
 * Created on Aug 27, 2004
 *
 * 
 * 
 */
package org.mortbay.jetty.plus.jmx;

import javax.management.MBeanException;


public class AbstractDataSourceServiceMBean extends AbstractServiceMBean
{
    
    
    public AbstractDataSourceServiceMBean()
    throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        
        defineOperation("addDataSource",
                new String[] {STRING, "javax.sql.DataSource"},
                IMPACT_ACTION);
        defineOperation ("addConnectionPoolDataSource",
                new String[] {STRING, "javax.sql.ConnectionPoolDataSource"},
                IMPACT_ACTION);
    }

}
