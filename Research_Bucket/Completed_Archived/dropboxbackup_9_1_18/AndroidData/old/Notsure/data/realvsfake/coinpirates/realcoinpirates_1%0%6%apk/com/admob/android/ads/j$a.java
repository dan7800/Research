package com.admob.android.ads;

public enum j$a
{
  private String k;

  static
  {
    a = new a("CLICK_TO_BROWSER", 3, "url");
    h = new a("CLICK_TO_CALL", 4, "call");
    i = new a("CLICK_TO_MUSIC", 5, "itunes");
    b = new a("CLICK_TO_CANVAS", 6, "canvas");
    j = new a("CLICK_TO_CONTACT", 7, "contact");
    c = new a("CLICK_TO_INTERACTIVE_VIDEO", 8, "movie");
    d = new a("CLICK_TO_FULLSCREEN_BROWSER", 9, "screen");
    a[] arrayOfa = new a[10];
    arrayOfa[0] = e;
    arrayOfa[1] = f;
    arrayOfa[2] = g;
    arrayOfa[3] = a;
    arrayOfa[4] = h;
    arrayOfa[5] = i;
    arrayOfa[6] = b;
    arrayOfa[7] = j;
    arrayOfa[8] = c;
    arrayOfa[9] = d;
  }

  private j$a(String paramString)
  {
    this.k = paramString;
  }

  public static a a(String paramString)
  {
    a[] arrayOfa = values();
    int m = arrayOfa.length;
    for (int n = 0; ; n++)
    {
      Object localObject = null;
      if (n < m)
      {
        a locala = arrayOfa[n];
        if (locala.toString().equals(paramString))
          localObject = locala;
      }
      else
      {
        return localObject;
      }
    }
  }

  public final String toString()
  {
    return this.k;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.j.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */