package org.apache.james.mime4j.io;

import org.apache.james.mime4j.MimeIOException;

public class MaxLineLimitException extends MimeIOException
{
  private static final long serialVersionUID = 8039001187837730773L;

  public MaxLineLimitException(String paramString)
  {
    super(paramString);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.MaxLineLimitException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */