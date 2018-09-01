package org.apache.james.mime4j.io;

import org.apache.james.mime4j.MimeException;

public class MaxHeaderLimitException extends MimeException
{
  private static final long serialVersionUID = 2154269045186186769L;

  public MaxHeaderLimitException(String paramString)
  {
    super(paramString);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.io.MaxHeaderLimitException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */