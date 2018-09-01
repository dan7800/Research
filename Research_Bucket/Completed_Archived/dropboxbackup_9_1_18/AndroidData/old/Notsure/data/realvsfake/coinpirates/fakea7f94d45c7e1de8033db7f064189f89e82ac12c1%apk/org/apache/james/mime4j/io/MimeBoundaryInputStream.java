package org.apache.james.mime4j.io;

import java.io.IOException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public class MimeBoundaryInputStream extends LineReaderInputStream
{
  private boolean atBoundary;
  private final byte[] boundary;
  private int boundaryLen;
  private BufferedLineReaderInputStream buffer;
  private boolean completed;
  private boolean eof;
  private boolean lastPart;
  private int limit;

  public MimeBoundaryInputStream(BufferedLineReaderInputStream paramBufferedLineReaderInputStream, String paramString)
    throws IOException
  {
    super(paramBufferedLineReaderInputStream);
    if (paramBufferedLineReaderInputStream.capacity() <= paramString.length())
      throw new IllegalArgumentException("Boundary is too long");
    this.buffer = paramBufferedLineReaderInputStream;
    this.eof = false;
    this.limit = -1;
    this.atBoundary = false;
    this.boundaryLen = 0;
    this.lastPart = false;
    this.completed = false;
    this.boundary = new byte[2 + paramString.length()];
    this.boundary[0] = 45;
    this.boundary[1] = 45;
    for (int i = 0; i < paramString.length(); i++)
    {
      int j = (byte)paramString.charAt(i);
      if ((j == 13) || (j == 10))
        throw new IllegalArgumentException("Boundary may not contain CR or LF");
      this.boundary[(i + 2)] = j;
    }
    fillBuffer();
  }

  private void calculateBoundaryLen()
    throws IOException
  {
    this.boundaryLen = this.boundary.length;
    int i = this.limit - this.buffer.pos();
    if ((i > 0) && (this.buffer.charAt(this.limit - 1) == 10))
    {
      this.boundaryLen = (1 + this.boundaryLen);
      this.limit -= 1;
    }
    if ((i > 1) && (this.buffer.charAt(this.limit - 1) == 13))
    {
      this.boundaryLen = (1 + this.boundaryLen);
      this.limit -= 1;
    }
  }

  private boolean endOfStream()
  {
    return (this.eof) || (this.atBoundary);
  }

  private int fillBuffer()
    throws IOException
  {
    if (this.eof)
      return -1;
    int i;
    if (!hasData())
    {
      i = this.buffer.fillBuffer();
      if (i != -1)
        break label108;
    }
    int j;
    label108: for (boolean bool = true; ; bool = false)
    {
      this.eof = bool;
      int k;
      for (j = this.buffer.indexOf(this.boundary); (j > 0) && (this.buffer.charAt(j - 1) != 10); j = this.buffer.indexOf(this.boundary, k, this.buffer.limit() - k))
        k = j + this.boundary.length;
      i = 0;
      break;
    }
    if (j != -1)
    {
      this.limit = j;
      this.atBoundary = true;
      calculateBoundaryLen();
    }
    while (true)
    {
      return i;
      if (this.eof)
        this.limit = this.buffer.limit();
      else
        this.limit = (this.buffer.limit() - (1 + this.boundary.length));
    }
  }

  private boolean hasData()
  {
    return (this.limit > this.buffer.pos()) && (this.limit <= this.buffer.limit());
  }

  private void skipBoundary()
    throws IOException
  {
    int i;
    if (!this.completed)
    {
      this.completed = true;
      this.buffer.skip(this.boundaryLen);
      i = 1;
    }
    while (true)
      if (this.buffer.length() > 1)
      {
        j = this.buffer.charAt(this.buffer.pos());
        k = this.buffer.charAt(1 + this.buffer.pos());
        if ((i != 0) && (j == 45) && (k == 45))
        {
          this.lastPart = true;
          this.buffer.skip(2);
          i = 0;
        }
        else if ((j == 13) && (k == 10))
        {
          this.buffer.skip(2);
        }
      }
      else
      {
        while (this.eof)
        {
          int j;
          int k;
          return;
          if (j == 10)
          {
            this.buffer.skip(1);
            return;
          }
          this.buffer.skip(1);
          break;
        }
        fillBuffer();
      }
  }

  public void close()
    throws IOException
  {
  }

  public boolean eof()
  {
    return (this.eof) && (!this.buffer.hasBufferedData());
  }

  public boolean isLastPart()
  {
    return this.lastPart;
  }

  public boolean markSupported()
  {
    return false;
  }

  public int read()
    throws IOException
  {
    if (this.completed)
      return -1;
    if ((endOfStream()) && (!hasData()))
    {
      skipBoundary();
      return -1;
    }
    do
    {
      fillBuffer();
      if (hasData())
        return this.buffer.read();
    }
    while (!endOfStream());
    skipBoundary();
    return -1;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.completed)
      return -1;
    if ((endOfStream()) && (!hasData()))
    {
      skipBoundary();
      return -1;
    }
    fillBuffer();
    if (!hasData())
      return read(paramArrayOfByte, paramInt1, paramInt2);
    int i = Math.min(paramInt2, this.limit - this.buffer.pos());
    return this.buffer.read(paramArrayOfByte, paramInt1, i);
  }

  public int readLine(ByteArrayBuffer paramByteArrayBuffer)
    throws IOException
  {
    if (paramByteArrayBuffer == null)
      throw new IllegalArgumentException("Destination buffer may not be null");
    if (this.completed)
      return -1;
    if ((endOfStream()) && (!hasData()))
    {
      skipBoundary();
      return -1;
    }
    int i = 0;
    int j = 0;
    int k = 0;
    label204: 
    while (true)
    {
      if (j == 0)
      {
        if (!hasData())
        {
          k = fillBuffer();
          if ((!hasData()) && (endOfStream()))
          {
            skipBoundary();
            k = -1;
          }
        }
      }
      else
      {
        if ((i != 0) || (k != -1))
          break;
        return -1;
      }
      int m = this.limit - this.buffer.pos();
      int n = this.buffer.indexOf((byte)10, this.buffer.pos(), m);
      if (n != -1)
        j = 1;
      for (int i1 = n + 1 - this.buffer.pos(); ; i1 = m)
      {
        if (i1 <= 0)
          break label204;
        paramByteArrayBuffer.append(this.buffer.buf(), this.buffer.pos(), i1);
        this.buffer.skip(i1);
        i += i1;
        break;
      }
    }
    return i;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder("MimeBoundaryInputStream, boundary ");
    byte[] arrayOfByte = this.boundary;
    int i = arrayOfByte.length;
    for (int j = 0; j < i; j++)
      localStringBuilder.append((char)arrayOfByte[j]);
    return localStringBuilder.toString();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.MimeBoundaryInputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */