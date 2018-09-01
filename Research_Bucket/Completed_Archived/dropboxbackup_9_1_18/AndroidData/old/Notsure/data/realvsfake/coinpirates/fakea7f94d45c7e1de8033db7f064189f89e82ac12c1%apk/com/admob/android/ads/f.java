package com.admob.android.ads;

import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class f
  implements e
{
  private static Executor m = null;
  private static String n;
  protected String a;
  protected int b;
  protected Exception c = null;
  protected Map<String, String> d;
  protected int e;
  protected int f;
  protected String g;
  protected h h;
  protected URL i;
  protected byte[] j;
  protected boolean k;
  protected String l;
  private String o;
  private Object p;

  protected f(String paramString1, String paramString2, h paramh, int paramInt, Map<String, String> paramMap, String paramString3)
  {
    this.o = paramString1;
    this.g = paramString2;
    this.h = paramh;
    this.b = paramInt;
    this.d = paramMap;
    this.k = true;
    this.e = 0;
    this.f = 3;
    if (paramString3 != null)
    {
      this.l = paramString3;
      this.a = "application/x-www-form-urlencoded";
      return;
    }
    this.l = null;
    this.a = null;
  }

  public static String h()
  {
    StringBuffer localStringBuffer;
    if (n == null)
    {
      localStringBuffer = new StringBuffer();
      String str1 = Build.VERSION.RELEASE;
      if (str1.length() <= 0)
        break label209;
      localStringBuffer.append(str1);
      localStringBuffer.append("; ");
      Locale localLocale = Locale.getDefault();
      String str2 = localLocale.getLanguage();
      if (str2 == null)
        break label219;
      localStringBuffer.append(str2.toLowerCase());
      String str5 = localLocale.getCountry();
      if (str5 != null)
      {
        localStringBuffer.append("-");
        localStringBuffer.append(str5.toLowerCase());
      }
    }
    while (true)
    {
      String str3 = Build.MODEL;
      if (str3.length() > 0)
      {
        localStringBuffer.append("; ");
        localStringBuffer.append(str3);
      }
      String str4 = Build.ID;
      if (str4.length() > 0)
      {
        localStringBuffer.append(" Build/");
        localStringBuffer.append(str4);
      }
      n = String.format("Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2 (AdMob-ANDROID-%s)", new Object[] { localStringBuffer, "20101109" });
      if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "Phone's user-agent is:  " + n);
      return n;
      label209: localStringBuffer.append("1.0");
      break;
      label219: localStringBuffer.append("en");
    }
  }

  public final void a(int paramInt)
  {
    this.f = paramInt;
  }

  public void a(h paramh)
  {
    this.h = paramh;
  }

  public final void a(Object paramObject)
  {
    this.p = paramObject;
  }

  public final void a(String paramString)
  {
    this.a = paramString;
  }

  public final byte[] a()
  {
    return this.j;
  }

  public final String b()
  {
    return this.o;
  }

  public final URL c()
  {
    return this.i;
  }

  public final void f()
  {
    if (m == null)
      m = Executors.newCachedThreadPool();
    m.execute(this);
  }

  public final Object g()
  {
    return this.p;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.f
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */