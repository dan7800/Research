package com.admob.android.ads;

import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public final class am extends TranslateAnimation
{
  public am(int paramInt1, float paramFloat1, int paramInt2, float paramFloat2, int paramInt3, float paramFloat3, int paramInt4, float paramFloat4)
  {
    super(0, paramFloat1, 0, paramFloat2, 0, paramFloat3, 0, paramFloat4);
  }

  protected final void applyTransformation(float paramFloat, Transformation paramTransformation)
  {
    if ((paramFloat >= 0.0D) || (paramFloat <= 1.0D))
      super.applyTransformation(paramFloat, paramTransformation);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.am
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */