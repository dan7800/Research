package com.google.android.apps.analytics;

import android.os.Handler;

class NetworkDispatcher$DispatcherThread$RequesterCallbacks
  implements PipelinedRequester.Callbacks
{
  private NetworkDispatcher$DispatcherThread$RequesterCallbacks(NetworkDispatcher.DispatcherThread paramDispatcherThread)
  {
  }

  public void pipelineModeChanged(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      NetworkDispatcher.DispatcherThread.access$902(this.this$0, 30);
      return;
    }
    NetworkDispatcher.DispatcherThread.access$902(this.this$0, 1);
  }

  public void requestSent()
  {
    if (NetworkDispatcher.DispatcherThread.access$400(this.this$0) == null);
    Event localEvent;
    do
    {
      return;
      localEvent = NetworkDispatcher.DispatcherThread.access$400(this.this$0).removeNextEvent();
    }
    while (localEvent == null);
    long l = localEvent.eventId;
    NetworkDispatcher.DispatcherThread.access$800(this.this$0).sendMessage(NetworkDispatcher.DispatcherThread.access$800(this.this$0).obtainMessage(6178583, new Long(l)));
  }

  public void serverError(int paramInt)
  {
    NetworkDispatcher.DispatcherThread.access$502(this.this$0, paramInt);
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.NetworkDispatcher.DispatcherThread.RequesterCallbacks
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */