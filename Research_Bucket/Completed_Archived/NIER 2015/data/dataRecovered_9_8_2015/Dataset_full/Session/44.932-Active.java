/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active;

/**
   An object that implements <code>Active</code> is able to notify its user of
   object-defined events.
   <p>
   A user cookie ('active user id') is passed along with the registration.
   Normally, an object will pass the cookie back on notification, for the
   benefit of the user.
   Some objects, however, may choose to ignore this user cookie and instead
   allow different user cookies to be installed through other methods for
   different kinds of events.
   <p>
   An object cookie ('active registration id') is returned as a result of a
   registration. Normally, the user must pass the cookie on deregistration.
   Some objects, e.g. those that do not allow more than one registration, might
   not use the registration id in which case the user will not be required to
   remember it.
   <p>
   An object that supports activation of one service will typically implement
   <code>activate(Notifiable, ActiveUID)</code>. Activation of multiple services
   must be implemented by <code>activate(int, Notifiable, ActiveUID)</code>,
   with object-defined service identifiers.
   
   @author Patrick Verkaik
*/

public interface Active
{
   /**
      Registers a <code>Notifiable</code> interface. In doing so, the user and
      the object exchange cookies. This method may be invoked by callback
      methods.


      @param note       the <code>Notifiable</code> interface to register
      @param userInfo   the user's cookie
      @return           the object's cookie
   */
   ActiveRID activate (Notifiable note, ActiveUID userInfo);

   /**
      Registers a <code>Notifiable</code> interface for a particular service
      provided by the active object. In doing so, the user and the object
      exchange cookies. This method may be invoked by callback methods.

      @param service    an object-defined service identifier
      @param note       the <code>Notifiable</code> interface to register
      @param userInfo   the user's cookie
      @return           the object's cookie
   */
   ActiveRID activate (int service, Notifiable note, ActiveUID userInfo);

   /**
      Unregisters a <code>Notifiable</code> interface. The user is normally 
      required to pass the registration id obtained when the interface was
      registered by activate(). However, this requirement may be removed by
      some objects. Note that callbacks are not guaranteed to have ceased on
      return of this method. This method may be invoked by callback methods.

      @param      an object-defined service identifier
   */
   void deactivate (ActiveRID regID);
}
