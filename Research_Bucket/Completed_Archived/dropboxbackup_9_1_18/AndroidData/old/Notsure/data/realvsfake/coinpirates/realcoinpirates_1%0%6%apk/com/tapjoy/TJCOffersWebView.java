package com.tapjoy;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class TJCOffersWebView extends Activity
{
  final String TAPJOY_OFFERS = "Offers";
  private String clickURL = null;
  private String clientPackage = "";
  private Dialog dialog = null;
  private ProgressBar progressBar;
  private String tapjoyURL = "https://ws.tapjoyads.com/get_offers/webpage?";
  private String urlParams = "";
  private String userID = "";
  private WebView webView = null;

  public TJCOffersWebView()
  {
  }

  private void initMetaData(Bundle paramBundle)
  {
    if (paramBundle != null)
    {
      this.urlParams = paramBundle.getString("URL_PARAMS");
      this.clientPackage = paramBundle.getString("CLIENT_PACKAGE");
      this.userID = paramBundle.getString("USER_ID");
      this.urlParams = (String.valueOf(this.urlParams) + "&publisher_user_id=" + this.userID);
      TapjoyLog.i("Offers", "urlParams: [" + this.urlParams + "]");
      TapjoyLog.i("Offers", "clientPackage: [" + this.clientPackage + "]");
      return;
    }
    TapjoyLog.e("Offers", "Tapjoy offers meta data initialization fail.");
  }

  protected void onCreate(Bundle paramBundle)
  {
    initMetaData(getIntent().getExtras());
    this.clickURL = (String.valueOf(this.tapjoyURL) + this.urlParams);
    this.clickURL = this.clickURL.replaceAll(" ", "%20");
    super.onCreate(paramBundle);
    requestWindowFeature(1);
    setContentView(2130903046);
    this.webView = ((WebView)findViewById(2131296308));
    this.webView.setWebViewClient(new TapjoyWebViewClient(null));
    this.webView.getSettings().setJavaScriptEnabled(true);
    this.progressBar = ((ProgressBar)findViewById(2131296309));
    this.progressBar.setVisibility(0);
    this.webView.loadUrl(this.clickURL);
    TapjoyLog.i("Offers", "Opening URL = [" + this.clickURL + "]");
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if ((paramInt == 4) && (this.webView.canGoBack()))
    {
      this.webView.goBack();
      return true;
    }
    return super.onKeyDown(paramInt, paramKeyEvent);
  }

  protected void onResume()
  {
    super.onResume();
    if ((this.clickURL != null) && (this.webView != null))
      this.webView.loadUrl(this.clickURL);
  }

  private class TapjoyWebViewClient extends WebViewClient
  {
    private TapjoyWebViewClient()
    {
    }

    public void onPageFinished(WebView paramWebView, String paramString)
    {
      TJCOffersWebView.this.progressBar.setVisibility(8);
    }

    public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
    {
      TapjoyLog.i("Offers", "URL = [" + paramString + "]");
      if (paramString.indexOf("market") > -1)
      {
        TapjoyLog.i("Offers", "Market URL = [" + paramString + "]");
        try
        {
          String[] arrayOfString = paramString.split("q=");
          String str = "http://market.android.com/search?q=pname:" + arrayOfString[1] + "&referrer=" + TJCOffersWebView.this.clientPackage;
          Intent localIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(str));
          TJCOffersWebView.this.startActivity(localIntent2);
          TapjoyLog.i("Offers", "Open URL of application = [" + str + "]");
          return true;
        }
        catch (Exception localException1)
        {
          TJCOffersWebView.this.dialog = new AlertDialog.Builder(TJCOffersWebView.this).setTitle("").setMessage("Android market is unavailable at this device. To view this link install market.").setPositiveButton("OK", new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              paramAnonymousDialogInterface.dismiss();
            }
          }).create();
        }
        try
        {
          TJCOffersWebView.this.dialog.show();
          TapjoyLog.i("Offers", "Android market is unavailable at this device. To view this link install market.");
          return true;
        }
        catch (Exception localException2)
        {
          while (true)
            localException2.printStackTrace();
        }
      }
      if (paramString.contains("ws.tapjoyads.com"))
      {
        TapjoyLog.i("Offers", "Open redirecting URL = [" + paramString + "]");
        paramWebView.loadUrl(paramString);
        return true;
      }
      TapjoyLog.i("Offers", "Opening URL in new browser = [" + paramString + "]");
      Intent localIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(paramString));
      TJCOffersWebView.this.startActivity(localIntent1);
      return true;
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TJCOffersWebView
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */