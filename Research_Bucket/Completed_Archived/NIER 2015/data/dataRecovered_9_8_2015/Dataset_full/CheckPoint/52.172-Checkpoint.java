package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class Checkpoint
{
    static void p(String s) { System.out.println(s); }

    static private PrintStream creat(String name) {
	try {
	    // Yes, this isn't as good as a real creat call. Not crucial for
	    // this app.
	    new File(name).delete();
	    return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)));
	}
	catch (Throwable t) { }
	return null;
    }

    static private boolean rename(String from, String to) {
	return !(new File(from).renameTo(new File(to)));
    }

    static private InputStream openRead(String name)
    {
	try {
	    return new BufferedInputStream(new FileInputStream(name));
	}
	catch (Throwable t) { }
	return null;
    }

    static private void checkpointTimes(int[] times, PrintStream f) {
	// Format is independent of MoteInfo.maxPeople
	f.print("  ");
	for (int i = 0; i < MoteInfo.maxPeople; i++) 
	    if (times[i] != 0)
		f.print(" " + i + " " + times[i]);
	f.println(" -1");
    }

    static private void checkpointTo(Registry db, PrintStream f) {
	MoteInfo visitor;
	MoteInfo[] localMotes = db.getMotes();

	f.println("SOCIAL checkpoint at " + System.currentTimeMillis());
	for (int i = 0; i < localMotes.length; i++)
	    if ((visitor = localMotes[i]) != null) {
		f.println("mote " + visitor.moteId +
			  " id " + visitor.localId +
			  " lsst " + visitor.lastSocialStartTime +
			  " lset " + visitor.lastSocialEndTime);
		checkpointTimes(visitor.socialTimes, f);
		checkpointTimes(visitor.lastSocialTimes, f);
	    }
    }

    static void checkpoint(Registry db) {
	PrintStream tempF = creat("checkpoint.temp");

	if (tempF != null) {
	    checkpointTo(db, tempF);
	    tempF.close();
	    if (!tempF.checkError()) {
		/*if (!rename("checkpoint.temp", "checkpoint.social"))
		  return;*/ // Doesn't work for Windoze
		new File("checkpoint.old").delete();
		if (!rename("checkpoint.social", "checkpoint.old")) {
		    if (!rename("checkpoint.temp", "checkpoint.social"))
			return;
		    rename("checkpoint.old", "checkpoint.social");
		}
		else if (!rename("checkpoint.temp", "checkpoint.social"))
		    return;
	    }
	}
	if (tempF != null)
	    tempF.close();
	new File("checkpoint.temp").delete();
	System.out.println("checkpoint failed");
    }

    static private boolean checkpointTimesFrom(int[] times, ParseStream f) throws IOException
    {
	for (;;) {
	    int localId = f.readInt();
	    if (localId == -1)
		return true;
	    if (!(localId >= 0 && localId < MoteInfo.maxPeople))
		return false;
	    int time = f.readInt();
	    if (time < 0)
		return false;
	    times[localId] = time;
	}
    }

    static private void ptimes(String s, int[] times) {
	System.out.print(s);
	checkpointTimes(times, System.out);
    }

    static private boolean checkpointFrom(Registry db, ParseStream f) {
	try {
	    String line1 = f.readLine();

	    if (!line1.startsWith("SOCIAL checkpoint at "))
		return false;

	    Vector motes = new Vector();

	    while (!f.skipWhiteSpace()) {
		if (!f.readWord().equals("mote"))
		    return false;
		int moteId = f.readInt();
		if (!f.readWord().equals("id"))
		    return false;
		int localId = f.readInt();
		if (!f.readWord().equals("lsst"))
		    return false;
		long lsst = f.readLong();
		if (!f.readWord().equals("lset"))
		    return false;
		long lset = f.readLong();

		MoteInfo m = new MoteInfo(moteId, localId);
		m.lastSocialStartTime = lsst;
		m.lastSocialEndTime = lset;
		if (!checkpointTimesFrom(m.socialTimes, f) ||
		    !checkpointTimesFrom(m.lastSocialTimes, f))
		    return false;

		motes.addElement(m);
		p("mote " + m.moteId + " id " + m.localId +
		  " lsst " + m.lastSocialStartTime +
		  " lset " + m.lastSocialEndTime);
		ptimes("sum", m.socialTimes);
		ptimes("last", m.lastSocialTimes);
	    }

	    db.setMotes(motes);
	    return true;
	}
	catch (IOException e) {
	}
	return false;
    }

    static void readCheckpoint(Registry db) {
	InputStream f = openRead("checkpoint.social");

	if (f != null) {
	    checkpointFrom(db, new ParseStream(f));
	    try { f.close(); }
	    catch (IOException e) { }
	}
    }
}
package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class Checkpoint
{
    static void p(String s) { System.out.println(s); }

    static private PrintStream creat(String name) {
	try {
	    // Yes, this isn't as good as a real creat call. Not crucial for
	    // this app.
	    new File(name).delete();
	    return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)));
	}
	catch (Throwable t) { }
	return null;
    }

    static private boolean rename(String from, String to) {
	return !(new File(from).renameTo(new File(to)));
    }

    static private InputStream openRead(String name)
    {
	try {
	    return new BufferedInputStream(new FileInputStream(name));
	}
	catch (Throwable t) { }
	return null;
    }

    static private void checkpointTimes(int[] times, PrintStream f) {
	// Format is independent of Social.MAX_LOCAL_IDS
	f.print("  ");
	for (int i = 0; i < Social.MAX_LOCAL_IDS; i++) 
	    if (times[i] != 0)
		f.print(" " + i + " " + times[i]);
	f.println(" -1");
    }

    static private void checkpointTo(Registry db, PrintStream f) {
	MoteInfo visitor;
	MoteInfo[] localMotes = db.getMotes();

	f.println("SOCIAL checkpoint at " + System.currentTimeMillis());
	for (int i = 0; i < localMotes.length; i++)
	    if ((visitor = localMotes[i]) != null) {
		f.println("mote " + visitor.moteId +
			  " id " + visitor.localId +
			  " lsst " + visitor.lastSocialStartTime +
			  " lset " + visitor.lastSocialEndTime);
		checkpointTimes(visitor.socialTimes, f);
		checkpointTimes(visitor.lastSocialTimes, f);
	    }
    }

    static void checkpoint(Registry db) {
	PrintStream tempF = creat("checkpoint.temp");

	if (tempF != null) {
	    checkpointTo(db, tempF);
	    tempF.close();
	    if (!tempF.checkError()) {
		/*if (!rename("checkpoint.temp", "checkpoint.social"))
		  return;*/ // Doesn't work for Windoze
		new File("checkpoint.old").delete();
		rename("checkpoint.social", "checkpoint.old");
		if (!rename("checkpoint.temp", "checkpoint.social"))
		    return;
		rename("checkpoint.old", "checkpoint.social");
	    }
	}
	if (tempF != null)
	    tempF.close();
	new File("checkpoint.temp").delete();
	System.out.println("checkpoint failed");
    }

    static private boolean checkpointTimesFrom(int[] times, ParseStream f) throws IOException
    {
	for (;;) {
	    int localId = f.readInt();
	    if (localId == -1)
		return true;
	    if (!(localId >= 0 && localId < Social.MAX_LOCAL_IDS))
		return false;
	    int time = f.readInt();
	    if (time < 0)
		return false;
	    times[localId] = time;
	}
    }

    static private void ptimes(String s, int[] times) {
	System.out.print(s);
	checkpointTimes(times, System.out);
    }

    static private boolean checkpointFrom(Registry db, ParseStream f) {
	try {
	    String line1 = f.readLine();

	    if (!line1.startsWith("SOCIAL checkpoint at "))
		return false;

	    Vector motes = new Vector();

	    while (!f.skipWhiteSpace()) {
		if (!f.readWord().equals("mote"))
		    return false;
		int moteId = f.readInt();
		if (!f.readWord().equals("id"))
		    return false;
		int localId = f.readInt();
		if (!f.readWord().equals("lsst"))
		    return false;
		long lsst = f.readLong();
		if (!f.readWord().equals("lset"))
		    return false;
		long lset = f.readLong();

		MoteInfo m = new MoteInfo(moteId, localId);
		m.lastSocialStartTime = lsst;
		m.lastSocialEndTime = lset;
		if (!checkpointTimesFrom(m.socialTimes, f) ||
		    !checkpointTimesFrom(m.lastSocialTimes, f))
		    return false;

		motes.addElement(m);
		p("mote " + m.moteId + " id " + m.localId +
		  " lsst " + m.lastSocialStartTime +
		  " lset " + m.lastSocialEndTime);
		ptimes("sum", m.socialTimes);
		ptimes("last", m.lastSocialTimes);
	    }

	    db.setMotes(motes);
	    return true;
	}
	catch (IOException e) {
	}
	return false;
    }

    static void readCheckpoint(Registry db) {
	InputStream f = openRead("checkpoint.social");

	if (f != null) {
	    checkpointFrom(db, new ParseStream(f));
	    try { f.close(); }
	    catch (IOException e) { }
	}
    }
}
package net.tinyos.social;

import java.util.*;
import java.io.*;
import net.tinyos.util.*;

class Checkpoint
{
    static void p(String s) { System.out.println(s); }

    static private PrintStream creat(String name) {
	try {
	    // Yes, this isn't as good as a real creat call. Not crucial for
	    // this app.
	    new File(name).delete();
	    return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)));
	}
	catch (Throwable t) { }
	return null;
    }

    static private boolean rename(String from, String to) {
	return !(new File(from).renameTo(new File(to)));
    }

    static private InputStream openRead(String name)
    {
	try {
	    return new BufferedInputStream(new FileInputStream(name));
	}
	catch (Throwable t) { }
	return null;
    }

    static private void checkpointTimes(int[] times, PrintStream f) {
	// Format is independent of MoteInfo.maxPeople
	f.print("  ");
	for (int i = 0; i < MoteInfo.maxPeople; i++) 
	    if (times[i] != 0)
		f.print(" " + i + " " + times[i]);
	f.println(" -1");
    }

    static private void checkpointTo(Registry db, PrintStream f) {
	MoteInfo visitor;
	MoteInfo[] localMotes = db.getMotes();

	f.println("SOCIAL checkpoint at " + System.currentTimeMillis());
	for (int i = 0; i < localMotes.length; i++)
	    if ((visitor = localMotes[i]) != null) {
		f.println("mote " + visitor.moteId +
			  " id " + visitor.localId +
			  " lsst " + visitor.lastSocialStartTime +
			  " lset " + visitor.lastSocialEndTime);
		checkpointTimes(visitor.socialTimes, f);
		checkpointTimes(visitor.lastSocialTimes, f);
	    }
    }

    static void checkpoint(Registry db) {
	PrintStream tempF = creat("checkpoint.temp");

	if (tempF != null) {
	    checkpointTo(db, tempF);
	    tempF.close();
	    if (!tempF.checkError()) {
		/*if (!rename("checkpoint.temp", "checkpoint.social"))
		  return;*/ // Doesn't work for Windoze
		new File("checkpoint.old").delete();
		if (!rename("checkpoint.social", "checkpoint.old")) {
		    if (!rename("checkpoint.temp", "checkpoint.social"))
			return;
		    rename("checkpoint.old", "checkpoint.social");
		}
		else if (!rename("checkpoint.temp", "checkpoint.social"))
		    return;
	    }
	}
	if (tempF != null)
	    tempF.close();
	new File("checkpoint.temp").delete();
	System.out.println("checkpoint failed");
    }

    static private boolean checkpointTimesFrom(int[] times, ParseStream f) throws IOException
    {
	for (;;) {
	    int localId = f.readInt();
	    if (localId == -1)
		return true;
	    if (!(localId >= 0 && localId < MoteInfo.maxPeople))
		return false;
	    int time = f.readInt();
	    if (time < 0)
		return false;
	    times[localId] = time;
	}
    }

    static private void ptimes(String s, int[] times) {
	System.out.print(s);
	checkpointTimes(times, System.out);
    }

    static private boolean checkpointFrom(Registry db, ParseStream f) {
	try {
	    String line1 = f.readLine();

	    if (!line1.startsWith("SOCIAL checkpoint at "))
		return false;

	    Vector motes = new Vector();

	    while (!f.skipWhiteSpace()) {
		if (!f.readWord().equals("mote"))
		    return false;
		int moteId = f.readInt();
		if (!f.readWord().equals("id"))
		    return false;
		int localId = f.readInt();
		if (!f.readWord().equals("lsst"))
		    return false;
		long lsst = f.readLong();
		if (!f.readWord().equals("lset"))
		    return false;
		long lset = f.readLong();

		MoteInfo m = new MoteInfo(moteId, localId);
		m.lastSocialStartTime = lsst;
		m.lastSocialEndTime = lset;
		if (!checkpointTimesFrom(m.socialTimes, f) ||
		    !checkpointTimesFrom(m.lastSocialTimes, f))
		    return false;

		motes.addElement(m);
		p("mote " + m.moteId + " id " + m.localId +
		  " lsst " + m.lastSocialStartTime +
		  " lset " + m.lastSocialEndTime);
		ptimes("sum", m.socialTimes);
		ptimes("last", m.lastSocialTimes);
	    }

	    db.setMotes(motes);
	    return true;
	}
	catch (IOException e) {
	}
	return false;
    }

    static void readCheckpoint(Registry db) {
	InputStream f = openRead("checkpoint.social");

	if (f != null) {
	    checkpointFrom(db, new ParseStream(f));
	    try { f.close(); }
	    catch (IOException e) { }
	}
    }
}
