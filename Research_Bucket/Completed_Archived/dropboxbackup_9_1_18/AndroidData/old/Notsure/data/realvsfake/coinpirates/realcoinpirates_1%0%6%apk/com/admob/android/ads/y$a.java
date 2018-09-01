package com.admob.android.ads;

import android.app.Activity;
import android.webkit.WebView;
import java.lang.ref.WeakReference;

public final class y$a extends ad
{
  public y$a(y paramy, WeakReference<Activity> paramWeakReference)
  {
    super(paramWeakReference, localWeakReference);
  }

  public final void onPageFinished(WebView paramWebView, String paramString)
  {
    if (("http://mm.admob.com/static/android/canvas.html".equals(paramString)) && (this.a.b))
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append("javascript:cb('");
      localStringBuilder.append(this.a.c);
      localStringBuilder.append("','");
      localStringBuilder.append(this.a.a);
      localStringBuilder.append("')");
      this.a.b = false;
      this.a.loadUrl(localStringBuilder.toString());
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.y.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */