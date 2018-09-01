package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.codec.CodecUtil;

public abstract class AbstractStorageProvider
  implements StorageProvider
{
  protected AbstractStorageProvider()
  {
  }

  public final Storage store(InputStream paramInputStream)
    throws IOException
  {
    StorageOutputStream localStorageOutputStream = createStorageOutputStream();
    CodecUtil.copy(paramInputStream, localStorageOutputStream);
    return localStorageOutputStream.toStorage();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.AbstractStorageProvider
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */