package com.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver
{
  private static final String LOG_TAG = "MyMonitor";

  public BootReceiver()
  {
  }

  public void onReceive(Context paramContext, Intent paramIntent)
  {
    Log.d("MyMonitor", "MyBootReceiver.onReceive");
    Intent localIntent = new Intent();
    localIntent.setAction("com.android.MonitorService");
    paramContext.startService(localIntent);
  }
}

/* Location:
 * Qualified Name:     com.android.BootReceiver
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */