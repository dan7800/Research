package com.nubee.coinpirates.common;

public enum GetBitmapOptions$CacheOption
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

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.GetBitmapOptions.CacheOption
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */