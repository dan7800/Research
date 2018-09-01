package com.admob.android.ads;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

final class b
{
  private static String a = "http://r.admob.com/ad_source.php";
  private static int b;
  private static long c;
  private static String d = null;
  private static boolean e = false;
  private static boolean f = false;

  b()
  {
  }

  static j a(m paramm, Context paramContext, String paramString1, String paramString2, int paramInt1, int paramInt2, int paramInt3, k paramk, int paramInt4, j.b paramb, InterstitialAd.Event paramEvent, AdView.f paramf)
  {
    if (paramContext.checkCallingOrSelfPermission("android.permission.INTERNET") == -1)
      AdManager.clientError("Cannot request an ad without Internet permissions!  Open manifest.xml and just before the final </manifest> tag add:  <uses-permission android:name=\"android.permission.INTERNET\" />");
    boolean bool2;
    ResolveInfo localResolveInfo;
    if (!f)
    {
      f = true;
      bool2 = true;
      localResolveInfo = paramContext.getPackageManager().resolveActivity(new Intent(paramContext, AdMobActivity.class), 65536);
      if ((localResolveInfo != null) && (localResolveInfo.activityInfo != null) && ("com.admob.android.ads.AdMobActivity".equals(localResolveInfo.activityInfo.name)))
        break label134;
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "could not find com.admob.android.ads.AdMobActivity, please make sure it is registered in AndroidManifest.xml");
      bool2 = false;
    }
    Object localObject;
    while (true)
    {
      e = bool2;
      if (e)
        break;
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "com.admob.android.ads.AdMobActivity must be registered in your AndroidManifest.xml file.");
      localObject = null;
      return localObject;
      label134: if (localResolveInfo.activityInfo.theme != 16973831)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "The activity Theme for com.admob.android.ads.AdMobActivity is not @android:style/Theme.NoTitleBar.Fullscreen, please change in AndroidManifest.xml");
        bool2 = false;
      }
      if ((0x80 & localResolveInfo.activityInfo.configChanges) == 0)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "The android:configChanges value of the com.admob.android.ads.AdMobActivity must include orientation");
        bool2 = false;
      }
      if ((0x10 & localResolveInfo.activityInfo.configChanges) == 0)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "The android:configChanges value of the com.admob.android.ads.AdMobActivity must include keyboard");
        bool2 = false;
      }
      if ((0x20 & localResolveInfo.activityInfo.configChanges) == 0)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "The android:configChanges value of the com.admob.android.ads.AdMobActivity must include keyboardHidden");
        bool2 = false;
      }
    }
    t.a(paramContext);
    long l1 = SystemClock.uptimeMillis();
    String str1 = a(paramContext, paramString1, paramString2, paramInt4, paramb, paramEvent, paramf);
    e locale = g.a(a, null, AdManager.getUserId(paramContext), null, 3000, null, str1);
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Requesting an ad with POST params:  " + str1);
    boolean bool1 = locale.d();
    String str3;
    if (bool1)
    {
      byte[] arrayOfByte = locale.a();
      str3 = new String(arrayOfByte);
    }
    for (String str2 = str3; ; str2 = null)
    {
      while (true)
        if (bool1)
        {
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Ad response: ");
          if (!str2.equals(""))
          {
            JSONTokener localJSONTokener = new JSONTokener(str2);
            try
            {
              JSONObject localJSONObject = new JSONObject(localJSONTokener);
              if (InterstitialAd.c.a("AdMobSDK", 3))
                Log.d("AdMobSDK", localJSONObject.toString(4));
              localj = j.a(paramm, paramContext, localJSONObject, paramInt1, paramInt2, paramInt3, paramk, paramb);
              localObject = localj;
              if (!InterstitialAd.c.a("AdMobSDK", 4))
                break;
              l2 = SystemClock.uptimeMillis() - l1;
              if (localObject != null)
                break;
              Log.i("AdMobSDK", "No fill.  Server replied that no ads are available (" + l2 + "ms)");
              return localObject;
            }
            catch (JSONException localJSONException)
            {
              if (InterstitialAd.c.a("AdMobSDK", 5))
                Log.w("AdMobSDK", "Problem decoding ad response.  Cannot display ad: \"" + str2 + "\"", localJSONException);
            }
          }
        }
      while (true)
      {
        j localj;
        long l2;
        localObject = null;
      }
    }
  }

  static j a(m paramm, Context paramContext, String paramString1, String paramString2, InterstitialAd.Event paramEvent)
  {
    return a(paramm, paramContext, paramString1, paramString2, -1, -1, -1, null, -1, j.b.b, paramEvent, null);
  }

  static String a()
  {
    return a;
  }

  static String a(Context paramContext, String paramString1, String paramString2, int paramInt)
  {
    return a(paramContext, null, null, 0, null, null, null);
  }

  private static String a(Context paramContext, String paramString1, String paramString2, int paramInt, j.b paramb, InterstitialAd.Event paramEvent, AdView.f paramf)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Ad request:");
    AdManager.a(paramContext);
    StringBuilder localStringBuilder1 = new StringBuilder();
    long l = System.currentTimeMillis();
    localStringBuilder1.append("z").append("=").append(l / 1000L).append(".").append(l % 1000L);
    if (paramb == null);
    for (j.b localb1 = j.b.c; ; localb1 = paramb)
    {
      a(localStringBuilder1, "ad_type", localb1.toString());
      switch (1.a[localb1.ordinal()])
      {
      default:
      case 1:
      case 2:
      }
      String str1;
      while (true)
      {
        a(localStringBuilder1, "rt", "0");
        j.b localb2 = j.b.b;
        str1 = null;
        if (paramb == localb2)
          str1 = AdManager.getInterstitialPublisherId(paramContext);
        if (str1 == null)
          str1 = AdManager.getPublisherId(paramContext);
        if (str1 != null)
          break;
        throw new IllegalStateException("Publisher ID is not set!  To serve ads you must set your publisher ID assigned from www.admob.com.  Either add it to AndroidManifest.xml under the <application> tag or call AdManager.setPublisherId().");
        if (paramEvent != null)
        {
          a(localStringBuilder1, "event", String.valueOf(paramEvent.ordinal()));
          continue;
          if (paramf != null)
            a(localStringBuilder1, "dim", paramf.toString());
        }
      }
      a(localStringBuilder1, "s", str1);
      a(localStringBuilder1, "l", t.a());
      a(localStringBuilder1, "f", "jsonp");
      a(localStringBuilder1, "client_sdk", "1");
      a(localStringBuilder1, "ex", "1");
      a(localStringBuilder1, "v", "20101109-ANDROID-3312276cc1406347");
      a(localStringBuilder1, "isu", AdManager.getUserId(paramContext));
      a(localStringBuilder1, "so", AdManager.getOrientation(paramContext));
      if (paramInt > 0)
        a(localStringBuilder1, "screen_width", String.valueOf(paramInt));
      a(localStringBuilder1, "d[coord]", AdManager.b(paramContext));
      a(localStringBuilder1, "d[coord_timestamp]", AdManager.a());
      a(localStringBuilder1, "d[pc]", AdManager.getPostalCode());
      a(localStringBuilder1, "d[dob]", AdManager.b());
      a(localStringBuilder1, "d[gender]", AdManager.c());
      a(localStringBuilder1, "k", paramString1);
      a(localStringBuilder1, "search", paramString2);
      a(localStringBuilder1, "density", String.valueOf(k.d()));
      if (AdManager.isTestDevice(paramContext))
      {
        if (InterstitialAd.c.a("AdMobSDK", 4))
          Log.i("AdMobSDK", "Making ad request in test mode");
        a(localStringBuilder1, "m", "test");
        String str3 = AdManager.getTestAction();
        if ((paramb == j.b.b) && (j.a.a.toString().equals(str3)))
          str3 = "video_int";
        a(localStringBuilder1, "test_action", str3);
      }
      if (d == null)
      {
        StringBuilder localStringBuilder2 = new StringBuilder();
        PackageManager localPackageManager = paramContext.getPackageManager();
        List localList1 = localPackageManager.queryIntentActivities(new Intent("android.intent.action.VIEW", Uri.parse("geo:0,0?q=donuts")), 65536);
        if ((localList1 == null) || (localList1.size() == 0))
          localStringBuilder2.append("m");
        List localList2 = localPackageManager.queryIntentActivities(new Intent("android.intent.action.VIEW", Uri.parse("market://search?q=pname:com.admob")), 65536);
        if ((localList2 == null) || (localList2.size() == 0))
        {
          if (localStringBuilder2.length() > 0)
            localStringBuilder2.append(",");
          localStringBuilder2.append("a");
        }
        List localList3 = localPackageManager.queryIntentActivities(new Intent("android.intent.action.VIEW", Uri.parse("tel://6509313940")), 65536);
        if ((localList3 == null) || (localList3.size() == 0))
        {
          if (localStringBuilder2.length() > 0)
            localStringBuilder2.append(",");
          localStringBuilder2.append("t");
        }
        d = localStringBuilder2.toString();
      }
      String str2 = d;
      if ((str2 != null) && (str2.length() > 0))
        a(localStringBuilder1, "ic", str2);
      a(localStringBuilder1, "audio", String.valueOf(AdManager.a(new v(paramContext)).ordinal()));
      int i = 1 + b;
      b = i;
      if (i == 1)
      {
        c = System.currentTimeMillis();
        a(localStringBuilder1, "pub_data[identifier]", AdManager.getApplicationPackageName(paramContext));
        a(localStringBuilder1, "pub_data[version]", String.valueOf(AdManager.getApplicationVersion(paramContext)));
      }
      while (true)
      {
        return localStringBuilder1.toString();
        a(localStringBuilder1, "stats[reqs]", String.valueOf(b));
        a(localStringBuilder1, "stats[time]", String.valueOf((System.currentTimeMillis() - c) / 1000L));
      }
    }
  }

  static void a(String paramString)
  {
    if (paramString == null);
    for (String str = "http://r.admob.com/ad_source.php"; ; str = paramString)
    {
      if ((!"http://r.admob.com/ad_source.php".equals(str)) && (InterstitialAd.c.a("AdMobSDK", 5)))
        Log.w("AdMobSDK", "NOT USING PRODUCTION AD SERVER!  Using " + str);
      a = str;
      return;
    }
  }

  private static void a(StringBuilder paramStringBuilder, String paramString1, String paramString2)
  {
    if ((paramString2 != null) && (paramString2.length() > 0));
    try
    {
      paramStringBuilder.append("&").append(URLEncoder.encode(paramString1, "UTF-8")).append("=").append(URLEncoder.encode(paramString2, "UTF-8"));
      if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "    " + paramString1 + ": " + paramString2);
      return;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "UTF-8 encoding is not supported on this device.  Ad requests are impossible.", localUnsupportedEncodingException);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.b
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */