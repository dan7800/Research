package org.apache.james.mime4j;

import java.io.IOException;

public class MimeIOException extends IOException
{
  private static final long serialVersionUID = 5393613459533735409L;

  public MimeIOException(String paramString)
  {
    this(new MimeException(paramString));
  }

  public MimeIOException(MimeException paramMimeException)
  {
    super(paramMimeException.getMessage());
    initCause(paramMimeException);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.MimeIOException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */