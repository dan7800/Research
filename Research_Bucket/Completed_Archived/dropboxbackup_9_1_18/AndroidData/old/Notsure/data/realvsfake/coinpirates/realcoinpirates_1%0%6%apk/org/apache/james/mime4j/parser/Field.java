package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.util.ByteSequence;

public abstract interface Field
{
  public abstract String getBody();

  public abstract String getName();

  public abstract ByteSequence getRaw();
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.Field
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */