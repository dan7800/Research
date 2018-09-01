package com.admob.android.ads;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public final class p
  implements n
{
  public String a;
  public String b;
  public int c;
  public int d;
  public String e;
  public String f;
  public double g;
  public String h;
  public String i;
  public boolean j = false;
  public String k;
  public String l;
  public Vector<o> m = new Vector();

  public p()
  {
  }

  public final Bundle a()
  {
    Bundle localBundle = new Bundle();
    localBundle.putString("u", this.a);
    localBundle.putString("t", this.b);
    localBundle.putInt("c", this.c);
    localBundle.putInt("msm", this.d);
    localBundle.putString("s", this.e);
    localBundle.putString("sin", this.f);
    localBundle.putDouble("sd", this.g);
    localBundle.putString("skd", this.h);
    localBundle.putString("sku", this.i);
    localBundle.putByte("nosk", r.a(this.j));
    localBundle.putString("rd", this.k);
    localBundle.putString("ru", this.l);
    localBundle.putParcelableArrayList("b", AdView.a.a(this.m));
    return localBundle;
  }

  public final boolean a(Bundle paramBundle)
  {
    if (paramBundle == null)
      return false;
    this.a = paramBundle.getString("u");
    this.b = paramBundle.getString("t");
    this.c = paramBundle.getInt("c");
    this.d = paramBundle.getInt("msm");
    this.e = paramBundle.getString("s");
    this.f = paramBundle.getString("sin");
    this.g = paramBundle.getDouble("sd");
    this.h = paramBundle.getString("skd");
    this.i = paramBundle.getString("sku");
    this.j = r.a(paramBundle.getByte("nosk"));
    this.k = paramBundle.getString("rd");
    this.l = paramBundle.getString("ru");
    this.m = null;
    ArrayList localArrayList = paramBundle.getParcelableArrayList("b");
    if (localArrayList != null)
    {
      Vector localVector = new Vector();
      Iterator localIterator = localArrayList.iterator();
      while (localIterator.hasNext())
      {
        Bundle localBundle = (Bundle)localIterator.next();
        if (localBundle != null)
        {
          o localo = new o();
          if (localo.a(localBundle))
            localVector.add(localo);
        }
      }
      this.m = localVector;
    }
    return true;
  }

  public final boolean b()
  {
    return (this.c == 0) || (this.m == null) || (this.m.size() == 0);
  }

  public final boolean c()
  {
    return (this.f != null) && (this.f.length() > 0) && (this.g > 0.0D);
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.p
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */