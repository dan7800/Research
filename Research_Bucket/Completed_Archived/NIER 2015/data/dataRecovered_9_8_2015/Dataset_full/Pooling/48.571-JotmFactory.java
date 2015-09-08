/*
 * $Id: JotmFactory.java 7669 2006-05-25 05:23:21Z jonesde $
 *
 * Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.jotm;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;
import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.config.DatasourceInfo;
import org.ofbiz.entity.config.EntityConfigUtil;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.transaction.TransactionFactoryInterface;
import org.ofbiz.entity.transaction.MinervaConnectionFactory;

/**
 * JotmFactory - Central source for JOTM JTA objects
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev: 7669 $
 * @since      2.1
 */
public class JotmFactory implements TransactionFactoryInterface {

    public static final String module = JotmFactory.class.getName();        
    
    private static TMService jotm;
    static {    
        try {
            // creates an instance of JOTM with a local transaction factory which is not bound to a registry            
            jotm = new Jotm(true, false);         
        } catch (NamingException ne) {
            Debug.logError(ne, "Problems creating JOTM instance", module);
        }
    }

    /*
     * @see org.ofbiz.entity.transaction.TransactionFactoryInterface#getTransactionManager()
     */
    public TransactionManager getTransactionManager() {  
        if (jotm != null) {                     
         return jotm.getTransactionManager();
        } else {
            Debug.logError("Cannot get TransactionManager, JOTM object is null", module);
            return null;
        }
    }

    /*
     * @see org.ofbiz.entity.transaction.TransactionFactoryInterface#getUserTransaction()
     */
    public UserTransaction getUserTransaction() {  
        if (jotm != null) {           
            return jotm.getUserTransaction();
        } else {
            Debug.logError("Cannot get UserTransaction, JOTM object is null", module);
            return null;
        }
    }                
    
    public String getTxMgrName() {
        return "jotm";
    }
    
    public Connection getConnection(String helperName) throws SQLException, GenericEntityException {
        DatasourceInfo datasourceInfo = EntityConfigUtil.getDatasourceInfo(helperName);

        if (datasourceInfo != null && datasourceInfo.inlineJdbcElement != null) {
            // Use JOTM (xapool.jar) connection pooling
            try {
                Connection con = MinervaConnectionFactory.getConnection(helperName, datasourceInfo.inlineJdbcElement);
                if (con != null) return con;
            } catch (Exception ex) {
                Debug.logError(ex, "JOTM is the configured transaction manager but there was an error getting a database Connection through JOTM for the " + helperName + " datasource. Please check your configuration, class path, etc.", module);
            }
        
            Connection otherCon = ConnectionFactory.tryGenericConnectionSources(helperName, datasourceInfo.inlineJdbcElement);
            return otherCon;
        } else {            
            Debug.logError("JOTM is the configured transaction manager but no inline-jdbc element was specified in the " + helperName + " datasource. Please check your configuration", module);
            return null;
        }
    }
    
    public void shutdown() {
        MinervaConnectionFactory.closeAll();
        if (jotm != null) {
            jotm.stop();
            jotm = null;
        }           
    }
}
