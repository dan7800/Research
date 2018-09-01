package org.apache.http.entity.mime;

import org.apache.james.mime4j.MimeException;

@Deprecated
public class UnexpectedMimeException extends RuntimeException
{
  private static final long serialVersionUID = 1316818299528463579L;

  public UnexpectedMimeException(MimeException paramMimeException)
  {
    super(paramMimeException.getMessage(), paramMimeException);
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.UnexpectedMimeException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */