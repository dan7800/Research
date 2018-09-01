package com.google.android.apps.analytics;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHttpRequest;

class NetworkDispatcher$DispatcherThread extends HandlerThread
{
  private AsyncDispatchTask currentTask = null;
  private Handler handlerExecuteOnDispatcherThread;
  private int lastStatusCode;
  private int maxEventsPerRequest = 30;
  private final Handler messageHandler;
  private final PipelinedRequester pipelinedRequester;
  private final String referrer;
  private long retryInterval;
  private final String userAgent;

  private NetworkDispatcher$DispatcherThread(Handler paramHandler, PipelinedRequester paramPipelinedRequester, String paramString1, String paramString2)
  {
    super("DispatcherThread");
    this.messageHandler = paramHandler;
    this.referrer = paramString1;
    this.userAgent = paramString2;
    this.pipelinedRequester = paramPipelinedRequester;
    this.pipelinedRequester.installCallbacks(new RequesterCallbacks(null));
  }

  private NetworkDispatcher$DispatcherThread(Handler paramHandler, String paramString1, String paramString2)
  {
    this(paramHandler, new PipelinedRequester(NetworkDispatcher.access$200()), paramString1, paramString2);
  }

  public void dispatchEvents(Event[] paramArrayOfEvent)
  {
    if (this.handlerExecuteOnDispatcherThread != null)
      this.handlerExecuteOnDispatcherThread.post(new AsyncDispatchTask(paramArrayOfEvent));
  }

  protected void onLooperPrepared()
  {
    this.handlerExecuteOnDispatcherThread = new Handler();
  }

  private class AsyncDispatchTask
    implements Runnable
  {
    private final LinkedList<Event> events = new LinkedList();

    public AsyncDispatchTask(Event[] arg2)
    {
      Object[] arrayOfObject;
      Collections.addAll(this.events, arrayOfObject);
    }

    private void dispatchSomePendingEvents()
      throws IOException, ParseException, HttpException
    {
      int i = 0;
      if ((i < this.events.size()) && (i < NetworkDispatcher.DispatcherThread.this.maxEventsPerRequest))
      {
        Event localEvent = (Event)this.events.get(i);
        if ("__##GOOGLEPAGEVIEW##__".equals(localEvent.category));
        for (String str = NetworkRequestUtil.constructPageviewRequestPath(localEvent, NetworkDispatcher.DispatcherThread.this.referrer); ; str = NetworkRequestUtil.constructEventRequestPath(localEvent, NetworkDispatcher.DispatcherThread.this.referrer))
        {
          BasicHttpRequest localBasicHttpRequest = new BasicHttpRequest("GET", str);
          localBasicHttpRequest.addHeader("Host", NetworkDispatcher.access$200().getHostName());
          localBasicHttpRequest.addHeader("User-Agent", NetworkDispatcher.DispatcherThread.this.userAgent);
          NetworkDispatcher.DispatcherThread.this.pipelinedRequester.addRequest(localBasicHttpRequest);
          i++;
          break;
        }
      }
      NetworkDispatcher.DispatcherThread.this.pipelinedRequester.sendRequests();
    }

    public Event removeNextEvent()
    {
      return (Event)this.events.poll();
    }

    public void run()
    {
      NetworkDispatcher.DispatcherThread.access$402(NetworkDispatcher.DispatcherThread.this, this);
      i = 0;
      while (true)
        if ((i < 5) && (this.events.size() > 0))
        {
          l = 0L;
          try
          {
            if ((NetworkDispatcher.DispatcherThread.this.lastStatusCode == 500) || (NetworkDispatcher.DispatcherThread.this.lastStatusCode == 503))
            {
              l = ()(Math.random() * NetworkDispatcher.DispatcherThread.this.retryInterval);
              if (NetworkDispatcher.DispatcherThread.this.retryInterval < 256L)
                NetworkDispatcher.DispatcherThread.access$630(NetworkDispatcher.DispatcherThread.this, 2L);
            }
            while (true)
            {
              Thread.sleep(l * 1000L);
              dispatchSomePendingEvents();
              i++;
              break;
              NetworkDispatcher.DispatcherThread.access$602(NetworkDispatcher.DispatcherThread.this, 2L);
            }
          }
          catch (InterruptedException localInterruptedException)
          {
            Log.w("googleanalytics", "Couldn't sleep.", localInterruptedException);
            NetworkDispatcher.DispatcherThread.this.pipelinedRequester.finishedCurrentRequests();
            NetworkDispatcher.DispatcherThread.this.messageHandler.sendMessage(NetworkDispatcher.DispatcherThread.this.messageHandler.obtainMessage(13651479));
            NetworkDispatcher.DispatcherThread.access$402(NetworkDispatcher.DispatcherThread.this, null);
            return;
          }
          catch (IOException localIOException)
          {
            while (true)
              Log.w("googleanalytics", "Problem with socket or streams.", localIOException);
          }
          catch (HttpException localHttpException)
          {
            while (true)
              Log.w("googleanalytics", "Problem with http streams.", localHttpException);
          }
        }
    }
  }

  private class RequesterCallbacks
    implements PipelinedRequester.Callbacks
  {
    private RequesterCallbacks()
    {
    }

    public void pipelineModeChanged(boolean paramBoolean)
    {
      if (paramBoolean)
      {
        NetworkDispatcher.DispatcherThread.access$902(NetworkDispatcher.DispatcherThread.this, 30);
        return;
      }
      NetworkDispatcher.DispatcherThread.access$902(NetworkDispatcher.DispatcherThread.this, 1);
    }

    public void requestSent()
    {
      if (NetworkDispatcher.DispatcherThread.this.currentTask == null);
      Event localEvent;
      do
      {
        return;
        localEvent = NetworkDispatcher.DispatcherThread.this.currentTask.removeNextEvent();
      }
      while (localEvent == null);
      long l = localEvent.eventId;
      NetworkDispatcher.DispatcherThread.this.messageHandler.sendMessage(NetworkDispatcher.DispatcherThread.this.messageHandler.obtainMessage(6178583, new Long(l)));
    }

    public void serverError(int paramInt)
    {
      NetworkDispatcher.DispatcherThread.access$502(NetworkDispatcher.DispatcherThread.this, paramInt);
    }
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.NetworkDispatcher.DispatcherThread
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */