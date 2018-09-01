package com.admob.android.ads.analytics;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.admob.android.ads.AdManager;
import com.admob.android.ads.InterstitialAd.c;
import com.admob.android.ads.e;
import com.admob.android.ads.g;
import com.admob.android.ads.h;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

public class InstallReceiver extends BroadcastReceiver
{
  public InstallReceiver()
  {
  }

  private static String a(String paramString1, String paramString2, String paramString3)
  {
    int i = 0;
    if (paramString1 != null)
      try
      {
        String[] arrayOfString1 = paramString1.split("&");
        localStringBuilder = null;
        if (i < arrayOfString1.length)
        {
          str4 = arrayOfString1[i];
          if (!str4.startsWith("admob_"))
            break label247;
          arrayOfString2 = str4.substring("admob_".length()).split("=");
          str5 = URLEncoder.encode(arrayOfString2[0], "UTF-8");
          str6 = URLEncoder.encode(arrayOfString2[1], "UTF-8");
          if (localStringBuilder == null)
            localStringBuilder = new StringBuilder(128);
          while (true)
          {
            localStringBuilder.append(str5).append("=").append(str6);
            break;
            localStringBuilder.append("&");
          }
        }
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "Could not create install URL.  Install not tracked.", localUnsupportedEncodingException);
      }
    while (true)
    {
      StringBuilder localStringBuilder;
      String str4;
      String[] arrayOfString2;
      String str5;
      String str6;
      return null;
      if (localStringBuilder != null)
      {
        String str1 = URLEncoder.encode(paramString2, "UTF-8");
        localStringBuilder.append("&").append("isu").append("=").append(str1);
        String str2 = URLEncoder.encode(paramString3, "UTF-8");
        localStringBuilder.append("&").append("app_id").append("=").append(str2);
        String str3 = "http://a.admob.com/f0?" + localStringBuilder.toString();
        return str3;
        label247: i++;
      }
    }
  }

  private static void a(Context paramContext, Intent paramIntent)
  {
    try
    {
      PackageManager localPackageManager = paramContext.getPackageManager();
      if (localPackageManager != null)
      {
        localActivityInfo = localPackageManager.getReceiverInfo(new ComponentName(paramContext, InstallReceiver.class), 128);
        if ((localActivityInfo != null) && (localActivityInfo.metaData != null))
        {
          localSet = localActivityInfo.metaData.keySet();
          if (localSet != null)
          {
            localIterator = localSet.iterator();
            while (localIterator.hasNext())
            {
              str1 = (String)localIterator.next();
              str2 = null;
              try
              {
                str2 = localActivityInfo.metaData.getString(str1);
                if (!str2.equals("com.google.android.apps.analytics.AnalyticsReceiver"))
                {
                  ((BroadcastReceiver)Class.forName(str2).newInstance()).onReceive(paramContext, paramIntent);
                  if (InterstitialAd.c.a("AdMobSDK", 3))
                    Log.d("AdMobSDK", "Successfully forwarded install notification to " + str2);
                }
              }
              catch (Exception localException3)
              {
                Log.w("AdMobSDK", "Could not forward Market's INSTALL_REFERRER intent to " + str2, localException3);
              }
            }
          }
        }
      }
    }
    catch (Exception localException1)
    {
      if (InterstitialAd.c.a("AdMobSDK", 5))
        Log.w("AdMobSDK", "Unhandled exception while forwarding install intents.  Possibly lost some install information.", localException1);
      return;
    }
    do
      try
      {
        do
        {
          ActivityInfo localActivityInfo;
          Set localSet;
          Iterator localIterator;
          String str1;
          String str2;
          ((BroadcastReceiver)Class.forName("com.google.android.apps.analytics.AnalyticsReceiver").newInstance()).onReceive(paramContext, paramIntent);
        }
        while (!InterstitialAd.c.a("AdMobSDK", 3));
        Log.d("AdMobSDK", "Successfully forwarded install notification to com.google.android.apps.analytics.AnalyticsReceiver");
        return;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        while (!InterstitialAd.c.a("AdMobSDK", 2));
        Log.v("AdMobSDK", "Google Analytics not installed.");
        return;
      }
      catch (Exception localException2)
      {
      }
    while (!InterstitialAd.c.a("AdMobSDK", 5));
    Log.w("AdMobSDK", "Exception from the Google Analytics install receiver.", localException2);
  }

  public void onReceive(Context paramContext, Intent paramIntent)
  {
    try
    {
      String str1 = paramIntent.getStringExtra("referrer");
      String str2 = AdManager.getUserId(paramContext);
      String str3 = a(str1, str2, AdManager.getApplicationPackageName(paramContext));
      if (str3 != null)
      {
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "Processing install from an AdMob ad (" + str3 + ").");
        e locale = g.a(str3, "Conversion", str2);
        locale.a(new h()
        {
          public final void a(e paramAnonymouse)
          {
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Recorded install from an AdMob ad.");
          }

          public final void a(e paramAnonymouse, Exception paramAnonymousException)
          {
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Failed to record install from an AdMob ad.", paramAnonymousException);
          }
        });
        locale.d();
      }
      a(paramContext, paramIntent);
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "Unhandled exception processing Market install.", localException);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.analytics.InstallReceiver
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */