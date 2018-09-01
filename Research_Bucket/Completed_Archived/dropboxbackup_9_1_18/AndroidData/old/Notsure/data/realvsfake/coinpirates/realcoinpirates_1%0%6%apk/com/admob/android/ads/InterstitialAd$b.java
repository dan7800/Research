package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class InterstitialAd$b
  implements Runnable
{
  private WeakReference<InterstitialAd> a;

  public InterstitialAd$b(InterstitialAd paramInterstitialAd)
  {
    this.a = new WeakReference(paramInterstitialAd);
  }

  public final void run()
  {
    InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
    if (localInterstitialAd != null)
      localInterstitialAd.d();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.b
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */