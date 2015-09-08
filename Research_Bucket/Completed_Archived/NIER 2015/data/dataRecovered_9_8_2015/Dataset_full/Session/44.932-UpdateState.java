/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver.stdclient.proto;

import vu.globe.svcs.gls.active.ActiveUID;
 
/**
   The state of a resolver client update operation.
   Apart from waking up the user thread (see <code>OperationState</code>), an
   updating user of a client can optionally be notified through a callback.
   This can occur at most once.
 
   @author Patrick Verkaik
*/

class UpdateState extends OperationState
{
   private int opcode;     // always set
   private ActiveUID user_info; // null if left undefined

   /**
      Constructor.
      @param opcode  one of <code>ProtOperation</code>'s resolver update opcodes
   */
   public UpdateState (int opcode)
   {
      this.opcode = opcode;
   }

   /**
      Returns the opcode set earlier by the user.
   */
   public int getOpCode ()
   {
      return opcode;
   }

   /**
      Sets the user cookie for notification following an update operation.
      Need not be called if no such notification is desired. May be called
      more than once.

      @param user_info  a user cookie, or null if no notification is desired.
   */
   public void setUID (ActiveUID user_info)
   {
      this.user_info = user_info;
   }

   /**
      Retrieves the user cookie for user notification.

      @return  a user cookie, or null if no notification is desired.
   */
   public ActiveUID getUid ()
   {
      return user_info;
   }
}
