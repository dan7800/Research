package com.admob.android.ads;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

final class AdView$g$1
  implements Animation.AnimationListener
{
  AdView$g$1(AdView.g paramg, k paramk1, AdView paramAdView, k paramk2)
  {
  }

  public final void onAnimationEnd(Animation paramAnimation)
  {
    if (this.a != null)
      this.b.removeView(this.a);
    AdView.c(this.b, this.c);
    if (this.a != null)
      this.a.e();
  }

  public final void onAnimationRepeat(Animation paramAnimation)
  {
  }

  public final void onAnimationStart(Animation paramAnimation)
  {
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.g.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */