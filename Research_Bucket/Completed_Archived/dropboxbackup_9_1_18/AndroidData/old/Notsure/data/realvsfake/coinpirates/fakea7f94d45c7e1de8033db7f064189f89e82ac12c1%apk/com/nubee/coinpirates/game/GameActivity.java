package com.nubee.coinpirates.game;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.nubee.coinpirates.common.AnalyticsService;
import com.nubee.coinpirates.common.BaseActivity;
import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.common.CommonConfig;
import com.nubee.coinpirates.common.HttpGuestRequestHelper;
import com.nubee.coinpirates.common.HttpRequestHelper;
import com.nubee.coinpirates.common.OnWorkerThreadResultListener;
import com.nubee.coinpirates.common.WorkerThreadResult;
import com.nubee.coinpirates.login.GuestLogin;
import com.nubee.coinpirates.login.GuestRegistXmlParser;
import com.nubee.coinpirates.payment.PaymentAccessor;
import com.nubee.coinpirates.payment.PaymentAccessor.Callback;
import com.nubee.coinpirates.payment.PaymentInfoEntity;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.TapjoySpendPointsNotifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GameActivity extends BaseActivity
  implements TapjoyNotifier, TapjoySpendPointsNotifier
{
  static final int LOAD_INSTANCE_STATE = 1;
  static final int LOAD_ON_RENDERER_CREATED = 0;
  private static final int MENU_SOUND_OFF = 0;
  private static final int MENU_SOUND_ON = 1;
  static final int SAVE_FIRST_LOGIN_BONUS = 102;
  static final int SAVE_INSTANCE_STATE = 101;
  static final int SAVE_LOAD_CHECK = 200;
  static final int SAVE_ON_NATIVE_ERRROR = 1000;
  static final int SAVE_PAUSE = 100;
  static final int SAVE_SHOP_CHARGE = 103;
  static final int SAVE_TAPJOY_CHARGE = 104;
  public static boolean isShowAdMob = true;
  static int nPendingCoinsToAdd = 0;
  private GameRenderer mRenderer;
  private GLSurfaceView mView;
  private File m_debugFile = null;
  private int m_versionCode = 0;
  private String m_versionName = "";
  private GuestRegistXmlParser registParser;
  private GuestRegistThread registThread;
  UseItemDialog useItemDlg = null;

  static
  {
    System.loadLibrary("Pusher");
  }

  public GameActivity()
  {
  }

  public static native void charge(int paramInt);

  private boolean checkGuestData()
  {
    String str1 = GuestLogin.getDeviceId(this);
    String str2 = getSharedPreferences("GuestRegist", 0).getString("IMEI", null);
    return (str2 != null) && (str1.equalsIgnoreCase(str2));
  }

  public static native boolean checkStateData(byte[] paramArrayOfByte, int paramInt);

  private boolean clearMember()
  {
    return getSharedPreferences("GuestRegist", 0).edit().clear().commit();
  }

  private void confirmCoinCharge(final String paramString1, final String paramString2, final int paramInt)
  {
    int i;
    if (paramInt <= 1)
      i = 2131165266;
    for (int j = 2131165267; ; j = 2131165271)
    {
      new AlertDialog.Builder(this).setTitle(i).setMessage(j).setCancelable(false).setPositiveButton(2131165268, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          PaymentAccessor localPaymentAccessor = new PaymentAccessor(GameActivity.this, paramString1, paramString2);
          Coins7Log.e("executePayment", String.valueOf(paramInt));
          localPaymentAccessor.executePayments(paramInt);
          GameActivity.setPause(false);
        }
      }).setNegativeButton(2131165187, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          GameActivity.setPause(false);
        }
      }).create().show();
      return;
      i = 2131165270;
    }
  }

  public static native byte[] getStateData(int paramInt, boolean paramBoolean);

  public static native String getStateDataError();

  public static native int getUseItemSet();

  public static native int getUseItemType();

  private boolean loadData(int paramInt)
  {
    SharedPreferences localSharedPreferences = getSharedPreferences("KEY_PREF_LAST_TIME", 0);
    int i = localSharedPreferences.getInt("KEY_APP_VERSION_CODE", 0);
    Coins7Log.i("version: ", String.valueOf(localSharedPreferences.getString("KEY_APP_VERSION_NAME", "")) + " : " + localSharedPreferences.getInt("KEY_APP_VERSION_CODE", 0));
    byte[] arrayOfByte1 = loadDataPreference(paramInt, i);
    if ((arrayOfByte1 != null) && (this.m_debugFile == null))
    {
      setPause(true);
      setStateData(arrayOfByte1, i);
    }
    while (true)
    {
      long l = localSharedPreferences.getLong("KEY_GAME_LAST_TIME", -1L);
      if (l > 0L)
      {
        final int j = offlineBonus((int)((System.currentTimeMillis() - l) / 1000L));
        if (j != 0)
          this.mHandler.post(new Runnable()
          {
            public void run()
            {
              GameActivity localGameActivity = GameActivity.this;
              Object[] arrayOfObject = new Object[1];
              arrayOfObject[0] = Integer.valueOf(j);
              String str = localGameActivity.getString(2131165273, arrayOfObject);
              new AlertDialog.Builder(GameActivity.this).setCancelable(false).setMessage(str).setPositiveButton(2131165186, null).create().show();
            }
          });
      }
      return true;
      byte[] arrayOfByte2 = loadDataFile(paramInt, i);
      if (arrayOfByte2 != null)
      {
        setPause(true);
        setStateData(arrayOfByte2, i);
      }
      else
      {
        Coins7Log.i("loadData", "initialize");
        setStateData(new byte[0], i);
      }
    }
  }

  private byte[] loadDataFile(int paramInt1, int paramInt2)
  {
    try
    {
      String str;
      if (this.m_debugFile != null)
        str = this.m_debugFile.getAbsolutePath();
      FileInputStream localFileInputStream;
      for (Object localObject = new FileInputStream(this.m_debugFile); ; localObject = localFileInputStream)
      {
        arrayOfByte = new byte[((FileInputStream)localObject).available()];
        ((FileInputStream)localObject).read(arrayOfByte);
        ((FileInputStream)localObject).close();
        Coins7Log.i("load file", str);
        boolean bool = checkStateData(arrayOfByte, paramInt2);
        Coins7Log.e("loadData", "checkStateData[" + paramInt1 + "]: " + bool);
        if (bool)
          break;
        sendErrorReport(paramInt1, getStateDataError(), arrayOfByte);
        break;
        str = getSharedPreferences("KEY_PREF_LAST_TIME", 0).getString("KEY_GAME_SAVE_DATA", "sav_file");
        localFileInputStream = openFileInput(str);
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
      return null;
    }
    byte[] arrayOfByte;
    return arrayOfByte;
  }

  private byte[] loadDataPreference(int paramInt1, int paramInt2)
  {
    SharedPreferences localSharedPreferences = getSharedPreferences("KEY_PREF_LAST_TIME", 0);
    for (int i = 0; ; i++)
    {
      if (i >= 2)
        return null;
      int j = 0x1 & i + localSharedPreferences.getInt("KEY_GAME_SAVE_INDEX", 0);
      int k = localSharedPreferences.getInt("KEY_GAME_SAVE_SIZE_" + j, 0);
      if (k > 0)
      {
        String str = "KEY_GAME_SAVE_BINARY_" + j + "_";
        byte[] arrayOfByte = new byte[k];
        long l = 0L;
        for (int m = 0; ; m++)
        {
          int n = arrayOfByte.length;
          if (m >= n)
          {
            boolean bool = checkStateData(arrayOfByte, paramInt2);
            Coins7Log.e("loadData", "checkStateData[" + paramInt1 + "]: " + bool);
            if ((!bool) && (i == 1))
              sendErrorReport(paramInt1, getStateDataError(), arrayOfByte);
            if ((!bool) && (i != 1))
              break;
            Coins7Log.i("load preference", j);
            return arrayOfByte;
          }
          int i1 = m & 0x7;
          if (i1 == 0)
            l = localSharedPreferences.getLong(String.valueOf(str) + (m >> 3), 0L);
          arrayOfByte[m] = (byte)(int)(0xFF & l >> i1 * 8);
        }
      }
    }
  }

  private String loadGuest()
  {
    if (!checkGuestData())
      return null;
    String str = getSharedPreferences("GuestRegist", 0).getString("MEMBER_ID", null);
    Coins7Log.d("GUEST", "loadGuest=" + str);
    return str;
  }

  public static native int offlineBonus(int paramInt);

  private void postLogin()
  {
    if (new PaymentAccessor(this, "", "").hasPaymentKey());
    for (boolean bool = false; ; bool = true)
    {
      checkShopCharge(bool, 1);
      return;
    }
  }

  private boolean saveData(int paramInt)
  {
    setPause(true);
    byte[] arrayOfByte = getStateData(this.m_versionCode, false);
    if (arrayOfByte.length > 0)
    {
      boolean bool = checkStateData(arrayOfByte, this.m_versionCode);
      Coins7Log.e("saveData", "checkStateData[" + paramInt + "]: " + bool);
      if (!bool)
      {
        if (paramInt < 1000)
          sendErrorReport(paramInt, getStateDataError(), arrayOfByte);
        return true;
      }
      saveDataPreference(paramInt, arrayOfByte);
      return true;
    }
    Coins7Log.e("saveData", "getStateData() is failed.");
    return true;
  }

  private void saveDataPreference(int paramInt, byte[] paramArrayOfByte)
  {
    SharedPreferences localSharedPreferences = getSharedPreferences("KEY_PREF_LAST_TIME", 0);
    SharedPreferences.Editor localEditor = localSharedPreferences.edit();
    int i = localSharedPreferences.getInt("KEY_GAME_SAVE_INDEX", 0);
    int j = localSharedPreferences.getInt("KEY_GAME_SAVE_SIZE_" + i, 0);
    String str1 = "KEY_GAME_SAVE_BINARY_" + i + "_";
    String str2;
    int n;
    for (int k = 0xFFFFFFF8 & paramArrayOfByte.length; ; k += 8)
    {
      if (k >= j)
      {
        int m = 0x1 & i + 1;
        localEditor.putInt("KEY_GAME_SAVE_SIZE_" + m, paramArrayOfByte.length);
        str2 = "KEY_GAME_SAVE_BINARY_" + m + "_";
        l = 0L;
        n = 0;
        if (n < paramArrayOfByte.length)
          break;
        if ((0x7 & paramArrayOfByte.length) != 0)
          localEditor.putLong(String.valueOf(str2) + (paramArrayOfByte.length >> 3), l);
        localEditor.putInt("KEY_GAME_SAVE_INDEX", m);
        localEditor.putLong("KEY_GAME_LAST_TIME", System.currentTimeMillis());
        localEditor.putString("KEY_APP_VERSION_NAME", this.m_versionName);
        localEditor.putInt("KEY_APP_VERSION_CODE", this.m_versionCode);
        Coins7Log.i("version", this.m_versionName);
        localEditor.remove("KEY_GAME_SAVE_DATA");
        localEditor.commit();
        return;
      }
      localEditor.remove(String.valueOf(str1) + (k >> 3));
    }
    int i1 = n & 0x7;
    if (i1 == 0);
    for (long l = 0xFF & paramArrayOfByte[n]; ; l |= paramArrayOfByte[n] << i1 * 8 & 255L << i1 * 8)
    {
      if (i1 == 7)
        localEditor.putLong(String.valueOf(str2) + (n >> 3), l);
      n++;
      break;
    }
  }

  private boolean saveGuest()
  {
    if ((this.registParser == null) || (this.registParser.getId() == null))
      return false;
    String str1 = this.registParser.getId();
    Coins7Log.d("GUEST", "saveGuest=" + str1);
    SharedPreferences localSharedPreferences = getSharedPreferences("GuestRegist", 0);
    String str2 = GuestLogin.getDeviceId(this);
    return localSharedPreferences.edit().putString("IMEI", str2).putString("MEMBER_ID", str1).commit();
  }

  private void sendErrorReport(int paramInt, String paramString, byte[] paramArrayOfByte)
  {
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    String str1 = String.valueOf(localDisplayMetrics.widthPixels) + "x" + localDisplayMetrics.heightPixels;
    Object localObject = "";
    String str2 = loadGuest();
    if (str2 != null)
      localObject = str2;
    if (paramInt >= 100)
    {
      str3 = this.m_versionName;
      str4 = String.valueOf(this.m_versionCode);
      while (true)
      {
        localHttpRequestHelper = new HttpRequestHelper("https://pirates.nubee.com/.req_out.php");
        localHttpRequestHelper.setParameter("device_info", GuestLogin.getDeviceId(this));
        localHttpRequestHelper.setParameter("fingerprint", Build.FINGERPRINT);
        localHttpRequestHelper.setParameter("display_size", str1);
        localHttpRequestHelper.setParameter("nubee_id", (String)localObject);
        localHttpRequestHelper.setParameter("version_name", str3);
        localHttpRequestHelper.setParameter("version_code", str4);
        localHttpRequestHelper.setParameter("report_code", String.valueOf(paramInt));
        localHttpRequestHelper.setParameter("report_message", paramString);
        localHttpRequestHelper.setParameter("savedata_size", String.valueOf(paramArrayOfByte.length));
        try
        {
          if (paramArrayOfByte.length > 0)
          {
            FileOutputStream localFileOutputStream = openFileOutput("error_report.dat", 0);
            localFileOutputStream.write(paramArrayOfByte);
            localFileOutputStream.close();
            localHttpRequestHelper.setParameterMaltipart("savedata", "/data/data/" + getPackageName() + "/files/" + "error_report.dat");
          }
          label272: if (localHttpRequestHelper.execute())
          {
            Coins7Log.i("sendErrorReport", "send");
            deleteFile("error_report.dat");
            return;
            SharedPreferences localSharedPreferences = getSharedPreferences("KEY_PREF_LAST_TIME", 0);
            str3 = String.valueOf(localSharedPreferences.getString("KEY_APP_VERSION_NAME", "")) + " -> " + this.m_versionName;
            str4 = String.valueOf(String.valueOf(localSharedPreferences.getInt("KEY_APP_VERSION_CODE", 0))) + " -> " + String.valueOf(this.m_versionCode);
            continue;
          }
          Coins7Log.e("http error", "statusCode: " + localHttpRequestHelper.getStatusCode());
          return;
        }
        catch (IOException localIOException)
        {
          break label272;
        }
      }
    }
  }

  public static native void setPause(boolean paramBoolean);

  public static native boolean setStateData(byte[] paramArrayOfByte, int paramInt);

  public static native boolean useItem(int paramInt1, int paramInt2);

  public static native void wallUp(int paramInt);

  void autoGuestLoginCheck()
  {
    if (this.mHandler == null)
      this.mHandler = new Handler();
    startProgress();
    Coins7Log.e("AUTOLOGIN", "1");
    if ((this.registThread != null) && (this.registThread.isAlive()));
    try
    {
      Coins7Log.e("AUTOLOGIN", "2");
      this.registThread.join();
      this.registThread = null;
      Coins7Log.e("AUTOLOGIN", "3");
      label78: Coins7Log.e("AUTOLOGIN", "4");
      this.registThread = new GuestRegistThread();
      this.registThread.start();
      Coins7Log.e("AUTOLOGIN", "5");
      return;
    }
    catch (InterruptedException localInterruptedException)
    {
      break label78;
    }
  }

  protected void backActivity()
  {
    if (GameRenderer.getState() == 1)
    {
      GameRenderer.setState(0);
      return;
    }
    confirmFinish(2131165188, 2131165189);
  }

  public void checkShopCharge(final boolean paramBoolean, int paramInt)
  {
    String str1 = loadGuest();
    if (str1 != null)
    {
      str2 = GuestLogin.getDeviceId(this);
      localPaymentAccessor = new PaymentAccessor(this, str1, str2);
      if (localPaymentAccessor.hasPaymentKey())
      {
        localPaymentAccessor.setCallback(new PaymentAccessor.Callback()
        {
          public boolean callback(PaymentInfoEntity paramAnonymousPaymentInfoEntity)
          {
            if (paramAnonymousPaymentInfoEntity == null)
            {
              if (paramBoolean)
                GameActivity.this.dismissProgress();
              GameActivity.setPause(false);
              return false;
            }
            try
            {
              int i = Integer.valueOf(paramAnonymousPaymentInfoEntity.getQuantity()).intValue();
              int j = Integer.valueOf(paramAnonymousPaymentInfoEntity.getItemType()).intValue();
              GameActivity.setPause(true);
              if (j == 2)
              {
                GameActivity.wallUp(i * 60);
                GameActivity.this.activityService.trackEvent("Shop", "Commit", "Wall_" + String.valueOf(i));
              }
              while (true)
              {
                GameActivity.this.saveData(103);
                return true;
                GameActivity.charge(i);
                GameActivity.this.activityService.trackEvent("Shop", "Commit", String.valueOf(i));
              }
            }
            finally
            {
              if (paramBoolean)
                GameActivity.this.dismissProgress();
              GameActivity.setPause(false);
            }
          }

          public String completeMessage(PaymentInfoEntity paramAnonymousPaymentInfoEntity)
          {
            if (Integer.valueOf(paramAnonymousPaymentInfoEntity.getItemType()).intValue() == 2)
            {
              GameActivity localGameActivity2 = GameActivity.this;
              Object[] arrayOfObject2 = new Object[1];
              arrayOfObject2[0] = Integer.valueOf(paramAnonymousPaymentInfoEntity.getQuantity());
              return localGameActivity2.getString(2131165272, arrayOfObject2);
            }
            GameActivity localGameActivity1 = GameActivity.this;
            Object[] arrayOfObject1 = new Object[1];
            arrayOfObject1[0] = Integer.valueOf(paramAnonymousPaymentInfoEntity.getQuantity());
            return localGameActivity1.getString(2131165269, arrayOfObject1);
          }
        });
        if (paramBoolean)
          startProgress();
        localPaymentAccessor.executeReceive();
      }
    }
    while (!paramBoolean)
    {
      String str2;
      PaymentAccessor localPaymentAccessor;
      do
        return;
      while (!paramBoolean);
      confirmCoinCharge(str1, str2, paramInt);
      return;
    }
    autoGuestLoginCheck();
  }

  public void getSpendPointsResponse(String paramString, int paramInt)
  {
    Coins7Log.i("TapJoy_CalLback", "currencyName: " + paramString);
    Coins7Log.i("TapJoy_CalLback", "pointTotal: " + paramInt + ", pendingcoins: " + nPendingCoinsToAdd);
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        GameActivity.setPause(true);
        GameActivity.charge(GameActivity.nPendingCoinsToAdd);
        GameActivity.this.saveData(104);
        GameActivity localGameActivity1 = GameActivity.this;
        String str = GameActivity.this.getString(2131165203);
        GameActivity localGameActivity2 = GameActivity.this;
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = Integer.valueOf(GameActivity.nPendingCoinsToAdd);
        localGameActivity1.alertDialog(str, localGameActivity2.getString(2131165204, arrayOfObject), null);
        GameActivity.nPendingCoinsToAdd = 0;
        GameActivity.setPause(false);
      }
    });
  }

  public void getSpendPointsResponseFailed(String paramString)
  {
    Coins7Log.i("TapJoy_CalLback", "spendTapPoints error: " + paramString);
  }

  public void getUpdatePoints(String paramString, int paramInt)
  {
    Coins7Log.i("TapJoy_CalLback", "currencyName: " + paramString);
    Coins7Log.i("TapJoy_CalLback", "pointTotal: " + paramInt);
    if (paramInt > 0)
    {
      TapjoyConnect.getTapjoyConnectInstance(this).spendTapPoints(paramInt, this);
      nPendingCoinsToAdd = paramInt;
    }
  }

  public void getUpdatePointsFailed(String paramString)
  {
    Coins7Log.i("TapJoy_CalLback", "getTapPoints error: " + paramString);
  }

  public void moveToShop(final int paramInt)
  {
    AnalyticsService localAnalyticsService = this.activityService;
    if (paramInt == 101);
    for (String str = "Coin"; ; str = "Stone")
    {
      localAnalyticsService.trackEvent("Shop", "Show", str);
      this.mHandler.post(new Runnable()
      {
        public void run()
        {
          Coins7Log.e("GameRenderer", "move to shop");
          GameActivity localGameActivity = GameActivity.this;
          if (paramInt == 101);
          for (int i = 1; ; i = 2)
          {
            localGameActivity.checkShopCharge(true, i);
            return;
          }
        }
      });
      return;
    }
  }

  public void moveToTapjoy()
  {
    this.activityService.trackEvent("Tapjoy", "Show", null);
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        GameActivity.this.confirmDialog(2131165201, 2131165202, new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            Coins7Log.e("GameRenderer", "move to tapjoy");
            TapjoyConnect.getTapjoyConnectInstance(GameActivity.this).showOffers(GameActivity.this);
          }
        }
        , null);
      }
    });
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle, getClass().getSimpleName());
    try
    {
      PackageInfo localPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 128);
      this.m_versionName = localPackageInfo.versionName;
      this.m_versionCode = localPackageInfo.versionCode;
      label46: requestWindowFeature(1);
      getWindow().addFlags(1024);
      this.mView = new GLSurfaceView(this);
      this.mView.setEGLConfigChooser(true);
      this.mRenderer = new GameRenderer(this);
      this.mView.setRenderer(this.mRenderer);
      this.mView.setOnTouchListener(this.mRenderer);
      SoundManager.initialize(this);
      setContentView(2130903042);
      ((ViewGroup)findViewById(2131296276)).addView(this.mView);
      AdManager.setTestDevices(new String[] { "emulator" });
      TapjoyConnect.getTapjoyConnectInstance(getApplicationContext());
      return;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      break label46;
    }
  }

  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    super.onCreateOptionsMenu(paramMenu);
    Resources localResources = getResources();
    paramMenu.add(0, 0, 0, localResources.getString(2131165206)).setIcon(2130837553);
    paramMenu.add(0, 1, 0, localResources.getString(2131165205)).setIcon(2130837554);
    return true;
  }

  protected void onDestroy()
  {
    setPause(true);
    this.mRenderer.onDestroy();
    super.onDestroy();
    SoundManager.destroy();
    TapjoyConnect.getTapjoyConnectInstance(getApplicationContext()).finalize();
  }

  public void onNativeError(int paramInt, String paramString)
  {
    Coins7Log.e("onNativeError", "errCode: " + paramInt + ", message: " + paramString);
    saveData(paramInt + 1000);
    byte[] arrayOfByte = getStateData(this.m_versionCode, true);
    if (checkStateData(arrayOfByte, this.m_versionCode))
    {
      sendErrorReport(paramInt + 1000, paramString, new byte[0]);
      return;
    }
    sendErrorReport(paramInt + 1000, getStateDataError(), arrayOfByte);
  }

  public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    if (super.onOptionsItemSelected(paramMenuItem))
      return true;
    switch (paramMenuItem.getItemId())
    {
    default:
      return false;
    case 0:
      SoundManager.enableSe(false);
      return true;
    case 1:
    }
    SoundManager.enableSe(true);
    return true;
  }

  protected void onPause()
  {
    setPause(true);
    saveData(100);
    SoundManager.onPause();
    super.onPause();
    if (this.mView != null)
      this.mView.onPause();
    if (this.useItemDlg != null)
    {
      this.useItemDlg.hide();
      this.useItemDlg = null;
    }
  }

  protected void onRendererCreated()
  {
    loadData(0);
    setPause(false);
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        GameActivity.setPause(true);
        GameActivity.this.checkShopCharge(false, 1);
        GameActivity.setPause(false);
      }
    });
  }

  protected void onRestoreInstanceState(Bundle paramBundle)
  {
    super.onRestoreInstanceState(paramBundle);
    Coins7Log.d("GameAcitivity", "onRestoreInstanceState::loadData");
    loadData(1);
    setPause(false);
  }

  protected void onResume()
  {
    SoundManager.onResume();
    super.onResume();
    if (this.mView != null)
      this.mView.onResume();
    Log.i("Tapjoy", "start query server for points");
    TapjoyConnect.getTapjoyConnectInstance(this).getTapPoints(this);
    setPause(false);
  }

  protected void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    Coins7Log.d("GameAcitivity", "onSaveInstanceState::saveData");
    setPause(true);
    saveData(101);
  }

  public void regetGuestLoginCheck()
  {
    clearMember();
    autoGuestLoginCheck();
  }

  public void showAdMob(boolean paramBoolean)
  {
    if ((paramBoolean) && (!isShowAdMob))
      this.mHandler.post(new Runnable()
      {
        public void run()
        {
          ((AdView)GameActivity.this.findViewById(2131296277)).setVisibility(0);
          GameActivity.isShowAdMob = true;
        }
      });
    while ((paramBoolean) || (!isShowAdMob))
      return;
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        ((AdView)GameActivity.this.findViewById(2131296277)).setVisibility(4);
        GameActivity.isShowAdMob = false;
      }
    });
  }

  public void useItem()
  {
    this.mHandler.post(new Runnable()
    {
      public void run()
      {
        GameActivity.this.useItemDlg = UseItemDialog.show(GameActivity.this);
      }
    });
  }

  private final class GuestDailyThread extends Thread
    implements OnWorkerThreadResultListener
  {
    public GuestDailyThread()
    {
      super();
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
          GameActivity.this.dismissProgress();
          GameActivity.this.postLogin();
          continue;
        }
        finally
        {
          GameActivity.setPause(false);
        }
        Coins7Log.e("AUTOLOGIN", "18");
        GameActivity.this.dismissProgress();
        continue;
        Coins7Log.e("AUTOLOGIN", "19");
        new GuestDailyThread(GameActivity.this).start();
        continue;
        Coins7Log.e("AUTOLOGIN", "20");
        GameActivity.this.dismissProgress();
      }
    }

    public void run()
    {
      Coins7Log.e("AUTOLOGIN", "14");
      String str1 = ((TelephonyManager)GameActivity.this.getSystemService("phone")).getDeviceId();
      WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult(GameActivity.this, this);
      String str2 = GameActivity.this.loadGuest();
      if (str2 == null)
      {
        Coins7Log.d("Roulette", "Guest Data is null, skip DailyAccess.");
        GameActivity.this.mHandler.post(localWorkerThreadResult);
        return;
      }
      GuestLogin.login(GameActivity.this, localWorkerThreadResult, str2, str1, "6");
      GameActivity.this.mHandler.post(localWorkerThreadResult);
    }
  }

  private final class GuestRegistThread extends Thread
    implements OnWorkerThreadResultListener
  {
    public GuestRegistThread()
    {
      super();
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
        GameActivity.this.saveGuest();
        new GameActivity.GuestDailyThread(GameActivity.this).start();
        return;
      case 1:
        Coins7Log.e("AUTOLOGIN", "11");
        GameActivity.this.dismissProgress();
        return;
      case 2:
        Coins7Log.e("AUTOLOGIN", "12");
        GameActivity.this.autoGuestLoginCheck();
        return;
      case 3:
      }
      Coins7Log.e("AUTOLOGIN", "13");
      GameActivity.this.dismissProgress();
    }

    public void run()
    {
      Coins7Log.e("AUTOLOGIN", "7");
      String str = ((TelephonyManager)GameActivity.this.getSystemService("phone")).getDeviceId();
      WorkerThreadResult localWorkerThreadResult = new WorkerThreadResult(GameActivity.this, this);
      if (GameActivity.this.loadGuest() != null)
      {
        Coins7Log.e("AUTOLOGIN", "7-SKIP_REQUEST");
        GameActivity.this.mHandler.post(localWorkerThreadResult);
        return;
      }
      HttpGuestRequestHelper localHttpGuestRequestHelper = new HttpGuestRequestHelper("https://appli.nubee.com/index.php", str);
      localHttpGuestRequestHelper.setParameter("page_id", "guest_regist");
      DisplayMetrics localDisplayMetrics = new DisplayMetrics();
      GameActivity.this.getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
      localHttpGuestRequestHelper.addDiaplaySize(String.valueOf(localDisplayMetrics.widthPixels) + "x" + localDisplayMetrics.heightPixels);
      localHttpGuestRequestHelper.addModel();
      localHttpGuestRequestHelper.addOsVersion();
      if (localHttpGuestRequestHelper.execute())
        Coins7Log.e("AUTOLOGIN", "8");
      while (true)
      {
        try
        {
          GameActivity.this.registParser = new GuestRegistXmlParser(localHttpGuestRequestHelper.getResponse());
          GameActivity.this.registParser.BasicParse();
          if (CommonConfig.getStringToInt(String.valueOf(GameActivity.this.registParser.getCODE())) != 0)
          {
            localWorkerThreadResult.setErrorInfo(0, GameActivity.this.registParser.getERRORMESSAGE());
            GameActivity.this.mHandler.post(localWorkerThreadResult);
            return;
          }
          GameActivity.this.registParser.parse();
          continue;
        }
        catch (Exception localException)
        {
          localWorkerThreadResult.setErrorInfo(1, GameActivity.this.getString(2131165227));
          continue;
        }
        Coins7Log.e("AUTOLOGIN", "9");
        localWorkerThreadResult.setErrorInfo(5, GameActivity.this.getString(2131165227));
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */