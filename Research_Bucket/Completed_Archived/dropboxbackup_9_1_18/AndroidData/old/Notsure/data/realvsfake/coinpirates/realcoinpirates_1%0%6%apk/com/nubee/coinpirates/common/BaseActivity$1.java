package com.nubee.coinpirates.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

class BaseActivity$1
  implements ServiceConnection
{
  BaseActivity$1(BaseActivity paramBaseActivity)
  {
  }

  public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder)
  {
    this.this$0.activityService = ((AnalyticsService.ActivityServiceBinder)paramIBinder).getService();
    if (this.this$0.mPageName != null)
      this.this$0.activityService.track(this.this$0.mPageName);
  }

  public void onServiceDisconnected(ComponentName paramComponentName)
  {
    this.this$0.activityService = null;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.BaseActivity.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */