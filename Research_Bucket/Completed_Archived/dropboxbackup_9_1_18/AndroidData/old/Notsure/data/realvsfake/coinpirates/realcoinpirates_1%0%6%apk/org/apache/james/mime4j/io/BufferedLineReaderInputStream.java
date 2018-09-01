package org.apache.james.mime4j.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public class BufferedLineReaderInputStream extends LineReaderInputStream
{
  private byte[] buffer;
  private int buflen;
  private int bufpos;
  private final int maxLineLen;
  private boolean truncated;

  public BufferedLineReaderInputStream(InputStream paramInputStream, int paramInt)
  {
    this(paramInputStream, paramInt, -1);
  }

  public BufferedLineReaderInputStream(InputStream paramInputStream, int paramInt1, int paramInt2)
  {
    super(paramInputStream);
    if (paramInputStream == null)
      throw new IllegalArgumentException("Input stream may not be null");
    if (paramInt1 <= 0)
      throw new IllegalArgumentException("Buffer size may not be negative or zero");
    this.buffer = new byte[paramInt1];
    this.bufpos = 0;
    this.buflen = 0;
    this.maxLineLen = paramInt2;
    this.truncated = false;
  }

  private void expand(int paramInt)
  {
    byte[] arrayOfByte = new byte[paramInt];
    int i = this.buflen - this.bufpos;
    if (i > 0)
      System.arraycopy(this.buffer, this.bufpos, arrayOfByte, this.bufpos, i);
    this.buffer = arrayOfByte;
  }

  public byte[] buf()
  {
    return this.buffer;
  }

  public int capacity()
  {
    return this.buffer.length;
  }

  public byte charAt(int paramInt)
  {
    if ((paramInt < this.bufpos) || (paramInt > this.buflen))
      throw new IndexOutOfBoundsException();
    return this.buffer[paramInt];
  }

  public void clear()
  {
    this.bufpos = 0;
    this.buflen = 0;
  }

  public void ensureCapacity(int paramInt)
  {
    if (paramInt > this.buffer.length)
      expand(paramInt);
  }

  public int fillBuffer()
    throws IOException
  {
    if (this.bufpos > 0)
    {
      int m = this.buflen - this.bufpos;
      if (m > 0)
        System.arraycopy(this.buffer, this.bufpos, this.buffer, 0, m);
      this.bufpos = 0;
      this.buflen = m;
    }
    int i = this.buflen;
    int j = this.buffer.length - i;
    int k = this.in.read(this.buffer, i, j);
    if (k == -1)
      return -1;
    this.buflen = (i + k);
    return k;
  }

  public boolean hasBufferedData()
  {
    return this.bufpos < this.buflen;
  }

  public int indexOf(byte paramByte)
  {
    return indexOf(paramByte, this.bufpos, this.buflen - this.bufpos);
  }

  public int indexOf(byte paramByte, int paramInt1, int paramInt2)
  {
    if ((paramInt1 < this.bufpos) || (paramInt2 < 0) || (paramInt1 + paramInt2 > this.buflen))
      throw new IndexOutOfBoundsException();
    for (int i = paramInt1; i < paramInt1 + paramInt2; i++)
      if (this.buffer[i] == paramByte)
        return i;
    return -1;
  }

  public int indexOf(byte[] paramArrayOfByte)
  {
    return indexOf(paramArrayOfByte, this.bufpos, this.buflen - this.bufpos);
  }

  public int indexOf(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramArrayOfByte == null)
      throw new IllegalArgumentException("Pattern may not be null");
    if ((paramInt1 < this.bufpos) || (paramInt2 < 0) || (paramInt1 + paramInt2 > this.buflen))
      throw new IndexOutOfBoundsException();
    if (paramInt2 < paramArrayOfByte.length)
      return -1;
    int[] arrayOfInt = new int[256];
    for (int i = 0; i < arrayOfInt.length; i++)
      arrayOfInt[i] = (1 + paramArrayOfByte.length);
    for (int j = 0; j < paramArrayOfByte.length; j++)
      arrayOfInt[(0xFF & paramArrayOfByte[j])] = (paramArrayOfByte.length - j);
    int k = 0;
    while (true)
    {
      int i2;
      if (k <= paramInt2 - paramArrayOfByte.length)
      {
        int m = paramInt1 + k;
        int n = 1;
        for (int i1 = 0; ; i1++)
          if (i1 < paramArrayOfByte.length)
          {
            if (this.buffer[(m + i1)] != paramArrayOfByte[i1])
              n = 0;
          }
          else
          {
            if (n == 0)
              break;
            return m;
          }
        i2 = m + paramArrayOfByte.length;
        if (i2 < this.buffer.length);
      }
      else
      {
        return -1;
      }
      k += arrayOfInt[(0xFF & this.buffer[i2])];
    }
  }

  public int length()
  {
    return this.buflen - this.bufpos;
  }

  public int limit()
  {
    return this.buflen;
  }

  public boolean markSupported()
  {
    return false;
  }

  public int pos()
  {
    return this.bufpos;
  }

  public int read()
    throws IOException
  {
    if (this.truncated)
      return -1;
    while (!hasBufferedData())
      if (fillBuffer() == -1)
        return -1;
    byte[] arrayOfByte = this.buffer;
    int i = this.bufpos;
    this.bufpos = (i + 1);
    return 0xFF & arrayOfByte[i];
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    if (this.truncated)
      return -1;
    if (paramArrayOfByte == null)
      return 0;
    return read(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.truncated)
      return -1;
    if (paramArrayOfByte == null)
      return 0;
    while (!hasBufferedData())
      if (fillBuffer() == -1)
        return -1;
    int i = this.buflen - this.bufpos;
    if (i > paramInt2)
      i = paramInt2;
    System.arraycopy(this.buffer, this.bufpos, paramArrayOfByte, paramInt1, i);
    this.bufpos = (i + this.bufpos);
    return i;
  }

  public int readLine(ByteArrayBuffer paramByteArrayBuffer)
    throws IOException
  {
    if (paramByteArrayBuffer == null)
      throw new IllegalArgumentException("Buffer may not be null");
    if (this.truncated)
      return -1;
    int i = 0;
    int j = 0;
    int k = 0;
    if (j == 0)
    {
      if (!hasBufferedData())
      {
        k = fillBuffer();
        if (k != -1);
      }
    }
    else
    {
      if ((i != 0) || (k != -1))
        break label160;
      return -1;
    }
    int m = indexOf((byte)10);
    if (m != -1)
      j = 1;
    for (int n = m + 1 - pos(); ; n = length())
    {
      if (n > 0)
      {
        paramByteArrayBuffer.append(buf(), pos(), n);
        skip(n);
        i += n;
      }
      if ((this.maxLineLen <= 0) || (paramByteArrayBuffer.length() < this.maxLineLen))
        break;
      throw new MaxLineLimitException("Maximum line length limit exceeded");
    }
    label160: return i;
  }

  public int skip(int paramInt)
  {
    int i = Math.min(paramInt, this.buflen - this.bufpos);
    this.bufpos = (i + this.bufpos);
    return i;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("[pos: ");
    localStringBuilder.append(this.bufpos);
    localStringBuilder.append("]");
    localStringBuilder.append("[limit: ");
    localStringBuilder.append(this.buflen);
    localStringBuilder.append("]");
    localStringBuilder.append("[");
    for (int i = this.bufpos; i < this.buflen; i++)
      localStringBuilder.append((char)this.buffer[i]);
    localStringBuilder.append("]");
    return localStringBuilder.toString();
  }

  public void truncate()
  {
    clear();
    this.truncated = true;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.BufferedLineReaderInputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */