package com.admob.android.ads;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class AdView$a
  implements m
{
  private WeakReference<AdView> a;

  public AdView$a()
  {
  }

  public AdView$a(AdView paramAdView)
  {
    this.a = new WeakReference(paramAdView);
  }

  public static Bundle a(n paramn)
  {
    if (paramn == null)
      return null;
    return paramn.a();
  }

  public static ArrayList<Bundle> a(Vector<? extends n> paramVector)
  {
    Object localObject;
    if (paramVector == null)
      localObject = null;
    while (true)
    {
      return localObject;
      localObject = new ArrayList();
      Iterator localIterator = paramVector.iterator();
      while (localIterator.hasNext())
      {
        n localn = (n)localIterator.next();
        if (localn == null)
          ((ArrayList)localObject).add(null);
        else
          ((ArrayList)localObject).add(localn.a());
      }
    }
  }

  public final void a()
  {
    AdView localAdView = (AdView)this.a.get();
    if (localAdView != null)
      AdView.f(localAdView);
  }

  public final void a(j paramj)
  {
    AdView localAdView = (AdView)this.a.get();
    if (localAdView != null)
      try
      {
        if ((AdView.a(localAdView) != null) && (paramj.equals(AdView.a(localAdView).c())))
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Received the same ad we already had.  Discarding it.");
        while (true)
        {
          return;
          if (InterstitialAd.c.a("AdMobSDK", 4))
            Log.i("AdMobSDK", "Ad returned (" + (SystemClock.uptimeMillis() - AdView.g(localAdView)) + " ms):  " + paramj);
          localAdView.getContext();
          localAdView.a(paramj, paramj.c());
        }
      }
      finally
      {
      }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */