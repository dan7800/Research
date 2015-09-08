import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;

/**
 * @author jule
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Connect implements Runnable {

    String client;
    int nb;
    UserTransaction ut;
    String productlist;
    private static TMService jotm;
    private static TransactionManager tm;
    private Transaction t;
    private static StandardXAPoolDataSource spds1;
    private static StandardXAPoolDataSource spds2;

    /**
     * Constructor for ConnectOracle.
     */
    public Connect(String s, String mydo, int index) {
	super();

	client = mydo;
	nb = index;
	productlist = s;

    }

    private static synchronized void init() {
	String login = null;
	String password = null;
	String url = null;
	String driver = null;

	//init   jotm
	try {
	    if (jotm == null)
		jotm = new Jotm(true, false);
	    if (tm == null)
		tm = jotm.getTransactionManager();
	    if (tm == null) {
		System.out.println("moniteur transactionel non initialisé");
		return;
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




	    //init xapool
	    if (spds1 == null) {

		spds1 = new StandardXAPoolDataSource(4);
		spds1.setMaxSize(15);
		spds1.setMinSize(13);
		spds1.setUser(login);
		spds1.setPassword(password);
		StandardXADataSource xds1 = new StandardXADataSource();

		xds1.setDriverName(driver);
		xds1.setUrl(url);
		xds1.setUser(login);
		xds1.setPassword(password);
		spds1.setTransactionManager(tm);
		spds1.setDataSource(xds1);
	    }
	    if (spds2 == null) {
		spds2 = new StandardXAPoolDataSource(4);
		spds2.setMaxSize(15);
		spds2.setMinSize(13);
		spds2.setUser(login);
		spds2.setPassword(password);
		StandardXADataSource xds2 = new StandardXADataSource();

		xds2.setDriverName(driver);
		xds2.setUrl(url);
		xds2.setUser(login);
		xds2.setPassword(password);
		spds2.setTransactionManager(tm);
		spds2.setDataSource(xds2);
	    }

	} catch (Exception e) {
	    System.out.println("Problem jotm/xapool");
	    System.exit(0);
	}
    }

    public void run() {
	int res = 0;
	//NDC.push(Thread.currentThread().toString());
	ut = jotm.getUserTransaction();
	DataSource hds1 = null;
	PreparedStatement ps1 = null;
	Connection conn1 = null;
	PreparedStatement ps2b = null;
	Connection conn2b = null;
	DataSource hds2 = null;
	PreparedStatement ps2 = null;
	Connection conn2 = null;
	for (int tr = 0; tr < productlist.length(); tr++) {
	    try {

		if (ut == null)
		    throw new Exception("ut is null");
		ut.begin();

		//
		int size = 9;
		char[] cs = new char[size];
		char c = productlist.charAt(tr);

		for (int l = 0; l < size; l++)
		    cs[l] = c;
		String obj = String.valueOf(cs);
		String objs = Integer.toString(++nb);

		Timestamp dtcr = null;

		try {
		    System.out.println("insert BASE1 ");
		    hds1 = (DataSource) spds1;
		    conn1 = hds1.getConnection();

		    dtcr = new Timestamp(System.currentTimeMillis());
		    ps1 =
			conn1.prepareStatement(
					       "insert into hproduct (name,createdate,updatedate) VALUES (?,?,?)");

		    ps1.setString(1, obj);
		    ps1.setTimestamp(2, dtcr);
		    ps1.setTimestamp(3, dtcr);
		    res = ps1.executeUpdate();

		} catch (Exception e) {
		    System.out.println(
				       "pb insert BASE1: "
				       + e.getClass()
				       + ": "
				       + e.getMessage());
		    ut.setRollbackOnly();

		} finally {
		    try {
			if (ps1 != null)
			    ps1.close();
			if (conn1 != null)
			    conn1.close();
			conn1 = null;
		    } catch (SQLException sqle) {
			System.out.println(
					   "close pb BASE1: "
					   + sqle.getClass()
					   + ": "
					   + sqle.getMessage());
		    }

		}
		//

		try {
		    System.out.println("insert BASE2 ");
		    hds2 = (DataSource) spds2;
		    conn2b = hds2.getConnection();

		    dtcr = new Timestamp(System.currentTimeMillis());
		    ps2b =
			conn2b.prepareStatement(
						"insert into horder (ordernum,customer,createdate,updatedate) VALUES (?,?,?,?)");
		    ps2b.setString(1, objs);
		    ps2b.setString(2, client);
		    ps2b.setTimestamp(3, dtcr);
		    ps2b.setTimestamp(4, dtcr);
		    res = ps2b.executeUpdate();

		} catch (Exception e) {
		    System.out.println(
				       "pb insert BASE2: "
				       + e.getClass()
				       + ": "
				       + e.getMessage());
		    ut.setRollbackOnly();
		} finally {
		    try {
			if (ps2b != null)
			    ps2b.close();
			if (conn2b != null)
			    conn2b.close();
			conn2b = null;
		    } catch (SQLException sqle) {
			System.out.println(
					   "close pb BASE2: "
					   + sqle.getClass()
					   + ": "
					   + sqle.getMessage());
		    }
		}

		int stat = ut.getStatus();
		if (stat == Status.STATUS_ACTIVE) {
		    System.out.println(
				       "##### ut commit " + obj + " " + objs + " " + client);
		    ut.commit();
		} else {
		    System.out.println(
				       "##### ut rollback " + obj + " " + objs + " " + client);
		    ut.rollback();
		}
	    } catch (Exception e) {
		System.out.println(
				   "UT termination: " + e.getClass() + ": " + e.getMessage());
		try {
		    if (!(e instanceof RollbackException)) {
			System.out.println("ut rollback...");
			ut.rollback();
		    }
		} catch (Exception e1) {
		    System.out.println(
				       "rollback: " + e1.getClass() + ": " + e1.getMessage());
		}

	    }

	}

    }
    public static void main(String[] args) {

	init();

	Connect co1 =
	    new Connect("abcdefghijklmnopqrstuvwxyz", "leclerc", 1000);

	Connect co2 =
	    new Connect("ABCDEFGHIJKLMNOPQRSTUVWXYa", "carrefour", 2000);

	Thread t1 = new Thread(co1);
	Thread t2 = new Thread(co2);

	t1.start();
	t2.start();
	try {
	    t1.join();
	    t2.join();

	} catch (InterruptedException ie) {

	} finally {
	    System.exit(0);
	}

    }
}
