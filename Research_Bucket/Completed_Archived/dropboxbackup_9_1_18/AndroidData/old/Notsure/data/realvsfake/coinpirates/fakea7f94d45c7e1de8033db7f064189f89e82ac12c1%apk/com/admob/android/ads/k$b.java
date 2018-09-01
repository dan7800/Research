package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;
import org.json.JSONObject;

public final class k$b extends Thread
{
  private JSONObject a;
  private WeakReference<k> b;

  public k$b(JSONObject paramJSONObject, k paramk)
  {
    this.a = paramJSONObject;
    this.b = new WeakReference(paramk);
  }

  public final void run()
  {
    try
    {
      k localk = (k)this.b.get();
      if ((localk != null) && (localk.a != null))
      {
        localk.a.a(this.a);
        if (localk.b != null)
          localk.b.performClick();
      }
      return;
    }
    catch (Exception localException)
    {
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "exception caught in AdClickThread.run(), ", localException);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.k.b
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */