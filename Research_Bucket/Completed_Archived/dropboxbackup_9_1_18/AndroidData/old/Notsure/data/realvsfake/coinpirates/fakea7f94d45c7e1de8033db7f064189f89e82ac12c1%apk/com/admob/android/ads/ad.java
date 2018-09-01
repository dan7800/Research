package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.admob.android.ads.view.AdMobWebView;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

public class ad extends WebViewClient
{
  private WeakReference<Activity> a;
  private ag b;
  private Map<String, String> c;
  protected WeakReference<AdMobWebView> d;

  public ad(AdMobWebView paramAdMobWebView)
  {
    this(paramAdMobWebView, null);
  }

  public ad(AdMobWebView paramAdMobWebView, WeakReference<Activity> paramWeakReference)
  {
    this.d = new WeakReference(paramAdMobWebView);
    this.a = paramWeakReference;
    this.b = new ag((Activity)paramWeakReference.get(), this.d);
    this.c = null;
    paramAdMobWebView.addJavascriptInterface(this.b, "JsProxy");
  }

  public static Hashtable<String, String> a(String paramString)
  {
    Hashtable localHashtable = null;
    if (paramString != null)
    {
      localHashtable = new Hashtable();
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "&");
      while (localStringTokenizer.hasMoreTokens())
      {
        String str1 = localStringTokenizer.nextToken();
        int i = str1.indexOf('=');
        if (i != -1)
        {
          String str2 = str1.substring(0, i);
          String str3 = str1.substring(i + 1);
          if ((str2 != null) && (str3 != null))
            localHashtable.put(str2, str3);
        }
      }
    }
    return localHashtable;
  }

  public void onPageFinished(WebView paramWebView, String paramString)
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
    if (this.c == null)
    {
      this.c = new HashMap();
      Context localContext = localAdMobWebView.getContext();
      this.c.put("sdkVersion", "20101109-ANDROID-3312276cc1406347");
      this.c.put("ua", f.h());
      this.c.put("portrait", AdManager.getOrientation(localContext));
      this.c.put("width", String.valueOf(localAdMobWebView.getWidth()));
      this.c.put("height", String.valueOf(localAdMobWebView.getHeight()));
      this.c.put("isu", AdManager.getUserId(localContext));
    }
    Object[] arrayOfObject = new Object[2];
    arrayOfObject[0] = "loaded";
    arrayOfObject[1] = this.c;
    localAdMobWebView.a("onEvent", arrayOfObject);
  }

  public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
  {
    if (InterstitialAd.c.a("AdMobSDK", 2))
      Log.v("AdMobSDK", "shouldOverrideUrlLoading, url: " + paramString);
    AdMobWebView localAdMobWebView = (AdMobWebView)this.d.get();
    if (localAdMobWebView == null)
      return false;
    Context localContext = localAdMobWebView.getContext();
    try
    {
      URI localURI = new URI(paramString);
      if ("admob".equals(localURI.getScheme()))
      {
        String str1 = localURI.getHost();
        if ("launch".equals(str1))
        {
          String str6 = localURI.getQuery();
          if (str6 != null)
          {
            Hashtable localHashtable3 = a(str6);
            if (localHashtable3 != null)
            {
              String str7 = (String)localHashtable3.get("url");
              if (str7 != null)
              {
                if (!(localContext instanceof Activity))
                  localContext = (Context)this.a.get();
                if (localContext == null)
                  break label429;
                localContext.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str7)));
                break label429;
              }
            }
          }
        }
        else if ("open".equals(str1))
        {
          String str4 = localURI.getQuery();
          if (str4 != null)
          {
            Hashtable localHashtable2 = a(str4);
            if (localHashtable2 != null)
            {
              String str5 = (String)localHashtable2.get("vars");
              if (str5 != null)
              {
                localAdMobWebView.loadUrl("javascript: JsProxy.setDataAndOpen(" + str5 + ")");
                return true;
              }
            }
          }
        }
        else if ("closecanvas".equals(str1))
        {
          if (paramWebView == localAdMobWebView)
          {
            localAdMobWebView.a();
            return true;
          }
        }
        else if ("log".equals(str1))
        {
          String str2 = localURI.getQuery();
          if (str2 != null)
          {
            Hashtable localHashtable1 = a(str2);
            if (localHashtable1 != null)
            {
              String str3 = (String)localHashtable1.get("string");
              if (str3 != null)
              {
                if (!InterstitialAd.c.a("AdMobSDK", 3))
                  break label431;
                Log.d("AdMobSDK", "<AdMob:WebView>: " + str3);
                break label431;
              }
            }
          }
        }
        else
        {
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Received message from JS but didn't know how to handle: " + paramString);
          return true;
        }
      }
    }
    catch (URISyntaxException localURISyntaxException)
    {
      Log.w("AdMobSDK", "Bad link URL in AdMob web view.", localURISyntaxException);
    }
    return false;
    label429: return true;
    label431: return true;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ad
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */