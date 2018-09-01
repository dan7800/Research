package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;

final class AdView$d
  implements Runnable
{
  boolean a;
  private WeakReference<AdView> b;

  public AdView$d(AdView paramAdView)
  {
    this.b = new WeakReference(paramAdView);
  }

  public final void run()
  {
    try
    {
      AdView localAdView = (AdView)this.b.get();
      if ((!this.a) && (localAdView != null))
      {
        if (InterstitialAd.c.a("AdMobSDK", 3))
        {
          int i = AdView.h(localAdView) / 1000;
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Requesting a fresh ad because a request interval passed (" + i + " seconds).");
        }
        AdView.i(localAdView);
      }
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "exception caught in RefreshHandler.run(), " + localException.getMessage());
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.d
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */