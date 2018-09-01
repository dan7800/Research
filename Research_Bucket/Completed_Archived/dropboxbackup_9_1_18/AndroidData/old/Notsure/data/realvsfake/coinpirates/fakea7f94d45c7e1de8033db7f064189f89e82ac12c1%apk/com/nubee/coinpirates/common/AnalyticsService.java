package com.nubee.coinpirates.common;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsService extends Service
{
  public static final String ACTION = "Google Analytics Service SNS";
  private final String TRACKER = "UA-20792251-8";
  GoogleAnalyticsTracker tracker;

  public AnalyticsService()
  {
  }

  public IBinder onBind(Intent paramIntent)
  {
    return new ActivityServiceBinder();
  }

  public void onCreate()
  {
    super.onCreate();
    this.tracker = GoogleAnalyticsTracker.getInstance();
    this.tracker.start("UA-20792251-8", this);
  }

  public void onDestroy()
  {
    super.onDestroy();
    this.tracker.stop();
  }

  public void onRebind(Intent paramIntent)
  {
  }

  public void onStart(Intent paramIntent, int paramInt)
  {
    super.onStart(paramIntent, paramInt);
  }

  public boolean onUnbind(Intent paramIntent)
  {
    return true;
  }

  public void track(String paramString)
  {
    Log.w(getClass().getSimpleName(), "track:start:" + paramString);
    this.tracker.trackPageView(paramString);
    this.tracker.dispatch();
    Log.w(getClass().getSimpleName(), "track:finish:" + paramString);
  }

  public void trackEvent(String paramString1, String paramString2, String paramString3)
  {
    Log.w(getClass().getSimpleName(), "track:start:" + paramString1 + "," + paramString2 + "," + paramString3);
    this.tracker.trackEvent(paramString1, paramString2, paramString3, 1);
    this.tracker.dispatch();
    Log.w(getClass().getSimpleName(), "track:finish:" + paramString1 + "," + paramString2 + "," + paramString3);
  }

  class ActivityServiceBinder extends Binder
  {
    ActivityServiceBinder()
    {
    }

    AnalyticsService getService()
    {
      return AnalyticsService.this;
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.AnalyticsService
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */