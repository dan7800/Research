
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

/**
 * this test is used to show the following process:<br>
 * <pre>
 *    c = ds.getConnection();
 *    utx.begin();
 *    ...
 *    utx.commit();
 *    utx.begin();
 *    ...
 *    utx.rollback();
 *    utx.close();
 * </pre>
 */
public class MultipleTransaction {
    private String SQL_REQUEST = "select id, foo from testdata";
    private String SQL_QUERY = "update testdata set foo = ? where id=1";
    private String USAGE = "usage: java MultipleTransaction [number]";

    private Connection conn;
    private TMService jotm;

    private String login = null;
    private String password = null;
    private String url = null;
    private String driver = null;
    private String USER_TRANSACTION_JNDI_NAME = "UserTransaction";

    public MultipleTransaction(String [] args) throws Exception {
        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(1);
        }

        // first, load the properties from the spy.properties file
        Properties prop = new Properties();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("spy.properties"));
        } catch (Exception e) {
            System.err.println("problem to load properties.");
            e.printStackTrace();
            System.exit(1);
        }

        login = prop.getProperty("login");
        password = prop.getProperty("password");
        url = prop.getProperty("url");
        driver = prop.getProperty("driver");

        // get the new value which will be assign to the database
        int newValue = 0;
        try {
            newValue = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
            System.err.println("[number] has to be an integer\n");
            System.exit(1);
        }


        // Get a transaction manager
        try {
            // creates an instance of JOTM with a local transaction factory which is not bound to a registry
            jotm = new Jotm(true, false);
            InitialContext ictx = new InitialContext();
            ictx.rebind(USER_TRANSACTION_JNDI_NAME, jotm.getUserTransaction());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // get an user transaction
        UserTransaction utx = null;

        try {
            System.out.println("create initial context");
            Context ictx = new InitialContext();
            System.out.println("lookup UserTransaction at : " + USER_TRANSACTION_JNDI_NAME);
            utx = (UserTransaction) ictx.lookup(USER_TRANSACTION_JNDI_NAME);
        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // create an XA pool datasource with a minimum of 4 objects
        StandardXAPoolDataSource spds = new StandardXAPoolDataSource(4);
	    spds.setUser(login);
        spds.setPassword(password);

        // create an XA datasource which will be given to the XA pool
        StandardXADataSource xads = new StandardXADataSource();
        try {
            xads.setDriverName(driver);
            xads.setUrl(url);
            xads.setUser(login);
            xads.setPassword(password);
            xads.setTransactionManager(jotm.getTransactionManager());
        } catch (Exception e) {
            System.err.println("JOTM problem.");
            e.printStackTrace();
            System.exit(1);
        }
        
        // give the XA datasource to the pool (to create futur objects)
		spds.setTransactionManager(jotm.getTransactionManager());
		spds.setDataSource(xads);
        

        conn = spds.getConnection(login, password);
        try {
            utx.begin();
            PreparedStatement pstmt0 = conn.prepareStatement(SQL_QUERY);
            pstmt0.setInt(1, 13);
            pstmt0.executeUpdate();
            System.out.println("dump, with 13 before commit:");
            printTable();
            System.out.println("** commit **");
            utx.commit();

            System.out.println("dump, with 13 after commit:");
            printTable();

            utx.begin();
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
            System.out.println("dump, with the new value("+newValue+") before rollback:");
            printTable();
            System.out.println("** rollback **");
	        utx.rollback();

            System.out.println("dump, with the new value("+newValue+") after rollback:");
            printTable();
        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Print table after work:");
        printTable();
        conn.close();
        stop();
    }

    public void printTable() {
        try {
            PreparedStatement stmt = conn.prepareStatement(SQL_REQUEST);
            ResultSet rset = stmt.executeQuery(SQL_REQUEST);
            int numcols = rset.getMetaData().getColumnCount();
            for (int i = 1; i <= numcols; i++) {
                System.out.print("\t" + rset.getMetaData().getColumnName(i));
            }
            System.out.println();
            while (rset.next()) {
                for (int i = 1; i <= numcols; i++) {
                    System.out.print("\t" + rset.getString(i));
                }
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
           InitialContext ictx = new InitialContext();
           ictx.unbind(USER_TRANSACTION_JNDI_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jotm.stop();
        jotm = null;
    }

    static public void main(String [] argv) throws Exception{
        MultipleTransaction spdse = new MultipleTransaction(argv);
        System.exit(1);
    }
}
