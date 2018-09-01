package org.apache.james.mime4j.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import org.apache.james.mime4j.util.ByteArrayBuffer;

public class ThresholdStorageProvider extends AbstractStorageProvider
{
  private final StorageProvider backend;
  private final int thresholdSize;

  public ThresholdStorageProvider(StorageProvider paramStorageProvider)
  {
    this(paramStorageProvider, 2048);
  }

  public ThresholdStorageProvider(StorageProvider paramStorageProvider, int paramInt)
  {
    if (paramStorageProvider == null)
      throw new IllegalArgumentException();
    if (paramInt < 1)
      throw new IllegalArgumentException();
    this.backend = paramStorageProvider;
    this.thresholdSize = paramInt;
  }

  public StorageOutputStream createStorageOutputStream()
  {
    return new ThresholdStorageOutputStream();
  }

  private static final class ThresholdStorage
    implements Storage
  {
    private byte[] head;
    private final int headLen;
    private Storage tail;

    public ThresholdStorage(byte[] paramArrayOfByte, int paramInt, Storage paramStorage)
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

  private final class ThresholdStorageOutputStream extends StorageOutputStream
  {
    private final ByteArrayBuffer head = new ByteArrayBuffer(Math.min(ThresholdStorageProvider.this.thresholdSize, 1024));
    private StorageOutputStream tail;

    public ThresholdStorageOutputStream()
    {
    }

    public void close()
      throws IOException
    {
      super.close();
      if (this.tail != null)
        this.tail.close();
    }

    protected Storage toStorage0()
      throws IOException
    {
      if (this.tail == null)
        return new MemoryStorageProvider.MemoryStorage(this.head.buffer(), this.head.length());
      return new ThresholdStorageProvider.ThresholdStorage(this.head.buffer(), this.head.length(), this.tail.toStorage());
    }

    protected void write0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
      throws IOException
    {
      int i = ThresholdStorageProvider.this.thresholdSize - this.head.length();
      if (i > 0)
      {
        int j = Math.min(i, paramInt2);
        this.head.append(paramArrayOfByte, paramInt1, j);
        paramInt1 += j;
        paramInt2 -= j;
      }
      if (paramInt2 > 0)
      {
        if (this.tail == null)
          this.tail = ThresholdStorageProvider.this.backend.createStorageOutputStream();
        this.tail.write(paramArrayOfByte, paramInt1, paramInt2);
      }
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.ThresholdStorageProvider
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */