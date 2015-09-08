/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active;

/**
   A scheduler object schedules callbacks in mutual exclusion. Callbacks should
   avoid long-term blocking operations, and must never wait for other scheduler
   callbacks to occur.
   <p>
   Regular users may use the scheduler by registering so-called 'events' with
   the <code>addEvent</code> method. Each such event is associated with a
   user registration, obtained through the <code>Active</code> interface.
   <p>
   Upcalling threads can make similar but more efficient use of events by
   calling <code>schedEvent</code>. They can also bypass events entirely and
   make direct callbacks by invoking <code>schedCallBack</code>. Such callbacks
   will still be made in mutual exclusion with events and other callbacks.
 
   @author Patrick Verkaik
*/

public interface Scheduler extends Active
{
   /**
      Adds an event to be scheduled. The event is associated with the given
      registration.
   */
   void addEvent (ActiveRID regID);

   /**
      Adds an event to be scheduled. The event is associated with the given
      registration.
      In addition the caller's thread may be used to handle any events
      currently waiting. Downcalling threads should never invoke this method.
      They should use <code>addEvent</code> instead.
   */
   void schedEvent (ActiveRID regID);

   /**
      Schedules a callback. In addition the caller's thread may be used to
      handle any events currently waiting.  May only be invoked by upcalling
      threads.
   */
   void schedCallBack (Notifiable note, ActiveUID user_info);
}
