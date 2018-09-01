package org.apache.james.mime4j.field;

import org.apache.james.mime4j.util.ByteSequence;

public class ContentTransferEncodingField extends AbstractField
{
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new ContentTransferEncodingField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  private String encoding;

  ContentTransferEncodingField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
    this.encoding = paramString2.trim().toLowerCase();
  }

  public static String getEncoding(ContentTransferEncodingField paramContentTransferEncodingField)
  {
    if ((paramContentTransferEncodingField != null) && (paramContentTransferEncodingField.getEncoding().length() != 0))
      return paramContentTransferEncodingField.getEncoding();
    return "7bit";
  }

  public String getEncoding()
  {
    return this.encoding;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ContentTransferEncodingField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */