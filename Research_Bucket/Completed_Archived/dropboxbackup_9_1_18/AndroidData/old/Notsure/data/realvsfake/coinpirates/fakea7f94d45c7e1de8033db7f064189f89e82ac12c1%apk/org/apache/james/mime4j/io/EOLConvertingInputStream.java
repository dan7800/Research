package org.apache.james.mime4j.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class EOLConvertingInputStream extends InputStream
{
  public static final int CONVERT_BOTH = 3;
  public static final int CONVERT_CR = 1;
  public static final int CONVERT_LF = 2;
  private int flags = 3;
  private PushbackInputStream in = null;
  private int previous = 0;

  public EOLConvertingInputStream(InputStream paramInputStream)
  {
    this(paramInputStream, 3);
  }

  public EOLConvertingInputStream(InputStream paramInputStream, int paramInt)
  {
    this.in = new PushbackInputStream(paramInputStream, 2);
    this.flags = paramInt;
  }

  public void close()
    throws IOException
  {
    this.in.close();
  }

  public int read()
    throws IOException
  {
    int i = this.in.read();
    if (i == -1)
      return -1;
    if (((0x1 & this.flags) != 0) && (i == 13))
    {
      int j = this.in.read();
      if (j != -1)
        this.in.unread(j);
      if (j != 10)
        this.in.unread(10);
    }
    while (true)
    {
      this.previous = i;
      return i;
      if (((0x2 & this.flags) != 0) && (i == 10) && (this.previous != 13))
      {
        i = 13;
        this.in.unread(10);
      }
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.EOLConvertingInputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */