package com.google.android.apps.analytics;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHttpRequest;

class NetworkDispatcher$DispatcherThread$AsyncDispatchTask
  implements Runnable
{
  private final LinkedList<Event> events = new LinkedList();

  public NetworkDispatcher$DispatcherThread$AsyncDispatchTask(NetworkDispatcher.DispatcherThread paramDispatcherThread, Event[] paramArrayOfEvent)
  {
    Collections.addAll(this.events, paramArrayOfEvent);
  }

  private void dispatchSomePendingEvents()
    throws IOException, ParseException, HttpException
  {
    int i = 0;
    if ((i < this.events.size()) && (i < NetworkDispatcher.DispatcherThread.access$900(this.this$0)))
    {
      Event localEvent = (Event)this.events.get(i);
      if ("__##GOOGLEPAGEVIEW##__".equals(localEvent.category));
      for (String str = NetworkRequestUtil.constructPageviewRequestPath(localEvent, NetworkDispatcher.DispatcherThread.access$1000(this.this$0)); ; str = NetworkRequestUtil.constructEventRequestPath(localEvent, NetworkDispatcher.DispatcherThread.access$1000(this.this$0)))
      {
        BasicHttpRequest localBasicHttpRequest = new BasicHttpRequest("GET", str);
        localBasicHttpRequest.addHeader("Host", NetworkDispatcher.access$200().getHostName());
        localBasicHttpRequest.addHeader("User-Agent", NetworkDispatcher.DispatcherThread.access$1100(this.this$0));
        NetworkDispatcher.DispatcherThread.access$700(this.this$0).addRequest(localBasicHttpRequest);
        i++;
        break;
      }
    }
    NetworkDispatcher.DispatcherThread.access$700(this.this$0).sendRequests();
  }

  public Event removeNextEvent()
  {
    return (Event)this.events.poll();
  }

  public void run()
  {
    NetworkDispatcher.DispatcherThread.access$402(this.this$0, this);
    i = 0;
    while (true)
      if ((i < 5) && (this.events.size() > 0))
      {
        l = 0L;
        try
        {
          if ((NetworkDispatcher.DispatcherThread.access$500(this.this$0) == 500) || (NetworkDispatcher.DispatcherThread.access$500(this.this$0) == 503))
          {
            l = ()(Math.random() * NetworkDispatcher.DispatcherThread.access$600(this.this$0));
            if (NetworkDispatcher.DispatcherThread.access$600(this.this$0) < 256L)
              NetworkDispatcher.DispatcherThread.access$630(this.this$0, 2L);
          }
          while (true)
          {
            Thread.sleep(l * 1000L);
            dispatchSomePendingEvents();
            i++;
            break;
            NetworkDispatcher.DispatcherThread.access$602(this.this$0, 2L);
          }
        }
        catch (InterruptedException localInterruptedException)
        {
          Log.w("googleanalytics", "Couldn't sleep.", localInterruptedException);
          NetworkDispatcher.DispatcherThread.access$700(this.this$0).finishedCurrentRequests();
          NetworkDispatcher.DispatcherThread.access$800(this.this$0).sendMessage(NetworkDispatcher.DispatcherThread.access$800(this.this$0).obtainMessage(13651479));
          NetworkDispatcher.DispatcherThread.access$402(this.this$0, null);
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

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.NetworkDispatcher.DispatcherThread.AsyncDispatchTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */