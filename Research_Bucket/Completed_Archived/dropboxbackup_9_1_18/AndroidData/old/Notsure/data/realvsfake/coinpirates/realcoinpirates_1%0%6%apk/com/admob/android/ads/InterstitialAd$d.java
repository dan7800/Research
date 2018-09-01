package com.admob.android.ads;

import android.app.Activity;
import java.lang.ref.WeakReference;

final class InterstitialAd$d
  implements Runnable
{
  private WeakReference<Activity> a;
  private WeakReference<InterstitialAd> b;

  public InterstitialAd$d(Activity paramActivity, InterstitialAd paramInterstitialAd)
  {
    this.a = new WeakReference(paramActivity);
    this.b = new WeakReference(paramInterstitialAd);
  }

  public final void run()
  {
    Activity localActivity = (Activity)this.a.get();
    InterstitialAd localInterstitialAd = (InterstitialAd)this.b.get();
    if ((localActivity != null) && (localInterstitialAd != null))
      localInterstitialAd.a(localActivity);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.d
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */