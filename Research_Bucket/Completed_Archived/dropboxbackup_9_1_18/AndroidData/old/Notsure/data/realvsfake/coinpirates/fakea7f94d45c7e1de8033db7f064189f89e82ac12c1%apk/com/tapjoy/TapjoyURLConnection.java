package com.tapjoy;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

public class TapjoyURLConnection
{
  private static final String TAPJOY_URL_CONNECTION = "TapjoyURLConnection";

  public TapjoyURLConnection()
  {
  }

  public String connectToURL(String paramString1, String paramString2)
  {
    String str1 = null;
    try
    {
      String str2 = (String.valueOf(paramString1) + paramString2).replaceAll(" ", "%20");
      TapjoyLog.i("TapjoyURLConnection", "baseURL: " + paramString1);
      TapjoyLog.i("TapjoyURLConnection", "requestURL: " + str2);
      HttpGet localHttpGet = new HttpGet(str2);
      BasicHttpParams localBasicHttpParams = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(localBasicHttpParams, 15000);
      HttpConnectionParams.setSoTimeout(localBasicHttpParams, 30000);
      HttpResponse localHttpResponse = new DefaultHttpClient(localBasicHttpParams).execute(localHttpGet);
      str1 = EntityUtils.toString(localHttpResponse.getEntity());
      TapjoyLog.i("TapjoyURLConnection", "--------------------");
      TapjoyLog.i("TapjoyURLConnection", "response status: " + localHttpResponse.getStatusLine().getStatusCode());
      TapjoyLog.i("TapjoyURLConnection", "response size: " + str1.length());
      TapjoyLog.i("TapjoyURLConnection", "response: ");
      TapjoyLog.i("TapjoyURLConnection", str1);
      TapjoyLog.i("TapjoyURLConnection", "--------------------");
      return str1;
    }
    catch (Exception localException)
    {
      TapjoyLog.e("TapjoyURLConnection", "Exception: " + localException.toString());
    }
    return str1;
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyURLConnection
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */