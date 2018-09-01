package com.nubee.coinpirates.game;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import com.nubee.coinpirates.common.Coins7Log;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer
  implements GLSurfaceView.Renderer, View.OnTouchListener
{
  static final int ITEM = 1;
  static final int JACKPOT = 3;
  static final int NORMAL = 0;
  static final int[] RESOURCE_IDS = { 2130837555, 2130837509, 2130837544, 2130837550, 2130837542, 2130837530, 2130837531, 2130837511, 2130837548, 2130837552, 2130837551, 2130837545, 2130837510, 2130837533, 2130837504, 2130837558, 2130837549 };
  static final int RETURN_BACK = 0;
  static final int RETURN_DISABLE_SHOP = 900;
  static final int RETURN_DISABLE_SHOP_BACK = 901;
  static final int RETURN_EXIT = 1;
  static final int RETURN_FREECOIN = 103;
  static final int RETURN_GAME = 102;
  static final int RETURN_ITEM = 100;
  static final int RETURN_ITEM_USE = 300;
  static final int RETURN_RANKING = 104;
  static final int RETURN_SHOP = 101;
  static final int RETURN_SHOP_BUY1 = 200;
  static final int RETURN_SHOP_BUY2 = 201;
  static final int RETURN_SHOP_BUY3 = 202;
  static final int RETURN_SHOP_BUY4 = 203;
  static final int RETURN_SHOP_WALL_15 = 204;
  static final int RETURN_SHOP_WALL_60 = 205;
  static final int RETURN_STONE = 105;
  static final int RETURN_UNKNOWN = 999;
  static final int SHOP = 2;
  static boolean initSoundManager = false;
  private static Context mContext;
  private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

  public GameRenderer(Context paramContext)
  {
    mContext = paramContext;
    sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
  }

  public static native void destroy();

  public static native int drawFrame();

  public static void errorCallback(int paramInt, String paramString)
  {
    ((GameActivity)mContext).onNativeError(paramInt, paramString);
  }

  public static native int getState();

  private void loadTexture(GL10 paramGL10)
  {
    ((GameActivity)mContext).startProgress();
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    paramGL10.glShadeModel(7424);
    paramGL10.glHint(3153, 4353);
    paramGL10.glHint(3155, 4353);
    int[] arrayOfInt = new int[1 + RESOURCE_IDS.length];
    paramGL10.glGenTextures(arrayOfInt.length, arrayOfInt, 0);
    Resources localResources = mContext.getResources();
    BitmapFactory.Options localOptions = new BitmapFactory.Options();
    localOptions.inScaled = false;
    for (int i = 0; ; i++)
    {
      if (i >= RESOURCE_IDS.length)
      {
        ((GameActivity)mContext).dismissProgress();
        return;
      }
      Bitmap localBitmap = BitmapFactory.decodeResource(localResources, RESOURCE_IDS[i], localOptions);
      paramGL10.glBindTexture(3553, arrayOfInt[i]);
      paramGL10.glTexParameterf(3553, 10241, 9729.0F);
      paramGL10.glTexParameterf(3553, 10240, 9729.0F);
      paramGL10.glTexParameterf(3553, 10242, 33071.0F);
      paramGL10.glTexParameterf(3553, 10243, 33071.0F);
      int j = localBitmap.getWidth();
      int k = localBitmap.getHeight();
      GLUtils.texImage2D(3553, 0, localBitmap, 0);
      localBitmap.recycle();
      setTextureInfo(i, arrayOfInt[i], j, k);
    }
  }

  public static native void setState(int paramInt);

  public static native void setTextureInfo(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public static native void surfaceChanged(int paramInt1, int paramInt2);

  public static native void surfaceCreated();

  public static void touchCallback(int paramInt)
  {
    switch (paramInt)
    {
    default:
    case 101:
    case 105:
    case 103:
    case 300:
    }
    while (true)
    {
      Coins7Log.e("GameRenderer", "result: " + paramInt);
      return;
      ((GameActivity)mContext).moveToShop(paramInt);
      continue;
      ((GameActivity)mContext).moveToTapjoy();
      continue;
      ((GameActivity)mContext).useItem();
    }
  }

  public static native void touchDown(float paramFloat1, float paramFloat2);

  public static native void touchMove(float paramFloat1, float paramFloat2);

  public static native int touchUp(float paramFloat1, float paramFloat2);

  public void onDestroy()
  {
    destroy();
  }

  public void onDrawFrame(GL10 paramGL10)
  {
    if (drawFrame() == 0)
      if (!GameActivity.isShowAdMob)
        ((GameActivity)mContext).showAdMob(true);
    while (!GameActivity.isShowAdMob)
      return;
    ((GameActivity)mContext).showAdMob(false);
  }

  public void onSurfaceChanged(GL10 paramGL10, int paramInt1, int paramInt2)
  {
    surfaceChanged(paramInt1, paramInt2);
  }

  public void onSurfaceCreated(GL10 paramGL10, EGLConfig paramEGLConfig)
  {
    SoundManager.setCallback();
    loadTexture(paramGL10);
    surfaceCreated();
    ((GameActivity)mContext).onRendererCreated();
  }

  public boolean onTouch(View paramView, MotionEvent paramMotionEvent)
  {
    switch (paramMotionEvent.getAction())
    {
    default:
    case 0:
    case 2:
    case 1:
    }
    while (true)
    {
      return true;
      touchDown(paramMotionEvent.getX(), paramMotionEvent.getY());
      continue;
      touchMove(paramMotionEvent.getX(), paramMotionEvent.getY());
      continue;
      touchUp(paramMotionEvent.getX(), paramMotionEvent.getY());
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameRenderer
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */