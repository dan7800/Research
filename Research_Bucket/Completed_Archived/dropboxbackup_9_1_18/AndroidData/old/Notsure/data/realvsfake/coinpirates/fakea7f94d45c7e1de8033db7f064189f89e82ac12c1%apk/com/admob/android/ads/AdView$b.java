package com.admob.android.ads;

import android.content.Context;
import android.util.Log;
import java.lang.ref.WeakReference;

final class AdView$b extends Thread
{
  private WeakReference<AdView> a;

  public AdView$b(AdView paramAdView)
  {
    this.a = new WeakReference(paramAdView);
  }

  public final void run()
  {
    localAdView = (AdView)this.a.get();
    if (localAdView != null)
      try
      {
        Context localContext = localAdView.getContext();
        k localk = new k(null, localContext, localAdView);
        int i = (int)(localAdView.getMeasuredWidth() / k.d());
        if (b.a(AdView.c(localAdView), localContext, AdView.d(localAdView), AdView.e(localAdView), localAdView.getPrimaryTextColor(), localAdView.getSecondaryTextColor(), localAdView.getBackgroundColor(), localk, i, localAdView.a(), null, localAdView.b()) == null)
          AdView.f(localAdView);
        return;
      }
      catch (Exception localException)
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "Unhandled exception requesting a fresh ad.", localException);
        AdView.f(localAdView);
        return;
      }
      finally
      {
        AdView.a(localAdView, false);
        AdView.b(localAdView, true);
      }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.b
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */