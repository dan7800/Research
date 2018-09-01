package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Rect;;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View;;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation;;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Interpolator;;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class j
  implements u.a
{
  private static final int a = Color.rgb(102, 102, 102);
  private static final Rect b = new Rect(0, 0, 0, 0);
  private static final PointF c = localPointF;
  private static final PointF d = localPointF;
  private static final PointF e = new PointF(0.5F, 0.5F);
  private static final Matrix f = new Matrix();
  private static final RectF g = new RectF(0.0F, 0.0F, 0.0F, 0.0F);
  private static float h = -1.0F;
  private static Handler i = null;
  private c A;
  private double B;
  private double C;
  private q D;
  private b E;
  private boolean F;
  private s G;
  private String j;
  private boolean k;
  private boolean l;
  private Vector<String> m = new Vector();
  private Rect n;
  private long o = 0L;
  private int p;
  private int q;
  private WeakReference<m> r;
  private k s;
  private int t;
  private int u;
  private int v;
  private JSONObject w;
  private u x;
  private int y;
  private Vector<Bitmap> z;

  static
  {
    PointF localPointF = new PointF(0.0F, 0.0F);
  }

  protected j()
  {
    a(null);
    this.j = null;
    this.n = null;
    this.p = -1;
    this.q = -1;
    this.A = null;
    this.u = -1;
    this.t = -1;
    this.v = -16777216;
    this.x = new u(this);
    this.y = 0;
    this.z = new Vector();
    this.B = -1.0D;
    this.C = -1.0D;
    this.D = new q();
    this.E = b.c;
    h = 5.0F;
    this.F = false;
    this.G = null;
  }

  public static float a(Context paramContext)
  {
    if (h < 0.0F)
      h = paramContext.getSharedPreferences("admob_prefs", 2).getFloat("timeout", 5.0F);
    return h;
  }

  private static float a(JSONObject paramJSONObject, String paramString, float paramFloat)
  {
    return (float)paramJSONObject.optDouble(paramString, paramFloat);
  }

  public static int a(int paramInt, double paramDouble)
  {
    double d1 = paramInt;
    if (paramDouble > 0.0D)
      d1 *= paramDouble;
    return (int)d1;
  }

  private static int a(JSONObject paramJSONObject, String paramString, int paramInt)
  {
    if ((paramJSONObject != null) && (paramJSONObject.has(paramString)))
      try
      {
        JSONArray localJSONArray = paramJSONObject.getJSONArray(paramString);
        int i1 = (int)(255.0D * localJSONArray.getDouble(0));
        int i2 = (int)(255.0D * localJSONArray.getDouble(1));
        int i3 = (int)(255.0D * localJSONArray.getDouble(2));
        int i4 = Color.argb((int)(255.0D * localJSONArray.getDouble(3)), i1, i2, i3);
        return i4;
      }
      catch (Exception localException)
      {
        return paramInt;
      }
    return paramInt;
  }

  private static Matrix a(JSONArray paramJSONArray)
  {
    float[] arrayOfFloat = b(paramJSONArray);
    Matrix localMatrix = null;
    if (arrayOfFloat != null)
    {
      int i1 = arrayOfFloat.length;
      localMatrix = null;
      if (i1 == 9)
      {
        localMatrix = new Matrix();
        localMatrix.setValues(arrayOfFloat);
      }
    }
    return localMatrix;
  }

  private static Matrix a(JSONObject paramJSONObject, String paramString, Matrix paramMatrix)
  {
    float[] arrayOfFloat = b(paramJSONObject, paramString);
    if ((arrayOfFloat != null) && (arrayOfFloat.length == 9))
    {
      Matrix localMatrix = new Matrix();
      localMatrix.setValues(arrayOfFloat);
      return localMatrix;
    }
    return paramMatrix;
  }

  private static PointF a(RectF paramRectF, PointF paramPointF)
  {
    float f1 = paramRectF.width();
    float f2 = paramRectF.height();
    return new PointF(paramRectF.left + f1 * paramPointF.x, paramRectF.top + f2 * paramPointF.y);
  }

  private static PointF a(JSONObject paramJSONObject, String paramString, PointF paramPointF)
  {
    if ((paramJSONObject != null) && (paramJSONObject.has(paramString)))
      try
      {
        PointF localPointF = e(paramJSONObject.getJSONArray(paramString));
        return localPointF;
      }
      catch (JSONException localJSONException)
      {
        return paramPointF;
      }
    return paramPointF;
  }

  private static Rect a(JSONObject paramJSONObject, String paramString, Rect paramRect)
  {
    if ((paramJSONObject != null) && (paramJSONObject.has(paramString)))
      try
      {
        JSONArray localJSONArray = paramJSONObject.getJSONArray(paramString);
        int i1 = (int)localJSONArray.getDouble(0);
        int i2 = (int)localJSONArray.getDouble(1);
        int i3 = (int)localJSONArray.getDouble(2);
        int i4 = (int)localJSONArray.getDouble(3);
        Rect localRect = new Rect(i1, i2, i3 + i1, i4 + i2);
        return localRect;
      }
      catch (JSONException localJSONException)
      {
        return paramRect;
      }
    return paramRect;
  }

  private static RectF a(JSONObject paramJSONObject, String paramString, RectF paramRectF)
  {
    if ((paramJSONObject != null) && (paramJSONObject.has(paramString)))
      try
      {
        RectF localRectF = d(paramJSONObject.getJSONArray(paramString));
        return localRectF;
      }
      catch (JSONException localJSONException)
      {
        return paramRectF;
      }
    return paramRectF;
  }

  private View a(JSONObject paramJSONObject, Rect paramRect)
  {
    float f1;
    float f2;
    int i1;
    if (this.s != null)
    {
      f1 = a(paramJSONObject, "ia", 0.5F);
      f2 = a(paramJSONObject, "epy", 0.4375F);
      i1 = a(paramJSONObject, "bc", this.v);
      try
      {
        Bitmap localBitmap = Bitmap.createBitmap(paramRect.width(), paramRect.height(), Bitmap.Config.ARGB_8888);
        if (localBitmap == null)
          return null;
        this.z.add(localBitmap);
        a(new Canvas(localBitmap), paramRect, i1, -1, (int)(f1 * 255.0F), f2);
        View localView = new View(this.s.getContext());
        localView.setBackgroundDrawable(new BitmapDrawable(localBitmap));
        return localView;
      }
      catch (Throwable localThrowable)
      {
        return null;
      }
    }
    return null;
  }

  private static Animation a(int paramInt, String paramString1, String paramString2, float[] paramArrayOfFloat, JSONArray paramJSONArray1, String[] paramArrayOfString, long paramLong, View paramView, Rect paramRect, JSONObject paramJSONObject, JSONArray paramJSONArray2)
  {
    int i1 = paramInt + 1;
    float f1 = paramArrayOfFloat[paramInt];
    float f2 = paramArrayOfFloat[i1];
    if ((paramString1 == null) || (paramString2 == null))
    {
      if (!InterstitialAd.c.a("AdMobSDK", 6))
        break label520;
      Log.e("AdMobSDK", "Could not read keyframe animation: keyPath(" + paramString1 + ") or valueType(" + paramString2 + ") is null.");
    }
    label520: for (Object localObject = null; ; localObject = null)
      while (true)
      {
        if (localObject != null)
        {
          int i2 = (int)(f1 * (float)paramLong);
          long l1 = (int)((f2 - f1) * (float)paramLong);
          ((Animation)localObject).setDuration(paramLong);
          Interpolator localInterpolator = a(paramArrayOfString[paramInt], i2, l1, paramLong);
          if (localInterpolator != null)
            ((Animation)localObject).setInterpolator(localInterpolator);
        }
        return localObject;
        try
        {
          if (("position".equals(paramString1)) && ("P".equals(paramString2)))
          {
            localObject = a(e(paramJSONArray1.getJSONArray(paramInt)), e(paramJSONArray1.getJSONArray(i1)), paramView, paramRect);
          }
          else if (("opacity".equals(paramString1)) && ("F".equals(paramString2)))
          {
            localObject = a((float)paramJSONArray1.getDouble(paramInt), (float)paramJSONArray1.getDouble(i1));
          }
          else if (("bounds".equals(paramString1)) && ("R".equals(paramString2)))
          {
            localObject = a(d(paramJSONArray1.getJSONArray(paramInt)), d(paramJSONArray1.getJSONArray(i1)), paramView, paramRect);
          }
          else if (("zPosition".equals(paramString1)) && ("F".equals(paramString2)))
          {
            localObject = a((float)paramJSONArray1.getDouble(paramInt), (float)paramJSONArray1.getDouble(i1), paramView);
          }
          else if (("backgroundColor".equals(paramString1)) && ("C".equals(paramString2)))
          {
            localObject = a(c(paramJSONArray1.getJSONArray(paramInt)), c(paramJSONArray1.getJSONArray(i1)), paramView);
          }
          else if (("transform".equals(paramString1)) && ("AT".equals(paramString2)))
          {
            if (paramJSONArray2 != null)
            {
              a(paramJSONArray1.getJSONArray(paramInt));
              a(paramJSONArray1.getJSONArray(i1));
              localObject = a(paramView, paramRect, paramJSONObject, paramJSONArray2.getJSONArray(paramInt), paramJSONArray2.getJSONArray(i1));
            }
          }
          else
          {
            if (InterstitialAd.c.a("AdMobSDK", 6))
              Log.e("AdMobSDK", "Could not read keyframe animation: could not interpret keyPath(" + paramString1 + ") and valueType(" + paramString2 + ") combination.");
            localObject = null;
          }
        }
        catch (JSONException localJSONException)
        {
        }
      }
  }

  private static Animation a(View paramView, Rect paramRect, JSONObject paramJSONObject, JSONArray paramJSONArray1, JSONArray paramJSONArray2)
    throws JSONException
  {
    String str = paramJSONObject.optString("tt", null);
    if (str != null)
    {
      if ("t".equals(str))
        return a(e(paramJSONArray1), e(paramJSONArray2), paramView, paramRect);
      if ("r".equals(str))
      {
        float[] arrayOfFloat5 = b(paramJSONArray1);
        float[] arrayOfFloat6 = b(paramJSONArray2);
        if ((arrayOfFloat5 == null) || (arrayOfFloat6 == null) || (Arrays.equals(arrayOfFloat5, arrayOfFloat6)))
          break label284;
        PointF localPointF3 = ah.b(paramView);
        PointF localPointF4 = a(new RectF(paramRect), localPointF3);
        return new an(arrayOfFloat5, arrayOfFloat6, localPointF4.x, localPointF4.y, 0.0F, false);
      }
      if ("sc".equals(str))
      {
        float[] arrayOfFloat3 = b(paramJSONArray1);
        float[] arrayOfFloat4 = b(paramJSONArray2);
        PointF localPointF2 = ah.b(paramView);
        return new ScaleAnimation(arrayOfFloat3[0], arrayOfFloat4[0], arrayOfFloat3[1], arrayOfFloat4[1], 1, localPointF2.x, 1, localPointF2.y);
      }
      if ("sk".equals(str))
      {
        float[] arrayOfFloat1 = b(paramJSONArray1);
        float[] arrayOfFloat2 = b(paramJSONArray2);
        if ((arrayOfFloat1 != null) && (arrayOfFloat2 != null) && (!Arrays.equals(arrayOfFloat1, arrayOfFloat2)))
        {
          PointF localPointF1 = ah.b(paramView);
          return new ao(arrayOfFloat1, arrayOfFloat2, a(new RectF(paramRect), localPointF1));
        }
      }
      else
      {
        "p".equals(str);
      }
    }
    else
    {
      return null;
    }
    return null;
    label284: return null;
  }

  private AnimationSet a(JSONArray paramJSONArray, JSONObject paramJSONObject, View paramView, Rect paramRect)
    throws JSONException
  {
    AnimationSet localAnimationSet = new AnimationSet(false);
    int i1 = 0;
    if (i1 < paramJSONArray.length())
    {
      JSONObject localJSONObject = paramJSONArray.getJSONObject(i1);
      String str1 = localJSONObject.optString("t", null);
      int i2 = (int)(1000.0D * a(localJSONObject, "d", 0.25F));
      String str2;
      String str3;
      Object localObject2;
      label162: Object localObject1;
      if ("B".equals(str1))
      {
        str2 = localJSONObject.optString("kp", null);
        str3 = localJSONObject.optString("vt", null);
        if ((str2 == null) || (str3 == null))
        {
          if (!InterstitialAd.c.a("AdMobSDK", 6))
            break label643;
          Log.e("AdMobSDK", "Could not read basic animation: keyPath(" + str2 + ") or valueType(" + str3 + ") is null.");
          localObject2 = null;
          if (localObject2 != null)
          {
            Interpolator localInterpolator = a(localJSONObject.optString("tf", null), -1L, -1L, -1L);
            if (localInterpolator == null)
              localInterpolator = null;
            if (localInterpolator != null)
              ((Animation)localObject2).setInterpolator(localInterpolator);
          }
          localObject1 = localObject2;
        }
      }
      while (true)
      {
        if (localObject1 != null)
        {
          ((Animation)localObject1).setDuration(i2);
          a(localJSONObject, (Animation)localObject1, localAnimationSet);
          localAnimationSet.addAnimation((Animation)localObject1);
          ((Animation)localObject1).getDuration();
        }
        i1++;
        break;
        if (("position".equals(str2)) && ("P".equals(str3)))
        {
          localObject2 = a(a(localJSONObject, "fv", c), a(localJSONObject, "tv", d), paramView, paramRect);
          break label162;
        }
        if (("opacity".equals(str2)) && ("F".equals(str3)))
        {
          localObject2 = a(a(localJSONObject, "fv", 0.0F), a(localJSONObject, "tv", 0.0F));
          break label162;
        }
        if (("transform".equals(str2)) && ("AT".equals(str3)))
        {
          a(localJSONObject, "fv", f);
          a(localJSONObject, "fv", f);
          localObject2 = a(paramView, paramRect, localJSONObject, localJSONObject.getJSONArray("tfv"), localJSONObject.getJSONArray("ttv"));
          break label162;
        }
        if (("bounds".equals(str2)) && ("R".equals(str3)))
        {
          localObject2 = a(a(localJSONObject, "fv", g), a(localJSONObject, "tv", g), null, paramRect);
          break label162;
        }
        if (("zPosition".equals(str2)) && ("F".equals(str3)))
        {
          localObject2 = a(a(localJSONObject, "fv", 0.0F), a(localJSONObject, "tv", 0.0F), paramView);
          break label162;
        }
        if (("backgroundColor".equals(str2)) && ("C".equals(str3)))
        {
          localObject2 = a(a(localJSONObject, "fv", 0), a(localJSONObject, "tv", 0), paramView);
          break label162;
        }
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "Could not read basic animation: could not interpret keyPath(" + str2 + ") and valueType(" + str3 + ") combination.");
        label643: localObject2 = null;
        break label162;
        boolean bool = "K".equals(str1);
        localObject1 = null;
        if (bool)
          localObject1 = a(localJSONObject, paramView, paramRect, i2);
      }
    }
    if (paramJSONObject != null)
      a(paramJSONObject, localAnimationSet, null);
    return localAnimationSet;
  }

  private AnimationSet a(JSONObject paramJSONObject, View paramView, Rect paramRect, long paramLong)
    throws JSONException
  {
    String str1 = paramJSONObject.getString("vt");
    float[] arrayOfFloat = b(paramJSONObject, "kt");
    JSONArray localJSONArray1 = paramJSONObject.getJSONArray("vs");
    String[] arrayOfString = a(paramJSONObject, "tfs");
    JSONArray localJSONArray2 = paramJSONObject.optJSONArray("ttvs");
    int i1 = arrayOfFloat.length;
    int i2 = localJSONArray1.length();
    int i3 = arrayOfString.length;
    if (((i1 != i2) || (i2 != i3 + 1)) && (arrayOfFloat[0] == 0.0D) && (arrayOfFloat[(i1 - 1)] == 1.0D))
    {
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "keyframe animations were invalid: numKeyTimes=" + i1 + " numKeyValues=" + i2 + " numKeyFunctions=" + i3 + " keyTimes[0]=" + arrayOfFloat[0] + " keyTimes[" + (i1 - 1) + "]=" + arrayOfFloat[(i1 - 1)]);
      return null;
    }
    AnimationSet localAnimationSet = new AnimationSet(false);
    String str2 = paramJSONObject.getString("kp");
    int i4 = c(paramJSONObject);
    for (int i5 = 0; i5 < i1 - 1; i5++)
    {
      Animation localAnimation = a(i5, str2, str1, arrayOfFloat, localJSONArray1, arrayOfString, paramLong, paramView, paramRect, paramJSONObject, localJSONArray2);
      if (localAnimation != null)
      {
        localAnimation.setRepeatCount(i4);
        localAnimationSet.addAnimation(localAnimation);
      }
    }
    a(paramJSONObject.optString("fm", "r"), localAnimationSet);
    return localAnimationSet;
  }

  private static Interpolator a(String paramString, long paramLong1, long paramLong2, long paramLong3)
  {
    Object localObject;
    if ("i".equals(paramString))
      localObject = new AccelerateInterpolator();
    while (true)
      if ((localObject != null) && (paramLong1 != -1L) && (paramLong2 != -1L) && (paramLong3 != -1L))
      {
        return new ai((Interpolator)localObject, paramLong1, paramLong2, paramLong3);
        if ("o".equals(paramString))
          localObject = new DecelerateInterpolator();
        else if ("io".equals(paramString))
          localObject = new AccelerateDecelerateInterpolator();
        else if ("l".equals(paramString))
          localObject = new LinearInterpolator();
      }
      else
      {
        return localObject;
        localObject = null;
      }
  }

  private static aj a(int paramInt1, int paramInt2, View paramView)
  {
    aj localaj = null;
    if (paramInt1 != paramInt2)
      localaj = new aj(paramInt1, paramInt2, paramView);
    return localaj;
  }

  private static ak a(float paramFloat1, float paramFloat2)
  {
    boolean bool = paramFloat1 < paramFloat2;
    ak localak = null;
    if (bool)
      localak = new ak(paramFloat1, paramFloat2);
    return localak;
  }

  private static al a(RectF paramRectF1, RectF paramRectF2, View paramView, Rect paramRect)
  {
    boolean bool = paramRectF1.equals(paramRectF2);
    al localal = null;
    if (!bool)
    {
      PointF localPointF = a(paramRectF1, ah.b(paramView));
      float f1 = paramRect.width();
      float f2 = paramRect.height();
      float f3 = paramRectF1.width() / f1;
      float f4 = paramRectF1.height() / f2;
      localal = new al(f3, paramRectF2.width() / f1, f4, paramRectF2.height() / f2, localPointF.x, localPointF.y);
    }
    return localal;
  }

  private static am a(PointF paramPointF1, PointF paramPointF2, View paramView, Rect paramRect)
  {
    boolean bool = paramPointF1.equals(paramPointF2);
    am localam = null;
    if (!bool)
    {
      PointF localPointF = ah.b(paramView);
      float f1 = paramRect.width() * localPointF.x + paramRect.left;
      float f2 = paramRect.height() * localPointF.y + paramRect.top;
      paramPointF1.x -= f1;
      paramPointF1.y -= f2;
      paramPointF2.x -= f1;
      paramPointF2.y -= f2;
      localam = new am(0, paramPointF1.x, 0, paramPointF2.x, 0, paramPointF1.y, 0, paramPointF2.y);
    }
    return localam;
  }

  private static ap a(float paramFloat1, float paramFloat2, View paramView)
  {
    boolean bool = paramFloat1 < paramFloat2;
    ap localap = null;
    if (bool)
      localap = new ap(paramFloat1, paramFloat2, paramView);
    return localap;
  }

  public static j a(m paramm, Context paramContext, JSONObject paramJSONObject, int paramInt1, int paramInt2, int paramInt3, k paramk, b paramb)
  {
    j localj;
    if ((paramJSONObject == null) || (paramJSONObject.length() == 0))
      localj = null;
    while (true)
    {
      return localj;
      localj = new j();
      localj.E = paramb;
      localj.a(paramm);
      localj.t = paramInt1;
      localj.u = paramInt2;
      localj.v = paramInt3;
      localj.s = paramk;
      localj.G = s.a(paramContext);
      localj.x.c = localj.G;
      boolean bool;
      if (localj.E == b.b)
      {
        float f1 = a(paramJSONObject, "timeout", 0.0F);
        if (f1 > 0.0F)
        {
          h = f1;
          SharedPreferences.Editor localEditor = paramContext.getSharedPreferences("admob_prefs", 2).edit();
          localEditor.putFloat("timeout", h);
          localEditor.commit();
        }
        localj.D.a(paramContext, paramJSONObject, localj.x);
        localj.D.a.l = true;
        a locala = localj.D.a.a;
        if ((locala != a.d) && (locala != a.c))
          bool = false;
      }
      while (!bool)
      {
        return null;
        localj.x.b();
        if (localj.x.a())
          localj.k();
        bool = true;
        continue;
        bool = localj.a(paramContext, paramJSONObject);
      }
    }
  }

  public static void a(Canvas paramCanvas, Rect paramRect, int paramInt1, int paramInt2, int paramInt3, float paramFloat)
  {
    int i1 = (int)(paramFloat * paramRect.height()) + paramRect.top;
    Rect localRect1 = new Rect(paramRect.left, paramRect.top, paramRect.right, i1);
    Paint localPaint1 = new Paint();
    localPaint1.setColor(-1);
    localPaint1.setStyle(Paint.Style.FILL);
    paramCanvas.drawRect(localRect1, localPaint1);
    int[] arrayOfInt = { Color.argb(paramInt3, Color.red(paramInt1), Color.green(paramInt1), Color.blue(paramInt1)), paramInt1 };
    GradientDrawable localGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, arrayOfInt);
    localGradientDrawable.setBounds(localRect1);
    localGradientDrawable.draw(paramCanvas);
    Rect localRect2 = new Rect(paramRect.left, i1, paramRect.right, paramRect.bottom);
    Paint localPaint2 = new Paint();
    localPaint2.setColor(paramInt1);
    localPaint2.setStyle(Paint.Style.FILL);
    paramCanvas.drawRect(localRect2, localPaint2);
  }

  public static void a(Handler paramHandler)
  {
    i = paramHandler;
  }

  private static void a(Animation paramAnimation, int paramInt1, int paramInt2, float paramFloat, String paramString, boolean paramBoolean)
  {
    if (paramBoolean)
      paramAnimation.setRepeatMode(2);
    paramAnimation.setRepeatCount(paramInt1);
    paramAnimation.setStartOffset(paramInt2);
    paramAnimation.startNow();
    paramAnimation.scaleCurrentDuration(paramFloat);
    a(paramString, paramAnimation);
  }

  private void a(ImageView paramImageView, Bitmap paramBitmap, JSONObject paramJSONObject)
  {
    f1 = a(paramJSONObject, "bw", 0.5F);
    i1 = a(paramJSONObject, "bdc", a);
    f2 = a(paramJSONObject, "br", 6.5F);
    if (f1 < 1.0F)
      f1 = 1.0F;
    i2 = paramBitmap.getWidth();
    i3 = paramBitmap.getHeight();
    try
    {
      Bitmap localBitmap2 = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
      if (localBitmap2 == null)
        return;
      localBitmap2.eraseColor(0);
      Canvas localCanvas = new Canvas(localBitmap2);
      localCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 1));
      float f3 = f2 + f1;
      Path localPath1 = new Path();
      RectF localRectF = new RectF(0.0F, 0.0F, i2, i3);
      localPath1.addRoundRect(localRectF, f3, f3, Path.Direction.CCW);
      localCanvas.clipPath(localPath1, Region.Op.REPLACE);
      localCanvas.drawBitmap(paramBitmap, 0.0F, 0.0F, new Paint(3));
      localCanvas.clipRect(localRectF, Region.Op.REPLACE);
      Paint localPaint = new Paint(1);
      localPaint.setStrokeWidth(f1);
      localPaint.setColor(i1);
      localPaint.setStyle(Paint.Style.STROKE);
      Path localPath2 = new Path();
      float f4 = f1 / 2.0F;
      localPath2.addRoundRect(new RectF(f4, f4, i2 - f4, i3 - f4), f2, f2, Path.Direction.CCW);
      localCanvas.drawPath(localPath2, localPaint);
      if (paramBitmap != null)
        paramBitmap.recycle();
      localBitmap1 = localBitmap2;
      this.z.add(localBitmap1);
      paramImageView.setImageBitmap(localBitmap1);
      return;
    }
    catch (Throwable localThrowable)
    {
      while (true)
        Bitmap localBitmap1 = paramBitmap;
    }
  }

  private void a(m paramm)
  {
    this.r = new WeakReference(paramm);
  }

  private void a(String paramString)
  {
    if ((paramString != null) && (!"".equals(paramString)))
      this.m.add(paramString);
  }

  private static void a(String paramString, Animation paramAnimation)
  {
    if ((paramString != null) && (paramAnimation != null))
    {
      Class localClass = paramAnimation.getClass();
      try
      {
        Class[] arrayOfClass = new Class[1];
        arrayOfClass[0] = Boolean.TYPE;
        Method localMethod = localClass.getMethod("setFillEnabled", arrayOfClass);
        if (localMethod != null)
        {
          Object[] arrayOfObject = new Object[1];
          arrayOfObject[0] = Boolean.valueOf(true);
          localMethod.invoke(paramAnimation, arrayOfObject);
        }
        if ("b".equals(paramString))
        {
          paramAnimation.setFillBefore(true);
          paramAnimation.setFillAfter(false);
        }
        do
        {
          return;
          if (("fb".equals(paramString)) || ("r".equals(paramString)))
          {
            paramAnimation.setFillBefore(true);
            paramAnimation.setFillAfter(true);
            return;
          }
          if ("f".equals(paramString))
          {
            paramAnimation.setFillBefore(false);
            paramAnimation.setFillAfter(true);
            return;
          }
        }
        while (!"r".equals(paramString));
        paramAnimation.setFillBefore(false);
        paramAnimation.setFillAfter(false);
        return;
      }
      catch (Exception localException)
      {
        break label65;
      }
    }
  }

  private void a(JSONObject paramJSONObject, Animation paramAnimation, AnimationSet paramAnimationSet)
  {
    float f1 = a(paramJSONObject, "bt", 0.0F);
    float f2 = a(paramJSONObject, "to", 0.0F);
    int i1 = c(paramJSONObject);
    boolean bool = paramJSONObject.optBoolean("ar", false);
    String str = paramJSONObject.optString("fm", "r");
    float f3 = a(paramJSONObject, "s", 1.0F);
    int i2 = (int)(1000.0D * f2 + (f1 + 0.0F));
    float f4 = 1.0F / f3;
    a(paramAnimation, i1, i2, f4, str, bool);
    if (paramAnimationSet != null)
      a(paramAnimationSet, i1, i2, f4, str, bool);
  }

  private boolean a(Context paramContext, JSONObject paramJSONObject)
  {
    JSONObject localJSONObject1 = paramJSONObject.optJSONObject("o");
    if (localJSONObject1 != null)
    {
      this.D.a(paramContext, localJSONObject1, null);
      String str3 = paramJSONObject.optString("jsonp_url", null);
      String str4 = paramJSONObject.optString("tracking_url", null);
      this.D.a.a(str3, true);
      this.D.a.a(str4, false);
      if (paramJSONObject.has("refreshInterval"))
        this.B = paramJSONObject.optDouble("refreshInterval");
      if (!paramJSONObject.has("density"))
        break label280;
    }
    PointF localPointF;
    label280: for (this.C = paramJSONObject.optDouble("density"); ; this.C = k.d())
    {
      localPointF = a(paramJSONObject, "d", null);
      if (localPointF == null)
        localPointF = new PointF(320.0F, 48.0F);
      if ((localPointF.x >= 0.0F) && (localPointF.y >= 0.0F))
        break label291;
      return false;
      this.j = paramJSONObject.optString("text", null);
      String str1 = paramJSONObject.optString("6", null);
      q localq1 = this.D;
      String str2 = paramJSONObject.optString("8", null);
      localq1.a.b = str2;
      a locala = a.a(str1);
      this.D.a.a = locala;
      JSONArray localJSONArray = paramJSONObject.optJSONArray("ac");
      if (localJSONArray != null)
        this.D.a(paramContext, localJSONArray);
      JSONObject localJSONObject2 = paramJSONObject.optJSONObject("ac");
      if (localJSONObject2 == null)
        break;
      this.D.a(paramContext, localJSONObject2);
      break;
    }
    label291: int i1 = (int)localPointF.x;
    int i2 = (int)localPointF.y;
    this.p = i1;
    this.q = i2;
    String str5 = paramJSONObject.optString("cpm_url", null);
    if (str5 != null)
    {
      this.k = true;
      a(str5);
    }
    Object localObject = paramJSONObject.optString("tracking_pixel", null);
    if (localObject != null);
    try
    {
      new URL((String)localObject);
      if (localObject != null)
        a((String)localObject);
      localJSONObject3 = paramJSONObject.optJSONObject("markup");
      if ((this.D.a.a == a.b) && (!(this.s.getContext() instanceof Activity)))
      {
        r();
        return false;
      }
    }
    catch (MalformedURLException localMalformedURLException)
    {
      try
      {
        while (true)
        {
          str7 = URLEncoder.encode((String)localObject, "UTF-8");
          localObject = str7;
        }
        if (localJSONObject3 == null)
          return false;
        localq2 = this.D;
        if ((localq2.a.c != null) && (localq2.a.c.size() > 0));
        for (i3 = 1; i3 == 0; i3 = 0)
        {
          if (InterstitialAd.c.a("AdMobSDK", 6))
            Log.e("AdMobSDK", "Bad response:  didn't get clickURLString.  erroring out.");
          return false;
        }
        this.w = localJSONObject3;
        try
        {
          localJSONObject4 = this.w.optJSONObject("$");
          if (this.s != null)
          {
            str6 = AdManager.getUserId(this.s.getContext());
            this.x.a(localJSONObject4, str6);
            p();
            d1 = this.w.optDouble("itid");
            if (d1 > 0.0D)
              this.o = ()(d1 * 1000.0D);
            this.x.b();
            if (this.x.a())
              k();
            return true;
          }
        }
        catch (JSONException localJSONException)
        {
          while (true)
            if (InterstitialAd.c.a("AdMobSDK", 6))
              Log.e("AdMobSDK", "Could not read in the flex ad.", localJSONException);
        }
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
      }
    }
    while (true)
    {
      JSONObject localJSONObject3;
      String str7;
      q localq2;
      int i3;
      JSONObject localJSONObject4;
      double d1;
      break;
      String str6 = null;
    }
  }

  private static String[] a(JSONObject paramJSONObject, String paramString)
  {
    JSONArray localJSONArray = paramJSONObject.optJSONArray(paramString);
    if (localJSONArray == null)
      return null;
    int i1 = localJSONArray.length();
    try
    {
      String[] arrayOfString = new String[i1];
      for (int i2 = 0; i2 < i1; i2++)
        arrayOfString[i2] = localJSONArray.getString(i2);
      return arrayOfString;
    }
    catch (JSONException localJSONException)
    {
    }
    return null;
  }

  private View b(JSONObject paramJSONObject)
  {
    try
    {
      k localk1 = this.s;
      localObject1 = null;
      if (localk1 == null)
        break label1089;
      localObject1 = null;
      if (paramJSONObject == null)
        break label1089;
      str1 = paramJSONObject.getString("t");
      localRect = a(a(paramJSONObject, "f", b));
      if ("l".equals(str1))
      {
        if (this.s == null)
          break label1092;
        str4 = paramJSONObject.getString("x");
        f1 = a(paramJSONObject, "fs", 13.0F);
        localJSONArray2 = paramJSONObject.optJSONArray("fa");
        localTypeface1 = Typeface.DEFAULT;
        if (localJSONArray2 == null)
          break label1059;
        i6 = 0;
        i7 = 0;
        if (i7 < localJSONArray2.length())
        {
          str5 = localJSONArray2.getString(i7);
          if ("b".equals(str5))
          {
            i6 |= 1;
            break label1066;
          }
          if ("i".equals(str5))
          {
            i6 |= 2;
            break label1066;
          }
          if ("m".equals(str5))
          {
            localTypeface1 = Typeface.MONOSPACE;
            break label1066;
          }
          if ("s".equals(str5))
          {
            localTypeface1 = Typeface.SERIF;
            break label1066;
          }
          if (!"ss".equals(str5))
            break label1066;
          localTypeface1 = Typeface.SANS_SERIF;
          break label1066;
        }
        localTypeface2 = Typeface.create(localTypeface1, i6);
        i3 = this.t;
        if (paramJSONObject.has("fco"))
        {
          i5 = a(paramJSONObject, "fco", i3);
          if (i5 != i3)
            i3 = i5;
          bool2 = paramJSONObject.optBoolean("afstfw", true);
          f2 = a(paramJSONObject, "mfs", 8.0F);
          i4 = paramJSONObject.optInt("nol", 1);
          localaf2 = new af(this.s.getContext(), k.d());
          localaf2.b = bool2;
          localaf2.a = (f2 * localaf2.c);
          localaf2.setBackgroundColor(0);
          localaf2.setText(str4);
          localaf2.setTextColor(i3);
          localaf2.setTextSize(f1);
          localaf2.setTypeface(localTypeface2);
          localaf2.setLines(i4);
          localaf1 = localaf2;
          break label1072;
        }
      }
      while (true)
        if (localObject2 != null)
          if (i2 != 0)
          {
            ((View)localObject2).setBackgroundColor(a(paramJSONObject, "bgc", 0));
            localPointF = a(paramJSONObject, "ap", e);
            localah = ah.c((View)localObject2);
            localah.b = localPointF;
            ((View)localObject2).setTag(localah);
            localJSONArray1 = paramJSONObject.optJSONArray("a");
            localJSONObject = paramJSONObject.optJSONObject("ag");
            localAnimationSet = null;
            if (localJSONArray1 != null)
              localAnimationSet = a(localJSONArray1, localJSONObject, (View)localObject2, localRect);
            str2 = paramJSONObject.optString("ut", null);
            if ((localObject2 != null) && (str2 != null))
              ((View)localObject2).setTag(ah.c((View)localObject2));
            localLayoutParams = new RelativeLayout.LayoutParams(localRect.width(), localRect.height());
            localLayoutParams.addRule(9);
            localLayoutParams.addRule(10);
            localLayoutParams.setMargins(localRect.left, localRect.top, 0, 0);
            ((View)localObject2).setLayoutParams(localLayoutParams);
            if (localAnimationSet != null)
              ((View)localObject2).setAnimation(localAnimationSet);
            if ((!paramJSONObject.optBoolean("cav")) || (this.s == null))
              break label1085;
            this.s.a((View)localObject2, localLayoutParams);
            break label1085;
            if (paramJSONObject.optInt("fc", 0) == 1)
            {
              i3 = this.u;
              break;
            }
            i3 = this.t;
            break;
            if ("bg".equals(str1))
            {
              localObject2 = a(paramJSONObject, localRect);
              i1 = 0;
              i2 = 0;
              continue;
            }
            if ("i".equals(str1))
            {
              localk2 = this.s;
              localObject3 = null;
              if (localk2 == null)
                break label1098;
              str3 = paramJSONObject.getString("$");
              if (str3 != null)
              {
                localu = this.x;
                if (localu.a == null)
                  break label1111;
                localBitmap = (Bitmap)localu.a.get(str3);
                if (localBitmap != null)
                {
                  localImageView = new ImageView(this.s.getContext());
                  localImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                  if (paramJSONObject.optBoolean("b", false))
                  {
                    a(localImageView, localBitmap, paramJSONObject);
                    localObject3 = localImageView;
                  }
                  else
                  {
                    this.z.add(localBitmap);
                    localImageView.setImageBitmap(localBitmap);
                    localObject3 = localImageView;
                  }
                }
                else
                {
                  if (!InterstitialAd.c.a("AdMobSDK", 6))
                    break label1117;
                  Log.e("AdMobSDK", "couldn't find Bitmap " + str3);
                  break label1117;
                }
              }
              else
              {
                bool1 = InterstitialAd.c.a("AdMobSDK", 3);
                localObject3 = null;
                if (bool1)
                {
                  Log.d("AdMobSDK", "Could not find asset name " + paramJSONObject);
                  localObject3 = null;
                }
              }
            }
          }
    }
    catch (JSONException localJSONException)
    {
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "exception while trying to create a flex view.", localJSONException);
      return null;
    }
    Object localObject1;
    af localaf1;
    Object localObject2;
    int i2;
    int i1;
    Object localObject3;
    Bitmap localBitmap;
    label745: View localView2;
    while (true)
    {
      String str1;
      Rect localRect;
      String str4;
      float f1;
      JSONArray localJSONArray2;
      Typeface localTypeface1;
      int i6;
      int i7;
      String str5;
      Typeface localTypeface2;
      int i3;
      int i5;
      boolean bool2;
      float f2;
      int i4;
      af localaf2;
      PointF localPointF;
      ah localah;
      JSONArray localJSONArray1;
      JSONObject localJSONObject;
      AnimationSet localAnimationSet;
      String str2;
      RelativeLayout.LayoutParams localLayoutParams;
      k localk2;
      String str3;
      u localu;
      ImageView localImageView;
      boolean bool1;
      if ("P".equals(str1))
      {
        if (this.s == null)
          break label1136;
        localView2 = new View(this.s.getContext());
        break;
      }
      if ("wv".equals(str1))
      {
        View localView1 = d(paramJSONObject);
        i2 = 1;
        localObject2 = localView1;
        i1 = 1;
        continue;
        if (i1 != 0)
        {
          ((View)localObject2).setBackgroundDrawable(null);
          continue;
          if (InterstitialAd.c.a("AdMobSDK", 6))
            Log.e("AdMobSDK", "created a null view.");
          return null;
        }
      }
      else
      {
        i1 = 1;
        i2 = 1;
        localObject2 = null;
        continue;
        label1059: localTypeface2 = localTypeface1;
        continue;
        label1066: i7++;
      }
    }
    while (true)
    {
      label1072: i2 = 1;
      localObject2 = localaf1;
      i1 = 1;
      break;
      label1085: localObject1 = localObject2;
      label1089: return localObject1;
      label1092: localaf1 = null;
    }
    while (true)
    {
      label1098: i2 = 1;
      localObject2 = localObject3;
      i1 = 1;
      break;
      label1111: localBitmap = null;
      break label745;
      label1117: localObject3 = null;
    }
    while (true)
    {
      i2 = 1;
      localObject2 = localView2;
      i1 = 1;
      break;
      label1136: localView2 = null;
    }
  }

  private static JSONArray b(int paramInt)
  {
    JSONArray localJSONArray = new JSONArray();
    localJSONArray.put(Color.red(paramInt));
    localJSONArray.put(Color.green(paramInt));
    localJSONArray.put(Color.blue(paramInt));
    localJSONArray.put(Color.alpha(paramInt));
    return localJSONArray;
  }

  private static float[] b(JSONArray paramJSONArray)
  {
    int i1 = paramJSONArray.length();
    try
    {
      float[] arrayOfFloat = new float[i1];
      for (int i2 = 0; i2 < i1; i2++)
        arrayOfFloat[i2] = (float)paramJSONArray.getDouble(i2);
      return arrayOfFloat;
    }
    catch (JSONException localJSONException)
    {
    }
    return null;
  }

  private static float[] b(JSONObject paramJSONObject, String paramString)
  {
    JSONArray localJSONArray = paramJSONObject.optJSONArray(paramString);
    if (localJSONArray == null)
      return null;
    return b(localJSONArray);
  }

  private static int c(JSONArray paramJSONArray)
    throws JSONException
  {
    int i1 = (int)(255.0D * paramJSONArray.getDouble(0));
    int i2 = (int)(255.0D * paramJSONArray.getDouble(1));
    int i3 = (int)(255.0D * paramJSONArray.getDouble(2));
    return Color.argb((int)(255.0D * paramJSONArray.getDouble(3)), i1, i2, i3);
  }

  private static int c(JSONObject paramJSONObject)
  {
    int i1 = (int)a(paramJSONObject, "rc", 1.0F);
    if (i1 > 0)
      i1--;
    return i1;
  }

  private static RectF d(JSONArray paramJSONArray)
    throws JSONException
  {
    float f1 = (float)paramJSONArray.getDouble(0);
    float f2 = (float)paramJSONArray.getDouble(1);
    float f3 = (float)paramJSONArray.getDouble(2);
    float f4 = (float)paramJSONArray.getDouble(3);
    return new RectF(f1, f2, f3 + f1, f4 + f2);
  }

  private View d(JSONObject paramJSONObject)
  {
    if (this.s != null)
    {
      String str1 = paramJSONObject.optString("u");
      str2 = paramJSONObject.optString("html");
      str3 = paramJSONObject.optString("base");
      this.y = (1 + this.y);
      localz = new z(this.s.getContext(), this);
      if ((str1 != null) && (!str1.equals("")))
      {
        localz.b(str1);
        localz.loadUrl(str1);
        while (true)
        {
          JSONObject localJSONObject1 = paramJSONObject.optJSONObject("d");
          if (localJSONObject1 != null)
            localz.a = localJSONObject1;
          AdView localAdView = this.s.b;
          localJSONObject2 = new JSONObject();
          try
          {
            localJSONObject2.put("ptc", b(localAdView.getPrimaryTextColor()));
            localJSONObject2.put("stc", b(localAdView.getSecondaryTextColor()));
            localJSONObject2.put("bgc", b(localAdView.getBackgroundColor()));
            label175: localz.b = localJSONObject2;
            localz.b();
            this.F = true;
            return localz;
            if ((str2 != null) && (!str2.equals("")) && (str3 != null) && (!str3.equals("")))
            {
              localz.loadDataWithBaseURL(str3, str2, null, null, null);
              continue;
            }
            a(false);
            return null;
            return null;
          }
          catch (JSONException localJSONException)
          {
            break label175;
          }
        }
      }
    }
  }

  private static PointF e(JSONArray paramJSONArray)
    throws JSONException
  {
    return new PointF((float)paramJSONArray.getDouble(0), (float)paramJSONArray.getDouble(1));
  }

  public static float n()
  {
    return h;
  }

  private boolean o()
  {
    return this.y <= 0;
  }

  private void p()
  {
    localObject = new Rect(0, 0, this.p, this.q);
    if (this.w.has("ta"))
      try
      {
        JSONArray localJSONArray = this.w.getJSONArray("ta");
        int i1 = localJSONArray.getInt(0);
        int i2 = localJSONArray.getInt(1);
        int i3 = localJSONArray.getInt(2);
        int i4 = localJSONArray.getInt(3);
        Rect localRect = new Rect(i1, i2, i3 + i1, i4 + i2);
        if (Math.abs(localRect.width()) >= 44)
        {
          int i5 = Math.abs(localRect.height());
          if (i5 >= 44)
            localObject = localRect;
        }
        this.n = ((Rect)localObject);
        return;
      }
      catch (JSONException localJSONException)
      {
        while (true)
          if (InterstitialAd.c.a("AdMobSDK", 3))
            Log.d("AdMobSDK", "could not read in the touchable area for the ad.");
      }
  }

  private void q()
  {
    m localm = (m)this.r.get();
    if (localm != null)
      localm.a(this);
    if (this.G != null)
      s.a();
  }

  private void r()
  {
    m localm = (m)this.r.get();
    if (localm != null)
      localm.a();
    if (this.G != null)
      s.a();
  }

  public final int a(int paramInt)
  {
    double d1 = paramInt;
    if (this.C > 0.0D)
      d1 *= this.C;
    return (int)d1;
  }

  final Rect a(Rect paramRect)
  {
    Rect localRect = new Rect(paramRect);
    if (this.C > 0.0D)
    {
      localRect.left = a(paramRect.left);
      localRect.top = a(paramRect.top);
      localRect.right = a(paramRect.right);
      localRect.bottom = a(paramRect.bottom);
    }
    return localRect;
  }

  public final q a()
  {
    return this.D;
  }

  public final void a(c paramc)
  {
    this.A = paramc;
  }

  public final void a(k paramk)
  {
    this.s = paramk;
  }

  public final void a(JSONObject paramJSONObject)
  {
    Context localContext2;
    if (this.l)
    {
      if (InterstitialAd.c.a("AdMobSDK", 4))
        Log.i("AdMobSDK", "Ad clicked again.  Stats on admob.com will only reflect the first click.");
      if (this.s == null)
        break label183;
      localContext2 = this.s.getContext();
      if (!(localContext2 instanceof Activity))
        break label183;
    }
    label183: for (Activity localActivity = (Activity)localContext2; ; localActivity = null)
    {
      if (localActivity != null)
        this.D.a(localActivity, this.s);
      while (true)
      {
        if (this.A != null)
          this.A.a();
        return;
        this.l = true;
        if (InterstitialAd.c.a("AdMobSDK", 4))
          Log.i("AdMobSDK", "Ad clicked.");
        if (this.s == null)
          break;
        Context localContext1 = this.s.getContext();
        q localq = this.D;
        String str = AdManager.getUserId(localContext1);
        q.a(localq.a.c, paramJSONObject, str);
        break;
        if (InterstitialAd.c.a("AdMobSDK", 3))
          Log.d("AdMobSDK", "Context null, not able to finish click.");
      }
    }
  }

  public final void a(boolean paramBoolean)
  {
    this.y -= 1;
    if (paramBoolean)
    {
      if (o())
        q();
      return;
    }
    this.x.c();
  }

  public final double b()
  {
    return this.B;
  }

  public final k c()
  {
    return this.s;
  }

  public final long d()
  {
    return this.o;
  }

  public final boolean e()
  {
    return this.k;
  }

  public final boolean equals(Object paramObject)
  {
    if ((paramObject instanceof j))
    {
      j localj = (j)paramObject;
      return toString().equals(localj.toString());
    }
    return false;
  }

  public final int f()
  {
    return this.p;
  }

  public final int g()
  {
    return this.q;
  }

  public final Rect h()
  {
    if (this.n == null)
      this.n = new Rect(0, 0, this.p, this.q);
    return this.n;
  }

  public final int hashCode()
  {
    return toString().hashCode();
  }

  final void i()
  {
    Iterator localIterator = this.z.iterator();
    while (localIterator.hasNext())
    {
      Bitmap localBitmap = (Bitmap)localIterator.next();
      if (localBitmap != null)
        localBitmap.recycle();
    }
    this.z.clear();
    if (this.D != null)
      this.D.a();
  }

  final void j()
  {
    if (this.s != null)
    {
      Context localContext = this.s.getContext();
      Iterator localIterator = this.m.iterator();
      while (localIterator.hasNext())
        g.a((String)localIterator.next(), "impression_request", AdManager.getUserId(localContext)).f();
    }
  }

  // ERROR //
  public final void k()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 158	com/admob/android/ads/j:D	Lcom/admob/android/ads/q;
    //   4: ifnull +34 -> 38
    //   7: aload_0
    //   8: getfield 158	com/admob/android/ads/j:D	Lcom/admob/android/ads/q;
    //   11: invokevirtual 1348	com/admob/android/ads/q:c	()Z
    //   14: ifeq +17 -> 31
    //   17: aload_0
    //   18: getfield 158	com/admob/android/ads/j:D	Lcom/admob/android/ads/q;
    //   21: aload_0
    //   22: getfield 143	com/admob/android/ads/j:x	Lcom/admob/android/ads/u;
    //   25: getfield 1167	com/admob/android/ads/u:a	Ljava/util/Hashtable;
    //   28: invokevirtual 1351	com/admob/android/ads/q:a	(Ljava/util/Hashtable;)V
    //   31: aload_0
    //   32: getfield 158	com/admob/android/ads/j:D	Lcom/admob/android/ads/q;
    //   35: invokevirtual 1352	com/admob/android/ads/q:b	()V
    //   38: aload_0
    //   39: getfield 1015	com/admob/android/ads/j:w	Lorg/json/JSONObject;
    //   42: ifnull +136 -> 178
    //   45: aload_0
    //   46: getfield 1015	com/admob/android/ads/j:w	Lorg/json/JSONObject;
    //   49: astore_1
    //   50: aload_0
    //   51: aconst_null
    //   52: putfield 1015	com/admob/android/ads/j:w	Lorg/json/JSONObject;
    //   55: aload_1
    //   56: ldc_w 1353
    //   59: invokevirtual 543	org/json/JSONObject:optJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   62: astore 7
    //   64: aload 7
    //   66: ifnull +72 -> 138
    //   69: new 1355	com/admob/android/ads/j$d
    //   72: dup
    //   73: aload_0
    //   74: aload 7
    //   76: invokespecial 1357	com/admob/android/ads/j$d:<init>	(Lcom/admob/android/ads/j;Lorg/json/JSONArray;)V
    //   79: astore 8
    //   81: getstatic 108	com/admob/android/ads/j:i	Landroid/os/Handler;
    //   84: ifnull +12 -> 96
    //   87: getstatic 108	com/admob/android/ads/j:i	Landroid/os/Handler;
    //   90: aload 8
    //   92: invokevirtual 1363	android/os/Handler:post	(Ljava/lang/Runnable;)Z
    //   95: pop
    //   96: aload_0
    //   97: getfield 143	com/admob/android/ads/j:x	Lcom/admob/android/ads/u;
    //   100: astore 4
    //   102: aload 4
    //   104: getfield 1366	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   107: ifnull +30 -> 137
    //   110: aload 4
    //   112: getfield 1366	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   115: astore 5
    //   117: aload 5
    //   119: monitorenter
    //   120: aload 4
    //   122: getfield 1366	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   125: invokevirtual 1369	java/util/HashSet:clear	()V
    //   128: aload 4
    //   130: aconst_null
    //   131: putfield 1366	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   134: aload 5
    //   136: monitorexit
    //   137: return
    //   138: aload_0
    //   139: invokespecial 828	com/admob/android/ads/j:r	()V
    //   142: goto -46 -> 96
    //   145: astore_2
    //   146: ldc_w 325
    //   149: iconst_3
    //   150: invokestatic 330	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   153: ifeq -57 -> 96
    //   156: ldc_w 325
    //   159: ldc_w 1371
    //   162: aload_2
    //   163: invokestatic 1373	android/util/Log:d	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   166: pop
    //   167: goto -71 -> 96
    //   170: astore 6
    //   172: aload 5
    //   174: monitorexit
    //   175: aload 6
    //   177: athrow
    //   178: aload_0
    //   179: invokespecial 875	com/admob/android/ads/j:o	()Z
    //   182: ifeq -45 -> 137
    //   185: aload_0
    //   186: invokespecial 877	com/admob/android/ads/j:q	()V
    //   189: return
    //
    // Exception table:
    //   from	to	target	type
    //   55	64	145	org/json/JSONException
    //   69	96	145	org/json/JSONException
    //   138	142	145	org/json/JSONException
    //   120	137	170	finally
  }

  public final void l()
  {
    this.w = null;
    if (InterstitialAd.c.a("AdMobSDK", 4))
      Log.i("AdMobSDK", "assetsDidFailToLoad()");
    r();
  }

  public final boolean m()
  {
    return this.F;
  }

  public final String toString()
  {
    String str = this.j;
    if (str == null)
      str = "";
    return str;
  }

  public static enum a
  {
    private String k;

    static
    {
      a = new a("CLICK_TO_BROWSER", 3, "url");
      h = new a("CLICK_TO_CALL", 4, "call");
      i = new a("CLICK_TO_MUSIC", 5, "itunes");
      b = new a("CLICK_TO_CANVAS", 6, "canvas");
      j = new a("CLICK_TO_CONTACT", 7, "contact");
      c = new a("CLICK_TO_INTERACTIVE_VIDEO", 8, "movie");
      d = new a("CLICK_TO_FULLSCREEN_BROWSER", 9, "screen");
      a[] arrayOfa = new a[10];
      arrayOfa[0] = e;
      arrayOfa[1] = f;
      arrayOfa[2] = g;
      arrayOfa[3] = a;
      arrayOfa[4] = h;
      arrayOfa[5] = i;
      arrayOfa[6] = b;
      arrayOfa[7] = j;
      arrayOfa[8] = c;
      arrayOfa[9] = d;
    }

    private a(String paramString)
    {
      this.k = paramString;
    }

    public static a a(String paramString)
    {
      a[] arrayOfa = values();
      int m = arrayOfa.length;
      for (int n = 0; ; n++)
      {
        Object localObject = null;
        if (n < m)
        {
          a locala = arrayOfa[n];
          if (locala.toString().equals(paramString))
            localObject = locala;
        }
        else
        {
          return localObject;
        }
      }
    }

    public final String toString()
    {
      return this.k;
    }
  }

  public static enum b
  {
    private String d;

    static
    {
      b[] arrayOfb = new b[3];
      arrayOfb[0] = a;
      arrayOfb[1] = b;
      arrayOfb[2] = c;
    }

    private b(String paramString)
    {
      this.d = paramString;
    }

    public final String toString()
    {
      return this.d;
    }
  }

  public static abstract interface c
  {
    public abstract void a();
  }

  static final class d
    implements Runnable
  {
    private WeakReference<j> a;
    private JSONArray b;

    public d(j paramj, JSONArray paramJSONArray)
    {
      this.a = new WeakReference(paramj);
      this.b = paramJSONArray;
    }

    public final void run()
    {
      try
      {
        j localj2 = (j)this.a.get();
        if (localj2 != null)
          j.a(localj2, this.b);
        return;
      }
      catch (Exception localException)
      {
        j localj1;
        do
        {
          if (InterstitialAd.c.a("AdMobSDK", 6))
          {
            Log.e("AdMobSDK", "exception caught in Ad$ViewAdd.run(), " + localException.getMessage());
            localException.printStackTrace();
          }
          localj1 = (j)this.a.get();
        }
        while (localj1 == null);
        j.a(localj1);
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.j
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */