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

package org.jMule.partialfile;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.logging.*;

import org.jMule.*;

/**
* This class manages the downloading process.
* It handles the writting process of collecting Data. Manages the GapList and the DataBlocks.
*/
public class PartialFile {
	
  final static Logger log = Logger.getLogger( PartialFile.class.getName() );
  
  public final static char ST_Hashing = 'H';
  public final static char ST_Looking = 'L';  
  public final static char ST_Downloading = 'D';
  public final static char ST_Paused = 'P';
  public final static char ST_Completed = 'C';
    
  public final static char PR_Low = 'L'; //Integer.MIN_VALUE;
  public final static char PR_Normal = 'N'; // 0;
  public final static char PR_High = 'H'; //Integer.MAX_VALUE;
    
private char status;
  private char priority;
  
  private String tempFName;
  private String fname;
  private FileID fileID; 
  
  private File fileTempHandle;
  private FileChannel fileChannel = null;
  private RandomAccessFile fileRAHandle;
 private String fileMode = "rw";

 /**
 * Because we have to handle partial downloads, we have to know which blocks of our file were already downloaded and whitch were not. We handle this by the gapList:
 * every not downloaded block goes here, and when we have some block downloaded, we just update/remove this block from the gapList.
 * So our file is fully downloaded if there are no more gaps in the list ( gapList.isEmpty() == true )
 */
 private GapList gapList;
	 
  private long lastSyncTime;
  private long lastFileAccessTime;
  /**
  * Interval at which we check the open files for to long staying open.
  */
  private static final int closeOpenFilesTimeoutInterval = 10 * MiscUtil.TU_MillisPerMinute;
	  
  /**
  * this is the interface to the LazyFlusher.
  * this method handles two cases:
  * <ul>
  * <item>it flushes the written data to the storage device;</item>
  * <item>it looks if we have run in the closeOpenFilesTimeoutInterval timeout, and if so, close the files to take the ressource pressure down.
  *</ul>
  * Set the closeOpenFilesTimeoutInterval to Integer.MAX_VALUE, if you dont wannt to check for too longly opened files ( actually it maximizes the interval to check / but this is good ). 
  */
  public void flush( int syncInterval, long currentTime ) {
	log.fine( "starting flush() of " + this.toString() );
	if( fileChannel == null ) return;
	  
	if( lastSyncTime + syncInterval > currentTime ) {
    try {
		fileChannel.force( false );
		lastSyncTime = currentTime;
    } catch( IOException io_err ) {
      io_err.printStackTrace();
      log.severe( "Cannot write the collected data to the file. Lost of data possible." );
    };
	};
	
	if( lastFileAccessTime + closeOpenFilesTimeoutInterval > currentTime ) {
		if( fileRAHandle != null ) { 
			if ( fileChannel != null ) fileChannel = null;
				try {
			fileRAHandle.close();
				} catch( IOException io_err ) {
          io_err.printStackTrace();
					log.info( "Trouble while closing file." );
				};
			fileRAHandle = null;
		};
	};
	log.fine( "flush() of "+ this.toString() + " complete" );
  }
  
  
  // Link to the PartFileInfoWriter
  
  /**  * used to create a new PartialFile from FileSearchRequest Response
  */
  public PartialFile( FileID fileid, String fname, long filesize ) throws IOException {
    //assert  ! AppContext.getPartialFileList().isKnownPartialFile( fileid ) ; // as we can use only static methods as assertions: shoud be an exception then
	  
        
    tempFName = AppContext.getInstance().getConfig().getTempDirectory() + File.pathSeparator +  fileid /*.toString()*/ + ".tmp";
    fileTempHandle = new File( tempFName );
    if( fileTempHandle.exists() ) throw new IOException( "FileAlreadyExists" );
      
    fileRAHandle = new RandomAccessFile( tempFName, "ws" );
    fileRAHandle.setLength( filesize );
    fileRAHandle.close();
    fileRAHandle=null;
    this.fname = fname;
  }
  
  /**
  * used to load a PartialFile from persistent infos. ?? part.met.file
  */
  /*
  public PartialFile(  PartFileInfo info ) {
   info.getTempFile();
   info.lastWriten(); 
   info.fileID();
   info.getGapList();
   info.getHashList();
    fileID = id;
  }
  */
  public PartialFile() { // fname, fileid
	  
    setStatus( ST_Hashing );
    setPriority( PR_Normal );
  };
  
  // test purposes only: for the new PartMetFileReader-in-progress
  public PartialFile(FileID fileid ) {
    System.out.println( "PartialFile(" + fileid + ")" );
    this.fileID = fileid;
  }
  
  /**
  * returns the file channel we can use to write/read from data to the file.
  * we hold one channel per PartialFile. if there isnt a file channel set up, we set up one and *aquiere a lock* on this file. So nobody else can mess up with this file.
  */
  private FileChannel getFileChannel() throws IOException {
	if( fileChannel == null ) {
		if( fileRAHandle == null ) fileRAHandle = new RandomAccessFile( fileTempHandle, fileMode );
		fileChannel = fileRAHandle.getChannel();
	};
	
	return fileChannel;
  }
  
  public String getFName() {
    return fname;
  }
  
  public char getPriority() {
    return priority;
  }
  
  public void setPriority( char p ) {
    // assert
    priority = p;
  }
  
  public char getStatus() {
    return status;
  }
  
  public FileID getFileID() {
    System.out.println( "getFileID:" + fileID );
    return fileID;
  }
  
  public void setStatus( char s ) {
    /*
    assert ( status == ST_Hashing )
    || ( status == ST_Looking ) 
    || ( status == ST_Downloading )
    || ( status == ST_Paused )
    || ( status == ST_Completed )
    */
    
    // FIXME: transision check 
    // assert s == ST_Hashing && ( this.status == ST_Looking || this.status == ST_Downloading ...
    
    this.status = s;
  }
  
  public void setLastWriteTime( int time ) {// FIXME: ??? long and we shoud just check it and ggf. schedule to hashing
  }
  
  /**
  * sets the hashes from ???
  */
  public void setHashList( int count, ByteBuffer hashlist, int offset ) {
	  
	  }
	
public GapList getGapList() {
  return gapList;
}  
  /**
  * copies the data of <italic>length</italic> bytes from the <italic>data</italic> ByteBuffer starting at the given <italic>offset</italic> to the given <italic>fileStart</italic> position in the underlaying PartialFile.
  * This is interface to the ???
  */
  public void collectData( long fileStart, ByteBuffer data, int offset, int length ) {
	if( gapList.countGapsInSection( fileStart, fileStart + length ) == 0 ) return; // we have already collected all data in this block
	/* TODO: for now, we write the whole block of the data regardless of the fact, 
	there can be some already filled blocks, which we dont need to write again. 
	We can try to optimize it to write only the needed parts */
  ByteBuffer [] dataarray= { data }; // Java API 1.4.1 flaw: there isnt a write( ByteBuffer, offset, length ), but a write( ByteBuffer[], offset, length )
  try {
  FileChannel fc = getFileChannel();
	fc.position( fileStart );
	fc.write( dataarray, offset, length );
  gapList.removeSection( fileStart, fileStart + length );
  } catch( IOException io_err ) {
    io_err.printStackTrace();
    log.severe( "Cannot write data. Possible datalost." );
  };
	//statistics.collectedData( length );
  }
  
  public String toString() {
	return  PartialFile.class.getName() + "[" 
	  + "fileName:" + getFName() 
	  + ",fileID:" + getFileID().toString()
	  + ",status:" + ( CompileOptions.isVerboseObjTracingEnabled ? statusAsStr( getStatus()  ) : String.valueOf( getStatus() ) )
	  + ",priority:" + ( CompileOptions.isVerboseObjTracingEnabled ? priorityAsStr( getPriority()  ) : String.valueOf( getPriority() ) )
	  +"]";
  }
  
  
  private String statusAsStr( char s ) {
	  if( CompileOptions.isVerboseObjTracingEnabled ) {
      /*
      switch( s ) {
        case ST_Hashing: return "Hashing";
        case ST_Looking: return "Looking";
        case ST_Downloading: return "Downloading";
        case ST_Paused: return "Paused";
        case ST_Completed: return "Completed";
        // case ST_: return "";
        default: return "Unknown!("; // + s + ")";
      };
      */
      if( s == ST_Hashing ) return "Hashing";
      else if( s == ST_Looking ) return "Looking";
      else if( s == ST_Downloading ) return "Downloading";
      else if( s == ST_Paused ) return "Paused";
      else if( s == ST_Completed ) return "Completed";
      else return "Unknown!(" + s + ")";
    } else return String.valueOf( s );
  }
  
  private String priorityAsStr( char p ) {
	  if( CompileOptions.isVerboseObjTracingEnabled ) {
      /*
	switch( p ) {
		case PR_High: return "High";
		case PR_Normal: return "Normal";
		case PR_Low: return "Low";
		default: return "Unknow!(" + p + ")";
	};
      */
      if( p == PR_High ) return "High";
      else if( p == PR_Normal ) return "Normal";
      else if( p == PR_Low ) return "Low";
      else return "Unknown!(" + p + ")";
  } else return String.valueOf( p );
  }
  
  /**
*  for test purposes only.
  * gets a file name as argument: in_file, creates a new PartialFile, and than processes as follow:
  * starts a thread ( or given count of the threads ) which simulate(s) a client.
  * starts a second thread which simulates datablock consumer
  * 1) berechnet die hashesh, and setzt die HashList ( in den Blocks ) ( als ob wir mal ne antwort auf getHashList bekämen. )
  getRandom Positions from in_files, and transfers this data to the PartialFile throu the method collectData.
  manchmal verursacht es nen fehler in den transferierenden daten: dann sollte sich PartialFile beschweren
  */
  public static final void main( String[] args ) {
	  /*
    args[c].compareToIgnoreCase( "createDummy" ) { fileName = "dummyName_" + AppContext.createRandomString( 3 );
    args[c].compareToIgnoreCase( "createFromMet"
    args[c].compareToIgnoreCase( "gapList" )
	  */
  }
  
}

/* ????
class PartFileInfo {
  private char status;
  private char priority;
  
  private String fname;
  private FileID fileID; 
}
*/