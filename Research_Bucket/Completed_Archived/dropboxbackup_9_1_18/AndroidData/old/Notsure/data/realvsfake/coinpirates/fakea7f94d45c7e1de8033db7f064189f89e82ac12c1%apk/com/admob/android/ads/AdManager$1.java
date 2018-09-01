package com.admob.android.ads;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import java.util.Date;

final class AdManager$1
  implements LocationListener
{
  AdManager$1(LocationManager paramLocationManager)
  {
  }

  public final void onLocationChanged(Location paramLocation)
  {
    AdManager.a(paramLocation);
    AdManager.a(System.currentTimeMillis());
    this.a.removeUpdates(this);
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Acquired location " + AdManager.d().getLatitude() + "," + AdManager.d().getLongitude() + " at " + new Date(AdManager.e()).toString() + ".");
  }

  public final void onProviderDisabled(String paramString)
  {
  }

  public final void onProviderEnabled(String paramString)
  {
  }

  public final void onStatusChanged(String paramString, int paramInt, Bundle paramBundle)
  {
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdManager.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */