package com.admob.android.ads;

public abstract interface AdListener
{
  public abstract void onFailedToReceiveAd(AdView paramAdView);

  public abstract void onFailedToReceiveRefreshedAd(AdView paramAdView);

  public abstract void onReceiveAd(AdView paramAdView);

  public abstract void onReceiveRefreshedAd(AdView paramAdView);
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdListener
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */