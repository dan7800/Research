package org.apache.james.mime4j.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PositionInputStream extends FilterInputStream
{
  private long markedPosition = 0L;
  protected long position = 0L;

  public PositionInputStream(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public int available()
    throws IOException
  {
    return this.in.available();
  }

  public void close()
    throws IOException
  {
    this.in.close();
  }

  public long getPosition()
  {
    return this.position;
  }

  public void mark(int paramInt)
  {
    this.in.mark(paramInt);
    this.markedPosition = this.position;
  }

  public boolean markSupported()
  {
    return this.in.markSupported();
  }

  public int read()
    throws IOException
  {
    int i = this.in.read();
    if (i != -1)
      this.position = (1L + this.position);
    return i;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    if (i > 0)
      this.position += i;
    return i;
  }

  public void reset()
    throws IOException
  {
    this.in.reset();
    this.position = this.markedPosition;
  }

  public long skip(long paramLong)
    throws IOException
  {
    long l = this.in.skip(paramLong);
    if (l > 0L)
      this.position = (l + this.position);
    return l;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.PositionInputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */