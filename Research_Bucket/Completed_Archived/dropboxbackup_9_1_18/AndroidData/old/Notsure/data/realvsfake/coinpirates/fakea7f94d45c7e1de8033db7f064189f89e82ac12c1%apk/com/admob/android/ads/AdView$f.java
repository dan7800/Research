package com.admob.android.ads;

final class AdView$f
{
  public static final f a = new f(320, 48);
  private int b;
  private int c;

  static
  {
    new f(320, 270);
    new f(748, 110);
    new f(488, 80);
  }

  private AdView$f(int paramInt1, int paramInt2)
  {
    this.b = paramInt1;
    this.c = paramInt2;
  }

  public final String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(String.valueOf(this.b));
    localStringBuilder.append("x");
    localStringBuilder.append(String.valueOf(this.c));
    return localStringBuilder.toString();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView.f
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */