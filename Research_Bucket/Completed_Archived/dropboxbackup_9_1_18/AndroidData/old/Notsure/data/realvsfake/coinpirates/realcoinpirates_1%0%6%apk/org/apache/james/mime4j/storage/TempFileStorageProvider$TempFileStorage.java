package org.apache.james.mime4j.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

final class TempFileStorageProvider$TempFileStorage
  implements Storage
{
  private static final Set<File> filesToDelete = new HashSet();
  private File file;

  public TempFileStorageProvider$TempFileStorage(File paramFile)
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

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.TempFileStorageProvider.TempFileStorage
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */