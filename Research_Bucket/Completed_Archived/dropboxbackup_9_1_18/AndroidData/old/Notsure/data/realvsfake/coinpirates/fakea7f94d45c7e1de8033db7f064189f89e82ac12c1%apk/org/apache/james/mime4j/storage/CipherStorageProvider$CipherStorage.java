package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

final class CipherStorageProvider$CipherStorage
  implements Storage
{
  private final String algorithm;
  private Storage encrypted;
  private final SecretKeySpec skeySpec;

  public CipherStorageProvider$CipherStorage(Storage paramStorage, String paramString, SecretKeySpec paramSecretKeySpec)
  {
    this.encrypted = paramStorage;
    this.algorithm = paramString;
    this.skeySpec = paramSecretKeySpec;
  }

  public void delete()
  {
    if (this.encrypted != null)
    {
      this.encrypted.delete();
      this.encrypted = null;
    }
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (this.encrypted == null)
      throw new IllegalStateException("storage has been deleted");
    try
    {
      Cipher localCipher = Cipher.getInstance(this.algorithm);
      localCipher.init(2, this.skeySpec);
      CipherInputStream localCipherInputStream = new CipherInputStream(this.encrypted.getInputStream(), localCipher);
      return localCipherInputStream;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw ((IOException)new IOException().initCause(localGeneralSecurityException));
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.CipherStorageProvider.CipherStorage
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */