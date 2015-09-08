/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFile.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


/**
 * This class contains the constants associated with checkpoint files. A
 * checkpoint file is a binary file that holds the passivation state of an
 * object server.
 */

/*
 * A checkpoint file is structured as follows:
 *
 *   checkpoint_file_header
 *   pm_state_record
 *   po_state_record
 *   po_state_record
 *        :
 *   po_state_record
 * 
 * Where checkpoint_file_header (see ChkptFileHeader.java) is structured as:
 *
 *   magic_cookie (2 bytes)
 *   version_number (2 bytes)
 *   num_entries (4 bytes, = number of pm state and po state records)
 *   creation_date (8 bytes)
 *   reserved (16 bytes)
 *
 * and pm_state_record is structured as:
 *
 *   state length (4 bytes, > 0)
 *   state (state length bytes)
 *
 * and po_state_record is structured as:
 *
 *   persistence_id (8 bytes)
 *   state length (4 bytes, >= 0)
 *   state (state length bytes, absent if the state length is zero)
 */
public class ChkptFile
{
  /** Magic cookie to identify checkpoint files. */
  public static final short MAGIC_COOKIE = (short)0x2000abcd;

  /** Checkpoint file version. */
  public static final short VERSION = 1;

  /** The size of a checkpoint file header (in bytes). */
  public static final int HDR_SIZE = ChkptFileHeader.HDR_SIZE;
}
