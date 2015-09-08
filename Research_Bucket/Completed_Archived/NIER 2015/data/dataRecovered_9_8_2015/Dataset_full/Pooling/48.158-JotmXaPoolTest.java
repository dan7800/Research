// JOTM/XAPool Test Class
// This is to test transactions and connection
// Written By: Andy Zeneski (jaz@ofbiz.org)

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.sql.XAConnection;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;

public class JotmXaPoolTest {

    protected static Logger logger = Logger.getLogger("jotm.test");         
    
    protected String createColumns = null;
    protected String tableName = null;
    protected int counter = 0;
    protected int loops = 500;
    protected TMService jotm = null;
    protected StandardXAPoolDataSource pool = null;
    
    public JotmXaPoolTest() throws Exception {        
        // start JOTM
        jotm = new Jotm(true, false);
        logger.info("Started JOTM...");
        
        // need a classloader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        // get the datasource config file name
        String dsConfigName = System.getProperty("jdbc.config.file");
        
        // if not in environment load from datasource.properties
        if (dsConfigName == null || dsConfigName.length() == 0) {        
            // load the datasource properties
            Properties dsProps = new Properties();       
            dsProps.load(cl.getResourceAsStream("config.properties"));
            dsConfigName = dsProps.getProperty("jdbc.config.file");
            tableName = dsProps.getProperty("table.name", "jotm_xapool_test");
            String loopAmt = dsProps.getProperty("loop.amount", "500");            
            try {
                loops = Integer.parseInt(loopAmt);
            } catch (NumberFormatException e) {
                loops = 500;
            }
        }
        
        // no config we cannot run
        if (dsConfigName == null || dsConfigName.length() == 0) {
            throw new IllegalStateException("No datasource properties configured; set the jdbc.config.file variable in the environment or in datasource.properties");
        }
                        
        // if we have the name; load the props
        Properties props = new Properties();
        props.load(cl.getResourceAsStream(dsConfigName));
                        
        // set the sql for the create statement
        createColumns = props.getProperty("sql.create.columns");
        
        // get an instance of the datasource wrapper
        String defaultWrapper = "org.enhydra.jdbc.standard.StandardXADataSource";
        String wrapperName = props.getProperty("jdbc.wrapper", defaultWrapper);
        
        Class c = cl.loadClass(wrapperName);
        Object o = c.newInstance();
                
        // create the datasource
        StandardXADataSource ds = (StandardXADataSource) o;
        ds.setTransactionManager(jotm.getTransactionManager());
                
        ds.setDriverName(props.getProperty("jdbc.driver"));
        ds.setUrl(props.getProperty("jdbc.url"));
        ds.setUser(props.getProperty("jdbc.login"));
        ds.setPassword(props.getProperty("jdbc.password", ""));
        ds.setDescription(props.getProperty("jdbc.description", "NA"));
        
        // get the isolation level (default is READ_COMITTED)
        int isoLevel = 2;
        try {
            isoLevel = Integer.parseInt(props.getProperty("jdbc.isolation.level", "2"));
        } catch (NumberFormatException e) {
            logger.error("Problems parsing the isolation level (should be a number) using READ_COMMITED");
            isoLevel = 2;
        }        
        ds.setTransactionIsolation(isoLevel);
                                
        // create the pool
        pool = new StandardXAPoolDataSource();
        pool.setDataSource(ds);
        pool.setDescription(ds.getDescription());
        pool.setUser(ds.getUser());
        pool.setPassword(ds.getPassword());
        pool.setTransactionManager(jotm.getTransactionManager());
              
        logger.info("Created pool...");        
    }    
    
    public void dropTest() throws SQLException {        
        Connection con = pool.getConnection();
        Statement s = con.createStatement();
        s.executeUpdate("DROP TABLE " + tableName);
        s.close();
        con.close();
        logger.info("Table '" + tableName + "' dropped.");        
    }
    
    public void createTest() throws SQLException {                
        // get a connection
        Connection con = pool.getConnection();
        logger.info("Isolation level : " + con.getTransactionIsolation());
                       
        String sql = "CREATE TABLE " + tableName + " " + this.createColumns;                
        Statement s = con.createStatement();
        s.executeUpdate(sql);
        s.close();                               
        con.close();
        logger.info("Table '" + tableName + "' created.");                    
    }
    
    /** Tests a loop of inserts using different connections and transaction */
    public void insertTest() {
        // start the transaction
        UserTransaction trans = jotm.getUserTransaction();                   
        logger.info("Beginning multiple insert with unique transactions/connections...");
                                
        for (int i = 0; i < loops; i++) { 
            try {
                trans.begin();            
            } catch (NotSupportedException e1) {            
                logger.error("Exception", e1);
            } catch (SystemException e1) {            
                logger.error("Exception", e1);
            }
            
            Connection con = null;
            try {
                con = pool.getConnection();
            } catch (SQLException e) {            
                logger.error("Problems getting new connection - Test Failed!", e);
                return;
            }    
            if (con == null) {
                logger.error("Pool returned null connection with no exception - Test Failed!");
                return;    
            }            
                                                       
            logger.debug("[A] Looping.. inserting #" + i);
            try {
                // insert item            
                String sql1 = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps1 = con.prepareStatement(sql1);
                ps1.setInt(1, ++counter);
                ps1.setString(2, "insTest" + i);
                ps1.setString(3, "Insert Test");
                ps1.setTimestamp(4, new Timestamp(new Date().getTime()));
                ps1.executeUpdate();
                ps1.close();   
                // select it back
                String sql2 = "SELECT * FROM " + tableName + " WHERE idx_1 = ?";
                PreparedStatement ps2 = con.prepareStatement(sql2);
                ps2.setInt(1, counter);
                ResultSet res = ps2.executeQuery();
                if (res == null || !res.next()) {
                    logger.error("Could not get inserted item back from select!");
                } else {
                    logger.debug(res.getString(1) + " : " + res.getString(2) + "[" + res.getString(3) + "] - " + res.getString(4));
                }
                res.close();
                ps2.close();                 
            } catch (Exception e) {
                logger.error("Exception", e);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e2) {                    
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }                
            } finally {
                // close the connection
                try {
                    con.close();
                } catch (SQLException e2) {            
                    logger.error("Exception", e2);
                }                
        
                // commit the transaction
                try {        
                    trans.commit();            
                } catch (Exception e) {
                    logger.error("Exception", e);
                }                           
            }
        }                        
    }
    
    /** Tests a loop of inserts using different connections, same transaction */
    public void connectionTest() {
        // start the transaction
        UserTransaction trans = jotm.getUserTransaction();
        try {
            trans.begin();
        } catch (NotSupportedException e1) {
            logger.error("Exception", e1);                       
        } catch (SystemException e1) {
            logger.error("Exception", e1);                        
        }
        
        logger.info("Beginning multiple insert/connection single transaction...");
        for (int i = 0; i < loops; i++) {            
            Connection con = null;
            try {
                con = pool.getConnection();
            } catch (SQLException e) {            
                logger.error("Problems getting new connection - Test Failed!", e);
                return;
            }    
            if (con == null) {
                logger.error("Pool returned null connection with no exception - Test Failed!");
                return;    
            }
            
            logger.debug("Got connection.. inserting #" + i);
            try {            
                String sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, ++counter);
                ps.setString(2, "conTest" + i);
                ps.setString(3, "Connection Test");
                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                ps.executeUpdate();
                ps.close();                
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e2) {                    
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }
            } finally {                
                try {
                    con.close();
                } catch (SQLException e2) {                    
                    logger.error("Exception", e2);
                }
            }
        }
        try {        
            trans.commit();
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }
    
    /** Tests a loop of inserts using same connection & transaction */
    public void loopTest() {
        // start the transaction
        UserTransaction trans = jotm.getUserTransaction();
        try {
            trans.begin();            
        } catch (NotSupportedException e1) {            
            logger.error("Exception", e1);
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
        }
                                
        logger.info("Beginning multiple insert single transaction/connection...");
        
        Connection con = null;
        try {
            con = pool.getConnection();
        } catch (SQLException e) {            
            logger.error("Problems getting new connection - Test Failed!", e);
            return;
        }    
        if (con == null) {
            logger.error("Pool returned null connection with no exception - Test Failed!");
            return;    
        }
        
        logger.debug("Got connection.. ");
        
        for (int i = 0; i < loops; i++) {                       
            logger.debug("[B] Looping.. inserting #" + i);
            try {            
                String sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, ++counter);
                ps.setString(2, "loopTest" + i);
                ps.setString(3, "Loop Test");
                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                ps.executeUpdate();
                ps.close();                
            } catch (Exception e) {
                logger.error("Exception", e);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e2) {                    
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }
            }            
        }
        
        // close the connection
        try {
            con.close();
        } catch (SQLException e2) {            
            logger.error("Exception", e2);
        }
        logger.debug("Closed connection..");
        
        // commit the transaction
        try {        
            trans.commit();            
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }
    
    /** Tests multiple inserts; suspending before each one, insert in new trans, resuming to continue */
    public void suspendTest() {
        // start the transaction
        UserTransaction trans = jotm.getUserTransaction();
        TransactionManager tm = jotm.getTransactionManager();
        
        // set the timeout to something reasonable
        try {    
            trans.setTransactionTimeout(300);
        } catch (SystemException e) {
            logger.error("Exception", e);
            return;
        }
                
        // begin the parent transaction
        try {
            trans.begin();
        } catch (NotSupportedException e1) {           
            logger.error("Exception", e1);
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
        }
        
        logger.info("Beginning multiple insert/connection on suspend main transaction...");
        for (int i = 0; i < loops; i++) {            
            // suspend the main transaction                    
            Transaction transaction = null;            
            try {
                transaction = tm.suspend();
            } catch (SystemException e2) {                
                logger.error("Exception", e2);
            }
            logger.debug("Suspended #" + i);
            
            // begin a new transaction
            try {
                trans.begin();
            } catch (NotSupportedException e3) {               
                logger.error("Exception", e3);
            } catch (SystemException e3) {                
                logger.error("Exception", e3);
            }
            logger.debug("Began new transaction.");
            
            // do some stuff in the new transaction
            Connection con1 = null;
            try {
                con1 = pool.getConnection();
            } catch (SQLException e) {               
                logger.error("Problems getting new (sub) connection - Test Failed!", e);
                return;
            }
            if (con1 == null) {
                logger.error("Pool returned null connection with no exception - Test Failed!");
                return;
            }
            logger.debug("Got connection.");
            
            try {
                // insert item            
                String sql1 = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps1 = con1.prepareStatement(sql1);
                ps1.setInt(1, ++counter);
                ps1.setString(2, "susTest" + i);
                ps1.setString(3, "Suspend Test - Main Suspended");
                ps1.setTimestamp(4, new Timestamp(new Date().getTime()));
                ps1.executeUpdate();
                ps1.close();                              
            } catch (Exception e) {
                logger.error("Exception", e);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e4) {                    
                    logger.error("Exception", e4);
                } catch (SystemException e4) {                    
                    logger.error("Exception", e4);
                }
            } finally {                        
                try {
                    con1.close();
                } catch (SQLException e5) {                   
                    logger.error("Exception", e5);
                }  
                
                // commit the new transaction              
                try {
                    trans.commit();
                } catch (SecurityException e4) {                   
                    logger.error("Exception", e4);
                } catch (IllegalStateException e4) {                    
                    logger.error("Exception", e4);
                } catch (RollbackException e4) {                    
                    logger.error("Exception", e4);
                } catch (HeuristicMixedException e4) {                    
                    logger.error("Exception", e4);
                } catch (HeuristicRollbackException e4) {                    
                    logger.error("Exception", e4);
                } catch (SystemException e4) {                    
                    logger.error("Exception", e4);
                }                                
            }
            logger.debug("Inserted record.");
            
            // resume the main transaction
            try {
                tm.resume(transaction);
            } catch (InvalidTransactionException e4) {                
                logger.error("Exception", e4);
            } catch (IllegalStateException e4) {                
                logger.error("Exception", e4);
            } catch (SystemException e4) {                
                logger.error("Exception", e4);
            }
            logger.debug("Resumed #" + i);
            
            // do some stuff in the main transaction
            Connection con2 = null;
            try {
                con2 = pool.getConnection();
            } catch (SQLException e) {
                logger.error("Problems getting new (main) connection - Test Failed!", e);
                return;              
            }          
            if (con2 == null) {
                logger.error("Pool returned null connection with no exception - Test Failed!");
                return;  
            }
            
            try {            
                String sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps = con2.prepareStatement(sql, 0, 0);
                ps.setInt(1, ++counter);
                ps.setString(2, "susTest" + i);
                ps.setString(3, "Suspend Test - Main");
                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                ps.executeUpdate();
                ps.close();                
                logger.debug("Inserted main transaction.");                                              
            } catch (Exception e) {
                logger.error("Exception", e);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e5) {                   
                    logger.error("Exception", e5);
                } catch (SystemException e5) {                    
                    logger.error("Exception", e5);
                }
            } finally {
                try {
                    con2.close();
                } catch (SQLException e5) {                    
                    logger.error("Exception", e5);
                }
            }
            
            // get another connection
            Connection con3 = null;
            try {
                con3 = pool.getConnection();                
            } catch (SQLException e) {
                logger.error("Problems getting new (main select) connection - Test Failed!", e);
                return;              
            }          
            if (con3 == null) {
                logger.error("Pool returned null connection with no exception - Test Failed!");
                return;  
            }
                      
            // now select it back
            try {
                String sql = "SELECT * FROM " + tableName + " WHERE idx_1 = ?";
                PreparedStatement ps = con3.prepareStatement(sql);
                ps.setInt(1, counter);
                ResultSet res = ps.executeQuery();
                if (res == null || !res.next()) {
                    logger.error("Could not get inserted item back from select!");
                } else {
                    logger.debug(res.getString(1) + " : " + res.getString(2) + "[" + res.getString(3) + "] - " + res.getString(4));
                }       
                res.close();
                ps.close();                     
            } catch (Exception e) {
                logger.error("Exception", e);                
            } finally {
                try {
                    con3.close();
                } catch (SQLException e2) {                    
                    logger.error("Exception", e2);
                }
            }
        }
        
        // commit the main transaction - outside the loop
        try {
            trans.commit();
        } catch (SecurityException e) {            
            logger.error("Exception", e);
        } catch (IllegalStateException e) {            
            logger.error("Exception", e);
        } catch (RollbackException e) {            
            logger.error("Exception", e);
        } catch (HeuristicMixedException e) {            
            logger.error("Exception", e);
        } catch (HeuristicRollbackException e) {            
            logger.error("Exception", e);
        } catch (SystemException e) {            
            logger.error("Exception", e);
        }
    }
    
    public void transactionTest() {
        UserTransaction trans = jotm.getUserTransaction();
        Connection con = null;
        String sql = null;
        PreparedStatement ps = null;
        
        logger.info("Beginning transaction test...");
        
        // insert a row - close connection - no transaction
        try {
            con = pool.getConnection();
            sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setInt(1, ++counter);
            ps.setString(2, "transTest");
            ps.setString(3, "Transaction Test - This Should Be Updated!");
            ps.setTimestamp(4, new Timestamp(new Date().getTime()));                
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            logger.error("Transaction test failed on first insert", e);            
            return;
        }
                
        // select the row - close connection - no transaction
        try {
            con = pool.getConnection();
            sql = "SELECT * FROM " + tableName + " WHERE idx_1 = ?";
            ps = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, counter);
            ResultSet res = ps.executeQuery();
            if (!res.next()) {
                logger.error("Transaction test failed; no results returned from first insert");               
                return;
            } else {
                logger.debug(res.getString(1) + " : " + res.getString(2) + "[" + res.getString(3) + "] - " + res.getString(4));
            }
            res.close();
            ps.close();
            con.close();            
        } catch (SQLException e) {
            logger.error("Transaction test failed; cannot select first insert", e);
            return;
        }
        
        // start the transaction
        try {
            trans.begin();
        } catch (NotSupportedException e1) {            
            logger.error("Exception", e1);
            return;
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
            return;
        }
        
        // update the row - close connection
        try {
            con = pool.getConnection();
            sql = "UPDATE " + tableName + " SET col_3 = ? WHERE idx_1 = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "Transaction Test - First Update; One To Go!");
            ps.setInt(2, counter);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            logger.error("Transaction test failed; cannot do first update", e);
            return;
        }
        
        // select the row - close connection
        try {
            con = pool.getConnection();
            sql = "SELECT * FROM " + tableName + " WHERE idx_1 = ?";
            ps = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, counter);
            ResultSet res = ps.executeQuery();
            if (!res.next()) {
                logger.error("Transaction test failed; no results returned after first update");               
                return;
            } else {
                logger.debug(res.getString(1) + " : " + res.getString(2) + "[" + res.getString(3) + "] - " + res.getString(4));
            }
            res.close();
            ps.close();
            con.close();            
        } catch (SQLException e) {
            logger.error("Transaction test failed; cannot select first update", e);
            return;
        }
        
        // commit transaction        
        try {        
            trans.commit();        
        } catch (SystemException e) {
            logger.error("Exception", e);
            return;
        } catch (SecurityException e) {
            logger.error("Exception", e);
            return;
        } catch (IllegalStateException e) {            
            logger.error("Exception", e);
            return;                        
        } catch (RollbackException re) {                                   
            logger.error("Transaction rolledback!", re);
            return;                                    
        } catch (HeuristicMixedException e) {            
            logger.error("Exception", e);
            return;
        } catch (HeuristicRollbackException e) {          
            logger.error("Exception", e);
            return;
        }      
        
        // select row - close connection - no transaction
        try {
            con = pool.getConnection();
            sql = "SELECT * FROM " + tableName + " WHERE idx_1 = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, counter);
            ResultSet res = ps.executeQuery();
            if (!res.next()) {
                logger.error("Transaction test failed; no results returned from select after commit");               
                return;
            }
            res.close();
            ps.close();
            con.close();            
        } catch (SQLException e) {
            logger.error("Transaction test failed; cannot select after commit", e);
            return;
        }        
        
        // start transaction
        try {
            trans.begin();
        } catch (NotSupportedException e1) {            
            logger.error("Exception", e1);
            return;
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
            return;
        }        
        
        // update row - close connection
        try {
            con = pool.getConnection();
            sql = "UPDATE " + tableName + " SET col_3 = ? WHERE idx_1 = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "Transaction Test - Second Update!");
            ps.setInt(2, counter);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            logger.error("Transaction test failed; cannot do second update", e);
            return;
        }        
        
        // commit transaction
        try {        
            trans.commit();        
        } catch (SystemException e) {
            logger.error("Exception", e);
            return;
        } catch (SecurityException e) {
            logger.error("Exception", e);
            return;
        } catch (IllegalStateException e) {            
            logger.error("Exception", e);
            return;                        
        } catch (RollbackException re) {                                   
            logger.error("Transaction rolledback!", re);
            return;                                    
        } catch (HeuristicMixedException e) {            
            logger.error("Exception", e);
            return;
        } catch (HeuristicRollbackException e) {          
            logger.error("Exception", e);
            return;
        }                      
    }
        
    public void rollbackOnlyTest() { 
        // start the transaction
        UserTransaction trans = jotm.getUserTransaction();
        try {
            trans.begin();
        } catch (NotSupportedException e1) {            
            logger.error("Exception", e1);
            return;
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
            return;
        }
        
        logger.info("Beginning rollback test...");
        Random rand = new Random();
        int randomInt = rand.nextInt(9);
        
        for (int i = 0; i < 10; i++) {            
            Connection con = null;
            try {
                con = pool.getConnection();                
            } catch (SQLException e) {                
                logger.error("Problems getting connection - rolling back now.", e);
                try {
                    trans.rollback();
                } catch (IllegalStateException e2) {                  
                    logger.error("Exception", e2);
                } catch (SecurityException e2) {                    
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }
            }
            if (con == null) {
                logger.error("Pool returned a null connection w/ no exception! Test Failed!");
                return;
            }
            
            logger.debug("Got connection.. inserting #" + i);
            try {            
                String sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, ++counter);
                ps.setString(2, "rollTest" + i);
                ps.setString(3, "Rollback Test - This should not show in selectTest!");
                ps.setTimestamp(4, new Timestamp(new Date().getTime()));                
                ps.executeUpdate();
                ps.close();                                  
            } catch (Exception e) {
                logger.error("Exception", e);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e2) {                   
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }
            } finally {                
                try {
                    con.close();
                } catch (SQLException e2) {                    
                    logger.error("Exception", e2);
                }                
            }
            
            // will set rollback only on some random pass
            if (randomInt == i) {
                logger.info("Setting rollback only on pass #" + i);
                try {
                    trans.setRollbackOnly();
                } catch (IllegalStateException e2) {                   
                    logger.error("Exception", e2);
                } catch (SystemException e2) {                    
                    logger.error("Exception", e2);
                }
            }
            
        }
        
        try {        
            trans.commit();        
        } catch (SystemException e) {
            logger.error("Commit failed; RollbackException not thrown!", e);
        } catch (SecurityException e) {
            logger.error("Commit failed; RollbackException not thrown!", e);
        } catch (IllegalStateException e) {            
            logger.error("Commit failed; RollbackException not thrown!", e);                        
        } catch (RollbackException re) {                        
            // This SHOULD happen!
            logger.info("Commit failed (good), transaction rolled back by commit().");                                    
        } catch (HeuristicMixedException e) {            
            logger.error("Commit failed; RollbackException not thrown!", e);
        } catch (HeuristicRollbackException e) {          
            logger.error("Commit failed; RollbackException not thrown!", e);
        }
    }  
    
    public void timeoutTest() {
        // get the transaction
        UserTransaction trans = jotm.getUserTransaction();
                                   
        logger.info("Beginning timeout test...");
        Random rand = new Random();
        int randomInt = rand.nextInt(60);
        randomInt++;
        
        logger.info("Setting timeout to: " + randomInt);
        try {            
            trans.setTransactionTimeout(randomInt); // set to new value
        } catch (SystemException e) {            
            logger.error("Exception", e);
        }
        
        // begin the transaction
        try {
            trans.begin();
        } catch (NotSupportedException e1) {            
            logger.error("Exception", e1);
        } catch (SystemException e1) {            
            logger.error("Exception", e1);
        }
        logger.info("Began transaction; now waiting...");           
        
        // now wait a few seconds
        long wait = new Date().getTime() + ((randomInt + 2) * 1000);
        long now = 0;
        while ((now = new Date().getTime()) < wait) {
            //logger.info(now + " != " + wait);                                        
        }        
        
        // attempt to commit the transaction; should fail
        try {
            trans.commit();
            logger.info("Transaction commited; shouldn't have happened!");
        } catch (SecurityException e) {           
            logger.error("Exception", e);
        } catch (IllegalStateException e) {            
            logger.error("Exception", e);            
        } catch (RollbackException e) {            
            logger.info("RollBackException caught! Good! The transaction was rolled back.");            
        } catch (HeuristicMixedException e) {            
            logger.error("Exception", e);
        } catch (HeuristicRollbackException e) {            
            logger.error("Exception", e);            
        } catch (SystemException e) {            
            logger.error("Exception", e);
        }        
    }              
    
    public void selectTest() throws SQLException {
        logger.info("Beginning select test.. We should have exactly " + (loops * 5) + " records.");
        Connection con = pool.getConnection();
        Statement s = con.createStatement();        
        ResultSet res = s.executeQuery("SELECT * FROM " + tableName + " ORDER BY idx_1");
        int rowCount = 0;
        while (res.next()) {
            rowCount++;
            logger.debug(res.getString(1) + " : " + res.getString(2) + "[" + res.getString(3) + "] - " + res.getString(4));
        }
        res.close();
        con.close();
        logger.info("Total Rows: " + rowCount + " of " + (loops * 5));
        if (rowCount == (loops * 5)) logger.info("Looks good...");        
    }
    
    public void close() {
        pool.shutdown(true);
        jotm.stop();
        logger.info("Shutdown pool and jotm.");                        
    }
    
    public void runPreTests() {
        // try to drop the table
        try {       
            dropTest();
        } catch (SQLException e) {
            // ignore this; the table may not exist          
        }
        
        // try to create the table
        try {        
            createTest();
        } catch (SQLException e) {
            logger.error("Fatal SQL Error", e);
            this.close();
            System.exit(-1);
        }
    }
    
    public void runPostTests() {
        // show the results         
        try {        
            selectTest();
        } catch (SQLException e) {
            logger.error("SQL Error", e);
        }
        
        // drop the table
        try {       
            dropTest();
        } catch (SQLException e) {
            logger.error("Fatal SQL Error", e);
            this.close(); 
            System.exit(-1);          
        }         
    }        
                    
    public void runAllTests() {       
        runPreTests();
        
        // test some basic inserts (unique transactions/connections)                              
        insertTest();        
        
        // test connections (same connection)
        connectionTest();
        
        // test looping (same connection/transaction)      
        loopTest(); 
        
        // test transactions
        transactionTest();       
        
        // test some suspend/resume inserts            
        suspendTest();
                
        // rollback only test       
        rollbackOnlyTest();
        
        // test transaction timeouts
        timeoutTest();          
        
        runPostTests();
                
    }
            
    public static void main(String[] args) throws Exception {           
        JotmXaPoolTest test = new JotmXaPoolTest();
        test.runAllTests(); 
        test.close();        
        System.exit(1);                        
    }
}