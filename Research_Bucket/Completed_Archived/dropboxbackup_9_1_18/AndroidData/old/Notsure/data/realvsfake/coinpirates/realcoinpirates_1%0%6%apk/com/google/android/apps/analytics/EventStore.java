package com.google.android.apps.analytics;

abstract interface EventStore
{
  public abstract void deleteEvent(long paramLong);

  public abstract int getNumStoredEvents();

  public abstract String getReferrer();

  public abstract int getStoreId();

  public abstract Event[] peekEvents();

  public abstract Event[] peekEvents(int paramInt);

  public abstract void putEvent(Event paramEvent);

  public abstract void setReferrer(String paramString);

  public abstract void startNewVisit();
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.EventStore
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */