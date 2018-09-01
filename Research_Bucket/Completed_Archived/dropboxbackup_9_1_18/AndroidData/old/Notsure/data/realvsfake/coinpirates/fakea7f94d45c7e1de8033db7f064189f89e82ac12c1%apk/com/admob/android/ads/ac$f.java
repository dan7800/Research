package com.admob.android.ads;

import android.widget.VideoView;
import java.lang.ref.WeakReference;

final class ac$f
  implements Runnable
{
  private WeakReference<ac> a;

  public ac$f(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void run()
  {
    ac localac = (ac)this.a.get();
    if (localac == null);
    while (localac.e == null)
      return;
    localac.e.setVisibility(0);
    localac.e.requestLayout();
    localac.e.requestFocus();
    localac.e.start();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.f
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */