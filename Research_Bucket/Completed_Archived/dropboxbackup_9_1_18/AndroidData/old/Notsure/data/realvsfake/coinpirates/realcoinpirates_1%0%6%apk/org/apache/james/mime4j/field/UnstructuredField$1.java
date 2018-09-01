package org.apache.james.mime4j.field;

import org.apache.james.mime4j.util.ByteSequence;

final class UnstructuredField$1
  implements FieldParser
{
  UnstructuredField$1()
  {
  }

  public ParsedField parse(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    return new UnstructuredField(paramString1, paramString2, paramByteSequence);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.UnstructuredField.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */