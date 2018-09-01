package org.apache.james.mime4j.field;

import java.util.HashMap;
import java.util.Map;
import org.apache.james.mime4j.util.ByteSequence;

public class DelegatingFieldParser
  implements FieldParser
{
  private FieldParser defaultParser = UnstructuredField.PARSER;
  private Map<String, FieldParser> parsers = new HashMap();

  public DelegatingFieldParser()
  {
  }

  public FieldParser getParser(String paramString)
  {
    FieldParser localFieldParser = (FieldParser)this.parsers.get(paramString.toLowerCase());
    if (localFieldParser == null)
      return this.defaultParser;
    return localFieldParser;
  }

  public ParsedField parse(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    return getParser(paramString1).parse(paramString1, paramString2, paramByteSequence);
  }

  public void setFieldParser(String paramString, FieldParser paramFieldParser)
  {
    this.parsers.put(paramString.toLowerCase(), paramFieldParser);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.DelegatingFieldParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */