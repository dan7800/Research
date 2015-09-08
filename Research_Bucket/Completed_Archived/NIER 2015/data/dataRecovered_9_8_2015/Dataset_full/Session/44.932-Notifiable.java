/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active;

/**
   The interface through which an object can notify its user.
   <p>
   To notify its user of an object-defined event, an object allows a
   <code>Notifiable</code> to be installed together with one or more user
   cookies of type <code>ActiveUID</code>. (Different cookies might be used
   for different kinds of events.)
   When the event occurs, the user is notified through the installed
   <code>Notifiable</code> and a user cookie is passed along as well.
   <p>
   In general:
   <ul>
   <li>
   exceptions also count as events,
   <li>
   a single notification represents exactly one event,
   <li>
   an 'unhandled' event will not cause another notification.
   </ul>
 
   @author Patrick Verkaik
*/

public interface Notifiable
{
   /**
      Notifies the user of an object-defined event.

      @param userInfo   the user cookie.
   */
   void notifyEvent (ActiveUID userInfo);
}
