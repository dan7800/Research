package com.google.android.apps.analytics;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

public class GoogleAnalyticsTracker
{
  static final boolean DEBUG = false;
  private static final GoogleAnalyticsTracker INSTANCE = new GoogleAnalyticsTracker();
  public static final String TRACKER_TAG = "googleanalytics";
  public static final String VERSION = "1.0";
  private String accountId;
  private ConnectivityManager connetivityManager;
  private int dispatchPeriod;
  private Runnable dispatchRunner = new Runnable()
  {
    public void run()
    {
      GoogleAnalyticsTracker.this.dispatch();
    }
  };
  private Dispatcher dispatcher;
  private boolean dispatcherIsBusy;
  private EventStore eventStore;
  private int eventsBeingDispatched;
  private int eventsDispatched;
  private Handler handler;
  private Context parent;
  private boolean powerSaveMode;

  private GoogleAnalyticsTracker()
  {
  }

  private void cancelPendingDispathes()
  {
    this.handler.removeCallbacks(this.dispatchRunner);
  }

  private void createEvent(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
  {
    Event localEvent = new Event(this.eventStore.getStoreId(), paramString1, paramString2, paramString3, paramString4, paramInt, this.parent.getResources().getDisplayMetrics().widthPixels, this.parent.getResources().getDisplayMetrics().heightPixels);
    this.eventStore.putEvent(localEvent);
    resetPowerSaveMode();
  }

  public static GoogleAnalyticsTracker getInstance()
  {
    return INSTANCE;
  }

  private void maybeScheduleNextDispatch()
  {
    if (this.dispatchPeriod < 0);
    while (!this.handler.postDelayed(this.dispatchRunner, 1000 * this.dispatchPeriod))
      return;
  }

  private void resetPowerSaveMode()
  {
    if (this.powerSaveMode)
    {
      this.powerSaveMode = false;
      maybeScheduleNextDispatch();
    }
  }

  public boolean dispatch()
  {
    if (this.dispatcherIsBusy)
    {
      maybeScheduleNextDispatch();
      return false;
    }
    NetworkInfo localNetworkInfo = this.connetivityManager.getActiveNetworkInfo();
    if ((localNetworkInfo == null) || (!localNetworkInfo.isAvailable()))
    {
      maybeScheduleNextDispatch();
      return false;
    }
    this.eventsBeingDispatched = this.eventStore.getNumStoredEvents();
    if (this.eventsBeingDispatched != 0)
    {
      this.eventsDispatched = 0;
      Event[] arrayOfEvent = this.eventStore.peekEvents();
      this.dispatcher.dispatchEvents(arrayOfEvent);
      this.dispatcherIsBusy = true;
      maybeScheduleNextDispatch();
      return true;
    }
    this.powerSaveMode = true;
    return false;
  }

  void dispatchFinished()
  {
    int i = this.eventsBeingDispatched - this.eventsDispatched;
    if (i != 0)
      Log.w("googleanalytics", "Dispatcher thinks it finished, but there were " + i + " failed events");
    this.eventsBeingDispatched = 0;
    this.dispatcherIsBusy = false;
  }

  void eventDispatched(long paramLong)
  {
    this.eventStore.deleteEvent(paramLong);
    this.eventsDispatched = (1 + this.eventsDispatched);
  }

  public void setDispatchPeriod(int paramInt)
  {
    int i = this.dispatchPeriod;
    this.dispatchPeriod = paramInt;
    if (i <= 0)
      maybeScheduleNextDispatch();
    while (i <= 0)
      return;
    cancelPendingDispathes();
    maybeScheduleNextDispatch();
  }

  public void start(String paramString, int paramInt, Context paramContext)
  {
    Object localObject1;
    if (this.eventStore == null)
    {
      localObject1 = new PersistentEventStore(paramContext);
      if (this.dispatcher != null)
        break label54;
    }
    label54: for (Object localObject2 = new NetworkDispatcher(); ; localObject2 = this.dispatcher)
    {
      start(paramString, paramInt, paramContext, (EventStore)localObject1, (Dispatcher)localObject2);
      return;
      localObject1 = this.eventStore;
      break;
    }
  }

  void start(String paramString, int paramInt, Context paramContext, EventStore paramEventStore, Dispatcher paramDispatcher)
  {
    this.accountId = paramString;
    this.parent = paramContext;
    this.eventStore = paramEventStore;
    this.eventStore.startNewVisit();
    this.dispatcher = paramDispatcher;
    this.dispatcher.init(new DispatcherMessageHandler(this.parent.getMainLooper()), this.eventStore.getReferrer());
    this.eventsBeingDispatched = 0;
    this.dispatcherIsBusy = false;
    if (this.connetivityManager == null)
      this.connetivityManager = ((ConnectivityManager)this.parent.getSystemService("connectivity"));
    if (this.handler == null)
      this.handler = new Handler(paramContext.getMainLooper());
    while (true)
    {
      setDispatchPeriod(paramInt);
      return;
      cancelPendingDispathes();
    }
  }

  public void start(String paramString, Context paramContext)
  {
    start(paramString, -1, paramContext);
  }

  public void stop()
  {
    this.dispatcher.stop();
    cancelPendingDispathes();
  }

  public void trackEvent(String paramString1, String paramString2, String paramString3, int paramInt)
  {
    createEvent(this.accountId, paramString1, paramString2, paramString3, paramInt);
  }

  public void trackPageView(String paramString)
  {
    createEvent(this.accountId, "__##GOOGLEPAGEVIEW##__", paramString, null, -1);
  }

  private class DispatcherMessageHandler extends Handler
  {
    public DispatcherMessageHandler(Looper arg2)
    {
      super();
    }

    public void handleMessage(Message paramMessage)
    {
      if (paramMessage.what == 13651479)
        GoogleAnalyticsTracker.this.dispatchFinished();
      while (paramMessage.what != 6178583)
        return;
      GoogleAnalyticsTracker.this.eventDispatched(((Long)paramMessage.obj).longValue());
    }
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.GoogleAnalyticsTracker
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */