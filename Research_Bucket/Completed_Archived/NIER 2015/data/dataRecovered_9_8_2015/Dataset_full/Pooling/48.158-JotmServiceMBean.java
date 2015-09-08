/*
 * Created on Jun 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mortbay.jetty.plus.jmx;

import javax.management.MBeanException;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JotmServiceMBean extends TMServiceMBean
{
    public JotmServiceMBean()
    throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        
        defineOperation("addDataSource",
                new String[] {STRING, "org.enhydra.jdbc.standard.StandardXADataSource"},
                IMPACT_ACTION);
        defineOperation ("addDataSource",
                new String[] {STRING, "org.enhydra.jdbc.standard.StandardXADataSource", "org.enhydra.jdbc.pool.StandardXAPoolDataSource"},
                IMPACT_ACTION);
    }
}
