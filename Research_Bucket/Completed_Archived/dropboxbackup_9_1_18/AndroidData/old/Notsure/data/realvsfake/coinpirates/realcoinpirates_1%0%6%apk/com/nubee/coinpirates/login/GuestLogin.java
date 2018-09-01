package com.nubee.coinpirates.login;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.nubee.coinpirates.common.HttpRequestHelper;

public class GuestLogin
{
  public GuestLogin()
  {
  }

  public static final String getDeviceId(Context paramContext)
  {
    return ((TelephonyManager)paramContext.getSystemService("phone")).getDeviceId();
  }

  // ERROR //
  public static GuestDailyXmlParser login(android.app.Activity paramActivity, com.nubee.coinpirates.common.WorkerThreadResult paramWorkerThreadResult, String paramString1, String paramString2, String paramString3)
  {
    // Byte code:
    //   0: aload_3
    //   1: ifnonnull +8 -> 9
    //   4: aload_0
    //   5: invokestatic 29	com/nubee/coinpirates/login/GuestLogin:getDeviceId	(Landroid/content/Context;)Ljava/lang/String;
    //   8: astore_3
    //   9: aload_2
    //   10: ifnonnull +13 -> 23
    //   13: new 31	java/lang/IllegalStateException
    //   16: dup
    //   17: ldc 33
    //   19: invokespecial 36	java/lang/IllegalStateException:<init>	(Ljava/lang/String;)V
    //   22: athrow
    //   23: new 38	com/nubee/coinpirates/common/HttpRequestHelper
    //   26: dup
    //   27: ldc 40
    //   29: invokespecial 41	com/nubee/coinpirates/common/HttpRequestHelper:<init>	(Ljava/lang/String;)V
    //   32: astore 5
    //   34: aload_0
    //   35: aload 5
    //   37: aload 4
    //   39: invokestatic 45	com/nubee/coinpirates/login/GuestLogin:setDefaultHttpRequest	(Landroid/content/Context;Lcom/nubee/coinpirates/common/HttpRequestHelper;Ljava/lang/String;)V
    //   42: aload 5
    //   44: ldc 47
    //   46: ldc 48
    //   48: invokevirtual 52	com/nubee/coinpirates/common/HttpRequestHelper:setParameter	(Ljava/lang/String;Ljava/lang/String;)V
    //   51: aload 5
    //   53: ldc 54
    //   55: aload_3
    //   56: invokevirtual 52	com/nubee/coinpirates/common/HttpRequestHelper:setParameter	(Ljava/lang/String;Ljava/lang/String;)V
    //   59: aload 5
    //   61: ldc 56
    //   63: aload_2
    //   64: invokevirtual 52	com/nubee/coinpirates/common/HttpRequestHelper:setParameter	(Ljava/lang/String;Ljava/lang/String;)V
    //   67: new 58	android/util/DisplayMetrics
    //   70: dup
    //   71: invokespecial 59	android/util/DisplayMetrics:<init>	()V
    //   74: astore 6
    //   76: aload_0
    //   77: invokevirtual 65	android/app/Activity:getWindowManager	()Landroid/view/WindowManager;
    //   80: invokeinterface 71 1 0
    //   85: aload 6
    //   87: invokevirtual 77	android/view/Display:getMetrics	(Landroid/util/DisplayMetrics;)V
    //   90: aload 5
    //   92: ldc 79
    //   94: new 81	java/lang/StringBuilder
    //   97: dup
    //   98: aload 6
    //   100: getfield 85	android/util/DisplayMetrics:widthPixels	I
    //   103: invokestatic 91	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   106: invokespecial 92	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   109: ldc 94
    //   111: invokevirtual 98	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   114: aload 6
    //   116: getfield 101	android/util/DisplayMetrics:heightPixels	I
    //   119: invokevirtual 104	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   122: invokevirtual 107	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   125: invokevirtual 52	com/nubee/coinpirates/common/HttpRequestHelper:setParameter	(Ljava/lang/String;Ljava/lang/String;)V
    //   128: aconst_null
    //   129: astore 7
    //   131: aload 5
    //   133: invokevirtual 111	com/nubee/coinpirates/common/HttpRequestHelper:execute	()Z
    //   136: ifeq +73 -> 209
    //   139: new 113	com/nubee/coinpirates/login/GuestDailyXmlParser
    //   142: dup
    //   143: aload 5
    //   145: invokevirtual 117	com/nubee/coinpirates/common/HttpRequestHelper:getResponse	()Ljava/io/InputStream;
    //   148: invokespecial 120	com/nubee/coinpirates/login/GuestDailyXmlParser:<init>	(Ljava/io/InputStream;)V
    //   151: astore 8
    //   153: aload 8
    //   155: invokevirtual 123	com/nubee/coinpirates/login/GuestDailyXmlParser:BasicParse	()V
    //   158: aload 8
    //   160: invokevirtual 127	com/nubee/coinpirates/login/GuestDailyXmlParser:getCODE	()I
    //   163: invokestatic 91	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   166: invokestatic 133	com/nubee/coinpirates/common/CommonConfig:getStringToInt	(Ljava/lang/String;)I
    //   169: ifeq +16 -> 185
    //   172: aload_1
    //   173: iconst_0
    //   174: aload 8
    //   176: invokevirtual 136	com/nubee/coinpirates/login/GuestDailyXmlParser:getERRORMESSAGE	()Ljava/lang/String;
    //   179: invokevirtual 142	com/nubee/coinpirates/common/WorkerThreadResult:setErrorInfo	(ILjava/lang/String;)V
    //   182: aload 8
    //   184: areturn
    //   185: aload 8
    //   187: invokevirtual 145	com/nubee/coinpirates/login/GuestDailyXmlParser:parse	()V
    //   190: aload 8
    //   192: areturn
    //   193: astore 10
    //   195: aload_1
    //   196: iconst_1
    //   197: aload_0
    //   198: ldc 146
    //   200: invokevirtual 149	android/app/Activity:getString	(I)Ljava/lang/String;
    //   203: invokevirtual 142	com/nubee/coinpirates/common/WorkerThreadResult:setErrorInfo	(ILjava/lang/String;)V
    //   206: aload 7
    //   208: areturn
    //   209: aload_1
    //   210: iconst_5
    //   211: aload_0
    //   212: ldc 146
    //   214: invokevirtual 149	android/app/Activity:getString	(I)Ljava/lang/String;
    //   217: invokevirtual 142	com/nubee/coinpirates/common/WorkerThreadResult:setErrorInfo	(ILjava/lang/String;)V
    //   220: aconst_null
    //   221: areturn
    //   222: astore 9
    //   224: aload 8
    //   226: astore 7
    //   228: goto -33 -> 195
    //
    // Exception table:
    //   from	to	target	type
    //   139	153	193	java/lang/Exception
    //   153	182	222	java/lang/Exception
    //   185	190	222	java/lang/Exception
  }

  public static void setDefaultHttpRequest(Context paramContext, HttpRequestHelper paramHttpRequestHelper, String paramString)
  {
    try
    {
      str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 128).versionName;
      paramHttpRequestHelper.setParameter("contents_id", paramString);
      paramHttpRequestHelper.setParameter("machine", Build.MODEL);
      paramHttpRequestHelper.setParameter("systemVersion", Build.VERSION.RELEASE);
      paramHttpRequestHelper.setParameter("appVersion", str);
      paramHttpRequestHelper.setParameter("os_version", Build.VERSION.RELEASE);
      paramHttpRequestHelper.setParameter("os_device", "Android");
      return;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      while (true)
      {
        Log.e(GuestLogin.class.getSimpleName(), localNameNotFoundException.getMessage(), localNameNotFoundException);
        String str = "";
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.login.GuestLogin
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */