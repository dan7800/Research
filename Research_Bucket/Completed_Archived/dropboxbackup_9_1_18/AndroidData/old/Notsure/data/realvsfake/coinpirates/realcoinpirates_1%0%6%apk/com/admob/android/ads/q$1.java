package com.admob.android.ads;

import android.util.Log;

final class q$1
  implements h
{
  q$1()
  {
  }

  public final void a(e parame)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Click processed at " + parame.c());
  }

  public final void a(e parame, Exception paramException)
  {
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Click processing failed at " + parame.c(), paramException);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.q.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */