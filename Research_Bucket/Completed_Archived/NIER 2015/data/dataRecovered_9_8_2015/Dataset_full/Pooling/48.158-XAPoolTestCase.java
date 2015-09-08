import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Properties;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.transaction.UserTransaction;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Statement;

public class XAPoolTestCase extends TestCase {

    public String login = null;
    public String password = null;
    public String url = null;
    public String driver = null;
    public TMService jotm;
    public String USER_TRANSACTION_JNDI_NAME = "UserTransaction";
    public UserTransaction utx = null;
    public StandardXAPoolDataSource spds;
    public StandardXADataSource xads;
    public Connection conn;
    public String SQL_REQUEST = "select id, foo from testdata where id=1";
    public String SQL_QUERY = "update testdata set foo = ? where id=1";
    public String datasourceClassName;
    
    public XAPoolTestCase(String strTestName) {
	super(strTestName);
	//System.out.println("New Object");
    }
    
    public static Test suite() {
	return new TestSuite(XAPoolTestSuite.class);
    }

    protected void setUp() throws Exception {
	//System.out.println("setUp");
        // first, load the properties from the spy.properties file
        Properties prop = new Properties();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("spy.properties"));
        } catch (Exception e) {
            System.err.println("problem to load properties.");
            e.printStackTrace();
            throw e;
        }
	
        login = prop.getProperty("login");
        password = prop.getProperty("password");
        url = prop.getProperty("url");
        driver = prop.getProperty("driver");

        // TML -Allow overriding the datasource class name in order to
        // support Sybase, IDB, etc.  Defaults to the Standard implementation
        datasourceClassName = prop.getProperty("datasource-class",
              StandardXADataSource.class.getName());
        
        // Get a transaction manager
        try {
            // creates an instance of JOTM with a local transaction factory which is not bound to a registry
            jotm = new Jotm(true, false);
            //InitialContext ictx = new InitialContext();
            //ictx.rebind(USER_TRANSACTION_JNDI_NAME, jotm.getUserTransaction());
        } catch (Exception e) {
            System.err.println("JOTM problem.");
            e.printStackTrace();
            throw e;
        }


        try {
            //Context ictx = new InitialContext();
            //utx = (UserTransaction) ictx.lookup(USER_TRANSACTION_JNDI_NAME);
            utx = jotm.getUserTransaction();
        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // create an XA pool datasource with a minimum of 4 objects
        spds = new StandardXAPoolDataSource(2);
        spds.setMaxSize(15);
        spds.setMinSize(13);
        spds.setUser(login);
        spds.setPassword(password);

        // create an XA datasource which will be given to the XA pool
        Class datasourceClass=Class.forName(datasourceClassName, true, getClass().getClassLoader());
        xads = (StandardXADataSource)datasourceClass.newInstance();
        try {
            xads.setDriverName(driver);
            xads.setUrl(url);
            xads.setUser(login);
            xads.setPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        spds.setTransactionManager(jotm.getTransactionManager());

        // give the XA datasource to the pool (to create futur objects)
        spds.setDataSource(xads);
    }

    public int getValue() {
        try {
	    // some tests close the connection, here, we need to get it 
	    // and do not forget to close it after use !!!
	    boolean toClose = false;
	    if (conn.isClosed()) {
		toClose = true;
		conn = spds.getConnection(login, password);

	    }

	    Statement st = conn.createStatement();
            ResultSet rset = st.executeQuery(SQL_REQUEST);
            int numcols = rset.getMetaData().getColumnCount();
	    int res;
            rset.next();
	    res = Integer.parseInt(rset.getString(2));
	    rset.close();
            st.close();

	    // here, we need to close the connection because it was
	    // closed at the beginning of the method
	    if (toClose)
		conn.close();
	    return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
	return 0;
    }
    
    protected void tearDown() throws Exception {
	//System.out.println("tearDown");
        try {
	    //InitialContext ictx = new InitialContext();
	    //ictx.unbind(USER_TRANSACTION_JNDI_NAME);
        } catch (Exception e) {
           throw e;
        }
        jotm.stop();
        jotm = null;
	spds.stopPool();
	xads = null;
	spds = null;
	
    }

}
