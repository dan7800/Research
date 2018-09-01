package com.nubee.coinpirates.payment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import com.nubee.coinpirates.common.HttpRequestHelper;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.game.GameActivity;
import com.nubee.coinpirates.login.GuestLogin;

final class PaymentAccessor$PaymentRunner
  implements Runnable, OnWorkerThreadResultListener
{
  int shopType;

  public PaymentAccessor$PaymentRunner(PaymentAccessor paramPaymentAccessor, int paramInt)
  {
    this.shopType = paramInt;
  }

  public void onWorkerThreadResult(int paramInt1, int paramInt2, String paramString)
  {
    switch (paramInt1)
    {
    default:
    case 0:
    case 1:
      do
      {
        return;
        this.this$0.stopDialog();
        PaymentAccessor.access$6(this.this$0);
        return;
        this.this$0.stopDialog();
      }
      while (paramInt2 != 7);
      ((GameActivity)PaymentAccessor.access$0(this.this$0)).regetGuestLoginCheck();
      return;
    case 2:
      PaymentAccessor.access$7(this.this$0, this.shopType);
      return;
    case 3:
    }
    this.this$0.stopDialog();
  }

  public void run()
  {
    WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult((Activity)PaymentAccessor.access$0(this.this$0), this);
    HttpRequestHelper localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/");
    localHttpRequestHelper.setParameter("page_id", "payment_url");
    localHttpRequestHelper.setParameter("device_info", PaymentAccessor.access$1(this.this$0));
    localHttpRequestHelper.setParameter("id", PaymentAccessor.access$2(this.this$0));
    localHttpRequestHelper.setParameter("item_type", String.valueOf(this.shopType));
    GuestLogin.setDefaultHttpRequest(PaymentAccessor.access$0(this.this$0), localHttpRequestHelper, "6");
    if (localHttpRequestHelper.execute());
    while (true)
    {
      try
      {
        PaymentAccessor.access$3(this.this$0, new PaymentUrlXmlParser(localHttpRequestHelper.getResponse()));
        PaymentAccessor.access$4(this.this$0).BasicParse();
        if (Integer.valueOf(PaymentAccessor.access$4(this.this$0).getCODE()).intValue() != 0)
        {
          if (Integer.valueOf(PaymentAccessor.access$4(this.this$0).getCODE()).intValue() == 502)
          {
            localWorkerThreadResult.setErrorInfo(7, PaymentAccessor.access$4(this.this$0).getERRORMESSAGE());
            PaymentAccessor.access$5(this.this$0).post(localWorkerThreadResult);
            return;
          }
          localWorkerThreadResult.setErrorInfo(0, PaymentAccessor.access$4(this.this$0).getERRORMESSAGE());
          continue;
        }
      }
      catch (Exception localException)
      {
        localWorkerThreadResult.setErrorInfo(1, PaymentAccessor.access$0(this.this$0).getString(2131165248));
        continue;
        PaymentAccessor.access$4(this.this$0).parse();
        continue;
      }
      localWorkerThreadResult.setErrorInfo(5, PaymentAccessor.access$0(this.this$0).getString(2131165248));
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentAccessor.PaymentRunner
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */