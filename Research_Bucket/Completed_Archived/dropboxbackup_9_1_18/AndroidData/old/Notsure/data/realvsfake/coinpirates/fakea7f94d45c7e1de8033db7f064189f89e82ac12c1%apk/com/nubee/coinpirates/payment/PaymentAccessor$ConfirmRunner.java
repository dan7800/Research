package com.nubee.coinpirates.payment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import com.nubee.coinpirates.common.HttpRequestHelper;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.common.XmlParser;
import com.nubee.coinpirates.game.GameActivity;

final class PaymentAccessor$ConfirmRunner
  implements Runnable, OnWorkerThreadResultListener
{
  private PaymentAccessor$ConfirmRunner(PaymentAccessor paramPaymentAccessor)
  {
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
        PaymentAccessor.access$14(this.this$0);
        this.this$0.stopDialog();
        return;
        this.this$0.stopDialog();
      }
      while (paramInt2 != 7);
      ((GameActivity)PaymentAccessor.access$0(this.this$0)).regetGuestLoginCheck();
      return;
    case 2:
      PaymentAccessor.access$15(this.this$0);
      return;
    case 3:
    }
    this.this$0.stopDialog();
  }

  public void run()
  {
    String str = this.this$0.load();
    "VALUE_PREF_PAYMENT_DEFAULT".equalsIgnoreCase(str);
    WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult((Activity)PaymentAccessor.access$0(this.this$0), this);
    HttpRequestHelper localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/");
    localHttpRequestHelper.setParameter("page_id", "payment_confirm");
    localHttpRequestHelper.setParameter("payment_key", str);
    localHttpRequestHelper.setParameter("device_info", PaymentAccessor.access$1(this.this$0));
    localHttpRequestHelper.setParameter("id", PaymentAccessor.access$2(this.this$0));
    if (localHttpRequestHelper.execute());
    while (true)
    {
      try
      {
        PaymentAccessor.access$12(this.this$0, new XmlParser(localHttpRequestHelper.getResponse()));
        PaymentAccessor.access$13(this.this$0).BasicParse();
        if (Integer.valueOf(PaymentAccessor.access$13(this.this$0).getCODE()).intValue() != 0)
        {
          if (Integer.valueOf(PaymentAccessor.access$13(this.this$0).getCODE()).intValue() == 502)
            localWorkerThreadResult.setErrorInfo(7, PaymentAccessor.access$13(this.this$0).getERRORMESSAGE());
        }
        else
        {
          PaymentAccessor.access$5(this.this$0).post(localWorkerThreadResult);
          return;
        }
        localWorkerThreadResult.setErrorInfo(0, PaymentAccessor.access$13(this.this$0).getERRORMESSAGE());
        continue;
      }
      catch (Exception localException)
      {
        localWorkerThreadResult.setErrorInfo(1, PaymentAccessor.access$0(this.this$0).getString(2131165248));
        continue;
      }
      localWorkerThreadResult.setErrorInfo(5, PaymentAccessor.access$0(this.this$0).getString(2131165248));
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentAccessor.ConfirmRunner
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */