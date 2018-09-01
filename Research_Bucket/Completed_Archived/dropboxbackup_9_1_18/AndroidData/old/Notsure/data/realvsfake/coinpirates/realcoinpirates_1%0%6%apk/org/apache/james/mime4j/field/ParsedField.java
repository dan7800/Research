package org.apache.james.mime4j.field;

import org.apache.james.mime4j.parser.Field;

public abstract interface ParsedField extends Field
{
  public abstract ParseException getParseException();

  public abstract boolean isValidField();
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ParsedField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */