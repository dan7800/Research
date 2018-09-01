package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.Reader;

public abstract class TextBody extends SingleBody
{
  protected TextBody()
  {
  }

  public abstract String getMimeCharset();

  public abstract Reader getReader()
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.TextBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */