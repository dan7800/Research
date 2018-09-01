package com.tapjoy;

public abstract interface TapjoySpendPointsNotifier
{
  public abstract void getSpendPointsResponse(String paramString, int paramInt);

  public abstract void getSpendPointsResponseFailed(String paramString);
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoySpendPointsNotifier
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */