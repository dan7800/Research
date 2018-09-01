package com.google.android.apps.analytics;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class PersistentEventStore$DataBaseHelper extends SQLiteOpenHelper
{
  public PersistentEventStore$DataBaseHelper(Context paramContext)
  {
    super(paramContext, "google_analytics.db", null, 1);
  }

  public PersistentEventStore$DataBaseHelper(Context paramContext, String paramString)
  {
    super(paramContext, paramString, null, 1);
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

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.PersistentEventStore.DataBaseHelper
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */