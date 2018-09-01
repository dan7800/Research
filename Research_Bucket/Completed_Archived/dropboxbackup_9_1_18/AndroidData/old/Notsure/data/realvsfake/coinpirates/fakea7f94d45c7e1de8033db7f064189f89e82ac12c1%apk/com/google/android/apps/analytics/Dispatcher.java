package com.google.android.apps.analytics;

import android.os.Handler;

abstract interface Dispatcher
{
  public static final int MSG_DISPATCHED_FINISHED = 13651479;
  public static final int MSG_EVENT_DISPATCHED = 6178583;

  public abstract void dispatchEvents(Event[] paramArrayOfEvent);

  public abstract void init(Handler paramHandler, String paramString);

  public abstract void stop();
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.Dispatcher
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */