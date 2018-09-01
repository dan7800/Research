package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class q
  implements u.a
{
  public r a = new r();
  public Vector<Intent> b = new Vector();
  private u c = new u(this);
  private PopupWindow d = null;

  public q()
  {
  }

  private static Bundle a(JSONObject paramJSONObject)
  {
    Object localObject1 = null;
    Iterator localIterator;
    Bundle localBundle;
    if (paramJSONObject != null)
    {
      localIterator = paramJSONObject.keys();
      if (!localIterator.hasNext())
        break label81;
      localBundle = new Bundle();
    }
    while (true)
      if (localIterator.hasNext())
      {
        String str = (String)localIterator.next();
        Object localObject2 = paramJSONObject.opt(str);
        if ((str != null) && (localObject2 != null))
          a(localBundle, str, localObject2);
      }
      else
      {
        localObject1 = localBundle;
        return localObject1;
        label81: localBundle = null;
      }
  }

  private void a(Context paramContext)
  {
    PackageManager localPackageManager = paramContext.getPackageManager();
    Iterator localIterator = this.b.iterator();
    while (true)
      if (localIterator.hasNext())
      {
        Intent localIntent = (Intent)localIterator.next();
        if (localPackageManager.resolveActivity(localIntent, 65536) == null)
          continue;
        try
        {
          paramContext.startActivity(localIntent);
          do
            return;
          while (!InterstitialAd.c.a("AdMobSDK", 6));
          Log.e("AdMobSDK", "Could not find a resolving intent on ad click");
          return;
        }
        catch (Exception localException)
        {
        }
      }
  }

  private void a(Context paramContext, String paramString)
  {
    a(b(paramContext, paramString));
  }

  private void a(Intent paramIntent)
  {
    if (paramIntent != null)
      this.b.add(paramIntent);
  }

  private static void a(Bundle paramBundle, String paramString, Object paramObject)
  {
    if ((paramString == null) || (paramObject == null));
    Vector localVector;
    Object localObject;
    do
    {
      do
      {
        int i;
        do
        {
          JSONArray localJSONArray;
          do
          {
            do
            {
              return;
              if ((paramObject instanceof String))
              {
                paramBundle.putString(paramString, (String)paramObject);
                return;
              }
              if ((paramObject instanceof Integer))
              {
                paramBundle.putInt(paramString, ((Integer)paramObject).intValue());
                return;
              }
              if ((paramObject instanceof Boolean))
              {
                paramBundle.putBoolean(paramString, ((Boolean)paramObject).booleanValue());
                return;
              }
              if ((paramObject instanceof Double))
              {
                paramBundle.putDouble(paramString, ((Double)paramObject).doubleValue());
                return;
              }
              if ((paramObject instanceof Long))
              {
                paramBundle.putLong(paramString, ((Long)paramObject).longValue());
                return;
              }
              if ((paramObject instanceof JSONObject))
              {
                paramBundle.putBundle(paramString, a((JSONObject)paramObject));
                return;
              }
            }
            while (!(paramObject instanceof JSONArray));
            localJSONArray = (JSONArray)paramObject;
          }
          while ((paramString == null) || (localJSONArray == null));
          localVector = new Vector();
          i = localJSONArray.length();
          int j = 0;
          while (true)
            if (j < i)
              try
              {
                localVector.add(localJSONArray.get(j));
                j++;
              }
              catch (JSONException localJSONException)
              {
                if (InterstitialAd.c.a("AdMobSDK", 6))
                  Log.e("AdMobSDK", "couldn't read bundle array while adding extras");
              }
        }
        while (i == 0);
        try
        {
          localObject = localVector.get(0);
          if (!(localObject instanceof String))
            break;
          paramBundle.putStringArray(paramString, (String[])localVector.toArray(new String[0]));
          return;
        }
        catch (ArrayStoreException localArrayStoreException)
        {
        }
      }
      while (!InterstitialAd.c.a("AdMobSDK", 6));
      Log.e("AdMobSDK", "Couldn't read in array when making extras");
      return;
      if ((localObject instanceof Integer))
      {
        Integer[] arrayOfInteger = (Integer[])localVector.toArray(new Integer[0]);
        int[] arrayOfInt = new int[arrayOfInteger.length];
        for (int i1 = 0; i1 < arrayOfInteger.length; i1++)
          arrayOfInt[i1] = arrayOfInteger[i1].intValue();
        paramBundle.putIntArray(paramString, arrayOfInt);
        return;
      }
      if ((localObject instanceof Boolean))
      {
        Boolean[] arrayOfBoolean = (Boolean[])localVector.toArray(new Boolean[0]);
        boolean[] arrayOfBoolean1 = new boolean[arrayOfBoolean.length];
        for (int n = 0; n < arrayOfBoolean1.length; n++)
          arrayOfBoolean1[n] = arrayOfBoolean[n].booleanValue();
        paramBundle.putBooleanArray(paramString, arrayOfBoolean1);
        return;
      }
      if ((localObject instanceof Double))
      {
        Double[] arrayOfDouble = (Double[])localVector.toArray(new Double[0]);
        double[] arrayOfDouble1 = new double[arrayOfDouble.length];
        for (int m = 0; m < arrayOfDouble1.length; m++)
          arrayOfDouble1[m] = arrayOfDouble[m].doubleValue();
        paramBundle.putDoubleArray(paramString, arrayOfDouble1);
        return;
      }
    }
    while (!(localObject instanceof Long));
    Long[] arrayOfLong = (Long[])localVector.toArray(new Long[0]);
    long[] arrayOfLong1 = new long[arrayOfLong.length];
    for (int k = 0; k < arrayOfLong1.length; k++)
      arrayOfLong1[k] = arrayOfLong[k].longValue();
    paramBundle.putLongArray(paramString, arrayOfLong1);
  }

  private void a(String paramString)
  {
    this.a.d = paramString;
  }

  public static void a(List<w> paramList, JSONObject paramJSONObject, String paramString)
  {
    if (paramList == null);
    while (true)
    {
      return;
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        w localw = (w)localIterator.next();
        h local1 = new h()
        {
          public final void a(e paramAnonymouse)
          {
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Click processed at " + paramAnonymouse.c());
          }

          public final void a(e paramAnonymouse, Exception paramAnonymousException)
          {
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Click processing failed at " + paramAnonymouse.c(), paramAnonymousException);
          }
        };
        boolean bool = localw.b;
        JSONObject localJSONObject = null;
        if (bool)
          localJSONObject = paramJSONObject;
        g.a(localw.a, "click_time_tracking", paramString, localJSONObject, local1).f();
      }
    }
  }

  private static Intent b(Context paramContext)
  {
    return new Intent(paramContext, AdMobActivity.class);
  }

  private Intent b(Context paramContext, String paramString)
  {
    j.a locala = this.a.a;
    Intent localIntent = null;
    if (locala != null)
      switch (2.a[locala.ordinal()])
      {
      default:
      case 1:
      case 2:
      case 3:
      }
    do
    {
      localIntent = new Intent();
      localIntent.setAction("android.intent.action.VIEW");
      localIntent.setData(Uri.parse(paramString));
      return localIntent;
      a(paramString);
      return null;
      return b(paramContext);
    }
    while ((this.a.h == null) || (this.a.h.b()));
    return b(paramContext);
  }

  public final void a()
  {
    if (this.d != null)
    {
      this.d.dismiss();
      this.d = null;
    }
  }

  public final void a(Activity paramActivity, View paramView)
  {
    if (this.a.a == j.a.b)
    {
      String str1 = this.a.d;
      String str2 = this.a.b;
      this.d = new PopupWindow(paramActivity);
      Rect localRect = new Rect();
      paramView.getWindowVisibleDisplayFrame(localRect);
      double d1 = k.d();
      RelativeLayout localRelativeLayout = new RelativeLayout(paramActivity);
      localRelativeLayout.setGravity(17);
      y localy = new y(paramActivity, str2, this);
      localy.setBackgroundColor(-1);
      localy.setId(1);
      RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(j.a(320, d1), j.a(295, d1));
      localLayoutParams.addRule(13);
      localRelativeLayout.addView(localy, localLayoutParams);
      localy.a(str1);
      localy.loadUrl("http://mm.admob.com/static/android/canvas.html");
      this.d.setBackgroundDrawable(null);
      this.d.setFocusable(true);
      this.d.setClippingEnabled(false);
      this.d.setWidth(localRect.width());
      this.d.setHeight(localRect.height());
      this.d.setContentView(localRelativeLayout);
      View localView = paramView.getRootView();
      this.d.showAtLocation(localView, 0, localRect.left, localRect.top);
      ViewGroup.LayoutParams localLayoutParams1 = localRelativeLayout.getLayoutParams();
      if ((localLayoutParams1 instanceof WindowManager.LayoutParams))
      {
        WindowManager.LayoutParams localLayoutParams2 = (WindowManager.LayoutParams)localLayoutParams1;
        localLayoutParams2.flags = (0x6 | localLayoutParams2.flags);
        localLayoutParams2.dimAmount = 0.5F;
        ((WindowManager)paramActivity.getSystemService("window")).updateViewLayout(localRelativeLayout, localLayoutParams1);
      }
      return;
    }
    if (!this.c.a())
    {
      this.c.d = new WeakReference(paramActivity);
      this.c.b();
      return;
    }
    a(paramActivity);
  }

  public final void a(Context paramContext, JSONArray paramJSONArray)
  {
    int i = 0;
    while (true)
      if (i < paramJSONArray.length())
        try
        {
          a(paramContext, paramJSONArray.getJSONObject(i));
          i++;
        }
        catch (JSONException localJSONException)
        {
          while (true)
            if (InterstitialAd.c.a("AdMobSDK", 6))
              Log.e("AdMobSDK", "Could not form an intent from ad action response: " + paramJSONArray.toString());
        }
  }

  public final void a(Context paramContext, JSONObject paramJSONObject)
  {
    if (paramJSONObject != null)
    {
      String str1 = paramJSONObject.optString("u");
      if ((str1 != null) && (!str1.equals("")))
        a(paramContext, str1);
    }
    else
    {
      return;
    }
    String str2 = paramJSONObject.optString("a", "android.intent.action.VIEW");
    String str3 = paramJSONObject.optString("d", null);
    if (this.a.d == null)
      a(str3);
    int i = paramJSONObject.optInt("f", 0);
    Bundle localBundle = a(paramJSONObject.optJSONObject("b"));
    Intent localIntent1;
    if (this.a.a != null)
      switch (2.a[this.a.a.ordinal()])
      {
      default:
        Intent localIntent2 = new Intent(str2, Uri.parse(str3));
        if (i != 0)
          localIntent2.addFlags(i);
        if (localBundle != null)
          localIntent2.putExtras(localBundle);
        localIntent1 = localIntent2;
      case 1:
      }
    while (true)
    {
      a(localIntent1);
      return;
      localIntent1 = b(paramContext, str3);
      continue;
      localIntent1 = null;
    }
  }

  public final void a(Context paramContext, JSONObject paramJSONObject, u paramu)
  {
    if (paramu == null);
    for (u localu = this.c; ; localu = paramu)
    {
      this.a.a(paramJSONObject, localu, AdManager.getUserId(paramContext));
      this.a.d = paramJSONObject.optString("u");
      JSONArray localJSONArray1 = paramJSONObject.optJSONArray("ua");
      JSONObject localJSONObject = paramJSONObject.optJSONObject("ac");
      JSONArray localJSONArray2 = paramJSONObject.optJSONArray("ac");
      if (localJSONArray2 != null)
        a(paramContext, localJSONArray2);
      do
        while (true)
        {
          return;
          if (localJSONObject != null)
          {
            a(paramContext, localJSONObject);
            return;
          }
          if (localJSONArray1 == null)
            break;
          for (int i = 0; i < localJSONArray1.length(); i++)
          {
            String str = localJSONArray1.optString(i);
            if (str != null)
              a(paramContext, str);
          }
        }
      while ((this.a.d == null) || (this.a.d.length() <= 0));
      a(paramContext, this.a.d);
      return;
    }
  }

  public final void a(Hashtable<String, Bitmap> paramHashtable)
  {
    if (this.a != null)
    {
      r localr = this.a;
      if (paramHashtable != null)
      {
        Iterator localIterator = paramHashtable.keySet().iterator();
        while (localIterator.hasNext())
        {
          String str = (String)localIterator.next();
          localr.k.putParcelable(str, (Parcelable)paramHashtable.get(str));
        }
      }
    }
  }

  public final void b()
  {
    if ((this.a != null) && (c()))
    {
      int i = this.b.size();
      Intent localIntent = null;
      if (i > 0)
        localIntent = (Intent)this.b.firstElement();
      if (localIntent != null)
        localIntent.putExtra("o", this.a.a());
    }
  }

  public final boolean c()
  {
    return (this.a != null) && (((this.a.a == j.a.c) && (this.a.h != null) && (!this.a.h.b())) || (this.a.a == j.a.d));
  }

  public final void k()
  {
    a(this.c.a);
    b();
    u localu = this.c;
    if (localu.d != null);
    for (Context localContext = (Context)localu.d.get(); ; localContext = null)
    {
      if (localContext != null)
        a(localContext);
      return;
    }
  }

  public final void l()
  {
  }

  public static enum a
  {
    private String d;

    static
    {
      a[] arrayOfa = new a[3];
      arrayOfa[0] = a;
      arrayOfa[1] = b;
      arrayOfa[2] = c;
    }

    private a(String paramString)
    {
      this.d = paramString;
    }

    public static a a(int paramInt)
    {
      a locala1 = c;
      a[] arrayOfa = values();
      int i = arrayOfa.length;
      Object localObject = locala1;
      for (int j = 0; j < i; j++)
      {
        a locala2 = arrayOfa[j];
        if (locala2.ordinal() == paramInt)
          localObject = locala2;
      }
      return localObject;
    }

    public final String toString()
    {
      return this.d;
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.q
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */