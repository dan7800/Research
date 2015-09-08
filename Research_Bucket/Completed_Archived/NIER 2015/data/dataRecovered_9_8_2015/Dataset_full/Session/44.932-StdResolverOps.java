/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver;

import vu.globe.svcs.gls.alert.*;
import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.types.*;
import java.io.IOException;

/**
   The <code>StdResolverOps</code> interface specifies the standard location
   service update and look-up operations.
   <p>
   This interface provides for calls to be alerted, and update operations to be
   passed a user cookie for notification. Whether and how alerting and
   activation are used depends on the object implementing this interface.
   <p>
   Unless specified otherwise, for the purpose of deadlock avoidance these
   operations should be considered
   blocking operations. They should not be invoked or waited for by callback
   methods or by non-blocking methods, or deadlock may ensue.
   <p>
   The interface is a compromise between flexibility and ease of use: although
   user cookies can be passed, they cannot be associated with independent
   'active' registrations. In effect, a single user can be notified. A more
   sophisticated interface would instead allow independent registrations for
   multiple users. On
   the other hand, a simpler interface would not take account of any
   notification features, and would perhaps not even allow calls to be alerted.
 
   @author Patrick Verkaik
*/

public interface StdResolverOps
{
   /**
      Inserts an object's contact address. Note: unless otherwise specified,
      this is a blocking method.

      @param ohandle    The object to which the address belongs.
      @param caddr      The address to insert.
      @param userInfo   A user-defined cookie used for notification.  No
                        notification will take place if null is passed.
      @param id         Unless null, an alert id identifying this call. It is
                        ignored by non-alertable objects.
      @exception        LocatorException
         if the location service node could not complete the operation
   */
   void insert (ObjectHandle ohandle, ContactAddress caddr, ActiveUID userInfo,
                AlertID id) throws IOException;

   /**
      Deletes an object's contact address. Note: unless otherwise specified,
      this is a blocking method.

      @param ohandle    The object to which the address belongs.
      @param caddr      The address to delete.
      @param userInfo   A user-defined cookie used for notification.  No
                        notification will take place if null is passed.
      @param id         Unless null, an alert id identifying this call. It is
                        ignored by non-alertable objects.
      @exception        LocatorException
         if the location service node could not complete the operation
   */
   void delete (ObjectHandle ohandle, ContactAddress caddr, ActiveUID userInfo,
                AlertID id) throws IOException;

   /**
      Tests contact address deletion in the location service. Note: unless
      otherwise specified, this is a blocking method.

      @param ohandle    The object to which the address belongs.
      @param caddr      The address to test.
      @param userInfo   A user-defined cookie used for notification.  No
                        notification will take place if null is passed.
      @param id         Unless null, an alert id identifying this call. It is
                        ignored by non-alertable objects.

      @exception TestFailedException
         if the test delete failed
      @exception        LocatorException
         if the location service node could not complete the operation
   */
   void testDelete (ObjectHandle ohandle, ContactAddress caddr,
                  ActiveUID userInfo, AlertID id) throws IOException;

   /**
      Tests contact address insertion in the location service. Note: unless
      otherwise specified, this is a blocking method.

      @param ohandle    The object to which the address belongs.
      @param caddr      The address to test.
      @param userInfo   A user-defined cookie used for notification.  No
                        notification will take place if null is passed.
      @param id         Unless null, an alert id identifying this call. It is
                        ignored by non-alertable objects.

      @exception TestFailedException
         if the test insert failed
      @exception        LocatorException
         if the location service node could not complete the operation
   */
   void testInsert (ObjectHandle ohandle, ContactAddress caddr,
                  ActiveUID userInfo, AlertID id) throws IOException;


   /**
      Looks up a number of contact addresses based on a query. Returns new
      contact addresses resulting from the query. Unless the query contained
      duplicate object handles, each returned contact address will appear only
      once per object handle. The result will never exceed <code>max</code>.
      Note: unless otherwise specified, this is a blocking method.

      @param query      The query to perform.
      @param min        A desired minimum number of contact addresses to return;
                        <code>-1</code> for all available addresses
      @param max        A maximum number of contact addresses to return;
                        <code>-1</code> for all available addresses
      @param id         Unless null, an alert id identifying this call. It is
                        ignored by non-alertable objects.
      @return     New contact addresses resulting from the query.
      @exception  LocatorException
         if the location service node could not complete the operation
   */
   ObjectCAddr [] lookup (Query query, int min, int max, AlertID id)
                           throws IOException;
}
