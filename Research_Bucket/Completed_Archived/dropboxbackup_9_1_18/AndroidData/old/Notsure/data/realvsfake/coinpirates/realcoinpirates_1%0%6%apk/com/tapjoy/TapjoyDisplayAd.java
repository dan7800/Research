package com.tapjoy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TapjoyDisplayAd
{
  private static final byte[] DECODE_TABLE = arrayOfByte;
  private static final int MASK_8BITS = 255;
  private static final byte PAD = 61;
  static final String TJC_DISPLAY_AD_URL_PATH = "display_ad?";
  private static String adClickURL;
  private static TapjoyDisplayAdNotifier displayAdNotifier;
  private static TapjoyURLConnection tapjoyURLConnection = null;
  final String TAPJOY_DISPLAY_AD = "Display Ad";
  View adView;
  private String baseURL = "";
  Bitmap bitmapImage;
  private byte[] buffer;
  private String clickURL = "";
  private ConnectTask connectTask = null;
  private Context context;
  private boolean eof;
  private int modulus;
  private int pos;
  private String urlParams = "";
  private int x;

  static
  {
    byte[] arrayOfByte = new byte[123];
    arrayOfByte[0] = -1;
    arrayOfByte[1] = -1;
    arrayOfByte[2] = -1;
    arrayOfByte[3] = -1;
    arrayOfByte[4] = -1;
    arrayOfByte[5] = -1;
    arrayOfByte[6] = -1;
    arrayOfByte[7] = -1;
    arrayOfByte[8] = -1;
    arrayOfByte[9] = -1;
    arrayOfByte[10] = -1;
    arrayOfByte[11] = -1;
    arrayOfByte[12] = -1;
    arrayOfByte[13] = -1;
    arrayOfByte[14] = -1;
    arrayOfByte[15] = -1;
    arrayOfByte[16] = -1;
    arrayOfByte[17] = -1;
    arrayOfByte[18] = -1;
    arrayOfByte[19] = -1;
    arrayOfByte[20] = -1;
    arrayOfByte[21] = -1;
    arrayOfByte[22] = -1;
    arrayOfByte[23] = -1;
    arrayOfByte[24] = -1;
    arrayOfByte[25] = -1;
    arrayOfByte[26] = -1;
    arrayOfByte[27] = -1;
    arrayOfByte[28] = -1;
    arrayOfByte[29] = -1;
    arrayOfByte[30] = -1;
    arrayOfByte[31] = -1;
    arrayOfByte[32] = -1;
    arrayOfByte[33] = -1;
    arrayOfByte[34] = -1;
    arrayOfByte[35] = -1;
    arrayOfByte[36] = -1;
    arrayOfByte[37] = -1;
    arrayOfByte[38] = -1;
    arrayOfByte[39] = -1;
    arrayOfByte[40] = -1;
    arrayOfByte[41] = -1;
    arrayOfByte[42] = -1;
    arrayOfByte[43] = 62;
    arrayOfByte[44] = -1;
    arrayOfByte[45] = 62;
    arrayOfByte[46] = -1;
    arrayOfByte[47] = 63;
    arrayOfByte[48] = 52;
    arrayOfByte[49] = 53;
    arrayOfByte[50] = 54;
    arrayOfByte[51] = 55;
    arrayOfByte[52] = 56;
    arrayOfByte[53] = 57;
    arrayOfByte[54] = 58;
    arrayOfByte[55] = 59;
    arrayOfByte[56] = 60;
    arrayOfByte[57] = 61;
    arrayOfByte[58] = -1;
    arrayOfByte[59] = -1;
    arrayOfByte[60] = -1;
    arrayOfByte[61] = -1;
    arrayOfByte[62] = -1;
    arrayOfByte[63] = -1;
    arrayOfByte[64] = -1;
    arrayOfByte[66] = 1;
    arrayOfByte[67] = 2;
    arrayOfByte[68] = 3;
    arrayOfByte[69] = 4;
    arrayOfByte[70] = 5;
    arrayOfByte[71] = 6;
    arrayOfByte[72] = 7;
    arrayOfByte[73] = 8;
    arrayOfByte[74] = 9;
    arrayOfByte[75] = 10;
    arrayOfByte[76] = 11;
    arrayOfByte[77] = 12;
    arrayOfByte[78] = 13;
    arrayOfByte[79] = 14;
    arrayOfByte[80] = 15;
    arrayOfByte[81] = 16;
    arrayOfByte[82] = 17;
    arrayOfByte[83] = 18;
    arrayOfByte[84] = 19;
    arrayOfByte[85] = 20;
    arrayOfByte[86] = 21;
    arrayOfByte[87] = 22;
    arrayOfByte[88] = 23;
    arrayOfByte[89] = 24;
    arrayOfByte[90] = 25;
    arrayOfByte[91] = -1;
    arrayOfByte[92] = -1;
    arrayOfByte[93] = -1;
    arrayOfByte[94] = -1;
    arrayOfByte[95] = 63;
    arrayOfByte[96] = -1;
    arrayOfByte[97] = 26;
    arrayOfByte[98] = 27;
    arrayOfByte[99] = 28;
    arrayOfByte[100] = 29;
    arrayOfByte[101] = 30;
    arrayOfByte[102] = 31;
    arrayOfByte[103] = 32;
    arrayOfByte[104] = 33;
    arrayOfByte[105] = 34;
    arrayOfByte[106] = 35;
    arrayOfByte[107] = 36;
    arrayOfByte[108] = 37;
    arrayOfByte[109] = 38;
    arrayOfByte[110] = 39;
    arrayOfByte[111] = 40;
    arrayOfByte[112] = 41;
    arrayOfByte[113] = 42;
    arrayOfByte[114] = 43;
    arrayOfByte[115] = 44;
    arrayOfByte[116] = 45;
    arrayOfByte[117] = 46;
    arrayOfByte[118] = 47;
    arrayOfByte[119] = 48;
    arrayOfByte[120] = 49;
    arrayOfByte[121] = 50;
    arrayOfByte[122] = 51;
  }

  public TapjoyDisplayAd(Context paramContext)
  {
    this.context = paramContext;
    tapjoyURLConnection = new TapjoyURLConnection();
  }

  private boolean buildResponse(String paramString)
  {
    DocumentBuilderFactory localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    try
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramString.getBytes("UTF-8"));
      Document localDocument = localDocumentBuilderFactory.newDocumentBuilder().parse(localByteArrayInputStream);
      adClickURL = getNodeTrimValue(localDocument.getElementsByTagName("ClickURL"));
      String str = getNodeTrimValue(localDocument.getElementsByTagName("Image"));
      TapjoyLog.i("Display Ad", "decoding...");
      decodeBase64(str.getBytes(), 0, str.getBytes().length);
      TapjoyLog.i("Display Ad", "pos: " + this.pos);
      TapjoyLog.i("Display Ad", "buffer_size: " + this.buffer.length);
      this.bitmapImage = BitmapFactory.decodeByteArray(this.buffer, 0, this.pos);
      TapjoyLog.i("Display Ad", "image: " + this.bitmapImage.getWidth() + "x" + this.bitmapImage.getHeight());
      this.adView = new View(this.context);
      ViewGroup.LayoutParams localLayoutParams = new ViewGroup.LayoutParams(this.bitmapImage.getWidth(), this.bitmapImage.getHeight());
      this.adView.setLayoutParams(localLayoutParams);
      this.adView.setBackgroundDrawable(new BitmapDrawable(this.bitmapImage));
      this.adView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          TapjoyLog.i("Display Ad", "Opening URL in new browser = [" + TapjoyDisplayAd.adClickURL + "]");
          Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse(TapjoyDisplayAd.adClickURL));
          localIntent.setFlags(268435456);
          TapjoyDisplayAd.this.context.startActivity(localIntent);
        }
      });
      displayAdNotifier.getDisplayAdResponse(this.adView);
      return true;
    }
    catch (Exception localException)
    {
      TapjoyLog.e("Display Ad", "Error parsing XML: " + localException.toString());
    }
    return false;
  }

  private String getNodeTrimValue(NodeList paramNodeList)
  {
    Element localElement = (Element)paramNodeList.item(0);
    String str = "";
    if (localElement != null)
    {
      NodeList localNodeList = localElement.getChildNodes();
      int i = localNodeList.getLength();
      for (int j = 0; ; j++)
      {
        if (j >= i)
        {
          if ((str == null) || (str.equals("")))
            break;
          return str.trim();
        }
        Node localNode = localNodeList.item(j);
        if (localNode != null)
          str = String.valueOf(str) + localNode.getNodeValue();
      }
      return null;
    }
    return null;
  }

  void decodeBase64(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.buffer = new byte[paramArrayOfByte.length];
    this.pos = 0;
    this.eof = false;
    this.modulus = 0;
    if (paramInt2 < 0)
      this.eof = true;
    int i = 0;
    int k;
    for (int j = paramInt1; ; j = k)
    {
      if (i >= paramInt2);
      int m;
      while (true)
      {
        if ((this.eof) && (this.modulus != 0))
          this.x <<= 6;
        switch (this.modulus)
        {
        default:
          return;
          k = j + 1;
          m = paramArrayOfByte[j];
          if (m != 61)
            break label128;
          this.eof = true;
        case 2:
        case 3:
        }
      }
      label128: if ((m >= 0) && (m < DECODE_TABLE.length))
      {
        int n = DECODE_TABLE[m];
        if (n >= 0)
        {
          int i1 = 1 + this.modulus;
          this.modulus = i1;
          this.modulus = (i1 % 4);
          this.x = (n + (this.x << 6));
          if (this.modulus == 0)
          {
            byte[] arrayOfByte1 = this.buffer;
            int i2 = this.pos;
            this.pos = (i2 + 1);
            arrayOfByte1[i2] = (byte)(0xFF & this.x >> 16);
            byte[] arrayOfByte2 = this.buffer;
            int i3 = this.pos;
            this.pos = (i3 + 1);
            arrayOfByte2[i3] = (byte)(0xFF & this.x >> 8);
            byte[] arrayOfByte3 = this.buffer;
            int i4 = this.pos;
            this.pos = (i4 + 1);
            arrayOfByte3[i4] = (byte)(0xFF & this.x);
          }
        }
      }
      i++;
    }
    this.x <<= 6;
    byte[] arrayOfByte6 = this.buffer;
    int i7 = this.pos;
    this.pos = (i7 + 1);
    arrayOfByte6[i7] = (byte)(0xFF & this.x >> 16);
    return;
    byte[] arrayOfByte4 = this.buffer;
    int i5 = this.pos;
    this.pos = (i5 + 1);
    arrayOfByte4[i5] = (byte)(0xFF & this.x >> 16);
    byte[] arrayOfByte5 = this.buffer;
    int i6 = this.pos;
    this.pos = (i6 + 1);
    arrayOfByte5[i6] = (byte)(0xFF & this.x >> 8);
  }

  public void getDisplayAdDataFromServer(String paramString1, String paramString2, TapjoyDisplayAdNotifier paramTapjoyDisplayAdNotifier)
  {
    this.baseURL = paramString1;
    this.clickURL = (String.valueOf(this.baseURL) + "display_ad?");
    this.urlParams = paramString2;
    displayAdNotifier = paramTapjoyDisplayAdNotifier;
    this.connectTask = new ConnectTask(null);
    this.connectTask.execute(new Void[0]);
  }

  private class ConnectTask extends AsyncTask<Void, Void, Boolean>
  {
    private ConnectTask()
    {
    }

    protected Boolean doInBackground(Void[] paramArrayOfVoid)
    {
      boolean bool = false;
      String str = TapjoyDisplayAd.tapjoyURLConnection.connectToURL(TapjoyDisplayAd.this.clickURL, TapjoyDisplayAd.this.urlParams);
      if ((str == null) || (str.length() == 0))
        TapjoyDisplayAd.displayAdNotifier.getDisplayAdResponseFailed("Network error.");
      while (true)
      {
        return Boolean.valueOf(bool);
        bool = TapjoyDisplayAd.this.buildResponse(str);
        if (!bool)
          TapjoyDisplayAd.displayAdNotifier.getDisplayAdResponseFailed("No ad to display.");
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyDisplayAd
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */