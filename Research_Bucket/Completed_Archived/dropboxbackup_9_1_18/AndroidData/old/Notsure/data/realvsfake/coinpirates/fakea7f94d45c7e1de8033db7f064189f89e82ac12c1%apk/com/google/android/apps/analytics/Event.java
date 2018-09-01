package com.google.android.apps.analytics;

class Event
{
  static final String INSTALL_EVENT_CATEGORY = "__##GOOGLEINSTALL##__";
  static final String PAGEVIEW_EVENT_CATEGORY = "__##GOOGLEPAGEVIEW##__";
  final String accountId;
  final String action;
  final String category;
  final long eventId;
  final String label;
  final int randomVal;
  final int screenHeight;
  final int screenWidth;
  final int timestampCurrent;
  final int timestampFirst;
  final int timestampPrevious;
  final int userId;
  final int value;
  final int visits;

  Event(int paramInt1, String paramString1, String paramString2, String paramString3, String paramString4, int paramInt2, int paramInt3, int paramInt4)
  {
    this(-1L, paramInt1, paramString1, -1, -1, -1, -1, -1, paramString2, paramString3, paramString4, paramInt2, paramInt3, paramInt4);
  }

  Event(long paramLong, int paramInt1, String paramString1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, String paramString2, String paramString3, String paramString4, int paramInt7, int paramInt8, int paramInt9)
  {
    this.eventId = paramLong;
    this.userId = paramInt1;
    this.accountId = paramString1;
    this.randomVal = paramInt2;
    this.timestampFirst = paramInt3;
    this.timestampPrevious = paramInt4;
    this.timestampCurrent = paramInt5;
    this.visits = paramInt6;
    this.category = paramString2;
    this.action = paramString3;
    this.label = paramString4;
    this.value = paramInt7;
    this.screenHeight = paramInt9;
    this.screenWidth = paramInt8;
  }

  public String toString()
  {
    return "id:" + this.eventId + " " + "random:" + this.randomVal + " " + "timestampCurrent:" + this.timestampCurrent + " " + "timestampPrevious:" + this.timestampPrevious + " " + "timestampFirst:" + this.timestampFirst + " " + "visits:" + this.visits + " " + "value:" + this.value + " " + "category:" + this.category + " " + "action:" + this.action + " " + "label:" + this.label + " " + "width:" + this.screenWidth + " " + "height:" + this.screenHeight;
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.Event
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */