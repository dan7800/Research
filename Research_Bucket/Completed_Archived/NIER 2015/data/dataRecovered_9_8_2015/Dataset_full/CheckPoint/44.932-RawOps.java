/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.util.comm;

import vu.globe.idlsys.g;
import vu.globe.util.comm.idl.rawData.*;

import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.exc.AssertionFailedException;

import vu.globe.util.types.SubByteArray;

/**
   Operations on the raw data type. These are all static. Unless otherwise
   specified, the operations follow the IDL specification in
   '//globe.vu/comm/rawData.idl'. The read/write operations are not implemented
   here. They can be found in RawCursor. As specified in the IDL module, none
   of these operations are thread safe.
*/

/*
   Notes.

   The IDL definition assumes C, where multiple headers can be allocated in one
   go. This is not the case with Java, where headers are separate objects.
   Therefore not much efficiency is to be gained by header preallocation. Until
   the new Java mapping is established, which *I think* will be closely tied
   to C, I am simply going to ignore this issue.

   In the current implementation, entries in the header sequence may be
   preallocated, but the actual header objects are not. Header objects which
   were allocated but subsequently invalidated are removed from the header
   sequence. In other words, invalid entries in the header sequence are
   always null.
*/

public class RawOps
{
   /** The number of header sequence space to preallocate in a raw. */
   static final int HDR_PREALLOC = 15;

   /** The number of bytes to preallocate in a data buffer. */
   static final int DATA_PREALLOC = 128;

   /**
      The maximum allowed 'allocated space / contents size' allowed by
      reallocRaw.
   */
   // just a guess
   static final int REALLOC_MAX_OVERHEAD = 5;

   /**
      Raws that are smaller than REALLOC_CHECK_THRESHOLD will never be
      reallocated by reallocRaw.
   */
   static final int REALLOC_CHECK_THRESHOLD = 1024;

   public static rawDef createRaw ()
   {
      rawDef result = new rawDef (); //'size' and 'nhdrs' implicitly set to zero
      result.hdrs = new rawHdrSeq (HDR_PREALLOC);

      return result;
   }


   //CHECKP>>
   /**
     The performance of the current checkpointing mechanism suffers
     badly from the design of the "raw" data structure. Raws are used
     to create an in-core copy of the object server's state. The problem
     is that raws grow incrementally and thus do a lot of small memory
     allocation operations. This new createRaw() method allocates a
     user-defined amount of memory in advance. Using this method in parts
     of the in-core copy of the server's state dramatically increases
     checkpointing performance: a server with 5000 objects, 3 MB state
     per object now checkpoints in 10 rather than 100 seconds.
     (Arno, 2-10-2002).
   */
   public static rawDef createRaw( int nhdrs, int bufsize ) 
   {							   
      rawDef r = new rawDef (); //'size' and 'nhdrs' implicitly set to zero
      r.hdrs = new rawHdrSeq( nhdrs );
      
      byteSeq buf = new byteSeq( bufsize );
      rawHdr hdr = createHeader( buf, 0, 0 );
      hdr.owns_buf = g.bool_Inf.True;
      hdr.raw_off = 0;
      r.hdrs.v[ 0 ] = hdr;
      r.size = 0;
      r.nvalid = 1;

      return r;
   }
  
  
   
   public static void setRaw (rawDef r, byte [] bytes, int off, int len)
   {  
      // place everything in one buf, one header.

      invalidate (r);
      ensureHdrSpace (r, 1);
      
      byteSeq buf = new byteSeq (0);
      buf.v = bytes;

      rawHdr hdr = createHeader (buf, off, len);

      r.size = len;
      r.nvalid = 1;
      r.hdrs.v [0] = hdr;

   }

   
   public static void setRawCopy (rawDef r, byte [] bytes, int off, int len)
   {
      // similar to setRaw, but creates a new byte array. Extra room is
      // allocated. The user data is placed at the beginning of the new byte
      // array.

      // place everything in one buf, one header.

      invalidate (r);
      ensureHdrSpace (r, 1);
      
      byteSeq buf = new byteSeq (len + DATA_PREALLOC);
      System.arraycopy (bytes, off, buf.v, 0, len);

      rawHdr hdr = createHeader (buf, 0, len);
      hdr.owns_buf = g.bool_Inf.True;

      r.size = len;
      r.nvalid = 1;
      r.hdrs.v [0] = hdr;
   }


   public static byte [] getRaw (rawDef r)
   {
      // note: non-copying the entire raw is a *very* special case

      // a) one header
      if (r.nvalid == 1) {

         rawHdr hdr = r.hdrs.v [0];

         // b) one *whole* buffer
         if (hdr.data_start == 0 && hdr.data_len == hdr.data_buf.v.length)
            return r.hdrs.v [0].data_buf.v;
      }
      return getRawCopy (r);
   }

   public static SubByteArray getRawSubByteArray( rawDef r )
   {
      // note: non-copying the entire raw is a *very* special case
      // Arno: less special now...

      // only if one header
      if (r.nvalid == 1) 
      {
         rawHdr hdr = r.hdrs.v [0];
         return new SubByteArray( hdr.data_buf.v, hdr.data_start, hdr.data_len );
      }
      byte[] buf = getRawCopy (r);
      return new SubByteArray( buf, 0, buf.length );
   }

   
   public static byte [] getRawCopy (rawDef r)
   {
      byte [] result = new byte [r.size];

      // loop through valid headers
      for (int h = 0; h < r.nvalid; h++) {

         rawHdr hdr = r.hdrs.v [h];

         // copy data_buf [data_start ... data_start + data_len - 1] to
         // result [offset ... offset + data_len - 1]
         System.arraycopy (hdr.data_buf.v, hdr.data_start, result, hdr.raw_off,
                           hdr.data_len);
      }

      return result;
   }

   public static int sizeOfRaw (rawDef r)
   {
      return r.size;
   }

   public static void truncRaw (rawDef r, int head, int tail)
                                       throws InvalidRawOffsetException
   {
      // The bytes between head and tail must be preserved. First determine a
      // subrange of headers that contain data to be preserved. The first and
      // last header of the subrange will then be adjusted. Then all preserved
      // headers will be copied to the beginning of the header space, and their
      // offsets adjusted. Remaining headers are invalidated. Finally, the
      // nvalid and size fields of the raw are updated.
 
      int org_len = r.size;

      if (org_len - head - tail < 0)
         throw new InvalidRawOffsetException ("head or tail too big");

      // first exclude trivial case, ensuring that at least one byte, and thus
      // one header, will be preserved

      if (org_len - head - tail == 0) {
         invalidate (r);
         return;
      }

      // the first preserved byte is 'head'
      // the last preserved byte is 'org_len - tail - 1'

      // locate first header that must be (partially) preserved
      int first_h = searchHeaderForward (r, 0, head);

      // locate last header that must be (partially) preserved
      int last_h = searchHeaderForward (r, first_h, org_len - tail - 1);
 
      rawHdr first_hdr = r.hdrs.v [first_h];
      rawHdr last_hdr = r.hdrs.v [last_h];

      // note: first and last header may be the same!

      // see how many bytes to chop off first_hdr and last_hdr respectively
      int adjust_first = head - first_hdr.raw_off;
      int adjust_last = last_hdr.raw_off + last_hdr.data_len - (org_len - tail);

      // adjust the first and last headers
      first_hdr.data_start += adjust_first;
      first_hdr.data_len -= adjust_first;
      last_hdr.data_len -= adjust_last;
 
      // Loop:
      // 1. copy r [first_h ... last_h] to r [0 ...] 
      // 2. recalculate headers' raw_off fields

      int next_offset = 0;
      for (int h = first_h; h <= last_h; h++) {

         rawHdr hdr = r.hdrs.v [h];

         r.hdrs.v [h - first_h] = hdr;

         hdr.raw_off = next_offset;
         next_offset += hdr.data_len;
      }
      
      // invalidate remaining headers. Start invalidating at position 'nhdrs'.
      int nhdrs = last_h - first_h + 1;
      for (int h = nhdrs; h < r.nvalid; h++)
         r.hdrs.v [h] = null;

      // set new raw fields

      r.nvalid = nhdrs;
      setSize (r);
   }

   public static rawDef reallocRaw (rawDef r)
   {
      // don't bother with fragmentation costs, just examine storage allocated
      
      int allocated = 0;

      // loop through valid headers
      for (int h = 0; h < r.nvalid; h++) {

         rawHdr hdr = r.hdrs.v [h];
         allocated += hdr.data_buf.v.length;
      }

      if (allocated <= REALLOC_CHECK_THRESHOLD)
         return r;

      if (allocated / r.size > REALLOC_MAX_OVERHEAD)
         return copyRaw (r);
      else
         return r;
   }

   public static rawDef cutRawHead (rawDef r, int head_len)
                                       throws InvalidRawOffsetException
   {
      // probably not very efficient

      int raw_len = r.size;
      rawDef result = dupRaw (r);

      truncRaw (result, 0, raw_len - head_len);
      truncRaw (r, head_len, 0);

      return result;
   }


   public static rawDef cutRawTail (rawDef r, int tail_len)
                                       throws InvalidRawOffsetException
   {
      // probably not very efficient

      int raw_len = r.size;
      rawDef result = dupRaw (r);

      truncRaw (r, 0, tail_len);
      truncRaw (result, raw_len - tail_len, 0);

      return result;
   }


   public static void appendRaw (rawDef r, rawDef conc)
   {
      int total_hdrs = r.nvalid + conc.nvalid;

      // allocate enough header space
      ensureHdrSpace (r, total_hdrs);

      // copy conc's headers after r's, adjusting the owns_buf and raw_off
      // fields
      for (int h = 0; h < conc.nvalid; h++) {

         rawHdr hdr = copyHdr (conc.hdrs.v [h]); // adjusts owns_buf
         r.hdrs.v [r.nvalid + h] = hdr;

         hdr.raw_off += r.size;
      }

      // update r's fields
      r.nvalid = total_hdrs;
      setSize (r);
   }

   public static void prependRaw (rawDef r, rawDef conc)
   {
      int total_hdrs = r.nvalid + conc.nvalid;

      // allocate enough header space
      ensureHdrSpace (r, total_hdrs);

      // 1. move r's headers to their new position
      // 2. copy conc's headers to the beginning of r

      // move r headers. adjust raw_off field
      // the headers are moved in reverse to avoid overwriting
      for (int h = r.nvalid - 1; h >= 0; h--) {
         rawHdr hdr = r.hdrs.v [h];
         r.hdrs.v [conc.nvalid + h] = hdr;
         hdr.raw_off += conc.size;
      }

      // copy conc headers, adjusting the owns_buf field. The raw_off field
      // remains the same.

      for (int h = 0; h < conc.nvalid; h++) {
         rawHdr hdr = copyHdr (conc.hdrs.v [h]); // adjusts owns_buf
         r.hdrs.v [h] = hdr;
      }

      // update r's fields
      r.nvalid = total_hdrs;
      setSize (r);
   }

   public static rawDef dupRaw (rawDef r)
   {
      // create a new raw with the same amount of header space
      rawDef result = new rawDef ();// 'size' and 'nhdrs' implicitly set to zero
      result.hdrs = new rawHdrSeq (r.hdrs.v.length);

      // copy r's valid headers, adjusting the owns_buf field. The raw_off
      // field remains the same.
      for (int h = 0; h < r.nvalid; h++) {
         rawHdr hdr = copyHdr (r.hdrs.v [h]); // adjusts owns_buf
         result.hdrs.v [h] = hdr;
      }

      // set result's fields
      result.nvalid = r.nvalid;
      result.size = r.size;

      return result;
   }


   public static rawDef dupRawSection (rawDef r, int off, int len)
                                       throws InvalidRawOffsetException
   {
      rawDef result = dupRaw (r);
      truncRaw (result, off, r.size - (off + len));
      return result;
   }

   public static rawDef copyRaw (rawDef r)
   {
      // copy everything into one buf

      byte [] bytes = getRawCopy (r);
      rawDef result = createRaw ();
      setRaw (result, bytes, 0, bytes.length);

      return result;
   }

   public static byte readRawByte (rawDef r, int off)
                                       throws InvalidRawOffsetException
   {
      if (off >= r.size)
         throw new InvalidRawOffsetException ();

      int h = searchHeaderForward (r, 0, off);
      rawHdr hdr = r.hdrs.v [h];

      return hdr.data_buf.v [hdr.data_start + off - hdr.raw_off];
   }

   public static void writeRawByte (rawDef r, int off, byte b)
   {
      int h = ensureOffset (r, off);
      rawHdr hdr = r.hdrs.v [h];
      hdr.data_buf.v [hdr.data_start + off - hdr.raw_off] = b;
   }

   public static void readRawArray (rawDef r, byte [] bytes, int rawOff,
                     int arrayOff, int len) throws InvalidRawOffsetException
   {
      if (len == 0)
         return;
 
      // check existence of required bytes
      if (rawOff + len - 1 >= r.size)
         throw new InvalidRawOffsetException ();

      int h = searchHeaderForward (r, 0, rawOff);

      // loop over headers until len reaches zero, updating arrayOff, rawOff, h
      while (len > 0) {
 
         rawHdr hdr = r.hdrs.v [h];
         int nread = readHeaderArray (hdr, bytes, rawOff - hdr.raw_off,
                                    arrayOff, len);
      
         len -= nread;
         arrayOff += nread;
         rawOff += nread;
         h++;
      }
   }

   public static void writeRawArray (rawDef r, byte [] bytes, int rawOff,
                                    int arrayOff, int len)
   {
      if (len == 0)
         return;
 
      // Allocate enough storage.
      ensureOffset (r, rawOff + len - 1);

      int h = searchHeaderForward (r, 0, rawOff);

      // loop over headers until len reaches zero, updating arrayOff, rawOff, h

      while (len > 0) {
 
         rawHdr hdr = r.hdrs.v [h];
         int nwritten = writeHeaderArray (hdr, bytes, rawOff - hdr.raw_off,
                                          arrayOff, len);
 
         len -= nwritten;
         arrayOff += nwritten;
         rawOff += nwritten;
         h++;
      }
   }


   /************************** Non-public methods  **************************/

   /**
      Allocates a new header. The data_buf, data_start and data_len fields
      are set to the parameters of this method. The owns_buf field is set to
      false and the raw_off field to zero.
   */
   static rawHdr createHeader (byteSeq data_buf, int data_start, int data_len)
   {
      rawHdr hdr = new rawHdr ();

      hdr.data_buf = data_buf;
      hdr.data_start = data_start;
      hdr.data_len = data_len;
      // remaining fields are OK

      return hdr;
   }

   /**
      Creates a new header and copies hdr's contents to it. The copy's
      owns_buf field is set to false.
   */
   static rawHdr copyHdr (rawHdr hdr)
   {
      rawHdr copy = new rawHdr ();

      // copy.owns_buf implicitly set to false on creation
      copy.raw_off = hdr.raw_off;
      copy.data_buf = hdr.data_buf;
      copy.data_start = hdr.data_start;
      copy.data_len = hdr.data_len;

      return copy;
   }  

   /**
      Ensures that the header sequence can accommodate at least 'nhdrs'
      headers. The header objects themselves are not allocted. The number of
      valid headers does not change.
   */
   static void ensureHdrSpace (rawDef r, int nhdrs)
   {
      if (r.hdrs.v.length < nhdrs) {

         // reallocate
         rawHdr [] saved = r.hdrs.v;
         r.hdrs.v = new rawHdr [nhdrs + HDR_PREALLOC];

         // note: invalid headers are also copied
         System.arraycopy (saved, 0, r.hdrs.v, 0, saved.length);
      }
   }

   /**
      Extends the size of a raw. The additional 'nbytes' bytes are all placed
      in a new header and buffer, and the raw's fields are all updated. In
      addition, room may be preallocated inside the new buffer. As usual, the
      preallocated space is not part of the raw.

      @return
         the new header index
   */
   static int addRawHeader (rawDef r, int nbytes)
   {
      // allocate and set a header object
      byteSeq buf = new byteSeq (nbytes + DATA_PREALLOC);
      rawHdr hdr = createHeader (buf, 0, nbytes);
      hdr.owns_buf = g.bool_Inf.True;
      hdr.raw_off = r.size;
 
      // place the header object in header space, reallocating header
      // space if necessary
      int h = r.nvalid; // index of the new header
      ensureHdrSpace (r, h + 1);
      r.hdrs.v [h] = hdr;
 
      // adjust r
      r.nvalid++;
      r.size += nbytes;

      return h;
   }

   /**
      Invalidates all headers in a raw, and updates the raw fields.
   */
   static void invalidate (rawDef r)
   {
      for (int h = 0; h < r.nvalid; h++)
         r.hdrs.v [h] = null;
      r.size = r.nvalid = 0;
   }

   /**
      Calculates the size of the raw and sets its size field. As a
      precondition, the raw's nvalid field, and all fields in all headers must
      be set.
   */
   static void setSize (rawDef r)
   {
      if (r.nvalid == 0)
         r.size = 0;
      else {
         rawHdr last_hdr = r.hdrs.v [r.nvalid - 1];
         r.size = last_hdr.raw_off + last_hdr.data_len;
      }
   }

   /**
      Starting with from_hdr, searches forward for the header containing
      'offset'. If not found, then returns the first header beyond the raw,
      possibly a non-allocated one.
      <p>
      As a precondition, from_hdr must itself either be a valid header or the
      first (non-allocated) header beyond the raw.

      @return
         the header index
   */
   static int searchHeaderForward (rawDef r, int from_hdr, int offset)
   {
      // loop forward over headers
      for ( ; from_hdr < r.nvalid; from_hdr++) {
 
         rawHdr hdr = r.hdrs.v [from_hdr];
         if (offset >= hdr.raw_off && offset - hdr.raw_off < hdr.data_len) {
            // found the header
            break;
         }
      }
      // a header may or may not have been found
      return from_hdr;
   }

   /**
      Ensures that the raw extends to offset 'off'. If necessary extends the
      raw, possibly using preallocated space.

      @return
         the header index of the header containing 'off'.
   */
   static int ensureOffset (rawDef r, int off)
   {
      if (off < r.size)
         return searchHeaderForward (r, 0, off);
 
      // additional number of bytes needed by the raw
      int add_bytes = off - r.size + 1;

      // get the last valid buffer
      int h = r.nvalid - 1;
      rawHdr hdr = h >= 0 ? r.hdrs.v [h] : null;

      // Extend the raw.
      // See if we can use preallocated space in the final buffer. For this,
      // the raw must own the buffer.

      if (
         // is there a buffer?
         hdr != null &&
         // ownership?
         hdr.owns_buf == g.bool_Inf.True &&
         // enough preallocated space?
         hdr.data_buf.v.length - hdr.data_start - hdr.data_len >= add_bytes
      ) {
         // extend the header into preallocated space
         hdr.data_len += add_bytes;
         // extend the raw
         r.size += add_bytes;
 
         // done
         return h;
      }
      else {
         // extend the raw by adding a new header
         return addRawHeader (r, add_bytes);
      }
   }

   /**
      Reads a number of bytes from a header into an array. The number of
      bytes read is the minimum of 'len' and those available in the header.
 
      @param arrayOff
         the offset in the array to copy the first byte to
      @return
         the number of bytes copied
   */
   static int readHeaderArray (rawHdr hdr, byte [] bytes, int hdrOff,
                           int arrayOff, int len)
   {
      int hdr_bytes_avail = hdr.data_len - hdrOff;
      int copy_len = Math.min (hdr_bytes_avail, len);
 
      /*
         Copy from: hdr.data_buf [hdr.data_start + hdrOff]
         Copy to:   bytes [arrayOff]
         Copy len:  copy_len
      */
      System.arraycopy (hdr.data_buf.v, hdr.data_start + hdrOff, bytes,
                        arrayOff, copy_len);

      return copy_len;
   }

   /**
      Writes a number of bytes from an array into a header, by overwriting
      existing bytes in the header. The number of bytes written is the minimum
      of 'len' and the header space left.
 
      @param arrayOff
         the offset in the array to copy the first byte from
      @return
         the number of bytes copied
   */
   static int writeHeaderArray (rawHdr hdr, byte [] bytes, int hdrOff,
                                    int arrayOff, int len)
   {
      int hdr_bytes_avail = hdr.data_len - hdrOff;
      int copy_len = Math.min (hdr_bytes_avail, len);

      /*
         Copy from: bytes [arrayOff]
         Copy to:   hdr.data_buf [hdr.data_start + hdrOff]
         Copy len:  copy_len
      */
      System.arraycopy (bytes, arrayOff, hdr.data_buf.v,
                        hdr.data_start + hdrOff, copy_len);
      return copy_len;
   }

   /************************** Debugging methods  **************************/

   /**
      Debugging method. Goes through all of r's headers checking if their
      'raw_off' fields are correct. Also checks r's 'size' field.

      @exception AssertionFailedException
         if the test failed
   */
   public static void debugChkRaw (rawDef r)
   {
      int offset = 0;

      // loop through valid headers
      for (int h = 0; h < r.nvalid; h++) {

         rawHdr hdr = r.hdrs.v [h];
         if (hdr.raw_off != offset)
            throw new AssertionFailedException ("header no " + h +
                  " has offset " + hdr.raw_off + ", should be " + offset);
         offset += hdr.data_len;
      }

      int compute_size;
      if (r.nvalid == 0)
         compute_size = 0;
      else {
         rawHdr last_hdr = r.hdrs.v [r.nvalid - 1];
         compute_size = last_hdr.raw_off + last_hdr.data_len;
      }

      if (r.size != compute_size)
         throw new AssertionFailedException ("raw has size " + r.size +
                  ", should be " + compute_size);
   }

   /**
      Debugging method. Dumps info about r.
   */
   public static void debugDumpRaw (rawDef r)
   {
      System.out.println ("debugDumpRaw: r.size = " + r.size);
      System.out.println ("debugDumpRaw: r.nvalid = " + r.nvalid);

      // loop through valid headers
      for (int h = 0; h < r.nvalid; h++) {

         rawHdr hdr = r.hdrs.v [h];
         System.out.println ("debugDumpRaw: header " + h + ":");
         System.out.println ("debugDumpRaw:    raw_off = " + hdr.raw_off);
         System.out.println ("debugDumpRaw:    data_len = " + hdr.data_len);
         System.out.println ("debugDumpRaw:    data_start = " + hdr.data_start);
         System.out.println ("debugDumpRaw:    owns_buf = " + hdr.owns_buf);
         System.out.println ("debugDumpRaw:    extra buf space = " +
                     (hdr.data_buf.v.length - hdr.data_len - hdr.data_start));
      }

      System.out.println ("");
   }

}
