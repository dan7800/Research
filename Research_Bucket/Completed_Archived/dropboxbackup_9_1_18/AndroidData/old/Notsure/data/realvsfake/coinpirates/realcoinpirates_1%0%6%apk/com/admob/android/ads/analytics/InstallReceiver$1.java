package com.admob.android.ads.analytics;

import android.util.Log;
import com.admob.android.ads.InterstitialAd.c;
import com.admob.android.ads.e;
import com.admob.android.ads.h;

final class InstallReceiver$1
  implements h
{
  InstallReceiver$1(InstallReceiver paramInstallReceiver)
  {
  }

  public final void a(e parame)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Recorded install from an AdMob ad.");
  }

  public final void a(e parame, Exception paramException)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Failed to record install from an AdMob ad.", paramException);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.analytics.InstallReceiver.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */