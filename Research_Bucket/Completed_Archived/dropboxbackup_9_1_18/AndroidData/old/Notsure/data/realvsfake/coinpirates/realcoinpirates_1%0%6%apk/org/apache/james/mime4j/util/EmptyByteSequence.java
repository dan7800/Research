package org.apache.james.mime4j.util;

final class EmptyByteSequence
  implements ByteSequence
{
  private static final byte[] EMPTY_BYTES = new byte[0];

  EmptyByteSequence()
  {
  }

  public byte byteAt(int paramInt)
  {
    throw new IndexOutOfBoundsException();
  }

  public int length()
  {
    return 0;
  }

  public byte[] toByteArray()
  {
    return EMPTY_BYTES;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.EmptyByteSequence
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */