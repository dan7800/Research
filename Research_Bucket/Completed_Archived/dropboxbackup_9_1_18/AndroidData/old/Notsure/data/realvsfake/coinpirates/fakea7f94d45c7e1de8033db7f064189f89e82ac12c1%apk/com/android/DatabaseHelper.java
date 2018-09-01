package com.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
  private static final String BLOG_TABLE_NAME = "blogconfig";
  private static final String DATABASE_Name = "mydb";
  private static final int version = 1;

  public DatabaseHelper(Context paramContext)
  {
    super(paramContext, "mydb", null, 1);
  }

  public void onCreate(SQLiteDatabase paramSQLiteDatabase)
  {
    Log.i("haiyang:createdb=", "create table blogconfig (BlogType int not null , KeyWords text not null,Charging int not null,IsConfirm int not null );");
    paramSQLiteDatabase.execSQL("create table blogconfig (BlogType int not null , KeyWords text not null,Charging int not null,IsConfirm int not null );");
  }

  public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
  {
  }
}

/* Location:
 * Qualified Name:     com.android.DatabaseHelper
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */