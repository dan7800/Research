/*
 * Copyright (c) 2007
 *	The President and Fellows of Harvard College.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.util.*;
import net.tinyos.message.Message;
import java.util.Collections.*;
import net.tinyos.message.MoteIF;
import java.io.Serializable;

interface RequestListener
{
    /**
     * This method is called to signal that the request completed.
     * @param isSuccessful - true if the request completed successfully; false otherwise
     */
    public void requestDone(Request request, boolean isSuccessful);
    public void percentCompletedChanged(Request request, double percentCompleted);
}


class RequestScheduler implements RequestListener
{
    // =========================== Data members ================================
    private SpauldingApp spauldingApp;
    private List<Request> requestQueue = new Vector<Request>();
    private Request runningRequest = null;

    // Constants
    public static final int MAX_REQUEST_QUEUE_SIZE = 32;

    // =========================== Methods =====================================
    RequestScheduler(final SpauldingApp spauldingApp)
    {
        this.spauldingApp = spauldingApp;
    }

    synchronized public boolean scheduleRequest(Request newRequest, boolean highPriority)
    {
        if (requestQueue.size() < MAX_REQUEST_QUEUE_SIZE) {
            if (highPriority)
                requestQueue.add(0, newRequest);
            else
                requestQueue.add(newRequest);

            if (runningRequest == null)
                runNextRequest();
            return true;
        }
        else
            return false;
    }

    synchronized public boolean scheduleRequest(Request newRequest)
    {
        return scheduleRequest(newRequest, false);
    }

    synchronized public void requestDone(Request request, boolean isSuccessful)
    {
        //System.err.println("Request done, isSuccessful= " + isSuccessful);
        runningRequest = null;
        runNextRequest();
    }

    public void percentCompletedChanged(Request request, double percentCompleted)
    {  // just ignore
    }

    synchronized private void runNextRequest()
    {
        assert (runningRequest == null);

        if (requestQueue.size() > 0) {
            runningRequest = requestQueue.remove(0);
            runningRequest.registerListener(this);
            runningRequest.start();

            // update the views
          /*  try {
                spauldingApp.gui.systemStatusPanel.update();
            } catch (Exception e) {
                // Ignore
            }*/
        }
    }

    synchronized public boolean isMessageHandled(Message msg)
    {
        if (runningRequest != null)
            return runningRequest.isMessageHandled(msg);
        else
            return false;
    }

    public int getQueueSize() {return requestQueue.size();}

//    public int getRunningRequestType()
//    {
//        if (runningRequest == null)
//            return Request.NONE;
//        else
//            return runningRequest.getType();
//    }

    public String toString() {
//        String reqTypeStr = "";
//
//        switch (getRunningRequestType()) {
//            case Request.NONE:
//                reqTypeStr += "NONE";
//                break;
//            case Request.PING:
//                reqTypeStr += "PING, " + ((int) runningRequest.getPercentDone()) + "%";
//                break;
//            default:
//                reqTypeStr += "UNKNOWN!!!";
//        }
//
//        return reqTypeStr + ", qlen: " + requestQueue.size();
        return "REQUEST.TYPE";
    }

}


abstract class Request extends Thread implements Serializable
{
    // =========================== Data Members ================================
    private Vector<RequestListener> listeners = new Vector<RequestListener>();

    public enum Type {
        UPDATE_STATUS,
        START_SAMPLING,
        STOP_SAMPLING,
        DOWNLOAD,
        RESET_DATASTORE,
        INVALID
    }

    // =========================== Methods ================================
    public void start()                       {super.start();}
    abstract public Type getType();
    abstract public int getDestAddr();
    abstract public double getPercentDone();
    abstract public boolean isMessageHandled(Message msg);
    synchronized public void registerListener(RequestListener requestListener)
    {
        assert (requestListener != null);
        listeners.add(requestListener);
    }

    synchronized protected void signalRequestDone(boolean isSuccessful)
    {
        for (RequestListener rl: listeners)
            rl.requestDone(this, isSuccessful);
    }

    synchronized protected void signalPercentCompletedChanged()
    {
        for (RequestListener rl: listeners)
            rl.percentCompletedChanged(this, getPercentDone());
    }
}


class AckRequest extends Request
{
    // =========================== Data members ================================
    private SpauldingApp spauldingApp;
    private Node node;

    private RequestMsg requestMsg;
    private int nbrTransmits = 0;
    private boolean receivedReply = false;

    // Constants
    public static final int MAX_NBR_TRANSMITS = 5;
    public static final int RETRANSMIT_TIMEOUT = 750;


    // =========================== Methods =====================================
    AckRequest(SpauldingApp spauldingApp, Node node, int requestType)
    {
        assert (spauldingApp != null && node != null);
        this.spauldingApp = spauldingApp;
        this.node = node;

        // (1) - Create the request msg
        requestMsg = new RequestMsg();
        requestMsg.set_srcAddr(spauldingApp.BASE_MOTE_SRCADDR);

        // make sure we have a valid requestType
        assert (requestType == DriverMsgs.REQUESTMSG_TYPE_STATUS ||
                requestType == DriverMsgs.REQUESTMSG_TYPE_STARTSAMPLING ||
                requestType == DriverMsgs.REQUESTMSG_TYPE_STOPSAMPLING ||
                requestType == DriverMsgs.REQUESTMSG_TYPE_RESETDATASTORE);
        requestMsg.set_type(requestType);
        signalPercentCompletedChanged();
    }

    // ------------- Request super ---------------------------------------------
    public Type getType() {
        switch (requestMsg.get_type()) {
            case DriverMsgs.REQUESTMSG_TYPE_STATUS:
                return Type.UPDATE_STATUS;
            case DriverMsgs.REQUESTMSG_TYPE_STARTSAMPLING:
                return Type.START_SAMPLING;
            case DriverMsgs.REQUESTMSG_TYPE_STOPSAMPLING:
                return Type.STOP_SAMPLING;
            case DriverMsgs.REQUESTMSG_TYPE_RESETDATASTORE:
                return Type.RESET_DATASTORE;
            default:
                assert (false);
                return Type.INVALID;
        }
    }

    public int getDestAddr()        {return node.getNodeID();}
    public double getPercentDone()
    {
        if (this.receivedReply == true || nbrTransmits > MAX_NBR_TRANSMITS)
            return 100.0;
        else
            return 0.0;
    }

    public boolean isMessageHandled(Message msg)
    {
        if (msg instanceof ReplyMsg) {
            ReplyMsg rm = (ReplyMsg) msg;
            if (rm.get_srcAddr() == node.getNodeID()) {
                newReplyMsg(rm);
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
    // -------------------------------------------------------------------------

    synchronized private void newReplyMsg(ReplyMsg rm)
    {
        // (1) - Update the node's state
        node.addReplyMsg(rm);

        // (2) - Check if it was successful
        if ((rm.get_type() == DriverMsgs.REPLYMSG_TYPE_STATUS) ||
            (requestMsg.get_type() == DriverMsgs.REQUESTMSG_TYPE_STARTSAMPLING && isSampling(rm)) ||
            (requestMsg.get_type() == DriverMsgs.REQUESTMSG_TYPE_STOPSAMPLING && !isSampling(rm)) ||
            (requestMsg.get_type() == DriverMsgs.REQUESTMSG_TYPE_RESETDATASTORE && rm.get_data_status_headBlockID() <= 1)) {
            receivedReply = true;
            this.notifyAll();
        }
    }

    private boolean isSampling(ReplyMsg rm)
    {
        int bitmask = 0;
        bitmask |= (1 << DriverMsgs.SYSTEM_STATUS_BIT_ISSAMPLING);
        if ( (rm.get_data_status_systemStatus() & bitmask) > 0 )
            return true;
        else
            return false;
    }

    private void sendMsg()
    {
        // (1) - Send the message
        try {
            spauldingApp.println("AckRequest.sendMsg() - sending request to nodeID= " + node.getNodeID() + " ...");
            this.nbrTransmits++;
            //spauldingApp.eventLogger.writeMsgSentPing(fetchReqMsg);
            spauldingApp.getMoteIF().send(node.getNodeID(), requestMsg);
        } catch (Exception e) {
            spauldingApp.println("ERROR: Can't send message: " + e);
            e.printStackTrace();
        }
    }

    private int currRetransmitTimeout()
    {
        // 1.5^nbrRetransmits * RETRANSMIT_TIMEOUT
        return (int)Math.round(Math.pow(1.5, (double)nbrTransmits) *  (double)RETRANSMIT_TIMEOUT);
    }

    public void run()
    {
        synchronized (this) {
            signalPercentCompletedChanged();
            while (true) {
                // (1) - Check if we received a reply
                if (receivedReply == true) {
                    spauldingApp.println("AckRequest.run() - successfully acked");
                    //node.setCurrState(node.STATE_IDLE);
                    signalPercentCompletedChanged();
                    signalRequestDone(true);
                    break;
                }
                // (2) - Check if we timed out
                else if (nbrTransmits > MAX_NBR_TRANSMITS) {
                    spauldingApp.println("AckRequest.run() - request FAILED (timeout)");
                    //node.setCurrState(node.STATE_IDLE);
                    signalPercentCompletedChanged();
                    signalRequestDone(false);
                    break;
                }
                // (3) - Send a ping message
                else {
                    sendMsg();
                    try {
                        this.wait(currRetransmitTimeout());
                    } catch (InterruptedException ie) {
                        System.err.println("AckRequest.run.thread.wait() - error + e");
                    }
                }
            }
        }
    }
}

class FetchRequest extends Request
{
    // =========================== Data members ================================
    private SpauldingApp spauldingApp;
    private FetchLogger logger;
    private Node node;

    private long startBlockID = 0;
    private int nbrBlocksToFetch = 0;
    private int nbrBlocksSuccessfullyFetched = 0;

    // The state of the current block (i.e. the one being downloaded)
    private Block currBlock;
    private int currBitmask = 0;       // of the needed segments for currBlockID
    private int currNbrTransmits = 0;  // of currBlock

    // Constants
    public static final int MAX_NBR_TRANSMITS_PER_BLOCK = 5;
    public static final int RETRANSMIT_TIMEOUT = 750;

    // moved temporarily here for debugging
    FetchRequestMsg fetchReqMsg = null;

    // =========================== Methods =====================================
    FetchRequest(SpauldingApp spauldingApp, Node node, long startBlockID, int nbrBlocksToFetch,
                 FetchLogger logger)
    {
        assert (spauldingApp != null && node != null && logger != null);
        assert (startBlockID >= 0 && nbrBlocksToFetch >= 0);
        this.spauldingApp = spauldingApp;
        this.logger = logger;
        this.node = node;
        this.startBlockID = startBlockID;
        this.nbrBlocksToFetch = nbrBlocksToFetch;
        this.nbrBlocksSuccessfullyFetched = 0;

        // Set the state of the current block
        synchronized (this) {
            this.currBlock = new Block(node, startBlockID);
            resetCurrBitmask();
            this.currNbrTransmits = 0;
            signalPercentCompletedChanged();
        }
    }

    // ------------- Request super ---------------------------------------------
    public Type getType()             {return Type.DOWNLOAD;}
    public int getDestAddr()          {return node.getNodeID();}
    public double getPercentDone()
    {
        if (isLastBlock(currBlock.getBlockID()) && this.currBitmask == 0)
            return 100.0;
        else
            return 100.0 * ((double) (currBlock.getBlockID()-startBlockID) / (double) nbrBlocksToFetch);
    }

    public boolean isMessageHandled(Message msg)
    {
        if (msg instanceof FetchReplyMsg) {
            FetchReplyMsg frm = (FetchReplyMsg) msg;
            if (frm.get_originaddr() == node.getNodeID()) {
                newFetchReplyMsg(frm);
                return true;
            }
            else {
                System.err.println("==> frm.get_originaddr()= " + frm.get_originaddr() +
                                   ", node.getNodeID()" + node.getNodeID());
                return false;
            }
        }
        else {
            // Must be a StatusMsg -- do nothing
            // for debugging
            /*if (fetchReqMsg != null) {
                System.err.println("==> WRONG INSTANCE");
                System.err.println(msg);
                System.err.println("Expecting reply for msg:\n");
                System.err.println(fetchReqMsg);
            }*/
            return false;
        }

    }
    // -------------------------------------------------------------------------

    public void newFetchReplyMsg(FetchReplyMsg frm)
    {
        // (1) - Unpack the msg
        int recvNodeID = frm.get_originaddr();
        long recvBlockID = frm.get_block_id();
        int recvOffset = frm.get_offset();
        short[] recvData = frm.get_data();

        // Extract the received bitmask
        int offsetBitIndex = recvOffset / FetchMsgs.FETCH_SEGMENT_SIZE;
        int recvBitmask = (0x1 << offsetBitIndex);

        spauldingApp.println("FetchRequest.newFetchReplyMsg() - nodeID= " + recvNodeID +
                             ", blockID= " + recvBlockID + ", offset= " + recvOffset);

        // (2) - Process the msg
        synchronized (this) {
            // a) Drop bad packets or redundant segments
            if (recvBlockID != currBlock.getBlockID())
                spauldingApp.println("    - WARNING, bad blockID= " + recvBlockID + ", expecting= " + currBlock.getBlockID());
            else if ((recvOffset % FetchMsgs.FETCH_SEGMENT_SIZE) != 0)
                spauldingApp.println("    - WARNING, bad offset= " + recvOffset + ", not a multiple of " + FetchMsgs.FETCH_SEGMENT_SIZE);
            else if (recvData.length != FetchMsgs.FETCH_SEGMENT_SIZE)
                spauldingApp.println("    - WARNING, bad segment size= " + recvData.length + ", should be= " + FetchMsgs.FETCH_SEGMENT_SIZE);
            else if ((currBitmask & recvBitmask) == 0)
                return; // redundant segment, just drop it
            else {
                // We received a good segment that we need
                // 1) Clear the currBitmask
                currBitmask &= ~recvBitmask;

                // 2) Save the received segment
                assert (currBlock.getBlockID() == recvBlockID);
                currBlock.setBlockBytes(recvOffset, recvData);


                // 3) If we received the entire block, then notify the listening thread
                if (currBitmask == 0)
                    this.notify();
            }
        }
    }


    private void resetCurrBitmask()
    {
        synchronized (this) {
            currBitmask = 0;
            for (int i = 0; i < FetchMsgs.FETCH_BLOCK_SIZE; i += FetchMsgs.FETCH_SEGMENT_SIZE) {
                currBitmask <<= 1;
                currBitmask |= 0x01;
            }
        }
    }

    private String bitmaskToStr(int bitmask)
    {
        String str = "";
        for (int i = 0; i < FetchMsgs.FETCH_BLOCK_SIZE/FetchMsgs.FETCH_SEGMENT_SIZE; ++i) {
            if (i != 0 && i % 4 == 0)
                str = " " + str;
            str = ((bitmask >> i) & 0x01) + str;
        }
        return str;
    }

    // Note, this should only be called from within the run() method
    private void createAndSendMsg(long blockID, int bitmask)
    {
        // (1) - Create the msg
        fetchReqMsg = new FetchRequestMsg();
        fetchReqMsg.set_srcAddr(SpauldingApp.BASE_MOTE_SRCADDR);
        fetchReqMsg.set_blockID(blockID);
        fetchReqMsg.set_bitmask(bitmask);

        // (2) - Send the msg
        try {
            spauldingApp.println("FetchRequest.sendMsg() - sending request to nodeID= " + node.getNodeID() +
                                 ", blockID= " + blockID + ", bitmask= " + bitmaskToStr(bitmask));
            this.currNbrTransmits++;
            //spauldingApp.eventLogger.writeMsgSentPing(fetchReqMsg);
            spauldingApp.getMoteIF().send(node.getNodeID(), fetchReqMsg);
        } catch (Exception e) {
            spauldingApp.println("ERROR: Can't send message: " + e);
            e.printStackTrace();
        }
    }

    private boolean isLastBlock(long blockID) { return blockID == startBlockID + nbrBlocksToFetch - 1;}

    private void printSummaryAndSignalDone()
    {
        if (nbrBlocksSuccessfullyFetched == nbrBlocksToFetch) {
            spauldingApp.println("\nFetchRequest.run() - ===> SUCCESSFULLY downloaded all " + nbrBlocksToFetch  + " blocks.");
            signalPercentCompletedChanged();
            signalRequestDone(true);
        }
        else {
            spauldingApp.println("\nFetchRequest.run() - ===> FAILED, only received " + nbrBlocksSuccessfullyFetched +
                                 " out of " + this.nbrBlocksToFetch + " blocks!");
            signalPercentCompletedChanged();
            signalRequestDone(false);
        }
    }

    private int currRetransmitTimeout()
    {
        // 1.5^nbrRetransmits * RETRANSMIT_TIMEOUT
        return (int)Math.round(Math.pow(1.5, (double)currNbrTransmits) *  (double)RETRANSMIT_TIMEOUT);
    }


    public void run()
    {
        synchronized (this) {
            logger.open();
            while (true) {
                assert (startBlockID <= currBlock.getBlockID() && currBlock.getBlockID() < startBlockID + nbrBlocksToFetch);

                // (1) - Check if this block is done (either successfully or via timeout)
                if (currBitmask == 0 || currNbrTransmits > MAX_NBR_TRANSMITS_PER_BLOCK) {

                    if (currBitmask == 0) {
                        nbrBlocksSuccessfullyFetched++;
                        logger.logBlock(currBlock);  // only log successfull blocks
                    }

                    // a) Check if this was the last block
                    if ( isLastBlock(currBlock.getBlockID()) ) {
                        printSummaryAndSignalDone();
                        break;
                    }
                    // b) It wasn't, so set currBlock to the next one
                    else {
                        currBlock = new Block(node, currBlock.getBlockID()+1);
                        resetCurrBitmask();
                        currNbrTransmits = 0;
                        signalPercentCompletedChanged();
                    }
                }

                // (2) - Send the FetchRequest for the currBlock
                createAndSendMsg(currBlock.getBlockID(), currBitmask);
                try {
                    this.wait(currRetransmitTimeout());
                } catch (InterruptedException ie) {
                    System.err.println("FetchRequest.run.thread.wait() - error + e");
                }
            }

            // We are done, so close the logger
            logger.close();
        }
    }
}




//
//class FetchRequest extends Request
//{
//    // =========================== Data members ================================
//    private SpauldingApp spauldingApp;
//    private Node node;
//    private FetchLogger logger;
//    private FetchRequestMsg fetchReqMsg;
//
//    // The Fetch state
//    private Map<Long, Block> recvBlocks = Collections.synchronizedMap(new HashMap<Long, Block>());
//    private int nbrTransmits = 0;
//    private long fetchBlockID;
//    private long fetchBitmask, resetBitmask;
////    private int fetchNumBytes;
//    private int nbrBlocksToFetch, nbrBlocksFetched;
//    private int actualFetchNumBytes, fetchNumRetry, fetchNumRedundant, fetchNumExpired;
//    private long fetchSentTime = 0;
//    private long fetchStartTime;
////    private long lastBitmask = 0;
////    private int currentFetchDelay = 1000;
//    private Vector dripDelayHistory;
//    private long justSentDripTime = 0;
//    private long totalDripDelay = 0;
//    private long totalDripCount = 0;
//    private long latestDripDelay = 1000;
//    private Vector blockDelayHistory;
//    private long latestBlockDelay = 100;
//    private long totalBlockDelay = 0;
//    private long totalBlockCount = 0;
//    private long justReceivedBlockTime = 0;
//    private boolean firstFetch = true;
//    private boolean lastBlockIn = false;
//    private long oneTimeExtraFetchWait = 0;
//    private long fetchBlockStartTime = 0;
//    private boolean receivedReply = false;
//
//    // Constants
//    public static final int MAX_NBR_TRANSMITS = 20;
//    public static final int FETCH_DELAY = 500;
//    public static final int FETCH_INITIAL_DRIP_DELAY = 500;
//    public static final int TOTAL_RETRANSMIT_TIMEOUT = 20 * 60 * 1000;
//    public static final int FETCH_PAUSE_DELAY = 500;
//    public static final int DRIP_DELAY_HISTORY_SIZE = 5;
//    public static final int BLOCK_DELAY_HISTORY_SIZE = 10;
//    public static final int FETCH_DRIP_REDUNDANT_BACKOFF = 100;
//    public static final double FETCH_DRIP_DELAY_FACTOR = 1.5;
//    public static final double FETCH_BLOCK_DELAY_FACTOR = 5;
//    public static final int FETCH_WAIT_AMOUNT = 10;
//    public static final int FETCH_MAX_DRIP_DELAY_FACTOR = 1000;
//    public static final int FETCH_MIN_DRIP_DELAY_FACTOR = 250;
//
//    // =========================== Methods =====================================
//    FetchRequest(SpauldingApp spauldingApp, Node node, FetchLogger logger,
//                 long startBlockID, int nbrBlocks)
//    {
//        assert (spauldingApp != null && node != null && logger != null);
//        this.spauldingApp = spauldingApp;
//        this.node = node;
//        this.logger = logger;
//
////        node.setCurrState(Node.STATE_FETCH_PENDING);
//        this.dripDelayHistory = new Vector(DRIP_DELAY_HISTORY_SIZE);
//        this.blockDelayHistory = new Vector(BLOCK_DELAY_HISTORY_SIZE);
//
//        // (1) - Initialize the fetch variables
//        synchronized (this) {
//            fetchStartTime = 0;
////            lastBitmask = 0;
////            currentFetchDelay = FETCH_DELAY * (node.getDepth() + 1);
//
//            // If -1 is specified as the start block, start from
//            // the (latest block - nbrBlocks).
//            if (startBlockID == -1) {
//                fetchBlockID = node.getHeadBlockID() - nbrBlocks;
//                if (fetchBlockID < 0)
//                    fetchBlockID = 0;
//            }
//            else {
//                fetchBlockID = startBlockID;
//            }
//            resetBitmask();
//
//            nbrBlocksToFetch = nbrBlocks;
//            nbrBlocksFetched = 0;
//            fetchBlockStartTime = SpauldingApp.getUTCDate().getTime();
////            fetchNumBytes = nbrBlocks * FetchMsgs.FETCH_BLOCK_SIZE;
//        }
//    }
//
//    // ------------- Request super ---------------------------------------------
//    public int getType()
//    {
//        return 0; //Request.FETCH;
//    }
//
//    public double getPercentDone()
//    {
//        return 100.0 * ((double) nbrBlocksFetched / (double) nbrBlocksToFetch);
//    }
//
//    public boolean isMessageHandled(Message msg)
//    {
//        if (msg instanceof FetchReplyMsg) {
//            FetchReplyMsg frm = (FetchReplyMsg) msg;
//            if (frm.get_originaddr() == node.getNodeID()) {
//                //System.out.println("FetchRequest.isMessagehandled() - true");
//                newFetchReplyMsg((FetchReplyMsg) msg);
//                return true;
//            }
//            else
//                return false;
//        }
//        else {
//            //System.err.println("FetchRequest.isMessagehandled() - FALSE");
//            return false;
//        }
//    }
//
//    public void cancel()
//    {
//        //super.cancel();
//        // Ensure we reset node state
//        //node.setCurrState(node.STATE_IDLE);
//        //node.resumeSampling();
//    }
//
//    // -------------------------------------------------------------------------
//
//    synchronized private void resetBitmask()
//    {
//        fetchBitmask = 0;
//        for (int i = 0; i < FetchMsgs.FETCH_BLOCK_SIZE; i += FetchMsgs.FETCH_SEGMENT_SIZE) {
//            fetchBitmask <<= 1;
//            fetchBitmask |= 0x01;
//        }
//        resetBitmask = fetchBitmask;
//        //System.out.println("resetBitmask() - resetBitmask= " + resetBitmask + " <" + toStrBitmask(resetBitmask) + ">\n" +
//        //                   "                 fetchBitmask= " + fetchBitmask + " <" + toStrBitmask(fetchBitmask) + ">");
//    }
//
//
//    /**
//     * Send a single fetch request for the current block and bitmask
//     */
//    private void sendFetchBlock()
//    {
//        //System.err.println("RequestScheduler.run() - called");
//        if (fetchBitmask != resetBitmask)
//            fetchNumRetry++;
//
//        // (2) - Create the fetch request msg
//        fetchReqMsg = new FetchRequestMsg();
//        fetchReqMsg.set_srcAddr(SpauldingApp.BASE_MOTE_SRCADDR);
//        fetchReqMsg.set_blockID(fetchBlockID);
//        fetchReqMsg.set_bitmask(fetchBitmask);
//
////        // (3) - Wrap a DripMsg around the FetchRequestMsg. This is NECESSARY for Drip to work!
////        DripMsg dripMsg = new DripMsg(DripMsg.DEFAULT_MESSAGE_SIZE + FetchRequestMsg.DEFAULT_MESSAGE_SIZE);
////        dripMsg.dataSet(fetchReqMsg.dataGet(), 0, dripMsg.offset_data(0), FetchRequestMsg.DEFAULT_MESSAGE_SIZE);
////        dripMsg.set_metadata_id(spauldingApp.AM_CMDMSG);
////        dripMsg.set_metadata_seqno((short) 0); // Always 0
////        dripMsg.set_sourceaddr(spauldingApp.DRIP_SOURCE_ADDR);
//
//        // (3) - Send the msg
//        try {
//            nbrTransmits++;
//            synchronized (this) {
//                if (this.justSentDripTime == 0) {
//                    this.justSentDripTime = SpauldingApp.getUTCDate().getTime();
//                }
//                firstFetch = true;
//                fetchSentTime = SpauldingApp.getUTCDate().getTime();
//                lastBlockIn = false;
//            }
//
//            spauldingApp.println("Sending fetch command: block " + fetchBlockID + " bitmask 0x" + Long.toHexString(fetchBitmask & 0xffffffff));
////            spauldingApp.eventLogger.writeMsgSentFetch(fetchReqMsg);
//            spauldingApp.getMoteIF().send(node.getNodeID(), fetchReqMsg);
//            receivedReply = false;
//        }
//        catch (Exception e) {
//            spauldingApp.println("Got exception sending fetch: " + e);
//            e.printStackTrace();
//        }
//    }
//
//
//    public void run()
//    {
//        synchronized (this) {
//            fetchStartTime = SpauldingApp.getUTCDate().getTime();
//            logger.open();
////            assert (node.getCurrState() == node.STATE_IDLE);
////            node.setCurrState(node.STATE_WAITING_FETCH);
//
//            while (true) {
//
////                if (this.requestCancelled) {
////                    String str = "Fetch from node " + node.getNodeID() + " cancelled by user.";
////                    spauldingApp.println(str);
////                    logger.logSummary(str);
////
////                    fetchBlockCount = 0;
////                    node.setCurrState(node.STATE_IDLE);
////                    signalRequestDone(true);
////                    break;
////                }
//
////                // Don't do anything if pause button is checked
////                while (spauldingApp.isPaused()) {
////                    try {
////                        this.wait(FETCH_PAUSE_DELAY);
////                    } catch (InterruptedException ie) {
////                        // Ignore
////                    }
////                }
//
//                // (1) - Check if we timed out
//                if (nbrTransmits > MAX_NBR_TRANSMITS ||
//                    (SpauldingApp.getUTCDate().getTime() - fetchStartTime) > TOTAL_RETRANSMIT_TIMEOUT) {
//
//                    long time = SpauldingApp.getUTCDate().getTime() - fetchStartTime;
//                    logger.logComment("Fetch timed out, fetchCount=" + nbrTransmits + ", time=" + time);
//                    spauldingApp.println("FetchRequest.run() - fetch FAILED (timeout, fetchCount=" + nbrTransmits + ", time=" + time + ")");
////                    Vector nodes = spauldingApp.getNodesSorted();
////                    this.node.setRebootState(60);
////                    RebootRequest req = new RebootRequest(spauldingApp, this.node, 60);
////                    spauldingApp.scheduleRequest(req);
////                    spauldingApp.println("Rebooting node " + this.node.getNodeID() + " in 60 seconds.");
////                    for (int i = 0; i < nodes.size(); ++i) {
////                        PingRequest pingRequest = new PingRequest(spauldingApp, (Node) nodes.get(i));
////                        spauldingApp.scheduleRequest(pingRequest);
////                    }
////                    printSummary(false);
////                    node.setCurrState(node.STATE_IDLE);
//                    signalRequestDone(false);
//                    break;
//                }
//
//                // (2) - Fetch the next segment for the current block
//                //  a) Check if we received all segments of a block
//                if (fetchBitmask == 0) {
//                    // We did, so proceed to the next block
//
//                    //  1. Log the received block
//                    Block currBlock = recvBlocks.get(fetchBlockID);
//                    assert (currBlock != null);
//                    //spauldingApp.toLogOfBlocks(currBlock.toLogString());
////                    logger.logBlock(currBlock);
//
//                    //  2. Update the state
//                    nbrTransmits = 0;
//                    firstFetch = true;
//                    long lastBlockTime = (SpauldingApp.getUTCDate().getTime() -
//                                          fetchBlockStartTime);
////                    lastBlockTime /= (this.node.getDepth() + 1);
////                    this.spauldingApp.fetchTotalScaledBlockDelay += lastBlockTime;
//                    fetchBlockStartTime = SpauldingApp.getUTCDate().getTime();
//                    fetchSentTime = 0;
//                    fetchBlockID++;
//                    nbrBlocksFetched++;
//                    resetBitmask();
//
////                    this.spauldingApp.fetchTotalNumberBlocks++;
//
//                    //  3. Update the views
//                    //     update the percent complete in the status field
////                    assert (node.getCurrState() == node.STATE_WAITING_FETCH);
////                    node.setCurrState(node.STATE_WAITING_FETCH);
//                }
//
//                // Skip blocks that have already expired from the node's
//                // memory store
//                while ((fetchBlockID < node.getTailBlockID() || fetchBlockID >= node.getHeadBlockID()) &&
//                       nbrBlocksFetched < nbrBlocksToFetch) {
//
//                    logger.logComment("Fetch for node " + node.getNodeID() + " skipping block " + fetchBlockID + ", seems to have expired (tail=" + node.getTailBlockID() + ", head=" + node.getHeadBlockID());
//
//                    spauldingApp.println("Fetch for node " + node.getNodeID() + " skipping block " + fetchBlockID + ", seems to have expired (tail=" + node.getTailBlockID() + ", head=" +
//                                           node.getHeadBlockID());
//
//                    fetchNumExpired++;
//                    fetchBlockID++;
//                    nbrBlocksFetched++;
//                }
//
//                // b) If there are more blocks to fetch, fetch them
//                if (nbrBlocksFetched < nbrBlocksToFetch) {
//
//                    long waitTime = this.latestBlockDelay;
//
//                    //if ((firstFetch) &&
//                    if ((SpauldingApp.getUTCDate().getTime() -
//                         this.fetchSentTime >= (long) (this.latestDripDelay * FETCH_DRIP_DELAY_FACTOR + oneTimeExtraFetchWait))) {
//                    //if (SpauldingApp.getUTCDate().getTime() - this.fetchSentTime >= 3000) {
////                    if (!receivedReply) {
//                        sendFetchBlock();
//                        spauldingApp.println("RETRYING FETCH : Drip Timeout");
//                        /*} else if ((!firstFetch) &&
//                                   (SpauldingApp.getUTCDate().getTime() -
//                                    this.justReceivedBlockTime >= (long) (this.latestBlockDelay * FETCH_BLOCK_DELAY_FACTOR))) {
//                            sendFetchBlock();
//                            spauldingApp.println("RETRYING FETCH : Block Timeout " + this.latestBlockDelay + " " +
//                                                   (SpauldingApp.getUTCDate().getTime() - this.justReceivedBlockTime));
//                         */
//                    }
//                    else if (!firstFetch && lastBlockIn) {
//                        sendFetchBlock();
//                        spauldingApp.println("RETRYING FETCH : Got Last Block and Others Missing");
//                    }
//                    oneTimeExtraFetchWait = 0;
//                    try {
//                        this.wait(waitTime);
//                    } catch (InterruptedException ie) {
//                        System.err.println("Fetch thread.wait() error + e");
//                    }
//                }
//                else {
//                    // Hurray!  We fetched all the blocks succesfully
//                    printSummary(true);
//                    notify();
//                    nbrBlocksFetched = 0;
////                    node.setCurrState(node.STATE_IDLE);
//                    signalRequestDone(true);
//                    break;
//                }
//            }
//
//            // Start sampling again following fetch
////            node.resumeSampling();
//            logger.close();
//        }
//    }
//
//    public void newFetchReplyMsg(FetchReplyMsg frm)
//    {
//        // (1) - Unpack the msg
//        int nodeID = frm.get_originaddr();
//        assert (nodeID == node.getNodeID()) : "invalid nodeID= " + nodeID;
//        long frmblockid = frm.get_block_id();
//        int frmoffset = frm.get_offset();
//        long lastDripDelay = 0;
//        long lastBlockDelay = 0;
//        short[] frmdata = frm.get_data();
//        boolean sawRedundantBlock = false;
//        boolean sawRedundantSegment = false;
//        boolean sawLastBlock = true;
//
//        spauldingApp.println("Fetch reply:  nodeID= " + nodeID + ", blockID= " + frmblockid + ", offset= " + frmoffset);
//
//        synchronized (this) {
//            // Sanity checks
//            if (frmblockid != fetchBlockID) {
//                spauldingApp.println("Node.newFetchReplyMsg() - WARNING, bad block ID " + frmblockid + ", expecting " + fetchBlockID);
//                sawRedundantBlock = true;
//                fetchNumRedundant++;
//            }
//            if ((frmoffset % FetchMsgs.FETCH_SEGMENT_SIZE) != 0) {
//                spauldingApp.println("Node.newFetchReplyMsg() - WARNING, block with bad offset " + frmoffset + ", not a multiple of " + FetchMsgs.FETCH_SEGMENT_SIZE);
//                return;
//            }
//            receivedReply = true;
//
//            long mask = 0x1;
//            int tmpFrmOffset = frmoffset;
//            int index = 0;
//            while (tmpFrmOffset > 0) {
//                mask <<= 1;
//                tmpFrmOffset -= FetchMsgs.FETCH_SEGMENT_SIZE;
//                index++;
//            }
//            index++;
//            if (!sawRedundantBlock && (fetchBitmask & mask) == 0) {
//                sawRedundantSegment = true;
//            }
//            long tmpMask = mask;
//            if (!sawRedundantBlock && !sawRedundantSegment) {
//                for (; index < 8; index++) {
//                    tmpMask <<= 1;
//                    if ((fetchBitmask & tmpMask) != 0) {
//                        sawLastBlock = false;
//                    }
//                }
//            }
//            if (!sawRedundantBlock && !sawRedundantSegment && sawLastBlock) {
//                this.lastBlockIn = true;
//            }
//
//            // (2) - Update the node's state
//            if (!sawRedundantBlock && !sawRedundantSegment) {
//                firstFetch = false;
//            }
//
//            if (this.justSentDripTime != 0) {
//                lastDripDelay = SpauldingApp.getUTCDate().getTime() - justSentDripTime;
//                if (!sawRedundantBlock && !sawRedundantSegment) {
//                    if (this.dripDelayHistory.size() == DRIP_DELAY_HISTORY_SIZE) {
//                        this.dripDelayHistory.removeElementAt(0);
//                    }
//                    this.dripDelayHistory.addElement(new Long(lastDripDelay));
//                }
//                long newLatestDripDelay = 0;
//                for (int i = 0; i < this.dripDelayHistory.size(); i++) {
//                    if (sawRedundantBlock) {
//                        long cached = ((Long)this.dripDelayHistory.get(i)).longValue();
//                        cached += FETCH_DRIP_REDUNDANT_BACKOFF / this.dripDelayHistory.size();
//                        this.dripDelayHistory.setElementAt(new Long(cached), i);
//                        newLatestDripDelay += cached;
//                        oneTimeExtraFetchWait = this.latestDripDelay;
//                    }
//                    else {
//                        newLatestDripDelay += ((Long)this.dripDelayHistory.get(i)).longValue();
//                    }
//                }
//                this.latestDripDelay = newLatestDripDelay / this.dripDelayHistory.size();
//                if (this.latestDripDelay > ((this.node.getDepth() + 1) * FETCH_MAX_DRIP_DELAY_FACTOR)) {
//                    this.latestDripDelay = (this.node.getDepth() + 1) * FETCH_MAX_DRIP_DELAY_FACTOR;
//                }
//                if (this.latestDripDelay < ((this.node.getDepth() + 1) * FETCH_MIN_DRIP_DELAY_FACTOR)) {
//                    this.latestDripDelay = (this.node.getDepth() + 1) * FETCH_MIN_DRIP_DELAY_FACTOR;
//                }
//                this.totalDripDelay += lastDripDelay;
//                this.totalDripCount++;
//                if (!sawRedundantBlock && !sawRedundantSegment) {
//                    this.justSentDripTime = 0;
//                }
//            }
//            else {
//                if (!sawRedundantBlock) {
//                    lastBlockDelay = SpauldingApp.getUTCDate().getTime() -
//                                     this.justReceivedBlockTime;
//                    if (this.blockDelayHistory.size() == BLOCK_DELAY_HISTORY_SIZE) {
//                        this.blockDelayHistory.removeElementAt(0);
//                    }
//                    this.blockDelayHistory.addElement(new Long(lastBlockDelay));
//                    long newLatestBlockDelay = 0;
//                    for (int i = 0; i < this.blockDelayHistory.size(); i++) {
//                        newLatestBlockDelay += ((Long)this.blockDelayHistory.get(i)).longValue();
//                    }
//                    this.latestBlockDelay = newLatestBlockDelay / this.blockDelayHistory.size();
//                    this.totalBlockDelay += lastBlockDelay;
//                    this.totalBlockCount++;
//                }
//            }
//
//            this.justReceivedBlockTime = SpauldingApp.getUTCDate().getTime();
//            if (sawRedundantBlock) {
//                return;
//            }
//            if (sawRedundantSegment) {
//                fetchNumRedundant++;
//                return;
//            }
//
//            // a) - Save the received block segment
//            actualFetchNumBytes += FetchMsgs.FETCH_BLOCK_SIZE;
//
//            Block currBlock = (Block) recvBlocks.get(new Long(frmblockid));
//            if (currBlock == null) {
//                currBlock = new Block(node, frmblockid);
//                recvBlocks.put(new Long(frmblockid), currBlock);
//            }
//            // copy the data part
//            for (int i = 0; i < frmdata.length; ++i)
//                currBlock.blockBytes[frmoffset + i] = frmdata[i];
//
//            // Clear mask bit associated with incoming segment
//            if ((fetchBitmask & mask) == 0)
//                fetchNumRedundant++; // We weren't waiting for this segment
//            else
//                fetchBitmask &= ~mask;
//
//            notify();
//        }
//    }
//
//    private void printSummary(boolean success)
//    {
////        if (success) {
////            this.spauldingApp.fetchNumberNodesSuccess++;
////        }
////        else {
////            this.spauldingApp.fetchNumberNodesFail++;
////        }
//
//        String str = "Fetch summary (" + ((success == true) ? "SUCCESS" : "FAILED") + "): nodeID= " + node.getNodeID() +
//                     ", bytes= " + actualFetchNumBytes +
//                     ", time_ms= " + (SpauldingApp.getUTCDate().getTime() - fetchStartTime) +
//                     ", incompleteBlocks= " + (nbrBlocksToFetch - nbrBlocksFetched) +
//                     ", retries= " + fetchNumRetry +
//                     ", redundantSegments= " + fetchNumRedundant +
//                     ", expiredBlocks= " + fetchNumExpired;
//        if (totalDripCount != 0) {
//            str += ", averageDripDelay=" + (totalDripDelay / totalDripCount);
//        }
//        if (totalBlockCount != 0) {
//            str += ", averageBlockDelay=" + (totalBlockDelay / totalBlockCount);
//        }
//
//        spauldingApp.println(str);
//        logger.logSummary(str);
//    }
//}
//

