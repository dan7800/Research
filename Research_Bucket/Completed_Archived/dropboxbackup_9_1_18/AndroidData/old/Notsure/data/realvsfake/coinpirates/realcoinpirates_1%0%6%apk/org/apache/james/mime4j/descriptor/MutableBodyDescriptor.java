package org.apache.james.mime4j.descriptor;

import org.apache.james.mime4j.parser.Field;

public abstract interface MutableBodyDescriptor extends BodyDescriptor
{
  public abstract void addField(Field paramField);
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.descriptor.MutableBodyDescriptor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */