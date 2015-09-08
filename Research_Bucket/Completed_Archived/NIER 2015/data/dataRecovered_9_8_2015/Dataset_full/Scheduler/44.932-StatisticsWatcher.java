/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.stats;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.active.proto.ActiveTimerFactImpl;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.svcs.gls.node.recovery.Recovery;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.TimeLib;

import java.util.*;

/**
   A <code>StatisticsWatcher</code> wakes up regularly and prints some
   statistics.
 
   @author Patrick Verkaik
*/

/*
   We use the scheduler to run this object on.
   The timer_reg field is protected by a lock.
*/

public class StatisticsWatcher
{
   private Statistics stats;
   private Statistics saved_stats;  // the previous dump, stable

   private ActiveTimerFact timer_fact;
   private ActiveRID timer_reg; // set while activated

   private ActiveTimer timer;
   private long interval; // in millisecs, -1 if not watching

   private Recovery recovery;

   /**
      Constructor.
   */
   public StatisticsWatcher (ObjEnv env)
   {
      stats = env.getStatistics ();
      Scheduler sched = env.getScheduler ();
      timer_fact = new ActiveTimerFactImpl (sched, stats);

      if(env instanceof NodeObjEnv)
         recovery = ((NodeObjEnv) env).getNodeRecovery();
   }

   /**
      Turns this object on.

      @param
         the number of milliseconds between statistics analises
   */
   public void startWatching (long millis)
   {
      Notifiable note = new Notifiable () {
         public void notifyEvent (ActiveUID userInfo) {
            notifyTimerEvent (userInfo);
         }
      };

      timer_reg = timer_fact.activate (note, null);
      timer = timer_fact.createTimer (timer_reg);
      interval = millis;
      saved_stats = (Statistics) stats.clone ();
      timer.set (millis, null);
   }

   /**
      Switches this object off.
   */
   public synchronized void stopWatching ()
   {
      if (timer_reg != null) {
        timer_fact.deactivate (timer_reg);
        printStats();
        timer_reg = null;
      }
   }

   /**
      Called on timer expiry. Processes the statistics and resets the timer.
   */
   private synchronized void notifyTimerEvent (ActiveUID userInfo)
   {
      printStats();
   }

   private void printStats()
   {
      if (timer_reg == null)
         return;

      System.out.println ("statistics at " + new Date () + ":");

/*
      System.out.println ("Datagram Statistics");
      System.out.println ("   total sent:                               " +
                        formatStatistic (stats.countDatagramSent (),
                                         saved_stats.countDatagramSent ()));
      System.out.println ("   total received:                           " +
                        formatStatistic (stats.countDatagramReceived (),
                                         saved_stats.countDatagramReceived ()));
      System.out.println ("   bytes sent:                               " +
                        formatStatistic (stats.sizeDatagramSent (),
                                         saved_stats.sizeDatagramSent ()));
      System.out.println ("   bytes received:                           " +
                        formatStatistic (stats.sizeDatagramReceived (),
                                         saved_stats.sizeDatagramReceived ()));
        
      if(Debug.statsTime())
      {
         
         System.out.println ("   total datagram (un)marshall time:         " + 
                        formatTimeStatistic (stats.timeDatagramMarshallTime (),
                                 stats.timeDatagramMarshallTime ()));
         System.out.println ("   avg. datagram (un)marshall time:          " + 
                        formatTimeStatistic (stats.avgDatagramMarshallTime (),
                                 stats.avgDatagramMarshallTime ()));
      }

      System.out.println ("Message Statistics");
     
      System.out.println ("   total sent:                               " +
                        formatStatistic (stats.countMessageSent (),
                                         saved_stats.countMessageSent ()));
      System.out.println ("   total received:                           " +
                        formatStatistic (stats.countMessageReceived (),
                                         saved_stats.countMessageReceived ()));
     
      System.out.println ("   data sent:                                " + 
                        formatStatistic (stats.countDataMessageSent (),
                                         saved_stats.countDataMessageSent ()));
      System.out.println ("   data received:                            " +
                        formatStatistic (stats.countDataMessageReceived (),
                                         saved_stats.countDataMessageReceived ()));
      System.out.println ("   data retransmitted:                       " + 
                        formatStatistic (stats.countDataMessageSentRetrans (),
                                         saved_stats.countDataMessageSentRetrans ()));
      System.out.println ("   data duplicates received:                 " +
                        formatStatistic (stats.countDataMessageReceivedRetrans (),
                                         saved_stats.countDataMessageReceivedRetrans ()));
     
      System.out.println ("   acks sent:                                " +
                        formatStatistic (stats.countAckMessageSent (),
                                         saved_stats.countAckMessageSent ()));
      System.out.println ("   acks received:                            " +
                        formatStatistic (stats.countAckMessageReceived (),
                                         saved_stats.countAckMessageReceived ()));

      System.out.println ("   epochs sent:                              " +
                        formatStatistic (stats.countEpochMessageSent (),
                                         saved_stats.countEpochMessageSent ()));
      System.out.println ("   epochs received:                          " +
                        formatStatistic (stats.countEpochMessageReceived (),
                                         saved_stats.countEpochMessageReceived ()));
      System.out.println ("   epochs retransmitted:                     " +
                        formatStatistic (stats.countEpochMessageSentRetrans (),
                                         saved_stats.countEpochMessageSentRetrans ()));

      if(Debug.statsTime())
      {
         
         System.out.println ("   total epoch processing time:              " + 
                     formatTimeStatistic (stats.timeEpochProcessingTime (),
                              saved_stats.timeEpochProcessingTime ()));
         System.out.println ("   total ack sending time:                   " + 
                     formatTimeStatistic (stats.timeMessengerAckTime (),
                              saved_stats.timeMessengerAckTime ()));
         System.out.println ("   avg. ack sending time:                    " + 
                     formatTimeStatistic (stats.avgMessengerAckTime (),
                              saved_stats.avgMessengerAckTime ()));
         System.out.println ("   total messenger callback queue wait time: " + 
                     formatTime(stats.timeMessengerCallbackQueueTime ()));
      
         System.out.println ("   avg. messenger callback queue wait time:  " + 
                     formatTime(stats.avgMessengerCallbackQueueTime ()));
            
         System.out.println ("   total message (un)marshall time:          "
                     + formatTimeStatistic (stats.timeMessageMarshallTime (),
                                 stats.timeMessageMarshallTime ()));
         System.out.println ("   avg. message (un)marshall time:           "
                     + formatTimeStatistic (stats.avgMessageMarshallTime (),
                                 stats.avgMessageMarshallTime ()));
         
         System.out.println ("   total message order queue wait time:      "
                     + formatTimeStatistic (stats.timeMessageOrderQueueTime (),
                                 stats.timeMessageOrderQueueTime ()));
         System.out.println ("   avg. message order queue wait time:       "
                     + formatTimeStatistic (stats.avgMessageOrderQueueTime (),
                                 stats.avgMessageOrderQueueTime ()));


      }
*/
      if (stats instanceof NodeStatistics) 
      {

         NodeStatistics node_stats = (NodeStatistics) stats;
         NodeStatistics saved_node_stats = (NodeStatistics) saved_stats;
/*
         System.out.println ("RPC Statistics");
         System.out.println ("   total sent:                               " +
                        formatStatistic (node_stats.countRPCSent (),
                                         saved_node_stats.countRPCSent ()));
         System.out.println ("   total received:                           " +
                        formatStatistic (node_stats.countRPCReceived (),
                                         saved_node_stats.countRPCReceived ()));
         System.out.println ("   requests sent:                            " +
                        formatStatistic (node_stats.countRequestRPCSent (),
                                         saved_node_stats.countRequestRPCSent ()));
         System.out.println ("   requests received:                        " +
                        formatStatistic (node_stats.countRequestRPCReceived (),
                                         saved_node_stats.countRequestRPCReceived ()));
         System.out.println ("   requests resent:                          " +
                        formatStatistic (node_stats.countRequestRPCResent (),
                                         saved_node_stats.countRequestRPCResent ()));
         System.out.println ("   requests timed out:                       " +
                        formatStatistic (node_stats.countRequestRPCTimeout (),
                                         saved_node_stats.countRequestRPCTimeout ()));
         System.out.println ("   replies sent:                             " +
                        formatStatistic (node_stats.countReplyRPCSent (),
                                         saved_node_stats.countReplyRPCSent ()));
         System.out.println ("   replies received:                         " +
                        formatStatistic (node_stats.countReplyRPCReceived (),
                                         saved_node_stats.countReplyRPCReceived ()));
         System.out.println ("   end recoveries sent:                      " + 
                        formatStatistic (node_stats.countEndRecoveryRPCSent (),
                                         saved_node_stats.countEndRecoveryRPCSent ()));
         System.out.println ("   end recoveries received:                  " + 
                        formatStatistic (node_stats.countEndRecoveryRPCReceived (),
                                         saved_node_stats.countEndRecoveryRPCReceived ()));

         if(Debug.statsTime())
         {
            
            System.out.println ("   requests replayed from rpc log:           "
                     + formatTimeStatistic (node_stats.countReplayRpcLogRPC (),
                                 saved_node_stats.countReplayRpcLogRPC ()));
            System.out.println ("   total reply queue wait time:              "
                     + formatTime(node_stats.timeRPCReplyCallbackQueueTime ()));
            System.out.println ("   avg. reply queue wait time:               "
                     + formatTime(node_stats.avgRPCReplyCallbackQueueTime ()));
            System.out.println ("   total request queue wait time:            "
                  + formatTime(node_stats.timeRPCRequestCallbackQueueTime ()));
            System.out.println ("   avg. request queue wait time:             "
                  + formatTime(node_stats.avgRPCRequestCallbackQueueTime ()));
            System.out.println ("   mapper conversions:                       "
                     + formatTimeStatistic (node_stats.countMapperConversion (),
                                 saved_node_stats.countMapperConversion ()));
            System.out.println ("   total mapper conversion time:             "
                     + formatTimeStatistic (node_stats.timeMapperConversionTime (),
                                 saved_node_stats.timeMapperConversionTime ()));
            System.out.println ("   avg. mapper conversion time:              "
                     + formatTimeStatistic (node_stats.avgMapperConversionTime (),
                                 saved_node_stats.avgMapperConversionTime ()));
            System.out.println ("   total RPC (un)marshall time:              "
                     + formatStatistic (node_stats.timeRPCMarshallTime (),
                                 saved_node_stats.timeRPCMarshallTime ()));
            System.out.println ("   avg. RPC (un)marshall time:               "
                     + formatTimeStatistic (node_stats.avgRPCMarshallTime (),
                                 saved_node_stats.avgRPCMarshallTime ()));

         }
*/
         System.out.println ("Operation Statistics");
         System.out.println ("   total sent:                               " +
                     formatStatistic (node_stats.countTotalOperationSent (),
                                      saved_node_stats.countTotalOperationSent ()));
         System.out.println ("   total received:                           " +
                     formatStatistic (node_stats.countTotalOperationReceived (),
                                      saved_node_stats.countTotalOperationReceived ()));
         System.out.println ("   lookups sent:                             " +
                     formatStatistic (node_stats.countLookupOperationSent (),
                                      saved_node_stats.countLookupOperationSent ()));
         System.out.println ("   lookups received:                         " +
                     formatStatistic (node_stats.countLookupOperationReceived (),
                                      saved_node_stats.countLookupOperationReceived ()));
         System.out.println ("   inserts sent:                             " +
                     formatStatistic (node_stats.countInsertOperationSent (),
                                      saved_node_stats.countInsertOperationSent ()));
         System.out.println ("   inserts received:                         " +
                     formatStatistic (node_stats.countInsertOperationReceived (),
                                      saved_node_stats.countInsertOperationReceived ()));
         System.out.println ("   insert checks sent:                       " +
                     formatStatistic (node_stats.countInsertCheckOperationSent (),
                                      saved_node_stats.countInsertCheckOperationSent ()));
         System.out.println ("   insert checks received:                   " +
                     formatStatistic (node_stats.countInsertCheckOperationReceived (),
                                      saved_node_stats.countInsertCheckOperationReceived ()));
         System.out.println ("   deletes sent:                             " +
                     formatStatistic (node_stats.countDeleteOperationSent (),
                                      saved_node_stats.countDeleteOperationSent ()));
         System.out.println ("   deletes received:                         " +
                     formatStatistic (node_stats.countDeleteOperationReceived (),
                                      saved_node_stats.countDeleteOperationReceived ()));
         System.out.println ("   test inserts sent:                        " +
                     formatStatistic (node_stats.countTestInsertOperationSent (),
                                      saved_node_stats.countTestInsertOperationSent ()));
         System.out.println ("   test inserts received:                    " +
                     formatStatistic (node_stats.countTestInsertOperationReceived (),
                                      saved_node_stats.countTestInsertOperationReceived ()));
         System.out.println ("   test deletes sent:                        " +
                     formatStatistic (node_stats.countTestDeleteOperationSent (),
                                      saved_node_stats.countTestDeleteOperationSent ()));
         System.out.println ("   test deletes received:                    " +
                     formatStatistic (node_stats.countTestDeleteOperationReceived (),
                                      saved_node_stats.countTestDeleteOperationReceived ()));
         System.out.println ("   get domains sent:                         " +
                     formatStatistic (node_stats.countGetDomainOperationSent (),
                                      saved_node_stats.countGetDomainOperationSent ()));
         System.out.println ("   get domains received:                     " +
                     formatStatistic (node_stats.countGetDomainOperationReceived (),
                                      saved_node_stats.countGetDomainOperationReceived ()));
         System.out.println ("   avg. requests received/second:            " +
                     formatStatistic (node_stats.timeInvokerRequest (),
                                      saved_node_stats.timeInvokerRequest ()));
         
         if(Debug.statsTime())
         {
            System.out.println ("   total lookup time:                        "
                     + formatTimeStatistic (node_stats.timeLookupOperationTime (),
                                 saved_node_stats.timeLookupOperationTime ()));
            System.out.println ("   avg. lookup time:                         "
                     + formatTimeStatistic (node_stats.avgLookupOperationTime (),
                                 saved_node_stats.avgLookupOperationTime ()));
            System.out.println ("   total insert time:                        "
                     + formatTimeStatistic (node_stats.timeInsertOperationTime (),
                                 saved_node_stats.timeInsertOperationTime ()));
            System.out.println ("   avg. insert time:                         "
                     + formatTimeStatistic (node_stats.avgInsertOperationTime (),
                                 saved_node_stats.avgInsertOperationTime ()));
            System.out.println ("   total insert check time:                  "
                     + formatTimeStatistic (node_stats.timeInsertChkOperationTime (),
                                 saved_node_stats.timeInsertChkOperationTime ()));
            System.out.println ("   avg. insert check time:                   "
                     + formatTimeStatistic (node_stats.avgInsertChkOperationTime (),
                                 saved_node_stats.avgInsertChkOperationTime ()));
            System.out.println ("   total delete time:                        "
                     + formatTimeStatistic (node_stats.timeDeleteOperationTime (),
                                 saved_node_stats.timeDeleteOperationTime ()));
            System.out.println ("   avg. delete time:                         "
                     + formatTimeStatistic (node_stats.avgDeleteOperationTime (),
                                 saved_node_stats.avgDeleteOperationTime ()));
            System.out.println ("   total update time:                        "
                     + formatTimeStatistic (node_stats.timeUpdateOperationTime (),
                                 saved_node_stats.timeUpdateOperationTime ()));
            System.out.println ("   avg. update time:                         "
                     + formatTimeStatistic (node_stats.avgUpdateOperationTime (),
                                 saved_node_stats.avgUpdateOperationTime ()));
            System.out.println ("   number of view series evaluations:        "
                     + formatStatistic (node_stats.countViewSeriesEval (),
                                 saved_node_stats.countViewSeriesEval ()));
            System.out.println ("   total view series evaluation time:        "
                     + formatTimeStatistic (node_stats.timeViewSeriesEvalTime (),
                                 saved_node_stats.timeViewSeriesEvalTime ()));
            System.out.println ("   avg. view series evaluation time:         "
                     + formatTimeStatistic (node_stats.avgViewSeriesEvalTime (),
                                 saved_node_stats.avgViewSeriesEvalTime ()));
            
            System.out.println("--------------------");
            System.out.println ("   total operation (un)marshall time:        "
                     + formatTimeStatistic (node_stats.timeOperationMarshallTime (),
                                 saved_node_stats.timeOperationMarshallTime ()));
            System.out.println ("   avg. operation (un)marshall time:         "
                     + formatTimeStatistic (node_stats.avgOperationMarshallTime (),
                                 saved_node_stats.avgOperationMarshallTime ()));

         }


         System.out.println ("Recovery Statistics");

         if(! recovery.inRecovery())
            System.out.println ("   recovery mode duration:                   "+
               node_stats.timeRecoveryMode() + " (s)");

         System.out.println ("   writes to log:                            " +
                     formatStatistic (node_stats.countRpcLogWrite (),
                                      saved_node_stats.countRpcLogWrite ()));
         System.out.println ("   reads from log:                           " +
                     formatStatistic (node_stats.countRpcLogRead (),
                                      saved_node_stats.countRpcLogRead ()));
         System.out.println ("   deletes from log:                         " +
                     formatStatistic (node_stats.countRpcLogRemove (),
                                      saved_node_stats.countRpcLogRemove ()));
         System.out.println ("   bytes written to log:                     " +
                     formatStatistic (node_stats.sizeRpcLogWrite (),
                                      saved_node_stats.sizeRpcLogWrite ()));
         System.out.println ("   bytes read from log:                      " +
                     formatStatistic (node_stats.sizeRpcLogRead (),
                                      saved_node_stats.sizeRpcLogRead ()));
         System.out.println ("   avg. bytes/write to log:                  " +
                     formatStatistic (node_stats.avgSizeRpcLogWrite (),
                                      saved_node_stats.avgSizeRpcLogWrite ()));
         System.out.println ("   avg. bytes/read from log:                 " +
                     formatStatistic (node_stats.avgSizeRpcLogRead (),
                                      saved_node_stats.avgSizeRpcLogRead ()));
         if(Debug.statsTime()) 
         {
            System.out.println ("   time writing to log:                      "
                     + formatTimeStatistic (node_stats.timeRpcLogWriteTime (),
                                       saved_node_stats.timeRpcLogWriteTime ()));
            System.out.println ("   time reading from log:                    "
                     + formatTimeStatistic (node_stats.timeRpcLogReadTime (),
                                       saved_node_stats.timeRpcLogReadTime ()));
            System.out.println ("   time deleting from log:                   "
                     + formatTimeStatistic (node_stats.timeRpcLogRemoveTime (),
                                       saved_node_stats.timeRpcLogRemoveTime ()));
            System.out.println ("   avg. time/write to log:                   "
                     + formatTimeStatistic (node_stats.avgRpcLogWriteTime (),
                                       saved_node_stats.avgRpcLogWriteTime ()));
            System.out.println ("   avg. time/read from log:                  "
                     + formatTimeStatistic (node_stats.avgRpcLogReadTime (),
                                       saved_node_stats.avgRpcLogReadTime ()));
            System.out.println ("   avg. time/delete from log:                "
                     + formatTimeStatistic (node_stats.avgRpcLogRemoveTime (),
                                       saved_node_stats.avgRpcLogRemoveTime ()));
            System.out.println ("   total log (un)marshall time:              "
                     + formatTimeStatistic (node_stats.timeRpcLogMarshallTime (),
                                 saved_node_stats.timeRpcLogMarshallTime ()));
            System.out.println ("   avg. log (un)marshall time:               "
                     + formatTimeStatistic (node_stats.avgRpcLogMarshallTime (),
                                 saved_node_stats.avgRpcLogMarshallTime ()));

         }

         System.out.println ("Contact Record Database Statistics");

         System.out.println ("   writes:                                   " +
                     formatStatistic (node_stats.countCRDBWrite (),
                                         saved_node_stats.countCRDBWrite ()));
         System.out.println ("   reads:                                    " +
                     formatStatistic (node_stats.countCRDBRead (),
                                         saved_node_stats.countCRDBRead ()));
         System.out.println ("   deletes:                                  " +
                     formatStatistic (node_stats.countCRDBRemove (),
                                         saved_node_stats.countCRDBRemove ()));
         System.out.println ("   bytes written:                            " +
                     formatStatistic (node_stats.sizeCRDBWrite (),
                                      saved_node_stats.sizeCRDBWrite ()));
         System.out.println ("   bytes read:                               " +
                     formatStatistic (node_stats.sizeCRDBRead (),
                                      saved_node_stats.sizeCRDBRead ()));
         System.out.println ("   avg. bytes/write:                         " +
                     formatStatistic (node_stats.avgSizeCRDBWrite (),
                                      saved_node_stats.avgSizeCRDBWrite ()));
         System.out.println ("   avg. bytes/read:                          " +
                     formatStatistic (node_stats.avgSizeCRDBRead (),
                                      saved_node_stats.avgSizeCRDBRead ()));
         
         if(Debug.statsTime())
         {
            System.out.println ("   time writing:                             "
                     + formatTimeStatistic (node_stats.timeCRDBWriteTime (),
                                       saved_node_stats.timeCRDBWriteTime ()));
            System.out.println ("   time reading:                             "
                     + formatTimeStatistic (node_stats.timeCRDBReadTime (),
                                       saved_node_stats.timeCRDBReadTime ()));
            System.out.println ("   time deleting:                            "
                     + formatTimeStatistic (node_stats.timeCRDBRemoveTime (),
                                       saved_node_stats.timeCRDBRemoveTime ()));
            System.out.println ("   avg. time/write:                          "
                     + formatTimeStatistic (node_stats.avgCRDBWriteTime (),
                                       saved_node_stats.avgCRDBWriteTime ()));
            System.out.println ("   avg. time/read:                           "
                     + formatTimeStatistic (node_stats.avgCRDBReadTime (),
                                       saved_node_stats.avgCRDBReadTime ()));
            System.out.println ("   avg. time/delete:                         "
                     + formatTimeStatistic (node_stats.avgCRDBRemoveTime (),
                                       saved_node_stats.avgCRDBRemoveTime ()));
            System.out.println ("   total crdb (un)marshall time:             "
                     + formatTimeStatistic (node_stats.timeCRDBMarshallTime (),
                                 saved_node_stats.timeCRDBMarshallTime ()));
            System.out.println ("   avg. crdb (un)marshall time:              "
                     + formatTimeStatistic (node_stats.avgCRDBMarshallTime (),
                                 saved_node_stats.avgCRDBMarshallTime ()));
         }

         if(Debug.statsTime())
         {
            System.out.println ("Thread Statistics");
            System.out.println("   Total Thread time                         "
                     + formatTime(node_stats.timeTotalThreadTime ()));
            System.out.println("   Scheduler time                            "
                     + formatTime(node_stats.timeSchedulerTime ()));
            System.out.println("   PassiveActivator time                     "
                     + formatTime(node_stats.timePassiveActivatorTime ()));
            System.out.println("   ActiveTimer time                          "
                     + formatTime(node_stats.timeActiveTimerTime ()));

            System.out.println("--------------------");
            System.out.println("Logical Components -- Total Time             "
                     + formatTimeStatistic(node_stats.timeTotalThreadTime (),
								saved_node_stats.timeTotalThreadTime ()));
            System.out.println("   Other:                                    "
                     + formatTimeStatistic(node_stats.timeMiscTime (),
								saved_node_stats.timeMiscTime ()));
            System.out.println("   Comm (Un)marshalling:                     "
                     + formatTimeStatistic(node_stats.timeCommMarshallTime (),
								saved_node_stats.timeCommMarshallTime ()));
            System.out.println("   Algorithms:                               "
                     + formatTimeStatistic(node_stats.timeAlgorithmTime (),
								saved_node_stats.timeAlgorithmTime ()));
            System.out.println("   Partitioning:                             "
                     + formatTimeStatistic(node_stats.timePartitioningTime (),
								saved_node_stats.timePartitioningTime ()));
            System.out.println("   CRDB Disk I/O:                            "
                     + formatTimeStatistic(node_stats.timeDiskIOCRDBTime ()
                        + node_stats.timeCRDBMarshallTime (),
								saved_node_stats.timeDiskIOCRDBTime ()
								+ saved_node_stats.timeCRDBMarshallTime ()));
            System.out.println("   Crash Recovery:                           "
                     + formatTimeStatistic(node_stats.timeRecoveryTime (),
								saved_node_stats.timeRecoveryTime ()));
            System.out.println("   Crash Recovery Log Disk I/O:              "
                     + formatTimeStatistic(node_stats.timeDiskIORecoveryTime ()
                        + node_stats.timeRpcLogMarshallTime (),
								saved_node_stats.timeDiskIORecoveryTime () 
								+ saved_node_stats.timeRpcLogMarshallTime ()));
            System.out.println("   RPC Ordering & Reliability:               "
                     + formatTimeStatistic(node_stats.timeRPCTime (),
								saved_node_stats.timeRPCTime ()));
            System.out.println("--------------------");
            System.out.println("Node Layers -- Total Time                    "
                     + formatTimeStatistic(node_stats.timeUnderlyingLayerTime ()
                        + node_stats.timeDatagramLayerTime ()
                        + node_stats.timeMessengerLayerTime ()
                        + node_stats.timeRPCLayerTime ()
                        + node_stats.timeAlgorithmTime ()
                        + node_stats.timeDiskIOCRDBTime ()
                        + node_stats.timeCRDBMarshallTime ()
                        + node_stats.timeAlgorithmLayerTime (),
								saved_node_stats.timeUnderlyingLayerTime ()
                        + saved_node_stats.timeDatagramLayerTime ()
                        + saved_node_stats.timeMessengerLayerTime ()
                        + saved_node_stats.timeRPCLayerTime ()
                        + saved_node_stats.timeAlgorithmTime ()
                        + saved_node_stats.timeDiskIOCRDBTime ()
                        + saved_node_stats.timeCRDBMarshallTime ()
                        + saved_node_stats.timeAlgorithmLayerTime ()));
            System.out.println("   Underlying Send Layer:                    "
                     + formatTimeStatistic(node_stats.timeUnderlyingSendTime (),
								saved_node_stats.timeUnderlyingSendTime ()));
            System.out.println("   Underlying Receive Layer:                 "
                     + formatTimeStatistic(
								node_stats.timeUnderlyingReceiveTime (),
								saved_node_stats.timeUnderlyingReceiveTime ()));
            System.out.println("   Datagram Layer:                           "
                     + formatTimeStatistic(node_stats.timeDatagramLayerTime (),
								saved_node_stats.timeDatagramLayerTime ()));
            System.out.println("   Messenger Layer:                          "
                     + formatTimeStatistic(node_stats.timeMessengerLayerTime (),
								saved_node_stats.timeMessengerLayerTime ()));
            System.out.println("   RPC Layer:                                "
                     + formatTimeStatistic(node_stats.timeRPCLayerTime (),
							saved_node_stats.timeRPCLayerTime ()));
            System.out.println("   Algorithm Layer:                          "
                     + formatTimeStatistic(node_stats.timeAlgorithmTime ()
                        + node_stats.timeDiskIOCRDBTime ()
                        + node_stats.timeCRDBMarshallTime ()
                        + node_stats.timeAlgorithmLayerTime (),
								saved_node_stats.timeAlgorithmTime ()
								+ saved_node_stats.timeDiskIOCRDBTime ()
								+ saved_node_stats.timeCRDBMarshallTime ()
								+ saved_node_stats.timeAlgorithmLayerTime ()));
            System.out.println("--------------------");
         }
         
         System.out.println ("statistics at " + new Date () + ":");
      }
      else
      {
         System.out.println ("RPC Statistics");

         System.out.println ("   requests sent:                            " +
                        formatStatistic (stats.countRequestRPCSent (),
                                         saved_stats.countRequestRPCSent ()));
         System.out.println ("   requests timed out:                       " +
                        formatStatistic (stats.countRequestRPCTimeout (),
                                         saved_stats.countRequestRPCTimeout ()));
         System.out.println ("   replies received:                         " +
                        formatStatistic (stats.countReplyRPCReceived (),
                                         saved_stats.countReplyRPCReceived ()));
         System.out.println ("   mapper conversions:                       " +
                        formatStatistic (stats.countMapperConversion (),
                                         saved_stats.countMapperConversion ()));
         System.out.println ("   total mapper conversion time:             " + 
                        formatTimeStatistic (stats.timeMapperConversionTime (),
                                 saved_stats.timeMapperConversionTime ()));
         System.out.println ("   avg. mapper conversion time:              " + 
                        formatTimeStatistic (stats.avgMapperConversionTime (),
                                 saved_stats.avgMapperConversionTime ()));
      

         System.out.println("Operation Statistics");

         System.out.println ("   total sent:                               " +
                     formatStatistic (stats.countTotalOperationSent (),
                                      saved_stats.countTotalOperationSent ()));
         System.out.println ("   lookups sent:                             " +
                     formatStatistic (stats.countLookupOperationSent (),
                                      saved_stats.countLookupOperationSent ()));
         System.out.println ("   inserts sent:                             " +
                     formatStatistic (stats.countInsertOperationSent (),
                                      saved_stats.countInsertOperationSent ()));
         System.out.println ("   deletes sent:                             " +
                     formatStatistic (stats.countDeleteOperationSent (),
                                      saved_stats.countDeleteOperationSent ()));
         System.out.println ("   test inserts sent:                        " +
                     formatStatistic (stats.countTestInsertOperationSent (),
                                      saved_stats.countTestInsertOperationSent ()));
         System.out.println ("   test deletes sent:                        " +
                     formatStatistic (stats.countTestDeleteOperationSent (),
                                      saved_stats.countTestDeleteOperationSent ()));
         System.out.println ("   get domains sent:                         " +
                     formatStatistic (stats.countGetDomainOperationSent (),
                                      saved_stats.countGetDomainOperationSent ()));

      }
      
      System.out.println ("");

      saved_stats = (Statistics) stats.clone ();
      timer.set (interval, null);
   }

   /**
      Formats a statistic and its increase.

      @return
         a string: 'current (+diff)', where diff = current-previous.
   */
   private String formatStatistic (int current, int previous)
   {
      return current + " (+" + (current - previous) + ")";
   }

   private String formatStatistic (long current, long previous)
   {
      return current + " (+" + (current - previous) + ")";
   }

   private String formatStatistic (double current, double previous)
   {
      return current + " (+" + truncDouble(current - previous) + ")";
   }

   private String formatTimeStatistic (long current, long previous)
   {
      if(TimeLib.haveMicros())
         return current + " (+" + (current - previous) + ") (us)";
      else
         return current + " (+" + (current - previous) + ") (ms)";
   }

   private String formatTimeStatistic (double current, double previous)
   {
      if(TimeLib.haveMicros())
         return current + " (+" + truncDouble(current - previous) + ") (us)";
      else
         return current + " (+" + truncDouble(current - previous) + ") (ms)";
   }

   private String formatTime (double current)
   {
      if(TimeLib.haveMicros())
         return current + " (us)";
      else
         return current + " (ms)";
   }

   private String formatTime (long current)
   {
      if(TimeLib.haveMicros())
         return current + " (us)";
      else
         return current + " (ms)";
   }

   // truncates a double to 2 decimals places
   private double truncDouble (double d)
   {
      return (double) Math.round(d * 100) / 100;
   }
}
