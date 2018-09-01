package com.admob.android.ads;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class r
  implements n
{
  public j.a a = j.a.a;
  public String b = "";
  public Vector<w> c = new Vector();
  public String d = null;
  public q.a e = q.a.c;
  public boolean f = false;
  public Point g = new Point(4, 4);
  public p h = null;
  public String i = null;
  public String j = null;
  public Bundle k = new Bundle();
  public boolean l = false;
  private boolean m = false;
  private Point n = new Point(0, 0);
  private String o = null;

  public r()
  {
  }

  public static byte a(boolean paramBoolean)
  {
    if (paramBoolean)
      return 1;
    return 0;
  }

  private static Point a(int[] paramArrayOfInt)
  {
    if ((paramArrayOfInt == null) || (paramArrayOfInt.length == 2))
      return null;
    return new Point(paramArrayOfInt[0], paramArrayOfInt[1]);
  }

  public static boolean a(byte paramByte)
  {
    return paramByte == 1;
  }

  private static int[] a(Point paramPoint)
  {
    if (paramPoint == null)
      return null;
    int[] arrayOfInt = new int[2];
    arrayOfInt[0] = paramPoint.x;
    arrayOfInt[1] = paramPoint.y;
    return arrayOfInt;
  }

  public final Bundle a()
  {
    Bundle localBundle = new Bundle();
    localBundle.putString("a", this.a.toString());
    localBundle.putString("t", this.b);
    localBundle.putParcelableArrayList("c", AdView.a.a(this.c));
    localBundle.putString("u", this.d);
    localBundle.putInt("or", this.e.ordinal());
    localBundle.putByte("tr", a(this.m));
    localBundle.putByte("sc", a(this.f));
    localBundle.putIntArray("cbo", a(this.g));
    localBundle.putIntArray("cs", a(this.n));
    localBundle.putBundle("mi", AdView.a.a(this.h));
    localBundle.putString("su", this.i);
    localBundle.putString("si", this.j);
    localBundle.putString("json", this.o);
    localBundle.putBundle("$", this.k);
    localBundle.putByte("int", a(this.l));
    return localBundle;
  }

  public final void a(String paramString, boolean paramBoolean)
  {
    if ((paramString != null) && (!"".equals(paramString)))
      this.c.add(new w(paramString, paramBoolean));
  }

  public final void a(JSONObject paramJSONObject, u paramu, String paramString)
  {
    this.a = j.a.a(paramJSONObject.optString("a"));
    a(paramJSONObject.optString("au"), true);
    a(paramJSONObject.optString("tu"), false);
    JSONObject localJSONObject1 = paramJSONObject.optJSONObject("stats");
    if (localJSONObject1 != null)
    {
      this.i = localJSONObject1.optString("url");
      this.j = localJSONObject1.optString("id");
    }
    String str = paramJSONObject.optString("or");
    if ((str != null) && (!str.equals("")))
    {
      if ("l".equals(str))
        this.e = q.a.b;
    }
    else
      while (true)
        if (paramJSONObject.opt("t") != null)
        {
          bool1 = true;
          label122: this.m = bool1;
          this.b = paramJSONObject.optString("title");
          if (this.a != j.a.c)
            break label513;
          this.h = new p();
          JSONObject localJSONObject2 = paramJSONObject.optJSONObject("$");
          if (paramu != null);
          try
          {
            paramu.a(localJSONObject2, paramString);
            label178: this.h.a = paramJSONObject.optString("u");
            this.h.b = paramJSONObject.optString("title");
            this.h.c = paramJSONObject.optInt("mc", 2);
            this.h.d = paramJSONObject.optInt("msm", 0);
            this.h.e = paramJSONObject.optString("stats");
            this.h.f = paramJSONObject.optString("splash");
            this.h.g = paramJSONObject.optDouble("splash_duration", 1.5D);
            this.h.h = paramJSONObject.optString("skip_down");
            this.h.i = paramJSONObject.optString("skip_up");
            this.h.j = paramJSONObject.optBoolean("no_splash_skip");
            this.h.k = paramJSONObject.optString("replay_down");
            this.h.l = paramJSONObject.optString("replay_up");
            JSONArray localJSONArray2 = paramJSONObject.optJSONArray("buttons");
            if (localJSONArray2 != null)
            {
              int i1 = localJSONArray2.length();
              int i2 = 0;
              while (true)
                if (i2 < i1)
                {
                  JSONObject localJSONObject3 = localJSONArray2.optJSONObject(i2);
                  o localo = new o();
                  localo.a = localJSONObject3.optString("$");
                  localo.b = localJSONObject3.optString("h");
                  localo.c = localJSONObject3.optString("x");
                  localo.e = localJSONObject3.optString("analytics_page_name");
                  localo.d.a(localJSONObject3.optJSONObject("o"), paramu, paramString);
                  localo.f = localJSONObject3.optJSONObject("o").toString();
                  this.h.m.add(localo);
                  i2++;
                  continue;
                  this.e = q.a.a;
                  break;
                  bool1 = false;
                  break label122;
                }
            }
            if (paramJSONObject.optInt("sc", 0) != 0);
            for (boolean bool2 = true; ; bool2 = false)
            {
              this.f = bool2;
              JSONArray localJSONArray1 = paramJSONObject.optJSONArray("co");
              if ((localJSONArray1 != null) && (localJSONArray1.length() >= 2))
                this.g = new Point(localJSONArray1.optInt(0), localJSONArray1.optInt(1));
              this.o = paramJSONObject.toString();
              return;
            }
          }
          catch (JSONException localJSONException)
          {
            break label178;
          }
        }
  }

  public final boolean a(Bundle paramBundle)
  {
    if (paramBundle == null)
      return false;
    this.a = j.a.a(paramBundle.getString("a"));
    this.b = paramBundle.getString("t");
    this.c = new Vector();
    ArrayList localArrayList = paramBundle.getParcelableArrayList("c");
    if (localArrayList != null)
    {
      Iterator localIterator = localArrayList.iterator();
      while (localIterator.hasNext())
      {
        Bundle localBundle = (Bundle)localIterator.next();
        if (localBundle != null)
        {
          w localw = new w();
          localw.a = localBundle.getString("u");
          localw.b = localBundle.getBoolean("p", false);
          this.c.add(localw);
        }
      }
    }
    this.d = paramBundle.getString("u");
    this.e = q.a.a(paramBundle.getInt("or"));
    this.m = a(paramBundle.getByte("tr"));
    this.f = a(paramBundle.getByte("sc"));
    this.g = a(paramBundle.getIntArray("cbo"));
    if (this.g == null)
      this.g = new Point(4, 4);
    this.n = a(paramBundle.getIntArray("cs"));
    p localp = new p();
    if (localp.a(paramBundle.getBundle("mi")));
    for (this.h = localp; ; this.h = null)
    {
      this.i = paramBundle.getString("su");
      this.j = paramBundle.getString("si");
      this.o = paramBundle.getString("json");
      this.k = paramBundle.getBundle("$");
      this.l = a(paramBundle.getByte("int"));
      return true;
    }
  }

  public final Hashtable<String, Bitmap> b()
  {
    Set localSet = this.k.keySet();
    Hashtable localHashtable = new Hashtable();
    Iterator localIterator = localSet.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      Parcelable localParcelable = this.k.getParcelable(str);
      if ((localParcelable instanceof Bitmap))
        localHashtable.put(str, (Bitmap)localParcelable);
    }
    return localHashtable;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.r
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */