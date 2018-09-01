package com.nubee.coinpirates.common;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import com.nubee.coinpirates.game.GameActivity;

public abstract class BaseActivity extends Activity
{
  protected AnalyticsService activityService;
  protected Handler mHandler = new Handler();
  protected String mPageName;
  protected ProgressDialogEx mProgressDialog;
  private final ActivityServiceReceiver receiver = new ActivityServiceReceiver(null);
  private ServiceConnection serviceConnection = new ServiceConnection()
  {
    public void onServiceConnected(ComponentName paramAnonymousComponentName, IBinder paramAnonymousIBinder)
    {
      BaseActivity.this.activityService = ((AnalyticsService.ActivityServiceBinder)paramAnonymousIBinder).getService();
      if (BaseActivity.this.mPageName != null)
        BaseActivity.this.activityService.track(BaseActivity.this.mPageName);
    }

    public void onServiceDisconnected(ComponentName paramAnonymousComponentName)
    {
      BaseActivity.this.activityService = null;
    }
  };

  public BaseActivity()
  {
  }

  public void alertDialog(int paramInt1, int paramInt2, DialogInterface.OnClickListener paramOnClickListener)
  {
    try
    {
      new AlertDialog.Builder(this).setTitle(paramInt1).setMessage(paramInt2).setPositiveButton(2131165186, paramOnClickListener).show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public void alertDialog(String paramString1, String paramString2, DialogInterface.OnClickListener paramOnClickListener)
  {
    try
    {
      new AlertDialog.Builder(this).setTitle(paramString1).setMessage(paramString2).setPositiveButton(2131165186, paramOnClickListener).show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  protected abstract void backActivity();

  public void confirmDialog(int paramInt1, int paramInt2, DialogInterface.OnClickListener paramOnClickListener1, DialogInterface.OnClickListener paramOnClickListener2)
  {
    try
    {
      new AlertDialog.Builder(this).setTitle(paramInt1).setMessage(paramInt2).setPositiveButton(2131165186, paramOnClickListener1).setNegativeButton(2131165187, paramOnClickListener2).show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public void confirmDialog(String paramString1, String paramString2, DialogInterface.OnClickListener paramOnClickListener1, DialogInterface.OnClickListener paramOnClickListener2)
  {
    try
    {
      new AlertDialog.Builder(this).setTitle(paramString1).setMessage(paramString2).setPositiveButton(2131165186, paramOnClickListener1).setNegativeButton(2131165187, paramOnClickListener2).show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public void confirmFinish(int paramInt1, int paramInt2)
  {
    confirmDialog(paramInt1, paramInt2, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        BaseActivity.this.finish();
      }
    }
    , null);
  }

  public void dismissProgress()
  {
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        if (BaseActivity.this.mProgressDialog != null)
        {
          BaseActivity.this.mProgressDialog.dismiss();
          GameActivity.setPause(false);
        }
      }
    });
  }

  public void onCreate(Bundle paramBundle, String paramString)
  {
    this.mPageName = paramString;
    Coins7Log.d(this.mPageName, "onCreate");
    super.onCreate(paramBundle);
    Intent localIntent = new Intent(this, AnalyticsService.class);
    startService(localIntent);
    IntentFilter localIntentFilter = new IntentFilter("Google Analytics Service SNS");
    registerReceiver(this.receiver, localIntentFilter);
    bindService(localIntent, this.serviceConnection, 1);
    requestWindowFeature(1);
    getWindow().addFlags(1024);
    getWindow().addFlags(128);
  }

  protected void onDestroy()
  {
    Coins7Log.d(this.mPageName, "onDestroy");
    unbindService(this.serviceConnection);
    unregisterReceiver(this.receiver);
    if (this.activityService != null)
      this.activityService.stopSelf();
    super.onDestroy();
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if ((paramInt == 3) || (paramInt == 4))
    {
      backActivity();
      return true;
    }
    return false;
  }

  protected void onPause()
  {
    Coins7Log.d(this.mPageName, "onPause");
    super.onPause();
  }

  protected void onRestart()
  {
    Coins7Log.d(this.mPageName, "onRestart");
    super.onResume();
  }

  protected void onResume()
  {
    Coins7Log.d(this.mPageName, "onResume");
    super.onResume();
  }

  protected void onStart()
  {
    Coins7Log.d(this.mPageName, "onStart");
    super.onResume();
  }

  protected void onStop()
  {
    Coins7Log.d(this.mPageName, "onStop");
    super.onStop();
  }

  public void startProgress()
  {
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        if ((BaseActivity.this.mProgressDialog == null) || (!BaseActivity.this.mProgressDialog.isShowing()))
        {
          BaseActivity.this.mProgressDialog = ProgressDialogEx.show(BaseActivity.this, BaseActivity.this.getString(2131165192), BaseActivity.this.getString(2131165193));
          GameActivity.setPause(true);
        }
      }
    });
  }

  private class ActivityServiceReceiver extends BroadcastReceiver
  {
    private ActivityServiceReceiver()
    {
    }

    public void onReceive(Context paramContext, Intent paramIntent)
    {
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.BaseActivity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */