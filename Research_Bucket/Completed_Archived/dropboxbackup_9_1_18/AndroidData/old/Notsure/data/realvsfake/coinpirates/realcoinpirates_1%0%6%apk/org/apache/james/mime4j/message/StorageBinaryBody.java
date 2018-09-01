package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.storage.MultiReferenceStorage;

class StorageBinaryBody extends BinaryBody
{
  private MultiReferenceStorage storage;

  public StorageBinaryBody(MultiReferenceStorage paramMultiReferenceStorage)
  {
    this.storage = paramMultiReferenceStorage;
  }

  public StorageBinaryBody copy()
  {
    this.storage.addReference();
    return new StorageBinaryBody(this.storage);
  }

  public void dispose()
  {
    if (this.storage != null)
    {
      this.storage.delete();
      this.storage = null;
    }
  }

  public InputStream getInputStream()
    throws IOException
  {
    return this.storage.getInputStream();
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    if (paramOutputStream == null)
      throw new IllegalArgumentException();
    InputStream localInputStream = this.storage.getInputStream();
    CodecUtil.copy(localInputStream, paramOutputStream);
    localInputStream.close();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.StorageBinaryBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */