/*
 * @(#) DataBaseLayer.java
 *
 * JOTM: Java Open Transaction Manager 
 *
 * This project is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: DataBaseLayer.java,v 1.3 2003/04/11 14:43:34 jmesnil Exp $
 * --------------------------------------------------------------------------
 */

import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.enhydra.jdbc.pool.StandardXAPoolDataSource;

/**
 * @author jmesnil
 */
public class DataBaseLayer {

    private static TransactionManager tm;

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(ClassLoader.getSystemResourceAsStream("spy.properties"));
        } catch (Exception e) {
            System.err.println("no properties file found to init the database");
            System.exit(1);
        }

        System.out.println("\n database configuration:");
        props.list(System.out);
        System.out.println("------------------------\n");

        try {
            TMService jotm = new Jotm(true, true);
            tm = jotm.getTransactionManager();

            StandardXAPoolDataSource ds = null;
            StandardXADataSource xads = new StandardXADataSource();
            ds = new StandardXAPoolDataSource(xads);
            ds.setVerbose(true);
            ds.setDebug(true);

            xads.setDriverName(props.getProperty("driver"));
            xads.setUrl(props.getProperty("url"));
            xads.setUser(props.getProperty("login"));
            xads.setPassword(props.getProperty("password"));
            xads.setTransactionManager(tm);

            ds.setUser(props.getProperty("login"));
            ds.setPassword(props.getProperty("password"));
            ds.setDataSource(xads);
            ds.setDataSourceName("XADataSource");

            InitialContext ictx = new InitialContext();           
            ictx.rebind("XADataSource", (XADataSource) xads);
            System.out.println(
                "bound XADataSource with JNDI name 'XADataSource'");
            //System.out.println("DataSource ref="+ ds.getReference());
            ictx.rebind("DataSource", (DataSource) ds);
            System.out.println("bound DataSource with JNDI name 'DataSource'");
            ictx.rebind("UserTransaction", jotm.getUserTransaction());
            System.out.println(
                "bound UserTransaction with JNDI name 'UserTransaction'");
            ictx.rebind("javax.transaction.TransactionManager", tm);
            System.out.println(
                "bound TransactionManager JNDI name 'javax.transaction.TransactionManager'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
