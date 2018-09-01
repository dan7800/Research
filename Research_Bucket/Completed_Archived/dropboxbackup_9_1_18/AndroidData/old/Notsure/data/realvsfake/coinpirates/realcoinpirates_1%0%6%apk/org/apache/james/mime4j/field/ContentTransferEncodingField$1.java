package org.apache.james.mime4j.field;

import org.apache.james.mime4j.util.ByteSequence;

final class ContentTransferEncodingField$1
  implements FieldParser
{
  ContentTransferEncodingField$1()
  {
  }

  public ParsedField parse(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    return new ContentTransferEncodingField(paramString1, paramString2, paramByteSequence);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ContentTransferEncodingField.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */