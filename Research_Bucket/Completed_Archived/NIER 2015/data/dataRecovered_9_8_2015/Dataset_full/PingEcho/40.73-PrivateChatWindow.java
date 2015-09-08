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
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/chat/src/com/arsenal/chat/client/PrivateChatWindow.java,v 1.1.1.1 2005/03/26 17:12:31 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: PrivateChatWindow.java $ 
 *
 *    Description: 
 *
 *    Created as a window such that the User can see and send chat sent directly
 *    to/from the specified user.
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Date;

import com.arsenal.chat.*;
import com.arsenal.log.Log;
import com.arsenal.chat.message.*;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.Client;

public class PrivateChatWindow extends JFrame {
  
   private String userChattingWith = null;
   private JLabel inputLabel = new JLabel("To User: ");
   private JPanel inputPanel = new JPanel();
   private JButton button = new JButton("Send Message");
   private JPanel buttonPanel = new JPanel();
   private JTextField inputField = new JTextField(22);
   private JPanel splitPanel = new JPanel();
   private JTextPane textArea = new JTextPane();
   private JScrollPane scrollPane = new JScrollPane(textArea);
   private JViewport vp = scrollPane.getViewport();
   private StyledDocument doc = textArea.getStyledDocument();
   private HashMap styleMap = new HashMap();
   private String[] equalsChars = { "pie", "mad", "startled", "shoot", "beer", "flower", "lol", "shot", "sleep", "worried", "wow", "idea", "??", "happy", "cheese", "smile",
       "lmfao", "hello", "hi", "hungry", "eat", "shocked", "suprised", "weird" };
   private String[] expChars = { ":)", ":^)", ":(", ":^(", ":|" }; 
   private String[] likeChars = { "nooo", "ahhh" };

   private String chatDirname = "." + File.separator + "data" + File.separator + "chat" + File.separator;
   private PrintStream chatprintstream = null;


   public PrivateChatWindow(String userChattingWith) {
      super("Arsenal Private Chat Window");
      this.userChattingWith = userChattingWith;
      inputLabel.setText("Chat with User: " + userChattingWith);
      setIconImage(new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ico.gif").getImage());
      JSplitPane jsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputField, scrollPane);
      jsplitpane.setDividerLocation(25);
      jsplitpane.setDividerSize(0);
      //textArea.setSize(new Dimension(220, 175));
      //scrollPane.setSize(new Dimension(220, 175));
      setSize(new Dimension(350, 300));
      addStylesToDocument(doc);
      getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
      scrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
      inputLabel.setBackground(Color.white);
      inputField.setBackground(Color.lightGray);
      inputField.setForeground(Color.black);
      textArea.setBackground(Color.white);
      textArea.setForeground(Color.black);
      inputPanel.setBackground(Color.lightGray);
      splitPanel.setBackground(Color.lightGray);
      buttonPanel.setBackground(Color.lightGray);
      
      //getContentPane().add(inputField);
      //inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      //button.setAlignmentX(Component.CENTER_ALIGNMENT);
      //jsplitpane.setAlignmentX(Component.CENTER_ALIGNMENT);
      buttonPanel.add(button);
      inputPanel.add(inputLabel);
      splitPanel.add(jsplitpane);
      getContentPane().setBackground(Color.white);
      getContentPane().add(inputLabel);
      getContentPane().add(jsplitpane);
      //getContentPane().add(buttonPanel);
      //button.addActionListener(new ActionListener() {
      //  public void actionPerformed(ActionEvent e) {    
      //    sendNewChatMessage();
      //  }
      //});
      inputField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {    
          sendNewChatMessage();
        }
      });
      int xposition = (new Double(Math.random()*800)).intValue();
      int yposition = (new Double(Math.random()*600)).intValue();
      setLocation(xposition, yposition);

      setupChatPrintStream();
      //(Toolkit.getDefaultToolkit()).beep();
   }
   

   private void addStylesToDocument(StyledDocument doc) {
      try{
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("small", regular);
        StyleConstants.setFontFamily(s, "SansSerif");
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setFontSize(s, 16);
        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 12);

        //smile
        s = doc.addStyle("smile", regular);
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon smileIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "smile.gif");
        StyleConstants.setIcon(s, smileIcon); 
        styleMap.put("smile", doc.getStyle("smile"));
        styleMap.put(":)", doc.getStyle("smile"));

        //happy
        s = doc.addStyle("happy", regular);
        ImageIcon happyIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "happy.gif");
        StyleConstants.setIcon(s, happyIcon);
        styleMap.put("happy", doc.getStyle("happy"));
        styleMap.put(":^)", doc.getStyle("happy"));

        //nooo
        s = doc.addStyle("nooo", regular);
        ImageIcon noooIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "nooo.gif");
        StyleConstants.setIcon(s, noooIcon); 
        styleMap.put("nooo", doc.getStyle("nooo"));

        //mad
        s = doc.addStyle("mad", regular);
        ImageIcon madIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "mad.gif");
        StyleConstants.setIcon(s, madIcon);  
        styleMap.put("mad", doc.getStyle("mad"));
        styleMap.put(":^(", doc.getStyle("mad"));

        //idea
        s = doc.addStyle("idea", regular);
        ImageIcon ideaIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "idea.gif");
        StyleConstants.setIcon(s, ideaIcon);
        styleMap.put("idea", doc.getStyle("idea"));
   
        //ahhh
        s = doc.addStyle("ahhh", regular);
        ImageIcon ahhIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ahhh.gif");
        StyleConstants.setIcon(s, ahhIcon);
        styleMap.put("ahhh", doc.getStyle("ahhh"));
                          
        //bastard
        s = doc.addStyle("bastard", regular);
        ImageIcon bastardIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "bastard.gif");
        StyleConstants.setIcon(s, bastardIcon);
        styleMap.put("bastard", doc.getStyle("bastard"));
        styleMap.put(":(", doc.getStyle("bastard"));

        //worried
        s = doc.addStyle("worried", regular);
        ImageIcon worriedIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "worried.gif");
        StyleConstants.setIcon(s, worriedIcon);
        styleMap.put("worried", doc.getStyle("worried"));

        //wow
        s = doc.addStyle("wow", regular);
        ImageIcon wowIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "wow.gif");
        StyleConstants.setIcon(s, wowIcon);
        styleMap.put("wow", doc.getStyle("wow"));

        //startled
        s = doc.addStyle("startled", regular);
        ImageIcon startledIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "startled.gif");
        StyleConstants.setIcon(s, startledIcon);
        styleMap.put("startled", doc.getStyle("startled"));
        styleMap.put("shocked", doc.getStyle("startled"));
        styleMap.put("suprised", doc.getStyle("startled"));

        //sleep
        s = doc.addStyle("sleep", regular);
        ImageIcon sleepIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "sleep.gif");
        StyleConstants.setIcon(s, sleepIcon);
        styleMap.put("sleep", doc.getStyle("sleep"));

        //shoot
        s = doc.addStyle("shoot", regular);
        ImageIcon shootIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "shoot.gif");
        StyleConstants.setIcon(s, shootIcon);
        styleMap.put("shoot", doc.getStyle("shoot"));
        styleMap.put("shot", doc.getStyle("shoot"));

        //question mark
        s = doc.addStyle("question", regular);
        ImageIcon questionIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "question.gif");
        StyleConstants.setIcon(s, questionIcon);
        styleMap.put("??", doc.getStyle("question"));

        //pie
        s = doc.addStyle("pie", regular);
        ImageIcon pieIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "pie.gif");
        StyleConstants.setIcon(s, pieIcon);
        styleMap.put("pie", doc.getStyle("pie"));

        //hypnotize
        s = doc.addStyle("hypnotize", regular);
        ImageIcon hypnotizeIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hypnotize.gif");
        StyleConstants.setIcon(s, hypnotizeIcon);
        styleMap.put(":|", doc.getStyle("hypnotize"));

        //cheese
        s = doc.addStyle("cheese", regular);
        ImageIcon cheeseIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "cheese.gif");
        StyleConstants.setIcon(s, cheeseIcon);
        styleMap.put("cheese", doc.getStyle("cheese"));

        //flower
        s = doc.addStyle("flower", regular);
        ImageIcon flowerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "flower.gif");
        StyleConstants.setIcon(s, flowerIcon);
        styleMap.put("flower", doc.getStyle("flower"));

        //hungry
        s = doc.addStyle("hungry", regular);
        ImageIcon hungryIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hungry.gif");
        StyleConstants.setIcon(s, hungryIcon);
        styleMap.put("hungry", doc.getStyle("hungry"));
        styleMap.put("eat", doc.getStyle("hungry"));

        //beer
        s = doc.addStyle("beer", regular);
        ImageIcon beerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "beer.gif");
        StyleConstants.setIcon(s, beerIcon);
        styleMap.put("beer", doc.getStyle("beer"));

        //weird
        s = doc.addStyle("weird", regular);
        ImageIcon weirdIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "weird.gif");
        StyleConstants.setIcon(s, weirdIcon);
        styleMap.put("weird", doc.getStyle("weird"));

        //lol
        s = doc.addStyle("lol", regular);
        ImageIcon lolIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "lol.gif");
        StyleConstants.setIcon(s, lolIcon);
        styleMap.put("lol", doc.getStyle("lol"));
        styleMap.put("lmfao", doc.getStyle("lol"));

        //hello and hi
        s = doc.addStyle("hi", regular);
        ImageIcon hiIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hi.gif");
        StyleConstants.setIcon(s, hiIcon);
        styleMap.put("hi", doc.getStyle("hi"));
        styleMap.put("hello", doc.getStyle("hi"));

      }
      catch(Exception e) { }

    }      


    private void renderRestOfChat(StyledDocument doc, String message) {
      try {
        StringTokenizer st = new StringTokenizer(message);
        while(st.hasMoreTokens()) {
          boolean stop = false;
          String str = st.nextToken();
          for(int i=0; i < likeChars.length; i++) {
            if(str.indexOf(likeChars[i]) != -1) {
              doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
              doc.insertString(doc.getLength(), "x", (Style)styleMap.get(likeChars[i]));
              doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
              stop = true;
              break;
            }
          }
          if(!stop) {
            for(int j=0; j < equalsChars.length; j++) {
              if(str.toLowerCase().equals(equalsChars[j])) {
                doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
                doc.insertString(doc.getLength(), "z", (Style)styleMap.get(equalsChars[j]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(!stop) {
            for(int k=0; k < expChars.length; k++) {
              if(str.toLowerCase().equals(expChars[k])) {
                doc.insertString(doc.getLength(), "y", (Style)styleMap.get(expChars[k]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(stop)
            doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
          else
            doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
        }
        doc.insertString(doc.getLength(), "\n", doc.getStyle("small"));
      }
      catch(Exception e) { Log.debug(this, e.getMessage(), e); }
    }

   public void addNewChatMessage(ChatBean bean) {
     if((bean == null) || (bean.getChatMessage() == null)) return;
     if(!isVisible()) {
        setVisible(true);
        Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
     }
     try {
        doc.insertString(doc.getLength(), bean.getFromUserName()+ ": ",
                                 doc.getStyle("bold"));
        renderRestOfChat(doc,bean.getChatMessage());
        textArea.getCaret().setSelectionVisible(true);
        Point p = new Point(0,textArea.getDocument().getLength());
        vp.setViewPosition(p);
        scrollPane.setViewport(vp);
        Client.getInstance().playAudioFile(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "pop.wav");
     }
     catch(Exception e) { }
     logChat(bean);
   }

   public void sendNewChatMessage() {
     ChatBean bean = new ChatBean();
     bean.setFromUserName(ConnectionWindow.getInstance().getUsername());
     bean.setToUserName(userChattingWith);
     //bean.setSessionName(SessionPanel.getInstance().getMyCurrentSession());
     bean.setIsPrivateMessage(true);
     bean.setChatMessage(inputField.getText());
     PrivateChatMessageToUserMessage message = new PrivateChatMessageToUserMessage(); 
     message.setHandlerName("chat");
     message.setPayload(bean);     
     ChatClientHandler.getInstance().sendMessage(message);
     inputField.setText("");      
   }

  private boolean setupChatPrintStream() {
     Date date = new Date();
     try {
       File f = new File(chatDirname);
       if(!f.exists()) f.mkdir();

       this.chatprintstream = new PrintStream(new FileOutputStream(chatDirname + userChattingWith + "_" + date.getTime() + ".txt", true));
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "setupChatPrintStream() could not load setupChatPrintStream(): " + chatDirname + userChattingWith + "_" + date.getTime() + ".txt");
       Log.debug(this, e.getMessage(), e);
       return false;
     }          
  }

  private void logChat(ChatBean bean) {
    if(this.chatprintstream != null)
      this.chatprintstream.println(new Date() + "|" + bean.getFromUserName() + ": "+ bean.getChatMessage()); 
  } 

  public void disable() {
    button.setEnabled(false);
    inputField.setText("");
    inputField.setEnabled(false);
    textArea.setEnabled(false);
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
 *    $Header: /cvsroot/arsenal-1/chat/src/com/arsenal/chat/client/PrivateChatWindow.java,v 1.2 2005/08/26 18:18:39 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: PrivateChatWindow.java $ 
 *
 *    Description: 
 *
 *    Created as a window such that the User can see and send chat sent directly
 *    to/from the specified user.
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Date;

import com.arsenal.chat.*;
import com.arsenal.log.Log;
import com.arsenal.chat.message.*;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.Client;

public class PrivateChatWindow extends JFrame {
  
   private String userChattingWith = null;
   private JLabel inputLabel = new JLabel("To User: ");
   private JPanel inputPanel = new JPanel();
   private JButton button = new JButton("Send Message");
   private JPanel buttonPanel = new JPanel();
   private JTextField inputField = new JTextField(22);
   private JPanel splitPanel = new JPanel();
   private JTextPane textArea = new JTextPane();
   private JScrollPane scrollPane = new JScrollPane(textArea);
   private JViewport vp = scrollPane.getViewport();
   private StyledDocument doc = textArea.getStyledDocument();
   private HashMap styleMap = new HashMap();
   private String[] equalsChars = { "pie", "mad", "startled", "shoot", "beer", "flower", "lol", "shot", "sleep", "worried", "wow", "idea", "??", "happy", "cheese", "smile",
       "lmfao", "hello", "hi", "hungry", "eat", "shocked", "suprised", "weird" };
   private String[] expChars = { ":)", ":^)", ":(", ":^(", ":|" }; 
   private String[] likeChars = { "nooo", "ahhh" };

   private String chatDirname = "." + File.separator + "data" + File.separator + "chat" + File.separator;
   private PrintStream chatprintstream = null;


   public PrivateChatWindow(String userChattingWith) {
      super("Arsenal Private Chat Window");
      this.userChattingWith = userChattingWith;
      inputLabel.setText("Chat with User: " + userChattingWith);
      setIconImage(new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ico.gif").getImage());
      JSplitPane jsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputField, scrollPane);
      jsplitpane.setDividerLocation(25);
      jsplitpane.setDividerSize(0);
      //textArea.setSize(new Dimension(220, 175));
      //scrollPane.setSize(new Dimension(220, 175));
      setSize(new Dimension(350, 300));
      addStylesToDocument(doc);
      getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
      scrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
      inputLabel.setBackground(Color.white);
      inputField.setBackground(Color.lightGray);
      inputField.setForeground(Color.black);
      textArea.setBackground(Color.white);
      textArea.setForeground(Color.black);
      inputPanel.setBackground(Color.lightGray);
      splitPanel.setBackground(Color.lightGray);
      buttonPanel.setBackground(Color.lightGray);
      
      //getContentPane().add(inputField);
      //inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      //button.setAlignmentX(Component.CENTER_ALIGNMENT);
      //jsplitpane.setAlignmentX(Component.CENTER_ALIGNMENT);
      buttonPanel.add(button);
      inputPanel.add(inputLabel);
      splitPanel.add(jsplitpane);
      getContentPane().setBackground(Color.white);
      getContentPane().add(inputLabel);
      getContentPane().add(jsplitpane);
      //getContentPane().add(buttonPanel);
      //button.addActionListener(new ActionListener() {
      //  public void actionPerformed(ActionEvent e) {    
      //    sendNewChatMessage();
      //  }
      //});
      inputField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {    
          sendNewChatMessage();
        }
      });
      int xposition = (new Double(Math.random()*800)).intValue();
      int yposition = (new Double(Math.random()*600)).intValue();
      setLocation(xposition, yposition);

      setupChatPrintStream();
      //(Toolkit.getDefaultToolkit()).beep();
   }
   

   private void addStylesToDocument(StyledDocument doc) {
      try{
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("small", regular);
        StyleConstants.setFontFamily(s, "SansSerif");
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setFontSize(s, 16);
        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 12);

        //smile
        s = doc.addStyle("smile", regular);
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon smileIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "smile.gif");
        StyleConstants.setIcon(s, smileIcon); 
        styleMap.put("smile", doc.getStyle("smile"));
        styleMap.put(":)", doc.getStyle("smile"));

        //happy
        s = doc.addStyle("happy", regular);
        ImageIcon happyIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "happy.gif");
        StyleConstants.setIcon(s, happyIcon);
        styleMap.put("happy", doc.getStyle("happy"));
        styleMap.put(":^)", doc.getStyle("happy"));

        //nooo
        s = doc.addStyle("nooo", regular);
        ImageIcon noooIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "nooo.gif");
        StyleConstants.setIcon(s, noooIcon); 
        styleMap.put("nooo", doc.getStyle("nooo"));

        //mad
        s = doc.addStyle("mad", regular);
        ImageIcon madIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "mad.gif");
        StyleConstants.setIcon(s, madIcon);  
        styleMap.put("mad", doc.getStyle("mad"));
        styleMap.put(":^(", doc.getStyle("mad"));

        //idea
        s = doc.addStyle("idea", regular);
        ImageIcon ideaIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "idea.gif");
        StyleConstants.setIcon(s, ideaIcon);
        styleMap.put("idea", doc.getStyle("idea"));
   
        //ahhh
        s = doc.addStyle("ahhh", regular);
        ImageIcon ahhIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ahhh.gif");
        StyleConstants.setIcon(s, ahhIcon);
        styleMap.put("ahhh", doc.getStyle("ahhh"));
                          
        //bastard
        s = doc.addStyle("bastard", regular);
        ImageIcon bastardIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "bastard.gif");
        StyleConstants.setIcon(s, bastardIcon);
        styleMap.put("bastard", doc.getStyle("bastard"));
        styleMap.put(":(", doc.getStyle("bastard"));

        //worried
        s = doc.addStyle("worried", regular);
        ImageIcon worriedIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "worried.gif");
        StyleConstants.setIcon(s, worriedIcon);
        styleMap.put("worried", doc.getStyle("worried"));

        //wow
        s = doc.addStyle("wow", regular);
        ImageIcon wowIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "wow.gif");
        StyleConstants.setIcon(s, wowIcon);
        styleMap.put("wow", doc.getStyle("wow"));

        //startled
        s = doc.addStyle("startled", regular);
        ImageIcon startledIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "startled.gif");
        StyleConstants.setIcon(s, startledIcon);
        styleMap.put("startled", doc.getStyle("startled"));
        styleMap.put("shocked", doc.getStyle("startled"));
        styleMap.put("suprised", doc.getStyle("startled"));

        //sleep
        s = doc.addStyle("sleep", regular);
        ImageIcon sleepIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "sleep.gif");
        StyleConstants.setIcon(s, sleepIcon);
        styleMap.put("sleep", doc.getStyle("sleep"));

        //shoot
        s = doc.addStyle("shoot", regular);
        ImageIcon shootIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "shoot.gif");
        StyleConstants.setIcon(s, shootIcon);
        styleMap.put("shoot", doc.getStyle("shoot"));
        styleMap.put("shot", doc.getStyle("shoot"));

        //question mark
        s = doc.addStyle("question", regular);
        ImageIcon questionIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "question.gif");
        StyleConstants.setIcon(s, questionIcon);
        styleMap.put("??", doc.getStyle("question"));

        //pie
        s = doc.addStyle("pie", regular);
        ImageIcon pieIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "pie.gif");
        StyleConstants.setIcon(s, pieIcon);
        styleMap.put("pie", doc.getStyle("pie"));

        //hypnotize
        s = doc.addStyle("hypnotize", regular);
        ImageIcon hypnotizeIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hypnotize.gif");
        StyleConstants.setIcon(s, hypnotizeIcon);
        styleMap.put(":|", doc.getStyle("hypnotize"));

        //cheese
        s = doc.addStyle("cheese", regular);
        ImageIcon cheeseIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "cheese.gif");
        StyleConstants.setIcon(s, cheeseIcon);
        styleMap.put("cheese", doc.getStyle("cheese"));

        //flower
        s = doc.addStyle("flower", regular);
        ImageIcon flowerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "flower.gif");
        StyleConstants.setIcon(s, flowerIcon);
        styleMap.put("flower", doc.getStyle("flower"));

        //hungry
        s = doc.addStyle("hungry", regular);
        ImageIcon hungryIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hungry.gif");
        StyleConstants.setIcon(s, hungryIcon);
        styleMap.put("hungry", doc.getStyle("hungry"));
        styleMap.put("eat", doc.getStyle("hungry"));

        //beer
        s = doc.addStyle("beer", regular);
        ImageIcon beerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "beer.gif");
        StyleConstants.setIcon(s, beerIcon);
        styleMap.put("beer", doc.getStyle("beer"));

        //weird
        s = doc.addStyle("weird", regular);
        ImageIcon weirdIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "weird.gif");
        StyleConstants.setIcon(s, weirdIcon);
        styleMap.put("weird", doc.getStyle("weird"));

        //lol
        s = doc.addStyle("lol", regular);
        ImageIcon lolIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "lol.gif");
        StyleConstants.setIcon(s, lolIcon);
        styleMap.put("lol", doc.getStyle("lol"));
        styleMap.put("lmfao", doc.getStyle("lol"));

        //hello and hi
        s = doc.addStyle("hi", regular);
        ImageIcon hiIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hi.gif");
        StyleConstants.setIcon(s, hiIcon);
        styleMap.put("hi", doc.getStyle("hi"));
        styleMap.put("hello", doc.getStyle("hi"));

      }
      catch(Exception e) { }

    }      


    private void renderRestOfChat(StyledDocument doc, String message) {
      try {
        StringTokenizer st = new StringTokenizer(message);
        while(st.hasMoreTokens()) {
          boolean stop = false;
          String str = st.nextToken();
          for(int i=0; i < likeChars.length; i++) {
            if(str.indexOf(likeChars[i]) != -1) {
              doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
              doc.insertString(doc.getLength(), "x", (Style)styleMap.get(likeChars[i]));
              doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
              stop = true;
              break;
            }
          }
          if(!stop) {
            for(int j=0; j < equalsChars.length; j++) {
              if(str.toLowerCase().equals(equalsChars[j])) {
                doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
                doc.insertString(doc.getLength(), "z", (Style)styleMap.get(equalsChars[j]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(!stop) {
            for(int k=0; k < expChars.length; k++) {
              if(str.toLowerCase().equals(expChars[k])) {
                doc.insertString(doc.getLength(), "y", (Style)styleMap.get(expChars[k]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(stop)
            doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
          else
            doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
        }
        doc.insertString(doc.getLength(), "\n", doc.getStyle("small"));
      }
      catch(Exception e) { Log.debug(this, e.getMessage(), e); }
    }

   public void addNewChatMessage(ChatBean bean) {
     if((bean == null) || (bean.getChatMessage() == null)) return;
     if(!isVisible()) {
        setVisible(true);
        Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
     }
     try {
        doc.insertString(doc.getLength(), bean.getFromUserName()+ ": ",
                                 doc.getStyle("bold"));
        renderRestOfChat(doc,bean.getChatMessage());
        textArea.getCaret().setSelectionVisible(true);
        Point p = new Point(0,textArea.getDocument().getLength());
        vp.setViewPosition(p);
        scrollPane.setViewport(vp);
        Client.getInstance().playAudioFile(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "blip.wav");
     }
     catch(Exception e) { }
     logChat(bean);
   }

   public void sendNewChatMessage() {
     ChatBean bean = new ChatBean();
     bean.setFromUserName(ConnectionWindow.getInstance().getUsername());
     bean.setToUserName(userChattingWith);
     //bean.setSessionName(SessionPanel.getInstance().getMyCurrentSession());
     bean.setIsPrivateMessage(true);
     bean.setChatMessage(inputField.getText());
     PrivateChatMessageToUserMessage message = new PrivateChatMessageToUserMessage(); 
     message.setHandlerName("chat");
     message.setPayload(bean);     
     ChatClientHandler.getInstance().sendMessage(message);
     inputField.setText("");      
   }

  private boolean setupChatPrintStream() {
     Date date = new Date();
     try {
       File f = new File(chatDirname);
       if(!f.exists()) f.mkdir();

       this.chatprintstream = new PrintStream(new FileOutputStream(chatDirname + userChattingWith + "_" + date.getTime() + ".txt", true));
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "setupChatPrintStream() could not load setupChatPrintStream(): " + chatDirname + userChattingWith + "_" + date.getTime() + ".txt");
       Log.debug(this, e.getMessage(), e);
       return false;
     }          
  }

  private void logChat(ChatBean bean) {
    if(this.chatprintstream != null)
      this.chatprintstream.println(new Date() + "|" + bean.getFromUserName() + ": "+ bean.getChatMessage()); 
  } 

  public void disable() {
    button.setEnabled(false);
    inputField.setText("");
    inputField.setEnabled(false);
    textArea.setEnabled(false);
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
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/chat/src/com/arsenal/chat/client/PrivateChatWindow.java,v 1.1.1.1 2005/03/26 17:12:31 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: PrivateChatWindow.java $ 
 *
 *    Description: 
 *
 *    Created as a window such that the User can see and send chat sent directly
 *    to/from the specified user.
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Date;

import com.arsenal.chat.*;
import com.arsenal.log.Log;
import com.arsenal.chat.message.*;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.Client;

public class PrivateChatWindow extends JFrame {
  
   private String userChattingWith = null;
   private JLabel inputLabel = new JLabel("To User: ");
   private JPanel inputPanel = new JPanel();
   private JButton button = new JButton("Send Message");
   private JPanel buttonPanel = new JPanel();
   private JTextField inputField = new JTextField(22);
   private JPanel splitPanel = new JPanel();
   private JTextPane textArea = new JTextPane();
   private JScrollPane scrollPane = new JScrollPane(textArea);
   private JViewport vp = scrollPane.getViewport();
   private StyledDocument doc = textArea.getStyledDocument();
   private HashMap styleMap = new HashMap();
   private String[] equalsChars = { "pie", "mad", "startled", "shoot", "beer", "flower", "lol", "shot", "sleep", "worried", "wow", "idea", "??", "happy", "cheese", "smile",
       "lmfao", "hello", "hi", "hungry", "eat", "shocked", "suprised", "weird" };
   private String[] expChars = { ":)", ":^)", ":(", ":^(", ":|" }; 
   private String[] likeChars = { "nooo", "ahhh" };

   private String chatDirname = "." + File.separator + "data" + File.separator + "chat" + File.separator;
   private PrintStream chatprintstream = null;


   public PrivateChatWindow(String userChattingWith) {
      super("Arsenal Private Chat Window");
      this.userChattingWith = userChattingWith;
      inputLabel.setText("Chat with User: " + userChattingWith);
      setIconImage(new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ico.gif").getImage());
      JSplitPane jsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputField, scrollPane);
      jsplitpane.setDividerLocation(25);
      jsplitpane.setDividerSize(0);
      //textArea.setSize(new Dimension(220, 175));
      //scrollPane.setSize(new Dimension(220, 175));
      setSize(new Dimension(350, 300));
      addStylesToDocument(doc);
      getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
      scrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
      inputLabel.setBackground(Color.white);
      inputField.setBackground(Color.lightGray);
      inputField.setForeground(Color.black);
      textArea.setBackground(Color.white);
      textArea.setForeground(Color.black);
      inputPanel.setBackground(Color.lightGray);
      splitPanel.setBackground(Color.lightGray);
      buttonPanel.setBackground(Color.lightGray);
      
      //getContentPane().add(inputField);
      //inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      //button.setAlignmentX(Component.CENTER_ALIGNMENT);
      //jsplitpane.setAlignmentX(Component.CENTER_ALIGNMENT);
      buttonPanel.add(button);
      inputPanel.add(inputLabel);
      splitPanel.add(jsplitpane);
      getContentPane().setBackground(Color.white);
      getContentPane().add(inputLabel);
      getContentPane().add(jsplitpane);
      //getContentPane().add(buttonPanel);
      //button.addActionListener(new ActionListener() {
      //  public void actionPerformed(ActionEvent e) {    
      //    sendNewChatMessage();
      //  }
      //});
      inputField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {    
          sendNewChatMessage();
        }
      });
      int xposition = (new Double(Math.random()*800)).intValue();
      int yposition = (new Double(Math.random()*600)).intValue();
      setLocation(xposition, yposition);

      setupChatPrintStream();
      //(Toolkit.getDefaultToolkit()).beep();
   }
   

   private void addStylesToDocument(StyledDocument doc) {
      try{
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("small", regular);
        StyleConstants.setFontFamily(s, "SansSerif");
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setFontSize(s, 16);
        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 12);

        //smile
        s = doc.addStyle("smile", regular);
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon smileIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "smile.gif");
        StyleConstants.setIcon(s, smileIcon); 
        styleMap.put("smile", doc.getStyle("smile"));
        styleMap.put(":)", doc.getStyle("smile"));

        //happy
        s = doc.addStyle("happy", regular);
        ImageIcon happyIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "happy.gif");
        StyleConstants.setIcon(s, happyIcon);
        styleMap.put("happy", doc.getStyle("happy"));
        styleMap.put(":^)", doc.getStyle("happy"));

        //nooo
        s = doc.addStyle("nooo", regular);
        ImageIcon noooIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "nooo.gif");
        StyleConstants.setIcon(s, noooIcon); 
        styleMap.put("nooo", doc.getStyle("nooo"));

        //mad
        s = doc.addStyle("mad", regular);
        ImageIcon madIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "mad.gif");
        StyleConstants.setIcon(s, madIcon);  
        styleMap.put("mad", doc.getStyle("mad"));
        styleMap.put(":^(", doc.getStyle("mad"));

        //idea
        s = doc.addStyle("idea", regular);
        ImageIcon ideaIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "idea.gif");
        StyleConstants.setIcon(s, ideaIcon);
        styleMap.put("idea", doc.getStyle("idea"));
   
        //ahhh
        s = doc.addStyle("ahhh", regular);
        ImageIcon ahhIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ahhh.gif");
        StyleConstants.setIcon(s, ahhIcon);
        styleMap.put("ahhh", doc.getStyle("ahhh"));
                          
        //bastard
        s = doc.addStyle("bastard", regular);
        ImageIcon bastardIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "bastard.gif");
        StyleConstants.setIcon(s, bastardIcon);
        styleMap.put("bastard", doc.getStyle("bastard"));
        styleMap.put(":(", doc.getStyle("bastard"));

        //worried
        s = doc.addStyle("worried", regular);
        ImageIcon worriedIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "worried.gif");
        StyleConstants.setIcon(s, worriedIcon);
        styleMap.put("worried", doc.getStyle("worried"));

        //wow
        s = doc.addStyle("wow", regular);
        ImageIcon wowIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "wow.gif");
        StyleConstants.setIcon(s, wowIcon);
        styleMap.put("wow", doc.getStyle("wow"));

        //startled
        s = doc.addStyle("startled", regular);
        ImageIcon startledIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "startled.gif");
        StyleConstants.setIcon(s, startledIcon);
        styleMap.put("startled", doc.getStyle("startled"));
        styleMap.put("shocked", doc.getStyle("startled"));
        styleMap.put("suprised", doc.getStyle("startled"));

        //sleep
        s = doc.addStyle("sleep", regular);
        ImageIcon sleepIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "sleep.gif");
        StyleConstants.setIcon(s, sleepIcon);
        styleMap.put("sleep", doc.getStyle("sleep"));

        //shoot
        s = doc.addStyle("shoot", regular);
        ImageIcon shootIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "shoot.gif");
        StyleConstants.setIcon(s, shootIcon);
        styleMap.put("shoot", doc.getStyle("shoot"));
        styleMap.put("shot", doc.getStyle("shoot"));

        //question mark
        s = doc.addStyle("question", regular);
        ImageIcon questionIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "question.gif");
        StyleConstants.setIcon(s, questionIcon);
        styleMap.put("??", doc.getStyle("question"));

        //pie
        s = doc.addStyle("pie", regular);
        ImageIcon pieIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "pie.gif");
        StyleConstants.setIcon(s, pieIcon);
        styleMap.put("pie", doc.getStyle("pie"));

        //hypnotize
        s = doc.addStyle("hypnotize", regular);
        ImageIcon hypnotizeIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hypnotize.gif");
        StyleConstants.setIcon(s, hypnotizeIcon);
        styleMap.put(":|", doc.getStyle("hypnotize"));

        //cheese
        s = doc.addStyle("cheese", regular);
        ImageIcon cheeseIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "cheese.gif");
        StyleConstants.setIcon(s, cheeseIcon);
        styleMap.put("cheese", doc.getStyle("cheese"));

        //flower
        s = doc.addStyle("flower", regular);
        ImageIcon flowerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "flower.gif");
        StyleConstants.setIcon(s, flowerIcon);
        styleMap.put("flower", doc.getStyle("flower"));

        //hungry
        s = doc.addStyle("hungry", regular);
        ImageIcon hungryIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hungry.gif");
        StyleConstants.setIcon(s, hungryIcon);
        styleMap.put("hungry", doc.getStyle("hungry"));
        styleMap.put("eat", doc.getStyle("hungry"));

        //beer
        s = doc.addStyle("beer", regular);
        ImageIcon beerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "beer.gif");
        StyleConstants.setIcon(s, beerIcon);
        styleMap.put("beer", doc.getStyle("beer"));

        //weird
        s = doc.addStyle("weird", regular);
        ImageIcon weirdIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "weird.gif");
        StyleConstants.setIcon(s, weirdIcon);
        styleMap.put("weird", doc.getStyle("weird"));

        //lol
        s = doc.addStyle("lol", regular);
        ImageIcon lolIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "lol.gif");
        StyleConstants.setIcon(s, lolIcon);
        styleMap.put("lol", doc.getStyle("lol"));
        styleMap.put("lmfao", doc.getStyle("lol"));

        //hello and hi
        s = doc.addStyle("hi", regular);
        ImageIcon hiIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hi.gif");
        StyleConstants.setIcon(s, hiIcon);
        styleMap.put("hi", doc.getStyle("hi"));
        styleMap.put("hello", doc.getStyle("hi"));

      }
      catch(Exception e) { }

    }      


    private void renderRestOfChat(StyledDocument doc, String message) {
      try {
        StringTokenizer st = new StringTokenizer(message);
        while(st.hasMoreTokens()) {
          boolean stop = false;
          String str = st.nextToken();
          for(int i=0; i < likeChars.length; i++) {
            if(str.indexOf(likeChars[i]) != -1) {
              doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
              doc.insertString(doc.getLength(), "x", (Style)styleMap.get(likeChars[i]));
              doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
              stop = true;
              break;
            }
          }
          if(!stop) {
            for(int j=0; j < equalsChars.length; j++) {
              if(str.toLowerCase().equals(equalsChars[j])) {
                doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
                doc.insertString(doc.getLength(), "z", (Style)styleMap.get(equalsChars[j]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(!stop) {
            for(int k=0; k < expChars.length; k++) {
              if(str.toLowerCase().equals(expChars[k])) {
                doc.insertString(doc.getLength(), "y", (Style)styleMap.get(expChars[k]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(stop)
            doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
          else
            doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
        }
        doc.insertString(doc.getLength(), "\n", doc.getStyle("small"));
      }
      catch(Exception e) { Log.debug(this, e.getMessage(), e); }
    }

   public void addNewChatMessage(ChatBean bean) {
     if((bean == null) || (bean.getChatMessage() == null)) return;
     if(!isVisible()) {
        setVisible(true);
        Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
     }
     try {
        doc.insertString(doc.getLength(), bean.getFromUserName()+ ": ",
                                 doc.getStyle("bold"));
        renderRestOfChat(doc,bean.getChatMessage());
        textArea.getCaret().setSelectionVisible(true);
        Point p = new Point(0,textArea.getDocument().getLength());
        vp.setViewPosition(p);
        scrollPane.setViewport(vp);
        Client.getInstance().playAudioFile(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "pop.wav");
     }
     catch(Exception e) { }
     logChat(bean);
   }

   public void sendNewChatMessage() {
     ChatBean bean = new ChatBean();
     bean.setFromUserName(ConnectionWindow.getInstance().getUsername());
     bean.setToUserName(userChattingWith);
     //bean.setSessionName(SessionPanel.getInstance().getMyCurrentSession());
     bean.setIsPrivateMessage(true);
     bean.setChatMessage(inputField.getText());
     PrivateChatMessageToUserMessage message = new PrivateChatMessageToUserMessage(); 
     message.setHandlerName("chat");
     message.setPayload(bean);     
     ChatClientHandler.getInstance().sendMessage(message);
     inputField.setText("");      
   }

  private boolean setupChatPrintStream() {
     Date date = new Date();
     try {
       File f = new File(chatDirname);
       if(!f.exists()) f.mkdir();

       this.chatprintstream = new PrintStream(new FileOutputStream(chatDirname + userChattingWith + "_" + date.getTime() + ".txt", true));
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "setupChatPrintStream() could not load setupChatPrintStream(): " + chatDirname + userChattingWith + "_" + date.getTime() + ".txt");
       Log.debug(this, e.getMessage(), e);
       return false;
     }          
  }

  private void logChat(ChatBean bean) {
    if(this.chatprintstream != null)
      this.chatprintstream.println(new Date() + "|" + bean.getFromUserName() + ": "+ bean.getChatMessage()); 
  } 

  public void disable() {
    button.setEnabled(false);
    inputField.setText("");
    inputField.setEnabled(false);
    textArea.setEnabled(false);
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
 *    $Header: /cvsroot/arsenal-1/chat/src/com/arsenal/chat/client/PrivateChatWindow.java,v 1.2 2005/08/26 18:18:39 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: PrivateChatWindow.java $ 
 *
 *    Description: 
 *
 *    Created as a window such that the User can see and send chat sent directly
 *    to/from the specified user.
 *
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *
 */
package com.arsenal.chat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Date;

import com.arsenal.chat.*;
import com.arsenal.log.Log;
import com.arsenal.chat.message.*;
import com.arsenal.client.ConnectionWindow;
import com.arsenal.client.Client;

public class PrivateChatWindow extends JFrame {
  
   private String userChattingWith = null;
   private JLabel inputLabel = new JLabel("To User: ");
   private JPanel inputPanel = new JPanel();
   private JButton button = new JButton("Send Message");
   private JPanel buttonPanel = new JPanel();
   private JTextField inputField = new JTextField(22);
   private JPanel splitPanel = new JPanel();
   private JTextPane textArea = new JTextPane();
   private JScrollPane scrollPane = new JScrollPane(textArea);
   private JViewport vp = scrollPane.getViewport();
   private StyledDocument doc = textArea.getStyledDocument();
   private HashMap styleMap = new HashMap();
   private String[] equalsChars = { "pie", "mad", "startled", "shoot", "beer", "flower", "lol", "shot", "sleep", "worried", "wow", "idea", "??", "happy", "cheese", "smile",
       "lmfao", "hello", "hi", "hungry", "eat", "shocked", "suprised", "weird" };
   private String[] expChars = { ":)", ":^)", ":(", ":^(", ":|" }; 
   private String[] likeChars = { "nooo", "ahhh" };

   private String chatDirname = "." + File.separator + "data" + File.separator + "chat" + File.separator;
   private PrintStream chatprintstream = null;


   public PrivateChatWindow(String userChattingWith) {
      super("Arsenal Private Chat Window");
      this.userChattingWith = userChattingWith;
      inputLabel.setText("Chat with User: " + userChattingWith);
      setIconImage(new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ico.gif").getImage());
      JSplitPane jsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputField, scrollPane);
      jsplitpane.setDividerLocation(25);
      jsplitpane.setDividerSize(0);
      //textArea.setSize(new Dimension(220, 175));
      //scrollPane.setSize(new Dimension(220, 175));
      setSize(new Dimension(350, 300));
      addStylesToDocument(doc);
      getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
      scrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
      inputLabel.setBackground(Color.white);
      inputField.setBackground(Color.lightGray);
      inputField.setForeground(Color.black);
      textArea.setBackground(Color.white);
      textArea.setForeground(Color.black);
      inputPanel.setBackground(Color.lightGray);
      splitPanel.setBackground(Color.lightGray);
      buttonPanel.setBackground(Color.lightGray);
      
      //getContentPane().add(inputField);
      //inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      //button.setAlignmentX(Component.CENTER_ALIGNMENT);
      //jsplitpane.setAlignmentX(Component.CENTER_ALIGNMENT);
      buttonPanel.add(button);
      inputPanel.add(inputLabel);
      splitPanel.add(jsplitpane);
      getContentPane().setBackground(Color.white);
      getContentPane().add(inputLabel);
      getContentPane().add(jsplitpane);
      //getContentPane().add(buttonPanel);
      //button.addActionListener(new ActionListener() {
      //  public void actionPerformed(ActionEvent e) {    
      //    sendNewChatMessage();
      //  }
      //});
      inputField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {    
          sendNewChatMessage();
        }
      });
      int xposition = (new Double(Math.random()*800)).intValue();
      int yposition = (new Double(Math.random()*600)).intValue();
      setLocation(xposition, yposition);

      setupChatPrintStream();
      //(Toolkit.getDefaultToolkit()).beep();
   }
   

   private void addStylesToDocument(StyledDocument doc) {
      try{
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("small", regular);
        StyleConstants.setFontFamily(s, "SansSerif");
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setFontSize(s, 16);
        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 12);

        //smile
        s = doc.addStyle("smile", regular);
        StyleConstants.setFontFamily(s, "Arial");
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon smileIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "smile.gif");
        StyleConstants.setIcon(s, smileIcon); 
        styleMap.put("smile", doc.getStyle("smile"));
        styleMap.put(":)", doc.getStyle("smile"));

        //happy
        s = doc.addStyle("happy", regular);
        ImageIcon happyIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "happy.gif");
        StyleConstants.setIcon(s, happyIcon);
        styleMap.put("happy", doc.getStyle("happy"));
        styleMap.put(":^)", doc.getStyle("happy"));

        //nooo
        s = doc.addStyle("nooo", regular);
        ImageIcon noooIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "nooo.gif");
        StyleConstants.setIcon(s, noooIcon); 
        styleMap.put("nooo", doc.getStyle("nooo"));

        //mad
        s = doc.addStyle("mad", regular);
        ImageIcon madIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "mad.gif");
        StyleConstants.setIcon(s, madIcon);  
        styleMap.put("mad", doc.getStyle("mad"));
        styleMap.put(":^(", doc.getStyle("mad"));

        //idea
        s = doc.addStyle("idea", regular);
        ImageIcon ideaIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "idea.gif");
        StyleConstants.setIcon(s, ideaIcon);
        styleMap.put("idea", doc.getStyle("idea"));
   
        //ahhh
        s = doc.addStyle("ahhh", regular);
        ImageIcon ahhIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "ahhh.gif");
        StyleConstants.setIcon(s, ahhIcon);
        styleMap.put("ahhh", doc.getStyle("ahhh"));
                          
        //bastard
        s = doc.addStyle("bastard", regular);
        ImageIcon bastardIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "bastard.gif");
        StyleConstants.setIcon(s, bastardIcon);
        styleMap.put("bastard", doc.getStyle("bastard"));
        styleMap.put(":(", doc.getStyle("bastard"));

        //worried
        s = doc.addStyle("worried", regular);
        ImageIcon worriedIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "worried.gif");
        StyleConstants.setIcon(s, worriedIcon);
        styleMap.put("worried", doc.getStyle("worried"));

        //wow
        s = doc.addStyle("wow", regular);
        ImageIcon wowIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "wow.gif");
        StyleConstants.setIcon(s, wowIcon);
        styleMap.put("wow", doc.getStyle("wow"));

        //startled
        s = doc.addStyle("startled", regular);
        ImageIcon startledIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "startled.gif");
        StyleConstants.setIcon(s, startledIcon);
        styleMap.put("startled", doc.getStyle("startled"));
        styleMap.put("shocked", doc.getStyle("startled"));
        styleMap.put("suprised", doc.getStyle("startled"));

        //sleep
        s = doc.addStyle("sleep", regular);
        ImageIcon sleepIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "sleep.gif");
        StyleConstants.setIcon(s, sleepIcon);
        styleMap.put("sleep", doc.getStyle("sleep"));

        //shoot
        s = doc.addStyle("shoot", regular);
        ImageIcon shootIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "shoot.gif");
        StyleConstants.setIcon(s, shootIcon);
        styleMap.put("shoot", doc.getStyle("shoot"));
        styleMap.put("shot", doc.getStyle("shoot"));

        //question mark
        s = doc.addStyle("question", regular);
        ImageIcon questionIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "question.gif");
        StyleConstants.setIcon(s, questionIcon);
        styleMap.put("??", doc.getStyle("question"));

        //pie
        s = doc.addStyle("pie", regular);
        ImageIcon pieIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "pie.gif");
        StyleConstants.setIcon(s, pieIcon);
        styleMap.put("pie", doc.getStyle("pie"));

        //hypnotize
        s = doc.addStyle("hypnotize", regular);
        ImageIcon hypnotizeIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hypnotize.gif");
        StyleConstants.setIcon(s, hypnotizeIcon);
        styleMap.put(":|", doc.getStyle("hypnotize"));

        //cheese
        s = doc.addStyle("cheese", regular);
        ImageIcon cheeseIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "cheese.gif");
        StyleConstants.setIcon(s, cheeseIcon);
        styleMap.put("cheese", doc.getStyle("cheese"));

        //flower
        s = doc.addStyle("flower", regular);
        ImageIcon flowerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "flower.gif");
        StyleConstants.setIcon(s, flowerIcon);
        styleMap.put("flower", doc.getStyle("flower"));

        //hungry
        s = doc.addStyle("hungry", regular);
        ImageIcon hungryIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hungry.gif");
        StyleConstants.setIcon(s, hungryIcon);
        styleMap.put("hungry", doc.getStyle("hungry"));
        styleMap.put("eat", doc.getStyle("hungry"));

        //beer
        s = doc.addStyle("beer", regular);
        ImageIcon beerIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "beer.gif");
        StyleConstants.setIcon(s, beerIcon);
        styleMap.put("beer", doc.getStyle("beer"));

        //weird
        s = doc.addStyle("weird", regular);
        ImageIcon weirdIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "weird.gif");
        StyleConstants.setIcon(s, weirdIcon);
        styleMap.put("weird", doc.getStyle("weird"));

        //lol
        s = doc.addStyle("lol", regular);
        ImageIcon lolIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "lol.gif");
        StyleConstants.setIcon(s, lolIcon);
        styleMap.put("lol", doc.getStyle("lol"));
        styleMap.put("lmfao", doc.getStyle("lol"));

        //hello and hi
        s = doc.addStyle("hi", regular);
        ImageIcon hiIcon = new ImageIcon("." + File.separator + "images" + File.separator + "arsenal" + File.separator + "hi.gif");
        StyleConstants.setIcon(s, hiIcon);
        styleMap.put("hi", doc.getStyle("hi"));
        styleMap.put("hello", doc.getStyle("hi"));

      }
      catch(Exception e) { }

    }      


    private void renderRestOfChat(StyledDocument doc, String message) {
      try {
        StringTokenizer st = new StringTokenizer(message);
        while(st.hasMoreTokens()) {
          boolean stop = false;
          String str = st.nextToken();
          for(int i=0; i < likeChars.length; i++) {
            if(str.indexOf(likeChars[i]) != -1) {
              doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
              doc.insertString(doc.getLength(), "x", (Style)styleMap.get(likeChars[i]));
              doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
              stop = true;
              break;
            }
          }
          if(!stop) {
            for(int j=0; j < equalsChars.length; j++) {
              if(str.toLowerCase().equals(equalsChars[j])) {
                doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
                doc.insertString(doc.getLength(), "z", (Style)styleMap.get(equalsChars[j]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(!stop) {
            for(int k=0; k < expChars.length; k++) {
              if(str.toLowerCase().equals(expChars[k])) {
                doc.insertString(doc.getLength(), "y", (Style)styleMap.get(expChars[k]));
                doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
                stop = true;
                break;
              }
            }
          }
          if(stop)
            doc.insertString(doc.getLength(), " ", doc.getStyle("large"));
          else
            doc.insertString(doc.getLength(), str + " ", doc.getStyle("large"));
        }
        doc.insertString(doc.getLength(), "\n", doc.getStyle("small"));
      }
      catch(Exception e) { Log.debug(this, e.getMessage(), e); }
    }

   public void addNewChatMessage(ChatBean bean) {
     if((bean == null) || (bean.getChatMessage() == null)) return;
     if(!isVisible()) {
        setVisible(true);
        Client.getInstance().playAudioFileInThread(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "ping.wav");
     }
     try {
        doc.insertString(doc.getLength(), bean.getFromUserName()+ ": ",
                                 doc.getStyle("bold"));
        renderRestOfChat(doc,bean.getChatMessage());
        textArea.getCaret().setSelectionVisible(true);
        Point p = new Point(0,textArea.getDocument().getLength());
        vp.setViewPosition(p);
        scrollPane.setViewport(vp);
        Client.getInstance().playAudioFile(System.getProperty("user.dir") + 
          File.separator + "sounds" + File.separator + "blip.wav");
     }
     catch(Exception e) { }
     logChat(bean);
   }

   public void sendNewChatMessage() {
     ChatBean bean = new ChatBean();
     bean.setFromUserName(ConnectionWindow.getInstance().getUsername());
     bean.setToUserName(userChattingWith);
     //bean.setSessionName(SessionPanel.getInstance().getMyCurrentSession());
     bean.setIsPrivateMessage(true);
     bean.setChatMessage(inputField.getText());
     PrivateChatMessageToUserMessage message = new PrivateChatMessageToUserMessage(); 
     message.setHandlerName("chat");
     message.setPayload(bean);     
     ChatClientHandler.getInstance().sendMessage(message);
     inputField.setText("");      
   }

  private boolean setupChatPrintStream() {
     Date date = new Date();
     try {
       File f = new File(chatDirname);
       if(!f.exists()) f.mkdir();

       this.chatprintstream = new PrintStream(new FileOutputStream(chatDirname + userChattingWith + "_" + date.getTime() + ".txt", true));
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "setupChatPrintStream() could not load setupChatPrintStream(): " + chatDirname + userChattingWith + "_" + date.getTime() + ".txt");
       Log.debug(this, e.getMessage(), e);
       return false;
     }          
  }

  private void logChat(ChatBean bean) {
    if(this.chatprintstream != null)
      this.chatprintstream.println(new Date() + "|" + bean.getFromUserName() + ": "+ bean.getChatMessage()); 
  } 

  public void disable() {
    button.setEnabled(false);
    inputField.setText("");
    inputField.setEnabled(false);
    textArea.setEnabled(false);
  }

}