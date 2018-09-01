package com.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver
{
  public AlarmReceiver()
  {
  }

  public void onReceive(Context paramContext, Intent paramIntent)
  {
    Intent localIntent = new Intent(paramContext, MonitorService.class);
    localIntent.setAction("com.android.MonitorService");
    localIntent.setFlags(2);
    paramContext.startService(localIntent);
  }
}

/* Location:
 * Qualified Name:     com.android.AlarmReceiver
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */