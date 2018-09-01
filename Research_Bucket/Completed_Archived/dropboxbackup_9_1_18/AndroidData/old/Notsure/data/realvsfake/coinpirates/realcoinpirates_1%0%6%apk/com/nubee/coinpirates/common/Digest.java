package com.nubee.coinpirates.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest
{
  public static final String SHA256 = "SHA-256";
  private MessageDigest messageDigest;

  public Digest(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("Algorithm must not be null");
    try
    {
      this.messageDigest = MessageDigest.getInstance(paramString);
      return;
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
    }
  }

  public static String hex(String paramString1, String paramString2)
  {
    return new Digest(paramString1).hex(paramString2);
  }

  public String hex(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("Message must not be null");
    if (this.messageDigest == null)
      return "";
    StringBuilder localStringBuilder = new StringBuilder();
    this.messageDigest.reset();
    this.messageDigest.update(paramString.getBytes());
    byte[] arrayOfByte = this.messageDigest.digest();
    for (int i = 0; ; i++)
    {
      if (i >= arrayOfByte.length)
        return localStringBuilder.toString();
      String str = Integer.toHexString(0xFF & arrayOfByte[i]);
      if (str.length() == 1)
        localStringBuilder.append("0");
      localStringBuilder.append(str);
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.Digest
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */