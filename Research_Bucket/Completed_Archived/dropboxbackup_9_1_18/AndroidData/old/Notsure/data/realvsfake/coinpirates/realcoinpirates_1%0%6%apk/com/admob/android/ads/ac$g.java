package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class ac$g
  implements Runnable
{
  private WeakReference<ac> a;

  public ac$g(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void run()
  {
    ac localac = (ac)this.a.get();
    if (localac == null)
      return;
    localac.b();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.g
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */