package com.admob.android.ads;

import android.util.Log;

public class InterstitialAd$c
  implements m
{
  InterstitialAd a;

  public InterstitialAd$c()
  {
  }

  public InterstitialAd$c(InterstitialAd paramInterstitialAd)
  {
    this.a = paramInterstitialAd;
  }

  public static boolean a(String paramString, int paramInt)
  {
    if (paramInt >= 5);
    for (int i = 1; (i != 0) || (Log.isLoggable(paramString, paramInt)); i = 0)
      return true;
    return false;
  }

  public final void a()
  {
    if (this.a != null)
      this.a.c();
  }

  public final void a(j paramj)
  {
    if (this.a != null)
    {
      InterstitialAd.a(this.a, paramj);
      this.a.a();
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.c
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */