/**
 *   Arsenal Real-Time Collaboration Server Project
 *   Copyright (C) 2003  Michael Burnside, Arsenal Project
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * ***************************************************************** 
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/chat/src/com/arsenal/chat/client/ChatClientHandler.java,v 1.1.1.1 2005/03/26 17:12:31 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: ChatClientHandler.java $ 
 *
 *    Description: 
 *
 *    The message handler for the client plugin
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import javax.swing.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;
import java.io.File;

import com.arsenal.session.message.*;
import com.arsenal.log.Log;
import com.arsenal.observer.IHandler;
import com.arsenal.message.*;
import com.arsenal.session.*;
import com.arsenal.client.Client;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.observer.*;
import com.arsenal.chat.client.panels.*;
import com.arsenal.user.client.panels.UserPanel;
import com.arsenal.user.UserBean;
import com.arsenal.chat.*;
import com.arsenal.client.observer.*;
import com.arsenal.user.client.panels.*;
import com.arsenal.user.client.*;
import com.arsenal.skin.*;

public class ChatClientHandler implements IHandler, LogoutObserver, UserSelectedObserver, UserUnselectedObserver, SkinChangeObserver, RemoveActiveUserObserver {
        
  private Hashtable privateChatClients = new Hashtable();
  private JButton privateChatWithUserButton = new JButton("Chat With User");

  private static ChatClientHandler instance = new ChatClientHandler();
 
  public static ChatClientHandler getInstance() { 
    if (instance == null) {
      instance = new ChatClientHandler();
    }
    return instance;
  }

  
  public ChatClientHandler() { 
    this.instance = this;
  }
   
  public void init() {
    Log.debug(this, "ChatClientHandler initing....");
    Client.getInstance().addToMainLeftPanelTop(ChatPanel.getInstance());
    UserPanel.getInstance().addToButtonPanel(privateChatWithUserButton);
    privateChatWithUserButton.addActionListener(new PrivateChatWithUserButtonListener());
    privateChatWithUserButton.setEnabled(false);
    registerLogoutListener(this);
    registerUserSelectedListener(this);
    registerUserUnselectedListener(this);
    registerSkinChangeListener(this);
    registerRemoveActiveUserListener(this);
  }

   public void handleMessage(IMessage message) {
    message.execute(); 
   }

   public void sendMessage(IMessage message) {
     Client.getInstance().sendMessage(message);
   }
   
  /*********************************************************************
   *
   * action event handlers
   *
   *********************************************************************/

  public void doLogoutAction() {  
    privateChatWithUserButton.setEnabled(false); 
    Enumeration e = privateChatClients.elements();
    while(e.hasMoreElements()) {
      PrivateChatWindow window = (PrivateChatWindow)e.nextElement();
      window.hide();
    }
    privateChatClients.clear();   
  }

  public void registerLogoutListener(LogoutObserver logoutObserver) {
    Client.getInstance().registerLogoutObserver(logoutObserver);
  }

  public void doUserSelectedAction(String name) {
    if(UserPanel.getInstance().getUserChosen().equals(ConnectionWindow.getInstance().getUsername()))
      privateChatWithUserButton.setEnabled(false);
    else
      privateChatWithUserButton.setEnabled(true);
  }

  public void registerUserSelectedListener(UserSelectedObserver userSelectedObserver) {
    UserPanel.getInstance().registerUserSelectedListener(userSelectedObserver);      
  }
  
  public void doUserUnselectedAction() { 
     privateChatWithUserButton.setEnabled(false);
  }

  public void registerUserUnselectedListener(UserUnselectedObserver userUnselectedObserver) {
    UserPanel.getInstance().registerUserUnselectedListener(userUnselectedObserver);       
  }

  public void doRemoveActiveUserAction(Object object) {
    UserBean bean = (UserBean)object;
    if(privateChatClients.get(bean.getName()) != null) {
      ChatBean chatbean = new ChatBean();
      chatbean.setFromUserName("Arsenal Server");
      chatbean.setChatMessage("The User you were chatting with has logged out.");
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).addNewChatMessage(chatbean);
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).disable();
      privateChatClients.remove(bean.getName());
      
    }
  }

  public void registerRemoveActiveUserListener(RemoveActiveUserObserver removeActiveUserObserver) {
    Client.getInstance().registerRemoveActiveUserObserver(removeActiveUserObserver);
  }

  public void doSkinChangeAction(Object object) {
    //SkinBean bean = (SkinBean)object; 
    //privateChatWithUserButton.setBackground(bean.getForegroundColor());
    //privateChatWithUserButton.setForeground(bean.getBackgroundColor());
  }

  public void registerSkinChangeListener(SkinChangeObserver skinchangeObserver) {
    Client.getInstance().registerSkinChangeObserver(skinchangeObserver);
  }
  
  /**********************************************************************
   *
   * methods for messages to use
   *
   **********************************************************************/
   
   public void addNewChatMessage(ChatBean bean) {
     ChatPanel.getInstance().addNewChatMessage(bean);
   }

   public void addNewPrivateChatMessage(ChatBean bean) {
     Log.debug(this, "addNewPrivateChatMessage(): new private chat sent from: " + bean.getFromUserName());
     if(bean == null) return;
     if((bean.getFromUserName() == null) || (bean.getToUserName() == null)) return;

     //if the message is from me, check to see if the to user private window already exists,
     // if it doesnt make one
     if(bean.getFromUserName().equals(ConnectionWindow.getInstance().getUsername())) {
       if(privateChatClients.get(bean.getToUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getToUserName(), new PrivateChatWindow(bean.getToUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getToUserName());
       }
       ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).addNewChatMessage(bean);
     }
     else { // it's not from us, so check if we have a window
       if(privateChatClients.get(bean.getFromUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getFromUserName(), new PrivateChatWindow(bean.getFromUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getFromUserName());
         Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
       }
     ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).addNewChatMessage(bean);
     }
   }

   public void prepareForPrivateChat() {
     Log.debug(this, "prepareForPrivateChat(): chat with this user: " + UserPanel.getInstance().getUserChosen()); 
     if(UserPanel.getInstance().getUserChosen() == null) return;
     if(privateChatClients.get(UserPanel.getInstance().getUserChosen()) == null) {
       //this is first time we chat with the user in this vm instance
       privateChatClients.put(UserPanel.getInstance().getUserChosen(), new PrivateChatWindow(UserPanel.getInstance().getUserChosen()));
       Log.debug(this, "prepareForPrivateChat(): new chat window added for user: " + UserPanel.getInstance().getUserChosen());
     }
     ((PrivateChatWindow)privateChatClients.get(UserPanel.getInstance().getUserChosen())).show();
   }
  
}/**
 *   Arsenal Real-Time Collaboration Server Project
 *   Copyright (C) 2003  Michael Burnside, Arsenal Project
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * ***************************************************************** 
 *    $Header: /cvsroot/arsenal-1/chat/src/com/arsenal/chat/client/ChatClientHandler.java,v 1.7 2005/10/19 05:22:12 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: ChatClientHandler.java $ 
 *
 *    Description: 
 *
 *    The message handler for the client plugin
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import javax.swing.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;
import java.io.File;

import com.arsenal.session.message.*;
import com.arsenal.session.client.*;
import com.arsenal.log.Log;
import com.arsenal.observer.IHandler;
import com.arsenal.message.*;
import com.arsenal.session.*;
import com.arsenal.client.Client;
import com.arsenal.client.MainPanel;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.observer.*;
import com.arsenal.chat.client.panels.*;
import com.arsenal.user.client.panels.UserPanel;
import com.arsenal.user.UserBean;
import com.arsenal.chat.*;
import com.arsenal.client.observer.*;
import com.arsenal.user.client.panels.*;
import com.arsenal.user.client.*;
import com.arsenal.skin.*;
import com.arsenal.util.*;

public class ChatClientHandler implements IHandler, LogoutObserver, UserSelectedObserver, UserUnselectedObserver, SkinChangeObserver, RemoveActiveUserObserver {
        
  private Hashtable privateChatClients = new Hashtable();
  //private JButton privateChatWithUserButton = new JButton("Chat With User");
  private AnimatedButton privateChatWithUserButton = new AnimatedButton("Chat With User", false, Color.black, false);

  private static ChatClientHandler instance = new ChatClientHandler();
 
  public static ChatClientHandler getInstance() { 
    if (instance == null) {
      instance = new ChatClientHandler();
    }
    return instance;
  }

  
  public ChatClientHandler() { 
    this.instance = this;
  }
   
  public void init() {
    Log.debug(this, "ChatClientHandler initing....");
    SessionFrame.getInstance().addToBottomPanel(ChatPanel.getInstance());
    UserPanel.getInstance().addToButtonPanel(privateChatWithUserButton);
    privateChatWithUserButton.getButton().addActionListener(new PrivateChatWithUserButtonListener());
    privateChatWithUserButton.setEnabled(false);
    registerLogoutListener(this);
    registerUserSelectedListener(this);
    registerUserUnselectedListener(this);
    registerSkinChangeListener(this);
    registerRemoveActiveUserListener(this);
    privateChatWithUserButton.setEnable(false);
  }

   public void handleMessage(IMessage message) {
    message.execute(); 
   }

   public void sendMessage(IMessage message) {
     Client.getInstance().sendMessage(message);
   }
   
  /*********************************************************************
   *
   * action event handlers
   *
   *********************************************************************/

  public void doLogoutAction() {  
    privateChatWithUserButton.setEnable(false); 
    Enumeration e = privateChatClients.elements();
    while(e.hasMoreElements()) {
      PrivateChatWindow window = (PrivateChatWindow)e.nextElement();
      window.hide();
    }
    privateChatClients.clear();   
  }

  public void registerLogoutListener(LogoutObserver logoutObserver) {
    Client.getInstance().registerLogoutObserver(logoutObserver);
  }

  public void doUserSelectedAction(String name) {
    if(UserPanel.getInstance().getUserChosen().equals(ConnectionWindow.getInstance().getUsername()))
      privateChatWithUserButton.setEnable(false);
    else
      privateChatWithUserButton.setEnable(true);
  }

  public void registerUserSelectedListener(UserSelectedObserver userSelectedObserver) {
    UserPanel.getInstance().registerUserSelectedListener(userSelectedObserver);      
  }
  
  public void doUserUnselectedAction() { 
     privateChatWithUserButton.setEnable(false);
  }

  public void registerUserUnselectedListener(UserUnselectedObserver userUnselectedObserver) {
    UserPanel.getInstance().registerUserUnselectedListener(userUnselectedObserver);       
  }

  public void doRemoveActiveUserAction(Object object) {
    UserBean bean = (UserBean)object;
    if(privateChatClients.get(bean.getName()) != null) {
      ChatBean chatbean = new ChatBean();
      chatbean.setFromUserName("Arsenal Server");
      chatbean.setChatMessage("The User you were chatting with has logged out.");
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).addNewChatMessage(chatbean);
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).disable();
      privateChatClients.remove(bean.getName());
      
    }
  }

  public void registerRemoveActiveUserListener(RemoveActiveUserObserver removeActiveUserObserver) {
    Client.getInstance().registerRemoveActiveUserObserver(removeActiveUserObserver);
  }

  public void doSkinChangeAction(Object object) {
    //SkinBean bean = (SkinBean)object; 
    //privateChatWithUserButton.setBackground(bean.getForegroundColor());
    //privateChatWithUserButton.setForeground(bean.getBackgroundColor());
  }

  public void registerSkinChangeListener(SkinChangeObserver skinchangeObserver) {
    Client.getInstance().registerSkinChangeObserver(skinchangeObserver);
  }
  
  /**********************************************************************
   *
   * methods for messages to use
   *
   **********************************************************************/
   
   public void addNewChatMessage(ChatBean bean) {
     ChatPanel.getInstance().addNewChatMessage(bean);
   }

   public void addNewPrivateChatMessage(ChatBean bean) {
     Log.debug(this, "addNewPrivateChatMessage(): new private chat sent from: " + bean.getFromUserName());
     if(bean == null) return;
     if((bean.getFromUserName() == null) || (bean.getToUserName() == null)) return;

     //if the message is from me, check to see if the to user private window already exists,
     // if it doesnt make one
     if(bean.getFromUserName().equals(ConnectionWindow.getInstance().getUsername())) {
       if(privateChatClients.get(bean.getToUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getToUserName(), new PrivateChatWindow(bean.getToUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getToUserName());
       }
       ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).addNewChatMessage(bean);
     }
     else { // it's not from us, so check if we have a window
       if(privateChatClients.get(bean.getFromUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getFromUserName(), new PrivateChatWindow(bean.getFromUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getFromUserName());
         Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
       }
     ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).addNewChatMessage(bean);
     }
   }

   public void prepareForPrivateChat() {
     Log.debug(this, "prepareForPrivateChat(): chat with this user: " + UserPanel.getInstance().getUserChosen()); 
     if(UserPanel.getInstance().getUserChosen() == null) return; //if a user is selected
     if(UserList.getInstance().get(UserPanel.getInstance().getUserChosen()) == null) return; //the user is not active
     if(privateChatClients.get(UserPanel.getInstance().getUserChosen()) == null) {
       //this is first time we chat with the user in this vm instance
       privateChatClients.put(UserPanel.getInstance().getUserChosen(), new PrivateChatWindow(UserPanel.getInstance().getUserChosen()));
       Log.debug(this, "prepareForPrivateChat(): new chat window added for user: " + UserPanel.getInstance().getUserChosen());
     }
     ((PrivateChatWindow)privateChatClients.get(UserPanel.getInstance().getUserChosen())).show();
   }
  
}/**
 *   Arsenal Real-Time Collaboration Server Project
 *   Copyright (C) 2003  Michael Burnside, Arsenal Project
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * ***************************************************************** 
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/chat/src/com/arsenal/chat/client/ChatClientHandler.java,v 1.1.1.1 2005/03/26 17:12:31 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: ChatClientHandler.java $ 
 *
 *    Description: 
 *
 *    The message handler for the client plugin
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import javax.swing.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;
import java.io.File;

import com.arsenal.session.message.*;
import com.arsenal.log.Log;
import com.arsenal.observer.IHandler;
import com.arsenal.message.*;
import com.arsenal.session.*;
import com.arsenal.client.Client;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.observer.*;
import com.arsenal.chat.client.panels.*;
import com.arsenal.user.client.panels.UserPanel;
import com.arsenal.user.UserBean;
import com.arsenal.chat.*;
import com.arsenal.client.observer.*;
import com.arsenal.user.client.panels.*;
import com.arsenal.user.client.*;
import com.arsenal.skin.*;

public class ChatClientHandler implements IHandler, LogoutObserver, UserSelectedObserver, UserUnselectedObserver, SkinChangeObserver, RemoveActiveUserObserver {
        
  private Hashtable privateChatClients = new Hashtable();
  private JButton privateChatWithUserButton = new JButton("Chat With User");

  private static ChatClientHandler instance = new ChatClientHandler();
 
  public static ChatClientHandler getInstance() { 
    if (instance == null) {
      instance = new ChatClientHandler();
    }
    return instance;
  }

  
  public ChatClientHandler() { 
    this.instance = this;
  }
   
  public void init() {
    Log.debug(this, "ChatClientHandler initing....");
    Client.getInstance().addToMainLeftPanelTop(ChatPanel.getInstance());
    UserPanel.getInstance().addToButtonPanel(privateChatWithUserButton);
    privateChatWithUserButton.addActionListener(new PrivateChatWithUserButtonListener());
    privateChatWithUserButton.setEnabled(false);
    registerLogoutListener(this);
    registerUserSelectedListener(this);
    registerUserUnselectedListener(this);
    registerSkinChangeListener(this);
    registerRemoveActiveUserListener(this);
  }

   public void handleMessage(IMessage message) {
    message.execute(); 
   }

   public void sendMessage(IMessage message) {
     Client.getInstance().sendMessage(message);
   }
   
  /*********************************************************************
   *
   * action event handlers
   *
   *********************************************************************/

  public void doLogoutAction() {  
    privateChatWithUserButton.setEnabled(false); 
    Enumeration e = privateChatClients.elements();
    while(e.hasMoreElements()) {
      PrivateChatWindow window = (PrivateChatWindow)e.nextElement();
      window.hide();
    }
    privateChatClients.clear();   
  }

  public void registerLogoutListener(LogoutObserver logoutObserver) {
    Client.getInstance().registerLogoutObserver(logoutObserver);
  }

  public void doUserSelectedAction(String name) {
    if(UserPanel.getInstance().getUserChosen().equals(ConnectionWindow.getInstance().getUsername()))
      privateChatWithUserButton.setEnabled(false);
    else
      privateChatWithUserButton.setEnabled(true);
  }

  public void registerUserSelectedListener(UserSelectedObserver userSelectedObserver) {
    UserPanel.getInstance().registerUserSelectedListener(userSelectedObserver);      
  }
  
  public void doUserUnselectedAction() { 
     privateChatWithUserButton.setEnabled(false);
  }

  public void registerUserUnselectedListener(UserUnselectedObserver userUnselectedObserver) {
    UserPanel.getInstance().registerUserUnselectedListener(userUnselectedObserver);       
  }

  public void doRemoveActiveUserAction(Object object) {
    UserBean bean = (UserBean)object;
    if(privateChatClients.get(bean.getName()) != null) {
      ChatBean chatbean = new ChatBean();
      chatbean.setFromUserName("Arsenal Server");
      chatbean.setChatMessage("The User you were chatting with has logged out.");
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).addNewChatMessage(chatbean);
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).disable();
      privateChatClients.remove(bean.getName());
      
    }
  }

  public void registerRemoveActiveUserListener(RemoveActiveUserObserver removeActiveUserObserver) {
    Client.getInstance().registerRemoveActiveUserObserver(removeActiveUserObserver);
  }

  public void doSkinChangeAction(Object object) {
    //SkinBean bean = (SkinBean)object; 
    //privateChatWithUserButton.setBackground(bean.getForegroundColor());
    //privateChatWithUserButton.setForeground(bean.getBackgroundColor());
  }

  public void registerSkinChangeListener(SkinChangeObserver skinchangeObserver) {
    Client.getInstance().registerSkinChangeObserver(skinchangeObserver);
  }
  
  /**********************************************************************
   *
   * methods for messages to use
   *
   **********************************************************************/
   
   public void addNewChatMessage(ChatBean bean) {
     ChatPanel.getInstance().addNewChatMessage(bean);
   }

   public void addNewPrivateChatMessage(ChatBean bean) {
     Log.debug(this, "addNewPrivateChatMessage(): new private chat sent from: " + bean.getFromUserName());
     if(bean == null) return;
     if((bean.getFromUserName() == null) || (bean.getToUserName() == null)) return;

     //if the message is from me, check to see if the to user private window already exists,
     // if it doesnt make one
     if(bean.getFromUserName().equals(ConnectionWindow.getInstance().getUsername())) {
       if(privateChatClients.get(bean.getToUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getToUserName(), new PrivateChatWindow(bean.getToUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getToUserName());
       }
       ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).addNewChatMessage(bean);
     }
     else { // it's not from us, so check if we have a window
       if(privateChatClients.get(bean.getFromUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getFromUserName(), new PrivateChatWindow(bean.getFromUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getFromUserName());
         Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
       }
     ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).addNewChatMessage(bean);
     }
   }

   public void prepareForPrivateChat() {
     Log.debug(this, "prepareForPrivateChat(): chat with this user: " + UserPanel.getInstance().getUserChosen()); 
     if(UserPanel.getInstance().getUserChosen() == null) return;
     if(privateChatClients.get(UserPanel.getInstance().getUserChosen()) == null) {
       //this is first time we chat with the user in this vm instance
       privateChatClients.put(UserPanel.getInstance().getUserChosen(), new PrivateChatWindow(UserPanel.getInstance().getUserChosen()));
       Log.debug(this, "prepareForPrivateChat(): new chat window added for user: " + UserPanel.getInstance().getUserChosen());
     }
     ((PrivateChatWindow)privateChatClients.get(UserPanel.getInstance().getUserChosen())).show();
   }
  
}/**
 *   Arsenal Real-Time Collaboration Server Project
 *   Copyright (C) 2003  Michael Burnside, Arsenal Project
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * ***************************************************************** 
 *    $Header: /cvsroot/arsenal-1/chat/src/com/arsenal/chat/client/ChatClientHandler.java,v 1.4 2005/08/22 03:24:33 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: ChatClientHandler.java $ 
 *
 *    Description: 
 *
 *    The message handler for the client plugin
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import javax.swing.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;
import java.io.File;

import com.arsenal.session.message.*;
import com.arsenal.session.client.*;
import com.arsenal.log.Log;
import com.arsenal.observer.IHandler;
import com.arsenal.message.*;
import com.arsenal.session.*;
import com.arsenal.client.Client;
import com.arsenal.client.MainPanel;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.observer.*;
import com.arsenal.chat.client.panels.*;
import com.arsenal.user.client.panels.UserPanel;
import com.arsenal.user.UserBean;
import com.arsenal.chat.*;
import com.arsenal.client.observer.*;
import com.arsenal.user.client.panels.*;
import com.arsenal.user.client.*;
import com.arsenal.skin.*;
import com.arsenal.util.*;

public class ChatClientHandler implements IHandler, LogoutObserver, UserSelectedObserver, UserUnselectedObserver, SkinChangeObserver, RemoveActiveUserObserver {
        
  private Hashtable privateChatClients = new Hashtable();
  //private JButton privateChatWithUserButton = new JButton("Chat With User");
  private AnimatedButton privateChatWithUserButton = new AnimatedButton("Chat With User", false, Color.black, true);

  private static ChatClientHandler instance = new ChatClientHandler();
 
  public static ChatClientHandler getInstance() { 
    if (instance == null) {
      instance = new ChatClientHandler();
    }
    return instance;
  }

  
  public ChatClientHandler() { 
    this.instance = this;
  }
   
  public void init() {
    Log.debug(this, "ChatClientHandler initing....");
    SessionFrame.getInstance().addToBottomPanel(ChatPanel.getInstance());
    MainPanel.getInstance().addButtonToLeftPanel(privateChatWithUserButton);
    privateChatWithUserButton.getButton().addActionListener(new PrivateChatWithUserButtonListener());
    privateChatWithUserButton.setEnabled(false);
    registerLogoutListener(this);
    registerUserSelectedListener(this);
    registerUserUnselectedListener(this);
    registerSkinChangeListener(this);
    registerRemoveActiveUserListener(this);
    privateChatWithUserButton.setEnable(false);
  }

   public void handleMessage(IMessage message) {
    message.execute(); 
   }

   public void sendMessage(IMessage message) {
     Client.getInstance().sendMessage(message);
   }
   
  /*********************************************************************
   *
   * action event handlers
   *
   *********************************************************************/

  public void doLogoutAction() {  
    privateChatWithUserButton.setEnable(false); 
    Enumeration e = privateChatClients.elements();
    while(e.hasMoreElements()) {
      PrivateChatWindow window = (PrivateChatWindow)e.nextElement();
      window.hide();
    }
    privateChatClients.clear();   
  }

  public void registerLogoutListener(LogoutObserver logoutObserver) {
    Client.getInstance().registerLogoutObserver(logoutObserver);
  }

  public void doUserSelectedAction(String name) {
    if(UserPanel.getInstance().getUserChosen().equals(ConnectionWindow.getInstance().getUsername()))
      privateChatWithUserButton.setEnable(false);
    else
      privateChatWithUserButton.setEnable(true);
  }

  public void registerUserSelectedListener(UserSelectedObserver userSelectedObserver) {
    UserPanel.getInstance().registerUserSelectedListener(userSelectedObserver);      
  }
  
  public void doUserUnselectedAction() { 
     privateChatWithUserButton.setEnable(false);
  }

  public void registerUserUnselectedListener(UserUnselectedObserver userUnselectedObserver) {
    UserPanel.getInstance().registerUserUnselectedListener(userUnselectedObserver);       
  }

  public void doRemoveActiveUserAction(Object object) {
    UserBean bean = (UserBean)object;
    if(privateChatClients.get(bean.getName()) != null) {
      ChatBean chatbean = new ChatBean();
      chatbean.setFromUserName("Arsenal Server");
      chatbean.setChatMessage("The User you were chatting with has logged out.");
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).addNewChatMessage(chatbean);
      ((PrivateChatWindow)privateChatClients.get(bean.getName())).disable();
      privateChatClients.remove(bean.getName());
      
    }
  }

  public void registerRemoveActiveUserListener(RemoveActiveUserObserver removeActiveUserObserver) {
    Client.getInstance().registerRemoveActiveUserObserver(removeActiveUserObserver);
  }

  public void doSkinChangeAction(Object object) {
    //SkinBean bean = (SkinBean)object; 
    //privateChatWithUserButton.setBackground(bean.getForegroundColor());
    //privateChatWithUserButton.setForeground(bean.getBackgroundColor());
  }

  public void registerSkinChangeListener(SkinChangeObserver skinchangeObserver) {
    Client.getInstance().registerSkinChangeObserver(skinchangeObserver);
  }
  
  /**********************************************************************
   *
   * methods for messages to use
   *
   **********************************************************************/
   
   public void addNewChatMessage(ChatBean bean) {
     ChatPanel.getInstance().addNewChatMessage(bean);
   }

   public void addNewPrivateChatMessage(ChatBean bean) {
     Log.debug(this, "addNewPrivateChatMessage(): new private chat sent from: " + bean.getFromUserName());
     if(bean == null) return;
     if((bean.getFromUserName() == null) || (bean.getToUserName() == null)) return;

     //if the message is from me, check to see if the to user private window already exists,
     // if it doesnt make one
     if(bean.getFromUserName().equals(ConnectionWindow.getInstance().getUsername())) {
       if(privateChatClients.get(bean.getToUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getToUserName(), new PrivateChatWindow(bean.getToUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getToUserName());
       }
       ((PrivateChatWindow)privateChatClients.get(bean.getToUserName())).addNewChatMessage(bean);
     }
     else { // it's not from us, so check if we have a window
       if(privateChatClients.get(bean.getFromUserName()) == null) { // doesnt exists
         privateChatClients.put(bean.getFromUserName(), new PrivateChatWindow(bean.getFromUserName()));
         ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).show();
         Log.debug(this, "addNewPrivateChatMessage(): new chat window added for user: " + bean.getFromUserName());
         Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
       }
     ((PrivateChatWindow)privateChatClients.get(bean.getFromUserName())).addNewChatMessage(bean);
     }
   }

   public void prepareForPrivateChat() {
     Log.debug(this, "prepareForPrivateChat(): chat with this user: " + UserPanel.getInstance().getUserChosen()); 
     if(UserPanel.getInstance().getUserChosen() == null) return;
     if(privateChatClients.get(UserPanel.getInstance().getUserChosen()) == null) {
       //this is first time we chat with the user in this vm instance
       privateChatClients.put(UserPanel.getInstance().getUserChosen(), new PrivateChatWindow(UserPanel.getInstance().getUserChosen()));
       Log.debug(this, "prepareForPrivateChat(): new chat window added for user: " + UserPanel.getInstance().getUserChosen());
     }
     ((PrivateChatWindow)privateChatClients.get(UserPanel.getInstance().getUserChosen())).show();
   }
  
}
