package com.admob.android.ads;

import android.util.Log;
import android.webkit.WebView;
import com.admob.android.ads.view.AdMobWebView;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public final class z$a extends ad
{
  boolean a;
  boolean b;
  j c;
  private Timer e;
  private TimerTask f;

  public z$a(z paramz, AdMobWebView paramAdMobWebView, j paramj)
  {
    super(paramAdMobWebView);
    this.c = paramj;
    this.a = false;
    this.b = false;
  }

  public final void onPageFinished(WebView paramWebView, String paramString)
  {
    AdMobWebView localAdMobWebView = (AdMobWebView)this.d.get();
    if (localAdMobWebView == null);
    do
    {
      return;
      if ((paramString != null) && (paramString.equals(localAdMobWebView.c)))
        break;
    }
    while (!InterstitialAd.c.a("AdMobSDK", 4));
    Log.i("AdMobSDK", "Unexpected page loaded, urlThatFinished: " + paramString);
    return;
    this.b = true;
    super.onPageFinished(paramWebView, paramString);
    if ((localAdMobWebView instanceof z))
      ((z)localAdMobWebView).b();
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "startResponseTimer()");
    this.f = new TimerTask()
    {
      public final void run()
      {
        if (!z.a.this.a)
        {
          z.a.this.a = true;
          if (z.a.this.c != null)
            z.a.this.c.a(false);
        }
      }
    };
    this.e = new Timer();
    this.e.schedule(this.f, 10000L);
  }

  public final boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
  {
    if (InterstitialAd.c.a("AdMobSDK", 2))
      Log.v("AdMobSDK", "shouldOverrideUrlLoading, url: " + paramString);
    try
    {
      URI localURI = new URI(paramString);
      if ("admob".equals(localURI.getScheme()))
      {
        String str1 = localURI.getHost();
        if ("ready".equals(str1))
        {
          if (this.a)
            return true;
          this.a = true;
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "cancelResponseTimer()");
          if (this.e != null)
            this.e.cancel();
          String str4 = localURI.getQuery();
          if (str4 != null)
          {
            Hashtable localHashtable2 = a(str4);
            if (localHashtable2 != null)
            {
              String str5 = (String)localHashtable2.get("success");
              if ((str5 != null) && ("true".equalsIgnoreCase(str5)))
              {
                if (this.c == null)
                  break label358;
                this.c.a(true);
                break label358;
              }
            }
          }
          if (this.c == null)
            break label360;
          this.c.a(false);
          break label360;
        }
        if ("movie".equals(str1))
        {
          String str2 = localURI.getQuery();
          if (str2 != null)
          {
            Hashtable localHashtable1 = a(str2);
            if (localHashtable1 != null)
            {
              String str3 = (String)localHashtable1.get("action");
              if ((str3 != null) && (!"play".equalsIgnoreCase(str3)) && (!"pause".equalsIgnoreCase(str3)) && (!"stop".equalsIgnoreCase(str3)) && (!"remove".equalsIgnoreCase(str3)) && (!"replay".equalsIgnoreCase(str3)) && (InterstitialAd.c.a("AdMobSDK", 5)))
                Log.w("AdMobSDK", "Unknown actionString, admob://movie?action=" + str3);
            }
          }
          return true;
        }
      }
    }
    catch (URISyntaxException localURISyntaxException)
    {
      Log.w("AdMobSDK", "Bad link URL in AdMob web view.", localURISyntaxException);
    }
    return super.shouldOverrideUrlLoading(paramWebView, paramString);
    label358: return true;
    label360: return true;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.z.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */