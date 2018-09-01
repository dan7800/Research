package org.apache.james.mime4j.descriptor;

public abstract interface BodyDescriptor extends ContentDescriptor
{
  public abstract String getBoundary();
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.descriptor.BodyDescriptor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */