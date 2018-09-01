package com.admob.android.ads;

import android.graphics.PointF;
import android.view.View;

public final class ah
{
  public float a = 0.0F;
  public PointF b = new PointF(0.5F, 0.5F);

  public ah()
  {
  }

  public static float a(View paramView)
  {
    if (paramView != null)
      return c(paramView).a;
    return 0.0F;
  }

  public static PointF b(View paramView)
  {
    if (paramView != null)
      return c(paramView).b;
    return null;
  }

  public static ah c(View paramView)
  {
    Object localObject = paramView.getTag();
    if ((localObject != null) && ((localObject instanceof ah)))
      return (ah)localObject;
    return new ah();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ah
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */