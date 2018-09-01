package org.apache.james.mime4j.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public abstract class LineReaderInputStream extends FilterInputStream
{
  protected LineReaderInputStream(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public abstract int readLine(ByteArrayBuffer paramByteArrayBuffer)
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.LineReaderInputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */