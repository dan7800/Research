package com.tapjoy;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

class TJCOffersWebView$TapjoyWebViewClient extends WebViewClient
{
  private TJCOffersWebView$TapjoyWebViewClient(TJCOffersWebView paramTJCOffersWebView)
  {
  }

  public void onPageFinished(WebView paramWebView, String paramString)
  {
    TJCOffersWebView.access$0(this.this$0).setVisibility(8);
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
        String str = "http://market.android.com/search?q=pname:" + arrayOfString[1] + "&referrer=" + TJCOffersWebView.access$1(this.this$0);
        Intent localIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(str));
        this.this$0.startActivity(localIntent2);
        TapjoyLog.i("Offers", "Open URL of application = [" + str + "]");
        return true;
      }
      catch (Exception localException1)
      {
        TJCOffersWebView.access$2(this.this$0, new AlertDialog.Builder(this.this$0).setTitle("").setMessage("Android market is unavailable at this device. To view this link install market.").setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            paramAnonymousDialogInterface.dismiss();
          }
        }).create());
      }
      try
      {
        TJCOffersWebView.access$3(this.this$0).show();
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
    this.this$0.startActivity(localIntent1);
    return true;
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TJCOffersWebView.TapjoyWebViewClient
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */