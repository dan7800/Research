package com.nubee.coinpirates.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Handler;
import com.nubee.coinpirates.common.CommonConfig;
import com.nubee.coinpirates.common.HttpRequestHelper;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.common.XmlParser;
import com.nubee.coinpirates.game.GameActivity;
import com.nubee.coinpirates.game.GameRenderer;
import com.nubee.coinpirates.login.GuestLogin;

public final class PaymentAccessor
{
  public static final int CODE_PAYMENT_ALREADY_CHARGED = 418;
  public static final int CODE_PAYMENT_NOT_REGISTERED = 417;
  public static final int SHOP_TYPE_COIN = 1;
  public static final int SHOP_TYPE_WALL = 2;
  private Callback callback;
  private XmlParser confirmParser;
  private final transient Context context;
  private final transient String deviceId;
  private Handler handler;
  private PaymentInfoXmlParser infoParser;
  private final transient String memberId;
  private Thread requestThread;
  private PaymentUrlXmlParser urlParser;

  public PaymentAccessor(Context paramContext, String paramString1, String paramString2)
  {
    this.context = paramContext;
    this.memberId = paramString1;
    this.deviceId = paramString2;
  }

  private void callbackConfirm()
  {
    if (!clear())
      CommonConfig.showDialogForNothing((Activity)this.context, this.context.getString(2131165248));
    while (this.callback == null)
      return;
    CommonConfig.showDialogForNothing((Activity)this.context, this.callback.completeMessage(this.infoParser.getResult()));
  }

  private void callbackInfo()
  {
    switch (this.infoParser.getCODE())
    {
    case 418:
    default:
    case 417:
    }
    while (true)
    {
      if ((this.callback != null) && (this.callback.callback(this.infoParser.getResult())))
        getConfirm();
      return;
      clear();
    }
  }

  private void callbackPayment()
  {
    PaymentURLEntity localPaymentURLEntity = this.urlParser.getResult();
    if (save(localPaymentURLEntity.getPaymentKey()))
    {
      Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse(localPaymentURLEntity.getPaymentUrl()));
      localIntent.addFlags(1073741824);
      ((Activity)this.context).startActivity(localIntent);
      ((Activity)this.context).finish();
      GameRenderer.destroy();
      return;
    }
    CommonConfig.showDialogForNothing((Activity)this.context, this.context.getString(2131165248));
  }

  private void getConfirm()
  {
    warmUp();
    this.requestThread = new Thread(new ConfirmRunner(null), "GetConfirm");
    this.requestThread.start();
  }

  private void getInfo()
  {
    warmUp();
    this.requestThread = new Thread(new InfomationRunner(null), "GetInfo");
    this.requestThread.start();
  }

  private void getPayment(int paramInt)
  {
    warmUp();
    this.requestThread = new Thread(new PaymentRunner(paramInt), "GetUrl");
    this.requestThread.start();
  }

  private void warmUp()
  {
    if (this.handler == null)
      this.handler = new Handler();
    startDialog("", "");
    if ((this.requestThread != null) && (this.requestThread.isAlive()));
    try
    {
      this.requestThread.join();
      this.requestThread = null;
      return;
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  public boolean clear()
  {
    return this.context.getSharedPreferences("KEY_PREF_PAYMENT", 0).edit().clear().commit();
  }

  public void executePayments(int paramInt)
  {
    getPayment(paramInt);
  }

  public void executeReceive()
  {
    getInfo();
  }

  public boolean hasPaymentKey()
  {
    return !"VALUE_PREF_PAYMENT_DEFAULT".equalsIgnoreCase(this.context.getSharedPreferences("KEY_PREF_PAYMENT", 0).getString("KEY_PAYMENT", "VALUE_PREF_PAYMENT_DEFAULT"));
  }

  public String load()
  {
    return this.context.getSharedPreferences("KEY_PREF_PAYMENT", 0).getString("KEY_PAYMENT", "VALUE_PREF_PAYMENT_DEFAULT");
  }

  public boolean save(String paramString)
  {
    return this.context.getSharedPreferences("KEY_PREF_PAYMENT", 0).edit().putString("KEY_PAYMENT", paramString).commit();
  }

  public void setCallback(Callback paramCallback)
  {
    this.callback = paramCallback;
  }

  protected void startDialog(CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
  }

  protected void stopDialog()
  {
  }

  public static abstract interface Callback
  {
    public abstract boolean callback(PaymentInfoEntity paramPaymentInfoEntity);

    public abstract String completeMessage(PaymentInfoEntity paramPaymentInfoEntity);
  }

  private final class ConfirmRunner
    implements Runnable, OnWorkerThreadResultListener
  {
    private ConfirmRunner()
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
          PaymentAccessor.this.callbackConfirm();
          PaymentAccessor.this.stopDialog();
          return;
          PaymentAccessor.this.stopDialog();
        }
        while (paramInt2 != 7);
        ((GameActivity)PaymentAccessor.this.context).regetGuestLoginCheck();
        return;
      case 2:
        PaymentAccessor.this.getConfirm();
        return;
      case 3:
      }
      PaymentAccessor.this.stopDialog();
    }

    public void run()
    {
      String str = PaymentAccessor.this.load();
      "VALUE_PREF_PAYMENT_DEFAULT".equalsIgnoreCase(str);
      WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult((Activity)PaymentAccessor.this.context, this);
      HttpRequestHelper localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/");
      localHttpRequestHelper.setParameter("page_id", "payment_confirm");
      localHttpRequestHelper.setParameter("payment_key", str);
      localHttpRequestHelper.setParameter("device_info", PaymentAccessor.this.deviceId);
      localHttpRequestHelper.setParameter("id", PaymentAccessor.this.memberId);
      if (localHttpRequestHelper.execute());
      while (true)
      {
        try
        {
          PaymentAccessor.this.confirmParser = new XmlParser(localHttpRequestHelper.getResponse());
          PaymentAccessor.this.confirmParser.BasicParse();
          if (Integer.valueOf(PaymentAccessor.this.confirmParser.getCODE()).intValue() != 0)
          {
            if (Integer.valueOf(PaymentAccessor.this.confirmParser.getCODE()).intValue() == 502)
              localWorkerThreadResult.setErrorInfo(7, PaymentAccessor.this.confirmParser.getERRORMESSAGE());
          }
          else
          {
            PaymentAccessor.this.handler.post(localWorkerThreadResult);
            return;
          }
          localWorkerThreadResult.setErrorInfo(0, PaymentAccessor.this.confirmParser.getERRORMESSAGE());
          continue;
        }
        catch (Exception localException)
        {
          localWorkerThreadResult.setErrorInfo(1, PaymentAccessor.this.context.getString(2131165248));
          continue;
        }
        localWorkerThreadResult.setErrorInfo(5, PaymentAccessor.this.context.getString(2131165248));
      }
    }
  }

  private final class InfomationRunner
    implements Runnable, OnWorkerThreadResultListener
  {
    private InfomationRunner()
    {
    }

    public void onWorkerThreadResult(int paramInt1, int paramInt2, String paramString)
    {
      switch (paramInt1)
      {
      default:
        return;
      case 0:
        PaymentAccessor.this.callbackInfo();
        PaymentAccessor.this.stopDialog();
        return;
      case 1:
        if (paramInt2 == 7)
        {
          PaymentAccessor.this.stopDialog();
          ((GameActivity)PaymentAccessor.this.context).regetGuestLoginCheck();
          return;
        }
        PaymentAccessor.this.callbackInfo();
        PaymentAccessor.this.stopDialog();
        return;
      case 2:
        PaymentAccessor.this.getInfo();
        return;
      case 3:
      }
      PaymentAccessor.this.stopDialog();
    }

    public void run()
    {
      String str = PaymentAccessor.this.load();
      "VALUE_PREF_PAYMENT_DEFAULT".equalsIgnoreCase(str);
      WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult((Activity)PaymentAccessor.this.context, this);
      HttpRequestHelper localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/");
      localHttpRequestHelper.setParameter("page_id", "payment_info");
      localHttpRequestHelper.setParameter("payment_key", str);
      localHttpRequestHelper.setParameter("device_info", PaymentAccessor.this.deviceId);
      localHttpRequestHelper.setParameter("id", PaymentAccessor.this.memberId);
      if (localHttpRequestHelper.execute());
      while (true)
      {
        try
        {
          PaymentAccessor.this.infoParser = new PaymentInfoXmlParser(localHttpRequestHelper.getResponse());
          PaymentAccessor.this.infoParser.BasicParse();
          if (Integer.valueOf(PaymentAccessor.this.infoParser.getCODE()).intValue() != 0)
          {
            if (Integer.valueOf(PaymentAccessor.this.infoParser.getCODE()).intValue() == 502)
            {
              localWorkerThreadResult.setErrorInfo(7, PaymentAccessor.this.infoParser.getERRORMESSAGE());
              PaymentAccessor.this.handler.post(localWorkerThreadResult);
              return;
            }
            localWorkerThreadResult.setErrorInfo(0, PaymentAccessor.this.infoParser.getERRORMESSAGE());
            continue;
          }
        }
        catch (Exception localException)
        {
          localWorkerThreadResult.setErrorInfo(1, PaymentAccessor.this.context.getString(2131165248));
          continue;
          PaymentAccessor.this.infoParser.parse();
          continue;
        }
        localWorkerThreadResult.setErrorInfo(5, PaymentAccessor.this.context.getString(2131165248));
      }
    }
  }

  private final class PaymentRunner
    implements Runnable, OnWorkerThreadResultListener
  {
    int shopType;

    public PaymentRunner(int arg2)
    {
      int i;
      this.shopType = i;
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
          PaymentAccessor.this.stopDialog();
          PaymentAccessor.this.callbackPayment();
          return;
          PaymentAccessor.this.stopDialog();
        }
        while (paramInt2 != 7);
        ((GameActivity)PaymentAccessor.this.context).regetGuestLoginCheck();
        return;
      case 2:
        PaymentAccessor.this.getPayment(this.shopType);
        return;
      case 3:
      }
      PaymentAccessor.this.stopDialog();
    }

    public void run()
    {
      WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult((Activity)PaymentAccessor.this.context, this);
      HttpRequestHelper localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/");
      localHttpRequestHelper.setParameter("page_id", "payment_url");
      localHttpRequestHelper.setParameter("device_info", PaymentAccessor.this.deviceId);
      localHttpRequestHelper.setParameter("id", PaymentAccessor.this.memberId);
      localHttpRequestHelper.setParameter("item_type", String.valueOf(this.shopType));
      GuestLogin.setDefaultHttpRequest(PaymentAccessor.this.context, localHttpRequestHelper, "6");
      if (localHttpRequestHelper.execute());
      while (true)
      {
        try
        {
          PaymentAccessor.this.urlParser = new PaymentUrlXmlParser(localHttpRequestHelper.getResponse());
          PaymentAccessor.this.urlParser.BasicParse();
          if (Integer.valueOf(PaymentAccessor.this.urlParser.getCODE()).intValue() != 0)
          {
            if (Integer.valueOf(PaymentAccessor.this.urlParser.getCODE()).intValue() == 502)
            {
              localWorkerThreadResult.setErrorInfo(7, PaymentAccessor.this.urlParser.getERRORMESSAGE());
              PaymentAccessor.this.handler.post(localWorkerThreadResult);
              return;
            }
            localWorkerThreadResult.setErrorInfo(0, PaymentAccessor.this.urlParser.getERRORMESSAGE());
            continue;
          }
        }
        catch (Exception localException)
        {
          localWorkerThreadResult.setErrorInfo(1, PaymentAccessor.this.context.getString(2131165248));
          continue;
          PaymentAccessor.this.urlParser.parse();
          continue;
        }
        localWorkerThreadResult.setErrorInfo(5, PaymentAccessor.this.context.getString(2131165248));
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentAccessor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */