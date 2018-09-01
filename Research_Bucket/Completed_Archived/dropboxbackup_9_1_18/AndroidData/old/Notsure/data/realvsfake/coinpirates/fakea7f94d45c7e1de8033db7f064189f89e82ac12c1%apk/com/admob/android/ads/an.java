package com.admob.android.ads;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public final class an extends Animation
{
  private final float[] a;
  private final float[] b;
  private final float c;
  private final float d;
  private final float e;
  private final boolean f;
  private Camera g;

  public an(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, boolean paramBoolean)
  {
    this(new float[] { 0.0F, paramFloat1, 0.0F }, new float[] { 0.0F, paramFloat2, 0.0F }, paramFloat3, paramFloat4, paramFloat5, paramBoolean);
  }

  public an(float[] paramArrayOfFloat1, float[] paramArrayOfFloat2, float paramFloat1, float paramFloat2, float paramFloat3, boolean paramBoolean)
  {
    this.a = paramArrayOfFloat1;
    this.b = paramArrayOfFloat2;
    this.c = paramFloat1;
    this.d = paramFloat2;
    this.e = paramFloat3;
    this.f = paramBoolean;
  }

  protected final void applyTransformation(float paramFloat, Transformation paramTransformation)
  {
    if ((paramFloat < 0.0D) || (paramFloat > 1.0D))
    {
      paramTransformation.setTransformationType(Transformation.TYPE_IDENTITY);
      return;
    }
    float[] arrayOfFloat1 = this.a;
    float[] arrayOfFloat2 = this.b;
    float[] arrayOfFloat3 = new float[3];
    for (int i = 0; i < 3; i++)
      arrayOfFloat1[i] += paramFloat * (arrayOfFloat2[i] - arrayOfFloat1[i]);
    float f1 = this.c;
    float f2 = this.d;
    Camera localCamera = this.g;
    Matrix localMatrix = paramTransformation.getMatrix();
    localCamera.save();
    if (this.f)
      localCamera.translate(0.0F, 0.0F, paramFloat * this.e);
    while (true)
    {
      localCamera.rotateX(arrayOfFloat3[0]);
      localCamera.rotateY(arrayOfFloat3[1]);
      localCamera.rotateZ(arrayOfFloat3[2]);
      localCamera.getMatrix(localMatrix);
      localCamera.restore();
      localMatrix.preTranslate(-f1, -f2);
      localMatrix.postTranslate(f1, f2);
      paramTransformation.setTransformationType(Transformation.TYPE_MATRIX);
      return;
      localCamera.translate(0.0F, 0.0F, this.e * (1.0F - paramFloat));
    }
  }

  public final void initialize(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.initialize(paramInt1, paramInt2, paramInt3, paramInt4);
    this.g = new Camera();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.an
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */