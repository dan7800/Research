package com.tapjoy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class TapjoyConnect
{
  static final String CONTAINS_EXTERNAL_DATA = "containsExternalData";
  static final String EMULATOR_DEVICE_ID = "emulatorDeviceId";
  static final String EXTRA_CLIENT_PACKAGE = "CLIENT_PACKAGE";
  static final String EXTRA_FEATURED_APP_FULLSCREEN_AD_URL = "FULLSCREEN_AD_URL";
  static final String EXTRA_URL_BASE = "URL_BASE";
  static final String EXTRA_URL_PARAMS = "URL_PARAMS";
  static final String EXTRA_USER_ID = "USER_ID";
  static final String REFERRAL_URL = "InstallReferral";
  static final String TAPJOY_CONNECT = "TapjoyConnect";
  static final String TAPJOY_PRIMARY_COLOR = "tapjoyPrimaryColor";
  static final String TJC_BASE_REDIRECT_DOMAIN = "ws.tapjoyads.com";
  static final String TJC_CONNECT_URL_PATH = "connect?";
  static final String TJC_DEVICE_PLATFORM_TYPE = "android";
  public static final String TJC_LIBRARY_VERSION_NUMBER = "7.1.1";
  static final String TJC_PREFERENCE = "tjcPrefrences";
  static final String TJC_SERVICE_URL = "https://ws.tapjoyads.com/";
  static final String TJC_SPEND_POINTS_URL_PATH = "purchase_vg/spend?";
  static final String TJC_USERDATA_URL_PATH = "get_vg_store_items/user_account?";
  private static TapjoyConnect tapjoyConnectInstance = null;
  private static TapjoyDisplayAd tapjoyDisplayAd = null;
  private static TapjoyFeaturedApp tapjoyFeaturedApp = null;
  private static TapjoyNotifier tapjoyNotifier;
  private static TapjoySpendPointsNotifier tapjoySpendPointsNotifier;
  private static TapjoyURLConnection tapjoyURLConnection = null;
  final String TJC_APP_ID_NAME = "app_id";
  final String TJC_APP_VERSION_NAME = "app_version";
  final String TJC_CONNECT_LIBRARY_VERSION_NAME = "library_version";
  final String TJC_DEVICE_COUNTRY_CODE = "country_code";
  final String TJC_DEVICE_ID_NAME = "udid";
  final String TJC_DEVICE_LANGUAGE = "language";
  final String TJC_DEVICE_NAME = "device_name";
  final String TJC_DEVICE_OS_VERSION_NAME = "os_version";
  final String TJC_DEVICE_SCREEN_DENSITY = "screen_density";
  final String TJC_DEVICE_SCREEN_LAYOUT_SIZE = "screen_layout_size";
  final String TJC_DEVICE_TYPE_NAME = "device_type";
  final String TJC_MULTIPLE_CURRENCY_ID = "currency_id";
  final String TJC_MULTIPLE_CURRENCY_SELECTOR_FLAG = "currency_selector";
  final String TJC_SPEND_TAP_POINTS = "tap_points";
  final String TJC_USER_ID = "publisher_user_id";
  private String actionURLParams = "";
  private String appID = "";
  private String appVersion = "";
  private String clientPackage = "";
  private ConnectGetPointsTask connectGetPointsTask = null;
  private ConnectSpendPointsTask connectSpendPointsTask = null;
  private ConnectTask connectTask = null;
  private Context context = null;
  private String deviceCountryCode = "";
  private String deviceID = "";
  private String deviceLanguage = "";
  private String deviceName = "";
  private String deviceOSVersion = "";
  private String deviceScreenDensity = "";
  private String deviceScreenLayoutSize = "";
  private String deviceType = "";
  private String deviceUserID = "";
  private String libraryVersion = "";
  private String multipleCurrencyID = "";
  private String multipleCurrencySelector = "";
  private PayPerActionTask payPerActionTask = null;
  private String referralURL = "";
  private String spendTapPoints = "";
  private String urlParams = "";

  private TapjoyConnect(Context paramContext)
  {
    this.context = paramContext;
    initMetaData();
    this.urlParams = (String.valueOf(this.urlParams) + "udid=" + this.deviceID + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "device_name=" + this.deviceName + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "device_type=" + this.deviceType + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "os_version=" + this.deviceOSVersion + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "country_code=" + this.deviceCountryCode + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "language=" + this.deviceLanguage + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "app_id=" + this.appID + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "app_version=" + this.appVersion + "&");
    this.urlParams = (String.valueOf(this.urlParams) + "library_version=" + this.libraryVersion);
    if ((this.deviceScreenDensity.length() > 0) && (this.deviceScreenLayoutSize.length() > 0))
    {
      this.urlParams = (String.valueOf(this.urlParams) + "&");
      this.urlParams = (String.valueOf(this.urlParams) + "screen_density=" + this.deviceScreenDensity + "&");
      this.urlParams = (String.valueOf(this.urlParams) + "screen_layout_size=" + this.deviceScreenLayoutSize);
    }
    TapjoyLog.i("TapjoyConnect", "URL parameters: " + this.urlParams);
    this.connectTask = new ConnectTask(null);
    this.connectTask.execute(new Void[0]);
  }

  private Document buildDocument(String paramString)
  {
    try
    {
      DocumentBuilderFactory localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramString.getBytes("UTF-8"));
      Document localDocument = localDocumentBuilderFactory.newDocumentBuilder().parse(localByteArrayInputStream);
      return localDocument;
    }
    catch (Exception localException)
    {
      TapjoyLog.e("TapjoyConnect", "buildDocument exception: " + localException.toString());
    }
    return null;
  }

  private String getNodeTrimValue(NodeList paramNodeList)
  {
    Element localElement = (Element)paramNodeList.item(0);
    String str = "";
    if (localElement != null)
    {
      NodeList localNodeList = localElement.getChildNodes();
      int i = localNodeList.getLength();
      for (int j = 0; ; j++)
      {
        if (j >= i)
        {
          if ((str == null) || (str.equals("")))
            break;
          return str.trim();
        }
        Node localNode = localNodeList.item(j);
        if (localNode != null)
          str = String.valueOf(str) + localNode.getNodeValue();
      }
      return null;
    }
    return null;
  }

  private void getTapPointsHelper()
  {
    this.connectGetPointsTask = new ConnectGetPointsTask(null);
    this.connectGetPointsTask.execute(new Void[0]);
  }

  public static TapjoyConnect getTapjoyConnectInstance(Context paramContext)
  {
    if (tapjoyURLConnection == null)
      tapjoyURLConnection = new TapjoyURLConnection();
    if (tapjoyConnectInstance == null)
      tapjoyConnectInstance = new TapjoyConnect(paramContext);
    if (tapjoyFeaturedApp == null)
      tapjoyFeaturedApp = new TapjoyFeaturedApp(paramContext);
    if (tapjoyDisplayAd == null)
      tapjoyDisplayAd = new TapjoyDisplayAd(paramContext);
    return tapjoyConnectInstance;
  }

  private boolean handleConnectResponse(String paramString)
  {
    Document localDocument = buildDocument(paramString);
    if (localDocument != null)
    {
      String str = getNodeTrimValue(localDocument.getElementsByTagName("Success"));
      if ((str != null) && (str.equals("true")))
      {
        TapjoyLog.i("TapjoyConnect", "Successfully connected to tapjoy site.");
        return true;
      }
      TapjoyLog.e("TapjoyConnect", "Tapjoy Connect call failed.");
    }
    return false;
  }

  private boolean handleGetPointsResponse(String paramString)
  {
    Document localDocument = buildDocument(paramString);
    if (localDocument != null)
    {
      String str1 = getNodeTrimValue(localDocument.getElementsByTagName("Success"));
      if ((str1 == null) || (!str1.equals("true")))
        break label105;
      String str2 = getNodeTrimValue(localDocument.getElementsByTagName("TapPoints"));
      String str3 = getNodeTrimValue(localDocument.getElementsByTagName("CurrencyName"));
      if ((str2 != null) && (str3 != null))
      {
        tapjoyNotifier.getUpdatePoints(str3, Integer.parseInt(str2));
        return true;
      }
      TapjoyLog.e("TapjoyConnect", "Invalid XML: Missing tags.");
    }
    while (true)
    {
      return false;
      label105: TapjoyLog.e("TapjoyConnect", "Invalid XML: Missing <Success> tag.");
    }
  }

  private boolean handlePayPerActionResponse(String paramString)
  {
    Document localDocument = buildDocument(paramString);
    if (localDocument != null)
    {
      String str = getNodeTrimValue(localDocument.getElementsByTagName("Success"));
      if ((str != null) && (str.equals("true")))
      {
        TapjoyLog.i("TapjoyConnect", "Successfully sent completed Pay-Per-Action to Tapjoy server.");
        return true;
      }
      TapjoyLog.e("TapjoyConnect", "Completed Pay-Per-Action call failed.");
    }
    return false;
  }

  private boolean handleSpendPointsResponse(String paramString)
  {
    Document localDocument = buildDocument(paramString);
    String str1;
    if (localDocument != null)
    {
      str1 = getNodeTrimValue(localDocument.getElementsByTagName("Success"));
      if ((str1 == null) || (!str1.equals("true")))
        break label105;
      String str3 = getNodeTrimValue(localDocument.getElementsByTagName("TapPoints"));
      String str4 = getNodeTrimValue(localDocument.getElementsByTagName("CurrencyName"));
      if ((str3 != null) && (str4 != null))
      {
        tapjoySpendPointsNotifier.getSpendPointsResponse(str4, Integer.parseInt(str3));
        return true;
      }
      TapjoyLog.e("TapjoyConnect", "Invalid XML: Missing tags.");
    }
    while (true)
    {
      return false;
      label105: if ((str1 != null) && (str1.endsWith("false")))
      {
        String str2 = getNodeTrimValue(localDocument.getElementsByTagName("Message"));
        TapjoyLog.i("TapjoyConnect", str2);
        tapjoySpendPointsNotifier.getSpendPointsResponseFailed(str2);
        return true;
      }
      TapjoyLog.e("TapjoyConnect", "Invalid XML: Missing <Success> tag.");
    }
  }

  // ERROR //
  private void initMetaData()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   4: invokevirtual 492	android/content/Context:getPackageManager	()Landroid/content/pm/PackageManager;
    //   7: astore_1
    //   8: aload_1
    //   9: aload_0
    //   10: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   13: invokevirtual 495	android/content/Context:getPackageName	()Ljava/lang/String;
    //   16: sipush 128
    //   19: invokevirtual 501	android/content/pm/PackageManager:getApplicationInfo	(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;
    //   22: astore_3
    //   23: aload_3
    //   24: ifnull +1093 -> 1117
    //   27: aload_3
    //   28: getfield 507	android/content/pm/ApplicationInfo:metaData	Landroid/os/Bundle;
    //   31: ifnull +1086 -> 1117
    //   34: aload_3
    //   35: getfield 507	android/content/pm/ApplicationInfo:metaData	Landroid/os/Bundle;
    //   38: ldc_w 509
    //   41: invokevirtual 515	android/os/Bundle:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   44: astore 4
    //   46: aload 4
    //   48: ifnull +694 -> 742
    //   51: aload 4
    //   53: ldc 135
    //   55: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   58: ifne +684 -> 742
    //   61: aload_0
    //   62: aload 4
    //   64: invokevirtual 402	java/lang/String:trim	()Ljava/lang/String;
    //   67: putfield 149	com/tapjoy/TapjoyConnect:appID	Ljava/lang/String;
    //   70: aload_3
    //   71: getfield 507	android/content/pm/ApplicationInfo:metaData	Landroid/os/Bundle;
    //   74: ldc 14
    //   76: invokevirtual 515	android/os/Bundle:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   79: astore 5
    //   81: aload 5
    //   83: ifnull +678 -> 761
    //   86: aload 5
    //   88: ldc 135
    //   90: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   93: ifne +668 -> 761
    //   96: aload_0
    //   97: aload 5
    //   99: putfield 167	com/tapjoy/TapjoyConnect:clientPackage	Ljava/lang/String;
    //   102: aload_0
    //   103: aload_1
    //   104: aload_0
    //   105: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   108: invokevirtual 495	android/content/Context:getPackageName	()Ljava/lang/String;
    //   111: iconst_0
    //   112: invokevirtual 519	android/content/pm/PackageManager:getPackageInfo	(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
    //   115: getfield 524	android/content/pm/PackageInfo:versionName	Ljava/lang/String;
    //   118: putfield 151	com/tapjoy/TapjoyConnect:appVersion	Ljava/lang/String;
    //   121: aload_0
    //   122: ldc 44
    //   124: putfield 141	com/tapjoy/TapjoyConnect:deviceType	Ljava/lang/String;
    //   127: aload_0
    //   128: getstatic 529	android/os/Build:MODEL	Ljava/lang/String;
    //   131: putfield 139	com/tapjoy/TapjoyConnect:deviceName	Ljava/lang/String;
    //   134: aload_0
    //   135: getstatic 534	android/os/Build$VERSION:RELEASE	Ljava/lang/String;
    //   138: putfield 143	com/tapjoy/TapjoyConnect:deviceOSVersion	Ljava/lang/String;
    //   141: aload_0
    //   142: invokestatic 540	java/util/Locale:getDefault	()Ljava/util/Locale;
    //   145: invokevirtual 543	java/util/Locale:getCountry	()Ljava/lang/String;
    //   148: putfield 145	com/tapjoy/TapjoyConnect:deviceCountryCode	Ljava/lang/String;
    //   151: aload_0
    //   152: invokestatic 540	java/util/Locale:getDefault	()Ljava/util/Locale;
    //   155: invokevirtual 546	java/util/Locale:getLanguage	()Ljava/lang/String;
    //   158: putfield 147	com/tapjoy/TapjoyConnect:deviceLanguage	Ljava/lang/String;
    //   161: aload_0
    //   162: ldc 47
    //   164: putfield 153	com/tapjoy/TapjoyConnect:libraryVersion	Ljava/lang/String;
    //   167: aload_0
    //   168: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   171: ldc 50
    //   173: iconst_0
    //   174: invokevirtual 550	android/content/Context:getSharedPreferences	(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    //   177: astore 6
    //   179: aload_3
    //   180: getfield 507	android/content/pm/ApplicationInfo:metaData	Landroid/os/Bundle;
    //   183: ldc_w 552
    //   186: invokevirtual 515	android/os/Bundle:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   189: astore 7
    //   191: aload 7
    //   193: ifnull +577 -> 770
    //   196: aload 7
    //   198: ldc 135
    //   200: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   203: ifne +567 -> 770
    //   206: aload_0
    //   207: aload 7
    //   209: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   212: ldc 32
    //   214: new 244	java/lang/StringBuilder
    //   217: dup
    //   218: ldc_w 554
    //   221: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   224: aload_0
    //   225: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   228: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   231: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   234: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   237: new 556	android/util/DisplayMetrics
    //   240: dup
    //   241: invokespecial 557	android/util/DisplayMetrics:<init>	()V
    //   244: astore 9
    //   246: aload_0
    //   247: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   250: ldc_w 559
    //   253: invokevirtual 563	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
    //   256: checkcast 565	android/view/WindowManager
    //   259: invokeinterface 569 1 0
    //   264: aload 9
    //   266: invokevirtual 575	android/view/Display:getMetrics	(Landroid/util/DisplayMetrics;)V
    //   269: aload_0
    //   270: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   273: invokevirtual 579	android/content/Context:getResources	()Landroid/content/res/Resources;
    //   276: invokevirtual 585	android/content/res/Resources:getConfiguration	()Landroid/content/res/Configuration;
    //   279: astore 12
    //   281: aload_0
    //   282: new 244	java/lang/StringBuilder
    //   285: dup
    //   286: invokespecial 586	java/lang/StringBuilder:<init>	()V
    //   289: aload 9
    //   291: getfield 590	android/util/DisplayMetrics:densityDpi	I
    //   294: invokevirtual 593	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   297: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   300: putfield 157	com/tapjoy/TapjoyConnect:deviceScreenDensity	Ljava/lang/String;
    //   303: aload_0
    //   304: new 244	java/lang/StringBuilder
    //   307: dup
    //   308: invokespecial 586	java/lang/StringBuilder:<init>	()V
    //   311: bipush 15
    //   313: aload 12
    //   315: getfield 598	android/content/res/Configuration:screenLayout	I
    //   318: iand
    //   319: invokevirtual 593	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   322: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   325: putfield 159	com/tapjoy/TapjoyConnect:deviceScreenLayoutSize	Ljava/lang/String;
    //   328: aload 6
    //   330: ldc 29
    //   332: aconst_null
    //   333: invokeinterface 603 3 0
    //   338: astore 11
    //   340: aload 11
    //   342: ifnull +19 -> 361
    //   345: aload 11
    //   347: ldc 135
    //   349: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   352: ifne +9 -> 361
    //   355: aload_0
    //   356: aload 11
    //   358: putfield 171	com/tapjoy/TapjoyConnect:referralURL	Ljava/lang/String;
    //   361: ldc 32
    //   363: ldc_w 605
    //   366: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   369: ldc 32
    //   371: new 244	java/lang/StringBuilder
    //   374: dup
    //   375: ldc_w 607
    //   378: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   381: aload_0
    //   382: getfield 149	com/tapjoy/TapjoyConnect:appID	Ljava/lang/String;
    //   385: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   388: ldc_w 609
    //   391: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   394: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   397: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   400: ldc 32
    //   402: new 244	java/lang/StringBuilder
    //   405: dup
    //   406: ldc_w 611
    //   409: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   412: aload_0
    //   413: getfield 167	com/tapjoy/TapjoyConnect:clientPackage	Ljava/lang/String;
    //   416: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   419: ldc_w 609
    //   422: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   425: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   428: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   431: ldc 32
    //   433: new 244	java/lang/StringBuilder
    //   436: dup
    //   437: ldc_w 613
    //   440: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   443: aload_0
    //   444: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   447: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   450: ldc_w 609
    //   453: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   456: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   459: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   462: ldc 32
    //   464: new 244	java/lang/StringBuilder
    //   467: dup
    //   468: ldc_w 615
    //   471: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   474: aload_0
    //   475: getfield 139	com/tapjoy/TapjoyConnect:deviceName	Ljava/lang/String;
    //   478: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   481: ldc_w 609
    //   484: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   487: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   490: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   493: ldc 32
    //   495: new 244	java/lang/StringBuilder
    //   498: dup
    //   499: ldc_w 617
    //   502: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   505: aload_0
    //   506: getfield 141	com/tapjoy/TapjoyConnect:deviceType	Ljava/lang/String;
    //   509: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   512: ldc_w 609
    //   515: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   518: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   521: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   524: ldc 32
    //   526: new 244	java/lang/StringBuilder
    //   529: dup
    //   530: ldc_w 619
    //   533: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   536: aload_0
    //   537: getfield 153	com/tapjoy/TapjoyConnect:libraryVersion	Ljava/lang/String;
    //   540: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   543: ldc_w 609
    //   546: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   549: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   552: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   555: ldc 32
    //   557: new 244	java/lang/StringBuilder
    //   560: dup
    //   561: ldc_w 621
    //   564: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   567: aload_0
    //   568: getfield 143	com/tapjoy/TapjoyConnect:deviceOSVersion	Ljava/lang/String;
    //   571: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   574: ldc_w 609
    //   577: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   580: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   583: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   586: ldc 32
    //   588: new 244	java/lang/StringBuilder
    //   591: dup
    //   592: ldc_w 623
    //   595: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   598: aload_0
    //   599: getfield 145	com/tapjoy/TapjoyConnect:deviceCountryCode	Ljava/lang/String;
    //   602: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   605: ldc_w 609
    //   608: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   611: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   614: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   617: ldc 32
    //   619: new 244	java/lang/StringBuilder
    //   622: dup
    //   623: ldc_w 625
    //   626: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   629: aload_0
    //   630: getfield 147	com/tapjoy/TapjoyConnect:deviceLanguage	Ljava/lang/String;
    //   633: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   636: ldc_w 609
    //   639: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   642: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   645: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   648: ldc 32
    //   650: new 244	java/lang/StringBuilder
    //   653: dup
    //   654: ldc_w 627
    //   657: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   660: aload_0
    //   661: getfield 157	com/tapjoy/TapjoyConnect:deviceScreenDensity	Ljava/lang/String;
    //   664: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   667: ldc_w 609
    //   670: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   673: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   676: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   679: ldc 32
    //   681: new 244	java/lang/StringBuilder
    //   684: dup
    //   685: ldc_w 629
    //   688: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   691: aload_0
    //   692: getfield 159	com/tapjoy/TapjoyConnect:deviceScreenLayoutSize	Ljava/lang/String;
    //   695: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   698: ldc_w 609
    //   701: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   704: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   707: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   710: ldc 32
    //   712: new 244	java/lang/StringBuilder
    //   715: dup
    //   716: ldc_w 631
    //   719: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   722: aload_0
    //   723: getfield 171	com/tapjoy/TapjoyConnect:referralURL	Ljava/lang/String;
    //   726: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   729: ldc_w 609
    //   732: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   735: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   738: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   741: return
    //   742: ldc 32
    //   744: ldc_w 633
    //   747: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   750: return
    //   751: astore_2
    //   752: ldc 32
    //   754: ldc_w 635
    //   757: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   760: return
    //   761: ldc 32
    //   763: ldc_w 637
    //   766: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   769: return
    //   770: aload_0
    //   771: getfield 133	com/tapjoy/TapjoyConnect:context	Landroid/content/Context;
    //   774: ldc_w 639
    //   777: invokevirtual 563	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
    //   780: checkcast 641	android/telephony/TelephonyManager
    //   783: astore 13
    //   785: aload 13
    //   787: ifnull +12 -> 799
    //   790: aload_0
    //   791: aload 13
    //   793: invokevirtual 644	android/telephony/TelephonyManager:getDeviceId	()Ljava/lang/String;
    //   796: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   799: ldc 32
    //   801: new 244	java/lang/StringBuilder
    //   804: dup
    //   805: ldc_w 646
    //   808: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   811: aload_0
    //   812: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   815: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   818: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   821: invokestatic 297	com/tapjoy/TapjoyLog:i	(Ljava/lang/String;Ljava/lang/String;)V
    //   824: aload_0
    //   825: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   828: ifnonnull +109 -> 937
    //   831: ldc 32
    //   833: ldc_w 648
    //   836: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   839: iconst_1
    //   840: istore 14
    //   842: iload 14
    //   844: ifeq -607 -> 237
    //   847: new 650	java/lang/StringBuffer
    //   850: dup
    //   851: invokespecial 651	java/lang/StringBuffer:<init>	()V
    //   854: astore 15
    //   856: aload 15
    //   858: ldc_w 653
    //   861: invokevirtual 656	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   864: pop
    //   865: aload 6
    //   867: ldc 11
    //   869: aconst_null
    //   870: invokeinterface 603 3 0
    //   875: astore 17
    //   877: aload 17
    //   879: ifnull +247 -> 1126
    //   882: aload 17
    //   884: ldc 135
    //   886: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   889: ifne +237 -> 1126
    //   892: aload_0
    //   893: aload 17
    //   895: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   898: goto -661 -> 237
    //   901: astore 8
    //   903: ldc 32
    //   905: new 244	java/lang/StringBuilder
    //   908: dup
    //   909: ldc_w 658
    //   912: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   915: aload 8
    //   917: invokevirtual 375	java/lang/Exception:toString	()Ljava/lang/String;
    //   920: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   923: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   926: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   929: aload_0
    //   930: aconst_null
    //   931: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   934: goto -697 -> 237
    //   937: aload_0
    //   938: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   941: invokevirtual 285	java/lang/String:length	()I
    //   944: ifeq +29 -> 973
    //   947: aload_0
    //   948: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   951: ldc_w 660
    //   954: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   957: ifne +16 -> 973
    //   960: aload_0
    //   961: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   964: ldc_w 662
    //   967: invokevirtual 399	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   970: ifeq +17 -> 987
    //   973: ldc 32
    //   975: ldc_w 664
    //   978: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   981: iconst_1
    //   982: istore 14
    //   984: goto -142 -> 842
    //   987: aload_0
    //   988: aload_0
    //   989: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   992: invokevirtual 667	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   995: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   998: iconst_0
    //   999: istore 14
    //   1001: goto -159 -> 842
    //   1004: iload 18
    //   1006: bipush 32
    //   1008: if_icmplt +49 -> 1057
    //   1011: aload_0
    //   1012: aload 15
    //   1014: invokevirtual 668	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1017: invokevirtual 667	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   1020: putfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   1023: aload 6
    //   1025: invokeinterface 672 1 0
    //   1030: astore 19
    //   1032: aload 19
    //   1034: ldc 11
    //   1036: aload_0
    //   1037: getfield 137	com/tapjoy/TapjoyConnect:deviceID	Ljava/lang/String;
    //   1040: invokeinterface 678 3 0
    //   1045: pop
    //   1046: aload 19
    //   1048: invokeinterface 682 1 0
    //   1053: pop
    //   1054: goto -817 -> 237
    //   1057: aload 15
    //   1059: ldc_w 684
    //   1062: ldc2_w 685
    //   1065: invokestatic 692	java/lang/Math:random	()D
    //   1068: dmul
    //   1069: d2i
    //   1070: bipush 30
    //   1072: irem
    //   1073: invokevirtual 696	java/lang/String:charAt	(I)C
    //   1076: invokevirtual 699	java/lang/StringBuffer:append	(C)Ljava/lang/StringBuffer;
    //   1079: pop
    //   1080: iinc 18 1
    //   1083: goto -79 -> 1004
    //   1086: astore 10
    //   1088: ldc 32
    //   1090: new 244	java/lang/StringBuilder
    //   1093: dup
    //   1094: ldc_w 701
    //   1097: invokespecial 253	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   1100: aload 10
    //   1102: invokevirtual 375	java/lang/Exception:toString	()Ljava/lang/String;
    //   1105: invokevirtual 259	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1108: invokevirtual 265	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1111: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   1114: goto -786 -> 328
    //   1117: ldc 32
    //   1119: ldc_w 635
    //   1122: invokestatic 378	com/tapjoy/TapjoyLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   1125: return
    //   1126: iconst_0
    //   1127: istore 18
    //   1129: goto -125 -> 1004
    //
    // Exception table:
    //   from	to	target	type
    //   8	23	751	android/content/pm/PackageManager$NameNotFoundException
    //   27	46	751	android/content/pm/PackageManager$NameNotFoundException
    //   51	81	751	android/content/pm/PackageManager$NameNotFoundException
    //   86	191	751	android/content/pm/PackageManager$NameNotFoundException
    //   196	237	751	android/content/pm/PackageManager$NameNotFoundException
    //   237	328	751	android/content/pm/PackageManager$NameNotFoundException
    //   328	340	751	android/content/pm/PackageManager$NameNotFoundException
    //   345	361	751	android/content/pm/PackageManager$NameNotFoundException
    //   361	741	751	android/content/pm/PackageManager$NameNotFoundException
    //   742	750	751	android/content/pm/PackageManager$NameNotFoundException
    //   761	769	751	android/content/pm/PackageManager$NameNotFoundException
    //   770	785	751	android/content/pm/PackageManager$NameNotFoundException
    //   790	799	751	android/content/pm/PackageManager$NameNotFoundException
    //   799	839	751	android/content/pm/PackageManager$NameNotFoundException
    //   847	877	751	android/content/pm/PackageManager$NameNotFoundException
    //   882	898	751	android/content/pm/PackageManager$NameNotFoundException
    //   903	934	751	android/content/pm/PackageManager$NameNotFoundException
    //   937	973	751	android/content/pm/PackageManager$NameNotFoundException
    //   973	981	751	android/content/pm/PackageManager$NameNotFoundException
    //   987	998	751	android/content/pm/PackageManager$NameNotFoundException
    //   1011	1054	751	android/content/pm/PackageManager$NameNotFoundException
    //   1057	1080	751	android/content/pm/PackageManager$NameNotFoundException
    //   1088	1114	751	android/content/pm/PackageManager$NameNotFoundException
    //   1117	1125	751	android/content/pm/PackageManager$NameNotFoundException
    //   770	785	901	java/lang/Exception
    //   790	799	901	java/lang/Exception
    //   799	839	901	java/lang/Exception
    //   847	877	901	java/lang/Exception
    //   882	898	901	java/lang/Exception
    //   937	973	901	java/lang/Exception
    //   973	981	901	java/lang/Exception
    //   987	998	901	java/lang/Exception
    //   1011	1054	901	java/lang/Exception
    //   1057	1080	901	java/lang/Exception
    //   237	328	1086	java/lang/Exception
  }

  private void spendTapPointsHelper()
  {
    this.connectSpendPointsTask = new ConnectSpendPointsTask(null);
    this.connectSpendPointsTask.execute(new Void[0]);
  }

  public void actionComplete(String paramString)
  {
    this.actionURLParams = "";
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "udid=" + this.deviceID + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "device_name=" + this.deviceName + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "device_type=" + this.deviceType + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "os_version=" + this.deviceOSVersion + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "country_code=" + this.deviceCountryCode + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "language=" + this.deviceLanguage + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "app_id=" + paramString + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "app_version=" + this.appVersion + "&");
    this.actionURLParams = (String.valueOf(this.actionURLParams) + "library_version=" + this.libraryVersion);
    if ((this.deviceScreenDensity.length() > 0) && (this.deviceScreenLayoutSize.length() > 0))
    {
      this.urlParams = (String.valueOf(this.urlParams) + "&");
      this.urlParams = (String.valueOf(this.urlParams) + "screen_density=" + this.deviceScreenDensity + "&");
      this.urlParams = (String.valueOf(this.urlParams) + "screen_layout_size=" + this.deviceScreenLayoutSize);
    }
    TapjoyLog.i("TapjoyConnect", "PPA URL parameters: " + this.actionURLParams);
    this.payPerActionTask = new PayPerActionTask(null);
    this.payPerActionTask.execute(new Void[0]);
  }

  public void finalize()
  {
    tapjoyConnectInstance = null;
    TapjoyLog.i("TapjoyConnect", "Cleaning resources.");
  }

  public void getDisplayAd(Context paramContext, TapjoyDisplayAdNotifier paramTapjoyDisplayAdNotifier)
  {
    String str = this.urlParams;
    tapjoyDisplayAd.getDisplayAdDataFromServer("https://ws.tapjoyads.com/", str, paramTapjoyDisplayAdNotifier);
  }

  public void getFeaturedApp(Context paramContext, TapjoyFeaturedAppNotifier paramTapjoyFeaturedAppNotifier)
  {
    getFeaturedApp(paramContext, this.deviceID, paramTapjoyFeaturedAppNotifier);
  }

  public void getFeaturedApp(Context paramContext, String paramString, TapjoyFeaturedAppNotifier paramTapjoyFeaturedAppNotifier)
  {
    TapjoyLog.i("TapjoyConnect", "Get Featured App (userID = " + paramString + ")");
    this.deviceUserID = paramString;
    String str = String.valueOf(this.urlParams) + "&publisher_user_id=" + paramString;
    tapjoyFeaturedApp.getFeaturedAppDataFromServer("https://ws.tapjoyads.com/", str, paramTapjoyFeaturedAppNotifier);
  }

  public void getFeaturedAppWithCurrencyID(Context paramContext, String paramString1, String paramString2, TapjoyFeaturedAppNotifier paramTapjoyFeaturedAppNotifier)
  {
    TapjoyLog.i("TapjoyConnect", "Get Featured App with currencyID: " + paramString2 + " (userID = " + paramString1 + ")");
    this.deviceUserID = paramString1;
    this.multipleCurrencyID = paramString2;
    String str = String.valueOf(new StringBuilder(String.valueOf(this.urlParams)).append("&publisher_user_id=").append(paramString1).toString()) + "&currency_id=" + this.multipleCurrencyID;
    tapjoyFeaturedApp.getFeaturedAppDataFromServer("https://ws.tapjoyads.com/", str, paramTapjoyFeaturedAppNotifier);
  }

  public void getTapPoints(TapjoyNotifier paramTapjoyNotifier)
  {
    if (tapjoyConnectInstance != null)
    {
      tapjoyNotifier = paramTapjoyNotifier;
      tapjoyConnectInstance.getTapPointsHelper();
    }
  }

  public void setFeaturedAppDisplayCount(int paramInt)
  {
    tapjoyFeaturedApp.setDisplayCount(paramInt);
  }

  public void showFeaturedAppFullScreenAd(Context paramContext)
  {
    String str = "";
    if (tapjoyFeaturedApp.getFeaturedAppObject() != null)
      str = tapjoyFeaturedApp.getFeaturedAppObject().fullScreenAdURL;
    TapjoyLog.i("TapjoyConnect", "Displaying Full Screen AD with URL: " + str);
    if (str.length() != 0)
    {
      Intent localIntent = new Intent(paramContext, TapjoyFeaturedAppWebView.class);
      localIntent.putExtra("USER_ID", this.deviceUserID);
      localIntent.putExtra("FULLSCREEN_AD_URL", str);
      localIntent.putExtra("CLIENT_PACKAGE", this.clientPackage);
      paramContext.startActivity(localIntent);
    }
  }

  public void showOffers(Context paramContext)
  {
    TapjoyLog.i("TapjoyConnect", "Showing offers without userID.");
    showOffers(paramContext, this.deviceID);
  }

  public void showOffers(Context paramContext, String paramString)
  {
    TapjoyLog.i("TapjoyConnect", "Showing offers (userID = " + paramString + ")");
    Intent localIntent = new Intent(paramContext, TJCOffersWebView.class);
    localIntent.putExtra("USER_ID", paramString);
    localIntent.putExtra("URL_PARAMS", this.urlParams);
    localIntent.putExtra("CLIENT_PACKAGE", this.clientPackage);
    paramContext.startActivity(localIntent);
  }

  public void showOffersWithCurrencyID(Context paramContext, String paramString1, String paramString2, boolean paramBoolean)
  {
    TapjoyLog.i("TapjoyConnect", "Showing offers with currencyID: " + paramString2 + ", selector: " + paramBoolean + " (userID = " + paramString1 + ")");
    this.multipleCurrencyID = paramString2;
    if (paramBoolean);
    for (String str1 = "1"; ; str1 = "0")
    {
      this.multipleCurrencySelector = str1;
      String str2 = String.valueOf(new StringBuilder(String.valueOf(this.urlParams)).append("&currency_id=").append(this.multipleCurrencyID).toString()) + "&currency_selector=" + this.multipleCurrencySelector;
      Intent localIntent = new Intent(paramContext, TJCOffersWebView.class);
      localIntent.putExtra("USER_ID", paramString1);
      localIntent.putExtra("URL_PARAMS", str2);
      localIntent.putExtra("CLIENT_PACKAGE", this.clientPackage);
      paramContext.startActivity(localIntent);
      return;
    }
  }

  public void spendTapPoints(int paramInt, TapjoySpendPointsNotifier paramTapjoySpendPointsNotifier)
  {
    if (paramInt < 0)
      TapjoyLog.e("TapjoyConnect", "spendTapPoints error: amount must be a positive number");
    do
    {
      return;
      this.spendTapPoints = paramInt;
    }
    while (tapjoyConnectInstance == null);
    tapjoySpendPointsNotifier = paramTapjoySpendPointsNotifier;
    tapjoyConnectInstance.spendTapPointsHelper();
  }

  private class ConnectGetPointsTask extends AsyncTask<Void, Void, Boolean>
  {
    private ConnectGetPointsTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      String str = TapjoyConnect.tapjoyURLConnection.connectToURL("https://ws.tapjoyads.com/get_vg_store_items/user_account?", TapjoyConnect.this.urlParams);
      boolean bool = false;
      if (str != null)
        bool = TapjoyConnect.this.handleGetPointsResponse(str);
      if (!bool)
        TapjoyConnect.tapjoyNotifier.getUpdatePointsFailed("Failed to retrieve points from server");
      return Boolean.valueOf(bool);
    }
  }

  private class ConnectSpendPointsTask extends AsyncTask<Void, Void, Boolean>
  {
    private ConnectSpendPointsTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      String str1 = String.valueOf(TapjoyConnect.this.urlParams) + "&tap_points=" + TapjoyConnect.this.spendTapPoints;
      String str2 = TapjoyConnect.tapjoyURLConnection.connectToURL("https://ws.tapjoyads.com/purchase_vg/spend?", str1);
      boolean bool = false;
      if (str2 != null)
        bool = TapjoyConnect.this.handleSpendPointsResponse(str2);
      if (!bool)
        TapjoyConnect.tapjoySpendPointsNotifier.getSpendPointsResponseFailed("Failed to spend points.");
      return Boolean.valueOf(bool);
    }
  }

  private class ConnectTask extends AsyncTask<Void, Void, Boolean>
  {
    private ConnectTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      String str1 = TapjoyConnect.this.urlParams;
      if (!TapjoyConnect.this.referralURL.equals(""))
        str1 = String.valueOf(str1) + "&" + TapjoyConnect.this.referralURL;
      String str2 = TapjoyConnect.tapjoyURLConnection.connectToURL("https://ws.tapjoyads.com/connect?", str1);
      boolean bool = false;
      if (str2 != null)
        bool = TapjoyConnect.this.handleConnectResponse(str2);
      return Boolean.valueOf(bool);
    }
  }

  private class PayPerActionTask extends AsyncTask<Void, Void, Boolean>
  {
    private PayPerActionTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      String str = TapjoyConnect.tapjoyURLConnection.connectToURL("https://ws.tapjoyads.com/connect?", TapjoyConnect.this.actionURLParams);
      boolean bool = false;
      if (str != null)
        bool = TapjoyConnect.this.handlePayPerActionResponse(str);
      return Boolean.valueOf(bool);
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyConnect
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */