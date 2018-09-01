package com.admob.android.ads;

import java.lang.ref.WeakReference;

final class ac$b
  implements Runnable
{
  private WeakReference<ac> a;

  public ac$b(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void run()
  {
    ac localac = (ac)this.a.get();
    if (localac != null)
    {
      ac.a(localac);
      localac.d();
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.b
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */