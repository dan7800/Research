/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver;

import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.active.*;
import java.io.IOException;

/**
   The <code>ActiveResolverOps</code> interface specifies the location service
   update and look-up operations. Methods that initiate an operation do
   not wait for its completion. The user must make separate calls to find out
   about the state of an operation. In addition, the user may be notified
   through a callback. Multiple users are assumed. Users can install separate
   callbacks. There is currently no way to interrupt an initiated operation.
   <p>
   Users can enable zero or more services on update operations. Currently
   two such services are defined: information as to whether the local domain
   has been updated, and information as to whether the entire update has
   completed. Services may be queried by the user to examine their state.
   <p>
   A service will perform a callback to inform the user that the state of the
   service has changed, or that an exception has occurred. The user may then
   decide to query the state. In the case of an exception, the exception
   will be thrown at this point.
   <p>
   A look-up operation has some state consisting of the results of the look-up
   (if any). The user can collect the results after initiating the operation.
   The user will be notified when the results arrive, or when an exception
   occurs. Again, in the case of an exception, the exception will be thrown at
   this point.
 
   @author Patrick Verkaik
*/

public interface ActiveResolverOps
{
   /**
      Service: report whether an update has been performed in the local
      domain.
   */
   static final int LOCALLY_UPDATED = 0;

   /**
      Service: report whether an update has become visible throughout the
      location service.
   */
   static final int FULLY_UPDATED = 1;

   static final int MAX_SERVICE = 1;
   static final int NSERVICES = MAX_SERVICE + 1;

   /**
      Inserts an object's contact address. In doing so, the user and the object
      exchange cookies. This method may be invoked by callback methods.
      <p>
      The <code>services</code> parameter is an array of booleans to enable
      services. <code>services [t]</code> enables service 't' if it is true.
      <p>
      A single registration id is returned representing this operation.

      @param   ohandle  the object to which the address belongs
      @param   caddr    the address to insert
      @param   note     the callback interface to use for notification. No
                        notification will occur if null is passed.
      @param   userInfo a user-defined cookie to use for notification.
      @param   services an array of booleans selecting services
      @return  the object's registration id representing this operation.
   */
   ActiveRID insert (ObjectHandle ohandle, ContactAddress caddr,
                     Notifiable note, ActiveUID userInfo, boolean [] services)
                     throws IOException;

   /**
      Checks the progress of an insert operation, identified by its registration
      ID. The array returned describes the state of the services, with
      <code>result [t]</code> being true iff service 't' was enabled and has
      completed. This method is carried out without delaying, and may be
      invoked by callback methods.

      @param regID   the registration id obtained when the operation
                     was initiated
      @return        the completed, enabled services
      @exception LocatorException
         if the location service node could not complete the operation
   */
   boolean [] assessInsert (ActiveRID regID) throws IOException;

   /**
      Deletes a contact address. In doing so, the user and the object exchange
      cookies. This method may be invoked by callback methods.
      <p>
      The <code>services</code> parameter is an array of booleans to enable
      services. <code>services [t]</code> enables service 't' if it is true.
      <p>
      A single registration id is returned representing this operation.

      @param   ohandle  the object to which the address belongs
      @param   caddr    the address to delete
      @param   note     the callback interface to use for notification. No
                        notification will occur if null is passed.
      @param   userInfo a user-defined cookie to use for notification.
      @param   services an array of booleans selecting services
      @return  the object's registration id representing this operation.
   */
   ActiveRID delete (ObjectHandle ohandle, ContactAddress caddr,
                     Notifiable note, ActiveUID userInfo, boolean [] services)
                     throws IOException;

   /**
      Checks the progress of a delete operation, identified by its registration
      ID. The array returned describes the state of the services, with
      <code>result [t]</code> being true iff service 't' was enabled and has
      completed. This method is carried out without delaying, and may be
      invoked by callback methods.

      @param regID   the registration id obtained when the operation
                     was initiated
      @return        the completed, enabled services
      @exception LocatorException
         if the location service node could not complete the operation
   */
   boolean [] assessDelete (ActiveRID regID) throws IOException;

   /**
      Tests contact address deletion in the location service. In doing so, the
      user and the object exchange cookies. This method may be invoked by
      callback methods.
      <p>
      This is an operation defined by
      the location service to test whether a previous <code>delete</code>
      operation has taken effect. It is used for debugging.
      <p>
      The <code>services</code> parameter is an array of booleans to enable
      services. <code>services [t]</code> enables service 't' if it is true.
      <p>
      A single registration id is returned representing this operation.

      @param   ohandle  the object to which the address belongs
      @param   caddr    the address to test
      @param   note     the callback interface to use for notification. No
                        notification will occur if null is passed.
      @param   userInfo a user-defined cookie to use for notification.
      @param   services an array of booleans selecting services
      @return  the object's registration id representing this operation.
   */
   ActiveRID testDelete (ObjectHandle ohandle, ContactAddress caddr,
               Notifiable note, ActiveUID userInfo, boolean [] services)
               throws IOException;

   /**
      Checks the progress of a test delete operation, identified by its
      registration ID. The array returned describes the state of the services,
      with <code>result [t]</code> being true iff service 't' was enabled and
      has completed. This method is carried out without delaying, and may be
      invoked by callback methods.

      @param regID   the registration id obtained when the operation
                     was initiated
      @return        the completed, enabled services

      @exception TestFailedException
         if the test delete failed
      @exception LocatorException
         if the location service node could not complete the operation
   */
   boolean [] assessTestDelete (ActiveRID regID) throws IOException;

   /**
      Tests contact address insertion in the location service. In doing so, the
      user and the object exchange cookies. This method may be invoked by
      callback methods.
      <p>
      This is an operation defined by
      the location service to test whether a previous <code>insert</code>
      operation has taken effect. It is used for debugging.
      <p>
      The <code>services</code> parameter is an array of booleans to enable
      services. <code>services [t]</code> enables service 't' if it is true.
      <p>
      A single registration id is returned representing this operation.

      @param   ohandle  the object to which the address belongs
      @param   caddr    the address to test
      @param   note     the callback interface to use for notification. No
                        notification will occur if null is passed.
      @param   userInfo a user-defined cookie to use for notification.
      @param   services an array of booleans selecting services
      @return  the object's registration id representing this operation.
   */
   ActiveRID testInsert (ObjectHandle ohandle, ContactAddress caddr,
               Notifiable note, ActiveUID userInfo, boolean [] services)
               throws IOException;

   /**
      Checks the progress of a test insert operation, identified by its
      registration ID. The array returned describes the state of the services,
      with <code>result [t]</code> being true iff service 't' was enabled and
      has completed. This method is carried out without delaying, and may be
      invoked by callback methods.

      @param regID   the registration id obtained when the operation
                     was initiated
      @return        the completed, enabled services

      @exception TestFailedException
         if the test insert failed
      @exception LocatorException
         if the location service node could not complete the operation
   */
   boolean [] assessTestInsert (ActiveRID regID) throws IOException;

   /**
      Looks up a number of contact addresses based on a query. In doing so,
      the user and the object exchange cookies. As a pre-condition, the query
      must not be empty. This method may be invoked by callback methods.

      @param query      the query to perform
      @param min        a desired minimum number of contact addresses to return;
                        <code>-1</code> for all available addresses
      @param max        a maximum number of contact addresses to return;
                        <code>-1</code> for all available addresses
      @param note       The callback interface to use for notification. No
                        notification will occur if null is passed.
      @param userInfo   a user-defined cookie to use for notification.
      @return           the object's registration id.
   */
   ActiveRID lookup (Query query, int min, int max, Notifiable note,
                     ActiveUID userInfo) throws IOException;

   /**
      Collects the results of a look-up operation. The look-up operation is
      identified by passing the registration ID originally returned by the
      look-up operation. This method is carried out without delaying, and may
      be invoked by callback methods.
      <p>
      Unless the query contained duplicate object handles, each returned
      contact address will appear only once per object handle.
      The result will never exceed <code>max</code> passed to the look-up
      operation.

      @param regID   The registration id obtained  when the look-up operation
                     was initiated.
      @return     New contact addresses resulting from the query. Returns null
                  if the results are not yet available.
      @exception LocatorException
         if the location service node could not complete the operation
   */
   ObjectCAddr [] lookupResults (ActiveRID regID) throws IOException;
}
