package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class k$c
  implements Runnable
{
  private WeakReference<k> a;

  public k$c(k paramk)
  {
    this.a = new WeakReference(paramk);
  }

  public final void run()
  {
    try
    {
      k localk = (k)this.a.get();
      if (localk != null)
        localk.f();
      return;
    }
    catch (Exception localException)
    {
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.k.c
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */