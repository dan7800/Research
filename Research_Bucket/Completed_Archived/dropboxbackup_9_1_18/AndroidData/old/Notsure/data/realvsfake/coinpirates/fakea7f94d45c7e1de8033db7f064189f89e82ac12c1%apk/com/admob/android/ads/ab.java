package com.admob.android.ads;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

public abstract class ab extends RelativeLayout
{
  protected Handler a = new Handler();
  protected float b = getResources().getDisplayMetrics().density;
  protected r c;

  public ab(Context paramContext)
  {
    super(paramContext);
  }

  public final void a(r paramr)
  {
    this.c = paramr;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ab
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */