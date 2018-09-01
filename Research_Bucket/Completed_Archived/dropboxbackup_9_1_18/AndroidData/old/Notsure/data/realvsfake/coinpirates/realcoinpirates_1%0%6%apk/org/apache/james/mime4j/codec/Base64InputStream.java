package org.apache.james.mime4j.codec;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Base64InputStream extends InputStream
{
  private static final int[] BASE64_DECODE;
  private static final byte BASE64_PAD = 61;
  private static final int ENCODED_BUFFER_SIZE = 1536;
  private static final int EOF = -1;
  private static Log log;
  private boolean closed = false;
  private final byte[] encoded = new byte[1536];
  private boolean eof;
  private final InputStream in;
  private int position = 0;
  private final ByteQueue q = new ByteQueue();
  private final byte[] singleByte = new byte[1];
  private int size = 0;
  private boolean strict;

  static
  {
    if (!Base64InputStream.class.desiredAssertionStatus());
    for (boolean bool = true; ; bool = false)
    {
      $assertionsDisabled = bool;
      log = LogFactory.getLog(Base64InputStream.class);
      BASE64_DECODE = new int[256];
      for (int i = 0; i < 256; i++)
        BASE64_DECODE[i] = -1;
    }
    for (int j = 0; j < Base64OutputStream.BASE64_TABLE.length; j++)
      BASE64_DECODE[(0xFF & Base64OutputStream.BASE64_TABLE[j])] = j;
  }

  public Base64InputStream(InputStream paramInputStream)
  {
    this(paramInputStream, false);
  }

  public Base64InputStream(InputStream paramInputStream, boolean paramBoolean)
  {
    if (paramInputStream == null)
      throw new IllegalArgumentException();
    this.in = paramInputStream;
    this.strict = paramBoolean;
  }

  private int decodePad(int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws IOException
  {
    this.eof = true;
    if (paramInt2 == 2)
    {
      byte b3 = (byte)(paramInt1 >>> 4);
      if (paramInt3 < paramInt4)
      {
        int m = paramInt3 + 1;
        paramArrayOfByte[paramInt3] = b3;
        return m;
      }
      this.q.enqueue(b3);
      return paramInt3;
    }
    if (paramInt2 == 3)
    {
      byte b1 = (byte)(paramInt1 >>> 10);
      byte b2 = (byte)(0xFF & paramInt1 >>> 2);
      if (paramInt3 < paramInt4 - 1)
      {
        int j = paramInt3 + 1;
        paramArrayOfByte[paramInt3] = b1;
        int k = j + 1;
        paramArrayOfByte[j] = b2;
        return k;
      }
      if (paramInt3 < paramInt4)
      {
        int i = paramInt3 + 1;
        paramArrayOfByte[paramInt3] = b1;
        this.q.enqueue(b2);
        return i;
      }
      this.q.enqueue(b1);
      this.q.enqueue(b2);
      return paramInt3;
    }
    handleUnexpecedPad(paramInt2);
    return paramInt3;
  }

  private void handleUnexpecedPad(int paramInt)
    throws IOException
  {
    if (this.strict)
      throw new IOException("unexpected padding character");
    log.warn("unexpected padding character; dropping " + paramInt + " sextet(s)");
  }

  private void handleUnexpectedEof(int paramInt)
    throws IOException
  {
    if (this.strict)
      throw new IOException("unexpected end of file");
    log.warn("unexpected end of file; dropping " + paramInt + " sextet(s)");
  }

  private int read0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = this.q.count();
    int i12;
    for (int j = paramInt1; ; j = i12)
    {
      int k = i - 1;
      if ((i <= 0) || (j >= paramInt2))
        break;
      i12 = j + 1;
      paramArrayOfByte[j] = this.q.dequeue();
      i = k;
    }
    if (this.eof)
    {
      if (j == paramInt1);
      for (int i11 = -1; ; i11 = j - paramInt1)
        return i11;
    }
    int m = 0;
    int n = 0;
    int i1 = j;
    while (i1 < paramInt2)
    {
      int i3;
      int i4;
      while (this.position == this.size)
      {
        int i10 = this.in.read(this.encoded, 0, this.encoded.length);
        if (i10 == -1)
        {
          this.eof = true;
          if (n != 0)
            handleUnexpectedEof(n);
          if (i1 == paramInt1)
            return -1;
          return i1 - paramInt1;
        }
        if (i10 > 0)
        {
          this.position = 0;
          this.size = i10;
        }
        else if ((!$assertionsDisabled) && (i10 != 0))
        {
          throw new AssertionError();
          i4 = BASE64_DECODE[i3];
          if (i4 >= 0)
            break label290;
        }
      }
      label290: byte b1;
      byte b2;
      byte b3;
      while (true)
      {
        if ((this.position >= this.size) || (i1 >= paramInt2))
          break label382;
        byte[] arrayOfByte = this.encoded;
        int i2 = this.position;
        this.position = (i2 + 1);
        i3 = 0xFF & arrayOfByte[i2];
        if (i3 != 61)
          break;
        return decodePad(m, n, paramArrayOfByte, i1, paramInt2) - paramInt1;
        m = i4 | m << 6;
        n++;
        if (n == 4)
        {
          b1 = (byte)(m >>> 16);
          b2 = (byte)(m >>> 8);
          b3 = (byte)m;
          if (i1 >= paramInt2 - 2)
            break label384;
          int i7 = i1 + 1;
          paramArrayOfByte[i1] = b1;
          int i8 = i7 + 1;
          paramArrayOfByte[i7] = b2;
          int i9 = i8 + 1;
          paramArrayOfByte[i8] = b3;
          i1 = i9;
          n = 0;
        }
      }
      label382: continue;
      label384: if (i1 < paramInt2 - 1)
      {
        int i6 = i1 + 1;
        paramArrayOfByte[i1] = b1;
        i1 = i6 + 1;
        paramArrayOfByte[i6] = b2;
        this.q.enqueue(b3);
      }
      while ((!$assertionsDisabled) && (i1 != paramInt2))
      {
        throw new AssertionError();
        if (i1 < paramInt2)
        {
          int i5 = i1 + 1;
          paramArrayOfByte[i1] = b1;
          this.q.enqueue(b2);
          this.q.enqueue(b3);
          i1 = i5;
        }
        else
        {
          this.q.enqueue(b1);
          this.q.enqueue(b2);
          this.q.enqueue(b3);
        }
      }
      return paramInt2 - paramInt1;
    }
    assert (n == 0);
    assert (i1 == paramInt2);
    return paramInt2 - paramInt1;
  }

  public void close()
    throws IOException
  {
    if (this.closed)
      return;
    this.closed = true;
  }

  public int read()
    throws IOException
  {
    if (this.closed)
      throw new IOException("Base64InputStream has been closed");
    int i;
    do
    {
      i = read0(this.singleByte, 0, 1);
      if (i == -1)
        return -1;
    }
    while (i != 1);
    return 0xFF & this.singleByte[0];
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    if (this.closed)
      throw new IOException("Base64InputStream has been closed");
    if (paramArrayOfByte == null)
      throw new NullPointerException();
    if (paramArrayOfByte.length == 0)
      return 0;
    return read0(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.closed)
      throw new IOException("Base64InputStream has been closed");
    if (paramArrayOfByte == null)
      throw new NullPointerException();
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length))
      throw new IndexOutOfBoundsException();
    if (paramInt2 == 0)
      return 0;
    return read0(paramArrayOfByte, paramInt1, paramInt1 + paramInt2);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.Base64InputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */