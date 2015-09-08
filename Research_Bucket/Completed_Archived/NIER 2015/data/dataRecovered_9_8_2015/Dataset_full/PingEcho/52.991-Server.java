/********************************************************************************

    jMule - A java eMule port
    
    Copyright (C) 2002 myon, gos, andyl

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details ( see the LICENSE file ).

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
	
********************************************************************************/

package org.jMule;

import java.net.*;
import java.util.*;
import java.util.logging.Logger;

/**
* This class holds the information about a server. 
* We know a server either from a server.met file, GETSOURCE response or user interaction.
* FIXME: here we have the ip and port vars too !!! 
* BAD in ServerConnection we should only give this vars further, wenn ueberhaupt 
**/
public class Server {
 private int			files = 		0;
 private int			users = 		0;
 private int			preferences = 	0;
 private int			ping = 			0;
 private String			description = 	null;
 private String			listname = 		null;
 private InetSocketAddress	ip = 		null;
 private TagList 			tagList =	null; 

 public Server() {
 };
 
 public	Server( int in_port, InetAddress in_ip ) {
  this.ip = new InetSocketAddress( in_ip, in_port);
 };
 
 /*
 * Used in ServerMetFile to create a server from server_met_entry.
 * This is a better solution as the tag.parseFromFile() method couze we dont overload the Server class with unneeded knowlege of the server.met. FIXME: So fix the Tag.java
 */
 public	Server( byte [] ip, int port, int tagCount ) throws UnknownHostException {
   tagList = new TagList( tagCount );
   this.ip = new InetSocketAddress( InetAddress.getByAddress( ip ), port);
 };
 
  public void addTag( Tag tag )	{
    // assert tagList != null 
		tagList.addTag( tag );
  }

 public	String	getListName()	{
 	return listname;
 }

 public	String	getRealName() {
	return ip.getHostName() ;
 }

 public	int	getPort()	{
	return ip.getPort();
 }

 public	boolean	hasServerName()	{
	return ! MiscUtil.isStrEmpty( getListName() );
 }
	
//	public	boolean	AddTagFromFile(FILE servermet)
//	{
//	}
	
 public	void	setListName(String newname) {
 }
	
 public	InetAddress getIP()	{
 	return ip.getAddress();
 }

 public	String getFullIP()	{
 	return ip.getAddress().getHostAddress();
 }

 public	InetSocketAddress getSocketAddress()	{
 	return ip;
 }

 public int getUserCount() {
   try {
     Tag tag = tagList.getTagByName( "users" );
     if( tag != null && tag.isIntTag() ) return tag.getIntValue();
   } catch( NoSuchElementException err ) {
   };
   return 0;
 };
 
 public	void	setUserCount(int in_users)	{
	users = in_users;
 }

 public int getFileCount() {
   try {
     Tag tag = tagList.getTagByName( "files" );
     if( tag != null && tag.isIntTag() ) return tag.getIntValue();
   } catch( NoSuchElementException err ) {
   };
   return 0;
 };
 
 public	void	setFileCount(int in_files)	 {
	files = in_files;
 }
 
 public String toString()
 {
 	return Server.class.getName() + "[ " + ip.getHostName() + "/" + getSocketAddress() + " ]";
 }
 
} // Server