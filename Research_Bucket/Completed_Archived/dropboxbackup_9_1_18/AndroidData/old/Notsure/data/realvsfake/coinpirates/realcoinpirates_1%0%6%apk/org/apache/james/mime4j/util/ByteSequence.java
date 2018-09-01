package org.apache.james.mime4j.util;

public abstract interface ByteSequence
{
  public static final ByteSequence EMPTY = new EmptyByteSequence();

  public abstract byte byteAt(int paramInt);

  public abstract int length();

  public abstract byte[] toByteArray();
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.ByteSequence
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */