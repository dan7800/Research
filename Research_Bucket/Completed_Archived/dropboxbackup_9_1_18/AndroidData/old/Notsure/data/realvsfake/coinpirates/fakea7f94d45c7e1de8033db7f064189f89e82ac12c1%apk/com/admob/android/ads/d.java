package com.admob.android.ads;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class d
  implements h
{
  private final String a;
  private final String b;
  private final String c;
  private String d;
  private a e;
  private HashSet<e> f;
  private int g;

  public d(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    this.a = paramString1;
    this.b = paramString2;
    this.c = paramString3;
    this.d = paramString4;
    this.e = null;
    this.f = new HashSet();
    this.g = 0;
  }

  private String a(String paramString, Map<String, String> paramMap, boolean paramBoolean)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    try
    {
      localStringBuilder.append("rt=1&ex=1");
      localStringBuilder.append("&a=").append(this.a);
      localStringBuilder.append("&p=").append(URLEncoder.encode(paramString, "UTF-8"));
      localStringBuilder.append("&o=").append(this.d);
      localStringBuilder.append("&v=").append("20101109-ANDROID-3312276cc1406347");
      long l = System.currentTimeMillis();
      localStringBuilder.append("&z").append("=").append(l / 1000L).append(".").append(l % 1000L);
      localStringBuilder.append("&h%5BHTTP_HOST%5D=").append(URLEncoder.encode(this.c, "UTF-8"));
      localStringBuilder.append("&h%5BHTTP_REFERER%5D=http%3A%2F%2F").append(this.b);
      if (paramBoolean)
        localStringBuilder.append("&startvisit=1");
      if (paramMap != null)
      {
        Set localSet = paramMap.keySet();
        if (localSet != null)
        {
          Iterator localIterator = localSet.iterator();
          while (localIterator.hasNext())
          {
            String str = (String)localIterator.next();
            localStringBuilder.append("&").append(str).append("=").append(URLEncoder.encode((String)paramMap.get(str)));
          }
        }
      }
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      return null;
    }
    return localStringBuilder.toString();
  }

  private void b(e parame)
  {
    this.f.remove(parame);
  }

  public final void a(e parame)
  {
    if (InterstitialAd.c.a("AdMobSDK", 2))
    {
      Log.v("AdMobSDK", "Analytics event " + parame.b() + " has been recorded.");
      int i = this.f.size();
      if (i > 0)
        Log.v("AdMobSDK", "Pending Analytics requests: " + i);
    }
    b(parame);
  }

  public final void a(e parame, Exception paramException)
  {
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "analytics request failed for " + parame.b(), paramException);
    b(parame);
  }

  public final void a(String paramString, Map<String, String> paramMap)
  {
    if (this.c == null);
    label157: 
    do
    {
      do
        return;
      while (paramString == null);
      this.g = (1 + this.g);
      if (this.g == 1);
      for (boolean bool = true; ; bool = false)
      {
        String str = a(paramString, paramMap, bool);
        if (str == null)
          break label157;
        if (this.f != null)
        {
          e locale = g.a("http://r.admob.com/ad_source.php", "AnalyticsData", this.d, this, 5000, null, str);
          this.f.add(locale);
          locale.f();
        }
        if (!InterstitialAd.c.a("AdMobSDK", 3))
          break;
        Log.d("AdMobSDK", "Analytics event " + this.c + "/" + paramString + " data:" + str + " has been recorded.");
        return;
      }
    }
    while (!InterstitialAd.c.a("AdMobSDK", 6));
    Log.e("AdMobSDK", "Could not create analytics URL.  Analytics data not tracked.");
  }

  public static abstract interface a
  {
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.d
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */