package com.admob.android.ads;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public final class x extends RelativeLayout
{
  private int a;
  private int b;
  private ImageView c;
  private float d;

  public x(Context paramContext, View paramView, int paramInt1, int paramInt2, Bitmap paramBitmap)
  {
    super(paramContext);
    this.b = paramInt1;
    this.a = paramInt2;
    setClickable(true);
    setFocusable(true);
    this.d = getResources().getDisplayMetrics().density;
    this.c = new ImageView(paramContext);
    BitmapDrawable localBitmapDrawable = new BitmapDrawable(paramBitmap);
    localBitmapDrawable.setBounds(0, 0, (int)(paramInt1 * this.d), (int)(paramInt2 * this.d));
    this.c.setImageDrawable(localBitmapDrawable);
    this.c.setVisibility(4);
    RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams((int)(paramInt1 * this.d), (int)(paramInt2 * this.d));
    localLayoutParams.addRule(13);
    addView(paramView, localLayoutParams);
    addView(this.c, localLayoutParams);
  }

  private void a(boolean paramBoolean)
  {
    if (paramBoolean == true)
    {
      this.c.setVisibility(0);
      return;
    }
    this.c.setVisibility(4);
  }

  public final boolean dispatchTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = paramMotionEvent.getAction();
    if (InterstitialAd.c.a("AdMobSDK", 2))
      Log.v("AdMobSDK", "dispatchTouchEvent: action=" + i + " x=" + paramMotionEvent.getX() + " y=" + paramMotionEvent.getY());
    if (i == 0)
      a(true);
    while (true)
    {
      return super.dispatchTouchEvent(paramMotionEvent);
      if (i == 2)
        a(new Rect(0, 0, (int)(this.b * this.d), (int)(this.a * this.d)).contains((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY()));
      else if (i == 1)
        a(false);
      else if (i == 3)
        a(false);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.x
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */