/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/*
The RSD Frontend Server
Copyright (C) 2000  Ihor Kuz (ikuz@cs.vu.nl) - All Rights Reserved

based on code from the Java LDAP Server
Copyright (C) 2000  Clayton Donley (donley@linc-dev.com) - All Rights Reserved

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package vu.globe.svcs.gids.rsd;

/**
 * MessageHandlers involve some heavy setup (BER encoder/decoder, Connection
 * object, etc...), so we pool them using the MessageHandlerPool.
 *
 * @author: Clayton Donley
 */
class MessageHandlerPool extends vu.globe.svcs.gids.rsd.util.ObjectPool {
        private static MessageHandlerPool mhp = null;

        /**
         * ConnectionHandlerPool constructor comment.
         */
        private MessageHandlerPool() {
                super();
        }
        /**
         * create method comment.
         */
        public Object create() throws Exception {
                return new MessageHandler();
        }
        /**
         * expire method comment.
         */
        public void expire(Object o) {
        }
        public static MessageHandlerPool getInstance() {
                if (mhp == null) {
                        mhp = new MessageHandlerPool();
                }
                return mhp;
        }
        /**
         * validate method comment.
         */
        public boolean validate(Object o) {
                return false;
        }
}




