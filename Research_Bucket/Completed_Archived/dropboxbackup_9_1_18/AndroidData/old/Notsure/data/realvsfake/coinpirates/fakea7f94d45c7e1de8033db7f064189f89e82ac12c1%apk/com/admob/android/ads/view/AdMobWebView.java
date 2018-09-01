package com.admob.android.ads.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.admob.android.ads.InterstitialAd.c;
import com.admob.android.ads.ad;
import com.admob.android.ads.f;
import com.admob.android.ads.j;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONObject;

public class AdMobWebView extends WebView
  implements View.OnClickListener
{
  private boolean a;
  private WeakReference<Activity> b;
  public String c;
  protected ad d;

  public AdMobWebView(Context paramContext, boolean paramBoolean, WeakReference<Activity> paramWeakReference)
  {
    super(paramContext);
    this.a = paramBoolean;
    this.b = paramWeakReference;
    WebSettings localWebSettings = getSettings();
    localWebSettings.setLoadsImagesAutomatically(true);
    localWebSettings.setPluginsEnabled(true);
    localWebSettings.setJavaScriptEnabled(true);
    localWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    localWebSettings.setSaveFormData(false);
    localWebSettings.setSavePassword(false);
    localWebSettings.setUserAgentString(f.h());
    this.d = a(paramWeakReference);
    setWebViewClient(this.d);
  }

  public static View a(Context paramContext, String paramString, boolean paramBoolean1, boolean paramBoolean2, Point paramPoint, float paramFloat, WeakReference<Activity> paramWeakReference)
  {
    RelativeLayout localRelativeLayout = new RelativeLayout(paramContext);
    localRelativeLayout.setGravity(17);
    AdMobWebView localAdMobWebView = new AdMobWebView(paramContext, paramBoolean2, paramWeakReference);
    localAdMobWebView.setBackgroundColor(0);
    localRelativeLayout.addView(localAdMobWebView, new RelativeLayout.LayoutParams(-1, -1));
    if (paramBoolean2)
    {
      ImageButton localImageButton = new ImageButton(paramContext);
      localImageButton.setImageResource(17301527);
      localImageButton.setBackgroundDrawable(null);
      localImageButton.setPadding(0, 0, 0, 0);
      localImageButton.setOnClickListener(localAdMobWebView);
      RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
      localLayoutParams.setMargins(j.a(paramPoint.x, paramFloat), j.a(paramPoint.y, paramFloat), 0, 0);
      localRelativeLayout.addView(localImageButton, localLayoutParams);
    }
    localAdMobWebView.c = paramString;
    localAdMobWebView.loadUrl(paramString);
    return localRelativeLayout;
  }

  private String a(Object paramObject)
  {
    if (paramObject == null)
      return "{}";
    if (((paramObject instanceof Integer)) || ((paramObject instanceof Double)))
      return paramObject.toString();
    if ((paramObject instanceof String))
    {
      String str4 = (String)paramObject;
      return "'" + str4 + "'";
    }
    if ((paramObject instanceof Map))
    {
      Iterator localIterator = ((Map)paramObject).entrySet().iterator();
      String str3;
      for (Object localObject1 = "{"; localIterator.hasNext(); localObject1 = str3)
      {
        Map.Entry localEntry = (Map.Entry)localIterator.next();
        Object localObject2 = localEntry.getKey();
        Object localObject3 = localEntry.getValue();
        String str1 = a(localObject2);
        String str2 = a(localObject3);
        str3 = ((String)localObject1).concat(str1 + ":" + str2);
        if (localIterator.hasNext())
          str3 = str3.concat(",");
      }
      return ((String)localObject1).concat("}");
    }
    if ((paramObject instanceof JSONObject))
      return ((JSONObject)paramObject).toString();
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "Unable to create JSON from object: " + paramObject);
    return "";
  }

  protected ad a(WeakReference<Activity> paramWeakReference)
  {
    return new ad(this, paramWeakReference);
  }

  public void a()
  {
    if (this.b != null)
    {
      Activity localActivity = (Activity)this.b.get();
      if (localActivity != null)
        localActivity.finish();
    }
  }

  public final void a(String paramString, Object[] paramArrayOfObject)
  {
    List localList = Arrays.asList(paramArrayOfObject);
    String str1 = "";
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      str1 = str1.concat(a(localIterator.next()));
      if (localIterator.hasNext())
        str1 = str1.concat(",");
    }
    String str2 = "javascript:admob.".concat(paramString) + "(" + str1 + ");";
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.w("AdMobSDK", "Sending url to webView: " + str2);
  }

  public final void b(String paramString)
  {
    this.c = paramString;
  }

  public void loadUrl(String paramString)
  {
    if (this.a);
    for (String str = paramString + "#sdk_close"; ; str = paramString)
    {
      super.loadUrl(str);
      return;
    }
  }

  public void onClick(View paramView)
  {
    a();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.view.AdMobWebView
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */