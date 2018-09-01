package com.admob.android.ads;

import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public final class ap extends Animation
{
  private View a;
  private float b;
  private float c;

  public ap(float paramFloat1, float paramFloat2, View paramView)
  {
    this.b = paramFloat1;
    this.c = paramFloat2;
    this.a = paramView;
  }

  protected final void applyTransformation(float paramFloat, Transformation paramTransformation)
  {
    paramTransformation.setTransformationType(Transformation.TYPE_IDENTITY);
    if ((paramFloat < 0.0D) || (paramFloat > 1.0D));
    ViewParent localViewParent;
    do
    {
      return;
      float f = this.b + paramFloat * (this.c - this.b);
      View localView = this.a;
      if (localView != null)
      {
        ah localah = ah.c(localView);
        localah.a = f;
        localView.setTag(localah);
      }
      localViewParent = this.a.getParent();
    }
    while (!(localViewParent instanceof a));
    ((a)localViewParent).g();
  }

  public static abstract interface a
  {
    public abstract void g();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ap
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */