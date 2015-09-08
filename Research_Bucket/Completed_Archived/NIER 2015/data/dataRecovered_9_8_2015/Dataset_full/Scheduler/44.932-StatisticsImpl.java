/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.stats;

import vu.globe.util.time.Stopwatch;

import vu.globe.util.exc.AssertionFailedException;

/**
   A <code>Statistics</code> implementation.
 
   @author Patrick Verkaik
*/

public class StatisticsImpl implements Statistics, Cloneable
{
   private int under_send = 0;
   private long under_send_time = 0;
   private int under_rcv = 0;
   private long under_rcv_time = 0;

   private int dgram_sent_count = 0;
   private int dgram_rcvd_count = 0;
   private int dgram_sent_size = 0;
   private int dgram_rcvd_size = 0;
  
   private int dgram_marshall = 0;
   private long dgram_marshall_time = 0;

   private int dgram_layer = 0;
   private long dgram_layer_time = 0;

   private int msg_total_sent = 0;
   private int msg_total_rcvd = 0;
   private int msg_data_sent = 0;
   private int msg_data_rcvd = 0;
   private int msg_data_sent_retrans = 0;
   private int msg_data_rcvd_retrans = 0;
   private int msg_epoch_sent = 0;
   private int msg_epoch_rcvd = 0;
   private int msg_epoch_sent_retrans = 0;
   private int msg_ack_sent = 0;
   private int msg_ack_rcvd = 0;

   private int msg_marshall = 0;
   private long msg_marshall_time = 0;

   private long epoch_time = 0;
   private long ack_time = 0;

   private long msger_callback_queue_time = 0;
   private double msger_callback_queue_avg = 0;

   private long msg_order_queue_time = 0;
   private int msg_order_queue = 0;
   
   private long msger_layer_time = 0;
   private int msger_layer = 0;

   protected int rpc_total_sent = 0;
   protected int rpc_total_rcvd = 0;
   protected int rpc_req_sent = 0;
   protected int rpc_req_timeout = 0;
   protected int rpc_rep_rcvd = 0;
   
   private int rpc_marshall = 0;
   private long rpc_marshall_time = 0;

   private int mapper_conv = 0;
   private long mapper_conv_time = 0;

   private int rpc_layer = 0;
   private long rpc_layer_time = 0;

   protected long rpc_rep_callback_queue_time = 0;
   protected double rpc_rep_callback_queue_avg = 0;

   protected int op_total_sent = 0;
   protected int op_lookup_sent = 0;
   protected int op_insert_sent = 0;
   protected int op_delete_sent = 0;
   protected int op_test_ins_sent = 0;
   protected int op_test_del_sent = 0;
   protected int op_get_dom_sent = 0;
   
   private int op_marshall = 0;
   private long op_marshall_time = 0;

   private int alg = 0;
   private long alg_time = 0;

   private int sched_thread = 0;
   private long sched_thread_time = 0;
   
   private int pass_thread = 0;
   private long pass_thread_time = 0;

   private int timer_thread = 0;
   private long timer_thread_time = 0;

   public synchronized void addUnderlyingSendTime (Stopwatch timer)
   {
      under_send_time += timer.elapsedTime();
      under_send++;
   }

   public synchronized long timeUnderlyingSendTime ()
   {
      return under_send_time;
   }

   public synchronized void addUnderlyingReceiveTime (Stopwatch timer)
   {
      under_rcv_time += timer.elapsedTime();
      under_rcv++;
   }

   public synchronized long timeUnderlyingReceiveTime ()
   {
      return under_rcv_time;
   }

   public synchronized long timeUnderlyingLayerTime ()
   {
      return timeUnderlyingSendTime() + timeUnderlyingReceiveTime ();
   }

   public synchronized void addDatagramSent (int size)
   {
      dgram_sent_count++;
      dgram_sent_size += size;
   }
 
   public synchronized int countDatagramSent ()
   {
      return dgram_sent_count;
   }
 
   public synchronized int sizeDatagramSent ()
   {
      return dgram_sent_size;
   }
 
   public synchronized void addDatagramReceived (int size)
   {
      dgram_rcvd_count++;
      dgram_rcvd_size += size;
   }
 
   public synchronized int countDatagramReceived ()
   {
      return dgram_rcvd_count;
   }
 
   public synchronized int sizeDatagramReceived ()
   {
      return dgram_rcvd_size;
   }

   public synchronized void addDatagramMarshallTime (Stopwatch timer)
   {
      dgram_marshall_time += timer.elapsedTime();
      dgram_marshall++;
   }
 
   public synchronized long timeDatagramMarshallTime ()
   {
      return dgram_marshall_time;
   }
 
   public synchronized double avgDatagramMarshallTime ()
   {
      if(dgram_marshall == 0)
         return 0;
      else
         return truncDouble((double) dgram_marshall_time / dgram_marshall);
   }

   public synchronized void addDatagramLayerTime (Stopwatch timer)
   {
      dgram_layer_time += timer.elapsedTime();
      dgram_layer++;
   }

   public synchronized long timeDatagramLayerTime ()
   {
      return dgram_layer_time;
   }

   public synchronized double avgDatagramLayerTime ()
   {
      if(dgram_layer == 0)
         return 0;
      else
         return truncDouble((double) dgram_layer_time / dgram_layer);
   }

   public synchronized void addDataMessageSent (int count)
   {
      msg_data_sent += count;
      msg_total_sent += count;
   }
 
   public synchronized void addDataMessageReceived (int count)
   {
      msg_data_rcvd += count;
      msg_total_rcvd += count;
   }
 
   public synchronized void addDataMessageSentRetrans (int count)
   {
      msg_data_sent_retrans += count;
      msg_total_sent += count;
   }
 
   public synchronized void addDataMessageReceivedRetrans (int count)
   {
      msg_data_rcvd_retrans += count;
      msg_total_rcvd += count;
   }
 
   public synchronized int countDataMessageSent ()
   {
      return msg_data_sent;
   }
 
   public synchronized int countDataMessageReceived ()
   {
      return msg_data_rcvd;
   }

   public synchronized int countDataMessageSentRetrans ()
   {
      return msg_data_sent_retrans;
   }
 
   public synchronized int countDataMessageReceivedRetrans ()
   {
      return msg_data_rcvd_retrans;
   }

   public synchronized void addAckMessageSent (int count)
   {
      msg_ack_sent += count;
      msg_total_sent += count;
   }
 
   public synchronized void addAckMessageReceived (int count)
   {
      msg_ack_rcvd += count;
      msg_total_rcvd += count;
   }
 
   public synchronized int countAckMessageSent ()
   {
      return msg_ack_sent;
   }
 
   public synchronized int countAckMessageReceived ()
   {
      return msg_ack_rcvd;
   }

   public synchronized void addEpochMessageSent (int count)
   {
      msg_epoch_sent += count;
      msg_total_sent += count;
   }
 
   public synchronized void addEpochMessageReceived (int count)
   {
      msg_epoch_rcvd += count;
      msg_total_rcvd += count;
   }
 
   public synchronized void addEpochMessageSentRetrans (int count)
   {
      msg_epoch_sent_retrans += count;
      msg_total_sent += count;
   }
 
   public synchronized int countEpochMessageSent ()
   {
      return msg_epoch_sent;
   }
 
   public synchronized int countEpochMessageSentRetrans ()
   {
      return msg_epoch_sent_retrans;
   }
 
   public synchronized int countEpochMessageReceived ()
   {
      return msg_epoch_rcvd;
   }

   public synchronized void addEpochProcessingTime (Stopwatch timer)
   {
      epoch_time += timer.elapsedTime();
   }

   public synchronized void addMessengerAckTime (Stopwatch timer)
   {
      ack_time += timer.elapsedTime();
   }

   public synchronized long timeEpochProcessingTime ()
   {
      return epoch_time;
   }

   public synchronized long timeMessengerAckTime ()
   {
      return ack_time; 
   }

   public synchronized double avgMessengerAckTime ()
   {
      if(msg_ack_sent == 0)
         return 0;
      else
         return truncDouble((double) ack_time / msg_ack_sent);
   }

   public synchronized int countMessageSent ()
   {
      return msg_total_sent;
   }
 
   public synchronized int countMessageReceived ()
   {
      return msg_total_rcvd;
   }

   public synchronized void setMessengerCallbackQueueTotalTime (long time)
   {
      msger_callback_queue_time = time; 
   }

   public synchronized void setMessengerCallbackQueueAvgTime (double avg)
   {
      msger_callback_queue_avg = avg; 
   }
   
   public synchronized long timeMessengerCallbackQueueTime ()
   {
      return msger_callback_queue_time; 
   }

   public synchronized double avgMessengerCallbackQueueTime ()
   {
      return msger_callback_queue_avg; 
   }

   public synchronized void addMessageMarshallTime (Stopwatch timer)
   {
      msg_marshall_time += timer.elapsedTime();
      msg_marshall++;
   }

   public synchronized long timeMessageMarshallTime ()
   {
      return msg_marshall_time;
   }

   public synchronized double avgMessageMarshallTime ()
   {
      if(msg_marshall == 0)
         return 0;
      else
         return truncDouble((double) msg_marshall_time / msg_marshall);
   }

   public synchronized void addMessageOrderQueueTime (Stopwatch timer)
   {
      msg_order_queue_time += timer.elapsedTime();
      msg_order_queue++;
   }
 
   public synchronized long timeMessageOrderQueueTime ()
   {
      return msg_order_queue_time;
   }
 
   public synchronized double avgMessageOrderQueueTime ()
   {
      if(msg_order_queue == 0)
         return 0;
      else
         return truncDouble((double) msg_order_queue_time / msg_order_queue);
   }

   public synchronized void addMessengerLayerTime (Stopwatch timer)
   {
      msger_layer_time += timer.elapsedTime();
      msger_layer++;
   }

   public synchronized long timeMessengerLayerTime ()
   {
      return msger_layer_time; 
   }

   public synchronized double avgMessengerLayerTime ()
   {
      if(msger_layer == 0)
         return 0;
      else
         return truncDouble((double) msger_layer_time / msger_layer);
   }

   public synchronized void addRequestRPCSent (int count)
   {
      rpc_req_sent += count;
      rpc_total_sent += count;
   }

   public synchronized void addRequestRPCTimeout (int count)
   {
      rpc_req_timeout += count;
   }

   public synchronized void addReplyRPCReceived (int count)
   {
      rpc_rep_rcvd += count;
      rpc_total_rcvd += count;
   }

   public synchronized int countRequestRPCSent ()
   {
      return rpc_req_sent;   
   }
   
   public synchronized int countRequestRPCTimeout ()
   {
      return rpc_req_timeout;   
   }

   public synchronized int countReplyRPCReceived ()
   {
      return rpc_rep_rcvd; 
   }

   public synchronized void addMapperConversion (int count)
   {
      mapper_conv += count;
   }

   public synchronized void addMapperConversionTime (Stopwatch timer)
   {
      mapper_conv_time += timer.elapsedTime ();   
   }

   public synchronized int countMapperConversion ()
   {
      return mapper_conv;
   }

   public synchronized long timeMapperConversionTime ()
   {
      return mapper_conv_time;
   }

   public synchronized double avgMapperConversionTime ()
   {
      if(mapper_conv == 0)
         return 0;
      else
         return truncDouble((double) mapper_conv_time / mapper_conv);
   }

   public synchronized void setRPCReplyCallbackQueueTotalTime (long time)
   {
      rpc_rep_callback_queue_time = time;
   }

   public synchronized void setRPCReplyCallbackQueueAvgTime (double avg)
   {
      rpc_rep_callback_queue_avg = avg;
   }

   public synchronized long timeRPCReplyCallbackQueueTime ()
   {
      return rpc_rep_callback_queue_time;
   }

   public synchronized double avgRPCReplyCallbackQueueTime ()
   {
      return rpc_rep_callback_queue_avg;
   }

   public synchronized void addRPCMarshallTime (Stopwatch timer)
   {
      rpc_marshall_time += timer.elapsedTime();
      rpc_marshall++;
   }

   public synchronized long timeRPCMarshallTime ()
   {
      return rpc_marshall_time;
   }

   public synchronized double avgRPCMarshallTime ()
   {
      if(rpc_marshall == 0)
         return 0;
      else
         return truncDouble((double) rpc_marshall_time / rpc_marshall);
   }

   public synchronized void addRPCLayerTime (Stopwatch timer)
   {
      rpc_layer_time += timer.elapsedTime();
      rpc_layer++;
   }
 
   public synchronized long timeRPCLayerTime ()
   {
      return rpc_layer_time;
   }
 
   public synchronized double avgRPCLayerTime ()
   {
      if(rpc_layer == 0)
         return 0;
      else
         return truncDouble((double) rpc_layer_time / rpc_layer);
   }

   public synchronized void addLookupOperationSent (int count)
   {
      op_lookup_sent += count;
      op_total_sent += count;   
   }

   public synchronized void addInsertOperationSent (int count)
   {
      op_insert_sent += count;
      op_total_sent += count;   
   }

   public synchronized void addDeleteOperationSent (int count)
   {
      op_delete_sent += count;
      op_total_sent += count;   
   }

   public synchronized void addTestInsertOperationSent (int count)
   {
      op_test_ins_sent += count;
      op_total_sent += count;   
   }

   public synchronized void addTestDeleteOperationSent (int count)
   {
      op_test_del_sent += count;
      op_total_sent += count;   
   }

   public synchronized void addGetDomainOperationSent (int count)
   {
      op_get_dom_sent += count;
      op_total_sent += count;   
   }
   
   public synchronized int countLookupOperationSent ()
   {
      return op_lookup_sent;
   }

   public synchronized int countInsertOperationSent ()
   {
      return op_insert_sent;
   }

   public synchronized int countDeleteOperationSent ()
   {
      return op_delete_sent;
   }

   public synchronized int countTestInsertOperationSent ()
   {
      return op_test_ins_sent;
   }

   public synchronized int countTestDeleteOperationSent ()
   {
      return op_test_del_sent;
   }

   public synchronized int countGetDomainOperationSent ()
   {
      return op_get_dom_sent;
   }

   public synchronized int countTotalOperationSent ()
   {
      return op_total_sent;
   }

   public synchronized void addOperationMarshallTime (Stopwatch timer)
   {
      op_marshall_time += timer.elapsedTime();
      op_marshall++;
   }

   public synchronized void addAlgorithmLayerTime (Stopwatch timer)
   {
      alg_time += timer.elapsedTime();
      alg++;
   }

   public synchronized long timeOperationMarshallTime ()
   {
      return op_marshall_time;
   }

   public synchronized long timeAlgorithmLayerTime ()
   {
      return alg_time;
   }

   public synchronized double avgOperationMarshallTime ()
   {
      if(op_marshall == 0)
         return 0;
      else
         return truncDouble((double) op_marshall_time / op_marshall);
   }

   public synchronized void addSchedulerTime (Stopwatch timer)
   {
      sched_thread++;
      sched_thread_time += timer.elapsedTime();
   }

   public synchronized long timeSchedulerTime ()
   {
      return sched_thread_time; 
   }

   public synchronized double avgSchedulerTime ()
   {
      if(sched_thread == 0)
         return 0; 
      else
         return truncDouble((double) sched_thread_time / sched_thread);
   }

   public synchronized void addPassiveActivatorTime (Stopwatch timer)
   {
      pass_thread++;
      pass_thread_time += timer.elapsedTime();
   }
 
   public synchronized long timePassiveActivatorTime ()
   {
      return pass_thread_time; 
   }

   public synchronized double avgPassiveActivatorTime ()
   {
      if(pass_thread == 0)
         return 0;
      else
         return truncDouble((double) pass_thread_time / pass_thread);
   }
 
   public synchronized void addActiveTimerTime (Stopwatch timer)
   {
      timer_thread++;
      timer_thread_time += timer.elapsedTime();
   }
 
   public synchronized long timeActiveTimerTime ()
   {
      return timer_thread_time; 
   }

   public synchronized double avgActiveTimerTime ()
   {
      if(timer_thread == 0)
         return 0;
      else
         return truncDouble((double) timer_thread_time / timer_thread);
   }

   // truncates a double to 2 decimals places
   protected double truncDouble (double d)
   {
      return (double) Math.round(d * 100) / 100;
   }

   public Object clone ()
   {
      try {
         return super.clone ();
      }
      catch (CloneNotSupportedException exc) {
         throw new AssertionFailedException ();
      }
   }
}
