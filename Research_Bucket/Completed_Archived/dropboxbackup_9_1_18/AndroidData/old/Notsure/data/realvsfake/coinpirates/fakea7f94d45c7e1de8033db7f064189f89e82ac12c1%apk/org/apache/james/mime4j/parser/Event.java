package org.apache.james.mime4j.parser;

public final class Event
{
  public static final Event HEADERS_PREMATURE_END = new Event("Unexpected end of headers detected. Higher level boundary detected or EOF reached.");
  public static final Event INALID_HEADER = new Event("Invalid header encountered");
  public static final Event MIME_BODY_PREMATURE_END = new Event("Body part ended prematurely. Boundary detected in header or EOF reached.");
  private final String code;

  public Event(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("Code may not be null");
    this.code = paramString;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject == null)
      return false;
    if (this == paramObject)
      return true;
    if ((paramObject instanceof Event))
    {
      Event localEvent = (Event)paramObject;
      return this.code.equals(localEvent.code);
    }
    return false;
  }

  public int hashCode()
  {
    return this.code.hashCode();
  }

  public String toString()
  {
    return this.code;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.Event
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */