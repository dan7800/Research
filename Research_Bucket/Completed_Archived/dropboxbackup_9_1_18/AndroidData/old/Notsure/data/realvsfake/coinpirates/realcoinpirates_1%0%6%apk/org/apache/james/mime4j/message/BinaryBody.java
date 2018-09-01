package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;

public abstract class BinaryBody extends SingleBody
{
  protected BinaryBody()
  {
  }

  public abstract InputStream getInputStream()
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.BinaryBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */