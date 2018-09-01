package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;

final class AdView$c
  implements Runnable
{
  private WeakReference<AdView> a;

  public AdView$c(AdView paramAdView)
  {
    this.a = new WeakReference(paramAdView);
  }

  public final void run()
  {
    AdView localAdView = (AdView)this.a.get();
    if ((localAdView == null) || (((AdView.a(localAdView) == null) || (AdView.a(localAdView).getParent() == null)) && (AdView.b(localAdView) != null)))
      try
      {
        AdView.b(localAdView).onFailedToReceiveAd(localAdView);
        return;
      }
      catch (Exception localException2)
      {
        Log.w("AdMobSDK", "Unhandled exception raised in your AdListener.onFailedToReceiveAd.", localException2);
        return;
      }
    try
    {
      AdView.b(localAdView).onFailedToReceiveRefreshedAd(localAdView);
      return;
    }
    catch (Exception localException1)
    {
      Log.w("AdMobSDK", "Unhandled exception raised in your AdListener.onFailedToReceiveRefreshedAd.", localException1);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.c
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */