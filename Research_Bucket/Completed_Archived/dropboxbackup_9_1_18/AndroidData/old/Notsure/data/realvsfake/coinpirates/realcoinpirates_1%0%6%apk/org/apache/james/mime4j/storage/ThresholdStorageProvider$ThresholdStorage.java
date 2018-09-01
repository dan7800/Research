package org.apache.james.mime4j.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

final class ThresholdStorageProvider$ThresholdStorage
  implements Storage
{
  private byte[] head;
  private final int headLen;
  private Storage tail;

  public ThresholdStorageProvider$ThresholdStorage(byte[] paramArrayOfByte, int paramInt, Storage paramStorage)
  {
    this.head = paramArrayOfByte;
    this.headLen = paramInt;
    this.tail = paramStorage;
  }

  public void delete()
  {
    if (this.head != null)
    {
      this.head = null;
      this.tail.delete();
      this.tail = null;
    }
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (this.head == null)
      throw new IllegalStateException("storage has been deleted");
    return new SequenceInputStream(new ByteArrayInputStream(this.head, 0, this.headLen), this.tail.getInputStream());
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.ThresholdStorageProvider.ThresholdStorage
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */