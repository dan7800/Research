/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 * Limitation: only one replica per IP address. The problem is that when
 * a replica connects back to the evaluator after the replica's object
 * server was rebooted, its ReplicaMuxAddr will have changed (i.e.,
 * that contains a port number of a client socket. This change means we
 * cannot easily identify the slave. 
 * 
 * Solution would be to give it a cookie which it should present when
 * reconnecting. Doesn't solve expectNewReplica() problem. Perhaps
 * both problems can be solved by having replica include its LRID in
 * the GetObjectID request (assuming that it gets back the LRID after a reboot)
 * The evaluator knows this LRID from the "GOSClient::bind()".
 * 
 */
package vu.globe.rts.lr.replication.auto;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.UnknownHostException;

import vu.globe.idlsys.g;               // g.idl
import vu.globe.rts.comm.tcp.TcpAddress;
import vu.globe.rts.comm.idl.p2p.commErrors_illegalAddress;
import vu.globe.rts.lr.replication.std.protocol.ReplRequest;

import vu.globe.util.debug.DebugOutput;

import vu.globe.util.comm.RawCursor;
import vu.globe.util.comm.RawBasic;
import java.net.ProtocolException;


class ARReplicaRecord
{
    // Record identified by replica remote address,
    // not locus, as there may be multiple obj servs per
    // base region (the locus type currently used)
    //
    private String                	  _locus=null;
    private ARSimpleReplicaAccessPattern  _rap=null;
    private ARLRMgmtAddr                  _lrMgmtAddr=null;
    private long        _signOfLiveTime; // last time we heard from this
                                         // replica. Used to remove entries
					 // for transients that disappeared
					 // without telling the evaluator.

    // used when unmarshalling
    public ARReplicaRecord()
    {
        _rap = new ARSimpleReplicaAccessPattern();
    }

    // used for creating first copy
    public ARReplicaRecord( String Locus )
    {
        _locus = Locus;
        _rap = new ARSimpleReplicaAccessPattern();
    }

    public String getLocus()
    {
        return _locus;
    }

    public void setLocus( String Locus )
    {
        _locus = Locus;
    }

    public ARLRMgmtAddr getLRMgmtAddr()
    {
        return _lrMgmtAddr;
    }

    public void setLRMgmtAddr( ARLRMgmtAddr MA )
    {
        _lrMgmtAddr = MA;
    }

    public long getSignOfLiveTime()
    {
	return _signOfLiveTime;
    }

    public void add( ARSimpleReplicaAccessPattern RAP )
    {
	_rap.add( RAP );
	_signOfLiveTime = System.currentTimeMillis();
    }

    public ARSimpleReplicaAccessPattern getRAP()
    {
	return _rap;
    }

    // marshalling
    public void marshall( RawCursor cursor )
    {
	RawBasic.writeUnicode( cursor, _locus );
	_lrMgmtAddr.marshall( cursor );
	RawBasic.writeInt64( cursor, _signOfLiveTime );
	_rap.marshall( cursor );
    }

    public void unmarshall( RawCursor cursor ) throws ProtocolException
    {
	_locus = RawBasic.readUnicode( cursor );
	_lrMgmtAddr = new ARLRMgmtAddr();
	_lrMgmtAddr.unmarshall( cursor );
        _signOfLiveTime = RawBasic.readInt64( cursor );
	_rap = new ARSimpleReplicaAccessPattern();
	_rap.unmarshall( cursor );
    }
}



public class ARSimpleObjectAccessPattern
{
    /** 
     * Record of client accesses directly to evaluator, 
     * to be used for automatic replication
     */
    private ARSimpleReplicaAccessPattern     _evals_ap = null;
    private String			     _evals_locus = "_evals_locus";
    private ARLRMgmtAddr		     _evals_mgmtaddr = null; // fake value

    /**
     * Hashtable containing records about each slave 
     * to be used for automatic replication. 
     * Indexed by ipAddr obtained from ReplicaMuxAddr.
     */
    private Hashtable     _rrtable = null;

    /**
     * Hashtable of (IP address, MgmtAddr) pairs, identifying
     * object servers who have been asked to create a new
     * replica, but have not yet contacted the evaluator
     * to register as this new replica. (Basically, we're remembering
     * the mgmt addr here for when the server signs in.
     * 
     * TODO: replace this, for example, have replica ask its obj serv
     * for its LRID and have it send this info in the GetObjectIDRequest
     * along with its Locus and Proxy/Replica bit (SECURITY permitting)
     */
    private Hashtable	  _waitingMgmtAddresses = null;

    /**
     * The info needed by the policy to decide when to reevaluate:
     */
    private long	  _earliestStartTime = 0L; // out of all replicas
    private long	  _highestHourlyRate = 0L; // out of all replicas

    /**
      Threshold set by evaluator which determines when a hourly
      rate can be calculated. There should be a minimum of elaped
      time, otherwise the hourly rate can get extremely high. E.g.
      if you received 2 recs in 10 ms the hourly rate is 720000!
    */
    private long	  _minElapsedTime = 120 * 1000;  // 2 minutes


    // called by unmarshalling code
    public ARSimpleObjectAccessPattern()
    {
    }

    // Called at first create
    public ARSimpleObjectAccessPattern( String EvalsLocus, String EvalsMgmtAddrPart )
    {
        _rrtable = new Hashtable();
        _evals_ap = new ARSimpleReplicaAccessPattern();
	_evals_locus = EvalsLocus;
	_evals_mgmtaddr = new ARLRMgmtAddr( EvalsMgmtAddrPart, -1, -1);
	_earliestStartTime = System.currentTimeMillis();
	_waitingMgmtAddresses = new Hashtable();
    }


    // Called by evaluator to reset OAP after reeval.
    public void reset()
    {
	_earliestStartTime = System.currentTimeMillis(); // should be min of
							 // all RAPs in OAP.
	_highestHourlyRate = 0;
	_evals_ap.reset();
	// for each replica
	for (Enumeration re = _rrtable.elements(); re.hasMoreElements() ;)
	{	
	    ARReplicaRecord arrr = (ARReplicaRecord)re.nextElement();
	    ARSimpleReplicaAccessPattern rap = arrr.getRAP();
	    rap.reset();
	}
    }

    // Called by evaluator.
    public void setMinElapsedTime( long MinElapsedTime )
    {
	_minElapsedTime = MinElapsedTime;
    }

    // To handle locus changes during reboots.
    public void setEvalsLocus( String EvalsLocus )
    {
	_evals_locus = EvalsLocus;
    }



    public void logRequest( String Locus, ReplRequest Req )
    {
        _evals_ap.logRequest( Locus, Req );
	updateHighestHourlyRate( _evals_ap );
    }

    
    /**
     * Called by: ARSimpleReplicationPolicy
     */ 
    public long getEarliestStartTime()
    {
	return _earliestStartTime;
    }


    public boolean replicaExceedsHourlyMax( long MaxRequestsPerReplicaPerHour )
    {
	return _highestHourlyRate > MaxRequestsPerReplicaPerHour;
    }

  
    public Hashtable getPerLocusReport()
    {
	Hashtable table = new Hashtable();

	// for each replica
	for (Enumeration re = _rrtable.elements(); re.hasMoreElements() ;)
	{	
	    ARReplicaRecord arrr = (ARReplicaRecord)re.nextElement();
	    ARSimpleReplicaAccessPattern rap = arrr.getRAP();

	    // record in which locus the replica identified by arrr is located.
	    ARLRMgmtAddr ma = arrr.getLRMgmtAddr();
	    ARLocusInfo li = getOrCreateLocusInfo( arrr.getLocus(), table );
	    li.addReplica( ma, rap.getTotalRequests() );

	    DebugOutput.println( ARDefs.DebugLevel, "ar: replica@" + arrr.getLocus() + " got requests from following loci: " );

	    // see from what locusses it got requests from
	    for (Enumeration le = rap.getLocusses(); le.hasMoreElements() ; )
	    {
		String locus = (String)le.nextElement();
		long nreads = rap.getReads( locus );

		DebugOutput.println( ARDefs.DebugLevel, "\t" + locus + ": nreads= " + nreads ); 

		li = getOrCreateLocusInfo( locus, table );
		li.totreqs += nreads;
	    }
	}

	//
        // Do the same for the evaluating replica (e.g., the master)
	// 
	DebugOutput.println( ARDefs.DebugLevel, "ar: evaluator@" + _evals_locus + " got requests from following loci: " );
	for (Enumeration le = _evals_ap.getLocusses(); le.hasMoreElements() ; )
	{
		String locus = (String)le.nextElement();
		long nreads = _evals_ap.getReads( locus );

		DebugOutput.println( ARDefs.DebugLevel, "\t" + locus + ": nreads= " + nreads ); 

		ARLocusInfo li = getOrCreateLocusInfo( locus, table );
		li.totreqs += nreads;
	}

	// CAREFUL: say we're a replica ourselves (needed to exclude
	// our server as a candidate for a new replica if there are
	// a lot of local clients). We should, however, avoid that
	// the eval replica will be deleted!
	ARLocusInfo li = getOrCreateLocusInfo( _evals_locus, table );
	li.addReplica( _evals_mgmtaddr, _evals_ap.getTotalRequests() ); 
	li.isEvaluatorsLocus = true;
      
	return table;
    }

    /**
     * create record for locus, or add stats to record if already 
     * present
     */
    private ARLocusInfo getOrCreateLocusInfo( String Locus, Hashtable Table )
    {
        ARLocusInfo s = (ARLocusInfo)Table.get( Locus );
        if (s == null)
        {
             s = new ARLocusInfo();
             Table.put( Locus, s );
        }
        return s;
    }


    /** Record the MgmtAddr (ObjSvrAddrString + LRID) for a replica that
     * is about to be created. 
     *
     * BEWARE: CONCURRENCY RISK: if remote server notified before this call,
     * in theory it could connect to the evaluator and
     * be added to the OAP (via addReplica) before we do here. This risk is in
     * particular present if this re-evaluation code is run by
     * an object-server thread instead of a popup!
     *
     * This method is currently called before the request to the server.
     * 
     */
    public boolean expectNewReplica( ARLRMgmtAddr MA ) // MA.lrid not yet set!
    {
	//
	// Hmmm... need to correlate this call with the call to addReplica
	// BUG/HACK: match IP addresses. Can't do much else here really.
	//  
	String hostAddr = MA.objSvrAddrString.substring( 0, MA.objSvrAddrString.indexOf( ':' ));
	String ipAddrStr = "0.0.0.0";
	try
	{
	    // host id in objSvrAddrString can be a hostname i.s.o. IP addr.
	    ipAddrStr = InetAddress.getByName( hostAddr ).getHostAddress();
	    DebugOutput.println( ARDefs.DebugLevel, "ar: "+
			         "Expecting new replica from " + ipAddrStr );
	}
	catch( UnknownHostException e )
	{
	    DebugOutput.println( ARDefs.DebugLevel, "ar: "+
				 "expectNewReplica: **** ERROR: cannot resolve DNS hostname " + hostAddr );
	    return false;
	}

	_waitingMgmtAddresses.put( ipAddrStr, MA );

	// MA.lrid is set by the caller of this method.
	return true;
    }

    /**
     * New replica registers at evaluator:
     */ 
    public void addReplica( String ReplicaMuxAddr, String Locus )
    throws
    	g.stdErrors_invOp
    {
	// Match with waiting mgmt address
	String ipAddrStr = muxAddr2IPAddr( ReplicaMuxAddr );
	ARLRMgmtAddr ma = (ARLRMgmtAddr)_waitingMgmtAddresses.remove( ipAddrStr );
	if (ma == null)
	{
	    // Replica connecting that we didn't ask for,
	    // check if it is an existing replica reconnecting after
	    // a reboot of its server.
	    ARReplicaRecord arrr = (ARReplicaRecord)_rrtable.get( ipAddrStr );
	    if (arrr == null)
	    {
		// make replica go away, we don't have its mgmt address so
	        // we can't delete him (TODO: could drop connection later
                // to make him go away....)
	        throw new g.stdErrors_invOp();
	    }
	    else
	    {
		// Hmmm... the owner of the replica's server could have
		// changed the locus of his server during reboot,
		// let's update.
		arrr.setLocus( Locus );
	        DebugOutput.println( ARDefs.DebugLevel, "ar: "+
 			             "Welcoming back replica from " + 
				     ipAddrStr );
	    }
	}
	else
	{
	    // Welcome, new replica, we have been expecting you...
	    ARReplicaRecord arrr = new ARReplicaRecord( Locus );
	    arrr.setLRMgmtAddr( ma );
            _rrtable.put( ipAddrStr, arrr );
        }
    }

  
  
    public void logReplicaPattern( String ReplicaMuxAddr, ARSimpleReplicaAccessPattern RAP )
    {
	DebugOutput.println( ARDefs.DebugLevel, "ar: Received access pattern from " + ReplicaMuxAddr );

	String ipAddrStr = muxAddr2IPAddr( ReplicaMuxAddr );
        ARReplicaRecord arrr = (ARReplicaRecord)_rrtable.get( ipAddrStr );
        arrr.add( RAP );

	//
        // Recalculate info the policy needs to decide when to reevaluate
 	//
	// Check _evals_ap too?
	if (RAP.getStartTime() < _earliestStartTime)
	{
		_earliestStartTime = RAP.getStartTime();
	}
	updateHighestHourlyRate( RAP );
    }

    private void updateHighestHourlyRate( ARSimpleReplicaAccessPattern RAP )
    {
        long elapsed = System.currentTimeMillis() - RAP.getStartTime();

	DebugOutput.println( DebugOutput.DBG_DEBUGPLUS, "ar: updateHighestHourlyRate: "+
						    "elapsed= " + elapsed +
						    " minT= " + _minElapsedTime +
						    " raptot= " + RAP.getTotalRequests() );

	if (elapsed > _minElapsedTime)
	{
	    double elapsedHours = (elapsed)/(3600.0*1000.0);
	    long hourlyRate = (long)((RAP.getTotalRequests()*1.0)/elapsedHours);
	    if (hourlyRate > _highestHourlyRate)
	    {
	        DebugOutput.println( ARDefs.DebugLevel, "ar: Highest hourly rate updated to " + hourlyRate );
	        _highestHourlyRate = hourlyRate;
	    }
	}
    }

  
    public void deleteReplica( ARLRMgmtAddr MA )
    {
	// replica could either be expected or already connected
	String s = (String)_waitingMgmtAddresses.remove( MA.objSvrAddrString );
	if (s != null)
	    return; // replica was in just expected.

	// delete connected replica.
	for (Enumeration e = _rrtable.keys(); e.hasMoreElements(); )
	{
	    String rcpa = (String)e.nextElement();
	    ARReplicaRecord arrr = (ARReplicaRecord)_rrtable.get( rcpa );
	    if (arrr.getLRMgmtAddr().equals( MA ))
	    {
		_rrtable.remove( rcpa );
		break;
	    }
	}
    }



    /** See if there are (transient) replicas that we have not heard from,
      but should have because their time-to-report has elapsed.  One reason
      for being silent is they died during a reboot.  Another is that they
      did not have any clients for a long time.  In both cases they should
      be removed from the admin.  To handle the last case we'll also attempt
      to delete them (the caller of this method) will.
     */
    public ArrayList getSilentTransients( long ReplicaStartTimeThreshold )
    {
	long eventhorizon = System.currentTimeMillis() - ReplicaStartTimeThreshold;
	ArrayList deathrow = new ArrayList();

	// for each replica
	for (Enumeration re = _rrtable.elements(); re.hasMoreElements() ;)
	{	
	    ARReplicaRecord arrr = (ARReplicaRecord)re.nextElement();
	    if (arrr.getSignOfLiveTime() <= eventhorizon)
	    {
		// should have reported in!
		deathrow.add( arrr.getLRMgmtAddr() );
            }
	}
	for (int i=0; i<deathrow.size(); i++)
	{
	    ARLRMgmtAddr ma = (ARLRMgmtAddr)deathrow.get( i );
	    // remove from admin
	    deleteReplica( ma );
	}
	return deathrow;
    }

  
    //
    // Misc.
    //
    private String muxAddr2objSvrAddr( String ReplicaMuxAddr )
    {
	try
	{
	    TcpAddress ta = new TcpAddress( ReplicaMuxAddr );
	    return ta.getIpAddress()+":"+ta.getPort();
	}
	catch( commErrors_illegalAddress e )
	{
  	    DebugOutput.print( ARDefs.DebugLevel, "ar: muxAddr2objSvrAddr: **** ERROR: " );
	    DebugOutput.printException( ARDefs.DebugLevel, e );
	    return "commErrors_illegalAddress";
	}
    }

    private String muxAddr2IPAddr( String ReplicaMuxAddr )
    {
	try
	{
	    TcpAddress ta = new TcpAddress( ReplicaMuxAddr );
	    return ta.getIpAddress();
	}
	catch( commErrors_illegalAddress e )
	{
	    DebugOutput.print( ARDefs.DebugLevel, "ar: muxAddr2IPAddr: **** ERROR: " );
	    DebugOutput.printException( ARDefs.DebugLevel, e );
	    return "commErrors_illegalAddress";
	}
    }

    // marshalling
    public void marshall( RawCursor cursor )
    {
	_evals_ap.marshall( cursor );
	RawBasic.writeUnicode( cursor, _evals_locus );
	_evals_mgmtaddr.marshall( cursor );
	RawBasic.writeInt32( cursor, _rrtable.size() );
	for (Enumeration e = _rrtable.keys() ; e.hasMoreElements() ;)
	{	
	    String replCAstr = (String)e.nextElement();
	    ARReplicaRecord arrr = (ARReplicaRecord)_rrtable.get( replCAstr );
	    RawBasic.writeUnicode( cursor, replCAstr );
	    arrr.marshall( cursor );
	}
    
        /* BUG: _waitingMgmtAddresses not marshalled. Remember we're (we =
	   evaluator) preparing to become passive. Hence, I prefer that the
	   bind from the regular replica just fails, rather than having an
	   old value in the table when we're coming back up. Chances are the
	   slave failed anyway because we were going down.
         */
        //These are derived values, but keeping them is easier than
	// recalculating.
	RawBasic.writeInt64( cursor, _earliestStartTime );
	RawBasic.writeInt64( cursor, _highestHourlyRate );
        // _minElapsedTime is assumed to be reset by evaluator.
    }


    public void unmarshall( RawCursor cursor ) throws ProtocolException
    {
	_evals_ap = new ARSimpleReplicaAccessPattern();
	_evals_ap.unmarshall( cursor );
	_evals_locus = RawBasic.readUnicode( cursor );
	_evals_mgmtaddr = new ARLRMgmtAddr();
	_evals_mgmtaddr.unmarshall( cursor );
	int size = RawBasic.readInt32( cursor );
	_rrtable = new Hashtable();
	for (int i=0; i<size; i++)
	{
	    String replCAstr = RawBasic.readUnicode( cursor );
	    ARReplicaRecord arrr = new ARReplicaRecord();
	    arrr.unmarshall( cursor );
	    _rrtable.put( replCAstr, arrr );
	}
	_waitingMgmtAddresses = new Hashtable();
	_earliestStartTime = RawBasic.readInt64( cursor );
	_highestHourlyRate = RawBasic.readInt64( cursor );
    }
}
