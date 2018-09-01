package org.apache.james.mime4j.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TempFileStorageProvider extends AbstractStorageProvider
{
  private static final String DEFAULT_PREFIX = "m4j";
  private final File directory;
  private final String prefix;
  private final String suffix;

  public TempFileStorageProvider()
  {
    this("m4j", null, null);
  }

  public TempFileStorageProvider(File paramFile)
  {
    this("m4j", null, paramFile);
  }

  public TempFileStorageProvider(String paramString1, String paramString2, File paramFile)
  {
    if ((paramString1 == null) || (paramString1.length() < 3))
      throw new IllegalArgumentException("invalid prefix");
    if ((paramFile != null) && (!paramFile.isDirectory()) && (!paramFile.mkdirs()))
      throw new IllegalArgumentException("invalid directory");
    this.prefix = paramString1;
    this.suffix = paramString2;
    this.directory = paramFile;
  }

  public StorageOutputStream createStorageOutputStream()
    throws IOException
  {
    File localFile = File.createTempFile(this.prefix, this.suffix, this.directory);
    localFile.deleteOnExit();
    return new TempFileStorageOutputStream(localFile);
  }

  private static final class TempFileStorage
    implements Storage
  {
    private static final Set<File> filesToDelete = new HashSet();
    private File file;

    public TempFileStorage(File paramFile)
    {
      this.file = paramFile;
    }

    public void delete()
    {
      synchronized (filesToDelete)
      {
        if (this.file != null)
        {
          filesToDelete.add(this.file);
          this.file = null;
        }
        Iterator localIterator = filesToDelete.iterator();
        while (localIterator.hasNext())
          if (((File)localIterator.next()).delete())
            localIterator.remove();
      }
    }

    public InputStream getInputStream()
      throws IOException
    {
      if (this.file == null)
        throw new IllegalStateException("storage has been deleted");
      return new BufferedInputStream(new FileInputStream(this.file));
    }
  }

  private static final class TempFileStorageOutputStream extends StorageOutputStream
  {
    private File file;
    private OutputStream out;

    public TempFileStorageOutputStream(File paramFile)
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
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.TempFileStorageProvider
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */