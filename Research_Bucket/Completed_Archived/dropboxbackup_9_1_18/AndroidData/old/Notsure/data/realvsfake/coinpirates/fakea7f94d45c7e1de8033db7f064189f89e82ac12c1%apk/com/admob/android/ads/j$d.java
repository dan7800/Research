package com.admob.android.ads;

import android.util.Log;
import java.lang.ref.WeakReference;
import org.json.JSONArray;

final class j$d
  implements Runnable
{
  private WeakReference<j> a;
  private JSONArray b;

  public j$d(j paramj, JSONArray paramJSONArray)
  {
    this.a = new WeakReference(paramj);
    this.b = paramJSONArray;
  }

  public final void run()
  {
    try
    {
      j localj2 = (j)this.a.get();
      if (localj2 != null)
        j.a(localj2, this.b);
      return;
    }
    catch (Exception localException)
    {
      j localj1;
      do
      {
        if (InterstitialAd.c.a("AdMobSDK", 6))
        {
          Log.e("AdMobSDK", "exception caught in Ad$ViewAdd.run(), " + localException.getMessage());
          localException.printStackTrace();
        }
        localj1 = (j)this.a.get();
      }
      while (localj1 == null);
      j.a(localj1);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.j.d
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */