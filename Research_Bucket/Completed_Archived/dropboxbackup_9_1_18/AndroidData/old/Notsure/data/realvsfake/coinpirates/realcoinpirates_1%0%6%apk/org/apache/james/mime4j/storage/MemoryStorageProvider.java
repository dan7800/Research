package org.apache.james.mime4j.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public class MemoryStorageProvider extends AbstractStorageProvider
{
  public MemoryStorageProvider()
  {
  }

  public StorageOutputStream createStorageOutputStream()
  {
    return new MemoryStorageOutputStream(null);
  }

  static final class MemoryStorage
    implements Storage
  {
    private final int count;
    private byte[] data;

    public MemoryStorage(byte[] paramArrayOfByte, int paramInt)
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

  private static final class MemoryStorageOutputStream extends StorageOutputStream
  {
    ByteArrayBuffer bab = new ByteArrayBuffer(1024);

    private MemoryStorageOutputStream()
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
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.MemoryStorageProvider
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */