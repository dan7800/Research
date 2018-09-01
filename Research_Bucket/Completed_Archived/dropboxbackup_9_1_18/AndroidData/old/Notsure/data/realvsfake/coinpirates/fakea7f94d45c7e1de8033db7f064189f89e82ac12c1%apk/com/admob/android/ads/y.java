package com.admob.android.ads;

import android.app.Activity;
import android.view.KeyEvent;
import android.webkit.WebView;
import com.admob.android.ads.view.AdMobWebView;
import java.lang.ref.WeakReference;

public final class y extends AdMobWebView
{
  String a;
  boolean b = true;
  private q e;

  public y(Activity paramActivity, String paramString, q paramq)
  {
    super(paramActivity, false, new WeakReference(paramActivity));
    this.a = paramString;
    this.e = paramq;
  }

  protected final ad a(WeakReference<Activity> paramWeakReference)
  {
    return new a(this, paramWeakReference);
  }

  public final void a()
  {
    if (this.e != null)
      this.e.a();
  }

  public final void a(String paramString)
  {
    this.c = (paramString + "#sdk");
  }

  public final boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if (paramInt == 4)
    {
      a();
      return true;
    }
    return super.onKeyDown(paramInt, paramKeyEvent);
  }

  public final class a extends ad
  {
    public a(WeakReference<Activity> arg2)
    {
      super(localWeakReference);
    }

    public final void onPageFinished(WebView paramWebView, String paramString)
    {
      if (("http://mm.admob.com/static/android/canvas.html".equals(paramString)) && (y.this.b))
      {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("javascript:cb('");
        localStringBuilder.append(y.this.c);
        localStringBuilder.append("','");
        localStringBuilder.append(y.this.a);
        localStringBuilder.append("')");
        y.this.b = false;
        y.this.loadUrl(localStringBuilder.toString());
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.y
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */