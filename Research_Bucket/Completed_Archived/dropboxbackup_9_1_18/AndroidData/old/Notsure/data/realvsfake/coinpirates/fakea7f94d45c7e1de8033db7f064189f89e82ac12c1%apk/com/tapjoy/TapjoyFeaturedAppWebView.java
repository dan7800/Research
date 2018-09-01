package com.tapjoy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class TapjoyFeaturedAppWebView extends Activity
{
  final String TAPJOY_FEATURED_APP = "Featured App";
  private String clientPackage = "";
  private String fullScreenAdURL = "";
  private ProgressBar progressBar;
  private String userID = "";
  private WebView webView = null;

  public TapjoyFeaturedAppWebView()
  {
  }

  private void finishActivity()
  {
    finish();
  }

  private void showOffers()
  {
    TapjoyConnect.getTapjoyConnectInstance(this).showOffers(this, this.userID);
  }

  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if (this.webView != null)
      new RefreshTask(null).execute(new Void[0]);
  }

  protected void onCreate(Bundle paramBundle)
  {
    Bundle localBundle = getIntent().getExtras();
    this.userID = localBundle.getString("USER_ID");
    this.clientPackage = localBundle.getString("CLIENT_PACKAGE");
    this.fullScreenAdURL = localBundle.getString("FULLSCREEN_AD_URL");
    this.fullScreenAdURL = this.fullScreenAdURL.replaceAll(" ", "%20");
    super.onCreate(paramBundle);
    requestWindowFeature(1);
    setContentView(getApplicationContext().getResources().getIdentifier("tapjoy_featured_app_web_view", "layout", this.clientPackage));
    this.webView = ((WebView)findViewById(getApplicationContext().getResources().getIdentifier("FeaturedAppWebView", "id", this.clientPackage)));
    this.webView.setWebViewClient(new TapjoyWebViewClient(null));
    this.webView.getSettings().setJavaScriptEnabled(true);
    this.progressBar = ((ProgressBar)findViewById(getResources().getIdentifier("FeaturedAppProgressBar", "id", this.clientPackage)));
    this.progressBar.setVisibility(0);
    this.webView.loadUrl(this.fullScreenAdURL);
    TapjoyLog.i("Featured App", "Opening Full Screen AD URL = [" + this.fullScreenAdURL + "]");
  }

  private class RefreshTask extends AsyncTask<Void, Void, Boolean>
  {
    private RefreshTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      try
      {
        Thread.sleep(200L);
        return Boolean.valueOf(true);
      }
      catch (InterruptedException localInterruptedException)
      {
        while (true)
          localInterruptedException.printStackTrace();
      }
    }

    protected void onPostExecute(Boolean paramBoolean)
    {
      if (TapjoyFeaturedAppWebView.this.webView != null)
        TapjoyFeaturedAppWebView.this.webView.loadUrl("javascript:window.onorientationchange();");
    }
  }

  private class TapjoyWebViewClient extends WebViewClient
  {
    private TapjoyWebViewClient()
    {
    }

    public void onPageFinished(WebView paramWebView, String paramString)
    {
      TapjoyFeaturedAppWebView.this.progressBar.setVisibility(8);
    }

    public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
    {
      TapjoyLog.i("Featured App", "URL = [" + paramString + "]");
      if (paramString.contains("showOffers"))
      {
        TapjoyLog.i("Featured App", "show offers");
        TapjoyFeaturedAppWebView.this.showOffers();
        return true;
      }
      if (paramString.contains("dismiss"))
      {
        TapjoyLog.i("Featured App", "dismiss");
        TapjoyFeaturedAppWebView.this.finishActivity();
        return true;
      }
      if (paramString.indexOf("market") > -1)
      {
        TapjoyLog.i("Featured App", "Market URL = [" + paramString + "]");
        try
        {
          String[] arrayOfString = paramString.split("q=");
          String str = "http://market.android.com/search?q=pname:" + arrayOfString[1] + "&referrer=" + TapjoyFeaturedAppWebView.this.clientPackage;
          Intent localIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(str));
          TapjoyFeaturedAppWebView.this.startActivity(localIntent2);
          TapjoyLog.i("Featured App", "Open URL of application = [" + str + "]");
          return true;
        }
        catch (Exception localException)
        {
          TapjoyLog.i("Featured App", "Android market is unavailable at this device. To view this link install market.");
          return true;
        }
      }
      if (paramString.contains("ws.tapjoyads.com"))
      {
        TapjoyLog.i("Featured App", "Open redirecting URL = [" + paramString + "]");
        paramWebView.loadUrl(paramString);
        return true;
      }
      TapjoyLog.i("Featured App", "Opening URL in new browser = [" + paramString + "]");
      Intent localIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(paramString));
      TapjoyFeaturedAppWebView.this.startActivity(localIntent1);
      return true;
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyFeaturedAppWebView
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */