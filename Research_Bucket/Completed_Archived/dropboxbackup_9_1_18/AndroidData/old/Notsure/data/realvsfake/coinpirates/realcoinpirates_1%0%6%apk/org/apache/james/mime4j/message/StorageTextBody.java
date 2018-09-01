package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.storage.MultiReferenceStorage;
import org.apache.james.mime4j.util.CharsetUtil;

class StorageTextBody extends TextBody
{
  private Charset charset;
  private MultiReferenceStorage storage;

  public StorageTextBody(MultiReferenceStorage paramMultiReferenceStorage, Charset paramCharset)
  {
    this.storage = paramMultiReferenceStorage;
    this.charset = paramCharset;
  }

  public StorageTextBody copy()
  {
    this.storage.addReference();
    return new StorageTextBody(this.storage, this.charset);
  }

  public void dispose()
  {
    if (this.storage != null)
    {
      this.storage.delete();
      this.storage = null;
    }
  }

  public String getMimeCharset()
  {
    return CharsetUtil.toMimeCharset(this.charset.name());
  }

  public Reader getReader()
    throws IOException
  {
    return new InputStreamReader(this.storage.getInputStream(), this.charset);
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
 * Qualified Name:     org.apache.james.mime4j.message.StorageTextBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */