package com.nubee.coinpirates.game;

import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.common.CommonConfig;
import com.nubee.coinpirates.common.HttpGuestRequestHelper;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.login.GuestRegistXmlParser;

final class GameActivity$GuestRegistThread extends Thread
  implements OnWorkerThreadResultListener
{
  public GameActivity$GuestRegistThread(GameActivity paramGameActivity)
  {
    super("GameActivity.GuestRegistThread");
  }

  public void onWorkerThreadResult(int paramInt1, int paramInt2, String paramString)
  {
    Coins7Log.d("Roulette", "GameActivity." + Thread.currentThread().getName() + ":onWorkerThreadResult");
    switch (paramInt1)
    {
    default:
      return;
    case 0:
      Coins7Log.e("AUTOLOGIN", "10");
      GameActivity.access$4(this.this$0);
      new GameActivity.GuestDailyThread(this.this$0).start();
      return;
    case 1:
      Coins7Log.e("AUTOLOGIN", "11");
      this.this$0.dismissProgress();
      return;
    case 2:
      Coins7Log.e("AUTOLOGIN", "12");
      this.this$0.autoGuestLoginCheck();
      return;
    case 3:
    }
    Coins7Log.e("AUTOLOGIN", "13");
    this.this$0.dismissProgress();
  }

  public void run()
  {
    Coins7Log.e("AUTOLOGIN", "7");
    String str = ((TelephonyManager)this.this$0.getSystemService("phone")).getDeviceId();
    WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult(this.this$0, this);
    if (GameActivity.access$0(this.this$0) != null)
    {
      Coins7Log.e("AUTOLOGIN", "7-SKIP_REQUEST");
      GameActivity.access$1(this.this$0).post(localWorkerThreadResult);
      return;
    }
    HttpGuestRequestHelper localHttpGuestRequestHelper = new HttpGuestRequestHelper("https://appli.nubee.com/index.php", str);
    localHttpGuestRequestHelper.setParameter("page_id", "guest_regist");
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    this.this$0.getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    localHttpGuestRequestHelper.addDiaplaySize(String.valueOf(localDisplayMetrics.widthPixels) + "x" + localDisplayMetrics.heightPixels);
    localHttpGuestRequestHelper.addModel();
    localHttpGuestRequestHelper.addOsVersion();
    if (localHttpGuestRequestHelper.execute())
      Coins7Log.e("AUTOLOGIN", "8");
    while (true)
    {
      try
      {
        GameActivity.access$2(this.this$0, new GuestRegistXmlParser(localHttpGuestRequestHelper.getResponse()));
        GameActivity.access$3(this.this$0).BasicParse();
        if (CommonConfig.getStringToInt(String.valueOf(GameActivity.access$3(this.this$0).getCODE())) != 0)
        {
          localWorkerThreadResult.setErrorInfo(0, GameActivity.access$3(this.this$0).getERRORMESSAGE());
          GameActivity.access$1(this.this$0).post(localWorkerThreadResult);
          return;
        }
        GameActivity.access$3(this.this$0).parse();
        continue;
      }
      catch (Exception localException)
      {
        localWorkerThreadResult.setErrorInfo(1, this.this$0.getString(2131165227));
        continue;
      }
      Coins7Log.e("AUTOLOGIN", "9");
      localWorkerThreadResult.setErrorInfo(5, this.this$0.getString(2131165227));
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.GuestRegistThread
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */