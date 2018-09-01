package org.apache.james.mime4j.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

final class MemoryStorageProvider$MemoryStorage
  implements Storage
{
  private final int count;
  private byte[] data;

  public MemoryStorageProvider$MemoryStorage(byte[] paramArrayOfByte, int paramInt)
  {
    this.data = paramArrayOfByte;
    this.count = paramInt;
  }

  public void delete()
  {
    this.data = null;
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (this.data == null)
      throw new IllegalStateException("storage has been deleted");
    return new ByteArrayInputStream(this.data, 0, this.count);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.MemoryStorageProvider.MemoryStorage
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */