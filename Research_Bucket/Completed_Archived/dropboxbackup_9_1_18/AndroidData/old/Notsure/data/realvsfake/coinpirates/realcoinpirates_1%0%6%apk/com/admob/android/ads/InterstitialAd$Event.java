package com.admob.android.ads;

public enum InterstitialAd$Event
{
  static
  {
    PRE_ROLL = new Event("PRE_ROLL", 2);
    POST_ROLL = new Event("POST_ROLL", 3);
    OTHER = new Event("OTHER", 4);
    Event[] arrayOfEvent = new Event[5];
    arrayOfEvent[0] = APP_START;
    arrayOfEvent[1] = SCREEN_CHANGE;
    arrayOfEvent[2] = PRE_ROLL;
    arrayOfEvent[3] = POST_ROLL;
    arrayOfEvent[4] = OTHER;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.Event
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */