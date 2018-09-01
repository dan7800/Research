package com.admob.android.ads;

import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;

public final class ak extends AlphaAnimation
{
  public ak(float paramFloat1, float paramFloat2)
  {
    super(paramFloat1, paramFloat2);
  }

  protected final void applyTransformation(float paramFloat, Transformation paramTransformation)
  {
    if ((paramFloat >= 0.0D) || (paramFloat <= 1.0D))
      super.applyTransformation(paramFloat, paramTransformation);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ak
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */