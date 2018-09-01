package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.io.OutputStream;

public abstract class StorageOutputStream extends OutputStream
{
  private boolean closed;
  private byte[] singleByte;
  private boolean usedUp;

  protected StorageOutputStream()
  {
  }

  public void close()
    throws IOException
  {
    this.closed = true;
  }

  public final Storage toStorage()
    throws IOException
  {
    if (this.usedUp)
      throw new IllegalStateException("toStorage may be invoked only once");
    if (!this.closed)
      close();
    this.usedUp = true;
    return toStorage0();
  }

  protected abstract Storage toStorage0()
    throws IOException;

  public final void write(int paramInt)
    throws IOException
  {
    if (this.closed)
      throw new IOException("StorageOutputStream has been closed");
    if (this.singleByte == null)
      this.singleByte = new byte[1];
    this.singleByte[0] = (byte)paramInt;
    write0(this.singleByte, 0, 1);
  }

  public final void write(byte[] paramArrayOfByte)
    throws IOException
  {
    if (this.closed)
      throw new IOException("StorageOutputStream has been closed");
    if (paramArrayOfByte == null)
      throw new NullPointerException();
    if (paramArrayOfByte.length == 0)
      return;
    write0(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public final void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.closed)
      throw new IOException("StorageOutputStream has been closed");
    if (paramArrayOfByte == null)
      throw new NullPointerException();
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length))
      throw new IndexOutOfBoundsException();
    if (paramInt2 == 0)
      return;
    write0(paramArrayOfByte, paramInt1, paramInt2);
  }

  protected abstract void write0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.StorageOutputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */