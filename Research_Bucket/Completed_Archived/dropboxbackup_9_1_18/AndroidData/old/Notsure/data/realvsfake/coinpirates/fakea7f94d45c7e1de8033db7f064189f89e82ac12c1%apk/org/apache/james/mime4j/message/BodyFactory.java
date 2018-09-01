package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.MultiReferenceStorage;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.CharsetUtil;

public class BodyFactory
{
  private static final Charset FALLBACK_CHARSET = CharsetUtil.DEFAULT_CHARSET;
  private static Log log = LogFactory.getLog(BodyFactory.class);
  private StorageProvider storageProvider;

  public BodyFactory()
  {
    this.storageProvider = DefaultStorageProvider.getInstance();
  }

  public BodyFactory(StorageProvider paramStorageProvider)
  {
    if (paramStorageProvider == null)
      paramStorageProvider = DefaultStorageProvider.getInstance();
    this.storageProvider = paramStorageProvider;
  }

  private static Charset toJavaCharset(String paramString, boolean paramBoolean)
  {
    String str = CharsetUtil.toJavaCharset(paramString);
    if (str == null)
    {
      if (log.isWarnEnabled())
        log.warn("MIME charset '" + paramString + "' has no " + "corresponding Java charset. Using " + FALLBACK_CHARSET + " instead.");
      return FALLBACK_CHARSET;
    }
    if ((paramBoolean) && (!CharsetUtil.isEncodingSupported(str)))
    {
      if (log.isWarnEnabled())
        log.warn("MIME charset '" + paramString + "' does not support encoding. Using " + FALLBACK_CHARSET + " instead.");
      return FALLBACK_CHARSET;
    }
    if ((!paramBoolean) && (!CharsetUtil.isDecodingSupported(str)))
    {
      if (log.isWarnEnabled())
        log.warn("MIME charset '" + paramString + "' does not support decoding. Using " + FALLBACK_CHARSET + " instead.");
      return FALLBACK_CHARSET;
    }
    return Charset.forName(str);
  }

  public BinaryBody binaryBody(InputStream paramInputStream)
    throws IOException
  {
    if (paramInputStream == null)
      throw new IllegalArgumentException();
    return new StorageBinaryBody(new MultiReferenceStorage(this.storageProvider.store(paramInputStream)));
  }

  public BinaryBody binaryBody(Storage paramStorage)
    throws IOException
  {
    if (paramStorage == null)
      throw new IllegalArgumentException();
    return new StorageBinaryBody(new MultiReferenceStorage(paramStorage));
  }

  public StorageProvider getStorageProvider()
  {
    return this.storageProvider;
  }

  public TextBody textBody(InputStream paramInputStream)
    throws IOException
  {
    if (paramInputStream == null)
      throw new IllegalArgumentException();
    return new StorageTextBody(new MultiReferenceStorage(this.storageProvider.store(paramInputStream)), CharsetUtil.DEFAULT_CHARSET);
  }

  public TextBody textBody(InputStream paramInputStream, String paramString)
    throws IOException
  {
    if (paramInputStream == null)
      throw new IllegalArgumentException();
    if (paramString == null)
      throw new IllegalArgumentException();
    Storage localStorage = this.storageProvider.store(paramInputStream);
    Charset localCharset = toJavaCharset(paramString, false);
    return new StorageTextBody(new MultiReferenceStorage(localStorage), localCharset);
  }

  public TextBody textBody(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    return new StringTextBody(paramString, CharsetUtil.DEFAULT_CHARSET);
  }

  public TextBody textBody(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      throw new IllegalArgumentException();
    if (paramString2 == null)
      throw new IllegalArgumentException();
    return new StringTextBody(paramString1, toJavaCharset(paramString2, true));
  }

  public TextBody textBody(Storage paramStorage)
    throws IOException
  {
    if (paramStorage == null)
      throw new IllegalArgumentException();
    return new StorageTextBody(new MultiReferenceStorage(paramStorage), CharsetUtil.DEFAULT_CHARSET);
  }

  public TextBody textBody(Storage paramStorage, String paramString)
    throws IOException
  {
    if (paramStorage == null)
      throw new IllegalArgumentException();
    if (paramString == null)
      throw new IllegalArgumentException();
    Charset localCharset = toJavaCharset(paramString, false);
    return new StorageTextBody(new MultiReferenceStorage(paramStorage), localCharset);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.BodyFactory
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */