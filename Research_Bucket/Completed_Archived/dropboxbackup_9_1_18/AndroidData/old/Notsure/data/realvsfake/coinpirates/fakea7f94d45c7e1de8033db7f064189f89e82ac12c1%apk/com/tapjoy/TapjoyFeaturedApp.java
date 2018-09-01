package com.tapjoy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TapjoyFeaturedApp
{
  static final String TJC_FEATURED_APP_URL_PATH = "get_offers/featured?";
  private static TapjoyFeaturedAppNotifier featuredAppNotifier;
  private static TapjoyURLConnection tapjoyURLConnection = null;
  final String PREFS_NAME = "TapjoyFeaturedAppPrefs";
  final String TAPJOY_FEATURED_APP = "Featured App";
  private String baseURL = "";
  private String clickURL = "";
  private ConnectTask connectTask = null;
  private Context context;
  private int displayCount = 5;
  private TapjoyFeaturedAppObject featuredAppObject = null;
  private String urlParams = "";

  public TapjoyFeaturedApp(Context paramContext)
  {
    this.context = paramContext;
    tapjoyURLConnection = new TapjoyURLConnection();
  }

  private boolean buildResponse(String paramString)
  {
    DocumentBuilderFactory localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    try
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramString.getBytes("UTF-8"));
      Document localDocument = localDocumentBuilderFactory.newDocumentBuilder().parse(localByteArrayInputStream);
      this.featuredAppObject.cost = getNodeTrimValue(localDocument.getElementsByTagName("Cost"));
      String str = getNodeTrimValue(localDocument.getElementsByTagName("Amount"));
      if (str != null)
        this.featuredAppObject.amount = Integer.parseInt(str);
      this.featuredAppObject.description = getNodeTrimValue(localDocument.getElementsByTagName("Description"));
      this.featuredAppObject.iconURL = getNodeTrimValue(localDocument.getElementsByTagName("IconURL"));
      this.featuredAppObject.name = getNodeTrimValue(localDocument.getElementsByTagName("Name"));
      this.featuredAppObject.redirectURL = getNodeTrimValue(localDocument.getElementsByTagName("RedirectURL"));
      this.featuredAppObject.storeID = getNodeTrimValue(localDocument.getElementsByTagName("StoreID"));
      this.featuredAppObject.fullScreenAdURL = getNodeTrimValue(localDocument.getElementsByTagName("FullScreenAdURL"));
      TapjoyLog.i("Featured App", "cost: " + this.featuredAppObject.cost);
      TapjoyLog.i("Featured App", "amount: " + this.featuredAppObject.amount);
      TapjoyLog.i("Featured App", "description: " + this.featuredAppObject.description);
      TapjoyLog.i("Featured App", "iconURL: " + this.featuredAppObject.iconURL);
      TapjoyLog.i("Featured App", "name: " + this.featuredAppObject.name);
      TapjoyLog.i("Featured App", "redirectURL: " + this.featuredAppObject.redirectURL);
      TapjoyLog.i("Featured App", "storeID: " + this.featuredAppObject.storeID);
      TapjoyLog.i("Featured App", "fullScreenAdURL: " + this.featuredAppObject.fullScreenAdURL);
      if (this.featuredAppObject.fullScreenAdURL != null)
      {
        int i = this.featuredAppObject.fullScreenAdURL.length();
        if (i != 0)
          break label495;
      }
      for (bool = false; ; bool = true)
      {
        if (!bool)
          break label547;
        if (getDisplayCountForStoreID(this.featuredAppObject.storeID) >= this.displayCount)
          break;
        featuredAppNotifier.getFeaturedAppResponse(this.featuredAppObject);
        incrementDisplayCountForStoreID(this.featuredAppObject.storeID);
        return bool;
      }
    }
    catch (Exception localException)
    {
      boolean bool;
      while (true)
      {
        TapjoyLog.e("Featured App", "Error parsing XML: " + localException.toString());
        bool = false;
      }
      featuredAppNotifier.getFeaturedAppResponseFailed("Featured App to display has exceeded display count");
      return bool;
    }
    label495: featuredAppNotifier.getFeaturedAppResponseFailed("Failed to parse XML file from response");
    label547: return true;
  }

  private int getDisplayCountForStoreID(String paramString)
  {
    int i = this.context.getSharedPreferences("TapjoyFeaturedAppPrefs", 0).getInt(paramString, 0);
    TapjoyLog.i("Featured App", "getDisplayCount: " + i + ", storeID: " + paramString);
    return i;
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

  private void incrementDisplayCountForStoreID(String paramString)
  {
    SharedPreferences localSharedPreferences = this.context.getSharedPreferences("TapjoyFeaturedAppPrefs", 0);
    SharedPreferences.Editor localEditor = localSharedPreferences.edit();
    int i = 1 + localSharedPreferences.getInt(paramString, 0);
    TapjoyLog.i("Featured App", "incrementDisplayCount: " + i + ", storeID: " + paramString);
    localEditor.putInt(paramString, i);
    localEditor.commit();
  }

  public void getFeaturedAppDataFromServer(String paramString1, String paramString2, TapjoyFeaturedAppNotifier paramTapjoyFeaturedAppNotifier)
  {
    this.baseURL = paramString1;
    this.clickURL = (String.valueOf(this.baseURL) + "get_offers/featured?");
    this.urlParams = paramString2;
    featuredAppNotifier = paramTapjoyFeaturedAppNotifier;
    this.featuredAppObject = new TapjoyFeaturedAppObject();
    this.connectTask = new ConnectTask(null);
    this.connectTask.execute(new Void[0]);
  }

  public TapjoyFeaturedAppObject getFeaturedAppObject()
  {
    return this.featuredAppObject;
  }

  public void setDisplayCount(int paramInt)
  {
    this.displayCount = paramInt;
  }

  private class ConnectTask extends AsyncTask<Void, Void, Boolean>
  {
    private ConnectTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      String str = TapjoyFeaturedApp.tapjoyURLConnection.connectToURL(TapjoyFeaturedApp.this.clickURL, TapjoyFeaturedApp.this.urlParams);
      boolean bool = false;
      if (str != null)
        bool = TapjoyFeaturedApp.this.buildResponse(str);
      if (!bool)
        TapjoyFeaturedApp.featuredAppNotifier.getFeaturedAppResponseFailed("Error retrieving featured app data from the server.");
      return Boolean.valueOf(bool);
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyFeaturedApp
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */