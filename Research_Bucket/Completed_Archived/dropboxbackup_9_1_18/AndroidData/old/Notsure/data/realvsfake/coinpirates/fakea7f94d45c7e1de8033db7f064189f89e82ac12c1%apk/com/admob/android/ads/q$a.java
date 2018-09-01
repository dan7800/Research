package com.admob.android.ads;

public enum q$a
{
  private String d;

  static
  {
    a[] arrayOfa = new a[3];
    arrayOfa[0] = a;
    arrayOfa[1] = b;
    arrayOfa[2] = c;
  }

  private q$a(String paramString)
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

/* Location:
 * Qualified Name:     com.admob.android.ads.q.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */