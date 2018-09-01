package com.nubee.coinpirates.common;

public enum GetBitmapOptions$ScaleOption
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

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.GetBitmapOptions.ScaleOption
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */