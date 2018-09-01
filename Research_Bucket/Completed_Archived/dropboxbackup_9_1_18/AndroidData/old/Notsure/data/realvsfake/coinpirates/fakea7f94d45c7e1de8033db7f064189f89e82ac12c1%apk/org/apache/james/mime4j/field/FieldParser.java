package org.apache.james.mime4j.field;

import org.apache.james.mime4j.util.ByteSequence;

public abstract interface FieldParser
{
  public abstract ParsedField parse(String paramString1, String paramString2, ByteSequence paramByteSequence);
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.FieldParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */