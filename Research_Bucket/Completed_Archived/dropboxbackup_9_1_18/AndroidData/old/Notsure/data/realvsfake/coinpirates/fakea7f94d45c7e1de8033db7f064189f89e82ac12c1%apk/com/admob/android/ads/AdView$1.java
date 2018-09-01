package com.admob.android.ads;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

final class AdView$1
  implements Animation.AnimationListener
{
  AdView$1(AdView paramAdView, k paramk)
  {
  }

  public final void onAnimationEnd(Animation paramAnimation)
  {
    this.b.post(new AdView.g(this.a, this.b));
  }

  public final void onAnimationRepeat(Animation paramAnimation)
  {
  }

  public final void onAnimationStart(Animation paramAnimation)
  {
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */