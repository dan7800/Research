package org.apache.james.mime4j.storage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

final class CipherStorageProvider$CipherStorageOutputStream extends StorageOutputStream
{
  private final String algorithm;
  private final CipherOutputStream cipherOut;
  private final SecretKeySpec skeySpec;
  private final StorageOutputStream storageOut;

  public CipherStorageProvider$CipherStorageOutputStream(StorageOutputStream paramStorageOutputStream, String paramString, SecretKeySpec paramSecretKeySpec)
    throws IOException
  {
    try
    {
      this.storageOut = paramStorageOutputStream;
      this.algorithm = paramString;
      this.skeySpec = paramSecretKeySpec;
      Cipher localCipher = Cipher.getInstance(paramString);
      localCipher.init(1, paramSecretKeySpec);
      this.cipherOut = new CipherOutputStream(paramStorageOutputStream, localCipher);
      return;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw ((IOException)new IOException().initCause(localGeneralSecurityException));
    }
  }

  public void close()
    throws IOException
  {
    super.close();
    this.cipherOut.close();
  }

  protected Storage toStorage0()
    throws IOException
  {
    return new CipherStorageProvider.CipherStorage(this.storageOut.toStorage(), this.algorithm, this.skeySpec);
  }

  protected void write0(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.cipherOut.write(paramArrayOfByte, paramInt1, paramInt2);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.storage.CipherStorageProvider.CipherStorageOutputStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */