package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class ac$c
  implements Runnable
{
  private WeakReference<ac> a;

  public ac$c(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void run()
  {
    ac localac = (ac)this.a.get();
    if (localac == null);
    while ((!localac.e()) || (localac.g != 2) || (localac.k == null))
      return;
    localac.k.a();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.c
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */