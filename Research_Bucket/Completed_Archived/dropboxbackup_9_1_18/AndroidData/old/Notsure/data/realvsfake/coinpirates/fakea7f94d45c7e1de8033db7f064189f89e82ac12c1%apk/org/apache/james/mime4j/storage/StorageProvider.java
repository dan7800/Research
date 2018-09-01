package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.io.InputStream;

public abstract interface StorageProvider
{
  public abstract StorageOutputStream createStorageOutputStream()
    throws IOException;

  public abstract Storage store(InputStream paramInputStream)
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.StorageProvider
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */