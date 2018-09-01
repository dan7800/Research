package org.apache.james.mime4j.storage;

import java.io.IOException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

final class MemoryStorageProvider$MemoryStorageOutputStream extends StorageOutputStream
{
  ByteArrayBuffer bab = new ByteArrayBuffer(1024);

  private MemoryStorageProvider$MemoryStorageOutputStream()
  {
  }

  protected Storage toStorage0()
    throws IOException
  {
    return new MemoryStorageProvider.MemoryStorage(this.bab.buffer(), this.bab.length());
  }

  protected void write0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.bab.append(paramArrayOfByte, paramInt1, paramInt2);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.MemoryStorageProvider.MemoryStorageOutputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */