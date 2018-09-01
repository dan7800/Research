package org.apache.james.mime4j.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

final class TempFileStorageProvider$TempFileStorageOutputStream extends StorageOutputStream
{
  private File file;
  private OutputStream out;

  public TempFileStorageProvider$TempFileStorageOutputStream(File paramFile)
    throws IOException
  {
    this.file = paramFile;
    this.out = new FileOutputStream(paramFile);
  }

  public void close()
    throws IOException
  {
    super.close();
    this.out.close();
  }

  protected Storage toStorage0()
    throws IOException
  {
    return new TempFileStorageProvider.TempFileStorage(this.file);
  }

  protected void write0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.out.write(paramArrayOfByte, paramInt1, paramInt2);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.TempFileStorageProvider.TempFileStorageOutputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */