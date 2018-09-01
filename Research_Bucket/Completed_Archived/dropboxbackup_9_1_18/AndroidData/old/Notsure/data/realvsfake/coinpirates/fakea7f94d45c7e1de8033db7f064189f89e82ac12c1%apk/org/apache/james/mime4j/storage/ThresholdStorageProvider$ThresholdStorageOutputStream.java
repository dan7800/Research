package org.apache.james.mime4j.storage;

import java.io.IOException;
import org.apache.james.mime4j.util.ByteArrayBuffer;

final class ThresholdStorageProvider$ThresholdStorageOutputStream extends StorageOutputStream
{
  private final ByteArrayBuffer head;
  private StorageOutputStream tail;

  public ThresholdStorageProvider$ThresholdStorageOutputStream(ThresholdStorageProvider paramThresholdStorageProvider)
  {
    this.head = new ByteArrayBuffer(Math.min(ThresholdStorageProvider.access$000(paramThresholdStorageProvider), 1024));
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
    int i = ThresholdStorageProvider.access$000(this.this$0) - this.head.length();
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
        this.tail = ThresholdStorageProvider.access$100(this.this$0).createStorageOutputStream();
      this.tail.write(paramArrayOfByte, paramInt1, paramInt2);
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.ThresholdStorageProvider.ThresholdStorageOutputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */