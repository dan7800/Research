package com.admob.android.ads;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import java.lang.ref.WeakReference;

final class AdView$g
  implements Runnable
{
  private WeakReference<AdView> a;
  private WeakReference<k> b;

  public AdView$g(k paramk, AdView paramAdView)
  {
    this.b = new WeakReference(paramk);
    this.a = new WeakReference(paramAdView);
  }

  public final void run()
  {
    try
    {
      final AdView localAdView = (AdView)this.a.get();
      final k localk1 = (k)this.b.get();
      if ((localAdView != null) && (localk1 != null))
      {
        final k localk2 = AdView.a(localAdView);
        if (localk2 != null)
          localk2.setVisibility(8);
        localk1.setVisibility(0);
        an localan = new an(90.0F, 0.0F, localAdView.getWidth() / 2.0F, localAdView.getHeight() / 2.0F, -0.4F * localAdView.getWidth(), false);
        localan.setDuration(700L);
        localan.setFillAfter(true);
        localan.setInterpolator(new DecelerateInterpolator());
        localan.setAnimationListener(new Animation.AnimationListener()
        {
          public final void onAnimationEnd(Animation paramAnonymousAnimation)
          {
            if (localk2 != null)
              localAdView.removeView(localk2);
            AdView.c(localAdView, localk1);
            if (localk2 != null)
              localk2.e();
          }

          public final void onAnimationRepeat(Animation paramAnonymousAnimation)
          {
          }

          public final void onAnimationStart(Animation paramAnonymousAnimation)
          {
          }
        });
        localAdView.startAnimation(localan);
      }
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "exception caught in SwapViews.run(), " + localException.getMessage());
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.g
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */