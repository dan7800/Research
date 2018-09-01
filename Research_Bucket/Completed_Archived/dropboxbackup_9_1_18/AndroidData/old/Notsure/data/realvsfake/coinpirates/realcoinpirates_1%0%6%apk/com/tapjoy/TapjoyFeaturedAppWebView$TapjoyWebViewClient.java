package com.tapjoy;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

class TapjoyFeaturedAppWebView$TapjoyWebViewClient extends WebViewClient
{
  private TapjoyFeaturedAppWebView$TapjoyWebViewClient(TapjoyFeaturedAppWebView paramTapjoyFeaturedAppWebView)
  {
  }

  public void onPageFinished(WebView paramWebView, String paramString)
  {
    TapjoyFeaturedAppWebView.access$1(this.this$0).setVisibility(8);
  }

  public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString)
  {
    TapjoyLog.i("Featured App", "URL = [" + paramString + "]");
    if (paramString.contains("showOffers"))
    {
      TapjoyLog.i("Featured App", "show offers");
      TapjoyFeaturedAppWebView.access$2(this.this$0);
      return true;
    }
    if (paramString.contains("dismiss"))
    {
      TapjoyLog.i("Featured App", "dismiss");
      TapjoyFeaturedAppWebView.access$3(this.this$0);
      return true;
    }
    if (paramString.indexOf("market") > -1)
    {
      TapjoyLog.i("Featured App", "Market URL = [" + paramString + "]");
      try
      {
        String[] arrayOfString = paramString.split("q=");
        String str = "http://market.android.com/search?q=pname:" + arrayOfString[1] + "&referrer=" + TapjoyFeaturedAppWebView.access$4(this.this$0);
        Intent localIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(str));
        this.this$0.startActivity(localIntent2);
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
    this.this$0.startActivity(localIntent1);
    return true;
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyFeaturedAppWebView.TapjoyWebViewClient
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */