package com.admob.android.ads;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

public class AdManager
{
  public static final String LOG = "AdMobSDK";
  public static final String SDK_VERSION = "20101109-ANDROID-3312276cc1406347";
  public static final String SDK_VERSION_DATE = "20101109";
  public static final String TEST_EMULATOR = "emulator";
  private static String a;
  private static int b;
  private static String c;
  private static String d;
  private static String e = j.a.a.toString();
  private static String[] f = null;
  private static String g;
  private static Location h;
  private static boolean i = false;
  private static boolean j = false;
  private static long k;
  private static String l;
  private static GregorianCalendar m;
  private static Gender n;
  private static boolean o = false;
  private static Boolean p = null;

  static
  {
    if (InterstitialAd.c.a("AdMobSDK", 4))
      Log.i("AdMobSDK", "AdMob SDK version is 20101109-ANDROID-3312276cc1406347");
  }

  private AdManager()
  {
  }

  static a a(v paramv)
  {
    int i1 = paramv.a();
    if (isEmulator())
      return a.c;
    if ((paramv.b()) || (paramv.c()) || (i1 == 2) || (i1 == 1))
      return a.b;
    int i2 = paramv.d();
    if ((i2 == 0) || (i2 == 1))
      return a.b;
    return a.a;
  }

  static String a()
  {
    return String.valueOf(k / 1000L);
  }

  private static String a(Bundle paramBundle, String paramString1, String paramString2)
  {
    String str1 = paramBundle.getString(paramString1);
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "Publisher ID read from AndroidManifest.xml is " + str1);
    String str2 = null;
    if (paramString2 == null)
    {
      str2 = null;
      if (str1 != null)
        str2 = str1;
    }
    return str2;
  }

  static void a(Context paramContext)
  {
    if (!o)
    {
      o = true;
      try
      {
        PackageManager localPackageManager = paramContext.getPackageManager();
        String str1 = paramContext.getPackageName();
        ApplicationInfo localApplicationInfo = localPackageManager.getApplicationInfo(str1, 128);
        if (localApplicationInfo != null)
        {
          if (localApplicationInfo.metaData != null)
          {
            String str2 = a(localApplicationInfo.metaData, "ADMOB_PUBLISHER_ID", c);
            if (str2 != null)
              setPublisherId(str2);
            String str3 = a(localApplicationInfo.metaData, "ADMOB_INTERSTITIAL_PUBLISHER_ID", d);
            if (str3 != null)
              setInterstitialPublisherId(str3);
            if (!j)
              i = localApplicationInfo.metaData.getBoolean("ADMOB_ALLOW_LOCATION_FOR_ADS", false);
          }
          a = localApplicationInfo.packageName;
          if (c != null)
            a(c);
          if (d != null)
            a(d);
          if (InterstitialAd.c.a("AdMobSDK", 2))
            Log.v("AdMobSDK", "Application's package name is " + a);
        }
        PackageInfo localPackageInfo = localPackageManager.getPackageInfo(str1, 0);
        if (localPackageInfo != null)
        {
          b = localPackageInfo.versionCode;
          if (InterstitialAd.c.a("AdMobSDK", 2))
            Log.v("AdMobSDK", "Application's version number is " + b);
        }
        return;
      }
      catch (Exception localException)
      {
      }
    }
  }

  private static void a(String paramString)
  {
    if ((paramString == null) || (paramString.length() != 15))
      clientError("SETUP ERROR:  Incorrect AdMob publisher ID.  Should 15 [a-f,0-9] characters:  " + c);
    if ((a != null) && (paramString.equalsIgnoreCase("a1496ced2842262")) && (!"com.admob.android.ads".equals(a)) && (!"com.example.admob.lunarlander".equals(a)))
      clientError("SETUP ERROR:  Cannot use the sample publisher ID (a1496ced2842262).  Yours is available on www.admob.com.");
  }

  static String b()
  {
    GregorianCalendar localGregorianCalendar = getBirthday();
    String str = null;
    if (localGregorianCalendar != null)
    {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = Integer.valueOf(localGregorianCalendar.get(1));
      arrayOfObject[1] = Integer.valueOf(1 + localGregorianCalendar.get(2));
      arrayOfObject[2] = Integer.valueOf(localGregorianCalendar.get(5));
      str = String.format("%04d%02d%02d", arrayOfObject);
    }
    return str;
  }

  static String b(Context paramContext)
  {
    Location localLocation = getCoordinates(paramContext);
    String str = null;
    if (localLocation != null)
      str = localLocation.getLatitude() + "," + localLocation.getLongitude();
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "User coordinates are " + str);
    return str;
  }

  static String c()
  {
    if (n == Gender.MALE)
      return "m";
    if (n == Gender.FEMALE)
      return "f";
    return null;
  }

  protected static void clientError(String paramString)
  {
    if (InterstitialAd.c.a("AdMobSDK", 6))
      Log.e("AdMobSDK", paramString);
    throw new IllegalArgumentException(paramString);
  }

  public static String getApplicationPackageName(Context paramContext)
  {
    if (a == null)
      a(paramContext);
    return a;
  }

  protected static int getApplicationVersion(Context paramContext)
  {
    if (a == null)
      a(paramContext);
    return b;
  }

  public static GregorianCalendar getBirthday()
  {
    return m;
  }

  public static Location getCoordinates(Context paramContext)
  {
    if ((isEmulator()) && (!i) && (InterstitialAd.c.a("AdMobSDK", 4)))
    {
      Log.i("AdMobSDK", "Location information is not being used for ad requests. Enable location");
      Log.i("AdMobSDK", "based ads with AdManager.setAllowUseOfLocation(true) or by setting ");
      Log.i("AdMobSDK", "meta-data ADMOB_ALLOW_LOCATION_FOR_ADS to true in AndroidManifest.xml");
    }
    if ((i) && (paramContext != null) && ((h == null) || (System.currentTimeMillis() > 900000L + k)))
      try
      {
        if ((h == null) || (System.currentTimeMillis() > 900000L + k))
        {
          k = System.currentTimeMillis();
          if (paramContext.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0)
            break label373;
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Trying to get locations from the network.");
          localLocationManager = (LocationManager)paramContext.getSystemService("location");
          if (localLocationManager == null)
            break label365;
          localCriteria2 = new Criteria();
          localCriteria2.setAccuracy(2);
          localCriteria2.setCostAllowed(false);
          str = localLocationManager.getBestProvider(localCriteria2, true);
          i1 = 1;
          if ((str == null) && (paramContext.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0))
          {
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Trying to get locations from GPS.");
            localLocationManager = (LocationManager)paramContext.getSystemService("location");
            if (localLocationManager == null)
              break label359;
            localCriteria1 = new Criteria();
            localCriteria1.setAccuracy(1);
            localCriteria1.setCostAllowed(false);
            str = localLocationManager.getBestProvider(localCriteria1, true);
            i1 = 1;
          }
          if (i1 != 0)
            break label289;
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Cannot access user's location.  Permissions are not set.");
        }
        while (true)
        {
          return h;
          if (str != null)
            break;
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "No location providers are available.  Ads will not be geotargeted.");
        }
      }
      finally
      {
      }
    while (true)
    {
      Criteria localCriteria2;
      Criteria localCriteria1;
      label289: if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "Location provider setup successfully.");
      localLocationManager.requestLocationUpdates(str, 0L, 0.0F, new LocationListener()
      {
        public final void onLocationChanged(Location paramAnonymousLocation)
        {
          AdManager.a(paramAnonymousLocation);
          AdManager.a(System.currentTimeMillis());
          this.a.removeUpdates(this);
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Acquired location " + AdManager.d().getLatitude() + "," + AdManager.d().getLongitude() + " at " + new Date(AdManager.e()).toString() + ".");
        }

        public final void onProviderDisabled(String paramAnonymousString)
        {
        }

        public final void onProviderEnabled(String paramAnonymousString)
        {
        }

        public final void onStatusChanged(String paramAnonymousString, int paramAnonymousInt, Bundle paramAnonymousBundle)
        {
        }
      }
      , paramContext.getMainLooper());
      continue;
      label359: int i1 = 1;
      continue;
      label365: i1 = 1;
      String str = null;
      continue;
      label373: LocationManager localLocationManager = null;
      str = null;
      i1 = 0;
    }
  }

  static String getEndpoint()
  {
    return b.a();
  }

  public static Gender getGender()
  {
    return n;
  }

  public static String getInterstitialPublisherId(Context paramContext)
  {
    if (d == null)
      a(paramContext);
    if ((d == null) && (InterstitialAd.c.a("AdMobSDK", 6)))
      Log.e("AdMobSDK", "getInterstitialPublisherId returning null publisher id.  Please set the publisher id in AndroidManifest.xml or using AdManager.setPublisherId(String)");
    return d;
  }

  public static String getOrientation(Context paramContext)
  {
    String str = "p";
    if (((WindowManager)paramContext.getSystemService("window")).getDefaultDisplay().getOrientation() == 1)
      str = "l";
    return str;
  }

  public static String getPostalCode()
  {
    return l;
  }

  public static String getPublisherId(Context paramContext)
  {
    if (c == null)
      a(paramContext);
    if ((c == null) && (InterstitialAd.c.a("AdMobSDK", 6)))
      Log.e("AdMobSDK", "getPublisherId returning null publisher id.  Please set the publisher id in AndroidManifest.xml or using AdManager.setPublisherId(String)");
    return c;
  }

  protected static int getScreenWidth(Context paramContext)
  {
    Display localDisplay = ((WindowManager)paramContext.getSystemService("window")).getDefaultDisplay();
    if (localDisplay != null)
      return localDisplay.getWidth();
    return 0;
  }

  public static String getTestAction()
  {
    return e;
  }

  static String[] getTestDevices()
  {
    return f;
  }

  public static String getUserId(Context paramContext)
  {
    String str;
    if (g == null)
    {
      str = Settings.Secure.getString(paramContext.getContentResolver(), "android_id");
      if ((str != null) && (!isEmulator()))
        break label88;
      g = "emulator";
      Log.i("AdMobSDK", "To get test ads on the emulator use AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR } )");
    }
    while (true)
    {
      if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "The user ID is " + g);
      if (g != "emulator")
        break;
      return null;
      label88: g = md5(str);
      Log.i("AdMobSDK", "To get test ads on this device use AdManager.setTestDevices( new String[] { \"" + g + "\" } )");
    }
    return g;
  }

  public static boolean isEmulator()
  {
    return ("unknown".equals(Build.BOARD)) && ("generic".equals(Build.DEVICE)) && ("generic".equals(Build.BRAND));
  }

  public static boolean isTestDevice(Context paramContext)
  {
    if (f != null)
    {
      String str = getUserId(paramContext);
      if (str == null)
        str = "emulator";
      return Arrays.binarySearch(f, str) >= 0;
    }
    return false;
  }

  protected static String md5(String paramString)
  {
    Object localObject = null;
    if (paramString != null)
    {
      int i1 = paramString.length();
      localObject = null;
      if (i1 > 0)
        try
        {
          MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
          localMessageDigest.update(paramString.getBytes(), 0, paramString.length());
          Object[] arrayOfObject = new Object[1];
          arrayOfObject[0] = new BigInteger(1, localMessageDigest.digest());
          String str = String.format("%032X", arrayOfObject);
          localObject = str;
          return localObject;
        }
        catch (Exception localException)
        {
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "Could not generate hash of " + paramString, localException);
        }
    }
    return paramString.substring(0, 32);
  }

  public static void setAllowUseOfLocation(boolean paramBoolean)
  {
    j = true;
    i = paramBoolean;
  }

  public static void setBirthday(int paramInt1, int paramInt2, int paramInt3)
  {
    GregorianCalendar localGregorianCalendar = new GregorianCalendar();
    localGregorianCalendar.set(paramInt1, paramInt2 - 1, paramInt3);
    setBirthday(localGregorianCalendar);
  }

  public static void setBirthday(GregorianCalendar paramGregorianCalendar)
  {
    m = paramGregorianCalendar;
  }

  static void setEndpoint(String paramString)
  {
    b.a(paramString);
  }

  public static void setGender(Gender paramGender)
  {
    n = paramGender;
  }

  public static void setInterstitialPublisherId(String paramString)
  {
    a(paramString);
    if (InterstitialAd.c.a("AdMobSDK", 4))
      Log.i("AdMobSDK", "Interstitial Publisher ID set to " + paramString);
    d = paramString;
  }

  public static void setPostalCode(String paramString)
  {
    l = paramString;
  }

  public static void setPublisherId(String paramString)
  {
    a(paramString);
    if (InterstitialAd.c.a("AdMobSDK", 4))
      Log.i("AdMobSDK", "Publisher ID set to " + paramString);
    c = paramString;
  }

  public static void setTestAction(String paramString)
  {
    e = paramString;
  }

  public static void setTestDevices(String[] paramArrayOfString)
  {
    if (paramArrayOfString == null)
    {
      f = null;
      return;
    }
    String[] arrayOfString = (String[])paramArrayOfString.clone();
    f = arrayOfString;
    Arrays.sort(arrayOfString);
  }

  public static enum Gender
  {
    static
    {
      FEMALE = new Gender("FEMALE", 1);
      Gender[] arrayOfGender = new Gender[2];
      arrayOfGender[0] = MALE;
      arrayOfGender[1] = FEMALE;
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdManager
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */