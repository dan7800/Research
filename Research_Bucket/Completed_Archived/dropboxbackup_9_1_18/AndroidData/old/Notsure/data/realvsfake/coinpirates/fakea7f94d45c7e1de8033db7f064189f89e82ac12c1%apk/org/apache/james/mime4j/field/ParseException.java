package org.apache.james.mime4j.field;

import org.apache.james.mime4j.MimeException;

public class ParseException extends MimeException
{
  private static final long serialVersionUID = 1L;

  protected ParseException(String paramString)
  {
    super(paramString);
  }

  protected ParseException(String paramString, Throwable paramThrowable)
  {
    super(paramString, paramThrowable);
  }

  protected ParseException(Throwable paramThrowable)
  {
    super(paramThrowable);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ParseException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */