// $Id: TASKServer.java,v 1.4 2004/03/09 01:34:14 philipb Exp $

/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
package net.tinyos.task.tasksvr;

import java.util.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import net.tinyos.tinydb.*;
import net.tinyos.tinydb.parser.*;
import net.tinyos.task.taskapi.*;
import net.tinyos.message.*;
import javax.servlet.*;
import org.mortbay.util.*;
import org.mortbay.http.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.http.handler.*;
import org.mortbay.servlet.*;     


/** TASKServer handles commands on port SERVER_PORT, 
    Commands have the form:
      commandid\n
      commandparam1\n
      commandparam2\n
      ...
   Results are returned as a character followed by a newline on this
   socket;  typically, a result code of '1' indicates success, 
   anything else indicates failure.  After the result code, the connection
   is usually closed, although some commands create persisten connections
   (e.g. ADD_LISTENER)

   See the list of available commands below
   @author madden@cs.berkeley.edu
*/
public class TASKServer implements ResultListener 
{
    public static final int DEFAULT_SERVER_PORT = 5431; //the default server port
    static final int MAX_HANG_TIME=512; //in ms

    /** RUN_QUERY_COMMAND initiates a query on the server
		parameters are as follows:
		1: query id (byte in range 0-127)
		2: query string (SQL string)
    */
    public static final short RUN_QUERY=1; 
    /** ADD_LISTENER adds a listener for a specified query id
		(which need not be running yet.)
		Parameters are as follows:
		1: query id
	
		Note that after the result code is returned, the 
		connection remains open and query results are streamed
		accross it
    */
    public static final short ADD_LISTENER=2;
	public static final short PREFETCH_METADATA=3;
	public static final short GET_QUERY=4;
	// moved to the client side for performance reasons
	// public static final short ESTIMATE_LIFETIME=5;
	// public static final short ESTIMATE_SAMPLEPERIODS=6;
	public static final short GET_CLIENTINFOS=7;
	public static final short GET_CLIENTINFO=8;
	public static final short ADD_CLIENTINFO=9;
	public static final short DELETE_CLIENTINFO=10;
	public static final short GET_MOTECLIENTINFO=11;
	public static final short ADD_MOTE=12;
	public static final short DELETE_MOTE=13;
	public static final short GET_ALLMOTECLIENTINFO=14;
	public static final short GET_SERVERCONFIGINFO=15;
	public static final short STOP_QUERY=16;
	public static final short RUN_COMMAND=17;
	public static final short DELETE_MOTES=18;
	public static final short RUN_CALIBRATION=19;

	public static final short SENSOR_QUERY=1;
	public static final short HEALTH_QUERY=2;

	// reserved TinyDB query id for the calibration query
	public static final byte CALIBRATION_TINYDB_QID=0;
	public static final byte CALIBRATION_QUERY_ID=0;
 
    //maximum number of outstaning connections
	static final int MAX_CLIENT_CONNECTIONS=5;


    /** Do the main loop of the server, listening on port <port> */
    private void doServer(int port) throws IOException
    {
		Socket sock = null;
		ObjectInputStream r;
		ObjectOutputStream w;
		InetAddress from;
		ServerSocket ss = null;
		SocketLoop sl;
		boolean closeSock;
		short commandId;

		try {
			ss = new ServerSocket(port);
			ss.setSoTimeout(MAX_HANG_TIME);
			sl = new SocketLoop(ss); //start the socket listener
		} catch (IOException e) {
			System.out.println("doServer failed, couldn't open new ServerSocket: " + e);
			throw e;
		}

		System.out.println("TASK Server started. Accepting connections...");

		while (true) {
			try
				{
					sock = sl.getConnection();

					w = new ObjectOutputStream(sock.getOutputStream());
					w.flush();
					r = new ObjectInputStream(sock.getInputStream());
					commandId = r.readShort();
					System.out.println("Read command: " + commandId);
					closeSock = processCommand(commandId, r, w, sock);
				
					if (closeSock)
						sock.close();
				}
			catch (SocketException e) { //probably means client disappeared
			    System.out.println("Socket exception, client is dead? \n" +e);
			    if (sock != null) sock.close();
			}
			catch (IOException e) 
				{
					System.out.println("Error in getting command -- " + e);
					throw e;
				} 
		    
		}
    }

	private void startHttpServer() throws IOException
	{
		ServletHttpContext httpContext;
		ServletContext svltContext;
		WebApplicationContext webAppContext;
		httpServer = new Server();
		httpServer.addListener(":8080");

		//httpContext  =  (ServletHttpContext)httpServer.getContext("/");
		//httpContext.setResourceBase(".");
		webAppContext = httpServer.addWebApplication("/","net/tinyos/task/tasksvr/web/");
		webAppContext.setDefaultsDescriptor("net/tinyos/task/tasksvr/webdefault.xml");
		webAppContext.setAttribute("TASKInstance",this);
		webAppContext.setAttribute("TASKDBMSConn",dbConn);
		//svltContext = webAppContext.getServletContext();
		//svltContext.setAttribute("TASKInstance",this);
		
		try {
		    //httpContext.addServlet("TASKData","/data/*","net.tinyos.task.tasksvr.TASKDataServlet"):
			//httpContext.addServlet("TASKCmd","/command/*","net.tinyos.task.tasksvr.TASKCommandServlet");
			//httpContext.addServlet("TASKQry","/","net.tinyos.task.tasksvr.TASKQueryServlet");
			httpServer.start();
			
		}
		catch (MultiException e) {
			List elst = e.getExceptions();
			Iterator it = elst.iterator();
			while (it.hasNext()) {
				Exception ex = (Exception)it.next(); 
				System.err.println("Exception caught: " + e.getMessage());
				e.printStackTrace();
			}
		}

	}
    
    /** Process a command received over the server socket 
		@param command The first line of the command from the socket
		@param inStream An ObjectInputStream containing the rest of the command (if any)
		@param sock The socket over which the command was received and acknowledgements can be sent
		@throws IOException If an IO error occurs
		@throws NumberFormatException If the command can't be parsed or is invalid
		@return true if the socket should be close, false if it should be left open.
    */
    private boolean processCommand(short commandId,
								   ObjectInputStream inStream, 
								   ObjectOutputStream outStream,
								   Socket sock) throws IOException, NumberFormatException
    {
		switch (commandId) 
			{
			case RUN_QUERY:
				{
					TASKQuery query = null;
					short whichQuery;
					int   error;
					try
						{
							query = (TASKQuery)inStream.readObject();
							whichQuery  = inStream.readShort();
							error = runTASKQuery(query,whichQuery);
						}
					catch (Exception e)
						{
							e.printStackTrace();
							error = TASKError.FAIL;
						}

					outStream.writeInt(error);
					outStream.flush();
					return true;
				}
			case ADD_LISTENER:
				{
					short whichQuery = inStream.readShort();
					TASKQuery query;
					Integer queryId;
					Vector ls;
					if (whichQuery == SENSOR_QUERY)
						query = sensorQuery;
					else
						query = healthQuery;
					queryId = new Integer(query.getQueryId());
					System.out.println("add listener for query id " + queryId);

					ls = (Vector)listeners.get(queryId);
					if (ls == null) 
						{
							ls = new Vector();
							listeners.put(queryId, ls);
						}
					ls.addElement(outStream);

					System.out.println("add listener done.");
					outStream.writeInt(TASKError.SUCCESS);
					outStream.flush();
					System.out.println("add listener success message sent.");
					return false;
				}
			case PREFETCH_METADATA:
				outStream.writeObject(attributeInfos);
				System.out.println("attributeinfos send done.");
				outStream.writeObject(commandInfos);
				System.out.println("commandinfos send done.");
				outStream.writeObject(aggregateInfos);
				System.out.println("aggregateinfos send done.");
				outStream.writeInt(TASKError.SUCCESS);
				outStream.flush();
				return true;
			case GET_QUERY:
				{
					short whichQuery = inStream.readShort();
					TASKQuery query = getTASKQuery(whichQuery);
					outStream.writeObject(query);
					outStream.flush();
					return true;
				}
			case GET_CLIENTINFOS:
				{
					Vector clientInfos = new Vector();
					try
						{
							ResultSet rs = dbStmt.executeQuery("SELECT name FROM task_client_info");
							while (rs.next())
								{
									clientInfos.add(rs.getString(1));
								}
							rs.close();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}
					outStream.writeObject(clientInfos);
					System.out.println("Vector of ClientInfo names sent.");
					outStream.flush();
					return true;
				}
			case GET_CLIENTINFO:
				{
					try
						{
							String name = (String)inStream.readObject();
							ResultSet rs = dbStmt.executeQuery("SELECT name, type, clientinfo FROM task_client_info where name = '" + name + "'");
							TASKClientInfo clientInfo;
							if (!rs.next())
								clientInfo = null;
							clientInfo = new TASKClientInfo(rs.getString(1), rs.getString(2), rs.getBytes(3));
							rs.close();
							outStream.writeObject(clientInfo);
						}
					catch (Exception e)
						{
							outStream.writeObject(null);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case ADD_CLIENTINFO:
				{
					TASKClientInfo clientInfo = null;
					try
						{
							clientInfo = (TASKClientInfo)inStream.readObject();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}
					PreparedStatement ps = null;
					try
						{
							ps = dbConn.prepareStatement("INSERT INTO task_client_info values (?, ?, ?)");
							ps.setString(1, clientInfo.name);
							ps.setString(2, clientInfo.type);
							ps.setBytes(3, clientInfo.data);
							ps.executeUpdate();
							ps.close();
							outStream.writeInt(TASKError.SUCCESS);
						}
					catch (SQLException e)
						{
							if (ps != null)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
											e1.printStackTrace();
										}
								}
							outStream.writeInt(TASKError.FAIL);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case DELETE_CLIENTINFO:
				{
					String name = null;
					try
						{
							name = (String)inStream.readObject();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}
					PreparedStatement ps = null;
					try
						{
							ps = dbConn.prepareStatement("DELETE FROM task_client_info WHERE name = ?");
							ps.setString(1, name);
							ps.executeUpdate();
							ps.close();
							outStream.writeInt(TASKError.SUCCESS);
						}
					catch (SQLException e)
						{
							if (ps != null)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
											e1.printStackTrace();
										}
								}
							outStream.writeInt(TASKError.FAIL);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case GET_MOTECLIENTINFO:
				{
					try
						{
							int moteId = inStream.readInt();
							ResultSet rs = dbStmt.executeQuery("SELECT mote_id, clientinfo_name, x_coord, y_coord, z_coord, moteinfo, clientinfo_name FROM task_mote_info where mote_id = " + moteId);
							TASKMoteClientInfo moteClientInfo;
							if (!rs.next())
								moteClientInfo = null;
							moteClientInfo = new TASKMoteClientInfo(rs.getInt(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getBytes(5), rs.getString(6));
							rs.close();
							outStream.writeObject(moteClientInfo);
						}
					catch (Exception e)
						{
							outStream.writeObject(null);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case ADD_MOTE:
				{
					TASKMoteClientInfo moteClientInfo = null;
					try
						{
							moteClientInfo = (TASKMoteClientInfo)inStream.readObject();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}
					PreparedStatement ps = null;
					try
						{
							ps = dbConn.prepareStatement("INSERT INTO task_mote_info values (?, ?, ?, ?, NULL, ?, ?)");
							ps.setInt(1, moteClientInfo.moteId);
							ps.setDouble(2, moteClientInfo.xCoord);
							ps.setDouble(3, moteClientInfo.yCoord);
							ps.setDouble(4, moteClientInfo.zCoord);
							ps.setBytes(5, moteClientInfo.data);
							ps.setString(6, moteClientInfo.clientInfoName);
							ps.executeUpdate();
							ps.close();
							outStream.writeInt(TASKError.SUCCESS);
						}
					catch (SQLException e)
						{
							if (ps != null)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
											e1.printStackTrace();
										}
								}
							outStream.writeInt(TASKError.FAIL);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case DELETE_MOTE:
				{
					int moteId = inStream.readInt();
					PreparedStatement ps = null;
					try
						{
							ps = dbConn.prepareStatement("DELETE FROM task_mote_info WHERE mote_id = ?");
							ps.setInt(1, moteId);
							ps.executeUpdate();
							ps.close();
							outStream.writeInt(TASKError.SUCCESS);
						}
					catch (SQLException e)
						{
							if (ps != null)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
										}
								}
							outStream.writeInt(TASKError.FAIL);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case DELETE_MOTES:
				{
					String clientInfoName = null;
					try
						{
							clientInfoName = (String)inStream.readObject();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}
					PreparedStatement ps = null;
					try
						{
							ps = dbConn.prepareStatement("DELETE FROM task_mote_info WHERE clientinfo_name = ?");
							ps.setString(1, clientInfoName);
							ps.executeUpdate();
							ps.close();
							outStream.writeInt(TASKError.SUCCESS);
						}
					catch (SQLException e)
						{
							if (ps != null)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
										}
								}
							outStream.writeInt(TASKError.FAIL);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case GET_ALLMOTECLIENTINFO:
				{
					try
						{
							String clientinfoName = (String)inStream.readObject();
							ResultSet rs = dbStmt.executeQuery("SELECT mote_id, clientinfo_name, x_coord, y_coord, z_coord, moteinfo, clientinfo_name FROM task_mote_info where clientinfo_name = '" + clientinfoName + "'");
							Vector moteClientInfos = new Vector();
							while (rs.next())
								{
									TASKMoteClientInfo moteClientInfo = new TASKMoteClientInfo(rs.getInt(1), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getBytes(6), rs.getString(2));
									moteClientInfos.add(moteClientInfo);
								}
							rs.close();
							outStream.writeObject(moteClientInfos);
						}
					catch (Exception e)
						{
							outStream.writeObject(null);
							e.printStackTrace();
						}
					outStream.flush();
					return true;
				}
			case GET_SERVERCONFIGINFO:
				{
					TASKServerConfigInfo serverConfigInfo;
					serverConfigInfo = new TASKServerConfigInfo(TinyDBMain.groupid,
																urlPSQL, dbUser, dbPwd,
																TinyDBMain.sfHost,
																TinyDBMain.sfPort,
																TinyDBMain.sfCommPort);
					outStream.writeObject(serverConfigInfo);
					outStream.flush();
					return true;
				}
			case STOP_QUERY:
				{
					short whichQuery = inStream.readShort();
					int error;

					error = stopTASKQuery(whichQuery);
					outStream.writeInt(error);
					outStream.flush();
					return true;
				}
			case RUN_COMMAND:
				{
					TASKCommand command = null;
					int error;
					try
						{
							command = (TASKCommand)inStream.readObject();
						}
					catch (Exception e)
						{
							e.printStackTrace();
						}

					error = runTASKCommand(command);
					outStream.writeInt(error);
				
					/* 
					   TASKCommandInfo commandInfo = null;
					   boolean found = false;
					   for (Iterator it = commandInfos.iterator(); it.hasNext(); )
					   {
					   commandInfo = (TASKCommandInfo)it.next();
					   if (commandInfo.getCommandName().equalsIgnoreCase(command.getCommandName()))
					   {
					   found = true;
					   break;
					   }
					   }
					   int error;
					   if (!found)
					   {
					   error = TASKError.INVALID_COMMAND;
					   }
					   else
					   {
					   Message cmdMessage = command.getTinyOSMessage(commandInfo);
					   if (cmdMessage != null)
					   error = sendTinyOSMessage(cmdMessage);
					   else
					   System.out.println("Invalid command.");
					   }
					   String cmdStr = command.toString(commandInfo);
					   PreparedStatement ps = null;
					   try
					   {
					   ps = dbConn.prepareStatement("INSERT INTO task_command_log VALUES (?, now(), ?)");
					   ps.setInt(1, nextCommandId());
					   ps.setString(2, cmdStr);
					   ps.executeUpdate();
					   ps.close();
					   outStream.writeInt(TASKError.SUCCESS);
					   }
					   catch (SQLException e)
					   {
					   if (ps != null)
					   {
					   try
					   {
					   ps.close();
					   }
					   catch (Exception e1)
					   {
					   }
					   }
					   outStream.writeInt(TASKError.FAIL);
					   e.printStackTrace();
					   }
					*/
					outStream.flush();
					return true;
				}
			case RUN_CALIBRATION:
				{
					if (calibQueryInProgress)
						{
							System.out.println("calibration query in progress.");
							outStream.writeInt(TASKError.SUCCESS);
							outStream.flush();
							return true;
						}
					else
						calibQueryInProgress = true;
					int error = TASKError.SUCCESS;
					PreparedStatement ps = null;
					if (calibTinyDBQuery == null)
						{
							String queryStr = "SELECT nodeid, prcalib SAMPLE PERIOD 2000";
							try 
								{
									calibTinyDBQuery = SensorQueryer.translateQuery(queryStr, CALIBRATION_TINYDB_QID);
								}
							catch (Exception e)
								{
									e.printStackTrace();
									error = TASKError.INVALID_QUERY;
									outStream.writeInt(error);
									outStream.flush();
									calibQueryInProgress = false;
									return true;
								}
							try
								{
									ps = dbConn.prepareStatement("INSERT INTO task_query_log VALUES (?, ?, ?, ?, ?)");
									ps.setInt(1, CALIBRATION_QUERY_ID);
									ps.setShort(2, (short)CALIBRATION_TINYDB_QID);
									ps.setString(3, queryStr);
									ps.setString(4, "calibration");
									ps.setString(5, "task_mote_info");
									ps.executeUpdate();
									ps.close();
								}
							catch (SQLException e)
								{
									// ignore exception here because the calibration
									// query might have already been logged
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
										}
								}
							try
								{
									ps = dbConn.prepareStatement("INSERT INTO task_query_time_log (query_id, start_time) VALUES (?, now())");
									ps.setInt(1, CALIBRATION_QUERY_ID);
									ps.executeUpdate();
									ps.close();
								}
							catch (SQLException e)
								{
									try
										{
											dbConn.setAutoCommit(true);
											ps.close();
										}
									catch (Exception e1)
										{
										}
									outStream.writeInt(TASKError.FAIL);
									e.printStackTrace();
									outStream.flush();
									calibQueryInProgress = false;
									return true;
								}
						}
					else
						{
							try
								{
									ps = dbConn.prepareStatement("UPDATE task_query_time_log SET stop_time = now() WHERE query_id = ? AND stop_time IS NULL");
									ps.setInt(1, CALIBRATION_QUERY_ID);
									ps.executeUpdate();
									ps.close();
									ps = dbConn.prepareStatement("INSERT INTO task_query_time_log (query_id, start_time) VALUES (?, now())");
									ps.setInt(1, CALIBRATION_QUERY_ID);
									ps.executeUpdate();
									ps.close();
								}
							catch (SQLException e)
								{
									try
										{
											ps.close();
										}
									catch (Exception e1)
										{
										}
									outStream.writeInt(TASKError.FAIL);
									e.printStackTrace();
									outStream.flush();
									calibQueryInProgress = false;
									return true;
								}
						}
					calibNodes = new Vector();
					try
						{
							ResultSet rs = dbStmt.executeQuery("SELECT distinct mote_id FROM task_mote_info WHERE calib IS NULL and mote_id > 0");
							while (rs.next())
								{
									calibNodes.add(new Integer(rs.getInt(1)));
								}
							rs.close();
						}
					catch (Exception e)
						{
							e.printStackTrace();
							error = TASKError.FAIL;
							outStream.writeInt(error);
							outStream.flush();
							calibQueryInProgress = false;
							return true;
						}
					if (calibNodes.isEmpty()) {
						System.out.println("Already have calibration data for all nodes.");
						try {
							outStream.writeInt(TASKError.SUCCESS);
							outStream.flush();
						} catch (IOException e) {
							//oh well...
						}
						calibQueryInProgress = false;
						return true;
					}
					try
						{
							int cnt = 0;
							while (calibQueryInProgress)
								{
									if (cnt == 15)
										{
											System.out.println("calibration query timed out.");
											error = TASKError.FAIL;
											outStream.writeInt(error);
											outStream.flush();
											calibQueryInProgress = false;
											stopRunningQuery(calibTinyDBQuery, CALIBRATION_QUERY_ID);
											return true;
										}
									System.out.println("sending calibration query.");
									TinyDBMain.injectQuery(calibTinyDBQuery, this);
									Thread.currentThread().sleep(4000);
									cnt++;
								}
						}
					catch (Exception e)
						{
							e.printStackTrace();
							error = TASKError.INVALID_QUERY;
							outStream.writeInt(error);
							outStream.flush();
							calibQueryInProgress = false;
							return true;
						}
					System.out.println("calibration query injected.");
					outStream.writeInt(TASKError.SUCCESS);
					outStream.flush();
					return true;
				}
			}
		return true;
    }

	private int sendTinyOSMessage(Message msg)
	{
		int error = TASKError.SUCCESS;
		try
			{
				System.out.print(msg);
				System.out.println("");
				TinyDBMain.mif.send(TASKCommand.BROADCAST_ID, msg);
			}
		catch (Exception e)
			{
				System.out.println("Error sending TinyOS message.");
				e.printStackTrace();
				error = TASKError.TINYOS_MESSAGE_SEND_FAILED;
			}
		return error;
	}

    /** ResultListener method called when a query returns are result
		TASKServer maps these onto its own set of result listener
		registered through the socket interface.
    */
    public void addResult(QueryResult qr) 
	{
		String tableName = null;
		int queryId = -1;
		if (sensorTinyDBQuery != null &&
			sensorTinyDBQuery.getId() == qr.qid())
			{
				tableName = sensorQuery.getTableName();
				queryId = sensorQuery.getQueryId();
			}
		else if (healthQuery != null &&
				 healthTinyDBQuery.getId() == qr.qid())
			{
				tableName = healthQuery.getTableName();
				queryId = healthQuery.getQueryId();
			}
		else if (qr.qid() == CALIBRATION_TINYDB_QID)
			{
				handleCalibrationResult(qr);
				return;
			}
		if (tableName == null)
			return;
		String insertStmt = DBLogger.insertStmt(qr, tableName);
		System.out.println("insert statement: " + insertStmt);
		try
			{
				if (dbConn == null)
					{
						// try to reconnect to database
						dbConn = DriverManager.getConnection(urlPSQL, dbUser, dbPwd);
						dbStmt = dbConn.createStatement();
					}
			
				dbStmt.executeUpdate(insertStmt);
			
			}
		catch (SQLException sqle) {
		    System.out.println("Got SQL exception! -" + sqle.getMessage());
		}
		catch (Exception e)
			{
				try
					{
						if (dbConn != null)
							dbConn.close();
					}
				catch (Exception e1)
					{
					}
				dbConn = null;
				e.printStackTrace();
			}
		Vector ls = (Vector)listeners.get(new Integer(queryId));
		/*
		  if (ls != null)
		  {
		  Iterator it = ls.iterator();
		  while (it.hasNext()) 
		  {
		  ObjectOutputStream outStream = (ObjectOutputStream)it.next();
		  try 
		  {
		  // XXX repeatedly sending fieldinfos is very wasteful!
		  Vector fieldInfos = new Vector();
		  Vector fieldValues = qr.getFieldValueObjs();
		  TinyDBQuery q = qr.getQuery();
		  for (int i = 0; i < fieldValues.size(); i++)
		  {
		  QueryField qf = q.getField(i);
		  fieldInfos.add(new TASKFieldInfo(qf));
		  }
		  fieldValues.add(new Integer(qr.epochNo()));
		  fieldInfos.add(new TASKFieldInfo("epoch", TASKTypes.UINT16));

		  TASKResult result = new TASKResult(queryId, fieldValues, fieldInfos);
		  outStream.writeObject(result);
		  outStream.flush();
		  System.out.println("TASKResult sent "+qr.epochNo());
		  }
		  catch (IOException e) 
		  {
		  System.out.println("Removing listener.");
		  it.remove(); //delete this listener, since it died
		  e.printStackTrace();
		  }
		  }
		  } */
    }

	private void handleCalibrationResult(QueryResult qr)
	{
		Vector fieldValues = qr.getFieldValueObjs();
		if (fieldValues.size() < 2)
			return;
		int nodeId = ((Integer)fieldValues.firstElement()).intValue();
		byte[] calib = (byte[])(fieldValues.elementAt(1));
		if (!calibQueryInProgress)
			{
				System.out.println("got calibration result from node " + nodeId + ", but the calibration query is no longer in progress.");
				return;
			}
		int mote = -1;
		int i = 0;
		for (Iterator it = calibNodes.iterator(); it.hasNext(); i++)
			{
				if (((Integer)it.next()).intValue() == nodeId)
					{
						mote = i;
						break;
					}
			}
		PreparedStatement ps = null;
		if (mote < 0)
			{
				System.out.println("already got calibration for node " + nodeId);
				return;
			}
		else
			{
				System.out.println("got new calibration for node " + nodeId);
				calibNodes.remove(mote);
				if (calibNodes.isEmpty())
					{
						TinyDBMain.network.abortQuery(calibTinyDBQuery);
						try
							{
								ps = dbConn.prepareStatement("UPDATE task_query_time_log SET stop_time = now() WHERE query_id = ? AND stop_time IS NULL");
								ps.setInt(1, CALIBRATION_QUERY_ID);
								ps.executeUpdate();
								ps.close();
							}
						catch (SQLException e)
							{
								try
									{
										ps.close();
									}
								catch (Exception e1)
									{
									}
								e.printStackTrace();
							}
						calibQueryInProgress = false;
					}
			}
		try
			{
				ps = dbConn.prepareStatement("UPDATE task_mote_info SET calib = ? WHERE mote_id = ?");
				ps.setBytes(1, calib);
				ps.setInt(2, nodeId);
				ps.executeUpdate();
				ps.close();
			}
		catch (SQLException e)
			{
				try
					{
						ps.close();
					}
				catch (Exception e1)
					{
					}
				e.printStackTrace();
			}
	}

    /** Stop a currently running  query */
    private boolean stopRunningQuery(TinyDBQuery q, int qid) {
		PreparedStatement ps = null;

		TinyDBMain.network.abortQuery(q);
		try
			{
				ps = dbConn.prepareStatement("UPDATE task_query_time_log SET stop_time = now() WHERE query_id = ? AND stop_time IS NULL");
				ps.setInt(1, qid);
				ps.executeUpdate();
				ps.close();
				return true;
			}
		catch (SQLException e)
			{
				try
					{
						ps.close();
					}
				catch (Exception e1)
					{
					}
				e.printStackTrace();
				return false;
			}
    }



	private void fetchRunningQueries(Vector qids)
	{
		short qid1 = -1, qid2 = -1;
		if (qids.size() == 1)
			qid1 = ((Integer)qids.firstElement()).shortValue();
		else if (qids.size() >= 2)
			{
				qid1 = ((Integer)qids.firstElement()).shortValue();
				qid2 = ((Integer)qids.elementAt(1)).shortValue();
				if (qids.size() > 2)
					System.out.println("more than two queries running in sensor network.");
			}
		System.out.println("feching running queries " + qid1 + " and " + qid2);
		if (qid1 != -1)
			{
				PreparedStatement ps = null;
				try
					{
						String sensorQueryStr = null, healthQueryStr = null, queryType;
						byte sensorTinyDBQid = 0, healthTinyDBQid = 0;
						int sensorQueryId = TASKQuery.INVALID_QUERYID, healthQueryId = TASKQuery.INVALID_QUERYID;
						String sensorTableName = null, healthTableName = null;
						ps = dbConn.prepareStatement("SELECT q.query_text, q.query_type, q.query_id, q.table_name FROM task_query_log q, task_query_time_log t WHERE q.tinydb_qid = ? AND q.query_id = t.query_id AND t.stop_time IS NULL;");
						ps.setInt(1, qid1);
						ResultSet rs = ps.executeQuery();
						if (rs.next())
							{
								queryType = rs.getString(2);
								if (queryType.equalsIgnoreCase("health"))
									{
										healthQueryStr = rs.getString(1);
										healthTinyDBQid = (byte)qid1;
										healthQueryId = rs.getInt(3);
										healthTableName = rs.getString(4);
									}
								else if (queryType.equalsIgnoreCase("sensor"))
									{
										sensorQueryStr = rs.getString(1);
										sensorTinyDBQid = (byte)qid1;
										sensorQueryId = rs.getInt(3);
										sensorTableName = rs.getString(4);
										System.out.println("fetched sensor query " + sensorQueryId + ": " + sensorQueryStr);
									}
							}
						rs.close();
						if (qid2 != -1)
							{
								ps.setInt(1, qid2);
								rs = ps.executeQuery();
								if (rs.next())
									{
										queryType = rs.getString(2);
										if (queryType.equalsIgnoreCase("health"))
											{
												healthQueryStr = rs.getString(1);
												healthTinyDBQid = (byte)qid2;
												healthQueryId = rs.getInt(3);
												healthTableName = rs.getString(4);
											}
										else
											{
												sensorQueryStr = rs.getString(1);
												sensorTinyDBQid = (byte)qid2;
												sensorQueryId = rs.getInt(3);
												sensorTableName = rs.getString(4);
												System.out.println("fetched sensor query " + sensorQueryId + ": " + sensorQueryStr);
											}
									}
								rs.close();
							}
						ps.close();
						ps = null;
						if (sensorQueryStr != null)
							{
								sensorTinyDBQuery = SensorQueryer.translateQuery(sensorQueryStr, sensorTinyDBQid);
								sensorQuery = new TASKQuery(sensorTinyDBQuery, sensorQueryId, sensorTableName);
								TinyDBMain.notifyAddedQuery(sensorTinyDBQuery);
								TinyDBMain.network.addResultListener(this, true, sensorTinyDBQid);
							}
						if (healthQueryStr != null)
							{
								healthTinyDBQuery = SensorQueryer.translateQuery(healthQueryStr, healthTinyDBQid);
								healthQuery = new TASKQuery(healthTinyDBQuery, healthQueryId, healthTableName);
								TinyDBMain.notifyAddedQuery(healthTinyDBQuery);
								TinyDBMain.network.addResultListener(this, true, healthTinyDBQid);
							}
					}
				catch (Exception e)
					{
						if (ps != null)
							{
								try
									{
										ps.close();
									}
								catch (Exception e1)
									{
									}
							}
						e.printStackTrace();
					}
			}
	}

	public TASKServer()
	{
		calibQueryInProgress = false;
	}

    private static void initDBConn()
    {
		try 
			{
				urlPSQL = "jdbc:postgresql://" + Config.getParam("postgres-host") + "/" + Config.getParam("postgres-db");
				dbUser = Config.getParam("postgres-user");
				dbPwd = Config.getParam("postgres-passwd");
				System.out.println("urlPSQL = " + urlPSQL + ", dbUser = " + dbUser + ", dbPwd = " + dbPwd);
				Class.forName ( "org.postgresql.Driver" );
				dbConn = DriverManager.getConnection(urlPSQL, dbUser, dbPwd);
				dbStmt = dbConn.createStatement();
				dbStmt.setQueryTimeout(5);
				if (TinyDBMain.debug) System.out.println("connected to " + urlPSQL);
			} 
		catch (Exception ex) 
			{
				System.out.println("failed to connect to Postgres!\n");
				ex.printStackTrace();
			}
		if (TinyDBMain.debug) System.out.println("Connected to Postgres!\n");
    }

	private static void prefetchMetaData()
	{
		attributeInfos = new Vector();
		commandInfos = new Vector();
		aggregateInfos = new Vector();
		try
			{
				ResultSet rs = dbStmt.executeQuery("SELECT name, typeid, power_cons, description FROM task_attributes");
				TASKAttributeInfo attrInfo;
				while (rs.next())
					{
						attrInfo = new TASKAttributeInfo(rs.getString(1),
														 rs.getInt(2),
														 rs.getInt(3),
														 rs.getString(4));
						attributeInfos.add(attrInfo);
					}
				rs.close();
				rs = dbStmt.executeQuery("SELECT name, return_type, num_args, arg_types, description FROM task_commands");
				TASKCommandInfo cmdInfo;
				while (rs.next())
					{
						cmdInfo = new TASKCommandInfo(rs.getString(1),
													  rs.getInt(2),
													  getIntArray(rs.getString(4)),
													  rs.getString(5));
						commandInfos.add(cmdInfo);
					}
				rs.close();
				rs = dbStmt.executeQuery("SELECT name, return_type, num_args, arg_type, description FROM task_aggregates");
				TASKAggInfo aggInfo;
				while (rs.next())
					{
						aggInfo = new TASKAggInfo(rs.getString(1),
												  rs.getInt(2),
												  rs.getInt(3) - 1,
												  rs.getInt(4),
												  rs.getString(5));
						aggregateInfos.add(aggInfo);
					}
				rs.close();
			}
		catch (Exception e)
			{
				e.printStackTrace();
			}
	}

	/**
	 * convert a string representation of a PostgreSQL array
	 * to an int[]
	 */
	public static int[] getIntArray(String pgArrayStr)
	{
		Vector v = new Vector();
		int curPos = 1;
		int endPos;
		int[] ret;
		while ((endPos = pgArrayStr.indexOf(',', curPos)) != -1)
			{
				v.add(Integer.valueOf(pgArrayStr.substring(curPos, endPos)));
				for (curPos = endPos + 1; pgArrayStr.charAt(curPos) == ' ';
					 curPos++);
			}
		String intStr = pgArrayStr.substring(curPos, pgArrayStr.length() - 1);
		if (intStr.length() > 0)
			v.add(Integer.valueOf(intStr));
		ret = new int[v.size()];
		for (int i = 0; i < v.size(); i++)
			ret[i] = ((Integer)v.elementAt(i)).intValue();
		return ret;
	}

	private int nextCommandId()
	{
		int commandId = -1;
		PreparedStatement ps = null;
		try
			{
				dbConn.setAutoCommit(false);
				ResultSet rs = dbStmt.executeQuery("SELECT command_id FROM task_next_query_id");
				if (rs.next())
					{
						commandId = rs.getInt(1);
					}
				rs.close();
				ps = dbConn.prepareStatement("UPDATE task_next_query_id SET command_id = command_id + 1");
				ps.executeUpdate();
				ps.close();
				dbConn.commit();
				dbConn.setAutoCommit(true);
			}
		catch (Exception e)
			{
				try
					{
						dbConn.setAutoCommit(true);
						ps.close();
					}
				catch (Exception e1)
					{
					}
				e.printStackTrace();
			}
		return commandId;
	}

	private QueryIds nextQueryId()
	{
		int queryId = TASKQuery.INVALID_QUERYID;
		QueryIds qids = null;
		short tinyDBQid = -1;
		PreparedStatement ps = null;
		try
			{
				dbConn.setAutoCommit(false);
				ResultSet rs = dbStmt.executeQuery("SELECT query_id, tinydb_qid FROM task_next_query_id");
				if (rs.next())
					{
						queryId = rs.getInt(1);
						tinyDBQid = rs.getShort(2);
					}
				rs.close();
				qids = new QueryIds(queryId, (byte)tinyDBQid);
				tinyDBQid = (short)((tinyDBQid + 1) & (short)0x7F); // truncate to 1 byte
				ps = dbConn.prepareStatement("UPDATE task_next_query_id SET query_id = query_id + 1, tinydb_qid = ?");
				ps.setShort(1, tinyDBQid);
				ps.executeUpdate();
				ps.close();
				dbConn.commit();
				dbConn.setAutoCommit(true);
			}
		catch (Exception e)
			{
				try
					{
						dbConn.setAutoCommit(true);
						ps.close();
					}
				catch (Exception e1)
					{
					}
				e.printStackTrace();
			}
		return qids;
	}

	public static int getServerPort()
	{
		return serverPort;
	}

	public TASKQuery getTASKQuery(short whichQuery) 
	{
		if (whichQuery == SENSOR_QUERY)
		    return sensorQuery;
		else
			return healthQuery;
	}

	public int runTASKQuery(TASKQuery query, short whichQuery) throws IOException 
	{
		String queryStr;
		TinyDBQuery tinyDBQuery = null;
		PreparedStatement ps = null;
		String tableName = null;
		int queryId = query.getQueryId();
		boolean isNewQuery = (queryId == TASKQuery.INVALID_QUERYID);
		byte tinyDBQid;
		int error = TASKError.SUCCESS;

		if (isNewQuery) {
			QueryIds qids;
			// XXX should try to unify TASKQuery and TinyDBQuery
			queryStr = query.toSQL();
			System.out.println("TASKServer got new query: " + queryStr);
			qids = nextQueryId();
			queryId = qids.queryId;
			tinyDBQid = qids.tinyDBQid;
			System.out.println("queryId = " + qids.queryId + " tinyDBQid = " + qids.tinyDBQid);
			try 
				{
					tinyDBQuery = SensorQueryer.translateQuery(queryStr, tinyDBQid);
				}
			catch(Exception e) 
				{
					e.printStackTrace();
					error = TASKError.INVALID_QUERY;
					return error;
				}
			query.setQueryId(queryId);
			query.setTinyDBQid(tinyDBQid);
			if (whichQuery == SENSOR_QUERY) {
				sensorQuery = query;
				sensorTinyDBQuery = tinyDBQuery;
			}
			else {
				healthQuery = query;
				healthTinyDBQuery = tinyDBQuery;
			}
			if (query.getTableName() == null) {
				tableName = "query" + queryId + "_results";
				query.setTableName(tableName);
			}
			else {
				tableName = query.getTableName();
			}
			try
				{
					dbConn.setAutoCommit(false);
					ps = dbConn.prepareStatement("INSERT INTO task_query_log VALUES (?, ?, ?, ?, ?)");
					ps.setInt(1, queryId);
					ps.setShort(2, tinyDBQid);
					ps.setString(3, queryStr);
					ps.setString(4, whichQuery == SENSOR_QUERY ? "sensor" : "health");
					ps.setString(5, tableName);
					ps.executeUpdate();
					ps.close();
					ps = dbConn.prepareStatement("INSERT INTO task_query_time_log (query_id, start_time) VALUES (?, now())");
					ps.setInt(1, queryId);
					ps.executeUpdate();
					ps.close();
					String createTableStmt = 
						DBLogger.createTableStmt(tinyDBQuery, tableName);
					dbStmt.executeUpdate(createTableStmt);
					dbConn.commit();
					dbConn.setAutoCommit(true);
					dbStmt.executeUpdate("DROP VIEW task_current_results");
					dbStmt.executeUpdate("CREATE OR REPLACE VIEW task_current_results AS SELECT * FROM " + tableName);
					error = TASKError.SUCCESS;
				}
			catch (SQLException e)
				{
					try
						{
							dbConn.setAutoCommit(true);
							ps.close();
						}
					catch (Exception e1)
						{
						}
					error = TASKError.FAIL;
					e.printStackTrace();
				}

			// must inject query after result table is created
			try 
				{
					TinyDBMain.injectQuery(tinyDBQuery,this);
				}
			catch(Exception e) 
				{
					e.printStackTrace();
					error = TASKError.TINYOS_MESSAGE_SEND_FAILED;
				}

		}
		else {
			System.out.println("TASKServer got existing query, id = " + queryId);
			if (whichQuery == SENSOR_QUERY && 
				sensorQuery.getQueryId() != queryId ||
				whichQuery == HEALTH_QUERY &&
				healthQuery.getQueryId() != queryId) {
				error = TASKError.STALE_QUERY;
				return error;
			}
			if (whichQuery == SENSOR_QUERY)
				tinyDBQuery = sensorTinyDBQuery;
			else
				tinyDBQuery = healthTinyDBQuery;
			TinyDBMain.network.sendQuery(tinyDBQuery);
			try
				{
					ps = dbConn.prepareStatement("UPDATE task_query_time_log SET stop_time = now() " +
												 "WHERE query_id = ? AND stop_time IS NULL");
					ps.setInt(1, queryId);
					ps.executeUpdate();
					ps.close();
					ps = dbConn.prepareStatement("INSERT INTO task_query_time_log (query_id, start_time) VALUES (?, now())");
					ps.setInt(1, queryId);
					ps.executeUpdate();
					ps.close();
					error = TASKError.SUCCESS;
				}
			catch (SQLException e)
				{
					try
						{
							ps.close();
						}
					catch (Exception e1)
						{
						}
					error = TASKError.FAIL;
					e.printStackTrace();
				}
		}
		return error;
	}
	
	public int stopTASKQuery(short whichQuery) 
	{
		int queryId;
		TinyDBQuery q;
		
		if (whichQuery == SENSOR_QUERY)
			{
				if (sensorTinyDBQuery == null || sensorQuery == null)
					{
						return TASKError.SUCCESS;
					}
				q = sensorTinyDBQuery;
				queryId = sensorQuery.getQueryId();
			}
		else
			{
				if (healthTinyDBQuery == null || healthQuery == null)
					{
						return TASKError.SUCCESS;
					}
				q = healthTinyDBQuery;
				queryId = healthQuery.getQueryId();
			}
		boolean ok = stopRunningQuery(q,queryId);
		if (ok) {
			return TASKError.SUCCESS;
		} else {
			return TASKError.FAIL;
		}
	}

	public boolean isTASKQueryActive(short whichQuery)
	{
		int queryId;
		TinyDBQuery q;
		boolean result;
		switch (whichQuery) {
		case SENSOR_QUERY:
			{
				if (sensorTinyDBQuery == null || sensorQuery == null)
					{
						return false;
					}
				q = sensorTinyDBQuery;
				queryId = sensorQuery.getQueryId();
				break;
			}
		case HEALTH_QUERY:
			{
				if (healthTinyDBQuery == null || healthQuery == null)
					{
						return false;
					}
				q = healthTinyDBQuery;
				queryId = healthQuery.getQueryId();
				break;
			}
		default:
			return false;
		}

		result = q.active();

		return result;
	}

	public int runTASKCommand(TASKCommand command) 
	{
		TASKCommandInfo commandInfo = null;
		PreparedStatement ps = null;
		boolean found = false;
		int error = TASKError.SUCCESS;

		for (Iterator it = commandInfos.iterator(); it.hasNext(); )
			{
				commandInfo = (TASKCommandInfo)it.next();
				if (commandInfo.getCommandName().equalsIgnoreCase(command.getCommandName()))
					{
						found = true;
						break;
					}
			}

		if (!found)
			{
				error = TASKError.INVALID_COMMAND;
				return error;
			}

		Message cmdMessage = command.getTinyOSMessage(commandInfo);
		if (cmdMessage == null) 
			{
				System.out.println("Invalid command.");
				error = TASKError.INVALID_COMMAND;
				return error;
			}

		error = sendTinyOSMessage(cmdMessage);
		
		if (error != TASKError.SUCCESS) {
			return error;
		}

		try
			{
				String cmdStr = command.toString(commandInfo);

				ps = dbConn.prepareStatement("INSERT INTO task_command_log VALUES (?, now(), ?)");
				ps.setInt(1, nextCommandId());
				ps.setString(2, cmdStr);
				ps.executeUpdate();
				ps.close();
				error = TASKError.SUCCESS;
			}
		catch (SQLException e)
			{
				if (ps != null)
					{
						try
							{
								ps.close();
							}
						catch (Exception e1)
							{
							}
					}
				error = TASKError.FAIL;
				e.printStackTrace();
			}

		return error;
	}
	
	public Vector getTASKAttributes()
	{
		return attributeInfos;
	}

	public Vector getTASKCommands() 
	{
		return commandInfos;
	}

	public void scheduleTASKShutdown(int seconds) 
	{
		java.util.Date sdDate = new java.util.Date(System.currentTimeMillis() + seconds*1000);
		autoShutdownTimer = new Timer();
		autoShutdownTimer.schedule(new ShutdownServerTask(), sdDate);
		return;
	}

    /** Create the server
		Currently, only one parameter, "-sim", is understood,
		indicating that the server should run in simulation
		mode.
    */
    public static void main(String[] argv) {
		TASKServer server = new TASKServer();
		String cfgstr;
		boolean sim;
		
		System.out.println("Starting TASK Server...");
	
		if (argv.length == 1 && argv[0].equals("-sim")) 
			sim = true;
		else
			sim = false;

		try {
			TinyDBMain.simulate = sim;
			TinyDBMain.debug = true;
			TinyDBMain.initMain();
			TinyDBStatus status = new TinyDBStatus(TinyDBMain.network, TinyDBMain.mif, false);
			status.requestStatus(1000, 1);

			cfgstr = Config.getParam("task-server-port");
			if (cfgstr == null)
				serverPort = DEFAULT_SERVER_PORT;
			else
				serverPort = Integer.valueOf(cfgstr).intValue();
		   
			cfgstr = Config.getParam("task-server-autoshutdown");
			if (cfgstr !=null) {
				server.scheduleTASKShutdown(Integer.valueOf(cfgstr).intValue());
			}

			server.initDBConn();
			server.prefetchMetaData();
			server.fetchRunningQueries(status.getQueryIds());
			server.startHttpServer();
			server.doServer(serverPort); // Never returns
		} catch (Exception e) {
			System.out.println("SERVER ERROR: " + e);
			e.printStackTrace();
		}
    }

	class ShutdownServerTask extends TimerTask 
	{
		public void run() 
		{
			try
				{
					httpServer.stop();
				}
			catch (Exception e)
				{
				}

			try
				{
					dbConn.close();
				}
			catch (Exception e)
				{
				}

			System.exit(0);
		}
	}

    private Hashtable listeners = new Hashtable();
	static private String urlPSQL;
	static private String dbUser;
	static private String dbPwd;
	static private Connection dbConn = null;
	static private Statement dbStmt = null;
	static private int serverPort;
	private TASKQuery sensorQuery;
	private TinyDBQuery sensorTinyDBQuery;
	private TASKQuery healthQuery;
	private TinyDBQuery healthTinyDBQuery;
	private TinyDBQuery calibTinyDBQuery = null;
	private boolean calibQueryInProgress = false;
	private Vector calibNodes;
	static private Vector attributeInfos;
	static private Vector commandInfos;
	static private Vector aggregateInfos;
	private Server httpServer = null;
	private Timer autoShutdownTimer;
}

/** Internal class that's responsible for enqueing incoming connections */
class SocketLoop implements Runnable 
{
    Vector connections; //note that vector is synchronized, which allows
	//us to get away with all sorts of things that look non-thread safe
    ServerSocket ss;

	public SocketLoop(ServerSocket ss) 
	{
		Thread t = new Thread(this);
		connections = new Vector();
		this.ss = ss;
		t.start();
	}

	public void run() 
	{
		Socket sock;
		int numConnections = 0;
		long lastTime = System.currentTimeMillis();
		int lastNumConnections = 0;
		while (true) 
			{
				try 
					{
						lastTime = System.currentTimeMillis();
						lastNumConnections = connections.size();
						sock = ss.accept();
						if (connections.size() > TASKServer.MAX_CLIENT_CONNECTIONS) 
							{
								System.out.println("Accepting And Closing Connetction");
								sock.close();
							}
						else 
							{
								System.out.println("Adding new connection");
								connections.addElement(sock);
							}
					} 
				catch (java.io.InterruptedIOException e) 
					{
						//just finished listening
					}
				catch (Exception e) 
					{
						System.out.println("Error in accepting connection: " + e);
					}
			}
	}

	public Socket getConnection() 
	{
		Socket sock;
		//loop forever
		int i=0;
		while (connections.size() == 0) 
			{
				i++;
				try 
					{
						Thread.currentThread().sleep(10);
					} 
				catch (Exception e) 
					{
						//who cares?
					}
				if (i%1000==0) 
					System.err.print(".");
			}
		sock = (Socket)connections.elementAt(0);
		connections.removeElementAt(0);
		return sock;
	}
}

class QueryIds
{
	public QueryIds(int queryId, byte tinyDBQid)
	{
		this.queryId = queryId;
		this.tinyDBQid = tinyDBQid;
	}
	public int		queryId;
	public byte		tinyDBQid;
}
