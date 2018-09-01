package com.admob.android.ads;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONTokener;

final class c
{
  private static boolean a = false;

  c()
  {
  }

  public static void a(Context paramContext)
  {
    if (!a)
    {
      a = true;
      if (AdManager.isEmulator())
        try
        {
          String str1 = b.a(paramContext, null, null, 0);
          StringBuilder localStringBuilder = new StringBuilder();
          localStringBuilder.append("http://api.admob.com/v1/pubcode/android_sdk_emulator_notice");
          localStringBuilder.append("?");
          localStringBuilder.append(str1);
          e locale = g.a(localStringBuilder.toString(), "developer_message", AdManager.getUserId(paramContext));
          if (locale.d())
          {
            byte[] arrayOfByte = locale.a();
            if (arrayOfByte != null)
            {
              String str2 = new JSONObject(new JSONTokener(new String(arrayOfByte))).getString("data");
              if ((str2 != null) && (!str2.equals("")))
                Log.w("AdMobSDK", str2);
            }
          }
          return;
        }
        catch (Exception localException)
        {
          while (!InterstitialAd.c.a("AdMobSDK", 2));
          Log.v("AdMobSDK", "Unhandled exception retrieving developer message.", localException);
        }
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.c
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */