package com.admob.android.ads;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public final class t
  implements h
{
  private static t a = null;
  private static Context b = null;
  private static Thread c = null;
  private static String d = null;
  private Properties e;
  private Context f;

  private t(Context paramContext)
  {
    this.f = paramContext;
    this.e = null;
    d = a();
    if (a != null)
      a.e = null;
    if ((!b()) && (c == null))
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append("http://mm.admob.com/static/android/i18n/20101109");
      localStringBuilder.append("/");
      localStringBuilder.append(d);
      localStringBuilder.append(".properties");
      Thread localThread = new Thread(g.a(localStringBuilder.toString(), null, AdManager.getUserId(this.f), this, 1));
      c = localThread;
      localThread.start();
    }
  }

  private static File a(Context paramContext, String paramString)
  {
    File localFile1 = new File(paramContext.getCacheDir(), "admob_cache");
    if (!localFile1.exists())
      localFile1.mkdir();
    File localFile2 = new File(localFile1, "20101109");
    if (!localFile2.exists())
      localFile2.mkdir();
    return new File(localFile2, paramString + ".properties");
  }

  public static String a()
  {
    if (d == null)
    {
      String str = Locale.getDefault().getLanguage();
      d = str;
      if (str == null)
        d = "en";
    }
    return d;
  }

  public static String a(String paramString)
  {
    a(b);
    t localt = a;
    localt.b();
    if (localt.e != null)
    {
      String str = localt.e.getProperty(paramString);
      if ((str == null) || (str.equals("")))
        str = paramString;
      return str;
    }
    return paramString;
  }

  public static void a(Context paramContext)
  {
    if ((b == null) && (paramContext != null))
      b = paramContext.getApplicationContext();
    if (a == null)
      a = new t(b);
  }

  private boolean b()
  {
    if (this.e == null);
    try
    {
      Properties localProperties = new Properties();
      File localFile = a(this.f, d);
      if (localFile.exists())
      {
        localProperties.load(new FileInputStream(localFile));
        this.e = localProperties;
      }
      if (this.e != null)
        return true;
    }
    catch (IOException localIOException)
    {
      while (true)
        this.e = null;
    }
    return false;
  }

  public final void a(e parame)
  {
    try
    {
      byte[] arrayOfByte = parame.a();
      if (arrayOfByte != null)
      {
        FileOutputStream localFileOutputStream = new FileOutputStream(a(this.f, d));
        localFileOutputStream.write(arrayOfByte);
        localFileOutputStream.close();
      }
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 3));
      Log.d("AdMobSDK", "Could not store localized strings to cache file.");
    }
  }

  public final void a(e parame, Exception paramException)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Could not get localized strings from the AdMob servers.");
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.t
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */