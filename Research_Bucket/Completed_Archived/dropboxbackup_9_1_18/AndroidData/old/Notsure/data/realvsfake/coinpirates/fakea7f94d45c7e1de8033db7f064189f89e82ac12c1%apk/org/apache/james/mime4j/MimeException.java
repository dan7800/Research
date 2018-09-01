package org.apache.james.mime4j;

public class MimeException extends Exception
{
  private static final long serialVersionUID = 8352821278714188542L;

  public MimeException(String paramString)
  {
    super(paramString);
  }

  public MimeException(String paramString, Throwable paramThrowable)
  {
    super(paramString);
    initCause(paramThrowable);
  }

  public MimeException(Throwable paramThrowable)
  {
    super(paramThrowable);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.MimeException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */