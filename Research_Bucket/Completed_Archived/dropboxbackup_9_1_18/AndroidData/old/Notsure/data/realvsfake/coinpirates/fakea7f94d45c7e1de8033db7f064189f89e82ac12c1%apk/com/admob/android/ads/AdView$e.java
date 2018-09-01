package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;

final class AdView$e
  implements Runnable
{
  private WeakReference<AdView> a;
  private WeakReference<k> b;
  private int c;
  private boolean d;

  public AdView$e(AdView paramAdView, k paramk, int paramInt, boolean paramBoolean)
  {
    this.a = new WeakReference(paramAdView);
    this.b = new WeakReference(paramk);
    this.c = paramInt;
    this.d = paramBoolean;
  }

  public final void run()
  {
    try
    {
      localAdView = (AdView)this.a.get();
      localk = (k)this.b.get();
      if ((localAdView != null) && (localk != null))
      {
        localAdView.addView(localk);
        AdView.a(localAdView, localk.c());
        if (this.c == 0)
        {
          if (this.d)
          {
            AdView.a(localAdView, localk);
            return;
          }
          AdView.b(localAdView, localk);
          return;
        }
      }
    }
    catch (Exception localException)
    {
      AdView localAdView;
      k localk;
      if (InterstitialAd.c.a("AdMobSDK", 6))
      {
        Log.e("AdMobSDK", "Unhandled exception placing AdContainer into AdView.", localException);
        return;
        AdView.c(localAdView, localk);
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.e
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */