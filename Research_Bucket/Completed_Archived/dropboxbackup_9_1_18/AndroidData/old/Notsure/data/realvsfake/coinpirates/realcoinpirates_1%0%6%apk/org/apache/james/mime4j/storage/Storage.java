package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.io.InputStream;

public abstract interface Storage
{
  public abstract void delete();

  public abstract InputStream getInputStream()
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.Storage
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */