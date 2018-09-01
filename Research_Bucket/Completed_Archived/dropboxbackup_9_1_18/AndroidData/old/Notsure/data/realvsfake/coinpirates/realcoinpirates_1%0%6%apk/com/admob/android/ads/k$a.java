package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;

final class k$a
  implements Runnable
{
  private WeakReference<k> a;

  public k$a(k paramk)
  {
    this.a = new WeakReference(paramk);
  }

  public final void run()
  {
    try
    {
      k localk = (k)this.a.get();
      if (localk != null)
        localk.addView(localk.c);
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "exception caught in AdContainer post run(), " + localException.getMessage());
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.k.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */