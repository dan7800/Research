package net.tinyos.social;

import java.util.*;
import net.tinyos.util.*;

class Registry
{
    Checkpointer checkpointer;

    int nextLocalId = 1;
    protected MoteInfo localMotes[];
    
    Registry(Checkpointer c)
    {
	localMotes = new MoteInfo[16];
	checkpointer = c;
    }

    MoteInfo[] getMotes()
    {
	return localMotes;
    }

    void setMotes(Vector motes)
    {
	Enumeration elems;
	int largestLocalId = -1;

	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();

	    if (m.localId > largestLocalId)
		largestLocalId = m.localId;
	}
	nextLocalId = largestLocalId + 1;

	localMotes = new MoteInfo[nextLocalId + 16];
	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();
	    localMotes[m.localId] = m;
	}
    }

    MoteInfo lookupMote(int moteId)
    {
	for (int i = 0; i < localMotes.length; i++)
	    if (localMotes[i] != null &&
		localMotes[i].moteId == moteId)
		return localMotes[i];
	return null;
    }

    private void doubleLocalMotes()
    {
	MoteInfo[] newMotes = new MoteInfo[2 * localMotes.length];

	for (int i = 0; i < localMotes.length; i++)
	    newMotes[i] = localMotes[i];
	localMotes = newMotes;
    }

    MoteInfo registerMote(MoteIF moteIF, int moteId, int localId)
    {
	MoteInfo mote = lookupMote(moteId);

	if (mote != null) {
	    if (localId != mote.localId) /* Node forgot its id. Remind it. */
		moteIF.sendRegister(moteId, mote.localId);
	    return mote;
	}
	/* Give node the next local id (except if that happens to match
	   its current id - we don't want that as the mote must clear its
	   state as it must be bogus).
	   This code could conceivably lead to only 50% local id usage, but
	   that's rather unlikely */
	if (nextLocalId == localId)
	    nextLocalId++;

	localId = nextLocalId++;
	mote = new MoteInfo(moteId, localId);

	/* Register mote. */
	if (localId >= localMotes.length)
	    doubleLocalMotes();
	localMotes[localId] = mote;

	checkpointer.checkpoint();
	moteIF.sendRegister(moteId, localId);

	return mote;
    }
}
package net.tinyos.social;

import java.util.*;
import net.tinyos.util.*;

class Registry
{
    Checkpointer checkpointer;

    int nextLocalId = 1;
    protected MoteInfo localMotes[];
    
    Registry(Checkpointer c)
    {
	localMotes = new MoteInfo[16];
	checkpointer = c;
    }

    MoteInfo[] getMotes()
    {
	return localMotes;
    }

    void setMotes(Vector motes)
    {
	Enumeration elems;
	int largestLocalId = -1;

	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();

	    if (m.localId > largestLocalId)
		largestLocalId = m.localId;
	}
	nextLocalId = largestLocalId + 1;

	localMotes = new MoteInfo[nextLocalId + 16];
	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();
	    localMotes[m.localId] = m;
	}
    }

    MoteInfo lookupMote(int moteId)
    {
	for (int i = 0; i < localMotes.length; i++)
	    if (localMotes[i] != null &&
		localMotes[i].moteId == moteId)
		return localMotes[i];
	return null;
    }

    private void doubleLocalMotes()
    {
	MoteInfo[] newMotes = new MoteInfo[2 * localMotes.length];

	for (int i = 0; i < localMotes.length; i++)
	    newMotes[i] = localMotes[i];
	localMotes = newMotes;
    }

    MoteInfo registerMote(MoteIF moteIF, int moteId, int localId)
    {
	MoteInfo mote = lookupMote(moteId);

	if (mote != null) {
	    if (localId != mote.localId) /* Node forgot its id. Remind it. */
		moteIF.sendRegister(moteId, mote.localId);
	    return mote;
	}
	/* Give node the next local id (except if that happens to match
	   its current id - we don't want that as the mote must clear its
	   state as it must be bogus).
	   This code could conceivably lead to only 50% local id usage, but
	   that's rather unlikely */
	if (nextLocalId == localId)
	    nextLocalId++;

	localId = nextLocalId++;
	mote = new MoteInfo(moteId, localId);

	/* Register mote. */
	if (localId >= localMotes.length)
	    doubleLocalMotes();
	localMotes[localId] = mote;

	checkpointer.checkpoint();
	moteIF.sendRegister(moteId, localId);

	return mote;
    }
}
package net.tinyos.social;

import java.util.*;
import net.tinyos.util.*;

class Registry
{
    Checkpointer checkpointer;

    int nextLocalId = 1;
    protected MoteInfo localMotes[];
    
    Registry(Checkpointer c)
    {
	localMotes = new MoteInfo[16];
	checkpointer = c;
    }

    MoteInfo[] getMotes()
    {
	return localMotes;
    }

    void setMotes(Vector motes)
    {
	Enumeration elems;
	int largestLocalId = -1;

	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();

	    if (m.localId > largestLocalId)
		largestLocalId = m.localId;
	}
	nextLocalId = largestLocalId + 1;

	localMotes = new MoteInfo[nextLocalId + 16];
	for (elems = motes.elements(); elems.hasMoreElements();) {
	    MoteInfo m = (MoteInfo)elems.nextElement();
	    localMotes[m.localId] = m;
	}
    }

    MoteInfo lookupMote(int moteId)
    {
	for (int i = 0; i < localMotes.length; i++)
	    if (localMotes[i] != null &&
		localMotes[i].moteId == moteId)
		return localMotes[i];
	return null;
    }

    private void doubleLocalMotes()
    {
	MoteInfo[] newMotes = new MoteInfo[2 * localMotes.length];

	for (int i = 0; i < localMotes.length; i++)
	    newMotes[i] = localMotes[i];
	localMotes = newMotes;
    }

    MoteInfo registerMote(MoteIF moteIF, int moteId, int localId)
    {
	MoteInfo mote = lookupMote(moteId);

	if (mote != null) {
	    if (localId != mote.localId) /* Node forgot its id. Remind it. */
		moteIF.sendRegister(moteId, mote.localId);
	    return mote;
	}
	/* Give node the next local id (except if that happens to match
	   its current id - we don't want that as the mote must clear its
	   state as it must be bogus).
	   This code could conceivably lead to only 50% local id usage, but
	   that's rather unlikely */
	if (nextLocalId == localId)
	    nextLocalId++;

	localId = nextLocalId++;
	mote = new MoteInfo(moteId, localId);

	/* Register mote. */
	if (localId >= localMotes.length)
	    doubleLocalMotes();
	localMotes[localId] = mote;

	checkpointer.checkpoint();
	moteIF.sendRegister(moteId, localId);

	return mote;
    }
}
