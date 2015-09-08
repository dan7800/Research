/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver.gclient;

import vu.globe.idlsys.g;
import vu.globe.svcs.gls.active.ActiveUID;

/**
   Utility class to convert an IDL active user cookie to a legacy
   <code>ActiveUID</code>.

   @author Patrick Verkaik
*/

class LegacyActiveUID implements ActiveUID
{
   public g.opaque user_info;

   private LegacyActiveUID (g.opaque user_info)
   {
      this.user_info = user_info;
   }

   /**
      Returns an appropriate <code>LegacyActiveUID</code> for a given
      IDL active uid. Special care is taken that a null IDL uid is mapped to
      a null legacy uid.
   */
   static public LegacyActiveUID getLegacyActiveUID (g.opaque user_info)
   {
      return user_info == null ? null : new LegacyActiveUID (user_info);
   }
}
