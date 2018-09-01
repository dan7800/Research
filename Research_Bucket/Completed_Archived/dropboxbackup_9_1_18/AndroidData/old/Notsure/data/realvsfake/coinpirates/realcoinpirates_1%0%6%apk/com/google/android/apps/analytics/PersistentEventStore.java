package com.google.android.apps.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

class PersistentEventStore
  implements EventStore
{
  private static final String ACCOUNT_ID = "account_id";
  private static final String ACTION = "action";
  private static final String CATEGORY = "category";
  private static final String DATABASE_NAME = "google_analytics.db";
  private static final int DATABASE_VERSION = 1;
  private static final String EVENT_ID = "event_id";
  private static final String LABEL = "label";
  private static final int MAX_EVENTS = 1000;
  private static final String RANDOM_VAL = "random_val";
  private static final String REFERRER = "referrer";
  private static final String SCREEN_HEIGHT = "screen_height";
  private static final String SCREEN_WIDTH = "screen_width";
  private static final String STORE_ID = "store_id";
  private static final String TIMESTAMP_CURRENT = "timestamp_current";
  private static final String TIMESTAMP_FIRST = "timestamp_first";
  private static final String TIMESTAMP_PREVIOUS = "timestamp_previous";
  private static final String USER_ID = "user_id";
  private static final String VALUE = "value";
  private static final String VISITS = "visits";
  private SQLiteStatement compiledCountStatement = null;
  private DataBaseHelper databaseHelper;
  private int numStoredEvents;
  private boolean sessionUpdated;
  private int storeId;
  private long timestampCurrent;
  private long timestampFirst;
  private long timestampPrevious;
  private int visits;

  public PersistentEventStore(Context paramContext)
  {
    this(paramContext, null);
  }

  public PersistentEventStore(Context paramContext, String paramString)
  {
    if (paramString != null)
    {
      this.databaseHelper = new DataBaseHelper(paramContext, paramString);
      return;
    }
    this.databaseHelper = new DataBaseHelper(paramContext);
  }

  private void storeUpdatedSession()
  {
    SQLiteDatabase localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
    ContentValues localContentValues = new ContentValues();
    localContentValues.put("timestamp_previous", Long.valueOf(this.timestampPrevious));
    localContentValues.put("timestamp_current", Long.valueOf(this.timestampCurrent));
    localContentValues.put("visits", Integer.valueOf(this.visits));
    String[] arrayOfString = new String[1];
    arrayOfString[0] = Long.toString(this.timestampFirst);
    localSQLiteDatabase.update("session", localContentValues, "timestamp_first=?", arrayOfString);
    this.sessionUpdated = true;
  }

  public void deleteEvent(long paramLong)
  {
    if (this.databaseHelper.getWritableDatabase().delete("events", "event_id=" + paramLong, null) != 0)
      this.numStoredEvents -= 1;
  }

  public int getNumStoredEvents()
  {
    if (this.compiledCountStatement == null)
      this.compiledCountStatement = this.databaseHelper.getReadableDatabase().compileStatement("SELECT COUNT(*) from events");
    return (int)this.compiledCountStatement.simpleQueryForLong();
  }

  public String getReferrer()
  {
    Cursor localCursor = this.databaseHelper.getReadableDatabase().query("install_referrer", new String[] { "referrer" }, null, null, null, null, null);
    if (localCursor.moveToFirst());
    for (String str = localCursor.getString(0); ; str = null)
    {
      localCursor.close();
      return str;
    }
  }

  public int getStoreId()
  {
    return this.storeId;
  }

  public Event[] peekEvents()
  {
    return peekEvents(1000);
  }

  public Event[] peekEvents(int paramInt)
  {
    Cursor localCursor = this.databaseHelper.getReadableDatabase().query("events", null, null, null, null, null, "event_id", Integer.toString(paramInt));
    ArrayList localArrayList = new ArrayList();
    while (localCursor.moveToNext())
      localArrayList.add(new Event(localCursor.getLong(0), localCursor.getInt(1), localCursor.getString(2), localCursor.getInt(3), localCursor.getInt(4), localCursor.getInt(5), localCursor.getInt(6), localCursor.getInt(7), localCursor.getString(8), localCursor.getString(9), localCursor.getString(10), localCursor.getInt(11), localCursor.getInt(12), localCursor.getInt(13)));
    localCursor.close();
    return (Event[])localArrayList.toArray(new Event[localArrayList.size()]);
  }

  public void putEvent(Event paramEvent)
  {
    if (this.numStoredEvents >= 1000)
      Log.w("googleanalytics", "Store full. Not storing last event.");
    SQLiteDatabase localSQLiteDatabase;
    ContentValues localContentValues;
    do
    {
      return;
      if (!this.sessionUpdated)
        storeUpdatedSession();
      localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
      localContentValues = new ContentValues();
      localContentValues.put("user_id", Integer.valueOf(paramEvent.userId));
      localContentValues.put("account_id", paramEvent.accountId);
      localContentValues.put("random_val", Integer.valueOf((int)(2147483647.0D * Math.random())));
      localContentValues.put("timestamp_first", Long.valueOf(this.timestampFirst));
      localContentValues.put("timestamp_previous", Long.valueOf(this.timestampPrevious));
      localContentValues.put("timestamp_current", Long.valueOf(this.timestampCurrent));
      localContentValues.put("visits", Integer.valueOf(this.visits));
      localContentValues.put("category", paramEvent.category);
      localContentValues.put("action", paramEvent.action);
      localContentValues.put("label", paramEvent.label);
      localContentValues.put("value", Integer.valueOf(paramEvent.value));
      localContentValues.put("screen_width", Integer.valueOf(paramEvent.screenWidth));
      localContentValues.put("screen_height", Integer.valueOf(paramEvent.screenHeight));
    }
    while (localSQLiteDatabase.insert("events", "event_id", localContentValues) == -1L);
    this.numStoredEvents = (1 + this.numStoredEvents);
  }

  public void setReferrer(String paramString)
  {
    SQLiteDatabase localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
    ContentValues localContentValues = new ContentValues();
    localContentValues.put("referrer", paramString);
    localSQLiteDatabase.insert("install_referrer", null, localContentValues);
  }

  public void startNewVisit()
  {
    this.sessionUpdated = false;
    this.numStoredEvents = getNumStoredEvents();
    SQLiteDatabase localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
    Cursor localCursor = localSQLiteDatabase.query("session", null, null, null, null, null, null);
    if (!localCursor.moveToFirst())
    {
      long l = System.currentTimeMillis() / 1000L;
      this.timestampFirst = l;
      this.timestampPrevious = l;
      this.timestampCurrent = l;
      this.visits = 1;
      this.storeId = (0x7FFFFFFF & new SecureRandom().nextInt());
      ContentValues localContentValues = new ContentValues();
      localContentValues.put("timestamp_first", Long.valueOf(this.timestampFirst));
      localContentValues.put("timestamp_previous", Long.valueOf(this.timestampPrevious));
      localContentValues.put("timestamp_current", Long.valueOf(this.timestampCurrent));
      localContentValues.put("visits", Integer.valueOf(this.visits));
      localContentValues.put("store_id", Integer.valueOf(this.storeId));
      localSQLiteDatabase.insert("session", "timestamp_first", localContentValues);
    }
    while (true)
    {
      localCursor.close();
      return;
      this.timestampFirst = localCursor.getLong(0);
      this.timestampPrevious = localCursor.getLong(2);
      this.timestampCurrent = (System.currentTimeMillis() / 1000L);
      this.visits = (1 + localCursor.getInt(3));
      this.storeId = localCursor.getInt(4);
    }
  }

  private static class DataBaseHelper extends SQLiteOpenHelper
  {
    public DataBaseHelper(Context paramContext)
    {
      super("google_analytics.db", null, 1);
    }

    public DataBaseHelper(Context paramContext, String paramString)
    {
      super(paramString, null, 1);
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase)
    {
      paramSQLiteDatabase.execSQL("CREATE TABLE events (" + String.format(" '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,", new Object[] { "event_id" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "user_id" }) + String.format(" '%s' CHAR(256) NOT NULL,", new Object[] { "account_id" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "random_val" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "timestamp_first" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "timestamp_previous" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "timestamp_current" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "visits" }) + String.format(" '%s' CHAR(256) NOT NULL,", new Object[] { "category" }) + String.format(" '%s' CHAR(256) NOT NULL,", new Object[] { "action" }) + String.format(" '%s' CHAR(256), ", new Object[] { "label" }) + String.format(" '%s' INTEGER,", new Object[] { "value" }) + String.format(" '%s' INTEGER,", new Object[] { "screen_width" }) + String.format(" '%s' INTEGER);", new Object[] { "screen_height" }));
      paramSQLiteDatabase.execSQL("CREATE TABLE session (" + String.format(" '%s' INTEGER PRIMARY KEY,", new Object[] { "timestamp_first" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "timestamp_previous" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "timestamp_current" }) + String.format(" '%s' INTEGER NOT NULL,", new Object[] { "visits" }) + String.format(" '%s' INTEGER NOT NULL);", new Object[] { "store_id" }));
      paramSQLiteDatabase.execSQL("CREATE TABLE install_referrer (referrer TEXT PRIMARY KEY NOT NULL);");
    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
    {
      Log.w("googleanalytics", "Database upgrade attempted, with no upgrade method available");
    }
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.PersistentEventStore
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */