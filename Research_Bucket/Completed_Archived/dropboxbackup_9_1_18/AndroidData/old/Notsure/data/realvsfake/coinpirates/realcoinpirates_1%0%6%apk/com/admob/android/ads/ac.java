package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.VideoView;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public final class ac extends ab
  implements ae
{
  ViewGroup d;
  VideoView e;
  d f;
  int g;
  boolean h;
  boolean i = false;
  boolean j = false;
  aa k;
  private long l;
  private Button m;
  private Runnable n;
  private boolean o;
  private b p;
  private WeakReference<Activity> q;
  private MediaController r;

  public ac(Context paramContext, WeakReference<Activity> paramWeakReference)
  {
    super(paramContext);
    this.q = paramWeakReference;
    this.n = new c(this);
    this.h = false;
    this.i = false;
    this.j = false;
  }

  private void a(Context paramContext)
  {
    p localp = this.c.h;
    this.e = new VideoView(paramContext);
    a locala = new a(this);
    this.e.setOnPreparedListener(locala);
    this.e.setOnCompletionListener(locala);
    this.e.setVideoPath(localp.a);
    this.e.setBackgroundDrawable(null);
    this.e.setOnErrorListener(locala);
    RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(-1, -1);
    localLayoutParams.addRule(13);
    addView(this.e, localLayoutParams);
    if (this.k != null)
      this.k.b();
  }

  public static void a(View paramView)
  {
    AlphaAnimation localAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
    localAlphaAnimation.setDuration(1000L);
    localAlphaAnimation.setFillAfter(true);
    paramView.startAnimation(localAlphaAnimation);
    paramView.invalidate();
  }

  public static void b(View paramView)
  {
    AlphaAnimation localAlphaAnimation = new AlphaAnimation(1.0F, 0.0F);
    localAlphaAnimation.setDuration(1000L);
    localAlphaAnimation.setFillAfter(true);
    paramView.startAnimation(localAlphaAnimation);
    paramView.invalidate();
  }

  private void b(boolean paramBoolean)
  {
    this.o = paramBoolean;
    if (!paramBoolean)
      g();
  }

  private void g()
  {
    if (this.p != null)
    {
      this.a.removeCallbacks(this.p);
      this.p = null;
    }
  }

  private void h()
  {
    if (this.c.h.c())
    {
      Context localContext = getContext();
      this.d = new RelativeLayout(localContext);
      ImageView localImageView = new ImageView(localContext);
      Hashtable localHashtable = this.c.b();
      if (localHashtable != null)
      {
        Bitmap localBitmap = (Bitmap)localHashtable.get(this.c.h.f);
        if (localBitmap != null)
        {
          BitmapDrawable localBitmapDrawable = new BitmapDrawable(localBitmap);
          float f1 = getResources().getDisplayMetrics().density;
          localImageView.setImageDrawable(localBitmapDrawable);
          RelativeLayout.LayoutParams localLayoutParams1 = new RelativeLayout.LayoutParams(j.a(localBitmap.getWidth(), f1), j.a(localBitmap.getHeight(), f1));
          localLayoutParams1.addRule(13);
          this.d.addView(localImageView, localLayoutParams1);
          this.d.setBackgroundColor(0);
          this.d.setVisibility(4);
          RelativeLayout.LayoutParams localLayoutParams2 = new RelativeLayout.LayoutParams(-1, -1);
          addView(this.d, localLayoutParams2);
        }
      }
      this.l = System.currentTimeMillis();
    }
  }

  public final void a()
  {
    if (this.h)
    {
      this.a.post(new f(this));
      return;
    }
    g localg = new g(this);
    long l1 = System.currentTimeMillis() - this.l;
    long l2 = (int)(1000.0D * this.c.h.g);
    if (l2 > l1)
    {
      this.a.postDelayed(localg, l2 - l1);
      return;
    }
    this.a.post(localg);
  }

  public final void a(Configuration paramConfiguration)
  {
    this.g = paramConfiguration.orientation;
    if ((this.k != null) && (e()))
    {
      if ((this.g == 2) && (this.k.b))
        this.k.a();
      while ((this.k.b) || (this.g != 1))
        return;
      this.k.b();
      return;
    }
    this.a.removeCallbacks(this.n);
  }

  public final void a(boolean paramBoolean)
  {
    this.a.removeCallbacks(this.n);
    if (this.d == null)
      h();
    if (this.d != null)
      a(this.d);
    if (this.k != null)
    {
      aa localaa1 = this.k;
      Context localContext = getContext();
      r localr = this.c;
      float f1 = this.b;
      if (localaa1.a == null)
      {
        RelativeLayout localRelativeLayout = new RelativeLayout(localContext);
        Button localButton = new Button(localContext);
        localButton.setTextColor(-1);
        localButton.setOnClickListener(new h(this));
        BitmapDrawable localBitmapDrawable = new BitmapDrawable((Bitmap)localr.b().get(localr.h.l));
        localBitmapDrawable.setBounds(0, 0, (int)(134.0F * f1), (int)(134.0F * f1));
        localButton.setWidth((int)(134.0F * f1));
        localButton.setHeight(134);
        localButton.setBackgroundDrawable(localBitmapDrawable);
        RelativeLayout.LayoutParams localLayoutParams1 = new RelativeLayout.LayoutParams((int)(134.0F * f1), (int)(134.0F * f1));
        localLayoutParams1.addRule(13);
        localRelativeLayout.addView(localButton, localLayoutParams1);
        localRelativeLayout.setOnClickListener(new h(this));
        TextView localTextView = new TextView(localContext);
        localTextView.setTextColor(-1);
        localTextView.setTypeface(Typeface.DEFAULT_BOLD);
        localTextView.setText("Replay");
        localTextView.setPadding(0, 0, 0, (int)(14.0F * f1));
        RelativeLayout.LayoutParams localLayoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        localLayoutParams2.addRule(12);
        localLayoutParams2.addRule(14);
        localRelativeLayout.addView(localTextView, localLayoutParams2);
        localaa1.a = new x(localContext, localRelativeLayout, 134, 134, (Bitmap)localr.b().get(localr.h.k));
        localaa1.a.setOnClickListener(new h(this));
        localaa1.a.setVisibility(4);
        RelativeLayout.LayoutParams localLayoutParams3 = new RelativeLayout.LayoutParams((int)(134.0F * f1), (int)(134.0F * f1));
        localLayoutParams3.addRule(13);
        addView(localaa1.a, localLayoutParams3);
      }
      if (paramBoolean)
      {
        aa localaa2 = this.k;
        if (localaa2.a != null)
        {
          localaa2.a.bringToFront();
          a(localaa2.a);
        }
      }
      if (!this.k.b)
        this.k.b();
    }
    if ((this.o) && (this.p == null))
    {
      this.p = new b(this);
      this.a.postDelayed(this.p, 7500L);
    }
  }

  public final void b()
  {
    if (this.d != null)
      b(this.d);
    if (this.m != null)
      b(this.m);
    if ((this.k != null) && (!this.k.b))
      this.k.b();
    if (this.k != null)
    {
      aa localaa = this.k;
      if (localaa.a != null)
        b(localaa.a);
    }
    invalidate();
    if ((this.g == 2) && (this.k != null) && (this.k.b))
      this.a.postDelayed(this.n, 3000L);
    this.a.postDelayed(new f(this), 1000L);
  }

  public final void c()
  {
    f();
    boolean bool = this.i;
    HashMap localHashMap = null;
    if (bool)
    {
      localHashMap = new HashMap();
      localHashMap.put("event", "completed");
    }
    this.f.a("done", localHashMap);
    d();
  }

  void d()
  {
    if (this.q != null)
    {
      Activity localActivity = (Activity)this.q.get();
      if (localActivity != null)
        localActivity.finish();
    }
  }

  boolean e()
  {
    return (this.e != null) && (this.e.isPlaying());
  }

  void f()
  {
    if (this.e != null)
    {
      this.e.stopPlayback();
      this.e.setVisibility(4);
      removeView(this.e);
      this.e = null;
    }
  }

  protected final void onAttachedToWindow()
  {
    this.p = null;
    if (this.c == null)
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "openerInfo is null");
    p localp;
    do
    {
      return;
      b(this.c.l);
      localp = this.c.h;
      if (localp != null)
        break;
    }
    while (!InterstitialAd.c.a("AdMobSDK", 6));
    Log.e("AdMobSDK", "movieInfo is null");
    return;
    Context localContext = getContext();
    if (AdManager.getOrientation(localContext) == "l")
    {
      this.g = 2;
      this.f = new d(this.c.j, AdManager.getPublisherId(localContext), this.c.i, AdManager.getUserId(localContext));
      this.f.a("video", null);
      a(localContext);
      if (!this.c.l)
        break label418;
    }
    label418: for (String str1 = "Skip"; ; str1 = "Done")
    {
      String str2 = t.a(str1);
      if (localp.c())
      {
        h();
        if (this.d != null)
          a(this.d);
        if ((!localp.j) || (!localp.c()))
        {
          this.m = new Button(localContext);
          this.m.setOnClickListener(new i(this, true));
          this.m.setBackgroundResource(17301509);
          this.m.setTextSize(13.0F);
          this.m.setText(str2);
          this.m.setVisibility(4);
          RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams((int)(54.0F * this.b), (int)(36.0F * this.b));
          localLayoutParams.addRule(11);
          localLayoutParams.addRule(12);
          localLayoutParams.setMargins(0, 0, (int)(2.0F * this.b), (int)(8.0F * this.b));
          addView(this.m, localLayoutParams);
          a(this.m);
        }
      }
      if ((localp.c != 2) || (localp.m == null) || (localp.m.size() <= 0))
        break label425;
      this.k = new aa();
      this.k.a(localContext, str2, localp, this.b, this, this.c, this.q);
      return;
      this.g = 1;
      break;
    }
    label425: if (localp.c == 0);
    for (boolean bool = true; ; bool = false)
    {
      Activity localActivity = (Activity)this.q.get();
      if ((localActivity == null) || (this.e == null))
        break;
      this.r = new MediaController(localActivity, bool);
      this.r.setAnchorView(this.e);
      this.e.setMediaController(this.r);
      return;
    }
  }

  static final class a
    implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener
  {
    private WeakReference<ac> a;

    public a(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void onCompletion(MediaPlayer paramMediaPlayer)
    {
      ac localac = (ac)this.a.get();
      if (localac != null)
      {
        localac.i = true;
        localac.f();
        localac.a(true);
      }
    }

    public final boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2)
    {
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "error playing video, what: " + paramInt1 + ", extra: " + paramInt2);
      ac localac = (ac)this.a.get();
      if (localac == null)
        return false;
      localac.c();
      return true;
    }

    public final void onPrepared(MediaPlayer paramMediaPlayer)
    {
      ac localac = (ac)this.a.get();
      if (localac != null)
        localac.a();
    }
  }

  static final class b
    implements Runnable
  {
    private WeakReference<ac> a;

    public b(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void run()
    {
      ac localac = (ac)this.a.get();
      if (localac != null)
      {
        ac.a(localac);
        localac.d();
      }
    }
  }

  static final class c
    implements Runnable
  {
    private WeakReference<ac> a;

    public c(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void run()
    {
      ac localac = (ac)this.a.get();
      if (localac == null);
      while ((!localac.e()) || (localac.g != 2) || (localac.k == null))
        return;
      localac.k.a();
    }
  }

  public static final class d
    implements View.OnTouchListener
  {
    private WeakReference<ac> a;

    public d(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final boolean onTouch(View paramView, MotionEvent paramMotionEvent)
    {
      ac localac = (ac)this.a.get();
      if (localac == null)
        return false;
      ac.a(localac, false);
      ac.a(localac, paramMotionEvent);
      return false;
    }
  }

  public static final class e
    implements View.OnClickListener
  {
    private WeakReference<ac> a;
    private WeakReference<o> b;
    private WeakReference<Activity> c;

    public e(ac paramac, o paramo, WeakReference<Activity> paramWeakReference)
    {
      this.a = new WeakReference(paramac);
      this.b = new WeakReference(paramo);
      this.c = paramWeakReference;
    }

    public final void onClick(View paramView)
    {
      ac localac = (ac)this.a.get();
      if (localac == null);
      o localo;
      do
      {
        return;
        ac.a(localac, false);
        localo = (o)this.b.get();
      }
      while (localo == null);
      Context localContext = localac.getContext();
      HashMap localHashMap;
      if (!localac.j)
      {
        localac.j = true;
        localHashMap = new HashMap();
        localHashMap.put("event", "interaction");
      }
      while (true)
      {
        q localq;
        while (true)
        {
          localac.f.a(localo.e, localHashMap);
          boolean bool = localac.e();
          if (bool)
            localac.f();
          localac.a(bool);
          localq = new q();
          try
          {
            localq.a(localContext, new JSONObject(localo.f), null);
            localq.b();
            if (this.c == null)
              break;
            Activity localActivity = (Activity)this.c.get();
            if (localActivity == null)
              break;
            localq.a(localActivity, localac);
            return;
          }
          catch (JSONException localJSONException)
          {
            while (true)
              if (InterstitialAd.c.a("AdMobSDK", 6))
                Log.e("AdMobSDK", "Could not create JSONObject from button click", localJSONException);
          }
        }
        localHashMap = null;
      }
    }
  }

  static final class f
    implements Runnable
  {
    private WeakReference<ac> a;

    public f(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void run()
    {
      ac localac = (ac)this.a.get();
      if (localac == null);
      while (localac.e == null)
        return;
      localac.e.setVisibility(0);
      localac.e.requestLayout();
      localac.e.requestFocus();
      localac.e.start();
    }
  }

  static final class g
    implements Runnable
  {
    private WeakReference<ac> a;

    public g(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void run()
    {
      ac localac = (ac)this.a.get();
      if (localac == null)
        return;
      localac.b();
    }
  }

  public static final class h
    implements View.OnClickListener
  {
    private WeakReference<ac> a;

    public h(ac paramac)
    {
      this.a = new WeakReference(paramac);
    }

    public final void onClick(View paramView)
    {
      ac localac = (ac)this.a.get();
      if (localac == null)
        return;
      localac.f.a("replay", null);
      if (localac.d != null)
        ac.b(localac.d);
      ac.a(localac, false);
      localac.h = true;
      ac.a(localac, localac.getContext());
    }
  }

  public static final class i
    implements View.OnClickListener
  {
    private WeakReference<ac> a;
    private boolean b;

    public i(ac paramac, boolean paramBoolean)
    {
      this.a = new WeakReference(paramac);
      this.b = paramBoolean;
    }

    public final void onClick(View paramView)
    {
      ac localac = (ac)this.a.get();
      if (localac == null)
        return;
      if (this.b)
        localac.f.a("skip", null);
      localac.c();
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */