
import junit.framework.Assert;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.ResultSet;

import org.enhydra.jdbc.standard.StandardXAConnectionHandle;

/**
 * This object does not contains TestCase from Junit, but only
 * tests from XAPool. All tests begin with test...
 * Please see setUp and tearDown from XAPoolTestCase to take a 
 * look to the initialisation of the database (XA) and JOTM 
 */
public class XAPoolTestSuite extends XAPoolTestCase {
    public XAPoolTestSuite(String strTestName) {
	super(strTestName);
    }

    public void testSimpleStatementCommit() throws Exception {
	int newValue = 54;
	String completion = "commit";
	System.out.println("SimpleStatementCommit: begin, getConnection, executeUpdate, VERIFY, closeAll, commit, getConnection, VERIFY, close");
	try {
            utx.begin();
	    
            conn = spds.getConnection(login, password);
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+newValue+" does not work", getValue() == 54);
    	    pstmt.close();
            conn.close();

	    utx.commit();

        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        conn = spds.getConnection(login, password);
	Assert.assertTrue("after commit with "+newValue+" does not work", getValue() == 54);
        conn.close();        
    }
    
    public void testSimpleStatementRollback() throws Exception {
	int newValue = 12;
	String completion = "rollback";
	System.out.println("SimpleStatementRollback: begin, getConnection, executeUpdate, VERIFY, closeAll, rollback, getConnection, VERIFY, close");
	try {
            utx.begin();
	    
            conn = spds.getConnection(login, password);
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+newValue+" does not work", getValue() == 12);
    	    pstmt.close();
            conn.close();

	    utx.rollback();

        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        conn = spds.getConnection(login, password);
	Assert.assertTrue("after rollback with "+newValue+" does not work", getValue() == 54);
        conn.close();        
    }
    
    public void testSaveAutoCommitTrue() throws Exception {
	int newValue = 12;
	System.out.println("SaveAutoCommitTrue: getConnection, setAutoCommit(true), begin, executeUpdate, rollback, VERIFY, closeAll");
	try {
            conn = spds.getConnection(login, password);
            boolean autocom = true;
            conn.setAutoCommit(autocom);
	    
            utx.begin();
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();

    	    utx.rollback();
	    Assert.assertTrue("Saveautocommit with TRUE does not work", conn.getAutoCommit() == autocom);
	    pstmt.close();
            conn.close();
	    
        } catch (Exception e) {
            System.out.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.out.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void testSaveAutoCommitFalse() throws Exception{
	int newValue = 12;
	System.out.println("SaveAutoCommitFalse: getConnection, setAutoCommit(false), begin, executeUpdate, rollback, VERIFY, closeAll");
        try {
            conn = spds.getConnection(login, password);
            boolean autocom = false;
            conn.setAutoCommit(autocom);

            utx.begin();
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
    	    utx.rollback();

	    Assert.assertTrue("Saveautocommit with FALSE does not work", conn.getAutoCommit() == autocom);
	    pstmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.out.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    public void testMultipleConnectionCommit() throws Exception {
	int newValue = 66;
	System.out.println("MultipleConnectionCommit: begin, getConnection, executeUpdate, VERIFY, closeAll, getConnection, executeUpdate, VERIFY, closeAll, commit, getConnection, VERIFY, close");
        try {
            utx.begin();

            conn = spds.getConnection(login, password);
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+(newValue)+" does not work", getValue() == 66);
    	    pstmt.close();
            conn.close();

	    conn = spds.getConnection(login, password);
	    PreparedStatement pstmt2 = conn.prepareStatement(SQL_QUERY);
            pstmt2.setInt(1, newValue+2);
            pstmt2.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+(newValue+2)+" does not work", getValue() == 68);
    	    pstmt2.close();
            conn.close();

	    utx.commit();

        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        conn = spds.getConnection(login, password);
	Assert.assertTrue("after commit with "+(newValue+2)+" does not work", getValue() == 68);
        conn.close();
    }


    public void testMultipleConnectionRollback() throws Exception {
	int newValue = 900;
	System.out.println("MultipleConnectionCommit: begin, getConnection, executeUpdate, VERIFY, closeAll, getConnection, executeUpdate, VERIFY, closeAll, rollback, getConnection, VERIFY, close");
        try {
            utx.begin();

            conn = spds.getConnection(login, password);
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+(newValue)+" does not work", getValue() == 900);
    	    pstmt.close();
            conn.close();

	    conn = spds.getConnection(login, password);
	    PreparedStatement pstmt2 = conn.prepareStatement(SQL_QUERY);
            pstmt2.setInt(1, newValue+2);
            pstmt2.executeUpdate();
	    Assert.assertTrue("executeUpdate with "+(newValue+2)+" does not work", getValue() == 902);
    	    pstmt2.close();
            conn.close();

	    utx.rollback();    // ***** ROLLBACK ******

        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        conn = spds.getConnection(login, password);
	Assert.assertTrue("after rollback with "+(newValue+2)+" does not work", getValue() == 68);
        conn.close();
    }

    public void testMultipleTransaction() throws Exception {
	System.out.println("MultipleTransaction: getConnection, begin, executeUpdate, VERIFY, commit, VERIFY, begin, executeUpdate, VERIFY, rollback, VERIFY, close");
	try {
	    conn = spds.getConnection(login, password);
	} catch(Exception e) {
	    System.out.println("problem");
	    if (conn == null) {
		System.out.println("the connection is null");
		// do some work
	    }
	    else {
		System.out.println("the connection is not null");
		// do some work
	    }
	}
	int newValue = 44;
        try {
            utx.begin();
            PreparedStatement pstmt0 = conn.prepareStatement(SQL_QUERY);
            pstmt0.setInt(1, 13);
            pstmt0.executeUpdate();
	    pstmt0.close();
	    Assert.assertTrue("executeUpdate with 13 does not work", getValue() == 13);
            utx.commit();

	    Assert.assertTrue("after commit with 13 does not work", getValue() == 13);

            utx.begin();
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
	    pstmt.close();
	    Assert.assertTrue("executeUpdate with "+newValue+" does not work", getValue() == 44);
	    
	    utx.rollback();

	    Assert.assertTrue("after rollback with "+newValue+" does not work", getValue() == 13);

        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        conn.close();

    }
    
    public void testPotentialDeadLock() throws Exception {
	System.out.println("PotentialDeadLock: begin, getConnection, executeUpdate, VERIFY, pclose, VERIFY, close, VERIFY, commit, VERIFY");
	int newValue = 44;
        try {
            utx.begin();
	    conn = spds.getConnection(login, password);
            PreparedStatement pstmt0 = conn.prepareStatement(SQL_QUERY);
            pstmt0.setInt(1, 133);
            pstmt0.executeUpdate();
	    Assert.assertTrue("executeUpdate with 133 does not work", getValue() == 133);
	    pstmt0.close();
	    Assert.assertTrue("after stmt.close: executeUpdate with 133 does not work", getValue() == 133);
	    conn.close();
	    Assert.assertTrue("after con.close: executeUpdate with 133 does not work (value is "+getValue()+") ", getValue() == 133);
	    utx.commit();
	    Assert.assertTrue("after commit with 133 does not work", getValue() == 133);
        } catch (Exception e) {
            System.err.println("Exception of type :" + e.getClass().getName() + " has been thrown");
            System.err.println("Exception message :" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void testMiroRequestConnectionCountInTransaction() throws Exception {
	StandardXAConnectionHandle tempCon = null;
	StringBuffer sbBuffer = new StringBuffer();
	String strCon;
	PreparedStatement insertStatement = null;;
	Map mpReturnPatternMap = new HashMap(); 
	Map mpConnections = new HashMap();
	Map mpUsedConnections = new HashMap();
	List lstConnections = new ArrayList();
	int iTable1Index = 0;
	int iInsertCount;
	System.out.println("RequestConnectionCountInTransaction: Request multiple connections in one transactions over and over and see if they will be returned in the same order.");
	try {
	    try {
		utx.begin();
		
		for (int iIndex = 0; iIndex < 10; iIndex++) {   
		    try {
			sbBuffer.delete(0, sbBuffer.length());
			for (int iCon = 0; iCon < 4; iCon++) {
			    conn = spds.getConnection();
			    Assert.assertTrue("XAPool no longer returns instance" +
					      " of StandardXAConnectionHandle",
					      conn instanceof StandardXAConnectionHandle);
			    tempCon = (StandardXAConnectionHandle)conn;
			    strCon = tempCon.con.toString();
			    sbBuffer.append(strCon);
			    sbBuffer.append("::");
			    lstConnections.add(conn);
			    mpConnections.put(strCon, strCon);
			    try {
				insertStatement = conn.prepareStatement(SQL_QUERY);
				insertStatement.setInt(1, iTable1Index++);
				iInsertCount = insertStatement.executeUpdate();
				Assert.assertEquals("One record should have been inserted.", 
						    1, iInsertCount);
			    }
			    finally {
				insertStatement.close();
				insertStatement = null;                        
			    }
			    tempCon = (StandardXAConnectionHandle)conn;
			    strCon = tempCon.con.toString();
			    mpUsedConnections.put(strCon, strCon);
			}
			mpReturnPatternMap.put(sbBuffer.toString(), sbBuffer.toString());
		    }
		    finally {
			for (int iCon = 0; iCon < 4; iCon++) {
			    ((Connection)lstConnections.remove(0)).close();
			}
		    }
		}
		/*
		  System.out.println("Total allocated connections = "+ mpConnections.size());
		  System.out.println("Total used connections = " + mpUsedConnections.size());
		  System.out.println("Connections return pattern count = " + mpReturnPatternMap.size());
		  for (Iterator itrElements = mpReturnPatternMap.values().iterator(); itrElements.hasNext();) {
		  System.out.println(itrElements.next().toString());
		  }
		*/
		Assert.assertEquals("More than expected number of connections were used.", 4, mpUsedConnections.size());
		Assert.assertEquals("More than expected number of connections were allocated.", 4, mpConnections.size());
		utx.commit();
	    }
	    catch (Throwable throwable) {
		utx.rollback();
	    }
	    
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	      
	      utx.begin();
	      try {
	      try {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      } finally {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      utx.commit();
	      } catch (Throwable throwable) {
	      utx.rollback();
	    }
	    */
	}
    }
    




    
    public void testMiroRequestConnectionCountOutOfTransaction() throws Exception {
	Connection                 con = null;
	StandardXAConnectionHandle tempCon;
	String                     strCon;
	int                        iInsertCount;
	PreparedStatement          insertStatement = null;;
	Map                        mpReturnPatternMap = new HashMap(); 
	Map                        mpConnections = new HashMap();
	Map                        mpUsedConnections = new HashMap();
	List                       lstConnections = new ArrayList();
	int                        iTable1Index = 0;
	int                        iIndex;
	int                        iCon;
	StringBuffer               sbBuffer = new StringBuffer();
	System.out.println("RequestConnectionCountOutOfTransaction: Request multiple connections outside of transactions over and over and see if they will be returned in the same order.");
	try {
	    
	    for (iIndex = 0; iIndex < 10; iIndex++) {   
		try {
		    sbBuffer.delete(0, sbBuffer.length());
		    for (iCon = 0; iCon < 4; iCon++) {
			conn = spds.getConnection();
			Assert.assertTrue("XAPool no longer returns instance" +
					  " of StandardXAConnectionHandle",
					  conn instanceof StandardXAConnectionHandle);
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			sbBuffer.append(strCon);
			sbBuffer.append("::");
			lstConnections.add(conn);
			mpConnections.put(strCon, strCon);
			try {
			    insertStatement = conn.prepareStatement(SQL_QUERY);
			    insertStatement.setInt(1, iTable1Index++);
			    iInsertCount = insertStatement.executeUpdate();
			    Assert.assertEquals("One record should have been inserted.", 
						1, iInsertCount);
			} finally {
			    insertStatement.close();
			    insertStatement = null;                        
			}
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			mpUsedConnections.put(strCon, strCon);
			
			// DOn't commit immidiately, commit later so we can simmulate
			// the jotm transaction behaviour
		    }
		    mpReturnPatternMap.put(sbBuffer.toString(), sbBuffer.toString());
		} finally {
		    for (iCon = 0; iCon < 4; iCon++) {
			try {
			    conn = (Connection)lstConnections.remove(0);

			} catch (Throwable thr) {
			} finally {
			    conn.close();
			    con = null;
			}
		    }
		}
	    }
	    /*
	      Log.getInstance().info("testRequestConnectionCountOutOfTransaction =====");
	      Log.getInstance().info("Total allocated connections = " 
	      + mpConnections.size());
	      Log.getInstance().info("Total used connections = " 
	      + mpUsedConnections.size());
	      Log.getInstance().info("Connections return pattern count = " 
	      + mpReturnPatternMap.size());
	      for (Iterator itrElements = mpReturnPatternMap.values().iterator(); 
              itrElements.hasNext();)
	      {
	      Log.getInstance().info(itrElements.next().toString());
	      }
	    */
	    Assert.assertEquals("More than expected number of connections were used.",
				4, mpUsedConnections.size());
	    Assert.assertEquals("More than expected number of connections were allocated.",
				4, mpConnections.size());
	    //         Assert.assertEquals("Connections were not returned always in the same order.",
	    //                             1, mpReturnPatternMap.size());
	} finally {
	    /*    
		  PreparedStatement deleteStatement = null;
		  
		  m_transaction.begin();
		  try {
		  try {
		  deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
		  int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
		  } finally {
		  DatabaseUtils.closeStatement(deleteStatement);
		  deleteStatement = null;            
		  }
		  m_transaction.commit();
		  } catch (Throwable throwable) {
		  m_transaction.rollback();
		  throw throwable;
		  }
	    */
	}
    }



    public void testMiroRepeatedRequestConnectionInTransaction() throws Throwable {
	System.out.println("RepeatedRequestConnectionInTransaction: Request one connection over and over in one transactions and see if the same connection will be returned.");
	try {
	    // Just get connections and return
	    try {
		StandardXAConnectionHandle tempCon;
		String                     strCon;
		int                        iInsertCount;
		PreparedStatement          insertStatement = null;;
		int                        iTable1Index = 0;
		int                        iIndex;
		int                        iConChange = 0;
		String                     strLastCon = null;
		Map                        mpConnections = new HashMap();
		
		utx.begin();
		for (iIndex = 0; iIndex < 10; iIndex++) {   
		    try {
			conn = spds.getConnection();
			Assert.assertTrue("XAPool no longer returns instance" +
					  " of StandardXAConnectionHandle",
					  conn instanceof StandardXAConnectionHandle);
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			if (strLastCon == null) {
			    strLastCon = strCon;
			} else {
			    if (!strCon.equals(strLastCon)) {
				// The connection has changed, count how many times 
				// it has changed
				strLastCon = strCon;
				iConChange++;
			    }
			}
			mpConnections.put(strCon, strCon);
			
			// Don't do anything here, just get and return
		    }
		    finally {
			conn.close();
			conn = null;
		    }
		}
		/*
		  Log.getInstance().info("testRepeatedRequestConnectionInTransaction =====");
		  for (Iterator itrElements = mpConnections.values().iterator(); 
		  itrElements.hasNext();)
		  {
		  Log.getInstance().info(itrElements.next().toString());
		  }
		  Log.getInstance().info("Total allocated connections = " 
		  + mpConnections.size());
		  Log.getInstance().info("How many times pool returned different connection = " 
		  + iConChange);
		*/
		Assert.assertEquals("More than expected number of connections were allocated.",
				    1, mpConnections.size());
		
		utx.commit();
	    } catch (Throwable throwable) {
		utx.rollback();
		throw throwable;
	    }
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	      
	      m_transaction.begin();
	      try {
	      try {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      } finally {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      m_transaction.commit();
	      } catch (Throwable throwable) {
	      m_transaction.rollback();
	      throw throwable;
	      }
	    */
	}
    }


    
    public void testMiroRepeatedRequestConnectionWithSelectInTransaction() throws Throwable {
	System.out.println("RepeatedRequestConnectionWithSelectInTransaction: Request one connection over and over in one transactions and do select and then see if the same connection will be returned and used");
	try {
	    // Get connection, select and return
	    try {
		StandardXAConnectionHandle tempCon;
		String                     strCon;
		PreparedStatement          selectStatement = null;
		int                        iTable1Index = 0;
		int                        iIndex;
		int                        iConChange = 0;
		String                     strLastCon = null;
		ResultSet                  rsResults = null;
		Map                        mpConnections = new HashMap();
		Map                        mpUsedConnections = new HashMap();
		
		utx.begin();
		for (iIndex = 0; iIndex < 10; iIndex++) {   
		    try {
			conn = spds.getConnection();
			Assert.assertTrue("XAPool no longer returns instance" +
					  " of StandardXAConnectionHandle",
					  conn instanceof StandardXAConnectionHandle);
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			if (strLastCon == null) {
			    strLastCon = strCon;
			} else {
			    if (!strCon.equals(strLastCon)) {
				// The connection has changed, count how many times 
				// it has changed
				strLastCon = strCon;
				iConChange++;
			    }
			}
			mpConnections.put(strCon, strCon);
			
			// Now try to select using this connection
			try {
			    selectStatement = conn.prepareStatement(SQL_REQUEST);
			    rsResults = selectStatement.executeQuery();
			    rsResults.close();
			} finally {

			    selectStatement.close();
			    rsResults = null;
			    selectStatement = null;
			}
			
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			mpUsedConnections.put(strCon, strCon);                  
		    } finally {
			conn.close();
			conn = null;
		    }
		}
		/*
		  Log.getInstance().info("testRepeatedRequestConnectionInTransaction =====");
		  Log.getInstance().info("Total allocated connections when selecting = " 
		  + mpConnections.size());
		  for (Iterator itrElements = mpConnections.values().iterator(); 
		  itrElements.hasNext();)
		  {
		  Log.getInstance().info(itrElements.next().toString());
		  }
		  Log.getInstance().info("Total used connections when selecting = " 
		  + mpUsedConnections.size());
		  for (Iterator itrElements = mpUsedConnections.values().iterator(); 
		  itrElements.hasNext();)
		  {
		  Log.getInstance().info(itrElements.next().toString());
		  }
		  Log.getInstance().info("How many times pool returned different connection" +
		  " when selecting = " + iConChange);
		*/
		Assert.assertEquals("More than expected number of connections were used" +
				    " when selecting.", 1, mpUsedConnections.size());
		// This really doesn't mean anything, two connections were taken from the pool
		// but only 1 was used so that should be OK
		//            Assert.assertEquals("More than expected number of connections were allocated" +
		//                                " when selecting.", 1, mpConnections.size());
		
		utx.commit();
	    } catch (Throwable throwable) {
		utx.rollback();
		throw throwable;
	    }
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	      
	      m_transaction.begin();
	      try
	      {
	      try
	      {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      }
	      finally
	      {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      m_transaction.commit();
	      }
	      catch (Throwable throwable)
	      {
	      m_transaction.rollback();
	      throw throwable;
	      }
	    */
	}
    }

    
    public void testMiroRepeatedRequestConnectionWithSelectOutOfTransaction() throws Throwable {
	System.out.println("RepeatedRequestConnectionWithSelectOutOfTransaction: Request one connection over and over without transactions and do select and then see if the same connection will be returned and used");
	try {
	    StandardXAConnectionHandle tempCon;
	    String                     strCon;
	    PreparedStatement          selectStatement = null;;
	    int                        iTable1Index = 0;
	    int                        iIndex;
	    int                        iConChange = 0;
	    String                     strLastCon = null;
	    ResultSet                  rsResults = null;
	    Map                        mpConnections = new HashMap();
	    Map                        mpUsedConnections = new HashMap();
	    
	    for (iIndex = 0; iIndex < 10; iIndex++) {   
		try {
		    conn = spds.getConnection();
		    Assert.assertTrue("XAPool no longer returns instance" +
				      " of StandardXAConnectionHandle",
				      conn instanceof StandardXAConnectionHandle);
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    if (strLastCon == null) {
			strLastCon = strCon;
		    } else {
			if (!strCon.equals(strLastCon)) {
			    // The connection has changed, count how many times 
			    // it has changed
			    strLastCon = strCon;
			    iConChange++;
			}
		    }
		    mpConnections.put(strCon, strCon);
		    
		    // Now try to select using this connection
		    try {
			selectStatement = conn.prepareStatement(SQL_REQUEST);
			rsResults = selectStatement.executeQuery();
			rsResults.close();
		    } finally {

			selectStatement.close();

			rsResults = null;
			selectStatement = null;
		    }
		    
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    mpUsedConnections.put(strCon, strCon);                                 
		} finally {
		    conn.close();
		    conn = null;
		}
	    }
	    /*
	      Log.getInstance().info("testRepeatedRequestConnectionWithSelectOutOfTransaction =====");
	      Log.getInstance().info("Total allocated connections when selecting = " 
	      + mpConnections.size());
	      for (Iterator itrElements = mpConnections.values().iterator(); 
	      itrElements.hasNext();)
	      {
	      Log.getInstance().info(itrElements.next().toString());
	      }
	      Log.getInstance().info("Total used connections when selecting = " 
	      + mpUsedConnections.size());
	      for (Iterator itrElements = mpUsedConnections.values().iterator(); 
	      itrElements.hasNext();)
	      {
	      Log.getInstance().info(itrElements.next().toString());
	      }
	      Log.getInstance().info("How many times pool returned different connection" +
	      " when selecting = " + iConChange);
	    */
	    Assert.assertEquals("More than expected number of connections were used" +
				" when selecting.", 1, mpUsedConnections.size());
	    Assert.assertEquals("More than expected number of connections were allocated" +
				" when selecting.", 1, mpConnections.size());
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	      
	      m_transaction.begin();
	      try
	      {
	      try
	      {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      }
	      finally
	      {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      m_transaction.commit();
	      }
	      catch (Throwable throwable)
	      {
	      m_transaction.rollback();
	      throw throwable;
	      }
	    */
	}
    }
    

    public void testMiroRepeatedRequestConnectionWithInsertInTransaction() throws Throwable {
	System.out.println("RepeatedRequestConnectionWithInsertInTransaction: Request one connection over and over in one transactions and do insert and then see if the same connection will be returned and used");
	try {
	    try {
		StandardXAConnectionHandle tempCon;
		String                     strCon;
		int                        iInsertCount;
		PreparedStatement          insertStatement = null;;
		int                        iTable1Index = 0;
		int                        iIndex;
		int                        iConChange = 0;
		String                     strLastCon = null;
		Map                        mpConnections = new HashMap();
		Map                        mpUsedConnections = new HashMap();
		
		utx.begin();
		for (iIndex = 0; iIndex < 10; iIndex++) {   
		    try {
			conn = spds.getConnection();
			Assert.assertTrue("XAPool no longer returns instance" +
					  " of StandardXAConnectionHandle",
					  conn instanceof StandardXAConnectionHandle);
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			if (strLastCon == null) {
			    strLastCon = strCon;
			} else {
			    if (!strCon.equals(strLastCon)) {
				// The connection has changed, count how many times 
				// it has changed
				strLastCon = strCon;
				iConChange++;
			    }
			}
			mpConnections.put(strCon, strCon);
			
			// Now try to insert using this connection
			try {
			    insertStatement = conn.prepareStatement(SQL_QUERY);
			    insertStatement.setInt(1, iTable1Index++);
			    iInsertCount = insertStatement.executeUpdate();
			    Assert.assertEquals("One record should have been inserted.", 
						1, iInsertCount);
			} finally {
			    insertStatement.close();
			    insertStatement = null;                        
			}
			
			tempCon = (StandardXAConnectionHandle)conn;
			strCon = tempCon.con.toString();
			mpUsedConnections.put(strCon, strCon);                  
		    } finally {
			conn.close();
			conn = null;
		    }
		}
		/*
		  Log.getInstance().info("testRepeatedRequestConnectionWithInsertInTransaction =====");
		  Log.getInstance().info("Total allocated connections when inserting = " 
		  + mpConnections.size());
		  for (Iterator itrElements = mpConnections.values().iterator(); 
		  itrElements.hasNext();)
		  {
		  Log.getInstance().info(itrElements.next().toString());
		  }
		  Log.getInstance().info("Total used connections when inserting = " 
		  + mpUsedConnections.size());
		  for (Iterator itrElements = mpUsedConnections.values().iterator(); 
		  itrElements.hasNext();)
		  {
		  Log.getInstance().info(itrElements.next().toString());
		  }
		  Log.getInstance().info("How many times pool returned different connection" +
		  " when inserting = " + iConChange);
		*/
		Assert.assertEquals("More than expected number of connections were used" +
				    " when inserting.", 1, mpUsedConnections.size());
		//          This really doesn't mean anything, two connections were taken from the pool
		//          but only 1 was used so that should be OK
		//            Assert.assertEquals("More than expected number of connections were allocated" +
		//                                " when inserting.", 1, mpConnections.size());
		
		utx.commit();
	    } catch (Throwable throwable) {
		utx.rollback();
		throw throwable;
	    }
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	      
	      m_transaction.begin();
	      try
	      {
	      try
	      {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      }
	      finally
	      {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      m_transaction.commit();
	      }
	      catch (Throwable throwable)
	      {
	      m_transaction.rollback();
	      throw throwable;
	      }
	    */
	}
    }
    
    public void testMiroInsertSelectInTransaction() throws Throwable {
	System.out.println("InsertSelectInTransaction: We try to simulate situation, when xapool returns different connections for first and second request inside of the same transaction and see if it affects functionality.");
	try {
	    Map mpConnections = new HashMap();
	    Map mpUsedConnections = new HashMap();
	    int iConChange = 0;
	    
	    try {
		StandardXAConnectionHandle tempCon;
		String                     strCon;
		int                        iInsertCount;
		PreparedStatement          insertStatement = null;
		PreparedStatement          selectStatement = null;
		ResultSet                  rsResults = null;
		int                        iTable1Index = 0;
		int                        iIndex;
		String                     strLastCon = null;
		
		utx.begin();
		
		try {
		    conn = spds.getConnection();
		    Assert.assertTrue("XAPool no longer returns instance" +
				      " of StandardXAConnectionHandle",
				      conn instanceof StandardXAConnectionHandle);
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    if (strLastCon == null) {
			strLastCon = strCon;
		    } else {
			if (!strCon.equals(strLastCon)) {
			    // The connection has changed, count how many times 
			    // it has changed
			    strLastCon = strCon;
			    iConChange++;
			}
		    }
		    mpConnections.put(strCon, strCon);
		    
		    // Now try to insert using this connection
		    try {
			insertStatement = conn.prepareStatement(SQL_QUERY);
			insertStatement.setInt(1, iTable1Index++);
			iInsertCount = insertStatement.executeUpdate();
			Assert.assertEquals("One record should have been inserted.", 
					    1, iInsertCount);
		    } finally {
			insertStatement.close();
			insertStatement = null;                        
		    }
		    
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    mpUsedConnections.put(strCon, strCon);                                 
		} finally {
		    conn.close();
		    conn = null;
		}
		
		try {
		    conn = spds.getConnection();
		    Assert.assertTrue("XAPool no longer returns instance" +
				      " of StandardXAConnectionHandle",
				      conn instanceof StandardXAConnectionHandle);
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    if (strLastCon == null) {
			strLastCon = strCon;
		    } else {
			if (!strCon.equals(strLastCon)) {
			    // The connection has changed, count how many times 
			    // it has changed
			    strLastCon = strCon;
			    iConChange++;
			}
		    }
		    mpConnections.put(strCon, strCon);
		    
		    // Now try to select using this connection
		    try {
			selectStatement = conn.prepareStatement(SQL_REQUEST);
			rsResults = selectStatement.executeQuery();
			Assert.assertTrue("Select didn't find inserted record in the database.",
					  rsResults.next());
		    } finally {
			rsResults.close();
			selectStatement.close();
			rsResults = null;
			selectStatement = null;
		    }
		    
		    tempCon = (StandardXAConnectionHandle)conn;
		    strCon = tempCon.con.toString();
		    mpUsedConnections.put(strCon, strCon);                                 
		} finally {
		    conn.close();
		    conn = null;
		}
		
		utx.commit();
	    } catch (Throwable throwable) {
		utx.rollback();
		throw throwable;
	    }
	    /*
	      Log.getInstance().info("testInsertSelectInTransaction =====");
	      Log.getInstance().info("Total allocated connections when insert/select = " 
	      + mpConnections.size());
	      for (Iterator itrElements = mpConnections.values().iterator(); 
	      itrElements.hasNext();)
	      {
	      Log.getInstance().info(itrElements.next().toString());
	      }
	      Log.getInstance().info("Total used connections when insert/select = " 
	      + mpUsedConnections.size());
	      for (Iterator itrElements = mpUsedConnections.values().iterator(); 
	      itrElements.hasNext();)
	      {
	      Log.getInstance().info(itrElements.next().toString());
	      }
	      Log.getInstance().info("How many times pool returned different connection" +
	      " when insert/select = " + iConChange);
	    */
	    Assert.assertEquals("More than expected number of connections were used" +
				" when insert/select.", 1, mpUsedConnections.size());
	    //       This really doesn't mean anything, two connections were taken from the pool
	    //       but only 1 was used so that should be OK
	    //         Assert.assertEquals("More than expected number of connections were allocated" +
	    //                             " when insert/select.", 1, mpConnections.size());
	} finally {
	    /*
	      PreparedStatement deleteStatement = null;
	    
	      m_transaction.begin();
	      try
	      {
	      try
	      {
	      deleteStatement = m_connection.prepareStatement(DELETE_ALL1);
	      int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
	      }
	      finally
	      {
	      DatabaseUtils.closeStatement(deleteStatement);
	      deleteStatement = null;            
	      }
	      m_transaction.commit();
	      }
	      catch (Throwable throwable)
	      {
	      m_transaction.rollback();
	      throw throwable;
	      }
	    */
	}
    }
    
    
    public void testMiroEmptyRequestReturnConnection() throws Throwable {
	System.out.println("EmptyRequestReturnConnection: This test triggers bug in out modified XAPool 1.3.1 which doesn't occur with latest the original XAPool or the latest XAPool.");
	
	Connection                 con1 = null;
	Connection                 con2 = null;
	StandardXAConnectionHandle tempCon;
	String                     strCon1;
	String                     strCon2;
	int                        iInsertCount;
	
	PreparedStatement insertStatement = null;;
	PreparedStatement selectStatement = null;;
	ResultSet         rsResults = null;
	
	try {
         try {
	     utx.begin();
	     final int VALUE_TEST_1 = 0;
	     Map mpConnectionMap = new HashMap();
	     
	     // Request a connection insert a record and return it to the pool
	     try {
		 con1 = spds.getConnection();
		 Assert.assertTrue("XAPool no longer returns instance of StandardXAConnectionHandle",
				   con1 instanceof StandardXAConnectionHandle);
		 tempCon = (StandardXAConnectionHandle)con1;
		 strCon1 = tempCon.con.toString();
		 mpConnectionMap.put(strCon1, new Integer(0));
		 
		 // Inserted record 0
		 insertStatement = con1.prepareStatement(SQL_QUERY);
		 insertStatement.setInt(1, VALUE_TEST_1);
		 iInsertCount = insertStatement.executeUpdate();
		 Assert.assertEquals("One record should have been inserted.", 1, iInsertCount);
	     } finally {
		 insertStatement.close();
		 insertStatement = null;
		 con1.close();
		 con1 = null;
	     }
	     
	     boolean bDetectedTheSame = false;
	     int     iIndex;
	     
	     // Now request a connection from the pool, if it is a connection which
	     // was already requested before, just return it to the pool. 
	     // -----------------------------------------------------------------
	     // This should trigger bug in our modified XAPool 1.3.1 which will cause rollback 
	     // on the record inserted before using the same connection.
	     // -----------------------------------------------------------------
	     // If it is not the same connection, then insert new record and return 
	     // connection to the pool.
	     // Loop till 100 which is hopefully less than max connection count
	     // therefore we should get at some point the same connection
	     for (iIndex = 1; iIndex < 100; iIndex++) {   
		 try {
		     con2 = spds.getConnection();
		     Assert.assertTrue("XAPool no longer returns instance" 
				       + " of StandardXAConnectionHandle",
				       con2 instanceof StandardXAConnectionHandle);
		     tempCon = (StandardXAConnectionHandle)con2;
		     strCon2 = tempCon.con.toString();
		     
		     if (mpConnectionMap.get(strCon2) == null) {   
			 // This is highly undesirable, since it can cause deadlock,
			 // but I will let it be for now and use other test to deal with it
			 
			 // Inserted record 1, 2, ...
			 insertStatement = con2.prepareStatement(SQL_QUERY);
			 insertStatement.setInt(1, iIndex);
			 iInsertCount = insertStatement.executeUpdate();
			 Assert.assertEquals("One record should have been inserted.", 1, iInsertCount);
		     } else {
			 // Retrieve the index which was already inserted
			 iIndex = ((Integer)mpConnectionMap.get(strCon2)).intValue();
			 bDetectedTheSame = true;
			 // Just let it close the connection without doing anything
			 // -----------------------------------------------------------------
			 // This should trigger bug in our modified XAPool 1.3.1 which will 
			 // cause rollback on the record inserted before using the same connection.
			 // -----------------------------------------------------------------
			 break;
		     }
		     mpConnectionMap.put(strCon2, new Integer(iIndex));
		 } finally {
		     if (insertStatement != null)
			 insertStatement.close();
		     insertStatement = null;
		     con2.close();
		     con2 = null;
		 }
	     }
             
	     Assert.assertTrue("Unable to get the same connection from the pool.", 
			       bDetectedTheSame);
	     
	     // -----------------------------------------------------------------
	     // This detects bug in our modified XAPool 1.3.1 since the inserted record is 
	     // not there
	     // -----------------------------------------------------------------
	     try {
		 con1 = spds.getConnection();
		 selectStatement = con1.prepareStatement(SQL_QUERY);
		 selectStatement.setInt(1, iIndex);
		 try {
		     rsResults = selectStatement.executeQuery();
		     Assert.assertTrue("Canot find inserted record with index " + iIndex, 
				       rsResults.next());
		     rsResults.close();
		   
		 } catch (Exception e) {
		     // in case rsReults contains no data
		 }
	     } finally {

		 selectStatement.close();
		 selectStatement = null;
		 con1.close();
		 con1 = null;
	     }
	     
	     utx.commit();
         } catch (Throwable throwable) {
	     utx.rollback();
	     throw throwable;
         }
	} finally {
	    utx.begin();
	    try {
		conn = spds.getConnection();
		/*
		  PreparedStatement deleteStatement = conn.prepareStatement(DELETE_ALL);
		  
		  int iDeleteCount = DatabaseUtils.executeUpdateAndClose(deleteStatement);
		*/
		conn.close();
		utx.commit();
	    } catch (Throwable throwable) {
		utx.rollback();
		throw throwable;
	    }
	}
    } 
    
}
