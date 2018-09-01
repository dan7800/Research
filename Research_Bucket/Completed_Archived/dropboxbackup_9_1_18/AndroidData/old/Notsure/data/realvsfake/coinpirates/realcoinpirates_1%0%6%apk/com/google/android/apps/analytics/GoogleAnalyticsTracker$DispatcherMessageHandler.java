package com.google.android.apps.analytics;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class GoogleAnalyticsTracker$DispatcherMessageHandler extends Handler
{
  public GoogleAnalyticsTracker$DispatcherMessageHandler(GoogleAnalyticsTracker paramGoogleAnalyticsTracker, Looper paramLooper)
  {
    super(paramLooper);
  }

  public void handleMessage(Message paramMessage)
  {
    if (paramMessage.what == 13651479)
      this.this$0.dispatchFinished();
    while (paramMessage.what != 6178583)
      return;
    this.this$0.eventDispatched(((Long)paramMessage.obj).longValue());
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.GoogleAnalyticsTracker.DispatcherMessageHandler
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */