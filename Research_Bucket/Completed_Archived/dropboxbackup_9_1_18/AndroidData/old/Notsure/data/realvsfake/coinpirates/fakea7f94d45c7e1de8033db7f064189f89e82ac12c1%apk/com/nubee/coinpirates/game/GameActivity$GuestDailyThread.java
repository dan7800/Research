package com.nubee.coinpirates.game;

import android.os.Handler;
import android.telephony.TelephonyManager;
import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.login.GuestLogin;

final class GameActivity$GuestDailyThread extends Thread
  implements OnWorkerThreadResultListener
{
  public GameActivity$GuestDailyThread(GameActivity paramGameActivity)
  {
    super("GameActivity.GuestDailyThread");
  }

  public void onWorkerThreadResult(int paramInt1, int paramInt2, String paramString)
  {
    Coins7Log.d("Roulette", "GameActivity." + Thread.currentThread().getName() + ":onWorkerThreadResult");
    switch (paramInt1)
    {
    default:
    case 0:
    case 1:
    case 2:
    case 3:
    }
    while (true)
    {
      GameActivity.setPause(false);
      return;
      try
      {
        Coins7Log.e("AUTOLOGIN", "17");
        this.this$0.dismissProgress();
        GameActivity.access$5(this.this$0);
        continue;
      }
      finally
      {
        GameActivity.setPause(false);
      }
      Coins7Log.e("AUTOLOGIN", "18");
      this.this$0.dismissProgress();
      continue;
      Coins7Log.e("AUTOLOGIN", "19");
      new GuestDailyThread(this.this$0).start();
      continue;
      Coins7Log.e("AUTOLOGIN", "20");
      this.this$0.dismissProgress();
    }
  }

  public void run()
  {
    Coins7Log.e("AUTOLOGIN", "14");
    String str1 = ((TelephonyManager)this.this$0.getSystemService("phone")).getDeviceId();
    WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult(this.this$0, this);
    String str2 = GameActivity.access$0(this.this$0);
    if (str2 == null)
    {
      Coins7Log.d("Roulette", "Guest Data is null, skip DailyAccess.");
      GameActivity.access$1(this.this$0).post(localWorkerThreadResult);
      return;
    }
    GuestLogin.login(this.this$0, localWorkerThreadResult, str2, str1, "6");
    GameActivity.access$1(this.this$0).post(localWorkerThreadResult);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.GuestDailyThread
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */