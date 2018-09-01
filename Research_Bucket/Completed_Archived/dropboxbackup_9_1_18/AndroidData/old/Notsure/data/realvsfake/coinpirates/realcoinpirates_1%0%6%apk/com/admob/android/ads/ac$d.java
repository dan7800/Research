package com.admob.android.ads;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import java.lang.ref.WeakReference;

public final class ac$d
  implements View.OnTouchListener
{
  private WeakReference<ac> a;

  public ac$d(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final boolean onTouch(View paramView, MotionEvent paramMotionEvent)
  {
    ac localac = (ac)this.a.get();
    if (localac == null)
      return false;
    ac.a(localac, false);
    ac.a(localac, paramMotionEvent);
    return false;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.d
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */