package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class InterstitialAd$e
  implements Runnable
{
  private WeakReference<InterstitialAd> a;

  public InterstitialAd$e(InterstitialAd paramInterstitialAd)
  {
    this.a = new WeakReference(paramInterstitialAd);
  }

  public final void run()
  {
    InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
    if (localInterstitialAd != null)
      localInterstitialAd.b();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.e
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */