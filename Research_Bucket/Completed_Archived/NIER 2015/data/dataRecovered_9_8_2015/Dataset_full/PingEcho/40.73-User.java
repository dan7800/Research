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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/user/src/com/arsenal/user/User.java,v 1.1.1.1 2005/03/26 17:16:42 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.5 2005/10/24 12:40:35 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean = new UserBean();
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName()); 
      return true;
    }    
    return false;
  }
  
  public boolean modifyPersonalInfo(UserBean bean) {
	    if((bean != null) && (bean.getName() != null)) {
	      assignDataPersonalInfo(bean);     
	      Log.debug(this, "edit() - successfully modifed user personal info: " + bean.getName());   
	      return true;
	    }    
	    return false;
	  }  
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       return Util.storePropertiesToFilename(this.props, getUserFilename());
       //return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   private boolean assignDataPersonalInfo(UserBean bean) {
	     try{
	      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
	        setPassword(bean.getPassword());
	      setPhone(bean.getPhone());
	      setEmail(bean.getEmail());
	      setFirstname(bean.getFirstname());
	      setLastname(bean.getLastname());
	      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
	        props.setProperty("password", bean.getPassword());
	      props.setProperty("firstname", bean.getFirstname());
	      props.setProperty("lastname", bean.getLastname());
	      props.setProperty("phone", bean.getPhone());
	      props.setProperty("email", bean.getEmail());
	      Log.debug(this, "assign Data Personal Info: " + bean.getName() + "|" + getName() + " Role: " + getRole());
	      setBeanData();
	      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
	      //setProperties(props);
	      if(save())
	        return true;
	      else
	        return false;
	     }
	     catch(Exception e) { 
	       Log.debug(this, e.getMessage(), e);
	       return false; }      
	   }   
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name); 
       if(Util.deleteFile(userDirname + name + File.separator + propsFilename)) {
          return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + userDirname + name + File.separator + propsFilename);
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/html/arsenal-1/user/src/com/arsenal/user/User.java,v 1.1.1.1 2005/03/26 17:16:42 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
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
 *    $Header: /cvsroot/arsenal-1/user/src/com/arsenal/user/User.java,v 1.2 2005/08/13 06:27:48 arsenal-1 Exp $ 
 *     
 *    File: $Workfile: User.java $ 
 *     
 *    Description: 
 *     
 *    This class is an object representation of a user.
 *    We use this to store a username and ServerConnection object to be
 *    associated with the username
 *     
 *    @author      michael@michaelburnside.com (arsenal-1) 
 *    @author      Michael Burnside 
 *    @version     %I%, %G% 
 *    @since       1.0 
 *     
 */
package com.arsenal.user;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.io.*;
import com.arsenal.rtcomm.server.*;
import com.arsenal.log.Log;
import com.arsenal.message.IMessage;
import com.arsenal.server.Server;
import com.arsenal.util.Util;

public class User {
        
  private String userDirname = UserManager.userDirname;
  private String propsFilename = UserManager.propsFilename;

  private String name = null;
  private String password = null;
  private String role = null;
  private String group = null;
  private String dateCreated = (new Date()).toString();
  private String userFilename = null;
  private String org = null;
  private String firstname = null;
  private String lastname = null;
  private String phone = null;
  private String email = null;
  private String key = null;
  private ServerConnection serverConnection = null;
  private long lastPingTime = (new Date()).getTime();
  private FileOutputStream userFileOutputStream = null;
  private FileInputStream userFileInputStream = null;
  private Properties props = new Properties();
  private UserBean bean = new UserBean();
  private String userType = UserType.NORMAL;
 
  public User() { }
  
  public void setName(String name) { this.name = name; }
  public String getName() { return this.name; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public String getFirstname() { return this.firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public String getLastname() { return this.lastname; }  
  public void setPhone(String phone) { this.phone = phone; }
  public String getPhone() { return this.phone; }
  public void setEmail(String email) { this.email = email; }
  public String getEmail() { return this.email; }
  public void setPassword(String password) { this.password = password; }
  public String getPassword() { return this.password; }
  public void setRole(String role) { this.role = role; }
  public String getRole() { return this.role; }
  public void setGroup(String group) { this.group = group; }
  public String getGroup() { return this.group; }
  public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
  public String getDateCreated() { return this.dateCreated; }
  public void setOrg(String org) { this.org = org; }
  public String getOrg() { return this.org; }
  public void setKey(String key) { this.key = key; }
  public String getKey() { return this.key; }
  public void setServerConnection(ServerConnection serverConnection) {
    if(serverConnection == null) {
      this.serverConnection.closeIOStreams();
    }
      this.serverConnection = serverConnection; 
  }
  public ServerConnection getServerConnection() { return this.serverConnection; }
  public void setUserFilename(String userFilename) { this.userFilename = userFilename; }
  public String getUserFilename() { return this.userFilename; } 
  public void setProperties(Properties props) { this.props = props; }
  public Properties getProperties() { return this.props; }
  public UserBean getBean() {
    bean.setUserType(getUserType()); 
    return this.bean; 
  }  
  public boolean isMobileUser() { 
    if(userType.equals(UserType.MOBILE)) return true;
    else return false;
  }
  public void setUserType(String userType) { this.userType = userType; }
  public String getUserType() { return this.userType; }

  public void sendMessage(IMessage message) {
    try {
      ObjectOutputStream oos = getServerConnection().getObjectOutputStream();
      oos.writeObject(message);
      oos.flush();
      Log.debug(this, "just sent user: " + getName() + " message to objectoutputstream: " + oos);
    }
    catch(Exception r) { 
     Log.debug(this, "User: " + getName() + " has broken output stream");
     Log.debug(this, r.getMessage(), r);
     UserManager.getInstance().logoutUser(getBean());
    }          
  }
  
  private void setBeanData() {
    this.bean.setName(getName());
    this.bean.setRole(getRole());
    this.bean.setGroup(getGroup()); 
    this.bean.setFirstname(getFirstname());
    this.bean.setLastname(getLastname());
    this.bean.setPhone(getPhone());
    this.bean.setEmail(getEmail());
    this.bean.setOrg(getOrg()); 
    this.bean.setRole(getRole());
    this.bean.setPassword(getPassword());      
    this.bean.setUserType(getUserType());
  }
  
  public void setLastPingTime(long lastPingTime) { 
    this.lastPingTime = lastPingTime; 
    Log.debug(this, "Just updated last ping time for user: " + getName() + ":" + String.valueOf(this.lastPingTime));
  }
  public long getLastPingTime() { return lastPingTime; }

  public boolean load(String name) {
     StringBuffer buf = new StringBuffer(userDirname);
     buf.append(name);
     buf.append(File.separator);
     buf.append(propsFilename);
     try {
       Log.debug(this, "load() Attempting to load session props for: " + buf.toString());
       this.props = Util.loadPropertiesFromFilename(buf.toString());
       buf = null;
       setName(this.props.getProperty("name"));
       setPassword(this.props.getProperty("password"));
       setFirstname(this.props.getProperty("firstname"));
       setLastname(this.props.getProperty("lastname"));
       setPhone(this.props.getProperty("phone"));
       setEmail(this.props.getProperty("email"));
       setGroup(this.props.getProperty("group"));
       setRole(this.props.getProperty("role"));
       //setUserFilename(this.props.getProperty("userFilename"));
       setDateCreated(this.props.getProperty("dateCreated"));
       setOrg(this.props.getProperty("organization"));
       setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
       setBeanData();
       
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "load()could not load user: " + name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
          
  }  

  public void unload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    phone = null;
    email = null;
    dateCreated = null;
    userFilename = null;
    serverConnection = null;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;
  }
  
  public void softUnload() {
    // remove all object references so they can be garbage collected
    userDirname = null;
    propsFilename = null;
    name = null;
    phone = null;
    email = null;
    password = null;
    role = null;
    group = null;
    dateCreated = null;
    userFilename = null;
    props = null;
  }
  

  public boolean edit(UserBean bean) {
    if((bean != null) && (bean.getName() != null)) {
      assignData(bean);     
      Log.debug(this, "edit() - successfully modifed user: " + bean.getName());
      //cleanup();   
      return true;
    }    
    return false;
  }
   
   public boolean save() {
     
     try {
       Log.debug(this, "try to save user file: " + getUserFilename());
       Util.storePropertiesToFilename(this.props, getUserFilename());
       return true;
     }
     catch(Exception e) { 
       Log.debug(this, "could not save user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }
     
   }   

   public boolean create(UserBean bean) {
    Log.debug(this, "attempt to create user: " + bean.getName());
    try {
      // make a new file object for the new directory
      File newDir = new File(userDirname + bean.getName());
      // if the directory does not exist
      if(!newDir.exists()) {
        // create the directory on the file system
        newDir.mkdir();
        assignData(bean);
        Log.debug(this, "successfully created user: " + getName());
        return true; 
      }
      else {
        Log.debug(this, "could not create user, user already exists: " + bean.getName());
        return false;
      }
    }
    catch(Exception e) {
      Log.debug(this, "could not create user: " + bean.getName());
      Log.debug(this, e.getMessage(), e);
      return false; 
    }

   }

   private boolean assignData(UserBean bean) {
     try{
      setName(bean.getName());
      if(!bean.getPassword().trim().equals("")) //if it's not blank we set it
        setPassword(bean.getPassword());
      setRole(bean.getRole());
      setGroup(bean.getGroup());
      setOrg(bean.getOrg());
      setPhone(bean.getPhone());
      setEmail(bean.getEmail());
      setFirstname(bean.getFirstname());
      setLastname(bean.getLastname());
      setUserType(bean.getUserType());
      props.setProperty("name", bean.getName());
      if((bean.getPassword() != null) && !bean.getPassword().trim().equals(""))
        props.setProperty("password", bean.getPassword());
      props.setProperty("role", bean.getRole());
      props.setProperty("group", bean.getGroup());
      props.setProperty("organization", bean.getOrg());
      props.setProperty("firstname", bean.getFirstname());
      props.setProperty("lastname", bean.getLastname());
      props.setProperty("phone", bean.getPhone());
      props.setProperty("email", bean.getEmail());
      Log.debug(this, "assign Data: " + bean.getName() + "|" + getName() + " Role: " + getRole());
      setBeanData();
      setUserFilename(userDirname + bean.getName() + File.separator + propsFilename);
      //setProperties(props);
      if(save())
        return true;
      else
        return false;
     }
     catch(Exception e) { 
       Log.debug(this, e.getMessage(), e);
       return false; }      
   }
   
   public boolean delete() {
     try {
       Log.debug(this, "attempt to delete user: " + this.name);
       File f = new File(userDirname + name + File.separator + propsFilename);
       if(f.exists() && f.isFile()) {
         f.delete();
         return Util.removeDirectory(userDirname + name);
       }
       else {
         Log.debug(this, "could not delete user, either the file properties doesnt exist or is not a file: " + f.getPath());
         return false;
       }
     }
     catch(Exception e) { 
       Log.debug(this, "could not delete user: " + this.name);
       Log.debug(this, e.getMessage(), e);
       return false;
     }     
   }
  
  // allow for complete garbage collection by losing references
  public void cleanup() {
    userDirname = null;
    propsFilename = null;
    name = null;
    password = null;
    role = null;
    group = null;
    dateCreated = (new Date()).toString();
    userFilename = null;
    org = null;
    firstname = null;
    lastname = null;
    phone = null;
    email = null;
    serverConnection = null;
    lastPingTime = 0;
    userFileOutputStream = null;
    userFileInputStream = null;
    props = null;          
  }
  
  public boolean isAdmin() {
    if(role.equals(UserRoles.ADMIN)) return true;
    return false;       
  }
  
  public boolean isUser() {
    if(role.equals(UserRoles.USER)) return true;
    return false;       
  }
  
  public boolean isGuest() {
    if(role.equals(UserRoles.GUEST)) return true;
    return false;       
  }  
  
}
