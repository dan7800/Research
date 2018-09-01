package com.admob.android.ads;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

final class InterstitialAd$f extends TimerTask
{
  private WeakReference<InterstitialAd> a;

  public InterstitialAd$f(InterstitialAd paramInterstitialAd)
  {
    this.a = new WeakReference(paramInterstitialAd);
  }

  public final void run()
  {
    InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
    if (localInterstitialAd != null)
      InterstitialAd.a(localInterstitialAd);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.f
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */