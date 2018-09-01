package com.nubee.coinpirates.common;

public class GetBitmapOptions
{
  CacheOption cacheOption;
  int height;
  ScaleOption scaleOption;
  int width;

  public GetBitmapOptions(CacheOption paramCacheOption)
  {
    this.cacheOption = paramCacheOption;
    this.scaleOption = ScaleOption.None;
  }

  public GetBitmapOptions(CacheOption paramCacheOption, ScaleOption paramScaleOption)
  {
    this.cacheOption = paramCacheOption;
    if ((paramScaleOption == ScaleOption.Fit) || (paramScaleOption == ScaleOption.Aspect))
      paramScaleOption = ScaleOption.None;
    this.scaleOption = paramScaleOption;
  }

  public GetBitmapOptions(CacheOption paramCacheOption, ScaleOption paramScaleOption, int paramInt1, int paramInt2)
  {
    this.cacheOption = paramCacheOption;
    if (((paramScaleOption == ScaleOption.Fit) || (paramScaleOption == ScaleOption.Aspect)) && (paramInt1 <= 0) && (paramInt2 <= 0))
      paramScaleOption = ScaleOption.None;
    this.scaleOption = paramScaleOption;
    this.width = paramInt1;
    this.height = paramInt2;
  }

  public static enum CacheOption
  {
    static
    {
      Auto = new CacheOption("Auto", 1);
      Force = new CacheOption("Force", 2);
      CacheOption[] arrayOfCacheOption = new CacheOption[3];
      arrayOfCacheOption[0] = None;
      arrayOfCacheOption[1] = Auto;
      arrayOfCacheOption[2] = Force;
    }
  }

  public static enum ScaleOption
  {
    static
    {
      Density = new ScaleOption("Density", 1);
      Fit = new ScaleOption("Fit", 2);
      Aspect = new ScaleOption("Aspect", 3);
      ScaleOption[] arrayOfScaleOption = new ScaleOption[4];
      arrayOfScaleOption[0] = None;
      arrayOfScaleOption[1] = Density;
      arrayOfScaleOption[2] = Fit;
      arrayOfScaleOption[3] = Aspect;
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.GetBitmapOptions
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */