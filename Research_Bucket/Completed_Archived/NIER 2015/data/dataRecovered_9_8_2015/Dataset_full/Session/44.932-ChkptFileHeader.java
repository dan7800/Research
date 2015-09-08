/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFileHeader.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;
import java.util.*;

import vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile;


/**
 * This class represents the header of a checkpoint file.
 */

/*
 * The header is structured as:
 *
 *   magic_cookie (2 bytes)
 *   version_number (2 bytes)
 *   num_entries (4 bytes, = number of pm state and po state records)
 *   creation_date (8 bytes)
 *   reserved (16 bytes)
 *
 * See vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile.java.
 */
public class ChkptFileHeader
{
  /** The size of a checkpoint file header (in bytes). */
  public static final int HDR_SIZE = 32;

  /** The number of bytes in the header that are reserved. */
  private static final int SZ_RESERVED = 16;

  private int _numEntries;          // number of pm and po state entries
  private short _version;           // checkpoint file version
  private short _cookie;            // checkpoint file cookie
  private Date _creationDate;       // creation date


  /**
   * Instance creation. Sets the creation date to the current date.
   *
   * @param  numEntries  number of pm and po state entries of the
   *                     checkpoint file
   */
  public ChkptFileHeader(int numEntries)
  {
    this(numEntries, new Date());
  }


  /**
   * Instance creation.
   *
   * @param  numEntries  number of pm and po state entries of the
   *                     checkpoint file
   * @param  date        creation date
   */
  public ChkptFileHeader(int numEntries, Date date)
  {
    _numEntries = numEntries;
    _version = ChkptFile.VERSION;
    _cookie = ChkptFile.MAGIC_COOKIE;
    _creationDate = date;
  }

     
  /**
   * Read the header of the checkpoint file. Assumes that the file pointer
   * is at the start of the data to be read.
   *
   * @param  dis  input stream to read the header from
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ChkptFileHeader(DataInputStream dis)
    throws ChkptFileException, IOException
  {
    /*
     * Read and check the magic cookie.
     */
    if ( (_cookie = dis.readShort()) != ChkptFile.MAGIC_COOKIE) {
      throw new ChkptFileException("not a checkpoint file: magic cookie "
                                   + "doesn't match");
    }

    /*
     * Read the checkpoint file version number.
     */
    if ( (_version = dis.readShort()) != ChkptFile.VERSION) {
      throw new ChkptFileException("unsupported checkpoint file version: "
                                   + _version);
    }
 
    /*
     * Read the number of entries in the checkpoint file.
     */
    if ( (_numEntries = dis.readInt()) < 0) {
      throw new ChkptFileException("invalid number of entries: "
                                   + _numEntries);
    }

    /*
     * Read the creation date of the checkpoint file.
     */
    _creationDate = new Date(dis.readLong());

    /*
     * Read the reserved bytes.
     */
    byte buf[] = new byte[SZ_RESERVED];
    try {
      dis.readFully(buf);
    }
    catch(EOFException e) {
      throw new ChkptFileException("undersized header");
    }
  }


  /**
   * Set the value of the ``number of entries'' field.
   */
  public void setNumEntries(int n)
  {
    _numEntries = n;
  }


  /**
   * Return the value of the ``number of entries'' field.
   */
  public int getNumEntries()
  {
    return _numEntries;
  }


  /**
   * Set the creation date.
   */
  public void setCreationDate(Date date)
  {
    _creationDate = date;
  }


  /**
   * Return the creation date.
   */
  public Date getCreationDate()
  {
    return _creationDate;
  }


  /**
   * Return a byte representation of this ChkptFileHeader.
   *
   * @return the byte array
   *
   * @exception  IOException  if an I/O error occurs
   */
  public byte[] toByteArray()
    throws IOException
  {
    DataOutputStream dos;
    ByteArrayOutputStream baos;
    byte hdr[];
    byte buf[];

    baos = new ByteArrayOutputStream(HDR_SIZE);
    buf = new byte[SZ_RESERVED];
    dos = new DataOutputStream(baos);

    try {
      dos.writeShort(_cookie);
      dos.writeShort(_version);
      dos.writeInt(_numEntries);
      dos.writeLong(_creationDate.getTime());
      dos.write(buf);
      dos.flush();
      hdr = baos.toByteArray();
      return hdr;
    }
    finally {
      dos.close();
    }
  }
}
