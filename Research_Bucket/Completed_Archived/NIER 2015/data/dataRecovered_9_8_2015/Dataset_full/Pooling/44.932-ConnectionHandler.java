/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/*
The The RSD Frontend Server
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

/**
 * The ConnectionHandler is spawned when a new connection arrives. It 
 * retrieves a suitable MessageHandler from the pool and uses it to parse 
 * incoming messages.  It also makes a connection to the RSD and waits for
 * messages from it.
 *
 * @author: Clayton Donley, Ihor Kuz
 */

package vu.globe.svcs.gids.rsd;

import java.net.*;
import java.io.*;
import java.util.*;
import netscape.ldap.LDAPMessage;

public class ConnectionHandler extends Thread {
        private MessageHandler msgHandler = null;
        private boolean request = true;

        public ConnectionHandler(Socket client) throws Exception {

                client.setTcpNoDelay(true);
                msgHandler = (MessageHandler)MessageHandlerPool.getInstance().
                  checkOut();
                msgHandler.reset();
                msgHandler.getRSDConnection().setClient(client);
                msgHandler.getRSDConnection().setDebug(false);
		try {
                  msgHandler.connectLDAP();
		}
		catch (IOException exc) {
		  throw new IOException ("unable to contact LDAP server: " +
		     exc.getMessage());
		}

                msgHandler.getLDAPConnection().setDebug(false);
                setPriority(NORM_PRIORITY-1);
                request = true;
        }

        public ConnectionHandler(MessageHandler msg, boolean request) {
                msgHandler = msg;
                setPriority(NORM_PRIORITY-1);
                this.request = request;
        }


        public void setRequest(boolean request) {
                this.request = request;
        }
        public void setMsgHandler(MessageHandler m) {
                this.msgHandler = m;
        }


        public boolean getRequest() {
                return this.request;
        }
        public MessageHandler getMsgHandler() {
                return msgHandler;
        }


        public void run() {

                boolean continueSession = true;

                while (continueSession == true) {
                        LDAPMessage msg = null;
                        if (request) {
                                msg = msgHandler.getNextRequest();
                                if (msg != null) {
                                    continueSession = msgHandler.
                                            answerRequest(msg);
                                } else {
                                        continueSession = false;
                                        msgHandler.resetRSD();
                                        msgHandler.resetLDAP();
                                }               
                        } else {
                                msg = msgHandler.getNextResponse();
                                if (msg != null) {
                                        continueSession = msgHandler.
                                            sendResponse(msg);
                                } else {
                                        continueSession = false;
                                        msgHandler.resetLDAP();
                                }               
                        }
                        msg = null;
                }

                MessageHandlerPool.getInstance().checkIn(msgHandler);
        }
}







