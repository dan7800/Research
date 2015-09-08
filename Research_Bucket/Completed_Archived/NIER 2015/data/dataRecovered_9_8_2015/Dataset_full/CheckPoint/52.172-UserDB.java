package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class UserDB extends Thread implements SocialReceiver, Checkpointer
{
    static void p(String s) { System.out.println(s); }

    /* Times in ms */
    final static int checkPeriod = 1000;
    final static int idLife = 60000;
    final static int socialRequestInterval = 1800000;
    // Minimum time that must elapse between two social data reqs from the
    // same mote (supresses duplicates in response to receiving the same
    // identity message from multiple base stations)
    final static int minRequestInterval = 4000;

    DBReceiver dbListener;
    Registry db;
    Sql sql;

    UserDB()
    {
	db = new Registry(this);
	sql = new Sql();
	Checkpoint.readCheckpoint(db);
	sql.connect();
    }

    void setDBListener(DBReceiver dbl)
    {
	dbListener = dbl;
    }

    public void checkpoint()
    {
	Checkpoint.checkpoint(db);
    }

    synchronized void sendReqData(int moteId) {
	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null) {
	    p("unknown mote " + moteId);
	    return;
	}
	p("last data time for " + moteId + " is " + mote.lastSocialEndTime);
    }

    synchronized public void identityReceived(MoteIF from,
					      int moteId, int localId,
					      int seqNo,
					      int broadcastPeriod,
					      long timeInfoStarts)
    {
	long currentTime = System.currentTimeMillis();

	MoteInfo mote = db.registerMote(from, moteId, localId);
	mote.arrivalTime = currentTime;

	if ((timeInfoStarts != mote.lastSocialEndTime ||
	     mote.lastSocialEndTime * 1000 + socialRequestInterval < currentTime) &&
	    mote.lastSocialRequestTime + minRequestInterval < currentTime) {
	    mote.lastSocialRequestTime = currentTime;
	    p("AUTO social data req " + moteId + ", ct " + currentTime +
	      ", tis " + timeInfoStarts + ", lst " + mote.lastSocialEndTime);
	    from.sendReqData(moteId, mote.lastSocialEndTime);
	}

	dbListener.dbChange(db.getMotes());

	sql.writeTracking(moteId, from.id, currentTime, -1);
	sql.commit();
    }

    synchronized void timeoutDB() {
	long currentTime = System.currentTimeMillis();
	MoteInfo visitor;
	boolean change = false;
	MoteInfo[] localMotes = db.getMotes();

	for (int i = 0; i < localMotes.length; i++) 
	    if ((visitor = localMotes[i]) != null) {
		if (visitor.arrivalTime >= 0 &&
		    visitor.arrivalTime + idLife < currentTime) {
		    /* Goodbye! */
		    change = true;
		    visitor.arrivalTime = -1;
		}
	}
	if (change)
	    dbListener.dbChange(localMotes);
    }


    public void run() {
	while (true) {
	    try { sleep(checkPeriod, 0); }
	    catch (InterruptedException e) { }
	    timeoutDB();
	}
    }

    void printSocialData(MoteInfo mote)
    {
	p("Mote " + mote.moteId + "(" + mote.localId + ")" +
	  "sent social data #" + mote.socialPA.getPacketNumber() +
	  " for times from " + mote.lastSocialStartTime +
	  " to " + mote.lastSocialEndTime);

	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < MoteInfo.maxPeople ?
	    localMotes.length : MoteInfo.maxPeople;
	int[] socialTimes = mote.lastSocialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0)
		if (localMotes[i] != null)
		    p("" + localMotes[i].moteId + "(" + i + ") = " + socialTimes[i]);
		else 
		    p("UNKNOWN(" + i + ") = " + socialTimes[i]);
	p("END\n");
    }
    
    /* We received a social packet */
    synchronized public void socialDataReceived(MoteIF from, DataMsg packet)
    {
	int moteId = packet.get_moteId();

	p("social message from mote " + moteId);

	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null)
	    return;

	p("checking for complete packet");

	if (mote.socialPA.messageReceived(packet)) {
	    /* Get the whole packet */
	    SocialPacket spacket = new SocialPacket(mote.socialPA.getPacket());

	    int[] socialTimes = spacket.get_timeTogether();
	    long timeStarts = spacket.get_timeInfoStarts();
	    long timeEnds = spacket.get_timeInfoEnds();

	    if (timeEnds < mote.lastSocialEndTime) {
		p("received old data " + timeEnds +
		  " -- most recent data is " + mote.lastSocialEndTime);
		return;
	    }

	    handleSocialData(mote, timeStarts, timeEnds, socialTimes);

	    printSocialData(mote);
	}
    }

    void handleSocialData(MoteInfo mote, long tstart, long tend, int[] newTimes)
    {
	if (tstart != mote.lastSocialEndTime) {
	    // Oops: the data didn't start where the last data ended. Therefore
	    //       we should remove the last received data from the cumulative 
	    //       sum
	    // Note: tstart should == lastSocialStartTime (except for the very
	    //       first social data)
	    if (mote.lastSocialStartTime != 0 && tstart != mote.lastSocialStartTime) {
		p("BUG: inconsistent start/end times");
		p("  PC: s = " + mote.lastSocialStartTime + ", e = " + mote.lastSocialEndTime);
		p("  Mote " + mote.moteId + ": s = " + tstart + ", e = " + tend);
	    }
	    else
		p("Note: Repeated data. s = " + tstart +
		  ", e = " + tend + " PC end = " + mote.lastSocialEndTime);

	    for (int i = 0; i < MoteInfo.maxPeople; i++)
		mote.socialTimes[i] -= mote.lastSocialTimes[i];
	    
	}
	for (int i = 0; i < MoteInfo.maxPeople; i++)
	    mote.socialTimes[i] += newTimes[i];

	mote.lastSocialStartTime = tstart;
	mote.lastSocialEndTime = tend;
	mote.lastSocialTimes = newTimes;
	checkpoint();

	/* Add to DB */
	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < MoteInfo.maxPeople ?
	    localMotes.length : MoteInfo.maxPeople;
	int[] socialTimes = mote.socialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0 && localMotes[i] != null)
		sql.writeSocial(mote.moteId, localMotes[i].moteId, tend * 1000, socialTimes[i]);
	sql.commit();
    }
}
package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class UserDB extends Thread implements SocialReceiver, Checkpointer
{
    static void p(String s) { System.out.println(s); }

    /* Times in ms */
    final static int checkPeriod = 1000;
    final static int idLife = 60000;
    final static int socialRequestInterval = 1800000;
    // Minimum time that must elapse between two social data reqs from the
    // same mote (supresses duplicates in response to receiving the same
    // identity message from multiple base stations)
    final static int minRequestInterval = 4000;

    DBReceiver dbListener;
    Registry db;
    Sql sql;

    UserDB()
    {
	db = new Registry(this);
	sql = new Sql();
	Checkpoint.readCheckpoint(db);
	sql.connect();
    }

    void setDBListener(DBReceiver dbl)
    {
	dbListener = dbl;
    }

    public void checkpoint()
    {
	Checkpoint.checkpoint(db);
    }

    synchronized void sendReqData(int moteId) {
	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null) {
	    p("unknown mote " + moteId);
	    return;
	}
	p("last data time for " + moteId + " is " + mote.lastSocialEndTime);
    }

    synchronized public void identityReceived(MoteIF from,
					      int moteId, int localId,
					      int seqNo,
					      int broadcastPeriod,
					      long timeInfoStarts)
    {
	long currentTime = System.currentTimeMillis();

	MoteInfo mote = db.registerMote(from, moteId, localId);
	mote.arrivalTime = currentTime;

	if ((timeInfoStarts != mote.lastSocialEndTime ||
	     mote.lastSocialEndTime * 1000 + socialRequestInterval < currentTime) &&
	    mote.lastSocialRequestTime + minRequestInterval < currentTime) {
	    mote.lastSocialRequestTime = currentTime;
	    p("AUTO social data req " + moteId + ", ct " + currentTime +
	      ", tis " + timeInfoStarts + ", lst " + mote.lastSocialEndTime);
	    from.sendReqData(moteId, mote.lastSocialEndTime);
	}

	dbListener.dbChange(db.getMotes());

	sql.writeTracking(moteId, from.id, currentTime, -1);
	sql.commit();
    }

    synchronized void timeoutDB() {
	long currentTime = System.currentTimeMillis();
	MoteInfo visitor;
	boolean change = false;
	MoteInfo[] localMotes = db.getMotes();

	for (int i = 0; i < localMotes.length; i++) 
	    if ((visitor = localMotes[i]) != null) {
		if (visitor.arrivalTime >= 0 &&
		    visitor.arrivalTime + idLife < currentTime) {
		    /* Goodbye! */
		    change = true;
		    visitor.arrivalTime = -1;
		}
	}
	if (change)
	    dbListener.dbChange(localMotes);
    }


    public void run() {
	while (true) {
	    try { sleep(checkPeriod, 0); }
	    catch (InterruptedException e) { }
	    timeoutDB();
	}
    }

    long decodeTimeStarts(byte[] spacket)
    {
	return MoteIF.readInt(spacket, 2);
    }

    long decodeTimeEnds(byte[] spacket)
    {
	return MoteIF.readInt(spacket, 6);
    }

    int[] decodeSocialInfo(byte[] spacket)
    {
	int[] socialCounts = new int[Social.MAX_LOCAL_IDS];

	for (int i = 0; i < Social.MAX_LOCAL_IDS; i++)
	    socialCounts[i] = MoteIF.readShort(spacket, MoteIF.SSI_DATA + 2 * i);

	return socialCounts;
    }

    void printSocialData(MoteInfo mote)
    {
	p("Mote " + mote.moteId + "(" + mote.localId + ")" +
	  "sent social data #" + mote.socialPA.getPacketNumber() +
	  " for times from " + mote.lastSocialStartTime +
	  " to " + mote.lastSocialEndTime);

	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < Social.MAX_LOCAL_IDS ?
	    localMotes.length : Social.MAX_LOCAL_IDS;
	int[] socialTimes = mote.lastSocialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0)
		if (localMotes[i] != null)
		    p("" + localMotes[i].moteId + "(" + i + ") = " + socialTimes[i]);
		else 
		    p("UNKNOWN(" + i + ") = " + socialTimes[i]);
	p("END\n");
    }
    
    /* We received a social packet */
    synchronized public void socialDataReceived(MoteIF from, byte[] packet)
    {
	int moteId = MoteIF.readShort(packet, 4);

	p("social message from mote " + moteId);

	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null)
	    return;

	p("checking for complete packet");

	if (mote.socialPA.messageReceived(packet)) {
	    /* Get the whole packet */
	    packet = mote.socialPA.getPacket();

	    int[] socialTimes = decodeSocialInfo(packet);
	    long timeStarts = decodeTimeStarts(packet);
	    long timeEnds = decodeTimeEnds(packet);

	    if (timeEnds < mote.lastSocialEndTime) {
		p("received old data " + timeEnds +
		  " -- most recent data is " + mote.lastSocialEndTime);
		return;
	    }

	    handleSocialData(mote, timeStarts, timeEnds, socialTimes);

	    printSocialData(mote);
	}
    }

    void handleSocialData(MoteInfo mote, long tstart, long tend, int[] newTimes)
    {
	if (tstart != mote.lastSocialEndTime) {
	    // Oops: the data didn't start where the last data ended. Therefore
	    //       we should remove the last received data from the cumulative 
	    //       sum
	    // Note: tstart should == lastSocialStartTime (except for the very
	    //       first social data)
	    if (mote.lastSocialStartTime != 0 && tstart != mote.lastSocialStartTime) {
		p("BUG: inconsistent start/end times");
		p("  PC: s = " + mote.lastSocialStartTime + ", e = " + mote.lastSocialEndTime);
		p("  Mote " + mote.moteId + ": s = " + tstart + ", e = " + tend);
	    }
	    else
		p("Note: Repeated data. s = " + tstart +
		  ", e = " + tend + " PC end = " + mote.lastSocialEndTime);

	    for (int i = 0; i < Social.MAX_LOCAL_IDS; i++)
		mote.socialTimes[i] -= mote.lastSocialTimes[i];
	    
	}
	for (int i = 0; i < Social.MAX_LOCAL_IDS; i++)
	    mote.socialTimes[i] += newTimes[i];

	mote.lastSocialStartTime = tstart;
	mote.lastSocialEndTime = tend;
	mote.lastSocialTimes = newTimes;
	checkpoint();

	/* Add to DB */
	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < Social.MAX_LOCAL_IDS ?
	    localMotes.length : Social.MAX_LOCAL_IDS;
	int[] socialTimes = mote.socialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0 && localMotes[i] != null)
		sql.writeSocial(mote.moteId, localMotes[i].moteId, tend * 1000, socialTimes[i]);
	sql.commit();
    }
}
package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class UserDB extends Thread implements SocialReceiver, Checkpointer
{
    static void p(String s) { System.out.println(s); }

    /* Times in ms */
    final static int checkPeriod = 1000;
    final static int idLife = 60000;
    final static int socialRequestInterval = 1800000;
    // Minimum time that must elapse between two social data reqs from the
    // same mote (supresses duplicates in response to receiving the same
    // identity message from multiple base stations)
    final static int minRequestInterval = 4000;

    DBReceiver dbListener;
    Registry db;
    Sql sql;

    UserDB()
    {
	db = new Registry(this);
	sql = new Sql();
	Checkpoint.readCheckpoint(db);
	sql.connect();
    }

    void setDBListener(DBReceiver dbl)
    {
	dbListener = dbl;
    }

    public void checkpoint()
    {
	Checkpoint.checkpoint(db);
    }

    synchronized void sendReqData(int moteId) {
	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null) {
	    p("unknown mote " + moteId);
	    return;
	}
	p("last data time for " + moteId + " is " + mote.lastSocialEndTime);
    }

    synchronized public void identityReceived(MoteIF from,
					      int moteId, int localId,
					      int seqNo,
					      int broadcastPeriod,
					      long timeInfoStarts)
    {
	long currentTime = System.currentTimeMillis();

	MoteInfo mote = db.registerMote(from, moteId, localId);
	mote.arrivalTime = currentTime;

	if ((timeInfoStarts != mote.lastSocialEndTime ||
	     mote.lastSocialEndTime * 1000 + socialRequestInterval < currentTime) &&
	    mote.lastSocialRequestTime + minRequestInterval < currentTime) {
	    mote.lastSocialRequestTime = currentTime;
	    p("AUTO social data req " + moteId + ", ct " + currentTime +
	      ", tis " + timeInfoStarts + ", lst " + mote.lastSocialEndTime);
	    from.sendReqData(moteId, mote.lastSocialEndTime);
	}

	dbListener.dbChange(db.getMotes());

	sql.writeTracking(moteId, from.id, currentTime, -1);
	sql.commit();
    }

    synchronized void timeoutDB() {
	long currentTime = System.currentTimeMillis();
	MoteInfo visitor;
	boolean change = false;
	MoteInfo[] localMotes = db.getMotes();

	for (int i = 0; i < localMotes.length; i++) 
	    if ((visitor = localMotes[i]) != null) {
		if (visitor.arrivalTime >= 0 &&
		    visitor.arrivalTime + idLife < currentTime) {
		    /* Goodbye! */
		    change = true;
		    visitor.arrivalTime = -1;
		}
	}
	if (change)
	    dbListener.dbChange(localMotes);
    }


    public void run() {
	while (true) {
	    try { sleep(checkPeriod, 0); }
	    catch (InterruptedException e) { }
	    timeoutDB();
	}
    }

    int[] decodeSocialInfo(SocialPacket spacket)
    {
	int[] socialCounts = new int[MoteInfo.maxPeople];

	for (int i = 0; i < socialCounts.length; i++)
	    socialCounts[i] = spacket.getTimeTogether(i);

	return socialCounts;
    }

    void printSocialData(MoteInfo mote)
    {
	p("Mote " + mote.moteId + "(" + mote.localId + ")" +
	  "sent social data #" + mote.socialPA.getPacketNumber() +
	  " for times from " + mote.lastSocialStartTime +
	  " to " + mote.lastSocialEndTime);

	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < MoteInfo.maxPeople ?
	    localMotes.length : MoteInfo.maxPeople;
	int[] socialTimes = mote.lastSocialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0)
		if (localMotes[i] != null)
		    p("" + localMotes[i].moteId + "(" + i + ") = " + socialTimes[i]);
		else 
		    p("UNKNOWN(" + i + ") = " + socialTimes[i]);
	p("END\n");
    }
    
    /* We received a social packet */
    synchronized public void socialDataReceived(MoteIF from, DataMsg packet)
    {
	int moteId = packet.getMoteId();

	p("social message from mote " + moteId);

	MoteInfo mote = db.lookupMote(moteId);
	if (mote == null)
	    return;

	p("checking for complete packet");

	if (mote.socialPA.messageReceived(packet)) {
	    /* Get the whole packet */
	    SocialPacket spacket = new SocialPacket(mote.socialPA.getPacket());

	    int[] socialTimes = decodeSocialInfo(spacket);
	    long timeStarts = spacket.getTimeInfoStarts();
	    long timeEnds = spacket.getTimeInfoEnds();

	    if (timeEnds < mote.lastSocialEndTime) {
		p("received old data " + timeEnds +
		  " -- most recent data is " + mote.lastSocialEndTime);
		return;
	    }

	    handleSocialData(mote, timeStarts, timeEnds, socialTimes);

	    printSocialData(mote);
	}
    }

    void handleSocialData(MoteInfo mote, long tstart, long tend, int[] newTimes)
    {
	if (tstart != mote.lastSocialEndTime) {
	    // Oops: the data didn't start where the last data ended. Therefore
	    //       we should remove the last received data from the cumulative 
	    //       sum
	    // Note: tstart should == lastSocialStartTime (except for the very
	    //       first social data)
	    if (mote.lastSocialStartTime != 0 && tstart != mote.lastSocialStartTime) {
		p("BUG: inconsistent start/end times");
		p("  PC: s = " + mote.lastSocialStartTime + ", e = " + mote.lastSocialEndTime);
		p("  Mote " + mote.moteId + ": s = " + tstart + ", e = " + tend);
	    }
	    else
		p("Note: Repeated data. s = " + tstart +
		  ", e = " + tend + " PC end = " + mote.lastSocialEndTime);

	    for (int i = 0; i < MoteInfo.maxPeople; i++)
		mote.socialTimes[i] -= mote.lastSocialTimes[i];
	    
	}
	for (int i = 0; i < MoteInfo.maxPeople; i++)
	    mote.socialTimes[i] += newTimes[i];

	mote.lastSocialStartTime = tstart;
	mote.lastSocialEndTime = tend;
	mote.lastSocialTimes = newTimes;
	checkpoint();

	/* Add to DB */
	MoteInfo[] localMotes = db.getMotes();
	int length = localMotes.length < MoteInfo.maxPeople ?
	    localMotes.length : MoteInfo.maxPeople;
	int[] socialTimes = mote.socialTimes;
	for (int i = 0; i < length; i++)
	    if (socialTimes[i] > 0 && localMotes[i] != null)
		sql.writeSocial(mote.moteId, localMotes[i].moteId, tend * 1000, socialTimes[i]);
	sql.commit();
    }
}
