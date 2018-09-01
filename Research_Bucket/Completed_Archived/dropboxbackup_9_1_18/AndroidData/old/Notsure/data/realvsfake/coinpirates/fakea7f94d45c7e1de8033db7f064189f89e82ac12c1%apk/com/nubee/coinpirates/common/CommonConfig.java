package com.nubee.coinpirates.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Bitmap;;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommonConfig
{
  public CommonConfig()
  {
  }

  public static int calcHeight(Bitmap paramBitmap, int paramInt)
  {
    int i = paramBitmap.getWidth();
    int j = paramBitmap.getHeight();
    int k = paramBitmap.getHeight();
    if (paramInt > 0)
    {
      float f = i / j;
      k = (int)(paramInt / f);
    }
    return k;
  }

  public static boolean checkSdcard()
  {
    return Environment.getExternalStorageState().equals("mounted");
  }

  public static String compareDateWithToday(String paramString)
  {
    Calendar localCalendar1 = Calendar.getInstance();
    Calendar localCalendar2 = Calendar.getInstance();
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    localSimpleDateFormat.setLenient(false);
    if (paramString != null)
      try
      {
        localCalendar2.setTime(localSimpleDateFormat.parse(paramString));
        localCalendar1.set(11, 0);
        localCalendar1.set(12, 0);
        localCalendar1.set(13, 0);
        localCalendar1.set(14, 0);
        str1 = paramString.substring(5, 7);
        str2 = paramString.substring(8, 10);
        String str3 = paramString.substring(11, 13);
        String str4 = paramString.substring(14, 16);
        if (localCalendar1.compareTo(localCalendar2) < 0)
        {
          return String.valueOf(str3) + ":" + str4;
          return "";
        }
      }
      catch (ParseException localParseException)
      {
        return "";
      }
    String str1;
    String str2;
    String str5;
    StringBuilder localStringBuilder;
    if (str1.startsWith("0"))
    {
      str5 = str1.substring(1);
      localStringBuilder = new StringBuilder(String.valueOf(str5)).append("/");
      if (!str2.startsWith("0"))
        break label218;
    }
    label218: for (String str6 = str2.substring(1); ; str6 = str2)
    {
      return str6;
      str5 = str1;
      break;
    }
  }

  protected static BitmapFactory.Options createDefaultOptions()
  {
    BitmapFactory.Options localOptions = new BitmapFactory.Options();
    localOptions.inJustDecodeBounds = false;
    localOptions.inDither = false;
    localOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    return localOptions;
  }

  public static String formatPrice(int paramInt)
  {
    return NumberFormat.getNumberInstance().format(paramInt);
  }

  public static String formatPrice(String paramString)
  {
    int i = Integer.parseInt(paramString);
    return NumberFormat.getNumberInstance().format(i);
  }

  public static Bitmap getBitmapDrawableImage(Context paramContext, int paramInt)
  {
    BitmapFactory.Options localOptions = createDefaultOptions();
    Bitmap localBitmap = BitmapFactory.decodeResource(paramContext.getResources(), paramInt, localOptions);
    if (localBitmap == null)
    {
      Coins7Log.e("getBitmapDrawableImage", String.valueOf(paramInt) + " is not loaded.");
      return null;
    }
    Coins7Log.e("getBitmapLocalImage3", String.valueOf(paramInt) + " is loaded.");
    Coins7Log.e("getBitmapDrawableImage", "GetImageType: " + localBitmap.getConfig());
    return localBitmap;
  }

  public static Bitmap getBitmapLocalImage(Context paramContext, String paramString)
  {
    Bitmap localBitmap1 = BitmapFactory.decodeFile(paramString, createDefaultOptions());
    if (localBitmap1 == null)
    {
      Coins7Log.e("getBitmapLocalImage3", String.valueOf(paramString) + " is not loaded.");
      return null;
    }
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    ((Activity)paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, (int)(localBitmap1.getWidth() * localDisplayMetrics.density), (int)(localBitmap1.getHeight() * localDisplayMetrics.density), true);
    localBitmap1.recycle();
    Coins7Log.e("getBitmapLocalImage3", String.valueOf(paramString) + " is loaded.");
    return localBitmap2;
  }

  public static Bitmap getBitmapLocalImage(String paramString, int paramInt1, int paramInt2)
  {
    Bitmap localBitmap1 = BitmapFactory.decodeFile(paramString, createDefaultOptions());
    if (localBitmap1 == null)
    {
      Coins7Log.e("getBitmapLocalImage2", String.valueOf(paramString) + " is not loaded.");
      return null;
    }
    if (paramInt2 == 0)
      paramInt2 = calcHeight(localBitmap1, paramInt1);
    Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, paramInt1, paramInt2, true);
    localBitmap1.recycle();
    Coins7Log.e("getBitmapLocalImage2", String.valueOf(paramString) + " is loaded.");
    return localBitmap2;
  }

  public static Bitmap getBitmapLocalImage565(Context paramContext, String paramString)
  {
    Bitmap localBitmap1 = BitmapFactory.decodeFile(paramString, createDefaultOptions());
    if (localBitmap1 == null)
    {
      Coins7Log.e("getBitmapLocalImage4", String.valueOf(paramString) + " is not loaded.");
      return null;
    }
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    ((Activity)paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, (int)(localBitmap1.getWidth() * localDisplayMetrics.density), (int)(localBitmap1.getHeight() * localDisplayMetrics.density), true);
    localBitmap1.recycle();
    Coins7Log.e("getBitmapLocalImage4", String.valueOf(paramString) + " is loaded.");
    return localBitmap2;
  }

  public static Bitmap getBitmapServerImage(Context paramContext, String paramString, GetBitmapOptions paramGetBitmapOptions)
  {
    try
    {
      Bitmap localBitmap = getBitmapServerImage(paramContext, new URL(paramString), paramGetBitmapOptions);
      return localBitmap;
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    return null;
  }

  public static Bitmap getBitmapServerImage(Context paramContext, URL paramURL)
  {
    try
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)paramURL.openConnection();
      localHttpURLConnection.setConnectTimeout(10000);
      localHttpURLConnection.setReadTimeout(10000);
      localHttpURLConnection.connect();
      BitmapFactory.Options localOptions = createDefaultOptions();
      Bitmap localBitmap1 = BitmapFactory.decodeStream(localHttpURLConnection.getInputStream(), null, localOptions);
      if (localBitmap1 == null)
        return null;
      DisplayMetrics localDisplayMetrics = new DisplayMetrics();
      ((Activity)paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
      Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, (int)(localBitmap1.getWidth() * localDisplayMetrics.density), (int)(localBitmap1.getHeight() * localDisplayMetrics.density), true);
      localBitmap1.recycle();
      return localBitmap2;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static Bitmap getBitmapServerImage(Context paramContext, URL paramURL, GetBitmapOptions paramGetBitmapOptions)
  {
    try
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)paramURL.openConnection();
      localHttpURLConnection.setConnectTimeout(15000);
      localHttpURLConnection.setReadTimeout(15000);
      localObject = null;
      if (0 == 0)
      {
        localHttpURLConnection.connect();
        localObject = BitmapFactory.decodeStream(localHttpURLConnection.getInputStream());
      }
      if (paramGetBitmapOptions.scaleOption == GetBitmapOptions.ScaleOption.None)
        break label350;
      localScaleOption1 = paramGetBitmapOptions.scaleOption;
      localScaleOption2 = GetBitmapOptions.ScaleOption.Fit;
      localDisplayMetrics = null;
      if (localScaleOption1 != localScaleOption2)
      {
        localDisplayMetrics = new DisplayMetrics();
        ((Activity)paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
      }
      switch ($SWITCH_TABLE$com$nubee$coinpirates$common$GetBitmapOptions$ScaleOption()[paramGetBitmapOptions.scaleOption.ordinal()])
      {
      default:
        k = (int)(((Bitmap)localObject).getWidth() * localDisplayMetrics.density);
        m = (int)(((Bitmap)localObject).getHeight() * localDisplayMetrics.density);
        break;
        localBitmap = Bitmap.createScaledBitmap((Bitmap)localObject, k, m, true);
        ((Bitmap)localObject).recycle();
        localObject = localBitmap;
        break;
      case 3:
        k = paramGetBitmapOptions.width;
        m = paramGetBitmapOptions.height;
        break;
      case 4:
        i = ((Bitmap)localObject).getWidth();
        j = ((Bitmap)localObject).getHeight();
        f1 = paramGetBitmapOptions.width / i * localDisplayMetrics.density;
        f2 = paramGetBitmapOptions.height / j * localDisplayMetrics.density;
        if (f1 >= f2)
        {
          k = (int)(f2 * i);
          m = (int)(paramGetBitmapOptions.height * localDisplayMetrics.density);
        }
        else
        {
          f3 = paramGetBitmapOptions.width;
          f4 = localDisplayMetrics.density;
          k = (int)(f3 * f4);
          m = (int)(f1 * j);
        }
      }
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
      return null;
    }
    Object localObject;
    while (true)
    {
      GetBitmapOptions.ScaleOption localScaleOption1;
      GetBitmapOptions.ScaleOption localScaleOption2;
      DisplayMetrics localDisplayMetrics;
      int k;
      int m;
      Bitmap localBitmap;
      int i;
      int j;
      float f1;
      float f2;
      float f3;
      float f4;
      if (k <= 0)
        k = 1;
      if (m <= 0)
        m = 1;
    }
    label350: return localObject;
  }

  public static Bitmap getBitmapServerImage(String paramString)
  {
    try
    {
      Bitmap localBitmap = getBitmapServerImage(new URL(paramString));
      return localBitmap;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static Bitmap getBitmapServerImage(String paramString, int paramInt1, int paramInt2)
  {
    try
    {
      Bitmap localBitmap = getBitmapServerImage(new URL(paramString), paramInt1, paramInt2);
      return localBitmap;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static Bitmap getBitmapServerImage(URL paramURL)
  {
    try
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)paramURL.openConnection();
      localHttpURLConnection.setConnectTimeout(10000);
      localHttpURLConnection.setReadTimeout(10000);
      localHttpURLConnection.connect();
      BitmapFactory.Options localOptions = createDefaultOptions();
      Bitmap localBitmap = BitmapFactory.decodeStream(localHttpURLConnection.getInputStream(), null, localOptions);
      return localBitmap;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static Bitmap getBitmapServerImage(URL paramURL, int paramInt1, int paramInt2)
  {
    try
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)paramURL.openConnection();
      localHttpURLConnection.setConnectTimeout(10000);
      localHttpURLConnection.setReadTimeout(10000);
      localHttpURLConnection.connect();
      BitmapFactory.Options localOptions = createDefaultOptions();
      Bitmap localBitmap1 = BitmapFactory.decodeStream(localHttpURLConnection.getInputStream(), null, localOptions);
      if (localBitmap1 == null)
      {
        Coins7Log.e("getBitmapLocalImage1", paramURL + " is not loaded.");
        return null;
      }
      if (paramInt2 == 0)
        paramInt2 = calcHeight(localBitmap1, paramInt1);
      Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, paramInt1, paramInt2, true);
      localBitmap1.recycle();
      Coins7Log.e("getBitmapLocalImage1", paramURL + " is loaded.");
      return localBitmap2;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static Bitmap[] getBitmapsLocalImages(Context paramContext, String paramString1, String paramString2)
  {
    ArrayList localArrayList = new ArrayList();
    if (Environment.getExternalStorageState().equals("mounted"))
    {
      String str = String.valueOf(Environment.getExternalStorageDirectory().getPath()) + paramString1;
      String[] arrayOfString;
      int i;
      if (new File(str).exists())
      {
        arrayOfString = new File(str).list();
        i = 0;
        try
        {
          while (true)
          {
            int j = arrayOfString.length;
            if (j <= i)
            {
              Bitmap[] arrayOfBitmap = new Bitmap[localArrayList.size()];
              localArrayList.toArray(arrayOfBitmap);
              return arrayOfBitmap;
            }
            File localFile = new File(String.valueOf(str) + "/" + arrayOfString[i]);
            if (localFile.getName().startsWith(paramString2))
              localArrayList.add(getBitmapLocalImage(paramContext, localFile.getPath()));
            i++;
          }
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
          Coins7Log.e("getBitmapsLocalImages", "OutOfMemoryError");
          return null;
        }
      }
      Coins7Log.e("getBitmapsLocalImages", "directory is not exists: " + str);
      return null;
    }
    Coins7Log.e("getBitmapsLocalImages", "SDCard not inserted");
    return null;
  }

  public static Bitmap[] getBitmapsLocalImages(Context paramContext, String paramString, String[] paramArrayOfString)
  {
    if (Environment.getExternalStorageState().equals("mounted"))
    {
      String str = String.valueOf(Environment.getExternalStorageDirectory().getPath()) + paramString;
      Bitmap[] arrayOfBitmap;
      int i;
      if (new File(str).exists())
      {
        arrayOfBitmap = new Bitmap[paramArrayOfString.length];
        i = 0;
        try
        {
          while (true)
          {
            if (i >= paramArrayOfString.length)
              return arrayOfBitmap;
            arrayOfBitmap[i] = getBitmapLocalImage(paramContext, new File(String.valueOf(str) + "/" + paramArrayOfString[i]).getPath());
            Bitmap localBitmap = arrayOfBitmap[i];
            if (localBitmap == null)
              return null;
            i++;
          }
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
          Coins7Log.e("getBitmapsLocalImages", "OutOfMemoryError");
          return null;
        }
      }
      Coins7Log.e("getBitmapsLocalImages", "directory is not exists: " + str);
      return null;
    }
    Coins7Log.e("getBitmapsLocalImages", "SDCard not inserted");
    return null;
  }

  public static Bitmap[] getBitmapsLocalImagesScale(Context paramContext, String paramString, String[] paramArrayOfString, float paramFloat)
  {
    if (Environment.getExternalStorageState().equals("mounted"))
    {
      String str = String.valueOf(Environment.getExternalStorageDirectory().getPath()) + paramString;
      Bitmap[] arrayOfBitmap;
      Matrix localMatrix;
      int i;
      if (new File(str).exists())
      {
        arrayOfBitmap = new Bitmap[paramArrayOfString.length];
        localMatrix = new Matrix();
        localMatrix.postScale(paramFloat, paramFloat);
        i = 0;
        try
        {
          while (true)
          {
            if (i >= paramArrayOfString.length)
              return arrayOfBitmap;
            Bitmap localBitmap1 = getBitmapLocalImage(paramContext, new File(String.valueOf(str) + "/" + paramArrayOfString[i]).getPath());
            arrayOfBitmap[i] = Bitmap.createBitmap(localBitmap1, 0, 0, localBitmap1.getWidth(), localBitmap1.getHeight(), localMatrix, true);
            Bitmap localBitmap2 = arrayOfBitmap[i];
            if (localBitmap2 == null)
              return null;
            i++;
          }
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
          Coins7Log.e("getBitmapsLocalImages", "OutOfMemoryError");
          return null;
        }
      }
      Coins7Log.e("getBitmapsLocalImages", "directory is not exists: " + str);
      return null;
    }
    Coins7Log.e("getBitmapsLocalImages", "SDCard not inserted");
    return null;
  }

  public static String getDecryptString(String paramString1, String paramString2)
    throws Exception
  {
    byte[] arrayOfByte1 = getRawKey(paramString1.getBytes());
    byte[] arrayOfByte2 = toByte(paramString2);
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte1, "AES");
    Cipher localCipher = Cipher.getInstance("AES");
    localCipher.init(2, localSecretKeySpec);
    return new String(localCipher.doFinal(arrayOfByte2));
  }

  public static String getEncryptString(String paramString1, String paramString2)
    throws Exception
  {
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(getRawKey(paramString1.getBytes()), "AES");
    Cipher localCipher = Cipher.getInstance("AES");
    localCipher.init(1, localSecretKeySpec);
    return toHex(localCipher.doFinal(paramString2.getBytes()));
  }

  public static String getExternalStoragePath(String paramString)
  {
    if (Environment.getExternalStorageState().equals("mounted"))
      return String.valueOf(Environment.getExternalStorageDirectory().getPath()) + paramString;
    Coins7Log.e("getExternalStorageState", "SDCARD NOT MOUNTED.");
    return null;
  }

  public static String getFormatDate(String paramString)
  {
    Calendar localCalendar = Calendar.getInstance();
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    localSimpleDateFormat.setLenient(false);
    if (paramString != null)
      try
      {
        localCalendar.setTime(localSimpleDateFormat.parse(paramString));
        str1 = paramString.substring(5, 7);
        str2 = paramString.substring(8, 10);
        if (str1.startsWith("0"))
        {
          str3 = str1.substring(1);
          localStringBuilder = new StringBuilder(String.valueOf(str3)).append("月");
          if (!str2.startsWith("0"))
            break label138;
          str4 = str2.substring(1);
          return str4 + "日";
          return "";
        }
      }
      catch (ParseException localParseException)
      {
        return "";
      }
    while (true)
    {
      String str1;
      String str2;
      StringBuilder localStringBuilder;
      String str3 = str1;
      continue;
      label138: String str4 = str2;
    }
  }

  public static String getGmtDiff()
  {
    Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
    return "GMT" + DateFormat.format("z", localCalendar);
  }

  public static String getLanguageCode()
  {
    try
    {
      String str = Locale.getDefault().toString().substring(0, Locale.getDefault().toString().indexOf("_"));
      return str;
    }
    catch (Exception localException)
    {
    }
    return "en";
  }

  private static byte[] getRawKey(byte[] paramArrayOfByte)
    throws Exception
  {
    KeyGenerator localKeyGenerator = KeyGenerator.getInstance("AES");
    SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
    localSecureRandom.setSeed(paramArrayOfByte);
    localKeyGenerator.init(128, localSecureRandom);
    return localKeyGenerator.generateKey().getEncoded();
  }

  public static Bitmap getScaledBitmapDrawableBaseX(Context paramContext, int paramInt)
  {
    BitmapFactory.Options localOptions = createDefaultOptions();
    localOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(paramContext.getResources(), paramInt, localOptions);
    int i = ((Activity)paramContext).getWindowManager().getDefaultDisplay().getWidth();
    int j = (int)Math.floor(i * localOptions.outHeight / localOptions.outWidth);
    localOptions.inJustDecodeBounds = false;
    Bitmap localBitmap1 = BitmapFactory.decodeResource(paramContext.getResources(), paramInt, localOptions);
    if (localBitmap1 == null)
    {
      Coins7Log.e("getScaledBitmapDrawableBaseX", String.valueOf(paramInt) + " is not loaded.");
      return null;
    }
    Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, i, j, true);
    Coins7Log.e("getScaledBitmapDrawableBaseX", String.valueOf(paramInt) + " is loaded.");
    return localBitmap2;
  }

  public static Bitmap getScaledBitmapDrawableBaseY(Context paramContext, int paramInt)
  {
    BitmapFactory.Options localOptions = createDefaultOptions();
    localOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(paramContext.getResources(), paramInt, localOptions);
    int i = ((Activity)paramContext).getWindowManager().getDefaultDisplay().getHeight();
    int j = (int)Math.floor(i * localOptions.outWidth / localOptions.outHeight);
    localOptions.inJustDecodeBounds = false;
    Bitmap localBitmap1 = BitmapFactory.decodeResource(paramContext.getResources(), paramInt, localOptions);
    if (localBitmap1 == null)
    {
      Coins7Log.e("getScaledBitmapDrawableBaseY", String.valueOf(paramInt) + " is not loaded.");
      return null;
    }
    Bitmap localBitmap2 = Bitmap.createScaledBitmap(localBitmap1, j, i, true);
    Coins7Log.e("getScaledBitmapDrawableBaseY", String.valueOf(paramInt) + " is loaded.");
    return localBitmap2;
  }

  public static int getStringToInt(String paramString)
  {
    try
    {
      double d = Double.parseDouble(paramString);
      return (int)d;
    }
    catch (Exception localException)
    {
    }
    return 0;
  }

  public static boolean isBlank(String paramString)
  {
    int i = 0;
    int j = paramString.length();
    char[] arrayOfChar = paramString.toCharArray();
    while (true)
    {
      if ((i >= j) || ((arrayOfChar[i] > ' ') && (arrayOfChar[i] != '　')))
      {
        if (i != j)
          break;
        return true;
      }
      i++;
    }
    return false;
  }

  public static boolean isDayPassed(long paramLong)
  {
    long l = System.currentTimeMillis();
    if (l - paramLong > 86400000L);
    for (int i = 1; i == 0; i = 0)
      return false;
    Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
    localCalendar.setTimeInMillis(paramLong);
    int j = localCalendar.get(5);
    localCalendar.setTimeInMillis(l);
    if (j != localCalendar.get(5));
    for (int k = 1; k == 0; k = 0)
      return false;
    return true;
  }

  public static Hashtable<String, String> readCpuInfo()
  {
    localObject = "";
    try
    {
      InputStream localInputStream = new ProcessBuilder(new String[] { "/system/bin/cat", "/proc/cpuinfo" }).start().getInputStream();
      byte[] arrayOfByte = new byte[1024];
      while (true)
      {
        if (localInputStream.read(arrayOfByte) == -1)
        {
          localInputStream.close();
          arrayOfString1 = ((String)localObject).split("\n");
          localHashtable = new Hashtable();
          i = 0;
          if (i < arrayOfString1.length)
            break;
          return localHashtable;
        }
        String str = String.valueOf(localObject) + new String(arrayOfByte);
        localObject = str;
      }
    }
    catch (IOException localIOException)
    {
      while (true)
      {
        String[] arrayOfString1;
        Hashtable localHashtable;
        int i;
        localIOException.printStackTrace();
        continue;
        String[] arrayOfString2 = arrayOfString1[i].split(":");
        if (arrayOfString2.length >= 2)
          localHashtable.put(arrayOfString2[0].trim(), arrayOfString2[1].trim());
        i++;
      }
    }
  }

  public static void showDialog(Activity paramActivity, String paramString1, String paramString2)
  {
    try
    {
      new AlertDialog.Builder(paramActivity).setTitle(paramString1).setMessage(paramString2).setPositiveButton(2131165186, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          CommonConfig.this.finish();
        }
      }).show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public static void showDialogFinish(Activity paramActivity, String paramString)
  {
    try
    {
      new AlertDialog.Builder(paramActivity).setMessage(paramString).setPositiveButton(2131165186, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          CommonConfig.this.finish();
        }
      }).create().show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public static void showDialogForNothing(Activity paramActivity, String paramString)
  {
    try
    {
      new AlertDialog.Builder(paramActivity).setMessage(paramString).setPositiveButton(2131165186, null).create().show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public static void showDialogForNothing(Activity paramActivity, String paramString1, String paramString2)
  {
    try
    {
      new AlertDialog.Builder(paramActivity).setTitle(paramString1).setMessage(paramString2).setPositiveButton(2131165186, null).create().show();
      return;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
  }

  public static void showToastMessage(Context paramContext, String paramString, int paramInt)
  {
    Toast.makeText(paramContext, paramString, paramInt).show();
  }

  public static void showToastMessage(Context paramContext, String paramString, int paramInt1, int paramInt2)
  {
    new Toast(paramContext);
    Toast localToast = Toast.makeText(paramContext, paramString, paramInt1);
    localToast.setGravity(49, 0, paramInt2);
    localToast.show();
  }

  private static byte[] toByte(String paramString)
  {
    int i = paramString.length() / 2;
    byte[] arrayOfByte = new byte[i];
    for (int j = 0; ; j++)
    {
      if (j >= i)
        return arrayOfByte;
      arrayOfByte[j] = Integer.valueOf(paramString.substring(j * 2, 2 + j * 2), 16).byteValue();
    }
  }

  private static String toHex(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte == null)
      return "";
    StringBuffer localStringBuffer = new StringBuffer(2 * paramArrayOfByte.length);
    for (int i = 0; ; i++)
    {
      if (i >= paramArrayOfByte.length)
        return localStringBuffer.toString();
      localStringBuffer.append("0123456789ABCDEF".charAt(0xF & paramArrayOfByte[i] >> 4)).append("0123456789ABCDEF".charAt(0xF & paramArrayOfByte[i]));
    }
  }

  public static String toSha256(String paramString)
  {
    localStringBuilder = new StringBuilder();
    try
    {
      byte[] arrayOfByte = MessageDigest.getInstance("SHA256").digest(paramString.getBytes());
      int i = arrayOfByte.length;
      for (int j = 0; ; j++)
      {
        if (j >= i)
          return localStringBuilder.toString();
        int k = arrayOfByte[j];
        if (k < 0)
          k += 256;
        if (k < 10)
          localStringBuilder.append("0");
        localStringBuilder.append(Integer.toString(k, 16));
      }
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      while (true)
        Coins7Log.e("CommonConfig", localNoSuchAlgorithmException.getMessage(), localNoSuchAlgorithmException);
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.CommonConfig
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */